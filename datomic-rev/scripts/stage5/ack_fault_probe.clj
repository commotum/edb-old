(ns stage5.ack-fault-probe
  "Focused fault cut at PostgreSQL log-root publication for the recovered pair.

  crash-window creates a fresh database, seeds a baseline, holds a PostgreSQL
  row lock on the authoritative pod-log-tail/<db-id> row, and submits one
  asynchronous transaction.  It emits STAGE5-ACK-BLOCKED only after proving
  that the Transactor writer is waiting on that exact UPDATE, an immutable tail
  row is committed, the authoritative row is byte-identical to the baseline,
  and the Peer Future is incomplete.  The caller must then kill the owned
  Transactor, abort its exact lock-blocked PostgreSQL session while the holder
  still owns the row lock, and write TRANSACTOR_KILLED followed by a newline to
  TOKEN_FILE.  Aborting that server-side session is necessary because a backend
  asleep in PostgreSQL's lock manager need not notice the dead JVM socket until
  after the lock is released.

  recover runs through a fresh Peer after Transactor restart.  It proves the
  interrupted transaction is absent, submits one recovery transaction, and
  observes the authoritative root advanced after the normal return.  audit runs
  through another fresh Peer and verifies the exact final projection.

  This probe establishes no-success-before-authoritative-publication for the
  injected pre-publication crash cut.  It does not establish how a client
  resolves a crash after publication but before result delivery.

    clojure.main -m stage5.ack-fault-probe crash-window
      URI JDBC_URL USER PASSWORD EXPECTED_CREATED EXPECTED_SOURCE_PROTOCOL
      TOKEN_FILE
    clojure.main -m stage5.ack-fault-probe recover
      URI JDBC_URL USER PASSWORD EXPECTED_SOURCE_PROTOCOL EXPECTED_DATABASE_ID
      EXPECTED_BASELINE_SHA EXPECTED_BASELINE_BASIS
      EXPECTED_PRECRASH_ROOT_SHA
    clojure.main -m stage5.ack-fault-probe audit
      URI JDBC_URL USER PASSWORD EXPECTED_SOURCE_PROTOCOL EXPECTED_DATABASE_ID
      EXPECTED_FINAL_SHA EXPECTED_FINAL_BASIS"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [datomic.api :as d])
  (:import [datomic Database Datom]
           [java.io BufferedReader File]
           [java.math BigInteger]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]
           [java.sql Connection DriverManager ResultSet]
           [java.util.concurrent CancellationException ExecutionException Future
            ThreadFactory TimeUnit TimeoutException]
           [java.util.concurrent.atomic AtomicLong]))

(set! *warn-on-reflection* true)

(def ^:private blocked-prefix "STAGE5-ACK-BLOCKED ")
(def ^:private fault-result-prefix "STAGE5-ACK-FAULT-RESULT ")
(def ^:private recover-result-prefix "STAGE5-ACK-RECOVER ")
(def ^:private audit-prefix "STAGE5-ACK-AUDIT ")
(def ^:private error-prefix "STAGE5-ACK-ERROR ")
(def ^:private connect-timeout-ms 30000)
(def ^:private transaction-timeout-ms 30000)
(def ^:private lock-wait-timeout-ms 20000)
(def ^:private backend-exit-timeout-ms 10000)
(def ^:private failed-future-observation-ms 5000)
(def ^:private cleanup-timeout-ms 15000)
(def ^:private expected-kill-token "TRANSACTOR_KILLED")
(def ^:private baseline-id "baseline")
(def ^:private fault-id "fault-before-publication")
(def ^:private recovery-id "recovery-after-crash")

(defn- fail!
  [message data]
  (throw (ex-info message (assoc data ::error :probe-failed))))

(defn- ensure!
  [pred message data]
  (when-not pred
    (fail! message data)))

(defn- sha256
  [value]
  (let [bytes (.digest (MessageDigest/getInstance "SHA-256")
                       (.getBytes (pr-str value) StandardCharsets/UTF_8))]
    (format "%064x" (BigInteger. 1 bytes))))

(defn- bytes->hex
  [bytes]
  (when bytes
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes))))

(defn- deadline-after-ms
  [milliseconds]
  (+ (System/nanoTime) (* 1000000 (long milliseconds))))

(defn- deadline-open?
  [deadline]
  (pos? (- (long deadline) (System/nanoTime))))

(defn- remaining-nanos
  [deadline]
  (max 0 (- (long deadline) (System/nanoTime))))

(defn- daemon-thread-factory
  [prefix]
  (let [counter (AtomicLong.)]
    (reify ThreadFactory
      (newThread [_ runnable]
        (doto (Thread. ^Runnable runnable
                       (str prefix "-" (.incrementAndGet counter)))
          (.setDaemon true))))))

(defn- bounded-call!
  [milliseconds label f]
  (let [executor (java.util.concurrent.Executors/newSingleThreadExecutor
                   (daemon-thread-factory (str "stage5-ack-" (name label))))
        task (.submit executor
                      ^java.util.concurrent.Callable
                      (reify java.util.concurrent.Callable
                        (call [_] (f))))]
    (try
      (try
        (.get ^Future task (long milliseconds) TimeUnit/MILLISECONDS)
        (catch TimeoutException _
          (.cancel ^Future task true)
          (fail! "Bounded operation timed out" {:operation label})))
      (catch ExecutionException exception
        (throw (or (.getCause exception) exception)))
      (finally
        (.shutdownNow executor)
        (try
          (.awaitTermination executor 2 TimeUnit/SECONDS)
          (catch InterruptedException _
            (.interrupt (Thread/currentThread))))))))

(defn- connect!
  [uri]
  (bounded-call! connect-timeout-ms :connect #(d/connect uri)))

(defn- release!
  [connection]
  (when connection
    (bounded-call! cleanup-timeout-ms :release #(d/release connection)))
  nil)

(defn- transact-before!
  [connection tx-data label]
  (let [future ^Future (d/transact-async connection tx-data)]
    (try
      (.get future transaction-timeout-ms TimeUnit/MILLISECONDS)
      (catch TimeoutException _
        (.cancel future true)
        (fail! "Transaction timed out" {:operation label}))
      (catch ExecutionException exception
        (throw (or (.getCause exception) exception))))))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- error-profile
  [^Throwable throwable]
  (let [causes (cause-chain throwable)]
    (sorted-map
      :anomaly-categories
      (vec (keep #(some-> % ex-data :cognitect.anomalies/category) causes))
      :cause-classes (mapv #(.getName (class %)) causes)
      :db-errors (vec (keep #(some-> % ex-data :db/error) causes))
      :exception-class (.getName (class throwable))
      :probe-errors (vec (keep #(some-> % ex-data ::error) causes)))))

(defn- capture
  [f]
  (try
    (sorted-map :returned (f))
    (catch Throwable throwable
      (sorted-map :thrown throwable))))

(defn- classpath-entries
  []
  (str/split (System/getProperty "java.class.path")
             (re-pattern
               (java.util.regex.Pattern/quote File/pathSeparator))))

(defn- forbidden-classpath-entry?
  [entry]
  (let [name (.getName (io/file entry))]
    (or (str/includes? entry "*")
        (boolean (re-matches #"(?i)peer-[^/]*[.]jar" name))
        (boolean (re-matches #"(?i)core2-[^/]*[.]jar" name))
        (boolean
          (re-matches #"(?i)datomic-transactor[^/]*[.]jar" name)))))

(defn- audit-candidate-boundary!
  [expected-source-protocol]
  (ensure! (#{"jar" "file"} expected-source-protocol)
           "Expected source protocol must be jar or file"
           {:expected-source-protocol expected-source-protocol})
  (let [entries (classpath-entries)
        forbidden (vec (filter forbidden-classpath-entry? entries))
        api-source (io/resource "datomic/api.clj")
        peer-source (io/resource "datomic/peer.clj")
        core2-source (io/resource "datomic/core2/async.clj")
        api-aot (io/resource "datomic/api__init.class")
        peer-aot (io/resource "datomic/peer__init.class")
        core2-aot (io/resource "datomic/core2/async__init.class")]
    (ensure! (empty? forbidden)
             "Forbidden Datomic implementation is on the candidate classpath"
             {:forbidden-classpath-entry-count (count forbidden)})
    (doseq [[resource-name resource]
            [["datomic/api.clj" api-source]
             ["datomic/peer.clj" peer-source]
             ["datomic/core2/async.clj" core2-source]]]
      (ensure! resource
               "Recovered candidate source is not visible"
               {:resource resource-name})
      (ensure! (= expected-source-protocol
                  (.getProtocol ^java.net.URL resource))
               "Recovered candidate source protocol differs"
               {:expected-source-protocol expected-source-protocol
                :resource resource-name
                :source-protocol (.getProtocol ^java.net.URL resource)}))
    (ensure! (and (nil? api-aot) (nil? peer-aot) (nil? core2-aot))
             "Original Peer/core2 AOT implementation is visible"
             {:api-aot-visible? (boolean api-aot)
              :core2-aot-visible? (boolean core2-aot)
              :peer-aot-visible? (boolean peer-aot)})
    (sorted-map
      :api-source-protocol expected-source-protocol
      :core2-aot-visible? false
      :core2-source-protocol expected-source-protocol
      :forbidden-implementation-entry-count 0
      :peer-aot-visible? false
      :peer-source-protocol expected-source-protocol)))

(defn- schema-tx
  []
  [{:db/ident :stage5-ack/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage5-ack/kind
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/index true
    :db.install/_attribute :db.part/db}])

(defn- entity-present?
  [db id]
  (boolean (d/entid db [:stage5-ack/id id])))

(defn- fact-t
  [db entity-id]
  (let [eid (d/entid db [:stage5-ack/id entity-id])
        datoms (when eid (seq (d/datoms db :eavt eid :stage5-ack/id)))]
    (ensure! (= 1 (count datoms))
             "Expected exactly one identity fact"
             {:entity-id entity-id :fact-count (count datoms)})
    (d/tx->t (.tx ^Datom (first datoms)))))

(defn- probe-rows
  [db]
  (->> (d/q '[:find ?id ?kind
              :where
              [?e :stage5-ack/id ?id]
              [?e :stage5-ack/kind ?kind]]
            db)
       (map (fn [[id kind]] [id kind (fact-t db id)]))
       (sort-by first)
       vec))

(defn- canonical-state
  [^Database db]
  (sorted-map
    :basis-t (d/basis-t db)
    :database-id (str (.id db))
    :rows (probe-rows db)))

(defn- require-baseline-state!
  [^Database db expected-database-id expected-basis expected-sha]
  (let [canonical (canonical-state db)
        actual-sha (sha256 canonical)]
    (ensure! (= expected-database-id (str (.id db)))
             "Database identity differs from the crash baseline"
             {:actual (str (.id db)) :expected expected-database-id})
    (ensure! (= expected-basis (d/basis-t db))
             "Basis differs from the crash baseline"
             {:actual (d/basis-t db) :expected expected-basis})
    (ensure! (= expected-sha actual-sha)
             "Canonical state differs from the crash baseline"
             {:actual actual-sha :expected expected-sha})
    (ensure! (= [[baseline-id :stage5-ack/baseline (fact-t db baseline-id)]]
                (:rows canonical))
             "Crash baseline contains an unexpected probe entity"
             {:rows (:rows canonical)})
    (ensure! (not (entity-present? db fault-id))
             "Interrupted transaction is present in the baseline"
             {})
    canonical))

(defn- ^Connection open-sql!
  [jdbc-url user password]
  (Class/forName "org.postgresql.Driver")
  (DriverManager/getConnection jdbc-url user password))

(defn- result-row
  [^ResultSet result-set]
  (let [revision (.getLong result-set "rev")
        revision-null? (.wasNull result-set)
        value (.getBytes result-set "val")]
    (sorted-map
      :id (.getString result-set "id")
      :map (.getString result-set "map")
      :rev (when-not revision-null? revision)
      :val-hex (bytes->hex value))))

(defn- sql-row
  [jdbc-url user password id]
  (with-open [connection (open-sql! jdbc-url user password)
              statement (.prepareStatement
                          connection
                          (str "select id, rev, map, val "
                               "from public.datomic_kvs where id = ?"))]
    (.setString statement 1 id)
    (with-open [result-set (.executeQuery statement)]
      (ensure! (.next result-set)
               "PostgreSQL row is absent"
               {:id id})
      (let [row (result-row result-set)]
        (ensure! (not (.next result-set))
                 "PostgreSQL returned duplicate rows"
                 {:id id})
        row))))

(defn- sql-log-root
  [jdbc-url user password database-id]
  (sql-row jdbc-url user password (str "pod-log-tail/" database-id)))

(defn- sql-ids
  [jdbc-url user password]
  (with-open [connection (open-sql! jdbc-url user password)
              statement (.prepareStatement
                          connection
                          "select id from public.datomic_kvs order by id")
              result-set (.executeQuery statement)]
    (loop [ids []]
      (if (.next result-set)
        (recur (conj ids (.getString result-set "id")))
        ids))))

(defn- backend-pid
  [^Connection connection]
  (with-open [statement (.prepareStatement connection "select pg_backend_pid()")
              result-set (.executeQuery statement)]
    (ensure! (.next result-set)
             "PostgreSQL backend PID query returned no row"
             {})
    (.getInt result-set 1)))

(defn- lock-log-root!
  [^Connection holder database-id expected-root]
  (.setAutoCommit holder false)
  (let [holder-pid (backend-pid holder)
        root-id (str "pod-log-tail/" database-id)]
    (with-open [statement (.prepareStatement
                            holder
                            (str "select id, rev, map, val "
                                 "from public.datomic_kvs "
                                 "where id = ? for update"))]
      (.setString statement 1 root-id)
      (with-open [result-set (.executeQuery statement)]
        (ensure! (.next result-set)
                 "Authoritative log-root row is absent at lock acquisition"
                 {:id root-id})
        (let [locked-row (result-row result-set)]
          (ensure! (not (.next result-set))
                   "Lock query returned duplicate authoritative rows"
                   {:id root-id})
          (ensure! (= expected-root locked-row)
                   "Authoritative log root changed before its lock was acquired"
                   {:actual-sha256 (sha256 locked-row)
                    :expected-sha256 (sha256 expected-root)}))))
    (sorted-map :holder-backend-pid holder-pid :root-id root-id)))

(defn- normalize-query
  [value]
  (some-> value str/trim (str/replace #"\s+" " ")))

(defn- blocking-writers
  [jdbc-url user password holder-pid]
  (with-open [connection (open-sql! jdbc-url user password)
              statement
              (.prepareStatement
                connection
                (str "select pid, wait_event_type, wait_event, query, "
                     "pg_blocking_pids(pid)::text as blocking_pids "
                     "from pg_stat_activity "
                     "where datname = current_database() "
                     "and pid <> ? "
                     "and wait_event_type = 'Lock' "
                     "and lower(query) like '%update%datomic_kvs%' "
                     "and ? = any(pg_blocking_pids(pid)) "
                     "order by pid"))]
    (.setInt statement 1 holder-pid)
    (.setInt statement 2 holder-pid)
    (with-open [result-set (.executeQuery statement)]
      (loop [rows []]
        (if (.next result-set)
          (recur
            (conj rows
                  (sorted-map
                    :blocking-pids (.getString result-set "blocking_pids")
                    :pid (.getInt result-set "pid")
                    :query (normalize-query (.getString result-set "query"))
                    :wait-event (.getString result-set "wait_event")
                    :wait-event-type (.getString result-set
                                                 "wait_event_type"))))
          rows)))))

(defn- wait-for-blocking-writer!
  [jdbc-url user password holder-pid]
  (let [deadline (deadline-after-ms lock-wait-timeout-ms)]
    (loop []
      (let [writers (blocking-writers jdbc-url user password holder-pid)]
        (cond
          (= 1 (count writers)) (first writers)
          (> (count writers) 1)
          (fail! "More than one Transactor writer is blocked by the holder"
                 {:holder-backend-pid holder-pid
                  :writer-count (count writers)})
          (deadline-open? deadline)
          (do (Thread/sleep 25) (recur))
          :else
          (fail! "No authoritative PostgreSQL UPDATE became lock-blocked"
                 {:holder-backend-pid holder-pid}))))))

(defn- backend-present?
  [jdbc-url user password pid]
  (with-open [connection (open-sql! jdbc-url user password)
              statement (.prepareStatement
                          connection
                          "select exists(select 1 from pg_stat_activity where pid = ?)")]
    (.setInt statement 1 pid)
    (with-open [result-set (.executeQuery statement)]
      (ensure! (.next result-set)
               "PostgreSQL backend existence query returned no row"
               {:pid pid})
      (.getBoolean result-set 1))))

(defn- wait-for-backend-exit!
  [jdbc-url user password pid]
  (let [deadline (deadline-after-ms backend-exit-timeout-ms)]
    (loop []
      (cond
        (not (backend-present? jdbc-url user password pid)) true
        (deadline-open? deadline) (do (Thread/sleep 25) (recur))
        :else (fail! "Blocked Transactor PostgreSQL backend did not exit"
                     {:pid pid})))))

(defn- summarize-immutable-row
  [row]
  (sorted-map
    :id (:id row)
    :map (:map row)
    :rev (:rev row)
    :row-sha256 (sha256 row)
    :value-bytes (quot (count (or (:val-hex row) "")) 2)))

(defn- parse-row-map!
  [row label]
  (let [raw (:map row)
        parsed
        (try
          (when raw (edn/read-string raw))
          (catch Throwable throwable
            (fail! "PostgreSQL KV metadata is not readable EDN"
                   {:label label
                    :map-sha256 (sha256 raw)
                    :throwable-class (.getName (class throwable))})))]
    (ensure! (map? parsed)
             "PostgreSQL KV metadata is not a map"
             {:label label :map-present? (boolean raw)})
    parsed))

(defn- require-one-immutable-tail!
  [jdbc-url user password baseline-ids blocked-ids root-id baseline-root]
  (let [baseline-set (set baseline-ids)
        blocked-set (set blocked-ids)
        removed (vec (sort (set/difference baseline-set blocked-set)))
        added (vec (sort (set/difference blocked-set baseline-set)))
        baseline-root-map (parse-row-map! baseline-root :baseline-root)
        baseline-tail (:tail baseline-root-map)]
    (ensure! (empty? removed)
             "A PostgreSQL KV row disappeared in the blocked crash window"
             {:removed-ids removed})
    (ensure! (= 1 (count added))
             "Blocked write did not leave exactly one bounded immutable tail row"
             {:added-ids added :added-row-count (count added)})
    (ensure! (not= root-id (first added))
             "The added row unexpectedly names the authoritative root"
             {:added-id (first added)})
    (let [row (sql-row jdbc-url user password (first added))]
      (ensure! (nil? (:rev row))
               "The pre-publication tail row is unexpectedly revisioned"
               {:row (summarize-immutable-row row)})
      (ensure! (pos? (quot (count (or (:val-hex row) "")) 2))
               "The pre-publication tail row has no immutable value bytes"
               {:row (summarize-immutable-row row)})
      (let [row-map (parse-row-map! row :new-immutable-tail)]
        (ensure! (and (string? baseline-tail) (not (str/blank? baseline-tail)))
                 "Baseline root has no current tail identity"
                 {:baseline-root-map baseline-root-map})
        (ensure! (= {:prev baseline-tail} row-map)
                 "Blocked immutable row is not the submitted transaction append"
                 {:actual-map row-map
                  :expected-map {:prev baseline-tail}})
        [(assoc (summarize-immutable-row row)
                :append-prev baseline-tail
                :append-prev-matches-baseline-tail? true
                :metadata row-map)]))))

(defn- emit-marker!
  [prefix value]
  (println (str prefix (pr-str value)))
  (flush))

(defn- read-kill-token!
  [token-file]
  (with-open [reader ^BufferedReader (io/reader token-file)]
    (let [token (.readLine reader)]
      (ensure! (= expected-kill-token token)
               "Crash-window control token differs"
               {:actual token :expected expected-kill-token})))
  :received)

(defn- wait-for-future-terminal!
  [^Future future]
  (let [deadline (deadline-after-ms failed-future-observation-ms)]
    (loop []
      (cond
        (.isDone future)
        (let [outcome
              (try
                (sorted-map :returned (.get future 1 TimeUnit/MILLISECONDS))
                (catch ExecutionException exception
                  (sorted-map :thrown (or (.getCause exception) exception)))
                (catch CancellationException exception
                  (sorted-map :thrown exception))
                (catch TimeoutException exception
                  (sorted-map :thrown exception))
                (catch Throwable throwable
                  (sorted-map :thrown throwable)))]
          (ensure! (contains? outcome :thrown)
                   "Interrupted transaction Future reported success"
                   {:future-state :returned})
          (sorted-map
            :error-profile (error-profile (:thrown outcome))
            :state :failed))

        (deadline-open? deadline)
        (do (Thread/sleep 25) (recur))

        :else
        (let [cancelled? (.cancel future true)]
          (ensure! (or cancelled? (.isCancelled future))
                   "Pending interrupted transaction Future could not be cancelled"
                   {:done? (.isDone future)})
          (sorted-map
            :observation-milliseconds failed-future-observation-ms
            :state :pending-then-cancelled))))))

(defn- crash-window!
  [{:keys [expected-created expected-source-protocol jdbc-url password token-file
           uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        created? (boolean
                   (bounded-call! connect-timeout-ms
                                  :create-database
                                  #(d/create-database uri)))
        peer-connection (atom nil)
        holder (atom nil)
        holder-active? (atom false)
        transaction-future (atom nil)]
    (ensure! (= expected-created created?)
             "Unexpected create-database result"
             {:actual created? :expected expected-created})
    (try
      (reset! peer-connection (connect! uri))
      (transact-before! @peer-connection (schema-tx) :schema)
      (transact-before! @peer-connection
                        [{:stage5-ack/id baseline-id
                          :stage5-ack/kind :stage5-ack/baseline}]
                        :baseline)
      (let [baseline-db (d/db @peer-connection)
            database-id (str (.id ^Database baseline-db))
            baseline-basis (d/basis-t baseline-db)
            baseline-canonical (canonical-state baseline-db)
            baseline-sha (sha256 baseline-canonical)
            baseline-root (sql-log-root jdbc-url user password database-id)
            baseline-root-sha (sha256 baseline-root)
            baseline-ids (sql-ids jdbc-url user password)]
        (ensure! (= [[baseline-id :stage5-ack/baseline
                      (fact-t baseline-db baseline-id)]]
                    (:rows baseline-canonical))
                 "Fresh crash baseline contains unexpected probe rows"
                 {:rows (:rows baseline-canonical)})
        (ensure! (and (integer? (:rev baseline-root))
                      (not (neg? (:rev baseline-root))))
                 "Crash baseline log root has an invalid revision"
                 {:root-revision (:rev baseline-root)})
        (reset! holder (open-sql! jdbc-url user password))
        (let [{:keys [holder-backend-pid root-id]}
              (lock-log-root! @holder database-id baseline-root)]
          (reset! holder-active? true)
          (reset! transaction-future
                  (d/transact-async
                    @peer-connection
                    [{:stage5-ack/id fault-id
                      :stage5-ack/kind :stage5-ack/fault-before-publication}]))
          (let [writer (wait-for-blocking-writer!
                         jdbc-url user password holder-backend-pid)
                blocked-root (sql-log-root jdbc-url user password database-id)
                blocked-ids (sql-ids jdbc-url user password)
                orphan-rows (require-one-immutable-tail!
                              jdbc-url user password baseline-ids blocked-ids
                              root-id baseline-root)
                blocked-db (d/db @peer-connection)
                blocked-canonical (canonical-state blocked-db)
                fault-present? (entity-present? blocked-db fault-id)]
            (ensure! (= baseline-root blocked-root)
                     "Authoritative log root changed while its UPDATE was blocked"
                     {:actual-sha256 (sha256 blocked-root)
                      :expected-sha256 baseline-root-sha})
            (ensure! (= baseline-canonical blocked-canonical)
                     "Peer state advanced before authoritative publication"
                     {:actual-sha256 (sha256 blocked-canonical)
                      :expected-sha256 baseline-sha})
            (ensure! (not fault-present?)
                     "Interrupted transaction became visible before publication"
                     {})
            (ensure! (not (.isDone ^Future @transaction-future))
                     "Transaction Future completed before authoritative publication"
                     {})
            (let [blocked-evidence
                  (sorted-map
                    :authoritative-root-id root-id
                    :baseline-basis-t baseline-basis
                    :baseline-canonical-sha256 baseline-sha
                    :baseline-root-revision (:rev baseline-root)
                    :baseline-root-sha256 baseline-root-sha
                    :blocked-backend-pid (:pid writer)
                    :candidate-boundary boundary
                    :created? created?
                    :database-id database-id
                    :fault-sentinel-present? fault-present?
                    :future-done-while-blocked? false
                    :future-incomplete? true
                    :holder-backend-pid holder-backend-pid
                    :immutable-tail-rows orphan-rows
                    :orphan-candidate-count (count orphan-rows)
                    :peer-state-unchanged? true
                    :precrash-root-sha256 baseline-root-sha
                    :root-row-byte-identical? true
                    :transactor-backend-pid (:pid writer)
                    :transactor-blocking-pids (:blocking-pids writer)
                    :transactor-query (:query writer)
                    :transactor-wait-event (:wait-event writer)
                    :transactor-wait-event-type (:wait-event-type writer))]
              (emit-marker! blocked-prefix blocked-evidence)
              (read-kill-token! token-file)
              (wait-for-backend-exit!
                jdbc-url user password (:pid writer))
              (let [post-kill-locked-root
                    (sql-log-root jdbc-url user password database-id)]
                (ensure! (= baseline-root post-kill-locked-root)
                         "Killed writer changed the root before holder rollback"
                         {:actual-sha256 (sha256 post-kill-locked-root)
                          :expected-sha256 baseline-root-sha}))
              (.rollback ^Connection @holder)
              (reset! holder-active? false)
              (.close ^Connection @holder)
              (reset! holder nil)
              (let [future-state (wait-for-future-terminal!
                                   ^Future @transaction-future)
                    final-root (sql-log-root
                                 jdbc-url user password database-id)
                    final-ids (sql-ids jdbc-url user password)
                    final-db (d/db @peer-connection)
                    final-canonical (canonical-state final-db)]
                (ensure! (= baseline-root final-root)
                         "Authoritative root changed after killed-writer rollback"
                         {:actual-sha256 (sha256 final-root)
                          :expected-sha256 baseline-root-sha})
                (ensure! (= blocked-ids final-ids)
                         "PostgreSQL KV membership changed after writer death"
                         {:after-count (count final-ids)
                          :blocked-count (count blocked-ids)})
                (ensure! (= baseline-canonical final-canonical)
                         "Peer basis or projection advanced after writer death"
                         {:actual-sha256 (sha256 final-canonical)
                          :expected-sha256 baseline-sha})
                (ensure! (not (entity-present? final-db fault-id))
                         "Interrupted transaction sentinel became visible"
                         {})
                (sorted-map
                  :authoritative-publication :not-committed
                  :baseline-basis-t baseline-basis
                  :baseline-canonical-sha256 baseline-sha
                  :baseline-root-revision (:rev baseline-root)
                  :baseline-root-sha256 baseline-root-sha
                  :blocked-backend-pid (:pid writer)
                  :candidate-boundary boundary
                  :created? created?
                  :database-id database-id
                  :fault-sentinel-absent? true
                  :fault-sentinel-present? false
                  :future-outcome future-state
                  :future-done-while-blocked? false
                  :holder-backend-pid holder-backend-pid
                  :immutable-orphan-row-count (count orphan-rows)
                  :immutable-orphan-rows orphan-rows
                  :no-success-before-authoritative-publication true
                  :orphan-candidate-count (count orphan-rows)
                  :peer-basis-unchanged? true
                  :precrash-root-sha256 baseline-root-sha
                  :root-row-byte-identical? true
                  :status :passed
                  :transactor-backend-exited? true))))))
      (finally
        (when (and @transaction-future
                   (not (.isDone ^Future @transaction-future)))
          (.cancel ^Future @transaction-future true))
        (when (and @holder @holder-active?)
          (try
            (.rollback ^Connection @holder)
            (catch Throwable _)))
        (when @holder
          (try
            (.close ^Connection @holder)
            (catch Throwable _)))
        (release! @peer-connection)))))

(defn- recover!
  [{:keys [expected-baseline-basis expected-baseline-sha expected-database-id
           expected-precrash-root-sha expected-source-protocol jdbc-url password
           uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        connection (connect! uri)]
    (try
      (let [baseline-db (d/db connection)
            baseline (require-baseline-state!
                       baseline-db expected-database-id
                       expected-baseline-basis expected-baseline-sha)
            root-before (sql-log-root jdbc-url user password
                                      expected-database-id)
            root-before-sha (sha256 root-before)
            report (transact-before!
                     connection
                     [{:stage5-ack/id recovery-id
                       :stage5-ack/kind :stage5-ack/recovery}]
                     :recovery-transaction)
            final-db (:db-after report)
            root-after (sql-log-root jdbc-url user password
                                     expected-database-id)
            final-canonical (canonical-state final-db)
            final-sha (sha256 final-canonical)
            recovery-t (fact-t final-db recovery-id)]
        (ensure! (= expected-baseline-basis
                    (d/basis-t (:db-before report)))
                 "Recovery report did not begin at the crash baseline"
                 {:actual (d/basis-t (:db-before report))
                  :expected expected-baseline-basis})
        (ensure! (> (d/basis-t final-db) expected-baseline-basis)
                 "Recovery transaction did not advance basis"
                 {:after (d/basis-t final-db)
                  :before expected-baseline-basis})
        (ensure! (= (d/basis-t final-db) recovery-t)
                 "Recovery sentinel transaction differs from report basis"
                 {:event-t recovery-t
                  :report-basis (d/basis-t final-db)})
        (ensure! (not (entity-present? final-db fault-id))
                 "Interrupted transaction appeared after restart"
                 {})
        (ensure! (entity-present? final-db baseline-id)
                 "Baseline sentinel disappeared after restart"
                 {})
        (ensure! (entity-present? final-db recovery-id)
                 "Recovery sentinel is absent"
                 {})
        (ensure! (and (integer? (:rev root-before))
                      (integer? (:rev root-after))
                      (> (:rev root-after) (:rev root-before))
                      (not= root-before root-after))
                 "Normally returned recovery transaction did not advance the root"
                 {:after-revision (:rev root-after)
                  :before-revision (:rev root-before)})
        (sorted-map
          :baseline-canonical-sha256 (sha256 baseline)
          :baseline-basis-t expected-baseline-basis
          :candidate-boundary boundary
          :database-id expected-database-id
          :fault-sentinel-absent? true
          :fault-sentinel-present? false
          :final-basis-t (d/basis-t final-db)
          :final-canonical-sha256 final-sha
          :post-return-root-observed-advanced? true
          :pre-recovery-root-matches-precrash?
          (= expected-precrash-root-sha root-before-sha)
          :pre-recovery-root-revision (:rev root-before)
          :pre-recovery-root-sha256 root-before-sha
          :precrash-root-sha256 expected-precrash-root-sha
          :recovery-event-t recovery-t
          :recovery-sentinel-present? true
          :root-revision-after (:rev root-after)
          :root-revision-before (:rev root-before)
          :root-sha256-after (sha256 root-after)
          :status :passed))
      (finally
        (release! connection)))))

(defn- require-final-state!
  [^Database db expected-database-id expected-basis expected-sha]
  (let [canonical (canonical-state db)
        actual-sha (sha256 canonical)
        rows (:rows canonical)]
    (ensure! (= expected-database-id (str (.id db)))
             "Audit database identity differs"
             {:actual (str (.id db)) :expected expected-database-id})
    (ensure! (= expected-basis (d/basis-t db))
             "Audit basis differs"
             {:actual (d/basis-t db) :expected expected-basis})
    (ensure! (= expected-sha actual-sha)
             "Audit canonical state differs"
             {:actual actual-sha :expected expected-sha})
    (ensure! (= #{baseline-id recovery-id} (set (map first rows)))
             "Audit probe identity set differs"
             {:actual (set (map first rows))})
    (ensure! (not (entity-present? db fault-id))
             "Interrupted transaction appeared in fresh audit"
             {})
    (ensure! (= expected-basis (fact-t db recovery-id))
             "Recovery fact transaction differs from final basis"
             {:event-t (fact-t db recovery-id)
              :final-basis expected-basis})
    canonical))

(defn- audit!
  [{:keys [expected-database-id expected-final-basis expected-final-sha
           expected-source-protocol jdbc-url password uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        connection (connect! uri)]
    (try
      (let [db (d/db connection)
            canonical (require-final-state!
                        db expected-database-id
                        expected-final-basis expected-final-sha)
            root (sql-log-root jdbc-url user password expected-database-id)]
        (ensure! (and (integer? (:rev root)) (not (neg? (:rev root))))
                 "Fresh audit PostgreSQL log root has an invalid revision"
                 {:root-revision (:rev root)})
        (sorted-map
          :candidate-boundary boundary
          :canonical-sha256 (sha256 canonical)
          :database-id expected-database-id
          :fault-sentinel-absent? true
          :fault-sentinel-present? false
          :final-basis-t expected-final-basis
          :final-canonical-sha256 (sha256 canonical)
          :recovery-sentinel-present? true
          :sql-log-root :present
          :sql-log-root-revision (:rev root)
          :sql-log-root-sha256 (sha256 root)
          :status :passed))
      (finally
        (release! connection)))))

(defn- require-uri!
  [uri]
  (ensure! (and (string? uri)
                (str/starts-with? uri "datomic:sql://")
                (str/includes? uri "jdbc:postgresql://"))
           "URI must name a PostgreSQL-backed Datomic SQL database"
           {:valid? false})
  uri)

(defn- require-jdbc-url!
  [jdbc-url]
  (ensure! (and (string? jdbc-url)
                (str/starts-with? jdbc-url "jdbc:postgresql://"))
           "JDBC URL must name PostgreSQL"
           {:valid? false})
  jdbc-url)

(defn- parse-boolean!
  [value]
  (ensure! (#{"true" "false"} value)
           "EXPECTED_CREATED must be true or false"
           {:value value})
  (= "true" value))

(defn- parse-source-protocol!
  [value]
  (ensure! (#{"jar" "file"} value)
           "EXPECTED_SOURCE_PROTOCOL must be jar or file"
           {:value value})
  value)

(defn- parse-sha!
  [label value]
  (ensure! (and value (re-matches #"[0-9a-f]{64}" value))
           "Expected lowercase SHA-256 argument"
           {:argument label})
  value)

(defn- parse-long!
  [label value]
  (try
    (let [parsed (Long/parseLong value)]
      (ensure! (not (neg? parsed))
               "Numeric argument must be nonnegative"
               {:argument label})
      parsed)
    (catch NumberFormatException _
      (fail! "Numeric argument is invalid" {:argument label}))))

(defn- parse-command
  [args]
  (let [mode (first args)]
    (case mode
      "crash-window"
      (let [[_ uri jdbc-url user password expected-created source-protocol
             token-file & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password expected-created
                              source-protocol token-file])
                      (empty? extra))
                 "crash-window requires exactly eight arguments"
                 {:argument-count (count args)})
        {:expected-created (parse-boolean! expected-created)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :token-file token-file
         :uri (require-uri! uri)
         :user user})

      "recover"
      (let [[_ uri jdbc-url user password source-protocol database-id
             baseline-sha baseline-basis precrash-root-sha & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password source-protocol
                              database-id baseline-sha baseline-basis
                              precrash-root-sha])
                      (empty? extra))
                 "recover requires exactly ten arguments"
                 {:argument-count (count args)})
        {:expected-baseline-basis
         (parse-long! :expected-baseline-basis baseline-basis)
         :expected-baseline-sha
         (parse-sha! :expected-baseline-sha baseline-sha)
         :expected-database-id database-id
         :expected-precrash-root-sha
         (parse-sha! :expected-precrash-root-sha precrash-root-sha)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :uri (require-uri! uri)
         :user user})

      "audit"
      (let [[_ uri jdbc-url user password source-protocol database-id
             final-sha final-basis & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password source-protocol
                              database-id final-sha final-basis])
                      (empty? extra))
                 "audit requires exactly nine arguments"
                 {:argument-count (count args)})
        {:expected-database-id database-id
         :expected-final-basis (parse-long! :expected-final-basis final-basis)
         :expected-final-sha (parse-sha! :expected-final-sha final-sha)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :uri (require-uri! uri)
         :user user})

      (fail! "Unknown Stage 5 acknowledgement-fault mode" {:mode mode}))))

(defn- run-command
  [{:keys [mode] :as command}]
  (case mode
    "crash-window" (crash-window! command)
    "recover" (recover! command)
    "audit" (audit! command)))

(defn- shutdown!
  []
  (try
    (bounded-call! cleanup-timeout-ms :peer-shutdown #(d/shutdown false))
    (finally
      (shutdown-agents)))
  :completed)

(defn -main
  [& args]
  (let [mode (first args)
        operation (capture #(run-command (parse-command args)))
        cleanup (capture shutdown!)
        successful? (and (contains? operation :returned)
                         (contains? cleanup :returned))]
    (if successful?
      (emit-marker!
        (case mode
          "crash-window" fault-result-prefix
          "recover" recover-result-prefix
          "audit" audit-prefix)
        (:returned operation))
      (do
        (binding [*out* *err*]
          (emit-marker!
            error-prefix
            (sorted-map
              :cleanup-error (some-> cleanup :thrown error-profile)
              :mode mode
              :operation-error (some-> operation :thrown error-profile))))
        (System/exit 1)))))

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
  injected pre-publication crash cut.  publication-window reuses the same
  deterministic root lock, but the controller freezes this Peer process,
  terminates the holder backend to allow publication, observes the root
  advance, crashes and restarts the Transactor, and only then resumes this
  process.  The fault transaction combines a unique sentinel with a CAS so a
  reconnect-side duplicate execution is observable.

    clojure.main -m stage5.ack-fault-probe crash-window
      URI JDBC_URL USER PASSWORD EXPECTED_CREATED EXPECTED_SOURCE_PROTOCOL
      TOKEN_FILE
    clojure.main -m stage5.ack-fault-probe publication-window
      URI JDBC_URL USER PASSWORD EXPECTED_CREATED EXPECTED_SOURCE_PROTOCOL
      TOKEN_FILE
    clojure.main -m stage5.ack-fault-probe recover
      URI JDBC_URL USER PASSWORD EXPECTED_SOURCE_PROTOCOL EXPECTED_DATABASE_ID
      EXPECTED_BASELINE_SHA EXPECTED_BASELINE_BASIS
      EXPECTED_PRECRASH_ROOT_SHA
    clojure.main -m stage5.ack-fault-probe audit
      URI JDBC_URL USER PASSWORD EXPECTED_SOURCE_PROTOCOL EXPECTED_DATABASE_ID
      EXPECTED_FINAL_SHA EXPECTED_FINAL_BASIS
    clojure.main -m stage5.ack-fault-probe publication-audit
      URI JDBC_URL USER PASSWORD EXPECTED_SOURCE_PROTOCOL EXPECTED_DATABASE_ID
      EXPECTED_FINAL_SHA EXPECTED_FINAL_BASIS EXPECTED_PUBLICATION_T"
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
(def ^:private publication-result-prefix
  "STAGE5-ACK-POSTPUBLICATION-RESULT ")
(def ^:private publication-audit-prefix
  "STAGE5-ACK-POSTPUBLICATION-AUDIT ")
(def ^:private ha-inflight-result-prefix
  "STAGE7-HA-INFLIGHT-RESULT ")
(def ^:private ha-inflight-audit-prefix
  "STAGE7-HA-INFLIGHT-AUDIT ")
(def ^:private ha-concurrent-result-prefix
  "STAGE7-HA-CONCURRENT-RESULT ")
(def ^:private ha-concurrent-audit-prefix
  "STAGE7-HA-CONCURRENT-AUDIT ")
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
(def ^:private expected-ha-takeover-token
  "STANDBY_PROMOTED_AND_WRITER_TERMINATED")
(def ^:private expected-publication-token
  "PUBLICATION_COMMITTED_TRANSACTOR_RESTARTED")
(def ^:private baseline-id "baseline")
(def ^:private fault-id "fault-before-publication")
(def ^:private recovery-id "recovery-after-crash")
(def ^:private published-kind :stage5-ack/published)
(def ^:private concurrent-kind :stage7-ha/concurrent-submission)
(def ^:private concurrent-ids
  (mapv #(str "takeover-concurrent-" %) (range 4)))

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
      :cause-messages (mapv #(.getMessage ^Throwable %) causes)
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

(defn- publication-tx
  []
  [[:db.fn/cas
    [:stage5-ack/id baseline-id]
    :stage5-ack/kind
    :stage5-ack/baseline
    published-kind]
   {:stage5-ack/id fault-id
    :stage5-ack/kind :stage5-ack/fault-after-publication}])

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

(defn- attribute-history
  [db entity-id attribute]
  (->> (d/datoms (d/history db) :eavt entity-id attribute)
       (map (fn [^Datom datom]
              [(.v datom) (.added datom) (d/tx->t (.tx datom))]))
       (sort-by pr-str)
       vec))

(defn- publication-history!
  [db expected-publication-t]
  (let [baseline-eid (d/entid db [:stage5-ack/id baseline-id])
        fault-eid (d/entid db [:stage5-ack/id fault-id])
        baseline-kind-history
        (attribute-history db baseline-eid :stage5-ack/kind)
        fault-id-history (attribute-history db fault-eid :stage5-ack/id)
        fault-kind-history (attribute-history db fault-eid :stage5-ack/kind)
        publication-events
        (concat
          (filter (fn [[value _ _]]
                    (#{:stage5-ack/baseline published-kind} value))
                  baseline-kind-history)
          fault-id-history
          fault-kind-history)
        publication-ts
        (->> publication-events
             (filter (fn [[value added? _]]
                       (or (and (= value :stage5-ack/baseline) (not added?))
                           (= value published-kind)
                           (= value fault-id)
                           (= value :stage5-ack/fault-after-publication))))
             (map #(nth % 2))
             set)]
    (ensure! (and baseline-eid fault-eid)
             "Published acknowledgement state is missing an entity"
             {:baseline-present? (boolean baseline-eid)
              :fault-present? (boolean fault-eid)})
    (ensure! (= 3 (count baseline-kind-history))
             "Baseline kind history contains a duplicate or missing transition"
             {:history baseline-kind-history})
    (ensure! (= 1 (count fault-id-history))
             "Fault identity history contains a duplicate or missing assertion"
             {:history fault-id-history})
    (ensure! (= 1 (count fault-kind-history))
             "Fault kind history contains a duplicate or missing assertion"
             {:history fault-kind-history})
    (ensure! (= #{expected-publication-t} publication-ts)
             "Publication changes do not belong to exactly one transaction"
             {:actual-publication-ts publication-ts
              :expected-publication-t expected-publication-t})
    (ensure! (some #{[:stage5-ack/baseline false expected-publication-t]}
                   baseline-kind-history)
             "Publication transaction did not retract the CAS baseline"
             {:history baseline-kind-history})
    (ensure! (some #{[published-kind true expected-publication-t]}
                   baseline-kind-history)
             "Publication transaction did not assert the CAS result"
             {:history baseline-kind-history})
    (sorted-map
      :baseline-kind baseline-kind-history
      :fault-id fault-id-history
      :fault-kind fault-kind-history
      :publication-t expected-publication-t)))

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

(defn- read-control-token!
  [token-file expected-token]
  (with-open [reader ^BufferedReader (io/reader token-file)]
    (let [token (.readLine reader)]
      (ensure! (= expected-token token)
               "Crash-window control token differs"
               {:actual token :expected expected-token})))
  :received)

(defn- unavailable?
  [^Throwable throwable]
  (= #{:cognitect.anomalies/unavailable}
     (set (:anomaly-categories (error-profile throwable)))))

(defn- sync-after-restart!
  [connection]
  (let [deadline (deadline-after-ms 90000)]
    (loop [attempt 1
           unavailable-count 0
           timeout-count 0]
      (ensure! (deadline-open? deadline)
               "Peer did not synchronize after the Transactor restart"
               {:attempt-count (dec attempt)
                :timeout-count timeout-count
                :unavailable-count unavailable-count})
      (let [attempt-millis
            (long (max 1 (min 10000
                              (quot (remaining-nanos deadline) 1000000))))
            outcome
            (try
              (let [pending ^Future (d/sync connection)]
                (try
                  {:db (.get pending attempt-millis TimeUnit/MILLISECONDS)
                   :status :succeeded}
                  (catch TimeoutException _
                    (.cancel pending true)
                    {:status :timeout})))
              (catch ExecutionException exception
                (let [failure (or (.getCause exception) exception)]
                  (if (unavailable? failure)
                    {:status :unavailable}
                    (throw failure))))
              (catch Throwable failure
                (if (unavailable? failure)
                  {:status :unavailable}
                  (throw failure))))]
        (case (:status outcome)
          :succeeded
          (let [db (:db outcome)]
            (ensure! (instance? Database db)
                     "Post-restart sync did not return a Database"
                     {:actual-class (some-> db class .getName)})
            {:attempt-count attempt
             :db db
             :timeout-count timeout-count
             :unavailable-count unavailable-count})

          :timeout
          (recur (inc attempt) unavailable-count (inc timeout-count))

          :unavailable
          (do
            (Thread/sleep 100)
            (recur (inc attempt) (inc unavailable-count) timeout-count)))))))

(defn- publication-future-outcome!
  [^Future future]
  (try
    (let [report (.get future 30000 TimeUnit/MILLISECONDS)]
      (ensure! (map? report)
               "Published transaction Future returned a non-report"
               {:actual-class (some-> report class .getName)})
      (sorted-map
        :db-after-basis (some-> report :db-after d/basis-t)
        :state :returned))
    (catch TimeoutException _
      (fail! "Published transaction Future remained ambiguous after recovery"
             {:observation-milliseconds 30000}))
    (catch ExecutionException exception
      (let [failure (or (.getCause exception) exception)]
        (ensure! (unavailable? failure)
                 "Published transaction Future failed with a semantic error"
                 {:error-profile (error-profile failure)})
        (sorted-map
          :error-profile (error-profile failure)
          :state :failed-unavailable)))
    (catch CancellationException exception
      (fail! "Published transaction Future was cancelled"
             {:error-profile (error-profile exception)}))
    (catch Throwable failure
      (ensure! (unavailable? failure)
               "Published transaction Future failed with a semantic error"
               {:error-profile (error-profile failure)})
      (sorted-map
        :error-profile (error-profile failure)
        :state :failed-unavailable))))

(defn- concurrent-future-outcome!
  [id ^Future future]
  (assoc (publication-future-outcome! future) :id id))

(defn- concurrent-history!
  [db id]
  (let [eid (d/entid db [:stage5-ack/id id])
        event-t (fact-t db id)
        id-history (attribute-history db eid :stage5-ack/id)
        kind-history (attribute-history db eid :stage5-ack/kind)]
    (ensure! (= [[id true event-t]] id-history)
             "Concurrent identity was duplicated or retracted"
             {:history id-history :id id})
    (ensure! (= [[concurrent-kind true event-t]] kind-history)
             "Concurrent kind was duplicated, changed, or retracted"
             {:history kind-history :id id})
    (sorted-map :event-t event-t
                :id id
                :id-history id-history
                :kind-history kind-history)))

(defn- recover-concurrent-submissions!
  [connection initial-db]
  (loop [ids concurrent-ids
         db initial-db
         recovery []]
    (if-let [id (first ids)]
      (if (entity-present? db id)
        (recur (next ids)
               db
               (conj recovery (sorted-map :action :already-committed :id id)))
        (let [report
              (transact-before!
                connection
                [{:stage5-ack/id id :stage5-ack/kind concurrent-kind}]
                :ha-concurrent-recovery)
              after (:db-after report)]
          (ensure! (>= (d/basis-t (:db-before report)) (d/basis-t db))
                   "Concurrent recovery report regressed the Peer basis"
                   {:actual (d/basis-t (:db-before report))
                    :expected-at-least (d/basis-t db)
                    :id id})
          (recur (next ids)
                 after
                 (conj recovery
                       (sorted-map :action :resubmitted
                                   :event-t (fact-t after id)
                                   :id id)))))
      {:db db :recovery recovery})))

(defn- require-published-state!
  [^Database db expected-database-id expected-publication-t]
  (let [rows (probe-rows db)
        publication-t (fact-t db fault-id)]
    (ensure! (= expected-database-id (str (.id db)))
             "Post-publication database identity differs"
             {:actual (str (.id db)) :expected expected-database-id})
    (ensure! (= #{[baseline-id published-kind]
                  [fault-id :stage5-ack/fault-after-publication]}
                (set (map #(subvec % 0 2) rows)))
             "Post-publication logical state differs"
             {:rows rows})
    (when expected-publication-t
      (ensure! (= expected-publication-t publication-t)
               "Publication transaction identity differs"
               {:actual publication-t :expected expected-publication-t}))
    (ensure! (<= publication-t (d/basis-t db))
             "Publication transaction is newer than the database basis"
             {:basis-t (d/basis-t db) :publication-t publication-t})
    (let [history (publication-history! db publication-t)]
      (sorted-map
        :canonical (canonical-state db)
        :history history
        :publication-t publication-t))))

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
  [{:keys [cut expected-created expected-source-protocol jdbc-url password
           token-file uri user]}]
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
                    (if (= cut :postpublication)
                      (publication-tx)
                      [{:stage5-ack/id fault-id
                        :stage5-ack/kind
                        :stage5-ack/fault-before-publication}])))
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
                    :cut cut
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
              (if (= cut :postpublication)
                (do
                  (read-control-token!
                    token-file expected-publication-token)
                  ;; The controller terminated this holder backend while this
                  ;; JVM was stopped, then observed publication, crashed the
                  ;; writer, and restarted the Transactor.
                  (reset! holder-active? false)
                  (try
                    (.close ^Connection @holder)
                    (catch Throwable _))
                  (reset! holder nil)
                  (wait-for-backend-exit!
                    jdbc-url user password (:pid writer))
                  (let [sync-result (sync-after-restart! @peer-connection)
                        future-outcome
                        (publication-future-outcome!
                          ^Future @transaction-future)
                        final-root
                        (sql-log-root jdbc-url user password database-id)
                        final-ids (sql-ids jdbc-url user password)
                        final-db (:db sync-result)
                        published
                        (require-published-state! final-db database-id nil)
                        publication-t (:publication-t published)
                        final-canonical (:canonical published)]
                    (ensure! (and (integer? (:rev final-root))
                                  (> (:rev final-root) (:rev baseline-root))
                                  (not= baseline-root final-root))
                             "Authoritative root did not remain advanced after restart"
                             {:baseline-revision (:rev baseline-root)
                              :final-revision (:rev final-root)})
                    (ensure! (= blocked-ids final-ids)
                             "PostgreSQL KV membership changed after publication"
                             {:after-count (count final-ids)
                              :blocked-count (count blocked-ids)})
                    (when (= :returned (:state future-outcome))
                      (ensure! (= publication-t
                                  (:db-after-basis future-outcome))
                               "Returned transaction report names another basis"
                               {:future-outcome future-outcome
                                :publication-t publication-t}))
                    (sorted-map
                      :authoritative-publication :committed
                      :baseline-basis-t baseline-basis
                      :baseline-canonical-sha256 baseline-sha
                      :baseline-root-revision (:rev baseline-root)
                      :baseline-root-sha256 baseline-root-sha
                      :blocked-backend-pid (:pid writer)
                      :candidate-boundary boundary
                      :created? created?
                      :database-id database-id
                      :fault-sentinel-present? true
                      :final-basis-t (d/basis-t final-db)
                      :final-canonical-sha256 (sha256 final-canonical)
                      :final-root-revision (:rev final-root)
                      :final-root-sha256 (sha256 final-root)
                      :future-done-while-blocked? false
                      :future-outcome future-outcome
                      :holder-backend-pid holder-backend-pid
                      :no-duplicate-committed-effect true
                      :publication-history (:history published)
                      :publication-t publication-t
                      :same-peer-recovered? true
                      :status :passed
                      :sync-attempt-count (:attempt-count sync-result)
                      :sync-timeout-count (:timeout-count sync-result)
                      :sync-unavailable-count
                      (:unavailable-count sync-result)
                      :transactor-backend-exited? true)))
                (do
                  (read-control-token!
                    token-file
                    (if (= cut :ha-takeover)
                      expected-ha-takeover-token
                      expected-kill-token))
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
                  (let [ha-takeover? (= cut :ha-takeover)
                        future-state
                        (if ha-takeover?
                          (publication-future-outcome!
                            ^Future @transaction-future)
                          (wait-for-future-terminal!
                            ^Future @transaction-future))
                        sync-result
                        (when ha-takeover?
                          (sync-after-restart! @peer-connection))
                        final-root (sql-log-root
                                     jdbc-url user password database-id)
                        final-ids (sql-ids jdbc-url user password)
                        final-db (if ha-takeover?
                                   (:db sync-result)
                                   (d/db @peer-connection))
                        final-canonical (canonical-state final-db)]
                    (if ha-takeover?
                      (ensure! (and (integer? (:rev final-root))
                                    (> (:rev final-root)
                                       (:rev baseline-root))
                                    (not= baseline-root final-root))
                               "Promoted standby did not adopt the in-flight tail"
                               {:after-revision (:rev final-root)
                                :before-revision (:rev baseline-root)})
                      (ensure! (= baseline-root final-root)
                               "Authoritative root changed after killed-writer rollback"
                               {:actual-sha256 (sha256 final-root)
                                :expected-sha256 baseline-root-sha}))
                    (ensure! (= blocked-ids final-ids)
                             "PostgreSQL KV membership changed after writer death"
                             {:after-count (count final-ids)
                              :blocked-count (count blocked-ids)})
                    (if ha-takeover?
                      (do
                        (ensure! (= :failed-unavailable (:state future-state))
                                 "In-flight takeover Future did not fail unavailable"
                                 {:future-outcome future-state})
                        (let [takeover-t (fact-t final-db fault-id)
                              takeover-rows (:rows final-canonical)
                              fault-eid
                              (d/entid final-db [:stage5-ack/id fault-id])
                              fault-id-history
                              (attribute-history
                                final-db fault-eid :stage5-ack/id)
                              fault-kind-history
                              (attribute-history
                                final-db fault-eid :stage5-ack/kind)]
                          (ensure! (= database-id (str (.id ^Database final-db)))
                                   "Takeover database identity differs"
                                   {:actual (str (.id ^Database final-db))
                                    :expected database-id})
                          (ensure! (= #{[baseline-id :stage5-ack/baseline]
                                        [fault-id
                                         :stage5-ack/fault-before-publication]}
                                      (set (map #(subvec % 0 2)
                                                takeover-rows)))
                                   "Takeover logical state differs"
                                   {:rows takeover-rows})
                          (ensure! (= takeover-t (d/basis-t final-db))
                                   "Takeover fact and basis differ"
                                   {:basis-t (d/basis-t final-db)
                                    :future-outcome future-state
                                    :takeover-t takeover-t})
                          (ensure! (and (= [[fault-id true takeover-t]]
                                            fault-id-history)
                                        (= [[:stage5-ack/fault-before-publication
                                             true takeover-t]]
                                           fault-kind-history))
                                   "Takeover transaction was duplicated or incomplete"
                                   {:fault-id-history fault-id-history
                                    :fault-kind-history fault-kind-history})
                          (let [report
                                (transact-before!
                                  @peer-connection
                                  [{:stage5-ack/id recovery-id
                                    :stage5-ack/kind
                                    :stage5-ack/ha-takeover-recovery}]
                                  :ha-takeover-recovery)
                                recovered-db (:db-after report)
                                recovered-root
                                (sql-log-root
                                  jdbc-url user password database-id)
                                recovered-canonical
                                (canonical-state recovered-db)
                                recovery-t (fact-t recovered-db recovery-id)]
                            (ensure! (= takeover-t
                                        (d/basis-t (:db-before report)))
                                     "HA recovery write did not begin after takeover"
                                     {:actual
                                      (d/basis-t (:db-before report))
                                      :expected takeover-t})
                            (ensure! (= recovery-t
                                        (d/basis-t recovered-db))
                                     "HA recovery write/report basis differs"
                                     {:event-t recovery-t
                                      :report-basis
                                      (d/basis-t recovered-db)})
                            (ensure! (and (entity-present?
                                           recovered-db fault-id)
                                          (entity-present?
                                            recovered-db recovery-id))
                                     "HA recovery state has the wrong sentinels"
                                     {})
                            (ensure! (and
                                       (integer? (:rev recovered-root))
                                       (> (:rev recovered-root)
                                          (:rev baseline-root))
                                       (not= recovered-root baseline-root))
                                     "HA recovery write did not advance root"
                                     {:after-revision (:rev recovered-root)
                                      :before-revision
                                      (:rev baseline-root)})
                            (sorted-map
                              :authoritative-publication
                              :committed-during-takeover
                              :baseline-basis-t baseline-basis
                              :baseline-canonical-sha256 baseline-sha
                              :baseline-root-revision (:rev baseline-root)
                              :baseline-root-sha256 baseline-root-sha
                              :blocked-backend-pid (:pid writer)
                              :candidate-boundary boundary
                              :created? created?
                              :database-id database-id
                              :fault-sentinel-absent? false
                              :fault-sentinel-present? true
                              :final-basis-t (d/basis-t recovered-db)
                              :final-canonical-sha256
                              (sha256 recovered-canonical)
                              :final-root-revision (:rev recovered-root)
                              :final-root-sha256 (sha256 recovered-root)
                              :future-outcome future-state
                              :future-done-while-blocked? false
                              :holder-backend-pid holder-backend-pid
                              :immutable-orphan-row-count
                              (count orphan-rows)
                              :immutable-orphan-rows orphan-rows
                              :no-success-before-authoritative-publication
                              true
                              :no-duplicate-committed-effect true
                              :orphan-candidate-count (count orphan-rows)
                              :original-future-unavailable-with-adopted-transaction?
                              true
                              :precrash-root-sha256 baseline-root-sha
                              :recovery-event-t recovery-t
                              :recovery-sentinel-present? true
                              :root-row-byte-identical-before-recovery? true
                              :same-peer-recovered-through-promoted-endpoint?
                              true
                              :status :passed
                              :sync-attempt-count (:attempt-count sync-result)
                              :sync-timeout-count (:timeout-count sync-result)
                              :sync-unavailable-count
                              (:unavailable-count sync-result)
                              :takeover-event-t takeover-t
                              :takeover-history
                              {:fault-id fault-id-history
                               :fault-kind fault-kind-history}
                              :transactor-backend-exited? true))))
                      (do
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
                          :transactor-backend-exited? true))))))))))
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

(defn- ha-concurrent-window!
  [{:keys [expected-created expected-source-protocol jdbc-url password
           token-file uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        created? (boolean
                   (bounded-call! connect-timeout-ms
                                  :create-database
                                  #(d/create-database uri)))
        peer-connection (atom nil)
        holder (atom nil)
        holder-active? (atom false)
        transaction-futures (atom [])]
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
                 "Fresh concurrent-takeover baseline contains unexpected rows"
                 {:rows (:rows baseline-canonical)})
        (reset! holder (open-sql! jdbc-url user password))
        (let [{:keys [holder-backend-pid root-id]}
              (lock-log-root! @holder database-id baseline-root)]
          (reset! holder-active? true)
          (reset! transaction-futures
                  (mapv (fn [id]
                          [id (d/transact-async
                                @peer-connection
                                [{:stage5-ack/id id
                                  :stage5-ack/kind concurrent-kind}])])
                        concurrent-ids))
          (let [writer (wait-for-blocking-writer!
                         jdbc-url user password holder-backend-pid)
                blocked-root (sql-log-root jdbc-url user password database-id)
                blocked-ids (sql-ids jdbc-url user password)
                orphan-rows (require-one-immutable-tail!
                              jdbc-url user password baseline-ids blocked-ids
                              root-id baseline-root)
                blocked-db (d/db @peer-connection)
                blocked-canonical (canonical-state blocked-db)
                completed-count
                (count (filter (fn [[_ ^Future pending]] (.isDone pending))
                               @transaction-futures))]
            (ensure! (= baseline-root blocked-root)
                     "Authoritative root changed while concurrent publication was blocked"
                     {:actual-sha256 (sha256 blocked-root)
                      :expected-sha256 baseline-root-sha})
            (ensure! (= baseline-canonical blocked-canonical)
                     "Peer state advanced before concurrent authoritative publication"
                     {:actual-sha256 (sha256 blocked-canonical)
                      :expected-sha256 baseline-sha})
            (ensure! (every? #(not (entity-present? blocked-db %))
                             concurrent-ids)
                     "A concurrent sentinel became visible before publication"
                     {:visible-ids (filterv #(entity-present? blocked-db %)
                                            concurrent-ids)})
            (ensure! (zero? completed-count)
                     "A concurrent Future completed before takeover"
                     {:completed-count completed-count})
            (emit-marker!
              blocked-prefix
              (sorted-map
                :authoritative-root-id root-id
                :baseline-basis-t baseline-basis
                :baseline-canonical-sha256 baseline-sha
                :baseline-root-revision (:rev baseline-root)
                :baseline-root-sha256 baseline-root-sha
                :blocked-backend-pid (:pid writer)
                :blocked-writer-count 1
                :candidate-boundary boundary
                :concurrent-future-done-count completed-count
                :concurrent-submission-count (count concurrent-ids)
                :cut :ha-concurrent
                :database-id database-id
                :future-incomplete? true
                :holder-backend-pid holder-backend-pid
                :immutable-tail-rows orphan-rows
                :orphan-candidate-count (count orphan-rows)
                :peer-state-unchanged? true
                :root-row-byte-identical? true
                :transactor-wait-event (:wait-event writer)
                :transactor-wait-event-type (:wait-event-type writer)))
            (read-control-token! token-file expected-ha-takeover-token)
            (wait-for-backend-exit! jdbc-url user password (:pid writer))
            (let [locked-root
                  (sql-log-root jdbc-url user password database-id)]
              (ensure! (= baseline-root locked-root)
                       "Terminated writer changed the root before holder rollback"
                       {:actual-sha256 (sha256 locked-root)
                        :expected-sha256 baseline-root-sha}))
            (.rollback ^Connection @holder)
            (reset! holder-active? false)
            (.close ^Connection @holder)
            (reset! holder nil)
            (let [initial-outcomes
                  (mapv (fn [[id pending]]
                          (concurrent-future-outcome! id pending))
                        @transaction-futures)
                  initial-returned-count
                  (count (filter #(= :returned (:state %)) initial-outcomes))
                  initial-unavailable-count
                  (count (filter #(= :failed-unavailable (:state %))
                                 initial-outcomes))
                  sync-result (sync-after-restart! @peer-connection)
                  adopted-db (:db sync-result)
                  adopted-basis (d/basis-t adopted-db)
                  adopted-ids
                  (filterv #(entity-present? adopted-db %) concurrent-ids)]
              (doseq [{:keys [db-after-basis id state] :as outcome}
                      initial-outcomes]
                (ensure! (#{:returned :failed-unavailable} state)
                         "Concurrent Future has an unaccounted outcome"
                         {:outcome outcome})
                (when (= :returned state)
                  (ensure! (and (entity-present? adopted-db id)
                                (= db-after-basis (fact-t adopted-db id)))
                           "Returned concurrent Future disagrees with durable state"
                           {:outcome outcome})))
              (let [{final-db :db recovery :recovery}
                    (recover-concurrent-submissions!
                      @peer-connection adopted-db)
                    histories (mapv #(concurrent-history! final-db %)
                                    concurrent-ids)
                    committed-order (vec (sort-by :event-t histories))
                    event-ts (mapv :event-t committed-order)
                    final-canonical (canonical-state final-db)
                    final-root (sql-log-root
                                 jdbc-url user password database-id)
                    final-id-set (set (map first (:rows final-canonical)))
                    resubmitted-count
                    (count (filter #(= :resubmitted (:action %)) recovery))]
                (ensure! (= (set (conj concurrent-ids baseline-id))
                            final-id-set)
                         "Concurrent takeover final identity set differs"
                         {:actual final-id-set})
                (ensure! (and (= (count concurrent-ids) (count event-ts))
                              (= (count event-ts) (count (distinct event-ts)))
                              (every? #(< baseline-basis %) event-ts)
                              (apply < event-ts))
                         "Concurrent commits do not form one strict monotonic order"
                         {:event-ts event-ts})
                (ensure! (>= (d/basis-t final-db) (last event-ts))
                         "Final Peer basis precedes a concurrent commit"
                         {:event-ts event-ts
                          :final-basis (d/basis-t final-db)})
                (ensure! (and (integer? (:rev final-root))
                              (> (:rev final-root) (:rev baseline-root))
                              (not= final-root baseline-root))
                         "Concurrent recovery did not advance the authoritative root"
                         {:after-revision (:rev final-root)
                          :before-revision (:rev baseline-root)})
                (sorted-map
                  :adopted-basis-t adopted-basis
                  :adopted-before-recovery-count (count adopted-ids)
                  :adopted-before-recovery-ids adopted-ids
                  :authoritative-log-serialized? true
                  :baseline-basis-t baseline-basis
                  :baseline-canonical-sha256 baseline-sha
                  :baseline-root-revision (:rev baseline-root)
                  :baseline-root-sha256 baseline-root-sha
                  :blocked-backend-pid (:pid writer)
                  :candidate-boundary boundary
                  :committed-order committed-order
                  :concurrent-submission-count (count concurrent-ids)
                  :database-id database-id
                  :every-submission-outcome-accounted? true
                  :final-basis-t (d/basis-t final-db)
                  :final-canonical-sha256 (sha256 final-canonical)
                  :final-root-revision (:rev final-root)
                  :final-root-sha256 (sha256 final-root)
                  :first-commit-t (first event-ts)
                  :initial-outcomes initial-outcomes
                  :initial-returned-count initial-returned-count
                  :initial-unavailable-count initial-unavailable-count
                  :last-commit-t (last event-ts)
                  :no-duplicate-committed-effect true
                  :no-lost-logical-submission true
                  :recovery recovery
                  :resubmitted-count resubmitted-count
                  :status :passed
                  :strict-monotonic-committed-order true
                  :sync-attempt-count (:attempt-count sync-result)
                  :sync-timeout-count (:timeout-count sync-result)
                  :sync-unavailable-count (:unavailable-count sync-result)
                  :transactor-backend-exited? true))))))
      (finally
        (doseq [[_ ^Future pending] @transaction-futures]
          (when-not (.isDone pending)
            (.cancel pending true)))
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
  [^Database db expected-database-id expected-basis expected-sha
   ha-takeover?]
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
    (ensure! (= (if ha-takeover?
                  #{baseline-id fault-id recovery-id}
                  #{baseline-id recovery-id})
                (set (map first rows)))
             "Audit probe identity set differs"
             {:actual (set (map first rows))})
    (if ha-takeover?
      (ensure! (= :stage5-ack/fault-before-publication
                  (:stage5-ack/kind
                    (d/entity db [:stage5-ack/id fault-id])))
               "Adopted takeover transaction differs in fresh audit"
               {})
      (ensure! (not (entity-present? db fault-id))
               "Interrupted transaction appeared in fresh audit"
               {}))
    (ensure! (= expected-basis (fact-t db recovery-id))
             "Recovery fact transaction differs from final basis"
             {:event-t (fact-t db recovery-id)
              :final-basis expected-basis})
    canonical))

(defn- audit!
  [{:keys [expected-database-id expected-final-basis expected-final-sha
           expected-source-protocol ha-takeover? jdbc-url password uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        connection (connect! uri)]
    (try
      (let [db (d/db connection)
            canonical (require-final-state!
                        db expected-database-id
                        expected-final-basis expected-final-sha ha-takeover?)
            root (sql-log-root jdbc-url user password expected-database-id)]
        (ensure! (and (integer? (:rev root)) (not (neg? (:rev root))))
                 "Fresh audit PostgreSQL log root has an invalid revision"
                 {:root-revision (:rev root)})
        (sorted-map
          :candidate-boundary boundary
          :canonical-sha256 (sha256 canonical)
          :database-id expected-database-id
          :fault-sentinel-absent? (not ha-takeover?)
          :fault-sentinel-present? (boolean ha-takeover?)
          :final-basis-t expected-final-basis
          :final-canonical-sha256 (sha256 canonical)
          :recovery-sentinel-present? true
          :sql-log-root :present
          :sql-log-root-revision (:rev root)
          :sql-log-root-sha256 (sha256 root)
          :status :passed))
      (finally
        (release! connection)))))

(defn- ha-concurrent-audit!
  [{:keys [expected-database-id expected-final-basis expected-final-sha
           expected-source-protocol jdbc-url password uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        connection (connect! uri)]
    (try
      (let [db (d/db connection)
            canonical (canonical-state db)
            actual-sha (sha256 canonical)
            actual-ids (set (map first (:rows canonical)))
            histories (mapv #(concurrent-history! db %) concurrent-ids)
            committed-order (vec (sort-by :event-t histories))
            event-ts (mapv :event-t committed-order)
            root (sql-log-root jdbc-url user password expected-database-id)]
        (ensure! (= expected-database-id (str (.id ^Database db)))
                 "Concurrent audit database identity differs"
                 {:actual (str (.id ^Database db))
                  :expected expected-database-id})
        (ensure! (= expected-final-basis (d/basis-t db))
                 "Concurrent audit basis differs"
                 {:actual (d/basis-t db) :expected expected-final-basis})
        (ensure! (= expected-final-sha actual-sha)
                 "Concurrent audit canonical state differs"
                 {:actual actual-sha :expected expected-final-sha})
        (ensure! (= (set (conj concurrent-ids baseline-id)) actual-ids)
                 "Concurrent audit identity set differs"
                 {:actual actual-ids})
        (ensure! (= :stage5-ack/baseline
                    (:stage5-ack/kind
                      (d/entity db [:stage5-ack/id baseline-id])))
                 "Concurrent audit baseline differs"
                 {})
        (ensure! (and (= (count concurrent-ids) (count event-ts))
                      (= (count event-ts) (count (distinct event-ts)))
                      (apply < event-ts))
                 "Concurrent audit order is not strict and unique"
                 {:event-ts event-ts})
        (ensure! (and (integer? (:rev root)) (not (neg? (:rev root))))
                 "Concurrent audit PostgreSQL log root is invalid"
                 {:root-revision (:rev root)})
        (sorted-map
          :candidate-boundary boundary
          :canonical-sha256 actual-sha
          :committed-order committed-order
          :concurrent-submission-count (count concurrent-ids)
          :database-id expected-database-id
          :every-submission-present? true
          :final-basis-t expected-final-basis
          :final-canonical-sha256 actual-sha
          :no-duplicate-committed-effect true
          :no-lost-logical-submission true
          :sql-log-root :present
          :sql-log-root-revision (:rev root)
          :sql-log-root-sha256 (sha256 root)
          :status :passed
          :strict-monotonic-committed-order true))
      (finally
        (release! connection)))))

(defn- publication-audit!
  [{:keys [expected-database-id expected-final-basis expected-final-sha
           expected-publication-t expected-source-protocol jdbc-url password
           uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        connection (connect! uri)]
    (try
      (let [db (d/db connection)
            published
            (require-published-state!
              db expected-database-id expected-publication-t)
            canonical (:canonical published)
            actual-sha (sha256 canonical)
            root (sql-log-root jdbc-url user password expected-database-id)]
        (ensure! (= expected-final-basis (d/basis-t db))
                 "Fresh publication audit basis differs"
                 {:actual (d/basis-t db) :expected expected-final-basis})
        (ensure! (= expected-final-sha actual-sha)
                 "Fresh publication audit state differs"
                 {:actual actual-sha :expected expected-final-sha})
        (ensure! (and (integer? (:rev root)) (not (neg? (:rev root))))
                 "Fresh publication audit root has an invalid revision"
                 {:root-revision (:rev root)})
        (sorted-map
          :candidate-boundary boundary
          :canonical-sha256 actual-sha
          :database-id expected-database-id
          :fault-sentinel-present? true
          :final-basis-t expected-final-basis
          :no-duplicate-committed-effect true
          :publication-history (:history published)
          :publication-t expected-publication-t
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
        {:cut :prepublication
         :expected-created (parse-boolean! expected-created)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :token-file token-file
         :uri (require-uri! uri)
         :user user})

      "publication-window"
      (let [[_ uri jdbc-url user password expected-created source-protocol
             token-file & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password expected-created
                              source-protocol token-file])
                      (empty? extra))
                 "publication-window requires exactly eight arguments"
                 {:argument-count (count args)})
        {:cut :postpublication
         :expected-created (parse-boolean! expected-created)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :token-file token-file
         :uri (require-uri! uri)
         :user user})

      "ha-takeover-window"
      (let [[_ uri jdbc-url user password expected-created source-protocol
             token-file & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password expected-created
                              source-protocol token-file])
                      (empty? extra))
                 "ha-takeover-window requires exactly eight arguments"
                 {:argument-count (count args)})
        {:cut :ha-takeover
         :expected-created (parse-boolean! expected-created)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :token-file token-file
         :uri (require-uri! uri)
         :user user})

      "ha-concurrent-window"
      (assoc (parse-command (cons "ha-takeover-window" (rest args)))
             :cut :ha-concurrent
             :mode mode)

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

      "ha-takeover-audit"
      (assoc (parse-command (cons "audit" (rest args)))
             :ha-takeover? true
             :mode mode)

      "ha-concurrent-audit"
      (assoc (parse-command (cons "audit" (rest args)))
             :mode mode)

      "publication-audit"
      (let [[_ uri jdbc-url user password source-protocol database-id
             final-sha final-basis publication-t & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password source-protocol
                              database-id final-sha final-basis publication-t])
                      (empty? extra))
                 "publication-audit requires exactly ten arguments"
                 {:argument-count (count args)})
        {:expected-database-id database-id
         :expected-final-basis (parse-long! :expected-final-basis final-basis)
         :expected-final-sha (parse-sha! :expected-final-sha final-sha)
         :expected-publication-t
         (parse-long! :expected-publication-t publication-t)
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
    "publication-window" (crash-window! command)
    "ha-takeover-window" (crash-window! command)
    "ha-concurrent-window" (ha-concurrent-window! command)
    "recover" (recover! command)
    "audit" (audit! command)
    "ha-takeover-audit" (audit! command)
    "ha-concurrent-audit" (ha-concurrent-audit! command)
    "publication-audit" (publication-audit! command)))

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
          "publication-window" publication-result-prefix
          "ha-takeover-window" ha-inflight-result-prefix
          "ha-takeover-audit" ha-inflight-audit-prefix
          "ha-concurrent-window" ha-concurrent-result-prefix
          "ha-concurrent-audit" ha-concurrent-audit-prefix
          "recover" recover-result-prefix
          "audit" audit-prefix
          "publication-audit" publication-audit-prefix)
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

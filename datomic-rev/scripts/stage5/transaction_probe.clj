(ns stage5.transaction-probe
  "Focused PostgreSQL-backed transaction semantics for the recovered pair.

  Exercise mode creates one fresh database and proves atomic rejection, one
  normally returned transaction whose PostgreSQL log root is observed advanced
  after return, CAS arbitration, and ordering of concurrently accepted
  transactions. Audit mode runs in a fresh Peer JVM after a recovered Transactor
  restart and verifies the exact canonical transaction projection selected by
  this probe. This does not claim every database byte or crash/fault safety at
  the acknowledgement boundary.

    clojure.main -m stage5.transaction-probe exercise
      URI JDBC_URL USER PASSWORD EXPECTED_CREATED EXPECTED_SOURCE_PROTOCOL
    clojure.main -m stage5.transaction-probe audit
      URI JDBC_URL USER PASSWORD EXPECTED_SOURCE_PROTOCOL
      EXPECTED_DATABASE_ID EXPECTED_CANONICAL_SHA EXPECTED_FINAL_BASIS"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.api :as d])
  (:import [datomic Database Datom]
           [java.io File]
           [java.math BigInteger]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]
           [java.sql DriverManager]
           [java.util.concurrent Callable CountDownLatch ExecutionException
            ExecutorService Executors Future ThreadFactory TimeUnit
            TimeoutException]
           [java.util.concurrent.atomic AtomicLong]))

(set! *warn-on-reflection* true)

(def ^:private result-prefix "STAGE5-TRANSACTION-RESULT ")
(def ^:private audit-prefix "STAGE5-TRANSACTION-AUDIT ")
(def ^:private error-prefix "STAGE5-TRANSACTION-ERROR ")
(def ^:private connect-timeout-ms 30000)
(def ^:private transaction-timeout-ms 30000)
(def ^:private concurrent-timeout-ms 90000)
(def ^:private cleanup-timeout-ms 30000)
(def ^:private cas-workers 4)
(def ^:private cas-rounds 2)
(def ^:private ordered-workers 4)

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

(defn- strictly-increasing?
  [values]
  (every? (fn [[left right]] (< left right))
          (partition 2 1 values)))

(defn- bytes->hex
  [bytes]
  (when bytes
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes))))

(defn- deadline-after-ms
  [milliseconds]
  (+ (System/nanoTime) (* 1000000 (long milliseconds))))

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

(defn- fixed-executor
  [threads prefix]
  (Executors/newFixedThreadPool
    (int threads)
    (daemon-thread-factory prefix)))

(defn- shutdown-executor!
  [^ExecutorService executor]
  (when executor
    (.shutdownNow executor)
    (try
      (ensure! (.awaitTermination executor 2 TimeUnit/SECONDS)
               "Owned executor did not terminate"
               {})
      (catch InterruptedException _
        (.interrupt (Thread/currentThread))
        (fail! "Interrupted while awaiting owned executor termination" {}))))
  nil)

(defn- submit-call!
  [^ExecutorService executor f]
  (.submit executor
           ^Callable
           (reify Callable
             (call [_] (f)))))

(defn- get-before!
  [^Future future deadline label]
  (let [remaining (remaining-nanos deadline)]
    (when-not (pos? remaining)
      (.cancel future true)
      (fail! "Absolute deadline elapsed" {:operation label}))
    (try
      (.get future remaining TimeUnit/NANOSECONDS)
      (catch TimeoutException _
        (.cancel future true)
        (fail! "Absolute deadline elapsed" {:operation label}))
      (catch ExecutionException exception
        (throw (or (.getCause exception) exception))))))

(defn- await-latch!
  [^CountDownLatch latch deadline label]
  (let [remaining (remaining-nanos deadline)]
    (ensure! (and (pos? remaining)
                  (.await latch remaining TimeUnit/NANOSECONDS))
             "Barrier deadline elapsed"
             {:operation label
              :remaining-participants (.getCount latch)})))

(defn- bounded-call!
  [milliseconds label f]
  (let [executor (fixed-executor 1 (str "stage5-" (name label)))
        deadline (deadline-after-ms milliseconds)
        task (submit-call! executor f)]
    (try
      (get-before! task deadline label)
      (finally
        (shutdown-executor! executor)))))

(defn- connect!
  [uri]
  (bounded-call! connect-timeout-ms :connect #(d/connect uri)))

(defn- release!
  [connection]
  (when connection
    (bounded-call! connect-timeout-ms :release #(d/release connection)))
  nil)

(defn- transact-before!
  [connection tx-data deadline label]
  (get-before! ^Future (d/transact-async connection tx-data) deadline label))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- error-profile
  [^Throwable throwable]
  (let [causes (cause-chain throwable)]
    (sorted-map
      :anomaly-categories
      (vec (keep #(some-> % ex-data :cognitect.anomalies/category) causes))
      :cancelled-values
      (vec (keep #(when (contains? (or (ex-data %) {}) :datomic/cancelled)
                    (boolean (:datomic/cancelled (ex-data %))))
                 causes))
      :cause-classes (mapv #(.getName (class %)) causes)
      :db-errors (vec (keep #(some-> % ex-data :db/error) causes))
      :exception-class (.getName (class throwable))
      :probe-messages
      (vec (keep #(when (= :probe-failed (some-> % ex-data ::error))
                    (.getMessage ^Throwable %))
                 causes))
      :probe-errors (vec (keep #(some-> % ex-data ::error) causes)))))

(defn- capture
  [f]
  (try
    (sorted-map :returned (f))
    (catch Throwable throwable
      (sorted-map :thrown throwable))))

(defn- require-error!
  [outcome db-error require-cancelled?]
  (ensure! (contains? outcome :thrown)
           "Rejected transaction unexpectedly returned"
           {:expected-db-error db-error})
  (let [profile (error-profile (:thrown outcome))]
    (ensure! (some #{db-error} (:db-errors profile))
             "Rejected transaction lost its exact Datomic error"
             {:expected-db-error db-error :profile profile})
    (ensure! (some #{:cognitect.anomalies/conflict}
                   (:anomaly-categories profile))
             "Rejected transaction lost its conflict anomaly category"
             {:expected-db-error db-error :profile profile})
    (ensure! (some #{"datomic.impl.Exceptions$IllegalStateExceptionInfo"}
                   (:cause-classes profile))
             "Rejected transaction lost IllegalStateExceptionInfo"
             {:expected-db-error db-error :profile profile})
    (when require-cancelled?
      (ensure! (some true? (:cancelled-values profile))
               "Rejected transaction lost :datomic/cancelled true"
               {:expected-db-error db-error :profile profile}))
    profile))

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
  [{:db/ident :stage5/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage5/kind
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage5/counter
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :stage5/round
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :stage5/worker
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :stage5/unique-token
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/value
    :db/index true
    :db.install/_attribute :db.part/db}])

(defn- seed-tx
  []
  [{:stage5/id "counter"
    :stage5/kind :stage5/counter
    :stage5/counter 0}
   {:stage5/id "unique-owner-a"
    :stage5/kind :stage5/unique-owner
    :stage5/unique-token "owned-token"}
   {:stage5/id "unique-owner-b"
    :stage5/kind :stage5/unique-owner}])

(defn- counter-value
  [db]
  (d/q '[:find ?value .
         :where
         [?e :stage5/id "counter"]
         [?e :stage5/counter ?value]]
       db))

(defn- entity-present?
  [db id]
  (boolean (d/entid db [:stage5/id id])))

(defn- counter-history
  [db]
  (let [entity-id (d/entid db [:stage5/id "counter"])]
    (ensure! entity-id "Counter entity is absent" {})
    (->> (d/datoms (d/history db) :eavt entity-id :stage5/counter)
         (map (fn [^Datom datom]
                [(.v datom) (.added datom) (d/tx->t (.tx datom))]))
         (sort-by #(nth % 2))
         vec)))

(defn- fact-t
  [db entity-id attribute]
  (let [eid (d/entid db [:stage5/id entity-id])
        datoms (when eid (seq (d/datoms db :eavt eid attribute)))]
    (ensure! (= 1 (count datoms))
             "Expected exactly one current fact for transaction identity"
             {:attribute attribute :entity-id entity-id :fact-count (count datoms)})
    (d/tx->t (.tx ^Datom (first datoms)))))

(defn- cas-events
  [db]
  (->> (d/q '[:find ?id ?round ?worker ?value
              :where
              [?e :stage5/kind :stage5/cas-event]
              [?e :stage5/id ?id]
              [?e :stage5/round ?round]
              [?e :stage5/worker ?worker]
              [?e :stage5/counter ?value]]
            db)
       (map (fn [[id round worker value]]
              [id round worker value (fact-t db id :stage5/round)]))
       (sort-by first)
       vec))

(defn- ordered-events
  [db]
  (->> (d/q '[:find ?id ?worker
              :where
              [?e :stage5/kind :stage5/ordered-event]
              [?e :stage5/id ?id]
              [?e :stage5/worker ?worker]]
            db)
       (map (fn [[id worker]]
              [id worker (fact-t db id :stage5/worker)]))
       (sort-by first)
       vec))

(defn- unique-owner-state
  [db]
  (->> ["unique-owner-a" "unique-owner-b"]
       (map (fn [id]
              [id
               (d/q '[:find ?token .
                      :in $ ?id
                      :where
                      [?e :stage5/id ?id]
                      [?e :stage5/unique-token ?token]]
                    db id)]))
       vec))

(defn- canonical-state
  [^Database db]
  (sorted-map
    :basis-t (d/basis-t db)
    :cas-events (cas-events db)
    :counter (counter-value db)
    :counter-history (counter-history db)
    :database-id (.id db)
    :ordered-events (ordered-events db)
    :rejected-cas-sentinel-present?
    (entity-present? db "rejected-cas-sentinel")
    :rejected-unique-sentinel-present?
    (entity-present? db "rejected-unique-sentinel")
    :unique-owner-state (unique-owner-state db)))

(defn- ensure-final-state!
  [^Database db]
  (ensure! (= 3 (counter-value db))
           "Final counter differs"
           {:actual (counter-value db)})
  (let [events (cas-events db)
        accepted (filterv #(= -1 (nth % 1)) events)]
    (ensure! (= 3 (count events))
             "Final CAS event count differs"
             {:actual events})
    (ensure! (= [["accepted-cas" -1 -1 1]]
                (mapv #(subvec % 0 4) accepted))
             "Accepted CAS event differs"
             {:actual accepted})
    (doseq [round (range cas-rounds)]
      (let [round-events (filterv #(= round (nth % 1)) events)
            [id _ worker value] (first round-events)]
        (ensure! (= 1 (count round-events))
                 "Final state does not contain exactly one CAS winner event"
                 {:events round-events :round round})
        (ensure! (and (= id (format "cas-round-%02d-worker-%02d"
                                     round worker))
                      (<= 0 worker (dec cas-workers))
                      (= value (+ 2 round)))
                 "Final CAS winner event identity differs"
                 {:event (first round-events) :round round}))))
  (ensure! (= (mapv (fn [worker]
                      [(format "ordered-%02d" worker) worker])
                    (range ordered-workers))
              (mapv #(subvec % 0 2) (ordered-events db)))
           "Final ordered events differ"
           {:actual (ordered-events db)})
  (ensure! (= [["unique-owner-a" "owned-token"]
               ["unique-owner-b" nil]]
              (unique-owner-state db))
           "Unique owner state differs"
           {:actual (unique-owner-state db)})
  (ensure! (not (entity-present? db "rejected-cas-sentinel"))
           "Rejected CAS companion entity survived"
           {})
  (ensure! (not (entity-present? db "rejected-unique-sentinel"))
           "Rejected uniqueness companion entity survived"
           {})
  (let [history (counter-history db)]
    (ensure! (= 7 (count history))
             "Counter history does not contain exactly seven datoms"
             {:history history})
    (ensure! (= [[0 true] [0 false] [1 true] [1 false]
                 [2 true] [2 false] [3 true]]
                (mapv #(subvec % 0 2) history))
             "Counter history values/retractions differ"
             {:history history}))
  db)

(defn- sql-log-root
  [jdbc-url user password database-id]
  (Class/forName "org.postgresql.Driver")
  (with-open [connection (DriverManager/getConnection jdbc-url user password)
              statement (.prepareStatement
                          connection
                          (str "select id, rev, map, val "
                               "from public.datomic_kvs where id = ?"))]
    (.setString statement 1 (str "pod-log-tail/" database-id))
    (with-open [result-set (.executeQuery statement)]
      (ensure! (.next result-set)
               "PostgreSQL log-root row is absent"
               {:database-id database-id})
      (let [revision (.getLong result-set "rev")
            revision-null? (.wasNull result-set)
            state (sorted-map
                    :id (.getString result-set "id")
                    :map (.getString result-set "map")
                    :rev (when-not revision-null? revision)
                    :val-hex (bytes->hex (.getBytes result-set "val")))]
        (ensure! (not (.next result-set))
                 "PostgreSQL returned duplicate log-root rows"
                 {:database-id database-id})
        state))))

(defn- root-advanced!
  [before after label]
  (ensure! (not= before after)
           "Successful transaction did not change the PostgreSQL log root"
           {:operation label})
  (ensure! (and (integer? (:rev before))
                (integer? (:rev after))
                (> (:rev after) (:rev before)))
           "Successful transaction did not advance the log-root revision"
           {:after-revision (:rev after)
            :before-revision (:rev before)
            :operation label})
  after)

(defn- stale-cas-rejection!
  [connection jdbc-url user password database-id]
  (let [before-db (d/db connection)
        before-basis (d/basis-t before-db)
        before-history (counter-history before-db)
        before-root (sql-log-root jdbc-url user password database-id)
        outcome (capture
                  #(transact-before!
                     connection
                     [{:stage5/id "rejected-cas-sentinel"
                       :stage5/kind :stage5/rejected}
                      [:db.fn/cas [:stage5/id "counter"]
                       :stage5/counter 999 1]]
                     (deadline-after-ms transaction-timeout-ms)
                     :stale-cas))
        profile (require-error! outcome :db.error/cas-failed true)
        after-db (d/db connection)
        after-root (sql-log-root jdbc-url user password database-id)]
    (ensure! (= before-basis (d/basis-t after-db))
             "Rejected CAS advanced basis"
             {:after-basis (d/basis-t after-db) :before-basis before-basis})
    (ensure! (= 0 (counter-value after-db))
             "Rejected CAS changed the counter"
             {:actual (counter-value after-db)})
    (ensure! (not (entity-present? after-db "rejected-cas-sentinel"))
             "Rejected CAS persisted its companion entity"
             {})
    (ensure! (= before-history (counter-history after-db))
             "Rejected CAS changed counter history"
             {})
    (ensure! (= before-root after-root)
             "Rejected CAS changed the PostgreSQL log root"
             {:after after-root :before before-root})
    (sorted-map
      :basis-unchanged? true
      :error-profile profile
      :log-root-unchanged? true
      :outcome :rejected
      :sentinel-absent? true)))

(defn- accepted-cas!
  [connection jdbc-url user password database-id]
  (let [before-db (d/db connection)
        before-basis (d/basis-t before-db)
        before-root (sql-log-root jdbc-url user password database-id)
        report (transact-before!
                 connection
                 [[:db.fn/cas [:stage5/id "counter"]
                   :stage5/counter 0 1]
                  {:stage5/id "accepted-cas"
                   :stage5/kind :stage5/cas-event
                   :stage5/round -1
                   :stage5/worker -1
                   :stage5/counter 1}]
                 (deadline-after-ms transaction-timeout-ms)
                 :accepted-cas)
        after-db (:db-after report)
        after-root (sql-log-root jdbc-url user password database-id)]
    (ensure! (= before-basis (d/basis-t (:db-before report)))
             "Accepted CAS report db-before differs"
             {})
    (ensure! (> (d/basis-t after-db) before-basis)
             "Accepted CAS did not advance basis"
             {:after-basis (d/basis-t after-db) :before-basis before-basis})
    (ensure! (= 1 (counter-value after-db))
             "Accepted CAS did not set counter to one"
             {:actual (counter-value after-db)})
    (ensure! (entity-present? after-db "accepted-cas")
             "Accepted CAS event is absent"
             {})
    (ensure! (= (d/basis-t after-db)
                (fact-t after-db "accepted-cas" :stage5/round))
             "Accepted CAS event transaction differs from report basis"
             {:after-basis (d/basis-t after-db)
              :event-t (fact-t after-db "accepted-cas" :stage5/round)})
    (root-advanced! before-root after-root :accepted-cas)
    (sorted-map
      :after-basis-t (d/basis-t after-db)
      :before-basis-t before-basis
      :event-t (fact-t after-db "accepted-cas" :stage5/round)
      :post-return-log-root-observed-advanced? true
      :log-root-revision-after (:rev after-root)
      :log-root-revision-before (:rev before-root))))

(defn- uniqueness-rejection!
  [connection jdbc-url user password database-id]
  (let [before-db (d/db connection)
        before-basis (d/basis-t before-db)
        before-state (unique-owner-state before-db)
        before-root (sql-log-root jdbc-url user password database-id)
        outcome (capture
                  #(transact-before!
                     connection
                     [{:stage5/id "rejected-unique-sentinel"
                       :stage5/kind :stage5/rejected}
                      [:db/add [:stage5/id "unique-owner-b"]
                       :stage5/unique-token "owned-token"]]
                     (deadline-after-ms transaction-timeout-ms)
                     :unique-conflict))
        profile (require-error! outcome :db.error/unique-conflict false)
        after-db (d/db connection)
        after-root (sql-log-root jdbc-url user password database-id)]
    (ensure! (= before-basis (d/basis-t after-db))
             "Rejected uniqueness transaction advanced basis"
             {:after-basis (d/basis-t after-db) :before-basis before-basis})
    (ensure! (= before-state (unique-owner-state after-db))
             "Rejected uniqueness transaction changed ownership"
             {:after (unique-owner-state after-db) :before before-state})
    (ensure! (not (entity-present? after-db "rejected-unique-sentinel"))
             "Rejected uniqueness transaction persisted its companion entity"
             {})
    (ensure! (= before-root after-root)
             "Rejected uniqueness transaction changed the PostgreSQL log root"
             {:after after-root :before before-root})
    (sorted-map
      :basis-unchanged? true
      :error-profile profile
      :log-root-unchanged? true
      :outcome :rejected
      :sentinel-absent? true)))

(defn- cas-round-tx
  [round expected worker]
  [[:db.fn/cas [:stage5/id "counter"]
    :stage5/counter expected (inc expected)]
   {:stage5/id (format "cas-round-%02d-worker-%02d" round worker)
    :stage5/kind :stage5/cas-event
    :stage5/round round
    :stage5/worker worker
    :stage5/counter (inc expected)}])

(defn- cas-attempt
  [connection round expected worker ready start deadline]
  (.countDown ^CountDownLatch ready)
  (await-latch! start deadline :cas-start)
  (let [outcome (capture
                  #(transact-before!
                     connection
                     (cas-round-tx round expected worker)
                     deadline
                     :cas-contention))]
    (if-let [report (:returned outcome)]
      (sorted-map
        :after-basis-t (d/basis-t (:db-after report))
        :before-basis-t (d/basis-t (:db-before report))
        :outcome :success
        :worker worker)
      (let [profile (require-error! outcome :db.error/cas-failed true)]
        (sorted-map
          :error-profile profile
          :outcome :cas-conflict
          :worker worker)))))

(defn- run-cas-round!
  [^ExecutorService executor connection round expected deadline]
  (let [ready (CountDownLatch. cas-workers)
        start (CountDownLatch. 1)
        tasks (mapv
                (fn [worker]
                  (submit-call!
                    executor
                    #(cas-attempt connection round expected worker
                                  ready start deadline)))
                (range cas-workers))]
    (await-latch! ready deadline :cas-workers-ready)
    (.countDown start)
    (let [outcomes (mapv #(get-before! % deadline :cas-worker) tasks)
          frequencies (frequencies (map :outcome outcomes))
          winner (first (filter #(= :success (:outcome %)) outcomes))]
      (ensure! (= 1 (get frequencies :success 0))
               "CAS contention round did not have exactly one winner"
               {:outcomes frequencies :round round})
      (ensure! (= (dec cas-workers) (get frequencies :cas-conflict 0))
               "CAS contention round did not reject every loser"
               {:outcomes frequencies :round round})
      (ensure! (> (:after-basis-t winner) (:before-basis-t winner))
               "CAS winner did not advance basis"
               {:round round :winner winner})
      (sorted-map
        :after-basis-t (:after-basis-t winner)
        :before-basis-t (:before-basis-t winner)
        :conflict-count (dec cas-workers)
        :round round
        :success-count 1
        :winner-worker (:worker winner)))))

(defn- cas-contention!
  [connection jdbc-url user password database-id]
  (let [executor (fixed-executor cas-workers "stage5-cas")
        deadline (deadline-after-ms concurrent-timeout-ms)]
    (try
      (let [start-basis (d/basis-t (d/db connection))
            rounds
            (mapv
              (fn [round]
                (let [expected (inc round)
                      round-before-basis (d/basis-t (d/db connection))
                      round-before-root
                      (sql-log-root jdbc-url user password database-id)]
                  (ensure! (= expected (counter-value (d/db connection)))
                           "Counter did not begin CAS round at expected value"
                           {:actual (counter-value (d/db connection))
                            :expected expected
                            :round round})
                  (let [result (run-cas-round!
                                 executor connection round expected deadline)
                        db (d/db connection)
                        round-events
                        (filterv #(= round (nth % 1)) (cas-events db))
                        winner-worker (:winner-worker result)
                        winner-id (format "cas-round-%02d-worker-%02d"
                                          round winner-worker)
                        round-after-root
                        (sql-log-root jdbc-url user password database-id)]
                    (ensure! (= round-before-basis (:before-basis-t result))
                             "CAS winner report did not chain from prior basis"
                             {:prior-basis round-before-basis
                              :result result
                              :round round})
                    (ensure! (= (:after-basis-t result) (d/basis-t db))
                             "CAS losers advanced basis after the sole winner"
                             {:actual-basis (d/basis-t db)
                              :round round
                              :winner-after-basis (:after-basis-t result)})
                    (ensure! (= 1 (count round-events))
                             "CAS contention persisted a loser companion event"
                             {:events round-events :round round})
                    (ensure! (= [winner-id round winner-worker (inc expected)
                                 (:after-basis-t result)]
                                (first round-events))
                             "CAS winner report and persisted event differ"
                             {:event (first round-events)
                              :round round
                              :winner result})
                    (doseq [loser-worker (remove #{winner-worker}
                                                 (range cas-workers))]
                      (ensure! (not (entity-present?
                                      db
                                      (format "cas-round-%02d-worker-%02d"
                                              round loser-worker)))
                               "CAS loser companion entity survived"
                               {:loser-worker loser-worker :round round}))
                    (root-advanced! round-before-root round-after-root
                                    :concurrent-cas)
                    (ensure! (= (inc (:rev round-before-root))
                                (:rev round-after-root))
                             "CAS contention round published more than one log-root revision"
                             {:after-revision (:rev round-after-root)
                              :before-revision (:rev round-before-root)
                              :round round})
                    (ensure! (= (inc expected) (counter-value db))
                             "CAS round did not advance counter exactly once"
                             {:actual (counter-value db) :round round})
                    (ensure! (= (inc (inc round))
                                (count (cas-events db)))
                             "CAS round persisted a duplicate or missing event"
                             {:events (cas-events db) :round round})
                    (assoc result
                           :event-identity :exact
                           :log-root-revision-after (:rev round-after-root)
                           :log-root-revision-before (:rev round-before-root)))))
              (range cas-rounds))
            winner-ts (mapv :after-basis-t rounds)]
        (ensure! (and (= start-basis (:before-basis-t (first rounds)))
                      (strictly-increasing? winner-ts))
                 "CAS winner transaction order is not monotonic"
                 {:start-basis start-basis :winner-ts winner-ts})
        (sorted-map
          :conflict-count (* cas-rounds (dec cas-workers))
          :one-published-root-revision-per-round? true
          :rounds rounds
          :success-count cas-rounds
          :winner-ts winner-ts
          :worker-count cas-workers))
      (finally
        (shutdown-executor! executor)))))

(defn- ordered-attempt
  [connection worker ready start deadline]
  (.countDown ^CountDownLatch ready)
  (await-latch! start deadline :ordered-start)
  (let [report (transact-before!
                 connection
                 [{:stage5/id (format "ordered-%02d" worker)
                   :stage5/kind :stage5/ordered-event
                   :stage5/worker worker}]
                 deadline
                 :ordered-transaction)]
    (sorted-map
      :after-basis-t (d/basis-t (:db-after report))
      :before-basis-t (d/basis-t (:db-before report))
      :worker worker)))

(defn- ordered-successes!
  [connection jdbc-url user password database-id]
  (let [executor (fixed-executor ordered-workers "stage5-ordered")
        deadline (deadline-after-ms concurrent-timeout-ms)
        before-db (d/db connection)
        before-basis (d/basis-t before-db)
        before-root (sql-log-root jdbc-url user password database-id)
        ready (CountDownLatch. ordered-workers)
        start (CountDownLatch. 1)
        tasks (mapv
                (fn [worker]
                  (submit-call!
                    executor
                    #(ordered-attempt connection worker ready start deadline)))
                (range ordered-workers))]
    (try
      (await-latch! ready deadline :ordered-workers-ready)
      (.countDown start)
      (let [reports (mapv #(get-before! % deadline :ordered-worker) tasks)
            reports-by-t (vec (sort-by :after-basis-t reports))
            ordered-ts (mapv :after-basis-t reports-by-t)
            expected-before-chain (vec (cons before-basis (butlast ordered-ts)))
            after-db (d/db connection)
            event-rows (ordered-events after-db)
            event-ts (mapv #(nth % 2) event-rows)
            report-worker-ts
            (into (sorted-map)
                  (map (juxt :worker :after-basis-t) reports))
            event-worker-ts
            (into (sorted-map)
                  (map (fn [[_ worker transaction-t]]
                         [worker transaction-t])
                       event-rows))
            after-root (sql-log-root jdbc-url user password database-id)]
        (doseq [report reports]
          (ensure! (> (:after-basis-t report) (:before-basis-t report))
                   "Concurrent accepted transaction did not advance basis"
                   {:report report}))
        (ensure! (and (strictly-increasing? ordered-ts)
                      (= ordered-workers (count (set ordered-ts))))
                 "Concurrent accepted transactions did not receive one strict order"
                 {:actual ordered-ts})
        (ensure! (= expected-before-chain
                    (mapv :before-basis-t reports-by-t))
                 "Concurrent accepted transaction reports do not form one basis chain"
                 {:actual (mapv :before-basis-t reports-by-t)
                  :expected expected-before-chain})
        (ensure! (= ordered-ts (vec (sort event-ts)))
                 "Persisted ordered-event transaction ids differ from reports"
                 {:event-ts event-ts :expected ordered-ts})
        (ensure! (= report-worker-ts event-worker-ts)
                 "Persisted worker transaction ids differ from their reports"
                 {:event-worker-ts event-worker-ts
                  :report-worker-ts report-worker-ts})
        (ensure! (= (last ordered-ts) (d/basis-t after-db))
                 "Final basis differs from accepted transaction order"
                 {:actual (d/basis-t after-db) :expected (last ordered-ts)})
        (ensure! (= ordered-workers (count (set event-ts)))
                 "Concurrent accepted transactions share or duplicate transaction ids"
                 {:event-ts event-ts})
        (root-advanced! before-root after-root :concurrent-accepted)
        (sorted-map
          :commit-order (mapv #(select-keys %
                                           [:after-basis-t :before-basis-t :worker])
                              reports-by-t)
          :event-order (vec (sort-by #(nth % 2) event-rows))
          :log-root-revision-after (:rev after-root)
          :log-root-revision-before (:rev before-root)
          :success-count ordered-workers
          :worker-t-map report-worker-ts))
      (finally
        (shutdown-executor! executor)))))

(defn- exercise!
  [{:keys [expected-created expected-source-protocol jdbc-url password uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        created? (boolean
                   (bounded-call! connect-timeout-ms
                                  :create-database
                                  #(d/create-database uri)))
        connection (atom nil)]
    (ensure! (= expected-created created?)
             "Unexpected create-database result"
             {:actual created? :expected expected-created})
    (try
      (reset! connection (connect! uri))
      (transact-before! @connection (schema-tx)
                        (deadline-after-ms transaction-timeout-ms)
                        :schema)
      (transact-before! @connection (seed-tx)
                        (deadline-after-ms transaction-timeout-ms)
                        :seed)
      (let [seed-db (d/db @connection)
            database-id (.id ^Database seed-db)
            _ (ensure! (= 0 (counter-value seed-db))
                       "Seed counter differs"
                       {:actual (counter-value seed-db)})
            stale-cas (stale-cas-rejection!
                        @connection jdbc-url user password database-id)
            accepted-cas (accepted-cas!
                           @connection jdbc-url user password database-id)
            unique-conflict (uniqueness-rejection!
                              @connection jdbc-url user password database-id)
            contention (cas-contention!
                         @connection jdbc-url user password database-id)
            ordered (ordered-successes!
                      @connection jdbc-url user password database-id)
            final-db (ensure-final-state! (d/db @connection))
            canonical (canonical-state final-db)]
        (sorted-map
          :accepted-return-with-advanced-log-root true
          :accepted-cas accepted-cas
          :candidate-boundary boundary
          :canonical-sha256 (sha256 canonical)
          :cas-conflict-count (:conflict-count contention)
          :concurrent-cas-single-root-publication-per-round
          (:one-published-root-revision-per-round? contention)
          :concurrent-cas-winner-event-identity :exact
          :cas-success-count (:success-count contention)
          :created? created?
          :database-id database-id
          :final-basis-t (d/basis-t final-db)
          :ordered-commit-ts (mapv :after-basis-t (:commit-order ordered))
          :ordered-success-count (:success-count ordered)
          :ordered-worker-transaction-identity :exact
          :serial-rejections-basis-and-log-root :unchanged
          :serial-rejection-log-roots-unchanged true
          :stale-cas :rejected
          :stale-cas-error-profile (:error-profile stale-cas)
          :status :passed
          :transaction-t-order :strict-monotonic-gaps-permitted
          :unique-conflict :rejected
          :unique-conflict-error-profile (:error-profile unique-conflict)))
      (finally
        (release! @connection)))))

(defn- audit!
  [{:keys [expected-canonical-sha expected-database-id expected-final-basis
           expected-source-protocol jdbc-url password uri user]}]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        connection (connect! uri)]
    (try
      (let [db (ensure-final-state! (d/db connection))
            canonical (canonical-state db)
            database-id (.id ^Database db)
            basis (d/basis-t db)
            canonical-sha (sha256 canonical)
            root (sql-log-root jdbc-url user password database-id)]
        (ensure! (= expected-database-id database-id)
                 "Fresh audit database identity differs"
                 {:actual database-id :expected expected-database-id})
        (ensure! (= expected-final-basis basis)
                 "Fresh audit basis differs"
                 {:actual basis :expected expected-final-basis})
        (ensure! (= expected-canonical-sha canonical-sha)
                 "Fresh audit canonical state differs"
                 {:actual canonical-sha :expected expected-canonical-sha})
        (ensure! (and (integer? (:rev root)) (not (neg? (:rev root))))
                 "Fresh audit PostgreSQL log root has invalid revision"
                 {:root root})
        (sorted-map
          :candidate-boundary boundary
          :canonical-sha256 canonical-sha
          :database-id database-id
          :final-basis-t basis
          :log-root-revision (:rev root)
          :sql-log-root :present
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
      "exercise"
      (let [[_ uri jdbc-url user password expected-created source-protocol
             & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password expected-created
                              source-protocol])
                      (empty? extra))
                 "exercise requires exactly seven arguments"
                 {:argument-count (count args)})
        {:expected-created (parse-boolean! expected-created)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :uri (require-uri! uri)
         :user user})

      "audit"
      (let [[_ uri jdbc-url user password source-protocol database-id
             canonical-sha final-basis & extra] args]
        (ensure! (and (every? some?
                             [uri jdbc-url user password source-protocol
                              database-id canonical-sha final-basis])
                      (empty? extra))
                 "audit requires exactly nine arguments"
                 {:argument-count (count args)})
        (ensure! (re-matches #"[0-9a-f]{64}" canonical-sha)
                 "EXPECTED_CANONICAL_SHA must be lowercase SHA-256"
                 {})
        {:expected-canonical-sha canonical-sha
         :expected-database-id database-id
         :expected-final-basis (parse-long! :expected-final-basis final-basis)
         :expected-source-protocol (parse-source-protocol! source-protocol)
         :jdbc-url (require-jdbc-url! jdbc-url)
         :mode mode
         :password password
         :uri (require-uri! uri)
         :user user})

      (fail! "Unknown Stage 5 transaction mode" {:mode mode}))))

(defn- run-command
  [{:keys [mode] :as command}]
  (case mode
    "exercise" (exercise! command)
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
      (println (str (if (= "audit" mode) audit-prefix result-prefix)
                    (pr-str (:returned operation))))
      (do
        (binding [*out* *err*]
          (println
            (str error-prefix
                 (pr-str
                   (sorted-map
                     :cleanup-error
                     (some-> cleanup :thrown error-profile)
                     :mode mode
                     :operation-error
                     (some-> operation :thrown error-profile))))))
        (System/exit 1)))))

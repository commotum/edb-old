(ns stage3.peer-probe
  "Deterministic PostgreSQL-backed peer checks for Stage 3.

  Every mode is intended to run in its own JVM, with the recovered artifact
  first on the classpath and without the original peer/core2 AOT jars:

    clojure.main -m stage3.peer-probe seed URI EXPECTED_CREATED
    clojure.main -m stage3.peer-probe query-controls URI
    clojure.main -m stage3.peer-probe lifecycle-race URI
    clojure.main -m stage3.peer-probe contention URI
    clojure.main -m stage3.peer-probe audit URI

  The modes form a strict state machine.  A later mode refuses to run unless
  the database has exactly the canonical state left by its predecessors."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.api :as d])
  (:import [datomic Datom]
           [java.io File]
           [java.math BigInteger]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]
           [java.util.concurrent Callable CountDownLatch ExecutionException
            ExecutorService Executors Future ThreadFactory TimeUnit
            TimeoutException]
           [java.util.concurrent.atomic AtomicLong]))

(set! *warn-on-reflection* true)

(def ^:private result-prefix "STAGE3-RESULT ")
(def ^:private error-prefix "STAGE3-ERROR ")
(def ^:private baseline-count 32)
(def ^:private contention-workers 8)
(def ^:private contention-rounds 8)
(def ^:private connect-deadline-ms 30000)
(def ^:private transaction-deadline-ms 30000)
(def ^:private query-deadline-ms 5000)
(def ^:private lifecycle-deadline-ms 30000)
(def ^:private contention-deadline-ms 90000)
(def ^:private query-timeout-ms 30)
(def ^:private cleanup-deadline-ms 30000)

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
      (.awaitTermination executor 2 TimeUnit/SECONDS)
      (catch InterruptedException _
        (.interrupt (Thread/currentThread)))))
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
  (let [executor (fixed-executor 1 (str "stage3-" (name label)))
        deadline (deadline-after-ms milliseconds)
        task (submit-call! executor f)]
    (try
      (get-before! task deadline label)
      (finally
        (shutdown-executor! executor)))))

(defn- connect!
  [uri]
  (bounded-call! connect-deadline-ms :connect #(d/connect uri)))

(defn- transact-before!
  [connection tx-data deadline label]
  (get-before! ^Future (d/transact-async connection tx-data) deadline label))

(defn- release-quietly!
  [connection]
  (when connection
    (try
      (bounded-call! connect-deadline-ms :release #(d/release connection))
      (catch Throwable _ nil)))
  nil)

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- cause-messages
  [^Throwable throwable]
  (vec (keep #(.getMessage ^Throwable %) (cause-chain throwable))))

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
      :probe-errors (vec (keep #(some-> % ex-data ::error) causes))
      :stage3-reasons (vec (keep #(some-> % ex-data :stage3/reason) causes)))))

(defn- capture
  [f]
  (try
    (sorted-map :returned (f))
    (catch Throwable throwable
      (sorted-map :thrown throwable))))

(defn- bounded-capture
  [milliseconds label f]
  (capture #(bounded-call! milliseconds label f)))

(defn cancel-query-predicate
  "Named query predicate used to prove explicit d/cancel propagation."
  [_]
  (d/cancel
    {:cognitect.anomalies/category :cognitect.anomalies/conflict
     :cognitect.anomalies/message "Stage 3 deterministic query cancellation"
     :stage3/reason :explicit-query-cancel}))

(defn slow-query-predicate
  "Named query predicate that gives the Datalog timeout scheduler a stable window."
  [_]
  (Thread/sleep 10)
  true)

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
  []
  (let [entries (classpath-entries)
        forbidden (vec (filter forbidden-classpath-entry? entries))
        api-source (io/resource "datomic/api.clj")
        peer-source (io/resource "datomic/peer.clj")
        core2-async-source (io/resource "datomic/core2/async.clj")
        api-aot (io/resource "datomic/api__init.class")
        peer-aot (io/resource "datomic/peer__init.class")
        core2-async-aot (io/resource "datomic/core2/async__init.class")]
    (ensure! (empty? forbidden)
             "Forbidden Datomic implementation is on the candidate classpath"
             {:forbidden-classpath-entry-count (count forbidden)})
    (doseq [[resource-name resource]
            [["datomic/api.clj" api-source]
             ["datomic/peer.clj" peer-source]
             ["datomic/core2/async.clj" core2-async-source]]]
      (ensure! resource
               "Recovered candidate source is not visible"
               {:resource resource-name})
      (ensure! (= "jar" (.getProtocol ^java.net.URL resource))
               "Recovered candidate source must load from the packaged artifact"
               {:resource resource-name
                :protocol (.getProtocol ^java.net.URL resource)}))
    (ensure! (and (nil? api-aot) (nil? peer-aot) (nil? core2-async-aot))
             "Original peer/core2 AOT implementation is visible"
             {:api-aot-visible? (boolean api-aot)
              :core2-async-aot-visible? (boolean core2-async-aot)
              :peer-aot-visible? (boolean peer-aot)})
    (sorted-map
      :api-source-protocol "jar"
      :core2-async-aot-visible? false
      :core2-async-source-protocol "jar"
      :forbidden-implementation-entry-count 0
      :peer-aot-visible? false
      :peer-source-protocol "jar")))

(defn- schema-tx
  []
  [{:db/ident :stage3/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage3/kind
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage3/value
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :stage3/counter
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :stage3/round
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])

(defn- expected-baseline-rows
  []
  (mapv (fn [n]
          [(format "baseline-%02d" n) :stage3/baseline n])
        (range baseline-count)))

(defn- baseline-entities
  []
  (mapv (fn [n]
          {:stage3/id (format "baseline-%02d" n)
           :stage3/kind :stage3/baseline
           :stage3/value n})
        (range baseline-count)))

(defn- baseline-rows
  [db]
  (->> (d/q '[:find ?id ?kind ?value
              :where
              [?e :stage3/kind :stage3/baseline]
              [?e :stage3/id ?id]
              [?e :stage3/kind ?kind]
              [?e :stage3/value ?value]]
            db)
       (sort-by first)
       vec))

(def ^:private marker-kinds
  #{:stage3/query-controls :stage3/lifecycle})

(defn- marker-rows
  [db]
  (->> (d/q '[:find ?id ?kind ?value
              :where
              [?e :stage3/id ?id]
              [?e :stage3/kind ?kind]
              [?e :stage3/value ?value]]
            db)
       (filter #(contains? marker-kinds (nth % 1)))
       (sort-by first)
       vec))

(defn- expected-marker-rows
  [query-controls? lifecycle?]
  (cond-> []
    lifecycle?
    (conj ["lifecycle-sentinel" :stage3/lifecycle 1])

    query-controls?
    (conj ["query-controls-sentinel" :stage3/query-controls 1])

    true
    (->> (sort-by first) vec)))

(defn- expected-contention-rows
  []
  (mapv (fn [round]
          [(format "contention-round-%02d" round)
           round
           (inc round)])
        (range contention-rounds)))

(defn- contention-rows
  [db]
  (->> (d/q '[:find ?id ?round ?value
              :where
              [?e :stage3/kind :stage3/contention-event]
              [?e :stage3/id ?id]
              [?e :stage3/round ?round]
              [?e :stage3/value ?value]]
            db)
       (sort-by first)
       vec))

(defn- counter-value
  [db]
  (d/q '[:find ?value .
         :where
         [?e :stage3/id "counter"]
         [?e :stage3/counter ?value]]
       db))

(defn- counter-history
  [db]
  (let [entity-id (d/entid db [:stage3/id "counter"])]
    (if entity-id
      (->> (d/datoms (d/history db) :eavt entity-id :stage3/counter)
           (map (fn [^Datom datom]
                  [(.v datom) (.added datom)]))
           (sort-by pr-str)
           vec)
      [])))

(defn- expected-counter-history
  [final-value]
  (->> (concat
         (map (fn [value] [value true]) (range (inc final-value)))
         (map (fn [value] [value false]) (range final-value)))
       (sort-by pr-str)
       vec))

(defn- state-summary
  [db]
  (let [baseline (baseline-rows db)
        markers (marker-rows db)
        events (contention-rows db)]
    (sorted-map
      :baseline-count (count baseline)
      :baseline-sha256 (sha256 baseline)
      :contention-event-count (count events)
      :contention-events-sha256 (sha256 events)
      :counter (counter-value db)
      :marker-count (count markers)
      :markers-sha256 (sha256 markers))))

(defn- ensure-base!
  [db]
  (let [actual (baseline-rows db)
        expected (expected-baseline-rows)]
    (ensure! (= expected actual)
             "Stage 3 baseline differs"
             {:expected-count (count expected)
              :actual-count (count actual)
              :expected-sha256 (sha256 expected)
              :actual-sha256 (sha256 actual)}))
  db)

(defn- ensure-stage!
  [db stage]
  (ensure-base! db)
  (let [{:keys [query-controls? lifecycle? counter events]}
        (case stage
          :seed
          {:query-controls? false :lifecycle? false :counter 0 :events []}

          :lifecycle-ready
          {:query-controls? true :lifecycle? false :counter 0 :events []}

          :contention-ready
          {:query-controls? true :lifecycle? true :counter 0 :events []}

          :audit
          {:query-controls? true
           :lifecycle? true
           :counter contention-rounds
           :events (expected-contention-rows)})
        expected-markers (expected-marker-rows query-controls? lifecycle?)
        actual-markers (marker-rows db)
        actual-events (contention-rows db)
        actual-counter (counter-value db)]
    (ensure! (= expected-markers actual-markers)
             "Stage 3 marker state differs"
             {:stage stage
              :expected expected-markers
              :actual actual-markers})
    (ensure! (= counter actual-counter)
             "Stage 3 counter differs"
             {:stage stage :expected counter :actual actual-counter})
    (ensure! (= events actual-events)
             "Stage 3 contention events differ"
             {:stage stage
              :expected-count (count events)
              :actual-count (count actual-events)
              :expected-sha256 (sha256 events)
              :actual-sha256 (sha256 actual-events)}))
  db)

(defn- canonical-audit-data
  [db]
  [(baseline-rows db)
   (marker-rows db)
   (counter-value db)
   (contention-rows db)
   (counter-history db)])

(defn- seed!
  [uri expected-created]
  (let [boundary (audit-candidate-boundary!)
        created? (boolean
                   (bounded-call! connect-deadline-ms
                                  :create-database
                                  #(d/create-database uri)))
        connection (atom nil)]
    (ensure! (= expected-created created?)
             "Unexpected create-database result"
             {:expected expected-created :actual created?})
    (try
      (reset! connection (connect! uri))
      (transact-before! @connection
                        (schema-tx)
                        (deadline-after-ms transaction-deadline-ms)
                        :seed-schema)
      (transact-before! @connection
                        (conj (baseline-entities)
                              {:stage3/id "counter"
                               :stage3/kind :stage3/counter
                               :stage3/counter 0})
                        (deadline-after-ms transaction-deadline-ms)
                        :seed-data)
      (let [db (ensure-stage! (d/db @connection) :seed)]
        (sorted-map
          :candidate-boundary boundary
          :created? created?
          :state (state-summary db)))
      (finally
        (release-quietly! @connection)))))

(defn- query-controls!
  [uri]
  (let [boundary (audit-candidate-boundary!)
        connection (connect! uri)]
    (try
      (let [before-db (ensure-stage! (d/db connection) :seed)
            cancellation
            (bounded-capture
              query-deadline-ms
              :explicit-query-cancel
              #(d/query
                 {:query '[:find ?x
                           :in [?x ...]
                           :where
                           [(stage3.peer-probe/cancel-query-predicate ?x)]]
                  :args [[0]]}))
            cancellation-error (:thrown cancellation)
            cancellation-profile (when cancellation-error
                                   (error-profile cancellation-error))
            cancellation-messages (when cancellation-error
                                    (cause-messages cancellation-error))
            timeout
            (bounded-capture
              query-deadline-ms
              :query-timeout
              #(d/query
                 {:query '[:find ?x
                           :in [?x ...]
                           :where
                           [(stage3.peer-probe/slow-query-predicate ?x)]]
                  :args [(vec (range 1000))]
                  :timeout query-timeout-ms}))
            timeout-error (:thrown timeout)
            timeout-profile (when timeout-error (error-profile timeout-error))
            timeout-messages (when timeout-error (cause-messages timeout-error))]
        (ensure! cancellation-error
                 "Explicit query cancellation returned normally"
                 {})
        (ensure! (some #{:cognitect.anomalies/conflict}
                       (:anomaly-categories cancellation-profile))
                 "Explicit query cancellation lost its anomaly category"
                 {:profile cancellation-profile})
        (ensure! (some true? (:cancelled-values cancellation-profile))
                 "Explicit query cancellation lost :datomic/cancelled"
                 {:profile cancellation-profile})
        (ensure! (some #{:explicit-query-cancel}
                       (:stage3-reasons cancellation-profile))
                 "Explicit query cancellation lost its stable reason"
                 {:profile cancellation-profile})
        (ensure! (some #{"Stage 3 deterministic query cancellation"}
                       cancellation-messages)
                 "Explicit query cancellation lost its deterministic message"
                 {:messages cancellation-messages})
        (ensure! timeout-error
                 "Timed query returned normally"
                 {})
        (ensure! (some #{"java.util.concurrent.TimeoutException"}
                       (:cause-classes timeout-profile))
                 "Timed query did not report TimeoutException"
                 {:profile timeout-profile})
        (ensure! (some #{"Query canceled: timeout elapsed"} timeout-messages)
                 "Timed query lost the Datalog timeout cause"
                 {:messages timeout-messages})
        (ensure! (= baseline-count (count (baseline-rows before-db)))
                 "Query controls changed their immutable database input"
                 {})
        (ensure-base! (d/db connection))
        (transact-before!
          connection
          [{:stage3/id "query-controls-sentinel"
            :stage3/kind :stage3/query-controls
            :stage3/value 1}]
          (deadline-after-ms transaction-deadline-ms)
          :query-controls-sentinel)
        (let [after-db (ensure-stage! (d/db connection) :lifecycle-ready)]
          (sorted-map
            :candidate-boundary boundary
            :explicit-cancellation
            (assoc cancellation-profile
                   :message "Stage 3 deterministic query cancellation")
            :post-control-baseline-count (count (baseline-rows after-db))
            :state (state-summary after-db)
            :timeout (assoc timeout-profile
                            :configured-timeout-ms query-timeout-ms
                            :message "Query canceled: timeout elapsed"
                            :outer-deadline-ms query-deadline-ms))))
      (finally
        (release-quietly! connection)))))

(defn- lifecycle-holder
  [connection ready released deadline]
  (let [db (d/db connection)]
    (.countDown ^CountDownLatch ready)
    (await-latch! released deadline :lifecycle-holder-release)
    (ensure-base! db)
    :snapshot-survived))

(defn- lifecycle-late-user
  [connection ready released deadline]
  (.countDown ^CountDownLatch ready)
  (await-latch! released deadline :lifecycle-late-release)
  (let [outcome (capture #(d/db connection))
        throwable (:thrown outcome)
        profile (when throwable (error-profile throwable))]
    (ensure! throwable
             "Connection accepted a late database request after release"
             {})
    (ensure! (some #{:db.error/connection-released} (:db-errors profile))
             "Late connection request failed with the wrong error"
             {:profile profile})
    :connection-released))

(defn- lifecycle-race!
  [uri]
  (let [boundary (audit-candidate-boundary!)
        connection (atom (connect! uri))
        reconnected (atom nil)
        executor (fixed-executor 9 "stage3-lifecycle")
        deadline (deadline-after-ms lifecycle-deadline-ms)
        ready (CountDownLatch. 8)
        released (CountDownLatch. 1)]
    (try
      (let [before-db (ensure-stage! (d/db @connection) :lifecycle-ready)
            holder-tasks
            (mapv (fn [_]
                    (submit-call!
                      executor
                      #(lifecycle-holder @connection ready released deadline)))
                  (range 4))
            late-tasks
            (mapv (fn [_]
                    (submit-call!
                      executor
                      #(lifecycle-late-user @connection ready released deadline)))
                  (range 4))
            release-task
            (submit-call!
              executor
              #(do
                 (await-latch! ready deadline :lifecycle-participants-ready)
                 (try
                   (d/release @connection)
                   :released
                   (finally
                     (.countDown released)))))]
        (ensure! (= :released
                    (get-before! release-task deadline :lifecycle-release))
                 "Connection release did not complete"
                 {})
        (ensure! (= (vec (repeat 4 :snapshot-survived))
                    (mapv #(get-before! % deadline :lifecycle-holder)
                          holder-tasks))
                 "An immutable database value did not survive connection release"
                 {})
        (ensure! (= (vec (repeat 4 :connection-released))
                    (mapv #(get-before! % deadline :lifecycle-late-user)
                          late-tasks))
                 "A late connection user was not rejected consistently"
                 {})
        (ensure-base! before-db)
        (let [post-release (capture #(d/db @connection))
              post-release-error (:thrown post-release)
              post-release-profile (when post-release-error
                                     (error-profile post-release-error))]
          (ensure! (and post-release-error
                        (some #{:db.error/connection-released}
                              (:db-errors post-release-profile)))
                   "Released connection remained usable"
                   {:profile post-release-profile})
          (reset! reconnected (connect! uri))
          (ensure! (not (identical? @connection @reconnected))
                   "Reconnect returned the released connection object"
                   {})
          (ensure-stage! (d/db @reconnected) :lifecycle-ready)
          (transact-before!
            @reconnected
            [{:stage3/id "lifecycle-sentinel"
              :stage3/kind :stage3/lifecycle
              :stage3/value 1}]
            (deadline-after-ms transaction-deadline-ms)
            :lifecycle-sentinel)
          (let [after-db (ensure-stage! (d/db @reconnected) :contention-ready)]
            (sorted-map
              :candidate-boundary boundary
              :held-snapshot-count 4
              :late-rejection-count 4
              :post-release-error post-release-profile
              :reconnected-object-distinct? true
              :resource-census-boundary :fresh-jvm-process-termination
              :release-completed? true
              :state (state-summary after-db)))))
      (finally
        (shutdown-executor! executor)
        (release-quietly! @reconnected)
        (release-quietly! @connection)))))

(defn- contention-tx
  [round]
  [[:db.fn/cas
    [:stage3/id "counter"]
    :stage3/counter
    round
    (inc round)]
   {:stage3/id (format "contention-round-%02d" round)
    :stage3/kind :stage3/contention-event
    :stage3/round round
    :stage3/value (inc round)}])

(defn- contention-attempt
  [connection round worker ready start deadline]
  (.countDown ^CountDownLatch ready)
  (await-latch! start deadline :contention-start)
  (let [outcome (capture
                  #(transact-before!
                     connection
                     (contention-tx round)
                     deadline
                     :contention-transaction))]
    (if (contains? outcome :returned)
      (sorted-map :outcome :success :worker worker)
      (let [profile (error-profile (:thrown outcome))]
        (if (some #{:db.error/cas-failed} (:db-errors profile))
          (do
            (ensure! (some true? (:cancelled-values profile))
                     "CAS loser lost :datomic/cancelled true"
                     {:profile profile :round round :worker worker})
            (ensure! (some #{:cognitect.anomalies/conflict}
                           (:anomaly-categories profile))
                     "CAS loser lost its conflict anomaly category"
                     {:profile profile :round round :worker worker})
            (sorted-map :outcome :cas-conflict :worker worker))
          (sorted-map :error profile :outcome :unexpected-error :worker worker))))))

(defn- run-contention-round!
  [^ExecutorService executor connection round deadline]
  (let [ready (CountDownLatch. contention-workers)
        start (CountDownLatch. 1)
        tasks (mapv
                (fn [worker]
                  (submit-call!
                    executor
                    #(contention-attempt
                       connection round worker ready start deadline)))
                (range contention-workers))]
    (await-latch! ready deadline :contention-workers-ready)
    (.countDown start)
    (let [outcomes (mapv #(get-before! % deadline :contention-worker) tasks)
          frequencies (frequencies (map :outcome outcomes))
          unexpected (vec (filter #(= :unexpected-error (:outcome %)) outcomes))]
      (ensure! (empty? unexpected)
               "Contention worker failed unexpectedly"
               {:round round
                :errors (mapv :error unexpected)})
      (ensure! (= 1 (get frequencies :success 0))
               "CAS contention round did not have exactly one winner"
               {:round round :outcomes frequencies})
      (ensure! (= (dec contention-workers)
                  (get frequencies :cas-conflict 0))
               "CAS contention round did not reject every losing writer"
               {:round round :outcomes frequencies})
      (sorted-map
        :cas-conflict-count (dec contention-workers)
        :round round
        :success-count 1))))

(defn- contention!
  [uri]
  (let [boundary (audit-candidate-boundary!)
        connection (connect! uri)
        executor (fixed-executor contention-workers "stage3-contention")
        deadline (deadline-after-ms contention-deadline-ms)]
    (try
      (ensure-stage! (d/db connection) :contention-ready)
      (let [round-results
            (mapv
              (fn [round]
                (ensure! (= round (counter-value (d/db connection)))
                         "Counter did not start contention round at its expected value"
                         {:round round
                          :actual (counter-value (d/db connection))})
                (let [result (run-contention-round!
                               executor connection round deadline)
                      db (d/db connection)
                      expected-events (subvec (expected-contention-rows)
                                              0
                                              (inc round))]
                  (ensure! (= (inc round) (counter-value db))
                           "Winning CAS did not advance the counter exactly once"
                           {:round round :actual (counter-value db)})
                  (ensure! (= expected-events (contention-rows db))
                           "Contention round persisted a non-canonical event set"
                           {:round round
                            :expected expected-events
                            :actual (contention-rows db)})
                  result))
              (range contention-rounds))
            db (ensure-stage! (d/db connection) :audit)
            expected-history (expected-counter-history contention-rounds)
            actual-history (counter-history db)]
        (ensure! (= expected-history actual-history)
                 "Counter history does not contain exactly one committed transition per round"
                 {:expected expected-history :actual actual-history})
        (sorted-map
          :cas-conflict-count (* contention-rounds (dec contention-workers))
          :candidate-boundary boundary
          :canonical-sha256 (sha256 (canonical-audit-data db))
          :rounds round-results
          :state (state-summary db)
          :success-count contention-rounds
          :worker-count contention-workers))
      (finally
        (shutdown-executor! executor)
        (release-quietly! connection)))))

(defn- audit!
  [uri]
  (let [boundary (audit-candidate-boundary!)
        connection (connect! uri)]
    (try
      (let [db (ensure-stage! (d/db connection) :audit)
            history (counter-history db)
            expected-history (expected-counter-history contention-rounds)
            canonical (canonical-audit-data db)]
        (ensure! (= expected-history history)
                 "Stage 3 audit found an incomplete or duplicated counter history"
                 {:expected expected-history :actual history})
        (sorted-map
          :candidate-boundary boundary
          :canonical-sha256 (sha256 canonical)
          :counter-history-count (count history)
          :counter-history-sha256 (sha256 history)
          :state (state-summary db)))
      (finally
        (release-quietly! connection)))))

(defn- parse-exact-boolean
  [value]
  (ensure! (contains? #{"true" "false"} value)
           "EXPECTED_CREATED must be exactly true or false"
           {:argument :expected-created})
  (= "true" value))

(defn- require-postgresql-uri!
  [uri]
  (ensure! (and (string? uri) (not (str/blank? uri)))
           "URI must not be blank"
           {:argument :uri})
  (ensure! (and (str/starts-with? uri "datomic:sql://")
                (str/includes? uri "jdbc:postgresql://"))
           "Stage 3 peer modes require a PostgreSQL-backed Datomic SQL URI"
           {:argument :uri
            :datomic-sql-prefix? (str/starts-with? uri "datomic:sql://")
            :postgresql-jdbc? (str/includes? uri "jdbc:postgresql://")})
  uri)

(defn- parse-command
  [args]
  (let [[mode uri arg] args
        argument-count (count args)]
    (ensure! mode
             "Usage: stage3.peer-probe MODE URI [EXPECTED_CREATED]"
             {:argument-count argument-count})
    (case mode
      "seed"
      (do
        (ensure! (= 3 argument-count)
                 "seed requires exactly MODE URI EXPECTED_CREATED"
                 {:argument-count argument-count})
        (sorted-map
          :arg (parse-exact-boolean arg)
          :mode mode
          :uri (require-postgresql-uri! uri)))

      ("query-controls" "lifecycle-race" "contention" "audit")
      (do
        (ensure! (= 2 argument-count)
                 "Mode requires exactly MODE URI"
                 {:mode mode :argument-count argument-count})
        (sorted-map :mode mode :uri (require-postgresql-uri! uri)))

      (fail! "Unknown Stage 3 peer mode" {:mode mode}))))

(defn- run-command
  [{:keys [mode uri arg]}]
  (case mode
    "seed" (seed! uri arg)
    "query-controls" (query-controls! uri)
    "lifecycle-race" (lifecycle-race! uri)
    "contention" (contention! uri)
    "audit" (audit! uri)))

(defn- sanitized-error
  [mode ^Throwable throwable]
  (assoc (error-profile throwable) :mode mode))

(defn- shutdown!
  []
  (try
    (bounded-call!
      cleanup-deadline-ms
      :peer-shutdown
      #(d/shutdown false))
    (finally
      (shutdown-agents)))
  :completed)

(defn- main-error
  [mode operation cleanup]
  (sorted-map
    :cleanup-error
    (some-> cleanup :thrown error-profile)
    :fresh-jvm-process-termination-boundary true
    :mode mode
    :operation-error
    (when-let [throwable (:thrown operation)]
      (sanitized-error mode throwable))))

(defn -main
  [& args]
  (let [mode (first args)
        operation
        (capture
          #(let [command (parse-command args)]
             {:command command
              :result (run-command command)}))
        cleanup (capture shutdown!)
        successful? (and (contains? operation :returned)
                         (contains? cleanup :returned))]
    (if successful?
      (let [{:keys [command result]} (:returned operation)]
        (println
          (str result-prefix
               (pr-str
                 (sorted-map
                   :fresh-jvm-process-termination-boundary true
                   :mode (keyword (:mode command))
                   :result result))))
        (flush))
      (binding [*out* *err*]
        (println
          (str error-prefix
               (pr-str (main-error mode operation cleanup))))
        (flush)))
    ;; Every invocation owns a dedicated JVM. Recovered Datomic initializes
    ;; process-global query executors that are not part of Peer/shutdown; an
    ;; explicit exit is therefore the tested process-termination boundary.
    (System/exit (if successful? 0 1))))

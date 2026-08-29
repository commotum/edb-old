(ns stage3.transport-probe
  "Bounded external-transactor interruption and recovery probe.

  Invoke in a fresh recovered-candidate JVM with one SQL URI and, optionally,
  the expected recovered-source URL protocol (default: jar). After
  STAGE3-FAULT-READY, send the exact line FAULT once the transactor has been
  stopped. After STAGE3-RESUME-READY, resume the transactor and send the exact
  line RESUME."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.api :as d])
  (:import [java.io BufferedReader File InputStreamReader]
           [java.nio.charset StandardCharsets]
           [java.util.concurrent Future TimeUnit]))

(set! *warn-on-reflection* true)

(def ^:private ready-prefix "STAGE3-FAULT-READY ")
(def ^:private fault-sync-prefix "STAGE3-FAULT-SYNC-START ")
(def ^:private unavailable-prefix "STAGE3-FAULT-UNAVAILABLE ")
(def ^:private resume-ready-prefix "STAGE3-RESUME-READY ")
(def ^:private recovery-sync-prefix "STAGE3-RECOVERY-SYNC-START ")
(def ^:private sentinel-prefix "STAGE3-SENTINEL-START ")
(def ^:private result-prefix "STAGE3-RESULT ")
(def ^:private error-prefix "STAGE3-ERROR ")

(def ^:private fault-timeout-seconds 30)
(def ^:private recovery-timeout-seconds 45)
(def ^:private transaction-timeout-seconds 30)
(def ^:private retry-delay-millis 100)
(def ^:private timeout-token (Object.))
(def ^:private unavailable-category :cognitect.anomalies/unavailable)
(def ^:private sentinel-value "stage3/transport-recovery-sentinel/v1")

(defn- fail!
  [message data]
  (throw (ex-info message (assoc data ::error :transport-probe-failed))))

(defn- ensure!
  [pred message data]
  (when-not pred
    (fail! message data)))

(defn- emit!
  [prefix value]
  (println (str prefix (pr-str value)))
  (flush))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- anomaly-categories
  [^Throwable throwable]
  (->> (cause-chain throwable)
       (keep #(some-> % ex-data :cognitect.anomalies/category))
       distinct
       vec))

(defn- unavailable?
  [^Throwable throwable]
  (= #{unavailable-category} (set (anomaly-categories throwable))))

(defn- stable-unavailable
  [^Throwable throwable]
  (sorted-map
    :category unavailable-category
    :db-errors
    (->> (cause-chain throwable)
         (keep #(some-> % ex-data :db/error))
         distinct
         sort
         vec)))

(defn- sanitized-throwable
  [^Throwable throwable]
  (let [causes (cause-chain throwable)]
    (sorted-map
      :anomaly-categories
      (vec (distinct (keep #(some-> % ex-data
                                   :cognitect.anomalies/category)
                           causes)))
      :cause-classes (mapv #(.getName (class %)) causes)
      :db-errors
      (vec (distinct (keep #(some-> % ex-data :db/error) causes)))
      :exception-class (.getName (class throwable))
      :probe-errors
      (vec (distinct (keep #(some-> % ex-data ::error) causes))))))

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
  (let [entries (classpath-entries)
        forbidden (vec (filter forbidden-classpath-entry? entries))
        peer-source (io/resource "datomic/peer.clj")
        core2-source (io/resource "datomic/core2/atom.clj")
        peer-aot (io/resource "datomic/peer__init.class")
        core2-aot (io/resource "datomic/core2/atom__init.class")]
    (ensure! (empty? forbidden)
             "Forbidden original Datomic implementation is on the classpath"
             {:forbidden-classpath-entry-count (count forbidden)})
    (ensure! peer-source
             "Recovered datomic.peer source is not visible"
             {})
    (ensure! core2-source
             "Recovered datomic.core2 source is not visible"
             {})
    (ensure! (= expected-source-protocol (.getProtocol peer-source))
             "Recovered datomic.peer must load from the candidate artifact"
             {:expected-source-protocol expected-source-protocol
              :peer-source-protocol (.getProtocol peer-source)})
    (ensure! (= expected-source-protocol (.getProtocol core2-source))
             "Recovered datomic.core2 must load from the candidate artifact"
             {:core2-source-protocol (.getProtocol core2-source)
              :expected-source-protocol expected-source-protocol})
    (ensure! (and (nil? peer-aot) (nil? core2-aot))
             "Original Peer/core2 AOT implementation must not be visible"
             {:core2-aot-visible? (boolean core2-aot)
              :peer-aot-visible? (boolean peer-aot)})
    (sorted-map
      :classpath-entry-count (count entries)
      :core2-aot-visible? false
      :core2-source-protocol expected-source-protocol
      :forbidden-implementation-entry-count 0
      :peer-aot-visible? false
      :peer-source-protocol expected-source-protocol)))

(defn- parse-arguments!
  [args]
  (ensure! (<= 1 (count args) 2)
           "Usage: transport_probe.clj URI [EXPECTED_SOURCE_PROTOCOL]"
           {:argument-count (count args)})
  (let [[uri supplied-source-protocol] args
        expected-source-protocol (or supplied-source-protocol "jar")]
    (ensure! (and (string? uri) (not (str/blank? uri)))
             "URI must not be blank"
             {:uri-present? (boolean (and (string? uri)
                                          (not (str/blank? uri))))})
    (ensure! (str/starts-with? uri "datomic:sql://")
             "URI must identify an external SQL database"
             {:sql-uri? false})
    (ensure! (#{"jar" "file"} expected-source-protocol)
             "Expected source protocol must be jar or file"
             {:expected-source-protocol expected-source-protocol})
    {:expected-source-protocol expected-source-protocol
     :uri uri}))

(defn- read-command!
  [^BufferedReader reader expected]
  (let [actual (.readLine reader)]
    (ensure! (= expected actual)
             "Unexpected transport-probe command"
             {:actual-command-present? (some? actual)
              :expected-command expected})
    actual))

(defn- deadline
  [seconds]
  (+ (System/nanoTime) (.toNanos TimeUnit/SECONDS (long seconds))))

(defn- remaining-millis
  [deadline-nanos]
  (let [remaining (- deadline-nanos (System/nanoTime))]
    (when (pos? remaining)
      (max 1 (quot (+ remaining 999999) 1000000)))))

(defn- bounded-deref!
  [pending deadline-nanos phase]
  (let [remaining (remaining-millis deadline-nanos)]
    (ensure! remaining
             "Operation exceeded its absolute deadline"
             {:phase phase})
    (let [result (deref pending remaining timeout-token)]
      (when (identical? timeout-token result)
        (when (instance? Future pending)
          (.cancel ^Future pending true))
        (fail! "Operation exceeded its absolute deadline"
               {:phase phase}))
      result)))

(defn- sync-outcome
  [conn deadline-nanos phase]
  (try
    {:status :succeeded
     :db (bounded-deref! (d/sync conn) deadline-nanos phase)}
    (catch Throwable throwable
      (if (unavailable? throwable)
        {:status :unavailable
         :error (stable-unavailable throwable)}
        (throw
          (ex-info "Sync failed with a non-unavailable error"
                   {::error :unexpected-sync-failure
                    :phase phase
                    :sync-error (sanitized-throwable throwable)}
                   throwable))))))

(defn- observe-fault!
  [conn]
  (emit! fault-sync-prefix
         (sorted-map
           :operation :zero-argument-sync
           :timeout-seconds fault-timeout-seconds))
  (let [outcome (sync-outcome
                  conn (deadline fault-timeout-seconds) :fault-sync)]
    (ensure! (= :unavailable (:status outcome))
             "Zero-argument sync succeeded while the transactor was stopped"
             {:phase :fault-sync
              :status (:status outcome)})
    (emit! unavailable-prefix
           (sorted-map
             :error (:error outcome)
             :operation :zero-argument-sync
             :status :unavailable))
    (:error outcome)))

(defn- recover-sync!
  [conn]
  (let [deadline-nanos (deadline recovery-timeout-seconds)]
    (emit! recovery-sync-prefix
           (sorted-map
             :absolute-timeout-seconds recovery-timeout-seconds
             :operation :zero-argument-sync))
    (loop [attempt-count 1
           unavailable-count 0]
      (ensure! (remaining-millis deadline-nanos)
               "Recovery sync exceeded its absolute deadline"
               {:attempt-count (dec attempt-count)
                :phase :recovery-sync
                :unavailable-attempt-count unavailable-count})
      (let [outcome (sync-outcome conn deadline-nanos :recovery-sync)]
        (case (:status outcome)
          :succeeded
          (do
            (ensure! (instance? datomic.Database (:db outcome))
                     "Recovery sync did not return a Database"
                     {:actual-class (some-> outcome :db class .getName)})
            {:attempt-count attempt-count
             :db (:db outcome)
             :unavailable-attempt-count unavailable-count})

          :unavailable
          (do
            (when-let [remaining (remaining-millis deadline-nanos)]
              (Thread/sleep (long (min retry-delay-millis remaining))))
            (recur (inc attempt-count) (inc unavailable-count))))))))

(defn- sentinel-entities
  [db]
  (->> (d/q '[:find [?e ...]
              :in $ ?value
              :where
              [?e :db/doc ?value]]
            db sentinel-value)
       sort
       vec))

(defn- commit-sentinel!
  [conn synced-db]
  (let [before-entities (sentinel-entities synced-db)]
    (ensure! (empty? before-entities)
             "Transport sentinel already exists before the recovery write"
             {:sentinel-count (count before-entities)})
    (emit! sentinel-prefix
           (sorted-map
             :operation :single-sentinel-transaction
             :timeout-seconds transaction-timeout-seconds))
    (let [tempid (d/tempid :db.part/user)
          tx-result
          (bounded-deref!
            (d/transact-async
              conn [{:db/id tempid :db/doc sentinel-value}])
            (deadline transaction-timeout-seconds)
            :sentinel-transaction)
          before-db (:db-before tx-result)
          after-db (:db-after tx-result)
          entity-id (d/resolve-tempid after-db (:tempids tx-result) tempid)
          doc-attribute (d/entid after-db :db/doc)
          sentinel-datoms
          (->> (:tx-data tx-result)
               (filter #(and (= entity-id (:e %))
                             (= doc-attribute (:a %))
                             (= sentinel-value (:v %))
                             (true? (:added %))))
               vec)
          after-entities (sentinel-entities after-db)]
      (ensure! (= 1 (count sentinel-datoms))
               "Sentinel transaction did not add exactly one sentinel datom"
               {:sentinel-datom-count (count sentinel-datoms)})
      (ensure! (= [entity-id] after-entities)
               "Committed transport sentinel was not uniquely readable"
               {:actual-entity-count (count after-entities)
                :expected-entity-present? (boolean (some #{entity-id}
                                                         after-entities))})
      (ensure! (> (d/basis-t after-db) (d/basis-t before-db))
               "Sentinel transaction did not advance the database basis"
               {:after-basis-t (d/basis-t after-db)
                :before-basis-t (d/basis-t before-db)})
      (sorted-map
        :after-basis-t (d/basis-t after-db)
        :before-basis-t (d/basis-t before-db)
        :entity-id entity-id
        :read-count (count after-entities)
        :transaction-sentinel-datom-count (count sentinel-datoms)
        :value sentinel-value))))

(defn- run-probe!
  [uri expected-source-protocol]
  (let [boundary (audit-candidate-boundary! expected-source-protocol)
        reader (BufferedReader.
                 (InputStreamReader. System/in StandardCharsets/UTF_8))
        conn (d/connect uri)]
    (try
      (let [initial-db (d/db conn)
            initial-basis-t (d/basis-t initial-db)]
        ;; No transaction is submitted before this marker or the fault sync.
        (emit! ready-prefix
               (sorted-map
                 :basis-t initial-basis-t
                 :next-command "FAULT"
                 :state :connected))
        (read-command! reader "FAULT")
        (let [fault (observe-fault! conn)]
          (emit! resume-ready-prefix
                 (sorted-map
                   :next-command "RESUME"
                   :state :unavailable-observed))
          (read-command! reader "RESUME")
          (let [recovery (recover-sync! conn)
                synced-db (:db recovery)
                sentinel (commit-sentinel! conn synced-db)]
            (sorted-map
              :candidate-boundary boundary
              :fault
              (sorted-map
                :error fault
                :operation :zero-argument-sync
                :status :unavailable)
              :initial-basis-t initial-basis-t
              :recovery
              (sorted-map
                :attempt-count (:attempt-count recovery)
                :basis-t (d/basis-t synced-db)
                :operation :zero-argument-sync
                :status :succeeded
                :unavailable-attempt-count
                (:unavailable-attempt-count recovery))
              :sentinel sentinel))))
      (finally
        (d/release conn)))))

(defn- shutdown!
  []
  (d/shutdown false)
  (shutdown-agents)
  :completed)

(defn- capture
  [f]
  (try
    {:returned (f)}
    (catch Throwable throwable
      {:thrown throwable})))

(defn -main
  [& args]
  (let [operation
        (capture
          #(let [{:keys [expected-source-protocol uri]}
                 (parse-arguments! args)]
             (run-probe! uri expected-source-protocol)))
        cleanup (capture shutdown!)
        successful? (and (contains? operation :returned)
                         (contains? cleanup :returned))]
    (if successful?
      (emit! result-prefix (:returned operation))
      (binding [*out* *err*]
        (emit!
          error-prefix
          (sorted-map
            :cleanup-error (some-> cleanup :thrown sanitized-throwable)
            :operation-error (some-> operation :thrown sanitized-throwable)))))
    (System/exit (if successful? 0 1))))

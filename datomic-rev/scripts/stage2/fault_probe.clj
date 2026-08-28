(ns stage2.fault-probe
  "Candidate-only Stage 2 backup fault and verification probe.

  Usage:
    clojure.main -m stage2.fault-probe root-store-failure SOURCE_URI BACKUP_ROOT
    clojure.main -m stage2.fault-probe leaf-faults BACKUP_ROOT T
    clojure.main -m stage2.fault-probe restore-interruption BACKUP_ROOT T TARGET_URI
    clojure.main -m stage2.fault-probe verify BACKUP_ROOT T clean|missing|unreadable

  The filesystem adapter and wrappers are test infrastructure only.  Run each
  mode in a fresh JVM with the recovered artifact first on the classpath."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.api :as d]
            [datomic.backup :as backup]
            [stage2.storage :as storage])
  (:import [java.io File StringWriter]
           [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]
           [java.util.concurrent CountDownLatch TimeUnit]))

(set! *warn-on-reflection* true)

(def ^:private result-prefix "STAGE2-FAULT-RESULT ")
(def ^:private error-prefix "STAGE2-FAULT-ERROR ")
(def ^:private backup-concurrency 4)
(def ^:private late-miss-timeout-seconds 30)
(def ^:private expectations #{"clean" "missing" "unreadable"})

(defn- fail!
  [message data]
  (throw (ex-info message (assoc data ::error :probe-failed))))

(defn- ensure!
  [pred message data]
  (when-not pred
    (fail! message data)))

(defn- hex
  [^bytes bytes]
  (apply str
         (map (fn [value]
                (format "%02x" (bit-and 0xff value)))
              bytes)))

(defn- sha256-string
  [value]
  (when (some? value)
    (let [digest (MessageDigest/getInstance "SHA-256")]
      (.update digest (.getBytes (str value) StandardCharsets/UTF_8))
      (hex (.digest digest)))))

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
        backup-source (io/resource "datomic/backup.clj")
        fsbackup-source (io/resource "datomic/fsbackup.clj")
        fsbackup-aot (io/resource "datomic/fsbackup__init.class")]
    (ensure! (empty? forbidden)
             "Forbidden original Datomic implementation on candidate classpath"
             {:forbidden-classpath-entry-count (count forbidden)})
    (ensure! backup-source
             "Recovered datomic.backup source is not visible"
             {})
    (ensure! (= "jar" (.getProtocol backup-source))
             "Recovered datomic.backup must load from the candidate artifact"
             {:backup-source-protocol (.getProtocol backup-source)})
    (ensure! (and (nil? fsbackup-source) (nil? fsbackup-aot))
             "Original datomic.fsbackup must not be visible"
             {:fsbackup-source-visible? (boolean fsbackup-source)
              :fsbackup-aot-visible? (boolean fsbackup-aot)})
    (sorted-map
      :backup-source-protocol "jar"
      :classpath-entry-count (count entries)
      :forbidden-implementation-entry-count 0
      :fsbackup-visible? false)))

(defn- parse-t
  [text]
  (try
    (let [t (Long/parseLong text)]
      (ensure! (not (neg? t)) "T must not be negative" {:argument :t :t t})
      t)
    (catch NumberFormatException _
      (fail! "T must be an integer" {:argument :t}))))

(defn- require-text!
  [label value]
  (ensure! (and (string? value) (not (str/blank? value)))
           (str label " must not be blank")
           {:argument label})
  value)

(defn- require-backup-root!
  [value]
  (require-text! :backup-root value)
  (let [root (io/file value)]
    (ensure! (and (.exists root) (.isDirectory root))
             "BACKUP_ROOT must be an existing directory"
             {:argument :backup-root})
    value))

(defn- normalize-description
  [description]
  (sorted-map
    :owner-sha256 (sha256-string (:db-id description))
    :ts (vec (:ts description))))

(defn- normalize-counts
  [counts]
  (into (sorted-map)
        (map (fn [[operation outcomes]]
               [operation (into (sorted-map) outcomes)]))
        counts))

(defn- normalize-verification
  [verification]
  (sorted-map
    :missing-segment-ids
    (vec (sort (map str (:missing-segments verification))))
    :t (:t verification)
    :total-segments (long (:total-segments verification))
    :unreadable-segment-ids
    (vec (sort (map str (:unreadable-segments verification))))))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- sanitized-throwable
  [^Throwable throwable]
  (let [causes (cause-chain throwable)]
    (sorted-map
      :cause-classes (mapv #(.getName (class %)) causes)
      :db-errors (vec (keep #(some-> % ex-data :db/error) causes))
      :exception-class (.getName (class throwable))
      :probe-errors (vec (keep #(some-> % ex-data ::error) causes))
      :storage-errors
      (vec (keep #(some-> % ex-data ::storage/error) causes)))))

(defn- count-value-keys
  [adapter]
  (count (:ks (backup/list-keys adapter "values/"))))

(defn- progress-fn
  [counter]
  (fn [event]
    (swap! counter update event (fnil inc 0))))

(defn- request-current-index!
  [source-uri]
  (let [connection (d/connect source-uri)]
    (try
      (let [requested-t (d/basis-t (d/db connection))
            request-accepted? (boolean (d/request-index connection))
            indexed-db @(d/sync-index connection requested-t)
            indexed-t (d/basis-t indexed-db)]
        (ensure! (>= indexed-t requested-t)
                 "Index did not reach the source basis before backup"
                 {:requested-t requested-t
                  :indexed-t indexed-t})
        (sorted-map
          :indexed-t indexed-t
          :request-accepted? request-accepted?
          :requested-t requested-t))
      (finally
        (d/release connection)))))

(defn- run-root-store-failure!
  [source-uri backup-root]
  (let [boundary (audit-candidate-boundary!)
        index-state (request-current-index! source-uri)
        base (storage/file-storage backup-root)
        before-description (normalize-description
                             (backup/describe-backups base))
        before-value-count (count-value-keys base)
        _ (storage/reset-operation-log! base)
        faulty (storage/root-store-failure-storage base)
        progress (atom {})
        failure (try
                  @(backup/backup-db
                     source-uri faulty (progress-fn progress)
                     backup-concurrency true)
                  nil
                  (catch Throwable throwable throwable))
        base-backup-log (storage/operation-log base)
        fault-log (storage/operation-log faulty)
        after-description (normalize-description
                            (backup/describe-backups base))
        after-value-count (count-value-keys base)
        value-store-count
        (count
          (filter #(and (= :store (:operation %))
                        (= :stored (:outcome %))
                        (str/starts-with? (:key %) "values/"))
                  (:events base-backup-log)))
        injected-count
        (get-in fault-log [:counts :store :injected-failure] 0)
        failure-data (when failure (sanitized-throwable failure))]
    (ensure! failure
             "Root-store failure injection did not fail backup-db"
             {})
    (ensure! (some #{:injected-root-store-failure}
                   (:storage-errors failure-data))
             "Backup failed for a reason other than the injected root store"
             {:failure failure-data})
    (ensure! (= 1 injected-count)
             "Expected exactly one rejected root publication"
             {:injected-root-store-failure-count injected-count})
    (ensure! (= before-description after-description)
             "Failed backup published or changed a restore point"
             {:before before-description :after after-description})
    (ensure! (pos? value-store-count)
             "Fault scenario did not copy any values before root publication"
             {:value-store-count value-store-count})
    (ensure! (> after-value-count before-value-count)
             "Fault scenario did not leave newly copied immutable values"
             {:before-value-count before-value-count
              :after-value-count after-value-count})
    (sorted-map
      :after-restore-points after-description
      :after-value-count after-value-count
      :backup-concurrency backup-concurrency
      :base-storage-counts (normalize-counts (:counts base-backup-log))
      :before-restore-points before-description
      :before-value-count before-value-count
      :candidate-boundary boundary
      :failure failure-data
      :fault-storage-counts (normalize-counts (:counts fault-log))
      :index index-state
      :mode :root-store-failure
      :new-value-count (- after-value-count before-value-count)
      :progress (into (sorted-map) @progress)
      :restore-points-unchanged? true
      :value-store-count value-store-count)))

(defn- segment-id-from-key
  [key]
  (subs key (inc (.lastIndexOf ^String key "/"))))

(defn- leaf-profile
  [adapter t]
  (storage/reset-operation-log! adapter)
  (let [segment-ids (vec (backup/backup-seg-ids adapter t))
        segment-id-strings (set (map str segment-ids))
        traversal-log (storage/operation-log adapter)
        traversal-value-keys
        (->> (:events traversal-log)
             (filter #(and (= :retrieve (:operation %))
                           (= :hit (:outcome %))
                           (str/starts-with? (:key %) "values/")))
             (map :key)
             set)
        reachable-value-keys
        (->> (:ks (backup/list-keys adapter "values/"))
             (filter #(contains? segment-id-strings
                                 (segment-id-from-key %)))
             sort
             vec)
        leaf-keys (vec (remove traversal-value-keys reachable-value-keys))
        key (first leaf-keys)
        segment-id-text (some-> key segment-id-from-key)
        segment-id (first (filter #(= segment-id-text (str %)) segment-ids))]
    (ensure! (seq segment-ids)
             "Restore point has no reachable segments"
             {:t t})
    (ensure! key
             "Could not find a reachable value leaf outside topology traversal"
             {:reachable-segment-count (count segment-ids)
              :topology-value-read-count (count traversal-value-keys)})
    (ensure! segment-id
             "Selected leaf key does not map to a reachable segment"
             {:storage-key key})
    {:key key
     :leaf-count (count leaf-keys)
     :leaf-keys leaf-keys
     :reachable-segment-count (count segment-ids)
     :segment-id segment-id
     :segment-id-text segment-id-text
     :topology-value-read-count (count traversal-value-keys)}))

(defrecord CorruptRetrieveStorage [delegate selected-key retrieval-count]
  backup/Storage
  (store [_ key buffer]
    (backup/store delegate key buffer))
  (retrieve [_ key]
    (if (= selected-key key)
      (do
        (swap! retrieval-count inc)
        {:k key :v (ByteBuffer/wrap (byte-array [0]))})
      (backup/retrieve delegate key)))
  (exists? [_ key]
    (backup/exists? delegate key))
  (list-keys [_ prefix]
    (backup/list-keys delegate prefix)))

(defn corrupt-retrieve-storage
  "Returns a test-only Storage that serves one selected key as malformed bytes."
  [delegate selected-key]
  (->CorruptRetrieveStorage delegate selected-key (atom 0)))

(defrecord LateMissingRetrieveStorage
  [delegate selected-key ^CountDownLatch copied-latch progress state]
  backup/Storage
  (store [_ key buffer]
    (backup/store delegate key buffer))
  (retrieve [_ key]
    (if (= selected-key key)
      (let [_ (swap! state update :injection-count (fnil inc 0))
            copy-observed?
            (.await copied-latch late-miss-timeout-seconds TimeUnit/SECONDS)
            copied-count (get @progress :copied 0)]
        (swap! state assoc
               :copied-count-at-miss copied-count
               :copy-observed-before-miss? copy-observed?)
        nil)
      (backup/retrieve delegate key)))
  (exists? [_ key]
    (backup/exists? delegate key))
  (list-keys [_ prefix]
    (backup/list-keys delegate prefix)))

(defn late-missing-retrieve-storage
  "Returns test-only Storage that misses one key only after copy progress.

  The selected key, exists?, and list-keys remain deterministic.  Its retrieve
  waits for the supplied copied latch before returning nil, ensuring the
  injected restore read failure is late rather than a target-creation failure."
  [delegate selected-key copied-latch progress]
  (->LateMissingRetrieveStorage
    delegate selected-key copied-latch progress
    (atom {:injection-count 0
           :copied-count-at-miss 0
           :copy-observed-before-miss? false})))

(defn- restore-progress-fn
  [counter ^CountDownLatch copied-latch]
  (fn [event]
    (swap! counter update event (fnil inc 0))
    (when (= :copied event)
      (.countDown copied-latch))))

(defn- await-progress-quiescence!
  [counter]
  (let [deadline (+ (System/nanoTime) (* 30 1000 1000 1000))]
    (loop [previous @counter
           unchanged-polls 0]
      (Thread/sleep 50)
      (let [current @counter
            unchanged-polls (if (= previous current)
                              (inc unchanged-polls)
                              0)]
        (cond
          (>= unchanged-polls 20) current
          (< (System/nanoTime) deadline)
          (recur current unchanged-polls)
          :else
          (fail! "Interrupted restore workers did not quiesce"
                 {:progress (into (sorted-map) current)}))))))

(defn- run-leaf-faults!
  [backup-root t]
  (let [boundary (audit-candidate-boundary!)
        adapter (storage/file-storage backup-root)
        {:keys [key segment-id segment-id-text]
         :as profile} (leaf-profile adapter t)
        base-hit? (boolean (backup/retrieve adapter key))
        missing (storage/missing-retrieve-storage adapter key)
        missing-retrieve? (nil? (backup/retrieve missing key))
        missing-exists? (boolean (backup/exists? missing key))
        missing-listed? (contains? (set (:ks (backup/list-keys missing "values/")))
                                   key)
        corrupt (corrupt-retrieve-storage adapter key)
        lookup (:lookup (backup/create-restore-job corrupt t))
        captured-output (StringWriter.)
        unreadable
        (binding [*out* captured-output]
          (vec (backup/unreadable-seg-ids lookup [segment-id])))
        unreadable-text (vec (sort (map str unreadable)))
        corrupt-retrieval-count @(:retrieval-count corrupt)
        missing-injection-count
        (get-in (storage/operation-log missing)
                [:counts :retrieve :injected-miss] 0)]
    (ensure! base-hit?
             "Selected leaf is not readable from base storage"
             {:storage-key key})
    (ensure! (and missing-retrieve? missing-exists? missing-listed?)
             "Missing-retrieve wrapper did not preserve advertised presence"
             {:retrieve-miss? missing-retrieve?
              :exists-advertised? missing-exists?
              :listing-advertised? missing-listed?})
    (ensure! (= 1 missing-injection-count)
             "Missing-retrieve wrapper recorded an unexpected number of misses"
             {:injected-miss-count missing-injection-count})
    (ensure! (= [segment-id-text] unreadable-text)
             "Recovered unreadable-seg-ids did not return the corrupt leaf"
             {:expected [segment-id-text]
              :actual unreadable-text
              :corrupt-retrieval-count corrupt-retrieval-count})
    (ensure! (pos? corrupt-retrieval-count)
             "Unreadable verifier did not retrieve the corrupt leaf"
             {})
    (sorted-map
      :candidate-boundary boundary
      :corrupt-wrapper
      (sorted-map
        :retrieval-count corrupt-retrieval-count
        :unreadable-segment-ids unreadable-text)
      :leaf
      (sorted-map
        :segment-id segment-id-text
        :storage-key key)
      :leaf-count (:leaf-count profile)
      :missing-wrapper
      (sorted-map
        :exists-advertised? missing-exists?
        :injected-miss-count missing-injection-count
        :listing-advertised? missing-listed?
        :retrieve-miss? missing-retrieve?)
      :mode :leaf-faults
      :reachable-segment-count (:reachable-segment-count profile)
      :t t
      :topology-value-read-count (:topology-value-read-count profile))))

(defn- run-restore-interruption!
  [backup-root t target-uri]
  (let [boundary (audit-candidate-boundary!)
        base (storage/file-storage backup-root)
        profile (leaf-profile base t)
        selected-key (last (:leaf-keys profile))
        selected-segment-id (segment-id-from-key selected-key)
        _ (ensure! (boolean (backup/retrieve base selected-key))
                   "Selected late-miss leaf is not readable"
                   {:storage-key selected-key})
        first-progress (atom {})
        copied-latch (CountDownLatch. 1)
        faulty (late-missing-retrieve-storage
                 base selected-key copied-latch first-progress)
        first-outcome
        (try
          {:returned
           @(backup/restore-db
              {:from-storage faulty :t t}
              {:to-uri target-uri}
              (restore-progress-fn first-progress copied-latch)
              backup-concurrency
              false)}
          (catch Throwable throwable
            {:threw throwable}))
        _ (await-progress-quiescence! first-progress)
        first-progress-result (into (sorted-map) @first-progress)
        injection-state @(:state faulty)
        first-failure (:threw first-outcome)
        first-failure-data
        (when first-failure (sanitized-throwable first-failure))]
    (ensure! (and first-failure
                  (not (contains? first-outcome :returned)))
             "Interrupted restore falsely returned success"
             {:returned (:returned first-outcome)})
    (ensure! (= 1 (:injection-count injection-state))
             "Late missing leaf was not injected exactly once"
             {:injection-count (:injection-count injection-state)})
    (ensure! (:copy-observed-before-miss? injection-state)
             "Late missing leaf timed out before any value was copied"
             {:injection-state injection-state})
    (ensure! (pos? (get first-progress-result :copied 0))
             "Interrupted restore did not copy values before failing"
             {:progress first-progress-result})
    (ensure! (some #{:restore/read-failed}
                   (:db-errors first-failure-data))
             "First restore failed for a reason other than the injected miss"
             {:failure first-failure-data})
    (let [retry-progress (atom {})
          retry-latch (CountDownLatch. 0)
          retry-result
          @(backup/restore-db
             {:from-storage base :t t}
             {:to-uri target-uri}
             (restore-progress-fn retry-progress retry-latch)
             backup-concurrency
             true)
          retry-progress-result (into (sorted-map) @retry-progress)]
      (ensure! (= :succeeded retry-result)
               "Clean incremental restore retry did not succeed"
               {:actual retry-result})
      (ensure! (pos? (get retry-progress-result :skipped 0))
               "Incremental retry did not reuse values from the failed attempt"
               {:progress retry-progress-result})
      (sorted-map
        :candidate-boundary boundary
        :first-attempt
        (sorted-map
          :failure first-failure-data
          :incremental? false
          :injection
          (sorted-map
            :copied-count-at-miss (:copied-count-at-miss injection-state)
            :copy-observed-before-miss?
            (:copy-observed-before-miss? injection-state)
            :injection-count (:injection-count injection-state))
          :progress first-progress-result
          :result :failed)
        :leaf
        (sorted-map
          :selection :lexicographically-last-topology-independent-leaf
          :segment-id selected-segment-id
          :storage-key selected-key)
        :mode :restore-interruption
        :retry
        (sorted-map
          :incremental? true
          :progress retry-progress-result
          :result retry-result)
        :t t
        :target-uri-sha256 (sha256-string target-uri)))))

(defn- expected-verification?
  [expectation verification]
  (let [missing? (seq (:missing-segment-ids verification))
        unreadable? (seq (:unreadable-segment-ids verification))]
    (case expectation
      "clean" (and (not missing?) (not unreadable?))
      "missing" (and missing? (not unreadable?))
      "unreadable" (and (not missing?) unreadable?))))

(defn- run-verify!
  [backup-root t expectation]
  (ensure! (contains? expectations expectation)
           "Unknown verification expectation"
           {:allowed (vec (sort expectations))})
  (let [boundary (audit-candidate-boundary!)
        backup-uri (str (storage/stage2-file-uri backup-root))
        captured-output (StringWriter.)
        verification
        (binding [*out* captured-output]
          (normalize-verification
            (backup/verify-backup
              {:backup-uri backup-uri :t t :read-all true})))]
    (ensure! (expected-verification? expectation verification)
             "Backup verification did not match the expected fault class"
             {:expectation (keyword expectation)
              :verification verification})
    (sorted-map
      :candidate-boundary boundary
      :expectation (keyword expectation)
      :mode :verify
      :verification verification)))

(defn- parse-and-run!
  [args]
  (let [[mode & mode-args] args]
    (case mode
      "root-store-failure"
      (do
        (ensure! (= 2 (count mode-args))
                 "Expected SOURCE_URI BACKUP_ROOT"
                 {:mode :root-store-failure
                  :argument-count (count mode-args)})
        (require-text! :source-uri (first mode-args))
        (require-backup-root! (second mode-args))
        (apply run-root-store-failure! mode-args))

      "leaf-faults"
      (do
        (ensure! (= 2 (count mode-args))
                 "Expected BACKUP_ROOT T"
                 {:mode :leaf-faults
                  :argument-count (count mode-args)})
        (require-backup-root! (first mode-args))
        (run-leaf-faults! (first mode-args) (parse-t (second mode-args))))

      "restore-interruption"
      (do
        (ensure! (= 3 (count mode-args))
                 "Expected BACKUP_ROOT T TARGET_URI"
                 {:mode :restore-interruption
                  :argument-count (count mode-args)})
        (require-backup-root! (first mode-args))
        (require-text! :target-uri (nth mode-args 2))
        (run-restore-interruption!
          (first mode-args) (parse-t (second mode-args)) (nth mode-args 2)))

      "verify"
      (do
        (ensure! (= 3 (count mode-args))
                 "Expected BACKUP_ROOT T EXPECTATION"
                 {:mode :verify
                  :argument-count (count mode-args)})
        (require-backup-root! (first mode-args))
        (run-verify!
          (first mode-args) (parse-t (second mode-args)) (nth mode-args 2)))

      (fail! "Unknown Stage 2 fault-probe mode"
             {:mode mode
              :allowed-modes
              [:leaf-faults :restore-interruption
               :root-store-failure :verify]}))))

(defn- shutdown!
  []
  (try
    (d/shutdown true)
    (catch Throwable _ nil))
  (shutdown-agents))

(defn -main
  [& args]
  (let [mode (first args)
        exit-code
        (try
          (println (str result-prefix (pr-str (parse-and-run! args))))
          (flush)
          0
          (catch Throwable throwable
            (binding [*out* *err*]
              (println
                (str error-prefix
                     (pr-str
                       (sorted-map
                         :error (sanitized-throwable throwable)
                         :mode mode))))
              (flush))
            1)
          (finally
            (shutdown!)))]
    (when-not (zero? exit-code)
      (System/exit exit-code))))

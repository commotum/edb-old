(ns stage2.backup-probe
  "Candidate-only Stage 2 backup probe.

  Usage:
    backup_probe.clj full SOURCE_URI BACKUP_ROOT
    backup_probe.clj incremental SOURCE_URI BACKUP_ROOT
    backup_probe.clj verify-only - BACKUP_ROOT

  The probe exercises recovered datomic.backup/backup-db directly with the
  test-only stage2-file storage adapter.  It must run on the canonical
  recovered artifact classpath and refuses peer, core2, transactor, or loose
  recovered-source classpath contamination."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.api :as d]
            [datomic.backup :as backup]
            [stage2.storage :as storage])
  (:import [java.io File StringWriter]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(def ^:private result-prefix "STAGE2-BACKUP-RESULT ")
(def ^:private error-prefix "STAGE2-BACKUP-ERROR ")
(def ^:private backup-concurrency 4)
(def ^:private modes #{"full" "incremental" "verify-only"})

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

(defn- sha256-bytes
  [^bytes bytes]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest bytes)
    (hex (.digest digest))))

(defn- sha256-string
  [value]
  (sha256-bytes (.getBytes (str value) StandardCharsets/UTF_8)))

(defn- segment-set-sha256
  [segment-ids]
  (let [digest (MessageDigest/getInstance "SHA-256")
        newline (byte-array [(byte 10)])]
    (doseq [segment-id (sort segment-ids)]
      (.update digest (.getBytes ^String segment-id StandardCharsets/UTF_8))
      (.update digest newline))
    (hex (.digest digest))))

(defn- classpath-entries
  []
  (str/split (System/getProperty "java.class.path")
             (re-pattern (java.util.regex.Pattern/quote File/pathSeparator))))

(defn- forbidden-classpath-entry?
  [entry]
  (let [name (.getName (io/file entry))]
    (or (str/includes? entry "*")
        (boolean (re-matches #"(?i)peer-[^/]*[.]jar" name))
        (boolean (re-matches #"(?i)core2-[^/]*[.]jar" name))
        (boolean (re-matches #"(?i)datomic-transactor[^/]*[.]jar" name)))))

(defn- audit-candidate-boundary!
  []
  (let [entries (classpath-entries)
        forbidden (vec (filter forbidden-classpath-entry? entries))
        backup-source (io/resource "datomic/backup.clj")
        fsbackup-source (io/resource "datomic/fsbackup.clj")
        fsbackup-aot (io/resource "datomic/fsbackup__init.class")]
    (ensure! (empty? forbidden)
             "Forbidden original Datomic implementation on candidate classpath"
             {:forbidden-classpath-entries forbidden})
    (ensure! backup-source
             "Recovered datomic.backup source is not visible"
             {})
    (ensure! (= "jar" (.getProtocol backup-source))
             "Recovered datomic.backup must load from the packaged candidate artifact"
             {:backup-source (str backup-source)})
    (ensure! (nil? fsbackup-source)
             "Original datomic.fsbackup source must not be visible to the candidate probe"
             {:fsbackup-source (str fsbackup-source)})
    (ensure! (nil? fsbackup-aot)
             "Original datomic.fsbackup AOT must not be visible to the candidate probe"
             {:fsbackup-aot (str fsbackup-aot)})
    {:classpath-entry-count (count entries)
     :backup-source (str backup-source)
     :forbidden-implementation-entries 0
     :fsbackup-visible? false}))

(defn- parse-args
  [args]
  (when-not (= 3 (count args))
    (fail! "Expected MODE SOURCE_URI BACKUP_ROOT"
           {:usage ["full SOURCE_URI BACKUP_ROOT"
                    "incremental SOURCE_URI BACKUP_ROOT"
                    "verify-only - BACKUP_ROOT"]
            :argument-count (count args)}))
  (let [[mode source-uri backup-root] args]
    (ensure! (contains? modes mode)
             "Unknown Stage 2 backup mode"
             {:mode mode
              :allowed-modes (vec (sort modes))})
    (ensure! (not (str/blank? backup-root))
             "BACKUP_ROOT must not be blank"
             {})
    (when-not (= mode "verify-only")
      (ensure! (not (str/blank? source-uri))
               "SOURCE_URI must not be blank for backup modes"
               {:mode mode}))
    {:mode mode
     :source-uri source-uri
     :backup-root backup-root}))

(defn- progress-counter
  []
  (atom {:copied 0
         :skipped 0}))

(defn- progress-fn
  [counter]
  (fn [event]
    (swap! counter update event (fnil inc 0))))

(defn- sorted-progress
  [progress]
  (into (sorted-map-by #(compare (str %1) (str %2))) progress))

(defn- normalize-description
  [description]
  {:db-id (:db-id description)
   :ts (vec (:ts description))})

(defn- normalize-verification
  [verification]
  {:t (:t verification)
   :total-segments (long (:total-segments verification))
   :missing-segments (vec (sort (map str (:missing-segments verification))))
   :unreadable-segments (vec (sort (map str (:unreadable-segments verification))))})

(defn- verify-latest!
  [backup-uri latest-t]
  ;; Recovered verify-backup prints periodic progress to *out*.  Keep stdout
  ;; machine-readable by capturing that chatter; diagnostics still surface in
  ;; the normalized verification map and failures.
  (let [captured-output (StringWriter.)
        verification (binding [*out* captured-output]
                       (backup/verify-backup
                         {:backup-uri backup-uri
                          :t latest-t
                          :read-all true}))
        normalized (normalize-verification verification)]
    (ensure! (= latest-t (:t normalized))
             "Verifier returned a different restore point"
             {:latest-t latest-t
              :verification-t (:t normalized)})
    (ensure! (empty? (:missing-segments normalized))
             "Backup has missing segments"
             normalized)
    (ensure! (empty? (:unreadable-segments normalized))
             "Backup has unreadable segments"
             normalized)
    normalized))

(defn- run-backup!
  [mode source-uri adapter progress]
  (if (= mode "verify-only")
    :not-run
    (let [incremental? (= mode "incremental")
          result @(backup/backup-db
                    source-uri
                    adapter
                    (progress-fn progress)
                    backup-concurrency
                    incremental?)]
      (ensure! (= :succeeded result)
               "Recovered backup-db did not succeed"
               {:mode mode
                :backup-result result})
      result)))

(defn- run-probe
  [args]
  (let [{:keys [mode source-uri backup-root]} (parse-args args)
        boundary (audit-candidate-boundary!)
        adapter (storage/file-storage backup-root)
        backup-uri (str (storage/stage2-file-uri (storage/storage-root adapter)))
        progress (progress-counter)
        backup-result (run-backup! mode source-uri adapter progress)
        description (normalize-description (backup/describe-backups adapter))
        latest-t (first (:ts description))]
    (ensure! latest-t
             "Backup storage has no restore points"
             {:mode mode
              :backup-root (str (storage/storage-root adapter))})
    (ensure! (:db-id description)
             "Backup storage is missing its database ownership claim"
             {:restore-points description})
    (let [verification (verify-latest! backup-uri latest-t)
          segment-ids (->> (backup/backup-seg-ids adapter latest-t)
                           (map str)
                           set)
          segment-count (count segment-ids)]
      (ensure! (pos? segment-count)
               "Backup restore point contains no reachable segments"
               {:latest-t latest-t})
      (ensure! (= segment-count (:total-segments verification))
               "Verifier and segment enumeration disagree"
               {:enumerated-segments segment-count
                :verified-segments (:total-segments verification)})
      {:mode (keyword mode)
       :backup-result backup-result
       :backup-concurrency backup-concurrency
       :backup-root (str (storage/storage-root adapter))
       :backup-uri backup-uri
       :source-uri-sha256 (when-not (= mode "verify-only")
                            (sha256-string source-uri))
       :progress (sorted-progress @progress)
       :restore-points description
       :latest-t latest-t
       :verification verification
       :segments {:count segment-count
                  :set-sha256 (segment-set-sha256 segment-ids)
                  :digest-encoding :sorted-utf8-lines}
       :candidate-boundary boundary})))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- sanitized-error
  [mode ^Throwable throwable]
  {:mode mode
   :exception-class (.getName (class throwable))
   :cause-classes (mapv #(.getName (class %)) (cause-chain throwable))
   :db-error (some #(some-> % ex-data :db/error) (cause-chain throwable))
   :probe-error (some #(some-> % ex-data ::error) (cause-chain throwable))})

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
          (println (str result-prefix (pr-str (run-probe args))))
          (flush)
          0
          (catch Throwable throwable
            (binding [*out* *err*]
              (println (str error-prefix (pr-str (sanitized-error mode throwable))))
              (flush))
            1)
          (finally
            (shutdown!)))]
    (when-not (zero? exit-code)
      (System/exit exit-code))))

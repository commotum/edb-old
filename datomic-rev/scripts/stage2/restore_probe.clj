(ns stage2.restore-probe
  "Candidate-only verified restore harness for disposable Stage 2 SQL targets."
  (:require [clojure.string :as str]
            [datomic.api :as d]
            [datomic.backup :as backup]
            [stage2.storage :as storage])
  (:import [java.io StringWriter]))

(set! *warn-on-reflection* true)

(def ^:private result-prefix "STAGE2-RESTORE-RESULT ")
(def ^:private error-prefix "STAGE2-RESTORE-ERROR ")

(defn- fail!
  [message data]
  (throw (ex-info message (assoc data ::error :restore-probe-failed))))

(defn- ensure!
  [pred message data]
  (when-not pred
    (fail! message data)))

(defn- normalize-verification
  [verification]
  {:t (:t verification)
   :total-segments (long (:total-segments verification))
   :missing-segments (vec (sort (map str (:missing-segments verification))))
   :unreadable-segments (vec (sort (map str (:unreadable-segments verification))))})

(defn- verify-backup!
  [backup-uri requested-t]
  (let [captured-output (StringWriter.)
        verification (binding [*out* captured-output]
                       (backup/verify-backup
                         {:backup-uri backup-uri
                          :t requested-t
                          :read-all true}))]
    (ensure! (map? verification)
             "Backup verifier did not return a map"
             {:verification-type (some-> verification class .getName)})
    (doseq [key [:t :total-segments :missing-segments :unreadable-segments]]
      (ensure! (contains? verification key)
               "Backup verifier omitted a required result"
               {:missing-key key}))
    (ensure! (= requested-t (:t verification))
             "Backup verifier returned a different restore point"
             {:requested-t requested-t
              :verification-t (:t verification)})
    (ensure! (and (number? (:total-segments verification))
                  (pos? (long (:total-segments verification))))
             "Backup restore point contains no segments"
             {:total-segments (:total-segments verification)})
    (ensure! (coll? (:missing-segments verification))
             "Backup verifier returned invalid missing-segments"
             {:value-type (some-> (:missing-segments verification) class .getName)})
    (ensure! (coll? (:unreadable-segments verification))
             "Backup verifier returned invalid unreadable-segments"
             {:value-type (some-> (:unreadable-segments verification) class .getName)})
    (let [normalized (normalize-verification verification)]
      (ensure! (empty? (:missing-segments normalized))
               "Backup has missing segments"
               normalized)
      (ensure! (empty? (:unreadable-segments normalized))
               "Backup has unreadable segments"
               normalized)
      normalized)))

(defn- normalized-progress
  [progress]
  (into (sorted-map) @progress))

(defn- parse-exact-boolean
  [value]
  (ensure! (contains? #{"true" "false"} value)
           "INCREMENTAL? must be exactly true or false"
           {})
  (= "true" value))

(defn- parse-args
  [args]
  (ensure! (= 4 (count args))
           "Usage: restore_probe.clj BACKUP_ROOT T TARGET_URI INCREMENTAL?"
           {:argument-count (count args)})
  (let [[backup-root t-text target-uri incremental-text] args]
    (ensure! (not (str/blank? backup-root))
             "BACKUP_ROOT must not be blank"
             {})
    (ensure! (not (str/blank? t-text))
             "T must not be blank"
             {})
    (ensure! (not (str/blank? target-uri))
             "TARGET_URI must not be blank"
             {})
    (let [t (Long/parseLong t-text)]
      (ensure! (not (neg? t))
               "T must not be negative"
               {:t t})
      {:backup-root backup-root
       :t t
       :target-uri target-uri
       :incremental? (parse-exact-boolean incremental-text)})))

(defn- run-restore!
  [{:keys [backup-root t target-uri incremental?]}]
  (let [adapter (storage/file-storage backup-root)
        backup-uri (str (storage/stage2-file-uri (storage/storage-root adapter)))
        verification (verify-backup! backup-uri t)
        progress (atom {})
        progress-fn #(swap! progress update % (fnil inc 0))
        restored @(backup/restore-db
                    {:from-storage adapter :t t}
                    {:to-uri target-uri}
                    progress-fn
                    4
                    incremental?)]
    (ensure! (= :succeeded restored)
             "Recovered restore-db did not succeed"
             {:actual restored})
    {:incremental incremental?
     :progress (normalized-progress progress)
     :result restored
     :t t
     :verification verification}))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- sanitized-error
  [^Throwable throwable]
  {:exception-class (.getName (class throwable))
   :cause-classes (mapv #(.getName (class %)) (cause-chain throwable))
   :db-error (some #(some-> % ex-data :db/error) (cause-chain throwable))
   :probe-error (some #(some-> % ex-data ::error) (cause-chain throwable))})

(defn- shutdown!
  []
  (try
    (d/shutdown false)
    (catch Throwable _ nil)
    (finally
      (try
        (shutdown-agents)
        (catch Throwable _ nil)))))

(defn -main
  [& args]
  (let [exit-code
        (try
          (println (str result-prefix (pr-str (run-restore! (parse-args args)))))
          (flush)
          0
          (catch Throwable throwable
            (binding [*out* *err*]
              (println (str error-prefix (pr-str (sanitized-error throwable))))
              (flush))
            1)
          (finally
            (shutdown!)))]
    (when-not (zero? exit-code)
      (System/exit exit-code))))

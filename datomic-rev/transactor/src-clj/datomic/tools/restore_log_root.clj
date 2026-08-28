(do
  (clojure.core/in-ns 'datomic.tools.restore-log-root)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.backup :as 'backup]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.tools.restore-log-root 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.restore-log-root))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.backup :as 'backup]
          ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (defn progress-tracker
    ([]
      (let [copied (atom 0) skipped (atom 0)]
        (fn fn__33041
          ([] {:copied (deref copied), :skipped (deref skipped)})
          ([x]
            (do
              (print
                (let [G__33042 x]
                  (case
                    G__33042
                    :skipped
                    (do (swap! skipped inc) "-")
                    :copied
                    (do (swap! copied inc) "."))))
              (flush)))))))
  (defn restore-log-root
    ([from_storage_uri next_t to_uri]
      (let [from_storage (backup/create-storage from_storage_uri)
            job (backup/create-restore-job from_storage next_t)
            _ (println "Restoring log root: " (:log-root-id job))
            backup_version (:backup/version job)
            map__33045 job
            map__33045 (if (seq? map__33045)
                         (if (next map__33045)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__33045))
                           (if (seq map__33045) (first map__33045) {}))
                         map__33045)
            log_root_node (get map__33045 :log-root-node)
            db_id (get map__33045 :db-id)
            lookup (get map__33045 :lookup)
            to_cluster (backup/create-restore-target to_uri {:db-id db_id} 1)
            progress (progress-tracker)
            restore (backup/create-value-restore
                      :from-storage
                      from_storage
                      :to-cluster
                      to_cluster
                      :backup-version
                      backup_version
                      :progress
                      progress
                      :ids->nodes
                      (backup/create-ids->nodes lookup)
                      :incremental?
                      false
                      :concurrency
                      (backup/backup-concurrency from_storage_uri))]
        (backup/restore-node restore log_root_node)
        (:log-root-id job))))
  (defn -main
    ([from_storage_uri next_t to_uri]
      (try
        (let [result (restore-log-root from_storage_uri next_t to_uri)]
          (println "\nLog Root ID: " result))
        (finally (shutdown-agents))))))
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
        (fn fn__31804
          ([] {:copied (deref copied), :skipped (deref skipped)})
          ([x]
            (do
              (print
                (let [G__31805 x]
                  (case
                    G__31805
                    :skipped
                    (do (swap! skipped inc) "-")
                    :copied
                    (do (swap! copied inc) "."))))
              (flush)))))))
  (reset-meta!
    #'progress-tracker
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'progress-tracker :ns *ns*))
  (def restore-log-root
   (fn restore_log_root
     ([from_storage_uri next_t to_uri]
       (let [from_storage (backup/create-storage from_storage_uri)
             job (backup/create-restore-job from_storage next_t)
             _ (println "Restoring log root: " (:log-root-id job))
             backup_version (:backup/version job)
             map__31808 job
             map__31808 (if (seq? map__31808)
                          (if (next map__31808)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31808))
                            (if (seq map__31808) (first map__31808) {}))
                          map__31808)
             log_root_node (get map__31808 :log-root-node)
             db_id (get map__31808 :db-id)
             lookup (get map__31808 :lookup)
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
         (:log-root-id job)))))
  (reset-meta!
    #'restore-log-root
    (assoc
      {:arglists (clojure.core/list ['from-storage-uri 'next-t 'to-uri]), :column (int 1)}
      :name
      'restore-log-root
      :ns
      *ns*))
  (def -main
   (fn _main
     ([from_storage_uri next_t to_uri]
       (try
         (let [result (restore-log-root from_storage_uri next_t to_uri)]
           (println "\nLog Root ID: " result))
         (finally (shutdown-agents))))))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['from-storage-uri 'next-t 'to-uri]), :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))
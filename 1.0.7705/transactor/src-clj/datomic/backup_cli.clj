(do
  (clojure.core/in-ns 'datomic.backup-cli)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['datomic.db :as 'db]
        ['datomic.backup :as 'backup]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.db-io :as 'db-io]
        ['datomic.require :as 'req]
        ['datomic.process-monitor :as 'process-monitor]
        ['datomic.uri :as 'uri])
      (clojure.core/import 'datomic.Database)))
  (when-not (.equals 'datomic.backup-cli 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.backup-cli))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['datomic.db :as 'db]
          ['datomic.backup :as 'backup]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.db-io :as 'db-io]
          ['datomic.require :as 'req]
          ['datomic.process-monitor :as 'process-monitor]
          ['datomic.uri :as 'uri])
        (clojure.core/import 'datomic.Database))))
  (defn minimize-object-cache-max!
    ([] (java.lang.System/setProperty "datomic.objectCacheMax" "32m") (config/reset!)))
  (reset-meta!
    #'minimize-object-cache-max!
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'minimize-object-cache-max!
      :ns
      *ns*))
  (defn status-message-loop
    ([job copied skipped]
      (while
        (not (realized? job))
        (println (str "Copied " (deref copied) " segments, skipped " (deref skipped) " segments."))
        (java.lang.Thread/sleep 5000))
      (println (str "Copied " (deref copied) " segments, skipped " (deref skipped) " segments."))
      (println (deref job))))
  (reset-meta!
    #'status-message-loop
    (assoc
      {:arglists (clojure.core/list ['job 'copied 'skipped]), :column (int 1)}
      :name
      'status-message-loop
      :ns
      *ns*))
  (def backup
   (fn backup
     ([p__22681]
       (let [map__22682 p__22681
             map__22682 (if (seq? map__22682)
                          (if (next map__22682)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__22682))
                            (if (seq map__22682) (first map__22682) {}))
                          map__22682)
             from_db_uri (get map__22682 :from-db-uri)
             to_backup_uri (get map__22682 :to-backup-uri)
             encryption (get map__22682 :encryption)]
         (when-not (or (nil? encryption) (= :sse encryption))
           (throw (java.lang.IllegalArgumentException. "only 'sse' supported for encryption")))
         (minimize-object-cache-max!)
         (process-monitor/start-metrics)
         (let [copied (atom 0) skipped (atom 0)]
           (status-message-loop
             (backup/backup
               from_db_uri
               to_backup_uri
               (= encryption :sse)
               (fn fn__22683
                 ([p1__22680#]
                   (swap!
                     (let [G__22684 p1__22680#] (case G__22684 :skipped skipped :copied copied))
                     inc)))
               true)
             copied
             skipped))))))
  (reset-meta!
    #'backup
    (assoc
      {:arglists (clojure.core/list [{:keys ['from-db-uri 'to-backup-uri 'encryption]}]),
       :column (int 1)}
      :name
      'backup
      :ns
      *ns*))
  (def restore
   (fn restore
     ([p__22689]
       (let [map__22690 p__22689
             map__22690 (if (seq? map__22690)
                          (if (next map__22690)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__22690))
                            (if (seq map__22690) (first map__22690) {}))
                          map__22690)
             from_backup_uri (get map__22690 :from-backup-uri)
             to_db_uri (get map__22690 :to-db-uri)
             t (get map__22690 :t)]
         (minimize-object-cache-max!)
         (process-monitor/start-metrics)
         (let [copied (atom 0)
               skipped (atom 0)
               inc! (fn inc_BANG_
                      ([p1__22688#]
                        (swap!
                          (let [G__22692 p1__22688#]
                            (case G__22692 :skipped skipped :copied copied))
                          inc)))
               t (or t (backup/require-latest-t from_backup_uri))]
           (status-message-loop
             (backup/restore from_backup_uri to_db_uri inc! t false)
             copied
             skipped)
           (let [db (:db (db-io/db-resources (db-io/storage-resources to_db_uri)))
                 basis (:basisT db)]
             (common/log-and-print
               {:event :restore,
                :db (:db-name (uri/parse to_db_uri)),
                :basis-t basis,
                :inst
                (:db/txInstant
                  (.entity
                    ^datomic.Database db
                    (long (db/t->tx (long ^java.lang.Number basis)))))})))))))
  (reset-meta!
    #'restore
    (assoc
      {:arglists (clojure.core/list [{:keys ['from-backup-uri 'to-db-uri 't]}]), :column (int 1)}
      :name
      'restore
      :ns
      *ns*))
  (def list-backups
   (fn list_backups
     ([p__22696]
       (let [map__22697 p__22696
             map__22697 (if (seq? map__22697)
                          (if (next map__22697)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__22697))
                            (if (seq map__22697) (first map__22697) {}))
                          map__22697)
             backup_uri (get map__22697 :backup-uri)]
         (:ts (backup/describe-backups backup_uri))))))
  (reset-meta!
    #'list-backups
    (assoc
      {:arglists (clojure.core/list [{:keys ['backup-uri]}]), :column (int 1)}
      :name
      'list-backups
      :ns
      *ns*))
  (def verify-backup
   (fn verify_backup
     ([p__22699]
       (let [map__22700 p__22699
             map__22700 (if (seq? map__22700)
                          (if (next map__22700)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__22700))
                            (if (seq map__22700) (first map__22700) {}))
                          map__22700)
             backup_uri (get map__22700 :backup-uri)
             read_all (get map__22700 :read-all)
             t (get map__22700 :t)]
         (minimize-object-cache-max!)
         (process-monitor/start-metrics)
         (let [results (backup/verify-backup {:backup-uri backup_uri, :t t, :read-all read_all})]
           (common/log-and-print (assoc results :event :verify-backup/complete))
           (when (or
                   (not-empty (:missing-segments results))
                   (not-empty (:unreadable-segments results)))
             (java.lang.System/exit (int 1))
             nil))))))
  (reset-meta!
    #'verify-backup
    (assoc
      {:arglists (clojure.core/list [{:keys ['backup-uri 'read-all 't]}]), :column (int 1)}
      :name
      'verify-backup
      :ns
      *ns*)))
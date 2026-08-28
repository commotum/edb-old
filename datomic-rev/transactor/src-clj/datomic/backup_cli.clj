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
  (defn status-message-loop
    ([job copied skipped]
      (while
        (not (realized? job))
        (println (str "Copied " (deref copied) " segments, skipped " (deref skipped) " segments."))
        (java.lang.Thread/sleep 5000))
      (println (str "Copied " (deref copied) " segments, skipped " (deref skipped) " segments."))
      (println (deref job))))
  (defn backup
    ([p__31979]
      (let [map__31980 p__31979
            map__31980 (if (seq? map__31980)
                         (if (next map__31980)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31980))
                           (if (seq map__31980) (first map__31980) {}))
                         map__31980)
            from_db_uri (get map__31980 :from-db-uri)
            to_backup_uri (get map__31980 :to-backup-uri)
            encryption (get map__31980 :encryption)]
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
              (fn fn__31981
                ([p1__31978#]
                  (swap!
                    (let [G__31982 p1__31978#] (case G__31982 :skipped skipped :copied copied))
                    inc)))
              true)
            copied
            skipped)))))
  (defn restore
    ([p__31987]
      (let [map__31988 p__31987
            map__31988 (if (seq? map__31988)
                         (if (next map__31988)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31988))
                           (if (seq map__31988) (first map__31988) {}))
                         map__31988)
            from_backup_uri (get map__31988 :from-backup-uri)
            to_db_uri (get map__31988 :to-db-uri)
            t (get map__31988 :t)]
        (minimize-object-cache-max!)
        (process-monitor/start-metrics)
        (let [copied (atom 0)
              skipped (atom 0)
              inc! (fn inc_BANG_
                     ([p1__31986#]
                       (swap!
                         (let [G__31990 p1__31986#]
                           (case G__31990 :skipped skipped :copied copied))
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
                   (long (db/t->tx (long ^java.lang.Number basis)))))}))))))
  (defn list-backups
    ([p__31994]
      (let [map__31995 p__31994
            map__31995 (if (seq? map__31995)
                         (if (next map__31995)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31995))
                           (if (seq map__31995) (first map__31995) {}))
                         map__31995)
            backup_uri (get map__31995 :backup-uri)]
        (:ts (backup/describe-backups backup_uri)))))
  (defn verify-backup
    ([p__31997]
      (let [map__31998 p__31997
            map__31998 (if (seq? map__31998)
                         (if (next map__31998)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31998))
                           (if (seq map__31998) (first map__31998) {}))
                         map__31998)
            backup_uri (get map__31998 :backup-uri)
            read_all (get map__31998 :read-all)
            t (get map__31998 :t)]
        (minimize-object-cache-max!)
        (process-monitor/start-metrics)
        (let [results (backup/verify-backup {:backup-uri backup_uri, :t t, :read-all read_all})]
          (common/log-and-print (assoc results :event :verify-backup/complete))
          (when (or
                  (not-empty (:missing-segments results))
                  (not-empty (:unreadable-segments results)))
            (java.lang.System/exit (int 1))
            nil))))))
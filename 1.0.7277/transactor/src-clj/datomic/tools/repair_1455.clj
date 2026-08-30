(do
  (clojure.core/in-ns 'datomic.tools.repair-1455)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.backup :as 'backup]
        ['datomic.cluster :as 'cluster]
        ['datomic.db :as 'db]
        ['datomic.log :as 'log]
        ['datomic.slf4j :as 'logger]
        ['datomic.tools :as 'tools]
        ['datomic.tools.log-tools :as 'lt])))
  (when-not (.equals 'datomic.tools.repair-1455 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.repair-1455))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.backup :as 'backup]
          ['datomic.cluster :as 'cluster]
          ['datomic.db :as 'db]
          ['datomic.log :as 'log]
          ['datomic.slf4j :as 'logger]
          ['datomic.tools :as 'tools]
          ['datomic.tools.log-tools :as 'lt]))))
  (set! *warn-on-reflection* true)
  (def repair-1455
   (fn repair_1455
     ([uri root_id]
       (let [cr (tools/connection-resources uri)
             cs (:cluster cr)
             db_log (log/find-log cs (:olookup cr))
             split_t (first (lt/t-range db_log))]
         (if (< 1000 split_t)
           (let [_ (println "Integrating log up to " split_t)
                 trunc_report (lt/truncate-log cr root_id split_t)
                 _ (pp/pprint trunc_report)
                 trunc_root_id (:root-id (:truncated trunc_report))
                 transform (fn transform
                             ([p1__33111#]
                               (cluster/uuid->val-key
                                 (lt/merge-roots
                                   cr
                                   trunc_root_id
                                   (cluster/val-key->uuid p1__33111#)))))]
             (lt/race-to-transform-root cs transform 10))
           (println "Nothing to do!"))))))
  (reset-meta!
    #'repair-1455
    (assoc
      {:arglists (clojure.core/list ['uri 'root-id]), :column (int 1)}
      :name
      'repair-1455
      :ns
      *ns*))
  (def -main
   (fn _main
     ([uri root_id] (try (pp/pprint (repair-1455 uri root_id)) (finally (shutdown-agents))))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri 'root-id]), :column (int 1)} :name '-main :ns *ns*)))
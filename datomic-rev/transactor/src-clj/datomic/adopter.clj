(do
  (clojure.core/in-ns 'datomic.adopter)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['datomic.db :as 'db])))
  (when-not (.equals 'datomic.adopter 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.adopter))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['datomic.db :as 'db]))))
  (def adopt-index
   (fn adopt_index
     ([db_ref adopt_db]
       (let [initial_db (deref db_ref)]
         (when-not (< (:indexBasisT adopt_db) (:indexBasisT initial_db))
           (let [msec (java.lang.System/currentTimeMillis)
                 txes (db/memlog-txes-since initial_db (:indexBasisT adopt_db))]
             (loop [db initial_db adopt_db (db/accept-txes adopt_db txes) n 1]
               (if (compare-and-set! db_ref db adopt_db)
                 {:basis-db db,
                  :adopted-db adopt_db,
                  :msec (long (- (java.lang.System/currentTimeMillis) msec)),
                  :iterations (long n)}
                 (let [db (deref db_ref) txes (db/memlog-txes-since db (:basisT adopt_db))]
                   (recur db (db/accept-txes adopt_db txes) (inc n)))))))))))
  (reset-meta!
    #'adopt-index
    (assoc
      {:arglists (clojure.core/list ['db-ref 'adopt-db]), :column (int 1)}
      :name
      'adopt-index
      :ns
      *ns*)))
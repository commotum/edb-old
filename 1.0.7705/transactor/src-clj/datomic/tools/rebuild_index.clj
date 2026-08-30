(do
  (clojure.core/in-ns 'datomic.tools.rebuild-index)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.api :as 'd]
        ['datomic.db :as 'db]
        ['datomic.config :as 'config]
        ['datomic.garbage :as 'garbage]
        ['datomic.index :as 'index]
        ['datomic.memory-size :as 'size]
        ['datomic.slf4j :as 'logger]
        ['datomic.tools :as 'tools])))
  (when-not (.equals 'datomic.tools.rebuild-index 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.rebuild-index))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.api :as 'd]
          ['datomic.db :as 'db]
          ['datomic.config :as 'config]
          ['datomic.garbage :as 'garbage]
          ['datomic.index :as 'index]
          ['datomic.memory-size :as 'size]
          ['datomic.slf4j :as 'logger]
          ['datomic.tools :as 'tools]))))
  (set! *warn-on-reflection* true)
  (defn memory-index-max
    ([]
      (quot
        (- (.maxMemory (java.lang.Runtime/getRuntime)) (config/property "datomic.objectCacheMax"))
        2)))
  (reset-meta!
    #'memory-index-max
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'memory-index-max :ns *ns*))
  (defn do-merge
    ([p__28277 db]
      (let [map__28278 p__28277
            map__28278 (if (seq? map__28278)
                         (if (next map__28278)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__28278))
                           (if (seq map__28278) (first map__28278) {}))
                         map__28278)
            cluster (get map__28278 :cluster)
            olookup (get map__28278 :olookup)
            db (db/prepare-for-indexing db)
            vec__28279 (index/merge-db*
                         cluster
                         olookup
                         db
                         (d/next-t db)
                         (:index-root-id db)
                         #:datomic.tools.rebuild-index{:rebuild true}
                         false
                         false)
            index_id (nth vec__28279 (int 0) nil)
            _ (nth vec__28279 (int 1) nil)
            garbage (nth vec__28279 (int 2) nil)
            after_db (db/complete-indexing db (index/load-index olookup index_id))]
        (prn
          {:from-root-id (:index-root-id db),
           :to-root-id (:index-root-id after_db),
           :from-basis-t (:indexBasisT db),
           :to-basis-t (:indexBasisT after_db),
           :garbage (java.lang.Integer/valueOf (int (count garbage)))})
        [after_db garbage])))
  (reset-meta!
    #'do-merge
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 'db]), :column (int 1)}
      :name
      'do-merge
      :ns
      *ns*))
  (defn txes-after-basis
    ([cr db] (eduction (map :data) (tools/tx-range-from-log cr (d/next-t db) nil))))
  (reset-meta!
    #'txes-after-basis
    (assoc
      {:arglists (clojure.core/list ['cr 'db]), :column (int 1)}
      :name
      'txes-after-basis
      :ns
      *ns*))
  (defn rebuild-index
    ([p__28284 index_root_id mem_index_max]
      (let [map__28285 p__28284
            map__28285 (if (seq? map__28285)
                         (if (next map__28285)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__28285))
                           (if (seq map__28285) (first map__28285) {}))
                         map__28285)
            cr map__28285
            cluster (get map__28285 :cluster)
            olookup (get map__28285 :olookup)
            resolved_conf (get map__28285 :resolved-conf)
            db (db/db (:db-id resolved_conf) (index/load-index olookup index_root_id))
            size 0
            garbage []
            G__28289 (txes-after-basis cr db)
            vec__28290 G__28289
            seq__28291 (seq vec__28290)
            first__28292 (first seq__28291)
            seq__28291 (next seq__28291)
            tx first__28292
            more seq__28291]
        (loop [db db size size garbage garbage G__28289 G__28289]
          (let [db db
                size size
                garbage garbage
                vec__28293 G__28289
                seq__28294 (seq vec__28293)
                first__28295 (first seq__28294)
                seq__28294 (next seq__28294)
                tx first__28295
                more seq__28294]
            (if (or tx (not (zero? size)))
              (if (and (>= mem_index_max size) tx)
                (let [db (.acceptDataCheck ^datomic.db.IDbImpl db tx false)]
                  (recur db (+ size (size/memory-size tx)) garbage more))
                (let [vec__28296 (do-merge cr db)
                      db (nth vec__28296 (int 0) nil)
                      new_garbage (nth vec__28296 (int 1) nil)]
                  (recur db 0 (into garbage new_garbage) (txes-after-basis cr db))))
              [db garbage]))))))
  (reset-meta!
    #'rebuild-index
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['cluster 'olookup 'resolved-conf], :as 'cr} 'index-root-id 'mem-index-max]),
       :column (int 1)}
      :name
      'rebuild-index
      :ns
      *ns*))
  (defn -main*
    ([uri mem_index_max]
      (let [map__28302 (tools/connection-resources uri)
            map__28302 (if (seq? map__28302)
                         (if (next map__28302)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__28302))
                           (if (seq map__28302) (first map__28302) {}))
                         map__28302)
            cr map__28302
            cluster (get map__28302 :cluster)
            olookup (get map__28302 :olookup)
            index_root_id (index/init-index* cluster)
            vec__28303 (rebuild-index cr index_root_id mem_index_max)
            db (nth vec__28303 (int 0) nil)
            garbage (nth vec__28303 (int 1) nil)]
        (tools/replace-index cluster (tools/uuid->val-key (:index-root-id db)))
        (tools/touch-heartbeat uri)
        (garbage/mark-garbage cluster olookup garbage)
        (garbage/flush-garbage cluster olookup)
        db)))
  (reset-meta!
    #'-main*
    (assoc
      {:arglists (clojure.core/list ['uri 'mem-index-max]), :column (int 1)}
      :name
      '-main*
      :ns
      *ns*))
  (defn -main
    ([uri]
      (try
        (do (-main* uri (memory-index-max)) (println "Index rebuild complete."))
        (finally (d/shutdown true)))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main :ns *ns*)))
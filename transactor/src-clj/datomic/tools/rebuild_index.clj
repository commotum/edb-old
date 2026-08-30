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
  (def do-merge
   (fn do_merge
     ([p__33121 db]
       (let [map__33122 p__33121
             map__33122 (if (seq? map__33122)
                          (if (next map__33122)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__33122))
                            (if (seq map__33122) (first map__33122) {}))
                          map__33122)
             cluster (get map__33122 :cluster)
             olookup (get map__33122 :olookup)
             db (db/prepare-for-indexing db)
             vec__33123 (index/merge-db*
                          cluster
                          olookup
                          db
                          (d/next-t db)
                          (:index-root-id db)
                          #:datomic.tools.rebuild-index{:rebuild true}
                          false
                          false)
             index_id (nth vec__33123 (int 0) nil)
             _ (nth vec__33123 (int 1) nil)
             garbage (nth vec__33123 (int 2) nil)
             after_db (db/complete-indexing db (index/load-index olookup index_id))]
         (prn
           {:from-root-id (:index-root-id db),
            :to-root-id (:index-root-id after_db),
            :from-basis-t (:indexBasisT db),
            :to-basis-t (:indexBasisT after_db),
            :garbage (java.lang.Integer/valueOf (int (count garbage)))})
         [after_db garbage]))))
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
  (def rebuild-index
   (fn rebuild_index
     ([p__33128 index_root_id mem_index_max]
       (let [map__33129 p__33128
             map__33129 (if (seq? map__33129)
                          (if (next map__33129)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__33129))
                            (if (seq map__33129) (first map__33129) {}))
                          map__33129)
             cr map__33129
             cluster (get map__33129 :cluster)
             olookup (get map__33129 :olookup)
             resolved_conf (get map__33129 :resolved-conf)
             db (db/db (:db-id resolved_conf) (index/load-index olookup index_root_id))
             size 0
             garbage []
             G__33133 (txes-after-basis cr db)
             vec__33134 G__33133
             seq__33135 (seq vec__33134)
             first__33136 (first seq__33135)
             seq__33135 (next seq__33135)
             tx first__33136
             more seq__33135]
         (loop [db db size size garbage garbage G__33133 G__33133]
           (let [db db
                 size size
                 garbage garbage
                 vec__33137 G__33133
                 seq__33138 (seq vec__33137)
                 first__33139 (first seq__33138)
                 seq__33138 (next seq__33138)
                 tx first__33139
                 more seq__33138]
             (if (or tx (not (zero? size)))
               (if (and (>= mem_index_max size) tx)
                 (let [db (.acceptDataCheck ^datomic.db.IDbImpl db tx false)]
                   (recur db (+ size (long (size/memory-size tx))) garbage more))
                 (let [vec__33140 (do-merge cr db)
                       db (nth vec__33140 (int 0) nil)
                       new_garbage (nth vec__33140 (int 1) nil)]
                   (recur db 0 (into garbage new_garbage) (txes-after-basis cr db))))
               [db garbage])))))))
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
  (def -main*
   (fn _main_STAR_
     ([uri mem_index_max]
       (let [map__33146 (tools/connection-resources uri)
             map__33146 (if (seq? map__33146)
                          (if (next map__33146)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__33146))
                            (if (seq map__33146) (first map__33146) {}))
                          map__33146)
             cr map__33146
             cluster (get map__33146 :cluster)
             olookup (get map__33146 :olookup)
             index_root_id (index/init-index* cluster)
             vec__33147 (rebuild-index cr index_root_id mem_index_max)
             db (nth vec__33147 (int 0) nil)
             garbage (nth vec__33147 (int 1) nil)]
         (tools/replace-index cluster (tools/uuid->val-key (:index-root-id db)))
         (tools/touch-heartbeat uri)
         (garbage/mark-garbage cluster olookup garbage)
         (garbage/flush-garbage cluster olookup)
         db))))
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
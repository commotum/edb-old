(do
  (clojure.core/in-ns 'datomic.stats)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.clusterfs :as 'clusterfs]
        ['datomic.memory-size :as 'size]
        ['datomic.iter :as 'iter]
        ['datomic.math :as 'math]
        ['datomic.core2.algo.lazy :as 'algo.lazy])
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.btset.IDataSet)
      (clojure.core/import 'java.util.Comparator)))
  (when-not (.equals 'datomic.stats 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.stats))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.clusterfs :as 'clusterfs]
          ['datomic.memory-size :as 'size]
          ['datomic.iter :as 'iter]
          ['datomic.math :as 'math]
          ['datomic.core2.algo.lazy :as 'algo.lazy])
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.btset.IDataSet)
        (clojure.core/import 'java.util.Comparator))))
  (set! *warn-on-reflection* true)
  (defn sparse-v-count
    ([des]
      (reduce
        (fn fn__13967 ([n de] (let [v (:v (:key de))] (if (or (nil? v) (= 0 v)) (inc n) n))))
        0
        des)))
  (reset-meta!
    #'sparse-v-count
    (assoc
      {:private true, :arglists (clojure.core/list ['des]), :column (int 1)}
      :name
      'sparse-v-count
      :ns
      *ns*))
  (def key-summary
   (fn key_summary
     ([des a_freqs?]
       (when (seq des)
         (let [sizes (map (comp size/memory-size :key) des) ms (math/mean-and-stddev sizes)]
           (merge
             {:key-size-min (apply min sizes),
              :key-size-max (apply max sizes),
              :key-size-mean (long (:mean ms)),
              :key-size-stddev (long (:stddev ms)),
              :sparse-vs (sparse-v-count des)}
             (when a_freqs? {:a-freqs (frequencies (map (comp :a :key) des))})))))
     ([des] (key-summary des false))))
  (reset-meta!
    #'key-summary
    (assoc
      {:arglists (clojure.core/list ['des] ['des 'a-freqs?]), :column (int 1)}
      :name
      'key-summary
      :ns
      *ns*))
  (def index-summary
   (fn index_summary
     ([db index idx partfn]
       (reduce
         (fn fn__13972
           ([acc entry]
             (let [temp__5802__auto__ (some-> entry (:key) (^clojure.lang.IFn partfn))]
               (if temp__5802__auto__
                 (let [k temp__5802__auto__
                       map__13974 (get acc k {:data-count 0, :seg-count 0})
                       map__13974 (if (seq? map__13974)
                                    (if (next map__13974)
                                      (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                        (to-array map__13974))
                                      (if (seq map__13974) (first map__13974) {}))
                                    map__13974)
                       m map__13974
                       data_count (get map__13974 :data-count)
                       seg_count (get map__13974 :seg-count)
                       m (assoc
                           m
                           :seg-count
                           (inc seg_count)
                           :data-count
                           (+ data_count (:count entry)))]
                   (assoc acc k m))
                 acc))))
         {}
         (^clojure.lang.IFn idx index)))))
  (reset-meta!
    #'index-summary
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index 'idx 'partfn]),
       :column (int 1)}
      :name
      'index-summary
      :ns
      *ns*))
  (defn avet
    ([db index]
      (index-summary
        db
        index
        :avet
        (fn fn__13979
          ([p1__13978#]
            (.ident
              db
              (db/resolve-kw
                db
                (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__13978#))))))))))
  (reset-meta!
    #'avet
    (assoc {:arglists (clojure.core/list ['db 'index]), :column (int 1)} :name 'avet :ns *ns*))
  (defn aevt
    ([db index]
      (index-summary
        db
        index
        :aevt
        (fn fn__13983
          ([p1__13982#]
            (.ident
              db
              (db/resolve-kw
                db
                (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__13982#))))))))))
  (reset-meta!
    #'aevt
    (assoc {:arglists (clojure.core/list ['db 'index]), :column (int 1)} :name 'aevt :ns *ns*))
  (defn eavt
    ([db index]
      (index-summary
        db
        index
        :eavt
        (fn fn__13987
          ([p1__13986#]
            (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__13986#))]
              (or (.ident db (long part)) (long part))))))))
  (reset-meta!
    #'eavt
    (assoc {:arglists (clojure.core/list ['db 'index]), :column (int 1)} :name 'eavt :ns *ns*))
  (defn raet
    ([db index]
      (index-summary
        db
        index
        :raet
        (fn fn__13992
          ([p1__13991#]
            (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__13991#))]
              (or (.ident db (long part)) (long part))))))))
  (reset-meta!
    #'raet
    (assoc {:arglists (clojure.core/list ['db 'index]), :column (int 1)} :name 'raet :ns *ns*))
  (defn total ([m] (apply + (map :data-count (vals m)))))
  (reset-meta!
    #'total
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'total :ns *ns*))
  (def datom-counts
   (fn datom_counts
     ([summary_fn db]
       (let [index (total (^clojure.lang.IFn summary_fn db (:index db)))
             mid (total (^clojure.lang.IFn summary_fn db (:mid-index db)))
             hist (total (^clojure.lang.IFn summary_fn db (:history db)))]
         {:index-datoms index,
          :mid-index-datoms mid,
          :index+mid-datoms (+ index mid),
          :history-datoms hist}))))
  (reset-meta!
    #'datom-counts
    (assoc
      {:arglists (clojure.core/list ['summary-fn 'db]), :column (int 1)}
      :name
      'datom-counts
      :ns
      *ns*))
  (defn fulltext
    ([db index]
      (let [olookup (:olookup (:fulltext index))
            attrmap (:attrmap (:root (:fulltext index)))
            aevt_summary (aevt db index)
            result (reduce
                     (fn fn__13999
                       ([m p__13998]
                         (let [vec__14000 p__13998
                               k (nth vec__14000 (int 0) nil)
                               v (nth vec__14000 (int 1) nil)
                               attr (db/resolve-kw db k)
                               detail (assoc
                                        (clusterfs/describe (get olookup v))
                                        :data-count
                                        (or
                                          (:data-count (^clojure.lang.IFn attr aevt_summary))
                                          0))]
                           (assoc m attr detail))))
                     {}
                     attrmap)]
        result)))
  (reset-meta!
    #'fulltext
    (assoc {:arglists (clojure.core/list ['db 'index]), :column (int 1)} :name 'fulltext :ns *ns*))
  (def storage-tiers #{:mid-index :index :history})
  (reset-meta! #'storage-tiers (assoc {:column (int 1)} :name 'storage-tiers :ns *ns*))
  (def memory-tiers #{:indexing :memidx})
  (reset-meta! #'memory-tiers (assoc {:column (int 1)} :name 'memory-tiers :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.stats" "tiers") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.stats" "tiers") (into storage-tiers memory-tiers))
  (def index-attr-stats
   (fn index_attr_stats
     ([db tier]
       (cond
         (storage-tiers tier) (algo.lazy/fred
                                (fn fn__14006
                                  ([acc entries]
                                    (let [temp__5802__auto__ (.ident
                                                               ^datomic.Database db
                                                               (:a (:key (first entries))))]
                                      (if temp__5802__auto__
                                        (let [k temp__5802__auto__]
                                          (assoc acc k {:count (apply + (map :count entries))}))
                                        acc))))
                                {}
                                (algo.lazy/fully-partition-by
                                  (fn fn__14010
                                    ([p__14009]
                                      (let [map__14011 p__14009
                                            map__14011 (if (seq? map__14011)
                                                         (if (next map__14011)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__14011))
                                                           (if
                                                             (seq map__14011)
                                                             (first map__14011)
                                                             {}))
                                                         map__14011)
                                            key (get map__14011 :key)]
                                        (:a key))))
                                  (:aevt (^clojure.lang.IFn tier db))))
         (memory-tiers tier) (do
                               (algo.lazy/fred
                                 (fn fn__14013
                                   ([acc datoms]
                                     (let [temp__5802__auto__ (.ident
                                                                ^datomic.Database db
                                                                (:a (first datoms)))]
                                       (if temp__5802__auto__
                                         (let [k temp__5802__auto__]
                                           (assoc
                                             acc
                                             k
                                             {:count
                                              (java.lang.Integer/valueOf (int (count datoms)))}))
                                         acc))))
                                 {}
                                 (algo.lazy/fully-partition-by
                                   (fn fn__14016 ([datom] (:a datom)))
                                   (:aevt (^clojure.lang.IFn tier db)))))))))
  (reset-meta!
    #'index-attr-stats
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'tier]), :column (int 1)}
      :name
      'index-attr-stats
      :ns
      *ns*))
  (defn db-attr-stats
    ([db]
      (apply
        merge-with
        (partial merge-with +)
        (map (fn fn__14020 ([p1__14019#] (index-attr-stats db p1__14019#))) tiers))))
  (reset-meta!
    #'db-attr-stats
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'db-attr-stats :ns *ns*))
  (defn db-stats
    ([db]
      (let [attr_stats (db-attr-stats db)]
        {:datoms (transduce (map :count) + (vals attr_stats)), :attrs attr_stats})))
  (reset-meta!
    #'db-stats
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'db-stats :ns *ns*))
  (def index-attr-splits
   (fn index_attr_splits
     ([db tier attr]
       (let [a (.entid ^datomic.Database db attr)]
         (cond
           (memory-tiers tier) (let [temp__5804__auto__ (:aevt (^clojure.lang.IFn tier db))]
                                 (when temp__5804__auto__
                                   (let [idx temp__5804__auto__
                                         split_n 1000
                                         diter (iter/take-while
                                                 (fn fn__14030
                                                   ([p1__14024#] (= a (:a p1__14024#))))
                                                 (.seek
                                                   ^datomic.btset.IDataSet idx
                                                   (db/datum db :a a)))
                                         vec__14027 (iter/reduce
                                                      (fn fn__14033
                                                        ([p__14032 datom]
                                                          (let [vec__14034 p__14032
                                                                ret (nth vec__14034 (int 0) nil)
                                                                nexte (nth vec__14034 (int 1) nil)
                                                                c (nth vec__14034 (int 2) nil)]
                                                            (if
                                                              (= (long split_n) c)
                                                              [(conj ret [nexte (long split_n)])
                                                               (:e datom)
                                                               1]
                                                              [ret nexte (inc c)]))))
                                                      [[] (:e (iter/iget diter)) 0]
                                                      diter)
                                         ret (nth vec__14027 (int 0) nil)
                                         nexte (nth vec__14027 (int 1) nil)
                                         c (nth vec__14027 (int 2) nil)]
                                     (cond->
                                       ret
                                       (clojure.lang.Numbers/isPos c)
                                       (conj [nexte c])))))
           (storage-tiers tier) (do
                                  (let [temp__5804__auto__ (seq
                                                             (:aevt (^clojure.lang.IFn tier db)))]
                                    (when temp__5804__auto__
                                      (let [dirs temp__5804__auto__]
                                        (into
                                          []
                                          (comp
                                            (drop-while
                                              (fn fn__14039
                                                ([p1__14025#] (< (:a (:key p1__14025#)) a))))
                                            (take-while
                                              (fn fn__14041
                                                ([p1__14026#] (= a (:a (:key p1__14026#))))))
                                            (map
                                              (fn fn__14044
                                                ([p__14043]
                                                  (let [map__14045 p__14043
                                                        map__14045 (if
                                                                     (seq? map__14045)
                                                                     (if
                                                                       (next map__14045)
                                                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                         (to-array map__14045))
                                                                       (if
                                                                         (seq map__14045)
                                                                         (first map__14045)
                                                                         {}))
                                                                     map__14045)
                                                        key (get map__14045 :key)
                                                        count (get map__14045 :count)]
                                                    [(:e key) count])))))
                                          dirs))))))))))
  (reset-meta!
    #'index-attr-splits
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'tier 'attr]),
       :column (int 1)}
      :name
      'index-attr-splits
      :ns
      *ns*))
  (def merge-data
   (fn merge_data
     ([cmp ds1 ds2]
       (let [ds1 (seq ds1) ds2 (seq ds2)]
         (if (and ds1 ds2)
           (lazy-seq
             (let [d1 (first ds1) d2 (first ds2) c (.compare ^java.util.Comparator cmp d1 d2)]
               (cond
                 (< c 0) (cons d1 (merge-data cmp (next ds1) ds2))
                 (> c 0) (cons d2 (merge-data cmp ds1 (next ds2)))
                 :else (do (cons d1 (cons d2 (merge-data cmp (next ds1) (next ds2))))))))
           (or ds1 ds2))))))
  (reset-meta!
    #'merge-data
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cmp {:tag 'Comparator}) 'ds1 'ds2]),
       :column (int 1)}
      :name
      'merge-data
      :ns
      *ns*))
  (defn merge-splits
    ([splits]
      (reduce
        (fn fn__14057
          ([p1__14055# p2__14056#]
            (merge-data
              (reify
                java.util.Comparator
                (^int compare [this a b] (clojure.lang.Util/compare a b)))
              p1__14055#
              p2__14056#)))
        splits)))
  (reset-meta!
    #'merge-splits
    (assoc
      {:private true, :arglists (clojure.core/list ['splits]), :column (int 1)}
      :name
      'merge-splits
      :ns
      *ns*))
  (defn db-attr-splits
    ([db attr]
      (into
        []
        (map
          (fn fn__14064
            ([p__14063]
              (let [vec__14065 p__14063
                    vec__14068 (nth vec__14065 (int 0) nil)
                    e1 (nth vec__14068 (int 0) nil)
                    ct (nth vec__14068 (int 1) nil)
                    vec__14071 (nth vec__14065 (int 1) nil)
                    e2 (nth vec__14071 (int 0) nil)]
                [e1 e2 ct]))))
        (partition-all
          2
          1
          (merge-splits
            (map (fn fn__14075 ([p1__14062#] (index-attr-splits db p1__14062# attr))) tiers))))))
  (reset-meta!
    #'db-attr-splits
    (assoc
      {:arglists (clojure.core/list ['db 'attr]), :column (int 1)}
      :name
      'db-attr-splits
      :ns
      *ns*))
  (defn attr-stats-from-splits
    ([db]
      (into
        {}
        (map
          (fn fn__14079
            ([attr]
              [attr
               {:count
                (apply
                  +
                  (map
                    (fn fn__14080 ([p1__14078#] (nth p1__14078# (int 2))))
                    (db-attr-splits db attr)))}])))
        (keys (db-attr-stats db)))))
  (reset-meta!
    #'attr-stats-from-splits
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'attr-stats-from-splits
      :ns
      *ns*))
  (def sizes
   (fn sizes
     ([db & p__14084]
       (let [map__14085 p__14084
             map__14085 (if (seq? map__14085)
                          (if (next map__14085)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14085))
                            (if (seq map__14085) (first map__14085) {}))
                          map__14085)
             with_key_summary (get map__14085 :with-key-summary)
             iter__6373__auto__ (fn iter__14086
                                  ([s__14087]
                                    (lazy-seq
                                      (loop [s__14087 s__14087]
                                        (let [temp__5804__auto__ (seq s__14087)]
                                          (when temp__5804__auto__
                                            (let [xs__6360__auto__ temp__5804__auto__
                                                  index (first xs__6360__auto__)
                                                  iterys__6369__auto__ (fn 
                                                                         iter__14088
                                                                         ([s__14089]
                                                                           (lazy-seq
                                                                             (let 
                                                                               [s__14089 s__14089
                                                                                temp__5804__auto__
                                                                                (seq s__14089)]
                                                                               (when
                                                                                 temp__5804__auto__
                                                                                 (let 
                                                                                   [s__14089
                                                                                    temp__5804__auto__]
                                                                                   (if
                                                                                     (chunked-seq?
                                                                                       s__14089)
                                                                                     (let 
                                                                                       [c__6371__auto__
                                                                                        (chunk-first
                                                                                          s__14089)
                                                                                        size__6372__auto__
                                                                                        (int
                                                                                          (count
                                                                                            c__6371__auto__))
                                                                                        b__14091
                                                                                        (chunk-buffer
                                                                                          (java.lang.Integer/valueOf
                                                                                            (int
                                                                                              size__6372__auto__)))]
                                                                                       (if
                                                                                         (loop 
                                                                                           [i__14090
                                                                                            (int
                                                                                              0)]
                                                                                           (if
                                                                                             (<
                                                                                               i__14090
                                                                                               size__6372__auto__)
                                                                                             (let 
                                                                                               [tier
                                                                                                (.nth
                                                                                                  ^clojure.lang.Indexed c__6371__auto__
                                                                                                  (int
                                                                                                    i__14090))]
                                                                                               (chunk-append
                                                                                                 b__14091
                                                                                                 (let 
                                                                                                   [ks
                                                                                                    (when
                                                                                                      (and
                                                                                                        with_key_summary
                                                                                                        (not=
                                                                                                          :fulltext
                                                                                                          index))
                                                                                                      (key-summary
                                                                                                        (^clojure.lang.IFn index
                                                                                                          (^clojure.lang.IFn tier
                                                                                                            db))
                                                                                                        (#{:aevt
                                                                                                           :avet}
                                                                                                          index)))]
                                                                                                   (merge
                                                                                                     (apply
                                                                                                       merge-with
                                                                                                       +
                                                                                                       (vals
                                                                                                         ((ns-resolve
                                                                                                            'datomic.stats
                                                                                                            (symbol
                                                                                                              (name
                                                                                                                index)))
                                                                                                           db
                                                                                                           (^clojure.lang.IFn tier
                                                                                                             db))))
                                                                                                     {:index
                                                                                                      index,
                                                                                                      :tier
                                                                                                      tier}
                                                                                                     ks)))
                                                                                               (recur
                                                                                                 (inc
                                                                                                   i__14090)))
                                                                                             true))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__14091)
                                                                                           (^clojure.lang.IFn iter__14088
                                                                                             (chunk-rest
                                                                                               s__14089)))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__14091)
                                                                                           nil)))
                                                                                     (let 
                                                                                       [tier
                                                                                        (first
                                                                                          s__14089)]
                                                                                       (cons
                                                                                         (let 
                                                                                           [ks
                                                                                            (when
                                                                                              (and
                                                                                                with_key_summary
                                                                                                (not=
                                                                                                  :fulltext
                                                                                                  index))
                                                                                              (key-summary
                                                                                                (^clojure.lang.IFn index
                                                                                                  (^clojure.lang.IFn tier
                                                                                                    db))
                                                                                                (#{:aevt
                                                                                                   :avet}
                                                                                                  index)))]
                                                                                           (merge
                                                                                             (apply
                                                                                               merge-with
                                                                                               +
                                                                                               (vals
                                                                                                 ((ns-resolve
                                                                                                    'datomic.stats
                                                                                                    (symbol
                                                                                                      (name
                                                                                                        index)))
                                                                                                   db
                                                                                                   (^clojure.lang.IFn tier
                                                                                                     db))))
                                                                                             {:index
                                                                                              index,
                                                                                              :tier
                                                                                              tier}
                                                                                             ks))
                                                                                         (^clojure.lang.IFn iter__14088
                                                                                           (rest
                                                                                             s__14089)))))))))))
                                                  fs__6370__auto__ (seq
                                                                     (^clojure.lang.IFn iterys__6369__auto__
                                                                       [:index
                                                                        :mid-index
                                                                        :history]))]
                                              (if fs__6370__auto__
                                                (concat
                                                  fs__6370__auto__
                                                  (^clojure.lang.IFn iter__14086 (rest s__14087)))
                                                (recur (rest s__14087))))))))))]
         (^clojure.lang.IFn iter__6373__auto__ [:avet :aevt :eavt :raet :fulltext])))))
  (reset-meta!
    #'sizes
    (assoc
      {:arglists (clojure.core/list ['db '& {:keys ['with-key-summary]}]), :column (int 1)}
      :name
      'sizes
      :ns
      *ns*))
  (defn datom-count
    ([sizes]
      (apply
        +
        (filter
          identity
          (map
            :data-count
            (filter (fn fn__14114 ([p1__14113#] (= :eavt (:index p1__14113#)))) sizes))))))
  (reset-meta!
    #'datom-count
    (assoc {:arglists (clojure.core/list ['sizes]), :column (int 1)} :name 'datom-count :ns *ns*))
  (defn index-datom-count ([sizes] (apply + (map :data-count (filter :data-count sizes)))))
  (reset-meta!
    #'index-datom-count
    (assoc
      {:arglists (clojure.core/list ['sizes]), :column (int 1)}
      :name
      'index-datom-count
      :ns
      *ns*))
  (defn segment-count ([sizes] (apply + (map :seg-count (filter :seg-count sizes)))))
  (reset-meta!
    #'segment-count
    (assoc
      {:arglists (clojure.core/list ['sizes]), :column (int 1)}
      :name
      'segment-count
      :ns
      *ns*))
  (defn sizes->metrics
    ([s]
      {:IndexSegments
       (segment-count (remove (fn fn__14121 ([p1__14119#] (= :fulltext (:index p1__14119#)))) s)),
       :FulltextSegments
       (segment-count (filter (fn fn__14123 ([p1__14120#] (= :fulltext (:index p1__14120#)))) s)),
       :Datoms (datom-count s),
       :IndexDatoms (index-datom-count s)}))
  (reset-meta!
    #'sizes->metrics
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'sizes->metrics :ns *ns*))
  (defn index-metrics ([db] (sizes->metrics (sizes db))))
  (reset-meta!
    #'index-metrics
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'index-metrics :ns *ns*)))
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
        (fn fn__17777 ([n de] (let [v (:v (:key de))] (if (or (nil? v) (= 0 v)) (inc n) n))))
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
         (fn fn__17782
           ([acc entry]
             (let [temp__5802__auto__ (some-> entry (:key) (^clojure.lang.IFn partfn))]
               (if temp__5802__auto__
                 (let [k temp__5802__auto__
                       map__17784 (get acc k {:data-count 0, :seg-count 0})
                       map__17784 (if (seq? map__17784)
                                    (if (next map__17784)
                                      (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                        (to-array map__17784))
                                      (if (seq map__17784) (first map__17784) {}))
                                    map__17784)
                       m map__17784
                       data_count (get map__17784 :data-count)
                       seg_count (get map__17784 :seg-count)
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
  (def avet
   (fn avet
     ([db index]
       (index-summary
         db
         index
         :avet
         (fn fn__17789
           ([p1__17788#]
             (.ident
               ^datomic.Database db
               (db/resolve-kw
                 db
                 (java.lang.Integer/valueOf
                   (int (.getA ^datomic.impl.db.IDatum p1__17788#)))))))))))
  (reset-meta!
    #'avet
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'avet
      :ns
      *ns*))
  (def aevt
   (fn aevt
     ([db index]
       (index-summary
         db
         index
         :aevt
         (fn fn__17793
           ([p1__17792#]
             (.ident
               ^datomic.Database db
               (db/resolve-kw
                 db
                 (java.lang.Integer/valueOf
                   (int (.getA ^datomic.impl.db.IDatum p1__17792#)))))))))))
  (reset-meta!
    #'aevt
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'aevt
      :ns
      *ns*))
  (def eavt
   (fn eavt
     ([db index]
       (index-summary
         db
         index
         :eavt
         (fn fn__17797
           ([p1__17796#]
             (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__17796#))]
               (or (.ident ^datomic.Database db (long part)) (long part)))))))))
  (reset-meta!
    #'eavt
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'eavt
      :ns
      *ns*))
  (def raet
   (fn raet
     ([db index]
       (index-summary
         db
         index
         :raet
         (fn fn__17802
           ([p1__17801#]
             (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__17801#))]
               (or (.ident ^datomic.Database db (long part)) (long part)))))))))
  (reset-meta!
    #'raet
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'raet
      :ns
      *ns*))
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
                     (fn fn__17809
                       ([m p__17808]
                         (let [vec__17810 p__17808
                               k (nth vec__17810 (int 0) nil)
                               v (nth vec__17810 (int 1) nil)
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
                                (fn fn__17816
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
                                  (fn fn__17820
                                    ([p__17819]
                                      (let [map__17821 p__17819
                                            map__17821 (if (seq? map__17821)
                                                         (if (next map__17821)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__17821))
                                                           (if
                                                             (seq map__17821)
                                                             (first map__17821)
                                                             {}))
                                                         map__17821)
                                            key (get map__17821 :key)]
                                        (:a key))))
                                  (:aevt (^clojure.lang.IFn tier db))))
         (memory-tiers tier) (do
                               (algo.lazy/fred
                                 (fn fn__17823
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
                                   (fn fn__17826 ([datom] (:a datom)))
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
        (map (fn fn__17830 ([p1__17829#] (index-attr-stats db p1__17829#))) tiers))))
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
                                                 (fn fn__17840
                                                   ([p1__17834#] (= a (:a p1__17834#))))
                                                 (.seek
                                                   ^datomic.btset.IDataSet idx
                                                   (db/datum db :a a)))
                                         vec__17837 (iter/reduce
                                                      (fn fn__17843
                                                        ([p__17842 datom]
                                                          (let [vec__17844 p__17842
                                                                ret (nth vec__17844 (int 0) nil)
                                                                nexte (nth vec__17844 (int 1) nil)
                                                                c (nth vec__17844 (int 2) nil)]
                                                            (if
                                                              (= (long split_n) c)
                                                              [(conj ret [nexte (long split_n)])
                                                               (:e datom)
                                                               1]
                                                              [ret nexte (inc c)]))))
                                                      [[] (:e (iter/iget diter)) 0]
                                                      diter)
                                         ret (nth vec__17837 (int 0) nil)
                                         nexte (nth vec__17837 (int 1) nil)
                                         c (nth vec__17837 (int 2) nil)]
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
                                              (fn fn__17849
                                                ([p1__17835#] (< (:a (:key p1__17835#)) a))))
                                            (take-while
                                              (fn fn__17851
                                                ([p1__17836#] (= a (:a (:key p1__17836#))))))
                                            (map
                                              (fn fn__17854
                                                ([p__17853]
                                                  (let [map__17855 p__17853
                                                        map__17855 (if
                                                                     (seq? map__17855)
                                                                     (if
                                                                       (next map__17855)
                                                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                         (to-array map__17855))
                                                                       (if
                                                                         (seq map__17855)
                                                                         (first map__17855)
                                                                         {}))
                                                                     map__17855)
                                                        key (get map__17855 :key)
                                                        count (get map__17855 :count)]
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
        (fn fn__17867
          ([p1__17865# p2__17866#]
            (merge-data
              (reify
                java.util.Comparator
                (^int compare [this a b] (clojure.lang.Util/compare a b)))
              p1__17865#
              p2__17866#)))
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
          (fn fn__17874
            ([p__17873]
              (let [vec__17875 p__17873
                    vec__17878 (nth vec__17875 (int 0) nil)
                    e1 (nth vec__17878 (int 0) nil)
                    ct (nth vec__17878 (int 1) nil)
                    vec__17881 (nth vec__17875 (int 1) nil)
                    e2 (nth vec__17881 (int 0) nil)]
                [e1 e2 ct]))))
        (partition-all
          2
          1
          (merge-splits
            (map (fn fn__17885 ([p1__17872#] (index-attr-splits db p1__17872# attr))) tiers))))))
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
          (fn fn__17889
            ([attr]
              [attr
               {:count
                (apply
                  +
                  (map
                    (fn fn__17890 ([p1__17888#] (nth p1__17888# (int 2))))
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
     ([db & p__17894]
       (let [map__17895 p__17894
             map__17895 (if (seq? map__17895)
                          (if (next map__17895)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__17895))
                            (if (seq map__17895) (first map__17895) {}))
                          map__17895)
             with_key_summary (get map__17895 :with-key-summary)
             iter__6373__auto__ (fn iter__17896
                                  ([s__17897]
                                    (lazy-seq
                                      (loop [s__17897 s__17897]
                                        (let [temp__5804__auto__ (seq s__17897)]
                                          (when temp__5804__auto__
                                            (let [xs__6360__auto__ temp__5804__auto__
                                                  index (first xs__6360__auto__)
                                                  iterys__6369__auto__ (fn 
                                                                         iter__17898
                                                                         ([s__17899]
                                                                           (lazy-seq
                                                                             (let 
                                                                               [s__17899 s__17899
                                                                                temp__5804__auto__
                                                                                (seq s__17899)]
                                                                               (when
                                                                                 temp__5804__auto__
                                                                                 (let 
                                                                                   [s__17899
                                                                                    temp__5804__auto__]
                                                                                   (if
                                                                                     (chunked-seq?
                                                                                       s__17899)
                                                                                     (let 
                                                                                       [c__6371__auto__
                                                                                        (chunk-first
                                                                                          s__17899)
                                                                                        size__6372__auto__
                                                                                        (int
                                                                                          (count
                                                                                            c__6371__auto__))
                                                                                        b__17901
                                                                                        (chunk-buffer
                                                                                          (java.lang.Integer/valueOf
                                                                                            (int
                                                                                              size__6372__auto__)))]
                                                                                       (if
                                                                                         (loop 
                                                                                           [i__17900
                                                                                            (int
                                                                                              0)]
                                                                                           (if
                                                                                             (<
                                                                                               i__17900
                                                                                               size__6372__auto__)
                                                                                             (let 
                                                                                               [tier
                                                                                                (.nth
                                                                                                  ^clojure.lang.Indexed c__6371__auto__
                                                                                                  (int
                                                                                                    i__17900))]
                                                                                               (chunk-append
                                                                                                 b__17901
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
                                                                                                   i__17900)))
                                                                                             true))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__17901)
                                                                                           (^clojure.lang.IFn iter__17898
                                                                                             (chunk-rest
                                                                                               s__17899)))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__17901)
                                                                                           nil)))
                                                                                     (let 
                                                                                       [tier
                                                                                        (first
                                                                                          s__17899)]
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
                                                                                         (^clojure.lang.IFn iter__17898
                                                                                           (rest
                                                                                             s__17899)))))))))))
                                                  fs__6370__auto__ (seq
                                                                     (^clojure.lang.IFn iterys__6369__auto__
                                                                       [:index
                                                                        :mid-index
                                                                        :history]))]
                                              (if fs__6370__auto__
                                                (concat
                                                  fs__6370__auto__
                                                  (^clojure.lang.IFn iter__17896 (rest s__17897)))
                                                (recur (rest s__17897))))))))))]
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
            (filter (fn fn__17924 ([p1__17923#] (= :eavt (:index p1__17923#)))) sizes))))))
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
       (segment-count (remove (fn fn__17931 ([p1__17929#] (= :fulltext (:index p1__17929#)))) s)),
       :FulltextSegments
       (segment-count (filter (fn fn__17933 ([p1__17930#] (= :fulltext (:index p1__17930#)))) s)),
       :Datoms (datom-count s),
       :IndexDatoms (index-datom-count s)}))
  (reset-meta!
    #'sizes->metrics
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'sizes->metrics :ns *ns*))
  (defn index-metrics ([db] (sizes->metrics (sizes db))))
  (reset-meta!
    #'index-metrics
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'index-metrics :ns *ns*)))
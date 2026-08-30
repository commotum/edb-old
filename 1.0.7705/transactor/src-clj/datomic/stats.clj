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
        (fn fn__15096 ([n de] (let [v (:v (:key de))] (if (or (nil? v) (= 0 v)) (inc n) n))))
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
  (defn key-summary
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
    ([des] (key-summary des false)))
  (reset-meta!
    #'key-summary
    (assoc
      {:arglists (clojure.core/list ['des] ['des 'a-freqs?]), :column (int 1)}
      :name
      'key-summary
      :ns
      *ns*))
  (defn index-summary
    ([db index idx partfn]
      (reduce
        (fn fn__15101
          ([acc entry]
            (let [temp__5823__auto__ (some-> entry (:key) (^clojure.lang.IFn partfn))]
              (if temp__5823__auto__
                (let [k temp__5823__auto__
                      map__15103 (get acc k {:data-count 0, :seg-count 0})
                      map__15103 (if (seq? map__15103)
                                   (if (next map__15103)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__15103))
                                     (if (seq map__15103) (first map__15103) {}))
                                   map__15103)
                      m map__15103
                      data_count (get map__15103 :data-count)
                      seg_count (get map__15103 :seg-count)
                      m (assoc
                          m
                          :seg-count
                          (inc seg_count)
                          :data-count
                          (+ data_count (:count entry)))]
                  (assoc acc k m))
                acc))))
        {}
        (^clojure.lang.IFn idx index))))
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
        (fn fn__15108
          ([p1__15107#]
            (.ident
              ^datomic.Database db
              (db/resolve-kw
                db
                (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__15107#))))))))))
  (reset-meta!
    #'avet
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'avet
      :ns
      *ns*))
  (defn aevt
    ([db index]
      (index-summary
        db
        index
        :aevt
        (fn fn__15112
          ([p1__15111#]
            (.ident
              ^datomic.Database db
              (db/resolve-kw
                db
                (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__15111#))))))))))
  (reset-meta!
    #'aevt
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'aevt
      :ns
      *ns*))
  (defn eavt
    ([db index]
      (index-summary
        db
        index
        :eavt
        (fn fn__15116
          ([p1__15115#]
            (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__15115#))]
              (or (.ident ^datomic.Database db (long part)) (long part))))))))
  (reset-meta!
    #'eavt
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'index]), :column (int 1)}
      :name
      'eavt
      :ns
      *ns*))
  (defn raet
    ([db index]
      (index-summary
        db
        index
        :raet
        (fn fn__15121
          ([p1__15120#]
            (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__15120#))]
              (or (.ident ^datomic.Database db (long part)) (long part))))))))
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
  (defn datom-counts
    ([summary_fn db]
      (let [index (total (^clojure.lang.IFn summary_fn db (:index db)))
            mid (total (^clojure.lang.IFn summary_fn db (:mid-index db)))
            hist (total (^clojure.lang.IFn summary_fn db (:history db)))]
        {:index-datoms index,
         :mid-index-datoms mid,
         :index+mid-datoms (+ index mid),
         :history-datoms hist})))
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
                     (fn fn__15128
                       ([m p__15127]
                         (let [vec__15129 p__15127
                               k (nth vec__15129 (int 0) nil)
                               v (nth vec__15129 (int 1) nil)
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
  (defn index-attr-stats
    ([db tier]
      (cond
        (storage-tiers tier) (algo.lazy/fred
                               (fn fn__15135
                                 ([acc entries]
                                   (let [temp__5823__auto__ (.ident
                                                              ^datomic.Database db
                                                              (:a (:key (first entries))))]
                                     (if temp__5823__auto__
                                       (let [k temp__5823__auto__]
                                         (assoc acc k {:count (apply + (map :count entries))}))
                                       acc))))
                               {}
                               (algo.lazy/fully-partition-by
                                 (fn fn__15139
                                   ([p__15138]
                                     (let [map__15140 p__15138
                                           map__15140 (if (seq? map__15140)
                                                        (if (next map__15140)
                                                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                            (to-array map__15140))
                                                          (if (seq map__15140)
                                                            (first map__15140)
                                                            {}))
                                                        map__15140)
                                           key (get map__15140 :key)]
                                       (:a key))))
                                 (:aevt (^clojure.lang.IFn tier db))))
        (memory-tiers tier) (do
                              (algo.lazy/fred
                                (fn fn__15142
                                  ([acc datoms]
                                    (let [temp__5823__auto__ (.ident
                                                               ^datomic.Database db
                                                               (:a (first datoms)))]
                                      (if temp__5823__auto__
                                        (let [k temp__5823__auto__]
                                          (assoc
                                            acc
                                            k
                                            {:count
                                             (java.lang.Integer/valueOf (int (count datoms)))}))
                                        acc))))
                                {}
                                (algo.lazy/fully-partition-by
                                  (fn fn__15145 ([datom] (:a datom)))
                                  (:aevt (^clojure.lang.IFn tier db))))))))
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
        (map (fn fn__15149 ([p1__15148#] (index-attr-stats db p1__15148#))) tiers))))
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
  (defn index-attr-splits
    ([db tier attr]
      (let [a (.entid ^datomic.Database db attr)]
        (cond
          (memory-tiers tier) (let [temp__5825__auto__ (:aevt (^clojure.lang.IFn tier db))]
                                (when temp__5825__auto__
                                  (let [idx temp__5825__auto__
                                        split_n 1000
                                        diter (iter/take-while
                                                (fn fn__15159 ([p1__15153#] (= a (:a p1__15153#))))
                                                (.seek
                                                  ^datomic.btset.IDataSet idx
                                                  (db/datum db :a a)))
                                        vec__15156 (iter/reduce
                                                     (fn fn__15162
                                                       ([p__15161 datom]
                                                         (let [vec__15163 p__15161
                                                               ret (nth vec__15163 (int 0) nil)
                                                               nexte (nth vec__15163 (int 1) nil)
                                                               c (nth vec__15163 (int 2) nil)]
                                                           (if
                                                             (= (long split_n) c)
                                                             [(conj ret [nexte (long split_n)])
                                                              (:e datom)
                                                              1]
                                                             [ret nexte (inc c)]))))
                                                     [[] (:e (iter/iget diter)) 0]
                                                     diter)
                                        ret (nth vec__15156 (int 0) nil)
                                        nexte (nth vec__15156 (int 1) nil)
                                        c (nth vec__15156 (int 2) nil)]
                                    (cond-> ret (clojure.lang.Numbers/isPos c) (conj [nexte c])))))
          (storage-tiers tier) (do
                                 (let [temp__5825__auto__ (seq
                                                            (:aevt (^clojure.lang.IFn tier db)))]
                                   (when temp__5825__auto__
                                     (let [dirs temp__5825__auto__]
                                       (into
                                         []
                                         (comp
                                           (drop-while
                                             (fn fn__15168
                                               ([p1__15154#] (< (:a (:key p1__15154#)) a))))
                                           (take-while
                                             (fn fn__15170
                                               ([p1__15155#] (= a (:a (:key p1__15155#))))))
                                           (map
                                             (fn fn__15173
                                               ([p__15172]
                                                 (let [map__15174 p__15172
                                                       map__15174 (if
                                                                    (seq? map__15174)
                                                                    (if
                                                                      (next map__15174)
                                                                      (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                        (to-array map__15174))
                                                                      (if
                                                                        (seq map__15174)
                                                                        (first map__15174)
                                                                        {}))
                                                                    map__15174)
                                                       key (get map__15174 :key)
                                                       count (get map__15174 :count)]
                                                   [(:e key) count])))))
                                         dirs)))))))))
  (reset-meta!
    #'index-attr-splits
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'tier 'attr]),
       :column (int 1)}
      :name
      'index-attr-splits
      :ns
      *ns*))
  (defn merge-data
    ([cmp ds1 ds2]
      (let [ds1 (seq ds1) ds2 (seq ds2)]
        (if (and ds1 ds2)
          (lazy-seq
            (let [d1 (first ds1) d2 (first ds2) c (.compare ^java.util.Comparator cmp d1 d2)]
              (cond
                (< c 0) (cons d1 (merge-data cmp (next ds1) ds2))
                (> c 0) (cons d2 (merge-data cmp ds1 (next ds2)))
                :else (do (cons d1 (cons d2 (merge-data cmp (next ds1) (next ds2))))))))
          (or ds1 ds2)))))
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
        (fn fn__15186
          ([p1__15184# p2__15185#]
            (merge-data
              (reify
                java.util.Comparator
                (^int compare [this a b] (clojure.lang.Util/compare a b)))
              p1__15184#
              p2__15185#)))
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
          (fn fn__15193
            ([p__15192]
              (let [vec__15194 p__15192
                    vec__15197 (nth vec__15194 (int 0) nil)
                    e1 (nth vec__15197 (int 0) nil)
                    ct (nth vec__15197 (int 1) nil)
                    vec__15200 (nth vec__15194 (int 1) nil)
                    e2 (nth vec__15200 (int 0) nil)]
                [e1 e2 ct]))))
        (partition-all
          2
          1
          (merge-splits
            (map (fn fn__15204 ([p1__15191#] (index-attr-splits db p1__15191# attr))) tiers))))))
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
          (fn fn__15208
            ([attr]
              [attr
               {:count
                (apply
                  +
                  (map
                    (fn fn__15209 ([p1__15207#] (nth p1__15207# (int 2))))
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
  (defn sizes
    ([db & p__15213]
      (let [map__15214 p__15213
            map__15214 (if (seq? map__15214)
                         (if (next map__15214)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15214))
                           (if (seq map__15214) (first map__15214) {}))
                         map__15214)
            with_key_summary (get map__15214 :with-key-summary)
            iter__6398__auto__ (fn iter__15215
                                 ([s__15216]
                                   (lazy-seq
                                     (loop [s__15216 s__15216]
                                       (let [temp__5825__auto__ (seq s__15216)]
                                         (when temp__5825__auto__
                                           (let [xs__6385__auto__ temp__5825__auto__
                                                 index (first xs__6385__auto__)
                                                 iterys__6394__auto__ (fn 
                                                                        iter__15217
                                                                        ([s__15218]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__15218 s__15218
                                                                               temp__5825__auto__
                                                                               (seq s__15218)]
                                                                              (when
                                                                                temp__5825__auto__
                                                                                (let 
                                                                                  [s__15218
                                                                                   temp__5825__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__15218)
                                                                                    (let 
                                                                                      [c__6396__auto__
                                                                                       (chunk-first
                                                                                         s__15218)
                                                                                       size__6397__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6396__auto__))
                                                                                       b__15220
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6397__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__15219
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__15219
                                                                                              size__6397__auto__)
                                                                                            (let 
                                                                                              [tier
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6396__auto__
                                                                                                 (int
                                                                                                   i__15219))]
                                                                                              (chunk-append
                                                                                                b__15220
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
                                                                                                  i__15219)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__15220)
                                                                                          (^clojure.lang.IFn iter__15217
                                                                                            (chunk-rest
                                                                                              s__15218)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__15220)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [tier
                                                                                       (first
                                                                                         s__15218)]
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
                                                                                        (^clojure.lang.IFn iter__15217
                                                                                          (rest
                                                                                            s__15218)))))))))))
                                                 fs__6395__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6394__auto__
                                                                      [:index
                                                                       :mid-index
                                                                       :history]))]
                                             (if fs__6395__auto__
                                               (concat
                                                 fs__6395__auto__
                                                 (^clojure.lang.IFn iter__15215 (rest s__15216)))
                                               (recur (rest s__15216))))))))))]
        (^clojure.lang.IFn iter__6398__auto__ [:avet :aevt :eavt :raet :fulltext]))))
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
            (filter (fn fn__15243 ([p1__15242#] (= :eavt (:index p1__15242#)))) sizes))))))
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
       (segment-count (remove (fn fn__15250 ([p1__15248#] (= :fulltext (:index p1__15248#)))) s)),
       :FulltextSegments
       (segment-count (filter (fn fn__15252 ([p1__15249#] (= :fulltext (:index p1__15249#)))) s)),
       :Datoms (datom-count s),
       :IndexDatoms (index-datom-count s)}))
  (reset-meta!
    #'sizes->metrics
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'sizes->metrics :ns *ns*))
  (defn index-metrics ([db] (sizes->metrics (sizes db))))
  (reset-meta!
    #'index-metrics
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'index-metrics :ns *ns*)))
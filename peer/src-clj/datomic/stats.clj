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
        (fn fn__17833 ([n de] (let [v (:v (:key de))] (if (or (nil? v) (= 0 v)) (inc n) n))))
        0
        des)))
  (reset-meta!
    #'sparse-v-count
    (assoc
      {:private true, :arglists (clojure.core/list ['des]), :column 1}
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
  (defn index-summary
    ([db index idx partfn]
      (reduce
        (fn fn__17838
          ([acc entry]
            (let [temp__5455__auto__ (some-> entry (:key) (^clojure.lang.IFn partfn))]
              (if temp__5455__auto__
                (let [k temp__5455__auto__
                      map__17840 (get acc k {:data-count 0, :seg-count 0})
                      map__17840 (if (seq? map__17840)
                                   (clojure.lang.PersistentHashMap/create (seq map__17840))
                                   map__17840)
                      m map__17840
                      data_count (get map__17840 :data-count)
                      seg_count (get map__17840 :seg-count)
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
  (defn avet
    ([db index]
      (index-summary
        db
        index
        :avet
        (fn fn__17845
          ([p1__17844#]
            (.ident
              db
              (db/resolve-kw
                db
                (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__17844#))))))))))
  (defn aevt
    ([db index]
      (index-summary
        db
        index
        :aevt
        (fn fn__17849
          ([p1__17848#]
            (.ident
              db
              (db/resolve-kw
                db
                (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__17848#))))))))))
  (defn eavt
    ([db index]
      (index-summary
        db
        index
        :eavt
        (fn fn__17853
          ([p1__17852#]
            (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__17852#))]
              (or (.ident db (long part)) (long part))))))))
  (defn raet
    ([db index]
      (index-summary
        db
        index
        :raet
        (fn fn__17858
          ([p1__17857#]
            (let [part (db/partition-eid (.getE ^datomic.impl.db.IDatum p1__17857#))]
              (or (.ident db (long part)) (long part))))))))
  (defn total ([m] (apply + (map :data-count (vals m)))))
  (defn datom-counts
    ([summary_fn db]
      (let [index (total (^clojure.lang.IFn summary_fn db (:index db)))
            mid (total (^clojure.lang.IFn summary_fn db (:mid-index db)))
            hist (total (^clojure.lang.IFn summary_fn db (:history db)))]
        {:index-datoms index,
         :mid-index-datoms mid,
         :index+mid-datoms (+ index mid),
         :history-datoms hist})))
  (defn fulltext
    ([db index]
      (let [olookup (:olookup (:fulltext index))
            attrmap (:attrmap (:root (:fulltext index)))
            aevt_summary (aevt db index)
            result (reduce
                     (fn fn__17865
                       ([m p__17864]
                         (let [vec__17866 p__17864
                               k (nth vec__17866 (int 0) nil)
                               v (nth vec__17866 (int 1) nil)
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
  (def storage-tiers #{:mid-index :index :history})
  (def memory-tiers #{:indexing :memidx})
  (def tiers (into storage-tiers memory-tiers))
  (defn index-attr-stats
    ([db tier]
      (cond
        (storage-tiers tier) (algo.lazy/fred
                               (fn fn__17872
                                 ([acc entries]
                                   (let [temp__5455__auto__ (.ident
                                                              ^datomic.Database db
                                                              (:a (:key (first entries))))]
                                     (if temp__5455__auto__
                                       (let [k temp__5455__auto__]
                                         (assoc acc k {:count (apply + (map :count entries))}))
                                       acc))))
                               {}
                               (algo.lazy/fully-partition-by
                                 (fn fn__17876
                                   ([p__17875]
                                     (let [map__17877 p__17875
                                           map__17877 (if (seq? map__17877)
                                                        (clojure.lang.PersistentHashMap/create
                                                          (seq map__17877))
                                                        map__17877)
                                           key (get map__17877 :key)]
                                       (:a key))))
                                 (:aevt (^clojure.lang.IFn tier db))))
        (memory-tiers tier) (do
                              (algo.lazy/fred
                                (fn fn__17879
                                  ([acc datoms]
                                    (let [temp__5455__auto__ (.ident
                                                               ^datomic.Database db
                                                               (:a (first datoms)))]
                                      (if temp__5455__auto__
                                        (let [k temp__5455__auto__]
                                          (assoc
                                            acc
                                            k
                                            {:count
                                             (java.lang.Integer/valueOf (int (count datoms)))}))
                                        acc))))
                                {}
                                (algo.lazy/fully-partition-by
                                  (fn fn__17882 ([datom] (:a datom)))
                                  (:aevt (^clojure.lang.IFn tier db))))))))
  (defn db-attr-stats
    ([db]
      (apply
        merge-with
        (partial merge-with +)
        (map (fn fn__17886 ([p1__17885#] (index-attr-stats db p1__17885#))) tiers))))
  (defn db-stats
    ([db]
      (let [attr_stats (db-attr-stats db)]
        {:datoms (transduce (map :count) + (vals attr_stats)), :attrs attr_stats})))
  (defn index-attr-splits
    ([db tier attr]
      (let [a (.entid ^datomic.Database db attr)]
        (cond
          (memory-tiers tier) (let [temp__5457__auto__ (:aevt (^clojure.lang.IFn tier db))]
                                (when temp__5457__auto__
                                  (let [idx temp__5457__auto__
                                        split_n 1000
                                        diter (iter/take-while
                                                (fn fn__17896 ([p1__17890#] (= a (:a p1__17890#))))
                                                (.seek
                                                  ^datomic.btset.IDataSet idx
                                                  (db/datum db :a a)))
                                        vec__17893 (iter/reduce
                                                     (fn fn__17899
                                                       ([p__17898 datom]
                                                         (let [vec__17900 p__17898
                                                               ret (nth vec__17900 (int 0) nil)
                                                               nexte (nth vec__17900 (int 1) nil)
                                                               c (nth vec__17900 (int 2) nil)]
                                                           (if
                                                             (= (long split_n) c)
                                                             [(conj ret [nexte (long split_n)])
                                                              (:e datom)
                                                              1]
                                                             [ret nexte (inc c)]))))
                                                     [[] (:e (iter/iget diter)) 0]
                                                     diter)
                                        ret (nth vec__17893 (int 0) nil)
                                        nexte (nth vec__17893 (int 1) nil)
                                        c (nth vec__17893 (int 2) nil)]
                                    (cond-> ret (clojure.lang.Numbers/isPos c) (conj [nexte c])))))
          (storage-tiers tier) (do
                                 (let [temp__5457__auto__ (seq
                                                            (:aevt (^clojure.lang.IFn tier db)))]
                                   (when temp__5457__auto__
                                     (let [dirs temp__5457__auto__]
                                       (into
                                         []
                                         (comp
                                           (drop-while
                                             (fn fn__17905
                                               ([p1__17891#] (< (:a (:key p1__17891#)) a))))
                                           (take-while
                                             (fn fn__17907
                                               ([p1__17892#] (= a (:a (:key p1__17892#))))))
                                           (map
                                             (fn fn__17910
                                               ([p__17909]
                                                 (let [map__17911 p__17909
                                                       map__17911 (if
                                                                    (seq? map__17911)
                                                                    (clojure.lang.PersistentHashMap/create
                                                                      (seq map__17911))
                                                                    map__17911)
                                                       key (get map__17911 :key)
                                                       count (get map__17911 :count)]
                                                   [(:e key) count])))))
                                         dirs)))))))))
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
  (defn merge-splits
    ([splits]
      (reduce
        (fn fn__17923
          ([p1__17921# p2__17922#]
            (merge-data
              (reify
                java.util.Comparator
                (^int compare [this a b] (clojure.lang.Util/compare a b)))
              p1__17921#
              p2__17922#)))
        splits)))
  (reset-meta!
    #'merge-splits
    (assoc
      {:private true, :arglists (clojure.core/list ['splits]), :column 1}
      :name
      'merge-splits
      :ns
      *ns*))
  (defn db-attr-splits
    ([db attr]
      (into
        []
        (map
          (fn fn__17930
            ([p__17929]
              (let [vec__17931 p__17929
                    vec__17934 (nth vec__17931 (int 0) nil)
                    e1 (nth vec__17934 (int 0) nil)
                    ct (nth vec__17934 (int 1) nil)
                    vec__17937 (nth vec__17931 (int 1) nil)
                    e2 (nth vec__17937 (int 0) nil)]
                [e1 e2 ct]))))
        (partition-all
          2
          1
          (merge-splits
            (map (fn fn__17941 ([p1__17928#] (index-attr-splits db p1__17928# attr))) tiers))))))
  (defn attr-stats-from-splits
    ([db]
      (into
        {}
        (map
          (fn fn__17945
            ([attr]
              [attr
               {:count
                (apply
                  +
                  (map
                    (fn fn__17946 ([p1__17944#] (nth p1__17944# (int 2))))
                    (db-attr-splits db attr)))}])))
        (keys (db-attr-stats db)))))
  (defn sizes
    ([db & p__17950]
      (let [map__17951 p__17950
            map__17951 (if (seq? map__17951)
                         (clojure.lang.PersistentHashMap/create (seq map__17951))
                         map__17951)
            with_key_summary (get map__17951 :with-key-summary)
            iter__6025__auto__ (fn iter__17952
                                 ([s__17953]
                                   (lazy-seq
                                     (loop [s__17953 s__17953]
                                       (let [temp__5457__auto__ (seq s__17953)]
                                         (when temp__5457__auto__
                                           (let [xs__6012__auto__ temp__5457__auto__
                                                 index (first xs__6012__auto__)
                                                 iterys__6021__auto__ (fn 
                                                                        iter__17954
                                                                        ([s__17955]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__17955 s__17955
                                                                               temp__5457__auto__
                                                                               (seq s__17955)]
                                                                              (when
                                                                                temp__5457__auto__
                                                                                (let 
                                                                                  [s__17955
                                                                                   temp__5457__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__17955)
                                                                                    (let 
                                                                                      [c__6023__auto__
                                                                                       (chunk-first
                                                                                         s__17955)
                                                                                       size__6024__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6023__auto__))
                                                                                       b__17957
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6024__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__17956
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__17956
                                                                                              size__6024__auto__)
                                                                                            (let 
                                                                                              [tier
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6023__auto__
                                                                                                 (int
                                                                                                   i__17956))]
                                                                                              (chunk-append
                                                                                                b__17957
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
                                                                                                  i__17956)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__17957)
                                                                                          (^clojure.lang.IFn iter__17954
                                                                                            (chunk-rest
                                                                                              s__17955)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__17957)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [tier
                                                                                       (first
                                                                                         s__17955)]
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
                                                                                        (^clojure.lang.IFn iter__17954
                                                                                          (rest
                                                                                            s__17955)))))))))))
                                                 fs__6022__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6021__auto__
                                                                      [:index
                                                                       :mid-index
                                                                       :history]))]
                                             (if fs__6022__auto__
                                               (concat
                                                 fs__6022__auto__
                                                 (^clojure.lang.IFn iter__17952 (rest s__17953)))
                                               (recur (rest s__17953))))))))))]
        (^clojure.lang.IFn iter__6025__auto__ [:avet :aevt :eavt :raet :fulltext]))))
  (defn datom-count
    ([sizes]
      (apply
        +
        (filter
          identity
          (map
            :data-count
            (filter (fn fn__17980 ([p1__17979#] (= :eavt (:index p1__17979#)))) sizes))))))
  (defn index-datom-count ([sizes] (apply + (map :data-count (filter :data-count sizes)))))
  (defn segment-count ([sizes] (apply + (map :seg-count (filter :seg-count sizes)))))
  (defn sizes->metrics
    ([s]
      {:IndexSegments
       (segment-count (remove (fn fn__17987 ([p1__17985#] (= :fulltext (:index p1__17985#)))) s)),
       :FulltextSegments
       (segment-count (filter (fn fn__17989 ([p1__17986#] (= :fulltext (:index p1__17986#)))) s)),
       :Datoms (datom-count s),
       :IndexDatoms (index-datom-count s)}))
  (defn index-metrics ([db] (sizes->metrics (sizes db)))))
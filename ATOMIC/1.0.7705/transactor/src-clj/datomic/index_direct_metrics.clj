(do
  (clojure.core/in-ns 'datomic.index-direct-metrics)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.index-direct-metrics)
    {:doc
     "Computes datom and segment totals directly from durable index roots and directory nodes. Invoked after background indexing jobs to report the sizes of main, mid, history, and fulltext tiers without scanning every datom."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.index :as 'index] ['datomic.clusterfs :as 'clusterfs])
      (clojure.core/import 'datomic.index.Index)
      (clojure.core/import 'datomic.index.RootNode)
      (clojure.core/import 'datomic.index.DirNode)
      (clojure.core/import 'datomic.impl.db.IDatum)))
  (when-not (.equals 'datomic.index-direct-metrics 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.index-direct-metrics))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.index :as 'index] ['datomic.clusterfs :as 'clusterfs])
        (clojure.core/import 'datomic.index.Index)
        (clojure.core/import 'datomic.index.RootNode)
        (clojure.core/import 'datomic.index.DirNode)
        (clojure.core/import 'datomic.impl.db.IDatum))))
  (set! *warn-on-reflection* true)
  (defn index-totals
    ([idx double_count_att_ids]
      (let [totals (long-array 2)]
        (when (instance? datomic.index.Index idx)
          (let [idx idx
                r (.-root ^datomic.index.Index idx)
                lookup (.-lookup ^datomic.index.Index idx)
                n_dirs (count (.-dirids ^datomic.index.RootNode r))]
            (dotimes [dir_idx n_dirs]
              (let [d (index/get-dir-node r (long dir_idx) lookup false)
                    n_segs (count (.-segids ^datomic.index.DirNode d))
                    counts (.-counts ^datomic.index.DirNode d)
                    keydata (.-keydata ^datomic.index.DirNode d)
                    _ (aset ^longs totals (int 0) (long (+ (aget ^longs totals (int 0)) n_segs)))]
                (dotimes [seg_idx n_segs]
                  (let [datom_count (aget ^ints counts (int seg_idx))
                        datom_count (if (and
                                          double_count_att_ids
                                          (.contains
                                            ^java.util.Set double_count_att_ids
                                            (long
                                              (.getA
                                                (.get ^java.util.List keydata (int seg_idx))))))
                                      (* datom_count 2)
                                      datom_count)
                        _ (aset
                            ^longs totals
                            (int 1)
                            (long (+ (aget ^longs totals (int 1)) datom_count)))]
                    nil))))))
        {:seg-count (long (aget ^longs totals (int 0))),
         :datom-count (long (aget ^longs totals (int 1)))})))
  (reset-meta!
    #'index-totals
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['idx (.withMeta 'double-count-att-ids {:tag 'java.util.Set})]),
       :column (int 1)}
      :name
      'index-totals
      :ns
      *ns*))
  (defn fulltext-totals
    ([db tier]
      (let [totals (long-array 1)]
        (let [temp__5825__auto__ (get-in db [tier :fulltext])]
          (when temp__5825__auto__
            (let [ftstore temp__5825__auto__
                  ftroot (get ftstore :root)
                  lookup (get ftstore :olookup)
                  attrmap (get ftroot :attrmap)]
              (run!
                (fn fn__30734
                  ([kv]
                    (let [ft_id (val kv)
                          _ (aset
                              ^longs totals
                              (int 0)
                              (long
                                (+
                                  (aget ^longs totals (int 0))
                                  (long (:seg-count (clusterfs/describe (get lookup ft_id)))))))]
                      nil)))
                attrmap))))
        {:seg-count (long (aget ^longs totals (int 0))), :datom-count 0})))
  (reset-meta!
    #'fulltext-totals
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'tier]), :column (int 1)}
      :name
      'fulltext-totals
      :ns
      *ns*))
  (defn fulltext-att-ids ([db tier] (keys (get-in db [tier :fulltext :root :attrmap]))))
  (reset-meta!
    #'fulltext-att-ids
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'tier]), :column (int 1)}
      :name
      'fulltext-att-ids
      :ns
      *ns*))
  (defn direct-metrics
    ([db]
      (let [totals (long-array 4)
            tiers [:index :mid-index :history]
            double_count_att_ids (let [G__30740 (java.util.HashSet.)]
                                   (.addAll
                                     ^java.util.AbstractCollection G__30740
                                     (mapcat
                                       (fn fn__30741
                                         ([p1__30739#] (fulltext-att-ids db p1__30739#)))
                                       tiers))
                                   G__30740)]
        (run!
          (fn fn__30744
            ([p__30743]
              (let [vec__30745 p__30743
                    index (nth vec__30745 (int 0) nil)
                    tier (nth vec__30745 (int 1) nil)
                    map__30748 (if (not (= index :fulltext))
                                 (index-totals
                                   (get-in db [tier index])
                                   (when (= index :aevt) double_count_att_ids))
                                 (fulltext-totals db tier))
                    map__30748 (if (seq? map__30748)
                                 (if (next map__30748)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__30748))
                                   (if (seq map__30748) (first map__30748) {}))
                                 map__30748)
                    seg_count (get map__30748 :seg-count)
                    datom_count (get map__30748 :datom-count)
                    _ (aset
                        ^longs totals
                        (int 3)
                        (long
                          (+ (aget ^longs totals (int 3)) (long ^java.lang.Number datom_count))))
                    _ (when-not (= :fulltext index)
                        (long
                          (aset
                            ^longs totals
                            (int 0)
                            (long
                              (+
                                (aget ^longs totals (int 0))
                                (long ^java.lang.Number seg_count))))))
                    _ (when (= :eavt index)
                        (long
                          (aset
                            ^longs totals
                            (int 2)
                            (long
                              (+
                                (aget ^longs totals (int 2))
                                (long ^java.lang.Number datom_count))))))
                    _ (when (= :fulltext index)
                        (long
                          (aset
                            ^longs totals
                            (int 1)
                            (long
                              (+
                                (aget ^longs totals (int 1))
                                (long ^java.lang.Number seg_count))))))]
                nil)))
          (let [iter__6398__auto__ (fn iter__30750
                                     ([s__30751]
                                       (lazy-seq
                                         (loop [s__30751 s__30751]
                                           (let [temp__5825__auto__ (seq s__30751)]
                                             (when temp__5825__auto__
                                               (let [xs__6385__auto__ temp__5825__auto__
                                                     index (first xs__6385__auto__)
                                                     iterys__6394__auto__ (fn
                                                                            iter__30752
                                                                            ([s__30753]
                                                                              (lazy-seq
                                                                                (let
                                                                                  [s__30753
                                                                                   s__30753
                                                                                   temp__5825__auto__
                                                                                   (seq s__30753)]
                                                                                  (when
                                                                                    temp__5825__auto__
                                                                                    (let
                                                                                      [s__30753
                                                                                       temp__5825__auto__]
                                                                                      (if
                                                                                        (chunked-seq?
                                                                                          s__30753)
                                                                                        (let
                                                                                          [c__6396__auto__
                                                                                           (chunk-first
                                                                                             s__30753)
                                                                                           size__6397__auto__
                                                                                           (int
                                                                                             (count
                                                                                               c__6396__auto__))
                                                                                           b__30755
                                                                                           (chunk-buffer
                                                                                             (java.lang.Integer/valueOf
                                                                                               (int
                                                                                                 size__6397__auto__)))]
                                                                                          (if
                                                                                            (loop
                                                                                              [i__30754
                                                                                               (int
                                                                                                 0)]
                                                                                              (if
                                                                                                (<
                                                                                                  i__30754
                                                                                                  size__6397__auto__)
                                                                                                (let
                                                                                                  [tier
                                                                                                   (.nth
                                                                                                     ^clojure.lang.Indexed c__6396__auto__
                                                                                                     (int
                                                                                                       i__30754))]
                                                                                                  (chunk-append
                                                                                                    b__30755
                                                                                                    [index
                                                                                                     tier])
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__30754)))
                                                                                                true))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__30755)
                                                                                              (^clojure.lang.IFn iter__30752
                                                                                                (chunk-rest
                                                                                                  s__30753)))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__30755)
                                                                                              nil)))
                                                                                        (let
                                                                                          [tier
                                                                                           (first
                                                                                             s__30753)]
                                                                                          (cons
                                                                                            [index
                                                                                             tier]
                                                                                            (^clojure.lang.IFn iter__30752
                                                                                              (rest
                                                                                                s__30753)))))))))))
                                                     fs__6395__auto__ (seq
                                                                        (^clojure.lang.IFn iterys__6394__auto__
                                                                          tiers))]
                                                 (if fs__6395__auto__
                                                   (concat
                                                     fs__6395__auto__
                                                     (^clojure.lang.IFn iter__30750
                                                       (rest s__30751)))
                                                   (recur (rest s__30751))))))))))]
            (^clojure.lang.IFn iter__6398__auto__ [:avet :aevt :eavt :raet :fulltext])))
        {:IndexSegments (long (aget ^longs totals (int 0))),
         :FulltextSegments (long (aget ^longs totals (int 1))),
         :Datoms (long (aget ^longs totals (int 2))),
         :IndexDatoms (long (aget ^longs totals (int 3)))})))
  (reset-meta!
    #'direct-metrics
    (assoc
      {:arglists (clojure.core/list ['db]),
       :doc
       "Returns durable index totals as :IndexSegments, :FulltextSegments, :Datoms, and :IndexDatoms. Counts include main, mid, and history tiers. AEVT counts for fulltext attribute ids are doubled when computing :IndexDatoms.",
       :column (int 1)}
      :name
      'direct-metrics
      :ns
      *ns*)))

(do
  (clojure.core/in-ns 'datomic.index-direct-metrics)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.index-direct-metrics)
    {:doc
     "Generates statistics about datoms and segments by looking at root and dirs entries. Called at the end of indexing jobs. Reads fulltext cluster directly"})
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
        (let [temp__5804__auto__ (get-in db [tier :fulltext])]
          (when temp__5804__auto__
            (let [ftstore temp__5804__auto__
                  ftroot (get ftstore :root)
                  lookup (get ftstore :olookup)
                  attrmap (get ftroot :attrmap)]
              (run!
                (fn fn__17626
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
            double_count_att_ids (let [G__17632 (java.util.HashSet.)]
                                   (.addAll
                                     ^java.util.AbstractCollection G__17632
                                     (mapcat
                                       (fn fn__17633
                                         ([p1__17631#] (fulltext-att-ids db p1__17631#)))
                                       tiers))
                                   G__17632)]
        (run!
          (fn fn__17636
            ([p__17635]
              (let [vec__17637 p__17635
                    index (nth vec__17637 (int 0) nil)
                    tier (nth vec__17637 (int 1) nil)
                    map__17640 (if (not (= index :fulltext))
                                 (index-totals
                                   (get-in db [tier index])
                                   (when (= index :aevt) double_count_att_ids))
                                 (fulltext-totals db tier))
                    map__17640 (if (seq? map__17640)
                                 (if (next map__17640)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__17640))
                                   (if (seq map__17640) (first map__17640) {}))
                                 map__17640)
                    seg_count (get map__17640 :seg-count)
                    datom_count (get map__17640 :datom-count)
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
          (let [iter__6373__auto__ (fn iter__17642
                                     ([s__17643]
                                       (lazy-seq
                                         (loop [s__17643 s__17643]
                                           (let [temp__5804__auto__ (seq s__17643)]
                                             (when temp__5804__auto__
                                               (let [xs__6360__auto__ temp__5804__auto__
                                                     index (first xs__6360__auto__)
                                                     iterys__6369__auto__ (fn
                                                                            iter__17644
                                                                            ([s__17645]
                                                                              (lazy-seq
                                                                                (let
                                                                                  [s__17645
                                                                                   s__17645
                                                                                   temp__5804__auto__
                                                                                   (seq s__17645)]
                                                                                  (when
                                                                                    temp__5804__auto__
                                                                                    (let
                                                                                      [s__17645
                                                                                       temp__5804__auto__]
                                                                                      (if
                                                                                        (chunked-seq?
                                                                                          s__17645)
                                                                                        (let
                                                                                          [c__6371__auto__
                                                                                           (chunk-first
                                                                                             s__17645)
                                                                                           size__6372__auto__
                                                                                           (int
                                                                                             (count
                                                                                               c__6371__auto__))
                                                                                           b__17647
                                                                                           (chunk-buffer
                                                                                             (java.lang.Integer/valueOf
                                                                                               (int
                                                                                                 size__6372__auto__)))]
                                                                                          (if
                                                                                            (loop
                                                                                              [i__17646
                                                                                               (int
                                                                                                 0)]
                                                                                              (if
                                                                                                (<
                                                                                                  i__17646
                                                                                                  size__6372__auto__)
                                                                                                (let
                                                                                                  [tier
                                                                                                   (.nth
                                                                                                     ^clojure.lang.Indexed c__6371__auto__
                                                                                                     (int
                                                                                                       i__17646))]
                                                                                                  (chunk-append
                                                                                                    b__17647
                                                                                                    [index
                                                                                                     tier])
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__17646)))
                                                                                                true))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__17647)
                                                                                              (^clojure.lang.IFn iter__17644
                                                                                                (chunk-rest
                                                                                                  s__17645)))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__17647)
                                                                                              nil)))
                                                                                        (let
                                                                                          [tier
                                                                                           (first
                                                                                             s__17645)]
                                                                                          (cons
                                                                                            [index
                                                                                             tier]
                                                                                            (^clojure.lang.IFn iter__17644
                                                                                              (rest
                                                                                                s__17645)))))))))))
                                                     fs__6370__auto__ (seq
                                                                        (^clojure.lang.IFn iterys__6369__auto__
                                                                          tiers))]
                                                 (if fs__6370__auto__
                                                   (concat
                                                     fs__6370__auto__
                                                     (^clojure.lang.IFn iter__17642
                                                       (rest s__17643)))
                                                   (recur (rest s__17643))))))))))]
            (^clojure.lang.IFn iter__6373__auto__ [:avet :aevt :eavt :raet :fulltext])))
        {:IndexSegments (long (aget ^longs totals (int 0))),
         :FulltextSegments (long (aget ^longs totals (int 1))),
         :Datoms (long (aget ^longs totals (int 2))),
         :IndexDatoms (long (aget ^longs totals (int 3)))})))
  (reset-meta!
    #'direct-metrics
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'direct-metrics :ns *ns*)))
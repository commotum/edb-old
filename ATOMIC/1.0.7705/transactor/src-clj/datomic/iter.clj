(do
  (clojure.core/in-ns 'datomic.iter)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.iter)
    {:doc
     "Bidirectional immutable-index cursors. An Iter exposes the current value and advances with next or prev, allowing index consumers to seek once and consume lazily in either direction. The adapters in this namespace map, filter, concatenate, merge, reverse, reduce, and expose cursors as Clojure sequences or Java iterators without realizing the index."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer
        'clojure.core
        :exclude
        ['reduce 'filter 'take-while 'drop-while 'concat 'map])
      (clojure.core/require ['clojure.core :as 'core])
      (clojure.core/import 'java.util.Comparator)))
  (when-not (.equals 'datomic.iter 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.iter))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer
          'clojure.core
          :exclude
          ['reduce 'filter 'take-while 'drop-while 'concat 'map])
        (clojure.core/require ['clojure.core :as 'core])
        (clojure.core/import 'java.util.Comparator))))
  (set! *warn-on-reflection* true)
  ;; Iter is positioned on a value. next and prev return the cursor at the
  ;; adjacent value, or nil at the corresponding end of the ordered source.
  (definterface
    Iter
    (^java.lang.Object prev [])
    (^java.lang.Object next [])
    (^java.lang.Object get []))
  (clojure.core/import 'datomic.iter.Iter)
  (deftype
    Iterator
    [^{:unsynchronized-mutable true} iter]
    java.util.Iterator
    (next
      [this]
      (let [ret (.get ^datomic.iter.Iter iter)] (set! iter (.next ^datomic.iter.Iter iter)) ret))
    (^boolean hasNext [this] (boolean iter)))
  (clojure.core/import 'datomic.iter.Iterator)
  (defn ->Iterator ([iter] (datomic.iter.Iterator. iter)))
  (reset-meta!
    #'->Iterator
    (assoc {:arglists (clojure.core/list ['iter]), :column (int 1)} :name '->Iterator :ns *ns*))
  (defn iterator ([iter] (datomic.iter.Iterator. iter)))
  (reset-meta!
    #'iterator
    (assoc
      {:arglists (clojure.core/list (.withMeta ['iter] {:tag 'java.util.Iterator})),
       :doc "Adapts an Iter to a forward-only java.util.Iterator without realizing its remaining values.",
       :column (int 1)}
      :name
      'iterator
      :ns
      *ns*))
  (defn iterable
    ([iter_fn]
      (reify
        java.lang.Iterable
        (^java.util.Iterator iterator [this] (iterator (^clojure.lang.IFn iter_fn))))))
  (reset-meta!
    #'iterable
    (assoc
      {:arglists (clojure.core/list (.withMeta ['iter-fn] {:tag 'java.lang.Iterable})),
       :doc "Returns a repeatable Java Iterable that obtains a fresh Iter by invoking iter-fn for each traversal.",
       :column (int 1)}
      :name
      'iterable
      :ns
      *ns*))
  (deftype
    MapIter
    [f ^{:unsynchronized-mutable true} iter]
    datomic.iter.Iter
    (next
      [this]
      (let [temp__5825__auto__ (.next ^datomic.iter.Iter iter)]
        (when temp__5825__auto__ (let [n temp__5825__auto__] (set! iter n) this))))
    (get [this] (^clojure.lang.IFn f (.get ^datomic.iter.Iter iter))))
  (clojure.core/import 'datomic.iter.MapIter)
  (defn ->MapIter ([f iter] (datomic.iter.MapIter. f iter)))
  (reset-meta!
    #'->MapIter
    (assoc {:arglists (clojure.core/list ['f 'iter]), :column (int 1)} :name '->MapIter :ns *ns*))
  (defn map ([f iter] (when iter (datomic.iter.MapIter. f iter))))
  (reset-meta!
    #'map
    (assoc {:arglists (clojure.core/list ['f 'iter]), :column (int 1)} :name 'map :ns *ns*))
  (defn iter-seq
    ([iter]
      (lazy-seq
        (when iter
          (cons (.get ^datomic.iter.Iter iter) (iter-seq (.next ^datomic.iter.Iter iter)))))))
  (reset-meta!
    #'iter-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'iter {:tag 'Iter})]), :column (int 1)}
      :doc
      "Returns a lazy sequence beginning at iter's current value and continuing forward with next."
      :name
      'iter-seq
      :ns
      *ns*))
  (defn iter-rseq
    ([iter]
      (lazy-seq
        (when iter
          (cons (.get ^datomic.iter.Iter iter) (iter-rseq (.prev ^datomic.iter.Iter iter)))))))
  (reset-meta!
    #'iter-rseq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'iter {:tag 'Iter})]), :column (int 1)}
      :doc
      "Returns a lazy sequence beginning at iter's current value and continuing backward with prev."
      :name
      'iter-rseq
      :ns
      *ns*))
  (defn reduce
    ([f init iter]
      (loop [ret init iter iter]
        (if iter
          (recur
            (^clojure.lang.IFn f ret (.get ^datomic.iter.Iter iter))
            (.next ^datomic.iter.Iter iter))
          ret))))
  (reset-meta!
    #'reduce
    (assoc
      {:arglists (clojure.core/list ['f 'init (.withMeta 'iter {:tag 'Iter})]), :column (int 1)}
      :name
      'reduce
      :ns
      *ns*))
  (deftype
    IterCat
    [^{:unsynchronized-mutable true} iter ^{:unsynchronized-mutable true} iters]
    datomic.iter.Iter
    (next
      [this]
      (let [temp__5823__auto__ (.next ^datomic.iter.Iter iter)]
        (if temp__5823__auto__
          (let [nxt temp__5823__auto__] (set! iter nxt) this)
          (let [vec__11052 iters
                seq__11053 (seq vec__11052)
                first__11054 (first seq__11053)
                seq__11053 (next seq__11053)
                nxt first__11054
                more seq__11053]
            (when nxt (set! iter nxt) (set! iters more) this)))))
    (get [this] (.get ^datomic.iter.Iter iter)))
  (clojure.core/import 'datomic.iter.IterCat)
  (defn ->IterCat ([iter iters] (datomic.iter.IterCat. iter iters)))
  (reset-meta!
    #'->IterCat
    (assoc
      {:arglists (clojure.core/list ['iter 'iters]), :column (int 1)}
      :name
      '->IterCat
      :ns
      *ns*))
  (defn concat
    ([iters]
      (let [vec__11060 (clojure.core/filter identity iters)
            seq__11061 (seq vec__11060)
            first__11062 (first seq__11061)
            seq__11061 (next seq__11061)
            iter first__11062
            iters seq__11061]
        (when iter (datomic.iter.IterCat. iter iters)))))
  (reset-meta!
    #'concat
    (assoc {:arglists (clojure.core/list ['iters]), :column (int 1)} :name 'concat :ns *ns*))
  (defn filter
    ([p iter]
      (let [advance (fn advance
                      ([iter]
                        (loop [iter iter]
                          (when iter
                            (if (^clojure.lang.IFn p (.get ^datomic.iter.Iter iter))
                              iter
                              (recur (.next ^datomic.iter.Iter iter)))))))
            temp__5825__auto__ (^clojure.lang.IFn advance iter)]
        (when temp__5825__auto__
          (let [iter temp__5825__auto__]
            (reify
              datomic.iter.Iter
              (next
                [this]
                (let [temp__5825__auto__ (^clojure.lang.IFn advance
                                           (.next ^datomic.iter.Iter iter))]
                  (when temp__5825__auto__
                    (let [inext temp__5825__auto__]
                      (if (identical? inext iter) this (filter p inext))))))
              (get [this] (.get ^datomic.iter.Iter iter))))))))
  (reset-meta!
    #'filter
    (assoc
      {:arglists (clojure.core/list ['p (.withMeta 'iter {:tag 'Iter})]), :column (int 1)}
      :name
      'filter
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.iter" "take-while")
    {:tag datomic.iter.Iter,
     :arglists (clojure.core/list ['p (.withMeta 'iter {:tag 'Iter})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.iter" "take-while")
    (fn take_while
      ([p iter]
        (when (and iter (^clojure.lang.IFn p (.get ^datomic.iter.Iter iter)))
          (reify
            datomic.iter.Iter
            (next
              [this]
              (let [inext (.next ^datomic.iter.Iter iter)]
                (when (and inext (^clojure.lang.IFn p (.get ^datomic.iter.Iter inext)))
                  (if (identical? inext iter) this (take-while p inext)))))
            (get [this] (.get ^datomic.iter.Iter iter)))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.iter" "drop-while")
    {:tag datomic.iter.Iter, :arglists (clojure.core/list ['p 'iter]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.iter" "drop-while")
    (fn drop_while
      ([p iter]
        (loop [iter iter]
          (when iter
            (if (^clojure.lang.IFn p (.get ^datomic.iter.Iter iter))
              (recur (.next ^datomic.iter.Iter iter))
              iter))))))
  (defn least-index
    (^long [cmp values]
      (let [v1 (aget ^"[Ljava.lang.Object;" values (int 0))
            v2 (aget ^"[Ljava.lang.Object;" values (int 1))]
        (if (< (.compare ^java.util.Comparator cmp v1 v2) 0) 0 1))))
  (reset-meta!
    #'least-index
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'cmp {:tag 'Comparator}) (.withMeta 'values {:tag 'objects})]
           {:tag 'long})),
       :column (int 1)}
      :name
      'least-index
      :ns
      *ns*))
  (deftype
    MergeIter
    [cmp iters values ^{:tag long, :unsynchronized-mutable true} i]
    datomic.iter.Iter
    (next
      [this]
      (let [nxt (.next (aget ^"[Ljava.lang.Object;" iters (int i)))]
        (if nxt
          (do
            (aset ^"[Ljava.lang.Object;" iters (int i) nxt)
            (aset ^"[Ljava.lang.Object;" values (int i) (.get ^datomic.iter.Iter nxt))
            (set! i (long (least-index cmp values)))
            this)
          (aget ^"[Ljava.lang.Object;" iters (int (if (= i 0) 1 0))))))
    (get [this] (aget ^"[Ljava.lang.Object;" values (int i))))
  (clojure.core/import 'datomic.iter.MergeIter)
  (defn ->MergeIter
    ([cmp iters values i] (datomic.iter.MergeIter. cmp iters values (long ^java.lang.Number i))))
  (reset-meta!
    #'->MergeIter
    (assoc
      {:arglists (clojure.core/list ['cmp 'iters 'values 'i]), :column (int 1)}
      :name
      '->MergeIter
      :ns
      *ns*))
  (deftype
    ReversedIter
    [^{:unsynchronized-mutable true} iter]
    datomic.iter.Iter
    (next
      [this]
      (let [temp__5825__auto__ (.prev ^datomic.iter.Iter iter)]
        (when temp__5825__auto__ (let [it temp__5825__auto__] (set! iter it) this))))
    (get [this] (.get ^datomic.iter.Iter iter)))
  (clojure.core/import 'datomic.iter.ReversedIter)
  (defn ->ReversedIter ([iter] (datomic.iter.ReversedIter. iter)))
  (reset-meta!
    #'->ReversedIter
    (assoc
      {:arglists (clojure.core/list ['iter]), :column (int 1)}
      :name
      '->ReversedIter
      :ns
      *ns*))
  (defn reversed-iter ([iter] (when iter (datomic.iter.ReversedIter. iter))))
  (reset-meta!
    #'reversed-iter
    (assoc
      {:arglists (clojure.core/list ['iter]),
       :doc "Returns a cursor whose next operation follows prev on iter, enabling reverse consumption from a seek position.",
       :column (int 1)}
      :name
      'reversed-iter
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.iter" "merge-iters")
    {:tag datomic.iter.Iter,
     :arglists
     (clojure.core/list
       [(.withMeta 'cmp {:tag 'Comparator})
        (.withMeta 'iter1 {:tag 'Iter})
        (.withMeta 'iter2 {:tag 'Iter})]
       ['cmp 'iter1 'iter2 'iter3]
       ['cmp 'iter1 'iter2 'iter3 'iter4]
       ['cmp 'iter1 'iter2 'iter3 'iter4 'iter5]),
     :doc
     "Lazily merges two to five sorted cursors according to cmp. The returned cursor always exposes the least current value and advances only the source that supplied it.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.iter" "merge-iters")
    (fn merge_iters
      ([cmp iter1 iter2 iter3 iter4 iter5]
        (merge-iters cmp iter1 (merge-iters cmp iter2 iter3 iter4 iter5)))
      ([cmp iter1 iter2 iter3 iter4] (merge-iters cmp iter1 (merge-iters cmp iter2 iter3 iter4)))
      ([cmp iter1 iter2 iter3] (merge-iters cmp iter1 (merge-iters cmp iter2 iter3)))
      ([cmp iter1 iter2]
        (if (and iter1 iter2)
          (let [arr (object-array 2) values (object-array 2)]
            (aset ^"[Ljava.lang.Object;" arr (int 0) iter1)
            (aset ^"[Ljava.lang.Object;" arr (int 1) iter2)
            (aset ^"[Ljava.lang.Object;" values (int 0) (.get ^datomic.iter.Iter iter1))
            (aset ^"[Ljava.lang.Object;" values (int 1) (.get ^datomic.iter.Iter iter2))
            (datomic.iter.MergeIter. cmp arr values (long (least-index cmp values))))
          (or iter1 iter2)))))
  (defn iget ([iter] (and iter (.get ^datomic.iter.Iter iter))))
  (reset-meta!
    #'iget
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'iter {:tag 'Iter})]), :column (int 1)}
      :name
      'iget
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.iter" "inext")
    {:tag datomic.iter.Iter,
     :arglists (clojure.core/list [(.withMeta 'iter {:tag 'Iter})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.iter" "inext")
    (fn inext ([iter] (and iter (.next ^datomic.iter.Iter iter)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.iter" "iprev")
    {:tag datomic.iter.Iter,
     :arglists (clojure.core/list [(.withMeta 'iter {:tag 'Iter})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.iter" "iprev")
    (fn iprev ([iter] (and iter (.prev ^datomic.iter.Iter iter))))))

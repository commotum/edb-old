;; ATOMIC-NOTE [scope] Shared-collection pilot: nodes, insertion and cursors, not benchmarks.
;; Unannotated baseline: cd7192e63d883a4a34aa7de4d5bcd17e6edb692d. Original forms are retained.
(do
  (clojure.core/in-ns 'datomic.btset)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.btset)
    {:doc
     "In-memory sorted set used for recent datoms. The balanced tree supports ordered insertion, forward seek, reverse seek, and bidirectional iteration. Memory indexes use these operations to merge recent transactions with durable index tiers while preserving each index's datom order."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['comp 'compare])
      (clojure.core/use ['datomic.common :only ['compare]])
      (clojure.core/require
        ['datomic.iter :as 'iter :refer (clojure.core/list 'iget 'inext 'iprev)])
      (clojure.core/import 'datomic.iter.Iter)))
  (when-not (.equals 'datomic.btset 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.btset))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['comp 'compare])
        (clojure.core/use ['datomic.common :only ['compare]])
        (clojure.core/require
          ['datomic.iter :as 'iter :refer (clojure.core/list 'iget 'inext 'iprev)])
        (clojure.core/import 'datomic.iter.Iter))))
  (set! *warn-on-reflection* true)
  (set! *unchecked-math* true)
  (defn comp
    (^long [cmp x y]
      (if cmp (.compare ^java.util.Comparator cmp x y) (.compareTo ^java.lang.Comparable x y))))
  (reset-meta!
    #'comp
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'cmp {:tag 'java.util.Comparator}) 'x 'y] {:tag 'long})),
       :column (int 1)}
      :name
      'comp
      :ns
      *ns*))
  (definterface
    IBTSetBranch
    (^long count [])
    (^java.lang.Object childAt [^long arg0])
    (^java.lang.Object upsert [^long arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.btset.IBTSetBranch)
  (definterface IBTSetIterLink (^long offset []) (^void incOffset []) (^void decOffset []))
  (clojure.core/import 'datomic.btset.IBTSetIterLink)
  (deftype
    BTSetIterLink
    [branch ^{:tag long, :unsynchronized-mutable true} offset parent]
    datomic.btset.IBTSetIterLink
    (^void decOffset [this] (do (set! offset (long (dec offset))) (long offset) nil))
    (^void incOffset [this] (do (set! offset (long (inc offset))) (long offset) nil))
    (^long offset [this] offset))
  (clojure.core/import 'datomic.btset.BTSetIterLink)
  (defn ->BTSetIterLink
    ([branch offset parent]
      (datomic.btset.BTSetIterLink. branch (unchecked-long ^java.lang.Number offset) parent)))
  (reset-meta!
    #'->BTSetIterLink
    (assoc
      {:arglists (clojure.core/list ['branch 'offset 'parent]), :column (int 1)}
      :name
      '->BTSetIterLink
      :ns
      *ns*))
  (definterface
    IBTSetNode
    (^datomic.iter.Iter seek [^datomic.btset.IBTSetIterLink arg0])
    (^datomic.iter.Iter seek [^java.lang.Object arg0 ^datomic.btset.IBTSetIterLink arg1])
    (^datomic.iter.Iter rseek [^datomic.btset.IBTSetIterLink arg0])
    (^java.lang.Object conjoin [^java.lang.Object arg0])
    (^long compare [^java.lang.Object arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.btset.IBTSetNode)
  (deftype BTSetSplit [left k right])
  (clojure.core/import 'datomic.btset.BTSetSplit)
  (defn ->BTSetSplit ([left k right] (datomic.btset.BTSetSplit. left k right)))
  (reset-meta!
    #'->BTSetSplit
    (assoc
      {:arglists (clojure.core/list ['left 'k 'right]), :column (int 1)}
      :name
      '->BTSetSplit
      :ns
      *ns*))
  (definterface IBTSetLeaf (^long count []) (^java.lang.Object keyAt [^long arg0]))
  (clojure.core/import 'datomic.btset.IBTSetLeaf)
  ;; ATOMIC-NOTE [observed] Iteration mutates this cursor and its BTSetIterLink
  ;; offsets, while retaining immutable branches/leaves. next/prev walk the saved
  ;; path at leaf boundaries. [inferred] Cursor-local mutation avoids rebuilding
  ;; the set or a persistent cursor path for every returned datom.
  (deftype
    BTSetIter
    [^{:unsynchronized-mutable true} branches
     ^{:unsynchronized-mutable true} leaf
     ^{:tag long, :unsynchronized-mutable true} offset]
    datomic.iter.Iter
    (get [this] (.keyAt ^datomic.btset.IBTSetLeaf leaf (long offset)))
    (prev
      [this]
      (if (> offset 0)
        (do (set! offset (long (dec offset))) this)
        (let [bpath (loop [link branches]
                      (when link
                        (let [b (.-branch ^datomic.btset.BTSetIterLink link)]
                          (if (> (.offset ^datomic.btset.BTSetIterLink link) 0)
                            link
                            (recur (.-parent ^datomic.btset.BTSetIterLink link))))))]
          (when bpath
            (.decOffset ^datomic.btset.BTSetIterLink bpath)
            (loop [bpath bpath]
              (let [node (.childAt
                           (.-branch ^datomic.btset.BTSetIterLink bpath)
                           (long (.offset ^datomic.btset.BTSetIterLink bpath)))]
                (if (instance? datomic.btset.IBTSetBranch node)
                  (recur
                    (datomic.btset.BTSetIterLink.
                      node
                      (long (dec (.count ^datomic.btset.IBTSetBranch node)))
                      bpath))
                  (do
                    (set! branches bpath)
                    (set! leaf node)
                    (set! offset (long (dec (.count ^datomic.btset.IBTSetLeaf leaf))))
                    nil))))
            this))))
    (next
      [this]
      (if (< (inc offset) (.count ^datomic.btset.IBTSetLeaf leaf))
        (do (set! offset (long (inc offset))) this)
        (let [bpath (loop [link branches]
                      (when link
                        (let [b (.-branch ^datomic.btset.BTSetIterLink link)]
                          (if (<
                                (inc (.offset ^datomic.btset.BTSetIterLink link))
                                (.count ^datomic.btset.IBTSetBranch b))
                            link
                            (recur (.-parent ^datomic.btset.BTSetIterLink link))))))]
          (when bpath
            (.incOffset ^datomic.btset.BTSetIterLink bpath)
            (loop [bpath bpath]
              (let [node (.childAt
                           (.-branch ^datomic.btset.BTSetIterLink bpath)
                           (long (.offset ^datomic.btset.BTSetIterLink bpath)))]
                (if (instance? datomic.btset.IBTSetBranch node)
                  (recur (datomic.btset.BTSetIterLink. node 0 bpath))
                  (do (set! branches bpath) (set! leaf node) (set! offset (long 0)) nil))))
            this)))))
  (clojure.core/import 'datomic.btset.BTSetIter)
  (defn ->BTSetIter
    ([branches leaf offset]
      (datomic.btset.BTSetIter. branches leaf (unchecked-long ^java.lang.Number offset))))
  (reset-meta!
    #'->BTSetIter
    (assoc
      {:arglists (clojure.core/list ['branches 'leaf 'offset]), :column (int 1)}
      :name
      '->BTSetIter
      :ns
      *ns*))
  (def BT_BRANCH_SIZE 16)
  (reset-meta!
    #'BT_BRANCH_SIZE
    (assoc {:const true, :column (int 1)} :name 'BT_BRANCH_SIZE :ns *ns*))
  (def BT_LEAF_SIZE 16)
  (reset-meta! #'BT_LEAF_SIZE (assoc {:const true, :column (int 1)} :name 'BT_LEAF_SIZE :ns *ns*))
  ;; ATOMIC-NOTE [observed] conjoin descends one child; upsert clones only the
  ;; changed ancestor array and shares untouched children, or propagates a split.
  ;; An identical child returns this node unchanged. The width 16 bounds nks slots
  ;; (children alternate with separators), not sixteen child pointers.
  ;; [inferred] This preserves old roots with update work bounded by tree height.
  (deftype
    BTSetBranch
    [cmp nks]
    datomic.btset.IBTSetNode
    datomic.btset.IBTSetBranch
    (^datomic.iter.Iter rseek
      [this ^datomic.btset.IBTSetIterLink path]
      (.rseek
        (aget ^"[Ljava.lang.Object;" nks (int (dec (alength ^"[Ljava.lang.Object;" nks))))
        (datomic.btset.BTSetIterLink. this (long (dec (.count this))) path)))
    (^datomic.iter.Iter seek
      [this k ^datomic.btset.IBTSetIterLink path]
      (let [split (* 2 (quot (alength ^"[Ljava.lang.Object;" nks) 4))]
        (loop [pos (if (< (.compare this k (aget ^"[Ljava.lang.Object;" nks (int (inc split)))) 0)
                     0
                     split)]
          (if (or
                (= (long pos) (long (dec (alength ^"[Ljava.lang.Object;" nks))))
                (neg? (.compare this k (aget ^"[Ljava.lang.Object;" nks (int (inc pos))))))
            (.seek
              (aget ^"[Ljava.lang.Object;" nks (int pos))
              k
              (datomic.btset.BTSetIterLink. this (long (quot pos 2)) path))
            (recur (+ pos 2))))))
    (^datomic.iter.Iter seek
      [this ^datomic.btset.IBTSetIterLink path]
      (.seek (aget ^"[Ljava.lang.Object;" nks 0) (datomic.btset.BTSetIterLink. this 0 path)))
    (conjoin
      [this k]
      (let [split (* 2 (quot (alength ^"[Ljava.lang.Object;" nks) 4))]
        (loop [pos (if (< (.compare this k (aget ^"[Ljava.lang.Object;" nks (int (inc split)))) 0)
                     0
                     split)]
          (if (or
                (= (long pos) (long (dec (alength ^"[Ljava.lang.Object;" nks))))
                (neg? (.compare this k (aget ^"[Ljava.lang.Object;" nks (int (inc pos))))))
            (.upsert this (long pos) (.conjoin (aget ^"[Ljava.lang.Object;" nks (int pos)) k))
            (recur (+ 2 pos))))))
    (^long compare
      [this x y]
      (if cmp (.compare ^java.util.Comparator cmp x y) (.compareTo ^java.lang.Comparable x y)))
    (upsert
      [this ^long pos c]
      (cond
        (identical? c (aget ^"[Ljava.lang.Object;" nks (int pos))) this
        (instance? datomic.btset.BTSetSplit c) (let [half_branch (quot 16 2)
                                                     c c
                                                     new_nks (object-array
                                                               (long
                                                                 (+
                                                                   2
                                                                   (alength
                                                                     ^"[Ljava.lang.Object;" nks))))]
                                                 (java.lang.System/arraycopy
                                                   nks
                                                   (unchecked-int 0)
                                                   new_nks
                                                   (unchecked-int 0)
                                                   (unchecked-int pos))
                                                 (aset
                                                   ^"[Ljava.lang.Object;" new_nks
                                                   (int pos)
                                                   (.-left ^datomic.btset.BTSetSplit c))
                                                 (aset
                                                   ^"[Ljava.lang.Object;" new_nks
                                                   (int (inc pos))
                                                   (.-k ^datomic.btset.BTSetSplit c))
                                                 (aset
                                                   ^"[Ljava.lang.Object;" new_nks
                                                   (int (+ 2 pos))
                                                   (.-right ^datomic.btset.BTSetSplit c))
                                                 (java.lang.System/arraycopy
                                                   nks
                                                   (unchecked-int (inc pos))
                                                   new_nks
                                                   (unchecked-int (+ 3 pos))
                                                   (unchecked-int
                                                     (dec
                                                       (-
                                                         (alength ^"[Ljava.lang.Object;" nks)
                                                         pos))))
                                                 (if (<=
                                                       (alength ^"[Ljava.lang.Object;" new_nks)
                                                       16)
                                                   (BTSetBranch. cmp new_nks)
                                                   (let [anks (object-array
                                                                (long (inc half_branch)))
                                                         bnks (object-array
                                                                (long (dec half_branch)))]
                                                     (java.lang.System/arraycopy
                                                       new_nks
                                                       (unchecked-int 0)
                                                       anks
                                                       (unchecked-int 0)
                                                       (int (alength ^"[Ljava.lang.Object;" anks)))
                                                     (java.lang.System/arraycopy
                                                       new_nks
                                                       (unchecked-int (+ 2 (quot 16 2)))
                                                       bnks
                                                       (unchecked-int 0)
                                                       (int (alength ^"[Ljava.lang.Object;" bnks)))
                                                     (datomic.btset.BTSetSplit.
                                                       (BTSetBranch. cmp anks)
                                                       (aget
                                                         ^"[Ljava.lang.Object;" new_nks
                                                         (int (inc half_branch)))
                                                       (BTSetBranch. cmp bnks)))))
        :else (do
                (let [new_nks (aclone ^"[Ljava.lang.Object;" nks)]
                  (aset ^"[Ljava.lang.Object;" new_nks (int pos) c)
                  (BTSetBranch. cmp new_nks)))))
    (childAt [this ^long i] (aget ^"[Ljava.lang.Object;" nks (int (* 2 i))))
    (^long count [this] (inc (quot (alength ^"[Ljava.lang.Object;" nks) 2))))
  (clojure.core/import 'datomic.btset.BTSetBranch)
  (defn ->BTSetBranch ([cmp nks] (datomic.btset.BTSetBranch. cmp nks)))
  (reset-meta!
    #'->BTSetBranch
    (assoc
      {:arglists (clojure.core/list ['cmp 'nks]), :column (int 1)}
      :name
      '->BTSetBranch
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed] Comparator-equal insertion returns this leaf; a new
  ;; value copies its small sorted array and may split it. Appending beyond a full
  ;; leaf reuses that leaf as the left split. Existing leaf arrays are never edited.
  (deftype
    BTSetLeaf
    [^long cnt cmp ks]
    datomic.btset.IBTSetLeaf
    datomic.btset.IBTSetNode
    (^datomic.iter.Iter rseek
      [this ^datomic.btset.IBTSetIterLink path]
      (datomic.btset.BTSetIter. path this (long (dec cnt))))
    (^datomic.iter.Iter seek
      [this k ^datomic.btset.IBTSetIterLink path]
      (let [split (quot cnt 2)]
        (loop [i (if (< (.compare this k (aget ^"[Ljava.lang.Object;" ks (int split))) 0) 0 split)]
          (cond
            (or
              (= (long i) (long cnt))
              (<= (.compare this k (aget ^"[Ljava.lang.Object;" ks (int i))) 0)) (let
                                                                                   [ret
                                                                                    (datomic.btset.BTSetIter.
                                                                                      path
                                                                                      this
                                                                                      (long i))]
                                                                                   (if
                                                                                     (= i cnt)
                                                                                     (.next
                                                                                       ^datomic.btset.BTSetIter ret)
                                                                                     ret))
            :else (do (recur (inc i)))))))
    (^datomic.iter.Iter seek
      [this ^datomic.btset.IBTSetIterLink path]
      (datomic.btset.BTSetIter. path this 0))
    (conjoin
      [this k]
      (let [half_leaf (quot 16 2) split (quot cnt 2)]
        (loop [i (if (< (.compare this k (aget ^"[Ljava.lang.Object;" ks (int split))) 0) 0 split)]
          (cond
            (or
              (= (long i) (long cnt))
              (neg? (.compare this k (aget ^"[Ljava.lang.Object;" ks (int i))))) (if
                                                                                   (=
                                                                                     (long i)
                                                                                     (long cnt)
                                                                                     16)
                                                                                   (datomic.btset.BTSetSplit.
                                                                                     this
                                                                                     k
                                                                                     (BTSetLeaf.
                                                                                       1
                                                                                       cmp
                                                                                       (object-array
                                                                                         [k])))
                                                                                   (let
                                                                                     [new_ks
                                                                                      (object-array
                                                                                        (long
                                                                                          (inc
                                                                                            cnt)))]
                                                                                     (java.lang.System/arraycopy
                                                                                       ks
                                                                                       (unchecked-int
                                                                                         0)
                                                                                       new_ks
                                                                                       (unchecked-int
                                                                                         0)
                                                                                       (unchecked-int
                                                                                         i))
                                                                                     (aset
                                                                                       ^"[Ljava.lang.Object;" new_ks
                                                                                       (int i)
                                                                                       k)
                                                                                     (java.lang.System/arraycopy
                                                                                       ks
                                                                                       (unchecked-int
                                                                                         i)
                                                                                       new_ks
                                                                                       (unchecked-int
                                                                                         (inc i))
                                                                                       (unchecked-int
                                                                                         (-
                                                                                           cnt
                                                                                           i)))
                                                                                     (if
                                                                                       (< cnt 16)
                                                                                       (BTSetLeaf.
                                                                                         (long
                                                                                           (inc
                                                                                             cnt))
                                                                                         cmp
                                                                                         new_ks)
                                                                                       (let
                                                                                         [aks
                                                                                          (object-array
                                                                                            (long
                                                                                              (inc
                                                                                                half_leaf)))
                                                                                          bks
                                                                                          (object-array
                                                                                            (long
                                                                                              half_leaf))]
                                                                                         (java.lang.System/arraycopy
                                                                                           new_ks
                                                                                           (unchecked-int
                                                                                             0)
                                                                                           aks
                                                                                           (unchecked-int
                                                                                             0)
                                                                                           (unchecked-int
                                                                                             (inc
                                                                                               half_leaf)))
                                                                                         (java.lang.System/arraycopy
                                                                                           new_ks
                                                                                           (unchecked-int
                                                                                             (inc
                                                                                               half_leaf))
                                                                                           bks
                                                                                           (unchecked-int
                                                                                             0)
                                                                                           (unchecked-int
                                                                                             half_leaf))
                                                                                         (datomic.btset.BTSetSplit.
                                                                                           (BTSetLeaf.
                                                                                             (long
                                                                                               (inc
                                                                                                 half_leaf))
                                                                                             cmp
                                                                                             aks)
                                                                                           (aget
                                                                                             ^"[Ljava.lang.Object;" bks
                                                                                             0)
                                                                                           (BTSetLeaf.
                                                                                             (long
                                                                                               half_leaf)
                                                                                             cmp
                                                                                             bks))))))
            (= (.compare this (aget ^"[Ljava.lang.Object;" ks (int i)) k) 0) this
            :else (do (recur (inc i)))))))
    (^long compare
      [this x y]
      (if cmp (.compare ^java.util.Comparator cmp x y) (.compareTo ^java.lang.Comparable x y)))
    (keyAt [this ^long n] (aget ^"[Ljava.lang.Object;" ks (int n)))
    (^long count [this] cnt))
  (clojure.core/import 'datomic.btset.BTSetLeaf)
  (defn ->BTSetLeaf
    ([cnt cmp ks] (datomic.btset.BTSetLeaf. (unchecked-long ^java.lang.Number cnt) cmp ks)))
  (reset-meta!
    #'->BTSetLeaf
    (assoc
      {:arglists (clojure.core/list ['cnt 'cmp 'ks]), :column (int 1)}
      :name
      '->BTSetLeaf
      :ns
      *ns*))
  ;; IDataSet is the ordered-cursor contract shared by in-memory and durable
  ;; index tiers. A keyed seek positions at the lowest value greater than or
  ;; equal to the key; seekLast positions at the greatest value.
  ;; ATOMIC-NOTE [observed] db/seekEAVT and its sibling index methods seek each
  ;; memory/durable tier through IDataSet, then iter/merge-iters combines their
  ;; ordered cursors. [documented] Index Model / Efficient Accumulation describes
  ;; this sorted-view composition; consumers need not materialize and sort all tiers.
  (definterface
    IDataSet
    (^long longCount [])
    (^datomic.iter.Iter seek [])
    (^datomic.iter.Iter seek [^java.lang.Object arg0])
    (^datomic.iter.Iter seekLast []))
  (clojure.core/import 'datomic.btset.IDataSet)
  (definterface
    IBTSet
    (^datomic.iter.Iter rseek [])
    (^datomic.iter.Iter rseek [^java.lang.Object arg0]))
  (clojure.core/import 'datomic.btset.IBTSet)
  ;; ATOMIC-NOTE [observed] cons publishes a new root/count after path copying;
  ;; comparator duplicates preserve this set. rseek adjusts a lower-bound cursor
  ;; to its predecessor when necessary. disjoin is unsupported: this is the recent
  ;; datom accumulation mechanism, not a complete general-purpose set API.
  (deftype
    BTSet
    [cmp ^long cnt root]
    datomic.btset.IBTSet
    clojure.lang.IPersistentCollection
    clojure.lang.IPersistentSet
    clojure.lang.Reversible
    clojure.lang.Counted
    datomic.btset.IDataSet
    clojure.lang.Seqable
    (^datomic.iter.Iter rseek
      [this k]
      (let [ret (.seek this k)]
        (cond
          (nil? ret) (.rseek this)
          (< (comp cmp k (.get ^datomic.iter.Iter ret)) 0) (.prev ^datomic.iter.Iter ret)
          :else (do ret))))
    (^datomic.iter.Iter rseek [this] (when root (.rseek ^datomic.btset.IBTSetNode root nil)))
    (^datomic.iter.Iter seekLast [this] (.rseek this))
    (^datomic.iter.Iter seek [this k] (when root (.seek ^datomic.btset.IBTSetNode root k nil)))
    (^datomic.iter.Iter seek [this] (when root (.seek ^datomic.btset.IBTSetNode root nil)))
    (^long longCount [this] cnt)
    (^clojure.lang.ISeq rseq [this] (iter/iter-rseq (.rseek this)))
    (^clojure.lang.ISeq seq [this] (iter/iter-seq (.seek this)))
    (^int count [this] (int cnt))
    (get
      [this k]
      (let [temp__5825__auto__ (.seek this k)]
        (when temp__5825__auto__
          (let [i temp__5825__auto__]
            (when (= k (.get ^datomic.iter.Iter i)) (.get ^datomic.iter.Iter i))))))
    (^boolean contains
      [this k]
      (boolean
        (let [temp__5825__auto__ (.seek this k)]
          (when temp__5825__auto__
            (let [i temp__5825__auto__] (= k (.get ^datomic.iter.Iter i)))))))
    (^clojure.lang.IPersistentSet disjoin
      [this k]
      (throw (java.lang.UnsupportedOperationException.)))
    (^boolean equiv [this x] (.booleanValue false))
    (^clojure.lang.IPersistentCollection cons
      [this k]
      (if (nil? root)
        (BTSet. cmp 1 (datomic.btset.BTSetLeaf. 1 cmp (object-array [k])))
        (let [new_root (.conjoin ^datomic.btset.IBTSetNode root k)]
          (cond
            (identical? root new_root) this
            (instance? datomic.btset.BTSetSplit new_root) (let [split new_root]
                                                            (BTSet.
                                                              cmp
                                                              (long (inc cnt))
                                                              (datomic.btset.BTSetBranch.
                                                                cmp
                                                                (object-array
                                                                  [(.-left
                                                                     ^datomic.btset.BTSetSplit split)
                                                                   (.-k
                                                                     ^datomic.btset.BTSetSplit split)
                                                                   (.-right
                                                                     ^datomic.btset.BTSetSplit split)]))))
            :else (do (BTSet. cmp (long (inc cnt)) new_root))))))
    (^clojure.lang.IPersistentCollection empty [this] (BTSet. cmp 0 nil)))
  (clojure.core/import 'datomic.btset.BTSet)
  (defn ->BTSet
    ([cmp cnt root] (datomic.btset.BTSet. cmp (unchecked-long ^java.lang.Number cnt) root)))
  (reset-meta!
    #'->BTSet
    (assoc
      {:arglists (clojure.core/list ['cmp 'cnt 'root]), :column (int 1)}
      :name
      '->BTSet
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.btset" "btset")
    {:tag datomic.btset.BTSet,
     :arglists (clojure.core/list [] ['cmp]),
     :doc "Returns an empty immutable balanced-tree set, ordered by cmp or by the values' natural order.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.btset" "btset")
    (fn btset ([cmp] (datomic.btset.BTSet. cmp 0 nil)) ([] (btset nil))))
  (.setMeta
    (clojure.lang.RT/var "datomic.btset" "seek")
    {:tag datomic.iter.Iter,
     :arglists
     (clojure.core/list [(.withMeta 'ds {:tag 'IDataSet})] [(.withMeta 'ds {:tag 'IDataSet}) 'k]),
     :doc
     "Returns a forward cursor at the first value in ds, or at the lowest value greater than or equal to k. Returns nil when no such value exists.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.btset" "seek")
    (fn seek
      ([ds k] (and ds (.seek ^datomic.btset.IDataSet ds k)))
      ([ds] (and ds (.seek ^datomic.btset.IDataSet ds)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.btset" "seek-last")
    {:tag datomic.iter.Iter,
     :arglists (clojure.core/list [(.withMeta 'ds {:tag 'IDataSet})]),
     :doc "Returns a cursor positioned at the greatest value in ds, or nil when ds is empty.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.btset" "seek-last")
    (fn seek_last ([ds] (and ds (.seekLast ^datomic.btset.IDataSet ds)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.btset" "rseek")
    {:tag datomic.iter.Iter,
     :arglists
     (clojure.core/list [(.withMeta 'bt {:tag 'BTSet})] [(.withMeta 'bt {:tag 'BTSet}) 'k]),
     :doc
     "Returns a reverse cursor at the greatest value in bt, or at the greatest value less than or equal to k. Returns nil when no such value exists.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.btset" "rseek")
    (fn rseek ([bt k] (.rseek ^datomic.btset.BTSet bt k)) ([bt] (.rseek ^datomic.btset.BTSet bt))))
  (defn bench
    ([x]
      (let [_ (println "sorted-set")
            ss (let [start__6228__auto__ (java.lang.System/nanoTime)
                     ret__6229__auto__ (let [r (java.util.Random. 4242)]
                                         (loop [bs (sorted-set) i 0]
                                           (if (< i x)
                                             (let [n (.nextLong ^java.util.Random r)]
                                               (recur (conj bs (long n)) (inc i)))
                                             bs)))]
                 (prn
                   (str
                     "Elapsed time: "
                     (java.lang.Double/valueOf
                       (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                     " msecs"))
                 ret__6229__auto__)
            _ (println "j.u. tree set")
            ts (let [start__6228__auto__ (java.lang.System/nanoTime)
                     ret__6229__auto__ (let [r (java.util.Random. 4242) ts (java.util.TreeSet.)]
                                         (loop [i 0]
                                           (if (< i x)
                                             (let [n (.nextLong ^java.util.Random r)]
                                               (.add ^java.util.TreeSet ts (long n))
                                               (recur (inc i)))
                                             ts)))]
                 (prn
                   (str
                     "Elapsed time: "
                     (java.lang.Double/valueOf
                       (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                     " msecs"))
                 ret__6229__auto__)
            _ (println "bt-set")
            bt (let [start__6228__auto__ (java.lang.System/nanoTime)
                     ret__6229__auto__ (let [r (java.util.Random. 4242)]
                                         (loop [bt (btset) i 0]
                                           (if (< i x)
                                             (let [n (.nextLong ^java.util.Random r)]
                                               (recur (conj bt (long n)) (inc i)))
                                             bt)))]
                 (prn
                   (str
                     "Elapsed time: "
                     (java.lang.Double/valueOf
                       (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                     " msecs"))
                 ret__6229__auto__)]
        (when-not (= (count bt) (count ss))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list
                    '=
                    (clojure.core/list 'count 'bt)
                    (clojure.core/list 'count 'ss)))))))
        (when-not (every?
                    (fn fn__11170
                      ([p1__11161#]
                        (let [x (seek bt p1__11161#)]
                          (and x (= (.get ^datomic.iter.Iter x) p1__11161#)))))
                    ss)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list
                    'every?
                    (clojure.core/list
                      'fn*
                      ['p1__11161#]
                      (clojure.core/list
                        'let
                        ['x (clojure.core/list 'seek 'bt 'p1__11161#)]
                        (clojure.core/list
                          'and
                          'x
                          (clojure.core/list '= (clojure.core/list '.get 'x) 'p1__11161#))))
                    'ss))))))
        (when-not (every?
                    (fn fn__11173
                      ([p1__11162#]
                        (let [x (rseek bt p1__11162#)]
                          (and x (= (.get ^datomic.iter.Iter x) p1__11162#)))))
                    ss)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list
                    'every?
                    (clojure.core/list
                      'fn*
                      ['p1__11162#]
                      (clojure.core/list
                        'let
                        ['x (clojure.core/list 'rseek 'bt 'p1__11162#)]
                        (clojure.core/list
                          'and
                          'x
                          (clojure.core/list '= (clojure.core/list '.get 'x) 'p1__11162#))))
                    'ss))))))
        (when-not (every? (fn fn__11176 ([p1__11163#] (contains? bt p1__11163#))) ss)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list
                    'every?
                    (clojure.core/list
                      'fn*
                      ['p1__11163#]
                      (clojure.core/list 'contains? 'bt 'p1__11163#))
                    'ss))))))
        (loop [bts (seek bt) sss (seq ss)]
          (when sss
            (when-not (= (iter/iget bts) (first sss))
              (throw
                (java.lang.AssertionError.
                  (str
                    "Assert failed: "
                    (pr-str
                      (clojure.core/list
                        '=
                        (clojure.core/list 'iget 'bts)
                        (clojure.core/list 'first 'sss)))))))
            (recur (iter/inext bts) (next sss))))
        (loop [bts (rseek bt) sss (rseq ss)]
          (when sss
            (when-not (= (iter/iget bts) (first sss))
              (throw
                (java.lang.AssertionError.
                  (str
                    "Assert failed: "
                    (pr-str
                      (clojure.core/list
                        '=
                        (clojure.core/list 'iget 'bts)
                        (clojure.core/list 'first 'sss)))))))
            (recur (iter/iprev bts) (next sss))))
        (println "sorted-set")
        (dotimes [_ 20]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (loop [s (seq ss) ret 0] (if s (recur (next s) (first s)) ret))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (dotimes [_ 5]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (loop [s (seq ss) ret 0]
                                    (if s (recur (next s) (subseq ss < (first s))) ret))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (println "bt-set")
        (dotimes [_ 20]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (loop [s (seek bt) ret 0]
                                    (if s (recur (iter/inext s) (iter/iget s)) ret))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (dotimes [_ 5]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (loop [s (seek bt) ret 0]
                                    (if s (recur (iter/inext s) (seek bt (iter/iget s))) ret))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (println "j.u. tree set")
        (dotimes [_ 20]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (loop [s (.iterator ^java.util.TreeSet ts) ret 0]
                                    (if (.hasNext ^java.util.Iterator s)
                                      (recur s (.next ^java.util.Iterator s))
                                      ret))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (dotimes [_ 5]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (loop [s (.iterator ^java.util.TreeSet ts) ret 0]
                                    (if (.hasNext ^java.util.Iterator s)
                                      (recur
                                        s
                                        (.iterator
                                          (.tailSet
                                            ^java.util.TreeSet ts
                                            (.next ^java.util.Iterator s))))
                                      ret))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (java.lang.Integer/valueOf (int (count ss))))))
  (reset-meta!
    #'bench
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'bench
      :ns
      *ns*)))

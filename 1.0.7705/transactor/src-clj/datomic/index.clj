(do
  (clojure.core/in-ns (.withMeta 'datomic.index {:author "Rich Hickey"}))
  (.resetMeta
    (clojure.lang.Namespace/find (.withMeta 'datomic.index {:author "Rich Hickey"}))
    {:doc "Index on cluster", :author "Rich Hickey"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['bounded-count 'compare])
      (clojure.core/use ['datomic.common :only ['compare]])
      (clojure.core/require
        ['clojure.core.async :as 'a :refer (clojure.core/list '<!! '>!!)]
        ['clojure.edn :as 'edn]
        ['datomic.btset :as 'btset]
        ['datomic.db :as 'db]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.cache :as 'cache]
        ['datomic.io :as 'io]
        ['datomic.measure.io-stats :as 'io-stats]
        ['datomic.measure.io-trace :as 'io-trace]
        ['datomic.cluster :as 'cluster]
        ['datomic.error :as 'error]
        ['datomic.external-sort-datoms :as 'esd]
        ['datomic.math :as 'math]
        ['datomic.monitor :as 'monitor]
        ['datomic.memory-size :as 'mem]
        ['datomic.iter :as 'iter]
        ['datomic.process.events :as 'events]
        ['datomic.slf4j :as 'logger]
        ['datomic.fulltext :as 'fulltext]
        ['datomic.fressian :as 'fressian]
        ['datomic.excise :as 'x]
        ['datomic.process :as 'process]
        ['datomic.core2.thread :as 'thread])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'clojure.lang.ILookup)
      (clojure.core/import 'clojure.lang.Indexed)
      (clojure.core/import 'org.fressian.impl.BytesOutputStream)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'datomic.btset.IDataSet)
      (clojure.core/import 'datomic.db.IDatumImpl)
      (clojure.core/import 'datomic.db.Db)
      (clojure.core/import 'datomic.db.IndexSet)
      (clojure.core/import 'datomic.db.ProcessInpoint)
      (clojure.core/import 'datomic.db.IProcess)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'java.io.BufferedOutputStream)
      (clojure.core/import 'java.util.zip.GZIPOutputStream)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)))
  (when-not (.equals (.withMeta 'datomic.index {:author "Rich Hickey"}) 'clojure.core)
    (dosync
      (commute
        (deref #'clojure.core/*loaded-libs*)
        conj
        (.withMeta 'datomic.index {:author "Rich Hickey"})))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['bounded-count 'compare])
        (clojure.core/use ['datomic.common :only ['compare]])
        (clojure.core/require
          ['clojure.core.async :as 'a :refer (clojure.core/list '<!! '>!!)]
          ['clojure.edn :as 'edn]
          ['datomic.btset :as 'btset]
          ['datomic.db :as 'db]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.cache :as 'cache]
          ['datomic.io :as 'io]
          ['datomic.measure.io-stats :as 'io-stats]
          ['datomic.measure.io-trace :as 'io-trace]
          ['datomic.cluster :as 'cluster]
          ['datomic.error :as 'error]
          ['datomic.external-sort-datoms :as 'esd]
          ['datomic.math :as 'math]
          ['datomic.monitor :as 'monitor]
          ['datomic.memory-size :as 'mem]
          ['datomic.iter :as 'iter]
          ['datomic.process.events :as 'events]
          ['datomic.slf4j :as 'logger]
          ['datomic.fulltext :as 'fulltext]
          ['datomic.fressian :as 'fressian]
          ['datomic.excise :as 'x]
          ['datomic.process :as 'process]
          ['datomic.core2.thread :as 'thread])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'clojure.lang.ILookup)
        (clojure.core/import 'clojure.lang.Indexed)
        (clojure.core/import 'org.fressian.impl.BytesOutputStream)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'datomic.btset.IDataSet)
        (clojure.core/import 'datomic.db.IDatumImpl)
        (clojure.core/import 'datomic.db.Db)
        (clojure.core/import 'datomic.db.IndexSet)
        (clojure.core/import 'datomic.db.ProcessInpoint)
        (clojure.core/import 'datomic.db.IProcess)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'java.io.BufferedOutputStream)
        (clojure.core/import 'java.util.zip.GZIPOutputStream)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'org.fressian.handlers.WriteHandler)
        (clojure.core/import 'org.fressian.handlers.ReadHandler))))
  (set! *warn-on-reflection* true)
  (set! *unchecked-math* true)
  (defn deref-or-throw
    ([ref]
      (let [result (deref ref)]
        (when (instance? java.lang.Throwable result) (throw ^java.lang.Throwable result))
        result)))
  (reset-meta!
    #'deref-or-throw
    (assoc
      {:private true, :arglists (clojure.core/list ['ref]), :column (int 1)}
      :name
      'deref-or-throw
      :ns
      *ns*))
  (definterface
    ITransposeData
    (^boolean isAssertion [^int arg0])
    (^long getE [^int arg0])
    (^int getA [^int arg0])
    (^java.lang.Object getV [^int arg0])
    (^long getT [^int arg0])
    (^long getLongV [^int arg0])
    (^double getDoubleV [^int arg0])
    (^int getIntV [^int arg0])
    (^float getFloatV [^int arg0])
    (^boolean getBooleanV [^int arg0])
    (^java.lang.Object getEs [])
    (^java.lang.Object getAs []))
  (clojure.core/import 'datomic.index.ITransposeData)
  (deftype
    TransposedData
    [^int cnt eas vs ts ops]
    datomic.index.ITransposeData
    java.util.RandomAccess
    java.util.List
    (^java.util.List subList
      [this ^int from ^int to]
      (reify
        java.util.List
        (^int size [this] (int (- to from)))
        (get [this__reify ^int idx] (.get this (unchecked-int (+ from idx))))))
    (get
      [this ^int idx]
      (if (aget ^booleans ops idx)
        (db/asserting-datum
          (aget ^longs eas (bit-shift-left idx 1))
          (aget ^longs eas (inc (bit-shift-left idx 1)))
          (aget ^"[Ljava.lang.Object;" vs (int idx))
          (aget ^longs ts idx))
        (db/retracting-datum
          (aget ^longs eas (bit-shift-left idx 1))
          (aget ^longs eas (inc (bit-shift-left idx 1)))
          (aget ^"[Ljava.lang.Object;" vs (int idx))
          (aget ^longs ts idx))))
    (^java.util.Iterator iterator
      [this]
      (let [i (atom -1)]
        (reify
          java.util.Iterator
          (next [this__reify] (.get this (unchecked-int (swap! i inc))))
          (^boolean hasNext [this] (< (inc (deref i)) cnt)))))
    (^int size [this] cnt)
    (getAs
      [this]
      (let [as (int-array (long (quot (alength ^longs eas) 2)))]
        (dotimes [i (alength ^ints as)]
          (aset ^ints as (int i) (unchecked-int (aget ^longs eas (inc (bit-shift-left i 1))))))
        as))
    (getEs
      [this]
      (let [es (long-array (long (quot (alength ^longs eas) 2)))]
        (dotimes [i (alength ^longs es)]
          (aset ^longs es (int i) (long (aget ^longs eas (bit-shift-left i 1)))))
        es))
    (^boolean getBooleanV [this ^int idx] (aget (booleans vs) idx))
    (^float getFloatV [this ^int idx] (aget (floats vs) idx))
    (^int getIntV [this ^int idx] (aget (ints vs) idx))
    (^double getDoubleV [this ^int idx] (aget (doubles vs) idx))
    (^long getLongV [this ^int idx] (aget (longs vs) idx))
    (^long getT [this ^int idx] (aget ^longs ts idx))
    (getV [this ^int idx] (aget ^"[Ljava.lang.Object;" vs (int idx)))
    (^int getA [this ^int idx] (int (aget ^longs eas (inc (bit-shift-left idx 1)))))
    (^long getE [this ^int idx] (aget ^longs eas (bit-shift-left idx 1)))
    (^boolean isAssertion [this ^int idx] (aget ^booleans ops idx)))
  (clojure.core/import 'datomic.index.TransposedData)
  (defn ->TransposedData
    ([cnt eas vs ts ops]
      (datomic.index.TransposedData. (unchecked-int ^java.lang.Number cnt) eas vs ts ops)))
  (reset-meta!
    #'->TransposedData
    (assoc
      {:arglists (clojure.core/list ['cnt 'eas 'vs 'ts 'ops]), :column (int 1)}
      :name
      '->TransposedData
      :ns
      *ns*))
  (defn transposed-data
    ([es as vs ts ops]
      (let [eas (long-array (long (* 2 (alength ^longs es))))]
        (dotimes [i (alength ^longs es)]
          (aset ^longs eas (int (* 2 i)) (long (aget ^longs es i)))
          (aset ^longs eas (int (inc (* 2 i))) (long (aget ^ints as i))))
        (datomic.index.TransposedData. (int (count vs)) eas vs ts ops))))
  (reset-meta!
    #'transposed-data
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'es {:tag 'longs})
          (.withMeta 'as {:tag 'ints})
          'vs
          (.withMeta 'ts {:tag 'longs})
          (.withMeta 'ops {:tag 'booleans})]),
       :column (int 1)}
      :name
      'transposed-data
      :ns
      *ns*))
  (deftype RootNode [keydata dirids dirs])
  (clojure.core/import 'datomic.index.RootNode)
  (defn ->RootNode ([keydata dirids dirs] (datomic.index.RootNode. keydata dirids dirs)))
  (reset-meta!
    #'->RootNode
    (assoc
      {:arglists (clojure.core/list ['keydata 'dirids 'dirs]), :column (int 1)}
      :name
      '->RootNode
      :ns
      *ns*))
  (defn root-node
    ([keydata dirids]
      (datomic.index.RootNode.
        keydata
        dirids
        (object-array (java.lang.Integer/valueOf (int (alength ^"[Ljava.lang.Object;" dirids)))))))
  (reset-meta!
    #'root-node
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['keydata (.withMeta 'dirids {:tag 'objects})]
           {:tag 'datomic.index.RootNode})),
       :column (int 1)}
      :name
      'root-node
      :ns
      *ns*))
  (deftype DirNode [keydata segids offsets counts segs])
  (clojure.core/import 'datomic.index.DirNode)
  (defn ->DirNode
    ([keydata segids offsets counts segs]
      (datomic.index.DirNode. keydata segids offsets counts segs)))
  (reset-meta!
    #'->DirNode
    (assoc
      {:arglists (clojure.core/list ['keydata 'segids 'offsets 'counts 'segs]), :column (int 1)}
      :name
      '->DirNode
      :ns
      *ns*))
  (defn dir-node
    ([keydata segids offsets counts]
      (datomic.index.DirNode.
        keydata
        segids
        offsets
        counts
        (object-array (java.lang.Integer/valueOf (int (alength ^"[Ljava.lang.Object;" segids)))))))
  (reset-meta!
    #'dir-node
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['keydata (.withMeta 'segids {:tag 'objects}) 'offsets 'counts]
           {:tag 'datomic.index.DirNode})),
       :column (int 1)}
      :name
      'dir-node
      :ns
      *ns*))
  (defn get-dir-node
    ([root ridx lookup cache?]
      (let [k (aget (.-dirids ^datomic.index.RootNode root) (unchecked-int ridx))]
        (io-stats/inc! :dir)
        (io-trace/note! k :dir)
        (or
          (let [ref (aget (.-dirs ^datomic.index.RootNode root) (unchecked-int ridx))]
            (and ref (.get ^java.lang.ref.Reference ref)))
          (let [dir (common/getx
                      lookup
                      (aget (.-dirids ^datomic.index.RootNode root) (unchecked-int ridx)))]
            (let [and__5600__auto__ cache?]
              (when and__5600__auto__
                (aset
                  (.-dirs ^datomic.index.RootNode root)
                  (unchecked-int ridx)
                  (java.lang.ref.WeakReference. dir))))
            dir)))))
  (reset-meta!
    #'get-dir-node
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'root {:tag 'RootNode}) 'ridx 'lookup 'cache?]
           {:tag 'datomic.index.DirNode})),
       :column (int 1)}
      :name
      'get-dir-node
      :ns
      *ns*))
  (let [protocol_metadata__7454 {:column (int 1)}]
    (defprotocol
      ITreeIter
      (seg+item-seq
        [iter]
        "Returns a seq of {:segid ... :item ...} for items in tree,\nand their seg ids.  Does not cache segs.")
      (seg-seq
        [iter]
        "Returns a seq of key data corresponding to each segment in the tree,\n                   starting at iter. Loads the segs as it goes.")
      (dir-seq
        [iter]
        "Returns a seq of {:key key-datum :seg seg-uuid :count count} corresponding to each segment in the tree,\n                   starting at iter."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.index" "ITreeIter")
      (assoc (assoc protocol_metadata__7454 :doc nil) :name 'ITreeIter :ns *ns*))
    (let [protocol_signature__7455 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'seg+item-seq
                                        {:arglists (clojure.core/list ['iter])}),
                                      :arglists (clojure.core/list ['iter]),
                                      :doc
                                      "Returns a seq of {:segid ... :item ...} for items in tree,\nand their seg ids.  Does not cache segs."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.index" "ITreeIter"))
          protocol_method_name__7456 (with-meta
                                       (:name protocol_signature__7455)
                                       protocol_signature__7455)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.index" "seg+item-seq")
        (assoc protocol_signature__7455 :name protocol_method_name__7456 :ns *ns*)))
    (let [protocol_signature__7457 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'seg-seq {:arglists (clojure.core/list ['iter])}),
                                      :arglists (clojure.core/list ['iter]),
                                      :doc
                                      "Returns a seq of key data corresponding to each segment in the tree,\n                   starting at iter. Loads the segs as it goes."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.index" "ITreeIter"))
          protocol_method_name__7458 (with-meta
                                       (:name protocol_signature__7457)
                                       protocol_signature__7457)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.index" "seg-seq")
        (assoc protocol_signature__7457 :name protocol_method_name__7458 :ns *ns*)))
    (let [protocol_signature__7459 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'dir-seq {:arglists (clojure.core/list ['iter])}),
                                      :arglists (clojure.core/list ['iter]),
                                      :doc
                                      "Returns a seq of {:key key-datum :seg seg-uuid :count count} corresponding to each segment in the tree,\n                   starting at iter."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.index" "ITreeIter"))
          protocol_method_name__7460 (with-meta
                                       (:name protocol_signature__7459)
                                       protocol_signature__7459)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.index" "dir-seq")
        (assoc protocol_signature__7459 :name protocol_method_name__7460 :ns *ns*))))
  (let [protocol_metadata__7461 {:column (int 1)}]
    (defprotocol
      IIndex
      (seek-seg
        [idx k]
        "Returns the segid of seg that would contain k (but might not). Does not read seg."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.index" "IIndex")
      (assoc (assoc protocol_metadata__7461 :doc nil) :name 'IIndex :ns *ns*))
    (let [protocol_signature__7462 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'seek-seg
                                        {:arglists (clojure.core/list ['idx 'k])}),
                                      :arglists (clojure.core/list ['idx 'k]),
                                      :doc
                                      "Returns the segid of seg that would contain k (but might not). Does not read seg."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.index" "IIndex"))
          protocol_method_name__7463 (with-meta
                                       (:name protocol_signature__7462)
                                       protocol_signature__7462)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.index" "seek-seg")
        (assoc protocol_signature__7462 :name protocol_method_name__7463 :ns *ns*))))
  (deftype
    TreeIter
    [lookup
     root
     ^{:tag int, :unsynchronized-mutable true} ridx
     ^{:unsynchronized-mutable true} dir
     ^{:tag int, :unsynchronized-mutable true} didx
     ^{:unsynchronized-mutable true} seg
     ^{:tag int, :unsynchronized-mutable true} sidx]
    datomic.index.ITreeIter
    datomic.iter.Iter
    (prev
      [this]
      (cond
        (>= (dec sidx) (aget (ints (.-offsets ^datomic.index.DirNode dir)) didx)) (do
                                                                                    (set!
                                                                                      sidx
                                                                                      (int
                                                                                        (dec
                                                                                          sidx)))
                                                                                    this)
        (>= (dec didx) 0) (do
                            (set! didx (int (dec didx)))
                            (set!
                              seg
                              (common/getx
                                lookup
                                (aget (.-segids ^datomic.index.DirNode dir) (int didx))))
                            (set!
                              sidx
                              (int
                                (-> (ints (.-offsets ^datomic.index.DirNode dir))
                                 (aget didx)
                                 (+ (aget (ints (.-counts ^datomic.index.DirNode dir)) didx))
                                 (+ -1)
                                 (int))))
                            this)
        (>= (dec ridx) 0) (do
                            (set! ridx (int (dec ridx)))
                            (set!
                              dir
                              (datomic.index/get-dir-node
                                root
                                (java.lang.Integer/valueOf (int ridx))
                                lookup
                                false))
                            (set! didx (int (dec (count (.-segids ^datomic.index.DirNode dir)))))
                            (set!
                              seg
                              (common/getx
                                lookup
                                (aget (.-segids ^datomic.index.DirNode dir) (int didx))))
                            (set!
                              sidx
                              (int
                                (-> (ints (.-offsets ^datomic.index.DirNode dir))
                                 (aget didx)
                                 (+ (aget (ints (.-counts ^datomic.index.DirNode dir)) didx))
                                 (+ -1)
                                 (int))))
                            this)
        :else (do nil)))
    (next
      [this]
      (cond
        (<
          (inc sidx)
          (+
            (aget (ints (.-offsets ^datomic.index.DirNode dir)) didx)
            (aget (ints (.-counts ^datomic.index.DirNode dir)) didx))) (do
                                                                         (set!
                                                                           sidx
                                                                           (int (inc sidx)))
                                                                         this)
        (< (inc didx) (count (.-segids ^datomic.index.DirNode dir))) (do
                                                                       (set! didx (int (inc didx)))
                                                                       (when
                                                                         (= (rem didx 2) 0)
                                                                         (loop
                                                                           [dir dir
                                                                            i 0
                                                                            didx (+ 4 didx)]
                                                                           (when
                                                                             (and
                                                                               (< i 2)
                                                                               (<
                                                                                 didx
                                                                                 (count
                                                                                   (.-segids
                                                                                     ^datomic.index.DirNode dir))))
                                                                             (let
                                                                               [k
                                                                                (aget
                                                                                  (.-segids
                                                                                    ^datomic.index.DirNode dir)
                                                                                  (int didx))]
                                                                               (cache/read-ahead
                                                                                 lookup
                                                                                 k))
                                                                             (recur
                                                                               dir
                                                                               (inc i)
                                                                               (inc didx)))))
                                                                       (set!
                                                                         seg
                                                                         (common/getx
                                                                           lookup
                                                                           (aget
                                                                             (.-segids
                                                                               ^datomic.index.DirNode dir)
                                                                             (int didx))))
                                                                       (set!
                                                                         sidx
                                                                         (int
                                                                           (aget
                                                                             (ints
                                                                               (.-offsets
                                                                                 ^datomic.index.DirNode dir))
                                                                             didx)))
                                                                       this)
        (< (inc ridx) (count (.-dirids ^datomic.index.RootNode root))) (do
                                                                         (set!
                                                                           ridx
                                                                           (int (inc ridx)))
                                                                         (set!
                                                                           dir
                                                                           (datomic.index/get-dir-node
                                                                             root
                                                                             (java.lang.Integer/valueOf
                                                                               (int ridx))
                                                                             lookup
                                                                             false))
                                                                         (set! didx (int 0))
                                                                         (set!
                                                                           seg
                                                                           (common/getx
                                                                             lookup
                                                                             (aget
                                                                               (.-segids
                                                                                 ^datomic.index.DirNode dir)
                                                                               (int didx))))
                                                                         (set!
                                                                           sidx
                                                                           (int
                                                                             (aget
                                                                               (ints
                                                                                 (.-offsets
                                                                                   ^datomic.index.DirNode dir))
                                                                               didx)))
                                                                         this)
        :else (do nil)))
    (get [this] (.get ^datomic.index.TransposedData seg (int sidx)))
    (dir-seq
      [this]
      (let [iter__6398__auto__ (fn iter__14291
                                 ([s__14292]
                                   (lazy-seq
                                     (loop [s__14292 s__14292]
                                       (let [temp__5825__auto__ (seq s__14292)]
                                         (when temp__5825__auto__
                                           (let [xs__6385__auto__ temp__5825__auto__
                                                 ri (first xs__6385__auto__)
                                                 d (datomic.index/get-dir-node
                                                     root
                                                     ri
                                                     lookup
                                                     false)
                                                 iterys__6394__auto__ (fn
                                                                        iter__14293
                                                                        ([s__14294]
                                                                          (lazy-seq
                                                                            (loop
                                                                              [s__14294 s__14294]
                                                                              (let
                                                                                [temp__5825__auto__
                                                                                 (seq s__14294)]
                                                                                (when
                                                                                  temp__5825__auto__
                                                                                  (let
                                                                                    [s__14294
                                                                                     temp__5825__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        s__14294)
                                                                                      (let
                                                                                        [c__6396__auto__
                                                                                         (chunk-first
                                                                                           s__14294)
                                                                                         size__6397__auto__
                                                                                         (count
                                                                                           c__6396__auto__)
                                                                                         b__14296
                                                                                         (chunk-buffer
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               size__6397__auto__)))]
                                                                                        (if
                                                                                          (loop
                                                                                            [i__14295
                                                                                             0]
                                                                                            (if
                                                                                              (<
                                                                                                i__14295
                                                                                                size__6397__auto__)
                                                                                              (let
                                                                                                [di
                                                                                                 (.nth
                                                                                                   ^clojure.lang.Indexed c__6396__auto__
                                                                                                   (unchecked-int
                                                                                                     i__14295))]
                                                                                                (if
                                                                                                  (or
                                                                                                    (not=
                                                                                                      ri
                                                                                                      (java.lang.Integer/valueOf
                                                                                                        (int
                                                                                                          ridx)))
                                                                                                    (>=
                                                                                                      di
                                                                                                      didx))
                                                                                                  (do
                                                                                                    (chunk-append
                                                                                                      b__14296
                                                                                                      {:key
                                                                                                       (nth
                                                                                                         (.-keydata
                                                                                                           ^datomic.index.DirNode d)
                                                                                                         (unchecked-int
                                                                                                           ^java.lang.Number di)),
                                                                                                       :seg
                                                                                                       (aget
                                                                                                         (.-segids
                                                                                                           ^datomic.index.DirNode d)
                                                                                                         (unchecked-int
                                                                                                           di)),
                                                                                                       :count
                                                                                                       (java.lang.Integer/valueOf
                                                                                                         (int
                                                                                                           (aget
                                                                                                             (.-counts
                                                                                                               ^datomic.index.DirNode d)
                                                                                                             (unchecked-int
                                                                                                               di)))),
                                                                                                       :ri
                                                                                                       ri,
                                                                                                       :di
                                                                                                       di})
                                                                                                    (recur
                                                                                                      (inc
                                                                                                        i__14295)))
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__14295))))
                                                                                              true))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__14296)
                                                                                            (^clojure.lang.IFn iter__14293
                                                                                              (chunk-rest
                                                                                                s__14294)))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__14296)
                                                                                            nil)))
                                                                                      (let
                                                                                        [di
                                                                                         (first
                                                                                           s__14294)]
                                                                                        (if
                                                                                          (or
                                                                                            (not=
                                                                                              ri
                                                                                              (java.lang.Integer/valueOf
                                                                                                (int
                                                                                                  ridx)))
                                                                                            (>=
                                                                                              di
                                                                                              didx))
                                                                                          (cons
                                                                                            {:key
                                                                                             (nth
                                                                                               (.-keydata
                                                                                                 ^datomic.index.DirNode d)
                                                                                               (unchecked-int
                                                                                                 ^java.lang.Number di)),
                                                                                             :seg
                                                                                             (aget
                                                                                               (.-segids
                                                                                                 ^datomic.index.DirNode d)
                                                                                               (unchecked-int
                                                                                                 di)),
                                                                                             :count
                                                                                             (java.lang.Integer/valueOf
                                                                                               (int
                                                                                                 (aget
                                                                                                   (.-counts
                                                                                                     ^datomic.index.DirNode d)
                                                                                                   (unchecked-int
                                                                                                     di)))),
                                                                                             :ri
                                                                                             ri,
                                                                                             :di
                                                                                             di}
                                                                                            (^clojure.lang.IFn iter__14293
                                                                                              (rest
                                                                                                s__14294)))
                                                                                          (recur
                                                                                            (rest
                                                                                              s__14294))))))))))))
                                                 fs__6395__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6394__auto__
                                                                      (range
                                                                        (java.lang.Integer/valueOf
                                                                          (int
                                                                            (count
                                                                              (.-segids
                                                                                ^datomic.index.DirNode d)))))))]
                                             (if fs__6395__auto__
                                               (concat
                                                 fs__6395__auto__
                                                 (^clojure.lang.IFn iter__14291 (rest s__14292)))
                                               (recur (rest s__14292))))))))))]
        (^clojure.lang.IFn iter__6398__auto__
          (range
            (java.lang.Integer/valueOf (int ridx))
            (java.lang.Integer/valueOf (int (count (.-dirids ^datomic.index.RootNode root))))))))
    (seg-seq
      [this]
      (let [iter__6398__auto__ (fn iter__14266
                                 ([s__14267]
                                   (lazy-seq
                                     (loop [s__14267 s__14267]
                                       (let [temp__5825__auto__ (seq s__14267)]
                                         (when temp__5825__auto__
                                           (let [xs__6385__auto__ temp__5825__auto__
                                                 ri (first xs__6385__auto__)
                                                 d (datomic.index/get-dir-node
                                                     root
                                                     ri
                                                     lookup
                                                     false)
                                                 iterys__6394__auto__ (fn
                                                                        iter__14268
                                                                        ([s__14269]
                                                                          (lazy-seq
                                                                            (loop
                                                                              [s__14269 s__14269]
                                                                              (let
                                                                                [temp__5825__auto__
                                                                                 (seq s__14269)]
                                                                                (when
                                                                                  temp__5825__auto__
                                                                                  (let
                                                                                    [s__14269
                                                                                     temp__5825__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        s__14269)
                                                                                      (let
                                                                                        [c__6396__auto__
                                                                                         (chunk-first
                                                                                           s__14269)
                                                                                         size__6397__auto__
                                                                                         (count
                                                                                           c__6396__auto__)
                                                                                         b__14271
                                                                                         (chunk-buffer
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               size__6397__auto__)))]
                                                                                        (if
                                                                                          (loop
                                                                                            [i__14270
                                                                                             0]
                                                                                            (if
                                                                                              (<
                                                                                                i__14270
                                                                                                size__6397__auto__)
                                                                                              (let
                                                                                                [di
                                                                                                 (.nth
                                                                                                   ^clojure.lang.Indexed c__6396__auto__
                                                                                                   (unchecked-int
                                                                                                     i__14270))]
                                                                                                (if
                                                                                                  (or
                                                                                                    (not=
                                                                                                      ri
                                                                                                      (java.lang.Integer/valueOf
                                                                                                        (int
                                                                                                          ridx)))
                                                                                                    (>=
                                                                                                      di
                                                                                                      didx))
                                                                                                  (do
                                                                                                    (chunk-append
                                                                                                      b__14271
                                                                                                      (let
                                                                                                        [s
                                                                                                         (common/getx
                                                                                                           lookup
                                                                                                           (nth
                                                                                                             (.-segids
                                                                                                               ^datomic.index.DirNode d)
                                                                                                             (unchecked-int
                                                                                                               ^java.lang.Number di)))]
                                                                                                        (nth
                                                                                                          (.-keydata
                                                                                                            ^datomic.index.DirNode d)
                                                                                                          (unchecked-int
                                                                                                            ^java.lang.Number di))))
                                                                                                    (recur
                                                                                                      (inc
                                                                                                        i__14270)))
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__14270))))
                                                                                              true))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__14271)
                                                                                            (^clojure.lang.IFn iter__14268
                                                                                              (chunk-rest
                                                                                                s__14269)))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__14271)
                                                                                            nil)))
                                                                                      (let
                                                                                        [di
                                                                                         (first
                                                                                           s__14269)]
                                                                                        (if
                                                                                          (or
                                                                                            (not=
                                                                                              ri
                                                                                              (java.lang.Integer/valueOf
                                                                                                (int
                                                                                                  ridx)))
                                                                                            (>=
                                                                                              di
                                                                                              didx))
                                                                                          (cons
                                                                                            (let
                                                                                              [s
                                                                                               (common/getx
                                                                                                 lookup
                                                                                                 (nth
                                                                                                   (.-segids
                                                                                                     ^datomic.index.DirNode d)
                                                                                                   (unchecked-int
                                                                                                     ^java.lang.Number di)))]
                                                                                              (nth
                                                                                                (.-keydata
                                                                                                  ^datomic.index.DirNode d)
                                                                                                (unchecked-int
                                                                                                  ^java.lang.Number di)))
                                                                                            (^clojure.lang.IFn iter__14268
                                                                                              (rest
                                                                                                s__14269)))
                                                                                          (recur
                                                                                            (rest
                                                                                              s__14269))))))))))))
                                                 fs__6395__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6394__auto__
                                                                      (range
                                                                        (java.lang.Integer/valueOf
                                                                          (int
                                                                            (count
                                                                              (.-segids
                                                                                ^datomic.index.DirNode d)))))))]
                                             (if fs__6395__auto__
                                               (concat
                                                 fs__6395__auto__
                                                 (^clojure.lang.IFn iter__14266 (rest s__14267)))
                                               (recur (rest s__14267))))))))))]
        (^clojure.lang.IFn iter__6398__auto__
          (range
            (java.lang.Integer/valueOf (int ridx))
            (java.lang.Integer/valueOf (int (count (.-dirids ^datomic.index.RootNode root))))))))
    (seg+item-seq
      [this]
      (let [segids (let [iter__6398__auto__ (fn iter__14237
                                              ([s__14238]
                                                (lazy-seq
                                                  (loop [s__14238 s__14238]
                                                    (let [temp__5825__auto__ (seq s__14238)]
                                                      (when temp__5825__auto__
                                                        (let [xs__6385__auto__ temp__5825__auto__
                                                              ri (first xs__6385__auto__)
                                                              d
                                                              (datomic.index/get-dir-node
                                                                root
                                                                ri
                                                                lookup
                                                                false)
                                                              iterys__6394__auto__
                                                              (fn
                                                                iter__14239
                                                                ([s__14240]
                                                                  (lazy-seq
                                                                    (loop
                                                                      [s__14240 s__14240]
                                                                      (let
                                                                        [temp__5825__auto__
                                                                         (seq s__14240)]
                                                                        (when
                                                                          temp__5825__auto__
                                                                          (let
                                                                            [s__14240
                                                                             temp__5825__auto__]
                                                                            (if
                                                                              (chunked-seq?
                                                                                s__14240)
                                                                              (let
                                                                                [c__6396__auto__
                                                                                 (chunk-first
                                                                                   s__14240)
                                                                                 size__6397__auto__
                                                                                 (count
                                                                                   c__6396__auto__)
                                                                                 b__14242
                                                                                 (chunk-buffer
                                                                                   (java.lang.Integer/valueOf
                                                                                     (int
                                                                                       size__6397__auto__)))]
                                                                                (if
                                                                                  (loop
                                                                                    [i__14241 0]
                                                                                    (if
                                                                                      (<
                                                                                        i__14241
                                                                                        size__6397__auto__)
                                                                                      (let
                                                                                        [di
                                                                                         (.nth
                                                                                           ^clojure.lang.Indexed c__6396__auto__
                                                                                           (unchecked-int
                                                                                             i__14241))]
                                                                                        (if
                                                                                          (or
                                                                                            (not=
                                                                                              ri
                                                                                              (java.lang.Integer/valueOf
                                                                                                (int
                                                                                                  ridx)))
                                                                                            (>=
                                                                                              di
                                                                                              didx))
                                                                                          (do
                                                                                            (chunk-append
                                                                                              b__14242
                                                                                              (nth
                                                                                                (.-segids
                                                                                                  ^datomic.index.DirNode d)
                                                                                                (unchecked-int
                                                                                                  ^java.lang.Number di)))
                                                                                            (recur
                                                                                              (inc
                                                                                                i__14241)))
                                                                                          (recur
                                                                                            (inc
                                                                                              i__14241))))
                                                                                      true))
                                                                                  (chunk-cons
                                                                                    (chunk
                                                                                      b__14242)
                                                                                    (^clojure.lang.IFn iter__14239
                                                                                      (chunk-rest
                                                                                        s__14240)))
                                                                                  (chunk-cons
                                                                                    (chunk
                                                                                      b__14242)
                                                                                    nil)))
                                                                              (let
                                                                                [di
                                                                                 (first s__14240)]
                                                                                (if
                                                                                  (or
                                                                                    (not=
                                                                                      ri
                                                                                      (java.lang.Integer/valueOf
                                                                                        (int
                                                                                          ridx)))
                                                                                    (>= di didx))
                                                                                  (cons
                                                                                    (nth
                                                                                      (.-segids
                                                                                        ^datomic.index.DirNode d)
                                                                                      (unchecked-int
                                                                                        ^java.lang.Number di))
                                                                                    (^clojure.lang.IFn iter__14239
                                                                                      (rest
                                                                                        s__14240)))
                                                                                  (recur
                                                                                    (rest
                                                                                      s__14240))))))))))))
                                                              fs__6395__auto__
                                                              (seq
                                                                (^clojure.lang.IFn iterys__6394__auto__
                                                                  (range
                                                                    (java.lang.Integer/valueOf
                                                                      (int
                                                                        (count
                                                                          (.-segids
                                                                            ^datomic.index.DirNode d)))))))]
                                                          (if fs__6395__auto__
                                                            (concat
                                                              fs__6395__auto__
                                                              (^clojure.lang.IFn iter__14237
                                                                (rest s__14238)))
                                                            (recur (rest s__14238))))))))))]
                     (^clojure.lang.IFn iter__6398__auto__
                       (range
                         (java.lang.Integer/valueOf (int ridx))
                         (java.lang.Integer/valueOf
                           (int (count (.-dirids ^datomic.index.RootNode root)))))))]
        (mapcat
          (fn fn__14262
            ([segid]
              (map
                (fn fn__14263 ([item] {:segid segid, :item item}))
                (cache/getx-uncached lookup segid))))
          segids))))
  (clojure.core/import 'datomic.index.TreeIter)
  (defn ->TreeIter
    ([lookup root ridx dir didx seg sidx]
      (datomic.index.TreeIter.
        lookup
        root
        (unchecked-int ^java.lang.Number ridx)
        dir
        (unchecked-int ^java.lang.Number didx)
        seg
        (unchecked-int ^java.lang.Number sidx))))
  (reset-meta!
    #'->TreeIter
    (assoc
      {:arglists (clojure.core/list ['lookup 'root 'ridx 'dir 'didx 'seg 'sidx]), :column (int 1)}
      :name
      '->TreeIter
      :ns
      *ns*))
  (definterface
    IBinarySearch
    (^long search [^datomic.index.TransposedData arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.index.IBinarySearch)
  (definterface
    IndexedComparator
    (^long compare [^datomic.db.Datum arg0 ^datomic.index.TransposedData arg1 ^long arg2]))
  (clojure.core/import 'datomic.index.IndexedComparator)
  (.setMeta (clojure.lang.RT/var "datomic.index" "eavt-cmpi") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "eavt-cmpi")
    (reify
      datomic.index.IndexedComparator
      datomic.index.IBinarySearch
      (^long compare
        [this ^datomic.db.Datum x ^datomic.index.TransposedData ys ^long i]
        (.longValue
          (let [eai (bit-shift-left i 1)
                a (aget (.-eas ^datomic.index.TransposedData ys) (inc eai))
                e (aget (.-eas ^datomic.index.TransposedData ys) eai)]
            (cond
              (< (.getE ^datomic.db.Datum x) e) -1
              (> (.getE ^datomic.db.Datum x) e) 1
              (< (.getA ^datomic.db.Datum x) a) -1
              (> (.getA ^datomic.db.Datum x) a) 1
              :else (do
                      (let [c (common/compare
                                (.getV ^datomic.db.Datum x)
                                (.getV ^datomic.index.TransposedData ys (unchecked-int i)))]
                        (cond
                          (not (zero? c)) (long c)
                          (>
                            (.getT ^datomic.db.Datum x)
                            (.getT ^datomic.index.TransposedData ys (unchecked-int i))) -1
                          (<
                            (.getT ^datomic.db.Datum x)
                            (.getT ^datomic.index.TransposedData ys (unchecked-int i))) 1
                          (=
                            (.isAssertion ^datomic.db.Datum x)
                            (.isAssertion ^datomic.index.TransposedData ys (unchecked-int i))) 0
                          (.isAssertion ^datomic.db.Datum x) -1
                          :else (do 1))))))))
      (^long search
        [this ^datomic.index.TransposedData tdata k]
        (loop [low 0 high (dec (.size ^datomic.index.TransposedData tdata))]
          (if (<= low high)
            (let [mid (quot (+ low high) 2)
                  c (.compare
                      this
                      ^datomic.db.Datum k
                      ^datomic.index.TransposedData tdata
                      (long mid))]
              (if (> c 0) (recur (inc mid) high) (if (< c 0) (recur low (dec mid)) mid)))
            (- (inc low)))))))
  (.setMeta (clojure.lang.RT/var "datomic.index" "avet-cmpi") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "avet-cmpi")
    (reify
      datomic.index.IndexedComparator
      datomic.index.IBinarySearch
      (^long compare
        [this ^datomic.db.Datum x ^datomic.index.TransposedData ys ^long i]
        (.longValue
          (let [eai (bit-shift-left i 1)
                a (aget (.-eas ^datomic.index.TransposedData ys) (inc eai))
                e (aget (.-eas ^datomic.index.TransposedData ys) eai)]
            (cond
              (< (.getA ^datomic.db.Datum x) a) -1
              (> (.getA ^datomic.db.Datum x) a) 1
              :else (do
                      (let [c (common/compare
                                (.getV ^datomic.db.Datum x)
                                (.getV ^datomic.index.TransposedData ys (unchecked-int i)))]
                        (cond
                          (not (zero? c)) (long c)
                          (< (.getE ^datomic.db.Datum x) e) -1
                          (> (.getE ^datomic.db.Datum x) e) 1
                          (>
                            (.getT ^datomic.db.Datum x)
                            (.getT ^datomic.index.TransposedData ys (unchecked-int i))) -1
                          (<
                            (.getT ^datomic.db.Datum x)
                            (.getT ^datomic.index.TransposedData ys (unchecked-int i))) 1
                          (=
                            (.isAssertion ^datomic.db.Datum x)
                            (.isAssertion ^datomic.index.TransposedData ys (unchecked-int i))) 0
                          (.isAssertion ^datomic.db.Datum x) -1
                          :else (do 1))))))))
      (^long search
        [this ^datomic.index.TransposedData tdata k]
        (loop [low 0 high (dec (.size ^datomic.index.TransposedData tdata))]
          (if (<= low high)
            (let [mid (quot (+ low high) 2)
                  c (.compare
                      this
                      ^datomic.db.Datum k
                      ^datomic.index.TransposedData tdata
                      (long mid))]
              (if (> c 0) (recur (inc mid) high) (if (< c 0) (recur low (dec mid)) mid)))
            (- (inc low)))))))
  (.setMeta (clojure.lang.RT/var "datomic.index" "aevt-cmpi") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "aevt-cmpi")
    (reify
      datomic.index.IndexedComparator
      datomic.index.IBinarySearch
      (^long compare
        [this ^datomic.db.Datum x ^datomic.index.TransposedData ys ^long i]
        (.longValue
          (let [eai (bit-shift-left i 1)
                a (aget (.-eas ^datomic.index.TransposedData ys) (inc eai))
                e (aget (.-eas ^datomic.index.TransposedData ys) eai)]
            (cond
              (< (.getA ^datomic.db.Datum x) a) -1
              (> (.getA ^datomic.db.Datum x) a) 1
              (< (.getE ^datomic.db.Datum x) e) -1
              (> (.getE ^datomic.db.Datum x) e) 1
              :else (do
                      (let [c (common/compare
                                (.getV ^datomic.db.Datum x)
                                (.getV ^datomic.index.TransposedData ys (unchecked-int i)))]
                        (cond
                          (not (zero? c)) (long c)
                          (>
                            (.getT ^datomic.db.Datum x)
                            (.getT ^datomic.index.TransposedData ys (unchecked-int i))) -1
                          (<
                            (.getT ^datomic.db.Datum x)
                            (.getT ^datomic.index.TransposedData ys (unchecked-int i))) 1
                          (=
                            (.isAssertion ^datomic.db.Datum x)
                            (.isAssertion ^datomic.index.TransposedData ys (unchecked-int i))) 0
                          (.isAssertion ^datomic.db.Datum x) -1
                          :else (do 1))))))))
      (^long search
        [this ^datomic.index.TransposedData tdata k]
        (loop [low 0 high (dec (.size ^datomic.index.TransposedData tdata))]
          (if (<= low high)
            (let [mid (bit-shift-right (+ low high) 1)
                  c (.compare
                      this
                      ^datomic.db.Datum k
                      ^datomic.index.TransposedData tdata
                      (long mid))]
              (if (> c 0) (recur (inc mid) high) (if (< c 0) (recur low (dec mid)) mid)))
            (- (inc low)))))))
  (.setMeta (clojure.lang.RT/var "datomic.index" "raet-cmpi") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "raet-cmpi")
    (reify
      datomic.index.IndexedComparator
      datomic.index.IBinarySearch
      (^long compare
        [this ^datomic.db.Datum x ^datomic.index.TransposedData ys ^long i]
        (.longValue
          (let [c (common/compare
                    (.getV ^datomic.db.Datum x)
                    (.getV ^datomic.index.TransposedData ys (unchecked-int i)))
                eai (bit-shift-left i 1)
                a (aget (.-eas ^datomic.index.TransposedData ys) (inc eai))
                e (aget (.-eas ^datomic.index.TransposedData ys) eai)]
            (cond
              (not (zero? c)) (long c)
              (< (.getA ^datomic.db.Datum x) a) -1
              (> (.getA ^datomic.db.Datum x) a) 1
              (< (.getE ^datomic.db.Datum x) e) -1
              (> (.getE ^datomic.db.Datum x) e) 1
              (>
                (.getT ^datomic.db.Datum x)
                (.getT ^datomic.index.TransposedData ys (unchecked-int i))) -1
              (<
                (.getT ^datomic.db.Datum x)
                (.getT ^datomic.index.TransposedData ys (unchecked-int i))) 1
              (=
                (.isAssertion ^datomic.db.Datum x)
                (.isAssertion ^datomic.index.TransposedData ys (unchecked-int i))) 0
              (.isAssertion ^datomic.db.Datum x) -1
              :else (do 1)))))
      (^long search
        [this ^datomic.index.TransposedData tdata k]
        (loop [low 0 high (dec (.size ^datomic.index.TransposedData tdata))]
          (if (<= low high)
            (let [mid (quot (+ low high) 2)
                  c (.compare
                      this
                      ^datomic.db.Datum k
                      ^datomic.index.TransposedData tdata
                      (long mid))]
              (if (> c 0) (recur (inc mid) high) (if (< c 0) (recur low (dec mid)) mid)))
            (- (inc low)))))))
  (defn btree-search
    ([coll k cmp]
      (let [idx (java.util.Collections/binarySearch
                  ^java.util.List coll
                  k
                  ^java.util.Comparator cmp)]
        (if (< idx 0) (long (max 0 (dec (- (inc idx))))) (java.lang.Integer/valueOf (int idx))))))
  (reset-meta!
    #'btree-search
    (assoc
      {:private true, :arglists (clojure.core/list ['coll 'k 'cmp]), :column (int 1)}
      :name
      'btree-search
      :ns
      *ns*))
  (defn binary-search
    ([coll k cmp]
      (let [idx (java.util.Collections/binarySearch
                  ^java.util.List coll
                  k
                  ^java.util.Comparator cmp)
            idx (if (< idx 0) (long (- (inc idx))) (java.lang.Integer/valueOf (int idx)))]
        (when (< idx (count coll)) idx))))
  (reset-meta!
    #'binary-search
    (assoc
      {:private true, :arglists (clojure.core/list ['coll 'k 'cmp]), :column (int 1)}
      :name
      'binary-search
      :ns
      *ns*))
  (defn indexed-binary-search
    (^long [tdata k cmpi]
      (.longValue
        (loop [low 0 high (dec (.size ^datomic.index.TransposedData tdata))]
          (if (<= low high)
            (let [mid (quot (+ low high) 2)
                  c (.compare
                      ^datomic.index.IndexedComparator cmpi
                      ^datomic.db.Datum k
                      ^datomic.index.TransposedData tdata
                      (long mid))]
              (cond
                (> c 0) (recur (inc mid) high)
                (< c 0) (recur low (dec mid))
                :else (do (long mid))))
            (long (- (inc low))))))))
  (reset-meta!
    #'indexed-binary-search
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'tdata {:tag 'TransposedData})
            'k
            (.withMeta 'cmpi {:tag 'IndexedComparator})]
           {:tag 'long})),
       :column (int 1)}
      :name
      'indexed-binary-search
      :ns
      *ns*))
  (defn ibtree-search
    (^long [coll k cmp]
      (let [idx (.search ^datomic.index.IBinarySearch cmp ^datomic.index.TransposedData coll k)]
        (if (< idx 0) (max 0 (dec (- (inc idx)))) idx))))
  (reset-meta!
    #'ibtree-search
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta ['coll 'k (.withMeta 'cmp {:tag 'IBinarySearch})] {:tag 'long})),
       :column (int 1)}
      :name
      'ibtree-search
      :ns
      *ns*))
  (defn ibinary-search
    ([coll k cmp]
      (let [idx (.search ^datomic.index.IBinarySearch cmp ^datomic.index.TransposedData coll k)
            idx (if (< idx 0) (- (inc idx)) idx)]
        (when (< idx (.size ^java.util.List coll)) (long idx)))))
  (reset-meta!
    #'ibinary-search
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'coll {:tag 'java.util.List}) 'k (.withMeta 'cmp {:tag 'IBinarySearch})]),
       :column (int 1)}
      :name
      'ibinary-search
      :ns
      *ns*))
  (deftype
    Index
    [lookup cmpi root cached_count order]
    datomic.index.IIndex
    clojure.lang.IPersistentSet
    clojure.lang.Counted
    datomic.btset.IDataSet
    clojure.lang.Seqable
    (^datomic.iter.Iter seek
      [this k]
      (do
        (io-stats/using-index! order)
        (when (> (alength (.-dirids ^datomic.index.RootNode root)) 0)
          (let [ridx (datomic.index/ibtree-search (.-keydata ^datomic.index.RootNode root) k cmpi)
                dir (datomic.index/get-dir-node root (long ridx) lookup true)]
            (when (> (alength (.-segids ^datomic.index.DirNode dir)) 0)
              (let [didx (datomic.index/ibtree-search
                           (.-keydata ^datomic.index.DirNode dir)
                           k
                           cmpi)
                    segk (aget (.-segids ^datomic.index.DirNode dir) (int didx))
                    _ (io-trace/note! segk order)
                    seg (or
                          (let [seg (aget (.-segs ^datomic.index.DirNode dir) (int didx))
                                temp__5825__auto__ (and seg (.get ^java.lang.ref.Reference seg))]
                            (when temp__5825__auto__
                              (let [ret temp__5825__auto__]
                                (io-stats/inc! io-stats/*io-index*)
                                ret)))
                          (let [seg (common/getx
                                      lookup
                                      (aget (.-segids ^datomic.index.DirNode dir) (int didx)))]
                            (aset
                              (.-segs ^datomic.index.DirNode dir)
                              (int didx)
                              (java.lang.ref.WeakReference. seg))
                            seg))
                    doff (aget (.-offsets ^datomic.index.DirNode dir) didx)
                    dcount (aget (.-counts ^datomic.index.DirNode dir) didx)
                    sidx (datomic.index/ibinary-search seg k cmpi)]
                (if sidx
                  (datomic.index.TreeIter.
                    lookup
                    root
                    (unchecked-int ridx)
                    dir
                    (unchecked-int didx)
                    seg
                    (unchecked-int (+ (long sidx) doff)))
                  (.next
                    (datomic.index.TreeIter.
                      lookup
                      root
                      (unchecked-int ridx)
                      dir
                      (unchecked-int didx)
                      seg
                      (unchecked-int (+ (+ doff dcount) -1)))))))))))
    (^datomic.iter.Iter seek
      [this]
      (do
        (io-stats/using-index! order)
        (when (> (alength (.-dirids ^datomic.index.RootNode root)) 0)
          (let [dir (datomic.index/get-dir-node root 0 lookup false)]
            (when (> (count (.-segids ^datomic.index.DirNode dir)) 0)
              (datomic.index.TreeIter.
                lookup
                root
                (unchecked-int 0)
                dir
                (unchecked-int 0)
                (common/getx lookup (nth (.-segids ^datomic.index.DirNode dir) (unchecked-int 0)))
                (unchecked-int
                  (nth (.-offsets ^datomic.index.DirNode dir) (unchecked-int 0)))))))))
    (^datomic.iter.Iter seekLast
      [this]
      (do
        (io-stats/using-index! order)
        (let [dirids (.-dirids ^datomic.index.RootNode root) ridx (dec (count dirids))]
          (when-not (< ridx 0)
            (let [dir (datomic.index/get-dir-node root (long ridx) lookup false)
                  segids (.-segids ^datomic.index.DirNode dir)
                  didx (dec (count segids))]
              (when-not (< didx 0)
                (let [seg (common/getx lookup (nth segids (unchecked-int didx)))
                      segidx (dec
                               (+
                                 (nth (.-offsets ^datomic.index.DirNode dir) (unchecked-int didx))
                                 (count seg)))]
                  (datomic.index.TreeIter.
                    lookup
                    root
                    (unchecked-int ridx)
                    dir
                    (unchecked-int didx)
                    seg
                    (unchecked-int ^java.lang.Number segidx)))))))))
    (^long longCount
      [this]
      (.longValue (or (deref cached_count) (reset! cached_count (reduce + (map :count this))))))
    (seek-seg
      [this k]
      (when (> (alength (.-dirids ^datomic.index.RootNode root)) 0)
        (let [ridx (datomic.index/ibtree-search (.-keydata ^datomic.index.RootNode root) k cmpi)
              dir (datomic.index/get-dir-node root (long ridx) lookup true)]
          (when (> (alength (.-segids ^datomic.index.DirNode dir)) 0)
            (let [didx (datomic.index/ibtree-search (.-keydata ^datomic.index.DirNode dir) k cmpi)]
              (aget (.-segids ^datomic.index.DirNode dir) (int didx)))))))
    (^clojure.lang.ISeq seq
      [this]
      (seq
        (let [iter__6398__auto__ (fn iter__14342
                                   ([s__14343]
                                     (lazy-seq
                                       (loop [s__14343 s__14343]
                                         (let [temp__5825__auto__ (seq s__14343)]
                                           (when temp__5825__auto__
                                             (let [xs__6385__auto__ temp__5825__auto__
                                                   ri (first xs__6385__auto__)
                                                   d (datomic.index/get-dir-node
                                                       root
                                                       ri
                                                       lookup
                                                       false)
                                                   iterys__6394__auto__ (fn
                                                                          iter__14344
                                                                          ([s__14345]
                                                                            (lazy-seq
                                                                              (let
                                                                                [s__14345 s__14345
                                                                                 temp__5825__auto__
                                                                                 (seq s__14345)]
                                                                                (when
                                                                                  temp__5825__auto__
                                                                                  (let
                                                                                    [s__14345
                                                                                     temp__5825__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        s__14345)
                                                                                      (let
                                                                                        [c__6396__auto__
                                                                                         (chunk-first
                                                                                           s__14345)
                                                                                         size__6397__auto__
                                                                                         (count
                                                                                           c__6396__auto__)
                                                                                         b__14347
                                                                                         (chunk-buffer
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               size__6397__auto__)))]
                                                                                        (if
                                                                                          (loop
                                                                                            [i__14346
                                                                                             0]
                                                                                            (if
                                                                                              (<
                                                                                                i__14346
                                                                                                size__6397__auto__)
                                                                                              (let
                                                                                                [di
                                                                                                 (.nth
                                                                                                   ^clojure.lang.Indexed c__6396__auto__
                                                                                                   (unchecked-int
                                                                                                     i__14346))]
                                                                                                (chunk-append
                                                                                                  b__14347
                                                                                                  {:key
                                                                                                   (nth
                                                                                                     (.-keydata
                                                                                                       ^datomic.index.DirNode d)
                                                                                                     (unchecked-int
                                                                                                       ^java.lang.Number di)),
                                                                                                   :seg
                                                                                                   (aget
                                                                                                     (.-segids
                                                                                                       ^datomic.index.DirNode d)
                                                                                                     (unchecked-int
                                                                                                       di)),
                                                                                                   :count
                                                                                                   (java.lang.Integer/valueOf
                                                                                                     (int
                                                                                                       (aget
                                                                                                         (.-counts
                                                                                                           ^datomic.index.DirNode d)
                                                                                                         (unchecked-int
                                                                                                           di))))})
                                                                                                (recur
                                                                                                  (inc
                                                                                                    i__14346)))
                                                                                              true))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__14347)
                                                                                            (^clojure.lang.IFn iter__14344
                                                                                              (chunk-rest
                                                                                                s__14345)))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__14347)
                                                                                            nil)))
                                                                                      (let
                                                                                        [di
                                                                                         (first
                                                                                           s__14345)]
                                                                                        (cons
                                                                                          {:key
                                                                                           (nth
                                                                                             (.-keydata
                                                                                               ^datomic.index.DirNode d)
                                                                                             (unchecked-int
                                                                                               ^java.lang.Number di)),
                                                                                           :seg
                                                                                           (aget
                                                                                             (.-segids
                                                                                               ^datomic.index.DirNode d)
                                                                                             (unchecked-int
                                                                                               di)),
                                                                                           :count
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               (aget
                                                                                                 (.-counts
                                                                                                   ^datomic.index.DirNode d)
                                                                                                 (unchecked-int
                                                                                                   di))))}
                                                                                          (^clojure.lang.IFn iter__14344
                                                                                            (rest
                                                                                              s__14345)))))))))))
                                                   fs__6395__auto__ (seq
                                                                      (^clojure.lang.IFn iterys__6394__auto__
                                                                        (range
                                                                          (java.lang.Integer/valueOf
                                                                            (int
                                                                              (count
                                                                                (.-segids
                                                                                  ^datomic.index.DirNode d)))))))]
                                               (if fs__6395__auto__
                                                 (concat
                                                   fs__6395__auto__
                                                   (^clojure.lang.IFn iter__14342 (rest s__14343)))
                                                 (recur (rest s__14343))))))))))]
          (^clojure.lang.IFn iter__6398__auto__
            (range
              (java.lang.Integer/valueOf
                (int (count (.-dirids ^datomic.index.RootNode root)))))))))
    (^int count [this] (int (.longCount this)))
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
    (^boolean equiv [this x] (.booleanValue false)))
  (clojure.core/import 'datomic.index.Index)
  (defn ->Index
    ([lookup cmpi root cached_count order]
      (datomic.index.Index. lookup cmpi root cached_count order)))
  (reset-meta!
    #'->Index
    (assoc
      {:arglists (clojure.core/list ['lookup 'cmpi 'root 'cached-count 'order]), :column (int 1)}
      :name
      '->Index
      :ns
      *ns*))
  (defn lookup-index
    ([lookup cmpi rootid]
      (datomic.index.Index.
        lookup
        cmpi
        (common/getx lookup rootid)
        (atom nil)
        (cond
          (= cmpi datomic.index/eavt-cmpi) :eavt
          (= cmpi datomic.index/avet-cmpi) :avet
          (= cmpi datomic.index/aevt-cmpi) :aevt
          (= cmpi datomic.index/raet-cmpi) (do :vaet)))))
  (reset-meta!
    #'lookup-index
    (assoc
      {:arglists (clojure.core/list ['lookup 'cmpi 'rootid]), :column (int 1)}
      :name
      'lookup-index
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.index" "common-write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "common-write-handlers")
    (merge
      fressian/user-write-handlers
      {datomic.index.TransposedData
       {"index-tdata"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w o]
            (do
              (let [d o]
                (.writeTag ^org.fressian.Writer w "index-tdata" (unchecked-int 5))
                (.writeObject ^org.fressian.Writer w (.-vs ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.getEs ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.getAs ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.-ts ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.-ops ^datomic.index.TransposedData d)))
              nil)))},
       datomic.index.RootNode
       {"index-root-node"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w o]
            (do
              (let [r o]
                (.writeTag ^org.fressian.Writer w "index-root-node" (unchecked-int 2))
                (.writeObject ^org.fressian.Writer w (.-keydata ^datomic.index.RootNode r))
                (.writeObject ^org.fressian.Writer w (.-dirids ^datomic.index.RootNode r)))
              nil)))},
       datomic.index.DirNode
       {"index-dir-node"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w o]
            (do
              (let [d o]
                (.writeTag ^org.fressian.Writer w "index-dir-node" (unchecked-int 4))
                (.writeObject ^org.fressian.Writer w (.-keydata ^datomic.index.DirNode d))
                (.writeObject ^org.fressian.Writer w (.-segids ^datomic.index.DirNode d))
                (.writeObject ^org.fressian.Writer w (ints (.-offsets ^datomic.index.DirNode d)))
                (.writeObject ^org.fressian.Writer w (ints (.-counts ^datomic.index.DirNode d))))
              nil)))}}))
  (.setMeta (clojure.lang.RT/var "datomic.index" "index-read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "index-read-handlers")
    (merge
      fressian/user-read-handlers
      {"index-tdata"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (let [vs (.readObject ^org.fressian.Reader rdr)
                 vs (if (.isArray (class vs)) vs (to-array vs))
                 vs (common/ensure-vectors-in-array vs)
                 es (.readObject ^org.fressian.Reader rdr)
                 as (.readObject ^org.fressian.Reader rdr)
                 ts (.readObject ^org.fressian.Reader rdr)
                 ops (.readObject ^org.fressian.Reader rdr)]
             (datomic.index/transposed-data es as vs ts ops)))),
       "index-root-node"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (datomic.index/root-node
             (.readObject ^org.fressian.Reader rdr)
             (.readObject ^org.fressian.Reader rdr)))),
       "index-dir-node"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (datomic.index/dir-node
             (.readObject ^org.fressian.Reader rdr)
             (.readObject ^org.fressian.Reader rdr)
             (ints (.readObject ^org.fressian.Reader rdr))
             (ints (.readObject ^org.fressian.Reader rdr)))))}))
  (defn index-ref-key-name
    ([cs]
      (let [temp__5825__auto__ (cluster/dbId cs)]
        (when temp__5825__auto__ (let [dbid temp__5825__auto__] (str "ref-index-root/" dbid))))))
  (reset-meta!
    #'index-ref-key-name
    (assoc
      {:arglists (clojure.core/list ['cs]), :column (int 1)}
      :name
      'index-ref-key-name
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.index" "fress")
    {:tag java.nio.ByteBuffer, :arglists (clojure.core/list ['val 'handlers]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "fress")
    (fn fress
      ([val handlers] (io/gzip-buffer (fressian/byte-buf val :handlers handlers :footer true)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.index" "transpose")
    {:tag datomic.index.TransposedData, :arglists (clojure.core/list ['data]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "transpose")
    (fn transpose
      ([data]
        (let [n (count data)
              es (long-array (java.lang.Integer/valueOf (int n)))
              as (int-array (java.lang.Integer/valueOf (int n)))
              vs (object-array (java.lang.Integer/valueOf (int n)))
              ts (long-array (java.lang.Integer/valueOf (int n)))
              ops (boolean-array (java.lang.Integer/valueOf (int n)))]
          (dotimes [i n]
            (let [d (nth data (unchecked-int i))]
              (aset ^longs es (int i) (long (.getE ^datomic.impl.db.IDatum d)))
              (aset ^ints as (int i) (int (.getA ^datomic.impl.db.IDatum d)))
              (aset ^"[Ljava.lang.Object;" vs (int i) (.getV ^datomic.impl.db.IDatum d))
              (aset ^longs ts (int i) (long (.getT ^datomic.impl.db.IDatum d)))
              (aset ^booleans ops (int i) (boolean (.isAssertion ^datomic.impl.db.IDatum d)))))
          (datomic.index/transposed-data es as vs ts ops)))))
  (defn write-vals
    ([cs vmap]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process
              {:event :index/write-vals,
               :count (java.lang.Integer/valueOf (int (count vmap))),
               :bytes
               (reduce
                 +
                 (map
                   (fn fn__14395
                     ([p1__14394#]
                       (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer p1__14394#)))))
                   (vals vmap)))})))
        nil)
      (monitor/add-stat :IndexWriteBatchCount (java.lang.Integer/valueOf (int (count vmap))))
      (cluster/write-vals cs :index vmap)))
  (reset-meta!
    #'write-vals
    (assoc
      {:arglists (clojure.core/list ['cs 'vmap]), :column (int 1)}
      :name
      'write-vals
      :ns
      *ns*))
  (def INDEX_VERSION 2)
  (reset-meta!
    #'INDEX_VERSION
    (assoc {:const true, :column (int 1)} :name 'INDEX_VERSION :ns *ns*))
  (defn init-index*
    ([cstore]
      (let [map__14400 cstore
            map__14400 (if (seq? map__14400)
                         (if (next map__14400)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__14400))
                           (if (seq map__14400) (first map__14400) {}))
                         map__14400)
            tenant (get map__14400 :tenant)
            dbname (get map__14400 :db)
            db (db/bootstrap-db dbname)
            iset (.-memidx ^datomic.db.Db db)
            vec__14401 (repeatedly common/rand-uuid)
            rootid (nth vec__14401 (unchecked-int 0) nil)
            eavtid (nth vec__14401 (unchecked-int 1) nil)
            avetid (nth vec__14401 (unchecked-int 2) nil)
            aevtid (nth vec__14401 (unchecked-int 3) nil)
            raetid (nth vec__14401 (unchecked-int 4) nil)
            eavt_dirid (nth vec__14401 (unchecked-int 5) nil)
            avet_dirid (nth vec__14401 (unchecked-int 6) nil)
            aevt_dirid (nth vec__14401 (unchecked-int 7) nil)
            raet_dirid (nth vec__14401 (unchecked-int 8) nil)
            eavt_segid (nth vec__14401 (unchecked-int 9) nil)
            raet_segid (nth vec__14401 (unchecked-int 10) nil)
            root (datomic.index/fress
                   {:birth-level (:birth-level db),
                    :nextT (long (.nextT ^datomic.db.Db db)),
                    :schema-level (:schema-level db),
                    :eavt-main eavtid,
                    :avet-mid nil,
                    :aevt-main aevtid,
                    :rev 0,
                    :raet-main raetid,
                    :raet-mid nil,
                    :buildRevision (config/property "datomic.buildRevision"),
                    :avet-hist nil,
                    :eavt-hist nil,
                    :raet-hist nil,
                    :aevt-mid nil,
                    :version 2,
                    :avet-main avetid,
                    :aevt-hist nil,
                    :eavt-mid nil}
                   datomic.index/common-write-handlers)
            eavt (vec (seq (.-eavt ^datomic.db.IndexSet iset)))
            eavt_root (datomic.index/fress
                        (datomic.index/root-node
                          (datomic.index/transpose [(^clojure.lang.IFn eavt 0)])
                          (to-array [eavt_dirid]))
                        datomic.index/common-write-handlers)
            eavt_dir (datomic.index/fress
                       (datomic.index/dir-node
                         (datomic.index/transpose [(^clojure.lang.IFn eavt 0)])
                         (to-array [eavt_segid])
                         (int-array [0])
                         (int-array [(java.lang.Integer/valueOf (int (count eavt)))]))
                       datomic.index/common-write-handlers)
            eavt_seg (datomic.index/fress
                       (datomic.index/transpose eavt)
                       datomic.index/common-write-handlers)
            raet (vec (seq (.-raet ^datomic.db.IndexSet iset)))
            raet_root (datomic.index/fress
                        (datomic.index/root-node
                          (datomic.index/transpose [(^clojure.lang.IFn raet 0)])
                          (to-array [raet_dirid]))
                        datomic.index/common-write-handlers)
            raet_dir (datomic.index/fress
                       (datomic.index/dir-node
                         (datomic.index/transpose [(^clojure.lang.IFn raet 0)])
                         (to-array [raet_segid])
                         (int-array [0])
                         (int-array [(java.lang.Integer/valueOf (int (count raet)))]))
                       datomic.index/common-write-handlers)
            raet_seg (datomic.index/fress
                       (datomic.index/transpose raet)
                       datomic.index/common-write-handlers)
            avet (vec (seq (.-avet ^datomic.db.IndexSet iset)))
            avets (map
                    vec
                    (partition-by
                      (fn fn__14410
                        ([p1__14398#]
                          (java.lang.Integer/valueOf
                            (int (.getA ^datomic.impl.db.IDatum p1__14398#)))))
                      avet))
            avet_root (datomic.index/fress
                        (datomic.index/root-node
                          (datomic.index/transpose [(^clojure.lang.IFn avet 0)])
                          (to-array [avet_dirid]))
                        datomic.index/common-write-handlers)
            vec__14404 (reduce
                         (fn fn__14413
                           ([p__14412 av]
                             (let [vec__14414 p__14412
                                   keys (nth vec__14414 (unchecked-int 0) nil)
                                   ids (nth vec__14414 (unchecked-int 1) nil)
                                   offs (nth vec__14414 (unchecked-int 2) nil)
                                   cnts (nth vec__14414 (unchecked-int 3) nil)
                                   bufs (nth vec__14414 (unchecked-int 4) nil)
                                   id (common/rand-uuid)
                                   buf (datomic.index/fress
                                         (datomic.index/transpose av)
                                         datomic.index/common-write-handlers)]
                               [(conj keys (^clojure.lang.IFn av 0))
                                (conj ids id)
                                (conj offs 0)
                                (conj cnts (java.lang.Integer/valueOf (int (count av))))
                                (assoc bufs id buf)])))
                         [[] [] [] [] {}]
                         avets)
            avet_keys (nth vec__14404 (unchecked-int 0) nil)
            avet_ids (nth vec__14404 (unchecked-int 1) nil)
            avet_offsets (nth vec__14404 (unchecked-int 2) nil)
            avet_counts (nth vec__14404 (unchecked-int 3) nil)
            bufmap (nth vec__14404 (unchecked-int 4) nil)
            avet_dir (datomic.index/fress
                       (datomic.index/dir-node
                         (datomic.index/transpose avet_keys)
                         (to-array avet_ids)
                         (into-array java.lang.Integer/TYPE avet_offsets)
                         (into-array java.lang.Integer/TYPE avet_counts))
                       datomic.index/common-write-handlers)
            aevt (vec (seq (.-aevt ^datomic.db.IndexSet iset)))
            aevts (map
                    vec
                    (partition-by
                      (fn fn__14418
                        ([p1__14399#]
                          (java.lang.Integer/valueOf
                            (int (.getA ^datomic.impl.db.IDatum p1__14399#)))))
                      aevt))
            aevt_root (datomic.index/fress
                        (datomic.index/root-node
                          (datomic.index/transpose [(^clojure.lang.IFn aevt 0)])
                          (to-array [aevt_dirid]))
                        datomic.index/common-write-handlers)
            vec__14407 (reduce
                         (fn fn__14421
                           ([p__14420 av]
                             (let [vec__14422 p__14420
                                   keys (nth vec__14422 (unchecked-int 0) nil)
                                   ids (nth vec__14422 (unchecked-int 1) nil)
                                   offs (nth vec__14422 (unchecked-int 2) nil)
                                   cnts (nth vec__14422 (unchecked-int 3) nil)
                                   bufs (nth vec__14422 (unchecked-int 4) nil)
                                   id (common/rand-uuid)
                                   buf (datomic.index/fress
                                         (datomic.index/transpose av)
                                         datomic.index/common-write-handlers)]
                               [(conj keys (^clojure.lang.IFn av 0))
                                (conj ids id)
                                (conj offs 0)
                                (conj cnts (java.lang.Integer/valueOf (int (count av))))
                                (assoc bufs id buf)])))
                         [[] [] [] [] bufmap]
                         aevts)
            aevt_keys (nth vec__14407 (unchecked-int 0) nil)
            aevt_ids (nth vec__14407 (unchecked-int 1) nil)
            aevt_offsets (nth vec__14407 (unchecked-int 2) nil)
            aevt_counts (nth vec__14407 (unchecked-int 3) nil)
            bufmap (nth vec__14407 (unchecked-int 4) nil)
            aevt_dir (datomic.index/fress
                       (datomic.index/dir-node
                         (datomic.index/transpose aevt_keys)
                         (to-array aevt_ids)
                         (into-array java.lang.Integer/TYPE aevt_offsets)
                         (into-array java.lang.Integer/TYPE aevt_counts))
                       datomic.index/common-write-handlers)
            bufmap (assoc
                     bufmap
                     rootid
                     root
                     eavtid
                     eavt_root
                     eavt_dirid
                     eavt_dir
                     eavt_segid
                     eavt_seg
                     raetid
                     raet_root
                     raet_dirid
                     raet_dir
                     raet_segid
                     raet_seg
                     avetid
                     avet_root
                     avet_dirid
                     avet_dir
                     aevtid
                     aevt_root
                     aevt_dirid
                     aevt_dir)]
        (datomic.index/write-vals cstore bufmap)
        rootid)))
  (reset-meta!
    #'init-index*
    (assoc {:arglists (clojure.core/list ['cstore]), :column (int 1)} :name 'init-index* :ns *ns*))
  (defn init-index
    ([cstore]
      (let [rootid (datomic.index/init-index* cstore)]
        (when (=
                :ok
                (deref
                  (cluster/set-ref
                    cstore
                    (datomic.index/index-ref-key-name cstore)
                    0
                    (cluster/uuid->val-key rootid))))
          rootid))))
  (reset-meta!
    #'init-index
    (assoc {:arglists (clojure.core/list ['cstore]), :column (int 1)} :name 'init-index :ns *ns*))
  (defn find-index-root-id
    ([cstore]
      (let [temp__5825__auto__ (:key
                                 (deref
                                   (cluster/get-ref
                                     cstore
                                     (datomic.index/index-ref-key-name cstore))))]
        (when temp__5825__auto__ (let [k temp__5825__auto__] (cluster/val-key->uuid k))))))
  (reset-meta!
    #'find-index-root-id
    (assoc
      {:arglists (clojure.core/list ['cstore]), :column (int 1)}
      :name
      'find-index-root-id
      :ns
      *ns*))
  (defn valid-version
    (^long [root_map]
      (.longValue
        (let [version (or (:version root_map) 1)]
          (when-not (contains? #{1 2} version)
            (error/state
              :db.error/index-version
              (str "This version of Datomic cannot read index version " version)))
          version))))
  (reset-meta!
    #'valid-version
    (assoc
      {:arglists (clojure.core/list (.withMeta ['root-map] {:tag 'long})), :column (int 1)}
      :name
      'valid-version
      :ns
      *ns*))
  (defn version-root-key
    ([^long version k]
      (when-not (contains? #{:eavt-main :aevt-main :raet-main :avet-main} k)
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list
                  'contains?
                  #{:eavt-main :aevt-main :raet-main :avet-main}
                  'k))))))
      (let [G__14432 version]
        (case
          G__14432
          1
          (let [G__14433 k]
            (case G__14433 :aevt-main :aevt :avet-main :avet :eavt-main :eavt :raet-main :raet))
          k))))
  (reset-meta!
    #'version-root-key
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'version {:tag 'long}) 'k]),
       :column (int 1)}
      :name
      'version-root-key
      :ns
      *ns*))
  (defn load-index
    ([olookup index_root_id]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process
              {:event :index/load-index, :olookup olookup, :index-root-id index_root_id})))
        nil)
      (let [temp__5825__auto__ (common/getx olookup index_root_id)]
        (when temp__5825__auto__
          (let [root_map temp__5825__auto__
                version (datomic.index/valid-version root_map)
                eavt (datomic.index/lookup-index
                       olookup
                       datomic.index/eavt-cmpi
                       (get root_map (datomic.index/version-root-key version :eavt-main)))
                avet (datomic.index/lookup-index
                       olookup
                       datomic.index/avet-cmpi
                       (get root_map (datomic.index/version-root-key version :avet-main)))
                aevt (datomic.index/lookup-index
                       olookup
                       datomic.index/aevt-cmpi
                       (get root_map (datomic.index/version-root-key version :aevt-main)))
                raet (datomic.index/lookup-index
                       olookup
                       datomic.index/raet-cmpi
                       (get root_map (datomic.index/version-root-key version :raet-main)))
                fulltext (fulltext/clustered-fulltext olookup (:fulltext root_map))
                eavt_hist (and
                            (:eavt-hist root_map)
                            (datomic.index/lookup-index
                              olookup
                              datomic.index/eavt-cmpi
                              (:eavt-hist root_map)))
                avet_hist (and
                            (:avet-hist root_map)
                            (datomic.index/lookup-index
                              olookup
                              datomic.index/avet-cmpi
                              (:avet-hist root_map)))
                aevt_hist (and
                            (:aevt-hist root_map)
                            (datomic.index/lookup-index
                              olookup
                              datomic.index/aevt-cmpi
                              (:aevt-hist root_map)))
                raet_hist (and
                            (:raet-hist root_map)
                            (datomic.index/lookup-index
                              olookup
                              datomic.index/raet-cmpi
                              (:raet-hist root_map)))
                fulltext_hist (fulltext/clustered-fulltext olookup (:fulltext-hist root_map))
                eavt_mid (and
                           (:eavt-mid root_map)
                           (datomic.index/lookup-index
                             olookup
                             datomic.index/eavt-cmpi
                             (:eavt-mid root_map)))
                avet_mid (and
                           (:avet-mid root_map)
                           (datomic.index/lookup-index
                             olookup
                             datomic.index/avet-cmpi
                             (:avet-mid root_map)))
                aevt_mid (and
                           (:aevt-mid root_map)
                           (datomic.index/lookup-index
                             olookup
                             datomic.index/aevt-cmpi
                             (:aevt-mid root_map)))
                raet_mid (and
                           (:raet-mid root_map)
                           (datomic.index/lookup-index
                             olookup
                             datomic.index/raet-cmpi
                             (:raet-mid root_map)))]
            {:birth-level (or (:birth-level root_map) db/MIN_SCHEMA_LEVEL),
             :root-id index_root_id,
             :mid-index (datomic.db.IndexSet. eavt_mid avet_mid aevt_mid raet_mid nil),
             :nextT (:nextT root_map),
             :schema-level (or (:schema-level root_map) db/MIN_SCHEMA_LEVEL),
             :index (datomic.db.IndexSet. eavt avet aevt raet fulltext),
             :basisT (:basisT root_map),
             :history (datomic.db.IndexSet. eavt_hist avet_hist aevt_hist raet_hist fulltext_hist),
             :rev (:rev root_map),
             :buildRevision (:buildRevision root_map)})))))
  (reset-meta!
    #'load-index
    (assoc
      {:arglists (clojure.core/list ['olookup 'index-root-id]), :column (int 1)}
      :name
      'load-index
      :ns
      *ns*))
  (def SEG_BYTES_TARGET 16000)
  (reset-meta!
    #'SEG_BYTES_TARGET
    (assoc {:private true, :const true, :column (int 1)} :name 'SEG_BYTES_TARGET :ns *ns*))
  (def SEGS_PER_DIR 3000)
  (reset-meta!
    #'SEGS_PER_DIR
    (assoc {:private true, :const true, :column (int 1)} :name 'SEGS_PER_DIR :ns *ns*))
  (def with-log
   (fn with_log
     ([&form &env name & body]
       (if name
         (seq
           (concat
             (clojure.core/list 'do)
             (clojure.core/list
               (seq
                 (concat
                   (clojure.core/list 'datomic.common/log-and-print)
                   (clojure.core/list
                     (apply
                       hash-map
                       (seq (concat (clojure.core/list :in) (clojure.core/list name))))))))
             (clojure.core/list
               (seq
                 (concat
                   (clojure.core/list 'clojure.core/let)
                   (clojure.core/list
                     (apply
                       vector
                       (seq
                         (concat
                           (clojure.core/list 'ret__14447__auto__)
                           (clojure.core/list (seq (concat (clojure.core/list 'do) body)))))))
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list 'datomic.common/log-and-print)
                         (clojure.core/list
                           (apply
                             hash-map
                             (seq (concat (clojure.core/list :out) (clojure.core/list name))))))))
                   (clojure.core/list 'ret__14447__auto__))))))
         (seq (concat (clojure.core/list 'do) body))))))
  (reset-meta!
    #'with-log
    (assoc
      {:arglists (clojure.core/list ['name '& 'body]), :column (int 1)}
      :name
      'with-log
      :ns
      *ns*))
  (.setMacro #'datomic.index/with-log)
  (def floop
   (fn floop
     ([&form &env bindings & body]
       (let [bs (partition 2 bindings) params (map first bs) args (map second bs)]
         (seq
           (concat
             (clojure.core/list 'clojure.core/let)
             (clojure.core/list
               (apply
                 vector
                 (seq
                   (concat
                     (clojure.core/list 'f__14449__auto__)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'clojure.core/fn)
                           (clojure.core/list (apply vector (seq (concat params))))
                           body)))))))
             (clojure.core/list (seq (concat (clojure.core/list 'f__14449__auto__) args)))))))))
  (reset-meta!
    #'floop
    (assoc
      {:arglists (clojure.core/list ['bindings '& 'body]), :column (int 1)}
      :name
      'floop
      :ns
      *ns*))
  (.setMacro #'datomic.index/floop)
  (defn create-pace-calculator
    ([target_work window_msec]
      (let [start (java.lang.System/currentTimeMillis)
            state (atom {:window 0, :work 0, :msec 0})
            next_state (fn next_state
                         ([p__14451 nwork]
                           (let [map__14453 p__14451
                                 map__14453 (if (seq? map__14453)
                                              (if (next map__14453)
                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                  (to-array map__14453))
                                                (if (seq map__14453) (first map__14453) {}))
                                              map__14453)
                                 window (get map__14453 :window)
                                 work (get map__14453 :work)
                                 now (java.lang.System/currentTimeMillis)
                                 elapsed (- now start)
                                 nwind (quot elapsed window_msec)
                                 msec (rem elapsed window_msec)
                                 nwork (+ nwork (if (= window nwind) work 0))]
                             {:window nwind, :work nwork, :msec (- window_msec msec)})))]
        (fn fn__14455
          ([nwork]
            (let [map__14456 (swap! state next_state nwork)
                  map__14456 (if (seq? map__14456)
                               (if (next map__14456)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__14456))
                                 (if (seq map__14456) (first map__14456) {}))
                               map__14456)
                  nstate map__14456
                  work (get map__14456 :work)
                  msec (get map__14456 :msec)
                  remaining_msec (- window_msec msec)]
              (when (>= work target_work) msec)))))))
  (reset-meta!
    #'create-pace-calculator
    (assoc
      {:arglists (clojure.core/list ['target-work 'window-msec]), :column (int 1)}
      :name
      'create-pace-calculator
      :ns
      *ns*))
  (def ^{:dynamic true} *pace-index-fn* nil)
  (reset-meta!
    #'*pace-index-fn*
    (assoc {:dynamic true, :column (int 1)} :name '*pace-index-fn* :ns *ns*))
  (defn build-one-seg
    ([data cnt write_handlers]
      (let [temp__5825__auto__ datomic.index/*pace-index-fn*]
        (when temp__5825__auto__ (let [f temp__5825__auto__] (^clojure.lang.IFn f 1))))
      (with-open [bos (org.fressian.impl.BytesOutputStream.)]
        (with-open [gz (java.util.zip.GZIPOutputStream. ^java.io.OutputStream bos)]
          (with-open [bs (java.io.BufferedOutputStream. ^java.io.OutputStream gz)]
            (let [data_start data
                  eatover 3
                  w (fressian/create-writer bs write_handlers)
                  _ (.writeTag ^org.fressian.Writer w "index-tdata" (unchecked-int 5))
                  _ (fressian/begin-closed-list w)
                  proc (fn proc
                         ([d]
                           (.writeObject
                             ^org.fressian.Writer w
                             (.getV ^datomic.impl.db.IDatum d))))
                  vec__14459 (let [f__14449__auto__ (fn f__14449__auto__
                                                      ([data cnt i over prev]
                                                        (cond
                                                          (nil? data) [nil 0 i]
                                                          (and
                                                            (nil? over)
                                                            (>
                                                              (+
                                                                (.length
                                                                  ^org.fressian.impl.BytesOutputStream bos)
                                                                (* eatover i))
                                                              16000)) (if
                                                                        (<
                                                                          (*
                                                                            (+
                                                                              (/
                                                                                (.length
                                                                                  ^org.fressian.impl.BytesOutputStream bos)
                                                                                i)
                                                                              eatover)
                                                                            cnt)
                                                                          (* 0.5 16000))
                                                                        (recur
                                                                          data
                                                                          cnt
                                                                          i
                                                                          true
                                                                          prev)
                                                                        (let
                                                                          [f__14449__auto__
                                                                           (fn
                                                                             f__14449__auto__
                                                                             ([data cnt i]
                                                                               (if
                                                                                 (and
                                                                                   data
                                                                                   (let
                                                                                     [d
                                                                                      (first data)]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getE
                                                                                             ^datomic.impl.db.IDatum prev))
                                                                                         (long
                                                                                           (.getE
                                                                                             ^datomic.impl.db.IDatum d)))
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum prev))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d)))
                                                                                       (zero?
                                                                                         (common/compare
                                                                                           (.getV
                                                                                             ^datomic.impl.db.IDatum prev)
                                                                                           (.getV
                                                                                             ^datomic.impl.db.IDatum d))))))
                                                                                 (do
                                                                                   (^clojure.lang.IFn proc
                                                                                     (first data))
                                                                                   (recur
                                                                                     (next data)
                                                                                     (dec cnt)
                                                                                     (inc i)))
                                                                                 [data cnt i])))]
                                                                          (^clojure.lang.IFn f__14449__auto__
                                                                            data
                                                                            cnt
                                                                            i)))
                                                          :else (do
                                                                  (let
                                                                    [d (first data)]
                                                                    (^clojure.lang.IFn proc d)
                                                                    (recur
                                                                      (next data)
                                                                      (dec cnt)
                                                                      (inc i)
                                                                      over
                                                                      d))))))]
                               (^clojure.lang.IFn f__14449__auto__ data cnt 0 nil nil))
                  data (nth vec__14459 (unchecked-int 0) nil)
                  cnt (nth vec__14459 (unchecked-int 1) nil)
                  written (nth vec__14459 (unchecked-int 2) nil)]
              (fressian/end-list w)
              (let [dvec (vec (take written data_start)) d (datomic.index/transpose dvec)]
                (.writeObject ^org.fressian.Writer w (.getEs ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.getAs ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.-ts ^datomic.index.TransposedData d))
                (.writeObject ^org.fressian.Writer w (.-ops ^datomic.index.TransposedData d))
                (.writeFooter ^org.fressian.Writer w)
                (.flush ^java.io.BufferedOutputStream bs)
                (.finish ^java.util.zip.GZIPOutputStream gz)
                (let [buf (io/bytestream->buf bos)]
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                      (.debug
                        ^org.slf4j.Logger logger
                        (logger/process
                          {:event 'index/build-one-seg,
                           :cnt cnt,
                           :written written,
                           :length
                           (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf))),
                           :bpd (quot (.remaining ^java.nio.Buffer buf) written)})))
                    nil)
                  [buf data written (nth dvec (unchecked-int (dec (count dvec)))) d]))))))))
  (reset-meta!
    #'build-one-seg
    (assoc
      {:private true, :arglists (clojure.core/list ['data 'cnt 'write-handlers]), :column (int 1)}
      :name
      'build-one-seg
      :ns
      *ns*))
  (defn bounded-count
    ([n coll] (loop [i 0 s (seq coll)] (if (and s (< i n)) (recur (inc i) (next s)) (long i)))))
  (reset-meta!
    #'bounded-count
    (assoc
      {:arglists (clojure.core/list ['n 'coll]), :column (int 1)}
      :name
      'bounded-count
      :ns
      *ns*))
  (defn build-psegs
    ([cstore olookup data es write_handlers segs_written_ref]
      (let [bound (* 2 16000)
            vec__14478 (let [f__14449__auto__ (fn f__14449__auto__
                                                ([vmap data es c]
                                                  (if data
                                                    (let [d (first data)
                                                          segid (common/rand-uuid)
                                                          vec__14482 (datomic.index/build-one-seg
                                                                       data
                                                                       (datomic.index/bounded-count
                                                                         (long bound)
                                                                         data)
                                                                       write_handlers)
                                                          buf (nth
                                                                vec__14482
                                                                (unchecked-int 0)
                                                                nil)
                                                          data (nth
                                                                 vec__14482
                                                                 (unchecked-int 1)
                                                                 nil)
                                                          written (nth
                                                                    vec__14482
                                                                    (unchecked-int 2)
                                                                    nil)
                                                          last_d (nth
                                                                   vec__14482
                                                                   (unchecked-int 3)
                                                                   nil)
                                                          td (nth vec__14482 (unchecked-int 4) nil)
                                                          vmap (assoc vmap segid buf)]
                                                      (recur
                                                        (if (> (count vmap) 100)
                                                          (do
                                                            (datomic.index/write-vals cstore vmap)
                                                            {})
                                                          vmap)
                                                        data
                                                        (conj
                                                          es
                                                          {:key d,
                                                           :segid segid,
                                                           :offset 0,
                                                           :count written,
                                                           :last-d last_d})
                                                        (inc c)))
                                                    [vmap es c])))]
                         (^clojure.lang.IFn f__14449__auto__ {} (seq data) es 0))
            vmap (nth vec__14478 (unchecked-int 0) nil)
            es (nth vec__14478 (unchecked-int 1) nil)
            c (nth vec__14478 (unchecked-int 2) nil)]
        (datomic.index/write-vals cstore vmap)
        (swap! segs_written_ref + c)
        es)))
  (reset-meta!
    #'build-psegs
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['cstore 'olookup 'data 'es 'write-handlers 'segs-written-ref]),
       :column (int 1)}
      :name
      'build-psegs
      :ns
      *ns*))
  (defn filter-nohist-pairs
    ([db data]
      (when data
        (let [d (first data)
              nohist (.-noHistory
                       (db/attribute
                         db
                         (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))))]
          (if (or (.isAssertion ^datomic.impl.db.IDatum d) (not nohist))
            (let [nd (next data)] (lazy-seq (cons d (datomic.index/filter-nohist-pairs db nd))))
            (let [n (fnext data)]
              (if (and
                    n
                    (.isAssertion ^datomic.impl.db.IDatum n)
                    (=
                      (long (.getE ^datomic.impl.db.IDatum d))
                      (long (.getE ^datomic.impl.db.IDatum n)))
                    (=
                      (long (.getA ^datomic.impl.db.IDatum d))
                      (long (.getA ^datomic.impl.db.IDatum n)))
                    (zero?
                      (common/compare
                        (.getV ^datomic.impl.db.IDatum d)
                        (.getV ^datomic.impl.db.IDatum n))))
                (recur db (nnext data))
                (lazy-seq (cons d (datomic.index/filter-nohist-pairs db (next data)))))))))))
  (reset-meta!
    #'filter-nohist-pairs
    (assoc
      {:arglists (clojure.core/list ['db 'data]), :column (int 1)}
      :name
      'filter-nohist-pairs
      :ns
      *ns*))
  (defn separating-retractions
    ([db retref data]
      (when data
        (let [d (first data)]
          (if (.isAssertion ^datomic.impl.db.IDatum d)
            (let [nd (next data)]
              (lazy-seq (cons d (datomic.index/separating-retractions db retref nd))))
            (let [n (fnext data)
                  nohist (.-noHistory
                           (db/attribute
                             db
                             (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))))]
              (if (and
                    n
                    (.isAssertion ^datomic.impl.db.IDatum n)
                    (=
                      (long (.getE ^datomic.impl.db.IDatum d))
                      (long (.getE ^datomic.impl.db.IDatum n)))
                    (=
                      (long (.getA ^datomic.impl.db.IDatum d))
                      (long (.getA ^datomic.impl.db.IDatum n)))
                    (zero?
                      (common/compare
                        (.getV ^datomic.impl.db.IDatum d)
                        (.getV ^datomic.impl.db.IDatum n))))
                (do (when-not nohist (swap! retref conj d n)) (recur db retref (nnext data)))
                (do (when-not nohist (swap! retref conj d)) (recur db retref (next data))))))))))
  (reset-meta!
    #'separating-retractions
    (assoc
      {:arglists (clojure.core/list ['db 'retref 'data]), :column (int 1)}
      :name
      'separating-retractions
      :ns
      *ns*))
  (defn fully-take-while-delivering-tail
    ([p pred coll]
      (lazy-seq
        (let [temp__5823__auto__ (seq coll)]
          (if temp__5823__auto__
            (let [s temp__5823__auto__ fst (first s) rst (rest s)]
              (if (^clojure.lang.IFn pred fst)
                (cons fst (datomic.index/fully-take-while-delivering-tail p pred rst))
                (do (deliver p s) nil)))
            (do (deliver p nil) nil))))))
  (reset-meta!
    #'fully-take-while-delivering-tail
    (assoc
      {:arglists (clojure.core/list ['p 'pred 'coll]), :column (int 1)}
      :name
      'fully-take-while-delivering-tail
      :ns
      *ns*))
  (defn fully-partition-by
    ([f coll]
      (letfn
        [(fpb
           [f pcoll]
           (lazy-seq
             (let [coll (deref pcoll) temp__5825__auto__ (seq coll)]
               (when temp__5825__auto__
                 (let [s temp__5825__auto__
                       fst (first s)
                       fv (^clojure.lang.IFn f fst)
                       pcoll (promise)]
                   (cons
                     (datomic.index/fully-take-while-delivering-tail
                       pcoll
                       (fn fn__14512 ([p1__14509#] (= fv (^clojure.lang.IFn f p1__14509#))))
                       s)
                     (^clojure.lang.IFn fpb f pcoll)))))))]
        (^clojure.lang.IFn fpb f (atom coll)))))
  (reset-meta!
    #'fully-partition-by
    (assoc
      {:arglists (clojure.core/list ['f 'coll]), :column (int 1)}
      :name
      'fully-partition-by
      :ns
      *ns*))
  (defn fred
    ([f ret coll]
      (let [temp__5823__auto__ (seq coll)]
        (if temp__5823__auto__
          (let [xs temp__5823__auto__ x (first xs) rst (rest xs)]
            (recur f (^clojure.lang.IFn f ret x) rst))
          ret))))
  (reset-meta!
    #'fred
    (assoc {:arglists (clojure.core/list ['f 'ret 'coll]), :column (int 1)} :name 'fred :ns *ns*))
  (defn build-segs
    ([db cstore olookup data es retractions partfn write_handlers segs_written_ref]
      (let [retref (atom retractions)
            data (datomic.index/filter-nohist-pairs db data)
            data (if retractions (datomic.index/separating-retractions db retref data) data)
            pdata (datomic.index/fully-partition-by partfn data)
            es (datomic.index/fred
                 (fn fn__14520
                   ([es pd]
                     (datomic.index/build-psegs
                       cstore
                       olookup
                       pd
                       es
                       write_handlers
                       segs_written_ref)))
                 es
                 pdata)]
        [es (deref retref)])))
  (reset-meta!
    #'build-segs
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['db 'cstore 'olookup 'data 'es 'retractions 'partfn 'write-handlers 'segs-written-ref]),
       :column (int 1)}
      :name
      'build-segs
      :ns
      *ns*))
  (defn merge-data
    ([cmp ds1 ds2]
      (if (and ds1 ds2)
        (lazy-seq
          (let [d1 (first ds1) d2 (first ds2) c (.compare ^java.util.Comparator cmp d1 d2)]
            (cond
              (< c 0) (cons d1 (datomic.index/merge-data cmp (next ds1) ds2))
              (> c 0) (cons d2 (datomic.index/merge-data cmp ds1 (next ds2)))
              :else (do (cons d1 (datomic.index/merge-data cmp (next ds1) (next ds2)))))))
        (or ds1 ds2))))
  (reset-meta!
    #'merge-data
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'cmp {:tag 'Comparator}) 'ds1 'ds2]),
       :column (int 1)}
      :name
      'merge-data
      :ns
      *ns*))
  (def DIR_PARTITION_SIZE {:eavt 2400, :aevt 2400, :avet 1000, :raet 2400})
  (reset-meta!
    #'DIR_PARTITION_SIZE
    (assoc {:private true, :column (int 1)} :name 'DIR_PARTITION_SIZE :ns *ns*))
  (def MEM_TO_STG_RATIO 0.07)
  (reset-meta!
    #'MEM_TO_STG_RATIO
    (assoc {:private true, :column (int 1)} :name 'MEM_TO_STG_RATIO :ns *ns*))
  (def BYTES_PER_SEG 45000)
  (reset-meta!
    #'BYTES_PER_SEG
    (assoc {:private true, :column (int 1)} :name 'BYTES_PER_SEG :ns *ns*))
  (def MID_INDEX_THRESHOLD_FACTOR 10)
  (reset-meta!
    #'MID_INDEX_THRESHOLD_FACTOR
    (assoc {:private true, :column (int 1)} :name 'MID_INDEX_THRESHOLD_FACTOR :ns *ns*))
  (def MIN_MAIN_INDEX_THRESHOLD 20000000)
  (reset-meta!
    #'MIN_MAIN_INDEX_THRESHOLD
    (assoc {:private true, :column (int 1)} :name 'MIN_MAIN_INDEX_THRESHOLD :ns *ns*))
  (def SLICE_STEP 1)
  (reset-meta! #'SLICE_STEP (assoc {:private true, :column (int 1)} :name 'SLICE_STEP :ns *ns*))
  (defn dir-partition-size
    ([idx]
      (let [scale (config/property "datomic.indexDirScale")]
        (* scale (^clojure.lang.IFn idx datomic.index/DIR_PARTITION_SIZE)))))
  (reset-meta!
    #'dir-partition-size
    (assoc
      {:private true, :arglists (clojure.core/list ['idx]), :column (int 1)}
      :name
      'dir-partition-size
      :ns
      *ns*))
  (defn make-sparse-lt
    ([cmp]
      (fn fn__14529
        ([k1 k2]
          (when-not (and
                      (= (long (.getE ^datomic.db.Datum k1)) (long (.getE ^datomic.db.Datum k2)))
                      (= (long (.getA ^datomic.db.Datum k1)) (long (.getA ^datomic.db.Datum k2)))
                      (zero?
                        (common/compare
                          (.getV ^datomic.db.Datum k1)
                          (.getV ^datomic.db.Datum k2))))
            (neg? (.compare ^java.util.Comparator cmp k1 k2)))))))
  (reset-meta!
    #'make-sparse-lt
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cmp {:tag 'Comparator})]), :column (int 1)}
      :name
      'make-sparse-lt
      :ns
      *ns*))
  (defn strdiff
    ([mins maxs]
      (loop [i 0]
        (if (< i (.length ^java.lang.String maxs))
          (if (or
                (= (long i) (long (.length ^java.lang.String mins)))
                (not=
                  (java.lang.Character/valueOf
                    (char (.charAt ^java.lang.String mins (unchecked-int i))))
                  (java.lang.Character/valueOf
                    (char (.charAt ^java.lang.String maxs (unchecked-int i))))))
            (.substring ^java.lang.String maxs (unchecked-int 0) (unchecked-int (inc i)))
            (recur (inc i)))
          maxs))))
  (reset-meta!
    #'strdiff
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'mins {:tag 'String}) (.withMeta 'maxs {:tag 'String})]),
       :column (int 1)}
      :name
      'strdiff
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.index" "mindiff") {:declared true, :column (int 1)})
  (defn vecdiff
    ([minv maxv]
      (loop [i 0]
        (if (< i (count maxv))
          (cond
            (= i (count minv)) (subvec maxv 0 (long (inc i)))
            (not (zero? (common/compare (get minv (long i)) (get maxv (long i))))) (conj
                                                                                     (subvec
                                                                                       maxv
                                                                                       0
                                                                                       (long i))
                                                                                     (datomic.index/mindiff
                                                                                       (get
                                                                                         minv
                                                                                         (long i))
                                                                                       (get
                                                                                         maxv
                                                                                         (long
                                                                                           i))))
            :default (do (recur (inc i))))
          maxv))))
  (reset-meta!
    #'vecdiff
    (assoc {:arglists (clojure.core/list ['minv 'maxv]), :column (int 1)} :name 'vecdiff :ns *ns*))
  (defn mindiff
    ([minv maxv]
      (cond
        (and (string? minv) (string? maxv)) (datomic.index/strdiff minv maxv)
        (and (vector? minv) (vector? maxv)) (datomic.index/vecdiff minv maxv)
        :default (do maxv))))
  (reset-meta!
    #'mindiff
    (assoc {:arglists (clojure.core/list ['minv 'maxv]), :column (int 1)} :name 'mindiff :ns *ns*))
  (defn sparse-datom
    ([idx prior d]
      (let [maked (if (.isAssertion ^datomic.impl.db.IDatum d)
                    db/asserting-datum
                    db/retracting-datum)
            G__14540 idx]
        (case
          G__14540
          :avet
          (cond
            (not=
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                                         0
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d)))
                                                                                         nil
                                                                                         0)
            :else (do
                    (let [dv (.getV ^datomic.impl.db.IDatum d)
                          diff_v (datomic.index/mindiff (.getV ^datomic.impl.db.IDatum prior) dv)]
                      (if (not= diff_v dv)
                        (^clojure.lang.IFn maked
                          0
                          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                          diff_v
                          0)
                        d))))
          :raet
          (cond
            (not
              (zero?
                (common/compare
                  (.getV ^datomic.impl.db.IDatum d)
                  (.getV ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                             0
                                                             0
                                                             (.getV ^datomic.impl.db.IDatum d)
                                                             0)
            (not=
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                                         0
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d)))
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         0)
            :else (do d))
          :eavt
          (cond
            (not=
              (long (.getE ^datomic.impl.db.IDatum d))
              (long (.getE ^datomic.impl.db.IDatum prior))) (^clojure.lang.IFn maked
                                                              (long
                                                                (.getE ^datomic.impl.db.IDatum d))
                                                              0
                                                              nil
                                                              0)
            (not=
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                                         (long
                                                                                           (.getE
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d)))
                                                                                         nil
                                                                                         0)
            :else (do
                    (let [dv (.getV ^datomic.impl.db.IDatum d)
                          diff_v (datomic.index/mindiff (.getV ^datomic.impl.db.IDatum prior) dv)]
                      (if (not= diff_v dv)
                        (^clojure.lang.IFn maked
                          (long (.getE ^datomic.impl.db.IDatum d))
                          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                          diff_v
                          0)
                        d))))
          :aevt
          (cond
            (not=
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
              (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                                         0
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d)))
                                                                                         nil
                                                                                         0)
            (not=
              (long (.getE ^datomic.impl.db.IDatum d))
              (long (.getE ^datomic.impl.db.IDatum prior))) (^clojure.lang.IFn maked
                                                              (long
                                                                (.getE ^datomic.impl.db.IDatum d))
                                                              (java.lang.Integer/valueOf
                                                                (int
                                                                  (.getA
                                                                    ^datomic.impl.db.IDatum d)))
                                                              nil
                                                              0)
            :else (do
                    (let [dv (.getV ^datomic.impl.db.IDatum d)
                          diff_v (datomic.index/mindiff (.getV ^datomic.impl.db.IDatum prior) dv)]
                      (if (not= diff_v dv)
                        (^clojure.lang.IFn maked
                          (long (.getE ^datomic.impl.db.IDatum d))
                          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                          diff_v
                          0)
                        d))))))))
  (reset-meta!
    #'sparse-datom
    (assoc
      {:arglists
       (clojure.core/list ['idx (.withMeta 'prior {:tag 'IDatum}) (.withMeta 'd {:tag 'IDatum})]),
       :column (int 1)}
      :name
      'sparse-datom
      :ns
      *ns*))
  (defn seg-last-datom
    ([olookup segid]
      (let [seg_data (cache/getx-uncached olookup segid)]
        (nth seg_data (unchecked-int (dec (count seg_data)))))))
  (reset-meta!
    #'seg-last-datom
    (assoc
      {:arglists (clojure.core/list ['olookup 'segid]), :column (int 1)}
      :name
      'seg-last-datom
      :ns
      *ns*))
  (defn sparse-e-xf
    ([olookup idx]
      (let [vprev (volatile! nil)]
        (fn fn__14543
          ([rf]
            (fn fn__14544
              ([] (^clojure.lang.IFn rf))
              ([result] (^clojure.lang.IFn rf result))
              ([result entry]
                (let [prev_entry (deref vprev)]
                  (vreset! vprev entry)
                  (if (and prev_entry (or (:last-d entry) (:last-d prev_entry)))
                    (let [d (:key entry)
                          prior (or
                                  (:last-d prev_entry)
                                  (datomic.index/seg-last-datom olookup (:segid prev_entry)))]
                      (^clojure.lang.IFn rf
                        result
                        (assoc entry :key (datomic.index/sparse-datom idx prior d))))
                    (^clojure.lang.IFn rf result entry))))))))))
  (reset-meta!
    #'sparse-e-xf
    (assoc
      {:arglists (clojure.core/list ['olookup 'idx]), :column (int 1)}
      :name
      'sparse-e-xf
      :ns
      *ns*))
  (defn sparse-es-2 ([olookup idx es] (into [] (datomic.index/sparse-e-xf olookup idx) es)))
  (reset-meta!
    #'sparse-es-2
    (assoc
      {:arglists (clojure.core/list ['olookup 'idx 'es]), :column (int 1)}
      :name
      'sparse-es-2
      :ns
      *ns*))
  (defn sparse-es
    ([olookup idx es]
      (mapv
        (fn fn__14552
          ([i]
            (let [e (nth es (unchecked-int ^java.lang.Number i))]
              (if (and
                    (clojure.lang.Numbers/isPos i)
                    (or (:last-d e) (:last-d (nth es (unchecked-int (dec i))))))
                (let [d (:key e)
                      prev_e (nth es (unchecked-int (dec i)))
                      prior (or
                              (:last-d prev_e)
                              (let [seg_data (cache/getx-uncached olookup (:segid prev_e))]
                                (nth seg_data (unchecked-int (dec (count seg_data))))))
                      maked (if (.isAssertion ^datomic.impl.db.IDatum d)
                              db/asserting-datum
                              db/retracting-datum)]
                  (assoc
                    e
                    :key
                    (let [G__14553 idx]
                      (case
                        G__14553
                        :avet
                        (cond
                          (not=
                            (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                            (java.lang.Integer/valueOf
                              (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                              0
                                                                              (java.lang.Integer/valueOf
                                                                                (int
                                                                                  (.getA
                                                                                    ^datomic.impl.db.IDatum d)))
                                                                              nil
                                                                              0)
                          :else (do
                                  (let [dv (.getV ^datomic.impl.db.IDatum d)
                                        diff_v (datomic.index/mindiff
                                                 (.getV ^datomic.impl.db.IDatum prior)
                                                 dv)]
                                    (if (not= diff_v dv)
                                      (^clojure.lang.IFn maked
                                        0
                                        (java.lang.Integer/valueOf
                                          (int (.getA ^datomic.impl.db.IDatum d)))
                                        diff_v
                                        0)
                                      d))))
                        :raet
                        (cond
                          (not
                            (zero?
                              (common/compare
                                (.getV ^datomic.impl.db.IDatum d)
                                (.getV ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                           0
                                                                           0
                                                                           (.getV
                                                                             ^datomic.impl.db.IDatum d)
                                                                           0)
                          (not=
                            (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                            (java.lang.Integer/valueOf
                              (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                              0
                                                                              (java.lang.Integer/valueOf
                                                                                (int
                                                                                  (.getA
                                                                                    ^datomic.impl.db.IDatum d)))
                                                                              (.getV
                                                                                ^datomic.impl.db.IDatum d)
                                                                              0)
                          :else (do d))
                        :eavt
                        (cond
                          (not=
                            (long (.getE ^datomic.impl.db.IDatum d))
                            (long (.getE ^datomic.impl.db.IDatum prior))) (^clojure.lang.IFn maked
                                                                            (long
                                                                              (.getE
                                                                                ^datomic.impl.db.IDatum d))
                                                                            0
                                                                            nil
                                                                            0)
                          (not=
                            (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                            (java.lang.Integer/valueOf
                              (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                              (long
                                                                                (.getE
                                                                                  ^datomic.impl.db.IDatum d))
                                                                              (java.lang.Integer/valueOf
                                                                                (int
                                                                                  (.getA
                                                                                    ^datomic.impl.db.IDatum d)))
                                                                              nil
                                                                              0)
                          :else (do
                                  (let [dv (.getV ^datomic.impl.db.IDatum d)
                                        diff_v (datomic.index/mindiff
                                                 (.getV ^datomic.impl.db.IDatum prior)
                                                 dv)]
                                    (if (not= diff_v dv)
                                      (^clojure.lang.IFn maked
                                        (long (.getE ^datomic.impl.db.IDatum d))
                                        (java.lang.Integer/valueOf
                                          (int (.getA ^datomic.impl.db.IDatum d)))
                                        diff_v
                                        0)
                                      d))))
                        :aevt
                        (cond
                          (not=
                            (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                            (java.lang.Integer/valueOf
                              (int (.getA ^datomic.impl.db.IDatum prior)))) (^clojure.lang.IFn maked
                                                                              0
                                                                              (java.lang.Integer/valueOf
                                                                                (int
                                                                                  (.getA
                                                                                    ^datomic.impl.db.IDatum d)))
                                                                              nil
                                                                              0)
                          (not=
                            (long (.getE ^datomic.impl.db.IDatum d))
                            (long (.getE ^datomic.impl.db.IDatum prior))) (^clojure.lang.IFn maked
                                                                            (long
                                                                              (.getE
                                                                                ^datomic.impl.db.IDatum d))
                                                                            (java.lang.Integer/valueOf
                                                                              (int
                                                                                (.getA
                                                                                  ^datomic.impl.db.IDatum d)))
                                                                            nil
                                                                            0)
                          :else (do
                                  (let [dv (.getV ^datomic.impl.db.IDatum d)
                                        diff_v (datomic.index/mindiff
                                                 (.getV ^datomic.impl.db.IDatum prior)
                                                 dv)]
                                    (if (not= diff_v dv)
                                      (^clojure.lang.IFn maked
                                        (long (.getE ^datomic.impl.db.IDatum d))
                                        (java.lang.Integer/valueOf
                                          (int (.getA ^datomic.impl.db.IDatum d)))
                                        diff_v
                                        0)
                                      d))))))))
                e))))
        (range (java.lang.Integer/valueOf (int (count es)))))))
  (reset-meta!
    #'sparse-es
    (assoc
      {:arglists (clojure.core/list ['olookup 'idx 'es]), :column (int 1)}
      :name
      'sparse-es
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.index" "index-parallelism") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.index" "index-parallelism")
    (atom 1 :validator (fn fn__14560 ([p1__14559#] (<= 1 p1__14559#)))))
  (defn serialize-dir
    ([des]
      [(:key (first des))
       (datomic.index/fress
         (let [keydata (datomic.index/transpose (vec (map :key des)))
               segids (to-array (map :segid des))
               offsets (into-array java.lang.Integer/TYPE (map :offset des))
               counts (into-array java.lang.Integer/TYPE (map :count des))]
           (datomic.index/dir-node keydata segids offsets counts))
         datomic.index/common-write-handlers)]))
  (reset-meta!
    #'serialize-dir
    (assoc {:arglists (clojure.core/list ['des]), :column (int 1)} :name 'serialize-dir :ns *ns*))
  (defn conjable-on-channel
    ([ch error_ch]
      (reify
        clojure.lang.IPersistentCollection
        (^clojure.lang.IPersistentCollection cons
          [this o]
          (let [vec__14564 (a/alts!! [[ch o] error_ch] :priority true)
                v (nth vec__14564 (unchecked-int 0) nil)
                sc (nth vec__14564 (unchecked-int 1) nil)]
            (when (identical? sc error_ch)
              (when (instance? java.lang.Throwable v) (throw ^java.lang.Throwable v))
              (throw (java.lang.IllegalStateException. "Aborted")))
            (if v this (do (throw (java.lang.IllegalStateException. "Channel closed")) nil))))
        (^boolean equiv [this o] (identical? this o)))))
  (reset-meta!
    #'conjable-on-channel
    (assoc
      {:arglists (clojure.core/list ['ch 'error-ch]), :column (int 1)}
      :name
      'conjable-on-channel
      :ns
      *ns*))
  (defn es-equal?
    ([cmp left_es right_es]
      (and
        (= (long (count left_es)) (long (count right_es)))
        (reduce
          (fn fn__14569 ([acc v] (if v acc (reduced false))))
          true
          (map
            (fn fn__14571
              ([l r]
                (and
                  (= (:segid l) (:segid r))
                  (== 0 (.compare ^java.util.Comparator cmp (:key l) (:key r))))))
            left_es
            right_es)))))
  (reset-meta!
    #'es-equal?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cmp {:tag 'Comparator}) 'left-es 'right-es]),
       :column (int 1)}
      :name
      'es-equal?
      :ns
      *ns*))
  (defn aligned-dedup
    ([dirs cmp]
      (fn fn__14576
        ([rf]
          (let [dref (volatile! (seq dirs))]
            (fn fn__14577
              ([] (^clojure.lang.IFn rf))
              ([acc] (^clojure.lang.IFn rf acc))
              ([acc dir_entries]
                (let [k (:key (first dir_entries))
                      newdir [k (common/rand-uuid) dir_entries]
                      result
                      (loop []
                        (if-let [[dir-k dir-id dir-es] (first @dref)]
                          (case (Long/signum
                                  (.compare ^java.util.Comparator cmp k dir-k))
                            -1 newdir
                            0 (if (es-equal? cmp dir_entries dir-es)
                                [dir-k dir-id]
                                newdir)
                            1 (do
                                (vswap! ^clojure.lang.Volatile dref next)
                                (recur)))
                          newdir))]
                  (^clojure.lang.IFn rf acc result)))))))))
  (reset-meta!
    #'aligned-dedup
    (assoc
      {:arglists (clojure.core/list ['dirs (.withMeta 'cmp {:tag 'Comparator})]), :column (int 1)}
      :name
      'aligned-dedup
      :ns
      *ns*))
  (defn partition-at-existing-keydata
    ([dir_partition_size cmp root_keys]
      (fn fn__14588
        ([rf]
          (let [cur_dir (java.util.ArrayList.)
                root_keys* (volatile! (seq root_keys))
                dir_partition_size (long dir_partition_size)
                max_size (long (inc (* dir_partition_size (/ 3 2))))
                min_size (long (dec (* dir_partition_size (/ 1 2))))]
            (fn fn__14590
              ([] (^clojure.lang.IFn rf))
              ([acc]
                (^clojure.lang.IFn rf
                  (if (> (.size ^java.util.ArrayList cur_dir) 0)
                    (let [last_dir (vec cur_dir)]
                      (.clear ^java.util.ArrayList cur_dir)
                      (unreduced (^clojure.lang.IFn rf acc last_dir)))
                    acc)))
              ([acc p__14589]
                (let [map__14591 p__14589
                      map__14591 (if (seq? map__14591)
                                   (if (next map__14591)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__14591))
                                     (if (seq map__14591) (first map__14591) {}))
                                   map__14591)
                      dir_entry map__14591
                      key (get map__14591 :key)
                      next_k (first (deref root_keys*))]
                  (if (and next_k (> (.compare ^java.util.Comparator cmp key next_k) -1))
                    (let [acc (if (>= (.size ^java.util.ArrayList cur_dir) min_size)
                                (let [acc (^clojure.lang.IFn rf acc (vec cur_dir))]
                                  (.clear ^java.util.ArrayList cur_dir)
                                  acc)
                                acc)]
                      (vswap! ^clojure.lang.Volatile root_keys* next)
                      (.add ^java.util.ArrayList cur_dir dir_entry)
                      acc)
                    (do
                      (.add ^java.util.ArrayList cur_dir dir_entry)
                      (if (>= (.size ^java.util.ArrayList cur_dir) max_size)
                        (let [des (.subList
                                    ^java.util.ArrayList cur_dir
                                    (unchecked-int 0)
                                    (unchecked-int dir_partition_size))
                              acc (^clojure.lang.IFn rf acc (vec des))]
                          (.clear ^java.util.List des)
                          acc)
                        acc)))))))))))
  (reset-meta!
    #'partition-at-existing-keydata
    (assoc
      {:arglists
       (clojure.core/list ['dir-partition-size (.withMeta 'cmp {:tag 'Comparator}) 'root-keys]),
       :column (int 1)}
      :name
      'partition-at-existing-keydata
      :ns
      *ns*))
  (defn existing-dirs
    ([olookup root]
      (when root
        (map
          (fn fn__14596
            ([ridx]
              (let [dir (datomic.index/get-dir-node root ridx olookup true)
                    key (nth
                          (.-keydata ^datomic.index.RootNode root)
                          (unchecked-int ^java.lang.Number ridx))
                    id (nth
                         (.-dirids ^datomic.index.RootNode root)
                         (unchecked-int ^java.lang.Number ridx))
                    des (mapv
                          (fn fn__14597
                            ([didx]
                              {:key
                               (nth
                                 (.-keydata ^datomic.index.DirNode dir)
                                 (unchecked-int ^java.lang.Number didx)),
                               :segid
                               (nth
                                 (.-segids ^datomic.index.DirNode dir)
                                 (unchecked-int ^java.lang.Number didx))}))
                          (range
                            (java.lang.Integer/valueOf
                              (int (count (.-segids ^datomic.index.DirNode dir))))))]
                [key id des])))
          (range
            (java.lang.Integer/valueOf (int (count (.-dirids ^datomic.index.RootNode root)))))))))
  (reset-meta!
    #'existing-dirs
    (assoc
      {:private true,
       :arglists (clojure.core/list ['olookup (.withMeta 'root {:tag 'RootNode})]),
       :column (int 1)}
      :name
      'existing-dirs
      :ns
      *ns*))
  (defn partition-dirs
    ([dirnode_size cmp root]
      (datomic.index/partition-at-existing-keydata
        dirnode_size
        cmp
        (when root (.-keydata ^datomic.index.RootNode root)))))
  (reset-meta!
    #'partition-dirs
    (assoc
      {:arglists (clojure.core/list ['dirnode-size 'cmp (.withMeta 'root {:tag 'RootNode})]),
       :column (int 1)}
      :name
      'partition-dirs
      :ns
      *ns*))
  (defn merge-dedup
    ([olookup cmp root]
      (datomic.index/aligned-dedup (datomic.index/existing-dirs olookup root) cmp)))
  (reset-meta!
    #'merge-dedup
    (assoc
      {:arglists (clojure.core/list ['olookup 'cmp 'root]), :column (int 1)}
      :name
      'merge-dedup
      :ns
      *ns*))
  (defn ch->seq
    ([ch]
      (lazy-seq
        (let [temp__5829__auto__ (a/<!! ch)]
          (when-not (nil? temp__5829__auto__)
            (let [v temp__5829__auto__] (cons v (datomic.index/ch->seq ch))))))))
  (reset-meta!
    #'ch->seq
    (assoc {:arglists (clojure.core/list ['ch]), :column (int 1)} :name 'ch->seq :ns *ns*))
  (defn start-dirs-pipeline
    ([& p__14607]
      (let [map__14608 p__14607
            map__14608 (if (seq? map__14608)
                         (if (next map__14608)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__14608))
                           (if (seq map__14608) (first map__14608) {}))
                         map__14608)
            cstore (get map__14608 :cstore)
            olookup (get map__14608 :olookup)
            idx (get map__14608 :idx)
            dirnode_size (get map__14608 :dirnode-size)
            dirs_ahead (get map__14608 :dirs-ahead)
            cmp (get map__14608 :cmp)
            root (get map__14608 :root)
            dir_entries_ch (a/chan dirs_ahead)
            error_ch (a/promise-chan)
            es (datomic.index/conjable-on-channel dir_entries_ch error_ch)
            maybe_write_dir! (fn maybe_write_dir_BANG_
                               ([p__14609]
                                 (let [vec__14611 p__14609
                                       key (nth vec__14611 (unchecked-int 0) nil)
                                       dirid (nth vec__14611 (unchecked-int 1) nil)
                                       des (nth vec__14611 (unchecked-int 2) nil)]
                                   (when des
                                     (let [vec__14614 (datomic.index/serialize-dir des)
                                           _ (nth vec__14614 (unchecked-int 0) nil)
                                           dbuf (nth vec__14614 (unchecked-int 1) nil)]
                                       (datomic.index/write-vals cstore {dirid dbuf})))
                                   [key dirid])))
            fut (future-call
                  (fn fn__14618
                    ([]
                      (try
                        (into
                          []
                          (comp
                            (datomic.index/sparse-e-xf olookup idx)
                            (datomic.index/partition-dirs dirnode_size cmp root)
                            (datomic.index/merge-dedup olookup cmp root)
                            (map maybe_write_dir!))
                          (datomic.index/ch->seq dir_entries_ch))
                        (catch
                          java.lang.Throwable
                          t
                          (do
                            (a/>!! error_ch t)
                            (a/close! dir_entries_ch)
                            (throw ^java.lang.Throwable t)
                            nil))))))]
        [es dir_entries_ch fut])))
  (reset-meta!
    #'start-dirs-pipeline
    (assoc
      {:arglists
       (clojure.core/list
         ['& {:keys ['cstore 'olookup 'idx 'dirnode-size 'dirs-ahead 'cmp 'root]}]),
       :column (int 1)}
      :name
      'start-dirs-pipeline
      :ns
      *ns*))
  (defn merge-one-index
    ([db
      cstore
      olookup
      old_root_id
      data
      garbage_ids
      partfn
      cmp
      write_handlers
      filter_retractions?
      as_of_t
      idx
      xpreds
      filter_segids
      xcmp]
      'merge-one-index
      (try
        (let [pario (config/property "datomic.indexIOParallelism")
              cstore (if pario
                       (cluster/queueing-writer
                         cstore
                         pario
                         cluster/BOUNDING_TIMEOUT_MSEC
                         (fn fn__14633
                           ([p1__14621#] (monitor/add-stat :IndexIOQueueCount p1__14621#))))
                       cstore)
              lt (datomic.index/make-sparse-lt cmp)
              old_root (when old_root_id (common/getx olookup old_root_id))
              vec__14627 (datomic.index/start-dirs-pipeline
                           :cstore
                           cstore
                           :olookup
                           olookup
                           :idx
                           idx
                           :dirnode-size
                           (datomic.index/dir-partition-size idx)
                           :dirs-ahead
                           32
                           :root
                           old_root
                           :cmp
                           cmp)
              es (nth vec__14627 (unchecked-int 0) nil)
              des_ch (nth vec__14627 (unchecked-int 1) nil)
              rootnode_fut (nth vec__14627 (unchecked-int 2) nil)
              retractions (when filter_retractions? [])
              xsegs (when old_root_id
                      (let [index (datomic.index/lookup-index olookup xcmp old_root_id)]
                        (reduce
                          (fn fn__14635
                            ([xsegs p]
                              (into
                                xsegs
                                (filter
                                  identity
                                  (map
                                    (fn fn__14636
                                      ([p1__14622#] (datomic.index/seek-seg index p1__14622#)))
                                    (x/datoms p))))))
                          #{}
                          xpreds)))
              _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process
                        {:event :index/xsegs,
                         :count (java.lang.Integer/valueOf (int (count xsegs))),
                         :idx idx})))
                  nil)
              _ (monitor/add-stat :ExciseSegments (java.lang.Integer/valueOf (int (count xsegs))))
              excise? (fn excise_QMARK_
                        ([d] (some (fn fn__14640 ([p1__14623#] (x/remove? p1__14623# d))) xpreds)))
              garbage_ids (into garbage_ids xsegs)
              segs_written_ref (atom 0)
              dirs_written_ref (atom 0)
              dirs (when old_root
                     (map
                       (fn fn__14643 ([p1__14624#] (cache/getx-uncached olookup p1__14624#)))
                       (.-dirids ^datomic.index.RootNode old_root)))
              mkdes (fn mkdes
                      ([dir]
                        (reduce
                          (fn fn__14646
                            ([ret i]
                              (let [segid (aget
                                            (.-segids ^datomic.index.DirNode dir)
                                            (unchecked-int i))]
                                (cond
                                  (contains? filter_segids segid) ret
                                  (contains? xsegs segid) (let [pd
                                                                (remove
                                                                  excise?
                                                                  (cache/getx-uncached
                                                                    olookup
                                                                    segid))]
                                                            (if
                                                              (seq pd)
                                                              (let
                                                                [ret
                                                                 (into
                                                                   ret
                                                                   (datomic.index/build-psegs
                                                                     cstore
                                                                     olookup
                                                                     pd
                                                                     []
                                                                     write_handlers
                                                                     segs_written_ref))]
                                                                (when
                                                                  pario
                                                                  (common/bounded-deref
                                                                    (cluster/sync-writes cstore)
                                                                    cluster/BOUNDING_TIMEOUT_MSEC))
                                                                ret)
                                                              ret))
                                  :else (do
                                          (conj
                                            ret
                                            {:key
                                             (.get
                                               (.-keydata ^datomic.index.DirNode dir)
                                               (unchecked-int ^java.lang.Number i)),
                                             :segid segid,
                                             :offset
                                             (java.lang.Integer/valueOf
                                               (int
                                                 (aget
                                                   (.-offsets ^datomic.index.DirNode dir)
                                                   (unchecked-int i)))),
                                             :count
                                             (java.lang.Integer/valueOf
                                               (int
                                                 (aget
                                                   (.-counts ^datomic.index.DirNode dir)
                                                   (unchecked-int i))))}))))))
                          []
                          (range
                            (java.lang.Integer/valueOf
                              (int (count (.-segids ^datomic.index.DirNode dir))))))))
              drainq (fn drainq
                       ([es rs erq]
                         (if (empty? erq)
                           [es rs erq]
                           (let [vec__14650 (deref (peek erq))
                                 nes (nth vec__14650 (unchecked-int 0) nil)
                                 nrs (nth vec__14650 (unchecked-int 1) nil)]
                             (recur (into es nes) (into rs nrs) (pop erq))))))
              vec__14630 (let [f__14449__auto__ (fn f__14449__auto__
                                                  ([es garbage retractions des data erq]
                                                    (cond
                                                      (nil? (seq data)) (do
                                                                          (let
                                                                            [logger
                                                                             (org.slf4j.LoggerFactory/getLogger
                                                                               "datomic.index")]
                                                                            (when
                                                                              (.isDebugEnabled
                                                                                ^org.slf4j.Logger logger)
                                                                              (.debug
                                                                                ^org.slf4j.Logger logger
                                                                                (logger/process
                                                                                  {:event
                                                                                   'index/merge-one-index,
                                                                                   :stage
                                                                                   :nil-data})))
                                                                            nil)
                                                                          (let
                                                                            [vec__14655
                                                                             (^clojure.lang.IFn drainq
                                                                               es
                                                                               retractions
                                                                               erq)
                                                                             es
                                                                             (nth
                                                                               vec__14655
                                                                               (unchecked-int 0)
                                                                               nil)
                                                                             retractions
                                                                             (nth
                                                                               vec__14655
                                                                               (unchecked-int 1)
                                                                               nil)
                                                                             erq
                                                                             (nth
                                                                               vec__14655
                                                                               (unchecked-int 2)
                                                                               nil)]
                                                                            [(into es des)
                                                                             garbage
                                                                             retractions]))
                                                      (nil? (seq des)) (do
                                                                         (let
                                                                           [logger
                                                                            (org.slf4j.LoggerFactory/getLogger
                                                                              "datomic.index")]
                                                                           (when
                                                                             (.isDebugEnabled
                                                                               ^org.slf4j.Logger logger)
                                                                             (.debug
                                                                               ^org.slf4j.Logger logger
                                                                               (logger/process
                                                                                 {:event
                                                                                  'index/merge-one-index,
                                                                                  :stage
                                                                                  :nil-des})))
                                                                           nil)
                                                                         (let
                                                                           [vec__14658
                                                                            (^clojure.lang.IFn drainq
                                                                              es
                                                                              retractions
                                                                              erq)
                                                                            es
                                                                            (nth
                                                                              vec__14658
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            retractions
                                                                            (nth
                                                                              vec__14658
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            erq
                                                                            (nth
                                                                              vec__14658
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            vec__14661
                                                                            (datomic.index/build-segs
                                                                              db
                                                                              cstore
                                                                              olookup
                                                                              data
                                                                              es
                                                                              retractions
                                                                              partfn
                                                                              write_handlers
                                                                              segs_written_ref)
                                                                            es
                                                                            (nth
                                                                              vec__14661
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            retractions
                                                                            (nth
                                                                              vec__14661
                                                                              (unchecked-int 1)
                                                                              nil)]
                                                                           [es
                                                                            garbage
                                                                            retractions]))
                                                      :else (do
                                                              (if
                                                                (and
                                                                  (next des)
                                                                  (^clojure.lang.IFn lt
                                                                    (:key (fnext des))
                                                                    (first data)))
                                                                (do
                                                                  (let
                                                                    [logger
                                                                     (org.slf4j.LoggerFactory/getLogger
                                                                       "datomic.index")]
                                                                    (when
                                                                      (.isDebugEnabled
                                                                        ^org.slf4j.Logger logger)
                                                                      (.debug
                                                                        ^org.slf4j.Logger logger
                                                                        (logger/process
                                                                          {:event
                                                                           'index/merge-one-index,
                                                                           :stage :skip})))
                                                                    nil)
                                                                  (let
                                                                    [d (first data)
                                                                     vec__14664
                                                                     (^clojure.lang.IFn drainq
                                                                       es
                                                                       retractions
                                                                       erq)
                                                                     es
                                                                     (nth
                                                                       vec__14664
                                                                       (unchecked-int 0)
                                                                       nil)
                                                                     retractions
                                                                     (nth
                                                                       vec__14664
                                                                       (unchecked-int 1)
                                                                       nil)
                                                                     erq
                                                                     (nth
                                                                       vec__14664
                                                                       (unchecked-int 2)
                                                                       nil)
                                                                     vec__14667
                                                                     (let
                                                                       [f__14449__auto__
                                                                        (fn
                                                                          f__14449__auto__
                                                                          ([es des]
                                                                            (if
                                                                              (and
                                                                                (next des)
                                                                                (^clojure.lang.IFn lt
                                                                                  (:key
                                                                                    (fnext des))
                                                                                  d))
                                                                              (recur
                                                                                (conj
                                                                                  es
                                                                                  (first des))
                                                                                (next des))
                                                                              [es des])))]
                                                                       (^clojure.lang.IFn f__14449__auto__
                                                                         es
                                                                         des))
                                                                     es
                                                                     (nth
                                                                       vec__14667
                                                                       (unchecked-int 0)
                                                                       nil)
                                                                     des
                                                                     (nth
                                                                       vec__14667
                                                                       (unchecked-int 1)
                                                                       nil)]
                                                                    (recur
                                                                      es
                                                                      garbage
                                                                      retractions
                                                                      des
                                                                      data
                                                                      erq)))
                                                                (do
                                                                  (let
                                                                    [logger
                                                                     (org.slf4j.LoggerFactory/getLogger
                                                                       "datomic.index")]
                                                                    (when
                                                                      (.isDebugEnabled
                                                                        ^org.slf4j.Logger logger)
                                                                      (.debug
                                                                        ^org.slf4j.Logger logger
                                                                        (logger/process
                                                                          {:event
                                                                           'index/merge-one-index,
                                                                           :stage :merge})))
                                                                    nil)
                                                                  (let
                                                                    [tailp (promise)
                                                                     insert_data
                                                                     (seq
                                                                       (if
                                                                         (next des)
                                                                         (datomic.index/fully-take-while-delivering-tail
                                                                           tailp
                                                                           (fn
                                                                             fn__14676
                                                                             ([p1__14626#]
                                                                               (^clojure.lang.IFn lt
                                                                                 p1__14626#
                                                                                 (:key
                                                                                   (fnext des)))))
                                                                           data)
                                                                         data))
                                                                     segid
                                                                     (and des (:segid (first des)))
                                                                     seg_data*
                                                                     (future-call
                                                                       (fn
                                                                         fn__14678
                                                                         ([]
                                                                           (and
                                                                             segid
                                                                             (seq
                                                                               (cache/getx-uncached
                                                                                 olookup
                                                                                 segid))))))
                                                                     _
                                                                     (let
                                                                       [lookahead 10000]
                                                                       (when
                                                                         (=
                                                                           (datomic.index/bounded-count
                                                                             (long lookahead)
                                                                             insert_data)
                                                                           (long lookahead))
                                                                         nil))
                                                                     _
                                                                     (let
                                                                       [logger
                                                                        (org.slf4j.LoggerFactory/getLogger
                                                                          "datomic.index")]
                                                                       (when
                                                                         (.isDebugEnabled
                                                                           ^org.slf4j.Logger logger)
                                                                         (.debug
                                                                           ^org.slf4j.Logger logger
                                                                           (logger/process
                                                                             {:event
                                                                              'index/merge-one-index,
                                                                              :stage :build})))
                                                                       nil)
                                                                     vec__14673
                                                                     (if
                                                                       (<=
                                                                         (deref
                                                                           datomic.index/index-parallelism)
                                                                         (count erq))
                                                                       (let
                                                                         [vec__14681
                                                                          (deref (peek erq))
                                                                          nes
                                                                          (nth
                                                                            vec__14681
                                                                            (unchecked-int 0)
                                                                            nil)
                                                                          nrs
                                                                          (nth
                                                                            vec__14681
                                                                            (unchecked-int 1)
                                                                            nil)]
                                                                         [(into es nes)
                                                                          (into retractions nrs)
                                                                          (pop erq)])
                                                                       [es retractions erq])
                                                                     es
                                                                     (nth
                                                                       vec__14673
                                                                       (unchecked-int 0)
                                                                       nil)
                                                                     retractions
                                                                     (nth
                                                                       vec__14673
                                                                       (unchecked-int 1)
                                                                       nil)
                                                                     erq
                                                                     (nth
                                                                       vec__14673
                                                                       (unchecked-int 2)
                                                                       nil)
                                                                     erq
                                                                     (conj
                                                                       erq
                                                                       (future-call
                                                                         (fn
                                                                           fn__14684
                                                                           ([]
                                                                             (try
                                                                               (let
                                                                                 [seg_data
                                                                                  (deref seg_data*)
                                                                                  mdata
                                                                                  (if
                                                                                    seg_data
                                                                                    (datomic.index/merge-data
                                                                                      cmp
                                                                                      seg_data
                                                                                      insert_data)
                                                                                    insert_data)]
                                                                                 (datomic.index/build-segs
                                                                                   db
                                                                                   cstore
                                                                                   olookup
                                                                                   mdata
                                                                                   []
                                                                                   (when
                                                                                     retractions
                                                                                     [])
                                                                                   partfn
                                                                                   write_handlers
                                                                                   segs_written_ref))
                                                                               (catch
                                                                                 java.lang.Throwable
                                                                                 t
                                                                                 (do
                                                                                   (deliver
                                                                                     tailp
                                                                                     t)
                                                                                   (throw
                                                                                     ^java.lang.Throwable t)
                                                                                   nil)))))))]
                                                                    (monitor/add-stat
                                                                      :IndexWriteQueueCount
                                                                      (java.lang.Integer/valueOf
                                                                        (int (count erq))))
                                                                    (recur
                                                                      es
                                                                      (if
                                                                        segid
                                                                        (conj garbage segid)
                                                                        garbage)
                                                                      retractions
                                                                      (next des)
                                                                      (when
                                                                        (next des)
                                                                        (datomic.index/deref-or-throw
                                                                          tailp))
                                                                      erq))))))))]
                           (^clojure.lang.IFn f__14449__auto__
                             es
                             garbage_ids
                             retractions
                             (seq (apply concat (pmap mkdes dirs)))
                             (seq
                               (filter
                                 (fn fn__14690
                                   ([p1__14625#]
                                     (< (.getT ^datomic.impl.db.IDatum p1__14625#) as_of_t)))
                                 (remove excise? data)))
                             clojure.lang.PersistentQueue/EMPTY))
              es (nth vec__14630 (unchecked-int 0) nil)
              garbage (nth vec__14630 (unchecked-int 1) nil)
              retractions (nth vec__14630 (unchecked-int 2) nil)
              _ (a/close! des_ch)
              rootnode (deref rootnode_fut)
              mkroot (fn mkroot
                       ([rootnode_data]
                         (let [keydata (datomic.index/transpose (mapv first rootnode_data))
                               dirids (to-array (mapv second rootnode_data))]
                           (datomic.index/root-node keydata dirids))))
              rootid (common/rand-uuid)
              nroot (^clojure.lang.IFn mkroot rootnode)
              vmap {rootid (datomic.index/fress nroot write_handlers)}
              old_root_dirids (when old_root (.-dirids ^datomic.index.RootNode old_root))]
          (cache/put olookup rootid nroot)
          (datomic.index/write-vals cstore vmap)
          (when pario
            (common/bounded-deref (cluster/finish-writer cstore) cluster/BOUNDING_TIMEOUT_MSEC))
          (swap! segs_written_ref + (java.lang.Integer/valueOf (int (count vmap))))
          (swap!
            dirs_written_ref
            +
            (java.lang.Integer/valueOf
              (int
                (count (remove (set old_root_dirids) (.-dirids ^datomic.index.RootNode nroot))))))
          [rootid
           (concat
             garbage
             (map
               cluster/uuid->val-key
               (remove (set (.-dirids ^datomic.index.RootNode nroot)) old_root_dirids))
             (when old_root_id [(cluster/uuid->val-key old_root_id)]))
           retractions
           {:written (deref segs_written_ref), :dirs-written (deref dirs_written_ref)}])
        (catch
          java.lang.Throwable
          ex
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index") ex ex]
              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                (.warn
                  ^org.slf4j.Logger logger
                  (logger/process "merge-one-index failed")
                  ^java.lang.Throwable ex)
                (logger/caused-by logger ex))
              nil)
            (throw ^java.lang.Throwable ex)
            nil)))))
  (reset-meta!
    #'merge-one-index
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['db
          'cstore
          'olookup
          'old-root-id
          'data
          'garbage-ids
          'partfn
          'cmp
          'write-handlers
          'filter-retractions?
          'as-of-t
          'idx
          'xpreds
          'filter-segids
          'xcmp]),
       :column (int 1)}
      :name
      'merge-one-index
      :ns
      *ns*))
  (defn excise-ents
    ([db]
      (filter
        :db/excise
        (map
          (fn fn__14698 ([p1__14697#] (.entity ^datomic.Database db (:e p1__14697#))))
          (take-while
            (fn fn__14700 ([p1__14696#] (= (:a p1__14696#) 15)))
            (iter/iter-seq (btset/seek (:aevt (:indexing db)) (db/datum db :a 15))))))))
  (reset-meta!
    #'excise-ents
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]), :column (int 1)}
      :name
      'excise-ents
      :ns
      *ns*))
  (defn get-avet-sorted-datoms-mem
    ([db index attrids]
      (let [ret (java.util.ArrayList.)]
        (when index
          (loop [seq_14706 (seq attrids) chunk_14707 nil count_14708 0 i_14709 0]
            (if (< i_14709 count_14708)
              (let [attrid (.nth ^clojure.lang.Indexed chunk_14707 (unchecked-int i_14709))]
                (reduce
                  (fn fn__14710
                    ([p1__14704# p2__14703#] (.add ^java.util.ArrayList ret p2__14703#)))
                  nil
                  (iter/iter-seq
                    (iter/take-while
                      (fn fn__14712
                        ([p1__14705#]
                          (= attrid (long (.getA ^datomic.impl.db.IDatum p1__14705#)))))
                      (btset/seek index (db/datum db :a attrid)))))
                (recur seq_14706 chunk_14707 count_14708 (inc i_14709)))
              (let [temp__5825__auto__ (seq seq_14706)]
                (when temp__5825__auto__
                  (let [seq_14706 temp__5825__auto__]
                    (if (chunked-seq? seq_14706)
                      (let [c__6090__auto__ (chunk-first seq_14706)]
                        (recur (chunk-rest seq_14706) c__6090__auto__ (count c__6090__auto__) 0))
                      (let [attrid (first seq_14706)]
                        (reduce
                          (fn fn__14714
                            ([p1__14704# p2__14703#] (.add ^java.util.ArrayList ret p2__14703#)))
                          nil
                          (iter/iter-seq
                            (iter/take-while
                              (fn fn__14716
                                ([p1__14705#]
                                  (= attrid (long (.getA ^datomic.impl.db.IDatum p1__14705#)))))
                              (btset/seek index (db/datum db :a attrid)))))
                        (recur (next seq_14706) nil 0 0)))))))))
        (java.util.Collections/sort ^java.util.List ret db/avet-cmp)
        ret)))
  (reset-meta!
    #'get-avet-sorted-datoms-mem
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'index 'attrids]), :column (int 1)}
      :name
      'get-avet-sorted-datoms-mem
      :ns
      *ns*))
  (defn avet-sort-and-process-datoms
    ([datoms f]
      (let [dir (config/property "datomic.indexWorkDir")]
        (.mkdirs ^java.io.File dir)
        (esd/consume-sorted-datoms
          datoms
          {:cmp db/avet-cmp,
           :dir dir,
           :prog-fn
           (fn fn__14721
             ([m]
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                 (when (.isInfoEnabled ^org.slf4j.Logger logger)
                   (.info
                     ^org.slf4j.Logger logger
                     (logger/process (assoc m :event :index/external-sort))))
                 nil))),
           :max-chunk-size (long (* (* 10 1000) 1000))}
          f))))
  (reset-meta!
    #'avet-sort-and-process-datoms
    (assoc
      {:private true, :arglists (clojure.core/list ['datoms 'f]), :column (int 1)}
      :name
      'avet-sort-and-process-datoms
      :ns
      *ns*))
  (defn attr-datoms
    ([db index attrid]
      (iter/take-while
        (fn fn__14725 ([p1__14724#] (= attrid (long (.getA ^datomic.impl.db.IDatum p1__14724#)))))
        (btset/seek index (db/datum db :a attrid)))))
  (reset-meta!
    #'attr-datoms
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Db}) 'index 'attrid]), :column (int 1)}
      :name
      'attr-datoms
      :ns
      *ns*))
  (defn aevt-attrs-datoms
    ([db attrids]
      (let [datoms (map
                     (fn fn__14728
                       ([attrid]
                         (let [main_aevt_datoms (datomic.index/attr-datoms
                                                  db
                                                  (.-aevt (.-index ^datomic.db.Db db))
                                                  attrid)
                               mid_aevt_datoms (let [temp__5825__auto__ (.-aevt
                                                                          (.-mid-index
                                                                            ^datomic.db.Db db))]
                                                 (when temp__5825__auto__
                                                   (let [mid_aevt temp__5825__auto__]
                                                     (datomic.index/attr-datoms
                                                       db
                                                       mid_aevt
                                                       attrid))))
                               aevt_datoms (if mid_aevt_datoms
                                             (iter/merge-iters
                                               db/aevt-cmp
                                               main_aevt_datoms
                                               mid_aevt_datoms)
                                             main_aevt_datoms)
                               hist_aevt_datoms (let [temp__5825__auto__ (.-aevt
                                                                           (.-history
                                                                             ^datomic.db.Db db))]
                                                  (when temp__5825__auto__
                                                    (let [hist_aevt temp__5825__auto__]
                                                      (datomic.index/attr-datoms
                                                        db
                                                        hist_aevt
                                                        attrid))))]
                           [aevt_datoms hist_aevt_datoms])))
                     attrids)]
        [(iter/concat (filter identity (map first datoms)))
         (iter/concat (filter identity (map second datoms)))])))
  (reset-meta!
    #'aevt-attrs-datoms
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Db}) 'attrids]), :column (int 1)}
      :name
      'aevt-attrs-datoms
      :ns
      *ns*))
  (defn idx-key
    ([& ks]
      (fn fn__14734
        ([m] (some (fn fn__14735 ([p1__14733#] (^clojure.lang.IFn p1__14733# m))) ks)))))
  (reset-meta!
    #'idx-key
    (assoc {:arglists (clojure.core/list ['& 'ks]), :column (int 1)} :name 'idx-key :ns *ns*))
  (defn add-avet-indexes
    ([cstore olookup db as_of_t root_map attrids]
      (let [vec__14740 (datomic.index/aevt-attrs-datoms db attrids)
            mid_main_aevt_datoms (nth vec__14740 (unchecked-int 0) nil)
            hist_aevt_datoms (nth vec__14740 (unchecked-int 1) nil)
            sort_and_merge (fn sort_and_merge
                             ([k aevt_datoms garbage]
                               (let [m_14750 {:event :index/add-avet,
                                              :next-t as_of_t,
                                              :attributes attrids}
                                     ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.index")]
                                                       (when (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.info
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_14750 :phase :begin))))
                                                       nil)
                                     start__8599__auto__ (java.lang.System/nanoTime)
                                     result__8600__auto__ (try
                                                            {:returned
                                                             (datomic.index/avet-sort-and-process-datoms
                                                               aevt_datoms
                                                               (fn
                                                                 fn__14754
                                                                 ([avet_sorted_datoms]
                                                                   (datomic.index/merge-one-index
                                                                     db
                                                                     cstore
                                                                     olookup
                                                                     (^clojure.lang.IFn k root_map)
                                                                     (iter/iter-seq
                                                                       avet_sorted_datoms)
                                                                     garbage
                                                                     (fn
                                                                       fn__14755
                                                                       ([p1__14739#]
                                                                         (java.lang.Integer/valueOf
                                                                           (int
                                                                             (.getA
                                                                               ^datomic.impl.db.IDatum p1__14739#)))))
                                                                     db/avet-cmp
                                                                     datomic.index/common-write-handlers
                                                                     false
                                                                     as_of_t
                                                                     :avet
                                                                     nil
                                                                     nil
                                                                     datomic.index/avet-cmpi))))}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8601__auto__
                                                              {:threw t__8601__auto__}))
                                     elapsed_14751 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8599__auto__)
                                     msec_14752 (logger/format-as-msec (long elapsed_14751))]
                                 (monitor/add-stat :AddIndexMsec msec_14752)
                                 (let [endmsg__8602__auto__ (merge
                                                              (assoc
                                                                m_14750
                                                                :msec
                                                                msec_14752
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8600__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8600__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                     (.info
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8602__auto__)))
                                   nil)
                                 (if (contains? result__8600__auto__ :returned)
                                   (:returned result__8600__auto__)
                                   (do (throw (:threw result__8600__auto__)) nil)))))
            vec__14743 (^clojure.lang.IFn sort_and_merge
                         (datomic.index/idx-key :avet-main :avet)
                         mid_main_aevt_datoms
                         [])
            avetid (nth vec__14743 (unchecked-int 0) nil)
            garbage (nth vec__14743 (unchecked-int 1) nil)
            vec__14746 (^clojure.lang.IFn sort_and_merge :avet-hist hist_aevt_datoms garbage)
            hist_avetid (nth vec__14746 (unchecked-int 0) nil)
            garbage (nth vec__14746 (unchecked-int 1) nil)]
        [(assoc root_map :avet-main avetid :avet-hist hist_avetid) garbage])))
  (reset-meta!
    #'add-avet-indexes
    (assoc
      {:arglists
       (clojure.core/list
         ['cstore 'olookup (.withMeta 'db {:tag 'Db}) 'as-of-t 'root-map 'attrids]),
       :column (int 1)}
      :name
      'add-avet-indexes
      :ns
      *ns*))
  (defn needs-new-avet
    ([db]
      (seq
        (map
          (fn fn__14765 ([a] (.id ^datomic.db.Attribute a)))
          (filter
            (fn fn__14767
              ([a]
                (and
                  (.-needsAVET ^datomic.db.Attribute a)
                  (not (.hasAVET ^datomic.db.Attribute a)))))
            (filter (partial instance? datomic.db.Attribute) (:elements db)))))))
  (reset-meta!
    #'needs-new-avet
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Db})]), :column (int 1)}
      :name
      'needs-new-avet
      :ns
      *ns*))
  (defn dropped-avet-aids
    ([db]
      (let [hist (.history ^datomic.Database db)
            fadd (fn fadd
                   ([aids ds]
                     (reduce
                       (fn fn__14772
                         ([aids d]
                           (if (.-needsAVET
                                 (db/attribute db (long (.getE ^datomic.impl.db.IDatum d))))
                             aids
                             (conj! aids (long (.getE ^datomic.impl.db.IDatum d))))))
                       aids
                       ds)))]
        (persistent!
          (^clojure.lang.IFn fadd
            (^clojure.lang.IFn fadd (transient #{}) (db/datoms hist :aevt [:db/index]))
            (db/datoms hist :aevt [:db/unique]))))))
  (reset-meta!
    #'dropped-avet-aids
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]), :column (int 1)}
      :name
      'dropped-avet-aids
      :ns
      *ns*))
  (defn write-object
    ([store olookup v]
      (let [uuid (common/rand-uuid)]
        (datomic.index/write-vals
          store
          {uuid (datomic.index/fress v datomic.index/common-write-handlers)})
        (cache/put olookup uuid v)
        uuid)))
  (reset-meta!
    #'write-object
    (assoc
      {:arglists (clojure.core/list ['store 'olookup 'v]), :column (int 1)}
      :name
      'write-object
      :ns
      *ns*))
  (defn drop-dirnode-leaves
    ([dirnode drop?]
      (let [result (reduce
                     (fn fn__14777
                       ([result n]
                         (let [kd (nth
                                    (.-keydata ^datomic.index.DirNode dirnode)
                                    (unchecked-int ^java.lang.Number n))]
                           (if (^clojure.lang.IFn drop? kd)
                             (update
                               result
                               :garbage
                               conj
                               (nth
                                 (.-segids ^datomic.index.DirNode dirnode)
                                 (unchecked-int ^java.lang.Number n)))
                             (update
                               (update
                                 (update
                                   (update result :keydata conj kd)
                                   :segids
                                   conj
                                   (nth
                                     (.-segids ^datomic.index.DirNode dirnode)
                                     (unchecked-int ^java.lang.Number n)))
                                 :offsets
                                 conj
                                 (nth
                                   (.-offsets ^datomic.index.DirNode dirnode)
                                   (unchecked-int ^java.lang.Number n)))
                               :counts
                               conj
                               (nth
                                 (.-counts ^datomic.index.DirNode dirnode)
                                 (unchecked-int ^java.lang.Number n)))))))
                     (zipmap [:keydata :segids :offsets :counts :garbage] (repeat []))
                     (range
                       (java.lang.Integer/valueOf
                         (int (count (.-keydata ^datomic.index.DirNode dirnode))))))]
        (if (seq (:garbage result))
          (if (seq (:keydata result))
            [(datomic.index/dir-node
               (datomic.index/transpose (:keydata result))
               (to-array (:segids result))
               (into-array java.lang.Integer/TYPE (:offsets result))
               (into-array java.lang.Integer/TYPE (:counts result)))
             (:garbage result)]
            [nil (:garbage result)])
          [dirnode nil]))))
  (reset-meta!
    #'drop-dirnode-leaves
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'dirnode {:tag 'DirNode}) 'drop?]),
       :column (int 1)}
      :name
      'drop-dirnode-leaves
      :ns
      *ns*))
  (defn drop-avet
    ([store olookup old_rootid aid old_garbage]
      (let [oldroot (common/getx olookup old_rootid)
            ct (count (.-keydata ^datomic.index.RootNode oldroot))
            map__14780 (reduce
                         (fn fn__14781
                           ([result n]
                             (let [temp__5825__auto__ datomic.index/*pace-index-fn*]
                               (when temp__5825__auto__
                                 (let [f temp__5825__auto__] (^clojure.lang.IFn f 1))))
                             (let [kd (nth
                                        (.-keydata ^datomic.index.RootNode oldroot)
                                        (unchecked-int ^java.lang.Number n))
                                   a1 (:a kd)
                                   a2 (let [n2 (inc n)]
                                        (if (< n2 ct)
                                          (:a
                                            (nth
                                              (.-keydata ^datomic.index.RootNode oldroot)
                                              (unchecked-int ^java.lang.Number n2)))
                                          (long java.lang.Long/MAX_VALUE)))
                                   olddirid (when (<= a1 aid a2)
                                              (nth
                                                (.-dirids ^datomic.index.RootNode oldroot)
                                                (unchecked-int ^java.lang.Number n)))
                                   olddir (when olddirid (common/getx olookup olddirid))
                                   vec__14782 (when olddirid
                                                (datomic.index/drop-dirnode-leaves
                                                  olddir
                                                  (fn fn__14786
                                                    ([p__14785]
                                                      (let [map__14787 p__14785
                                                            map__14787
                                                            (if
                                                              (seq? map__14787)
                                                              (if
                                                                (next map__14787)
                                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                  (to-array map__14787))
                                                                (if
                                                                  (seq map__14787)
                                                                  (first map__14787)
                                                                  {}))
                                                              map__14787)
                                                            a (get map__14787 :a)]
                                                        (= a aid))))))
                                   newdir (nth vec__14782 (unchecked-int 0) nil)
                                   leaf_garbage (nth vec__14782 (unchecked-int 1) nil)]
                               (cond
                                 (and newdir (seq leaf_garbage)) (update
                                                                   (update
                                                                     (update
                                                                       (update
                                                                         result
                                                                         :keydata
                                                                         conj
                                                                         (first
                                                                           (.-keydata
                                                                             ^datomic.index.DirNode newdir)))
                                                                       :dirids
                                                                       conj
                                                                       (datomic.index/write-object
                                                                         store
                                                                         olookup
                                                                         newdir))
                                                                     :garbage
                                                                     into
                                                                     leaf_garbage)
                                                                   :garbage
                                                                   conj
                                                                   olddirid)
                                 (and (not newdir) (seq leaf_garbage)) (update
                                                                         (update
                                                                           result
                                                                           :garbage
                                                                           into
                                                                           leaf_garbage)
                                                                         :garbage
                                                                         conj
                                                                         olddirid)
                                 (or (not olddirid) (and newdir (empty? leaf_garbage))) (update
                                                                                          (update
                                                                                            result
                                                                                            :keydata
                                                                                            conj
                                                                                            kd)
                                                                                          :dirids
                                                                                          conj
                                                                                          (nth
                                                                                            (.-dirids
                                                                                              ^datomic.index.RootNode oldroot)
                                                                                            (unchecked-int
                                                                                              ^java.lang.Number n)))
                                 :default (do
                                            (when-not false
                                              (throw
                                                (java.lang.AssertionError.
                                                  (str "Assert failed: " (pr-str false))))
                                              nil))))))
                         (zipmap [:keydata :dirids :garbage] (repeat []))
                         (range
                           (java.lang.Integer/valueOf
                             (int (count (.-keydata ^datomic.index.RootNode oldroot))))))
            map__14780 (if (seq? map__14780)
                         (if (next map__14780)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__14780))
                           (if (seq map__14780) (first map__14780) {}))
                         map__14780)
            keydata (get map__14780 :keydata)
            dirids (get map__14780 :dirids)
            garbage (get map__14780 :garbage)]
        (if (seq garbage)
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:event :index/drop-avet-segments,
                     :aid aid,
                     :count (long (inc (count garbage)))})))
              nil)
            [(datomic.index/write-object
               store
               olookup
               (datomic.index/root-node (datomic.index/transpose keydata) (to-array dirids)))
             (conj (into old_garbage garbage) old_rootid)])
          [old_rootid old_garbage]))))
  (reset-meta!
    #'drop-avet
    (assoc
      {:arglists (clojure.core/list ['store 'olookup 'old-rootid 'aid 'old-garbage]),
       :column (int 1)}
      :name
      'drop-avet
      :ns
      *ns*))
  (defn drop-avets
    ([store olookup root_id attrids garbage]
      (reduce
        (fn fn__14797
          ([p__14796 aid]
            (let [vec__14798 p__14796
                  root_id (nth vec__14798 (unchecked-int 0) nil)
                  garbage (nth vec__14798 (unchecked-int 1) nil)]
              (datomic.index/drop-avet store olookup root_id aid garbage))))
        [root_id garbage]
        attrids)))
  (reset-meta!
    #'drop-avets
    (assoc
      {:arglists (clojure.core/list ['store 'olookup 'root-id 'attrids 'garbage]), :column (int 1)}
      :name
      'drop-avets
      :ns
      *ns*))
  (defn drop-avet-indexes
    ([store olookup old_root_ids attrids as_of_t garbage]
      (let [% (reduce
                (fn fn__14804
                  ([p__14803 root_id]
                    (let [vec__14805 p__14803
                          root_ids (nth vec__14805 (unchecked-int 0) nil)
                          garbage (nth vec__14805 (unchecked-int 1) nil)]
                      (if root_id
                        (let [m_14808 {:event :index/drop-avets,
                                       :root-id root_id,
                                       :next-t as_of_t,
                                       :attrs (java.lang.Integer/valueOf (int (count attrids)))}
                              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.index")]
                                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                  (.info
                                                    ^org.slf4j.Logger logger
                                                    (logger/process
                                                      (assoc m_14808 :phase :begin))))
                                                nil)
                              start__8599__auto__ (java.lang.System/nanoTime)
                              result__8600__auto__ (try
                                                     {:returned
                                                      (let [vec__14812
                                                            (datomic.index/drop-avets
                                                              store
                                                              olookup
                                                              root_id
                                                              attrids
                                                              garbage)
                                                            new_id
                                                            (nth vec__14812 (unchecked-int 0) nil)
                                                            garbage
                                                            (nth vec__14812 (unchecked-int 1) nil)]
                                                        [(conj root_ids new_id) garbage])}
                                                     (catch
                                                       java.lang.Throwable
                                                       t__8601__auto__
                                                       {:threw t__8601__auto__}))
                              elapsed_14809 (- (java.lang.System/nanoTime) start__8599__auto__)
                              msec_14810 (logger/format-as-msec (long elapsed_14809))]
                          (let [endmsg__8602__auto__ (merge
                                                       (assoc m_14808 :msec msec_14810 :phase :end)
                                                       (when (:threw result__8600__auto__)
                                                         {:threw
                                                          (class (:threw result__8600__auto__))}))
                                logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                            (when (.isInfoEnabled ^org.slf4j.Logger logger)
                              (.info
                                ^org.slf4j.Logger logger
                                (logger/process endmsg__8602__auto__)))
                            nil)
                          (if (contains? result__8600__auto__ :returned)
                            (:returned result__8600__auto__)
                            (do (throw (:threw result__8600__auto__)) nil)))
                        [(conj root_ids root_id) garbage]))))
                [[] garbage]
                old_root_ids)]
        (when-not (= (count old_root_ids) (count (first %)))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list
                    '=
                    (clojure.core/list 'count 'old-root-ids)
                    (clojure.core/list 'count (clojure.core/list 'first '%))))))))
        %)))
  (reset-meta!
    #'drop-avet-indexes
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['store 'olookup 'old-root-ids 'attrids 'as-of-t 'garbage]
           {:post
            [(.withMeta
               (clojure.core/list
                 '=
                 (.withMeta (clojure.core/list 'count 'old-root-ids) {:column (int 14)})
                 (.withMeta
                   (clojure.core/list
                     'count
                     (.withMeta (clojure.core/list 'first '%) {:column (int 42)}))
                   {:column (int 35)}))
               {:column (int 11)})]})),
       :column (int 1)}
      :name
      'drop-avet-indexes
      :ns
      *ns*))
  (defn mem-index-bytes
    ([index]
      (if index
        (java.lang.Float/valueOf
          (unchecked-float (* (mem/memory-size index) datomic.index/MEM_TO_STG_RATIO)))
        0.0)))
  (reset-meta!
    #'mem-index-bytes
    (assoc
      {:arglists (clojure.core/list ['index]), :column (int 1)}
      :name
      'mem-index-bytes
      :ns
      *ns*))
  (defn seg-count-cumsum
    ([olookup root cache?]
      (let [nd (if root
                 (java.lang.Integer/valueOf (int (count (.-dirids ^datomic.index.RootNode root))))
                 0)]
        (if (== 0 nd)
          (long-array [0])
          (let [counts (long-array nd)]
            (loop [idx 0 prev_count 0]
              (if (< idx nd)
                (let [dir_count (count
                                  (.-segids
                                    (datomic.index/get-dir-node root (long idx) olookup cache?)))
                      new_sum (+ dir_count prev_count)]
                  (aset ^longs counts (int idx) (long new_sum))
                  (recur (inc idx) new_sum))
                counts)))))))
  (reset-meta!
    #'seg-count-cumsum
    (assoc
      {:arglists (clojure.core/list (.withMeta ['olookup 'root 'cache?] {:tag 'longs})),
       :column (int 1)}
      :name
      'seg-count-cumsum
      :ns
      *ns*))
  (defn stg-index-size
    ([olookup index part_size]
      (let [seg_counts (datomic.index/seg-count-cumsum
                         olookup
                         (when index (.-root ^datomic.index.Index index))
                         true)
            seg_count (aget ^longs seg_counts (dec (alength ^longs seg_counts)))]
        [(java.lang.Float/valueOf (unchecked-float (* seg_count datomic.index/BYTES_PER_SEG)))
         (long seg_count)])))
  (reset-meta!
    #'stg-index-size
    (assoc
      {:arglists (clojure.core/list ['olookup 'index 'part-size]), :column (int 1)}
      :name
      'stg-index-size
      :ns
      *ns*))
  (defn estimate-seg-offset
    ([olookup index dir_partition_size seg_counts_cmsm k]
      (let [root (.-root ^datomic.index.Index index)
            cmpi (.-cmpi ^datomic.index.Index index)
            ri (datomic.index/ibtree-search (.-keydata ^datomic.index.RootNode root) k cmpi)]
        (if (< ri (count (.-keydata ^datomic.index.RootNode root)))
          (let [dir (datomic.index/get-dir-node root (long ri) olookup true)
                di (datomic.index/ibtree-search (.-keydata ^datomic.index.DirNode dir) k cmpi)]
            {:key k,
             :index-key (nth (.-keydata ^datomic.index.DirNode dir) (unchecked-int di)),
             :offset (long (+ (aget ^longs seg_counts_cmsm ri) di))})
          {:key k,
           :index-key nil,
           :offset
           (long (aget ^longs seg_counts_cmsm (int (dec (alength ^longs seg_counts_cmsm)))))}))))
  (reset-meta!
    #'estimate-seg-offset
    (assoc
      {:arglists
       (clojure.core/list
         ['olookup
          (.withMeta 'index {:tag 'Index})
          'dir-partition-size
          (.withMeta 'seg-counts-cmsm {:tag 'longs})
          'k]),
       :column (int 1)}
      :name
      'estimate-seg-offset
      :ns
      *ns*))
  (defn least-pop-slice
    ([olookup ks index dir_partition_size slice_size]
      (let [seg_counts_cmsm (datomic.index/seg-count-cumsum
                              olookup
                              (.-root ^datomic.index.Index index)
                              true)
            offsets (map
                      (fn fn__14827
                        ([p1__14826#]
                          (datomic.index/estimate-seg-offset
                            olookup
                            index
                            dir_partition_size
                            seg_counts_cmsm
                            p1__14826#)))
                      ks)
            get_slice (fn get_slice
                        ([lo hi] [(:key lo) (- (:offset hi) (:offset lo)) (:offset lo)]))
            pops (map get_slice offsets (drop slice_size offsets))]
        (if (seq pops)
          (apply min-key second pops)
          (^clojure.lang.IFn get_slice (first offsets) (last offsets))))))
  (reset-meta!
    #'least-pop-slice
    (assoc
      {:arglists
       (clojure.core/list
         ['olookup 'ks (.withMeta 'index {:tag 'Index}) 'dir-partition-size 'slice-size]),
       :column (int 1)}
      :name
      'least-pop-slice
      :ns
      *ns*))
  (defn retract-assert-pair?
    ([d1 d2]
      (and
        (= (long (.getE ^datomic.db.Datum d1)) (long (.getE ^datomic.db.Datum d2)))
        (= (long (.getA ^datomic.db.Datum d1)) (long (.getA ^datomic.db.Datum d2)))
        (zero? (common/compare (.getV ^datomic.db.Datum d1) (.getV ^datomic.db.Datum d2)))
        (false? (.isAssertion ^datomic.db.Datum d1))
        (true? (.isAssertion ^datomic.db.Datum d2)))))
  (reset-meta!
    #'retract-assert-pair?
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'd1 {:tag 'datomic.db.Datum}) (.withMeta 'd2 {:tag 'datomic.db.Datum})]),
       :column (int 1)}
      :name
      'retract-assert-pair?
      :ns
      *ns*))
  (defn paired-assertion-state
    ([sd1 p__14837]
      (let [vec__14838 p__14837
            sd2 (nth vec__14838 (unchecked-int 0) nil)
            more vec__14838
            vec__14841 (let [G__14847 (cons sd1 more)
                             vec__14848 G__14847
                             sd1 (nth vec__14848 (unchecked-int 0) nil)
                             sd2 (nth vec__14848 (unchecked-int 1) nil)
                             more vec__14848]
                         (loop [G__14847 G__14847]
                           (let [vec__14852 G__14847
                                 sd1 (nth vec__14852 (unchecked-int 0) nil)
                                 sd2 (nth vec__14852 (unchecked-int 1) nil)
                                 more vec__14852]
                             (if (= (:item sd1) (:item sd2)) (recur (next more)) more))))
            sd1 (nth vec__14841 (unchecked-int 0) nil)
            sd2 (nth vec__14841 (unchecked-int 1) nil)
            more vec__14841]
        (cond
          (nil? sd2) :absent
          (datomic.index/retract-assert-pair? (:item sd1) (:item sd2)) (if
                                                                         (not=
                                                                           (:segid sd1)
                                                                           (:segid sd2))
                                                                         :segmented
                                                                         :present)
          :else (do
                  (let [map__14856 (:item sd1)
                        map__14856 (if (seq? map__14856)
                                     (if (next map__14856)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__14856))
                                       (if (seq map__14856) (first map__14856) {}))
                                     map__14856)
                        e (get map__14856 :e)
                        a (get map__14856 :a)
                        v (get map__14856 :v)
                        lookahead (seq
                                    (take-while
                                      (fn fn__14858
                                        ([p__14857]
                                          (let [map__14859 p__14857
                                                map__14859 (if
                                                             (seq? map__14859)
                                                             (if
                                                               (next map__14859)
                                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                 (to-array map__14859))
                                                               (if
                                                                 (seq map__14859)
                                                                 (first map__14859)
                                                                 {}))
                                                             map__14859)
                                                item (get map__14859 :item)]
                                            (and
                                              (= (:e item) e)
                                              (= (:a item) a)
                                              (zero? (common/compare (:v item) v))))))
                                      more))]
                    (if (some
                          (fn fn__14863
                            ([sdn] (datomic.index/retract-assert-pair? (:item sd1) (:item sdn))))
                          lookahead)
                      :separated
                      :absent)))))))
  (reset-meta!
    #'paired-assertion-state
    (assoc
      {:arglists (clojure.core/list ['sd1 ['sd2 :as 'more]]), :column (int 1)}
      :name
      'paired-assertion-state
      :ns
      *ns*))
  (defn problem-assertion-state?
    ([tier state]
      (let [G__14866 tier]
        (case
          G__14866
          :mid-index
          (contains? #{:segmented :separated} state)
          :history
          (contains? #{:absent :segmented :separated} state)))))
  (reset-meta!
    #'problem-assertion-state?
    (assoc
      {:private true, :arglists (clojure.core/list ['tier 'state]), :column (int 1)}
      :name
      'problem-assertion-state?
      :ns
      *ns*))
  (defn disjoined-datoms
    ([db tier sort]
      (let [temp__5825__auto__ (some-> db (^clojure.lang.IFn tier) (^clojure.lang.IFn sort))]
        (when temp__5825__auto__
          (let [idx temp__5825__auto__
                G__14872 (some-> idx (.seek) (datomic.index/seg+item-seq))
                vec__14873 G__14872
                seq__14874 (seq vec__14873)
                first__14875 (first seq__14874)
                seq__14874 (next seq__14874)
                sd1 first__14875
                more seq__14874
                result (if (= tier :history)
                         {:segmented [], :separated [], :absent []}
                         {:segmented [], :separated []})]
            (loop [G__14872 G__14872 result result]
              (let [vec__14877 G__14872
                    seq__14878 (seq vec__14877)
                    first__14879 (first seq__14878)
                    seq__14878 (next seq__14878)
                    sd1 first__14879
                    more seq__14878
                    result result]
                (if (nil? sd1)
                  result
                  (if (:added (:item sd1))
                    (recur more result)
                    (recur
                      more
                      (let [state (datomic.index/paired-assertion-state sd1 more)]
                        (if (datomic.index/problem-assertion-state? tier state)
                          (update-in result [state] conj (:item sd1))
                          result))))))))))))
  (reset-meta!
    #'disjoined-datoms
    (assoc
      {:arglists (clojure.core/list ['db 'tier 'sort]), :column (int 1)}
      :name
      'disjoined-datoms
      :ns
      *ns*))
  (defn repair-disjoined
    ([db olookup root_map]
      (if (< (or (^clojure.lang.IFn root_map :buildRevision) 0) 4846)
        (let [m_14884 {:event :index/repair-disjoined-db, :root-map root_map}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_14884 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (reduce
                                        (fn fn__14888
                                          ([db idxsort]
                                            (let [m_14889 {:event :index/repair-disjoined-index,
                                                           :index idxsort}
                                                  ___8598__auto__ (let
                                                                    [logger
                                                                     (org.slf4j.LoggerFactory/getLogger
                                                                       "datomic.index")]
                                                                    (when
                                                                      (.isInfoEnabled
                                                                        ^org.slf4j.Logger logger)
                                                                      (.info
                                                                        ^org.slf4j.Logger logger
                                                                        (logger/process
                                                                          (assoc
                                                                            m_14889
                                                                            :phase
                                                                            :begin))))
                                                                    nil)
                                                  start__8599__auto__ (java.lang.System/nanoTime)
                                                  result__8600__auto__ (try
                                                                         {:returned
                                                                          (let
                                                                            [mids
                                                                             (datomic.index/disjoined-datoms
                                                                               db
                                                                               :mid-index
                                                                               idxsort)
                                                                             hists
                                                                             (datomic.index/disjoined-datoms
                                                                               db
                                                                               :history
                                                                               idxsort)
                                                                             datoms
                                                                             (concat
                                                                               (mapcat
                                                                                 identity
                                                                                 (vals mids))
                                                                               (mapcat
                                                                                 identity
                                                                                 (vals hists)))]
                                                                            (if
                                                                              (seq datoms)
                                                                              (let
                                                                                [counts
                                                                                 (fn
                                                                                   counts
                                                                                   ([p1__14882#]
                                                                                     (reduce
                                                                                       (fn
                                                                                         fn__14895
                                                                                         ([m
                                                                                           p__14894]
                                                                                           (let
                                                                                             [vec__14896
                                                                                              p__14894
                                                                                              k
                                                                                              (nth
                                                                                                vec__14896
                                                                                                (unchecked-int
                                                                                                  0)
                                                                                                nil)
                                                                                              v
                                                                                              (nth
                                                                                                vec__14896
                                                                                                (unchecked-int
                                                                                                  1)
                                                                                                nil)]
                                                                                             (assoc
                                                                                               m
                                                                                               k
                                                                                               (java.lang.Integer/valueOf
                                                                                                 (int
                                                                                                   (count
                                                                                                     v)))))))
                                                                                       {}
                                                                                       p1__14882#)))]
                                                                                (let
                                                                                  [logger
                                                                                   (org.slf4j.LoggerFactory/getLogger
                                                                                     "datomic.index")]
                                                                                  (when
                                                                                    (.isInfoEnabled
                                                                                      ^org.slf4j.Logger logger)
                                                                                    (.info
                                                                                      ^org.slf4j.Logger logger
                                                                                      (logger/process
                                                                                        {:event
                                                                                         :index/repair-disjoined-datoms,
                                                                                         :index
                                                                                         idxsort,
                                                                                         :mids
                                                                                         (^clojure.lang.IFn counts
                                                                                           mids),
                                                                                         :hists
                                                                                         (^clojure.lang.IFn counts
                                                                                           hists)})))
                                                                                  nil)
                                                                                (update-in
                                                                                  db
                                                                                  [:indexing
                                                                                   idxsort]
                                                                                  (fn
                                                                                    fn__14901
                                                                                    ([p1__14883#]
                                                                                      (into
                                                                                        p1__14883#
                                                                                        datoms)))))
                                                                              db))}
                                                                         (catch
                                                                           java.lang.Throwable
                                                                           t__8601__auto__
                                                                           {:threw
                                                                            t__8601__auto__}))
                                                  elapsed_14890 (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8599__auto__)
                                                  msec_14891 (logger/format-as-msec
                                                               (long elapsed_14890))]
                                              (let [endmsg__8602__auto__ (merge
                                                                           (assoc
                                                                             m_14889
                                                                             :msec
                                                                             msec_14891
                                                                             :phase
                                                                             :end)
                                                                           (when
                                                                             (:threw
                                                                               result__8600__auto__)
                                                                             {:threw
                                                                              (class
                                                                                (:threw
                                                                                  result__8600__auto__))}))
                                                    logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.index")]
                                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                  (.info
                                                    ^org.slf4j.Logger logger
                                                    (logger/process endmsg__8602__auto__)))
                                                nil)
                                              (if (contains? result__8600__auto__ :returned)
                                                (:returned result__8600__auto__)
                                                (do (throw (:threw result__8600__auto__)) nil)))))
                                        db
                                        [:eavt :aevt :avet :raet])}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_14885 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_14886 (logger/format-as-msec (long elapsed_14885))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_14884 :msec msec_14886 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil)))
        db)))
  (reset-meta!
    #'repair-disjoined
    (assoc
      {:arglists (clojure.core/list ['db 'olookup 'root-map]), :column (int 1)}
      :name
      'repair-disjoined
      :ns
      *ns*))
  (defn aggregate-metrics ([& metrics] (apply merge-with + metrics)))
  (reset-meta!
    #'aggregate-metrics
    (assoc
      {:arglists (clojure.core/list ['& 'metrics]), :column (int 1)}
      :name
      'aggregate-metrics
      :ns
      *ns*))
  (def index-fire
   (fn index_fire
     ([&form &env & body]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq
                 (concat
                   (clojure.core/list 'f__14917__auto__)
                   (-> (with-meta
                         (.withMeta 'fn* {:once true})
                         (apply
                           hash-map
                           (seq (concat (clojure.core/list :once) (clojure.core/list true)))))
                    (clojure.core/list)
                    (concat (clojure.core/list (apply vector (seq (concat)))) body)
                    (seq)
                    (clojure.core/list))))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'if)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.config/property)
                       (clojure.core/list "datomic.indexParallelMerges"))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/future-call)
                       (clojure.core/list 'f__14917__auto__))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/atom)
                       (clojure.core/list
                         (seq (concat (clojure.core/list 'f__14917__auto__)))))))))))))))
  (reset-meta!
    #'index-fire
    (assoc {:arglists (clojure.core/list ['& 'body]), :column (int 1)} :name 'index-fire :ns *ns*))
  (.setMacro #'datomic.index/index-fire)
  (defn merge-db*
    ([cstore olookup db as_of_t old_root_id extra fulltext_enabled? excise_enabled?]
      (let [job_started (java.lang.System/nanoTime)
            root_map (common/getx olookup old_root_id)
            vec__14924 (let [temp__5823__auto__ (datomic.index/needs-new-avet db)]
                         (if temp__5823__auto__
                           (let [attrids temp__5823__auto__]
                             (datomic.index/add-avet-indexes
                               cstore
                               olookup
                               db
                               as_of_t
                               root_map
                               attrids))
                           [root_map []]))
            root_map (nth vec__14924 (unchecked-int 0) nil)
            garbage (nth vec__14924 (unchecked-int 1) nil)
            xents (when excise_enabled? (datomic.index/excise-ents db))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process
                      {:event :excise/ents,
                       :next-t as_of_t,
                       :count (java.lang.Integer/valueOf (int (count xents)))})))
                nil)
            xpreds (x/create-xpreds db xents)
            build_tiered_index (fn build_tiered_index
                                 ([idx_name
                                   midk
                                   maink
                                   histk
                                   partfn
                                   part_size
                                   mem_idx
                                   mid_idx
                                   main_idx
                                   cmp
                                   cmpi
                                   write_handlers
                                   idxcmp]
                                   (let [build_index (fn build_index
                                                       ([destk
                                                         datoms
                                                         filter_retractions?
                                                         filter_segids
                                                         garbage]
                                                         (datomic.index/merge-one-index
                                                           db
                                                           cstore
                                                           olookup
                                                           (^clojure.lang.IFn destk root_map)
                                                           datoms
                                                           garbage
                                                           partfn
                                                           cmp
                                                           write_handlers
                                                           filter_retractions?
                                                           as_of_t
                                                           idxcmp
                                                           xpreds
                                                           filter_segids
                                                           cmpi)))
                                         count_mem_idx (count mem_idx)
                                         lt (datomic.index/make-sparse-lt cmp)
                                         N (let [m_14954 {:event :index/mem-index-bytes,
                                                          :index idx_name}
                                                 ___8598__auto__ (let
                                                                   [logger
                                                                    (org.slf4j.LoggerFactory/getLogger
                                                                      "datomic.index")]
                                                                   (when
                                                                     (.isInfoEnabled
                                                                       ^org.slf4j.Logger logger)
                                                                     (.info
                                                                       ^org.slf4j.Logger logger
                                                                       (logger/process
                                                                         (assoc
                                                                           m_14954
                                                                           :phase
                                                                           :begin))))
                                                                   nil)
                                                 start__8599__auto__ (java.lang.System/nanoTime)
                                                 result__8600__auto__ (try
                                                                        {:returned
                                                                         (datomic.index/mem-index-bytes
                                                                           mem_idx)}
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          t__8601__auto__
                                                                          {:threw
                                                                           t__8601__auto__}))
                                                 elapsed_14955 (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8599__auto__)
                                                 msec_14956 (logger/format-as-msec
                                                              (long elapsed_14955))]
                                             (let [endmsg__8602__auto__ (merge
                                                                          (assoc
                                                                            m_14954
                                                                            :msec
                                                                            msec_14956
                                                                            :phase
                                                                            :end)
                                                                          (when
                                                                            (:threw
                                                                              result__8600__auto__)
                                                                            {:threw
                                                                             (class
                                                                               (:threw
                                                                                 result__8600__auto__))}))
                                                   logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.index")]
                                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                 (.info
                                                   ^org.slf4j.Logger logger
                                                   (logger/process endmsg__8602__auto__)))
                                               nil)
                                             (if (contains? result__8600__auto__ :returned)
                                               (:returned result__8600__auto__)
                                               (do (throw (:threw result__8600__auto__)) nil)))
                                         vec__14949 (datomic.index/stg-index-size
                                                      olookup
                                                      main_idx
                                                      part_size)
                                         M (nth vec__14949 (unchecked-int 0) nil)
                                         main_segs (nth vec__14949 (unchecked-int 1) nil)
                                         M_to_N (if (not (zero? N)) (/ M N) 0)]
                                     (if (and
                                           (nil? mid_idx)
                                           (or
                                             (< M datomic.index/MIN_MAIN_INDEX_THRESHOLD)
                                             (< M_to_N datomic.index/MID_INDEX_THRESHOLD_FACTOR)))
                                       (let [m_14959 {:event :index/merge-main,
                                                      :index idx_name,
                                                      :count
                                                      (java.lang.Integer/valueOf
                                                        (int count_mem_idx)),
                                                      :main-segs main_segs,
                                                      :as-of-t as_of_t,
                                                      :N N,
                                                      :M M}
                                             ___8598__auto__ (let
                                                               [logger
                                                                (org.slf4j.LoggerFactory/getLogger
                                                                  "datomic.index")]
                                                               (when
                                                                 (.isInfoEnabled
                                                                   ^org.slf4j.Logger logger)
                                                                 (.info
                                                                   ^org.slf4j.Logger logger
                                                                   (logger/process
                                                                     (assoc
                                                                       m_14959
                                                                       :phase
                                                                       :begin))))
                                                               nil)
                                             start__8599__auto__ (java.lang.System/nanoTime)
                                             result__8600__auto__ (try
                                                                    {:returned
                                                                     (let
                                                                       [vec__14963
                                                                        (^clojure.lang.IFn build_index
                                                                          maink
                                                                          (iter/iter-seq
                                                                            (btset/seek mem_idx))
                                                                          true
                                                                          nil
                                                                          garbage)
                                                                        retid
                                                                        (nth
                                                                          vec__14963
                                                                          (unchecked-int 0)
                                                                          nil)
                                                                        garbage
                                                                        (nth
                                                                          vec__14963
                                                                          (unchecked-int 1)
                                                                          nil)
                                                                        retractions
                                                                        (nth
                                                                          vec__14963
                                                                          (unchecked-int 2)
                                                                          nil)
                                                                        main_metrics
                                                                        (nth
                                                                          vec__14963
                                                                          (unchecked-int 3)
                                                                          nil)
                                                                        vec__14966
                                                                        (if
                                                                          (or
                                                                            (seq retractions)
                                                                            (seq xpreds))
                                                                          (^clojure.lang.IFn build_index
                                                                            histk
                                                                            retractions
                                                                            false
                                                                            nil
                                                                            garbage)
                                                                          [(^clojure.lang.IFn histk
                                                                             root_map)
                                                                           garbage])
                                                                        hist_retid
                                                                        (nth
                                                                          vec__14966
                                                                          (unchecked-int 0)
                                                                          nil)
                                                                        garbage
                                                                        (nth
                                                                          vec__14966
                                                                          (unchecked-int 1)
                                                                          nil)
                                                                        _retract
                                                                        (nth
                                                                          vec__14966
                                                                          (unchecked-int 2)
                                                                          nil)
                                                                        hist_metrics
                                                                        (nth
                                                                          vec__14966
                                                                          (unchecked-int 3)
                                                                          nil)
                                                                        metrics
                                                                        (datomic.index/aggregate-metrics
                                                                          main_metrics
                                                                          hist_metrics)]
                                                                       (let
                                                                         [logger
                                                                          (org.slf4j.LoggerFactory/getLogger
                                                                            "datomic.index")]
                                                                         (when
                                                                           (.isInfoEnabled
                                                                             ^org.slf4j.Logger logger)
                                                                           (.info
                                                                             ^org.slf4j.Logger logger
                                                                             (logger/process
                                                                               (merge
                                                                                 metrics
                                                                                 {:event
                                                                                  :index/merged-index,
                                                                                  :mode
                                                                                  :merge-main,
                                                                                  :index idx_name,
                                                                                  :count
                                                                                  (java.lang.Integer/valueOf
                                                                                    (int
                                                                                      count_mem_idx)),
                                                                                  :as-of-t
                                                                                  as_of_t}))))
                                                                         nil)
                                                                       [(^clojure.lang.IFn midk
                                                                          root_map)
                                                                        retid
                                                                        hist_retid
                                                                        garbage
                                                                        metrics])}
                                                                    (catch
                                                                      java.lang.Throwable
                                                                      t__8601__auto__
                                                                      {:threw t__8601__auto__}))
                                             elapsed_14960 (-
                                                             (java.lang.System/nanoTime)
                                                             start__8599__auto__)
                                             msec_14961 (logger/format-as-msec
                                                          (long elapsed_14960))]
                                         (let [endmsg__8602__auto__ (merge
                                                                      (assoc
                                                                        m_14959
                                                                        :msec
                                                                        msec_14961
                                                                        :phase
                                                                        :end)
                                                                      (when
                                                                        (:threw
                                                                          result__8600__auto__)
                                                                        {:threw
                                                                         (class
                                                                           (:threw
                                                                             result__8600__auto__))}))
                                               logger (org.slf4j.LoggerFactory/getLogger
                                                        "datomic.index")]
                                           (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                             (.info
                                               ^org.slf4j.Logger logger
                                               (logger/process endmsg__8602__auto__)))
                                           nil)
                                         (if (contains? result__8600__auto__ :returned)
                                           (:returned result__8600__auto__)
                                           (do (throw (:threw result__8600__auto__)) nil)))
                                       (let [TI (*
                                                  (java.lang.Math/sqrt
                                                    (unchecked-double ^java.lang.Number M_to_N))
                                                  N)
                                             vec__14971 (datomic.index/stg-index-size
                                                          olookup
                                                          mid_idx
                                                          part_size)
                                             I (nth vec__14971 (unchecked-int 0) nil)
                                             mid_segs (nth vec__14971 (unchecked-int 1) nil)
                                             TI (if (< I M) (max TI (* I (- 1.0 (/ I M)))) TI)
                                             TI (max TI 2000000)
                                             S (- (+ I N) TI)]
                                         (if (neg? S)
                                           (let [m_14974 {:M M,
                                                          :I I,
                                                          :index idx_name,
                                                          :mid-segs mid_segs,
                                                          :TI TI,
                                                          :event :index/merge-mid,
                                                          :count
                                                          (java.lang.Integer/valueOf
                                                            (int count_mem_idx)),
                                                          :S S,
                                                          :N N,
                                                          :as-of-t as_of_t}
                                                 ___8598__auto__ (let
                                                                   [logger
                                                                    (org.slf4j.LoggerFactory/getLogger
                                                                      "datomic.index")]
                                                                   (when
                                                                     (.isInfoEnabled
                                                                       ^org.slf4j.Logger logger)
                                                                     (.info
                                                                       ^org.slf4j.Logger logger
                                                                       (logger/process
                                                                         (assoc
                                                                           m_14974
                                                                           :phase
                                                                           :begin))))
                                                                   nil)
                                                 start__8599__auto__ (java.lang.System/nanoTime)
                                                 result__8600__auto__ (try
                                                                        {:returned
                                                                         (let
                                                                           [vec__14978
                                                                            (^clojure.lang.IFn build_index
                                                                              midk
                                                                              (iter/iter-seq
                                                                                (btset/seek
                                                                                  mem_idx))
                                                                              false
                                                                              nil
                                                                              garbage)
                                                                            mid_retid
                                                                            (nth
                                                                              vec__14978
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__14978
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            _retract
                                                                            (nth
                                                                              vec__14978
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            mid_metrics
                                                                            (nth
                                                                              vec__14978
                                                                              (unchecked-int 3)
                                                                              nil)
                                                                            vec__14981
                                                                            (if
                                                                              (seq xpreds)
                                                                              (^clojure.lang.IFn build_index
                                                                                maink
                                                                                []
                                                                                false
                                                                                nil
                                                                                garbage)
                                                                              [(^clojure.lang.IFn maink
                                                                                 root_map)
                                                                               garbage])
                                                                            main_retid
                                                                            (nth
                                                                              vec__14981
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__14981
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            _retract
                                                                            (nth
                                                                              vec__14981
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            main_metrics
                                                                            (nth
                                                                              vec__14981
                                                                              (unchecked-int 3)
                                                                              nil)
                                                                            vec__14984
                                                                            (if
                                                                              (seq xpreds)
                                                                              (^clojure.lang.IFn build_index
                                                                                histk
                                                                                []
                                                                                false
                                                                                nil
                                                                                garbage)
                                                                              [(^clojure.lang.IFn histk
                                                                                 root_map)
                                                                               garbage])
                                                                            hist_retid
                                                                            (nth
                                                                              vec__14984
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__14984
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            _retract
                                                                            (nth
                                                                              vec__14984
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            hist_metrics
                                                                            (nth
                                                                              vec__14984
                                                                              (unchecked-int 3)
                                                                              nil)
                                                                            metrics
                                                                            (datomic.index/aggregate-metrics
                                                                              mid_metrics
                                                                              main_metrics
                                                                              hist_metrics)]
                                                                           (let
                                                                             [logger
                                                                              (org.slf4j.LoggerFactory/getLogger
                                                                                "datomic.index")]
                                                                             (when
                                                                               (.isInfoEnabled
                                                                                 ^org.slf4j.Logger logger)
                                                                               (.info
                                                                                 ^org.slf4j.Logger logger
                                                                                 (logger/process
                                                                                   (merge
                                                                                     metrics
                                                                                     {:event
                                                                                      :index/merged-index,
                                                                                      :mode
                                                                                      :merge-mid,
                                                                                      :index
                                                                                      idx_name,
                                                                                      :count
                                                                                      (java.lang.Integer/valueOf
                                                                                        (int
                                                                                          count_mem_idx)),
                                                                                      :as-of-t
                                                                                      as_of_t}))))
                                                                             nil)
                                                                           [mid_retid
                                                                            main_retid
                                                                            hist_retid
                                                                            garbage
                                                                            metrics])}
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          t__8601__auto__
                                                                          {:threw
                                                                           t__8601__auto__}))
                                                 elapsed_14975 (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8599__auto__)
                                                 msec_14976 (logger/format-as-msec
                                                              (long elapsed_14975))]
                                             (let [endmsg__8602__auto__ (merge
                                                                          (assoc
                                                                            m_14974
                                                                            :msec
                                                                            msec_14976
                                                                            :phase
                                                                            :end)
                                                                          (when
                                                                            (:threw
                                                                              result__8600__auto__)
                                                                            {:threw
                                                                             (class
                                                                               (:threw
                                                                                 result__8600__auto__))}))
                                                   logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.index")]
                                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                 (.info
                                                   ^org.slf4j.Logger logger
                                                   (logger/process endmsg__8602__auto__)))
                                               nil)
                                             (if (contains? result__8600__auto__ :returned)
                                               (:returned result__8600__auto__)
                                               (do (throw (:threw result__8600__auto__)) nil)))
                                           (let [size (long (/ S datomic.index/BYTES_PER_SEG))
                                                 n_segs (if (< size 1) 1 size)
                                                 ratio (fn ratio
                                                         ([num denom]
                                                           (when-not
                                                             (zero? denom)
                                                             (math/round
                                                               (/ (double num) denom)
                                                               2))))
                                                 vec__14988 (let
                                                              [m_14993
                                                               {:event :event/least-pop-slice,
                                                                :index idx_name,
                                                                :n-segs (long n_segs)}
                                                               ___8598__auto__
                                                               (let
                                                                 [logger
                                                                  (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.index")]
                                                                 (when
                                                                   (.isInfoEnabled
                                                                     ^org.slf4j.Logger logger)
                                                                   (.info
                                                                     ^org.slf4j.Logger logger
                                                                     (logger/process
                                                                       (assoc
                                                                         m_14993
                                                                         :phase
                                                                         :begin))))
                                                                 nil)
                                                               start__8599__auto__
                                                               (java.lang.System/nanoTime)
                                                               result__8600__auto__
                                                               (try
                                                                 {:returned
                                                                  (datomic.index/least-pop-slice
                                                                    olookup
                                                                    (map
                                                                      :key
                                                                      (.dir-seq
                                                                        (.seek
                                                                          ^datomic.index.Index mid_idx)))
                                                                    main_idx
                                                                    part_size
                                                                    (long n_segs))}
                                                                 (catch
                                                                   java.lang.Throwable
                                                                   t__8601__auto__
                                                                   {:threw t__8601__auto__}))
                                                               elapsed_14994
                                                               (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8599__auto__)
                                                               msec_14995
                                                               (logger/format-as-msec
                                                                 (long elapsed_14994))]
                                                              (let
                                                                [endmsg__8602__auto__
                                                                 (merge
                                                                   (assoc
                                                                     m_14993
                                                                     :msec
                                                                     msec_14995
                                                                     :phase
                                                                     :end)
                                                                   (when
                                                                     (:threw result__8600__auto__)
                                                                     {:threw
                                                                      (class
                                                                        (:threw
                                                                          result__8600__auto__))}))
                                                                 logger
                                                                 (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.index")]
                                                                (when
                                                                  (.isInfoEnabled
                                                                    ^org.slf4j.Logger logger)
                                                                  (.info
                                                                    ^org.slf4j.Logger logger
                                                                    (logger/process
                                                                      endmsg__8602__auto__)))
                                                                nil)
                                                              (if
                                                                (contains?
                                                                  result__8600__auto__
                                                                  :returned)
                                                                (:returned result__8600__auto__)
                                                                (do
                                                                  (throw
                                                                    (:threw result__8600__auto__))
                                                                  nil)))
                                                 start_key (nth vec__14988 (unchecked-int 0) nil)
                                                 main_pop (nth vec__14988 (unchecked-int 1) nil)
                                                 main_offset (nth vec__14988 (unchecked-int 2) nil)
                                                 slice_segids (take
                                                                (long n_segs)
                                                                (map
                                                                  :seg
                                                                  (.dir-seq
                                                                    (.seek
                                                                      ^datomic.index.Index mid_idx
                                                                      start_key))))
                                                 m_14998 {:M M,
                                                          :I I,
                                                          :main-segs main_segs,
                                                          :index idx_name,
                                                          :main-offset main_offset,
                                                          :main-ratio
                                                          (^clojure.lang.IFn ratio
                                                            main_pop
                                                            main_segs),
                                                          :mid-segs mid_segs,
                                                          :main-pop main_pop,
                                                          :n-segs (long n_segs),
                                                          :mid-ratio
                                                          (^clojure.lang.IFn ratio
                                                            (long n_segs)
                                                            mid_segs),
                                                          :TI TI,
                                                          :event :index/merge-slice,
                                                          :count
                                                          (java.lang.Integer/valueOf
                                                            (int count_mem_idx)),
                                                          :S S,
                                                          :N N,
                                                          :as-of-t as_of_t}
                                                 ___8598__auto__ (let
                                                                   [logger
                                                                    (org.slf4j.LoggerFactory/getLogger
                                                                      "datomic.index")]
                                                                   (when
                                                                     (.isInfoEnabled
                                                                       ^org.slf4j.Logger logger)
                                                                     (.info
                                                                       ^org.slf4j.Logger logger
                                                                       (logger/process
                                                                         (assoc
                                                                           m_14998
                                                                           :phase
                                                                           :begin))))
                                                                   nil)
                                                 start__8599__auto__ (java.lang.System/nanoTime)
                                                 result__8600__auto__ (try
                                                                        {:returned
                                                                         (let
                                                                           [main_ret
                                                                            (let
                                                                              [f__14917__auto__
                                                                               (fn
                                                                                 f__14917__auto__
                                                                                 ([]
                                                                                   (let
                                                                                     [vec__15009
                                                                                      (^clojure.lang.IFn build_index
                                                                                        maink
                                                                                        (mapcat
                                                                                          (fn
                                                                                            fn__15015
                                                                                            ([p1__14919#]
                                                                                              (cache/getx-uncached
                                                                                                olookup
                                                                                                p1__14919#)))
                                                                                          slice_segids)
                                                                                        true
                                                                                        nil
                                                                                        garbage)
                                                                                      retid
                                                                                      (nth
                                                                                        vec__15009
                                                                                        (unchecked-int
                                                                                          0)
                                                                                        nil)
                                                                                      garbage
                                                                                      (nth
                                                                                        vec__15009
                                                                                        (unchecked-int
                                                                                          1)
                                                                                        nil)
                                                                                      retractions
                                                                                      (nth
                                                                                        vec__15009
                                                                                        (unchecked-int
                                                                                          2)
                                                                                        nil)
                                                                                      main_metrics
                                                                                      (nth
                                                                                        vec__15009
                                                                                        (unchecked-int
                                                                                          3)
                                                                                        nil)
                                                                                      _
                                                                                      (let
                                                                                        [logger
                                                                                         (org.slf4j.LoggerFactory/getLogger
                                                                                           "datomic.index")]
                                                                                        (when
                                                                                          (.isInfoEnabled
                                                                                            ^org.slf4j.Logger logger)
                                                                                          (.info
                                                                                            ^org.slf4j.Logger logger
                                                                                            (logger/process
                                                                                              (merge
                                                                                                main_metrics
                                                                                                {:event
                                                                                                 :index/step-1,
                                                                                                 :index
                                                                                                 idx_name}))))
                                                                                        nil)
                                                                                      vec__15012
                                                                                      (if
                                                                                        (or
                                                                                          (seq
                                                                                            retractions)
                                                                                          (seq
                                                                                            xpreds))
                                                                                        (^clojure.lang.IFn build_index
                                                                                          histk
                                                                                          retractions
                                                                                          false
                                                                                          nil
                                                                                          garbage)
                                                                                        [(^clojure.lang.IFn histk
                                                                                           root_map)
                                                                                         garbage])
                                                                                      hist_retid
                                                                                      (nth
                                                                                        vec__15012
                                                                                        (unchecked-int
                                                                                          0)
                                                                                        nil)
                                                                                      garbage
                                                                                      (nth
                                                                                        vec__15012
                                                                                        (unchecked-int
                                                                                          1)
                                                                                        nil)
                                                                                      _retract
                                                                                      (nth
                                                                                        vec__15012
                                                                                        (unchecked-int
                                                                                          2)
                                                                                        nil)
                                                                                      hist_metrics
                                                                                      (nth
                                                                                        vec__15012
                                                                                        (unchecked-int
                                                                                          3)
                                                                                        nil)
                                                                                      _
                                                                                      (let
                                                                                        [logger
                                                                                         (org.slf4j.LoggerFactory/getLogger
                                                                                           "datomic.index")]
                                                                                        (when
                                                                                          (.isInfoEnabled
                                                                                            ^org.slf4j.Logger logger)
                                                                                          (.info
                                                                                            ^org.slf4j.Logger logger
                                                                                            (logger/process
                                                                                              (merge
                                                                                                (datomic.index/aggregate-metrics
                                                                                                  main_metrics
                                                                                                  hist_metrics)
                                                                                                {:event
                                                                                                 :index/step-2,
                                                                                                 :index
                                                                                                 idx_name}))))
                                                                                        nil)]
                                                                                     [retid
                                                                                      hist_retid
                                                                                      garbage
                                                                                      main_metrics
                                                                                      hist_metrics])))]
                                                                              (if
                                                                                (config/property
                                                                                  "datomic.indexParallelMerges")
                                                                                (future-call
                                                                                  f__14917__auto__)
                                                                                (atom
                                                                                  (^clojure.lang.IFn f__14917__auto__))))
                                                                            vec__15002
                                                                            (^clojure.lang.IFn build_index
                                                                              midk
                                                                              (iter/iter-seq
                                                                                (btset/seek
                                                                                  mem_idx))
                                                                              false
                                                                              (set slice_segids)
                                                                              garbage)
                                                                            mid_retid
                                                                            (nth
                                                                              vec__15002
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__15002
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            _retract
                                                                            (nth
                                                                              vec__15002
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            mid_metrics
                                                                            (nth
                                                                              vec__15002
                                                                              (unchecked-int 3)
                                                                              nil)
                                                                            garbage
                                                                            (concat
                                                                              garbage
                                                                              slice_segids)
                                                                            vec__15005
                                                                            (deref main_ret)
                                                                            retid
                                                                            (nth
                                                                              vec__15005
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            hist_retid
                                                                            (nth
                                                                              vec__15005
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            main_garbage
                                                                            (nth
                                                                              vec__15005
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            main_metrics
                                                                            (nth
                                                                              vec__15005
                                                                              (unchecked-int 3)
                                                                              nil)
                                                                            hist_metrics
                                                                            (nth
                                                                              vec__15005
                                                                              (unchecked-int 4)
                                                                              nil)
                                                                            metrics
                                                                            (datomic.index/aggregate-metrics
                                                                              main_metrics
                                                                              hist_metrics
                                                                              mid_metrics)]
                                                                           (let
                                                                             [logger
                                                                              (org.slf4j.LoggerFactory/getLogger
                                                                                "datomic.index")]
                                                                             (when
                                                                               (.isInfoEnabled
                                                                                 ^org.slf4j.Logger logger)
                                                                               (.info
                                                                                 ^org.slf4j.Logger logger
                                                                                 (logger/process
                                                                                   (merge
                                                                                     metrics
                                                                                     {:event
                                                                                      :index/merged-index,
                                                                                      :mode
                                                                                      :merge-slice,
                                                                                      :index
                                                                                      idx_name,
                                                                                      :count
                                                                                      (java.lang.Integer/valueOf
                                                                                        (int
                                                                                          count_mem_idx)),
                                                                                      :as-of-t
                                                                                      as_of_t}))))
                                                                             nil)
                                                                           [mid_retid
                                                                            retid
                                                                            hist_retid
                                                                            (concat
                                                                              garbage
                                                                              main_garbage)
                                                                            metrics])}
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          t__8601__auto__
                                                                          {:threw
                                                                           t__8601__auto__}))
                                                 elapsed_14999 (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8599__auto__)
                                                 msec_15000 (logger/format-as-msec
                                                              (long elapsed_14999))]
                                             (let [endmsg__8602__auto__ (merge
                                                                          (assoc
                                                                            m_14998
                                                                            :msec
                                                                            msec_15000
                                                                            :phase
                                                                            :end)
                                                                          (when
                                                                            (:threw
                                                                              result__8600__auto__)
                                                                            {:threw
                                                                             (class
                                                                               (:threw
                                                                                 result__8600__auto__))}))
                                                   logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.index")]
                                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                 (.info
                                                   ^org.slf4j.Logger logger
                                                   (logger/process endmsg__8602__auto__)))
                                               nil)
                                             (if (contains? result__8600__auto__ :returned)
                                               (:returned result__8600__auto__)
                                               (do
                                                 (throw (:threw result__8600__auto__))
                                                 nil)))))))))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :eavt})))
                nil)
            eavt_ret (let [f__14917__auto__ (fn f__14917__auto__
                                              ([]
                                                (^clojure.lang.IFn build_tiered_index
                                                  :eavt
                                                  :eavt-mid
                                                  (datomic.index/idx-key :eavt-main :eavt)
                                                  :eavt-hist
                                                  (constantly 42)
                                                  (datomic.index/dir-partition-size :eavt)
                                                  (.-eavt (.-indexing ^datomic.db.Db db))
                                                  (.-eavt (.-mid-index ^datomic.db.Db db))
                                                  (.-eavt (.-index ^datomic.db.Db db))
                                                  db/eavt-cmp
                                                  datomic.index/eavt-cmpi
                                                  datomic.index/common-write-handlers
                                                  :eavt)))]
                       (if (config/property "datomic.indexParallelMerges")
                         (future-call f__14917__auto__)
                         (atom (^clojure.lang.IFn f__14917__auto__))))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :avet})))
                nil)
            avet_ret (let [f__14917__auto__ (fn f__14917__auto__
                                              ([]
                                                (^clojure.lang.IFn build_tiered_index
                                                  :avet
                                                  :avet-mid
                                                  (datomic.index/idx-key :avet-main :avet)
                                                  :avet-hist
                                                  (fn fn__15047
                                                    ([p1__14920#]
                                                      (java.lang.Integer/valueOf
                                                        (int
                                                          (.getA
                                                            ^datomic.impl.db.IDatum p1__14920#)))))
                                                  (datomic.index/dir-partition-size :avet)
                                                  (.-avet (.-indexing ^datomic.db.Db db))
                                                  (.-avet (.-mid-index ^datomic.db.Db db))
                                                  (.-avet (.-index ^datomic.db.Db db))
                                                  db/avet-cmp
                                                  datomic.index/avet-cmpi
                                                  datomic.index/common-write-handlers
                                                  :avet)))]
                       (if (config/property "datomic.indexParallelMerges")
                         (future-call f__14917__auto__)
                         (atom (^clojure.lang.IFn f__14917__auto__))))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :aevt})))
                nil)
            aevt_ret (let [f__14917__auto__ (fn f__14917__auto__
                                              ([]
                                                (^clojure.lang.IFn build_tiered_index
                                                  :aevt
                                                  :aevt-mid
                                                  (datomic.index/idx-key :aevt-main :aevt)
                                                  :aevt-hist
                                                  (fn fn__15051
                                                    ([p1__14921#]
                                                      (java.lang.Integer/valueOf
                                                        (int
                                                          (.getA
                                                            ^datomic.impl.db.IDatum p1__14921#)))))
                                                  (datomic.index/dir-partition-size :aevt)
                                                  (.-aevt (.-indexing ^datomic.db.Db db))
                                                  (.-aevt (.-mid-index ^datomic.db.Db db))
                                                  (.-aevt (.-index ^datomic.db.Db db))
                                                  db/aevt-cmp
                                                  datomic.index/aevt-cmpi
                                                  datomic.index/common-write-handlers
                                                  :aevt)))]
                       (if (config/property "datomic.indexParallelMerges")
                         (future-call f__14917__auto__)
                         (atom (^clojure.lang.IFn f__14917__auto__))))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :ft})))
                nil)
            fulltext_ret (if fulltext_enabled?
                           (let [pario (config/property "datomic.indexIOParallelism")
                                 cstore (if pario
                                          (cluster/queueing-writer
                                            cstore
                                            pario
                                            cluster/BOUNDING_TIMEOUT_MSEC
                                            (fn fn__15054
                                              ([p1__14922#]
                                                (monitor/add-stat :IndexIOQueueCount p1__14922#))))
                                          cstore)
                                 ret (atom
                                       (fulltext/build-index
                                         cstore
                                         olookup
                                         db
                                         (.-aevt (.-indexing ^datomic.db.Db db))
                                         (map
                                           (fn fn__15056
                                             ([p1__14923#] (db/resolve-id db p1__14923#)))
                                           (db/fulltext-attrs db))
                                         (:fulltext root_map)
                                         (:fulltext-hist root_map)))]
                             (when pario
                               (common/bounded-deref
                                 (cluster/finish-writer cstore)
                                 cluster/BOUNDING_TIMEOUT_MSEC))
                             ret)
                           (atom [(:fulltext root_map) (:fulltext-hist root_map) nil]))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :raet})))
                nil)
            vec__14927 (^clojure.lang.IFn build_tiered_index
                         :raet
                         :raet-mid
                         (datomic.index/idx-key :raet-main :raet)
                         :raet-hist
                         (constantly 42)
                         (datomic.index/dir-partition-size :raet)
                         (.-raet (.-indexing ^datomic.db.Db db))
                         (.-raet (.-mid-index ^datomic.db.Db db))
                         (.-raet (.-index ^datomic.db.Db db))
                         db/raet-cmp
                         datomic.index/raet-cmpi
                         datomic.index/common-write-handlers
                         :raet)
            mid_raetid (nth vec__14927 (unchecked-int 0) nil)
            raetid (nth vec__14927 (unchecked-int 1) nil)
            hist_raetid (nth vec__14927 (unchecked-int 2) nil)
            garbage (nth vec__14927 (unchecked-int 3) nil)
            raet_metrics (nth vec__14927 (unchecked-int 4) nil)
            vec__14930 (deref eavt_ret)
            mid_eavtid (nth vec__14930 (unchecked-int 0) nil)
            eavtid (nth vec__14930 (unchecked-int 1) nil)
            hist_eavtid (nth vec__14930 (unchecked-int 2) nil)
            eavt_garbage (nth vec__14930 (unchecked-int 3) nil)
            eavt_metrics (nth vec__14930 (unchecked-int 4) nil)
            vec__14933 (deref avet_ret)
            mid_avetid (nth vec__14933 (unchecked-int 0) nil)
            avetid (nth vec__14933 (unchecked-int 1) nil)
            hist_avetid (nth vec__14933 (unchecked-int 2) nil)
            avet_garbage (nth vec__14933 (unchecked-int 3) nil)
            avet_metrics (nth vec__14933 (unchecked-int 4) nil)
            vec__14936 (let [temp__5823__auto__ (seq (datomic.index/dropped-avet-aids db))]
                         (if temp__5823__auto__
                           (let [attrids temp__5823__auto__]
                             (datomic.index/drop-avet-indexes
                               cstore
                               olookup
                               [mid_avetid avetid hist_avetid]
                               attrids
                               as_of_t
                               avet_garbage))
                           [[mid_avetid avetid hist_avetid] avet_garbage]))
            vec__14939 (nth vec__14936 (unchecked-int 0) nil)
            mid_avetid (nth vec__14939 (unchecked-int 0) nil)
            avetid (nth vec__14939 (unchecked-int 1) nil)
            hist_avetid (nth vec__14939 (unchecked-int 2) nil)
            avet_garbage (nth vec__14936 (unchecked-int 1) nil)
            vec__14942 (deref aevt_ret)
            mid_aevtid (nth vec__14942 (unchecked-int 0) nil)
            aevtid (nth vec__14942 (unchecked-int 1) nil)
            hist_aevtid (nth vec__14942 (unchecked-int 2) nil)
            aevt_garbage (nth vec__14942 (unchecked-int 3) nil)
            aevt_metrics (nth vec__14942 (unchecked-int 4) nil)
            vec__14945 (deref fulltext_ret)
            fulltextid (nth vec__14945 (unchecked-int 0) nil)
            hist_fulltextid (nth vec__14945 (unchecked-int 1) nil)
            fulltext_garbage_keys (nth vec__14945 (unchecked-int 2) nil)
            garbage (into
                      []
                      (concat
                        fulltext_garbage_keys
                        (map
                          cluster/uuid->val-key
                          (concat garbage eavt_garbage avet_garbage aevt_garbage))))
            rootid (common/rand-uuid)
            root (datomic.index/fress
                   (merge
                     extra
                     {:birth-level (:birth-level db),
                      :nextT (long (.nextT ^datomic.db.Db db)),
                      :schema-level (:schema-level db),
                      :eavt-main eavtid,
                      :basisT (long (.basisT ^datomic.db.Db db)),
                      :fulltext-hist hist_fulltextid,
                      :fulltext fulltextid,
                      :avet-mid mid_avetid,
                      :aevt-main aevtid,
                      :rev (inc (.-index-rev ^datomic.db.Db db)),
                      :raet-main raetid,
                      :raet-mid mid_raetid,
                      :buildRevision (config/property "datomic.buildRevision"),
                      :avet-hist hist_avetid,
                      :eavt-hist hist_eavtid,
                      :raet-hist hist_raetid,
                      :aevt-mid mid_aevtid,
                      :version 2,
                      :avet-main avetid,
                      :aevt-hist hist_aevtid,
                      :eavt-mid mid_eavtid})
                   datomic.index/common-write-handlers)]
        (datomic.index/write-vals cstore {rootid root})
        (let [map__15058 (datomic.index/aggregate-metrics
                           raet_metrics
                           eavt_metrics
                           avet_metrics
                           aevt_metrics)
              map__15058 (if (seq? map__15058)
                           (if (next map__15058)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__15058))
                             (if (seq map__15058) (first map__15058) {}))
                           map__15058)
              written (get map__15058 :written)
              dirs_written (get map__15058 :dirs-written)]
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info
                ^org.slf4j.Logger logger
                (logger/process
                  {:event :index/create-index,
                   :root-id rootid,
                   :written written,
                   :dirs-written dirs_written,
                   :as-of-t as_of_t,
                   :msec
                   (logger/format-as-msec (long (- (java.lang.System/nanoTime) job_started))),
                   :datoms
                   (let [btset (:aevt (:indexing db))]
                     (long (.longCount ^datomic.btset.IDataSet btset)))})))
            nil)
          (monitor/add-stat :IndexWrites written)
          (monitor/add-stat :IndexDirWrites dirs_written))
        [rootid xpreds (conj garbage (cluster/uuid->val-key old_root_id))])))
  (reset-meta!
    #'merge-db*
    (assoc
      {:arglists
       (clojure.core/list
         ['cstore
          'olookup
          (.withMeta 'db {:tag 'Db})
          'as-of-t
          'old-root-id
          'extra
          'fulltext-enabled?
          'excise-enabled?]),
       :column (int 1)}
      :name
      'merge-db*
      :ns
      *ns*))
  (defn merge-db
    ([cstore olookup db as_of_t]
      (when-not (.-indexing ^datomic.db.Db db)
        (throw
          (java.lang.IllegalStateException. "db must be prepared with db/prepare-for-indexing")))
      (try
        (let [m_15066 {:event :index/merge-db, :as-of-t as_of_t}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_15066 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [index_ref_key (datomic.index/index-ref-key-name cstore)
                                            storage_index_root_ref (deref
                                                                     (cluster/get-ref
                                                                       cstore
                                                                       (datomic.index/index-ref-key-name
                                                                         cstore)))
                                            storage_index_root_id (cluster/val-key->uuid
                                                                    (:key storage_index_root_ref))
                                            root_map (common/getx olookup storage_index_root_id)
                                            db (datomic.index/repair-disjoined
                                                 db
                                                 olookup
                                                 root_map)]
                                        (if (>
                                              (common/getx root_map :rev)
                                              (.-index-rev ^datomic.db.Db db))
                                          {:new-index
                                           (datomic.index/load-index
                                             olookup
                                             storage_index_root_id)}
                                          (binding [datomic.index/*pace-index-fn* (let
                                                                                    [temp__5825__auto__
                                                                                     (config/property
                                                                                       "datomic.indexSegsPerSecond")]
                                                                                    (when
                                                                                      temp__5825__auto__
                                                                                      (let
                                                                                        [sps
                                                                                         temp__5825__auto__
                                                                                         calc
                                                                                         (datomic.index/create-pace-calculator
                                                                                           (/
                                                                                             sps
                                                                                             10)
                                                                                           100)]
                                                                                        (fn
                                                                                          fn__15070
                                                                                          ([segs]
                                                                                            (let
                                                                                              [temp__5825__auto__
                                                                                               (^clojure.lang.IFn calc
                                                                                                 segs)]
                                                                                              (when
                                                                                                temp__5825__auto__
                                                                                                (let
                                                                                                  [msec
                                                                                                   temp__5825__auto__]
                                                                                                  (monitor/add-stat
                                                                                                    :IndexPacingMsec
                                                                                                    msec)
                                                                                                  (java.lang.Thread/sleep
                                                                                                    (unchecked-long
                                                                                                      ^java.lang.Number msec))
                                                                                                  nil))))))))]
                                            (let [vec__15074 (datomic.index/merge-db*
                                                               cstore
                                                               olookup
                                                               db
                                                               as_of_t
                                                               (:key storage_index_root_ref)
                                                               nil
                                                               true
                                                               true)
                                                  rootid (nth vec__15074 (unchecked-int 0) nil)
                                                  xpreds (nth vec__15074 (unchecked-int 1) nil)
                                                  garbage (nth vec__15074 (unchecked-int 2) nil)
                                                  cluster_rev (inc
                                                                (common/getx
                                                                  storage_index_root_ref
                                                                  :rev))]
                                              (process/throw-if-failing!)
                                              (if (=
                                                    :ok
                                                    (deref
                                                      (cluster/set-ref
                                                        cstore
                                                        index_ref_key
                                                        cluster_rev
                                                        (cluster/uuid->val-key rootid))))
                                                (let [ret (datomic.index/load-index
                                                            olookup
                                                            rootid)]
                                                  (events/publish
                                                    {:key :datomic.garbage/mark,
                                                     :cluster cstore,
                                                     :garbage garbage})
                                                  {:new-index ret, :xpreds xpreds})
                                                (process/fail
                                                  process/instance
                                                  (str
                                                    "Conflict updating index root at rev "
                                                    cluster_rev)))))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_15067 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_15068 (logger/format-as-msec (long elapsed_15067))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_15066 :msec msec_15068 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil)))
        (catch
          java.lang.Throwable
          ex
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index") ex ex]
              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                (.warn
                  ^org.slf4j.Logger logger
                  (logger/process "merge-db failed")
                  ^java.lang.Throwable ex)
                (logger/caused-by logger ex))
              nil)
            (throw ^java.lang.Throwable ex)
            nil)))))
  (reset-meta!
    #'merge-db
    (assoc
      {:arglists (clojure.core/list ['cstore 'olookup (.withMeta 'db {:tag 'Db}) 'as-of-t]),
       :column (int 1)}
      :name
      'merge-db
      :ns
      *ns*)))

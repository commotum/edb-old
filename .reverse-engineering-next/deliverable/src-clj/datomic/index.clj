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
        (if (instance? java.lang.Throwable result)
          (do (throw ^java.lang.Throwable result) nil)
          result))))
  (reset-meta!
    #'deref-or-throw
    (assoc
      {:private true, :arglists (clojure.core/list ['ref]), :column 1}
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
  (defn transposed-data
    ([es as vs ts ops]
      (let [eas (long-array (long (* 2 (alength ^longs es))))]
        (dotimes [i (alength ^longs es)]
          (aset ^longs eas (int (* 2 i)) (long (aget ^longs es i)))
          (aset ^longs eas (int (inc (* 2 i))) (long (aget ^ints as i))))
        (datomic.index.TransposedData. (int (count vs)) eas vs ts ops))))
  (deftype RootNode [keydata dirids dirs])
  (clojure.core/import 'datomic.index.RootNode)
  (defn ->RootNode ([keydata dirids dirs] (datomic.index.RootNode. keydata dirids dirs)))
  (defn root-node
    ([keydata dirids]
      (datomic.index.RootNode.
        keydata
        dirids
        (object-array (java.lang.Integer/valueOf (int (alength ^"[Ljava.lang.Object;" dirids)))))))
  (deftype DirNode [keydata segids offsets counts segs])
  (clojure.core/import 'datomic.index.DirNode)
  (defn ->DirNode
    ([keydata segids offsets counts segs]
      (datomic.index.DirNode. keydata segids offsets counts segs)))
  (defn dir-node
    ([keydata segids offsets counts]
      (datomic.index.DirNode.
        keydata
        segids
        offsets
        counts
        (object-array (java.lang.Integer/valueOf (int (alength ^"[Ljava.lang.Object;" segids)))))))
  (def array-cache-ref (delay (config/property "datomic.useIndexArrayCaches")))
  (defn use-array-cache? ([] (deref datomic.index/array-cache-ref)))
  (defn get-dir-node
    ([root ridx lookup cache?]
      (let [k (aget (.-dirids ^datomic.index.RootNode root) (unchecked-int ridx))
            ac (datomic.index/use-array-cache?)]
        (io-stats/inc! :dir)
        (io-trace/note! k :dir)
        (or
          (when ac (aget (.-dirs ^datomic.index.RootNode root) (unchecked-int ridx)))
          (let [dir (common/getx
                      lookup
                      (aget (.-dirids ^datomic.index.RootNode root) (unchecked-int ridx)))]
            (let [and__5236__auto__ ac]
              (when and__5236__auto__
                (let [and__5236__auto__ cache?]
                  (when and__5236__auto__
                    (aset (.-dirs ^datomic.index.RootNode root) (unchecked-int ridx) dir)))))
            dir)))))
  (defonce ITreeIter {})
  (defprotocol ITreeIter (seg+item-seq [iter]) (seg-seq [iter]) (dir-seq [iter]))
  (defonce IIndex {})
  (defprotocol IIndex (seek-seg [idx k]))
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
      (let [iter__6025__auto__ (fn iter__15146
                                 ([s__15147]
                                   (lazy-seq
                                     (loop [s__15147 s__15147]
                                       (let [temp__5457__auto__ (seq s__15147)]
                                         (when temp__5457__auto__
                                           (let [xs__6012__auto__ temp__5457__auto__
                                                 ri (first xs__6012__auto__)
                                                 d (datomic.index/get-dir-node
                                                     root
                                                     ri
                                                     lookup
                                                     false)
                                                 iterys__6021__auto__ (fn 
                                                                        iter__15148
                                                                        ([s__15149]
                                                                          (lazy-seq
                                                                            (loop 
                                                                              [s__15149 s__15149]
                                                                              (let 
                                                                                [temp__5457__auto__
                                                                                 (seq s__15149)]
                                                                                (when
                                                                                  temp__5457__auto__
                                                                                  (let 
                                                                                    [s__15149
                                                                                     temp__5457__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        s__15149)
                                                                                      (let 
                                                                                        [c__6023__auto__
                                                                                         (chunk-first
                                                                                           s__15149)
                                                                                         size__6024__auto__
                                                                                         (count
                                                                                           c__6023__auto__)
                                                                                         b__15151
                                                                                         (chunk-buffer
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               size__6024__auto__)))]
                                                                                        (if
                                                                                          (loop 
                                                                                            [i__15150
                                                                                             0]
                                                                                            (if
                                                                                              (<
                                                                                                i__15150
                                                                                                size__6024__auto__)
                                                                                              (let 
                                                                                                [di
                                                                                                 (.nth
                                                                                                   ^clojure.lang.Indexed c__6023__auto__
                                                                                                   (unchecked-int
                                                                                                     i__15150))]
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
                                                                                                      b__15151
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
                                                                                                        i__15150)))
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__15150))))
                                                                                              true))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__15151)
                                                                                            (^clojure.lang.IFn iter__15148
                                                                                              (chunk-rest
                                                                                                s__15149)))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__15151)
                                                                                            nil)))
                                                                                      (let 
                                                                                        [di
                                                                                         (first
                                                                                           s__15149)]
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
                                                                                            (^clojure.lang.IFn iter__15148
                                                                                              (rest
                                                                                                s__15149)))
                                                                                          (recur
                                                                                            (rest
                                                                                              s__15149))))))))))))
                                                 fs__6022__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6021__auto__
                                                                      (range
                                                                        (java.lang.Integer/valueOf
                                                                          (int
                                                                            (count
                                                                              (.-segids
                                                                                ^datomic.index.DirNode d)))))))]
                                             (if fs__6022__auto__
                                               (concat
                                                 fs__6022__auto__
                                                 (^clojure.lang.IFn iter__15146 (rest s__15147)))
                                               (recur (rest s__15147))))))))))]
        (^clojure.lang.IFn iter__6025__auto__
          (range
            (java.lang.Integer/valueOf (int ridx))
            (java.lang.Integer/valueOf (int (count (.-dirids ^datomic.index.RootNode root))))))))
    (seg-seq
      [this]
      (let [iter__6025__auto__ (fn iter__15121
                                 ([s__15122]
                                   (lazy-seq
                                     (loop [s__15122 s__15122]
                                       (let [temp__5457__auto__ (seq s__15122)]
                                         (when temp__5457__auto__
                                           (let [xs__6012__auto__ temp__5457__auto__
                                                 ri (first xs__6012__auto__)
                                                 d (datomic.index/get-dir-node
                                                     root
                                                     ri
                                                     lookup
                                                     false)
                                                 iterys__6021__auto__ (fn 
                                                                        iter__15123
                                                                        ([s__15124]
                                                                          (lazy-seq
                                                                            (loop 
                                                                              [s__15124 s__15124]
                                                                              (let 
                                                                                [temp__5457__auto__
                                                                                 (seq s__15124)]
                                                                                (when
                                                                                  temp__5457__auto__
                                                                                  (let 
                                                                                    [s__15124
                                                                                     temp__5457__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        s__15124)
                                                                                      (let 
                                                                                        [c__6023__auto__
                                                                                         (chunk-first
                                                                                           s__15124)
                                                                                         size__6024__auto__
                                                                                         (count
                                                                                           c__6023__auto__)
                                                                                         b__15126
                                                                                         (chunk-buffer
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               size__6024__auto__)))]
                                                                                        (if
                                                                                          (loop 
                                                                                            [i__15125
                                                                                             0]
                                                                                            (if
                                                                                              (<
                                                                                                i__15125
                                                                                                size__6024__auto__)
                                                                                              (let 
                                                                                                [di
                                                                                                 (.nth
                                                                                                   ^clojure.lang.Indexed c__6023__auto__
                                                                                                   (unchecked-int
                                                                                                     i__15125))]
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
                                                                                                      b__15126
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
                                                                                                        i__15125)))
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__15125))))
                                                                                              true))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__15126)
                                                                                            (^clojure.lang.IFn iter__15123
                                                                                              (chunk-rest
                                                                                                s__15124)))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__15126)
                                                                                            nil)))
                                                                                      (let 
                                                                                        [di
                                                                                         (first
                                                                                           s__15124)]
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
                                                                                            (^clojure.lang.IFn iter__15123
                                                                                              (rest
                                                                                                s__15124)))
                                                                                          (recur
                                                                                            (rest
                                                                                              s__15124))))))))))))
                                                 fs__6022__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6021__auto__
                                                                      (range
                                                                        (java.lang.Integer/valueOf
                                                                          (int
                                                                            (count
                                                                              (.-segids
                                                                                ^datomic.index.DirNode d)))))))]
                                             (if fs__6022__auto__
                                               (concat
                                                 fs__6022__auto__
                                                 (^clojure.lang.IFn iter__15121 (rest s__15122)))
                                               (recur (rest s__15122))))))))))]
        (^clojure.lang.IFn iter__6025__auto__
          (range
            (java.lang.Integer/valueOf (int ridx))
            (java.lang.Integer/valueOf (int (count (.-dirids ^datomic.index.RootNode root))))))))
    (seg+item-seq
      [this]
      (let [segids (let [iter__6025__auto__ (fn iter__15092
                                              ([s__15093]
                                                (lazy-seq
                                                  (loop [s__15093 s__15093]
                                                    (let [temp__5457__auto__ (seq s__15093)]
                                                      (when temp__5457__auto__
                                                        (let [xs__6012__auto__ temp__5457__auto__
                                                              ri (first xs__6012__auto__)
                                                              d
                                                              (datomic.index/get-dir-node
                                                                root
                                                                ri
                                                                lookup
                                                                false)
                                                              iterys__6021__auto__
                                                              (fn 
                                                                iter__15094
                                                                ([s__15095]
                                                                  (lazy-seq
                                                                    (loop 
                                                                      [s__15095 s__15095]
                                                                      (let 
                                                                        [temp__5457__auto__
                                                                         (seq s__15095)]
                                                                        (when
                                                                          temp__5457__auto__
                                                                          (let 
                                                                            [s__15095
                                                                             temp__5457__auto__]
                                                                            (if
                                                                              (chunked-seq?
                                                                                s__15095)
                                                                              (let 
                                                                                [c__6023__auto__
                                                                                 (chunk-first
                                                                                   s__15095)
                                                                                 size__6024__auto__
                                                                                 (count
                                                                                   c__6023__auto__)
                                                                                 b__15097
                                                                                 (chunk-buffer
                                                                                   (java.lang.Integer/valueOf
                                                                                     (int
                                                                                       size__6024__auto__)))]
                                                                                (if
                                                                                  (loop 
                                                                                    [i__15096 0]
                                                                                    (if
                                                                                      (<
                                                                                        i__15096
                                                                                        size__6024__auto__)
                                                                                      (let 
                                                                                        [di
                                                                                         (.nth
                                                                                           ^clojure.lang.Indexed c__6023__auto__
                                                                                           (unchecked-int
                                                                                             i__15096))]
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
                                                                                              b__15097
                                                                                              (nth
                                                                                                (.-segids
                                                                                                  ^datomic.index.DirNode d)
                                                                                                (unchecked-int
                                                                                                  ^java.lang.Number di)))
                                                                                            (recur
                                                                                              (inc
                                                                                                i__15096)))
                                                                                          (recur
                                                                                            (inc
                                                                                              i__15096))))
                                                                                      true))
                                                                                  (chunk-cons
                                                                                    (chunk
                                                                                      b__15097)
                                                                                    (^clojure.lang.IFn iter__15094
                                                                                      (chunk-rest
                                                                                        s__15095)))
                                                                                  (chunk-cons
                                                                                    (chunk
                                                                                      b__15097)
                                                                                    nil)))
                                                                              (let 
                                                                                [di
                                                                                 (first s__15095)]
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
                                                                                    (^clojure.lang.IFn iter__15094
                                                                                      (rest
                                                                                        s__15095)))
                                                                                  (recur
                                                                                    (rest
                                                                                      s__15095))))))))))))
                                                              fs__6022__auto__
                                                              (seq
                                                                (^clojure.lang.IFn iterys__6021__auto__
                                                                  (range
                                                                    (java.lang.Integer/valueOf
                                                                      (int
                                                                        (count
                                                                          (.-segids
                                                                            ^datomic.index.DirNode d)))))))]
                                                          (if fs__6022__auto__
                                                            (concat
                                                              fs__6022__auto__
                                                              (^clojure.lang.IFn iter__15092
                                                                (rest s__15093)))
                                                            (recur (rest s__15093))))))))))]
                     (^clojure.lang.IFn iter__6025__auto__
                       (range
                         (java.lang.Integer/valueOf (int ridx))
                         (java.lang.Integer/valueOf
                           (int (count (.-dirids ^datomic.index.RootNode root)))))))]
        (mapcat
          (fn fn__15117
            ([segid]
              (map
                (fn fn__15118 ([item] {:segid segid, :item item}))
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
  (definterface
    IBinarySearch
    (^long search [^datomic.index.TransposedData arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.index.IBinarySearch)
  (definterface
    IndexedComparator
    (^long compare [^datomic.db.Datum arg0 ^datomic.index.TransposedData arg1 ^long arg2]))
  (clojure.core/import 'datomic.index.IndexedComparator)
  (def eavt-cmpi
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
  (def avet-cmpi
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
  (def aevt-cmpi
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
  (def raet-cmpi
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
      {:private true, :arglists (clojure.core/list ['coll 'k 'cmp]), :column 1}
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
      {:private true, :arglists (clojure.core/list ['coll 'k 'cmp]), :column 1}
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
       :column 1}
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
       :column 1}
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
                    ac (datomic.index/use-array-cache?)
                    seg (or
                          (when ac
                            (let [seg (aget (.-segs ^datomic.index.DirNode dir) (int didx))
                                  temp__5457__auto__ (and seg (.get ^java.lang.ref.Reference seg))]
                              (when temp__5457__auto__
                                (let [ret temp__5457__auto__]
                                  (io-stats/inc! io-stats/*io-index*)
                                  ret))))
                          (let [seg (common/getx
                                      lookup
                                      (aget (.-segids ^datomic.index.DirNode dir) (int didx)))]
                            (when ac
                              (aset
                                (.-segs ^datomic.index.DirNode dir)
                                (int didx)
                                (java.lang.ref.WeakReference. seg)))
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
        (let [iter__6025__auto__ (fn iter__15197
                                   ([s__15198]
                                     (lazy-seq
                                       (loop [s__15198 s__15198]
                                         (let [temp__5457__auto__ (seq s__15198)]
                                           (when temp__5457__auto__
                                             (let [xs__6012__auto__ temp__5457__auto__
                                                   ri (first xs__6012__auto__)
                                                   d (datomic.index/get-dir-node
                                                       root
                                                       ri
                                                       lookup
                                                       false)
                                                   iterys__6021__auto__ (fn 
                                                                          iter__15199
                                                                          ([s__15200]
                                                                            (lazy-seq
                                                                              (let 
                                                                                [s__15200 s__15200
                                                                                 temp__5457__auto__
                                                                                 (seq s__15200)]
                                                                                (when
                                                                                  temp__5457__auto__
                                                                                  (let 
                                                                                    [s__15200
                                                                                     temp__5457__auto__]
                                                                                    (if
                                                                                      (chunked-seq?
                                                                                        s__15200)
                                                                                      (let 
                                                                                        [c__6023__auto__
                                                                                         (chunk-first
                                                                                           s__15200)
                                                                                         size__6024__auto__
                                                                                         (count
                                                                                           c__6023__auto__)
                                                                                         b__15202
                                                                                         (chunk-buffer
                                                                                           (java.lang.Integer/valueOf
                                                                                             (int
                                                                                               size__6024__auto__)))]
                                                                                        (if
                                                                                          (loop 
                                                                                            [i__15201
                                                                                             0]
                                                                                            (if
                                                                                              (<
                                                                                                i__15201
                                                                                                size__6024__auto__)
                                                                                              (let 
                                                                                                [di
                                                                                                 (.nth
                                                                                                   ^clojure.lang.Indexed c__6023__auto__
                                                                                                   (unchecked-int
                                                                                                     i__15201))]
                                                                                                (chunk-append
                                                                                                  b__15202
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
                                                                                                    i__15201)))
                                                                                              true))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__15202)
                                                                                            (^clojure.lang.IFn iter__15199
                                                                                              (chunk-rest
                                                                                                s__15200)))
                                                                                          (chunk-cons
                                                                                            (chunk
                                                                                              b__15202)
                                                                                            nil)))
                                                                                      (let 
                                                                                        [di
                                                                                         (first
                                                                                           s__15200)]
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
                                                                                          (^clojure.lang.IFn iter__15199
                                                                                            (rest
                                                                                              s__15200)))))))))))
                                                   fs__6022__auto__ (seq
                                                                      (^clojure.lang.IFn iterys__6021__auto__
                                                                        (range
                                                                          (java.lang.Integer/valueOf
                                                                            (int
                                                                              (count
                                                                                (.-segids
                                                                                  ^datomic.index.DirNode d)))))))]
                                               (if fs__6022__auto__
                                                 (concat
                                                   fs__6022__auto__
                                                   (^clojure.lang.IFn iter__15197 (rest s__15198)))
                                                 (recur (rest s__15198))))))))))]
          (^clojure.lang.IFn iter__6025__auto__
            (range
              (java.lang.Integer/valueOf
                (int (count (.-dirids ^datomic.index.RootNode root)))))))))
    (^int count [this] (int (.longCount this)))
    (get
      [this k]
      (let [temp__5457__auto__ (.seek this k)]
        (when temp__5457__auto__
          (let [i temp__5457__auto__]
            (when (= k (.get ^datomic.iter.Iter i)) (.get ^datomic.iter.Iter i))))))
    (^boolean contains
      [this k]
      (boolean
        (let [temp__5457__auto__ (.seek this k)]
          (when temp__5457__auto__
            (let [i temp__5457__auto__] (= k (.get ^datomic.iter.Iter i)))))))
    (^clojure.lang.IPersistentSet disjoin
      [this k]
      (do (throw (java.lang.UnsupportedOperationException.)) nil))
    (^boolean equiv [this x] (.booleanValue false)))
  (clojure.core/import 'datomic.index.Index)
  (defn ->Index
    ([lookup cmpi root cached_count order]
      (datomic.index.Index. lookup cmpi root cached_count order)))
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
  (def common-write-handlers
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
  (def index-read-handlers
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
      (let [temp__5457__auto__ (cluster/dbId cs)]
        (when temp__5457__auto__ (let [dbid temp__5457__auto__] (str "ref-index-root/" dbid))))))
  (defn fress
    ([val handlers] (io/gzip-buffer (fressian/byte-buf val :handlers handlers :footer true))))
  (reset-meta!
    #'fress
    (assoc
      {:tag java.nio.ByteBuffer, :arglists (clojure.core/list ['val 'handlers]), :column 1}
      :name
      'fress
      :ns
      *ns*))
  (defn transpose
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
        (datomic.index/transposed-data es as vs ts ops))))
  (reset-meta!
    #'transpose
    (assoc
      {:tag datomic.index.TransposedData, :arglists (clojure.core/list ['data]), :column 1}
      :name
      'transpose
      :ns
      *ns*))
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
                   (fn fn__15250
                     ([p1__15249#]
                       (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer p1__15249#)))))
                   (vals vmap)))}))
          nil)
        nil)
      (monitor/add-stat :IndexWriteBatchCount (java.lang.Integer/valueOf (int (count vmap))))
      (cluster/write-vals cs :index vmap)))
  (def INDEX_VERSION 2)
  (reset-meta! #'INDEX_VERSION (assoc {:const true, :column 1} :name 'INDEX_VERSION :ns *ns*))
  (defn init-index*
    ([cstore]
      (let [map__15255 cstore
            map__15255 (if (seq? map__15255)
                         (clojure.lang.PersistentHashMap/create (seq map__15255))
                         map__15255)
            tenant (get map__15255 :tenant)
            dbname (get map__15255 :db)
            db (db/bootstrap-db dbname)
            iset (.-memidx ^datomic.db.Db db)
            vec__15256 (repeatedly common/rand-uuid)
            rootid (nth vec__15256 (unchecked-int 0) nil)
            eavtid (nth vec__15256 (unchecked-int 1) nil)
            avetid (nth vec__15256 (unchecked-int 2) nil)
            aevtid (nth vec__15256 (unchecked-int 3) nil)
            raetid (nth vec__15256 (unchecked-int 4) nil)
            eavt_dirid (nth vec__15256 (unchecked-int 5) nil)
            avet_dirid (nth vec__15256 (unchecked-int 6) nil)
            aevt_dirid (nth vec__15256 (unchecked-int 7) nil)
            raet_dirid (nth vec__15256 (unchecked-int 8) nil)
            eavt_segid (nth vec__15256 (unchecked-int 9) nil)
            raet_segid (nth vec__15256 (unchecked-int 10) nil)
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
                      (fn fn__15265
                        ([p1__15253#]
                          (java.lang.Integer/valueOf
                            (int (.getA ^datomic.impl.db.IDatum p1__15253#)))))
                      avet))
            avet_root (datomic.index/fress
                        (datomic.index/root-node
                          (datomic.index/transpose [(^clojure.lang.IFn avet 0)])
                          (to-array [avet_dirid]))
                        datomic.index/common-write-handlers)
            vec__15259 (reduce
                         (fn fn__15268
                           ([p__15267 av]
                             (let [vec__15269 p__15267
                                   keys (nth vec__15269 (unchecked-int 0) nil)
                                   ids (nth vec__15269 (unchecked-int 1) nil)
                                   offs (nth vec__15269 (unchecked-int 2) nil)
                                   cnts (nth vec__15269 (unchecked-int 3) nil)
                                   bufs (nth vec__15269 (unchecked-int 4) nil)
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
            avet_keys (nth vec__15259 (unchecked-int 0) nil)
            avet_ids (nth vec__15259 (unchecked-int 1) nil)
            avet_offsets (nth vec__15259 (unchecked-int 2) nil)
            avet_counts (nth vec__15259 (unchecked-int 3) nil)
            bufmap (nth vec__15259 (unchecked-int 4) nil)
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
                      (fn fn__15273
                        ([p1__15254#]
                          (java.lang.Integer/valueOf
                            (int (.getA ^datomic.impl.db.IDatum p1__15254#)))))
                      aevt))
            aevt_root (datomic.index/fress
                        (datomic.index/root-node
                          (datomic.index/transpose [(^clojure.lang.IFn aevt 0)])
                          (to-array [aevt_dirid]))
                        datomic.index/common-write-handlers)
            vec__15262 (reduce
                         (fn fn__15276
                           ([p__15275 av]
                             (let [vec__15277 p__15275
                                   keys (nth vec__15277 (unchecked-int 0) nil)
                                   ids (nth vec__15277 (unchecked-int 1) nil)
                                   offs (nth vec__15277 (unchecked-int 2) nil)
                                   cnts (nth vec__15277 (unchecked-int 3) nil)
                                   bufs (nth vec__15277 (unchecked-int 4) nil)
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
            aevt_keys (nth vec__15262 (unchecked-int 0) nil)
            aevt_ids (nth vec__15262 (unchecked-int 1) nil)
            aevt_offsets (nth vec__15262 (unchecked-int 2) nil)
            aevt_counts (nth vec__15262 (unchecked-int 3) nil)
            bufmap (nth vec__15262 (unchecked-int 4) nil)
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
  (defn find-index-root-id
    ([cstore]
      (let [temp__5457__auto__ (:key
                                 (deref
                                   (cluster/get-ref
                                     cstore
                                     (datomic.index/index-ref-key-name cstore))))]
        (when temp__5457__auto__ (let [k temp__5457__auto__] (cluster/val-key->uuid k))))))
  (defn valid-version
    (^long [root_map]
      (.longValue
        (let [version (or (:version root_map) 1)]
          (when-not (contains? #{1 2} version)
            (error/state
              :db.error/index-version
              (str "This version of Datomic cannot read index version " version)))
          version))))
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
      (let [G__15287 version]
        (case
          G__15287
          1
          (let [G__15288 k]
            (case G__15288 :aevt-main :aevt :avet-main :avet :eavt-main :eavt :raet-main :raet))
          k))))
  (reset-meta!
    #'version-root-key
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'version {:tag 'long}) 'k]),
       :column 1}
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
              {:event :index/load-index, :olookup olookup, :index-root-id index_root_id}))
          nil)
        nil)
      (let [temp__5457__auto__ (common/getx olookup index_root_id)]
        (when temp__5457__auto__
          (let [root_map temp__5457__auto__
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
  (def SEG_BYTES_TARGET 16000)
  (reset-meta!
    #'SEG_BYTES_TARGET
    (assoc {:private true, :const true, :column 1} :name 'SEG_BYTES_TARGET :ns *ns*))
  (def SEGS_PER_DIR 3000)
  (reset-meta!
    #'SEGS_PER_DIR
    (assoc {:private true, :const true, :column 1} :name 'SEGS_PER_DIR :ns *ns*))
  (defn with-log
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
                          (clojure.core/list 'ret__15302__auto__)
                          (clojure.core/list (seq (concat (clojure.core/list 'do) body)))))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'datomic.common/log-and-print)
                        (clojure.core/list
                          (apply
                            hash-map
                            (seq (concat (clojure.core/list :out) (clojure.core/list name))))))))
                  (clojure.core/list 'ret__15302__auto__))))))
        (seq (concat (clojure.core/list 'do) body)))))
  (.setMacro #'datomic.index/with-log)
  (defn floop
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
                    (clojure.core/list 'f__15304__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'clojure.core/fn)
                          (clojure.core/list (apply vector (seq (concat params))))
                          body)))))))
            (clojure.core/list (seq (concat (clojure.core/list 'f__15304__auto__) args))))))))
  (.setMacro #'datomic.index/floop)
  (defn create-pace-calculator
    ([target_work window_msec]
      (let [start (java.lang.System/currentTimeMillis)
            state (atom {:window 0, :work 0, :msec 0})
            next_state (fn next_state
                         ([p__15306 nwork]
                           (let [map__15308 p__15306
                                 map__15308 (if (seq? map__15308)
                                              (clojure.lang.PersistentHashMap/create
                                                (seq map__15308))
                                              map__15308)
                                 window (get map__15308 :window)
                                 work (get map__15308 :work)
                                 now (java.lang.System/currentTimeMillis)
                                 elapsed (- now start)
                                 nwind (quot elapsed window_msec)
                                 msec (rem elapsed window_msec)
                                 nwork (+ nwork (if (= window nwind) work 0))]
                             {:window nwind, :work nwork, :msec (- window_msec msec)})))]
        (fn fn__15310
          ([nwork]
            (let [map__15311 (swap! state next_state nwork)
                  map__15311 (if (seq? map__15311)
                               (clojure.lang.PersistentHashMap/create (seq map__15311))
                               map__15311)
                  nstate map__15311
                  work (get map__15311 :work)
                  msec (get map__15311 :msec)
                  remaining_msec (- window_msec msec)]
              (when (>= work target_work) msec)))))))
  (def ^{:dynamic true} *pace-index-fn* nil)
  (reset-meta!
    #'*pace-index-fn*
    (assoc {:dynamic true, :column 1} :name '*pace-index-fn* :ns *ns*))
  (defn build-one-seg
    ([data cnt write_handlers]
      (let [temp__5457__auto__ datomic.index/*pace-index-fn*]
        (when temp__5457__auto__ (let [f temp__5457__auto__] (^clojure.lang.IFn f 1))))
      (let [bos (org.fressian.impl.BytesOutputStream.)]
        (try
          (let [gz (java.util.zip.GZIPOutputStream. ^java.io.OutputStream bos)]
            (try
              (let [bs (java.io.BufferedOutputStream. ^java.io.OutputStream gz)]
                (try
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
                        vec__15314 (let [f__15304__auto__ (fn f__15304__auto__
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
                                                                    16000))
                                                                (if
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
                                                                  (recur data cnt i true prev)
                                                                  (let 
                                                                    [f__15304__auto__
                                                                     (fn 
                                                                       f__15304__auto__
                                                                       ([data cnt i]
                                                                         (if
                                                                           (and
                                                                             data
                                                                             (let 
                                                                               [d (first data)]
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
                                                                    (^clojure.lang.IFn f__15304__auto__
                                                                      data
                                                                      cnt
                                                                      i)))
                                                                :else
                                                                (do
                                                                  (let 
                                                                    [d (first data)]
                                                                    (^clojure.lang.IFn proc d)
                                                                    (recur
                                                                      (next data)
                                                                      (dec cnt)
                                                                      (inc i)
                                                                      over
                                                                      d))))))]
                                     (^clojure.lang.IFn f__15304__auto__ data cnt 0 nil nil))
                        data (nth vec__15314 (unchecked-int 0) nil)
                        cnt (nth vec__15314 (unchecked-int 1) nil)
                        written (nth vec__15314 (unchecked-int 2) nil)]
                    (fressian/end-list w)
                    (let [dvec (vec (take written data_start)) d (datomic.index/transpose dvec)]
                      (.writeObject
                        ^org.fressian.Writer w
                        (.getEs ^datomic.index.TransposedData d))
                      (.writeObject
                        ^org.fressian.Writer w
                        (.getAs ^datomic.index.TransposedData d))
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
                                 (java.lang.Integer/valueOf
                                   (int (.remaining ^java.nio.Buffer buf))),
                                 :bpd (quot (.remaining ^java.nio.Buffer buf) written)}))
                            nil)
                          nil)
                        [buf data written (nth dvec (unchecked-int (dec (count dvec)))) d])))
                  (finally (do (.close ^java.io.FilterOutputStream bs) nil))))
              (finally (do (.close ^java.util.zip.DeflaterOutputStream gz) nil))))
          (finally (do (.close ^java.io.ByteArrayOutputStream bos) nil))))))
  (reset-meta!
    #'build-one-seg
    (assoc
      {:private true, :arglists (clojure.core/list ['data 'cnt 'write-handlers]), :column 1}
      :name
      'build-one-seg
      :ns
      *ns*))
  (defn bounded-count
    ([n coll] (loop [i 0 s (seq coll)] (if (and s (< i n)) (recur (inc i) (next s)) (long i)))))
  (defn build-psegs
    ([cstore olookup data es write_handlers segs_written_ref]
      (let [bound (* 2 16000)
            vec__15333 (let [f__15304__auto__ (fn f__15304__auto__
                                                ([vmap data es c]
                                                  (if data
                                                    (let [d (first data)
                                                          segid (common/rand-uuid)
                                                          vec__15337 (datomic.index/build-one-seg
                                                                       data
                                                                       (datomic.index/bounded-count
                                                                         (long bound)
                                                                         data)
                                                                       write_handlers)
                                                          buf (nth
                                                                vec__15337
                                                                (unchecked-int 0)
                                                                nil)
                                                          data (nth
                                                                 vec__15337
                                                                 (unchecked-int 1)
                                                                 nil)
                                                          written (nth
                                                                    vec__15337
                                                                    (unchecked-int 2)
                                                                    nil)
                                                          last_d (nth
                                                                   vec__15337
                                                                   (unchecked-int 3)
                                                                   nil)
                                                          td (nth vec__15337 (unchecked-int 4) nil)
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
                         (^clojure.lang.IFn f__15304__auto__ {} (seq data) es 0))
            vmap (nth vec__15333 (unchecked-int 0) nil)
            es (nth vec__15333 (unchecked-int 1) nil)
            c (nth vec__15333 (unchecked-int 2) nil)]
        (datomic.index/write-vals cstore vmap)
        (swap! segs_written_ref + c)
        es)))
  (reset-meta!
    #'build-psegs
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['cstore 'olookup 'data 'es 'write-handlers 'segs-written-ref]),
       :column 1}
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
  (defn fully-take-while-delivering-tail
    ([p pred coll]
      (lazy-seq
        (let [temp__5455__auto__ (seq coll)]
          (if temp__5455__auto__
            (let [s temp__5455__auto__ fst (first s) rst (rest s)]
              (if (^clojure.lang.IFn pred fst)
                (cons fst (datomic.index/fully-take-while-delivering-tail p pred rst))
                (do (deliver p s) nil)))
            (do (deliver p nil) nil))))))
  (defn fully-partition-by
    ([f coll]
      (letfn
        [(fpb
           [f pcoll]
           (lazy-seq
             (let [coll (deref pcoll) temp__5457__auto__ (seq coll)]
               (when temp__5457__auto__
                 (let [s temp__5457__auto__
                       fst (first s)
                       fv (^clojure.lang.IFn f fst)
                       pcoll (promise)]
                   (cons
                     (datomic.index/fully-take-while-delivering-tail
                       pcoll
                       (fn fn__15367 ([p1__15364#] (= fv (^clojure.lang.IFn f p1__15364#))))
                       s)
                     (^clojure.lang.IFn fpb f pcoll)))))))]
        (^clojure.lang.IFn fpb f (atom coll)))))
  (defn fred
    ([f ret coll]
      (let [temp__5455__auto__ (seq coll)]
        (if temp__5455__auto__
          (let [xs temp__5455__auto__ x (first xs) rst (rest xs)]
            (recur f (^clojure.lang.IFn f ret x) rst))
          ret))))
  (defn build-segs
    ([db cstore olookup data es retractions partfn write_handlers segs_written_ref]
      (let [retref (atom retractions)
            data (datomic.index/filter-nohist-pairs db data)
            data (if retractions (datomic.index/separating-retractions db retref data) data)
            pdata (datomic.index/fully-partition-by partfn data)
            es (datomic.index/fred
                 (fn fn__15375
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
       :column 1}
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
       :column 1}
      :name
      'merge-data
      :ns
      *ns*))
  (def DIR_PARTITION_SIZE {:eavt 2400, :aevt 2400, :avet 1000, :raet 2400})
  (reset-meta!
    #'DIR_PARTITION_SIZE
    (assoc {:private true, :column 1} :name 'DIR_PARTITION_SIZE :ns *ns*))
  (def MEM_TO_STG_RATIO 0.07)
  (reset-meta!
    #'MEM_TO_STG_RATIO
    (assoc {:private true, :column 1} :name 'MEM_TO_STG_RATIO :ns *ns*))
  (def BYTES_PER_SEG 45000)
  (reset-meta! #'BYTES_PER_SEG (assoc {:private true, :column 1} :name 'BYTES_PER_SEG :ns *ns*))
  (def MID_INDEX_THRESHOLD_FACTOR 10)
  (reset-meta!
    #'MID_INDEX_THRESHOLD_FACTOR
    (assoc {:private true, :column 1} :name 'MID_INDEX_THRESHOLD_FACTOR :ns *ns*))
  (def MIN_MAIN_INDEX_THRESHOLD 20000000)
  (reset-meta!
    #'MIN_MAIN_INDEX_THRESHOLD
    (assoc {:private true, :column 1} :name 'MIN_MAIN_INDEX_THRESHOLD :ns *ns*))
  (def SLICE_STEP 1)
  (reset-meta! #'SLICE_STEP (assoc {:private true, :column 1} :name 'SLICE_STEP :ns *ns*))
  (defn dir-partition-size
    ([idx]
      (let [scale (config/property "datomic.indexDirScale")]
        (* scale (^clojure.lang.IFn idx datomic.index/DIR_PARTITION_SIZE)))))
  (reset-meta!
    #'dir-partition-size
    (assoc
      {:private true, :arglists (clojure.core/list ['idx]), :column 1}
      :name
      'dir-partition-size
      :ns
      *ns*))
  (defn make-sparse-lt
    ([cmp]
      (fn fn__15384
        ([k1 k2]
          (when-not (and
                      (= (long (.getE ^datomic.db.Datum k1)) (long (.getE ^datomic.db.Datum k2)))
                      (= (long (.getA ^datomic.db.Datum k1)) (long (.getA ^datomic.db.Datum k2)))
                      (zero?
                        (common/compare
                          (.getV ^datomic.db.Datum k1)
                          (.getV ^datomic.db.Datum k2))))
            (neg? (.compare ^java.util.Comparator cmp k1 k2)))))))
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
  (declare datomic.index/mindiff)
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
  (defn mindiff
    ([minv maxv]
      (cond
        (and (string? minv) (string? maxv)) (datomic.index/strdiff minv maxv)
        (and (vector? minv) (vector? maxv)) (datomic.index/vecdiff minv maxv)
        :default (do maxv))))
  (defn sparse-datom
    ([idx prior d]
      (let [maked (if (.isAssertion ^datomic.impl.db.IDatum d)
                    db/asserting-datum
                    db/retracting-datum)
            G__15395 idx]
        (case
          G__15395
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
  (defn seg-last-datom
    ([olookup segid]
      (let [seg_data (cache/getx-uncached olookup segid)]
        (nth seg_data (unchecked-int (dec (count seg_data)))))))
  (defn sparse-e-xf
    ([olookup idx]
      (let [vprev (volatile! nil)]
        (fn fn__15398
          ([rf]
            (fn fn__15399
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
  (defn sparse-es-2 ([olookup idx es] (into [] (datomic.index/sparse-e-xf olookup idx) es)))
  (defn sparse-es
    ([olookup idx es]
      (mapv
        (fn fn__15407
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
                    (let [G__15408 idx]
                      (case
                        G__15408
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
  (def index-parallelism (atom 1 :validator (fn fn__15415 ([p1__15414#] (<= 1 p1__15414#)))))
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
  (defn write-dirs
    ([cstore olookup ch]
      (loop [rootnode []]
        (let [temp__5455__auto__ (a/<!! ch)]
          (if temp__5455__auto__
            (let [vec__15418 temp__5455__auto__
                  key (nth vec__15418 (unchecked-int 0) nil)
                  dbuf (nth vec__15418 (unchecked-int 1) nil)
                  dirid (common/rand-uuid)]
              (datomic.index/write-vals cstore {dirid dbuf})
              (recur (conj rootnode [key dirid])))
            rootnode)))))
  (defn conjable-on-channel
    ([ch]
      (reify
        clojure.lang.IPersistentCollection
        (^clojure.lang.IPersistentCollection cons
          [this o]
          (if (a/>!! ch o)
            this
            (do (throw (java.lang.IllegalStateException. "Channel closed")) nil)))
        (^boolean equiv [this o] (identical? this o)))))
  (defn capture-last-ex
    ([]
      (let [last_ex (atom nil) ex_handler (fn ex_handler ([t] (reset! last_ex t) nil))]
        [ex_handler last_ex])))
  (defn start-dirs-pipeline
    ([& p__15429]
      (let [map__15430 p__15429
            map__15430 (if (seq? map__15430)
                         (clojure.lang.PersistentHashMap/create (seq map__15430))
                         map__15430)
            cstore (get map__15430 :cstore)
            olookup (get map__15430 :olookup)
            idx (get map__15430 :idx)
            dirnode_size (get map__15430 :dirnode-size)
            dirs_ahead (get map__15430 :dirs-ahead)
            serialize_par (get map__15430 :serialize-par)
            vec__15431 (datomic.index/capture-last-ex)
            ex_handler (nth vec__15431 (unchecked-int 0) nil)
            last_ex (nth vec__15431 (unchecked-int 1) nil)
            serialized_dirs_ch (a/chan)
            dir_entries_ch (a/chan
                             dirs_ahead
                             (comp
                               (datomic.index/sparse-e-xf olookup idx)
                               (partition-all (unchecked-long ^java.lang.Number dirnode_size)))
                             ex_handler)
            es (datomic.index/conjable-on-channel dir_entries_ch)
            fut (future-call
                  (fn fn__15434
                    ([]
                      (let [result (datomic.index/write-dirs cstore olookup serialized_dirs_ch)]
                        (let [temp__5457__auto__ (deref last_ex)]
                          (when temp__5457__auto__
                            (let [ex temp__5457__auto__] (throw ^java.lang.Throwable ex))))
                        result))))]
        (add-watch
          last_ex
          :fail-fast
          (fn fn__15437 ([& _] (a/close! dir_entries_ch) (a/close! serialized_dirs_ch))))
        (a/pipeline
          serialize_par
          serialized_dirs_ch
          (map datomic.index/serialize-dir)
          dir_entries_ch
          true
          ex_handler)
        [es dir_entries_ch fut])))
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
      xcmp
      segs_written_ref
      dirs_written_ref]
      'merge-one-index
      (try
        (let [pario (config/property "datomic.indexIOParallelism")
              cstore (if pario
                       (cluster/queueing-writer
                         cstore
                         pario
                         cluster/BOUNDING_TIMEOUT_MSEC
                         (fn fn__15452
                           ([p1__15440#] (monitor/add-stat :IndexIOQueueCount p1__15440#))))
                       cstore)
              lt (datomic.index/make-sparse-lt cmp)
              old_root (when old_root_id (common/getx olookup old_root_id))
              vec__15446 (datomic.index/start-dirs-pipeline
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
                           :serialize-par
                           (deref datomic.index/index-parallelism))
              es (nth vec__15446 (unchecked-int 0) nil)
              des_ch (nth vec__15446 (unchecked-int 1) nil)
              rootnode_fut (nth vec__15446 (unchecked-int 2) nil)
              retractions (when filter_retractions? [])
              xsegs (when old_root_id
                      (let [index (datomic.index/lookup-index olookup xcmp old_root_id)]
                        (reduce
                          (fn fn__15454
                            ([xsegs p]
                              (into
                                xsegs
                                (filter
                                  identity
                                  (map
                                    (fn fn__15455
                                      ([p1__15441#] (datomic.index/seek-seg index p1__15441#)))
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
                         :idx idx}))
                    nil)
                  nil)
              _ (monitor/add-stat :ExciseSegments (java.lang.Integer/valueOf (int (count xsegs))))
              excise? (fn excise_QMARK_
                        ([d] (some (fn fn__15459 ([p1__15442#] (x/remove? p1__15442# d))) xpreds)))
              garbage_ids (into garbage_ids xsegs)
              dirs (when old_root
                     (map
                       (fn fn__15462 ([p1__15443#] (cache/getx-uncached olookup p1__15443#)))
                       (.-dirids ^datomic.index.RootNode old_root)))
              mkdes (fn mkdes
                      ([dir]
                        (reduce
                          (fn fn__15465
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
                           (let [vec__15469 (deref (peek erq))
                                 nes (nth vec__15469 (unchecked-int 0) nil)
                                 nrs (nth vec__15469 (unchecked-int 1) nil)]
                             (recur (into es nes) (into rs nrs) (pop erq))))))
              vec__15449 (let [f__15304__auto__ (fn f__15304__auto__
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
                                                                                   :nil-data}))
                                                                              nil)
                                                                            nil)
                                                                          (let 
                                                                            [vec__15474
                                                                             (^clojure.lang.IFn drainq
                                                                               es
                                                                               retractions
                                                                               erq)
                                                                             es
                                                                             (nth
                                                                               vec__15474
                                                                               (unchecked-int 0)
                                                                               nil)
                                                                             retractions
                                                                             (nth
                                                                               vec__15474
                                                                               (unchecked-int 1)
                                                                               nil)
                                                                             erq
                                                                             (nth
                                                                               vec__15474
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
                                                                                  :nil-des}))
                                                                             nil)
                                                                           nil)
                                                                         (let 
                                                                           [vec__15477
                                                                            (^clojure.lang.IFn drainq
                                                                              es
                                                                              retractions
                                                                              erq)
                                                                            es
                                                                            (nth
                                                                              vec__15477
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            retractions
                                                                            (nth
                                                                              vec__15477
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            erq
                                                                            (nth
                                                                              vec__15477
                                                                              (unchecked-int 2)
                                                                              nil)
                                                                            vec__15480
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
                                                                              vec__15480
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            retractions
                                                                            (nth
                                                                              vec__15480
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
                                                                           :stage :skip}))
                                                                      nil)
                                                                    nil)
                                                                  (let 
                                                                    [d (first data)
                                                                     vec__15483
                                                                     (^clojure.lang.IFn drainq
                                                                       es
                                                                       retractions
                                                                       erq)
                                                                     es
                                                                     (nth
                                                                       vec__15483
                                                                       (unchecked-int 0)
                                                                       nil)
                                                                     retractions
                                                                     (nth
                                                                       vec__15483
                                                                       (unchecked-int 1)
                                                                       nil)
                                                                     erq
                                                                     (nth
                                                                       vec__15483
                                                                       (unchecked-int 2)
                                                                       nil)
                                                                     vec__15486
                                                                     (let 
                                                                       [f__15304__auto__
                                                                        (fn 
                                                                          f__15304__auto__
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
                                                                       (^clojure.lang.IFn f__15304__auto__
                                                                         es
                                                                         des))
                                                                     es
                                                                     (nth
                                                                       vec__15486
                                                                       (unchecked-int 0)
                                                                       nil)
                                                                     des
                                                                     (nth
                                                                       vec__15486
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
                                                                           :stage :merge}))
                                                                      nil)
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
                                                                             fn__15495
                                                                             ([p1__15445#]
                                                                               (^clojure.lang.IFn lt
                                                                                 p1__15445#
                                                                                 (:key
                                                                                   (fnext des)))))
                                                                           data)
                                                                         data))
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
                                                                     segid
                                                                     (and des (:segid (first des)))
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
                                                                              :stage :build}))
                                                                         nil)
                                                                       nil)
                                                                     vec__15492
                                                                     (if
                                                                       (<=
                                                                         (deref
                                                                           datomic.index/index-parallelism)
                                                                         (count erq))
                                                                       (let 
                                                                         [vec__15497
                                                                          (deref (peek erq))
                                                                          nes
                                                                          (nth
                                                                            vec__15497
                                                                            (unchecked-int 0)
                                                                            nil)
                                                                          nrs
                                                                          (nth
                                                                            vec__15497
                                                                            (unchecked-int 1)
                                                                            nil)]
                                                                         [(into es nes)
                                                                          (into retractions nrs)
                                                                          (pop erq)])
                                                                       [es retractions erq])
                                                                     es
                                                                     (nth
                                                                       vec__15492
                                                                       (unchecked-int 0)
                                                                       nil)
                                                                     retractions
                                                                     (nth
                                                                       vec__15492
                                                                       (unchecked-int 1)
                                                                       nil)
                                                                     erq
                                                                     (nth
                                                                       vec__15492
                                                                       (unchecked-int 2)
                                                                       nil)
                                                                     erq
                                                                     (conj
                                                                       erq
                                                                       (future-call
                                                                         (fn 
                                                                           fn__15500
                                                                           ([]
                                                                             (try
                                                                               (let 
                                                                                 [seg_data
                                                                                  (and
                                                                                    segid
                                                                                    (seq
                                                                                      (cache/getx-uncached
                                                                                        olookup
                                                                                        segid)))
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
                           (^clojure.lang.IFn f__15304__auto__
                             es
                             garbage_ids
                             retractions
                             (seq (apply concat (pmap mkdes dirs)))
                             (seq
                               (filter
                                 (fn fn__15507
                                   ([p1__15444#]
                                     (< (.getT ^datomic.impl.db.IDatum p1__15444#) as_of_t)))
                                 (remove excise? data)))
                             clojure.lang.PersistentQueue/EMPTY))
              es (nth vec__15449 (unchecked-int 0) nil)
              garbage (nth vec__15449 (unchecked-int 1) nil)
              retractions (nth vec__15449 (unchecked-int 2) nil)
              _ (a/close! des_ch)
              rootnode (deref rootnode_fut)
              _ (swap! dirs_written_ref + (java.lang.Integer/valueOf (int (count rootnode))))
              mkroot (fn mkroot
                       ([rootnode_data]
                         (let [keydata (datomic.index/transpose (mapv first rootnode_data))
                               dirids (to-array (mapv second rootnode_data))]
                           (datomic.index/root-node keydata dirids))))
              rootid (common/rand-uuid)
              nroot (^clojure.lang.IFn mkroot rootnode)
              vmap {rootid (datomic.index/fress nroot write_handlers)}]
          (cache/put olookup rootid nroot)
          (datomic.index/write-vals cstore vmap)
          (when pario
            (common/bounded-deref (cluster/finish-writer cstore) cluster/BOUNDING_TIMEOUT_MSEC))
          (swap! segs_written_ref + (java.lang.Integer/valueOf (int (count vmap))))
          [rootid
           (concat
             garbage
             (when old_root
               (map cluster/uuid->val-key (.-dirids ^datomic.index.RootNode old_root)))
             (when old_root_id [(cluster/uuid->val-key old_root_id)]))
           retractions])
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
          'xcmp
          'segs-written-ref
          'dirs-written-ref]),
       :column 1}
      :name
      'merge-one-index
      :ns
      *ns*))
  (defn excise-ents
    ([db]
      (filter
        :db/excise
        (map
          (fn fn__15515 ([p1__15514#] (.entity ^datomic.Database db (:e p1__15514#))))
          (take-while
            (fn fn__15517 ([p1__15513#] (= (:a p1__15513#) 15)))
            (iter/iter-seq (btset/seek (:aevt (:indexing db)) (db/datum db :a 15))))))))
  (defn get-avet-sorted-datoms-mem
    ([db index attrids]
      (let [ret (java.util.ArrayList.)]
        (when index
          (loop [seq_15523 (seq attrids) chunk_15524 nil count_15525 0 i_15526 0]
            (if (< i_15526 count_15525)
              (let [attrid (.nth ^clojure.lang.Indexed chunk_15524 (unchecked-int i_15526))]
                (reduce
                  (fn fn__15527
                    ([p1__15521# p2__15520#] (.add ^java.util.ArrayList ret p2__15520#)))
                  nil
                  (iter/iter-seq
                    (iter/take-while
                      (fn fn__15529
                        ([p1__15522#]
                          (= attrid (long (.getA ^datomic.impl.db.IDatum p1__15522#)))))
                      (btset/seek index (db/datum db :a attrid)))))
                (recur seq_15523 chunk_15524 count_15525 (inc i_15526)))
              (let [temp__5457__auto__ (seq seq_15523)]
                (when temp__5457__auto__
                  (let [seq_15523 temp__5457__auto__]
                    (if (chunked-seq? seq_15523)
                      (let [c__5719__auto__ (chunk-first seq_15523)]
                        (recur (chunk-rest seq_15523) c__5719__auto__ (count c__5719__auto__) 0))
                      (let [attrid (first seq_15523)]
                        (reduce
                          (fn fn__15531
                            ([p1__15521# p2__15520#] (.add ^java.util.ArrayList ret p2__15520#)))
                          nil
                          (iter/iter-seq
                            (iter/take-while
                              (fn fn__15533
                                ([p1__15522#]
                                  (= attrid (long (.getA ^datomic.impl.db.IDatum p1__15522#)))))
                              (btset/seek index (db/datum db :a attrid)))))
                        (recur (next seq_15523) nil 0 0)))))))))
        (java.util.Collections/sort ^java.util.List ret db/avet-cmp)
        ret)))
  (reset-meta!
    #'get-avet-sorted-datoms-mem
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'index 'attrids]), :column 1}
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
           (fn fn__15538
             ([m]
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                 (when (.isInfoEnabled ^org.slf4j.Logger logger)
                   (.info
                     ^org.slf4j.Logger logger
                     (logger/process (assoc m :event :index/external-sort)))
                   nil)
                 nil))),
           :max-chunk-size (long (* (* 10 1000) 1000))}
          f))))
  (reset-meta!
    #'avet-sort-and-process-datoms
    (assoc
      {:private true, :arglists (clojure.core/list ['datoms 'f]), :column 1}
      :name
      'avet-sort-and-process-datoms
      :ns
      *ns*))
  (defn attr-datoms
    ([db index attrid]
      (iter/take-while
        (fn fn__15542 ([p1__15541#] (= attrid (long (.getA ^datomic.impl.db.IDatum p1__15541#)))))
        (btset/seek index (db/datum db :a attrid)))))
  (defn aevt-attrs-datoms
    ([db attrids]
      (let [datoms (map
                     (fn fn__15545
                       ([attrid]
                         (let [main_aevt_datoms (datomic.index/attr-datoms
                                                  db
                                                  (.-aevt (.-index ^datomic.db.Db db))
                                                  attrid)
                               mid_aevt_datoms (let [temp__5457__auto__ (.-aevt
                                                                          (.-mid-index
                                                                            ^datomic.db.Db db))]
                                                 (when temp__5457__auto__
                                                   (let [mid_aevt temp__5457__auto__]
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
                               hist_aevt_datoms (let [temp__5457__auto__ (.-aevt
                                                                           (.-history
                                                                             ^datomic.db.Db db))]
                                                  (when temp__5457__auto__
                                                    (let [hist_aevt temp__5457__auto__]
                                                      (datomic.index/attr-datoms
                                                        db
                                                        hist_aevt
                                                        attrid))))]
                           [aevt_datoms hist_aevt_datoms])))
                     attrids)]
        [(iter/concat (filter identity (map first datoms)))
         (iter/concat (filter identity (map second datoms)))])))
  (defn idx-key
    ([& ks]
      (fn fn__15551
        ([m] (some (fn fn__15552 ([p1__15550#] (^clojure.lang.IFn p1__15550# m))) ks)))))
  (defn add-avet-indexes
    ([cstore olookup db as_of_t root_map attrids]
      (let [vec__15557 (datomic.index/aevt-attrs-datoms db attrids)
            mid_main_aevt_datoms (nth vec__15557 (unchecked-int 0) nil)
            hist_aevt_datoms (nth vec__15557 (unchecked-int 1) nil)
            sort_and_merge (fn sort_and_merge
                             ([k aevt_datoms garbage]
                               (let [m_15567 {:event :index/add-avet,
                                              :next-t as_of_t,
                                              :attributes attrids}
                                     ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                    "datomic.index")]
                                                       (when (.isInfoEnabled
                                                               ^org.slf4j.Logger logger)
                                                         (.info
                                                           ^org.slf4j.Logger logger
                                                           (logger/process
                                                             (assoc m_15567 :phase :begin)))
                                                         nil)
                                                       nil)
                                     start__8981__auto__ (java.lang.System/nanoTime)
                                     result__8982__auto__ (try
                                                            {:returned
                                                             (datomic.index/avet-sort-and-process-datoms
                                                               aevt_datoms
                                                               (fn 
                                                                 fn__15571
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
                                                                       fn__15572
                                                                       ([p1__15556#]
                                                                         (java.lang.Integer/valueOf
                                                                           (int
                                                                             (.getA
                                                                               ^datomic.impl.db.IDatum p1__15556#)))))
                                                                     db/avet-cmp
                                                                     datomic.index/common-write-handlers
                                                                     false
                                                                     as_of_t
                                                                     :avet
                                                                     nil
                                                                     nil
                                                                     datomic.index/avet-cmpi
                                                                     (atom 0)
                                                                     (atom 0)))))}
                                                            (catch
                                                              java.lang.Throwable
                                                              t__8983__auto__
                                                              {:threw t__8983__auto__}))
                                     elapsed_15568 (-
                                                     (java.lang.System/nanoTime)
                                                     start__8981__auto__)
                                     msec_15569 (logger/format-as-msec (long elapsed_15568))]
                                 (monitor/add-stat :AddIndexMsec msec_15569)
                                 (let [endmsg__8984__auto__ (merge
                                                              (assoc
                                                                m_15567
                                                                :msec
                                                                msec_15569
                                                                :phase
                                                                :end)
                                                              (when
                                                                (:threw result__8982__auto__)
                                                                {:threw
                                                                 (class
                                                                   (:threw
                                                                     result__8982__auto__))}))
                                       logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                     (.info
                                       ^org.slf4j.Logger logger
                                       (logger/process endmsg__8984__auto__))
                                     nil)
                                   nil)
                                 (if (contains? result__8982__auto__ :returned)
                                   (:returned result__8982__auto__)
                                   (do (throw (:threw result__8982__auto__)) nil)))))
            vec__15560 (^clojure.lang.IFn sort_and_merge
                         (datomic.index/idx-key :avet-main :avet)
                         mid_main_aevt_datoms
                         [])
            avetid (nth vec__15560 (unchecked-int 0) nil)
            garbage (nth vec__15560 (unchecked-int 1) nil)
            vec__15563 (^clojure.lang.IFn sort_and_merge :avet-hist hist_aevt_datoms garbage)
            hist_avetid (nth vec__15563 (unchecked-int 0) nil)
            garbage (nth vec__15563 (unchecked-int 1) nil)]
        [(assoc root_map :avet-main avetid :avet-hist hist_avetid) garbage])))
  (defn needs-new-avet
    ([db]
      (seq
        (map
          (fn fn__15582 ([a] (.id ^datomic.db.Attribute a)))
          (filter
            (fn fn__15584
              ([a]
                (and
                  (.-needsAVET ^datomic.db.Attribute a)
                  (not (.hasAVET ^datomic.db.Attribute a)))))
            (filter (partial instance? datomic.db.Attribute) (:elements db)))))))
  (defn dropped-avet-aids
    ([db]
      (let [hist (.history ^datomic.Database db)
            fadd (fn fadd
                   ([aids ds]
                     (reduce
                       (fn fn__15589
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
  (defn write-object
    ([store olookup v]
      (let [uuid (common/rand-uuid)]
        (datomic.index/write-vals
          store
          {uuid (datomic.index/fress v datomic.index/common-write-handlers)})
        (cache/put olookup uuid v)
        uuid)))
  (defn drop-dirnode-leaves
    ([dirnode drop?]
      (let [result (reduce
                     (fn fn__15594
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
  (defn drop-avet
    ([store olookup old_rootid aid old_garbage]
      (let [oldroot (common/getx olookup old_rootid)
            ct (count (.-keydata ^datomic.index.RootNode oldroot))
            map__15597 (reduce
                         (fn fn__15598
                           ([result n]
                             (let [temp__5457__auto__ datomic.index/*pace-index-fn*]
                               (when temp__5457__auto__
                                 (let [f temp__5457__auto__] (^clojure.lang.IFn f 1))))
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
                                   vec__15599 (when olddirid
                                                (datomic.index/drop-dirnode-leaves
                                                  olddir
                                                  (fn fn__15603
                                                    ([p__15602]
                                                      (let [map__15604 p__15602
                                                            map__15604
                                                            (if
                                                              (seq? map__15604)
                                                              (clojure.lang.PersistentHashMap/create
                                                                (seq map__15604))
                                                              map__15604)
                                                            a (get map__15604 :a)]
                                                        (= a aid))))))
                                   newdir (nth vec__15599 (unchecked-int 0) nil)
                                   leaf_garbage (nth vec__15599 (unchecked-int 1) nil)]
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
            map__15597 (if (seq? map__15597)
                         (clojure.lang.PersistentHashMap/create (seq map__15597))
                         map__15597)
            keydata (get map__15597 :keydata)
            dirids (get map__15597 :dirids)
            garbage (get map__15597 :garbage)]
        (if (seq garbage)
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:event :index/drop-avet-segments,
                     :aid aid,
                     :count (long (inc (count garbage)))}))
                nil)
              nil)
            [(datomic.index/write-object
               store
               olookup
               (datomic.index/root-node (datomic.index/transpose keydata) (to-array dirids)))
             (conj (into old_garbage garbage) old_rootid)])
          [old_rootid old_garbage]))))
  (defn drop-avets
    ([store olookup root_id attrids garbage]
      (reduce
        (fn fn__15614
          ([p__15613 aid]
            (let [vec__15615 p__15613
                  root_id (nth vec__15615 (unchecked-int 0) nil)
                  garbage (nth vec__15615 (unchecked-int 1) nil)]
              (datomic.index/drop-avet store olookup root_id aid garbage))))
        [root_id garbage]
        attrids)))
  (defn drop-avet-indexes
    ([store olookup old_root_ids attrids as_of_t garbage]
      (let [% (reduce
                (fn fn__15621
                  ([p__15620 root_id]
                    (let [vec__15622 p__15620
                          root_ids (nth vec__15622 (unchecked-int 0) nil)
                          garbage (nth vec__15622 (unchecked-int 1) nil)]
                      (if root_id
                        (let [m_15625 {:event :index/drop-avets,
                                       :root-id root_id,
                                       :next-t as_of_t,
                                       :attrs (java.lang.Integer/valueOf (int (count attrids)))}
                              ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.index")]
                                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                  (.info
                                                    ^org.slf4j.Logger logger
                                                    (logger/process (assoc m_15625 :phase :begin)))
                                                  nil)
                                                nil)
                              start__8981__auto__ (java.lang.System/nanoTime)
                              result__8982__auto__ (try
                                                     {:returned
                                                      (let [vec__15629
                                                            (datomic.index/drop-avets
                                                              store
                                                              olookup
                                                              root_id
                                                              attrids
                                                              garbage)
                                                            new_id
                                                            (nth vec__15629 (unchecked-int 0) nil)
                                                            garbage
                                                            (nth vec__15629 (unchecked-int 1) nil)]
                                                        [(conj root_ids new_id) garbage])}
                                                     (catch
                                                       java.lang.Throwable
                                                       t__8983__auto__
                                                       {:threw t__8983__auto__}))
                              elapsed_15626 (- (java.lang.System/nanoTime) start__8981__auto__)
                              msec_15627 (logger/format-as-msec (long elapsed_15626))]
                          (let [endmsg__8984__auto__ (merge
                                                       (assoc m_15625 :msec msec_15627 :phase :end)
                                                       (when (:threw result__8982__auto__)
                                                         {:threw
                                                          (class (:threw result__8982__auto__))}))
                                logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                            (when (.isInfoEnabled ^org.slf4j.Logger logger)
                              (.info
                                ^org.slf4j.Logger logger
                                (logger/process endmsg__8984__auto__))
                              nil)
                            nil)
                          (if (contains? result__8982__auto__ :returned)
                            (:returned result__8982__auto__)
                            (do (throw (:threw result__8982__auto__)) nil)))
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
  (defn mem-index-bytes
    ([index]
      (if index
        (java.lang.Float/valueOf
          (unchecked-float (* (mem/memory-size index) datomic.index/MEM_TO_STG_RATIO)))
        0.0)))
  (defn stg-index-segs
    ([olookup index part_size]
      (or
        (when index
          (let [root (.-root ^datomic.index.Index index)
                n_dirs (alength (.-dirids ^datomic.index.RootNode root))]
            (when-not (= n_dirs 0)
              (let [last_idx (dec n_dirs)
                    last_dir (datomic.index/get-dir-node root (long last_idx) olookup true)
                    last_dir_n_segs (alength (.-segids ^datomic.index.DirNode last_dir))]
                (+ (* part_size last_idx) last_dir_n_segs)))))
        0)))
  (defn stg-index-size
    ([olookup index part_size]
      (let [seg_count (datomic.index/stg-index-segs olookup index part_size)]
        [(java.lang.Float/valueOf (unchecked-float (* seg_count datomic.index/BYTES_PER_SEG)))
         seg_count])))
  (defn estimate-seg-offset
    ([olookup index dir_partition_size k]
      (let [map__15643 (first
                         (some-> (.seek ^datomic.index.Index index k) (datomic.index/dir-seq)))
            map__15643 (if (seq? map__15643)
                         (clojure.lang.PersistentHashMap/create (seq map__15643))
                         map__15643)
            key (get map__15643 :key)
            ri (get
                 map__15643
                 :ri
                 (let [root (.-root ^datomic.index.Index index)]
                   (java.lang.Integer/valueOf
                     (int (count (.-dirids ^datomic.index.RootNode root))))))
            di (get map__15643 :di 0)]
        {:key k, :index-key key, :offset (+ (* ri dir_partition_size) di)})))
  (defn least-pop-slice
    ([olookup ks index dir_partition_size slice_size]
      (let [offsets (map
                      (fn fn__15647
                        ([p1__15646#]
                          (datomic.index/estimate-seg-offset
                            olookup
                            index
                            dir_partition_size
                            p1__15646#)))
                      ks)
            get_slice (fn get_slice
                        ([lo hi] [(:key lo) (- (:offset hi) (:offset lo)) (:offset lo)]))
            pops (map get_slice offsets (drop slice_size offsets))]
        (if (seq pops)
          (apply min-key second pops)
          (^clojure.lang.IFn get_slice (first offsets) (last offsets))))))
  (defn retract-assert-pair?
    ([d1 d2]
      (and
        (= (long (.getE ^datomic.db.Datum d1)) (long (.getE ^datomic.db.Datum d2)))
        (= (long (.getA ^datomic.db.Datum d1)) (long (.getA ^datomic.db.Datum d2)))
        (zero? (common/compare (.getV ^datomic.db.Datum d1) (.getV ^datomic.db.Datum d2)))
        (false? (.isAssertion ^datomic.db.Datum d1))
        (true? (.isAssertion ^datomic.db.Datum d2)))))
  (defn paired-assertion-state
    ([sd1 p__15657]
      (let [vec__15658 p__15657
            sd2 (nth vec__15658 (unchecked-int 0) nil)
            more vec__15658
            vec__15661 (let [G__15667 (cons sd1 more)
                             vec__15668 G__15667
                             sd1 (nth vec__15668 (unchecked-int 0) nil)
                             sd2 (nth vec__15668 (unchecked-int 1) nil)
                             more vec__15668]
                         (loop [G__15667 G__15667]
                           (let [vec__15672 G__15667
                                 sd1 (nth vec__15672 (unchecked-int 0) nil)
                                 sd2 (nth vec__15672 (unchecked-int 1) nil)
                                 more vec__15672]
                             (if (= (:item sd1) (:item sd2)) (recur (next more)) more))))
            sd1 (nth vec__15661 (unchecked-int 0) nil)
            sd2 (nth vec__15661 (unchecked-int 1) nil)
            more vec__15661]
        (cond
          (nil? sd2) :absent
          (datomic.index/retract-assert-pair? (:item sd1) (:item sd2)) (if
                                                                         (not=
                                                                           (:segid sd1)
                                                                           (:segid sd2))
                                                                         :segmented
                                                                         :present)
          :else (do
                  (let [map__15676 (:item sd1)
                        map__15676 (if (seq? map__15676)
                                     (clojure.lang.PersistentHashMap/create (seq map__15676))
                                     map__15676)
                        e (get map__15676 :e)
                        a (get map__15676 :a)
                        v (get map__15676 :v)
                        lookahead (seq
                                    (take-while
                                      (fn fn__15678
                                        ([p__15677]
                                          (let [map__15679 p__15677
                                                map__15679 (if
                                                             (seq? map__15679)
                                                             (clojure.lang.PersistentHashMap/create
                                                               (seq map__15679))
                                                             map__15679)
                                                item (get map__15679 :item)]
                                            (and
                                              (= (:e item) e)
                                              (= (:a item) a)
                                              (zero? (common/compare (:v item) v))))))
                                      more))]
                    (if (some
                          (fn fn__15683
                            ([sdn] (datomic.index/retract-assert-pair? (:item sd1) (:item sdn))))
                          lookahead)
                      :separated
                      :absent)))))))
  (defn problem-assertion-state?
    ([tier state]
      (let [G__15686 tier]
        (case
          G__15686
          :mid-index
          (contains? #{:segmented :separated} state)
          :history
          (contains? #{:absent :segmented :separated} state)))))
  (reset-meta!
    #'problem-assertion-state?
    (assoc
      {:private true, :arglists (clojure.core/list ['tier 'state]), :column 1}
      :name
      'problem-assertion-state?
      :ns
      *ns*))
  (defn disjoined-datoms
    ([db tier sort]
      (let [temp__5457__auto__ (some-> db (^clojure.lang.IFn tier) (^clojure.lang.IFn sort))]
        (when temp__5457__auto__
          (let [idx temp__5457__auto__
                G__15692 (some-> idx (.seek) (datomic.index/seg+item-seq))
                vec__15693 G__15692
                seq__15694 (seq vec__15693)
                first__15695 (first seq__15694)
                seq__15694 (next seq__15694)
                sd1 first__15695
                more seq__15694
                result (if (= tier :history)
                         {:segmented [], :separated [], :absent []}
                         {:segmented [], :separated []})]
            (loop [G__15692 G__15692 result result]
              (let [vec__15697 G__15692
                    seq__15698 (seq vec__15697)
                    first__15699 (first seq__15698)
                    seq__15698 (next seq__15698)
                    sd1 first__15699
                    more seq__15698
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
                          result))))))))
          nil))))
  (defn repair-disjoined
    ([db olookup root_map]
      (if (< (or (^clojure.lang.IFn root_map :buildRevision) 0) 4846)
        (let [m_15704 {:event :index/repair-disjoined-db, :root-map root_map}
              ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_15704 :phase :begin)))
                                  nil)
                                nil)
              start__8981__auto__ (java.lang.System/nanoTime)
              result__8982__auto__ (try
                                     {:returned
                                      (reduce
                                        (fn fn__15708
                                          ([db idxsort]
                                            (let [m_15709 {:event :index/repair-disjoined-index,
                                                           :index idxsort}
                                                  ___8980__auto__ (let 
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
                                                                            m_15709
                                                                            :phase
                                                                            :begin)))
                                                                      nil)
                                                                    nil)
                                                  start__8981__auto__ (java.lang.System/nanoTime)
                                                  result__8982__auto__ (try
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
                                                                                   ([p1__15702#]
                                                                                     (reduce
                                                                                       (fn 
                                                                                         fn__15715
                                                                                         ([m
                                                                                           p__15714]
                                                                                           (let 
                                                                                             [vec__15716
                                                                                              p__15714
                                                                                              k
                                                                                              (nth
                                                                                                vec__15716
                                                                                                (unchecked-int
                                                                                                  0)
                                                                                                nil)
                                                                                              v
                                                                                              (nth
                                                                                                vec__15716
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
                                                                                       p1__15702#)))]
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
                                                                                           hists)}))
                                                                                    nil)
                                                                                  nil)
                                                                                (update-in
                                                                                  db
                                                                                  [:indexing
                                                                                   idxsort]
                                                                                  (fn 
                                                                                    fn__15721
                                                                                    ([p1__15703#]
                                                                                      (into
                                                                                        p1__15703#
                                                                                        datoms)))))
                                                                              db))}
                                                                         (catch
                                                                           java.lang.Throwable
                                                                           t__8983__auto__
                                                                           {:threw
                                                                            t__8983__auto__}))
                                                  elapsed_15710 (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8981__auto__)
                                                  msec_15711 (logger/format-as-msec
                                                               (long elapsed_15710))]
                                              (let [endmsg__8984__auto__ (merge
                                                                           (assoc
                                                                             m_15709
                                                                             :msec
                                                                             msec_15711
                                                                             :phase
                                                                             :end)
                                                                           (when
                                                                             (:threw
                                                                               result__8982__auto__)
                                                                             {:threw
                                                                              (class
                                                                                (:threw
                                                                                  result__8982__auto__))}))
                                                    logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.index")]
                                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                  (.info
                                                    ^org.slf4j.Logger logger
                                                    (logger/process endmsg__8984__auto__))
                                                  nil)
                                                nil)
                                              (if (contains? result__8982__auto__ :returned)
                                                (:returned result__8982__auto__)
                                                (do (throw (:threw result__8982__auto__)) nil)))))
                                        db
                                        [:eavt :aevt :avet :raet])}
                                     (catch
                                       java.lang.Throwable
                                       t__8983__auto__
                                       {:threw t__8983__auto__}))
              elapsed_15705 (- (java.lang.System/nanoTime) start__8981__auto__)
              msec_15706 (logger/format-as-msec (long elapsed_15705))]
          (let [endmsg__8984__auto__ (merge
                                       (assoc m_15704 :msec msec_15706 :phase :end)
                                       (when (:threw result__8982__auto__)
                                         {:threw (class (:threw result__8982__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
              nil)
            nil)
          (if (contains? result__8982__auto__ :returned)
            (:returned result__8982__auto__)
            (do (throw (:threw result__8982__auto__)) nil)))
        db)))
  (defn merge-db*
    ([cstore olookup db as_of_t old_root_id extra fulltext_enabled? excise_enabled?]
      (let [job_started (java.lang.System/nanoTime)
            segs_written_ref (atom 0)
            dirs_written_ref (atom 0)
            root_map (common/getx olookup old_root_id)
            vec__15741 (let [temp__5455__auto__ (datomic.index/needs-new-avet db)]
                         (if temp__5455__auto__
                           (let [attrids temp__5455__auto__]
                             (datomic.index/add-avet-indexes
                               cstore
                               olookup
                               db
                               as_of_t
                               root_map
                               attrids))
                           [root_map []]))
            root_map (nth vec__15741 (unchecked-int 0) nil)
            garbage (nth vec__15741 (unchecked-int 1) nil)
            xents (when excise_enabled? (datomic.index/excise-ents db))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process
                      {:event :excise/ents,
                       :next-t as_of_t,
                       :count (java.lang.Integer/valueOf (int (count xents)))}))
                  nil)
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
                                                           cmpi
                                                           segs_written_ref
                                                           dirs_written_ref)))
                                         written (deref segs_written_ref)
                                         dirs_written (deref dirs_written_ref)
                                         count_mem_idx (count mem_idx)
                                         lt (datomic.index/make-sparse-lt cmp)
                                         N (let [m_15771 {:event :index/mem-index-bytes,
                                                          :index idx_name}
                                                 ___8980__auto__ (let 
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
                                                                           m_15771
                                                                           :phase
                                                                           :begin)))
                                                                     nil)
                                                                   nil)
                                                 start__8981__auto__ (java.lang.System/nanoTime)
                                                 result__8982__auto__ (try
                                                                        {:returned
                                                                         (datomic.index/mem-index-bytes
                                                                           mem_idx)}
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          t__8983__auto__
                                                                          {:threw
                                                                           t__8983__auto__}))
                                                 elapsed_15772 (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8981__auto__)
                                                 msec_15773 (logger/format-as-msec
                                                              (long elapsed_15772))]
                                             (let [endmsg__8984__auto__ (merge
                                                                          (assoc
                                                                            m_15771
                                                                            :msec
                                                                            msec_15773
                                                                            :phase
                                                                            :end)
                                                                          (when
                                                                            (:threw
                                                                              result__8982__auto__)
                                                                            {:threw
                                                                             (class
                                                                               (:threw
                                                                                 result__8982__auto__))}))
                                                   logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.index")]
                                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                 (.info
                                                   ^org.slf4j.Logger logger
                                                   (logger/process endmsg__8984__auto__))
                                                 nil)
                                               nil)
                                             (if (contains? result__8982__auto__ :returned)
                                               (:returned result__8982__auto__)
                                               (do (throw (:threw result__8982__auto__)) nil)))
                                         vec__15766 (datomic.index/stg-index-size
                                                      olookup
                                                      main_idx
                                                      part_size)
                                         M (nth vec__15766 (unchecked-int 0) nil)
                                         main_segs (nth vec__15766 (unchecked-int 1) nil)
                                         M_to_N (if (not (zero? N)) (/ M N) 0)]
                                     (if (and
                                           (nil? mid_idx)
                                           (or
                                             (< M datomic.index/MIN_MAIN_INDEX_THRESHOLD)
                                             (< M_to_N datomic.index/MID_INDEX_THRESHOLD_FACTOR)))
                                       (let [m_15776 {:event :index/merge-main,
                                                      :index idx_name,
                                                      :count
                                                      (java.lang.Integer/valueOf
                                                        (int count_mem_idx)),
                                                      :main-segs main_segs,
                                                      :as-of-t as_of_t,
                                                      :N N,
                                                      :M M}
                                             ___8980__auto__ (let 
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
                                                                       m_15776
                                                                       :phase
                                                                       :begin)))
                                                                 nil)
                                                               nil)
                                             start__8981__auto__ (java.lang.System/nanoTime)
                                             result__8982__auto__ (try
                                                                    {:returned
                                                                     (let 
                                                                       [vec__15780
                                                                        (^clojure.lang.IFn build_index
                                                                          maink
                                                                          (iter/iter-seq
                                                                            (btset/seek mem_idx))
                                                                          true
                                                                          nil
                                                                          garbage)
                                                                        retid
                                                                        (nth
                                                                          vec__15780
                                                                          (unchecked-int 0)
                                                                          nil)
                                                                        garbage
                                                                        (nth
                                                                          vec__15780
                                                                          (unchecked-int 1)
                                                                          nil)
                                                                        retractions
                                                                        (nth
                                                                          vec__15780
                                                                          (unchecked-int 2)
                                                                          nil)
                                                                        vec__15783
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
                                                                          vec__15783
                                                                          (unchecked-int 0)
                                                                          nil)
                                                                        garbage
                                                                        (nth
                                                                          vec__15783
                                                                          (unchecked-int 1)
                                                                          nil)]
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
                                                                                :index/merged-index,
                                                                                :mode :merge-main,
                                                                                :index idx_name,
                                                                                :count
                                                                                (java.lang.Integer/valueOf
                                                                                  (int
                                                                                    count_mem_idx)),
                                                                                :as-of-t as_of_t,
                                                                                :written
                                                                                (-
                                                                                  (deref
                                                                                    segs_written_ref)
                                                                                  written),
                                                                                :dirs-written
                                                                                (-
                                                                                  (deref
                                                                                    dirs_written_ref)
                                                                                  dirs_written)}))
                                                                           nil)
                                                                         nil)
                                                                       [(^clojure.lang.IFn midk
                                                                          root_map)
                                                                        retid
                                                                        hist_retid
                                                                        garbage])}
                                                                    (catch
                                                                      java.lang.Throwable
                                                                      t__8983__auto__
                                                                      {:threw t__8983__auto__}))
                                             elapsed_15777 (-
                                                             (java.lang.System/nanoTime)
                                                             start__8981__auto__)
                                             msec_15778 (logger/format-as-msec
                                                          (long elapsed_15777))]
                                         (let [endmsg__8984__auto__ (merge
                                                                      (assoc
                                                                        m_15776
                                                                        :msec
                                                                        msec_15778
                                                                        :phase
                                                                        :end)
                                                                      (when
                                                                        (:threw
                                                                          result__8982__auto__)
                                                                        {:threw
                                                                         (class
                                                                           (:threw
                                                                             result__8982__auto__))}))
                                               logger (org.slf4j.LoggerFactory/getLogger
                                                        "datomic.index")]
                                           (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                             (.info
                                               ^org.slf4j.Logger logger
                                               (logger/process endmsg__8984__auto__))
                                             nil)
                                           nil)
                                         (if (contains? result__8982__auto__ :returned)
                                           (:returned result__8982__auto__)
                                           (do (throw (:threw result__8982__auto__)) nil)))
                                       (let [TI (*
                                                  (java.lang.Math/sqrt
                                                    (unchecked-double ^java.lang.Number M_to_N))
                                                  N)
                                             vec__15788 (datomic.index/stg-index-size
                                                          olookup
                                                          mid_idx
                                                          part_size)
                                             I (nth vec__15788 (unchecked-int 0) nil)
                                             mid_segs (nth vec__15788 (unchecked-int 1) nil)
                                             TI (if (< I M) (max TI (* I (- 1.0 (/ I M)))) TI)
                                             TI (max TI 2000000)
                                             S (- (+ I N) TI)]
                                         (if (neg? S)
                                           (let [m_15791 {:M M,
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
                                                 ___8980__auto__ (let 
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
                                                                           m_15791
                                                                           :phase
                                                                           :begin)))
                                                                     nil)
                                                                   nil)
                                                 start__8981__auto__ (java.lang.System/nanoTime)
                                                 result__8982__auto__ (try
                                                                        {:returned
                                                                         (let 
                                                                           [vec__15795
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
                                                                              vec__15795
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__15795
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            vec__15798
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
                                                                              vec__15798
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__15798
                                                                              (unchecked-int 1)
                                                                              nil)]
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
                                                                                    as_of_t,
                                                                                    :written
                                                                                    (-
                                                                                      (deref
                                                                                        segs_written_ref)
                                                                                      written),
                                                                                    :dirs-written
                                                                                    (-
                                                                                      (deref
                                                                                        dirs_written_ref)
                                                                                      dirs_written)}))
                                                                               nil)
                                                                             nil)
                                                                           [mid_retid
                                                                            main_retid
                                                                            (^clojure.lang.IFn histk
                                                                              root_map)
                                                                            garbage])}
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          t__8983__auto__
                                                                          {:threw
                                                                           t__8983__auto__}))
                                                 elapsed_15792 (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8981__auto__)
                                                 msec_15793 (logger/format-as-msec
                                                              (long elapsed_15792))]
                                             (let [endmsg__8984__auto__ (merge
                                                                          (assoc
                                                                            m_15791
                                                                            :msec
                                                                            msec_15793
                                                                            :phase
                                                                            :end)
                                                                          (when
                                                                            (:threw
                                                                              result__8982__auto__)
                                                                            {:threw
                                                                             (class
                                                                               (:threw
                                                                                 result__8982__auto__))}))
                                                   logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.index")]
                                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                 (.info
                                                   ^org.slf4j.Logger logger
                                                   (logger/process endmsg__8984__auto__))
                                                 nil)
                                               nil)
                                             (if (contains? result__8982__auto__ :returned)
                                               (:returned result__8982__auto__)
                                               (do (throw (:threw result__8982__auto__)) nil)))
                                           (let [size (long (/ S datomic.index/BYTES_PER_SEG))
                                                 n_segs (if (< size 1) 1 size)
                                                 ratio (fn ratio
                                                         ([num denom]
                                                           (when-not
                                                             (zero? denom)
                                                             (math/round
                                                               (/ (double num) denom)
                                                               2))))
                                                 vec__15802 (let 
                                                              [m_15807
                                                               {:event :event/least-pop-slice,
                                                                :index idx_name,
                                                                :n-segs (long n_segs)}
                                                               ___8980__auto__
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
                                                                         m_15807
                                                                         :phase
                                                                         :begin)))
                                                                   nil)
                                                                 nil)
                                                               start__8981__auto__
                                                               (java.lang.System/nanoTime)
                                                               result__8982__auto__
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
                                                                   t__8983__auto__
                                                                   {:threw t__8983__auto__}))
                                                               elapsed_15808
                                                               (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8981__auto__)
                                                               msec_15809
                                                               (logger/format-as-msec
                                                                 (long elapsed_15808))]
                                                              (let 
                                                                [endmsg__8984__auto__
                                                                 (merge
                                                                   (assoc
                                                                     m_15807
                                                                     :msec
                                                                     msec_15809
                                                                     :phase
                                                                     :end)
                                                                   (when
                                                                     (:threw result__8982__auto__)
                                                                     {:threw
                                                                      (class
                                                                        (:threw
                                                                          result__8982__auto__))}))
                                                                 logger
                                                                 (org.slf4j.LoggerFactory/getLogger
                                                                   "datomic.index")]
                                                                (when
                                                                  (.isInfoEnabled
                                                                    ^org.slf4j.Logger logger)
                                                                  (.info
                                                                    ^org.slf4j.Logger logger
                                                                    (logger/process
                                                                      endmsg__8984__auto__))
                                                                  nil)
                                                                nil)
                                                              (if
                                                                (contains?
                                                                  result__8982__auto__
                                                                  :returned)
                                                                (:returned result__8982__auto__)
                                                                (do
                                                                  (throw
                                                                    (:threw result__8982__auto__))
                                                                  nil)))
                                                 start_key (nth vec__15802 (unchecked-int 0) nil)
                                                 main_pop (nth vec__15802 (unchecked-int 1) nil)
                                                 main_offset (nth vec__15802 (unchecked-int 2) nil)
                                                 slice_segids (take
                                                                (long n_segs)
                                                                (map
                                                                  :seg
                                                                  (.dir-seq
                                                                    (.seek
                                                                      ^datomic.index.Index mid_idx
                                                                      start_key))))
                                                 m_15812 {:M M,
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
                                                 ___8980__auto__ (let 
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
                                                                           m_15812
                                                                           :phase
                                                                           :begin)))
                                                                     nil)
                                                                   nil)
                                                 start__8981__auto__ (java.lang.System/nanoTime)
                                                 result__8982__auto__ (try
                                                                        {:returned
                                                                         (let 
                                                                           [vec__15816
                                                                            (^clojure.lang.IFn build_index
                                                                              maink
                                                                              (mapcat
                                                                                (fn 
                                                                                  fn__15825
                                                                                  ([p1__15736#]
                                                                                    (cache/getx-uncached
                                                                                      olookup
                                                                                      p1__15736#)))
                                                                                slice_segids)
                                                                              true
                                                                              nil
                                                                              garbage)
                                                                            retid
                                                                            (nth
                                                                              vec__15816
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__15816
                                                                              (unchecked-int 1)
                                                                              nil)
                                                                            retractions
                                                                            (nth
                                                                              vec__15816
                                                                              (unchecked-int 2)
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
                                                                                    {:event
                                                                                     :index/step-1,
                                                                                     :index
                                                                                     idx_name,
                                                                                     :written
                                                                                     (-
                                                                                       (deref
                                                                                         segs_written_ref)
                                                                                       written),
                                                                                     :dirs-written
                                                                                     (-
                                                                                       (deref
                                                                                         dirs_written_ref)
                                                                                       dirs_written)}))
                                                                                nil)
                                                                              nil)
                                                                            vec__15819
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
                                                                              vec__15819
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__15819
                                                                              (unchecked-int 1)
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
                                                                                    {:event
                                                                                     :index/step-2,
                                                                                     :index
                                                                                     idx_name,
                                                                                     :written
                                                                                     (-
                                                                                       (deref
                                                                                         segs_written_ref)
                                                                                       written),
                                                                                     :dirs-written
                                                                                     (-
                                                                                       (deref
                                                                                         dirs_written_ref)
                                                                                       dirs_written)}))
                                                                                nil)
                                                                              nil)
                                                                            vec__15822
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
                                                                              vec__15822
                                                                              (unchecked-int 0)
                                                                              nil)
                                                                            garbage
                                                                            (nth
                                                                              vec__15822
                                                                              (unchecked-int 1)
                                                                              nil)]
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
                                                                                    as_of_t,
                                                                                    :written
                                                                                    (-
                                                                                      (deref
                                                                                        segs_written_ref)
                                                                                      written),
                                                                                    :dirs-written
                                                                                    (-
                                                                                      (deref
                                                                                        dirs_written_ref)
                                                                                      dirs_written)}))
                                                                               nil)
                                                                             nil)
                                                                           [mid_retid
                                                                            retid
                                                                            hist_retid
                                                                            garbage])}
                                                                        (catch
                                                                          java.lang.Throwable
                                                                          t__8983__auto__
                                                                          {:threw
                                                                           t__8983__auto__}))
                                                 elapsed_15813 (-
                                                                 (java.lang.System/nanoTime)
                                                                 start__8981__auto__)
                                                 msec_15814 (logger/format-as-msec
                                                              (long elapsed_15813))]
                                             (let [endmsg__8984__auto__ (merge
                                                                          (assoc
                                                                            m_15812
                                                                            :msec
                                                                            msec_15814
                                                                            :phase
                                                                            :end)
                                                                          (when
                                                                            (:threw
                                                                              result__8982__auto__)
                                                                            {:threw
                                                                             (class
                                                                               (:threw
                                                                                 result__8982__auto__))}))
                                                   logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.index")]
                                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                 (.info
                                                   ^org.slf4j.Logger logger
                                                   (logger/process endmsg__8984__auto__))
                                                 nil)
                                               nil)
                                             (if (contains? result__8982__auto__ :returned)
                                               (:returned result__8982__auto__)
                                               (do
                                                 (throw (:threw result__8982__auto__))
                                                 nil)))))))))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :eavt}))
                  nil)
                nil)
            eavt_ret (atom
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
                         :eavt))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :avet}))
                  nil)
                nil)
            avet_ret (atom
                       (^clojure.lang.IFn build_tiered_index
                         :avet
                         :avet-mid
                         (datomic.index/idx-key :avet-main :avet)
                         :avet-hist
                         (fn fn__15852
                           ([p1__15737#]
                             (java.lang.Integer/valueOf
                               (int (.getA ^datomic.impl.db.IDatum p1__15737#)))))
                         (datomic.index/dir-partition-size :avet)
                         (.-avet (.-indexing ^datomic.db.Db db))
                         (.-avet (.-mid-index ^datomic.db.Db db))
                         (.-avet (.-index ^datomic.db.Db db))
                         db/avet-cmp
                         datomic.index/avet-cmpi
                         datomic.index/common-write-handlers
                         :avet))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :aevt}))
                  nil)
                nil)
            aevt_ret (atom
                       (^clojure.lang.IFn build_tiered_index
                         :aevt
                         :aevt-mid
                         (datomic.index/idx-key :aevt-main :aevt)
                         :aevt-hist
                         (fn fn__15854
                           ([p1__15738#]
                             (java.lang.Integer/valueOf
                               (int (.getA ^datomic.impl.db.IDatum p1__15738#)))))
                         (datomic.index/dir-partition-size :aevt)
                         (.-aevt (.-indexing ^datomic.db.Db db))
                         (.-aevt (.-mid-index ^datomic.db.Db db))
                         (.-aevt (.-index ^datomic.db.Db db))
                         db/aevt-cmp
                         datomic.index/aevt-cmpi
                         datomic.index/common-write-handlers
                         :aevt))
            _ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event 'index/merge-db, :index :ft}))
                  nil)
                nil)
            fulltext_ret (if fulltext_enabled?
                           (let [pario (config/property "datomic.indexIOParallelism")
                                 cstore (if pario
                                          (cluster/queueing-writer
                                            cstore
                                            pario
                                            cluster/BOUNDING_TIMEOUT_MSEC
                                            (fn fn__15856
                                              ([p1__15739#]
                                                (monitor/add-stat :IndexIOQueueCount p1__15739#))))
                                          cstore)
                                 ret (atom
                                       (fulltext/build-index
                                         cstore
                                         olookup
                                         db
                                         (.-aevt (.-indexing ^datomic.db.Db db))
                                         (map
                                           (fn fn__15858
                                             ([p1__15740#] (db/resolve-id db p1__15740#)))
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
                    (logger/process {:event 'index/merge-db, :index :raet}))
                  nil)
                nil)
            vec__15744 (^clojure.lang.IFn build_tiered_index
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
            mid_raetid (nth vec__15744 (unchecked-int 0) nil)
            raetid (nth vec__15744 (unchecked-int 1) nil)
            hist_raetid (nth vec__15744 (unchecked-int 2) nil)
            garbage (nth vec__15744 (unchecked-int 3) nil)
            vec__15747 (deref eavt_ret)
            mid_eavtid (nth vec__15747 (unchecked-int 0) nil)
            eavtid (nth vec__15747 (unchecked-int 1) nil)
            hist_eavtid (nth vec__15747 (unchecked-int 2) nil)
            eavt_garbage (nth vec__15747 (unchecked-int 3) nil)
            vec__15750 (deref avet_ret)
            mid_avetid (nth vec__15750 (unchecked-int 0) nil)
            avetid (nth vec__15750 (unchecked-int 1) nil)
            hist_avetid (nth vec__15750 (unchecked-int 2) nil)
            avet_garbage (nth vec__15750 (unchecked-int 3) nil)
            vec__15753 (let [temp__5455__auto__ (seq (datomic.index/dropped-avet-aids db))]
                         (if temp__5455__auto__
                           (let [attrids temp__5455__auto__]
                             (datomic.index/drop-avet-indexes
                               cstore
                               olookup
                               [mid_avetid avetid hist_avetid]
                               attrids
                               as_of_t
                               avet_garbage))
                           [[mid_avetid avetid hist_avetid] avet_garbage]))
            vec__15756 (nth vec__15753 (unchecked-int 0) nil)
            mid_avetid (nth vec__15756 (unchecked-int 0) nil)
            avetid (nth vec__15756 (unchecked-int 1) nil)
            hist_avetid (nth vec__15756 (unchecked-int 2) nil)
            avet_garbage (nth vec__15753 (unchecked-int 1) nil)
            vec__15759 (deref aevt_ret)
            mid_aevtid (nth vec__15759 (unchecked-int 0) nil)
            aevtid (nth vec__15759 (unchecked-int 1) nil)
            hist_aevtid (nth vec__15759 (unchecked-int 2) nil)
            aevt_garbage (nth vec__15759 (unchecked-int 3) nil)
            vec__15762 (deref fulltext_ret)
            fulltextid (nth vec__15762 (unchecked-int 0) nil)
            hist_fulltextid (nth vec__15762 (unchecked-int 1) nil)
            fulltext_garbage_keys (nth vec__15762 (unchecked-int 2) nil)
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
        (let [written (inc (deref segs_written_ref)) dirs_written (deref dirs_written_ref)]
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
                     (long (.longCount ^datomic.btset.IDataSet btset)))}))
              nil)
            nil)
          (monitor/add-stat :IndexWrites written)
          (monitor/add-stat :IndexDirWrites dirs_written))
        [rootid xpreds (conj garbage (cluster/uuid->val-key old_root_id))])))
  (defn merge-db
    ([cstore olookup db as_of_t]
      (when-not (.-indexing ^datomic.db.Db db)
        (throw
          (java.lang.IllegalStateException. "db must be prepared with db/prepare-for-indexing")))
      (try
        (let [m_15864 {:event :index/merge-db, :as-of-t as_of_t}
              ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_15864 :phase :begin)))
                                  nil)
                                nil)
              start__8981__auto__ (java.lang.System/nanoTime)
              result__8982__auto__ (try
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
                                                                                    [temp__5457__auto__
                                                                                     (config/property
                                                                                       "datomic.indexSegsPerSecond")]
                                                                                    (when
                                                                                      temp__5457__auto__
                                                                                      (let 
                                                                                        [sps
                                                                                         temp__5457__auto__
                                                                                         calc
                                                                                         (datomic.index/create-pace-calculator
                                                                                           (/
                                                                                             sps
                                                                                             10)
                                                                                           100)]
                                                                                        (fn 
                                                                                          fn__15868
                                                                                          ([segs]
                                                                                            (let 
                                                                                              [temp__5457__auto__
                                                                                               (^clojure.lang.IFn calc
                                                                                                 segs)]
                                                                                              (when
                                                                                                temp__5457__auto__
                                                                                                (let 
                                                                                                  [msec
                                                                                                   temp__5457__auto__]
                                                                                                  (monitor/add-stat
                                                                                                    :IndexPacingMsec
                                                                                                    msec)
                                                                                                  (java.lang.Thread/sleep
                                                                                                    (unchecked-long
                                                                                                      ^java.lang.Number msec))
                                                                                                  nil))))))))]
                                            (let [vec__15872 (datomic.index/merge-db*
                                                               cstore
                                                               olookup
                                                               db
                                                               as_of_t
                                                               (:key storage_index_root_ref)
                                                               nil
                                                               true
                                                               true)
                                                  rootid (nth vec__15872 (unchecked-int 0) nil)
                                                  xpreds (nth vec__15872 (unchecked-int 1) nil)
                                                  garbage (nth vec__15872 (unchecked-int 2) nil)
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
                                       t__8983__auto__
                                       {:threw t__8983__auto__}))
              elapsed_15865 (- (java.lang.System/nanoTime) start__8981__auto__)
              msec_15866 (logger/format-as-msec (long elapsed_15865))]
          (let [endmsg__8984__auto__ (merge
                                       (assoc m_15864 :msec msec_15866 :phase :end)
                                       (when (:threw result__8982__auto__)
                                         {:threw (class (:threw result__8982__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.index")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
              nil)
            nil)
          (if (contains? result__8982__auto__ :returned)
            (:returned result__8982__auto__)
            (do (throw (:threw result__8982__auto__)) nil)))
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
            nil))))))
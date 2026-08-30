(do
  (clojure.core/in-ns 'datomic.datalog)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'compare))
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.index :as 'index]
        ['datomic.config :as 'config]
        ['datomic.cache :as 'cache]
        ['datomic.error :as 'error]
        ['datomic.iter :as 'iter]
        ['datomic.extensions :as 'ext]
        ['datomic.common :as 'common :refer (clojure.core/list 'compare)]
        ['datomic.measure.query-stats :as 'query-stats]
        ['clojure.set :as 'set]
        ['clojure.string :as 'str]
        ['clojure.data :as 'data])
      (clojure.core/import 'java.util.Arrays)
      (clojure.core/import 'java.util.Set)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.util.HashSet)
      (clojure.core/import 'java.util.HashMap)
      (clojure.core/import 'java.util.Iterator)
      (clojure.core/import 'java.util.Collection)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/import 'java.util.concurrent.Executors)
      (clojure.core/import 'java.util.concurrent.ThreadFactory)
      (clojure.core/import 'java.util.concurrent.ScheduledExecutorService)
      (clojure.core/import 'java.util.concurrent.ScheduledThreadPoolExecutor)
      (clojure.core/import 'datomic.db.Db)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.db.IDbImpl)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.btset.IDataSet)
      (clojure.core/import 'datomic.iter.Iter)))
  (when-not (.equals 'datomic.datalog 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.datalog))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'compare))
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.index :as 'index]
          ['datomic.config :as 'config]
          ['datomic.cache :as 'cache]
          ['datomic.error :as 'error]
          ['datomic.iter :as 'iter]
          ['datomic.extensions :as 'ext]
          ['datomic.common :as 'common :refer (clojure.core/list 'compare)]
          ['datomic.measure.query-stats :as 'query-stats]
          ['clojure.set :as 'set]
          ['clojure.string :as 'str]
          ['clojure.data :as 'data])
        (clojure.core/import 'java.util.Arrays)
        (clojure.core/import 'java.util.Set)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.util.HashSet)
        (clojure.core/import 'java.util.HashMap)
        (clojure.core/import 'java.util.Iterator)
        (clojure.core/import 'java.util.Collection)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'java.util.concurrent.ConcurrentMap)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/import 'java.util.concurrent.Executors)
        (clojure.core/import 'java.util.concurrent.ThreadFactory)
        (clojure.core/import 'java.util.concurrent.ScheduledExecutorService)
        (clojure.core/import 'java.util.concurrent.ScheduledThreadPoolExecutor)
        (clojure.core/import 'datomic.db.Db)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.db.IDbImpl)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.btset.IDataSet)
        (clojure.core/import 'datomic.iter.Iter))))
  (set! *warn-on-reflection* true)
  (set! *unchecked-math* true)
  (def query-thread-prefix "query-40e7d292-b60a-40ef-b6d8-db96660e415b-")
  (reset-meta!
    #'query-thread-prefix
    (assoc {:const true, :column (int 1)} :name 'query-thread-prefix :ns *ns*))
  (defn on-query-thread?
    ([]
      (str/starts-with?
        (.getName (java.lang.Thread/currentThread))
        "query-40e7d292-b60a-40ef-b6d8-db96660e415b-")))
  (reset-meta!
    #'on-query-thread?
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'on-query-thread? :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.datalog" "query-pool")
    {:tag java.util.concurrent.ExecutorService, :column (int 1)})
  (let [v__6812__auto__ #'query-pool]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.datalog" "query-pool")
        {:tag java.util.concurrent.ExecutorService, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.datalog" "query-pool")
        (common/thread-pool
          {:nthreads (config/property "datomic.queryPool"),
           :name "query-40e7d292-b60a-40ef-b6d8-db96660e415b-",
           :metrics? false}))
      #'query-pool))
  (defn qmapv
    ([f coll] (if (on-query-thread?) (mapv f coll) (common/pooled-mapv query-pool f coll))))
  (reset-meta!
    #'qmapv
    (assoc {:arglists (clojure.core/list ['f 'coll]), :column (int 1)} :name 'qmapv :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.datalog" "cancel-service")
    {:tag java.util.concurrent.ScheduledExecutorService, :column (int 1)})
  (let [v__6812__auto__ #'cancel-service]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.datalog" "cancel-service")
        {:tag java.util.concurrent.ScheduledExecutorService, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.datalog" "cancel-service")
        (java.util.concurrent.ScheduledThreadPoolExecutor. (unchecked-int 1)))
      #'cancel-service))
  (def QUERY_DEFAULT_TIMEOUT nil)
  (reset-meta!
    #'QUERY_DEFAULT_TIMEOUT
    (assoc {:const true, :column (int 1)} :name 'QUERY_DEFAULT_TIMEOUT :ns *ns*))
  (.setDynamic (clojure.lang.RT/var "datomic.datalog" "*cancel*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.datalog" "*cancel*") true)
    {:dynamic true, :column (int 1)})
  (.bindRoot (.setDynamic (clojure.lang.RT/var "datomic.datalog" "*cancel*") true) (atom nil))
  (defn maybe-cancel
    ([]
      (let [temp__5804__auto__ (deref *cancel*)]
        (when temp__5804__auto__
          (let [why temp__5804__auto__]
            (throw (java.util.concurrent.TimeoutException. (str "Query canceled: " why)))))
        nil)))
  (reset-meta!
    #'maybe-cancel
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'maybe-cancel :ns *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      IJoin
      (join-project [xs ys join-map project-map-x project-map-y predctor])
      (join-project-with [ys xs join-map project-map-x project-map-y predctor]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.datalog" "IJoin")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'IJoin :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'join-project
                                        {:arglists
                                         (clojure.core/list
                                           ['xs
                                            'ys
                                            'join-map
                                            'project-map-x
                                            'project-map-y
                                            'predctor])}),
                                      :arglists
                                      (clojure.core/list
                                        ['xs
                                         'ys
                                         'join-map
                                         'project-map-x
                                         'project-map-y
                                         'predctor]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.datalog" "IJoin"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.datalog" "join-project")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'join-project-with
                                        {:arglists
                                         (clojure.core/list
                                           ['ys
                                            'xs
                                            'join-map
                                            'project-map-x
                                            'project-map-y
                                            'predctor])}),
                                      :arglists
                                      (clojure.core/list
                                        ['ys
                                         'xs
                                         'join-map
                                         'project-map-x
                                         'project-map-y
                                         'predctor]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.datalog" "IJoin"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.datalog" "join-project-with")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*))))
  (defn truep ([] (fn fn__18048 ([_] true))))
  (reset-meta!
    #'truep
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'truep :ns *ns*))
  (defn tuple
    ([arr]
      (let [array (to-array arr)]
        (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" array))))
  (reset-meta!
    #'tuple
    (assoc {:arglists (clojure.core/list ['arr]), :column (int 1)} :name 'tuple :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.datalog" "iterator")
    {:tag java.util.Iterator,
     :private true,
     :arglists (clojure.core/list [(.withMeta 'xs {:tag 'Iterable})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.datalog" "iterator")
    (fn iterator ([xs] (.iterator ^java.lang.Iterable xs))))
  (defn matchf
    ([bindings]
      (fn fn__18053
        ([x y]
          (loop [i 0]
            (if (< i (alength ^"[Ljava.lang.Object;" bindings))
              (let [b (aget ^"[Ljava.lang.Object;" bindings (int i))]
                (if b
                  (if (= (nth x (unchecked-int i)) (nth y (unchecked-int ^java.lang.Number b)))
                    (recur (inc i))
                    false)
                  (recur (inc i))))
              true))))))
  (reset-meta!
    #'matchf
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bindings {:tag 'objects})]), :column (int 1)}
      :name
      'matchf
      :ns
      *ns*))
  (defn hashxf
    ([bindings]
      (fn fn__18056
        ([x]
          (loop [i 0 h 0]
            (if (< i (alength ^"[Ljava.lang.Object;" bindings))
              (let [b (aget ^"[Ljava.lang.Object;" bindings (int i))]
                (recur
                  (inc i)
                  (if b
                    (clojure.lang.Util/hashCombine
                      (unchecked-int h)
                      (int (clojure.lang.Util/hash (nth x (unchecked-int i)))))
                    h)))
              (long h)))))))
  (reset-meta!
    #'hashxf
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bindings {:tag 'objects})]), :column (int 1)}
      :name
      'hashxf
      :ns
      *ns*))
  (defn hashyf
    ([bindings]
      (fn fn__18059
        ([y]
          (loop [i 0 h 0]
            (if (< i (alength ^"[Ljava.lang.Object;" bindings))
              (let [b (aget ^"[Ljava.lang.Object;" bindings (int i))]
                (recur
                  (inc i)
                  (if b
                    (clojure.lang.Util/hashCombine
                      (unchecked-int h)
                      (int (clojure.lang.Util/hash (nth y (unchecked-int ^java.lang.Number b)))))
                    h)))
              (long h)))))))
  (reset-meta!
    #'hashyf
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bindings {:tag 'objects})]), :column (int 1)}
      :name
      'hashyf
      :ns
      *ns*))
  (defn partv
    ([^long n coll]
      (let [pv (fn pv
                 ([iter]
                   (lazy-seq
                     (when (.hasNext ^java.util.Iterator iter)
                       (cons
                         (loop [i 0 ret (transient [])]
                           (if (and (< i n) (.hasNext ^java.util.Iterator iter))
                             (recur (inc i) (conj! ret (.next ^java.util.Iterator iter)))
                             (persistent! ret)))
                         (^clojure.lang.IFn pv iter))))))]
        (^clojure.lang.IFn pv (.iterator ^java.lang.Iterable coll)))))
  (reset-meta!
    #'partv
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'n {:tag 'long}) (.withMeta 'coll {:tag 'Iterable})]),
       :column (int 1)}
      :name
      'partv
      :ns
      *ns*))
  (defn join-project-coll
    ([xs ys join_map project_map_x project_map_y predctor]
      (join-project-with
        ys
        xs
        (zipmap (vals join_map) (keys join_map))
        project_map_y
        project_map_x
        predctor)))
  (reset-meta!
    #'join-project-coll
    (assoc
      {:arglists (clojure.core/list ['xs 'ys 'join-map 'project-map-x 'project-map-y 'predctor]),
       :column (int 1)}
      :name
      'join-project-coll
      :ns
      *ns*))
  (defn join-project-coll-with
    ([xs ys join_map project_map_x project_map_y predctor]
      (cond
        (< (count xs) (count ys)) (join-project-with
                                    ys
                                    xs
                                    (zipmap (vals join_map) (keys join_map))
                                    project_map_y
                                    project_map_x
                                    predctor)
        (and
          (empty? join_map)
          (empty? project_map_y)
          (=
            (long (count project_map_x))
            (if (instance? java.util.Map$Entry (first xs))
              2
              (java.lang.Integer/valueOf (int (count (first xs))))))
          (= (keys project_map_x) (vals project_map_x))
          (identical? truep predctor)
          (instance? java.util.Set xs)) xs
        :else (do
                (let [px_from (to-array (keys project_map_x))
                      px_to (to-array (vals project_map_x))
                      py_from (to-array (keys project_map_y))
                      py_to (to-array (vals project_map_y))
                      proj_count (count (into (set px_to) py_to))
                      ks (seq (keys join_map))
                      bindings (object-array (if ks (inc (apply max ks)) 0))
                      _ (loop [seq_18071 (seq join_map) chunk_18072 nil count_18073 0 i_18074 0]
                          (if (< i_18074 count_18073)
                            (let [vec__18076 (.nth
                                               ^clojure.lang.Indexed chunk_18072
                                               (unchecked-int i_18074))
                                  k (nth vec__18076 (unchecked-int 0) nil)
                                  v (nth vec__18076 (unchecked-int 1) nil)]
                              (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                              (recur seq_18071 chunk_18072 count_18073 (inc i_18074)))
                            (let [temp__5804__auto__ (seq seq_18071)]
                              (when temp__5804__auto__
                                (let [seq_18071 temp__5804__auto__]
                                  (if (chunked-seq? seq_18071)
                                    (let [c__6065__auto__ (chunk-first seq_18071)]
                                      (recur
                                        (chunk-rest seq_18071)
                                        c__6065__auto__
                                        (count c__6065__auto__)
                                        0))
                                    (let [vec__18079 (first seq_18071)
                                          k (nth vec__18079 (unchecked-int 0) nil)
                                          v (nth vec__18079 (unchecked-int 1) nil)]
                                      (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                                      (recur (next seq_18071) nil 0 0))))))))
                      match? (matchf bindings)
                      hashx (hashxf bindings)
                      hashy (hashyf bindings)
                      project (fn project
                                ([x y]
                                  (let [ret (object-array
                                              (java.lang.Integer/valueOf (int proj_count)))]
                                    (dotimes [i (alength ^"[Ljava.lang.Object;" px_from)]
                                      (aset
                                        ^"[Ljava.lang.Object;" ret
                                        (unchecked-int (aget ^"[Ljava.lang.Object;" px_to (int i)))
                                        (nth
                                          x
                                          (unchecked-int
                                            (aget ^"[Ljava.lang.Object;" px_from (int i))))))
                                    (dotimes [i (alength ^"[Ljava.lang.Object;" py_from)]
                                      (aset
                                        ^"[Ljava.lang.Object;" ret
                                        (unchecked-int (aget ^"[Ljava.lang.Object;" py_to (int i)))
                                        (nth
                                          y
                                          (unchecked-int
                                            (aget ^"[Ljava.lang.Object;" py_from (int i))))))
                                    (tuple ret))))
                      ret (java.util.Collections/newSetFromMap
                            (java.util.concurrent.ConcurrentHashMap.))
                      ht (when (and (seq bindings) (> (count ys) 10))
                           (let [ret (java.util.HashMap.) yiter (iterator ys)]
                             (while
                               (.hasNext ^java.util.Iterator yiter)
                               (let [y (.next ^java.util.Iterator yiter)
                                     h (^clojure.lang.IFn hashy y)
                                     vs (or
                                          (.get ^java.util.HashMap ret h)
                                          (let [vs (java.util.ArrayList. (unchecked-int 2))]
                                            (.put ^java.util.HashMap ret h vs)
                                            vs))]
                                 (.add ^java.util.ArrayList vs y)))
                             ret))
                      PART 10
                      proc (fn proc
                             ([xs]
                               (let [pred (^clojure.lang.IFn predctor) xiter (iterator xs)]
                                 (maybe-cancel)
                                 (if ht
                                   (do
                                     (while
                                       (.hasNext ^java.util.Iterator xiter)
                                       (let [x (.next ^java.util.Iterator xiter)
                                             temp__5804__auto__ (.get
                                                                  ^java.util.Map ht
                                                                  (^clojure.lang.IFn hashx x))]
                                         (when temp__5804__auto__
                                           (let [ys temp__5804__auto__ yiter (iterator ys)]
                                             (while
                                               (.hasNext ^java.util.Iterator yiter)
                                               (let [y (.next ^java.util.Iterator yiter)]
                                                 (when (^clojure.lang.IFn match? x y)
                                                   (let [p (^clojure.lang.IFn project x y)]
                                                     (when (^clojure.lang.IFn pred p)
                                                       (.add ^java.util.Set ret p))))))
                                             nil))))
                                     nil)
                                   (do
                                     (while
                                       (.hasNext ^java.util.Iterator xiter)
                                       (let [x (.next ^java.util.Iterator xiter)
                                             yiter (iterator ys)]
                                         (while
                                           (.hasNext ^java.util.Iterator yiter)
                                           (let [y (.next ^java.util.Iterator yiter)]
                                             (when (^clojure.lang.IFn match? x y)
                                               (let [p (^clojure.lang.IFn project x y)]
                                                 (when (^clojure.lang.IFn pred p)
                                                   (.add ^java.util.Set ret p))))))
                                         nil))
                                     nil)))))]
                  (qmapv proc (partv PART xs))
                  ret)))))
  (reset-meta!
    #'join-project-coll-with
    (assoc
      {:arglists (clojure.core/list ['xs 'ys 'join-map 'project-map-x 'project-map-y 'predctor]),
       :column (int 1)}
      :name
      'join-project-coll-with
      :ns
      *ns*))
  (extend
    java.util.Collection
    IJoin
    {:join-project
     (fn fn__18100
       ([xs ys join_map project_map_x project_map_y predctor]
         (join-project-coll xs ys join_map project_map_x project_map_y predctor))),
     :join-project-with
     (fn fn__18102
       ([xs ys join_map project_map_x project_map_y predctor]
         (join-project-coll-with xs ys join_map project_map_x project_map_y predctor)))})
  (extend
    java.lang.Object
    IJoin
    {:join-project
     (fn fn__18104
       ([xs ys join_map project_map_x project_map_y predctor]
         (cond
           (instance? java.lang.Iterable xs) (join-project-coll
                                               xs
                                               ys
                                               join_map
                                               project_map_x
                                               project_map_y
                                               predctor)
           (instance? java.util.Map xs) (join-project-coll
                                          (seq xs)
                                          ys
                                          join_map
                                          project_map_x
                                          project_map_y
                                          predctor)
           :else (do
                   (error/arg
                     :db.error/invalid-data-source
                     (str (.getClass xs) " is not a valid data source type.")
                     {:input xs}))))),
     :join-project-with
     (fn fn__18106
       ([xs ys join_map project_map_x project_map_y predctor]
         (cond
           (instance? java.lang.Iterable xs) (join-project-coll-with
                                               xs
                                               ys
                                               join_map
                                               project_map_x
                                               project_map_y
                                               predctor)
           (instance? java.util.Map xs) (join-project-coll-with
                                          (seq xs)
                                          ys
                                          join_map
                                          project_map_x
                                          project_map_y
                                          predctor)
           :else (do
                   (error/arg
                     :db.error/invalid-data-source
                     (str (.getClass xs) " is not a valid data source type.")
                     {:input xs})))))})
  (deftype DbRel [db isref iskey consts starts whiles])
  (clojure.core/import 'datomic.datalog.DbRel)
  (defn ->DbRel
    ([db isref iskey consts starts whiles]
      (datomic.datalog.DbRel. db isref iskey consts starts whiles)))
  (reset-meta!
    #'->DbRel
    (assoc
      {:arglists (clojure.core/list ['db 'isref 'iskey 'consts 'starts 'whiles]), :column (int 1)}
      :name
      '->DbRel
      :ns
      *ns*))
  (defn resolve-id
    ([db x]
      (when x
        (or
          (db/resolve-id db x)
          (do (throw (java.lang.IllegalArgumentException. (str "Cannot resolve key: " x))) nil)))))
  (reset-meta!
    #'resolve-id
    (assoc {:arglists (clojure.core/list ['db 'x]), :column (int 1)} :name 'resolve-id :ns *ns*))
  (defn dbrel
    ([db p__18115 starts whiles]
      (let [vec__18116 p__18115
            e (nth vec__18116 (unchecked-int 0) nil)
            a (nth vec__18116 (unchecked-int 1) nil)
            v (nth vec__18116 (unchecked-int 2) nil)
            t (nth vec__18116 (unchecked-int 3) nil)
            added (nth vec__18116 (unchecked-int 4) nil)
            attrid (and a (long (db/require-id db a)))
            attr (and attrid (db/attribute db attrid))
            ref? (and attr (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
            key? (and attr (= 21 (.-vtypeid ^datomic.db.Attribute attr)))]
        (datomic.datalog.DbRel.
          db
          ref?
          key?
          [(resolve-id db e)
           attrid
           (cond ref? (resolve-id db v) key? (db/normalize-kw v) :else (do v))
           t
           added]
          starts
          whiles))))
  (reset-meta!
    #'dbrel
    (assoc
      {:arglists (clojure.core/list ['db ['e 'a 'v 't 'added] 'starts 'whiles]), :column (int 1)}
      :name
      'dbrel
      :ns
      *ns*))
  (let [protocol_metadata__7468 {:column (int 1)}]
    (defprotocol ExtRel (extrel [src consts starts whiles]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.datalog" "ExtRel")
      (assoc (assoc protocol_metadata__7468 :doc nil) :name 'ExtRel :ns *ns*))
    (let [protocol_signature__7469 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'extrel
                                        {:arglists
                                         (clojure.core/list ['src 'consts 'starts 'whiles])}),
                                      :arglists (clojure.core/list ['src 'consts 'starts 'whiles]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.datalog" "ExtRel"))
          protocol_method_name__7470 (with-meta
                                       (:name protocol_signature__7469)
                                       protocol_signature__7469)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.datalog" "extrel")
        (assoc protocol_signature__7469 :name protocol_method_name__7470 :ns *ns*))))
  (defn extrel-coll
    ([src consts]
      (if (every? nil? consts)
        src
        (filter
          (fn fn__18147
            ([p1__18146#]
              (loop [i 0]
                (if (< i (count consts))
                  (if (or
                        (nil? (nth consts (unchecked-int i)))
                        (= (nth p1__18146# (unchecked-int i)) (nth consts (unchecked-int i))))
                    (recur (inc i))
                    false)
                  true))))
          src))))
  (reset-meta!
    #'extrel-coll
    (assoc
      {:arglists (clojure.core/list ['src 'consts]), :column (int 1)}
      :name
      'extrel-coll
      :ns
      *ns*))
  (extend
    nil
    ExtRel
    {:extrel
     (fn fn__18151
       ([src consts _ _]
         (error/arg
           :db.error/invalid-data-source
           "Nil or missing data source. Did you forget to pass a database argument?"
           {:input src})))})
  (extend
    java.lang.Object
    ExtRel
    {:extrel
     (fn fn__18153
       ([src consts _ _]
         (if (instance? java.lang.Iterable src)
           (extrel-coll src consts)
           (error/arg
             :db.error/invalid-data-source
             (str (.getClass src) " is not a valid data source type.")
             {:input src}))))})
  (extend
    datomic.db.Db
    ExtRel
    {:extrel (fn fn__18155 ([src consts starts whiles] (dbrel src consts starts whiles)))})
  (extend
    java.util.Map
    ExtRel
    {:extrel (fn fn__18157 ([src consts _ _] (extrel (seq src) consts nil nil)))})
  (extend
    java.util.Collection
    ExtRel
    {:extrel (fn fn__18159 ([src consts _ _] (extrel-coll src consts)))})
  (def E 0)
  (reset-meta! #'E (assoc {:const true, :column (int 1)} :name 'E :ns *ns*))
  (def A 1)
  (reset-meta! #'A (assoc {:const true, :column (int 1)} :name 'A :ns *ns*))
  (def V 2)
  (reset-meta! #'V (assoc {:const true, :column (int 1)} :name 'V :ns *ns*))
  (def T 3)
  (reset-meta! #'T (assoc {:const true, :column (int 1)} :name 'T :ns *ns*))
  (def ADDED 4)
  (reset-meta! #'ADDED (assoc {:const true, :column (int 1)} :name 'ADDED :ns *ns*))
  (def DCOUNT 5)
  (reset-meta! #'DCOUNT (assoc {:const true, :column (int 1)} :name 'DCOUNT :ns *ns*))
  (extend
    datomic.datalog.DbRel
    IJoin
    {:join-project
     (fn fn__18175
       ([dbrel ys join_map project_map_x project_map_y predctor]
         (join-project-with dbrel ys join_map project_map_x project_map_y predctor))),
     :join-project-with
     (fn fn__18177
       ([dbrel ys join_map project_map_x project_map_y predctor]
         (let [db (.-db ^datomic.datalog.DbRel dbrel)
               consts (.-consts ^datomic.datalog.DbRel dbrel)
               starts (.-starts ^datomic.datalog.DbRel dbrel)
               whiles (.-whiles ^datomic.datalog.DbRel dbrel)
               const_attrid (get consts 1)
               const_attr (when const_attrid (.elementAt ^datomic.db.IDbImpl db const_attrid))
               startv (and const_attr (.hasAVET ^datomic.db.Attribute const_attr) (get starts 2))
               whilev (get whiles 2)
               starte (and const_attr (get starts 0))
               whilee (get whiles 0)
               px_from (to-array (keys project_map_x))
               px_to (to-array (vals project_map_x))
               py_from (to-array (keys project_map_y))
               py_to (to-array (vals project_map_y))
               proj_count (count (into (set px_to) py_to))
               bindings (to-array
                          (map (fn fn__18184 ([p1__18161#] (get join_map p1__18161#))) (range 5)))
               bound (object-array 5)
               _ (dotimes [i 5]
                   (when (or
                           (aget ^"[Ljava.lang.Object;" bindings (int i))
                           (not (nil? (get consts (long i)))))
                     (aset ^"[Ljava.lang.Object;" bound (int i) true)))
               bind (fn bind
                      ([y]
                        (let [added (cond
                                      (not (nil? (get consts 4))) (get consts 4)
                                      (aget ^"[Ljava.lang.Object;" bindings 4) (nth
                                                                                 y
                                                                                 (unchecked-int
                                                                                   (aget
                                                                                     ^"[Ljava.lang.Object;" bindings
                                                                                     4)))
                                      :else (do true))]
                          ((if added db/asserting-datum db/retracting-datum)
                            (or
                              (get consts 0)
                              (if (aget ^"[Ljava.lang.Object;" bindings 0)
                                (resolve-id
                                  db
                                  (nth y (unchecked-int (aget ^"[Ljava.lang.Object;" bindings 0))))
                                (long java.lang.Long/MIN_VALUE)))
                            (or
                              const_attrid
                              (if (aget ^"[Ljava.lang.Object;" bindings 1)
                                (resolve-id
                                  db
                                  (nth y (unchecked-int (aget ^"[Ljava.lang.Object;" bindings 1))))
                                -1))
                            (let [v (let [cv (get consts 2)]
                                      (if (nil? cv)
                                        (when (aget ^"[Ljava.lang.Object;" bindings 2)
                                          (nth
                                            y
                                            (unchecked-int
                                              (aget ^"[Ljava.lang.Object;" bindings 2))))
                                        cv))]
                              (if v
                                (cond
                                  (.-isref ^datomic.datalog.DbRel dbrel) (resolve-id db v)
                                  (.-iskey ^datomic.datalog.DbRel dbrel) (db/normalize-kw v)
                                  :else (do v))
                                v))
                            (or
                              (let [temp__5804__auto__ (get consts 3)]
                                (when temp__5804__auto__ (let [t temp__5804__auto__] t)))
                              (if (aget ^"[Ljava.lang.Object;" bindings 3)
                                (nth y (unchecked-int (aget ^"[Ljava.lang.Object;" bindings 3)))
                                2305843009213693951))))))
               vec__18178 (if (aget ^"[Ljava.lang.Object;" bound 1)
                            (cond
                              (aget ^"[Ljava.lang.Object;" bound 0) [(fn 
                                                                       fn__18195
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           (.getE
                                                                             ^datomic.impl.db.IDatum d)
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           (cond
                                                                             (aget
                                                                               ^"[Ljava.lang.Object;" bound
                                                                               2)
                                                                             (.getV
                                                                               ^datomic.impl.db.IDatum d)
                                                                             startv (do startv))
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__18197
                                                                       ([d]
                                                                         (db/windowed
                                                                           db
                                                                           (fn 
                                                                             fn__18198
                                                                             ([p1__18162#]
                                                                               (and
                                                                                 (=
                                                                                   (long
                                                                                     (.getE
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getE
                                                                                       ^datomic.impl.db.IDatum p1__18162#)))
                                                                                 (=
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__18162#)))
                                                                                 (or
                                                                                   (and
                                                                                     (not
                                                                                       (aget
                                                                                         ^"[Ljava.lang.Object;" bound
                                                                                         2))
                                                                                     (nil? whilev))
                                                                                   (if
                                                                                     (aget
                                                                                       ^"[Ljava.lang.Object;" bound
                                                                                       2)
                                                                                     (=
                                                                                       (.getV
                                                                                         ^datomic.impl.db.IDatum d)
                                                                                       (.getV
                                                                                         ^datomic.impl.db.IDatum p1__18162#))
                                                                                     (^clojure.lang.IFn whilev
                                                                                       (.getV
                                                                                         ^datomic.impl.db.IDatum p1__18162#)))))))
                                                                           (.seekAEVT
                                                                             ^datomic.db.IDb db
                                                                             ^datomic.impl.db.IDatum d))))]
                              (aget ^"[Ljava.lang.Object;" bound 2) [(fn 
                                                                       fn__18205
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           java.lang.Long/MIN_VALUE
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           (.getV
                                                                             ^datomic.impl.db.IDatum d)
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__18207
                                                                       ([d]
                                                                         (let 
                                                                           [attr
                                                                            (.elementAt
                                                                              ^datomic.db.IDbImpl db
                                                                              (java.lang.Integer/valueOf
                                                                                (int
                                                                                  (.getA
                                                                                    ^datomic.impl.db.IDatum d))))]
                                                                           (when
                                                                             (instance?
                                                                               datomic.db.Attribute
                                                                               attr)
                                                                             (cond
                                                                               (.hasAVET
                                                                                 ^datomic.db.Attribute attr)
                                                                               (db/windowed
                                                                                 db
                                                                                 (fn 
                                                                                   fn__18208
                                                                                   ([p1__18163#]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum p1__18163#)))
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__18163#)))))
                                                                                 (.seekAVET
                                                                                   ^datomic.db.IDb db
                                                                                   ^datomic.impl.db.IDatum d))
                                                                               (=
                                                                                 20
                                                                                 (.-vtypeid
                                                                                   ^datomic.db.Attribute attr))
                                                                               (db/windowed
                                                                                 db
                                                                                 (fn 
                                                                                   fn__18211
                                                                                   ([p1__18164#]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum p1__18164#)))
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__18164#)))))
                                                                                 (.seekRAET
                                                                                   ^datomic.db.IDb db
                                                                                   ^datomic.impl.db.IDatum d))
                                                                               :else
                                                                               (do
                                                                                 (iter/filter
                                                                                   (fn 
                                                                                     fn__18214
                                                                                     ([p1__18165#]
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__18165#))))
                                                                                   (db/windowed
                                                                                     db
                                                                                     (fn 
                                                                                       fn__18216
                                                                                       ([p1__18166#]
                                                                                         (=
                                                                                           (long
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d))
                                                                                           (long
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum p1__18166#)))))
                                                                                     (.seekAEVT
                                                                                       ^datomic.db.IDb db
                                                                                       ^datomic.impl.db.IDatum d)))))))))]
                              startv [(fn fn__18219
                                        ([d]
                                          (db/asserting-datum
                                            java.lang.Long/MIN_VALUE
                                            (.getA ^datomic.impl.db.IDatum d)
                                            startv
                                            2305843009213693951)))
                                      (fn fn__18221
                                        ([d]
                                          (db/windowed
                                            db
                                            (fn fn__18222
                                              ([p1__18167#]
                                                (and
                                                  (=
                                                    const_attrid
                                                    (long
                                                      (.getA ^datomic.impl.db.IDatum p1__18167#)))
                                                  (or
                                                    (nil? whilev)
                                                    (^clojure.lang.IFn whilev
                                                      (.getV
                                                        ^datomic.impl.db.IDatum p1__18167#))))))
                                            (.seekAVET
                                              ^datomic.db.IDb db
                                              ^datomic.impl.db.IDatum d))))]
                              starte [(fn fn__18227
                                        ([d]
                                          (db/asserting-datum
                                            (unchecked-long ^java.lang.Number starte)
                                            (.getA ^datomic.impl.db.IDatum d)
                                            nil
                                            2305843009213693951)))
                                      (fn fn__18229
                                        ([d]
                                          (db/windowed
                                            db
                                            (fn fn__18230
                                              ([p1__18168#]
                                                (and
                                                  (=
                                                    const_attrid
                                                    (long
                                                      (.getA ^datomic.impl.db.IDatum p1__18168#)))
                                                  (or
                                                    (nil? whilee)
                                                    (^clojure.lang.IFn whilee
                                                      (long
                                                        (.getE
                                                          ^datomic.impl.db.IDatum p1__18168#)))))))
                                            (.seekAEVT
                                              ^datomic.db.IDb db
                                              ^datomic.impl.db.IDatum d))))]
                              (and whilev const_attr (.hasAVET ^datomic.db.Attribute const_attr)) [(fn 
                                                                                                     fn__18235
                                                                                                     ([d]
                                                                                                       (db/asserting-datum
                                                                                                         java.lang.Long/MIN_VALUE
                                                                                                         (.getA
                                                                                                           ^datomic.impl.db.IDatum d)
                                                                                                         nil
                                                                                                         2305843009213693951)))
                                                                                                   (fn 
                                                                                                     fn__18237
                                                                                                     ([d]
                                                                                                       (db/windowed
                                                                                                         db
                                                                                                         (fn 
                                                                                                           fn__18238
                                                                                                           ([p1__18169#]
                                                                                                             (and
                                                                                                               (=
                                                                                                                 const_attrid
                                                                                                                 (long
                                                                                                                   (.getA
                                                                                                                     ^datomic.impl.db.IDatum p1__18169#)))
                                                                                                               (^clojure.lang.IFn whilev
                                                                                                                 (.getV
                                                                                                                   ^datomic.impl.db.IDatum p1__18169#)))))
                                                                                                         (.seekAVET
                                                                                                           ^datomic.db.IDb db
                                                                                                           ^datomic.impl.db.IDatum d))))]
                              :else (do
                                      [(fn fn__18242
                                         ([d]
                                           (db/asserting-datum
                                             java.lang.Long/MIN_VALUE
                                             (.getA ^datomic.impl.db.IDatum d)
                                             nil
                                             2305843009213693951)))
                                       (fn fn__18244
                                         ([d]
                                           (db/windowed
                                             db
                                             (fn fn__18245
                                               ([p1__18170#]
                                                 (=
                                                   (long (.getA ^datomic.impl.db.IDatum d))
                                                   (long
                                                     (.getA ^datomic.impl.db.IDatum p1__18170#)))))
                                             (.seekAEVT
                                               ^datomic.db.IDb db
                                               ^datomic.impl.db.IDatum d))))]))
                            (if (aget ^"[Ljava.lang.Object;" bound 0)
                              [(fn fn__18248
                                 ([d]
                                   (db/asserting-datum
                                     (.getE ^datomic.impl.db.IDatum d)
                                     -1
                                     nil
                                     2305843009213693951)))
                               (fn fn__18250
                                 ([d]
                                   (let [ret (db/windowed
                                               db
                                               (fn fn__18251
                                                 ([p1__18171#]
                                                   (=
                                                     (long (.getE ^datomic.impl.db.IDatum d))
                                                     (long
                                                       (.getE
                                                         ^datomic.impl.db.IDatum p1__18171#)))))
                                               (.seekEAVT
                                                 ^datomic.db.IDb db
                                                 ^datomic.impl.db.IDatum d))]
                                     ret)))]
                              (if (aget ^"[Ljava.lang.Object;" bound 2)
                                [(fn fn__18254
                                   ([d]
                                     (db/asserting-datum
                                       java.lang.Long/MIN_VALUE
                                       -1
                                       (.getV ^datomic.impl.db.IDatum d)
                                       2305843009213693951)))
                                 (fn fn__18256
                                   ([d]
                                     (db/windowed
                                       db
                                       (fn fn__18257
                                         ([p1__18172#]
                                           (=
                                             (.getV ^datomic.impl.db.IDatum d)
                                             (.getV ^datomic.impl.db.IDatum p1__18172#))))
                                       (.seekRAET ^datomic.db.IDb db ^datomic.impl.db.IDatum d))))]
                                (do
                                  (when :else
                                    (throw
                                      (java.lang.Exception.
                                        "Insufficient bindings, will cause db scan")))
                                  nil))))
               prober (nth vec__18178 (unchecked-int 0) nil)
               probe (nth vec__18178 (unchecked-int 1) nil)
               project (fn project
                         ([d y]
                           (let [ret (object-array (java.lang.Integer/valueOf (int proj_count)))]
                             (dotimes [i (alength ^"[Ljava.lang.Object;" px_from)]
                               (aset
                                 ^"[Ljava.lang.Object;" ret
                                 (unchecked-int (aget ^"[Ljava.lang.Object;" px_to (int i)))
                                 (let [G__18261 (long
                                                  (aget ^"[Ljava.lang.Object;" px_from (int i)))]
                                   (case
                                     G__18261
                                     0
                                     (long (.getE ^datomic.impl.db.IDatum d))
                                     1
                                     (long (.getA ^datomic.impl.db.IDatum d))
                                     2
                                     (.getV ^datomic.impl.db.IDatum d)
                                     3
                                     (long (.getTx ^datomic.impl.db.IDatum d))
                                     4
                                     (.isAssertion ^datomic.impl.db.IDatum d)))))
                             (dotimes [i (alength ^"[Ljava.lang.Object;" py_from)]
                               (aset
                                 ^"[Ljava.lang.Object;" ret
                                 (unchecked-int (aget ^"[Ljava.lang.Object;" py_to (int i)))
                                 (nth
                                   y
                                   (unchecked-int (aget ^"[Ljava.lang.Object;" py_from (int i))))))
                             (tuple ret))))
               ident (fn ident
                       ([i v]
                         (cond
                           (or (< i 2) (and (= i 2) (.-isref ^datomic.datalog.DbRel dbrel))) (db/resolve-id
                                                                                               db
                                                                                               v)
                           (and (= i 2) (.-iskey ^datomic.datalog.DbRel dbrel)) (db/normalize-kw v)
                           :else (do v))))
               match? (fn match_QMARK_
                        ([x y]
                          (loop [i 0]
                            (if (< i (alength ^"[Ljava.lang.Object;" bindings))
                              (let [b (aget ^"[Ljava.lang.Object;" bindings (int i))]
                                (if b
                                  (if (=
                                        (^clojure.lang.IFn ident
                                          (long i)
                                          (nth x (unchecked-int i)))
                                        (^clojure.lang.IFn ident
                                          (long i)
                                          (nth y (unchecked-int ^java.lang.Number b))))
                                    (recur (inc i))
                                    false)
                                  (recur (inc i))))
                              true))))
               hashx (fn hashx
                       ([x]
                         (loop [i 0 h 0]
                           (if (< i (alength ^"[Ljava.lang.Object;" bound))
                             (let [b (aget ^"[Ljava.lang.Object;" bound (int i))]
                               (recur
                                 (inc i)
                                 (if b
                                   (clojure.lang.Util/hashCombine
                                     (unchecked-int h)
                                     (int
                                       (clojure.lang.Util/hash
                                         (^clojure.lang.IFn ident
                                           (long i)
                                           (nth x (unchecked-int i))))))
                                   h)))
                             (long h)))))
               hashy (fn hashy
                       ([y]
                         (loop [i 0 h 0]
                           (if (< i (alength ^"[Ljava.lang.Object;" bound))
                             (let [b (aget ^"[Ljava.lang.Object;" bound (int i))]
                               (recur
                                 (inc i)
                                 (if b
                                   (clojure.lang.Util/hashCombine
                                     (unchecked-int h)
                                     (int
                                       (clojure.lang.Util/hash
                                         (^clojure.lang.IFn ident
                                           (long i)
                                           (nth y (unchecked-int i))))))
                                   h)))
                             (long h)))))
               vec__18181 (let [ht (java.util.HashMap.)
                                ps (java.util.HashSet.)
                                yiter (iterator ys)]
                            (while
                              (.hasNext ^java.util.Iterator yiter)
                              (let [y (.next ^java.util.Iterator yiter)
                                    bindy (^clojure.lang.IFn bind y)
                                    h (^clojure.lang.IFn hashy bindy)
                                    vs (or
                                         (.get ^java.util.HashMap ht h)
                                         (let [vs (java.util.ArrayList. (unchecked-int 2))]
                                           (.put ^java.util.HashMap ht h vs)
                                           vs))]
                                (.add ^java.util.ArrayList vs y)
                                (.add ^java.util.HashSet ps (^clojure.lang.IFn prober bindy))))
                            [ht ps])
               ht (nth vec__18181 (unchecked-int 0) nil)
               probeset (nth vec__18181 (unchecked-int 1) nil)
               PAR 4
               probelists (let [cnt (count probeset)]
                            (if (> cnt 100)
                              (let [palist (java.util.ArrayList. ^java.util.Collection probeset)
                                    sz (if (= (rem cnt PAR) 0)
                                         (quot cnt PAR)
                                         (inc (quot cnt PAR)))]
                                (mapv
                                  (fn fn__18276
                                    ([p1__18173#]
                                      (.subList
                                        ^java.util.ArrayList palist
                                        (unchecked-int (* sz p1__18173#))
                                        (unchecked-int (min cnt (+ sz (* sz p1__18173#)))))))
                                  (range (long PAR))))
                              [probeset]))
               join (fn join
                      ([ps ret]
                        (let [pred (^clojure.lang.IFn predctor) piter (iterator ps)]
                          (while
                            (.hasNext ^java.util.Iterator piter)
                            (maybe-cancel)
                            (let [p (.next ^java.util.Iterator piter)]
                              (loop [diter (^clojure.lang.IFn probe p)]
                                (do
                                  (maybe-cancel)
                                  (when diter
                                    (let [d (.get ^datomic.iter.Iter diter)
                                          temp__5804__auto__ (.get
                                                               ^java.util.Map ht
                                                               (^clojure.lang.IFn hashx d))]
                                      (when temp__5804__auto__
                                        (let [ys temp__5804__auto__ yiter (iterator ys)]
                                          (while
                                            (.hasNext ^java.util.Iterator yiter)
                                            (let [y (.next ^java.util.Iterator yiter)]
                                              (when (^clojure.lang.IFn match? d y)
                                                (let [p (^clojure.lang.IFn project d y)]
                                                  (when (^clojure.lang.IFn pred p)
                                                    (.add ^java.util.Collection ret p))))))
                                          nil)))
                                    (recur (.next ^datomic.iter.Iter diter)))))))
                          nil)))
               ret (java.util.Collections/newSetFromMap
                     (java.util.concurrent.ConcurrentHashMap.
                       (unchecked-int 16)
                       0.75
                       (unchecked-int PAR)))]
           (qmapv (fn fn__18281 ([p1__18174#] (^clojure.lang.IFn join p1__18174# ret))) probelists)
           ret)))})
  (deftype
    FnRel
    [db f arity src? consts]
    datomic.datalog.IJoin
    (join-project-with
      [this ys join_map project_map_x project_map_y predctor]
      (if (zero? arity)
        (join-project-with
          (if src? (^clojure.lang.IFn f db) (^clojure.lang.IFn f))
          ys
          join_map
          project_map_x
          project_map_y
          predctor)
        (let [rel (java.util.HashSet.)
              px_from (to-array (keys project_map_x))
              px_to (to-array (vals project_map_x))
              py_from (to-array (keys project_map_y))
              py_to (to-array (vals project_map_y))
              proj_count (count (into (set px_to) py_to))
              project (fn project
                        ([x y]
                          (let [ret (object-array (java.lang.Integer/valueOf (int proj_count)))]
                            (dotimes [i (alength ^"[Ljava.lang.Object;" px_from)]
                              (aset
                                ^"[Ljava.lang.Object;" ret
                                (unchecked-int (aget ^"[Ljava.lang.Object;" px_to (int i)))
                                (nth
                                  x
                                  (unchecked-int (aget ^"[Ljava.lang.Object;" px_from (int i))))))
                            (dotimes [i (alength ^"[Ljava.lang.Object;" py_from)]
                              (aset
                                ^"[Ljava.lang.Object;" ret
                                (unchecked-int (aget ^"[Ljava.lang.Object;" py_to (int i)))
                                (nth
                                  y
                                  (unchecked-int (aget ^"[Ljava.lang.Object;" py_from (int i))))))
                            (tuple ret))))
              ks (seq (keys join_map))
              bindings (object-array (if ks (inc (apply max ks)) 0))
              _ (loop [seq_18296 (seq join_map) chunk_18297 nil count_18298 0 i_18299 0]
                  (if (< i_18299 count_18298)
                    (let [vec__18301 (.nth
                                       ^clojure.lang.Indexed chunk_18297
                                       (unchecked-int i_18299))
                          k (nth vec__18301 (unchecked-int 0) nil)
                          v (nth vec__18301 (unchecked-int 1) nil)]
                      (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                      (recur seq_18296 chunk_18297 count_18298 (inc i_18299)))
                    (let [temp__5804__auto__ (seq seq_18296)]
                      (when temp__5804__auto__
                        (let [seq_18296 temp__5804__auto__]
                          (if (chunked-seq? seq_18296)
                            (let [c__6065__auto__ (chunk-first seq_18296)]
                              (recur
                                (chunk-rest seq_18296)
                                c__6065__auto__
                                (count c__6065__auto__)
                                0))
                            (let [vec__18304 (first seq_18296)
                                  k (nth vec__18304 (unchecked-int 0) nil)
                                  v (nth vec__18304 (unchecked-int 1) nil)]
                              (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                              (recur (next seq_18296) nil 0 0))))))))
              match? (matchf bindings)
              PART 100
              proc (fn proc
                     ([ys]
                       (let [pred (^clojure.lang.IFn predctor)
                             yiter (iterator ys)
                             rel (java.util.ArrayList. (unchecked-int PART))
                             args (object-array arity)]
                         (maybe-cancel)
                         (while
                           (.hasNext ^java.util.Iterator yiter)
                           (let [y (.next ^java.util.Iterator yiter)]
                             (dotimes [i (long arity)]
                               (aset
                                 ^"[Ljava.lang.Object;" args
                                 (int i)
                                 (if (not (nil? (^clojure.lang.IFn consts (long i))))
                                   (^clojure.lang.IFn consts (long i))
                                   (nth y (unchecked-int (^clojure.lang.IFn join_map (long i)))))))
                             (let [xiter (iterator (if src? (apply f db args) (apply f args)))]
                               (while
                                 (.hasNext ^java.util.Iterator xiter)
                                 (let [x (.next ^java.util.Iterator xiter)]
                                   (when (^clojure.lang.IFn match? x y)
                                     (let [p (^clojure.lang.IFn project x y)]
                                       (when (^clojure.lang.IFn pred p)
                                         (.add ^java.util.ArrayList rel p))))))
                               nil)))
                         rel)))]
          (loop [seq_18313 (seq (qmapv proc (partv PART ys)))
                 chunk_18314 nil
                 count_18315 0
                 i_18316 0]
            (if (< i_18316 count_18315)
              (let [part (.nth ^clojure.lang.Indexed chunk_18314 (unchecked-int i_18316))]
                (.addAll ^java.util.AbstractCollection rel ^java.util.Collection part)
                (recur seq_18313 chunk_18314 count_18315 (inc i_18316)))
              (let [temp__5804__auto__ (seq seq_18313)]
                (when temp__5804__auto__
                  (let [seq_18313 temp__5804__auto__]
                    (if (chunked-seq? seq_18313)
                      (let [c__6065__auto__ (chunk-first seq_18313)]
                        (recur (chunk-rest seq_18313) c__6065__auto__ (count c__6065__auto__) 0))
                      (let [part (first seq_18313)]
                        (.addAll ^java.util.AbstractCollection rel ^java.util.Collection part)
                        (recur (next seq_18313) nil 0 0))))))))
          rel)))
    (join-project
      [this ys join_map project_map_x project_map_y predctor]
      (join-project-with this ys join_map project_map_x project_map_y predctor)))
  (clojure.core/import 'datomic.datalog.FnRel)
  (defn ->FnRel ([db f arity src? consts] (datomic.datalog.FnRel. db f arity src? consts)))
  (reset-meta!
    #'->FnRel
    (assoc
      {:arglists (clojure.core/list ['db 'f 'arity 'src? 'consts]), :column (int 1)}
      :name
      '->FnRel
      :ns
      *ns*))
  (defn fnrel
    ([db emap consts]
      (datomic.datalog.FnRel.
        db
        (:fn emap)
        (java.lang.Integer/valueOf (int (count (:argvars emap))))
        (:needs-source emap)
        consts)))
  (reset-meta!
    #'fnrel
    (assoc
      {:arglists (clojure.core/list ['db 'emap 'consts]), :column (int 1)}
      :name
      'fnrel
      :ns
      *ns*))
  (defn create-join-maps
    ([xbinds ybinds zbinds]
      (let [xb (zipmap xbinds (range))
            yb (zipmap ybinds (range))
            zb (zipmap zbinds (range))
            m (fn m
                ([ib jb jbinds]
                  (reduce
                    (fn fn__18327
                      ([p1__18325# p2__18324#]
                        (if (^clojure.lang.IFn ib p2__18324#)
                          (assoc
                            p1__18325#
                            (^clojure.lang.IFn ib p2__18324#)
                            (^clojure.lang.IFn jb p2__18324#))
                          p1__18325#)))
                    {}
                    jbinds)))
            join_map (^clojure.lang.IFn m xb yb ybinds)
            proj_x (^clojure.lang.IFn m xb zb zbinds)
            proj_y (^clojure.lang.IFn m yb zb zbinds)]
        [join_map proj_x proj_y])))
  (reset-meta!
    #'create-join-maps
    (assoc
      {:arglists (clojure.core/list ['xbinds 'ybinds 'zbinds]), :column (int 1)}
      :name
      'create-join-maps
      :ns
      *ns*))
  (defn project
    ([xs xbinds ybinds]
      (if (= xbinds ybinds)
        xs
        (let [xb (zipmap xbinds (range)) yb (zipmap (range) ybinds) ret (java.util.HashSet.)]
          (loop [seq_18331 (seq xs) chunk_18332 nil count_18333 0 i_18334 0]
            (if (< i_18334 count_18333)
              (let [x (.nth ^clojure.lang.Indexed chunk_18332 (unchecked-int i_18334))]
                (let [tos (object-array (java.lang.Integer/valueOf (int (count ybinds))))]
                  (dotimes [i (count ybinds)]
                    (aset
                      ^"[Ljava.lang.Object;" tos
                      (int i)
                      (nth
                        x
                        (unchecked-int (^clojure.lang.IFn xb (^clojure.lang.IFn yb (long i)))))))
                  (.add ^java.util.HashSet ret (tuple tos)))
                (recur seq_18331 chunk_18332 count_18333 (inc i_18334)))
              (let [temp__5804__auto__ (seq seq_18331)]
                (when temp__5804__auto__
                  (let [seq_18331 temp__5804__auto__]
                    (if (chunked-seq? seq_18331)
                      (let [c__6065__auto__ (chunk-first seq_18331)]
                        (recur (chunk-rest seq_18331) c__6065__auto__ (count c__6065__auto__) 0))
                      (let [x (first seq_18331)]
                        (let [tos (object-array (java.lang.Integer/valueOf (int (count ybinds))))]
                          (dotimes [i (count ybinds)]
                            (aset
                              ^"[Ljava.lang.Object;" tos
                              (int i)
                              (nth
                                x
                                (unchecked-int
                                  (^clojure.lang.IFn xb (^clojure.lang.IFn yb (long i)))))))
                          (.add ^java.util.HashSet ret (tuple tos)))
                        (recur (next seq_18331) nil 0 0))))))))
          ret))))
  (reset-meta!
    #'project
    (assoc
      {:arglists (clojure.core/list ['xs 'xbinds 'ybinds]), :column (int 1)}
      :name
      'project
      :ns
      *ns*))
  (deftype
    PredRel
    [db f arity src? consts]
    datomic.datalog.IJoin
    (join-project-with
      [this ys join_map project_map_x project_map_y _]
      (let [rel (java.util.HashSet.)
            args (object-array arity)
            yiter (iterator ys)
            n (count project_map_y)
            invpmy (object-array (java.lang.Integer/valueOf (int n)))]
        (loop [seq_18341 (seq project_map_y) chunk_18342 nil count_18343 0 i_18344 0]
          (if (< i_18344 count_18343)
            (let [vec__18345 (.nth ^clojure.lang.Indexed chunk_18342 (unchecked-int i_18344))
                  y (nth vec__18345 (unchecked-int 0) nil)
                  i (nth vec__18345 (unchecked-int 1) nil)]
              (aset ^"[Ljava.lang.Object;" invpmy (unchecked-int i) y)
              (recur seq_18341 chunk_18342 count_18343 (inc i_18344)))
            (let [temp__5804__auto__ (seq seq_18341)]
              (when temp__5804__auto__
                (let [seq_18341 temp__5804__auto__]
                  (if (chunked-seq? seq_18341)
                    (let [c__6065__auto__ (chunk-first seq_18341)]
                      (recur (chunk-rest seq_18341) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [vec__18348 (first seq_18341)
                          y (nth vec__18348 (unchecked-int 0) nil)
                          i (nth vec__18348 (unchecked-int 1) nil)]
                      (aset ^"[Ljava.lang.Object;" invpmy (unchecked-int i) y)
                      (recur (next seq_18341) nil 0 0))))))))
        (while
          (.hasNext ^java.util.Iterator yiter)
          (maybe-cancel)
          (let [y (.next ^java.util.Iterator yiter)]
            (dotimes [i (long arity)]
              (aset
                ^"[Ljava.lang.Object;" args
                (int i)
                (if (not (nil? (^clojure.lang.IFn consts (long i))))
                  (^clojure.lang.IFn consts (long i))
                  (nth y (unchecked-int (^clojure.lang.IFn join_map (long i)))))))
            (when (if src? (apply f db args) (apply f args))
              (let [tos (object-array (java.lang.Integer/valueOf (int n)))]
                (dotimes [i n]
                  (aset
                    ^"[Ljava.lang.Object;" tos
                    (int i)
                    (nth y (unchecked-int (aget ^"[Ljava.lang.Object;" invpmy (int i))))))
                (.add ^java.util.HashSet rel (tuple tos))))))
        rel))
    (join-project
      [this ys join_map project_map_x project_map_y predctor]
      (join-project-with this ys join_map project_map_x project_map_y predctor)))
  (clojure.core/import 'datomic.datalog.PredRel)
  (defn ->PredRel ([db f arity src? consts] (datomic.datalog.PredRel. db f arity src? consts)))
  (reset-meta!
    #'->PredRel
    (assoc
      {:arglists (clojure.core/list ['db 'f 'arity 'src? 'consts]), :column (int 1)}
      :name
      '->PredRel
      :ns
      *ns*))
  (defn predrel
    ([db emap consts]
      (datomic.datalog.PredRel.
        db
        (:fn emap)
        (java.lang.Integer/valueOf (int (count (:argvars emap))))
        (:needs-source emap)
        consts)))
  (reset-meta!
    #'predrel
    (assoc
      {:arglists (clojure.core/list ['db 'emap 'consts]), :column (int 1)}
      :name
      'predrel
      :ns
      *ns*))
  (defn variable? ([x] (= \? (first (config/sym-name x)))))
  (reset-meta!
    #'variable?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'variable? :ns *ns*))
  (defn source? ([x] (= \$ (first (config/sym-name x)))))
  (reset-meta!
    #'source?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'source? :ns *ns*))
  (defn blank? ([x] (= '_ x)))
  (reset-meta!
    #'blank?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'blank? :ns *ns*))
  (defn variable-or-blank? ([x] (or (variable? x) (blank? x))))
  (reset-meta!
    #'variable-or-blank?
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'variable-or-blank?
      :ns
      *ns*))
  (defn attr?
    ([db x]
      (let [attrid (db/resolve-id db x)]
        (when attrid
          (instance? datomic.db.Attribute (.elementAt ^datomic.db.IDbImpl db attrid))))))
  (reset-meta!
    #'attr?
    (assoc {:arglists (clojure.core/list ['db 'x]), :column (int 1)} :name 'attr? :ns *ns*))
  (defn extensional? ([rules pred] (not (contains? rules pred))))
  (reset-meta!
    #'extensional?
    (assoc
      {:arglists (clojure.core/list ['rules 'pred]), :column (int 1)}
      :name
      'extensional?
      :ns
      *ns*))
  (defn adorned-pred
    ([query bindset]
      [(first query)
       (vec
         (map (fn fn__18368 ([p1__18367#] (not (contains? bindset p1__18367#)))) (rest query)))])
    ([query] [(first query) (vec (map variable? (rest query)))]))
  (reset-meta!
    #'adorned-pred
    (assoc
      {:arglists (clojure.core/list ['query] ['query 'bindset]), :column (int 1)}
      :name
      'adorned-pred
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.datalog" "eval-query") {:declared true, :column (int 1)})
  (defn not-join-clause? ([c] (and (instance? java.util.List c) (= 'not-join (first c)))))
  (reset-meta!
    #'not-join-clause?
    (assoc {:arglists (clojure.core/list ['c]), :column (int 1)} :name 'not-join-clause? :ns *ns*))
  (defn free-var? ([x] (variable? x)))
  (reset-meta!
    #'free-var?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'free-var? :ns *ns*))
  (defn free-vars
    ([c] (cond (not-join-clause? c) (second c) :else (do (filter free-var? (flatten c))))))
  (reset-meta!
    #'free-vars
    (assoc {:arglists (clojure.core/list ['c]), :column (int 1)} :name 'free-vars :ns *ns*))
  (defn unifying-vars
    ([p__18376]
      (let [vec__18377 p__18376
            seq__18378 (seq vec__18377)
            first__18379 (first seq__18378)
            seq__18378 (next seq__18378)
            p first__18379
            cs seq__18378
            c vec__18377
            vec__18380 (if (source? p) cs c)
            seq__18381 (seq vec__18380)
            first__18382 (first seq__18381)
            seq__18381 (next seq__18381)
            p first__18382
            cs seq__18381
            c vec__18380]
        (cond
          (#{'not-join 'or-join} p) (flatten (first cs))
          (#{'and 'not} p) (mapcat unifying-vars cs)
          (= 'or p) (let [uvs (mapv
                                (fn fn__18383 ([p1__18375#] (set (unifying-vars p1__18375#))))
                                cs)]
                      (when-not (apply = uvs)
                        (throw
                          (java.lang.AssertionError.
                            (str
                              "Assert failed: "
                              (str "All clauses in 'or' must use same set of vars, had " uvs)
                              "\n"
                              (pr-str (clojure.core/list 'apply '= 'uvs))))))
                      (first uvs))
          :else (do (filter variable? (flatten c)))))))
  (reset-meta!
    #'unifying-vars
    (assoc
      {:arglists (clojure.core/list [['p '& 'cs :as 'c]]), :column (int 1)}
      :name
      'unifying-vars
      :ns
      *ns*))
  (defn unifying-var-set ([c] (set (unifying-vars c))))
  (reset-meta!
    #'unifying-var-set
    (assoc {:arglists (clojure.core/list ['c]), :column (int 1)} :name 'unifying-var-set :ns *ns*))
  (defn used-srcs
    ([srcs c]
      (cond
        (not-join-clause? c) (conj (into #{} (mapcat (partial used-srcs srcs) (nnext c))) '$)
        :else (do (filter srcs (flatten c))))))
  (reset-meta!
    #'used-srcs
    (assoc {:arglists (clojure.core/list ['srcs 'c]), :column (int 1)} :name 'used-srcs :ns *ns*))
  (defn sched-in-order
    ([srcs prog p__18399 init_binds]
      (let [vec__18400 p__18399
            seq__18401 (seq vec__18400)
            first__18402 (first seq__18401)
            seq__18401 (next seq__18401)
            vec__18403 first__18402
            seq__18404 (seq vec__18403)
            first__18405 (first seq__18404)
            seq__18404 (next seq__18404)
            hpred first__18405
            hargs seq__18404
            body seq__18401
            rule vec__18400
            src (fn src ([p1__18388#] (or (:tag (meta p1__18388#)) '$)))
            pack (fn pack ([c] [(^clojure.lang.IFn src c) c]))
            unpack (fn unpack ([p1__18389#] (nth p1__18389# (unchecked-int 1))))
            cargs (fn cargs
                    ([clause]
                      (cond
                        (map? clause) (:argvars clause)
                        (not-join-clause? clause) (free-vars clause)
                        (extensional? prog (first clause)) clause
                        :else (do (next clause)))))
            extdb (fn extdb
                    ([p__18424]
                      (let [vec__18426 p__18424
                            src (nth vec__18426 (unchecked-int 0) nil)
                            c (nth vec__18426 (unchecked-int 1) nil)
                            temp__5804__auto__ (and
                                                 (not (map? c))
                                                 (not (not-join-clause? c))
                                                 (extensional? prog (first c))
                                                 (^clojure.lang.IFn srcs src))]
                        (when temp__5804__auto__
                          (let [db temp__5804__auto__] (when (instance? datomic.db.IDb db) db))))))
            cbinds (fn cbinds
                     ([clause]
                       (cond
                         (map? clause) (concat (:argvars clause) (:binds clause))
                         (not-join-clause? clause) (free-vars clause)
                         :else (do (filter variable? clause)))))
            sv_clause? (fn sv_clause_QMARK_
                         ([p1__18390#]
                           (and
                             (map? p1__18390#)
                             (nil? (:argvars p1__18390#))
                             (#{:tuple :scalar} (:bind-type p1__18390#)))))
            in_clause? (fn in_clause_QMARK_
                         ([p1__18391#]
                           (and
                             (map? p1__18391#)
                             (nil? (:argvars p1__18391#))
                             (#{:rel :list} (:bind-type p1__18391#))
                             (.startsWith (name (^clojure.lang.IFn src p1__18391#)) "$__in"))))
            delay_ins (fn delay_ins
                        ([cs]
                          (let [vec__18446 (split-with in_clause? cs)
                                ins (nth vec__18446 (unchecked-int 0) nil)
                                cs (nth vec__18446 (unchecked-int 1) nil)
                                m (reduce
                                    (fn fn__18449
                                      ([m in]
                                        (let [bset (set (^clojure.lang.IFn cbinds in))
                                              uc (first
                                                   (filter
                                                     (fn fn__18450
                                                       ([p1__18392#]
                                                         (some
                                                           bset
                                                           (^clojure.lang.IFn cargs p1__18392#))))
                                                     cs))]
                                          (update-in m [uc] conj in))))
                                    {}
                                    ins)]
                            (concat
                              (reduce
                                (fn fn__18453
                                  ([ret c]
                                    (let [vec__18454 (find m c)
                                          k (nth vec__18454 (unchecked-int 0) nil)
                                          vs (nth vec__18454 (unchecked-int 1) nil)]
                                      (if (identical? k c)
                                        (into (conj ret (first vs) c) (next vs))
                                        (conj ret c)))))
                                []
                                cs)
                              (get m nil)))))
            pred? (fn pred_QMARK_
                    ([p1__18393#] (and (map? p1__18393#) (nil? (:binds p1__18393#)))))
            vec__18406 (common/split-filter pred? body)
            preds (nth vec__18406 (unchecked-int 0) nil)
            npreds (nth vec__18406 (unchecked-int 1) nil)
            vec__18409 (common/split-filter not-join-clause? npreds)
            njcs (nth vec__18409 (unchecked-int 0) nil)
            npreds (nth vec__18409 (unchecked-int 1) nil)
            vec__18412 (common/split-filter sv_clause? npreds)
            svs (nth vec__18412 (unchecked-int 0) nil)
            npreds (nth vec__18412 (unchecked-int 1) nil)
            body (concat svs preds njcs (^clojure.lang.IFn delay_ins npreds))
            reqcnt (fn reqcnt
                     ([clause]
                       (when (contains? prog (first clause))
                         (let [vec__18463 (find prog (first clause))
                               p (nth vec__18463 (unchecked-int 0) nil)]
                           (:reqcnt (meta p))))))
            underbound? (fn underbound_QMARK_
                          ([n bindings pc]
                            (let [clause (^clojure.lang.IFn unpack pc)
                                  args (^clojure.lang.IFn cargs clause)]
                              (cond
                                (or (not-join-clause? clause) (map? clause)) (some
                                                                               (fn 
                                                                                 fn__18468
                                                                                 ([p1__18394#]
                                                                                   (and
                                                                                     (variable?
                                                                                       p1__18394#)
                                                                                     (not
                                                                                       (contains?
                                                                                         bindings
                                                                                         p1__18394#)))))
                                                                               args)
                                (contains? prog (first clause)) (or
                                                                  (let 
                                                                    [rcnt
                                                                     (^clojure.lang.IFn reqcnt
                                                                       clause)]
                                                                    (and
                                                                      rcnt
                                                                      (not
                                                                        (every?
                                                                          (fn 
                                                                            fn__18471
                                                                            ([p1__18395#]
                                                                              (contains?
                                                                                bindings
                                                                                p1__18395#)))
                                                                          (take rcnt args)))))
                                                                  (and
                                                                    (= hpred (first clause))
                                                                    (not
                                                                      (some
                                                                        (fn 
                                                                          fn__18473
                                                                          ([p1__18396#]
                                                                            (contains?
                                                                              bindings
                                                                              p1__18396#)))
                                                                        (filter variable? args)))))
                                :else (do
                                        (let [temp__5804__auto__ (^clojure.lang.IFn extdb pc)]
                                          (when temp__5804__auto__
                                            (let [db temp__5804__auto__]
                                              (not
                                                (some
                                                  (fn fn__18475
                                                    ([p1__18397#]
                                                      (or
                                                        (not (variable? p1__18397#))
                                                        (contains? bindings p1__18397#))))
                                                  (take n clause)))))))))))
            clauses (loop [clauses [] bindings (set init_binds) remclauses (map pack body)]
                      (if (empty? remclauses)
                        clauses
                        (let [vec__18485 (split-with (partial underbound? 2 bindings) remclauses)
                              skip (nth vec__18485 (unchecked-int 0) nil)
                              ready (nth vec__18485 (unchecked-int 1) nil)
                              vec__18488 (if (empty? ready)
                                           (split-with (partial underbound? 3 bindings) remclauses)
                                           [skip ready])
                              skip (nth vec__18488 (unchecked-int 0) nil)
                              ready (nth vec__18488 (unchecked-int 1) nil)
                              _ (when (empty? ready)
                                  (let [c (^clojure.lang.IFn unpack (first skip))]
                                    (error/arg
                                      :db.error/insufficient-binding
                                      (cond
                                        (not-join-clause? c) (str
                                                               (vec
                                                                 (remove bindings (free-vars c)))
                                                               " not bound in not clause: "
                                                               c)
                                        (^clojure.lang.IFn reqcnt c) (str
                                                                       (vec
                                                                         (remove
                                                                           bindings
                                                                           (take
                                                                             (^clojure.lang.IFn reqcnt
                                                                               c)
                                                                             (next c))))
                                                                       " not bound in clause: "
                                                                       c)
                                        (map? c) (str
                                                   (vec (remove bindings (:argvars c)))
                                                   " not bound in expression clause: "
                                                   (:clause c))
                                        :else (do
                                                (str
                                                  "Insufficient binding of db clause: "
                                                  c
                                                  " would cause full scan"))))))
                              pc (first ready)
                              c (^clojure.lang.IFn unpack pc)
                              next_bindings (into bindings (^clojure.lang.IFn cbinds c))]
                          (recur (conj clauses c) next_bindings (concat skip (rest ready))))))
            blist (map
                    (fn fn__18492 ([p1__18398#] (set (^clojure.lang.IFn cbinds p1__18398#))))
                    (cons init_binds (conj clauses hargs)))
            bup (next (reductions set/union blist))
            bdown (nnext (reverse (reductions set/union (reverse blist))))
            binds (-> (comp vec set/intersection)
                   (map bup bdown)
                   (vec)
                   (pop)
                   (conj (vec (filter variable? hargs))))
            ret (partition 2 (interleave clauses binds))]
        (query-stats/acc-with-phase-stats! :sched ret)
        ret)))
  (reset-meta!
    #'sched-in-order
    (assoc
      {:arglists
       (clojure.core/list ['srcs 'prog [['hpred '& 'hargs] '& 'body :as 'rule] 'init-binds]),
       :column (int 1)}
      :name
      'sched-in-order
      :ns
      *ns*))
  (defn push-preds
    ([srcs sched]
      (let [pred? (fn pred_QMARK_
                    ([p1__18495#] (and (map? p1__18495#) (nil? (:binds p1__18495#)))))
            ctor (fn ctor
                   ([inbinds p__18502]
                     (let [map__18504 p__18502
                           map__18504 (if (seq? map__18504)
                                        (if (next map__18504)
                                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                            (to-array map__18504))
                                          (if (seq map__18504) (first map__18504) {}))
                                        map__18504)
                           clause map__18504
                           f (get map__18504 :fn)
                           argvars (get map__18504 :argvars)
                           needs_source (get map__18504 :needs-source)
                           binds (get map__18504 :binds)
                           params (concat argvars binds)
                           arity (count params)
                           consts (vec
                                    (map
                                      (fn fn__18508
                                        ([p1__18496#]
                                          (when-not (variable-or-blank? p1__18496#) p1__18496#)))
                                      params))
                           vec__18505 (create-join-maps params inbinds [])
                           join_map (nth vec__18505 (unchecked-int 0) nil)
                           _ (nth vec__18505 (unchecked-int 1) nil)
                           _ (nth vec__18505 (unchecked-int 2) nil)
                           src (when needs_source (get srcs (or (:tag (meta clause)) '$)))]
                       (query-stats/acc-with-clause-stats! :preds clause)
                       (fn fn__18510
                         ([]
                           (let [args (object-array (java.lang.Integer/valueOf (int arity)))]
                             (fn fn__18511
                               ([y]
                                 (dotimes [i arity]
                                   (aset
                                     ^"[Ljava.lang.Object;" args
                                     (int i)
                                     (if (not (nil? (^clojure.lang.IFn consts (long i))))
                                       (^clojure.lang.IFn consts (long i))
                                       (nth
                                         y
                                         (unchecked-int (^clojure.lang.IFn join_map (long i)))))))
                                 (if needs_source (apply f src args) (apply f args))))))))))
            ret (let [ret []
                      G__18523 (seq sched)
                      vec__18524 G__18523
                      seq__18525 (seq vec__18524)
                      first__18526 (first seq__18525)
                      seq__18525 (next seq__18525)
                      vec__18527 first__18526
                      clause (nth vec__18527 (unchecked-int 0) nil)
                      outbinds (nth vec__18527 (unchecked-int 1) nil)
                      clauses seq__18525
                      sched vec__18524]
                  (loop [ret ret G__18523 G__18523]
                    (let [ret ret
                          vec__18531 G__18523
                          seq__18532 (seq vec__18531)
                          first__18533 (first seq__18532)
                          seq__18532 (next seq__18532)
                          vec__18534 first__18533
                          clause (nth vec__18534 (unchecked-int 0) nil)
                          outbinds (nth vec__18534 (unchecked-int 1) nil)
                          clauses seq__18532
                          sched vec__18531]
                      (if sched
                        (if (^clojure.lang.IFn pred? clause)
                          (recur (conj ret [clause outbinds nil]) (seq clauses))
                          (let [vec__18537 (split-with (comp pred? first) clauses)
                                preds (nth vec__18537 (unchecked-int 0) nil)
                                clauses (nth vec__18537 (unchecked-int 1) nil)]
                            (recur
                              (conj
                                ret
                                [clause
                                 outbinds
                                 (if (empty? preds)
                                   truep
                                   (let [ctors (map
                                                 (fn fn__18540
                                                   ([p1__18497#]
                                                     (^clojure.lang.IFn ctor
                                                       outbinds
                                                       (first p1__18497#))))
                                                 preds)]
                                     (fn fn__18542
                                       ([]
                                         (apply
                                           every-pred
                                           (map
                                             (fn fn__18543
                                               ([p1__18498#] (^clojure.lang.IFn p1__18498#)))
                                             ctors))))))])
                              (seq clauses))))
                        ret))))]
        ret)))
  (reset-meta!
    #'push-preds
    (assoc
      {:arglists (clojure.core/list ['srcs 'sched]), :column (int 1)}
      :name
      'push-preds
      :ns
      *ns*))
  (defn recursive?
    ([prog pred seen]
      (if (or (map? pred) (extensional? prog pred))
        false
        (or
          (contains? seen pred)
          (let [seen (conj seen pred) rules (get prog pred)]
            (some
              (fn fn__18552 ([p1__18551#] (recursive? prog p1__18551# seen)))
              (map first (remove map? (mapcat rest rules))))))))
    ([prog pred] (recursive? prog pred #{})))
  (reset-meta!
    #'recursive?
    (assoc
      {:arglists (clojure.core/list ['prog 'pred] ['prog 'pred 'seen]), :column (int 1)}
      :name
      'recursive?
      :ns
      *ns*))
  (defn rules? ([x] (= \% (first (config/sym-name x)))))
  (reset-meta!
    #'rules?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'rules? :ns *ns*))
  (defn scalar->rel ([x] (if (nil? x) [] [[x]])))
  (reset-meta!
    #'scalar->rel
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'scalar->rel :ns *ns*))
  (defn tuple->rel ([x] (if (nil? x) [] [x])))
  (reset-meta!
    #'tuple->rel
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'tuple->rel :ns *ns*))
  (defn compile-expr-clause
    ([sources vars expr bind_type binds]
      (let [params (vec (concat sources vars))
            projection (keep-indexed
                         (fn fn__18562
                           ([p1__18561# p2__18560#] (when-not (blank? p2__18560#) p1__18561#)))
                         binds)
            retlen (+ (count vars) (count projection))
            gret (gensym)
            term (gensym)
            fexpr (if bind_type
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/fn)
                        (clojure.core/list params)
                        (clojure.core/list
                          (seq
                            (concat
                              (clojure.core/list 'clojure.core/mapv)
                              (clojure.core/list
                                (seq
                                  (concat
                                    (clojure.core/list 'clojure.core/fn)
                                    (clojure.core/list
                                      (apply vector (seq (concat (clojure.core/list term)))))
                                    (clojure.core/list
                                      (seq
                                        (concat
                                          (clojure.core/list 'clojure.core/let)
                                          (clojure.core/list
                                            (apply
                                              vector
                                              (seq
                                                (concat
                                                  (clojure.core/list gret)
                                                  (clojure.core/list
                                                    (seq
                                                      (concat
                                                        (clojure.core/list
                                                          'clojure.core/object-array)
                                                        (clojure.core/list (long retlen)))))))))
                                          (let [iter__6373__auto__ (fn 
                                                                     iter__18564
                                                                     ([s__18565]
                                                                       (lazy-seq
                                                                         (let 
                                                                           [s__18565 s__18565
                                                                            temp__5804__auto__
                                                                            (seq s__18565)]
                                                                           (when
                                                                             temp__5804__auto__
                                                                             (let 
                                                                               [s__18565
                                                                                temp__5804__auto__]
                                                                               (if
                                                                                 (chunked-seq?
                                                                                   s__18565)
                                                                                 (let 
                                                                                   [c__6371__auto__
                                                                                    (chunk-first
                                                                                      s__18565)
                                                                                    size__6372__auto__
                                                                                    (count
                                                                                      c__6371__auto__)
                                                                                    b__18567
                                                                                    (chunk-buffer
                                                                                      (java.lang.Integer/valueOf
                                                                                        (int
                                                                                          size__6372__auto__)))]
                                                                                   (if
                                                                                     (loop 
                                                                                       [i__18566 0]
                                                                                       (if
                                                                                         (<
                                                                                           i__18566
                                                                                           size__6372__auto__)
                                                                                         (let 
                                                                                           [i
                                                                                            (.nth
                                                                                              ^clojure.lang.Indexed c__6371__auto__
                                                                                              (unchecked-int
                                                                                                i__18566))]
                                                                                           (chunk-append
                                                                                             b__18567
                                                                                             (seq
                                                                                               (concat
                                                                                                 (clojure.core/list
                                                                                                   'clojure.core/aset)
                                                                                                 (clojure.core/list
                                                                                                   gret)
                                                                                                 (clojure.core/list
                                                                                                   i)
                                                                                                 (clojure.core/list
                                                                                                   (nth
                                                                                                     vars
                                                                                                     (unchecked-int
                                                                                                       ^java.lang.Number i))))))
                                                                                           (recur
                                                                                             (inc
                                                                                               i__18566)))
                                                                                         true))
                                                                                     (chunk-cons
                                                                                       (chunk
                                                                                         b__18567)
                                                                                       (^clojure.lang.IFn iter__18564
                                                                                         (chunk-rest
                                                                                           s__18565)))
                                                                                     (chunk-cons
                                                                                       (chunk
                                                                                         b__18567)
                                                                                       nil)))
                                                                                 (let 
                                                                                   [i
                                                                                    (first
                                                                                      s__18565)]
                                                                                   (cons
                                                                                     (seq
                                                                                       (concat
                                                                                         (clojure.core/list
                                                                                           'clojure.core/aset)
                                                                                         (clojure.core/list
                                                                                           gret)
                                                                                         (clojure.core/list
                                                                                           i)
                                                                                         (clojure.core/list
                                                                                           (nth
                                                                                             vars
                                                                                             (unchecked-int
                                                                                               ^java.lang.Number i)))))
                                                                                     (^clojure.lang.IFn iter__18564
                                                                                       (rest
                                                                                         s__18565)))))))))))]
                                            (^clojure.lang.IFn iter__6373__auto__
                                              (range
                                                (java.lang.Integer/valueOf (int (count vars))))))
                                          (map-indexed
                                            (fn fn__18577
                                              ([i proj]
                                                (seq
                                                  (concat
                                                    (clojure.core/list 'clojure.core/aset)
                                                    (clojure.core/list gret)
                                                    (clojure.core/list (+ i (count vars)))
                                                    (clojure.core/list
                                                      (seq
                                                        (concat
                                                          (clojure.core/list 'clojure.core/nth)
                                                          (clojure.core/list term)
                                                          (clojure.core/list proj))))))))
                                            projection)
                                          (clojure.core/list
                                            (seq
                                              (concat
                                                (clojure.core/list 'datomic.datalog/tuple)
                                                (clojure.core/list gret))))))))))
                              (clojure.core/list
                                (let [G__18579 bind_type]
                                  (case
                                    G__18579
                                    :scalar
                                    (seq
                                      (concat
                                        (clojure.core/list 'datomic.datalog/scalar->rel)
                                        (clojure.core/list expr)))
                                    :tuple
                                    (seq
                                      (concat
                                        (clojure.core/list 'datomic.datalog/tuple->rel)
                                        (clojure.core/list expr)))
                                    :list
                                    (seq
                                      (concat
                                        (clojure.core/list 'clojure.core/mapv)
                                        (clojure.core/list 'clojure.core/vector)
                                        (clojure.core/list expr)))
                                    :rel
                                    expr))))))))
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/fn)
                        (clojure.core/list params)
                        (clojure.core/list expr))))]
        (binding [*ns* (find-ns 'datomic.extensions) *warn-on-reflection* true] (eval fexpr)))))
  (reset-meta!
    #'compile-expr-clause
    (assoc
      {:arglists (clojure.core/list ['sources 'vars 'expr 'bind-type 'binds]), :column (int 1)}
      :name
      'compile-expr-clause
      :ns
      *ns*))
  (defn binding-type
    ([binds]
      (when-not (nil? binds)
        (cond
          (symbol? binds) :scalar
          (instance? java.util.List binds) (cond
                                             (instance? java.util.List (first binds)) :rel
                                             (= '... (second binds)) :list
                                             :else (do :tuple))
          :else (do
                  (error/arg
                    :db.error/not-a-binding-form
                    (str "Invalid binding form: " binds)))))))
  (reset-meta!
    #'binding-type
    (assoc {:arglists (clojure.core/list ['binds]), :column (int 1)} :name 'binding-type :ns *ns*))
  (defn expr-clause
    ([p__18586]
      (let [vec__18587 p__18586
            call (nth vec__18587 (unchecked-int 0) nil)
            binds (nth vec__18587 (unchecked-int 1) nil)
            xtra (nth vec__18587 (unchecked-int 2) nil)
            clause vec__18587]
        (if (instance? java.util.List call)
          (let [vec__18590 (list* call)
                seq__18591 (seq vec__18590)
                first__18592 (first seq__18591)
                seq__18591 (next seq__18591)
                f first__18592
                body seq__18591
                expr vec__18590
                vars (seq (set (filter variable? body)))
                sources (seq (set (filter source? body)))
                _ (when-not (or (not= f 'ground) (nil? vars))
                    (throw
                      (java.lang.AssertionError.
                        (str
                          "Assert failed: "
                          "Can't have variable in ground expression"
                          "\n"
                          (pr-str
                            (clojure.core/list
                              'or
                              (clojure.core/list 'not= 'f (clojure.core/list 'quote 'ground))
                              (clojure.core/list 'nil? 'vars))))))
                    nil)
                bind_type (binding-type binds)
                binds (and
                        binds
                        (vec
                          (let [G__18593 bind_type]
                            (case
                              G__18593
                              :scalar
                              [binds]
                              :tuple
                              binds
                              :list
                              [(first binds)]
                              :rel
                              (first binds)))))
                has_blanks? (and binds (some blank? binds))
                needs_source (not (nil? sources))
                f (if (and (= f 'ground) (nil? vars) needs_source (not has_blanks?))
                    (let [G__18594 bind_type]
                      (case
                        G__18594
                        :scalar
                        (fn fn__18595 ([p1__18583#] (vector (vector p1__18583#))))
                        :tuple
                        (fn fn__18597 ([p1__18585#] (vector p1__18585#)))
                        :list
                        (fn fn__18599 ([p1__18584#] (mapv vector p1__18584#)))
                        :rel
                        identity))
                    (compile-expr-clause sources vars expr bind_type binds))
                binds (if has_blanks? (vec (remove blank? binds)) binds)]
            (when-not (< (count sources) 2)
              (throw
                (java.lang.AssertionError.
                  (str
                    "Assert failed: "
                    "Can't have more than one data source in expression"
                    "\n"
                    (pr-str (clojure.core/list '< (clojure.core/list 'count 'sources) 2))))))
            (when-not (nil? xtra)
              (throw
                (java.lang.AssertionError.
                  (str
                    "Assert failed: "
                    (str "Can't have anything after binding expression: " xtra)
                    "\n"
                    (pr-str (clojure.core/list 'nil? 'xtra))))))
            (let [ret {:argvars vars,
                       :fn f,
                       :clause clause,
                       :binds binds,
                       :bind-type bind_type,
                       :needs-source needs_source}
                  ret (if sources (with-meta ret {:tag (first sources)}) ret)]
              ret))
          clause))))
  (reset-meta!
    #'expr-clause
    (assoc
      {:arglists (clojure.core/list [['call 'binds 'xtra :as 'clause]]), :column (int 1)}
      :name
      'expr-clause
      :ns
      *ns*))
  (defn lift-consts-from-preds
    ([clauses]
      (let [vec__18608 (reduce
                         (fn fn__18613
                           ([p__18611 p__18612]
                             (let [vec__18614 p__18611
                                   m (nth vec__18614 (unchecked-int 0) nil)
                                   cs (nth vec__18614 (unchecked-int 1) nil)
                                   vec__18617 p__18612
                                   seq__18618 (seq vec__18617)
                                   first__18619 (first seq__18618)
                                   seq__18618 (next seq__18618)
                                   pred first__18619
                                   args seq__18618
                                   c vec__18617]
                               (if (and
                                     (symbol? pred)
                                     (not= 'not-join pred)
                                     (not (variable-or-blank? pred))
                                     (not (.startsWith (name pred) "$"))
                                     (not (every? variable-or-blank? args)))
                                 (let [vec__18620 (reduce
                                                    (fn fn__18624
                                                      ([p__18623 arg]
                                                        (let [vec__18625 p__18623
                                                              m
                                                              (nth
                                                                vec__18625
                                                                (unchecked-int 0)
                                                                nil)
                                                              args
                                                              (nth
                                                                vec__18625
                                                                (unchecked-int 1)
                                                                nil)]
                                                          (if (variable-or-blank? arg)
                                                            [m (conj args arg)]
                                                            (let 
                                                              [garg (gensym "?c__")]
                                                              [(assoc m garg arg)
                                                               (conj args garg)])))))
                                                    [m []]
                                                    args)
                                       m (nth vec__18620 (unchecked-int 0) nil)
                                       args (nth vec__18620 (unchecked-int 1) nil)]
                                   [m (conj cs (cons pred args))])
                                 [m (conj cs c)]))))
                         [{} []]
                         clauses)
            subs (nth vec__18608 (unchecked-int 0) nil)
            new_clauses (nth vec__18608 (unchecked-int 1) nil)]
        (if (empty? subs)
          clauses
          (vec
            (cons
              (apply
                vector
                (seq
                  (-> (clojure.core/list 'clojure.core/vector)
                   (concat (vals subs))
                   (seq)
                   (clojure.core/list)
                   (concat (clojure.core/list (apply vector (seq (concat (keys subs)))))))))
              new_clauses))))))
  (reset-meta!
    #'lift-consts-from-preds
    (assoc
      {:arglists (clojure.core/list ['clauses]), :column (int 1)}
      :name
      'lift-consts-from-preds
      :ns
      *ns*))
  (defn all-pred
    ([p__18635]
      (let [vec__18636 p__18635
            seq__18637 (seq vec__18636)
            first__18638 (first seq__18637)
            seq__18637 (next seq__18637)
            p first__18638
            cs seq__18637
            c vec__18636]
        (cond
          (#{'and 'not 'or} p) (every? all-pred cs)
          (and (instance? java.util.List p) (nil? cs)) (do true)))))
  (reset-meta!
    #'all-pred
    (assoc
      {:arglists (clojure.core/list [['p '& 'cs :as 'c]]), :column (int 1)}
      :name
      'all-pred
      :ns
      *ns*))
  (defn to-pred
    ([p__18641]
      (let [vec__18642 p__18641
            seq__18643 (seq vec__18642)
            first__18644 (first seq__18643)
            seq__18643 (next seq__18643)
            p first__18644
            cs seq__18643
            c vec__18642]
        (cond
          (#{'and 'or} p) (cons p (map to-pred cs))
          (= 'not p) (clojure.core/list p (list* 'and (map to-pred cs)))
          :else (do p)))))
  (reset-meta!
    #'to-pred
    (assoc
      {:arglists (clojure.core/list [['p '& 'cs :as 'c]]), :column (int 1)}
      :name
      'to-pred
      :ns
      *ns*))
  (defn not-or-all-pred
    ([p__18646]
      (let [vec__18647 p__18646
            seq__18648 (seq vec__18647)
            first__18649 (first seq__18648)
            seq__18648 (next seq__18648)
            p first__18649
            cs seq__18648
            c vec__18647]
        (if (and (#{'not 'or} p) (all-pred c))
          (let [vs (vec (unifying-var-set c)) pred (to-pred c)]
            (apply
              vector
              (seq
                (-> (clojure.core/list 'clojure.core/fn)
                 (concat
                   (clojure.core/list (apply vector (seq (concat vs))))
                   (clojure.core/list pred))
                 (seq)
                 (clojure.core/list)
                 (concat vs)
                 (seq)
                 (clojure.core/list)
                 (concat)))))
          c))))
  (reset-meta!
    #'not-or-all-pred
    (assoc
      {:arglists (clojure.core/list [['p '& 'cs :as 'c]]), :column (int 1)}
      :name
      'not-or-all-pred
      :ns
      *ns*))
  (defn not-or->not-or-join
    ([p__18652]
      (let [vec__18653 p__18652
            seq__18654 (seq vec__18653)
            first__18655 (first seq__18654)
            seq__18654 (next seq__18654)
            p first__18655
            cs seq__18654
            c vec__18653]
        (if (#{'not 'or} p)
          (let [vs (vec (unifying-var-set c))]
            (with-meta (list* ({'not 'not-join, 'or 'or-join} p) vs cs) (meta c)))
          c))))
  (reset-meta!
    #'not-or->not-or-join
    (assoc
      {:arglists (clojure.core/list [['p '& 'cs :as 'c]]), :column (int 1)}
      :name
      'not-or->not-or-join
      :ns
      *ns*))
  (defn normalize-or-join
    ([p__18658]
      (let [vec__18659 p__18658
            seq__18660 (seq vec__18659)
            first__18661 (first seq__18660)
            seq__18660 (next seq__18660)
            p first__18661
            first__18661 (first seq__18660)
            seq__18660 (next seq__18660)
            vs first__18661
            cs seq__18660
            c vec__18659]
        (if (= 'or-join p)
          (let [cs (map
                     (fn fn__18662
                       ([p1__18657#]
                         (if (= 'and (first p1__18657#))
                           (rest p1__18657#)
                           (clojure.core/list p1__18657#))))
                     cs)]
            (when (empty? vs)
              (error/arg
                :db.error/or-binding-empty
                (str "'or' cannot have empty binding set: " c)))
            (with-meta (list* p vs cs) (meta c)))
          c))))
  (reset-meta!
    #'normalize-or-join
    (assoc
      {:arglists (clojure.core/list [['p 'vs '& 'cs :as 'c]]), :column (int 1)}
      :name
      'normalize-or-join
      :ns
      *ns*))
  (defn or-join->rule-preds
    ([p__18665]
      (let [vec__18666 p__18665
            seq__18667 (seq vec__18666)
            first__18668 (first seq__18667)
            seq__18667 (next seq__18667)
            p first__18668
            first__18668 (first seq__18667)
            seq__18667 (next seq__18667)
            vs first__18668
            cs seq__18667
            rname (gensym "arule__")
            head (cons rname vs)]
        (mapv (partial cons (with-meta head #:query-stats{:clause (list* p [vs])})) cs))))
  (reset-meta!
    #'or-join->rule-preds
    (assoc
      {:arglists (clojure.core/list [['p 'vs '& 'cs]]), :column (int 1)}
      :name
      'or-join->rule-preds
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.datalog" "add-rule") {:declared true, :column (int 1)})
  (defn prep-clauses
    ([rm clauses]
      (let [clauses (map normalize-or-join (map not-or->not-or-join (map not-or-all-pred clauses)))
            vec__18670 (reduce
                         (fn fn__18674
                           ([p__18673 c]
                             (let [vec__18675 p__18673
                                   rm (nth vec__18675 (unchecked-int 0) nil)
                                   cs (nth vec__18675 (unchecked-int 1) nil)]
                               (if (= 'or-join (first c))
                                 (let [preds (or-join->rule-preds c)
                                       vec__18678 (ffirst preds)
                                       seq__18679 (seq vec__18678)
                                       first__18680 (first seq__18679)
                                       seq__18679 (next seq__18679)
                                       rname first__18680
                                       args seq__18679
                                       head vec__18678
                                       rm (add-rule rm rname preds)]
                                   [rm
                                    (conj
                                      cs
                                      (with-meta (flatten head) (merge (meta c) (meta head))))])
                                 [rm (conj cs c)]))))
                         [rm []]
                         clauses)
            rm (nth vec__18670 (unchecked-int 0) nil)
            clauses (nth vec__18670 (unchecked-int 1) nil)]
        [rm (mapv expr-clause (lift-consts-from-preds clauses))])))
  (reset-meta!
    #'prep-clauses
    (assoc
      {:arglists (clojure.core/list ['rm 'clauses]), :column (int 1)}
      :name
      'prep-clauses
      :ns
      *ns*))
  (defn callees
    ([x]
      (if (list? x)
        (cons (first x) (mapcat callees (rest x)))
        (let [G__18683 (data/equality-partition x)]
          (case
            G__18683
            (:sequential :set)
            (mapcat callees x)
            :map
            (concat (mapcat (callees (keys x))) (mapcat (callees (vals x))))
            :atom
            nil)))))
  (reset-meta!
    #'callees
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'callees :ns *ns*))
  (defn add-rule
    ([rm rname preds]
      (let [heads (map first preds)
            rcnt (fn rcnt
                   ([p__18688]
                     (let [vec__18690 p__18688
                           seq__18691 (seq vec__18690)
                           first__18692 (first seq__18691)
                           seq__18691 (next seq__18691)
                           p first__18692
                           first__18692 (first seq__18691)
                           seq__18691 (next seq__18691)
                           r first__18692
                           xs seq__18691]
                       (when-not (every? symbol? xs)
                         (throw
                           (java.lang.AssertionError.
                             (str
                               "Assert failed: "
                               (str "Required args list must be first in predicate: " p)
                               "\n"
                               (pr-str (clojure.core/list 'every? 'symbol? 'xs))))))
                       (if (instance? java.util.List r)
                         (java.lang.Integer/valueOf (int (count r)))
                         0))))
            rmax (apply max (map rcnt heads))
            rname (cond-> rname (clojure.lang.Numbers/isPos rmax) (vary-meta assoc :reqcnt rmax))
            vec__18685 (reduce
                         (fn fn__18697
                           ([p__18695 p__18696]
                             (let [vec__18698 p__18695
                                   rm (nth vec__18698 (unchecked-int 0) nil)
                                   ps (nth vec__18698 (unchecked-int 1) nil)
                                   vec__18701 p__18696
                                   seq__18702 (seq vec__18701)
                                   first__18703 (first seq__18702)
                                   seq__18702 (next seq__18702)
                                   head first__18703
                                   clauses seq__18702
                                   pred vec__18701
                                   vec__18704 (prep-clauses rm clauses)
                                   rm (nth vec__18704 (unchecked-int 0) nil)
                                   cs (nth vec__18704 (unchecked-int 1) nil)]
                               [rm (conj ps (cons (vec (flatten head)) cs))])))
                         [rm []]
                         preds)
            rm (nth vec__18685 (unchecked-int 0) nil)
            preds (nth vec__18685 (unchecked-int 1) nil)]
        (assoc rm rname (vec preds)))))
  (reset-meta!
    #'add-rule
    (assoc
      {:arglists (clojure.core/list ['rm 'rname 'preds]), :column (int 1)}
      :name
      'add-rule
      :ns
      *ns*))
  (defn rule-map
    ([rules]
      (let [rules (if (string? rules) (binding [*read-eval* false] (read-string rules)) rules)
            rules (group-by ffirst rules)
            rules (reduce-kv add-rule {} rules)]
        (loop [seq_18711 (seq rules) chunk_18712 nil count_18713 0 i_18714 0]
          (if (< i_18714 count_18713)
            (let [vec__18715 (.nth ^clojure.lang.Indexed chunk_18712 (unchecked-int i_18714))
                  k (nth vec__18715 (unchecked-int 0) nil)
                  v (nth vec__18715 (unchecked-int 1) nil)]
              (when-not (apply = (map (comp count first) v))
                (throw
                  (java.lang.AssertionError.
                    (str
                      "Assert failed: "
                      (str "Arity mismatch for predicate: " k)
                      "\n"
                      (pr-str
                        (clojure.core/list
                          'apply
                          '=
                          (clojure.core/list 'map (clojure.core/list 'comp 'count 'first) 'v)))))))
              (recur seq_18711 chunk_18712 count_18713 (inc i_18714)))
            (let [temp__5804__auto__ (seq seq_18711)]
              (when temp__5804__auto__
                (let [seq_18711 temp__5804__auto__]
                  (if (chunked-seq? seq_18711)
                    (let [c__6065__auto__ (chunk-first seq_18711)]
                      (recur (chunk-rest seq_18711) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [vec__18718 (first seq_18711)
                          k (nth vec__18718 (unchecked-int 0) nil)
                          v (nth vec__18718 (unchecked-int 1) nil)]
                      (when-not (apply = (map (comp count first) v))
                        (throw
                          (java.lang.AssertionError.
                            (str
                              "Assert failed: "
                              (str "Arity mismatch for predicate: " k)
                              "\n"
                              (pr-str
                                (clojure.core/list
                                  'apply
                                  '=
                                  (clojure.core/list
                                    'map
                                    (clojure.core/list 'comp 'count 'first)
                                    'v)))))))
                      (recur (next seq_18711) nil 0 0))))))))
        rules)))
  (reset-meta!
    #'rule-map
    (assoc {:arglists (clojure.core/list ['rules]), :column (int 1)} :name 'rule-map :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.datalog" "rule-cache") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.datalog" "rule-cache")
    (cache/create-computing rule-map 1000))
  (.setMeta (clojure.lang.RT/var "datomic.datalog" "q") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.datalog" "q") (clojure.lang.RT/var "datomic.query" "q"))
  (defn eval-not-join
    ([srcs prog inrel inbinds p__18725]
      (let [vec__18726 p__18725
            seq__18727 (seq vec__18726)
            first__18728 (first seq__18727)
            seq__18727 (next seq__18727)
            _ first__18728
            first__18728 (first seq__18727)
            seq__18727 (next seq__18727)
            vars first__18728
            cs seq__18727
            not_join_clause vec__18726
            used (used-srcs srcs not_join_clause)
            dbs (select-keys srcs used)
            csrc (:tag (meta not_join_clause))
            dbs (cond-> dbs csrc (assoc '$ (get srcs csrc)))
            uvs (set vars)
            binds (map
                    (fn fn__18730
                      ([p1__18724#] (or (^clojure.lang.IFn uvs p1__18724#) (gensym "?nb__"))))
                    inbinds)
            query (apply
                    hash-map
                    (seq
                      (concat
                        (clojure.core/list :find)
                        (clojure.core/list binds)
                        (clojure.core/list :in)
                        (clojure.core/list
                          (apply
                            vector
                            (seq
                              (concat
                                (keys dbs)
                                (clojure.core/list '%)
                                (clojure.core/list
                                  (apply
                                    vector
                                    (seq
                                      (concat
                                        (clojure.core/list
                                          (apply vector (seq (concat binds))))))))))))
                        (clojure.core/list :where)
                        (clojure.core/list (apply vector (seq (concat cs)))))))
            G__18733 (java.util.HashSet. ^java.util.Collection inrel)]
        (.removeAll ^java.util.AbstractSet G__18733 (q query (conj (vec (vals dbs)) prog inrel)))
        G__18733)))
  (reset-meta!
    #'eval-not-join
    (assoc
      {:arglists
       (clojure.core/list
         ['srcs
          'prog
          (.withMeta 'inrel {:tag 'Collection})
          'inbinds
          ['_ 'vars '& 'cs :as 'not-join-clause]]),
       :column (int 1)}
      :name
      'eval-not-join
      :ns
      *ns*))
  (defn eval-clause
    ([db srcs prog oprog clause predctor inrel inbinds next_binds sched_fn src ans ins top_bounds]
      (query-stats/merge-with-clause-stats!
        {:clause clause,
         :rows-in (java.lang.Integer/valueOf (int (count inrel))),
         :binds-in inbinds,
         :binds-out next_binds})
      (maybe-cancel)
      (if (not-join-clause? clause)
        (let [nrel (eval-not-join srcs oprog inrel inbinds clause)
              ret (project nrel inbinds next_binds)]
          (query-stats/merge-with-clause-stats!
            {:rows-out (java.lang.Integer/valueOf (int (count ret)))})
          ret)
        (let [exf (and (map? clause) (:binds clause))
              exp (and (map? clause) (nil? (:binds clause)))
              ext? (and (not (or exf exp)) (extensional? prog (first clause)))
              args (cond
                     ext? clause
                     (or exf exp) (concat (:argvars clause) (:binds clause))
                     :else (do (rest clause)))
              _ (when (some nil? args)
                  (error/arg
                    :db.error/invalid-clause
                    (str "Can't have nil args in clause: " clause)))
              consts (mapv
                       (fn fn__18741
                         ([p1__18735#]
                           (if (variable-or-blank? p1__18735#)
                             (some-> top_bounds (:consts) (^clojure.lang.IFn p1__18735#))
                             p1__18735#)))
                       args)
              starts (mapv
                       (fn fn__18744 ([p1__18736#] (some-> top_bounds (:starts) (get p1__18736#))))
                       args)
              whiles (mapv
                       (fn fn__18747 ([p1__18737#] (some-> top_bounds (:whiles) (get p1__18737#))))
                       args)
              vec__18738 (create-join-maps inbinds args next_binds)
              join (nth vec__18738 (unchecked-int 0) nil)
              projx (nth vec__18738 (unchecked-int 1) nil)
              projy (nth vec__18738 (unchecked-int 2) nil)
              root? (and (empty? join) (empty? inbinds))
              ret (try
                    (if ext?
                      (join-project
                        (if (and root? (empty? inrel)) #{[]} inrel)
                        (extrel db consts starts whiles)
                        join
                        projx
                        projy
                        predctor)
                      (if exf
                        (join-project
                          (if (and root? (empty? inrel)) #{[]} inrel)
                          (fnrel db clause consts)
                          join
                          projx
                          projy
                          predctor)
                        (if exp
                          (join-project
                            (if (and root? (empty? inrel)) #{[]} inrel)
                            (predrel db clause consts)
                            join
                            projx
                            projy
                            nil)
                          (if (get prog (first clause))
                            (let [bindset (set inbinds)
                                  apred (adorned-pred clause bindset)
                                  outbinds (filter bindset (filter variable? (next clause)))
                                  pred (first clause)
                                  aresk [src pred]]
                              (query-stats/with-phase-stats
                                (fn fn__18751
                                  ([]
                                    (eval-query
                                      db
                                      prog
                                      oprog
                                      apred
                                      (project inrel inbinds outbinds)
                                      sched_fn
                                      src
                                      ans
                                      ins
                                      nil
                                      (when top_bounds
                                        {:consts consts, :starts starts, :whiles whiles})))))
                              (join-project
                                (if (and root? (empty? inrel)) #{[]} inrel)
                                (get ans aresk [])
                                join
                                projx
                                projy
                                predctor))
                            (do
                              (when :else
                                (throw
                                  (java.lang.IllegalArgumentException.
                                    (str "Undefined predicate: " (first clause)))))
                              nil)))))
                    (catch
                      java.lang.Exception
                      ex
                      (do
                        (throw
                          (java.lang.Exception.
                            (str
                              "processing clause: "
                              clause
                              ", message: "
                              (.getMessage ^java.lang.Throwable ex))
                            ^java.lang.Throwable ex))
                        nil)))]
          (query-stats/merge-with-clause-stats!
            {:rows-out (java.lang.Integer/valueOf (int (count ret)))})
          ret))))
  (reset-meta!
    #'eval-clause
    (assoc
      {:arglists
       (clojure.core/list
         ['db
          'srcs
          'prog
          'oprog
          'clause
          'predctor
          'inrel
          'inbinds
          'next-binds
          'sched-fn
          'src
          'ans
          'ins
          'top-bounds]),
       :column (int 1)}
      :name
      'eval-clause
      :ns
      *ns*))
  (defn remap-bounds
    ([nb args]
      (let [ia (zipmap (range) args)
            mapize (fn mapize
                     ([p1__18765#]
                       (into
                         {}
                         (map-indexed
                           (fn fn__18767 ([i c] (when-not (nil? c) [(^clojure.lang.IFn ia i) c])))
                           p1__18765#))))]
        {:consts (^clojure.lang.IFn mapize (:consts nb)),
         :starts (^clojure.lang.IFn mapize (:starts nb)),
         :whiles (^clojure.lang.IFn mapize (:whiles nb))})))
  (reset-meta!
    #'remap-bounds
    (assoc
      {:arglists (clojure.core/list ['nb 'args]), :column (int 1)}
      :name
      'remap-bounds
      :ns
      *ns*))
  (defn eval-rule
    ([db prog oprog p__18771 p__18772 inrel sched_fn src ans ins top_bounds nested_bounds]
      (let [vec__18773 p__18771
            seq__18774 (seq vec__18773)
            first__18775 (first seq__18774)
            seq__18774 (next seq__18774)
            head first__18775
            body seq__18774
            rule vec__18773
            vec__18776 p__18772
            pred (nth vec__18776 (unchecked-int 0) nil)
            adorn (nth vec__18776 (unchecked-int 1) nil)
            apred vec__18776
            vec__18779 head
            seq__18780 (seq vec__18779)
            first__18781 (first seq__18780)
            seq__18780 (next seq__18780)
            hpred first__18781
            hargs seq__18780
            aresk [src pred]
            inbinds (keep-indexed
                      (fn fn__18782 ([i a] (when-not (^clojure.lang.IFn adorn i) a)))
                      hargs)
            multi? (and (map? db) (not (instance? datomic.db.IDb db)))
            srcs (if multi? db {'$ db})
            cbs (push-preds srcs (^clojure.lang.IFn sched_fn srcs prog rule inbinds))
            top_bounds (if nested_bounds (remap-bounds nested_bounds hargs) top_bounds)
            res (try
                  (loop [sbinds inbinds sup inrel cbs cbs]
                    (if cbs
                      (let [vec__18785 (first cbs)
                            c (nth vec__18785 (unchecked-int 0) nil)
                            next_binds (nth vec__18785 (unchecked-int 1) nil)
                            predctor (nth vec__18785 (unchecked-int 2) nil)
                            csrc (:tag (meta c))
                            cdb (cond
                                  (and csrc multi?) (let [x (get db csrc)]
                                                      (when (nil? x)
                                                        (throw
                                                          (java.lang.Exception.
                                                            (str
                                                              "Unable to find data source: "
                                                              csrc
                                                              " in: "
                                                              (keys db)))))
                                                      x)
                                  multi? (get db '$)
                                  :else (do db))
                            sup1 (query-stats/with-clause-stats
                                   (fn fn__18788
                                     ([]
                                       (eval-clause
                                         cdb
                                         srcs
                                         prog
                                         oprog
                                         c
                                         predctor
                                         sup
                                         sbinds
                                         next_binds
                                         sched_fn
                                         (or csrc src)
                                         ans
                                         ins
                                         top_bounds))))]
                        (recur next_binds sup1 (next cbs)))
                      (project sup sbinds (vec (filter variable? hargs)))))
                  (catch
                    java.lang.Exception
                    ex
                    (do
                      (throw
                        (java.lang.Exception.
                          (str
                            "processing rule: "
                            head
                            ", message: "
                            (.getMessage ^java.lang.Throwable ex))
                          ^java.lang.Throwable ex))
                      nil)))
            anspred (get ans aresk (java.util.HashSet.))]
        (.addAll ^java.util.Set anspred ^java.util.Collection res)
        (.put ^java.util.Map ans aresk anspred)
        nil)))
  (reset-meta!
    #'eval-rule
    (assoc
      {:arglists
       (clojure.core/list
         ['db
          'prog
          'oprog
          ['head '& 'body :as 'rule]
          ['pred 'adorn :as 'apred]
          'inrel
          'sched-fn
          'src
          (.withMeta 'ans {:tag 'Map})
          'ins
          'top-bounds
          'nested-bounds]),
       :column (int 1)}
      :name
      'eval-rule
      :ns
      *ns*))
  (defn eval-query
    ([db prog oprog p__18795 input sched_fn src ans ins top_bounds nested_bounds]
      (let [vec__18796 p__18795
            pred (nth vec__18796 (unchecked-int 0) nil)
            adorn (nth vec__18796 (unchecked-int 1) nil)
            apred vec__18796
            iresk [src apred]
            inpred (get ins iresk (java.util.HashSet.))
            input (java.util.HashSet. ^java.util.Collection input)]
        (.removeAll ^java.util.AbstractSet input ^java.util.Collection inpred)
        (when (or (not (.isEmpty ^java.util.HashSet input)) (every? identity adorn))
          (let [rules (get prog pred)]
            (.addAll ^java.util.Set inpred ^java.util.Collection input)
            (.put ^java.util.Map ins iresk inpred)
            (loop [seq_18799 (seq rules) chunk_18800 nil count_18801 0 i_18802 0]
              (if (< i_18802 count_18801)
                (let [rule (.nth ^clojure.lang.Indexed chunk_18800 (unchecked-int i_18802))]
                  (eval-rule
                    db
                    prog
                    oprog
                    rule
                    apred
                    input
                    sched_fn
                    src
                    ans
                    ins
                    top_bounds
                    nested_bounds)
                  (recur seq_18799 chunk_18800 count_18801 (inc i_18802)))
                (let [temp__5804__auto__ (seq seq_18799)]
                  (when temp__5804__auto__
                    (let [seq_18799 temp__5804__auto__]
                      (if (chunked-seq? seq_18799)
                        (let [c__6065__auto__ (chunk-first seq_18799)]
                          (recur (chunk-rest seq_18799) c__6065__auto__ (count c__6065__auto__) 0))
                        (let [rule (first seq_18799)]
                          (eval-rule
                            db
                            prog
                            oprog
                            rule
                            apred
                            input
                            sched_fn
                            src
                            ans
                            ins
                            top_bounds
                            nested_bounds)
                          (recur (next seq_18799) nil 0 0))))))))))
        nil)))
  (reset-meta!
    #'eval-query
    (assoc
      {:arglists
       (clojure.core/list
         ['db
          'prog
          'oprog
          ['pred 'adorn :as 'apred]
          (.withMeta 'input {:tag 'Set})
          'sched-fn
          'src
          'ans
          (.withMeta 'ins {:tag 'Map})
          'top-bounds
          'nested-bounds]),
       :column (int 1)}
      :name
      'eval-query
      :ns
      *ns*))
  (defn bound-consts
    ([srcs query]
      (reduce-kv
        (fn fn__18807
          ([m v b]
            (assoc
              m
              v
              (if (vector? b)
                (let [vec__18808 b
                      src (nth vec__18808 (unchecked-int 0) nil)
                      idx (nth vec__18808 (unchecked-int 1) nil)]
                  (nth (get srcs src) (unchecked-int ^java.lang.Number idx)))
                (get srcs b)))))
        {}
        (:in-consts query))))
  (reset-meta!
    #'bound-consts
    (assoc
      {:arglists (clojure.core/list ['srcs 'query]), :column (int 1)}
      :name
      'bound-consts
      :ns
      *ns*))
  (defn ranges
    ([in_consts query]
      [(reduce-kv
         (fn fn__18814 ([m v c] (assoc m v (if (variable? c) (get in_consts c) c))))
         {}
         (:range-starts query))
       (let [cmps {'= =, '< ext/<, '<= ext/<=}]
         (reduce-kv
           (fn fn__18817
             ([m v p__18816]
               (let [vec__18818 p__18816
                     cmpsym (nth vec__18818 (unchecked-int 0) nil)
                     cb (nth vec__18818 (unchecked-int 1) nil)
                     c (if (variable? cb) (get in_consts cb) cb)
                     cmp (^clojure.lang.IFn cmps cmpsym)]
                 (assoc m v (fn fn__18821 ([p1__18813#] (^clojure.lang.IFn cmp p1__18813# c)))))))
           {}
           (:range-whiles query)))]))
  (reset-meta!
    #'ranges
    (assoc
      {:arglists (clojure.core/list ['in-consts 'query]), :column (int 1)}
      :name
      'ranges
      :ns
      *ns*))
  (defn qsqr
    ([db query sched_fn]
      (let [map__18825 query
            map__18825 (if (seq? map__18825)
                         (if (next map__18825)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18825))
                           (if (seq map__18825) (first map__18825) {}))
                         map__18825)
            from (get map__18825 :in)
            pargs (get map__18825 :find)
            clauses (get map__18825 :where)
            db (if from (zipmap from db) (first db))
            inrel (java.util.HashSet.)
            pred (gensym "q__")
            q (cons pred pargs)
            oprog (when from (get db '%))
            prog (when oprog (get rule-cache oprog))
            prog (merge prog (:arules query))
            prog (assoc prog pred [(cons q clauses)])
            rec (recursive? prog pred)
            in_consts (bound-consts db query)
            vec__18826 (ranges in_consts query)
            range_starts (nth vec__18826 (unchecked-int 0) nil)
            range_whiles (nth vec__18826 (unchecked-int 1) nil)
            top_bounds {:consts in_consts, :starts range_starts, :whiles range_whiles}
            ans (java.util.HashMap.)
            apred (adorned-pred q)
            aresk [nil pred]
            cancel (atom nil)]
        (let [temp__5804__auto__ (when (contains? query :timeout) (first (:timeout query)))]
          (when temp__5804__auto__
            (let [timeout temp__5804__auto__ f (fn f ([] (reset! cancel "timeout elapsed")))]
              (.schedule
                cancel-service
                ^java.util.concurrent.Callable f
                (long timeout)
                TimeUnit/MILLISECONDS))))
        (push-thread-bindings (hash-map #'*cancel* cancel))
        (try
          (loop [asnap {} round 0]
            (do
              (query-stats/with-phase-stats
                (fn fn__18831
                  ([]
                    (eval-query
                      db
                      prog
                      oprog
                      apred
                      inrel
                      sched_fn
                      nil
                      ans
                      (java.util.HashMap.)
                      top_bounds
                      nil))))
              (let [asnap_next (zipmap (keys ans) (map count (vals ans)))]
                (if (or (not rec) (= asnap_next asnap))
                  (get ans aresk (java.util.HashSet.))
                  (recur asnap_next (inc round))))))
          (finally (pop-thread-bindings)))))
    ([db query] (qsqr db query sched-in-order)))
  (reset-meta!
    #'qsqr
    (assoc
      {:arglists (clojure.core/list ['db 'query] ['db 'query 'sched-fn]), :column (int 1)}
      :name
      'qsqr
      :ns
      *ns*))
  (defn rel-fn
    ([f] (fn fn__18836 ([_ & args] (let [ret (apply f args)] [(conj (into [] args) ret)])))))
  (reset-meta!
    #'rel-fn
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'rel-fn :ns *ns*))
  (defn rel-pred ([f] (fn fn__18839 ([_ & args] (apply f args)))))
  (reset-meta!
    #'rel-pred
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'rel-pred :ns *ns*)))
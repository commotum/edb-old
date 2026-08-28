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
    (assoc {:const true, :column 1} :name 'query-thread-prefix :ns *ns*))
  (defn on-query-thread?
    ([]
      (str/starts-with?
        (.getName (java.lang.Thread/currentThread))
        "query-40e7d292-b60a-40ef-b6d8-db96660e415b-")))
  (defonce query-pool
   (common/thread-pool
     {:nthreads (config/property "datomic.queryPool"),
      :name "query-40e7d292-b60a-40ef-b6d8-db96660e415b-",
      :metrics? false}))
  (reset-meta!
    #'query-pool
    (assoc {:tag java.util.concurrent.ExecutorService, :column 1} :name 'query-pool :ns *ns*))
  (defn qmapv
    ([f coll] (if (on-query-thread?) (mapv f coll) (common/pooled-mapv query-pool f coll))))
  (defonce cancel-service (java.util.concurrent.ScheduledThreadPoolExecutor. (unchecked-int 1)))
  (reset-meta!
    #'cancel-service
    (assoc
      {:tag java.util.concurrent.ScheduledExecutorService, :column 1}
      :name
      'cancel-service
      :ns
      *ns*))
  (def QUERY_DEFAULT_TIMEOUT nil)
  (reset-meta!
    #'QUERY_DEFAULT_TIMEOUT
    (assoc {:const true, :column 1} :name 'QUERY_DEFAULT_TIMEOUT :ns *ns*))
  (def ^{:dynamic true} *cancel* (atom nil))
  (reset-meta! #'*cancel* (assoc {:dynamic true, :column 1} :name '*cancel* :ns *ns*))
  (defn maybe-cancel
    ([]
      (let [temp__5804__auto__ (deref *cancel*)]
        (when temp__5804__auto__
          (let [why temp__5804__auto__]
            (throw (java.util.concurrent.TimeoutException. (str "Query canceled: " why)))))
        nil)))
  (defonce IJoin {})
  (defprotocol
    IJoin
    (join-project [xs ys join-map project-map-x project-map-y predctor])
    (join-project-with [ys xs join-map project-map-x project-map-y predctor]))
  (defn truep ([] (fn fn__14238 ([_] true))))
  (defn tuple
    ([arr]
      (let [array (to-array arr)]
        (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" array))))
  (defn iterator ([xs] (.iterator ^java.lang.Iterable xs)))
  (reset-meta!
    #'iterator
    (assoc
      {:tag java.util.Iterator,
       :private true,
       :arglists (clojure.core/list [(.withMeta 'xs {:tag 'Iterable})]),
       :column 1}
      :name
      'iterator
      :ns
      *ns*))
  (defn matchf
    ([bindings]
      (fn fn__14243
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
  (defn hashxf
    ([bindings]
      (fn fn__14246
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
  (defn hashyf
    ([bindings]
      (fn fn__14249
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
  (defn join-project-coll
    ([xs ys join_map project_map_x project_map_y predctor]
      (join-project-with
        ys
        xs
        (zipmap (vals join_map) (keys join_map))
        project_map_y
        project_map_x
        predctor)))
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
                      _ (loop [seq_14261 (seq join_map) chunk_14262 nil count_14263 0 i_14264 0]
                          (if (< i_14264 count_14263)
                            (let [vec__14266 (.nth
                                               ^clojure.lang.Indexed chunk_14262
                                               (unchecked-int i_14264))
                                  k (nth vec__14266 (unchecked-int 0) nil)
                                  v (nth vec__14266 (unchecked-int 1) nil)]
                              (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                              (recur seq_14261 chunk_14262 count_14263 (inc i_14264)))
                            (let [temp__5804__auto__ (seq seq_14261)]
                              (when temp__5804__auto__
                                (let [seq_14261 temp__5804__auto__]
                                  (if (chunked-seq? seq_14261)
                                    (let [c__6065__auto__ (chunk-first seq_14261)]
                                      (recur
                                        (chunk-rest seq_14261)
                                        c__6065__auto__
                                        (count c__6065__auto__)
                                        0))
                                    (let [vec__14269 (first seq_14261)
                                          k (nth vec__14269 (unchecked-int 0) nil)
                                          v (nth vec__14269 (unchecked-int 1) nil)]
                                      (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                                      (recur (next seq_14261) nil 0 0))))))))
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
  (extend
    java.util.Collection
    IJoin
    {:join-project
     (fn fn__14290
       ([xs ys join_map project_map_x project_map_y predctor]
         (join-project-coll xs ys join_map project_map_x project_map_y predctor))),
     :join-project-with
     (fn fn__14292
       ([xs ys join_map project_map_x project_map_y predctor]
         (join-project-coll-with xs ys join_map project_map_x project_map_y predctor)))})
  (extend
    java.lang.Object
    IJoin
    {:join-project
     (fn fn__14294
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
     (fn fn__14296
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
  (defn resolve-id
    ([db x]
      (when x
        (or
          (db/resolve-id db x)
          (do (throw (java.lang.IllegalArgumentException. (str "Cannot resolve key: " x))) nil)))))
  (defn dbrel
    ([db p__14305 starts whiles]
      (let [vec__14306 p__14305
            e (nth vec__14306 (unchecked-int 0) nil)
            a (nth vec__14306 (unchecked-int 1) nil)
            v (nth vec__14306 (unchecked-int 2) nil)
            t (nth vec__14306 (unchecked-int 3) nil)
            added (nth vec__14306 (unchecked-int 4) nil)
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
  (defonce ExtRel {})
  (defprotocol ExtRel (extrel [src consts starts whiles]))
  (defn extrel-coll
    ([src consts]
      (if (every? nil? consts)
        src
        (filter
          (fn fn__14337
            ([p1__14336#]
              (loop [i 0]
                (if (< i (count consts))
                  (if (or
                        (nil? (nth consts (unchecked-int i)))
                        (= (nth p1__14336# (unchecked-int i)) (nth consts (unchecked-int i))))
                    (recur (inc i))
                    false)
                  true))))
          src))))
  (extend
    nil
    ExtRel
    {:extrel
     (fn fn__14341
       ([src consts _ _]
         (error/arg
           :db.error/invalid-data-source
           "Nil or missing data source. Did you forget to pass a database argument?"
           {:input src})))})
  (extend
    java.lang.Object
    ExtRel
    {:extrel
     (fn fn__14343
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
    {:extrel (fn fn__14345 ([src consts starts whiles] (dbrel src consts starts whiles)))})
  (extend
    java.util.Map
    ExtRel
    {:extrel (fn fn__14347 ([src consts _ _] (extrel (seq src) consts nil nil)))})
  (extend
    java.util.Collection
    ExtRel
    {:extrel (fn fn__14349 ([src consts _ _] (extrel-coll src consts)))})
  (def E 0)
  (reset-meta! #'E (assoc {:const true, :column 1} :name 'E :ns *ns*))
  (def A 1)
  (reset-meta! #'A (assoc {:const true, :column 1} :name 'A :ns *ns*))
  (def V 2)
  (reset-meta! #'V (assoc {:const true, :column 1} :name 'V :ns *ns*))
  (def T 3)
  (reset-meta! #'T (assoc {:const true, :column 1} :name 'T :ns *ns*))
  (def ADDED 4)
  (reset-meta! #'ADDED (assoc {:const true, :column 1} :name 'ADDED :ns *ns*))
  (def DCOUNT 5)
  (reset-meta! #'DCOUNT (assoc {:const true, :column 1} :name 'DCOUNT :ns *ns*))
  (extend
    datomic.datalog.DbRel
    IJoin
    {:join-project
     (fn fn__14365
       ([dbrel ys join_map project_map_x project_map_y predctor]
         (join-project-with dbrel ys join_map project_map_x project_map_y predctor))),
     :join-project-with
     (fn fn__14367
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
                          (map (fn fn__14374 ([p1__14351#] (get join_map p1__14351#))) (range 5)))
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
               vec__14368 (if (aget ^"[Ljava.lang.Object;" bound 1)
                            (cond
                              (aget ^"[Ljava.lang.Object;" bound 0) [(fn 
                                                                       fn__14385
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
                                                                       fn__14387
                                                                       ([d]
                                                                         (db/windowed
                                                                           db
                                                                           (fn 
                                                                             fn__14388
                                                                             ([p1__14352#]
                                                                               (and
                                                                                 (=
                                                                                   (long
                                                                                     (.getE
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getE
                                                                                       ^datomic.impl.db.IDatum p1__14352#)))
                                                                                 (=
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__14352#)))
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
                                                                                         ^datomic.impl.db.IDatum p1__14352#))
                                                                                     (^clojure.lang.IFn whilev
                                                                                       (.getV
                                                                                         ^datomic.impl.db.IDatum p1__14352#)))))))
                                                                           (.seekAEVT
                                                                             ^datomic.db.IDb db
                                                                             ^datomic.impl.db.IDatum d))))]
                              (aget ^"[Ljava.lang.Object;" bound 2) [(fn 
                                                                       fn__14395
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           java.lang.Long/MIN_VALUE
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           (.getV
                                                                             ^datomic.impl.db.IDatum d)
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__14397
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
                                                                                   fn__14398
                                                                                   ([p1__14353#]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum p1__14353#)))
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__14353#)))))
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
                                                                                   fn__14401
                                                                                   ([p1__14354#]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum p1__14354#)))
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__14354#)))))
                                                                                 (.seekRAET
                                                                                   ^datomic.db.IDb db
                                                                                   ^datomic.impl.db.IDatum d))
                                                                               :else
                                                                               (do
                                                                                 (iter/filter
                                                                                   (fn 
                                                                                     fn__14404
                                                                                     ([p1__14355#]
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__14355#))))
                                                                                   (db/windowed
                                                                                     db
                                                                                     (fn 
                                                                                       fn__14406
                                                                                       ([p1__14356#]
                                                                                         (=
                                                                                           (long
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d))
                                                                                           (long
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum p1__14356#)))))
                                                                                     (.seekAEVT
                                                                                       ^datomic.db.IDb db
                                                                                       ^datomic.impl.db.IDatum d)))))))))]
                              startv [(fn fn__14409
                                        ([d]
                                          (db/asserting-datum
                                            java.lang.Long/MIN_VALUE
                                            (.getA ^datomic.impl.db.IDatum d)
                                            startv
                                            2305843009213693951)))
                                      (fn fn__14411
                                        ([d]
                                          (db/windowed
                                            db
                                            (fn fn__14412
                                              ([p1__14357#]
                                                (and
                                                  (=
                                                    const_attrid
                                                    (long
                                                      (.getA ^datomic.impl.db.IDatum p1__14357#)))
                                                  (or
                                                    (nil? whilev)
                                                    (^clojure.lang.IFn whilev
                                                      (.getV
                                                        ^datomic.impl.db.IDatum p1__14357#))))))
                                            (.seekAVET
                                              ^datomic.db.IDb db
                                              ^datomic.impl.db.IDatum d))))]
                              starte [(fn fn__14417
                                        ([d]
                                          (db/asserting-datum
                                            (unchecked-long ^java.lang.Number starte)
                                            (.getA ^datomic.impl.db.IDatum d)
                                            nil
                                            2305843009213693951)))
                                      (fn fn__14419
                                        ([d]
                                          (db/windowed
                                            db
                                            (fn fn__14420
                                              ([p1__14358#]
                                                (and
                                                  (=
                                                    const_attrid
                                                    (long
                                                      (.getA ^datomic.impl.db.IDatum p1__14358#)))
                                                  (or
                                                    (nil? whilee)
                                                    (^clojure.lang.IFn whilee
                                                      (long
                                                        (.getE
                                                          ^datomic.impl.db.IDatum p1__14358#)))))))
                                            (.seekAEVT
                                              ^datomic.db.IDb db
                                              ^datomic.impl.db.IDatum d))))]
                              (and whilev const_attr (.hasAVET ^datomic.db.Attribute const_attr)) [(fn 
                                                                                                     fn__14425
                                                                                                     ([d]
                                                                                                       (db/asserting-datum
                                                                                                         java.lang.Long/MIN_VALUE
                                                                                                         (.getA
                                                                                                           ^datomic.impl.db.IDatum d)
                                                                                                         nil
                                                                                                         2305843009213693951)))
                                                                                                   (fn 
                                                                                                     fn__14427
                                                                                                     ([d]
                                                                                                       (db/windowed
                                                                                                         db
                                                                                                         (fn 
                                                                                                           fn__14428
                                                                                                           ([p1__14359#]
                                                                                                             (and
                                                                                                               (=
                                                                                                                 const_attrid
                                                                                                                 (long
                                                                                                                   (.getA
                                                                                                                     ^datomic.impl.db.IDatum p1__14359#)))
                                                                                                               (^clojure.lang.IFn whilev
                                                                                                                 (.getV
                                                                                                                   ^datomic.impl.db.IDatum p1__14359#)))))
                                                                                                         (.seekAVET
                                                                                                           ^datomic.db.IDb db
                                                                                                           ^datomic.impl.db.IDatum d))))]
                              :else (do
                                      [(fn fn__14432
                                         ([d]
                                           (db/asserting-datum
                                             java.lang.Long/MIN_VALUE
                                             (.getA ^datomic.impl.db.IDatum d)
                                             nil
                                             2305843009213693951)))
                                       (fn fn__14434
                                         ([d]
                                           (db/windowed
                                             db
                                             (fn fn__14435
                                               ([p1__14360#]
                                                 (=
                                                   (long (.getA ^datomic.impl.db.IDatum d))
                                                   (long
                                                     (.getA ^datomic.impl.db.IDatum p1__14360#)))))
                                             (.seekAEVT
                                               ^datomic.db.IDb db
                                               ^datomic.impl.db.IDatum d))))]))
                            (if (aget ^"[Ljava.lang.Object;" bound 0)
                              [(fn fn__14438
                                 ([d]
                                   (db/asserting-datum
                                     (.getE ^datomic.impl.db.IDatum d)
                                     -1
                                     nil
                                     2305843009213693951)))
                               (fn fn__14440
                                 ([d]
                                   (let [ret (db/windowed
                                               db
                                               (fn fn__14441
                                                 ([p1__14361#]
                                                   (=
                                                     (long (.getE ^datomic.impl.db.IDatum d))
                                                     (long
                                                       (.getE
                                                         ^datomic.impl.db.IDatum p1__14361#)))))
                                               (.seekEAVT
                                                 ^datomic.db.IDb db
                                                 ^datomic.impl.db.IDatum d))]
                                     ret)))]
                              (if (aget ^"[Ljava.lang.Object;" bound 2)
                                [(fn fn__14444
                                   ([d]
                                     (db/asserting-datum
                                       java.lang.Long/MIN_VALUE
                                       -1
                                       (.getV ^datomic.impl.db.IDatum d)
                                       2305843009213693951)))
                                 (fn fn__14446
                                   ([d]
                                     (db/windowed
                                       db
                                       (fn fn__14447
                                         ([p1__14362#]
                                           (=
                                             (.getV ^datomic.impl.db.IDatum d)
                                             (.getV ^datomic.impl.db.IDatum p1__14362#))))
                                       (.seekRAET ^datomic.db.IDb db ^datomic.impl.db.IDatum d))))]
                                (do
                                  (when :else
                                    (throw
                                      (java.lang.Exception.
                                        "Insufficient bindings, will cause db scan")))
                                  nil))))
               prober (nth vec__14368 (unchecked-int 0) nil)
               probe (nth vec__14368 (unchecked-int 1) nil)
               project (fn project
                         ([d y]
                           (let [ret (object-array (java.lang.Integer/valueOf (int proj_count)))]
                             (dotimes [i (alength ^"[Ljava.lang.Object;" px_from)]
                               (aset
                                 ^"[Ljava.lang.Object;" ret
                                 (unchecked-int (aget ^"[Ljava.lang.Object;" px_to (int i)))
                                 (let [G__14451 (long
                                                  (aget ^"[Ljava.lang.Object;" px_from (int i)))]
                                   (case
                                     G__14451
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
               vec__14371 (let [ht (java.util.HashMap.)
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
               ht (nth vec__14371 (unchecked-int 0) nil)
               probeset (nth vec__14371 (unchecked-int 1) nil)
               PAR 4
               probelists (let [cnt (count probeset)]
                            (if (> cnt 100)
                              (let [palist (java.util.ArrayList. ^java.util.Collection probeset)
                                    sz (if (= (rem cnt PAR) 0)
                                         (quot cnt PAR)
                                         (inc (quot cnt PAR)))]
                                (mapv
                                  (fn fn__14466
                                    ([p1__14363#]
                                      (.subList
                                        ^java.util.ArrayList palist
                                        (unchecked-int (* sz p1__14363#))
                                        (unchecked-int (min cnt (+ sz (* sz p1__14363#)))))))
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
           (qmapv (fn fn__14471 ([p1__14364#] (^clojure.lang.IFn join p1__14364# ret))) probelists)
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
              _ (loop [seq_14486 (seq join_map) chunk_14487 nil count_14488 0 i_14489 0]
                  (if (< i_14489 count_14488)
                    (let [vec__14491 (.nth
                                       ^clojure.lang.Indexed chunk_14487
                                       (unchecked-int i_14489))
                          k (nth vec__14491 (unchecked-int 0) nil)
                          v (nth vec__14491 (unchecked-int 1) nil)]
                      (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                      (recur seq_14486 chunk_14487 count_14488 (inc i_14489)))
                    (let [temp__5804__auto__ (seq seq_14486)]
                      (when temp__5804__auto__
                        (let [seq_14486 temp__5804__auto__]
                          (if (chunked-seq? seq_14486)
                            (let [c__6065__auto__ (chunk-first seq_14486)]
                              (recur
                                (chunk-rest seq_14486)
                                c__6065__auto__
                                (count c__6065__auto__)
                                0))
                            (let [vec__14494 (first seq_14486)
                                  k (nth vec__14494 (unchecked-int 0) nil)
                                  v (nth vec__14494 (unchecked-int 1) nil)]
                              (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                              (recur (next seq_14486) nil 0 0))))))))
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
          (loop [seq_14503 (seq (qmapv proc (partv PART ys)))
                 chunk_14504 nil
                 count_14505 0
                 i_14506 0]
            (if (< i_14506 count_14505)
              (let [part (.nth ^clojure.lang.Indexed chunk_14504 (unchecked-int i_14506))]
                (.addAll ^java.util.AbstractCollection rel ^java.util.Collection part)
                (recur seq_14503 chunk_14504 count_14505 (inc i_14506)))
              (let [temp__5804__auto__ (seq seq_14503)]
                (when temp__5804__auto__
                  (let [seq_14503 temp__5804__auto__]
                    (if (chunked-seq? seq_14503)
                      (let [c__6065__auto__ (chunk-first seq_14503)]
                        (recur (chunk-rest seq_14503) c__6065__auto__ (count c__6065__auto__) 0))
                      (let [part (first seq_14503)]
                        (.addAll ^java.util.AbstractCollection rel ^java.util.Collection part)
                        (recur (next seq_14503) nil 0 0))))))))
          rel)))
    (join-project
      [this ys join_map project_map_x project_map_y predctor]
      (join-project-with this ys join_map project_map_x project_map_y predctor)))
  (clojure.core/import 'datomic.datalog.FnRel)
  (defn ->FnRel ([db f arity src? consts] (datomic.datalog.FnRel. db f arity src? consts)))
  (defn fnrel
    ([db emap consts]
      (datomic.datalog.FnRel.
        db
        (:fn emap)
        (java.lang.Integer/valueOf (int (count (:argvars emap))))
        (:needs-source emap)
        consts)))
  (defn create-join-maps
    ([xbinds ybinds zbinds]
      (let [xb (zipmap xbinds (range))
            yb (zipmap ybinds (range))
            zb (zipmap zbinds (range))
            m (fn m
                ([ib jb jbinds]
                  (reduce
                    (fn fn__14517
                      ([p1__14515# p2__14514#]
                        (if (^clojure.lang.IFn ib p2__14514#)
                          (assoc
                            p1__14515#
                            (^clojure.lang.IFn ib p2__14514#)
                            (^clojure.lang.IFn jb p2__14514#))
                          p1__14515#)))
                    {}
                    jbinds)))
            join_map (^clojure.lang.IFn m xb yb ybinds)
            proj_x (^clojure.lang.IFn m xb zb zbinds)
            proj_y (^clojure.lang.IFn m yb zb zbinds)]
        [join_map proj_x proj_y])))
  (defn project
    ([xs xbinds ybinds]
      (if (= xbinds ybinds)
        xs
        (let [xb (zipmap xbinds (range)) yb (zipmap (range) ybinds) ret (java.util.HashSet.)]
          (loop [seq_14521 (seq xs) chunk_14522 nil count_14523 0 i_14524 0]
            (if (< i_14524 count_14523)
              (let [x (.nth ^clojure.lang.Indexed chunk_14522 (unchecked-int i_14524))]
                (let [tos (object-array (java.lang.Integer/valueOf (int (count ybinds))))]
                  (dotimes [i (count ybinds)]
                    (aset
                      ^"[Ljava.lang.Object;" tos
                      (int i)
                      (nth
                        x
                        (unchecked-int (^clojure.lang.IFn xb (^clojure.lang.IFn yb (long i)))))))
                  (.add ^java.util.HashSet ret (tuple tos)))
                (recur seq_14521 chunk_14522 count_14523 (inc i_14524)))
              (let [temp__5804__auto__ (seq seq_14521)]
                (when temp__5804__auto__
                  (let [seq_14521 temp__5804__auto__]
                    (if (chunked-seq? seq_14521)
                      (let [c__6065__auto__ (chunk-first seq_14521)]
                        (recur (chunk-rest seq_14521) c__6065__auto__ (count c__6065__auto__) 0))
                      (let [x (first seq_14521)]
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
                        (recur (next seq_14521) nil 0 0))))))))
          ret))))
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
        (loop [seq_14531 (seq project_map_y) chunk_14532 nil count_14533 0 i_14534 0]
          (if (< i_14534 count_14533)
            (let [vec__14535 (.nth ^clojure.lang.Indexed chunk_14532 (unchecked-int i_14534))
                  y (nth vec__14535 (unchecked-int 0) nil)
                  i (nth vec__14535 (unchecked-int 1) nil)]
              (aset ^"[Ljava.lang.Object;" invpmy (unchecked-int i) y)
              (recur seq_14531 chunk_14532 count_14533 (inc i_14534)))
            (let [temp__5804__auto__ (seq seq_14531)]
              (when temp__5804__auto__
                (let [seq_14531 temp__5804__auto__]
                  (if (chunked-seq? seq_14531)
                    (let [c__6065__auto__ (chunk-first seq_14531)]
                      (recur (chunk-rest seq_14531) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [vec__14538 (first seq_14531)
                          y (nth vec__14538 (unchecked-int 0) nil)
                          i (nth vec__14538 (unchecked-int 1) nil)]
                      (aset ^"[Ljava.lang.Object;" invpmy (unchecked-int i) y)
                      (recur (next seq_14531) nil 0 0))))))))
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
  (defn predrel
    ([db emap consts]
      (datomic.datalog.PredRel.
        db
        (:fn emap)
        (java.lang.Integer/valueOf (int (count (:argvars emap))))
        (:needs-source emap)
        consts)))
  (defn variable? ([x] (= \? (first (config/sym-name x)))))
  (defn source? ([x] (= \$ (first (config/sym-name x)))))
  (defn variable-or-blank? ([x] (or (variable? x) (= '_ x))))
  (defn attr?
    ([db x]
      (let [attrid (db/resolve-id db x)]
        (when attrid
          (instance? datomic.db.Attribute (.elementAt ^datomic.db.IDbImpl db attrid))))))
  (defn extensional? ([rules pred] (not (contains? rules pred))))
  (defn adorned-pred
    ([query bindset]
      [(first query)
       (vec
         (map (fn fn__14557 ([p1__14556#] (not (contains? bindset p1__14556#)))) (rest query)))])
    ([query] [(first query) (vec (map variable? (rest query)))]))
  (declare eval-query)
  (defn not-join-clause? ([c] (and (instance? java.util.List c) (= 'not-join (first c)))))
  (defn free-var? ([x] (variable? x)))
  (defn free-vars
    ([c] (cond (not-join-clause? c) (second c) :else (do (filter free-var? (flatten c))))))
  (defn unifying-vars
    ([p__14565]
      (let [vec__14566 p__14565
            seq__14567 (seq vec__14566)
            first__14568 (first seq__14567)
            seq__14567 (next seq__14567)
            p first__14568
            cs seq__14567
            c vec__14566
            vec__14569 (if (source? p) cs c)
            seq__14570 (seq vec__14569)
            first__14571 (first seq__14570)
            seq__14570 (next seq__14570)
            p first__14571
            cs seq__14570
            c vec__14569]
        (cond
          (#{'not-join 'or-join} p) (flatten (first cs))
          (#{'and 'not} p) (mapcat unifying-vars cs)
          (= 'or p) (let [uvs (mapv
                                (fn fn__14572 ([p1__14564#] (set (unifying-vars p1__14564#))))
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
  (defn unifying-var-set ([c] (set (unifying-vars c))))
  (defn used-srcs
    ([srcs c]
      (cond
        (not-join-clause? c) (conj (into #{} (mapcat (partial used-srcs srcs) (nnext c))) '$)
        :else (do (filter srcs (flatten c))))))
  (defn sched-in-order
    ([srcs prog p__14588 init_binds]
      (let [vec__14589 p__14588
            seq__14590 (seq vec__14589)
            first__14591 (first seq__14590)
            seq__14590 (next seq__14590)
            vec__14592 first__14591
            seq__14593 (seq vec__14592)
            first__14594 (first seq__14593)
            seq__14593 (next seq__14593)
            hpred first__14594
            hargs seq__14593
            body seq__14590
            rule vec__14589
            src (fn src ([p1__14577#] (or (:tag (meta p1__14577#)) '$)))
            pack (fn pack ([c] [(^clojure.lang.IFn src c) c]))
            unpack (fn unpack ([p1__14578#] (nth p1__14578# (unchecked-int 1))))
            cargs (fn cargs
                    ([clause]
                      (cond
                        (map? clause) (:argvars clause)
                        (not-join-clause? clause) (free-vars clause)
                        (extensional? prog (first clause)) clause
                        :else (do (next clause)))))
            extdb (fn extdb
                    ([p__14613]
                      (let [vec__14615 p__14613
                            src (nth vec__14615 (unchecked-int 0) nil)
                            c (nth vec__14615 (unchecked-int 1) nil)
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
                         ([p1__14579#]
                           (and
                             (map? p1__14579#)
                             (nil? (:argvars p1__14579#))
                             (#{:tuple :scalar} (:bind-type p1__14579#)))))
            in_clause? (fn in_clause_QMARK_
                         ([p1__14580#]
                           (and
                             (map? p1__14580#)
                             (nil? (:argvars p1__14580#))
                             (#{:rel :list} (:bind-type p1__14580#))
                             (.startsWith (name (^clojure.lang.IFn src p1__14580#)) "$__in"))))
            delay_ins (fn delay_ins
                        ([cs]
                          (let [vec__14635 (split-with in_clause? cs)
                                ins (nth vec__14635 (unchecked-int 0) nil)
                                cs (nth vec__14635 (unchecked-int 1) nil)
                                m (reduce
                                    (fn fn__14638
                                      ([m in]
                                        (let [bset (set (^clojure.lang.IFn cbinds in))
                                              uc (first
                                                   (filter
                                                     (fn fn__14639
                                                       ([p1__14581#]
                                                         (some
                                                           bset
                                                           (^clojure.lang.IFn cargs p1__14581#))))
                                                     cs))]
                                          (update-in m [uc] conj in))))
                                    {}
                                    ins)]
                            (concat
                              (reduce
                                (fn fn__14642
                                  ([ret c]
                                    (let [vec__14643 (find m c)
                                          k (nth vec__14643 (unchecked-int 0) nil)
                                          vs (nth vec__14643 (unchecked-int 1) nil)]
                                      (if (identical? k c)
                                        (into (conj ret (first vs) c) (next vs))
                                        (conj ret c)))))
                                []
                                cs)
                              (get m nil)))))
            pred? (fn pred_QMARK_
                    ([p1__14582#] (and (map? p1__14582#) (nil? (:binds p1__14582#)))))
            vec__14595 (common/split-filter pred? body)
            preds (nth vec__14595 (unchecked-int 0) nil)
            npreds (nth vec__14595 (unchecked-int 1) nil)
            vec__14598 (common/split-filter not-join-clause? npreds)
            njcs (nth vec__14598 (unchecked-int 0) nil)
            npreds (nth vec__14598 (unchecked-int 1) nil)
            vec__14601 (common/split-filter sv_clause? npreds)
            svs (nth vec__14601 (unchecked-int 0) nil)
            npreds (nth vec__14601 (unchecked-int 1) nil)
            body (concat svs preds njcs (^clojure.lang.IFn delay_ins npreds))
            reqcnt (fn reqcnt
                     ([clause]
                       (when (contains? prog (first clause))
                         (let [vec__14652 (find prog (first clause))
                               p (nth vec__14652 (unchecked-int 0) nil)]
                           (:reqcnt (meta p))))))
            underbound? (fn underbound_QMARK_
                          ([n bindings pc]
                            (let [clause (^clojure.lang.IFn unpack pc)
                                  args (^clojure.lang.IFn cargs clause)]
                              (cond
                                (or (not-join-clause? clause) (map? clause)) (some
                                                                               (fn 
                                                                                 fn__14657
                                                                                 ([p1__14583#]
                                                                                   (and
                                                                                     (variable?
                                                                                       p1__14583#)
                                                                                     (not
                                                                                       (contains?
                                                                                         bindings
                                                                                         p1__14583#)))))
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
                                                                            fn__14660
                                                                            ([p1__14584#]
                                                                              (contains?
                                                                                bindings
                                                                                p1__14584#)))
                                                                          (take rcnt args)))))
                                                                  (and
                                                                    (= hpred (first clause))
                                                                    (not
                                                                      (some
                                                                        (fn 
                                                                          fn__14662
                                                                          ([p1__14585#]
                                                                            (contains?
                                                                              bindings
                                                                              p1__14585#)))
                                                                        (filter variable? args)))))
                                :else (do
                                        (let [temp__5804__auto__ (^clojure.lang.IFn extdb pc)]
                                          (when temp__5804__auto__
                                            (let [db temp__5804__auto__]
                                              (not
                                                (some
                                                  (fn fn__14664
                                                    ([p1__14586#]
                                                      (or
                                                        (not (variable? p1__14586#))
                                                        (contains? bindings p1__14586#))))
                                                  (take n clause)))))))))))
            clauses (loop [clauses [] bindings (set init_binds) remclauses (map pack body)]
                      (if (empty? remclauses)
                        clauses
                        (let [vec__14674 (split-with (partial underbound? 2 bindings) remclauses)
                              skip (nth vec__14674 (unchecked-int 0) nil)
                              ready (nth vec__14674 (unchecked-int 1) nil)
                              vec__14677 (if (empty? ready)
                                           (split-with (partial underbound? 3 bindings) remclauses)
                                           [skip ready])
                              skip (nth vec__14677 (unchecked-int 0) nil)
                              ready (nth vec__14677 (unchecked-int 1) nil)
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
                    (fn fn__14681 ([p1__14587#] (set (^clojure.lang.IFn cbinds p1__14587#))))
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
  (defn push-preds
    ([srcs sched]
      (let [pred? (fn pred_QMARK_
                    ([p1__14684#] (and (map? p1__14684#) (nil? (:binds p1__14684#)))))
            ctor (fn ctor
                   ([inbinds p__14691]
                     (let [map__14693 p__14691
                           map__14693 (if (seq? map__14693)
                                        (if (next map__14693)
                                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                            (to-array map__14693))
                                          (if (seq map__14693) (first map__14693) {}))
                                        map__14693)
                           clause map__14693
                           f (get map__14693 :fn)
                           argvars (get map__14693 :argvars)
                           needs_source (get map__14693 :needs-source)
                           binds (get map__14693 :binds)
                           params (concat argvars binds)
                           arity (count params)
                           consts (vec
                                    (map
                                      (fn fn__14697
                                        ([p1__14685#]
                                          (when-not (variable-or-blank? p1__14685#) p1__14685#)))
                                      params))
                           vec__14694 (create-join-maps params inbinds [])
                           join_map (nth vec__14694 (unchecked-int 0) nil)
                           _ (nth vec__14694 (unchecked-int 1) nil)
                           _ (nth vec__14694 (unchecked-int 2) nil)
                           src (when needs_source (get srcs (or (:tag (meta clause)) '$)))]
                       (query-stats/acc-with-clause-stats! :preds clause)
                       (fn fn__14699
                         ([]
                           (let [args (object-array (java.lang.Integer/valueOf (int arity)))]
                             (fn fn__14700
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
                      G__14712 (seq sched)
                      vec__14713 G__14712
                      seq__14714 (seq vec__14713)
                      first__14715 (first seq__14714)
                      seq__14714 (next seq__14714)
                      vec__14716 first__14715
                      clause (nth vec__14716 (unchecked-int 0) nil)
                      outbinds (nth vec__14716 (unchecked-int 1) nil)
                      clauses seq__14714
                      sched vec__14713]
                  (loop [ret ret G__14712 G__14712]
                    (let [ret ret
                          vec__14720 G__14712
                          seq__14721 (seq vec__14720)
                          first__14722 (first seq__14721)
                          seq__14721 (next seq__14721)
                          vec__14723 first__14722
                          clause (nth vec__14723 (unchecked-int 0) nil)
                          outbinds (nth vec__14723 (unchecked-int 1) nil)
                          clauses seq__14721
                          sched vec__14720]
                      (if sched
                        (if (^clojure.lang.IFn pred? clause)
                          (recur (conj ret [clause outbinds nil]) (seq clauses))
                          (let [vec__14726 (split-with (comp pred? first) clauses)
                                preds (nth vec__14726 (unchecked-int 0) nil)
                                clauses (nth vec__14726 (unchecked-int 1) nil)]
                            (recur
                              (conj
                                ret
                                [clause
                                 outbinds
                                 (if (empty? preds)
                                   truep
                                   (let [ctors (map
                                                 (fn fn__14729
                                                   ([p1__14686#]
                                                     (^clojure.lang.IFn ctor
                                                       outbinds
                                                       (first p1__14686#))))
                                                 preds)]
                                     (fn fn__14731
                                       ([]
                                         (apply
                                           every-pred
                                           (map
                                             (fn fn__14732
                                               ([p1__14687#] (^clojure.lang.IFn p1__14687#)))
                                             ctors))))))])
                              (seq clauses))))
                        ret))))]
        ret)))
  (defn recursive?
    ([prog pred seen]
      (if (or (map? pred) (extensional? prog pred))
        false
        (or
          (contains? seen pred)
          (let [seen (conj seen pred) rules (get prog pred)]
            (some
              (fn fn__14741 ([p1__14740#] (recursive? prog p1__14740# seen)))
              (map first (remove map? (mapcat rest rules))))))))
    ([prog pred] (recursive? prog pred #{})))
  (defn rules? ([x] (= \% (first (config/sym-name x)))))
  (defn scalar->rel ([x] (if (nil? x) [] [[x]])))
  (defn tuple->rel ([x] (if (nil? x) [] [x])))
  (defn compile-expr-clause
    ([sources vars expr bind_type binds]
      (let [params (vec (concat sources vars))
            retlen (+ (count vars) (count binds))
            gret (gensym)
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
                                      (apply
                                        vector
                                        (seq (concat (clojure.core/list 't__14749__auto__)))))
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
                                                                     iter__14751
                                                                     ([s__14752]
                                                                       (lazy-seq
                                                                         (let 
                                                                           [s__14752 s__14752
                                                                            temp__5804__auto__
                                                                            (seq s__14752)]
                                                                           (when
                                                                             temp__5804__auto__
                                                                             (let 
                                                                               [s__14752
                                                                                temp__5804__auto__]
                                                                               (if
                                                                                 (chunked-seq?
                                                                                   s__14752)
                                                                                 (let 
                                                                                   [c__6371__auto__
                                                                                    (chunk-first
                                                                                      s__14752)
                                                                                    size__6372__auto__
                                                                                    (count
                                                                                      c__6371__auto__)
                                                                                    b__14754
                                                                                    (chunk-buffer
                                                                                      (java.lang.Integer/valueOf
                                                                                        (int
                                                                                          size__6372__auto__)))]
                                                                                   (if
                                                                                     (loop 
                                                                                       [i__14753 0]
                                                                                       (if
                                                                                         (<
                                                                                           i__14753
                                                                                           size__6372__auto__)
                                                                                         (let 
                                                                                           [i
                                                                                            (.nth
                                                                                              ^clojure.lang.Indexed c__6371__auto__
                                                                                              (unchecked-int
                                                                                                i__14753))]
                                                                                           (chunk-append
                                                                                             b__14754
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
                                                                                               i__14753)))
                                                                                         true))
                                                                                     (chunk-cons
                                                                                       (chunk
                                                                                         b__14754)
                                                                                       (^clojure.lang.IFn iter__14751
                                                                                         (chunk-rest
                                                                                           s__14752)))
                                                                                     (chunk-cons
                                                                                       (chunk
                                                                                         b__14754)
                                                                                       nil)))
                                                                                 (let 
                                                                                   [i
                                                                                    (first
                                                                                      s__14752)]
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
                                                                                     (^clojure.lang.IFn iter__14751
                                                                                       (rest
                                                                                         s__14752)))))))))))]
                                            (^clojure.lang.IFn iter__6373__auto__
                                              (range
                                                (java.lang.Integer/valueOf (int (count vars))))))
                                          (clojure.core/list
                                            (seq
                                              (concat
                                                (clojure.core/list 'clojure.core/dotimes)
                                                (clojure.core/list
                                                  (apply
                                                    vector
                                                    (seq
                                                      (concat
                                                        (clojure.core/list 'i__14750__auto__)
                                                        (clojure.core/list
                                                          (java.lang.Integer/valueOf
                                                            (int (count binds))))))))
                                                (clojure.core/list
                                                  (seq
                                                    (concat
                                                      (clojure.core/list 'clojure.core/aset)
                                                      (clojure.core/list gret)
                                                      (clojure.core/list
                                                        (seq
                                                          (concat
                                                            (clojure.core/list 'clojure.core/+)
                                                            (clojure.core/list 'i__14750__auto__)
                                                            (clojure.core/list
                                                              (java.lang.Integer/valueOf
                                                                (int (count vars)))))))
                                                      (clojure.core/list
                                                        (seq
                                                          (concat
                                                            (clojure.core/list 'clojure.core/nth)
                                                            (clojure.core/list 't__14749__auto__)
                                                            (clojure.core/list
                                                              'i__14750__auto__))))))))))
                                          (clojure.core/list
                                            (seq
                                              (concat
                                                (clojure.core/list 'datomic.datalog/tuple)
                                                (clojure.core/list gret))))))))))
                              (clojure.core/list
                                (let [G__14764 bind_type]
                                  (case
                                    G__14764
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
  (defn expr-clause
    ([p__14771]
      (let [vec__14772 p__14771
            call (nth vec__14772 (unchecked-int 0) nil)
            binds (nth vec__14772 (unchecked-int 1) nil)
            xtra (nth vec__14772 (unchecked-int 2) nil)
            clause vec__14772]
        (if (instance? java.util.List call)
          (let [vec__14775 (list* call)
                seq__14776 (seq vec__14775)
                first__14777 (first seq__14776)
                seq__14776 (next seq__14776)
                f first__14777
                body seq__14776
                expr vec__14775
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
                          (let [G__14778 bind_type]
                            (case
                              G__14778
                              :scalar
                              [binds]
                              :tuple
                              binds
                              :list
                              [(first binds)]
                              :rel
                              (first binds)))))
                needs_source (not (nil? sources))
                f (if (and (= f 'ground) (nil? vars) needs_source)
                    (let [G__14779 bind_type]
                      (case
                        G__14779
                        :scalar
                        (fn fn__14780 ([p1__14768#] (vector (vector p1__14768#))))
                        :tuple
                        (fn fn__14782 ([p1__14770#] (vector p1__14770#)))
                        :list
                        (fn fn__14784 ([p1__14769#] (mapv vector p1__14769#)))
                        :rel
                        identity))
                    (compile-expr-clause sources vars expr bind_type binds))]
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
  (defn lift-consts-from-preds
    ([clauses]
      (let [vec__14791 (reduce
                         (fn fn__14796
                           ([p__14794 p__14795]
                             (let [vec__14797 p__14794
                                   m (nth vec__14797 (unchecked-int 0) nil)
                                   cs (nth vec__14797 (unchecked-int 1) nil)
                                   vec__14800 p__14795
                                   seq__14801 (seq vec__14800)
                                   first__14802 (first seq__14801)
                                   seq__14801 (next seq__14801)
                                   pred first__14802
                                   args seq__14801
                                   c vec__14800]
                               (if (and
                                     (symbol? pred)
                                     (not= 'not-join pred)
                                     (not (variable-or-blank? pred))
                                     (not (.startsWith (name pred) "$"))
                                     (not (every? variable-or-blank? args)))
                                 (let [vec__14803 (reduce
                                                    (fn fn__14807
                                                      ([p__14806 arg]
                                                        (let [vec__14808 p__14806
                                                              m
                                                              (nth
                                                                vec__14808
                                                                (unchecked-int 0)
                                                                nil)
                                                              args
                                                              (nth
                                                                vec__14808
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
                                       m (nth vec__14803 (unchecked-int 0) nil)
                                       args (nth vec__14803 (unchecked-int 1) nil)]
                                   [m (conj cs (cons pred args))])
                                 [m (conj cs c)]))))
                         [{} []]
                         clauses)
            subs (nth vec__14791 (unchecked-int 0) nil)
            new_clauses (nth vec__14791 (unchecked-int 1) nil)]
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
  (defn all-pred
    ([p__14818]
      (let [vec__14819 p__14818
            seq__14820 (seq vec__14819)
            first__14821 (first seq__14820)
            seq__14820 (next seq__14820)
            p first__14821
            cs seq__14820
            c vec__14819]
        (cond
          (#{'and 'not 'or} p) (every? all-pred cs)
          (and (instance? java.util.List p) (nil? cs)) (do true)))))
  (defn to-pred
    ([p__14824]
      (let [vec__14825 p__14824
            seq__14826 (seq vec__14825)
            first__14827 (first seq__14826)
            seq__14826 (next seq__14826)
            p first__14827
            cs seq__14826
            c vec__14825]
        (cond
          (#{'and 'or} p) (cons p (map to-pred cs))
          (= 'not p) (clojure.core/list p (list* 'and (map to-pred cs)))
          :else (do p)))))
  (defn not-or-all-pred
    ([p__14829]
      (let [vec__14830 p__14829
            seq__14831 (seq vec__14830)
            first__14832 (first seq__14831)
            seq__14831 (next seq__14831)
            p first__14832
            cs seq__14831
            c vec__14830]
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
  (defn not-or->not-or-join
    ([p__14835]
      (let [vec__14836 p__14835
            seq__14837 (seq vec__14836)
            first__14838 (first seq__14837)
            seq__14837 (next seq__14837)
            p first__14838
            cs seq__14837
            c vec__14836]
        (if (#{'not 'or} p)
          (let [vs (vec (unifying-var-set c))]
            (with-meta (list* ({'not 'not-join, 'or 'or-join} p) vs cs) (meta c)))
          c))))
  (defn normalize-or-join
    ([p__14841]
      (let [vec__14842 p__14841
            seq__14843 (seq vec__14842)
            first__14844 (first seq__14843)
            seq__14843 (next seq__14843)
            p first__14844
            first__14844 (first seq__14843)
            seq__14843 (next seq__14843)
            vs first__14844
            cs seq__14843
            c vec__14842]
        (if (= 'or-join p)
          (let [cs (map
                     (fn fn__14845
                       ([p1__14840#]
                         (if (= 'and (first p1__14840#))
                           (rest p1__14840#)
                           (clojure.core/list p1__14840#))))
                     cs)]
            (when (empty? vs)
              (error/arg
                :db.error/or-binding-empty
                (str "'or' cannot have empty binding set: " c)))
            (with-meta (list* p vs cs) (meta c)))
          c))))
  (defn or-join->rule-preds
    ([p__14848]
      (let [vec__14849 p__14848
            seq__14850 (seq vec__14849)
            first__14851 (first seq__14850)
            seq__14850 (next seq__14850)
            p first__14851
            first__14851 (first seq__14850)
            seq__14850 (next seq__14850)
            vs first__14851
            cs seq__14850
            rname (gensym "arule__")
            head (cons rname vs)]
        (mapv (partial cons (with-meta head #:query-stats{:clause (list* p [vs])})) cs))))
  (declare add-rule)
  (defn prep-clauses
    ([rm clauses]
      (let [clauses (map normalize-or-join (map not-or->not-or-join (map not-or-all-pred clauses)))
            vec__14853 (reduce
                         (fn fn__14857
                           ([p__14856 c]
                             (let [vec__14858 p__14856
                                   rm (nth vec__14858 (unchecked-int 0) nil)
                                   cs (nth vec__14858 (unchecked-int 1) nil)]
                               (if (= 'or-join (first c))
                                 (let [preds (or-join->rule-preds c)
                                       vec__14861 (ffirst preds)
                                       seq__14862 (seq vec__14861)
                                       first__14863 (first seq__14862)
                                       seq__14862 (next seq__14862)
                                       rname first__14863
                                       args seq__14862
                                       head vec__14861
                                       rm (add-rule rm rname preds)]
                                   [rm
                                    (conj
                                      cs
                                      (with-meta (flatten head) (merge (meta c) (meta head))))])
                                 [rm (conj cs c)]))))
                         [rm []]
                         clauses)
            rm (nth vec__14853 (unchecked-int 0) nil)
            clauses (nth vec__14853 (unchecked-int 1) nil)]
        [rm (mapv expr-clause (lift-consts-from-preds clauses))])))
  (defn callees
    ([x]
      (if (list? x)
        (cons (first x) (mapcat callees (rest x)))
        (let [G__14866 (data/equality-partition x)]
          (case
            G__14866
            (:sequential :set)
            (mapcat callees x)
            :map
            (concat (mapcat (callees (keys x))) (mapcat (callees (vals x))))
            :atom
            nil)))))
  (defn add-rule
    ([rm rname preds]
      (let [heads (map first preds)
            rcnt (fn rcnt
                   ([p__14871]
                     (let [vec__14873 p__14871
                           seq__14874 (seq vec__14873)
                           first__14875 (first seq__14874)
                           seq__14874 (next seq__14874)
                           p first__14875
                           first__14875 (first seq__14874)
                           seq__14874 (next seq__14874)
                           r first__14875
                           xs seq__14874]
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
            vec__14868 (reduce
                         (fn fn__14880
                           ([p__14878 p__14879]
                             (let [vec__14881 p__14878
                                   rm (nth vec__14881 (unchecked-int 0) nil)
                                   ps (nth vec__14881 (unchecked-int 1) nil)
                                   vec__14884 p__14879
                                   seq__14885 (seq vec__14884)
                                   first__14886 (first seq__14885)
                                   seq__14885 (next seq__14885)
                                   head first__14886
                                   clauses seq__14885
                                   pred vec__14884
                                   vec__14887 (prep-clauses rm clauses)
                                   rm (nth vec__14887 (unchecked-int 0) nil)
                                   cs (nth vec__14887 (unchecked-int 1) nil)]
                               [rm (conj ps (cons (vec (flatten head)) cs))])))
                         [rm []]
                         preds)
            rm (nth vec__14868 (unchecked-int 0) nil)
            preds (nth vec__14868 (unchecked-int 1) nil)]
        (assoc rm rname (vec preds)))))
  (defn rule-map
    ([rules]
      (let [rules (if (string? rules) (binding [*read-eval* false] (read-string rules)) rules)
            rules (group-by ffirst rules)
            rules (reduce-kv add-rule {} rules)]
        (loop [seq_14894 (seq rules) chunk_14895 nil count_14896 0 i_14897 0]
          (if (< i_14897 count_14896)
            (let [vec__14898 (.nth ^clojure.lang.Indexed chunk_14895 (unchecked-int i_14897))
                  k (nth vec__14898 (unchecked-int 0) nil)
                  v (nth vec__14898 (unchecked-int 1) nil)]
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
              (recur seq_14894 chunk_14895 count_14896 (inc i_14897)))
            (let [temp__5804__auto__ (seq seq_14894)]
              (when temp__5804__auto__
                (let [seq_14894 temp__5804__auto__]
                  (if (chunked-seq? seq_14894)
                    (let [c__6065__auto__ (chunk-first seq_14894)]
                      (recur (chunk-rest seq_14894) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [vec__14901 (first seq_14894)
                          k (nth vec__14901 (unchecked-int 0) nil)
                          v (nth vec__14901 (unchecked-int 1) nil)]
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
                      (recur (next seq_14894) nil 0 0))))))))
        rules)))
  (def rule-cache (cache/create-computing rule-map 1000))
  (def q (clojure.lang.RT/var "datomic.query" "q"))
  (defn eval-not-join
    ([srcs prog inrel inbinds p__14908]
      (let [vec__14909 p__14908
            seq__14910 (seq vec__14909)
            first__14911 (first seq__14910)
            seq__14910 (next seq__14910)
            _ first__14911
            first__14911 (first seq__14910)
            seq__14910 (next seq__14910)
            vars first__14911
            cs seq__14910
            not_join_clause vec__14909
            used (used-srcs srcs not_join_clause)
            dbs (select-keys srcs used)
            csrc (:tag (meta not_join_clause))
            dbs (cond-> dbs csrc (assoc '$ (get srcs csrc)))
            uvs (set vars)
            binds (map
                    (fn fn__14913
                      ([p1__14907#] (or (^clojure.lang.IFn uvs p1__14907#) (gensym "?nb__"))))
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
            G__14916 (java.util.HashSet. ^java.util.Collection inrel)]
        (.removeAll ^java.util.AbstractSet G__14916 (q query (conj (vec (vals dbs)) prog inrel)))
        G__14916)))
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
                       (fn fn__14924
                         ([p1__14918#]
                           (if (variable-or-blank? p1__14918#)
                             (some-> top_bounds (:consts) (^clojure.lang.IFn p1__14918#))
                             p1__14918#)))
                       args)
              starts (mapv
                       (fn fn__14927 ([p1__14919#] (some-> top_bounds (:starts) (get p1__14919#))))
                       args)
              whiles (mapv
                       (fn fn__14930 ([p1__14920#] (some-> top_bounds (:whiles) (get p1__14920#))))
                       args)
              vec__14921 (create-join-maps inbinds args next_binds)
              join (nth vec__14921 (unchecked-int 0) nil)
              projx (nth vec__14921 (unchecked-int 1) nil)
              projy (nth vec__14921 (unchecked-int 2) nil)
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
                                (fn fn__14934
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
  (defn remap-bounds
    ([nb args]
      (let [ia (zipmap (range) args)
            mapize (fn mapize
                     ([p1__14948#]
                       (into
                         {}
                         (map-indexed
                           (fn fn__14950 ([i c] (when-not (nil? c) [(^clojure.lang.IFn ia i) c])))
                           p1__14948#))))]
        {:consts (^clojure.lang.IFn mapize (:consts nb)),
         :starts (^clojure.lang.IFn mapize (:starts nb)),
         :whiles (^clojure.lang.IFn mapize (:whiles nb))})))
  (defn eval-rule
    ([db prog oprog p__14954 p__14955 inrel sched_fn src ans ins top_bounds nested_bounds]
      (let [vec__14956 p__14954
            seq__14957 (seq vec__14956)
            first__14958 (first seq__14957)
            seq__14957 (next seq__14957)
            head first__14958
            body seq__14957
            rule vec__14956
            vec__14959 p__14955
            pred (nth vec__14959 (unchecked-int 0) nil)
            adorn (nth vec__14959 (unchecked-int 1) nil)
            apred vec__14959
            vec__14962 head
            seq__14963 (seq vec__14962)
            first__14964 (first seq__14963)
            seq__14963 (next seq__14963)
            hpred first__14964
            hargs seq__14963
            aresk [src pred]
            inbinds (keep-indexed
                      (fn fn__14965 ([i a] (when-not (^clojure.lang.IFn adorn i) a)))
                      hargs)
            multi? (and (map? db) (not (instance? datomic.db.IDb db)))
            srcs (if multi? db {'$ db})
            cbs (push-preds srcs (^clojure.lang.IFn sched_fn srcs prog rule inbinds))
            top_bounds (if nested_bounds (remap-bounds nested_bounds hargs) top_bounds)
            res (try
                  (loop [sbinds inbinds sup inrel cbs cbs]
                    (if cbs
                      (let [vec__14968 (first cbs)
                            c (nth vec__14968 (unchecked-int 0) nil)
                            next_binds (nth vec__14968 (unchecked-int 1) nil)
                            predctor (nth vec__14968 (unchecked-int 2) nil)
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
                                   (fn fn__14971
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
  (defn eval-query
    ([db prog oprog p__14978 input sched_fn src ans ins top_bounds nested_bounds]
      (let [vec__14979 p__14978
            pred (nth vec__14979 (unchecked-int 0) nil)
            adorn (nth vec__14979 (unchecked-int 1) nil)
            apred vec__14979
            iresk [src apred]
            inpred (get ins iresk (java.util.HashSet.))
            input (java.util.HashSet. ^java.util.Collection input)]
        (.removeAll ^java.util.AbstractSet input ^java.util.Collection inpred)
        (when (or (not (.isEmpty ^java.util.HashSet input)) (every? identity adorn))
          (let [rules (get prog pred)]
            (.addAll ^java.util.Set inpred ^java.util.Collection input)
            (.put ^java.util.Map ins iresk inpred)
            (loop [seq_14982 (seq rules) chunk_14983 nil count_14984 0 i_14985 0]
              (if (< i_14985 count_14984)
                (let [rule (.nth ^clojure.lang.Indexed chunk_14983 (unchecked-int i_14985))]
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
                  (recur seq_14982 chunk_14983 count_14984 (inc i_14985)))
                (let [temp__5804__auto__ (seq seq_14982)]
                  (when temp__5804__auto__
                    (let [seq_14982 temp__5804__auto__]
                      (if (chunked-seq? seq_14982)
                        (let [c__6065__auto__ (chunk-first seq_14982)]
                          (recur (chunk-rest seq_14982) c__6065__auto__ (count c__6065__auto__) 0))
                        (let [rule (first seq_14982)]
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
                          (recur (next seq_14982) nil 0 0))))))))))
        nil)))
  (defn bound-consts
    ([srcs query]
      (reduce-kv
        (fn fn__14990
          ([m v b]
            (assoc
              m
              v
              (if (vector? b)
                (let [vec__14991 b
                      src (nth vec__14991 (unchecked-int 0) nil)
                      idx (nth vec__14991 (unchecked-int 1) nil)]
                  (nth (get srcs src) (unchecked-int ^java.lang.Number idx)))
                (get srcs b)))))
        {}
        (:in-consts query))))
  (defn ranges
    ([in_consts query]
      [(reduce-kv
         (fn fn__14997 ([m v c] (assoc m v (if (variable? c) (get in_consts c) c))))
         {}
         (:range-starts query))
       (let [cmps {'= =, '< ext/<, '<= ext/<=}]
         (reduce-kv
           (fn fn__15000
             ([m v p__14999]
               (let [vec__15001 p__14999
                     cmpsym (nth vec__15001 (unchecked-int 0) nil)
                     cb (nth vec__15001 (unchecked-int 1) nil)
                     c (if (variable? cb) (get in_consts cb) cb)
                     cmp (^clojure.lang.IFn cmps cmpsym)]
                 (assoc m v (fn fn__15004 ([p1__14996#] (^clojure.lang.IFn cmp p1__14996# c)))))))
           {}
           (:range-whiles query)))]))
  (defn qsqr
    ([db query sched_fn]
      (let [map__15008 query
            map__15008 (if (seq? map__15008)
                         (if (next map__15008)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15008))
                           (if (seq map__15008) (first map__15008) {}))
                         map__15008)
            from (get map__15008 :in)
            pargs (get map__15008 :find)
            clauses (get map__15008 :where)
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
            vec__15009 (ranges in_consts query)
            range_starts (nth vec__15009 (unchecked-int 0) nil)
            range_whiles (nth vec__15009 (unchecked-int 1) nil)
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
                (fn fn__15014
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
  (defn rel-fn
    ([f] (fn fn__15019 ([_ & args] (let [ret (apply f args)] [(conj (into [] args) ret)])))))
  (defn rel-pred ([f] (fn fn__15022 ([_ & args] (apply f args))))))
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
      (let [temp__5457__auto__ (deref *cancel*)]
        (when temp__5457__auto__
          (let [why temp__5457__auto__]
            (throw (java.util.concurrent.TimeoutException. (str "Query canceled: " why))))
          nil))))
  (defonce IJoin {})
  (defprotocol
    IJoin
    (join-project [xs ys join-map project-map-x project-map-y predctor])
    (join-project-with [ys xs join-map project-map-x project-map-y predctor]))
  (defn truep ([] (fn fn__18104 ([_] true))))
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
      (fn fn__18109
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
      (fn fn__18112
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
      (fn fn__18115
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
                      _ (loop [seq_18127 (seq join_map) chunk_18128 nil count_18129 0 i_18130 0]
                          (if (< i_18130 count_18129)
                            (let [vec__18132 (.nth
                                               ^clojure.lang.Indexed chunk_18128
                                               (unchecked-int i_18130))
                                  k (nth vec__18132 (unchecked-int 0) nil)
                                  v (nth vec__18132 (unchecked-int 1) nil)]
                              (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                              (recur seq_18127 chunk_18128 count_18129 (inc i_18130)))
                            (let [temp__5457__auto__ (seq seq_18127)]
                              (when temp__5457__auto__
                                (let [seq_18127 temp__5457__auto__]
                                  (if (chunked-seq? seq_18127)
                                    (let [c__5719__auto__ (chunk-first seq_18127)]
                                      (recur
                                        (chunk-rest seq_18127)
                                        c__5719__auto__
                                        (count c__5719__auto__)
                                        0))
                                    (let [vec__18135 (first seq_18127)
                                          k (nth vec__18135 (unchecked-int 0) nil)
                                          v (nth vec__18135 (unchecked-int 1) nil)]
                                      (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                                      (recur (next seq_18127) nil 0 0))))))))
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
                                   (while
                                     (.hasNext ^java.util.Iterator xiter)
                                     (let [x (.next ^java.util.Iterator xiter)
                                           temp__5457__auto__ (.get
                                                                ^java.util.Map ht
                                                                (^clojure.lang.IFn hashx x))]
                                       (when temp__5457__auto__
                                         (let [ys temp__5457__auto__ yiter (iterator ys)]
                                           (while
                                             (.hasNext ^java.util.Iterator yiter)
                                             (let [y (.next ^java.util.Iterator yiter)]
                                               (when (^clojure.lang.IFn match? x y)
                                                 (let [p (^clojure.lang.IFn project x y)]
                                                   (when (^clojure.lang.IFn pred p)
                                                     (.add ^java.util.Set ret p))))))))))
                                   (while
                                     (.hasNext ^java.util.Iterator xiter)
                                     (let [x (.next ^java.util.Iterator xiter) yiter (iterator ys)]
                                       (while
                                         (.hasNext ^java.util.Iterator yiter)
                                         (let [y (.next ^java.util.Iterator yiter)]
                                           (when (^clojure.lang.IFn match? x y)
                                             (let [p (^clojure.lang.IFn project x y)]
                                               (when (^clojure.lang.IFn pred p)
                                                 (.add ^java.util.Set ret p))))))))))
                               nil))]
                  (qmapv proc (partv PART xs))
                  ret)))))
  (extend
    java.util.Collection
    IJoin
    {:join-project
     (fn fn__18156
       ([xs ys join_map project_map_x project_map_y predctor]
         (join-project-coll xs ys join_map project_map_x project_map_y predctor))),
     :join-project-with
     (fn fn__18158
       ([xs ys join_map project_map_x project_map_y predctor]
         (join-project-coll-with xs ys join_map project_map_x project_map_y predctor)))})
  (extend
    java.lang.Object
    IJoin
    {:join-project
     (fn fn__18160
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
     (fn fn__18162
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
    ([db p__18171 starts whiles]
      (let [vec__18172 p__18171
            e (nth vec__18172 (unchecked-int 0) nil)
            a (nth vec__18172 (unchecked-int 1) nil)
            v (nth vec__18172 (unchecked-int 2) nil)
            t (nth vec__18172 (unchecked-int 3) nil)
            added (nth vec__18172 (unchecked-int 4) nil)
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
          (fn fn__18203
            ([p1__18202#]
              (loop [i 0]
                (if (< i (count consts))
                  (if (or
                        (nil? (nth consts (unchecked-int i)))
                        (= (nth p1__18202# (unchecked-int i)) (nth consts (unchecked-int i))))
                    (recur (inc i))
                    false)
                  true))))
          src))))
  (extend
    nil
    ExtRel
    {:extrel
     (fn fn__18207
       ([src consts _ _]
         (error/arg
           :db.error/invalid-data-source
           "Nil or missing data source. Did you forget to pass a database argument?"
           {:input src})))})
  (extend
    java.lang.Object
    ExtRel
    {:extrel
     (fn fn__18209
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
    {:extrel (fn fn__18211 ([src consts starts whiles] (dbrel src consts starts whiles)))})
  (extend
    java.util.Map
    ExtRel
    {:extrel (fn fn__18213 ([src consts _ _] (extrel (seq src) consts nil nil)))})
  (extend
    java.util.Collection
    ExtRel
    {:extrel (fn fn__18215 ([src consts _ _] (extrel-coll src consts)))})
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
     (fn fn__18231
       ([dbrel ys join_map project_map_x project_map_y predctor]
         (join-project-with dbrel ys join_map project_map_x project_map_y predctor))),
     :join-project-with
     (fn fn__18233
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
                          (map (fn fn__18240 ([p1__18217#] (get join_map p1__18217#))) (range 5)))
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
                              (let [temp__5457__auto__ (get consts 3)]
                                (when temp__5457__auto__ (let [t temp__5457__auto__] t)))
                              (if (aget ^"[Ljava.lang.Object;" bindings 3)
                                (nth y (unchecked-int (aget ^"[Ljava.lang.Object;" bindings 3)))
                                2305843009213693951))))))
               vec__18234 (cond
                            (aget ^"[Ljava.lang.Object;" bound 1) (cond
                                                                    (aget
                                                                      ^"[Ljava.lang.Object;" bound
                                                                      0)
                                                                    [(fn 
                                                                       fn__18251
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
                                                                       fn__18253
                                                                       ([d]
                                                                         (db/windowed
                                                                           db
                                                                           (fn 
                                                                             fn__18254
                                                                             ([p1__18218#]
                                                                               (and
                                                                                 (=
                                                                                   (long
                                                                                     (.getE
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getE
                                                                                       ^datomic.impl.db.IDatum p1__18218#)))
                                                                                 (=
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__18218#)))
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
                                                                                         ^datomic.impl.db.IDatum p1__18218#))
                                                                                     (^clojure.lang.IFn whilev
                                                                                       (.getV
                                                                                         ^datomic.impl.db.IDatum p1__18218#)))))))
                                                                           (.seekAEVT
                                                                             ^datomic.db.IDb db
                                                                             ^datomic.impl.db.IDatum d))))]
                                                                    (aget
                                                                      ^"[Ljava.lang.Object;" bound
                                                                      2)
                                                                    [(fn 
                                                                       fn__18261
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           java.lang.Long/MIN_VALUE
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           (.getV
                                                                             ^datomic.impl.db.IDatum d)
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__18263
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
                                                                                   fn__18264
                                                                                   ([p1__18219#]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum p1__18219#)))
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__18219#)))))
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
                                                                                   fn__18267
                                                                                   ([p1__18220#]
                                                                                     (and
                                                                                       (=
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum d))
                                                                                         (long
                                                                                           (.getA
                                                                                             ^datomic.impl.db.IDatum p1__18220#)))
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__18220#)))))
                                                                                 (.seekRAET
                                                                                   ^datomic.db.IDb db
                                                                                   ^datomic.impl.db.IDatum d))
                                                                               :else
                                                                               (do
                                                                                 (iter/filter
                                                                                   (fn 
                                                                                     fn__18270
                                                                                     ([p1__18221#]
                                                                                       (=
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum d)
                                                                                         (.getV
                                                                                           ^datomic.impl.db.IDatum p1__18221#))))
                                                                                   (db/windowed
                                                                                     db
                                                                                     (fn 
                                                                                       fn__18272
                                                                                       ([p1__18222#]
                                                                                         (=
                                                                                           (long
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum d))
                                                                                           (long
                                                                                             (.getA
                                                                                               ^datomic.impl.db.IDatum p1__18222#)))))
                                                                                     (.seekAEVT
                                                                                       ^datomic.db.IDb db
                                                                                       ^datomic.impl.db.IDatum d)))))))))]
                                                                    startv
                                                                    [(fn 
                                                                       fn__18275
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           java.lang.Long/MIN_VALUE
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           startv
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__18277
                                                                       ([d]
                                                                         (db/windowed
                                                                           db
                                                                           (fn 
                                                                             fn__18278
                                                                             ([p1__18223#]
                                                                               (and
                                                                                 (=
                                                                                   const_attrid
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__18223#)))
                                                                                 (or
                                                                                   (nil? whilev)
                                                                                   (^clojure.lang.IFn whilev
                                                                                     (.getV
                                                                                       ^datomic.impl.db.IDatum p1__18223#))))))
                                                                           (.seekAVET
                                                                             ^datomic.db.IDb db
                                                                             ^datomic.impl.db.IDatum d))))]
                                                                    starte
                                                                    [(fn 
                                                                       fn__18283
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           (unchecked-long
                                                                             ^java.lang.Number starte)
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           nil
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__18285
                                                                       ([d]
                                                                         (db/windowed
                                                                           db
                                                                           (fn 
                                                                             fn__18286
                                                                             ([p1__18224#]
                                                                               (and
                                                                                 (=
                                                                                   const_attrid
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__18224#)))
                                                                                 (or
                                                                                   (nil? whilee)
                                                                                   (^clojure.lang.IFn whilee
                                                                                     (long
                                                                                       (.getE
                                                                                         ^datomic.impl.db.IDatum p1__18224#)))))))
                                                                           (.seekAEVT
                                                                             ^datomic.db.IDb db
                                                                             ^datomic.impl.db.IDatum d))))]
                                                                    (and
                                                                      whilev
                                                                      const_attr
                                                                      (.hasAVET
                                                                        ^datomic.db.Attribute const_attr))
                                                                    [(fn 
                                                                       fn__18291
                                                                       ([d]
                                                                         (db/asserting-datum
                                                                           java.lang.Long/MIN_VALUE
                                                                           (.getA
                                                                             ^datomic.impl.db.IDatum d)
                                                                           nil
                                                                           2305843009213693951)))
                                                                     (fn 
                                                                       fn__18293
                                                                       ([d]
                                                                         (db/windowed
                                                                           db
                                                                           (fn 
                                                                             fn__18294
                                                                             ([p1__18225#]
                                                                               (and
                                                                                 (=
                                                                                   const_attrid
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__18225#)))
                                                                                 (^clojure.lang.IFn whilev
                                                                                   (.getV
                                                                                     ^datomic.impl.db.IDatum p1__18225#)))))
                                                                           (.seekAVET
                                                                             ^datomic.db.IDb db
                                                                             ^datomic.impl.db.IDatum d))))]
                                                                    :else
                                                                    (do
                                                                      [(fn 
                                                                         fn__18298
                                                                         ([d]
                                                                           (db/asserting-datum
                                                                             java.lang.Long/MIN_VALUE
                                                                             (.getA
                                                                               ^datomic.impl.db.IDatum d)
                                                                             nil
                                                                             2305843009213693951)))
                                                                       (fn 
                                                                         fn__18300
                                                                         ([d]
                                                                           (db/windowed
                                                                             db
                                                                             (fn 
                                                                               fn__18301
                                                                               ([p1__18226#]
                                                                                 (=
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum d))
                                                                                   (long
                                                                                     (.getA
                                                                                       ^datomic.impl.db.IDatum p1__18226#)))))
                                                                             (.seekAEVT
                                                                               ^datomic.db.IDb db
                                                                               ^datomic.impl.db.IDatum d))))]))
                            (aget ^"[Ljava.lang.Object;" bound 0) [(fn 
                                                                     fn__18304
                                                                     ([d]
                                                                       (db/asserting-datum
                                                                         (.getE
                                                                           ^datomic.impl.db.IDatum d)
                                                                         -1
                                                                         nil
                                                                         2305843009213693951)))
                                                                   (fn 
                                                                     fn__18306
                                                                     ([d]
                                                                       (let 
                                                                         [ret
                                                                          (db/windowed
                                                                            db
                                                                            (fn 
                                                                              fn__18307
                                                                              ([p1__18227#]
                                                                                (=
                                                                                  (long
                                                                                    (.getE
                                                                                      ^datomic.impl.db.IDatum d))
                                                                                  (long
                                                                                    (.getE
                                                                                      ^datomic.impl.db.IDatum p1__18227#)))))
                                                                            (.seekEAVT
                                                                              ^datomic.db.IDb db
                                                                              ^datomic.impl.db.IDatum d))]
                                                                         ret)))]
                            (aget ^"[Ljava.lang.Object;" bound 2) [(fn 
                                                                     fn__18310
                                                                     ([d]
                                                                       (db/asserting-datum
                                                                         java.lang.Long/MIN_VALUE
                                                                         -1
                                                                         (.getV
                                                                           ^datomic.impl.db.IDatum d)
                                                                         2305843009213693951)))
                                                                   (fn 
                                                                     fn__18312
                                                                     ([d]
                                                                       (db/windowed
                                                                         db
                                                                         (fn 
                                                                           fn__18313
                                                                           ([p1__18228#]
                                                                             (=
                                                                               (.getV
                                                                                 ^datomic.impl.db.IDatum d)
                                                                               (.getV
                                                                                 ^datomic.impl.db.IDatum p1__18228#))))
                                                                         (.seekRAET
                                                                           ^datomic.db.IDb db
                                                                           ^datomic.impl.db.IDatum d))))]
                            :else (do
                                    (throw
                                      (java.lang.Exception.
                                        "Insufficient bindings, will cause db scan"))
                                    nil))
               prober (nth vec__18234 (unchecked-int 0) nil)
               probe (nth vec__18234 (unchecked-int 1) nil)
               project (fn project
                         ([d y]
                           (let [ret (object-array (java.lang.Integer/valueOf (int proj_count)))]
                             (dotimes [i (alength ^"[Ljava.lang.Object;" px_from)]
                               (aset
                                 ^"[Ljava.lang.Object;" ret
                                 (unchecked-int (aget ^"[Ljava.lang.Object;" px_to (int i)))
                                 (let [G__18317 (long
                                                  (aget ^"[Ljava.lang.Object;" px_from (int i)))]
                                   (case
                                     G__18317
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
               vec__18237 (let [ht (java.util.HashMap.)
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
               ht (nth vec__18237 (unchecked-int 0) nil)
               probeset (nth vec__18237 (unchecked-int 1) nil)
               PAR 4
               probelists (let [cnt (count probeset)]
                            (if (> cnt 100)
                              (let [palist (java.util.ArrayList. ^java.util.Collection probeset)
                                    sz (if (= (rem cnt PAR) 0)
                                         (quot cnt PAR)
                                         (inc (quot cnt PAR)))]
                                (mapv
                                  (fn fn__18332
                                    ([p1__18229#]
                                      (.subList
                                        ^java.util.ArrayList palist
                                        (unchecked-int (* sz p1__18229#))
                                        (unchecked-int (min cnt (+ sz (* sz p1__18229#)))))))
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
                                          temp__5457__auto__ (.get
                                                               ^java.util.Map ht
                                                               (^clojure.lang.IFn hashx d))]
                                      (when temp__5457__auto__
                                        (let [ys temp__5457__auto__ yiter (iterator ys)]
                                          (while
                                            (.hasNext ^java.util.Iterator yiter)
                                            (let [y (.next ^java.util.Iterator yiter)]
                                              (when (^clojure.lang.IFn match? d y)
                                                (let [p (^clojure.lang.IFn project d y)]
                                                  (when (^clojure.lang.IFn pred p)
                                                    (.add ^java.util.Collection ret p)))))))))
                                    (recur (.next ^datomic.iter.Iter diter))))))))
                        nil))
               ret (java.util.Collections/newSetFromMap
                     (java.util.concurrent.ConcurrentHashMap.
                       (unchecked-int 16)
                       0.75
                       (unchecked-int PAR)))]
           (qmapv (fn fn__18337 ([p1__18230#] (^clojure.lang.IFn join p1__18230# ret))) probelists)
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
              _ (loop [seq_18352 (seq join_map) chunk_18353 nil count_18354 0 i_18355 0]
                  (if (< i_18355 count_18354)
                    (let [vec__18357 (.nth
                                       ^clojure.lang.Indexed chunk_18353
                                       (unchecked-int i_18355))
                          k (nth vec__18357 (unchecked-int 0) nil)
                          v (nth vec__18357 (unchecked-int 1) nil)]
                      (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                      (recur seq_18352 chunk_18353 count_18354 (inc i_18355)))
                    (let [temp__5457__auto__ (seq seq_18352)]
                      (when temp__5457__auto__
                        (let [seq_18352 temp__5457__auto__]
                          (if (chunked-seq? seq_18352)
                            (let [c__5719__auto__ (chunk-first seq_18352)]
                              (recur
                                (chunk-rest seq_18352)
                                c__5719__auto__
                                (count c__5719__auto__)
                                0))
                            (let [vec__18360 (first seq_18352)
                                  k (nth vec__18360 (unchecked-int 0) nil)
                                  v (nth vec__18360 (unchecked-int 1) nil)]
                              (aset ^"[Ljava.lang.Object;" bindings (unchecked-int k) v)
                              (recur (next seq_18352) nil 0 0))))))))
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
                                         (.add ^java.util.ArrayList rel p)))))))))
                         rel)))]
          (loop [seq_18369 (seq (qmapv proc (partv PART ys)))
                 chunk_18370 nil
                 count_18371 0
                 i_18372 0]
            (if (< i_18372 count_18371)
              (let [part (.nth ^clojure.lang.Indexed chunk_18370 (unchecked-int i_18372))]
                (.addAll ^java.util.AbstractCollection rel ^java.util.Collection part)
                (recur seq_18369 chunk_18370 count_18371 (inc i_18372)))
              (let [temp__5457__auto__ (seq seq_18369)]
                (when temp__5457__auto__
                  (let [seq_18369 temp__5457__auto__]
                    (if (chunked-seq? seq_18369)
                      (let [c__5719__auto__ (chunk-first seq_18369)]
                        (recur (chunk-rest seq_18369) c__5719__auto__ (count c__5719__auto__) 0))
                      (let [part (first seq_18369)]
                        (.addAll ^java.util.AbstractCollection rel ^java.util.Collection part)
                        (recur (next seq_18369) nil 0 0))))))))
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
                    (fn fn__18383
                      ([p1__18381# p2__18380#]
                        (if (^clojure.lang.IFn ib p2__18380#)
                          (assoc
                            p1__18381#
                            (^clojure.lang.IFn ib p2__18380#)
                            (^clojure.lang.IFn jb p2__18380#))
                          p1__18381#)))
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
          (loop [seq_18387 (seq xs) chunk_18388 nil count_18389 0 i_18390 0]
            (if (< i_18390 count_18389)
              (let [x (.nth ^clojure.lang.Indexed chunk_18388 (unchecked-int i_18390))]
                (let [tos (object-array (java.lang.Integer/valueOf (int (count ybinds))))]
                  (dotimes [i (count ybinds)]
                    (aset
                      ^"[Ljava.lang.Object;" tos
                      (int i)
                      (nth
                        x
                        (unchecked-int (^clojure.lang.IFn xb (^clojure.lang.IFn yb (long i)))))))
                  (.add ^java.util.HashSet ret (tuple tos)))
                (recur seq_18387 chunk_18388 count_18389 (inc i_18390)))
              (let [temp__5457__auto__ (seq seq_18387)]
                (when temp__5457__auto__
                  (let [seq_18387 temp__5457__auto__]
                    (if (chunked-seq? seq_18387)
                      (let [c__5719__auto__ (chunk-first seq_18387)]
                        (recur (chunk-rest seq_18387) c__5719__auto__ (count c__5719__auto__) 0))
                      (let [x (first seq_18387)]
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
                        (recur (next seq_18387) nil 0 0))))))))
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
        (loop [seq_18397 (seq project_map_y) chunk_18398 nil count_18399 0 i_18400 0]
          (if (< i_18400 count_18399)
            (let [vec__18401 (.nth ^clojure.lang.Indexed chunk_18398 (unchecked-int i_18400))
                  y (nth vec__18401 (unchecked-int 0) nil)
                  i (nth vec__18401 (unchecked-int 1) nil)]
              (aset ^"[Ljava.lang.Object;" invpmy (unchecked-int i) y)
              (recur seq_18397 chunk_18398 count_18399 (inc i_18400)))
            (let [temp__5457__auto__ (seq seq_18397)]
              (when temp__5457__auto__
                (let [seq_18397 temp__5457__auto__]
                  (if (chunked-seq? seq_18397)
                    (let [c__5719__auto__ (chunk-first seq_18397)]
                      (recur (chunk-rest seq_18397) c__5719__auto__ (count c__5719__auto__) 0))
                    (let [vec__18404 (first seq_18397)
                          y (nth vec__18404 (unchecked-int 0) nil)
                          i (nth vec__18404 (unchecked-int 1) nil)]
                      (aset ^"[Ljava.lang.Object;" invpmy (unchecked-int i) y)
                      (recur (next seq_18397) nil 0 0))))))))
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
         (map (fn fn__18423 ([p1__18422#] (not (contains? bindset p1__18422#)))) (rest query)))])
    ([query] [(first query) (vec (map variable? (rest query)))]))
  (declare eval-query)
  (defn not-join-clause? ([c] (and (instance? java.util.List c) (= 'not-join (first c)))))
  (defn free-var? ([x] (variable? x)))
  (defn free-vars
    ([c] (cond (not-join-clause? c) (second c) :else (do (filter free-var? (flatten c))))))
  (defn unifying-vars
    ([p__18431]
      (let [vec__18432 p__18431
            seq__18433 (seq vec__18432)
            first__18434 (first seq__18433)
            seq__18433 (next seq__18433)
            p first__18434
            cs seq__18433
            c vec__18432
            vec__18435 (if (source? p) cs c)
            seq__18436 (seq vec__18435)
            first__18437 (first seq__18436)
            seq__18436 (next seq__18436)
            p first__18437
            cs seq__18436
            c vec__18435]
        (cond
          (#{'not-join 'or-join} p) (flatten (first cs))
          (#{'and 'not} p) (mapcat unifying-vars cs)
          (= 'or p) (let [uvs (mapv
                                (fn fn__18438 ([p1__18430#] (set (unifying-vars p1__18430#))))
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
    ([srcs prog p__18454 init_binds]
      (let [vec__18455 p__18454
            seq__18456 (seq vec__18455)
            first__18457 (first seq__18456)
            seq__18456 (next seq__18456)
            vec__18458 first__18457
            seq__18459 (seq vec__18458)
            first__18460 (first seq__18459)
            seq__18459 (next seq__18459)
            hpred first__18460
            hargs seq__18459
            body seq__18456
            rule vec__18455
            src (fn src ([p1__18443#] (or (:tag (meta p1__18443#)) '$)))
            pack (fn pack ([c] [(^clojure.lang.IFn src c) c]))
            unpack (fn unpack ([p1__18444#] (nth p1__18444# (unchecked-int 1))))
            cargs (fn cargs
                    ([clause]
                      (cond
                        (map? clause) (:argvars clause)
                        (not-join-clause? clause) (free-vars clause)
                        (extensional? prog (first clause)) clause
                        :else (do (next clause)))))
            extdb (fn extdb
                    ([p__18479]
                      (let [vec__18481 p__18479
                            src (nth vec__18481 (unchecked-int 0) nil)
                            c (nth vec__18481 (unchecked-int 1) nil)
                            temp__5457__auto__ (and
                                                 (not (map? c))
                                                 (not (not-join-clause? c))
                                                 (extensional? prog (first c))
                                                 (^clojure.lang.IFn srcs src))]
                        (when temp__5457__auto__
                          (let [db temp__5457__auto__] (when (instance? datomic.db.IDb db) db))))))
            cbinds (fn cbinds
                     ([clause]
                       (cond
                         (map? clause) (concat (:argvars clause) (:binds clause))
                         (not-join-clause? clause) (free-vars clause)
                         :else (do (filter variable? clause)))))
            sv_clause? (fn sv_clause_QMARK_
                         ([p1__18445#]
                           (and
                             (map? p1__18445#)
                             (nil? (:argvars p1__18445#))
                             (#{:tuple :scalar} (:bind-type p1__18445#)))))
            in_clause? (fn in_clause_QMARK_
                         ([p1__18446#]
                           (and
                             (map? p1__18446#)
                             (nil? (:argvars p1__18446#))
                             (#{:rel :list} (:bind-type p1__18446#))
                             (.startsWith (name (^clojure.lang.IFn src p1__18446#)) "$__in"))))
            delay_ins (fn delay_ins
                        ([cs]
                          (let [vec__18501 (split-with in_clause? cs)
                                ins (nth vec__18501 (unchecked-int 0) nil)
                                cs (nth vec__18501 (unchecked-int 1) nil)
                                m (reduce
                                    (fn fn__18504
                                      ([m in]
                                        (let [bset (set (^clojure.lang.IFn cbinds in))
                                              uc (first
                                                   (filter
                                                     (fn fn__18505
                                                       ([p1__18447#]
                                                         (some
                                                           bset
                                                           (^clojure.lang.IFn cargs p1__18447#))))
                                                     cs))]
                                          (update-in m [uc] conj in))))
                                    {}
                                    ins)]
                            (concat
                              (reduce
                                (fn fn__18508
                                  ([ret c]
                                    (let [vec__18509 (find m c)
                                          k (nth vec__18509 (unchecked-int 0) nil)
                                          vs (nth vec__18509 (unchecked-int 1) nil)]
                                      (if (identical? k c)
                                        (into (conj ret (first vs) c) (next vs))
                                        (conj ret c)))))
                                []
                                cs)
                              (get m nil)))))
            pred? (fn pred_QMARK_
                    ([p1__18448#] (and (map? p1__18448#) (nil? (:binds p1__18448#)))))
            vec__18461 (common/split-filter pred? body)
            preds (nth vec__18461 (unchecked-int 0) nil)
            npreds (nth vec__18461 (unchecked-int 1) nil)
            vec__18464 (common/split-filter not-join-clause? npreds)
            njcs (nth vec__18464 (unchecked-int 0) nil)
            npreds (nth vec__18464 (unchecked-int 1) nil)
            vec__18467 (common/split-filter sv_clause? npreds)
            svs (nth vec__18467 (unchecked-int 0) nil)
            npreds (nth vec__18467 (unchecked-int 1) nil)
            body (concat svs preds njcs (^clojure.lang.IFn delay_ins npreds))
            reqcnt (fn reqcnt
                     ([clause]
                       (when (contains? prog (first clause))
                         (let [vec__18518 (find prog (first clause))
                               p (nth vec__18518 (unchecked-int 0) nil)]
                           (:reqcnt (meta p))))))
            underbound? (fn underbound_QMARK_
                          ([n bindings pc]
                            (let [clause (^clojure.lang.IFn unpack pc)
                                  args (^clojure.lang.IFn cargs clause)]
                              (cond
                                (or (not-join-clause? clause) (map? clause)) (some
                                                                               (fn 
                                                                                 fn__18523
                                                                                 ([p1__18449#]
                                                                                   (and
                                                                                     (variable?
                                                                                       p1__18449#)
                                                                                     (not
                                                                                       (contains?
                                                                                         bindings
                                                                                         p1__18449#)))))
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
                                                                            fn__18526
                                                                            ([p1__18450#]
                                                                              (contains?
                                                                                bindings
                                                                                p1__18450#)))
                                                                          (take rcnt args)))))
                                                                  (and
                                                                    (= hpred (first clause))
                                                                    (not
                                                                      (some
                                                                        (fn 
                                                                          fn__18528
                                                                          ([p1__18451#]
                                                                            (contains?
                                                                              bindings
                                                                              p1__18451#)))
                                                                        (filter variable? args)))))
                                :else (do
                                        (let [temp__5457__auto__ (^clojure.lang.IFn extdb pc)]
                                          (when temp__5457__auto__
                                            (let [db temp__5457__auto__]
                                              (not
                                                (some
                                                  (fn fn__18530
                                                    ([p1__18452#]
                                                      (or
                                                        (not (variable? p1__18452#))
                                                        (contains? bindings p1__18452#))))
                                                  (take n clause)))))))))))
            clauses (loop [clauses [] bindings (set init_binds) remclauses (map pack body)]
                      (if (empty? remclauses)
                        clauses
                        (let [vec__18540 (split-with (partial underbound? 2 bindings) remclauses)
                              skip (nth vec__18540 (unchecked-int 0) nil)
                              ready (nth vec__18540 (unchecked-int 1) nil)
                              vec__18543 (if (empty? ready)
                                           (split-with (partial underbound? 3 bindings) remclauses)
                                           [skip ready])
                              skip (nth vec__18543 (unchecked-int 0) nil)
                              ready (nth vec__18543 (unchecked-int 1) nil)
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
                    (fn fn__18547 ([p1__18453#] (set (^clojure.lang.IFn cbinds p1__18453#))))
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
                    ([p1__18550#] (and (map? p1__18550#) (nil? (:binds p1__18550#)))))
            ctor (fn ctor
                   ([inbinds p__18557]
                     (let [map__18559 p__18557
                           map__18559 (if (seq? map__18559)
                                        (clojure.lang.PersistentHashMap/create (seq map__18559))
                                        map__18559)
                           clause map__18559
                           f (get map__18559 :fn)
                           argvars (get map__18559 :argvars)
                           needs_source (get map__18559 :needs-source)
                           binds (get map__18559 :binds)
                           params (concat argvars binds)
                           arity (count params)
                           consts (vec
                                    (map
                                      (fn fn__18563
                                        ([p1__18551#]
                                          (when-not (variable-or-blank? p1__18551#) p1__18551#)))
                                      params))
                           vec__18560 (create-join-maps params inbinds [])
                           join_map (nth vec__18560 (unchecked-int 0) nil)
                           _ (nth vec__18560 (unchecked-int 1) nil)
                           _ (nth vec__18560 (unchecked-int 2) nil)
                           src (when needs_source (get srcs (or (:tag (meta clause)) '$)))]
                       (query-stats/acc-with-clause-stats! :preds clause)
                       (fn fn__18565
                         ([]
                           (let [args (object-array (java.lang.Integer/valueOf (int arity)))]
                             (fn fn__18566
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
                      G__18578 (seq sched)
                      vec__18579 G__18578
                      seq__18580 (seq vec__18579)
                      first__18581 (first seq__18580)
                      seq__18580 (next seq__18580)
                      vec__18582 first__18581
                      clause (nth vec__18582 (unchecked-int 0) nil)
                      outbinds (nth vec__18582 (unchecked-int 1) nil)
                      clauses seq__18580
                      sched vec__18579]
                  (loop [ret ret G__18578 G__18578]
                    (let [ret ret
                          vec__18586 G__18578
                          seq__18587 (seq vec__18586)
                          first__18588 (first seq__18587)
                          seq__18587 (next seq__18587)
                          vec__18589 first__18588
                          clause (nth vec__18589 (unchecked-int 0) nil)
                          outbinds (nth vec__18589 (unchecked-int 1) nil)
                          clauses seq__18587
                          sched vec__18586]
                      (if sched
                        (if (^clojure.lang.IFn pred? clause)
                          (recur (conj ret [clause outbinds nil]) (seq clauses))
                          (let [vec__18592 (split-with (comp pred? first) clauses)
                                preds (nth vec__18592 (unchecked-int 0) nil)
                                clauses (nth vec__18592 (unchecked-int 1) nil)]
                            (recur
                              (conj
                                ret
                                [clause
                                 outbinds
                                 (if (empty? preds)
                                   truep
                                   (let [ctors (map
                                                 (fn fn__18595
                                                   ([p1__18552#]
                                                     (^clojure.lang.IFn ctor
                                                       outbinds
                                                       (first p1__18552#))))
                                                 preds)]
                                     (fn fn__18597
                                       ([]
                                         (apply
                                           every-pred
                                           (map
                                             (fn fn__18598
                                               ([p1__18553#] (^clojure.lang.IFn p1__18553#)))
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
              (fn fn__18607 ([p1__18606#] (recursive? prog p1__18606# seen)))
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
                                        (seq (concat (clojure.core/list 't__18615__auto__)))))
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
                                          (let [iter__6025__auto__ (fn 
                                                                     iter__18617
                                                                     ([s__18618]
                                                                       (lazy-seq
                                                                         (let 
                                                                           [s__18618 s__18618
                                                                            temp__5457__auto__
                                                                            (seq s__18618)]
                                                                           (when
                                                                             temp__5457__auto__
                                                                             (let 
                                                                               [s__18618
                                                                                temp__5457__auto__]
                                                                               (if
                                                                                 (chunked-seq?
                                                                                   s__18618)
                                                                                 (let 
                                                                                   [c__6023__auto__
                                                                                    (chunk-first
                                                                                      s__18618)
                                                                                    size__6024__auto__
                                                                                    (count
                                                                                      c__6023__auto__)
                                                                                    b__18620
                                                                                    (chunk-buffer
                                                                                      (java.lang.Integer/valueOf
                                                                                        (int
                                                                                          size__6024__auto__)))]
                                                                                   (if
                                                                                     (loop 
                                                                                       [i__18619 0]
                                                                                       (if
                                                                                         (<
                                                                                           i__18619
                                                                                           size__6024__auto__)
                                                                                         (let 
                                                                                           [i
                                                                                            (.nth
                                                                                              ^clojure.lang.Indexed c__6023__auto__
                                                                                              (unchecked-int
                                                                                                i__18619))]
                                                                                           (chunk-append
                                                                                             b__18620
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
                                                                                               i__18619)))
                                                                                         true))
                                                                                     (chunk-cons
                                                                                       (chunk
                                                                                         b__18620)
                                                                                       (^clojure.lang.IFn iter__18617
                                                                                         (chunk-rest
                                                                                           s__18618)))
                                                                                     (chunk-cons
                                                                                       (chunk
                                                                                         b__18620)
                                                                                       nil)))
                                                                                 (let 
                                                                                   [i
                                                                                    (first
                                                                                      s__18618)]
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
                                                                                     (^clojure.lang.IFn iter__18617
                                                                                       (rest
                                                                                         s__18618)))))))))))]
                                            (^clojure.lang.IFn iter__6025__auto__
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
                                                        (clojure.core/list 'i__18616__auto__)
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
                                                            (clojure.core/list 'i__18616__auto__)
                                                            (clojure.core/list
                                                              (java.lang.Integer/valueOf
                                                                (int (count vars)))))))
                                                      (clojure.core/list
                                                        (seq
                                                          (concat
                                                            (clojure.core/list 'clojure.core/nth)
                                                            (clojure.core/list 't__18615__auto__)
                                                            (clojure.core/list
                                                              'i__18616__auto__))))))))))
                                          (clojure.core/list
                                            (seq
                                              (concat
                                                (clojure.core/list 'datomic.datalog/tuple)
                                                (clojure.core/list gret))))))))))
                              (clojure.core/list
                                (let [G__18630 bind_type]
                                  (case
                                    G__18630
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
    ([p__18637]
      (let [vec__18638 p__18637
            call (nth vec__18638 (unchecked-int 0) nil)
            binds (nth vec__18638 (unchecked-int 1) nil)
            xtra (nth vec__18638 (unchecked-int 2) nil)
            clause vec__18638]
        (if (instance? java.util.List call)
          (let [vec__18641 (list* call)
                seq__18642 (seq vec__18641)
                first__18643 (first seq__18642)
                seq__18642 (next seq__18642)
                f first__18643
                body seq__18642
                expr vec__18641
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
                          (let [G__18644 bind_type]
                            (case
                              G__18644
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
                    (let [G__18645 bind_type]
                      (case
                        G__18645
                        :scalar
                        (fn fn__18646 ([p1__18634#] (vector (vector p1__18634#))))
                        :tuple
                        (fn fn__18648 ([p1__18636#] (vector p1__18636#)))
                        :list
                        (fn fn__18650 ([p1__18635#] (mapv vector p1__18635#)))
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
      (let [vec__18657 (reduce
                         (fn fn__18662
                           ([p__18660 p__18661]
                             (let [vec__18663 p__18660
                                   m (nth vec__18663 (unchecked-int 0) nil)
                                   cs (nth vec__18663 (unchecked-int 1) nil)
                                   vec__18666 p__18661
                                   seq__18667 (seq vec__18666)
                                   first__18668 (first seq__18667)
                                   seq__18667 (next seq__18667)
                                   pred first__18668
                                   args seq__18667
                                   c vec__18666]
                               (if (and
                                     (symbol? pred)
                                     (not= 'not-join pred)
                                     (not (variable-or-blank? pred))
                                     (not (.startsWith (name pred) "$"))
                                     (not (every? variable-or-blank? args)))
                                 (let [vec__18669 (reduce
                                                    (fn fn__18673
                                                      ([p__18672 arg]
                                                        (let [vec__18674 p__18672
                                                              m
                                                              (nth
                                                                vec__18674
                                                                (unchecked-int 0)
                                                                nil)
                                                              args
                                                              (nth
                                                                vec__18674
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
                                       m (nth vec__18669 (unchecked-int 0) nil)
                                       args (nth vec__18669 (unchecked-int 1) nil)]
                                   [m (conj cs (cons pred args))])
                                 [m (conj cs c)]))))
                         [{} []]
                         clauses)
            subs (nth vec__18657 (unchecked-int 0) nil)
            new_clauses (nth vec__18657 (unchecked-int 1) nil)]
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
    ([p__18684]
      (let [vec__18685 p__18684
            seq__18686 (seq vec__18685)
            first__18687 (first seq__18686)
            seq__18686 (next seq__18686)
            p first__18687
            cs seq__18686
            c vec__18685]
        (cond
          (#{'and 'not 'or} p) (every? all-pred cs)
          (and (instance? java.util.List p) (nil? cs)) (do true)))))
  (defn to-pred
    ([p__18690]
      (let [vec__18691 p__18690
            seq__18692 (seq vec__18691)
            first__18693 (first seq__18692)
            seq__18692 (next seq__18692)
            p first__18693
            cs seq__18692
            c vec__18691]
        (cond
          (#{'and 'or} p) (cons p (map to-pred cs))
          (= 'not p) (clojure.core/list p (list* 'and (map to-pred cs)))
          :else (do p)))))
  (defn not-or-all-pred
    ([p__18695]
      (let [vec__18696 p__18695
            seq__18697 (seq vec__18696)
            first__18698 (first seq__18697)
            seq__18697 (next seq__18697)
            p first__18698
            cs seq__18697
            c vec__18696]
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
    ([p__18701]
      (let [vec__18702 p__18701
            seq__18703 (seq vec__18702)
            first__18704 (first seq__18703)
            seq__18703 (next seq__18703)
            p first__18704
            cs seq__18703
            c vec__18702]
        (if (#{'not 'or} p)
          (let [vs (vec (unifying-var-set c))]
            (with-meta (list* ({'not 'not-join, 'or 'or-join} p) vs cs) (meta c)))
          c))))
  (defn normalize-or-join
    ([p__18707]
      (let [vec__18708 p__18707
            seq__18709 (seq vec__18708)
            first__18710 (first seq__18709)
            seq__18709 (next seq__18709)
            p first__18710
            first__18710 (first seq__18709)
            seq__18709 (next seq__18709)
            vs first__18710
            cs seq__18709
            c vec__18708]
        (if (= 'or-join p)
          (let [cs (map
                     (fn fn__18711
                       ([p1__18706#]
                         (if (= 'and (first p1__18706#))
                           (rest p1__18706#)
                           (clojure.core/list p1__18706#))))
                     cs)]
            (when (empty? vs)
              (error/arg
                :db.error/or-binding-empty
                (str "'or' cannot have empty binding set: " c)))
            (with-meta (list* p vs cs) (meta c)))
          c))))
  (defn or-join->rule-preds
    ([p__18714]
      (let [vec__18715 p__18714
            seq__18716 (seq vec__18715)
            first__18717 (first seq__18716)
            seq__18716 (next seq__18716)
            p first__18717
            first__18717 (first seq__18716)
            seq__18716 (next seq__18716)
            vs first__18717
            cs seq__18716
            rname (gensym "arule__")
            head (cons rname vs)]
        (mapv (partial cons (with-meta head #:query-stats{:clause (list* p [vs])})) cs))))
  (declare add-rule)
  (defn prep-clauses
    ([rm clauses]
      (let [clauses (map normalize-or-join (map not-or->not-or-join (map not-or-all-pred clauses)))
            vec__18719 (reduce
                         (fn fn__18723
                           ([p__18722 c]
                             (let [vec__18724 p__18722
                                   rm (nth vec__18724 (unchecked-int 0) nil)
                                   cs (nth vec__18724 (unchecked-int 1) nil)]
                               (if (= 'or-join (first c))
                                 (let [preds (or-join->rule-preds c)
                                       vec__18727 (ffirst preds)
                                       seq__18728 (seq vec__18727)
                                       first__18729 (first seq__18728)
                                       seq__18728 (next seq__18728)
                                       rname first__18729
                                       args seq__18728
                                       head vec__18727
                                       rm (add-rule rm rname preds)]
                                   [rm
                                    (conj
                                      cs
                                      (with-meta (flatten head) (merge (meta c) (meta head))))])
                                 [rm (conj cs c)]))))
                         [rm []]
                         clauses)
            rm (nth vec__18719 (unchecked-int 0) nil)
            clauses (nth vec__18719 (unchecked-int 1) nil)]
        [rm (mapv expr-clause (lift-consts-from-preds clauses))])))
  (defn callees
    ([x]
      (if (list? x)
        (cons (first x) (mapcat callees (rest x)))
        (let [G__18732 (data/equality-partition x)]
          (case
            G__18732
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
                   ([p__18737]
                     (let [vec__18739 p__18737
                           seq__18740 (seq vec__18739)
                           first__18741 (first seq__18740)
                           seq__18740 (next seq__18740)
                           p first__18741
                           first__18741 (first seq__18740)
                           seq__18740 (next seq__18740)
                           r first__18741
                           xs seq__18740]
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
            vec__18734 (reduce
                         (fn fn__18746
                           ([p__18744 p__18745]
                             (let [vec__18747 p__18744
                                   rm (nth vec__18747 (unchecked-int 0) nil)
                                   ps (nth vec__18747 (unchecked-int 1) nil)
                                   vec__18750 p__18745
                                   seq__18751 (seq vec__18750)
                                   first__18752 (first seq__18751)
                                   seq__18751 (next seq__18751)
                                   head first__18752
                                   clauses seq__18751
                                   pred vec__18750
                                   vec__18753 (prep-clauses rm clauses)
                                   rm (nth vec__18753 (unchecked-int 0) nil)
                                   cs (nth vec__18753 (unchecked-int 1) nil)]
                               [rm (conj ps (cons (vec (flatten head)) cs))])))
                         [rm []]
                         preds)
            rm (nth vec__18734 (unchecked-int 0) nil)
            preds (nth vec__18734 (unchecked-int 1) nil)]
        (assoc rm rname (vec preds)))))
  (defn rule-map
    ([rules]
      (let [rules (if (string? rules) (binding [*read-eval* false] (read-string rules)) rules)
            rules (group-by ffirst rules)
            rules (reduce-kv add-rule {} rules)]
        (loop [seq_18760 (seq rules) chunk_18761 nil count_18762 0 i_18763 0]
          (if (< i_18763 count_18762)
            (let [vec__18764 (.nth ^clojure.lang.Indexed chunk_18761 (unchecked-int i_18763))
                  k (nth vec__18764 (unchecked-int 0) nil)
                  v (nth vec__18764 (unchecked-int 1) nil)]
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
              (recur seq_18760 chunk_18761 count_18762 (inc i_18763)))
            (let [temp__5457__auto__ (seq seq_18760)]
              (when temp__5457__auto__
                (let [seq_18760 temp__5457__auto__]
                  (if (chunked-seq? seq_18760)
                    (let [c__5719__auto__ (chunk-first seq_18760)]
                      (recur (chunk-rest seq_18760) c__5719__auto__ (count c__5719__auto__) 0))
                    (let [vec__18767 (first seq_18760)
                          k (nth vec__18767 (unchecked-int 0) nil)
                          v (nth vec__18767 (unchecked-int 1) nil)]
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
                      (recur (next seq_18760) nil 0 0))))))))
        rules)))
  (def rule-cache (cache/create-computing rule-map 1000))
  (def q (clojure.lang.RT/var "datomic.query" "q"))
  (defn eval-not-join
    ([srcs prog inrel inbinds p__18774]
      (let [vec__18775 p__18774
            seq__18776 (seq vec__18775)
            first__18777 (first seq__18776)
            seq__18776 (next seq__18776)
            _ first__18777
            first__18777 (first seq__18776)
            seq__18776 (next seq__18776)
            vars first__18777
            cs seq__18776
            not_join_clause vec__18775
            used (used-srcs srcs not_join_clause)
            dbs (select-keys srcs used)
            csrc (:tag (meta not_join_clause))
            dbs (cond-> dbs csrc (assoc '$ (get srcs csrc)))
            uvs (set vars)
            binds (map
                    (fn fn__18779
                      ([p1__18773#] (or (^clojure.lang.IFn uvs p1__18773#) (gensym "?nb__"))))
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
            G__18782 (java.util.HashSet. ^java.util.Collection inrel)]
        (.removeAll ^java.util.AbstractSet G__18782 (q query (conj (vec (vals dbs)) prog inrel)))
        G__18782)))
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
                       (fn fn__18790
                         ([p1__18784#]
                           (if (variable-or-blank? p1__18784#)
                             (some-> top_bounds (:consts) (^clojure.lang.IFn p1__18784#))
                             p1__18784#)))
                       args)
              starts (mapv
                       (fn fn__18793 ([p1__18785#] (some-> top_bounds (:starts) (get p1__18785#))))
                       args)
              whiles (mapv
                       (fn fn__18796 ([p1__18786#] (some-> top_bounds (:whiles) (get p1__18786#))))
                       args)
              vec__18787 (create-join-maps inbinds args next_binds)
              join (nth vec__18787 (unchecked-int 0) nil)
              projx (nth vec__18787 (unchecked-int 1) nil)
              projy (nth vec__18787 (unchecked-int 2) nil)
              root? (and (empty? join) (empty? inbinds))
              ret (try
                    (cond
                      ext? (join-project
                             (if (and root? (empty? inrel)) #{[]} inrel)
                             (extrel db consts starts whiles)
                             join
                             projx
                             projy
                             predctor)
                      exf (join-project
                            (if (and root? (empty? inrel)) #{[]} inrel)
                            (fnrel db clause consts)
                            join
                            projx
                            projy
                            predctor)
                      exp (join-project
                            (if (and root? (empty? inrel)) #{[]} inrel)
                            (predrel db clause consts)
                            join
                            projx
                            projy
                            nil)
                      (get prog (first clause)) (let [bindset (set inbinds)
                                                      apred (adorned-pred clause bindset)
                                                      outbinds (filter
                                                                 bindset
                                                                 (filter variable? (next clause)))
                                                      pred (first clause)
                                                      aresk [src pred]]
                                                  (query-stats/with-phase-stats
                                                    (fn fn__18800
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
                                                            {:consts consts,
                                                             :starts starts,
                                                             :whiles whiles})))))
                                                  (join-project
                                                    (if (and root? (empty? inrel)) #{[]} inrel)
                                                    (get ans aresk [])
                                                    join
                                                    projx
                                                    projy
                                                    predctor))
                      :else (do
                              (throw
                                (java.lang.IllegalArgumentException.
                                  (str "Undefined predicate: " (first clause))))
                              nil))
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
                     ([p1__18814#]
                       (into
                         {}
                         (map-indexed
                           (fn fn__18816 ([i c] (when-not (nil? c) [(^clojure.lang.IFn ia i) c])))
                           p1__18814#))))]
        {:consts (^clojure.lang.IFn mapize (:consts nb)),
         :starts (^clojure.lang.IFn mapize (:starts nb)),
         :whiles (^clojure.lang.IFn mapize (:whiles nb))})))
  (defn eval-rule
    ([db prog oprog p__18820 p__18821 inrel sched_fn src ans ins top_bounds nested_bounds]
      (let [vec__18822 p__18820
            seq__18823 (seq vec__18822)
            first__18824 (first seq__18823)
            seq__18823 (next seq__18823)
            head first__18824
            body seq__18823
            rule vec__18822
            vec__18825 p__18821
            pred (nth vec__18825 (unchecked-int 0) nil)
            adorn (nth vec__18825 (unchecked-int 1) nil)
            apred vec__18825
            vec__18828 head
            seq__18829 (seq vec__18828)
            first__18830 (first seq__18829)
            seq__18829 (next seq__18829)
            hpred first__18830
            hargs seq__18829
            aresk [src pred]
            inbinds (keep-indexed
                      (fn fn__18831 ([i a] (when-not (^clojure.lang.IFn adorn i) a)))
                      hargs)
            multi? (and (map? db) (not (instance? datomic.db.IDb db)))
            srcs (if multi? db {'$ db})
            cbs (push-preds srcs (^clojure.lang.IFn sched_fn srcs prog rule inbinds))
            top_bounds (if nested_bounds (remap-bounds nested_bounds hargs) top_bounds)
            res (try
                  (loop [sbinds inbinds sup inrel cbs cbs]
                    (if cbs
                      (let [vec__18834 (first cbs)
                            c (nth vec__18834 (unchecked-int 0) nil)
                            next_binds (nth vec__18834 (unchecked-int 1) nil)
                            predctor (nth vec__18834 (unchecked-int 2) nil)
                            csrc (:tag (meta c))
                            cdb (cond
                                  (and csrc multi?) (let [x (get db csrc)]
                                                      (if (nil? x)
                                                        (do
                                                          (throw
                                                            (java.lang.Exception.
                                                              (str
                                                                "Unable to find data source: "
                                                                csrc
                                                                " in: "
                                                                (keys db))))
                                                          nil)
                                                        x))
                                  multi? (get db '$)
                                  :else (do db))
                            sup1 (query-stats/with-clause-stats
                                   (fn fn__18837
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
    ([db prog oprog p__18844 input sched_fn src ans ins top_bounds nested_bounds]
      (let [vec__18845 p__18844
            pred (nth vec__18845 (unchecked-int 0) nil)
            adorn (nth vec__18845 (unchecked-int 1) nil)
            apred vec__18845
            iresk [src apred]
            inpred (get ins iresk (java.util.HashSet.))
            input (java.util.HashSet. ^java.util.Collection input)]
        (.removeAll ^java.util.AbstractSet input ^java.util.Collection inpred)
        (when (or (not (.isEmpty ^java.util.HashSet input)) (every? identity adorn))
          (let [rules (get prog pred)]
            (.addAll ^java.util.Set inpred ^java.util.Collection input)
            (.put ^java.util.Map ins iresk inpred)
            (loop [seq_18848 (seq rules) chunk_18849 nil count_18850 0 i_18851 0]
              (if (< i_18851 count_18850)
                (let [rule (.nth ^clojure.lang.Indexed chunk_18849 (unchecked-int i_18851))]
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
                  (recur seq_18848 chunk_18849 count_18850 (inc i_18851)))
                (let [temp__5457__auto__ (seq seq_18848)]
                  (when temp__5457__auto__
                    (let [seq_18848 temp__5457__auto__]
                      (if (chunked-seq? seq_18848)
                        (let [c__5719__auto__ (chunk-first seq_18848)]
                          (recur (chunk-rest seq_18848) c__5719__auto__ (count c__5719__auto__) 0))
                        (let [rule (first seq_18848)]
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
                          (recur (next seq_18848) nil 0 0))))))))))
        nil)))
  (defn bound-consts
    ([srcs query]
      (reduce-kv
        (fn fn__18856
          ([m v b]
            (assoc
              m
              v
              (if (vector? b)
                (let [vec__18857 b
                      src (nth vec__18857 (unchecked-int 0) nil)
                      idx (nth vec__18857 (unchecked-int 1) nil)]
                  (nth (get srcs src) (unchecked-int ^java.lang.Number idx)))
                (get srcs b)))))
        {}
        (:in-consts query))))
  (defn ranges
    ([in_consts query]
      [(reduce-kv
         (fn fn__18863 ([m v c] (assoc m v (if (variable? c) (get in_consts c) c))))
         {}
         (:range-starts query))
       (let [cmps {'= =, '< ext/<, '<= ext/<=}]
         (reduce-kv
           (fn fn__18866
             ([m v p__18865]
               (let [vec__18867 p__18865
                     cmpsym (nth vec__18867 (unchecked-int 0) nil)
                     cb (nth vec__18867 (unchecked-int 1) nil)
                     c (if (variable? cb) (get in_consts cb) cb)
                     cmp (^clojure.lang.IFn cmps cmpsym)]
                 (assoc m v (fn fn__18870 ([p1__18862#] (^clojure.lang.IFn cmp p1__18862# c)))))))
           {}
           (:range-whiles query)))]))
  (defn qsqr
    ([db query sched_fn]
      (let [map__18874 query
            map__18874 (if (seq? map__18874)
                         (clojure.lang.PersistentHashMap/create (seq map__18874))
                         map__18874)
            from (get map__18874 :in)
            pargs (get map__18874 :find)
            clauses (get map__18874 :where)
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
            vec__18875 (ranges in_consts query)
            range_starts (nth vec__18875 (unchecked-int 0) nil)
            range_whiles (nth vec__18875 (unchecked-int 1) nil)
            top_bounds {:consts in_consts, :starts range_starts, :whiles range_whiles}
            ans (java.util.HashMap.)
            apred (adorned-pred q)
            aresk [nil pred]
            cancel (atom nil)]
        (let [temp__5457__auto__ (when (contains? query :timeout) (first (:timeout query)))]
          (when temp__5457__auto__
            (let [timeout temp__5457__auto__ f (fn f ([] (reset! cancel "timeout elapsed")))]
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
                (fn fn__18880
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
    ([f] (fn fn__18885 ([_ & args] (let [ret (apply f args)] [(conj (into [] args) ret)])))))
  (defn rel-pred ([f] (fn fn__18888 ([_ & args] (apply f args))))))
(do
  (clojure.core/in-ns 'datomic.query)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['compare 'qualified-symbol?])
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.cache :as 'cache]
        ['datomic.index :as 'index]
        ['datomic.iter :as 'iter]
        ['datomic.fulltext :as 'ft]
        ['datomic.config :as 'config]
        ['datomic.common :as 'common :refer (clojure.core/list 'compare 'qualified-symbol?)]
        ['datomic.datalog :as 'datalog]
        ['datomic.error :as 'error]
        ['datomic.pull :as 'pull]
        ['datomic.query.support :as 'qs]
        ['datomic.measure.query-stats :as 'query-stats]
        ['datomic.measure.io-stats :as 'io-stats]
        ['clojure.set :as 'set]
        ['clojure.pprint :as 'pp]
        'datomic.extensions
        'datomic.aggregation)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'clojure.lang.IPersistentVector)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.Entity)
      (clojure.core/import 'datomic.db.IDbImpl)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.db.Db)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.btset.IDataSet)
      (clojure.core/import 'java.util.List)
      (clojure.core/import 'java.util.Set)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'java.util.Collection)
      (clojure.core/import 'datomic.iter.Iter)))
  (when-not (.equals 'datomic.query 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.query))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['compare 'qualified-symbol?])
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.cache :as 'cache]
          ['datomic.index :as 'index]
          ['datomic.iter :as 'iter]
          ['datomic.fulltext :as 'ft]
          ['datomic.config :as 'config]
          ['datomic.common :as 'common :refer (clojure.core/list 'compare 'qualified-symbol?)]
          ['datomic.datalog :as 'datalog]
          ['datomic.error :as 'error]
          ['datomic.pull :as 'pull]
          ['datomic.query.support :as 'qs]
          ['datomic.measure.query-stats :as 'query-stats]
          ['datomic.measure.io-stats :as 'io-stats]
          ['clojure.set :as 'set]
          ['clojure.pprint :as 'pp]
          'datomic.extensions
          'datomic.aggregation)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'clojure.lang.IPersistentVector)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.Entity)
        (clojure.core/import 'datomic.db.IDbImpl)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.db.Db)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.btset.IDataSet)
        (clojure.core/import 'java.util.List)
        (clojure.core/import 'java.util.Set)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'java.util.Collection)
        (clojure.core/import 'datomic.iter.Iter))))
  (set! *warn-on-reflection* true)
  (declare emap)
  (defn rae
    ([db r a]
      (let [attrid (db/resolve-id db a)
            rid (db/resolve-id db r)
            iter (db/windowed
                   db
                   (fn fn__15346
                     ([p1__15343#]
                       (and
                         (= rid (.getV ^datomic.impl.db.IDatum p1__15343#))
                         (= attrid (long (.getA ^datomic.impl.db.IDatum p1__15343#))))))
                   (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid)))
            attr (and attrid (db/attribute db attrid))
            component? (and attr (.-isComponent ^datomic.db.Attribute attr))]
        (when iter
          (if component?
            (emap db (long (.getE (.get ^datomic.iter.Iter iter))))
            (iter/reduce
              (fn fn__15349
                ([p1__15344# p2__15345#]
                  (conj p1__15344# (emap db (long (.getE ^datomic.impl.db.IDatum p2__15345#))))))
              #{}
              iter))))))
  (defn ref-val ([db ref? v] (if ref? (or (db/resolve-kw db v) (emap db v)) v)))
  (reset-meta!
    #'ref-val
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'ref? 'v]), :column 1}
      :name
      'ref-val
      :ns
      *ns*))
  (defn eav
    ([db e a]
      (if (db/reverse-lookup? db a)
        (rae db e (keyword (namespace a) (subs (name a) 1)))
        (let [attrid (db/resolve-id db a)
              eid (db/resolve-id db e)
              iter (db/windowed
                     db
                     (fn fn__15359
                       ([p1__15356#]
                         (and
                           (= eid (long (.getE ^datomic.impl.db.IDatum p1__15356#)))
                           (= attrid (long (.getA ^datomic.impl.db.IDatum p1__15356#))))))
                     (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid :a attrid)))
              attr (and attrid (db/attribute db attrid))
              vtypeid (and attr (.-vtypeid ^datomic.db.Attribute attr))
              ref? (and vtypeid (= vtypeid 20))
              maybe_bind (fn maybe_bind ([v] (ref-val db ref? v)))]
          (when iter
            (if (and attr (= 36 (.-cardinality ^datomic.db.Attribute attr)))
              (iter/reduce
                (fn fn__15364
                  ([p1__15357# p2__15358#]
                    (conj
                      p1__15357#
                      (^clojure.lang.IFn maybe_bind (.getV ^datomic.impl.db.IDatum p2__15358#)))))
                #{}
                iter)
              (^clojure.lang.IFn maybe_bind (.getV (.get ^datomic.iter.Iter iter)))))))))
  (defn get-lazy-entity
    ([db ent]
      (let [eid (db/resolve-id db ent)
            ret (iter/reduce
                  (fn fn__15373
                    ([ret d]
                      (let [attrid (.getA ^datomic.impl.db.IDatum d)
                            attr (.elementAt
                                   ^datomic.db.IDbImpl db
                                   (java.lang.Integer/valueOf (int attrid)))
                            attrk (if attr
                                    (.kw ^datomic.db.Attribute attr)
                                    (.keywordOf
                                      ^datomic.db.IDb db
                                      (java.lang.Integer/valueOf (int attrid))))
                            v (.getV ^datomic.impl.db.IDatum d)
                            vtypeid (and attr (.-vtypeid ^datomic.db.Attribute attr))
                            ref? (and vtypeid (= vtypeid 20))
                            val (ref-val db ref? v)]
                        (assoc
                          (or ret {})
                          attrk
                          (if (and attr (= 36 (.-cardinality ^datomic.db.Attribute attr)))
                            (conj (get ret attrk #{}) val)
                            val)))))
                  nil
                  (db/windowed
                    db
                    (fn fn__15379
                      ([p1__15371#] (= eid (long (.getE ^datomic.impl.db.IDatum p1__15371#)))))
                    (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid))))]
        ret)))
  (definterface EMapImpl (^java.lang.Object cache []))
  (clojure.core/import 'datomic.query.EMapImpl)
  (defn touch ([e] (if (keyword? e) e (.touch ^datomic.Entity e))))
  (deftype
    EntityMap
    [db eid ^{:unsynchronized-mutable true} cache edits]
    java.lang.Object
    clojure.lang.Associative
    datomic.query.EMapImpl
    datomic.Entity
    clojure.lang.ILookup
    clojure.lang.IPersistentCollection
    clojure.lang.Seqable
    (valAt
      [this k not_found]
      (let [k (db/normalize-kw k) v (get edits k this)]
        (if (identical? v this)
          (let [v (get cache k this)]
            (if (identical? v this)
              (let [v (eav db eid k)]
                (if (nil? v) not_found (do (set! cache (assoc cache k v)) v)))
              v))
          v)))
    (valAt [this k] (.valAt this k nil))
    (^clojure.lang.IMapEntry entryAt
      [this k]
      (let [v (.valAt this k this)]
        (when-not (identical? v this) (clojure.lang.MapEntry. (db/normalize-kw k) v))))
    (^boolean containsKey
      [this k]
      (.booleanValue (let [v (.valAt this k this)] (not (identical? v this)))))
    (^clojure.lang.ISeq seq
      [this]
      (let [m (get-lazy-entity db eid)]
        (seq
          (concat
            (remove
              (fn fn__15399
                ([p__15398]
                  (let [vec__15400 p__15398
                        k (nth vec__15400 (int 0) nil)
                        v (nth vec__15400 (int 1) nil)]
                    (contains? edits k))))
              m)
            (remove
              (fn fn__15405
                ([p__15404]
                  (let [vec__15406 p__15404
                        k (nth vec__15406 (int 0) nil)
                        v (nth vec__15406 (int 1) nil)]
                    (nil? v))))
              edits)))))
    (^datomic.Database db [this] ^datomic.Database db)
    (^java.util.Set keySet [this] (into #{} (map (comp str first) (seq this))))
    (^datomic.Entity touch
      [this]
      (do
        (loop [seq_15386 (seq (keys this)) chunk_15387 nil count_15388 0 i_15389 0]
          (if (< i_15389 count_15388)
            (let [a (.nth ^clojure.lang.Indexed chunk_15387 (int i_15389))]
              (let [attrid (db/resolve-id db a)
                    attr (.elementAt ^datomic.db.IDbImpl db attrid)
                    v (.valAt this a)]
                (when (and
                        (.-isComponent ^datomic.db.Attribute attr)
                        (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                  (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                    (loop [seq_15390 (seq v) chunk_15391 nil count_15392 0 i_15393 0]
                      (if (< i_15393 count_15392)
                        (let [v (.nth ^clojure.lang.Indexed chunk_15391 (int i_15393))]
                          (touch v)
                          (recur seq_15390 chunk_15391 count_15392 (inc i_15393)))
                        (let [temp__5804__auto__ (seq seq_15390)]
                          (when temp__5804__auto__
                            (let [seq_15390 temp__5804__auto__]
                              (if (chunked-seq? seq_15390)
                                (let [c__6065__auto__ (chunk-first seq_15390)]
                                  (recur
                                    (chunk-rest seq_15390)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [v (first seq_15390)]
                                  (touch v)
                                  (recur (next seq_15390) nil 0 0))))))))
                    (touch v))))
              (recur seq_15386 chunk_15387 count_15388 (inc i_15389)))
            (let [temp__5804__auto__ (seq seq_15386)]
              (when temp__5804__auto__
                (let [seq_15386 temp__5804__auto__]
                  (if (chunked-seq? seq_15386)
                    (let [c__6065__auto__ (chunk-first seq_15386)]
                      (recur
                        (chunk-rest seq_15386)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [a (first seq_15386)]
                      (let [attrid (db/resolve-id db a)
                            attr (.elementAt ^datomic.db.IDbImpl db attrid)
                            v (.valAt this a)]
                        (when (and
                                (.-isComponent ^datomic.db.Attribute attr)
                                (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                          (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                            (loop [seq_15394 (seq v) chunk_15395 nil count_15396 0 i_15397 0]
                              (if (< i_15397 count_15396)
                                (let [v (.nth ^clojure.lang.Indexed chunk_15395 (int i_15397))]
                                  (touch v)
                                  (recur seq_15394 chunk_15395 count_15396 (inc i_15397)))
                                (let [temp__5804__auto__ (seq seq_15394)]
                                  (when temp__5804__auto__
                                    (let [seq_15394 temp__5804__auto__]
                                      (if (chunked-seq? seq_15394)
                                        (let [c__6065__auto__ (chunk-first seq_15394)]
                                          (recur
                                            (chunk-rest seq_15394)
                                            c__6065__auto__
                                            (int (count c__6065__auto__))
                                            (int 0)))
                                        (let [v (first seq_15394)]
                                          (touch v)
                                          (recur (next seq_15394) nil 0 0))))))))
                            (touch v))))
                      (recur (next seq_15386) nil 0 0))))))))
        this))
    (get [this k] (.valAt this k))
    (^clojure.lang.IPersistentCollection empty [this] (emap db eid))
    (^int count [this] (count (seq this)))
    (^boolean equiv [this o] (.equals this o))
    (^int hashCode
      [this]
      (clojure.lang.Util/hashCombine
        (int (hash (.getRawId ^datomic.db.IDbImpl db)))
        (int (hash eid))))
    (^boolean equals
      [this other]
      (and
        (instance? EntityMap other)
        (= eid (.-eid ^EntityMap other))
        (= (.getRawId ^datomic.db.IDbImpl db) (.getRawId (.db ^datomic.Entity other)))))
    (^java.lang.String toString [this] (binding [*print-length* 20 *print-level* 3] (pr-str this)))
    (cache [this] cache))
  (clojure.core/import 'datomic.query.EntityMap)
  (defn ->EntityMap ([db eid cache edits] (datomic.query.EntityMap. db eid cache edits)))
  (defn emap ([db eid] (datomic.query.EntityMap. db eid #:db{:id eid} nil)))
  (defmethod
    print-method
    datomic.query.EntityMap
    fn__15425
    ([m w]
      (print-method
        (merge (.cache ^datomic.query.EMapImpl m) (.-edits ^datomic.query.EntityMap m))
        w)))
  (defonce Immutify {})
  (defprotocol Immutify (immutify [x]))
  (extend nil Immutify {:immutify (fn fn__15443 ([x] x))})
  (extend java.lang.Object Immutify {:immutify (fn fn__15445 ([x] x))})
  (extend
    java.util.Map
    Immutify
    {:immutify
     (fn fn__15447
       ([x]
         (if (map? x)
           x
           (persistent!
             (reduce
               (fn fn__15449
                 ([m p__15448]
                   (let [vec__15450 p__15448
                         k (nth vec__15450 (int 0) nil)
                         v (nth vec__15450 (int 1) nil)]
                     (assoc! m (immutify k) (immutify v)))))
               (transient {})
               x)))))})
  (extend
    java.util.Set
    Immutify
    {:immutify (fn fn__15455 ([x] (if (set? x) x (set (map immutify x)))))})
  (extend
    java.util.List
    Immutify
    {:immutify (fn fn__15457 ([x] (if (sequential? x) x (mapv immutify x))))})
  (defn listq->mapq
    ([lq]
      (reduce
        (fn fn__15460
          ([m p__15459]
            (let [vec__15461 p__15459
                  k (nth vec__15461 (int 0) nil)
                  v (nth vec__15461 (int 1) nil)
                  k (first k)]
              (assoc m k v))))
        {}
        (partition 2 (partition-by #{:find :where :syms :keys :with :timeout :strs :in} lq)))))
  (defn move-sources-to-meta
    ([p__15467]
      (let [map__15468 p__15467
            map__15468 (if (seq? map__15468)
                         (if (next map__15468)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15468))
                           (if (seq map__15468) (first map__15468) {}))
                         map__15468)
            q map__15468
            srcs (get map__15468 :in)
            clauses (get map__15468 :where)]
        (if srcs
          (let [sset (set srcs)]
            (assoc
              q
              :where
              (map
                (fn fn__15469
                  ([p1__15466#]
                    (if (^clojure.lang.IFn sset (first p1__15466#))
                      (with-meta (next p1__15466#) {:tag (first p1__15466#)})
                      (do
                        (when (datalog/source? (first p1__15466#))
                          (throw
                            (java.lang.IllegalArgumentException.
                              (str "Data source not supplied: " (first p1__15466#)))))
                        (when :else p1__15466#)))))
                clauses)))
          q))))
  (defn flatten-listy
    ([x]
      (let [listy? (fn listy_QMARK_
                     ([p1__15472#]
                       (or (sequential? p1__15472#) (instance? java.util.List p1__15472#))))]
        (filter (complement listy?) (rest (tree-seq listy? seq x))))))
  (reset-meta!
    #'flatten-listy
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column 1}
      :name
      'flatten-listy
      :ns
      *ns*))
  (defn variables-in-clause
    ([clause] (into #{} (filter datalog/variable? (flatten-listy clause)))))
  (reset-meta!
    #'variables-in-clause
    (assoc
      {:private true, :arglists (clojure.core/list ['clause]), :column 1}
      :name
      'variables-in-clause
      :ns
      *ns*))
  (defn validate-query
    ([qmap]
      (when-not (seq (:find qmap))
        (throw (java.lang.IllegalArgumentException. "No :find clause specified")))
      (when-not (or (seq (:in qmap)) (seq (:where qmap)))
        (throw (java.lang.IllegalArgumentException. "No :in or :where clause specified")))
      (loop [seq_15478 (seq (:find qmap)) chunk_15479 nil count_15480 0 i_15481 0]
        (if (< i_15481 count_15480)
          (let [f (.nth ^clojure.lang.Indexed chunk_15479 (int i_15481))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :find is not a variable"))))
            (recur seq_15478 chunk_15479 count_15480 (inc i_15481)))
          (let [temp__5804__auto__ (seq seq_15478)]
            (when temp__5804__auto__
              (let [seq_15478 temp__5804__auto__]
                (if (chunked-seq? seq_15478)
                  (let [c__6065__auto__ (chunk-first seq_15478)]
                    (recur
                      (chunk-rest seq_15478)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [f (first seq_15478)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :find is not a variable"))))
                    (recur (next seq_15478) nil 0 0))))))))
      (loop [seq_15482 (seq (:with qmap)) chunk_15483 nil count_15484 0 i_15485 0]
        (if (< i_15485 count_15484)
          (let [f (.nth ^clojure.lang.Indexed chunk_15483 (int i_15485))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :with is not a variable"))))
            (recur seq_15482 chunk_15483 count_15484 (inc i_15485)))
          (let [temp__5804__auto__ (seq seq_15482)]
            (when temp__5804__auto__
              (let [seq_15482 temp__5804__auto__]
                (if (chunked-seq? seq_15482)
                  (let [c__6065__auto__ (chunk-first seq_15482)]
                    (recur
                      (chunk-rest seq_15482)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [f (first seq_15482)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :with is not a variable"))))
                    (recur (next seq_15482) nil 0 0))))))))
      (loop [seq_15486 (seq (:where qmap)) chunk_15487 nil count_15488 0 i_15489 0]
        (if (< i_15489 count_15488)
          (let [w (.nth ^clojure.lang.Indexed chunk_15487 (int i_15489))]
            (when-not (instance? java.util.List w)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " w " in :where is not a list"))))
            (recur seq_15486 chunk_15487 count_15488 (inc i_15489)))
          (let [temp__5804__auto__ (seq seq_15486)]
            (when temp__5804__auto__
              (let [seq_15486 temp__5804__auto__]
                (if (chunked-seq? seq_15486)
                  (let [c__6065__auto__ (chunk-first seq_15486)]
                    (recur
                      (chunk-rest seq_15486)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [w (first seq_15486)]
                    (when-not (instance? java.util.List w)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " w " in :where is not a list"))))
                    (recur (next seq_15486) nil 0 0))))))))
      (loop [seq_15490 (seq (:in qmap)) chunk_15491 nil count_15492 0 i_15493 0]
        (if (< i_15493 count_15492)
          (let [i (.nth ^clojure.lang.Indexed chunk_15491 (int i_15493))]
            (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " i " in :in is not a source"))))
            (recur seq_15490 chunk_15491 count_15492 (inc i_15493)))
          (let [temp__5804__auto__ (seq seq_15490)]
            (when temp__5804__auto__
              (let [seq_15490 temp__5804__auto__]
                (if (chunked-seq? seq_15490)
                  (let [c__6065__auto__ (chunk-first seq_15490)]
                    (recur
                      (chunk-rest seq_15490)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [i (first seq_15490)]
                    (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " i " in :in is not a source"))))
                    (recur (next seq_15490) nil 0 0))))))))
      (let [temp__5804__auto__ (seq (datalog/callees (:construct qmap)))]
        (when temp__5804__auto__
          (let [callees temp__5804__auto__]
            (throw
              (java.lang.IllegalArgumentException.
                (str (first callees) " is not valid in :construct")))))
        nil)
      (let [temp__5804__auto__ (seq
                                 (set/difference
                                   (variables-in-clause (:construct qmap))
                                   (variables-in-clause (:find qmap))))]
        (when temp__5804__auto__
          (let [unfound temp__5804__auto__]
            (error/arg
              :db.error/unfound-construct-variables
              (str "Query construct references variables not in :find: " unfound)
              {:variables unfound}))))
      (let [outvars (set/union
                      (variables-in-clause (:with qmap))
                      (variables-in-clause (:find qmap)))
            invars (set/union (variables-in-clause (:in qmap)) (variables-in-clause (:where qmap)))
            missing (set/difference outvars invars)]
        (when (seq missing)
          (error/arg
            :db.error/unbound-query-variables
            (str "Query is referencing unbound variables: " missing)
            {:variables missing})))
      qmap))
  (defn process-in-bindings
    ([qmap prefix]
      (let [temp__5802__auto__ (:in qmap)]
        (if temp__5802__auto__
          (let [ins temp__5802__auto__
                binding? (fn binding_QMARK_
                           ([p1__15510#]
                             (or
                               (datalog/variable? p1__15510#)
                               (instance? java.util.List p1__15510#))))]
            (reduce
              (fn fn__15518
                ([m p__15517]
                  (let [vec__15519 p__15517
                        i (nth vec__15519 (int 0) nil)
                        x (nth vec__15519 (int 1) nil)]
                    (if (^clojure.lang.IFn binding? x)
                      (let [gs (symbol (str prefix (inc i))) bind_type (datalog/binding-type x)]
                        (cond->
                          (update-in
                            (update-in m [:in] conj gs)
                            [:where]
                            (fn fn__15523 ([p1__15512# p2__15511#] (cons p2__15511# p1__15512#)))
                            (apply
                              vector
                              (seq
                                (-> (clojure.core/list 'ground)
                                 (concat (clojure.core/list gs))
                                 (seq)
                                 (clojure.core/list)
                                 (concat (clojure.core/list x))))))
                          (= bind_type :scalar)
                          (update-in [:in-consts] assoc x gs)
                          (= bind_type :tuple)
                          (update-in
                            [:in-consts]
                            merge
                            (zipmap
                              x
                              (map
                                (fn fn__15525 ([p1__15513#] (vector gs p1__15513#)))
                                (range))))))
                      (update-in m [:in] conj x)))))
              (assoc qmap :in [])
              (map-indexed vector ins)))
          qmap))))
  (defn process-ranges
    ([qmap]
      (let [consts (:in-consts qmap)
            const? (fn const_QMARK_
                     ([p1__15530#]
                       (or (get consts p1__15530#) (not (datalog/variable? p1__15530#)))))
            swap {'< '>, '<= '>=, '> '<, '>= '<=, '= '=}
            norm (fn norm
                   ([p__15535]
                     (let [vec__15537 p__15535
                           vec__15540 (nth vec__15537 (int 0) nil)
                           cmp (nth vec__15540 (int 0) nil)
                           x (nth vec__15540 (int 1) nil)
                           y (nth vec__15540 (int 2) nil)
                           c vec__15540]
                       (cond
                         (and (not (^clojure.lang.IFn const? x)) (^clojure.lang.IFn const? y)) c
                         (and (^clojure.lang.IFn const? x) (not (^clojure.lang.IFn const? y))) (do
                                                                                                 (clojure.core/list
                                                                                                   (^clojure.lang.IFn swap
                                                                                                     cmp)
                                                                                                   y
                                                                                                   x))))))
            cexprs (keep
                     (fn fn__15546
                       ([p1__15531#]
                         (let [or__5581__auto__ (and
                                                  (instance? java.util.List p1__15531#)
                                                  (instance? java.util.List (first p1__15531#))
                                                  (= (long (count p1__15531#)) 1)
                                                  (#{'>= '> '<= '= '<} (ffirst p1__15531#))
                                                  (^clojure.lang.IFn norm p1__15531#))]
                           (when or__5581__auto__ or__5581__auto__))))
                     (:where qmap))
            assoc_stronger (fn assoc_stronger
                             ([m k v cmp]
                               (let [ev (get m k)]
                                 (if (or (nil? ev) (= '= cmp)) (assoc m k v) m))))]
        (reduce
          (fn fn__15557
            ([m p__15556]
              (let [vec__15558 p__15556
                    cmp (nth vec__15558 (int 0) nil)
                    var (nth vec__15558 (int 1) nil)
                    const (nth vec__15558 (int 2) nil)]
                (cond->
                  m
                  (#{'>= '> '=} cmp)
                  (update-in [:range-starts] assoc_stronger var const cmp)
                  (#{'<= '= '<} cmp)
                  (update-in [:range-whiles] assoc_stronger var [cmp const] cmp)))))
          qmap
          cexprs))))
  (defn process-aggregates
    ([qmap]
      (let [finds (:find qmap)
            list? (fn list_QMARK_ ([p1__15564#] (instance? java.util.List p1__15564#)))
            qmap (if (or (:with qmap) (some list? finds))
                   (assoc
                     qmap
                     :find
                     (vec
                       (distinct
                         (map
                           (fn fn__15568
                             ([p1__15565#]
                               (if (^clojure.lang.IFn list? p1__15565#)
                                 (last p1__15565#)
                                 p1__15565#)))
                           finds)))
                     :group
                     (vec finds))
                   qmap)
            qmap (let [temp__5802__auto__ (:with qmap)]
                   (if temp__5802__auto__
                     (let [w temp__5802__auto__] (assoc qmap :find (into (vec (:find qmap)) w)))
                     qmap))]
        qmap)))
  (defn process-find-bindings
    ([p__15574]
      (let [map__15575 p__15574
            map__15575 (if (seq? map__15575)
                         (if (next map__15575)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15575))
                           (if (seq map__15575) (first map__15575) {}))
                         map__15575)
            qmap map__15575
            find (get map__15575 :find)]
        (cond
          (and
            (= 1 (long (count find)))
            (instance? java.util.List (first find))
            (every?
              (fn fn__15576
                ([p1__15573#]
                  (or (datalog/variable? p1__15573#) (instance? java.util.List p1__15573#))))
              (first find))) (assoc qmap :find (first find) :find-bindings 'first-tuple)
          (and
            (= 1 (long (count find)))
            (instance? java.util.List (first find))
            (= 2 (long (count (first find))))
            (= '... (second (first find)))) (assoc
                                              qmap
                                              :find
                                              (take 1 (first find))
                                              :find-bindings
                                              'one-column)
          (and (= 2 (long (count find))) (= '. (second find))) (assoc
                                                                 qmap
                                                                 :find
                                                                 (take 1 find)
                                                                 :find-bindings
                                                                 'one-value)
          :default (do qmap)))))
  (defn pattern? ([s] (and (not (datalog/variable? s)) (not (datalog/source? s)))))
  (defn normalize-pull
    ([expr]
      (let [result (if (datalog/source? (second expr))
                     expr
                     (concat [(first expr) '$] (rest expr)))]
        (when-not (and (datalog/variable? (nth result (int 2))) (pattern? (nth result (int 3))))
          (error/arg :db.error/invalid-pull (str "Invalid pull expression " expr)))
        result)))
  (defn process-pulls
    ([p__15593]
      (let [map__15594 p__15593
            map__15594 (if (seq? map__15594)
                         (if (next map__15594)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15594))
                           (if (seq map__15594) (first map__15594) {}))
                         map__15594)
            qmap map__15594
            find (get map__15594 :find)
            in (get map__15594 :in)
            pull? (fn pull_QMARK_
                    ([p1__15590#]
                      (and (instance? java.util.List p1__15590#) (= 'pull (first p1__15590#)))))]
        (if (some pull? find)
          (assoc
            qmap
            :pull
            (mapv
              (fn fn__15598
                ([p1__15591#]
                  (if (^clojure.lang.IFn pull? p1__15591#)
                    (let [vec__15599 (normalize-pull p1__15591#)
                          _ (nth vec__15599 (int 0) nil)
                          db (nth vec__15599 (int 1) nil)
                          var (nth vec__15599 (int 2) nil)
                          pattern (nth vec__15599 (int 3) nil)]
                      (when (and (symbol? pattern) (not (some #{pattern} in)))
                        (error/arg
                          :db.error/pattern-not-bound
                          (str "Pull pattern not found in inputs: " pattern)))
                      {:db db, :var var, :pattern pattern})
                    p1__15591#)))
              find)
            :find
            (mapv
              (fn fn__15604
                ([p1__15592#]
                  (if (^clojure.lang.IFn pull? p1__15592#)
                    (first (filter datalog/variable? p1__15592#))
                    p1__15592#)))
              find))
          qmap))))
  (defn mapify-query
    ([query]
      (let [query (if (string? query) (binding [*read-eval* false] (read-string query)) query)
            query (immutify query)
            query (if (sequential? query) (listq->mapq query) query)]
        (when-not (map? query)
          (throw
            (java.lang.IllegalArgumentException.
              "query must be a readable edn string, list, or map")))
        query)))
  (defn rename-self-unifications
    ([qmap padding]
      (update-in
        qmap
        [:where]
        (fn fn__15611
          ([p1__15610#]
            (reduce
              (fn fn__15612
                ([result clause]
                  (let [result result
                        syms (java.util.HashSet.)
                        rclause []
                        G__15616 clause
                        vec__15617 G__15616
                        seq__15618 (seq vec__15617)
                        first__15619 (first seq__15618)
                        seq__15618 (next seq__15618)
                        item first__15619
                        more seq__15618]
                    (loop [result result syms syms rclause rclause G__15616 G__15616]
                      (let [result result
                            syms syms
                            rclause rclause
                            vec__15620 G__15616
                            seq__15621 (seq vec__15620)
                            first__15622 (first seq__15621)
                            seq__15621 (next seq__15621)
                            item first__15622
                            more seq__15621]
                        (if item
                          (if (datalog/variable? item)
                            (if (.add ^java.util.HashSet syms item)
                              (recur result syms (conj rclause item) more)
                              (let [rsym (gensym (str item padding))]
                                (recur
                                  (conj
                                    result
                                    (apply
                                      vector
                                      (seq
                                        (-> (clojure.core/list '=)
                                         (concat (clojure.core/list item) (clojure.core/list rsym))
                                         (seq)
                                         (clojure.core/list)
                                         (concat)))))
                                  syms
                                  (conj rclause rsym)
                                  more)))
                            (recur result syms (conj rclause item) more))
                          (conj result rclause)))))))
              []
              p1__15610#))))))
  (reset-meta!
    #'rename-self-unifications
    (assoc
      {:private true, :arglists (clojure.core/list ['qmap 'padding]), :column 1}
      :name
      'rename-self-unifications
      :ns
      *ns*))
  (defn gensym-padding
    ([qmap]
      (apply
        str
        (repeat
          (apply
            max
            0
            (map (comp dec count str) (filter datalog/variable? (flatten (:where qmap)))))
          "-"))))
  (reset-meta!
    #'gensym-padding
    (assoc
      {:private true, :arglists (clojure.core/list ['qmap]), :column 1}
      :name
      'gensym-padding
      :ns
      *ns*))
  (defn has-self-unifications?
    ([qmap]
      (boolean
        (first
          (remove
            (fn fn__15627
              ([clause]
                (=
                  (long (count (filter datalog/variable? clause)))
                  (long (count (set (filter datalog/variable? clause)))))))
            (:where qmap))))))
  (reset-meta!
    #'has-self-unifications?
    (assoc
      {:private true, :arglists (clojure.core/list ['qmap]), :column 1}
      :name
      'has-self-unifications?
      :ns
      *ns*))
  (defn process-self-unifications
    ([qmap]
      (if (has-self-unifications? qmap)
        (rename-self-unifications qmap (gensym-padding qmap))
        qmap)))
  (reset-meta!
    #'process-self-unifications
    (assoc
      {:private true, :arglists (clojure.core/list ['qmap]), :column 1}
      :name
      'process-self-unifications
      :ns
      *ns*))
  (defn compile-construct-1
    ([smap clause]
      (cons
        'vector
        (map
          (fn fn__15631
            ([x]
              (if (datalog/variable? x)
                (let [pos (get smap x)]
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/get)
                      (clojure.core/list 'tuple)
                      (clojure.core/list pos))))
                x)))
          clause))))
  (defn compile-construct-n
    ([find_clause construct_clause]
      (let [smap (zipmap find_clause (range))]
        (seq
          (concat
            (clojure.core/list 'clojure.core/fn)
            (clojure.core/list (apply vector (seq (concat (clojure.core/list 'tuple)))))
            (clojure.core/list
              (apply
                vector
                (seq (concat (mapv (partial compile-construct-1 smap) construct_clause))))))))))
  (defn compile-construct
    ([query]
      (let [temp__5802__auto__ (:construct query)]
        (if temp__5802__auto__
          (let [construct temp__5802__auto__ src (compile-construct-n (:find query) construct)]
            (assoc query :construct-fn-src src :construct-fn (eval src)))
          query))))
  (defn resolve-qualified-fn
    ([x]
      (let [temp__5804__auto__ (and (sequential? x) (first x))]
        (when temp__5804__auto__
          (let [f temp__5804__auto__]
            (when (common/qualified-symbol? f) (common/maybe-require 'datomic.extensions f)))))))
  (defn resolve-qualified-fns
    ([p__15640]
      (let [map__15641 p__15640
            map__15641 (if (seq? map__15641)
                         (if (next map__15641)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15641))
                           (if (seq map__15641) (first map__15641) {}))
                         map__15641)
            where (get map__15641 :where)
            group (get map__15641 :group)]
        (loop [seq_15642 (seq (map first where)) chunk_15643 nil count_15644 0 i_15645 0]
          (if (< i_15645 count_15644)
            (let [clause (.nth ^clojure.lang.Indexed chunk_15643 (int i_15645))]
              (resolve-qualified-fn clause)
              (recur seq_15642 chunk_15643 count_15644 (inc i_15645)))
            (let [temp__5804__auto__ (seq seq_15642)]
              (when temp__5804__auto__
                (let [seq_15642 temp__5804__auto__]
                  (if (chunked-seq? seq_15642)
                    (let [c__6065__auto__ (chunk-first seq_15642)]
                      (recur
                        (chunk-rest seq_15642)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [clause (first seq_15642)]
                      (resolve-qualified-fn clause)
                      (recur (next seq_15642) nil 0 0))))))))
        (loop [seq_15646 (seq group) chunk_15647 nil count_15648 0 i_15649 0]
          (if (< i_15649 count_15648)
            (let [clause (.nth ^clojure.lang.Indexed chunk_15647 (int i_15649))]
              (resolve-qualified-fn clause)
              (recur seq_15646 chunk_15647 count_15648 (inc i_15649)))
            (let [temp__5804__auto__ (seq seq_15646)]
              (when temp__5804__auto__
                (let [seq_15646 temp__5804__auto__]
                  (if (chunked-seq? seq_15646)
                    (let [c__6065__auto__ (chunk-first seq_15646)]
                      (recur
                        (chunk-rest seq_15646)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [clause (first seq_15646)]
                      (resolve-qualified-fn clause)
                      (recur (next seq_15646) nil 0 0)))))))))))
  (defn parse-query
    ([query]
      (let [query (mapify-query query)
            query (process-self-unifications query)
            query (process-find-bindings query)
            query (process-pulls query)
            query (process-aggregates query)
            query (process-in-bindings query "$__in__")
            query (process-ranges query)
            query (validate-query query)]
        (resolve-qualified-fns query)
        query)))
  (defn load-query
    ([query]
      (let [query (parse-query query)
            query (move-sources-to-meta query)
            vec__15656 (datalog/prep-clauses nil (:where query))
            rm (nth vec__15656 (int 0) nil)
            cs (nth vec__15656 (int 1) nil)
            query (assoc query :where cs :arules rm)
            query (compile-construct query)]
        query)))
  (defn group-rel
    ([fv rel]
      (let [grp_idxs (filterv integer? fv)]
        (if (= (count fv) (count grp_idxs))
          (mapv
            (fn fn__15661
              ([p1__15660#] (subvec p1__15660# 0 (java.lang.Integer/valueOf (int (count fv))))))
            rel)
          (let [cmp (reify
                      java.util.Comparator
                      (^int compare
                        [this r1 r2]
                        (int
                          (loop [i 0]
                            (if (= i (count grp_idxs))
                              0
                              (let [j (^clojure.lang.IFn grp_idxs (long i))
                                    c (common/compare
                                        (nth r1 (int ^java.lang.Number j))
                                        (nth r2 (int ^java.lang.Number j)))]
                                (if (= c 0) (recur (inc i)) c)))))))
                srel (java.util.ArrayList. ^java.util.Collection rel)]
            (java.util.Collections/sort ^java.util.List srel ^java.util.Comparator cmp)
            (let [_ nil
                  agg (fn agg
                        ([i r cnt]
                          (reify
                            java.util.Collection
                            java.util.RandomAccess
                            java.util.List
                            (get
                              [this ^int off]
                              (nth
                                (.get ^java.util.ArrayList srel (int (+ r off)))
                                (int ^java.lang.Number i)))
                            (^java.util.Iterator iterator
                              [this]
                              (let [off (atom 0)]
                                (reify
                                  java.util.Iterator
                                  (next
                                    [this]
                                    (let [ret (nth
                                                (.get
                                                  ^java.util.ArrayList srel
                                                  (int (+ r (deref off))))
                                                (int ^java.lang.Number i))]
                                      (swap! off inc)
                                      ret))
                                  (^boolean hasNext [this] (boolean (< (deref off) cnt))))))
                            (^"[Ljava.lang.Object;" toArray
                              [this]
                              (let [arr (object-array cnt)]
                                (dotimes [i (long cnt)]
                                  (aset ^"[Ljava.lang.Object;" arr (int i) (.get this (int i))))
                                arr))
                            (^int size [this] (.intValue ^java.lang.Number cnt))
                            (^boolean isEmpty [this] (.booleanValue false)))))]
              (loop [ret [] r 0]
                (if (= r (.size ^java.util.ArrayList srel))
                  ret
                  (let [row (.get ^java.util.ArrayList srel (int r))
                        nextr (long
                                (loop [nr (inc r)]
                                  (cond
                                    (= nr (.size ^java.util.ArrayList srel)) (long nr)
                                    (=
                                      (.compare
                                        ^java.util.Comparator cmp
                                        row
                                        (.get ^java.util.ArrayList srel (int nr)))
                                      0) (recur (inc nr))
                                    :else (do (long nr)))))
                        nextrow (object-array (java.lang.Integer/valueOf (int (count fv))))
                        cnt (- nextr r)]
                    (dotimes [i (count fv)]
                      (let [fve (nth fv (int i))]
                        (aset
                          ^"[Ljava.lang.Object;" nextrow
                          (int i)
                          (if (integer? fve)
                            (nth row (int ^java.lang.Number fve))
                            (let [vec__15674 fve
                                  idx (nth vec__15674 (int 0) nil)
                                  f (nth vec__15674 (int 1) nil)]
                              (^clojure.lang.IFn f
                                (^clojure.lang.IFn agg idx (long r) (long cnt))))))))
                    (recur (conj ret (vec nextrow)) nextr))))))))))
  (def query-cache (cache/create-computing load-query 1000))
  (defn group-fv
    ([find_clause group_clause]
      (let [smap (zipmap find_clause (range))
            gns (find-ns 'datomic.aggregation)
            list? (fn list_QMARK_ ([p1__15679#] (instance? java.util.List p1__15679#)))]
        (mapv
          (fn fn__15683
            ([p1__15680#]
              (if (^clojure.lang.IFn list? p1__15680#)
                (let [temp__5802__auto__ (ns-resolve gns (first p1__15680#))]
                  (if temp__5802__auto__
                    (let [agg_fn temp__5802__auto__]
                      [(^clojure.lang.IFn smap (last p1__15680#))
                       (apply partial agg_fn (butlast (rest p1__15680#)))])
                    (error/arg
                      :db.error/invalid-aggregate
                      (str
                        "Argument "
                        (first p1__15680#)
                        " in :find is not an aggregate function"))))
                (^clojure.lang.IFn smap p1__15680#))))
          group_clause))))
  (defn xf-tuple
    ([fv tuple]
      (let [result (transient [])
            G__15693 tuple
            vec__15695 G__15693
            seq__15696 (seq vec__15695)
            first__15697 (first seq__15696)
            seq__15696 (next seq__15696)
            col first__15697
            tuple seq__15696
            G__15694 fv
            vec__15698 G__15694
            seq__15699 (seq vec__15698)
            first__15700 (first seq__15699)
            seq__15699 (next seq__15699)
            f first__15700
            fv seq__15699]
        (loop [result result G__15693 G__15693 G__15694 G__15694]
          (let [result result
                vec__15701 G__15693
                seq__15702 (seq vec__15701)
                first__15703 (first seq__15702)
                seq__15702 (next seq__15702)
                col first__15703
                tuple seq__15702
                vec__15704 G__15694
                seq__15705 (seq vec__15704)
                first__15706 (first seq__15705)
                seq__15705 (next seq__15705)
                f first__15706
                fv seq__15705]
            (if f
              (recur (conj! result (^clojure.lang.IFn f col)) tuple fv)
              (persistent! result)))))))
  (defn pull-fv
    ([p__15709 srcs]
      (let [map__15710 p__15709
            map__15710 (if (seq? map__15710)
                         (if (next map__15710)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15710))
                           (if (seq map__15710) (first map__15710) {}))
                         map__15710)
            qmap map__15710
            find (get map__15710 :find)
            pull (get map__15710 :pull)
            with (get map__15710 :with)
            in (get map__15710 :in)
            smap (zipmap find (range))
            srcmap (zipmap in srcs)
            colct (- (count find) (count with))]
        (reduce
          (fn fn__15712
            ([m p__15711]
              (let [map__15713 p__15711
                    map__15713 (if (seq? map__15713)
                                 (if (next map__15713)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__15713))
                                   (if (seq map__15713) (first map__15713) {}))
                                 map__15713)
                    db (get map__15713 :db)
                    var (get map__15713 :var)
                    pattern (get map__15713 :pattern)]
                (assoc
                  m
                  (common/getx smap var)
                  (fn fn__15714
                    ([p1__15708#]
                      (first
                        (pull/pull
                          (get srcmap db (first srcs))
                          (get srcmap pattern pattern)
                          [p1__15708#]))))))))
          (vec (repeat (long colct) identity))
          (filter map? pull)))))
  (defn sort-collection-by-indexed
    ([coll ^long idx]
      (let [cmp (reify
                  java.util.Comparator
                  (^int compare
                    [this a b]
                    (.intValue
                      (let [aval (.nth ^clojure.lang.Indexed a (int idx))
                            bval (.nth ^clojure.lang.Indexed b (int idx))]
                        (if (and (instance? java.lang.Long aval) (instance? java.lang.Long bval))
                          (java.lang.Integer/valueOf
                            (int (.compareTo ^java.lang.Long aval ^java.lang.Long bval)))
                          (long (common/compare aval bval)))))))
            G__15721 (java.util.ArrayList. ^java.util.Collection coll)]
        (.sort ^java.util.ArrayList G__15721 ^java.util.Comparator cmp)
        G__15721)))
  (reset-meta!
    #'sort-collection-by-indexed
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [(.withMeta 'coll {:tag 'Collection}) (.withMeta 'idx {:tag 'long})]),
       :column 1}
      :name
      'sort-collection-by-indexed
      :ns
      *ns*))
  (defn sort-for-pull
    ([query result]
      (let [temp__5802__auto__ (first
                                 (keep-indexed
                                   (fn fn__15723 ([idx item] (when (map? item) idx)))
                                   (:pull query)))]
        (if temp__5802__auto__
          (let [idx temp__5802__auto__]
            (sort-collection-by-indexed result (long ^java.lang.Number idx)))
          result))))
  (reset-meta!
    #'sort-for-pull
    (assoc
      {:private true, :arglists (clojure.core/list ['query 'result]), :column 1}
      :name
      'sort-for-pull
      :ns
      *ns*))
  (defn q*
    ([query srcs]
      (let [vec__15729 (qs/parse-as query)
            query (nth vec__15729 (int 0) nil)
            as (nth vec__15729 (int 1) nil)
            qmap (get query-cache query)]
        (when-not (<= (count (:in qmap)) (count srcs))
          (error/arg
            :db.error/too-few-inputs
            (str
              "Query expected "
              (java.lang.Integer/valueOf (int (count (:in qmap))))
              " inputs but received "
              (java.lang.Integer/valueOf (int (count srcs))))))
        (let [group_clause (group-fv (:find qmap) (:group qmap))
              ret (datalog/qsqr srcs qmap)
              ret (if (let [and__5579__auto__ (count ret)]
                        (if (java.lang.Integer/valueOf (int and__5579__auto__))
                          (or (:group qmap) (:with qmap))
                          (java.lang.Integer/valueOf (int and__5579__auto__))))
                    (group-rel group_clause ret)
                    ret)
              ret (sort-for-pull qmap ret)
              pullfn (when (:pull qmap)
                       (let [fv (pull-fv qmap srcs)]
                         (fn fn__15732 ([p1__15727#] (xf-tuple fv p1__15727#)))))
              asfn (when as
                     (fn fn__15734
                       ([p1__15728#]
                         (datomic.query.support.MapOnIndexed.
                           ^clojure.lang.IPersistentVector as
                           ^clojure.lang.Indexed p1__15728#))))
              pfn (when (or pullfn asfn) (comp (or asfn identity) (or pullfn identity)))
              temp__5802__auto__ (:find-bindings qmap)]
          (if temp__5802__auto__
            (let [fb temp__5802__auto__ ret (if pfn (map pfn ret) ret)]
              [(let [G__15736 fb]
                 (case
                   G__15736
                   one-value
                   (ffirst ret)
                   first-tuple
                   (first ret)
                   one-column
                   (mapv first ret)))
               nil])
            [ret pfn])))))
  (defn query*
    ([query_map]
      (let [timeout (:timeout query_map)
            qmap (cond-> (mapify-query (:query query_map)) timeout (assoc :timeout [timeout]))]
        (q* qmap (:args query_map)))))
  (defn apply-pf
    ([p__15746]
      (let [vec__15747 p__15746
            result (nth vec__15747 (int 0) nil)
            pf (nth vec__15747 (int 1) nil)]
        (if pf (mapv pf result) result))))
  (reset-meta!
    #'apply-pf
    (assoc
      {:private true, :arglists (clojure.core/list [['result 'pf]]), :column 1}
      :name
      'apply-pf
      :ns
      *ns*))
  (defn q ([query srcs] (apply-pf (q* query srcs))))
  (defn query
    ([p__15752]
      (let [map__15753 p__15752
            map__15753 (if (seq? map__15753)
                         (if (next map__15753)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15753))
                           (if (seq map__15753) (first map__15753) {}))
                         map__15753)
            query_map map__15753
            io_context (get map__15753 :io-context)
            query_stats (get map__15753 :query-stats)
            f (fn f ([] (apply-pf (query* query_map))))
            f (if io_context
                (fn fn__15756
                  ([]
                    (io-stats/throw-if-ex!
                      (io-stats/with-io-stats f {:io-context io_context, :api :query}))))
                f)
            f (if query_stats
                (fn fn__15758 ([] (query-stats/with-query-stats f {:query (:query query_map)})))
                f)]
        (^clojure.lang.IFn f))))
  (defn qseq
    ([query_map args] (qseq {:query query_map, :args args}))
    ([p__15761]
      (let [map__15762 p__15761
            map__15762 (if (seq? map__15762)
                         (if (next map__15762)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15762))
                           (if (seq map__15762) (first map__15762) {}))
                         map__15762)
            query_map map__15762
            offset (get map__15762 :offset 0)
            limit (get map__15762 :limit (long java.lang.Long/MAX_VALUE))
            io_context (get map__15762 :io-context)
            f (fn f ([] (query* query_map)))
            map__15763 (when io_context
                         (io-stats/throw-if-ex!
                           (io-stats/with-io-stats f {:io-context io_context, :api :qseq})))
            map__15763 (if (seq? map__15763)
                         (if (next map__15763)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15763))
                           (if (seq map__15763) (first map__15763) {}))
                         map__15763)
            ret (get map__15763 :ret)
            io_stats (get map__15763 :io-stats)
            ret (if io_context ret (^clojure.lang.IFn f))
            vec__15764 ret
            result (nth vec__15764 (int 0) nil)
            pf (nth vec__15764 (int 1) nil)
            xform (common/result-xform offset limit pf)
            cseq (qs/counted-seq
                   (sequence xform result)
                   (common/result-count offset limit result))]
        (if io_context {:ret cseq, :io-stats io_stats} cseq))))
  (defn cache ([q] (get query-cache q)))
  (defn construct-fn ([qform] (fn fn__15771 ([& args] (q qform args))))))
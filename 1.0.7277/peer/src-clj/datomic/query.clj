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
                   (fn fn__19135
                     ([p1__19132#]
                       (and
                         (= rid (.getV ^datomic.impl.db.IDatum p1__19132#))
                         (= attrid (long (.getA ^datomic.impl.db.IDatum p1__19132#))))))
                   (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid)))
            attr (and attrid (db/attribute db attrid))
            component? (and attr (.-isComponent ^datomic.db.Attribute attr))]
        (when iter
          (if component?
            (emap db (long (.getE (.get ^datomic.iter.Iter iter))))
            (iter/reduce
              (fn fn__19138
                ([p1__19133# p2__19134#]
                  (conj p1__19133# (emap db (long (.getE ^datomic.impl.db.IDatum p2__19134#))))))
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
                     (fn fn__19148
                       ([p1__19145#]
                         (and
                           (= eid (long (.getE ^datomic.impl.db.IDatum p1__19145#)))
                           (= attrid (long (.getA ^datomic.impl.db.IDatum p1__19145#))))))
                     (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid :a attrid)))
              attr (and attrid (db/attribute db attrid))
              vtypeid (and attr (.-vtypeid ^datomic.db.Attribute attr))
              ref? (and vtypeid (= vtypeid 20))
              maybe_bind (fn maybe_bind ([v] (ref-val db ref? v)))]
          (when iter
            (if (and attr (= 36 (.-cardinality ^datomic.db.Attribute attr)))
              (iter/reduce
                (fn fn__19153
                  ([p1__19146# p2__19147#]
                    (conj
                      p1__19146#
                      (^clojure.lang.IFn maybe_bind (.getV ^datomic.impl.db.IDatum p2__19147#)))))
                #{}
                iter)
              (^clojure.lang.IFn maybe_bind (.getV (.get ^datomic.iter.Iter iter)))))))))
  (defn get-lazy-entity
    ([db ent]
      (let [eid (db/resolve-id db ent)
            ret (iter/reduce
                  (fn fn__19162
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
                    (fn fn__19168
                      ([p1__19160#] (= eid (long (.getE ^datomic.impl.db.IDatum p1__19160#)))))
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
              (fn fn__19188
                ([p__19187]
                  (let [vec__19189 p__19187
                        k (nth vec__19189 (int 0) nil)
                        v (nth vec__19189 (int 1) nil)]
                    (contains? edits k))))
              m)
            (remove
              (fn fn__19194
                ([p__19193]
                  (let [vec__19195 p__19193
                        k (nth vec__19195 (int 0) nil)
                        v (nth vec__19195 (int 1) nil)]
                    (nil? v))))
              edits)))))
    (^datomic.Database db [this] ^datomic.Database db)
    (^java.util.Set keySet [this] (into #{} (map (comp str first) (seq this))))
    (^datomic.Entity touch
      [this]
      (do
        (loop [seq_19175 (seq (keys this)) chunk_19176 nil count_19177 0 i_19178 0]
          (if (< i_19178 count_19177)
            (let [a (.nth ^clojure.lang.Indexed chunk_19176 (int i_19178))]
              (let [attrid (db/resolve-id db a)
                    attr (.elementAt ^datomic.db.IDbImpl db attrid)
                    v (.valAt this a)]
                (when (and
                        (.-isComponent ^datomic.db.Attribute attr)
                        (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                  (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                    (loop [seq_19179 (seq v) chunk_19180 nil count_19181 0 i_19182 0]
                      (if (< i_19182 count_19181)
                        (let [v (.nth ^clojure.lang.Indexed chunk_19180 (int i_19182))]
                          (touch v)
                          (recur seq_19179 chunk_19180 count_19181 (inc i_19182)))
                        (let [temp__5457__auto__ (seq seq_19179)]
                          (when temp__5457__auto__
                            (let [seq_19179 temp__5457__auto__]
                              (if (chunked-seq? seq_19179)
                                (let [c__5719__auto__ (chunk-first seq_19179)]
                                  (recur
                                    (chunk-rest seq_19179)
                                    c__5719__auto__
                                    (int (count c__5719__auto__))
                                    (int 0)))
                                (let [v (first seq_19179)]
                                  (touch v)
                                  (recur (next seq_19179) nil 0 0))))))))
                    (touch v))))
              (recur seq_19175 chunk_19176 count_19177 (inc i_19178)))
            (let [temp__5457__auto__ (seq seq_19175)]
              (when temp__5457__auto__
                (let [seq_19175 temp__5457__auto__]
                  (if (chunked-seq? seq_19175)
                    (let [c__5719__auto__ (chunk-first seq_19175)]
                      (recur
                        (chunk-rest seq_19175)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [a (first seq_19175)]
                      (let [attrid (db/resolve-id db a)
                            attr (.elementAt ^datomic.db.IDbImpl db attrid)
                            v (.valAt this a)]
                        (when (and
                                (.-isComponent ^datomic.db.Attribute attr)
                                (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                          (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                            (loop [seq_19183 (seq v) chunk_19184 nil count_19185 0 i_19186 0]
                              (if (< i_19186 count_19185)
                                (let [v (.nth ^clojure.lang.Indexed chunk_19184 (int i_19186))]
                                  (touch v)
                                  (recur seq_19183 chunk_19184 count_19185 (inc i_19186)))
                                (let [temp__5457__auto__ (seq seq_19183)]
                                  (when temp__5457__auto__
                                    (let [seq_19183 temp__5457__auto__]
                                      (if (chunked-seq? seq_19183)
                                        (let [c__5719__auto__ (chunk-first seq_19183)]
                                          (recur
                                            (chunk-rest seq_19183)
                                            c__5719__auto__
                                            (int (count c__5719__auto__))
                                            (int 0)))
                                        (let [v (first seq_19183)]
                                          (touch v)
                                          (recur (next seq_19183) nil 0 0))))))))
                            (touch v))))
                      (recur (next seq_19175) nil 0 0))))))))
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
    fn__19214
    ([m w]
      (print-method
        (merge (.cache ^datomic.query.EMapImpl m) (.-edits ^datomic.query.EntityMap m))
        w)))
  (defonce Immutify {})
  (defprotocol Immutify (immutify [x]))
  (extend nil Immutify {:immutify (fn fn__19232 ([x] x))})
  (extend java.lang.Object Immutify {:immutify (fn fn__19234 ([x] x))})
  (extend
    java.util.Map
    Immutify
    {:immutify
     (fn fn__19236
       ([x]
         (if (map? x)
           x
           (persistent!
             (reduce
               (fn fn__19238
                 ([m p__19237]
                   (let [vec__19239 p__19237
                         k (nth vec__19239 (int 0) nil)
                         v (nth vec__19239 (int 1) nil)]
                     (assoc! m (immutify k) (immutify v)))))
               (transient {})
               x)))))})
  (extend
    java.util.Set
    Immutify
    {:immutify (fn fn__19244 ([x] (if (set? x) x (set (map immutify x)))))})
  (extend
    java.util.List
    Immutify
    {:immutify (fn fn__19246 ([x] (if (sequential? x) x (mapv immutify x))))})
  (defn listq->mapq
    ([lq]
      (reduce
        (fn fn__19249
          ([m p__19248]
            (let [vec__19250 p__19248
                  k (nth vec__19250 (int 0) nil)
                  v (nth vec__19250 (int 1) nil)
                  k (first k)]
              (assoc m k v))))
        {}
        (partition 2 (partition-by #{:find :where :syms :keys :with :timeout :strs :in} lq)))))
  (defn move-sources-to-meta
    ([p__19256]
      (let [map__19257 p__19256
            map__19257 (if (seq? map__19257)
                         (clojure.lang.PersistentHashMap/create (seq map__19257))
                         map__19257)
            q map__19257
            srcs (get map__19257 :in)
            clauses (get map__19257 :where)]
        (if srcs
          (let [sset (set srcs)]
            (assoc
              q
              :where
              (map
                (fn fn__19258
                  ([p1__19255#]
                    (cond
                      (^clojure.lang.IFn sset (first p1__19255#)) (with-meta
                                                                    (next p1__19255#)
                                                                    {:tag (first p1__19255#)})
                      (datalog/source? (first p1__19255#)) (do
                                                             (throw
                                                               (java.lang.IllegalArgumentException.
                                                                 (str
                                                                   "Data source not supplied: "
                                                                   (first p1__19255#))))
                                                             nil)
                      :else (do p1__19255#))))
                clauses)))
          q))))
  (defn flatten-listy
    ([x]
      (let [listy? (fn listy_QMARK_
                     ([p1__19261#]
                       (or (sequential? p1__19261#) (instance? java.util.List p1__19261#))))]
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
      (loop [seq_19267 (seq (:find qmap)) chunk_19268 nil count_19269 0 i_19270 0]
        (if (< i_19270 count_19269)
          (let [f (.nth ^clojure.lang.Indexed chunk_19268 (int i_19270))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :find is not a variable"))))
            (recur seq_19267 chunk_19268 count_19269 (inc i_19270)))
          (let [temp__5457__auto__ (seq seq_19267)]
            (when temp__5457__auto__
              (let [seq_19267 temp__5457__auto__]
                (if (chunked-seq? seq_19267)
                  (let [c__5719__auto__ (chunk-first seq_19267)]
                    (recur
                      (chunk-rest seq_19267)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [f (first seq_19267)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :find is not a variable"))))
                    (recur (next seq_19267) nil 0 0))))))))
      (loop [seq_19271 (seq (:with qmap)) chunk_19272 nil count_19273 0 i_19274 0]
        (if (< i_19274 count_19273)
          (let [f (.nth ^clojure.lang.Indexed chunk_19272 (int i_19274))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :with is not a variable"))))
            (recur seq_19271 chunk_19272 count_19273 (inc i_19274)))
          (let [temp__5457__auto__ (seq seq_19271)]
            (when temp__5457__auto__
              (let [seq_19271 temp__5457__auto__]
                (if (chunked-seq? seq_19271)
                  (let [c__5719__auto__ (chunk-first seq_19271)]
                    (recur
                      (chunk-rest seq_19271)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [f (first seq_19271)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :with is not a variable"))))
                    (recur (next seq_19271) nil 0 0))))))))
      (loop [seq_19275 (seq (:where qmap)) chunk_19276 nil count_19277 0 i_19278 0]
        (if (< i_19278 count_19277)
          (let [w (.nth ^clojure.lang.Indexed chunk_19276 (int i_19278))]
            (when-not (instance? java.util.List w)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " w " in :where is not a list"))))
            (recur seq_19275 chunk_19276 count_19277 (inc i_19278)))
          (let [temp__5457__auto__ (seq seq_19275)]
            (when temp__5457__auto__
              (let [seq_19275 temp__5457__auto__]
                (if (chunked-seq? seq_19275)
                  (let [c__5719__auto__ (chunk-first seq_19275)]
                    (recur
                      (chunk-rest seq_19275)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [w (first seq_19275)]
                    (when-not (instance? java.util.List w)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " w " in :where is not a list"))))
                    (recur (next seq_19275) nil 0 0))))))))
      (loop [seq_19279 (seq (:in qmap)) chunk_19280 nil count_19281 0 i_19282 0]
        (if (< i_19282 count_19281)
          (let [i (.nth ^clojure.lang.Indexed chunk_19280 (int i_19282))]
            (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " i " in :in is not a source"))))
            (recur seq_19279 chunk_19280 count_19281 (inc i_19282)))
          (let [temp__5457__auto__ (seq seq_19279)]
            (when temp__5457__auto__
              (let [seq_19279 temp__5457__auto__]
                (if (chunked-seq? seq_19279)
                  (let [c__5719__auto__ (chunk-first seq_19279)]
                    (recur
                      (chunk-rest seq_19279)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [i (first seq_19279)]
                    (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " i " in :in is not a source"))))
                    (recur (next seq_19279) nil 0 0))))))))
      (let [temp__5457__auto__ (seq (datalog/callees (:construct qmap)))]
        (when temp__5457__auto__
          (let [callees temp__5457__auto__]
            (throw
              (java.lang.IllegalArgumentException.
                (str (first callees) " is not valid in :construct"))))))
      (let [temp__5457__auto__ (seq
                                 (set/difference
                                   (variables-in-clause (:construct qmap))
                                   (variables-in-clause (:find qmap))))]
        (when temp__5457__auto__
          (let [unfound temp__5457__auto__]
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
      (let [temp__5455__auto__ (:in qmap)]
        (if temp__5455__auto__
          (let [ins temp__5455__auto__
                binding? (fn binding_QMARK_
                           ([p1__19299#]
                             (or
                               (datalog/variable? p1__19299#)
                               (instance? java.util.List p1__19299#))))]
            (reduce
              (fn fn__19307
                ([m p__19306]
                  (let [vec__19308 p__19306
                        i (nth vec__19308 (int 0) nil)
                        x (nth vec__19308 (int 1) nil)]
                    (if (^clojure.lang.IFn binding? x)
                      (let [gs (symbol (str prefix (inc i))) bind_type (datalog/binding-type x)]
                        (cond->
                          (update-in
                            (update-in m [:in] conj gs)
                            [:where]
                            (fn fn__19312 ([p1__19301# p2__19300#] (cons p2__19300# p1__19301#)))
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
                                (fn fn__19314 ([p1__19302#] (vector gs p1__19302#)))
                                (range))))))
                      (update-in m [:in] conj x)))))
              (assoc qmap :in [])
              (map-indexed vector ins)))
          qmap))))
  (defn process-ranges
    ([qmap]
      (let [consts (:in-consts qmap)
            const? (fn const_QMARK_
                     ([p1__19319#]
                       (or (get consts p1__19319#) (not (datalog/variable? p1__19319#)))))
            swap {'< '>, '<= '>=, '> '<, '>= '<=, '= '=}
            norm (fn norm
                   ([p__19324]
                     (let [vec__19326 p__19324
                           vec__19329 (nth vec__19326 (int 0) nil)
                           cmp (nth vec__19329 (int 0) nil)
                           x (nth vec__19329 (int 1) nil)
                           y (nth vec__19329 (int 2) nil)
                           c vec__19329]
                       (cond
                         (and (not (^clojure.lang.IFn const? x)) (^clojure.lang.IFn const? y)) c
                         (and (^clojure.lang.IFn const? x) (not (^clojure.lang.IFn const? y))) (do
                                                                                                 (clojure.core/list
                                                                                                   (^clojure.lang.IFn swap
                                                                                                     cmp)
                                                                                                   y
                                                                                                   x))))))
            cexprs (keep
                     (fn fn__19335
                       ([p1__19320#]
                         (let [or__5238__auto__ (and
                                                  (instance? java.util.List p1__19320#)
                                                  (instance? java.util.List (first p1__19320#))
                                                  (= (long (count p1__19320#)) 1)
                                                  (#{'>= '> '<= '= '<} (ffirst p1__19320#))
                                                  (^clojure.lang.IFn norm p1__19320#))]
                           (when or__5238__auto__ or__5238__auto__))))
                     (:where qmap))
            assoc_stronger (fn assoc_stronger
                             ([m k v cmp]
                               (let [ev (get m k)]
                                 (if (or (nil? ev) (= '= cmp)) (assoc m k v) m))))]
        (reduce
          (fn fn__19346
            ([m p__19345]
              (let [vec__19347 p__19345
                    cmp (nth vec__19347 (int 0) nil)
                    var (nth vec__19347 (int 1) nil)
                    const (nth vec__19347 (int 2) nil)]
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
            list? (fn list_QMARK_ ([p1__19353#] (instance? java.util.List p1__19353#)))
            qmap (if (or (:with qmap) (some list? finds))
                   (assoc
                     qmap
                     :find
                     (vec
                       (distinct
                         (map
                           (fn fn__19357
                             ([p1__19354#]
                               (if (^clojure.lang.IFn list? p1__19354#)
                                 (last p1__19354#)
                                 p1__19354#)))
                           finds)))
                     :group
                     (vec finds))
                   qmap)
            qmap (let [temp__5455__auto__ (:with qmap)]
                   (if temp__5455__auto__
                     (let [w temp__5455__auto__] (assoc qmap :find (into (vec (:find qmap)) w)))
                     qmap))]
        qmap)))
  (defn process-find-bindings
    ([p__19363]
      (let [map__19364 p__19363
            map__19364 (if (seq? map__19364)
                         (clojure.lang.PersistentHashMap/create (seq map__19364))
                         map__19364)
            qmap map__19364
            find (get map__19364 :find)]
        (cond
          (and
            (= 1 (long (count find)))
            (instance? java.util.List (first find))
            (every?
              (fn fn__19365
                ([p1__19362#]
                  (or (datalog/variable? p1__19362#) (instance? java.util.List p1__19362#))))
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
    ([p__19382]
      (let [map__19383 p__19382
            map__19383 (if (seq? map__19383)
                         (clojure.lang.PersistentHashMap/create (seq map__19383))
                         map__19383)
            qmap map__19383
            find (get map__19383 :find)
            in (get map__19383 :in)
            pull? (fn pull_QMARK_
                    ([p1__19379#]
                      (and (instance? java.util.List p1__19379#) (= 'pull (first p1__19379#)))))]
        (if (some pull? find)
          (assoc
            qmap
            :pull
            (mapv
              (fn fn__19387
                ([p1__19380#]
                  (if (^clojure.lang.IFn pull? p1__19380#)
                    (let [vec__19388 (normalize-pull p1__19380#)
                          _ (nth vec__19388 (int 0) nil)
                          db (nth vec__19388 (int 1) nil)
                          var (nth vec__19388 (int 2) nil)
                          pattern (nth vec__19388 (int 3) nil)]
                      (when (and (symbol? pattern) (not (some #{pattern} in)))
                        (error/arg
                          :db.error/pattern-not-bound
                          (str "Pull pattern not found in inputs: " pattern)))
                      {:db db, :var var, :pattern pattern})
                    p1__19380#)))
              find)
            :find
            (mapv
              (fn fn__19393
                ([p1__19381#]
                  (if (^clojure.lang.IFn pull? p1__19381#)
                    (first (filter datalog/variable? p1__19381#))
                    p1__19381#)))
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
        (fn fn__19400
          ([p1__19399#]
            (reduce
              (fn fn__19401
                ([result clause]
                  (let [result result
                        syms (java.util.HashSet.)
                        rclause []
                        G__19405 clause
                        vec__19406 G__19405
                        seq__19407 (seq vec__19406)
                        first__19408 (first seq__19407)
                        seq__19407 (next seq__19407)
                        item first__19408
                        more seq__19407]
                    (loop [result result syms syms rclause rclause G__19405 G__19405]
                      (let [result result
                            syms syms
                            rclause rclause
                            vec__19409 G__19405
                            seq__19410 (seq vec__19409)
                            first__19411 (first seq__19410)
                            seq__19410 (next seq__19410)
                            item first__19411
                            more seq__19410]
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
              p1__19399#))))))
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
            (fn fn__19416
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
          (fn fn__19420
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
      (let [temp__5455__auto__ (:construct query)]
        (if temp__5455__auto__
          (let [construct temp__5455__auto__ src (compile-construct-n (:find query) construct)]
            (assoc query :construct-fn-src src :construct-fn (eval src)))
          query))))
  (defn resolve-qualified-fn
    ([x]
      (let [temp__5457__auto__ (and (sequential? x) (first x))]
        (when temp__5457__auto__
          (let [f temp__5457__auto__]
            (when (common/qualified-symbol? f) (common/maybe-require 'datomic.extensions f)))))))
  (defn resolve-qualified-fns
    ([p__19429]
      (let [map__19430 p__19429
            map__19430 (if (seq? map__19430)
                         (clojure.lang.PersistentHashMap/create (seq map__19430))
                         map__19430)
            where (get map__19430 :where)
            group (get map__19430 :group)]
        (loop [seq_19431 (seq (map first where)) chunk_19432 nil count_19433 0 i_19434 0]
          (if (< i_19434 count_19433)
            (let [clause (.nth ^clojure.lang.Indexed chunk_19432 (int i_19434))]
              (resolve-qualified-fn clause)
              (recur seq_19431 chunk_19432 count_19433 (inc i_19434)))
            (let [temp__5457__auto__ (seq seq_19431)]
              (when temp__5457__auto__
                (let [seq_19431 temp__5457__auto__]
                  (if (chunked-seq? seq_19431)
                    (let [c__5719__auto__ (chunk-first seq_19431)]
                      (recur
                        (chunk-rest seq_19431)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [clause (first seq_19431)]
                      (resolve-qualified-fn clause)
                      (recur (next seq_19431) nil 0 0))))))))
        (loop [seq_19435 (seq group) chunk_19436 nil count_19437 0 i_19438 0]
          (if (< i_19438 count_19437)
            (let [clause (.nth ^clojure.lang.Indexed chunk_19436 (int i_19438))]
              (resolve-qualified-fn clause)
              (recur seq_19435 chunk_19436 count_19437 (inc i_19438)))
            (let [temp__5457__auto__ (seq seq_19435)]
              (when temp__5457__auto__
                (let [seq_19435 temp__5457__auto__]
                  (if (chunked-seq? seq_19435)
                    (let [c__5719__auto__ (chunk-first seq_19435)]
                      (recur
                        (chunk-rest seq_19435)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [clause (first seq_19435)]
                      (resolve-qualified-fn clause)
                      (recur (next seq_19435) nil 0 0)))))))))))
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
            vec__19445 (datalog/prep-clauses nil (:where query))
            rm (nth vec__19445 (int 0) nil)
            cs (nth vec__19445 (int 1) nil)
            query (assoc query :where cs :arules rm)
            query (compile-construct query)]
        query)))
  (defn group-rel
    ([fv rel]
      (let [grp_idxs (filterv integer? fv)]
        (if (= (count fv) (count grp_idxs))
          (mapv
            (fn fn__19450
              ([p1__19449#] (subvec p1__19449# 0 (java.lang.Integer/valueOf (int (count fv))))))
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
                            (let [vec__19463 fve
                                  idx (nth vec__19463 (int 0) nil)
                                  f (nth vec__19463 (int 1) nil)]
                              (^clojure.lang.IFn f
                                (^clojure.lang.IFn agg idx (long r) (long cnt))))))))
                    (recur (conj ret (vec nextrow)) nextr))))))))))
  (def query-cache (cache/create-computing load-query 1000))
  (defn group-fv
    ([find_clause group_clause]
      (let [smap (zipmap find_clause (range))
            gns (find-ns 'datomic.aggregation)
            list? (fn list_QMARK_ ([p1__19468#] (instance? java.util.List p1__19468#)))]
        (mapv
          (fn fn__19472
            ([p1__19469#]
              (if (^clojure.lang.IFn list? p1__19469#)
                (let [temp__5455__auto__ (ns-resolve gns (first p1__19469#))]
                  (if temp__5455__auto__
                    (let [agg_fn temp__5455__auto__]
                      [(^clojure.lang.IFn smap (last p1__19469#))
                       (apply partial agg_fn (butlast (rest p1__19469#)))])
                    (error/arg
                      :db.error/invalid-aggregate
                      (str
                        "Argument "
                        (first p1__19469#)
                        " in :find is not an aggregate function"))))
                (^clojure.lang.IFn smap p1__19469#))))
          group_clause))))
  (defn xf-tuple
    ([fv tuple]
      (let [result (transient [])
            G__19482 tuple
            vec__19484 G__19482
            seq__19485 (seq vec__19484)
            first__19486 (first seq__19485)
            seq__19485 (next seq__19485)
            col first__19486
            tuple seq__19485
            G__19483 fv
            vec__19487 G__19483
            seq__19488 (seq vec__19487)
            first__19489 (first seq__19488)
            seq__19488 (next seq__19488)
            f first__19489
            fv seq__19488]
        (loop [result result G__19482 G__19482 G__19483 G__19483]
          (let [result result
                vec__19490 G__19482
                seq__19491 (seq vec__19490)
                first__19492 (first seq__19491)
                seq__19491 (next seq__19491)
                col first__19492
                tuple seq__19491
                vec__19493 G__19483
                seq__19494 (seq vec__19493)
                first__19495 (first seq__19494)
                seq__19494 (next seq__19494)
                f first__19495
                fv seq__19494]
            (if f
              (recur (conj! result (^clojure.lang.IFn f col)) tuple fv)
              (persistent! result)))))))
  (defn pull-fv
    ([p__19498 srcs]
      (let [map__19499 p__19498
            map__19499 (if (seq? map__19499)
                         (clojure.lang.PersistentHashMap/create (seq map__19499))
                         map__19499)
            qmap map__19499
            find (get map__19499 :find)
            pull (get map__19499 :pull)
            with (get map__19499 :with)
            in (get map__19499 :in)
            smap (zipmap find (range))
            srcmap (zipmap in srcs)
            colct (- (count find) (count with))]
        (reduce
          (fn fn__19501
            ([m p__19500]
              (let [map__19502 p__19500
                    map__19502 (if (seq? map__19502)
                                 (clojure.lang.PersistentHashMap/create (seq map__19502))
                                 map__19502)
                    db (get map__19502 :db)
                    var (get map__19502 :var)
                    pattern (get map__19502 :pattern)]
                (assoc
                  m
                  (common/getx smap var)
                  (fn fn__19503
                    ([p1__19497#]
                      (first
                        (pull/pull
                          (get srcmap db (first srcs))
                          (get srcmap pattern pattern)
                          [p1__19497#]))))))))
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
            G__19510 (java.util.ArrayList. ^java.util.Collection coll)]
        (.sort ^java.util.ArrayList G__19510 ^java.util.Comparator cmp)
        G__19510)))
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
      (let [temp__5455__auto__ (first
                                 (keep-indexed
                                   (fn fn__19512 ([idx item] (when (map? item) idx)))
                                   (:pull query)))]
        (if temp__5455__auto__
          (let [idx temp__5455__auto__]
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
      (let [vec__19518 (qs/parse-as query)
            query (nth vec__19518 (int 0) nil)
            as (nth vec__19518 (int 1) nil)
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
              ret (if (let [and__5236__auto__ (count ret)]
                        (if (java.lang.Integer/valueOf (int and__5236__auto__))
                          (or (:group qmap) (:with qmap))
                          (java.lang.Integer/valueOf (int and__5236__auto__))))
                    (group-rel group_clause ret)
                    ret)
              ret (sort-for-pull qmap ret)
              pullfn (when (:pull qmap)
                       (let [fv (pull-fv qmap srcs)]
                         (fn fn__19521 ([p1__19516#] (xf-tuple fv p1__19516#)))))
              asfn (when as
                     (fn fn__19523
                       ([p1__19517#]
                         (datomic.query.support.MapOnIndexed.
                           ^clojure.lang.IPersistentVector as
                           ^clojure.lang.Indexed p1__19517#))))
              pfn (when (or pullfn asfn) (comp (or asfn identity) (or pullfn identity)))
              temp__5455__auto__ (:find-bindings qmap)]
          (if temp__5455__auto__
            (let [fb temp__5455__auto__ ret (if pfn (map pfn ret) ret)]
              [(let [G__19525 fb]
                 (case
                   G__19525
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
    ([p__19535]
      (let [vec__19536 p__19535
            result (nth vec__19536 (int 0) nil)
            pf (nth vec__19536 (int 1) nil)]
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
    ([p__19541]
      (let [map__19542 p__19541
            map__19542 (if (seq? map__19542)
                         (clojure.lang.PersistentHashMap/create (seq map__19542))
                         map__19542)
            query_map map__19542
            io_context (get map__19542 :io-context)
            query_stats (get map__19542 :query-stats)
            f (fn f ([] (apply-pf (query* query_map))))
            f (if io_context
                (fn fn__19545
                  ([]
                    (io-stats/throw-if-ex!
                      (io-stats/with-io-stats f {:io-context io_context, :api :query}))))
                f)
            f (if query_stats
                (fn fn__19547 ([] (query-stats/with-query-stats f {:query (:query query_map)})))
                f)]
        (^clojure.lang.IFn f))))
  (defn qseq
    ([query_map args] (qseq {:query query_map, :args args}))
    ([p__19550]
      (let [map__19551 p__19550
            map__19551 (if (seq? map__19551)
                         (clojure.lang.PersistentHashMap/create (seq map__19551))
                         map__19551)
            query_map map__19551
            offset (get map__19551 :offset 0)
            limit (get map__19551 :limit (long java.lang.Long/MAX_VALUE))
            io_context (get map__19551 :io-context)
            f (fn f ([] (query* query_map)))
            map__19552 (when io_context
                         (io-stats/throw-if-ex!
                           (io-stats/with-io-stats f {:io-context io_context, :api :qseq})))
            map__19552 (if (seq? map__19552)
                         (clojure.lang.PersistentHashMap/create (seq map__19552))
                         map__19552)
            ret (get map__19552 :ret)
            io_stats (get map__19552 :io-stats)
            ret (if io_context ret (^clojure.lang.IFn f))
            vec__19553 ret
            result (nth vec__19553 (int 0) nil)
            pf (nth vec__19553 (int 1) nil)
            xform (common/result-xform offset limit pf)
            cseq (qs/counted-seq
                   (sequence xform result)
                   (common/result-count offset limit result))]
        (if io_context {:ret cseq, :io-stats io_stats} cseq))))
  (defn cache ([q] (get query-cache q)))
  (defn construct-fn ([qform] (fn fn__19560 ([& args] (q qform args))))))
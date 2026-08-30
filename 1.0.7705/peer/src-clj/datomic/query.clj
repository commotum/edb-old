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
  (.setMeta (clojure.lang.RT/var "datomic.query" "emap") {:declared true, :column (int 1)})
  (defn rae
    ([db r a]
      (let [attrid (db/resolve-id db a)
            rid (db/resolve-id db r)
            iter (db/windowed
                   db
                   (fn fn__18947
                     ([p1__18944#]
                       (and
                         (= rid (.getV ^datomic.impl.db.IDatum p1__18944#))
                         (= attrid (long (.getA ^datomic.impl.db.IDatum p1__18944#))))))
                   (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid)))
            attr (and attrid (db/attribute db attrid))
            component? (and attr (.-isComponent ^datomic.db.Attribute attr))]
        (when iter
          (if component?
            (emap db (long (.getE (.get ^datomic.iter.Iter iter))))
            (iter/reduce
              (fn fn__18950
                ([p1__18945# p2__18946#]
                  (conj p1__18945# (emap db (long (.getE ^datomic.impl.db.IDatum p2__18946#))))))
              #{}
              iter))))))
  (reset-meta!
    #'rae
    (assoc {:arglists (clojure.core/list ['db 'r 'a]), :column (int 1)} :name 'rae :ns *ns*))
  (defn ref-val ([db ref? v] (if ref? (or (db/resolve-kw db v) (emap db v)) v)))
  (reset-meta!
    #'ref-val
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'ref? 'v]), :column (int 1)}
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
                     (fn fn__18960
                       ([p1__18957#]
                         (and
                           (= eid (long (.getE ^datomic.impl.db.IDatum p1__18957#)))
                           (= attrid (long (.getA ^datomic.impl.db.IDatum p1__18957#))))))
                     (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid :a attrid)))
              attr (and attrid (db/attribute db attrid))
              vtypeid (and attr (.-vtypeid ^datomic.db.Attribute attr))
              ref? (and vtypeid (= vtypeid 20))
              maybe_bind (fn maybe_bind ([v] (ref-val db ref? v)))]
          (when iter
            (if (and attr (= 36 (.-cardinality ^datomic.db.Attribute attr)))
              (iter/reduce
                (fn fn__18965
                  ([p1__18958# p2__18959#]
                    (conj
                      p1__18958#
                      (^clojure.lang.IFn maybe_bind (.getV ^datomic.impl.db.IDatum p2__18959#)))))
                #{}
                iter)
              (^clojure.lang.IFn maybe_bind (.getV (.get ^datomic.iter.Iter iter)))))))))
  (reset-meta!
    #'eav
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Db}) 'e 'a]), :column (int 1)}
      :name
      'eav
      :ns
      *ns*))
  (defn get-lazy-entity
    ([db ent]
      (let [eid (db/resolve-id db ent)
            ret (iter/reduce
                  (fn fn__18974
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
                    (fn fn__18980
                      ([p1__18972#] (= eid (long (.getE ^datomic.impl.db.IDatum p1__18972#)))))
                    (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid))))]
        ret)))
  (reset-meta!
    #'get-lazy-entity
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'IDb}) 'ent]), :column (int 1)}
      :name
      'get-lazy-entity
      :ns
      *ns*))
  (definterface EMapImpl (^java.lang.Object cache []))
  (clojure.core/import 'datomic.query.EMapImpl)
  (defn touch ([e] (if (keyword? e) e (.touch ^datomic.Entity e))))
  (reset-meta!
    #'touch
    (assoc {:arglists (clojure.core/list ['e]), :column (int 1)} :name 'touch :ns *ns*))
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
              (fn fn__19000
                ([p__18999]
                  (let [vec__19001 p__18999
                        k (nth vec__19001 (int 0) nil)
                        v (nth vec__19001 (int 1) nil)]
                    (contains? edits k))))
              m)
            (remove
              (fn fn__19006
                ([p__19005]
                  (let [vec__19007 p__19005
                        k (nth vec__19007 (int 0) nil)
                        v (nth vec__19007 (int 1) nil)]
                    (nil? v))))
              edits)))))
    (^datomic.Database db [this] ^datomic.Database db)
    (^java.util.Set keySet [this] (into #{} (map (comp str first) (seq this))))
    (^datomic.Entity touch
      [this]
      (do
        (loop [seq_18987 (seq (keys this)) chunk_18988 nil count_18989 0 i_18990 0]
          (if (< i_18990 count_18989)
            (let [a (.nth ^clojure.lang.Indexed chunk_18988 (int i_18990))]
              (let [attrid (db/resolve-id db a)
                    attr (.elementAt ^datomic.db.IDbImpl db attrid)
                    v (.valAt this a)]
                (when (and
                        (.-isComponent ^datomic.db.Attribute attr)
                        (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                  (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                    (loop [seq_18991 (seq v) chunk_18992 nil count_18993 0 i_18994 0]
                      (if (< i_18994 count_18993)
                        (let [v (.nth ^clojure.lang.Indexed chunk_18992 (int i_18994))]
                          (touch v)
                          (recur seq_18991 chunk_18992 count_18993 (inc i_18994)))
                        (let [temp__5804__auto__ (seq seq_18991)]
                          (when temp__5804__auto__
                            (let [seq_18991 temp__5804__auto__]
                              (if (chunked-seq? seq_18991)
                                (let [c__6065__auto__ (chunk-first seq_18991)]
                                  (recur
                                    (chunk-rest seq_18991)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [v (first seq_18991)]
                                  (touch v)
                                  (recur (next seq_18991) nil 0 0))))))))
                    (touch v))))
              (recur seq_18987 chunk_18988 count_18989 (inc i_18990)))
            (let [temp__5804__auto__ (seq seq_18987)]
              (when temp__5804__auto__
                (let [seq_18987 temp__5804__auto__]
                  (if (chunked-seq? seq_18987)
                    (let [c__6065__auto__ (chunk-first seq_18987)]
                      (recur
                        (chunk-rest seq_18987)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [a (first seq_18987)]
                      (let [attrid (db/resolve-id db a)
                            attr (.elementAt ^datomic.db.IDbImpl db attrid)
                            v (.valAt this a)]
                        (when (and
                                (.-isComponent ^datomic.db.Attribute attr)
                                (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                          (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                            (loop [seq_18995 (seq v) chunk_18996 nil count_18997 0 i_18998 0]
                              (if (< i_18998 count_18997)
                                (let [v (.nth ^clojure.lang.Indexed chunk_18996 (int i_18998))]
                                  (touch v)
                                  (recur seq_18995 chunk_18996 count_18997 (inc i_18998)))
                                (let [temp__5804__auto__ (seq seq_18995)]
                                  (when temp__5804__auto__
                                    (let [seq_18995 temp__5804__auto__]
                                      (if (chunked-seq? seq_18995)
                                        (let [c__6065__auto__ (chunk-first seq_18995)]
                                          (recur
                                            (chunk-rest seq_18995)
                                            c__6065__auto__
                                            (int (count c__6065__auto__))
                                            (int 0)))
                                        (let [v (first seq_18995)]
                                          (touch v)
                                          (recur (next seq_18995) nil 0 0))))))))
                            (touch v))))
                      (recur (next seq_18987) nil 0 0))))))))
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
  (reset-meta!
    #'->EntityMap
    (assoc
      {:arglists (clojure.core/list ['db 'eid 'cache 'edits]), :column (int 1)}
      :name
      '->EntityMap
      :ns
      *ns*))
  (defn emap ([db eid] (datomic.query.EntityMap. db eid #:db{:id eid} nil)))
  (reset-meta!
    #'emap
    (assoc {:arglists (clojure.core/list ['db 'eid]), :column (int 1)} :name 'emap :ns *ns*))
  (defmethod
    print-method
    datomic.query.EntityMap
    fn__19026
    ([m w]
      (print-method
        (merge (.cache ^datomic.query.EMapImpl m) (.-edits ^datomic.query.EntityMap m))
        w)))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      Immutify
      (immutify
        [x]
        "Return immutable form of x. Presumes that if top of a data structure\nis immutable, rest is too."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.query" "Immutify")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Immutify :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'immutify {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc
                                      "Return immutable form of x. Presumes that if top of a data structure\nis immutable, rest is too."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.query" "Immutify"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.query" "immutify")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend nil Immutify {:immutify (fn fn__19044 ([x] x))})
  (extend java.lang.Object Immutify {:immutify (fn fn__19046 ([x] x))})
  (extend
    java.util.Map
    Immutify
    {:immutify
     (fn fn__19048
       ([x]
         (if (map? x)
           x
           (persistent!
             (reduce
               (fn fn__19050
                 ([m p__19049]
                   (let [vec__19051 p__19049
                         k (nth vec__19051 (int 0) nil)
                         v (nth vec__19051 (int 1) nil)]
                     (assoc! m (immutify k) (immutify v)))))
               (transient {})
               x)))))})
  (extend
    java.util.Set
    Immutify
    {:immutify (fn fn__19056 ([x] (if (set? x) x (set (map immutify x)))))})
  (extend
    java.util.List
    Immutify
    {:immutify (fn fn__19058 ([x] (if (sequential? x) x (mapv immutify x))))})
  (defn listq->mapq
    ([lq]
      (reduce
        (fn fn__19061
          ([m p__19060]
            (let [vec__19062 p__19060
                  k (nth vec__19062 (int 0) nil)
                  v (nth vec__19062 (int 1) nil)
                  k (first k)]
              (assoc m k v))))
        {}
        (partition 2 (partition-by #{:find :where :syms :keys :with :timeout :strs :in} lq)))))
  (reset-meta!
    #'listq->mapq
    (assoc {:arglists (clojure.core/list ['lq]), :column (int 1)} :name 'listq->mapq :ns *ns*))
  (defn move-sources-to-meta
    ([p__19068]
      (let [map__19069 p__19068
            map__19069 (if (seq? map__19069)
                         (if (next map__19069)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19069))
                           (if (seq map__19069) (first map__19069) {}))
                         map__19069)
            q map__19069
            srcs (get map__19069 :in)
            clauses (get map__19069 :where)]
        (if srcs
          (let [sset (set srcs)]
            (assoc
              q
              :where
              (map
                (fn fn__19070
                  ([p1__19067#]
                    (if (^clojure.lang.IFn sset (first p1__19067#))
                      (with-meta (next p1__19067#) {:tag (first p1__19067#)})
                      (do
                        (when (datalog/source? (first p1__19067#))
                          (throw
                            (java.lang.IllegalArgumentException.
                              (str "Data source not supplied: " (first p1__19067#)))))
                        (when :else p1__19067#)))))
                clauses)))
          q))))
  (reset-meta!
    #'move-sources-to-meta
    (assoc
      {:arglists (clojure.core/list [{'srcs :in, 'clauses :where, :as 'q}]), :column (int 1)}
      :name
      'move-sources-to-meta
      :ns
      *ns*))
  (defn flatten-listy
    ([x]
      (let [listy? (fn listy_QMARK_
                     ([p1__19073#]
                       (or (sequential? p1__19073#) (instance? java.util.List p1__19073#))))]
        (filter (complement listy?) (rest (tree-seq listy? seq x))))))
  (reset-meta!
    #'flatten-listy
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'flatten-listy
      :ns
      *ns*))
  (defn variables-in-clause
    ([clause] (into #{} (filter datalog/variable? (flatten-listy clause)))))
  (reset-meta!
    #'variables-in-clause
    (assoc
      {:private true, :arglists (clojure.core/list ['clause]), :column (int 1)}
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
      (loop [seq_19079 (seq (:find qmap)) chunk_19080 nil count_19081 0 i_19082 0]
        (if (< i_19082 count_19081)
          (let [f (.nth ^clojure.lang.Indexed chunk_19080 (int i_19082))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :find is not a variable"))))
            (recur seq_19079 chunk_19080 count_19081 (inc i_19082)))
          (let [temp__5804__auto__ (seq seq_19079)]
            (when temp__5804__auto__
              (let [seq_19079 temp__5804__auto__]
                (if (chunked-seq? seq_19079)
                  (let [c__6065__auto__ (chunk-first seq_19079)]
                    (recur
                      (chunk-rest seq_19079)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [f (first seq_19079)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :find is not a variable"))))
                    (recur (next seq_19079) nil 0 0))))))))
      (loop [seq_19083 (seq (:with qmap)) chunk_19084 nil count_19085 0 i_19086 0]
        (if (< i_19086 count_19085)
          (let [f (.nth ^clojure.lang.Indexed chunk_19084 (int i_19086))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :with is not a variable"))))
            (recur seq_19083 chunk_19084 count_19085 (inc i_19086)))
          (let [temp__5804__auto__ (seq seq_19083)]
            (when temp__5804__auto__
              (let [seq_19083 temp__5804__auto__]
                (if (chunked-seq? seq_19083)
                  (let [c__6065__auto__ (chunk-first seq_19083)]
                    (recur
                      (chunk-rest seq_19083)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [f (first seq_19083)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :with is not a variable"))))
                    (recur (next seq_19083) nil 0 0))))))))
      (loop [seq_19087 (seq (:where qmap)) chunk_19088 nil count_19089 0 i_19090 0]
        (if (< i_19090 count_19089)
          (let [w (.nth ^clojure.lang.Indexed chunk_19088 (int i_19090))]
            (when-not (instance? java.util.List w)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " w " in :where is not a list"))))
            (recur seq_19087 chunk_19088 count_19089 (inc i_19090)))
          (let [temp__5804__auto__ (seq seq_19087)]
            (when temp__5804__auto__
              (let [seq_19087 temp__5804__auto__]
                (if (chunked-seq? seq_19087)
                  (let [c__6065__auto__ (chunk-first seq_19087)]
                    (recur
                      (chunk-rest seq_19087)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [w (first seq_19087)]
                    (when-not (instance? java.util.List w)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " w " in :where is not a list"))))
                    (recur (next seq_19087) nil 0 0))))))))
      (loop [seq_19091 (seq (:in qmap)) chunk_19092 nil count_19093 0 i_19094 0]
        (if (< i_19094 count_19093)
          (let [i (.nth ^clojure.lang.Indexed chunk_19092 (int i_19094))]
            (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " i " in :in is not a source"))))
            (recur seq_19091 chunk_19092 count_19093 (inc i_19094)))
          (let [temp__5804__auto__ (seq seq_19091)]
            (when temp__5804__auto__
              (let [seq_19091 temp__5804__auto__]
                (if (chunked-seq? seq_19091)
                  (let [c__6065__auto__ (chunk-first seq_19091)]
                    (recur
                      (chunk-rest seq_19091)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [i (first seq_19091)]
                    (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " i " in :in is not a source"))))
                    (recur (next seq_19091) nil 0 0))))))))
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
  (reset-meta!
    #'validate-query
    (assoc
      {:arglists (clojure.core/list ['qmap]), :column (int 1)}
      :name
      'validate-query
      :ns
      *ns*))
  (defn process-in-bindings
    ([qmap prefix]
      (let [temp__5802__auto__ (:in qmap)]
        (if temp__5802__auto__
          (let [ins temp__5802__auto__
                binding? (fn binding_QMARK_
                           ([p1__19111#]
                             (or
                               (datalog/variable? p1__19111#)
                               (instance? java.util.List p1__19111#))))]
            (reduce
              (fn fn__19119
                ([m p__19118]
                  (let [vec__19120 p__19118
                        i (nth vec__19120 (int 0) nil)
                        x (nth vec__19120 (int 1) nil)]
                    (if (^clojure.lang.IFn binding? x)
                      (let [gs (symbol (str prefix (inc i))) bind_type (datalog/binding-type x)]
                        (cond->
                          (update-in
                            (update-in m [:in] conj gs)
                            [:where]
                            (fn fn__19124 ([p1__19113# p2__19112#] (cons p2__19112# p1__19113#)))
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
                                (fn fn__19126 ([p1__19114#] (vector gs p1__19114#)))
                                (range))))))
                      (update-in m [:in] conj x)))))
              (assoc qmap :in [])
              (map-indexed vector ins)))
          qmap))))
  (reset-meta!
    #'process-in-bindings
    (assoc
      {:arglists (clojure.core/list ['qmap 'prefix]), :column (int 1)}
      :name
      'process-in-bindings
      :ns
      *ns*))
  (defn process-ranges
    ([qmap]
      (let [consts (:in-consts qmap)
            const? (fn const_QMARK_
                     ([p1__19131#]
                       (or (get consts p1__19131#) (not (datalog/variable? p1__19131#)))))
            swap {'< '>, '<= '>=, '> '<, '>= '<=, '= '=}
            norm (fn norm
                   ([p__19136]
                     (let [vec__19138 p__19136
                           vec__19141 (nth vec__19138 (int 0) nil)
                           cmp (nth vec__19141 (int 0) nil)
                           x (nth vec__19141 (int 1) nil)
                           y (nth vec__19141 (int 2) nil)
                           c vec__19141]
                       (cond
                         (and (not (^clojure.lang.IFn const? x)) (^clojure.lang.IFn const? y)) c
                         (and (^clojure.lang.IFn const? x) (not (^clojure.lang.IFn const? y))) (do
                                                                                                 (clojure.core/list
                                                                                                   (^clojure.lang.IFn swap
                                                                                                     cmp)
                                                                                                   y
                                                                                                   x))))))
            cexprs (keep
                     (fn fn__19147
                       ([p1__19132#]
                         (let [or__5581__auto__ (and
                                                  (instance? java.util.List p1__19132#)
                                                  (instance? java.util.List (first p1__19132#))
                                                  (= (long (count p1__19132#)) 1)
                                                  (#{'>= '> '<= '= '<} (ffirst p1__19132#))
                                                  (^clojure.lang.IFn norm p1__19132#))]
                           (when or__5581__auto__ or__5581__auto__))))
                     (:where qmap))
            assoc_stronger (fn assoc_stronger
                             ([m k v cmp]
                               (let [ev (get m k)]
                                 (if (or (nil? ev) (= '= cmp)) (assoc m k v) m))))]
        (reduce
          (fn fn__19158
            ([m p__19157]
              (let [vec__19159 p__19157
                    cmp (nth vec__19159 (int 0) nil)
                    var (nth vec__19159 (int 1) nil)
                    const (nth vec__19159 (int 2) nil)]
                (cond->
                  m
                  (#{'>= '> '=} cmp)
                  (update-in [:range-starts] assoc_stronger var const cmp)
                  (#{'<= '= '<} cmp)
                  (update-in [:range-whiles] assoc_stronger var [cmp const] cmp)))))
          qmap
          cexprs))))
  (reset-meta!
    #'process-ranges
    (assoc
      {:arglists (clojure.core/list ['qmap]), :column (int 1)}
      :name
      'process-ranges
      :ns
      *ns*))
  (defn process-aggregates
    ([qmap]
      (let [finds (:find qmap)
            list? (fn list_QMARK_ ([p1__19165#] (instance? java.util.List p1__19165#)))
            qmap (if (or (:with qmap) (some list? finds))
                   (assoc
                     qmap
                     :find
                     (vec
                       (distinct
                         (map
                           (fn fn__19169
                             ([p1__19166#]
                               (if (^clojure.lang.IFn list? p1__19166#)
                                 (last p1__19166#)
                                 p1__19166#)))
                           finds)))
                     :group
                     (vec finds))
                   qmap)
            qmap (let [temp__5802__auto__ (:with qmap)]
                   (if temp__5802__auto__
                     (let [w temp__5802__auto__] (assoc qmap :find (into (vec (:find qmap)) w)))
                     qmap))]
        qmap)))
  (reset-meta!
    #'process-aggregates
    (assoc
      {:arglists (clojure.core/list ['qmap]), :column (int 1)}
      :name
      'process-aggregates
      :ns
      *ns*))
  (defn process-find-bindings
    ([p__19175]
      (let [map__19176 p__19175
            map__19176 (if (seq? map__19176)
                         (if (next map__19176)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19176))
                           (if (seq map__19176) (first map__19176) {}))
                         map__19176)
            qmap map__19176
            find (get map__19176 :find)]
        (cond
          (and
            (= 1 (long (count find)))
            (instance? java.util.List (first find))
            (every?
              (fn fn__19177
                ([p1__19174#]
                  (or (datalog/variable? p1__19174#) (instance? java.util.List p1__19174#))))
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
  (reset-meta!
    #'process-find-bindings
    (assoc
      {:arglists (clojure.core/list [{:keys ['find], :as 'qmap}]), :column (int 1)}
      :name
      'process-find-bindings
      :ns
      *ns*))
  (defn pattern? ([s] (and (not (datalog/variable? s)) (not (datalog/source? s)))))
  (reset-meta!
    #'pattern?
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'pattern? :ns *ns*))
  (defn normalize-pull
    ([expr]
      (let [result (if (datalog/source? (second expr))
                     expr
                     (concat [(first expr) '$] (rest expr)))]
        (when-not (and (datalog/variable? (nth result (int 2))) (pattern? (nth result (int 3))))
          (error/arg :db.error/invalid-pull (str "Invalid pull expression " expr)))
        result)))
  (reset-meta!
    #'normalize-pull
    (assoc
      {:arglists (clojure.core/list ['expr]), :column (int 1)}
      :name
      'normalize-pull
      :ns
      *ns*))
  (defn process-pulls
    ([p__19194]
      (let [map__19195 p__19194
            map__19195 (if (seq? map__19195)
                         (if (next map__19195)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19195))
                           (if (seq map__19195) (first map__19195) {}))
                         map__19195)
            qmap map__19195
            find (get map__19195 :find)
            in (get map__19195 :in)
            pull? (fn pull_QMARK_
                    ([p1__19191#]
                      (and (instance? java.util.List p1__19191#) (= 'pull (first p1__19191#)))))]
        (if (some pull? find)
          (assoc
            qmap
            :pull
            (mapv
              (fn fn__19199
                ([p1__19192#]
                  (if (^clojure.lang.IFn pull? p1__19192#)
                    (let [vec__19200 (normalize-pull p1__19192#)
                          _ (nth vec__19200 (int 0) nil)
                          db (nth vec__19200 (int 1) nil)
                          var (nth vec__19200 (int 2) nil)
                          pattern (nth vec__19200 (int 3) nil)]
                      (when (and (symbol? pattern) (not (some #{pattern} in)))
                        (error/arg
                          :db.error/pattern-not-bound
                          (str "Pull pattern not found in inputs: " pattern)))
                      {:db db, :var var, :pattern pattern})
                    p1__19192#)))
              find)
            :find
            (mapv
              (fn fn__19205
                ([p1__19193#]
                  (if (^clojure.lang.IFn pull? p1__19193#)
                    (first (filter datalog/variable? p1__19193#))
                    p1__19193#)))
              find))
          qmap))))
  (reset-meta!
    #'process-pulls
    (assoc
      {:arglists (clojure.core/list [{:keys ['find 'in], :as 'qmap}]), :column (int 1)}
      :name
      'process-pulls
      :ns
      *ns*))
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
  (reset-meta!
    #'mapify-query
    (assoc {:arglists (clojure.core/list ['query]), :column (int 1)} :name 'mapify-query :ns *ns*))
  (defn rename-self-unifications
    ([qmap padding]
      (update-in
        qmap
        [:where]
        (fn fn__19212
          ([p1__19211#]
            (reduce
              (fn fn__19213
                ([result clause]
                  (let [result result
                        syms (java.util.HashSet.)
                        rclause []
                        G__19217 clause
                        vec__19218 G__19217
                        seq__19219 (seq vec__19218)
                        first__19220 (first seq__19219)
                        seq__19219 (next seq__19219)
                        item first__19220
                        more seq__19219]
                    (loop [result result syms syms rclause rclause G__19217 G__19217]
                      (let [result result
                            syms syms
                            rclause rclause
                            vec__19221 G__19217
                            seq__19222 (seq vec__19221)
                            first__19223 (first seq__19222)
                            seq__19222 (next seq__19222)
                            item first__19223
                            more seq__19222]
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
              p1__19211#))))))
  (reset-meta!
    #'rename-self-unifications
    (assoc
      {:private true, :arglists (clojure.core/list ['qmap 'padding]), :column (int 1)}
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
      {:private true, :arglists (clojure.core/list ['qmap]), :column (int 1)}
      :name
      'gensym-padding
      :ns
      *ns*))
  (defn has-self-unifications?
    ([qmap]
      (boolean
        (first
          (remove
            (fn fn__19228
              ([clause]
                (=
                  (long (count (filter datalog/variable? clause)))
                  (long (count (set (filter datalog/variable? clause)))))))
            (:where qmap))))))
  (reset-meta!
    #'has-self-unifications?
    (assoc
      {:private true, :arglists (clojure.core/list ['qmap]), :column (int 1)}
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
      {:private true, :arglists (clojure.core/list ['qmap]), :column (int 1)}
      :name
      'process-self-unifications
      :ns
      *ns*))
  (defn compile-construct-1
    ([smap clause]
      (cons
        'vector
        (map
          (fn fn__19232
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
  (reset-meta!
    #'compile-construct-1
    (assoc
      {:arglists (clojure.core/list ['smap 'clause]), :column (int 1)}
      :name
      'compile-construct-1
      :ns
      *ns*))
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
  (reset-meta!
    #'compile-construct-n
    (assoc
      {:arglists (clojure.core/list ['find-clause 'construct-clause]), :column (int 1)}
      :name
      'compile-construct-n
      :ns
      *ns*))
  (defn compile-construct
    ([query]
      (let [temp__5802__auto__ (:construct query)]
        (if temp__5802__auto__
          (let [construct temp__5802__auto__ src (compile-construct-n (:find query) construct)]
            (assoc query :construct-fn-src src :construct-fn (eval src)))
          query))))
  (reset-meta!
    #'compile-construct
    (assoc
      {:arglists (clojure.core/list ['query]), :column (int 1)}
      :name
      'compile-construct
      :ns
      *ns*))
  (defn resolve-qualified-fn
    ([x]
      (let [temp__5804__auto__ (and (sequential? x) (first x))]
        (when temp__5804__auto__
          (let [f temp__5804__auto__]
            (when (common/qualified-symbol? f) (common/maybe-require 'datomic.extensions f)))))))
  (reset-meta!
    #'resolve-qualified-fn
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'resolve-qualified-fn
      :ns
      *ns*))
  (defn resolve-qualified-fns
    ([p__19241]
      (let [map__19242 p__19241
            map__19242 (if (seq? map__19242)
                         (if (next map__19242)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19242))
                           (if (seq map__19242) (first map__19242) {}))
                         map__19242)
            where (get map__19242 :where)
            group (get map__19242 :group)]
        (loop [seq_19243 (seq (map first where)) chunk_19244 nil count_19245 0 i_19246 0]
          (if (< i_19246 count_19245)
            (let [clause (.nth ^clojure.lang.Indexed chunk_19244 (int i_19246))]
              (resolve-qualified-fn clause)
              (recur seq_19243 chunk_19244 count_19245 (inc i_19246)))
            (let [temp__5804__auto__ (seq seq_19243)]
              (when temp__5804__auto__
                (let [seq_19243 temp__5804__auto__]
                  (if (chunked-seq? seq_19243)
                    (let [c__6065__auto__ (chunk-first seq_19243)]
                      (recur
                        (chunk-rest seq_19243)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [clause (first seq_19243)]
                      (resolve-qualified-fn clause)
                      (recur (next seq_19243) nil 0 0))))))))
        (loop [seq_19247 (seq group) chunk_19248 nil count_19249 0 i_19250 0]
          (if (< i_19250 count_19249)
            (let [clause (.nth ^clojure.lang.Indexed chunk_19248 (int i_19250))]
              (resolve-qualified-fn clause)
              (recur seq_19247 chunk_19248 count_19249 (inc i_19250)))
            (let [temp__5804__auto__ (seq seq_19247)]
              (when temp__5804__auto__
                (let [seq_19247 temp__5804__auto__]
                  (if (chunked-seq? seq_19247)
                    (let [c__6065__auto__ (chunk-first seq_19247)]
                      (recur
                        (chunk-rest seq_19247)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [clause (first seq_19247)]
                      (resolve-qualified-fn clause)
                      (recur (next seq_19247) nil 0 0)))))))))))
  (reset-meta!
    #'resolve-qualified-fns
    (assoc
      {:arglists (clojure.core/list [{:keys ['where 'group]}]), :column (int 1)}
      :name
      'resolve-qualified-fns
      :ns
      *ns*))
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
  (reset-meta!
    #'parse-query
    (assoc {:arglists (clojure.core/list ['query]), :column (int 1)} :name 'parse-query :ns *ns*))
  (defn load-query
    ([query]
      (let [query (parse-query query)
            query (move-sources-to-meta query)
            vec__19257 (datalog/prep-clauses nil (:where query))
            rm (nth vec__19257 (int 0) nil)
            cs (nth vec__19257 (int 1) nil)
            query (assoc query :where cs :arules rm)
            query (compile-construct query)]
        query)))
  (reset-meta!
    #'load-query
    (assoc {:arglists (clojure.core/list ['query]), :column (int 1)} :name 'load-query :ns *ns*))
  (defn group-rel
    ([fv rel]
      (let [grp_idxs (filterv integer? fv)]
        (if (= (count fv) (count grp_idxs))
          (mapv
            (fn fn__19262
              ([p1__19261#] (subvec p1__19261# 0 (java.lang.Integer/valueOf (int (count fv))))))
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
                            (let [vec__19275 fve
                                  idx (nth vec__19275 (int 0) nil)
                                  f (nth vec__19275 (int 1) nil)]
                              (^clojure.lang.IFn f
                                (^clojure.lang.IFn agg idx (long r) (long cnt))))))))
                    (recur (conj ret (vec nextrow)) nextr))))))))))
  (reset-meta!
    #'group-rel
    (assoc {:arglists (clojure.core/list ['fv 'rel]), :column (int 1)} :name 'group-rel :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.query" "query-cache") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.query" "query-cache")
    (cache/create-computing load-query 1000))
  (defn group-fv
    ([find_clause group_clause]
      (let [smap (zipmap find_clause (range))
            gns (find-ns 'datomic.aggregation)
            list? (fn list_QMARK_ ([p1__19280#] (instance? java.util.List p1__19280#)))]
        (mapv
          (fn fn__19284
            ([p1__19281#]
              (if (^clojure.lang.IFn list? p1__19281#)
                (let [temp__5802__auto__ (ns-resolve gns (first p1__19281#))]
                  (if temp__5802__auto__
                    (let [agg_fn temp__5802__auto__]
                      [(^clojure.lang.IFn smap (last p1__19281#))
                       (apply partial agg_fn (butlast (rest p1__19281#)))])
                    (error/arg
                      :db.error/invalid-aggregate
                      (str
                        "Argument "
                        (first p1__19281#)
                        " in :find is not an aggregate function"))))
                (^clojure.lang.IFn smap p1__19281#))))
          group_clause))))
  (reset-meta!
    #'group-fv
    (assoc
      {:arglists (clojure.core/list ['find-clause 'group-clause]), :column (int 1)}
      :name
      'group-fv
      :ns
      *ns*))
  (defn xf-tuple
    ([fv tuple]
      (let [result (transient [])
            G__19294 tuple
            vec__19296 G__19294
            seq__19297 (seq vec__19296)
            first__19298 (first seq__19297)
            seq__19297 (next seq__19297)
            col first__19298
            tuple seq__19297
            G__19295 fv
            vec__19299 G__19295
            seq__19300 (seq vec__19299)
            first__19301 (first seq__19300)
            seq__19300 (next seq__19300)
            f first__19301
            fv seq__19300]
        (loop [result result G__19294 G__19294 G__19295 G__19295]
          (let [result result
                vec__19302 G__19294
                seq__19303 (seq vec__19302)
                first__19304 (first seq__19303)
                seq__19303 (next seq__19303)
                col first__19304
                tuple seq__19303
                vec__19305 G__19295
                seq__19306 (seq vec__19305)
                first__19307 (first seq__19306)
                seq__19306 (next seq__19306)
                f first__19307
                fv seq__19306]
            (if f
              (recur (conj! result (^clojure.lang.IFn f col)) tuple fv)
              (persistent! result)))))))
  (reset-meta!
    #'xf-tuple
    (assoc {:arglists (clojure.core/list ['fv 'tuple]), :column (int 1)} :name 'xf-tuple :ns *ns*))
  (defn pull-fv
    ([p__19310 srcs]
      (let [map__19311 p__19310
            map__19311 (if (seq? map__19311)
                         (if (next map__19311)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19311))
                           (if (seq map__19311) (first map__19311) {}))
                         map__19311)
            qmap map__19311
            find (get map__19311 :find)
            pull (get map__19311 :pull)
            with (get map__19311 :with)
            in (get map__19311 :in)
            smap (zipmap find (range))
            srcmap (zipmap in srcs)
            colct (- (count find) (count with))]
        (reduce
          (fn fn__19313
            ([m p__19312]
              (let [map__19314 p__19312
                    map__19314 (if (seq? map__19314)
                                 (if (next map__19314)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__19314))
                                   (if (seq map__19314) (first map__19314) {}))
                                 map__19314)
                    db (get map__19314 :db)
                    var (get map__19314 :var)
                    pattern (get map__19314 :pattern)]
                (assoc
                  m
                  (common/getx smap var)
                  (fn fn__19315
                    ([p1__19309#]
                      (first
                        (pull/pull
                          (get srcmap db (first srcs))
                          (get srcmap pattern pattern)
                          [p1__19309#]))))))))
          (vec (repeat (long colct) identity))
          (filter map? pull)))))
  (reset-meta!
    #'pull-fv
    (assoc
      {:arglists (clojure.core/list [{:keys ['find 'pull 'with 'in], :as 'qmap} 'srcs]),
       :column (int 1)}
      :name
      'pull-fv
      :ns
      *ns*))
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
            G__19322 (java.util.ArrayList. ^java.util.Collection coll)]
        (.sort ^java.util.ArrayList G__19322 ^java.util.Comparator cmp)
        G__19322)))
  (reset-meta!
    #'sort-collection-by-indexed
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [(.withMeta 'coll {:tag 'Collection}) (.withMeta 'idx {:tag 'long})]),
       :column (int 1)}
      :name
      'sort-collection-by-indexed
      :ns
      *ns*))
  (defn sort-for-pull
    ([query result]
      (let [temp__5802__auto__ (first
                                 (keep-indexed
                                   (fn fn__19324 ([idx item] (when (map? item) idx)))
                                   (:pull query)))]
        (if temp__5802__auto__
          (let [idx temp__5802__auto__]
            (sort-collection-by-indexed result (long ^java.lang.Number idx)))
          result))))
  (reset-meta!
    #'sort-for-pull
    (assoc
      {:private true, :arglists (clojure.core/list ['query 'result]), :column (int 1)}
      :name
      'sort-for-pull
      :ns
      *ns*))
  (defn q*
    ([query srcs]
      (let [vec__19330 (qs/parse-as query)
            query (nth vec__19330 (int 0) nil)
            as (nth vec__19330 (int 1) nil)
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
                         (fn fn__19333 ([p1__19328#] (xf-tuple fv p1__19328#)))))
              asfn (when as
                     (fn fn__19335
                       ([p1__19329#]
                         (datomic.query.support.MapOnIndexed.
                           ^clojure.lang.IPersistentVector as
                           ^clojure.lang.Indexed p1__19329#))))
              pfn (when (or pullfn asfn) (comp (or asfn identity) (or pullfn identity)))
              temp__5802__auto__ (:find-bindings qmap)]
          (if temp__5802__auto__
            (let [fb temp__5802__auto__ ret (if pfn (map pfn ret) ret)]
              [(let [G__19337 fb]
                 (case
                   G__19337
                   one-value
                   (ffirst ret)
                   first-tuple
                   (first ret)
                   one-column
                   (mapv first ret)))
               nil])
            [ret pfn])))))
  (reset-meta!
    #'q*
    (assoc {:arglists (clojure.core/list ['query 'srcs]), :column (int 1)} :name 'q* :ns *ns*))
  (defn query*
    ([query_map]
      (let [timeout (:timeout query_map)
            qmap (cond-> (mapify-query (:query query_map)) timeout (assoc :timeout [timeout]))]
        (q* qmap (:args query_map)))))
  (reset-meta!
    #'query*
    (assoc {:arglists (clojure.core/list ['query-map]), :column (int 1)} :name 'query* :ns *ns*))
  (defn apply-pf
    ([p__19347]
      (let [vec__19348 p__19347
            result (nth vec__19348 (int 0) nil)
            pf (nth vec__19348 (int 1) nil)]
        (if pf (mapv pf result) result))))
  (reset-meta!
    #'apply-pf
    (assoc
      {:private true, :arglists (clojure.core/list [['result 'pf]]), :column (int 1)}
      :name
      'apply-pf
      :ns
      *ns*))
  (defn q ([query srcs] (apply-pf (q* query srcs))))
  (reset-meta!
    #'q
    (assoc {:arglists (clojure.core/list ['query 'srcs]), :column (int 1)} :name 'q :ns *ns*))
  (defn query
    ([p__19353]
      (let [map__19354 p__19353
            map__19354 (if (seq? map__19354)
                         (if (next map__19354)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19354))
                           (if (seq map__19354) (first map__19354) {}))
                         map__19354)
            query_map map__19354
            io_context (get map__19354 :io-context)
            query_stats (get map__19354 :query-stats)
            f (fn f ([] (apply-pf (query* query_map))))
            f (if io_context
                (fn fn__19357
                  ([]
                    (io-stats/throw-if-ex!
                      (io-stats/with-io-stats f {:io-context io_context, :api :query}))))
                f)
            f (if query_stats
                (fn fn__19359 ([] (query-stats/with-query-stats f {:query (:query query_map)})))
                f)]
        (^clojure.lang.IFn f))))
  (reset-meta!
    #'query
    (assoc
      {:arglists (clojure.core/list [{:keys ['io-context 'query-stats], :as 'query-map}]),
       :column (int 1)}
      :name
      'query
      :ns
      *ns*))
  (defn qseq
    ([query_map args] (qseq {:query query_map, :args args}))
    ([p__19362]
      (let [map__19363 p__19362
            map__19363 (if (seq? map__19363)
                         (if (next map__19363)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19363))
                           (if (seq map__19363) (first map__19363) {}))
                         map__19363)
            query_map map__19363
            offset (get map__19363 :offset 0)
            limit (get map__19363 :limit (long java.lang.Long/MAX_VALUE))
            io_context (get map__19363 :io-context)
            f (fn f ([] (query* query_map)))
            map__19364 (when io_context
                         (io-stats/throw-if-ex!
                           (io-stats/with-io-stats f {:io-context io_context, :api :qseq})))
            map__19364 (if (seq? map__19364)
                         (if (next map__19364)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19364))
                           (if (seq map__19364) (first map__19364) {}))
                         map__19364)
            ret (get map__19364 :ret)
            io_stats (get map__19364 :io-stats)
            ret (if io_context ret (^clojure.lang.IFn f))
            vec__19365 ret
            result (nth vec__19365 (int 0) nil)
            pf (nth vec__19365 (int 1) nil)
            xform (common/result-xform offset limit pf)
            cseq (qs/counted-seq
                   (sequence xform result)
                   (common/result-count offset limit result))]
        (if io_context {:ret cseq, :io-stats io_stats} cseq))))
  (reset-meta!
    #'qseq
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['offset 'limit 'io-context],
           :or {'offset 0, 'limit 'Long/MAX_VALUE},
           :as 'query-map}]
         ['query-map 'args]),
       :column (int 1)}
      :name
      'qseq
      :ns
      *ns*))
  (defn cache ([q] (get query-cache q)))
  (reset-meta!
    #'cache
    (assoc {:arglists (clojure.core/list ['q]), :column (int 1)} :name 'cache :ns *ns*))
  (defn construct-fn ([qform] (fn fn__19372 ([& args] (q qform args)))))
  (reset-meta!
    #'construct-fn
    (assoc {:arglists (clojure.core/list ['qform]), :column (int 1)} :name 'construct-fn :ns *ns*)))
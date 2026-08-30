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
                   (fn fn__16482
                     ([p1__16479#]
                       (and
                         (= rid (.getV ^datomic.impl.db.IDatum p1__16479#))
                         (= attrid (long (.getA ^datomic.impl.db.IDatum p1__16479#))))))
                   (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid)))
            attr (and attrid (db/attribute db attrid))
            component? (and attr (.-isComponent ^datomic.db.Attribute attr))]
        (when iter
          (if component?
            (emap db (long (.getE (.get ^datomic.iter.Iter iter))))
            (iter/reduce
              (fn fn__16485
                ([p1__16480# p2__16481#]
                  (conj p1__16480# (emap db (long (.getE ^datomic.impl.db.IDatum p2__16481#))))))
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
  (def eav
   (fn eav
     ([db e a]
       (if (db/reverse-lookup? db a)
         (rae db e (keyword (namespace a) (subs (name a) 1)))
         (let [attrid (db/resolve-id db a)
               eid (db/resolve-id db e)
               iter (db/windowed
                      db
                      (fn fn__16495
                        ([p1__16492#]
                          (and
                            (= eid (long (.getE ^datomic.impl.db.IDatum p1__16492#)))
                            (= attrid (long (.getA ^datomic.impl.db.IDatum p1__16492#))))))
                      (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid :a attrid)))
               attr (and attrid (db/attribute db attrid))
               vtypeid (and attr (.-vtypeid ^datomic.db.Attribute attr))
               ref? (and vtypeid (= vtypeid 20))
               maybe_bind (fn maybe_bind ([v] (ref-val db ref? v)))]
           (when iter
             (if (and attr (= 36 (.-cardinality ^datomic.db.Attribute attr)))
               (iter/reduce
                 (fn fn__16500
                   ([p1__16493# p2__16494#]
                     (conj
                       p1__16493#
                       (^clojure.lang.IFn maybe_bind (.getV ^datomic.impl.db.IDatum p2__16494#)))))
                 #{}
                 iter)
               (^clojure.lang.IFn maybe_bind (.getV (.get ^datomic.iter.Iter iter))))))))))
  (reset-meta!
    #'eav
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Db}) 'e 'a]), :column (int 1)}
      :name
      'eav
      :ns
      *ns*))
  (def get-lazy-entity
   (fn get_lazy_entity
     ([db ent]
       (let [eid (db/resolve-id db ent)
             ret (iter/reduce
                   (fn fn__16509
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
                     (fn fn__16515
                       ([p1__16507#] (= eid (long (.getE ^datomic.impl.db.IDatum p1__16507#)))))
                     (.seekEAVT ^datomic.db.IDb db (db/datum db :e eid))))]
         ret))))
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
              (fn fn__16535
                ([p__16534]
                  (let [vec__16536 p__16534
                        k (nth vec__16536 (int 0) nil)
                        v (nth vec__16536 (int 1) nil)]
                    (contains? edits k))))
              m)
            (remove
              (fn fn__16541
                ([p__16540]
                  (let [vec__16542 p__16540
                        k (nth vec__16542 (int 0) nil)
                        v (nth vec__16542 (int 1) nil)]
                    (nil? v))))
              edits)))))
    (^datomic.Database db [this] ^datomic.Database db)
    (^java.util.Set keySet [this] (into #{} (map (comp str first) (seq this))))
    (^datomic.Entity touch
      [this]
      (do
        (loop [seq_16522 (seq (keys this)) chunk_16523 nil count_16524 0 i_16525 0]
          (if (< i_16525 count_16524)
            (let [a (.nth ^clojure.lang.Indexed chunk_16523 (int i_16525))]
              (let [attrid (db/resolve-id db a)
                    attr (.elementAt ^datomic.db.IDbImpl db attrid)
                    v (.valAt this a)]
                (when (and
                        (.-isComponent ^datomic.db.Attribute attr)
                        (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                  (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                    (loop [seq_16526 (seq v) chunk_16527 nil count_16528 0 i_16529 0]
                      (if (< i_16529 count_16528)
                        (let [v (.nth ^clojure.lang.Indexed chunk_16527 (int i_16529))]
                          (touch v)
                          (recur seq_16526 chunk_16527 count_16528 (inc i_16529)))
                        (let [temp__5825__auto__ (seq seq_16526)]
                          (when temp__5825__auto__
                            (let [seq_16526 temp__5825__auto__]
                              (if (chunked-seq? seq_16526)
                                (let [c__6090__auto__ (chunk-first seq_16526)]
                                  (recur
                                    (chunk-rest seq_16526)
                                    c__6090__auto__
                                    (int (count c__6090__auto__))
                                    (int 0)))
                                (let [v (first seq_16526)]
                                  (touch v)
                                  (recur (next seq_16526) nil 0 0))))))))
                    (touch v))))
              (recur seq_16522 chunk_16523 count_16524 (inc i_16525)))
            (let [temp__5825__auto__ (seq seq_16522)]
              (when temp__5825__auto__
                (let [seq_16522 temp__5825__auto__]
                  (if (chunked-seq? seq_16522)
                    (let [c__6090__auto__ (chunk-first seq_16522)]
                      (recur
                        (chunk-rest seq_16522)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [a (first seq_16522)]
                      (let [attrid (db/resolve-id db a)
                            attr (.elementAt ^datomic.db.IDbImpl db attrid)
                            v (.valAt this a)]
                        (when (and
                                (.-isComponent ^datomic.db.Attribute attr)
                                (= (.-vtypeid ^datomic.db.Attribute attr) 20))
                          (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                            (loop [seq_16530 (seq v) chunk_16531 nil count_16532 0 i_16533 0]
                              (if (< i_16533 count_16532)
                                (let [v (.nth ^clojure.lang.Indexed chunk_16531 (int i_16533))]
                                  (touch v)
                                  (recur seq_16530 chunk_16531 count_16532 (inc i_16533)))
                                (let [temp__5825__auto__ (seq seq_16530)]
                                  (when temp__5825__auto__
                                    (let [seq_16530 temp__5825__auto__]
                                      (if (chunked-seq? seq_16530)
                                        (let [c__6090__auto__ (chunk-first seq_16530)]
                                          (recur
                                            (chunk-rest seq_16530)
                                            c__6090__auto__
                                            (int (count c__6090__auto__))
                                            (int 0)))
                                        (let [v (first seq_16530)]
                                          (touch v)
                                          (recur (next seq_16530) nil 0 0))))))))
                            (touch v))))
                      (recur (next seq_16522) nil 0 0))))))))
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
    fn__16561
    ([m w]
      (print-method
        (merge (.cache ^datomic.query.EMapImpl m) (.-edits ^datomic.query.EntityMap m))
        w)))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      Immutify
      (immutify
        [x]
        "Return immutable form of x. Presumes that if top of a data structure\nis immutable, rest is too."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.query" "Immutify")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'Immutify :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'immutify {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc
                                      "Return immutable form of x. Presumes that if top of a data structure\nis immutable, rest is too."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.query" "Immutify"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.query" "immutify")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (extend nil Immutify {:immutify (fn fn__16579 ([x] x))})
  (extend java.lang.Object Immutify {:immutify (fn fn__16581 ([x] x))})
  (extend
    java.util.Map
    Immutify
    {:immutify
     (fn fn__16583
       ([x]
         (if (map? x)
           x
           (persistent!
             (reduce
               (fn fn__16585
                 ([m p__16584]
                   (let [vec__16586 p__16584
                         k (nth vec__16586 (int 0) nil)
                         v (nth vec__16586 (int 1) nil)]
                     (assoc! m (immutify k) (immutify v)))))
               (transient {})
               x)))))})
  (extend
    java.util.Set
    Immutify
    {:immutify (fn fn__16591 ([x] (if (set? x) x (set (map immutify x)))))})
  (extend
    java.util.List
    Immutify
    {:immutify (fn fn__16593 ([x] (if (sequential? x) x (mapv immutify x))))})
  (defn listq->mapq
    ([lq]
      (reduce
        (fn fn__16596
          ([m p__16595]
            (let [vec__16597 p__16595
                  k (nth vec__16597 (int 0) nil)
                  v (nth vec__16597 (int 1) nil)
                  k (first k)]
              (assoc m k v))))
        {}
        (partition 2 (partition-by #{:find :where :syms :keys :with :timeout :strs :in} lq)))))
  (reset-meta!
    #'listq->mapq
    (assoc {:arglists (clojure.core/list ['lq]), :column (int 1)} :name 'listq->mapq :ns *ns*))
  (def move-sources-to-meta
   (fn move_sources_to_meta
     ([p__16603]
       (let [map__16604 p__16603
             map__16604 (if (seq? map__16604)
                          (if (next map__16604)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16604))
                            (if (seq map__16604) (first map__16604) {}))
                          map__16604)
             q map__16604
             srcs (get map__16604 :in)
             clauses (get map__16604 :where)]
         (if srcs
           (let [sset (set srcs)]
             (assoc
               q
               :where
               (map
                 (fn fn__16605
                   ([p1__16602#]
                     (if (^clojure.lang.IFn sset (first p1__16602#))
                       (with-meta (next p1__16602#) {:tag (first p1__16602#)})
                       (do
                         (when (datalog/source? (first p1__16602#))
                           (throw
                             (java.lang.IllegalArgumentException.
                               (str "Data source not supplied: " (first p1__16602#)))))
                         (when :else p1__16602#)))))
                 clauses)))
           q)))))
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
                     ([p1__16608#]
                       (or (sequential? p1__16608#) (instance? java.util.List p1__16608#))))]
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
      (loop [seq_16614 (seq (:find qmap)) chunk_16615 nil count_16616 0 i_16617 0]
        (if (< i_16617 count_16616)
          (let [f (.nth ^clojure.lang.Indexed chunk_16615 (int i_16617))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :find is not a variable"))))
            (recur seq_16614 chunk_16615 count_16616 (inc i_16617)))
          (let [temp__5825__auto__ (seq seq_16614)]
            (when temp__5825__auto__
              (let [seq_16614 temp__5825__auto__]
                (if (chunked-seq? seq_16614)
                  (let [c__6090__auto__ (chunk-first seq_16614)]
                    (recur
                      (chunk-rest seq_16614)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [f (first seq_16614)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :find is not a variable"))))
                    (recur (next seq_16614) nil 0 0))))))))
      (loop [seq_16618 (seq (:with qmap)) chunk_16619 nil count_16620 0 i_16621 0]
        (if (< i_16621 count_16620)
          (let [f (.nth ^clojure.lang.Indexed chunk_16619 (int i_16621))]
            (when-not (datalog/variable? f)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " f " in :with is not a variable"))))
            (recur seq_16618 chunk_16619 count_16620 (inc i_16621)))
          (let [temp__5825__auto__ (seq seq_16618)]
            (when temp__5825__auto__
              (let [seq_16618 temp__5825__auto__]
                (if (chunked-seq? seq_16618)
                  (let [c__6090__auto__ (chunk-first seq_16618)]
                    (recur
                      (chunk-rest seq_16618)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [f (first seq_16618)]
                    (when-not (datalog/variable? f)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " f " in :with is not a variable"))))
                    (recur (next seq_16618) nil 0 0))))))))
      (loop [seq_16622 (seq (:where qmap)) chunk_16623 nil count_16624 0 i_16625 0]
        (if (< i_16625 count_16624)
          (let [w (.nth ^clojure.lang.Indexed chunk_16623 (int i_16625))]
            (when-not (instance? java.util.List w)
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " w " in :where is not a list"))))
            (recur seq_16622 chunk_16623 count_16624 (inc i_16625)))
          (let [temp__5825__auto__ (seq seq_16622)]
            (when temp__5825__auto__
              (let [seq_16622 temp__5825__auto__]
                (if (chunked-seq? seq_16622)
                  (let [c__6090__auto__ (chunk-first seq_16622)]
                    (recur
                      (chunk-rest seq_16622)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [w (first seq_16622)]
                    (when-not (instance? java.util.List w)
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " w " in :where is not a list"))))
                    (recur (next seq_16622) nil 0 0))))))))
      (loop [seq_16626 (seq (:in qmap)) chunk_16627 nil count_16628 0 i_16629 0]
        (if (< i_16629 count_16628)
          (let [i (.nth ^clojure.lang.Indexed chunk_16627 (int i_16629))]
            (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
              (throw
                (java.lang.IllegalArgumentException.
                  (str "Argument " i " in :in is not a source"))))
            (recur seq_16626 chunk_16627 count_16628 (inc i_16629)))
          (let [temp__5825__auto__ (seq seq_16626)]
            (when temp__5825__auto__
              (let [seq_16626 temp__5825__auto__]
                (if (chunked-seq? seq_16626)
                  (let [c__6090__auto__ (chunk-first seq_16626)]
                    (recur
                      (chunk-rest seq_16626)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [i (first seq_16626)]
                    (when-not (or (datalog/source? i) (datalog/rules? i) (symbol? i))
                      (throw
                        (java.lang.IllegalArgumentException.
                          (str "Argument " i " in :in is not a source"))))
                    (recur (next seq_16626) nil 0 0))))))))
      (let [temp__5825__auto__ (seq (datalog/callees (:construct qmap)))]
        (when temp__5825__auto__
          (let [callees temp__5825__auto__]
            (throw
              (java.lang.IllegalArgumentException.
                (str (first callees) " is not valid in :construct")))))
        nil)
      (let [temp__5825__auto__ (seq
                                 (set/difference
                                   (variables-in-clause (:construct qmap))
                                   (variables-in-clause (:find qmap))))]
        (when temp__5825__auto__
          (let [unfound temp__5825__auto__]
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
      (let [temp__5823__auto__ (:in qmap)]
        (if temp__5823__auto__
          (let [ins temp__5823__auto__
                binding? (fn binding_QMARK_
                           ([p1__16646#]
                             (or
                               (datalog/variable? p1__16646#)
                               (instance? java.util.List p1__16646#))))]
            (reduce
              (fn fn__16654
                ([m p__16653]
                  (let [vec__16655 p__16653
                        i (nth vec__16655 (int 0) nil)
                        x (nth vec__16655 (int 1) nil)]
                    (if (^clojure.lang.IFn binding? x)
                      (let [gs (symbol (str prefix (inc i))) bind_type (datalog/binding-type x)]
                        (cond->
                          (update-in
                            (update-in m [:in] conj gs)
                            [:where]
                            (fn fn__16659 ([p1__16648# p2__16647#] (cons p2__16647# p1__16648#)))
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
                                (fn fn__16661 ([p1__16649#] (vector gs p1__16649#)))
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
                     ([p1__16666#]
                       (or (get consts p1__16666#) (not (datalog/variable? p1__16666#)))))
            swap {'< '>, '<= '>=, '> '<, '>= '<=, '= '=}
            norm (fn norm
                   ([p__16671]
                     (let [vec__16673 p__16671
                           vec__16676 (nth vec__16673 (int 0) nil)
                           cmp (nth vec__16676 (int 0) nil)
                           x (nth vec__16676 (int 1) nil)
                           y (nth vec__16676 (int 2) nil)
                           c vec__16676]
                       (cond
                         (and (not (^clojure.lang.IFn const? x)) (^clojure.lang.IFn const? y)) c
                         (and (^clojure.lang.IFn const? x) (not (^clojure.lang.IFn const? y))) (do
                                                                                                 (clojure.core/list
                                                                                                   (^clojure.lang.IFn swap
                                                                                                     cmp)
                                                                                                   y
                                                                                                   x))))))
            cexprs (keep
                     (fn fn__16682
                       ([p1__16667#]
                         (let [or__5602__auto__ (and
                                                  (instance? java.util.List p1__16667#)
                                                  (instance? java.util.List (first p1__16667#))
                                                  (= (long (count p1__16667#)) 1)
                                                  (#{'>= '> '<= '= '<} (ffirst p1__16667#))
                                                  (^clojure.lang.IFn norm p1__16667#))]
                           (when or__5602__auto__ or__5602__auto__))))
                     (:where qmap))
            assoc_stronger (fn assoc_stronger
                             ([m k v cmp]
                               (let [ev (get m k)]
                                 (if (or (nil? ev) (= '= cmp)) (assoc m k v) m))))]
        (reduce
          (fn fn__16693
            ([m p__16692]
              (let [vec__16694 p__16692
                    cmp (nth vec__16694 (int 0) nil)
                    var (nth vec__16694 (int 1) nil)
                    const (nth vec__16694 (int 2) nil)]
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
            list? (fn list_QMARK_ ([p1__16700#] (instance? java.util.List p1__16700#)))
            qmap (if (or (:with qmap) (some list? finds))
                   (assoc
                     qmap
                     :find
                     (vec
                       (distinct
                         (map
                           (fn fn__16704
                             ([p1__16701#]
                               (if (^clojure.lang.IFn list? p1__16701#)
                                 (last p1__16701#)
                                 p1__16701#)))
                           finds)))
                     :group
                     (vec finds))
                   qmap)
            qmap (let [temp__5823__auto__ (:with qmap)]
                   (if temp__5823__auto__
                     (let [w temp__5823__auto__] (assoc qmap :find (into (vec (:find qmap)) w)))
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
  (def process-find-bindings
   (fn process_find_bindings
     ([p__16710]
       (let [map__16711 p__16710
             map__16711 (if (seq? map__16711)
                          (if (next map__16711)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16711))
                            (if (seq map__16711) (first map__16711) {}))
                          map__16711)
             qmap map__16711
             find (get map__16711 :find)]
         (cond
           (and
             (= 1 (long (count find)))
             (instance? java.util.List (first find))
             (every?
               (fn fn__16712
                 ([p1__16709#]
                   (or (datalog/variable? p1__16709#) (instance? java.util.List p1__16709#))))
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
           :default (do qmap))))))
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
  (def process-pulls
   (fn process_pulls
     ([p__16729]
       (let [map__16730 p__16729
             map__16730 (if (seq? map__16730)
                          (if (next map__16730)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16730))
                            (if (seq map__16730) (first map__16730) {}))
                          map__16730)
             qmap map__16730
             find (get map__16730 :find)
             in (get map__16730 :in)
             pull? (fn pull_QMARK_
                     ([p1__16726#]
                       (and (instance? java.util.List p1__16726#) (= 'pull (first p1__16726#)))))]
         (if (some pull? find)
           (assoc
             qmap
             :pull
             (mapv
               (fn fn__16734
                 ([p1__16727#]
                   (if (^clojure.lang.IFn pull? p1__16727#)
                     (let [vec__16735 (normalize-pull p1__16727#)
                           _ (nth vec__16735 (int 0) nil)
                           db (nth vec__16735 (int 1) nil)
                           var (nth vec__16735 (int 2) nil)
                           pattern (nth vec__16735 (int 3) nil)]
                       (when (and (symbol? pattern) (not (some #{pattern} in)))
                         (error/arg
                           :db.error/pattern-not-bound
                           (str "Pull pattern not found in inputs: " pattern)))
                       {:db db, :var var, :pattern pattern})
                     p1__16727#)))
               find)
             :find
             (mapv
               (fn fn__16740
                 ([p1__16728#]
                   (if (^clojure.lang.IFn pull? p1__16728#)
                     (first (filter datalog/variable? p1__16728#))
                     p1__16728#)))
               find))
           qmap)))))
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
        (fn fn__16747
          ([p1__16746#]
            (reduce
              (fn fn__16748
                ([result clause]
                  (let [result result
                        syms (java.util.HashSet.)
                        rclause []
                        G__16752 clause
                        vec__16753 G__16752
                        seq__16754 (seq vec__16753)
                        first__16755 (first seq__16754)
                        seq__16754 (next seq__16754)
                        item first__16755
                        more seq__16754]
                    (loop [result result syms syms rclause rclause G__16752 G__16752]
                      (let [result result
                            syms syms
                            rclause rclause
                            vec__16756 G__16752
                            seq__16757 (seq vec__16756)
                            first__16758 (first seq__16757)
                            seq__16757 (next seq__16757)
                            item first__16758
                            more seq__16757]
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
              p1__16746#))))))
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
            (fn fn__16763
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
          (fn fn__16767
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
  (def compile-construct-n
   (fn compile_construct_n
     ([find_clause construct_clause]
       (let [smap (zipmap find_clause (range))]
         (seq
           (concat
             (clojure.core/list 'clojure.core/fn)
             (clojure.core/list (apply vector (seq (concat (clojure.core/list 'tuple)))))
             (clojure.core/list
               (apply
                 vector
                 (seq (concat (mapv (partial compile-construct-1 smap) construct_clause)))))))))))
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
      (let [temp__5823__auto__ (:construct query)]
        (if temp__5823__auto__
          (let [construct temp__5823__auto__ src (compile-construct-n (:find query) construct)]
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
      (let [temp__5825__auto__ (and (sequential? x) (first x))]
        (when temp__5825__auto__
          (let [f temp__5825__auto__]
            (when (common/qualified-symbol? f) (common/maybe-require 'datomic.extensions f)))))))
  (reset-meta!
    #'resolve-qualified-fn
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'resolve-qualified-fn
      :ns
      *ns*))
  (def resolve-qualified-fns
   (fn resolve_qualified_fns
     ([p__16776]
       (let [map__16777 p__16776
             map__16777 (if (seq? map__16777)
                          (if (next map__16777)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16777))
                            (if (seq map__16777) (first map__16777) {}))
                          map__16777)
             where (get map__16777 :where)
             group (get map__16777 :group)]
         (loop [seq_16778 (seq (map first where)) chunk_16779 nil count_16780 0 i_16781 0]
           (if (< i_16781 count_16780)
             (let [clause (.nth ^clojure.lang.Indexed chunk_16779 (int i_16781))]
               (resolve-qualified-fn clause)
               (recur seq_16778 chunk_16779 count_16780 (inc i_16781)))
             (let [temp__5825__auto__ (seq seq_16778)]
               (when temp__5825__auto__
                 (let [seq_16778 temp__5825__auto__]
                   (if (chunked-seq? seq_16778)
                     (let [c__6090__auto__ (chunk-first seq_16778)]
                       (recur
                         (chunk-rest seq_16778)
                         c__6090__auto__
                         (int (count c__6090__auto__))
                         (int 0)))
                     (let [clause (first seq_16778)]
                       (resolve-qualified-fn clause)
                       (recur (next seq_16778) nil 0 0))))))))
         (loop [seq_16782 (seq group) chunk_16783 nil count_16784 0 i_16785 0]
           (if (< i_16785 count_16784)
             (let [clause (.nth ^clojure.lang.Indexed chunk_16783 (int i_16785))]
               (resolve-qualified-fn clause)
               (recur seq_16782 chunk_16783 count_16784 (inc i_16785)))
             (let [temp__5825__auto__ (seq seq_16782)]
               (when temp__5825__auto__
                 (let [seq_16782 temp__5825__auto__]
                   (if (chunked-seq? seq_16782)
                     (let [c__6090__auto__ (chunk-first seq_16782)]
                       (recur
                         (chunk-rest seq_16782)
                         c__6090__auto__
                         (int (count c__6090__auto__))
                         (int 0)))
                     (let [clause (first seq_16782)]
                       (resolve-qualified-fn clause)
                       (recur (next seq_16782) nil 0 0))))))))))))
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
            vec__16792 (datalog/prep-clauses nil (:where query))
            rm (nth vec__16792 (int 0) nil)
            cs (nth vec__16792 (int 1) nil)
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
            (fn fn__16797
              ([p1__16796#] (subvec p1__16796# 0 (java.lang.Integer/valueOf (int (count fv))))))
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
                            (let [vec__16810 fve
                                  idx (nth vec__16810 (int 0) nil)
                                  f (nth vec__16810 (int 1) nil)]
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
  (def group-fv
   (fn group_fv
     ([find_clause group_clause]
       (let [smap (zipmap find_clause (range))
             gns (find-ns 'datomic.aggregation)
             list? (fn list_QMARK_ ([p1__16815#] (instance? java.util.List p1__16815#)))]
         (mapv
           (fn fn__16819
             ([p1__16816#]
               (if (^clojure.lang.IFn list? p1__16816#)
                 (let [temp__5823__auto__ (ns-resolve gns (first p1__16816#))]
                   (if temp__5823__auto__
                     (let [agg_fn temp__5823__auto__]
                       [(^clojure.lang.IFn smap (last p1__16816#))
                        (apply partial agg_fn (butlast (rest p1__16816#)))])
                     (error/arg
                       :db.error/invalid-aggregate
                       (str
                         "Argument "
                         (first p1__16816#)
                         " in :find is not an aggregate function"))))
                 (^clojure.lang.IFn smap p1__16816#))))
           group_clause)))))
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
            G__16829 tuple
            vec__16831 G__16829
            seq__16832 (seq vec__16831)
            first__16833 (first seq__16832)
            seq__16832 (next seq__16832)
            col first__16833
            tuple seq__16832
            G__16830 fv
            vec__16834 G__16830
            seq__16835 (seq vec__16834)
            first__16836 (first seq__16835)
            seq__16835 (next seq__16835)
            f first__16836
            fv seq__16835]
        (loop [result result G__16829 G__16829 G__16830 G__16830]
          (let [result result
                vec__16837 G__16829
                seq__16838 (seq vec__16837)
                first__16839 (first seq__16838)
                seq__16838 (next seq__16838)
                col first__16839
                tuple seq__16838
                vec__16840 G__16830
                seq__16841 (seq vec__16840)
                first__16842 (first seq__16841)
                seq__16841 (next seq__16841)
                f first__16842
                fv seq__16841]
            (if f
              (recur (conj! result (^clojure.lang.IFn f col)) tuple fv)
              (persistent! result)))))))
  (reset-meta!
    #'xf-tuple
    (assoc {:arglists (clojure.core/list ['fv 'tuple]), :column (int 1)} :name 'xf-tuple :ns *ns*))
  (def pull-fv
   (fn pull_fv
     ([p__16845 srcs]
       (let [map__16846 p__16845
             map__16846 (if (seq? map__16846)
                          (if (next map__16846)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16846))
                            (if (seq map__16846) (first map__16846) {}))
                          map__16846)
             qmap map__16846
             find (get map__16846 :find)
             pull (get map__16846 :pull)
             with (get map__16846 :with)
             in (get map__16846 :in)
             smap (zipmap find (range))
             srcmap (zipmap in srcs)
             colct (- (count find) (count with))]
         (reduce
           (fn fn__16848
             ([m p__16847]
               (let [map__16849 p__16847
                     map__16849 (if (seq? map__16849)
                                  (if (next map__16849)
                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                      (to-array map__16849))
                                    (if (seq map__16849) (first map__16849) {}))
                                  map__16849)
                     db (get map__16849 :db)
                     var (get map__16849 :var)
                     pattern (get map__16849 :pattern)]
                 (assoc
                   m
                   (common/getx smap var)
                   (fn fn__16850
                     ([p1__16844#]
                       (first
                         (pull/pull
                           (get srcmap db (first srcs))
                           (get srcmap pattern pattern)
                           [p1__16844#]))))))))
           (vec (repeat (long colct) identity))
           (filter map? pull))))))
  (reset-meta!
    #'pull-fv
    (assoc
      {:arglists (clojure.core/list [{:keys ['find 'pull 'with 'in], :as 'qmap} 'srcs]),
       :column (int 1)}
      :name
      'pull-fv
      :ns
      *ns*))
  (def sort-collection-by-indexed
   (fn sort_collection_by_indexed
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
             G__16857 (java.util.ArrayList. ^java.util.Collection coll)]
         (.sort ^java.util.ArrayList G__16857 ^java.util.Comparator cmp)
         G__16857))))
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
      (let [temp__5823__auto__ (first
                                 (keep-indexed
                                   (fn fn__16859 ([idx item] (when (map? item) idx)))
                                   (:pull query)))]
        (if temp__5823__auto__
          (let [idx temp__5823__auto__]
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
      (let [vec__16865 (qs/parse-as query)
            query (nth vec__16865 (int 0) nil)
            as (nth vec__16865 (int 1) nil)
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
              ret (if (let [and__5600__auto__ (count ret)]
                        (if (java.lang.Integer/valueOf (int and__5600__auto__))
                          (or (:group qmap) (:with qmap))
                          (java.lang.Integer/valueOf (int and__5600__auto__))))
                    (group-rel group_clause ret)
                    ret)
              ret (sort-for-pull qmap ret)
              pullfn (when (:pull qmap)
                       (let [fv (pull-fv qmap srcs)]
                         (fn fn__16868 ([p1__16863#] (xf-tuple fv p1__16863#)))))
              asfn (when as
                     (fn fn__16870
                       ([p1__16864#]
                         (datomic.query.support.MapOnIndexed.
                           ^clojure.lang.IPersistentVector as
                           ^clojure.lang.Indexed p1__16864#))))
              pfn (when (or pullfn asfn) (comp (or asfn identity) (or pullfn identity)))
              temp__5823__auto__ (:find-bindings qmap)]
          (if temp__5823__auto__
            (let [fb temp__5823__auto__ ret (if pfn (map pfn ret) ret)]
              [(let [G__16872 fb]
                 (case
                   G__16872
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
  (def query*
   (fn query_STAR_
     ([query_map]
       (let [timeout (:timeout query_map)
             qmap (cond-> (mapify-query (:query query_map)) timeout (assoc :timeout [timeout]))]
         (q* qmap (:args query_map))))))
  (reset-meta!
    #'query*
    (assoc {:arglists (clojure.core/list ['query-map]), :column (int 1)} :name 'query* :ns *ns*))
  (def apply-pf
   (fn apply_pf
     ([p__16882]
       (let [vec__16883 p__16882
             result (nth vec__16883 (int 0) nil)
             pf (nth vec__16883 (int 1) nil)]
         (if pf (mapv pf result) result)))))
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
  (def query
   (fn query
     ([p__16888]
       (let [map__16889 p__16888
             map__16889 (if (seq? map__16889)
                          (if (next map__16889)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16889))
                            (if (seq map__16889) (first map__16889) {}))
                          map__16889)
             query_map map__16889
             io_context (get map__16889 :io-context)
             query_stats (get map__16889 :query-stats)
             f (fn f ([] (apply-pf (query* query_map))))
             f (if io_context
                 (fn fn__16892
                   ([]
                     (io-stats/throw-if-ex!
                       (io-stats/with-io-stats f {:io-context io_context, :api :query}))))
                 f)
             f (if query_stats
                 (fn fn__16894 ([] (query-stats/with-query-stats f {:query (:query query_map)})))
                 f)]
         (^clojure.lang.IFn f)))))
  (reset-meta!
    #'query
    (assoc
      {:arglists (clojure.core/list [{:keys ['io-context 'query-stats], :as 'query-map}]),
       :column (int 1)}
      :name
      'query
      :ns
      *ns*))
  (def qseq
   (fn qseq
     ([query_map args] (qseq {:query query_map, :args args}))
     ([p__16897]
       (let [map__16898 p__16897
             map__16898 (if (seq? map__16898)
                          (if (next map__16898)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16898))
                            (if (seq map__16898) (first map__16898) {}))
                          map__16898)
             query_map map__16898
             offset (get map__16898 :offset 0)
             limit (get map__16898 :limit (long java.lang.Long/MAX_VALUE))
             io_context (get map__16898 :io-context)
             f (fn f ([] (query* query_map)))
             map__16899 (when io_context
                          (io-stats/throw-if-ex!
                            (io-stats/with-io-stats f {:io-context io_context, :api :qseq})))
             map__16899 (if (seq? map__16899)
                          (if (next map__16899)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16899))
                            (if (seq map__16899) (first map__16899) {}))
                          map__16899)
             ret (get map__16899 :ret)
             io_stats (get map__16899 :io-stats)
             ret (if io_context ret (^clojure.lang.IFn f))
             vec__16900 ret
             result (nth vec__16900 (int 0) nil)
             pf (nth vec__16900 (int 1) nil)
             xform (common/result-xform offset limit pf)
             cseq (qs/counted-seq
                    (sequence xform result)
                    (common/result-count offset limit result))]
         (if io_context {:ret cseq, :io-stats io_stats} cseq)))))
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
  (defn construct-fn ([qform] (fn fn__16907 ([& args] (q qform args)))))
  (reset-meta!
    #'construct-fn
    (assoc {:arglists (clojure.core/list ['qform]), :column (int 1)} :name 'construct-fn :ns *ns*)))
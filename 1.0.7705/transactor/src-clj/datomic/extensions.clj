(do
  (clojure.core/in-ns 'datomic.extensions)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.extensions)
    {:doc
     "Datomic-aware functions and predicates available to Datalog expressions. These extensions provide full-text search, transaction-log access, missing and fallback attribute lookup, optimizer-visible constants, tuple bindings, nested queries, Datomic value comparisons, and integer-preserving division."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['/ '< '> '<= '>= 'compare 'eval])
      (clojure.core/require
        ['datomic.common :refer (clojure.core/list 'compare)]
        ['datomic.stats :as 'stats]
        ['datomic.fulltext :as 'ft])
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.impl.Circular)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'datomic.Log)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.db.IDb)))
  (when-not (.equals 'datomic.extensions 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.extensions))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['/ '< '> '<= '>= 'compare 'eval])
        (clojure.core/require
          ['datomic.common :refer (clojure.core/list 'compare)]
          ['datomic.stats :as 'stats]
          ['datomic.fulltext :as 'ft])
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.impl.Circular)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'datomic.Log)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.db.IDb))))
  (set! *warn-on-reflection* true)
  (defn project
    ([xs binds]
      (if (= binds (range (java.lang.Integer/valueOf (int (count binds)))))
        xs
        (let [ret (java.util.HashSet.)]
          (loop [seq_15258 (seq xs) chunk_15259 nil count_15260 0 i_15261 0]
            (if (clojure.core/< i_15261 count_15260)
              (let [x (.nth ^clojure.lang.Indexed chunk_15259 (int i_15261))]
                (let [tos (object-array (java.lang.Integer/valueOf (int (count binds))))]
                  (dotimes [i (count binds)]
                    (aset ^"[Ljava.lang.Object;" tos (int i) (nth x (int (nth binds (int i))))))
                  (.add
                    ^java.util.HashSet ret
                    (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" tos)))
                (recur seq_15258 chunk_15259 count_15260 (inc i_15261)))
              (let [temp__5825__auto__ (seq seq_15258)]
                (when temp__5825__auto__
                  (let [seq_15258 temp__5825__auto__]
                    (if (chunked-seq? seq_15258)
                      (let [c__6090__auto__ (chunk-first seq_15258)]
                        (recur
                          (chunk-rest seq_15258)
                          c__6090__auto__
                          (int (count c__6090__auto__))
                          (int 0)))
                      (let [x (first seq_15258)]
                        (let [tos (object-array (java.lang.Integer/valueOf (int (count binds))))]
                          (dotimes [i (count binds)]
                            (aset
                              ^"[Ljava.lang.Object;" tos
                              (int i)
                              (nth x (int (nth binds (int i))))))
                          (.add
                            ^java.util.HashSet ret
                            (clojure.lang.LazilyPersistentVector/createOwning
                              ^"[Ljava.lang.Object;" tos)))
                        (recur (next seq_15258) nil 0 0))))))))
          ret))))
  (reset-meta!
    #'project
    (assoc
      {:private true, :arglists (clojure.core/list ['xs 'binds]), :column (int 1)}
      :name
      'project
      :ns
      *ns*))
  (defn fulltext
    "Searches a fulltext attribute and returns distinct [entity value transaction score] tuples."
    ([db attr qmap] (project (ft/search db attr qmap) [2 3 4 5])))
  (reset-meta!
    #'fulltext
    (assoc
      {:private true,
       :arglists (clojure.core/list ['db 'attr 'qmap]),
       :doc
       "Searches a fulltext attribute and returns distinct [entity value transaction score] tuples.",
       :column (int 1)}
      :name
      'fulltext
      :ns
      *ns*))
  (defn tx-ids
    "Returns transaction entity IDs from log in the half-open range [start, end)."
    ([log start end]
      (mapv
        (fn fn__15269 ([p1__15268#] (long (datomic.db/make-eid 3 (long (:t p1__15268#))))))
        (.txRange ^datomic.Log log start end))))
  (reset-meta!
    #'tx-ids
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'log {:tag 'Log}) 'start 'end]),
       :doc "Returns transaction entity IDs from log in the half-open range [start, end).",
       :column (int 1)}
      :name
      'tx-ids
      :ns
      *ns*))
  (defn tx-data
    "Returns the datoms recorded for transaction t, or an empty collection when t is absent."
    ([log t] (or (:data (first (.txRange ^datomic.Log log t (inc t)))) [])))
  (reset-meta!
    #'tx-data
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'log {:tag 'Log}) 't]),
       :doc
       "Returns the datoms recorded for transaction t, or an empty collection when t is absent.",
       :column (int 1)}
      :name
      'tx-data
      :ns
      *ns*))
  (defn missing? ([db e attr] (nil? (seq (datomic.db/datoms db :aevt [attr e])))))
  (reset-meta!
    #'missing?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'e 'attr]),
       :doc
       "Returns true when entity e has no current value for attr in db.",
       :column (int 1)}
      :name
      'missing?
      :ns
      *ns*))
  (defn ensure-sv-attrid
    ([db a]
      (let [attrid (datomic.db/require-attrid db a) attr (datomic.db/attribute db attrid)]
        (when (= 36 (.-cardinality ^datomic.db.Attribute attr))
          (throw
            (java.lang.IllegalArgumentException.
              (str "cardinality-many attrs not supported: " a))))
        attrid)))
  (reset-meta!
    #'ensure-sv-attrid
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'a]),
       :column (int 1)}
      :name
      'ensure-sv-attrid
      :ns
      *ns*))
  (defn get-else
    ([db e attr v]
      (when (nil? v)
        (throw (java.lang.IllegalArgumentException. "nil default value not supported")))
      (let [attrid (ensure-sv-attrid db attr)
            temp__5823__auto__ (first (datomic.db/datoms db :aevt [attrid e]))]
        (if temp__5823__auto__ (let [d temp__5823__auto__] (.v ^datomic.Datom d)) v))))
  (reset-meta!
    #'get-else
    (assoc
      {:arglists (clojure.core/list ['db 'e 'attr 'v]),
       :doc
       "Returns entity e's current value for the cardinality-one attr, or v when the attribute has no value. The default v must be non-nil.",
       :column (int 1)}
      :name
      'get-else
      :ns
      *ns*))
  (defn get-some
    ([db e & attrs]
      (let [G__15281 (seq attrs)
            vec__15282 G__15281
            attr (nth vec__15282 (int 0) nil)
            attrs vec__15282]
        (loop [G__15281 G__15281]
          (let [vec__15285 G__15281 attr (nth vec__15285 (int 0) nil) attrs vec__15285]
            (when attrs
              (let [attrid (ensure-sv-attrid db attr)
                    temp__5823__auto__ (first (datomic.db/datoms db :aevt [attrid e]))]
                (if temp__5823__auto__
                  (let [d temp__5823__auto__] [attrid (.v ^datomic.Datom d)])
                  (recur (next attrs))))))))))
  (reset-meta!
    #'get-some
    (assoc
      {:arglists (clojure.core/list ['db 'e '& 'attrs]),
       :doc
       "Examines the cardinality-one attrs in order and returns [attribute-id value] for the first attribute that entity e possesses, or nil when none has a value.",
       :column (int 1)}
      :name
      'get-some
      :ns
      *ns*))
  (defn ground ([x] x))
  (reset-meta!
    #'ground
    (assoc
      {:arglists (clojure.core/list ['x]),
       :doc
       "Returns the constant x unchanged. Datalog query analysis recognizes ground as a constant binding and can use it when optimizing clause execution.",
       :column (int 1)}
      :name
      'ground
      :ns
      *ns*))
  (defn -gather
    "Returns the cardinality-one values of attrs for entity e in order, or nil when any value is missing."
    ([db e & attrs]
      (let [cnt (count attrs)
            eid (datomic.db/resolve-id db e)
            arr (object-array (java.lang.Integer/valueOf (int cnt)))
            row (loop [i 0 attrs (seq attrs)]
                  (if (= i cnt)
                    (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" arr)
                    (let [attrid (ensure-sv-attrid db (first attrs))
                          temp__5823__auto__ (datomic.db/windowed
                                               db
                                               (fn fn__15293
                                                 ([p1__15291#]
                                                   (and
                                                     (=
                                                       eid
                                                       (long
                                                         (.getE
                                                           ^datomic.impl.db.IDatum p1__15291#)))
                                                     (=
                                                       attrid
                                                       (long
                                                         (.getA
                                                           ^datomic.impl.db.IDatum p1__15291#))))))
                                               (.seekEAVT
                                                 ^datomic.db.IDb db
                                                 (datomic.db/datum db :e eid :a attrid)))]
                      (when temp__5823__auto__
                        (let [iter temp__5823__auto__]
                          (aset
                            ^"[Ljava.lang.Object;" arr
                            (int i)
                            (.v (.get ^datomic.iter.Iter iter)))
                          (recur (inc i) (next attrs)))))))]
        row)))
  (reset-meta!
    #'-gather
    (assoc
      {:arglists (clojure.core/list ['db 'e '& 'attrs]),
       :doc
       "Returns the cardinality-one values of attrs for entity e in order, or nil when any value is missing.",
       :column (int 1)}
      :name
      '-gather
      :ns
      *ns*))
  (defn / ([a b] (if (and (integer? a) (integer? b)) (quot a b) (clojure.core// a b))))
  (reset-meta!
    #'/
    (assoc
      {:arglists (clojure.core/list ['a 'b]),
       :doc
       "Divides a by b. Integer arguments use quotient division so query results do not introduce ratio values; other numeric arguments use Clojure division.",
       :column (int 1)}
      :name
      '/
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.extensions" "!=")
    {:arglists (clojure.core/list ['a 'b]),
     :doc "Returns true when a and b are unequal.",
     :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "!=") not=)
  (defn < ([a b] (neg? (datomic.common/compare a b))))
  (reset-meta!
    #'<
    (assoc
      {:arglists (clojure.core/list ['a 'b]),
       :doc "Returns true when a sorts before b under Datomic value ordering.",
       :column (int 1)}
      :name
      '<
      :ns
      *ns*))
  (defn > ([a b] (< b a)))
  (reset-meta!
    #'>
    (assoc
      {:arglists (clojure.core/list ['a 'b]),
       :doc "Returns true when a sorts after b under Datomic value ordering.",
       :column (int 1)}
      :name
      '>
      :ns
      *ns*))
  (defn <= ([a b] (not (< b a))))
  (reset-meta!
    #'<=
    (assoc
      {:arglists (clojure.core/list ['a 'b]),
       :doc "Returns true when a sorts before or equal to b under Datomic value ordering.",
       :column (int 1)}
      :name
      '<=
      :ns
      *ns*))
  (defn >= ([a b] (not (< a b))))
  (reset-meta!
    #'>=
    (assoc
      {:arglists (clojure.core/list ['a 'b]),
       :doc "Returns true when a sorts after or equal to b under Datomic value ordering.",
       :column (int 1)}
      :name
      '>=
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.extensions" "tuple")
    {:arglists (clojure.core/list ['& 'values]),
     :doc "Constructs a tuple containing the supplied query values in order.",
     :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "tuple") vector)
  (.setMeta
    (clojure.lang.RT/var "datomic.extensions" "untuple")
    {:arglists (clojure.core/list ['tuple]),
     :doc
     "Returns tuple unchanged so a Datalog tuple binding can name its elements.",
     :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "untuple") identity)
  (defn q ([query & srcs] (Circular/q query srcs)))
  (reset-meta!
    #'q
    (assoc
      {:arglists (clojure.core/list ['query '& 'srcs]),
       :doc
       "Executes query as a nested Datalog query against srcs. Query forms, source arguments, and result shapes follow the variable-arity query API.",
       :column (int 1)}
      :name
      'q
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.extensions" "db-attr-splits") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "db-attr-splits") stats/db-attr-splits))

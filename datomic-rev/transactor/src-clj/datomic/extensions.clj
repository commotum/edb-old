(do
  (clojure.core/in-ns 'datomic.extensions)
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
          (loop [seq_14129 (seq xs) chunk_14130 nil count_14131 0 i_14132 0]
            (if (clojure.core/< i_14132 count_14131)
              (let [x (.nth ^clojure.lang.Indexed chunk_14130 (int i_14132))]
                (let [tos (object-array (java.lang.Integer/valueOf (int (count binds))))]
                  (dotimes [i (count binds)]
                    (aset ^"[Ljava.lang.Object;" tos (int i) (nth x (int (nth binds (int i))))))
                  (.add
                    ^java.util.HashSet ret
                    (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" tos)))
                (recur seq_14129 chunk_14130 count_14131 (inc i_14132)))
              (let [temp__5804__auto__ (seq seq_14129)]
                (when temp__5804__auto__
                  (let [seq_14129 temp__5804__auto__]
                    (if (chunked-seq? seq_14129)
                      (let [c__6065__auto__ (chunk-first seq_14129)]
                        (recur
                          (chunk-rest seq_14129)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [x (first seq_14129)]
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
                        (recur (next seq_14129) nil 0 0))))))))
          ret))))
  (reset-meta!
    #'project
    (assoc
      {:private true, :arglists (clojure.core/list ['xs 'binds]), :column (int 1)}
      :name
      'project
      :ns
      *ns*))
  (defn fulltext ([db attr qmap] (project (ft/search db attr qmap) [2 3 4 5])))
  (reset-meta!
    #'fulltext
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'attr 'qmap]), :column (int 1)}
      :name
      'fulltext
      :ns
      *ns*))
  (def tx-ids
   (fn tx_ids
     ([log start end]
       (mapv
         (fn fn__14140 ([p1__14139#] (long (datomic.db/make-eid 3 (long (:t p1__14139#))))))
         (.txRange ^datomic.Log log start end)))))
  (reset-meta!
    #'tx-ids
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'log {:tag 'Log}) 'start 'end]), :column (int 1)}
      :name
      'tx-ids
      :ns
      *ns*))
  (def tx-data
   (fn tx_data ([log t] (or (:data (first (.txRange ^datomic.Log log t (inc t)))) []))))
  (reset-meta!
    #'tx-data
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'log {:tag 'Log}) 't]), :column (int 1)}
      :name
      'tx-data
      :ns
      *ns*))
  (def missing?
   (fn missing_QMARK_ ([db e attr] (nil? (seq (datomic.db/datoms db :aevt [attr e]))))))
  (reset-meta!
    #'missing?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'e 'attr]), :column (int 1)}
      :name
      'missing?
      :ns
      *ns*))
  (def ensure-sv-attrid
   (fn ensure_sv_attrid
     ([db a]
       (let [attrid (datomic.db/require-attrid db a) attr (datomic.db/attribute db attrid)]
         (when (= 36 (.-cardinality ^datomic.db.Attribute attr))
           (throw
             (java.lang.IllegalArgumentException.
               (str "cardinality-many attrs not supported: " a))))
         attrid))))
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
            temp__5802__auto__ (first (datomic.db/datoms db :aevt [attrid e]))]
        (if temp__5802__auto__ (let [d temp__5802__auto__] (.v ^datomic.Datom d)) v))))
  (reset-meta!
    #'get-else
    (assoc
      {:arglists (clojure.core/list ['db 'e 'attr 'v]), :column (int 1)}
      :name
      'get-else
      :ns
      *ns*))
  (defn get-some
    ([db e & attrs]
      (let [G__14152 (seq attrs)
            vec__14153 G__14152
            attr (nth vec__14153 (int 0) nil)
            attrs vec__14153]
        (loop [G__14152 G__14152]
          (let [vec__14156 G__14152 attr (nth vec__14156 (int 0) nil) attrs vec__14156]
            (when attrs
              (let [attrid (ensure-sv-attrid db attr)
                    temp__5802__auto__ (first (datomic.db/datoms db :aevt [attrid e]))]
                (if temp__5802__auto__
                  (let [d temp__5802__auto__] [attrid (.v ^datomic.Datom d)])
                  (recur (next attrs))))))))))
  (reset-meta!
    #'get-some
    (assoc
      {:arglists (clojure.core/list ['db 'e '& 'attrs]), :column (int 1)}
      :name
      'get-some
      :ns
      *ns*))
  (defn ground ([x] x))
  (reset-meta!
    #'ground
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'ground :ns *ns*))
  (defn -gather
    ([db e & attrs]
      (let [cnt (count attrs)
            eid (datomic.db/resolve-id db e)
            arr (object-array (java.lang.Integer/valueOf (int cnt)))
            row (loop [i 0 attrs (seq attrs)]
                  (if (= i cnt)
                    (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" arr)
                    (let [attrid (ensure-sv-attrid db (first attrs))
                          temp__5802__auto__ (datomic.db/windowed
                                               db
                                               (fn fn__14164
                                                 ([p1__14162#]
                                                   (and
                                                     (=
                                                       eid
                                                       (long
                                                         (.getE
                                                           ^datomic.impl.db.IDatum p1__14162#)))
                                                     (=
                                                       attrid
                                                       (long
                                                         (.getA
                                                           ^datomic.impl.db.IDatum p1__14162#))))))
                                               (.seekEAVT
                                                 ^datomic.db.IDb db
                                                 (datomic.db/datum db :e eid :a attrid)))]
                      (when temp__5802__auto__
                        (let [iter temp__5802__auto__]
                          (aset
                            ^"[Ljava.lang.Object;" arr
                            (int i)
                            (.v (.get ^datomic.iter.Iter iter)))
                          (recur (inc i) (next attrs)))))))]
        row)))
  (reset-meta!
    #'-gather
    (assoc
      {:arglists (clojure.core/list ['db 'e '& 'attrs]), :column (int 1)}
      :name
      '-gather
      :ns
      *ns*))
  (defn / ([a b] (if (and (integer? a) (integer? b)) (quot a b) (clojure.core// a b))))
  (reset-meta!
    #'/
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '/ :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.extensions" "!=") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "!=") not=)
  (defn < ([a b] (neg? (datomic.common/compare a b))))
  (reset-meta!
    #'<
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '< :ns *ns*))
  (defn > ([a b] (< b a)))
  (reset-meta!
    #'>
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '> :ns *ns*))
  (defn <= ([a b] (not (< b a))))
  (reset-meta!
    #'<=
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '<= :ns *ns*))
  (defn >= ([a b] (not (< a b))))
  (reset-meta!
    #'>=
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '>= :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.extensions" "tuple") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "tuple") vector)
  (.setMeta (clojure.lang.RT/var "datomic.extensions" "untuple") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "untuple") identity)
  (defn q ([query & srcs] (Circular/q query srcs)))
  (reset-meta!
    #'q
    (assoc {:arglists (clojure.core/list ['query '& 'srcs]), :column (int 1)} :name 'q :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.extensions" "db-attr-splits") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.extensions" "db-attr-splits") stats/db-attr-splits))
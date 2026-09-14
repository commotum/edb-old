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
          (loop [seq_17995 (seq xs) chunk_17996 nil count_17997 0 i_17998 0]
            (if (clojure.core/< i_17998 count_17997)
              (let [x (.nth ^clojure.lang.Indexed chunk_17996 (int i_17998))]
                (let [tos (object-array (java.lang.Integer/valueOf (int (count binds))))]
                  (dotimes [i (count binds)]
                    (aset ^"[Ljava.lang.Object;" tos (int i) (nth x (int (nth binds (int i))))))
                  (.add
                    ^java.util.HashSet ret
                    (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" tos)))
                (recur seq_17995 chunk_17996 count_17997 (inc i_17998)))
              (let [temp__5457__auto__ (seq seq_17995)]
                (when temp__5457__auto__
                  (let [seq_17995 temp__5457__auto__]
                    (if (chunked-seq? seq_17995)
                      (let [c__5719__auto__ (chunk-first seq_17995)]
                        (recur
                          (chunk-rest seq_17995)
                          c__5719__auto__
                          (int (count c__5719__auto__))
                          (int 0)))
                      (let [x (first seq_17995)]
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
                        (recur (next seq_17995) nil 0 0))))))))
          ret))))
  (reset-meta!
    #'project
    (assoc
      {:private true, :arglists (clojure.core/list ['xs 'binds]), :column 1}
      :name
      'project
      :ns
      *ns*))
  (defn fulltext ([db attr qmap] (project (ft/search db attr qmap) [2 3 4 5])))
  (reset-meta!
    #'fulltext
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'attr 'qmap]), :column 1}
      :name
      'fulltext
      :ns
      *ns*))
  (defn tx-ids
    ([log start end]
      (mapv
        (fn fn__18006 ([p1__18005#] (long (datomic.db/make-eid 3 (long (:t p1__18005#))))))
        (.txRange ^datomic.Log log start end))))
  (defn tx-data ([log t] (or (:data (first (.txRange ^datomic.Log log t (inc t)))) [])))
  (defn missing? ([db e attr] (nil? (seq (datomic.db/datoms db :aevt [attr e])))))
  (defn ensure-sv-attrid
    ([db a]
      (let [attrid (datomic.db/require-attrid db a) attr (datomic.db/attribute db attrid)]
        (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
          (do
            (throw
              (java.lang.IllegalArgumentException.
                (str "cardinality-many attrs not supported: " a)))
            nil)
          attrid))))
  (reset-meta!
    #'ensure-sv-attrid
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'a]),
       :column 1}
      :name
      'ensure-sv-attrid
      :ns
      *ns*))
  (defn get-else
    ([db e attr v]
      (if (nil? v)
        (do (throw (java.lang.IllegalArgumentException. "nil default value not supported")) nil)
        (let [attrid (ensure-sv-attrid db attr)
              temp__5455__auto__ (first (datomic.db/datoms db :aevt [attrid e]))]
          (if temp__5455__auto__ (let [d temp__5455__auto__] (.v ^datomic.Datom d)) v)))))
  (defn get-some
    ([db e & attrs]
      (let [G__18018 (seq attrs)
            vec__18019 G__18018
            attr (nth vec__18019 (int 0) nil)
            attrs vec__18019]
        (loop [G__18018 G__18018]
          (let [vec__18022 G__18018 attr (nth vec__18022 (int 0) nil) attrs vec__18022]
            (when attrs
              (let [attrid (ensure-sv-attrid db attr)
                    temp__5455__auto__ (first (datomic.db/datoms db :aevt [attrid e]))]
                (if temp__5455__auto__
                  (let [d temp__5455__auto__] [attrid (.v ^datomic.Datom d)])
                  (recur (next attrs))))))))))
  (defn ground ([x] x))
  (defn -gather
    ([db e & attrs]
      (let [cnt (count attrs)
            eid (datomic.db/resolve-id db e)
            arr (object-array (java.lang.Integer/valueOf (int cnt)))
            row (loop [i 0 attrs (seq attrs)]
                  (if (= i cnt)
                    (clojure.lang.LazilyPersistentVector/createOwning ^"[Ljava.lang.Object;" arr)
                    (let [attrid (ensure-sv-attrid db (first attrs))
                          temp__5455__auto__ (datomic.db/windowed
                                               db
                                               (fn fn__18030
                                                 ([p1__18028#]
                                                   (and
                                                     (=
                                                       eid
                                                       (long
                                                         (.getE
                                                           ^datomic.impl.db.IDatum p1__18028#)))
                                                     (=
                                                       attrid
                                                       (long
                                                         (.getA
                                                           ^datomic.impl.db.IDatum p1__18028#))))))
                                               (.seekEAVT
                                                 ^datomic.db.IDb db
                                                 (datomic.db/datum db :e eid :a attrid)))]
                      (when temp__5455__auto__
                        (let [iter temp__5455__auto__]
                          (aset
                            ^"[Ljava.lang.Object;" arr
                            (int i)
                            (.v (.get ^datomic.iter.Iter iter)))
                          (recur (inc i) (next attrs)))))))]
        row)))
  (defn / ([a b] (if (and (integer? a) (integer? b)) (quot a b) (clojure.core// a b))))
  (def != not=)
  (defn < ([a b] (neg? (datomic.common/compare a b))))
  (defn > ([a b] (< b a)))
  (defn <= ([a b] (not (< b a))))
  (defn >= ([a b] (not (< a b))))
  (def tuple vector)
  (def untuple identity)
  (defn q ([query & srcs] (Circular/q query srcs)))
  (def db-attr-splits stats/db-attr-splits))
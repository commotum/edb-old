(do
  (clojure.core/in-ns 'datomic.builtins)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.builtins)
    {:doc
     "Built-in transaction functions. Expands entity retraction and compare-and-swap requests into ordinary transaction data evaluated against db-before."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/require
        ['clojure.set :as 'set]
        ['datomic.db :as 'db]
        ['datomic.error :as 'error]
        ['datomic.query :as 'query])))
  (when-not (.equals 'datomic.builtins 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.builtins))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/require
          ['clojure.set :as 'set]
          ['datomic.db :as 'db]
          ['datomic.error :as 'error]
          ['datomic.query :as 'query]))))
  (set! *warn-on-reflection* true)
  (defn component-attr?
    "Returns true when the attribute is a reference whose values are owned component entities."
    ([db a]
      (let [attr (.elementAt ^datomic.db.IDbImpl db a)]
        (and
          (.-isComponent ^datomic.db.Attribute attr)
          (= 20 (.-vtypeid ^datomic.db.Attribute attr))))))
  (reset-meta!
    #'component-attr?
    (assoc
      {:arglists (clojure.core/list ['db 'a]),
       :doc
       "Returns true when the attribute is a reference whose values are owned component entities.",
       :column (int 1)}
      :name
      'component-attr?
      :ns
      *ns*))
  (defn component-es-set
    "Returns the entity and every component entity reachable recursively from it. When via-attrs is supplied, the first traversal is limited to those component attributes."
    ([db e via-attrs]
      (when e
        (let [es #{e}
              G__23241 [e]
              vec__23242 G__23241
              seq__23243 (seq vec__23242)
              first__23244 (first seq__23243)
              seq__23243 (next seq__23243)
              check first__23244
              more seq__23243
              via via-attrs]
          (loop [es es G__23241 G__23241 via via]
            (let [es es
                  vec__23245 G__23241
                  seq__23246 (seq vec__23245)
                  first__23247 (first seq__23246)
                  seq__23246 (next seq__23246)
                  check first__23247
                  more seq__23246
                  via via]
              (if check
                (let [comps (reduce
                              (fn fn__23248
                                ([s d]
                                  (if (and
                                        (or
                                          (empty? via)
                                          (contains?
                                            via
                                            (java.lang.Integer/valueOf
                                              (int (.getA ^datomic.impl.db.IDatum d)))))
                                        (component-attr?
                                          db
                                          (java.lang.Integer/valueOf
                                            (int (.getA ^datomic.impl.db.IDatum d)))))
                                    (conj s (.getV ^datomic.impl.db.IDatum d))
                                    s)))
                              #{}
                              (db/datoms db :eavt [check]))]
                  (recur (into es comps) (into more (set/difference comps es)) nil))
                es))))))
    ([db e] (component-es-set db e nil)))
  (reset-meta!
    #'component-es-set
    (assoc
      {:arglists (clojure.core/list ['db 'e] ['db 'e 'via-attrs]),
       :doc
       "Returns the entity and every component entity reachable recursively from it. When via-attrs is supplied, the first traversal is limited to those component attributes.",
       :column (int 1)}
      :name
      'component-es-set
      :ns
      *ns*))
  (defn build-retract-args
    "Expands :db/retractEntity for an entity identifier. Returns retractions for the entity, its recursively owned components, and references from other entities to every retracted entity."
    ([db e]
      (let [temp__5804__auto__ (db/resolve-id db e)]
        (when temp__5804__auto__
          (let [e temp__5804__auto__ retract (db/resolve-id db :db/retract)]
            (persistent!
              (reduce
                (fn fn__23253
                  ([result e]
                    (let [result (reduce
                                   (fn fn__23255
                                     ([result p__23254]
                                       (let [map__23256 p__23254
                                             map__23256 (if (seq? map__23256)
                                                          (if (next map__23256)
                                                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                              (to-array map__23256))
                                                            (if
                                                              (seq map__23256)
                                                              (first map__23256)
                                                              {}))
                                                          map__23256)
                                             e (get map__23256 :e)
                                             a (get map__23256 :a)
                                             v (get map__23256 :v)]
                                         (if (component-attr? db a)
                                           result
                                           (conj! result [retract e a v])))))
                                   result
                                   (db/datoms db :eavt [e]))]
                      (reduce
                        (fn fn__23259
                          ([result p__23258]
                            (let [map__23260 p__23258
                                  map__23260 (if (seq? map__23260)
                                               (if (next map__23260)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__23260))
                                                 (if (seq map__23260) (first map__23260) {}))
                                               map__23260)
                                  e (get map__23260 :e)
                                  a (get map__23260 :a)
                                  v (get map__23260 :v)]
                              (conj! result [retract e a v]))))
                        result
                        (db/datoms db :vaet [e])))))
                (transient [])
                (component-es-set db e))))))))
  (reset-meta!
    #'build-retract-args
    (assoc
      {:arglists (clojure.core/list ['db 'e]),
       :doc
       "Expands :db/retractEntity for an entity identifier. Returns retractions for the entity, its recursively owned components, and references from other entities to every retracted entity.",
       :column (int 1)}
      :name
      'build-retract-args
      :ns
      *ns*))
  (defn compare-and-swap
    "Expands :db/cas for a cardinality-one attribute when its value in db-before equals v-old. A nil v-old requires the attribute to be absent. Throws a conflict when the observed value differs and rejects nil new values or cardinality-many attributes."
    ([db e a v-old v-new]
      (when-not (and e a (not (nil? v-new)))
        (error/arg
          :db.error/invalid-cas
          "entity, attribute, and new-value must be specified"
          {:datomic/cancelled true, :e e, :a a, :v-old v-old, :v-new v-new}))
      (when (= 36 (.-cardinality (db/attribute db (db/require-attrid db a))))
        (error/arg
          :db.error/invalid-cas-many
          "attribute must be cardinality-one"
          {:datomic/cancelled true, :e e, :a a, :v-old v-old, :v-new v-new}))
      (let [v-cur (:v (first (db/datoms db :eavt [e a])))]
        (if (= v-cur v-old)
          [[:db/add e a v-new]]
          (error/state
            :db.error/cas-failed
            (str "Compare failed: " v-old " " v-cur)
            {:datomic/cancelled true, :e e, :a a, :v-old v-old, :v v-cur})))))
  (reset-meta!
    #'compare-and-swap
    (assoc
      {:arglists (clojure.core/list ['db 'e 'a 'v-old 'v-new]),
       :doc
       "Expands :db/cas for a cardinality-one attribute when its value in db-before equals v-old. A nil v-old requires the attribute to be absent. Throws a conflict when the observed value differs and rejects nil new values or cardinality-many attributes.",
       :column (int 1)}
      :name
      'compare-and-swap
      :ns
      *ns*)))

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
              G__21713 [e]
              vec__21714 G__21713
              seq__21715 (seq vec__21714)
              first__21716 (first seq__21715)
              seq__21715 (next seq__21715)
              check first__21716
              more seq__21715
              via via-attrs]
          (loop [es es G__21713 G__21713 via via]
            (let [es es
                  vec__21717 G__21713
                  seq__21718 (seq vec__21717)
                  first__21719 (first seq__21718)
                  seq__21718 (next seq__21718)
                  check first__21719
                  more seq__21718
                  via via]
              (if check
                (let [comps (reduce
                              (fn fn__21720
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
      (let [temp__5825__auto__ (db/resolve-id db e)]
        (when temp__5825__auto__
          (let [e temp__5825__auto__ retract (db/resolve-id db :db/retract)]
            (persistent!
              (reduce
                (fn fn__21725
                  ([result e]
                    (let [result (reduce
                                   (fn fn__21727
                                     ([result p__21726]
                                       (let [map__21728 p__21726
                                             map__21728 (if (seq? map__21728)
                                                          (if (next map__21728)
                                                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                              (to-array map__21728))
                                                            (if
                                                              (seq map__21728)
                                                              (first map__21728)
                                                              {}))
                                                          map__21728)
                                             e (get map__21728 :e)
                                             a (get map__21728 :a)
                                             v (get map__21728 :v)]
                                         (if (component-attr? db a)
                                           result
                                           (conj! result [retract e a v])))))
                                   result
                                   (db/datoms db :eavt [e]))]
                      (reduce
                        (fn fn__21731
                          ([result p__21730]
                            (let [map__21732 p__21730
                                  map__21732 (if (seq? map__21732)
                                               (if (next map__21732)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__21732))
                                                 (if (seq map__21732) (first map__21732) {}))
                                               map__21732)
                                  e (get map__21732 :e)
                                  a (get map__21732 :a)
                                  v (get map__21732 :v)]
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

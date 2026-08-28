(do
  (clojure.core/in-ns 'datomic.builtins)
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
    ([db a]
      (let [attr (.elementAt ^datomic.db.IDbImpl db a)]
        (and
          (.-isComponent ^datomic.db.Attribute attr)
          (= 20 (.-vtypeid ^datomic.db.Attribute attr))))))
  (defn component-es-set
    ([db e via_attrs]
      (when e
        (let [es #{e}
              G__29913 [e]
              vec__29914 G__29913
              seq__29915 (seq vec__29914)
              first__29916 (first seq__29915)
              seq__29915 (next seq__29915)
              check first__29916
              more seq__29915
              via via_attrs]
          (loop [es es G__29913 G__29913 via via]
            (let [es es
                  vec__29917 G__29913
                  seq__29918 (seq vec__29917)
                  first__29919 (first seq__29918)
                  seq__29918 (next seq__29918)
                  check first__29919
                  more seq__29918
                  via via]
              (if check
                (let [comps (reduce
                              (fn fn__29920
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
                es))))
        nil))
    ([db e] (component-es-set db e nil)))
  (defn build-retract-args
    ([db e]
      (let [temp__5804__auto__ (db/resolve-id db e)]
        (when temp__5804__auto__
          (let [e temp__5804__auto__ retract (db/resolve-id db :db/retract)]
            (persistent!
              (reduce
                (fn fn__29925
                  ([result e]
                    (let [result (reduce
                                   (fn fn__29927
                                     ([result p__29926]
                                       (let [map__29928 p__29926
                                             map__29928 (if (seq? map__29928)
                                                          (if (next map__29928)
                                                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                              (to-array map__29928))
                                                            (if
                                                              (seq map__29928)
                                                              (first map__29928)
                                                              {}))
                                                          map__29928)
                                             e (get map__29928 :e)
                                             a (get map__29928 :a)
                                             v (get map__29928 :v)]
                                         (if (component-attr? db a)
                                           result
                                           (conj! result [retract e a v])))))
                                   result
                                   (db/datoms db :eavt [e]))]
                      (reduce
                        (fn fn__29931
                          ([result p__29930]
                            (let [map__29932 p__29930
                                  map__29932 (if (seq? map__29932)
                                               (if (next map__29932)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__29932))
                                                 (if (seq map__29932) (first map__29932) {}))
                                               map__29932)
                                  e (get map__29932 :e)
                                  a (get map__29932 :a)
                                  v (get map__29932 :v)]
                              (conj! result [retract e a v]))))
                        result
                        (db/datoms db :vaet [e])))))
                (transient [])
                (component-es-set db e))))))))
  (defn compare-and-swap
    ([db e a v_old v_new]
      (when-not (and e a (not (nil? v_new)))
        (error/arg
          :db.error/invalid-cas
          "entity, attribute, and new-value must be specified"
          {:datomic/cancelled true, :e e, :a a, :v-old v_old, :v-new v_new}))
      (when (= 36 (.-cardinality (db/attribute db (db/require-attrid db a))))
        (error/arg
          :db.error/invalid-cas-many
          "attribute must be cardinality-one"
          {:datomic/cancelled true, :e e, :a a, :v-old v_old, :v-new v_new}))
      (let [v_cur (:v (first (db/datoms db :eavt [e a])))]
        (if (= v_cur v_old)
          [[:db/add e a v_new]]
          (error/state
            :db.error/cas-failed
            (str "Compare failed: " v_old " " v_cur)
            {:datomic/cancelled true, :e e, :a a, :v-old v_old, :v v_cur}))))))
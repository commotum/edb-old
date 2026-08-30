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
              G__23388 [e]
              vec__23389 G__23388
              seq__23390 (seq vec__23389)
              first__23391 (first seq__23390)
              seq__23390 (next seq__23390)
              check first__23391
              more seq__23390
              via via_attrs]
          (loop [es es G__23388 G__23388 via via]
            (let [es es
                  vec__23392 G__23388
                  seq__23393 (seq vec__23392)
                  first__23394 (first seq__23393)
                  seq__23393 (next seq__23393)
                  check first__23394
                  more seq__23393
                  via via]
              (if check
                (let [comps (reduce
                              (fn fn__23395
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
      (let [temp__5457__auto__ (db/resolve-id db e)]
        (when temp__5457__auto__
          (let [e temp__5457__auto__ retract (db/resolve-id db :db/retract)]
            (persistent!
              (reduce
                (fn fn__23400
                  ([result e]
                    (let [result (reduce
                                   (fn fn__23402
                                     ([result p__23401]
                                       (let [map__23403 p__23401
                                             map__23403 (if (seq? map__23403)
                                                          (clojure.lang.PersistentHashMap/create
                                                            (seq map__23403))
                                                          map__23403)
                                             e (get map__23403 :e)
                                             a (get map__23403 :a)
                                             v (get map__23403 :v)]
                                         (if (component-attr? db a)
                                           result
                                           (conj! result [retract e a v])))))
                                   result
                                   (db/datoms db :eavt [e]))]
                      (reduce
                        (fn fn__23406
                          ([result p__23405]
                            (let [map__23407 p__23405
                                  map__23407 (if (seq? map__23407)
                                               (clojure.lang.PersistentHashMap/create
                                                 (seq map__23407))
                                               map__23407)
                                  e (get map__23407 :e)
                                  a (get map__23407 :a)
                                  v (get map__23407 :v)]
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
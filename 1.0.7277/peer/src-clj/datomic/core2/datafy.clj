(do
  (clojure.core/in-ns 'datomic.core2.datafy)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.lang.reflect.Field)
      (clojure.core/import 'java.lang.reflect.Modifier)))
  (when-not (.equals 'datomic.core2.datafy 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.datafy))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.lang.reflect.Field)
        (clojure.core/import 'java.lang.reflect.Modifier))))
  (defn -datafy-declared-fields-fn
    ([clsym]
      (let [cls (resolve clsym)
            _ (when-not (class? cls)
                (throw (java.lang.IllegalArgumentException. (str "Not a class: [" clsym "]")))
                nil)
            field_exprs (map
                          (fn fn__20545
                            ([f]
                              (let [name (.getName ^java.lang.reflect.Field f)]
                                [(keyword name) (clojure.core/list '. 'this (symbol name))])))
                          (filter
                            (fn fn__20547
                              ([f]
                                (zero?
                                  (bit-and
                                    Modifier/STATIC
                                    (.getModifiers ^java.lang.reflect.Field f)))))
                            (.getDeclaredFields cls)))]
        (clojure.core/list
          'extend-protocol
          'clojure.core.protocols/Datafiable
          (symbol (.getName cls))
          (clojure.core/list 'datafy ['this] (into {} field_exprs))))))
  (defn datafy-declared-fields
    ([&form &env & classes]
      (seq (concat (clojure.core/list 'do) (map -datafy-declared-fields-fn classes)))))
  (.setMacro #'datafy-declared-fields)
  (defn val-navs
    ([k_>f]
      (fn fn__20551
        ([_ k v]
          (let [temp__5802__auto__ (^clojure.lang.IFn k_>f k)]
            (if temp__5802__auto__ (let [f temp__5802__auto__] (^clojure.lang.IFn f v)) v))))))
  (defn with-nav ([x nav_fn] (when x (vary-meta x assoc 'clojure.core.protocols/nav nav_fn)))))
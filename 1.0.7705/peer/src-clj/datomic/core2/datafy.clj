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
                          (fn fn__21402
                            ([f]
                              (let [name (.getName ^java.lang.reflect.Field f)]
                                [(keyword name) (clojure.core/list '. 'this (symbol name))])))
                          (filter
                            (fn fn__21404
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
  (reset-meta!
    #'-datafy-declared-fields-fn
    (assoc
      {:arglists (clojure.core/list ['clsym]), :column (int 1)}
      :name
      '-datafy-declared-fields-fn
      :ns
      *ns*))
  (def datafy-declared-fields
   (fn datafy_declared_fields
     ([&form &env & classes]
       (seq (concat (clojure.core/list 'do) (map -datafy-declared-fields-fn classes))))))
  (reset-meta!
    #'datafy-declared-fields
    (assoc
      {:arglists (clojure.core/list ['& 'classes]), :column (int 1)}
      :name
      'datafy-declared-fields
      :ns
      *ns*))
  (.setMacro #'datafy-declared-fields)
  (def val-navs
   (fn val_navs
     ([k_>f]
       (fn fn__21408
         ([_ k v]
           (let [temp__5823__auto__ (^clojure.lang.IFn k_>f k)]
             (if temp__5823__auto__ (let [f temp__5823__auto__] (^clojure.lang.IFn f v)) v)))))))
  (reset-meta!
    #'val-navs
    (assoc {:arglists (clojure.core/list ['k->f]), :column (int 1)} :name 'val-navs :ns *ns*))
  (def with-nav
   (fn with_nav ([x nav_fn] (when x (vary-meta x assoc 'clojure.core.protocols/nav nav_fn)))))
  (reset-meta!
    #'with-nav
    (assoc {:arglists (clojure.core/list ['x 'nav-fn]), :column (int 1)} :name 'with-nav :ns *ns*)))
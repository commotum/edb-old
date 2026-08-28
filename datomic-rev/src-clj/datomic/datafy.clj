(do
  (clojure.core/in-ns 'datomic.datafy)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.data :as 'data]
        ['clojure.reflect :as 'reflect]
        ['clojure.string :as 'str])
      (clojure.core/import 'java.beans.Introspector)
      (clojure.core/import 'java.beans.PropertyDescriptor)
      (clojure.core/import 'java.lang.reflect.Method)
      (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])))
  (when-not (.equals 'datomic.datafy 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.datafy))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.data :as 'data]
          ['clojure.reflect :as 'reflect]
          ['clojure.string :as 'str])
        (clojure.core/import 'java.beans.Introspector)
        (clojure.core/import 'java.beans.PropertyDescriptor)
        (clojure.core/import 'java.lang.reflect.Method)
        (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)]))))
  (set! *warn-on-reflection* true)
  (defn setters
    ([cls]
      (when-not cls (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'cls)))))
      (filter
        (fn fn__17159
          ([p1__17158#]
            (and
              (= "set" (subs (.getName ^java.lang.reflect.Method p1__17158#) 0 3))
              (= 1 (long (count (.getParameterTypes ^java.lang.reflect.Method p1__17158#)))))))
        (.getMethods ^java.lang.Class cls))))
  (defn setter-name->keyword
    ([n] (let [base (subs n 3)] (keyword (str (.toLowerCase (subs base 0 1)) (subs base 1))))))
  (defn filter-overlapped-setters
    ([coll]
      (reduce
        (fn fn__17166
          ([result p__17165]
            (let [vec__17167 p__17165
                  k (nth vec__17167 (int 0) nil)
                  setters (nth vec__17167 (int 1) nil)
                  G__17170 (count setters)]
              (case
                G__17170
                1
                (conj result (first setters))
                2
                (let [first_string? (=
                                      java.lang.String
                                      (first
                                        (.getParameterTypes
                                          ^java.lang.reflect.Method (first setters))))]
                  (conj result ((if first_string? second first) setters)))))))
        []
        (group-by
          (fn fn__17172 ([p1__17164#] (.getName ^java.lang.reflect.Method p1__17164#)))
          coll))))
  (defn getter?
    ([m]
      (let [name (.getName ^java.lang.reflect.Method m)]
        (and
          (or (= "is" (subs name 0 2)) (= "get" (subs name 0 3)))
          (not= "getClass" name)
          (= 0 (long (count (.getParameterTypes ^java.lang.reflect.Method m))))))))
  (defn getters
    ([cls]
      (when-not cls (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'cls)))))
      (filter getter? (.getMethods ^java.lang.Class cls))))
  (defn type-descriptor-category
    ([c]
      (let [anc (conj (or (ancestors c) #{}) c)]
        (cond
          (contains? anc java.util.Map) :map
          (contains? anc java.util.Collection) :list
          (.isEnum ^java.lang.Class c) :enum
          (.isPrimitive ^java.lang.Class c) :self
          (.startsWith (.getName (.getPackage ^java.lang.Class c)) "java") :self
          :default (do :bean)))))
  (defmulti type-descriptor type-descriptor-category)
  (defmulti
    property-descriptor
    (fn fn__17187 ([container prop type] (type-descriptor-category type))))
  (def map-property-types {})
  (def list-property-types {})
  (defmethod
    property-descriptor
    :map
    fn__17192
    ([container prop type]
      (let [temp__5455__auto__ (map-property-types [container prop])]
        (if temp__5455__auto__
          (let [vec__17193 temp__5455__auto__
                k (nth vec__17193 (int 0) nil)
                v (nth vec__17193 (int 1) nil)]
            {k (type-descriptor v)})
          (type-descriptor type)))))
  (defmethod
    property-descriptor
    :list
    fn__17198
    ([container prop type]
      (let [temp__5455__auto__ (list-property-types [container prop])]
        (if temp__5455__auto__
          (let [v temp__5455__auto__] [(type-descriptor v)])
          (type-descriptor type)))))
  (defmethod property-descriptor :default fn__17201 ([_ _ type] (type-descriptor type)))
  (defmethod
    type-descriptor
    :enum
    fn__17204
    ([c]
      (into
        #{}
        (map
          :name
          (filter
            (fn fn__17205
              ([p1__17203#]
                (and
                  (contains? (:flags p1__17203#) :static)
                  (contains? (:flags p1__17203#) :public)
                  (:type p1__17203#)
                  (= (resolve (:type p1__17203#)) c))))
            (:members (reflect/reflect c)))))))
  (defmethod
    type-descriptor
    :bean
    fn__17211
    ([c]
      (let [temp__5455__auto__ (seq (filter-overlapped-setters (setters c)))]
        (if temp__5455__auto__
          (let [s temp__5455__auto__]
            (reduce
              (fn fn__17212
                ([m meth]
                  (let [k (setter-name->keyword (.getName ^java.lang.reflect.Method meth))]
                    (assoc
                      m
                      k
                      (property-descriptor
                        c
                        k
                        (nth (.getParameterTypes ^java.lang.reflect.Method meth) (int 0)))))))
              {}
              s))
          (symbol (.getName ^java.lang.Class c))))))
  (defmethod type-descriptor :default fn__17216 ([c] (symbol (.getName ^java.lang.Class c))))
  (defonce ObjectToData {})
  (defprotocol ObjectToData (object-to-data [o]))
  (defn object-to-data-wrapper ([x] (object-to-data x)))
  (extend java.lang.Object ObjectToData {:object-to-data (fn fn__17235 ([o] o))})
  (extend
    java.util.ArrayList
    ObjectToData
    {:object-to-data (fn fn__17237 ([o] (mapv object-to-data o)))})
  (extend
    java.util.Map
    ObjectToData
    {:object-to-data
     (fn fn__17239
       ([m]
         (reduce
           (fn fn__17241
             ([m p__17240]
               (let [vec__17242 p__17240
                     k (nth vec__17242 (int 0) nil)
                     v (nth vec__17242 (int 1) nil)]
                 (assoc m (object-to-data k) (object-to-data v)))))
           {}
           m)))})
  (defn vals-to-data
    ([m]
      (persistent!
        (reduce
          (fn fn__17248
            ([m p__17247]
              (let [vec__17249 p__17247
                    k (nth vec__17249 (int 0) nil)
                    v (nth vec__17249 (int 1) nil)]
                (assoc! m k (object-to-data v)))))
          (transient {})
          m))))
  (defmulti data-to-object (fn fn__17255 ([obj type] [(data/equality-partition obj) type])))
  (defmethod
    data-to-object
    [:atom java.lang.Integer]
    fn__17260
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Integer. (int n))))
  (defmethod
    data-to-object
    [:atom java.lang.Integer/TYPE]
    fn__17262
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Integer. (int n))))
  (defmethod
    data-to-object
    [:atom java.lang.Long]
    fn__17264
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Long. (long n))))
  (defmethod
    data-to-object
    [:atom java.lang.Long/TYPE]
    fn__17266
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Long. (long n))))
  (defmethod
    data-to-object
    [:atom java.lang.Boolean]
    fn__17268
    ([n _]
      (when-not (or (true? n) (false? n))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list
                  'or
                  (clojure.core/list 'true? 'n)
                  (clojure.core/list 'false? 'n)))))))
      (java.lang.Boolean. (boolean n))))
  (defmethod
    data-to-object
    [:atom java.lang.Boolean/TYPE]
    fn__17271
    ([n _]
      (when-not (or (true? n) (false? n))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list
                  'or
                  (clojure.core/list 'true? 'n)
                  (clojure.core/list 'false? 'n)))))))
      (java.lang.Boolean. (boolean n))))
  (defmethod
    data-to-object
    :default
    fn__17274
    ([obj type]
      (try
        (cast type obj)
        (catch
          java.lang.ClassCastException
          _
          (do
            (throw
              (java.lang.RuntimeException.
                (str "Could not cast " obj " [" (class obj) "]" " to " type)))
            nil)))))
  (defmulti property-to-object (fn fn__17277 ([container prop obj type] [container prop])))
  (defmethod
    property-to-object
    :default
    fn__17282
    ([_ prop obj type]
      (try
        (data-to-object obj type)
        (catch
          java.lang.Exception
          e
          (do
            (throw
              (java.lang.RuntimeException.
                (str "Could not convert property " prop " value " obj " to type " type)
                ^java.lang.Throwable e))
            nil)))))
  (defn invoke-setter
    ([o m meth]
      (let [wm (.getName ^java.lang.reflect.Method meth)
            kw (setter-name->keyword wm)
            type (first (.getParameterTypes ^java.lang.reflect.Method meth))]
        (seq
          (concat
            (clojure.core/list 'clojure.core/when)
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'clojure.core/contains?)
                  (clojure.core/list m)
                  (clojure.core/list kw))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'clojure.core/let)
                  (clojure.core/list
                    (apply
                      vector
                      (seq
                        (concat
                          (clojure.core/list 'v)
                          (clojure.core/list
                            (seq (concat (clojure.core/list kw) (clojure.core/list m))))
                          (clojure.core/list
                            (with-meta 'k {:tag (symbol (.getName ^java.lang.Class type))}))
                          (clojure.core/list
                            (seq
                              (concat
                                (clojure.core/list 'datomic.datafy/property-to-object)
                                (clojure.core/list
                                  (seq
                                    (concat
                                      (clojure.core/list 'clojure.core/class)
                                      (clojure.core/list o))))
                                (clojure.core/list kw)
                                (clojure.core/list 'v)
                                (clojure.core/list type))))))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list '.)
                        (clojure.core/list o)
                        (clojure.core/list (symbol wm))
                        (clojure.core/list 'k))))))))))))
  (defn invoke-getter
    ([o m meth]
      (let [rm (.getName ^java.lang.reflect.Method meth)
            base (if (= "get" (subs rm 0 3)) (subs rm 3) (subs rm 2))
            kw (keyword (str (.toLowerCase (subs base 0 1)) (subs base 1)))]
        (seq
          (concat
            (clojure.core/list 'clojure.core/when-let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list 'v__17285__auto__)
                    (clojure.core/list
                      (-> (str "." rm)
                       (symbol)
                       (clojure.core/list)
                       (concat (clojure.core/list o))
                       (seq)))))))
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list kw)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.datafy/object-to-data-wrapper)
                          (clojure.core/list 'v__17285__auto__)))))))))))))
  (defn can-data-to-object?
    ([cls]
      (let [default (get-method data-to-object nil)]
        (or
          (not= default (get-method data-to-object [:atom cls]))
          (not= default (get-method data-to-object [:map cls]))
          (str/starts-with? (.getName ^java.lang.Class cls) "java.")))))
  (defn method-input-classes ([m] (into #{} (.getParameterTypes ^java.lang.reflect.Method m))))
  (defn define-data-to-object
    ([bean_class]
      (let [cls (or
                  (resolve bean_class)
                  (do
                    (throw
                      (java.lang.IllegalArgumentException.
                        (str "Unable to resolve symbol as class: " bean_class)))
                    nil))]
        (if (contains? (ancestors cls) java.lang.Enum)
          (seq
            (concat
              (clojure.core/list 'clojure.core/defmethod)
              (clojure.core/list 'datomic.datafy/data-to-object)
              (clojure.core/list
                (apply
                  vector
                  (seq (concat (clojure.core/list :atom) (clojure.core/list bean_class)))))
              (clojure.core/list
                (apply vector (seq (concat (clojure.core/list 'n) (clojure.core/list '_)))))
              (clojure.core/list
                (seq
                  (concat
                    (clojure.core/list '.)
                    (clojure.core/list bean_class)
                    (clojure.core/list 'valueOf)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'clojure.core/name)
                          (clojure.core/list 'n)))))))))
          (let [s (filter-overlapped-setters (setters cls))
                legal_keys (into
                             #{}
                             (map
                               (fn fn__17292
                                 ([p1__17291#]
                                   (setter-name->keyword
                                     (.getName ^java.lang.reflect.Method p1__17291#))))
                               s))]
            (seq
              (concat
                (clojure.core/list 'clojure.core/defmethod)
                (clojure.core/list 'datomic.datafy/data-to-object)
                (clojure.core/list
                  (apply
                    vector
                    (seq (concat (clojure.core/list :map) (clojure.core/list bean_class)))))
                (clojure.core/list
                  (apply vector (seq (concat (clojure.core/list 'm) (clojure.core/list '_)))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/when-let)
                      (clojure.core/list
                        (apply
                          vector
                          (seq
                            (concat
                              (clojure.core/list 'bad-ks)
                              (clojure.core/list
                                (seq
                                  (concat
                                    (clojure.core/list 'clojure.core/seq)
                                    (clojure.core/list
                                      (seq
                                        (concat
                                          (clojure.core/list 'clojure.core/remove)
                                          (clojure.core/list legal_keys)
                                          (clojure.core/list
                                            (seq
                                              (concat
                                                (clojure.core/list 'clojure.core/keys)
                                                (clojure.core/list 'm))))))))))))))
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'throw)
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list 'clojure.core/ex-info)
                                  (clojure.core/list
                                    (seq
                                      (concat
                                        (clojure.core/list 'clojure.core/apply)
                                        (clojure.core/list 'clojure.core/str)
                                        (clojure.core/list "Unexpected keys ")
                                        (clojure.core/list 'bad-ks))))
                                  (clojure.core/list
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :keys)
                                          (clojure.core/list 'bad-ks)
                                          (clojure.core/list :legal-keys)
                                          (clojure.core/list legal_keys)
                                          (clojure.core/list :constructor)
                                          (clojure.core/list bean_class))))))))))))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/let)
                      (clojure.core/list
                        (apply
                          vector
                          (seq
                            (concat
                              (clojure.core/list 'o)
                              (clojure.core/list
                                (seq
                                  (concat
                                    (clojure.core/list 'new)
                                    (clojure.core/list bean_class))))))))
                      (mapv (partial invoke-setter 'o 'm) s)
                      (clojure.core/list 'o)))))))))))
  (defn define-object-to-data
    ([bean_class]
      (let [g (getters
                (or
                  (resolve bean_class)
                  (do
                    (throw
                      (java.lang.IllegalArgumentException.
                        (str "Unable to resolve symbol as class: " bean_class)))
                    getters)))]
        (seq
          (concat
            (clojure.core/list 'clojure.core/extend-protocol)
            (clojure.core/list 'datomic.datafy/ObjectToData)
            (clojure.core/list bean_class)
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'object-to-data)
                  (clojure.core/list (apply vector (seq (concat (clojure.core/list 'o)))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/apply)
                        (clojure.core/list 'clojure.core/hash-map)
                        (clojure.core/list
                          (seq
                            (concat
                              (clojure.core/list 'clojure.core/concat)
                              (mapv (partial invoke-getter 'o 'm) g)))))))))))))))
  (defn type-docstring
    ([p__17298]
      (let [vec__17299 p__17298
            argname (nth vec__17299 (int 0) nil)
            _ (nth vec__17299 (int 1) nil)
            argclass (nth vec__17299 (int 2) nil)
            s__6071__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6071__auto__]
          (do
            (println (str "Shape of " argname ":"))
            (clojure.pprint/pprint (type-descriptor argclass))
            (println)
            (str s__6071__auto__))))))
  (defn define-method-to-fn
    ([m fname docstring]
      (let [argspecs (map-indexed
                       (fn fn__17304
                         ([idx cls]
                           [(symbol (str "x" (inc idx)))
                            (symbol (.getName ^java.lang.Class cls))
                            cls]))
                       (.getParameterTypes ^java.lang.reflect.Method m))
            args (mapv
                   (fn fn__17307
                     ([p__17306]
                       (let [vec__17308 p__17306
                             aname (nth vec__17308 (int 0) nil)
                             atype (nth vec__17308 (int 1) nil)]
                         (with-meta
                           (clojure.core/list 'datomic.datafy/data-to-object aname atype)
                           {:tag atype}))))
                   argspecs)
            invoc (if (= java.lang.Void/TYPE (.getReturnType ^java.lang.reflect.Method m))
                    (apply
                      vector
                      (seq
                        (-> (clojure.core/list '.)
                         (concat
                           (clojure.core/list 'o)
                           (clojure.core/list (symbol (.getName ^java.lang.reflect.Method m)))
                           args)
                         (seq)
                         (clojure.core/list)
                         (concat (clojure.core/list :ok)))))
                    (apply
                      vector
                      (seq
                        (-> (clojure.core/list 'datomic.datafy/object-to-data)
                         (clojure.core/list '.)
                         (concat
                           (clojure.core/list 'o)
                           (clojure.core/list (symbol (.getName ^java.lang.reflect.Method m)))
                           args)
                         (seq)
                         (clojure.core/list)
                         (concat)
                         (seq)
                         (clojure.core/list)
                         (concat)))))]
        (seq
          (concat
            (clojure.core/list 'clojure.core/defn)
            (clojure.core/list
              (with-meta fname {:related-class (.getDeclaringClass ^java.lang.reflect.Method m)}))
            (clojure.core/list
              (str
                "Datafication of "
                (.getDeclaringClass ^java.lang.reflect.Method m)
                "."
                (.getName ^java.lang.reflect.Method m)
                "\n"
                "Generated argument docs:\n\n"
                (apply str (map type-docstring argspecs))
                docstring))
            (clojure.core/list
              (vec
                (cons
                  (with-meta
                    'o
                    {:tag (symbol (.getName (.getDeclaringClass ^java.lang.reflect.Method m)))})
                  (mapv first argspecs))))
            invoc)))))
  (defn get-java-method
    ([cls mname arity types]
      (let [ms (filter
                 (fn fn__17314
                   ([p1__17313#]
                     (and
                       (= (name mname) (.getName ^java.lang.reflect.Method p1__17313#))
                       (=
                         arity
                         (long (count (.getParameterTypes ^java.lang.reflect.Method p1__17313#))))
                       (if (empty? types)
                         true
                         (=
                           (seq (.getParameterTypes ^java.lang.reflect.Method p1__17313#))
                           types)))))
                 (.getMethods ^java.lang.Class cls))]
        (when (= 1 (count ms)) (first ms)))))
  (defn define-object-to-functions-for
    ([&form &env clsname & specs]
      (let [cls (resolve clsname)
            arglist (mapv
                      (fn fn__17320
                        ([p__17319]
                          (let [vec__17321 p__17319
                                mname (nth vec__17321 (int 0) nil)
                                arity (nth vec__17321 (int 1) nil)
                                fname (nth vec__17321 (int 2) nil)
                                docstring? (nth vec__17321 (int 3) nil)
                                types? (nth vec__17321 (int 4) nil)
                                vec__17324 (cond
                                             (string? docstring?) [docstring? types?]
                                             (vector? docstring?) [[] docstring?]
                                             :else (do nil))
                                docstring (nth vec__17324 (int 0) nil)
                                types (nth vec__17324 (int 1) nil)
                                types (map resolve types)
                                meth (get-java-method cls mname arity types)]
                            (if meth
                              [meth (symbol (name fname)) docstring]
                              (do
                                (throw
                                  (ex-info
                                    (str
                                      "Unable to find method "
                                      mname
                                      ", you may need to specify types in define-objects-to-functions")
                                    {:cls cls, :arity arity, :types types}))
                                nil)))))
                      specs)]
        (flush)
        (seq
          (concat
            (clojure.core/list 'do)
            (mapv
              (fn fn__17329
                ([p__17328]
                  (let [vec__17330 p__17328
                        meth (nth vec__17330 (int 0) nil)
                        fname (nth vec__17330 (int 1) nil)
                        docstring (nth vec__17330 (int 2) nil)]
                    (define-method-to-fn meth fname docstring))))
              arglist))))))
  (.setMacro #'define-object-to-functions-for)
  (defn define-data-to-object-for
    ([&form &env & classes]
      (seq
        (concat
          (clojure.core/list 'do)
          (mapv (fn fn__17336 ([p1__17335#] (define-data-to-object p1__17335#))) classes)))))
  (.setMacro #'define-data-to-object-for)
  (defn define-object-to-data-for
    ([&form &env & classes]
      (seq
        (concat
          (clojure.core/list 'do)
          (mapv (fn fn__17340 ([p1__17339#] (define-object-to-data p1__17339#))) classes)))))
  (.setMacro #'define-object-to-data-for)
  (defn define-map-valued-property-to-object
    ([container property map_key_type map_value_type]
      (seq
        (concat
          (clojure.core/list 'do)
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/alter-var-root)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'var)
                      (clojure.core/list 'datomic.datafy/map-property-types))))
                (clojure.core/list 'clojure.core/assoc)
                (clojure.core/list
                  (apply
                    vector
                    (seq (concat (clojure.core/list container) (clojure.core/list property)))))
                (clojure.core/list
                  (apply
                    vector
                    (seq
                      (concat
                        (clojure.core/list map_key_type)
                        (clojure.core/list map_value_type))))))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/defmethod)
                (clojure.core/list 'd/property-to-object)
                (clojure.core/list
                  (apply
                    vector
                    (seq (concat (clojure.core/list container) (clojure.core/list property)))))
                (clojure.core/list
                  (apply
                    vector
                    (seq
                      (concat
                        (clojure.core/list '_)
                        (clojure.core/list '_)
                        (clojure.core/list 'val)
                        (clojure.core/list '_)))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/reduce)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'clojure.core/fn)
                            (clojure.core/list
                              (apply
                                vector
                                (seq
                                  (concat
                                    (clojure.core/list 'm)
                                    (clojure.core/list
                                      (apply
                                        vector
                                        (seq
                                          (concat
                                            (clojure.core/list 'k)
                                            (clojure.core/list 'v)))))))))
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list 'clojure.core/assoc)
                                  (clojure.core/list 'm)
                                  (clojure.core/list 'k)
                                  (clojure.core/list
                                    (seq
                                      (concat
                                        (clojure.core/list 'd/data-to-object)
                                        (clojure.core/list 'v)
                                        (clojure.core/list map_value_type))))))))))
                      (clojure.core/list (apply hash-map (seq (concat))))
                      (clojure.core/list 'val)))))))))))
  (defn define-map-valued-property-to-object-for
    ([&form &env & descs]
      (seq
        (concat
          (clojure.core/list 'do)
          (mapv
            (fn fn__17345 ([p1__17344#] (apply define-map-valued-property-to-object p1__17344#)))
            descs)))))
  (.setMacro #'define-map-valued-property-to-object-for)
  (defn define-list-valued-property-to-object
    ([container property list_value_type]
      (seq
        (concat
          (clojure.core/list 'do)
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/alter-var-root)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'var)
                      (clojure.core/list 'datomic.datafy/list-property-types))))
                (clojure.core/list 'clojure.core/assoc)
                (clojure.core/list
                  (apply
                    vector
                    (seq (concat (clojure.core/list container) (clojure.core/list property)))))
                (clojure.core/list list_value_type))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/defmethod)
                (clojure.core/list 'd/property-to-object)
                (clojure.core/list
                  (apply
                    vector
                    (seq (concat (clojure.core/list container) (clojure.core/list property)))))
                (clojure.core/list
                  (apply
                    vector
                    (seq
                      (concat
                        (clojure.core/list '_)
                        (clojure.core/list '_)
                        (clojure.core/list 'val)
                        (clojure.core/list '_)))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/mapv)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'clojure.core/fn)
                            (clojure.core/list
                              (apply vector (seq (concat (clojure.core/list 'item)))))
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list 'd/data-to-object)
                                  (clojure.core/list 'item)
                                  (clojure.core/list list_value_type)))))))
                      (clojure.core/list 'val)))))))))))
  (defn define-list-valued-property-to-object-for
    ([&form &env & descs]
      (seq
        (concat
          (clojure.core/list 'do)
          (mapv
            (fn fn__17350 ([p1__17349#] (apply define-list-valued-property-to-object p1__17349#)))
            descs)))))
  (.setMacro #'define-list-valued-property-to-object-for)
  (defn val-setters
    ([bean_class]
      (reduce
        (fn fn__17353
          ([m pd]
            (let [name (.getName ^java.beans.FeatureDescriptor pd)
                  method (.getWriteMethod ^java.beans.PropertyDescriptor pd)]
              (if (and
                    method
                    (= 1 (long (alength (.getParameterTypes ^java.lang.reflect.Method method)))))
                (let [type (first (.getParameterTypes ^java.lang.reflect.Method method))]
                  (assoc
                    m
                    (keyword name)
                    (fn fn__17354
                      ([bean value]
                        (.invoke
                          ^java.lang.reflect.Method method
                          bean
                          (into-array [(data-to-object value type)]))))))
                m))))
        {}
        (.getPropertyDescriptors (Introspector/getBeanInfo ^java.lang.Class bean_class)))))
  (defn vals-to-bean
    ([bean props]
      (let [setters (val-setters (class bean))]
        (loop [seq_17359 (seq props) chunk_17360 nil count_17361 0 i_17362 0]
          (if (< i_17362 count_17361)
            (let [vec__17363 (.nth ^clojure.lang.Indexed chunk_17360 (int i_17362))
                  k (nth vec__17363 (int 0) nil)
                  v (nth vec__17363 (int 1) nil)]
              (let [temp__5455__auto__ (^clojure.lang.IFn setters k)]
                (if temp__5455__auto__
                  (let [setter temp__5455__auto__] (^clojure.lang.IFn setter bean v))
                  (throw (java.lang.IllegalArgumentException. (str "No property named " k)))))
              (recur seq_17359 chunk_17360 count_17361 (inc i_17362)))
            (let [temp__5457__auto__ (seq seq_17359)]
              (when temp__5457__auto__
                (let [seq_17359 temp__5457__auto__]
                  (if (chunked-seq? seq_17359)
                    (let [c__5719__auto__ (chunk-first seq_17359)]
                      (recur
                        (chunk-rest seq_17359)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [vec__17366 (first seq_17359)
                          k (nth vec__17366 (int 0) nil)
                          v (nth vec__17366 (int 1) nil)]
                      (let [temp__5455__auto__ (^clojure.lang.IFn setters k)]
                        (if temp__5455__auto__
                          (let [setter temp__5455__auto__] (^clojure.lang.IFn setter bean v))
                          (throw
                            (java.lang.IllegalArgumentException. (str "No property named " k)))))
                      (recur (next seq_17359) nil 0 0))))))))
        bean))))

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
  (def setters
   (fn setters
     ([cls]
       (when-not cls (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'cls)))))
       (filter
         (fn fn__23017
           ([p1__23016#]
             (and
               (= "set" (subs (.getName ^java.lang.reflect.Method p1__23016#) 0 3))
               (= 1 (long (count (.getParameterTypes ^java.lang.reflect.Method p1__23016#)))))))
         (.getMethods ^java.lang.Class cls)))))
  (reset-meta!
    #'setters
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cls {:tag 'Class})]), :column (int 1)}
      :name
      'setters
      :ns
      *ns*))
  (defn setter-name->keyword
    ([n] (let [base (subs n 3)] (keyword (str (.toLowerCase (subs base 0 1)) (subs base 1))))))
  (reset-meta!
    #'setter-name->keyword
    (assoc
      {:arglists (clojure.core/list ['n]), :column (int 1)}
      :name
      'setter-name->keyword
      :ns
      *ns*))
  (defn filter-overlapped-setters
    ([coll]
      (reduce
        (fn fn__23024
          ([result p__23023]
            (let [vec__23025 p__23023
                  k (nth vec__23025 (int 0) nil)
                  setters (nth vec__23025 (int 1) nil)
                  G__23028 (count setters)]
              (case
                G__23028
                1
                (conj result (first setters))
                2
                (let [first_string? (=
                                      java.lang.String
                                      (first (.getParameterTypes (first setters))))]
                  (conj result ((if first_string? second first) setters)))))))
        []
        (group-by
          (fn fn__23030 ([p1__23022#] (.getName ^java.lang.reflect.Method p1__23022#)))
          coll))))
  (reset-meta!
    #'filter-overlapped-setters
    (assoc
      {:arglists (clojure.core/list ['coll]), :column (int 1)}
      :name
      'filter-overlapped-setters
      :ns
      *ns*))
  (def getter?
   (fn getter_QMARK_
     ([m]
       (let [name (.getName ^java.lang.reflect.Method m)]
         (and
           (or (= "is" (subs name 0 2)) (= "get" (subs name 0 3)))
           (not= "getClass" name)
           (= 0 (long (count (.getParameterTypes ^java.lang.reflect.Method m)))))))))
  (reset-meta!
    #'getter?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'm {:tag 'Method})]), :column (int 1)}
      :name
      'getter?
      :ns
      *ns*))
  (def getters
   (fn getters
     ([cls]
       (when-not cls (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'cls)))))
       (filter getter? (.getMethods ^java.lang.Class cls)))))
  (reset-meta!
    #'getters
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cls {:tag 'Class})]), :column (int 1)}
      :name
      'getters
      :ns
      *ns*))
  (def type-descriptor-category
   (fn type_descriptor_category
     ([c]
       (let [anc (conj (or (ancestors c) #{}) c)]
         (cond
           (contains? anc java.util.Map) :map
           (contains? anc java.util.Collection) :list
           (.isEnum ^java.lang.Class c) :enum
           (.isPrimitive ^java.lang.Class c) :self
           (.startsWith (.getName (.getPackage ^java.lang.Class c)) "java") :self
           :default (do :bean))))))
  (reset-meta!
    #'type-descriptor-category
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'c {:tag 'Class})]), :column (int 1)}
      :name
      'type-descriptor-category
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.datafy" "type-descriptor") {:column (int 1)})
  (let [v__5792__auto__ #'type-descriptor]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.datafy" "type-descriptor") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.datafy" "type-descriptor")
        (clojure.lang.MultiFn.
          "type-descriptor"
          type-descriptor-category
          :default
          #'clojure.core/global-hierarchy))
      #'type-descriptor))
  (.setMeta (clojure.lang.RT/var "datomic.datafy" "property-descriptor") {:column (int 1)})
  (let [v__5792__auto__ #'property-descriptor]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.datafy" "property-descriptor") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.datafy" "property-descriptor")
        (clojure.lang.MultiFn.
          "property-descriptor"
          (fn fn__23045 ([container prop type] (type-descriptor-category type)))
          :default
          #'clojure.core/global-hierarchy))
      #'property-descriptor))
  (def map-property-types {})
  (reset-meta! #'map-property-types (assoc {:column (int 1)} :name 'map-property-types :ns *ns*))
  (def list-property-types {})
  (reset-meta! #'list-property-types (assoc {:column (int 1)} :name 'list-property-types :ns *ns*))
  (defmethod
    property-descriptor
    :map
    fn__23050
    ([container prop type]
      (let [temp__5802__auto__ (map-property-types [container prop])]
        (if temp__5802__auto__
          (let [vec__23051 temp__5802__auto__
                k (nth vec__23051 (int 0) nil)
                v (nth vec__23051 (int 1) nil)]
            {k (type-descriptor v)})
          (type-descriptor type)))))
  (defmethod
    property-descriptor
    :list
    fn__23056
    ([container prop type]
      (let [temp__5802__auto__ (list-property-types [container prop])]
        (if temp__5802__auto__
          (let [v temp__5802__auto__] [(type-descriptor v)])
          (type-descriptor type)))))
  (defmethod property-descriptor :default fn__23059 ([_ _ type] (type-descriptor type)))
  (defmethod
    type-descriptor
    :enum
    fn__23062
    ([c]
      (into
        #{}
        (map
          :name
          (filter
            (fn fn__23063
              ([p1__23061#]
                (and
                  (contains? (:flags p1__23061#) :static)
                  (contains? (:flags p1__23061#) :public)
                  (:type p1__23061#)
                  (= (resolve (:type p1__23061#)) c))))
            (:members (reflect/reflect c)))))))
  (defmethod
    type-descriptor
    :bean
    fn__23069
    ([c]
      (let [temp__5802__auto__ (seq (filter-overlapped-setters (setters c)))]
        (if temp__5802__auto__
          (let [s temp__5802__auto__]
            (reduce
              (fn fn__23070
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
  (defmethod type-descriptor :default fn__23074 ([c] (symbol (.getName ^java.lang.Class c))))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      ObjectToData
      (object-to-data
        [o]
        "Recursively convert data hidden in DAOs, DTOs, etc. into generic, usable data."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.datafy" "ObjectToData")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'ObjectToData :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'object-to-data
                                        {:arglists (clojure.core/list ['o])}),
                                      :arglists (clojure.core/list ['o]),
                                      :doc
                                      "Recursively convert data hidden in DAOs, DTOs, etc. into generic, usable data."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.datafy" "ObjectToData"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.datafy" "object-to-data")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (defn object-to-data-wrapper ([x] (object-to-data x)))
  (reset-meta!
    #'object-to-data-wrapper
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'object-to-data-wrapper
      :ns
      *ns*))
  (extend java.lang.Object ObjectToData {:object-to-data (fn fn__23093 ([o] o))})
  (extend
    java.util.ArrayList
    ObjectToData
    {:object-to-data (fn fn__23095 ([o] (mapv object-to-data o)))})
  (extend
    java.util.Map
    ObjectToData
    {:object-to-data
     (fn fn__23097
       ([m]
         (reduce
           (fn fn__23099
             ([m p__23098]
               (let [vec__23100 p__23098
                     k (nth vec__23100 (int 0) nil)
                     v (nth vec__23100 (int 1) nil)]
                 (assoc m (object-to-data k) (object-to-data v)))))
           {}
           m)))})
  (defn vals-to-data
    ([m]
      (persistent!
        (reduce
          (fn fn__23106
            ([m p__23105]
              (let [vec__23107 p__23105
                    k (nth vec__23107 (int 0) nil)
                    v (nth vec__23107 (int 1) nil)]
                (assoc! m k (object-to-data v)))))
          (transient {})
          m))))
  (reset-meta!
    #'vals-to-data
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'vals-to-data :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.datafy" "data-to-object") {:column (int 1)})
  (let [v__5792__auto__ #'data-to-object]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.datafy" "data-to-object") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.datafy" "data-to-object")
        (clojure.lang.MultiFn.
          "data-to-object"
          (fn fn__23113 ([obj type] [(data/equality-partition obj) type]))
          :default
          #'clojure.core/global-hierarchy))
      #'data-to-object))
  (defmethod
    data-to-object
    [:atom java.lang.Integer]
    fn__23118
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Integer/valueOf (int n))))
  (defmethod
    data-to-object
    [:atom java.lang.Integer/TYPE]
    fn__23120
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Integer/valueOf (int n))))
  (defmethod
    data-to-object
    [:atom java.lang.Long]
    fn__23122
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Long/valueOf (long n))))
  (defmethod
    data-to-object
    [:atom java.lang.Long/TYPE]
    fn__23124
    ([n _]
      (when-not (integer? n)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'n))))))
      (java.lang.Long/valueOf (long n))))
  (defmethod
    data-to-object
    [:atom java.lang.Boolean]
    fn__23126
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
      (java.lang.Boolean/valueOf (boolean n))))
  (defmethod
    data-to-object
    [:atom java.lang.Boolean/TYPE]
    fn__23129
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
      (java.lang.Boolean/valueOf (boolean n))))
  (defmethod
    data-to-object
    :default
    fn__23132
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
  (.setMeta (clojure.lang.RT/var "datomic.datafy" "property-to-object") {:column (int 1)})
  (let [v__5792__auto__ #'property-to-object]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.datafy" "property-to-object") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.datafy" "property-to-object")
        (clojure.lang.MultiFn.
          "property-to-object"
          (fn fn__23135 ([container prop obj type] [container prop]))
          :default
          #'clojure.core/global-hierarchy))
      #'property-to-object))
  (defmethod
    property-to-object
    :default
    fn__23140
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
  (def invoke-setter
   (fn invoke_setter
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
                         (clojure.core/list 'k)))))))))))))
  (reset-meta!
    #'invoke-setter
    (assoc
      {:arglists (clojure.core/list ['o 'm (.withMeta 'meth {:tag 'Method})]), :column (int 1)}
      :name
      'invoke-setter
      :ns
      *ns*))
  (def invoke-getter
   (fn invoke_getter
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
                     (clojure.core/list 'v__23143__auto__)
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
                           (clojure.core/list 'v__23143__auto__))))))))))))))
  (reset-meta!
    #'invoke-getter
    (assoc
      {:arglists (clojure.core/list ['o 'm (.withMeta 'meth {:tag 'Method})]), :column (int 1)}
      :name
      'invoke-getter
      :ns
      *ns*))
  (def can-data-to-object?
   (fn can_data_to_object_QMARK_
     ([cls]
       (let [default (get-method data-to-object nil)]
         (or
           (not= default (get-method data-to-object [:atom cls]))
           (not= default (get-method data-to-object [:map cls]))
           (str/starts-with? (.getName ^java.lang.Class cls) "java."))))))
  (reset-meta!
    #'can-data-to-object?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cls {:tag 'Class})]), :column (int 1)}
      :name
      'can-data-to-object?
      :ns
      *ns*))
  (def method-input-classes
   (fn method_input_classes ([m] (into #{} (.getParameterTypes ^java.lang.reflect.Method m)))))
  (reset-meta!
    #'method-input-classes
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'm {:tag 'Method})]), :column (int 1)}
      :name
      'method-input-classes
      :ns
      *ns*))
  (def define-data-to-object
   (fn define_data_to_object
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
                                (fn fn__23150
                                  ([p1__23149#]
                                    (setter-name->keyword
                                      (.getName ^java.lang.reflect.Method p1__23149#))))
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
                       (clojure.core/list 'o))))))))))))
  (reset-meta!
    #'define-data-to-object
    (assoc
      {:arglists (clojure.core/list ['bean-class]), :column (int 1)}
      :name
      'define-data-to-object
      :ns
      *ns*))
  (def define-object-to-data
   (fn define_object_to_data
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
                               (mapv (partial invoke-getter 'o 'm) g))))))))))))))))
  (reset-meta!
    #'define-object-to-data
    (assoc
      {:arglists (clojure.core/list ['bean-class]), :column (int 1)}
      :name
      'define-object-to-data
      :ns
      *ns*))
  (def type-docstring
   (fn type_docstring
     ([p__23156]
       (let [vec__23157 p__23156
             argname (nth vec__23157 (int 0) nil)
             _ (nth vec__23157 (int 1) nil)
             argclass (nth vec__23157 (int 2) nil)
             s__6419__auto__ (java.io.StringWriter.)]
         (binding [*out* s__6419__auto__]
           (do
             (println (str "Shape of " argname ":"))
             (clojure.pprint/pprint (type-descriptor argclass))
             (println)
             (str s__6419__auto__)))))))
  (reset-meta!
    #'type-docstring
    (assoc
      {:arglists (clojure.core/list [['argname '_ 'argclass]]), :column (int 1)}
      :name
      'type-docstring
      :ns
      *ns*))
  (def define-method-to-fn
   (fn define_method_to_fn
     ([m fname docstring]
       (let [argspecs (map-indexed
                        (fn fn__23162
                          ([idx cls]
                            [(symbol (str "x" (inc idx)))
                             (symbol (.getName ^java.lang.Class cls))
                             cls]))
                        (.getParameterTypes ^java.lang.reflect.Method m))
             args (mapv
                    (fn fn__23165
                      ([p__23164]
                        (let [vec__23166 p__23164
                              aname (nth vec__23166 (int 0) nil)
                              atype (nth vec__23166 (int 1) nil)]
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
             invoc))))))
  (reset-meta!
    #'define-method-to-fn
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'm {:tag 'Method}) 'fname 'docstring]),
       :column (int 1)}
      :name
      'define-method-to-fn
      :ns
      *ns*))
  (def get-java-method
   (fn get_java_method
     ([cls mname arity types]
       (let [ms (filter
                  (fn fn__23172
                    ([p1__23171#]
                      (and
                        (= (name mname) (.getName ^java.lang.reflect.Method p1__23171#))
                        (=
                          arity
                          (long (count (.getParameterTypes ^java.lang.reflect.Method p1__23171#))))
                        (if (empty? types)
                          true
                          (=
                            (seq (.getParameterTypes ^java.lang.reflect.Method p1__23171#))
                            types)))))
                  (.getMethods ^java.lang.Class cls))]
         (when (= 1 (count ms)) (first ms))))))
  (reset-meta!
    #'get-java-method
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'cls {:tag 'Class}) 'mname 'arity 'types]),
       :column (int 1)}
      :name
      'get-java-method
      :ns
      *ns*))
  (def define-object-to-functions-for
   (fn define_object_to_functions_for
     ([&form &env clsname & specs]
       (let [cls (resolve clsname)
             arglist (mapv
                       (fn fn__23178
                         ([p__23177]
                           (let [vec__23179 p__23177
                                 mname (nth vec__23179 (int 0) nil)
                                 arity (nth vec__23179 (int 1) nil)
                                 fname (nth vec__23179 (int 2) nil)
                                 docstring? (nth vec__23179 (int 3) nil)
                                 types? (nth vec__23179 (int 4) nil)
                                 vec__23182 (cond
                                              (string? docstring?) [docstring? types?]
                                              (vector? docstring?) [[] docstring?]
                                              :else (do nil))
                                 docstring (nth vec__23182 (int 0) nil)
                                 types (nth vec__23182 (int 1) nil)
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
               (fn fn__23187
                 ([p__23186]
                   (let [vec__23188 p__23186
                         meth (nth vec__23188 (int 0) nil)
                         fname (nth vec__23188 (int 1) nil)
                         docstring (nth vec__23188 (int 2) nil)]
                     (define-method-to-fn meth fname docstring))))
               arglist)))))))
  (reset-meta!
    #'define-object-to-functions-for
    (assoc
      {:arglists (clojure.core/list ['clsname '& 'specs]), :column (int 1)}
      :name
      'define-object-to-functions-for
      :ns
      *ns*))
  (.setMacro #'define-object-to-functions-for)
  (def define-data-to-object-for
   (fn define_data_to_object_for
     ([&form &env & classes]
       (seq
         (concat
           (clojure.core/list 'do)
           (mapv (fn fn__23194 ([p1__23193#] (define-data-to-object p1__23193#))) classes))))))
  (reset-meta!
    #'define-data-to-object-for
    (assoc
      {:arglists (clojure.core/list ['& 'classes]), :column (int 1)}
      :name
      'define-data-to-object-for
      :ns
      *ns*))
  (.setMacro #'define-data-to-object-for)
  (def define-object-to-data-for
   (fn define_object_to_data_for
     ([&form &env & classes]
       (seq
         (concat
           (clojure.core/list 'do)
           (mapv (fn fn__23198 ([p1__23197#] (define-object-to-data p1__23197#))) classes))))))
  (reset-meta!
    #'define-object-to-data-for
    (assoc
      {:arglists (clojure.core/list ['& 'classes]), :column (int 1)}
      :name
      'define-object-to-data-for
      :ns
      *ns*))
  (.setMacro #'define-object-to-data-for)
  (def define-map-valued-property-to-object
   (fn define_map_valued_property_to_object
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
                       (clojure.core/list 'val))))))))))))
  (reset-meta!
    #'define-map-valued-property-to-object
    (assoc
      {:arglists (clojure.core/list ['container 'property 'map-key-type 'map-value-type]),
       :column (int 1)}
      :name
      'define-map-valued-property-to-object
      :ns
      *ns*))
  (def define-map-valued-property-to-object-for
   (fn define_map_valued_property_to_object_for
     ([&form &env & descs]
       (seq
         (concat
           (clojure.core/list 'do)
           (mapv
             (fn fn__23203 ([p1__23202#] (apply define-map-valued-property-to-object p1__23202#)))
             descs))))))
  (reset-meta!
    #'define-map-valued-property-to-object-for
    (assoc
      {:arglists (clojure.core/list ['& 'descs]), :column (int 1)}
      :name
      'define-map-valued-property-to-object-for
      :ns
      *ns*))
  (.setMacro #'define-map-valued-property-to-object-for)
  (def define-list-valued-property-to-object
   (fn define_list_valued_property_to_object
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
                       (clojure.core/list 'val))))))))))))
  (reset-meta!
    #'define-list-valued-property-to-object
    (assoc
      {:arglists (clojure.core/list ['container 'property 'list-value-type]), :column (int 1)}
      :name
      'define-list-valued-property-to-object
      :ns
      *ns*))
  (def define-list-valued-property-to-object-for
   (fn define_list_valued_property_to_object_for
     ([&form &env & descs]
       (seq
         (concat
           (clojure.core/list 'do)
           (mapv
             (fn fn__23208 ([p1__23207#] (apply define-list-valued-property-to-object p1__23207#)))
             descs))))))
  (reset-meta!
    #'define-list-valued-property-to-object-for
    (assoc
      {:arglists (clojure.core/list ['& 'descs]), :column (int 1)}
      :name
      'define-list-valued-property-to-object-for
      :ns
      *ns*))
  (.setMacro #'define-list-valued-property-to-object-for)
  (def val-setters
   (fn val_setters
     ([bean_class]
       (reduce
         (fn fn__23211
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
                     (fn fn__23212
                       ([bean value]
                         (.invoke
                           ^java.lang.reflect.Method method
                           bean
                           (into-array [(data-to-object value type)]))))))
                 m))))
         {}
         (.getPropertyDescriptors (Introspector/getBeanInfo ^java.lang.Class bean_class))))))
  (reset-meta!
    #'val-setters
    (assoc
      {:arglists (clojure.core/list ['bean-class]), :column (int 1)}
      :name
      'val-setters
      :ns
      *ns*))
  (defn vals-to-bean
    ([bean props]
      (let [setters (val-setters (class bean))]
        (loop [seq_23217 (seq props) chunk_23218 nil count_23219 0 i_23220 0]
          (if (< i_23220 count_23219)
            (let [vec__23221 (.nth ^clojure.lang.Indexed chunk_23218 (int i_23220))
                  k (nth vec__23221 (int 0) nil)
                  v (nth vec__23221 (int 1) nil)]
              (let [temp__5802__auto__ (^clojure.lang.IFn setters k)]
                (if temp__5802__auto__
                  (let [setter temp__5802__auto__] (^clojure.lang.IFn setter bean v))
                  (throw (java.lang.IllegalArgumentException. (str "No property named " k)))))
              (recur seq_23217 chunk_23218 count_23219 (inc i_23220)))
            (let [temp__5804__auto__ (seq seq_23217)]
              (when temp__5804__auto__
                (let [seq_23217 temp__5804__auto__]
                  (if (chunked-seq? seq_23217)
                    (let [c__6065__auto__ (chunk-first seq_23217)]
                      (recur
                        (chunk-rest seq_23217)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__23224 (first seq_23217)
                          k (nth vec__23224 (int 0) nil)
                          v (nth vec__23224 (int 1) nil)]
                      (let [temp__5802__auto__ (^clojure.lang.IFn setters k)]
                        (if temp__5802__auto__
                          (let [setter temp__5802__auto__] (^clojure.lang.IFn setter bean v))
                          (throw
                            (java.lang.IllegalArgumentException. (str "No property named " k)))))
                      (recur (next seq_23217) nil 0 0))))))))
        bean)))
  (reset-meta!
    #'vals-to-bean
    (assoc
      {:arglists (clojure.core/list ['bean 'props]), :column (int 1)}
      :name
      'vals-to-bean
      :ns
      *ns*)))
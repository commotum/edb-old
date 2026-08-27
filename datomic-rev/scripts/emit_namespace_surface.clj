(require '[clojure.string :as str])

(defn fail! [message]
  (binding [*out* *err*]
    (println message))
  (System/exit 2))

(def namespace-name
  (or (some-> *command-line-args* first symbol)
      (fail! "usage: emit_namespace_surface.clj NAMESPACE")))

(defn arity-shape [argv]
  (let [[required tail] (split-with #(not= '& %) argv)]
    [(count required) (boolean (seq tail))]))

(declare primitive-fn-surface)

(defn root-kind [v]
  (if-not (bound? v)
    :unbound
    (let [root @v]
      (cond
        (nil? root) [:scalar nil]
        (or (boolean? root)
            (number? root)
            (string? root)
            (keyword? root)
            (symbol? root)
            (char? root)) [:scalar root]
        (instance? clojure.lang.MultiFn root) :multifn
        (fn? root) (let [primitive-abi (primitive-fn-surface root)]
                     (if (seq primitive-abi)
                       [:fn primitive-abi]
                       :fn))
        (class? root) [:class (.getName ^Class root)]
        (map? root) :map
        (vector? root) :vector
        (set? root) :set
        (list? root) :list
        (seq? root) :seq
        (.isArray (class root)) [:array (.getName (class root))]
        :else (let [class-name (.getName (class root))]
                (cond
                  (str/includes? class-name "$reify__") :reify
                  (str/includes? class-name ".proxy$") :proxy
                  :else [:object class-name]))))))

(defn var-surface [[sym v]]
  (let [m (meta v)
        arglists (:arglists m)
        ;; Each side is loaded in a different JVM, so the numeric process ID
        ;; cannot be equal even when its definition is semantically identical.
        root (if (and (= namespace-name 'datomic.slf4j)
                      (= sym 'pid))
               :process-id
               (root-kind v))]
    [sym
     (cond-> {:root root}
       (:private m) (assoc :private true)
       (:dynamic m) (assoc :dynamic true)
       (:macro m) (assoc :macro true)
       (seq arglists) (assoc :arities (->> arglists
                                           (map arity-shape)
                                           distinct
                                           sort
                                           vec)))]))

(defn compiler-generated-proxy-class-var? [[sym v]]
  ;; Evaluating a `proxy` form from source interns its generated class under a
  ;; name such as `the.ns.proxy$java.io.OutputStream$...`.  Loading the same
  ;; namespace from AOT bytecode does not retain that compiler scratch Var.
  ;; It is not part of the namespace's authored/runtime API surface.
  (and (str/includes? (name sym) ".proxy$")
       (bound? v)
       (class? @v)
       (str/includes? (.getName ^Class @v) ".proxy$")))

(def primitive-descriptors
  {Void/TYPE "V"
   Boolean/TYPE "Z"
   Byte/TYPE "B"
   Character/TYPE "C"
   Short/TYPE "S"
   Integer/TYPE "I"
   Long/TYPE "J"
   Float/TYPE "F"
   Double/TYPE "D"})

(defn descriptor [^Class c]
  (cond
    (.isPrimitive c) (primitive-descriptors c)
    (.isArray c) (str/replace (.getName c) "." "/")
    :else (str "L" (str/replace (.getName c) "." "/") ";")))

(defn executable-descriptor [parameter-types return-type]
  (str "(" (apply str (map descriptor parameter-types)) ")"
       (descriptor return-type)))

(defn field-surface [^java.lang.reflect.Field field]
  {:name (.getName field)
   :descriptor (descriptor (.getType field))
   :modifiers (.getModifiers field)})

(defn method-surface [^java.lang.reflect.Method method]
  {:name (.getName method)
   :descriptor (executable-descriptor (.getParameterTypes method)
                                      (.getReturnType method))
   :modifiers (.getModifiers method)})

(defn constructor-surface [^java.lang.reflect.Constructor constructor]
  {:descriptor (executable-descriptor (.getParameterTypes constructor)
                                      Void/TYPE)
   :modifiers (.getModifiers constructor)})

(defn primitive-fn-surface [root]
  (let [c (class root)
        interfaces (->> (.getInterfaces c)
                        (map #(.getName ^Class %))
                        (filter #(str/starts-with? % "clojure.lang.IFn$"))
                        sort
                        vec)
        methods (->> (.getDeclaredMethods c)
                     (filter #(= "invokePrim" (.getName ^java.lang.reflect.Method %)))
                     (map method-surface)
                     (sort-by pr-str)
                     vec)]
    (cond-> {}
      (seq interfaces) (assoc :interfaces interfaces)
      (seq methods) (assoc :methods methods))))

(defn class-surface [^Class c]
  [(.getName c)
   {:modifiers (.getModifiers c)
    :super (some-> (.getSuperclass c) .getName)
    :interfaces (->> (.getInterfaces c) (map #(.getName ^Class %)) sort vec)
    :fields (->> (.getDeclaredFields c)
                 (remove #(java.lang.reflect.Modifier/isStatic
                            (.getModifiers ^java.lang.reflect.Field %)))
                 (map field-surface)
                 (sort-by pr-str)
                 vec)
    :methods (->> (.getDeclaredMethods c)
                  (remove #(.isBridge ^java.lang.reflect.Method %))
                  (remove #(.isSynthetic ^java.lang.reflect.Method %))
                  (map method-surface)
                  (sort-by pr-str)
                  vec)
    :constructors (->> (.getDeclaredConstructors c)
                       (map constructor-surface)
                       (sort-by pr-str)
                       vec)}])

(defn namespace-classes [n]
  (let [prefix (str namespace-name ".")
        local-class? (fn [x]
                       (and (class? x)
                            (str/starts-with? (.getName ^Class x) prefix)))
        imported (filter local-class? (vals (ns-map n)))
        protocols (keep (fn [[_ v]]
                          (when (bound? v)
                            (let [root @v
                                  interface (when (map? root)
                                              (:on-interface root))]
                              (when (local-class? interface)
                                interface))))
                        (ns-interns n))]
    (->> (concat imported protocols)
         distinct
         (sort-by #(.getName ^Class %)))))

(try
  (binding [*out* (java.io.StringWriter.)]
    (require namespace-name))
  (let [n (the-ns namespace-name)]
    (prn {:vars (into (sorted-map)
                      (map var-surface)
                      (remove compiler-generated-proxy-class-var?
                              (ns-interns n)))
          :classes (into (sorted-map)
                         (map class-surface)
                         (namespace-classes n))}))
  (catch Throwable failure
    (let [chain (take-while some? (iterate ex-cause failure))]
      (binding [*out* *err*]
        (println "FAIL" namespace-name (pr-str (mapv ex-message chain)))))
    (System/exit 1)))

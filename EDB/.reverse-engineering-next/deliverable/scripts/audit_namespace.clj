(require '[clojure.java.io :as io]
         '[clojure.walk :as walk]
         '[clojure.string :as str])

(def source-file (first *command-line-args*))
(def raw-form
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (io/reader source-file))]
      (read r))))

(defn unquote-binding [x]
  (if (and (seq? x) (= 'quote (first x)) (= 2 (count x)))
    (second x)
    x))

(defn normalize-symbol-coll [x]
  (cond
    (vector? x) (mapv unquote-binding x)
    (and (seq? x) (= 'list (first x))) (mapv unquote-binding (rest x))
    :else x))

(defn fix-protocol [x]
  (let [[_ protocol-name & declarations] x]
    (loop [remaining declarations
           fixed ['defprotocol protocol-name]]
      (if (empty? remaining)
        (apply list fixed)
        (let [[method-name arglists & more] remaining
              arglists (mapv (fn [argv] (mapv unquote-binding argv)) arglists)
              declaration (if (= 1 (count arglists))
                            (list method-name (first arglists))
                            (list method-name (apply list arglists)))]
          (recur more (conj fixed declaration)))))))

(defn short-class-symbol [s]
  (let [n (name s)
        i (.lastIndexOf ^String n ".")]
    (symbol (if (neg? i) n (subs n (inc i))))))

(def form
  (walk/postwalk
    (fn [x]
      (cond
        (and (System/getenv "AUDIT_HINT_SCHEDULE_CALLABLE")
             (seq? x) (= '.schedule (first x)) (= 5 (count x)))
        (let [[method target task delay unit] x]
          (list method target
                (with-meta task (assoc (meta task) :tag 'java.util.concurrent.Callable))
                delay unit))

        ;; When auditing reconstructed source against the original peer JAR,
        ;; defining a type with the same binary name creates a child-loader
        ;; duplicate.  Interface method hints then resolve to the duplicate
        ;; while the parent-loaded interface references the original class,
        ;; producing a false "Can't find matching method" error.  This opt-in
        ;; mode keeps the original peer types for dependency-only compile
        ;; audits; the default behavior remains unchanged.
        (and (System/getenv "AUDIT_SKIP_DUPLICATE_TYPES")
             (seq? x) (#{'deftype 'defrecord} (first x))
             (symbol? (second x)))
        nil

        (and (seq? x) (= '.setDynamic (first x))
             (= 3 (count x)) (= 1 (nth x 2)))
        (list '.setDynamic (second x) true)

        (and (seq? x) (= 'defprotocol (first x))
             ;; The current recovered source has alternating method/arglist
             ;; entries. A freshly patched decompile already has valid
             ;; defprotocol declarations and must be left alone.
             (not (and (seq? (nth x 2 nil))
                       (symbol? (first (nth x 2 nil))))))
        (fix-protocol x)

        (and (seq? x) (#{'deftype 'defrecord} (first x))
             (symbol? (second x)))
        (apply list (first x) (short-class-symbol (second x)) (drop 2 x))

        :else x))
    raw-form))

(def target-ns
  (let [found (atom nil)]
    (walk/postwalk
      (fn [x]
        (when (and (nil? @found)
                   (seq? x)
                   (#{'in-ns 'clojure.core/in-ns} (first x))
                   (= 2 (count x)))
          (let [quoted (atom nil)]
            (walk/postwalk
              (fn [y]
                (when (and (nil? @quoted)
                           (seq? y)
                           (= 'quote (first y))
                           (symbol? (second y)))
                  (reset! quoted (second y)))
                y)
              (second x))
            (reset! found @quoted)))
        x)
      raw-form)
    @found))

(when-not target-ns
  (throw (ex-info "No in-ns form" {:file source-file})))

(def n (or (find-ns target-ns) (create-ns target-ns)))
(binding [*ns* n]
  (clojure.core/refer 'clojure.core))

(def exclusions
  (let [found (atom #{})]
    (walk/postwalk
      (fn [x]
        (when (and (seq? x) (= 'refer-clojure (first x)))
          (doseq [[k v] (partition 2 (rest x))
                  :when (= :exclude k)
                  sym (normalize-symbol-coll v)]
            (swap! found conj sym)))
        x)
      raw-form)
    @found))

;; Run one copy of the initializer's namespace setup before predeclaring local
;; Vars. The generated initializer contains the same require/import/refer block
;; twice; running both would remove the forward declarations on the second pass.
(def namespace-setup
  (walk/postwalk
    (fn [x]
      ;; The staged initializer's refer-clojure form is independently audited;
      ;; omit it here because this harness has already referred core and later
      ;; applies the exact exclusions itself.
      (if (and (seq? x)
               (#{'refer-clojure 'clojure.core/refer-clojure} (first x)))
        nil
        x))
    (nth form 2 nil)))
(def body-form (cons 'do (drop 4 form)))

(try
  (binding [*ns* n
            *err* (java.io.StringWriter.)]
    (eval namespace-setup))
  (catch Throwable t
    (let [chain (take-while some? (iterate ex-cause t))
          messages (mapv ex-message chain)]
      (println "FAIL" source-file "namespace-setup" (pr-str messages)))
    (System/exit 1)))

(doseq [sym exclusions]
  (ns-unmap n sym))

;; The decompiler emits references to Vars before their reconstructed defs.
;; Pre-intern them so compilation can proceed far enough to expose deeper faults.
(walk/postwalk
  (fn [x]
    (when (seq? x)
      (cond
        (and (= 'var (first x))
             (= 2 (count x))
             (symbol? (second x))
             (or (nil? (namespace (second x)))
                 (= (namespace (second x)) (str target-ns))))
        (intern n (symbol (name (second x))))

        (and (System/getenv "AUDIT_PREINTERN_QUALIFIED_VARS")
             (= 'var (first x))
             (= 2 (count x))
             (symbol? (second x))
             (namespace (second x)))
        (let [owner-sym (symbol (namespace (second x)))
              owner (or (find-ns owner-sym) (create-ns owner-sym))]
          (intern owner (symbol (name (second x)))))

        (and (symbol? (first x))
             (#{"def" "defn" "defmacro" "declare"} (name (first x)))
             (symbol? (second x)))
        (intern n (symbol (name (second x))))

        (and (symbol? (first x))
             (= "defprotocol" (name (first x)))
             (symbol? (second x)))
        (doseq [decl (drop 2 x)
                :when (and (seq? decl) (symbol? (first decl)))]
          (intern n (first decl)))))
    x)
  body-form)

;; Generated namespace setup directly accesses clojure.core's private Var.
(intern n '*loaded-libs*
        (var-get (ns-resolve 'clojure.core '*loaded-libs*)))

(when (System/getenv "AUDIT_DEBUG")
  (doseq [sym ['sync 'assert 'future 'get 'compare]]
    (when-let [v (ns-resolve n sym)]
      (println "DEBUG" sym v
               (select-keys (meta v) [:ns :name :macro])))))

(defn failure-category [messages]
  (let [s (str/join " <- " messages)]
    (cond
      (re-find #"Unable to resolve symbol" s) "unresolved-local"
      (re-find #"Cannot assign to non-mutable" s) "missing-mutable-field"
      (re-find #"Can't find matching overloaded method|No matching method|Must hint overloaded method" s) "missing-type-hint"
      (re-find #"Parameter declaration missing|Call to clojure\.core/fn did not conform|Parameter declaration" s) "malformed-fn"
      (re-find #"ClassNotFoundException|Could not locate" s) "missing-dependency"
      (re-find #"Only long and double primitives supported" s) "invalid-primitive-hint"
      (re-find #"More than 20 params" s) "invalid-arity"
      (re-find #"No such var" s) "missing-var"
      (re-find #"No such namespace" s) "missing-namespace"
      :else "other")))

(try
  (binding [*ns* n
            *err* (java.io.StringWriter.)]
    (if (System/getenv "AUDIT_STEP")
      (doseq [[idx subform] (map-indexed vector (rest body-form))]
        (try
          (eval subform)
          (catch Throwable t
            (println "STEP-FAIL" idx (first subform)
                     (subs (pr-str subform) 0
                           (min 500 (count (pr-str subform)))))
            (throw t))))
      (eval body-form)))
  (println "PASS" source-file)
  (catch Throwable t
    (let [chain (take-while some? (iterate ex-cause t))
          messages (mapv ex-message chain)]
      (println "FAIL" source-file
               (failure-category messages)
               (pr-str messages)))
    (System/exit 1)))

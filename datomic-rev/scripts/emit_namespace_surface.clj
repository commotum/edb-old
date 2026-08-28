(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn fail! [message]
  (binding [*out* *err*]
    (println message))
  (System/exit 2))

(def self-test? (= "--self-test" (first *command-line-args*)))

(def namespace-name
  (when-not self-test?
    (or (some-> *command-line-args* first symbol)
        (fail! "usage: emit_namespace_surface.clj NAMESPACE|--self-test"))))

(def unstable-source-location-keys
  #{:file :line :column :end-line :end-column})

(def ^:dynamic *metadata-view*
  (case (or (System/getenv "DATOMIC_VAR_METADATA_VIEW") "exact")
    "exact" :exact
    "diagnostic" :diagnostic
    (fail! "DATOMIC_VAR_METADATA_VIEW must be exact or diagnostic")))

(def ^:dynamic *surface-namespace-name* namespace-name)

(def compiler-elide-meta-property "[:doc :file :line]")
(def compiler-elide-meta-keys [:doc :file :line])

(def observed-metadata-value-types (atom #{}))
(def semantic-metadata-proxy-observations (atom []))

(def fresh-empty-atom-factory-contract
  :fresh-empty-atom-factory-v1)

(def fresh-empty-atom-factory-namespace
  'clojure.tools.analyzer.passes.add-binding-atom)

(def fresh-empty-atom-factory-source-resource
  "clojure/tools/analyzer/passes/add_binding_atom.clj")

(def fresh-empty-atom-factory-source-sha256
  "6d97ead2cf4a0fd350b038b6bc6f65cc2f3ac924723473329d5a2b7543ae5d12")

(def fresh-empty-atom-factory-path
  [:var 'add-binding-atom :metadata
   {:map-key [:keyword nil "pass-info"]} :value
   {:map-key [:keyword nil "state"]} :value])

(defn assert-compiler-elide-meta-contract! []
  (when-not (= compiler-elide-meta-property
               (System/getProperty "clojure.compiler.elide-meta"))
    (throw
      (ex-info "runtime must pin clojure.compiler.elide-meta"
               {:expected compiler-elide-meta-property
                :actual (System/getProperty
                          "clojure.compiler.elide-meta")})))
  (when-not (= compiler-elide-meta-keys
               (:elide-meta *compiler-options*))
    (throw
      (ex-info "active Clojure compiler options do not match the artifact"
               {:expected compiler-elide-meta-keys
                :actual (:elide-meta *compiler-options*)}))))

(defn stable-type-name [value]
  (if (nil? value) "nil" (.getName (class value))))

(defn record-observed-type! [value]
  (swap! observed-metadata-value-types conj (stable-type-name value))
  value)

(defn map-key-path-segment [key]
  {:map-key
   (cond
     (keyword? key) [:keyword (namespace key) (name key)]
     (symbol? key) [:symbol (namespace key) (name key)]
     (string? key) [:string key]
     (char? key) [:character (int key)]
     (number? key) [:number (stable-type-name key) (str key)]
     (nil? key) [:nil]
     :else [:type (stable-type-name key)])})

(defn unsupported-metadata-value!
  ([value path]
   (unsupported-metadata-value! value path nil))
  ([value path reason]
  (throw
    (ex-info
      (str "unsupported Var metadata value at " (pr-str path)
           " (" (stable-type-name value) ")"
           (when reason (str ": " reason)))
      (cond-> {:kind :unsupported-var-metadata-value
               :path path
               :type (stable-type-name value)}
        reason (assoc :reason reason))))))

(defn non-static-declared-fields [value]
  (->> (.getDeclaredFields (class value))
       (remove #(java.lang.reflect.Modifier/isStatic
                  (.getModifiers ^java.lang.reflect.Field %)))
       (mapv (fn [^java.lang.reflect.Field field]
               [(.getName field) (.getName (.getType field))]))
       sort
       vec))

(defn input-stream-sha256 [input]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        buffer (byte-array 8192)]
    (loop []
      (let [read-count (.read ^java.io.InputStream input buffer)]
        (when (pos? read-count)
          (.update digest buffer 0 read-count)
          (recur))))
    (apply str (map #(format "%02x" (bit-and (int %) 0xff))
                    (.digest digest)))))

(defn approved-semantic-proxy-source-sha256 []
  (let [resource (io/resource fresh-empty-atom-factory-source-resource)]
    (when-not resource
      (throw (ex-info "approved semantic proxy source resource is missing"
                      {:resource fresh-empty-atom-factory-source-resource})))
    (with-open [input (io/input-stream resource)]
      (input-stream-sha256 input))))

(def ^:dynamic *semantic-metadata-proxy-source-auditor*
  approved-semantic-proxy-source-sha256)

(def ^:dynamic *semantic-proxy-child-expression-transform* identity)

(def ^:dynamic *semantic-proxy-temp-file-factory*
  (fn [prefix suffix]
    (java.io.File/createTempFile prefix suffix)))

(defn delete-temp-files! [files]
  (let [failures
        (reduce
          (fn [failures ^java.io.File file]
            (try
              (java.nio.file.Files/delete (.toPath file))
              failures
              (catch Throwable failure
                (conj failures [file failure]))))
          []
          files)]
    (when (seq failures)
      (let [cleanup-failure
            (ex-info "metadata proxy temporary-file cleanup failed"
                     {:paths (mapv (comp str first) failures)})]
        (doseq [[_ failure] failures]
          (.addSuppressed cleanup-failure failure))
        (throw cleanup-failure)))))

(def ^:dynamic *semantic-proxy-temp-file-cleaner* delete-temp-files!)

(defn isolated-fresh-empty-atom-factory-verification! [_value _path]
  ;; Re-resolve and exercise the metadata function in a clean process.  The
  ;; value itself cannot safely cross the process boundary, so the helper
  ;; verifies the one approved namespace/Var/path again from the same runtime
  ;; classpath.  This keeps a generated function class out of the stable
  ;; surface without accepting arbitrary IFn metadata.
  (let [java-executable
        (str (System/getProperty "java.home") java.io.File/separator
             "bin" java.io.File/separator "java")
        java-tmpdir (System/getProperty "java.io.tmpdir")
        _ (when (str/blank? java-tmpdir)
            (throw (ex-info "java.io.tmpdir must be nonblank"
                            {:java.io.tmpdir java-tmpdir})))
        expression
        (*semantic-proxy-child-expression-transform*
         (str
          "(do "
          "(clojure.core/when-not "
          " (clojure.core/= \"[:doc :file :line]\" "
          "  (java.lang.System/getProperty \"clojure.compiler.elide-meta\")) "
          " (throw (java.lang.IllegalStateException. \"compiler-property\"))) "
          "(clojure.core/when-not "
          " (clojure.core/= [:doc :file :line] "
          "  (:elide-meta clojure.core/*compiler-options*)) "
          " (throw (java.lang.IllegalStateException. \"compiler-options\"))) "
          "(clojure.core/when "
          " (.isBlank (java.lang.System/getProperty \"java.io.tmpdir\")) "
          " (throw (java.lang.IllegalStateException. \"java-tmpdir\"))) "
          "(binding [clojure.core/*out* (java.io.StringWriter.)] "
          "  (clojure.core/require 'clojure.tools.analyzer.passes.add-binding-atom)) "
          "(clojure.core/let [v (clojure.core/ns-resolve "
          " 'clojure.tools.analyzer.passes.add-binding-atom 'add-binding-atom) "
          " f (clojure.core/get-in (clojure.core/meta v) [:pass-info :state]) "
          " fields (clojure.core/remove "
          "  (clojure.core/fn [field] "
          "   (java.lang.reflect.Modifier/isStatic (.getModifiers field))) "
          "  (.getDeclaredFields (clojure.core/class f)))] "
          " (clojure.core/when-not "
          "  (clojure.core/and (clojure.core/instance? clojure.lang.IFn f) "
          "                    (clojure.core/empty? fields)) "
          "  (throw (java.lang.IllegalStateException. \"shape\"))) "
          " (clojure.core/let [a (f) b (f)] "
          "  (clojure.core/when-not "
          "   (clojure.core/and "
          "    (clojure.core/instance? clojure.lang.Atom a) "
          "    (clojure.core/instance? clojure.lang.Atom b) "
          "    (clojure.core/not (clojure.core/identical? a b)) "
          "    (clojure.core/= {} (clojure.core/deref a)) "
          "    (clojure.core/= {} (clojure.core/deref b))) "
          "   (throw (java.lang.IllegalStateException. \"behavior\"))))) "
          "(clojure.core/print \"DATOMIC_METADATA_PROXY_PASS\"))"))
        temp-files (atom [])
        primary-failure (atom nil)]
    (try
      (let [output-file (*semantic-proxy-temp-file-factory*
                          "datomic-metadata-proxy-out-" ".log")
            _ (swap! temp-files conj output-file)
            error-file (*semantic-proxy-temp-file-factory*
                         "datomic-metadata-proxy-err-" ".log")
            _ (swap! temp-files conj error-file)
            builder (doto
                      (ProcessBuilder.
                        ^"[Ljava.lang.String;"
                        (into-array
                          String
                           [java-executable
                           "-XX:+PerfDisableSharedMem"
                           "-Dclojure.main.report=stderr"
                           (str "-Djava.io.tmpdir=" java-tmpdir)
                           (str "-Dclojure.compiler.elide-meta="
                                compiler-elide-meta-property)
                           "-cp" (System/getProperty "java.class.path")
                           "clojure.main" "-e" expression]))
                      (.redirectOutput output-file)
                      (.redirectError error-file))
            environment (.environment builder)
            _ (doseq [variable ["JAVA_TOOL_OPTIONS" "JDK_JAVA_OPTIONS"
                                "_JAVA_OPTIONS" "BASH_ENV" "ENV"
                                "CLASSPATH" "JAVA_HOME"]]
                (.remove environment variable))
            process (.start builder)
            finished? (.waitFor process 30 java.util.concurrent.TimeUnit/SECONDS)]
        (when-not finished?
          (.destroyForcibly process)
          (.waitFor process 5 java.util.concurrent.TimeUnit/SECONDS))
        (let [output (slurp output-file)
              error-output (slurp error-file)]
          (when-not (and finished?
                         (zero? (.exitValue process))
                         (= "DATOMIC_METADATA_PROXY_PASS" (str/trim output))
                         (str/blank? error-output))
            (throw
              (ex-info "isolated fresh-empty-atom factory verification failed"
                       {:finished finished?
                        :exit (when finished? (.exitValue process))
                        :stdout output
                        :stderr error-output}))))
        fresh-empty-atom-factory-contract)
      (catch Throwable failure
        (reset! primary-failure failure)
        (throw failure))
      (finally
        (try
          (*semantic-proxy-temp-file-cleaner* @temp-files)
          (catch Throwable cleanup-failure
            (if-let [failure @primary-failure]
              (.addSuppressed failure cleanup-failure)
              (throw cleanup-failure))))))))

(def ^:dynamic *semantic-metadata-proxy-verifier*
  isolated-fresh-empty-atom-factory-verification!)

(def semantic-proxy-origin-property
  "datomic.surface.semantic-proxy-origin")

(def ^:dynamic *semantic-proxy-origin-expectation* nil)

(defn semantic-proxy-class-origin [value]
  (let [runtime-class (class value)
        class-resource-path
        (str "/" (str/replace (.getName runtime-class) "." "/") ".class")
        code-source
        (some-> runtime-class .getProtectionDomain .getCodeSource .getLocation str)
        class-resource (some-> runtime-class
                               (.getResource class-resource-path)
                               str)
        code-source-present (boolean code-source)
        class-resource-present (boolean class-resource)
        origin-kind
        (cond
          (and (not code-source-present) (not class-resource-present))
          :source-generated

          (and code-source-present class-resource-present)
          :classpath-bytecode

          :else :mixed)]
    (sorted-map
      :class-resource-present class-resource-present
      :code-source-present code-source-present
      :origin-kind origin-kind)))

(defn assert-semantic-proxy-origin! [value path]
  (let [expected-text (System/getProperty semantic-proxy-origin-property)
        expected (or *semantic-proxy-origin-expectation*
                     (case expected-text
                       "source-generated" :source-generated
                       "classpath-bytecode" :classpath-bytecode
                       (unsupported-metadata-value!
                         value path
                         (str semantic-proxy-origin-property
                              " must be source-generated or classpath-bytecode"))))
        origin (semantic-proxy-class-origin value)]
    (when (= :mixed (:origin-kind origin))
      (unsupported-metadata-value!
        value path (str "approved semantic proxy has mixed class origin "
                        (pr-str origin))))
    (when-not (= expected (:origin-kind origin))
      (unsupported-metadata-value!
        value path (str "approved semantic proxy origin is "
                        (pr-str (:origin-kind origin))
                        ", expected " (pr-str expected))))
    origin))

(defn approved-semantic-metadata-proxy? [value path]
  (and (= fresh-empty-atom-factory-namespace *surface-namespace-name*)
       (= fresh-empty-atom-factory-path path)
       (instance? clojure.lang.IFn value)))

(defn normalize-semantic-metadata-proxy [value path]
  (let [fields (non-static-declared-fields value)
        origin (assert-semantic-proxy-origin! value path)
        source-sha256
        (try
          (*semantic-metadata-proxy-source-auditor*)
          (catch Throwable failure
            (unsupported-metadata-value!
              value path (str "approved semantic proxy source audit failed: "
                              (ex-message failure)))))]
    (when (seq fields)
      (unsupported-metadata-value!
        value path (str "approved semantic proxy has captured fields "
                        (pr-str fields))))
    (when-not (= fresh-empty-atom-factory-source-sha256 source-sha256)
      (unsupported-metadata-value!
        value path (str "approved semantic proxy source SHA-256 is "
                        source-sha256)))
    (let [contract
          (try
            (*semantic-metadata-proxy-verifier* value path)
            (catch Throwable failure
              (unsupported-metadata-value!
                value path
                (str "approved semantic proxy verification failed: "
                     (ex-message failure)))))]
      (when-not (= fresh-empty-atom-factory-contract contract)
        (unsupported-metadata-value!
          value path (str "approved semantic proxy verifier returned "
                          (pr-str contract))))
      (swap! semantic-metadata-proxy-observations
             conj
             (sorted-map
               :contract contract
               :namespace (str *surface-namespace-name*)
               :origin-kind (:origin-kind origin)
               :class-resource-present (:class-resource-present origin)
               :code-source-present (:code-source-present origin)
               :path path
               :runtime-class (stable-type-name value)
               :source-sha256 source-sha256))
      [:semantic-proxy contract])))

(declare normalize-metadata-value)

(defn metadata-map-for-view [metadata]
  (if (= :diagnostic *metadata-view*)
    (apply dissoc metadata unstable-source-location-keys)
    metadata))

(defn normalize-object-metadata [value path]
  (when (instance? clojure.lang.IObj value)
    (let [stable-metadata (metadata-map-for-view (or (meta value) {}))]
      (when (seq stable-metadata)
        (normalize-metadata-value stable-metadata
                                  (conj path :object-metadata))))))

(defn normalized-number [value]
  (cond
    (ratio? value)
    [:ratio (str (numerator value)) (str (denominator value))]

    (instance? java.math.BigDecimal value)
    (if (= :exact *metadata-view*)
      [:decimal (stable-type-name value)
       (.toString ^java.math.BigDecimal value)]
      [:decimal (.toPlainString
                  (.stripTrailingZeros ^java.math.BigDecimal value))])

    (integer? value)
    (if (= :exact *metadata-view*)
      [:integer (stable-type-name value) (str value)]
      [:integer (str value)])

    (or (instance? Double value) (instance? Float value))
    (let [encoded (cond
                    (Double/isNaN (double value)) "NaN"
                    (= Double/POSITIVE_INFINITY (double value)) "+Infinity"
                    (= Double/NEGATIVE_INFINITY (double value)) "-Infinity"
                    :else (Double/toHexString (double value)))]
      (if (= :exact *metadata-view*)
        [:floating-point (stable-type-name value) encoded]
        [:floating-point encoded]))

    :else
    (unsupported-metadata-value! value [:number])))

(defn normalize-map [value path]
  (let [own-metadata (normalize-object-metadata value path)
        entries
        (->> value
             (map (fn [[key item]]
                    (let [key-segment (map-key-path-segment key)]
                      [(normalize-metadata-value
                         key (conj path key-segment :key))
                       (normalize-metadata-value
                         item (conj path key-segment :value))])))
             (sort-by (fn [[key item]] [(pr-str key) (pr-str item)]))
             vec)]
    [(if (record? value) :record :map)
     (when (record? value) (stable-type-name value))
     own-metadata
     entries]))

(defn normalize-metadata-value [raw-value path]
  (let [value (record-observed-type! raw-value)]
    (cond
      (nil? value) [:nil]
      (boolean? value) [:boolean value]
      (string? value) [:string value]
      (char? value) [:character (int value)]
      (keyword? value) [:keyword (namespace value) (name value)]
      (number? value) (normalized-number value)

      (symbol? value)
      [:symbol (namespace value) (name value)
       (normalize-object-metadata value path)]

      (instance? clojure.lang.Namespace value)
      [:namespace (str (ns-name value))]

      (var? value)
      (let [{var-ns :ns var-name :name} (meta value)]
        (when-not (and (instance? clojure.lang.Namespace var-ns)
                       (symbol? var-name))
          (unsupported-metadata-value! value path))
        [:var (str (ns-name var-ns)) (name var-name)])

      (class? value)
      [:class (.getName ^Class value)]

      (instance? java.util.regex.Pattern value)
      [:regular-expression
       (.pattern ^java.util.regex.Pattern value)
       (.flags ^java.util.regex.Pattern value)]

      (instance? java.util.UUID value)
      [:uuid (str value)]

      (= java.util.Date (class value))
      [:instant (.getTime ^java.util.Date value)]

      (instance? java.net.URI value)
      [:uri (str value)]

      (map? value)
      (normalize-map value path)

      (vector? value)
      [:vector
       (normalize-object-metadata value path)
       (mapv (fn [index item]
               (normalize-metadata-value item (conj path index)))
             (range) value)]

      (set? value)
      [:set
       (normalize-object-metadata value path)
       (->> value
            (map-indexed
              (fn [index item]
                (normalize-metadata-value item (conj path [:set index]))))
            (sort-by pr-str)
            vec)]

      (seq? value)
      [:list
       (normalize-object-metadata value path)
       (mapv (fn [index item]
               (normalize-metadata-value item (conj path index)))
             (range) value)]

      (approved-semantic-metadata-proxy? value path)
      (normalize-semantic-metadata-proxy value path)

      :else
      (unsupported-metadata-value! value path))))

(defn normalized-var-metadata [sym v]
  (normalize-metadata-value
    (metadata-map-for-view (meta v))
    [:var sym :metadata]))

(defn stable-var-reference [value path]
  (let [{var-ns :ns var-name :name} (meta value)]
    (when-not (and (instance? clojure.lang.Namespace var-ns)
                   (symbol? var-name))
      (unsupported-metadata-value! value path))
    [(str (ns-name var-ns)) (name var-name)]))

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
        (var? root) (let [[target-ns target-name]
                          (stable-var-reference root [:root])]
                      [:var-root target-ns target-name])
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
     (cond-> (sorted-map :metadata (normalized-var-metadata sym v)
                         :root root)
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

(defn proxy-class-var-exclusion [[sym v]]
  (sorted-map
    :namespace (str *surface-namespace-name*)
    :root-class (.getName ^Class @v)
    :symbol (str sym)))

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

(defrecord MetadataEncoderFixtureRecord [value])

(defn contains-normalized-node? [normalized node]
  (boolean (some #(= node %) (tree-seq coll? seq normalized))))

(defn run-metadata-encoder-self-test! []
  (let [tagged-symbol (with-meta 'argument
                        {:tag 'String
                         :line 91
                         :column 7})
        tagged-vector (with-meta [tagged-symbol false nil]
                        {:audit :vector
                         :end-line 92})
        tagged-list (with-meta (list :first :second)
                      {:audit :list
                       :file "unstable-list-location.clj"})
        tagged-map (with-meta (array-map :z 3 :a 1)
                     {:audit :map
                      :line 93})
        tagged-set (with-meta #{:z :a}
                     {:audit :set
                      :column 4
                      :end-column 9})
        fixture-value
        {:nil nil
         :boolean true
         :string "text"
         :character \x
         :keyword :qualified/key
         :ratio 2/3
         :decimal 1.20M
         :integer 42
         :floating 1.5
         :symbol tagged-symbol
         :namespace (the-ns 'clojure.core)
         :var #'clojure.core/+
         :class String
         :regular-expression (java.util.regex.Pattern/compile "a+" 2)
         :uuid (java.util.UUID/fromString
                 "123e4567-e89b-12d3-a456-426614174000")
         :instant (java.util.Date. 123456789)
         :uri (java.net.URI. "https://example.invalid/path")
         :map tagged-map
         :record (->MetadataEncoderFixtureRecord :value)
         :vector tagged-vector
         :set tagged-set
         :list tagged-list}
        exact-a (binding [*metadata-view* :exact]
                  (normalize-metadata-value fixture-value [:self-test]))
        exact-b (binding [*metadata-view* :exact]
                  (normalize-metadata-value
                    (into (array-map) (reverse (seq fixture-value)))
                    [:self-test]))
        diagnostic-a (binding [*metadata-view* :diagnostic]
                       (normalize-metadata-value fixture-value [:self-test]))
        diagnostic-b (binding [*metadata-view* :diagnostic]
                       (normalize-metadata-value
                         (into (array-map) (reverse (seq fixture-value)))
                         [:self-test]))
        location-nodes
        #{[:keyword nil "file"]
          [:keyword nil "line"]
          [:keyword nil "column"]
          [:keyword nil "end-line"]
          [:keyword nil "end-column"]}]
    (assert (= exact-a exact-b))
    (assert (= diagnostic-a diagnostic-b))
    (doseq [expected-node
            [[:nil]
             [:boolean true]
             [:string "text"]
             [:character 120]
             [:keyword "qualified" "key"]
             [:ratio "2" "3"]
             [:decimal "java.math.BigDecimal" "1.20"]
             [:integer "java.lang.Long" "42"]
             [:floating-point "java.lang.Double" "0x1.8p0"]
             [:namespace "clojure.core"]
             [:var "clojure.core" "+"]
             [:class "java.lang.String"]
             [:regular-expression "a+" 2]
             [:uuid "123e4567-e89b-12d3-a456-426614174000"]
             [:instant 123456789]
             [:uri "https://example.invalid/path"]]]
      (assert (contains-normalized-node? exact-a expected-node)
              (str "metadata encoder missed supported branch " expected-node)))
    (assert (contains-normalized-node? diagnostic-a [:decimal "1.2"]))
    (assert (contains-normalized-node? diagnostic-a [:integer "42"]))
    (assert (contains-normalized-node? diagnostic-a
                                       [:floating-point "0x1.8p0"]))
    (doseq [collection-tag [:map :record :vector :set :list]]
      (assert (some #(and (vector? %) (= collection-tag (first %)))
                    (tree-seq coll? seq exact-a))
              (str "metadata encoder missed collection branch " collection-tag)))
    (doseq [location-node location-nodes]
      (assert (contains-normalized-node? exact-a location-node)
              (str "exact metadata encoder dropped source location " location-node))
      (assert (not (contains-normalized-node? diagnostic-a location-node))
              (str "diagnostic metadata encoder retained source location " location-node)))
    (assert (contains-normalized-node? exact-a
                                       [:keyword nil "audit"]))
    (assert (contains-normalized-node? diagnostic-a
                                       [:keyword nil "audit"]))
    (let [compile-fixture
          (fn [options]
            (let [fixture-ns
                  (symbol (str "metadata.encoder.compiler.option." (gensym)))
                  fixture-symbol
                  (with-meta 'compiled-value
                    {:doc "must-elide"
                     :file "must-elide.clj"
                     :line (int 7)
                     :column (int 11)
                     :pass-info {:walk :pre}})]
              (try
                (binding [*ns* (create-ns fixture-ns)
                          *compiler-options* options]
                  (clojure.core/refer 'clojure.core)
                  (eval (list 'def fixture-symbol :value))
                  (meta (ns-resolve *ns* 'compiled-value)))
                (finally
                  (when (find-ns fixture-ns) (remove-ns fixture-ns))))))
          elided (compile-fixture {:elide-meta compiler-elide-meta-keys})
          unsealed (compile-fixture {})]
      (assert (not-any? #(contains? elided %)
                        compiler-elide-meta-keys))
      (assert (contains? elided :column))
      (assert (instance? Integer (:column elided)))
      (assert (= {:walk :pre} (:pass-info elided)))
      (assert (every? #(contains? unsealed %)
                      compiler-elide-meta-keys)))
    (let [target-ns (symbol (str "metadata.encoder.var.target." (gensym)))
          alias-ns (symbol (str "metadata.encoder.var.alias." (gensym)))]
      (try
        (let [target-a (clojure.lang.RT/var (str target-ns) "a")
              target-b (clojure.lang.RT/var (str target-ns) "b")
              aliases (create-ns alias-ns)
              alias-a (intern aliases 'alias-a target-a)
              alias-b (intern aliases 'alias-b target-b)]
          (assert (= :unbound (root-kind target-a)))
          (assert (= [:var-root (str target-ns) "a"]
                     (root-kind alias-a)))
          (assert (= [:var-root (str target-ns) "b"]
                     (root-kind alias-b)))
          (assert (= (class target-a) (class target-b)))
          (assert (not= (root-kind alias-a) (root-kind alias-b))))
        (finally
          (when (find-ns alias-ns) (remove-ns alias-ns))
          (when (find-ns target-ns) (remove-ns target-ns)))))
    (let [proxy-ns (symbol (str "metadata.encoder.proxy.fixture." (gensym)))]
      (try
        (let [n (create-ns proxy-ns)]
          (binding [*ns* n]
            (clojure.core/refer 'clojure.core)
            (eval '(proxy [java.io.OutputStream] []
                     (write [byte-value] nil)))
            (eval '(proxy [java.io.InputStream] []
                     (read [] -1))))
          (let [entries (->> (ns-interns n)
                             (filter compiler-generated-proxy-class-var?)
                             (sort-by (comp str first))
                             vec)
                ledgers
                (binding [*surface-namespace-name* proxy-ns]
                  (mapv proxy-class-var-exclusion entries))
                runtime-class-names
                (mapv (fn [[_ v]] (.getName ^Class @v)) entries)]
            (assert (= 2 (count entries)))
            (assert (= 2 (count (distinct runtime-class-names))))
            (assert (= runtime-class-names (mapv :root-class ledgers)))
            (assert (not-any? #{"java.lang.Class"}
                              (map :root-class ledgers)))
            (assert (every? #(str/includes? % ".proxy$")
                            (map :root-class ledgers)))
            (assert (= #{(str proxy-ns)}
                       (set (map :namespace ledgers))))))
        (finally
          (when (find-ns proxy-ns) (remove-ns proxy-ns)))))
    (let [direct-verifier
          (fn [factory _path]
            (let [first-result (factory)
                  second-result (factory)]
              (when (and (instance? clojure.lang.Atom first-result)
                         (instance? clojure.lang.Atom second-result)
                         (not (identical? first-result second-result))
                         (= {} @first-result)
                         (= {} @second-result))
                fresh-empty-atom-factory-contract)))
          valid-factory (fn [] (atom {}))
          invalid-factory (fn [] (atom {:not-empty true}))
          same-class-factory (fn [result] (fn [] result))
          opaque-a (same-class-factory :a)
          opaque-b (same-class-factory :b)
          assert-unsupported!
          (fn [value path]
            (try
              (normalize-metadata-value value path)
              (assert false "opaque function metadata unexpectedly normalized")
              (catch clojure.lang.ExceptionInfo failure
                (assert (= :unsupported-var-metadata-value
                           (:kind (ex-data failure)))))))]
      (assert (= (class opaque-a) (class opaque-b)))
      (assert (not= opaque-a opaque-b))
      (binding [*surface-namespace-name*
                fresh-empty-atom-factory-namespace
                *semantic-proxy-origin-expectation* :source-generated
                *semantic-metadata-proxy-verifier* direct-verifier
                *semantic-metadata-proxy-source-auditor*
                (constantly fresh-empty-atom-factory-source-sha256)]
        (assert (= [:semantic-proxy fresh-empty-atom-factory-contract]
                   (normalize-metadata-value
                     valid-factory fresh-empty-atom-factory-path)))
        (assert-unsupported! invalid-factory
                             fresh-empty-atom-factory-path)
        (assert-unsupported!
          opaque-a
          (assoc fresh-empty-atom-factory-path 1 'not-add-binding-atom))
        (assert-unsupported!
          opaque-b
          (conj fresh-empty-atom-factory-path :lookalike))))
    (try
      (normalize-metadata-value
        (Object.)
        [:self-test {:map-key [:keyword nil "opaque"]} :value])
      (assert false "opaque metadata value unexpectedly normalized")
      (catch clojure.lang.ExceptionInfo failure
        (assert (= :unsupported-var-metadata-value
                   (:kind (ex-data failure))))
        (assert (= "java.lang.Object" (:type (ex-data failure))))
        (assert (= [:self-test
                    {:map-key [:keyword nil "opaque"]}
                    :value]
                   (:path (ex-data failure))))))
    (let [assert-child-failure!
          (fn [expression-transform stderr-marker]
            (let [created (atom [])
                  failure
                  (try
                    (binding
                      [*semantic-proxy-child-expression-transform*
                       expression-transform
                       *semantic-proxy-temp-file-factory*
                       (fn [prefix suffix]
                         (let [file (java.io.File/createTempFile prefix suffix)]
                           (swap! created conj file)
                           file))]
                      (isolated-fresh-empty-atom-factory-verification!
                        nil [:self-test :child]))
                    nil
                    (catch clojure.lang.ExceptionInfo failure failure))]
              (assert failure "bad child process unexpectedly passed")
              (assert (str/includes? (or (:stderr (ex-data failure)) "")
                                     stderr-marker))
              (assert (= 2 (count @created)))
              (assert (not-any? #(.exists ^java.io.File %) @created))))]
      (assert-child-failure!
        (constantly
          "(do (.println java.lang.System/err \"CHILD_ERROR_MARKER\") (throw (java.lang.IllegalStateException. \"child-error\")))")
        "CHILD_ERROR_MARKER")
      (assert-child-failure!
        (constantly
          "(do (.println java.lang.System/err \"NOISY_STDERR_MARKER\") (print \"DATOMIC_METADATA_PROXY_PASS\"))")
        "NOISY_STDERR_MARKER"))
    (let [created (atom [])
          calls (atom 0)
          failure
          (try
            (binding
              [*semantic-proxy-temp-file-factory*
               (fn [prefix suffix]
                 (if (= 1 (swap! calls inc))
                   (let [file (java.io.File/createTempFile prefix suffix)]
                     (swap! created conj file)
                     file)
                   (throw (ex-info "SECOND_TEMP_CREATE_FAILURE" {}))))]
              (isolated-fresh-empty-atom-factory-verification!
                nil [:self-test :second-temp-create]))
            nil
            (catch clojure.lang.ExceptionInfo failure failure))]
      (assert (= "SECOND_TEMP_CREATE_FAILURE" (ex-message failure)))
      (assert (= 1 (count @created)))
      (assert (not-any? #(.exists ^java.io.File %) @created)))
    (let [failure
          (try
            (binding
              [*semantic-proxy-child-expression-transform*
               (constantly
                 "(throw (java.lang.IllegalStateException. \"PRIMARY_CHILD_FAILURE\"))")
               *semantic-proxy-temp-file-cleaner*
               (fn [files]
                 (delete-temp-files! files)
                 (throw (ex-info "SECONDARY_CLEANUP_FAILURE" {})))]
              (isolated-fresh-empty-atom-factory-verification!
                nil [:self-test :primary-and-cleanup]))
            nil
            (catch clojure.lang.ExceptionInfo failure failure))]
      (assert (= "isolated fresh-empty-atom factory verification failed"
                 (ex-message failure)))
      (assert (= ["SECONDARY_CLEANUP_FAILURE"]
                 (mapv ex-message (.getSuppressed failure)))))
    (let [directory-path
          (java.nio.file.Files/createTempDirectory
            "datomic-metadata-proxy-cleanup-negative-"
            (make-array java.nio.file.attribute.FileAttribute 0))
          child-path
          (java.nio.file.Files/createTempFile
            directory-path "child-" ".tmp"
            (make-array java.nio.file.attribute.FileAttribute 0))]
      (try
        (let [failure
              (try
                (delete-temp-files! [(.toFile directory-path)])
                nil
                (catch clojure.lang.ExceptionInfo failure failure))]
          (assert failure "nonempty temporary directory unexpectedly deleted")
          (assert (= [(str (.toFile directory-path))]
                     (:paths (ex-data failure))))
          (assert (java.nio.file.Files/exists
                    directory-path
                    (make-array java.nio.file.LinkOption 0))))
        (finally
          (java.nio.file.Files/delete child-path)
          (java.nio.file.Files/delete directory-path))))
    (println "NAMESPACE_METADATA_ENCODER_SELF_TEST_PASS")))

(if self-test?
  (run-metadata-encoder-self-test!)
  (try
    (assert-compiler-elide-meta-contract!)
    (binding [*out* (java.io.StringWriter.)]
      (require namespace-name))
    (let [n (the-ns namespace-name)
          interns (ns-interns n)
          proxy-exclusions (->> interns
                                (filter compiler-generated-proxy-class-var?)
                                (mapv proxy-class-var-exclusion))]
      (prn {:vars (into (sorted-map)
                        (map var-surface)
                        (remove compiler-generated-proxy-class-var?
                                interns))
            :classes (into (sorted-map)
                           (map class-surface)
                           (namespace-classes n))})
      (binding [*out* *err*]
        (println "VAR_SURFACE_PROXY_EXCLUSIONS" (pr-str proxy-exclusions)))
      (binding [*out* *err*]
        (println "VAR_METADATA_SEMANTIC_PROXIES"
                 (pr-str @semantic-metadata-proxy-observations)))
      (when (= "1" (System/getenv "DATOMIC_VAR_METADATA_EMIT_TYPES"))
        (binding [*out* *err*]
          (println "VAR_METADATA_VALUE_TYPES"
                   (str/join "," (sort @observed-metadata-value-types))))))
    (catch Throwable failure
      (let [chain (take-while some? (iterate ex-cause failure))
            metadata-failure
            (some #(when (= :unsupported-var-metadata-value
                            (:kind (ex-data %)))
                     (ex-data %))
                  chain)]
        (binding [*out* *err*]
          (when metadata-failure
            (println "VAR_METADATA_FAIL"
                     (pr-str (select-keys metadata-failure
                                          [:kind :path :type :reason]))))
          (println "FAIL" namespace-name
                   (pr-str (mapv ex-message chain)))))
      (System/exit 1))))

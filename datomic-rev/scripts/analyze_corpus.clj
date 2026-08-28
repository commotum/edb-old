(require '[clojure.java.io :as io]
         '[clojure.pprint :as pprint]
         '[clojure.repl :as repl]
         '[clojure.set :as set]
         '[clojure.string :as str])
(import '(java.security MessageDigest)
        '(java.util.zip ZipFile))

(defn fail! [message]
  (binding [*out* *err*] (println message))
  (System/exit 2))

(defn sha256 [file]
  (let [digest (MessageDigest/getInstance "SHA-256")
        buffer (byte-array 65536)]
    (with-open [stream (io/input-stream file)]
      (loop []
        (let [n (.read stream buffer)]
          (when (pos? n)
            (.update digest buffer 0 n)
            (recur)))))
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) (.digest digest)))))

(defn relative-path [root file]
  (-> (.toPath (.getCanonicalFile (io/file root)))
      (.relativize (.toPath (.getCanonicalFile (io/file file))))
      str
      (str/replace java.io.File/separator "/")))

(defn reader-symbol [form]
  (cond
    (symbol? form) form
    (and (seq? form) (= 'quote (first form)) (symbol? (second form))) (second form)
    :else nil))

(defn qualify-lib [prefix target]
  (symbol (if prefix (str prefix "." target) (str target))))

(defn require-spec-aliases
  ([spec] (require-spec-aliases spec nil))
  ([spec prefix]
   (if (and (sequential? spec) (reader-symbol (first spec)))
     (let [target (qualify-lib prefix (reader-symbol (first spec)))
           tail (vec (rest spec))
           alias-position (or (let [position (.indexOf ^java.util.List tail :as)]
                                (when (not= -1 position) position))
                              (let [position (.indexOf ^java.util.List tail :as-alias)]
                                (when (not= -1 position) position)))
           alias-symbol (when (and alias-position (< (inc alias-position) (count tail)))
                          (reader-symbol (nth tail (inc alias-position))))
           nested (filter sequential? tail)]
       (cond-> (apply merge {} (map #(require-spec-aliases % target) nested))
         alias-symbol (assoc alias-symbol target)))
     {})))

(defn ns-reader-context [form]
  (when (and (seq? form)
             (symbol? (first form))
             (= "ns" (name (first form)))
             (reader-symbol (second form)))
    (let [namespace-symbol (reader-symbol (second form))
          aliases (->> (drop 2 form)
                       (filter #(and (seq? %) (= :require (first %))))
                       (mapcat rest)
                       (map require-spec-aliases)
                       (apply merge {}))]
      {:namespace namespace-symbol :aliases aliases})))

(defn source-reader-resolver [namespace-symbol aliases]
  (reify clojure.lang.LispReader$Resolver
    (currentNS [_] namespace-symbol)
    (resolveAlias [_ alias-symbol] (get aliases alias-symbol))
    (resolveClass [_ _] nil)
    (resolveVar [_ symbol-value]
      (if-let [symbol-namespace (namespace symbol-value)]
        (if-let [target (get aliases (symbol symbol-namespace))]
          (symbol (str target) (name symbol-value))
          symbol-value)
        (if (ns-resolve 'clojure.core symbol-value)
          (symbol "clojure.core" (name symbol-value))
          (symbol (str namespace-symbol) (name symbol-value)))))))

(defn read-source [file]
  (binding [*read-eval* false]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader. (io/reader file))]
      (let [eof (Object.)
            first-form (read {:eof eof} reader)
            reader-context (when-not (identical? eof first-form)
                             (ns-reader-context first-form))
            resolver (when reader-context
                       (source-reader-resolver (:namespace reader-context)
                                               (:aliases reader-context)))
            forms (if (identical? eof first-form)
                    []
                    (binding [*reader-resolver* resolver]
                      (loop [result [first-form]]
                        (let [form (read {:eof eof} reader)]
                          (if (identical? eof form)
                            result
                            (recur (conj result form)))))))]
        ;; Decompiled namespaces are emitted as one encompassing `do` form,
        ;; while handwritten/reused sources use ordinary, multiple top-level
        ;; forms.  Normalize both shapes without nesting the decompiled form,
        ;; because downstream definition discovery operates on (rest top).
        (if (and (= 1 (count forms))
                 (seq? (first forms))
                 (= 'do (ffirst forms)))
          (first forms)
          (cons 'do forms))))))

(defn nodes [form]
  (tree-seq #(or (seq? %) (vector? %) (map? %) (set? %)) seq form))

(defn quoted-value [form]
  (if (and (seq? form) (= 'quote (first form)))
    (second form)
    form))

(defn quoted-symbol [form]
  (let [value (quoted-value form)]
    (cond
      (symbol? value) value
      (and (seq? value) (= '.withMeta (first value))) (quoted-symbol (second value))
      :else nil)))

(defn form-head [form]
  (when (seq? form) (first form)))

(defn head-name [form]
  (let [head (form-head form)]
    (when (symbol? head) (name head))))

(defn find-forms [form wanted]
  (filter #(and (seq? %) (contains? wanted (head-name %))) (nodes form)))

(defn top-level-forms [top]
  (if (= "do" (head-name top)) (rest top) [top]))

(defn namespace-clauses [top clause-key]
  (for [form (top-level-forms top)
        :when (= "ns" (head-name form))
        clause (drop 2 form)
        :when (and (seq? clause) (= clause-key (first clause)))]
    clause))

(defn source-namespace [top]
  ;; Namespace declarations are executable top-level forms.  Searching the
  ;; entire tree admits examples inside `(comment ...)` and can silently
  ;; assign a source file to the wrong namespace.
  (some (fn [form]
          (when (contains? #{"ns" "in-ns"} (head-name form))
            (some-> form second quoted-symbol str)))
        (top-level-forms top)))

(defn namespace-doc [top]
  (some (fn [node]
          (when (and (map? node) (string? (:doc node))) (:doc node)))
        (nodes top)))

(defn vector-option [spec option]
  (let [position (.indexOf ^java.util.List spec option)]
    (when (and (not= -1 position) (< (inc position) (count spec)))
      (nth spec (inc position)))))

(defn symbols-in [form]
  (->> (nodes form)
       (map quoted-value)
       (filter symbol?)
       set))

(defn parse-require-spec [spec]
  (cond
    (quoted-symbol spec)
    {:namespace (str (quoted-symbol spec))}

    (vector? spec)
    (when-let [target (quoted-symbol (first spec))]
      (let [alias (some-> (vector-option spec :as) quoted-symbol str)
            referred (some->> (vector-option spec :refer) symbols-in (map name) sort vec)]
        (cond-> {:namespace (str target)}
          alias (assoc :alias alias)
          (seq referred) (assoc :refer referred))))

    :else nil))

(defn parse-requires [top]
  (->> (concat (find-forms top #{"require"})
               (namespace-clauses top :require))
       (mapcat rest)
       (keep parse-require-spec)
       (remove #(= "clojure.core" (:namespace %)))
       distinct
       (sort-by (juxt :namespace :alias))
       vec))

(defn parse-imports [top]
  (->> (find-forms top #{"import" "import*"})
       (mapcat rest)
       (keep quoted-symbol)
       (map str)
       distinct
       sort
       vec))

(defn literal-metadata [form]
  (cond
    (map? form) form

    (and (seq? form) (= "assoc" (head-name form)))
    (let [[_ base & entries] form
          base-map (literal-metadata base)]
      (when (and base-map
                 (even? (count entries))
                 (every? keyword? (take-nth 2 entries)))
        (reduce (fn [result [key value]] (assoc result key (quoted-value value)))
                base-map
                (partition 2 entries))))

    :else nil))

(defn metadata-overrides [top]
  (reduce
    (fn [result form]
      (let [[_ target metadata] form
            var-name (when (and (seq? target) (= 'var (first target))) (second target))
            metadata (literal-metadata metadata)]
        (if (and (symbol? var-name) metadata)
          (update result (name var-name) merge metadata)
          result)))
    {}
    (find-forms top #{"reset-meta!"})))

(def definition-heads
  #{"def" "defonce" "defn" "defn-" "defmacro" "defmulti" "defmethod"
    "defprotocol" "definterface" "defrecord" "deftype" "declare"})

(defn defn-arglists [form]
  (let [tail (drop 2 form)
        tail (if (string? (first tail)) (rest tail) tail)
        tail (if (map? (first tail)) (rest tail) tail)]
    (cond
      (vector? (first tail)) [(first tail)]
      :else (->> tail
                 (filter seq?)
                 (map first)
                 (filter vector?)
                 vec))))

(defn definition-doc [form]
  (let [candidate (nth form 2 nil)]
    (when (string? candidate) candidate)))

(defn protocol-methods [form]
  (->> (drop 2 form)
       (filter seq?)
       (filter #(symbol? (first %)))
       (map (fn [method]
              {:name (name (first method))
               :arglists (->> (rest method) (filter vector?) (mapv pr-str))}))
       vec))

(defn type-info [kind form]
  (when (contains? #{"defrecord" "deftype"} kind)
    (let [body (drop 3 form)]
      {:fields (mapv str (filter symbol? (nth form 2 [])))
       :interfaces (->> body
                        (take-while #(not (seq? %)))
                        (filter symbol?)
                        (map str)
                        vec)})))

(defn parse-definition [namespace-name overrides form]
  (let [kind (head-name form)
        symbol-name (when (seq? form) (second form))]
    (when (and kind (contains? definition-heads kind) (symbol? symbol-name))
      (let [name-string (name symbol-name)
            override (get overrides name-string)
            private? (boolean (or (= "defn-" kind)
                                  (:private (meta symbol-name))
                                  (:private override)))]
        (cond-> {:namespace namespace-name
                 :name name-string
                 :kind kind
                 :line (or (:line (meta form)) 0)
                 :visibility (if private? "private" "public")
                 :form form}
          (contains? #{"defn" "defn-" "defmacro"} kind)
          (assoc :arglists (mapv pr-str (defn-arglists form)))

          (or (definition-doc form) (:doc override))
          (assoc :doc (or (definition-doc form) (:doc override)))

          (contains? #{"defprotocol" "definterface"} kind)
          (assoc :methods (protocol-methods form))

          (type-info kind form)
          (merge (type-info kind form)))))))

(defn keyword-counts [top]
  (->> (nodes top)
       (filter keyword?)
       frequencies
       (map (fn [[keyword-value count]] [(str keyword-value) count]))
       (into (sorted-map))))

(defn interesting-string-kind [value]
  (cond
    (re-find #"(?i)\b(select|insert|update|delete|create[ ]+table|alter[ ]+table|drop[ ]+table)\b" value) "sql"
    (re-find #"(?i)^(https?://|jdbc:|datomic:)" value) "uri"
    (re-find #"(?i)(datomic\.|aws\.|ddb\.|cassandra|memcached|infinispan|couchbase)" value) "configuration"
    :else nil))

(defn interesting-strings [top]
  (->> (nodes top)
       (filter string?)
       frequencies
       (keep (fn [[value count]]
               (when-let [kind (interesting-string-kind value)]
                 {:kind kind :value value :count count})))
       (sort-by (juxt :kind :value))
       vec))

(defn decompilation-gaps [form]
  (->> (nodes form)
       (filter string?)
       (filter #(str/starts-with? % "BROKEN DECOMP "))
       distinct
       sort
       vec))

(defn aliases-for [namespace-entry]
  (into {} (keep (fn [{:keys [namespace alias]}] (when alias [alias namespace]))
                 (:requires namespace-entry))))

(defn refers-for [namespace-entry]
  (into {} (mapcat (fn [{:keys [namespace refer]}]
                     (map (fn [var-name] [var-name namespace]) refer))
                   (:requires namespace-entry))))

(defn resolve-var [registry namespace-entry symbol-value]
  (let [namespace-name (:namespace namespace-entry)
        symbol-ns (namespace symbol-value)
        symbol-name (name symbol-value)
        aliases (aliases-for namespace-entry)
        refers (refers-for namespace-entry)
        target-ns (cond
                    symbol-ns (get aliases symbol-ns symbol-ns)
                    (contains? (get registry namespace-name #{}) symbol-name) namespace-name
                    :else (get refers symbol-name))]
    (when (and target-ns (contains? (get registry target-ns #{}) symbol-name))
      [target-ns symbol-name])))

(defn call-heads [form]
  (->> (nodes form)
       (filter seq?)
       (map first)
       (filter symbol?)))

(defn qualified-symbols [form]
  (->> (nodes form)
       (filter symbol?)
       (filter namespace)))

(defn definition-edges [registry namespace-entry definition]
  (let [source [(:namespace definition) (:name definition)]
        calls (->> (call-heads (:form definition))
                   (keep #(resolve-var registry namespace-entry %))
                   (remove #{source})
                   frequencies)
        references (->> (qualified-symbols (:form definition))
                        (keep #(resolve-var registry namespace-entry %))
                        (remove #{source})
                        frequencies)]
    {:calls calls :references references}))

(defn java-class->clojure-namespace [class-name recovered-namespaces]
  (let [without-class (str/replace class-name #"\.class$" "")
        base (first (str/split without-class #"\$"))
        base (str/replace base #"__init$" "")
        direct (str/replace base "/" ".")
        parent (some-> base (str/replace #"/[^/]+$" "") (str/replace "/" "."))
        demunged-direct (repl/demunge direct)
        demunged-parent (some-> parent repl/demunge)]
    (cond
      (contains? recovered-namespaces direct) direct
      (contains? recovered-namespaces demunged-direct) demunged-direct
      (contains? recovered-namespaces parent) parent
      (contains? recovered-namespaces demunged-parent) demunged-parent
      :else nil)))

(defn peer-inventory [peer-jar recovered-namespaces]
  (with-open [archive (ZipFile. (io/file peer-jar))]
    (let [entries (->> (enumeration-seq (.entries archive))
                       (remove #(.isDirectory ^java.util.zip.ZipEntry %))
                       (map #(.getName ^java.util.zip.ZipEntry %))
                       sort
                       vec)
          classes (filterv #(str/ends-with? % ".class") entries)
          mapped (group-by #(java-class->clojure-namespace % recovered-namespaces) classes)]
      {:sha256 (sha256 peer-jar)
       :entry-count (count entries)
       :class-count (count classes)
       :mapped-class-count (reduce + (map count (vals (dissoc mapped nil))))
       :unmapped-classes (vec (get mapped nil))
       :classes-by-namespace (into (sorted-map)
                                   (map (fn [[namespace-name values]]
                                          [namespace-name (count values)]))
                                   (dissoc mapped nil))})))

(defn tsv-cell [value]
  (-> (str (or value ""))
      (str/replace "\t" " ")
      (str/replace "\r" " ")
      (str/replace "\n" "\\n")))

(defn write-tsv [path header rows]
  (io/make-parents path)
  (spit path
        (str (str/join "\t" header) "\n"
             (str/join "\n" (map #(str/join "\t" (map tsv-cell %)) rows))
             "\n")))

(defn write-edn [path value]
  (io/make-parents path)
  (spit path (with-out-str (pprint/pprint value))))

(let [[source-root peer-jar output-root] *command-line-args*]
  (when-not (and source-root peer-jar output-root)
    (fail! "usage: analyze_corpus.clj SOURCE_ROOT PEER_JAR OUTPUT_ROOT"))
  (doseq [path [source-root peer-jar]]
    (when-not (.exists (io/file path)) (fail! (str "missing input: " path))))

  (let [source-files (->> (file-seq (io/file source-root))
                          (filter #(.isFile ^java.io.File %))
                          (filter #(str/ends-with? (.getName ^java.io.File %) ".clj"))
                          (remove #(= "data_readers.clj" (.getName ^java.io.File %)))
                          (sort-by #(.getCanonicalPath ^java.io.File %)))
        raw-entries
        (mapv (fn [file]
                (let [top (read-source file)
                      namespace-name (source-namespace top)
                      _ (when-not namespace-name
                          (fail! (str "missing top-level namespace declaration: "
                                      (.getCanonicalPath ^java.io.File file))))
                      overrides (metadata-overrides top)
                      definitions (->> (rest top)
                                       (keep #(parse-definition namespace-name overrides %))
                                       vec)]
                  {:namespace namespace-name
                   :file (relative-path source-root file)
                   :sha256 (sha256 file)
                   :line-count (with-open [reader (io/reader file)] (count (line-seq reader)))
                   :doc (namespace-doc top)
                   :requires (parse-requires top)
                   :imports (parse-imports top)
                   :keywords (keyword-counts top)
                   :interesting-strings (interesting-strings top)
                   :definitions definitions}))
              source-files)
        registry (into {} (map (fn [entry]
                                 [(:namespace entry) (set (map :name (:definitions entry)))])
                               raw-entries))
        analyzed-entries
        (mapv (fn [entry]
                (assoc entry :definitions
                       (mapv (fn [definition]
                               (merge (dissoc definition :form)
                                      (let [{:keys [calls references]}
                                            (definition-edges registry entry definition)]
                                        {:calls (into (sorted-map)
                                                      (map (fn [[[ns-name var-name] count]]
                                                             [(str ns-name "/" var-name) count]))
                                                      calls)
                                         :references (into (sorted-map)
                                                           (map (fn [[[ns-name var-name] count]]
                                                                  [(str ns-name "/" var-name) count]))
                                                           references)})
                                      (let [gaps (decompilation-gaps (:form definition))]
                                        (when (seq gaps) {:decompilation-gaps gaps}))))
                             (:definitions entry))))
              raw-entries)
        namespace-set (set (map :namespace analyzed-entries))
        peer (peer-inventory peer-jar namespace-set)
        definitions (mapcat :definitions analyzed-entries)
        calls (for [definition definitions
                    [target count] (:calls definition)]
                [(:namespace definition) (:name definition) target count])
        references (for [definition definitions
                         [target count] (:references definition)]
                     [(:namespace definition) (:name definition) target count])
        gaps (for [definition definitions
                   marker (:decompilation-gaps definition)]
               [(:namespace definition) (:name definition) (:line definition)
                (subs marker (count "BROKEN DECOMP "))])
        dependency-rows
        (concat
          (for [entry analyzed-entries, requirement (:requires entry)]
            [(:namespace entry) (:namespace requirement) "require"])
          (for [[source-ns _ target _] calls
                :let [target-ns (subs target 0 (.lastIndexOf ^String target "/"))]
                :when (not= source-ns target-ns)]
            [source-ns target-ns "call"])
          (for [[source-ns _ target _] references
                :let [target-ns (subs target 0 (.lastIndexOf ^String target "/"))]
                :when (not= source-ns target-ns)]
            [source-ns target-ns "reference"]))
        keyword-index
        (reduce (fn [result entry]
                  (reduce-kv (fn [inner keyword-value count]
                               (update inner keyword-value
                                       (fnil assoc (sorted-map)) (:namespace entry) count))
                             result (:keywords entry)))
                (sorted-map) analyzed-entries)
        string-index
        (->> analyzed-entries
             (mapcat (fn [entry]
                       (map #(assoc % :namespace (:namespace entry))
                            (:interesting-strings entry))))
             (sort-by (juxt :kind :value :namespace))
             vec)
        corpus {:format-version 1
                :peer peer
                :summary {:namespace-count (count analyzed-entries)
                          :definition-count (count definitions)
                          :public-definition-count (count (filter #(= "public" (:visibility %)) definitions))
                          :private-definition-count (count (filter #(= "private" (:visibility %)) definitions))
                          :call-edge-count (count calls)
                          :reference-edge-count (count references)
                          :decompilation-gap-count (count gaps)
                          :keyword-count (count keyword-index)
                          :interesting-string-count (count string-index)}
                :namespaces (mapv #(dissoc % :keywords :interesting-strings) analyzed-entries)
                :keywords keyword-index
                :interesting-strings string-index}
        index-dir (str (io/file output-root "index"))]
    (write-edn (str (io/file index-dir "corpus.edn")) corpus)
    (write-tsv (str (io/file index-dir "namespaces.tsv"))
               ["namespace" "file" "lines" "definitions" "public" "private" "requires" "imports"]
               (for [entry analyzed-entries
                     :let [defs (:definitions entry)]]
                 [(:namespace entry) (:file entry) (:line-count entry) (count defs)
                  (count (filter #(= "public" (:visibility %)) defs))
                  (count (filter #(= "private" (:visibility %)) defs))
                  (str/join "," (map :namespace (:requires entry)))
                  (str/join "," (:imports entry))]))
    (write-tsv (str (io/file index-dir "vars.tsv"))
               ["namespace" "name" "kind" "visibility" "line" "arglists" "doc"]
               (for [definition definitions]
                 [(:namespace definition) (:name definition) (:kind definition)
                  (:visibility definition) (:line definition)
                  (str/join " " (:arglists definition)) (:doc definition)]))
    (write-tsv (str (io/file index-dir "calls.tsv"))
               ["source_namespace" "source_var" "target" "count"]
               (sort calls))
    (write-tsv (str (io/file index-dir "references.tsv"))
               ["source_namespace" "source_var" "target" "count"]
               (sort references))
    (write-tsv (str (io/file index-dir "dependencies.tsv"))
               ["source" "target" "kind"]
               (sort (distinct dependency-rows)))
    (write-tsv (str (io/file index-dir "gaps.tsv"))
               ["namespace" "definition" "line" "failed_class"]
               (sort gaps))
    (write-tsv (str (io/file index-dir "keywords.tsv"))
               ["keyword" "total" "namespaces"]
               (for [[keyword-value namespace-counts] keyword-index]
                 [keyword-value (reduce + (vals namespace-counts))
                  (str/join "," (map (fn [[ns-name count]] (str ns-name ":" count)) namespace-counts))]))
    (write-tsv (str (io/file index-dir "strings.tsv"))
               ["kind" "value" "namespace" "count"]
               (map (juxt :kind :value :namespace :count) string-index))
    (write-tsv (str (io/file index-dir "unmapped-classes.tsv"))
               ["class"]
               (map vector (:unmapped-classes peer)))
    (println "analyzed" (count analyzed-entries) "namespaces and" (count definitions) "definitions")
    (println "mapped" (:mapped-class-count peer) "of" (:class-count peer) "peer classes")
    (println "wrote" index-dir)))

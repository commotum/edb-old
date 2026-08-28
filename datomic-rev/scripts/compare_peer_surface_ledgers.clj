(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(def semantic-tag "VAR_METADATA_SEMANTIC_PROXIES ")
(def proxy-tag "VAR_SURFACE_PROXY_EXCLUSIONS ")
(def metadata-type-tag "VAR_METADATA_VALUE_TYPES ")
(def expected-bundled-source-sha
  "6d97ead2cf4a0fd350b038b6bc6f65cc2f3ac924723473329d5a2b7543ae5d12")
(def expected-bundled-source-entry
  "clojure/tools/analyzer/passes/add_binding_atom.clj")
(def expected-semantic-contract :fresh-empty-atom-factory-v1)
(def expected-semantic-namespace
  "clojure.tools.analyzer.passes.add-binding-atom")
(def expected-semantic-path
  [:var 'add-binding-atom :metadata
   {:map-key [:keyword nil "pass-info"]} :value
   {:map-key [:keyword nil "state"]} :value])
(def semantic-type-marker
  "semantic-proxy:fresh-empty-atom-factory-v1")

(def semantic-entry-keys
  #{:class-resource-present :code-source-present :contract :namespace
    :origin-kind :path :runtime-class :source-sha256})

(defn expected-semantic-origin [side]
  (cond
    (contains? #{:original :compiler-option-original} side)
    {:origin-kind :classpath-bytecode
     :class-resource-present true
     :code-source-present true}

    (contains? #{:recovered :compiler-option-recovered} side)
    {:origin-kind :source-generated
     :class-resource-present false
     :code-source-present false}

    :else
    (throw (ex-info "semantic metadata-proxy side has no approved origin lane"
                    {:side side}))))

(defn fail! [message data]
  (binding [*out* *err*]
    (println "PEER_SURFACE_LEDGER_FAIL" message (pr-str data)))
  (shutdown-agents)
  (System/exit 2))

(defn digest-stream [input]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        buffer (byte-array 65536)]
    (loop []
      (let [n (.read input buffer)]
        (when (pos? n)
          (.update digest buffer 0 n)
          (recur))))
    (format "%064x" (java.math.BigInteger. 1 (.digest digest)))))

(defn sha256-file [file]
  (with-open [input (io/input-stream file)]
    (digest-stream input)))

(defn sha256-zip-entry [archive entry-name]
  (with-open [zip-file (java.util.zip.ZipFile. (io/file archive))]
    (let [entry (.getEntry zip-file entry-name)]
      (when-not entry
        (throw (ex-info "bundled source entry is absent"
                        {:archive archive :entry entry-name})))
      (with-open [input (.getInputStream zip-file entry)]
        (digest-stream input)))))

(defn safe-name [namespace-name]
  (str/replace namespace-name #"[^A-Za-z0-9_.-]" "_"))

(defn read-tagged-vector [file tag namespace-name side]
  (let [lines (->> (str/split-lines (slurp file))
                   (remove str/blank?)
                   vec)]
    (when-not (= 1 (count lines))
      (throw (ex-info "ledger file is not exactly one tagged line"
                      {:namespace namespace-name :side side :file file
                       :line-count (count lines)})))
    (let [line (first lines)]
      (when-not (str/starts-with? line tag)
        (throw (ex-info "ledger tag mismatch"
                        {:namespace namespace-name :side side :file file
                         :expected tag :actual line})))
      (let [value (edn/read-string (subs line (count tag)))]
        (when-not (vector? value)
          (throw (ex-info "tagged ledger payload is not a vector"
                          {:namespace namespace-name :side side :file file
                           :type (some-> value class .getName)})))
        value))))

(defn read-tagged-types [file namespace-name side]
  (let [lines (->> (str/split-lines (slurp file))
                   (remove str/blank?)
                   vec)]
    (when-not (= 1 (count lines))
      (throw (ex-info "metadata-type ledger is not exactly one tagged line"
                      {:namespace namespace-name :side side :file file
                       :line-count (count lines)})))
    (let [line (first lines)]
      (when-not (str/starts-with? line metadata-type-tag)
        (throw (ex-info "metadata-type ledger tag mismatch"
                        {:namespace namespace-name :side side :file file
                         :actual line})))
      (let [payload (subs line (count metadata-type-tag))
            types (if (str/blank? payload) [] (str/split payload #"," -1))]
        (when-not (and (every? #(re-matches #"[A-Za-z0-9_.$\[;/-]+" %) types)
                       (= types (vec (sort (distinct types)))))
          (throw (ex-info "metadata-type ledger is not a sorted unique stable-type list"
                          {:namespace namespace-name :side side
                           :file file :types types})))
        (set types)))))

(defn read-source-forms [file]
  (binding [*read-eval* false]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                         (io/reader file))]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof} reader)]
            (if (identical? eof form)
              forms
              (recur (conj forms form)))))))))

(defn proxy-form? [form]
  (and (seq? form)
       (symbol? (first form))
       (contains? #{"proxy" "clojure.core/proxy"}
                  (str (first form)))))

(defn ns-import-pairs [forms]
  (let [ns-form (first (filter #(and (seq? %)
                                     (symbol? (first %))
                                     (= "ns" (name (first %))))
                               forms))]
    (mapcat
      (fn [clause]
        (when (and (seq? clause) (= :import (first clause)))
          (mapcat
            (fn [spec]
              (cond
                (and (vector? spec) (symbol? (first spec)))
                (let [package (str (first spec))]
                  (map (fn [class-name]
                         [(name class-name) (str package "." (name class-name))])
                       (rest spec)))

                (symbol? spec)
                [[(name spec) (str spec)]]

                :else
                (throw (ex-info "source ns has an unsupported import declaration"
                                {:declaration spec}))))
            (rest clause))))
      (nnext ns-form))))

(defn explicit-import-pairs [forms]
  (->> forms
       (mapcat #(tree-seq coll? seq %))
       (keep (fn [form]
               (when (and (seq? form)
                          (symbol? (first form))
                          (contains? #{"import" "import*"}
                                     (name (first form)))
                          (= 2 (count form)))
                 (let [class-value (second form)
                       class-name (cond
                                    (string? class-value) class-value
                                    (symbol? class-value) (str class-value)
                                    :else nil)]
                   (when class-name
                     [(last (str/split class-name #"\.")) class-name])))))
       vec))

(defn source-import-map [forms]
  (let [pairs (concat (ns-import-pairs forms) (explicit-import-pairs forms))
        grouped (group-by first pairs)
        ambiguous (->> grouped
                       (filter (fn [[_ entries]]
                                 (> (count (distinct (map second entries))) 1)))
                       (map first) vec)]
    (when (seq ambiguous)
      (throw (ex-info "source has ambiguous imported Class names"
                      {:simple-names ambiguous})))
    (into {} pairs)))

(defn resolve-proxy-type [imports type-symbol]
  (cond
    (namespace type-symbol) (str type-symbol)
    (contains? imports (name type-symbol)) (get imports (name type-symbol))
    :else
    (try
      (.getName (Class/forName (str "java.lang." (name type-symbol))
                               false (.getContextClassLoader
                                       (Thread/currentThread))))
      (catch ClassNotFoundException _
        (throw (ex-info "unqualified proxy type has no exact source import"
                        {:type type-symbol}))))))

(defn source-proxy-contract [imports form]
  (let [types (second form)]
    (when-not (and (vector? types) (seq types) (every? symbol? types))
      (throw (ex-info "source proxy form has unsupported type vector"
                      {:form form :types types})))
    (str "proxy$" (str/join "$" (map #(resolve-proxy-type imports %) types))
         "$")))

(defn source-proxy-contracts [source-file]
  (let [forms (read-source-forms source-file)
        imports (source-import-map forms)]
    (->> forms
         (mapcat #(tree-seq coll? seq %))
         (filter proxy-form?)
         (mapv #(source-proxy-contract imports %)))))

(defn validate-proxy-entry [namespace-name side entry]
  (when-not (and (map? entry)
                 (= #{:namespace :root-class :symbol} (set (keys entry)))
                 (every? string? (vals entry)))
    (throw (ex-info "proxy-exclusion entry has an unexpected schema"
                    {:namespace namespace-name :side side :entry entry})))
  (when-not (= namespace-name (:namespace entry))
    (throw (ex-info "proxy exclusion belongs to a different namespace"
                    {:namespace namespace-name :side side :entry entry})))
  (when-not (and (str/includes? (:symbol entry) ".proxy$")
                 (str/includes? (:root-class entry) ".proxy$")
                 (str/starts-with? (:symbol entry)
                                   (str (clojure.lang.Compiler/munge
                                          namespace-name)
                                        "."))
                 (str/starts-with? (:root-class entry)
                                   (str (clojure.lang.Compiler/munge
                                          namespace-name)
                                        ".")))
    (throw (ex-info "proxy exclusion does not identify a lane-local generated proxy Class Var"
                    {:namespace namespace-name :side side :entry entry})))
  entry)

(defn bind-proxy-entry-to-source!
  [namespace-name entry source-contracts]
  (let [munged-namespace (clojure.lang.Compiler/munge namespace-name)
        symbol-prefix (str munged-namespace ".")
        class-prefix (str munged-namespace ".")
        symbol-suffix (subs (:symbol entry) (count symbol-prefix))
        class-suffix (subs (:root-class entry) (count class-prefix))
        matches (->> source-contracts
                     (filter #(and (str/starts-with? symbol-suffix %)
                                   (str/starts-with? class-suffix %)))
                     distinct
                     vec)]
    (when-not (= symbol-suffix class-suffix)
      (throw (ex-info "proxy exclusion symbol/root Class identities disagree after namespace munging"
                      {:namespace namespace-name :entry entry
                       :symbol-suffix symbol-suffix
                       :root-class-suffix class-suffix})))
    (when-not (= 1 (count matches))
      (throw (ex-info "proxy exclusion does not bind to exactly one source proxy type/interface vector"
                      {:namespace namespace-name :entry entry
                       :source-contracts source-contracts
                       :matches matches})))
    (first matches)))

(defn validate-semantic-entry [namespace-name side entry]
  (let [expected-origin (expected-semantic-origin side)]
    (when-not (and (map? entry)
                   (= semantic-entry-keys (set (keys entry)))
                   (= expected-semantic-contract (:contract entry))
                   (= expected-semantic-namespace (:namespace entry))
                   (= expected-semantic-path (:path entry))
                   (= expected-bundled-source-sha (:source-sha256 entry))
                   (= expected-origin
                      (select-keys entry
                                   [:origin-kind :class-resource-present
                                    :code-source-present]))
                   (string? (:runtime-class entry))
                   (str/includes? (:runtime-class entry) "$"))
      (throw (ex-info "semantic metadata-proxy entry violates its sole approved contract and lane origin"
                      {:surface-namespace namespace-name :side side
                       :expected-origin expected-origin :entry entry})))
    (select-keys entry [:contract :namespace :path :source-sha256])))

(defn project-metadata-types
  [namespace-name side type-file semantic-entries]
  (let [raw-types (read-tagged-types type-file namespace-name side)
        runtime-types (mapv :runtime-class semantic-entries)
        duplicate-runtime-types (->> runtime-types frequencies
                                     (filter (fn [[_ count]] (> count 1)))
                                     (map first) vec)]
    (when (seq duplicate-runtime-types)
      (throw (ex-info "semantic proxies repeat a runtime Class identity"
                      {:namespace namespace-name :side side
                       :runtime-classes duplicate-runtime-types})))
    (doseq [runtime-type runtime-types]
      (when-not (contains? raw-types runtime-type)
        (throw (ex-info "semantic proxy runtime Class is absent from its metadata-type ledger"
                        {:namespace namespace-name :side side
                         :runtime-class runtime-type :types raw-types}))))
    {:raw raw-types
     :runtime-types (set runtime-types)
     :projected (cond-> (apply disj raw-types runtime-types)
                  (seq runtime-types) (conj semantic-type-marker))}))

(defn unique-by [key-fn description namespace-name side values]
  (let [grouped (group-by key-fn values)
        duplicates (->> grouped (filter (fn [[_ entries]] (> (count entries) 1)))
                        (map first) vec)]
    (when (seq duplicates)
      (throw (ex-info (str description " contains duplicate identities")
                      {:namespace namespace-name :side side
                       :duplicates duplicates})))
    values))

(defn parse-tagged-vector-string [line tag context]
  (when-not (str/starts-with? line tag)
    (throw (ex-info "fixture ledger tag mismatch"
                    {:context context :line line :expected tag})))
  (let [value (edn/read-string (subs line (count tag)))]
    (when-not (vector? value)
      (throw (ex-info "fixture ledger payload is not a vector"
                      {:context context :value value})))
    value))

(defn run-historical-proxy-self-test!
  [fixture-file source-root output-file]
  (when-not (every? some? [fixture-file source-root output-file])
    (fail! "usage: compare_peer_surface_ledgers.clj --historical-proxy-self-test FIXTURE_TSV SOURCE_ROOT OUTPUT_TSV"
           {}))
  (try
    (let [rows (->> (line-seq (io/reader fixture-file))
                    rest
                    (mapv #(str/split % #"\t" -1)))
          results
          (mapv
            (fn [[namespace-name source-path expected-source-sha
                  historical-log-sha ledger-line :as row]]
              (when-not (= 5 (count row))
                (throw (ex-info "historical proxy fixture row is malformed"
                                {:row row})))
              (let [source-file (io/file source-root source-path)
                    actual-source-sha (sha256-file source-file)
                    entries (parse-tagged-vector-string
                              ledger-line proxy-tag namespace-name)
                    _one-entry (when-not (= 1 (count entries))
                                 (throw (ex-info "historical proxy fixture must contain one collapsed entry"
                                                 {:namespace namespace-name
                                                  :entries entries})))
                    historical-entry (first entries)
                    source-contracts (source-proxy-contracts source-file)
                    contract-counts (frequencies source-contracts)
                    expected-contract "proxy$java.lang.ThreadLocal$"
                    rejection
                    (try
                      (validate-proxy-entry namespace-name :historical
                                            historical-entry)
                      nil
                      (catch clojure.lang.ExceptionInfo failure
                        (ex-message failure)))
                    corrected-entry (assoc historical-entry
                                           :root-class
                                           (:symbol historical-entry))
                    corrected-entry (validate-proxy-entry
                                      namespace-name :corrected
                                      corrected-entry)
                    corrected-contract (bind-proxy-entry-to-source!
                                         namespace-name corrected-entry
                                         source-contracts)]
                (when-not (= expected-source-sha actual-source-sha)
                  (throw (ex-info "historical proxy witness source hash mismatch"
                                  {:namespace namespace-name
                                   :expected expected-source-sha
                                   :actual actual-source-sha})))
                (when-not (re-matches #"[0-9a-f]{64}" historical-log-sha)
                  (throw (ex-info "historical proxy witness log hash is malformed"
                                  {:namespace namespace-name
                                   :hash historical-log-sha})))
                (when-not (= "java.lang.Class" (:root-class historical-entry))
                  (throw (ex-info "historical proxy witness no longer captures the Class-collapse bug"
                                  {:namespace namespace-name
                                   :entry historical-entry})))
                (when-not (= 2 (get contract-counts expected-contract 0))
                  (throw (ex-info "historical proxy witness source no longer has the two exact reusable proxy shapes"
                                  {:namespace namespace-name
                                   :contracts contract-counts})))
                (when-not (= "proxy exclusion does not identify a lane-local generated proxy Class Var"
                             rejection)
                  (throw (ex-info "historical java.lang.Class collapse was not rejected by the intended guard"
                                  {:namespace namespace-name
                                   :rejection rejection})))
                (when-not (= expected-contract corrected-contract)
                  (throw (ex-info "corrected proxy identity did not bind to the exact imported source shape"
                                  {:namespace namespace-name
                                   :expected expected-contract
                                   :actual corrected-contract})))
                {:namespace namespace-name
                 :source-path source-path
                 :source-sha actual-source-sha
                 :historical-log-sha historical-log-sha
                 :source-shape-count (get contract-counts expected-contract)
                 :historical-status "rejected-java.lang.Class-collapse"
                 :corrected-status "source-bound-exact-identity"}))
            rows)]
      (when-not (= 2 (count results))
        (throw (ex-info "historical proxy witness count is not two"
                        {:count (count results)})))
      (with-open [writer (io/writer output-file :encoding "UTF-8")]
        (.write writer
                "namespace\tsource_path\tsource_sha256\thistorical_log_sha256\tsource_shape_count\thistorical_status\tcorrected_status\n")
        (doseq [{:keys [namespace source-path source-sha historical-log-sha
                        source-shape-count historical-status corrected-status]}
                results]
          (.write writer
                  (str/join "\t"
                            [namespace source-path source-sha historical-log-sha
                             source-shape-count historical-status
                             corrected-status]))
          (.write writer "\n")))
      (println "PEER_HISTORICAL_PROXY_GUARD_RESULT witnesses=2 rejected=2 corrected_source_bound=2"))
    (catch Throwable failure
      (fail! "historical proxy guard self-test failed"
             (merge {:cause (ex-message failure)} (ex-data failure))))))

(when (= "--historical-proxy-self-test" (first *command-line-args*))
  (apply run-historical-proxy-self-test! (rest *command-line-args*))
  (shutdown-agents)
  (System/exit 0))

(let [[original-proxy-root recovered-proxy-root
       original-semantic-root recovered-semantic-root
       original-metadata-type-root recovered-metadata-type-root
       source-root namespace-index tools-analyzer-jar output-file
       compiler-option-original-semantic compiler-option-recovered-semantic
       compiler-option-original-types compiler-option-recovered-types]
      *command-line-args*]
  (when-not (every? some?
                    [original-proxy-root recovered-proxy-root
                     original-semantic-root recovered-semantic-root
                     original-metadata-type-root recovered-metadata-type-root
                     source-root namespace-index tools-analyzer-jar output-file])
    (fail! "usage: compare_peer_surface_ledgers.clj ORIGINAL_PROXY_ROOT RECOVERED_PROXY_ROOT ORIGINAL_SEMANTIC_ROOT RECOVERED_SEMANTIC_ROOT ORIGINAL_METADATA_TYPE_ROOT RECOVERED_METADATA_TYPE_ROOT SOURCE_ROOT NAMESPACE_INDEX TOOLS_ANALYZER_JAR OUTPUT_TSV [COMPILER_OPTION_ORIGINAL_SEMANTIC COMPILER_OPTION_RECOVERED_SEMANTIC COMPILER_OPTION_ORIGINAL_TYPES COMPILER_OPTION_RECOVERED_TYPES]"
           {}))
  (try
    (let [bundled-source-sha (sha256-zip-entry tools-analyzer-jar
                                               expected-bundled-source-entry)]
      (when-not (= expected-bundled-source-sha bundled-source-sha)
        (throw (ex-info "semantic metadata-proxy bundled-source owner hash mismatch"
                        {:archive tools-analyzer-jar
                         :entry expected-bundled-source-entry
                         :expected expected-bundled-source-sha
                         :actual bundled-source-sha})))
      (let [compiler-option-files
            [compiler-option-original-semantic
             compiler-option-recovered-semantic
             compiler-option-original-types
             compiler-option-recovered-types]]
        (when-not (or (every? nil? compiler-option-files)
                      (every? some? compiler-option-files))
          (throw (ex-info "compiler-option semantic/type evidence is incomplete"
                          {:files compiler-option-files}))))
      (when compiler-option-original-semantic
        (let [read-semantic-side
              (fn [file side]
                (->> (read-tagged-vector file semantic-tag
                                         expected-semantic-namespace side)
                     (mapv (fn [entry]
                             (validate-semantic-entry
                               expected-semantic-namespace side entry)
                             entry))
                     (unique-by (juxt :contract :namespace :path
                                      :source-sha256)
                                "compiler-option semantic-proxy ledger"
                                expected-semantic-namespace side)))
              original-raw (read-semantic-side
                             compiler-option-original-semantic
                             :compiler-option-original)
              recovered-raw (read-semantic-side
                              compiler-option-recovered-semantic
                              :compiler-option-recovered)
              original (->> original-raw
                            (mapv #(validate-semantic-entry
                                     expected-semantic-namespace
                                     :compiler-option-original %))
                            (sort-by pr-str) vec)
              recovered (->> recovered-raw
                             (mapv #(validate-semantic-entry
                                      expected-semantic-namespace
                                      :compiler-option-recovered %))
                             (sort-by pr-str) vec)
              original-types (project-metadata-types
                               expected-semantic-namespace
                               :compiler-option-original
                               compiler-option-original-types original-raw)
              recovered-types (project-metadata-types
                                expected-semantic-namespace
                                :compiler-option-recovered
                                compiler-option-recovered-types recovered-raw)]
          (when-not (and (= 1 (count original)) (= original recovered))
            (throw (ex-info "compiler-option semantic proxy is not a single stable source-bound contract"
                            {:original original :recovered recovered})))
          (when-not (= (:projected original-types)
                       (:projected recovered-types))
            (throw (ex-info "compiler-option metadata types differ after the one source-bound semantic proxy projection"
                            {:original original-types
                             :recovered recovered-types})))
          (spit (str output-file ".compiler-option.properties")
                (str "semantic.proxy.count=1\n"
                     "semantic.proxy.contract="
                     (name expected-semantic-contract) "\n"
                     "semantic.proxy.oracle.origin=classpath-bytecode\n"
                     "semantic.proxy.recovered.origin=source-generated\n"
                     "semantic.proxy.origin-evidence.exact=true\n"
                     "semantic.proxy.runtime-class.projected=true\n"
                     "metadata.types.semantic-proxy.projected=true\n"
                     "metadata.types.projected.count="
                     (count (:projected original-types)) "\n"
                     "semantic.proxy.bundled-source-owner.sha256="
                     bundled-source-sha "\n"))))
      (let [rows (->> (line-seq (io/reader namespace-index))
                      rest
                      (mapv #(str/split % #"\t" -1)))
            relations
            (mapv
              (fn [[namespace-name source-path & _]]
                (let [name-on-disk (safe-name namespace-name)
                      source-file (io/file source-root source-path)
                      original-proxies (read-tagged-vector
                                         (io/file original-proxy-root
                                                  (str name-on-disk ".edn"))
                                         proxy-tag namespace-name :original)
                      recovered-proxies (read-tagged-vector
                                          (io/file recovered-proxy-root
                                                   (str name-on-disk ".edn"))
                                          proxy-tag namespace-name :recovered)
                      original-semantics (read-tagged-vector
                                           (io/file original-semantic-root
                                                    (str name-on-disk ".edn"))
                                           semantic-tag namespace-name :original)
                      recovered-semantics (read-tagged-vector
                                            (io/file recovered-semantic-root
                                                     (str name-on-disk ".edn"))
                                            semantic-tag namespace-name :recovered)
                      original-metadata-type-file
                      (io/file original-metadata-type-root
                               (str name-on-disk ".txt"))
                      recovered-metadata-type-file
                      (io/file recovered-metadata-type-root
                               (str name-on-disk ".txt"))
                      _source-present (when-not (.isFile source-file)
                                        (throw (ex-info "proxy ownership source is missing"
                                                        {:namespace namespace-name
                                                         :source (.getPath source-file)})))
                      original-proxies (->> original-proxies
                                            (mapv #(validate-proxy-entry
                                                     namespace-name :original %))
                                            (unique-by (juxt :namespace :symbol)
                                                       "oracle proxy ledger"
                                                       namespace-name :original))
                      recovered-proxies (->> recovered-proxies
                                             (mapv #(validate-proxy-entry
                                                      namespace-name :recovered %))
                                             (unique-by (juxt :namespace :symbol)
                                                        "recovered proxy ledger"
                                                        namespace-name :recovered))
                      authored-proxy-contracts (source-proxy-contracts source-file)
                      authored-proxy-forms (count authored-proxy-contracts)
                      _oracle-empty (when (seq original-proxies)
                                      (throw (ex-info "oracle/AOT unexpectedly retained compiler scratch proxy Vars"
                                                      {:namespace namespace-name
                                                       :entries original-proxies})))
                      bound-proxy-contracts
                      (mapv #(bind-proxy-entry-to-source!
                               namespace-name % authored-proxy-contracts)
                            recovered-proxies)
                      source-contract-counts (frequencies authored-proxy-contracts)
                      exclusion-contract-counts (frequencies bound-proxy-contracts)
                      _source-bound
                      (doseq [[contract exclusion-count]
                              exclusion-contract-counts]
                        (when (> exclusion-count
                                 (get source-contract-counts contract 0))
                          (throw (ex-info "recovered proxy exclusions exceed source forms for the same type/interface identity"
                                          {:namespace namespace-name
                                           :source (.getPath source-file)
                                           :contract contract
                                           :source-count
                                           (get source-contract-counts contract 0)
                                           :exclusion-count exclusion-count}))))
                      original-semantics-raw
                      (->> original-semantics
                           (mapv (fn [entry]
                                   (validate-semantic-entry
                                     namespace-name :original entry)
                                   entry))
                           (unique-by (juxt :contract :namespace :path
                                            :source-sha256)
                                      "oracle semantic-proxy ledger"
                                      namespace-name :original))
                      recovered-semantics-raw
                      (->> recovered-semantics
                           (mapv (fn [entry]
                                   (validate-semantic-entry
                                     namespace-name :recovered entry)
                                   entry))
                           (unique-by (juxt :contract :namespace :path
                                            :source-sha256)
                                      "recovered semantic-proxy ledger"
                                      namespace-name :recovered))
                      original-semantics (->> original-semantics-raw
                                              (mapv #(validate-semantic-entry
                                                       namespace-name :original %))
                                              (unique-by (juxt :contract :namespace :path)
                                                         "oracle semantic-proxy ledger"
                                                         namespace-name :original)
                                              (sort-by pr-str)
                                              vec)
                      recovered-semantics (->> recovered-semantics-raw
                                               (mapv #(validate-semantic-entry
                                                        namespace-name :recovered %))
                                               (unique-by (juxt :contract :namespace :path)
                                                          "recovered semantic-proxy ledger"
                                                          namespace-name :recovered)
                                               (sort-by pr-str)
                                               vec)
                      _semantic-relation (when-not (= original-semantics recovered-semantics)
                                           (throw (ex-info "semantic metadata-proxy contracts differ after validated lane-origin/runtime-class projection"
                                                           {:namespace namespace-name
                                                            :original original-semantics
                                                            :recovered recovered-semantics})))
                      _semantic-source-owner
                      (when (seq original-semantics)
                        (let [source-sha (sha256-file source-file)]
                          (when-not (and (= expected-semantic-namespace
                                            namespace-name)
                                         (= expected-bundled-source-entry
                                            source-path)
                                         (= expected-bundled-source-sha
                                            source-sha))
                            (throw (ex-info "semantic metadata proxy is not owned by the exact recovered source file"
                                            {:namespace namespace-name
                                             :source-path source-path
                                             :expected-sha
                                             expected-bundled-source-sha
                                             :actual-sha source-sha})))))
                      original-types (project-metadata-types
                                       namespace-name :original
                                       original-metadata-type-file
                                       original-semantics-raw)
                      recovered-types (project-metadata-types
                                        namespace-name :recovered
                                        recovered-metadata-type-file
                                        recovered-semantics-raw)
                      _type-relation
                      (when-not (= (:projected original-types)
                                   (:projected recovered-types))
                        (throw (ex-info "metadata value types differ after the one source-bound semantic proxy projection"
                                        {:namespace namespace-name
                                         :original original-types
                                         :recovered recovered-types})))]
                  {:namespace namespace-name
                   :source-path source-path
                   :source-sha (sha256-file source-file)
                   :source-proxy-forms authored-proxy-forms
                   :original-proxy-exclusions (count original-proxies)
                   :recovered-proxy-exclusions (count recovered-proxies)
                   :proxy-relation (if (seq recovered-proxies)
                                     "recovered-only-source-bounded"
                                     "none")
                   :semantic-proxies (count original-semantics)
                   :semantic-relation
                   "contract-equal-after-exact-lane-origin-validation"
                   :metadata-types (count (:projected original-types))
                   :metadata-type-relation
                   "exact-after-origin-validated-semantic-proxy-projection"}))
              rows)]
        (with-open [writer (io/writer output-file :encoding "UTF-8")]
          (.write writer
                  "namespace\tsource_path\tsource_sha256\tsource_proxy_forms\toriginal_proxy_exclusions\trecovered_proxy_exclusions\tproxy_relation\tsemantic_proxies\tsemantic_relation\tmetadata_types\tmetadata_type_relation\tbundled_source_owner_sha256\n")
          (doseq [{:keys [namespace source-path source-sha source-proxy-forms
                          original-proxy-exclusions recovered-proxy-exclusions
                          proxy-relation semantic-proxies semantic-relation
                          metadata-types metadata-type-relation]}
                  relations]
            (.write writer
                    (str/join "\t"
                              [namespace source-path source-sha source-proxy-forms
                               original-proxy-exclusions recovered-proxy-exclusions
                               proxy-relation semantic-proxies semantic-relation
                               metadata-types metadata-type-relation
                               bundled-source-sha]))
            (.write writer "\n")))
        (println "PEER_SURFACE_LEDGER_RESULT"
                 (str "namespaces=" (count relations))
                 (str "recovered_proxy_exclusions="
                      (reduce + (map :recovered-proxy-exclusions relations)))
                 (str "semantic_proxies="
                      (reduce + (map :semantic-proxies relations)))
                 "semantic_origin_relation=exact-lane-provenance"
                 "metadata_type_relation=exact-after-projection"
                 (str "bundled_source_owner_sha256=" bundled-source-sha))))
    (catch Throwable failure
      (fail! "surface side-ledger comparison could not complete"
             (merge {:cause (ex-message failure)} (ex-data failure)))))
  (shutdown-agents))

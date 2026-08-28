(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(def location-metadata-keys
  #{:file :line :column :end-line :end-column})

(defn fail! [message data]
  (binding [*out* *err*]
    (println "PEER_SOURCE_FORM_COMPARISON_FAIL" message (pr-str data)))
  (shutdown-agents)
  (System/exit 2))

(defn sha256 [file]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (with-open [input (io/input-stream file)]
      (let [buffer (byte-array 65536)]
        (loop []
          (let [n (.read input buffer)]
            (when (pos? n)
              (.update digest buffer 0 n)
              (recur))))))
    (format "%064x" (java.math.BigInteger. 1 (.digest digest)))))

(defn read-forms [file]
  (binding [*read-eval* false]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                         (io/reader file))]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof} reader)]
            (if (identical? eof form)
              forms
              (recur (conj forms form)))))))))

(declare encode-form)

(defn location-metadata-key? [key]
  (contains? location-metadata-keys key))

(defn metadata-for-view [value metadata-view]
  (when (and (not= :none metadata-view)
             (instance? clojure.lang.IMeta value)
             (seq (meta value)))
    (let [metadata (if (= :semantic metadata-view)
                     (into (empty (meta value))
                           (remove (comp location-metadata-key? key))
                           (meta value))
                     (meta value))]
      (when (seq metadata)
        (encode-form metadata metadata-view)))))

(defn type-name [value]
  (if (nil? value) "nil" (.getName (class value))))

(defn encode-form [value metadata-view]
  (let [own-metadata (metadata-for-view value metadata-view)]
    (cond
      (nil? value) [:nil]
      (boolean? value) [:boolean value]
      (string? value) [:string value]
      (char? value) [:character (int value)]
      (keyword? value) [:keyword (namespace value) (name value)]
      (symbol? value) [:symbol (namespace value) (name value) own-metadata]
      (number? value) [:number (type-name value) (pr-str value)]
      (instance? java.util.regex.Pattern value)
      [:regular-expression (.pattern ^java.util.regex.Pattern value)
       (.flags ^java.util.regex.Pattern value)]

      (map? value)
      [:map own-metadata
       (->> value
            (map (fn [[key item]]
                   [(encode-form key metadata-view)
                    (encode-form item metadata-view)]))
            (sort-by pr-str)
            vec)]

      (vector? value)
      [:vector own-metadata (mapv #(encode-form % metadata-view) value)]

      (set? value)
      [:set own-metadata
       (->> value
            (map #(encode-form % metadata-view))
            (sort-by pr-str)
            vec)]

      (seq? value)
      [:list own-metadata (mapv #(encode-form % metadata-view) value)]

      :else
      (throw (ex-info "unsupported source-form value"
                      {:type (type-name value)
                       :value (pr-str value)})))))

(defn call-name [form]
  (when (and (seq? form) (symbol? (first form)))
    (name (first form))))

(defn exact-call? [form allowed-symbols]
  (and (seq? form)
       (symbol? (first form))
       (contains? allowed-symbols (str (first form)))))

(defn core-call? [form expected-name]
  (exact-call? form
               #{expected-name (str "clojure.core/" expected-name)}))

(defn defprotocol-form? [form]
  (core-call? form "defprotocol"))

(defn empty-protocol-placeholder? [form protocol]
  (and (core-call? form "defonce")
       (= 3 (count form))
       (symbol? (second form))
       (= (name protocol) (name (second form)))
       (map? (nth form 2))
       (empty? (nth form 2))))

(defn split-protocol-declaration [declaration]
  (when-not (and (seq? declaration) (symbol? (first declaration)))
    (throw (ex-info "malformed defprotocol method declaration"
                    {:declaration declaration})))
  (let [method-name (first declaration)
        tail (rest declaration)
        tail (if (string? (first tail)) (rest tail) tail)]
    (when-not (and (seq tail) (every? vector? tail))
      (throw (ex-info "defprotocol method lacks literal argument vectors"
                      {:method method-name :declaration declaration})))
    {:name (name method-name)
     ;; Protocol metadata is the sole source-level exception, and only when
     ;; backed by byte-for-byte oracle/recovered runtime surfaces.  Argument
     ;; names, arities, variadic markers, literal order, and numeric types are
     ;; still preserved here; only protocol metadata/docs are projected.
     :arglists (mapv #(encode-form % :none) tail)}))

(defn protocol-contract [form]
  (when-not (and (defprotocol-form? form)
                 (symbol? (second form)))
    (throw (ex-info "malformed defprotocol form" {:form form})))
  (let [protocol-name (second form)
        tail (nnext form)
        tail (if (string? (first tail)) (rest tail) tail)]
    {:protocol (name protocol-name)
     :methods (mapv split-protocol-declaration tail)}))

(defn quoted-symbol? [form expected]
  (and (seq? form)
       (core-call? form "quote")
       (= 2 (count form))
       (symbol? (second form))
       (= (name expected) (name (second form)))))

(defn var-reference-identity [form current-namespace]
  (cond
    (and (seq? form)
         (core-call? form "var")
         (= 2 (count form))
         (symbol? (second form)))
    [(or (namespace (second form)) current-namespace) (name (second form))]

    (and (seq? form)
         (= "clojure.lang.RT/var" (str (first form)))
         (= 3 (count form))
         (string? (second form))
         (string? (nth form 2)))
    [(second form) (nth form 2)]

    :else nil))

(defn expected-var-reference? [form current-namespace expected-name]
  (= [current-namespace (name expected-name)]
     (var-reference-identity form current-namespace)))

(defn safe-protocol-metadata-expression? [value]
  (cond
    (or (nil? value) (boolean? value) (string? value) (char? value)
        (keyword? value) (symbol? value) (number? value))
    true

    (map? value)
    (every? true? (mapcat (fn [[key item]]
                            [(safe-protocol-metadata-expression? key)
                             (safe-protocol-metadata-expression? item)])
                          value))

    (or (vector? value) (set? value))
    (every? safe-protocol-metadata-expression? value)

    (seq? value)
    (let [head (str (first value))]
      (cond
        (core-call? value "quote") (= 2 (count value))
        (core-call? value "int") (and (= 2 (count value))
                                       (integer? (second value)))
        (or (core-call? value "with-meta") (= ".withMeta" head))
        (and (= 3 (count value))
             (safe-protocol-metadata-expression? (second value))
             (safe-protocol-metadata-expression? (nth value 2)))
        (some #(core-call? value %)
              ["list" "vector" "hash-map" "array-map" "set"])
        (every? safe-protocol-metadata-expression? (rest value))
        (or (core-call? value "var")
            (= "clojure.lang.RT/var" head))
        (boolean (var-reference-identity value "unused"))
        :else false))

    :else false))

(defn exact-protocol-reset? [form current-namespace protocol metadata-binding doc]
  (and (core-call? form "reset-meta!")
       (= 3 (count form))
       (expected-var-reference? (second form) current-namespace protocol)
       (let [outer (nth form 2)
             inner (second outer)]
         (and (core-call? outer "assoc")
              (= 6 (count outer))
              (= :name (nth outer 2))
              (quoted-symbol? (nth outer 3) protocol)
              (= :ns (nth outer 4))
              (= '*ns* (nth outer 5))
              (core-call? inner "assoc")
              (= 4 (count inner))
              (= metadata-binding (second inner))
              (= :doc (nth inner 2))
              (= doc (nth inner 3))))))

(defn exact-method-reset? [form current-namespace protocol method]
  (and (core-call? form "let")
       (= 3 (count form))
       (vector? (second form))
       (= 4 (count (second form)))
       (let [[signature-binding signature-expression
              name-binding name-expression] (second form)
             reset-form (nth form 2)]
         (and (symbol? signature-binding)
              (re-matches #"protocol_signature__[0-9]+"
                          (name signature-binding))
              (symbol? name-binding)
              (re-matches #"protocol_method_name__[0-9]+"
                          (name name-binding))
              (core-call? signature-expression "assoc")
              (= 4 (count signature-expression))
              (map? (second signature-expression))
              (safe-protocol-metadata-expression?
                (second signature-expression))
              (= :protocol (nth signature-expression 2))
              (expected-var-reference? (nth signature-expression 3)
                                       current-namespace protocol)
              (core-call? name-expression "with-meta")
              (= 3 (count name-expression))
              (= (list :name signature-binding) (second name-expression))
              (= signature-binding (nth name-expression 2))
              (core-call? reset-form "reset-meta!")
              (= 3 (count reset-form))
              (expected-var-reference? (second reset-form)
                                       current-namespace (:name method))
              (let [metadata-expression (nth reset-form 2)]
                (and (core-call? metadata-expression "assoc")
                     (= 6 (count metadata-expression))
                     (= signature-binding (second metadata-expression))
                     (= :name (nth metadata-expression 2))
                     (= name-binding (nth metadata-expression 3))
                     (= :ns (nth metadata-expression 4))
                     (= '*ns* (nth metadata-expression 5))))))))

(defn exact-expanded-protocol-scaffold [form current-namespace]
  (when (and (core-call? form "let")
             (vector? (second form))
             (= 2 (count (second form)))
             (symbol? (first (second form))))
    (let [[metadata-binding metadata-expression] (second form)
          body (vec (nnext form))
          protocol-form (first body)]
      (when (defprotocol-form? protocol-form)
        (let [contract (protocol-contract protocol-form)
              protocol (symbol (:protocol contract))
              protocol-tail (nnext protocol-form)
              doc (when (string? (first protocol-tail))
                    (first protocol-tail))
              resets (subvec body 2)]
          (when (and (re-matches #"protocol_metadata__[0-9]+"
                                 (name metadata-binding))
                     (safe-protocol-metadata-expression?
                       metadata-expression)
                     (= (+ 2 (count (:methods contract))) (count body))
                     (exact-protocol-reset? (second body) current-namespace
                                            protocol metadata-binding doc)
                     (every? true?
                             (map #(exact-method-reset? %1 current-namespace
                                                        protocol %2)
                                  resets (:methods contract))))
            contract))))))

(defn top-level-body [forms]
  (if (and (= 1 (count forms)) (core-call? (first forms) "do"))
    (vec (rest (first forms)))
    (vec forms)))

(defn normalize-protocol-events [forms current-namespace]
  (loop [remaining (top-level-body forms)
         events []
         legacy-count 0
         expanded-count 0]
    (if (empty? remaining)
      {:events events :legacy legacy-count :expanded expanded-count}
      (let [form (first remaining)
            next-form (second remaining)
            next-contract (when (defprotocol-form? next-form)
                            (protocol-contract next-form))
            legacy? (and next-contract
                         (empty-protocol-placeholder?
                           form (symbol (:protocol next-contract))))
            expanded-contract (exact-expanded-protocol-scaffold
                                form current-namespace)]
        (cond
          legacy?
          (recur (subvec remaining 2)
                 (conj events [:protocol-event next-contract])
                 (inc legacy-count) expanded-count)

          expanded-contract
          (recur (subvec remaining 1)
                 (conj events [:protocol-event expanded-contract])
                 legacy-count (inc expanded-count))

          :else
          (recur (subvec remaining 1)
                 (conj events [:form (encode-form form :semantic)])
                 legacy-count expanded-count))))))

(defn split-tsv [line]
  (str/split line #"\t" -1))

(defn safe-name [namespace-name]
  (str/replace namespace-name #"[^A-Za-z0-9_.-]" "_"))

(defn require-runtime-surface-proof!
  [namespace-name result-root original-surface-root recovered-surface-root]
  (when-not (every? some?
                    [result-root original-surface-root recovered-surface-root])
    (throw (ex-info "protocol scaffold transition lacks runtime-surface proof roots"
                    {:namespace namespace-name})))
  (let [disk-name (safe-name namespace-name)
        result-file (io/file result-root (str disk-name ".tsv"))
        original-file (io/file original-surface-root (str disk-name ".edn"))
        recovered-file (io/file recovered-surface-root (str disk-name ".edn"))]
    (when-not (every? #(.isFile ^java.io.File %)
                      [result-file original-file recovered-file])
      (throw (ex-info "protocol scaffold transition runtime evidence is missing"
                      {:namespace namespace-name
                       :result (.getPath result-file)
                       :original (.getPath original-file)
                       :recovered (.getPath recovered-file)})))
    (let [result-lines (->> (str/split-lines (slurp result-file))
                            (remove str/blank?) vec)
          fields (when (= 1 (count result-lines))
                   (split-tsv (first result-lines)))
          original-sha (sha256 original-file)
          recovered-sha (sha256 recovered-file)]
      (when-not (and (= 7 (count fields))
                     (= ["DATOMIC_SURFACE_RESULT" "PASS" namespace-name
                         "exact" "none"]
                        (subvec (vec fields) 0 5))
                     (= original-sha (nth fields 5))
                     (= recovered-sha (nth fields 6))
                     (= original-sha recovered-sha))
        (throw (ex-info "protocol scaffold transition is not backed by an exact oracle/recovered runtime surface"
                        {:namespace namespace-name :result fields
                         :actual-original-sha original-sha
                         :actual-recovered-sha recovered-sha}))))))

(let [[reference-root regenerated-root namespace-index output-file
       surface-result-root original-surface-root recovered-surface-root]
      *command-line-args*]
  (when-not (every? some?
                    [reference-root regenerated-root namespace-index output-file])
    (fail! (str "usage: compare_peer_source_forms.clj REFERENCE_ROOT "
                "REGENERATED_ROOT NAMESPACE_INDEX OUTPUT_TSV "
                "[SURFACE_RESULT_ROOT ORIGINAL_SURFACE_ROOT "
                "RECOVERED_SURFACE_ROOT]")
           {}))
  (when-not (or (every? nil? [surface-result-root original-surface-root
                              recovered-surface-root])
                (every? some? [surface-result-root original-surface-root
                               recovered-surface-root]))
    (fail! "runtime-surface proof arguments are incomplete" {}))
  (try
    (let [rows (->> (line-seq (io/reader namespace-index))
                    rest
                    (mapv split-tsv))
          comparisons
          (mapv
            (fn [[namespace-name source-path & _]]
              (let [reference-file (io/file reference-root source-path)
                    regenerated-file (io/file regenerated-root source-path)]
                (when-not (and (.isFile reference-file)
                               (.isFile regenerated-file))
                  (throw (ex-info "source relation file is missing"
                                  {:namespace namespace-name
                                   :path source-path
                                   :reference (.getPath reference-file)
                                   :regenerated (.getPath regenerated-file)})))
                (let [reference-bytes (java.nio.file.Files/readAllBytes
                                        (.toPath reference-file))
                      regenerated-bytes (java.nio.file.Files/readAllBytes
                                          (.toPath regenerated-file))
                      byte-exact? (java.util.Arrays/equals reference-bytes
                                                          regenerated-bytes)
                      reference-forms (read-forms reference-file)
                      regenerated-forms (read-forms regenerated-file)
                      metadata-exact? (= (encode-form reference-forms :all)
                                         (encode-form regenerated-forms :all))
                      semantic-exact? (= (encode-form reference-forms :semantic)
                                         (encode-form regenerated-forms :semantic))
                      reference-events (when-not semantic-exact?
                                         (normalize-protocol-events
                                           reference-forms namespace-name))
                      regenerated-events (when-not semantic-exact?
                                           (normalize-protocol-events
                                             regenerated-forms namespace-name))
                      scaffold-transition?
                      (and (not semantic-exact?)
                           (= (:events reference-events)
                              (:events regenerated-events))
                           (pos? (+ (:expanded reference-events)
                                    (:expanded regenerated-events)))
                           (pos? (+ (:legacy reference-events)
                                    (:legacy regenerated-events)))
                           (= (+ (:legacy reference-events)
                                 (:expanded reference-events))
                              (+ (:legacy regenerated-events)
                                 (:expanded regenerated-events))))
                      _runtime-proof
                      (when scaffold-transition?
                        (require-runtime-surface-proof!
                          namespace-name surface-result-root
                          original-surface-root recovered-surface-root))
                      accepted? (or semantic-exact? scaffold-transition?)
                      classification
                      (cond
                        byte-exact? "byte-exact"
                        metadata-exact? "format-only"
                        semantic-exact? "location-metadata-or-format-only"
                        scaffold-transition? "oracle-proved-protocol-scaffold"
                        :else "semantic-body-or-structure")]
                  {:namespace namespace-name
                   :path source-path
                   :reference-sha (sha256 reference-file)
                   :regenerated-sha (sha256 regenerated-file)
                   :reference-form-count (count reference-forms)
                   :regenerated-form-count (count regenerated-forms)
                   :metadata-exact metadata-exact?
                   :semantic-exact semantic-exact?
                   :accepted accepted?
                   :classification classification
                   :protocol-scaffold-transition scaffold-transition?})))
            rows)
          rejected (count (remove :accepted comparisons))
          location-deltas (count (filter #(= "location-metadata-or-format-only"
                                             (:classification %))
                                         comparisons))
          scaffold-deltas (count (filter :protocol-scaffold-transition
                                         comparisons))
          format-deltas (count (filter #(= "format-only" (:classification %))
                                       comparisons))]
      (with-open [writer (io/writer output-file :encoding "UTF-8")]
        (.write writer
                "namespace\tpath\treference_sha256\tregenerated_sha256\treference_forms\tregenerated_forms\tmetadata_exact\tsemantic_exact\tclassification\tprotocol_scaffold_transition\n")
        (doseq [{:keys [namespace path reference-sha regenerated-sha
                        reference-form-count regenerated-form-count
                        metadata-exact semantic-exact classification
                        protocol-scaffold-transition]}
                comparisons]
          (.write writer
                  (str/join "\t"
                            [namespace path reference-sha regenerated-sha
                             reference-form-count regenerated-form-count
                             metadata-exact semantic-exact classification
                             protocol-scaffold-transition]))
          (.write writer "\n")))
      (println "PEER_SOURCE_FORM_RESULT"
               (str "namespaces=" (count comparisons))
               (str "semantic_body_or_structure=" rejected)
               (str "protocol_scaffold=" scaffold-deltas)
               (str "location_metadata_or_format=" location-deltas)
               (str "format_only=" format-deltas))
      (when (pos? rejected)
        (fail! "recovered Peer contains semantic source-body or unauthorized structure deltas"
               {:count rejected :report output-file})))
    (catch Throwable failure
      (fail! "source-form comparison could not complete"
             (merge {:cause (ex-message failure)} (ex-data failure)))))
  (shutdown-agents))

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.tools.decompiler.compact :as compact])

(defn fail! [message data]
  (throw (ex-info message data)))

(defn head-name [form]
  (when (and (seq? form) (symbol? (first form)))
    (name (first form))))

(def approved-legacy-read-eval-path "ring/util/codec.clj")
(def approved-legacy-read-eval-source "#=(int \\=)")
(def approved-legacy-read-eval-replacement
  "(datomic.source-delta/legacy-read-eval (int \\=))")
(def approved-legacy-read-eval-file-sha256
  "d0369e8c98ced16d2b8d19a49745c807b37638af5fbc59a811716f3737f5d697")

(defn file-sha256 [file]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        buffer (byte-array 8192)]
    (with-open [input (io/input-stream file)]
      (loop []
        (let [read-count (.read input buffer)]
          (when (pos? read-count)
            (.update digest buffer 0 read-count)
            (recur)))))
    (apply str (map #(format "%02x" (bit-and (int %) 0xff))
                    (.digest digest)))))

(defn source-path [file]
  (str/replace (.getCanonicalPath ^java.io.File file)
               java.io.File/separator
               "/"))

(defn active-read-eval-offsets [source]
  ;; Find reader-eval dispatches without mistaking text in strings, regexes,
  ;; character literals, or comments for executable reader syntax.  Unknown
  ;; active #= forms are still handed to core/read with *read-eval* false and
  ;; therefore fail closed.
  (let [length (count source)]
    (loop [offset 0 state :code escaped? false offsets []]
      (if (>= offset length)
        offsets
        (let [ch (.charAt ^String source offset)
              next-ch (when (< (inc offset) length)
                        (.charAt ^String source (inc offset)))]
          (case state
            :comment
            (recur (inc offset) (if (= ch \newline) :code :comment)
                   false offsets)

            :string
            (cond
              escaped? (recur (inc offset) :string false offsets)
              (= ch \\) (recur (inc offset) :string true offsets)
              (= ch \" ) (recur (inc offset) :code false offsets)
              :else (recur (inc offset) :string false offsets))

            :regex
            (cond
              escaped? (recur (inc offset) :regex false offsets)
              (= ch \\) (recur (inc offset) :regex true offsets)
              (= ch \" ) (recur (inc offset) :code false offsets)
              :else (recur (inc offset) :regex false offsets))

            :character
            (if (or (Character/isWhitespace ch)
                    (contains? #{\( \) \[ \] \{ \} \" \;} ch))
              (recur offset :code false offsets)
              (recur (inc offset) :character false offsets))

            :code
            (cond
              (= ch \;) (recur (inc offset) :comment false offsets)
              (= ch \" ) (recur (inc offset) :string false offsets)
              (and (= ch \#) (= next-ch \"))
              (recur (+ offset 2) :regex false offsets)
              (= ch \\) (recur (min length (+ offset 2))
                                  :character false offsets)
              (and (= ch \#) (= next-ch \=))
              (recur (+ offset 2) :code false (conj offsets offset))
              :else (recur (inc offset) :code false offsets))))))))

(defn reader-safe-source [file]
  (let [source (slurp file)
        offsets (active-read-eval-offsets source)]
    (cond
      (empty? offsets)
      source

      (and (= 1 (count offsets))
           (str/ends-with? (source-path file) approved-legacy-read-eval-path)
           (= approved-legacy-read-eval-file-sha256 (file-sha256 file))
           (str/starts-with? (subs source (first offsets))
                             approved-legacy-read-eval-source))
      (let [offset (first offsets)
            end (+ offset (count approved-legacy-read-eval-source))]
        (str (subs source 0 offset)
             approved-legacy-read-eval-replacement
             (subs source end)))

      :else
      (fail! "unapproved active reader-eval form"
             {:path (source-path file) :offsets offsets}))))

(defn quoted-symbol [form]
  (when (and (seq? form)
             (= "quote" (head-name form))
             (= 2 (count form))
             (symbol? (second form)))
    (second form)))

(defn qualified-lib [prefix lib]
  (if prefix
    (symbol (str prefix "." lib))
    lib))

(declare libspec-aliases)

(defn libspec-aliases [prefix spec]
  (let [spec (if (quoted-symbol spec) (quoted-symbol spec) spec)]
    (cond
      (symbol? spec)
      []

      (not (and (sequential? spec)
                (or (symbol? (first spec))
                    (quoted-symbol (first spec)))))
      (fail! "malformed namespace libspec" {:prefix prefix :libspec spec})

      :else
      (let [lib-name (or (quoted-symbol (first spec)) (first spec))
            lib (qualified-lib prefix lib-name)]
        (loop [items (seq (rest spec)) aliases []]
          (if-not items
            aliases
            (let [item (first items)]
              (cond
                (contains? #{:reload :reload-all :verbose} item)
                (recur (next items) aliases)

                (keyword? item)
                (let [remaining (next items)]
                  (when-not remaining
                    (fail! "namespace libspec option lacks a value"
                           {:libspec spec :option item}))
                  (let [value (first remaining)
                        alias-name (or (quoted-symbol value) value)]
                    (when (and (contains? #{:as :as-alias} item)
                               (not (symbol? alias-name)))
                      (fail! "namespace alias must be a symbol"
                             {:libspec spec :option item :value value}))
                    (recur (next remaining)
                           (cond-> aliases
                             (contains? #{:as :as-alias} item)
                             (conj [alias-name lib])))))

                (sequential? item)
                (recur (next items)
                       (into aliases (libspec-aliases lib item)))

                :else
                (fail! "malformed namespace libspec tail"
                       {:libspec spec :value item})))))))))

(defn unique-alias-map [entries context]
  (reduce
    (fn [aliases [alias-name target]]
      (when (contains? aliases alias-name)
        (fail! "duplicate namespace alias"
               {:context context
                :alias alias-name
                :first (get aliases alias-name)
                :second target}))
      (assoc aliases alias-name target))
    (sorted-map)
    entries))

(defn ns-form-aliases [form]
  (let [entries
        (->> (drop 2 form)
             (filter sequential?)
             (filter #(contains? #{"require" "require-macros"}
                                 (some-> % first name)))
             (mapcat rest)
             (mapcat #(libspec-aliases nil %)))]
    (unique-alias-map entries {:kind :ns-form :namespace (second form)})))

(defn source-ns-form [form]
  (when (and (seq? form)
             (= "ns" (head-name form))
             (symbol? (second form)))
    {:name (second form) :aliases (ns-form-aliases form)}))

(defn unquoted-form [form]
  (if (and (seq? form) (= "quote" (head-name form)) (= 2 (count form)))
    (second form)
    form))

(defn low-level-source-context [form]
  (let [records
        (letfn [(walk [value]
                  (cond
                    (= "quote" (head-name value)) []

                    (= "in-ns" (head-name value))
                    (let [namespace-name (unquoted-form (second value))]
                      (when-not (symbol? namespace-name)
                        (fail! "low-level in-ns target must be a quoted symbol"
                               {:form value}))
                      [{:kind :namespace :name namespace-name}])

                    (= "require" (head-name value))
                    [{:kind :aliases
                      :entries
                      (mapcat #(libspec-aliases nil (unquoted-form %))
                              (rest value))}]

                    (coll? value) (mapcat walk value)
                    :else []))]
          (walk form))
        namespace-names (map :name (filter #(= :namespace (:kind %)) records))
        namespace-name (first namespace-names)
        alias-entries (mapcat :entries (filter #(= :aliases (:kind %)) records))]
    (when (seq namespace-names)
      (when-not (= 1 (count (distinct namespace-names)))
        (fail! "low-level source switches namespaces during setup"
               {:namespaces (vec namespace-names)}))
      {:name namespace-name
       :aliases (unique-alias-map alias-entries
                                  {:kind :low-level
                                   :namespace namespace-name})})))

(defn restore-aliases! [namespace aliases]
  (doseq [alias-name (keys (ns-aliases namespace))]
    (ns-unalias namespace alias-name))
  (binding [*ns* namespace]
    (doseq [[alias-name target] aliases]
      (alias alias-name (ns-name target)))))

(defn ensure-reader-namespace! [created name]
  (or (find-ns name)
      (let [namespace (create-ns name)]
        (swap! created conj name)
        namespace)))

(defn configure-source-namespace! [touched created {:keys [name aliases]}]
  (let [namespace (ensure-reader-namespace! created name)]
    (when-not (contains? @touched name)
      (swap! touched assoc name (ns-aliases namespace)))
    (restore-aliases! namespace {})
    (binding [*ns* namespace]
      (doseq [[alias-name target-name] aliases]
        (ensure-reader-namespace! created target-name)
        (alias alias-name target-name)))
    namespace))

(defn read-forms [file]
  (let [source (reader-safe-source file)
        ;; The scratch namespace must never accidentally satisfy a source
        ;; alias.  It is removed after the read.
        namespaces-before (set (map ns-name (all-ns)))
        scratch-name (symbol (str "datomic.source-delta.reader." (gensym)))
        scratch (create-ns scratch-name)
        touched (atom {})
        created (atom #{scratch-name})]
    (try
      (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                           (java.io.StringReader. source))]
        (let [eof (Object.)
              options (cond-> {:eof eof}
                        (str/ends-with? (.getName ^java.io.File file) ".cljc")
                        (assoc :read-cond :allow :features #{:clj}))]
          (binding [*read-eval* false]
            (loop [forms [] current-ns scratch]
              (let [form (binding [*ns* current-ns]
                           (read options reader))]
                (if (identical? eof form)
                  forms
                  (if-let [declaration (source-ns-form form)]
                    (recur (conj forms form)
                           (configure-source-namespace!
                             touched created declaration))
                    (recur (conj forms form) current-ns))))))))
      (finally
        (doseq [[name aliases] @touched]
          (when-let [namespace (and (not (contains? @created name))
                                    (find-ns name))]
            (restore-aliases! namespace aliases)))
        (doseq [name (reverse (sort @created))]
          (when (find-ns name)
            (remove-ns name)))
        (let [namespaces-after (set (map ns-name (all-ns)))]
          (when-not (= namespaces-before namespaces-after)
            (fail! "source reader leaked namespace state"
                   {:added (sort (remove namespaces-before namespaces-after))
                    :removed (sort (remove namespaces-after namespaces-before))})))))))

(def location-metadata
  #{:file :line :column :end-line :end-column})

(declare encode-form)

(defn encode-metadata [form]
  (let [metadata (apply dissoc (meta form) location-metadata)]
    (when (seq metadata)
      (->> metadata
           (map (fn [[k v]] [(encode-form k) (encode-form v)]))
           (sort-by pr-str)
           vec))))

(defn encode-form [form]
  (cond
    (symbol? form) [:symbol (str form) (encode-metadata form)]
    (keyword? form) [:keyword (str form) (encode-metadata form)]
    (instance? java.util.regex.Pattern form)
    [:regex (.pattern ^java.util.regex.Pattern form)
     (.flags ^java.util.regex.Pattern form)]
    (vector? form) [:vector (mapv encode-form form) (encode-metadata form)]
    (map? form) [:map (->> form
                           (map (fn [[k v]] [(encode-form k) (encode-form v)]))
                           (sort-by pr-str)
                           vec)
                 (encode-metadata form)]
    (set? form) [:set (->> form (map encode-form) (sort-by pr-str) vec)
                 (encode-metadata form)]
    (seq? form) [:list (mapv encode-form form) (encode-metadata form)]
    :else [:value form (encode-metadata form)]))

(defn sha256 [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        octets (.getBytes ^String (pr-str value) "UTF-8")]
    (.update digest octets)
    (format "%064x" (java.math.BigInteger. 1 (.digest digest)))))

(defn clauses-from-body [body]
  (cond
    (vector? (first body))
    [(list* (first body) (rest body))]

    (and (seq body)
         (every? #(and (seq? %) (vector? (first %))) body))
    (vec body)

    :else
    nil))

(defn definition-record [form]
  (case (head-name form)
    "defn"
    (let [definition-name (second form)
          tail (drop 2 form)
          tail (if (string? (first tail)) (rest tail) tail)
          tail (if (map? (first tail)) (rest tail) tail)
          clauses (clauses-from-body tail)]
      (when (and (symbol? definition-name) clauses)
        {:name definition-name
         :kind :defn
         :fn-name definition-name
         :clauses clauses}))

    "def"
    (let [definition-name (second form)
          initializer (nth form 2 nil)]
      (when (and (symbol? definition-name)
                 (= "fn" (head-name initializer)))
        (let [tail (rest initializer)
              [fn-name tail] (if (symbol? (first tail))
                               [(first tail) (rest tail)]
                               [nil tail])
              clauses (clauses-from-body tail)]
          (when clauses
            {:name definition-name
             :kind :def+fn
             :fn-name fn-name
             :clauses clauses}))))

    nil))

(defn scalar-definition-record [form]
  ;; This intentionally recognizes only the canonical, initialized three-form
  ;; `def` shape.  Doc-string variants, declarations, function-valued defs,
  ;; and qualified definition symbols remain ordinary source deltas.
  (when (and (= "def" (head-name form))
             (= 3 (count form))
             (symbol? (second form))
             (nil? (namespace (second form)))
             (not= "fn" (head-name (nth form 2))))
    {:name (second form)
     :kind :scalar-def
     :initializer (nth form 2)}))

(defn canonical-var-definition [form]
  (if-let [function-definition (definition-record form)]
    (when (nil? (namespace (:name function-definition)))
      {:name (:name function-definition)
       :kind :function
       :source-kind (:kind function-definition)
       :clauses (:clauses function-definition)})
    (scalar-definition-record form)))

(defn collect-definitions [form]
  (let [definition (definition-record form)]
    (cond
      definition [definition]
      (= "quote" (head-name form)) []
      (coll? form) (mapcat collect-definitions form)
      :else [])))

(defn reset-target [target]
  (cond
    (and (seq? target)
         (= "var" (head-name target))
         (= 2 (count target))
         (symbol? (second target)))
    {:kind :var-symbol
     :name (symbol (name (second target)))
     :namespace (namespace (second target))
     :source target}

    (and (seq? target)
         (= "var" (head-name target))
         (= 3 (count target))
         (string? (second target))
         (string? (nth target 2)))
    {:kind :runtime-var
     :name (symbol (nth target 2))
     :namespace (second target)
     :source target}

    :else
    {:kind :unknown :source target}))

(defn reset-metadata-base [metadata-expression]
  (when (and (= "assoc" (head-name metadata-expression))
             (= 6 (count metadata-expression))
             (= [:name :ns]
                [(nth metadata-expression 2 nil)
                 (nth metadata-expression 4 nil)]))
    (second metadata-expression)))

(defn reset-record [form]
  (when (= "reset-meta!" (head-name form))
    (let [target (reset-target (second form))
          metadata-expression (nth form 2 nil)]
      {:form form
       :target target
       :name (:name target)
       :metadata-expression metadata-expression
       :metadata (reset-metadata-base metadata-expression)})))

(defn collect-resets [form]
  (let [reset (reset-record form)]
    (cond
      reset [reset]
      (= "quote" (head-name form)) []
      (coll? form) (mapcat collect-resets form)
      :else [])))

(defn runtime-var-target [target]
  (when (and (seq? target)
             (= 'clojure.lang.RT/var (first target))
             (= 3 (count target))
             (string? (second target))
             (string? (nth target 2)))
    (let [raw-name (nth target 2)
          parsed-name (symbol raw-name)]
      {:namespace (second target)
       :name (symbol (name parsed-name))
       :qualified-name? (boolean (namespace parsed-name))
       :raw-name raw-name
       :source target})))

(defn low-level-var-operation [method form]
  (when (and (seq? form)
             (= method (first form))
             (= 3 (count form)))
    (when-let [target (runtime-var-target (second form))]
      {:method method
       :target target
       :payload (nth form 2)
       :form form})))

(defn var-initialization-unit [items index]
  (let [set-operation
        (low-level-var-operation '.setMeta (nth items index nil))
        bind-operation
        (low-level-var-operation '.bindRoot (nth items (inc index) nil))]
    (when (and set-operation bind-operation)
      {:set set-operation
       :bind bind-operation
       ;; Match an old definition using the set operation's simple Var name;
       ;; the proof below still rejects a qualified or otherwise changed target.
       :name (get-in set-operation [:target :name])})))

(defn direct-source-items [forms]
  (if (and (= 1 (count forms))
           (= "do" (head-name (first forms))))
    (rest (first forms))
    forms))

(defn direct-source-namespace-symbol [form]
  (cond
    (and (= "quote" (head-name form))
         (= 2 (count form))
         (symbol? (second form)))
    (second form)

    (and (= ".withMeta" (head-name form))
         (= 3 (count form))
         (map? (nth form 2)))
    (direct-source-namespace-symbol (second form))

    :else nil))

(defn direct-namespace-declaration [form]
  (or (some-> (source-ns-form form) :name)
      (when (= "in-ns" (head-name form))
        (let [name (direct-source-namespace-symbol (second form))]
          (when-not (symbol? name)
            (fail! "direct in-ns target must be an exact quoted symbol"
                   {:form form}))
          name))))

(defn exact-direct-source-namespace [forms]
  (let [names (->> (direct-source-items forms)
                   (keep direct-namespace-declaration)
                   distinct
                   vec)]
    (when (= 1 (count names))
      (first names))))

(defn canonical-definition-identity [definition]
  [(:kind definition)
   (:name definition)
   (sha256 (encode-form
     (if (= :function (:kind definition))
       (:clauses definition)
       (:initializer definition))))])

(defn direct-definition-namespace-map [forms]
  (:namespaces
    (reduce
      (fn [{:keys [current] :as state} form]
        (if-let [namespace-name (direct-namespace-declaration form)]
          (assoc state :current namespace-name)
          (if-let [definition (canonical-var-definition form)]
            (update-in state [:namespaces
                              (canonical-definition-identity definition)]
                       (fnil conj #{}) current)
            state)))
      {:current nil :namespaces {}}
      (direct-source-items forms))))

(defn plain-argument-form [form canonical-spelling?]
  (cond
    (and (= "quote" (head-name form)) (= 2 (count form)))
    (plain-argument-form (second form) canonical-spelling?)

    (and (= ".withMeta" (head-name form)) (= 3 (count form)))
    (plain-argument-form (second form) canonical-spelling?)

    (symbol? form)
    (if canonical-spelling?
      (symbol (namespace form) (str/replace (name form) "_" "-"))
      form)

    (vector? form) (mapv #(plain-argument-form % canonical-spelling?) form)
    (map? form) (into {} (map (fn [[k v]]
                                [(plain-argument-form k canonical-spelling?)
                                 (plain-argument-form v canonical-spelling?)]))
                       form)
    (set? form) (set (map #(plain-argument-form % canonical-spelling?) form))
    (seq? form) (apply list (map #(plain-argument-form % canonical-spelling?) form))
    :else form))

(defn conversion-reason [definition reset]
  (let [metadata (:metadata reset)
        advertised (when (map? metadata)
                     (compact/literal-arglists (:arglists metadata)))
        implemented (mapv first (:clauses definition))
        advertised-shapes (when advertised
                            (mapv compact/parameter-arity-shape advertised))
        implemented-shapes (mapv compact/parameter-arity-shape implemented)]
    (cond
      (nil? reset) :original-arglists-absent
      (not (map? metadata)) :nonliteral-metadata
      (contains? metadata :doc) :doc-preservation
      (not (contains? metadata :arglists)) :original-arglists-absent-with-metadata
      (nil? advertised) :malformed-arglists
      (not= advertised-shapes implemented-shapes)
      (if (= (frequencies advertised-shapes) (frequencies implemented-shapes))
        :arglist-order-mismatch
        :arity-shape-mismatch)
      (= (mapv #(plain-argument-form % false) advertised)
         (mapv #(plain-argument-form % false) implemented))
      :argument-metadata-mismatch
      (= (mapv #(plain-argument-form % true) advertised)
         (mapv #(plain-argument-form % true) implemented))
      :compiler-local-spelling-mismatch
      :else :argument-form-or-name-mismatch)))

(defn relative-source-paths [root]
  (let [root-file (.getCanonicalFile (io/file root))
        prefix (str (.getCanonicalPath root-file) java.io.File/separator)]
    (->> (file-seq root-file)
         (filter #(.isFile ^java.io.File %))
         (map #(.getCanonicalPath ^java.io.File %))
         (filter #(or (str/ends-with? % ".clj") (str/ends-with? % ".cljc")))
         (map #(subs % (count prefix)))
         sort
         vec)))

(defn encoded= [left right]
  (= (encode-form left) (encode-form right)))

(defn encoded-sha256 [form]
  (sha256 (encode-form form)))

(def exact-declared-metadata
  {:declared true :column '(int 1)})

(defn direct-declare-record [source-namespace form]
  (when (and source-namespace
             (= "declare" (head-name form))
             (= 2 (count form))
             (symbol? (second form)))
    (let [declared (second form)
          declared-namespace
          (or (namespace declared) (str source-namespace))]
      {:form form
       :namespace declared-namespace
       :name (symbol (name declared))
       :qualified? (boolean (namespace declared))})))

(defn declared-setmeta-record [form]
  (when-let [operation (low-level-var-operation '.setMeta form)]
    {:form form
     :target (:target operation)
     :namespace (get-in operation [:target :namespace])
     :name (get-in operation [:target :name])
     :qualified? (get-in operation [:target :qualified-name?])
     :metadata (:payload operation)}))

(defn declare-identity [record]
  [(:namespace record) (:name record)])

(defn direct-record-counts [forms record-fn]
  (frequencies (keep (comp #(some-> % declare-identity) record-fn)
                     (direct-source-items forms))))

(defn exact-declare-setmeta-proof [old-form new-form state]
  (let [source-namespace (:source-namespace state)
        declaration (direct-declare-record source-namespace old-form)
        setmeta (declared-setmeta-record new-form)
        declaration-identity (some-> declaration declare-identity)
        setmeta-identity (some-> setmeta declare-identity)
        old-count (get (:old-declare-counts state)
                       declaration-identity 0)
        new-count (get (:new-setmeta-counts state)
                       setmeta-identity 0)
        metadata-exact? (and setmeta
                             (encoded= exact-declared-metadata
                                       (:metadata setmeta)))
        violations
        (cond-> []
          (nil? declaration) (conj :not-exact-one-symbol-declare)
          (nil? setmeta) (conj :not-low-level-setmeta)
          (and declaration
               (not= (str source-namespace) (:namespace declaration)))
          (conj :declaration-namespace-does-not-match-source)
          (and setmeta
               (not= (str source-namespace) (:namespace setmeta)))
          (conj :setmeta-namespace-does-not-match-source)
          (and setmeta (:qualified? setmeta))
          (conj :qualified-runtime-var-name)
          (and declaration setmeta
               (not= declaration-identity setmeta-identity))
          (conj :var-identity-mismatch)
          (and declaration (not= 1 old-count))
          (conj :old-declaration-identity-not-unique)
          (and setmeta (not= 1 new-count))
          (conj :new-setmeta-identity-not-unique)
          (not metadata-exact?)
          (conj :metadata-not-exact-declared-column))]
    {:valid? (empty? violations)
     :violations violations
     :declaration declaration
     :setmeta setmeta
     :identity declaration-identity
     :old-count old-count
     :new-count new-count
     :metadata-sha256 (when setmeta
                        (encoded-sha256 (:metadata setmeta)))}))

(defn add-event [state kind path data]
  (update state :events conj
          (merge (sorted-map :kind kind :path path) data)))

(defn add-issue [state kind path data]
  (update state :issues conj
          (merge (sorted-map :kind kind :path path) data)))

(defn target-matches-definition? [reset definition]
  (and reset
       (= :var-symbol (get-in reset [:target :kind]))
       (nil? (get-in reset [:target :namespace]))
       (= (:name definition) (:name reset))))

(defn exact-function-reset-proof [definition reset]
  (let [metadata-expression (:metadata-expression reset)
        metadata (:metadata reset)
        advertised (when (map? metadata)
                     (compact/literal-arglists (:arglists metadata)))
        violations
        (cond-> []
          (not (target-matches-definition? reset definition))
          (conj :wrong-target)

          (not (and (= "assoc" (head-name metadata-expression))
                    (= 6 (count metadata-expression))))
          (conj :noncanonical-assoc)

          (not= (:name definition)
                (quoted-symbol (nth metadata-expression 3 nil)))
          (conj :wrong-name-payload)

          (not= '*ns* (nth metadata-expression 5 nil))
          (conj :wrong-ns-payload)

          (not (map? metadata))
          (conj :nonliteral-metadata)

          (and (map? metadata) (not (contains? metadata :arglists)))
          (conj :missing-arglists)

          )]
    {:valid? (empty? violations)
     :violations violations
     :conversion-reason (conversion-reason definition reset)
     :payload-sha256 (encoded-sha256 metadata-expression)}))

(defn exact-reset-payload-proof [definition-name reset]
  (let [metadata-expression (:metadata-expression reset)
        metadata (:metadata reset)
        violations
        (cond-> []
          (not (and reset
                    (= :var-symbol (get-in reset [:target :kind]))
                    (nil? (get-in reset [:target :namespace]))
                    (= definition-name (:name reset))))
          (conj :wrong-target)

          (not (and (= "assoc" (head-name metadata-expression))
                    (= 6 (count metadata-expression))))
          (conj :noncanonical-assoc)

          (not= definition-name
                (quoted-symbol (nth metadata-expression 3 nil)))
          (conj :wrong-name-payload)

          (not= '*ns* (nth metadata-expression 5 nil))
          (conj :wrong-ns-payload)

          (not (map? metadata))
          (conj :nonliteral-metadata))]
    {:valid? (empty? violations)
     :violations violations
     :payload-sha256 (encoded-sha256 metadata-expression)}))

(defn exact-int-cast-of? [old-value new-form]
  (and (integer? old-value)
       (= (int old-value) old-value)
       (seq? new-form)
       (= 'int (first new-form))
       (= 2 (count new-form))
       (encoded= old-value (second new-form))))

(def exact-added-column-metadata
  {:column '(int 1)})

(defn exact-var-unit-metadata-proof [old-reset new-metadata]
  (let [old-metadata (:metadata old-reset)
        old-payload-proof
        (when old-reset
          (exact-reset-payload-proof (:name old-reset) old-reset))
        old-keys (when (map? old-metadata) (set (keys old-metadata)))
        new-keys (when (map? new-metadata) (set (keys new-metadata)))
        relation
        (cond
          old-reset :old-reset-plus-exact-column-int-cast
          :else :exact-added-column-only)
        violations
        (cond-> []
          (not (map? new-metadata))
          (conj :nonliteral-new-metadata)

          (and old-reset (not (:valid? old-payload-proof)))
          (into (map #(keyword (str "old-reset-" (name %)))
                     (:violations old-payload-proof)))

          (and old-reset (not (map? old-metadata)))
          (conj :nonliteral-old-metadata)

          (and old-reset (map? old-metadata)
               (not= old-keys new-keys))
          (conj :metadata-key-set-changed)

          (and old-reset (map? old-metadata)
               (not (contains? old-metadata :column)))
          (conj :old-column-absent)

          (and old-reset (map? old-metadata) (map? new-metadata)
               (not (encoded= (dissoc old-metadata :column)
                              (dissoc new-metadata :column))))
          (conj :semantic-metadata-changed)

          (and old-reset (map? old-metadata) (map? new-metadata)
               (not (exact-int-cast-of? (:column old-metadata)
                                        (:column new-metadata))))
          (conj :column-not-exact-int-cast)

          (and (not old-reset)
               (not (encoded= exact-added-column-metadata new-metadata)))
          (conj :unexpected-added-metadata))]
    {:valid? (empty? violations)
     :violations violations
     :relation relation
     :old-reset (boolean old-reset)
     :old-metadata-sha256 (when old-reset (encoded-sha256 old-metadata))
     :new-metadata-sha256 (encoded-sha256 new-metadata)}))

(defn exact-var-initialization-proof
  [definition unit old-reset expected-source-namespaces]
  (let [set-target (get-in unit [:set :target])
        bind-target (get-in unit [:bind :target])
        bind-value (get-in unit [:bind :payload])
        bound-function
        (when (= :function (:kind definition))
          (definition-record
            (list 'def (:name definition) bind-value)))
        root-relation
        (if (= :function (:kind definition))
          :exact-encoded-function-clauses
          :exact-encoded-initializer)
        root-match?
        (if (= :function (:kind definition))
          (and bound-function
               (encoded= (:clauses definition) (:clauses bound-function)))
          (encoded= (:initializer definition) bind-value))
        metadata-proof
        (exact-var-unit-metadata-proof
          old-reset (get-in unit [:set :payload]))
        expected-source-namespace
        (when (= 1 (count expected-source-namespaces))
          (first expected-source-namespaces))
        expected-namespace (some-> expected-source-namespace str)
        violations
        (cond-> []
          (or (not= 1 (count expected-source-namespaces))
              (nil? expected-source-namespace))
          (conj :source-namespace-unknown)

          (get set-target :qualified-name?)
          (conj :qualified-set-target-name)

          (get bind-target :qualified-name?)
          (conj :qualified-bind-target-name)

          (not= (:source set-target) (:source bind-target))
          (conj :targets-differ)

          (not= (:name definition) (:name set-target))
          (conj :wrong-set-target-name)

          (not= (:name definition) (:name bind-target))
          (conj :wrong-bind-target-name)

          (not= expected-namespace (:namespace set-target))
          (conj :wrong-set-target-namespace)

          (not= expected-namespace (:namespace bind-target))
          (conj :wrong-bind-target-namespace)

          (not root-match?)
          (conj (if (= :function (:kind definition))
                  :function-clauses-changed
                  :initializer-changed))

          (not (:valid? metadata-proof))
          (into (map #(keyword (str "metadata-" (name %)))
                     (:violations metadata-proof))))]
    {:valid? (empty? violations)
     :violations violations
     :root-relation root-relation
     :root-match? root-match?
     :metadata-proof metadata-proof
     :target [(get set-target :namespace) (str (get set-target :name))]
     :old-root-sha256
     (encoded-sha256
       (if (= :function (:kind definition))
         (:clauses definition)
         (:initializer definition)))
     :new-root-sha256
     (encoded-sha256
       (if (= :function (:kind definition))
         (:clauses bound-function)
         bind-value))}))

(defn relation-state
  ([] (relation-state {}))
  ([old-definition-namespaces]
   {:events []
    :issues []
    :seen-definitions #{}
    :paired-resets #{}
    :old-definition-namespaces old-definition-namespaces
    :source-namespace nil
    :old-declare-counts {}
    :new-setmeta-counts {}
    :old-direct-reset-counts {}
    :new-direct-reset-counts {}
    :old-protocol-unit-counts {}
    :new-protocol-unit-counts {}
    :protocol-unit-order-valid? true
    :old-defmulti-unit-counts {}
    :new-defmulti-unit-counts {}
    :defmulti-unit-order-valid? true}))

(defn reset-identity [reset]
  [(get-in reset [:target :namespace]) (:name reset)])

(defn rebuild-reset-with-metadata [form metadata]
  (let [metadata-expression (nth form 2)
        normalized-expression
        (with-meta
          (apply list (assoc (vec metadata-expression) 1 metadata))
          (meta metadata-expression))]
    (with-meta (apply list (assoc (vec form) 2 normalized-expression))
               (meta form))))

(defn exact-reset-column-cast-proof [old-form new-form state]
  (let [old-reset (reset-record old-form)
        new-reset (reset-record new-form)
        old-metadata (:metadata old-reset)
        new-metadata (:metadata new-reset)
        old-column (when (map? old-metadata) (:column old-metadata))
        new-column (when (map? new-metadata) (:column new-metadata))
        old-identity (some-> old-reset reset-identity)
        new-identity (some-> new-reset reset-identity)
        old-count (get (:old-direct-reset-counts state) old-identity 0)
        new-count (get (:new-direct-reset-counts state) new-identity 0)
        normalized-new-metadata
        (when (map? new-metadata)
          (with-meta (assoc new-metadata :column old-column)
                     (meta new-metadata)))
        normalized-new-form
        (when normalized-new-metadata
          (rebuild-reset-with-metadata new-form normalized-new-metadata))
        violations
        (cond-> []
          (nil? old-reset) (conj :old-not-canonical-reset)
          (nil? new-reset) (conj :new-not-canonical-reset)

          (and old-reset new-reset
               (not (encoded= (get-in old-reset [:target :source])
                              (get-in new-reset [:target :source]))))
          (conj :target-form-changed)

          (not (map? old-metadata))
          (conj :old-metadata-not-literal-map)

          (not (map? new-metadata))
          (conj :new-metadata-not-literal-map)

          (not (integer? old-column))
          (conj :old-column-not-integer)

          (and (integer? old-column)
               (not (exact-int-cast-of? old-column new-column)))
          (conj :new-column-not-exact-int-cast)

          (and old-reset (not= 1 old-count))
          (conj :old-reset-identity-not-unique)

          (and new-reset (not= 1 new-count))
          (conj :new-reset-identity-not-unique)

          (and old-reset new-reset normalized-new-form
               (not (encoded= old-form normalized-new-form)))
          (conj :non-column-reset-content-changed))]
    {:valid? (empty? violations)
     :violations violations
     :identity old-identity
     :old-count old-count
     :new-count new-count
     :old-column old-column
     :new-column new-column
     :old-sha256 (encoded-sha256 old-form)
     :new-sha256 (encoded-sha256 new-form)}))

(defn direct-reset-identity-counts [forms]
  (frequencies
    (keep (fn [form]
            (some-> (reset-record form) reset-identity))
          (direct-source-items forms))))

(defn exact-reset-column-cast-event-data [proof]
  {:target (:identity proof)
   :old-identity-count (:old-count proof)
   :new-identity-count (:new-count proof)
   :old-column (:old-column proof)
   :new-column (:new-column proof)
   :old-sha256 (:old-sha256 proof)
   :new-sha256 (:new-sha256 proof)
   :valid (:valid? proof)
   :violations (:violations proof)})

(defn protocol-parts [form]
  (when (and (= "defprotocol" (head-name form))
             (symbol? (second form)))
    (let [tail (drop 2 form)
          doc (when (string? (first tail)) (first tail))
          tail (if doc (next tail) tail)
          options (when (map? (first tail)) (first tail))
          methods (vec (if options (next tail) tail))]
      (when (every? #(and (seq? %) (symbol? (first %))) methods)
        {:name (second form)
         :doc doc
         :options options
         :methods methods}))))

(defn protocol-method-parts [method]
  (let [tail (rest method)
        doc (when (string? (last tail)) (last tail))
        arities (vec (if doc (butlast tail) tail))]
    (when (and (symbol? (first method))
               (seq arities)
               (every? vector? arities))
      {:name (first method)
       :arities arities
       :doc doc})))

(defn protocol-without-relocated-docs [form]
  (when-let [{:keys [name options methods]} (protocol-parts form)]
    (with-meta
      (apply list
             (concat
               [(first form) name]
               (when options [options])
               (for [method methods]
                 (let [items (if (string? (last method))
                               (butlast method)
                               method)]
                   (with-meta
                     (apply list items)
                     (meta method))))))
      (meta form))))

(defn protocol-runtime-target [source-namespace name]
  (list 'clojure.lang.RT/var (str source-namespace) (str name)))

(defn exact-protocol-method-wrapper?
  [source-namespace protocol-name old-method method method-wrapper]
  (let [{:keys [name arities doc]} (protocol-method-parts method)
        bindings (second method-wrapper)
        signature-symbol (nth bindings 0 nil)
        signature-expression (nth bindings 1 nil)
        method-name-symbol (nth bindings 2 nil)
        method-name-expression (nth bindings 3 nil)
        signature-map (nth signature-expression 1 nil)
        protocol-target (protocol-runtime-target
                          source-namespace protocol-name)
        method-target (protocol-runtime-target source-namespace name)
        arglists (:arglists signature-map)
        literal-arglists (compact/literal-arglists arglists)
        advertised-name (:name signature-map)
        advertised-name-metadata (nth advertised-name 2 nil)
        expected-tag (when-let [tag (:tag (meta (first old-method)))]
                       (list 'quote tag))
        expected-signature-expression
        (list 'assoc signature-map :protocol protocol-target)
        expected-name-expression
        (list 'with-meta (list :name signature-symbol) signature-symbol)
        expected-reset
        (list 'reset-meta! method-target
              (list 'assoc signature-symbol :name method-name-symbol
                    :ns '*ns*))]
    (and (= "let" (head-name method-wrapper))
         (vector? bindings)
         (= 4 (count bindings))
         (symbol? signature-symbol)
         (symbol? method-name-symbol)
         (not= signature-symbol method-name-symbol)
         (= 3 (count method-wrapper))
         (= "assoc" (head-name signature-expression))
         (= 4 (count signature-expression))
         (map? signature-map)
         (= #{:tag :name :arglists :doc} (set (keys signature-map)))
         (= doc (:doc signature-map))
         (encoded= expected-tag (:tag signature-map))
         (= "list" (head-name arglists))
         (encoded= arities
                   (mapv #(plain-argument-form % false)
                         literal-arglists))
         (= ".withMeta" (head-name advertised-name))
         (= 3 (count advertised-name))
         (= name (quoted-symbol (second advertised-name)))
         (map? advertised-name-metadata)
         (= #{:arglists} (set (keys advertised-name-metadata)))
         (encoded= arglists (:arglists advertised-name-metadata))
         (encoded= expected-signature-expression signature-expression)
         (encoded= expected-name-expression method-name-expression)
         (encoded= expected-reset (nth method-wrapper 2)))))

(defn old-protocol-initialization-unit [items index]
  (let [cache (nth items index nil)
        protocol (nth items (inc index) nil)
        parts (protocol-parts protocol)]
    (when (and (= "defonce" (head-name cache))
               parts
               (= (second cache) (:name parts)))
      {:name (:name parts)
       :index index
       :cache cache
       :protocol protocol})))

(defn new-protocol-initialization-unit [form]
  (when (seq? form)
    (let [parts (protocol-parts (nth form 2 nil))]
      (when (and (= "let" (head-name form)) parts)
        {:name (:name parts)
         :wrapper form}))))

(defn direct-old-protocol-initialization-units [forms]
  (let [items (vec (direct-source-items forms))]
    (keep-indexed
      (fn [index _]
        (old-protocol-initialization-unit items index))
      items)))

(defn direct-new-protocol-initialization-units [forms]
  (keep-indexed
    (fn [index form]
      (some-> (new-protocol-initialization-unit form)
              (assoc :index index)))
    (direct-source-items forms)))

(defn protocol-unit-counts [units]
  (frequencies (map :name units)))

(defn protocol-unit-order-valid?
  [old-units new-units old-counts new-counts]
  (let [unique-old
        (set (for [[identity count] old-counts :when (= 1 count)]
               identity))
        unique-new
        (set (for [[identity count] new-counts :when (= 1 count)]
               identity))
        common (set (filter unique-new unique-old))]
    (= (filterv common (map :name old-units))
       (filterv common (map :name new-units)))))

(defn exact-protocol-initialization-proof
  [old-unit new-unit state]
  (let [source-namespace (:source-namespace state)
        old-cache (:cache old-unit)
        old-protocol (:protocol old-unit)
        new-wrapper (:wrapper new-unit)
        old-parts (protocol-parts old-protocol)
        bindings (second new-wrapper)
        metadata-symbol (nth bindings 0 nil)
        protocol-metadata (nth bindings 1 nil)
        embedded (nth new-wrapper 2 nil)
        new-parts (protocol-parts embedded)
        protocol-name (:name old-parts)
        old-count (get (:old-protocol-unit-counts state) protocol-name 0)
        new-count (get (:new-protocol-unit-counts state) protocol-name 0)
        target (protocol-runtime-target source-namespace protocol-name)
        expected-cache (list 'defonce protocol-name {})
        expected-protocol-reset
        (list 'reset-meta! target
              (list 'assoc
                    (list 'assoc metadata-symbol :doc (:doc new-parts))
                    :name (list 'quote protocol-name) :ns '*ns*))
        method-wrappers (vec (drop 4 new-wrapper))
        violations
        (cond-> []
          (nil? source-namespace)
          (conj :missing-source-namespace)

          (nil? old-parts)
          (conj :old-not-protocol)

          (not (encoded= expected-cache old-cache))
          (conj :old-cache-not-exact-empty-defonce)

          (not= 1 old-count)
          (conj :old-identity-not-unique)

          (not= 1 new-count)
          (conj :new-identity-not-unique)

          (not (:protocol-unit-order-valid? state))
          (conj :protocol-identity-order-changed)

          (not (and (= "let" (head-name new-wrapper))
                    (vector? bindings)
                    (= 2 (count bindings))
                    (symbol? metadata-symbol)))
          (conj :new-not-canonical-protocol-wrapper)

          (not (encoded= {:column '(int 1)} protocol-metadata))
          (conj :protocol-metadata-not-exact-column)

          (nil? new-parts)
          (conj :wrapper-missing-protocol)

          (and old-parts new-parts
               (not= (:name old-parts) (:name new-parts)))
          (conj :protocol-name-changed)

          (and old-parts new-parts
               (not (encoded= old-protocol
                              (protocol-without-relocated-docs embedded))))
          (conj :non-doc-protocol-content-changed)

          (not (= (inc (count (:methods new-parts)))
                  (- (count new-wrapper) 3)))
          (conj :wrapper-body-count-mismatch)

          (not (encoded= expected-protocol-reset
                         (nth new-wrapper 3 nil)))
          (conj :protocol-reset-not-exact)

          (and new-parts
               (not= (count (:methods new-parts))
                     (count method-wrappers)))
          (conj :method-wrapper-count-mismatch)

          (and old-parts new-parts
               (= (count (:methods old-parts))
                  (count (:methods new-parts))
                  (count method-wrappers))
               (not (every? true?
                            (map #(exact-protocol-method-wrapper?
                                    source-namespace protocol-name %1 %2 %3)
                                 (:methods old-parts)
                                 (:methods new-parts)
                                 method-wrappers))))
          (conj :method-wrapper-not-exact))]
    {:valid? (empty? violations)
     :violations violations
     :identity protocol-name
     :old-count old-count
     :new-count new-count
     :old-cache-sha256 (encoded-sha256 old-cache)
     :old-protocol-sha256 (encoded-sha256 old-protocol)
     :new-wrapper-sha256 (encoded-sha256 new-wrapper)}))

(defn exact-protocol-initialization-event-data [proof]
  {:identity (:identity proof)
   :old-identity-count (:old-count proof)
   :new-identity-count (:new-count proof)
   :old-cache-sha256 (:old-cache-sha256 proof)
   :old-protocol-sha256 (:old-protocol-sha256 proof)
   :new-wrapper-sha256 (:new-wrapper-sha256 proof)
   :valid (:valid? proof)
   :violations (:violations proof)})

(defn unique-protocol-unit-map [units]
  (into {}
        (for [[identity matches] (group-by :name units)
              :when (= 1 (count matches))]
          [identity (first matches)])))

(defn exact-protocol-initialization-candidates
  [old-units new-units state]
  (let [old-map (unique-protocol-unit-map old-units)
        new-map (unique-protocol-unit-map new-units)]
    (into (sorted-map)
          (for [[identity old-unit] old-map
                :let [new-unit (get new-map identity)
                      proof
                      (when new-unit
                        (exact-protocol-initialization-proof
                          old-unit new-unit state))]
                :when (:valid? proof)]
            [(:index new-unit)
             {:identity identity
              :old-unit old-unit
              :new-unit new-unit
              :proof proof}]))))

(defn rebuild-direct-source-items [forms items]
  (if (and (= 1 (count forms))
           (= "do" (head-name (first forms))))
    [(with-meta (apply list (cons (first (first forms)) items))
                (meta (first forms)))]
    (vec items)))

(defn canonicalize-exact-protocol-initialization-units
  [new-forms candidates]
  (rebuild-direct-source-items
    new-forms
    (mapcat
      (fn [index form]
        (if-let [candidate (get candidates index)]
          [(get-in candidate [:old-unit :cache])
           (get-in candidate [:old-unit :protocol])]
          [form]))
      (range) (direct-source-items new-forms))))

(defn add-exact-protocol-initialization-events
  [state candidates]
  (reduce
    (fn [state [_ {:keys [identity old-unit new-unit proof]}]]
      (add-event
        state :exact-protocol-initialization-unit
        [:forms
         [:old-unit [(:index old-unit) (inc (:index old-unit))]
          :new (:index new-unit)]]
        (assoc (exact-protocol-initialization-event-data proof)
               :identity identity)))
    state candidates))

(defn exact-list-head-count? [head count form]
  (and (seq? form)
       (= head (first form))
       (= count (clojure.core/count form))))

(defn exact-simple-unannotated-symbol? [value]
  (and (symbol? value)
       (nil? (namespace value))
       (nil? (encode-metadata value))))

(defn old-defmulti-initialization-unit [form index]
  (when (and (seq? form)
             (= "defmulti" (head-name form))
             (symbol? (second form)))
    {:name (symbol (name (second form)))
     :index index
     :form form}))

(defn new-defmulti-initialization-unit [items index]
  (let [set-form (nth items index nil)
        wrapper (nth items (inc index) nil)
        set-operation (low-level-var-operation '.setMeta set-form)]
    (when (and set-operation (= "let" (head-name wrapper)))
      {:name (get-in set-operation [:target :name])
       :index index
       :set-form set-form
       :wrapper wrapper})))

(defn direct-old-defmulti-initialization-units [forms]
  (keep-indexed
    (fn [index form]
      (old-defmulti-initialization-unit form index))
    (direct-source-items forms)))

(defn direct-new-defmulti-initialization-units [forms]
  (let [items (vec (direct-source-items forms))]
    (keep-indexed
      (fn [index _]
        (new-defmulti-initialization-unit items index))
      items)))

(defn defmulti-unit-counts [units]
  (frequencies (map :name units)))

(defn defmulti-unit-order-valid?
  [old-units new-units old-counts new-counts]
  (let [unique-old
        (set (for [[identity count] old-counts :when (= 1 count)]
               identity))
        unique-new
        (set (for [[identity count] new-counts :when (= 1 count)]
               identity))
        common (set (filter unique-new unique-old))]
    (= (filterv common (map :name old-units))
       (filterv common (map :name new-units)))))

(defn exact-defmulti-initialization-proof [old-unit new-unit state]
  (let [source-namespace (:source-namespace state)
        old-form (:form old-unit)
        set-form (:set-form new-unit)
        wrapper (:wrapper new-unit)
        old-name (when (and (seq? old-form) (> (count old-form) 1))
                   (second old-form))
        identity (when (symbol? old-name) (symbol (name old-name)))
        dispatch (when (= 3 (count old-form)) (nth old-form 2))
        old-count (get (:old-defmulti-unit-counts state) identity 0)
        new-count (get (:new-defmulti-unit-counts state) identity 0)
        expected-target
        (when (and source-namespace identity)
          (list 'clojure.lang.RT/var
                (str source-namespace) (str identity)))
        expected-var-reference (when identity (list 'var identity))
        outer-set (low-level-var-operation '.setMeta set-form)
        outer-target-source (get-in outer-set [:target :source])
        outer-metadata (:payload outer-set)
        bindings (when (and (seq? wrapper) (> (count wrapper) 1))
                   (second wrapper))
        local (when (vector? bindings) (nth bindings 0 nil))
        tagged-local (when (symbol? local)
                       (with-meta local {:tag 'clojure.lang.Var}))
        binding-value (when (vector? bindings) (nth bindings 1 nil))
        body (when (and (seq? wrapper) (> (count wrapper) 2))
               (nth wrapper 2))
        predicate (when (and (seq? body) (> (count body) 1))
                    (nth body 1))
        inner-set-form (when (and (seq? body) (> (count body) 2))
                         (nth body 2))
        inner-bind-form (when (and (seq? body) (> (count body) 3))
                          (nth body 3))
        final-value (when (and (seq? body) (> (count body) 4))
                      (nth body 4))
        has-root (when (and (seq? predicate) (> (count predicate) 1))
                   (nth predicate 1))
        instance-check
        (when (and (seq? predicate) (> (count predicate) 2))
          (nth predicate 2))
        deref-local
        (when (and (seq? instance-check) (> (count instance-check) 2))
          (nth instance-check 2))
        inner-bind (low-level-var-operation '.bindRoot inner-bind-form)
        constructor (:payload inner-bind)
        violations
        (cond-> []
          (not (exact-list-head-count? 'defmulti 3 old-form))
          (conj :old-not-exact-three-item-defmulti)

          (not (exact-simple-unannotated-symbol? old-name))
          (conj :old-name-not-simple-unannotated-symbol)

          (some? (encode-metadata old-form))
          (conj :old-form-has-semantic-metadata)

          (not= 1 old-count)
          (conj :old-identity-not-unique)

          (not= 1 new-count)
          (conj :new-identity-not-unique)

          (not (:defmulti-unit-order-valid? state))
          (conj :relative-order-changed)

          (nil? source-namespace)
          (conj :source-namespace-not-exact)

          (and new-unit (not= identity (:name new-unit)))
          (conj :new-unit-identity-changed)

          (nil? outer-set)
          (conj :new-prefix-not-setmeta)

          (and expected-target
               (not (encoded= expected-target outer-target-source)))
          (conj :outer-target-not-exact-source-var)

          (not (encoded= {:column '(int 1)} outer-metadata))
          (conj :outer-metadata-not-exact-column)

          (not (exact-list-head-count? 'let 3 wrapper))
          (conj :wrapper-not-exact-single-body-let)

          (not (and (vector? bindings) (= 2 (count bindings))))
          (conj :binding-vector-not-exact-pair)

          (not (exact-simple-unannotated-symbol? local))
          (conj :compiler-local-not-simple-unannotated-symbol)

          (and expected-var-reference
               (not (encoded= expected-var-reference binding-value)))
          (conj :binding-var-reference-not-exact)

          (not (exact-list-head-count? 'when-not 5 body))
          (conj :guard-body-cardinality-mismatch)

          (not (exact-list-head-count? 'and 3 predicate))
          (conj :guard-predicate-not-exact-and)

          (not (and (exact-list-head-count? '.hasRoot 2 has-root)
                    (encoded= tagged-local (second has-root))))
          (conj :has-root-check-not-exact-tagged-var-local)

          (not (and (exact-list-head-count? 'instance? 3 instance-check)
                    (encoded= 'clojure.lang.MultiFn
                              (second instance-check))))
          (conj :instance-check-not-exact-multifn)

          (not (and (exact-list-head-count? 'deref 2 deref-local)
                    (encoded= local (second deref-local))))
          (conj :deref-check-not-exact-local)

          (not (encoded= set-form inner-set-form))
          (conj :inner-setmeta-not-identical-to-prefix)

          (nil? inner-bind)
          (conj :inner-bindroot-not-exact-operation)

          (and expected-target
               (not (encoded= expected-target
                              (get-in inner-bind [:target :source]))))
          (conj :bind-target-not-exact-source-var)

          (not (exact-list-head-count?
                 'clojure.lang.MultiFn. 5 constructor))
          (conj :constructor-shape-not-exact)

          (and identity (not= (str identity) (nth constructor 1 nil)))
          (conj :constructor-name-not-exact)

          (and dispatch (not (encoded= dispatch (nth constructor 2 nil))))
          (conj :dispatch-form-changed)

          (not (encoded= :default (nth constructor 3 nil)))
          (conj :default-dispatch-not-exact)

          (not (encoded= '(var clojure.core/global-hierarchy)
                         (nth constructor 4 nil)))
          (conj :hierarchy-not-exact-global-var)

          (and expected-var-reference
               (not (encoded= expected-var-reference final-value)))
          (conj :final-var-reference-not-exact))]
    {:valid? (empty? violations)
     :violations violations
     :identity identity
     :source-namespace source-namespace
     :old-count old-count
     :new-count new-count
     :old-sha256 (encoded-sha256 old-form)
     :dispatch-sha256 (when dispatch (encoded-sha256 dispatch))
     :setmeta-sha256 (encoded-sha256 set-form)
     :wrapper-sha256 (encoded-sha256 wrapper)}))

(defn exact-defmulti-initialization-event-data [proof]
  {:identity (:identity proof)
   :source-namespace (:source-namespace proof)
   :old-identity-count (:old-count proof)
   :new-identity-count (:new-count proof)
   :old-sha256 (:old-sha256 proof)
   :dispatch-sha256 (:dispatch-sha256 proof)
   :setmeta-sha256 (:setmeta-sha256 proof)
   :wrapper-sha256 (:wrapper-sha256 proof)
   :valid (:valid? proof)
   :violations (:violations proof)})

(defn unique-defmulti-unit-map [units]
  (into {}
        (for [[identity matches] (group-by :name units)
              :when (= 1 (count matches))]
          [identity (first matches)])))

(defn exact-defmulti-initialization-candidates
  [old-units new-units state]
  (let [old-map (unique-defmulti-unit-map old-units)
        new-map (unique-defmulti-unit-map new-units)]
    (into (sorted-map)
          (for [[identity old-unit] old-map
                :let [new-unit (get new-map identity)
                      proof
                      (when new-unit
                        (exact-defmulti-initialization-proof
                          old-unit new-unit state))]
                :when (:valid? proof)]
            [(:index new-unit)
             {:identity identity
              :old-unit old-unit
              :new-unit new-unit
              :proof proof}]))))

(defn canonicalize-exact-initialization-units
  [new-forms protocol-candidates defmulti-candidates]
  (let [protocol-indexes (set (keys protocol-candidates))
        defmulti-indexes (set (keys defmulti-candidates))
        defmulti-continuations (set (map inc defmulti-indexes))
        overlap
        (seq (filter protocol-indexes
                     (concat defmulti-indexes defmulti-continuations)))]
    (when overlap
      (fail! "exact initialization candidate ranges overlap"
             {:indexes (vec (sort overlap))}))
    (rebuild-direct-source-items
      new-forms
      (mapcat
        (fn [index form]
          (cond
            (contains? protocol-candidates index)
            [(get-in protocol-candidates [index :old-unit :cache])
             (get-in protocol-candidates [index :old-unit :protocol])]

            (contains? defmulti-candidates index)
            [(get-in defmulti-candidates [index :old-unit :form])]

            (contains? defmulti-continuations index)
            []

            :else [form]))
        (range) (direct-source-items new-forms)))))

(defn add-exact-defmulti-initialization-events
  [state candidates]
  (reduce
    (fn [state [_ {:keys [old-unit new-unit proof]}]]
      (add-event
        state :exact-defmulti-initialization-unit
        [:forms
         [:old (:index old-unit)
          :new-unit [(:index new-unit) (inc (:index new-unit))]]]
        (exact-defmulti-initialization-event-data proof)))
    state candidates))

(defn form-anchor [form]
  (if-let [definition (definition-record form)]
    [:definition (str (:name definition))
     (encoded-sha256 (:clauses definition))]
    (if-let [definition (scalar-definition-record form)]
      [:scalar-definition (str (:name definition))
       (encoded-sha256 form)]
      (when-let [reset (reset-record form)]
        [:reset (reset-identity reset)]))))

(defn exact-var-unit-anchor?
  [old-items old-index new-items new-index state]
  (let [definition
        (canonical-var-definition (nth old-items old-index nil))
        unit (var-initialization-unit new-items new-index)
        old-next-reset
        (reset-record (nth old-items (inc old-index) nil))
        old-paired-reset
        (when (target-matches-definition? old-next-reset definition)
          old-next-reset)
        expected-source-namespaces
        (when definition
          (get (:old-definition-namespaces state)
               (canonical-definition-identity definition)))]
    (and definition unit (= (:name definition) (:name unit))
         (:valid?
           (exact-var-initialization-proof
             definition unit old-paired-reset expected-source-namespaces)))))

(defn nearby-anchor [old-items old-index new-items new-index state]
  (let [old-limit (min (count old-items) (+ old-index 9))
        new-limit (min (count new-items) (+ new-index 9))
        candidates
        (for [oi (range old-index old-limit)
              ni (range new-index new-limit)
              :let [old-key (form-anchor (nth old-items oi))
                    new-key (form-anchor (nth new-items ni))]
              :when (and (or (and old-key (= old-key new-key))
                             (exact-var-unit-anchor?
                               old-items oi new-items ni state))
                         (not (and (= oi old-index) (= ni new-index))))]
          {:old-index oi :new-index ni
           :distance (+ (- oi old-index) (- ni new-index))})]
    (first (sort-by (juxt :distance :old-index :new-index) candidates))))

(declare compare-node compare-items)

(defn compare-declare-setmeta-unit
  [old-items old-index new-items new-index path state]
  (let [old-form (nth old-items old-index)
        new-form (nth new-items new-index)
        proof (exact-declare-setmeta-proof old-form new-form state)
        [target-namespace target-name] (:identity proof)
        event-path (conj path [:old old-index :new new-index])
        event-data
        {:target [target-namespace (str target-name)]
         :old-qualified (boolean
                          (get-in proof [:declaration :qualified?]))
         :old-identity-count (:old-count proof)
         :new-identity-count (:new-count proof)
         :metadata-sha256 (:metadata-sha256 proof)
         :valid (:valid? proof)
         :violations (:violations proof)}
        state (add-event state :exact-declare-setmeta-unit
                         event-path event-data)]
    (when-not (:valid? proof)
      (fail! "internal invalid exact declaration unit" event-data))
    {:state state :old-consumed 1 :new-consumed 1}))

(defn unmatched-new-reset [state reset path]
  (let [identity (reset-identity reset)
        kind (cond
               (contains? (:paired-resets state) identity) :duplicate-reset
               (contains? (:seen-definitions state) (:name reset)) :moved-reset
               :else :extra-reset)]
    (add-issue state kind path
               {:target identity
                :payload-sha256
                (encoded-sha256 (:metadata-expression reset))})))

(defn compare-var-initialization-unit
  [old-items old-index new-items new-index path state]
  (let [old-form (nth old-items old-index)
        definition (canonical-var-definition old-form)
        old-next-reset (reset-record (nth old-items (inc old-index) nil))
        old-paired-reset
        (when (target-matches-definition? old-next-reset definition)
          old-next-reset)
        unit (var-initialization-unit new-items new-index)
        expected-source-namespaces
        (get (:old-definition-namespaces state)
             (canonical-definition-identity definition))
        proof (exact-var-initialization-proof
                definition unit old-paired-reset
                expected-source-namespaces)
        event-path (conj path [:old old-index
                               :new-unit [new-index (inc new-index)]])
        metadata-proof (:metadata-proof proof)
        state
        (-> state
            (update :seen-definitions conj (:name definition))
            (add-event
              (if (:valid? proof)
                :exact-var-initialization-unit
                :var-initialization-unit-delta)
              event-path
              {:name (str (:name definition))
               :definition-kind (:kind definition)
               :source-kind (:source-kind definition)
               :target (:target proof)
               :root-relation (:root-relation proof)
               :root-match (:root-match? proof)
               :old-reset (boolean old-paired-reset)
               :metadata-relation (:relation metadata-proof)
               :metadata-old-sha256 (:old-metadata-sha256 metadata-proof)
               :metadata-new-sha256 (:new-metadata-sha256 metadata-proof)
               :old-root-sha256 (:old-root-sha256 proof)
               :new-root-sha256 (:new-root-sha256 proof)
               :valid (:valid? proof)
               :violations (:violations proof)}))
        state
        (if (:valid? proof)
          state
          (add-issue state :invalid-var-initialization-unit event-path
                     {:name (str (:name definition))
                      :violations (:violations proof)
                      :old-root-sha256 (:old-root-sha256 proof)
                      :new-root-sha256 (:new-root-sha256 proof)
                      :metadata-old-sha256
                      (:old-metadata-sha256 metadata-proof)
                      :metadata-new-sha256
                      (:new-metadata-sha256 metadata-proof)}))]
    {:state state
     :old-consumed (if old-paired-reset 2 1)
     :new-consumed 2}))

(defn exact-added-scalar-reset-proof [definition reset]
  (let [payload-proof
        (exact-reset-payload-proof (:name definition) reset)
        metadata-exact?
        (encoded= exact-added-column-metadata (:metadata reset))
        violations
        (cond-> []
          (not (:valid? payload-proof))
          (into (:violations payload-proof))

          (not metadata-exact?)
          (conj :metadata-not-exact-added-column-only))]
    {:valid? (empty? violations)
     :violations violations
     :payload-sha256 (:payload-sha256 payload-proof)
     :metadata-sha256 (encoded-sha256 (:metadata reset))}))

(defn compare-scalar-reset-addition
  [old-items old-index new-items new-index path state]
  (let [old-form (nth old-items old-index)
        new-form (nth new-items new-index)
        definition (scalar-definition-record old-form)
        reset (reset-record (nth new-items (inc new-index) nil))
        unchanged? (encoded= old-form new-form)
        proof (exact-added-scalar-reset-proof definition reset)
        event-path (conj path [:old old-index
                               :new-unit [new-index (inc new-index)]])
        valid? (and unchanged? (:valid? proof))
        state
        (-> state
            (update :seen-definitions conj (:name definition))
            (add-event
              (if valid?
                :scalar-def-plus-exact-reset
                :scalar-def-reset-addition-delta)
              event-path
              {:name (str (:name definition))
               :definition-unchanged unchanged?
               :reset-payload-sha256 (:payload-sha256 proof)
               :metadata-sha256 (:metadata-sha256 proof)
               :valid valid?
               :violations
               (cond-> (:violations proof)
                 (not unchanged?) (conj :definition-changed))}))
        state
        (if valid?
          state
          (add-issue state :invalid-scalar-reset-addition event-path
                     {:name (str (:name definition))
                      :definition-unchanged unchanged?
                      :violations
                      (cond-> (:violations proof)
                        (not unchanged?) (conj :definition-changed))
                      :reset-payload-sha256 (:payload-sha256 proof)}))
        state
        (if (and reset (= (:name definition) (:name reset)))
          (update state :paired-resets conj (reset-identity reset))
          state)]
    {:state state :old-consumed 1 :new-consumed 2}))

(defn compare-definition-pair
  [old-items old-index new-items new-index path state]
  (let [old-form (nth old-items old-index)
        new-form (nth new-items new-index)
        old-definition (definition-record old-form)
        new-definition (definition-record new-form)
        old-next (nth old-items (inc old-index) nil)
        new-next (nth new-items (inc new-index) nil)
        old-next-reset (reset-record old-next)
        new-next-reset (reset-record new-next)
        old-paired? (target-matches-definition? old-next-reset old-definition)
        new-paired? (target-matches-definition? new-next-reset new-definition)
        wrong-new-reset? (and new-next-reset (not new-paired?))
        clauses-match? (encoded= (:clauses old-definition)
                                 (:clauses new-definition))
        proof (when new-paired?
                (exact-function-reset-proof new-definition new-next-reset))
        conversion? (and (= :defn (:kind old-definition))
                         (= :def+fn (:kind new-definition)))
        unchanged-definition? (encoded= old-form new-form)
        reset-column-cast-proof
        (when (and old-paired? new-paired?)
          (exact-reset-column-cast-proof old-next new-next state))
        exact-reset-column-cast? (:valid? reset-column-cast-proof)
        unchanged-reset? (and old-paired? new-paired?
                              (or (encoded= old-next new-next)
                                  exact-reset-column-cast?))
        allowed-conversion? (and conversion? clauses-match? new-paired?
                                 (:valid? proof)
                                 (or (not old-paired?) unchanged-reset?))
        allowed-reset-addition? (and (= :defn (:kind old-definition))
                                     (= :defn (:kind new-definition))
                                     unchanged-definition?
                                     (not old-paired?)
                                     new-paired?
                                     (:valid? proof))
        event-path (conj path [:old old-index :new new-index])
        state (-> state
                  (update :seen-definitions conj (:name new-definition))
                  (add-event
                    (cond
                      allowed-conversion? :defn-to-def-fn-reset
                      allowed-reset-addition? :defn-plus-exact-reset
                      (and unchanged-definition?
                           (or (and (not old-paired?) (not new-paired?))
                               unchanged-reset?)) :unchanged-definition
                      :else :definition-delta)
                    event-path
                    {:name (str (:name new-definition))
                     :old-kind (:kind old-definition)
                     :new-kind (:kind new-definition)
                     :clauses-match clauses-match?
                     :old-reset old-paired?
                     :new-reset new-paired?
                     :approval-reason
                     (cond
                       allowed-conversion? :exact-defn-to-def-fn-reset
                       allowed-reset-addition? :unchanged-defn-exact-reset
                       :else nil)
                     :reset-proof (when proof
                                    (select-keys proof
                                      [:valid? :violations :conversion-reason
                                       :payload-sha256]))}))
        state
        (if exact-reset-column-cast?
          (add-event state :exact-reset-column-cast-unit
                     (conj path [:old (inc old-index)
                                 :new (inc new-index)])
                     (exact-reset-column-cast-event-data
                       reset-column-cast-proof))
          state)
        state (if clauses-match? state
                  (add-issue state :function-clauses-changed event-path
                             {:name (str (:name new-definition))}))
        state (if (or allowed-conversion? allowed-reset-addition?
                      unchanged-definition?) state
                  (add-issue state :definition-form-changed event-path
                             {:name (str (:name new-definition))
                              :old-sha256 (encoded-sha256 old-form)
                              :new-sha256 (encoded-sha256 new-form)}))
        state (if (and conversion? (not new-paired?))
                (add-issue state :missing-conversion-reset event-path
                           {:name (str (:name new-definition))})
                state)
        state (if (and new-paired? (not (:valid? proof)))
                (add-issue state :invalid-reset-payload event-path
                           {:name (str (:name new-definition))
                            :violations (:violations proof)
                            :payload-sha256 (:payload-sha256 proof)})
                state)
        state (if (and new-paired? (not conversion?)
                       (not allowed-reset-addition?)
                       (not (and unchanged-definition? unchanged-reset?)))
                (add-issue state :unapproved-definition-reset-transition
                           event-path
                           {:name (str (:name new-definition))
                            :old-kind (:kind old-definition)
                            :new-kind (:kind new-definition)})
                state)
        state (if (and old-paired? new-paired? (not unchanged-reset?))
                (add-issue state :existing-reset-changed event-path
                           {:name (str (:name new-definition))
                            :old-sha256 (encoded-sha256 old-next)
                            :new-sha256 (encoded-sha256 new-next)})
                state)
        state (if (and old-paired? (not new-paired?))
                (add-issue state :historical-reset-removed event-path
                           {:name (str (:name old-definition))})
                state)
        state (if wrong-new-reset?
                (add-issue state :wrong-reset-target event-path
                           {:definition (str (:name new-definition))
                            :target (reset-identity new-next-reset)})
                state)
        state (if new-paired?
                (update state :paired-resets conj
                        (reset-identity new-next-reset))
                state)]
    {:state state
     :old-consumed (if old-paired? 2 1)
     :new-consumed (if (or new-paired? wrong-new-reset?) 2 1)}))

(defn compare-items [old-values new-values path state]
  (let [old-items (vec old-values)
        new-items (vec new-values)]
    (loop [old-index 0 new-index 0 state state]
      (cond
        (and (= old-index (count old-items))
             (= new-index (count new-items)))
        state

        (= old-index (count old-items))
        (let [form (nth new-items new-index)
              reset (reset-record form)
              item-path (conj path [:new new-index])
              state (if reset
                      (unmatched-new-reset state reset item-path)
                      (add-issue state :extra-form item-path
                                 {:sha256 (encoded-sha256 form)
                                  :head (head-name form)}))]
          (recur old-index (inc new-index) state))

        (= new-index (count new-items))
        (let [form (nth old-items old-index)
              reset (reset-record form)
              item-path (conj path [:old old-index])
              state (add-issue state
                               (if reset :historical-reset-removed :form-removed)
                               item-path
                               {:sha256 (encoded-sha256 form)
                                :target (when reset (reset-identity reset))})]
          (recur (inc old-index) new-index state))

        :else
        (let [old-form (nth old-items old-index)
              new-form (nth new-items new-index)
              old-declaration
              (direct-declare-record (:source-namespace state) old-form)
              new-declared-setmeta
              (declared-setmeta-record new-form)
              declare-setmeta-proof
              (when (and old-declaration new-declared-setmeta
                         (= (:name old-declaration)
                            (:name new-declared-setmeta)))
                (exact-declare-setmeta-proof old-form new-form state))
              old-canonical-definition
              (canonical-var-definition old-form)
              new-var-unit
              (var-initialization-unit new-items new-index)
              old-scalar-definition
              (scalar-definition-record old-form)
              new-scalar-definition
              (scalar-definition-record new-form)
              old-next-reset
              (reset-record (nth old-items (inc old-index) nil))
              new-next-reset
              (reset-record (nth new-items (inc new-index) nil))
              old-definition (definition-record old-form)
              new-definition (definition-record new-form)
              old-reset (reset-record old-form)
              new-reset (reset-record new-form)
              reset-column-cast-proof
              (when (and old-reset new-reset)
                (exact-reset-column-cast-proof old-form new-form state))
              old-protocol-unit
              (old-protocol-initialization-unit old-items old-index)
              new-protocol-unit
              (new-protocol-initialization-unit new-form)
              protocol-unit-proof
              (when (and old-protocol-unit new-protocol-unit
                         (= (:name old-protocol-unit)
                            (:name new-protocol-unit)))
                (exact-protocol-initialization-proof
                  old-protocol-unit new-protocol-unit state))
              item-path (conj path [:old old-index :new new-index])]
          (cond
            (:valid? protocol-unit-proof)
            (recur (+ old-index 2) (inc new-index)
                   (add-event
                     state :exact-protocol-initialization-unit
                     (conj path [:old-unit [old-index (inc old-index)]
                                 :new new-index])
                     (exact-protocol-initialization-event-data
                       protocol-unit-proof)))

            (:valid? declare-setmeta-proof)
            (let [{:keys [state old-consumed new-consumed]}
                  (compare-declare-setmeta-unit
                    old-items old-index new-items new-index path state)]
              (recur (+ old-index old-consumed)
                     (+ new-index new-consumed) state))

            (and old-canonical-definition new-var-unit
                 (= (:name old-canonical-definition)
                    (:name new-var-unit)))
            (let [{:keys [state old-consumed new-consumed]}
                  (compare-var-initialization-unit
                    old-items old-index new-items new-index path state)]
              (recur (+ old-index old-consumed)
                     (+ new-index new-consumed) state))

            (and old-scalar-definition new-scalar-definition
                 (= (:name old-scalar-definition)
                    (:name new-scalar-definition))
                 (nil? old-next-reset)
                 new-next-reset
                 (= (:name old-scalar-definition)
                    (:name new-next-reset)))
            (let [{:keys [state old-consumed new-consumed]}
                  (compare-scalar-reset-addition
                    old-items old-index new-items new-index path state)]
              (recur (+ old-index old-consumed)
                     (+ new-index new-consumed) state))

            (and old-definition new-definition
                 (= (:name old-definition) (:name new-definition)))
            (let [{:keys [state old-consumed new-consumed]}
                  (compare-definition-pair old-items old-index
                                           new-items new-index path state)]
              (recur (+ old-index old-consumed)
                     (+ new-index new-consumed) state))

            (:valid? reset-column-cast-proof)
            (recur (inc old-index) (inc new-index)
                   (add-event state :exact-reset-column-cast-unit item-path
                              (exact-reset-column-cast-event-data
                                reset-column-cast-proof)))

            (and old-reset new-reset)
            (let [state (if (encoded= old-form new-form)
                          state
                          (add-issue state :reset-changed item-path
                                     {:old-target (reset-identity old-reset)
                                      :new-target (reset-identity new-reset)
                                      :old-sha256 (encoded-sha256 old-form)
                                      :new-sha256 (encoded-sha256 new-form)}))]
              (recur (inc old-index) (inc new-index) state))

            new-reset
            (recur old-index (inc new-index)
                   (unmatched-new-reset state new-reset item-path))

            old-reset
            (recur (inc old-index) new-index
                   (add-issue state :historical-reset-removed item-path
                              {:target (reset-identity old-reset)}))

            (encoded= old-form new-form)
            (recur (inc old-index) (inc new-index) state)

            :else
            (if-let [{anchor-old :old-index anchor-new :new-index}
                     (nearby-anchor old-items old-index
                                    new-items new-index state)]
              (let [state
                    (reduce
                      (fn [state index]
                        (add-issue state :form-removed
                                   (conj path [:old index])
                                   {:sha256 (encoded-sha256
                                              (nth old-items index))
                                    :head (head-name (nth old-items index))}))
                      state (range old-index anchor-old))
                    state
                    (reduce
                      (fn [state index]
                        (let [form (nth new-items index)
                              reset (reset-record form)]
                          (if reset
                            (unmatched-new-reset state reset
                                                 (conj path [:new index]))
                            (add-issue state :extra-form
                                       (conj path [:new index])
                                       {:sha256 (encoded-sha256 form)
                                        :head (head-name form)}))))
                      state (range new-index anchor-new))]
                (recur anchor-old anchor-new state))
              (recur (inc old-index) (inc new-index)
                     (compare-node old-form new-form item-path state)))))))))

(defn compare-node [old-form new-form path state]
  (let [state (if (= (encode-metadata old-form)
                     (encode-metadata new-form))
                state
                (add-issue state :semantic-metadata-changed path
                           {:old (encode-metadata old-form)
                            :new (encode-metadata new-form)}))]
    (cond
      (and (= "quote" (head-name old-form))
           (= "quote" (head-name new-form)))
      (if (encoded= old-form new-form) state
          (add-issue state :quoted-form-changed path
                     {:old-sha256 (encoded-sha256 old-form)
                      :new-sha256 (encoded-sha256 new-form)}))

      (and (list? old-form) (list? new-form))
      (compare-items old-form new-form (conj path :list) state)

      (and (vector? old-form) (vector? new-form))
      (compare-items old-form new-form (conj path :vector) state)

      (encoded= old-form new-form)
      state

      :else
      (add-issue state :ordinary-form-changed path
                 {:old-sha256 (encoded-sha256 old-form)
                  :new-sha256 (encoded-sha256 new-form)
                  :old-type (some-> old-form class .getName)
                  :new-type (some-> new-form class .getName)}))))

(defn compare-source-forms [old-forms new-forms]
  (let [old-source-namespace (exact-direct-source-namespace old-forms)
        new-source-namespace (exact-direct-source-namespace new-forms)
        source-namespace (when (= old-source-namespace new-source-namespace)
                           old-source-namespace)
        old-declare-counts
        (if source-namespace
          (direct-record-counts
            old-forms #(direct-declare-record source-namespace %))
          {})
        new-setmeta-counts
        (if source-namespace
          (direct-record-counts new-forms declared-setmeta-record)
          {})
        old-protocol-units
        (vec (direct-old-protocol-initialization-units old-forms))
        new-protocol-units
        (vec (direct-new-protocol-initialization-units new-forms))
        old-protocol-counts (protocol-unit-counts old-protocol-units)
        new-protocol-counts (protocol-unit-counts new-protocol-units)
        old-defmulti-units
        (vec (direct-old-defmulti-initialization-units old-forms))
        new-defmulti-units
        (vec (direct-new-defmulti-initialization-units new-forms))
        old-defmulti-counts (defmulti-unit-counts old-defmulti-units)
        new-defmulti-counts (defmulti-unit-counts new-defmulti-units)
        state
        (assoc (relation-state
                 (direct-definition-namespace-map old-forms))
               :source-namespace source-namespace
               :old-declare-counts old-declare-counts
               :new-setmeta-counts new-setmeta-counts
               :old-direct-reset-counts
               (direct-reset-identity-counts old-forms)
               :new-direct-reset-counts
               (direct-reset-identity-counts new-forms)
               :old-protocol-unit-counts old-protocol-counts
               :new-protocol-unit-counts new-protocol-counts
               :protocol-unit-order-valid?
               (protocol-unit-order-valid?
                 old-protocol-units new-protocol-units
                 old-protocol-counts new-protocol-counts)
               :old-defmulti-unit-counts old-defmulti-counts
               :new-defmulti-unit-counts new-defmulti-counts
               :defmulti-unit-order-valid?
               (defmulti-unit-order-valid?
                 old-defmulti-units new-defmulti-units
                 old-defmulti-counts new-defmulti-counts))
        protocol-candidates
        (exact-protocol-initialization-candidates
          old-protocol-units new-protocol-units state)
        defmulti-candidates
        (exact-defmulti-initialization-candidates
          old-defmulti-units new-defmulti-units state)
        canonical-new-forms
        (canonicalize-exact-initialization-units
          new-forms protocol-candidates defmulti-candidates)
        state
        (add-exact-protocol-initialization-events
          state protocol-candidates)
        state
        (add-exact-defmulti-initialization-events
          state defmulti-candidates)]
    (compare-items old-forms canonical-new-forms [:forms] state)))

(defn write-edn-lines! [file values]
  (with-open [writer (io/writer file)]
    (doseq [value values]
      (.write writer (str (pr-str value) "\n")))))

(defn validate-source-delta! [old-root new-root output-root]
  (let [old-root (.getCanonicalPath (io/file old-root))
        new-root (.getCanonicalPath (io/file new-root))
        output-file (io/file output-root)]
    (when (and (.exists output-file) (seq (.listFiles output-file)))
      (fail! "refusing non-empty output root" {:output output-root}))
    (.mkdirs output-file)
    (let [old-paths (relative-source-paths old-root)
          new-paths (relative-source-paths new-root)]
      (when-not (= old-paths new-paths)
        (fail! "source path sets differ"
               {:old (count old-paths) :new (count new-paths)}))
      (let [file-relations
            (mapv
              (fn [path]
                (let [old-file (io/file old-root path)
                      new-file (io/file new-root path)
                      text-match? (= (slurp old-file) (slurp new-file))
                      old-forms (read-forms old-file)
                      new-forms (if text-match?
                                  old-forms
                                  (read-forms new-file))
                      relation (if text-match?
                                 (relation-state)
                                 (compare-source-forms old-forms new-forms))
                      old-definitions (vec (mapcat collect-definitions old-forms))
                      new-definitions (vec (mapcat collect-definitions new-forms))
                      old-resets (vec (mapcat collect-resets old-forms))
                      new-resets (vec (mapcat collect-resets new-forms))]
                  {:path path
                   :text-relation (if text-match? :match :differ)
                   :old-definitions (count old-definitions)
                   :new-definitions (count new-definitions)
                   :old-resets (count old-resets)
                   :new-resets (count new-resets)
                   :events (mapv #(assoc % :file path) (:events relation))
                   :issues (mapv #(assoc % :file path) (:issues relation))}))
              old-paths)
            events (vec (mapcat :events file-relations))
            issues (vec (mapcat :issues file-relations))
            issue-counts (frequencies (map :kind issues))
            event-counts (frequencies (map :kind events))]
        (write-edn-lines! (io/file output-file "ordered-events.edn") events)
        (write-edn-lines! (io/file output-file "issues.edn") issues)
        (with-open [writer (io/writer (io/file output-file "file-relation.tsv"))]
          (.write writer
                  "path\ttext_relation\told_definitions\tnew_definitions\told_resets\tnew_resets\tevents\tissues\tstatus\n")
          (doseq [{:keys [path text-relation old-definitions new-definitions
                          old-resets new-resets events issues]} file-relations]
            (.write writer
                    (str path "\t" (name text-relation)
                         "\t" old-definitions "\t" new-definitions
                         "\t" old-resets "\t" new-resets
                         "\t" (count events) "\t" (count issues)
                         "\t" (if (empty? issues) "PASS" "FAIL") "\n"))))
        (with-open [writer (io/writer (io/file output-file "summary.tsv"))]
          (.write writer "metric\tvalue\n")
          (doseq [[metric value]
                  [["status" (if (empty? issues) "PASS" "FAIL")]
                   ["source.files" (count file-relations)]
                   ["source.files.text.match"
                    (count (filter #(= :match (:text-relation %))
                                   file-relations))]
                   ["source.files.text.differ"
                    (count (filter #(= :differ (:text-relation %))
                                   file-relations))]
                   ["ordered.events" (count events)]
                   ["issues" (count issues)]]]
            (.write writer (str metric "\t" value "\n")))
          (doseq [[kind count] (sort issue-counts)]
            (.write writer (str "issue." (name kind) "\t" count "\n")))
          (doseq [[kind count] (sort event-counts)]
            (.write writer (str "event." (name kind) "\t" count "\n"))))
        (when (seq issues)
          (fail! "metadata-aware source delta has unapproved changes"
                 {:issues (count issues)
                  :kinds issue-counts
                  :output output-root}))))
  (println "metadata-aware source delta validation passed:" output-root)))

(defn assert-fails! [thunk message-fragment]
  (let [failure (try (thunk) nil (catch Throwable failure failure))]
    (assert failure "negative fixture unexpectedly passed")
    (assert (str/includes? (or (ex-message failure) "") message-fragment)
            (str "unexpected negative-fixture failure: "
                 (ex-message failure)))
    failure))

(defn run-self-test! []
  (let [fixture-ns (symbol (str "datomic.source-delta.fixture." (gensym)))
        ns-form
        (read-string
          (str "(ns " fixture-ns
               " (:require [clojure.tools.analyzer"
               " [ast :refer [walk] :as ast]"
               " [utils :refer :all :as u]]"
               " [clojure.core.async :refer (put!) :as a]))"))
        aliases (ns-form-aliases ns-form)]
    (assert (= {'ast 'clojure.tools.analyzer.ast
                'u 'clojure.tools.analyzer.utils
                'a 'clojure.core.async}
               aliases))
    (assert-fails!
      #(ns-form-aliases
         (read-string
           (str "(ns " fixture-ns
                " (:require [one.alpha :as duplicate]"
                " [two.beta :as duplicate]))")))
      "duplicate namespace alias")
    (assert-fails!
      #(ns-form-aliases
         (read-string
           (str "(ns " fixture-ns " (:require [one.alpha :as]))")))
      "lacks a value")
    (let [context
          (low-level-source-context
            (read-string
              "(do (clojure.core/in-ns 'fixture.low-level) (clojure.core/require ['foo.bar :refer '(x) :as 'fb]))"))]
      (assert (= {:name 'fixture.low-level :aliases {'fb 'foo.bar}}
                 context))))
  (let [directory-path
        (java.nio.file.Files/createTempDirectory
          "datomic-source-reader-fixture-"
          (make-array java.nio.file.attribute.FileAttribute 0))
        directory (.toFile directory-path)
        namespace-name (symbol (str "datomic.source-reader.fixture." (gensym)))
        source-file (io/file directory "fixture.clj")]
    (try
      (spit source-file
            (str "(ns " namespace-name
                 " (:require [alpha.beta :as ab]"
                 " [prefix [one :refer [x] :as one]"
                 " [two :refer :all :as two]]))\n"
                 "[::current ::ab/value ::one/x ::two/y]\n"))
      (let [namespaces-before (set (map ns-name (all-ns)))
            forms (read-forms source-file)
            namespaces-after (set (map ns-name (all-ns)))]
        (assert (= namespaces-before namespaces-after))
        (assert (= [(keyword (str namespace-name) "current")
                    :alpha.beta/value
                    :prefix.one/x
                    :prefix.two/y]
                   (second forms))))
      (finally
        (java.nio.file.Files/delete (.toPath source-file))
        (java.nio.file.Files/delete directory-path))))
  (let [approved-file (io/file "transactor/src-clj/ring/util/codec.clj")]
    (assert (= approved-legacy-read-eval-file-sha256
               (file-sha256 approved-file)))
    (let [safe-source (reader-safe-source approved-file)]
      (assert (str/includes? safe-source
                             approved-legacy-read-eval-replacement))
      (assert (empty? (active-read-eval-offsets safe-source)))))
  (let [root-path
        (java.nio.file.Files/createTempDirectory
          "datomic-source-reader-legacy-negative-"
          (make-array java.nio.file.attribute.FileAttribute 0))
        ring-directory (io/file (.toFile root-path) "ring" "util")
        codec-file (io/file ring-directory "codec.clj")]
    (try
      (.mkdirs ring-directory)
      (spit codec-file (str "(def x " approved-legacy-read-eval-source ")\n"))
      (assert-fails! #(reader-safe-source codec-file)
                     "unapproved active reader-eval form")
      (spit codec-file
            (str "(def x " approved-legacy-read-eval-source ")\n"
                 "(def y " approved-legacy-read-eval-source ")\n"))
      (assert-fails! #(reader-safe-source codec-file)
                     "unapproved active reader-eval form")
      (assert (empty? (active-read-eval-offsets
                        (str "\"" approved-legacy-read-eval-source "\""))))
      (finally
        (java.nio.file.Files/delete (.toPath codec-file))
        (java.nio.file.Files/delete (.toPath ring-directory))
        (java.nio.file.Files/delete
          (.toPath (.getParentFile ring-directory)))
        (java.nio.file.Files/delete root-path))))
  (let [old-definition
        (read-string "(defn f ([x] x))")
        changed-definition
        (read-string "(defn f ([x] (inc x)))")
        new-definition
        (read-string "(def f (fn f ([x] x)))")
        reset-f
        (read-string
          "(reset-meta! #'f (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'f :ns *ns*))")
        reset-g
        (read-string
          "(reset-meta! #'g (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'g :ns *ns*))")
        changed-reset-f
        (read-string
          "(reset-meta! #'f (assoc {:arglists (clojure.core/list ['x]), :column (int 2)} :name 'f :ns *ns*))")
        bad-reset-f
        (read-string
          "(reset-meta! #'f (assoc {:column (int 1)} :name 'wrong :ns *ns*))")
        issue-kinds
        (fn [new-forms]
          (set (map :kind
                    (:issues (compare-source-forms
                               [old-definition] new-forms)))))]
    (assert (empty? (:issues
                      (compare-source-forms
                        [old-definition] [new-definition reset-f]))))
    (assert (= [:defn-to-def-fn-reset]
               (mapv :kind
                     (:events
                       (compare-source-forms
                         [old-definition] [new-definition reset-f])))))
    (assert (empty? (:issues
                      (compare-source-forms
                        [old-definition] [old-definition reset-f]))))
    (assert (= [:defn-plus-exact-reset]
               (mapv :kind
                     (:events
                       (compare-source-forms
                         [old-definition] [old-definition reset-f])))))
    (assert (contains? (issue-kinds [reset-f old-definition])
                       :extra-reset))
    (assert (contains? (issue-kinds
                         [old-definition '(def marker true) reset-f])
                       :moved-reset))
    (assert (contains? (issue-kinds [old-definition reset-g])
                       :wrong-reset-target))
    (assert (contains? (issue-kinds [old-definition bad-reset-f])
                       :invalid-reset-payload))
    (assert (contains?
              (set (map :kind
                        (:issues
                          (compare-source-forms
                            [old-definition reset-f]
                            [old-definition changed-reset-f]))))
              :existing-reset-changed))
    (assert (contains?
              (set (map :kind
                        (:issues
                          (compare-source-forms
                            [old-definition]
                            [changed-definition reset-f]))))
              :function-clauses-changed))
    (assert (contains? (issue-kinds [new-definition reset-f reset-f])
                       :duplicate-reset))
    (assert (contains? (issue-kinds [new-definition '(def marker true) reset-f])
                       :moved-reset))
    (assert (contains? (issue-kinds [new-definition reset-g])
                       :wrong-reset-target))
    (assert (contains? (issue-kinds [new-definition bad-reset-f])
                       :invalid-reset-payload))
    (assert (contains? (set (map :kind
                                 (:issues
                                   (compare-source-forms
                                     ['(def marker true)]
                                     ['(def marker true) reset-g]))))
                       :extra-reset))
    (assert (empty? (:issues
                      (compare-source-forms [reset-f] [reset-f]))))
    (assert (contains? (set (map :kind
                                 (:issues
                                   (compare-source-forms
                                     [reset-f] [changed-reset-f]))))
                       :reset-changed))
    (assert (contains? (set (map :kind
                                 (:issues
                                   (compare-source-forms
                                     [reset-f reset-g]
                                     [reset-g reset-f]))))
                       :reset-changed))
    (assert (encoded= #"same" #"same"))
    (assert (not (encoded= #"same" #"different")))
    (assert
      (not
        (encoded=
          (java.util.regex.Pattern/compile "same")
          (java.util.regex.Pattern/compile
            "same" java.util.regex.Pattern/CASE_INSENSITIVE))))
    (assert (contains? (set (map :kind
                                 (:issues
                                   (compare-source-forms
                                     [(with-meta 'x {:semantic :old})]
                                     [(with-meta 'x {:semantic :new})]))))
                       :semantic-metadata-changed))
    (assert (empty? (:issues
                      (compare-source-forms
                        [{:column 1 :domain true}]
                        [{:column 1 :domain true}])))))
  (let [namespace-name 'fixture.var-unit
        setup (list 'clojure.core/in-ns (list 'quote namespace-name))
        target (fn
                 ([name]
                  (list 'clojure.lang.RT/var (str namespace-name) name))
                 ([namespace name]
                  (list 'clojure.lang.RT/var namespace name)))
        set-meta (fn
                   ([name metadata]
                    (list '.setMeta (target name) metadata))
                   ([namespace name metadata]
                    (list '.setMeta (target namespace name) metadata)))
        bind-root (fn
                    ([name value]
                     (list '.bindRoot (target name) value))
                    ([namespace name value]
                     (list '.bindRoot (target namespace name) value)))
        reset (fn [target-name payload-name metadata]
                (list 'reset-meta! (list 'var target-name)
                      (list 'assoc metadata :name
                            (list 'quote payload-name) :ns '*ns*)))
        old-meta {:private true :column 1}
        new-meta {:private true :column (list 'int 1)}
        added-meta exact-added-column-metadata
        old-scalar (list 'def 'f 1)
        old-scalar-reset (reset 'f 'f old-meta)
        old-function '(defn f ([x] x))
        old-function-reset (reset 'f 'f {:column 1})
        new-set (set-meta "f" new-meta)
        new-function-set (set-meta "f" {:column (list 'int 1)})
        new-bind (bind-root "f" 1)
        new-function-bind (bind-root "f" '(fn f ([x] x)))
        marker '(def marker true)
        compare-do
        (fn [old-items new-items]
          (compare-source-forms
            [(apply list (concat ['do setup] old-items))]
            [(apply list (concat ['do setup] new-items))]))
        issue-kinds (fn [old-items new-items]
                      (set (map :kind
                                (:issues (compare-do old-items new-items)))))]
    (let [relation
          (compare-do [old-scalar old-scalar-reset]
                      [new-set new-bind])]
      (assert (empty? (:issues relation)))
      (assert (= [:exact-var-initialization-unit]
                 (mapv :kind (:events relation))))
      (assert (= :old-reset-plus-exact-column-int-cast
                 (:metadata-relation (first (:events relation))))))
    (let [relation
          (compare-do [old-function old-function-reset]
                      [new-function-set new-function-bind])]
      (assert (empty? (:issues relation)))
      (assert (= :exact-encoded-function-clauses
                 (:root-relation (first (:events relation))))))
    (let [relation
          (compare-do [old-scalar]
                      [(set-meta "f" added-meta) new-bind])]
      (assert (empty? (:issues relation)))
      (assert (= :exact-added-column-only
                 (:metadata-relation (first (:events relation))))))
    (assert (contains?
              (issue-kinds [old-scalar old-scalar-reset]
                           [(set-meta "fixture.changed" "f" new-meta)
                            (bind-root "fixture.changed" "f" 1)])
              :invalid-var-initialization-unit))
    (assert (contains?
              (issue-kinds [old-scalar old-scalar-reset]
                           [(set-meta "f" new-meta)
                            (bind-root "g" 1)])
              :invalid-var-initialization-unit))
    (assert (seq (issue-kinds [old-scalar old-scalar-reset]
                              [(set-meta "fixture.var-unit" "fixture/f"
                                         new-meta)
                               (bind-root "fixture.var-unit" "fixture/f" 1)])))
    (assert (contains?
              (issue-kinds [old-scalar old-scalar-reset]
                           [new-set (bind-root "f" 2)])
              :invalid-var-initialization-unit))
    (assert (contains?
              (issue-kinds [old-function old-function-reset]
                           [new-function-set
                            (bind-root "f" '(fn f ([x] (inc x))))])
              :invalid-var-initialization-unit))
    (assert (contains?
              (issue-kinds [old-scalar old-scalar-reset]
                           [(set-meta "f"
                              {:private false :column (list 'int 1)})
                            new-bind])
              :invalid-var-initialization-unit))
    (assert (seq (issue-kinds [old-scalar old-scalar-reset]
                              [new-bind new-set])))
    (assert (seq (issue-kinds [old-scalar old-scalar-reset]
                              [new-set marker new-bind])))
    (assert (seq (issue-kinds [old-scalar old-scalar-reset]
                              [new-set new-bind new-set new-bind])))
    (let [new-definition old-scalar
          exact-reset (reset 'f 'f added-meta)
          relation (compare-do [old-scalar]
                               [new-definition exact-reset])]
      (assert (empty? (:issues relation)))
      (assert (= [:scalar-def-plus-exact-reset]
                 (mapv :kind (:events relation)))))
    (assert (contains?
              (issue-kinds [old-scalar]
                           [(list 'def 'f 2)
                            (reset 'f 'f added-meta)])
              :invalid-scalar-reset-addition))
    (assert (seq
              (issue-kinds [old-scalar]
                           [old-scalar
                            (reset 'g 'g added-meta)])))
    (assert (contains?
              (issue-kinds [old-scalar]
                           [old-scalar
                            (reset 'fixture.var-unit/f 'f added-meta)])
              :invalid-scalar-reset-addition))
    (assert (contains?
              (issue-kinds [old-scalar]
                           [old-scalar
                            (reset 'f 'f {:column (list 'int 2)})])
              :invalid-scalar-reset-addition))
    (assert (seq (issue-kinds [old-scalar]
                              [(reset 'f 'f added-meta) old-scalar])))
    (assert (seq (issue-kinds [old-scalar]
                              [old-scalar marker
                               (reset 'f 'f added-meta)])))
    (assert (seq (issue-kinds [old-scalar]
                              [old-scalar
                               (reset 'f 'f added-meta)
                               (reset 'f 'f added-meta)]))))
  (let [namespace-name 'fixture.declare-unit
        setup (list 'clojure.core/in-ns (list 'quote namespace-name))
        target (fn [namespace name]
                 (list 'clojure.lang.RT/var namespace name))
        setmeta (fn [namespace name metadata]
                  (list '.setMeta (target namespace name) metadata))
        bind-root (fn [namespace name value]
                    (list '.bindRoot (target namespace name) value))
        exact-meta exact-declared-metadata
        exact-setmeta (setmeta (str namespace-name) "x" exact-meta)
        marker '(def marker true)
        compare-do
        (fn [old-items new-items]
          (compare-source-forms
            [(apply list (concat ['do setup] old-items))]
            [(apply list (concat ['do setup] new-items))]))
        issue-kinds
        (fn [old-items new-items]
          (set (map :kind (:issues (compare-do old-items new-items)))))
        exact-event?
        (fn [relation]
          (= [:exact-declare-setmeta-unit]
             (mapv :kind (:events relation))))]
    ;; Two positives: the old declaration may use either the source-local or
    ;; exact source-qualified spelling.  The runtime target stays unqualified.
    (doseq [old-declaration
            ['(declare x) '(declare fixture.declare-unit/x)]]
      (let [relation (compare-do [old-declaration] [exact-setmeta])]
        (assert (empty? (:issues relation)))
        (assert (exact-event? relation))))
    ;; Eight fail-closed negatives from the frozen relation boundary.
    (assert (seq (issue-kinds
                   ['(declare x)]
                   [(setmeta "fixture.other" "x" exact-meta)])))
    (assert (seq (issue-kinds
                   ['(declare x)]
                   [(setmeta (str namespace-name) "y" exact-meta)])))
    (assert (seq (issue-kinds
                   ['(declare x)]
                   [(setmeta (str namespace-name)
                      "fixture.declare-unit/x" exact-meta)])))
    (assert (seq
              (issue-kinds
                ['(declare x)]
                [(setmeta (str namespace-name) "x"
                   {:declared true :column (list 'int 2)})])))
    (assert (seq
              (issue-kinds
                ['(declare x)]
                [(setmeta (str namespace-name) "x"
                   {:column (list 'int 1)})])))
    (assert (seq
              (issue-kinds
                ['(declare x)]
                [(setmeta (str namespace-name) "x"
                   {:declared true :column (list 'int 1) :private true})])))
    (assert (seq (issue-kinds
                   ['(declare x)]
                   [(bind-root (str namespace-name) "x" true)])))
    (assert (seq (issue-kinds
                   ['(declare x y)] [exact-setmeta])))
    ;; Duplicate identities cannot be admitted on either side.
    (assert (seq
              (issue-kinds ['(declare x)]
                           [exact-setmeta exact-setmeta])))
    (assert (seq
              (issue-kinds ['(declare x) '(declare x)]
                           [exact-setmeta])))
    ;; Reordering and intervening forms remain observable sequence issues.
    (assert (seq
              (issue-kinds
                ['(declare x) '(declare y)]
                [(setmeta (str namespace-name) "y" exact-meta)
                 exact-setmeta])))
    (assert (seq
              (issue-kinds ['(declare x)] [marker exact-setmeta]))))
  (let [namespace-name 'fixture.reset-column-cast
        setup (list 'clojure.core/in-ns (list 'quote namespace-name))
        reset
        (fn [target-name payload-name payload-ns metadata]
          (list 'reset-meta! (list 'var target-name)
                (list 'assoc metadata :name
                      (list 'quote payload-name) :ns payload-ns)))
        old-meta-f {:private true :column 1}
        new-meta-f {:private true :column (list 'int 1)}
        old-meta-g {:arglists '([x]) :doc "fixture" :column 7}
        new-meta-g {:arglists '([x]) :doc "fixture"
                    :column (list 'int 7)}
        old-reset-f (reset 'f 'f '*ns* old-meta-f)
        new-reset-f (reset 'f 'f '*ns* new-meta-f)
        old-reset-g (reset 'g 'g '*ns* old-meta-g)
        new-reset-g (reset 'g 'g '*ns* new-meta-g)
        marker '(def marker true)
        proof
        (fn [old-form new-form]
          (exact-reset-column-cast-proof
            old-form new-form
            (assoc (relation-state)
                   :old-direct-reset-counts
                   (direct-reset-identity-counts [old-form])
                   :new-direct-reset-counts
                   (direct-reset-identity-counts [new-form]))))
        compare-do
        (fn [old-items new-items]
          (compare-source-forms
            [(apply list (concat ['do setup] old-items))]
            [(apply list (concat ['do setup] new-items))]))
        exact-event-count
        (fn [relation]
          (count (filter #(= :exact-reset-column-cast-unit (:kind %))
                         (:events relation))))
        positives
        [[:scalar-metadata (proof old-reset-f new-reset-f)]
         [:function-metadata (proof old-reset-g new-reset-g)]]
        negatives
        [[:target-changed
          (proof old-reset-f
                 (reset 'g 'f '*ns* new-meta-f))]
         [:name-payload-changed
          (proof old-reset-f
                 (reset 'f 'g '*ns* new-meta-f))]
         [:ns-payload-changed
          (proof old-reset-f
                 (reset 'f 'f (list 'quote 'fixture.other) new-meta-f))]
         [:column-value-changed
          (proof old-reset-f
                 (reset 'f 'f '*ns*
                        {:private true :column (list 'int 2)}))]
         [:column-not-cast
          (proof old-reset-f old-reset-f)]
         [:other-metadata-value-changed
          (proof old-reset-f
                 (reset 'f 'f '*ns*
                        {:private false :column (list 'int 1)}))]
         [:metadata-key-added
          (proof old-reset-f
                 (reset 'f 'f '*ns*
                        {:private true :column (list 'int 1) :doc "added"}))]
         [:column-missing
          (proof old-reset-f
                 (reset 'f 'f '*ns* {:private true}))]]]
    ;; Frozen proof matrix: exactly two admitted relations and eight distinct
    ;; ways to leave the one-field substitution boundary.
    (assert (= 2 (count positives)))
    (assert (every? (comp :valid? second) positives))
    (assert (= 8 (count negatives)))
    (assert (every? (comp not :valid? second) negatives))
    ;; Exercise both comparator call sites: a direct reset pair and a reset
    ;; consumed alongside its unchanged definition.
    (let [relation (compare-do [old-reset-f] [new-reset-f])]
      (assert (empty? (:issues relation)))
      (assert (= 1 (exact-event-count relation))))
    (let [definition '(defn g ([x] x))
          relation (compare-do [definition old-reset-g]
                               [definition new-reset-g])]
      (assert (empty? (:issues relation)))
      (assert (= [:unchanged-definition :exact-reset-column-cast-unit]
                 (mapv :kind (:events relation)))))
    ;; Identity uniqueness is corpus-wide on each side.  Order and
    ;; intervening forms remain visible to the existing sequence comparator.
    (let [duplicate-old
          (compare-do [old-reset-f old-reset-f] [new-reset-f])
          duplicate-new
          (compare-do [old-reset-f] [new-reset-f new-reset-f])
          reordered
          (compare-do [old-reset-f old-reset-g]
                      [new-reset-g new-reset-f])
          intervening
          (compare-do [old-reset-f] [marker new-reset-f])]
      (assert (seq (:issues duplicate-old)))
      (assert (zero? (exact-event-count duplicate-old)))
      (assert (seq (:issues duplicate-new)))
      (assert (zero? (exact-event-count duplicate-new)))
      (assert (seq (:issues reordered)))
      (assert (seq (:issues intervening)))))
  (let [namespace-name 'fixture.protocol-unit
        setup (list 'clojure.core/in-ns (list 'quote namespace-name))
        runtime-target
        (fn [target-namespace name]
          (list 'clojure.lang.RT/var (str target-namespace) (str name)))
        quote-arity
        (fn [arity]
          (mapv #(list 'quote %) arity))
        unit
        (fn [protocol-name protocol-doc methods]
          (let [metadata-symbol
                (symbol (str "protocol_metadata__" (name protocol-name)))
                old-methods
                (mapv (fn [{:keys [name arities tag]}]
                        (let [name (cond-> name tag (with-meta {:tag tag}))]
                          (apply list (cons name arities))))
                      methods)
                new-methods
                (mapv (fn [{:keys [name arities doc]}]
                        (apply list
                               (concat [name] arities (when doc [doc]))))
                      methods)
                old-protocol
                (apply list (concat ['defprotocol protocol-name]
                                    old-methods))
                new-protocol
                (apply list
                       (concat ['defprotocol protocol-name]
                               (when protocol-doc [protocol-doc])
                               new-methods))
                protocol-target (runtime-target namespace-name protocol-name)
                protocol-reset
                (list 'reset-meta! protocol-target
                      (list 'assoc
                            (list 'assoc metadata-symbol :doc protocol-doc)
                            :name (list 'quote protocol-name) :ns '*ns*))
                method-wrappers
                (mapv
                  (fn [index {:keys [name arities doc tag]}]
                    (let [signature-symbol
                          (symbol (str "protocol_signature__" index))
                          method-name-symbol
                          (symbol (str "protocol_method_name__" index))
                          arglists
                          (apply list 'clojure.core/list
                                 (map quote-arity arities))
                          signature-map
                          {:tag (when tag (list 'quote tag))
                           :name (list '.withMeta (list 'quote name)
                                       {:arglists arglists})
                           :arglists arglists
                           :doc doc}
                          signature-expression
                          (list 'assoc signature-map :protocol protocol-target)
                          method-target (runtime-target namespace-name name)]
                      (list 'let
                            [signature-symbol signature-expression
                             method-name-symbol
                             (list 'with-meta
                                   (list :name signature-symbol)
                                   signature-symbol)]
                            (list 'reset-meta! method-target
                                  (list 'assoc signature-symbol
                                        :name method-name-symbol :ns '*ns*)))))
                  (range 1 (inc (count methods))) methods)
                wrapper
                (apply list
                       (concat
                         ['let [metadata-symbol {:column (list 'int 1)}]
                          new-protocol protocol-reset]
                         method-wrappers))]
            {:cache (list 'defonce protocol-name {})
             :protocol old-protocol
             :wrapper wrapper}))
        p (unit 'P nil
                [{:name 'p :arities [['x]] :doc "p-doc"}])
        q (unit 'Q "protocol-doc"
                [{:name 'q :arities [['x] ['x 'y]] :doc nil}
                 {:name 'q2 :arities [['x]] :doc "q2-doc"}])
        marker '(def marker true)
        replace-nth
        (fn [form index value]
          (with-meta (apply list (assoc (vec form) index value))
                     (meta form)))
        compare-do
        (fn [old-items new-items]
          (compare-source-forms
            [(apply list (concat ['do setup] old-items))]
            [(apply list (concat ['do setup] new-items))]))
        relation
        (fn [old-items new-items]
          (compare-do old-items new-items))
        event-count
        (fn [result]
          (count
            (filter #(= :exact-protocol-initialization-unit (:kind %))
                    (:events result))))
        positive-relations
        [(relation [(:cache p) (:protocol p)] [(:wrapper p)])
         (relation [(:cache q) (:protocol q)] [(:wrapper q)])]
        p-bindings (second (:wrapper p))
        bad-metadata-wrapper
        (replace-nth (:wrapper p) 1
                     (assoc p-bindings 1 {:column (list 'int 2)}))
        negative-relations
        [[:nonempty-cache
          (relation [(list 'defonce 'P {:x true}) (:protocol p)]
                    [(:wrapper p)])]
         [:cache-name-changed
          (relation [(list 'defonce 'Other {}) (:protocol p)]
                    [(:wrapper p)])]
         [:protocol-metadata-changed
          (relation [(:cache p) (:protocol p)] [bad-metadata-wrapper])]
         [:protocol-reset-changed
          (relation [(:cache p) (:protocol p)]
                    [(replace-nth (:wrapper p) 3 '(identity nil))])]
         [:method-wrapper-changed
          (relation [(:cache p) (:protocol p)]
                    [(replace-nth (:wrapper p) 4 '(identity nil))])]
         [:extra-wrapper-body
          (relation [(:cache p) (:protocol p)]
                    [(with-meta
                       (apply list (concat (:wrapper p) ['(identity nil)]))
                       (meta (:wrapper p)))])]
         [:duplicate-old-identity
          (relation [(:cache p) (:protocol p) (:cache p) (:protocol p)]
                    [(:wrapper p)])]
         [:duplicate-new-identity
          (relation [(:cache p) (:protocol p)]
                    [(:wrapper p) (:wrapper p)])]]]
    ;; Frozen proof matrix: two exact wrappers and eight fail-closed exits.
    (assert (= 2 (count positive-relations)))
    (assert (every? #(and (empty? (:issues %)) (= 1 (event-count %)))
                    positive-relations))
    (assert (= 8 (count negative-relations)))
    (assert (every? (fn [[_ result]]
                      (and (seq (:issues result))
                           (zero? (event-count result))))
                    negative-relations))
    ;; Integration consumes exactly two adjacent old forms and one new form.
    (assert (= [:exact-protocol-initialization-unit]
               (mapv :kind (:events (first positive-relations)))))
    ;; Reordering, an intervening old form, and a changed runtime namespace
    ;; cannot become protocol units.  A tagged method remains excluded until
    ;; its complete wrapper relation is separately proven.
    (let [reordered
          (relation [(:cache p) (:protocol p) (:cache q) (:protocol q)]
                    [(:wrapper q) (:wrapper p)])
          intervening
          (relation [(:cache p) marker (:protocol p)] [(:wrapper p)])
          wrong-namespace-wrapper
          (let [other (unit 'P nil
                            [{:name 'p :arities [['x]] :doc "p-doc"}])
                target (runtime-target 'fixture.other 'P)
                reset (list 'reset-meta! target
                            (list 'assoc
                                  (list 'assoc
                                        (first (second (:wrapper other)))
                                        :doc nil)
                                  :name (list 'quote 'P) :ns '*ns*))]
            (replace-nth (:wrapper other) 3 reset))
          tagged
          (unit 'Tagged nil
                [{:name 'tagged
                  :arities [['x]]
                  :doc nil
                  :tag 'java.lang.String}])
          tagged-relation
          (relation [(:cache tagged) (:protocol tagged)]
                    [(:wrapper tagged)])]
      (assert (seq (:issues reordered)))
      (assert (zero? (event-count reordered)))
      (assert (seq (:issues intervening)))
      (assert (zero? (event-count intervening)))
      (assert (seq (:issues
                     (relation [(:cache p) (:protocol p)]
                               [wrong-namespace-wrapper]))))
      (assert (seq (:issues tagged-relation)))
      (assert (zero? (event-count tagged-relation)))))
  (let [namespace-name 'fixture.defmulti-unit
        setup (list 'clojure.core/in-ns (list 'quote namespace-name))
        runtime-target
        (fn [target-namespace identity]
          (list 'clojure.lang.RT/var
                (str target-namespace) (str identity)))
        unit
        (fn [identity dispatch local]
          (let [target (runtime-target namespace-name identity)
                metadata {:column (list 'int 1)}
                set-form (list '.setMeta target metadata)
                var-reference (list 'var identity)
                tagged-local (with-meta local {:tag 'clojure.lang.Var})
                wrapper
                (list
                  'let [local var-reference]
                  (list
                    'when-not
                    (list 'and
                          (list '.hasRoot tagged-local)
                          (list 'instance? 'clojure.lang.MultiFn
                                (list 'deref local)))
                    set-form
                    (list '.bindRoot target
                          (list 'clojure.lang.MultiFn.
                                (str identity) dispatch :default
                                (list 'var 'clojure.core/global-hierarchy)))
                    var-reference))]
            {:old (list 'defmulti identity dispatch)
             :set set-form
             :wrapper wrapper}))
        p (unit 'P 'dispatch-p 'v__fixture_p)
        q (unit 'Q '(fn q-dispatch ([x] (:kind x))) 'v__fixture_q)
        replace-nth
        (fn [form index value]
          (with-meta (apply list (assoc (vec form) index value))
                     (meta form)))
        replace-wrapper-body
        (fn [wrapper body]
          (replace-nth wrapper 2 body))
        proof
        (fn [old-form set-form wrapper]
          (let [identity (symbol (name (second old-form)))
                old-unit
                (old-defmulti-initialization-unit old-form 0)
                new-unit
                (new-defmulti-initialization-unit [set-form wrapper] 0)
                state
                (assoc (relation-state)
                       :source-namespace namespace-name
                       :old-defmulti-unit-counts {identity 1}
                       :new-defmulti-unit-counts {identity 1}
                       :defmulti-unit-order-valid? true)]
            (exact-defmulti-initialization-proof
              old-unit new-unit state)))
        compare-do
        (fn [old-items new-items]
          (compare-source-forms
            [(apply list (concat ['do setup] old-items))]
            [(apply list (concat ['do setup] new-items))]))
        event-count
        (fn [relation]
          (count
            (filter #(= :exact-defmulti-initialization-unit (:kind %))
                    (:events relation))))
        p-body (nth (:wrapper p) 2)
        p-predicate (nth p-body 1)
        p-instance-check (nth p-predicate 2)
        p-bindings (second (:wrapper p))
        p-target (second (:set p))
        p-constructor (nth (nth p-body 3) 2)
        positives
        [[:symbol-dispatch p]
         [:function-dispatch q]]
        negatives
        [[:old-doc-or-option-added
          (assoc p :old '(defmulti P "doc" dispatch-p))]
         [:old-name-annotated
          (assoc p :old
                 (list 'defmulti
                       (with-meta 'P {:private true}) 'dispatch-p))]
         [:outer-target-namespace-changed
          (assoc p :set
                 (list '.setMeta
                       (runtime-target 'fixture.other 'P)
                       {:column (list 'int 1)}))]
         [:outer-metadata-changed
          (assoc p :set
                 (list '.setMeta p-target {:column (list 'int 2)}))]
         [:outer-metadata-tag-added
          (assoc p :set
                 (list '.setMeta p-target
                       {:column (list 'int 1)
                        :tag 'java.lang.String}))]
         [:binding-var-changed
          (assoc p :wrapper
                 (replace-nth (:wrapper p) 1
                              [(first p-bindings) '(var Other)]))]
         [:guard-class-changed
          (assoc p :wrapper
                 (replace-wrapper-body
                   (:wrapper p)
                   (replace-nth
                     p-body 1
                     (replace-nth
                       p-predicate 2
                       (replace-nth p-instance-check 1
                                    'java.lang.Object)))))]
         [:inner-setmeta-changed
          (assoc p :wrapper
                 (replace-wrapper-body
                   (:wrapper p)
                   (replace-nth
                     p-body 2
                     (list '.setMeta p-target
                           {:column (list 'int 2)}))))]
         [:dispatch-changed
          (assoc p :wrapper
                 (replace-wrapper-body
                   (:wrapper p)
                   (replace-nth
                     p-body 3
                     (list '.bindRoot p-target
                           (replace-nth p-constructor 2
                                        'other-dispatch)))))]
         [:default-changed
          (assoc p :wrapper
                 (replace-wrapper-body
                   (:wrapper p)
                   (replace-nth
                     p-body 3
                     (list '.bindRoot p-target
                           (replace-nth p-constructor 3 :other)))))]
         [:hierarchy-changed
          (assoc p :wrapper
                 (replace-wrapper-body
                   (:wrapper p)
                   (replace-nth
                     p-body 3
                     (list '.bindRoot p-target
                           (replace-nth
                             p-constructor 4
                             '(var custom-hierarchy))))))]
         [:extra-wrapper-body
          (assoc p :wrapper
                 (with-meta
                   (apply list
                          (concat (:wrapper p) ['(identity nil)]))
                   (meta (:wrapper p))))]]]
    ;; Frozen proof matrix: two exact dispatch shapes and twelve independent
    ;; exits from identity, metadata, body, target, or MultiFn semantics.
    (assert (= 2 (count positives)))
    (doseq [[_ {:keys [old set wrapper]}] positives]
      (assert (:valid? (proof old set wrapper)))
      (let [relation (compare-do [old] [set wrapper])]
        (assert (empty? (:issues relation)))
        (assert (= 1 (event-count relation)))
        (assert (= [:exact-defmulti-initialization-unit]
                   (mapv :kind (:events relation))))))
    (assert (= 12 (count negatives)))
    (doseq [[_ {:keys [old set wrapper]}] negatives]
      (assert (not (:valid? (proof old set wrapper))))
      (let [relation (compare-do [old] [set wrapper])]
        (assert (seq (:issues relation)))
        (assert (zero? (event-count relation)))))
    ;; Identity cardinality, relative order, and exact new adjacency remain
    ;; fail-closed at the same direct-source scope as the corpus relation.
    (let [duplicate-old
          (compare-do [(:old p) (:old p)] [(:set p) (:wrapper p)])
          duplicate-new
          (compare-do [(:old p)]
                      [(:set p) (:wrapper p) (:set p) (:wrapper p)])
          reordered
          (compare-do [(:old p) (:old q)]
                      [(:set q) (:wrapper q) (:set p) (:wrapper p)])
          intervening
          (compare-do [(:old p)]
                      [(:set p) '(def marker true) (:wrapper p)])]
      (doseq [relation [duplicate-old duplicate-new reordered intervening]]
        (assert (seq (:issues relation)))
        (assert (zero? (event-count relation))))))
  (println "METADATA_AWARE_SOURCE_DELTA_SELF_TEST_PASS"))

(if (= ["--self-test"] (vec *command-line-args*))
  (run-self-test!)
  (let [[old-root new-root output-root & extra] *command-line-args*]
    (when-not (and old-root new-root output-root (empty? extra))
      (binding [*out* *err*]
        (println "usage: validate-metadata-aware-source-delta.clj OLD_SOURCE_ROOT NEW_SOURCE_ROOT EMPTY_OUTPUT_ROOT|--self-test"))
      (System/exit 2))
    (validate-source-delta! old-root new-root output-root)))

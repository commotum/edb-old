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

;; Clojure emits lexical names such as fn__17083 and p1__17087#.  Their
;; numeric portions can vary when the same authored form is compiled in a
;; different surrounding compilation context.  They are not safe to erase
;; textually: each spelling still identifies one lexical binder, and every use
;; must resolve to that binder.  This relation therefore introduces a mapping
;; only at a recognized binding position, keeps mappings in lexical frames,
;; and requires a bijection across every active scope.
(def compiler-generated-numeric-identifier
  #"^(.+__)([0-9]+)(#?)$")

(defn symbol-identity [value]
  [(namespace value) (name value)])

(defn compiler-generated-identifier-shape [value]
  (when (and (symbol? value) (nil? (namespace value)))
    (when-let [[_ stem _ auto-gensym]
               (re-matches compiler-generated-numeric-identifier
                           (name value))]
      [stem auto-gensym])))

(defn semantic-metadata-equal? [left right]
  (= (metadata-for-view left :semantic)
     (metadata-for-view right :semantic)))

(defn empty-alpha-environment []
  {:frames [{:left-to-right {} :right-to-left {}}]})

(defn push-alpha-scope [environment]
  (update environment :frames conj
          {:left-to-right {} :right-to-left {}}))

(defn active-alpha-mapping [environment direction identity]
  (loop [frames (rseq (:frames environment))]
    (when (seq frames)
      (let [frame (first frames)]
        (if (contains? (get frame direction) identity)
          (get-in frame [direction identity])
          (recur (next frames)))))))

(defn binding-pattern-collides-with-active-mapping?
  [environment direction pattern]
  (boolean
    (some (fn [value]
            (and (symbol? value)
                 (active-alpha-mapping environment direction
                                       (symbol-identity value))))
          (tree-seq coll? seq pattern))))

(defn add-alpha-binding [environment left right]
  (let [left-shape (compiler-generated-identifier-shape left)
        right-shape (compiler-generated-identifier-shape right)
        left-id (symbol-identity left)
        right-id (symbol-identity right)
        frame (peek (:frames environment))]
    (cond
      (not (semantic-metadata-equal? left right))
      nil

      (and (nil? left-shape) (nil? right-shape))
      (when (= left-id right-id) environment)

      (not= left-shape right-shape)
      nil

      ;; Reusing either side in one lexical frame would collapse two binders
      ;; or make one binder stand for two different variables.
      (or (active-alpha-mapping environment :left-to-right left-id)
          (active-alpha-mapping environment :right-to-left right-id)
          (contains? (:left-to-right frame) left-id)
          (contains? (:right-to-left frame) right-id))
      nil

      :else
      (update environment :frames
              (fn [frames]
                (let [frame (peek frames)
                      frame (-> frame
                                (assoc-in [:left-to-right left-id] right-id)
                                (assoc-in [:right-to-left right-id] left-id))]
                  (conj (pop frames) frame)))))))

(defn alpha-symbol-reference-equal? [left right environment]
  (and (semantic-metadata-equal? left right)
       (let [left-id (symbol-identity left)
             right-id (symbol-identity right)
             mapped-right (active-alpha-mapping
                            environment :left-to-right left-id)
             mapped-left (active-alpha-mapping
                           environment :right-to-left right-id)]
         (cond
           mapped-right (= mapped-right right-id)
           mapped-left (= mapped-left left-id)
           :else (= left-id right-id)))))

(declare alpha-value-equivalent?)

(defn alpha-values-equivalent? [left-values right-values environment]
  (and (= (count left-values) (count right-values))
       (every? true?
               (map #(alpha-value-equivalent? %1 %2 environment)
                    left-values right-values))))

(defn semantic-map-index [value]
  (reduce (fn [index [key item]]
            (let [encoded-key (encode-form key :semantic)]
              (if (contains? index encoded-key)
                (reduced nil)
                (assoc index encoded-key item))))
          {}
          value))

(defn alpha-map-equivalent? [left right environment]
  ;; Pair unordered entries only through exact semantic keys.  This permits a
  ;; binding-aware comparison inside the corresponding values without ever
  ;; guessing which entry on one side belongs to an entry on the other.
  (let [left-index (semantic-map-index left)
        right-index (semantic-map-index right)]
    (and left-index right-index
         (= (count left) (count right))
         (semantic-metadata-equal? left right)
         (= (set (keys left-index)) (set (keys right-index)))
         (every? (fn [[key left-item]]
                   (alpha-value-equivalent? left-item
                                            (get right-index key)
                                            environment))
                 left-index))))

(defn add-alpha-binding-pattern [environment left right]
  (cond
    (and (symbol? left) (symbol? right))
    (if (or (= '& left) (= '& right))
      (when (= left right) environment)
      (add-alpha-binding environment left right))

    (and (vector? left) (vector? right)
         (= (count left) (count right))
         (semantic-metadata-equal? left right))
    (loop [environment environment
           left-items (seq left)
           right-items (seq right)]
      (if-not left-items
        environment
        (when-let [next-environment
                   (add-alpha-binding-pattern environment
                                              (first left-items)
                                              (first right-items))]
          (recur next-environment (next left-items) (next right-items)))))

    ;; Map destructuring and other binding-pattern syntax is deliberately
    ;; conservative until it has its own binding-aware implementation.  An
    ;; exact pattern is still unsafe when one of its symbols shadows a mapped
    ;; outer binder: leaving that shadow unrecorded would let the outer mapping
    ;; rewrite references in the inner body.
    :else
    (when (and (= (encode-form left :semantic)
                  (encode-form right :semantic))
               (not (binding-pattern-collides-with-active-mapping?
                      environment :left-to-right left))
               (not (binding-pattern-collides-with-active-mapping?
                      environment :right-to-left right)))
      environment)))

(defn fn-form-parts [form]
  (let [tail (vec (rest form))
        named? (symbol? (first tail))
        function-name (when named? (first tail))
        tail (if named? (subvec tail 1) tail)]
    (cond
      (vector? (first tail))
      {:name function-name
       :clauses [{:metadata nil
                  :parameters (first tail)
                  :body (subvec tail 1)}]}

      (and (seq tail)
           (every? #(and (seq? %) (vector? (first %))) tail))
      {:name function-name
       :clauses (mapv (fn [clause]
                        {:metadata (meta clause)
                         :parameters (first clause)
                         :body (vec (rest clause))})
                      tail)}

      :else nil)))

(defn alpha-fn-equivalent? [left right environment]
  (let [left-parts (fn-form-parts left)
        right-parts (fn-form-parts right)]
    (when (and left-parts right-parts
               (= (count (:clauses left-parts))
                  (count (:clauses right-parts)))
               (semantic-metadata-equal? left right)
               (alpha-value-equivalent? (first left) (first right)
                                        environment))
      (let [function-environment (push-alpha-scope environment)
            function-environment
            (cond
              (and (nil? (:name left-parts)) (nil? (:name right-parts)))
              function-environment

              (and (symbol? (:name left-parts))
                   (symbol? (:name right-parts)))
              (add-alpha-binding function-environment
                                 (:name left-parts) (:name right-parts))

              :else nil)]
        (and function-environment
             (every?
               true?
               (map
                 (fn [left-clause right-clause]
                   (and (= (some-> (:metadata left-clause)
                                   (dissoc :file :line :column
                                           :end-line :end-column))
                           (some-> (:metadata right-clause)
                                   (dissoc :file :line :column
                                           :end-line :end-column)))
                        (let [clause-environment
                              (push-alpha-scope function-environment)
                              clause-environment
                              (add-alpha-binding-pattern
                                clause-environment
                                (:parameters left-clause)
                                (:parameters right-clause))]
                          (and clause-environment
                               (alpha-values-equivalent?
                                 (:body left-clause)
                                 (:body right-clause)
                                 clause-environment)))))
                 (:clauses left-parts) (:clauses right-parts))))))))

(def alpha-fn-heads
  #{"fn" "clojure.core/fn" "fn*"})

(def alpha-defmethod-heads
  #{"defmethod" "clojure.core/defmethod"})

(def alpha-sequential-binding-heads
  #{"let" "clojure.core/let" "let*"
    "loop" "clojure.core/loop" "loop*"
    "with-open" "clojure.core/with-open"
    "when-let" "clojure.core/when-let"
    "when-some" "clojure.core/when-some"})

;; These forms contain literal-symbol or lexical rules that are not modeled by
;; this narrow relation.  Exact semantic comparison is safer than treating
;; their contents as ordinary references.  In particular, high-level letfn is
;; not the alternating binder/value representation used by letfn*; if-let and
;; if-some do not scope their binding over the else branch; and with-local-vars
;; establishes all Var binders before evaluating the supplied initializers.
(def alpha-opaque-heads
  #{"var" "new" "." "set!" "declare"
    "letfn" "clojure.core/letfn" "letfn*"
    "if-let" "clojure.core/if-let"
    "if-some" "clojure.core/if-some"
    "with-local-vars" "clojure.core/with-local-vars"
    "instance?" "clojure.core/instance?"
    "deftype" "clojure.core/deftype" "deftype*"
    "reify" "clojure.core/reify" "reify*"})

(def alpha-def-heads
  #{"def" "defonce" "clojure.core/defonce"})

(defn interop-shorthand-call? [form]
  (and (seq? form)
       (symbol? (first form))
       (not= "." (str (first form)))
       (str/starts-with? (name (first form)) ".")))

(defn alpha-interop-shorthand-equivalent? [left right environment]
  (and (= (count left) (count right))
       (semantic-metadata-equal? left right)
       ;; The shorthand call head names a member; only target/argument forms
       ;; are lexical expressions.
       (= (encode-form (first left) :semantic)
          (encode-form (first right) :semantic))
       (alpha-values-equivalent? (rest left) (rest right) environment)))

(defn alpha-let-equivalent? [left right environment]
  (let [left-bindings (second left)
        right-bindings (second right)]
    (when (and (vector? left-bindings) (vector? right-bindings)
               (even? (count left-bindings))
               (= (count left-bindings) (count right-bindings))
               (= (count left) (count right))
               (semantic-metadata-equal? left right)
               (semantic-metadata-equal? left-bindings right-bindings)
               (alpha-value-equivalent? (first left) (first right)
                                        environment))
      (loop [offset 0
             scoped-environment (push-alpha-scope environment)]
        (if (= offset (count left-bindings))
          (alpha-values-equivalent? (nnext left) (nnext right)
                                    scoped-environment)
          (let [left-binding (nth left-bindings offset)
                right-binding (nth right-bindings offset)
                left-value (nth left-bindings (inc offset))
                right-value (nth right-bindings (inc offset))]
            (when (alpha-value-equivalent? left-value right-value
                                           scoped-environment)
              (when-let [next-environment
                         (add-alpha-binding-pattern scoped-environment
                                                    left-binding
                                                    right-binding)]
                (recur (+ offset 2) next-environment)))))))))

(defn alpha-catch-equivalent? [left right environment]
  (when (and (= (count left) (count right)) (<= 4 (count left))
             (semantic-metadata-equal? left right)
             (alpha-value-equivalent? (first left) (first right) environment)
             ;; A catch class is a literal class designator, not a lexical
             ;; reference, even if its spelling resembles a generated local.
             (= (encode-form (second left) :semantic)
                (encode-form (second right) :semantic))
             (symbol? (nth left 2)) (symbol? (nth right 2)))
    (when-let [scoped-environment
               (add-alpha-binding (push-alpha-scope environment)
                                  (nth left 2) (nth right 2))]
      (alpha-values-equivalent? (drop 3 left) (drop 3 right)
                                scoped-environment))))

(defn alpha-def-equivalent? [left right environment]
  (and (= (count left) (count right))
       (<= 2 (count left))
       (semantic-metadata-equal? left right)
       (= (encode-form (first left) :semantic)
          (encode-form (first right) :semantic))
       ;; The Var name is literal.  Initializer forms remain ordinary lexical
       ;; expressions and may legitimately reference an enclosing binder.
       (= (encode-form (second left) :semantic)
          (encode-form (second right) :semantic))
       (alpha-values-equivalent? (nnext left) (nnext right) environment)))

(defn alpha-defmethod-equivalent? [left right environment]
  ;; defmethod's tail is a fn-tail and can carry the compiler-generated
  ;; optional function name.  The multifn Var and dispatch value remain exact
  ;; literal selectors.
  (and (= (count left) (count right))
       (<= 5 (count left))
       (semantic-metadata-equal? left right)
       (= (encode-form (take 3 left) :semantic)
          (encode-form (take 3 right) :semantic))
       (boolean
         (alpha-fn-equivalent? (cons 'fn (drop 3 left))
                               (cons 'fn (drop 3 right))
                               environment))))

(defn alpha-value-equivalent? [left right environment]
  (cond
    (and (symbol? left) (symbol? right))
    (alpha-symbol-reference-equal? left right environment)

    (and (seq? left) (seq? right))
    (cond
      (or (core-call? left "quote") (core-call? right "quote"))
      (= (encode-form left :semantic) (encode-form right :semantic))

      (or (exact-call? left alpha-opaque-heads)
          (exact-call? right alpha-opaque-heads))
      (= (encode-form left :semantic) (encode-form right :semantic))

      (or (exact-call? left alpha-fn-heads)
          (exact-call? right alpha-fn-heads))
      (and (exact-call? left alpha-fn-heads)
           (exact-call? right alpha-fn-heads)
           (boolean (alpha-fn-equivalent? left right environment)))

      (or (exact-call? left alpha-sequential-binding-heads)
          (exact-call? right alpha-sequential-binding-heads))
      (and (exact-call? left alpha-sequential-binding-heads)
           (exact-call? right alpha-sequential-binding-heads)
           (= (first left) (first right))
           (boolean (alpha-let-equivalent? left right environment)))

      (or (exact-call? left alpha-def-heads)
          (exact-call? right alpha-def-heads))
      (and (exact-call? left alpha-def-heads)
           (exact-call? right alpha-def-heads)
           (boolean (alpha-def-equivalent? left right environment)))

      (or (exact-call? left alpha-defmethod-heads)
          (exact-call? right alpha-defmethod-heads))
      (and (exact-call? left alpha-defmethod-heads)
           (exact-call? right alpha-defmethod-heads)
           (boolean (alpha-defmethod-equivalent? left right environment)))

      (or (interop-shorthand-call? left)
          (interop-shorthand-call? right))
      (and (interop-shorthand-call? left)
           (interop-shorthand-call? right)
           (boolean
             (alpha-interop-shorthand-equivalent? left right environment)))

      (or (exact-call? left #{"catch"})
          (exact-call? right #{"catch"}))
      (and (exact-call? left #{"catch"})
           (exact-call? right #{"catch"})
           (boolean (alpha-catch-equivalent? left right environment)))

      :else
      (and (semantic-metadata-equal? left right)
           (alpha-values-equivalent? left right environment)))

    (and (vector? left) (vector? right))
    (and (semantic-metadata-equal? left right)
         (alpha-values-equivalent? left right environment))

    (and (map? left) (map? right))
    (alpha-map-equivalent? left right environment)

    (and (set? left) (set? right))
    (= (encode-form left :semantic) (encode-form right :semantic))

    :else
    (= (encode-form left :semantic) (encode-form right :semantic))))

(defn compiler-alpha-equivalent? [left-forms right-forms]
  (alpha-values-equivalent? left-forms right-forms
                            (empty-alpha-environment)))

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
        leading-doc? (string? (first tail))
        tail (if leading-doc? (rest tail) tail)
        trailing-doc? (and (seq tail) (string? (last tail)))
        tail (if trailing-doc? (butlast tail) tail)]
    (when (and leading-doc? trailing-doc?)
      (throw (ex-info "defprotocol method has multiple doc strings"
                      {:method method-name :declaration declaration})))
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
                 (conj events [:form form])
                 legacy-count expanded-count))))))

(defn read-one-form [source]
  (binding [*read-eval* false]
    (read-string source)))

(defn require-alpha-result! [label expected left right]
  (let [actual (boolean (compiler-alpha-equivalent? left right))]
    (when-not (= expected actual)
      (throw (ex-info "compiler-generated alpha regression failed"
                      {:label label :expected expected :actual actual
                       :left left :right right})))))

(defn run-compiler-alpha-self-test! [announce?]
  (let [positive-left
        [(read-one-form
           "(fn fn__101 ([p1__102# p2__103#] (let [tmp__104# p1__102#] (fn fn__105 ([p1__106#] [fn__105 tmp__104# p1__106# p2__103#])))))")]
        positive-right
        [(read-one-form
           "(fn fn__901 ([p1__902# p2__903#] (let [tmp__904# p1__902#] (fn fn__905 ([p1__906#] [fn__905 tmp__904# p1__906# p2__903#])))))")]
        loop-left
        [(read-one-form
           "(loop [p__101 0 q__102 p__101] [p__101 q__102])")]
        loop-right
        [(read-one-form
           "(loop [p__901 0 q__902 p__901] [p__901 q__902])")]
        catch-left
        [(read-one-form
           "(try work (catch java.lang.Exception e__101 e__101))")]
        catch-right
        [(read-one-form
           "(try work (catch java.lang.Exception e__901 e__901))")]
        vector-destructuring-left
        [(read-one-form "(fn [[p__101]] p__101)")]
        vector-destructuring-right
        [(read-one-form "(fn [[p__901]] p__901)")]
        syntax-unquote-left
        [(read-one-form "(fn [p__101] `(~p__101))")]
        syntax-unquote-right
        [(read-one-form "(fn [p__901] `(~p__901))")]
        collision-left
        [(read-one-form "(fn [p__1# p__2#] [p__1# p__2#])")]
        collision-right
        [(read-one-form "(fn [p__3# p__3#] [p__3# p__3#])")]
        nested-collision-left
        [(read-one-form
           "(fn [p__1#] (fn [p__2#] [p__1# p__2#]))")]
        nested-collision-right
        [(read-one-form
           "(fn [p__3#] (fn [p__3#] [p__3# p__3#]))")]
        cross-use-left
        [(read-one-form
           "(fn [p1__1# p2__2#] [p1__1# p2__2#])")]
        cross-use-right
        [(read-one-form
           "(fn [p1__3# p2__4#] [p2__4# p1__3#])")]
        scope-left
        [(read-one-form "(fn [p__1#] p__1#)")
         (read-one-form "p__1#")]
        scope-right
        [(read-one-form "(fn [p__2#] p__2#)")
         (read-one-form "p__2#")]
        qualified-fn-left
        [(read-one-form "(foo/fn [p__1#] p__1#)")]
        qualified-fn-right
        [(read-one-form "(foo/fn [p__2#] p__2#)")]
        var-literal-left
        [(read-one-form "(fn [p__1#] (var p__1#))")]
        var-literal-right
        [(read-one-form "(fn [p__2#] (var p__2#))")]
        interop-member-left
        [(read-one-form "(fn [method__1] (. target method__1))")]
        interop-member-right
        [(read-one-form "(fn [method__2] (. target method__2))")]
        interop-shorthand-left
        [(read-one-form "(fn [.method__1] (.method__1 target))")]
        interop-shorthand-right
        [(read-one-form "(fn [.method__2] (.method__2 target))")]
        letfn-shadow-left
        [(read-one-form
           "(fn [p__1] (letfn [(p__1 [] 0) (g [] p__1)] p__1))")]
        letfn-shadow-right
        [(read-one-form
           "(fn [p__2] (letfn [(p__1 [] 0) (g [] p__2)] p__2))")]
        destructuring-shadow-left
        [(read-one-form
           "(fn [p__1] (let [{:keys [p__1]} m] p__1))")]
        destructuring-shadow-right
        [(read-one-form
           "(fn [p__2] (let [{:keys [p__1]} m] p__2))")]
        conditional-scope-left
        [(read-one-form "(if-let [p__1 value] p__1 p__1)")]
        conditional-scope-right
        [(read-one-form "(if-let [p__2 value] p__2 p__2)")]
        catch-class-left
        [(read-one-form
           "(fn [Class__1] (try work (catch Class__1 e e)))")]
        catch-class-right
        [(read-one-form
           "(fn [Class__2] (try work (catch Class__2 e e)))")]
        quoted-left
        [(read-one-form "(fn [p__1] (quote p__1))")]
        quoted-right
        [(read-one-form "(fn [p__2] (quote p__2))")]
        syntax-quoted-data-left
        [(read-one-form "(fn [p__1] `(p__1))")]
        syntax-quoted-data-right
        [(read-one-form "(fn [p__2] `(p__2))")]
        conservative-shadow-left
        [(read-one-form "(fn [p__1] (fn [p__1] p__1))")]
        conservative-shadow-right
        [(read-one-form "(fn [p__2] (fn [p__2] p__2))")]
        exact-form (read-one-form "(fn [x] x)")
        location-left [(with-meta exact-form {:line 1 :column 2})]
        location-right [(with-meta exact-form {:line 90 :column 7})]
        semantic-metadata-left
        [(with-meta (read-one-form "(fn [p__1] p__1)") {:tag 'long})]
        semantic-metadata-right
        [(with-meta (read-one-form "(fn [p__2] p__2)") {:tag 'double})]
        keyed-map-alpha-left
        [(read-one-form
           "{:method (fn fn__101 ([x] x)), :fixed 7}")]
        keyed-map-alpha-right
        [(read-one-form
           "{:fixed 7, :method (fn fn__901 ([x] x))}")]
        keyed-map-cross-left
        [(read-one-form
           "{:a (fn fn__101 ([x] [:a x])), :b (fn fn__102 ([x] [:b x]))}")]
        keyed-map-cross-right
        [(read-one-form
           "{:a (fn fn__901 ([x] [:b x])), :b (fn fn__902 ([x] [:a x]))}")]
        defmethod-left
        [(read-one-form
           "(defmethod get-from-put :default fn__101 ([_] nil))")]
        defmethod-right
        [(read-one-form
           "(defmethod get-from-put :default fn__901 ([_] nil))")]
        defmethod-dispatch-right
        [(read-one-form
           "(defmethod get-from-put :other fn__901 ([_] nil))")]
        unordered-left [(array-map :a 1 :b 2) #{:a :b}]
        unordered-right [(array-map :b 2 :a 1) #{:b :a}]]
    (require-alpha-result! :nested-positive true
                           positive-left positive-right)
    (require-alpha-result! :sequential-loop-positive true
                           loop-left loop-right)
    (require-alpha-result! :catch-binder-positive true
                           catch-left catch-right)
    (require-alpha-result! :vector-destructuring-positive true
                           vector-destructuring-left
                           vector-destructuring-right)
    (require-alpha-result! :syntax-unquote-reference-positive true
                           syntax-unquote-left syntax-unquote-right)
    (require-alpha-result! :binder-collision false
                           collision-left collision-right)
    (require-alpha-result! :nested-binder-collision false
                           nested-collision-left nested-collision-right)
    (require-alpha-result! :cross-use false
                           cross-use-left cross-use-right)
    (require-alpha-result! :scope-escape false scope-left scope-right)
    (require-alpha-result! :qualified-lookalike-not-a-binding-form false
                           qualified-fn-left qualified-fn-right)
    (require-alpha-result! :var-argument-is-literal false
                           var-literal-left var-literal-right)
    (require-alpha-result! :interop-member-is-literal false
                           interop-member-left interop-member-right)
    (require-alpha-result! :interop-shorthand-head-is-literal false
                           interop-shorthand-left interop-shorthand-right)
    (require-alpha-result! :letfn-unmodeled-shadow false
                           letfn-shadow-left letfn-shadow-right)
    (require-alpha-result! :map-destructuring-shadow false
                           destructuring-shadow-left
                           destructuring-shadow-right)
    (require-alpha-result! :conditional-else-not-bound false
                           conditional-scope-left conditional-scope-right)
    (require-alpha-result! :catch-class-is-literal false
                           catch-class-left catch-class-right)
    (require-alpha-result! :quoted-symbol-is-data false
                           quoted-left quoted-right)
    (require-alpha-result! :syntax-quoted-symbol-is-data false
                           syntax-quoted-data-left syntax-quoted-data-right)
    (require-alpha-result! :shadowing-is-conservatively-rejected false
                           conservative-shadow-left
                           conservative-shadow-right)
    (require-alpha-result! :semantic-metadata-preserved false
                           semantic-metadata-left semantic-metadata-right)
    (require-alpha-result! :exact-keyed-map-alpha true
                           keyed-map-alpha-left keyed-map-alpha-right)
    (require-alpha-result! :map-values-not-repaired-across-keys false
                           keyed-map-cross-left keyed-map-cross-right)
    (require-alpha-result! :defmethod-optional-name true
                           defmethod-left defmethod-right)
    (require-alpha-result! :defmethod-dispatch-is-literal false
                           defmethod-left defmethod-dispatch-right)
    (require-alpha-result! :unordered-data-order true
                           unordered-left unordered-right)
    ;; The new relation must not redefine either existing output lane.
    (when (= (encode-form positive-left :semantic)
             (encode-form positive-right :semantic))
      (throw (ex-info "alpha positive unexpectedly changed semantic-exact lane"
                      {})))
    (when-not (= (encode-form location-left :semantic)
                 (encode-form location-right :semantic))
      (throw (ex-info "location-insensitive semantic lane regressed" {})))
    (when (= (encode-form location-left :all)
             (encode-form location-right :all))
      (throw (ex-info "exact metadata lane erased source locations" {})))
    (when announce?
      (println "PEER_SOURCE_ALPHA_SELF_TEST_PASS"
               "positive=8" "collision_negative=2"
               "cross_use_negative=1" "scope_negative=1"
               "literal_or_unmodeled_negative=12"
               "shadowing_conservative_negative=1"
               "metadata_negative=1"
               "exact_and_semantic_lanes_preserved=true"))))

(defn run-protocol-normalization-self-test! [announce?]
  (let [leading (split-protocol-declaration
                  '(fetch "documentation" [this key]))
        trailing (split-protocol-declaration
                   '(fetch [this key] "documentation"))
        overloaded (split-protocol-declaration
                     '(fetch [this] [this key] "documentation"))
        duplicate-doc-rejected?
        (try
          (split-protocol-declaration
            '(fetch "first" [this key] "second"))
          false
          (catch clojure.lang.ExceptionInfo _ true))
        interleaved-doc-rejected?
        (try
          (split-protocol-declaration
            '(fetch [this] "middle" [this key]))
          false
          (catch clojure.lang.ExceptionInfo _ true))]
    (when-not (and (= leading trailing)
                   (= "fetch" (:name leading))
                   (= 1 (count (:arglists leading)))
                   (= 2 (count (:arglists overloaded)))
                   duplicate-doc-rejected?
                   interleaved-doc-rejected?)
      (throw (ex-info "protocol declaration normalization regression failed"
                      {:leading leading :trailing trailing
                       :overloaded overloaded
                       :duplicate-doc-rejected duplicate-doc-rejected?
                       :interleaved-doc-rejected interleaved-doc-rejected?})))
    (when announce?
      (println "PEER_PROTOCOL_NORMALIZATION_SELF_TEST_PASS"
               "leading_doc=true" "trailing_doc=true"
               "overloaded=true" "malformed_negative=2"))))

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

(when (= ["--self-test"] *command-line-args*)
  (try
    (run-compiler-alpha-self-test! true)
    (run-protocol-normalization-self-test! true)
    (catch Throwable failure
      (fail! "source-form relation self-test failed"
             (merge {:cause (ex-message failure)} (ex-data failure)))))
  (shutdown-agents)
  (System/exit 0))

;; Every ordinary comparison carries the focused collision/cross-use/scope
;; controls; they are not an optional test that can drift away from the gate.
(run-compiler-alpha-self-test! false)
(run-protocol-normalization-self-test! false)

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
                      direct-compiler-alpha-equivalent?
                      (and (not semantic-exact?)
                           (compiler-alpha-equivalent?
                             reference-forms regenerated-forms))
                      reference-events (when-not (or semantic-exact?
                                                     direct-compiler-alpha-equivalent?)
                                         (normalize-protocol-events
                                           reference-forms namespace-name))
                      regenerated-events (when-not (or semantic-exact?
                                                       direct-compiler-alpha-equivalent?)
                                         (normalize-protocol-events
                                           regenerated-forms namespace-name))
                      scaffold-shape?
                      (and (not semantic-exact?)
                           (not direct-compiler-alpha-equivalent?)
                           (pos? (+ (:expanded reference-events)
                                    (:expanded regenerated-events)))
                           (pos? (+ (:legacy reference-events)
                                    (:legacy regenerated-events)))
                           (= (+ (:legacy reference-events)
                                 (:expanded reference-events))
                              (+ (:legacy regenerated-events)
                                 (:expanded regenerated-events))))
                      scaffold-semantic-exact?
                      (and scaffold-shape?
                           (= (encode-form (:events reference-events) :semantic)
                              (encode-form (:events regenerated-events) :semantic)))
                      scaffold-compiler-alpha-equivalent?
                      (and scaffold-shape?
                           (not scaffold-semantic-exact?)
                           (compiler-alpha-equivalent?
                             (:events reference-events)
                             (:events regenerated-events)))
                      scaffold-transition?
                      (or scaffold-semantic-exact?
                          scaffold-compiler-alpha-equivalent?)
                      _runtime-proof
                      (when scaffold-transition?
                        (require-runtime-surface-proof!
                          namespace-name surface-result-root
                          original-surface-root recovered-surface-root))
                      accepted? (or semantic-exact?
                                    direct-compiler-alpha-equivalent?
                                    scaffold-transition?)
                      classification
                      (cond
                        byte-exact? "byte-exact"
                        metadata-exact? "format-only"
                        semantic-exact? "location-metadata-or-format-only"
                        direct-compiler-alpha-equivalent?
                        "compiler-generated-alpha-equivalent"
                        scaffold-compiler-alpha-equivalent?
                        "oracle-proved-protocol-scaffold-with-compiler-alpha"
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
                   :compiler-alpha-equivalent
                   direct-compiler-alpha-equivalent?
                   :accepted accepted?
                   :classification classification
                   :protocol-scaffold-transition scaffold-transition?
                   :protocol-scaffold-compiler-alpha-equivalent
                   scaffold-compiler-alpha-equivalent?})))
            rows)
          rejected (count (remove :accepted comparisons))
          location-deltas (count (filter #(= "location-metadata-or-format-only"
                                             (:classification %))
                                         comparisons))
          scaffold-deltas (count (filter :protocol-scaffold-transition
                                         comparisons))
          scaffold-alpha-deltas
          (count (filter :protocol-scaffold-compiler-alpha-equivalent
                         comparisons))
          compiler-alpha-deltas (count (filter :compiler-alpha-equivalent
                                               comparisons))
          format-deltas (count (filter #(= "format-only" (:classification %))
                                       comparisons))]
      (with-open [writer (io/writer output-file :encoding "UTF-8")]
        (.write writer
                "namespace\tpath\treference_sha256\tregenerated_sha256\treference_forms\tregenerated_forms\tmetadata_exact\tsemantic_exact\tclassification\tprotocol_scaffold_transition\tcompiler_generated_alpha_equivalent\tprotocol_scaffold_compiler_generated_alpha_equivalent\n")
        (doseq [{:keys [namespace path reference-sha regenerated-sha
                        reference-form-count regenerated-form-count
                        metadata-exact semantic-exact classification
                        protocol-scaffold-transition
                        compiler-alpha-equivalent
                        protocol-scaffold-compiler-alpha-equivalent]}
                comparisons]
          (.write writer
                  (str/join "\t"
                            [namespace path reference-sha regenerated-sha
                             reference-form-count regenerated-form-count
                             metadata-exact semantic-exact classification
                             protocol-scaffold-transition
                             compiler-alpha-equivalent
                             protocol-scaffold-compiler-alpha-equivalent]))
          (.write writer "\n")))
      (println "PEER_SOURCE_FORM_RESULT"
               (str "namespaces=" (count comparisons))
               (str "semantic_body_or_structure=" rejected)
               (str "protocol_scaffold=" scaffold-deltas)
               (str "protocol_scaffold_compiler_generated_alpha="
                    scaffold-alpha-deltas)
               (str "compiler_generated_alpha=" compiler-alpha-deltas)
               (str "location_metadata_or_format=" location-deltas)
               (str "format_only=" format-deltas))
      (when (pos? rejected)
        (fail! "recovered Peer contains semantic source-body or unauthorized structure deltas"
               {:count rejected :report output-file})))
    (catch Throwable failure
      (fail! "source-form comparison could not complete"
             (merge {:cause (ex-message failure)} (ex-data failure)))))
  (shutdown-agents))

(require '[clojure.tools.decompiler :as decompiler]
         '[clojure.tools.decompiler.ast :as ast]
         '[clojure.tools.decompiler.compact :as compact]
         '[clojure.tools.decompiler.pprint :as decompiler.pprint]
         '[clojure.tools.decompiler.source :as source]
         '[clojure.tools.decompiler.sugar :as sugar])

(let [namespace-init-name "fixture.protocol__init"
      eager-class-name "fixture.protocol.EagerType"
      later-eager-class-name "fixture.protocol.LaterType"
      target-var 'fixture.protocol/unwrap
      string-instruction
      (fn [instruction-name value]
        {:insn/name instruction-name
         :insn/pool-element
         {:insn/target-value value
          :insn/target-type "java.lang.String"}})
      static-call
      (fn [target-class target-name argument-types return-type]
        {:insn/name "invokestatic"
         :insn/pool-element
         {:insn/target-class target-class
          :insn/target-name target-name
          :insn/target-arg-types argument-types
          :insn/target-ret-type return-type}})
      helper-call
      (static-call namespace-init-name "__init0" [] "void")
      second-helper-call
      (static-call namespace-init-name "__init1" [] "void")
      load-call
      (static-call namespace-init-name "load" [] "void")
      class-for-name-call
      (static-call "clojure.lang.RT" "classForName"
                   ["java.lang.String"] "java.lang.Class")
      rt-var-call
      (static-call "clojure.lang.RT" "var"
                   ["java.lang.String" "java.lang.String"]
                   "clojure.lang.Var")
      wrong-rt-var-call
      (static-call "foreign.lang.RT" "var"
                   ["java.lang.String" "java.lang.String"]
                   "clojure.lang.Var")
      return-instruction {:insn/name "return"}
      conditional-instruction {:insn/name "ifeq"}
      jump-instruction {:insn/name "goto"}
      var-checkcast
      {:insn/name "checkcast"
       :insn/pool-element {:insn/target-value "clojure/lang/Var"}}
      own-static-field-write
      {:insn/name "putstatic"
       :insn/pool-element
       {:insn/target-class eager-class-name
        :insn/target-name "const__0"}}
      unmap-call
      {:insn/name "invokevirtual"
       :insn/pool-element
       {:insn/target-class "clojure.lang.Namespace"
        :insn/target-name "unmap"
        :insn/target-arg-types ["clojure.lang.Symbol"]
        :insn/target-ret-type "clojure.lang.Object"}}
      arbitrary-helper-call
      (static-call "fixture.protocol.Mutator" "run" [] "void")
      unmodeled-ldc
      {:insn/name "ldc_w"
       :insn/pool-element
       {:insn/target-type "java.lang.invoke.ConstantDynamic"
        :insn/target-value :bootstrap-backed}}
      eager-load-bytecode
      [(string-instruction "ldc" eager-class-name)
       class-for-name-call
       return-instruction]
      eager-then-later-load-bytecode
      [(string-instruction "ldc" eager-class-name)
       class-for-name-call
       (string-instruction "ldc_w" later-eager-class-name)
       class-for-name-call
       return-instruction]
      target-var-bytecode
      [(string-instruction "ldc" "fixture.protocol")
       (string-instruction "ldc_w" "unwrap")
       rt-var-call
       var-checkcast
       own-static-field-write
       return-instruction]
      initialized-class
      (fn initialized-class
        ([bytecode]
         (initialized-class eager-class-name bytecode))
        ([class-name bytecode]
         {:class/name class-name
          :class/methods [{:method/name "<clinit>"
                           :method/arg-types []
                           :method/return-type "void"
                           :method/exception-table #{}
                           :method/bytecode (vec bytecode)}]}))
      namespace-class
      (fn [clinit-bytecode helper-bytecode load-bytecode]
        {:class/name namespace-init-name
         :class/methods
         [{:method/name "<clinit>"
           :method/arg-types []
           :method/return-type "void"
           :method/exception-table #{}
           :method/bytecode (vec clinit-bytecode)}
          {:method/name "__init0"
           :method/arg-types []
           :method/return-type "void"
           :method/exception-table #{}
           :method/bytecode (vec helper-bytecode)}
          {:method/name "load"
           :method/arg-types []
           :method/return-type "void"
           :method/exception-table #{}
           :method/bytecode (vec load-bytecode)}]})
      namespace-class-with-second-helper
      (fn [clinit-bytecode first-helper-bytecode second-helper-bytecode
           load-bytecode]
        (update (namespace-class clinit-bytecode
                                 first-helper-bytecode load-bytecode)
                :class/methods conj
                {:method/name "__init1"
                 :method/arg-types []
                 :method/return-type "void"
                 :method/exception-table #{}
                 :method/bytecode (vec second-helper-bytecode)}))
      facts
      (fn [namespace-bytecode eager-bytecode]
        (decompiler/preinterned-var-symbols-before-namespace-load
          namespace-bytecode
          (fn [class-name]
            (when (= eager-class-name class-name) eager-bytecode))))
      facts-with-later
      (fn [namespace-bytecode eager-bytecode later-bytecode]
        (decompiler/preinterned-var-symbols-before-namespace-load
          namespace-bytecode
          (fn [class-name]
            (cond
              (= eager-class-name class-name) eager-bytecode
              (= later-eager-class-name class-name) later-bytecode))))
      positive
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class target-var-bytecode))
      positive-with-proved-later-class
      (facts-with-later
        (namespace-class [helper-call load-call]
                         eager-then-later-load-bytecode [])
        (initialized-class target-var-bytecode)
        (initialized-class later-eager-class-name [return-instruction]))
      later-class-absent
      (facts (namespace-class [helper-call load-call]
                              eager-then-later-load-bytecode [])
             (initialized-class target-var-bytecode))
      absent
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             nil)
      after
      (facts (namespace-class [helper-call load-call]
                              [] eager-load-bytecode)
             (initialized-class target-var-bytecode))
      helper-after-load
      (facts (namespace-class [load-call helper-call]
                              eager-load-bytecode [])
             (initialized-class target-var-bytecode))
      conditional-prefix
      (facts (namespace-class [conditional-instruction helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class target-var-bytecode))
      jump-over-helper-body
      (facts (namespace-class [helper-call load-call]
                              [jump-instruction
                               (string-instruction "ldc" eager-class-name)
                               class-for-name-call
                               return-instruction]
                              [])
             (initialized-class target-var-bytecode))
      early-return-helper-body
      (facts (namespace-class [helper-call load-call]
                              [return-instruction
                               (string-instruction "ldc" eager-class-name)
                               class-for-name-call
                               return-instruction]
                              [])
             (initialized-class target-var-bytecode))
      conditional-target-var
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class
               (into [conditional-instruction]
                     target-var-bytecode)))
      transient-target-var-unmapped
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class
               (into (pop target-var-bytecode)
                     [unmap-call return-instruction])))
      transient-target-var-arbitrary-helper
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class
               (into (pop target-var-bytecode)
                     [arbitrary-helper-call return-instruction])))
      class-target-then-unmap
      (facts (namespace-class [helper-call load-call]
                              (into (pop eager-load-bytecode)
                                    [unmap-call return-instruction])
                              [])
             (initialized-class target-var-bytecode))
      later-helper-arbitrary-mutator
      (facts
        (namespace-class-with-second-helper
          [helper-call second-helper-call load-call]
          eager-load-bytecode
          [arbitrary-helper-call return-instruction]
          [])
        (initialized-class target-var-bytecode))
      class-target-then-unmodeled-ldc
      (facts (namespace-class [helper-call load-call]
                              (into (pop eager-load-bytecode)
                                    [unmodeled-ldc return-instruction])
                              [])
             (initialized-class target-var-bytecode))
      wrong-call-identity
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class
               [(string-instruction "ldc" "fixture.protocol")
                (string-instruction "ldc_w" "unwrap")
                wrong-rt-var-call
                var-checkcast
                own-static-field-write
                return-instruction]))
      wrong-var-identity
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class
               [(string-instruction "ldc" "fixture.other")
                (string-instruction "ldc_w" "unwrap")
                rt-var-call
                var-checkcast
                own-static-field-write
                return-instruction]))
      wrong-argument-order
      (facts (namespace-class [helper-call load-call]
                              eager-load-bytecode [])
             (initialized-class
               [(string-instruction "ldc" "unwrap")
                (string-instruction "ldc_w" "fixture.protocol")
                rt-var-call
                var-checkcast
                own-static-field-write
                return-instruction]))]
  (assert (contains? positive target-var))
  (assert (contains? positive-with-proved-later-class target-var))
  (doseq [[label evidence]
          [[:absent absent]
           [:later-class-absent later-class-absent]
           [:after after]
           [:helper-after-load helper-after-load]
           [:conditional-prefix conditional-prefix]
           [:jump-over-helper-body jump-over-helper-body]
           [:early-return-helper-body early-return-helper-body]
           [:conditional-target-var conditional-target-var]
           [:transient-target-var-unmapped transient-target-var-unmapped]
           [:transient-target-var-arbitrary-helper
            transient-target-var-arbitrary-helper]
           [:class-target-then-unmap class-target-then-unmap]
           [:later-helper-arbitrary-mutator later-helper-arbitrary-mutator]
           [:class-target-then-unmodeled-ldc
            class-target-then-unmodeled-ldc]
           [:wrong-call-identity wrong-call-identity]
           [:wrong-var-identity wrong-var-identity]
           [:wrong-argument-order wrong-argument-order]]]
    (assert (not (contains? evidence target-var))
            (str "pre-load Var evidence escaped exact boundary: " label)))
  (println "decompiler persistent pre-load Var CFG/identity/order controls passed"))

(let [source-ns (symbol (str "macrocompact.late.bound.source." (gensym)))
      target-ns (symbol (str "macrocompact.late.bound.target." (gensym)))
      target-sym (symbol (str target-ns) "uncreated")
      source
      (list 'do
            (list '.setMeta
                  (list 'var 'alias-root)
                  {:audit :late-bound-qualified-var})
            (list '.bindRoot
                  (list 'var 'alias-root)
                  (list 'var target-sym)))
      recovered (compact/macrocompact source)
      bind-root (first
                  (filter #(and (seq? %)
                                (= '.bindRoot (first %)))
                          (tree-seq coll? seq recovered)))]
  (doseq [[label lookalike]
          [[:nested '(identity (var qualified/x))]
           [:unqualified '(var x)]
           [:foreign-head '(foo/var qualified/x)]
           [:missing-argument '(var)]
           [:extra-argument '(var qualified/x :extra)]]]
    (assert (= lookalike
               (compact/late-bound-qualified-var-value lookalike))
            (str "late-bound qualified Var rewrite escaped exact scope: "
                 label)))
  (assert (nil? (find-ns target-ns)))
  (assert (= (list 'clojure.lang.RT/var (str target-ns) "uncreated")
             (nth bind-root 2)))
  (try
    (let [source-namespace (create-ns source-ns)]
      (clojure.core/intern source-namespace 'alias-root)
      (binding [*ns* source-namespace]
        (clojure.core/refer 'clojure.core)
        (eval recovered))
      (let [alias-var (ns-resolve source-namespace 'alias-root)
            target-var (clojure.lang.RT/var (str target-ns) "uncreated")]
        (assert (identical? target-var @alias-var))
        (assert (not (bound? target-var)))
        (assert (= :late-bound-qualified-var (:audit (meta alias-var))))))
    (finally
      (when (find-ns source-ns) (remove-ns source-ns))
      (when (find-ns target-ns) (remove-ns target-ns))))
  (println "decompiler late-bound qualified bind value fixture passed"))

(let [source
      '(do
         (.setMeta (var ordinary-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 10
                    :column 1
                    :arglists (clojure.core/list ['x])})
         (.bindRoot (var ordinary-root)
                    (clojure.core/fn ordinary-root ([x] x)))
         (.setMeta (var macro-generated-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 20
                    :column 1})
         (.bindRoot (var macro-generated-root)
                    (clojure.core/fn macro-generated-root ([x] x)))
         (.setMeta (var incongruent-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 30
                    :column 1
                    :arglists (clojure.core/list ['x 'y])})
         (.bindRoot (var incongruent-root)
                    (clojure.core/fn incongruent-root ([x] x)))
         (.setMeta (var malformed-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 40
                    :column 1
                    :arglists nil})
         (.bindRoot (var malformed-root)
                    (clojure.core/fn malformed-root ([x] x)))
         (.setMeta (var typed-order-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 50
                    :column 1
                    :arglists
                    (clojure.core/list
                      [(.withMeta 'string {:tag 'String})]
                      [(.withMeta 'stream {:tag 'InputStream})
                       (.withMeta 'encoding {:tag 'String})])})
         (.bindRoot (var typed-order-root)
                    (clojure.core/fn typed-order-root
                      ([stream encoding] [:stream stream encoding])
                      ([string] [:string string])))
         (.setMeta (var same-arity-metadata-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 60
                    :column 1
                    :arglists
                    (clojure.core/list
                      [(.withMeta 'advertised {:tag 'String})])})
         (.bindRoot (var same-arity-metadata-root)
                    (clojure.core/fn same-arity-metadata-root
                      ([implemented] implemented)))
         (.setMeta (var documented-root)
                   {:file "macrocompact-metadata-fixture.clj"
                    :line 70
                    :column 1
                    :doc "original Var documentation"
                    :arglists (clojure.core/list ['value])})
         (.bindRoot (var documented-root)
                    (clojure.core/fn documented-root ([value] value))))
      recovered (compact/macrocompact source)
      definitions (->> (tree-seq coll? seq recovered)
                       (filter #(and (seq? %)
                                     (symbol? (first %))
                                     (contains? #{"def" "defn"}
                                                (name (first %))))))
      definition-for (fn [target]
                       (first (filter #(= target (second %)) definitions)))
      ordinary-definition (definition-for 'ordinary-root)
      generated-definition (definition-for 'macro-generated-root)
      incongruent-definition (definition-for 'incongruent-root)
      malformed-definition (definition-for 'malformed-root)
      typed-order-definition (definition-for 'typed-order-root)
      same-arity-metadata-definition
      (definition-for 'same-arity-metadata-root)
      documented-definition (definition-for 'documented-root)
      fixture-ns (symbol (str "macrocompact.metadata.fixture." (gensym)))]
  (assert (= "defn" (name (first ordinary-definition))))
  (assert (= "def" (name (first generated-definition))))
  (assert (= "fn" (name (first (nth generated-definition 2)))))
  (assert (= "def" (name (first incongruent-definition))))
  (assert (= "fn" (name (first (nth incongruent-definition 2)))))
  (assert (= "def" (name (first malformed-definition))))
  (assert (= "fn" (name (first (nth malformed-definition 2)))))
  (assert (= "defn" (name (first typed-order-definition))))
  (assert (= "def" (name (first same-arity-metadata-definition))))
  (assert (= "fn"
             (name (first (nth same-arity-metadata-definition 2)))))
  (assert (= "def" (name (first documented-definition))))
  (assert (= "fn" (name (first (nth documented-definition 2)))))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [ordinary-meta (meta (ns-resolve *ns* 'ordinary-root))
            generated-meta (meta (ns-resolve *ns* 'macro-generated-root))
            incongruent-meta (meta (ns-resolve *ns* 'incongruent-root))
            malformed-meta (meta (ns-resolve *ns* 'malformed-root))
            typed-order-meta (meta (ns-resolve *ns* 'typed-order-root))
            typed-order-arglists (:arglists typed-order-meta)
            same-arity-metadata
            (meta (ns-resolve *ns* 'same-arity-metadata-root))
            same-arity-argument (-> same-arity-metadata :arglists first first)
            documented-meta (meta (ns-resolve *ns* 'documented-root))]
        (assert (contains? ordinary-meta :arglists))
        (assert (not (contains? generated-meta :arglists)))
        (assert (= '([x y]) (:arglists incongruent-meta)))
        (assert (and (contains? malformed-meta :arglists)
                     (nil? (:arglists malformed-meta))))
        (assert (= [[1 false] [2 false]]
                   (mapv (fn [argv]
                           [(count argv) (boolean (some #{'&} argv))])
                         typed-order-arglists)))
        (assert (= 'String (:tag (meta (ffirst typed-order-arglists)))))
        (assert (= 'InputStream
                   (:tag (meta (first (second typed-order-arglists))))))
        (assert (= 'advertised same-arity-argument))
        (assert (= 'String (:tag (meta same-arity-argument))))
        (assert (= "original Var documentation" (:doc documented-meta)))
        (assert (= '([value]) (:arglists documented-meta)))
        (assert (= 9 ((ns-resolve *ns* 'ordinary-root) 9)))
        (assert (= 9 ((ns-resolve *ns* 'macro-generated-root) 9)))
        (assert (= 9 ((ns-resolve *ns* 'incongruent-root) 9)))
        (assert (= 9 ((ns-resolve *ns* 'malformed-root) 9)))
        (assert (= [:string "payload"]
                   ((ns-resolve *ns* 'typed-order-root) "payload")))
        (assert (= [:stream :body "UTF-8"]
                   ((ns-resolve *ns* 'typed-order-root) :body "UTF-8")))
        (assert (= :payload
                   ((ns-resolve *ns* 'same-arity-metadata-root) :payload)))
        (assert (= :payload
                   ((ns-resolve *ns* 'documented-root) :payload)))))
    (finally
      (remove-ns fixture-ns)))
  (println "decompiler setMeta-guided defn/def+fn metadata fixture passed"))

(let [source
      '(do
         (.setMeta (var wrapped-return-root)
                   {:arglists
                    (clojure.core/list
                      (.withMeta ['value] {:tag 'long}))})
         (.bindRoot (var wrapped-return-root)
                    (clojure.core/fn wrapped-return-root
                      (^long [value] (long value))))
         (.setMeta (var munged-parameters-root)
                   {:arglists
                    (clojure.core/list ['bean-class 'ready?])})
         (.bindRoot (var munged-parameters-root)
                    (clojure.core/fn munged-parameters-root
                      ([bean_class ready_QMARK_]
                       [bean_class ready_QMARK_])))
         (.setMeta (var destructured-parameter-root)
                   {:arglists
                    (clojure.core/list [['left 'right]])})
         (.bindRoot (var destructured-parameter-root)
                    (clojure.core/fn destructured-parameter-root
                      ([p__12345]
                       (let [vec__12346 p__12345
                             left (nth vec__12346 0 nil)
                             right (nth vec__12346 1 nil)]
                         [left right])))))
      recovered (compact/macrocompact source)
      definitions
      (->> (tree-seq coll? seq recovered)
           (filter #(and (seq? %)
                         (symbol? (first %))
                         (= "defn" (name (first %)))))
           (map (juxt second identity))
           (into {}))]
  (assert (contains? definitions 'wrapped-return-root))
  (assert (contains? definitions 'munged-parameters-root))
  (assert (contains? definitions 'destructured-parameter-root))
  (println "decompiler Clojure 1.11/1.12 arglist metadata defn recovery passed"))

(let [source
      '(do
         (.setMeta (var alpha/x)
                   {:file "macrocompact-qualified-var-fixture.clj"
                    :line 10
                    :column 1})
         (.setMeta (var beta/x)
                   {:file "macrocompact-qualified-var-fixture.clj"
                    :line 20
                    :column 1
                    :arglists (clojure.core/list ['value])})
         (.bindRoot (var alpha/x)
                    (clojure.core/fn x ([value] value)))
         (.bindRoot (var beta/x)
                    (clojure.core/fn x ([value] value))))
      recovered (compact/macrocompact source)
      preserved-ops
      (->> (tree-seq coll? seq recovered)
           (keep (fn [form]
                   (when (and (seq? form)
                              (symbol? (first form))
                              (contains? #{".setMeta" ".bindRoot"}
                                         (name (first form))))
                     (let [var-reference (second form)]
                       [(name (first form))
                        (second var-reference)
                        (nth var-reference 2 nil)]))))
           vec)]
  (assert (= [[".setMeta" "alpha" "x"]
              [".setMeta" "beta" "x"]
              [".bindRoot" "alpha" "x"]
              [".bindRoot" "beta" "x"]]
             preserved-ops))
  (println "decompiler qualified same-name unsafe-order Var isolation passed"))

(let [source
      '(do
         (.setMeta (var alpha/x)
                   {:file "macrocompact-qualified-order-fixture.clj"
                    :line 10
                    :column 1
                    :audit :alpha})
         (.bindRoot (var beta/x)
                    (clojure.core/fn x ([value] value))))
      recovered (compact/macrocompact source)
      preserved-ops
      (->> (tree-seq coll? seq recovered)
           (keep (fn [form]
                   (when (and (seq? form)
                              (symbol? (first form))
                              (contains? #{".setMeta" ".bindRoot"}
                                         (name (first form))))
                     (let [var-reference (second form)]
                       [(name (first form))
                        (second var-reference)
                        (nth var-reference 2 nil)]))))
           vec)]
  (assert (= [[".setMeta" "alpha" "x"]
              [".bindRoot" "beta" "x"]]
             preserved-ops))
  (println "decompiler qualified same-name definition-order isolation passed"))

(let [fixture-ns (symbol (str "macrocompact.qualified.current." (gensym)))
      qualified-root (symbol (str fixture-ns) "current-root")
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            :clojure.tools.decompiler.compact/consumed-set-meta
            (list 'do
                  (list '.setMeta
                        (list 'var qualified-root)
                        {:file "macrocompact-qualified-current-fixture.clj"
                         :line 10
                         :column 1
                         :doc "qualified current namespace metadata"
                         :audit :pure
                         :arglists '(clojure.core/list [(quote value)])})
                  nil
                  false
                  (list '.bindRoot
                        (list 'var 'current-root)
                        '(clojure.core/fn current-root ([value] value)))))
      recovered (compact/macrocompact source)
      recovered-nodes (vec (tree-seq coll? seq recovered))
      node-index (fn [predicate]
                   (first (keep-indexed (fn [index node]
                                          (when (predicate node) index))
                                        recovered-nodes)))
      nil-index (node-index nil?)
      false-index (node-index #(identical? false %))
      definition-index
      (node-index #(and (seq? %)
                        (symbol? (first %))
                        (contains? #{"def" "defn"} (name (first %)))
                        (= 'current-root (second %))))
      resets (->> (tree-seq coll? seq recovered)
                  (filter #(and (seq? %)
                                (symbol? (first %))
                                (= "reset-meta!" (name (first %)))))
                  vec)
      ]
  (assert (some #{:clojure.tools.decompiler.compact/consumed-set-meta}
                (tree-seq coll? seq recovered)))
  (assert (= 1 (count resets)))
  (assert (= '(var current-root) (second (first resets))))
  (assert (and (< nil-index false-index)
               (< false-index definition-index)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* 'current-root)
            root-meta (meta root)]
        (assert (= :payload (root :payload)))
        (assert (= :pure (:audit root-meta)))
        (assert (= "qualified current namespace metadata" (:doc root-meta)))
        (assert (= '([value]) (:arglists root-meta)))))
    (finally
      (remove-ns fixture-ns)))
  (println "decompiler nested qualified-current identity/pure compaction/sentinel isolation passed"))

(let [foreign-set
      '(foreign.example/metadata-order-set* :authored-var :authored-meta)
      foreign-bind
      '(foreign.example/metadata-order-bind* :authored-var :authored-value)
      foreign-aware
      '(foreign.example/metadata-aware-bind-root*
         :authored-var :authored-value :authored-meta)
      exact-public-set
      '(clojure.tools.decompiler.compact/metadata-order-set*
         :authored-var :authored-meta)
      exact-public-bind
      '(clojure.tools.decompiler.compact/metadata-order-bind*
         :authored-var :authored-value)
      exact-public-aware
      '(clojure.tools.decompiler.compact/metadata-aware-bind-root*
         :authored-var :authored-value :authored-meta)
      recovered (compact/macrocompact
                  (list 'do foreign-set foreign-bind foreign-aware
                        exact-public-set exact-public-bind
                        exact-public-aware))]
  (assert (some #{foreign-set} (tree-seq coll? seq recovered)))
  (assert (some #{foreign-bind} (tree-seq coll? seq recovered)))
  (assert (some #{foreign-aware} (tree-seq coll? seq recovered)))
  (assert (some #{exact-public-set} (tree-seq coll? seq recovered)))
  (assert (some #{exact-public-bind} (tree-seq coll? seq recovered)))
  (assert (some #{exact-public-aware} (tree-seq coll? seq recovered)))
  (println "decompiler forged symbolic internal-marker isolation passed"))

(let [target-ns (symbol (str "macrocompact.noncurrent.target." (gensym)))
      evaluation-ns
      (symbol (str "macrocompact.noncurrent.evaluation." (gensym)))
      qualified-root (symbol (str target-ns) "qualified-root")
      source
      (list 'do
            (list '.setMeta
                  (list 'var qualified-root)
                  {:audit :noncurrent
                   :arglists '(clojure.core/list [(quote value)])})
            (list '.bindRoot
                  (list 'var qualified-root)
                  '(fn* qualified-root ([value] value))))
      recovered (compact/macrocompact source)
      low-level-targets
      (->> (tree-seq coll? seq recovered)
           (keep (fn [form]
                   (when (and (seq? form)
                              (symbol? (first form))
                              (contains? #{".setMeta" ".bindRoot"}
                                         (name (first form))))
                     (second form))))
           vec)]
  (assert (= [(list 'clojure.lang.RT/var (str target-ns)
                    "qualified-root")
              (list 'clojure.lang.RT/var (str target-ns)
                    "qualified-root")]
             low-level-targets))
  (try
    (binding [*ns* (create-ns evaluation-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve (find-ns target-ns) 'qualified-root)]
        (assert (= :payload (root :payload)))
        (assert (= :noncurrent (:audit (meta root))))
        (assert (nil? (ns-resolve *ns* 'qualified-root)))))
    (finally
      (when (find-ns target-ns) (remove-ns target-ns))
      (remove-ns evaluation-ns)))
  (println "decompiler qualified same-Var non-current namespace isolation passed"))

(def adversarial-purity-events (atom []))

(let [fixture-ns (symbol (str "macrocompact.adversarial.purity." (gensym)))
      qualified-root (symbol (str fixture-ns) "adversarial-root")
      runtime-var (list 'clojure.lang.RT/var
                        (str fixture-ns) "adversarial-root")
      evil-ns (create-ns 'evil.example)
      _ (intern evil-ns 'list
                (fn [value]
                  (swap! adversarial-purity-events
                         conj [:foreign-list
                               (bound? (clojure.lang.RT/var
                                         (str fixture-ns)
                                         "adversarial-root"))])
                  value))
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (list 'let
                  ['EvilLocal
                   '(do
                      (clojure.core/swap!
                        user/adversarial-purity-events
                        clojure.core/conj
                        [:local-evaluation false])
                      :local-value)]
                  (list 'do
                        (list '.setMeta
                              (list 'var qualified-root)
                              {:foreign
                               (list 'evil.example/list :foreign-value)
                               :local 'EvilLocal})
                        (list '.bindRoot
                              (list 'var 'adversarial-root)
                              '(fn* adversarial-root
                                 ([value] value))))))
      recovered (compact/macrocompact source)
      low-level-op-names
      (->> (tree-seq coll? seq recovered)
           (keep (fn [form]
                   (when (and (seq? form)
                              (symbol? (first form))
                              (contains? #{".setMeta" ".bindRoot"}
                                         (name (first form))))
                     (name (first form)))))
           vec)]
  (reset! adversarial-purity-events [])
  (assert (false? (compact/pure-metadata-form?
                    {:foreign '(evil.example/list :value)})))
  (assert (false? (compact/pure-metadata-form? {:local 'EvilLocal})))
  (assert (nil? (compact/in-ns-symbol
                  '(foreign.example/in-ns (quote wrong.namespace)))))
  (assert (= [".setMeta" ".bindRoot"] low-level-op-names))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* 'adversarial-root)]
        (assert (= :payload (root :payload)))
        (assert (= :foreign-value (:foreign (meta root))))
        (assert (= :local-value (:local (meta root))))))
    (finally
      (remove-ns fixture-ns)
      (remove-ns 'evil.example)))
  (assert (= 1 (count (filter #(= :local-evaluation (first %))
                              @adversarial-purity-events))))
  (assert (= [[:foreign-list false]]
             (vec (filter #(= :foreign-list (first %))
                          @adversarial-purity-events))))
  (println "decompiler adversarial namespaced-call/local-symbol purity rejection passed"))

(def metadata-order-events (atom []))

(let [fixture-ns (symbol (str "macrocompact.metadata.order." (gensym)))
      qualified-root (symbol (str fixture-ns) "ordered-root")
      runtime-var (list 'clojure.lang.RT/var (str fixture-ns) "ordered-root")
      metadata-form
      {:file "macrocompact-metadata-order-fixture.clj"
       :line 10
       :column 1
       :doc "order-sensitive metadata"
       :audit
       (list 'do
             (list 'clojure.core/swap!
                   'user/metadata-order-events
                   'clojure.core/conj
                   (list 'clojure.core/vector
                         :metadata-evaluation
                         (list 'clojure.core/bound? runtime-var)))
             :metadata-value)
       :arglists '(clojure.core/list [(quote value)])}
      intervening-form
      (list 'clojure.core/swap!
            'user/metadata-order-events
            'clojure.core/conj
            (list 'clojure.core/vector
                  :intervening-read
                  (list :audit (list 'clojure.core/meta runtime-var))
                  (list 'clojure.core/bound? runtime-var)))
      after-form
      (list 'clojure.core/swap!
            'user/metadata-order-events
            'clojure.core/conj
            (list 'clojure.core/vector
                  :after-bind
                  (list 'clojure.core/bound? runtime-var)))
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (list '.setMeta (list 'var qualified-root) metadata-form)
            intervening-form
            (list '.bindRoot
                  (list 'var 'ordered-root)
                  '(clojure.core/fn ordered-root ([value] value)))
            after-form)
      recovered (compact/macrocompact source)
      preserved-op-names
      (->> (tree-seq coll? seq recovered)
           (keep (fn [form]
                   (when (and (seq? form)
                              (symbol? (first form))
                              (contains? #{".setMeta" ".bindRoot"}
                                         (name (first form))))
                     (name (first form)))))
           vec)]
  (reset! metadata-order-events [])
  (assert (= [".setMeta" ".bindRoot"] preserved-op-names))
  (assert (not-any? #(and (seq? %)
                          (symbol? (first %))
                          (contains? #{"def" "defn"}
                                     (name (first %)))
                          (= 'ordered-root (second %)))
                    (tree-seq coll? seq recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* 'ordered-root)]
        (assert (= :payload (root :payload)))
        (assert (= :metadata-value (:audit (meta root))))))
    (finally
      (remove-ns fixture-ns)))
  (assert (= [[:metadata-evaluation false]
              [:intervening-read :metadata-value false]
              [:after-bind true]]
             @metadata-order-events))
  (assert (= 1 (count (filter #(= :metadata-evaluation (first %))
                              @metadata-order-events))))
  (println "decompiler impure metadata order/intervening visibility/single evaluation passed"))

(def scalar-order-events (atom []))

(let [fixture-ns (symbol (str "macrocompact.scalar.order." (gensym)))
      qualified-root (symbol (str fixture-ns) "scalar-root")
      runtime-var (list 'clojure.lang.RT/var (str fixture-ns) "scalar-root")
      metadata-form
      {:audit
       (list 'do
             (list 'clojure.core/swap!
                   'user/scalar-order-events
                   'clojure.core/conj
                   (list 'clojure.core/vector
                         :metadata-evaluation
                         (list 'clojure.core/bound? runtime-var)))
             :scalar-metadata)}
      intervening-form
      (list 'clojure.core/swap!
            'user/scalar-order-events
            'clojure.core/conj
            (list 'clojure.core/vector
                  :intervening-read
                  (list :audit (list 'clojure.core/meta runtime-var))
                  (list 'clojure.core/bound? runtime-var)))
      after-form
      (list 'clojure.core/swap!
            'user/scalar-order-events
            'clojure.core/conj
            (list 'clojure.core/vector
                  :after-bind
                  (list 'clojure.core/bound? runtime-var)
                  (list 'clojure.core/deref runtime-var)))
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (list '.setMeta (list 'var qualified-root) metadata-form)
            intervening-form
            (list '.bindRoot (list 'var 'scalar-root) 41)
            after-form)
      recovered (compact/macrocompact source)
      low-level-ops
      (->> (tree-seq coll? seq recovered)
           (keep (fn [form]
                   (when (and (seq? form)
                              (symbol? (first form))
                              (contains? #{".setMeta" ".bindRoot"}
                                         (name (first form))))
                     (name (first form)))))
           vec)]
  (reset! scalar-order-events [])
  (assert (= [".setMeta" ".bindRoot"] low-level-ops))
  (assert (not-any? #(= '(var scalar-root) %)
                    (tree-seq coll? seq recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* 'scalar-root)]
        (assert (= 41 @root))
        (assert (= :scalar-metadata (:audit (meta root))))
        (assert (= 'scalar-root (:name (meta root))))
        (assert (= fixture-ns (ns-name (:ns (meta root)))))))
    (finally
      (remove-ns fixture-ns)))
  (assert (= [[:metadata-evaluation false]
              [:intervening-read :scalar-metadata false]
              [:after-bind true 41]]
             @scalar-order-events))
  (println "decompiler scalar root RT.var order/metadata/root preservation passed"))

(let [fixture-ns (symbol (str "macrocompact.dynamic.function." (gensym)))
      qualified-root (symbol (str fixture-ns) "*function-root*")
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (list '.setDynamic (list 'var qualified-root) true)
            (list '.setMeta
                  (list '.setDynamic
                        (list 'var qualified-root) true)
                  {:dynamic true
                   :audit :dynamic-function
                   :arglists '(clojure.core/list [(quote value)])})
            (list '.bindRoot
                  (list '.setDynamic
                        (list 'var '*function-root*) true)
                  '(fn* function-root ([value] value))))
      recovered (compact/macrocompact source)
      definition
      (first (filter #(and (seq? %)
                           (symbol? (first %))
                           (= "def" (name (first %)))
                           (= '*function-root* (second %)))
                     (tree-seq coll? seq recovered)))]
  (assert (= true (:dynamic (meta (second definition)))))
  (assert (not-any? #(and (seq? %)
                          (symbol? (first %))
                          (contains? #{".setDynamic" ".setMeta" ".bindRoot"}
                                     (name (first %))))
                    (tree-seq coll? seq recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* '*function-root*)]
        (assert (= :payload (root :payload)))
        (assert (= true (:dynamic (meta root))))
        (assert (= :dynamic-function (:audit (meta root))))
        (assert (= '([value]) (:arglists (meta root))))))
    (finally
      (remove-ns fixture-ns)))
  (println "decompiler unbound dynamic function triple recovery passed"))

(doseq [[fixture-label initially-bound? expected-old expected-new]
        [[:unbound false nil 7]
         [:bound true :old :new]]]
  (let [fixture-ns
        (symbol (str "macrocompact.dynamic.scalar."
                     (name fixture-label) "." (gensym)))
        local-root (symbol (str (name fixture-label) "-root"))
        qualified-root (symbol (str fixture-ns) (name local-root))
        prefix (cond-> [(list 'clojure.core/in-ns
                              (list 'quote fixture-ns))]
                 initially-bound?
                 (conj (list 'def local-root expected-old)))
        source
        (list* 'do
               (concat prefix
                       [(list '.setDynamic (list 'var qualified-root) true)
                        (list '.setMeta
                              (list '.setDynamic
                                    (list 'var qualified-root) true)
                              {:dynamic true :audit fixture-label})
                        (list '.bindRoot
                              (list '.setDynamic
                                    (list 'var local-root) true)
                              expected-new)]))
        recovered (compact/macrocompact source)]
    (assert (not-any? #(and (seq? %)
                            (symbol? (first %))
                            (contains? #{".setDynamic" ".setMeta" ".bindRoot"}
                                       (name (first %))))
                      (tree-seq coll? seq recovered)))
    (assert (not-any?
              #(or (identical? compact/metadata-aware-bind-root-marker-tag %)
                   (identical?
                     compact/metadata-aware-dynamic-bind-root-marker-tag %)
                   (identical? compact/metadata-order-dynamic-marker-tag %)
                   (identical? compact/metadata-order-set-marker-tag %)
                   (identical? compact/metadata-order-bind-marker-tag %))
              (tree-seq coll? seq recovered)))
    (try
      (binding [*ns* (create-ns fixture-ns)]
        (clojure.core/refer 'clojure.core)
        (eval recovered)
        (let [root (ns-resolve *ns* local-root)]
          (assert (= expected-new @root))
          (assert (= true (:dynamic (meta root))))
          (assert (= fixture-label (:audit (meta root))))
          (assert (= local-root (:name (meta root))))
          (assert (= fixture-ns (ns-name (:ns (meta root)))))))
      (finally
        (remove-ns fixture-ns)))
    (println "decompiler dynamic scalar triple recovery passed" fixture-label)))

(def unsafe-dynamic-events (atom []))

(let [fixture-ns (symbol (str "macrocompact.dynamic.unsafe." (gensym)))
      local-root '*cancel*
      qualified-root (symbol (str fixture-ns) (name local-root))
      runtime-var (list 'clojure.lang.RT/var
                        (str fixture-ns) (name local-root))
      dynamic-target
      (fn [var-form] (list '.setDynamic var-form true))
      metadata-form
      {:dynamic true
       :audit
       (list 'do
             (list 'clojure.core/swap!
                   'user/unsafe-dynamic-events
                   'clojure.core/conj
                   (list 'clojure.core/vector
                         :metadata-evaluation
                         (list 'clojure.core/bound? runtime-var)))
             :unsafe-dynamic)}
      intervening-form
      (list 'clojure.core/swap!
            'user/unsafe-dynamic-events
            'clojure.core/conj
            (list 'clojure.core/vector
                  :intervening-read
                  (list :audit (list 'clojure.core/meta runtime-var))
                  (list 'clojure.core/bound? runtime-var)))
      root-value
      (list 'do
            (list 'clojure.core/swap!
                  'user/unsafe-dynamic-events
                  'clojure.core/conj
                  (list 'clojure.core/vector
                        :root-evaluation
                        (list 'clojure.core/bound? runtime-var)))
            (list 'clojure.core/atom nil))
      after-form
      (list 'clojure.core/swap!
            'user/unsafe-dynamic-events
            'clojure.core/conj
            (list 'clojure.core/vector
                  :after-bind
                  (list 'clojure.core/bound? runtime-var)))
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (dynamic-target (list 'var qualified-root))
            (list '.setMeta
                  (dynamic-target (list 'var qualified-root))
                  metadata-form)
            intervening-form
            (list '.bindRoot
                  (dynamic-target (list 'var local-root))
                  root-value)
            after-form)
      recovered (compact/macrocompact source)
      top-level-op-names
      (->> (rest recovered)
           (keep (fn [form]
                   (when (and (seq? form) (symbol? (first form)))
                     (let [head (name (first form))]
                       (when (contains? #{".setDynamic" ".setMeta"
                                          ".bindRoot"} head)
                         head)))))
           vec)
      dynamic-call-count
      (count (filter #(and (seq? %)
                           (symbol? (first %))
                           (= ".setDynamic" (name (first %))))
                     (tree-seq coll? seq recovered)))]
  (reset! unsafe-dynamic-events [])
  (assert (= [".setDynamic" ".setMeta" ".bindRoot"]
             top-level-op-names))
  (assert (= 3 dynamic-call-count))
  (assert (not-any? #(and (seq? %)
                          (contains? #{'var 'clojure.core/var} (first %)))
                    (tree-seq coll? seq recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* local-root)
            value @root]
        (assert (instance? clojure.lang.Atom value))
        (assert (nil? @value))
        (assert (.isDynamic root))
        (assert (= :unsafe-dynamic (:audit (meta root))))))
    (finally
      (remove-ns fixture-ns)))
  (assert (= [[:metadata-evaluation false]
              [:intervening-read :unsafe-dynamic false]
              [:root-evaluation false]
              [:after-bind true]]
             @unsafe-dynamic-events))
  (assert (= 1 (count (filter #(= :root-evaluation (first %))
                              @unsafe-dynamic-events))))
  (println "decompiler unsafe nested-target dynamic order/RT.var/root passed"))

(let [fixture-ns (symbol (str "macrocompact.dynamic.unbound." (gensym)))
      local-root '*retry*
      qualified-root (symbol (str fixture-ns) (name local-root))
      dynamic-target
      (fn [var-form] (list '.setDynamic var-form true))
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (dynamic-target (list 'var qualified-root))
            (list '.setMeta
                  (dynamic-target (list 'var local-root))
                  {:dynamic true :audit :unbound-no-root}))
      recovered (compact/macrocompact source)
      dynamic-call-count
      (count (filter #(and (seq? %)
                           (symbol? (first %))
                           (= ".setDynamic" (name (first %))))
                     (tree-seq coll? seq recovered)))]
  (assert (= 2 dynamic-call-count))
  (assert (not-any? #(and (seq? %)
                          (contains? #{'var 'clojure.core/var} (first %)))
                    (tree-seq coll? seq recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval recovered)
      (let [root (ns-resolve *ns* local-root)]
        (assert (not (bound? root)))
        (assert (.isDynamic root))
        (assert (= :unbound-no-root (:audit (meta root))))))
    (finally
      (remove-ns fixture-ns)))
  (println "decompiler unbound nested-target dynamic metadata-without-root passed"))

(def literal-nil-tail-evaluations (atom 0))

(let [fixture-ns (symbol (str "macrocompact.literal.nil." (gensym)))
      source
      '(do
         (def before-literal-nil :before)
         nil
         (def after-literal-nil :after)
         (clojure.core/swap!
           user/literal-nil-tail-evaluations
           clojure.core/inc))
      recovered (compact/macrocompact source)]
  (reset! literal-nil-tail-evaluations 0)
  (assert (some nil? (rest recovered)))
  (assert (some #(and (seq? %)
                      (= "def" (some-> (first %) name))
                      (= 'after-literal-nil (second %)))
                (rest recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (assert (= 1 (eval recovered)))
      (assert (= :before @(ns-resolve *ns* 'before-literal-nil)))
      (assert (= :after @(ns-resolve *ns* 'after-literal-nil))))
    (finally
      (remove-ns fixture-ns)))
  (assert (= 1 @literal-nil-tail-evaluations))
  (println "decompiler literal-nil do tail retention/execution passed"))

(def literal-false-tail-evaluations (atom 0))

(let [fixture-ns (symbol (str "macrocompact.literal.false." (gensym)))
      source
      '(do
         (def before-literal-false :before)
         false
         (def after-literal-false :after)
         (clojure.core/swap!
           user/literal-false-tail-evaluations
           clojure.core/inc))
      recovered (compact/macrocompact source)]
  (reset! literal-false-tail-evaluations 0)
  (assert (some #(identical? false %) (rest recovered)))
  (assert (some #(and (seq? %)
                      (= "def" (some-> (first %) name))
                      (= 'after-literal-false (second %)))
                (rest recovered)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (assert (= 1 (eval recovered)))
      (assert (= :before @(ns-resolve *ns* 'before-literal-false)))
      (assert (= :after @(ns-resolve *ns* 'after-literal-false))))
    (finally
      (remove-ns fixture-ns)))
  (assert (= 1 @literal-false-tail-evaluations))
  (println "decompiler literal-false do tail retention/execution passed"))

(let [source
      '(do
         (clojure.core/in-ns 'first.example)
         first.example/visible-here
         (clojure.core/in-ns 'second.example)
         first.example/must-stay-qualified
         second.example/visible-here)
      recovered (decompiler.pprint/elide-ns source)]
  (assert (= '(do
                (clojure.core/in-ns 'first.example)
                visible-here
                (clojure.core/in-ns 'second.example)
                first.example/must-stay-qualified
                visible-here)
             recovered))
  (println "decompiler in-ns transition qualification passed"))

(let [target {:op :local :name "target"}
      argument {:op :local :name "argument"}
      reflector-call
      {:op :invoke-static
       :target "clojure.lang.Reflector"
       :method "invokeInstanceMethod"
       :arg-types ["java.lang.Object" "java.lang.String" "java.lang.Object[]"]
       :args [target
              {:op :const :val "method"}
              {:op :array :!items (atom [argument])}]}
      recovered (sugar/ast->sugared-ast reflector-call)]
  (assert (= :invoke-instance (:op recovered)))
  (assert (= [argument] (:args recovered)))
  (assert (= ["java.lang.Object"] (:arg-types recovered)))
  (assert (= [argument]
             (sugar/restore-primitive-casts [argument] nil)))
  (println "decompiler fixed-point argument preservation passed"))

(let [array-arg {:op :array
                 :!items (atom [{:op :const :val :a}
                                {:op :const :val :b}])}
      ordinary-call
      (sugar/ast->sugared-ast*
        {:op :invoke-instance
         :target {:op :local :name "f"}
         :target-class "clojure.lang.IFn"
         :method "invoke"
         :arg-types ["java.lang.Object"]
         :args [array-arg]})
      variadic-call
      (sugar/ast->sugared-ast*
        {:op :invoke-instance
         :target {:op :local :name "f"}
         :target-class "clojure.lang.IFn"
         :method "invoke"
         :arg-types (conj (vec (repeat 20 "java.lang.Object"))
                          "java.lang.Object[]")
         :args (conj (vec (repeat 20 {:op :const :val :fixed}))
                     array-arg)})]
  (assert (= 1 (count (:args ordinary-call))))
  (assert (= :array (:op (first (:args ordinary-call)))))
  (assert (= 22 (count (:args variadic-call))))
  (assert (= [:a :b] (mapv :val (take-last 2 (:args variadic-call)))))
  (println "decompiler IFn array/variadic discrimination passed"))

(let [class-name "fixture$f__42"
      field (fn [name flags]
              {:field/name name :field/type "java.lang.Object"
               :field/flags flags})
      field-insn (fn [op target-class name]
                   {:insn/name op
                    :insn/pool-element
                    {:insn/target-class target-class
                     :insn/target-name name
                     :insn/target-type "java.lang.Object"}})
      captured-bc
      {:class/name class-name
       :class/fields [(field "f" #{:final}) (field "ch" #{:final})]
       :class/methods
       [{:method/name "<init>"
         :method/bytecode [(field-insn "putfield" class-name "f")
                           (field-insn "putfield" class-name "ch")]}
        {:method/name "invoke"
         :method/bytecode [(field-insn "getfield" class-name "f")]}]}
      alter-method
      (fn [bc method-name f]
        (update bc :class/methods
                (fn [methods]
                  (mapv #(if (= method-name (:method/name %)) (f %) %) methods))))]
  (assert (= "f" (ast/extract-fn-name class-name)))
  (assert (ast/derived-fn-name-is-used-capture? captured-bc "f"))
  (assert (nil? (ast/recover-fn-name captured-bc nil)))
  ;; An explicit name supplied by letfn/source evidence remains authoritative.
  (assert (= "f" (ast/recover-fn-name captured-bc "f")))
  ;; Genuine named recursion has no same-named captured instance field.
  (assert (= "f"
             (ast/recover-fn-name
               (assoc captured-bc :class/fields [(field "ch" #{:final})])
               nil)))
  ;; An unrelated capture does not invalidate the class-derived fn name.
  (assert (= "f"
             (ast/recover-fn-name
               (-> captured-bc
                   (assoc :class/fields [(field "outer" #{:final})])
                   (alter-method "<init>"
                                 #(assoc % :method/bytecode
                                           [(field-insn "putfield" class-name
                                                        "outer")]))
                   (alter-method "invoke"
                                 #(assoc % :method/bytecode
                                           [(field-insn "getfield" class-name
                                                        "outer")])))
               nil)))
  ;; A basename/field coincidence is insufficient without both constructor
  ;; capture and invocation-body use.
  (assert (= "f"
             (ast/recover-fn-name
               (alter-method captured-bc "invoke"
                             #(assoc % :method/bytecode []))
               nil)))
  (assert (= "f"
             (ast/recover-fn-name
               (alter-method captured-bc "<init>"
                             #(assoc % :method/bytecode []))
               nil)))
  (assert (= "f"
             (ast/recover-fn-name
               (assoc-in captured-bc [:class/fields 0 :field/flags]
                         #{:static})
               nil)))
  (assert (= "f"
             (ast/recover-fn-name
               (alter-method
                 captured-bc "invoke"
                 #(assoc % :method/bytecode
                           [(field-insn "getfield" "fixture$Other" "f")]))
               nil)))
  (assert (= '(fn* ([] :ok))
             (source/ast->clj
               {:op :fn :name nil
                :fn-methods [{:op :fn-method :args []
                              :body {:op :const :val :ok}}]})))
  (let [recovered-fn-form
        (source/ast->clj
          {:op :fn
           :name (ast/recover-fn-name captured-bc nil)
           :fn-methods
           [{:op :fn-method
             :args []
             :body {:op :invoke
                    :fn {:op :local :name "f"}
                    :args []}}]})
        result
        (eval
          `(let [calls# (atom 0)
                 result#
                 ((fn [~'f]
                    (let [~'f ~recovered-fn-form]
                      (~'f)))
                  (fn []
                    (swap! calls# inc)
                    :captured-outer-result))]
             [result# @calls#]))]
    (assert (= '(fn* ([] (f))) recovered-fn-form))
    (assert (= [:captured-outer-result 1] result)))
  (println "decompiler captured-field/class-derived fn-name discrimination passed"))

(let [method
      {:op :method
       :name "get"
       :args [{:name "this" :type "generated.Reify"}
              {:name "index" :type "int"}]
       :body {:op :invoke-instance
              :method "get"
              :target {:op :local
                       :name "this"
                       :captured-field "outer"}
              :args [{:op :local :name "index"}]}}
      recovered (ast/disambiguate-reify-receiver method)]
  (assert (= "this__reify" (get-in recovered [:args 0 :name])))
  (assert (nil? (get-in recovered [:args 0 :type])))
  (assert (= "this" (get-in recovered [:body :target :name])))
  (println "decompiler nested-reify receiver disambiguation passed"))

(assert (ast/current-receiver? {:op :local :name "this" :this? true}))
(assert (not (ast/current-receiver?
               {:op :local
                :name "this"
                :this? true
                :captured-field "outer"})))
(assert (not (ast/current-receiver? {:op :local :name "other"})))
(assert (= "Example"
           (:cast (ast/field-instance {:op :local :name "other"}
                                      "example.Example"
                                      "example.Example"))))
(assert (= "example.Other"
           (:cast (ast/field-instance {:op :local
                                       :name "other"
                                       :cast "example.Other"}
                                      "example.Other"
                                      "example.Example"))))
(println "decompiler same-class receiver discrimination passed")

(let [exception {:op :local :name "failure"}
      result (ast/process-insn {:stack [exception]
                                :statements []
                                :reachable #{0}
                                :pc 0
                                :ast {:op :new
                                      :class "clojure.lang.AFunction"}}
                               {:insn/name "athrow"})]
  (assert (empty? (:stack result)))
  (assert (= {:op :throw :ex exception} (last (:statements result))))
  (assert (= {:op :throw :ex exception} (get-in result [:ast :ret])))
  (assert (not= "clojure.lang.AFunction" (get-in result [:ast :class])))
  (assert (= {} (:ast (merge {:ast {:op :stale}}
                             ast/initial-local-ctx))))
  (println "decompiler terminal ATHROW method finalization passed"))

(let [throw-expr {:op :throw :ex {:op :local :name "failure"}}
      nonreturning {:ast {}
                    :statements [throw-expr]
                    :stack []
                    :reachable #{0}
                    :insns [{:insn/name "athrow" :insn/label 0}
                            {:insn/name "athrow" :insn/label 1}]}
      finalized (ast/finalize-nonreturning-method nonreturning)
      returning (assoc nonreturning
                       :reachable #{0}
                       :insns [{:insn/name "areturn" :insn/label 0}])]
  (assert (= {:op :do
              :statements [throw-expr]
              :ret ast/nil-expr}
             (:ast finalized)))
  (assert (= returning (ast/finalize-nonreturning-method returning)))
  (assert (= {:ast {} :statements [] :stack []
              :reachable #{0}
              :insns [{:insn/name "athrow" :insn/label 0}]}
             (ast/finalize-nonreturning-method
               {:ast {} :statements [] :stack []
                :reachable #{0}
                :insns [{:insn/name "athrow" :insn/label 0}]})))
  (println "decompiler non-returning method finalization passed"))

(let [visited (atom [])
      recur-expr {:op :recur :args []}
      result-expr {:op :const :val :done}
      branch-processor
      (fn [branch]
        (swap! visited conj (:pc branch))
        (case (:pc branch)
          10 (assoc branch
                    :stack [recur-expr]
                    :statements []
                    :ast {}
                    :recur? true)
          30 (do
               (assert ((:terminate? branch) {:pc 40}))
               (assoc branch
                      :stack [result-expr]
                      :statements []
                      :ast {}))))
      result (with-redefs [ast/process-insns branch-processor]
               (ast/process-if {:stack [] :statements []}
                               {:op :local :name "test"}
                               [10 20]
                               [30 40 true]))]
  (assert (= [10 30] @visited))
  (assert (= 40 (:pc result)))
  (assert (:recur? result))
  (assert (= recur-expr (get-in result [:stack 0 :then :ret])))
  (assert (= result-expr (get-in result [:stack 0 :else :ret])))
  (println "decompiler implicit function-loop else boundary passed"))

(let [visited (atom [])
      recur-expr {:op :recur :args []}
      result-expr {:op :const :val :done}
      branch-processor
      (fn [branch]
        (swap! visited conj (:pc branch))
        (case (:pc branch)
          10 (assoc branch
                    :stack [recur-expr]
                    :statements []
                    :ast {}
                    :recur? true)
          30 (assoc branch
                    :stack [result-expr]
                    :statements []
                    :ast {})))
      result (with-redefs [ast/process-insns branch-processor]
               (ast/process-if {:stack []
                                :statements []
                                :impure-loop-entry 0
                                :loop-args []}
                               {:op :local :name "continue?"}
                               [10 20]
                               [30 40 true]))]
  (assert (= [10] @visited))
  (assert (= 30 (:pc result)))
  (assert (:recur? result))
  (assert (empty? (:statements result)))
  (assert (= recur-expr (get-in result [:stack 0 :then :ret])))
  (assert (= ast/nil-expr (get-in result [:stack 0 :else])))
  (println "decompiler impure-loop one-armed recur recovery passed"))

(let [visited (atom [])
      recur-expr {:op :recur :args []}
      result-expr {:op :local :name "result"}
      branch-processor
      (fn [branch]
        (swap! visited conj (:pc branch))
        (case (:pc branch)
          10 (assoc branch
                    :stack [recur-expr]
                    :statements []
                    :ast {}
                    :recur? true)
          30 (assoc branch
                    :stack [result-expr]
                    :statements []
                    :ast {})))
      result (with-redefs [ast/process-insns branch-processor]
               (ast/process-if {:pc 5
                                :stack []
                                :statements []
                                :impure-loop-entry 0
                                :loop-args []
                                :enclosing-end-label 40
                                :insns [{:insn/name "aload_1"
                                         :insn/label 30}]
                                :jump-table {30 0}}
                               {:op :local :name "continue?"}
                               [10 20]
                               [30 50 true]))]
  (assert (= [10 30] @visited))
  (assert (= 40 (:pc result)))
  (assert (:recur? result))
  (assert (empty? (:statements result)))
  (assert (= recur-expr (get-in result [:stack 0 :then :ret])))
  (assert (= result-expr (get-in result [:stack 0 :else :ret])))
  (println "decompiler impure-loop protected value exit preservation passed"))

(let [visited (atom [])
      recur-expr {:op :recur :args []}
      result-expr {:op :const :val :done}
      branch-processor
      (fn [branch]
        (swap! visited conj (:pc branch))
        (case (:pc branch)
          10 (assoc branch
                    :stack [recur-expr]
                    :statements []
                    :ast {}
                    :recur? true)
          30 (assoc branch
                    :stack [result-expr]
                    :statements []
                    :ast {})))
      result (with-redefs [ast/process-insns branch-processor]
               (ast/process-if {:stack []
                                :statements []
                                :impure-loop-entry 0
                                :loop-args [{:name "nested"}]}
                               {:op :local :name "continue?"}
                               [10 20]
                               [30 40 true]))]
  (assert (= [10 30] @visited))
  (assert (= 40 (:pc result)))
  (assert (:recur? result))
  (assert (= recur-expr (get-in result [:stack 0 :then :ret])))
  (assert (= result-expr (get-in result [:stack 0 :else :ret])))
  (println "decompiler nested-loop else preservation passed"))

(let [loop-if {:op :if
               :test {:op :local :name "continue?"}
               :then {:op :do
                      :statements []
                      :ret {:op :recur :args []}}
               :else ast/nil-expr}
      classified-labels (atom [])
      body-processor (fn [body]
                       (assert (= 0 (:impure-loop-entry body)))
                       (assoc body
                              :pc 30
                              :stack []
                              :statements [loop-if]
                              :ast {}
                              :recur? true))
      result (with-redefs [ast/process-insns body-processor
                           ast/will-ret? (fn [_ label]
                                           (swap! classified-labels conj label)
                                           false)]
               (ast/process-impure-loop
                 {:pc 0
                  :impure-loops #{0}
                  :jump-table {0 0, 40 1}
                  :stack []
                  :statements []
                  :ast {}}))]
  (assert (= [30] @classified-labels))
  (assert (= 30 (:pc result)))
  (assert (empty? (:stack result)))
  (assert (= {} (:ast result)))
  (assert (= :loop (get-in result [:statements 0 :op])))
  (assert (= loop-if (get-in result [:statements 0 :body :statements 0])))
  (println "decompiler impure-loop continuation preservation passed"))

(let [ctx-for
      (fn [insns]
        {:insns insns
         :jump-table
         (into {}
               (map-indexed (fn [index insn]
                              [(:insn/label insn) index])
                            insns))})
      direct-return
      (ctx-for [{:insn/name "lreturn" :insn/label 10 :insn/length 1}])
      forward-direct
      (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                 :insn/jump-offset 10}
                {:insn/name "lreturn" :insn/label 20 :insn/length 1}])
      forward-scaffold
      (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                 :insn/jump-offset 10}
                {:insn/name "checkcast" :insn/label 20 :insn/length 3}
                {:insn/name "invokestatic" :insn/label 23 :insn/length 3}
                {:insn/name "nop" :insn/label 26 :insn/length 1}
                {:insn/name "invokevirtual" :insn/label 27 :insn/length 3}
                {:insn/name "areturn" :insn/label 30 :insn/length 1}])
      negative-cases
      [[:backward
        (ctx-for [{:insn/name "lreturn" :insn/label 10 :insn/length 1}
                  {:insn/name "goto" :insn/label 20 :insn/length 3
                   :insn/jump-offset -10}])
        20]
       [:self
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 0}])
        10]
       [:goto-chain
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "goto" :insn/label 20 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "lreturn" :insn/label 30 :insn/length 1}])
        10]
       [:missing-target
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}])
        10]
       [:fallthrough-without-return
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "nop" :insn/label 20 :insn/length 1}])
        10]
       [:non-scaffolding-target
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "astore_0" :insn/label 20 :insn/length 1}
                  {:insn/name "areturn" :insn/label 21 :insn/length 1}])
        10]
       [:conditional-jump
        (ctx-for [{:insn/name "ifne" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "lreturn" :insn/label 20 :insn/length 1}])
        10]
       [:void-return
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "return" :insn/label 20 :insn/length 1}])
        10]
       [:branch-inside-epilogue
        (ctx-for [{:insn/name "goto" :insn/label 10 :insn/length 3
                   :insn/jump-offset 10}
                  {:insn/name "nop" :insn/label 20 :insn/length 1}
                  {:insn/name "ifne" :insn/label 21 :insn/length 3
                   :insn/jump-offset 9}
                  {:insn/name "lreturn" :insn/label 30 :insn/length 1}])
        10]]]
  (assert (ast/will-ret? direct-return 10))
  (assert (ast/will-ret? forward-direct 10))
  (assert (ast/will-ret? forward-scaffold 10))
  (doseq [[label ctx start-label] negative-cases]
    (assert (not (ast/will-ret? ctx start-label))
            (str "forward return-epilogue rule escaped exact scope: " label)))
  (println "decompiler bounded forward value-return epilogue matrix passed"))

(let [body-expr {:op :const :val 17}
      body-processor (fn [body]
                       (assoc body
                              :pc 30
                              :stack [body-expr]
                              :statements []
                              :ast {}
                              :recur? false))
      value-ctx {:pc 0
                 :impure-loops #{0}
                 :insns [{:insn/name "aload_0" :insn/label 0 :insn/length 1}
                         {:insn/name "goto" :insn/label 30 :insn/length 3
                          :insn/jump-offset 10}
                         {:insn/name "lreturn" :insn/label 40 :insn/length 1}]
                 :jump-table {0 0, 30 1, 40 2}
                 :stack []
                 :statements []
                 :ast {}}
      value-result (with-redefs [ast/process-insns body-processor]
                     (ast/process-impure-loop value-ctx))
      value-form (source/ast->clj (peek (:stack value-result)))
      statement-ctx {:pc 0
                     :impure-loops #{0}
                     ;; The loop entry deliberately looks return-like.  The
                     ;; actual body continuation does not return a value.
                     :insns [{:insn/name "lreturn" :insn/label 0 :insn/length 1}
                             {:insn/name "astore_0" :insn/label 30 :insn/length 1}]
                     :jump-table {0 0, 30 1}
                     :stack []
                     :statements []
                     :ast {}}
      statement-result (with-redefs [ast/process-insns body-processor]
                         (ast/process-impure-loop statement-ctx))]
  (assert (= 30 (:pc value-result)))
  (assert (empty? (:statements value-result)))
  (assert (= :loop (get-in value-result [:stack 0 :op])))
  ;; A value-bearing implicit loop must remain the binding expression.  If it
  ;; is misclassified as a statement, the surrounding AST becomes
  ;; (do (loop ...) nil), which silently discards dequeued/updated values.
  (assert (= '(loop* [] (do 17)) value-form))
  (assert (= 17 (eval value-form)))
  (assert (= 30 (:pc statement-result)))
  (assert (empty? (:stack statement-result)))
  (assert (= :loop (get-in statement-result [:statements 0 :op])))
  (println "decompiler impure-loop body-continuation value behavior passed"))

;; Exact control-flow boundary from Transactor
;; datomic/common$compare_byte_arrays.class (SHA-256 23e34d076e57e17d...).
;; Its binding-free loop starts at pc 37, but both equal-length values leave
;; through pc 114's forward GOTO to the shared primitive return at pc 120.
;; Looking at the loop entry misclassifies the loop as a statement and emits
;; `(do (loop ...) nil)`; looking at the processed body's continuation keeps
;; the loop as the branch value.
(let [body-value {:op :const :val 0}
      body-processor (fn [body]
                       (assoc body
                              :pc 114
                              :stack [body-value]
                              :statements []
                              :ast {}
                              :recur? true))
      ctx {:pc 37
           :impure-loops #{37}
           :insns [{:insn/name "lload" :insn/label 37 :insn/length 2}
                   {:insn/name "goto" :insn/label 114 :insn/length 3
                    :insn/jump-offset 6}
                   {:insn/name "athrow" :insn/label 117 :insn/length 1}
                   {:insn/name "lload" :insn/label 118 :insn/length 2}
                   {:insn/name "lreturn" :insn/label 120 :insn/length 1}]
           :jump-table {37 0, 114 1, 117 2, 118 3, 120 4}
           :stack []
           :statements []
           :ast {}}
      recovered (with-redefs [ast/process-insns body-processor]
                  (ast/process-impure-loop ctx))
      recovered-form (source/ast->clj (peek (:stack recovered)))]
  (assert (not (ast/will-ret? ctx 37)))
  (assert (ast/will-ret? ctx 114))
  (assert (= 114 (:pc recovered)))
  (assert (empty? (:statements recovered)))
  (assert (= '(loop* [] (do 0)) recovered-form))
  (assert (= 0 (eval recovered-form)))
  (println "decompiler compare-byte-arrays exact AOT loop value passed"))

(let [ctx {:pc 10 :enclosing-end-label 20}
      crossed (ast/continue-within-enclosing-region ctx {:pc 30})
      exact (ast/continue-within-enclosing-region ctx {:pc 20})
      at-boundary (ast/continue-within-enclosing-region
                    {:pc 20 :enclosing-end-label 20}
                    {:pc 30})]
  (assert (= 20 (:pc crossed)))
  (assert (= 20 (:pc exact)))
  (assert (= 30 (:pc at-boundary)))
  (println "decompiler enclosing structured-region boundary passed"))

(let [init-expr {:op :const :val :binding-value}
      recur-expr {:op :recur :args []}
      result-expr {:op :local :name "j"}
      test-expr {:op :local :name "continue?"}
      local-variable {:op :local
                      :name "j"
                      :index 3
                      :start-label 1
                      :end-label 20}
      ctx {:pc 0
           :stack []
           :statements []
           :insns [{:insn/name "astore_3"
                    :insn/label 0
                    :insn/length 1}
                   ;; The value-bearing else begins inside the lexical
                   ;; region.  Its inferred method-wide end deliberately
                   ;; crosses that region, matching AOT which replaces an
                   ;; unreachable branch-to-end GOTO with NOP/NOP/ATHROW.
                   {:insn/name "aload_3"
                    :insn/label 15
                    :insn/length 1}]
           :jump-table {0 0, 15 1}}
      branch-processor
      (fn branch-processor [branch]
        (case (:pc branch)
          1 (let [result (ast/process-if branch test-expr
                                         [5 10] [15 30 true])]
              (if ((:terminate? result) result)
                result
                ;; Without the lexical boundary, processing advances to the
                ;; method return and consumes the branch value.
                (assoc result :pc 31 :stack [] :statements []
                              :ast {:op :const :val :consumed-return})))
          5 (assoc branch :pc 10 :stack [recur-expr]
                          :statements [] :recur? true)
          15 (assoc branch :pc 30 :stack [result-expr]
                           :statements [] :recur? false)))
      recovered (with-redefs [ast/process-insns branch-processor]
                  (ast/process-let ctx local-variable init-expr))]
  (assert (empty? (:statements recovered)))
  (assert (= :let (get-in recovered [:stack 0 :op])))
  (assert (= :if (get-in recovered [:stack 0 :body :ret :op])))
  (assert (= recur-expr
             (get-in recovered [:stack 0 :body :ret :then :ret])))
  (assert (= result-expr
             (get-in recovered [:stack 0 :body :ret :else :ret])))
  (println "decompiler lexical branch value across dead ATHROW padding passed"))

(let [x-local #:local-variable{:name "x"
                                :start-label 0
                                :end-label 47
                                :index 0
                                :type "java.lang.Object"}
      cause-local #:local-variable{:name "cause"
                                    :start-label 15
                                    :end-label 40
                                    :index 1
                                    :type "java.lang.Object"}
      load-local
      (fn [name label index]
        {:insn/name name
         :insn/label label
         :insn/length 1
         :insn/local-variable-element
         {:insn/target-type "java.lang.Object"
          :insn/target-index index}})
      store-local load-local
      bytecode
      [(load-local "aload_0" 0 0)
       {:insn/name "instanceof" :insn/label 1 :insn/length 3
        :insn/pool-element
        {:insn/target-value "java/lang/Throwable"
         :insn/target-type "java.lang.Throwable"}}
       {:insn/name "ifeq" :insn/label 4 :insn/length 3
        :insn/jump-offset 40}
       (load-local "aload_0" 7 0)
       {:insn/name "checkcast" :insn/label 8 :insn/length 3
        :insn/pool-element
        {:insn/target-value "java/lang/Throwable"
         :insn/target-type "java.lang.Throwable"}}
       {:insn/name "invokevirtual" :insn/label 11 :insn/length 3
        :insn/pool-element
        {:insn/target-class "java.lang.Throwable"
         :insn/target-name "getCause"
         :insn/target-arg-types []
         :insn/target-ret-type "java.lang.Throwable"}}
       (store-local "astore_1" 14 1)
       (load-local "aload_1" 15 1)
       {:insn/name "dup" :insn/label 16 :insn/length 1}
       {:insn/name "ifnull" :insn/label 17 :insn/length 3
        :insn/jump-offset 19}
       {:insn/name "getstatic" :insn/label 20 :insn/length 3
        :insn/pool-element
        {:insn/target-class "java.lang.Boolean"
         :insn/target-name "FALSE"
         :insn/target-type "java.lang.Boolean"}}
       {:insn/name "if_acmpeq" :insn/label 23 :insn/length 3
        :insn/jump-offset 14}
       (load-local "aload_1" 26 1)
       {:insn/name "aconst_null" :insn/label 27 :insn/length 1}
       (store-local "astore_1" 28 1)
       (store-local "astore_0" 29 0)
       {:insn/name "goto" :insn/label 30 :insn/length 3
        :insn/jump-offset -30}
       ;; Clojure 1.11 replaces the unreachable forward GOTO after recur with
       ;; this dead padding. The lexical end at 40 is therefore the only
       ;; reliable boundary for the value-bearing `x` branch at 37.
       {:insn/name "nop" :insn/label 33 :insn/length 1}
       {:insn/name "nop" :insn/label 34 :insn/length 1}
       {:insn/name "athrow" :insn/label 35 :insn/length 1}
       {:insn/name "pop" :insn/label 36 :insn/length 1}
       (load-local "aload_0" 37 0)
       {:insn/name "aconst_null" :insn/label 38 :insn/length 1}
       (store-local "astore_0" 39 0)
       {:insn/name "goto" :insn/label 40 :insn/length 3
        :insn/jump-offset 7}
       {:insn/name "athrow" :insn/label 43 :insn/length 1}
       (load-local "aload_0" 44 0)
       {:insn/name "aconst_null" :insn/label 45 :insn/length 1}
       (store-local "astore_0" 46 0)
       {:insn/name "areturn" :insn/label 47 :insn/length 1}]
      method #:method{:name "invokeStatic"
                      :flags #{:public :static}
                      :return-type "java.lang.Object"
                      :arg-types ["java.lang.Object"]
                      :bytecode bytecode
                      :jump-table
                      (into {}
                            (map-indexed
                              (fn [index instruction]
                                [(:insn/label instruction) index])
                              bytecode))
                      :local-variable-table #{x-local cause-local}
                      :exception-table #{}}
      recovered
      (ast/process-method-insns
        {:bc-for (constantly nil)
         :class-name "fixture.RootCause"
         :fn-name "root_cause"}
        method)
      outer-if (get-in recovered [:ast :ret])
      cause-let (get-in outer-if [:then :ret])
      cause-if (get-in cause-let [:body :ret])]
  (assert (= :if (:op outer-if)))
  (assert (= :let (:op cause-let)))
  (assert (= :if (:op cause-if)))
  (assert (= {:op :recur
              :args [(assoc (get-in cause-if [:test])
                            :init
                            (get-in cause-let [:local-variables 0 :init]))]}
             (get-in cause-if [:then :ret])))
  (assert (= "x" (get-in cause-if [:else :ret :name])))
  (assert (= "x" (get-in outer-if [:else :ret :name])))
  (println "decompiler recursive root-cause value across dead ATHROW padding passed"))

(let [outer-a {:start-label 0 :end-label 68 :handler-label 273 :type "java.lang.Throwable"}
      outer-b {:start-label 72 :end-label 268 :handler-label 273 :type "java.lang.Throwable"}
      inner-a {:start-label 0 :end-label 68 :handler-label 156 :type "java.lang.Throwable"}
      inner-b {:start-label 72 :end-label 152 :handler-label 156 :type "java.lang.Throwable"}
      table #{outer-a outer-b inner-a inner-b}
      outer (ast/coalesce-split-try-ranges 0 table)
      inner (ast/coalesce-split-try-ranges
              0
              (apply disj table (:consumed outer)))]
  (assert (= [{:start-label 0
               :end-label 268
               :handler-label 273
               :type "java.lang.Throwable"}]
             (:handlers outer)))
  (assert (= #{outer-a outer-b} (set (:consumed outer))))
  (assert (= 152 (get-in inner [:handlers 0 :end-label])))
  (assert (= #{inner-a inner-b} (set (:consumed inner))))
  (println "decompiler nested split-try coalescing passed"))

(let [terminal (with-redefs [ast/insn-at (fn [_ _] {:insn/name "athrow"})
                             ast/get-reachable (fn [& _] #{})]
                 (ast/try-return-label
                   {:pc 0
                    :insns [{:insn/label 10 :insn/length 1}]
                    :jump-table {10 0}}
                   [{:handler-label 9}]))
      outer-target (with-redefs [ast/insn-at (fn [_ _] {:insn/name "athrow"})
                                 ast/get-reachable (fn [& _] #{220})]
                     (ast/try-return-label
                       {:pc 139
                        :insns [{:insn/label 0 :insn/jump-offset 220}
                                {:insn/label 222 :insn/length 1}]
                        :jump-table {0 0, 220 1, 222 2}}
                       [{:handler-label 196}]))]
  (assert (= 11 terminal))
  (assert (= 220 outer-target))
  (println "decompiler terminal/pre-try continuation recovery passed"))

(let [insns [{:insn/label 196
              :insn/name "astore"
              :insn/local-variable-element {:insn/target-index 4}}
             {:insn/label 198 :insn/name "getstatic"}
             {:insn/label 213
              :insn/name "aload"
              :insn/local-variable-element {:insn/target-index 4}}
             {:insn/label 215 :insn/name "athrow"}]
      recovered (ast/exceptional-finally-range
                  {:insns insns
                   :jump-table {196 0, 198 1, 213 2, 215 3}}
                  {:handler-label 196})]
  (assert (= {:start-label 198 :end-label 213} recovered))
  (println "decompiler exceptional-only finally recovery passed"))

(let [keyword-ast (fn [namespace name]
                    {:op :invoke-static
                     :target "clojure.lang.RT"
                     :method "keyword"
                     :args [{:op :const :val namespace}
                            {:op :const :val name}]})
      namespaced (keyword-ast "db" "index")
      unqualified (keyword-ast nil "index")
      fields {"namespaced" {:args [namespaced]}
              "unqualified" {:args [unqualified]}}
      recovered-namespaced
      (sugar/ast->sugared-ast (ast/keyword-lookup-fn fields "namespaced"))
      recovered-unqualified
      (sugar/ast->sugared-ast (ast/keyword-lookup-fn fields "unqualified"))]
  (assert (= :db/index (:val recovered-namespaced)))
  (assert (= :index (:val recovered-unqualified)))
  (println "decompiler namespaced KeywordLookupSite recovery passed"))

(let [recovered
      (sugar/ast->sugared-ast*
        {:op :invoke-static
         :target "java.lang.Character"
         :method "valueOf"
         :arg-types ["char"]
         :args [{:op :const :val 95}]})]
  (assert (= :const (:op recovered)))
  (assert (= \_ (:val recovered)))
  (assert (= \_ (read-string (pr-str (:val recovered)))))
  (println "decompiler boxed character literal recovery passed"))

(let [basis (decompiler/record-basis-symbols
              {:class/fields
               [{:field/name "fressian_tag"
                 :field/flags #{:public :final}}
                {:field/name "root"
                 :field/flags #{:public :final}}
                {:field/name "__meta"
                 :field/flags #{:public :final}}]
               :class/methods
               [{:method/name "getBasis"
                 :method/flags #{:public :static}
                 :method/arg-types []
                 :method/bytecode
                 [{:insn/name "aconst_null"}
                  {:insn/name "ldc"
                   :insn/pool-element
                   {:insn/target-type "java.lang.String"
                    :insn/target-value "fressian-tag"}}
                  {:insn/name "invokestatic"
                   :insn/pool-element
                   {:insn/target-class "clojure.lang.Symbol"
                    :insn/target-name "intern"}}
                  {:insn/name "ldc"
                   :insn/pool-element
                   {:insn/target-type "java.lang.String"
                    :insn/target-value "root"}}
                  {:insn/name "invokestatic"
                   :insn/pool-element
                   {:insn/target-class "clojure.lang.Symbol"
                    :insn/target-name "intern"}}
                  {:insn/name "ldc"
                   :insn/pool-element
                   {:insn/target-type "java.lang.String"
                    :insn/target-value "tag"}}
                  {:insn/name "invokestatic"
                   :insn/pool-element
                   {:insn/target-class "clojure.lang.RT"
                    :insn/target-name "keyword"}}
                  {:insn/name "ldc"
                   :insn/pool-element
                   {:insn/target-type "java.lang.String"
                    :insn/target-value "Root"}}
                  {:insn/name "invokestatic"
                   :insn/pool-element
                   {:insn/target-class "clojure.lang.Symbol"
                    :insn/target-name "intern"}}]}]})
      restored
      (decompiler/restore-record-form-fields
        '(defrecord ValueType [^long fressian_tag root]
           Example
           (value [_] [fressian_tag root]))
        basis)
      fields (nth restored 2)
      body-symbols (-> restored last last)]
  (assert (= ['fressian-tag (with-meta 'root {:tag 'Root})] basis))
  (assert (= ['fressian-tag 'root] (mapv #(with-meta % nil) fields)))
  (assert (= 'long (:tag (meta (first fields)))))
  (assert (= 'Root (:tag (meta (second fields)))))
  (assert (= ['fressian-tag 'root] body-symbols))
  (assert (every? (comp nil? meta) body-symbols))
  (println "decompiler defrecord source-field spelling/metadata passed"))

(let [mutable-long (source/hinted-symbol
                     {:name "offset"
                      :type "long"
                      :mutable? :unsynchronized-mutable})
      metadata (meta mutable-long)]
  (assert (= 'long (:tag metadata)))
  (assert (= true (:unsynchronized-mutable metadata)))
  (println "decompiler mutable primitive field metadata passed"))

(let [int-assignment
      (sugar/ast->sugared-ast*
        {:op :set!
         :target {:op :local :name "idx"}
         :target-type "int"
         :val {:op :local :name "next-idx"}})
      boolean-assignment
      (sugar/ast->sugared-ast*
        {:op :set!
         :target {:op :local :name "flag"}
         :target-type "boolean"
         :val {:op :const :val 0}})]
  (assert (= "int" (get-in int-assignment [:val :fn :name])))
  (assert (= "boolean" (get-in boolean-assignment [:val :fn :name])))
  (assert (= false (get-in boolean-assignment [:val :args 0 :val])))
  (println "decompiler primitive mutable-field assignment coercion passed"))

(let [primitive-fn
      (source/ast->clj
        {:op :fn
         :name "primitive_audit"
         :fn-methods
         [{:op :fn-method
           :return-type "long"
           :args [{:name "x" :type "long"}]
           :body {:op :local :name "x"}}]})
      argv (-> primitive-fn (nth 2) first)
      compiled (eval primitive-fn)]
  (assert (= 'long (:tag (meta argv))))
  (assert (= 'long (:tag (meta (first argv)))))
  (assert (some #{"clojure.lang.IFn$LL"}
                (map #(.getName ^Class %) (.getInterfaces (class compiled)))))
  (println "decompiler primitive fn ABI metadata passed"))

(let [primitive-form
      (source/ast->clj
        {:op :fn
         :name "primitive_roundtrip_audit"
         :fn-methods
         [{:op :fn-method
           :return-type "long"
           :args [{:name "x" :type "long"}]
           :body {:op :local :name "x"}}]})
      roundtripped (read-string (decompiler.pprint/pprint primitive-form))
      argv (-> roundtripped (nth 2) first)
      compiled (eval roundtripped)]
  (assert (= 'long (:tag (meta argv))))
  (assert (some #{"clojure.lang.IFn$LL"}
                (map #(.getName ^Class %) (.getInterfaces (class compiled)))))
  (println "decompiler persisted primitive fn ABI metadata passed"))

;; Exact source shape recovered from the Transactor
;; datomic.promise/settable-future locking bodies. Raw monitor-enter/exit
;; special forms compile into an unsafe local-lifetime shape; the original
;; AOT instead came from clojure.core/locking and must be reconstructed.
(let [expanded
      '(let [lockee__5782__auto__ listeners
             locklocal__5783__auto__ lockee__5782__auto__]
         (monitor-enter locklocal__5783__auto__)
         (try
           (do (.countDown d) nil)
           (finally
             (do (monitor-exit locklocal__5783__auto__) nil))))
      source-stage-expanded
      '(let* [lockee__5782__auto__ listeners
              locklocal__5783__auto__ lockee__5782__auto__]
         (monitor-enter locklocal__5783__auto__)
         (try
           (do (.countDown d) nil)
           (finally
             (do (monitor-exit locklocal__5783__auto__) nil))))
      wrong-exit
      '(let [lockee__5782__auto__ listeners
             locklocal__5783__auto__ lockee__5782__auto__]
         (monitor-enter locklocal__5783__auto__)
         (try
           :body
           (finally
             (do (monitor-exit different-lock) nil))))
      wrong-source
      '(let [lockee__5782__auto__ listeners
             locklocal__5783__auto__ different-lockee]
         (monitor-enter locklocal__5783__auto__)
         (try :body
              (finally (monitor-exit locklocal__5783__auto__))))
      qualified-lookalike
      '(let [lockee__5782__auto__ listeners
             locklocal__5783__auto__ lockee__5782__auto__]
         (fake/monitor-enter locklocal__5783__auto__)
         (try :body
              (finally (monitor-exit locklocal__5783__auto__))))
      foreign-let
      '(foreign/let [lockee__5782__auto__ listeners
                     locklocal__5783__auto__ lockee__5782__auto__]
         (monitor-enter locklocal__5783__auto__)
         (try :body
              (finally (monitor-exit locklocal__5783__auto__))))
      ordinary-qualified-let
      '(clojure.core/let [x :value]
         :effect
         x)
      extra-finally-body
      '(let [lockee__5782__auto__ listeners
             locklocal__5783__auto__ lockee__5782__auto__]
         (monitor-enter locklocal__5783__auto__)
         (try :body
              (finally (monitor-exit locklocal__5783__auto__) :extra)))
      recovered (compact/macrocompact expanded)
      source-stage-recovered (compact/macrocompact source-stage-expanded)
      rejected (mapv compact/macrocompact
                     [wrong-exit wrong-source qualified-lookalike foreign-let
                      ordinary-qualified-let extra-finally-body])]
  (assert (= '(locking listeners (do (.countDown d) nil)) recovered))
  ;; `let*` is first compacted to syntax-quoted `clojure.core/let`; this is
  ;; the exact pass-order shape that previously escaped locking recovery in
  ;; datomic.promise/settable-future.
  (assert (= recovered source-stage-recovered))
  (assert (every? #(not= 'locking (first %)) rejected))
  (assert (some #{'monitor-enter}
                (tree-seq coll? seq (first rejected))))
  (assert (= 0
             (eval
               `(let [~'listeners (Object.)
                      ~'d (java.util.concurrent.CountDownLatch. 1)]
                  ~recovered
                  (.getCount ~'d)))))
  (println "decompiler exact locking monitor-pair recovery passed"))

(let [variadic-method
      (source/ast->clj
        {:op :fn-method
         :return-type "java.lang.Object"
         :var-args? true
         :args [{:name "fixed" :type "java.lang.Object"}
                {:name "more" :type "clojure.lang.ISeq"}]
         :body {:op :local :name "more"}})
      argv (first variadic-method)
      rest-arg (peek argv)]
  (assert (= '& (nth argv (- (count argv) 2))))
  (assert (nil? (:tag (meta rest-arg))))
  (println "decompiler variadic rest argument metadata passed"))

(let [coordinate (Integer/valueOf 17)
      semantic
      (list '.setMeta
            (list 'var 'typed-metadata-root)
            {:column coordinate
             :arglists
             (list 'clojure.core/list
                   [(list '.withMeta
                          (list 'quote 'value)
                          {:column coordinate
                           :pre (list 'quote
                                      [(list 'pos? 'value)])})])})
      ordinary (list 'identity {:column coordinate})
      quoted (list 'quote {:column coordinate})
      repaired
      (compact/preserve-exact-metadata-number-types
        (list 'do semantic ordinary quoted))
      repaired-semantic (second repaired)
      repaired-ordinary (nth repaired 2)
      repaired-quoted (nth repaired 3)
      roundtripped (read-string (decompiler.pprint/pprint repaired))
      metadata (nth repaired-semantic 2)
      nested-column
      (-> metadata :arglists second first (nth 2) :column)]
  (assert (= '(int 17) (:column metadata)))
  (assert (= '(int 17) nested-column))
  (assert (instance? Integer (:column (second repaired-ordinary))))
  (assert (instance? Integer (:column (second repaired-quoted))))
  (assert (= 17 (-> roundtripped second (nth 2) :column eval)))
  (assert (instance? Integer
                     (-> roundtripped second (nth 2) :column eval)))
  (assert (instance? Long (-> roundtripped (nth 2) second :column)))
  (assert (instance? Long (-> roundtripped (nth 3) second :column)))
  (println "decompiler exact typed-metadata/quoted-domain isolation passed"))

(let [coordinate (Integer/valueOf 23)
      metadata-operand
      {:arglists
       (list 'clojure.core/list
             [(list '.withMeta
                    (list 'quote 'value)
                    {:column coordinate
                     :pre (list 'quote [(list 'some? 'value)])})])}
      protected
      (compact/macrocompact
        (list 'do
              (list '.setMeta (list 'var 'semantic-with-meta)
                    metadata-operand)
              (list '.bindRoot
                    (list 'var 'semantic-with-meta)
                    '(fn* semantic-with-meta ([value] value)))))
      executable-lookalike
      (compact/macrocompact
        '(.withMeta (clojure.core/list :domain-value) {:line 9}))]
  (assert (some #(and (seq? %)
                      (= ".withMeta" (compact/call-name %)))
                (tree-seq coll? seq protected)))
  (assert (= '(clojure.core/list :domain-value) executable-lookalike))
  (println "decompiler semantic withMeta protection/scaffolding negative passed"))

(let [coordinate (Integer/valueOf 37)
      nested-default
      (list '.withMeta
            (list 'clojure.core/list (list 'quote 'build-default) 1)
            {:column coordinate})
      metadata-operand
      {:arglists
       (list 'clojure.core/list
             [{:keys [(list 'quote 'value)]
               :or {(list 'quote 'value) nested-default}}])}
      source
      (list 'do
            (list '.setMeta (list 'var 'nested-default-root)
                  metadata-operand)
            (list '.bindRoot (list 'var 'nested-default-root)
                  '(fn* nested-default-root ([options] options))))
      annotated (compact/annotate-bind-root-metadata source)
      annotated-marker (second annotated)
      [protected registry]
      (compact/protect-semantic-metadata-forms annotated)
      recovered (compact/macrocompact source)
      persisted (read-string (decompiler.pprint/pprint recovered))
      recovered-with-meta
      (first
        (filter #(and (seq? %)
                      (= ".withMeta" (compact/call-name %))
                      (= {:column '(int 37)} (nth % 2 nil)))
                (tree-seq coll? seq persisted)))
      executable-value
      (list '.withMeta
            (list 'clojure.core/list :executable-value)
            {:column coordinate})
      value-source
      (list 'do
            (list '.setMeta (list 'var 'value-negative-root)
                  {:arglists (list 'clojure.core/list [])})
            (list '.bindRoot (list 'var 'value-negative-root)
                  executable-value))
      value-recovered (compact/macrocompact value-source)
      foreign-head (Object.)
      malformed-marker
      (list compact/metadata-aware-bind-root-marker-tag
            (list 'var 'malformed-root)
            '(fn* malformed-root ([x] x)))
      foreign-marker
      (list foreign-head
            (list 'var 'foreign-root)
            '(fn* foreign-root ([x] x))
            metadata-operand)
      [_ malformed-registry]
      (compact/protect-semantic-metadata-forms malformed-marker)
      [_ foreign-registry]
      (compact/protect-semantic-metadata-forms foreign-marker)]
  (assert (and (= 4 (count annotated-marker))
               (identical? compact/metadata-aware-bind-root-marker-tag
                           (first annotated-marker))))
  (assert (= 1 (count registry)))
  (assert recovered-with-meta)
  (assert (not-any? #(and (seq? %)
                          (= ".withMeta" (compact/call-name %)))
                    (tree-seq coll? seq value-recovered)))
  (assert (empty? malformed-registry))
  (assert (empty? foreign-registry))
  (let [fixture-ns
        (symbol (str "macrocompact.nested.default.metadata." (gensym)))]
    (try
      (binding [*ns* (create-ns fixture-ns)]
        (clojure.core/refer 'clojure.core)
        (eval persisted)
        (let [root-metadata (meta (ns-resolve *ns* 'nested-default-root))
              default-form (-> root-metadata :arglists first first :or vals first)]
          (assert (= '(build-default 1) default-form))
          (assert (= {:column 37} (meta default-form)))
          (assert (= Integer (class (:column (meta default-form)))))))
      (finally
        (when (find-ns fixture-ns) (remove-ns fixture-ns)))))
  (println "decompiler metadata-aware marker nested location metadata boundary passed"))

(let [fixture-ns
      (symbol (str "macrocompact.ordered.nested.default." (gensym)))
      local-root 'ordered-default-root
      qualified-root (symbol (str fixture-ns) (name local-root))
      coordinate (Integer/valueOf 39)
      nested-default
      (list '.withMeta
            (list 'clojure.core/list (list 'quote 'ordered-default) 2)
            {:column coordinate})
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (list '.setMeta
                  (list 'var qualified-root)
                  {:tag (list 'clojure.lang.RT/classForName
                              "java.lang.String")
                   :arglists
                   (list 'clojure.core/list
                         [{:keys [(list 'quote 'value)]
                           :or {(list 'quote 'value) nested-default}}])})
            (list '.bindRoot
                  (list 'var local-root)
                  '(fn* ordered-default-root ([options] options))))
      annotated (compact/annotate-bind-root-metadata source)
      order-set-marker
      (first
        (filter #(and (seq? %)
                      (identical? compact/metadata-order-set-marker-tag
                                  (first %)))
                (tree-seq coll? seq annotated)))
      [protected registry]
      (compact/protect-semantic-metadata-forms annotated)
      persisted
      (read-string (decompiler.pprint/pprint (compact/macrocompact source)))]
  (assert (= 4 (count order-set-marker)))
  (assert (= 1 (count registry)))
  (assert (not-any? compact/erasable-list-location-with-meta?
                    (tree-seq coll? seq protected)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval persisted)
      (let [root (ns-resolve *ns* local-root)
            default-form (-> root meta :arglists first first :or vals first)]
        (assert (= java.lang.String (:tag (meta root))))
        (assert (= '(ordered-default 2) default-form))
        (assert (= {:column 39} (meta default-form)))
        (assert (= Integer (class (:column (meta default-form)))))))
    (finally
      (when (find-ns fixture-ns) (remove-ns fixture-ns))))
  (println "decompiler ordered setMeta marker nested location metadata passed"))

(let [fixture-ns
      (symbol (str "macrocompact.dynamic.nested.default." (gensym)))
      local-root '*dynamic-default-root*
      qualified-root (symbol (str fixture-ns) (name local-root))
      coordinate (Integer/valueOf 41)
      nested-default
      (list '.withMeta
            (list 'clojure.core/list (list 'quote 'dynamic-default) true)
            {:column coordinate})
      source
      (list 'do
            (list 'clojure.core/in-ns (list 'quote fixture-ns))
            (list '.setDynamic (list 'var qualified-root) true)
            (list '.setMeta
                  (list '.setDynamic (list 'var qualified-root) true)
                  {:dynamic true
                   :arglists
                   (list 'clojure.core/list
                         [{:keys [(list 'quote 'value)]
                           :or {(list 'quote 'value) nested-default}}])})
            (list '.bindRoot
                  (list '.setDynamic (list 'var local-root) true)
                  '(fn* dynamic-default-root ([options] options))))
      annotated (compact/annotate-bind-root-metadata source)
      annotated-marker
      (first
        (filter #(and (seq? %)
                      (identical?
                        compact/metadata-aware-dynamic-bind-root-marker-tag
                        (first %)))
                (tree-seq coll? seq annotated)))
      persisted
      (read-string (decompiler.pprint/pprint (compact/macrocompact source)))]
  (assert (= 4 (count annotated-marker)))
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval persisted)
      (let [root (ns-resolve *ns* local-root)
            default-form (-> root meta :arglists first first :or vals first)]
        (assert (= true (:dynamic (meta root))))
        (assert (= '(dynamic-default true) default-form))
        (assert (= {:column 41} (meta default-form)))
        (assert (= Integer (class (:column (meta default-form)))))))
    (finally
      (when (find-ns fixture-ns) (remove-ns fixture-ns))))
  (println "decompiler dynamic metadata-aware marker nested location metadata passed"))

(def exact-protocol-events (atom []))

(let [declaration
      (compact/protocol-method-declaration
        [:as-uri
         {:tag nil
          :name 'as-uri
          :arglists
          '(clojure.core/list
             (.withMeta [(quote x)] {:tag (quote URI)}))
          :doc "Coerce argument to a URI"}])
      persisted (read-string (decompiler.pprint/pprint declaration))]
  (assert (= 'as-uri (first persisted)))
  (assert (= '[x] (second persisted)))
  (assert (= 'URI (:tag (meta (second persisted)))))
  (assert (= "Coerce argument to a URI" (nth persisted 2)))
  (println "decompiler protocol arglist-vector return tag passed"))

(let [fixture-ns (symbol (str "macrocompact.exact.protocol." (gensym)))
      protocol (symbol (str fixture-ns) "Metrics")
      method-name 'metrics
      coordinate (Integer/valueOf 1)
      metadata-form
      (list 'do
            (list 'clojure.core/swap! 'user/exact-protocol-events
                  'clojure.core/conj :metadata)
            {:column coordinate})
      arglists
      (list 'clojure.core/list
            [(list '.withMeta
                   (list 'quote 'target)
                   {:tag (list 'quote 'Object)
                    :column coordinate})])
      raw-name (list '.withMeta (list 'quote method-name)
                     {:arglists arglists})
      signature {:tag (list 'quote 'long)
                 :name raw-name
                 :arglists arglists
                 :doc "Returns a measured value."}
      initializer
      (list
        (list 'fn* 'protocol_initializer
              (list []
                    (list 'do
                          (list '.setMeta (list 'var protocol)
                                metadata-form)
                          (list 'let* ['protocol_var (list 'var protocol)]
                                (list 'if
                                      (list '.hasRoot 'protocol_var)
                                      nil
                                      (list 'do
                                            (list '.setMeta
                                                  (list 'var protocol)
                                                  metadata-form)
                                            (list '.bindRoot
                                                  (list 'var protocol) {})
                                            (list 'var protocol))))))))
      class-load (list 'clojure.lang.RT/classForName
                       (compact/protocol-class-name protocol))
      doc-form (list 'clojure.core/alter-meta! (list 'var protocol)
                     'clojure.core/assoc :doc "Protocol documentation.")
      assertion
      (list (list 'var 'clojure.core/assert-same-protocol)
            (list 'var protocol)
            (list 'clojure.core/list raw-name))
      root-form
      (list 'clojure.core/alter-var-root
            (list 'var protocol)
            'clojure.core/merge
            (list 'clojure.core/assoc
                  {:on (list 'quote
                             (symbol (compact/protocol-class-name protocol)))
                   :on-interface class-load}
                  :sigs {(keyword method-name) signature}
                  :var (list 'var protocol)
                  :method-map {(keyword method-name) (keyword method-name)}
                  :method-builders {}))
      reset-form (list 'clojure.core/-reset-methods protocol)
      return-form (list 'quote (symbol (name protocol)))
      span [initializer class-load doc-form assertion root-form reset-form
            return-form]
      source
      (list* 'do
             (concat
               [(list 'clojure.core/in-ns (list 'quote fixture-ns))]
               span
               [(list 'clojure.core/swap! 'user/exact-protocol-events
                      'clojure.core/conj :after)]))
      [protected registry] (compact/protect-protocol-scaffolds source)
      [_ preinterned-registry]
      (compact/protect-protocol-scaffolds source
                                          #{(symbol (str fixture-ns)
                                                    (name method-name))})
      malformed-span (assoc span 5
                            (list 'clojure.core/-reset-methods
                                  (symbol (str fixture-ns) "Other")))
      [_ malformed-registry]
      (compact/protect-protocol-scaffolds (list* 'do malformed-span))
      recovered (compact/macrocompact source)
      preinterned-recovered
      (compact/macrocompact source
                            #{(symbol (str fixture-ns) (name method-name))})
      persisted (read-string (decompiler.pprint/pprint recovered))
      preinterned-persisted
      (read-string (decompiler.pprint/pprint preinterned-recovered))]
  (assert (= 1 (count registry)))
  (assert (= 1 (count preinterned-registry)))
  (assert (= {(keyword method-name) false}
             (:method-var-preinterned-before-namespace-load?
               (val (first registry)))))
  (assert (= {(keyword method-name) true}
             (:method-var-preinterned-before-namespace-load?
               (val (first preinterned-registry)))))
  (assert (empty? malformed-registry))
  (assert (some #(and (seq? %)
                      (identical? compact/protocol-scaffold-marker-tag
                                  (first %)))
                (tree-seq coll? seq protected)))
  (reset! exact-protocol-events [])
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval preinterned-persisted)
      (let [method-var (ns-resolve *ns* method-name)
            method-symbol (:name (meta method-var))]
        (assert (= [:metadata :after] @exact-protocol-events))
        (assert (= method-name method-symbol))
        (assert (nil? (meta method-symbol)))))
    (finally
      (when (find-ns fixture-ns) (remove-ns fixture-ns))))
  (reset! exact-protocol-events [])
  (try
    (binding [*ns* (create-ns fixture-ns)]
      (clojure.core/refer 'clojure.core)
      (eval persisted)
      (let [protocol-var (ns-resolve *ns* 'Metrics)
            method-var (ns-resolve *ns* method-name)
            protocol-metadata (meta protocol-var)
            method-metadata (meta method-var)
            method-symbol (:name method-metadata)
            raw-method-symbol (:name (meta method-symbol))
            target-symbol (-> method-metadata :arglists first first)]
        (assert (= [:metadata :after] @exact-protocol-events))
        (assert (= Integer (class (:column protocol-metadata))))
        (assert (= "Protocol documentation." (:doc protocol-metadata)))
        (assert (= "Returns a measured value." (:doc method-metadata)))
        (assert (= 'long (:tag method-metadata)))
        (assert (identical? protocol-var (:protocol method-metadata)))
        (assert (seq (meta method-symbol)))
        (assert (= "Returns a measured value." (:doc (meta method-symbol))))
        (assert (nil? (:doc (meta raw-method-symbol))))
        (assert (= Integer (class (:column (meta target-symbol)))))))
    (finally
      (when (find-ns fixture-ns) (remove-ns fixture-ns))))
  (println "decompiler generic protocol preintern/plain and Metrics rich-name controls passed"))

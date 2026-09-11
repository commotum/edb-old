;;   Copyright (c) Nicola Mometto & contributors.


(ns clojure.tools.decompiler
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.walk :as w]
            [clojure.tools.decompiler.bc :as bc]
            [clojure.tools.decompiler.ast :as ast]
            [clojure.tools.decompiler.sugar :as sa]
            [clojure.tools.decompiler.source :as src]
            [clojure.tools.decompiler.compact :as cmp]
            [clojure.tools.decompiler.pprint :as pp]))

(defn absolute-filename [filename]
  (-> filename
      (io/file)
      (.getAbsolutePath)))

(declare preinterned-var-symbols-before-namespace-load)

(defn class->clj [classfile-or-classname bc-for lenient?]
  (let [class-bc (bc/analyze-class classfile-or-classname)
        preinterned-vars
        (preinterned-var-symbols-before-namespace-load class-bc bc-for)]
    (-> class-bc
        (ast/bc->ast {:bc-for bc-for :lenient? lenient?})
        (sa/ast->sugared-ast)
        (src/ast->clj)
        (cmp/macrocompact preinterned-vars))))

(defn class->source [classfile-or-classname bc-for lenient?]
  (pp/pprint (class->clj classfile-or-classname bc-for lenient?)))

(defn form-head-name [form]
  (when (and (seq? form) (symbol? (first form)))
    (name (first form))))

(defn quoted-symbol [form]
  (when (and (seq? form)
             (= "quote" (form-head-name form))
             (symbol? (second form)))
    (second form)))

(defn protocol-names [form]
  (->> (tree-seq coll? seq form)
       (keep (fn [node]
               (when (and (= "defprotocol" (form-head-name node))
                          (symbol? (second node)))
                 (name (second node)))))
       set))

(defn imported-class [form]
  (when (= "import" (form-head-name form))
    (quoted-symbol (second form))))

(defn interface-method-form [{:method/keys [name return-type arg-types flags]}]
  (when (and (not (:static flags))
             (not (.startsWith ^String name "<")))
    (let [method-name (with-meta (symbol name) {:tag (src/type-tag return-type)})
          args (mapv (fn [index type]
                       (with-meta (symbol (str "arg" index))
                         {:tag (src/type-tag type)}))
                     (range)
                     arg-types)]
      (list method-name args))))

(defn interface-form [class-bc]
  (let [simple-name (-> (:class/name class-bc) (s/split #"\.") last symbol)]
    (list* 'definterface
           simple-name
           (keep interface-method-form (:class/methods class-bc)))))

(defn restore-definterfaces
  "Replace the original JAR's hidden dependency on plain definterface AOT
  classes with source definitions reconstructed from their exact descriptors.
  Protocol interfaces are already recreated by defprotocol and are skipped."
  [form ns-class-prefix classname->path]
  (let [protocols (protocol-names form)]
    (loop [remaining (rest form)
           emitted #{}
           output [(first form)]]
      (if (empty? remaining)
        (apply list output)
        (let [statement (first remaining)
              more (rest remaining)
              class-symbol (imported-class statement)
              class-name (some-> class-symbol str)
              simple-name (some-> class-name (s/split #"\.") last)
              class-path (when (and class-name
                                    (.startsWith ^String class-name
                                                 (str ns-class-prefix ".")))
                           (classname->path (s/replace class-name "." "/")))
              class-bc (when class-path (bc/analyze-class class-path))
              restore? (and (= :interface (:class/type class-bc))
                            (not (contains? protocols simple-name))
                            (not (contains? emitted class-name)))]
          (recur more
                 (cond-> emitted restore? (conj class-name))
                 (cond-> output
                   restore? (conj (interface-form class-bc))
                   true (conj statement))))))))

(defn string-call-at?
  [bytecode index target-class target-name]
  (let [instruction (nth bytecode index nil)
        next-instruction (nth bytecode (inc index) nil)
        pool-element (:insn/pool-element instruction)
        next-pool-element (:insn/pool-element next-instruction)]
    (and (= "ldc" (:insn/name instruction))
         (= "java.lang.String" (:insn/target-type pool-element))
         (= "invokestatic" (:insn/name next-instruction))
         (= target-class (:insn/target-class next-pool-element))
         (= target-name (:insn/target-name next-pool-element)))))

(defn interned-symbol-events [bytecode]
  (->> (range (count bytecode))
       (keep (fn [index]
               (when (string-call-at? bytecode index
                                      "clojure.lang.Symbol" "intern")
                 {:index index
                  :value (get-in bytecode
                                 [index :insn/pool-element
                                  :insn/target-value])})))
       vec))

(defn literal-string-at [bytecode index]
  (let [instruction (nth bytecode index nil)
        pool-element (:insn/pool-element instruction)]
    (when (and (contains? #{"ldc" "ldc_w"} (:insn/name instruction))
               (= "java.lang.String" (:insn/target-type pool-element))
               (string? (:insn/target-value pool-element)))
      (:insn/target-value pool-element))))

(defn exact-call?
  [instruction instruction-name target-class target-name
   argument-types return-type]
  (let [pool-element (:insn/pool-element instruction)]
    (and (= instruction-name (:insn/name instruction))
         (= target-class (:insn/target-class pool-element))
         (= target-name (:insn/target-name pool-element))
         (= argument-types (:insn/target-arg-types pool-element))
         (= return-type (:insn/target-ret-type pool-element)))))

(defn exact-static-call?
  [instruction target-class target-name argument-types return-type]
  (exact-call? instruction "invokestatic" target-class target-name
               argument-types return-type))

(defn rt-var-symbol-at [bytecode index]
  (when (exact-static-call? (nth bytecode index nil)
                            "clojure.lang.RT" "var"
                            ["java.lang.String" "java.lang.String"]
                            "clojure.lang.Var")
    (let [namespace-name (literal-string-at bytecode (- index 2))
          var-name (literal-string-at bytecode (dec index))]
      (when (and namespace-name var-name)
        (symbol namespace-name var-name)))))

(defn rt-class-for-name-at [bytecode index]
  (when (exact-static-call? (nth bytecode index nil)
                            "clojure.lang.RT" "classForName"
                            ["java.lang.String"] "java.lang.Class")
    (literal-string-at bytecode (dec index))))

(def namespace-static-initializer-pattern #"__init[0-9]+")

(def control-transfer-instruction-names
  #{"ifeq" "ifne" "iflt" "ifge" "ifgt" "ifle"
    "if_icmpeq" "if_icmpne" "if_icmplt" "if_icmpge"
    "if_icmpgt" "if_icmple" "if_acmpeq" "if_acmpne"
    "ifnull" "ifnonnull" "goto" "goto_w" "jsr" "jsr_w" "ret"
    "tableswitch" "lookupswitch" "ireturn" "lreturn" "freturn"
    "dreturn" "areturn" "return" "athrow"})

(defn control-transfer-instruction? [instruction]
  (contains? control-transfer-instruction-names (:insn/name instruction)))

(defn exact-zero-arity-void-method [class-bc method-name]
  (some #(when (and (= method-name (:method/name %))
                    (= [] (:method/arg-types %))
                    (= "void" (:method/return-type %)))
           %)
        (:class/methods class-bc)))

(defn straight-line-void-method-bytecode
  "Return bytecode only for an exact zero-arity void method whose execution is
  a single straight-line path ending in its sole return. Exception handlers,
  branches, switches, early returns, and throws fail closed."
  [method]
  (let [bytecode (vec (:method/bytecode method))]
    (when (and method
               (= [] (:method/arg-types method))
               (= "void" (:method/return-type method))
               (empty? (:method/exception-table method))
               (seq bytecode)
               (= "return" (:insn/name (peek bytecode)))
               (not-any? control-transfer-instruction? (pop bytecode)))
      bytecode)))

(defn exception-handlers-start-at-or-after?
  [method instruction]
  (let [exception-table (:method/exception-table method)]
    (or (empty? exception-table)
        (let [boundary-label (:insn/label instruction)]
          (and (number? boundary-label)
               (every? (fn [handler]
                         (let [start-label
                               (:exception-handler/start-label handler)]
                           (and (number? start-label)
                                (>= start-label boundary-label))))
                       exception-table))))))

(defn pre-load-execution-bytecode
  "Inline every admitted `__initN` helper at its exact `<clinit>` call site so
  the returned vector represents execution order up to, but excluding,
  namespace `load`. Any helper that is not a proved straight-line method makes
  the trace unavailable."
  [class-bc]
  (let [class-name (:class/name class-bc)
        clinit (exact-zero-arity-void-method class-bc "<clinit>")
        clinit-bytecode (vec (:method/bytecode clinit))
        load-indexes
        (keep-indexed
          (fn [index instruction]
            (when (exact-static-call? instruction class-name "load" [] "void")
              index))
          clinit-bytecode)]
    (when (= 1 (count load-indexes))
      (let [load-index (first load-indexes)
            load-instruction (nth clinit-bytecode load-index)
            prefix (subvec clinit-bytecode 0 load-index)]
        (when (and (not-any? control-transfer-instruction? prefix)
                   (exception-handlers-start-at-or-after?
                     clinit load-instruction))
          (reduce
            (fn [trace instruction]
              (let [pool-element (:insn/pool-element instruction)
                    helper-name (:insn/target-name pool-element)
                    helper-call?
                    (and (string? helper-name)
                         (exact-static-call? instruction class-name
                                              helper-name [] "void")
                         (re-matches namespace-static-initializer-pattern
                                     helper-name))]
                (if helper-call?
                  (if-let [helper-bytecode
                           (some-> (exact-zero-arity-void-method
                                     class-bc helper-name)
                                   straight-line-void-method-bytecode)]
                    (into trace helper-bytecode)
                    (reduced nil))
                  (conj trace instruction))))
            [] prefix))))))

(def mapping-preserving-tail-instruction-names
  #{"aconst_null" "iconst_0" "iconst_1" "iconst_2" "iconst_3"
    "iconst_4" "iconst_5" "bipush" "ldc" "ldc_w" "ldc2_w"
    "anewarray" "dup" "aastore"
    "checkcast" "putstatic" "new" "invokestatic" "invokespecial"
    "invokevirtual" "return"})

(defn exact-own-static-field-write? [class-name instruction]
  (let [pool-element (:insn/pool-element instruction)]
    (and (= "putstatic" (:insn/name instruction))
         (= class-name (:insn/target-class pool-element))
         (string? (:insn/target-name pool-element)))))

(defn exact-var-checkcast? [instruction]
  (and (= "checkcast" (:insn/name instruction))
       (= "clojure/lang/Var"
          (get-in instruction [:insn/pool-element :insn/target-value]))))

(defn mapping-preserving-runtime-call? [instruction]
  (or (exact-static-call? instruction
                          "clojure.lang.RT" "var"
                          ["java.lang.String" "java.lang.String"]
                          "clojure.lang.Var")
      (exact-static-call? instruction
                          "clojure.lang.RT" "keyword"
                          ["java.lang.String" "java.lang.String"]
                          "clojure.lang.Keyword")
      (exact-static-call? instruction
                          "clojure.lang.RT" "map"
                          ["java.lang.Object[]"]
                          "clojure.lang.IPersistentMap")
      (exact-static-call? instruction
                          "clojure.lang.RT" "vector"
                          ["java.lang.Object[]"]
                          "clojure.lang.IPersistentVector")
      (exact-static-call? instruction
                          "java.lang.Long" "valueOf"
                          ["long"] "java.lang.Long")
      (exact-static-call? instruction
                          "java.lang.Integer" "valueOf"
                          ["int"] "java.lang.Integer")
      (exact-static-call? instruction
                          "clojure.lang.Symbol" "intern"
                          ["java.lang.String" "java.lang.String"]
                          "clojure.lang.Symbol")
      (let [pool-element (:insn/pool-element instruction)]
        (and (= "invokestatic" (:insn/name instruction))
             (= "clojure.lang.Tuple" (:insn/target-class pool-element))
             (= "create" (:insn/target-name pool-element))
             (contains? #{["java.lang.Object"]
                          ["java.lang.Object" "java.lang.Object"]}
                        (:insn/target-arg-types pool-element))
             (= "clojure.lang.IPersistentVector"
                (:insn/target-ret-type pool-element))))
      (exact-static-call? instruction
                          "java.util.Arrays" "asList"
                          ["java.lang.Object[]"] "java.util.List")
      (exact-static-call? instruction
                          "clojure.lang.PersistentList" "create"
                          ["java.util.List"]
                          "clojure.lang.IPersistentList")
      (exact-static-call? instruction
                          "clojure.lang.PersistentHashSet" "create"
                          ["java.lang.Object[]"]
                          "clojure.lang.PersistentHashSet")
      (exact-call? instruction "invokespecial"
                   "clojure.lang.KeywordLookupSite" "<init>"
                   ["clojure.lang.Keyword"] "void")
      (exact-call? instruction "invokevirtual"
                   "java.lang.Class" "getClassLoader" []
                   "java.lang.ClassLoader")
      (exact-static-call? instruction
                          "clojure.lang.Compiler" "pushNSandLoader"
                          ["java.lang.ClassLoader"] "void")))

(defn inert-constant-instruction? [instruction]
  (let [instruction-name (:insn/name instruction)
        pool-element (:insn/pool-element instruction)
        target-type (:insn/target-type pool-element)
        target-value (:insn/target-value pool-element)]
    (case instruction-name
      ("ldc" "ldc_w")
      (and (= "java.lang.String" target-type)
           (string? target-value))

      "ldc2_w"
      (and (= "long" target-type)
           (instance? Long target-value))

      false)))

(defn mapping-preserving-compiler-tail-instruction?
  [class-name instruction]
  (and (contains? mapping-preserving-tail-instruction-names
                  (:insn/name instruction))
       (case (:insn/name instruction)
         "invokestatic" (mapping-preserving-runtime-call? instruction)
         "invokespecial" (mapping-preserving-runtime-call? instruction)
         "invokevirtual" (mapping-preserving-runtime-call? instruction)
         "putstatic" (exact-own-static-field-write? class-name instruction)
         "new" (= "clojure/lang/KeywordLookupSite"
                  (get-in instruction
                          [:insn/pool-element :insn/target-value]))
         "ldc" (inert-constant-instruction? instruction)
         "ldc_w" (inert-constant-instruction? instruction)
         "ldc2_w" (inert-constant-instruction? instruction)
         true)))

(defn persistent-static-var-symbol-at
  "Return the exact RT.var symbol only when the compiler stores it in this
  class's own static field and every remaining instruction is from a narrow
  mapping-preserving constant-initializer grammar. An unmap/remove call or any
  arbitrary helper therefore invalidates the evidence."
  [class-name bytecode index]
  (when-let [var-symbol (rt-var-symbol-at bytecode index)]
    (when (and (exact-var-checkcast? (nth bytecode (inc index) nil))
               (exact-own-static-field-write?
                 class-name (nth bytecode (+ index 2) nil))
               (every? #(mapping-preserving-compiler-tail-instruction?
                          class-name %)
                       (subvec (vec bytecode) (+ index 3))))
      var-symbol)))

(defn mapping-preserving-class-initializer? [class-bc]
  (when-let [bytecode
             (some-> (exact-zero-arity-void-method class-bc "<clinit>")
                     straight-line-void-method-bytecode)]
    (every? #(mapping-preserving-compiler-tail-instruction?
               (:class/name class-bc) %)
            bytecode)))

(defn mapping-preserving-pre-load-suffix?
  [namespace-class-name bytecode start-index bc-for]
  (loop [index start-index]
    (if (>= index (count bytecode))
      true
      (let [instruction (nth bytecode index)]
        (if-let [initialized-class-name
                 (rt-class-for-name-at bytecode index)]
          (if (or (= namespace-class-name initialized-class-name)
                  (some-> (bc-for initialized-class-name)
                          mapping-preserving-class-initializer?))
            (recur (inc index))
            false)
          (if (mapping-preserving-compiler-tail-instruction?
                namespace-class-name instruction)
            (recur (inc index))
            false))))))

(defn persistent-literal-class-initialization-targets
  [namespace-class-name bytecode bc-for]
  (into #{}
        (keep (fn [index]
                (when-let [class-name
                           (rt-class-for-name-at bytecode index)]
                  (when (mapping-preserving-pre-load-suffix?
                          namespace-class-name bytecode (inc index) bc-for)
                    class-name))))
        (range (count bytecode))))

(defn class-static-var-symbols [class-bc]
  (if-let [bytecode
           (some-> (exact-zero-arity-void-method class-bc "<clinit>")
                   straight-line-void-method-bytecode)]
    (into #{}
          (keep #(persistent-static-var-symbol-at
                   (:class/name class-bc) bytecode %))
          (range (count bytecode)))
    #{}))

(defn preinterned-var-symbols-before-namespace-load
  "Recover Vars proved to exist before a namespace initializer's `load`
  method (and therefore before its first `in-ns` form). Evidence is limited to
  exact literal RT.classForName targets on branch-free pre-load paths. A
  target's `<clinit>` must be straight-line with a sole final return, must
  store the exact RT.var result in its own static field, and must have only a
  narrow mapping-preserving compiler-constant tail after that store. Direct
  pre-load RT.var calls are deliberately excluded unless an equivalent
  persistence proof is added. Missing classes, nonliteral calls, wrong
  identities, branches, switches, early exits, exception handlers, arbitrary
  tail calls, unmap/remove calls, and post-load helpers fail closed. Every
  instruction actually executed after the target classForName through the
  `load` boundary must also be mapping-preserving; later classForName targets
  require their own proved constant-only class initializer."
  [class-bc bc-for]
  (let [execution-bytecode (pre-load-execution-bytecode class-bc)
        initialized-classes
        (persistent-literal-class-initialization-targets
          (:class/name class-bc) execution-bytecode bc-for)
        class-vars
        (into #{}
              (mapcat (fn [class-name]
                        (if-let [initialized-class (bc-for class-name)]
                          (class-static-var-symbols initialized-class)
                          #{})))
              initialized-classes)]
    class-vars))

(defn basis-field-tag [bytecode symbol-events start-index end-index]
  (let [tag-markers
        (->> (range (inc start-index) end-index)
             (filter (fn [index]
                       (and (string-call-at? bytecode index
                                             "clojure.lang.RT" "keyword")
                            (= "tag"
                               (get-in bytecode
                                       [index :insn/pool-element
                                        :insn/target-value])))))
             vec)]
    (when (> (count tag-markers) 1)
      (throw (ex-info "multiple :tag entries in defrecord getBasis field"
                      {:start-index start-index
                       :end-index end-index
                       :tag-markers tag-markers})))
    (when-let [tag-marker (first tag-markers)]
      (if-let [tag-event
               (first (filter #(< tag-marker (:index %) end-index)
                              symbol-events))]
        (symbol (:value tag-event))
        (throw (ex-info "missing :tag symbol in defrecord getBasis field"
                        {:start-index start-index
                         :end-index end-index
                         :tag-marker tag-marker}))))))

(defn record-basis-symbols
  "Recover the source-level field symbols embedded by defrecord's static
  getBasis method.  JVM field names are munged and cannot distinguish, for
  example, `fressian-tag` from `fressian_tag`; getBasis retains the authored
  spelling used for record lookup keys."
  [class-bc]
  (when-let [basis-method
             (some (fn [method]
                     (when (and (= "getBasis" (:method/name method))
                                (contains? (:method/flags method) :static)
                                (empty? (:method/arg-types method)))
                       method))
                   (:class/methods class-bc))]
    (let [reserved-fields #{"__meta" "__extmap" "__hash" "__hasheq"}
          jvm-fields (->> (:class/fields class-bc)
                          (remove #(contains? (:field/flags %) :static))
                          (remove #(reserved-fields (:field/name %)))
                          (mapv :field/name))
          bytecode (:method/bytecode basis-method)
          symbol-events (interned-symbol-events bytecode)
          field-events
          (reduce (fn [result event]
                    (let [jvm-name (clojure.lang.Compiler/munge
                                     (:value event))]
                      (if (and (some #{jvm-name} jvm-fields)
                               (not (some #(= jvm-name (:jvm-name %))
                                          result)))
                        (conj result (assoc event :jvm-name jvm-name))
                        result)))
                  []
                  symbol-events)]
      (when-not (= (count jvm-fields) (count field-events))
        (throw (ex-info "defrecord JVM fields differ from getBasis"
                        {:jvm-fields jvm-fields
                         :basis-events field-events})))
      (mapv (fn [event next-event]
              (let [start-index (:index event)
                    end-index (or (:index next-event) (count bytecode))
                    field-symbol (symbol (:value event))]
                (if-let [tag (basis-field-tag bytecode symbol-events
                                              start-index end-index)]
                  (with-meta field-symbol {:tag tag})
                  field-symbol)))
            field-events
            (concat (rest field-events) [nil])))))

(defn restore-record-form-fields [form basis]
  (let [fields (nth form 2)
        field-symbols (mapv #(with-meta % nil) fields)]
    (when-not (= (count fields) (count basis))
      (throw (ex-info "defrecord field count differs from getBasis"
                      {:record (second form)
                       :fields field-symbols
                       :basis basis})))
    (let [renames (into {}
                        (map (fn [old-field basis-field]
                               [old-field (with-meta basis-field nil)])
                             field-symbols basis))
          renamed
          (w/postwalk
            (fn [node]
              (if-let [replacement (and (symbol? node)
                                        (renames (with-meta node nil)))]
                (with-meta replacement (meta node))
                node))
            form)
          restored-fields
          (mapv (fn [old-field basis-field]
                  (with-meta (with-meta basis-field nil)
                    (merge (meta basis-field) (meta old-field))))
                fields basis)]
      (with-meta
        (apply list (concat (take 2 renamed)
                            [restored-fields]
                            (drop 3 renamed)))
        (meta renamed)))))

(defn restore-defrecord-field-names
  "Restore authored record field spelling from each generated class's
  getBasis method after deftype* has been compacted back to defrecord."
  [form ns-class-prefix classname->path]
  (w/postwalk
    (fn [node]
      (if (and (seq? node)
               (= "defrecord" (form-head-name node))
               (symbol? (second node))
               (vector? (nth node 2 nil)))
        (let [class-name (str ns-class-prefix "." (name (second node)))
              class-path (classname->path (s/replace class-name "." "/"))
              class-bc (when class-path (bc/analyze-class class-path))
              basis (record-basis-symbols class-bc)]
          (when-not (some? basis)
            (throw (ex-info "missing defrecord getBasis method"
                            {:record class-name :class-path class-path})))
          (restore-record-form-fields node basis))
        node))
    form))

(defn cname [c input-path]
  (-> c
      (subs 0 (- (count c) (count ".class")))
      (subs (inc (count input-path)))))

(defn classfile? [^String f]
  (.endsWith f ".class"))

(defn bc-for [classname->path]
  (fn [classname]
    ;; A constructor target is not necessarily one of the Clojure classes we
    ;; are reconstructing.  BCEL 6.1 also cannot parse modern constant-pool
    ;; entries (notably invokedynamic) in some third-party classes.  Treat
    ;; either case as "no nested Clojure bytecode" instead of aborting the
    ;; namespace that merely references the class.
    (try
      (some-> classname
              (s/replace "." "/")
              classname->path
              absolute-filename
              bc/analyze-class)
      (catch org.apache.bcel.classfile.ClassFormatException _
        nil))))

(defn decompile-classfiles [{:keys [input-path output-path ?only-classes lenient?]}]
  (let [files (filter classfile? (map str (file-seq (io/file input-path))))
        classname->path (into {} (map (fn [^String classfile]
                                        [(cname classfile input-path) classfile])
                                      files))
        inits (if ?only-classes
                (mapv classname->path ?only-classes)
                (filter (fn [^String i] (.endsWith i "__init.class")) files))]

    (doseq [init inits]
      (let [cname (cname init input-path)
            ns-name (subs cname 0 (- (count cname) (count "__init")))
            ns-file (str output-path "/" (s/replace ns-name "." "/") ".clj")]
        (println (str "Decompiling " init (when output-path (str " to " ns-file))))
        (let [source-form (class->clj (absolute-filename init) (bc-for classname->path) lenient?)
              source-form (restore-definterfaces source-form
                                                 (s/replace ns-name "/" ".")
                                                 classname->path)
              source-form (restore-defrecord-field-names source-form
                                                         (s/replace ns-name "/" ".")
                                                         classname->path)
              source (pp/pprint source-form)]
          (if output-path
            (do (io/make-parents ns-file)
                (spit ns-file source))
            (println source)))))))

(defn decompile-classes [{:keys [classes lenient?] :or {lenient? true}}]
  (doseq [class classes]
    (println "Decompiling" class)
    (let [source (class->source (s/replace class "." "/")
                                (fn [^String classname]
                                  (when-not (.startsWith classname "clojure.lang.")
                                    (bc/analyze-class (s/replace classname "." "/"))))
                                lenient?)]
      (println source))))

(comment
  (decompile-classfiles
   {:input-path "classes/"
    :output-path "src/"
    :?only-classes ["my/ns__init"]
    :lenient? true}))

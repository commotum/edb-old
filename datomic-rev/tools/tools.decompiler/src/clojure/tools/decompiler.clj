;;   Copyright (c) Nicola Mometto & contributors.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

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

(defn class->clj [classfile-or-classname bc-for lenient?]
  (-> classfile-or-classname
      (bc/analyze-class)
      (ast/bc->ast {:bc-for bc-for :lenient? lenient?})
      (sa/ast->sugared-ast)
      (src/ast->clj)
      (cmp/macrocompact)))

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

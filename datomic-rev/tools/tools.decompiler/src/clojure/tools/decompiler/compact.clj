;;   Copyright (c) Nicola Mometto & contributors.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns clojure.tools.decompiler.compact
  (:require [clojure.core.match :as m]
            [clojure.core.match.protocols :as mp]
            [clojure.set :as set]
            [clojure.string :as s]
            [clojure.walk :as w])
  (:import (clojure.core.match LeafNode FailNode BindNode SwitchNode)
           clojure.core.match.protocols.IPatternCompile
           clojure.lang.ExceptionInfo))

(defn compact-sequential-destructuring [binds]
  (loop [[[b v :as bind] & binds] (partition 2 binds)
         ret []]
    (cond
      (not bind)
      ret

      (and (symbol? b)
           (.startsWith (name b) "seq__")
           (seq? v)
           (= `seq (first v)))
      (let [init (second v)
            placeholder b
            [bind binds] (loop [[[b v :as bind] & binds :as curr] binds ret []]

                           (cond
                             (and (symbol? b)
                                  (.startsWith (name b) "first__"))
                             (recur binds (conj ret b))

                             (and (symbol? v)
                                  (.startsWith (name v) "first__"))
                             (recur binds (replace {v b} ret))

                             (= v placeholder)
                             [[(conj ret '& b) init] binds]

                             (= b placeholder)
                             (recur binds ret)

                             :else
                             [[(conj ret '& placeholder) init] curr]))]
        (into (into ret bind) (mapcat identity binds)))

      :else
      (recur binds (conj ret b v)))))

(defn compact-vec-destructuring [binds]
  (loop [[[b v :as bind] & binds] (partition 2 binds)
         ret []]
    (cond
      (not bind)
      ret

      (and (symbol? b)
           (.startsWith (name b) "vec__"))
      (let [init v
            placeholder b
            [bind binds] (loop [[[b v :as bind] & binds :as curr] binds ret []]

                           (cond

                             (= v placeholder)
                             (if (symbol? b)
                               [[(conj ret :as b) init] binds]
                               (recur binds b))

                             (and (seq? v)
                                  (= `nth (first v))
                                  (= placeholder (second v)))
                             (recur binds (conj ret b))

                             (and (seq? v)
                                  (= `nthnext (first v))
                                  (= placeholder (second v)))
                             [[(conj ret '& b) init] binds]

                             :else
                             [[ret init] curr]))]
        (into (into ret bind) (mapcat identity binds)))

      :else
      (recur binds (conj ret b v)))))

(defn simplify-map-destructuring [m]
  (let [{ks true opts false} (group-by (comp keyword? val) m)
        {ks true oths false} (group-by #(and (not (namespace (val %)))
                                             (and (keyword? (val %))
                                                  (symbol? (key %))
                                                  (= (name (val %))
                                                     (name (key %))))) ks)]
    (-> {}
        (cond-> (seq ks)
          (conj [:keys (mapv key ks)]))
        (into opts)
        (into oths))))

(defn compact-associative-destructuring [binds]
  (loop [[[b v :as bind] & binds] (partition 2 binds)
         ret []]
    (cond
      (not bind)
      ret

      (and (symbol? b)
           (.startsWith (name b) "map__"))
      (let [init v
            placeholder b
            ;; Recent Clojure compilers first shadow map__N with a normalized
            ;; persistent map and only then emit the keyword lookups. Skip
            ;; that same-name normalization binding; it is implementation
            ;; scaffolding, not an empty source destructuring map.
            nested-destructuring (when (map? (-> binds first first)) (first binds))
            remaining (if (= placeholder (-> binds first first))
                        (rest binds)
                        binds)
            [bind binds] (if nested-destructuring
                           [[(first nested-destructuring) init] (rest binds)]
                           (loop [[[b v :as bind] & binds :as curr] remaining ret {}]

                             (cond

                               (= v placeholder)
                               (recur binds (assoc ret :as b))

                               (and (seq? v)
                                    (= `get (first v))
                                    (= placeholder (second v)))
                               (let [k (nth v 2)
                                     ?or (and (= 4 (count v)) (nth v 3))]
                                 (recur binds (cond-> (assoc ret b k)
                                                ?or (assoc-in [:or b] ?or))))

                               :else
                               [[(simplify-map-destructuring ret) init] curr])))]
        (into (into ret bind) (mapcat identity binds)))

      :else
      (recur binds (conj ret b v)))))

(defn placeholder-symbols [prefix binds]
  (->> (take-nth 2 binds)
       (filter #(and (symbol? %) (s/starts-with? (name %) prefix)))
       set))

(defn symbols-in [forms]
  (->> (tree-seq coll? seq forms)
       (filter symbol?)
       set))

(defn symbol-occurs? [sym form]
  (contains? (symbols-in form) sym))

(defn compiler-lock-temp?
  "True only for the locals emitted by clojure.core/locking's macroexpansion.
  Keeping this test narrow prevents an arbitrary hand-written monitor pair
  from being rewritten as a source-level locking form."
  [kind value]
  (and (symbol? value)
       (nil? (namespace value))
       (boolean
         (re-matches (re-pattern (str kind "__[0-9]+__auto__"))
                     (name value)))))

(defn unqualified-call?
  [operation form]
  (and (seq? form)
       (= (symbol operation) (first form))))

(defn monitor-call?
  [operation lock-local form]
  (and (unqualified-call? operation form)
       (= 2 (count form))
       (= lock-local (second form))))

(defn matching-monitor-finally?
  [lock-local form]
  (when (and (unqualified-call? "finally" form)
             (= 2 (count form)))
    (let [body (second form)]
      (or (monitor-call? "monitor-exit" lock-local body)
          (and (unqualified-call? "do" body)
               (= 3 (count body))
               (monitor-call? "monitor-exit" lock-local (second body))
               (nil? (nth body 2)))))))

(defn recover-locking-form
  "Recover the exact compiler expansion of clojure.core/locking. The strict
  lockee/locklocal and matching-enter/exit checks are important: a partial
  monitor pattern is not safe to compact."
  [form]
  (when (and (unqualified-call? "let" form)
             (= 4 (count form))
             (vector? (second form)))
    (let [[lockee lock lock-local lockee-source :as bindings] (second form)
          enter-form (nth form 2)
          try-form (nth form 3)
          finally-form (when (unqualified-call? "try" try-form)
                         (last try-form))
          try-body (butlast (rest try-form))]
      (when (and (= 4 (count bindings))
                 (compiler-lock-temp? "lockee" lockee)
                 (compiler-lock-temp? "locklocal" lock-local)
                 (= lockee lockee-source)
                 (monitor-call? "monitor-enter" lock-local enter-form)
                 (not-any? #(or (unqualified-call? "catch" %)
                                (unqualified-call? "finally" %))
                           try-body)
                 (matching-monitor-finally? lock-local finally-form))
        (list* 'locking lock try-body)))))

(defn captured-compiler-temp-binding?
  "Detect a compiler `temp__N__auto__` let binding that is mentioned beyond
  the expansion's test and source-binding alias. Assertion-like macros retain
  these locals in a quoted bindings map, so reconstructing when-let/if-let
  would otherwise erase a still-live symbol."
  [form]
  (when (and (seq? form)
             (symbol? (first form))
             (= "let" (name (first form)))
             (vector? (second form)))
    (let [body (drop 2 form)]
      (some (fn [[binding _]]
              (and (symbol? binding)
                   (s/starts-with? (name binding) "temp__")
                   (< 2 (count (filter #{binding}
                                       (tree-seq coll? seq body))))))
            (partition 2 (second form))))))

(defn compact-bindings-unless-referenced [prefix compact-fn binds body]
  (let [compacted (compact-fn binds)]
    (if (seq (set/intersection (placeholder-symbols prefix binds)
                               (symbols-in [compacted body])))
      binds
      compacted)))

(defn remove-defrecord-methods [methods]
  (for [[name :as method] methods
        :when (not ('#{hasheq hashCode equals meta withMeta valAt getLookupThunk
                       count empty cons equiv containsKey entryAt seq iterator
                       assoc without size isEmpty containsValue get put
                       remove putAll clear keySet values entrySet}
                    name))]
    method))

(defn remove-defrecord-interfaces [interfaces]
  (remove '#{clojure.lang.IHashEq
             clojure.lang.IRecord
             clojure.lang.IObj
             clojure.lang.ILookup
             clojure.lang.IKeywordLookup
             clojure.lang.IPersistentMap
             java.util.Map
             java.io.Serializable}
    interfaces))

(defn defrecord-interfaces [interfaces methods]
  (let [interfaces (vec (remove-defrecord-interfaces interfaces))]
    ;; Object methods are legal defrecord extensions only when Object is
    ;; explicitly named among the implemented bases.
    (if (some (comp #{'toString 'clone 'finalize} first) methods)
      (conj interfaces 'java.lang.Object)
      interfaces)))

(defn deftype-interfaces [interfaces methods]
  ;; Clojure's deftype syntax requires Object before any overridden Object
  ;; methods. Object is a superclass, so it is absent from the classfile's
  ;; interface table and must be reconstructed explicitly.
  (let [interfaces (vec (remove #{'clojure.lang.IType} interfaces))]
    (if (and (some (comp #{'equals 'hashCode 'toString 'clone 'finalize} first)
                   methods)
             (not (some #{'java.lang.Object 'Object} interfaces)))
      (into ['java.lang.Object] interfaces)
      interfaces)))

(defn remove-defrecord-fields [fields]
  (vec (remove '#{__meta __extmap __hash __hasheq}  fields)))

(defn simple-class-symbol [class-symbol]
  (-> class-symbol name (s/split #"\.") last symbol))

(defn simplify-self-references [class-symbol forms]
  (let [qualified-ctor (symbol (str class-symbol "."))
        simple-class (simple-class-symbol class-symbol)
        simple-ctor (symbol (str simple-class "."))]
    (w/postwalk
      (fn [form]
        (cond
          (= qualified-ctor form)
          simple-ctor

          (and (seq? form)
               (symbol? (first form))
               (= "instance?" (name (first form)))
               (= class-symbol (second form)))
          (with-meta (list* (first form) simple-class (drop 2 form))
                     (meta form))

          :else form))
      forms)))

(defn quoted-symbol-value [form]
  (when (and (seq? form)
             (= 'quote (first form))
             (symbol? (second form)))
    (second form)))

(defn normalize-refer-filters [filters]
  (mapcat (fn [[option names-form]]
            [option
             (->> (tree-seq coll? seq names-form)
                  (keep quoted-symbol-value)
                  vec)])
          (partition 2 filters)))

(defn protocol-arg-symbol [form]
  (loop [form form]
    (if (and (seq? form)
             (symbol? (first form))
             (contains? #{"quote" ".withMeta"} (name (first form))))
      (recur (second form))
      form)))

(defn no-matching-clause-default? [form]
  (boolean
    (some #(and (string? %)
                (s/starts-with? % "No matching clause:"))
          (tree-seq coll? seq form))))

(defn call-name [form]
  (when (and (seq? form) (symbol? (first form)))
    (name (first form))))

(defn var-symbol [form]
  (when (and (seq? form)
             (contains? #{'var 'clojure.core/var} (first form))
             (symbol? (second form)))
    (second form)))

(defn dynamic-var-symbol [form]
  (when (and (seq? form)
             (= '.setDynamic (first form))
             (= true (nth form 2 nil)))
    (var-symbol (second form))))

(defn target-var-symbol [form]
  (or (var-symbol form) (dynamic-var-symbol form)))

(defn reset-meta-parts [form]
  (when (and (seq? form)
             (contains? #{'reset-meta! 'clojure.core/reset-meta!}
                        (first form)))
    [(second form) (nth form 2 nil)]))

(defn bind-root-parts [form]
  (when (and (seq? form) (= '.bindRoot (first form)))
    [(second form) (nth form 2 nil)]))

(defn set-meta-parts [form]
  (when (and (seq? form) (= '.setMeta (first form)))
    [(second form) (nth form 2 nil)]))

(defn quoted-symbol [form]
  (let [form (loop [form form]
               (if (and (seq? form)
                        (contains? #{'quote '.withMeta} (first form)))
                 (recur (second form))
                 form))]
    (when (symbol? form) form)))

(defn in-ns-symbol [form]
  (when (and (seq? form)
             (contains? #{'in-ns 'clojure.core/in-ns} (first form)))
    (quoted-symbol (second form))))

(defn canonical-var-symbol [current-ns var-name]
  (when var-name
    (if (namespace var-name)
      var-name
      (if current-ns
        (symbol (str current-ns) (name var-name))
        var-name))))

(defn namespace-after [current-ns forms]
  (reduce (fn [namespace form]
            (or (in-ns-symbol form) namespace))
          current-ns
          forms))

(defn literal-arglists [form]
  (let [form (loop [form form]
               (if (and (seq? form)
                        (contains? #{'quote '.withMeta} (first form)))
                 (recur (second form))
                 form))]
    (cond
      (and (sequential? form) (every? vector? form))
      (vec form)

      (and (= 'clojure.core/list (first form))
           (every? vector? (rest form)))
      (vec (rest form))

      :else
      nil)))

(defn parameter-arity-shape [parameters]
  (when (vector? parameters)
    (let [ampersands (keep-indexed
                       (fn [index parameter]
                         (when (= '& (protocol-arg-symbol parameter)) index))
                       parameters)]
      (cond
        (empty? ampersands)
        [:fixed (count parameters)]

        (and (= 1 (count ampersands))
             (= (first ampersands) (- (count parameters) 2)))
        [:variadic (first ampersands)]

        :else
        nil))))

(defn fn-parameter-vectors [body]
  (cond
    (vector? (first body))
    [(first body)]

    (and (seq body)
         (every? #(and (seq? %) (vector? (first %))) body))
    (mapv first body)

    :else
    nil))

(defn function-form? [form]
  (and (seq? form)
       (contains? #{'fn 'fn* 'clojure.core/fn} (first form))))

(declare normalize-argument-form)

(defn normalize-argument-metadata [metadata]
  (when (seq metadata)
    (into {}
          (map (fn [[k v]]
                 [(normalize-argument-form k)
                  (normalize-argument-form v)]))
          metadata)))

(defn normalize-argument-form
  ([form]
   (normalize-argument-form form (meta form)))
  ([form explicit-metadata]
   (cond
     (and (seq? form) (= 'quote (first form)) (= 2 (count form)))
     (normalize-argument-form (second form) explicit-metadata)

     (and (seq? form)
          (= '.withMeta (first form))
          (= 3 (count form))
          (map? (nth form 2)))
     (normalize-argument-form (second form) (nth form 2))

     (symbol? form)
     [:symbol form (normalize-argument-metadata explicit-metadata)]

     (vector? form)
     [:vector (mapv normalize-argument-form form)
      (normalize-argument-metadata explicit-metadata)]

     (map? form)
     [:map (into {}
                 (map (fn [[k v]]
                        [(normalize-argument-form k)
                         (normalize-argument-form v)]))
                 form)
      (normalize-argument-metadata explicit-metadata)]

     (set? form)
     [:set (set (map normalize-argument-form form))
      (normalize-argument-metadata explicit-metadata)]

     (seq? form)
     [:list (mapv normalize-argument-form form)
      (normalize-argument-metadata explicit-metadata)]

     :else
     [:value form (normalize-argument-metadata explicit-metadata)])))

(defn matching-function-arglists? [metadata fn-body]
  (when (and (map? metadata) (contains? metadata :arglists))
    (let [advertised (literal-arglists (:arglists metadata))
          implemented (fn-parameter-vectors fn-body)
          advertised-shapes (some->> advertised
                                     (mapv parameter-arity-shape))
          implemented-shapes (some->> implemented
                                     (mapv parameter-arity-shape))]
      (and (seq advertised)
           (seq implemented)
           (every? some? advertised-shapes)
           (every? some? implemented-shapes)
           (= advertised-shapes implemented-shapes)
           (= (mapv normalize-argument-form advertised)
              (mapv normalize-argument-form implemented))))))

(declare pure-metadata-form?)

(defn pure-metadata-call? [form]
  (let [head (first form)]
    (cond
      (= 'quote head)
      (= 2 (count form))

      (= 'var head)
      (and (= 2 (count form)) (symbol? (second form)))

      (= '.withMeta head)
      (and (= 3 (count form))
           (pure-metadata-form? (second form))
           (pure-metadata-form? (nth form 2)))

      (contains? #{'clojure.core/list 'clojure.core/vector
                   'clojure.core/hash-map 'clojure.core/array-map
                   'clojure.core/set}
                 head)
      (every? pure-metadata-form? (rest form))

      :else
      false)))

(defn pure-metadata-form? [form]
  (cond
    (symbol? form) false
    (map? form) (every? (fn [[k v]]
                          (and (pure-metadata-form? k)
                               (pure-metadata-form? v)))
                        form)
    (vector? form) (every? pure-metadata-form? form)
    (set? form) (every? pure-metadata-form? form)
    (seq? form) (pure-metadata-call? form)
    :else true))

(defn pure-root-value? [form]
  (cond
    (symbol? form) false
    (map? form) (every? (fn [[k v]]
                          (and (pure-root-value? k)
                               (pure-root-value? v)))
                        form)
    (vector? form) (every? pure-root-value? form)
    (set? form) (every? pure-root-value? form)
    (seq? form) (and (= 'quote (first form))
                     (= 2 (count form)))
    :else true))

(defn local-definition-safe? [current-ns var-name]
  (if-let [qualified-ns (some-> var-name namespace)]
    (and current-ns (= qualified-ns (str current-ns)))
    true))

(def metadata-aware-bind-root-marker-tag (Object.))
(def metadata-aware-dynamic-bind-root-marker-tag (Object.))
(def metadata-order-dynamic-marker-tag (Object.))
(def metadata-order-set-marker-tag (Object.))
(def metadata-order-bind-marker-tag (Object.))

(defn metadata-aware-bind-root-marker [target value metadata]
  (list metadata-aware-bind-root-marker-tag target value metadata))

(defn metadata-aware-dynamic-bind-root-marker [target value metadata]
  (list metadata-aware-dynamic-bind-root-marker-tag target value metadata))

(defn metadata-dynamic-marker [var-name]
  (list metadata-order-dynamic-marker-tag var-name))

(defn metadata-set-marker [var-name metadata dynamic-target?]
  (list metadata-order-set-marker-tag var-name metadata dynamic-target?))

(defn metadata-bind-marker [var-name value dynamic-target?]
  (list metadata-order-bind-marker-tag var-name value dynamic-target?))

(defn annotate-bind-root-metadata-body
  ([forms]
   (annotate-bind-root-metadata-body forms nil))
  ([forms initial-ns]
   (let [consumed-form (Object.)]
     (loop [remaining forms
            current-ns initial-ns
            metadata-by-var {}
            dynamic-by-var {}
            annotated []]
       (if (empty? remaining)
         (vec (remove #(identical? consumed-form %) annotated))
         (let [form (first remaining)
               remaining (rest remaining)
               raw-dynamic-var (dynamic-var-symbol form)
               dynamic-key (canonical-var-symbol current-ns raw-dynamic-var)
               [set-target original-metadata] (set-meta-parts form)
               set-var (target-var-symbol set-target)
               set-key (canonical-var-symbol current-ns set-var)
               set-target-dynamic? (boolean (dynamic-var-symbol set-target))
               [bind-target bind-value] (bind-root-parts form)
               bind-var (target-var-symbol bind-target)
               bind-key (canonical-var-symbol current-ns bind-var)
               bind-target-dynamic?
               (boolean (dynamic-var-symbol bind-target))
               evidence (get metadata-by-var bind-key)
               dynamic-evidence (get dynamic-by-var bind-key)
               next-ns (or (in-ns-symbol form) current-ns)]
           (cond
             raw-dynamic-var
             (recur remaining
                    next-ns
                    metadata-by-var
                    (assoc dynamic-by-var dynamic-key
                           {:output-index (count annotated)})
                    (conj annotated (metadata-dynamic-marker dynamic-key)))

             set-var
             (recur remaining
                    next-ns
                    (assoc metadata-by-var set-key
                           {:metadata original-metadata
                            :dynamic-target? set-target-dynamic?
                            :output-index (count annotated)})
                    dynamic-by-var
                    (conj annotated
                          (metadata-set-marker
                            set-key original-metadata
                            set-target-dynamic?)))

             (and bind-var (contains? metadata-by-var bind-key))
             (let [set-index (:output-index evidence)
                   intervening (subvec annotated (inc set-index))
                   nonobserving-interstitial?
                   (every? #(or (nil? %) (false? %)) intervening)
                   dynamic-index (:output-index dynamic-evidence)
                   dynamic-adjacent?
                   (or (nil? dynamic-evidence)
                       (= dynamic-index (dec set-index)))
                   dynamic-pair?
                   (or dynamic-evidence
                       (:dynamic-target? evidence)
                       bind-target-dynamic?)
                   safe? (and nonobserving-interstitial?
                              dynamic-adjacent?
                              (local-definition-safe? current-ns bind-key)
                              (or (function-form? bind-value)
                                  (pure-root-value? bind-value))
                              (map? (:metadata evidence))
                              (pure-metadata-form? (:metadata evidence)))
                   next-annotated
                   (cond
                     (and safe? dynamic-pair?)
                     (-> annotated
                         (cond-> dynamic-evidence
                           (assoc dynamic-index consumed-form))
                         (assoc set-index consumed-form)
                         (conj (metadata-aware-dynamic-bind-root-marker
                                 bind-target bind-value
                                 (:metadata evidence))))

                     safe?
                     (-> annotated
                         (assoc set-index consumed-form)
                         (conj (metadata-aware-bind-root-marker
                                 bind-target bind-value
                                 (:metadata evidence))))

                     :else
                     (conj annotated
                           (metadata-bind-marker
                             bind-key bind-value
                             bind-target-dynamic?)))]
               (recur remaining
                      next-ns
                      (dissoc metadata-by-var bind-key)
                      (dissoc dynamic-by-var bind-key)
                      next-annotated))

             bind-var
             (recur remaining
                    next-ns
                    metadata-by-var
                    (dissoc dynamic-by-var bind-key)
                    (conj annotated
                          (metadata-bind-marker
                            bind-key bind-value
                            bind-target-dynamic?)))

             :else
             (recur remaining next-ns metadata-by-var dynamic-by-var
                    (conj annotated form)))))))))

(declare annotate-bind-root-metadata-form)

(defn annotate-bind-root-metadata-children [form current-ns]
  (w/walk (fn [child]
            (first (annotate-bind-root-metadata-form child current-ns)))
          identity
          form))

(defn annotate-bind-root-metadata-form [form current-ns]
  (if (= "do" (call-name form))
    (let [[children final-ns]
          (loop [remaining (rest form)
                 namespace current-ns
                 children []]
            (if (empty? remaining)
              [children namespace]
              (let [[child child-final-ns]
                    (annotate-bind-root-metadata-form
                      (first remaining) namespace)]
                (recur (rest remaining)
                       child-final-ns
                       (conj children child)))))]
      [(list* (first form)
              (annotate-bind-root-metadata-body children current-ns))
       final-ns])
    [(if (coll? form)
       (annotate-bind-root-metadata-children form current-ns)
       form)
     (or (in-ns-symbol form) current-ns)]))

(defn annotate-bind-root-metadata [source]
  ;; Var.setMeta and Var.bindRoot are separate initializer statements. Carry
  ;; the former as an internal marker until bindRoot compaction decides
  ;; whether the original Var actually advertised function arglists.
  (first (annotate-bind-root-metadata-form source nil)))

(defn runtime-var-reference [var-name]
  (if-let [namespace (namespace var-name)]
    (list 'clojure.lang.RT/var namespace (name var-name))
    (list 'var var-name)))

(defn runtime-var-target [var-name dynamic-target?]
  (let [reference (runtime-var-reference var-name)]
    (if dynamic-target?
      (list '.setDynamic reference true)
      reference)))

(defn late-bound-qualified-var-value [form]
  ;; A value-side `(var qualified/name)` is stricter than the RT.var call in
  ;; the initializer bytecode: the reader/compiler requires the target Var to
  ;; exist already.  That breaks legitimate circular namespace initialization.
  ;; Rewrite only this exact bind value shape; target Vars and arbitrary nested
  ;; forms continue through the normal metadata/order repair unchanged.
  (if (and (seq? form)
           (= 2 (count form))
           (contains? #{'var 'clojure.core/var} (first form))
           (symbol? (second form))
           (namespace (second form)))
    (runtime-var-reference (second form))
    form))

(def semantic-metadata-form-key
  ::semantic-metadata-form)

(def semantic-metadata-marker-tag (Object.))
(def protocol-scaffold-marker-tag (Object.))

(def exact-source-location-keys
  #{:file :line :column :end-line :end-column})

(defn quoted-form? [form]
  (and (seq? form)
       (= "quote" (call-name form))
       (= 2 (count form))))

(defn semantic-metadata-marker [form registry]
  (let [identifier (Object.)]
    (swap! registry assoc identifier form)
    (list semantic-metadata-marker-tag identifier)))

(def erasable-list-location-keysets
  #{#{:line} #{:column} #{:line :column}})

(defn erasable-list-location-with-meta? [form]
  (and (seq? form)
       (= ".withMeta" (call-name form))
       (= 3 (count form))
       (let [value (second form)
             metadata (nth form 2)]
         (and (seq? value)
              (= "list" (call-name value))
              (map? metadata)
              (contains? erasable-list-location-keysets
                         (set (keys metadata)))))))

(defn protect-metadata-aware-marker-tree [form registry]
  ;; Metadata-aware bind markers must remain readable until their advertised
  ;; arglists are compared with the recovered fn body.  Protect only the
  ;; nested withMeta shape that the generic location-scaffolding rule would
  ;; erase; hiding the whole metadata operand would make that comparison
  ;; opaque and unnecessarily demote exact defn recoveries.
  (cond
    (erasable-list-location-with-meta? form)
    (semantic-metadata-marker form registry)

    (map? form)
    (with-meta
      (into (empty form)
            (map (fn [[key value]]
                   [(protect-metadata-aware-marker-tree key registry)
                    (protect-metadata-aware-marker-tree value registry)]))
            form)
      (meta form))

    (vector? form)
    (with-meta
      (mapv #(protect-metadata-aware-marker-tree % registry) form)
      (meta form))

    (set? form)
    (with-meta
      (set (map #(protect-metadata-aware-marker-tree % registry) form))
      (meta form))

    (seq? form)
    (with-meta
      (apply list
             (map #(protect-metadata-aware-marker-tree % registry) form))
      (meta form))

    :else form))

(defn protect-semantic-metadata-tree [form registry]
  (cond
    ;; Quoted values are runtime data, not executable forms to simplify.
    (quoted-form? form)
    (semantic-metadata-marker form registry)

    ;; `.withMeta` is itself the exact runtime metadata construction.  Hide
    ;; the whole call so neither it nor its quoted/list children are touched.
    (and (seq? form)
         (contains? #{".withMeta" "with-meta"} (call-name form)))
    (semantic-metadata-marker form registry)

    ;; Some source-stage forms carry IObj metadata directly instead of an
    ;; explicit withMeta call.  Preserve those objects through clojure.walk,
    ;; which otherwise reconstructs the sequence and loses its metadata.
    (and (seq? form) (seq (meta form)))
    (semantic-metadata-marker form registry)

    (map? form)
    (with-meta
      (into (empty form)
            (map (fn [[key value]]
                   [(protect-semantic-metadata-tree key registry)
                    (protect-semantic-metadata-tree value registry)]))
            form)
      (meta form))

    (vector? form)
    (with-meta
      (mapv #(protect-semantic-metadata-tree % registry) form)
      (meta form))

    (set? form)
    (with-meta
      (set (map #(protect-semantic-metadata-tree % registry) form))
      (meta form))

    (seq? form)
    (with-meta
      (apply list (map #(protect-semantic-metadata-tree % registry) form))
      (meta form))

    :else form))

(defn protect-semantic-metadata-forms [source]
  ;; The generic compact rules intentionally erase reader/compiler line maps.
  ;; A `.withMeta` nested in a Var metadata operand is different: it is part
  ;; of the runtime Var metadata (not scaffolding) and must survive exactly.
  ;; Traverse top-down so quoted data is protected before postwalk reaches it.
  (let [registry (atom {})]
    [(letfn [(protect [form]
            (cond
              (map? form)
              (with-meta
                (into (empty form)
                      (map (fn [[key value]] [(protect key) (protect value)]))
                      form)
                (meta form))

              (vector? form)
              (with-meta (mapv protect form) (meta form))

              (set? form)
              (with-meta (set (map protect form)) (meta form))

              (seq? form)
              (let [head-form (first form)
                    head (call-name form)
                    items (vec form)
                    marker-metadata-index
                    (cond
                      (and (= 4 (count items))
                           (or (identical?
                                 metadata-aware-bind-root-marker-tag
                                 head-form)
                               (identical?
                                 metadata-aware-dynamic-bind-root-marker-tag
                                 head-form))) 3
                      (and (= 4 (count items))
                           (identical? metadata-order-set-marker-tag
                                       head-form)) 2
                      :else nil)
                    metadata-index
                    (or marker-metadata-index
                        (cond
                          (contains? #{".setMeta" "reset-meta!"}
                                     head) 2
                          (contains? #{".withMeta" "with-meta"}
                                     head) 2
                          :else nil))]
                (with-meta
                  (apply list
                         (map-indexed
                           (fn [index item]
                             (if (= index metadata-index)
                               (if (= index marker-metadata-index)
                                 (protect-metadata-aware-marker-tree
                                   item registry)
                                 (protect-semantic-metadata-tree item registry))
                               (protect item)))
                           items))
                  (meta form)))

              :else form))]
       (protect source))
     @registry]))

(defn restore-semantic-metadata-markers [source registry]
  (w/postwalk
    (fn [form]
      (if (and (seq? form)
               (identical? semantic-metadata-marker-tag (first form))
               (contains? registry (second form)))
        (get registry (second form))
        form))
    source))

(defn typed-metadata-tree [form]
  (cond
    (quoted-form? form)
    form

    (map? form)
    (with-meta
      (into (empty form)
            (map (fn [[key value]]
                   [(typed-metadata-tree key)
                    (if (and (contains? exact-source-location-keys key)
                             (instance? Integer value))
                      (list 'int value)
                      (typed-metadata-tree value))]))
            form)
      (when-let [metadata (meta form)]
        (typed-metadata-tree metadata)))

    (vector? form)
    (with-meta (mapv typed-metadata-tree form)
      (when-let [metadata (meta form)]
        (typed-metadata-tree metadata)))

    (set? form)
    (with-meta (set (map typed-metadata-tree form))
      (when-let [metadata (meta form)]
        (typed-metadata-tree metadata)))

    (seq? form)
    (with-meta (apply list (map typed-metadata-tree form))
      (when-let [metadata (meta form)]
        (typed-metadata-tree metadata)))

    :else form))

(defn preserve-exact-metadata-number-types [source]
  ;; Pprinting an in-memory Integer as `1` makes the next reader create a
  ;; Long.  Emit `(int 1)` only beneath proven metadata operands.  Quoted
  ;; domain data and ordinary maps containing `:column` remain untouched.
  (w/postwalk
    (fn [form]
      (if (and (seq? form)
               (contains? #{".setMeta" "reset-meta!" ".withMeta" "with-meta"}
                          (call-name form))
               (< 2 (count form)))
        (with-meta
          (apply list
                 (assoc (vec form) 2
                        (typed-metadata-tree (nth form 2))))
          (meta form))
        form))
    source))

(defn immediate-protocol-initializer [form]
  (when (and (seq? form)
             (= 1 (count form))
             (function-form? (first form)))
    (let [nodes (tree-seq coll? seq form)
          sets (keep set-meta-parts nodes)
          binds (keep bind-root-parts nodes)
          set-vars (map (comp target-var-symbol first) sets)
          bind-vars (map (comp target-var-symbol first) binds)
          metadata (map second sets)]
      (when (and (= 2 (count sets))
                 (= 1 (count binds))
                 (every? symbol? (concat set-vars bind-vars))
                 (apply = (concat set-vars bind-vars))
                 (apply = metadata)
                 (= {} (second (first binds)))
                 (some #(= ".hasRoot" (call-name %)) nodes))
        {:protocol (first set-vars)
         :metadata (first metadata)}))))

(defn protocol-class-name [protocol]
  (clojure.lang.Compiler/munge
    (str (namespace protocol) "." (name protocol))))

(defn protocol-class-load? [form protocol]
  (and (= "classForName" (call-name form))
       (= 2 (count form))
       (= (protocol-class-name protocol) (second form))))

(defn protocol-doc-value [form protocol]
  (when (and (= "alter-meta!" (call-name form))
             (= 5 (count form))
             (= protocol (target-var-symbol (second form)))
             (= "assoc" (call-name (list (nth form 2 nil))))
             (= :doc (nth form 3 nil)))
    {:doc (nth form 4)}))

(defn protocol-assertion? [form protocol]
  (and (seq? form)
       (= 3 (count form))
       (let [callee (first form)]
         (and (seq? callee)
              (= 'clojure.core/assert-same-protocol
                 (target-var-symbol callee))))
       (= protocol (target-var-symbol (second form)))))

(defn protocol-root-evidence [form protocol]
  (when (and (= "alter-var-root" (call-name form))
             (= 4 (count form))
             (= protocol (target-var-symbol (second form)))
             (= "merge" (some-> (nth form 2 nil) name)))
    (let [assoc-form (nth form 3 nil)
          entries (when (and (= "assoc" (call-name assoc-form))
                             (even? (count (drop 2 assoc-form))))
                    (into {} (map vec (partition 2 (drop 2 assoc-form)))))]
      (when (and (= #{:sigs :var :method-map :method-builders}
                    (set (keys entries)))
                 (= protocol (target-var-symbol (:var entries)))
                 (map? (:sigs entries))
                 (= (set (keys (:sigs entries)))
                    (set (keys (:method-map entries)))))
        {:sigs (:sigs entries)}))))

(defn protocol-reset? [form protocol]
  (and (= "-reset-methods" (call-name form))
       (= 2 (count form))
       (= protocol (second form))))

(defn protocol-return? [form protocol]
  (= (symbol (name protocol)) (quoted-symbol form)))

(defn protocol-method-preintern-facts
  [protocol sigs preinterned-vars-before-namespace-load]
  (into {}
        (map (fn [method]
               [method
                (contains? preinterned-vars-before-namespace-load
                           (symbol (namespace protocol) (name method)))]))
        (keys sigs)))

(defn protocol-scaffold-evidence
  ([forms]
   (protocol-scaffold-evidence forms #{}))
  ([forms preinterned-vars-before-namespace-load]
   (when (= 7 (count forms))
     (let [[initializer class-load doc-form assertion root-form reset-form
            return-form] forms
           init-evidence (immediate-protocol-initializer initializer)
           protocol (:protocol init-evidence)
           doc-evidence (when protocol
                          (protocol-doc-value doc-form protocol))
           root-evidence (when protocol
                           (protocol-root-evidence root-form protocol))]
       (when (and init-evidence
                  (protocol-class-load? class-load protocol)
                  doc-evidence
                  (protocol-assertion? assertion protocol)
                  root-evidence
                  (protocol-reset? reset-form protocol)
                  (protocol-return? return-form protocol))
         (merge init-evidence doc-evidence root-evidence
                {:method-var-preinterned-before-namespace-load?
                 (protocol-method-preintern-facts
                   protocol (:sigs root-evidence)
                   preinterned-vars-before-namespace-load)}))))))

(defn protect-protocol-scaffold-body
  [forms registry preinterned-vars-before-namespace-load]
  (loop [remaining (vec forms) protected []]
    (if (empty? remaining)
      protected
      (if-let [evidence (and (<= 7 (count remaining))
                             (protocol-scaffold-evidence
                               (subvec remaining 0 7)
                               preinterned-vars-before-namespace-load))]
        (let [identifier (Object.)]
          (swap! registry assoc identifier evidence)
          (recur (subvec remaining 7)
                 (conj protected
                       (list protocol-scaffold-marker-tag identifier))))
        (recur (subvec remaining 1)
               (conj protected (first remaining)))))))

(defn protect-protocol-scaffolds
  ([source]
   (protect-protocol-scaffolds source #{}))
  ([source preinterned-vars-before-namespace-load]
   (let [registry (atom {})]
     [(w/postwalk
        (fn [form]
          (if (= "do" (call-name form))
            (with-meta
              (list* (first form)
                     (protect-protocol-scaffold-body
                       (rest form) registry
                       preinterned-vars-before-namespace-load))
              (meta form))
            form))
        source)
      @registry])))

(defn protocol-tag-value [tag]
  (cond
    (nil? tag) nil
    (quoted-form? tag) (second tag)
    (or (symbol? tag) (keyword? tag) (class? tag)) tag
    :else
    (throw (ex-info "unsupported defprotocol tag form" {:tag tag}))))

(defn protocol-source-argument [form]
  (loop [form form tag nil]
    (cond
      (and (seq? form) (= ".withMeta" (call-name form))
           (= 3 (count form)) (map? (nth form 2)))
      (recur (second form)
             (or (protocol-tag-value (:tag (nth form 2))) tag))

      (quoted-form? form)
      (let [argument (second form)]
        (when-not (symbol? argument)
          (throw (ex-info "non-symbol defprotocol argument"
                          {:argument form})))
        (cond-> argument tag (with-meta {:tag tag})))

      (symbol? form)
      (cond-> form tag (with-meta {:tag tag}))

      :else
      (throw (ex-info "unsupported defprotocol argument form"
                      {:argument form})))))

(defn protocol-method-declaration [[method signature]]
  (when-not (and (keyword? method) (map? signature))
    (throw (ex-info "malformed defprotocol signature"
                    {:method method :signature signature})))
  (let [arglists (literal-arglists (:arglists signature))
        tag (protocol-tag-value (:tag signature))
        method-name (cond-> (symbol (name method))
                      tag (with-meta {:tag tag}))
        parameters (when arglists
                     (mapv #(mapv protocol-source-argument %) arglists))
        doc (:doc signature)]
    (when-not (seq parameters)
      (throw (ex-info "defprotocol signature has no literal arglists"
                      {:method method :signature signature})))
    (when-not (or (nil? doc) (string? doc))
      (throw (ex-info "unsupported defprotocol method doc"
                      {:method method :doc doc})))
    (list* method-name (concat parameters (when doc [doc])))))

(defn expand-protocol-scaffold
  [{:keys [protocol metadata doc sigs
           method-var-preinterned-before-namespace-load?]}]
  (when-not (or (nil? doc) (string? doc))
    (throw (ex-info "unsupported defprotocol doc" {:protocol protocol :doc doc})))
  (let [protocol-name (symbol (name protocol))
        metadata-binding (gensym "protocol_metadata__")
        declarations (mapv protocol-method-declaration sigs)
        protocol-definition
        (list* 'defprotocol protocol-name
               (concat (when doc [doc]) declarations))
        protocol-reset
        (list 'reset-meta!
              (runtime-var-reference protocol)
              (list 'assoc
                    (list 'assoc metadata-binding :doc doc)
                    :name (list 'quote protocol-name)
                    :ns '*ns*))
        method-resets
        (mapv (fn [[method signature]]
                (let [method-name (symbol (name method))
                      preinterned?
                      (true?
                        (get method-var-preinterned-before-namespace-load?
                             method))
                      signature-binding (gensym "protocol_signature__")
                      name-binding (gensym "protocol_method_name__")]
                  (list
                    'let
                    [signature-binding
                     (list 'assoc signature
                           :protocol (runtime-var-reference protocol))
                     name-binding
                     (if preinterned?
                       (list 'quote method-name)
                       (list 'with-meta
                             (list :name signature-binding)
                             signature-binding))]
                    (list 'reset-meta!
                          (runtime-var-reference
                            (symbol (namespace protocol) (name method)))
                          (list 'assoc signature-binding
                                :name name-binding
                                :ns '*ns*)))))
              sigs)]
    (list* 'let
           [metadata-binding (typed-metadata-tree metadata)]
           protocol-definition
           protocol-reset
           method-resets)))

(defn expand-protocol-scaffold-markers [source registry]
  (w/postwalk
    (fn [form]
      (if (and (seq? form)
               (identical? protocol-scaffold-marker-tag (first form))
               (contains? registry (second form)))
        (expand-protocol-scaffold (get registry (second form)))
        form))
    source))

(defn restore-metadata-order [source]
  (w/postwalk
    (fn [form]
      (cond
        (and (seq? form)
             (identical? metadata-order-dynamic-marker-tag (first form)))
        (list '.setDynamic
              (runtime-var-reference (second form))
              true)

        (and (seq? form)
             (identical? metadata-order-set-marker-tag (first form)))
        (list '.setMeta
              (runtime-var-target (second form) (nth form 3 false))
              (nth form 2 nil))

        (and (seq? form)
             (identical? metadata-order-bind-marker-tag (first form)))
        (list '.bindRoot
              (runtime-var-target (second form) (nth form 3 false))
              (late-bound-qualified-var-value (nth form 2 nil)))

        :else
        form))
    source))

(defn definition-symbol [form]
  (when (and (contains? #{"def" "defn" "defonce" "defmulti" "defprotocol"
                          "defrecord" "deftype"}
                        (call-name form))
             (symbol? (second form)))
    (second form)))

(defn same-var? [current-ns left right]
  (and left right
       (= (canonical-var-symbol current-ns left)
          (canonical-var-symbol current-ns right))))

(defn repaired-reset-meta [var-name metadata]
  (let [simple-name (symbol (name var-name))]
    (list 'reset-meta!
          (list 'var simple-name)
          (list 'assoc metadata
                :name (list 'quote simple-name)
                :ns '*ns*))))

(defn expand-metadata-aware-bind-root [form]
  (let [target (second form)
        value (nth form 2 nil)
        original-metadata (nth form 3 nil)
        var-name (target-var-symbol target)
        simple-name (some-> var-name name symbol)
        function? (function-form? value)
        function-tail (when function? (rest value))
        function-body (when function?
                        (if (symbol? (first function-tail))
                          (rest function-tail)
                          function-tail))
        definition
        (if (and function?
                 (map? original-metadata)
                 (not (contains? original-metadata :doc))
                 (matching-function-arglists?
                   original-metadata function-body))
          (list* 'defn simple-name function-body)
          (list 'def simple-name value))]
    (list 'do
          definition
          (repaired-reset-meta simple-name original-metadata))))

(defn expand-metadata-aware-dynamic-bind-root [form]
  (let [target (second form)
        value (nth form 2 nil)
        original-metadata (nth form 3 nil)
        var-name (target-var-symbol target)
        simple-name (some-> var-name name symbol)
        dynamic-name (with-meta simple-name {:dynamic true})]
    (list 'do
          (list 'def dynamic-name value)
          (repaired-reset-meta simple-name original-metadata))))

(defn repair-definition-body [forms]
  (loop [remaining forms current-ns nil repaired []]
    (let [[a b c & tail] remaining
          dynamic-var (dynamic-var-symbol a)
          [reset-target metadata] (reset-meta-parts b)
          reset-dynamic-var (dynamic-var-symbol reset-target)
          [bind-target bind-value] (bind-root-parts c)
          bind-dynamic-var (dynamic-var-symbol bind-target)
          [plain-reset-target plain-metadata] (reset-meta-parts a)
          plain-reset-var (var-symbol plain-reset-target)
          definition-var (definition-symbol b)]
      (cond
        (empty? remaining)
        repaired

        (and (same-var? current-ns dynamic-var reset-dynamic-var)
             (same-var? current-ns dynamic-var bind-dynamic-var))
        (let [simple-name (symbol (name dynamic-var))
              dynamic-name (with-meta simple-name {:dynamic true})]
          (recur tail (namespace-after current-ns [a b c])
                 (conj repaired
                       (list 'def dynamic-name bind-value)
                       (repaired-reset-meta simple-name metadata))))

        (same-var? current-ns dynamic-var reset-dynamic-var)
        (let [simple-name (symbol (name dynamic-var))
              dynamic-name (with-meta simple-name {:dynamic true})]
          (recur (cons c tail) (namespace-after current-ns [a b])
                 (conj repaired
                       (list 'def dynamic-name)
                       (repaired-reset-meta simple-name metadata))))

        (same-var? current-ns plain-reset-var definition-var)
        (recur (cons c tail) (namespace-after current-ns [a b])
               (conj repaired b (repaired-reset-meta definition-var plain-metadata)))

        :else
        (recur (rest remaining) (namespace-after current-ns [a])
               (conj repaired a))))))

(defn repair-definition-order [source]
  (w/postwalk (fn [form]
                (if (= "do" (call-name form))
                  (list* (first form) (repair-definition-body (rest form)))
                  form))
              source))

(defn register! [sym !occurs]
  (if (contains? @!occurs sym)
    (let [s (gensym (str (name sym) "_"))]
      (swap! !occurs update sym conj s)
      s)
    (do
      (swap! !occurs assoc sym #{})
      sym)))

(defn maybe-guard [sym guards]
  (if-let [guard (get guards sym)]
    (list sym :guard [guard])
    sym))

(defn compile-pattern [form guards !occurs]
  (cond

    (seq? form)
    (if (and (= 'quote (first form))
             (symbol? (second form)))
      [form]
      [(list (vec (mapcat #(compile-pattern % guards !occurs) form)) :seq)])

    (vector? form)
    [(vec (mapcat #(compile-pattern % guards !occurs) form))]

    (symbol? form)

    (if (= \? (first (name form)))
      (let [fname (name form)]
        (if (= \& (second fname))
          ['& (if (and (= \_ (nth fname 2))
                       (= 3 (count fname)))
                '_
                (maybe-guard (register! form !occurs) guards))]
          (if (= \_ (second fname))
            [form]
            [(maybe-guard (register! form !occurs) guards)])))
      [(list 'quote form)])

    :else
    [form]))

(defn assert-unify [patterns]
  (list* `and true
         (for [[bind unifiers] patterns
               :when (seq unifiers)]
           `(= ~bind ~@unifiers))))

(defn cont! [f]
  {::cont f})

(defprotocol NodeToClj (to-clj [_]))

(def ^:dynamic *conts*)

(defn dag-clause-to-clj [occurrence cont pattern action]
  (let [test (if (instance? IPatternCompile pattern)
               (mp/to-source* pattern occurrence)
               (m/to-source pattern occurrence))]
    [test (to-clj (assoc action :cont cont))]))

(extend-protocol NodeToClj
  LeafNode
  (to-clj [{:keys [value bindings]}]
    (if (not (empty? bindings))
      (let [bindings (remove (fn [[sym _]] (= sym '_))
                             bindings)]
        `(let [~@(apply concat bindings)]
           ~value))
      value))

  FailNode
  (to-clj [{:keys [cont]}]
    `(cont! ~cont))

  BindNode
  (to-clj [{:keys [bindings node cont]}]
    `(let [~@bindings]
       ~(to-clj (assoc node :cont cont))))

  SwitchNode
  (to-clj [{:keys [occurrence cases default cont]}]
    (let [default-cont (when-not (instance? FailNode default)
                         `([] ~(to-clj (assoc default :cont cont))))

          _default-cont (if default-cont (gensym "default-cont_") cont)

          clauses (doall
                   (mapcat (partial apply dag-clause-to-clj occurrence _default-cont) cases))
          bind-expr (-> occurrence meta :bind-expr)
          cond-expr `(cond ~@clauses
                           :else
                           (cont! ~_default-cont))]

      (when default-cont
        (swap! *conts* conj (list* _default-cont default-cont)))

      `(let [~@(when bind-expr [occurrence bind-expr])]
         ~cond-expr))))

(defn run-match [f]
  (let [ret (f)]
    (if-let [cont (::cont ret)]
      (recur cont)
      ret)))

(defn compile-patterns [patterns cont]
  (->> (for [pattern patterns
             :let [[pattern guards _ replacement] (if (map? (second pattern))
                                                    pattern
                                                    [(first pattern) {} nil (last pattern)])
                   !occurs (atom {})]]
         [(compile-pattern pattern guards !occurs) (if (seq @!occurs)
                                                     `(if ~(assert-unify @!occurs)
                                                        ~replacement
                                                        (cont! ~cont))
                                                     replacement)])
       (mapcat identity)))

(defmacro compact
  {:style/indent 1}
  [expr & patterns]
  (let [_expr (gensym "expr_")
        _cont (gensym "cont_")
        [patterns else] (if (= :else (last (butlast patterns)))
                          [(-> patterns butlast butlast) (last patterns)]
                          [patterns _expr])]

    (binding [m/*line* (-> &form meta :line)
              m/*locals* (dissoc &env '_)
              m/*warned* (atom false)
              *conts* (atom #{})]
      (let [init (-> (m/emit-matrix [expr] (concat (compile-patterns patterns _cont) [:else else]))
                     m/compile
                     (assoc :cont _cont)
                     to-clj)]
        `(run-match (fn []
                      (let [~_expr ~expr
                            ~_cont (fn [] ~else)]
                        (letfn [~@@*conts*]
                          ~init))))))))

(defn macrocompact-step [expr]
  (cond
    (and (seq? expr)
         (true? (semantic-metadata-form-key (meta expr))))
    expr

    (captured-compiler-temp-binding? expr)
    expr

    (and (seq? expr)
         (identical? metadata-aware-bind-root-marker-tag (first expr)))
    (expand-metadata-aware-bind-root expr)

    (and (seq? expr)
         (identical? metadata-aware-dynamic-bind-root-marker-tag
                     (first expr)))
    (expand-metadata-aware-dynamic-bind-root expr)

    (recover-locking-form expr)
    (recover-locking-form expr)

    :else
    (compact expr
    [(do ?ret) :-> ?ret]
    [(`let [?a ?b] (`let ?binds ?&body)) :-> `(let [~?a ~?b ~@?binds] ~@?&body)]
    [(fn* ?&body) :-> `(fn ~@?&body)]
    [(let* ?binds ?&body) :-> `(let ~?binds ~@?&body)]
    [(if ?test (do ?&then)) :-> `(when ~?test ~@?&then)]
    [(if ?test ?then nil) :->`(when ~?test ~?then)]
    [(`when ?test (do ?&then)) :-> `(when ~?test ~@?&then)]
    [(`let ?bindings (do ?&body)) :-> `(let ~?bindings ~@?&body)]
    [(`when-let ?bindings (do ?&body)) :-> `(when-let ~?bindings ~@?&body)]
    [(`when-some ?bindings (do ?&body)) :-> `(when-some ~?bindings ~@?&body)]
    [(`fn ?name (?bindings (do ?&body))) :-> `(fn ~?name (~?bindings ~@?&body))]
    [(if ?test nil ?&body) :-> `(when-not ~?test ~@?&body)]
    [(`when-not ?bindings (do ?&body)) :-> `(when-not ~?bindings ~@?&body)]

    [(clojure.lang.RT/count ?arg) :-> `(count ~?arg)]
    [(clojure.lang.RT/nth ?&args) :-> `(nth ~@?&args)]
    [(clojure.lang.RT/get ?&args) :-> `(get ~@?&args)]
    [(clojure.lang.RT/isReduced ?arg) :-> `(reduced? ~?arg)]
    [(clojure.lang.RT/alength ?arg) :-> `(alength ~?arg)]
    [(clojure.lang.RT/aclone ?arg) :-> `(aclone ~?arg)]
    [(clojure.lang.RT/aget ?arr ?idx) :-> `(aget ~?arr ~?idx)]
    [(clojure.lang.RT/aset ?arr ?idx ?val) :-> `(aset ~?arr ~?idx ~?val)]
    [(clojure.lang.RT/object_array ?arg) :-> `(object-array ~?arg)]
    [(clojure.lang.Util/identical ?a ?b) :-> `(identical? ~?a ~?b)]
    [(clojure.lang.Util/equiv ?a ?b) :-> `(= ~?a ~?b)]
    [(clojure.lang.Numbers/num ?a) :-> ?a]
    [(java.lang.Long/valueOf ?a) {?a number?} :-> (long ?a)]
    [(java.lang.Integer/valueOf ?a) {?a number?} :-> (int ?a)]
    [(java.lang.Double/valueOf ?a) {?a number?} :-> (double ?a)]
    [(java.lang.Float/valueOf ?a) {?a number?} :-> (float ?a)]

    [(.get (var ?v)) :-> ?v]

    [(`-> ?&x)
     :->
     `(-> ~@(mapcat (fn [x]
                      (compact x
                         [(`-> ?&y) :-> `[~@?&y]]
                         :else [x]))
                    ?&x))]

    [(do ?&body)
     {?&body #(some (fn [expr]
                      (and (seq? expr)
                           (= 'do (first expr))))
                    %)}
     :->
     (list* 'do (->> (for [expr ?&body]
                       (if (and (seq? expr)
                                (= 'do (first expr)))
                         (rest expr)
                         [expr]))
                     (mapcat identity)))]

    [(clojure.lang.Var/pushThreadBindings ?binds) :-> `(push-thread-bindings ~?binds)]
    [(clojure.lang.Var/popThreadBindings) :-> `(pop-thread-bindings)]

    [(do (`push-thread-bindings ?binds)
         (try
           ?&body))
     {?&body #(= `(finally (pop-thread-bindings)) (last %))}
     :->
     (let [?&body (butlast ?&body)]
       (cond

         (map? ?binds)
         (if (every? #(and (seq? %) (= 'var (first %))) (keys ?binds))
           `(binding ~(vec (mapcat (fn [[[_ var] init]] [var init]) ?binds)) ~@?&body)
           `(with-bindings ~?binds ~@?&body))

         (and (seq? ?binds) (= `hash-map (first ?binds)))
         (if (every? #(and (seq? %) (= 'var (first %))) (take-nth 2 (rest ?binds)))
           `(binding ~(vec (mapcat (fn [[[_ var] init]] [var init]) (partition 2 (rest ?binds)))) ~@?&body)
           `(with-bindings ~?binds ~@?&body))

         :else
         `(with-bindings ~?binds ~@?&body)))]

    [(`with-bindings ?bindings ?&body)
     {?bindings #(and (map? %)
                      (contains? % 'clojure.lang.Compiler/LOADER)
                      (= 1 (count %)))}
     :->
     `(with-loading-context ~@?&body)]

    [(`identical? ?x nil) :-> `(nil? ~?x)]
    [(`identical? nil ?x) :-> `(nil? ~?x)]

    [(clojure.lang.LazySeq. (`fn ?_ ([] ?&body))) :-> `(lazy-seq ~@?&body)]
    [(clojure.lang.Delay. (`fn ?_ ([] ?&body))) :-> `(delay ~@?&body)]
    [(`bound-fn* (`fn ?_ ([] ?&body))) :-> `(bound-fn [] ~@?&body)]

    [(.reset ?v (?f (.deref ?v) ?&args)) :-> `(vswap! ~?v ~?f ~@?&args)]

    [(`when-not (.equals ?ns ''clojure.core)
      (`dosync ?&_)
      nil)
     :->
     nil]

    [(if ?test1 ?then1 (`when ?test2 ?&then2)) :-> `(cond ~?test1 ~?then1 ~?test2 (do ~@?&then2))]
    [(if ?test1 ?then1 (`cond ?&body)) :-> `(cond ~?test1 ~?then1 ~@?&body)]

    [(loop* ?&l) :-> `(loop ~@?&l)]

    [(`let [?a ?b] (try ?&body))
     {?&body #(and (seq? %)
                   (compact (last %)
                     [(finally (.close ?x)) :-> true]
                     :else false))}
     :->
     `(with-open [~?a ~?b]
        ~@(butlast ?&body))]

    [(`let [?c ?t]
      (`loop [?n 0]
       (`when (`< ?n ?c)
        ?&body)))
     {?body #(compact % [(recur (`inc ?a)) :-> true] :else false)}
     :->
     `(dotimes [~?n ~?t]
        ~@(butlast ?&body))]

    [(`let [?t ?x] (if ?t ?y ?t))
     {?t #(and (symbol? %) (-> % name (.startsWith "and__")))}
     :->
     `(and ~?x ~?y)]
    [(`and ?x (`and ?y ?&z)) :->  `(and ~?x ~?y ~@?&z)]

    [(`let [?t ?x] (if ?t ?t ?y))
     {?t #(and (symbol? %) (-> % name (.startsWith "or__")))}
     :->
     `(or ~?x ~?y)]
    [(`or ?x (`or ?y ?&z)) :-> `(or ~?x ~?y ~@?&z)]

    ;; WIP body should not use ?n
    [((`fn ?n ([] ?&body))) :-> `(do ~@?&body)]

    [(clojure.core/import* ?klass) :-> `(import '~(symbol ?klass))]

    [(.setMeta ?ref ?meta) :-> `(reset-meta! ~?ref ~?meta)]
    [(`reset-meta! ?var ?meta) {?meta #(and (map? %)
                                            (empty? (dissoc % :file :line :column :arglists :doc)))} :-> nil]

    [(`reset-meta! ?var ?meta) {?meta #(and (map? %) (:declared %))} :-> `(declare ~(-> ?var second))]

    [(.withMeta (`list ?&body) ?meta) {?meta #(and (map? %) (#{#{:line} #{:column} #{:line :column}} (set (keys %))))} :-> `(list ~@?&body)]
    [(.withMeta ?x ?meta) {?meta #(and (map? %) (empty? %))} :-> ?x]

    [(clojure.lang.LockingTransaction/runInTransaction (`fn ?_ ([] ?&body))) :-> `(dosync ~@?&body)]

    ;; WIP custom message
    [(if (clojure.lang.LockingInTransaction/isRunning)
       (throw ?_)
       ?body) :-> `(io! ~?body)]

    [(`when-let [?bind (`seq ?xs)]
      (`let [?x ?bind]
       ?&body)) {?bind #(and (symbol? %) (-> % name (.startsWith "xs__")))}
     :->
     `(when-first [~?x ~?xs]
        ~@?&body)]

    [(`when-not (`nil? ?g)
      (?f ?g ?&args))
     {?g #(and (symbol? %) (-> % name (.startsWith "G__")))}
     :-> `(some-> ~?g (~?f ~@?&args))]

    [(`let [?g (`some-> ?g ?&exprs)]
      (`some-> ?g ?&exprs2))
     :-> (if (symbol-occurs? ?g ?&exprs2)
           `(let [~?g (some-> ~?g ~@?&exprs)]
              (some-> ~?g ~@?&exprs2))
           `(some-> ~?g ~@?&exprs ~@?&exprs2))]

    [(`let [?g ?expr]
      (`some-> ?g ?&exprs2))
     :-> (if (symbol-occurs? ?g ?&exprs2)
           `(let [~?g ~?expr]
              (some-> ~?g ~@?&exprs2))
           `(some-> ~?expr ~@?&exprs2))]

    [(`if ?test
      (?f ?g ?&args)
      ?g)
     {?g #(and (symbol? %) (-> % name (.startsWith "G__")))}
     :-> `(cond-> ~?g ~?test (~?f ~@?&args))]

    [(`let [?g (`cond-> ?g ?&exprs)]
      (`cond-> ?g ?&exprs2))
     :-> (if (symbol-occurs? ?g ?&exprs2)
           `(let [~?g (cond-> ~?g ~@?&exprs)]
              (cond-> ~?g ~@?&exprs2))
           `(cond-> ~?g ~@?&exprs ~@?&exprs2))]

    [(`let [?g ?expr]
      (`cond-> ?g ?&exprs2))
     :-> (if (symbol-occurs? ?g ?&exprs2)
           `(let [~?g ~?expr]
              (cond-> ~?g ~@?&exprs2))
           `(cond-> ~?expr ~@?&exprs2))]

    [(do
       nil
       (`let [?v (var ?var)]
        (`when-not (`and (.hasRoot ?v)
                    (`instance? clojure.lang.MultiFn (`deref ?v)))
         ?_
         (def ?name (clojure.lang.MultiFn. ?sname ?dispatch-fn ?d ?h))
         (var ?var))))
     :->
     `(defmulti ~?name ~?dispatch-fn
        ~@(when-not (= ?d :default) [?d])
        ~@(when-not (= ?h '(var clojure.core/global-hierarchy)) [?h]))]

    [(`let [?s (java.io.StringWriter.)]
      (`binding [`*out* ?s]
       ?&body))
     {?&body #(compact (last %) [(`str ?_) :-> true] :else false)}
     :-> `(with-out-str ~@(butlast ?&body))]

    [(`let [?s (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. ?i))]
      (`binding [`*in* ?s]
       ?&body))
     :-> `(with-in-str ~@?&body)]

    [(clojure.lang.RT/classForName ?class) {?class string?} :-> (symbol ?class)]

    ;; This is initializer code, not an ns macro. Preserve the runtime refer
    ;; call and quoted exclusions; refer-clojure would evaluate macro names in
    ;; an exclusion vector instead of receiving their symbols.
    [(`refer ''clojure.core ?&filters)
     :-> `(clojure.core/refer 'clojure.core ~@?&filters)]

    [(`let [?v (var ?var)]
      (`when-not (.hasRoot ?v)
       ?_
       (def ?name ?expr)
       (var ?var))) :-> `(defonce ~?name ~?expr)]

    [(`loop []
      (`when ?test
        ?&body))
     {?&body #(= '(recur) (last %))}
     :->
     `(while ~?test ~@(butlast ?&body))]

    [(.addMethod ?multi ?dispatch-val (`fn ?&body)) :-> `(defmethod ~?multi ~?dispatch-val ~@?&body)]

    [(letfn* ?binds ?&body) :-> `(letfn ~(vec (for [[_ bind] (partition 2 ?binds)]
                                                (rest bind)))
                                   ~@?&body)]

    [(`future-call (fn ?_ [] ?&body)) :-> `(future ~@?&body)]

    [(reify* ?interfaces ?&methods) :-> `(reify ~@?interfaces ~@?&methods)]
    [(.withMeta (`reify ?&body) ?_) :-> `(reify ~@?&body)]

    [(`let [?p (?ctor ?&ctor-args)]
     (`init-proxy ?p ?methods-map)
      ?p) :->
     `(proxy ~(or (:proxy-bases (meta ?ctor))
                 (-> ?ctor str (s/split #"\$") (rest) (butlast) (->> (mapv symbol))))
          ~(vec ?&ctor-args)
          ~@(for [[method method-fn] ?methods-map]
              (let [arities (map (fn [[argv & body]]
                                   (list* (vec (rest argv)) body))
                                 (drop 2 method-fn))]
                (if (= 1 (count arities))
                  (list* (symbol method) (first arities))
                  (list* (symbol method) arities)))))]

    [(`proxy-call-with-super (`fn ?_ ([] ?meth)) ?&_)
     {?meth #(= 'this (second %) )}
     :-> `(proxy-super ~(-> ?meth first str (subs 1) symbol) ~@(->> ?meth (drop 2)))]

    [(`+ ?a 1) :-> `(inc ~?a)]
    [(`+ 1 ?a) :-> `(inc ~?a)]
    [(`- ?a 1) :-> `(dec ~?a)]

    [(`let [?a ?arr, ?ret (`aclone ?a)]
      (`loop [?idx 0]
       (if (`< ?idx (`alength ?a))
         (do
           (`aset ?ret (java.lang.Integer/valueOf ?idx) ?expr)
           (recur (`inc ?idx)))
         ?ret)))
     :-> `(amap ~?arr ~?idx ~?ret ~?expr)]

    [(`case ?&exprs)
     {?&exprs #(and (even? (count %))
                    (or (no-matching-clause-default? (last %))
                        (compact (last %)
                          [(do (throw (java.lang.IllegalArgumentException. (`str "No matching clause: " ?_))) ?&_) :-> true]
                          :else false)))}
     :-> `(case ~@(butlast ?&exprs))]

    [(`let [?a ?arr ?len (`alength ?a)]
      (`loop [?idx 0, ?ret ?init]
       (if (`< ?idx ?len) (recur (`inc ?idx) ?expr) ?ret)))
     :-> `(areduce ~?arr ~?idx ~?ret ~?init ~?expr)]

    [(`let [?x ?obj] (?f ?x ?&args) (?g ?x ?&args2) ?&exprs)
     {?x #(and (symbol? %) (-> % name (.startsWith "G__")))
      ?&exprs (fn [exprs] (every? #(and (seq? %) (= (last exprs) (second %))) (butlast exprs)))}
     :-> `(doto ~?obj (~?f ~@?&args) (~?g ~@?&args2) ~@(map #(list* (first %) (drop 2 %)) (butlast ?&exprs)))]

    [(deftype* ?record ?rtype ?argv :implements ?interfaces ?&impls)
     {?interfaces #(some #{'clojure.lang.IRecord} %)}

     :-> (let [methods (vec (remove-defrecord-methods ?&impls))]
           `(defrecord ~(simple-class-symbol ?record) ~(remove-defrecord-fields ?argv)
              ~@(defrecord-interfaces ?interfaces methods)
              ~@(simplify-self-references ?rtype methods)))]
    [(do
       nil
       (var ?_)
       nil
       (var ?__)
       (`defrecord ?&body)
       ?&_)
     :-> `(defrecord ~@?&body)]

    [(`alter-meta! (var ?v) `assoc :doc nil) :-> nil]
    [((var clojure.core/assert-same-protocol) ?&_) :-> nil]
    [(`-reset-methods ?_) :-> nil]
    [(`alter-var-root (var ?p) `merge (`assoc ?m :sigs ?sigs :var (var ?p) :method-map ?mm :method-builders ?mb))
     :-> `(defprotocol ~(simple-class-symbol ?p)
            ~@(for [[f {:keys [arglists]}] ?sigs]
                (list* (symbol (name f))
                       (map #(mapv (comp protocol-arg-symbol second)
                                   (if (vector? %) % (second %)))
                            (rest arglists)))))]

    [(deftype* ?type ?ttype ?argv :implements ?interfaces ?&impls)
     {?interfaces #(some #{'clojure.lang.IType} %)}

     :-> `(deftype ~(simple-class-symbol ?type) ~?argv
            ~@(deftype-interfaces ?interfaces ?&impls)
            ~@(simplify-self-references ?ttype ?&impls))]

    [(do (deftype ?&body) ?&_) :-> `(deftype ~@?&body)]

    [(.set (var ?v) ?val) ?-> `(set! ~?v ~?val)]

    ;; `(var qualified/name)` is compile-time strict and fails across circular
    ;; namespace dependencies even though the bytecode's RT.var call merely
    ;; interns (and may return an unbound) Var. Preserve the latter semantics.
    [(`def ?name (var ?v))
     {?v #(and (symbol? %) (namespace %))}
     :-> `(def ~?name (clojure.lang.RT/var ~(namespace ?v) ~(name ?v)))]

    [(.bindRoot (var ?var) (`fn ?name ?&body)) :->  `(defn ~(-> ?var name symbol) ~@?&body)]
    [(.bindRoot (var ?var) ?val) :->  `(def  ~(-> ?var name symbol) ~?val)])))

;; WIP for, assert, ns, condp, with-redefs, definterface

(defn macrocompact
  ([source]
   (macrocompact source #{}))
  ([source preinterned-vars-before-namespace-load]
   (let [[protocol-protected protocol-registry]
         (protect-protocol-scaffolds
           source preinterned-vars-before-namespace-load)
         [metadata-protected metadata-registry]
         (-> protocol-protected
             annotate-bind-root-metadata
             protect-semantic-metadata-forms)]
     (-> (w/postwalk
           (fn [node]
             (if (seq? node)
               (let [new-node (macrocompact-step node)]
                 (if (= node new-node)
                   node
                   (recur new-node)))
               node))
           metadata-protected)
         (repair-definition-order)
         (restore-metadata-order)
         (expand-protocol-scaffold-markers protocol-registry)
         (restore-semantic-metadata-markers metadata-registry)
         (preserve-exact-metadata-number-types)))))

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
             (= "var" (call-name form))
             (symbol? (second form)))
    (second form)))

(defn dynamic-var-symbol [form]
  (when (and (= ".setDynamic" (call-name form))
             (= true (nth form 2 nil)))
    (var-symbol (second form))))

(defn reset-meta-parts [form]
  (when (= "reset-meta!" (call-name form))
    [(second form) (nth form 2 nil)]))

(defn bind-root-parts [form]
  (when (= ".bindRoot" (call-name form))
    [(second form) (nth form 2 nil)]))

(defn definition-symbol [form]
  (when (and (contains? #{"def" "defn" "defonce" "defmulti" "defprotocol"
                          "defrecord" "deftype"}
                        (call-name form))
             (symbol? (second form)))
    (second form)))

(defn same-var? [left right]
  (and left right (= (name left) (name right))))

(defn repaired-reset-meta [var-name metadata]
  (let [simple-name (symbol (name var-name))]
    (list 'reset-meta!
          (list 'var simple-name)
          (list 'assoc metadata
                :name (list 'quote simple-name)
                :ns '*ns*))))

(defn repair-definition-body [forms]
  (loop [remaining forms repaired []]
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

        (and (same-var? dynamic-var reset-dynamic-var)
             (same-var? dynamic-var bind-dynamic-var))
        (let [simple-name (symbol (name dynamic-var))
              dynamic-name (with-meta simple-name {:dynamic true})]
          (recur tail
                 (conj repaired
                       (list 'def dynamic-name bind-value)
                       (repaired-reset-meta simple-name metadata))))

        (same-var? dynamic-var reset-dynamic-var)
        (let [simple-name (symbol (name dynamic-var))
              dynamic-name (with-meta simple-name {:dynamic true})]
          (recur (cons c tail)
                 (conj repaired
                       (list 'def dynamic-name)
                       (repaired-reset-meta simple-name metadata))))

        (same-var? plain-reset-var definition-var)
        (recur (cons c tail)
               (conj repaired b (repaired-reset-meta definition-var plain-metadata)))

        :else
        (recur (rest remaining) (conj repaired a))))))

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
  (if (captured-compiler-temp-binding? expr)
    expr
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
     (list* 'do (->> (for [expr ?&body
                           :when expr]
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

    [(`let [?l ?lock]
      (try
        (do (monitor-enter ?l)
            ?&body)
        (finally ?&_)))
     :->
     `(locking ~?lock ~@?&body)]

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

(defn macrocompact [source]
  (-> (w/postwalk
        (fn [node]
          (if (seq? node)
            (let [new-node (macrocompact-step node)]
              (if (= node new-node)
                node
                (recur new-node)))
            node))
        source)
      (repair-definition-order)))

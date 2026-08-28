(require '[clojure.tools.decompiler :as decompiler]
         '[clojure.tools.decompiler.ast :as ast]
         '[clojure.tools.decompiler.pprint :as decompiler.pprint]
         '[clojure.tools.decompiler.source :as source]
         '[clojure.tools.decompiler.sugar :as sugar])

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
  (assert (empty? (:stack result)))
  (assert (= recur-expr (get-in result [:statements 0 :then :ret])))
  (assert (= ast/nil-expr (get-in result [:statements 0 :else])))
  (println "decompiler impure-loop one-armed recur recovery passed"))

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
      body-processor (fn [body]
                       (assert (= 0 (:impure-loop-entry body)))
                       (assoc body
                              :pc 30
                              :stack []
                              :statements [loop-if]
                              :ast {}
                              :recur? true))
      result (with-redefs [ast/process-insns body-processor
                           ast/will-ret? (constantly false)]
               (ast/process-impure-loop
                 {:pc 0
                  :impure-loops #{0}
                  :jump-table {0 0, 40 1}
                  :stack []
                  :statements []
                  :ast {}}))]
  (assert (= 30 (:pc result)))
  (assert (empty? (:stack result)))
  (assert (= {} (:ast result)))
  (assert (= :loop (get-in result [:statements 0 :op])))
  (assert (= loop-if (get-in result [:statements 0 :body :statements 0])))
  (println "decompiler impure-loop continuation preservation passed"))

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

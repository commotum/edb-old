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

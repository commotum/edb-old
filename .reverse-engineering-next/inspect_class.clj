(require '[clojure.java.io :as io]
         '[clojure.pprint :as pp]
         '[clojure.tools.decompiler :as decompiler]
         '[clojure.tools.decompiler.ast :as ast]
         '[clojure.tools.decompiler.bc :as bc]
         '[clojure.tools.decompiler.compact :as compact]
         '[clojure.tools.decompiler.source :as source]
         '[clojure.tools.decompiler.sugar :as sugar])

(let [[root class-name stage] *command-line-args*
      files (filter #(.endsWith (str %) ".class") (file-seq (io/file root)))
      names (into {} (map (fn [file] [(decompiler/cname (str file) root) (str file)]) files))
      bc-for (decompiler/bc-for names)
      bytecode (bc/analyze-class (names class-name))
      tree (ast/bc->ast bytecode {:bc-for bc-for :lenient? true})
      tree (if (= stage "ast") tree (sugar/ast->sugared-ast tree))
      tree (if (#{"ast" "sugar"} stage) tree (source/ast->clj tree))
      tree (cond
             (= stage "compact") (compact/macrocompact tree)
             (= stage "trace")
             (let [original compact/compact-associative-destructuring]
               (with-redefs [compact/compact-associative-destructuring
                             (fn [bindings]
                               (let [result (original bindings)]
                                 (println "MAP-BINDS" (pr-str bindings))
                                 (println "MAP-RESULT" (pr-str result))
                                 result))]
                 (compact/macrocompact tree)))
             :else tree)]
  (binding [*print-meta* true
            pp/*print-right-margin* 140]
    (pp/pprint tree)))

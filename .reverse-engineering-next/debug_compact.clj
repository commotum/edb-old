(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.tools.decompiler :as decompiler]
         '[clojure.tools.decompiler.ast :as ast]
         '[clojure.tools.decompiler.bc :as bc]
         '[clojure.tools.decompiler.compact :as compact]
         '[clojure.tools.decompiler.source :as source]
         '[clojure.tools.decompiler.sugar :as sugar])

(let [root "/tmp/datomic-rev-test/peer-classes"
      files (filter #(.endsWith (str %) ".class") (file-seq (io/file root)))
      names (into {} (map (fn [file] [(decompiler/cname (str file) root) (str file)]) files))
      bc-for (decompiler/bc-for names)
      bytecode (bc/analyze-class (str root "/datomic/math__init.class"))
      clj (-> bytecode
              (ast/bc->ast {:bc-for bc-for :lenient? true})
              (sugar/ast->sugared-ast)
              (source/ast->clj))
      compacted (with-redefs [compact/repair-definition-order identity]
                  (compact/macrocompact clj))
      repaired (compact/repair-definition-order compacted)]
  (println :raw (count clj)
           :compacted (count compacted)
           :repaired (count repaired))
  (println :compacted-tail (map #(when (seq? %) (first %)) (take-last 20 compacted)))
  (println :repaired-tail (map #(when (seq? %) (first %)) (take-last 20 repaired)))
  (doseq [[index form] (map-indexed vector compacted)]
    (println index (if (seq? form) (first form) form)
             (when (and (seq? form) (< (count (pr-str form)) 180)) (pr-str form)))))

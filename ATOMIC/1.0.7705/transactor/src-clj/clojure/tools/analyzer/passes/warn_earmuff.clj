;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.passes.warn-earmuff
  (:require [clojure.tools.analyzer.utils :refer [dynamic?]]))

(defn warn-earmuff
  "Prints a warning to *err* if the AST node is a :def node and the
   var name contains earmuffs but the var is not marked dynamic"
  {:pass-info {:walk :pre :depends #{}}}
  [ast]
  (let [name (str (:name ast))]
    (when (and (= :def (:op ast))
               (> (count name) 2)  ;; Allow * and ** as non-dynamic names
               (= (nth name 0) \*)
               (= (nth name (dec (count name))) \*)
               (not (dynamic? (:var ast) (:val (:meta ast)))))
      (binding [*out* *err*]
        (println "Warning:" name "not declared dynamic and thus is not dynamically rebindable,"
                 "but its name suggests otherwise."
                 "Please either indicate ^:dynamic" name "or change the name"))))
  ast)

;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.passes.jvm.fix-case-test
  (:require [clojure.tools.analyzer.passes.add-binding-atom :refer [add-binding-atom]]))

(defn fix-case-test
  "If the node is a :case-test, annotates in the atom shared
   by the binding and the local node with :case-test"
  {:pass-info {:walk :pre :depends #{#'add-binding-atom}}}
  [ast]
  (when (:case-test ast)
    (swap! (:atom ast) assoc :case-test true))
  ast)

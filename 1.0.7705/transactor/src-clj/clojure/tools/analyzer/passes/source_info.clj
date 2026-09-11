;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.passes.source-info
  (:require [clojure.tools.analyzer.utils :refer [-source-info merge']]
            [clojure.tools.analyzer.ast :refer [update-children]]))

(defn -merge-source-info [source-info]
  (fn [ast]
    (update-in ast [:env] merge' source-info)))

(defn source-info
  "Adds (when avaliable) :line, :column, :end-line, :end-column and :file info to the AST :env"
  {:pass-info {:walk :pre :depends #{}}}
  [ast]
  (let [source-info (-source-info (:form ast) (:env ast))
        merge-source-info (-merge-source-info source-info)]
    (update-children (merge-source-info ast) merge-source-info)))

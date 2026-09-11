;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.passes.jvm.validate-recur
  (:require [clojure.tools.analyzer.ast :refer [update-children]]
            [clojure.tools.analyzer.utils :refer [-source-info]]))

(defmulti validate-recur
  "Ensures recurs don't cross try boundaries"
  {:pass-info {:walk :pre :depends #{}}}
  :op)

(defmethod validate-recur :default [ast]
  (if (-> ast :env :no-recur)
    (update-children ast (fn [ast] (update-in ast [:env] assoc :no-recur true)))
    ast))

(defmethod validate-recur :try [ast]
  (update-children ast (fn [ast] (update-in ast [:env] assoc :no-recur true))))

(defmethod validate-recur :fn-method [ast]
  (update-in ast [:env] dissoc :no-recur))

(defmethod validate-recur :method [ast]
  (update-in ast [:env] dissoc :no-recur))

(defmethod validate-recur :loop [ast]
  (update-in ast [:env] dissoc :no-recur))

(defmethod validate-recur :recur [ast]
  (when (-> ast :env :no-recur)
    (throw (ex-info "Cannot recur across try"
                    (merge {:form (:form ast)}
                           (-source-info (:form ast) (:env ast))))))
  ast)

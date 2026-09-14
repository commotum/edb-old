;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.passes.constant-lifter
  (:require [clojure.tools.analyzer.utils :refer [const-val]]))

(defmulti constant-lift
  "If the node represents a collection with no metadata, and every item of that
   collection is a literal, transform the node to an equivalent :const node."
  {:pass-info {:walk :post :depends #{}}}
  :op)

(defmethod constant-lift :vector
  [{:keys [items form env] :as ast}]
  (if (and (every? :literal? items)
           (empty? (meta form)))
    (merge (dissoc ast :items :children)
           {:op       :const
            :val      (mapv const-val items)
            :type     :vector
            :literal? true})
    ast))

(defmethod constant-lift :map
  [{:keys [keys vals form env] :as ast}]
  (if (and (every? :literal? keys)
           (every? :literal? vals)
           (empty? (meta form)))
    (let [c (into (empty form)
                  (zipmap (mapv const-val keys)
                          (mapv const-val vals)))
          c (if (= (class c) (class form))
              c
              (apply array-map (mapcat identity c)))]
      (merge (dissoc ast :keys :vals :children)
             {:op       :const
              :val      c
              :type     :map
              :literal? true}))
    ast))

(defmethod constant-lift :set
  [{:keys [items form env] :as ast}]
  (if (and (every? :literal? items)
           (empty? (meta form)))
    (merge (dissoc ast :items :children)
           {:op       :const
            :val      (into (empty form) (mapv const-val items))
            :type     :set
            :literal? true})
    ast))

(defmethod constant-lift :default [ast] ast)

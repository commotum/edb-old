;; Copyright (c) Nicola Mometto, Rich Hickey & contributors.

(ns clojure.tools.analyzer.passes.trim
  (:require [clojure.tools.analyzer.passes.elide-meta :refer [elide-meta]]
            [clojure.tools.analyzer.ast :refer [postwalk]]))

(defmulti -trim :op)

(defmethod -trim :default [ast] ast)

(defn preserving-raw-forms [{:keys [form raw-forms] :as ast} body]
  (let [raw-forms (reverse (cons form raw-forms))]
    (update-in (into ast body) [:raw-forms] into raw-forms)))

(defmethod -trim :do
  [{:keys [statements ret form] :as ast}]
  (if (and (every? :literal? statements)
           (not (:tag (meta form))))
    (preserving-raw-forms (dissoc ast :children :statements :ret) ret)
    ast))

;;TODO: letfn/loop
(defmethod -trim :let
  [{:keys [bindings body form] :as ast}]
  (if (and (or (and (every? (comp :literal? :init) bindings)
                    (:literal? body))
               (empty? bindings))
           (not (:tag (meta form))))
    (preserving-raw-forms (dissoc ast :children :bindings :body) body)
    ast))

(defmethod -trim :try
  [{:keys [catches finally body form] :as ast}]
  (if (and (empty? catches)
           (empty? finally)
           (not (:tag (meta form))))
    (preserving-raw-forms (dissoc ast :children :body :finally :catches) body)
    ast))

(defn trim
  "Trims the AST of unnecessary nodes, e.g. (do (do 1)) -> 1"
  {:pass-info {:walk :none :depends #{} :after #{#'elide-meta}}}
  [ast]
  (postwalk ast -trim))

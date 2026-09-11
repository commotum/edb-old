;;   Copyright (c) Nicola Mometto & contributors.


(ns clojure.tools.decompiler.utils
  (:require [clojure.string :as s])
  (:import clojure.lang.Compiler
           java.io.Writer))

(defn ungensym [s]
  (s/replace s #"(__[0-9]+)" ""))

(defn demunge [s]
  (if (#{"_" '_} s)
    (symbol s)
    (symbol (Compiler/demunge s))))

(defn find-methods [methods matches]
  (for [method methods
        :when (= matches (select-keys method (keys matches)))]
    method))

(defn find-method [methods matches]
  ;; assert just 1
  (first (find-methods methods matches)))

(defn pop-n [stack n]
  (let [c (count stack)]
    (subvec stack 0 (- c n))))

(defn peek-n [stack n]
  (let [c (count stack)]
    (subvec stack (- c n) c)))

(defmethod print-method (Class/forName "[Ljava.lang.Object;") [o w]
  (.write w "#array")
  (.write w " ")
  (print-method (vec o) w))

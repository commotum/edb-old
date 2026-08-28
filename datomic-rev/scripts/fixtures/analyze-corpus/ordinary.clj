(ns ^{:doc "Ordinary multi-form source fixture."}
    fixture.ordinary
  (:require [clojure.string :as str]))

(defn visible [value]
  (str/upper-case value))

(defn- hidden [value]
  (str/lower-case value))

(comment
  (in-ns 'fixture.wrong)
  (defn phantom [] :not-a-definition))

;; Copyright (c) Cognitect, Inc.


(ns ^{:doc "Common Datomic server-side specs shared across projects.
Do not add specs here until you need cross-project sharing."}
    datomic.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::db-name (s/and string? #(re-matches #"\w+(\-?\w+)*" %)))


(comment

  (require :reload 'datomic.specs)
  (in-ns 'datomic.specs)

  (s/valid? ::db-name "a.123.b")
  (s/valid? ::db-name "123:abc")
  (s/explain ::db-name "123:abc")
  (s/exercise ::db-name)
  
  )

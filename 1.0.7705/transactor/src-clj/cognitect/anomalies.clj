;; Copyright (c) Cognitect, Inc.



(ns cognitect.anomalies
  (:require [clojure.spec.alpha :as s]))

(s/def ::category #{::unavailable
                    ::interrupted
                    ::incorrect
                    ::forbidden
                    ::unsupported
                    ::not-found
                    ::conflict
                    ::fault
                    ::busy})
(s/def ::message string?)
(s/def ::anomaly (s/keys :req [::category]
                         :opt [::message]))



;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.passes.cleanup)

(defn cleanup
  {:pass-info {:walk :any :depends #{}}}
  [ast]
  (-> ast
    (update-in [:env] dissoc :loop-locals-casts)
    (update-in [:env :locals] #(reduce-kv (fn [m k l] (assoc m k (dissoc l :env :init))) {} %))
    (dissoc :atom)))

(ns stage3.core2-async-probe
  (:require
   [clojure.core.async :as async]
   [datomic.core2.async :as core2-async]
   [stage3.support :as support]))

(defn -main
  [& args]
  (support/ensure-no-args! args)
  (let [owned-channels (atom [])
        owned-chan (fn
                     ([]
                      (let [ch (async/chan)]
                        (swap! owned-channels conj ch)
                        ch))
                     ([size]
                      (let [ch (async/chan size)]
                        (swap! owned-channels conj ch)
                        ch)))
        one-shot (fn [value]
                   (let [ch (owned-chan 1)]
                     (support/ensure! (async/offer! ch value)
                                      "could not seed one-shot channel")
                     (async/close! ch)
                     ch))]
    (try
      (let [ch (owned-chan 1)]
        (support/ensure! (= ::timeout
                            (core2-async/aderef ch 20 ::timeout))
                         "aderef did not return the timeout sentinel")
        (support/ensure! (async/offer! ch :ready)
                         "could not offer aderef value")
        (support/ensure! (= :ready (core2-async/aderef ch 1000 ::timeout))
                         "aderef did not return the offered value"))

      (let [ch (owned-chan 2)]
        (async/offer! ch :one)
        (async/offer! ch :two)
        (support/ensure! (= [:one :two ::timeout]
                            (core2-async/aderef-n 3 ch 20 ::timeout))
                         "aderef-n did not preserve values and timeout"))

      (let [attempts (atom 0)
            backoffs (atom [])
            output (core2-async/retry
                    :f (fn []
                         (let [attempt (swap! attempts inc)]
                           (one-shot (if (= attempt 3)
                                       :ok
                                       (keyword (str "retry-" attempt))))))
                    :pred #(= :ok %)
                    :backoff (fn [attempt result]
                               (swap! backoffs conj [attempt result])
                               (when (< attempt 3) 1)))]
        (swap! owned-channels conj output)
        (support/ensure! (= :ok
                            (core2-async/aderef output 5000 ::timeout))
                         "retry did not publish its successful result")
        (support/ensure! (= 3 @attempts)
                         "retry used the wrong attempt count"
                         {:attempts @attempts})
        (support/ensure! (= [[1 :retry-1] [2 :retry-2]] @backoffs)
                         "retry used the wrong backoff sequence"
                         {:backoffs @backoffs}))

      (let [output (core2-async/retry
                    :f #(one-shot :terminal-failure)
                    :pred (constantly false)
                    :backoff (fn [_ _] nil)
                    :fail (fn [result] {:failed result}))]
        (swap! owned-channels conj output)
        (support/ensure! (= {:failed :terminal-failure}
                            (core2-async/aderef output 5000 ::timeout))
                         "retry fail transform returned the wrong value"))

      (let [destination (owned-chan 4)
            completion (core2-async/put-all! destination [1 2 3])]
        (swap! owned-channels conj completion)
        (support/ensure! (true? (core2-async/aderef completion 5000 ::timeout))
                         "put-all! did not report completion")
        (support/ensure! (= [1 2 3 nil]
                            (core2-async/aderef-n 4 destination 1000 ::timeout))
                         "put-all! did not publish and close deterministically"))

      (let [closed (owned-chan 1)]
        (async/close! closed)
        (support/ensure!
         (= {:error "Channel closed"
             :cognitect.anomalies/message "Channel closed"
             :cognitect.anomalies/category :cognitect.anomalies/fault}
            (core2-async/<!!x closed))
         "<!!x did not normalize a closed channel"))
      (finally
        (doseq [ch @owned-channels]
          (async/close! ch))
        (shutdown-agents))))
  (println "STAGE3-RESULT {:case :core2-async, :status :passed}"))

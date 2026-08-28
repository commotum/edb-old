(ns stage3.query-timeout-probe
  (:require
   [datomic.datalog :as datalog]
   [datomic.query :as query]
   [stage3.support :as support])
  (:import
   (java.util.concurrent ExecutorService ScheduledExecutorService
                         TimeUnit TimeoutException)))

(defn -main
  [& args]
  (support/ensure-no-args! args)
  (let [root-cancel datalog/*cancel*
        observed-cancel (atom nil)
        evaluator-calls (atom 0)]
    (try
      (let [parsed (query/load-query
                    (query/mapify-query
                     '[:find ?x :in [?x ...] :where]))
            query-map (assoc parsed :timeout [40])
            evaluator
            (fn [& _]
              (swap! evaluator-calls inc)
              (let [cancel datalog/*cancel*
                    deadline (+ (System/nanoTime)
                                (.toNanos TimeUnit/SECONDS 5))]
                (reset! observed-cancel cancel)
                (loop []
                  (when (and (nil? @cancel)
                             (< (System/nanoTime) deadline))
                    (Thread/sleep 1)
                    (recur)))
                (support/ensure! (= "timeout elapsed" @cancel)
                                 "query cancellation timer did not signal"
                                 {:cancel @cancel})
                (datalog/maybe-cancel)))
            thrown
            (with-redefs [datalog/eval-query evaluator]
              (try
                (datalog/qsqr [[1 2]] query-map)
                nil
                (catch Throwable failure
                  failure)))]
        (support/ensure! (instance? TimeoutException thrown)
                         "query timeout did not throw TimeoutException"
                         {:class (some-> thrown class str)})
        (support/ensure! (= "Query canceled: timeout elapsed"
                            (.getMessage ^Throwable thrown))
                         "query timeout message differed"
                         {:message (.getMessage ^Throwable thrown)})
        (support/ensure! (= 1 @evaluator-calls)
                         "controlled evaluator ran an unexpected number of times"
                         {:calls @evaluator-calls})
        (support/ensure! (= "timeout elapsed" (some-> @observed-cancel deref))
                         "controlled evaluator did not observe cancellation")
        (support/ensure! (identical? root-cancel datalog/*cancel*)
                         "qsqr did not restore the caller thread binding"))
      (finally
        (support/shutdown-executor!
         ^ScheduledExecutorService datalog/cancel-service
         "query-cancel-service")
        (support/shutdown-executor!
         ^ExecutorService datalog/query-pool
         "query-pool")
        (shutdown-agents))))
  (println "STAGE3-RESULT {:case :query-timeout, :status :passed}"))

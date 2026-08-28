(ns stage3.pool-probe
  (:require
   [datomic.async :as datomic-async]
   [datomic.core2.thread :as core2-thread]
   [stage3.support :as support])
  (:import
   (java.util.concurrent CountDownLatch ExecutionException ExecutorService
                         RejectedExecutionException SynchronousQueue
                         ThreadPoolExecutor TimeUnit)))

(defn- blocking-task
  [^CountDownLatch started ^CountDownLatch release ^CountDownLatch completed
   observation]
  (reify Runnable
    (run [_]
      (when observation
        (reset! observation [(.getName (Thread/currentThread))
                             (.isDaemon (Thread/currentThread))]))
      (.countDown started)
      (try
        (.await release 5 TimeUnit/SECONDS)
        (finally
          (.countDown completed))))))

(defn -main
  [& args]
  (support/ensure-no-args! args)
  (let [discard-started (CountDownLatch. 1)
        discard-release (CountDownLatch. 1)
        discard-completed (CountDownLatch. 1)
        discarded (CountDownLatch. 1)
        discarded-ran (atom false)
        discard-pool (ThreadPoolExecutor. 1 1 0 TimeUnit/MILLISECONDS
                                            (SynchronousQueue.))
        handoff-started (CountDownLatch. 1)
        handoff-release (CountDownLatch. 1)
        handoff-completed (CountDownLatch. 1)
        rejected-ran (atom false)
        worker-observation (atom nil)
        handoff-pool (core2-thread/handoff-thread-pool
                      {:name "stage3-handoff-"
                       :core-threads 1
                       :max-threads 1
                       :metrics? false})
        future-release (CountDownLatch. 1)
        future-started (CountDownLatch. 1)
        future-pool (core2-thread/thread-pool
                     {:name "stage3-future-"
                      :nthreads 1
                      :metrics? false})]
    (try
      (.setRejectedExecutionHandler
       discard-pool
       (datomic-async/discard-policy #(.countDown discarded)))
      (.execute discard-pool
                (blocking-task discard-started
                               discard-release
                               discard-completed
                               nil))
      (support/await-latch! discard-started "discard-pool saturation")
      (.execute discard-pool
                (reify Runnable
                  (run [_]
                    (reset! discarded-ran true))))
      (support/await-latch! discarded "discard callback")
      (support/ensure! (false? @discarded-ran)
                       "discarded task unexpectedly ran")
      (.countDown discard-release)
      (support/await-latch! discard-completed "discard-pool blocker cleanup")

      (.execute ^ExecutorService handoff-pool
                (blocking-task handoff-started
                               handoff-release
                               handoff-completed
                               worker-observation))
      (support/await-latch! handoff-started "handoff-pool saturation")
      (let [rejected
            (try
              (.execute ^ExecutorService handoff-pool
                        (reify Runnable
                          (run [_]
                            (reset! rejected-ran true))))
              nil
              (catch RejectedExecutionException failure
                failure))]
        (support/ensure! (instance? RejectedExecutionException rejected)
                         "saturated handoff pool did not reject"))
      (support/ensure! (false? @rejected-ran)
                       "rejected handoff task unexpectedly ran")
      (support/ensure! (and (true? (second @worker-observation))
                            (.startsWith ^String (first @worker-observation)
                                         "stage3-handoff-"))
                       "handoff worker naming/daemon contract differed"
                       {:worker @worker-observation})
      (.countDown handoff-release)
      (support/await-latch! handoff-completed "handoff-pool blocker cleanup")

      (support/ensure! (= :pfuture-value
                          @(core2-thread/pfuture (fn [] :pfuture-value)
                                                future-pool))
                       "core2 pfuture lost its return value")
      (let [failure (ex-info "stage3-core2-pfuture" {:case :pool})
            thrown (try
                     @(core2-thread/pfuture (fn [] (throw failure))
                                           future-pool)
                     nil
                     (catch ExecutionException execution-failure
                       execution-failure))]
        (support/ensure! (identical? failure (.getCause ^ExecutionException thrown))
                         "core2 pfuture lost its failure cause"))
      (let [blocked (core2-thread/pfuture
                     (fn []
                       (.countDown future-started)
                       (.await future-release 5 TimeUnit/SECONDS)
                       :released)
                     future-pool)]
        (support/await-latch! future-started "pfuture bounded-deref barrier")
        (support/ensure! (= ::timeout (deref blocked 20 ::timeout))
                         "core2 pfuture did not honor bounded deref")
        (.countDown future-release)
        (support/ensure! (= :released (deref blocked 5000 ::timeout))
                         "core2 pfuture did not complete after release"))
      (finally
        (.countDown discard-release)
        (.countDown handoff-release)
        (.countDown future-release)
        (support/shutdown-executor! future-pool "core2-future-pool")
        (support/shutdown-executor! handoff-pool "core2-handoff-pool")
        (support/shutdown-executor! discard-pool "discard-pool")
        (shutdown-agents))))
  (println "STAGE3-RESULT {:case :pool-rejection, :status :passed}"))

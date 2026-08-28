(ns stage3.promise-probe
  (:require
   [datomic.cleanup :as cleanup]
   [datomic.promise :as promise]
   [datomic.queue :as queue]
   [stage3.support :as support])
  (:import
   (datomic ListenableFuture)
   (datomic.cleanup Manager)
   (java.lang Thread$UncaughtExceptionHandler)
   (java.lang.ref Reference)
   (java.util.concurrent CancellationException CountDownLatch ExecutionException
                         Executor Executors Future RejectedExecutionException
                         TimeUnit TimeoutException)))

(defn- listener
  [seen value ^CountDownLatch latch]
  (reify Runnable
    (run [_]
      (swap! seen conj value)
      (.countDown latch))))

(defn- execution-exception
  [f]
  (try
    (f)
    nil
    (catch ExecutionException failure
      failure)))

(defn- timeout-exception
  [f]
  (try
    (f)
    nil
    (catch TimeoutException failure
      failure)))

(defn- exercise-cleanup-manager!
  []
  (let [manager (cleanup/create-manager)
        object (Object.)
        cleanup-calls (atom 0)
        cleanup-fn #(swap! cleanup-calls inc)
        sentinel (Object.)]
    (support/ensure! (identical? object
                                 (cleanup/register-cleanup manager object cleanup-fn))
                     "cleanup registration did not return the referent")
    (support/ensure! (identical? sentinel (queue/poll manager sentinel 10))
                     "cleanup queue was unexpectedly non-empty before enqueue")
    (let [phantoms (.-phantoms ^Manager manager)
          refs (vec (.keySet ^java.util.concurrent.ConcurrentHashMap phantoms))]
      (support/ensure! (= 1 (count refs))
                       "cleanup registration did not create exactly one phantom"
                       {:phantom-count (count refs)})
      (support/ensure! (.enqueue ^Reference (first refs))
                       "manual phantom enqueue was rejected")
      (let [queued-cleanup (queue/poll manager sentinel 1000)]
        (support/ensure! (identical? cleanup-fn queued-cleanup)
                         "cleanup manager returned the wrong callback")
        (queued-cleanup))
      (support/ensure! (zero? (.size ^java.util.concurrent.ConcurrentHashMap phantoms))
                       "cleanup manager retained a consumed phantom")
      (support/ensure! (= 1 @cleanup-calls)
                       "cleanup callback did not run exactly once"
                       {:cleanup-calls @cleanup-calls}))))

(defn -main
  [& args]
  (support/ensure-no-args! args)
  (let [executor (Executors/newFixedThreadPool 2)
        old-handler (Thread/getDefaultUncaughtExceptionHandler)]
    (try
      (let [future (promise/settable-future)
            seen (atom #{})
            before-latch (CountDownLatch. 2)
            after-latch (CountDownLatch. 1)]
        (support/ensure! (identical? ::pending (deref future 20 ::pending))
                         "pending future did not honor bounded deref")
        (support/ensure! (instance? TimeoutException
                                    (timeout-exception
                                     #(.get ^Future future
                                            20
                                            TimeUnit/MILLISECONDS)))
                         "pending Future.get did not time out")
        (.addListener ^ListenableFuture future
                      (listener seen :before-a before-latch)
                      executor)
        (.addListener ^ListenableFuture future
                      (listener seen :before-b before-latch)
                      executor)
        (support/ensure! (identical? future (deliver future :delivered-value))
                         "initial delivery did not return the future")
        (support/await-latch! before-latch "listeners registered before delivery")
        (.addListener ^ListenableFuture future
                      (listener seen :after after-latch)
                      executor)
        (support/await-latch! after-latch "listener registered after delivery")
        (support/ensure! (= #{:before-a :before-b :after} @seen)
                         "listener delivery set differed"
                         {:seen @seen})
        (support/ensure! (= :delivered-value @future)
                         "deref returned the wrong delivered value")
        (support/ensure! (= :delivered-value (.get ^Future future))
                         "Future.get returned the wrong delivered value")
        (support/ensure! (nil? (deliver future :ignored-value))
                         "second delivery was not rejected")
        (support/ensure! (= :delivered-value @future)
                         "second delivery changed the stored value"))

      (let [failure (ex-info "stage3-promise-failure" {:case :promise})
            delivered (promise/delivered failure)
            thrown (execution-exception #(.get ^Future delivered))]
        (support/ensure! (identical? failure (.getCause ^ExecutionException thrown))
                         "throwable delivery did not preserve its cause"))

      (let [cancelled (promise/settable-future)]
        (support/ensure! (.cancel ^Future cancelled true)
                         "initial cancellation was rejected")
        (support/ensure! (not (.cancel ^Future cancelled true))
                         "duplicate cancellation was accepted")
        (support/ensure! (.isDone ^Future cancelled)
                         "cancelled future was not done")
        (support/ensure! (.isCancelled ^Future cancelled)
                         "cancelled future did not report cancellation")
        (let [thrown (execution-exception #(.get ^Future cancelled))]
          (support/ensure! (instance? CancellationException
                                      (.getCause ^ExecutionException thrown))
                           "cancelled future did not retain CancellationException")))

      (let [rejection (RejectedExecutionException. "stage3-listener-rejected")
            reported (atom nil)
            reported-latch (CountDownLatch. 1)
            rejecting-executor
            (reify Executor
              (execute [_ _]
                (throw rejection)))
            handler
            (reify Thread$UncaughtExceptionHandler
              (uncaughtException [_ thread failure]
                (reset! reported [thread failure])
                (.countDown reported-latch)))
            future (promise/settable-future)]
        (Thread/setDefaultUncaughtExceptionHandler handler)
        (try
          (.addListener ^ListenableFuture future
                        (reify Runnable (run [_] nil))
                        rejecting-executor)
          (support/ensure! (identical? future (deliver future :survived-rejection))
                           "listener rejection poisoned promise delivery")
          (support/await-latch! reported-latch "rejected-listener reporting")
          (support/ensure! (identical? rejection (second @reported))
                           "listener rejection was not reported unchanged")
          (support/ensure! (= :survived-rejection @future)
                           "promise lost its value after listener rejection")
          (finally
            (Thread/setDefaultUncaughtExceptionHandler old-handler))))

      (exercise-cleanup-manager!)
      (finally
        (Thread/setDefaultUncaughtExceptionHandler old-handler)
        (support/shutdown-executor! executor "promise-listener-pool")
        (shutdown-agents))))
  (println "STAGE3-RESULT {:case :promise-cleanup, :status :passed}"))

(ns stage3.support
  (:import
   (java.util.concurrent CountDownLatch ExecutorService TimeUnit)))

(def default-timeout-ms 5000)

(defn fail!
  ([message]
   (fail! message {}))
  ([message data]
   (throw (ex-info message (assoc data :stage3/local-probe true)))))

(defn ensure!
  ([pred message]
   (ensure! pred message {}))
  ([pred message data]
   (when-not pred
     (fail! message data))))

(defn ensure-no-args!
  [args]
  (ensure! (empty? args)
           "probe accepts no arguments"
           {:args (vec args)}))

(defn await-latch!
  ([^CountDownLatch latch label]
   (await-latch! latch label default-timeout-ms))
  ([^CountDownLatch latch label timeout-ms]
   (ensure! (.await latch (long timeout-ms) TimeUnit/MILLISECONDS)
            (str "timed out waiting for " label)
            {:label label :timeout-ms timeout-ms})))

(defn shutdown-executor!
  [^ExecutorService executor label]
  (when executor
    (.shutdownNow executor)
    (ensure! (.awaitTermination executor
                                (long default-timeout-ms)
                                TimeUnit/MILLISECONDS)
             (str "executor did not terminate: " label)
             {:label label :timeout-ms default-timeout-ms})))

(defn find-cause
  [throwable expected-class]
  (loop [cause throwable]
    (cond
      (nil? cause) nil
      (.isInstance ^Class expected-class cause) cause
      :else (recur (.getCause ^Throwable cause)))))

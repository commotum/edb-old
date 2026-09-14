;;   Copyright (c) Rich Hickey and contributors. 


(ns ^{:skip-wiki true}
  clojure.core.async.impl.mutex
  (:import [java.util.concurrent.locks Lock ReentrantLock]))

(defn mutex []
  (let [m (ReentrantLock.)]
    (reify
     Lock
     (lock [_] (.lock m))
     (unlock [_] (.unlock m)))))

#_(defn mutex []
  (let [cas (java.util.concurrent.atomic.AtomicInteger.)]
    (reify
     Lock
     (lock [_] (loop [got (.compareAndSet cas 0 1)]
                 (if got
                   nil
                   (recur (.compareAndSet cas 0 1)))))
     (unlock [_] (.set cas 0)))))

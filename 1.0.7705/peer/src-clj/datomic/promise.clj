(do
  (clojure.core/in-ns 'datomic.promise)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.util.concurrent.CancellationException)
      (clojure.core/import 'java.util.concurrent.CountDownLatch)
      (clojure.core/import 'java.util.concurrent.Executor)
      (clojure.core/import 'java.util.concurrent.TimeoutException)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/import 'java.util.concurrent.ExecutionException)))
  (when-not (.equals 'datomic.promise 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.promise))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.util.concurrent.CancellationException)
        (clojure.core/import 'java.util.concurrent.CountDownLatch)
        (clojure.core/import 'java.util.concurrent.Executor)
        (clojure.core/import 'java.util.concurrent.TimeoutException)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/import 'java.util.concurrent.ExecutionException))))
  (set! *warn-on-reflection* true)
  (defn throw-executionexception-if-throwable
    ([o]
      (when (instance? java.lang.Throwable o)
        (throw (java.util.concurrent.ExecutionException. ^java.lang.Throwable o)))
      o))
  (reset-meta!
    #'throw-executionexception-if-throwable
    (assoc
      {:arglists (clojure.core/list ['o]), :column (int 1)}
      :name
      'throw-executionexception-if-throwable
      :ns
      *ns*))
  (defn call-user-code
    ([exec listener]
      (try
        (do (.execute ^java.util.concurrent.Executor exec ^java.lang.Runnable listener) nil)
        (catch
          java.lang.Throwable
          t
          (let [temp__5802__auto__ (java.lang.Thread/getDefaultUncaughtExceptionHandler)]
            (if temp__5802__auto__
              (let [h temp__5802__auto__]
                (.uncaughtException
                  ^java.lang.Thread$UncaughtExceptionHandler h
                  (java.lang.Thread/currentThread)
                  ^java.lang.Throwable t)
                nil)
              (do (.printStackTrace ^java.lang.Throwable t) nil)))))))
  (reset-meta!
    #'call-user-code
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'exec {:tag 'Executor}) 'listener]),
       :column (int 1)}
      :name
      'call-user-code
      :ns
      *ns*))
  (defn settable-future
    ([]
      (let [d (java.util.concurrent.CountDownLatch. (int 1)) listeners (atom []) v (atom d)]
        (reify
          clojure.lang.IPending
          datomic.ListenableFuture
          clojure.lang.IBlockingDeref
          java.util.concurrent.Future
          clojure.lang.IDeref
          clojure.lang.IFn
          (^void addListener
            [this ^java.lang.Runnable listener ^java.util.concurrent.Executor exec]
            (do
              (let [execute_now (locking listeners
                                 (if (.isRealized this)
                                   true
                                   (do (swap! listeners conj [listener exec]) false)))]
                (when execute_now (call-user-code exec listener)))
              nil))
          (invoke
            [this x]
            (when (and
                    (clojure.lang.Numbers/isPos
                      (long (.getCount ^java.util.concurrent.CountDownLatch d)))
                    (compare-and-set! v d x))
              (locking listeners (do (.countDown ^java.util.concurrent.CountDownLatch d) nil))
              (loop [seq_9125 (seq (deref listeners)) chunk_9126 nil count_9127 0 i_9128 0]
                (if (< i_9128 count_9127)
                  (let [vec__9129 (.nth ^clojure.lang.Indexed chunk_9126 (int i_9128))
                        listener (nth vec__9129 (int 0) nil)
                        exec (nth vec__9129 (int 1) nil)]
                    (call-user-code exec listener)
                    (recur seq_9125 chunk_9126 count_9127 (inc i_9128)))
                  (let [temp__5804__auto__ (seq seq_9125)]
                    (when temp__5804__auto__
                      (let [seq_9125 temp__5804__auto__]
                        (if (chunked-seq? seq_9125)
                          (let [c__6065__auto__ (chunk-first seq_9125)]
                            (recur
                              (chunk-rest seq_9125)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [vec__9132 (first seq_9125)
                                listener (nth vec__9132 (int 0) nil)
                                exec (nth vec__9132 (int 1) nil)]
                            (call-user-code exec listener)
                            (recur (next seq_9125) nil 0 0))))))))
              this))
          (^boolean isRealized [this] (zero? (.getCount ^java.util.concurrent.CountDownLatch d)))
          (deref
            [this ^long timeout_ms timeout_val]
            (if (.await
                  ^java.util.concurrent.CountDownLatch d
                  (long timeout_ms)
                  TimeUnit/MILLISECONDS)
              (throw-executionexception-if-throwable (deref v))
              timeout_val))
          (deref
            [this]
            (do
              (.await ^java.util.concurrent.CountDownLatch d)
              (throw-executionexception-if-throwable (deref v))))
          (get
            [this ^long timeout ^java.util.concurrent.TimeUnit unit]
            (let [result (.deref
                           this
                           (long
                             (.convert
                               TimeUnit/MILLISECONDS
                               (long timeout)
                               ^java.util.concurrent.TimeUnit unit))
                           this)]
              (when (= result this) (throw (java.util.concurrent.TimeoutException.)))
              (throw-executionexception-if-throwable result)))
          (get [this] (.deref this))
          (^boolean isDone [this] (.booleanValue (realized? this)))
          (^boolean isCancelled
            [this]
            (.booleanValue
              (and
                (realized? this)
                (instance? java.util.concurrent.CancellationException (deref v)))))
          (^boolean cancel
            [this ^boolean may_interrupt]
            (boolean (deliver this (java.util.concurrent.CancellationException.))))
          (^java.lang.String toString
            [this]
            (str
              "#<Future: "
              (if (.isRealized this)
                (binding [*print-length* 5 *print-level* 3] (pr-str (deref v)))
                :pending)
              ">"))))))
  (reset-meta!
    #'settable-future
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'settable-future :ns *ns*))
  (defn delivered ([o] (deliver (settable-future) o)))
  (reset-meta!
    #'delivered
    (assoc {:arglists (clojure.core/list ['o]), :column (int 1)} :name 'delivered :ns *ns*)))
(do
  (clojure.core/in-ns 'datomic.core2.thread)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list 'put! 'chan)]
        ['clojure.string :as 'str]
        ['cognitect.caster :as 'cast])
      (clojure.core/import 'java.util.concurrent.Executor)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.util.concurrent.Executors)
      (clojure.core/import 'java.util.concurrent.Future)
      (clojure.core/import 'java.util.concurrent.RejectedExecutionException)
      (clojure.core/import 'java.util.concurrent.RejectedExecutionHandler)
      (clojure.core/import 'java.util.concurrent.SynchronousQueue)
      (clojure.core/import 'java.util.concurrent.ThreadFactory)
      (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
      (clojure.core/import 'java.util.concurrent.TimeUnit)))
  (when-not (.equals 'datomic.core2.thread 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.thread))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :refer (clojure.core/list 'put! 'chan)]
          ['clojure.string :as 'str]
          ['cognitect.caster :as 'cast])
        (clojure.core/import 'java.util.concurrent.Executor)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.util.concurrent.Executors)
        (clojure.core/import 'java.util.concurrent.Future)
        (clojure.core/import 'java.util.concurrent.RejectedExecutionException)
        (clojure.core/import 'java.util.concurrent.RejectedExecutionHandler)
        (clojure.core/import 'java.util.concurrent.SynchronousQueue)
        (clojure.core/import 'java.util.concurrent.ThreadFactory)
        (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
        (clojure.core/import 'java.util.concurrent.TimeUnit))))
  (set! *warn-on-reflection* true)
  (def binding-conveyor-fn (deref #'clojure.core/binding-conveyor-fn))
  (defn pthread-fn
    ([exec fn] (.execute ^java.util.concurrent.Executor exec (binding-conveyor-fn fn)) nil))
  (defn pthread
    ([&form &env exec & body]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'f)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/fn)
                        (clojure.core/list (apply vector (seq (concat))))
                        body)))))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'datomic.core2.thread/pthread-fn)
                (clojure.core/list exec)
                (clojure.core/list 'f))))))))
  (.setMacro #'pthread)
  (defn pfuture
    ([f exec]
      (let [fut (.submit
                  ^java.util.concurrent.ExecutorService exec
                  ^java.util.concurrent.Callable (binding-conveyor-fn f))]
        (reify
          clojure.lang.IBlockingDeref
          clojure.lang.IDeref
          (deref
            [this ^long timeout_ms timeout_val]
            (try
              (.get ^java.util.concurrent.Future fut (long timeout_ms) TimeUnit/MILLISECONDS)
              (catch java.util.concurrent.TimeoutException e timeout_val)))
          (deref [this] (.get ^java.util.concurrent.Future fut))))))
  (defn daemon-factory
    ([name_prefix group]
      (let [idx (atom 0)]
        (reify
          java.util.concurrent.ThreadFactory
          (^java.lang.Thread newThread
            [this ^java.lang.Runnable runnable]
            (doto
              (if group
                (java.lang.Thread. ^java.lang.ThreadGroup group ^java.lang.Runnable runnable)
                (.newThread (Executors/defaultThreadFactory) ^java.lang.Runnable runnable))
              (.setName (str name_prefix (swap! idx inc)))
              (.setDaemon (boolean (.booleanValue true))))))))
    ([name_prefix] (daemon-factory name_prefix nil)))
  (defn fixed-thread-pool
    ([name_prefix n group]
      (Executors/newFixedThreadPool (int ^java.lang.Number n) (daemon-factory name_prefix group)))
    ([name_prefix n] (fixed-thread-pool name_prefix n nil)))
  (defn result-chan
    ([n xform] (clojure.core.async/chan n (comp xform (take n))))
    ([n] (clojure.core.async/chan n (take n)))
    ([] (clojure.core.async/chan 1 (take 1))))
  (defn pfuture-ch
    ([f exec ch]
      (do
        (pfuture (fn fn__21016 ([] (clojure.core.async/put! ch (^clojure.lang.IFn f)))) exec)
        ch))
    ([f exec] (pfuture-ch f exec (result-chan))))
  (defn pmap-n
    ([n f coll & colls]
      (let [step (fn step
                   ([cs]
                     (lazy-seq
                       (let [ss (map seq cs)]
                         (when (every? identity ss)
                           (cons (map first ss) (^clojure.lang.IFn step (map rest ss))))))))]
        (pmap-n
          n
          (fn fn__21038 ([p1__21020#] (apply f p1__21020#)))
          (^clojure.lang.IFn step (cons coll colls)))))
    ([n f coll]
      (let [rets (map
                   (fn fn__21021
                     ([p1__21019#]
                       (future-call (fn fn__21022 ([] (^clojure.lang.IFn f p1__21019#))))))
                   coll)
            step (fn step
                   ([p__21025 fs]
                     (let [vec__21027 p__21025
                           seq__21028 (seq vec__21027)
                           first__21029 (first seq__21028)
                           seq__21028 (next seq__21028)
                           x first__21029
                           xs seq__21028
                           vs vec__21027]
                       (lazy-seq
                         (let [temp__5802__auto__ (seq fs)]
                           (if temp__5802__auto__
                             (let [s temp__5802__auto__]
                               (cons (deref x) (^clojure.lang.IFn step xs (rest s))))
                             (map deref vs)))))))]
        (^clojure.lang.IFn step rets (drop n rets)))))
  (deftype
    ObservableThreadPool
    [pool callback]
    java.util.concurrent.ExecutorService
    (^java.util.concurrent.Future submit
      [this ^java.lang.Runnable task result]
      (let [result (.submit
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.lang.Runnable task
                     result)]
        (^clojure.lang.IFn callback pool)
        result))
    (^java.util.concurrent.Future submit
      [this ^java.lang.Runnable task]
      (let [result (.submit
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.lang.Runnable task)]
        (^clojure.lang.IFn callback pool)
        result))
    (^java.util.concurrent.Future submit
      [this ^java.util.concurrent.Callable task]
      (let [result (.submit
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.util.concurrent.Callable task)]
        (^clojure.lang.IFn callback pool)
        result))
    (^java.util.List shutdownNow
      [this]
      (.shutdownNow ^java.util.concurrent.ThreadPoolExecutor pool))
    (^void shutdown [this] (do (.shutdown ^java.util.concurrent.ThreadPoolExecutor pool) nil))
    (^boolean isTerminated [this] (.isTerminated ^java.util.concurrent.ThreadPoolExecutor pool))
    (^boolean isShutdown [this] (.isShutdown ^java.util.concurrent.ThreadPoolExecutor pool))
    (invokeAny
      [this ^java.util.Collection tasks ^long timeout ^java.util.concurrent.TimeUnit unit]
      (let [result (.invokeAny
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.util.Collection tasks
                     (long timeout)
                     ^java.util.concurrent.TimeUnit unit)]
        (^clojure.lang.IFn callback pool)
        result))
    (invokeAny
      [this ^java.util.Collection tasks]
      (let [result (.invokeAny
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.util.Collection tasks)]
        (^clojure.lang.IFn callback pool)
        result))
    (^java.util.List invokeAll
      [this ^java.util.Collection tasks ^long timeout ^java.util.concurrent.TimeUnit unit]
      (let [result (.invokeAll
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.util.Collection tasks
                     (long timeout)
                     ^java.util.concurrent.TimeUnit unit)]
        (^clojure.lang.IFn callback pool)
        result))
    (^java.util.List invokeAll
      [this ^java.util.Collection tasks]
      (let [result (.invokeAll
                     ^java.util.concurrent.AbstractExecutorService pool
                     ^java.util.Collection tasks)]
        (^clojure.lang.IFn callback pool)
        result))
    (^void execute
      [this ^java.lang.Runnable task]
      (do
        (.execute ^java.util.concurrent.ThreadPoolExecutor pool ^java.lang.Runnable task)
        (^clojure.lang.IFn callback pool)
        nil))
    (^boolean awaitTermination
      [this ^long timeout ^java.util.concurrent.TimeUnit unit]
      (.awaitTermination
        ^java.util.concurrent.ThreadPoolExecutor pool
        (long timeout)
        ^java.util.concurrent.TimeUnit unit)))
  (clojure.core/import 'datomic.core2.thread.ObservableThreadPool)
  (defn ->ObservableThreadPool
    ([pool callback] (datomic.core2.thread.ObservableThreadPool. pool callback)))
  (defn clojure-name->metric-name ([s] (str/join "." (map str/capitalize (str/split s #"\W")))))
  (defmulti
    cast-queue-metric
    (fn fn__21048 ([pool _] (class (.getQueue ^java.util.concurrent.ThreadPoolExecutor pool)))))
  (defmethod
    cast-queue-metric
    :default
    fn__21053
    ([pool metric]
      (cast/metric*
        cast/instance
        {:name metric,
         :value
         (java.lang.Integer/valueOf
           (int (count (.getQueue ^java.util.concurrent.ThreadPoolExecutor pool)))),
         :units :count})))
  (defmethod cast-queue-metric java.util.concurrent.SynchronousQueue fn__21055 ([_ _] nil))
  (defn observable-thread-pool
    ([pool name]
      (->ObservableThreadPool
        pool
        (let [mname (clojure-name->metric-name name)
              active_metric (keyword (str "Pool." mname ".Active"))
              queued_metric (keyword (str "Pool." mname ".Queued"))
              rejected_metric (keyword (str "Pool." mname ".Rejected"))]
          (.setRejectedExecutionHandler
            ^java.util.concurrent.ThreadPoolExecutor pool
            (reify
              java.util.concurrent.RejectedExecutionHandler
              (^void rejectedExecution
                [this ^java.lang.Runnable _ ^java.util.concurrent.ThreadPoolExecutor _]
                (do
                  (cast/metric* cast/instance {:name rejected_metric, :value 1, :units :count})
                  (throw (java.util.concurrent.RejectedExecutionException.))))))
          (fn fn__21059
            ([pool]
              (cast/metric*
                cast/instance
                {:name active_metric,
                 :value
                 (java.lang.Integer/valueOf
                   (int (.getActiveCount ^java.util.concurrent.ThreadPoolExecutor pool))),
                 :units :count})
              (cast-queue-metric pool queued_metric)))))))
  (defn thread-pool
    ([p__21062]
      (let [map__21063 p__21062
            map__21063 (if (seq? map__21063)
                         (if (next map__21063)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21063))
                           (if (seq map__21063) (first map__21063) {}))
                         map__21063)
            name (get map__21063 :name)
            nthreads (get map__21063 :nthreads)
            metrics? (get map__21063 :metrics? true)]
        (when-not name (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'name)))))
        (when-not nthreads
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'nthreads)))))
        (let [pool (Executors/newFixedThreadPool
                     (int ^java.lang.Number nthreads)
                     (daemon-factory name))]
          (if metrics? (observable-thread-pool pool name) pool)))))
  (defn cached-thread-pool
    ([p__21065]
      (let [map__21066 p__21065
            map__21066 (if (seq? map__21066)
                         (if (next map__21066)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21066))
                           (if (seq map__21066) (first map__21066) {}))
                         map__21066)
            name (get map__21066 :name)
            metrics? (get map__21066 :metrics? true)]
        (when-not name (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'name)))))
        (let [pool (Executors/newCachedThreadPool (daemon-factory name))]
          (if metrics? (observable-thread-pool pool name) pool)))))
  (defn handoff-thread-pool
    ([p__21068]
      (let [map__21069 p__21068
            map__21069 (if (seq? map__21069)
                         (if (next map__21069)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21069))
                           (if (seq map__21069) (first map__21069) {}))
                         map__21069)
            core_threads (get map__21069 :core-threads 2)
            name (get map__21069 :name)
            max_threads (get map__21069 :max-threads)
            metrics? (get map__21069 :metrics? true)]
        (when-not name (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'name)))))
        (when-not max_threads
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'max-threads)))))
        (let [pool (java.util.concurrent.ThreadPoolExecutor.
                     (int core_threads)
                     (int max_threads)
                     60
                     TimeUnit/SECONDS
                     (java.util.concurrent.SynchronousQueue.)
                     ^java.util.concurrent.ThreadFactory (daemon-factory name))]
          (if metrics? (observable-thread-pool pool name) pool))))))

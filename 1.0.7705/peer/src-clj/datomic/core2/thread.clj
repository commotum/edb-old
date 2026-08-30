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
  (.setMeta (clojure.lang.RT/var "datomic.core2.thread" "binding-conveyor-fn") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.core2.thread" "binding-conveyor-fn")
    (deref #'clojure.core/binding-conveyor-fn))
  (def pthread-fn
   (fn pthread_fn
     ([exec fn] (.execute ^java.util.concurrent.Executor exec (binding-conveyor-fn fn)) nil)))
  (reset-meta!
    #'pthread-fn
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'exec {:tag 'Executor}) 'fn]), :column (int 1)}
      :name
      'pthread-fn
      :ns
      *ns*))
  (def pthread
   (fn pthread
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
                 (clojure.core/list 'f)))))))))
  (reset-meta!
    #'pthread
    (assoc
      {:arglists (clojure.core/list ['exec '& 'body]), :column (int 1)}
      :name
      'pthread
      :ns
      *ns*))
  (.setMacro #'pthread)
  (def pfuture
   (fn pfuture
     ([f exec]
       (let [fut (.submit ^java.util.concurrent.ExecutorService exec (binding-conveyor-fn f))]
         (reify
           clojure.lang.IBlockingDeref
           clojure.lang.IDeref
           (deref
             [this ^long timeout_ms timeout_val]
             (try
               (.get ^java.util.concurrent.Future fut (long timeout_ms) TimeUnit/MILLISECONDS)
               (catch java.util.concurrent.TimeoutException e timeout_val)))
           (deref [this] (.get ^java.util.concurrent.Future fut)))))))
  (reset-meta!
    #'pfuture
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'f {:tag 'java.util.concurrent.Callable})
          (.withMeta 'exec {:tag 'ExecutorService})]),
       :column (int 1)}
      :name
      'pfuture
      :ns
      *ns*))
  (def daemon-factory
   (fn daemon_factory
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
     ([name_prefix] (daemon-factory name_prefix nil))))
  (reset-meta!
    #'daemon-factory
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta ['name-prefix] {:tag 'java.util.concurrent.ThreadFactory})
         (.withMeta
           ['name-prefix (.withMeta 'group {:tag 'ThreadGroup})]
           {:tag 'java.util.concurrent.ThreadFactory})),
       :column (int 1)}
      :name
      'daemon-factory
      :ns
      *ns*))
  (def fixed-thread-pool
   (fn fixed_thread_pool
     ([name_prefix n group]
       (Executors/newFixedThreadPool (int ^java.lang.Number n) (daemon-factory name_prefix group)))
     ([name_prefix n] (fixed-thread-pool name_prefix n nil))))
  (reset-meta!
    #'fixed-thread-pool
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta ['name-prefix 'n] {:tag 'java.util.concurrent.ExecutorService})
         (.withMeta ['name-prefix 'n 'group] {:tag 'java.util.concurrent.ExecutorService})),
       :column (int 1)}
      :name
      'fixed-thread-pool
      :ns
      *ns*))
  (def result-chan
   (fn result_chan
     ([n xform] (clojure.core.async/chan n (comp xform (take n))))
     ([n] (clojure.core.async/chan n (take n)))
     ([] (clojure.core.async/chan 1 (take 1)))))
  (reset-meta!
    #'result-chan
    (assoc
      {:arglists (clojure.core/list [] ['n] ['n 'xform]), :column (int 1)}
      :name
      'result-chan
      :ns
      *ns*))
  (def pfuture-ch
   (fn pfuture_ch
     ([f exec ch]
       (do
         (pfuture (fn fn__21873 ([] (clojure.core.async/put! ch (^clojure.lang.IFn f)))) exec)
         ch))
     ([f exec] (pfuture-ch f exec (result-chan)))))
  (reset-meta!
    #'pfuture-ch
    (assoc
      {:arglists (clojure.core/list ['f 'exec] ['f 'exec 'ch]), :column (int 1)}
      :name
      'pfuture-ch
      :ns
      *ns*))
  (def pmap-n
   (fn pmap_n
     ([n f coll & colls]
       (let [step (fn step
                    ([cs]
                      (lazy-seq
                        (let [ss (map seq cs)]
                          (when (every? identity ss)
                            (cons (map first ss) (^clojure.lang.IFn step (map rest ss))))))))]
         (pmap-n
           n
           (fn fn__21895 ([p1__21877#] (apply f p1__21877#)))
           (^clojure.lang.IFn step (cons coll colls)))))
     ([n f coll]
       (let [rets (map
                    (fn fn__21878
                      ([p1__21876#]
                        (future-call (fn fn__21879 ([] (^clojure.lang.IFn f p1__21876#))))))
                    coll)
             step (fn step
                    ([p__21882 fs]
                      (let [vec__21884 p__21882
                            seq__21885 (seq vec__21884)
                            first__21886 (first seq__21885)
                            seq__21885 (next seq__21885)
                            x first__21886
                            xs seq__21885
                            vs vec__21884]
                        (lazy-seq
                          (let [temp__5823__auto__ (seq fs)]
                            (if temp__5823__auto__
                              (let [s temp__5823__auto__]
                                (cons (deref x) (^clojure.lang.IFn step xs (rest s))))
                              (map deref vs)))))))]
         (^clojure.lang.IFn step rets (drop n rets))))))
  (reset-meta!
    #'pmap-n
    (assoc
      {:arglists (clojure.core/list ['n 'f 'coll] ['n 'f 'coll '& 'colls]), :column (int 1)}
      :name
      'pmap-n
      :ns
      *ns*))
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
  (reset-meta!
    #'->ObservableThreadPool
    (assoc
      {:arglists (clojure.core/list ['pool 'callback]), :column (int 1)}
      :name
      '->ObservableThreadPool
      :ns
      *ns*))
  (defn clojure-name->metric-name ([s] (str/join "." (map str/capitalize (str/split s #"\W")))))
  (reset-meta!
    #'clojure-name->metric-name
    (assoc
      {:arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'clojure-name->metric-name
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.core2.thread" "cast-queue-metric") {:column (int 1)})
  (let [v__5813__auto__ #'cast-queue-metric]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.core2.thread" "cast-queue-metric") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.core2.thread" "cast-queue-metric")
        (clojure.lang.MultiFn.
          "cast-queue-metric"
          (fn fn__21905
            ([pool _] (class (.getQueue ^java.util.concurrent.ThreadPoolExecutor pool))))
          :default
          #'clojure.core/global-hierarchy))
      #'cast-queue-metric))
  (defmethod
    cast-queue-metric
    :default
    fn__21910
    ([pool metric]
      (cast/metric*
        cast/instance
        {:name metric,
         :value
         (java.lang.Integer/valueOf
           (int (count (.getQueue ^java.util.concurrent.ThreadPoolExecutor pool)))),
         :units :count})))
  (defmethod cast-queue-metric java.util.concurrent.SynchronousQueue fn__21912 ([_ _] nil))
  (def observable-thread-pool
   (fn observable_thread_pool
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
           (fn fn__21916
             ([pool]
               (cast/metric*
                 cast/instance
                 {:name active_metric,
                  :value
                  (java.lang.Integer/valueOf
                    (int (.getActiveCount ^java.util.concurrent.ThreadPoolExecutor pool))),
                  :units :count})
               (cast-queue-metric pool queued_metric))))))))
  (reset-meta!
    #'observable-thread-pool
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'pool {:tag 'ThreadPoolExecutor}) 'name]
           {:tag 'java.util.concurrent.ExecutorService})),
       :column (int 1)}
      :name
      'observable-thread-pool
      :ns
      *ns*))
  (def thread-pool
   (fn thread_pool
     ([p__21919]
       (let [map__21920 p__21919
             map__21920 (if (seq? map__21920)
                          (if (next map__21920)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21920))
                            (if (seq map__21920) (first map__21920) {}))
                          map__21920)
             name (get map__21920 :name)
             nthreads (get map__21920 :nthreads)
             metrics? (get map__21920 :metrics? true)]
         (when-not name (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'name)))))
         (when-not nthreads
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'nthreads)))))
         (let [pool (Executors/newFixedThreadPool
                      (int ^java.lang.Number nthreads)
                      (daemon-factory name))]
           (if metrics? (observable-thread-pool pool name) pool))))))
  (reset-meta!
    #'thread-pool
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [{:keys ['name 'nthreads 'metrics?], :or {'metrics? true}}]
           {:tag 'java.util.concurrent.ExecutorService})),
       :column (int 1)}
      :name
      'thread-pool
      :ns
      *ns*))
  (def cached-thread-pool
   (fn cached_thread_pool
     ([p__21922]
       (let [map__21923 p__21922
             map__21923 (if (seq? map__21923)
                          (if (next map__21923)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21923))
                            (if (seq map__21923) (first map__21923) {}))
                          map__21923)
             name (get map__21923 :name)
             metrics? (get map__21923 :metrics? true)]
         (when-not name (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'name)))))
         (let [pool (Executors/newCachedThreadPool (daemon-factory name))]
           (if metrics? (observable-thread-pool pool name) pool))))))
  (reset-meta!
    #'cached-thread-pool
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [{:keys ['name 'metrics?], :or {'metrics? true}}]
           {:tag 'java.util.concurrent.ExecutorService})),
       :column (int 1)}
      :name
      'cached-thread-pool
      :ns
      *ns*))
  (def handoff-thread-pool
   (fn handoff_thread_pool
     ([p__21925]
       (let [map__21926 p__21925
             map__21926 (if (seq? map__21926)
                          (if (next map__21926)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21926))
                            (if (seq map__21926) (first map__21926) {}))
                          map__21926)
             core_threads (get map__21926 :core-threads 2)
             name (get map__21926 :name)
             max_threads (get map__21926 :max-threads)
             metrics? (get map__21926 :metrics? true)]
         (when-not name (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'name)))))
         (when-not max_threads
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'max-threads)))))
         (let [pool (java.util.concurrent.ThreadPoolExecutor.
                      (int core_threads)
                      (int max_threads)
                      60
                      TimeUnit/SECONDS
                      (java.util.concurrent.SynchronousQueue.)
                      (daemon-factory name))]
           (if metrics? (observable-thread-pool pool name) pool))))))
  (reset-meta!
    #'handoff-thread-pool
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [{:keys ['core-threads 'name 'max-threads 'metrics?],
             :or {'core-threads 2, 'metrics? true}}]
           {:tag 'java.util.concurrent.ExecutorService})),
       :column (int 1)}
      :name
      'handoff-thread-pool
      :ns
      *ns*)))
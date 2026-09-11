;; ATOMIC-NOTE [scope] Stage 1 admission-to-durable-notification pilot, not full runtime coverage.
;; Unannotated baseline: cd7192e63d883a4a34aa7de4d5bcd17e6edb692d. Original forms are retained.
(do
  (clojure.core/in-ns 'datomic.update)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.update)
    {:doc
     "Per-database transactor runtime. Queues and applies transactions in total order, writes transaction batches to the log, publishes novelty to peers, manages the memory index, applies back pressure, and coordinates background indexing."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['compare])
      (clojure.core/require
        ['clojure.core.async :as 'a]
        ['datomic.db :as 'db]
        ['datomic.index :as 'index]
        ['clojure.java.io :as 'io]
        ['datomic.catalog :as 'catalog]
        ['datomic.config :as 'config]
        ['datomic.artemis-client :as 'aclient]
        ['datomic.artemis-server :as 'aserver]
        ['datomic.slf4j :as 'logger]
        ['datomic.fressian :as 'fressian]
        ['datomic.log :as 'log]
        ['datomic.cluster :as 'cluster]
        ['datomic.memory :as 'memory]
        ['datomic.process.events :as 'events]
        ['datomic.queue :as 'queue]
        ['datomic.common :as 'common]
        ['datomic.error :as 'error]
        ['datomic.math :as 'math]
        ['clojure.set :as 'set]
        ['datomic.transaction :as 'tx]
        ['datomic.coordination :as 'coord]
        ['datomic.monitor :as 'monitor]
        ['datomic.stats :as 'stats]
        ['datomic.index-direct-metrics :as 'index-direct-metrics]
        ['datomic.indexer :as 'indexer]
        ['datomic.io :as 'dio :refer (clojure.core/list 'bytestream->buf 'remaining)]
        ['datomic.process :as 'process]
        ['datomic.garbage :as 'garbage]
        ['datomic.core2.thread :as 'thread]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'java.io.Closeable)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'java.util.concurrent.ArrayBlockingQueue)
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.concurrent.ConcurrentMap)
      (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
      (clojure.core/import 'java.util.concurrent.Semaphore)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/import 'org.fressian.impl.BytesOutputStream)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.db.IDbImpl)
      (clojure.core/import 'datomic.db.IProcess)
      (clojure.core/import 'datomic.db.PrefetchDispatcher)
      (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientMessage)))
  (when-not (.equals 'datomic.update 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.update))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['compare])
        (clojure.core/require
          ['clojure.core.async :as 'a]
          ['datomic.db :as 'db]
          ['datomic.index :as 'index]
          ['clojure.java.io :as 'io]
          ['datomic.catalog :as 'catalog]
          ['datomic.config :as 'config]
          ['datomic.artemis-client :as 'aclient]
          ['datomic.artemis-server :as 'aserver]
          ['datomic.slf4j :as 'logger]
          ['datomic.fressian :as 'fressian]
          ['datomic.log :as 'log]
          ['datomic.cluster :as 'cluster]
          ['datomic.memory :as 'memory]
          ['datomic.process.events :as 'events]
          ['datomic.queue :as 'queue]
          ['datomic.common :as 'common]
          ['datomic.error :as 'error]
          ['datomic.math :as 'math]
          ['clojure.set :as 'set]
          ['datomic.transaction :as 'tx]
          ['datomic.coordination :as 'coord]
          ['datomic.monitor :as 'monitor]
          ['datomic.stats :as 'stats]
          ['datomic.index-direct-metrics :as 'index-direct-metrics]
          ['datomic.indexer :as 'indexer]
          ['datomic.io :as 'dio :refer (clojure.core/list 'bytestream->buf 'remaining)]
          ['datomic.process :as 'process]
          ['datomic.garbage :as 'garbage]
          ['datomic.core2.thread :as 'thread]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'java.io.Closeable)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'java.util.concurrent.ArrayBlockingQueue)
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.concurrent.ConcurrentMap)
        (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
        (clojure.core/import 'java.util.concurrent.Semaphore)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/import 'org.fressian.impl.BytesOutputStream)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.db.IDbImpl)
        (clojure.core/import 'datomic.db.IProcess)
        (clojure.core/import 'datomic.db.PrefetchDispatcher)
        (clojure.core/import 'org.apache.activemq.artemis.api.core.client.ClientMessage))))
  (set! *warn-on-reflection* true)
  (def max-unprocessed-queue 5)
  (reset-meta!
    #'max-unprocessed-queue
    (assoc {:const true, :column (int 1)} :name 'max-unprocessed-queue :ns *ns*))
  (def PREFETCH_CHAN_BUFFER_SIZE 10000)
  (reset-meta!
    #'PREFETCH_CHAN_BUFFER_SIZE
    (assoc {:const true, :column (int 1)} :name 'PREFETCH_CHAN_BUFFER_SIZE :ns *ns*))
  ;; Run a named critical worker with a cooperative shutdown hook. An uncaught
  ;; worker failure marks the entire transactor process as failed.
  (defn background
    ([thread_name f & args]
      (let [shutdown_hook (promise)
            proc (fn proc
                   ([]
                     (try
                       (apply f shutdown_hook args)
                       (catch
                         java.lang.Throwable
                         t
                         (do
                           (process/fail process/instance "Critical background task failed" t)
                           (throw ^java.lang.Throwable t)
                           nil)))))
            thread (java.lang.Thread. ^java.lang.Runnable proc ^java.lang.String thread_name)]
        (reify
          clojure.lang.IDeref
          clojure.lang.IFn
          datomic.common.AsyncShutdown
          (async-shutdown
            [this]
            (future-call
              (fn fn__30780
                ([]
                  (let [temp__5825__auto__ (deref shutdown_hook)]
                    (when temp__5825__auto__
                      (let [hook temp__5825__auto__] (^clojure.lang.IFn hook))))
                  (.interrupt ^java.lang.Thread thread)
                  (.join ^java.lang.Thread thread)
                  nil))))
          (deref [this] args)
          (invoke [this] (do (.start ^java.lang.Thread thread) this))))))
  (reset-meta!
    #'background
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'thread-name {:tag 'String}) 'f '& 'args]),
       :column (int 1)}
      :name
      'background
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.update" "prefetch-channels-ref") {:column (int 1)})
  (let [v__6837__auto__ #'prefetch-channels-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.update" "prefetch-channels-ref") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.update" "prefetch-channels-ref")
        (delay
          (when (config/prefetch-enabled?)
            (let [probes_channel (a/chan (a/dropping-buffer 10000))
                  segments_channel (a/chan (a/dropping-buffer 10000))
                  thread_factory (thread/daemon-factory "prefetch-worker-")
                  concurrency (config/property "datomic.prefetchConcurrency")
                  runnable (fn runnable
                             ([]
                               (loop []
                                 (do
                                   (try
                                     (let [vec__30789 (a/alts!!
                                                        [probes_channel segments_channel]
                                                        :priority
                                                        true)
                                           f (nth vec__30789 (int 0) nil)]
                                       (when f
                                         (try
                                           (^clojure.lang.IFn f)
                                           (catch
                                             java.lang.Throwable
                                             t
                                             (let [logger (org.slf4j.LoggerFactory/getLogger
                                                            "datomic.update")
                                                   ex t]
                                               (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                                 (.warn
                                                   ^org.slf4j.Logger logger
                                                   (logger/process {:event :prefetch/failure})
                                                   ^java.lang.Throwable ex)
                                                 (logger/caused-by logger ex))
                                               nil)))))
                                     (catch
                                       java.lang.Throwable
                                       t
                                       (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.update")
                                             ex t]
                                         (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                           (.warn
                                             ^org.slf4j.Logger logger
                                             (logger/process {:event :prefetch/worker-failed})
                                             ^java.lang.Throwable ex)
                                           (logger/caused-by logger ex))
                                         nil)))
                                   (recur)))
                               nil))]
              (dotimes [_ (long concurrency)]
                (.start
                  (.newThread
                    ^java.util.concurrent.ThreadFactory thread_factory
                    ^java.lang.Runnable runnable)))
              {:probes-channel probes_channel, :segments-channel segments_channel}))))
      #'prefetch-channels-ref))
  (defn prefetch-channels ([] (deref prefetch-channels-ref)))
  (reset-meta!
    #'prefetch-channels
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'prefetch-channels :ns *ns*))
  (def until-interrupt
   (fn until_interrupt
     ([&form &env desc & body]
       (when-not (keyword? desc)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list 'keyword? 'desc))))))
       (seq
         (concat
           (clojure.core/list 'datomic.slf4j/log-time)
           (clojure.core/list
             (apply
               hash-map
               (seq
                 (concat
                   (clojure.core/list :level)
                   (clojure.core/list :info)
                   (clojure.core/list :event)
                   (clojure.core/list :update/loop)
                   (clojure.core/list :task)
                   (clojure.core/list desc)))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'clojure.core/let)
                 (clojure.core/list
                   (apply
                     vector
                     (seq
                       (concat
                         (clojure.core/list 'done)
                         (clojure.core/list
                           (seq (concat (clojure.core/list 'java.lang.Object.))))))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/loop)
                       (clojure.core/list (apply vector (seq (concat))))
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'if)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'datomic.process/failing?)
                                   (clojure.core/list 'datomic.process/instance))))
                             (clojure.core/list :process-failed)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'clojure.core/let)
                                   (clojure.core/list
                                     (apply
                                       vector
                                       (seq
                                         (concat
                                           (clojure.core/list 'result)
                                           (clojure.core/list
                                             (seq
                                               (concat
                                                 (clojure.core/list 'try)
                                                 body
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'catch)
                                                       (clojure.core/list
                                                         'java.lang.InterruptedException)
                                                       (clojure.core/list '_)
                                                       (clojure.core/list 'done))))
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'catch)
                                                       (clojure.core/list
                                                         'java.io.InterruptedIOException)
                                                       (clojure.core/list '_)
                                                       (clojure.core/list 'done))))
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'catch)
                                                       (clojure.core/list
                                                         'org.apache.activemq.artemis.api.core.ActiveMQInterruptedException)
                                                       (clojure.core/list '_)
                                                       (clojure.core/list 'done))))
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'catch)
                                                       (clojure.core/list
                                                         'org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException)
                                                       (clojure.core/list '_)
                                                       (clojure.core/list 'done)))))))))))
                                   (clojure.core/list
                                     (seq
                                       (concat
                                         (clojure.core/list 'if)
                                         (clojure.core/list
                                           (seq
                                             (concat
                                               (clojure.core/list 'clojure.core/=)
                                               (clojure.core/list 'done)
                                               (clojure.core/list 'result))))
                                         (clojure.core/list :interrupted)
                                         (clojure.core/list
                                           (seq
                                             (concat
                                               (clojure.core/list 'recur))))))))))))))))))))))))
  (reset-meta!
    #'until-interrupt
    (assoc
      {:arglists (clojure.core/list ['desc '& 'body]), :column (int 1)}
      :name
      'until-interrupt
      :ns
      *ns*))
  (.setMacro #'until-interrupt)
  ;; Warm an immutable segment into the lookup cache; a prefetch failure is
  ;; observable but does not decide the transaction result.
  (defn prefetch-segment
    ([olookup k]
      (try
        (do (get olookup k) (io-stats/inc! :prefetched))
        (catch
          java.lang.Exception
          e
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update") ex e]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process {:event :prefetch/failed, :k k})
                ^java.lang.Throwable ex)
              (logger/caused-by logger ex))
            nil)))
      nil))
  (reset-meta!
    #'prefetch-segment
    (assoc
      {:arglists (clojure.core/list ['olookup 'k]), :column (int 1)}
      :name
      'prefetch-segment
      :ns
      *ns*))
  ;; Expand client-supplied segment hints onto the bounded prefetch worker channel.
  ;; Work is abandoned as soon as the corresponding transaction completes.
  (defn segment-prefetch-processor
    ([shutdown_hook & p__30803]
      (let [map__30804 p__30803
            map__30804 (if (seq? map__30804)
                         (if (next map__30804)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30804))
                           (if (seq map__30804) (first map__30804) {}))
                         map__30804)
            prefetch_queue (get map__30804 :prefetch-queue)
            olookup (get map__30804 :olookup)
            segments_channel (get map__30804 :segments-channel)
            m_30805 {:task :segment-prefetch-processor, :event :update/loop}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_30805 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [done (java.lang.Object.)]
                                      (loop []
                                        (if (process/failing? process/instance)
                                          :process-failed
                                          (let [result (try
                                                         (let [map__30811
                                                               (queue/take prefetch_queue)
                                                               map__30811
                                                               (if
                                                                 (seq? map__30811)
                                                                 (if
                                                                   (next map__30811)
                                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                     (to-array map__30811))
                                                                   (if
                                                                     (seq map__30811)
                                                                     (first map__30811)
                                                                     {}))
                                                                 map__30811)
                                                               procargs map__30811
                                                               prefetch_io_stats_bindings
                                                               (get
                                                                 map__30811
                                                                 :prefetch-io-stats-bindings)
                                                               tx_promise
                                                               (get map__30811 :tx-promise)
                                                               segments
                                                               (get-in
                                                                 procargs
                                                                 [:options :hints :segments])]
                                                           (with-bindings*
                                                             prefetch_io_stats_bindings
                                                             io-stats/inc!
                                                             :segments
                                                             (java.lang.Integer/valueOf
                                                               (int (count segments))))
                                                           (when
                                                             (not (realized? tx_promise))
                                                             (run!
                                                               (fn
                                                                 fn__30812
                                                                 ([k]
                                                                   (a/offer!
                                                                     segments_channel
                                                                     (fn
                                                                       fn__30813
                                                                       ([]
                                                                         (when-not
                                                                           (realized? tx_promise)
                                                                           (with-bindings*
                                                                             prefetch_io_stats_bindings
                                                                             (fn
                                                                               fn__30814
                                                                               ([]
                                                                                 (prefetch-segment
                                                                                   olookup
                                                                                   k))))))))))
                                                               segments)))
                                                         (catch
                                                           org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                           _
                                                           done)
                                                         (catch
                                                           java.io.InterruptedIOException
                                                           _
                                                           done)
                                                         (catch
                                                           java.lang.InterruptedException
                                                           _
                                                           done)
                                                         (catch
                                                           org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                           _
                                                           done))]
                                            (if (= done result) :interrupted (recur))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_30806 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_30807 (logger/format-as-msec (long elapsed_30806))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_30805 :msec msec_30807 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'segment-prefetch-processor
    (assoc
      {:arglists
       (clojure.core/list
         ['shutdown-hook '& {:keys ['prefetch-queue 'olookup 'segments-channel]}]),
       :column (int 1)}
      :name
      'segment-prefetch-processor
      :ns
      *ns*))
  (defn dropping-dispatcher
    ([] (reify datomic.db.PrefetchDispatcher (close [this] nil) (prefetch1 [this _] nil))))
  (reset-meta!
    #'dropping-dispatcher
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'dropping-dispatcher
      :ns
      *ns*))
  (defn channel-forwarding-dispatcher
    ([tx_promise io_stats_bindings probes_channel]
      (reify
        datomic.db.PrefetchDispatcher
        (close [this] (deliver tx_promise true))
        (prefetch1
          [this f]
          (a/offer!
            probes_channel
            (fn fn__30830
              ([] (when-not (realized? tx_promise) (with-bindings* io_stats_bindings f)))))))))
  (reset-meta!
    #'channel-forwarding-dispatcher
    (assoc
      {:arglists (clojure.core/list ['tx-promise 'io-stats-bindings 'probes-channel]),
       :column (int 1)}
      :name
      'channel-forwarding-dispatcher
      :ns
      *ns*))
  (defn ->dispatcher
    ([procargs p__30834]
      (let [map__30835 p__30834
            map__30835 (if (seq? map__30835)
                         (if (next map__30835)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30835))
                           (if (seq map__30835) (first map__30835) {}))
                         map__30835)
            probes_channel (get map__30835 :probes-channel)
            prefetch_enabled? (get map__30835 :prefetch-enabled?)
            prefetch_io_stats_bindings (common/getx procargs :prefetch-io-stats-bindings)
            tx_promise (common/getx procargs :tx-promise)]
        (if (:hints (:options procargs))
          [(dropping-dispatcher) :datomic.db/segment-prefetch]
          (if prefetch_enabled?
            [(channel-forwarding-dispatcher tx_promise prefetch_io_stats_bindings probes_channel)
             :datomic.db/probe-prefetch]
            [(dropping-dispatcher) :datomic.db/probe-prefetch])))))
  (reset-meta!
    #'->dispatcher
    (assoc
      {:arglists (clojure.core/list ['procargs {:keys ['probes-channel 'prefetch-enabled?]}]),
       :column (int 1)}
      :name
      '->dispatcher
      :ns
      *ns*))
  ;; Receive transaction submissions from the database address and enqueue them
  ;; for serial processing. The bounded queue is the first transaction back-pressure point.
  ;; ATOMIC-NOTE [observed] reader consumes tx/submit-address, decodes via
  ;; tx/read-message and routes ordinary requests to unprocessed-updates-queue.
  ;; Its broker acknowledgement follows enqueueing, not log/append; it is transport
  ;; admission. The request's :tx-promise bounds prefetch, unlike the later :logged.
  (defn reader
    ([shutdown_hook & p__30837]
      (let [map__30838 p__30837
            map__30838 (if (seq? map__30838)
                         (if (next map__30838)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30838))
                           (if (seq map__30838) (first map__30838) {}))
                         map__30838)
            db_id (get map__30838 :db-id)
            session_fn (get map__30838 :session-fn)
            unprocessed_updates_queue (get map__30838 :unprocessed-updates-queue)
            prefetch_queue (get map__30838 :prefetch-queue)
            prefetch_enabled? (get map__30838 :prefetch-enabled?)
            session (^clojure.lang.IFn session_fn)
            submit_queue (aclient/create-temporary-queue session (tx/submit-address db_id))
            consumer (aclient/create-consumer session submit_queue :window-size 0)
            sync_producer (aclient/create-producer session (tx/push-address db_id))
            sync_queue (aclient/fressian-producer session sync_producer (tx/write-handlers false))]
        (^clojure.lang.IFn shutdown_hook
          (fn fn__30839
            ([]
              (deref (common/async-shutdown consumer))
              (aclient/delete-queue session submit_queue)
              (deref (common/async-shutdown sync_producer))
              (deref (common/async-shutdown session)))))
        (let [m_30841 {:task :reader, :event :update/loop}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_30841 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [done (java.lang.Object.)]
                                        (loop []
                                          (if (process/failing? process/instance)
                                            :process-failed
                                            (let [result (try
                                                           (let
                                                             [qmsg (queue/take consumer)]
                                                             (let
                                                               [msg
                                                                (tx/read-message
                                                                  (aclient/input-stream qmsg))
                                                                type (tx/peer-message-type msg)
                                                                txprom (promise)
                                                                prefetch_io_stats_bindings
                                                                (io-stats/bare-bindings)
                                                                msg
                                                                (cond->
                                                                  msg
                                                                  (or (= type :tx) (= type :error))
                                                                  (assoc
                                                                    :prefetch-io-stats-bindings
                                                                    prefetch_io_stats_bindings
                                                                    :tx-promise
                                                                    txprom))]
                                                               (queue/put
                                                                 (let
                                                                   [G__30848 type]
                                                                   (case
                                                                     G__30848
                                                                     :sync
                                                                     sync_queue
                                                                     unprocessed_updates_queue))
                                                                 msg)
                                                               (when
                                                                 (and
                                                                   (:hints (:options msg))
                                                                   prefetch_enabled?)
                                                                 (queue/put
                                                                   prefetch_queue
                                                                   (assoc
                                                                     msg
                                                                     :prefetch-io-stats-bindings
                                                                     prefetch_io_stats_bindings
                                                                     :tx-promise
                                                                     txprom))))
                                                             (.acknowledge
                                                               ^org.apache.activemq.artemis.api.core.client.ClientMessage qmsg)
                                                             qmsg)
                                                           (catch
                                                             java.io.InterruptedIOException
                                                             _
                                                             done)
                                                           (catch
                                                             java.lang.InterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                             _
                                                             done))]
                                              (if (= done result) :interrupted (recur))))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_30842 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_30843 (logger/format-as-msec (long elapsed_30842))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_30841 :msec msec_30843 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'reader
    (assoc
      {:arglists
       (clojure.core/list
         ['shutdown-hook
          '&
          {:keys
           ['db-id 'session-fn 'unprocessed-updates-queue 'prefetch-queue 'prefetch-enabled?]}]),
       :column (int 1)}
      :name
      'reader
      :ns
      *ns*))
  ;; Install a completed index root, release the single-index permit, schedule
  ;; any remaining work, and publish the new basis to peers.
  (defn process-new-index
    ([p__30859 p__30860]
      (let [map__30861 p__30859
            map__30861 (if (seq? map__30861)
                         (if (next map__30861)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30861))
                           (if (seq map__30861) (first map__30861) {}))
                         map__30861)
            new_index (get map__30861 :new-index)
            excised (get map__30861 :excised)
            map__30862 p__30860
            map__30862 (if (seq? map__30862)
                         (if (next map__30862)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30862))
                           (if (seq map__30862) (first map__30862) {}))
                         map__30862)
            db_ref (get map__30862 :db-ref)
            index_sem (get map__30862 :index-sem)
            indexer (get map__30862 :indexer)
            db_id (get map__30862 :db-id)
            processed_updates_queue (get map__30862 :processed-updates-queue)
            priority_updates_queue (get map__30862 :priority-updates-queue)
            requested_index_t_ref (get map__30862 :requested-index-t-ref)
            result_push (get map__30862 :result-push)
            db (swap! db_ref db/complete-indexing new_index)]
        (indexer/indexing-completed indexer db_id)
        (.release ^java.util.concurrent.Semaphore index_sem)
        (indexer/queue-db-index-job indexer db_id)
        (indexer/queue-index-jobs indexer)
        (when (< (:indexBasisT db) (deref requested_index_t_ref))
          (queue/put priority_updates_queue {:request-index true}))
        (when (config/property "datomic.indexMetrics")
          (future-call
            (fn fn__30863
              ([]
                (try
                  (let [metrics (index-direct-metrics/direct-metrics db)]
                    (loop [seq_30864 (seq metrics) chunk_30865 nil count_30866 0 i_30867 0]
                      (if (< i_30867 count_30866)
                        (let [vec__30868 (.nth ^clojure.lang.Indexed chunk_30865 (int i_30867))
                              k (nth vec__30868 (int 0) nil)
                              v (nth vec__30868 (int 1) nil)]
                          (monitor/add-stat k v)
                          (recur seq_30864 chunk_30865 count_30866 (inc i_30867)))
                        (let [temp__5825__auto__ (seq seq_30864)]
                          (when temp__5825__auto__
                            (let [seq_30864 temp__5825__auto__]
                              (if (chunked-seq? seq_30864)
                                (let [c__6090__auto__ (chunk-first seq_30864)]
                                  (recur
                                    (chunk-rest seq_30864)
                                    c__6090__auto__
                                    (int (count c__6090__auto__))
                                    (int 0)))
                                (let [vec__30871 (first seq_30864)
                                      k (nth vec__30871 (int 0) nil)
                                      v (nth vec__30871 (int 1) nil)]
                                  (monitor/add-stat k v)
                                  (recur (next seq_30864) nil 0 0))))))))
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process
                            (assoc metrics :event :update/adopted-index :id (:id db)))))
                      nil))
                  (catch
                    java.lang.Throwable
                    t__8765__auto__
                    (do
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                            ex t__8765__auto__]
                        (when (.isWarnEnabled ^org.slf4j.Logger logger)
                          (.warn
                            ^org.slf4j.Logger logger
                            (logger/process "error executing future")
                            ^java.lang.Throwable ex)
                          (logger/caused-by logger ex))
                        nil)
                      (monitor/alarm :UnhandledException)
                      (throw ^java.lang.Throwable t__8765__auto__)
                      nil)))))))
        (queue/put result_push {:notification :new-index}))))
  (reset-meta!
    #'process-new-index
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['new-index 'excised]}
          {:keys
           ['db-ref
            'index-sem
            'indexer
            'db-id
            'processed-updates-queue
            'priority-updates-queue
            'requested-index-t-ref
            'result-push]}]),
       :column (int 1)}
      :name
      'process-new-index
      :ns
      *ns*))
  ;; Record the requested target basis and start indexing when the memory index
  ;; and indexing semaphore permit it.
  (defn process-request-index
    ([p__30878 p__30879]
      (let [map__30880 p__30878
            map__30880 (if (seq? map__30880)
                         (if (next map__30880)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30880))
                           (if (seq map__30880) (first map__30880) {}))
                         map__30880)
            up_to_t (get map__30880 :up-to-t)
            map__30881 p__30879
            map__30881 (if (seq? map__30881)
                         (if (next map__30881)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30881))
                           (if (seq map__30881) (first map__30881) {}))
                         map__30881)
            log_tree_queue (get map__30881 :log-tree-queue)
            index_sem (get map__30881 :index-sem)
            db_id (get map__30881 :db-id)
            cluster (get map__30881 :cluster)
            priority_updates_queue (get map__30881 :priority-updates-queue)
            olookup (get map__30881 :olookup)
            db_ref (get map__30881 :db-ref)
            indexer (get map__30881 :indexer)
            processed_updates_queue (get map__30881 :processed-updates-queue)
            requested_index_t_ref (get map__30881 :requested-index-t-ref)
            shutdown_ref (get map__30881 :shutdown-ref)]
        (let [temp__5825__auto__ (cond
                                   (number? up_to_t) up_to_t
                                   (#{:basisT} up_to_t) (do (:basisT (deref db_ref))))]
          (when temp__5825__auto__
            (let [t temp__5825__auto__]
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process #:index{:requested-up-to-t t})))
                nil)
              (swap! requested_index_t_ref max t))))
        (when (.tryAcquire ^java.util.concurrent.Semaphore index_sem)
          (if (not (db/has-memory-index? (deref db_ref)))
            (do (.release ^java.util.concurrent.Semaphore index_sem) nil)
            (do
              (when-not (db/is-indexing? (deref db_ref)) (indexer/indexing-started indexer db_id))
              (let [db (swap! db_ref db/prepare-for-indexing)
                    log_segmented (promise)
                    nextT (.getNextT ^datomic.db.IDb db)]
                (future-call
                  (fn fn__30882
                    ([]
                      (try
                        (try
                          (loop [n 0]
                            (do
                              (when (< 2 n)
                                (process/fail process/instance "Indexing retry limit exceeded."))
                              (when-not (try
                                          (do
                                            (queue/put
                                              processed_updates_queue
                                              {:type :ensure-tree, :completed log_segmented})
                                            (when-not (deref log_segmented 60000 false)
                                              (throw
                                                (java.lang.Error.
                                                  "Timed out waiting to segment log.")))
                                            (let [m_30884 {:event :update/create-index,
                                                           :next-t nextT,
                                                           :id (:id db)}
                                                  ___8598__auto__ (let
                                                                    [logger
                                                                     (org.slf4j.LoggerFactory/getLogger
                                                                       "datomic.update")]
                                                                    (when
                                                                      (.isInfoEnabled
                                                                        ^org.slf4j.Logger logger)
                                                                      (.info
                                                                        ^org.slf4j.Logger logger
                                                                        (logger/process
                                                                          (assoc
                                                                            m_30884
                                                                            :phase
                                                                            :begin))))
                                                                    nil)
                                                  start__8599__auto__ (java.lang.System/nanoTime)
                                                  result__8600__auto__ (try
                                                                         {:returned
                                                                          (let
                                                                            [map__30888
                                                                             (index/merge-db
                                                                               cluster
                                                                               olookup
                                                                               db
                                                                               (.getNextT
                                                                                 ^datomic.db.IDb db))
                                                                             map__30888
                                                                             (if
                                                                               (seq? map__30888)
                                                                               (if
                                                                                 (next map__30888)
                                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                                   (to-array
                                                                                     map__30888))
                                                                                 (if
                                                                                   (seq map__30888)
                                                                                   (first
                                                                                     map__30888)
                                                                                   {}))
                                                                               map__30888)
                                                                             new_index
                                                                             (get
                                                                               map__30888
                                                                               :new-index)
                                                                             xpreds
                                                                             (get
                                                                               map__30888
                                                                               :xpreds)]
                                                                            (when
                                                                              xpreds
                                                                              (let
                                                                                [log
                                                                                 (log/find-log
                                                                                   cluster
                                                                                   olookup)
                                                                                 excise_ret
                                                                                 (log/excise
                                                                                   cluster
                                                                                   olookup
                                                                                   log
                                                                                   xpreds)
                                                                                 log_excised
                                                                                 (promise)]
                                                                                (queue/put
                                                                                  log_tree_queue
                                                                                  (merge
                                                                                    excise_ret
                                                                                    {:type
                                                                                     :excise-root,
                                                                                     :completed
                                                                                     log_excised}))
                                                                                (when-not
                                                                                  (deref
                                                                                    log_excised
                                                                                    60000
                                                                                    false)
                                                                                  (throw
                                                                                    (java.lang.Error.
                                                                                      "Timed out waiting on log excision.")))))
                                                                            (let
                                                                              [logger
                                                                               (org.slf4j.LoggerFactory/getLogger
                                                                                 "datomic.update")]
                                                                              (when
                                                                                (.isInfoEnabled
                                                                                  ^org.slf4j.Logger logger)
                                                                                (.info
                                                                                  ^org.slf4j.Logger logger
                                                                                  (logger/process
                                                                                    (merge
                                                                                      {:event
                                                                                       :update/new-index,
                                                                                       :db-id
                                                                                       db_id}
                                                                                      (select-keys
                                                                                        new_index
                                                                                        [:nextT
                                                                                         :rev])))))
                                                                              nil)
                                                                            (queue/put
                                                                              priority_updates_queue
                                                                              {:new-index
                                                                               new_index})
                                                                            true)}
                                                                         (catch
                                                                           java.lang.Throwable
                                                                           t__8601__auto__
                                                                           {:threw
                                                                            t__8601__auto__}))
                                                  elapsed_30885 (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8599__auto__)
                                                  msec_30886 (logger/format-as-msec
                                                               (long elapsed_30885))]
                                              (monitor/add-stat :CreateEntireIndexMsec msec_30886)
                                              (let [endmsg__8602__auto__ (merge
                                                                           (assoc
                                                                             m_30884
                                                                             :msec
                                                                             msec_30886
                                                                             :phase
                                                                             :end)
                                                                           (when
                                                                             (:threw
                                                                               result__8600__auto__)
                                                                             {:threw
                                                                              (class
                                                                                (:threw
                                                                                  result__8600__auto__))}))
                                                    logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.update")]
                                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                  (.info
                                                    ^org.slf4j.Logger logger
                                                    (logger/process endmsg__8602__auto__)))
                                                nil)
                                              (if (contains? result__8600__auto__ :returned)
                                                (:returned result__8600__auto__)
                                                (do (throw (:threw result__8600__auto__)) nil))))
                                          (catch
                                            java.lang.Throwable
                                            t
                                            (let [shutdown? (realized? shutdown_ref)]
                                              (when-not shutdown?
                                                (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.update")
                                                      ex t]
                                                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                                    (.warn
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        {:message "Index creation failed",
                                                         :db-id db_id})
                                                      ^java.lang.Throwable ex)
                                                    (logger/caused-by logger ex))
                                                  nil)
                                                (monitor/alarm :IndexingFailed))
                                              shutdown?)))
                                (recur (inc n)))))
                          (catch
                            java.lang.Throwable
                            t
                            (when-not (realized? shutdown_ref)
                              (process/fail process/instance "Index thread failed" t))))
                        (catch
                          java.lang.Throwable
                          t__8765__auto__
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                  ex t__8765__auto__]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "error executing future")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            (monitor/alarm :UnhandledException)
                            (throw ^java.lang.Throwable t__8765__auto__)
                            nil)))))))))))))
  (reset-meta!
    #'process-request-index
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['up-to-t]}
          {:keys
           [(.withMeta 'index-sem {:tag 'Semaphore})
            'db-ref
            'processed-updates-queue
            'cluster
            'olookup
            'requested-index-t-ref
            'priority-updates-queue
            'db-id
            'indexer
            'log-tree-queue
            'shutdown-ref]}]),
       :column (int 1)}
      :name
      'process-request-index
      :ns
      *ns*))
  ;; Apply f against a stable atom value, install xf's projection of the result,
  ;; and retry the calculation when another thread wins the compare-and-swap.
  (defn swap-xf!
    ([atom xf f & args]
      (loop []
        (let [v1 (deref atom) ret (apply f v1 args) v2 (^clojure.lang.IFn xf ret)]
          (if (compare-and-set! atom v1 v2) ret (recur))))))
  (reset-meta!
    #'swap-xf!
    (assoc
      {:arglists (clojure.core/list ['atom 'xf 'f '& 'args]), :column (int 1)}
      :name
      'swap-xf!
      :ns
      *ns*))
  ;; Evaluate a transaction while converting a thrown failure into a result map
  ;; whose :db-after remains the original database value.
  (defn with-tx*
    ([db dispatcher tx_data]
      (try
        (db/with-tx db dispatcher tx_data)
        (catch java.lang.Throwable t {:db-after db, :ex t}))))
  (reset-meta!
    #'with-tx*
    (assoc
      {:arglists (clojure.core/list ['db 'dispatcher 'tx-data]), :column (int 1)}
      :name
      'with-tx*
      :ns
      *ns*))
  ;; Evaluate one transaction against the current database, atomically advance
  ;; :db-after, capture I/O statistics, and route the result toward logging and reply.
  ;; ATOMIC-NOTE [observed] swap-xf! installs with-tx*'s :db-after into db-ref before
  ;; encoding or log I/O. Success queues one result to both encoders with a shared
  ;; :logged promise; failure leaves db-ref unchanged and queues only an error reply.
  ;; [inferred] This separates serialized assessment from publication latency;
  ;; the internal db-ref advance alone must not be treated as durable completion.
  (defn process-transaction
    ([procargs p__30900]
      (let [map__30901 p__30900
            map__30901 (if (seq? map__30901)
                         (if (next map__30901)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30901))
                           (if (seq map__30901) (first map__30901) {}))
                         map__30901)
            process_args map__30901
            db_ref (get map__30901 :db-ref)
            processed_updates_queue (get map__30901 :processed-updates-queue)
            fressian_for_notify_queue (get map__30901 :fressian-for-notify-queue)
            priority_updates_queue (get map__30901 :priority-updates-queue)
            id (common/getx procargs :id)
            started_at (java.lang.System/nanoTime)
            requested_context (get-in procargs [:options :io-context])
            prefetch_io_stats_bindings (common/getx procargs :prefetch-io-stats-bindings)
            tx_promise (common/getx procargs :tx-promise)
            vec__30902 (->dispatcher procargs process_args)
            dispatcher (nth vec__30902 (int 0) nil)
            prefetch_io_context (nth vec__30902 (int 1) nil)
            io_context (if (qualified-keyword? requested_context) requested_context :db/tx)
            data (common/getx procargs :data)
            map__30905 (io-stats/with-io-stats
                         (fn fn__30906 ([] (swap-xf! db_ref :db-after with-tx* dispatcher data)))
                         {:api :tx-with, :io-context io_context})
            map__30905 (if (seq? map__30905)
                         (if (next map__30905)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30905))
                           (if (seq map__30905) (first map__30905) {}))
                         map__30905)
            ret (get map__30905 :ret)
            io_stats (get map__30905 :io-stats)
            ex (get map__30905 :ex)]
        (deliver tx_promise true)
        (when ex (throw ^java.lang.Throwable ex))
        (if (:tx-data ret)
          (let [applied_at (java.lang.System/nanoTime)
                map__30908 ret
                map__30908 (if (seq? map__30908)
                             (if (next map__30908)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__30908))
                               (if (seq map__30908) (first map__30908) {}))
                             map__30908)
                tx_data (get map__30908 :tx-data)
                tempids (get map__30908 :tempids)
                db_after (get map__30908 :db-after)
                tx_stats (get map__30908 :tx-stats)
                t (:basisT db_after)
                datom_count (count tx_data)
                pf_stats (io-stats/io-stats-from-bindings
                           {:io-context prefetch_io_context}
                           prefetch_io_stats_bindings)
                msg {:id id,
                     :data tx_data,
                     :t t,
                     :io-stats io_stats,
                     :tempids tempids,
                     :logged (promise)}]
            (monitor/add-stat :TransactionDatoms (java.lang.Integer/valueOf (int datom_count)))
            (tx/add-to-log-event!
              id
              {:t t,
               :io-stats io_stats,
               :started-at (long started_at),
               :applied-at (long applied_at),
               :datom-count (java.lang.Integer/valueOf (int datom_count)),
               :tx-stats tx_stats,
               :pf-stats pf_stats})
            (when (db/data-needs-index? db_after tx_data)
              (queue/put
                priority_updates_queue
                {:request-index true, :up-to-t (:basisT db_after)}))
            (queue/put processed_updates_queue msg)
            (queue/put fressian_for_notify_queue msg))
          (let [ex (:ex ret)
                error_data (ex-data ex)
                classname (.getName (class ex))
                error (or (.getMessage ^java.lang.Throwable ex) classname)
                msg {:id id,
                     :classname classname,
                     :error error,
                     :error-data error_data,
                     :logged (let [G__30909 (promise)] (deliver G__30909 true) G__30909)}]
            (when (error/should-log-exception? error_data)
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update") ex ex]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process "Update failed")
                    ^java.lang.Throwable ex)
                  (logger/caused-by logger ex))
                nil))
            (tx/log-completion! [id])
            (queue/put fressian_for_notify_queue msg))))))
  (reset-meta!
    #'process-transaction
    (assoc
      {:arglists
       (clojure.core/list
         ['procargs
          {:keys
           ['db-ref 'processed-updates-queue 'fressian-for-notify-queue 'priority-updates-queue],
           :as 'process-args}]),
       :column (int 1)}
      :name
      'process-transaction
      :ns
      *ns*))
  ;; Serialize database state transitions, giving maintenance messages priority
  ;; over ordinary submissions and keeping result publication off this worker.
  ;; ATOMIC-NOTE [observed] Each database's processor is the common consumer for
  ;; transaction, request-index and completed-index messages. At the process-wide
  ;; memory limit it services only the priority queue, allowing index adoption to
  ;; release memory. [unknown] This ordering alone does not establish fairness.
  (defn processor
    ([shutdown_hook & p__30912]
      (let [map__30913 p__30912
            map__30913 (if (seq? map__30913)
                         (if (next map__30913)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30913))
                           (if (seq map__30913) (first map__30913) {}))
                         map__30913)
            processor_args map__30913
            index_sem (get map__30913 :index-sem)
            db_id (get map__30913 :db-id)
            unprocessed_updates_queue (get map__30913 :unprocessed-updates-queue)
            priority_updates_queue (get map__30913 :priority-updates-queue)
            session_fn (get map__30913 :session-fn)
            fressian_for_notify_queue (get map__30913 :fressian-for-notify-queue)
            db_ref (get map__30913 :db-ref)
            indexer (get map__30913 :indexer)
            processed_updates_queue (get map__30913 :processed-updates-queue)
            requested_index_t_ref (get map__30913 :requested-index-t-ref)
            session (^clojure.lang.IFn session_fn)
            producer (aclient/create-producer session (tx/push-address db_id))
            result_push (aclient/fressian-producer session producer (tx/write-handlers false))
            processor_args (assoc processor_args :result-push result_push)]
        (^clojure.lang.IFn shutdown_hook
          (fn fn__30914
            ([] (deref (common/async-shutdown producer)) (deref (common/async-shutdown session)))))
        (let [m_30916 {:task :processor, :event :update/loop}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_30916 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [done (java.lang.Object.)]
                                        (loop []
                                          (if (process/failing? process/instance)
                                            :process-failed
                                            (let [result (try
                                                           (let
                                                             [procargs
                                                              (loop
                                                                []
                                                                (if
                                                                  (indexer/memidx-limit-exceeded?
                                                                    indexer)
                                                                  (do
                                                                    (monitor/alarm :BackPressure)
                                                                    (let
                                                                      [nxt
                                                                       (queue/poll
                                                                         priority_updates_queue
                                                                         nil
                                                                         60000)]
                                                                      (if nxt nxt (recur))))
                                                                  (let
                                                                    [nxt
                                                                     (or
                                                                       (queue/poll
                                                                         priority_updates_queue)
                                                                       (queue/poll
                                                                         unprocessed_updates_queue
                                                                         nil
                                                                         1000))]
                                                                    (if nxt nxt (recur)))))]
                                                             (cond
                                                               (:new-index procargs)
                                                               (process-new-index
                                                                 procargs
                                                                 processor_args)
                                                               (:request-index procargs)
                                                               (process-request-index
                                                                 procargs
                                                                 processor_args)
                                                               :default
                                                               (do
                                                                 (process-transaction
                                                                   procargs
                                                                   processor_args))))
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             java.io.InterruptedIOException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                             _
                                                             done)
                                                           (catch
                                                             java.lang.InterruptedException
                                                             _
                                                             done))]
                                              (if (= done result) :interrupted (recur))))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_30917 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_30918 (logger/format-as-msec (long elapsed_30917))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_30916 :msec msec_30918 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'processor
    (assoc
      {:arglists
       (clojure.core/list
         ['shutdown-hook
          '&
          {:keys
           ['unprocessed-updates-queue
            'fressian-for-notify-queue
            'priority-updates-queue
            'processed-updates-queue
            'index-sem
            'indexer
            'requested-index-t-ref
            'db-ref
            'db-id
            'session-fn],
           :as 'processor-args}]),
       :column (int 1)}
      :name
      'processor
      :ns
      *ns*))
  ;; Encode successful transaction results for the durable log and pass encoded
  ;; work to the writer without making the processor perform serialization.
  ;; ATOMIC-NOTE [observed] log/fressianed-tx produces the stored form and the writer
  ;; receives the original :logged promise; maintenance messages pass through this
  ;; same queue. [inferred] A separate encoder can overlap computation and I/O;
  ;; the worker split is evidence of that opportunity, not a throughput measurement.
  (defn fressianer
    ([shutdown_hook & p__30933]
      (let [map__30934 p__30933
            map__30934 (if (seq? map__30934)
                         (if (next map__30934)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30934))
                           (if (seq map__30934) (first map__30934) {}))
                         map__30934)
            processed_updates_queue (get map__30934 :processed-updates-queue)
            writer_queue (get map__30934 :writer-queue)
            indexer (get map__30934 :indexer)
            db_id (get map__30934 :db-id)]
        (^clojure.lang.IFn shutdown_hook nil)
        (let [m_30935 {:task :fressianer, :event :update/loop}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_30935 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [done (java.lang.Object.)]
                                        (loop []
                                          (if (process/failing? process/instance)
                                            :process-failed
                                            (let [result (try
                                                           (let
                                                             [tx
                                                              (queue/take processed_updates_queue)
                                                              temp__5823__auto__ (get tx :id)]
                                                             (if
                                                               temp__5823__auto__
                                                               (let
                                                                 [id temp__5823__auto__]
                                                                 (indexer/notify-txdata
                                                                   indexer
                                                                   db_id
                                                                   (common/getx tx :data))
                                                                 (queue/put
                                                                   writer_queue
                                                                   {:id id,
                                                                    :fressianed-tx
                                                                    (log/fressianed-tx tx),
                                                                    :t (common/getx tx :t),
                                                                    :tx tx,
                                                                    :type :fressianed-tx,
                                                                    :logged (:logged tx)}))
                                                               (queue/put writer_queue tx)))
                                                           (catch
                                                             java.io.InterruptedIOException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             java.lang.InterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                             _
                                                             done))]
                                              (if (= done result) :interrupted (recur))))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_30936 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_30937 (logger/format-as-msec (long elapsed_30936))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_30935 :msec msec_30937 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'fressianer
    (assoc
      {:arglists
       (clojure.core/list
         ['shutdown-hook '& {:keys ['processed-updates-queue 'writer-queue 'indexer 'db-id]}]),
       :column (int 1)}
      :name
      'fressianer
      :ns
      *ns*))
  (defn fressian-tx-notification
    ([processor_result]
      (try
        (let [baos (org.fressian.impl.BytesOutputStream.)
              fressian_out (tx/writer baos (> (count (:data processor_result)) 1))]
          (.writeObject ^org.fressian.Writer fressian_out (dissoc processor_result :logged))
          baos)
        (catch
          java.lang.Exception
          e
          (let [baos (org.fressian.impl.BytesOutputStream.)
                fressian_out (tx/writer baos false)
                result {:id (get processor_result :id),
                        :classname "clojure.lang.ExceptionInfo",
                        :error "Exception fressianing transaction response",
                        :error-data {:cause (.getMessage ^java.lang.Throwable e)}}]
            (.writeObject ^org.fressian.Writer fressian_out result)
            baos)))))
  (reset-meta!
    #'fressian-tx-notification
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta ['processor-result] {:tag 'org.fressian.impl.BytesOutputStream})),
       :column (int 1)}
      :name
      'fressian-tx-notification
      :ns
      *ns*))
  ;; Encode the peer notification form and enqueue it for publication after the
  ;; writer has established the transaction's durable block boundary.
  ;; ATOMIC-NOTE [observed] This encoder can run before commit and carries :logged
  ;; alongside the response bytes. block-notifier performs the wait; serializing a
  ;; response here therefore neither commits the transaction nor makes it visible.
  (defn notify-fressianer
    ([shutdown_hook & p__30951]
      (let [map__30952 p__30951
            map__30952 (if (seq? map__30952)
                         (if (next map__30952)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30952))
                           (if (seq map__30952) (first map__30952) {}))
                         map__30952)
            fressian_for_notify_queue (get map__30952 :fressian-for-notify-queue)
            block_notify_queue (get map__30952 :block-notify-queue)]
        (^clojure.lang.IFn shutdown_hook nil)
        (let [m_30953 {:task :notifier, :event :update/loop}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_30953 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [done (java.lang.Object.)]
                                        (loop []
                                          (if (process/failing? process/instance)
                                            :process-failed
                                            (let [result (try
                                                           (let
                                                             [tx
                                                              (queue/take
                                                                fressian_for_notify_queue)
                                                              id (common/getx tx :id)]
                                                             (queue/put
                                                               block_notify_queue
                                                               {:id id,
                                                                :fressianed-tx
                                                                (dio/bytestream->buf
                                                                  (fressian-tx-notification tx)),
                                                                :logged (:logged tx)}))
                                                           (catch
                                                             java.lang.InterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             java.io.InterruptedIOException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                             _
                                                             done))]
                                              (if (= done result) :interrupted (recur))))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_30954 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_30955 (logger/format-as-msec (long elapsed_30954))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_30953 :msec msec_30955 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'notify-fressianer
    (assoc
      {:arglists
       (clojure.core/list
         ['shutdown-hook '& {:keys ['fressian-for-notify-queue 'block-notify-queue]}]),
       :column (int 1)}
      :name
      'notify-fressianer
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.update" "writer-process") {:column (int 1)})
  (let [v__5813__auto__ #'writer-process]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.update" "writer-process") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.update" "writer-process")
        (clojure.lang.MultiFn.
          "writer-process"
          (fn fn__30968 ([item ctxt] (:type item)))
          :default
          #'clojure.core/global-hierarchy))
      #'writer-process))
  (defmethod
    writer-process
    :ensure-tree
    fn__30975
    ([p__30973 p__30974]
      (let [map__30976 p__30973
            map__30976 (if (seq? map__30976)
                         (if (next map__30976)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30976))
                           (if (seq map__30976) (first map__30976) {}))
                         map__30976)
            completed (get map__30976 :completed)
            map__30977 p__30974
            map__30977 (if (seq? map__30977)
                         (if (next map__30977)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30977))
                           (if (seq map__30977) (first map__30977) {}))
                         map__30977)
            log_ref (get map__30977 :log-ref)
            cs (get map__30977 :cs)
            log_tree_queue (get map__30977 :log-tree-queue)
            map__30978 (deref log_ref)
            map__30978 (if (seq? map__30978)
                         (if (next map__30978)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30978))
                           (if (seq map__30978) (first map__30978) {}))
                         map__30978)
            tail (get map__30978 :tail)]
        (if (log/tail-empty? tail)
          (deliver completed true)
          (queue/put log_tree_queue {:type :extend-tree, :completed completed, :tail tail})))))
  (defmethod
    writer-process
    :adopt-tree
    fn__30982
    ([p__30980 p__30981]
      (let [map__30983 p__30980
            map__30983 (if (seq? map__30983)
                         (if (next map__30983)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30983))
                           (if (seq map__30983) (first map__30983) {}))
                         map__30983)
            completed (get map__30983 :completed)
            root_id (get map__30983 :root-id)
            garbage_ids (get map__30983 :garbage-ids)
            t (get map__30983 :t)
            map__30984 p__30981
            map__30984 (if (seq? map__30984)
                         (if (next map__30984)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30984))
                           (if (seq map__30984) (first map__30984) {}))
                         map__30984)
            log_ref (get map__30984 :log-ref)
            cs (get map__30984 :cs)
            olookup (get map__30984 :olookup)
            log_tree_queue (get map__30984 :log-tree-queue)]
        (let [map__30985 (swap! log_ref log/adopt-root cs root_id t)
              map__30985 (if (seq? map__30985)
                           (if (next map__30985)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__30985))
                             (if (seq map__30985) (first map__30985) {}))
                           map__30985)
              tail (get map__30985 :tail)]
          (when-not (log/tail-empty? tail)
            (queue/put log_tree_queue {:type :new-tail, :tail tail})))
        (events/publish {:key :datomic.garbage/mark, :cluster cs, :garbage garbage_ids})
        (some-> completed (deliver true)))))
  ;; Batch encoded transactions into log appends. Each transaction's :logged
  ;; promise is delivered only after the batch append succeeds.
  ;; ATOMIC-NOTE [documented] ACID / Durability requires storage acknowledgement
  ;; before completion; How It Works permits batching. [observed] log_block calls
  ;; log/append before delivering any :logged promise, and fails the process on an
  ;; append error. The batch shares a storage write, retaining each transaction's t.
  (defn writer
    ([& p__30988]
      (let [map__30989 p__30988
            map__30989 (if (seq? map__30989)
                         (if (next map__30989)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30989))
                           (if (seq map__30989) (first map__30989) {}))
                         map__30989)
            context map__30989
            writer_queue (get map__30989 :writer-queue)
            log_tree_queue (get map__30989 :log-tree-queue)
            block_threshold (get map__30989 :block-threshold)
            log (get map__30989 :log)
            cs (get map__30989 :cs)
            olookup (get map__30989 :olookup)
            db_id (get map__30989 :db-id)
            shutdown_ref (promise)
            log_ref (atom log)
            context (assoc context :log-ref log_ref)
            log_block (fn log_block
                        ([txes]
                          (try
                            (do
                              (let [log (swap! log_ref log/append cs txes) tail (:tail log)]
                                (queue/put log_tree_queue {:type :new-tail, :tail tail}))
                              (monitor/add-stat
                                :TransactionBatch
                                (java.lang.Integer/valueOf (int (count txes))))
                              (loop [seq_30991 (seq txes) chunk_30992 nil count_30993 0 i_30994 0]
                                (if (< i_30994 count_30993)
                                  (let [tx (.nth ^clojure.lang.Indexed chunk_30992 (int i_30994))]
                                    (deliver (common/getx tx :logged) true)
                                    (monitor/add-stat
                                      :TransactionBytes
                                      (long (dio/remaining (:fressianed-tx tx))))
                                    (recur seq_30991 chunk_30992 count_30993 (inc i_30994)))
                                  (let [temp__5825__auto__ (seq seq_30991)]
                                    (when temp__5825__auto__
                                      (let [seq_30991 temp__5825__auto__]
                                        (if (chunked-seq? seq_30991)
                                          (let [c__6090__auto__ (chunk-first seq_30991)]
                                            (recur
                                              (chunk-rest seq_30991)
                                              c__6090__auto__
                                              (int (count c__6090__auto__))
                                              (int 0)))
                                          (let [tx (first seq_30991)]
                                            (deliver (common/getx tx :logged) true)
                                            (monitor/add-stat
                                              :TransactionBytes
                                              (long (dio/remaining (:fressianed-tx tx))))
                                            (recur (next seq_30991) nil 0 0)))))))))
                            (catch
                              java.lang.Throwable
                              t
                              (do
                                (monitor/alarm :UnableToWriteLog)
                                (process/fail process/instance "Could not write log" t)
                                (throw ^java.lang.Throwable t)
                                nil)))))
            proc (fn proc
                   ([]
                     (while
                       (not (realized? shutdown_ref))
                       (loop [txes [] next_item (queue/take writer_queue)]
                         (when-not (= :done next_item)
                           (if (= :fressianed-tx (:type next_item))
                             (let [txes (conj txes next_item)]
                               (if (<
                                     block_threshold
                                     (apply + (map (comp dio/remaining :fressianed-tx) txes)))
                                 (do (^clojure.lang.IFn log_block txes) nil)
                                 (let [temp__5823__auto__ (queue/poll writer_queue)]
                                   (if temp__5823__auto__
                                     (let [next_item temp__5823__auto__] (recur txes next_item))
                                     (do (^clojure.lang.IFn log_block txes) nil)))))
                             (do
                               (when (seq txes) (^clojure.lang.IFn log_block txes))
                               (writer-process next_item context)
                               nil)))))
                     nil))
            launch (delay
                     (let [G__31002 (java.lang.Thread.
                                      (fn fn__31003
                                        ([]
                                          (try
                                            (^clojure.lang.IFn proc)
                                            (catch
                                              java.lang.Throwable
                                              t
                                              (process/fail process/instance "Writer failed" t)))))
                                      (str "writer-" db_id))]
                       (.start ^java.lang.Thread G__31002)
                       G__31002))]
        (reify
          clojure.lang.IDeref
          clojure.lang.IFn
          datomic.common.AsyncShutdown
          (async-shutdown
            [this]
            (do
              (deliver shutdown_ref true)
              (when-not (queue/offer writer_queue :done 10000)
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                    (.warn
                      ^org.slf4j.Logger logger
                      (logger/process "Timed out shutting down writer thread.")))
                  nil))
              shutdown_ref))
          (deref [this] context)
          (invoke [this] (do (deref launch) this))))))
  (reset-meta!
    #'writer
    (assoc
      {:arglists
       (clojure.core/list
         ['&
          {:keys ['writer-queue 'log-tree-queue 'block-threshold 'log 'cs 'olookup 'db-id],
           :as 'context}]),
       :column (int 1)}
      :name
      'writer
      :ns
      *ns*))
  ;; Broadcast committed transaction blocks to connected peers in log order.
  ;; ATOMIC-NOTE [observed] The FIFO notification path waits up to 60 seconds on
  ;; :logged before sending to tx/push-address, invoking process/fail on timeout.
  ;; [documented] ACID / Durability explains this boundary: a computed or encoded
  ;; result cannot be reported as complete merely because it entered the pipeline.
  (defn block-notifier
    ([shutdown_hook & p__31009]
      (let [map__31010 p__31009
            map__31010 (if (seq? map__31010)
                         (if (next map__31010)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31010))
                           (if (seq map__31010) (first map__31010) {}))
                         map__31010)
            db_id (get map__31010 :db-id)
            session_fn (get map__31010 :session-fn)
            block_notify_queue (get map__31010 :block-notify-queue)
            t_now (get map__31010 :t-now)
            db_ref (get map__31010 :db-ref)
            session (^clojure.lang.IFn session_fn)
            producer (aclient/create-producer session (tx/push-address db_id))]
        (^clojure.lang.IFn shutdown_hook
          (fn fn__31011
            ([] (deref (common/async-shutdown producer)) (deref (common/async-shutdown session)))))
        (let [m_31013 {:task :block-notifier, :event :update/loop}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_31013 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [done (java.lang.Object.)]
                                        (loop []
                                          (if (process/failing? process/instance)
                                            :process-failed
                                            (let [result (try
                                                           (let
                                                             [block (queue/take block_notify_queue)
                                                              msg
                                                              (aclient/create-message
                                                                session
                                                                false)
                                                              fressianed_tx (:fressianed-tx block)]
                                                             (.writeBytes
                                                               (.getBodyBuffer
                                                                 ^org.apache.activemq.artemis.api.core.client.ClientMessage msg)
                                                               ^java.nio.ByteBuffer fressianed_tx)
                                                             (when-not
                                                               (deref (:logged block) 60000 false)
                                                               (monitor/alarm :LogWriteTimedOut)
                                                               (process/fail
                                                                 process/instance
                                                                 "Timed out waiting for log write"))
                                                             (.send
                                                               ^org.apache.activemq.artemis.api.core.client.ClientProducer producer
                                                               ^org.apache.activemq.artemis.api.core.Message msg)
                                                             (reset! t_now (:t block)))
                                                           (catch
                                                             java.lang.InterruptedException
                                                             _
                                                             done)
                                                           (catch
                                                             java.io.InterruptedIOException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                             _
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                             _
                                                             done))]
                                              (if (= done result) :interrupted (recur))))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_31014 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_31015 (logger/format-as-msec (long elapsed_31014))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_31013 :msec msec_31015 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'block-notifier
    (assoc
      {:arglists
       (clojure.core/list
         ['shutdown-hook '& {:keys ['db-id 'session-fn 'block-notify-queue 't-now 'db-ref]}]),
       :column (int 1)}
      :name
      'block-notifier
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.update" "handle-log-tree-request") {:column (int 1)})
  (let [v__5813__auto__ #'handle-log-tree-request]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.update" "handle-log-tree-request") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.update" "handle-log-tree-request")
        (clojure.lang.MultiFn.
          "handle-log-tree-request"
          (fn fn__31028 ([msg _] (:type msg)))
          :default
          #'clojure.core/global-hierarchy))
      #'handle-log-tree-request))
  (defn extend-tree
    ([p__31033 p__31034]
      (let [map__31035 p__31033
            map__31035 (if (seq? map__31035)
                         (if (next map__31035)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31035))
                           (if (seq map__31035) (first map__31035) {}))
                         map__31035)
            tail (get map__31035 :tail)
            completed (get map__31035 :completed)
            map__31036 p__31034
            map__31036 (if (seq? map__31036)
                         (if (next map__31036)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31036))
                           (if (seq map__31036) (first map__31036) {}))
                         map__31036)
            cluster (get map__31036 :cluster)
            olookup (get map__31036 :olookup)
            writer_queue (get map__31036 :writer-queue)
            segment_threshold (get map__31036 :segment-threshold)
            dir_threshold (get map__31036 :dir-threshold)
            root_id_ref (get map__31036 :root-id-ref)
            t_ref (get map__31036 :t-ref)]
        (when-not (not (log/tail-empty? tail))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str (clojure.core/list 'not (clojure.core/list 'log/tail-empty? 'tail)))))))
        (let [ts (log/tail-ts tail) t (last ts) log_tail_byte_count (log/tail-byte-count tail)]
          (monitor/add-stat :LogTailBytes log_tail_byte_count)
          (let [m_31037 {:event :update/treeify-log,
                         :first-t (first ts),
                         :last-t t,
                         :tail-bytes log_tail_byte_count}
                ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_31037 :phase :begin))))
                                  nil)
                start__8599__auto__ (java.lang.System/nanoTime)
                result__8600__auto__ (try
                                       {:returned
                                        (let [map__31041 (log/extend-tree
                                                           cluster
                                                           olookup
                                                           (deref root_id_ref)
                                                           dir_threshold
                                                           (log/create-leaves
                                                             segment_threshold
                                                             tail))
                                              map__31041 (if (seq? map__31041)
                                                           (if
                                                             (next map__31041)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__31041))
                                                             (if
                                                               (seq map__31041)
                                                               (first map__31041)
                                                               {}))
                                                           map__31041)
                                              result map__31041
                                              root_id (get map__31041 :root-id)
                                              garbage_ids (get map__31041 :garbage-ids)]
                                          (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.update")]
                                            (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                              (.info
                                                ^org.slf4j.Logger logger
                                                (logger/process
                                                  {:event :update/extend-tree,
                                                   :root-id root_id,
                                                   :t t})))
                                            nil)
                                          (reset! root_id_ref root_id)
                                          (reset! t_ref t)
                                          (queue/put
                                            writer_queue
                                            {:type :adopt-tree,
                                             :completed completed,
                                             :root-id root_id,
                                             :t t,
                                             :garbage-ids garbage_ids}))}
                                       (catch
                                         java.lang.Throwable
                                         t__8601__auto__
                                         {:threw t__8601__auto__}))
                elapsed_31038 (- (java.lang.System/nanoTime) start__8599__auto__)
                msec_31039 (logger/format-as-msec (long elapsed_31038))]
            (let [endmsg__8602__auto__ (merge
                                         (assoc m_31037 :msec msec_31039 :phase :end)
                                         (when (:threw result__8600__auto__)
                                           {:threw (class (:threw result__8600__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
              nil)
            (if (contains? result__8600__auto__ :returned)
              (:returned result__8600__auto__)
              (do (throw (:threw result__8600__auto__)) nil)))))))
  (reset-meta!
    #'extend-tree
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['tail 'completed]}
          {:keys
           ['cluster
            'olookup
            'writer-queue
            'segment-threshold
            'dir-threshold
            'root-id-ref
            't-ref]}]),
       :column (int 1)}
      :name
      'extend-tree
      :ns
      *ns*))
  (defmethod
    handle-log-tree-request
    :new-tail
    fn__31050
    ([p__31048 p__31049]
      (let [map__31051 p__31048
            map__31051 (if (seq? map__31051)
                         (if (next map__31051)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31051))
                           (if (seq map__31051) (first map__31051) {}))
                         map__31051)
            tail (get map__31051 :tail)
            map__31052 p__31049
            map__31052 (if (seq? map__31052)
                         (if (next map__31052)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31052))
                           (if (seq map__31052) (first map__31052) {}))
                         map__31052)
            context map__31052
            segment_threshold (get map__31052 :segment-threshold)
            t_ref (get map__31052 :t-ref)
            tail (log/since tail (deref t_ref))]
        (when (<= segment_threshold (log/tail-byte-count tail))
          (extend-tree {:tail tail} context)))))
  (defmethod
    handle-log-tree-request
    :extend-tree
    fn__31056
    ([p__31054 p__31055]
      (let [map__31057 p__31054
            map__31057 (if (seq? map__31057)
                         (if (next map__31057)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31057))
                           (if (seq map__31057) (first map__31057) {}))
                         map__31057)
            tail (get map__31057 :tail)
            completed (get map__31057 :completed)
            map__31058 p__31055
            map__31058 (if (seq? map__31058)
                         (if (next map__31058)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31058))
                           (if (seq map__31058) (first map__31058) {}))
                         map__31058)
            context map__31058
            t_ref (get map__31058 :t-ref)
            tail (log/since tail (deref t_ref))]
        (if (log/tail-empty? tail)
          (deliver completed true)
          (extend-tree {:tail tail, :completed completed} context)))))
  (defmethod
    handle-log-tree-request
    :excise-root
    fn__31062
    ([p__31060 p__31061]
      (let [map__31063 p__31060
            map__31063 (if (seq? map__31063)
                         (if (next map__31063)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31063))
                           (if (seq map__31063) (first map__31063) {}))
                         map__31063)
            request map__31063
            completed (get map__31063 :completed)
            map__31064 p__31061
            map__31064 (if (seq? map__31064)
                         (if (next map__31064)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31064))
                           (if (seq map__31064) (first map__31064) {}))
                         map__31064)
            cluster (get map__31064 :cluster)
            olookup (get map__31064 :olookup)
            writer_queue (get map__31064 :writer-queue)
            root_id_ref (get map__31064 :root-id-ref)
            t_ref (get map__31064 :t-ref)
            map__31065 (log/excise-root cluster olookup (deref root_id_ref) request)
            map__31065 (if (seq? map__31065)
                         (if (next map__31065)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31065))
                           (if (seq map__31065) (first map__31065) {}))
                         map__31065)
            ret map__31065
            root_id (get map__31065 :root-id)
            garbage_ids (get map__31065 :garbage-ids)]
        (reset! root_id_ref root_id)
        (queue/put
          writer_queue
          {:type :adopt-tree,
           :completed completed,
           :root-id root_id,
           :t (deref t_ref),
           :garbage-ids garbage_ids}))))
  ;; Fold accumulated log-tail blocks into persistent log-tree roots and hand
  ;; adopted roots back to the writer for publication.
  (defn log-treeifier
    ([_ & p__31067]
      (let [map__31068 p__31067
            map__31068 (if (seq? map__31068)
                         (if (next map__31068)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31068))
                           (if (seq map__31068) (first map__31068) {}))
                         map__31068)
            context map__31068
            olookup (get map__31068 :olookup)
            log (get map__31068 :log)
            log_tree_queue (get map__31068 :log-tree-queue)
            root_id (log/get-root-id log)
            t (:t (log/last-tree-tx olookup root_id))
            context (assoc context :root-id-ref (atom root_id :validator string?) :t-ref (atom t))
            m_31069 {:task :log-treeifier, :event :update/loop}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_31069 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [done (java.lang.Object.)]
                                      (loop []
                                        (if (process/failing? process/instance)
                                          :process-failed
                                          (let [result (try
                                                         (do
                                                           (let
                                                             [last_new_tail_msg (volatile! nil)]
                                                             (loop
                                                               [msg (queue/take log_tree_queue)
                                                                ct
                                                                (.size
                                                                  ^java.util.concurrent.LinkedBlockingQueue log_tree_queue)]
                                                               (do
                                                                 (if
                                                                   (= :new-tail (:type msg))
                                                                   (vreset! last_new_tail_msg msg)
                                                                   (handle-log-tree-request
                                                                     msg
                                                                     context))
                                                                 (if
                                                                   (= ct 0)
                                                                   (let
                                                                     [temp__5825__auto__
                                                                      (deref last_new_tail_msg)]
                                                                     (when
                                                                       temp__5825__auto__
                                                                       (let
                                                                         [msg temp__5825__auto__]
                                                                         (vreset!
                                                                           last_new_tail_msg
                                                                           nil)
                                                                         (handle-log-tree-request
                                                                           msg
                                                                           context))))
                                                                   (recur
                                                                     (queue/take log_tree_queue)
                                                                     (dec ct))))))
                                                           nil)
                                                         (catch
                                                           org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                           _
                                                           done)
                                                         (catch
                                                           org.apache.activemq.artemis.api.core.ActiveMQInterruptedException
                                                           _
                                                           done)
                                                         (catch
                                                           java.lang.InterruptedException
                                                           _
                                                           done)
                                                         (catch
                                                           java.io.InterruptedIOException
                                                           _
                                                           done))]
                                            (if (= done result) :interrupted (recur))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_31070 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_31071 (logger/format-as-msec (long elapsed_31070))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_31069 :msec msec_31071 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'log-treeifier
    (assoc
      {:arglists
       (clojure.core/list
         ['_
          '&
          {:keys ['olookup 'log (.withMeta 'log-tree-queue {:tag 'LinkedBlockingQueue})],
           :as 'context}]),
       :column (int 1)}
      :name
      'log-treeifier
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol DatabaseImpl (explicit-request-index [_] "User requested index."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.update" "DatabaseImpl")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'DatabaseImpl :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'explicit-request-index
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "User requested index."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "DatabaseImpl"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "explicit-request-index")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (.setMeta (clojure.lang.RT/var "datomic.update" "->Database") {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.update" "map->Database")
    {:declared true, :column (int 1)})
  (defrecord
    Database
    [db-ref
     cluster
     olookup
     backgrounds
     ^Semaphore index-sem
     queues
     requested-index-t-ref
     shutdown-ref]
    datomic.update.DatabaseImpl
    datomic.indexer.QueueDatabaseIndex
    datomic.monitor.Metrics
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (do
        (deliver shutdown-ref true)
        (future-call
          (fn fn__31128
            ([]
              (try
                (do
                  (let [refs (mapv common/async-shutdown backgrounds)]
                    (loop [seq_31129 (seq refs) chunk_31130 nil count_31131 0 i_31132 0]
                      (if (< i_31132 count_31131)
                        (let [r (.nth ^clojure.lang.Indexed chunk_31130 (int i_31132))]
                          (deref r)
                          (recur seq_31129 chunk_31130 count_31131 (inc i_31132)))
                        (let [temp__5825__auto__ (seq seq_31129)]
                          (when temp__5825__auto__
                            (let [seq_31129 temp__5825__auto__]
                              (if (chunked-seq? seq_31129)
                                (let [c__6090__auto__ (chunk-first seq_31129)]
                                  (recur
                                    (chunk-rest seq_31129)
                                    c__6090__auto__
                                    (int (count c__6090__auto__))
                                    (int 0)))
                                (let [r (first seq_31129)]
                                  (deref r)
                                  (recur (next seq_31129) nil 0 0)))))))))
                  (cluster/close cluster)
                  true)
                (catch
                  java.lang.Throwable
                  t__8765__auto__
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                          ex t__8765__auto__]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "error executing future")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    (monitor/alarm :UnhandledException)
                    (throw ^java.lang.Throwable t__8765__auto__)
                    nil))))))))
    (memory-threshold-request-index
      [this]
      (when (not (db/is-indexing? (deref db-ref)))
        (queue/put (:priority-updates-queue queues) {:request-index true})))
    (explicit-request-index
      [this]
      (queue/put (:priority-updates-queue queues) {:request-index true, :up-to-t :basisT}))
    (metrics
      [this]
      (into
        {}
        (map
          (fn fn__31123
            ([p__31122]
              (let [vec__31124 p__31122
                    k (nth vec__31124 (int 0) nil)
                    v (nth vec__31124 (int 1) nil)]
                [k (java.lang.Integer/valueOf (int (count v)))])))
          queues))))
  (clojure.core/import 'datomic.update.Database)
  (defn ->Database
    ([db_ref cluster olookup backgrounds index_sem queues requested_index_t_ref shutdown_ref]
      (datomic.update.Database.
        db_ref
        cluster
        olookup
        backgrounds
        index_sem
        queues
        requested_index_t_ref
        shutdown_ref)))
  (reset-meta!
    #'->Database
    (assoc
      {:arglists
       (clojure.core/list
         ['db-ref
          'cluster
          'olookup
          'backgrounds
          'index-sem
          'queues
          'requested-index-t-ref
          'shutdown-ref]),
       :column (int 1)}
      :name
      '->Database
      :ns
      *ns*))
  (defn map->Database
    ([m__8001__auto__]
      (Database/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->Database
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->Database
      :ns
      *ns*))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol
      IMaster
      (remote-ips [_] "Returns set of connected remote ips")
      (get-database [_ db-id] "Returns database if loaded")
      (internal-start-database
        [_ db-id peer-prom]
        "Internal helper called only when holding semaphore. Delivers peer-prom when ready for peers to connect.")
      (internal-stop-database [_ db-id] "Internal helper called only when holding semaphore.")
      (start-database
        [_ db-id]
        "Starts database. Idempotent. Returns map with at least :status or :error key"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.update" "IMaster")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'IMaster :ns *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'remote-ips {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns set of connected remote ips"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "IMaster"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "remote-ips")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*)))
    (let [protocol_signature__7469 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-database
                                        {:arglists (clojure.core/list ['_ 'db-id])}),
                                      :arglists (clojure.core/list ['_ 'db-id]),
                                      :doc "Returns database if loaded"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "IMaster"))
          protocol_method_name__7470 (with-meta
                                       (:name protocol_signature__7469)
                                       protocol_signature__7469)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "get-database")
        (assoc protocol_signature__7469 :name protocol_method_name__7470 :ns *ns*)))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'internal-start-database
                                        {:arglists (clojure.core/list ['_ 'db-id 'peer-prom])}),
                                      :arglists (clojure.core/list ['_ 'db-id 'peer-prom]),
                                      :doc
                                      "Internal helper called only when holding semaphore. Delivers peer-prom when ready for peers to connect."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "IMaster"))
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "internal-start-database")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'internal-stop-database
                                        {:arglists (clojure.core/list ['_ 'db-id])}),
                                      :arglists (clojure.core/list ['_ 'db-id]),
                                      :doc "Internal helper called only when holding semaphore."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "IMaster"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "internal-stop-database")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'start-database
                                        {:arglists (clojure.core/list ['_ 'db-id])}),
                                      :arglists (clojure.core/list ['_ 'db-id]),
                                      :doc
                                      "Starts database. Idempotent. Returns map with at least :status or :error key"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "IMaster"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "start-database")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*))))
  (let [protocol_metadata__7477 {:column (int 1)}]
    (defprotocol
      CatalogService
      (create-database
        [_ arg]
        "Arg must contain :db-name key. Idempotent. Returns map with :failed on failure.")
      (delete-database
        [_ arg]
        "Arg must contain :db-name key. Idempotent. Returns map with :failed on failure.")
      (rename-database
        [_ arg]
        "Arg must contain :db-name :new-name keys.  Returns map with :failed on failure."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.update" "CatalogService")
      (assoc (assoc protocol_metadata__7477 :doc nil) :name 'CatalogService :ns *ns*))
    (let [protocol_signature__7478 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'create-database
                                        {:arglists (clojure.core/list ['_ 'arg])}),
                                      :arglists (clojure.core/list ['_ 'arg]),
                                      :doc
                                      "Arg must contain :db-name key. Idempotent. Returns map with :failed on failure."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "CatalogService"))
          protocol_method_name__7479 (with-meta
                                       (:name protocol_signature__7478)
                                       protocol_signature__7478)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "create-database")
        (assoc protocol_signature__7478 :name protocol_method_name__7479 :ns *ns*)))
    (let [protocol_signature__7480 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete-database
                                        {:arglists (clojure.core/list ['_ 'arg])}),
                                      :arglists (clojure.core/list ['_ 'arg]),
                                      :doc
                                      "Arg must contain :db-name key. Idempotent. Returns map with :failed on failure."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "CatalogService"))
          protocol_method_name__7481 (with-meta
                                       (:name protocol_signature__7480)
                                       protocol_signature__7480)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "delete-database")
        (assoc protocol_signature__7480 :name protocol_method_name__7481 :ns *ns*)))
    (let [protocol_signature__7482 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'rename-database
                                        {:arglists (clojure.core/list ['_ 'arg])}),
                                      :arglists (clojure.core/list ['_ 'arg]),
                                      :doc
                                      "Arg must contain :db-name :new-name keys.  Returns map with :failed on failure."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.update" "CatalogService"))
          protocol_method_name__7483 (with-meta
                                       (:name protocol_signature__7482)
                                       protocol_signature__7482)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.update" "rename-database")
        (assoc protocol_signature__7482 :name protocol_method_name__7483 :ns *ns*))))
  ;; Bound catalog administration to one operation at a time, failing after five minutes.
  (defn acquire-transactor-semaphore
    ([sem]
      (when-not (.tryAcquire ^java.util.concurrent.Semaphore sem 300 TimeUnit/SECONDS)
        (throw (java.lang.Error. "Transactor is busy"))
        nil)))
  (reset-meta!
    #'acquire-transactor-semaphore
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'sem {:tag 'Semaphore})]), :column (int 1)}
      :name
      'acquire-transactor-semaphore
      :ns
      *ns*))
  (def with-semaphore
   (fn with_semaphore
     ([&form &env sem & body]
       (seq
         (concat
           (clojure.core/list 'do)
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'datomic.update/acquire-transactor-semaphore)
                 (clojure.core/list sem))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'try)
                 body
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'finally)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list '.release)
                             (clojure.core/list
                               (vary-meta sem assoc :tag 'Semaphore))))))))))))))))
  (reset-meta!
    #'with-semaphore
    (assoc
      {:arglists (clojure.core/list ['sem '& 'body]), :column (int 1)}
      :name
      'with-semaphore
      :ns
      *ns*))
  (.setMacro #'with-semaphore)
  (defn calculate-basis-t
    ([db]
      (long
        (-> (reduce (fn fn__31284 ([_ d] d)) nil (db/datoms db :aevt [:db/txInstant]))
         (:tx)
         (long)
         (db/eid->eidx)
         (long)))))
  (reset-meta!
    #'calculate-basis-t
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'calculate-basis-t
      :ns
      *ns*))
  (defn patch-901
    ([db]
      (if (= (:basisT db) (:nextT db) (:indexBasisT db))
        (let [basis (calculate-basis-t db)]
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process {:event :db/patch-901, :from (:basisT db), :to basis})))
            nil)
          (assoc db :basisT basis :indexBasisT basis))
        db)))
  (reset-meta!
    #'patch-901
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'patch-901 :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.update" "->Master") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.update" "map->Master") {:declared true, :column (int 1)})
  (defrecord
    Master
    [system-cluster-conf
     olookup-factory
     endpoint
     artemis-server
     session-factory
     session-fn
     ^ConcurrentMap databases
     indexer
     ^Semaphore sem]
    datomic.update.CatalogService
    datomic.update.IMaster
    datomic.monitor.Metrics
    datomic.common.AsyncShutdown
    (async-shutdown
      [this]
      (future-call
        (fn fn__31341
          ([]
            (try
              (do
                (deref (common/async-shutdown session-factory))
                (deref (common/async-shutdown artemis-server)))
              (catch
                java.lang.Throwable
                t__8765__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                        ex t__8765__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8765__auto__)
                  nil)))))))
    (metrics
      [this]
      (apply
        merge-with
        +
        {:RemotePeers
         (java.lang.Integer/valueOf (int (count (aserver/remote-ips artemis-server))))}
        (monitor/metrics indexer)
        (map monitor/metrics (vals databases))))
    (start-database
      [this db_id]
      (merge
        (get this :td)
        (let [coord_cluster (coord/create-system-cluster system-cluster-conf)
              _ (acquire-transactor-semaphore sem)
              reason_no_start (try
                                (if (get-database this db_id)
                                  {:status :already-started}
                                  (let [cat (catalog/get-catalog coord_cluster)]
                                    (when-not (catalog/db-id->db-name cat db_id)
                                      {:failed "database does not exist"})))
                                (catch
                                  java.lang.Throwable
                                  t
                                  (do
                                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.update")
                                          ex t]
                                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                        (.warn
                                          ^org.slf4j.Logger logger
                                          (logger/process "Catalog check failed")
                                          ^java.lang.Throwable ex)
                                        (logger/caused-by logger ex))
                                      nil)
                                    (monitor/alarm :UnhandledException)
                                    {:failed "catalog check failed"})))]
          (if reason_no_start
            (do (.release ^java.util.concurrent.Semaphore sem) reason_no_start)
            (let [peer_prom (promise)]
              (future-call
                (fn fn__31339
                  ([]
                    (try
                      (try
                        (try
                          (internal-start-database this db_id peer_prom)
                          (catch
                            java.lang.Throwable
                            t
                            (if (realized? peer_prom)
                              (process/fail process/instance "Start database failed" t)
                              (do
                                (deliver peer_prom {:failed (.getMessage ^java.lang.Throwable t)})
                                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                      ex t]
                                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                    (.warn
                                      ^org.slf4j.Logger logger
                                      (logger/process "Start database failed")
                                      ^java.lang.Throwable ex)
                                    (logger/caused-by logger ex))
                                  nil)))))
                        (finally (.release ^java.util.concurrent.Semaphore sem)))
                      (catch
                        java.lang.Throwable
                        t__8765__auto__
                        (do
                          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                ex t__8765__auto__]
                            (when (.isWarnEnabled ^org.slf4j.Logger logger)
                              (.warn
                                ^org.slf4j.Logger logger
                                (logger/process "error executing future")
                                ^java.lang.Throwable ex)
                              (logger/caused-by logger ex))
                            nil)
                          (monitor/alarm :UnhandledException)
                          (throw ^java.lang.Throwable t__8765__auto__)
                          nil))))))
              (deref peer_prom 30000 {:failed "database initialization timed out"}))))))
    (internal-start-database
      [this db_id peer_prom]
      (do
        (let [m_31329 {:event :level, :info :transactor/start-db, :db-id db_id}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_31329 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
                                     {:returned
                                      (let [t_now (atom nil)
                                            unprocessed_updates_queue (java.util.concurrent.ArrayBlockingQueue.
                                                                        (int 5))
                                            prefetch_queue (java.util.concurrent.ArrayBlockingQueue.
                                                             (int 5))
                                            map__31333 (prefetch-channels)
                                            map__31333 (if (seq? map__31333)
                                                         (if (next map__31333)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__31333))
                                                           (if
                                                             (seq map__31333)
                                                             (first map__31333)
                                                             {}))
                                                         map__31333)
                                            segments_channel (get map__31333 :segments-channel)
                                            probes_channel (get map__31333 :probes-channel)
                                            priority_updates_queue (java.util.concurrent.LinkedBlockingQueue.)
                                            processed_updates_queue (java.util.concurrent.LinkedBlockingQueue.)
                                            writer_queue (java.util.concurrent.LinkedBlockingQueue.)
                                            log_tree_queue (java.util.concurrent.LinkedBlockingQueue.)
                                            block_notify_queue (java.util.concurrent.LinkedBlockingQueue.)
                                            fressian_for_notify_queue (java.util.concurrent.LinkedBlockingQueue.)
                                            index_sem (java.util.concurrent.Semaphore. (int 1))
                                            system_cluster (coord/create-system-cluster
                                                             system-cluster-conf)
                                            catalog (catalog/get-catalog system_cluster)
                                            db_name (catalog/db-id->db-name catalog db_id)
                                            cluster_conf (merge
                                                           system-cluster-conf
                                                           (catalog/parse-db-conf
                                                             (get catalog db_name)))
                                            cluster (coord/create-db-cluster
                                                      (assoc cluster_conf :shared-pool? false))
                                            olookup (^clojure.lang.IFn olookup-factory cluster)
                                            map__31334 (log/ensure-index-and-log
                                                         cluster
                                                         olookup
                                                         db_id)
                                            map__31334 (if (seq? map__31334)
                                                         (if (next map__31334)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__31334))
                                                           (if
                                                             (seq map__31334)
                                                             (first map__31334)
                                                             {}))
                                                         map__31334)
                                            idxroot (get map__31334 :idxroot)
                                            log (get map__31334 :log)
                                            log (log/claim log cluster)
                                            _ (when-not log
                                                (process/fail
                                                  process/instance
                                                  "Unable to claim log")
                                                (throw (java.lang.Error. "Unable to claim log"))
                                                1)
                                            prefetch_enabled? (config/prefetch-enabled?)
                                            bg_segment_prefetch_processor (background
                                                                            (str
                                                                              "segment-prefetch-processor-"
                                                                              db_id)
                                                                            segment-prefetch-processor
                                                                            :olookup
                                                                            olookup
                                                                            :prefetch-queue
                                                                            prefetch_queue
                                                                            :segments-channel
                                                                            segments_channel)
                                            _ (^clojure.lang.IFn bg_segment_prefetch_processor)
                                            bg_reader (background
                                                        (str "reader-" db_id)
                                                        reader
                                                        :db-id
                                                        db_id
                                                        :unprocessed-updates-queue
                                                        unprocessed_updates_queue
                                                        :session-fn
                                                        session-fn
                                                        :prefetch-queue
                                                        prefetch_queue
                                                        :prefetch-enabled?
                                                        prefetch_enabled?)
                                            _ (^clojure.lang.IFn bg_reader)
                                            _ (deliver peer_prom {:status :started})
                                            map__31335 (log/catchup
                                                         (db/db
                                                           db_id
                                                           (index/load-index olookup idxroot))
                                                         log)
                                            map__31335 (if (seq? map__31335)
                                                         (if (next map__31335)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__31335))
                                                           (if
                                                             (seq map__31335)
                                                             (first map__31335)
                                                             {}))
                                                         map__31335)
                                            db (get map__31335 :db)
                                            size (get map__31335 :size)
                                            db (patch-901 db)
                                            requested_index_t_ref (atom (:indexBasisT db))
                                            db_ref (atom db)
                                            bg_fressianer (background
                                                            (str "fressianer-" db_id)
                                                            fressianer
                                                            :processed-updates-queue
                                                            processed_updates_queue
                                                            :db-id
                                                            db_id
                                                            :writer-queue
                                                            writer_queue
                                                            :indexer
                                                            indexer)
                                            bg_notify_fressianer (background
                                                                   (str "notify-fressianer-" db_id)
                                                                   notify-fressianer
                                                                   :fressian-for-notify-queue
                                                                   fressian_for_notify_queue
                                                                   :block-notify-queue
                                                                   block_notify_queue)
                                            shutdown_ref (promise)
                                            bg_processor (background
                                                           (str "processor-" db_id)
                                                           processor
                                                           :unprocessed-updates-queue
                                                           unprocessed_updates_queue
                                                           :priority-updates-queue
                                                           priority_updates_queue
                                                           :fressian-for-notify-queue
                                                           fressian_for_notify_queue
                                                           :processed-updates-queue
                                                           processed_updates_queue
                                                           :log-tree-queue
                                                           log_tree_queue
                                                           :writer-queue
                                                           writer_queue
                                                           :index-sem
                                                           index_sem
                                                           :indexer
                                                           indexer
                                                           :processed-updates-queue
                                                           processed_updates_queue
                                                           :db-ref
                                                           db_ref
                                                           :db-id
                                                           db_id
                                                           :cluster
                                                           cluster
                                                           :olookup
                                                           olookup
                                                           :session-fn
                                                           session-fn
                                                           :shutdown-ref
                                                           shutdown_ref
                                                           :requested-index-t-ref
                                                           requested_index_t_ref
                                                           :probes-channel
                                                           probes_channel
                                                           :prefetch-enabled?
                                                           prefetch_enabled?)
                                            bg_writer (writer
                                                        :db-id
                                                        db_id
                                                        :writer-queue
                                                        writer_queue
                                                        :log-tree-queue
                                                        log_tree_queue
                                                        :block-threshold
                                                        log/segment-threshold
                                                        :block-notify-queue
                                                        block_notify_queue
                                                        :cs
                                                        cluster
                                                        :olookup
                                                        olookup
                                                        :log
                                                        log)
                                            bg_block_notifier (background
                                                                (str "block-notifier-" db_id)
                                                                block-notifier
                                                                :block-notify-queue
                                                                block_notify_queue
                                                                :db-id
                                                                db_id
                                                                :t-now
                                                                t_now
                                                                :session-fn
                                                                session-fn
                                                                :db-ref
                                                                db_ref
                                                                :indexer
                                                                indexer)
                                            bg_log_treeifier (background
                                                               (str "log-treeifier-" db_id)
                                                               log-treeifier
                                                               :cluster
                                                               cluster
                                                               :olookup
                                                               olookup
                                                               :log
                                                               log
                                                               :writer-queue
                                                               writer_queue
                                                               :log-tree-queue
                                                               log_tree_queue
                                                               :segment-threshold
                                                               log/segment-threshold
                                                               :dir-threshold
                                                               log/dir-threshold)
                                            database (datomic.update.Database.
                                                       db_ref
                                                       cluster
                                                       olookup
                                                       [bg_reader
                                                        bg_processor
                                                        bg_fressianer
                                                        bg_notify_fressianer
                                                        bg_writer
                                                        bg_block_notifier
                                                        bg_log_treeifier
                                                        bg_segment_prefetch_processor]
                                                       index_sem
                                                       {:unprocessed-updates-queue
                                                        unprocessed_updates_queue,
                                                        :processed-updates-queue
                                                        processed_updates_queue,
                                                        :fressian-for-notify-queue
                                                        fressian_for_notify_queue,
                                                        :writer-queue writer_queue,
                                                        :priority-updates-queue
                                                        priority_updates_queue,
                                                        :block-notify-queue block_notify_queue,
                                                        :log-tree-queue log_tree_queue,
                                                        :prefetch-queue prefetch_queue}
                                                       requested_index_t_ref
                                                       shutdown_ref)]
                                        (^clojure.lang.IFn bg_processor)
                                        (^clojure.lang.IFn bg_fressianer)
                                        (^clojure.lang.IFn bg_notify_fressianer)
                                        (^clojure.lang.IFn bg_writer)
                                        (^clojure.lang.IFn bg_log_treeifier)
                                        (^clojure.lang.IFn bg_block_notifier)
                                        (.put ^java.util.Map databases db_id database)
                                        (indexer/inc-memidx-usage indexer db_id size)
                                        (indexer/queue-index-jobs indexer)
                                        (when (db/memdb-needs-index? db)
                                          (queue/put
                                            priority_updates_queue
                                            {:request-index true, :up-to-t (:basisT db)})))}
                                     (catch
                                       java.lang.Throwable
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_31330 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_31331 (logger/format-as-msec (long elapsed_31330))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_31329 :msec msec_31331 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (throw (:threw result__8600__auto__))))
        :unused))
    (internal-stop-database
      [this db_id]
      (do
        (indexer/remove-database indexer db_id)
        (let [temp__5825__auto__ (.remove ^java.util.Map databases db_id)]
          (when temp__5825__auto__
            (let [database temp__5825__auto__]
              (future-call
                (fn fn__31318
                  ([]
                    (try
                      (let [m_31319 {:event :transactor/shutdown-db, :db-id db_id}
                            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.update")]
                                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                (.debug
                                                  ^org.slf4j.Logger logger
                                                  (logger/process (assoc m_31319 :phase :begin))))
                                              nil)
                            start__8599__auto__ (java.lang.System/nanoTime)
                            result__8600__auto__ (try
                                                   {:returned
                                                    (deref (common/async-shutdown database))}
                                                   (catch
                                                     java.lang.Throwable
                                                     t__8601__auto__
                                                     {:threw t__8601__auto__}))
                            elapsed_31320 (- (java.lang.System/nanoTime) start__8599__auto__)
                            msec_31321 (logger/format-as-msec (long elapsed_31320))]
                        (let [endmsg__8602__auto__ (merge
                                                     (assoc m_31319 :msec msec_31321 :phase :end)
                                                     (when (:threw result__8600__auto__)
                                                       {:threw
                                                        (class (:threw result__8600__auto__))}))
                              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                            (.debug
                              ^org.slf4j.Logger logger
                              (logger/process endmsg__8602__auto__)))
                          nil)
                        (if (contains? result__8600__auto__ :returned)
                          (:returned result__8600__auto__)
                          (do (throw (:threw result__8600__auto__)) nil)))
                      (catch
                        java.lang.Throwable
                        t__8765__auto__
                        (do
                          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                ex t__8765__auto__]
                            (when (.isWarnEnabled ^org.slf4j.Logger logger)
                              (.warn
                                ^org.slf4j.Logger logger
                                (logger/process "error executing future")
                                ^java.lang.Throwable ex)
                              (logger/caused-by logger ex))
                            nil)
                          (monitor/alarm :UnhandledException)
                          (throw ^java.lang.Throwable t__8765__auto__)
                          nil)))))))))
        :unused))
    (get-database [this db_id] (get databases db_id))
    (remote-ips [this] (aserver/remote-ips artemis-server))
    (rename-database
      [this p__31290]
      (let [map__31317 p__31290
            map__31317 (if (seq? map__31317)
                         (if (next map__31317)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31317))
                           (if (seq map__31317) (first map__31317) {}))
                         map__31317)
            db_name (get map__31317 :db-name)
            new_name (get map__31317 :new-name)]
        (acquire-transactor-semaphore sem)
        (try
          (let [cluster (coord/create-system-cluster system-cluster-conf)]
            (catalog/rename-database cluster db_name new_name))
          (finally (.release ^java.util.concurrent.Semaphore sem)))))
    (delete-database
      [this p__31289]
      (let [map__31316 p__31289
            map__31316 (if (seq? map__31316)
                         (if (next map__31316)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31316))
                           (if (seq map__31316) (first map__31316) {}))
                         map__31316)
            db_name (get map__31316 :db-name)]
        (acquire-transactor-semaphore sem)
        (try
          (let [cluster (coord/create-system-cluster system-cluster-conf)
                result (catalog/delete-database cluster db_name)]
            (let [temp__5825__auto__ (:db-id result)]
              (when temp__5825__auto__
                (let [db_id temp__5825__auto__] (internal-stop-database this db_id))))
            result)
          (finally (.release ^java.util.concurrent.Semaphore sem)))))
    (create-database
      [this p__31288]
      (let [map__31315 p__31288
            map__31315 (if (seq? map__31315)
                         (if (next map__31315)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31315))
                           (if (seq map__31315) (first map__31315) {}))
                         map__31315)
            desc map__31315
            db_name (get map__31315 :db-name)]
        (acquire-transactor-semaphore sem)
        (try
          (let [system_cluster (coord/create-system-cluster system-cluster-conf)
                result (catalog/create-database system_cluster desc)]
            (when (:created result)
              (let [db_id (:db-id result)
                    cluster (coord/create-db-cluster
                              (coord/cluster-conf->resolved-conf
                                (assoc system-cluster-conf :db-name db_name)))
                    olookup (^clojure.lang.IFn olookup-factory cluster)]
                (log/ensure-index-and-log cluster olookup db_id)))
            result)
          (finally (.release ^java.util.concurrent.Semaphore sem))))))
  (clojure.core/import 'datomic.update.Master)
  (defn ->Master
    ([system_cluster_conf
      olookup_factory
      endpoint
      artemis_server
      session_factory
      session_fn
      databases
      indexer
      sem]
      (datomic.update.Master.
        system_cluster_conf
        olookup_factory
        endpoint
        artemis_server
        session_factory
        session_fn
        databases
        indexer
        sem)))
  (reset-meta!
    #'->Master
    (assoc
      {:arglists
       (clojure.core/list
         ['system-cluster-conf
          'olookup-factory
          'endpoint
          'artemis-server
          'session-factory
          'session-fn
          'databases
          'indexer
          'sem]),
       :column (int 1)}
      :name
      '->Master
      :ns
      *ns*))
  (defn map->Master
    ([m__8001__auto__]
      (Master/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->Master
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->Master
      :ns
      *ns*))
  ;; Dispatch the transactor's catalog, indexing, and garbage-collection control protocol.
  (defn run-admin-command
    ([master p__31370]
      (let [vec__31371 p__31370
            seq__31372 (seq vec__31371)
            first__31373 (first seq__31372)
            seq__31372 (next seq__31372)
            cmd first__31373
            vec__31374 seq__31372
            arg (nth vec__31374 (int 0) nil)
            atype (:type arg)]
        (if (contains? #{nil :cryptf} atype)
          (let [result (let [G__31377 cmd]
                         (case
                           G__31377
                           :rename-database
                           (rename-database master arg)
                           :request-gc
                           (let [map__31378 arg
                                 map__31378 (if (seq? map__31378)
                                              (if (next map__31378)
                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                  (to-array map__31378))
                                                (if (seq map__31378) (first map__31378) {}))
                                              map__31378)
                                 db_id (get map__31378 :db-id)
                                 older_than (get map__31378 :older-than)
                                 temp__5823__auto__ (get-database master db_id)]
                             (if temp__5823__auto__
                               (let [dbase temp__5823__auto__]
                                 (garbage/queue-gc (:cluster dbase) older_than)
                                 {:queued db_id})
                               {:failed "no database"}))
                           :delete-database
                           (delete-database master arg)
                           :create-database
                           (create-database master arg)
                           :start-database
                           (start-database master arg)
                           :request-index
                           (let [temp__5823__auto__ (get-database master arg)]
                             (if temp__5823__auto__
                               (let [dbase temp__5823__auto__]
                                 (explicit-request-index dbase)
                                 {:queued arg})
                               {:failed "no database"}))
                           {:failed "protocol error"}))]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:event :transactor/admin-command, :cmd cmd, :arg arg, :result result})))
              nil)
            result)
          (let [td (:td master)]
            {:failed
             (str "Command not supported for " (:type td) " transactor " (:version td))})))))
  (reset-meta!
    #'run-admin-command
    (assoc
      {:arglists (clojure.core/list ['master ['cmd '& ['arg]]]), :column (int 1)}
      :name
      'run-admin-command
      :ns
      *ns*))
  ;; Scale index computation linearly from zero at the memory threshold to the
  ;; configured maximum at the redline.
  (defn compute-index-parallelism
    (^double [p__31382 bytes]
      (.doubleValue
        (let [map__31383 p__31382
              map__31383 (if (seq? map__31383)
                           (if (next map__31383)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__31383))
                             (if (seq map__31383) (first map__31383) {}))
                           map__31383)
              threshold (get map__31383 :threshold)
              redline (get map__31383 :redline)
              max_par (get map__31383 :max-par)]
          (cond
            (< bytes threshold) 0.0
            (>= bytes redline) max_par
            :else (do
                    (java.lang.Double/valueOf
                      (double
                        (* max_par (/ (double (- bytes threshold)) (- redline threshold)))))))))))
  (reset-meta!
    #'compute-index-parallelism
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [{:keys ['threshold 'redline 'max-par]} 'bytes] {:tag 'double})),
       :column (int 1)}
      :name
      'compute-index-parallelism
      :ns
      *ns*))
  ;; Adjust indexing concurrency as memory-index pressure rises, reserving more
  ;; compute capacity to drain novelty before reaching the configured maximum.
  (defn start-dynamic-index-parallelism
    ([memidx_usage]
      (let [threshold (config/property "datomic.memoryIndexThreshold")
            memidx_max (config/property "datomic.memoryIndexMax")
            redline (/ (+ threshold memidx_max) 2)
            config {:threshold threshold,
                    :redline redline,
                    :max-par
                    (long (max 1 (quot (.availableProcessors (java.lang.Runtime/getRuntime)) 2)))}]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process (merge {:event :datomic.update/dynamic-index-parallelism} config))))
          nil)
        (add-watch
          memidx_usage
          :datomic.update/dynamic-index-parallelism
          (fn fn__31385
            ([_ _ _ usage]
              (let [par (long (max 1 (compute-index-parallelism config (:total usage))))]
                (reset! index/index-parallelism (long par))))))
        (add-watch
          index/index-parallelism
          :datomic.update/index-parallelism-metric
          (fn fn__31387
            ([_ _ old new]
              (when-not (= old new)
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process {:event :update/index-compute-parallelism, :value new})))
                  nil)
                (monitor/add-stat :IndexComputeParallelism new))))))))
  (reset-meta!
    #'start-dynamic-index-parallelism
    (assoc
      {:arglists (clojure.core/list ['memidx-usage]), :column (int 1)}
      :name
      'start-dynamic-index-parallelism
      :ns
      *ns*))
  ;; Start the process-wide transport and indexer and construct the catalog
  ;; service that owns all per-database runtimes.
  (defn create-master
    ([& args]
      (let [m_31390 {:event :update/create-master}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_31390 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (let [map__31394 args
                                          map__31394 (if (seq? map__31394)
                                                       (if (next map__31394)
                                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                           (to-array map__31394))
                                                         (if (seq map__31394)
                                                           (first map__31394)
                                                           {}))
                                                       map__31394)
                                          system_cluster_conf (get map__31394 :system-cluster-conf)
                                          olookup_factory (get map__31394 :olookup-factory)
                                          endpoint (get map__31394 :endpoint)
                                          td (get map__31394 :td)
                                          server (let [G__31395 (aserver/start-server
                                                                  endpoint
                                                                  (if
                                                                    (= :limited-edition (:type td))
                                                                    2
                                                                    1000000))]
                                                   (aserver/add-address-settings
                                                     G__31395
                                                     "*.tx-submit"
                                                     :maxSizeBytes
                                                     (long (* 256 1024))
                                                     :addressFullMessagePolicy
                                                     org.apache.activemq.artemis.core.settings.impl.AddressFullMessagePolicy/BLOCK)
                                                   G__31395)
                                          connector (aclient/create-connector
                                                      aclient/in-vm-connector-factory
                                                      :serverId
                                                      (:port endpoint))
                                          session_factory (aclient/create-session-factory
                                                            connector
                                                            {:ttl -1})
                                          session_fn (fn session_fn
                                                       ([]
                                                         (aclient/start-session
                                                           session_factory
                                                           endpoint
                                                           :pre-acknowledge
                                                           true)))
                                          databases (java.util.concurrent.ConcurrentHashMap.)
                                          indexer (indexer/create-indexer :databases databases)
                                          master (datomic.update.Master.
                                                   system_cluster_conf
                                                   olookup_factory
                                                   endpoint
                                                   server
                                                   session_factory
                                                   session_fn
                                                   databases
                                                   indexer
                                                   (java.util.concurrent.Semaphore.
                                                     (int 1)
                                                     (boolean (.booleanValue true))))
                                          master (assoc master :td td)
                                          admin_service (aclient/create-rpc-server
                                                          session_fn
                                                          "admin.request"
                                                          "admin.response"
                                                          (partial run-admin-command master))]
                                      (if (config/property "datomic.dynamicIndexParallelism")
                                        (start-dynamic-index-parallelism
                                          (.-memidx-usage ^datomic.indexer.IndexerImpl indexer))
                                        (reset!
                                          index/index-parallelism
                                          (config/property "datomic.indexParallelism")))
                                      master)}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_31391 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_31392 (logger/format-as-msec (long elapsed_31391))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_31390 :msec msec_31392 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'create-master
    (assoc
      {:arglists (clojure.core/list ['& 'args]), :column (int 1)}
      :name
      'create-master
      :ns
      *ns*)))

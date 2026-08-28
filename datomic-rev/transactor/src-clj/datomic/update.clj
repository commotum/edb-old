(do
  (clojure.core/in-ns 'datomic.update)
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
    (assoc {:const true, :column 1} :name 'max-unprocessed-queue :ns *ns*))
  (def PREFETCH_CHAN_BUFFER_SIZE 10000)
  (reset-meta!
    #'PREFETCH_CHAN_BUFFER_SIZE
    (assoc {:const true, :column 1} :name 'PREFETCH_CHAN_BUFFER_SIZE :ns *ns*))
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
              (fn fn__24955
                ([]
                  (let [temp__5804__auto__ (deref shutdown_hook)]
                    (when temp__5804__auto__
                      (let [hook temp__5804__auto__] (^clojure.lang.IFn hook))))
                  (.interrupt ^java.lang.Thread thread)
                  (.join ^java.lang.Thread thread)
                  nil))))
          (deref [this] args)
          (invoke [this] (do (.start ^java.lang.Thread thread) this))))))
  (defonce prefetch-channels-ref
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
                                (let [vec__24964 (a/alts!!
                                                   [probes_channel segments_channel]
                                                   :priority
                                                   true)
                                      f (nth vec__24964 (int 0) nil)]
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
                                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
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
  (defn prefetch-channels ([] (deref prefetch-channels-ref)))
  (defn until-interrupt
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
                                              (clojure.core/list 'recur)))))))))))))))))))))))
  (.setMacro #'until-interrupt)
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
  (defn segment-prefetch-processor
    ([shutdown_hook & p__24978]
      (let [map__24979 p__24978
            map__24979 (if (seq? map__24979)
                         (if (next map__24979)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24979))
                           (if (seq map__24979) (first map__24979) {}))
                         map__24979)
            prefetch_queue (get map__24979 :prefetch-queue)
            olookup (get map__24979 :olookup)
            segments_channel (get map__24979 :segments-channel)
            m_24980 {:task :segment-prefetch-processor, :event :update/loop}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_24980 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [done (java.lang.Object.)]
                                      (loop []
                                        (if (process/failing? process/instance)
                                          :process-failed
                                          (let [result (try
                                                         (let [map__24986
                                                               (queue/take prefetch_queue)
                                                               map__24986
                                                               (if
                                                                 (seq? map__24986)
                                                                 (if
                                                                   (next map__24986)
                                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                     (to-array map__24986))
                                                                   (if
                                                                     (seq map__24986)
                                                                     (first map__24986)
                                                                     {}))
                                                                 map__24986)
                                                               procargs map__24986
                                                               prefetch_io_stats_bindings
                                                               (get
                                                                 map__24986
                                                                 :prefetch-io-stats-bindings)
                                                               tx_promise
                                                               (get map__24986 :tx-promise)
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
                                                                 fn__24987
                                                                 ([k]
                                                                   (a/offer!
                                                                     segments_channel
                                                                     (fn 
                                                                       fn__24988
                                                                       ([]
                                                                         (when-not
                                                                           (realized? tx_promise)
                                                                           (with-bindings*
                                                                             prefetch_io_stats_bindings
                                                                             (fn 
                                                                               fn__24989
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
                                            (if (= done result) :interrupted (recur)))))
                                      nil)}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_24981 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_24982 (logger/format-as-msec (long elapsed_24981))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_24980 :msec msec_24982 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn dropping-dispatcher
    ([] (reify datomic.db.PrefetchDispatcher (close [this] nil) (prefetch1 [this _] nil))))
  (defn channel-forwarding-dispatcher
    ([tx_promise io_stats_bindings probes_channel]
      (reify
        datomic.db.PrefetchDispatcher
        (close [this] (deliver tx_promise true))
        (prefetch1
          [this f]
          (a/offer!
            probes_channel
            (fn fn__25005
              ([] (when-not (realized? tx_promise) (with-bindings* io_stats_bindings f)))))))))
  (defn ->dispatcher
    ([procargs p__25009]
      (let [map__25010 p__25009
            map__25010 (if (seq? map__25010)
                         (if (next map__25010)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25010))
                           (if (seq map__25010) (first map__25010) {}))
                         map__25010)
            probes_channel (get map__25010 :probes-channel)
            prefetch_enabled? (get map__25010 :prefetch-enabled?)
            prefetch_io_stats_bindings (common/getx procargs :prefetch-io-stats-bindings)
            tx_promise (common/getx procargs :tx-promise)]
        (if (:hints (:options procargs))
          [(dropping-dispatcher) :datomic.db/segment-prefetch]
          (if prefetch_enabled?
            [(channel-forwarding-dispatcher tx_promise prefetch_io_stats_bindings probes_channel)
             :datomic.db/probe-prefetch]
            [(dropping-dispatcher) :datomic.db/probe-prefetch])))))
  (defn reader
    ([shutdown_hook & p__25012]
      (let [map__25013 p__25012
            map__25013 (if (seq? map__25013)
                         (if (next map__25013)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25013))
                           (if (seq map__25013) (first map__25013) {}))
                         map__25013)
            db_id (get map__25013 :db-id)
            session_fn (get map__25013 :session-fn)
            unprocessed_updates_queue (get map__25013 :unprocessed-updates-queue)
            prefetch_queue (get map__25013 :prefetch-queue)
            prefetch_enabled? (get map__25013 :prefetch-enabled?)
            session (^clojure.lang.IFn session_fn)
            submit_queue (aclient/create-temporary-queue session (tx/submit-address db_id))
            consumer (aclient/create-consumer session submit_queue :window-size 0)
            sync_producer (aclient/create-producer session (tx/push-address db_id))
            sync_queue (aclient/fressian-producer session sync_producer (tx/write-handlers false))]
        (^clojure.lang.IFn shutdown_hook
          (fn fn__25014
            ([]
              (deref (common/async-shutdown consumer))
              (aclient/delete-queue session submit_queue)
              (deref (common/async-shutdown sync_producer))
              (deref (common/async-shutdown session)))))
        (let [m_25016 {:task :reader, :event :update/loop}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_25016 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
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
                                                                  (= type :tx)
                                                                  (assoc
                                                                    :prefetch-io-stats-bindings
                                                                    prefetch_io_stats_bindings
                                                                    :tx-promise
                                                                    txprom))]
                                                               (queue/put
                                                                 (let 
                                                                   [G__25023 type]
                                                                   (case
                                                                     G__25023
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
                                                             done)
                                                           (catch
                                                             org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException
                                                             _
                                                             done))]
                                              (if (= done result) :interrupted (recur)))))
                                        nil)}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_25017 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_25018 (logger/format-as-msec (long elapsed_25017))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_25016 :msec msec_25018 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (do (throw (:threw result__8585__auto__)) nil))))))
  (defn process-new-index
    ([p__25033 p__25034]
      (let [map__25035 p__25033
            map__25035 (if (seq? map__25035)
                         (if (next map__25035)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25035))
                           (if (seq map__25035) (first map__25035) {}))
                         map__25035)
            new_index (get map__25035 :new-index)
            excised (get map__25035 :excised)
            map__25036 p__25034
            map__25036 (if (seq? map__25036)
                         (if (next map__25036)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25036))
                           (if (seq map__25036) (first map__25036) {}))
                         map__25036)
            db_ref (get map__25036 :db-ref)
            index_sem (get map__25036 :index-sem)
            indexer (get map__25036 :indexer)
            db_id (get map__25036 :db-id)
            processed_updates_queue (get map__25036 :processed-updates-queue)
            priority_updates_queue (get map__25036 :priority-updates-queue)
            requested_index_t_ref (get map__25036 :requested-index-t-ref)
            result_push (get map__25036 :result-push)
            db (swap! db_ref db/complete-indexing new_index)]
        (indexer/indexing-completed indexer db_id)
        (.release ^java.util.concurrent.Semaphore index_sem)
        (indexer/queue-db-index-job indexer db_id)
        (indexer/queue-index-jobs indexer)
        (when (< (:indexBasisT db) (deref requested_index_t_ref))
          (queue/put priority_updates_queue {:request-index true}))
        (when (config/property "datomic.indexMetrics")
          (future-call
            (fn fn__25037
              ([]
                (try
                  (let [metrics (stats/index-metrics db)]
                    (loop [seq_25038 (seq metrics) chunk_25039 nil count_25040 0 i_25041 0]
                      (if (< i_25041 count_25040)
                        (let [vec__25042 (.nth ^clojure.lang.Indexed chunk_25039 (int i_25041))
                              k (nth vec__25042 (int 0) nil)
                              v (nth vec__25042 (int 1) nil)]
                          (monitor/add-stat k v)
                          (recur seq_25038 chunk_25039 count_25040 (inc i_25041)))
                        (let [temp__5804__auto__ (seq seq_25038)]
                          (when temp__5804__auto__
                            (let [seq_25038 temp__5804__auto__]
                              (if (chunked-seq? seq_25038)
                                (let [c__6065__auto__ (chunk-first seq_25038)]
                                  (recur
                                    (chunk-rest seq_25038)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [vec__25045 (first seq_25038)
                                      k (nth vec__25045 (int 0) nil)
                                      v (nth vec__25045 (int 1) nil)]
                                  (monitor/add-stat k v)
                                  (recur (next seq_25038) nil 0 0))))))))
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process
                            (assoc metrics :event :update/adopted-index :id (:id db)))))
                      nil))
                  (catch
                    java.lang.Throwable
                    t__8829__auto__
                    (do
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                            ex t__8829__auto__]
                        (when (.isWarnEnabled ^org.slf4j.Logger logger)
                          (.warn
                            ^org.slf4j.Logger logger
                            (logger/process "error executing future")
                            ^java.lang.Throwable ex)
                          (logger/caused-by logger ex))
                        nil)
                      (monitor/alarm :UnhandledException)
                      (throw ^java.lang.Throwable t__8829__auto__)
                      nil)))))))
        (queue/put result_push {:notification :new-index}))))
  (defn process-request-index
    ([p__25052 p__25053]
      (let [map__25054 p__25052
            map__25054 (if (seq? map__25054)
                         (if (next map__25054)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25054))
                           (if (seq map__25054) (first map__25054) {}))
                         map__25054)
            up_to_t (get map__25054 :up-to-t)
            map__25055 p__25053
            map__25055 (if (seq? map__25055)
                         (if (next map__25055)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25055))
                           (if (seq map__25055) (first map__25055) {}))
                         map__25055)
            log_tree_queue (get map__25055 :log-tree-queue)
            index_sem (get map__25055 :index-sem)
            db_id (get map__25055 :db-id)
            cluster (get map__25055 :cluster)
            priority_updates_queue (get map__25055 :priority-updates-queue)
            olookup (get map__25055 :olookup)
            db_ref (get map__25055 :db-ref)
            indexer (get map__25055 :indexer)
            processed_updates_queue (get map__25055 :processed-updates-queue)
            requested_index_t_ref (get map__25055 :requested-index-t-ref)
            shutdown_ref (get map__25055 :shutdown-ref)]
        (let [temp__5804__auto__ (cond
                                   (number? up_to_t) up_to_t
                                   (#{:basisT} up_to_t) (do (:basisT (deref db_ref))))]
          (when temp__5804__auto__
            (let [t temp__5804__auto__]
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
                  (fn fn__25056
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
                                            (let [m_25058 {:event :update/create-index,
                                                           :next-t nextT,
                                                           :id (:id db)}
                                                  ___8583__auto__ (let 
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
                                                                            m_25058
                                                                            :phase
                                                                            :begin))))
                                                                    nil)
                                                  start__8584__auto__ (java.lang.System/nanoTime)
                                                  result__8585__auto__ (try
                                                                         {:returned
                                                                          (let 
                                                                            [map__25062
                                                                             (index/merge-db
                                                                               cluster
                                                                               olookup
                                                                               db
                                                                               (.getNextT
                                                                                 ^datomic.db.IDb db))
                                                                             map__25062
                                                                             (if
                                                                               (seq? map__25062)
                                                                               (if
                                                                                 (next map__25062)
                                                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                                   (to-array
                                                                                     map__25062))
                                                                                 (if
                                                                                   (seq map__25062)
                                                                                   (first
                                                                                     map__25062)
                                                                                   {}))
                                                                               map__25062)
                                                                             new_index
                                                                             (get
                                                                               map__25062
                                                                               :new-index)
                                                                             xpreds
                                                                             (get
                                                                               map__25062
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
                                                                           t__8586__auto__
                                                                           {:threw
                                                                            t__8586__auto__}))
                                                  elapsed_25059 (-
                                                                  (java.lang.System/nanoTime)
                                                                  start__8584__auto__)
                                                  msec_25060 (logger/format-as-msec
                                                               (long elapsed_25059))]
                                              (monitor/add-stat :CreateEntireIndexMsec msec_25060)
                                              (let [endmsg__8587__auto__ (merge
                                                                           (assoc
                                                                             m_25058
                                                                             :msec
                                                                             msec_25060
                                                                             :phase
                                                                             :end)
                                                                           (when
                                                                             (:threw
                                                                               result__8585__auto__)
                                                                             {:threw
                                                                              (class
                                                                                (:threw
                                                                                  result__8585__auto__))}))
                                                    logger (org.slf4j.LoggerFactory/getLogger
                                                             "datomic.update")]
                                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                  (.info
                                                    ^org.slf4j.Logger logger
                                                    (logger/process endmsg__8587__auto__)))
                                                nil)
                                              (if (contains? result__8585__auto__ :returned)
                                                (:returned result__8585__auto__)
                                                (do (throw (:threw result__8585__auto__)) nil))))
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
                          t__8829__auto__
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                  ex t__8829__auto__]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "error executing future")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            (monitor/alarm :UnhandledException)
                            (throw ^java.lang.Throwable t__8829__auto__)
                            nil)))))))))))))
  (defn swap-xf!
    ([atom xf f & args]
      (loop []
        (let [v1 (deref atom) ret (apply f v1 args) v2 (^clojure.lang.IFn xf ret)]
          (if (compare-and-set! atom v1 v2) ret (recur))))))
  (defn with-tx*
    ([db dispatcher tx_data]
      (try
        (db/with-tx db dispatcher tx_data)
        (catch java.lang.Throwable t {:db-after db, :ex t}))))
  (defn process-transaction
    ([procargs p__25074]
      (let [map__25075 p__25074
            map__25075 (if (seq? map__25075)
                         (if (next map__25075)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25075))
                           (if (seq map__25075) (first map__25075) {}))
                         map__25075)
            process_args map__25075
            db_ref (get map__25075 :db-ref)
            processed_updates_queue (get map__25075 :processed-updates-queue)
            fressian_for_notify_queue (get map__25075 :fressian-for-notify-queue)
            priority_updates_queue (get map__25075 :priority-updates-queue)
            id (common/getx procargs :id)
            started_at (java.lang.System/nanoTime)
            requested_context (get-in procargs [:options :io-context])
            prefetch_io_stats_bindings (common/getx procargs :prefetch-io-stats-bindings)
            tx_promise (common/getx procargs :tx-promise)
            vec__25076 (->dispatcher procargs process_args)
            dispatcher (nth vec__25076 (int 0) nil)
            prefetch_io_context (nth vec__25076 (int 1) nil)
            io_context (if (qualified-keyword? requested_context) requested_context :db/tx)
            data (common/getx procargs :data)
            map__25079 (io-stats/with-io-stats
                         (fn fn__25080 ([] (swap-xf! db_ref :db-after with-tx* dispatcher data)))
                         {:api :tx-with, :io-context io_context})
            map__25079 (if (seq? map__25079)
                         (if (next map__25079)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25079))
                           (if (seq map__25079) (first map__25079) {}))
                         map__25079)
            ret (get map__25079 :ret)
            io_stats (get map__25079 :io-stats)
            ex (get map__25079 :ex)]
        (deliver tx_promise true)
        (when ex (throw ^java.lang.Throwable ex))
        (if (:tx-data ret)
          (let [applied_at (java.lang.System/nanoTime)
                map__25082 ret
                map__25082 (if (seq? map__25082)
                             (if (next map__25082)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__25082))
                               (if (seq map__25082) (first map__25082) {}))
                             map__25082)
                tx_data (get map__25082 :tx-data)
                tempids (get map__25082 :tempids)
                db_after (get map__25082 :db-after)
                tx_stats (get map__25082 :tx-stats)
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
                     :logged (let [G__25083 (promise)] (deliver G__25083 true) G__25083)}]
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
  (defn processor
    ([shutdown_hook & p__25086]
      (let [map__25087 p__25086
            map__25087 (if (seq? map__25087)
                         (if (next map__25087)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25087))
                           (if (seq map__25087) (first map__25087) {}))
                         map__25087)
            processor_args map__25087
            index_sem (get map__25087 :index-sem)
            db_id (get map__25087 :db-id)
            unprocessed_updates_queue (get map__25087 :unprocessed-updates-queue)
            priority_updates_queue (get map__25087 :priority-updates-queue)
            session_fn (get map__25087 :session-fn)
            fressian_for_notify_queue (get map__25087 :fressian-for-notify-queue)
            db_ref (get map__25087 :db-ref)
            indexer (get map__25087 :indexer)
            processed_updates_queue (get map__25087 :processed-updates-queue)
            requested_index_t_ref (get map__25087 :requested-index-t-ref)
            session (^clojure.lang.IFn session_fn)
            producer (aclient/create-producer session (tx/push-address db_id))
            result_push (aclient/fressian-producer session producer (tx/write-handlers false))
            processor_args (assoc processor_args :result-push result_push)]
        (^clojure.lang.IFn shutdown_hook
          (fn fn__25088
            ([] (deref (common/async-shutdown producer)) (deref (common/async-shutdown session)))))
        (let [m_25090 {:task :processor, :event :update/loop}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_25090 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
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
                                              (if (= done result) :interrupted (recur)))))
                                        nil)}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_25091 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_25092 (logger/format-as-msec (long elapsed_25091))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_25090 :msec msec_25092 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (do (throw (:threw result__8585__auto__)) nil))))))
  (defn fressianer
    ([shutdown_hook & p__25107]
      (let [map__25108 p__25107
            map__25108 (if (seq? map__25108)
                         (if (next map__25108)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25108))
                           (if (seq map__25108) (first map__25108) {}))
                         map__25108)
            processed_updates_queue (get map__25108 :processed-updates-queue)
            writer_queue (get map__25108 :writer-queue)
            indexer (get map__25108 :indexer)
            db_id (get map__25108 :db-id)]
        (^clojure.lang.IFn shutdown_hook nil)
        (let [m_25109 {:task :fressianer, :event :update/loop}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_25109 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
                                     {:returned
                                      (let [done (java.lang.Object.)]
                                        (loop []
                                          (if (process/failing? process/instance)
                                            :process-failed
                                            (let [result (try
                                                           (let 
                                                             [tx
                                                              (queue/take processed_updates_queue)
                                                              temp__5802__auto__ (get tx :id)]
                                                             (if
                                                               temp__5802__auto__
                                                               (let 
                                                                 [id temp__5802__auto__]
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
                                              (if (= done result) :interrupted (recur)))))
                                        nil)}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_25110 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_25111 (logger/format-as-msec (long elapsed_25110))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_25109 :msec msec_25111 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (do (throw (:threw result__8585__auto__)) nil))))))
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
  (defn notify-fressianer
    ([shutdown_hook & p__25125]
      (let [map__25126 p__25125
            map__25126 (if (seq? map__25126)
                         (if (next map__25126)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25126))
                           (if (seq map__25126) (first map__25126) {}))
                         map__25126)
            fressian_for_notify_queue (get map__25126 :fressian-for-notify-queue)
            block_notify_queue (get map__25126 :block-notify-queue)]
        (^clojure.lang.IFn shutdown_hook nil)
        (let [m_25127 {:task :notifier, :event :update/loop}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_25127 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
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
                                              (if (= done result) :interrupted (recur)))))
                                        nil)}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_25128 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_25129 (logger/format-as-msec (long elapsed_25128))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_25127 :msec msec_25129 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (do (throw (:threw result__8585__auto__)) nil))))))
  (defmulti writer-process (fn fn__25142 ([item ctxt] (:type item))))
  (defmethod
    writer-process
    :ensure-tree
    fn__25149
    ([p__25147 p__25148]
      (let [map__25150 p__25147
            map__25150 (if (seq? map__25150)
                         (if (next map__25150)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25150))
                           (if (seq map__25150) (first map__25150) {}))
                         map__25150)
            completed (get map__25150 :completed)
            map__25151 p__25148
            map__25151 (if (seq? map__25151)
                         (if (next map__25151)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25151))
                           (if (seq map__25151) (first map__25151) {}))
                         map__25151)
            log_ref (get map__25151 :log-ref)
            cs (get map__25151 :cs)
            log_tree_queue (get map__25151 :log-tree-queue)
            map__25152 (deref log_ref)
            map__25152 (if (seq? map__25152)
                         (if (next map__25152)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25152))
                           (if (seq map__25152) (first map__25152) {}))
                         map__25152)
            tail (get map__25152 :tail)]
        (if (log/tail-empty? tail)
          (deliver completed true)
          (queue/put log_tree_queue {:type :extend-tree, :completed completed, :tail tail})))))
  (defmethod
    writer-process
    :adopt-tree
    fn__25156
    ([p__25154 p__25155]
      (let [map__25157 p__25154
            map__25157 (if (seq? map__25157)
                         (if (next map__25157)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25157))
                           (if (seq map__25157) (first map__25157) {}))
                         map__25157)
            completed (get map__25157 :completed)
            root_id (get map__25157 :root-id)
            garbage_ids (get map__25157 :garbage-ids)
            t (get map__25157 :t)
            map__25158 p__25155
            map__25158 (if (seq? map__25158)
                         (if (next map__25158)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25158))
                           (if (seq map__25158) (first map__25158) {}))
                         map__25158)
            log_ref (get map__25158 :log-ref)
            cs (get map__25158 :cs)
            olookup (get map__25158 :olookup)
            log_tree_queue (get map__25158 :log-tree-queue)]
        (let [map__25159 (swap! log_ref log/adopt-root cs root_id t)
              map__25159 (if (seq? map__25159)
                           (if (next map__25159)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__25159))
                             (if (seq map__25159) (first map__25159) {}))
                           map__25159)
              tail (get map__25159 :tail)]
          (when-not (log/tail-empty? tail)
            (queue/put log_tree_queue {:type :new-tail, :tail tail})))
        (events/publish {:key :datomic.garbage/mark, :cluster cs, :garbage garbage_ids})
        (some-> completed (deliver true)))))
  (defn writer
    ([& p__25162]
      (let [map__25163 p__25162
            map__25163 (if (seq? map__25163)
                         (if (next map__25163)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25163))
                           (if (seq map__25163) (first map__25163) {}))
                         map__25163)
            context map__25163
            writer_queue (get map__25163 :writer-queue)
            log_tree_queue (get map__25163 :log-tree-queue)
            block_threshold (get map__25163 :block-threshold)
            log (get map__25163 :log)
            cs (get map__25163 :cs)
            olookup (get map__25163 :olookup)
            db_id (get map__25163 :db-id)
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
                              (loop [seq_25165 (seq txes) chunk_25166 nil count_25167 0 i_25168 0]
                                (if (< i_25168 count_25167)
                                  (let [tx (.nth ^clojure.lang.Indexed chunk_25166 (int i_25168))]
                                    (deliver (common/getx tx :logged) true)
                                    (monitor/add-stat
                                      :TransactionBytes
                                      (long (dio/remaining (:fressianed-tx tx))))
                                    (recur seq_25165 chunk_25166 count_25167 (inc i_25168)))
                                  (let [temp__5804__auto__ (seq seq_25165)]
                                    (when temp__5804__auto__
                                      (let [seq_25165 temp__5804__auto__]
                                        (if (chunked-seq? seq_25165)
                                          (let [c__6065__auto__ (chunk-first seq_25165)]
                                            (recur
                                              (chunk-rest seq_25165)
                                              c__6065__auto__
                                              (int (count c__6065__auto__))
                                              (int 0)))
                                          (let [tx (first seq_25165)]
                                            (deliver (common/getx tx :logged) true)
                                            (monitor/add-stat
                                              :TransactionBytes
                                              (long (dio/remaining (:fressianed-tx tx))))
                                            (recur (next seq_25165) nil 0 0)))))))))
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
                                 (let [temp__5802__auto__ (queue/poll writer_queue)]
                                   (if temp__5802__auto__
                                     (let [next_item temp__5802__auto__] (recur txes next_item))
                                     (do (^clojure.lang.IFn log_block txes) nil)))))
                             (do
                               (when (seq txes) (^clojure.lang.IFn log_block txes))
                               (writer-process next_item context)
                               nil)))))
                     nil))
            launch (delay
                     (let [G__25176 (java.lang.Thread.
                                      (fn fn__25177
                                        ([]
                                          (try
                                            (^clojure.lang.IFn proc)
                                            (catch
                                              java.lang.Throwable
                                              t
                                              (process/fail process/instance "Writer failed" t)))))
                                      (str "writer-" db_id))]
                       (.start ^java.lang.Thread G__25176)
                       G__25176))]
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
  (defn block-notifier
    ([shutdown_hook & p__25183]
      (let [map__25184 p__25183
            map__25184 (if (seq? map__25184)
                         (if (next map__25184)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25184))
                           (if (seq map__25184) (first map__25184) {}))
                         map__25184)
            db_id (get map__25184 :db-id)
            session_fn (get map__25184 :session-fn)
            block_notify_queue (get map__25184 :block-notify-queue)
            t_now (get map__25184 :t-now)
            db_ref (get map__25184 :db-ref)
            session (^clojure.lang.IFn session_fn)
            producer (aclient/create-producer session (tx/push-address db_id))]
        (^clojure.lang.IFn shutdown_hook
          (fn fn__25185
            ([] (deref (common/async-shutdown producer)) (deref (common/async-shutdown session)))))
        (let [m_25187 {:task :block-notifier, :event :update/loop}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_25187 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
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
                                              (if (= done result) :interrupted (recur)))))
                                        nil)}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_25188 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_25189 (logger/format-as-msec (long elapsed_25188))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_25187 :msec msec_25189 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (do (throw (:threw result__8585__auto__)) nil))))))
  (defmulti handle-log-tree-request (fn fn__25202 ([msg _] (:type msg))))
  (defn extend-tree
    ([p__25207 p__25208]
      (let [map__25209 p__25207
            map__25209 (if (seq? map__25209)
                         (if (next map__25209)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25209))
                           (if (seq map__25209) (first map__25209) {}))
                         map__25209)
            tail (get map__25209 :tail)
            completed (get map__25209 :completed)
            map__25210 p__25208
            map__25210 (if (seq? map__25210)
                         (if (next map__25210)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25210))
                           (if (seq map__25210) (first map__25210) {}))
                         map__25210)
            cluster (get map__25210 :cluster)
            olookup (get map__25210 :olookup)
            writer_queue (get map__25210 :writer-queue)
            segment_threshold (get map__25210 :segment-threshold)
            dir_threshold (get map__25210 :dir-threshold)
            root_id_ref (get map__25210 :root-id-ref)
            t_ref (get map__25210 :t-ref)]
        (when-not (not (log/tail-empty? tail))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str (clojure.core/list 'not (clojure.core/list 'log/tail-empty? 'tail)))))))
        (let [ts (log/tail-ts tail) t (last ts) log_tail_byte_count (log/tail-byte-count tail)]
          (monitor/add-stat :LogTailBytes log_tail_byte_count)
          (let [m_25211 {:event :update/treeify-log,
                         :first-t (first ts),
                         :last-t t,
                         :tail-bytes log_tail_byte_count}
                ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_25211 :phase :begin))))
                                  nil)
                start__8584__auto__ (java.lang.System/nanoTime)
                result__8585__auto__ (try
                                       {:returned
                                        (let [map__25215 (log/extend-tree
                                                           cluster
                                                           olookup
                                                           (deref root_id_ref)
                                                           dir_threshold
                                                           (log/create-leaves
                                                             segment_threshold
                                                             tail))
                                              map__25215 (if (seq? map__25215)
                                                           (if
                                                             (next map__25215)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__25215))
                                                             (if
                                                               (seq map__25215)
                                                               (first map__25215)
                                                               {}))
                                                           map__25215)
                                              result map__25215
                                              root_id (get map__25215 :root-id)
                                              garbage_ids (get map__25215 :garbage-ids)]
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
                                         t__8586__auto__
                                         {:threw t__8586__auto__}))
                elapsed_25212 (- (java.lang.System/nanoTime) start__8584__auto__)
                msec_25213 (logger/format-as-msec (long elapsed_25212))]
            (let [endmsg__8587__auto__ (merge
                                         (assoc m_25211 :msec msec_25213 :phase :end)
                                         (when (:threw result__8585__auto__)
                                           {:threw (class (:threw result__8585__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
              nil)
            (if (contains? result__8585__auto__ :returned)
              (:returned result__8585__auto__)
              (do (throw (:threw result__8585__auto__)) nil)))))))
  (defmethod
    handle-log-tree-request
    :new-tail
    fn__25224
    ([p__25222 p__25223]
      (let [map__25225 p__25222
            map__25225 (if (seq? map__25225)
                         (if (next map__25225)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25225))
                           (if (seq map__25225) (first map__25225) {}))
                         map__25225)
            tail (get map__25225 :tail)
            map__25226 p__25223
            map__25226 (if (seq? map__25226)
                         (if (next map__25226)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25226))
                           (if (seq map__25226) (first map__25226) {}))
                         map__25226)
            context map__25226
            segment_threshold (get map__25226 :segment-threshold)
            t_ref (get map__25226 :t-ref)
            tail (log/since tail (deref t_ref))]
        (when (<= segment_threshold (log/tail-byte-count tail))
          (extend-tree {:tail tail} context)))))
  (defmethod
    handle-log-tree-request
    :extend-tree
    fn__25230
    ([p__25228 p__25229]
      (let [map__25231 p__25228
            map__25231 (if (seq? map__25231)
                         (if (next map__25231)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25231))
                           (if (seq map__25231) (first map__25231) {}))
                         map__25231)
            tail (get map__25231 :tail)
            completed (get map__25231 :completed)
            map__25232 p__25229
            map__25232 (if (seq? map__25232)
                         (if (next map__25232)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25232))
                           (if (seq map__25232) (first map__25232) {}))
                         map__25232)
            context map__25232
            t_ref (get map__25232 :t-ref)
            tail (log/since tail (deref t_ref))]
        (if (log/tail-empty? tail)
          (deliver completed true)
          (extend-tree {:tail tail, :completed completed} context)))))
  (defmethod
    handle-log-tree-request
    :excise-root
    fn__25236
    ([p__25234 p__25235]
      (let [map__25237 p__25234
            map__25237 (if (seq? map__25237)
                         (if (next map__25237)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25237))
                           (if (seq map__25237) (first map__25237) {}))
                         map__25237)
            request map__25237
            completed (get map__25237 :completed)
            map__25238 p__25235
            map__25238 (if (seq? map__25238)
                         (if (next map__25238)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25238))
                           (if (seq map__25238) (first map__25238) {}))
                         map__25238)
            cluster (get map__25238 :cluster)
            olookup (get map__25238 :olookup)
            writer_queue (get map__25238 :writer-queue)
            root_id_ref (get map__25238 :root-id-ref)
            t_ref (get map__25238 :t-ref)
            map__25239 (log/excise-root cluster olookup (deref root_id_ref) request)
            map__25239 (if (seq? map__25239)
                         (if (next map__25239)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25239))
                           (if (seq map__25239) (first map__25239) {}))
                         map__25239)
            ret map__25239
            root_id (get map__25239 :root-id)
            garbage_ids (get map__25239 :garbage-ids)]
        (reset! root_id_ref root_id)
        (queue/put
          writer_queue
          {:type :adopt-tree,
           :completed completed,
           :root-id root_id,
           :t (deref t_ref),
           :garbage-ids garbage_ids}))))
  (defn log-treeifier
    ([_ & p__25241]
      (let [map__25242 p__25241
            map__25242 (if (seq? map__25242)
                         (if (next map__25242)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25242))
                           (if (seq map__25242) (first map__25242) {}))
                         map__25242)
            context map__25242
            olookup (get map__25242 :olookup)
            log (get map__25242 :log)
            log_tree_queue (get map__25242 :log-tree-queue)
            root_id (log/get-root-id log)
            t (:t (log/last-tree-tx olookup root_id))
            context (assoc context :root-id-ref (atom root_id :validator string?) :t-ref (atom t))
            m_25243 {:task :log-treeifier, :event :update/loop}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_25243 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
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
                                                                     [temp__5804__auto__
                                                                      (deref last_new_tail_msg)]
                                                                     (when
                                                                       temp__5804__auto__
                                                                       (let 
                                                                         [msg temp__5804__auto__]
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
                                            (if (= done result) :interrupted (recur)))))
                                      nil)}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_25244 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_25245 (logger/format-as-msec (long elapsed_25244))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_25243 :msec msec_25245 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defonce DatabaseImpl {})
  (defprotocol DatabaseImpl (explicit-request-index [_]))
  (declare ->Database)
  (declare map->Database)
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
          (fn fn__25302
            ([]
              (try
                (do
                  (let [refs (mapv common/async-shutdown backgrounds)]
                    (loop [seq_25303 (seq refs) chunk_25304 nil count_25305 0 i_25306 0]
                      (if (< i_25306 count_25305)
                        (let [r (.nth ^clojure.lang.Indexed chunk_25304 (int i_25306))]
                          (deref r)
                          (recur seq_25303 chunk_25304 count_25305 (inc i_25306)))
                        (let [temp__5804__auto__ (seq seq_25303)]
                          (when temp__5804__auto__
                            (let [seq_25303 temp__5804__auto__]
                              (if (chunked-seq? seq_25303)
                                (let [c__6065__auto__ (chunk-first seq_25303)]
                                  (recur
                                    (chunk-rest seq_25303)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [r (first seq_25303)]
                                  (deref r)
                                  (recur (next seq_25303) nil 0 0)))))))))
                  (cluster/close cluster)
                  true)
                (catch
                  java.lang.Throwable
                  t__8829__auto__
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                          ex t__8829__auto__]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (logger/process "error executing future")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    (monitor/alarm :UnhandledException)
                    (throw ^java.lang.Throwable t__8829__auto__)
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
          (fn fn__25297
            ([p__25296]
              (let [vec__25298 p__25296
                    k (nth vec__25298 (int 0) nil)
                    v (nth vec__25298 (int 1) nil)]
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
  (defn map->Database
    ([m__7972__auto__]
      (Database/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defonce IMaster {})
  (defprotocol
    IMaster
    (remote-ips [_])
    (get-database [_ db-id])
    (internal-start-database [_ db-id peer-prom])
    (internal-stop-database [_ db-id])
    (start-database [_ db-id]))
  (defonce CatalogService {})
  (defprotocol
    CatalogService
    (create-database [_ arg])
    (delete-database [_ arg])
    (rename-database [_ arg]))
  (defn acquire-transactor-semaphore
    ([sem]
      (when-not (.tryAcquire ^java.util.concurrent.Semaphore sem 300 TimeUnit/SECONDS)
        (throw (java.lang.Error. "Transactor is busy"))
        nil)))
  (defn with-semaphore
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
                            (clojure.core/list (vary-meta sem assoc :tag 'Semaphore)))))))))))))))
  (.setMacro #'with-semaphore)
  (defn calculate-basis-t
    ([db]
      (long
        (-> (reduce (fn fn__25458 ([_ d] d)) nil (db/datoms db :aevt [:db/txInstant]))
         (:tx)
         (long)
         (db/eid->eidx)
         (long)))))
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
  (declare ->Master)
  (declare map->Master)
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
        (fn fn__25515
          ([]
            (try
              (do
                (deref (common/async-shutdown session-factory))
                (deref (common/async-shutdown artemis-server)))
              (catch
                java.lang.Throwable
                t__8829__auto__
                (do
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                        ex t__8829__auto__]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "error executing future")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil)
                  (monitor/alarm :UnhandledException)
                  (throw ^java.lang.Throwable t__8829__auto__)
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
                (fn fn__25513
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
                        t__8829__auto__
                        (do
                          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                ex t__8829__auto__]
                            (when (.isWarnEnabled ^org.slf4j.Logger logger)
                              (.warn
                                ^org.slf4j.Logger logger
                                (logger/process "error executing future")
                                ^java.lang.Throwable ex)
                              (logger/caused-by logger ex))
                            nil)
                          (monitor/alarm :UnhandledException)
                          (throw ^java.lang.Throwable t__8829__auto__)
                          nil))))))
              (deref peer_prom 30000 {:failed "database initialization timed out"}))))))
    (internal-start-database
      [this db_id peer_prom]
      (do
        (let [m_25503 {:event :level, :info :transactor/start-db, :db-id db_id}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_25503 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
                                     {:returned
                                      (let [t_now (atom nil)
                                            unprocessed_updates_queue (java.util.concurrent.ArrayBlockingQueue.
                                                                        (int 5))
                                            prefetch_queue (java.util.concurrent.ArrayBlockingQueue.
                                                             (int 5))
                                            map__25507 (prefetch-channels)
                                            map__25507 (if (seq? map__25507)
                                                         (if (next map__25507)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__25507))
                                                           (if
                                                             (seq map__25507)
                                                             (first map__25507)
                                                             {}))
                                                         map__25507)
                                            segments_channel (get map__25507 :segments-channel)
                                            probes_channel (get map__25507 :probes-channel)
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
                                            map__25508 (log/ensure-index-and-log
                                                         cluster
                                                         olookup
                                                         db_id)
                                            map__25508 (if (seq? map__25508)
                                                         (if (next map__25508)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__25508))
                                                           (if
                                                             (seq map__25508)
                                                             (first map__25508)
                                                             {}))
                                                         map__25508)
                                            idxroot (get map__25508 :idxroot)
                                            log (get map__25508 :log)
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
                                            map__25509 (log/catchup
                                                         (db/db
                                                           db_id
                                                           (index/load-index olookup idxroot))
                                                         log)
                                            map__25509 (if (seq? map__25509)
                                                         (if (next map__25509)
                                                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                             (to-array map__25509))
                                                           (if
                                                             (seq map__25509)
                                                             (first map__25509)
                                                             {}))
                                                         map__25509)
                                            db (get map__25509 :db)
                                            size (get map__25509 :size)
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
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_25504 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_25505 (logger/format-as-msec (long elapsed_25504))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_25503 :msec msec_25505 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (throw (:threw result__8585__auto__))))
        :unused))
    (internal-stop-database
      [this db_id]
      (do
        (indexer/remove-database indexer db_id)
        (let [temp__5804__auto__ (.remove ^java.util.Map databases db_id)]
          (when temp__5804__auto__
            (let [database temp__5804__auto__]
              (future-call
                (fn fn__25492
                  ([]
                    (try
                      (let [m_25493 {:event :transactor/shutdown-db, :db-id db_id}
                            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.update")]
                                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                                (.debug
                                                  ^org.slf4j.Logger logger
                                                  (logger/process (assoc m_25493 :phase :begin))))
                                              nil)
                            start__8584__auto__ (java.lang.System/nanoTime)
                            result__8585__auto__ (try
                                                   {:returned
                                                    (deref (common/async-shutdown database))}
                                                   (catch
                                                     java.lang.Throwable
                                                     t__8586__auto__
                                                     {:threw t__8586__auto__}))
                            elapsed_25494 (- (java.lang.System/nanoTime) start__8584__auto__)
                            msec_25495 (logger/format-as-msec (long elapsed_25494))]
                        (let [endmsg__8587__auto__ (merge
                                                     (assoc m_25493 :msec msec_25495 :phase :end)
                                                     (when (:threw result__8585__auto__)
                                                       {:threw
                                                        (class (:threw result__8585__auto__))}))
                              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                            (.debug
                              ^org.slf4j.Logger logger
                              (logger/process endmsg__8587__auto__)))
                          nil)
                        (if (contains? result__8585__auto__ :returned)
                          (:returned result__8585__auto__)
                          (do (throw (:threw result__8585__auto__)) nil)))
                      (catch
                        java.lang.Throwable
                        t__8829__auto__
                        (do
                          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")
                                ex t__8829__auto__]
                            (when (.isWarnEnabled ^org.slf4j.Logger logger)
                              (.warn
                                ^org.slf4j.Logger logger
                                (logger/process "error executing future")
                                ^java.lang.Throwable ex)
                              (logger/caused-by logger ex))
                            nil)
                          (monitor/alarm :UnhandledException)
                          (throw ^java.lang.Throwable t__8829__auto__)
                          nil)))))))))
        :unused))
    (get-database [this db_id] (get databases db_id))
    (remote-ips [this] (aserver/remote-ips artemis-server))
    (rename-database
      [this p__25464]
      (let [map__25491 p__25464
            map__25491 (if (seq? map__25491)
                         (if (next map__25491)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25491))
                           (if (seq map__25491) (first map__25491) {}))
                         map__25491)
            db_name (get map__25491 :db-name)
            new_name (get map__25491 :new-name)]
        (acquire-transactor-semaphore sem)
        (try
          (let [cluster (coord/create-system-cluster system-cluster-conf)]
            (catalog/rename-database cluster db_name new_name))
          (finally (.release ^java.util.concurrent.Semaphore sem)))))
    (delete-database
      [this p__25463]
      (let [map__25490 p__25463
            map__25490 (if (seq? map__25490)
                         (if (next map__25490)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25490))
                           (if (seq map__25490) (first map__25490) {}))
                         map__25490)
            db_name (get map__25490 :db-name)]
        (acquire-transactor-semaphore sem)
        (try
          (let [cluster (coord/create-system-cluster system-cluster-conf)
                result (catalog/delete-database cluster db_name)]
            (let [temp__5804__auto__ (:db-id result)]
              (when temp__5804__auto__
                (let [db_id temp__5804__auto__] (internal-stop-database this db_id))))
            result)
          (finally (.release ^java.util.concurrent.Semaphore sem)))))
    (create-database
      [this p__25462]
      (let [map__25489 p__25462
            map__25489 (if (seq? map__25489)
                         (if (next map__25489)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25489))
                           (if (seq map__25489) (first map__25489) {}))
                         map__25489)
            desc map__25489
            db_name (get map__25489 :db-name)]
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
  (defn map->Master
    ([m__7972__auto__]
      (Master/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn run-admin-command
    ([master p__25544]
      (let [vec__25545 p__25544
            seq__25546 (seq vec__25545)
            first__25547 (first seq__25546)
            seq__25546 (next seq__25546)
            cmd first__25547
            vec__25548 seq__25546
            arg (nth vec__25548 (int 0) nil)
            atype (:type arg)]
        (if (contains? #{nil :cryptf} atype)
          (let [result (let [G__25551 cmd]
                         (case
                           G__25551
                           :rename-database
                           (rename-database master arg)
                           :request-gc
                           (let [map__25552 arg
                                 map__25552 (if (seq? map__25552)
                                              (if (next map__25552)
                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                  (to-array map__25552))
                                                (if (seq map__25552) (first map__25552) {}))
                                              map__25552)
                                 db_id (get map__25552 :db-id)
                                 older_than (get map__25552 :older-than)
                                 temp__5802__auto__ (get-database master db_id)]
                             (if temp__5802__auto__
                               (let [dbase temp__5802__auto__]
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
                           (let [temp__5802__auto__ (get-database master arg)]
                             (if temp__5802__auto__
                               (let [dbase temp__5802__auto__]
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
  (defn compute-index-parallelism
    (^double [p__25556 bytes]
      (.doubleValue
        (let [map__25557 p__25556
              map__25557 (if (seq? map__25557)
                           (if (next map__25557)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__25557))
                             (if (seq map__25557) (first map__25557) {}))
                           map__25557)
              threshold (get map__25557 :threshold)
              redline (get map__25557 :redline)
              max_par (get map__25557 :max-par)]
          (cond
            (< bytes threshold) 0.0
            (>= bytes redline) max_par
            :else (do
                    (java.lang.Double/valueOf
                      (double
                        (* max_par (/ (double (- bytes threshold)) (- redline threshold)))))))))))
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
          (fn fn__25559
            ([_ _ _ usage]
              (let [par (long (max 1 (compute-index-parallelism config (:total usage))))]
                (reset! index/index-parallelism (long par))))))
        (add-watch
          index/index-parallelism
          :datomic.update/index-parallelism-metric
          (fn fn__25561
            ([_ _ old new]
              (when-not (= old new)
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process {:event :update/index-compute-parallelism, :value new})))
                  nil)
                (monitor/add-stat :IndexComputeParallelism new))))))))
  (defn create-master
    ([& args]
      (let [m_25564 {:event :update/create-master}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_25564 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [map__25568 args
                                          map__25568 (if (seq? map__25568)
                                                       (if (next map__25568)
                                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                           (to-array map__25568))
                                                         (if (seq map__25568)
                                                           (first map__25568)
                                                           {}))
                                                       map__25568)
                                          system_cluster_conf (get map__25568 :system-cluster-conf)
                                          olookup_factory (get map__25568 :olookup-factory)
                                          endpoint (get map__25568 :endpoint)
                                          td (get map__25568 :td)
                                          server (let [G__25569 (aserver/start-server
                                                                  endpoint
                                                                  (if
                                                                    (= :limited-edition (:type td))
                                                                    2
                                                                    1000000))]
                                                   (aserver/add-address-settings
                                                     G__25569
                                                     "*.tx-submit"
                                                     :maxSizeBytes
                                                     (long (* 256 1024))
                                                     :addressFullMessagePolicy
                                                     org.apache.activemq.artemis.core.settings.impl.AddressFullMessagePolicy/BLOCK)
                                                   G__25569)
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
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_25565 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_25566 (logger/format-as-msec (long elapsed_25565))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_25564 :msec msec_25566 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.update")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil))))))

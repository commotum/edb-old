(do
  (clojure.core/in-ns 'datomic.process-monitor)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.process-monitor)
    {:doc
     "Samples JVM memory, garbage collection, thread, executor, and process health information and records the corresponding operational metrics."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['datomic.callback :as 'cb]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.domain :as 'domain]
        ['datomic.monitor :as 'monitor]
        ['datomic.process :as 'process]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.process-monitor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.process-monitor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'str]
          ['datomic.callback :as 'cb]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.domain :as 'domain]
          ['datomic.monitor :as 'monitor]
          ['datomic.process :as 'process]
          ['datomic.slf4j :as 'logger]))))
  (.setMeta
    (clojure.lang.RT/var "datomic.process-monitor" "monitored-instances-ref")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.process-monitor" "monitored-instances-ref")
    (delay (atom [(java.lang.Runtime/getRuntime) (domain/system-cache)])))
  (defn convert-nanos-to-millis
    ([snapshot]
      (let [round (fn round
                    ([nanos] (java.lang.Double/valueOf (double (/ (quot nanos 10000) 100.0)))))]
        (reduce-kv
          (fn fn__23323
            ([m k v]
              (if (str/ends-with? (name k) "Nsec")
                (assoc
                  m
                  (keyword (str/replace (name k) "Nsec" "Msec"))
                  (reduce (fn fn__23324 ([v k] (update v k round))) v [:lo :hi :sum]))
                (assoc m k v))))
          {}
          snapshot))))
  (reset-meta!
    #'convert-nanos-to-millis
    (assoc
      {:arglists (clojure.core/list ['snapshot]), :column (int 1)}
      :name
      'convert-nanos-to-millis
      :ns
      *ns*))
  ;; Returns one reporting snapshot. Each metric value is either a number or a statistics map
  ;; containing :lo, :hi, :sum, and :count; callers must accept additional metric names and shapes.
  (defn snapshot-metrics
    ([]
      (let [status_map (mapv
                         (fn fn__23328 ([c] [c (monitor/metrics c)]))
                         (deref (deref monitored-instances-ref)))
            statistic_data (convert-nanos-to-millis (monitor/snapshot-statistics))]
        (apply merge statistic_data (map second status_map)))))
  (reset-meta!
    #'snapshot-metrics
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'snapshot-metrics :ns *ns*))
  ;; Resolves the configured one-argument Clojure function or public static Java method.
  (defn metrics-callback
    ([]
      (let [s (config/property "datomic.metricsCallback")
            m_23331 {:event :metrics/initializing, :metricsCallback s}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.process-monitor")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_23331 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned (some-> s (cb/create-callback))}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_23332 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_23333 (logger/format-as-msec (long elapsed_23332))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_23331 :msec msec_23333 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'metrics-callback
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'metrics-callback :ns *ns*))
  ;; Sends one snapshot to the callback and records success or failure without stopping reporting.
  (defn report-metrics
    ([callback]
      (let [m (snapshot-metrics)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process (assoc m :event :metrics))))
          nil)
        (let [m_23342 {:event :metrics/report}
              ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                             "datomic.process-monitor")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_23342 :phase :begin))))
                                nil)
              start__8553__auto__ (java.lang.System/nanoTime)
              result__8554__auto__ (try
                                     {:returned
                                      (try
                                        (do
                                          (^clojure.lang.IFn callback m)
                                          (monitor/add-stat :MetricsReport 1))
                                        (catch
                                          java.lang.Throwable
                                          t
                                          (do
                                            (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.process-monitor")
                                                  ex t]
                                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                (.info
                                                  ^org.slf4j.Logger logger
                                                  (logger/process "Unable to send metrics")
                                                  ^java.lang.Throwable ex)
                                                (logger/caused-by logger ex))
                                              nil)
                                            (monitor/add-stat :MetricsFail 1))))}
                                     (catch
                                       java.lang.Throwable
                                       t__8555__auto__
                                       {:threw t__8555__auto__}))
              elapsed_23343 (- (java.lang.System/nanoTime) start__8553__auto__)
              msec_23344 (logger/format-as-msec (long elapsed_23343))]
          (let [endmsg__8556__auto__ (merge
                                       (assoc m_23342 :msec msec_23344 :phase :end)
                                       (when (:threw result__8554__auto__)
                                         {:threw (class (:threw result__8554__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
            nil)
          (if (contains? result__8554__auto__ :returned)
            (:returned result__8554__auto__)
            (do (throw (:threw result__8554__auto__)) nil))))))
  (reset-meta!
    #'report-metrics
    (assoc
      {:arglists (clojure.core/list ['callback]), :column (int 1)}
      :name
      'report-metrics
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.process-monitor" "start-metrics-delay")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.process-monitor" "start-metrics-delay")
    (delay
      (let [temp__5804__auto__ (metrics-callback)]
        (when temp__5804__auto__
          (let [callback temp__5804__auto__]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process
                    #:metrics{:started (config/property "datomic.metricsCallback")})))
              nil)
            (process/add-fail-handler
              process/instance
              (fn fn__23355 ([] (monitor/add-stat :SelfDestruct 1) (report-metrics callback))))
            (future-call (fn fn__23357 ([] (report-metrics callback))))
            (common/schedule
              "Datomic Metrics Reporter"
              (partial report-metrics callback)
              60000))))))
  ;; Starts the process-wide reporter once; snapshots are delivered every sixty seconds.
  (defn start-metrics ([] (deref start-metrics-delay)))
  (reset-meta!
    #'start-metrics
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'start-metrics :ns *ns*)))

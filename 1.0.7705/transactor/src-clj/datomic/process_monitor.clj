(do
  (clojure.core/in-ns 'datomic.process-monitor)
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
          (fn fn__22637
            ([m k v]
              (if (str/ends-with? (name k) "Nsec")
                (assoc
                  m
                  (keyword (str/replace (name k) "Nsec" "Msec"))
                  (reduce (fn fn__22638 ([v k] (update v k round))) v [:lo :hi :sum]))
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
  (defn snapshot-metrics
    ([]
      (let [status_map (mapv
                         (fn fn__22642 ([c] [c (monitor/metrics c)]))
                         (deref (deref monitored-instances-ref)))
            statistic_data (convert-nanos-to-millis (monitor/snapshot-statistics))]
        (apply merge statistic_data (map second status_map)))))
  (reset-meta!
    #'snapshot-metrics
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'snapshot-metrics :ns *ns*))
  (defn metrics-callback
    ([]
      (let [s (config/property "datomic.metricsCallback")
            m_22645 {:event :metrics/initializing, :metricsCallback s}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.process-monitor")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22645 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned (some-> s (cb/create-callback))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_22646 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_22647 (logger/format-as-msec (long elapsed_22646))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_22645 :msec msec_22647 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
  (reset-meta!
    #'metrics-callback
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'metrics-callback :ns *ns*))
  (defn report-metrics
    ([callback]
      (let [m (snapshot-metrics)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process (assoc m :event :metrics))))
          nil)
        (let [m_22656 {:event :metrics/report}
              ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                             "datomic.process-monitor")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_22656 :phase :begin))))
                                nil)
              start__8599__auto__ (java.lang.System/nanoTime)
              result__8600__auto__ (try
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
                                       t__8601__auto__
                                       {:threw t__8601__auto__}))
              elapsed_22657 (- (java.lang.System/nanoTime) start__8599__auto__)
              msec_22658 (logger/format-as-msec (long elapsed_22657))]
          (let [endmsg__8602__auto__ (merge
                                       (assoc m_22656 :msec msec_22658 :phase :end)
                                       (when (:threw result__8600__auto__)
                                         {:threw (class (:threw result__8600__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
            nil)
          (if (contains? result__8600__auto__ :returned)
            (:returned result__8600__auto__)
            (do (throw (:threw result__8600__auto__)) nil))))))
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
      (let [temp__5825__auto__ (metrics-callback)]
        (when temp__5825__auto__
          (let [callback temp__5825__auto__]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process
                    #:metrics{:started (config/property "datomic.metricsCallback")})))
              nil)
            (process/add-fail-handler
              process/instance
              (fn fn__22669 ([] (monitor/add-stat :SelfDestruct 1) (report-metrics callback))))
            (future-call (fn fn__22671 ([] (report-metrics callback))))
            (common/schedule
              "Datomic Metrics Reporter"
              (partial report-metrics callback)
              60000))))))
  (defn start-metrics ([] (deref start-metrics-delay)))
  (reset-meta!
    #'start-metrics
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'start-metrics :ns *ns*)))
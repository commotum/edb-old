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
  (def monitored-instances-ref
   (delay (atom [(java.lang.Runtime/getRuntime) (domain/system-cache)])))
  (defn convert-nanos-to-millis
    ([snapshot]
      (let [round (fn round
                    ([nanos] (java.lang.Double/valueOf (double (/ (quot nanos 10000) 100.0)))))]
        (reduce-kv
          (fn fn__23490
            ([m k v]
              (if (str/ends-with? (name k) "Nsec")
                (assoc
                  m
                  (keyword (str/replace (name k) "Nsec" "Msec"))
                  (reduce (fn fn__23491 ([v k] (update v k round))) v [:lo :hi :sum]))
                (assoc m k v))))
          {}
          snapshot))))
  (defn snapshot-metrics
    ([]
      (let [status_map (mapv
                         (fn fn__23495 ([c] [c (monitor/metrics c)]))
                         (deref (deref monitored-instances-ref)))
            statistic_data (convert-nanos-to-millis (monitor/snapshot-statistics))]
        (apply merge statistic_data (map second status_map)))))
  (defn metrics-callback
    ([]
      (let [s (config/property "datomic.metricsCallback")
            m_23498 {:event :metrics/initializing, :metricsCallback s}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.process-monitor")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_23498 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned (some-> s (cb/create-callback))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_23499 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_23500 (logger/format-as-msec (long elapsed_23499))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_23498 :msec msec_23500 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn report-metrics
    ([callback]
      (let [m (snapshot-metrics)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process (assoc m :event :metrics)))
            nil)
          nil)
        (let [m_23509 {:event :metrics/report}
              ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                             "datomic.process-monitor")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_23509 :phase :begin)))
                                  nil)
                                nil)
              start__8981__auto__ (java.lang.System/nanoTime)
              result__8982__auto__ (try
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
                                       t__8983__auto__
                                       {:threw t__8983__auto__}))
              elapsed_23510 (- (java.lang.System/nanoTime) start__8981__auto__)
              msec_23511 (logger/format-as-msec (long elapsed_23510))]
          (let [endmsg__8984__auto__ (merge
                                       (assoc m_23509 :msec msec_23511 :phase :end)
                                       (when (:threw result__8982__auto__)
                                         {:threw (class (:threw result__8982__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
              nil)
            nil)
          (if (contains? result__8982__auto__ :returned)
            (:returned result__8982__auto__)
            (do (throw (:threw result__8982__auto__)) nil))))))
  (def start-metrics-delay
   (delay
     (let [temp__5457__auto__ (metrics-callback)]
       (when temp__5457__auto__
         (let [callback temp__5457__auto__]
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process-monitor")]
             (when (.isInfoEnabled ^org.slf4j.Logger logger)
               (.info
                 ^org.slf4j.Logger logger
                 (logger/process #:metrics{:started (config/property "datomic.metricsCallback")}))
               nil)
             nil)
           (process/add-fail-handler
             process/instance
             (fn fn__23522 ([] (monitor/add-stat :SelfDestruct 1) (report-metrics callback))))
           (future-call (fn fn__23524 ([] (report-metrics callback))))
           (common/schedule
             "Datomic Metrics Reporter"
             (partial report-metrics callback)
             60000))))))
  (reset-meta!
    #'start-metrics-delay
    (assoc {:private true, :column 1} :name 'start-metrics-delay :ns *ns*))
  (defn start-metrics ([] (deref start-metrics-delay))))
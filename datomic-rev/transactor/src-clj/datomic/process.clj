(do
  (clojure.core/in-ns 'datomic.process)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.process 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.process))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (defonce CriticalFailure {})
  (defprotocol CriticalFailure (add-fail-handler [_ h]) (failing? [_]) (fail [_ msg] [_ msg t]))
  (declare ->SharedCriticalFailure)
  (declare map->SharedCriticalFailure)
  (defrecord
    SharedCriticalFailure
    [handlers prom shutdown]
    datomic.process.CriticalFailure
    (fail
      [this msg t]
      (do
        (future-call
          (fn fn__13028
            ([]
              (let [s (str "Terminating process - " msg)]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process") ex t]
                  (when (.isErrorEnabled ^org.slf4j.Logger logger)
                    (.error ^org.slf4j.Logger logger (logger/process s) ex)
                    (logger/caused-by logger ex))
                  nil)
                (.println *err* ^java.lang.String s)
                (.printStackTrace ^java.lang.Throwable t)
                nil))))
        (deref shutdown)))
    (fail
      [this msg]
      (do
        (future-call
          (fn fn__13026
            ([]
              (let [s (str "Terminating process - " msg)]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process")]
                  (when (.isErrorEnabled ^org.slf4j.Logger logger)
                    (.error ^org.slf4j.Logger logger (logger/process s)))
                  nil)
                (.println *err* ^java.lang.String s)
                nil))))
        (deref shutdown)))
    (failing? [this] (realized? prom))
    (add-fail-handler [this h] (swap! handlers conj h)))
  (clojure.core/import 'datomic.process.SharedCriticalFailure)
  (defn ->SharedCriticalFailure
    ([handlers prom shutdown] (datomic.process.SharedCriticalFailure. handlers prom shutdown)))
  (defn map->SharedCriticalFailure
    ([m__7972__auto__]
      (SharedCriticalFailure/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn create-instance
    ([shutdown_time exit?]
      (let [handlers (atom []) prom (promise)]
        (datomic.process.SharedCriticalFailure.
          handlers
          prom
          (delay
            (deliver prom true)
            (loop [seq_13046 (seq (deref handlers)) chunk_13047 nil count_13048 0 i_13049 0]
              (if (< i_13049 count_13048)
                (let [h (.nth ^clojure.lang.Indexed chunk_13047 (int i_13049))]
                  (future-call
                    (fn fn__13050
                      ([]
                        (try
                          (^clojure.lang.IFn h)
                          (catch
                            java.lang.Throwable
                            t__8829__auto__
                            (do
                              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process")
                                    ex t__8829__auto__]
                                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                  (.warn
                                    ^org.slf4j.Logger logger
                                    (logger/process "error executing future")
                                    ^java.lang.Throwable ex)
                                  (logger/caused-by logger ex))
                                nil)
                              (datomic.monitor/alarm :UnhandledException)
                              (throw ^java.lang.Throwable t__8829__auto__)
                              nil))))))
                  (recur seq_13046 chunk_13047 count_13048 (inc i_13049)))
                (let [temp__5804__auto__ (seq seq_13046)]
                  (when temp__5804__auto__
                    (let [seq_13046 temp__5804__auto__]
                      (if (chunked-seq? seq_13046)
                        (let [c__6065__auto__ (chunk-first seq_13046)]
                          (recur
                            (chunk-rest seq_13046)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [h (first seq_13046)]
                          (future-call
                            (fn fn__13052
                              ([]
                                (try
                                  (^clojure.lang.IFn h)
                                  (catch
                                    java.lang.Throwable
                                    t__8829__auto__
                                    (do
                                      (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.process")
                                            ex t__8829__auto__]
                                        (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                          (.warn
                                            ^org.slf4j.Logger logger
                                            (logger/process "error executing future")
                                            ^java.lang.Throwable ex)
                                          (logger/caused-by logger ex))
                                        nil)
                                      (datomic.monitor/alarm :UnhandledException)
                                      (throw ^java.lang.Throwable t__8829__auto__)
                                      nil))))))
                          (recur (next seq_13046) nil 0 0))))))))
            (java.lang.Thread/sleep (long ^java.lang.Number shutdown_time))
            (when exit? (java.lang.System/exit (int -1)))
            :shutdown)))))
  (defonce instance (create-instance 30000 true))
  (defn claim-pid-file
    ([]
      (let [temp__5804__auto__ (config/property "datomic.pidFile")]
        (when temp__5804__auto__
          (let [pid_file temp__5804__auto__] (when (seq pid_file) (spit pid_file logger/pid)))))))
  (defn throw-if-failing!
    ([]
      (when (failing? instance) (throw (java.lang.RuntimeException. "Process is shutting down")))
      nil))
  (defn fail-on-exception
    ([f]
      (fn fn__13064
        ([& args]
          (try
            (apply f args)
            (catch
              java.lang.Throwable
              t
              (do (fail instance "Unhandled exception" t) (throw ^java.lang.Throwable t) nil))))))))
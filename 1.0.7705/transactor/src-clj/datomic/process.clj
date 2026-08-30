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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      CriticalFailure
      (add-fail-handler
        [_ h]
        "Add a function to be called in even of a critical process failure.\n   Critical failure handlers should be idempotent, e.g. via a delay.")
      (failing? [_] "Is process in a critical failure? Once true can never be false.")
      (fail [_ msg] [_ msg t] "Fail the process."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.process" "CriticalFailure")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'CriticalFailure :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'add-fail-handler
                                        {:arglists (clojure.core/list ['_ 'h])}),
                                      :arglists (clojure.core/list ['_ 'h]),
                                      :doc
                                      "Add a function to be called in even of a critical process failure.\n   Critical failure handlers should be idempotent, e.g. via a delay."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.process" "CriticalFailure"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.process" "add-fail-handler")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'failing? {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Is process in a critical failure? Once true can never be false."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.process" "CriticalFailure"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.process" "failing?")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'fail
                                        {:arglists (clojure.core/list ['_ 'msg] ['_ 'msg 't])}),
                                      :arglists (clojure.core/list ['_ 'msg] ['_ 'msg 't]),
                                      :doc "Fail the process."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.process" "CriticalFailure"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.process" "fail")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*))))
  (.setMeta
    (clojure.lang.RT/var "datomic.process" "->SharedCriticalFailure")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.process" "map->SharedCriticalFailure")
    {:declared true, :column (int 1)})
  (defrecord
    SharedCriticalFailure
    [handlers prom shutdown]
    datomic.process.CriticalFailure
    (fail
      [this msg t]
      (do
        (future-call
          (fn fn__10949
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
          (fn fn__10947
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
  (reset-meta!
    #'->SharedCriticalFailure
    (assoc
      {:arglists (clojure.core/list ['handlers 'prom 'shutdown]), :column (int 1)}
      :name
      '->SharedCriticalFailure
      :ns
      *ns*))
  (defn map->SharedCriticalFailure
    ([m__8001__auto__]
      (SharedCriticalFailure/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->SharedCriticalFailure
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->SharedCriticalFailure
      :ns
      *ns*))
  (defn create-instance
    ([shutdown_time exit?]
      (let [handlers (atom []) prom (promise)]
        (datomic.process.SharedCriticalFailure.
          handlers
          prom
          (delay
            (deliver prom true)
            (loop [seq_10967 (seq (deref handlers)) chunk_10968 nil count_10969 0 i_10970 0]
              (if (< i_10970 count_10969)
                (let [h (.nth ^clojure.lang.Indexed chunk_10968 (int i_10970))]
                  (future-call
                    (fn fn__10971
                      ([]
                        (try
                          (^clojure.lang.IFn h)
                          (catch
                            java.lang.Throwable
                            t__8765__auto__
                            (do
                              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.process")
                                    ex t__8765__auto__]
                                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                  (.warn
                                    ^org.slf4j.Logger logger
                                    (logger/process "error executing future")
                                    ^java.lang.Throwable ex)
                                  (logger/caused-by logger ex))
                                nil)
                              (datomic.monitor/alarm :UnhandledException)
                              (throw ^java.lang.Throwable t__8765__auto__)
                              nil))))))
                  (recur seq_10967 chunk_10968 count_10969 (inc i_10970)))
                (let [temp__5825__auto__ (seq seq_10967)]
                  (when temp__5825__auto__
                    (let [seq_10967 temp__5825__auto__]
                      (if (chunked-seq? seq_10967)
                        (let [c__6090__auto__ (chunk-first seq_10967)]
                          (recur
                            (chunk-rest seq_10967)
                            c__6090__auto__
                            (int (count c__6090__auto__))
                            (int 0)))
                        (let [h (first seq_10967)]
                          (future-call
                            (fn fn__10973
                              ([]
                                (try
                                  (^clojure.lang.IFn h)
                                  (catch
                                    java.lang.Throwable
                                    t__8765__auto__
                                    (do
                                      (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.process")
                                            ex t__8765__auto__]
                                        (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                          (.warn
                                            ^org.slf4j.Logger logger
                                            (logger/process "error executing future")
                                            ^java.lang.Throwable ex)
                                          (logger/caused-by logger ex))
                                        nil)
                                      (datomic.monitor/alarm :UnhandledException)
                                      (throw ^java.lang.Throwable t__8765__auto__)
                                      nil))))))
                          (recur (next seq_10967) nil 0 0))))))))
            (java.lang.Thread/sleep (long ^java.lang.Number shutdown_time))
            (when exit? (java.lang.System/exit (int -1)))
            :shutdown)))))
  (reset-meta!
    #'create-instance
    (assoc
      {:arglists (clojure.core/list ['shutdown-time 'exit?]), :column (int 1)}
      :name
      'create-instance
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.process" "instance") {:column (int 1)})
  (let [v__6837__auto__ #'instance]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.process" "instance") {:column (int 1)})
      (.bindRoot (clojure.lang.RT/var "datomic.process" "instance") (create-instance 30000 true))
      #'instance))
  (defn claim-pid-file
    ([]
      (let [temp__5825__auto__ (config/property "datomic.pidFile")]
        (when temp__5825__auto__
          (let [pid_file temp__5825__auto__] (when (seq pid_file) (spit pid_file logger/pid)))))))
  (reset-meta!
    #'claim-pid-file
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'claim-pid-file :ns *ns*))
  (defn throw-if-failing!
    ([]
      (when (failing? instance) (throw (java.lang.RuntimeException. "Process is shutting down")))
      nil))
  (reset-meta!
    #'throw-if-failing!
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'throw-if-failing! :ns *ns*))
  (defn fail-on-exception
    ([f]
      (fn fn__10985
        ([& args]
          (try
            (apply f args)
            (catch
              java.lang.Throwable
              t
              (do (fail instance "Unhandled exception" t) (throw ^java.lang.Throwable t) nil)))))))
  (reset-meta!
    #'fail-on-exception
    (assoc
      {:arglists (clojure.core/list ['f]), :column (int 1)}
      :name
      'fail-on-exception
      :ns
      *ns*)))
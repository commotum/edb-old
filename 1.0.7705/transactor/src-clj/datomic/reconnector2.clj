(do
  (clojure.core/in-ns 'datomic.reconnector2)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.reconnector2)
    {:doc
     "Maintains a reconnectable resource whose endpoint may change. Failed resources are closed, endpoint discovery is retried, and callers retain a stable reference while the active transactor changes."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.common :as 'common] ['datomic.promise :as 'promise])))
  (when-not (.equals 'datomic.reconnector2 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.reconnector2))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.common :as 'common] ['datomic.promise :as 'promise]))))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      Reconnectable
      (reconnect
        [_]
        "Try to reconnect. Idempotent. Async. Calls cleanon on previous state Returns ok."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.reconnector2" "Reconnectable")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Reconnectable :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'reconnect {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Try to reconnect. Idempotent. Async. Calls cleanon on previous state Returns ok."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.reconnector2" "Reconnectable"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.reconnector2" "reconnect")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (.setMeta
    (clojure.lang.RT/var "datomic.reconnector2" "shutdown?")
    {:declared true, :column (int 1)})
  (deftype
    Reconnector
    [current_promise_ref worker_ref shutdown_state reconnect_fn cleanup_fn]
    java.lang.Object
    clojure.lang.IBlockingDeref
    datomic.reconnector2.Reconnectable
    clojure.lang.IDeref
    datomic.common.AsyncShutdown
    (reconnect
      [this]
      (locking worker_ref
       (if (deref worker_ref)
         :ok
         (let [state (deref this)]
           (when-not (= state shutdown_state)
             (future-call
               (fn fn__18598
                 ([]
                   (try
                     (^clojure.lang.IFn cleanup_fn state)
                     (catch
                       java.lang.Throwable
                       t__8765__auto__
                       (do
                         (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.reconnector2")
                               ex t__8765__auto__]
                           (when (.isWarnEnabled ^org.slf4j.Logger logger)
                             (.warn
                               ^org.slf4j.Logger logger
                               (datomic.slf4j/process "error executing future")
                               ^java.lang.Throwable ex)
                             (datomic.slf4j/caused-by logger ex))
                           nil)
                         (datomic.monitor/alarm :UnhandledException)
                         (throw ^java.lang.Throwable t__8765__auto__)
                         nil))))))
             (reset! current_promise_ref (promise/settable-future))
             (reset!
               worker_ref
               (future-call
                 (fn fn__18600
                   ([]
                     (try
                       (let [state (^clojure.lang.IFn reconnect_fn)]
                         (locking worker_ref
                          (do
                            (reset! worker_ref nil)
                            (when-not (realized? (deref current_promise_ref))
                              ((deref current_promise_ref) state)))))
                       (catch
                         java.lang.Throwable
                         t__8765__auto__
                         (do
                           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.reconnector2")
                                 ex t__8765__auto__]
                             (when (.isWarnEnabled ^org.slf4j.Logger logger)
                               (.warn
                                 ^org.slf4j.Logger logger
                                 (datomic.slf4j/process "error executing future")
                                 ^java.lang.Throwable ex)
                               (datomic.slf4j/caused-by logger ex))
                             nil)
                           (datomic.monitor/alarm :UnhandledException)
                           (throw ^java.lang.Throwable t__8765__auto__)
                           nil)))))))
             :ok)))))
    (^java.lang.String toString
      [this]
      (let [obj (deref this 0 :reconnecting)] (str "#<Reconnector: " obj ">")))
    (deref [this ^long msec val] (deref (deref current_promise_ref) (long msec) val))
    (deref [this] (deref (deref current_promise_ref)))
    (async-shutdown
      [this]
      (future-call
        (fn fn__18592
          ([]
            (locking worker_ref
             (do
               (when (deref worker_ref)
                 (future-cancel (deref worker_ref))
                 ((deref current_promise_ref) shutdown_state)
                 (reset! worker_ref nil))
               (let [state (deref this)]
                 (when-not (= state shutdown_state)
                   (future-call
                     (fn fn__18593
                       ([]
                         (try
                           (^clojure.lang.IFn cleanup_fn state)
                           (catch
                             java.lang.Throwable
                             t__8765__auto__
                             (do
                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                              "datomic.reconnector2")
                                     ex t__8765__auto__]
                                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                   (.warn
                                     ^org.slf4j.Logger logger
                                     (datomic.slf4j/process "error executing future")
                                     ^java.lang.Throwable ex)
                                   (datomic.slf4j/caused-by logger ex))
                                 nil)
                               (datomic.monitor/alarm :UnhandledException)
                               (throw ^java.lang.Throwable t__8765__auto__)
                               nil))))))
                   (reset! current_promise_ref (promise/delivered shutdown_state)))))))))))
  (clojure.core/import 'datomic.reconnector2.Reconnector)
  (defn ->Reconnector
    ([current_promise_ref worker_ref shutdown_state reconnect_fn cleanup_fn]
      (datomic.reconnector2.Reconnector.
        current_promise_ref
        worker_ref
        shutdown_state
        reconnect_fn
        cleanup_fn)))
  (reset-meta!
    #'->Reconnector
    (assoc
      {:arglists
       (clojure.core/list
         ['current-promise-ref 'worker-ref 'shutdown-state 'reconnect-fn 'cleanup-fn]),
       :column (int 1)}
      :name
      '->Reconnector
      :ns
      *ns*))
  (defmethod
    print-method
    datomic.reconnector2.Reconnector
    fn__18610
    ([o w] (.write ^java.io.Writer w (.toString o)) nil))
  (defn reconnector-ref
    ([& p__18612]
      (let [map__18613 p__18612
            map__18613 (if (seq? map__18613)
                         (if (next map__18613)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18613))
                           (if (seq map__18613) (first map__18613) {}))
                         map__18613)
            state (get map__18613 :state)
            reconnect (get map__18613 :reconnect)
            cleanup (get map__18613 :cleanup)
            shutdown_state (get map__18613 :shutdown-state)]
        (when-not (and state reconnect cleanup shutdown_state)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str (clojure.core/list 'and 'state 'reconnect 'cleanup 'shutdown-state))))))
        (datomic.reconnector2.Reconnector.
          (atom (promise/delivered state))
          (atom nil)
          shutdown_state
          reconnect
          cleanup))))
  (reset-meta!
    #'reconnector-ref
    (assoc
      {:arglists (clojure.core/list ['& {:keys ['state 'reconnect 'cleanup 'shutdown-state]}]),
       :column (int 1)}
      :name
      'reconnector-ref
      :ns
      *ns*)))

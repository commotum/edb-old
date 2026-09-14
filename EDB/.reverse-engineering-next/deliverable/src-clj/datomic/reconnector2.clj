(do
  (clojure.core/in-ns 'datomic.reconnector2)
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
  (defonce Reconnectable {})
  (defprotocol Reconnectable (reconnect [_]))
  (declare shutdown?)
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
               (fn fn__17134
                 ([]
                   (try
                     (^clojure.lang.IFn cleanup_fn state)
                     (catch
                       java.lang.Throwable
                       t__9147__auto__
                       (do
                         (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.reconnector2")
                               ex t__9147__auto__]
                           (when (.isWarnEnabled ^org.slf4j.Logger logger)
                             (.warn
                               ^org.slf4j.Logger logger
                               (datomic.slf4j/process "error executing future")
                               ^java.lang.Throwable ex)
                             (datomic.slf4j/caused-by logger ex))
                           nil)
                         (datomic.monitor/alarm :UnhandledException)
                         (throw ^java.lang.Throwable t__9147__auto__)
                         nil))))))
             (reset! current_promise_ref (promise/settable-future))
             (reset!
               worker_ref
               (future-call
                 (fn fn__17136
                   ([]
                     (try
                       (let [state (^clojure.lang.IFn reconnect_fn)]
                         (locking worker_ref
                          (reset! worker_ref nil)
                          (when-not (realized? (deref current_promise_ref))
                            ((deref current_promise_ref) state))))
                       (catch
                         java.lang.Throwable
                         t__9147__auto__
                         (do
                           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.reconnector2")
                                 ex t__9147__auto__]
                             (when (.isWarnEnabled ^org.slf4j.Logger logger)
                               (.warn
                                 ^org.slf4j.Logger logger
                                 (datomic.slf4j/process "error executing future")
                                 ^java.lang.Throwable ex)
                               (datomic.slf4j/caused-by logger ex))
                             nil)
                           (datomic.monitor/alarm :UnhandledException)
                           (throw ^java.lang.Throwable t__9147__auto__)
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
        (fn fn__17129
          ([]
            (locking worker_ref
             (when (deref worker_ref)
               (future-cancel (deref worker_ref))
               ((deref current_promise_ref) shutdown_state)
               (reset! worker_ref nil))
             (let [state (deref this)]
               (when-not (= state shutdown_state)
                 (future-call
                   (fn fn__17130
                     ([]
                       (try
                         (^clojure.lang.IFn cleanup_fn state)
                         (catch
                           java.lang.Throwable
                           t__9147__auto__
                           (do
                             (let [logger (org.slf4j.LoggerFactory/getLogger
                                            "datomic.reconnector2")
                                   ex t__9147__auto__]
                               (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                 (.warn
                                   ^org.slf4j.Logger logger
                                   (datomic.slf4j/process "error executing future")
                                   ^java.lang.Throwable ex)
                                 (datomic.slf4j/caused-by logger ex))
                               nil)
                             (datomic.monitor/alarm :UnhandledException)
                             (throw ^java.lang.Throwable t__9147__auto__)
                             nil))))))
                 (reset! current_promise_ref (promise/delivered shutdown_state))))))))))
  (clojure.core/import 'datomic.reconnector2.Reconnector)
  (defn ->Reconnector
    ([current_promise_ref worker_ref shutdown_state reconnect_fn cleanup_fn]
      (datomic.reconnector2.Reconnector.
        current_promise_ref
        worker_ref
        shutdown_state
        reconnect_fn
        cleanup_fn)))
  (defmethod
    print-method
    datomic.reconnector2.Reconnector
    fn__17144
    ([o w] (.write ^java.io.Writer w (.toString o)) nil))
  (defn reconnector-ref
    ([& p__17146]
      (let [map__17147 p__17146
            map__17147 (if (seq? map__17147)
                         (clojure.lang.PersistentHashMap/create (seq map__17147))
                         map__17147)
            state (get map__17147 :state)
            reconnect (get map__17147 :reconnect)
            cleanup (get map__17147 :cleanup)
            shutdown_state (get map__17147 :shutdown-state)]
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
          cleanup)))))
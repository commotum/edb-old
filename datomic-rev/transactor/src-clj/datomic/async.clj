(do
  (clojure.core/in-ns 'datomic.async)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.async 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.async))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (def name-map (atom {}))
  (def binding-conveyor-fn (deref #'clojure.core/binding-conveyor-fn))
  (defn daemon
    ([f base]
      (let [n (get (swap! name-map update base (fnil inc 0)) base)
            name (str base "-" n)
            bound_f (binding-conveyor-fn f)]
        (doto
          (java.lang.Thread.
            (fn fn__20139
              ([]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.async")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process {:event :daemon/thread-started, :name name})))
                  nil)
                (try
                  (^clojure.lang.IFn bound_f)
                  (finally
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.async")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process {:event :daemon/thread-completed, :name name})))
                      nil))))))
          (.setName ^java.lang.String name)
          (.setDaemon (boolean (.booleanValue true)))
          (.start)))))
  (deftype
    DiscardPolicy
    [f]
    java.util.concurrent.RejectedExecutionHandler
    (^void rejectedExecution
      [this ^java.lang.Runnable _ ^java.util.concurrent.ThreadPoolExecutor _]
      (do (^clojure.lang.IFn f) nil)))
  (clojure.core/import 'datomic.async.DiscardPolicy)
  (defn ->DiscardPolicy ([f] (datomic.async.DiscardPolicy. f)))
  (defn discard-policy ([f] (datomic.async.DiscardPolicy. f))))
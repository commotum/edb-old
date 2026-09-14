(do
  (clojure.core/in-ns 'datomic.async)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.async 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.async))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.async" "name-map") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.async" "name-map") (atom {}))
  (.setMeta (clojure.lang.RT/var "datomic.async" "binding-conveyor-fn") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.async" "binding-conveyor-fn")
    (deref #'clojure.core/binding-conveyor-fn))
  (defn daemon
    ([f base]
      (let [n (get (swap! name-map update base (fnil inc 0)) base)
            name (str base "-" n)
            bound_f (binding-conveyor-fn f)]
        (doto
          (java.lang.Thread.
            (fn fn__27662
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
  (reset-meta!
    #'daemon
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'f {:tag 'Runnable}) (.withMeta 'base {:tag 'String})]),
       :column (int 1)}
      :name
      'daemon
      :ns
      *ns*))
  (deftype
    DiscardPolicy
    [f]
    java.util.concurrent.RejectedExecutionHandler
    (^void rejectedExecution
      [this ^java.lang.Runnable _ ^java.util.concurrent.ThreadPoolExecutor _]
      (do (^clojure.lang.IFn f) nil)))
  (clojure.core/import 'datomic.async.DiscardPolicy)
  (defn ->DiscardPolicy ([f] (datomic.async.DiscardPolicy. f)))
  (reset-meta!
    #'->DiscardPolicy
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name '->DiscardPolicy :ns *ns*))
  (defn discard-policy ([f] (datomic.async.DiscardPolicy. f)))
  (reset-meta!
    #'discard-policy
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'discard-policy :ns *ns*)))
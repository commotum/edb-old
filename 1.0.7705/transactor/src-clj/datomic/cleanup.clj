(do
  (clojure.core/in-ns 'datomic.cleanup)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cleanup)
    {:doc
     "Associates cleanup actions with object reachability through phantom references and executes them on a dedicated queue consumer."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.queue :as 'queue] ['datomic.error :as 'error])
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.lang.ref.PhantomReference)
      (clojure.core/import 'java.lang.ref.ReferenceQueue)))
  (when-not (.equals 'datomic.cleanup 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cleanup))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.queue :as 'queue] ['datomic.error :as 'error])
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.lang.ref.PhantomReference)
        (clojure.core/import 'java.lang.ref.ReferenceQueue))))
  (deftype
    Manager
    [phantoms queue]
    datomic.queue.Consumer
    datomic.queue.BlockingConsumer
    (poll-b
      [this or_else msec]
      (let [ref (queue/poll-b queue or_else msec)]
        (if (= ref or_else)
          or_else
          (let [f (.remove ^java.util.concurrent.ConcurrentHashMap phantoms ref)]
            (.clear ^java.lang.ref.Reference ref)
            f))))
    (take
      [this]
      (let [ref (queue/take queue)
            f (.remove ^java.util.concurrent.ConcurrentHashMap phantoms ref)]
        (.clear ^java.lang.ref.Reference ref)
        f))
    (poll-nb
      [this or_else]
      (let [ref (queue/poll-nb queue or_else)]
        (if (= ref or_else)
          or_else
          (let [f (.remove ^java.util.concurrent.ConcurrentHashMap phantoms ref)]
            (.clear ^java.lang.ref.Reference ref)
            f)))))
  (clojure.core/import 'datomic.cleanup.Manager)
  (defn ->Manager ([phantoms queue] (datomic.cleanup.Manager. phantoms queue)))
  (reset-meta!
    #'->Manager
    (assoc
      {:arglists (clojure.core/list ['phantoms 'queue]), :column (int 1)}
      :name
      '->Manager
      :ns
      *ns*))
  (defn register-cleanup
    ([manager object cleanup]
      (.put
        (.-phantoms ^datomic.cleanup.Manager manager)
        (java.lang.ref.PhantomReference. object (.-queue ^datomic.cleanup.Manager manager))
        cleanup)
      object))
  (reset-meta!
    #'register-cleanup
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'manager {:tag 'Manager}) 'object 'cleanup]),
       :column (int 1)}
      :name
      'register-cleanup
      :ns
      *ns*))
  (defn create-manager
    ([]
      (datomic.cleanup.Manager.
        (java.util.concurrent.ConcurrentHashMap.)
        (java.lang.ref.ReferenceQueue.))))
  (reset-meta!
    #'create-manager
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'create-manager :ns *ns*))
  (defn run-queue-loop
    ([q error_handler]
      (loop []
        (let [f (queue/take q)]
          (try
            (^clojure.lang.IFn f)
            (catch java.lang.Throwable t (^clojure.lang.IFn error_handler t)))
          (recur)))
      nil))
  (reset-meta!
    #'run-queue-loop
    (assoc
      {:arglists (clojure.core/list ['q 'error-handler]), :column (int 1)}
      :name
      'run-queue-loop
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cleanup" "shared-manager-ref") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cleanup" "shared-manager-ref")
    (delay
      (let [manager (create-manager)]
        (doto
          (java.lang.Thread.
            (fn fn__18556 ([] (run-queue-loop manager (fn fn__18557 ([t] (error/report t)))))))
          (.setDaemon (boolean (.booleanValue true)))
          (.start))
        manager))))

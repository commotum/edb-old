(do
  (clojure.core/in-ns 'datomic.cleanup)
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
  (defn register-cleanup
    ([manager object cleanup]
      (.put
        (.-phantoms ^datomic.cleanup.Manager manager)
        (java.lang.ref.PhantomReference. object (.-queue ^datomic.cleanup.Manager manager))
        cleanup)
      object))
  (defn create-manager
    ([]
      (datomic.cleanup.Manager.
        (java.util.concurrent.ConcurrentHashMap.)
        (java.lang.ref.ReferenceQueue.))))
  (defn run-queue-loop
    ([q error_handler]
      (loop []
        (let [f (queue/take q)]
          (try
            (^clojure.lang.IFn f)
            (catch java.lang.Throwable t (^clojure.lang.IFn error_handler t)))
          (recur)))
      nil))
  (def shared-manager-ref
   (delay
     (let [manager (create-manager)]
       (doto
         (java.lang.Thread.
           (fn fn__20751 ([] (run-queue-loop manager (fn fn__20752 ([t] (error/report t)))))))
         (.setDaemon (boolean (.booleanValue true)))
         (.start))
       manager))))
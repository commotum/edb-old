(do
  (clojure.core/in-ns 'datomic.log-gc)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.lang.management.ManagementFactory)
      (clojure.core/import 'java.lang.management.PlatformManagedObject)
      (clojure.core/import 'javax.management.NotificationListener)
      (clojure.core/import 'javax.management.openmbean.CompositeData)
      (clojure.core/require
        ['datomic.slf4j :as 'logger]
        ['datomic.monitor :as 'monitor]
        ['clojure.java.jmx :as 'jmx])))
  (when-not (.equals 'datomic.log-gc 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.log-gc))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.lang.management.ManagementFactory)
        (clojure.core/import 'java.lang.management.PlatformManagedObject)
        (clojure.core/import 'javax.management.NotificationListener)
        (clojure.core/import 'javax.management.openmbean.CompositeData)
        (clojure.core/require
          ['datomic.slf4j :as 'logger]
          ['datomic.monitor :as 'monitor]
          ['clojure.java.jmx :as 'jmx]))))
  (set! *warn-on-reflection* true)
  (defn gc-pause?
    ([data] (contains? #{"ZGC Pauses" "G1 Old Generation" "G1 Young Generation"} (:gcName data))))
  (reset-meta!
    #'gc-pause?
    (assoc
      {:private true, :arglists (clojure.core/list ['data]), :column 1}
      :name
      'gc-pause?
      :ns
      *ns*))
  (defn zgc-cycle? ([data] (= (:gcName data) "ZGC Cycles")))
  (reset-meta!
    #'zgc-cycle?
    (assoc
      {:private true, :arglists (clojure.core/list ['data]), :column 1}
      :name
      'zgc-cycle?
      :ns
      *ns*))
  (defn zgc-alloc-stall? ([data] (= (:gcCause data) "Allocation Stall")))
  (reset-meta!
    #'zgc-alloc-stall?
    (assoc
      {:private true, :arglists (clojure.core/list ['data]), :column 1}
      :name
      'zgc-alloc-stall?
      :ns
      *ns*))
  (def listener
   (reify
     javax.management.NotificationListener
     (^void handleNotification
       [this ^javax.management.Notification n _h]
       (do
         (let [data (.getUserData ^javax.management.Notification n)
               data (jmx/objects->data data)
               duration (get-in data [:gcInfo :duration])
               k (select-keys data [:gcName :gcAction :gcCause])]
           (when (gc-pause? data) (monitor/add-stat :GcPauseMsec duration))
           (when (and (zgc-cycle? data) (zgc-alloc-stall? data))
             (monitor/add-stat :ZgcAllocationStallMsec duration))
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log-gc")]
             (when (.isInfoEnabled ^org.slf4j.Logger logger)
               (.info
                 ^org.slf4j.Logger logger
                 (logger/process (assoc k :event :gc :duration duration))))
             nil))
         nil))))
  (reset-meta!
    #'listener
    (assoc {:tag javax.management.NotificationListener, :column 1} :name 'listener :ns *ns*))
  (defn log-gc-events
    ([]
      (loop [seq_25740 (seq (ManagementFactory/getGarbageCollectorMXBeans))
             chunk_25741 nil
             count_25742 0
             i_25743 0]
        (if (< i_25743 count_25742)
          (let [bean (.nth ^clojure.lang.Indexed chunk_25741 (int i_25743))]
            (.addNotificationListener
              jmx/*connection*
              (.getObjectName ^java.lang.management.PlatformManagedObject bean)
              listener
              nil
              :datomic.log-gc/events)
            (recur seq_25740 chunk_25741 count_25742 (inc i_25743)))
          (let [temp__5804__auto__ (seq seq_25740)]
            (when temp__5804__auto__
              (let [seq_25740 temp__5804__auto__]
                (if (chunked-seq? seq_25740)
                  (let [c__6065__auto__ (chunk-first seq_25740)]
                    (recur
                      (chunk-rest seq_25740)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [bean (first seq_25740)]
                    (.addNotificationListener
                      jmx/*connection*
                      (.getObjectName ^java.lang.management.PlatformManagedObject bean)
                      listener
                      nil
                      :datomic.log-gc/events)
                    (recur (next seq_25740) nil 0 0)))))))))))
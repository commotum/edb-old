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
    ([data]
      (contains?
        #{"ZGC Pauses" "G1 Old Generation" "ZGC Major Pauses" "ZGC Minor Pauses"
          "G1 Young Generation"}
        (:gcName data))))
  (reset-meta!
    #'gc-pause?
    (assoc
      {:private true, :arglists (clojure.core/list ['data]), :column (int 1)}
      :name
      'gc-pause?
      :ns
      *ns*))
  (defn alloc-stall? ([data] (= (:gcCause data) "Allocation Stall")))
  (reset-meta!
    #'alloc-stall?
    (assoc
      {:private true, :arglists (clojure.core/list ['data]), :column (int 1)}
      :name
      'alloc-stall?
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.log-gc" "listener")
    {:tag javax.management.NotificationListener, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log-gc" "listener")
    (reify
      javax.management.NotificationListener
      (^void handleNotification
        [this ^javax.management.Notification n _h]
        (do
          (let [data (.getUserData ^javax.management.Notification n)
                data (jmx/objects->data data)
                duration (get-in data [:gcInfo :duration])
                k (select-keys data [:gcName :gcAction :gcCause])]
            (when (gc-pause? data)
              (monitor/add-stat :GcPauseMsec duration)
              (when (alloc-stall? data) (monitor/add-stat :AllocationStallMsec duration)))
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log-gc")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process (assoc k :event :gc :duration duration))))
              nil))
          nil))))
  (defn log-gc-events
    ([]
      (loop [seq_22137 (seq (ManagementFactory/getGarbageCollectorMXBeans))
             chunk_22138 nil
             count_22139 0
             i_22140 0]
        (if (< i_22140 count_22139)
          (let [bean (.nth ^clojure.lang.Indexed chunk_22138 (int i_22140))]
            (.addNotificationListener
              jmx/*connection*
              (.getObjectName ^java.lang.management.PlatformManagedObject bean)
              listener
              nil
              :datomic.log-gc/events)
            (recur seq_22137 chunk_22138 count_22139 (inc i_22140)))
          (let [temp__5825__auto__ (seq seq_22137)]
            (when temp__5825__auto__
              (let [seq_22137 temp__5825__auto__]
                (if (chunked-seq? seq_22137)
                  (let [c__6090__auto__ (chunk-first seq_22137)]
                    (recur
                      (chunk-rest seq_22137)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [bean (first seq_22137)]
                    (.addNotificationListener
                      jmx/*connection*
                      (.getObjectName ^java.lang.management.PlatformManagedObject bean)
                      listener
                      nil
                      :datomic.log-gc/events)
                    (recur (next seq_22137) nil 0 0))))))))))
  (reset-meta!
    #'log-gc-events
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'log-gc-events :ns *ns*)))
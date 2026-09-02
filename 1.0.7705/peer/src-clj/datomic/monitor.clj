(do
  (clojure.core/in-ns 'datomic.monitor)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.monitor)
    {:doc
     "Deployment-agnostic process metrics. Collects counts and bounded statistics, takes reporting snapshots, emits alarms, and dispatches periodic metric maps to a configured callback. Metric maps are open to additional names and value shapes."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.edn :as 'edn])
      (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
      (clojure.core/require ['datomic.math :as 'math])
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.concurrent.atomic.LongAccumulator)
      (clojure.core/import 'java.util.concurrent.atomic.LongAdder)))
  (when-not (.equals 'datomic.monitor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.monitor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.edn :as 'edn])
        (clojure.core/use ['clojure.pprint :only (clojure.core/list 'pprint)])
        (clojure.core/require ['datomic.math :as 'math])
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.concurrent.atomic.LongAccumulator)
        (clojure.core/import 'java.util.concurrent.atomic.LongAdder))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Metrics (metrics [_] "Return a map of metrics information about an object."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.monitor" "Metrics")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Metrics :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'metrics {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Return a map of metrics information about an object."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.monitor" "Metrics"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.monitor" "metrics")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend
    java.lang.Runtime
    Metrics
    {:metrics
     (fn fn__8603
       ([runtime]
         (let [rt (java.lang.Runtime/getRuntime)
               max (.maxMemory ^java.lang.Runtime rt)
               total (.totalMemory ^java.lang.Runtime rt)
               free (.freeMemory ^java.lang.Runtime rt)]
           {:AvailableMB (math/rounded-mb (long (+ free (- max total))))})))})
  (definterface
    StatsUpdate
    (^java.lang.Object addObservation [^java.lang.Object arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.monitor.StatsUpdate)
  (.setMeta (clojure.lang.RT/var "datomic.monitor" "min*") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.monitor" "min*")
    (reify
      java.util.function.LongBinaryOperator
      (^long applyAsLong [this ^long a ^long b] (min a b))))
  (.setMeta (clojure.lang.RT/var "datomic.monitor" "max*") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.monitor" "max*")
    (reify
      java.util.function.LongBinaryOperator
      (^long applyAsLong [this ^long a ^long b] (max a b))))
  (deftype
    Statistics
    [lo hi sum count]
    datomic.monitor.StatsUpdate
    (addObservation
      [this k v]
      (do
        (.accumulate
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap lo k)
            (.computeIfAbsent
              ^java.util.concurrent.ConcurrentHashMap lo
              k
              (reify
                java.util.function.Function
                (apply
                  [this metric]
                  (java.util.concurrent.atomic.LongAccumulator.
                    min*
                    (long java.lang.Long/MAX_VALUE))))))
          (long ^java.lang.Number v))
        (.accumulate
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap hi k)
            (.computeIfAbsent
              ^java.util.concurrent.ConcurrentHashMap hi
              k
              (reify
                java.util.function.Function
                (apply
                  [this metric]
                  (java.util.concurrent.atomic.LongAccumulator.
                    max*
                    (long java.lang.Long/MIN_VALUE))))))
          (long ^java.lang.Number v))
        (.add
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap sum k)
            (.computeIfAbsent
              ^java.util.concurrent.ConcurrentHashMap sum
              k
              (reify
                java.util.function.Function
                (apply [this metric] (java.util.concurrent.atomic.LongAdder.)))))
          (long ^java.lang.Number v))
        (.increment
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap count k)
            (.computeIfAbsent
              ^java.util.concurrent.ConcurrentHashMap count
              k
              (reify
                java.util.function.Function
                (apply [this metric] (java.util.concurrent.atomic.LongAdder.))))))
        nil)))
  (clojure.core/import 'datomic.monitor.Statistics)
  (defn ->Statistics ([lo hi sum count] (datomic.monitor.Statistics. lo hi sum count)))
  (reset-meta!
    #'->Statistics
    (assoc
      {:arglists (clojure.core/list ['lo 'hi 'sum 'count]), :column (int 1)}
      :name
      '->Statistics
      :ns
      *ns*))
  (defn init-stats
    ([]
      (datomic.monitor.Statistics.
        (java.util.concurrent.ConcurrentHashMap.)
        (java.util.concurrent.ConcurrentHashMap.)
        (java.util.concurrent.ConcurrentHashMap.)
        (java.util.concurrent.ConcurrentHashMap.))))
  (reset-meta!
    #'init-stats
    (assoc
      {:private true, :arglists (clojure.core/list []), :column (int 1)}
      :name
      'init-stats
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.monitor" "statistics") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.monitor" "statistics") (atom (init-stats)))
  ;; Atomically rotates the live accumulators and returns the completed reporting interval.
  (defn snapshot-statistics
    ([]
      (let [stats (deref statistics)]
        (reset! statistics (init-stats))
        (merge-with
          merge
          (reduce
            (fn fn__8630
              ([m p__8629]
                (let [vec__8631 p__8629
                      k (nth vec__8631 (int 0) nil)
                      v (nth vec__8631 (int 1) nil)
                      gtrv (.get ^java.util.concurrent.atomic.LongAccumulator v)]
                  (assoc
                    m
                    k
                    {:lo (if (not= (long java.lang.Long/MAX_VALUE) (long gtrv)) (long gtrv) 0)}))))
            {}
            (.-lo ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__8636
              ([m p__8635]
                (let [vec__8637 p__8635
                      k (nth vec__8637 (int 0) nil)
                      v (nth vec__8637 (int 1) nil)
                      gtrv (.get ^java.util.concurrent.atomic.LongAccumulator v)]
                  (assoc
                    m
                    k
                    {:hi (if (not= (long java.lang.Long/MIN_VALUE) (long gtrv)) (long gtrv) 0)}))))
            {}
            (.-hi ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__8642
              ([m p__8641]
                (let [vec__8643 p__8641
                      k (nth vec__8643 (int 0) nil)
                      v (nth vec__8643 (int 1) nil)]
                  (assoc m k {:sum (long (.sum ^java.util.concurrent.atomic.LongAdder v))}))))
            {}
            (.-sum ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__8648
              ([m p__8647]
                (let [vec__8649 p__8647
                      k (nth vec__8649 (int 0) nil)
                      v (nth vec__8649 (int 1) nil)]
                  (assoc m k {:count (long (.sum ^java.util.concurrent.atomic.LongAdder v))}))))
            {}
            (.-count ^datomic.monitor.Statistics stats))))))
  (reset-meta!
    #'snapshot-statistics
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'snapshot-statistics
      :ns
      *ns*))
  ;; Resolves a fully qualified callback symbol named by an EDN system property.
  (defn load-callback
    ([prop-name]
      (let [temp__5804__auto__ (some->
                                 (java.lang.System/getProperty ^java.lang.String prop-name)
                                 (edn/read-string))]
        (when temp__5804__auto__
          (let [s temp__5804__auto__]
            (when (symbol? s)
              (let [temp__5804__auto__ (namespace s)]
                (when temp__5804__auto__
                  (let [ns temp__5804__auto__]
                    (clojure.core/require (symbol ns))
                    (deref (resolve s)))))))))))
  (reset-meta!
    #'load-callback
    (assoc
      {:arglists (clojure.core/list ['prop-name]), :column (int 1)}
      :name
      'load-callback
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.monitor" "metric-event-callback") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.monitor" "metric-event-callback")
    (load-callback "datomic.metricEventCallback"))
  ;; Records one observation and forwards it to the optional metric event callback.
  (defn add-stat
    ([k val]
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " val "\n" (pr-str 'k)))))
      (let [temp__5804__auto__ metric-event-callback]
        (when temp__5804__auto__ (let [cb temp__5804__auto__] (^clojure.lang.IFn cb k val))))
      (.addObservation (deref statistics) k val)))
  (reset-meta!
    #'add-stat
    (assoc {:arglists (clojure.core/list ['k 'val]), :column (int 1)} :name 'add-stat :ns *ns*))
  (defn ns->ms (^double [^long nanos] (/ (quot nanos 10000) 100.0)))
  (reset-meta!
    #'ns->ms
    (assoc
      {:arglists (clojure.core/list (.withMeta [(.withMeta 'nanos {:tag 'long})] {:tag 'double})),
       :column (int 1)}
      :name
      'ns->ms
      :ns
      *ns*))
  ;; Increments the aggregate alarm count and the count for a specific alarm category.
  (defn alarm ([k] (add-stat :Alarm 1) (add-stat (keyword (str "Alarm" (name k))) 1)))
  (reset-meta!
    #'alarm
    (assoc {:arglists (clojure.core/list ['k]), :column (int 1)} :name 'alarm :ns *ns*)))

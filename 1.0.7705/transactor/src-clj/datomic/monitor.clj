(do
  (clojure.core/in-ns 'datomic.monitor)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.monitor)
    {:doc "Deployment-agnostic status functions, requiring only a JVM."})
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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol Metrics (metrics [_] "Return a map of metrics information about an object."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.monitor" "Metrics")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'Metrics :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'metrics {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Return a map of metrics information about an object."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.monitor" "Metrics"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.monitor" "metrics")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (extend
    java.lang.Runtime
    Metrics
    {:metrics
     (fn fn__306
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
              k
              (reify
                java.util.function.Function
                (apply
                  [this metric]
                  (java.util.concurrent.atomic.LongAccumulator.
                    (if (instance? clojure.lang.IFn min*)
                      (instance? java.util.function.LongBinaryOperator min*)
                      min*)
                    (long java.lang.Long/MAX_VALUE))))
              (if (instance?
                    clojure.lang.IFn
                    (reify
                      java.util.function.Function
                      (apply
                        [this metric]
                        (java.util.concurrent.atomic.LongAccumulator.
                          (if (instance? clojure.lang.IFn min*)
                            (instance? java.util.function.LongBinaryOperator min*)
                            min*)
                          (long java.lang.Long/MAX_VALUE)))))
                (instance?
                  java.util.function.Function
                  (reify
                    java.util.function.Function
                    (apply
                      [this metric]
                      (java.util.concurrent.atomic.LongAccumulator.
                        (if (instance? clojure.lang.IFn min*)
                          (instance? java.util.function.LongBinaryOperator min*)
                          min*)
                        (long java.lang.Long/MAX_VALUE)))))
                (reify
                  java.util.function.Function
                  (apply
                    [this metric]
                    (java.util.concurrent.atomic.LongAccumulator.
                      (if (instance? clojure.lang.IFn min*)
                        (instance? java.util.function.LongBinaryOperator min*)
                        min*)
                      (long java.lang.Long/MAX_VALUE)))))))
          (long ^java.lang.Number v))
        (.accumulate
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap hi k)
            (.computeIfAbsent
              k
              (reify
                java.util.function.Function
                (apply
                  [this metric]
                  (java.util.concurrent.atomic.LongAccumulator.
                    (if (instance? clojure.lang.IFn max*)
                      (instance? java.util.function.LongBinaryOperator max*)
                      max*)
                    (long java.lang.Long/MIN_VALUE))))
              (if (instance?
                    clojure.lang.IFn
                    (reify
                      java.util.function.Function
                      (apply
                        [this metric]
                        (java.util.concurrent.atomic.LongAccumulator.
                          (if (instance? clojure.lang.IFn max*)
                            (instance? java.util.function.LongBinaryOperator max*)
                            max*)
                          (long java.lang.Long/MIN_VALUE)))))
                (instance?
                  java.util.function.Function
                  (reify
                    java.util.function.Function
                    (apply
                      [this metric]
                      (java.util.concurrent.atomic.LongAccumulator.
                        (if (instance? clojure.lang.IFn max*)
                          (instance? java.util.function.LongBinaryOperator max*)
                          max*)
                        (long java.lang.Long/MIN_VALUE)))))
                (reify
                  java.util.function.Function
                  (apply
                    [this metric]
                    (java.util.concurrent.atomic.LongAccumulator.
                      (if (instance? clojure.lang.IFn max*)
                        (instance? java.util.function.LongBinaryOperator max*)
                        max*)
                      (long java.lang.Long/MIN_VALUE)))))))
          (long ^java.lang.Number v))
        (.add
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap sum k)
            (.computeIfAbsent
              k
              (reify
                java.util.function.Function
                (apply [this metric] (java.util.concurrent.atomic.LongAdder.)))
              (if (instance?
                    clojure.lang.IFn
                    (reify
                      java.util.function.Function
                      (apply [this metric] (java.util.concurrent.atomic.LongAdder.))))
                (instance?
                  java.util.function.Function
                  (reify
                    java.util.function.Function
                    (apply [this metric] (java.util.concurrent.atomic.LongAdder.))))
                (reify
                  java.util.function.Function
                  (apply [this metric] (java.util.concurrent.atomic.LongAdder.))))))
          (long ^java.lang.Number v))
        (.increment
          (or
            (.get ^java.util.concurrent.ConcurrentHashMap count k)
            (.computeIfAbsent
              k
              (reify
                java.util.function.Function
                (apply [this metric] (java.util.concurrent.atomic.LongAdder.)))
              (if (instance?
                    clojure.lang.IFn
                    (reify
                      java.util.function.Function
                      (apply [this metric] (java.util.concurrent.atomic.LongAdder.))))
                (instance?
                  java.util.function.Function
                  (reify
                    java.util.function.Function
                    (apply [this metric] (java.util.concurrent.atomic.LongAdder.))))
                (reify
                  java.util.function.Function
                  (apply [this metric] (java.util.concurrent.atomic.LongAdder.)))))))
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
  (defn snapshot-statistics
    ([]
      (let [stats (deref statistics)]
        (reset! statistics (init-stats))
        (merge-with
          merge
          (reduce
            (fn fn__333
              ([m p__332]
                (let [vec__334 p__332
                      k (nth vec__334 (int 0) nil)
                      v (nth vec__334 (int 1) nil)
                      gtrv (.get ^java.util.concurrent.atomic.LongAccumulator v)]
                  (assoc
                    m
                    k
                    {:lo (if (not= (long java.lang.Long/MAX_VALUE) (long gtrv)) (long gtrv) 0)}))))
            {}
            (.-lo ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__339
              ([m p__338]
                (let [vec__340 p__338
                      k (nth vec__340 (int 0) nil)
                      v (nth vec__340 (int 1) nil)
                      gtrv (.get ^java.util.concurrent.atomic.LongAccumulator v)]
                  (assoc
                    m
                    k
                    {:hi (if (not= (long java.lang.Long/MIN_VALUE) (long gtrv)) (long gtrv) 0)}))))
            {}
            (.-hi ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__345
              ([m p__344]
                (let [vec__346 p__344 k (nth vec__346 (int 0) nil) v (nth vec__346 (int 1) nil)]
                  (assoc m k {:sum (long (.sum ^java.util.concurrent.atomic.LongAdder v))}))))
            {}
            (.-sum ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__351
              ([m p__350]
                (let [vec__352 p__350 k (nth vec__352 (int 0) nil) v (nth vec__352 (int 1) nil)]
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
  (def load-callback
   (fn load_callback
     ([prop_name]
       (let [temp__5825__auto__ (some->
                                  (java.lang.System/getProperty ^java.lang.String prop_name)
                                  (edn/read-string))]
         (when temp__5825__auto__
           (let [s temp__5825__auto__]
             (when (symbol? s)
               (let [temp__5825__auto__ (namespace s)]
                 (when temp__5825__auto__
                   (let [ns temp__5825__auto__]
                     (clojure.core/require (symbol ns))
                     (deref (resolve s))))))))))))
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
  (defn add-stat
    ([k val]
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " val "\n" (pr-str 'k)))))
      (let [temp__5825__auto__ metric-event-callback]
        (when temp__5825__auto__ (let [cb temp__5825__auto__] (^clojure.lang.IFn cb k val))))
      (.addObservation (deref statistics) k val)))
  (reset-meta!
    #'add-stat
    (assoc {:arglists (clojure.core/list ['k 'val]), :column (int 1)} :name 'add-stat :ns *ns*))
  (def ns->ms (fn ns__GT_ms (^double [^long nanos] (/ (quot nanos 10000) 100.0))))
  (reset-meta!
    #'ns->ms
    (assoc
      {:arglists (clojure.core/list (.withMeta [(.withMeta 'nanos {:tag 'long})] {:tag 'double})),
       :column (int 1)}
      :name
      'ns->ms
      :ns
      *ns*))
  (defn alarm ([k] (add-stat :Alarm 1) (add-stat (keyword (str "Alarm" (name k))) 1)))
  (reset-meta!
    #'alarm
    (assoc {:arglists (clojure.core/list ['k]), :column (int 1)} :name 'alarm :ns *ns*)))
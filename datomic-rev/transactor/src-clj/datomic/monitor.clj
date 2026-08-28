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
  (defonce Metrics {})
  (defprotocol Metrics (metrics [_]))
  (extend
    java.lang.Runtime
    Metrics
    {:metrics
     (fn fn__8634
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
  (def min*
   (reify
     java.util.function.LongBinaryOperator
     (^long applyAsLong [this ^long a ^long b] (min a b))))
  (def max*
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
      {:private true, :arglists (clojure.core/list []), :column 1}
      :name
      'init-stats
      :ns
      *ns*))
  (def statistics (atom (init-stats)))
  (defn snapshot-statistics
    ([]
      (let [stats (deref statistics)]
        (reset! statistics (init-stats))
        (merge-with
          merge
          (reduce
            (fn fn__8661
              ([m p__8660]
                (let [vec__8662 p__8660
                      k (nth vec__8662 (int 0) nil)
                      v (nth vec__8662 (int 1) nil)
                      gtrv (.get ^java.util.concurrent.atomic.LongAccumulator v)]
                  (assoc
                    m
                    k
                    {:lo (if (not= (long java.lang.Long/MAX_VALUE) (long gtrv)) (long gtrv) 0)}))))
            {}
            (.-lo ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__8667
              ([m p__8666]
                (let [vec__8668 p__8666
                      k (nth vec__8668 (int 0) nil)
                      v (nth vec__8668 (int 1) nil)
                      gtrv (.get ^java.util.concurrent.atomic.LongAccumulator v)]
                  (assoc
                    m
                    k
                    {:hi (if (not= (long java.lang.Long/MIN_VALUE) (long gtrv)) (long gtrv) 0)}))))
            {}
            (.-hi ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__8673
              ([m p__8672]
                (let [vec__8674 p__8672
                      k (nth vec__8674 (int 0) nil)
                      v (nth vec__8674 (int 1) nil)]
                  (assoc m k {:sum (long (.sum ^java.util.concurrent.atomic.LongAdder v))}))))
            {}
            (.-sum ^datomic.monitor.Statistics stats))
          (reduce
            (fn fn__8679
              ([m p__8678]
                (let [vec__8680 p__8678
                      k (nth vec__8680 (int 0) nil)
                      v (nth vec__8680 (int 1) nil)]
                  (assoc m k {:count (long (.sum ^java.util.concurrent.atomic.LongAdder v))}))))
            {}
            (.-count ^datomic.monitor.Statistics stats))))))
  (defn load-callback
    ([prop_name]
      (let [temp__5804__auto__ (some->
                                 (java.lang.System/getProperty ^java.lang.String prop_name)
                                 (edn/read-string))]
        (when temp__5804__auto__
          (let [s temp__5804__auto__]
            (when (symbol? s)
              (let [temp__5804__auto__ (namespace s)]
                (when temp__5804__auto__
                  (let [ns temp__5804__auto__]
                    (clojure.core/require (symbol ns))
                    (deref (resolve s)))))))))))
  (def metric-event-callback (load-callback "datomic.metricEventCallback"))
  (defn add-stat
    ([k val]
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " val "\n" (pr-str 'k)))))
      (let [temp__5804__auto__ metric-event-callback]
        (when temp__5804__auto__ (let [cb temp__5804__auto__] (^clojure.lang.IFn cb k val))))
      (.addObservation (deref statistics) k val)))
  (defn ns->ms (^double [^long nanos] (/ (quot nanos 10000) 100.0)))
  (defn alarm ([k] (add-stat :Alarm 1) (add-stat (keyword (str "Alarm" (name k))) 1))))
(do
  (clojure.core/in-ns 'datomic.core2.retry)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.core2.retry 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.retry))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (set! *warn-on-reflection* true)
  (defn retry
    ([f pred retry? calc_backoff p__20988]
      (let [map__20989 p__20988
            map__20989 (if (seq? map__20989)
                         (if (next map__20989)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20989))
                           (if (seq map__20989) (first map__20989) {}))
                         map__20989)
            on_success (get map__20989 :on-success identity)
            on_failure (get map__20989 :on-failure identity)
            start_ms (java.lang.System/currentTimeMillis)]
        (loop [i 0]
          (let [result (^clojure.lang.IFn f)
                end_ms (java.lang.System/currentTimeMillis)
                round_map {:ok? (^clojure.lang.IFn pred result),
                           :i (long i),
                           :result result,
                           :start-ms (long start_ms),
                           :end-ms (long end_ms)}
                map__20990 (merge
                             round_map
                             {:backoff-ms (^clojure.lang.IFn calc_backoff round_map)})
                map__20990 (if (seq? map__20990)
                             (if (next map__20990)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__20990))
                               (if (seq map__20990) (first map__20990) {}))
                             map__20990)
                round_map map__20990
                backoff_ms (get map__20990 :backoff-ms)
                ok? (get map__20990 :ok?)]
            (if ok?
              (do (^clojure.lang.IFn on_success round_map) result)
              (do
                (^clojure.lang.IFn on_failure round_map)
                (if (^clojure.lang.IFn retry? round_map)
                  (do (java.lang.Thread/sleep (long ^java.lang.Number backoff_ms)) (recur (inc i)))
                  result))))))))
  (defn limiting-retry
    ([^long iter p__20992]
      (let [map__20993 p__20992
            map__20993 (if (seq? map__20993)
                         (if (next map__20993)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20993))
                           (if (seq map__20993) (first map__20993) {}))
                         map__20993)
            i (get map__20993 :i)]
        (<= (long ^java.lang.Number i) iter))))
  (defn linear
    ([f pred iter backoff opts]
      (retry f pred (partial limiting-retry iter) (constantly backoff) opts))
    ([f pred iter backoff] (linear f pred iter backoff {})))
  (defn calc-exp-backoff
    (^long [^long backoff ^long base p__20996]
      (let [map__20997 p__20996
            map__20997 (if (seq? map__20997)
                         (if (next map__20997)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20997))
                           (if (seq map__20997) (first map__20997) {}))
                         map__20997)
            i (get map__20997 :i)]
        (long
          (* backoff (java.lang.Math/pow (double (long base)) (double ^java.lang.Number i)))))))
  (defn full-jitter ([i] (rand-int i)))
  (defn exp
    ([f pred iter backoff base opts]
      (retry f pred (partial limiting-retry iter) (partial calc-exp-backoff backoff base) opts))
    ([f pred iter backoff base] (exp f pred iter backoff base {}))))
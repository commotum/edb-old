(do
  (clojure.core/in-ns 'datomic.core2.retry)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.core2.retry 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.retry))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (set! *warn-on-reflection* true)
  (defn retry
    ([f pred retry? calc_backoff p__21845]
      (let [map__21846 p__21845
            map__21846 (if (seq? map__21846)
                         (if (next map__21846)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21846))
                           (if (seq map__21846) (first map__21846) {}))
                         map__21846)
            on_success (get map__21846 :on-success identity)
            on_failure (get map__21846 :on-failure identity)
            start_ms (java.lang.System/currentTimeMillis)]
        (loop [i 0]
          (let [result (^clojure.lang.IFn f)
                end_ms (java.lang.System/currentTimeMillis)
                round_map {:ok? (^clojure.lang.IFn pred result),
                           :i (long i),
                           :result result,
                           :start-ms (long start_ms),
                           :end-ms (long end_ms)}
                map__21847 (merge
                             round_map
                             {:backoff-ms (^clojure.lang.IFn calc_backoff round_map)})
                map__21847 (if (seq? map__21847)
                             (if (next map__21847)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__21847))
                               (if (seq map__21847) (first map__21847) {}))
                             map__21847)
                round_map map__21847
                backoff_ms (get map__21847 :backoff-ms)
                ok? (get map__21847 :ok?)]
            (if ok?
              (do (^clojure.lang.IFn on_success round_map) result)
              (do
                (^clojure.lang.IFn on_failure round_map)
                (if (^clojure.lang.IFn retry? round_map)
                  (do (java.lang.Thread/sleep (long ^java.lang.Number backoff_ms)) (recur (inc i)))
                  result))))))))
  (reset-meta!
    #'retry
    (assoc
      {:arglists
       (clojure.core/list
         ['f
          'pred
          'retry?
          'calc-backoff
          {:keys ['on-success 'on-failure], :or {'on-success 'identity, 'on-failure 'identity}}]),
       :column (int 1)}
      :name
      'retry
      :ns
      *ns*))
  (defn limiting-retry
    ([^long iter p__21849]
      (let [map__21850 p__21849
            map__21850 (if (seq? map__21850)
                         (if (next map__21850)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21850))
                           (if (seq map__21850) (first map__21850) {}))
                         map__21850)
            i (get map__21850 :i)]
        (<= (long ^java.lang.Number i) iter))))
  (reset-meta!
    #'limiting-retry
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'iter {:tag 'long}) {(.withMeta 'i {:tag 'long}) :i}]),
       :column (int 1)}
      :name
      'limiting-retry
      :ns
      *ns*))
  (defn linear
    ([f pred iter backoff opts]
      (retry f pred (partial limiting-retry iter) (constantly backoff) opts))
    ([f pred iter backoff] (linear f pred iter backoff {})))
  (reset-meta!
    #'linear
    (assoc
      {:arglists (clojure.core/list ['f 'pred 'iter 'backoff] ['f 'pred 'iter 'backoff 'opts]),
       :column (int 1)}
      :name
      'linear
      :ns
      *ns*))
  (defn calc-exp-backoff
    (^long [^long backoff ^long base p__21853]
      (let [map__21854 p__21853
            map__21854 (if (seq? map__21854)
                         (if (next map__21854)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21854))
                           (if (seq map__21854) (first map__21854) {}))
                         map__21854)
            i (get map__21854 :i)]
        (long
          (* backoff (java.lang.Math/pow (double (long base)) (double ^java.lang.Number i)))))))
  (reset-meta!
    #'calc-exp-backoff
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'backoff {:tag 'long})
            (.withMeta 'base {:tag 'long})
            {:keys [(.withMeta 'i {:tag 'long})]}]
           {:tag 'long})),
       :column (int 1)}
      :name
      'calc-exp-backoff
      :ns
      *ns*))
  (defn full-jitter ([i] (rand-int i)))
  (reset-meta!
    #'full-jitter
    (assoc {:arglists (clojure.core/list ['i]), :column (int 1)} :name 'full-jitter :ns *ns*))
  (defn exp
    ([f pred iter backoff base opts]
      (retry f pred (partial limiting-retry iter) (partial calc-exp-backoff backoff base) opts))
    ([f pred iter backoff base] (exp f pred iter backoff base {})))
  (reset-meta!
    #'exp
    (assoc
      {:arglists
       (clojure.core/list ['f 'pred 'iter 'backoff 'base] ['f 'pred 'iter 'backoff 'base 'opts]),
       :column (int 1)}
      :name
      'exp
      :ns
      *ns*)))
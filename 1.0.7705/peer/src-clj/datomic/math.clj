(do
  (clojure.core/in-ns 'datomic.math)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.math 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.math))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (def K 1024)
  (reset-meta! #'K (assoc {:const true, :column (int 1)} :name 'K :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.math" "M") {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.math" "M") (long (* 1024 1024)))
  (.setMeta (clojure.lang.RT/var "datomic.math" "G") {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.math" "G") (long (* 1024 1048576)))
  (def SECOND 1000)
  (reset-meta! #'SECOND (assoc {:const true, :column (int 1)} :name 'SECOND :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.math" "MINUTE") {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.math" "MINUTE") (long (* 60 1000)))
  (.setMeta (clojure.lang.RT/var "datomic.math" "HOUR") {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.math" "HOUR") (long (* 60 60000)))
  (.setMeta (clojure.lang.RT/var "datomic.math" "DAY") {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.math" "DAY") (long (* 24 3600000)))
  (defn mean
    ([coll]
      (when (seq coll)
        (java.lang.Double/valueOf (double (/ (double (apply + coll)) (count coll)))))))
  (reset-meta!
    #'mean
    (assoc {:arglists (clojure.core/list ['coll]), :column (int 1)} :name 'mean :ns *ns*))
  (defn mean-and-stddev
    ([coll]
      (when (seq coll)
        (let [m (mean coll) sos (apply + (map (fn fn__8520 ([v] (let [x (- v m)] (* x x)))) coll))]
          {:mean m,
           :stddev
           (java.lang.Double/valueOf
             (double (java.lang.Math/sqrt (double (/ sos (count coll))))))}))))
  (reset-meta!
    #'mean-and-stddev
    (assoc
      {:arglists (clojure.core/list ['coll]), :column (int 1)}
      :name
      'mean-and-stddev
      :ns
      *ns*))
  (defn sla
    ([sorted_coll]
      (loop [percentile 0.9 coll sorted_coll results {}]
        (if (< (count coll) 10)
          (into
            (sorted-map)
            (assoc results (java.lang.Double/valueOf (double percentile)) (last coll)))
          (let [rest (drop (java.lang.Double/valueOf (double (* 0.9 (count coll)))) coll)]
            (recur
              (+ percentile (* 0.9 (- 1 percentile)))
              rest
              (assoc results (java.lang.Double/valueOf (double percentile)) (first rest))))))))
  (reset-meta!
    #'sla
    (assoc {:arglists (clojure.core/list ['sorted-coll]), :column (int 1)} :name 'sla :ns *ns*))
  (defn median
    ([sorted_coll] (when (seq sorted_coll) (nth sorted_coll (int (/ (count sorted_coll) 2))))))
  (reset-meta!
    #'median
    (assoc {:arglists (clojure.core/list ['sorted-coll]), :column (int 1)} :name 'median :ns *ns*))
  (defn round
    ([^double num ^long significant_digits]
      (if (= num 0.0)
        (java.lang.Double/valueOf (double num))
        (let [d (java.lang.Math/ceil
                  (double (java.lang.Math/log10 (double (if (< num 0) (- num) num)))))
              power (- significant_digits d)
              mag (java.lang.Math/pow (double 10) (double power))
              shifted (java.lang.Math/round (double (* num mag)))]
          (java.lang.Double/valueOf (double (/ shifted mag))))))
    ([num] (if (integer? num) num (long (java.lang.Math/round (double num))))))
  (reset-meta!
    #'round
    (assoc
      {:arglists
       (clojure.core/list
         ['num]
         [(.withMeta 'num {:tag 'double}) (.withMeta 'significant-digits {:tag 'long})]),
       :column (int 1)}
      :name
      'round
      :ns
      *ns*))
  (defn rounded-mb ([bytes] (round (/ (double bytes) 1048576) 3)))
  (reset-meta!
    #'rounded-mb
    (assoc {:arglists (clojure.core/list ['bytes]), :column (int 1)} :name 'rounded-mb :ns *ns*))
  (defn create-exponential
    ([p__8527]
      (let [map__8528 p__8527
            map__8528 (if (seq? map__8528)
                        (if (next map__8528)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8528))
                          (if (seq map__8528) (first map__8528) {}))
                        map__8528)
            x1 (get map__8528 :x1)
            x2 (get map__8528 :x2)
            y1 (get map__8528 :y1)
            y2 (get map__8528 :y2)
            b (java.lang.Math/pow (double (/ (float y2) y1)) (double (/ 1.0 (- x2 x1))))
            a (/ y1 (java.lang.Math/pow (double b) (double ^java.lang.Number x1)))]
        (fn fn__8529
          ([x]
            (java.lang.Double/valueOf
              (double (* a (java.lang.Math/pow (double b) (double ^java.lang.Number x))))))))))
  (reset-meta!
    #'create-exponential
    (assoc
      {:arglists (clojure.core/list [{:keys ['x1 'x2 'y1 'y2]}]), :column (int 1)}
      :name
      'create-exponential
      :ns
      *ns*))
  (.setDynamic (clojure.lang.RT/var "datomic.math" "*rnd*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.math" "*rnd*") true)
    {:tag java.util.Random, :dynamic true, :column (int 1)})
  (.bindRoot
    (.setDynamic (clojure.lang.RT/var "datomic.math" "*rnd*") true)
    (java.util.Random. 42))
  (defn uniform
    (^long [lo hi]
      (do
        (when-not (< lo hi)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list '< 'lo 'hi))))))
        (long (java.lang.Math/floor (double (+ lo (* (.nextDouble *rnd*) (- hi lo))))))))
    (^long [] (.nextLong *rnd*)))
  (reset-meta!
    #'uniform
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [] {:tag 'long})
         (.withMeta
           ['lo 'hi]
           {:tag 'long, :pre [(.withMeta (clojure.core/list '< 'lo 'hi) {:column (int 25)})]})),
       :column (int 1)}
      :name
      'uniform
      :ns
      *ns*))
  (defn reservoir-sample
    ([ct coll]
      (loop [result (transient (vec (take ct coll))) n ct coll (drop ct coll)]
        (if (seq coll)
          (let [pos (uniform 0 (inc n))]
            (recur
              (if (< pos ct) (assoc! result (long pos) (first coll)) result)
              (inc n)
              (rest coll)))
          (persistent! result)))))
  (reset-meta!
    #'reservoir-sample
    (assoc
      {:arglists (clojure.core/list ['ct 'coll]), :column (int 1)}
      :name
      'reservoir-sample
      :ns
      *ns*)))
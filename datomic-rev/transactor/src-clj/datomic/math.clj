(do
  (clojure.core/in-ns 'datomic.math)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.math 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.math))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (def K 1024)
  (reset-meta! #'K (assoc {:const true, :column 1} :name 'K :ns *ns*))
  (def M (long (* 1024 1024)))
  (reset-meta! #'M (assoc {:const true, :column 1} :name 'M :ns *ns*))
  (def G (long (* 1024 1048576)))
  (reset-meta! #'G (assoc {:const true, :column 1} :name 'G :ns *ns*))
  (def SECOND 1000)
  (reset-meta! #'SECOND (assoc {:const true, :column 1} :name 'SECOND :ns *ns*))
  (def MINUTE (long (* 60 1000)))
  (reset-meta! #'MINUTE (assoc {:const true, :column 1} :name 'MINUTE :ns *ns*))
  (def HOUR (long (* 60 60000)))
  (reset-meta! #'HOUR (assoc {:const true, :column 1} :name 'HOUR :ns *ns*))
  (def DAY (long (* 24 3600000)))
  (reset-meta! #'DAY (assoc {:const true, :column 1} :name 'DAY :ns *ns*))
  (defn mean
    ([coll]
      (when (seq coll)
        (java.lang.Double/valueOf (double (/ (double (apply + coll)) (count coll)))))))
  (defn mean-and-stddev
    ([coll]
      (when (seq coll)
        (let [m (mean coll) sos (apply + (map (fn fn__8551 ([v] (let [x (- v m)] (* x x)))) coll))]
          {:mean m,
           :stddev
           (java.lang.Double/valueOf
             (double (java.lang.Math/sqrt (double (/ sos (count coll))))))}))))
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
  (defn median
    ([sorted_coll] (when (seq sorted_coll) (nth sorted_coll (int (/ (count sorted_coll) 2))))))
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
  (defn rounded-mb ([bytes] (round (/ (double bytes) 1048576) 3)))
  (defn create-exponential
    ([p__8558]
      (let [map__8559 p__8558
            map__8559 (if (seq? map__8559)
                        (if (next map__8559)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8559))
                          (if (seq map__8559) (first map__8559) {}))
                        map__8559)
            x1 (get map__8559 :x1)
            x2 (get map__8559 :x2)
            y1 (get map__8559 :y1)
            y2 (get map__8559 :y2)
            b (java.lang.Math/pow (double (/ (float y2) y1)) (double (/ 1.0 (- x2 x1))))
            a (/ y1 (java.lang.Math/pow (double b) (double ^java.lang.Number x1)))]
        (fn fn__8560
          ([x]
            (java.lang.Double/valueOf
              (double (* a (java.lang.Math/pow (double b) (double ^java.lang.Number x))))))))))
  (def ^{:dynamic true} *rnd* (java.util.Random. 42))
  (reset-meta!
    #'*rnd*
    (assoc {:tag java.util.Random, :dynamic true, :column 1} :name '*rnd* :ns *ns*))
  (defn uniform
    (^long [lo hi]
      (do
        (when-not (< lo hi)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list '< 'lo 'hi))))))
        (long (java.lang.Math/floor (double (+ lo (* (.nextDouble *rnd*) (- hi lo))))))))
    (^long [] (.nextLong *rnd*)))
  (defn reservoir-sample
    ([ct coll]
      (loop [result (transient (vec (take ct coll))) n ct coll (drop ct coll)]
        (if (seq coll)
          (let [pos (uniform 0 (inc n))]
            (recur
              (if (< pos ct) (assoc! result (long pos) (first coll)) result)
              (inc n)
              (rest coll)))
          (persistent! result))))))
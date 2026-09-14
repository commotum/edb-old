(do
  (clojure.core/in-ns 'datomic.aggregation)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aggregation)
    {:doc
     "Built-in Datalog aggregate functions. Aggregates consume each query group as a Java collection and return a scalar or a bounded collection. Min and max order values with Datomic's cross-type comparator. Median sorts numeric values by their natural order and uses a truncating quotient for an even integer middle pair. Random sampling is performed independently for each group."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer
        'clojure.core
        :exclude
        ['compare 'count 'min 'max 'count 'rand 'distinct])
      (clojure.core/require
        ['datomic.common :refer (clojure.core/list 'compare)]
        ['datomic.math :as 'math])))
  (when-not (.equals 'datomic.aggregation 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aggregation))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer
          'clojure.core
          :exclude
          ['compare 'count 'min 'max 'count 'rand 'distinct])
        (clojure.core/require
          ['datomic.common :refer (clojure.core/list 'compare)]
          ['datomic.math :as 'math]))))
  (set! *warn-on-reflection* true)
  ;; ATOMIC-NOTE [observed]: These functions receive one already-formed query
  ;; ATOMIC-NOTE: group. Scalar min/max reduce without sorting; bounded min/max
  ;; ATOMIC-NOTE: sort the group because they must retain an ordered prefix.
  ;; ATOMIC-NOTE: Both paths use Datomic's cross-type value comparator.
  (defn min
    ([n coll] (vec (take n (sort datomic.common/compare coll))))
    ([coll]
      (reduce
        (fn fn__16452 ([x y] (if (< (datomic.common/compare x y) 0) x y)))
        (first coll)
        coll)))
  (reset-meta!
    #'min
    (assoc
      {:arglists (clojure.core/list ['coll] ['n (.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc
       "Returns the least value in coll, or a vector containing up to n least values. Values are ordered with Datomic's comparator, which defines an order across database value types.",
       :column (int 1)}
      :name
      'min
      :ns
      *ns*))
  (defn max
    ([n coll]
      (vec
        (take
          n
          (sort
            (fn fn__16459
              ([p1__16456# p2__16455#] (long (datomic.common/compare p2__16455# p1__16456#))))
            coll))))
    ([coll]
      (reduce
        (fn fn__16457 ([x y] (if (< (datomic.common/compare x y) 0) y x)))
        (first coll)
        coll)))
  (reset-meta!
    #'max
    (assoc
      {:arglists (clojure.core/list ['coll] ['n (.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc
       "Returns the greatest value in coll, or a vector containing up to n greatest values. Values are ordered with Datomic's comparator, which defines an order across database value types.",
       :column (int 1)}
      :name
      'max
      :ns
      *ns*))
  (defn count ([coll] (java.lang.Integer/valueOf (int (.size ^java.util.Collection coll)))))
  (reset-meta!
    #'count
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the number of values in the aggregate group, including duplicates.",
       :column (int 1)}
      :name
      'count
      :ns
      *ns*))
  (defn count-distinct ([coll] (java.lang.Integer/valueOf (int (clojure.core/count (set coll))))))
  (reset-meta!
    #'count-distinct
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the number of distinct values in the aggregate group.",
       :column (int 1)}
      :name
      'count-distinct
      :ns
      *ns*))
  ;; ATOMIC-NOTE [documented/observed]: distinct returns a set, not an arbitrary
  ;; ATOMIC-NOTE: sequence. This result shape is part of the query API as well as
  ;; ATOMIC-NOTE: the mechanism that removes repeated values inside one group.
  ;; ATOMIC-NOTE [native repair]: QueryValue::Set preserves this public shape
  ;; through typed queries and readable EDN. A Collection with the same members
  ;; is not equivalent: it compares differently and renders as a vector.
  (defn distinct ([coll] (set coll)))
  (reset-meta!
    #'distinct
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the set of distinct values in the aggregate group.",
       :column (int 1)}
      :name
      'distinct
      :ns
      *ns*))
  (defn sum ([coll] (reduce + 0 coll)))
  (reset-meta!
    #'sum
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the numeric sum of the aggregate group. The sum of an empty group is zero.",
       :column (int 1)}
      :name
      'sum
      :ns
      *ns*))
  ;; ATOMIC-NOTE [native numeric adaptation]: Atomic retains exact integer and
  ;; decimal arithmetic until an explicitly approximate statistic requires f64.
  ;; It admits coefficient growth before allocation and centers/rescales exact
  ;; statistical inputs before conversion when necessary. That avoids losing
  ;; nearby large values, but does not claim identical JVM rounding behavior.
  (defn avg
    ([coll] (java.lang.Double/valueOf (double (/ (sum coll) (.size ^java.util.Collection coll))))))
  (reset-meta!
    #'avg
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the arithmetic mean of the numeric values in the aggregate group as a double.",
       :column (int 1)}
      :name
      'avg
      :ns
      *ns*))
  (defn median
    ([coll]
      (let [ls (java.util.ArrayList. ^java.util.Collection coll)]
        (java.util.Collections/sort ^java.util.List ls)
        (let [_ nil cnt (.size ^java.util.ArrayList ls) mid (quot cnt 2)]
          (if (odd? (java.lang.Integer/valueOf (int cnt)))
            (.get ^java.util.ArrayList ls (int mid))
            (quot
              (+
                (.get ^java.util.ArrayList ls (int mid))
                (.get ^java.util.ArrayList ls (int (dec mid))))
              2))))))
  (reset-meta!
    #'median
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc
       "Returns the median numeric value after sorting the aggregate group in natural ascending order. For an even group whose middle values are integers, sums that pair and applies quot by two, truncating toward zero.",
       :column (int 1)}
      :name
      'median
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed]: Variance is population variance: divide the sum of
  ;; ATOMIC-NOTE: squared deviations by the whole group size. stddev is merely
  ;; ATOMIC-NOTE: its square root; neither function applies a sample correction.
  (defn variance
    ([coll]
      (let [av (avg coll)
            xs (map
                 (fn fn__16469
                   ([p1__16468#]
                     (java.lang.Double/valueOf
                       (double (java.lang.Math/pow (double (- p1__16468# av)) (double 2))))))
                 coll)]
        (/ (sum xs) (.size ^java.util.Collection coll)))))
  (reset-meta!
    #'variance
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the population variance of the numeric values in the aggregate group.",
       :column (int 1)}
      :name
      'variance
      :ns
      *ns*))
  (defn stddev
    ([coll] (java.lang.Double/valueOf (double (java.lang.Math/sqrt (double (variance coll)))))))
  (reset-meta!
    #'stddev
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :doc "Returns the population standard deviation of the numeric values in the aggregate group.",
       :column (int 1)}
      :name
      'stddev
      :ns
      *ns*))
  (defn rand
    ([n coll] (repeatedly n (fn fn__16473 ([] (rand coll)))))
    ([coll]
      (.get
        ^java.util.List coll
        (int (rand-int (java.lang.Integer/valueOf (int (.size ^java.util.List coll))))))))
  (reset-meta!
    #'rand
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'coll {:tag 'java.util.List})]
         ['n (.withMeta 'coll {:tag 'java.util.List})]),
       :doc
       "Returns one random value, or a sequence of exactly n random values selected with replacement. Duplicate values may be returned.",
       :column (int 1)}
      :name
      'rand
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed]: Convert to a set before reservoir sampling, so the
  ;; ATOMIC-NOTE: aggregate samples distinct values without replacement. The
  ;; ATOMIC-NOTE: reservoir bounds retained state by n while reading the group.
  (defn sample ([n coll] (math/reservoir-sample n (set coll))))
  (reset-meta!
    #'sample
    (assoc
      {:arglists (clojure.core/list ['n 'coll]),
       :doc "Returns up to n distinct values selected without replacement.",
       :column (int 1)}
      :name
      'sample
      :ns
      *ns*)))

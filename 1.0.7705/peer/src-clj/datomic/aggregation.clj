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
  (defn min
    ([n coll] (vec (take n (sort datomic.common/compare coll))))
    ([coll]
      (reduce
        (fn fn__18917 ([x y] (if (< (datomic.common/compare x y) 0) x y)))
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
            (fn fn__18924
              ([p1__18921# p2__18920#] (long (datomic.common/compare p2__18920# p1__18921#))))
            coll))))
    ([coll]
      (reduce
        (fn fn__18922 ([x y] (if (< (datomic.common/compare x y) 0) y x)))
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
  (defn variance
    ([coll]
      (let [av (avg coll)
            xs (map
                 (fn fn__18934
                   ([p1__18933#]
                     (java.lang.Double/valueOf
                       (double (java.lang.Math/pow (double (- p1__18933# av)) (double 2))))))
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
    ([n coll] (repeatedly n (fn fn__18938 ([] (rand coll)))))
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

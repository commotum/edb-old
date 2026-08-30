(do
  (clojure.core/in-ns 'datomic.aggregation)
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
  (def min
   (fn min
     ([n coll] (vec (take n (sort datomic.common/compare coll))))
     ([coll]
       (reduce
         (fn fn__18917 ([x y] (if (< (datomic.common/compare x y) 0) x y)))
         (first coll)
         coll))))
  (reset-meta!
    #'min
    (assoc
      {:arglists (clojure.core/list ['coll] ['n (.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'min
      :ns
      *ns*))
  (def max
   (fn max
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
         coll))))
  (reset-meta!
    #'max
    (assoc
      {:arglists (clojure.core/list ['coll] ['n (.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'max
      :ns
      *ns*))
  (def count
   (fn count ([coll] (java.lang.Integer/valueOf (int (.size ^java.util.Collection coll))))))
  (reset-meta!
    #'count
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'count
      :ns
      *ns*))
  (def count-distinct
   (fn count_distinct ([coll] (java.lang.Integer/valueOf (int (clojure.core/count (set coll)))))))
  (reset-meta!
    #'count-distinct
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'count-distinct
      :ns
      *ns*))
  (def distinct (fn distinct ([coll] (set coll))))
  (reset-meta!
    #'distinct
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'distinct
      :ns
      *ns*))
  (def sum (fn sum ([coll] (reduce + 0 coll))))
  (reset-meta!
    #'sum
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'sum
      :ns
      *ns*))
  (def avg
   (fn avg
     ([coll]
       (java.lang.Double/valueOf (double (/ (sum coll) (.size ^java.util.Collection coll)))))))
  (reset-meta!
    #'avg
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'avg
      :ns
      *ns*))
  (def median
   (fn median
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
               2)))))))
  (reset-meta!
    #'median
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'median
      :ns
      *ns*))
  (def variance
   (fn variance
     ([coll]
       (let [av (avg coll)
             xs (map
                  (fn fn__18934
                    ([p1__18933#]
                      (java.lang.Double/valueOf
                        (double (java.lang.Math/pow (double (- p1__18933# av)) (double 2))))))
                  coll)]
         (/ (sum xs) (.size ^java.util.Collection coll))))))
  (reset-meta!
    #'variance
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'variance
      :ns
      *ns*))
  (def stddev
   (fn stddev
     ([coll] (java.lang.Double/valueOf (double (java.lang.Math/sqrt (double (variance coll))))))))
  (reset-meta!
    #'stddev
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coll {:tag 'java.util.Collection})]),
       :column (int 1)}
      :name
      'stddev
      :ns
      *ns*))
  (def rand
   (fn rand
     ([n coll] (repeatedly n (fn fn__18938 ([] (rand coll)))))
     ([coll]
       (.get
         ^java.util.List coll
         (int (rand-int (java.lang.Integer/valueOf (int (.size ^java.util.List coll)))))))))
  (reset-meta!
    #'rand
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'coll {:tag 'java.util.List})]
         ['n (.withMeta 'coll {:tag 'java.util.List})]),
       :column (int 1)}
      :name
      'rand
      :ns
      *ns*))
  (defn sample ([n coll] (math/reservoir-sample n (set coll))))
  (reset-meta!
    #'sample
    (assoc {:arglists (clojure.core/list ['n 'coll]), :column (int 1)} :name 'sample :ns *ns*)))
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
       :column (int 1)}
      :name
      'median
      :ns
      *ns*))
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
       :column (int 1)}
      :name
      'rand
      :ns
      *ns*))
  (defn sample ([n coll] (math/reservoir-sample n (set coll))))
  (reset-meta!
    #'sample
    (assoc {:arglists (clojure.core/list ['n 'coll]), :column (int 1)} :name 'sample :ns *ns*)))
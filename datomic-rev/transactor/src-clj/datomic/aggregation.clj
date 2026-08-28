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
        (fn fn__15316 ([x y] (if (< (datomic.common/compare x y) 0) x y)))
        (first coll)
        coll)))
  (defn max
    ([n coll]
      (vec
        (take
          n
          (sort
            (fn fn__15323
              ([p1__15320# p2__15319#] (long (datomic.common/compare p2__15319# p1__15320#))))
            coll))))
    ([coll]
      (reduce
        (fn fn__15321 ([x y] (if (< (datomic.common/compare x y) 0) y x)))
        (first coll)
        coll)))
  (defn count ([coll] (java.lang.Integer/valueOf (int (.size ^java.util.Collection coll)))))
  (defn count-distinct ([coll] (java.lang.Integer/valueOf (int (clojure.core/count (set coll))))))
  (defn distinct ([coll] (set coll)))
  (defn sum ([coll] (reduce + 0 coll)))
  (defn avg
    ([coll] (java.lang.Double/valueOf (double (/ (sum coll) (.size ^java.util.Collection coll))))))
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
  (defn variance
    ([coll]
      (let [av (avg coll)
            xs (map
                 (fn fn__15333
                   ([p1__15332#]
                     (java.lang.Double/valueOf
                       (double (java.lang.Math/pow (double (- p1__15332# av)) (double 2))))))
                 coll)]
        (/ (sum xs) (.size ^java.util.Collection coll)))))
  (defn stddev
    ([coll] (java.lang.Double/valueOf (double (java.lang.Math/sqrt (double (variance coll)))))))
  (defn rand
    ([n coll] (repeatedly n (fn fn__15337 ([] (rand coll)))))
    ([coll]
      (.get
        ^java.util.List coll
        (int (rand-int (java.lang.Integer/valueOf (int (.size ^java.util.List coll))))))))
  (defn sample ([n coll] (math/reservoir-sample n (set coll)))))
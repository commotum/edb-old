(do
  (clojure.core/in-ns 'datomic.core2.algo.search)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.util.Collections)
      (clojure.core/import 'java.util.Comparator)))
  (when-not (.equals 'datomic.core2.algo.search 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.algo.search))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.util.Collections)
        (clojure.core/import 'java.util.Comparator))))
  (defn fn-comparator
    ([f comp]
      (reify
        java.util.Comparator
        (^int compare
          [this o1 o2]
          (.compare
            ^java.util.Comparator comp
            (^clojure.lang.IFn f o1)
            (^clojure.lang.IFn f o2)))))
    ([f]
      (reify
        java.util.Comparator
        (^int compare
          [this o1 o2]
          (.compareTo (^clojure.lang.IFn f o1) (^clojure.lang.IFn f o2))))))
  (defn binary-search
    ([coll k cmp]
      (let [idx (Collections/binarySearch ^java.util.List coll k ^java.util.Comparator cmp)
            idx (if (< idx 0) (long (- (inc idx))) (java.lang.Integer/valueOf (int idx)))]
        (when (< idx (count coll)) idx)))))
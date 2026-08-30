(do
  (clojure.core/in-ns 'datomic.core2.algo.lazy)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.core2.algo.lazy 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.algo.lazy))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (set! *warn-on-reflection* true)
  (defn fully-take-while-delivering-tail
    ([p pred coll]
      (lazy-seq
        (let [temp__5802__auto__ (seq coll)]
          (if temp__5802__auto__
            (let [s temp__5802__auto__ fst (first s) rst (rest s)]
              (if (^clojure.lang.IFn pred fst)
                (cons fst (fully-take-while-delivering-tail p pred rst))
                (do (deliver p s) nil)))
            (do (deliver p nil) nil))))))
  (defn fully-partition-by
    ([f coll]
      (letfn
        [(fpb
           [f pcoll]
           (lazy-seq
             (let [coll (deref pcoll) temp__5804__auto__ (seq coll)]
               (when temp__5804__auto__
                 (let [s temp__5804__auto__
                       fst (first s)
                       fv (^clojure.lang.IFn f fst)
                       pcoll (promise)]
                   (cons
                     (fully-take-while-delivering-tail
                       pcoll
                       (fn fn__19421 ([p1__19418#] (= fv (^clojure.lang.IFn f p1__19418#))))
                       s)
                     (^clojure.lang.IFn fpb f pcoll)))))))]
        (^clojure.lang.IFn fpb f (atom coll)))))
  (defn fred
    ([f ret coll]
      (let [temp__5802__auto__ (seq coll)]
        (if temp__5802__auto__
          (let [xs temp__5802__auto__ x (first xs) rst (rest xs)]
            (recur f (^clojure.lang.IFn f ret x) rst))
          ret)))))
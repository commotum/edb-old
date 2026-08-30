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
        (let [temp__5823__auto__ (seq coll)]
          (if temp__5823__auto__
            (let [s temp__5823__auto__ fst (first s) rst (rest s)]
              (if (^clojure.lang.IFn pred fst)
                (cons fst (fully-take-while-delivering-tail p pred rst))
                (do (deliver p s) nil)))
            (do (deliver p nil) nil))))))
  (reset-meta!
    #'fully-take-while-delivering-tail
    (assoc
      {:arglists (clojure.core/list ['p 'pred 'coll]), :column (int 1)}
      :name
      'fully-take-while-delivering-tail
      :ns
      *ns*))
  (defn fully-partition-by
    ([f coll]
      (letfn
        [(fpb
           [f pcoll]
           (lazy-seq
             (let [coll (deref pcoll) temp__5825__auto__ (seq coll)]
               (when temp__5825__auto__
                 (let [s temp__5825__auto__
                       fst (first s)
                       fv (^clojure.lang.IFn f fst)
                       pcoll (promise)]
                   (cons
                     (fully-take-while-delivering-tail
                       pcoll
                       (fn fn__20162 ([p1__20159#] (= fv (^clojure.lang.IFn f p1__20159#))))
                       s)
                     (^clojure.lang.IFn fpb f pcoll)))))))]
        (^clojure.lang.IFn fpb f (atom coll)))))
  (reset-meta!
    #'fully-partition-by
    (assoc
      {:arglists (clojure.core/list ['f 'coll]), :column (int 1)}
      :name
      'fully-partition-by
      :ns
      *ns*))
  (defn fred
    ([f ret coll]
      (let [temp__5823__auto__ (seq coll)]
        (if temp__5823__auto__
          (let [xs temp__5823__auto__ x (first xs) rst (rest xs)]
            (recur f (^clojure.lang.IFn f ret x) rst))
          ret))))
  (reset-meta!
    #'fred
    (assoc {:arglists (clojure.core/list ['f 'ret 'coll]), :column (int 1)} :name 'fred :ns *ns*)))
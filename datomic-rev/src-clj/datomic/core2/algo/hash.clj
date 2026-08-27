(do
  (clojure.core/in-ns 'datomic.core2.algo.hash)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/import 'java.util.TreeMap)))
  (when-not (.equals 'datomic.core2.algo.hash 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.algo.hash))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/import 'java.util.TreeMap))))
  (set! *warn-on-reflection* true)
  (defn consistent-ring
    ([name_weights key_hashes_fn]
      (let [nmembers (count name_weights) tm (java.util.TreeMap.)]
        (reduce
          (fn fn__19390
            ([tm p__19389]
              (let [vec__19391 p__19389
                    name (nth vec__19391 (int 0) nil)
                    weight (nth vec__19391 (int 1) nil)]
                (loop [seq_19394 (seq (take weight (^clojure.lang.IFn key_hashes_fn name)))
                       chunk_19395 nil
                       count_19396 0
                       i_19397 0]
                  (if (< i_19397 count_19396)
                    (let [h (.nth ^clojure.lang.Indexed chunk_19395 (int i_19397))]
                      (.put ^java.util.TreeMap tm h name)
                      (recur seq_19394 chunk_19395 count_19396 (inc i_19397)))
                    (let [temp__5804__auto__ (seq seq_19394)]
                      (when temp__5804__auto__
                        (let [seq_19394 temp__5804__auto__]
                          (if (chunked-seq? seq_19394)
                            (let [c__6065__auto__ (chunk-first seq_19394)]
                              (recur
                                (chunk-rest seq_19394)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [h (first seq_19394)]
                              (.put ^java.util.TreeMap tm h name)
                              (recur (next seq_19394) nil 0 0))))))))
                tm)))
          tm
          name_weights)
        (fn fn__19401
          ([k]
            (let [vec__19402 (^clojure.lang.IFn key_hashes_fn k) h (nth vec__19402 (int 0) nil)]
              (take
                (java.lang.Integer/valueOf (int nmembers))
                (distinct
                  (concat
                    (vals (.tailMap ^java.util.TreeMap tm h))
                    (vals (.headMap ^java.util.TreeMap tm h)))))))))))
  (defn murmur3-with-dashnum-keys
    ([k]
      (map
        (fn fn__19407
          ([n]
            (java.lang.Integer/valueOf
              (int (clojure.lang.Murmur3/hashUnencodedChars (str k "-" n))))))
        (range)))))
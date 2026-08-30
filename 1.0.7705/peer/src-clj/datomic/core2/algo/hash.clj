(do
  (clojure.core/in-ns 'datomic.core2.algo.hash)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/import 'java.util.TreeMap)))
  (when-not (.equals 'datomic.core2.algo.hash 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.algo.hash))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/import 'java.util.TreeMap))))
  (set! *warn-on-reflection* true)
  (def consistent-ring
   (fn consistent_ring
     ([name_weights key_hashes_fn]
       (let [nmembers (count name_weights) tm (java.util.TreeMap.)]
         (reduce
           (fn fn__20131
             ([tm p__20130]
               (let [vec__20132 p__20130
                     name (nth vec__20132 (int 0) nil)
                     weight (nth vec__20132 (int 1) nil)]
                 (loop [seq_20135 (seq (take weight (^clojure.lang.IFn key_hashes_fn name)))
                        chunk_20136 nil
                        count_20137 0
                        i_20138 0]
                   (if (< i_20138 count_20137)
                     (let [h (.nth ^clojure.lang.Indexed chunk_20136 (int i_20138))]
                       (.put ^java.util.TreeMap tm h name)
                       (recur seq_20135 chunk_20136 count_20137 (inc i_20138)))
                     (let [temp__5825__auto__ (seq seq_20135)]
                       (when temp__5825__auto__
                         (let [seq_20135 temp__5825__auto__]
                           (if (chunked-seq? seq_20135)
                             (let [c__6090__auto__ (chunk-first seq_20135)]
                               (recur
                                 (chunk-rest seq_20135)
                                 c__6090__auto__
                                 (int (count c__6090__auto__))
                                 (int 0)))
                             (let [h (first seq_20135)]
                               (.put ^java.util.TreeMap tm h name)
                               (recur (next seq_20135) nil 0 0))))))))
                 tm)))
           tm
           name_weights)
         (fn fn__20142
           ([k]
             (let [vec__20143 (^clojure.lang.IFn key_hashes_fn k) h (nth vec__20143 (int 0) nil)]
               (take
                 (java.lang.Integer/valueOf (int nmembers))
                 (distinct
                   (concat
                     (vals (.tailMap ^java.util.TreeMap tm h))
                     (vals (.headMap ^java.util.TreeMap tm h))))))))))))
  (reset-meta!
    #'consistent-ring
    (assoc
      {:arglists (clojure.core/list ['name-weights 'key-hashes-fn]), :column (int 1)}
      :name
      'consistent-ring
      :ns
      *ns*))
  (defn murmur3-with-dashnum-keys
    ([k]
      (map
        (fn fn__20148
          ([n]
            (java.lang.Integer/valueOf
              (int (clojure.lang.Murmur3/hashUnencodedChars (str k "-" n))))))
        (range))))
  (reset-meta!
    #'murmur3-with-dashnum-keys
    (assoc
      {:arglists (clojure.core/list ['k]), :column (int 1)}
      :name
      'murmur3-with-dashnum-keys
      :ns
      *ns*)))
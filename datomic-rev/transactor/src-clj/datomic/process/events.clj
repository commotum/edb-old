(do
  (clojure.core/in-ns 'datomic.process.events)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.process.events 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.process.events))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defonce subscribers-ref (atom {}))
  (defn publish
    ([e]
      (let [n (:key e) subscribers (get (deref subscribers-ref) n)]
        (loop [seq_12465 (seq (vals subscribers)) chunk_12466 nil count_12467 0 i_12468 0]
          (if (< i_12468 count_12467)
            (let [f (.nth ^clojure.lang.Indexed chunk_12466 (int i_12468))]
              (^clojure.lang.IFn f e)
              (recur seq_12465 chunk_12466 count_12467 (inc i_12468)))
            (let [temp__5804__auto__ (seq seq_12465)]
              (when temp__5804__auto__
                (let [seq_12465 temp__5804__auto__]
                  (if (chunked-seq? seq_12465)
                    (let [c__6065__auto__ (chunk-first seq_12465)]
                      (recur
                        (chunk-rest seq_12465)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [f (first seq_12465)]
                      (^clojure.lang.IFn f e)
                      (recur (next seq_12465) nil 0 0)))))))))))
  (defn subscribe ([reference key fn] (swap! subscribers-ref update key assoc reference fn)))
  (defn unsubscribe ([reference key] (swap! subscribers-ref update key dissoc reference))))
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
        (loop [seq_10842 (seq (vals subscribers)) chunk_10843 nil count_10844 0 i_10845 0]
          (if (< i_10845 count_10844)
            (let [f (.nth ^clojure.lang.Indexed chunk_10843 (int i_10845))]
              (^clojure.lang.IFn f e)
              (recur seq_10842 chunk_10843 count_10844 (inc i_10845)))
            (let [temp__5457__auto__ (seq seq_10842)]
              (when temp__5457__auto__
                (let [seq_10842 temp__5457__auto__]
                  (if (chunked-seq? seq_10842)
                    (let [c__5719__auto__ (chunk-first seq_10842)]
                      (recur
                        (chunk-rest seq_10842)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [f (first seq_10842)]
                      (^clojure.lang.IFn f e)
                      (recur (next seq_10842) nil 0 0)))))))))))
  (defn subscribe ([reference key fn] (swap! subscribers-ref update key assoc reference fn)))
  (defn unsubscribe ([reference key] (swap! subscribers-ref update key dissoc reference))))
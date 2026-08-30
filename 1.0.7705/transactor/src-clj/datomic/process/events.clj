(do
  (clojure.core/in-ns 'datomic.process.events)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.process.events 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.process.events))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (.setMeta (clojure.lang.RT/var "datomic.process.events" "subscribers-ref") {:column (int 1)})
  (let [v__6837__auto__ #'subscribers-ref]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.process.events" "subscribers-ref") {:column (int 1)})
      (.bindRoot (clojure.lang.RT/var "datomic.process.events" "subscribers-ref") (atom {}))
      #'subscribers-ref))
  (defn publish
    ([e]
      (let [n (:key e) subscribers (get (deref subscribers-ref) n)]
        (loop [seq_10503 (seq (vals subscribers)) chunk_10504 nil count_10505 0 i_10506 0]
          (if (< i_10506 count_10505)
            (let [f (.nth ^clojure.lang.Indexed chunk_10504 (int i_10506))]
              (^clojure.lang.IFn f e)
              (recur seq_10503 chunk_10504 count_10505 (inc i_10506)))
            (let [temp__5825__auto__ (seq seq_10503)]
              (when temp__5825__auto__
                (let [seq_10503 temp__5825__auto__]
                  (if (chunked-seq? seq_10503)
                    (let [c__6090__auto__ (chunk-first seq_10503)]
                      (recur
                        (chunk-rest seq_10503)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [f (first seq_10503)]
                      (^clojure.lang.IFn f e)
                      (recur (next seq_10503) nil 0 0)))))))))))
  (reset-meta!
    #'publish
    (assoc {:arglists (clojure.core/list ['e]), :column (int 1)} :name 'publish :ns *ns*))
  (defn subscribe ([reference key fn] (swap! subscribers-ref update key assoc reference fn)))
  (reset-meta!
    #'subscribe
    (assoc
      {:arglists (clojure.core/list ['reference 'key 'fn]), :column (int 1)}
      :name
      'subscribe
      :ns
      *ns*))
  (defn unsubscribe ([reference key] (swap! subscribers-ref update key dissoc reference)))
  (reset-meta!
    #'unsubscribe
    (assoc
      {:arglists (clojure.core/list ['reference 'key]), :column (int 1)}
      :name
      'unsubscribe
      :ns
      *ns*)))
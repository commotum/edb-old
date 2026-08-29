(do
  (clojure.core/in-ns 'datomic.require)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.require 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.require))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defn maybe-require
    ([& syms]
      (when-not (java.lang.System/getProperty "datomic.disableAllExtensions")
        (loop [seq_9828 (seq syms) chunk_9829 nil count_9830 0 i_9831 0]
          (if (< i_9831 count_9830)
            (let [s (.nth ^clojure.lang.Indexed chunk_9829 (int i_9831))]
              (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
              (recur seq_9828 chunk_9829 count_9830 (inc i_9831)))
            (let [temp__5804__auto__ (seq seq_9828)]
              (when temp__5804__auto__
                (let [seq_9828 temp__5804__auto__]
                  (if (chunked-seq? seq_9828)
                    (let [c__6065__auto__ (chunk-first seq_9828)]
                      (recur
                        (chunk-rest seq_9828)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [s (first seq_9828)]
                      (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
                      (recur (next seq_9828) nil 0 0)))))))))))
  (reset-meta!
    #'maybe-require
    (assoc
      {:arglists (clojure.core/list ['& 'syms]), :column (int 1)}
      :name
      'maybe-require
      :ns
      *ns*))
  (defn require-and-run
    ([sym & args]
      (let [temp__5804__auto__ (namespace sym)]
        (when temp__5804__auto__ (let [ns temp__5804__auto__] (clojure.core/require (symbol ns)))))
      (apply (resolve sym) args)))
  (reset-meta!
    #'require-and-run
    (assoc
      {:arglists (clojure.core/list ['sym '& 'args]), :column (int 1)}
      :name
      'require-and-run
      :ns
      *ns*))
  (defn -main
    ([sname & args]
      (try
        (do (prn (apply require-and-run (symbol sname) args)) (java.lang.System/exit (int 0)) nil)
        (catch
          java.lang.Throwable
          t
          (do (.printStackTrace ^java.lang.Throwable t) (java.lang.System/exit (int -1)) nil)))))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['sname '& 'args]), :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))
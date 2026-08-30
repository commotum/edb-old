(do
  (clojure.core/in-ns 'datomic.require)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.require 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.require))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defn maybe-require
    ([& syms]
      (when-not (java.lang.System/getProperty "datomic.disableAllExtensions")
        (loop [seq_8928 (seq syms) chunk_8929 nil count_8930 0 i_8931 0]
          (if (< i_8931 count_8930)
            (let [s (.nth ^clojure.lang.Indexed chunk_8929 (int i_8931))]
              (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
              (recur seq_8928 chunk_8929 count_8930 (inc i_8931)))
            (let [temp__5804__auto__ (seq seq_8928)]
              (when temp__5804__auto__
                (let [seq_8928 temp__5804__auto__]
                  (if (chunked-seq? seq_8928)
                    (let [c__6065__auto__ (chunk-first seq_8928)]
                      (recur
                        (chunk-rest seq_8928)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [s (first seq_8928)]
                      (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
                      (recur (next seq_8928) nil 0 0)))))))))))
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
(do
  (clojure.core/in-ns 'datomic.require)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.require 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.require))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defn maybe-require
    ([& syms]
      (when-not (java.lang.System/getProperty "datomic.disableAllExtensions")
        (loop [seq_8893 (seq syms) chunk_8894 nil count_8895 0 i_8896 0]
          (if (< i_8896 count_8895)
            (let [s (.nth ^clojure.lang.Indexed chunk_8894 (int i_8896))]
              (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
              (recur seq_8893 chunk_8894 count_8895 (inc i_8896)))
            (let [temp__5825__auto__ (seq seq_8893)]
              (when temp__5825__auto__
                (let [seq_8893 temp__5825__auto__]
                  (if (chunked-seq? seq_8893)
                    (let [c__6090__auto__ (chunk-first seq_8893)]
                      (recur
                        (chunk-rest seq_8893)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [s (first seq_8893)]
                      (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
                      (recur (next seq_8893) nil 0 0)))))))))))
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
      (let [temp__5825__auto__ (namespace sym)]
        (when temp__5825__auto__ (let [ns temp__5825__auto__] (clojure.core/require (symbol ns)))))
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
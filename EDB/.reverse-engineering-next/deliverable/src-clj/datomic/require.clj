(do
  (clojure.core/in-ns 'datomic.require)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.require 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.require))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defn maybe-require
    ([& syms]
      (when-not (java.lang.System/getProperty "datomic.disableAllExtensions")
        (loop [seq_632 (seq syms) chunk_633 nil count_634 0 i_635 0]
          (if (< i_635 count_634)
            (let [s (.nth ^clojure.lang.Indexed chunk_633 (int i_635))]
              (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
              (recur seq_632 chunk_633 count_634 (inc i_635)))
            (let [temp__5457__auto__ (seq seq_632)]
              (when temp__5457__auto__
                (let [seq_632 temp__5457__auto__]
                  (if (chunked-seq? seq_632)
                    (let [c__5719__auto__ (chunk-first seq_632)]
                      (recur
                        (chunk-rest seq_632)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [s (first seq_632)]
                      (try (clojure.core/require s) (catch java.io.FileNotFoundException _ nil))
                      (recur (next seq_632) nil 0 0)))))))))))
  (defn require-and-run
    ([sym & args]
      (let [temp__5457__auto__ (namespace sym)]
        (when temp__5457__auto__ (let [ns temp__5457__auto__] (clojure.core/require (symbol ns)))))
      (apply (resolve sym) args)))
  (defn -main
    ([sname & args]
      (try
        (do (prn (apply require-and-run (symbol sname) args)) (java.lang.System/exit (int 0)) nil)
        (catch
          java.lang.Throwable
          t
          (do (.printStackTrace ^java.lang.Throwable t) (java.lang.System/exit (int -1)) nil))))))
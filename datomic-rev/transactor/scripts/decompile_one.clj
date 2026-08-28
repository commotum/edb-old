(require '[clojure.java.io :as io]
         '[clojure.tools.decompiler :as decompiler])

(let [[input-path output-path class-name] *command-line-args*]
  (when-not (and input-path output-path class-name)
    (binding [*out* *err*]
      (println "usage: decompile_one.clj INPUT_CLASSES OUTPUT_SOURCE INIT_CLASS"))
    (System/exit 2))

  (let [input-root (.getCanonicalPath (io/file input-path))
        output-root (.getCanonicalPath (io/file output-path))]
    (try
      (decompiler/decompile-classfiles
        {:input-path input-root
         :output-path output-root
         :?only-classes [class-name]
         ;; A lenient nested-class failure is emitted as a readable
         ;; "BROKEN DECOMP ..." string.  That is useful for diagnosis but is
         ;; not a recovered namespace, so the canonical runner is strict.
         :lenient? false})
      (catch Throwable failure
        (binding [*out* *err*]
          (println "decompilation failed for" class-name)
          (.printStackTrace failure *err*))
        (System/exit 1)))))

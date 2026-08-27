(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.tools.decompiler :as decompiler])

(let [[input-path output-path report-path] *command-line-args*]
  (when-not (and input-path output-path report-path)
    (binding [*out* *err*]
      (println "usage: decompile_clojure.clj INPUT_CLASSES OUTPUT_SOURCE REPORT_EDN"))
    (System/exit 2))

  (let [input-root (.getCanonicalPath (io/file input-path))
        init-classes
        (->> (file-seq (io/file input-root))
             (filter #(.isFile ^java.io.File %))
             (map #(.getCanonicalPath ^java.io.File %))
             (filter #(str/ends-with? % "__init.class"))
             (map #(decompiler/cname % input-root))
             sort
             vec)
        results
        (mapv
          (fn [class-name]
            (try
              (decompiler/decompile-classfiles
                {:input-path input-root
                 :output-path output-path
                 :?only-classes [class-name]
                 :lenient? true})
              (println "OK" class-name)
              {:class class-name :status :ok}
              (catch Throwable failure
                (binding [*out* *err*]
                  (println "FAIL" class-name "-"
                           (.getName (class failure)) ":" (.getMessage failure)))
                {:class class-name
                 :status :failed
                 :exception (.getName (class failure))
                 :message (.getMessage failure)})))
          init-classes)
        summary {:input input-root
                 :output (.getCanonicalPath (io/file output-path))
                 :namespace-count (count results)
                 :success-count (count (filter #(= :ok (:status %)) results))
                 :failure-count (count (filter #(= :failed (:status %)) results))
                 :results results}]
    (io/make-parents report-path)
    (spit report-path (with-out-str (clojure.pprint/pprint summary)))
    (println "SUMMARY"
             (:success-count summary) "succeeded,"
             (:failure-count summary) "failed")))

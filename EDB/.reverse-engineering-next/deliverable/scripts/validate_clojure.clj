(require '[clojure.java.io :as io])

(let [[root] *command-line-args*]
  (when-not root
    (binding [*out* *err*]
      (println "usage: validate_clojure.clj SOURCE_ROOT"))
    (System/exit 2))

  (let [files (->> (file-seq (io/file root))
                   (filter #(.isFile ^java.io.File %))
                   (filter #(.endsWith (.getName ^java.io.File %) ".clj"))
                   vec)]
    (binding [*read-eval* false]
      (doseq [file files]
        (with-open [reader (java.io.PushbackReader. (io/reader file))]
          (loop []
            (when-not (= ::eof (read {:eof ::eof} reader))
              (recur))))))
    (println "read validation passed for" (count files) "files")))

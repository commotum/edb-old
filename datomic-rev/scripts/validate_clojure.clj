(require '[clojure.java.io :as io])

(let [[root & extra] *command-line-args*]
  (when-not (and root (empty? extra))
    (binding [*out* *err*]
      (println "usage: validate_clojure.clj SOURCE_ROOT"))
    (System/exit 2))

  (let [root-file (io/file root)]
    (when-not (.isDirectory root-file)
      (binding [*out* *err*]
        (println "source root is not a directory:" root))
      (System/exit 2))

    (let [files (->> (file-seq root-file)
                   (filter #(.isFile ^java.io.File %))
                   (filter #(let [name (.getName ^java.io.File %)]
                              (or (.endsWith name ".clj")
                                  (.endsWith name ".cljc"))))
                   (sort-by #(.getPath ^java.io.File %))
                   vec)]
      (when (empty? files)
        (binding [*out* *err*]
          (println "source root contains no .clj or .cljc files:" root))
        (System/exit 1))

      (doseq [file files]
        (try
          (with-open [reader (java.io.PushbackReader. (io/reader file))]
            (let [options (cond-> {:eof ::eof}
                            (.endsWith (.getName ^java.io.File file) ".cljc")
                            (assoc :read-cond :allow :features #{:clj}))]
              (binding [*read-eval* false]
                (loop []
                  (when-not (= ::eof (read options reader))
                    (recur))))))
          (catch Throwable failure
            (throw (ex-info (str "read validation failed for "
                                 (.getPath ^java.io.File file))
                            {:file (.getPath ^java.io.File file)}
                            failure)))))
      (println "read validation passed for" (count files)
               "files (reader-eval disabled)"))))

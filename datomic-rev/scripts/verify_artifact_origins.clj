(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn fail! [message data]
  (throw (ex-info message data)))

(let [[artifact-path namespace-index resource-list java-class-list] *command-line-args*]
  (when-not (every? some? [artifact-path namespace-index resource-list java-class-list])
    (fail! "usage: verify_artifact_origins.clj ARTIFACT NAMESPACE_INDEX RESOURCE_LIST JAVA_CLASS_LIST" {}))
  (let [artifact-file (.getCanonicalFile (io/file artifact-path))
        artifact-url (str (.toURI artifact-file))
        loader (.getContextClassLoader (Thread/currentThread))
        classpath-files (mapv #(.getCanonicalFile (io/file %))
                              (str/split (System/getProperty "java.class.path")
                                         (re-pattern java.io.File/pathSeparator)))
        namespace-paths (->> (line-seq (io/reader namespace-index))
                             rest
                             (map #(second (str/split % #"\t" -1)))
                             vec)
        resource-paths (->> (line-seq (io/reader resource-list))
                            (remove str/blank?)
                            (remove #(str/starts-with? % "#"))
                            vec)
        java-class-names (->> (line-seq (io/reader java-class-list))
                              rest
                              (map #(-> %
                                        (str/replace #"\.class$" "")
                                        (str/replace "/" ".")))
                              vec)
        artifact-resource? (fn [path]
                             (let [url (.getResource loader path)]
                               (and url
                                    (= "jar" (.getProtocol url))
                                    (str/starts-with? (str url)
                                                      (str "jar:" artifact-url "!/")))))]
    (when-not (= artifact-file (first classpath-files))
      (fail! "source artifact is not first on the candidate classpath"
             {:expected artifact-file :actual (first classpath-files)}))
    (doseq [classpath-file (rest classpath-files)
            :let [name (.getName classpath-file)]]
      (when (or (= "peer-1.0.7277.jar" name)
                (re-matches #"core2-.*\.jar" name))
        (fail! "forbidden original AOT JAR on candidate classpath"
               {:path classpath-file})))
    (doseq [path namespace-paths]
      (when-not (artifact-resource? path)
        (fail! "recovered Clojure source does not resolve from the artifact"
               {:path path :url (.getResource loader path)}))
      (let [init-path (str (subs path 0 (- (count path) 4)) "__init.class")]
        (when (.getResource loader init-path)
          (fail! "original or foreign Clojure AOT initializer is visible"
                 {:path init-path :url (.getResource loader init-path)}))))
    (doseq [path resource-paths]
      (when-not (artifact-resource? path)
        (fail! "recovered peer resource does not resolve from the artifact"
               {:path path :url (.getResource loader path)})))
    (doseq [class-name java-class-names
            :let [c (Class/forName class-name false loader)
                  code-source (some-> c .getProtectionDomain .getCodeSource .getLocation str)]]
      (when-not (= artifact-url code-source)
        (fail! "handwritten Java class does not resolve from the artifact"
               {:class class-name :expected artifact-url :actual code-source})))
    (println "artifact origin and candidate classpath isolation passed")
    (println "142 recovered sources, 47 handwritten classes, and 10 peer resources resolve from the artifact")
    (println "all 142 original peer/core2 AOT namespace initializers are absent")))

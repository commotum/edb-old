(require '[clojure.java.io :as io]
         '[clojure.tools.namespace.parse :as ns-parse])

(import '(java.io PushbackReader)
        '(java.nio.file Files Path))

(defn fail!
  [message data]
  (throw (ex-info message data)))

(let [[owner-id source-root source-entry output-dir] *command-line-args*]
  (when-not (= 4 (count *command-line-args*))
    (fail! "expected: namespace source-root source-entry output-dir"
           {:arguments *command-line-args*}))
  (let [source-root-path (.toRealPath (Path/of source-root (make-array String 0))
                                      (make-array java.nio.file.LinkOption 0))
        source-path (.toRealPath (.resolve source-root-path source-entry)
                                 (make-array java.nio.file.LinkOption 0))
        output-path (Path/of output-dir (make-array String 0))]
    (when-not (Files/isRegularFile source-path (make-array java.nio.file.LinkOption 0))
      (fail! "exact source is not a regular file"
             {:owner-id owner-id :source (str source-path)}))
    (Files/createDirectories output-path (make-array java.nio.file.attribute.FileAttribute 0))
    (let [declaration
          (with-open [reader (PushbackReader. (io/reader (.toFile source-path)))]
            (binding [*read-eval* false]
              (ns-parse/read-ns-decl reader)))]
      (when-not declaration
        (fail! "could not read namespace declaration"
               {:owner-id owner-id :source (str source-path)}))
      (let [namespace-symbol (second declaration)
            munged-namespace (munge (str namespace-symbol))]
        (when-not (= owner-id munged-namespace)
          (fail! "munged namespace declaration does not match ownership manifest"
                 {:expected-owner-id owner-id
                  :declared namespace-symbol
                  :munged munged-namespace
                  :source (str source-path)}))
        (let [dependencies (sort (ns-parse/deps-from-ns-decl declaration))
              selected-resource (io/resource source-entry)
              selected-path (when (= "file" (some-> selected-resource .getProtocol))
                              (.toRealPath (Path/of (.toURI selected-resource))
                                           (make-array java.nio.file.LinkOption 0)))]
          (when-not (= source-path selected-path)
            (fail! "candidate source does not win classpath resolution"
                   {:namespace namespace-symbol
                    :expected (str source-path)
                    :selected (some-> selected-resource str)}))
          ;; Normal requires happen while *compile-files* is false. This loads
          ;; source-only dependencies without emitting their classes into the
          ;; target namespace's isolated output tree.
          (doseq [dependency dependencies]
            (require dependency))
          (when (find-ns namespace-symbol)
            (fail! "target namespace was loaded transitively before compilation"
                   {:namespace namespace-symbol
                    :dependencies dependencies}))
          (try
            (binding [*compile-path* (str output-path)
                      *compiler-options* {:elide-meta [:doc :file :line]}
                      *warn-on-reflection* true]
              (compile namespace-symbol))
            (finally
              (shutdown-agents)))
          (prn {:namespace namespace-symbol
                :owner-id owner-id
                :source source-entry
                :dependency-count (count dependencies)
                :dependencies dependencies
                :output (str output-path)}))))))

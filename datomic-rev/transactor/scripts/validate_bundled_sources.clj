(require '[clojure.java.io :as io]
         '[clojure.string :as str])
(import '(java.io ByteArrayInputStream InputStreamReader PushbackReader)
        '(java.nio.charset StandardCharsets)
        '(java.security MessageDigest)
        '(java.util.zip ZipFile))

(defn hex [^bytes bytes]
  (apply str (map #(format "%02x" (bit-and 0xff %)) bytes)))

(defn sha256 [^bytes bytes]
  (hex (.digest (MessageDigest/getInstance "SHA-256") bytes)))

(defn first-form [^bytes bytes]
  (with-open [reader (PushbackReader.
                       (InputStreamReader.
                         (ByteArrayInputStream. bytes)
                         StandardCharsets/UTF_8))]
    (binding [*read-eval* false]
      (read {:read-cond :allow :features #{:clj} :eof ::eof} reader))))

(defn normalized-ns [value]
  (str/replace (str value) "-" "_"))

(let [[mapping-path distribution-root output-path] *command-line-args*
      lines (str/split-lines (slurp mapping-path))
      header (first lines)
      rows (rest lines)]
  (assert (= header
             (str "namespace\tinit_entry\tinit_sha256\tstatus\tmatch_count"
                  "\towner_jars\towner_jar_sha256s\tsource_entries"
                  "\tsource_sha256s")))
  (with-open [writer (io/writer output-path)]
    (.write writer
            (str "namespace\towner_jar\tsource_entry\texpected_sha256"
                 "\tactual_sha256\tdeclared_namespace\tstatus\n"))
    (doseq [line rows]
      (let [[expected-ns _ _ status match-count owner-jar _ source-entry
             expected-sha]
            (str/split line #"\t" -1)]
        (assert (= "single-exact-path" status))
        (assert (= "1" match-count))
        (with-open [zip (ZipFile. (io/file distribution-root owner-jar))]
          (let [entry (.getEntry zip source-entry)
                bytes (with-open [input (.getInputStream zip entry)]
                        (.readAllBytes input))
                actual-sha (sha256 bytes)
                form (first-form bytes)
                declared (when (and (seq? form) (= 'ns (first form)))
                           (second form))
                ok? (and (= expected-sha actual-sha)
                         (= expected-ns (normalized-ns declared)))]
            (.write writer
                    (str/join "\t"
                              [expected-ns owner-jar source-entry
                               expected-sha actual-sha declared
                               (if ok? "pass" "fail")]))
            (.write writer "\n"))))))
  (let [results (rest (str/split-lines (slurp output-path)))
        failures (count (filter #(str/ends-with? % "\tfail") results))]
    (println (str "validated=" (count results)))
    (println (str "failures=" failures))
    (when (pos? failures)
      (System/exit 1))))

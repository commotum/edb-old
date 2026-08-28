(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn fail! [message data]
  (throw (ex-info message data)))

(defn canonical-file [path]
  (.getCanonicalFile (io/file path)))

(defn file-resource-url [root entry]
  (str (.toURI (canonical-file (io/file root entry)))))

(defn jar-resource-url [jar entry]
  (str "jar:" (.toURI (canonical-file jar)) "!/" entry))

(defn resources [loader entry]
  (->> (.getResources loader entry)
       enumeration-seq
       (mapv str)))

(defn tsv-rows [path]
  (->> (line-seq (io/reader path))
       rest
       (remove str/blank?)
       (mapv #(str/split % #"\t" -1))))

(let [[transactor-source-root core2-source-root java-class-root resource-root
       stub-root candidate-sources java-class-list ordered-dependencies
       dependency-duplicates datomic-home] *command-line-args*]
  (when-not (every? some?
                    [transactor-source-root core2-source-root java-class-root
                     resource-root stub-root candidate-sources java-class-list
                     ordered-dependencies dependency-duplicates datomic-home])
    (fail! "usage: verify_structural_origins.clj TRANS_SRC CORE2_SRC JAVA_CLASSES RESOURCES STUBS CANDIDATE_SOURCES JAVA_CLASS_LIST ORDERED_DEPS DUPLICATES DATOMIC_HOME"
           {}))
  (let [loader (.getContextClassLoader (Thread/currentThread))
        classpath (->> (str/split (System/getProperty "java.class.path")
                                  (re-pattern java.io.File/pathSeparator))
                       (mapv canonical-file))
        prefix (mapv canonical-file
                     [transactor-source-root core2-source-root java-class-root
                      resource-root stub-root])
        dependency-rows (tsv-rows ordered-dependencies)
        dependency-files (mapv (fn [[_candidate-ordinal _distribution-ordinal
                                     _jar artifact-path _sha
                                     _source-distribution-path _substitution]]
                                 (canonical-file artifact-path))
                               dependency-rows)
        expected-classpath (into prefix dependency-files)
        source-rows (tsv-rows candidate-sources)
        class-entries (->> (line-seq (io/reader java-class-list))
                           (remove str/blank?)
                           (remove #(str/starts-with? % "#"))
                           vec)
        source-root-for (fn [owner]
                          (case owner
                            "transactor" transactor-source-root
                            "peer-core2" core2-source-root
                            (fail! "unknown candidate source owner" {:owner owner})))]
    (when-not (= expected-classpath classpath)
      (let [first-difference
            (first (keep-indexed (fn [index [expected actual]]
                                   (when-not (= expected actual)
                                     {:index index :expected expected :actual actual}))
                                 (map vector
                                      (concat expected-classpath (repeat nil))
                                      (concat classpath (repeat nil)))))]
        (fail! "candidate classpath does not match the frozen candidate-first order"
               {:expected-count (count expected-classpath)
                :actual-count (count classpath)
                :first-difference first-difference})))

    (when-not (= 532 (count dependency-files))
      (fail! "candidate dependency boundary is not exactly 532 JARs"
             {:count (count dependency-files)}))
    (when-not (= 1 (count (filter #(= "sanitized-nano-impl" (nth % 6))
                                  dependency-rows)))
      (fail! "candidate classpath does not contain exactly one sanitized nano derivative"
             {}))
    (doseq [file classpath
            :let [name (.getName file)]]
      (when (or (= name "peer-1.0.7277.jar")
                (= name "datomic-transactor-pro-1.0.7277.jar")
                (= name "nano-impl-0.1.325.jar")
                (re-matches #"core2-.*[.]jar" name))
        (fail! "forbidden licensed/AOT or unsanitized artifact is on candidate classpath"
               {:path file})))

    (when-not (= 272 (count source-rows))
      (fail! "candidate source manifest is not exactly 272 rows"
             {:count (count source-rows)}))
    (doseq [[owner namespace entry _sha] source-rows
            :let [urls (resources loader entry)
                  expected (file-resource-url (source-root-for owner) entry)]]
      (when-not (= expected (first urls))
        (fail! "candidate source is not the first-resolved resource"
               {:namespace namespace :entry entry :expected expected :actual urls}))
      (let [extension (cond
                        (str/ends-with? entry ".cljc") ".cljc"
                        (str/ends-with? entry ".clj") ".clj"
                        :else (fail! "unexpected candidate source extension"
                                     {:entry entry}))
            init-entry (str (subs entry 0 (- (count entry) (count extension)))
                            "__init.class")
            init-urls (resources loader init-entry)]
        (when (seq init-urls)
          (fail! "original or dependency AOT initializer is visible"
                 {:namespace namespace :entry init-entry :urls init-urls}))))

    (when-not (= 52 (count class-entries))
      (fail! "Transactor Java class list is not exactly 52 entries"
             {:count (count class-entries)}))
    (doseq [entry class-entries
            :let [class-name (-> entry
                                 (str/replace #"[.]class$" "")
                                 (str/replace "/" "."))
                  class-object (Class/forName class-name false loader)
                  code-source (some-> class-object .getProtectionDomain
                                      .getCodeSource .getLocation str)
                  expected (str (.toURI (canonical-file java-class-root)))]]
      (when-not (= expected code-source)
        (fail! "Transactor Java class is not candidate-owned"
               {:class class-name :expected expected :actual code-source})))

    (doseq [entry ["org/infinispan/client/hotrod/Flag.class"
                   "org/infinispan/client/hotrod/RemoteCache.class"
                   "org/infinispan/client/hotrod/RemoteCacheManager.class"
                   "org/infinispan/client/hotrod/VersionedValue.class"]
            :let [urls (resources loader entry)
                  expected (file-resource-url stub-root entry)]]
      (when-not (= [expected] urls)
        (fail! "compile/load-only Hot Rod stub has an unexpected origin"
               {:entry entry :expected expected :actual urls})))

    (doseq [entry ["data_readers.clj"
                   "datomic/VERSION"
                   "datomic/aws/instance-arch.edn"
                   "datomic/aws/region-arch-ami.edn"]
            :let [urls (resources loader entry)
                  expected (file-resource-url resource-root entry)]]
      (when-not (= [expected] urls)
        (fail! "reconstructed runtime resource is not candidate-owned"
               {:entry entry :expected expected :actual urls})))
    (let [resource-text (fn [entry]
                          (let [urls (resources loader entry)]
                            (when-not (= 1 (count urls))
                              (fail! "candidate runtime resource is not unique"
                                     {:entry entry :urls urls}))
                            (slurp (first urls))))
          readers (edn/read-string (resource-text "data_readers.clj"))
          expected-readers '{db/id datomic.db/id-literal
                             db/fn datomic.function/construct
                             base64 datomic.codec/base-64-literal}
          version (resource-text "datomic/VERSION")
          instance-arch (edn/read-string
                         (resource-text "datomic/aws/instance-arch.edn"))
          region-arch (edn/read-string
                       (resource-text "datomic/aws/region-arch-ami.edn"))]
      (when-not (= expected-readers readers)
        (fail! "candidate tagged-literal registrations changed"
               {:expected expected-readers :actual readers}))
      (when-not (= "1.0.7277\n" version)
        (fail! "candidate compatibility VERSION changed"
               {:expected "1.0.7277\\n" :actual (pr-str version)}))
      (when-not (= {} instance-arch region-arch)
        (fail! "deferred AWS resource maps must remain empty"
               {:instance-arch instance-arch :region-arch region-arch})))
    (doseq [entry ["datomic/transactor-key.jks"
                   "datomic/transactor-trust.jks"
                   "nano_impl/transactor-key.jks"
                   "nano_impl/transactor-trust.jks"]]
      (when-let [urls (seq (resources loader entry))]
        (fail! "licensed Transactor key material is visible to candidate"
               {:entry entry :urls urls})))

    ;; All 35 duplicate dependency class paths are order-sensitive.  This
    ;; checks every URL, not only the first, so the 21 byte-differing paths are
    ;; tied to the exact order recorded by ScanStructuralClasspath.
    (let [duplicate-rows (tsv-rows dependency-duplicates)]
      (when-not (= 35 (count duplicate-rows))
        (fail! "dependency duplicate-class inventory changed"
               {:count (count duplicate-rows)}))
      (when-not (= 21 (count (filter #(= "DIFFER" (nth % 3)) duplicate-rows)))
        (fail! "differing dependency duplicate-class inventory changed"
               {}))
      (doseq [[entry _owner-count _sha-count _relation owners _pairs]
              duplicate-rows
              :let [expected (->> (str/split owners #";")
                                  (mapv (fn [owner]
                                          (let [[_ordinal relative-path]
                                                (str/split owner #":" 2)]
                                            (jar-resource-url
                                             (io/file relative-path)
                                             entry)))))
                    actual (resources loader entry)
                    ;; Java 9+ exposes platform module descriptors as jrt:
                    ;; resources, and multi-release JAR descriptors can also
                    ;; be surfaced while asking for root module-info.class.
                    ;; Compare the ordered owners from the frozen dependency
                    ;; inventory while retaining unexpected URLs in failures.
                    expected-set (set expected)
                    relevant-actual (filterv expected-set actual)]]
        (when-not (= expected relevant-actual)
          (fail! "duplicate dependency class resolution order changed"
                 {:entry entry :expected expected :actual actual
                  :relevant-actual relevant-actual}))))

    (println "STRUCTURAL_ORIGIN_PASS")
    (println "247 Transactor sources + 25 Peer core2 sources resolve candidate-first")
    (println "52 Transactor Java classes and four Hot Rod stubs are candidate-owned")
    (println "four candidate runtime resources have unique origins and expected semantics")
    (println "532 dependencies (including one sanitized nano derivative) match the frozen order")
    (println "all 35 duplicate class paths match frozen ordered resolution; 21 differ in bytes")
    (println "all 272 dependency AOT initializers are absent")
    (println "licensed Peer, Transactor, core2 AOT, original nano, key, and trust artifacts are absent")
    (shutdown-agents)
    (System/exit 0)))

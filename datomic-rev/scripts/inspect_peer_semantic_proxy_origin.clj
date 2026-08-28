(require '[clojure.edn :as edn]
         '[clojure.java.io :as io])

(def expected-property "[:doc :file :line]")
(def expected-options [:doc :file :line])
(def source-resource
  "clojure/tools/analyzer/passes/add_binding_atom.clj")
(def expected-source-sha
  "6d97ead2cf4a0fd350b038b6bc6f65cc2f3ac924723473329d5a2b7543ae5d12")

(defn fail! [message data]
  (binding [*out* *err*]
    (println "PEER_SEMANTIC_PROXY_ORIGIN_FAIL" message (pr-str data)))
  (System/exit 2))

(defn sha256-stream [input]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        buffer (byte-array 65536)]
    (loop []
      (let [n (.read input buffer)]
        (when (pos? n)
          (.update digest buffer 0 n)
          (recur))))
    (format "%064x" (java.math.BigInteger. 1 (.digest digest)))))

(let [[expected-source-root output-file] *command-line-args*]
  (when-not (and expected-source-root output-file)
    (fail! "usage: inspect_peer_semantic_proxy_origin.clj EXPECTED_SOURCE_ROOT OUTPUT_EDN"
           {}))
  (try
    (when-not (= expected-property
                 (System/getProperty "clojure.compiler.elide-meta"))
      (throw (ex-info "compiler property differs from the shipped Peer setting"
                      {:actual (System/getProperty
                                 "clojure.compiler.elide-meta")})))
    (when-not (= expected-options (:elide-meta *compiler-options*))
      (throw (ex-info "active compiler options differ from the shipped Peer setting"
                      {:actual (:elide-meta *compiler-options*)})))
    (require 'clojure.tools.analyzer.passes.add-binding-atom)
    (let [source-url (io/resource source-resource)
          expected-file (.getCanonicalFile
                          (io/file expected-source-root source-resource))]
      (when-not source-url
        (throw (ex-info "semantic-proxy owner source resource is absent" {})))
      (when-not (= "file" (.getProtocol source-url))
        (throw (ex-info "semantic-proxy owner was not loaded from source"
                        {:source-url (str source-url)})))
      (when-not (= expected-file
                   (.getCanonicalFile (io/file (.toURI source-url))))
        (throw (ex-info "semantic-proxy owner source came from the wrong root"
                        {:expected (.getPath expected-file)
                         :actual (str source-url)})))
      (let [source-sha (with-open [input (io/input-stream source-url)]
                         (sha256-stream input))
            owner-var (ns-resolve
                        'clojure.tools.analyzer.passes.add-binding-atom
                        'add-binding-atom)
            factory (get-in (meta owner-var) [:pass-info :state])
            runtime-class (class factory)
            class-name (.getName runtime-class)
            class-resource-path
            (str "/" (.replace class-name "." "/") ".class")
            code-source (some-> runtime-class .getProtectionDomain
                                .getCodeSource .getLocation str)
            class-resource (some-> (.getResource runtime-class
                                                 class-resource-path)
                                   str)]
        (when-not (= expected-source-sha source-sha)
          (throw (ex-info "semantic-proxy owner source hash mismatch"
                          {:expected expected-source-sha :actual source-sha})))
        (when-not (and (instance? clojure.lang.IFn factory)
                       (.contains class-name
                                  "add_binding_atom$fn__")
                       (nil? code-source)
                       (nil? class-resource))
          (throw (ex-info "semantic proxy did not originate as an in-memory source-generated class"
                          {:runtime-class class-name
                           :code-source code-source
                           :class-resource class-resource})))
        (spit output-file
              (pr-str (sorted-map
                        :class-resource class-resource
                        :code-source code-source
                        :contract :source-generated-in-memory-class-v1
                        :runtime-class class-name
                        :source-file (.getPath expected-file)
                        :source-sha256 source-sha
                        :source-url (str source-url))))))
    (catch Throwable failure
      (fail! "semantic-proxy source origin could not be proven"
             (merge {:cause (ex-message failure)} (ex-data failure))))))

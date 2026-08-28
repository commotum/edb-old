(defn fail! [message]
  (binding [*out* *err*]
    (println message))
  (shutdown-agents)
  (System/exit 1))

(let [namespace-name (some-> *command-line-args* first symbol)
      preload-namespaces (mapv symbol (rest *command-line-args*))]
  (when-not namespace-name
    (fail! "usage: require_structural_namespace.clj NAMESPACE"))
  (try
    ;; Namespace top-level printing is noise for this gate.  Native/logging
    ;; output on stderr is retained by the per-process runner.
    (binding [*out* (java.io.StringWriter.)]
      (doseq [preload preload-namespaces]
        (require preload))
      (require namespace-name))
    (println "STRUCTURAL_REQUIRE_PASS" namespace-name)
    (shutdown-agents)
    (System/exit 0)
    (catch Throwable failure
      (let [chain (take-while some? (iterate ex-cause failure))]
        (binding [*out* *err*]
          (println "STRUCTURAL_REQUIRE_FAIL" namespace-name
                   (pr-str (mapv (fn [cause]
                                   {:class (.getName (class cause))
                                    :message (ex-message cause)})
                                 chain)))))
      (shutdown-agents)
      (System/exit 1))))

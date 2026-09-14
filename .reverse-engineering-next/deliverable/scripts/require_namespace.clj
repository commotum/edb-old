(let [namespace-name (some-> *command-line-args* first symbol)]
  (when-not namespace-name
    (binding [*out* *err*]
      (println "usage: require_namespace.clj NAMESPACE"))
    (System/exit 2))
  (try
    (require namespace-name)
    (println "PASS" namespace-name)
    (catch Throwable failure
      (let [chain (take-while some? (iterate ex-cause failure))]
        (println "FAIL" namespace-name (pr-str (mapv ex-message chain))))
      (System/exit 1))))

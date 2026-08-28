(let [[namespace-name target-id output-dir] *command-line-args*
      target-id (parse-long target-id)
      observed-id (clojure.lang.RT/nextID)
      remaining (- target-id observed-id 1)]
  (when-not (and namespace-name target-id output-dir)
    (throw (ex-info
             "usage: compile_aligned_clojure_source.clj NAMESPACE TARGET_ID OUTPUT_DIR"
             {:arguments *command-line-args*})))
  (when (neg? remaining)
    (throw (ex-info
             "compiler ID already exceeds requested alignment"
             {:namespace namespace-name
              :observed-id observed-id
              :target-id target-id})))
  ;; The original Transactor build used these compiler options.  Aligning the
  ;; monotonically allocated compiler ID lets exact shipped source reproduce
  ;; its embedded AOT closure byte for byte instead of merely after renaming
  ;; generated fn classes.
  (dotimes [_ remaining]
    (clojure.lang.RT/nextID))
  (binding [*compile-path* output-dir
            *compiler-options* {:elide-meta [:doc :file :line]}]
    (compile (symbol namespace-name))))

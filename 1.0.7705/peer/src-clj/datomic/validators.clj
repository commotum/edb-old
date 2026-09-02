(do
  (clojure.core/in-ns 'datomic.validators)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.validators)
    {:doc
     "Validation helpers for database-function definitions and internal option maps. These checks reject unknown keys and report required keys that are absent."})
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.set :as 'set])))
  (when-not (.equals 'datomic.validators 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.validators))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.set :as 'set]))))
  (defn allow-keys
    "Validates that m contains only the supplied keys. Returns nil on success and throws IllegalArgumentException listing any unexpected keys."
    ([m & ks]
      (let [extras (set/difference (apply hash-set (keys m)) (apply hash-set ks))]
        (when (seq extras)
          (binding [*print-length* 100 *print-level* 10]
            (throw
              (java.lang.IllegalArgumentException. (str "Unexpected keys " extras " in " m)))))
        nil)))
  (reset-meta!
    #'allow-keys
    (assoc
      {:arglists (clojure.core/list ['m '& 'ks]),
       :doc
       "Validates that m contains only the supplied keys. Returns nil on success and throws IllegalArgumentException listing any unexpected keys.",
       :column (int 1)}
      :name
      'allow-keys
      :ns
      *ns*))
  (defn require-keys
    "Validates that m contains every supplied key. Returns nil on success and throws IllegalArgumentException listing any missing keys."
    ([m & ks]
      (let [missing (set/difference (apply hash-set ks) (apply hash-set (keys m)))]
        (when (seq missing)
          (binding [*print-length* 100 *print-level* 10]
            (throw (java.lang.IllegalArgumentException. (str "Missing keys " missing " in " m)))))
        nil)))
  (reset-meta!
    #'require-keys
    (assoc
      {:arglists (clojure.core/list ['m '& 'ks]),
       :doc
       "Validates that m contains every supplied key. Returns nil on success and throws IllegalArgumentException listing any missing keys.",
       :column (int 1)}
      :name
      'require-keys
      :ns
      *ns*)))

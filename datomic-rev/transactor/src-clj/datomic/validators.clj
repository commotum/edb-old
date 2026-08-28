(do
  (clojure.core/in-ns 'datomic.validators)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.validators)
    {:doc "Validation helpers. Used in data fns and internally."})
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.set :as 'set])))
  (when-not (.equals 'datomic.validators 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.validators))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.set :as 'set]))))
  (defn allow-keys
    ([m & ks]
      (let [extras (set/difference (apply hash-set (keys m)) (apply hash-set ks))]
        (when (seq extras)
          (binding [*print-length* 100 *print-level* 10]
            (throw
              (java.lang.IllegalArgumentException. (str "Unexpected keys " extras " in " m)))))
        nil)))
  (defn require-keys
    ([m & ks]
      (let [missing (set/difference (apply hash-set ks) (apply hash-set (keys m)))]
        (when (seq missing)
          (binding [*print-length* 100 *print-level* 10]
            (throw (java.lang.IllegalArgumentException. (str "Missing keys " missing " in " m)))))
        nil))))
(do
  (clojure.core/in-ns 'datomic.spec)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.spec.alpha :as 's])))
  (when-not (.equals 'datomic.spec 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.spec))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.spec.alpha :as 's]))))
  (defn conform!
    ([spec x]
      (let [conformed (s/conform spec x)]
        (when (= :clojure.spec.alpha/invalid conformed)
          (throw (ex-info (s/explain-str spec x) {:data (s/explain-data spec x), :value x})))
        conformed)))
  (reset-meta!
    #'conform!
    (assoc {:arglists (clojure.core/list ['spec 'x]), :column (int 1)} :name 'conform! :ns *ns*)))
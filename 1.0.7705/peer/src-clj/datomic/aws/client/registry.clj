(do
  (clojure.core/in-ns 'datomic.aws.client.registry)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws.client.registry)
    {:doc
     "Registry for AWS operations.\n\n  Populated by register-op macro calls at compile time."})
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.aws.client.registry 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.registry))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (.setMeta (clojure.lang.RT/var "datomic.aws.client.registry" "lookup-op") {:column (int 1)})
  (let [v__5792__auto__ #'lookup-op]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.aws.client.registry" "lookup-op") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.aws.client.registry" "lookup-op")
        (clojure.lang.MultiFn.
          "lookup-op"
          (fn fn__14451 ([client_class op] [client_class op]))
          :default
          #'clojure.core/global-hierarchy))
      #'lookup-op))
  (defmethod
    lookup-op
    :default
    fn__14456
    ([client_class op]
      {:cognitect.anomalies/category :cognitect.anomalies/incorrect,
       :message (str "Op not registered: " [client_class op]),
       :data {:client-class client_class, :op op}})))
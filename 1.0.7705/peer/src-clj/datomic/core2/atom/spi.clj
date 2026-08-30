(do
  (clojure.core/in-ns 'datomic.core2.atom.spi)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.core2.atom.spi 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.atom.spi))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      DurableAtom
      (-swap-vals! [_ f ch] "Like atom swap-vals! but puts result on channel")
      (-sync [_ ch] "Puts latest value from server on channel"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.atom.spi" "DurableAtom")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'DurableAtom :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-swap-vals!
                                        {:arglists (clojure.core/list ['_ 'f 'ch])}),
                                      :arglists (clojure.core/list ['_ 'f 'ch]),
                                      :doc "Like atom swap-vals! but puts result on channel"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.atom.spi" "DurableAtom"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.atom.spi" "-swap-vals!")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta '-sync {:arglists (clojure.core/list ['_ 'ch])}),
                                      :arglists (clojure.core/list ['_ 'ch]),
                                      :doc "Puts latest value from server on channel"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.atom.spi" "DurableAtom"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.atom.spi" "-sync")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))))
(do
  (clojure.core/in-ns 'datomic.core2.atom.spi)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.core2.atom.spi 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.atom.spi))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      DurableAtom
      (-swap-vals! [_ f ch] "Like atom swap-vals! but puts result on channel")
      (-sync [_ ch] "Puts latest value from server on channel"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.atom.spi" "DurableAtom")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'DurableAtom :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-swap-vals!
                                        {:arglists (clojure.core/list ['_ 'f 'ch])}),
                                      :arglists (clojure.core/list ['_ 'f 'ch]),
                                      :doc "Like atom swap-vals! but puts result on channel"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.atom.spi" "DurableAtom"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.atom.spi" "-swap-vals!")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*)))
    (let [protocol_signature__7434 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta '-sync {:arglists (clojure.core/list ['_ 'ch])}),
                                      :arglists (clojure.core/list ['_ 'ch]),
                                      :doc "Puts latest value from server on channel"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.atom.spi" "DurableAtom"))
          protocol_method_name__7435 (with-meta
                                       (:name protocol_signature__7434)
                                       protocol_signature__7434)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.atom.spi" "-sync")
        (assoc protocol_signature__7434 :name protocol_method_name__7435 :ns *ns*)))))
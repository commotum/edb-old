(do
  (clojure.core/in-ns 'datomic.core2.atom.spi)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.core2.atom.spi 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.atom.spi))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defonce DurableAtom {})
  (defprotocol DurableAtom (-swap-vals! [_ f ch]) (-sync [_ ch])))
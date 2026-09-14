(do
  (clojure.core/in-ns 'datomic.core2.atom)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.core2.atom)
    {:doc
     "API for a durable atom-like reference type with an\nin-memory copy of the latest known value.\n\nDurable atoms support reset!/swap-vals!/swap!, similar to in-memory\natoms but returning a channel. They also implement clojure.lang.IRef.\n\natom.logged provides a reference implementation of durable atoms\nbacked by a datomic.core2.log.\n\nDurable atom providers must implement spi/DurableAtom and\nclojure.lang.IRef."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['reset! 'sync 'swap-vals! 'swap!])
      (clojure.core/require
        ['clojure.core.async :as 'a]
        ['datomic.core2.anomalies :refer ['anom]]
        ['datomic.core2.atom.spi :as 'spi])))
  (when-not (.equals 'datomic.core2.atom 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.atom))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['reset! 'sync 'swap-vals! 'swap!])
        (clojure.core/require
          ['clojure.core.async :as 'a]
          ['datomic.core2.anomalies :refer ['anom]]
          ['datomic.core2.atom.spi :as 'spi]))))
  (defn reset!
    ([a v]
      (let [ch (a/promise-chan
                 (map
                   (fn fn__19663
                     ([p1__19662#]
                       (or (datomic.core2.anomalies/anom p1__19662#) (second p1__19662#))))))]
        (spi/-swap-vals! a (constantly v) ch))))
  (defn swap-vals!
    ([a f & args]
      (let [ch (a/promise-chan)] (spi/-swap-vals! a (fn fn__19667 ([v] (apply f v args))) ch))))
  (defn swap!
    ([a f & args]
      (let [ch (a/promise-chan
                 (map
                   (fn fn__19671
                     ([p1__19670#]
                       (or (datomic.core2.anomalies/anom p1__19670#) (second p1__19670#))))))]
        (spi/-swap-vals! a (fn fn__19674 ([v] (apply f v args))) ch))))
  (defn sync ([a] (let [ch (a/promise-chan)] (spi/-sync a ch)))))
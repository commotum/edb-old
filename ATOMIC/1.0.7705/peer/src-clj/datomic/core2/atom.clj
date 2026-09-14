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
                   (fn fn__20487
                     ([p1__20486#]
                       (or (datomic.core2.anomalies/anom p1__20486#) (second p1__20486#))))))]
        (spi/-swap-vals! a (constantly v) ch))))
  (reset-meta!
    #'reset!
    (assoc {:arglists (clojure.core/list ['a 'v]), :column (int 1)} :name 'reset! :ns *ns*))
  (defn swap-vals!
    ([a f & args]
      (let [ch (a/promise-chan)] (spi/-swap-vals! a (fn fn__20491 ([v] (apply f v args))) ch))))
  (reset-meta!
    #'swap-vals!
    (assoc
      {:arglists (clojure.core/list ['a 'f '& 'args]), :column (int 1)}
      :name
      'swap-vals!
      :ns
      *ns*))
  (defn swap!
    ([a f & args]
      (let [ch (a/promise-chan
                 (map
                   (fn fn__20495
                     ([p1__20494#]
                       (or (datomic.core2.anomalies/anom p1__20494#) (second p1__20494#))))))]
        (spi/-swap-vals! a (fn fn__20498 ([v] (apply f v args))) ch))))
  (reset-meta!
    #'swap!
    (assoc
      {:arglists (clojure.core/list ['a 'f '& 'args]), :column (int 1)}
      :name
      'swap!
      :ns
      *ns*))
  (defn sync ([a] (let [ch (a/promise-chan)] (spi/-sync a ch))))
  (reset-meta!
    #'sync
    (assoc {:arglists (clojure.core/list ['a]), :column (int 1)} :name 'sync :ns *ns*)))
(do
  (clojure.core/in-ns 'datomic.kv-store)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get]))
  (when-not (.equals 'datomic.kv-store 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-store))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get])))
  (def ^{:dynamic true} *retry*)
  (reset-meta! #'*retry* (assoc {:dynamic true, :column 1} :name '*retry* :ns *ns*))
  (defonce KVStore {})
  (defprotocol
    KVStore
    (put [_ val-map])
    (get [_ key consistent?])
    (delete [_ key consistent?])
    (close [_]))
  (defonce Retryable {})
  (defprotocol Retryable (retryable? [_]))
  (extend java.lang.Throwable Retryable {:retryable? (fn fn__10831 ([_] true))})
  (extend java.lang.InterruptedException Retryable {:retryable? (fn fn__10833 ([_] false))}))
(do
  (clojure.core/in-ns 'datomic.valcache.puts-pool)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.valcache.puts-pool 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.valcache.puts-pool))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defonce PutsPool {})
  (defprotocol PutsPool (submit [_ k data f]) (get-queued-put [_ k]))
  (defmulti get-from-put :source)
  (defmethod get-from-put :default fn__20705 ([_] nil))
  (defn get-from-queued-put
    ([puts_pool k] (some-> (get-queued-put puts_pool k) (:data) (get-from-put)))))
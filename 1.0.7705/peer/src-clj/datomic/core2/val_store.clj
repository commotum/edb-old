(do
  (clojure.core/in-ns 'datomic.core2.val-store)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require ['datomic.core2.val-store.spi :as 'spi])))
  (when-not (.equals 'datomic.core2.val-store 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.val-store))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require ['datomic.core2.val-store.spi :as 'spi]))))
  (set! *warn-on-reflection* true)
  (defn put
    ([val_store k v opts]
      (do
        (when-not (:val v)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list :val 'v))))))
        (spi/-put val_store k v opts)))
    ([val_store k v] (put val_store k v nil)))
  (reset-meta!
    #'put
    (assoc
      {:arglists (clojure.core/list ['val-store 'k 'v] ['val-store 'k 'v 'opts]), :column (int 1)}
      :name
      'put
      :ns
      *ns*))
  (defn get ([val_store k opts] (spi/-get val_store k opts)) ([val_store k] (get val_store k nil)))
  (reset-meta!
    #'get
    (assoc
      {:arglists (clojure.core/list ['val-store 'k] ['val-store 'k 'opts]), :column (int 1)}
      :name
      'get
      :ns
      *ns*))
  (defn delete
    ([val_store k opts] (spi/-delete val_store k opts))
    ([val_store k] (delete val_store k nil)))
  (reset-meta!
    #'delete
    (assoc
      {:arglists (clojure.core/list ['val-store 'k] ['val-store 'k 'opts]), :column (int 1)}
      :name
      'delete
      :ns
      *ns*)))
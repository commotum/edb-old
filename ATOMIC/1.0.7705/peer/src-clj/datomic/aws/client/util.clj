(do
  (clojure.core/in-ns 'datomic.aws.client.util)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.aws.client.util 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.util))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defn vmap
    ([& kvs]
      (reduce
        (fn fn__14463
          ([m p__14462]
            (let [vec__14464 p__14462
                  k (nth vec__14464 (int 0) nil)
                  v (nth vec__14464 (int 1) nil)]
              (if (some? v) (assoc m k v) m))))
        {}
        (partition 2 kvs))))
  (reset-meta!
    #'vmap
    (assoc {:arglists (clojure.core/list ['& 'kvs]), :column (int 1)} :name 'vmap :ns *ns*)))
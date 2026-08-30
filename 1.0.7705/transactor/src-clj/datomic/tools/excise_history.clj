(do
  (clojure.core/in-ns 'datomic.tools.excise-history)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.tools.repair-ch197761 :as 'repair])))
  (when-not (.equals 'datomic.tools.excise-history 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.excise-history))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.tools.repair-ch197761 :as 'repair]))))
  (defn -main ([& args] (apply repair/-main args)))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))
(do
  (clojure.core/in-ns 'datomic.tools.gc-db)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.tools.gc-db)
    {:doc
     "Standalone storage garbage collection for one Datomic database. The command accepts a database URI and an RFC3339-like cutoff instant, deletes unreachable storage segments older than that instant, prints per-batch and total segment counts, and always shuts down Datomic after a collection attempt. A conservative cutoff protects index values still used by long-running processes."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.instant :as 'instant]
        ['datomic.api :as 'd]
        ['datomic.coordination :as 'coord]
        ['datomic.garbage :as 'garbage]
        ['datomic.uri :as 'uri])))
  (when-not (.equals 'datomic.tools.gc-db 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.gc-db))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.instant :as 'instant]
          ['datomic.api :as 'd]
          ['datomic.coordination :as 'coord]
          ['datomic.garbage :as 'garbage]
          ['datomic.uri :as 'uri]))))
  (defn -main
    ([& args]
      (if (= (count args) 2)
        (let [[db-uri older-than] args
              cutoff (instant/read-instant-date older-than)]
          (try
            (let [cluster (coord/create-db-cluster
                            (coord/cluster-conf->resolved-conf (uri/parse db-uri)))
                  counter (atom 0)]
              (garbage/gc
                cluster
                cutoff
                (fn fn__32078 ([ct] (swap! counter + ct) (prn {:segments ct}))))
              (prn {:finished true, :segments (deref counter)}))
            (finally (d/shutdown true))))
        (do
          (println "Usage: datomic.tools.gc-db {uri} {older-than}")
          (println "older-than is an RFC3339-like timestamp.")
          (java.lang.System/exit (int -1))
          nil))))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['& 'args]),
       :doc
       "Collects unreachable storage for db-uri when it is older than the RFC3339-like older-than timestamp. Each callback prints {:segments n}; completion prints {:finished true :segments total}. Datomic shuts down in a finally block. Wrong argument count prints usage and exits -1; an invalid timestamp raises a parse error.",
       :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))

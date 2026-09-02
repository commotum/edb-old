(do
  (clojure.core/in-ns 'datomic.tools.excise-history)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.tools.excise-history)
    {:doc
     "Command-line entry point for reporting and repairing incomplete excisions. Report mode scans every stored history index and writes affected datoms to an EDN file; repair mode rechecks the database and irreversibly removes those datoms by rebuilding and publishing the history roots."})
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
    (assoc
      {:arglists (clojure.core/list ['& 'args]),
       :doc
       "Delegates report and repair commands to repair-ch197761. Report exits 0 when the database is clean and 1 when repair is needed. Repair defaults to three root-publication attempts and exits -1 on failure.",
       :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))

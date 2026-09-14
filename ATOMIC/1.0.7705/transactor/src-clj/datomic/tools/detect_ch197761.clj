(do
  (clojure.core/in-ns 'datomic.tools.detect-ch197761)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.tools.detect-ch197761)
    {:doc
     "Detection support for incomplete excisions in stored history indexes. Excision transactions are reconstructed as-of the transaction that recorded each request, converted to removal predicates, and checked concurrently against EAVT, AEVT, AVET, and VAET history tiers. Reports contain every affected datom grouped by index together with the database URI, basis t, and an :ok summary."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'jio]
        ['clojure.pprint :as 'pp]
        ['datomic.api :as 'd]
        ['datomic.cluster :as 'cluster]
        ['datomic.cluster-stack :as 'cluster-stack]
        ['datomic.db :as 'db]
        ['datomic.excise :as 'excise]
        ['datomic.fressian :as 'fressian]
        ['datomic.index :as 'index]
        ['datomic.tools :as 'tools]
        ['datomic.transaction :as 'tx])))
  (when-not (.equals 'datomic.tools.detect-ch197761 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.detect-ch197761))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'jio]
          ['clojure.pprint :as 'pp]
          ['datomic.api :as 'd]
          ['datomic.cluster :as 'cluster]
          ['datomic.cluster-stack :as 'cluster-stack]
          ['datomic.db :as 'db]
          ['datomic.excise :as 'excise]
          ['datomic.fressian :as 'fressian]
          ['datomic.index :as 'index]
          ['datomic.tools :as 'tools]
          ['datomic.transaction :as 'tx]))))
  (def excise-spec [:db/id :db/excise :db.excise/before :db.excise/beforeT :db.excise/attrs])
  (reset-meta!
    #'excise-spec
    (assoc
      {:doc
       "Pull pattern containing the target, time boundary, and attribute restriction of an excision request.",
       :column (int 1)}
      :name
      'excise-spec
      :ns
      *ns*))
  (def excision-txes
   [:find '?excision-record '?tx :in '$ :where ['?excision-record :db/excise '_ '?tx true]])
  (reset-meta!
    #'excision-txes
    (assoc
      {:doc
       "Query returning each excision-record entity and the transaction that asserted :db/excise.",
       :column (int 1)}
      :name
      'excision-txes
      :ns
      *ns*))
  (defn excision-specs
    ([db]
      (mapv
        (fn [[entity-id tx]]
          (d/pull (d/as-of db tx) excise-spec entity-id))
        (d/q excision-txes (d/history db)))))
  (reset-meta!
    #'excision-specs
    (assoc
      {:arglists (clojure.core/list ['db]),
       :doc
       "Returns every recorded excision specification as it appeared when its :db/excise assertion was transacted. Reconstructing the entity as-of that transaction prevents later changes from altering the historical request.",
       :column (int 1)}
      :name
      'excision-specs
      :ns
      *ns*))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__19852 ([& _] (when (zero? (mod (swap! c inc) n)) (print ".") (flush)))))))
  (reset-meta!
    #'progress-dot-fn
    (assoc
      {:arglists (clojure.core/list ['n]),
       :doc "Returns a callback that prints and flushes one progress dot after every n invocations.",
       :column (int 1)}
      :name
      'progress-dot-fn
      :ns
      *ns*))
  (defn excised-datoms
    ([db index xpreds]
      (let [progress (progress-dot-fn 100000)]
        (filterv
          (fn fn__19856
            ([d]
              (^clojure.lang.IFn progress)
              (some (fn fn__19857 ([p1__19855#] (excise/remove? p1__19855# d))) xpreds)))
          (d/datoms db index)))))
  (reset-meta!
    #'excised-datoms
    (assoc
      {:arglists (clojure.core/list ['db 'index 'xpreds]), :column (int 1)}
      :doc
      "Scans index in db and returns every datom matched by at least one excision predicate. A progress dot is printed for each 100,000 datoms inspected."
      :name
      'excised-datoms
      :ns
      *ns*))
  (defn mem-and-history-tier-db
    ([db]
      (when-not (d/is-history db)
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              "just-history-db requires a history-db"
              "\n"
              (pr-str (clojure.core/list 'd/is-history 'db))))))
      (assoc db :indexing nil :mid-index nil :index nil)))
  (reset-meta!
    #'mem-and-history-tier-db
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :doc
      "Returns a history database restricted to the memory and history tiers by removing the current indexing, mid-index, and index roots. Throws AssertionError unless db is a history database."
      :name
      'mem-and-history-tier-db
      :ns
      *ns*))
  (defn check-indexes
    ([db xpreds]
      (let [hdb (d/history db)
            hdb (mem-and-history-tier-db hdb)
            eavt (future-call (fn fn__19862 ([] (excised-datoms hdb :eavt xpreds))))
            aevt (future-call (fn fn__19864 ([] (excised-datoms hdb :aevt xpreds))))
            avet (future-call (fn fn__19866 ([] (excised-datoms hdb :avet xpreds))))
            vaet (future-call (fn fn__19868 ([] (excised-datoms hdb :vaet xpreds))))]
        {:eavt (deref eavt), :aevt (deref aevt), :avet (deref avet), :vaet (deref vaet)})))
  (reset-meta!
    #'check-indexes
    (assoc
      {:arglists (clojure.core/list ['db 'xpreds]), :column (int 1)}
      :doc
      "Scans the EAVT, AEVT, AVET, and VAET history indexes concurrently and returns the matched datoms under their index keys. The current database indexes are intentionally excluded."
      :name
      'check-indexes
      :ns
      *ns*))
  (defn find-zombie-datoms
    ([db]
      (let [excision-specs (excision-specs db)]
        (when (seq excision-specs)
          (let [xpreds (excise/create-xpreds db excision-specs)] (check-indexes db xpreds))))))
  (reset-meta!
    #'find-zombie-datoms
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :doc
      "Returns unexcised history datoms grouped by index, or nil when db contains no excision requests. A returned datom is one that a recorded excision predicate says should have been removed."
      :name
      'find-zombie-datoms
      :ns
      *ns*))
  (defn ok? ([results] (empty? (apply concat (vals results)))))
  (reset-meta!
    #'ok?
    (assoc
      {:arglists (clojure.core/list ['results]),
       :doc "Returns true when no index in results contains an unexcised datom.",
       :column (int 1)}
      :name
      'ok?
      :ns
      *ns*))
  (defn summarize ([db db-uri results] {:ok (ok? results), :uri db-uri, :t (d/basis-t db)}))
  (reset-meta!
    #'summarize
    (assoc
      {:arglists (clojure.core/list ['db 'db-uri 'results]), :column (int 1)}
      :doc
      "Returns the compact report summary: whether every scanned index is clean, the database URI, and the basis t at which db was inspected."
      :name
      'summarize
      :ns
      *ns*))
  (defn get-db-from-connection-resources
    ([{:keys [cluster olookup]}]
      (db/db
        (cluster/dbId cluster)
        (index/load-index olookup (index/find-index-root-id cluster)))))
  (reset-meta!
    #'get-db-from-connection-resources
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]}]), :column (int 1)}
      :doc
      "Loads the database at the cluster's currently published index root from connection resources."
      :name
      'get-db-from-connection-resources
      :ns
      *ns*))
  (defn get-db-from-uri
    ([uri]
      (when-not (deref cluster-stack/kv-cache-ref) (cluster-stack/start-kv-cache))
      (get-db-from-connection-resources (tools/connection-resources uri))))
  (reset-meta!
    #'get-db-from-uri
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :doc
      "Opens uri as a storage-level database, starting the shared key-value cache when necessary."
      :name
      'get-db-from-uri
      :ns
      *ns*))
  (defn find-zombie-datoms-with-summary
    ([uri]
      (let [db (get-db-from-uri uri)
            report (find-zombie-datoms db)
            summary (summarize db uri report)]
        (assoc summary :results report))))
  (reset-meta!
    #'find-zombie-datoms-with-summary
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :doc
      "Scans uri for incomplete excisions and returns a report containing :ok, :uri, :t, and per-index :results."
      :name
      'find-zombie-datoms-with-summary
      :ns
      *ns*))
  (defn write-report
    ([report out-filename]
      (with-open [edn-writer (apply jio/writer (str out-filename) {})]
        (pp/pprint report edn-writer))))
  (reset-meta!
    #'write-report
    (assoc
      {:arglists (clojure.core/list ['report 'out-filename]), :column (int 1)}
      :doc
      "Writes report as readable EDN to out-filename. Reports may contain the affected datoms themselves and must be handled as database data."
      :name
      'write-report
      :ns
      *ns*))
  (defn write-zombie-datoms-report
    ([db-uri out-filename]
      (let [db (get-db-from-uri db-uri)
            results (find-zombie-datoms db)
            summary (summarize db db-uri results)
            report (assoc summary :results results)]
        (write-report report out-filename)
        summary)))
  (reset-meta!
    #'write-zombie-datoms-report
    (assoc
      {:arglists (clojure.core/list ['db-uri 'out-filename]), :column (int 1)}
      :doc
      "Scans db-uri, writes the complete per-index report to out-filename, and returns the summary without :results."
      :name
      'write-zombie-datoms-report
      :ns
      *ns*))
  (defn -main
    ([db-uri out-filename]
      (println (write-zombie-datoms-report db-uri out-filename))
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['db-uri 'out-filename]), :column (int 1)}
      :doc
      "Runs the low-level detector for db-uri, writes the complete report to out-filename, prints its summary, and exits with status 0. The excise-history report command provides the user-facing 0-for-clean and 1-for-repair-needed status."
      :name
      '-main
      :ns
      *ns*)))

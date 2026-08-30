(do
  (clojure.core/in-ns 'datomic.tools.detect-ch197761)
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
  (reset-meta! #'excise-spec (assoc {:column (int 1)} :name 'excise-spec :ns *ns*))
  (def excision-txes
   [:find '?excision-record '?tx :in '$ :where ['?excision-record :db/excise '_ '?tx true]])
  (reset-meta! #'excision-txes (assoc {:column (int 1)} :name 'excision-txes :ns *ns*))
  (defn excision-specs
    ([db]
      (mapv
        (fn fn__19846
          ([p__19845]
            (let [vec__19847 p__19845
                  entity_id (nth vec__19847 (int 0) nil)
                  tx (nth vec__19847 (int 1) nil)]
              (d/pull (d/as-of db tx) excise-spec entity_id))))
        (d/q excision-txes (d/history db)))))
  (reset-meta!
    #'excision-specs
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'excision-specs :ns *ns*))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__19852 ([& _] (when (zero? (mod (swap! c inc) n)) (print ".") (flush)))))))
  (reset-meta!
    #'progress-dot-fn
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'progress-dot-fn :ns *ns*))
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
      :name
      'check-indexes
      :ns
      *ns*))
  (defn find-zombie-datoms
    ([db]
      (let [excision_specs (excision-specs db)]
        (when (seq excision_specs)
          (let [xpreds (excise/create-xpreds db excision_specs)] (check-indexes db xpreds))))))
  (reset-meta!
    #'find-zombie-datoms
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'find-zombie-datoms
      :ns
      *ns*))
  (defn ok? ([results] (empty? (apply concat (vals results)))))
  (reset-meta!
    #'ok?
    (assoc {:arglists (clojure.core/list ['results]), :column (int 1)} :name 'ok? :ns *ns*))
  (def summarize
   (fn summarize ([db db_uri results] {:ok (ok? results), :uri db_uri, :t (d/basis-t db)})))
  (reset-meta!
    #'summarize
    (assoc
      {:arglists (clojure.core/list ['db 'db-uri 'results]), :column (int 1)}
      :name
      'summarize
      :ns
      *ns*))
  (def get-db-from-connection-resources
   (fn get_db_from_connection_resources
     ([p__19874]
       (let [map__19875 p__19874
             map__19875 (if (seq? map__19875)
                          (if (next map__19875)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19875))
                            (if (seq map__19875) (first map__19875) {}))
                          map__19875)
             cluster (get map__19875 :cluster)
             olookup (get map__19875 :olookup)]
         (db/db
           (cluster/dbId cluster)
           (index/load-index olookup (index/find-index-root-id cluster)))))))
  (reset-meta!
    #'get-db-from-connection-resources
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]}]), :column (int 1)}
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
      :name
      'find-zombie-datoms-with-summary
      :ns
      *ns*))
  (def write-report
   (fn write_report
     ([report out_filename]
       (with-open [edn_writer (apply jio/writer (str out_filename) {})]
         (pp/pprint report edn_writer)))))
  (reset-meta!
    #'write-report
    (assoc
      {:arglists (clojure.core/list ['report 'out-filename]), :column (int 1)}
      :name
      'write-report
      :ns
      *ns*))
  (def write-zombie-datoms-report
   (fn write_zombie_datoms_report
     ([db_uri out_filename]
       (let [db (get-db-from-uri db_uri)
             results (find-zombie-datoms db)
             summary (summarize db db_uri results)
             r (assoc summary :results results)]
         (write-report r out_filename)
         summary))))
  (reset-meta!
    #'write-zombie-datoms-report
    (assoc
      {:arglists (clojure.core/list ['db-uri 'out-filename]), :column (int 1)}
      :name
      'write-zombie-datoms-report
      :ns
      *ns*))
  (def -main
   (fn _main
     ([db_uri out_filename]
       (println (write-zombie-datoms-report db_uri out_filename))
       (java.lang.System/exit (int 0))
       nil)))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['db-uri 'out-filename]), :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))
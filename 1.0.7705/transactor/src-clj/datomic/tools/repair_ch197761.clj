(do
  (clojure.core/in-ns 'datomic.tools.repair-ch197761)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.tools.repair-ch197761)
    {:doc
     "Irreversible repair of incomplete excisions in stored history indexes. For each attempt, the tool loads the currently published index root, rebuilds the EAVT, AEVT, AVET, and reverse-reference history tiers without the reported datoms, verifies that only the four history roots and root revision changed, persists a replacement root map, and publishes it with a compare-and-swap. Concurrent index publication causes a retry; a successful publication marks and flushes superseded index segments for storage garbage collection."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.data :as 'data]
        ['clojure.java.io :as 'jio]
        ['clojure.set :as 'set]
        ['datomic.api :as 'd]
        ['datomic.cluster :as 'cluster]
        ['datomic.cluster-stack :as 'cluster-stack]
        ['datomic.common :as 'common]
        ['datomic.db :as 'db]
        ['datomic.excise :as 'excise]
        ['datomic.fressian :as 'fressian]
        ['datomic.garbage :as 'garbage]
        ['datomic.index :as 'index]
        ['datomic.tools :as 'tools]
        ['datomic.tools.detect-ch197761 :as 'dx]
        ['datomic.transaction :as 'tx])
      (clojure.core/import 'datomic.db.Db)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'java.util.Collections)))
  (when-not (.equals 'datomic.tools.repair-ch197761 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.repair-ch197761))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.data :as 'data]
          ['clojure.java.io :as 'jio]
          ['clojure.set :as 'set]
          ['datomic.api :as 'd]
          ['datomic.cluster :as 'cluster]
          ['datomic.cluster-stack :as 'cluster-stack]
          ['datomic.common :as 'common]
          ['datomic.db :as 'db]
          ['datomic.excise :as 'excise]
          ['datomic.fressian :as 'fressian]
          ['datomic.garbage :as 'garbage]
          ['datomic.index :as 'index]
          ['datomic.tools :as 'tools]
          ['datomic.tools.detect-ch197761 :as 'dx]
          ['datomic.transaction :as 'tx])
        (clojure.core/import 'datomic.db.Db)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'java.util.Collections))))
  (set! *warn-on-reflection* true)
  (def DEFAULT_MAX_ATTEMPTS 3)
  (reset-meta!
    #'DEFAULT_MAX_ATTEMPTS
    (assoc
      {:doc "Default number of index-root publication attempts when concurrent indexing causes conflicts.",
       :column (int 1)}
      :name
      'DEFAULT_MAX_ATTEMPTS
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "merge-one-index")
    {:doc
     "Index merge primitive used to rebuild one stored history tier while applying excision predicates.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "merge-one-index")
    (deref #'index/merge-one-index))
  (defn error ([x] (tools/progress tools/println-err x)))
  (reset-meta!
    #'error
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column (int 1)}
      :doc "Writes x to the tool's synchronized progress-error stream."
      :name
      'error
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "index-configs")
    {:doc
     "Per-index configuration mapping report keys to history-root keys, datom comparators, directory comparators, partition functions, and merge result keys. VAET is stored under the RAET history root.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "index-configs")
    {:eavt
     {:hist-index-key :eavt-hist,
      :cmp db/eavt-cmp,
      :cmpi index/eavt-cmpi,
      :idxcmp :eavt,
      :part-fn (constantly 42),
      :result-key :eavt},
     :aevt
     {:hist-index-key :aevt-hist,
      :cmp db/aevt-cmp,
      :cmpi index/aevt-cmpi,
      :idxcmp :aevt,
      :part-fn
      (fn fn__19887
        ([p1__19885#]
          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__19885#))))),
      :result-key :aevt},
     :avet
     {:hist-index-key :avet-hist,
      :cmp db/avet-cmp,
      :cmpi index/avet-cmpi,
      :idxcmp :avet,
      :part-fn
      (fn fn__19889
        ([p1__19886#]
          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum p1__19886#))))),
      :result-key :avet},
     :raet
     {:hist-index-key :raet-hist,
      :cmp db/raet-cmp,
      :cmpi index/raet-cmpi,
      :idxcmp :raet,
      :part-fn (constantly 42),
      :result-key :vaet}})
  (defn assert-root-map-ok
    ([old-root-map new-root-map]
      (let [[old-diff new-diff _] (data/diff old-root-map new-root-map)
            expected-different-keys #{:rev :avet-hist :eavt-hist :raet-hist :aevt-hist}]
        (when-not (and
                    (= (set (keys old-diff)) expected-different-keys)
                    (= (set (keys new-diff)) expected-different-keys))
          (throw
            (java.lang.RuntimeException.
              (str
                "Something unexpected changed in the root-map.\nOld:\n"
                old-root-map
                "\nNew:\n"
                new-root-map)))
          nil))))
  (reset-meta!
    #'assert-root-map-ok
    (assoc
      {:arglists (clojure.core/list ['old-root-map 'new-root-map]), :column (int 1)}
      :doc
      "Validates that a repaired root map changes exactly :rev, :eavt-hist, :aevt-hist, :avet-hist, and :raet-hist. Throws RuntimeException when any expected field is unchanged or any unrelated field differs."
      :name
      'assert-root-map-ok
      :ns
      *ns*))
  (defn update-and-persist-root-map
    ([cluster old-root-map current-root-map new-root-id new-rev]
      (let [new-root-map (assoc current-root-map :rev new-rev)
            encoded-root (index/fress new-root-map index/common-write-handlers)]
        (assert-root-map-ok old-root-map new-root-map)
        (index/write-vals cluster {new-root-id encoded-root})
        [new-root-id new-root-map])))
  (reset-meta!
    #'update-and-persist-root-map
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['cluster 'old-root-map 'current-root-map 'new-root-id 'new-rev]),
       :doc
       "Sets the replacement root revision, validates the permitted changes against old-root-map, writes the encoded map under new-root-id, and returns [new-root-id new-root-map]. Publication of that id is performed separately."
       :column (int 1)}
      :name
      'update-and-persist-root-map
      :ns
      *ns*))
  (defn prepare-excise-preds
    ([unexcised-datoms]
      (let [cmp (get-in index-configs [:eavt :cmp])
            sorted-datoms (sort cmp unexcised-datoms)]
        [(reify
           datomic.excise.ExcisePred
           (ep-remove?
             [this d]
             (<=
               0
               (Collections/binarySearch
                 ^java.util.List sorted-datoms
                 d
                 ^java.util.Comparator cmp)))
           (ep-datoms [this] sorted-datoms))])))
  (reset-meta!
    #'prepare-excise-preds
    (assoc
      {:private true, :arglists (clojure.core/list ['unexcised-datoms]), :column (int 1)}
      :doc
      "Returns an excision predicate backed by the reported datoms sorted in EAVT order. Membership uses binary search, and ep-datoms exposes the same ordered collection to the index merger."
      :name
      'prepare-excise-preds
      :ns
      *ns*))
  (defn excise-tier
    ([{:keys [cluster olookup]} db as-of-t old-root-id config xpreds]
      (merge-one-index
        db
        cluster
        olookup
        old-root-id
        []
        []
        (:part-fn config)
        (:cmp config)
        index/common-write-handlers
        false
        as-of-t
        (:idxcmp config)
        xpreds
        nil
        (:cmpi config))))
  (reset-meta!
    #'excise-tier
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['cluster 'olookup]} 'db 'as-of-t 'old-root-id 'config 'xpreds]),
       :doc
       "Rebuilds one history index rooted at old-root-id through basis as-of-t, removing datoms selected by xpreds. Returns the index merger's new root id, garbage ids, retractions, and metrics."
       :column (int 1)}
      :name
      'excise-tier
      :ns
      *ns*))
  (defn p
    ([label root-map]
      (tools/progress
        prn
        (merge
          {:when label}
          (select-keys
            root-map
            [:basisT
             :nextT
             :rev
             :schema-level
             :birth-level
             :eavt-hist
             :aevt-hist
             :avet-hist
             :raet-hist])))))
  (reset-meta!
    #'p
    (assoc
      {:arglists (clojure.core/list ['label 'root-map]),
       :doc
       "Emits the identifying revision, level, and history-root fields of root-map with label to the progress stream.",
       :column (int 1)}
      :name
      'p
      :ns
      *ns*))
  (defn excise-index*
    ([connection-resources db root-map index->datoms]
      (let [as-of-t (:basisT root-map)
            [new-root-map garbage-ids metrics]
            (reduce
              (fn [[current-root-map accumulated-garbage metrics] index-name]
                (let [config (index-configs index-name)
                      history-index-root-id (root-map (:hist-index-key config))
                      unexcised-datoms (get index->datoms index-name)
                      _ (tools/progress
                          prn
                          {:phase :repair/index-start,
                           :index index-name,
                           :as-of-t as-of-t,
                           :hist-idx-root-id history-index-root-id,
                           :unexcised-datoms
                           (java.lang.Integer/valueOf (int (count unexcised-datoms))),
                           :version 1})
                      xpreds (prepare-excise-preds unexcised-datoms)
                      [new-history-index-root-id garbage-ids _retractions tier-metrics]
                      (excise-tier
                        connection-resources
                        db
                        as-of-t
                        history-index-root-id
                        config
                        xpreds)]
                  (tools/progress
                    prn
                    {:phase :repair/index-end,
                     :index index-name,
                     :old-root-id history-index-root-id,
                     :new-root-id new-history-index-root-id,
                     :version 1})
                  [(assoc
                     current-root-map
                     (:hist-index-key config)
                     new-history-index-root-id)
                   (into accumulated-garbage garbage-ids)
                   (index/aggregate-metrics metrics tier-metrics)]))
              [root-map [] {}]
              [:eavt :aevt :avet :raet])]
        [new-root-map garbage-ids (:written metrics) (:dirs-written metrics)])))
  (reset-meta!
    #'excise-index*
    (assoc
      {:private true,
       :arglists (clojure.core/list ['connection-resources 'db 'root-map 'index->datoms]),
       :doc
       "Rebuilds all four history indexes from root-map at its basis t. index->datoms supplies the reported removals for each index. Returns [new-root-map garbage-ids segments-written directories-written] after emitting per-index progress events."
       :column (int 1)}
      :name
      'excise-index*
      :ns
      *ns*))
  (defn excise-index
    ([{:keys [cluster olookup], :as connection-resources} index->datoms max-attempts]
      (loop [attempt 0]
        (if (< attempt max-attempts)
          (let [index-ref-key-name (index/index-ref-key-name cluster)
                index-root-ref (deref (cluster/get-ref cluster index-ref-key-name))
                index-root-id (cluster/val-key->uuid (common/getx index-root-ref :key))
                ref-revision (common/getx index-root-ref :rev)
                root-map (common/getx olookup index-root-id)
                _ (p "old-root-map" root-map)
                db (dx/get-db-from-connection-resources connection-resources)
                [new-root-map garbage-ids segments-written directories-written]
                (excise-index*
                  connection-resources
                  db
                  root-map
                  (set/rename-keys index->datoms {:vaet :raet}))
                [new-root-id persisted-root-map]
                (update-and-persist-root-map
                  cluster
                  root-map
                  new-root-map
                  (common/rand-uuid)
                  (inc (:rev root-map)))
                _ (p "new-root-map" persisted-root-map)]
            (if (=
                  :ok
                  (deref
                    (cluster/set-ref
                      cluster
                      index-ref-key-name
                      (inc ref-revision)
                      (cluster/uuid->val-key new-root-id))))
              (do
                (garbage/mark-garbage cluster olookup garbage-ids)
                (garbage/flush-garbage cluster olookup)
                (error (str "Index repair completed."))
                {:old-root-id index-root-id,
                 :new-root-id new-root-id,
                 :segs-written segments-written,
                 :dirs-written directories-written})
              (do
                (error (str "Conflict updating index root at rev " (inc ref-revision)))
                (recur (inc attempt)))))
          (do
            (throw
              (java.lang.RuntimeException.
                (str "Could not fix index after " max-attempts " tries. Quitting.")))
            nil)))))
  (reset-meta!
    #'excise-index
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['cluster 'olookup], :as 'connection-resources}
          'index->datoms
          'max-attempts]),
       :doc
       "Builds and publishes a repaired index root. Publication uses the current reference revision as a compare-and-swap guard; conflicts reload the newest root and rebuild up to max-attempts. On success, merge-reported superseded segments are marked and flushed for garbage collection. Returns old and new root ids with write metrics, or throws after the final conflict.",
       :column (int 1)}
      :name
      'excise-index
      :ns
      *ns*))
  (defn repair-db*
    ([uri report max-attempts]
      (error
        (str
          "Cleaning-up unexcised datoms for "
          uri
          " up to t "
          (:t report)
          ". This may take a long time!"))
      (tools/progress
        prn
        {:phase :repair/start, :start-t 0, :end-t (:t report), :uri uri, :version 1})
      (when-not (deref cluster-stack/kv-cache-ref) (cluster-stack/start-kv-cache))
      (let [connection-resources (tools/connection-resources uri)
            {:keys [segs-written dirs-written]}
            (excise-index
              (assoc connection-resources :uri uri)
              (:results report)
              max-attempts)]
        (tools/progress
          prn
          {:phase :repair/end,
           :segs-written segs_written,
           :dirs-written dirs_written,
           :version 1}))))
  (reset-meta!
    #'repair-db*
    (assoc
      {:arglists (clojure.core/list ['uri 'report 'max-attempts]), :column (int 1)}
      :doc
      "Repairs the history roots described by report for uri, retrying root publication at most max-attempts times. Starts the storage cache when needed and emits start, per-index, root-map, and completion progress records with write counts."
      :name
      'repair-db*
      :ns
      *ns*))
  (defn repair-db
    ([uri out-filename max-attempts]
      (try
        (let [report (dx/find-zombie-datoms-with-summary uri)]
          (println " ")
          (prn (select-keys report [:ok :uri :t]))
          (if (not= uri (:uri report))
            (error
              (str
                "Target DB "
                uri
                " is different than the one that generated the report, "
                (:uri report)))
            (if (:ok report)
              (error "DB does not need repair.")
              (do (dx/write-report report out-filename) (repair-db* uri report max-attempts)))))
        (catch
          java.lang.Throwable
          t
          (do
            (.printStackTrace ^java.lang.Throwable t)
            (tools/flush-progress)
            (d/shutdown true)
            (error "FAILED")
            (java.lang.System/exit (int -1))
            nil)))
      (tools/flush-progress)
      (d/shutdown true)))
  (reset-meta!
    #'repair-db
    (assoc
      {:arglists (clojure.core/list ['uri 'out-filename 'max-attempts]), :column (int 1)}
      :doc
      "Rechecks uri, prints its summary, and repairs it when incomplete excisions are found. The full report is written to out-filename before mutation. Failures print a stack trace, flush progress, shut down Datomic, and exit -1; normal completion flushes progress and shuts down Datomic."
      :name
      'repair-db
      :ns
      *ns*))
  (defn help
    ([]
      (println
        "To generate a report run\n"
        "bin/run -Xmx${MEM} -m datomic.tools.excise-history report ${URI} ${report-file-name}")
      (println
        "To clean up run\n"
        "bin/run -Xmx${MEM} -m datomic.tools.excise-history repair ${URI} ${report-file-name} ${max-attempts}")))
  (reset-meta!
    #'help
    (assoc
      {:arglists (clojure.core/list []),
       :doc "Prints report and repair command forms for the excise-history tool.",
       :column (int 1)}
      :name
      'help
      :ns
      *ns*))
  (defn -main
    ([& args]
      (if (>= (count args) 2)
        (let [op (first args)
              uri (second args)
              out-filename (nth args (int 2) nil)
              max-attempts (parse-long (nth args (int 3) ""))]
          (case
            op
            "report"
            (if (= (count args) 3)
              (let [summary (dx/write-zombie-datoms-report uri out-filename)]
                (println " ")
                (prn summary)
                (when-not (:ok summary) (error (str "Database needs repair: " uri)))
                (java.lang.System/exit (int (if (:ok summary) 0 1)))
                nil)
              (help))
            "repair"
            (if (= (count args) 3)
              (repair-db uri out-filename DEFAULT_MAX_ATTEMPTS)
              (if (and (= (long (count args)) 4) max-attempts (number? max-attempts))
                (repair-db uri out-filename max-attempts)
                (help)))
            (help)))
        (help))
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['& 'args]),
       :doc
       "Runs excise-history report or repair mode. Report exits 0 when clean and 1 when repair is needed. Repair uses three publication attempts unless max-attempts is supplied, exits -1 on failure, and exits 0 after success. Malformed commands print usage and exit 0.",
       :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))

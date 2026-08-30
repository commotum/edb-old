(do
  (clojure.core/in-ns 'datomic.tools.repair-ch197761)
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
    (assoc {:column (int 1)} :name 'DEFAULT_MAX_ATTEMPTS :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "merge-one-index")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "merge-one-index")
    (deref #'index/merge-one-index))
  (defn error ([x] (tools/progress tools/println-err x)))
  (reset-meta!
    #'error
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'error
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.tools.repair-ch197761" "index-configs")
    {:column (int 1)})
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
  (def assert-root-map-ok
   (fn assert_root_map_ok
     ([old_root_map new_root_map]
       (let [vec__19891 (data/diff old_root_map new_root_map)
             old_diff (nth vec__19891 (int 0) nil)
             new_diff (nth vec__19891 (int 1) nil)
             _ (nth vec__19891 (int 2) nil)
             expected_different_keys #{:rev :avet-hist :eavt-hist :raet-hist :aevt-hist}]
         (when-not (and
                     (= (set (keys old_diff)) expected_different_keys)
                     (= (set (keys new_diff)) expected_different_keys))
           (throw
             (java.lang.RuntimeException.
               (str
                 "Something unexpected changed in the root-map.\nOld:\n"
                 old_root_map
                 "\nNew:\n"
                 new_root_map)))
           nil)))))
  (reset-meta!
    #'assert-root-map-ok
    (assoc
      {:arglists (clojure.core/list ['old-root-map 'new-root-map]), :column (int 1)}
      :name
      'assert-root-map-ok
      :ns
      *ns*))
  (def update-and-persist-root-map
   (fn update_and_persist_root_map
     ([cluster old_root_map curr_root_map new_root_id new_rev]
       (let [new_root_map (assoc curr_root_map :rev new_rev)
             fressed_root (index/fress new_root_map index/common-write-handlers)]
         (assert-root-map-ok old_root_map new_root_map)
         (index/write-vals cluster {new_root_id fressed_root})
         [new_root_id new_root_map]))))
  (reset-meta!
    #'update-and-persist-root-map
    (assoc
      {:private true,
       :arglists (clojure.core/list ['cluster 'old-root-map 'curr-root-map 'new-root-id 'new-rev]),
       :column (int 1)}
      :name
      'update-and-persist-root-map
      :ns
      *ns*))
  (def prepare-excise-preds
   (fn prepare_excise_preds
     ([unexcised_datoms]
       (let [cmp (get-in index-configs [:eavt :cmp]) sds (sort cmp unexcised_datoms)]
         [(reify
            datomic.excise.ExcisePred
            (ep-remove?
              [this d]
              (<= 0 (Collections/binarySearch ^java.util.List sds d ^java.util.Comparator cmp)))
            (ep-datoms [this] sds))]))))
  (reset-meta!
    #'prepare-excise-preds
    (assoc
      {:private true, :arglists (clojure.core/list ['unexcised-datoms]), :column (int 1)}
      :name
      'prepare-excise-preds
      :ns
      *ns*))
  (def excise-tier
   (fn excise_tier
     ([p__19900 db as_of_t old_root_id config xpreds]
       (let [map__19901 p__19900
             map__19901 (if (seq? map__19901)
                          (if (next map__19901)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19901))
                            (if (seq map__19901) (first map__19901) {}))
                          map__19901)
             cluster (get map__19901 :cluster)
             olookup (get map__19901 :olookup)]
         (merge-one-index
           db
           cluster
           olookup
           old_root_id
           []
           []
           (:part-fn config)
           (:cmp config)
           index/common-write-handlers
           false
           as_of_t
           (:idxcmp config)
           xpreds
           nil
           (:cmpi config))))))
  (reset-meta!
    #'excise-tier
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['cluster 'olookup]} 'db 'as-of-t 'old-root-id 'config 'xpreds]),
       :column (int 1)}
      :name
      'excise-tier
      :ns
      *ns*))
  (defn p
    ([when map]
      (tools/progress
        prn
        (merge
          {:when when}
          (select-keys
            map
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
    (assoc {:arglists (clojure.core/list ['when 'map]), :column (int 1)} :name 'p :ns *ns*))
  (def excise-index*
   (fn excise_index_STAR_
     ([cr db root_map sort_kw_>datoms]
       (let [as_of_t (:basisT root_map)
             vec__19904 (reduce
                          (fn fn__19908
                            ([p__19907 sort]
                              (let [vec__19909 p__19907
                                    m (nth vec__19909 (int 0) nil)
                                    g (nth vec__19909 (int 1) nil)
                                    metrics (nth vec__19909 (int 2) nil)
                                    config (index-configs sort)
                                    hist_idx_root_id (^clojure.lang.IFn root_map
                                                       (:hist-index-key config))
                                    unexcised_datoms (get sort_kw_>datoms sort)
                                    _ (tools/progress
                                        prn
                                        {:phase :repair/index-start,
                                         :index sort,
                                         :as-of-t as_of_t,
                                         :hist-idx-root-id hist_idx_root_id,
                                         :unexcised-datoms
                                         (java.lang.Integer/valueOf
                                           (int (count unexcised_datoms))),
                                         :version 1})
                                    xpreds (prepare-excise-preds unexcised_datoms)
                                    vec__19912 (excise-tier
                                                 cr
                                                 db
                                                 as_of_t
                                                 hist_idx_root_id
                                                 config
                                                 xpreds)
                                    new_hist_idx_root_id (nth vec__19912 (int 0) nil)
                                    garbage_ids (nth vec__19912 (int 1) nil)
                                    _retract (nth vec__19912 (int 2) nil)
                                    tier_metrics (nth vec__19912 (int 3) nil)]
                                (tools/progress
                                  prn
                                  {:phase :repair/index-end,
                                   :index sort,
                                   :old-root-id hist_idx_root_id,
                                   :new-root-id new_hist_idx_root_id,
                                   :version 1})
                                [(assoc m (:hist-index-key config) new_hist_idx_root_id)
                                 (into g garbage_ids)
                                 (index/aggregate-metrics metrics tier_metrics)])))
                          [root_map [] {}]
                          [:eavt :aevt :avet :raet])
             new_root_map (nth vec__19904 (int 0) nil)
             garbage_ids (nth vec__19904 (int 1) nil)
             metrics (nth vec__19904 (int 2) nil)]
         [new_root_map garbage_ids (:written metrics) (:dirs-written metrics)]))))
  (reset-meta!
    #'excise-index*
    (assoc
      {:private true,
       :arglists (clojure.core/list ['cr 'db 'root-map 'sort-kw->datoms]),
       :column (int 1)}
      :name
      'excise-index*
      :ns
      *ns*))
  (def excise-index
   (fn excise_index
     ([p__19917 sort_kw_>datoms max_attempts]
       (let [map__19918 p__19917
             map__19918 (if (seq? map__19918)
                          (if (next map__19918)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19918))
                            (if (seq map__19918) (first map__19918) {}))
                          map__19918)
             cr map__19918
             cluster (get map__19918 :cluster)
             olookup (get map__19918 :olookup)]
         (loop [i 0]
           (if (< i max_attempts)
             (let [index_ref_key_name (index/index-ref-key-name cluster)
                   index_root_ref (deref (cluster/get-ref cluster index_ref_key_name))
                   index_root_id (cluster/val-key->uuid (common/getx index_root_ref :key))
                   ref_rev (common/getx index_root_ref :rev)
                   root_map (common/getx olookup index_root_id)
                   _ (p "old-root-map" root_map)
                   db (dx/get-db-from-connection-resources cr)
                   vec__19919 (excise-index*
                                cr
                                db
                                root_map
                                (set/rename-keys sort_kw_>datoms {:vaet :raet}))
                   new_root_map (nth vec__19919 (int 0) nil)
                   garbage_ids (nth vec__19919 (int 1) nil)
                   segs_written (nth vec__19919 (int 2) nil)
                   dirs_written (nth vec__19919 (int 3) nil)
                   vec__19922 (update-and-persist-root-map
                                cluster
                                root_map
                                new_root_map
                                (common/rand-uuid)
                                (inc (:rev root_map)))
                   new_root_id (nth vec__19922 (int 0) nil)
                   new_root_map (nth vec__19922 (int 1) nil)
                   _ (p "new-root-map" new_root_map)]
               (if (=
                     :ok
                     (deref
                       (cluster/set-ref
                         cluster
                         index_ref_key_name
                         (inc ref_rev)
                         (cluster/uuid->val-key new_root_id))))
                 (do
                   (garbage/mark-garbage cluster olookup garbage_ids)
                   (garbage/flush-garbage cluster olookup)
                   (error (str "Index repair completed."))
                   {:old-root-id index_root_id,
                    :new-root-id new_root_id,
                    :segs-written segs_written,
                    :dirs-written dirs_written})
                 (do
                   (error (str "Conflict updating index root at rev " (inc ref_rev)))
                   (recur (inc i)))))
             (do
               (throw
                 (java.lang.RuntimeException.
                   (str "Could not fix index after " max_attempts " tries. Quitting.")))
               nil)))))))
  (reset-meta!
    #'excise-index
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['cluster 'olookup], :as 'cr} 'sort-kw->datoms 'max-attempts]),
       :column (int 1)}
      :name
      'excise-index
      :ns
      *ns*))
  (def repair-db*
   (fn repair_db_STAR_
     ([uri report max_attempts]
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
       (let [cr (tools/connection-resources uri)
             map__19926 (excise-index (assoc cr :uri uri) (:results report) max_attempts)
             map__19926 (if (seq? map__19926)
                          (if (next map__19926)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19926))
                            (if (seq map__19926) (first map__19926) {}))
                          map__19926)
             segs_written (get map__19926 :segs-written)
             dirs_written (get map__19926 :dirs-written)]
         (tools/progress
           prn
           {:phase :repair/end,
            :segs-written segs_written,
            :dirs-written dirs_written,
            :version 1})))))
  (reset-meta!
    #'repair-db*
    (assoc
      {:arglists (clojure.core/list ['uri 'report 'max-attempts]), :column (int 1)}
      :name
      'repair-db*
      :ns
      *ns*))
  (def repair-db
   (fn repair_db
     ([uri out_filename max_attempts]
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
               (do (dx/write-report report out_filename) (repair-db* uri report max_attempts)))))
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
       (d/shutdown true))))
  (reset-meta!
    #'repair-db
    (assoc
      {:arglists (clojure.core/list ['uri 'out-filename 'max-attempts]), :column (int 1)}
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
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'help :ns *ns*))
  (defn -main
    ([& args]
      (if (>= (count args) 2)
        (let [op (first args)
              uri (second args)
              out_filename (nth args (int 2) nil)
              max_attempts (parse-long (nth args (int 3) ""))
              G__19932 op]
          (case
            G__19932
            "report"
            (if (= (count args) 3)
              (let [summary (dx/write-zombie-datoms-report uri out_filename)]
                (println " ")
                (prn summary)
                (when-not (:ok summary) (error (str "Database needs repair: " uri)))
                (java.lang.System/exit (int (if (:ok summary) 0 1)))
                nil)
              (help))
            "repair"
            (if (= (count args) 3)
              (repair-db uri out_filename DEFAULT_MAX_ATTEMPTS)
              (if (and (= (long (count args)) 4) max_attempts (number? max_attempts))
                (repair-db uri out_filename max_attempts)
                (help)))
            (help)))
        (help))
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))
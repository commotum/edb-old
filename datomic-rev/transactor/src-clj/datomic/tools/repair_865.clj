(do
  (clojure.core/in-ns 'datomic.tools.repair-865)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['clojure.java.io :as 'io]
        ['clojure.set :as 'set]
        ['datomic.api :as 'd]
        ['datomic.cache :as 'cache]
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.db :as 'db]
        ['datomic.garbage :as 'garbage]
        ['datomic.index :as 'index]
        ['datomic.iter :as 'iter]
        ['datomic.log :as 'log]
        ['datomic.memory-size :as 'size]
        ['datomic.slf4j :as 'logger]
        ['datomic.tools :as 'tools]
        ['datomic.tools.detect-865 :as 'detect]
        ['datomic.tools.filter-index :as 'fi])
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'java.io.PushbackReader)
      (clojure.core/import 'datomic.kv_cluster.KVCluster)))
  (when-not (.equals 'datomic.tools.repair-865 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.repair-865))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['clojure.java.io :as 'io]
          ['clojure.set :as 'set]
          ['datomic.api :as 'd]
          ['datomic.cache :as 'cache]
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.db :as 'db]
          ['datomic.garbage :as 'garbage]
          ['datomic.index :as 'index]
          ['datomic.iter :as 'iter]
          ['datomic.log :as 'log]
          ['datomic.memory-size :as 'size]
          ['datomic.slf4j :as 'logger]
          ['datomic.tools :as 'tools]
          ['datomic.tools.detect-865 :as 'detect]
          ['datomic.tools.filter-index :as 'fi])
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'java.io.PushbackReader)
        (clojure.core/import 'datomic.kv_cluster.KVCluster))))
  (set! *warn-on-reflection* true)
  (defn re-id-v
    ([d db m]
      (when (= 20 (.-vtypeid (db/attribute db (.a ^datomic.Datom d))))
        (get m (.v ^datomic.Datom d)))))
  (defn re-id-datom-eids
    ([d db m]
      (let [d (let [temp__5802__auto__ (get m (.e ^datomic.Datom d))]
                (if temp__5802__auto__
                  (let [e temp__5802__auto__
                        datom ((if (.added ^datomic.Datom d)
                                 db/asserting-datum
                                 db/retracting-datum)
                                e
                                (.a ^datomic.Datom d)
                                (.v ^datomic.Datom d)
                                (long (db/eid->eidx (long (.tx ^datomic.Datom d)))))]
                    (tools/progress
                      prn
                      {:phase :repair/progress,
                       :datom/added (tools/pretty-datom db datom),
                       :reason :re-id,
                       :from (.e ^datomic.Datom d),
                       :to e})
                    datom)
                  d))
            temp__5802__auto__ (re-id-v d db m)]
        (if temp__5802__auto__
          (let [v temp__5802__auto__
                datom ((if (.added ^datomic.Datom d) db/asserting-datum db/retracting-datum)
                        (.e ^datomic.Datom d)
                        (.a ^datomic.Datom d)
                        v
                        (long (db/eid->eidx (long (.tx ^datomic.Datom d)))))]
            (tools/progress
              prn
              {:phase :repair/progress,
               :datom/added (tools/pretty-datom db datom),
               :reason :re-id,
               :from (.v ^datomic.Datom d),
               :to v})
            datom)
          d))))
  (defn create-data-re-id-er
    ([ts m cant_merge unfixed_counter]
      (let [dest_es (into #{} (vals m)) cant_es (into #{} (keys cant_merge))]
        (fn fn__30746
          ([db _ tx_t data]
            (let [prev_db (d/as-of db (dec tx_t))]
              (into
                []
                (reduce
                  (fn fn__30747
                    ([data d]
                      (if (and
                            (contains? cant_es (.e ^datomic.Datom d))
                            (contains? ts (long (db/eid->eidx (long (.tx ^datomic.Datom d))))))
                        (let [sysd (db/asserting-datum
                                     (long (.tx ^datomic.Datom d))
                                     8
                                     true
                                     (db/eid->eidx (long (.tx ^datomic.Datom d))))]
                          (swap! unfixed_counter inc)
                          (tools/progress
                            prn
                            {:phase :repair/progress, :datom/unfixed (tools/pretty-datom db d)})
                          (if (contains? data sysd)
                            data
                            (do
                              (tools/progress
                                prn
                                {:phase :repair/progress,
                                 :datom/added (tools/pretty-datom db sysd),
                                 :reason :datom-skipped})
                              (conj data sysd))))
                        (if (and
                              (contains? dest_es (.e ^datomic.Datom d))
                              (.added ^datomic.Datom d)
                              (or
                                (first
                                  (d/datoms
                                    prev_db
                                    :eavt
                                    (.e ^datomic.Datom d)
                                    (.a ^datomic.Datom d)
                                    (.v ^datomic.Datom d)))
                                (and
                                  (= 20 (.-vtypeid (db/attribute db (.a ^datomic.Datom d))))
                                  (first
                                    (d/datoms
                                      prev_db
                                      :vaet
                                      (.v ^datomic.Datom d)
                                      (.a ^datomic.Datom d)
                                      (.e ^datomic.Datom d))))))
                          (do
                            (tools/progress
                              prn
                              {:phase :repair/progress, :datom/skipped (tools/pretty-datom db d)})
                            data)
                          (let [data (conj data (re-id-datom-eids d db m))
                                temp__5802__auto__ (get m (.e ^datomic.Datom d))]
                            (if temp__5802__auto__
                              (let [e temp__5802__auto__]
                                (if (first (d/datoms db :eavt (.e ^datomic.Datom d) 9 e))
                                  data
                                  (let [sysd (db/asserting-datum
                                               (long (.e ^datomic.Datom d))
                                               9
                                               e
                                               (db/eid->eidx (long (.tx ^datomic.Datom d))))]
                                    (if (contains? data sysd)
                                      data
                                      (do
                                        (tools/progress
                                          prn
                                          {:phase :repair/progress,
                                           :datom/added (tools/pretty-datom db sysd),
                                           :reason :re-id})
                                        (conj data sysd))))))
                              data))))))
                  #{}
                  data))))))))
  (deftype
    EAof
    [d]
    java.lang.Object
    (^int hashCode
      [this]
      (int (bit-xor (hash (.e ^datomic.Datom d)) (hash (.a ^datomic.Datom d)))))
    (^boolean equals
      [this other]
      (let [o (.-d ^EAof other)]
        (and
          (= (.e ^datomic.Datom d) (.e ^datomic.Datom o))
          (= (.a ^datomic.Datom d) (.a ^datomic.Datom o))))))
  (clojure.core/import 'datomic.tools.repair_865.EAof)
  (defn ->EAof ([d] (datomic.tools.repair_865.EAof. d)))
  (defonce IAssertionCache {})
  (defprotocol
    IAssertionCache
    (get-latest-assertion [cache d db])
    (update-latest-assertions [cached db data]))
  (deftype
    AssertionCache
    [cache asserts retracts misses]
    java.lang.Object
    datomic.tools.repair_865.IAssertionCache
    (update-latest-assertions
      [this db data]
      (reduce
        (fn fn__30801
          ([cache d]
            (when (= (.-cardinality (db/attribute db (.a ^datomic.Datom d))) 35)
              (let [ea (->EAof d)]
                (if (.added ^datomic.Datom d)
                  (cache/put cache ea d)
                  (let [ed (get cache (->EAof d))]
                    (when (and
                            (instance? datomic.Datom ed)
                            (= (.v ^datomic.Datom ed) (.v ^datomic.Datom d)))
                      (cache/put cache ea d))))))
            cache))
        cache
        data))
    (get-latest-assertion
      [this d db]
      (let [d d ea (->EAof d) ed (get cache ea)]
        (if ed
          (if (.added ^datomic.Datom ed) (do (swap! asserts inc) ed) (do (swap! retracts inc) nil))
          (do
            (swap! misses inc)
            (first (d/datoms db :eavt (.e ^datomic.Datom d) (.a ^datomic.Datom d)))))))
    (^java.lang.String toString
      [this]
      (str
        {:asserts (deref asserts),
         :retracts (deref retracts),
         :misses (deref misses),
         :size (cache/fast-count cache)})))
  (clojure.core/import 'datomic.tools.repair_865.AssertionCache)
  (defn ->AssertionCache
    ([cache asserts retracts misses]
      (datomic.tools.repair_865.AssertionCache. cache asserts retracts misses)))
  (defn create-assertion-cache
    ([mb]
      (->AssertionCache
        (cache/create-scaled-weight-limited
          (* (* mb 1024) 1024)
          (fn fn__30808 ([k v] (+ (+ 64 (size/memory-size k)) (size/memory-size v))))
          1000)
        (atom 0)
        (atom 0)
        (atom 0))))
  (defn missing-tombstones
    ([db basis_t tx_t data cache]
      (let [retracts (reduce
                       (fn fn__30811
                         ([s d]
                           (if (and
                                 (not (.added ^datomic.Datom d))
                                 (= (.-cardinality (db/attribute db (.a ^datomic.Datom d))) 35))
                             (conj s d)
                             s)))
                       #{}
                       data)]
        (reduce
          (fn fn__30814
            ([v d]
              (let [asof (d/as-of db (dec tx_t))
                    ed (and
                         (.added ^datomic.Datom d)
                         (= (.-cardinality (db/attribute db (.a ^datomic.Datom d))) 35)
                         (get-latest-assertion cache d asof))
                    ed (when (and ed (<= 1000 (db/eid->eidx (long (.tx ^datomic.Datom ed))))) ed)]
                (if ed
                  (let [retract (db/retracting-datum
                                  (long (.e ^datomic.Datom d))
                                  (long (.a ^datomic.Datom d))
                                  (.v ^datomic.Datom ed)
                                  (db/eid->eidx (long (.tx ^datomic.Datom d))))]
                    (if (get retracts retract) v (conj v retract)))
                  v))))
          []
          data))))
  (defn create-retombstoner
    ([cache]
      (fn fn__30820
        ([db basis_t tx_t data]
          (let [stones (missing-tombstones db basis_t tx_t data cache)]
            (update-latest-assertions cache db (concat data stones))
            (if (seq stones)
              (do
                (loop [seq_30821 (seq stones) chunk_30822 nil count_30823 0 i_30824 0]
                  (if (< i_30824 count_30823)
                    (let [stone (.nth ^clojure.lang.Indexed chunk_30822 (int i_30824))]
                      (tools/progress
                        prn
                        {:phase :repair/progress,
                         :datom/added (tools/pretty-datom db stone),
                         :reason :card-1-retraction})
                      (recur seq_30821 chunk_30822 count_30823 (inc i_30824)))
                    (let [temp__5804__auto__ (seq seq_30821)]
                      (when temp__5804__auto__
                        (let [seq_30821 temp__5804__auto__]
                          (if (chunked-seq? seq_30821)
                            (let [c__6065__auto__ (chunk-first seq_30821)]
                              (recur
                                (chunk-rest seq_30821)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [stone (first seq_30821)]
                              (tools/progress
                                prn
                                {:phase :repair/progress,
                                 :datom/added (tools/pretty-datom db stone),
                                 :reason :card-1-retraction})
                              (recur (next seq_30821) nil 0 0))))))))
                (concat data stones))
              data))))))
  (defn memory-db
    ([db] (d/history (assoc db :index nil :mid-index nil :indexing nil :history nil))))
  (defn indexing-db
    ([db] (d/history (assoc db :index nil :mid-index nil :memdb nil :history nil))))
  (defn rebuild-index
    ([p__30831 p__30832 old_index_id mem_index_max t indexed transforms]
      (let [map__30833 p__30831
            map__30833 (if (seq? map__30833)
                         (if (next map__30833)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30833))
                           (if (seq map__30833) (first map__30833) {}))
                         map__30833)
            cr map__30833
            cluster (get map__30833 :cluster)
            olookup (get map__30833 :olookup)
            map__30834 p__30832
            map__30834 (if (seq? map__30834)
                         (if (next map__30834)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30834))
                           (if (seq map__30834) (first map__30834) {}))
                         map__30834)
            db (get map__30834 :db)
            log (get map__30834 :log)
            do_merge (fn do_merge
                       ([db base_index_id]
                         (let [m_30836 {:event :reindex/merge-db,
                                        :basis-t (d/basis-t db),
                                        :next-t (d/next-t db)}
                               ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                              "datomic.tools.repair-865")]
                                                 (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                   (.info
                                                     ^org.slf4j.Logger logger
                                                     (logger/process
                                                       (assoc m_30836 :phase :begin))))
                                                 nil)
                               start__8584__auto__ (java.lang.System/nanoTime)
                               result__8585__auto__ (try
                                                      {:returned
                                                       (let [db (db/prepare-for-indexing db)
                                                             vec__30840
                                                             (index/merge-db*
                                                               cluster
                                                               olookup
                                                               db
                                                               (d/next-t db)
                                                               base_index_id
                                                               #:reindex{:basis t}
                                                               false
                                                               false)
                                                             index_id (nth vec__30840 (int 0) nil)
                                                             _ (nth vec__30840 (int 1) nil)
                                                             garbage (nth vec__30840 (int 2) nil)]
                                                         [index_id
                                                          garbage
                                                          (db/complete-indexing
                                                            db
                                                            (index/load-index olookup index_id))])}
                                                      (catch
                                                        java.lang.Throwable
                                                        t__8586__auto__
                                                        {:threw t__8586__auto__}))
                               elapsed_30837 (- (java.lang.System/nanoTime) start__8584__auto__)
                               msec_30838 (logger/format-as-msec (long elapsed_30837))]
                           (let [endmsg__8587__auto__ (merge
                                                        (assoc
                                                          m_30836
                                                          :msec
                                                          msec_30838
                                                          :phase
                                                          :end)
                                                        (when (:threw result__8585__auto__)
                                                          {:threw
                                                           (class (:threw result__8585__auto__))}))
                                 logger (org.slf4j.LoggerFactory/getLogger
                                          "datomic.tools.repair-865")]
                             (when (.isInfoEnabled ^org.slf4j.Logger logger)
                               (.info
                                 ^org.slf4j.Logger logger
                                 (logger/process endmsg__8587__auto__)))
                             nil)
                           (if (contains? result__8585__auto__ :returned)
                             (:returned result__8585__auto__)
                             (do (throw (:threw result__8585__auto__)) nil)))))
            log log
            db (assoc db :nextT t)
            basis_t nil
            size 0
            index_id old_index_id
            garbage []
            G__30852 (tools/tx-range-from-log cr t nil)
            vec__30853 G__30852
            seq__30854 (seq vec__30853)
            first__30855 (first seq__30854)
            seq__30854 (next seq__30854)
            tx first__30855
            more seq__30854]
        (loop [log log
               db db
               basis_t basis_t
               size size
               index_id index_id
               garbage garbage
               G__30852 G__30852]
          (let [log log
                db db
                basis_t basis_t
                size size
                index_id index_id
                garbage garbage
                vec__30856 G__30852
                seq__30857 (seq vec__30856)
                first__30858 (first seq__30857)
                seq__30857 (next seq__30857)
                tx first__30858
                more seq__30857]
            (if tx
              (if (< mem_index_max size)
                (let [vec__30859 (^clojure.lang.IFn do_merge db index_id)
                      index_id (nth vec__30859 (int 0) nil)
                      new_garbage (nth vec__30859 (int 1) nil)
                      db (nth vec__30859 (int 2) nil)
                      log (tools/log cr)
                      next_t (d/next-t db)]
                  (^clojure.lang.IFn indexed [next_t (log/segmented-basis-t log)])
                  (recur
                    log
                    db
                    (d/basis-t db)
                    0
                    index_id
                    (into garbage new_garbage)
                    (seq (tools/tx-range-from-log cr next_t nil))))
                (let [tx_next_t (tools/log-entry->next-t tx)
                      data (reduce
                             (fn fn__30862
                               ([data xform] (^clojure.lang.IFn xform db basis_t (:t tx) data)))
                             (:data tx)
                             transforms)
                      db (.acceptDataCheck ^datomic.db.IDbImpl db data false)
                      db (assoc db :nextT tx_next_t)]
                  (recur
                    log
                    db
                    (d/basis-t db)
                    (+ size (long (size/memory-size data)))
                    index_id
                    garbage
                    more)))
              (if (= size 0)
                (when index_id [(cluster/uuid->val-key index_id) garbage])
                (let [vec__30864 (^clojure.lang.IFn do_merge db index_id)
                      index_id (nth vec__30864 (int 0) nil)
                      new_garbage (nth vec__30864 (int 1) nil)]
                  [(cluster/uuid->val-key index_id) (into garbage new_garbage)]))))))))
  (defn progress ([x] (tools/progress tools/println-err x)))
  (reset-meta!
    #'progress
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column 1}
      :name
      'progress
      :ns
      *ns*))
  (defn filter-and-rebuild
    ([cr dbr mem_index_mb t transforms]
      (let [done (promise)]
        (try
          (let [map__30869 cr
                map__30869 (if (seq? map__30869)
                             (if (next map__30869)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__30869))
                               (if (seq map__30869) (first map__30869) {}))
                             map__30869)
                cluster (get map__30869 :cluster)
                olookup (get map__30869 :olookup)
                map__30870 dbr
                map__30870 (if (seq? map__30870)
                             (if (next map__30870)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__30870))
                               (if (seq map__30870) (first map__30870) {}))
                             map__30870)
                db (get map__30870 :db)
                basis_t (d/basis-t db)
                indexed_state (atom [t basis_t])
                indexed (partial reset! indexed_state)
                _ (future-call
                    (fn fn__30874
                      ([]
                        (try
                          (loop []
                            (do
                              (java.lang.Thread/sleep 15000)
                              (when-not (realized? done)
                                (let [vec__30875 (deref indexed_state)
                                      thru (nth vec__30875 (int 0) nil)
                                      goal (nth vec__30875 (int 1) nil)]
                                  (progress
                                    (str
                                      "Indexed through t "
                                      thru
                                      " of "
                                      goal
                                      ". "
                                      (deref cluster/segment-writes)
                                      " segments written."))
                                  (recur)))))
                          (catch
                            java.lang.Throwable
                            t__8829__auto__
                            (do
                              (let [logger (org.slf4j.LoggerFactory/getLogger
                                             "datomic.tools.repair-865")
                                    ex t__8829__auto__]
                                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                  (.warn
                                    ^org.slf4j.Logger logger
                                    (logger/process "error executing future")
                                    ^java.lang.Throwable ex)
                                  (logger/caused-by logger ex))
                                nil)
                              (datomic.monitor/alarm :UnhandledException)
                              (throw ^java.lang.Throwable t__8829__auto__)
                              nil))))))
                vec__30871 (fi/filter-index
                             (:key (tools/get-index-ref cluster))
                             cluster
                             olookup
                             (fn fn__30879 ([d] (<= t (d/tx->t (:tx d))))))
                index_id (nth vec__30871 (int 0) nil)
                filt_garbage (nth vec__30871 (int 1) nil)
                vec__30881 (rebuild-index
                             cr
                             dbr
                             index_id
                             (* (* mem_index_mb 1024) 1024)
                             t
                             indexed
                             transforms)
                index_id (nth vec__30881 (int 0) nil)
                index_garbage (nth vec__30881 (int 1) nil)]
            [index_id (concat filt_garbage index_garbage)])
          (finally (deliver done true))))))
  (defn read-desc-file
    ([f]
      (with-open [f (io/reader f)]
        (do
          (let [rdr (java.io.PushbackReader. ^java.io.Reader f)]
            (loop [ts (sorted-set) as #{} version nil]
              (let [temp__5802__auto__ (edn/read {:eof nil} rdr)]
                (if temp__5802__auto__
                  (let [form temp__5802__auto__]
                    (recur
                      (let [temp__5802__auto__ (:t form)]
                        (if temp__5802__auto__ (let [t temp__5802__auto__] (conj ts t)) ts))
                      (let [temp__5802__auto__ (:unique-attributes form)]
                        (if temp__5802__auto__ (let [a temp__5802__auto__] (concat as a)) as))
                      (if (= :detect/end (:phase form))
                        (do
                          (tools/progress
                            prn
                            {:phase :repair/progress, :detect/version (:version form)})
                          (:version form))
                        version)))
                  (if (= 1 version)
                    {:ts ts, :as as}
                    (do
                      (throw
                        (java.lang.RuntimeException.
                          "Input file not in format expected. Please verify that the input file is produced by the same version of detect"))
                      nil))))))
          nil))))
  (defn as->aids
    ([db as]
      (let [aids (into #{} (mapv (fn fn__30890 ([p1__30889#] (d/entid db p1__30889#))) as))]
        (when (some nil? aids)
          (throw (java.lang.RuntimeException. (str "Invalid attribute in list: " (pr-str as)))))
        (let [temp__5804__auto__ (seq
                                   (set/difference aids (into #{} (tools/unique-identities db))))]
          (when temp__5804__auto__
            (let [diff temp__5804__auto__]
              (throw
                (java.lang.RuntimeException.
                  (str "Not unique identity attribute ids: " (pr-str diff))))))
          nil)
        aids)))
  (reset-meta!
    #'as->aids
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'as]), :column 1}
      :name
      'as->aids
      :ns
      *ns*))
  (defn first-indexed-t-in-es
    ([db es]
      (let [temp__5804__auto__ (seq
                                 (map
                                   (fn fn__30894 ([d] (long (d/tx->t (.tx ^datomic.Datom d)))))
                                   (mapcat
                                     (fn fn__30896 ([e] (d/datoms (d/history db) :eavt e)))
                                     es)))]
        (when temp__5804__auto__ (let [ts temp__5804__auto__] (apply min ts))))))
  (defn -main*
    ([uri mem_index_mb p__30900]
      (let [map__30901 p__30900
            map__30901 (if (seq? map__30901)
                         (if (next map__30901)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30901))
                           (if (seq map__30901) (first map__30901) {}))
                         map__30901)
            ts (get map__30901 :ts)
            as (get map__30901 :as)]
        (when-not (<= 1000 (first ts))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str (clojure.core/list '<= 1000 (clojure.core/list 'first 'ts)))))))
        (progress
          (str
            "Rebuilding index for "
            uri
            " starting at t "
            (first ts)
            ". This may take a long time!"))
        (tools/progress
          prn
          {:phase :repair/start,
           :start-t (first ts),
           :end-t (last ts),
           :unique-attributes as,
           :uri uri,
           :version 1})
        (let [map__30902 (tools/connection-resources uri)
              map__30902 (if (seq? map__30902)
                           (if (next map__30902)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__30902))
                             (if (seq map__30902) (first map__30902) {}))
                           map__30902)
              cr map__30902
              cluster (get map__30902 :cluster)
              olookup (get map__30902 :olookup)
              map__30903 (tools/db-resources cr)
              map__30903 (if (seq? map__30903)
                           (if (next map__30903)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__30903))
                             (if (seq map__30903) (first map__30903) {}))
                           map__30903)
              db (get map__30903 :db)
              log (get map__30903 :log)
              aids (as->aids db as)
              vec__30904 (detect/precise-identity-substitutions
                           (merge
                             (tools/identities-in-ts db log ts)
                             (tools/values-in-ts db log ts))
                           db
                           aids)
              submap (nth vec__30904 (int 0) nil)
              cantsub (nth vec__30904 (int 1) nil)
              _ (tools/progress prn {:phase :repair/plan, :submap submap, :cantsub cantsub})
              cache (create-assertion-cache 10)
              unfixed_counter (atom 0)
              xforms (if (or (seq submap) (seq cantsub))
                       [(create-data-re-id-er ts submap cantsub unfixed_counter)
                        (create-retombstoner cache)]
                       [(create-retombstoner cache)])
              identity_t (first-indexed-t-in-es db (keys submap))
              _ (when identity_t
                  (tools/progress prn {:phase :repair/identity-t, :identity-t identity_t}))
              rebuild_from_t (if identity_t (min identity_t (first ts)) (first ts))
              vec__30907 (filter-and-rebuild
                           cr
                           {:db (tools/index-db cr), :log log}
                           mem_index_mb
                           rebuild_from_t
                           xforms)
              index_id (nth vec__30907 (int 0) nil)
              garbage (nth vec__30907 (int 1) nil)]
          (if index_id
            (do
              (tools/replace-index cluster index_id)
              (tools/segment-log uri)
              (tools/touch-heartbeat uri)
              (garbage/mark-garbage cluster olookup garbage)
              (garbage/flush-garbage cluster olookup)
              (progress (str "Unfixed: " (deref unfixed_counter) " datoms."))
              (progress "Index rebuild completed. Transactor will shutdown (or HA restart) now.")
              (tools/flush-progress))
            (progress "Index rebuild completed with no changes."))
          (tools/progress
            prn
            {:phase :repair/end,
             :sub-es submap,
             :skip-es cantsub,
             :unfixed (deref unfixed_counter),
             :index-id index_id,
             :version 1})))))
  (defn -main
    ([uri mem_index_mb detectfile]
      (try
        (-main* uri (edn/read-string mem_index_mb) (read-desc-file detectfile))
        (catch
          java.lang.Throwable
          t
          (do
            (.printStackTrace ^java.lang.Throwable t)
            (tools/flush-progress)
            (d/shutdown true)
            (java.lang.System/exit (int -1))
            nil)))
      (tools/flush-progress)
      (d/shutdown true)
      (java.lang.System/exit (int 0))
      nil)))
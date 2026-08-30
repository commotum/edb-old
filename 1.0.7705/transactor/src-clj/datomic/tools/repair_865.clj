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
  (def re-id-v
   (fn re_id_v
     ([d db m]
       (when (= 20 (.-vtypeid (db/attribute db (.a ^datomic.Datom d))))
         (get m (.v ^datomic.Datom d))))))
  (reset-meta!
    #'re-id-v
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'd {:tag 'Datom}) 'db 'm]), :column (int 1)}
      :name
      're-id-v
      :ns
      *ns*))
  (def re-id-datom-eids
   (fn re_id_datom_eids
     ([d db m]
       (let [d (let [temp__5823__auto__ (get m (.e ^datomic.Datom d))]
                 (if temp__5823__auto__
                   (let [e temp__5823__auto__
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
             temp__5823__auto__ (re-id-v d db m)]
         (if temp__5823__auto__
           (let [v temp__5823__auto__
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
           d)))))
  (reset-meta!
    #'re-id-datom-eids
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'd {:tag 'Datom}) 'db 'm]), :column (int 1)}
      :name
      're-id-datom-eids
      :ns
      *ns*))
  (def create-data-re-id-er
   (fn create_data_re_id_er
     ([ts m cant_merge unfixed_counter]
       (let [dest_es (into #{} (vals m)) cant_es (into #{} (keys cant_merge))]
         (fn fn__31603
           ([db _ tx_t data]
             (let [prev_db (d/as-of db (dec tx_t))]
               (into
                 []
                 (reduce
                   (fn fn__31604
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
                                 temp__5823__auto__ (get m (.e ^datomic.Datom d))]
                             (if temp__5823__auto__
                               (let [e temp__5823__auto__]
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
                   data)))))))))
  (reset-meta!
    #'create-data-re-id-er
    (assoc
      {:arglists (clojure.core/list ['ts 'm 'cant-merge 'unfixed-counter]), :column (int 1)}
      :name
      'create-data-re-id-er
      :ns
      *ns*))
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
  (reset-meta!
    #'->EAof
    (assoc {:arglists (clojure.core/list ['d]), :column (int 1)} :name '->EAof :ns *ns*))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      IAssertionCache
      (get-latest-assertion [cache d db])
      (update-latest-assertions [cached db data]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.tools.repair-865" "IAssertionCache")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'IAssertionCache :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-latest-assertion
                                        {:arglists (clojure.core/list ['cache 'd 'db])}),
                                      :arglists (clojure.core/list ['cache 'd 'db]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.tools.repair-865"
                                       "IAssertionCache"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.tools.repair-865" "get-latest-assertion")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*)))
    (let [protocol_signature__7434 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'update-latest-assertions
                                        {:arglists (clojure.core/list ['cached 'db 'data])}),
                                      :arglists (clojure.core/list ['cached 'db 'data]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.tools.repair-865"
                                       "IAssertionCache"))
          protocol_method_name__7435 (with-meta
                                       (:name protocol_signature__7434)
                                       protocol_signature__7434)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.tools.repair-865" "update-latest-assertions")
        (assoc protocol_signature__7434 :name protocol_method_name__7435 :ns *ns*))))
  (deftype
    AssertionCache
    [cache asserts retracts misses]
    java.lang.Object
    datomic.tools.repair_865.IAssertionCache
    (update-latest-assertions
      [this db data]
      (reduce
        (fn fn__31658
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
  (reset-meta!
    #'->AssertionCache
    (assoc
      {:arglists (clojure.core/list ['cache 'asserts 'retracts 'misses]), :column (int 1)}
      :name
      '->AssertionCache
      :ns
      *ns*))
  (defn create-assertion-cache
    ([mb]
      (->AssertionCache
        (cache/create-scaled-weight-limited
          (* (* mb 1024) 1024)
          (fn fn__31665 ([k v] (long (+ (+ 64 (size/memory-size k)) (size/memory-size v)))))
          1000)
        (atom 0)
        (atom 0)
        (atom 0))))
  (reset-meta!
    #'create-assertion-cache
    (assoc
      {:arglists (clojure.core/list ['mb]), :column (int 1)}
      :name
      'create-assertion-cache
      :ns
      *ns*))
  (def missing-tombstones
   (fn missing_tombstones
     ([db basis_t tx_t data cache]
       (let [retracts (reduce
                        (fn fn__31668
                          ([s d]
                            (if (and
                                  (not (.added ^datomic.Datom d))
                                  (= (.-cardinality (db/attribute db (.a ^datomic.Datom d))) 35))
                              (conj s d)
                              s)))
                        #{}
                        data)]
         (reduce
           (fn fn__31671
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
           data)))))
  (reset-meta!
    #'missing-tombstones
    (assoc
      {:arglists (clojure.core/list ['db 'basis-t 'tx-t 'data 'cache]), :column (int 1)}
      :name
      'missing-tombstones
      :ns
      *ns*))
  (defn create-retombstoner
    ([cache]
      (fn fn__31677
        ([db basis_t tx_t data]
          (let [stones (missing-tombstones db basis_t tx_t data cache)]
            (update-latest-assertions cache db (concat data stones))
            (if (seq stones)
              (do
                (loop [seq_31678 (seq stones) chunk_31679 nil count_31680 0 i_31681 0]
                  (if (< i_31681 count_31680)
                    (let [stone (.nth ^clojure.lang.Indexed chunk_31679 (int i_31681))]
                      (tools/progress
                        prn
                        {:phase :repair/progress,
                         :datom/added (tools/pretty-datom db stone),
                         :reason :card-1-retraction})
                      (recur seq_31678 chunk_31679 count_31680 (inc i_31681)))
                    (let [temp__5825__auto__ (seq seq_31678)]
                      (when temp__5825__auto__
                        (let [seq_31678 temp__5825__auto__]
                          (if (chunked-seq? seq_31678)
                            (let [c__6090__auto__ (chunk-first seq_31678)]
                              (recur
                                (chunk-rest seq_31678)
                                c__6090__auto__
                                (int (count c__6090__auto__))
                                (int 0)))
                            (let [stone (first seq_31678)]
                              (tools/progress
                                prn
                                {:phase :repair/progress,
                                 :datom/added (tools/pretty-datom db stone),
                                 :reason :card-1-retraction})
                              (recur (next seq_31678) nil 0 0))))))))
                (concat data stones))
              data))))))
  (reset-meta!
    #'create-retombstoner
    (assoc
      {:arglists (clojure.core/list ['cache]), :column (int 1)}
      :name
      'create-retombstoner
      :ns
      *ns*))
  (defn memory-db
    ([db] (d/history (assoc db :index nil :mid-index nil :indexing nil :history nil))))
  (reset-meta!
    #'memory-db
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'memory-db :ns *ns*))
  (defn indexing-db
    ([db] (d/history (assoc db :index nil :mid-index nil :memdb nil :history nil))))
  (reset-meta!
    #'indexing-db
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'indexing-db :ns *ns*))
  (def rebuild-index
   (fn rebuild_index
     ([p__31688 p__31689 old_index_id mem_index_max t indexed transforms]
       (let [map__31690 p__31688
             map__31690 (if (seq? map__31690)
                          (if (next map__31690)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31690))
                            (if (seq map__31690) (first map__31690) {}))
                          map__31690)
             cr map__31690
             cluster (get map__31690 :cluster)
             olookup (get map__31690 :olookup)
             map__31691 p__31689
             map__31691 (if (seq? map__31691)
                          (if (next map__31691)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31691))
                            (if (seq map__31691) (first map__31691) {}))
                          map__31691)
             db (get map__31691 :db)
             log (get map__31691 :log)
             do_merge (fn do_merge
                        ([db base_index_id]
                          (let [m_31693 {:event :reindex/merge-db,
                                         :basis-t (d/basis-t db),
                                         :next-t (d/next-t db)}
                                ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.tools.repair-865")]
                                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                    (.info
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        (assoc m_31693 :phase :begin))))
                                                  nil)
                                start__8599__auto__ (java.lang.System/nanoTime)
                                result__8600__auto__ (try
                                                       {:returned
                                                        (let [db (db/prepare-for-indexing db)
                                                              vec__31697
                                                              (index/merge-db*
                                                                cluster
                                                                olookup
                                                                db
                                                                (d/next-t db)
                                                                base_index_id
                                                                #:reindex{:basis t}
                                                                false
                                                                false)
                                                              index_id (nth vec__31697 (int 0) nil)
                                                              _ (nth vec__31697 (int 1) nil)
                                                              garbage (nth vec__31697 (int 2) nil)]
                                                          [index_id
                                                           garbage
                                                           (db/complete-indexing
                                                             db
                                                             (index/load-index
                                                               olookup
                                                               index_id))])}
                                                       (catch
                                                         java.lang.Throwable
                                                         t__8601__auto__
                                                         {:threw t__8601__auto__}))
                                elapsed_31694 (- (java.lang.System/nanoTime) start__8599__auto__)
                                msec_31695 (logger/format-as-msec (long elapsed_31694))]
                            (let [endmsg__8602__auto__ (merge
                                                         (assoc
                                                           m_31693
                                                           :msec
                                                           msec_31695
                                                           :phase
                                                           :end)
                                                         (when (:threw result__8600__auto__)
                                                           {:threw
                                                            (class
                                                              (:threw result__8600__auto__))}))
                                  logger (org.slf4j.LoggerFactory/getLogger
                                           "datomic.tools.repair-865")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process endmsg__8602__auto__)))
                              nil)
                            (if (contains? result__8600__auto__ :returned)
                              (:returned result__8600__auto__)
                              (do (throw (:threw result__8600__auto__)) nil)))))
             log log
             db (assoc db :nextT t)
             basis_t nil
             size 0
             index_id old_index_id
             garbage []
             G__31709 (tools/tx-range-from-log cr t nil)
             vec__31710 G__31709
             seq__31711 (seq vec__31710)
             first__31712 (first seq__31711)
             seq__31711 (next seq__31711)
             tx first__31712
             more seq__31711]
         (loop [log log
                db db
                basis_t basis_t
                size size
                index_id index_id
                garbage garbage
                G__31709 G__31709]
           (let [log log
                 db db
                 basis_t basis_t
                 size size
                 index_id index_id
                 garbage garbage
                 vec__31713 G__31709
                 seq__31714 (seq vec__31713)
                 first__31715 (first seq__31714)
                 seq__31714 (next seq__31714)
                 tx first__31715
                 more seq__31714]
             (if tx
               (if (< mem_index_max size)
                 (let [vec__31716 (^clojure.lang.IFn do_merge db index_id)
                       index_id (nth vec__31716 (int 0) nil)
                       new_garbage (nth vec__31716 (int 1) nil)
                       db (nth vec__31716 (int 2) nil)
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
                              (fn fn__31719
                                ([data xform] (^clojure.lang.IFn xform db basis_t (:t tx) data)))
                              (:data tx)
                              transforms)
                       db (.acceptDataCheck ^datomic.db.IDbImpl db data false)
                       db (assoc db :nextT tx_next_t)]
                   (recur
                     log
                     db
                     (d/basis-t db)
                     (+ size (size/memory-size data))
                     index_id
                     garbage
                     more)))
               (if (= size 0)
                 (when index_id [(cluster/uuid->val-key index_id) garbage])
                 (let [vec__31721 (^clojure.lang.IFn do_merge db index_id)
                       index_id (nth vec__31721 (int 0) nil)
                       new_garbage (nth vec__31721 (int 1) nil)]
                   [(cluster/uuid->val-key index_id) (into garbage new_garbage)])))))))))
  (reset-meta!
    #'rebuild-index
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['cluster 'olookup], :as 'cr}
          {:keys ['db 'log]}
          'old-index-id
          'mem-index-max
          't
          'indexed
          'transforms]),
       :column (int 1)}
      :name
      'rebuild-index
      :ns
      *ns*))
  (defn progress ([x] (tools/progress tools/println-err x)))
  (reset-meta!
    #'progress
    (assoc
      {:private true, :arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'progress
      :ns
      *ns*))
  (def filter-and-rebuild
   (fn filter_and_rebuild
     ([cr dbr mem_index_mb t transforms]
       (let [done (promise)]
         (try
           (let [map__31726 cr
                 map__31726 (if (seq? map__31726)
                              (if (next map__31726)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__31726))
                                (if (seq map__31726) (first map__31726) {}))
                              map__31726)
                 cluster (get map__31726 :cluster)
                 olookup (get map__31726 :olookup)
                 map__31727 dbr
                 map__31727 (if (seq? map__31727)
                              (if (next map__31727)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__31727))
                                (if (seq map__31727) (first map__31727) {}))
                              map__31727)
                 db (get map__31727 :db)
                 basis_t (d/basis-t db)
                 indexed_state (atom [t basis_t])
                 indexed (partial reset! indexed_state)
                 _ (future-call
                     (fn fn__31731
                       ([]
                         (try
                           (loop []
                             (do
                               (java.lang.Thread/sleep 15000)
                               (when-not (realized? done)
                                 (let [vec__31732 (deref indexed_state)
                                       thru (nth vec__31732 (int 0) nil)
                                       goal (nth vec__31732 (int 1) nil)]
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
                             t__8765__auto__
                             (do
                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                              "datomic.tools.repair-865")
                                     ex t__8765__auto__]
                                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                   (.warn
                                     ^org.slf4j.Logger logger
                                     (logger/process "error executing future")
                                     ^java.lang.Throwable ex)
                                   (logger/caused-by logger ex))
                                 nil)
                               (datomic.monitor/alarm :UnhandledException)
                               (throw ^java.lang.Throwable t__8765__auto__)
                               nil))))))
                 vec__31728 (fi/filter-index
                              (:key (tools/get-index-ref cluster))
                              cluster
                              olookup
                              (fn fn__31736 ([d] (<= t (d/tx->t (:tx d))))))
                 index_id (nth vec__31728 (int 0) nil)
                 filt_garbage (nth vec__31728 (int 1) nil)
                 vec__31738 (rebuild-index
                              cr
                              dbr
                              index_id
                              (* (* mem_index_mb 1024) 1024)
                              t
                              indexed
                              transforms)
                 index_id (nth vec__31738 (int 0) nil)
                 index_garbage (nth vec__31738 (int 1) nil)]
             [index_id (concat filt_garbage index_garbage)])
           (finally (deliver done true)))))))
  (reset-meta!
    #'filter-and-rebuild
    (assoc
      {:arglists (clojure.core/list ['cr 'dbr 'mem-index-mb 't 'transforms]), :column (int 1)}
      :name
      'filter-and-rebuild
      :ns
      *ns*))
  (defn read-desc-file
    ([f]
      (with-open [f (io/reader f)]
        (do
          (let [rdr (java.io.PushbackReader. ^java.io.Reader f)]
            (loop [ts (sorted-set) as #{} version nil]
              (let [temp__5823__auto__ (edn/read {:eof nil} rdr)]
                (if temp__5823__auto__
                  (let [form temp__5823__auto__]
                    (recur
                      (let [temp__5823__auto__ (:t form)]
                        (if temp__5823__auto__ (let [t temp__5823__auto__] (conj ts t)) ts))
                      (let [temp__5823__auto__ (:unique-attributes form)]
                        (if temp__5823__auto__ (let [a temp__5823__auto__] (concat as a)) as))
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
  (reset-meta!
    #'read-desc-file
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'read-desc-file :ns *ns*))
  (defn as->aids
    ([db as]
      (let [aids (into #{} (mapv (fn fn__31747 ([p1__31746#] (d/entid db p1__31746#))) as))]
        (when (some nil? aids)
          (throw (java.lang.RuntimeException. (str "Invalid attribute in list: " (pr-str as)))))
        (let [temp__5825__auto__ (seq
                                   (set/difference aids (into #{} (tools/unique-identities db))))]
          (when temp__5825__auto__
            (let [diff temp__5825__auto__]
              (throw
                (java.lang.RuntimeException.
                  (str "Not unique identity attribute ids: " (pr-str diff))))))
          nil)
        aids)))
  (reset-meta!
    #'as->aids
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'as]), :column (int 1)}
      :name
      'as->aids
      :ns
      *ns*))
  (defn first-indexed-t-in-es
    ([db es]
      (let [temp__5825__auto__ (seq
                                 (map
                                   (fn fn__31751 ([d] (long (d/tx->t (.tx ^datomic.Datom d)))))
                                   (mapcat
                                     (fn fn__31753 ([e] (d/datoms (d/history db) :eavt e)))
                                     es)))]
        (when temp__5825__auto__ (let [ts temp__5825__auto__] (apply min ts))))))
  (reset-meta!
    #'first-indexed-t-in-es
    (assoc
      {:arglists (clojure.core/list ['db 'es]), :column (int 1)}
      :name
      'first-indexed-t-in-es
      :ns
      *ns*))
  (def -main*
   (fn _main_STAR_
     ([uri mem_index_mb p__31757]
       (let [map__31758 p__31757
             map__31758 (if (seq? map__31758)
                          (if (next map__31758)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31758))
                            (if (seq map__31758) (first map__31758) {}))
                          map__31758)
             ts (get map__31758 :ts)
             as (get map__31758 :as)]
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
         (let [map__31759 (tools/connection-resources uri)
               map__31759 (if (seq? map__31759)
                            (if (next map__31759)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__31759))
                              (if (seq map__31759) (first map__31759) {}))
                            map__31759)
               cr map__31759
               cluster (get map__31759 :cluster)
               olookup (get map__31759 :olookup)
               map__31760 (tools/db-resources cr)
               map__31760 (if (seq? map__31760)
                            (if (next map__31760)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__31760))
                              (if (seq map__31760) (first map__31760) {}))
                            map__31760)
               db (get map__31760 :db)
               log (get map__31760 :log)
               aids (as->aids db as)
               vec__31761 (detect/precise-identity-substitutions
                            (merge
                              (tools/identities-in-ts db log ts)
                              (tools/values-in-ts db log ts))
                            db
                            aids)
               submap (nth vec__31761 (int 0) nil)
               cantsub (nth vec__31761 (int 1) nil)
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
               vec__31764 (filter-and-rebuild
                            cr
                            {:db (tools/index-db cr), :log log}
                            mem_index_mb
                            rebuild_from_t
                            xforms)
               index_id (nth vec__31764 (int 0) nil)
               garbage (nth vec__31764 (int 1) nil)]
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
              :version 1}))))))
  (reset-meta!
    #'-main*
    (assoc
      {:arglists (clojure.core/list ['uri 'mem-index-mb {:keys ['ts 'as]}]), :column (int 1)}
      :name
      '-main*
      :ns
      *ns*))
  (def -main
   (fn _main
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
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['uri 'mem-index-mb 'detectfile]), :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))
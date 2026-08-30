(do
  (clojure.core/in-ns 'datomic.garbage)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cache :as 'cache]
        ['datomic.catalog :as 'catalog]
        ['datomic.cluster :as 'cluster]
        ['datomic.coordination :as 'coord]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.domain :as 'domain]
        ['datomic.fressian :as 'fressian]
        ['datomic.garbage.fressian :as 'gf]
        ['datomic.log :as 'log]
        ['datomic.index :as 'index]
        ['datomic.io :as 'io]
        ['datomic.kv-cluster :as 'kv-cluster]
        ['datomic.monitor :as 'monitor]
        ['datomic.process.events :as 'events]
        ['datomic.slf4j :as 'logger]
        ['datomic.treewalk :as 'treewalk]
        ['datomic.uri :as 'uri])))
  (when-not (.equals 'datomic.garbage 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.garbage))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cache :as 'cache]
          ['datomic.catalog :as 'catalog]
          ['datomic.cluster :as 'cluster]
          ['datomic.coordination :as 'coord]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.domain :as 'domain]
          ['datomic.fressian :as 'fressian]
          ['datomic.garbage.fressian :as 'gf]
          ['datomic.log :as 'log]
          ['datomic.index :as 'index]
          ['datomic.io :as 'io]
          ['datomic.kv-cluster :as 'kv-cluster]
          ['datomic.monitor :as 'monitor]
          ['datomic.process.events :as 'events]
          ['datomic.slf4j :as 'logger]
          ['datomic.treewalk :as 'treewalk]
          ['datomic.uri :as 'uri]))))
  (set! *warn-on-reflection* true)
  (defn root-ref-key
    ([cluster]
      (let [temp__5825__auto__ (cluster/dbId cluster)]
        (when temp__5825__auto__ (let [dbid temp__5825__auto__] (str "ref-gc-root/" dbid))))))
  (reset-meta!
    #'root-ref-key
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'root-ref-key
      :ns
      *ns*))
  (def leaf-threshold 600)
  (reset-meta! #'leaf-threshold (assoc {:column (int 1)} :name 'leaf-threshold :ns *ns*))
  (defn create-garbage-node
    ([cluster uuid o]
      (let [buf (fressian/byte-buf o :handlers gf/write-handlers :footer true)
            zipped (io/gzip-buffer buf)
            key (cluster/uuid->val-key uuid)]
        (cluster/create-val cluster key zipped))))
  (reset-meta!
    #'create-garbage-node
    (assoc
      {:private true, :arglists (clojure.core/list ['cluster 'uuid 'o]), :column (int 1)}
      :name
      'create-garbage-node
      :ns
      *ns*))
  (defn dir-seq
    ([lookup root]
      (mapcat
        (fn fn__18344 ([root_entry] (:children (get lookup (str (:uuid root_entry))))))
        (:children root))))
  (reset-meta!
    #'dir-seq
    (assoc
      {:arglists (clojure.core/list ['lookup 'root]), :column (int 1)}
      :name
      'dir-seq
      :ns
      *ns*))
  (defn leaf-seq
    ([lookup root]
      (mapcat
        (fn fn__18347 ([dir_entry] (:children (get lookup (str (:uuid dir_entry))))))
        (dir-seq lookup root))))
  (reset-meta!
    #'leaf-seq
    (assoc
      {:arglists (clojure.core/list ['lookup 'root]), :column (int 1)}
      :name
      'leaf-seq
      :ns
      *ns*))
  (defn ensure-root-ref
    ([cluster forget_garbage]
      (let [temp__5825__auto__ (root-ref-key cluster)]
        (when temp__5825__auto__
          (let [root_key temp__5825__auto__ result (deref (cluster/get-ref cluster root_key))]
            (if (and result (not forget_garbage))
              result
              (let [root_uuid (common/rand-uuid)
                    create (fn create ([uuid val] (create-garbage-node cluster uuid val)))]
                (if (and
                      (=
                        :created
                        (deref (^clojure.lang.IFn create root_uuid (gf/->GarbageRoot []))))
                      (=
                        :ok
                        (deref
                          (cluster/reset-ref cluster root_key (cluster/uuid->val-key root_uuid)))))
                  (deref (cluster/get-ref cluster root_key))
                  (do (throw (java.lang.Exception. "Garbage root creation failed")) nil))))))))
    ([cluster] (ensure-root-ref cluster false)))
  (reset-meta!
    #'ensure-root-ref
    (assoc
      {:arglists (clojure.core/list ['cluster] ['cluster 'forget-garbage]), :column (int 1)}
      :name
      'ensure-root-ref
      :ns
      *ns*))
  (defn ensure-root
    ([cluster lookup]
      (let [key (:key (ensure-root-ref cluster))]
        (common/getx lookup (cluster/val-key->uuid key)))))
  (reset-meta!
    #'ensure-root
    (assoc
      {:arglists (clojure.core/list ['cluster 'lookup]), :column (int 1)}
      :name
      'ensure-root
      :ns
      *ns*))
  (defn append-leaf
    ([cluster lookup leaf max_dir_size]
      (let [map__18360 (ensure-root-ref cluster)
            map__18360 (if (seq? map__18360)
                         (if (next map__18360)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18360))
                           (if (seq map__18360) (first map__18360) {}))
                         map__18360)
            old_root_val_key (get map__18360 :key)
            old_root_rev (get map__18360 :rev)
            vec__18361 (repeatedly common/rand-uuid)
            leaf_uuid (nth vec__18361 (int 0) nil)
            dir_uuid (nth vec__18361 (int 1) nil)
            root_uuid (nth vec__18361 (int 2) nil)
            old_root_id (cluster/val-key->uuid old_root_val_key)
            old_root_val (common/getx lookup old_root_id)
            old_dir_id (get (peek (:children old_root_val)) :uuid)
            old_dir_val (when old_dir_id (get lookup old_dir_id))
            new_tail_dir? (or (not old_dir_val) (>= (count (:children old_dir_val)) max_dir_size))
            leaf (update-in
                   leaf
                   [:children]
                   conj
                   {:tstamp (java.util.Date.),
                    :vals
                    (if old_dir_id
                      [(cluster/uuid->val-key old_dir_id) (cluster/uuid->val-key old_root_id)]
                      [(cluster/uuid->val-key old_root_id)])})
            dir_entry {:start (:tstamp (first (:children leaf))),
                       :end (:tstamp (peek (:children leaf))),
                       :uuid leaf_uuid}
            root_entry {:start
                        (if new_tail_dir?
                          (:start dir_entry)
                          (:start (first (:children old_dir_val)))),
                        :end (:end dir_entry),
                        :uuid dir_uuid}
            new_dir_val (if new_tail_dir?
                          (gf/->GarbageDir [dir_entry])
                          (update-in
                            old_dir_val
                            [:children]
                            (fn fn__18364 ([p1__18357#] (conj p1__18357# dir_entry)))))
            new_root_val (if new_tail_dir?
                           (update-in
                             old_root_val
                             [:children]
                             (fn fn__18366 ([p1__18358#] (conj p1__18358# root_entry))))
                           (update-in
                             old_root_val
                             [:children]
                             (fn fn__18368
                               ([p1__18359#]
                                 (assoc p1__18359# (long (dec (count p1__18359#))) root_entry)))))
            create (fn create ([uuid val] (create-garbage-node cluster uuid val)))
            leaf_result (^clojure.lang.IFn create leaf_uuid leaf)
            dir_result (^clojure.lang.IFn create dir_uuid new_dir_val)
            root_result (^clojure.lang.IFn create root_uuid new_root_val)]
        (if (= :created (deref leaf_result) (deref dir_result) (deref root_result))
          (if (=
                :ok
                (deref
                  (cluster/set-ref
                    cluster
                    (root-ref-key cluster)
                    (inc old_root_rev)
                    (cluster/uuid->val-key root_uuid))))
            (do
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process
                      {:event :garbage/tree,
                       :root-from old_root_id,
                       :root-to root_uuid,
                       :dir-from old_dir_id,
                       :dir-to dir_uuid})))
                nil)
              :ok)
            (do (throw (java.lang.Exception. "Conflict")) nil))
          (do (throw (java.lang.Exception. "Value create failed")) nil)))))
  (reset-meta!
    #'append-leaf
    (assoc
      {:arglists (clojure.core/list ['cluster 'lookup 'leaf 'max-dir-size]), :column (int 1)}
      :name
      'append-leaf
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.garbage" "garbage-agent") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.garbage" "garbage-agent") (agent {}))
  (defn do-mark-garbage
    ([m cluster lookup ids max_leaf_size max_dir_size]
      (monitor/add-stat :GarbageSegments (java.lang.Integer/valueOf (int (count ids))))
      (let [cluster_garbage (get m cluster [])
            cluster_garbage (if (seq ids)
                              (conj
                                cluster_garbage
                                {:tstamp (java.util.Date.),
                                 :vals (mapv cluster/uuid->val-key ids)})
                              cluster_garbage)
            leaf_size (apply
                        +
                        (map
                          (fn fn__18375
                            ([p1__18374#]
                              (java.lang.Integer/valueOf (int (count (:vals p1__18374#))))))
                          cluster_garbage))]
        (if (< max_leaf_size leaf_size)
          (do
            (let [m_18377 {:event :garbage/append-leaf, :segments leaf_size}
                  ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.garbage")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_18377 :phase :begin))))
                                    nil)
                  start__8599__auto__ (java.lang.System/nanoTime)
                  result__8600__auto__ (try
                                         {:returned
                                          (let [leaf (gf/->GarbageLeaf cluster_garbage)]
                                            (try
                                              (append-leaf cluster lookup leaf max_dir_size)
                                              (catch
                                                java.lang.Throwable
                                                t
                                                (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.garbage")
                                                      ex t]
                                                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                                    (.warn
                                                      ^org.slf4j.Logger logger
                                                      (logger/process "Garbage write failed")
                                                      ^java.lang.Throwable ex)
                                                    (logger/caused-by logger ex))
                                                  nil))))}
                                         (catch
                                           java.lang.Throwable
                                           t__8601__auto__
                                           {:threw t__8601__auto__}))
                  elapsed_18378 (- (java.lang.System/nanoTime) start__8599__auto__)
                  msec_18379 (logger/format-as-msec (long elapsed_18378))]
              (let [endmsg__8602__auto__ (merge
                                           (assoc m_18377 :msec msec_18379 :phase :end)
                                           (when (:threw result__8600__auto__)
                                             {:threw (class (:threw result__8600__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                nil)
              (if (contains? result__8600__auto__ :returned)
                (:returned result__8600__auto__)
                (throw (:threw result__8600__auto__))))
            (dissoc m cluster))
          (assoc m cluster cluster_garbage)))))
  (reset-meta!
    #'do-mark-garbage
    (assoc
      {:arglists (clojure.core/list ['m 'cluster 'lookup 'ids 'max-leaf-size 'max-dir-size]),
       :column (int 1)}
      :name
      'do-mark-garbage
      :ns
      *ns*))
  (defn mark-garbage
    ([cluster lookup ids max_leaf_size max_dir_size]
      (do
        (loop [seq_18389 (seq (partition-all max_leaf_size ids))
               chunk_18390 nil
               count_18391 0
               i_18392 0]
          (if (< i_18392 count_18391)
            (let [chunks (.nth ^clojure.lang.Indexed chunk_18390 (int i_18392))]
              (send-off
                garbage-agent
                do-mark-garbage
                cluster
                lookup
                chunks
                max_leaf_size
                max_dir_size)
              (recur seq_18389 chunk_18390 count_18391 (inc i_18392)))
            (let [temp__5825__auto__ (seq seq_18389)]
              (when temp__5825__auto__
                (let [seq_18389 temp__5825__auto__]
                  (if (chunked-seq? seq_18389)
                    (let [c__6090__auto__ (chunk-first seq_18389)]
                      (recur
                        (chunk-rest seq_18389)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [chunks (first seq_18389)]
                      (send-off
                        garbage-agent
                        do-mark-garbage
                        cluster
                        lookup
                        chunks
                        max_leaf_size
                        max_dir_size)
                      (recur (next seq_18389) nil 0 0))))))))
        :ok))
    ([cluster lookup ids] (mark-garbage cluster lookup ids 1000 800)))
  (reset-meta!
    #'mark-garbage
    (assoc
      {:arglists
       (clojure.core/list
         ['cluster 'lookup 'ids]
         ['cluster 'lookup 'ids 'max-leaf-size 'max-dir-size]),
       :column (int 1)}
      :name
      'mark-garbage
      :ns
      *ns*))
  (defn install-mark-handler
    ([]
      (let [olookups (cache/create-computing
                       (fn fn__18396 ([cluster] (domain/system-cache-olookup cluster)))
                       10)]
        (events/subscribe
          :datomic.garbage/mark
          :datomic.garbage/mark
          (fn fn__18399
            ([p__18398]
              (let [map__18400 p__18398
                    map__18400 (if (seq? map__18400)
                                 (if (next map__18400)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__18400))
                                   (if (seq map__18400) (first map__18400) {}))
                                 map__18400)
                    cluster (get map__18400 :cluster)
                    garbage (get map__18400 :garbage)]
                (mark-garbage cluster (get olookups cluster) garbage))))))))
  (reset-meta!
    #'install-mark-handler
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'install-mark-handler
      :ns
      *ns*))
  (defn flush-garbage
    ([cluster olookup]
      (send-off garbage-agent do-mark-garbage cluster olookup nil 0 800)
      (await garbage-agent)))
  (reset-meta!
    #'flush-garbage
    (assoc
      {:arglists (clojure.core/list ['cluster 'olookup]), :column (int 1)}
      :name
      'flush-garbage
      :ns
      *ns*))
  (defn pending-garbage-count
    ([gmap cluster]
      (apply
        +
        (map
          (fn fn__18405
            ([p1__18404#] (java.lang.Integer/valueOf (int (count (:vals p1__18404#))))))
          (get gmap cluster)))))
  (reset-meta!
    #'pending-garbage-count
    (assoc
      {:arglists (clojure.core/list ['gmap 'cluster]), :column (int 1)}
      :name
      'pending-garbage-count
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.garbage" "gc-val->obj") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.garbage" "gc-val->obj")
    (fressian/val->obj gf/read-handlers))
  (defn gc-get-node
    ([cluster uuid]
      (let [temp__5825__auto__ (:buf
                                 (deref (cluster/get-val cluster (cluster/uuid->val-key uuid))))]
        (when temp__5825__auto__ (let [v temp__5825__auto__] (gc-val->obj v))))))
  (reset-meta!
    #'gc-get-node
    (assoc
      {:arglists (clojure.core/list ['cluster 'uuid]), :column (int 1)}
      :name
      'gc-get-node
      :ns
      *ns*))
  (defn pace-gc
    ([]
      (let [temp__5825__auto__ (config/property "datomic.gcStoragePaceMsec")]
        (when temp__5825__auto__
          (let [pace temp__5825__auto__]
            (java.lang.Thread/sleep (long ^java.lang.Number pace))
            nil)))))
  (reset-meta!
    #'pace-gc
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'pace-gc :ns *ns*))
  (defn gc-delete-vals
    ([cluster vs]
      (let [futs (mapv
                   (fn fn__18413 ([p1__18412#] (cluster/delete cluster p1__18412#)))
                   (remove empty? vs))
            result (reduce
                     (fn fn__18415 ([count fut] (pace-gc) (+ count (if (= :ok (deref fut)) 1 0))))
                     0
                     futs)]
        (monitor/add-stat :GarbageDeletedCount result)
        result)))
  (reset-meta!
    #'gc-delete-vals
    (assoc
      {:arglists (clojure.core/list ['cluster 'vs]), :column (int 1)}
      :name
      'gc-delete-vals
      :ns
      *ns*))
  (defn gc-leaf
    ([cluster uuid tstamp]
      (let [temp__5823__auto__ (gc-get-node cluster uuid)]
        (if temp__5823__auto__
          (let [leaf temp__5823__auto__]
            {:count
             (reduce
               (fn fn__18420
                 ([count p__18419]
                   (let [map__18421 p__18419
                         map__18421 (if (seq? map__18421)
                                      (if (next map__18421)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__18421))
                                        (if (seq map__18421) (first map__18421) {}))
                                      map__18421)
                         vals (get map__18421 :vals)]
                     (+ count (gc-delete-vals cluster vals)))))
               0
               (take-while
                 (fn fn__18423
                   ([p1__18418#] (neg? (clojure.lang.Util/compare (:tstamp p1__18418#) tstamp))))
                 (:children leaf))),
             :complete
             (neg? (clojure.lang.Util/compare (:tstamp (last (:children leaf))) tstamp))})
          {:count 0, :complete true}))))
  (reset-meta!
    #'gc-leaf
    (assoc
      {:arglists (clojure.core/list ['cluster 'uuid 'tstamp]), :column (int 1)}
      :name
      'gc-leaf
      :ns
      *ns*))
  (defn gc-dir
    ([cluster uuid tstamp]
      (let [temp__5823__auto__ (gc-get-node cluster uuid)]
        (if temp__5823__auto__
          (let [dir temp__5823__auto__]
            {:count
             (reduce
               (fn fn__18429
                 ([total p__18428]
                   (let [map__18430 p__18428
                         map__18430 (if (seq? map__18430)
                                      (if (next map__18430)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__18430))
                                        (if (seq map__18430) (first map__18430) {}))
                                      map__18430)
                         uuid (get map__18430 :uuid)
                         map__18431 (gc-leaf cluster uuid tstamp)
                         map__18431 (if (seq? map__18431)
                                      (if (next map__18431)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__18431))
                                        (if (seq map__18431) (first map__18431) {}))
                                      map__18431)
                         count (get map__18431 :count)
                         complete (get map__18431 :complete)]
                     (if complete
                       (+ (+ total count) (gc-delete-vals cluster [(cluster/uuid->val-key uuid)]))
                       (+ total count)))))
               0
               (take-while
                 (fn fn__18433
                   ([p1__18427#] (neg? (clojure.lang.Util/compare (:start p1__18427#) tstamp))))
                 (:children dir))),
             :complete (neg? (clojure.lang.Util/compare (:end (last (:children dir))) tstamp))})
          {:count 0, :complete true}))))
  (reset-meta!
    #'gc-dir
    (assoc
      {:arglists (clojure.core/list ['cluster 'uuid 'tstamp]), :column (int 1)}
      :name
      'gc-dir
      :ns
      *ns*))
  (defn gc
    ([cluster tstamp progress]
      (let [root (gc-get-node cluster (cluster/val-key->uuid (:key (ensure-root-ref cluster))))
            count (reduce
                    (fn fn__18439
                      ([total p__18438]
                        (let [map__18440 p__18438
                              map__18440 (if (seq? map__18440)
                                           (if (next map__18440)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__18440))
                                             (if (seq map__18440) (first map__18440) {}))
                                           map__18440)
                              uuid (get map__18440 :uuid)
                              map__18441 (gc-dir cluster uuid tstamp)
                              map__18441 (if (seq? map__18441)
                                           (if (next map__18441)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__18441))
                                             (if (seq map__18441) (first map__18441) {}))
                                           map__18441)
                              count (get map__18441 :count)
                              complete (get map__18441 :complete)]
                          (^clojure.lang.IFn progress count)
                          (if complete
                            (do
                              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process {:event :garbage/collect-dir, :id uuid})))
                                nil)
                              (+
                                (+ total count)
                                (gc-delete-vals cluster [(cluster/uuid->val-key uuid)])))
                            (+ total count)))))
                    0
                    (take-while
                      (fn fn__18443
                        ([p1__18437#]
                          (neg? (clojure.lang.Util/compare (:start p1__18437#) tstamp))))
                      (:children root)))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :garbage/collected, :count count})))
          nil)
        count))
    ([cluster tstamp] (gc cluster tstamp identity)))
  (reset-meta!
    #'gc
    (assoc
      {:arglists (clojure.core/list ['cluster 'tstamp] ['cluster 'tstamp 'progress]),
       :column (int 1)}
      :name
      'gc
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.garbage" "collection-agent") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.garbage" "collection-agent") (agent nil))
  (defn queue-gc
    ([cluster older_than]
      (send-off
        collection-agent
        (fn fn__18446
          ([_]
            (try
              (let [m_18448 {:event :garbage/collect,
                             :dbid (cluster/dbId cluster),
                             :older-than older_than}
                    ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.garbage")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_18448 :phase :begin))))
                                      nil)
                    start__8599__auto__ (java.lang.System/nanoTime)
                    result__8600__auto__ (try
                                           {:returned (gc cluster older_than)}
                                           (catch
                                             java.lang.Throwable
                                             t__8601__auto__
                                             {:threw t__8601__auto__}))
                    elapsed_18449 (- (java.lang.System/nanoTime) start__8599__auto__)
                    msec_18450 (logger/format-as-msec (long elapsed_18449))]
                (let [endmsg__8602__auto__ (merge
                                             (assoc m_18448 :msec msec_18450 :phase :end)
                                             (when (:threw result__8600__auto__)
                                               {:threw (class (:threw result__8600__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                  nil)
                (if (contains? result__8600__auto__ :returned)
                  (:returned result__8600__auto__)
                  (do (throw (:threw result__8600__auto__)) nil)))
              (catch
                java.lang.Throwable
                t
                (do
                  (monitor/alarm :StorageGCFailed)
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage") ex t]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process "Cluster gc failed")
                        ^java.lang.Throwable ex)
                      (logger/caused-by logger ex))
                    nil))))
            nil)))))
  (reset-meta!
    #'queue-gc
    (assoc
      {:arglists (clojure.core/list ['cluster 'older-than]), :column (int 1)}
      :name
      'queue-gc
      :ns
      *ns*))
  (defn gc-deleted-db
    ([system_cluster db_cluster status_callback]
      (let [status (fn status ([& args] (^clojure.lang.IFn status_callback (apply str args))))
            olookup (domain/deserializing-repairing-lookup db_cluster)]
        (let [temp__5825__auto__ (config/property "datomic.gcStoragePaceMsec")]
          (when temp__5825__auto__
            (let [n temp__5825__auto__] (println "Pacing with datomic.gcStoragePaceMsec =" n))))
        (let [temp__5825__auto__ (log/root-id db_cluster)]
          (when temp__5825__auto__
            (let [log_root_id temp__5825__auto__
                  c (count (treewalk/log-tree-seq log_root_id olookup true))]
              (reduce
                (fn fn__18462
                  ([n ks]
                    (let [n (+ n (gc-delete-vals db_cluster ks))]
                      (^clojure.lang.IFn status
                        "Deleted "
                        n
                        " of "
                        (java.lang.Integer/valueOf (int c))
                        " log segments")
                      n)))
                0
                (partition 1000 (treewalk/log-tree-seq log_root_id olookup true)))
              (^clojure.lang.IFn status
                "Deleted "
                (java.lang.Integer/valueOf (int c))
                " of "
                (java.lang.Integer/valueOf (int c))
                " log segments")
              (cluster/delete-reference db_cluster (log/tail-pod-key db_cluster)))))
        (let [temp__5825__auto__ (:key
                                   (deref
                                     (cluster/get-ref
                                       db_cluster
                                       (index/index-ref-key-name db_cluster))))]
          (when temp__5825__auto__
            (let [index_root_id temp__5825__auto__
                  c (count (treewalk/index-tree-seq index_root_id olookup true))]
              (reduce
                (fn fn__18464
                  ([n ks]
                    (let [n (+ n (gc-delete-vals db_cluster ks))]
                      (^clojure.lang.IFn status
                        "Deleted "
                        n
                        " of "
                        (java.lang.Integer/valueOf (int c))
                        " index segments")
                      n)))
                0
                (partition 1000 (treewalk/index-tree-seq index_root_id olookup true)))
              (^clojure.lang.IFn status
                "Deleted "
                (java.lang.Integer/valueOf (int c))
                " of "
                (java.lang.Integer/valueOf (int c))
                " index segments")
              (cluster/delete-reference db_cluster (index/index-ref-key-name db_cluster)))))
        (catalog/remove-deleted-database system_cluster (cluster/dbId db_cluster))
        (when (deref (cluster/get-ref db_cluster (root-ref-key db_cluster)))
          (^clojure.lang.IFn status "Deleting garbage segments.  This may take a while.")
          (let [c (gc db_cluster (java.util.Date.))]
            (^clojure.lang.IFn status "Deleting " c " garbage segments"))
          (cluster/delete-reference db_cluster (root-ref-key db_cluster)))
        true)))
  (reset-meta!
    #'gc-deleted-db
    (assoc
      {:arglists (clojure.core/list ['system-cluster 'db-cluster 'status-callback]),
       :column (int 1)}
      :name
      'gc-deleted-db
      :ns
      *ns*))
  (defn get-db-ids
    ([uri]
      (let [cluster_conf (uri/parse uri) protocol (:protocol cluster_conf)]
        (when (not= protocol :mem)
          (let [cat (catalog/get-catalog (coord/create-system-cluster cluster_conf))]
            {:deleted (:datomic/deleted cat), :active (catalog/db-ids cat)})))))
  (reset-meta!
    #'get-db-ids
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'get-db-ids :ns *ns*))
  (defn deleted-db-cluster-conf ([uri id] (assoc (dissoc (uri/parse uri) :db-name) :db-id id)))
  (reset-meta!
    #'deleted-db-cluster-conf
    (assoc
      {:arglists (clojure.core/list ['uri 'id]), :column (int 1)}
      :name
      'deleted-db-cluster-conf
      :ns
      *ns*))
  (defn gc-deleted-dbs
    ([p__18472]
      (let [map__18473 p__18472
            map__18473 (if (seq? map__18473)
                         (if (next map__18473)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18473))
                           (if (seq map__18473) (first map__18473) {}))
                         map__18473)
            uri (get map__18473 :uri)]
        (install-mark-handler)
        (let [map__18474 (get-db-ids uri)
              map__18474 (if (seq? map__18474)
                           (if (next map__18474)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__18474))
                             (if (seq map__18474) (first map__18474) {}))
                           map__18474)
              deleted (get map__18474 :deleted)
              active (get map__18474 :active)]
          (if (seq deleted)
            (do
              (println
                "Deleting storage for"
                (java.lang.Integer/valueOf (int (count deleted)))
                "deleted dbs.")
              (let [cluster_conf (uri/parse uri)
                    system_cluster (coord/create-system-cluster cluster_conf)]
                (loop [seq_18475 (seq deleted) chunk_18476 nil count_18477 0 i_18478 0]
                  (if (< i_18478 count_18477)
                    (let [id (.nth ^clojure.lang.Indexed chunk_18476 (int i_18478))]
                      (if (get active id)
                        (do
                          (println "Database has been restored, skipping " id)
                          (catalog/remove-deleted-database system_cluster id))
                        (do
                          (println "Deleting storage for " id)
                          (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                db_cluster (coord/create-db-cluster db_cluster_conf)]
                            (gc-deleted-db system_cluster db_cluster println))))
                      (recur seq_18475 chunk_18476 count_18477 (inc i_18478)))
                    (let [temp__5825__auto__ (seq seq_18475)]
                      (when temp__5825__auto__
                        (let [seq_18475 temp__5825__auto__]
                          (if (chunked-seq? seq_18475)
                            (let [c__6090__auto__ (chunk-first seq_18475)]
                              (recur
                                (chunk-rest seq_18475)
                                c__6090__auto__
                                (int (count c__6090__auto__))
                                (int 0)))
                            (let [id (first seq_18475)]
                              (if (get active id)
                                (do
                                  (println "Database has been restored, skipping " id)
                                  (catalog/remove-deleted-database system_cluster id))
                                (do
                                  (println "Deleting storage for " id)
                                  (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                        db_cluster (coord/create-db-cluster db_cluster_conf)]
                                    (gc-deleted-db system_cluster db_cluster println))))
                              (recur (next seq_18475) nil 0 0)))))))))
              (java.lang.Thread/sleep 100)
              (let [n (reduce
                        (fn fn__18480
                          ([n p__18479]
                            (let [vec__18481 p__18479
                                  cluster (nth vec__18481 (int 0) nil)
                                  garbage (nth vec__18481 (int 1) nil)]
                              (loop [seq_18484 (seq garbage)
                                     chunk_18485 nil
                                     count_18486 0
                                     i_18487 0]
                                (if (< i_18487 count_18486)
                                  (let [g (.nth ^clojure.lang.Indexed chunk_18485 (int i_18487))]
                                    (loop [seq_18488 (seq (:vals g))
                                           chunk_18489 nil
                                           count_18490 0
                                           i_18491 0]
                                      (if (< i_18491 count_18490)
                                        (let [v (.nth
                                                  ^clojure.lang.Indexed chunk_18489
                                                  (int i_18491))]
                                          (pace-gc)
                                          (deref (cluster/delete cluster v))
                                          (recur seq_18488 chunk_18489 count_18490 (inc i_18491)))
                                        (let [temp__5825__auto__ (seq seq_18488)]
                                          (when temp__5825__auto__
                                            (let [seq_18488 temp__5825__auto__]
                                              (if (chunked-seq? seq_18488)
                                                (let [c__6090__auto__ (chunk-first seq_18488)]
                                                  (recur
                                                    (chunk-rest seq_18488)
                                                    c__6090__auto__
                                                    (int (count c__6090__auto__))
                                                    (int 0)))
                                                (let [v (first seq_18488)]
                                                  (pace-gc)
                                                  (deref (cluster/delete cluster v))
                                                  (recur (next seq_18488) nil 0 0))))))))
                                    (recur seq_18484 chunk_18485 count_18486 (inc i_18487)))
                                  (let [temp__5825__auto__ (seq seq_18484)]
                                    (when temp__5825__auto__
                                      (let [seq_18484 temp__5825__auto__]
                                        (if (chunked-seq? seq_18484)
                                          (let [c__6090__auto__ (chunk-first seq_18484)]
                                            (recur
                                              (chunk-rest seq_18484)
                                              c__6090__auto__
                                              (int (count c__6090__auto__))
                                              (int 0)))
                                          (let [g (first seq_18484)]
                                            (loop [seq_18492 (seq (:vals g))
                                                   chunk_18493 nil
                                                   count_18494 0
                                                   i_18495 0]
                                              (if (< i_18495 count_18494)
                                                (let [v (.nth
                                                          ^clojure.lang.Indexed chunk_18493
                                                          (int i_18495))]
                                                  (pace-gc)
                                                  (deref (cluster/delete cluster v))
                                                  (recur
                                                    seq_18492
                                                    chunk_18493
                                                    count_18494
                                                    (inc i_18495)))
                                                (let [temp__5825__auto__ (seq seq_18492)]
                                                  (when temp__5825__auto__
                                                    (let [seq_18492 temp__5825__auto__]
                                                      (if (chunked-seq? seq_18492)
                                                        (let [c__6090__auto__
                                                              (chunk-first seq_18492)]
                                                          (recur
                                                            (chunk-rest seq_18492)
                                                            c__6090__auto__
                                                            (int (count c__6090__auto__))
                                                            (int 0)))
                                                        (let [v (first seq_18492)]
                                                          (pace-gc)
                                                          (deref (cluster/delete cluster v))
                                                          (recur (next seq_18492) nil 0 0))))))))
                                            (recur (next seq_18484) nil 0 0))))))))
                              (apply + n (map (comp count :vals) garbage)))))
                        0
                        (deref garbage-agent))]
                (println "Deleted" n "catalog segments")))
            (println "GC deleted dbs: no deleted dbs found."))))))
  (reset-meta!
    #'gc-deleted-dbs
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri]}]), :column (int 1)}
      :name
      'gc-deleted-dbs
      :ns
      *ns*)))
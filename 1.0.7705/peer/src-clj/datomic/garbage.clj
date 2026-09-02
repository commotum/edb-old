(do
  (clojure.core/in-ns 'datomic.garbage)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.garbage)
    {:doc
     "Tracks immutable storage segments made unreachable by new log and index roots and reclaims segments older than a caller-supplied safety boundary. Collection is incremental, paced, and independent of live tree reads."})
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
      (let [temp__5804__auto__ (cluster/dbId cluster)]
        (when temp__5804__auto__ (let [dbid temp__5804__auto__] (str "ref-gc-root/" dbid))))))
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
        (fn fn__21073 ([root_entry] (:children (get lookup (str (:uuid root_entry))))))
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
        (fn fn__21076 ([dir_entry] (:children (get lookup (str (:uuid dir_entry))))))
        (dir-seq lookup root))))
  (reset-meta!
    #'leaf-seq
    (assoc
      {:arglists (clojure.core/list ['lookup 'root]), :column (int 1)}
      :name
      'leaf-seq
      :ns
      *ns*))
  ;; Create the per-database garbage root reference on first use.
  (defn ensure-root-ref
    ([cluster forget_garbage]
      (let [temp__5804__auto__ (root-ref-key cluster)]
        (when temp__5804__auto__
          (let [root_key temp__5804__auto__ result (deref (cluster/get-ref cluster root_key))]
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
  ;; Persist a full pending-garbage leaf, extend its directory and root, then
  ;; publish the replacement root with a revision-checked reference update.
  (defn append-leaf
    ([cluster lookup leaf max_dir_size]
      (let [map__21089 (ensure-root-ref cluster)
            map__21089 (if (seq? map__21089)
                         (if (next map__21089)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21089))
                           (if (seq map__21089) (first map__21089) {}))
                         map__21089)
            old_root_val_key (get map__21089 :key)
            old_root_rev (get map__21089 :rev)
            vec__21090 (repeatedly common/rand-uuid)
            leaf_uuid (nth vec__21090 (int 0) nil)
            dir_uuid (nth vec__21090 (int 1) nil)
            root_uuid (nth vec__21090 (int 2) nil)
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
                            (fn fn__21093 ([p1__21086#] (conj p1__21086# dir_entry)))))
            new_root_val (if new_tail_dir?
                           (update-in
                             old_root_val
                             [:children]
                             (fn fn__21095 ([p1__21087#] (conj p1__21087# root_entry))))
                           (update-in
                             old_root_val
                             [:children]
                             (fn fn__21097
                               ([p1__21088#]
                                 (assoc p1__21088# (long (dec (count p1__21088#))) root_entry)))))
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
                          (fn fn__21104
                            ([p1__21103#]
                              (java.lang.Integer/valueOf (int (count (:vals p1__21103#))))))
                          cluster_garbage))]
        (if (< max_leaf_size leaf_size)
          (do
            (let [m_21106 {:event :garbage/append-leaf, :segments leaf_size}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.garbage")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_21106 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
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
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_21107 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_21108 (logger/format-as-msec (long elapsed_21107))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_21106 :msec msec_21108 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (throw (:threw result__8554__auto__))))
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
  ;; Queue newly unreachable immutable values for persistent, batched recording.
  (defn mark-garbage
    ([cluster lookup ids max_leaf_size max_dir_size]
      (do
        (loop [seq_21118 (seq (partition-all max_leaf_size ids))
               chunk_21119 nil
               count_21120 0
               i_21121 0]
          (if (< i_21121 count_21120)
            (let [chunks (.nth ^clojure.lang.Indexed chunk_21119 (int i_21121))]
              (send-off
                garbage-agent
                do-mark-garbage
                cluster
                lookup
                chunks
                max_leaf_size
                max_dir_size)
              (recur seq_21118 chunk_21119 count_21120 (inc i_21121)))
            (let [temp__5804__auto__ (seq seq_21118)]
              (when temp__5804__auto__
                (let [seq_21118 temp__5804__auto__]
                  (if (chunked-seq? seq_21118)
                    (let [c__6065__auto__ (chunk-first seq_21118)]
                      (recur
                        (chunk-rest seq_21118)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [chunks (first seq_21118)]
                      (send-off
                        garbage-agent
                        do-mark-garbage
                        cluster
                        lookup
                        chunks
                        max_leaf_size
                        max_dir_size)
                      (recur (next seq_21118) nil 0 0))))))))
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
                       (fn fn__21125 ([cluster] (domain/system-cache-olookup cluster)))
                       10)]
        (events/subscribe
          :datomic.garbage/mark
          :datomic.garbage/mark
          (fn fn__21128
            ([p__21127]
              (let [map__21129 p__21127
                    map__21129 (if (seq? map__21129)
                                 (if (next map__21129)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__21129))
                                   (if (seq map__21129) (first map__21129) {}))
                                 map__21129)
                    cluster (get map__21129 :cluster)
                    garbage (get map__21129 :garbage)]
                (mark-garbage cluster (get olookups cluster) garbage))))))))
  (reset-meta!
    #'install-mark-handler
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'install-mark-handler
      :ns
      *ns*))
  ;; Persist all pending marks for a database and wait for the marking agent.
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
          (fn fn__21134
            ([p1__21133#] (java.lang.Integer/valueOf (int (count (:vals p1__21133#))))))
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
      (let [temp__5804__auto__ (:buf
                                 (deref (cluster/get-val cluster (cluster/uuid->val-key uuid))))]
        (when temp__5804__auto__ (let [v temp__5804__auto__] (gc-val->obj v))))))
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
      (let [temp__5804__auto__ (config/property "datomic.gcStoragePaceMsec")]
        (when temp__5804__auto__
          (let [pace temp__5804__auto__]
            (java.lang.Thread/sleep (long ^java.lang.Number pace))
            nil)))))
  (reset-meta!
    #'pace-gc
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'pace-gc :ns *ns*))
  ;; Delete recorded immutable values at the configured storage pace and report the count.
  (defn gc-delete-vals
    ([cluster vs]
      (let [futs (mapv
                   (fn fn__21142 ([p1__21141#] (cluster/delete cluster p1__21141#)))
                   (remove empty? vs))
            result (reduce
                     (fn fn__21144 ([count fut] (pace-gc) (+ count (if (= :ok (deref fut)) 1 0))))
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
      (let [temp__5802__auto__ (gc-get-node cluster uuid)]
        (if temp__5802__auto__
          (let [leaf temp__5802__auto__]
            {:count
             (reduce
               (fn fn__21149
                 ([count p__21148]
                   (let [map__21150 p__21148
                         map__21150 (if (seq? map__21150)
                                      (if (next map__21150)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__21150))
                                        (if (seq map__21150) (first map__21150) {}))
                                      map__21150)
                         vals (get map__21150 :vals)]
                     (+ count (gc-delete-vals cluster vals)))))
               0
               (take-while
                 (fn fn__21152
                   ([p1__21147#] (neg? (clojure.lang.Util/compare (:tstamp p1__21147#) tstamp))))
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
      (let [temp__5802__auto__ (gc-get-node cluster uuid)]
        (if temp__5802__auto__
          (let [dir temp__5802__auto__]
            {:count
             (reduce
               (fn fn__21158
                 ([total p__21157]
                   (let [map__21159 p__21157
                         map__21159 (if (seq? map__21159)
                                      (if (next map__21159)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__21159))
                                        (if (seq map__21159) (first map__21159) {}))
                                      map__21159)
                         uuid (get map__21159 :uuid)
                         map__21160 (gc-leaf cluster uuid tstamp)
                         map__21160 (if (seq? map__21160)
                                      (if (next map__21160)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__21160))
                                        (if (seq map__21160) (first map__21160) {}))
                                      map__21160)
                         count (get map__21160 :count)
                         complete (get map__21160 :complete)]
                     (if complete
                       (+ (+ total count) (gc-delete-vals cluster [(cluster/uuid->val-key uuid)]))
                       (+ total count)))))
               0
               (take-while
                 (fn fn__21162
                   ([p1__21156#] (neg? (clojure.lang.Util/compare (:start p1__21156#) tstamp))))
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
  ;; Reclaim recorded values strictly older than tstamp, pruning completed
  ;; garbage leaves and directories as their contents are exhausted.
  (defn gc
    ([cluster tstamp progress]
      (let [root (gc-get-node cluster (cluster/val-key->uuid (:key (ensure-root-ref cluster))))
            count (reduce
                    (fn fn__21168
                      ([total p__21167]
                        (let [map__21169 p__21167
                              map__21169 (if (seq? map__21169)
                                           (if (next map__21169)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__21169))
                                             (if (seq map__21169) (first map__21169) {}))
                                           map__21169)
                              uuid (get map__21169 :uuid)
                              map__21170 (gc-dir cluster uuid tstamp)
                              map__21170 (if (seq? map__21170)
                                           (if (next map__21170)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__21170))
                                             (if (seq map__21170) (first map__21170) {}))
                                           map__21170)
                              count (get map__21170 :count)
                              complete (get map__21170 :complete)]
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
                      (fn fn__21172
                        ([p1__21166#]
                          (neg? (clojure.lang.Util/compare (:start p1__21166#) tstamp))))
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
  ;; Serialize a collection request on the process-wide collection agent.
  (defn queue-gc
    ([cluster older_than]
      (send-off
        collection-agent
        (fn fn__21175
          ([_]
            (try
              (let [m_21177 {:event :garbage/collect,
                             :dbid (cluster/dbId cluster),
                             :older-than older_than}
                    ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.garbage")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_21177 :phase :begin))))
                                      nil)
                    start__8553__auto__ (java.lang.System/nanoTime)
                    result__8554__auto__ (try
                                           {:returned (gc cluster older_than)}
                                           (catch
                                             java.lang.Throwable
                                             t__8555__auto__
                                             {:threw t__8555__auto__}))
                    elapsed_21178 (- (java.lang.System/nanoTime) start__8553__auto__)
                    msec_21179 (logger/format-as-msec (long elapsed_21178))]
                (let [endmsg__8556__auto__ (merge
                                             (assoc m_21177 :msec msec_21179 :phase :end)
                                             (when (:threw result__8554__auto__)
                                               {:threw (class (:threw result__8554__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                  nil)
                (if (contains? result__8554__auto__ :returned)
                  (:returned result__8554__auto__)
                  (do (throw (:threw result__8554__auto__)) nil)))
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
        (let [temp__5804__auto__ (config/property "datomic.gcStoragePaceMsec")]
          (when temp__5804__auto__
            (let [n temp__5804__auto__] (println "Pacing with datomic.gcStoragePaceMsec =" n))))
        (let [temp__5804__auto__ (log/root-id db_cluster)]
          (when temp__5804__auto__
            (let [log_root_id temp__5804__auto__
                  c (count (treewalk/log-tree-seq log_root_id olookup true))]
              (reduce
                (fn fn__21191
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
        (let [temp__5804__auto__ (:key
                                   (deref
                                     (cluster/get-ref
                                       db_cluster
                                       (index/index-ref-key-name db_cluster))))]
          (when temp__5804__auto__
            (let [index_root_id temp__5804__auto__
                  c (count (treewalk/index-tree-seq index_root_id olookup true))]
              (reduce
                (fn fn__21193
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
       :doc
       "Deletes complete 1,000-segment batches from a deleted database's log and index traversals, then collects its recorded-garbage segments. Trailing traversal groups smaller than 1,000 remain in storage. Progress messages are sent to status-callback; the log and index references and deleted-catalog entry are removed before the function returns true.",
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
    ([p__21201]
      (let [map__21202 p__21201
            map__21202 (if (seq? map__21202)
                         (if (next map__21202)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21202))
                           (if (seq map__21202) (first map__21202) {}))
                         map__21202)
            uri (get map__21202 :uri)]
        (install-mark-handler)
        (let [map__21203 (get-db-ids uri)
              map__21203 (if (seq? map__21203)
                           (if (next map__21203)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__21203))
                             (if (seq map__21203) (first map__21203) {}))
                           map__21203)
              deleted (get map__21203 :deleted)
              active (get map__21203 :active)]
          (if (seq deleted)
            (do
              (println
                "Deleting storage for"
                (java.lang.Integer/valueOf (int (count deleted)))
                "deleted dbs.")
              (let [cluster_conf (uri/parse uri)
                    system_cluster (coord/create-system-cluster cluster_conf)]
                (loop [seq_21204 (seq deleted) chunk_21205 nil count_21206 0 i_21207 0]
                  (if (< i_21207 count_21206)
                    (let [id (.nth ^clojure.lang.Indexed chunk_21205 (int i_21207))]
                      (if (get active id)
                        (do
                          (println "Database has been restored, skipping " id)
                          (catalog/remove-deleted-database system_cluster id))
                        (do
                          (println "Deleting storage for " id)
                          (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                db_cluster (coord/create-db-cluster db_cluster_conf)]
                            (gc-deleted-db system_cluster db_cluster println))))
                      (recur seq_21204 chunk_21205 count_21206 (inc i_21207)))
                    (let [temp__5804__auto__ (seq seq_21204)]
                      (when temp__5804__auto__
                        (let [seq_21204 temp__5804__auto__]
                          (if (chunked-seq? seq_21204)
                            (let [c__6065__auto__ (chunk-first seq_21204)]
                              (recur
                                (chunk-rest seq_21204)
                                c__6065__auto__
                                (int (count c__6065__auto__))
                                (int 0)))
                            (let [id (first seq_21204)]
                              (if (get active id)
                                (do
                                  (println "Database has been restored, skipping " id)
                                  (catalog/remove-deleted-database system_cluster id))
                                (do
                                  (println "Deleting storage for " id)
                                  (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                        db_cluster (coord/create-db-cluster db_cluster_conf)]
                                    (gc-deleted-db system_cluster db_cluster println))))
                              (recur (next seq_21204) nil 0 0)))))))))
              (java.lang.Thread/sleep 100)
              (let [n (reduce
                        (fn fn__21209
                          ([n p__21208]
                            (let [vec__21210 p__21208
                                  cluster (nth vec__21210 (int 0) nil)
                                  garbage (nth vec__21210 (int 1) nil)]
                              (loop [seq_21213 (seq garbage)
                                     chunk_21214 nil
                                     count_21215 0
                                     i_21216 0]
                                (if (< i_21216 count_21215)
                                  (let [g (.nth ^clojure.lang.Indexed chunk_21214 (int i_21216))]
                                    (loop [seq_21217 (seq (:vals g))
                                           chunk_21218 nil
                                           count_21219 0
                                           i_21220 0]
                                      (if (< i_21220 count_21219)
                                        (let [v (.nth
                                                  ^clojure.lang.Indexed chunk_21218
                                                  (int i_21220))]
                                          (pace-gc)
                                          (deref (cluster/delete cluster v))
                                          (recur seq_21217 chunk_21218 count_21219 (inc i_21220)))
                                        (let [temp__5804__auto__ (seq seq_21217)]
                                          (when temp__5804__auto__
                                            (let [seq_21217 temp__5804__auto__]
                                              (if (chunked-seq? seq_21217)
                                                (let [c__6065__auto__ (chunk-first seq_21217)]
                                                  (recur
                                                    (chunk-rest seq_21217)
                                                    c__6065__auto__
                                                    (int (count c__6065__auto__))
                                                    (int 0)))
                                                (let [v (first seq_21217)]
                                                  (pace-gc)
                                                  (deref (cluster/delete cluster v))
                                                  (recur (next seq_21217) nil 0 0))))))))
                                    (recur seq_21213 chunk_21214 count_21215 (inc i_21216)))
                                  (let [temp__5804__auto__ (seq seq_21213)]
                                    (when temp__5804__auto__
                                      (let [seq_21213 temp__5804__auto__]
                                        (if (chunked-seq? seq_21213)
                                          (let [c__6065__auto__ (chunk-first seq_21213)]
                                            (recur
                                              (chunk-rest seq_21213)
                                              c__6065__auto__
                                              (int (count c__6065__auto__))
                                              (int 0)))
                                          (let [g (first seq_21213)]
                                            (loop [seq_21221 (seq (:vals g))
                                                   chunk_21222 nil
                                                   count_21223 0
                                                   i_21224 0]
                                              (if (< i_21224 count_21223)
                                                (let [v (.nth
                                                          ^clojure.lang.Indexed chunk_21222
                                                          (int i_21224))]
                                                  (pace-gc)
                                                  (deref (cluster/delete cluster v))
                                                  (recur
                                                    seq_21221
                                                    chunk_21222
                                                    count_21223
                                                    (inc i_21224)))
                                                (let [temp__5804__auto__ (seq seq_21221)]
                                                  (when temp__5804__auto__
                                                    (let [seq_21221 temp__5804__auto__]
                                                      (if (chunked-seq? seq_21221)
                                                        (let [c__6065__auto__
                                                              (chunk-first seq_21221)]
                                                          (recur
                                                            (chunk-rest seq_21221)
                                                            c__6065__auto__
                                                            (int (count c__6065__auto__))
                                                            (int 0)))
                                                        (let [v (first seq_21221)]
                                                          (pace-gc)
                                                          (deref (cluster/delete cluster v))
                                                          (recur (next seq_21221) nil 0 0))))))))
                                            (recur (next seq_21213) nil 0 0))))))))
                              (apply + n (map (comp count :vals) garbage)))))
                        0
                        (deref garbage-agent))]
                (println "Deleted" n "catalog segments")))
            (println "GC deleted dbs: no deleted dbs found."))))))
  (reset-meta!
    #'gc-deleted-dbs
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri]}]),
       :doc
       "Runs deleted-database storage collection for every id listed as deleted in the catalog addressed by uri. Restored database ids are removed from the deleted list without collection; remaining ids are traversed individually, and buffered catalog garbage is drained after the pass. Progress is printed and storage pacing follows datomic.gcStoragePaceMsec.",
       :column (int 1)}
      :name
      'gc-deleted-dbs
      :ns
      *ns*)))

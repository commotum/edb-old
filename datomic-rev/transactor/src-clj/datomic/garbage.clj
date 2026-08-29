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
        (fn fn__24788 ([root_entry] (:children (get lookup (str (:uuid root_entry))))))
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
        (fn fn__24791 ([dir_entry] (:children (get lookup (str (:uuid dir_entry))))))
        (dir-seq lookup root))))
  (reset-meta!
    #'leaf-seq
    (assoc
      {:arglists (clojure.core/list ['lookup 'root]), :column (int 1)}
      :name
      'leaf-seq
      :ns
      *ns*))
  (def ensure-root-ref
   (fn ensure_root_ref
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
                           (cluster/reset-ref
                             cluster
                             root_key
                             (cluster/uuid->val-key root_uuid)))))
                   (deref (cluster/get-ref cluster root_key))
                   (do (throw (java.lang.Exception. "Garbage root creation failed")) nil))))))))
     ([cluster] (ensure-root-ref cluster false))))
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
  (def append-leaf
   (fn append_leaf
     ([cluster lookup leaf max_dir_size]
       (let [map__24804 (ensure-root-ref cluster)
             map__24804 (if (seq? map__24804)
                          (if (next map__24804)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24804))
                            (if (seq map__24804) (first map__24804) {}))
                          map__24804)
             old_root_val_key (get map__24804 :key)
             old_root_rev (get map__24804 :rev)
             vec__24805 (repeatedly common/rand-uuid)
             leaf_uuid (nth vec__24805 (int 0) nil)
             dir_uuid (nth vec__24805 (int 1) nil)
             root_uuid (nth vec__24805 (int 2) nil)
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
                             (fn fn__24808 ([p1__24801#] (conj p1__24801# dir_entry)))))
             new_root_val (if new_tail_dir?
                            (update-in
                              old_root_val
                              [:children]
                              (fn fn__24810 ([p1__24802#] (conj p1__24802# root_entry))))
                            (update-in
                              old_root_val
                              [:children]
                              (fn fn__24812
                                ([p1__24803#]
                                  (assoc p1__24803# (long (dec (count p1__24803#))) root_entry)))))
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
           (do (throw (java.lang.Exception. "Value create failed")) nil))))))
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
  (def do-mark-garbage
   (fn do_mark_garbage
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
                           (fn fn__24819
                             ([p1__24818#]
                               (java.lang.Integer/valueOf (int (count (:vals p1__24818#))))))
                           cluster_garbage))]
         (if (< max_leaf_size leaf_size)
           (do
             (let [m_24821 {:event :garbage/append-leaf, :segments leaf_size}
                   ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                  "datomic.garbage")]
                                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                       (.info
                                         ^org.slf4j.Logger logger
                                         (logger/process (assoc m_24821 :phase :begin))))
                                     nil)
                   start__8584__auto__ (java.lang.System/nanoTime)
                   result__8585__auto__ (try
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
                                            t__8586__auto__
                                            {:threw t__8586__auto__}))
                   elapsed_24822 (- (java.lang.System/nanoTime) start__8584__auto__)
                   msec_24823 (logger/format-as-msec (long elapsed_24822))]
               (let [endmsg__8587__auto__ (merge
                                            (assoc m_24821 :msec msec_24823 :phase :end)
                                            (when (:threw result__8585__auto__)
                                              {:threw (class (:threw result__8585__auto__))}))
                     logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                 (when (.isInfoEnabled ^org.slf4j.Logger logger)
                   (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                 nil)
               (if (contains? result__8585__auto__ :returned)
                 (:returned result__8585__auto__)
                 (throw (:threw result__8585__auto__))))
             (dissoc m cluster))
           (assoc m cluster cluster_garbage))))))
  (reset-meta!
    #'do-mark-garbage
    (assoc
      {:arglists (clojure.core/list ['m 'cluster 'lookup 'ids 'max-leaf-size 'max-dir-size]),
       :column (int 1)}
      :name
      'do-mark-garbage
      :ns
      *ns*))
  (def mark-garbage
   (fn mark_garbage
     ([cluster lookup ids max_leaf_size max_dir_size]
       (do
         (loop [seq_24833 (seq (partition-all max_leaf_size ids))
                chunk_24834 nil
                count_24835 0
                i_24836 0]
           (if (< i_24836 count_24835)
             (let [chunks (.nth ^clojure.lang.Indexed chunk_24834 (int i_24836))]
               (send-off
                 garbage-agent
                 do-mark-garbage
                 cluster
                 lookup
                 chunks
                 max_leaf_size
                 max_dir_size)
               (recur seq_24833 chunk_24834 count_24835 (inc i_24836)))
             (let [temp__5804__auto__ (seq seq_24833)]
               (when temp__5804__auto__
                 (let [seq_24833 temp__5804__auto__]
                   (if (chunked-seq? seq_24833)
                     (let [c__6065__auto__ (chunk-first seq_24833)]
                       (recur
                         (chunk-rest seq_24833)
                         c__6065__auto__
                         (int (count c__6065__auto__))
                         (int 0)))
                     (let [chunks (first seq_24833)]
                       (send-off
                         garbage-agent
                         do-mark-garbage
                         cluster
                         lookup
                         chunks
                         max_leaf_size
                         max_dir_size)
                       (recur (next seq_24833) nil 0 0))))))))
         :ok))
     ([cluster lookup ids] (mark-garbage cluster lookup ids 1000 800))))
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
                       (fn fn__24840 ([cluster] (domain/system-cache-olookup cluster)))
                       10)]
        (events/subscribe
          :datomic.garbage/mark
          :datomic.garbage/mark
          (fn fn__24843
            ([p__24842]
              (let [map__24844 p__24842
                    map__24844 (if (seq? map__24844)
                                 (if (next map__24844)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__24844))
                                   (if (seq map__24844) (first map__24844) {}))
                                 map__24844)
                    cluster (get map__24844 :cluster)
                    garbage (get map__24844 :garbage)]
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
          (fn fn__24849
            ([p1__24848#] (java.lang.Integer/valueOf (int (count (:vals p1__24848#))))))
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
  (defn gc-delete-vals
    ([cluster vs]
      (let [futs (mapv
                   (fn fn__24857 ([p1__24856#] (cluster/delete cluster p1__24856#)))
                   (remove empty? vs))
            result (reduce
                     (fn fn__24859 ([count fut] (pace-gc) (+ count (if (= :ok (deref fut)) 1 0))))
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
               (fn fn__24864
                 ([count p__24863]
                   (let [map__24865 p__24863
                         map__24865 (if (seq? map__24865)
                                      (if (next map__24865)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__24865))
                                        (if (seq map__24865) (first map__24865) {}))
                                      map__24865)
                         vals (get map__24865 :vals)]
                     (+ count (gc-delete-vals cluster vals)))))
               0
               (take-while
                 (fn fn__24867
                   ([p1__24862#] (neg? (clojure.lang.Util/compare (:tstamp p1__24862#) tstamp))))
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
               (fn fn__24873
                 ([total p__24872]
                   (let [map__24874 p__24872
                         map__24874 (if (seq? map__24874)
                                      (if (next map__24874)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__24874))
                                        (if (seq map__24874) (first map__24874) {}))
                                      map__24874)
                         uuid (get map__24874 :uuid)
                         map__24875 (gc-leaf cluster uuid tstamp)
                         map__24875 (if (seq? map__24875)
                                      (if (next map__24875)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__24875))
                                        (if (seq map__24875) (first map__24875) {}))
                                      map__24875)
                         count (get map__24875 :count)
                         complete (get map__24875 :complete)]
                     (if complete
                       (+ (+ total count) (gc-delete-vals cluster [(cluster/uuid->val-key uuid)]))
                       (+ total count)))))
               0
               (take-while
                 (fn fn__24877
                   ([p1__24871#] (neg? (clojure.lang.Util/compare (:start p1__24871#) tstamp))))
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
  (def gc
   (fn gc
     ([cluster tstamp progress]
       (let [root (gc-get-node cluster (cluster/val-key->uuid (:key (ensure-root-ref cluster))))
             count (reduce
                     (fn fn__24883
                       ([total p__24882]
                         (let [map__24884 p__24882
                               map__24884 (if (seq? map__24884)
                                            (if (next map__24884)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__24884))
                                              (if (seq map__24884) (first map__24884) {}))
                                            map__24884)
                               uuid (get map__24884 :uuid)
                               map__24885 (gc-dir cluster uuid tstamp)
                               map__24885 (if (seq? map__24885)
                                            (if (next map__24885)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__24885))
                                              (if (seq map__24885) (first map__24885) {}))
                                            map__24885)
                               count (get map__24885 :count)
                               complete (get map__24885 :complete)]
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
                       (fn fn__24887
                         ([p1__24881#]
                           (neg? (clojure.lang.Util/compare (:start p1__24881#) tstamp))))
                       (:children root)))]
         (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info
               ^org.slf4j.Logger logger
               (logger/process {:event :garbage/collected, :count count})))
           nil)
         count))
     ([cluster tstamp] (gc cluster tstamp identity))))
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
  (def queue-gc
   (fn queue_gc
     ([cluster older_than]
       (send-off
         collection-agent
         (fn fn__24890
           ([_]
             (try
               (let [m_24892 {:event :garbage/collect,
                              :dbid (cluster/dbId cluster),
                              :older-than older_than}
                     ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.garbage")]
                                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                         (.info
                                           ^org.slf4j.Logger logger
                                           (logger/process (assoc m_24892 :phase :begin))))
                                       nil)
                     start__8584__auto__ (java.lang.System/nanoTime)
                     result__8585__auto__ (try
                                            {:returned (gc cluster older_than)}
                                            (catch
                                              java.lang.Throwable
                                              t__8586__auto__
                                              {:threw t__8586__auto__}))
                     elapsed_24893 (- (java.lang.System/nanoTime) start__8584__auto__)
                     msec_24894 (logger/format-as-msec (long elapsed_24893))]
                 (let [endmsg__8587__auto__ (merge
                                              (assoc m_24892 :msec msec_24894 :phase :end)
                                              (when (:threw result__8585__auto__)
                                                {:threw (class (:threw result__8585__auto__))}))
                       logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                     (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                   nil)
                 (if (contains? result__8585__auto__ :returned)
                   (:returned result__8585__auto__)
                   (do (throw (:threw result__8585__auto__)) nil)))
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
             nil))))))
  (reset-meta!
    #'queue-gc
    (assoc
      {:arglists (clojure.core/list ['cluster 'older-than]), :column (int 1)}
      :name
      'queue-gc
      :ns
      *ns*))
  (def gc-deleted-db
   (fn gc_deleted_db
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
                 (fn fn__24906
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
                 (fn fn__24908
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
         true))))
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
  (def gc-deleted-dbs
   (fn gc_deleted_dbs
     ([p__24916]
       (let [map__24917 p__24916
             map__24917 (if (seq? map__24917)
                          (if (next map__24917)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24917))
                            (if (seq map__24917) (first map__24917) {}))
                          map__24917)
             uri (get map__24917 :uri)]
         (install-mark-handler)
         (let [map__24918 (get-db-ids uri)
               map__24918 (if (seq? map__24918)
                            (if (next map__24918)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__24918))
                              (if (seq map__24918) (first map__24918) {}))
                            map__24918)
               deleted (get map__24918 :deleted)
               active (get map__24918 :active)]
           (if (seq deleted)
             (do
               (println
                 "Deleting storage for"
                 (java.lang.Integer/valueOf (int (count deleted)))
                 "deleted dbs.")
               (let [cluster_conf (uri/parse uri)
                     system_cluster (coord/create-system-cluster cluster_conf)]
                 (loop [seq_24919 (seq deleted) chunk_24920 nil count_24921 0 i_24922 0]
                   (if (< i_24922 count_24921)
                     (let [id (.nth ^clojure.lang.Indexed chunk_24920 (int i_24922))]
                       (if (get active id)
                         (do
                           (println "Database has been restored, skipping " id)
                           (catalog/remove-deleted-database system_cluster id))
                         (do
                           (println "Deleting storage for " id)
                           (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                 db_cluster (coord/create-db-cluster db_cluster_conf)]
                             (gc-deleted-db system_cluster db_cluster println))))
                       (recur seq_24919 chunk_24920 count_24921 (inc i_24922)))
                     (let [temp__5804__auto__ (seq seq_24919)]
                       (when temp__5804__auto__
                         (let [seq_24919 temp__5804__auto__]
                           (if (chunked-seq? seq_24919)
                             (let [c__6065__auto__ (chunk-first seq_24919)]
                               (recur
                                 (chunk-rest seq_24919)
                                 c__6065__auto__
                                 (int (count c__6065__auto__))
                                 (int 0)))
                             (let [id (first seq_24919)]
                               (if (get active id)
                                 (do
                                   (println "Database has been restored, skipping " id)
                                   (catalog/remove-deleted-database system_cluster id))
                                 (do
                                   (println "Deleting storage for " id)
                                   (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                         db_cluster (coord/create-db-cluster db_cluster_conf)]
                                     (gc-deleted-db system_cluster db_cluster println))))
                               (recur (next seq_24919) nil 0 0)))))))))
               (java.lang.Thread/sleep 100)
               (let [n (reduce
                         (fn fn__24924
                           ([n p__24923]
                             (let [vec__24925 p__24923
                                   cluster (nth vec__24925 (int 0) nil)
                                   garbage (nth vec__24925 (int 1) nil)]
                               (loop [seq_24928 (seq garbage)
                                      chunk_24929 nil
                                      count_24930 0
                                      i_24931 0]
                                 (if (< i_24931 count_24930)
                                   (let [g (.nth ^clojure.lang.Indexed chunk_24929 (int i_24931))]
                                     (loop [seq_24932 (seq (:vals g))
                                            chunk_24933 nil
                                            count_24934 0
                                            i_24935 0]
                                       (if (< i_24935 count_24934)
                                         (let [v (.nth
                                                   ^clojure.lang.Indexed chunk_24933
                                                   (int i_24935))]
                                           (pace-gc)
                                           (deref (cluster/delete cluster v))
                                           (recur seq_24932 chunk_24933 count_24934 (inc i_24935)))
                                         (let [temp__5804__auto__ (seq seq_24932)]
                                           (when temp__5804__auto__
                                             (let [seq_24932 temp__5804__auto__]
                                               (if (chunked-seq? seq_24932)
                                                 (let [c__6065__auto__ (chunk-first seq_24932)]
                                                   (recur
                                                     (chunk-rest seq_24932)
                                                     c__6065__auto__
                                                     (int (count c__6065__auto__))
                                                     (int 0)))
                                                 (let [v (first seq_24932)]
                                                   (pace-gc)
                                                   (deref (cluster/delete cluster v))
                                                   (recur (next seq_24932) nil 0 0))))))))
                                     (recur seq_24928 chunk_24929 count_24930 (inc i_24931)))
                                   (let [temp__5804__auto__ (seq seq_24928)]
                                     (when temp__5804__auto__
                                       (let [seq_24928 temp__5804__auto__]
                                         (if (chunked-seq? seq_24928)
                                           (let [c__6065__auto__ (chunk-first seq_24928)]
                                             (recur
                                               (chunk-rest seq_24928)
                                               c__6065__auto__
                                               (int (count c__6065__auto__))
                                               (int 0)))
                                           (let [g (first seq_24928)]
                                             (loop [seq_24936 (seq (:vals g))
                                                    chunk_24937 nil
                                                    count_24938 0
                                                    i_24939 0]
                                               (if (< i_24939 count_24938)
                                                 (let [v (.nth
                                                           ^clojure.lang.Indexed chunk_24937
                                                           (int i_24939))]
                                                   (pace-gc)
                                                   (deref (cluster/delete cluster v))
                                                   (recur
                                                     seq_24936
                                                     chunk_24937
                                                     count_24938
                                                     (inc i_24939)))
                                                 (let [temp__5804__auto__ (seq seq_24936)]
                                                   (when temp__5804__auto__
                                                     (let [seq_24936 temp__5804__auto__]
                                                       (if (chunked-seq? seq_24936)
                                                         (let [c__6065__auto__
                                                               (chunk-first seq_24936)]
                                                           (recur
                                                             (chunk-rest seq_24936)
                                                             c__6065__auto__
                                                             (int (count c__6065__auto__))
                                                             (int 0)))
                                                         (let [v (first seq_24936)]
                                                           (pace-gc)
                                                           (deref (cluster/delete cluster v))
                                                           (recur (next seq_24936) nil 0 0))))))))
                                             (recur (next seq_24928) nil 0 0))))))))
                               (apply + n (map (comp count :vals) garbage)))))
                         0
                         (deref garbage-agent))]
                 (println "Deleted" n "catalog segments")))
             (println "GC deleted dbs: no deleted dbs found.")))))))
  (reset-meta!
    #'gc-deleted-dbs
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri]}]), :column (int 1)}
      :name
      'gc-deleted-dbs
      :ns
      *ns*)))
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
      (let [temp__5457__auto__ (cluster/dbId cluster)]
        (when temp__5457__auto__ (let [dbid temp__5457__auto__] (str "ref-gc-root/" dbid))))))
  (def leaf-threshold 600)
  (defn create-garbage-node
    ([cluster uuid o]
      (let [buf (fressian/byte-buf o :handlers gf/write-handlers :footer true)
            zipped (io/gzip-buffer buf)
            key (cluster/uuid->val-key uuid)]
        (cluster/create-val cluster key zipped))))
  (reset-meta!
    #'create-garbage-node
    (assoc
      {:private true, :arglists (clojure.core/list ['cluster 'uuid 'o]), :column 1}
      :name
      'create-garbage-node
      :ns
      *ns*))
  (defn dir-seq
    ([lookup root]
      (mapcat
        (fn fn__19767 ([root_entry] (:children (get lookup (str (:uuid root_entry))))))
        (:children root))))
  (defn leaf-seq
    ([lookup root]
      (mapcat
        (fn fn__19770 ([dir_entry] (:children (get lookup (str (:uuid dir_entry))))))
        (dir-seq lookup root))))
  (defn ensure-root-ref
    ([cluster forget_garbage]
      (let [temp__5457__auto__ (root-ref-key cluster)]
        (when temp__5457__auto__
          (let [root_key temp__5457__auto__ result (deref (cluster/get-ref cluster root_key))]
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
  (defn ensure-root
    ([cluster lookup]
      (let [key (:key (ensure-root-ref cluster))]
        (common/getx lookup (cluster/val-key->uuid key)))))
  (defn append-leaf
    ([cluster lookup leaf max_dir_size]
      (let [map__19783 (ensure-root-ref cluster)
            map__19783 (if (seq? map__19783)
                         (clojure.lang.PersistentHashMap/create (seq map__19783))
                         map__19783)
            old_root_val_key (get map__19783 :key)
            old_root_rev (get map__19783 :rev)
            vec__19784 (repeatedly common/rand-uuid)
            leaf_uuid (nth vec__19784 (int 0) nil)
            dir_uuid (nth vec__19784 (int 1) nil)
            root_uuid (nth vec__19784 (int 2) nil)
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
                            (fn fn__19787 ([p1__19780#] (conj p1__19780# dir_entry)))))
            new_root_val (if new_tail_dir?
                           (update-in
                             old_root_val
                             [:children]
                             (fn fn__19789 ([p1__19781#] (conj p1__19781# root_entry))))
                           (update-in
                             old_root_val
                             [:children]
                             (fn fn__19791
                               ([p1__19782#]
                                 (assoc p1__19782# (long (dec (count p1__19782#))) root_entry)))))
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
                       :dir-to dir_uuid}))
                  nil)
                nil)
              :ok)
            (do (throw (java.lang.Exception. "Conflict")) nil))
          (do (throw (java.lang.Exception. "Value create failed")) nil)))))
  (def garbage-agent (agent {}))
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
                          (fn fn__19798
                            ([p1__19797#]
                              (java.lang.Integer/valueOf (int (count (:vals p1__19797#))))))
                          cluster_garbage))]
        (if (< max_leaf_size leaf_size)
          (do
            (let [m_19800 {:event :garbage/append-leaf, :segments leaf_size}
                  ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.garbage")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_19800 :phase :begin)))
                                      nil)
                                    nil)
                  start__8981__auto__ (java.lang.System/nanoTime)
                  result__8982__auto__ (try
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
                                           t__8983__auto__
                                           {:threw t__8983__auto__}))
                  elapsed_19801 (- (java.lang.System/nanoTime) start__8981__auto__)
                  msec_19802 (logger/format-as-msec (long elapsed_19801))]
              (let [endmsg__8984__auto__ (merge
                                           (assoc m_19800 :msec msec_19802 :phase :end)
                                           (when (:threw result__8982__auto__)
                                             {:threw (class (:threw result__8982__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                  nil)
                nil)
              (if (contains? result__8982__auto__ :returned)
                (:returned result__8982__auto__)
                (throw (:threw result__8982__auto__))))
            (dissoc m cluster))
          (assoc m cluster cluster_garbage)))))
  (defn mark-garbage
    ([cluster lookup ids max_leaf_size max_dir_size]
      (do
        (loop [seq_19812 (seq (partition-all max_leaf_size ids))
               chunk_19813 nil
               count_19814 0
               i_19815 0]
          (if (< i_19815 count_19814)
            (let [chunks (.nth ^clojure.lang.Indexed chunk_19813 (int i_19815))]
              (send-off
                garbage-agent
                do-mark-garbage
                cluster
                lookup
                chunks
                max_leaf_size
                max_dir_size)
              (recur seq_19812 chunk_19813 count_19814 (inc i_19815)))
            (let [temp__5457__auto__ (seq seq_19812)]
              (when temp__5457__auto__
                (let [seq_19812 temp__5457__auto__]
                  (if (chunked-seq? seq_19812)
                    (let [c__5719__auto__ (chunk-first seq_19812)]
                      (recur
                        (chunk-rest seq_19812)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [chunks (first seq_19812)]
                      (send-off
                        garbage-agent
                        do-mark-garbage
                        cluster
                        lookup
                        chunks
                        max_leaf_size
                        max_dir_size)
                      (recur (next seq_19812) nil 0 0))))))))
        :ok))
    ([cluster lookup ids] (mark-garbage cluster lookup ids 1000 800)))
  (defn install-mark-handler
    ([]
      (let [olookups (cache/create-computing
                       (fn fn__19819 ([cluster] (domain/system-cache-olookup cluster)))
                       10)]
        (events/subscribe
          :datomic.garbage/mark
          :datomic.garbage/mark
          (fn fn__19822
            ([p__19821]
              (let [map__19823 p__19821
                    map__19823 (if (seq? map__19823)
                                 (clojure.lang.PersistentHashMap/create (seq map__19823))
                                 map__19823)
                    cluster (get map__19823 :cluster)
                    garbage (get map__19823 :garbage)]
                (mark-garbage cluster (get olookups cluster) garbage))))))))
  (defn flush-garbage
    ([cluster olookup]
      (send-off garbage-agent do-mark-garbage cluster olookup nil 0 800)
      (await garbage-agent)))
  (defn pending-garbage-count
    ([gmap cluster]
      (apply
        +
        (map
          (fn fn__19828
            ([p1__19827#] (java.lang.Integer/valueOf (int (count (:vals p1__19827#))))))
          (get gmap cluster)))))
  (def gc-val->obj (fressian/val->obj gf/read-handlers))
  (defn gc-get-node
    ([cluster uuid]
      (let [temp__5457__auto__ (:buf
                                 (deref (cluster/get-val cluster (cluster/uuid->val-key uuid))))]
        (when temp__5457__auto__ (let [v temp__5457__auto__] (gc-val->obj v))))))
  (defn pace-gc
    ([]
      (let [temp__5457__auto__ (config/property "datomic.gcStoragePaceMsec")]
        (when temp__5457__auto__
          (let [pace temp__5457__auto__]
            (java.lang.Thread/sleep (long ^java.lang.Number pace))
            nil)))))
  (defn gc-delete-vals
    ([cluster vs]
      (let [futs (mapv
                   (fn fn__19836 ([p1__19835#] (cluster/delete cluster p1__19835#)))
                   (remove empty? vs))
            result (reduce
                     (fn fn__19838 ([count fut] (pace-gc) (+ count (if (= :ok (deref fut)) 1 0))))
                     0
                     futs)]
        (monitor/add-stat :GarbageDeletedCount result)
        result)))
  (defn gc-leaf
    ([cluster uuid tstamp]
      (let [temp__5455__auto__ (gc-get-node cluster uuid)]
        (if temp__5455__auto__
          (let [leaf temp__5455__auto__]
            {:count
             (reduce
               (fn fn__19843
                 ([count p__19842]
                   (let [map__19844 p__19842
                         map__19844 (if (seq? map__19844)
                                      (clojure.lang.PersistentHashMap/create (seq map__19844))
                                      map__19844)
                         vals (get map__19844 :vals)]
                     (+ count (gc-delete-vals cluster vals)))))
               0
               (take-while
                 (fn fn__19846
                   ([p1__19841#] (neg? (clojure.lang.Util/compare (:tstamp p1__19841#) tstamp))))
                 (:children leaf))),
             :complete
             (neg? (clojure.lang.Util/compare (:tstamp (last (:children leaf))) tstamp))})
          {:count 0, :complete true}))))
  (defn gc-dir
    ([cluster uuid tstamp]
      (let [temp__5455__auto__ (gc-get-node cluster uuid)]
        (if temp__5455__auto__
          (let [dir temp__5455__auto__]
            {:count
             (reduce
               (fn fn__19852
                 ([total p__19851]
                   (let [map__19853 p__19851
                         map__19853 (if (seq? map__19853)
                                      (clojure.lang.PersistentHashMap/create (seq map__19853))
                                      map__19853)
                         uuid (get map__19853 :uuid)
                         map__19854 (gc-leaf cluster uuid tstamp)
                         map__19854 (if (seq? map__19854)
                                      (clojure.lang.PersistentHashMap/create (seq map__19854))
                                      map__19854)
                         count (get map__19854 :count)
                         complete (get map__19854 :complete)]
                     (if complete
                       (+ (+ total count) (gc-delete-vals cluster [(cluster/uuid->val-key uuid)]))
                       (+ total count)))))
               0
               (take-while
                 (fn fn__19856
                   ([p1__19850#] (neg? (clojure.lang.Util/compare (:start p1__19850#) tstamp))))
                 (:children dir))),
             :complete (neg? (clojure.lang.Util/compare (:end (last (:children dir))) tstamp))})
          {:count 0, :complete true}))))
  (defn gc
    ([cluster tstamp progress]
      (let [root (gc-get-node cluster (cluster/val-key->uuid (:key (ensure-root-ref cluster))))
            count (reduce
                    (fn fn__19862
                      ([total p__19861]
                        (let [map__19863 p__19861
                              map__19863 (if (seq? map__19863)
                                           (clojure.lang.PersistentHashMap/create (seq map__19863))
                                           map__19863)
                              uuid (get map__19863 :uuid)
                              map__19864 (gc-dir cluster uuid tstamp)
                              map__19864 (if (seq? map__19864)
                                           (clojure.lang.PersistentHashMap/create (seq map__19864))
                                           map__19864)
                              count (get map__19864 :count)
                              complete (get map__19864 :complete)]
                          (^clojure.lang.IFn progress count)
                          (if complete
                            (do
                              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                  (.info
                                    ^org.slf4j.Logger logger
                                    (logger/process {:event :garbage/collect-dir, :id uuid}))
                                  nil)
                                nil)
                              (+
                                (+ total count)
                                (gc-delete-vals cluster [(cluster/uuid->val-key uuid)])))
                            (+ total count)))))
                    0
                    (take-while
                      (fn fn__19866
                        ([p1__19860#]
                          (neg? (clojure.lang.Util/compare (:start p1__19860#) tstamp))))
                      (:children root)))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process {:event :garbage/collected, :count count}))
            nil)
          nil)
        count))
    ([cluster tstamp] (gc cluster tstamp identity)))
  (def collection-agent (agent nil))
  (defn queue-gc
    ([cluster older_than]
      (send-off
        collection-agent
        (fn fn__19869
          ([_]
            (try
              (let [m_19871 {:event :garbage/collect,
                             :dbid (cluster/dbId cluster),
                             :older-than older_than}
                    ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.garbage")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process (assoc m_19871 :phase :begin)))
                                        nil)
                                      nil)
                    start__8981__auto__ (java.lang.System/nanoTime)
                    result__8982__auto__ (try
                                           {:returned (gc cluster older_than)}
                                           (catch
                                             java.lang.Throwable
                                             t__8983__auto__
                                             {:threw t__8983__auto__}))
                    elapsed_19872 (- (java.lang.System/nanoTime) start__8981__auto__)
                    msec_19873 (logger/format-as-msec (long elapsed_19872))]
                (let [endmsg__8984__auto__ (merge
                                             (assoc m_19871 :msec msec_19873 :phase :end)
                                             (when (:threw result__8982__auto__)
                                               {:threw (class (:threw result__8982__auto__))}))
                      logger (org.slf4j.LoggerFactory/getLogger "datomic.garbage")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                    nil)
                  nil)
                (if (contains? result__8982__auto__ :returned)
                  (:returned result__8982__auto__)
                  (do (throw (:threw result__8982__auto__)) nil)))
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
  (defn gc-deleted-db
    ([system_cluster db_cluster status_callback]
      (let [status (fn status ([& args] (^clojure.lang.IFn status_callback (apply str args))))
            olookup (domain/deserializing-repairing-lookup db_cluster)]
        (let [temp__5457__auto__ (config/property "datomic.gcStoragePaceMsec")]
          (when temp__5457__auto__
            (let [n temp__5457__auto__] (println "Pacing with datomic.gcStoragePaceMsec =" n))))
        (let [temp__5457__auto__ (log/root-id db_cluster)]
          (when temp__5457__auto__
            (let [log_root_id temp__5457__auto__
                  c (count (treewalk/log-tree-seq log_root_id olookup true))]
              (reduce
                (fn fn__19885
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
        (let [temp__5457__auto__ (:key
                                   (deref
                                     (cluster/get-ref
                                       db_cluster
                                       (index/index-ref-key-name db_cluster))))]
          (when temp__5457__auto__
            (let [index_root_id temp__5457__auto__
                  c (count (treewalk/index-tree-seq index_root_id olookup true))]
              (reduce
                (fn fn__19887
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
  (defn get-db-ids
    ([uri]
      (let [cluster_conf (uri/parse uri) protocol (:protocol cluster_conf)]
        (when (not= protocol :mem)
          (let [cat (catalog/get-catalog (coord/create-system-cluster cluster_conf))]
            {:deleted (:datomic/deleted cat), :active (catalog/db-ids cat)})))))
  (defn deleted-db-cluster-conf ([uri id] (assoc (dissoc (uri/parse uri) :db-name) :db-id id)))
  (defn gc-deleted-dbs
    ([p__19895]
      (let [map__19896 p__19895
            map__19896 (if (seq? map__19896)
                         (clojure.lang.PersistentHashMap/create (seq map__19896))
                         map__19896)
            uri (get map__19896 :uri)]
        (install-mark-handler)
        (let [map__19897 (get-db-ids uri)
              map__19897 (if (seq? map__19897)
                           (clojure.lang.PersistentHashMap/create (seq map__19897))
                           map__19897)
              deleted (get map__19897 :deleted)
              active (get map__19897 :active)]
          (if (seq deleted)
            (do
              (println
                "Deleting storage for"
                (java.lang.Integer/valueOf (int (count deleted)))
                "deleted dbs.")
              (let [cluster_conf (uri/parse uri)
                    system_cluster (coord/create-system-cluster cluster_conf)]
                (loop [seq_19898 (seq deleted) chunk_19899 nil count_19900 0 i_19901 0]
                  (if (< i_19901 count_19900)
                    (let [id (.nth ^clojure.lang.Indexed chunk_19899 (int i_19901))]
                      (if (get active id)
                        (do
                          (println "Database has been restored, skipping " id)
                          (catalog/remove-deleted-database system_cluster id))
                        (do
                          (println "Deleting storage for " id)
                          (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                db_cluster (coord/create-db-cluster db_cluster_conf)]
                            (gc-deleted-db system_cluster db_cluster println))))
                      (recur seq_19898 chunk_19899 count_19900 (inc i_19901)))
                    (let [temp__5457__auto__ (seq seq_19898)]
                      (when temp__5457__auto__
                        (let [seq_19898 temp__5457__auto__]
                          (if (chunked-seq? seq_19898)
                            (let [c__5719__auto__ (chunk-first seq_19898)]
                              (recur
                                (chunk-rest seq_19898)
                                c__5719__auto__
                                (int (count c__5719__auto__))
                                (int 0)))
                            (let [id (first seq_19898)]
                              (if (get active id)
                                (do
                                  (println "Database has been restored, skipping " id)
                                  (catalog/remove-deleted-database system_cluster id))
                                (do
                                  (println "Deleting storage for " id)
                                  (let [db_cluster_conf (deleted-db-cluster-conf uri id)
                                        db_cluster (coord/create-db-cluster db_cluster_conf)]
                                    (gc-deleted-db system_cluster db_cluster println))))
                              (recur (next seq_19898) nil 0 0)))))))))
              (java.lang.Thread/sleep 100)
              (let [n (reduce
                        (fn fn__19903
                          ([n p__19902]
                            (let [vec__19904 p__19902
                                  cluster (nth vec__19904 (int 0) nil)
                                  garbage (nth vec__19904 (int 1) nil)]
                              (loop [seq_19907 (seq garbage)
                                     chunk_19908 nil
                                     count_19909 0
                                     i_19910 0]
                                (if (< i_19910 count_19909)
                                  (let [g (.nth ^clojure.lang.Indexed chunk_19908 (int i_19910))]
                                    (loop [seq_19911 (seq (:vals g))
                                           chunk_19912 nil
                                           count_19913 0
                                           i_19914 0]
                                      (if (< i_19914 count_19913)
                                        (let [v (.nth
                                                  ^clojure.lang.Indexed chunk_19912
                                                  (int i_19914))]
                                          (pace-gc)
                                          (deref (cluster/delete cluster v))
                                          (recur seq_19911 chunk_19912 count_19913 (inc i_19914)))
                                        (let [temp__5457__auto__ (seq seq_19911)]
                                          (when temp__5457__auto__
                                            (let [seq_19911 temp__5457__auto__]
                                              (if (chunked-seq? seq_19911)
                                                (let [c__5719__auto__ (chunk-first seq_19911)]
                                                  (recur
                                                    (chunk-rest seq_19911)
                                                    c__5719__auto__
                                                    (int (count c__5719__auto__))
                                                    (int 0)))
                                                (let [v (first seq_19911)]
                                                  (pace-gc)
                                                  (deref (cluster/delete cluster v))
                                                  (recur (next seq_19911) nil 0 0))))))))
                                    (recur seq_19907 chunk_19908 count_19909 (inc i_19910)))
                                  (let [temp__5457__auto__ (seq seq_19907)]
                                    (when temp__5457__auto__
                                      (let [seq_19907 temp__5457__auto__]
                                        (if (chunked-seq? seq_19907)
                                          (let [c__5719__auto__ (chunk-first seq_19907)]
                                            (recur
                                              (chunk-rest seq_19907)
                                              c__5719__auto__
                                              (int (count c__5719__auto__))
                                              (int 0)))
                                          (let [g (first seq_19907)]
                                            (loop [seq_19915 (seq (:vals g))
                                                   chunk_19916 nil
                                                   count_19917 0
                                                   i_19918 0]
                                              (if (< i_19918 count_19917)
                                                (let [v (.nth
                                                          ^clojure.lang.Indexed chunk_19916
                                                          (int i_19918))]
                                                  (pace-gc)
                                                  (deref (cluster/delete cluster v))
                                                  (recur
                                                    seq_19915
                                                    chunk_19916
                                                    count_19917
                                                    (inc i_19918)))
                                                (let [temp__5457__auto__ (seq seq_19915)]
                                                  (when temp__5457__auto__
                                                    (let [seq_19915 temp__5457__auto__]
                                                      (if (chunked-seq? seq_19915)
                                                        (let [c__5719__auto__
                                                              (chunk-first seq_19915)]
                                                          (recur
                                                            (chunk-rest seq_19915)
                                                            c__5719__auto__
                                                            (int (count c__5719__auto__))
                                                            (int 0)))
                                                        (let [v (first seq_19915)]
                                                          (pace-gc)
                                                          (deref (cluster/delete cluster v))
                                                          (recur (next seq_19915) nil 0 0))))))))
                                            (recur (next seq_19907) nil 0 0))))))))
                              (apply + n (map (comp count :vals) garbage)))))
                        0
                        (deref garbage-agent))]
                (println "Deleted" n "catalog segments")))
            (println "GC deleted dbs: no deleted dbs found.")))))))
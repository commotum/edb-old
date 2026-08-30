(do
  (clojure.core/in-ns 'datomic.tools)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['clojure.pprint :as 'pp]
        ['datomic.api :as 'd]
        ['datomic.db :as 'db]
        ['datomic.db-io :as 'db-io]
        ['datomic.cache :as 'cache]
        ['datomic.catalog :as 'catalog]
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.coordination :as 'coord]
        ['datomic.domain :as 'domain]
        ['datomic.index :as 'index]
        ['datomic.iter :as 'iter]
        ['datomic.kv-cluster :as 'kvc]
        ['datomic.log :as 'log]
        ['datomic.math :as 'math]
        ['datomic.memory-size :as 'size]
        ['datomic.monitor :as 'monitor]
        ['datomic.peer :as 'peer]
        ['datomic.require :as 'req]
        ['datomic.slf4j :as 'logger]
        ['datomic.stats :as 'stats]
        ['datomic.uri :as 'uri])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'datomic.Datom)))
  (when-not (.equals 'datomic.tools 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['clojure.pprint :as 'pp]
          ['datomic.api :as 'd]
          ['datomic.db :as 'db]
          ['datomic.db-io :as 'db-io]
          ['datomic.cache :as 'cache]
          ['datomic.catalog :as 'catalog]
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.coordination :as 'coord]
          ['datomic.domain :as 'domain]
          ['datomic.index :as 'index]
          ['datomic.iter :as 'iter]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.log :as 'log]
          ['datomic.math :as 'math]
          ['datomic.memory-size :as 'size]
          ['datomic.monitor :as 'monitor]
          ['datomic.peer :as 'peer]
          ['datomic.require :as 'req]
          ['datomic.slf4j :as 'logger]
          ['datomic.stats :as 'stats]
          ['datomic.uri :as 'uri])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'datomic.Datom))))
  (set! *warn-on-reflection* true)
  (req/maybe-require 'datomic.ddb)
  (def TOOLS_VERSION 1)
  (reset-meta!
    #'TOOLS_VERSION
    (assoc {:const true, :column (int 1)} :name 'TOOLS_VERSION :ns *ns*))
  (def datom-read-handler
   (fn datom_read_handler
     ([p__19845]
       (let [vec__19846 p__19845
             e (nth vec__19846 (int 0) nil)
             a (nth vec__19846 (int 1) nil)
             v (nth vec__19846 (int 2) nil)
             tx (nth vec__19846 (int 3) nil)
             op (nth vec__19846 (int 4) nil)]
         (if op
           (db/asserting-datum
             (long ^java.lang.Number e)
             (long ^java.lang.Number a)
             v
             (d/tx->t tx))
           (db/retracting-datum
             (long ^java.lang.Number e)
             (long ^java.lang.Number a)
             v
             (d/tx->t tx)))))))
  (reset-meta!
    #'datom-read-handler
    (assoc
      {:arglists (clojure.core/list [['e 'a 'v 'tx 'op]]), :column (int 1)}
      :name
      'datom-read-handler
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.tools" "serializer") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "serializer") (agent nil))
  (def serialized
   (fn serialized
     ([f agt]
       (fn fn__19850
         ([& args]
           (send-off
             agt
             (fn fn__19851
               ([_]
                 (try
                   (apply f args)
                   (catch
                     java.lang.Throwable
                     t
                     (do (.printStackTrace ^java.lang.Throwable t) nil)))
                 nil)))
           nil)))
     ([f] (serialized f serializer))))
  (reset-meta!
    #'serialized
    (assoc
      {:arglists (clojure.core/list ['f] ['f 'agt]), :column (int 1)}
      :name
      'serialized
      :ns
      *ns*))
  (defn prn-err ([x] (binding [*print-length* nil *out* *err*] (prn x))))
  (reset-meta!
    #'prn-err
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'prn-err :ns *ns*))
  (defn println-err ([x] (binding [*out* *err*] (println x))))
  (reset-meta!
    #'println-err
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'println-err :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.tools" "progress") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.tools" "progress")
    (serialized (fn fn__19859 ([report data] (^clojure.lang.IFn report data)))))
  (defn flush-progress ([] (await serializer)))
  (reset-meta!
    #'flush-progress
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'flush-progress :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.tools" "connection-resources") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "connection-resources") db-io/storage-resources)
  (.setMeta (clojure.lang.RT/var "datomic.tools" "index-db") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "index-db") db-io/index-db)
  (.setMeta (clojure.lang.RT/var "datomic.tools" "log") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "log") db-io/log)
  (.setMeta (clojure.lang.RT/var "datomic.tools" "db-resources") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "db-resources") db-io/db-resources)
  (def log-entry->next-t
   (fn log_entry__GT_next_t ([log_entry] (inc (log/max-eidx (:data log_entry))))))
  (reset-meta!
    #'log-entry->next-t
    (assoc
      {:arglists (clojure.core/list ['log-entry]), :column (int 1)}
      :name
      'log-entry->next-t
      :ns
      *ns*))
  (defn index-has-t?
    ([db t]
      (let [idb (assoc db :memidx db/mem-index-set)]
        (boolean (seq (d/datoms idb :eavt (d/t->tx (long ^java.lang.Number t))))))))
  (reset-meta!
    #'index-has-t?
    (assoc {:arglists (clojure.core/list ['db 't]), :column (int 1)} :name 'index-has-t? :ns *ns*))
  (defn set-index-basis
    ([uri basisT]
      (let [map__19864 (connection-resources uri)
            map__19864 (if (seq? map__19864)
                         (if (next map__19864)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19864))
                           (if (seq map__19864) (first map__19864) {}))
                         map__19864)
            cr map__19864
            cluster (get map__19864 :cluster)
            olookup (get map__19864 :olookup)
            map__19865 (db-resources cr)
            map__19865 (if (seq? map__19865)
                         (if (next map__19865)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19865))
                           (if (seq map__19865) (first map__19865) {}))
                         map__19865)
            db (get map__19865 :db)
            log (get map__19865 :log)
            tx (first (d/tx-range log basisT (inc basisT)))
            nextT (log-entry->next-t tx)
            refkey (index/index-ref-key-name cluster)
            k (:key (deref (cluster/get-ref cluster refkey)))]
        (when (and tx k (index-has-t? db basisT))
          (let [index (get olookup k)
                uuid (index/write-object
                       cluster
                       olookup
                       (assoc index :basisT basisT :nextT nextT))]
            (when (= :ok (deref (cluster/reset-ref cluster refkey (cluster/uuid->val-key uuid))))
              basisT))))))
  (reset-meta!
    #'set-index-basis
    (assoc
      {:arglists (clojure.core/list ['uri 'basisT]), :column (int 1)}
      :name
      'set-index-basis
      :ns
      *ns*))
  (defn get-val ([conn k] (get (peer/get-olookup conn) k)))
  (reset-meta!
    #'get-val
    (assoc {:arglists (clojure.core/list ['conn 'k]), :column (int 1)} :name 'get-val :ns *ns*))
  (defn get-index-ref
    ([cluster] (deref (cluster/get-ref cluster (index/index-ref-key-name cluster)))))
  (reset-meta!
    #'get-index-ref
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'get-index-ref
      :ns
      *ns*))
  (def reset-index-ref
   (fn reset_index_ref
     ([cluster root_id]
       (when-not (string? root_id)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list 'string? 'root-id))))))
       (deref (cluster/reset-ref cluster (index/index-ref-key-name cluster) root_id)))))
  (reset-meta!
    #'reset-index-ref
    (assoc
      {:private true, :arglists (clojure.core/list ['cluster 'root-id]), :column (int 1)}
      :name
      'reset-index-ref
      :ns
      *ns*))
  (def replace-index
   (fn replace_index
     ([cluster index_id]
       (when-not (string? index_id)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list 'string? 'index-id))))))
       (let [m_19872 {:event :tools/replace-index, :id index_id}
             ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.tools")]
                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                 (.info
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_19872 :phase :begin))))
                               nil)
             start__8584__auto__ (java.lang.System/nanoTime)
             result__8585__auto__ (try
                                    {:returned
                                     (loop [n 0]
                                       (do
                                         (when (< 10 n)
                                           (throw
                                             (java.lang.RuntimeException.
                                               "Retry limit exceeded replacing index.")))
                                         (if (= :ok (reset-index-ref cluster index_id))
                                           true
                                           (recur (inc n)))))}
                                    (catch
                                      java.lang.Throwable
                                      t__8586__auto__
                                      {:threw t__8586__auto__}))
             elapsed_19873 (- (java.lang.System/nanoTime) start__8584__auto__)
             msec_19874 (logger/format-as-msec (long elapsed_19873))]
         (let [endmsg__8587__auto__ (merge
                                      (assoc m_19872 :msec msec_19874 :phase :end)
                                      (when (:threw result__8585__auto__)
                                        {:threw (class (:threw result__8585__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.tools")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
           nil)
         (if (contains? result__8585__auto__ :returned)
           (:returned result__8585__auto__)
           (do (throw (:threw result__8585__auto__)) nil))))))
  (reset-meta!
    #'replace-index
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['cluster 'index-id]
           {:pre [(.withMeta (clojure.core/list 'string? 'index-id) {:column (int 10)})]})),
       :column (int 1)}
      :name
      'replace-index
      :ns
      *ns*))
  (defn touch-index-ref ([cluster] (cluster/touch-ref cluster (index/index-ref-key-name cluster))))
  (reset-meta!
    #'touch-index-ref
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'touch-index-ref
      :ns
      *ns*))
  (defn log-size*
    ([conn t]
      (let [log (d/log conn) db (d/db conn)]
        (reduce
          (fn fn__19885 ([size logentry] (+ size (size/memory-size logentry))))
          0
          (d/tx-range log t nil)))))
  (reset-meta!
    #'log-size*
    (assoc {:arglists (clojure.core/list ['conn 't]), :column (int 1)} :name 'log-size* :ns *ns*))
  (defn log-size ([uri t] (let [conn (d/connect uri)] (log-size* conn t))))
  (reset-meta!
    #'log-size
    (assoc {:arglists (clojure.core/list ['uri 't]), :column (int 1)} :name 'log-size :ns *ns*))
  (def get-index
   (fn get_index
     ([p__19889]
       (let [map__19890 p__19889
             map__19890 (if (seq? map__19890)
                          (if (next map__19890)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19890))
                            (if (seq map__19890) (first map__19890) {}))
                          map__19890)
             cluster (get map__19890 :cluster)
             olookup (get map__19890 :olookup)]
         (get
           olookup
           (:key (deref (cluster/get-ref cluster (index/index-ref-key-name cluster)))))))))
  (reset-meta!
    #'get-index
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]}]), :column (int 1)}
      :name
      'get-index
      :ns
      *ns*))
  (defn cr? ([o] (and (:cluster o) (:olookup o))))
  (reset-meta!
    #'cr?
    (assoc {:arglists (clojure.core/list ['o]), :column (int 1)} :name 'cr? :ns *ns*))
  (def tx-range-from-log
   (fn tx_range_from_log
     ([cr start end]
       (when-not (cr? cr)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list 'cr? 'cr))))))
       (let [s (iter/iter-seq (log/seek-tx (log/find-log (:cluster cr) (:olookup cr)) start))]
         (if end (take-while (fn fn__19894 ([logentry] (< (:t logentry) end))) s) s)))))
  (reset-meta!
    #'tx-range-from-log
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['cr 'start 'end]
           {:pre [(.withMeta (clojure.core/list 'cr? 'cr) {:column (int 10)})]})),
       :column (int 1)}
      :name
      'tx-range-from-log
      :ns
      *ns*))
  (defn unique-identities
    ([db]
      (mapv
        :id
        (filter
          (fn fn__19899 ([p1__19898#] (= :db.unique/identity (:unique p1__19898#))))
          (map
            (fn fn__19901 ([p1__19897#] (d/attribute db (:v p1__19897#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (reset-meta!
    #'unique-identities
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'unique-identities
      :ns
      *ns*))
  (defn read-capacity-units
    ([uri]
      (let [map__19904 (uri/parse uri)
            map__19904 (if (seq? map__19904)
                         (if (next map__19904)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19904))
                           (if (seq map__19904) (first map__19904) {}))
                         map__19904)
            parsed_uri map__19904
            protocol (get map__19904 :protocol)]
        (cond
          (= :ddb protocol) (-> ((resolve 'datomic.ddb/client) nil {:region (:region parsed_uri)})
                             ((resolve 'datomic.ddb/describe-table)
                               {:tableName (:system-root parsed_uri)})
                             (:table)
                             (:provisionedThroughput)
                             (:readCapacityUnits))
          (= :ddb+s3 protocol) (do
                                 (-> ((resolve 'datomic.ddb/client)
                                       nil
                                       {:region (:aws-region parsed_uri)})
                                  ((resolve 'datomic.ddb/describe-table)
                                    {:tableName (:aws-dynamodb-table parsed_uri)})
                                  (:table)
                                  (:provisionedThroughput)
                                  (:readCapacityUnits)))))))
  (reset-meta!
    #'read-capacity-units
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'read-capacity-units
      :ns
      *ns*))
  (def pace-msec-per-seg
   (fn pace_msec_per_seg
     ([provisioned_kbs]
       (let [use_kbs (/ provisioned_kbs 2)
             item_per_kb 0.03
             msec_per_sec 1000.0
             latency_msec 25
             pace_msec (- (/ (/ msec_per_sec use_kbs) item_per_kb) latency_msec)]
         (when (> pace_msec 0.0) (long pace_msec))))))
  (reset-meta!
    #'pace-msec-per-seg
    (assoc
      {:arglists (clojure.core/list ['provisioned-kbs]), :column (int 1)}
      :name
      'pace-msec-per-seg
      :ns
      *ns*))
  (def pace-fn
   (fn pace_fn
     ([pace_msec]
       (let [last_val_gets (atom (deref kvc/val-gets-ref))]
         (fn fn__19907
           ([]
             (let [new_val_gets (deref kvc/val-gets-ref)
                   msec (* (- new_val_gets (deref last_val_gets)) pace_msec)]
               (when (< 10 msec)
                 (monitor/add-stat :ToolsPaceReadMsec msec)
                 (java.lang.Thread/sleep (long ^java.lang.Number msec))
                 (reset! last_val_gets new_val_gets)))))))))
  (reset-meta!
    #'pace-fn
    (assoc {:arglists (clojure.core/list ['pace-msec]), :column (int 1)} :name 'pace-fn :ns *ns*))
  (defn peer-diagnostics
    ([]
      {:peer-config (deref datomic.config/properties-ref),
       :object-cache-count (cache/fast-count (domain/system-cache)),
       :memory-mb (math/rounded-mb (long (.maxMemory (java.lang.Runtime/getRuntime))))}))
  (reset-meta!
    #'peer-diagnostics
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'peer-diagnostics :ns *ns*))
  (def count-log-segs
   (fn count_log_segs
     ([p__19911]
       (let [map__19912 p__19911
             map__19912 (if (seq? map__19912)
                          (if (next map__19912)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19912))
                            (if (seq map__19912) (first map__19912) {}))
                          map__19912)
             cluster (get map__19912 :cluster)
             olookup (get map__19912 :olookup)
             temp__5804__auto__ (log/seek-tx (log/find-log cluster olookup) 0)]
         (when temp__5804__auto__
           (let [tree_iter temp__5804__auto__]
             (+
               (inc (count (log/log-dir-seq tree_iter)))
               (apply + (map count (log/log-dir-seq tree_iter))))))))))
  (reset-meta!
    #'count-log-segs
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]}]), :column (int 1)}
      :name
      'count-log-segs
      :ns
      *ns*))
  (defn storage-diagnostics
    ([uri]
      (let [map__19915 (connection-resources uri)
            map__19915 (if (seq? map__19915)
                         (if (next map__19915)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__19915))
                           (if (seq map__19915) (first map__19915) {}))
                         map__19915)
            cr map__19915
            cluster (get map__19915 :cluster)
            olookup (get map__19915 :olookup)
            vec__19916 (log/read-tail-descriptor cluster)
            desc (nth vec__19916 (int 0) nil)
            buf (nth vec__19916 (int 1) nil)
            index_root_key (deref (cluster/get-ref cluster (index/index-ref-key-name cluster)))
            index_root (get olookup (:key index_root_key))
            log_root_key (log/root-id cluster)
            log_root (get olookup (cluster/val-key->uuid log_root_key))
            log_root_disk_bytes (.remaining (:buf (deref (cluster/get-val cluster log_root_key))))]
        {:log-tail desc,
         :log-tail-size (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer buf))),
         :index-root index_root,
         :index-root-key index_root_key,
         :log-root-key log_root_key,
         :log-root-disk-bytes (java.lang.Integer/valueOf (int log_root_disk_bytes)),
         :log-root-mem-bytes (size/memory-size log_root),
         :log-segments (count-log-segs cr)})))
  (reset-meta!
    #'storage-diagnostics
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'storage-diagnostics
      :ns
      *ns*))
  (defn db-diagnostics
    ([uri]
      (let [conn (try (d/connect uri) (catch java.lang.Throwable t nil))
            db (when conn (try (d/db conn) (catch java.lang.Throwable t nil)))
            sizes (when db (stats/sizes db :with-key-summary true))]
        (when sizes {:index-sizes sizes, :index-metrics (stats/sizes->metrics sizes)}))))
  (reset-meta!
    #'db-diagnostics
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'db-diagnostics :ns *ns*))
  (defn system-cluster
    ([uri]
      (let [cluster_conf (uri/parse uri) protocol (:protocol cluster_conf)]
        (when-not (= protocol :mem) (coord/create-system-cluster cluster_conf)))))
  (reset-meta!
    #'system-cluster
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'system-cluster :ns *ns*))
  (def cause-chain
   (fn cause_chain
     ([t]
       (take-while
         identity
         (iterate (fn fn__19927 ([p1__19926#] (.getCause ^java.lang.Throwable p1__19926#))) t)))))
  (reset-meta!
    #'cause-chain
    (assoc
      {:arglists (clojure.core/list [(.withMeta 't {:tag 'Throwable})]), :column (int 1)}
      :name
      'cause-chain
      :ns
      *ns*))
  (defn class-sym ([obj] (symbol (.getName (.getClass obj)))))
  (reset-meta!
    #'class-sym
    (assoc {:arglists (clojure.core/list ['obj]), :column (int 1)} :name 'class-sym :ns *ns*))
  (def tools-fault
   (fn tools_fault
     ([t]
       (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.tools") ex t]
         (when (.isInfoEnabled ^org.slf4j.Logger logger)
           (.info
             ^org.slf4j.Logger logger
             (logger/process "Exception running tools")
             ^java.lang.Throwable ex)
           (logger/caused-by logger ex))
         nil)
       (merge
         {:cognitect.anomalies/category :cognitect.anomalies/fault,
          :datomic.tools/cause (map class-sym (cause-chain t))}
         (let [temp__5804__auto__ (.getMessage ^java.lang.Throwable t)]
           (when temp__5804__auto__
             (let [msg temp__5804__auto__] #:cognitect.anomalies{:message msg})))))))
  (reset-meta!
    #'tools-fault
    (assoc
      {:arglists (clojure.core/list [(.withMeta 't {:tag 'Throwable})]), :column (int 1)}
      :name
      'tools-fault
      :ns
      *ns*))
  (defn catalog ([uri] (some-> uri (system-cluster) (catalog/get-catalog))))
  (reset-meta!
    #'catalog
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'catalog :ns *ns*))
  (defn diagnostics
    ([uri]
      (merge
        {:db (try (db-diagnostics uri) (catch java.lang.Throwable t (tools-fault t)))}
        {:catalog (try (catalog uri) (catch java.lang.Throwable t (tools-fault t)))}
        {:db-storage (try (storage-diagnostics uri) (catch java.lang.Throwable t (tools-fault t)))}
        {:peer (try (peer-diagnostics) (catch java.lang.Throwable t (tools-fault t)))})))
  (reset-meta!
    #'diagnostics
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'diagnostics :ns *ns*))
  (defn unique-values
    ([db]
      (mapv
        :id
        (filter
          (fn fn__19946 ([p1__19945#] (= :db.unique/value (:unique p1__19945#))))
          (map
            (fn fn__19948 ([p1__19944#] (d/attribute db (:v p1__19944#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (reset-meta!
    #'unique-values
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'unique-values :ns *ns*))
  (defn card-ones
    ([db]
      (mapv
        :id
        (filter
          (fn fn__19953 ([p1__19952#] (= :db.cardinality/one (:cardinality p1__19952#))))
          (map
            (fn fn__19955 ([p1__19951#] (d/attribute db (:v p1__19951#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (reset-meta!
    #'card-ones
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'card-ones :ns *ns*))
  (defn fulltexts
    ([db]
      (mapv
        :id
        (filter
          :fulltext
          (map
            (fn fn__19959 ([p1__19958#] (d/attribute db (:v p1__19958#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (reset-meta!
    #'fulltexts
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'fulltexts :ns *ns*))
  (def pretty-datom
   (fn pretty_datom
     ([db datom]
       (if datom
         {:e (.e ^datomic.Datom datom),
          :a (db/resolve-kw db (.a ^datomic.Datom datom)),
          :v (.v ^datomic.Datom datom),
          :t (long (d/tx->t (.tx ^datomic.Datom datom))),
          :added (.added ^datomic.Datom datom)}
         {}))))
  (reset-meta!
    #'pretty-datom
    (assoc
      {:arglists (clojure.core/list ['db (.withMeta 'datom {:tag 'Datom})]), :column (int 1)}
      :name
      'pretty-datom
      :ns
      *ns*))
  (defn touch-heartbeat
    ([uri]
      (let [cluster (coord/create-system-cluster (uri/parse uri))]
        (loop [n 0]
          (do
            (when (< 10 n)
              (throw
                (java.lang.RuntimeException.
                  "Retry limit exceeded claiming transator heartbeat.")))
            (when-not (cluster/touch-ref cluster coord/pod-key) (recur (inc n))))))))
  (reset-meta!
    #'touch-heartbeat
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'touch-heartbeat
      :ns
      *ns*))
  (defn segment-log
    ([uri]
      (let [cr (connection-resources uri)]
        (loop [n 0]
          (do
            (when (< 10 n)
              (throw (java.lang.RuntimeException. "Retry limit exceeded segmenting log.")))
            (when-not (try
                        (do (log/segment (:cluster cr) (:olookup cr)) true)
                        (catch
                          java.lang.Error
                          e
                          (do
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.tools") ex e]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process "Segment log failed ")
                                  ^java.lang.Throwable ex)
                                (logger/caused-by logger ex))
                              nil)
                            false)))
              (recur (inc n))))))))
  (reset-meta!
    #'segment-log
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'segment-log :ns *ns*))
  (defn retract ([d] [:db/retract (:e d) (:a d) (:v d)]))
  (reset-meta!
    #'retract
    (assoc {:arglists (clojure.core/list ['d]), :column (int 1)} :name 'retract :ns *ns*))
  (defn retract-entity ([d] [:db.fn/retractEntity (:e d)]))
  (reset-meta!
    #'retract-entity
    (assoc {:arglists (clojure.core/list ['d]), :column (int 1)} :name 'retract-entity :ns *ns*))
  (def rehome
   (fn rehome
     ([p__19969 to]
       (let [map__19970 p__19969
             map__19970 (if (seq? map__19970)
                          (if (next map__19970)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19970))
                            (if (seq map__19970) (first map__19970) {}))
                          map__19970)
             e (get map__19970 :e)
             a (get map__19970 :a)
             v (get map__19970 :v)]
         [[:db/retract e a v] [:db/add to a v]]))))
  (reset-meta!
    #'rehome
    (assoc
      {:arglists (clojure.core/list [{:keys ['e 'a 'v]} 'to]), :column (int 1)}
      :name
      'rehome
      :ns
      *ns*))
  (def retarget
   (fn retarget
     ([p__19972 to]
       (let [map__19973 p__19972
             map__19973 (if (seq? map__19973)
                          (if (next map__19973)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19973))
                            (if (seq map__19973) (first map__19973) {}))
                          map__19973)
             e (get map__19973 :e)
             a (get map__19973 :a)
             v (get map__19973 :v)]
         [[:db/retract e a v] [:db/add e a to]]))))
  (reset-meta!
    #'retarget
    (assoc
      {:arglists (clojure.core/list [{:keys ['e 'a 'v]} 'to]), :column (int 1)}
      :name
      'retarget
      :ns
      *ns*))
  (defn avof-map
    ([datoms]
      (reduce (fn fn__19975 ([m d] (update-in m [(db/->AVof d)] (fnil conj []) d))) {} datoms)))
  (reset-meta!
    #'avof-map
    (assoc {:arglists (clojure.core/list ['datoms]), :column (int 1)} :name 'avof-map :ns *ns*))
  (defn uniques-in-ts
    ([log ts ids]
      (avof-map
        (filter
          (fn fn__19980
            ([p__19979]
              (let [map__19981 p__19979
                    map__19981 (if (seq? map__19981)
                                 (if (next map__19981)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__19981))
                                   (if (seq map__19981) (first map__19981) {}))
                                 map__19981)
                    a (get map__19981 :a)]
                (contains? ids a))))
          (mapcat
            (fn fn__19983
              ([p1__19978#]
                (let [tx (iter/iget (log/seek-tx log p1__19978#))]
                  (when (= p1__19978# (:t tx)) (:data tx)))))
            ts)))))
  (reset-meta!
    #'uniques-in-ts
    (assoc
      {:private true, :arglists (clojure.core/list ['log 'ts 'ids]), :column (int 1)}
      :name
      'uniques-in-ts
      :ns
      *ns*))
  (defn identities-in-ts
    ([db log ts] (let [uids (into #{} (unique-identities db))] (uniques-in-ts log ts uids))))
  (reset-meta!
    #'identities-in-ts
    (assoc
      {:arglists (clojure.core/list ['db 'log 'ts]), :column (int 1)}
      :name
      'identities-in-ts
      :ns
      *ns*))
  (defn values-in-ts
    ([db log ts] (let [vids (into #{} (unique-values db))] (uniques-in-ts log ts vids))))
  (reset-meta!
    #'values-in-ts
    (assoc
      {:arglists (clojure.core/list ['db 'log 'ts]), :column (int 1)}
      :name
      'values-in-ts
      :ns
      *ns*))
  (defn unsorted-seq
    ([pred s]
      (seq
        (map
          vec
          (remove
            (fn fn__19989
              ([p__19988]
                (let [vec__19990 p__19988
                      a (nth vec__19990 (int 0) nil)
                      b (nth vec__19990 (int 1) nil)]
                  (^clojure.lang.IFn pred a b))))
            (partition 2 1 s))))))
  (reset-meta!
    #'unsorted-seq
    (assoc
      {:arglists (clojure.core/list ['pred 's]), :column (int 1)}
      :name
      'unsorted-seq
      :ns
      *ns*))
  (defn ever-nohistory-attrs
    ([db] (into #{} (map first (d/q [:find '?e :where ['?e :db/noHistory true]] (d/history db))))))
  (reset-meta!
    #'ever-nohistory-attrs
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'ever-nohistory-attrs
      :ns
      *ns*))
  (defn nohistory-checker
    ([db]
      (let [attrs (ever-nohistory-attrs db)]
        (fn fn__19996 ([d] (contains? attrs (.a ^datomic.Datom d)))))))
  (reset-meta!
    #'nohistory-checker
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'nohistory-checker
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.tools" "uuid->val-key") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "uuid->val-key") cluster/uuid->val-key)
  (.setMeta (clojure.lang.RT/var "datomic.tools" "val-key->uuid") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.tools" "val-key->uuid") cluster/val-key->uuid))
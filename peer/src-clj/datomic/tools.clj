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
  (reset-meta! #'TOOLS_VERSION (assoc {:const true, :column 1} :name 'TOOLS_VERSION :ns *ns*))
  (defn datom-read-handler
    ([p__21725]
      (let [vec__21726 p__21725
            e (nth vec__21726 (int 0) nil)
            a (nth vec__21726 (int 1) nil)
            v (nth vec__21726 (int 2) nil)
            tx (nth vec__21726 (int 3) nil)
            op (nth vec__21726 (int 4) nil)]
        (if op
          (db/asserting-datum (long ^java.lang.Number e) (long ^java.lang.Number a) v (d/tx->t tx))
          (db/retracting-datum
            (long ^java.lang.Number e)
            (long ^java.lang.Number a)
            v
            (d/tx->t tx))))))
  (def serializer (agent nil))
  (defn serialized
    ([f agt]
      (fn fn__21730
        ([& args]
          (send-off
            agt
            (fn fn__21731
              ([_]
                (try
                  (apply f args)
                  (catch java.lang.Throwable t (do (.printStackTrace ^java.lang.Throwable t) nil)))
                nil)))
          nil)))
    ([f] (serialized f serializer)))
  (defn prn-err ([x] (binding [*print-length* nil *out* *err*] (prn x))))
  (defn println-err ([x] (binding [*out* *err*] (println x))))
  (def progress (serialized (fn fn__21739 ([report data] (^clojure.lang.IFn report data)))))
  (defn flush-progress ([] (await serializer)))
  (def connection-resources db-io/storage-resources)
  (def index-db db-io/index-db)
  (def log db-io/log)
  (def db-resources db-io/db-resources)
  (defn log-entry->next-t ([log_entry] (inc (log/max-eidx (:data log_entry)))))
  (defn index-has-t?
    ([db t]
      (let [idb (assoc db :memidx db/mem-index-set)]
        (boolean (seq (d/datoms idb :eavt (d/t->tx (long ^java.lang.Number t))))))))
  (defn set-index-basis
    ([uri basisT]
      (let [map__21744 (connection-resources uri)
            map__21744 (if (seq? map__21744)
                         (clojure.lang.PersistentHashMap/create (seq map__21744))
                         map__21744)
            cr map__21744
            cluster (get map__21744 :cluster)
            olookup (get map__21744 :olookup)
            map__21745 (db-resources cr)
            map__21745 (if (seq? map__21745)
                         (clojure.lang.PersistentHashMap/create (seq map__21745))
                         map__21745)
            db (get map__21745 :db)
            log (get map__21745 :log)
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
  (defn get-val ([conn k] (get (peer/get-olookup conn) k)))
  (defn get-index-ref
    ([cluster] (deref (cluster/get-ref cluster (index/index-ref-key-name cluster)))))
  (defn reset-index-ref
    ([cluster root_id]
      (when-not (string? root_id)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'string? 'root-id))))))
      (deref (cluster/reset-ref cluster (index/index-ref-key-name cluster) root_id))))
  (reset-meta!
    #'reset-index-ref
    (assoc
      {:private true, :arglists (clojure.core/list ['cluster 'root-id]), :column 1}
      :name
      'reset-index-ref
      :ns
      *ns*))
  (defn replace-index
    ([cluster index_id]
      (when-not (string? index_id)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'string? 'index-id))))))
      (let [m_21752 {:event :tools/replace-index, :id index_id}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.tools")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_21752 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
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
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_21753 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_21754 (logger/format-as-msec (long elapsed_21753))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_21752 :msec msec_21754 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.tools")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn touch-index-ref ([cluster] (cluster/touch-ref cluster (index/index-ref-key-name cluster))))
  (defn log-size*
    ([conn t]
      (let [log (d/log conn) db (d/db conn)]
        (reduce
          (fn fn__21765 ([size logentry] (+ size (size/memory-size logentry))))
          0
          (d/tx-range log t nil)))))
  (defn log-size ([uri t] (let [conn (d/connect uri)] (log-size* conn t))))
  (defn get-index
    ([p__21769]
      (let [map__21770 p__21769
            map__21770 (if (seq? map__21770)
                         (clojure.lang.PersistentHashMap/create (seq map__21770))
                         map__21770)
            cluster (get map__21770 :cluster)
            olookup (get map__21770 :olookup)]
        (get
          olookup
          (:key (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))))))
  (defn cr? ([o] (and (:cluster o) (:olookup o))))
  (defn tx-range-from-log
    ([cr start end]
      (when-not (cr? cr)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'cr? 'cr))))))
      (let [s (iter/iter-seq (log/seek-tx (log/find-log (:cluster cr) (:olookup cr)) start))]
        (if end (take-while (fn fn__21774 ([logentry] (< (:t logentry) end))) s) s))))
  (defn unique-identities
    ([db]
      (mapv
        :id
        (filter
          (fn fn__21779 ([p1__21778#] (= :db.unique/identity (:unique p1__21778#))))
          (map
            (fn fn__21781 ([p1__21777#] (d/attribute db (:v p1__21777#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (defn read-capacity-units
    ([uri]
      (let [map__21784 (uri/parse uri)
            map__21784 (if (seq? map__21784)
                         (clojure.lang.PersistentHashMap/create (seq map__21784))
                         map__21784)
            parsed_uri map__21784
            protocol (get map__21784 :protocol)]
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
  (defn pace-msec-per-seg
    ([provisioned_kbs]
      (let [use_kbs (/ provisioned_kbs 2)
            item_per_kb 0.03
            msec_per_sec 1000.0
            latency_msec 25
            pace_msec (- (/ (/ msec_per_sec use_kbs) item_per_kb) latency_msec)]
        (when (> pace_msec 0.0) (long pace_msec)))))
  (defn pace-fn
    ([pace_msec]
      (let [last_val_gets (atom (deref kvc/val-gets-ref))]
        (fn fn__21787
          ([]
            (let [new_val_gets (deref kvc/val-gets-ref)
                  msec (* (- new_val_gets (deref last_val_gets)) pace_msec)]
              (when (< 10 msec)
                (monitor/add-stat :ToolsPaceReadMsec msec)
                (java.lang.Thread/sleep (long ^java.lang.Number msec))
                (reset! last_val_gets new_val_gets))))))))
  (defn peer-diagnostics
    ([]
      {:peer-config (deref datomic.config/properties-ref),
       :object-cache-count (cache/fast-count (domain/system-cache)),
       :memory-mb (math/rounded-mb (long (.maxMemory (java.lang.Runtime/getRuntime))))}))
  (defn count-log-segs
    ([p__21791]
      (let [map__21792 p__21791
            map__21792 (if (seq? map__21792)
                         (clojure.lang.PersistentHashMap/create (seq map__21792))
                         map__21792)
            cluster (get map__21792 :cluster)
            olookup (get map__21792 :olookup)
            temp__5457__auto__ (log/seek-tx (log/find-log cluster olookup) 0)]
        (when temp__5457__auto__
          (let [tree_iter temp__5457__auto__]
            (+
              (inc (count (log/log-dir-seq tree_iter)))
              (apply + (map count (log/log-dir-seq tree_iter)))))))))
  (defn storage-diagnostics
    ([uri]
      (let [map__21795 (connection-resources uri)
            map__21795 (if (seq? map__21795)
                         (clojure.lang.PersistentHashMap/create (seq map__21795))
                         map__21795)
            cr map__21795
            cluster (get map__21795 :cluster)
            olookup (get map__21795 :olookup)
            vec__21796 (log/read-tail-descriptor cluster)
            desc (nth vec__21796 (int 0) nil)
            buf (nth vec__21796 (int 1) nil)
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
  (defn db-diagnostics
    ([uri]
      (let [conn (try (d/connect uri) (catch java.lang.Throwable t nil))
            db (when conn (try (d/db conn) (catch java.lang.Throwable t nil)))
            sizes (when db (stats/sizes db :with-key-summary true))]
        (when sizes {:index-sizes sizes, :index-metrics (stats/sizes->metrics sizes)}))))
  (defn system-cluster
    ([uri]
      (let [cluster_conf (uri/parse uri) protocol (:protocol cluster_conf)]
        (when-not (= protocol :mem) (coord/create-system-cluster cluster_conf)))))
  (defn cause-chain
    ([t]
      (take-while
        identity
        (iterate (fn fn__21807 ([p1__21806#] (.getCause ^java.lang.Throwable p1__21806#))) t))))
  (defn class-sym ([obj] (symbol (.getName (.getClass obj)))))
  (defn tools-fault
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
        (let [temp__5457__auto__ (.getMessage ^java.lang.Throwable t)]
          (when temp__5457__auto__
            (let [msg temp__5457__auto__] #:cognitect.anomalies{:message msg}))))))
  (defn catalog ([uri] (some-> uri (system-cluster) (catalog/get-catalog))))
  (defn diagnostics
    ([uri]
      (merge
        {:db (try (db-diagnostics uri) (catch java.lang.Throwable t (tools-fault t)))}
        {:catalog (try (catalog uri) (catch java.lang.Throwable t (tools-fault t)))}
        {:db-storage (try (storage-diagnostics uri) (catch java.lang.Throwable t (tools-fault t)))}
        {:peer (try (peer-diagnostics) (catch java.lang.Throwable t (tools-fault t)))})))
  (defn unique-values
    ([db]
      (mapv
        :id
        (filter
          (fn fn__21826 ([p1__21825#] (= :db.unique/value (:unique p1__21825#))))
          (map
            (fn fn__21828 ([p1__21824#] (d/attribute db (:v p1__21824#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (defn card-ones
    ([db]
      (mapv
        :id
        (filter
          (fn fn__21833 ([p1__21832#] (= :db.cardinality/one (:cardinality p1__21832#))))
          (map
            (fn fn__21835 ([p1__21831#] (d/attribute db (:v p1__21831#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (defn fulltexts
    ([db]
      (mapv
        :id
        (filter
          :fulltext
          (map
            (fn fn__21839 ([p1__21838#] (d/attribute db (:v p1__21838#))))
            (d/datoms db :aevt :db.install/attribute))))))
  (defn pretty-datom
    ([db datom]
      (if datom
        {:e (.e ^datomic.Datom datom),
         :a (db/resolve-kw db (.a ^datomic.Datom datom)),
         :v (.v ^datomic.Datom datom),
         :t (long (d/tx->t (.tx ^datomic.Datom datom))),
         :added (.added ^datomic.Datom datom)}
        {})))
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
  (defn retract ([d] [:db/retract (:e d) (:a d) (:v d)]))
  (defn retract-entity ([d] [:db.fn/retractEntity (:e d)]))
  (defn rehome
    ([p__21849 to]
      (let [map__21850 p__21849
            map__21850 (if (seq? map__21850)
                         (clojure.lang.PersistentHashMap/create (seq map__21850))
                         map__21850)
            e (get map__21850 :e)
            a (get map__21850 :a)
            v (get map__21850 :v)]
        [[:db/retract e a v] [:db/add to a v]])))
  (defn retarget
    ([p__21852 to]
      (let [map__21853 p__21852
            map__21853 (if (seq? map__21853)
                         (clojure.lang.PersistentHashMap/create (seq map__21853))
                         map__21853)
            e (get map__21853 :e)
            a (get map__21853 :a)
            v (get map__21853 :v)]
        [[:db/retract e a v] [:db/add e a to]])))
  (defn avof-map
    ([datoms]
      (reduce (fn fn__21855 ([m d] (update-in m [(db/->AVof d)] (fnil conj []) d))) {} datoms)))
  (defn uniques-in-ts
    ([log ts ids]
      (avof-map
        (filter
          (fn fn__21860
            ([p__21859]
              (let [map__21861 p__21859
                    map__21861 (if (seq? map__21861)
                                 (clojure.lang.PersistentHashMap/create (seq map__21861))
                                 map__21861)
                    a (get map__21861 :a)]
                (contains? ids a))))
          (mapcat
            (fn fn__21863
              ([p1__21858#]
                (let [tx (iter/iget (log/seek-tx log p1__21858#))]
                  (when (= p1__21858# (:t tx)) (:data tx)))))
            ts)))))
  (reset-meta!
    #'uniques-in-ts
    (assoc
      {:private true, :arglists (clojure.core/list ['log 'ts 'ids]), :column 1}
      :name
      'uniques-in-ts
      :ns
      *ns*))
  (defn identities-in-ts
    ([db log ts] (let [uids (into #{} (unique-identities db))] (uniques-in-ts log ts uids))))
  (defn values-in-ts
    ([db log ts] (let [vids (into #{} (unique-values db))] (uniques-in-ts log ts vids))))
  (defn unsorted-seq
    ([pred s]
      (seq
        (map
          vec
          (remove
            (fn fn__21869
              ([p__21868]
                (let [vec__21870 p__21868
                      a (nth vec__21870 (int 0) nil)
                      b (nth vec__21870 (int 1) nil)]
                  (^clojure.lang.IFn pred a b))))
            (partition 2 1 s))))))
  (defn ever-nohistory-attrs
    ([db] (into #{} (map first (d/q [:find '?e :where ['?e :db/noHistory true]] (d/history db))))))
  (defn nohistory-checker
    ([db]
      (let [attrs (ever-nohistory-attrs db)]
        (fn fn__21876 ([d] (contains? attrs (.a ^datomic.Datom d)))))))
  (def uuid->val-key cluster/uuid->val-key)
  (def val-key->uuid cluster/val-key->uuid))
(do
  (clojure.core/in-ns 'datomic.cassandra)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['datomic.callback :as 'cb]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'com.datastax.driver.core.ResultSet)
      (clojure.core/import 'com.datastax.driver.core.Session)
      (clojure.core/import 'com.datastax.driver.core.PreparedStatement)
      (clojure.core/import 'com.datastax.driver.core.ConsistencyLevel)
      (clojure.core/import 'com.datastax.driver.core.BoundStatement)
      (clojure.core/import 'com.datastax.driver.core.Row)))
  (when-not (.equals 'datomic.cassandra 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cassandra))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'str]
          ['datomic.callback :as 'cb]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'com.datastax.driver.core.ResultSet)
        (clojure.core/import 'com.datastax.driver.core.Session)
        (clojure.core/import 'com.datastax.driver.core.PreparedStatement)
        (clojure.core/import 'com.datastax.driver.core.ConsistencyLevel)
        (clojure.core/import 'com.datastax.driver.core.BoundStatement)
        (clojure.core/import 'com.datastax.driver.core.Row))))
  (set! *warn-on-reflection* true)
  (defn retry-policy ([] com.datastax.driver.core.policies.DefaultRetryPolicy/INSTANCE))
  (reset-meta!
    #'retry-policy
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'retry-policy :ns *ns*))
  (def update-stmt*
   (fn update_stmt_STAR_
     ([session table id_key col_names]
       (let [update_cql (str
                          "update "
                          table
                          " set "
                          (str/join
                            ", "
                            (map (fn fn__26992 ([p1__26991#] (str p1__26991# " = ?"))) col_names))
                          " where "
                          (name id_key)
                          " = ? if rev = ?")
             stmt (.prepare
                    ^com.datastax.driver.core.Session session
                    ^java.lang.String update_cql)]
         (.setConsistencyLevel
           ^com.datastax.driver.core.PreparedStatement stmt
           ConsistencyLevel/QUORUM)
         (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))
         stmt))))
  (reset-meta!
    #'update-stmt*
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id-key 'col-names]),
       :column (int 1)}
      :name
      'update-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra" "update-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra" "update-stmt") (memoize update-stmt*))
  (def updated?
   (fn updated_QMARK_
     ([res]
       (let [temp__5825__auto__ (.one ^com.datastax.driver.core.ResultSet res)]
         (when temp__5825__auto__
           (let [row temp__5825__auto__]
             (.getBool ^com.datastax.driver.core.GettableByIndexData row (int 0))))))))
  (reset-meta!
    #'updated?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'res {:tag 'ResultSet})]), :column (int 1)}
      :name
      'updated?
      :ns
      *ns*))
  (def cql-update
   (fn cql_update
     ([session table ensure_rev p__26997 v_map]
       (let [vec__26998 p__26997
             seq__26999 (seq vec__26998)
             first__27000 (first seq__26999)
             seq__26999 (next seq__26999)
             id_key first__27000
             ks seq__26999
             id (get v_map id_key)
             col_vals (filter second (select-keys v_map ks))
             col_names (map (comp name first) col_vals)
             stmt (update-stmt session table id_key col_names)
             bound (.bind
                     ^com.datastax.driver.core.PreparedStatement stmt
                     (into-array java.lang.Object (concat (map second col_vals) [id ensure_rev])))
             res (.execute
                   ^com.datastax.driver.core.Session session
                   (.setSerialConsistencyLevel
                     ^com.datastax.driver.core.Statement bound
                     ConsistencyLevel/SERIAL))]
         (updated? res)))))
  (reset-meta!
    #'cql-update
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'Session}) 'table 'ensure-rev ['id-key '& 'ks] 'v-map]),
       :column (int 1)}
      :name
      'cql-update
      :ns
      *ns*))
  (def insert-stmt*
   (fn insert_stmt_STAR_
     ([session table col_names consistent?]
       (let [insert_cql (str
                          "insert into "
                          table
                          " ("
                          (str/join ", " col_names)
                          ") values ("
                          (str/join
                            ", "
                            (take
                              (java.lang.Integer/valueOf (int (count col_names)))
                              (repeat "?")))
                          ")"
                          (when consistent? " if not exists"))
             stmt (.prepare
                    ^com.datastax.driver.core.Session session
                    ^java.lang.String insert_cql)]
         (.setConsistencyLevel
           ^com.datastax.driver.core.PreparedStatement stmt
           ConsistencyLevel/QUORUM)
         (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))
         stmt))))
  (reset-meta!
    #'insert-stmt*
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'col-names 'consistent?]),
       :column (int 1)}
      :name
      'insert-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra" "insert-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra" "insert-stmt") (memoize insert-stmt*))
  (def cql-insert
   (fn cql_insert
     ([session table ks v_map consistent?]
       (let [col_vals (filter second (select-keys v_map ks))
             col_names (map (comp name first) col_vals)
             stmt (insert-stmt session table col_names consistent?)
             bound (cond->
                     (.bind
                       ^com.datastax.driver.core.PreparedStatement stmt
                       (into-array java.lang.Object (map second col_vals)))
                     consistent?
                     (.setSerialConsistencyLevel ConsistencyLevel/SERIAL))
             res (.execute
                   ^com.datastax.driver.core.Session session
                   ^com.datastax.driver.core.Statement bound)]
         (if consistent?
           (updated? res)
           (.isExhausted ^com.datastax.driver.core.PagingIterable res))))))
  (reset-meta!
    #'cql-insert
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'ks 'v-map 'consistent?]),
       :column (int 1)}
      :name
      'cql-insert
      :ns
      *ns*))
  (def select-string
   (fn select_string
     ([table p__27005]
       (let [vec__27006 p__27005 id_key (nth vec__27006 (int 0) nil) ks vec__27006]
         (str
           "select "
           (str/join ", " (map name ks))
           " from "
           table
           " where "
           (name id_key)
           " = ?")))))
  (reset-meta!
    #'select-string
    (assoc
      {:arglists
       (clojure.core/list (.withMeta ['table ['id-key :as 'ks]] {:tag 'java.lang.String})),
       :column (int 1)}
      :name
      'select-string
      :ns
      *ns*))
  (def select-stmt*
   (fn select_stmt_STAR_
     ([session table ks]
       (let [stmt (.prepare ^com.datastax.driver.core.Session session (select-string table ks))]
         (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))))))
  (reset-meta!
    #'select-stmt*
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'ks]),
       :column (int 1)}
      :name
      'select-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra" "select-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra" "select-stmt") (memoize select-stmt*))
  (def select-with-consistency
   (fn select_with_consistency
     ([session stmt consistency serial id]
       (.setConsistencyLevel
         ^com.datastax.driver.core.PreparedStatement stmt
         ^com.datastax.driver.core.ConsistencyLevel consistency)
       (let [bound (.bind
                     ^com.datastax.driver.core.PreparedStatement stmt
                     (into-array java.lang.Object [id]))]
         (when serial
           (.setSerialConsistencyLevel
             ^com.datastax.driver.core.Statement bound
             ConsistencyLevel/SERIAL))
         (.one
           (.execute
             ^com.datastax.driver.core.Session session
             ^com.datastax.driver.core.Statement bound))))))
  (reset-meta!
    #'select-with-consistency
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'Session})
          (.withMeta 'stmt {:tag 'PreparedStatement})
          'consistency
          'serial
          'id]),
       :column (int 1)}
      :name
      'select-with-consistency
      :ns
      *ns*))
  (def row->map
   (fn row__GT_map
     ([row ks]
       (if (= ks [:id :rev :map :val])
         {:id (.getString ^com.datastax.driver.core.GettableByIndexData row (int 0)),
          :rev (long (.getLong ^com.datastax.driver.core.GettableByIndexData row (int 1))),
          :map (.getString ^com.datastax.driver.core.GettableByIndexData row (int 2)),
          :val (.getBytes ^com.datastax.driver.core.GettableByIndexData row (int 3))}
         (if (= ks [:id2 :rev :map :val :chunks])
           {:id (.getString ^com.datastax.driver.core.GettableByIndexData row (int 0)),
            :rev (long (.getLong ^com.datastax.driver.core.GettableByIndexData row (int 1))),
            :map (.getString ^com.datastax.driver.core.GettableByIndexData row (int 2)),
            :val (.getBytes ^com.datastax.driver.core.GettableByIndexData row (int 3)),
            :chunks
            (java.lang.Integer/valueOf
              (int (.getInt ^com.datastax.driver.core.GettableByIndexData row (int 4))))}
           (do (when :else (throw (java.lang.RuntimeException. "Invalid select."))) nil))))))
  (reset-meta!
    #'row->map
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'row {:tag 'Row}) 'ks]), :column (int 1)}
      :name
      'row->map
      :ns
      *ns*))
  (def cql-select
   (fn cql_select
     ([session table id ks consistent?]
       (let [stmt (select-stmt session table ks)
             temp__5825__auto__ (or
                                  (and
                                    (not consistent?)
                                    (select-with-consistency
                                      session
                                      stmt
                                      ConsistencyLevel/ONE
                                      false
                                      id))
                                  (select-with-consistency
                                    session
                                    stmt
                                    ConsistencyLevel/QUORUM
                                    true
                                    id))]
         (when temp__5825__auto__ (let [row temp__5825__auto__] (row->map row ks)))))))
  (reset-meta!
    #'cql-select
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id 'ks 'consistent?]),
       :column (int 1)}
      :name
      'cql-select
      :ns
      *ns*))
  (def delete-stmt*
   (fn delete_stmt_STAR_
     ([session table id_key]
       (let [stmt (.prepare
                    ^com.datastax.driver.core.Session session
                    (str "delete from " table " where " (name id_key) " = ?"))]
         (.setConsistencyLevel
           ^com.datastax.driver.core.PreparedStatement stmt
           ConsistencyLevel/QUORUM)
         (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))
         stmt))))
  (reset-meta!
    #'delete-stmt*
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id-key]),
       :column (int 1)}
      :name
      'delete-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra" "delete-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra" "delete-stmt") (memoize delete-stmt*))
  (def cql-delete
   (fn cql_delete
     ([session table id p__27018]
       (let [vec__27019 p__27018
             seq__27020 (seq vec__27019)
             first__27021 (first seq__27020)
             seq__27020 (next seq__27020)
             id_key first__27021
             ks seq__27020
             stmt (delete-stmt session table id_key)
             res (.execute
                   ^com.datastax.driver.core.Session session
                   (.bind
                     ^com.datastax.driver.core.PreparedStatement stmt
                     (into-array java.lang.Object [id])))]
         res))))
  (reset-meta!
    #'cql-delete
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'session {:tag 'Session}) 'table 'id ['id-key '& 'ks]]),
       :column (int 1)}
      :name
      'cql-delete
      :ns
      *ns*))
  (def cluster-from-callback
   (fn cluster_from_callback
     ([p__27023]
       (let [map__27024 p__27023
             map__27024 (if (seq? map__27024)
                          (if (next map__27024)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__27024))
                            (if (seq map__27024) (first map__27024) {}))
                          map__27024)
             endpoint map__27024
             cluster_callback (get map__27024 :cluster-callback)]
         (when cluster_callback
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra")]
             (when (.isInfoEnabled ^org.slf4j.Logger logger)
               (.info
                 ^org.slf4j.Logger logger
                 (logger/process (str "Using cassandra-cluster-callback " cluster_callback))))
             nil)
           (let [temp__5823__auto__ (cb/create-callback (symbol cluster_callback))]
             (if temp__5823__auto__
               (let [callback temp__5823__auto__
                     temp__5823__auto__ (^clojure.lang.IFn callback endpoint)]
                 (if temp__5823__auto__
                   (let [cluster temp__5823__auto__] cluster)
                   (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra")]
                     (when (.isWarnEnabled ^org.slf4j.Logger logger)
                       (.warn
                         ^org.slf4j.Logger logger
                         (logger/process
                           (str "The cassandra-cluster-callback " cluster_callback " nil"))))
                     nil)))
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra")]
                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                   (.warn
                     ^org.slf4j.Logger logger
                     (logger/process
                       (str "Could not resolve cassandra-cluster-callback " cluster_callback))))
                 nil))))))))
  (reset-meta!
    #'cluster-from-callback
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster-callback], :as 'endpoint}]), :column (int 1)}
      :name
      'cluster-from-callback
      :ns
      *ns*)))
(do
  (clojure.core/in-ns 'datomic.cassandra-v4)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['datomic.callback :as 'cb]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.ResultSet)
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.PreparedStatement)
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.BoundStatement)
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.Row)
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SyncCqlSession)
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SimpleStatement)
      (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder)
      (clojure.core/import 'com.datastax.oss.driver.api.core.ConsistencyLevel)
      (clojure.core/import 'com.datastax.oss.driver.api.core.DefaultConsistencyLevel)))
  (when-not (.equals 'datomic.cassandra-v4 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cassandra-v4))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'str]
          ['datomic.callback :as 'cb]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.ResultSet)
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.PreparedStatement)
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.BoundStatement)
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.Row)
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SyncCqlSession)
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SimpleStatement)
        (clojure.core/import 'com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder)
        (clojure.core/import 'com.datastax.oss.driver.api.core.ConsistencyLevel)
        (clojure.core/import 'com.datastax.oss.driver.api.core.DefaultConsistencyLevel))))
  (set! *warn-on-reflection* true)
  (def update-stmt*
   (fn update_stmt_STAR_
     ([session table id_key col_names]
       (let [update_cql (str
                          "update "
                          table
                          " set "
                          (str/join
                            ", "
                            (map (fn fn__30118 ([p1__30117#] (str p1__30117# " = ?"))) col_names))
                          " where "
                          (name id_key)
                          " = ? if rev = ?")
             ss (.build
                  (.setIdempotence
                    (.setConsistencyLevel
                      (com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder.
                        ^java.lang.String update_cql)
                      DefaultConsistencyLevel/LOCAL_QUORUM)
                    false))]
         (.prepare
           ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
           ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss)))))
  (reset-meta!
    #'update-stmt*
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'id-key 'col-names]),
       :column (int 1)}
      :name
      'update-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra-v4" "update-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra-v4" "update-stmt") (memoize update-stmt*))
  (def updated?
   (fn updated_QMARK_
     ([res]
       (let [temp__5804__auto__ (.one ^com.datastax.oss.driver.api.core.PagingIterable res)]
         (when temp__5804__auto__
           (let [row temp__5804__auto__]
             (.getBoolean ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 0))))))))
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
     ([session table ensure_rev p__30123 v_map]
       (let [vec__30124 p__30123
             seq__30125 (seq vec__30124)
             first__30126 (first seq__30125)
             seq__30125 (next seq__30125)
             id_key first__30126
             ks seq__30125
             id (get v_map id_key)
             col_vals (filter second (select-keys v_map ks))
             col_names (map (comp name first) col_vals)
             stmt (update-stmt session table id_key col_names)
             bound (.bind
                     ^com.datastax.oss.driver.api.core.cql.PreparedStatement stmt
                     (into-array java.lang.Object (concat (map second col_vals) [id ensure_rev])))
             res (.execute
                   ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
                   (.setSerialConsistencyLevel
                     ^com.datastax.oss.driver.api.core.cql.Statement bound
                     ConsistencyLevel/SERIAL))]
         (updated? res)))))
  (reset-meta!
    #'cql-update
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'ensure-rev ['id-key '& 'ks] 'v-map]),
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
             ss (.build
                  (.setIdempotence
                    (.setConsistencyLevel
                      (com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder.
                        ^java.lang.String insert_cql)
                      DefaultConsistencyLevel/LOCAL_QUORUM)
                    false))]
         (.prepare
           ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
           ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss)))))
  (reset-meta!
    #'insert-stmt*
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'col-names 'consistent?]),
       :column (int 1)}
      :name
      'insert-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra-v4" "insert-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra-v4" "insert-stmt") (memoize insert-stmt*))
  (def cql-insert
   (fn cql_insert
     ([session table ks v_map consistent?]
       (let [col_vals (filter second (select-keys v_map ks))
             col_names (map (comp name first) col_vals)
             stmt (insert-stmt session table col_names consistent?)
             bound (cond->
                     (.bind
                       ^com.datastax.oss.driver.api.core.cql.PreparedStatement stmt
                       (into-array java.lang.Object (map second col_vals)))
                     consistent?
                     (.setSerialConsistencyLevel ConsistencyLevel/SERIAL))
             res (.execute
                   ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
                   ^com.datastax.oss.driver.api.core.cql.Statement bound)]
         (if consistent?
           (updated? res)
           (nil? (.one ^com.datastax.oss.driver.api.core.PagingIterable res)))))))
  (reset-meta!
    #'cql-insert
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'ks 'v-map 'consistent?]),
       :column (int 1)}
      :name
      'cql-insert
      :ns
      *ns*))
  (def select-string
   (fn select_string
     ([table p__30131]
       (let [vec__30132 p__30131 id_key (nth vec__30132 (int 0) nil) ks vec__30132]
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
       (let [ss (.build
                  (.setIdempotence
                    (com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder.
                      (select-string table ks))
                    false))]
         (.prepare
           ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
           ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss)))))
  (reset-meta!
    #'select-stmt*
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'ks]),
       :column (int 1)}
      :name
      'select-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra-v4" "select-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra-v4" "select-stmt") (memoize select-stmt*))
  (def select-with-consistency
   (fn select_with_consistency
     ([session stmt consistency serial id]
       (let [bound (.bind
                     ^com.datastax.oss.driver.api.core.cql.PreparedStatement stmt
                     (into-array java.lang.Object [id]))
             bound (.setConsistencyLevel
                     ^com.datastax.oss.driver.api.core.cql.Statement bound
                     ^com.datastax.oss.driver.api.core.ConsistencyLevel consistency)
             bound (if serial
                     (.setSerialConsistencyLevel
                       ^com.datastax.oss.driver.api.core.cql.Statement bound
                       ConsistencyLevel/SERIAL)
                     bound)]
         (.one
           (.execute
             ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
             ^com.datastax.oss.driver.api.core.cql.Statement bound))))))
  (reset-meta!
    #'select-with-consistency
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'SyncCqlSession})
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
         {:id (.getString ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 0)),
          :rev
          (long (.getLong ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 1))),
          :map (.getString ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 2)),
          :val (.getByteBuffer ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 3))}
         (if (= ks [:id2 :rev :map :val :chunks])
           {:id (.getString ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 0)),
            :rev
            (long (.getLong ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 1))),
            :map (.getString ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 2)),
            :val
            (.getByteBuffer ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 3)),
            :chunks
            (java.lang.Integer/valueOf
              (int (.getInt ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 4))))}
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
             temp__5804__auto__ (or
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
                                    DefaultConsistencyLevel/LOCAL_QUORUM
                                    true
                                    id))]
         (when temp__5804__auto__ (let [row temp__5804__auto__] (row->map row ks)))))))
  (reset-meta!
    #'cql-select
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'id 'ks 'consistent?]),
       :column (int 1)}
      :name
      'cql-select
      :ns
      *ns*))
  (def delete-stmt*
   (fn delete_stmt_STAR_
     ([session table id_key]
       (let [ss (.build
                  (.setConsistencyLevel
                    (.setIdempotence
                      (com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder.
                        (str "delete from " table " where " (name id_key) " = ?"))
                      false)
                    DefaultConsistencyLevel/LOCAL_QUORUM))]
         (.prepare
           ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
           ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss)))))
  (reset-meta!
    #'delete-stmt*
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'id-key]),
       :column (int 1)}
      :name
      'delete-stmt*
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cassandra-v4" "delete-stmt") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cassandra-v4" "delete-stmt") (memoize delete-stmt*))
  (def cql-delete
   (fn cql_delete
     ([session table id p__30144]
       (let [vec__30145 p__30144
             seq__30146 (seq vec__30145)
             first__30147 (first seq__30146)
             seq__30146 (next seq__30146)
             id_key first__30147
             _ seq__30146
             stmt (delete-stmt session table id_key)
             bs (.bind
                  ^com.datastax.oss.driver.api.core.cql.PreparedStatement stmt
                  (into-array java.lang.Object [id]))
             res (.execute
                   ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
                   ^com.datastax.oss.driver.api.core.cql.Statement bs)]
         res))))
  (reset-meta!
    #'cql-delete
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'session {:tag 'SyncCqlSession}) 'table 'id ['id-key '& '_]]),
       :column (int 1)}
      :name
      'cql-delete
      :ns
      *ns*))
  (def session-from-callback
   (fn session_from_callback
     ([p__30149]
       (let [map__30150 p__30149
             map__30150 (if (seq? map__30150)
                          (if (next map__30150)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__30150))
                            (if (seq map__30150) (first map__30150) {}))
                          map__30150)
             endpoint map__30150
             session_callback (get map__30150 :session-callback)]
         (when session_callback
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-v4")]
             (when (.isInfoEnabled ^org.slf4j.Logger logger)
               (.info
                 ^org.slf4j.Logger logger
                 (logger/process (str "Using cassandra-session-callback " session_callback))))
             nil)
           (let [temp__5802__auto__ (cb/create-callback (symbol session_callback))]
             (if temp__5802__auto__
               (let [callback temp__5802__auto__
                     temp__5802__auto__ (^clojure.lang.IFn callback endpoint)]
                 (if temp__5802__auto__
                   (let [session temp__5802__auto__] session)
                   (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-v4")]
                     (when (.isWarnEnabled ^org.slf4j.Logger logger)
                       (.warn
                         ^org.slf4j.Logger logger
                         (logger/process
                           (str "The cassandra-session-callback " session_callback " nil"))))
                     nil)))
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-v4")]
                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                   (.warn
                     ^org.slf4j.Logger logger
                     (logger/process
                       (str "Could not resolve cassandra-session-callback " session_callback))))
                 nil))))))))
  (reset-meta!
    #'session-from-callback
    (assoc
      {:arglists (clojure.core/list [{:keys ['session-callback], :as 'endpoint}]), :column (int 1)}
      :name
      'session-from-callback
      :ns
      *ns*)))
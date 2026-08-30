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
  (defn update-stmt*
    ([session table id_key col_names]
      (let [update_cql (str
                         "update "
                         table
                         " set "
                         (str/join
                           ", "
                           (map (fn fn__10105 ([p1__10104#] (str p1__10104# " = ?"))) col_names))
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
          ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss))))
  (def update-stmt (memoize update-stmt*))
  (defn updated?
    ([res]
      (let [temp__5457__auto__ (.one ^com.datastax.oss.driver.api.core.PagingIterable res)]
        (when temp__5457__auto__
          (let [row temp__5457__auto__]
            (.getBoolean ^com.datastax.oss.driver.api.core.data.GettableByIndex row (int 0)))))))
  (defn cql-update
    ([session table ensure_rev p__10110 v_map]
      (let [vec__10111 p__10110
            seq__10112 (seq vec__10111)
            first__10113 (first seq__10112)
            seq__10112 (next seq__10112)
            id_key first__10113
            ks seq__10112
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
        (updated? res))))
  (defn insert-stmt*
    ([session table col_names consistent?]
      (let [insert_cql (str
                         "insert into "
                         table
                         " ("
                         (str/join ", " col_names)
                         ") values ("
                         (str/join
                           ", "
                           (take (java.lang.Integer/valueOf (int (count col_names))) (repeat "?")))
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
          ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss))))
  (def insert-stmt (memoize insert-stmt*))
  (defn cql-insert
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
          (nil? (.one ^com.datastax.oss.driver.api.core.PagingIterable res))))))
  (defn select-string
    ([table p__10118]
      (let [vec__10119 p__10118 id_key (nth vec__10119 (int 0) nil) ks vec__10119]
        (str
          "select "
          (str/join ", " (map name ks))
          " from "
          table
          " where "
          (name id_key)
          " = ?"))))
  (defn select-stmt*
    ([session table ks]
      (let [ss (.build
                 (.setIdempotence
                   (com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder.
                     (select-string table ks))
                   false))]
        (.prepare
          ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
          ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss))))
  (def select-stmt (memoize select-stmt*))
  (defn select-with-consistency
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
            ^com.datastax.oss.driver.api.core.cql.Statement bound)))))
  (defn row->map
    ([row ks]
      (cond
        (= ks [:id :rev :map :val]) {:id
                                     (.getString
                                       ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                       (int 0)),
                                     :rev
                                     (long
                                       (.getLong
                                         ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                         (int 1))),
                                     :map
                                     (.getString
                                       ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                       (int 2)),
                                     :val
                                     (.getByteBuffer
                                       ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                       (int 3))}
        (= ks [:id2 :rev :map :val :chunks]) {:id
                                              (.getString
                                                ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                                (int 0)),
                                              :rev
                                              (long
                                                (.getLong
                                                  ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                                  (int 1))),
                                              :map
                                              (.getString
                                                ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                                (int 2)),
                                              :val
                                              (.getByteBuffer
                                                ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                                (int 3)),
                                              :chunks
                                              (java.lang.Integer/valueOf
                                                (int
                                                  (.getInt
                                                    ^com.datastax.oss.driver.api.core.data.GettableByIndex row
                                                    (int 4))))}
        :else (do (throw (java.lang.RuntimeException. "Invalid select.")) nil))))
  (defn cql-select
    ([session table id ks consistent?]
      (let [stmt (select-stmt session table ks)
            temp__5457__auto__ (or
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
        (when temp__5457__auto__ (let [row temp__5457__auto__] (row->map row ks))))))
  (defn delete-stmt*
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
          ^com.datastax.oss.driver.api.core.cql.SimpleStatement ss))))
  (def delete-stmt (memoize delete-stmt*))
  (defn cql-delete
    ([session table id p__10131]
      (let [vec__10132 p__10131
            seq__10133 (seq vec__10132)
            first__10134 (first seq__10133)
            seq__10133 (next seq__10133)
            id_key first__10134
            _ seq__10133
            stmt (delete-stmt session table id_key)
            bs (.bind
                 ^com.datastax.oss.driver.api.core.cql.PreparedStatement stmt
                 (into-array java.lang.Object [id]))
            res (.execute
                  ^com.datastax.oss.driver.api.core.cql.SyncCqlSession session
                  ^com.datastax.oss.driver.api.core.cql.Statement bs)]
        res)))
  (defn session-from-callback
    ([p__10136]
      (let [map__10137 p__10136
            map__10137 (if (seq? map__10137)
                         (clojure.lang.PersistentHashMap/create (seq map__10137))
                         map__10137)
            endpoint map__10137
            session_callback (get map__10137 :session-callback)]
        (when session_callback
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-v4")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info
                ^org.slf4j.Logger logger
                (logger/process (str "Using cassandra-session-callback " session_callback)))
              nil)
            nil)
          (let [temp__5455__auto__ (cb/create-callback (symbol session_callback))]
            (if temp__5455__auto__
              (let [callback temp__5455__auto__
                    temp__5455__auto__ (^clojure.lang.IFn callback endpoint)]
                (if temp__5455__auto__
                  (let [session temp__5455__auto__] session)
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-v4")]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process
                          (str "The cassandra-session-callback " session_callback " nil")))
                      nil)
                    nil)))
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra-v4")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process
                      (str "Could not resolve cassandra-session-callback " session_callback)))
                  nil)
                nil))))))))
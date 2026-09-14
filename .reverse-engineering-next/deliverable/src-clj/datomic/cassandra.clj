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
  (defn update-stmt*
    ([session table id_key col_names]
      (let [update_cql (str
                         "update "
                         table
                         " set "
                         (str/join
                           ", "
                           (map (fn fn__17729 ([p1__17728#] (str p1__17728# " = ?"))) col_names))
                         " where "
                         (name id_key)
                         " = ? if rev = ?")
            stmt (.prepare ^com.datastax.driver.core.Session session ^java.lang.String update_cql)]
        (.setConsistencyLevel
          ^com.datastax.driver.core.PreparedStatement stmt
          ConsistencyLevel/QUORUM)
        (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))
        stmt)))
  (def update-stmt (memoize update-stmt*))
  (defn updated?
    ([res]
      (let [temp__5457__auto__ (.one ^com.datastax.driver.core.ResultSet res)]
        (when temp__5457__auto__
          (let [row temp__5457__auto__]
            (.getBool ^com.datastax.driver.core.GettableByIndexData row (int 0)))))))
  (defn cql-update
    ([session table ensure_rev p__17734 v_map]
      (let [vec__17735 p__17734
            seq__17736 (seq vec__17735)
            first__17737 (first seq__17736)
            seq__17736 (next seq__17736)
            id_key first__17737
            ks seq__17736
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
            stmt (.prepare ^com.datastax.driver.core.Session session ^java.lang.String insert_cql)]
        (.setConsistencyLevel
          ^com.datastax.driver.core.PreparedStatement stmt
          ConsistencyLevel/QUORUM)
        (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))
        stmt)))
  (def insert-stmt (memoize insert-stmt*))
  (defn cql-insert
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
          (.isExhausted ^com.datastax.driver.core.PagingIterable res)))))
  (defn select-string
    ([table p__17742]
      (let [vec__17743 p__17742 id_key (nth vec__17743 (int 0) nil) ks vec__17743]
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
      (let [stmt (.prepare ^com.datastax.driver.core.Session session (select-string table ks))]
        (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy)))))
  (def select-stmt (memoize select-stmt*))
  (defn select-with-consistency
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
            ^com.datastax.driver.core.Statement bound)))))
  (defn row->map
    ([row ks]
      (cond
        (= ks [:id :rev :map :val]) {:id
                                     (.getString
                                       ^com.datastax.driver.core.GettableByIndexData row
                                       (int 0)),
                                     :rev
                                     (long
                                       (.getLong
                                         ^com.datastax.driver.core.GettableByIndexData row
                                         (int 1))),
                                     :map
                                     (.getString
                                       ^com.datastax.driver.core.GettableByIndexData row
                                       (int 2)),
                                     :val
                                     (.getBytes
                                       ^com.datastax.driver.core.GettableByIndexData row
                                       (int 3))}
        (= ks [:id2 :rev :map :val :chunks]) {:id
                                              (.getString
                                                ^com.datastax.driver.core.GettableByIndexData row
                                                (int 0)),
                                              :rev
                                              (long
                                                (.getLong
                                                  ^com.datastax.driver.core.GettableByIndexData row
                                                  (int 1))),
                                              :map
                                              (.getString
                                                ^com.datastax.driver.core.GettableByIndexData row
                                                (int 2)),
                                              :val
                                              (.getBytes
                                                ^com.datastax.driver.core.GettableByIndexData row
                                                (int 3)),
                                              :chunks
                                              (java.lang.Integer/valueOf
                                                (int
                                                  (.getInt
                                                    ^com.datastax.driver.core.GettableByIndexData row
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
                                   ConsistencyLevel/QUORUM
                                   true
                                   id))]
        (when temp__5457__auto__ (let [row temp__5457__auto__] (row->map row ks))))))
  (defn delete-stmt*
    ([session table id_key]
      (let [stmt (.prepare
                   ^com.datastax.driver.core.Session session
                   (str "delete from " table " where " (name id_key) " = ?"))]
        (.setConsistencyLevel
          ^com.datastax.driver.core.PreparedStatement stmt
          ConsistencyLevel/QUORUM)
        (.setRetryPolicy ^com.datastax.driver.core.PreparedStatement stmt (retry-policy))
        stmt)))
  (def delete-stmt (memoize delete-stmt*))
  (defn cql-delete
    ([session table id p__17755]
      (let [vec__17756 p__17755
            seq__17757 (seq vec__17756)
            first__17758 (first seq__17757)
            seq__17757 (next seq__17757)
            id_key first__17758
            ks seq__17757
            stmt (delete-stmt session table id_key)
            res (.execute
                  ^com.datastax.driver.core.Session session
                  (.bind
                    ^com.datastax.driver.core.PreparedStatement stmt
                    (into-array java.lang.Object [id])))]
        res)))
  (defn cluster-from-callback
    ([p__17760]
      (let [map__17761 p__17760
            map__17761 (if (seq? map__17761)
                         (clojure.lang.PersistentHashMap/create (seq map__17761))
                         map__17761)
            endpoint map__17761
            cluster_callback (get map__17761 :cluster-callback)]
        (when cluster_callback
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info
                ^org.slf4j.Logger logger
                (logger/process (str "Using cassandra-cluster-callback " cluster_callback)))
              nil)
            nil)
          (let [temp__5455__auto__ (cb/create-callback (symbol cluster_callback))]
            (if temp__5455__auto__
              (let [callback temp__5455__auto__
                    temp__5455__auto__ (^clojure.lang.IFn callback endpoint)]
                (if temp__5455__auto__
                  (let [cluster temp__5455__auto__] cluster)
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra")]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process
                          (str "The cassandra-cluster-callback " cluster_callback " nil")))
                      nil)
                    nil)))
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cassandra")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process
                      (str "Could not resolve cassandra-cluster-callback " cluster_callback)))
                  nil)
                nil))))))))
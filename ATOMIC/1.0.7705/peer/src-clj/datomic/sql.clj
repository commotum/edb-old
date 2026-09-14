(do
  (clojure.core/in-ns 'datomic.sql)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.sql)
    {:doc
     "JDBC primitives for the datomic_kvs table. Rows contain an id, integer revision, serialized map value, and binary value. Updates use the current revision in the WHERE clause so callers can perform optimistic compare-and-swap. Row operations acquire and close their connections; connect and execute-commands expose caller-owned connection lifecycles."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/require ['datomic.error :as 'error] ['clojure.string :as 'str])
      (clojure.core/import 'java.sql.Connection)
      (clojure.core/import 'java.sql.PreparedStatement)
      (clojure.core/import 'java.sql.ResultSet)
      (clojure.core/import 'java.sql.Types)
      (clojure.core/import 'javax.sql.DataSource)
      (clojure.core/refer 'clojure.core :exclude ['update])))
  (when-not (.equals 'datomic.sql 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.sql))
    (clojure.core/with-loading-context
      (do
        (clojure.core/require ['datomic.error :as 'error] ['clojure.string :as 'str])
        (clojure.core/import 'java.sql.Connection)
        (clojure.core/import 'java.sql.PreparedStatement)
        (clojure.core/import 'java.sql.ResultSet)
        (clojure.core/import 'java.sql.Types)
        (clojure.core/import 'javax.sql.DataSource)
        (clojure.core/refer 'clojure.core :exclude ['update]))))
  (set! *warn-on-reflection* true)
  (defn connect
    ([p__15281]
      (let [map__15282 p__15281
            map__15282 (if (seq? map__15282)
                         (if (next map__15282)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15282))
                           (if (seq map__15282) (first map__15282) {}))
                         map__15282)
            args map__15282
            datasource (get map__15282 :datasource)
            factory (get map__15282 :factory)]
        (cond
          datasource (.getConnection ^javax.sql.DataSource datasource)
          factory (^clojure.lang.IFn factory)
          :else (do
                  (error/arg
                    :db.error/invalid-sql-connection
                    "Must supply DataSource or Callable<Connection>"))))))
  (reset-meta!
    #'connect
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys [(.withMeta 'datasource {:tag 'DataSource}) 'factory], :as 'args}]),
       :doc
       "Obtains a JDBC Connection from :datasource or by invoking :factory. Raises :db.error/invalid-sql-connection when neither source is supplied. The caller owns the returned connection.",
       :column (int 1)}
      :name
      'connect
      :ns
      *ns*))
  (defn update-with-nulls
    ([spec id ensure_rev p__15284]
      (let [map__15285 p__15284
            map__15285 (if (seq? map__15285)
                         (if (next map__15285)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15285))
                           (if (seq map__15285) (first map__15285) {}))
                         map__15285)
            rev (get map__15285 :rev)
            map (get map__15285 :map)
            val (get map__15285 :val)]
        (with-open [conn (connect spec)]
          (let [update_sql "update datomic_kvs set rev=?, map=?, val=? where id=? and rev=?"]
            (with-open [stmt (.prepareStatement
                               ^java.sql.Connection conn
                               ^java.lang.String update_sql)]
              (do
                (.setObject ^java.sql.PreparedStatement stmt (int 1) rev (int Types/INTEGER))
                (.setObject ^java.sql.PreparedStatement stmt (int 2) map (int Types/LONGVARCHAR))
                (.setObject ^java.sql.PreparedStatement stmt (int 3) val (int Types/LONGVARBINARY))
                (.setObject ^java.sql.PreparedStatement stmt (int 4) id (int Types/VARCHAR))
                (.setObject
                  ^java.sql.PreparedStatement stmt
                  (int 5)
                  ensure_rev
                  (int Types/INTEGER))
                (java.lang.Integer/valueOf
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))))))))
  (reset-meta!
    #'update-with-nulls
    (assoc
      {:arglists (clojure.core/list ['spec 'id 'ensure-rev {:keys ['rev 'map 'val]}]),
       :doc
       "Replaces rev, map, and val for id only when the stored revision equals ensure-rev. Uses explicit JDBC types so nil map and val values are preserved. Returns the number of updated rows; zero reports a missing row or revision conflict.",
       :column (int 1)}
      :name
      'update-with-nulls
      :ns
      *ns*))
  (defn insert-with-nulls
    ([spec p__15287]
      (let [map__15288 p__15287
            map__15288 (if (seq? map__15288)
                         (if (next map__15288)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15288))
                           (if (seq map__15288) (first map__15288) {}))
                         map__15288)
            id (get map__15288 :id)
            rev (get map__15288 :rev)
            map (get map__15288 :map)
            val (get map__15288 :val)]
        (with-open [conn (connect spec)]
          (let [insert_sql "insert into datomic_kvs (id, rev, map, val) values (?, ?, ?, ?)"]
            (with-open [stmt (.prepareStatement
                               ^java.sql.Connection conn
                               ^java.lang.String insert_sql)]
              (do
                (.setObject ^java.sql.PreparedStatement stmt (int 1) id (int Types/LONGVARCHAR))
                (.setObject ^java.sql.PreparedStatement stmt (int 2) rev (int Types/INTEGER))
                (.setObject ^java.sql.PreparedStatement stmt (int 3) map (int Types/LONGVARCHAR))
                (.setObject ^java.sql.PreparedStatement stmt (int 4) val (int Types/LONGVARBINARY))
                (java.lang.Integer/valueOf
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))))))))
  (reset-meta!
    #'insert-with-nulls
    (assoc
      {:arglists (clojure.core/list ['spec {:keys ['id 'rev 'map 'val]}]),
       :doc
       "Inserts a complete datomic_kvs row using explicit JDBC types, including SQL NULL values. Returns the number of inserted rows and propagates duplicate-key and other JDBC failures.",
       :column (int 1)}
      :name
      'insert-with-nulls
      :ns
      *ns*))
  (defn update
    ([spec id ensure_rev value-map]
      (with-open [conn (connect spec)]
        (let [col_vals (filter
                         (fn fn__15291
                           ([p__15290]
                             (let [vec__15292 p__15290
                                   k (nth vec__15292 (int 0) nil)
                                   v (nth vec__15292 (int 1) nil)]
                               v)))
                         (select-keys value-map [:rev :map :val]))
              col_num (count col_vals)
              col_str (str/join
                        ", "
                        (map
                          (fn fn__15297
                            ([p__15296]
                              (let [vec__15298 p__15296
                                    k (nth vec__15298 (int 0) nil)
                                    v (nth vec__15298 (int 1) nil)]
                                (str (name k) "=?"))))
                          col_vals))
              update_sql (str "update datomic_kvs set " col_str " where id=? and rev=?")]
          (with-open [stmt (.prepareStatement
                             ^java.sql.Connection conn
                             ^java.lang.String update_sql)]
            (do
              (dorun
                (map-indexed
                  (fn fn__15302
                    ([i cv]
                      (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                      nil))
                  col_vals))
              (.setObject ^java.sql.PreparedStatement stmt (int (inc col_num)) id)
              (.setObject ^java.sql.PreparedStatement stmt (int (+ col_num 2)) ensure_rev)
              (java.lang.Integer/valueOf
                (int (.executeUpdate ^java.sql.PreparedStatement stmt)))))))))
  (reset-meta!
    #'update
    (assoc
      {:arglists (clojure.core/list ['spec 'id 'ensure-rev 'value-map]),
       :doc
       "Updates the truthy :rev, :map, and :val entries in value-map when id currently has ensure-rev. Returns the affected-row count, with zero indicating a missing row or revision conflict. Use update-with-nulls when a column must be set to nil.",
       :column (int 1)}
      :name
      'update
      :ns
      *ns*))
  (defn insert
    ([spec value-map]
      (with-open [conn (connect spec)]
        (let [col_vals (filter
                         (fn fn__15306
                           ([p__15305]
                             (let [vec__15307 p__15305
                                   k (nth vec__15307 (int 0) nil)
                                   v (nth vec__15307 (int 1) nil)]
                               v)))
                         (select-keys value-map [:id :rev :map :val]))
              col_num (count col_vals)
              col_str (str/join
                        ", "
                        (map
                          (fn fn__15312
                            ([p__15311]
                              (let [vec__15313 p__15311
                                    k (nth vec__15313 (int 0) nil)
                                    v (nth vec__15313 (int 1) nil)]
                                (name k))))
                          col_vals))
              col_placeholders (str/join
                                 ", "
                                 (repeatedly
                                   (java.lang.Integer/valueOf (int col_num))
                                   (constantly "?")))
              insert_sql (str
                           "insert into datomic_kvs ("
                           col_str
                           ") values ("
                           col_placeholders
                           ")")]
          (with-open [stmt (.prepareStatement
                             ^java.sql.Connection conn
                             ^java.lang.String insert_sql)]
            (do
              (dorun
                (map-indexed
                  (fn fn__15317
                    ([i cv]
                      (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                      nil))
                  col_vals))
              (java.lang.Integer/valueOf
                (int (.executeUpdate ^java.sql.PreparedStatement stmt)))))))))
  (reset-meta!
    #'insert
    (assoc
      {:arglists (clojure.core/list ['spec 'value-map]),
       :doc
       "Inserts the truthy :id, :rev, :map, and :val entries in value-map and returns the affected-row count. Use insert-with-nulls when SQL NULL columns must be written explicitly.",
       :column (int 1)}
      :name
      'insert
      :ns
      *ns*))
  (defn select
    ([spec id]
      (with-open [conn (connect spec)]
        (with-open [stmt (.prepareStatement
                           ^java.sql.Connection conn
                           "select id, rev, map, val from datomic_kvs where id = ?")]
          (do
            (.setObject ^java.sql.PreparedStatement stmt (int 1) id)
            (with-open [rs (.executeQuery ^java.sql.PreparedStatement stmt)]
              (when (.next ^java.sql.ResultSet rs)
                {:id (.getString ^java.sql.ResultSet rs "id"),
                 :rev (long (.getLong ^java.sql.ResultSet rs "rev")),
                 :map (.getString ^java.sql.ResultSet rs "map"),
                 :val (.getBytes ^java.sql.ResultSet rs "val")})))))))
  (reset-meta!
    #'select
    (assoc
      {:arglists (clojure.core/list ['spec 'id]),
       :doc
       "Returns the datomic_kvs row for id as {:id :rev :map :val}, or nil when no row exists. Binary values are returned as byte arrays.",
       :column (int 1)}
      :name
      'select
      :ns
      *ns*))
  (defn delete
    ([spec id]
      (with-open [conn (connect spec)]
        (with-open [stmt (.prepareStatement
                           ^java.sql.Connection conn
                           "delete from datomic_kvs where id = ?")]
          (do
            (.setObject ^java.sql.PreparedStatement stmt (int 1) id)
            (java.lang.Integer/valueOf
              (int (.executeUpdate ^java.sql.PreparedStatement stmt))))))))
  (reset-meta!
    #'delete
    (assoc
      {:arglists (clojure.core/list ['spec 'id]),
       :doc "Deletes id from datomic_kvs and returns the number of affected rows.",
       :column (int 1)}
      :name
      'delete
      :ns
      *ns*))
  (defn execute-commands
    ([conn & cmds]
      (loop [seq_15322 (seq cmds) chunk_15323 nil count_15324 0 i_15325 0]
        (if (< i_15325 count_15324)
          (let [cmd (.nth ^clojure.lang.Indexed chunk_15323 (int i_15325))]
            (with-open [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String cmd)]
              (.execute ^java.sql.PreparedStatement stmt))
            (recur seq_15322 chunk_15323 count_15324 (inc i_15325)))
          (let [temp__5804__auto__ (seq seq_15322)]
            (when temp__5804__auto__
              (let [seq_15322 temp__5804__auto__]
                (if (chunked-seq? seq_15322)
                  (let [c__6065__auto__ (chunk-first seq_15322)]
                    (recur
                      (chunk-rest seq_15322)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [cmd (first seq_15322)]
                    (with-open [stmt (.prepareStatement
                                       ^java.sql.Connection conn
                                       ^java.lang.String cmd)]
                      (.execute ^java.sql.PreparedStatement stmt))
                    (recur (next seq_15322) nil 0 0))))))))))
  (reset-meta!
    #'execute-commands
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'Connection}) '& 'cmds]),
       :doc
       "Executes SQL command strings sequentially on a caller-owned connection. Each prepared statement is closed after execution; transaction and connection lifecycle remain with the caller.",
       :column (int 1)}
      :name
      'execute-commands
      :ns
      *ns*)))

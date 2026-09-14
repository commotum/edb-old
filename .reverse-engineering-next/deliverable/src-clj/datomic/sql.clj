(do
  (clojure.core/in-ns 'datomic.sql)
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
    ([p__11481]
      (let [map__11482 p__11481
            map__11482 (if (seq? map__11482)
                         (clojure.lang.PersistentHashMap/create (seq map__11482))
                         map__11482)
            args map__11482
            datasource (get map__11482 :datasource)
            factory (get map__11482 :factory)]
        (cond
          datasource (.getConnection ^javax.sql.DataSource datasource)
          factory (^clojure.lang.IFn factory)
          :else (do
                  (error/arg
                    :db.error/invalid-sql-connection
                    "Must supply DataSource or Callable<Connection>"))))))
  (defn update-with-nulls
    ([spec id ensure_rev p__11484]
      (let [map__11485 p__11484
            map__11485 (if (seq? map__11485)
                         (clojure.lang.PersistentHashMap/create (seq map__11485))
                         map__11485)
            rev (get map__11485 :rev)
            map (get map__11485 :map)
            val (get map__11485 :val)
            conn (connect spec)]
        (try
          (let [update_sql "update datomic_kvs set rev=?, map=?, val=? where id=? and rev=?"
                stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String update_sql)]
            (try
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
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))
              (finally (do (.close ^java.sql.Statement stmt) nil))))
          (finally (do (.close ^java.sql.Connection conn) nil))))))
  (defn insert-with-nulls
    ([spec p__11487]
      (let [map__11488 p__11487
            map__11488 (if (seq? map__11488)
                         (clojure.lang.PersistentHashMap/create (seq map__11488))
                         map__11488)
            id (get map__11488 :id)
            rev (get map__11488 :rev)
            map (get map__11488 :map)
            val (get map__11488 :val)
            conn (connect spec)]
        (try
          (let [insert_sql "insert into datomic_kvs (id, rev, map, val) values (?, ?, ?, ?)"
                stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String insert_sql)]
            (try
              (do
                (.setObject ^java.sql.PreparedStatement stmt (int 1) id (int Types/LONGVARCHAR))
                (.setObject ^java.sql.PreparedStatement stmt (int 2) rev (int Types/INTEGER))
                (.setObject ^java.sql.PreparedStatement stmt (int 3) map (int Types/LONGVARCHAR))
                (.setObject ^java.sql.PreparedStatement stmt (int 4) val (int Types/LONGVARBINARY))
                (java.lang.Integer/valueOf
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))
              (finally (do (.close ^java.sql.Statement stmt) nil))))
          (finally (do (.close ^java.sql.Connection conn) nil))))))
  (defn update
    ([spec id ensure_rev v_map]
      (let [conn (connect spec)]
        (try
          (let [col_vals (filter
                           (fn fn__11491
                             ([p__11490]
                               (let [vec__11492 p__11490
                                     k (nth vec__11492 (int 0) nil)
                                     v (nth vec__11492 (int 1) nil)]
                                 v)))
                           (select-keys v_map [:rev :map :val]))
                col_num (count col_vals)
                col_str (str/join
                          ", "
                          (map
                            (fn fn__11497
                              ([p__11496]
                                (let [vec__11498 p__11496
                                      k (nth vec__11498 (int 0) nil)
                                      v (nth vec__11498 (int 1) nil)]
                                  (str (name k) "=?"))))
                            col_vals))
                update_sql (str "update datomic_kvs set " col_str " where id=? and rev=?")
                stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String update_sql)]
            (try
              (do
                (dorun
                  (map-indexed
                    (fn fn__11502
                      ([i cv]
                        (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                        nil))
                    col_vals))
                (.setObject ^java.sql.PreparedStatement stmt (int (inc col_num)) id)
                (.setObject ^java.sql.PreparedStatement stmt (int (+ col_num 2)) ensure_rev)
                (java.lang.Integer/valueOf
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))
              (finally (do (.close ^java.sql.Statement stmt) nil))))
          (finally (do (.close ^java.sql.Connection conn) nil))))))
  (defn insert
    ([spec v_map]
      (let [conn (connect spec)]
        (try
          (let [col_vals (filter
                           (fn fn__11506
                             ([p__11505]
                               (let [vec__11507 p__11505
                                     k (nth vec__11507 (int 0) nil)
                                     v (nth vec__11507 (int 1) nil)]
                                 v)))
                           (select-keys v_map [:id :rev :map :val]))
                col_num (count col_vals)
                col_str (str/join
                          ", "
                          (map
                            (fn fn__11512
                              ([p__11511]
                                (let [vec__11513 p__11511
                                      k (nth vec__11513 (int 0) nil)
                                      v (nth vec__11513 (int 1) nil)]
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
                             ")")
                stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String insert_sql)]
            (try
              (do
                (dorun
                  (map-indexed
                    (fn fn__11517
                      ([i cv]
                        (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                        nil))
                    col_vals))
                (java.lang.Integer/valueOf
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))
              (finally (do (.close ^java.sql.Statement stmt) nil))))
          (finally (do (.close ^java.sql.Connection conn) nil))))))
  (defn select
    ([spec id]
      (let [conn (connect spec)]
        (try
          (let [stmt (.prepareStatement
                       ^java.sql.Connection conn
                       "select id, rev, map, val from datomic_kvs where id = ?")]
            (try
              (do
                (.setObject ^java.sql.PreparedStatement stmt (int 1) id)
                (let [rs (.executeQuery ^java.sql.PreparedStatement stmt)]
                  (try
                    (when (.next ^java.sql.ResultSet rs)
                      {:id (.getString ^java.sql.ResultSet rs "id"),
                       :rev (long (.getLong ^java.sql.ResultSet rs "rev")),
                       :map (.getString ^java.sql.ResultSet rs "map"),
                       :val (.getBytes ^java.sql.ResultSet rs "val")})
                    (finally (do (.close ^java.sql.ResultSet rs) nil)))))
              (finally (do (.close ^java.sql.Statement stmt) nil))))
          (finally (do (.close ^java.sql.Connection conn) nil))))))
  (defn delete
    ([spec id]
      (let [conn (connect spec)]
        (try
          (let [stmt (.prepareStatement
                       ^java.sql.Connection conn
                       "delete from datomic_kvs where id = ?")]
            (try
              (do
                (.setObject ^java.sql.PreparedStatement stmt (int 1) id)
                (java.lang.Integer/valueOf
                  (int (.executeUpdate ^java.sql.PreparedStatement stmt))))
              (finally (do (.close ^java.sql.Statement stmt) nil))))
          (finally (do (.close ^java.sql.Connection conn) nil))))))
  (defn execute-commands
    ([conn & cmds]
      (loop [seq_11522 (seq cmds) chunk_11523 nil count_11524 0 i_11525 0]
        (if (< i_11525 count_11524)
          (let [cmd (.nth ^clojure.lang.Indexed chunk_11523 (int i_11525))]
            (let [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String cmd)]
              (try
                (.execute ^java.sql.PreparedStatement stmt)
                (finally (do (.close ^java.sql.Statement stmt) nil))))
            (recur seq_11522 chunk_11523 count_11524 (inc i_11525)))
          (let [temp__5457__auto__ (seq seq_11522)]
            (when temp__5457__auto__
              (let [seq_11522 temp__5457__auto__]
                (if (chunked-seq? seq_11522)
                  (let [c__5719__auto__ (chunk-first seq_11522)]
                    (recur
                      (chunk-rest seq_11522)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [cmd (first seq_11522)]
                    (let [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String cmd)]
                      (try
                        (.execute ^java.sql.PreparedStatement stmt)
                        (finally (do (.close ^java.sql.Statement stmt) nil))))
                    (recur (next seq_11522) nil 0 0)))))))))))
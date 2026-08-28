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
    ([p__16481]
      (let [map__16482 p__16481
            map__16482 (if (seq? map__16482)
                         (if (next map__16482)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16482))
                           (if (seq map__16482) (first map__16482) {}))
                         map__16482)
            args map__16482
            datasource (get map__16482 :datasource)
            factory (get map__16482 :factory)]
        (cond
          datasource (.getConnection ^javax.sql.DataSource datasource)
          factory (^clojure.lang.IFn factory)
          :else (do
                  (error/arg
                    :db.error/invalid-sql-connection
                    "Must supply DataSource or Callable<Connection>"))))))
  (defn update-with-nulls
    ([spec id ensure_rev p__16484]
      (let [map__16485 p__16484
            map__16485 (if (seq? map__16485)
                         (if (next map__16485)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16485))
                           (if (seq map__16485) (first map__16485) {}))
                         map__16485)
            rev (get map__16485 :rev)
            map (get map__16485 :map)
            val (get map__16485 :val)]
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
  (defn insert-with-nulls
    ([spec p__16487]
      (let [map__16488 p__16487
            map__16488 (if (seq? map__16488)
                         (if (next map__16488)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16488))
                           (if (seq map__16488) (first map__16488) {}))
                         map__16488)
            id (get map__16488 :id)
            rev (get map__16488 :rev)
            map (get map__16488 :map)
            val (get map__16488 :val)]
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
  (defn update
    ([spec id ensure_rev v_map]
      (with-open [conn (connect spec)]
        (let [col_vals (filter
                         (fn fn__16491
                           ([p__16490]
                             (let [vec__16492 p__16490
                                   k (nth vec__16492 (int 0) nil)
                                   v (nth vec__16492 (int 1) nil)]
                               v)))
                         (select-keys v_map [:rev :map :val]))
              col_num (count col_vals)
              col_str (str/join
                        ", "
                        (map
                          (fn fn__16497
                            ([p__16496]
                              (let [vec__16498 p__16496
                                    k (nth vec__16498 (int 0) nil)
                                    v (nth vec__16498 (int 1) nil)]
                                (str (name k) "=?"))))
                          col_vals))
              update_sql (str "update datomic_kvs set " col_str " where id=? and rev=?")]
          (with-open [stmt (.prepareStatement
                             ^java.sql.Connection conn
                             ^java.lang.String update_sql)]
            (do
              (dorun
                (map-indexed
                  (fn fn__16502
                    ([i cv]
                      (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                      nil))
                  col_vals))
              (.setObject ^java.sql.PreparedStatement stmt (int (inc col_num)) id)
              (.setObject ^java.sql.PreparedStatement stmt (int (+ col_num 2)) ensure_rev)
              (java.lang.Integer/valueOf
                (int (.executeUpdate ^java.sql.PreparedStatement stmt)))))))))
  (defn insert
    ([spec v_map]
      (with-open [conn (connect spec)]
        (let [col_vals (filter
                         (fn fn__16506
                           ([p__16505]
                             (let [vec__16507 p__16505
                                   k (nth vec__16507 (int 0) nil)
                                   v (nth vec__16507 (int 1) nil)]
                               v)))
                         (select-keys v_map [:id :rev :map :val]))
              col_num (count col_vals)
              col_str (str/join
                        ", "
                        (map
                          (fn fn__16512
                            ([p__16511]
                              (let [vec__16513 p__16511
                                    k (nth vec__16513 (int 0) nil)
                                    v (nth vec__16513 (int 1) nil)]
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
                  (fn fn__16517
                    ([i cv]
                      (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                      nil))
                  col_vals))
              (java.lang.Integer/valueOf
                (int (.executeUpdate ^java.sql.PreparedStatement stmt)))))))))
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
  (defn execute-commands
    ([conn & cmds]
      (loop [seq_16522 (seq cmds) chunk_16523 nil count_16524 0 i_16525 0]
        (if (< i_16525 count_16524)
          (let [cmd (.nth ^clojure.lang.Indexed chunk_16523 (int i_16525))]
            (with-open [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String cmd)]
              (.execute ^java.sql.PreparedStatement stmt))
            (recur seq_16522 chunk_16523 count_16524 (inc i_16525)))
          (let [temp__5804__auto__ (seq seq_16522)]
            (when temp__5804__auto__
              (let [seq_16522 temp__5804__auto__]
                (if (chunked-seq? seq_16522)
                  (let [c__6065__auto__ (chunk-first seq_16522)]
                    (recur
                      (chunk-rest seq_16522)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [cmd (first seq_16522)]
                    (with-open [stmt (.prepareStatement
                                       ^java.sql.Connection conn
                                       ^java.lang.String cmd)]
                      (.execute ^java.sql.PreparedStatement stmt))
                    (recur (next seq_16522) nil 0 0)))))))))))
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
  (def connect
   (fn connect
     ([p__10714]
       (let [map__10715 p__10714
             map__10715 (if (seq? map__10715)
                          (if (next map__10715)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10715))
                            (if (seq map__10715) (first map__10715) {}))
                          map__10715)
             args map__10715
             datasource (get map__10715 :datasource)
             factory (get map__10715 :factory)]
         (cond
           datasource (.getConnection ^javax.sql.DataSource datasource)
           factory (^clojure.lang.IFn factory)
           :else (do
                   (error/arg
                     :db.error/invalid-sql-connection
                     "Must supply DataSource or Callable<Connection>")))))))
  (reset-meta!
    #'connect
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys [(.withMeta 'datasource {:tag 'DataSource}) 'factory], :as 'args}]),
       :column (int 1)}
      :name
      'connect
      :ns
      *ns*))
  (def update-with-nulls
   (fn update_with_nulls
     ([spec id ensure_rev p__10717]
       (let [map__10718 p__10717
             map__10718 (if (seq? map__10718)
                          (if (next map__10718)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10718))
                            (if (seq map__10718) (first map__10718) {}))
                          map__10718)
             rev (get map__10718 :rev)
             map (get map__10718 :map)
             val (get map__10718 :val)]
         (with-open [conn (connect spec)]
           (let [update_sql "update datomic_kvs set rev=?, map=?, val=? where id=? and rev=?"]
             (with-open [stmt (.prepareStatement
                                ^java.sql.Connection conn
                                ^java.lang.String update_sql)]
               (do
                 (.setObject ^java.sql.PreparedStatement stmt (int 1) rev (int Types/INTEGER))
                 (.setObject ^java.sql.PreparedStatement stmt (int 2) map (int Types/LONGVARCHAR))
                 (.setObject
                   ^java.sql.PreparedStatement stmt
                   (int 3)
                   val
                   (int Types/LONGVARBINARY))
                 (.setObject ^java.sql.PreparedStatement stmt (int 4) id (int Types/VARCHAR))
                 (.setObject
                   ^java.sql.PreparedStatement stmt
                   (int 5)
                   ensure_rev
                   (int Types/INTEGER))
                 (java.lang.Integer/valueOf
                   (int (.executeUpdate ^java.sql.PreparedStatement stmt)))))))))))
  (reset-meta!
    #'update-with-nulls
    (assoc
      {:arglists (clojure.core/list ['spec 'id 'ensure-rev {:keys ['rev 'map 'val]}]),
       :column (int 1)}
      :name
      'update-with-nulls
      :ns
      *ns*))
  (def insert-with-nulls
   (fn insert_with_nulls
     ([spec p__10720]
       (let [map__10721 p__10720
             map__10721 (if (seq? map__10721)
                          (if (next map__10721)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10721))
                            (if (seq map__10721) (first map__10721) {}))
                          map__10721)
             id (get map__10721 :id)
             rev (get map__10721 :rev)
             map (get map__10721 :map)
             val (get map__10721 :val)]
         (with-open [conn (connect spec)]
           (let [insert_sql "insert into datomic_kvs (id, rev, map, val) values (?, ?, ?, ?)"]
             (with-open [stmt (.prepareStatement
                                ^java.sql.Connection conn
                                ^java.lang.String insert_sql)]
               (do
                 (.setObject ^java.sql.PreparedStatement stmt (int 1) id (int Types/LONGVARCHAR))
                 (.setObject ^java.sql.PreparedStatement stmt (int 2) rev (int Types/INTEGER))
                 (.setObject ^java.sql.PreparedStatement stmt (int 3) map (int Types/LONGVARCHAR))
                 (.setObject
                   ^java.sql.PreparedStatement stmt
                   (int 4)
                   val
                   (int Types/LONGVARBINARY))
                 (java.lang.Integer/valueOf
                   (int (.executeUpdate ^java.sql.PreparedStatement stmt)))))))))))
  (reset-meta!
    #'insert-with-nulls
    (assoc
      {:arglists (clojure.core/list ['spec {:keys ['id 'rev 'map 'val]}]), :column (int 1)}
      :name
      'insert-with-nulls
      :ns
      *ns*))
  (def update
   (fn update
     ([spec id ensure_rev v_map]
       (with-open [conn (connect spec)]
         (let [col_vals (filter
                          (fn fn__10724
                            ([p__10723]
                              (let [vec__10725 p__10723
                                    k (nth vec__10725 (int 0) nil)
                                    v (nth vec__10725 (int 1) nil)]
                                v)))
                          (select-keys v_map [:rev :map :val]))
               col_num (count col_vals)
               col_str (str/join
                         ", "
                         (map
                           (fn fn__10730
                             ([p__10729]
                               (let [vec__10731 p__10729
                                     k (nth vec__10731 (int 0) nil)
                                     v (nth vec__10731 (int 1) nil)]
                                 (str (name k) "=?"))))
                           col_vals))
               update_sql (str "update datomic_kvs set " col_str " where id=? and rev=?")]
           (with-open [stmt (.prepareStatement
                              ^java.sql.Connection conn
                              ^java.lang.String update_sql)]
             (do
               (dorun
                 (map-indexed
                   (fn fn__10735
                     ([i cv]
                       (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                       nil))
                   col_vals))
               (.setObject ^java.sql.PreparedStatement stmt (int (inc col_num)) id)
               (.setObject ^java.sql.PreparedStatement stmt (int (+ col_num 2)) ensure_rev)
               (java.lang.Integer/valueOf
                 (int (.executeUpdate ^java.sql.PreparedStatement stmt))))))))))
  (reset-meta!
    #'update
    (assoc
      {:arglists (clojure.core/list ['spec 'id 'ensure-rev 'v-map]), :column (int 1)}
      :name
      'update
      :ns
      *ns*))
  (def insert
   (fn insert
     ([spec v_map]
       (with-open [conn (connect spec)]
         (let [col_vals (filter
                          (fn fn__10739
                            ([p__10738]
                              (let [vec__10740 p__10738
                                    k (nth vec__10740 (int 0) nil)
                                    v (nth vec__10740 (int 1) nil)]
                                v)))
                          (select-keys v_map [:id :rev :map :val]))
               col_num (count col_vals)
               col_str (str/join
                         ", "
                         (map
                           (fn fn__10745
                             ([p__10744]
                               (let [vec__10746 p__10744
                                     k (nth vec__10746 (int 0) nil)
                                     v (nth vec__10746 (int 1) nil)]
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
                   (fn fn__10750
                     ([i cv]
                       (.setObject ^java.sql.PreparedStatement stmt (int (inc i)) (second cv))
                       nil))
                   col_vals))
               (java.lang.Integer/valueOf
                 (int (.executeUpdate ^java.sql.PreparedStatement stmt))))))))))
  (reset-meta!
    #'insert
    (assoc {:arglists (clojure.core/list ['spec 'v-map]), :column (int 1)} :name 'insert :ns *ns*))
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
    (assoc {:arglists (clojure.core/list ['spec 'id]), :column (int 1)} :name 'select :ns *ns*))
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
    (assoc {:arglists (clojure.core/list ['spec 'id]), :column (int 1)} :name 'delete :ns *ns*))
  (def execute-commands
   (fn execute_commands
     ([conn & cmds]
       (loop [seq_10755 (seq cmds) chunk_10756 nil count_10757 0 i_10758 0]
         (if (< i_10758 count_10757)
           (let [cmd (.nth ^clojure.lang.Indexed chunk_10756 (int i_10758))]
             (with-open [stmt (.prepareStatement ^java.sql.Connection conn ^java.lang.String cmd)]
               (.execute ^java.sql.PreparedStatement stmt))
             (recur seq_10755 chunk_10756 count_10757 (inc i_10758)))
           (let [temp__5825__auto__ (seq seq_10755)]
             (when temp__5825__auto__
               (let [seq_10755 temp__5825__auto__]
                 (if (chunked-seq? seq_10755)
                   (let [c__6090__auto__ (chunk-first seq_10755)]
                     (recur
                       (chunk-rest seq_10755)
                       c__6090__auto__
                       (int (count c__6090__auto__))
                       (int 0)))
                   (let [cmd (first seq_10755)]
                     (with-open [stmt (.prepareStatement
                                        ^java.sql.Connection conn
                                        ^java.lang.String cmd)]
                       (.execute ^java.sql.PreparedStatement stmt))
                     (recur (next seq_10755) nil 0 0)))))))))))
  (reset-meta!
    #'execute-commands
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'Connection}) '& 'cmds]),
       :column (int 1)}
      :name
      'execute-commands
      :ns
      *ns*)))
(do
  (clojure.core/in-ns 'datomic.h2)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.sql :as 'sql]
        ['datomic.slf4j :as 'logger]
        ['datomic.kv-sql :as 'kv-sql])
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'org.h2.tools.Server)))
  (when-not (.equals 'datomic.h2 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.h2))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.sql :as 'sql]
          ['datomic.slf4j :as 'logger]
          ['datomic.kv-sql :as 'kv-sql])
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'org.h2.tools.Server))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.h2" "driver-manager-lock")
    {:private true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.h2" "driver-manager-lock") (java.lang.Object.))
  (.setMeta (clojure.lang.RT/var "datomic.h2" "create-sql-spec*") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.h2" "create-sql-spec*")
    (memoize
      (fn fn__16682
        ([p__16681]
          (let [map__16683 p__16681
                map__16683 (if (seq? map__16683)
                             (if (next map__16683)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__16683))
                               (if (seq map__16683) (first map__16683) {}))
                             map__16683)
                args map__16683
                sql_url (get map__16683 :sql-url)
                username (get map__16683 :username)
                password (get map__16683 :password)]
            (when-not (and sql_url username password)
              (throw
                (java.lang.AssertionError.
                  (str
                    "Assert failed: "
                    (pr-str (clojure.core/list 'and 'sql-url 'username 'password))))))
            {:datasource
             (doto
               (org.apache.tomcat.jdbc.pool.DataSource.)
               (.setUrl ^java.lang.String sql_url)
               (.setUsername ^java.lang.String username)
               (.setPassword ^java.lang.String password)
               (.setDriverClassName "org.h2.Driver")
               (.setValidationQuery "SELECT 1")
               (.setTestWhileIdle (boolean (.booleanValue true)))
               (.setInitialSize (int 2)))})))))
  (def create-sql-spec
   (fn create_sql_spec
     ([cluster_map] (locking driver-manager-lock (create-sql-spec* cluster_map)))))
  (reset-meta!
    #'create-sql-spec
    (assoc
      {:arglists (clojure.core/list ['cluster-map]), :column (int 1)}
      :name
      'create-sql-spec
      :ns
      *ns*))
  (def sql-url (fn sql_url ([data_dir] (str "jdbc:h2:" data_dir "/datomic"))))
  (reset-meta!
    #'sql-url
    (assoc {:arglists (clojure.core/list ['data-dir]), :column (int 1)} :name 'sql-url :ns *ns*))
  (def try-connect
   (fn try_connect
     ([p__16692]
       (let [map__16693 p__16692
             map__16693 (if (seq? map__16693)
                          (if (next map__16693)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16693))
                            (if (seq map__16693) (first map__16693) {}))
                          map__16693)
             data_dir (get map__16693 :data-dir)
             username (get map__16693 :username)
             password (get map__16693 :password)]
         (when-not (and data_dir username password)
           (throw
             (java.lang.AssertionError.
               (str
                 "Assert failed: "
                 (pr-str (clojure.core/list 'and 'data-dir 'username 'password))))))
         (try
           (sql/connect
             (create-sql-spec
               {:sql-url (sql-url data_dir), :username username, :password password}))
           (catch
             java.sql.SQLException
             e
             (when-not (= (.getErrorCode ^java.sql.SQLException e) 28000)
               (throw ^java.lang.Throwable e)
               nil)))))))
  (reset-meta!
    #'try-connect
    (assoc
      {:arglists (clojure.core/list [{:keys ['data-dir 'username 'password]}]), :column (int 1)}
      :name
      'try-connect
      :ns
      *ns*))
  (defn uq ([username] (if (= "" username) "\"\"" username)))
  (reset-meta!
    #'uq
    (assoc {:arglists (clojure.core/list ['username]), :column (int 1)} :name 'uq :ns *ns*))
  (def rename-user-cmd
   (fn rename_user_cmd
     ([p__16698]
       (let [map__16699 p__16698
             map__16699 (if (seq? map__16699)
                          (if (next map__16699)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16699))
                            (if (seq map__16699) (first map__16699) {}))
                          map__16699)
             old_user (get map__16699 :old-user)
             user (get map__16699 :user)]
         (when (and (and old_user user) (not= old_user user))
           (format "alter user %s rename to %s" (uq old_user) (uq user)))))))
  (reset-meta!
    #'rename-user-cmd
    (assoc
      {:arglists (clojure.core/list [{:keys ['old-user 'user]}]), :column (int 1)}
      :name
      'rename-user-cmd
      :ns
      *ns*))
  (def set-password-cmd
   (fn set_password_cmd
     ([p__16703]
       (let [map__16704 p__16703
             map__16704 (if (seq? map__16704)
                          (if (next map__16704)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16704))
                            (if (seq map__16704) (first map__16704) {}))
                          map__16704)
             user (get map__16704 :user)
             password (get map__16704 :password)]
         (when (and user password)
           (format "alter user %s set password '%s'" (uq user) password))))))
  (reset-meta!
    #'set-password-cmd
    (assoc
      {:arglists (clojure.core/list [{:keys ['user 'password]}]), :column (int 1)}
      :name
      'set-password-cmd
      :ns
      *ns*))
  (def updating-connect
   (fn updating_connect
     ([data_dir p__16707]
       (let [map__16708 p__16707
             map__16708 (if (seq? map__16708)
                          (if (next map__16708)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16708))
                            (if (seq map__16708) (first map__16708) {}))
                          map__16708)
             args map__16708
             user (get map__16708 :user)
             password (get map__16708 :password)
             old_user (get map__16708 :old-user)
             old_password (get map__16708 :old-password)]
         (when (and old_user user old_password password)
           (let [temp__5804__auto__ (try-connect
                                      {:data-dir data_dir,
                                       :username old_user,
                                       :password old_password})]
             (when temp__5804__auto__
               (let [conn temp__5804__auto__]
                 (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.h2")]
                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                     (.info
                       ^org.slf4j.Logger logger
                       (logger/process {:event :storage/update-password, :user user})))
                   nil)
                 (apply
                   sql/execute-commands
                   conn
                   (filter identity [(rename-user-cmd args) (set-password-cmd args)]))
                 conn))))))))
  (reset-meta!
    #'updating-connect
    (assoc
      {:arglists
       (clojure.core/list
         ['data-dir {:keys ['user 'password 'old-user 'old-password], :as 'args}]),
       :column (int 1)}
      :name
      'updating-connect
      :ns
      *ns*))
  (def ensure-admin-conn
   (fn ensure_admin_conn
     ([p__16714]
       (let [map__16715 p__16714
             map__16715 (if (seq? map__16715)
                          (if (next map__16715)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16715))
                            (if (seq map__16715) (first map__16715) {}))
                          map__16715)
             cluster_map map__16715
             data_dir (get map__16715 :data-dir)
             storage_admin_password (get map__16715 :storage-admin-password)
             old_storage_admin_password (get map__16715 :old-storage-admin-password)]
         (or
           (if storage_admin_password
             (try-connect {:data-dir data_dir, :username "", :password storage_admin_password})
             (try-connect {:data-dir data_dir, :username "", :password ""}))
           (updating-connect
             data_dir
             {:old-user "",
              :old-password old_storage_admin_password,
              :user "",
              :password storage_admin_password})
           (updating-connect
             data_dir
             {:old-user "", :old-password "", :user "", :password storage_admin_password}))))))
  (reset-meta!
    #'ensure-admin-conn
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['data-dir 'storage-admin-password 'old-storage-admin-password],
           :as 'cluster-map}]),
       :column (int 1)}
      :name
      'ensure-admin-conn
      :ns
      *ns*))
  (def ensure-datomic-password
   (fn ensure_datomic_password
     ([p__16719]
       (let [map__16720 p__16719
             map__16720 (if (seq? map__16720)
                          (if (next map__16720)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16720))
                            (if (seq map__16720) (first map__16720) {}))
                          map__16720)
             cluster_map map__16720
             data_dir (get map__16720 :data-dir)
             storage_datomic_password (get map__16720 :storage-datomic-password)
             old_storage_datomic_password (get map__16720 :old-storage-datomic-password)]
         (or
           (if storage_datomic_password
             (try-connect
               {:data-dir data_dir, :username "datomic", :password storage_datomic_password})
             (try-connect {:data-dir data_dir, :username "datomic", :password "datomic"}))
           (updating-connect
             data_dir
             {:old-user "datomic",
              :old-password old_storage_datomic_password,
              :user "datomic",
              :password storage_datomic_password})
           (updating-connect
             data_dir
             {:old-user "datomic",
              :old-password "datomic",
              :user "datomic",
              :password storage_datomic_password}))))))
  (reset-meta!
    #'ensure-datomic-password
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['data-dir 'storage-datomic-password 'old-storage-datomic-password],
           :as 'cluster-map}]),
       :column (int 1)}
      :name
      'ensure-datomic-password
      :ns
      *ns*))
  (def can-remote?
   (fn can_remote_QMARK_
     ([p__16724]
       (let [map__16725 p__16724
             map__16725 (if (seq? map__16725)
                          (if (next map__16725)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16725))
                            (if (seq map__16725) (first map__16725) {}))
                          map__16725)
             h2_init_spec map__16725
             storage_access (get map__16725 :storage-access)
             storage_datomic_password (get map__16725 :storage-datomic-password)
             storage_admin_password (get map__16725 :storage-admin-password)]
         (if (= "remote" storage_access)
           (if (and storage_datomic_password storage_admin_password)
             true
             (do
               (throw
                 (java.lang.IllegalArgumentException.
                   "You must set storage-datomic-password and storage-admin-password before enabling storage-access=remote."))
               nil))
           false)))))
  (reset-meta!
    #'can-remote?
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['storage-access 'storage-datomic-password 'storage-admin-password],
           :as 'h2-init-spec}]),
       :column (int 1)}
      :name
      'can-remote?
      :ns
      *ns*))
  (defn init-embedded
    ([spec]
      (when-not (java.lang.Class/forName "org.h2.Driver")
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'Class/forName "org.h2.Driver"))))))
      (with-open [conn (or
                         (ensure-admin-conn spec)
                         (do
                           (throw
                             (java.lang.RuntimeException.
                               "Unable to connect to embedded storage, make sure storage-admin-password is correct."))
                           nil))]
        (sql/execute-commands
          conn
          "create table if not exists datomic_kvs (id varchar primary key, rev integer, map varchar, val bytea)"
          "create user if not exists datomic password 'datomic'"
          "grant all on datomic_kvs to datomic"
          "grant all on datomic_kvs to public"))
      spec))
  (reset-meta!
    #'init-embedded
    (assoc {:arglists (clojure.core/list ['spec]), :column (int 1)} :name 'init-embedded :ns *ns*))
  (def init-tcp
   (fn init_tcp
     ([p__16732]
       (let [map__16733 p__16732
             map__16733 (if (seq? map__16733)
                          (if (next map__16733)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16733))
                            (if (seq map__16733) (first map__16733) {}))
                          map__16733)
             spec map__16733
             host (get map__16733 :host)
             h2_port (get map__16733 :h2-port)
             data_dir (get map__16733 :data-dir)]
         (init-embedded spec)
         (let [conn (ensure-datomic-password spec)]
           (if conn
             (.close ^java.lang.AutoCloseable conn)
             (throw (java.lang.RuntimeException. "Incorrect storage-datomic-password"))))
         (let [server (Server/createTcpServer
                        (into-array
                          (concat
                            (when (can-remote? spec) ["-tcpAllowOthers"])
                            ["-ifExists"
                             "-properties"
                             ""
                             "-tcpPort"
                             (str h2_port)
                             "-baseDir"
                             data_dir])))]
           (.start ^org.h2.tools.Server server)
           (assoc spec :server server))))))
  (reset-meta!
    #'init-tcp
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'h2-port 'data-dir], :as 'spec}]),
       :column (int 1)}
      :name
      'init-tcp
      :ns
      *ns*))
  (def shutdown
   (fn shutdown
     ([p__16735]
       (let [map__16736 p__16735
             map__16736 (if (seq? map__16736)
                          (if (next map__16736)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16736))
                            (if (seq map__16736) (first map__16736) {}))
                          map__16736)
             server (get map__16736 :server)]
         (.stop ^org.h2.tools.Server server)
         nil))))
  (reset-meta!
    #'shutdown
    (assoc
      {:arglists (clojure.core/list [{:keys [(.withMeta 'server {:tag 'Server})]}]),
       :column (int 1)}
      :name
      'shutdown
      :ns
      *ns*))
  (def local-jdbc-spec
   (fn local_jdbc_spec
     ([p__16738]
       (let [map__16739 p__16738
             map__16739 (if (seq? map__16739)
                          (if (next map__16739)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16739))
                            (if (seq map__16739) (first map__16739) {}))
                          map__16739)
             cluster_map map__16739
             data_dir (get map__16739 :data-dir)
             storage_datomic_password (get map__16739 :storage-datomic-password)]
         (when-not data_dir
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'data-dir)))))
         (create-sql-spec
           {:sql-url (str "jdbc:h2:" data_dir "/datomic"),
            :username "datomic",
            :password (or storage_datomic_password "datomic")})))))
  (reset-meta!
    #'local-jdbc-spec
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['data-dir 'storage-datomic-password], :as 'cluster-map}]),
       :column (int 1)}
      :name
      'local-jdbc-spec
      :ns
      *ns*))
  (def remote-jdbc-spec
   (fn remote_jdbc_spec
     ([p__16742]
       (let [map__16743 p__16742
             map__16743 (if (seq? map__16743)
                          (if (next map__16743)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16743))
                            (if (seq map__16743) (first map__16743) {}))
                          map__16743)
             host (get map__16743 :host)
             h2_port (get map__16743 :h2-port)
             password (get map__16743 :password)]
         (when-not (and host h2_port password)
           (throw
             (java.lang.AssertionError.
               (str
                 "Assert failed: "
                 (pr-str (clojure.core/list 'and 'host 'h2-port 'password))))))
         (create-sql-spec
           {:sql-url (str "jdbc:h2:tcp://" host ":" h2_port "/datomic"),
            :username "datomic",
            :password password})))))
  (reset-meta!
    #'remote-jdbc-spec
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'h2-port 'password]}]), :column (int 1)}
      :name
      'remote-jdbc-spec
      :ns
      *ns*)))
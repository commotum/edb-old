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
      (fn fn__31530
        ([p__31529]
          (let [map__31531 p__31529
                map__31531 (if (seq? map__31531)
                             (if (next map__31531)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__31531))
                               (if (seq map__31531) (first map__31531) {}))
                             map__31531)
                args map__31531
                sql_url (get map__31531 :sql-url)
                username (get map__31531 :username)
                password (get map__31531 :password)]
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
  (defn create-sql-spec
    ([cluster_map] (locking driver-manager-lock (create-sql-spec* cluster_map))))
  (reset-meta!
    #'create-sql-spec
    (assoc
      {:arglists (clojure.core/list ['cluster-map]), :column (int 1)}
      :name
      'create-sql-spec
      :ns
      *ns*))
  (defn sql-url ([data_dir] (str "jdbc:h2:" data_dir "/datomic")))
  (reset-meta!
    #'sql-url
    (assoc {:arglists (clojure.core/list ['data-dir]), :column (int 1)} :name 'sql-url :ns *ns*))
  (defn try-connect
    ([p__31540]
      (let [map__31541 p__31540
            map__31541 (if (seq? map__31541)
                         (if (next map__31541)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31541))
                           (if (seq map__31541) (first map__31541) {}))
                         map__31541)
            data_dir (get map__31541 :data-dir)
            username (get map__31541 :username)
            password (get map__31541 :password)]
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
              nil))))))
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
  (defn rename-user-cmd
    ([p__31546]
      (let [map__31547 p__31546
            map__31547 (if (seq? map__31547)
                         (if (next map__31547)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31547))
                           (if (seq map__31547) (first map__31547) {}))
                         map__31547)
            old_user (get map__31547 :old-user)
            user (get map__31547 :user)]
        (when (and (and old_user user) (not= old_user user))
          (format "alter user %s rename to %s" (uq old_user) (uq user))))))
  (reset-meta!
    #'rename-user-cmd
    (assoc
      {:arglists (clojure.core/list [{:keys ['old-user 'user]}]), :column (int 1)}
      :name
      'rename-user-cmd
      :ns
      *ns*))
  (defn set-password-cmd
    ([p__31551]
      (let [map__31552 p__31551
            map__31552 (if (seq? map__31552)
                         (if (next map__31552)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31552))
                           (if (seq map__31552) (first map__31552) {}))
                         map__31552)
            user (get map__31552 :user)
            password (get map__31552 :password)]
        (when (and user password) (format "alter user %s set password '%s'" (uq user) password)))))
  (reset-meta!
    #'set-password-cmd
    (assoc
      {:arglists (clojure.core/list [{:keys ['user 'password]}]), :column (int 1)}
      :name
      'set-password-cmd
      :ns
      *ns*))
  (defn updating-connect
    ([data_dir p__31555]
      (let [map__31556 p__31555
            map__31556 (if (seq? map__31556)
                         (if (next map__31556)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31556))
                           (if (seq map__31556) (first map__31556) {}))
                         map__31556)
            args map__31556
            user (get map__31556 :user)
            password (get map__31556 :password)
            old_user (get map__31556 :old-user)
            old_password (get map__31556 :old-password)]
        (when (and old_user user old_password password)
          (let [temp__5825__auto__ (try-connect
                                     {:data-dir data_dir,
                                      :username old_user,
                                      :password old_password})]
            (when temp__5825__auto__
              (let [conn temp__5825__auto__]
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
                conn)))))))
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
  (defn ensure-admin-conn
    ([p__31562]
      (let [map__31563 p__31562
            map__31563 (if (seq? map__31563)
                         (if (next map__31563)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31563))
                           (if (seq map__31563) (first map__31563) {}))
                         map__31563)
            cluster_map map__31563
            data_dir (get map__31563 :data-dir)
            storage_admin_password (get map__31563 :storage-admin-password)
            old_storage_admin_password (get map__31563 :old-storage-admin-password)]
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
            {:old-user "", :old-password "", :user "", :password storage_admin_password})))))
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
  (defn ensure-datomic-password
    ([p__31567]
      (let [map__31568 p__31567
            map__31568 (if (seq? map__31568)
                         (if (next map__31568)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31568))
                           (if (seq map__31568) (first map__31568) {}))
                         map__31568)
            cluster_map map__31568
            data_dir (get map__31568 :data-dir)
            storage_datomic_password (get map__31568 :storage-datomic-password)
            old_storage_datomic_password (get map__31568 :old-storage-datomic-password)]
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
             :password storage_datomic_password})))))
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
  (defn can-remote?
    ([p__31572]
      (let [map__31573 p__31572
            map__31573 (if (seq? map__31573)
                         (if (next map__31573)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31573))
                           (if (seq map__31573) (first map__31573) {}))
                         map__31573)
            h2_init_spec map__31573
            storage_access (get map__31573 :storage-access)
            storage_datomic_password (get map__31573 :storage-datomic-password)
            storage_admin_password (get map__31573 :storage-admin-password)]
        (if (= "remote" storage_access)
          (if (and storage_datomic_password storage_admin_password)
            true
            (do
              (throw
                (java.lang.IllegalArgumentException.
                  "You must set storage-datomic-password and storage-admin-password before enabling storage-access=remote."))
              nil))
          false))))
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
  (defn init-tcp
    ([p__31580]
      (let [map__31581 p__31580
            map__31581 (if (seq? map__31581)
                         (if (next map__31581)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31581))
                           (if (seq map__31581) (first map__31581) {}))
                         map__31581)
            spec map__31581
            host (get map__31581 :host)
            h2_port (get map__31581 :h2-port)
            data_dir (get map__31581 :data-dir)]
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
          (assoc spec :server server)))))
  (reset-meta!
    #'init-tcp
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'h2-port 'data-dir], :as 'spec}]),
       :column (int 1)}
      :name
      'init-tcp
      :ns
      *ns*))
  (defn shutdown
    ([p__31583]
      (let [map__31584 p__31583
            map__31584 (if (seq? map__31584)
                         (if (next map__31584)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31584))
                           (if (seq map__31584) (first map__31584) {}))
                         map__31584)
            server (get map__31584 :server)]
        (.stop ^org.h2.tools.Server server)
        nil)))
  (reset-meta!
    #'shutdown
    (assoc
      {:arglists (clojure.core/list [{:keys [(.withMeta 'server {:tag 'Server})]}]),
       :column (int 1)}
      :name
      'shutdown
      :ns
      *ns*))
  (defn local-jdbc-spec
    ([p__31586]
      (let [map__31587 p__31586
            map__31587 (if (seq? map__31587)
                         (if (next map__31587)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31587))
                           (if (seq map__31587) (first map__31587) {}))
                         map__31587)
            cluster_map map__31587
            data_dir (get map__31587 :data-dir)
            storage_datomic_password (get map__31587 :storage-datomic-password)]
        (when-not data_dir
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'data-dir)))))
        (create-sql-spec
          {:sql-url (str "jdbc:h2:" data_dir "/datomic"),
           :username "datomic",
           :password (or storage_datomic_password "datomic")}))))
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
  (defn remote-jdbc-spec
    ([p__31590]
      (let [map__31591 p__31590
            map__31591 (if (seq? map__31591)
                         (if (next map__31591)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__31591))
                           (if (seq map__31591) (first map__31591) {}))
                         map__31591)
            host (get map__31591 :host)
            h2_port (get map__31591 :h2-port)
            password (get map__31591 :password)]
        (when-not (and host h2_port password)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'and 'host 'h2-port 'password))))))
        (create-sql-spec
          {:sql-url (str "jdbc:h2:tcp://" host ":" h2_port "/datomic"),
           :username "datomic",
           :password password}))))
  (reset-meta!
    #'remote-jdbc-spec
    (assoc
      {:arglists (clojure.core/list [{:keys ['host 'h2-port 'password]}]), :column (int 1)}
      :name
      'remote-jdbc-spec
      :ns
      *ns*)))
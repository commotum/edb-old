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
  (def driver-manager-lock (java.lang.Object.))
  (reset-meta!
    #'driver-manager-lock
    (assoc {:private true, :column 1} :name 'driver-manager-lock :ns *ns*))
  (def create-sql-spec*
   (memoize
     (fn fn__11592
       ([p__11591]
         (let [map__11593 p__11591
               map__11593 (if (seq? map__11593)
                            (clojure.lang.PersistentHashMap/create (seq map__11593))
                            map__11593)
               args map__11593
               sql_url (get map__11593 :sql-url)
               username (get map__11593 :username)
               password (get map__11593 :password)]
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
  (defn sql-url ([data_dir] (str "jdbc:h2:" data_dir "/datomic")))
  (defn try-connect
    ([p__11601]
      (let [map__11602 p__11601
            map__11602 (if (seq? map__11602)
                         (clojure.lang.PersistentHashMap/create (seq map__11602))
                         map__11602)
            data_dir (get map__11602 :data-dir)
            username (get map__11602 :username)
            password (get map__11602 :password)]
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
  (defn uq ([username] (if (= "" username) "\"\"" username)))
  (defn rename-user-cmd
    ([p__11607]
      (let [map__11608 p__11607
            map__11608 (if (seq? map__11608)
                         (clojure.lang.PersistentHashMap/create (seq map__11608))
                         map__11608)
            old_user (get map__11608 :old-user)
            user (get map__11608 :user)]
        (when (and (and old_user user) (not= old_user user))
          (format "alter user %s rename to %s" (uq old_user) (uq user))))))
  (defn set-password-cmd
    ([p__11612]
      (let [map__11613 p__11612
            map__11613 (if (seq? map__11613)
                         (clojure.lang.PersistentHashMap/create (seq map__11613))
                         map__11613)
            user (get map__11613 :user)
            password (get map__11613 :password)]
        (when (and user password) (format "alter user %s set password '%s'" (uq user) password)))))
  (defn updating-connect
    ([data_dir p__11616]
      (let [map__11617 p__11616
            map__11617 (if (seq? map__11617)
                         (clojure.lang.PersistentHashMap/create (seq map__11617))
                         map__11617)
            args map__11617
            user (get map__11617 :user)
            password (get map__11617 :password)
            old_user (get map__11617 :old-user)
            old_password (get map__11617 :old-password)]
        (when (and old_user user old_password password)
          (let [temp__5457__auto__ (try-connect
                                     {:data-dir data_dir,
                                      :username old_user,
                                      :password old_password})]
            (when temp__5457__auto__
              (let [conn temp__5457__auto__]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.h2")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process {:event :storage/update-password, :user user}))
                    nil)
                  nil)
                (apply
                  sql/execute-commands
                  conn
                  (filter identity [(rename-user-cmd args) (set-password-cmd args)]))
                conn)))))))
  (defn ensure-admin-conn
    ([p__11623]
      (let [map__11624 p__11623
            map__11624 (if (seq? map__11624)
                         (clojure.lang.PersistentHashMap/create (seq map__11624))
                         map__11624)
            cluster_map map__11624
            data_dir (get map__11624 :data-dir)
            storage_admin_password (get map__11624 :storage-admin-password)
            old_storage_admin_password (get map__11624 :old-storage-admin-password)]
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
  (defn ensure-datomic-password
    ([p__11628]
      (let [map__11629 p__11628
            map__11629 (if (seq? map__11629)
                         (clojure.lang.PersistentHashMap/create (seq map__11629))
                         map__11629)
            cluster_map map__11629
            data_dir (get map__11629 :data-dir)
            storage_datomic_password (get map__11629 :storage-datomic-password)
            old_storage_datomic_password (get map__11629 :old-storage-datomic-password)]
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
  (defn can-remote?
    ([p__11633]
      (let [map__11634 p__11633
            map__11634 (if (seq? map__11634)
                         (clojure.lang.PersistentHashMap/create (seq map__11634))
                         map__11634)
            h2_init_spec map__11634
            storage_access (get map__11634 :storage-access)
            storage_datomic_password (get map__11634 :storage-datomic-password)
            storage_admin_password (get map__11634 :storage-admin-password)]
        (if (= "remote" storage_access)
          (if (and storage_datomic_password storage_admin_password)
            true
            (do
              (throw
                (java.lang.IllegalArgumentException.
                  "You must set storage-datomic-password and storage-admin-password before enabling storage-access=remote."))
              nil))
          false))))
  (defn init-embedded
    ([spec]
      (when-not (java.lang.Class/forName "org.h2.Driver")
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'Class/forName "org.h2.Driver"))))))
      (let [conn (or
                   (ensure-admin-conn spec)
                   (do
                     (throw
                       (java.lang.RuntimeException.
                         "Unable to connect to embedded storage, make sure storage-admin-password is correct."))
                     nil))]
        (try
          (sql/execute-commands
            conn
            "create table if not exists datomic_kvs (id varchar primary key, rev integer, map varchar, val bytea)"
            "create user if not exists datomic password 'datomic'"
            "grant all on datomic_kvs to datomic"
            "grant all on datomic_kvs to public")
          (finally (do (.close ^java.lang.AutoCloseable conn) nil))))
      spec))
  (defn init-tcp
    ([p__11641]
      (let [map__11642 p__11641
            map__11642 (if (seq? map__11642)
                         (clojure.lang.PersistentHashMap/create (seq map__11642))
                         map__11642)
            spec map__11642
            host (get map__11642 :host)
            h2_port (get map__11642 :h2-port)
            data_dir (get map__11642 :data-dir)]
        (init-embedded spec)
        (let [conn (ensure-datomic-password spec)]
          (if conn
            (do (.close ^java.lang.AutoCloseable conn) nil)
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
  (defn shutdown
    ([p__11644]
      (let [map__11645 p__11644
            map__11645 (if (seq? map__11645)
                         (clojure.lang.PersistentHashMap/create (seq map__11645))
                         map__11645)
            server (get map__11645 :server)]
        (.stop ^org.h2.tools.Server server)
        nil)))
  (defn local-jdbc-spec
    ([p__11647]
      (let [map__11648 p__11647
            map__11648 (if (seq? map__11648)
                         (clojure.lang.PersistentHashMap/create (seq map__11648))
                         map__11648)
            cluster_map map__11648
            data_dir (get map__11648 :data-dir)
            storage_datomic_password (get map__11648 :storage-datomic-password)]
        (when-not data_dir
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'data-dir)))))
        (create-sql-spec
          {:sql-url (str "jdbc:h2:" data_dir "/datomic"),
           :username "datomic",
           :password (or storage_datomic_password "datomic")}))))
  (defn remote-jdbc-spec
    ([p__11651]
      (let [map__11652 p__11651
            map__11652 (if (seq? map__11652)
                         (clojure.lang.PersistentHashMap/create (seq map__11652))
                         map__11652)
            host (get map__11652 :host)
            h2_port (get map__11652 :h2-port)
            password (get map__11652 :password)]
        (when-not (and host h2_port password)
          (throw
            (java.lang.AssertionError.
              (str "Assert failed: " (pr-str (clojure.core/list 'and 'host 'h2-port 'password))))))
        (create-sql-spec
          {:sql-url (str "jdbc:h2:tcp://" host ":" h2_port "/datomic"),
           :username "datomic",
           :password password})))))
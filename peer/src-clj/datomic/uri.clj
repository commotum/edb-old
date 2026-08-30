(do
  (clojure.core/in-ns 'datomic.uri)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['clojure.edn :as 'edn]
        ['datomic.common :as 'common]
        ['datomic.error :as 'error])
      (clojure.core/import 'java.net.URI)))
  (when-not (.equals 'datomic.uri 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.uri))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.string :as 'str]
          ['clojure.edn :as 'edn]
          ['datomic.common :as 'common]
          ['datomic.error :as 'error])
        (clojure.core/import 'java.net.URI))))
  (defn parse-query-string
    ([q]
      (into
        {}
        (filter
          (fn fn__16826 ([p1__16825#] (= 2 (long (count p1__16825#)))))
          (map
            (fn fn__16828 ([p1__16824#] (str/split p1__16824# #"=")))
            (str/split (or q "") #"&"))))))
  (defn read-port ([portstr] (let [port (edn/read-string portstr)] (when (integer? port) port))))
  (defn storage-protocol
    ([uri]
      (if (instance? java.util.Map uri)
        (keyword (or (get uri :protocol) (get uri "protocol")))
        (keyword
          (nth
            (.split (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)) ":")
            (int 0))))))
  (defmulti parse* storage-protocol)
  (defn fixup-uri-map
    ([m]
      (let [ret (common/force-map-keywords (into {} m))]
        (assoc ret :protocol (keyword (:protocol ret))))))
  (defn param-map
    ([query]
      (when query
        (into
          {}
          (map
            (fn fn__16842
              ([p__16841]
                (let [vec__16843 p__16841
                      k (nth vec__16843 (int 0) nil)
                      v (nth vec__16843 (int 1) nil)]
                  [(keyword (str/replace k #"_" "-")) v])))
            (map
              (fn fn__16847 ([p1__16840#] (str/split p1__16840# #"=")))
              (str/split (or query "") #"&")))))))
  (defmethod
    parse*
    :ddb
    fn__16851
    ([uri]
      (let [sub_uri (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            paths (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))
            region (.getHost ^java.net.URI sub_uri)]
        {:protocol :ddb,
         :region region,
         :system-root (first paths),
         :db-name (second paths),
         :params {:ddb params}})))
  (defmethod
    parse*
    :ddb-local
    fn__16853
    ([uri]
      (let [sub_uri (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            paths (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))
            endpoint (.getHost ^java.net.URI sub_uri)
            port (.getPort ^java.net.URI sub_uri)]
        {:protocol :ddb-local,
         :override-endpoint (str endpoint ":" (java.lang.Integer/valueOf (int port))),
         :system-root (first paths),
         :db-name (second paths),
         :params {:ddb params}})))
  (defn mapify-ddb+s3-uri
    ([uri]
      (let [sub_uri (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            paths (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))
            region (.getHost ^java.net.URI sub_uri)]
        (merge
          {:protocol :ddb+s3,
           :aws-region region,
           :aws-dynamodb-table (first paths),
           :db-name (last paths)}
          (when (> (count paths) 2) {:system (second paths)})
          params))))
  (reset-meta!
    #'mapify-ddb+s3-uri
    (assoc
      {:private true, :arglists (clojure.core/list ['uri]), :column 1}
      :name
      'mapify-ddb+s3-uri
      :ns
      *ns*))
  (defmethod
    parse*
    :ddb+s3
    fn__16856
    ([uri]
      (let [map__16857 (if (instance? java.util.Map uri)
                         (fixup-uri-map uri)
                         (mapify-ddb+s3-uri uri))
            map__16857 (if (seq? map__16857)
                         (clojure.lang.PersistentHashMap/create (seq map__16857))
                         map__16857)
            m map__16857
            aws_dynamodb_table (get map__16857 :aws-dynamodb-table)
            system (get map__16857 :system)
            system (or system "_default")]
        (merge m {:system system, :system-root (str aws_dynamodb_table "/" system)}))))
  (defmethod
    parse*
    :s3
    fn__16860
    ([uri]
      (let [sub_uri (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            paths (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))]
        (merge
          {:protocol :s3,
           :system-root (.getHost ^java.net.URI sub_uri),
           :aws-s3-path (apply str (interpose "/" (butlast paths))),
           :db-name (last paths)}
          params))))
  (defmethod
    parse*
    :couchbase
    fn__16862
    ([uri]
      (let [sub_uri (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            vec__16863 (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))
            bucket (nth vec__16863 (int 0) nil)
            dbname (nth vec__16863 (int 1) nil)
            host (.getHost ^java.net.URI sub_uri)]
        (merge {:protocol :couchbase, :host host, :bucket bucket, :db-name dbname} params))))
  (defmethod
    parse*
    :inf
    fn__16867
    ([uri]
      (let [vec__16868 (str/split (str/replace uri #"^datomic:inf://" "") #"/")
            hp (nth vec__16868 (int 0) nil)
            db_name (nth vec__16868 (int 1) nil)
            vec__16871 (str/split hp #":")
            host (nth vec__16871 (int 0) nil)
            port (nth vec__16871 (int 1) nil)]
        {:protocol :inf,
         :system-root hp,
         :db-name db_name,
         :host host,
         :port
         (and
           port
           (java.lang.Integer/valueOf
             (int (java.lang.Integer/parseInt ^java.lang.String port))))})))
  (defmethod
    parse*
    :cass
    fn__16876
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass://" "")
              vec__16877 (str/split path_query #"\?")
              path (nth vec__16877 (int 0) nil)
              query (nth vec__16877 (int 1) nil)
              vec__16880 (str/split path #"/")
              host_port (nth vec__16880 (int 0) nil)
              table (nth vec__16880 (int 1) nil)
              db_name (nth vec__16880 (int 2) nil)
              vec__16883 (str/split host_port #":")
              host (nth vec__16883 (int 0) nil)
              port (nth vec__16883 (int 1) nil)
              params (param-map query)]
          (merge
            {:protocol :cass,
             :system-root (str host_port "/" table),
             :host host,
             :port
             (and
               port
               (java.lang.Integer/valueOf
                 (int (java.lang.Integer/parseInt ^java.lang.String port)))),
             :table table,
             :db-name db_name}
            params)))))
  (defmethod
    parse*
    :cass2
    fn__16888
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass2://" "")
              vec__16889 (str/split path_query #"\?")
              path (nth vec__16889 (int 0) nil)
              query (nth vec__16889 (int 1) nil)
              vec__16892 (str/split path #"/")
              host_port (nth vec__16892 (int 0) nil)
              table (nth vec__16892 (int 1) nil)
              db_name (nth vec__16892 (int 2) nil)
              vec__16895 (str/split host_port #":")
              host (nth vec__16895 (int 0) nil)
              port (nth vec__16895 (int 1) nil)
              params (param-map query)]
          (merge
            {:protocol :cass2,
             :system-root (str host_port "/" table),
             :host host,
             :port
             (and
               port
               (java.lang.Integer/valueOf
                 (int (java.lang.Integer/parseInt ^java.lang.String port)))),
             :table table,
             :db-name db_name}
            params)))))
  (defmethod
    parse*
    :cass3
    fn__16900
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass3://" "")
              vec__16901 (str/split path_query #"\?")
              path (nth vec__16901 (int 0) nil)
              query (nth vec__16901 (int 1) nil)
              vec__16904 (str/split path #"/")
              host_port (nth vec__16904 (int 0) nil)
              table (nth vec__16904 (int 1) nil)
              db_name (nth vec__16904 (int 2) nil)
              vec__16907 (str/split host_port #":")
              host (nth vec__16907 (int 0) nil)
              port (nth vec__16907 (int 1) nil)
              params (param-map query)]
          (merge
            {:protocol :cass3,
             :system-root (str host_port "/" table),
             :host host,
             :port
             (and
               port
               (java.lang.Integer/valueOf
                 (int (java.lang.Integer/parseInt ^java.lang.String port)))),
             :table table,
             :db-name db_name}
            params)))))
  (defmethod
    parse*
    :sql
    fn__16912
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [suffix (str/replace uri #"datomic:sql://" "")
              vec__16913 (str/split suffix #"[?]")
              seq__16914 (seq vec__16913)
              first__16915 (first seq__16914)
              seq__16914 (next seq__16914)
              db_name first__16915
              args seq__16914
              sql_url (when args
                        (subs suffix (long (inc (.indexOf ^java.lang.String suffix "?")))))]
          {:protocol :sql, :system-root sql_url, :db-name db_name, :sql-url sql_url}))))
  (defn parse-h2
    ([uri]
      (let [vec__16917 (str/split
                         (str/replace (str/replace uri #"^datomic:[^:]+://" "") #"\?.*" "")
                         #"/")
            hp (nth vec__16917 (int 0) nil)
            db_name (nth vec__16917 (int 1) nil)
            vec__16920 (str/split hp #":")
            host (nth vec__16920 (int 0) nil)
            portstr (nth vec__16920 (int 1) nil)
            params (param-map
                     (.getQuery
                       (java.net.URI.
                         (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))))
            temp__5457__auto__ (read-port portstr)]
        (when temp__5457__auto__
          (let [port temp__5457__auto__]
            {:protocol (storage-protocol uri),
             :system-root hp,
             :db-name db_name,
             :host host,
             :port port,
             :password (or (:password params) "datomic"),
             :h2-port (or (read-port (:h2-port params)) (inc port))})))))
  (defmethod parse* :limited-edition fn__16927 ([uri] (parse-h2 uri)))
  (defmethod parse* :dev fn__16929 ([uri] (parse-h2 uri)))
  (defmethod
    parse*
    :mdev
    fn__16931
    ([uri]
      (let [vec__16932 (str/split (str/replace uri #"^datomic:mdev://" "") #"/")
            hp (nth vec__16932 (int 0) nil)
            db_name (nth vec__16932 (int 1) nil)
            vec__16935 (str/split hp #":")
            host (nth vec__16935 (int 0) nil)
            port (nth vec__16935 (int 1) nil)]
        {:protocol :mdev,
         :system-root hp,
         :db-name db_name,
         :host host,
         :port
         (and
           port
           (java.lang.Integer/valueOf
             (int (java.lang.Integer/parseInt ^java.lang.String port))))})))
  (defmethod
    parse*
    :mem
    fn__16940
    ([uri]
      (let [db_name (str/replace uri #"^datomic:mem://" "")]
        {:protocol :mem, :system-root "local", :db-name db_name})))
  (defmethod
    parse*
    :olddev
    fn__16942
    ([uri]
      (let [vec__16943 (str/split (str/replace uri #"^datomic:olddev://" "") #"/")
            hp (nth vec__16943 (int 0) nil)
            db_name (nth vec__16943 (int 1) nil)
            vec__16946 (str/split hp #":")
            host (nth vec__16946 (int 0) nil)
            port (nth vec__16946 (int 1) nil)]
        {:protocol :olddev,
         :system-root hp,
         :db-name db_name,
         :params
         {:ip
          {:host host,
           :port
           (java.lang.Integer/valueOf
             (int (java.lang.Integer/parseInt ^java.lang.String port)))}}})))
  (defmethod parse* :default fn__16950 ([uri] nil))
  (defn parse ([uri] (assoc (parse* uri) :uri uri)))
  (defn parse-db
    ([uri]
      (let [cluster_conf (parse uri)]
        (if (:db-name cluster_conf)
          cluster_conf
          (error/arg :db.error/invalid-db-uri (str "Invalid database URI " uri))))))
  (defn remove-query-string ([s] (str/replace s #"\?.*" "")))
  (reset-meta!
    #'remove-query-string
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column 1}
      :name
      'remove-query-string
      :ns
      *ns*))
  (defn loggable-cluster-conf
    ([cluster_conf]
      (let [m (select-keys
                cluster_conf
                [:protocol :db-name :system-root :host :port :bucket :db-id])]
        (cond-> m (:system-root m) (update :system-root remove-query-string)))))
  (defmulti create (fn fn__16958 ([cluster_conf] (:protocol cluster_conf))))
  (defn query-key ([s] (str/replace (name s) "-" "_")))
  (defn query-args
    ([m]
      (str/join
        "&"
        (map
          (fn fn__16965
            ([p__16964]
              (let [vec__16966 p__16964
                    k (nth vec__16966 (int 0) nil)
                    v (nth vec__16966 (int 1) nil)]
                (str (query-key k) "=" v))))
          m))))
  (defmethod
    create
    :ddb
    fn__16971
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (str
          "datomic:ddb://"
          (common/getx cluster_conf :region)
          "/"
          system_root
          (let [temp__5457__auto__ (:db-name cluster_conf)]
            (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name))))
          (let [temp__5457__auto__ (get-in cluster_conf [:params :ddb])]
            (when temp__5457__auto__
              (let [ddb_params temp__5457__auto__] (str "?" (query-args ddb_params)))))))))
  (defmethod
    create
    :ddb+s3
    fn__16975
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            root_or_table (if (= (common/getx cluster_conf :system) "_default")
                            (common/getx cluster_conf :aws-dynamodb-table)
                            system_root)
            map__16976 cluster_conf
            map__16976 (if (seq? map__16976)
                         (clojure.lang.PersistentHashMap/create (seq map__16976))
                         map__16976)
            db_name (get map__16976 :db-name)
            params (select-keys cluster_conf [:skip-efs])]
        (str
          "datomic:ddb+s3://"
          (common/getx cluster_conf :aws-region)
          "/"
          root_or_table
          (when db_name (str "/" db_name))
          (when-not (empty? params)
            (str
              "?"
              (str/join
                "&"
                (map
                  (fn fn__16978
                    ([p__16977]
                      (let [vec__16979 p__16977
                            k (nth vec__16979 (int 0) nil)
                            v (nth vec__16979 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass
    fn__16984
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            table (str/trim (common/getx cluster_conf :table))
            map__16985 cluster_conf
            map__16985 (if (seq? map__16985)
                         (clojure.lang.PersistentHashMap/create (seq map__16985))
                         map__16985)
            db_name (get map__16985 :db-name)
            params (select-keys cluster_conf [:user :password :ssl])]
        (str
          "datomic:cass://"
          system_root
          (when db_name (str "/" db_name))
          (when-not (empty? params)
            (str
              "?"
              (str/join
                "&"
                (map
                  (fn fn__16987
                    ([p__16986]
                      (let [vec__16988 p__16986
                            k (nth vec__16988 (int 0) nil)
                            v (nth vec__16988 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass2
    fn__16993
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            table (str/trim (common/getx cluster_conf :table))
            map__16994 cluster_conf
            map__16994 (if (seq? map__16994)
                         (clojure.lang.PersistentHashMap/create (seq map__16994))
                         map__16994)
            db_name (get map__16994 :db-name)
            params (select-keys cluster_conf [:user :password :ssl])]
        (str
          "datomic:cass2://"
          system_root
          (when db_name (str "/" db_name))
          (when-not (empty? params)
            (str
              "?"
              (str/join
                "&"
                (map
                  (fn fn__16996
                    ([p__16995]
                      (let [vec__16997 p__16995
                            k (nth vec__16997 (int 0) nil)
                            v (nth vec__16997 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass3
    fn__17002
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            table (str/trim (common/getx cluster_conf :table))
            map__17003 cluster_conf
            map__17003 (if (seq? map__17003)
                         (clojure.lang.PersistentHashMap/create (seq map__17003))
                         map__17003)
            db_name (get map__17003 :db-name)
            params (select-keys cluster_conf [:user :password :ssl :local-datacenter])]
        (str
          "datomic:cass3://"
          system_root
          (when db_name (str "/" db_name))
          (when-not (empty? params)
            (str
              "?"
              (str/join
                "&"
                (map
                  (fn fn__17005
                    ([p__17004]
                      (let [vec__17006 p__17004
                            k (nth vec__17006 (int 0) nil)
                            v (nth vec__17006 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :ddb-local
    fn__17011
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__17012 cluster_conf
            map__17012 (if (seq? map__17012)
                         (clojure.lang.PersistentHashMap/create (seq map__17012))
                         map__17012)
            override_endpoint (get map__17012 :override-endpoint)]
        (str
          "datomic:ddb-local://"
          override_endpoint
          "/"
          system_root
          (let [temp__5457__auto__ (:db-name cluster_conf)]
            (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name))))
          (let [temp__5457__auto__ (get-in cluster_conf [:params :ddb-local])]
            (when temp__5457__auto__
              (let [ddb_params temp__5457__auto__] (str "?" (query-args ddb_params)))))))))
  (defmethod
    create
    :s3
    fn__17016
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (str
          "datomic:s3://"
          system_root
          "/"
          (:aws-s3-path cluster_conf)
          (let [temp__5457__auto__ (:db-name cluster_conf)]
            (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name))))
          (let [temp__5457__auto__ (select-keys cluster_conf [:aws-access-key-id :aws-secret-key])]
            (when temp__5457__auto__
              (let [params temp__5457__auto__] (str "?" (query-args params)))))))))
  (defmethod
    create
    :couchbase
    fn__17020
    ([cluster_conf]
      (let [map__17021 cluster_conf
            map__17021 (if (seq? map__17021)
                         (clojure.lang.PersistentHashMap/create (seq map__17021))
                         map__17021)
            host (get map__17021 :host)
            bucket (get map__17021 :bucket)
            password (get map__17021 :password)
            db_name (get map__17021 :db-name)]
        (str
          "datomic:couchbase://"
          host
          "/"
          bucket
          (when db_name (str "/" db_name))
          (when password (str "?password=" password))))))
  (defmethod
    create
    :olddev
    fn__17023
    ([cluster_conf]
      (str
        "datomic:olddev://"
        (common/getx cluster_conf :system-root)
        (let [temp__5457__auto__ (:db-name cluster_conf)]
          (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name))))
        (let [temp__5457__auto__ (get-in cluster_conf [:params :ddb])]
          (when temp__5457__auto__
            (let [ddb_params temp__5457__auto__] (str "?" (query-args ddb_params))))))))
  (defmethod
    create
    :inf
    fn__17027
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:inf://"
            system_root
            (let [temp__5457__auto__ (:db-name cluster_conf)]
              (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name)))))))))
  (defmethod
    create
    :sql
    fn__17031
    ([cluster_conf]
      (str
        "datomic:sql://"
        (:db-name cluster_conf)
        "?"
        (or (:sql-url cluster_conf) (:system-root cluster_conf)))))
  (defn map->query-string
    ([m]
      (str/join
        "&"
        (map
          (fn fn__17035
            ([p__17034]
              (let [vec__17036 p__17034
                    k (nth vec__17036 (int 0) nil)
                    v (nth vec__17036 (int 1) nil)]
                (str (name k) "=" v))))
          (filter second m)))))
  (defn create-h2
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:"
            (name (:protocol cluster_conf))
            "://"
            system_root
            (let [temp__5457__auto__ (:db-name cluster_conf)]
              (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name))))
            (when-not (= {:h2-port 4335} (select-keys cluster_conf [:h2-port]))
              (str "?" (map->query-string (select-keys cluster_conf [:h2-port])))))))))
  (defmethod create :dev fn__17044 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod create :limited-edition fn__17046 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod
    create
    :mdev
    fn__17048
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:mdev://"
            system_root
            (let [temp__5457__auto__ (:db-name cluster_conf)]
              (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str "/" db_name)))))))))
  (defmethod
    create
    :mem
    fn__17052
    ([cluster_conf]
      (str
        "datomic:mem://"
        (let [temp__5457__auto__ (:db-name cluster_conf)]
          (when temp__5457__auto__ (let [db_name temp__5457__auto__] (str db_name)))))))
  (defn db-uri ([uri db_name] (create (assoc (parse uri) :db-name db_name)))))
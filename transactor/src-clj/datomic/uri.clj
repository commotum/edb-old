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
          (fn fn__17972 ([p1__17971#] (= 2 (long (count p1__17971#)))))
          (map
            (fn fn__17974 ([p1__17970#] (str/split p1__17970# #"=")))
            (str/split (or q "") #"&"))))))
  (reset-meta!
    #'parse-query-string
    (assoc
      {:arglists (clojure.core/list ['q]), :column (int 1)}
      :name
      'parse-query-string
      :ns
      *ns*))
  (defn read-port ([portstr] (let [port (edn/read-string portstr)] (when (integer? port) port))))
  (reset-meta!
    #'read-port
    (assoc {:arglists (clojure.core/list ['portstr]), :column (int 1)} :name 'read-port :ns *ns*))
  (defn storage-protocol
    ([uri]
      (if (instance? java.util.Map uri)
        (keyword (or (get uri :protocol) (get uri "protocol")))
        (keyword
          (nth
            (.split (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)) ":")
            (int 0))))))
  (reset-meta!
    #'storage-protocol
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'storage-protocol
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.uri" "parse*") {:column (int 1)})
  (let [v__5792__auto__ #'parse*]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.uri" "parse*") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.uri" "parse*")
        (clojure.lang.MultiFn. "parse*" storage-protocol :default #'clojure.core/global-hierarchy))
      #'parse*))
  (defn fixup-uri-map
    ([m]
      (let [ret (common/force-map-keywords (into {} m))]
        (assoc ret :protocol (keyword (:protocol ret))))))
  (reset-meta!
    #'fixup-uri-map
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'fixup-uri-map :ns *ns*))
  (defn param-map
    ([query]
      (when query
        (into
          {}
          (map
            (fn fn__17988
              ([p__17987]
                (let [vec__17989 p__17987
                      k (nth vec__17989 (int 0) nil)
                      v (nth vec__17989 (int 1) nil)]
                  [(keyword (str/replace k #"_" "-")) v])))
            (map
              (fn fn__17993 ([p1__17986#] (str/split p1__17986# #"=")))
              (str/split (or query "") #"&")))))))
  (reset-meta!
    #'param-map
    (assoc {:arglists (clojure.core/list ['query]), :column (int 1)} :name 'param-map :ns *ns*))
  (defmethod
    parse*
    :ddb
    fn__17997
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
    fn__17999
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
      {:private true, :arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'mapify-ddb+s3-uri
      :ns
      *ns*))
  (defmethod
    parse*
    :ddb+s3
    fn__18002
    ([uri]
      (let [map__18003 (if (instance? java.util.Map uri)
                         (fixup-uri-map uri)
                         (mapify-ddb+s3-uri uri))
            map__18003 (if (seq? map__18003)
                         (if (next map__18003)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18003))
                           (if (seq map__18003) (first map__18003) {}))
                         map__18003)
            m map__18003
            aws_dynamodb_table (get map__18003 :aws-dynamodb-table)
            system (get map__18003 :system)
            system (or system "_default")]
        (merge m {:system system, :system-root (str aws_dynamodb_table "/" system)}))))
  (defmethod
    parse*
    :s3
    fn__18006
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
    fn__18008
    ([uri]
      (let [sub_uri (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            vec__18009 (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))
            bucket (nth vec__18009 (int 0) nil)
            dbname (nth vec__18009 (int 1) nil)
            host (.getHost ^java.net.URI sub_uri)]
        (merge {:protocol :couchbase, :host host, :bucket bucket, :db-name dbname} params))))
  (defmethod
    parse*
    :inf
    fn__18013
    ([uri]
      (let [vec__18014 (str/split (str/replace uri #"^datomic:inf://" "") #"/")
            hp (nth vec__18014 (int 0) nil)
            db_name (nth vec__18014 (int 1) nil)
            vec__18017 (str/split hp #":")
            host (nth vec__18017 (int 0) nil)
            port (nth vec__18017 (int 1) nil)]
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
    fn__18022
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass://" "")
              vec__18023 (str/split path_query #"\?")
              path (nth vec__18023 (int 0) nil)
              query (nth vec__18023 (int 1) nil)
              vec__18026 (str/split path #"/")
              host_port (nth vec__18026 (int 0) nil)
              table (nth vec__18026 (int 1) nil)
              db_name (nth vec__18026 (int 2) nil)
              vec__18029 (str/split host_port #":")
              host (nth vec__18029 (int 0) nil)
              port (nth vec__18029 (int 1) nil)
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
    fn__18034
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass2://" "")
              vec__18035 (str/split path_query #"\?")
              path (nth vec__18035 (int 0) nil)
              query (nth vec__18035 (int 1) nil)
              vec__18038 (str/split path #"/")
              host_port (nth vec__18038 (int 0) nil)
              table (nth vec__18038 (int 1) nil)
              db_name (nth vec__18038 (int 2) nil)
              vec__18041 (str/split host_port #":")
              host (nth vec__18041 (int 0) nil)
              port (nth vec__18041 (int 1) nil)
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
    fn__18046
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass3://" "")
              vec__18047 (str/split path_query #"\?")
              path (nth vec__18047 (int 0) nil)
              query (nth vec__18047 (int 1) nil)
              vec__18050 (str/split path #"/")
              host_port (nth vec__18050 (int 0) nil)
              table (nth vec__18050 (int 1) nil)
              db_name (nth vec__18050 (int 2) nil)
              vec__18053 (str/split host_port #":")
              host (nth vec__18053 (int 0) nil)
              port (nth vec__18053 (int 1) nil)
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
    fn__18058
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [suffix (str/replace uri #"datomic:sql://" "")
              vec__18059 (str/split suffix #"[?]")
              seq__18060 (seq vec__18059)
              first__18061 (first seq__18060)
              seq__18060 (next seq__18060)
              db_name first__18061
              args seq__18060
              sql_url (when args
                        (subs suffix (long (inc (.indexOf ^java.lang.String suffix "?")))))]
          {:protocol :sql, :system-root sql_url, :db-name db_name, :sql-url sql_url}))))
  (defn parse-h2
    ([uri]
      (let [vec__18063 (str/split
                         (str/replace (str/replace uri #"^datomic:[^:]+://" "") #"\?.*" "")
                         #"/")
            hp (nth vec__18063 (int 0) nil)
            db_name (nth vec__18063 (int 1) nil)
            vec__18066 (str/split hp #":")
            host (nth vec__18066 (int 0) nil)
            portstr (nth vec__18066 (int 1) nil)
            params (param-map
                     (.getQuery
                       (java.net.URI.
                         (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))))
            temp__5804__auto__ (read-port portstr)]
        (when temp__5804__auto__
          (let [port temp__5804__auto__]
            {:protocol (storage-protocol uri),
             :system-root hp,
             :db-name db_name,
             :host host,
             :port port,
             :password (or (:password params) "datomic"),
             :h2-port (or (read-port (:h2-port params)) (inc port))})))))
  (reset-meta!
    #'parse-h2
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'parse-h2 :ns *ns*))
  (defmethod parse* :limited-edition fn__18073 ([uri] (parse-h2 uri)))
  (defmethod parse* :dev fn__18075 ([uri] (parse-h2 uri)))
  (defmethod
    parse*
    :mdev
    fn__18077
    ([uri]
      (let [vec__18078 (str/split (str/replace uri #"^datomic:mdev://" "") #"/")
            hp (nth vec__18078 (int 0) nil)
            db_name (nth vec__18078 (int 1) nil)
            vec__18081 (str/split hp #":")
            host (nth vec__18081 (int 0) nil)
            port (nth vec__18081 (int 1) nil)]
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
    fn__18086
    ([uri]
      (let [db_name (str/replace uri #"^datomic:mem://" "")]
        {:protocol :mem, :system-root "local", :db-name db_name})))
  (defmethod
    parse*
    :olddev
    fn__18088
    ([uri]
      (let [vec__18089 (str/split (str/replace uri #"^datomic:olddev://" "") #"/")
            hp (nth vec__18089 (int 0) nil)
            db_name (nth vec__18089 (int 1) nil)
            vec__18092 (str/split hp #":")
            host (nth vec__18092 (int 0) nil)
            port (nth vec__18092 (int 1) nil)]
        {:protocol :olddev,
         :system-root hp,
         :db-name db_name,
         :params
         {:ip
          {:host host,
           :port
           (java.lang.Integer/valueOf
             (int (java.lang.Integer/parseInt ^java.lang.String port)))}}})))
  (defmethod parse* :default fn__18096 ([uri] nil))
  (defn parse ([uri] (assoc (parse* uri) :uri uri)))
  (reset-meta!
    #'parse
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'parse :ns *ns*))
  (defn parse-db
    ([uri]
      (let [cluster_conf (parse uri)]
        (if (:db-name cluster_conf)
          cluster_conf
          (error/arg :db.error/invalid-db-uri (str "Invalid database URI " uri))))))
  (reset-meta!
    #'parse-db
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'parse-db :ns *ns*))
  (defn remove-query-string ([s] (str/replace s #"\?.*" "")))
  (reset-meta!
    #'remove-query-string
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'remove-query-string
      :ns
      *ns*))
  (def loggable-cluster-conf
   (fn loggable_cluster_conf
     ([cluster_conf]
       (let [m (select-keys
                 cluster_conf
                 [:protocol :db-name :system-root :host :port :bucket :db-id])]
         (cond-> m (:system-root m) (update :system-root remove-query-string))))))
  (reset-meta!
    #'loggable-cluster-conf
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'loggable-cluster-conf
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.uri" "create") {:column (int 1)})
  (let [v__5792__auto__ #'create]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.uri" "create") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.uri" "create")
        (clojure.lang.MultiFn.
          "create"
          (fn fn__18104 ([cluster_conf] (:protocol cluster_conf)))
          :default
          #'clojure.core/global-hierarchy))
      #'create))
  (defn query-key ([s] (str/replace (name s) "-" "_")))
  (reset-meta!
    #'query-key
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'query-key :ns *ns*))
  (defn query-args
    ([m]
      (str/join
        "&"
        (map
          (fn fn__18111
            ([p__18110]
              (let [vec__18112 p__18110
                    k (nth vec__18112 (int 0) nil)
                    v (nth vec__18112 (int 1) nil)]
                (str (query-key k) "=" v))))
          m))))
  (reset-meta!
    #'query-args
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'query-args :ns *ns*))
  (defmethod
    create
    :ddb
    fn__18117
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (str
          "datomic:ddb://"
          (common/getx cluster_conf :region)
          "/"
          system_root
          (let [temp__5804__auto__ (:db-name cluster_conf)]
            (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
          (let [temp__5804__auto__ (get-in cluster_conf [:params :ddb])]
            (when temp__5804__auto__
              (let [ddb_params temp__5804__auto__] (str "?" (query-args ddb_params)))))))))
  (defmethod
    create
    :ddb+s3
    fn__18121
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            root_or_table (if (= (common/getx cluster_conf :system) "_default")
                            (common/getx cluster_conf :aws-dynamodb-table)
                            system_root)
            map__18122 cluster_conf
            map__18122 (if (seq? map__18122)
                         (if (next map__18122)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18122))
                           (if (seq map__18122) (first map__18122) {}))
                         map__18122)
            db_name (get map__18122 :db-name)
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
                  (fn fn__18124
                    ([p__18123]
                      (let [vec__18125 p__18123
                            k (nth vec__18125 (int 0) nil)
                            v (nth vec__18125 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass
    fn__18130
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            table (str/trim (common/getx cluster_conf :table))
            map__18131 cluster_conf
            map__18131 (if (seq? map__18131)
                         (if (next map__18131)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18131))
                           (if (seq map__18131) (first map__18131) {}))
                         map__18131)
            db_name (get map__18131 :db-name)
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
                  (fn fn__18133
                    ([p__18132]
                      (let [vec__18134 p__18132
                            k (nth vec__18134 (int 0) nil)
                            v (nth vec__18134 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass2
    fn__18139
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            table (str/trim (common/getx cluster_conf :table))
            map__18140 cluster_conf
            map__18140 (if (seq? map__18140)
                         (if (next map__18140)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18140))
                           (if (seq map__18140) (first map__18140) {}))
                         map__18140)
            db_name (get map__18140 :db-name)
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
                  (fn fn__18142
                    ([p__18141]
                      (let [vec__18143 p__18141
                            k (nth vec__18143 (int 0) nil)
                            v (nth vec__18143 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass3
    fn__18148
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            table (str/trim (common/getx cluster_conf :table))
            map__18149 cluster_conf
            map__18149 (if (seq? map__18149)
                         (if (next map__18149)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18149))
                           (if (seq map__18149) (first map__18149) {}))
                         map__18149)
            db_name (get map__18149 :db-name)
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
                  (fn fn__18151
                    ([p__18150]
                      (let [vec__18152 p__18150
                            k (nth vec__18152 (int 0) nil)
                            v (nth vec__18152 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :ddb-local
    fn__18157
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__18158 cluster_conf
            map__18158 (if (seq? map__18158)
                         (if (next map__18158)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18158))
                           (if (seq map__18158) (first map__18158) {}))
                         map__18158)
            override_endpoint (get map__18158 :override-endpoint)]
        (str
          "datomic:ddb-local://"
          override_endpoint
          "/"
          system_root
          (let [temp__5804__auto__ (:db-name cluster_conf)]
            (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
          (let [temp__5804__auto__ (get-in cluster_conf [:params :ddb-local])]
            (when temp__5804__auto__
              (let [ddb_params temp__5804__auto__] (str "?" (query-args ddb_params)))))))))
  (defmethod
    create
    :s3
    fn__18162
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (str
          "datomic:s3://"
          system_root
          "/"
          (:aws-s3-path cluster_conf)
          (let [temp__5804__auto__ (:db-name cluster_conf)]
            (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
          (let [temp__5804__auto__ (select-keys cluster_conf [:aws-access-key-id :aws-secret-key])]
            (when temp__5804__auto__
              (let [params temp__5804__auto__] (str "?" (query-args params)))))))))
  (defmethod
    create
    :couchbase
    fn__18166
    ([cluster_conf]
      (let [map__18167 cluster_conf
            map__18167 (if (seq? map__18167)
                         (if (next map__18167)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18167))
                           (if (seq map__18167) (first map__18167) {}))
                         map__18167)
            host (get map__18167 :host)
            bucket (get map__18167 :bucket)
            password (get map__18167 :password)
            db_name (get map__18167 :db-name)]
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
    fn__18169
    ([cluster_conf]
      (str
        "datomic:olddev://"
        (common/getx cluster_conf :system-root)
        (let [temp__5804__auto__ (:db-name cluster_conf)]
          (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
        (let [temp__5804__auto__ (get-in cluster_conf [:params :ddb])]
          (when temp__5804__auto__
            (let [ddb_params temp__5804__auto__] (str "?" (query-args ddb_params))))))))
  (defmethod
    create
    :inf
    fn__18173
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:inf://"
            system_root
            (let [temp__5804__auto__ (:db-name cluster_conf)]
              (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name)))))))))
  (defmethod
    create
    :sql
    fn__18177
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
          (fn fn__18181
            ([p__18180]
              (let [vec__18182 p__18180
                    k (nth vec__18182 (int 0) nil)
                    v (nth vec__18182 (int 1) nil)]
                (str (name k) "=" v))))
          (filter second m)))))
  (reset-meta!
    #'map->query-string
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'map->query-string
      :ns
      *ns*))
  (def create-h2
   (fn create_h2
     ([cluster_conf]
       (let [system_root (str/trim (common/getx cluster_conf :system-root))]
         (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
           (str
             "datomic:"
             (name (:protocol cluster_conf))
             "://"
             system_root
             (let [temp__5804__auto__ (:db-name cluster_conf)]
               (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
             (when-not (= {:h2-port 4335} (select-keys cluster_conf [:h2-port]))
               (str "?" (map->query-string (select-keys cluster_conf [:h2-port]))))))))))
  (reset-meta!
    #'create-h2
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-h2
      :ns
      *ns*))
  (defmethod create :dev fn__18190 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod create :limited-edition fn__18192 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod
    create
    :mdev
    fn__18194
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:mdev://"
            system_root
            (let [temp__5804__auto__ (:db-name cluster_conf)]
              (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name)))))))))
  (defmethod
    create
    :mem
    fn__18198
    ([cluster_conf]
      (str
        "datomic:mem://"
        (let [temp__5804__auto__ (:db-name cluster_conf)]
          (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str db_name)))))))
  (def db-uri (fn db_uri ([uri db_name] (create (assoc (parse uri) :db-name db_name)))))
  (reset-meta!
    #'db-uri
    (assoc
      {:arglists (clojure.core/list ['uri 'db-name]), :column (int 1)}
      :name
      'db-uri
      :ns
      *ns*)))
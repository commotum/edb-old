(do
  (clojure.core/in-ns 'datomic.uri)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.uri)
    {:doc
     "Parses and constructs Datomic database and storage URIs. String and map forms are normalized into cluster configuration maps, including storage-specific parameters and read-only connection options."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.string :as 'str]
        ['clojure.edn :as 'edn]
        ['datomic.common :as 'common]
        ['datomic.slf4j :as 'logger]
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
          ['datomic.slf4j :as 'logger]
          ['datomic.error :as 'error])
        (clojure.core/import 'java.net.URI))))
  (defn parse-query-string
    ([q]
      (into
        {}
        (filter
          (fn fn__18088 ([p1__18087#] (= 2 (long (count p1__18087#)))))
          (map
            (fn fn__18090 ([p1__18086#] (str/split p1__18086# #"=")))
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
  ;; Returns the storage protocol keyword without performing full URI validation.
  (defn storage-protocol
    ([uri]
      (if (instance? java.util.Map uri)
        (keyword (or (get uri :protocol) (get uri "protocol")))
        (keyword (nth (.split ^java.lang.String uri ":") (int 1))))))
  (reset-meta!
    #'storage-protocol
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'storage-protocol
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.uri" "parse*") {:column (int 1)})
  (let [v__5813__auto__ #'parse*]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
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
            (fn fn__18104
              ([p__18103]
                (let [vec__18105 p__18103
                      k (nth vec__18105 (int 0) nil)
                      v (nth vec__18105 (int 1) nil)]
                  [(keyword (str/replace k #"_" "-")) v])))
            (map
              (fn fn__18109 ([p1__18102#] (str/split p1__18102# #"=")))
              (str/split (or query "") #"&")))))))
  (reset-meta!
    #'param-map
    (assoc {:arglists (clojure.core/list ['query]), :column (int 1)} :name 'param-map :ns *ns*))
  (defn warn-creds
    ([params]
      (when (:aws-access-key-id params)
        (logger/print-and-warn
          "Supplying AWS credentials in connect strings is deprecated. See https://docs.datomic.com/operation/aws-access-control.html for recommended approaches."))))
  (reset-meta!
    #'warn-creds
    (assoc {:arglists (clojure.core/list ['params]), :column (int 1)} :name 'warn-creds :ns *ns*))
  (defn extract-query-string
    ([uri]
      (if uri
        (let [idx (.indexOf ^java.lang.String uri "?")]
          (if (>= idx 0) (subs uri (long (inc idx))) ""))
        "")))
  (reset-meta!
    #'extract-query-string
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uri {:tag 'String})]), :column (int 1)}
      :name
      'extract-query-string
      :ns
      *ns*))
  (defn sub-uri
    ([uri] (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri)))))
  (reset-meta!
    #'sub-uri
    (assoc
      {:arglists (clojure.core/list (.withMeta ['uri] {:tag 'java.net.URI})), :column (int 1)}
      :name
      'sub-uri
      :ns
      *ns*))
  (defmethod
    parse*
    :ddb
    fn__18116
    ([uri]
      (let [sub_uri (sub-uri uri)
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            paths (seq (.split (subs (.getPath ^java.net.URI sub_uri) 1) "/"))
            region (.getHost ^java.net.URI sub_uri)]
        (warn-creds params)
        {:protocol :ddb,
         :region region,
         :system-root (first paths),
         :db-name (second paths),
         :params {:ddb params}})))
  (defmethod
    parse*
    :ddbx
    fn__18118
    ([uri]
      (let [sub_uri (sub-uri uri)
            q (.getQuery ^java.net.URI sub_uri)
            params (param-map q)
            table (:table params)
            db_name (.getAuthority ^java.net.URI sub_uri)]
        {:protocol :ddbx, :system-root table, :aws-dynamodb-table table, :db-name db_name})))
  (defmethod
    parse*
    :ddb-local
    fn__18120
    ([uri]
      (let [sub_uri (sub-uri uri)
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
      (let [sub_uri (sub-uri uri)
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
    fn__18123
    ([uri]
      (let [map__18124 (if (instance? java.util.Map uri)
                         (fixup-uri-map uri)
                         (mapify-ddb+s3-uri uri))
            map__18124 (if (seq? map__18124)
                         (if (next map__18124)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18124))
                           (if (seq map__18124) (first map__18124) {}))
                         map__18124)
            m map__18124
            aws_dynamodb_table (get map__18124 :aws-dynamodb-table)
            system (get map__18124 :system)
            system (or system "_default")]
        (merge m {:system system, :system-root (str aws_dynamodb_table "/" system)}))))
  (defmethod
    parse*
    :s3
    fn__18127
    ([uri]
      (let [sub_uri (sub-uri uri)
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
    :inf
    fn__18129
    ([uri]
      (let [vec__18130 (str/split (str/replace uri #"^datomic:inf://" "") #"/")
            hp (nth vec__18130 (int 0) nil)
            db_name (nth vec__18130 (int 1) nil)
            vec__18133 (str/split hp #":")
            host (nth vec__18133 (int 0) nil)
            port (nth vec__18133 (int 1) nil)]
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
    fn__18138
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass://" "")
              vec__18139 (str/split path_query #"\?")
              path (nth vec__18139 (int 0) nil)
              query (nth vec__18139 (int 1) nil)
              vec__18142 (str/split path #"/")
              host_port (nth vec__18142 (int 0) nil)
              table (nth vec__18142 (int 1) nil)
              db_name (nth vec__18142 (int 2) nil)
              vec__18145 (str/split host_port #":")
              host (nth vec__18145 (int 0) nil)
              port (nth vec__18145 (int 1) nil)
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
    fn__18150
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass2://" "")
              vec__18151 (str/split path_query #"\?")
              path (nth vec__18151 (int 0) nil)
              query (nth vec__18151 (int 1) nil)
              vec__18154 (str/split path #"/")
              host_port (nth vec__18154 (int 0) nil)
              table (nth vec__18154 (int 1) nil)
              db_name (nth vec__18154 (int 2) nil)
              vec__18157 (str/split host_port #":")
              host (nth vec__18157 (int 0) nil)
              port (nth vec__18157 (int 1) nil)
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
    fn__18162
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass3://" "")
              vec__18163 (str/split path_query #"\?")
              path (nth vec__18163 (int 0) nil)
              query (nth vec__18163 (int 1) nil)
              vec__18166 (str/split path #"/")
              host_port (nth vec__18166 (int 0) nil)
              table (nth vec__18166 (int 1) nil)
              db_name (nth vec__18166 (int 2) nil)
              vec__18169 (str/split host_port #":")
              host (nth vec__18169 (int 0) nil)
              port (nth vec__18169 (int 1) nil)
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
    fn__18174
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [suffix (str/replace uri #"datomic:sql://" "")
              db_name (first (str/split suffix #"[?]"))]
          {:protocol :sql, :db-name db_name}))))
  (defn parse-h2
    ([uri]
      (let [vec__18176 (str/split
                         (str/replace (str/replace uri #"^datomic:[^:]+://" "") #"\?.*" "")
                         #"/")
            hp (nth vec__18176 (int 0) nil)
            db_name (nth vec__18176 (int 1) nil)
            vec__18179 (str/split hp #":")
            host (nth vec__18179 (int 0) nil)
            portstr (nth vec__18179 (int 1) nil)
            params (param-map (.getQuery (sub-uri uri)))
            temp__5825__auto__ (read-port portstr)]
        (when temp__5825__auto__
          (let [port temp__5825__auto__]
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
  (defmethod parse* :limited-edition fn__18186 ([uri] (parse-h2 uri)))
  (defmethod parse* :dev fn__18188 ([uri] (parse-h2 uri)))
  (defmethod
    parse*
    :mdev
    fn__18190
    ([uri]
      (let [vec__18191 (str/split (str/replace uri #"^datomic:mdev://" "") #"/")
            hp (nth vec__18191 (int 0) nil)
            db_name (nth vec__18191 (int 1) nil)
            vec__18194 (str/split hp #":")
            host (nth vec__18194 (int 0) nil)
            port (nth vec__18194 (int 1) nil)]
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
    fn__18199
    ([uri]
      (let [data (str/replace uri #"^datomic:mem://" "")
            idx (.indexOf ^java.lang.String data "?")
            db_name (if (>= idx 0) (subs data 0 (java.lang.Integer/valueOf (int idx))) data)]
        {:protocol :mem, :system-root "local", :db-name db_name})))
  (defmethod
    parse*
    :olddev
    fn__18201
    ([uri]
      (let [vec__18202 (str/split (str/replace uri #"^datomic:olddev://" "") #"/")
            hp (nth vec__18202 (int 0) nil)
            db_name (nth vec__18202 (int 1) nil)
            vec__18205 (str/split hp #":")
            host (nth vec__18205 (int 0) nil)
            port (nth vec__18205 (int 1) nil)]
        {:protocol :olddev,
         :system-root hp,
         :db-name db_name,
         :params
         {:ip
          {:host host,
           :port
           (java.lang.Integer/valueOf
             (int (java.lang.Integer/parseInt ^java.lang.String port)))}}})))
  (defmethod
    parse*
    :backup
    fn__18209
    ([uri]
      (let [sub_uri_str (.getSchemeSpecificPart (sub-uri uri))]
        {:protocol :backup, :backup-uri (first (str/split sub_uri_str #"\?"))})))
  (defmethod parse* :default fn__18211 ([uri] nil))
  (.setMeta (clojure.lang.RT/var "datomic.uri" "process-query-params") {:column (int 1)})
  (let [v__5813__auto__ #'process-query-params]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.uri" "process-query-params") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.uri" "process-query-params")
        (clojure.lang.MultiFn.
          "process-query-params"
          storage-protocol
          :default
          #'clojure.core/global-hierarchy))
      #'process-query-params))
  (defmethod
    process-query-params
    :sql
    fn__18217
    ([uri]
      (let [query_string (extract-query-string uri)
            vec__18218 (if (not (str/includes? query_string "#"))
                         [(not-empty query_string) {}]
                         (let [vec__18221 (str/split query_string #"#" 2)
                               query_string (nth vec__18221 (int 0) nil)
                               jdbc_uri (nth vec__18221 (int 1) nil)
                               param_map (if (seq query_string) (param-map query_string) {})
                               read_only (:read-only param_map)
                               param_map (if read_only
                                           (assoc
                                             param_map
                                             :read-only
                                             (java.lang.Boolean/parseBoolean
                                               ^java.lang.String read_only))
                                           param_map)]
                           [(not-empty jdbc_uri) param_map]))
            connection_string (nth vec__18218 (int 0) nil)
            query_params (nth vec__18218 (int 1) nil)]
        (merge {:system-root connection_string, :sql-url connection_string} query_params))))
  (defmethod
    process-query-params
    :default
    fn__18225
    ([uri]
      (let [query (extract-query-string uri)]
        (when (not (empty? query))
          (let [params (param-map query) read_only (:read-only params)]
            (if read_only
              (assoc
                params
                :read-only
                (java.lang.Boolean/parseBoolean ^java.lang.String read_only))
              params))))))
  ;; Normalizes a URI string or protocol map into storage and connection configuration.
  (defn parse
    ([uri]
      (if (instance? java.lang.String uri)
        (let [parsed_uri (parse* uri) params (process-query-params uri)]
          (merge parsed_uri params {:uri uri}))
        (assoc (parse* uri) :uri uri))))
  (reset-meta!
    #'parse
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'parse :ns *ns*))
  ;; Requires a database-qualified URI and returns its normalized configuration.
  (defn parse-db
    ([uri]
      (let [cluster-conf (parse uri)]
        (if (or (:db-name cluster-conf) (= (:protocol cluster-conf) :backup))
          cluster-conf
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
  ;; Selects endpoint fields suitable for structured logs and removes embedded query data.
  (defn loggable-cluster-conf
    ([cluster-conf]
      (let [m (select-keys
                cluster-conf
                [:protocol :db-name :system-root :host :port :bucket :db-id])]
        (cond-> m (:system-root m) (update :system-root remove-query-string)))))
  (reset-meta!
    #'loggable-cluster-conf
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'loggable-cluster-conf
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.uri" "create") {:column (int 1)})
  (let [v__5813__auto__ #'create]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.uri" "create") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.uri" "create")
        (clojure.lang.MultiFn.
          "create"
          (fn fn__18234 ([cluster_conf] (:protocol cluster_conf)))
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
          (fn fn__18241
            ([p__18240]
              (let [vec__18242 p__18240
                    k (nth vec__18242 (int 0) nil)
                    v (nth vec__18242 (int 1) nil)]
                (str (query-key k) "=" v))))
          m))))
  (reset-meta!
    #'query-args
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'query-args :ns *ns*))
  (defmethod
    create
    :ddb
    fn__18247
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            uri (str
                  "datomic:ddb://"
                  (common/getx cluster_conf :region)
                  "/"
                  system_root
                  (let [temp__5825__auto__ (:db-name cluster_conf)]
                    (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name))))
                  (let [temp__5825__auto__ (dissoc
                                             (get-in cluster_conf [:params :ddb])
                                             :read-only)]
                    (when temp__5825__auto__
                      (let [ddb_params temp__5825__auto__]
                        (when (seq ddb_params) (str "?" (query-args ddb_params)))))))
            temp__5823__auto__ (:read-only cluster_conf)]
        (if temp__5823__auto__
          (let [read_only temp__5823__auto__]
            (if (str/includes? uri "?")
              (str uri "&read-only=" read_only)
              (str uri "?read-only=" read_only)))
          uri))))
  (defmethod
    create
    :ddbx
    fn__18252
    ([cluster_conf]
      (let [table_arn (str/trim (common/getx cluster_conf :aws-dynamodb-table))
            db_name (common/getx cluster_conf :db-name)
            read_only (let [temp__5825__auto__ (:read-only cluster_conf)]
                        (when temp__5825__auto__
                          (let [v temp__5825__auto__] (str "&read-only=" v))))]
        (str "datomic:ddbx://" db_name "?" (query-args {:table table_arn}) read_only))))
  (defmethod
    create
    :ddb+s3
    fn__18255
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            root_or_table (if (= (common/getx cluster_conf :system) "_default")
                            (common/getx cluster_conf :aws-dynamodb-table)
                            system_root)
            map__18256 cluster_conf
            map__18256 (if (seq? map__18256)
                         (if (next map__18256)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18256))
                           (if (seq map__18256) (first map__18256) {}))
                         map__18256)
            db_name (get map__18256 :db-name)
            params (select-keys cluster_conf [:skip-efs :read-only])]
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
                  (fn fn__18258
                    ([p__18257]
                      (let [vec__18259 p__18257
                            k (nth vec__18259 (int 0) nil)
                            v (nth vec__18259 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass
    fn__18264
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__18265 cluster_conf
            map__18265 (if (seq? map__18265)
                         (if (next map__18265)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18265))
                           (if (seq map__18265) (first map__18265) {}))
                         map__18265)
            db_name (get map__18265 :db-name)
            params (select-keys cluster_conf [:user :password :ssl :read-only])]
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
                  (fn fn__18267
                    ([p__18266]
                      (let [vec__18268 p__18266
                            k (nth vec__18268 (int 0) nil)
                            v (nth vec__18268 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass2
    fn__18273
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__18274 cluster_conf
            map__18274 (if (seq? map__18274)
                         (if (next map__18274)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18274))
                           (if (seq map__18274) (first map__18274) {}))
                         map__18274)
            db_name (get map__18274 :db-name)
            params (select-keys cluster_conf [:user :password :ssl :read-only])]
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
                  (fn fn__18276
                    ([p__18275]
                      (let [vec__18277 p__18275
                            k (nth vec__18277 (int 0) nil)
                            v (nth vec__18277 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass3
    fn__18282
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__18283 cluster_conf
            map__18283 (if (seq? map__18283)
                         (if (next map__18283)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18283))
                           (if (seq map__18283) (first map__18283) {}))
                         map__18283)
            db_name (get map__18283 :db-name)
            params (select-keys cluster_conf [:user :password :ssl :local-datacenter :read-only])]
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
                  (fn fn__18285
                    ([p__18284]
                      (let [vec__18286 p__18284
                            k (nth vec__18286 (int 0) nil)
                            v (nth vec__18286 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :ddb-local
    fn__18291
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__18292 cluster_conf
            map__18292 (if (seq? map__18292)
                         (if (next map__18292)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18292))
                           (if (seq map__18292) (first map__18292) {}))
                         map__18292)
            override_endpoint (get map__18292 :override-endpoint)
            uri (str
                  "datomic:ddb-local://"
                  override_endpoint
                  "/"
                  system_root
                  (let [temp__5825__auto__ (:db-name cluster_conf)]
                    (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name))))
                  (let [temp__5825__auto__ (dissoc
                                             (get-in cluster_conf [:params :ddb])
                                             :read-only)]
                    (when temp__5825__auto__
                      (let [ddb_params temp__5825__auto__]
                        (when (seq ddb_params) (str "?" (query-args ddb_params)))))))
            temp__5823__auto__ (:read-only cluster_conf)]
        (if temp__5823__auto__
          (let [read_only temp__5823__auto__]
            (if (str/includes? uri "?")
              (str uri "&read-only=" read_only)
              (str uri "?read-only=" read_only)))
          uri))))
  (defmethod
    create
    :s3
    fn__18297
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (str
          "datomic:s3://"
          system_root
          "/"
          (:aws-s3-path cluster_conf)
          (let [temp__5825__auto__ (:db-name cluster_conf)]
            (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name))))
          (let [temp__5825__auto__ (select-keys
                                     cluster_conf
                                     [:aws-access-key-id :aws-secret-key :read-only])]
            (when temp__5825__auto__
              (let [params temp__5825__auto__] (str "?" (query-args params)))))))))
  (defmethod
    create
    :olddev
    fn__18301
    ([cluster_conf]
      (str
        "datomic:olddev://"
        (common/getx cluster_conf :system-root)
        (let [temp__5825__auto__ (:db-name cluster_conf)]
          (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name))))
        (let [temp__5825__auto__ (get-in cluster_conf [:params :ddb])]
          (when temp__5825__auto__
            (let [ddb_params temp__5825__auto__] (str "?" (query-args ddb_params))))))))
  (defmethod
    create
    :inf
    fn__18305
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:inf://"
            system_root
            (let [temp__5825__auto__ (:db-name cluster_conf)]
              (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name)))))))))
  (defmethod
    create
    :sql
    fn__18309
    ([cluster_conf]
      (str
        "datomic:sql://"
        (:db-name cluster_conf)
        "?"
        (let [temp__5825__auto__ (:read-only cluster_conf)]
          (when temp__5825__auto__
            (let [read_only temp__5825__auto__] (str "read-only=" read_only "#"))))
        (or (:sql-url cluster_conf) (:system-root cluster_conf)))))
  (defn map->query-string
    ([m]
      (str/join
        "&"
        (map
          (fn fn__18314
            ([p__18313]
              (let [vec__18315 p__18313
                    k (nth vec__18315 (int 0) nil)
                    v (nth vec__18315 (int 1) nil)]
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
  (defn create-h2
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:"
            (name (:protocol cluster_conf))
            "://"
            system_root
            (let [temp__5825__auto__ (:db-name cluster_conf)]
              (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name))))
            (if (not (= {:h2-port 4335} (select-keys cluster_conf [:h2-port])))
              (str "?" (map->query-string (select-keys cluster_conf [:h2-port :read-only])))
              (str "?" (map->query-string (select-keys cluster_conf [:read-only])))))))))
  (reset-meta!
    #'create-h2
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-h2
      :ns
      *ns*))
  (defmethod create :dev fn__18323 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod create :limited-edition fn__18325 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod
    create
    :mdev
    fn__18327
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (when-not (or (.startsWith ^java.lang.String system_root ":") (= system_root ""))
          (str
            "datomic:mdev://"
            system_root
            (let [temp__5825__auto__ (:db-name cluster_conf)]
              (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str "/" db_name)))))))))
  (defmethod
    create
    :backup
    fn__18331
    ([cluster_conf]
      (str
        "datomic:backup:"
        (:backup-uri cluster_conf)
        (let [temp__5825__auto__ (:t cluster_conf)]
          (when temp__5825__auto__ (let [t temp__5825__auto__] (str "?t=" t)))))))
  (defmethod
    create
    :mem
    fn__18334
    ([cluster_conf]
      (str
        "datomic:mem://"
        (let [temp__5825__auto__ (:db-name cluster_conf)]
          (when temp__5825__auto__ (let [db_name temp__5825__auto__] (str db_name))))
        (let [temp__5825__auto__ (:read-only cluster_conf)]
          (when temp__5825__auto__
            (let [read_only temp__5825__auto__] (str "?read-only=" read_only)))))))
  (defn db-uri ([uri db-name] (create (assoc (parse uri) :db-name db-name))))
  (reset-meta!
    #'db-uri
    (assoc
      {:arglists (clojure.core/list ['uri 'db-name]), :column (int 1)}
      :name
      'db-uri
      :ns
      *ns*)))

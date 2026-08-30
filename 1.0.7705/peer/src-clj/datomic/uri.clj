(do
  (clojure.core/in-ns 'datomic.uri)
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
          (fn fn__15668 ([p1__15667#] (= 2 (long (count p1__15667#)))))
          (map
            (fn fn__15670 ([p1__15666#] (str/split p1__15666# #"=")))
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
            (fn fn__15684
              ([p__15683]
                (let [vec__15685 p__15683
                      k (nth vec__15685 (int 0) nil)
                      v (nth vec__15685 (int 1) nil)]
                  [(keyword (str/replace k #"_" "-")) v])))
            (map
              (fn fn__15689 ([p1__15682#] (str/split p1__15682# #"=")))
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
  (def extract-query-string
   (fn extract_query_string
     ([uri]
       (if uri
         (let [idx (.indexOf ^java.lang.String uri "?")]
           (if (>= idx 0) (subs uri (long (inc idx))) ""))
         ""))))
  (reset-meta!
    #'extract-query-string
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uri {:tag 'String})]), :column (int 1)}
      :name
      'extract-query-string
      :ns
      *ns*))
  (def sub-uri
   (fn sub_uri
     ([uri] (java.net.URI. (.getSchemeSpecificPart (java.net.URI. ^java.lang.String uri))))))
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
    fn__15696
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
    fn__15698
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
    fn__15700
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
    fn__15703
    ([uri]
      (let [map__15704 (if (instance? java.util.Map uri)
                         (fixup-uri-map uri)
                         (mapify-ddb+s3-uri uri))
            map__15704 (if (seq? map__15704)
                         (if (next map__15704)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15704))
                           (if (seq map__15704) (first map__15704) {}))
                         map__15704)
            m map__15704
            aws_dynamodb_table (get map__15704 :aws-dynamodb-table)
            system (get map__15704 :system)
            system (or system "_default")]
        (merge m {:system system, :system-root (str aws_dynamodb_table "/" system)}))))
  (defmethod
    parse*
    :s3
    fn__15707
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
    fn__15709
    ([uri]
      (let [vec__15710 (str/split (str/replace uri #"^datomic:inf://" "") #"/")
            hp (nth vec__15710 (int 0) nil)
            db_name (nth vec__15710 (int 1) nil)
            vec__15713 (str/split hp #":")
            host (nth vec__15713 (int 0) nil)
            port (nth vec__15713 (int 1) nil)]
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
    fn__15718
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass://" "")
              vec__15719 (str/split path_query #"\?")
              path (nth vec__15719 (int 0) nil)
              query (nth vec__15719 (int 1) nil)
              vec__15722 (str/split path #"/")
              host_port (nth vec__15722 (int 0) nil)
              table (nth vec__15722 (int 1) nil)
              db_name (nth vec__15722 (int 2) nil)
              vec__15725 (str/split host_port #":")
              host (nth vec__15725 (int 0) nil)
              port (nth vec__15725 (int 1) nil)
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
    fn__15730
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass2://" "")
              vec__15731 (str/split path_query #"\?")
              path (nth vec__15731 (int 0) nil)
              query (nth vec__15731 (int 1) nil)
              vec__15734 (str/split path #"/")
              host_port (nth vec__15734 (int 0) nil)
              table (nth vec__15734 (int 1) nil)
              db_name (nth vec__15734 (int 2) nil)
              vec__15737 (str/split host_port #":")
              host (nth vec__15737 (int 0) nil)
              port (nth vec__15737 (int 1) nil)
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
    fn__15742
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [path_query (str/replace uri #"^datomic:cass3://" "")
              vec__15743 (str/split path_query #"\?")
              path (nth vec__15743 (int 0) nil)
              query (nth vec__15743 (int 1) nil)
              vec__15746 (str/split path #"/")
              host_port (nth vec__15746 (int 0) nil)
              table (nth vec__15746 (int 1) nil)
              db_name (nth vec__15746 (int 2) nil)
              vec__15749 (str/split host_port #":")
              host (nth vec__15749 (int 0) nil)
              port (nth vec__15749 (int 1) nil)
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
    fn__15754
    ([uri]
      (if (instance? java.util.Map uri)
        (fixup-uri-map uri)
        (let [suffix (str/replace uri #"datomic:sql://" "")
              db_name (first (str/split suffix #"[?]"))]
          {:protocol :sql, :db-name db_name}))))
  (defn parse-h2
    ([uri]
      (let [vec__15756 (str/split
                         (str/replace (str/replace uri #"^datomic:[^:]+://" "") #"\?.*" "")
                         #"/")
            hp (nth vec__15756 (int 0) nil)
            db_name (nth vec__15756 (int 1) nil)
            vec__15759 (str/split hp #":")
            host (nth vec__15759 (int 0) nil)
            portstr (nth vec__15759 (int 1) nil)
            params (param-map (.getQuery (sub-uri uri)))
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
  (defmethod parse* :limited-edition fn__15766 ([uri] (parse-h2 uri)))
  (defmethod parse* :dev fn__15768 ([uri] (parse-h2 uri)))
  (defmethod
    parse*
    :mdev
    fn__15770
    ([uri]
      (let [vec__15771 (str/split (str/replace uri #"^datomic:mdev://" "") #"/")
            hp (nth vec__15771 (int 0) nil)
            db_name (nth vec__15771 (int 1) nil)
            vec__15774 (str/split hp #":")
            host (nth vec__15774 (int 0) nil)
            port (nth vec__15774 (int 1) nil)]
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
    fn__15779
    ([uri]
      (let [data (str/replace uri #"^datomic:mem://" "")
            idx (.indexOf ^java.lang.String data "?")
            db_name (if (>= idx 0) (subs data 0 (java.lang.Integer/valueOf (int idx))) data)]
        {:protocol :mem, :system-root "local", :db-name db_name})))
  (defmethod
    parse*
    :olddev
    fn__15781
    ([uri]
      (let [vec__15782 (str/split (str/replace uri #"^datomic:olddev://" "") #"/")
            hp (nth vec__15782 (int 0) nil)
            db_name (nth vec__15782 (int 1) nil)
            vec__15785 (str/split hp #":")
            host (nth vec__15785 (int 0) nil)
            port (nth vec__15785 (int 1) nil)]
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
    fn__15789
    ([uri]
      (let [sub_uri_str (.getSchemeSpecificPart (sub-uri uri))]
        {:protocol :backup, :backup-uri (first (str/split sub_uri_str #"\?"))})))
  (defmethod parse* :default fn__15791 ([uri] nil))
  (.setMeta (clojure.lang.RT/var "datomic.uri" "process-query-params") {:column (int 1)})
  (let [v__5792__auto__ #'process-query-params]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
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
    fn__15797
    ([uri]
      (let [query_string (extract-query-string uri)
            vec__15798 (if (not (str/includes? query_string "#"))
                         [(not-empty query_string) {}]
                         (let [vec__15801 (str/split query_string #"#" 2)
                               query_string (nth vec__15801 (int 0) nil)
                               jdbc_uri (nth vec__15801 (int 1) nil)
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
            connection_string (nth vec__15798 (int 0) nil)
            query_params (nth vec__15798 (int 1) nil)]
        (merge {:system-root connection_string, :sql-url connection_string} query_params))))
  (defmethod
    process-query-params
    :default
    fn__15805
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
  (defn parse
    ([uri]
      (if (instance? java.lang.String uri)
        (let [parsed_uri (parse* uri) params (process-query-params uri)]
          (merge parsed_uri params {:uri uri}))
        (assoc (parse* uri) :uri uri))))
  (reset-meta!
    #'parse
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'parse :ns *ns*))
  (defn parse-db
    ([uri]
      (let [cluster_conf (parse uri)]
        (if (or (:db-name cluster_conf) (= (:protocol cluster_conf) :backup))
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
          (fn fn__15814 ([cluster_conf] (:protocol cluster_conf)))
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
          (fn fn__15821
            ([p__15820]
              (let [vec__15822 p__15820
                    k (nth vec__15822 (int 0) nil)
                    v (nth vec__15822 (int 1) nil)]
                (str (query-key k) "=" v))))
          m))))
  (reset-meta!
    #'query-args
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'query-args :ns *ns*))
  (defmethod
    create
    :ddb
    fn__15827
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            uri (str
                  "datomic:ddb://"
                  (common/getx cluster_conf :region)
                  "/"
                  system_root
                  (let [temp__5804__auto__ (:db-name cluster_conf)]
                    (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
                  (let [temp__5804__auto__ (dissoc
                                             (get-in cluster_conf [:params :ddb])
                                             :read-only)]
                    (when temp__5804__auto__
                      (let [ddb_params temp__5804__auto__]
                        (when (seq ddb_params) (str "?" (query-args ddb_params)))))))
            temp__5802__auto__ (:read-only cluster_conf)]
        (if temp__5802__auto__
          (let [read_only temp__5802__auto__]
            (if (str/includes? uri "?")
              (str uri "&read-only=" read_only)
              (str uri "?read-only=" read_only)))
          uri))))
  (defmethod
    create
    :ddbx
    fn__15832
    ([cluster_conf]
      (let [table_arn (str/trim (common/getx cluster_conf :aws-dynamodb-table))
            db_name (common/getx cluster_conf :db-name)
            read_only (let [temp__5804__auto__ (:read-only cluster_conf)]
                        (when temp__5804__auto__
                          (let [v temp__5804__auto__] (str "&read-only=" v))))]
        (str "datomic:ddbx://" db_name "?" (query-args {:table table_arn}) read_only))))
  (defmethod
    create
    :ddb+s3
    fn__15835
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            root_or_table (if (= (common/getx cluster_conf :system) "_default")
                            (common/getx cluster_conf :aws-dynamodb-table)
                            system_root)
            map__15836 cluster_conf
            map__15836 (if (seq? map__15836)
                         (if (next map__15836)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15836))
                           (if (seq map__15836) (first map__15836) {}))
                         map__15836)
            db_name (get map__15836 :db-name)
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
                  (fn fn__15838
                    ([p__15837]
                      (let [vec__15839 p__15837
                            k (nth vec__15839 (int 0) nil)
                            v (nth vec__15839 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass
    fn__15844
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__15845 cluster_conf
            map__15845 (if (seq? map__15845)
                         (if (next map__15845)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15845))
                           (if (seq map__15845) (first map__15845) {}))
                         map__15845)
            db_name (get map__15845 :db-name)
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
                  (fn fn__15847
                    ([p__15846]
                      (let [vec__15848 p__15846
                            k (nth vec__15848 (int 0) nil)
                            v (nth vec__15848 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass2
    fn__15853
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__15854 cluster_conf
            map__15854 (if (seq? map__15854)
                         (if (next map__15854)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15854))
                           (if (seq map__15854) (first map__15854) {}))
                         map__15854)
            db_name (get map__15854 :db-name)
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
                  (fn fn__15856
                    ([p__15855]
                      (let [vec__15857 p__15855
                            k (nth vec__15857 (int 0) nil)
                            v (nth vec__15857 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :cass3
    fn__15862
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__15863 cluster_conf
            map__15863 (if (seq? map__15863)
                         (if (next map__15863)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15863))
                           (if (seq map__15863) (first map__15863) {}))
                         map__15863)
            db_name (get map__15863 :db-name)
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
                  (fn fn__15865
                    ([p__15864]
                      (let [vec__15866 p__15864
                            k (nth vec__15866 (int 0) nil)
                            v (nth vec__15866 (int 1) nil)]
                        (str (name k) "=" v))))
                  params))))))))
  (defmethod
    create
    :ddb-local
    fn__15871
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))
            map__15872 cluster_conf
            map__15872 (if (seq? map__15872)
                         (if (next map__15872)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15872))
                           (if (seq map__15872) (first map__15872) {}))
                         map__15872)
            override_endpoint (get map__15872 :override-endpoint)
            uri (str
                  "datomic:ddb-local://"
                  override_endpoint
                  "/"
                  system_root
                  (let [temp__5804__auto__ (:db-name cluster_conf)]
                    (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
                  (let [temp__5804__auto__ (dissoc
                                             (get-in cluster_conf [:params :ddb])
                                             :read-only)]
                    (when temp__5804__auto__
                      (let [ddb_params temp__5804__auto__]
                        (when (seq ddb_params) (str "?" (query-args ddb_params)))))))
            temp__5802__auto__ (:read-only cluster_conf)]
        (if temp__5802__auto__
          (let [read_only temp__5802__auto__]
            (if (str/includes? uri "?")
              (str uri "&read-only=" read_only)
              (str uri "?read-only=" read_only)))
          uri))))
  (defmethod
    create
    :s3
    fn__15877
    ([cluster_conf]
      (let [system_root (str/trim (common/getx cluster_conf :system-root))]
        (str
          "datomic:s3://"
          system_root
          "/"
          (:aws-s3-path cluster_conf)
          (let [temp__5804__auto__ (:db-name cluster_conf)]
            (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str "/" db_name))))
          (let [temp__5804__auto__ (select-keys
                                     cluster_conf
                                     [:aws-access-key-id :aws-secret-key :read-only])]
            (when temp__5804__auto__
              (let [params temp__5804__auto__] (str "?" (query-args params)))))))))
  (defmethod
    create
    :olddev
    fn__15881
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
    fn__15885
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
    fn__15889
    ([cluster_conf]
      (str
        "datomic:sql://"
        (:db-name cluster_conf)
        "?"
        (let [temp__5804__auto__ (:read-only cluster_conf)]
          (when temp__5804__auto__
            (let [read_only temp__5804__auto__] (str "read-only=" read_only "#"))))
        (or (:sql-url cluster_conf) (:system-root cluster_conf)))))
  (defn map->query-string
    ([m]
      (str/join
        "&"
        (map
          (fn fn__15894
            ([p__15893]
              (let [vec__15895 p__15893
                    k (nth vec__15895 (int 0) nil)
                    v (nth vec__15895 (int 1) nil)]
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
             (if (not (= {:h2-port 4335} (select-keys cluster_conf [:h2-port])))
               (str "?" (map->query-string (select-keys cluster_conf [:h2-port :read-only])))
               (str "?" (map->query-string (select-keys cluster_conf [:read-only]))))))))))
  (reset-meta!
    #'create-h2
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-h2
      :ns
      *ns*))
  (defmethod create :dev fn__15903 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod create :limited-edition fn__15905 ([cluster_conf] (create-h2 cluster_conf)))
  (defmethod
    create
    :mdev
    fn__15907
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
    :backup
    fn__15911
    ([cluster_conf]
      (str
        "datomic:backup:"
        (:backup-uri cluster_conf)
        (let [temp__5804__auto__ (:t cluster_conf)]
          (when temp__5804__auto__ (let [t temp__5804__auto__] (str "?t=" t)))))))
  (defmethod
    create
    :mem
    fn__15914
    ([cluster_conf]
      (str
        "datomic:mem://"
        (let [temp__5804__auto__ (:db-name cluster_conf)]
          (when temp__5804__auto__ (let [db_name temp__5804__auto__] (str db_name))))
        (let [temp__5804__auto__ (:read-only cluster_conf)]
          (when temp__5804__auto__
            (let [read_only temp__5804__auto__] (str "?read-only=" read_only)))))))
  (def db-uri (fn db_uri ([uri db_name] (create (assoc (parse uri) :db-name db_name)))))
  (reset-meta!
    #'db-uri
    (assoc
      {:arglists (clojure.core/list ['uri 'db-name]), :column (int 1)}
      :name
      'db-uri
      :ns
      *ns*)))
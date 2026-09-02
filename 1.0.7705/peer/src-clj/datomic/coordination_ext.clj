(do
  (clojure.core/in-ns 'datomic.coordination-ext)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.coordination-ext)
    {:doc
     "Storage-protocol registrations for cluster construction. Dispatches DynamoDB, DynamoDB/S3, Infinispan, Cassandra, SQL, and embedded development configurations to their storage adapters. Remote SQL stores are shared by system root, while :dev and :limited-edition use an initialized local H2 service when available."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.require :as 'req]
        ['datomic.config :as 'config]
        ['datomic.coordination :as 'coord]
        ['datomic.kv-cluster :as 'kvc]
        ['datomic.kv-sql :as 'kvsql]
        ['datomic.simple-kv :as 'skv])))
  (when-not (.equals 'datomic.coordination-ext 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.coordination-ext))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.require :as 'req]
          ['datomic.config :as 'config]
          ['datomic.coordination :as 'coord]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.kv-sql :as 'kvsql]
          ['datomic.simple-kv :as 'skv]))))
  (defmethod
    coord/create-cluster
    :ddb
    fn__17256
    ([cluster_conf] (req/require-and-run 'datomic.ddb-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddb+s3
    fn__17258
    ([cluster_conf] (req/require-and-run 'datomic.ddb-s3-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddb-local
    fn__17260
    ([cluster_conf] (req/require-and-run 'datomic.ddb-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddbx
    fn__17262
    ([cluster_conf]
      (req/require-and-run 'datomic.ddb-cluster/create-ddbx-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :inf
    fn__17264
    ([cluster_conf]
      (let [map__17265 cluster_conf
            map__17265 (if (seq? map__17265)
                         (if (next map__17265)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17265))
                           (if (seq map__17265) (first map__17265) {}))
                         map__17265)
            host (get map__17265 :host)
            port (get map__17265 :port)
            endpoint {:host host, :port port}]
        (kvc/kv-cluster
          (req/require-and-run 'datomic.kv-hotrod/kv-infinispan endpoint)
          cluster_conf))))
  (defmethod
    coord/create-cluster
    :cass
    fn__17267
    ([cluster_conf]
      (let [endpoint (select-keys
                       cluster_conf
                       [:host :port :table :user :password :cluster :ssl :cluster-callback])]
        (kvc/kv-cluster
          (req/require-and-run 'datomic.kv-cassandra/kv-cassandra endpoint)
          cluster_conf))))
  (defmethod
    coord/create-cluster
    :cass2
    fn__17269
    ([cluster_conf]
      (let [endpoint (select-keys
                       cluster_conf
                       [:host :port :table :user :password :cluster :ssl :cluster-callback])]
        (kvc/kv-cluster
          (req/require-and-run 'datomic.kv-cassandra2/kv-cassandra endpoint)
          cluster_conf))))
  (defmethod
    coord/create-cluster
    :cass3
    fn__17271
    ([cluster_conf]
      (let [endpoint (select-keys
                       cluster_conf
                       [:host
                        :port
                        :table
                        :user
                        :password
                        :local-datacenter
                        :session
                        :ssl
                        :session-callback])]
        (kvc/kv-cluster
          (req/require-and-run 'datomic.kv-cassandra3/kv-cassandra endpoint)
          cluster_conf))))
  (.setMeta
    (clojure.lang.RT/var "datomic.coordination-ext" "remote-sql-stores")
    {:doc
     "Process-local cache of SQL key-value stores keyed by :system-root. Sharing the adapter preserves one connection-pool boundary for cluster configurations that address the same Datomic system.",
     :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.coordination-ext" "remote-sql-stores") (atom {}))
  (defmethod
    coord/create-cluster
    :sql
    fn__17273
    ([cluster_conf]
      (let [ck (:system-root cluster_conf)
            kvs (or
                  (get (deref remote-sql-stores) ck)
                  (do
                    (swap!
                      remote-sql-stores
                      (fn fn__17274
                        ([cs]
                          (if (get cs ck)
                            cs
                            (assoc
                              cs
                              ck
                              (req/require-and-run
                                'datomic.kv-sql-ext/kv-sql
                                (select-keys
                                  cluster_conf
                                  [:sql-url
                                   :data-source
                                   :factory
                                   :sql-user
                                   :sql-password
                                   :sql-initial-size
                                   :sql-driver-class
                                   :sql-driver-params])))))))
                    (get (deref remote-sql-stores) ck)))]
        (kvc/kv-cluster kvs cluster_conf))))
  (.setMeta
    (clojure.lang.RT/var "datomic.coordination-ext" "devspec")
    {:doc "JDBC specification for the process's initialized local development H2 service.",
     :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.coordination-ext" "devspec") (atom nil))
  (defn init-dev
    ([cluster_map data_dir]
      (let [spec (assoc cluster_map :data-dir (str data_dir "/db"))]
        (compare-and-set! devspec nil (req/require-and-run 'datomic.h2/init-tcp spec)))))
  (reset-meta!
    #'init-dev
    (assoc
      {:arglists (clojure.core/list ['cluster-map 'data-dir]),
       :doc
       "Initializes the local H2 TCP service for :dev and :limited-edition storage under data-dir/db. The first successful initialization establishes the process-wide development JDBC specification.",
       :column (int 1)}
      :name
      'init-dev
      :ns
      *ns*))
  (defmethod
    coord/init-protocol
    :dev
    fn__17279
    ([cluster_map data_dir] (init-dev cluster_map data_dir)))
  (defmethod
    coord/init-protocol
    :limited-edition
    fn__17281
    ([cluster_map data_dir] (init-dev cluster_map data_dir)))
  (defn create-dev-cluster
    ([cluster_conf]
      (kvc/kv-cluster
        (let [temp__5802__auto__ (deref devspec)]
          (if temp__5802__auto__
            (let [spec temp__5802__auto__]
              (kvsql/from-spec (req/require-and-run 'datomic.h2/local-jdbc-spec spec)))
            (kvsql/from-spec (req/require-and-run 'datomic.h2/remote-jdbc-spec cluster_conf))))
        cluster_conf)))
  (reset-meta!
    #'create-dev-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]),
       :doc
       "Creates the development key-value cluster. Uses the initialized local H2 service in the transactor process, or derives a remote JDBC specification when connecting from another process.",
       :column (int 1)}
      :name
      'create-dev-cluster
      :ns
      *ns*))
  (defmethod
    coord/create-cluster
    :dev
    fn__17285
    ([cluster_conf] (create-dev-cluster cluster_conf)))
  (defmethod
    coord/create-cluster
    :limited-edition
    fn__17287
    ([cluster_conf] (create-dev-cluster cluster_conf)))
  (config/set-pro))

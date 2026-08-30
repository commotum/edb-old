(do
  (clojure.core/in-ns 'datomic.coordination-ext)
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
    fn__17891
    ([cluster_conf] (req/require-and-run 'datomic.ddb-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddb+s3
    fn__17893
    ([cluster_conf] (req/require-and-run 'datomic.ddb-s3-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddb-local
    fn__17895
    ([cluster_conf] (req/require-and-run 'datomic.ddb-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddbx
    fn__17897
    ([cluster_conf]
      (req/require-and-run 'datomic.ddb-cluster/create-ddbx-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :inf
    fn__17899
    ([cluster_conf]
      (let [map__17900 cluster_conf
            map__17900 (if (seq? map__17900)
                         (if (next map__17900)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17900))
                           (if (seq map__17900) (first map__17900) {}))
                         map__17900)
            host (get map__17900 :host)
            port (get map__17900 :port)
            endpoint {:host host, :port port}]
        (kvc/kv-cluster
          (req/require-and-run 'datomic.kv-hotrod/kv-infinispan endpoint)
          cluster_conf))))
  (defmethod
    coord/create-cluster
    :cass
    fn__17902
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
    fn__17904
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
    fn__17906
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
  (.setMeta (clojure.lang.RT/var "datomic.coordination-ext" "remote-sql-stores") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.coordination-ext" "remote-sql-stores") (atom {}))
  (defmethod
    coord/create-cluster
    :sql
    fn__17908
    ([cluster_conf]
      (let [ck (:system-root cluster_conf)
            kvs (or
                  (get (deref remote-sql-stores) ck)
                  (do
                    (swap!
                      remote-sql-stores
                      (fn fn__17909
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
  (.setMeta (clojure.lang.RT/var "datomic.coordination-ext" "devspec") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.coordination-ext" "devspec") (atom nil))
  (defn init-dev
    ([cluster_map data_dir]
      (let [spec (assoc cluster_map :data-dir (str data_dir "/db"))]
        (compare-and-set! devspec nil (req/require-and-run 'datomic.h2/init-tcp spec)))))
  (reset-meta!
    #'init-dev
    (assoc
      {:arglists (clojure.core/list ['cluster-map 'data-dir]), :column (int 1)}
      :name
      'init-dev
      :ns
      *ns*))
  (defmethod
    coord/init-protocol
    :dev
    fn__17914
    ([cluster_map data_dir] (init-dev cluster_map data_dir)))
  (defmethod
    coord/init-protocol
    :limited-edition
    fn__17916
    ([cluster_map data_dir] (init-dev cluster_map data_dir)))
  (defn create-dev-cluster
    ([cluster_conf]
      (kvc/kv-cluster
        (let [temp__5823__auto__ (deref devspec)]
          (if temp__5823__auto__
            (let [spec temp__5823__auto__]
              (kvsql/from-spec (req/require-and-run 'datomic.h2/local-jdbc-spec spec)))
            (kvsql/from-spec (req/require-and-run 'datomic.h2/remote-jdbc-spec cluster_conf))))
        cluster_conf)))
  (reset-meta!
    #'create-dev-cluster
    (assoc
      {:arglists (clojure.core/list ['cluster-conf]), :column (int 1)}
      :name
      'create-dev-cluster
      :ns
      *ns*))
  (defmethod
    coord/create-cluster
    :dev
    fn__17920
    ([cluster_conf] (create-dev-cluster cluster_conf)))
  (defmethod
    coord/create-cluster
    :limited-edition
    fn__17922
    ([cluster_conf] (create-dev-cluster cluster_conf)))
  (config/set-pro))
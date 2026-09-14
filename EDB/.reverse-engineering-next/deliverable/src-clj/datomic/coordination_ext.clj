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
          ['datomic.simple-kv :as 'skv]))))
  (defmethod
    coord/create-cluster
    :ddb
    fn__16741
    ([cluster_conf] (req/require-and-run 'datomic.ddb-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddb+s3
    fn__16743
    ([cluster_conf] (req/require-and-run 'datomic.ddb-s3-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :ddb-local
    fn__16745
    ([cluster_conf] (req/require-and-run 'datomic.ddb-cluster/create-connection cluster_conf)))
  (defmethod
    coord/create-cluster
    :inf
    fn__16747
    ([cluster_conf]
      (let [map__16748 cluster_conf
            map__16748 (if (seq? map__16748)
                         (clojure.lang.PersistentHashMap/create (seq map__16748))
                         map__16748)
            host (get map__16748 :host)
            port (get map__16748 :port)
            endpoint {:host host, :port port}]
        (kvc/kv-cluster
          (req/require-and-run 'datomic.kv-hotrod/kv-infinispan endpoint)
          cluster_conf))))
  (defmethod
    coord/create-cluster
    :cass
    fn__16750
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
    fn__16752
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
    fn__16754
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
  (defmethod
    coord/create-cluster
    :couchbase
    fn__16756
    ([cluster_conf]
      (kvc/kv-cluster
        (req/require-and-run
          'datomic.kv-couchbase/kv-couchbase
          (select-keys cluster_conf [:host :bucket :password]))
        cluster_conf)))
  (def remote-sql-stores (atom {}))
  (defmethod
    coord/create-cluster
    :sql
    fn__16758
    ([cluster_conf]
      (let [ck (:system-root cluster_conf)
            kvs (or
                  (get (deref remote-sql-stores) ck)
                  (do
                    (swap!
                      remote-sql-stores
                      (fn fn__16759
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
  (defmethod
    coord/init-protocol
    :dev
    fn__16763
    ([cluster_map data_dir] (coord/init-dev cluster_map data_dir)))
  (defmethod
    coord/create-cluster
    :dev
    fn__16765
    ([cluster_conf] (coord/create-dev-cluster cluster_conf)))
  (config/set-pro))
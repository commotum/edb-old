(do
  (clojure.core/in-ns 'datomic.ddb-cluster)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.cluster :as 'cluster]
        ['datomic.ddb :as 'ddb]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger]
        ['datomic.ddb-values :as 'ddbv]
        ['datomic.kv-dynamo :as 'kvd]
        ['datomic.kv-cluster :as 'kvc]
        ['datomic.config :as 'config])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.ddb-cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ddb-cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.cluster :as 'cluster]
          ['datomic.ddb :as 'ddb]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger]
          ['datomic.ddb-values :as 'ddbv]
          ['datomic.kv-dynamo :as 'kvd]
          ['datomic.kv-cluster :as 'kvc]
          ['datomic.config :as 'config])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (defn key-path ([path_map k] (cluster/path (assoc path_map :key k))))
  (reset-meta!
    #'key-path
    (assoc
      {:private true, :arglists (clojure.core/list ['path-map 'k]), :column 1}
      :name
      'key-path
      :ns
      *ns*))
  (defn create-connection
    ([p__20504]
      (let [map__20505 p__20504
            map__20505 (if (seq? map__20505)
                         (clojure.lang.PersistentHashMap/create (seq map__20505))
                         map__20505)
            cluster_conf map__20505
            system_root (get map__20505 :system-root)
            params (get map__20505 :params)
            region (get map__20505 :region)
            override_endpoint (get map__20505 :override-endpoint)
            creds (or (:ddb params) (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
            ddb_client (ddb/client
                         creds
                         (config/ddb-client-args
                           {:region region, :override-endpoint override_endpoint}))
            kvs (kvd/kv-dynamo ddb_client system_root)]
        (kvc/kv-cluster kvs cluster_conf)))))
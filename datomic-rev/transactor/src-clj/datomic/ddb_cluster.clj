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
    ([p__30664]
      (let [map__30665 p__30664
            map__30665 (if (seq? map__30665)
                         (if (next map__30665)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30665))
                           (if (seq map__30665) (first map__30665) {}))
                         map__30665)
            cluster_conf map__30665
            system_root (get map__30665 :system-root)
            params (get map__30665 :params)
            region (get map__30665 :region)
            override_endpoint (get map__30665 :override-endpoint)
            creds (or (:ddb params) (com.amazonaws.auth.DefaultAWSCredentialsProviderChain.))
            ddb_client (ddb/client
                         creds
                         (config/ddb-client-args
                           {:region region, :override-endpoint override_endpoint}))
            kvs (kvd/kv-dynamo ddb_client system_root)]
        (kvc/kv-cluster kvs cluster_conf)))))
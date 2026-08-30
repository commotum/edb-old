(do
  (clojure.core/in-ns 'datomic.kv-dynamo)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['clojure.string :as 'str]
        ['datomic.core2.aws.helpers :as 'aws]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.ddb :as 'ddb]
        ['datomic.ddb-values :as 'ddbv]
        ['datomic.kv-store :as 'kv])))
  (when-not (.equals 'datomic.kv-dynamo 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-dynamo))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['clojure.string :as 'str]
          ['datomic.core2.aws.helpers :as 'aws]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.ddb :as 'ddb]
          ['datomic.ddb-values :as 'ddbv]
          ['datomic.kv-store :as 'kv]))))
  (defn key-path
    ([prefix k] (when k (str (when-not (str/blank? prefix) (format "%s/" prefix)) k))))
  (reset-meta!
    #'key-path
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column (int 1)}
      :name
      'key-path
      :ns
      *ns*))
  (defn remove-prefix
    ([prefix k] (when k (if (str/blank? prefix) k (str/replace k (format "%s/" prefix) "")))))
  (reset-meta!
    #'remove-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column (int 1)}
      :name
      'remove-prefix
      :ns
      *ns*))
  (def expected-map
   (fn expected_map
     ([expect_map]
       (into
         {}
         (map
           (fn fn__10003
             ([p__10002]
               (let [vec__10004 p__10002
                     k (nth vec__10004 (int 0) nil)
                     v (nth vec__10004 (int 1) nil)]
                 [(name k)
                  (if (nil? v) {:Exists false} {:Value {(if (number? v) :N :S) (str v)}})])))
           expect_map)))))
  (reset-meta!
    #'expected-map
    (assoc
      {:arglists (clojure.core/list ['expect-map]), :column (int 1)}
      :name
      'expected-map
      :ns
      *ns*))
  (deftype
    KVDynamo
    [client table prefix]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete
      [this key consistent?]
      (do
        (let [path (key-path prefix key)]
          (if consistent?
            (aws/invoke
              client
              {:op :DeleteItem,
               :req {:TableName table, :Key (ddb/create-key path), :ReturnValues "NONE"}})
            (ddbv/delete-value client table path)))
        :ok))
    (get
      [this key consistent?]
      (let [path (key-path prefix key)
            ret (if consistent?
                  (let [temp__5804__auto__ (:Item
                                             (aws/invoke
                                               client
                                               {:op :GetItem,
                                                :req
                                                {:TableName table,
                                                 :ConsistentRead consistent?,
                                                 :Key (ddb/create-key path)}}))]
                    (when temp__5804__auto__ (let [ret temp__5804__auto__] (ddb/deitem ret))))
                  (binding [ddbv/*retry* kv/*retry*] (ddbv/get-value client table path)))]
        (cond-> ret (:id ret) (update :id (partial remove-prefix prefix)))))
    (put
      [this val_map]
      (try
        (do
          (let [val_map (cond-> val_map (:id val_map) (update :id (partial key-path prefix)))]
            (if (:ensure val_map)
              (let [item (ddb/create-item (dissoc val_map :ensure))]
                (aws/invoke
                  client
                  {:op :PutItem,
                   :req
                   {:TableName table, :Item item, :Expected (expected-map (:ensure val_map))}}))
              (binding [ddbv/*retry* kv/*retry*] (ddbv/put-value client table val_map))))
          :ok)
        (catch
          java.lang.Exception
          ex
          (when-not (canom/conflict? (ex-data ex)) (throw ^java.lang.Throwable ex) nil)))))
  (clojure.core/import 'datomic.kv_dynamo.KVDynamo)
  (defn ->KVDynamo ([client table prefix] (datomic.kv_dynamo.KVDynamo. client table prefix)))
  (reset-meta!
    #'->KVDynamo
    (assoc
      {:arglists (clojure.core/list ['client 'table 'prefix]), :column (int 1)}
      :name
      '->KVDynamo
      :ns
      *ns*))
  (def kv-dynamo
   (fn kv_dynamo
     ([client table prefix] (datomic.kv_dynamo.KVDynamo. client table prefix))
     ([client table] (datomic.kv_dynamo.KVDynamo. client table nil))))
  (reset-meta!
    #'kv-dynamo
    (assoc
      {:arglists (clojure.core/list ['client 'table] ['client 'table 'prefix]), :column (int 1)}
      :name
      'kv-dynamo
      :ns
      *ns*)))
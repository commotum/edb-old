(do
  (clojure.core/in-ns 'datomic.kv-dynamo)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['clojure.string :as 'str]
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
          ['datomic.ddb :as 'ddb]
          ['datomic.ddb-values :as 'ddbv]
          ['datomic.kv-store :as 'kv]))))
  (defn key-path
    ([prefix k] (when k (str (when-not (str/blank? prefix) (format "%s/" prefix)) k))))
  (reset-meta!
    #'key-path
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column 1}
      :name
      'key-path
      :ns
      *ns*))
  (defn remove-prefix
    ([prefix k] (when k (if (str/blank? prefix) k (str/replace k (format "%s/" prefix) "")))))
  (reset-meta!
    #'remove-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['prefix 'k]), :column 1}
      :name
      'remove-prefix
      :ns
      *ns*))
  (defn expected-map
    ([expect_map]
      (into
        {}
        (map
          (fn fn__20481
            ([p__20480]
              (let [vec__20482 p__20480
                    k (nth vec__20482 (int 0) nil)
                    v (nth vec__20482 (int 1) nil)]
                [(name k)
                 (if (nil? v) {:exists false} {:value {(if (number? v) :n :s) (str v)}})])))
          expect_map))))
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
            (ddb/delete-item
              client
              {:tableName table, :key (ddb/create-key path), :returnValues "NONE"})
            (ddbv/delete-value client table path)))
        :ok))
    (get
      [this key consistent?]
      (let [path (key-path prefix key)
            ret (if consistent?
                  (let [temp__5457__auto__ (:item
                                             (ddb/get-item
                                               client
                                               {:tableName table,
                                                :consistentRead consistent?,
                                                :key (ddb/create-key path)}))]
                    (when temp__5457__auto__ (let [ret temp__5457__auto__] (ddb/deitem ret))))
                  (binding [ddbv/*retry* kv/*retry*] (ddbv/get-value client table path)))]
        (cond-> ret (:id ret) (update :id (partial remove-prefix prefix)))))
    (put
      [this val_map]
      (try
        (do
          (let [val_map (cond-> val_map (:id val_map) (update :id (partial key-path prefix)))]
            (if (:ensure val_map)
              (let [item (ddb/create-item (dissoc val_map :ensure))]
                (ddb/put-item
                  client
                  {:tableName table, :item item, :expected (expected-map (:ensure val_map))}))
              (binding [ddbv/*retry* kv/*retry*] (ddbv/put-value client table val_map))))
          :ok)
        (catch com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException ex nil))))
  (clojure.core/import 'datomic.kv_dynamo.KVDynamo)
  (defn ->KVDynamo ([client table prefix] (datomic.kv_dynamo.KVDynamo. client table prefix)))
  (defn kv-dynamo
    ([client table prefix] (datomic.kv_dynamo.KVDynamo. client table prefix))
    ([client table] (datomic.kv_dynamo.KVDynamo. client table nil))))
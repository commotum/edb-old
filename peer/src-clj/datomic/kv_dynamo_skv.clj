(do
  (clojure.core/in-ns 'datomic.kv-dynamo-skv)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['datomic.ddb :as 'ddb]
        ['datomic.io :as 'io]
        ['datomic.simple-kv :as 'skv]
        ['datomic.ddb-values :as 'ddbv]
        ['datomic.kv-store :as 'kv])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.kv-dynamo-skv 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-dynamo-skv))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['datomic.ddb :as 'ddb]
          ['datomic.io :as 'io]
          ['datomic.simple-kv :as 'skv]
          ['datomic.ddb-values :as 'ddbv]
          ['datomic.kv-store :as 'kv])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (defn expected-map
    ([expect_map]
      (into
        {}
        (map
          (fn fn__23534
            ([p__23533]
              (let [vec__23535 p__23533
                    k (nth vec__23535 (int 0) nil)
                    v (nth vec__23535 (int 1) nil)]
                [(name k)
                 (if (nil? v) {:exists false} {:value {(if (number? v) :n :s) (str v)}})])))
          expect_map))))
  (deftype
    KVDynamoSKV
    [client table skv prefix]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete
      [this key consistent?]
      (if consistent? (ddbv/delete-value client table (str prefix "/" key)) (skv/delete skv key)))
    (get
      [this key consistent?]
      (if consistent?
        (let [temp__5457__auto__ (:item
                                   (ddb/get-item
                                     client
                                     {:tableName table,
                                      :consistentRead consistent?,
                                      :key (ddb/create-key (str prefix "/" key))}))]
          (when temp__5457__auto__
            (let [ret temp__5457__auto__] (assoc (ddb/deitem ret) :id key))))
        (let [temp__5457__auto__ (skv/get-with-retry skv key)]
          (when temp__5457__auto__ (let [ret temp__5457__auto__] (skv/unpack key ret))))))
    (put
      [this p__23540]
      (let [map__23542 p__23540
            map__23542 (if (seq? map__23542)
                         (clojure.lang.PersistentHashMap/create (seq map__23542))
                         map__23542)
            val_map map__23542
            id (clojure.core/get map__23542 :id)
            v (clojure.core/get map__23542 :v)
            ensure (clojure.core/get map__23542 :ensure)]
        (try
          (do
            (if (:ensure val_map)
              (let [item (ddb/create-item
                           (dissoc (assoc val_map :id (str prefix "/" id)) :ensure))]
                (ddb/put-item
                  client
                  {:tableName table, :item item, :expected (expected-map (:ensure val_map))}))
              (let [m (dissoc val_map :id :v :ensure)]
                (skv/put skv id (if (empty? m) v (skv/pack m v)))))
            :ok)
          (catch
            com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
            ex
            nil)))))
  (clojure.core/import 'datomic.kv_dynamo_skv.KVDynamoSKV)
  (defn ->KVDynamoSKV
    ([client table skv prefix] (datomic.kv_dynamo_skv.KVDynamoSKV. client table skv prefix)))
  (defn kv-ddb-skv-
    ([creds prefix skv]
      (let [sbuf (skv/get-with-retry skv "config/dynamo.properties")
            _ (when-not sbuf
                (throw
                  (java.lang.AssertionError.
                    (str
                      "Assert failed: "
                      "No 'config/dynamo.properties' key found in storage"
                      "\n"
                      (pr-str 'sbuf))))
                nil)
            props (let [G__23549 (java.util.Properties.)]
                    (.load
                      ^java.util.Properties G__23549
                      (java.io.StringReader. (io/bbuf->string sbuf)))
                    G__23549)
            table (.getProperty ^java.util.Properties props "aws-dynamodb-table")
            _ (when-not table
                (throw
                  (java.lang.AssertionError.
                    (str
                      "Assert failed: "
                      "No 'aws-dynamodb-table' entry found in config/dynamo.properties"
                      "\n"
                      (pr-str 'table))))
                nil)
            region (.getProperty ^java.util.Properties props "aws-dynamodb-region")
            override_endpoint (.getProperty
                                ^java.util.Properties props
                                "aws-dynamodb-override-endpoint")
            _ (when-not table
                (throw
                  (java.lang.AssertionError.
                    (str
                      "Assert failed: "
                      "No 'aws-dynamodb-region' entry found in config/dynamo.properties"
                      "\n"
                      (pr-str 'table))))
                nil)
            client (ddb/client
                     creds
                     {:region region, :override-endpoint override_endpoint, :maxErrorRetry 0})]
        (datomic.kv_dynamo_skv.KVDynamoSKV. client table skv prefix))))
  (def kv-ddb-skv (memoize kv-ddb-skv-)))
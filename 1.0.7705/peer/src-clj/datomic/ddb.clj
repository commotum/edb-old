(do
  (clojure.core/in-ns 'datomic.ddb)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['datomic.aws.client.api :as 'aws]
        ['datomic.aws.client.anomalizer :as 'izer])
      (clojure.core/import 'software.amazon.awssdk.services.dynamodb.DynamoDbClient)))
  (when-not (.equals 'datomic.ddb 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ddb))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['datomic.aws.client.api :as 'aws]
          ['datomic.aws.client.anomalizer :as 'izer])
        (clojure.core/import 'software.amazon.awssdk.services.dynamodb.DynamoDbClient))))
  (set! *warn-on-reflection* true)
  (swap!
    izer/error-code-categories
    assoc
    "ConditionalCheckFailedException"
    :cognitect.anomalies/conflict
    "ResourceNotFoundException"
    :cognitect.anomalies/not-found)
  (def client
   (fn client
     ([creds opts] (aws-helpers/sync-client (DynamoDbClient/builder) creds opts))
     ([opts] (aws-helpers/sync-client (DynamoDbClient/builder) opts))))
  (reset-meta!
    #'client
    (assoc
      {:arglists (clojure.core/list ['opts] ['creds 'opts]), :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :ListTables]
    fn__9490
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9491
         ([client9488 G__9489]
           (.listTables
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9488
             ^software.amazon.awssdk.services.dynamodb.model.ListTablesRequest G__9489))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9493
         ([] (software.amazon.awssdk.services.dynamodb.model.ListTablesRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :CreateTable]
    fn__9498
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9499
         ([client9496 G__9497]
           (.createTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9496
             ^software.amazon.awssdk.services.dynamodb.model.CreateTableRequest G__9497))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9501
         ([] (software.amazon.awssdk.services.dynamodb.model.CreateTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :UpdateTable]
    fn__9506
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9507
         ([client9504 G__9505]
           (.updateTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9504
             ^software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest G__9505))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9509
         ([] (software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :DeleteTable]
    fn__9514
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9515
         ([client9512 G__9513]
           (.deleteTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9512
             ^software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest G__9513))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9517
         ([] (software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :DescribeTable]
    fn__9522
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9523
         ([client9520 G__9521]
           (.describeTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9520
             ^software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest G__9521))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9525
         ([] (software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :PutItem]
    fn__9530
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9531
         ([client9528 G__9529]
           (.putItem
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9528
             ^software.amazon.awssdk.services.dynamodb.model.PutItemRequest G__9529))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9533
         ([] (software.amazon.awssdk.services.dynamodb.model.PutItemRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :GetItem]
    fn__9538
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9539
         ([client9536 G__9537]
           (.getItem
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9536
             ^software.amazon.awssdk.services.dynamodb.model.GetItemRequest G__9537))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9541
         ([] (software.amazon.awssdk.services.dynamodb.model.GetItemRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :DeleteItem]
    fn__9546
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__9547
         ([client9544 G__9545]
           (.deleteItem
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client9544
             ^software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest G__9545))),
       :ret-mode :sync,
       :request-builder
       (fn fn__9549
         ([] (software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest/builder)))}))
  (defn fullname ([s] (cond (keyword? s) (subs (str s) 1) :default (do (str s)))))
  (reset-meta!
    #'fullname
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'fullname :ns *ns*))
  (defn create-item
    ([m]
      (reduce
        (fn fn__9554
          ([m p__9553]
            (let [vec__9555 p__9553 k (nth vec__9555 (int 0) nil) v (nth vec__9555 (int 1) nil)]
              (assoc m (fullname k) (if (number? v) {:N (str v)} {:S v})))))
        {}
        m)))
  (reset-meta!
    #'create-item
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'create-item :ns *ns*))
  (defn deattr
    ([m]
      (let [G__9560 (set (keys m))]
        (case
          G__9560
          #{:N}
          (edn/read-string (:N m))
          #{:S}
          (:S m)
          (do (throw (ex-info "Could not parse DDB item " m)) (clojure.lang.Util/hash G__9560))))))
  (reset-meta!
    #'deattr
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'deattr :ns *ns*))
  (defn deitem
    ([m]
      (reduce
        (fn fn__9563
          ([m p__9562]
            (let [vec__9564 p__9562 k (nth vec__9564 (int 0) nil) v (nth vec__9564 (int 1) nil)]
              (assoc m (keyword k) (deattr v)))))
        {}
        m)))
  (reset-meta!
    #'deitem
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'deitem :ns *ns*))
  (defn create-key ([k] {"id" {(if (number? k) :N :S) k}}))
  (reset-meta!
    #'create-key
    (assoc {:arglists (clojure.core/list ['k]), :column (int 1)} :name 'create-key :ns *ns*)))
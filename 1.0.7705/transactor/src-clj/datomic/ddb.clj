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
  (defn client
    ([creds opts] (aws-helpers/sync-client (DynamoDbClient/builder) creds opts))
    ([opts] (aws-helpers/sync-client (DynamoDbClient/builder) opts)))
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
    fn__19607
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19608
         ([client19605 G__19606]
           (.listTables
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19605
             ^software.amazon.awssdk.services.dynamodb.model.ListTablesRequest G__19606))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19610
         ([] (software.amazon.awssdk.services.dynamodb.model.ListTablesRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :CreateTable]
    fn__19615
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19616
         ([client19613 G__19614]
           (.createTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19613
             ^software.amazon.awssdk.services.dynamodb.model.CreateTableRequest G__19614))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19618
         ([] (software.amazon.awssdk.services.dynamodb.model.CreateTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :UpdateTable]
    fn__19623
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19624
         ([client19621 G__19622]
           (.updateTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19621
             ^software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest G__19622))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19626
         ([] (software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :DeleteTable]
    fn__19631
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19632
         ([client19629 G__19630]
           (.deleteTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19629
             ^software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest G__19630))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19634
         ([] (software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :DescribeTable]
    fn__19639
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19640
         ([client19637 G__19638]
           (.describeTable
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19637
             ^software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest G__19638))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19642
         ([] (software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :PutItem]
    fn__19647
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19648
         ([client19645 G__19646]
           (.putItem
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19645
             ^software.amazon.awssdk.services.dynamodb.model.PutItemRequest G__19646))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19650
         ([] (software.amazon.awssdk.services.dynamodb.model.PutItemRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :GetItem]
    fn__19655
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19656
         ([client19653 G__19654]
           (.getItem
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19653
             ^software.amazon.awssdk.services.dynamodb.model.GetItemRequest G__19654))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19658
         ([] (software.amazon.awssdk.services.dynamodb.model.GetItemRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.dynamodb.DynamoDbClient :DeleteItem]
    fn__19663
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__19664
         ([client19661 G__19662]
           (.deleteItem
             ^software.amazon.awssdk.services.dynamodb.DynamoDbClient client19661
             ^software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest G__19662))),
       :ret-mode :sync,
       :request-builder
       (fn fn__19666
         ([] (software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest/builder)))}))
  (defn fullname ([s] (cond (keyword? s) (subs (str s) 1) :default (do (str s)))))
  (reset-meta!
    #'fullname
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'fullname :ns *ns*))
  (defn create-item
    ([m]
      (reduce
        (fn fn__19671
          ([m p__19670]
            (let [vec__19672 p__19670
                  k (nth vec__19672 (int 0) nil)
                  v (nth vec__19672 (int 1) nil)]
              (assoc m (fullname k) (if (number? v) {:N (str v)} {:S v})))))
        {}
        m)))
  (reset-meta!
    #'create-item
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'create-item :ns *ns*))
  (defn deattr
    ([m]
      (let [G__19677 (set (keys m))]
        (case
          G__19677
          #{:N}
          (edn/read-string (:N m))
          #{:S}
          (:S m)
          (do
            (throw (ex-info "Could not parse DDB item " m))
            (clojure.lang.Util/hash G__19677))))))
  (reset-meta!
    #'deattr
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'deattr :ns *ns*))
  (defn deitem
    ([m]
      (reduce
        (fn fn__19680
          ([m p__19679]
            (let [vec__19681 p__19679
                  k (nth vec__19681 (int 0) nil)
                  v (nth vec__19681 (int 1) nil)]
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
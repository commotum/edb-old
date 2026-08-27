(do
  (clojure.core/in-ns 'datomic.core2.aws.ddb)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.core2.aws.ddb 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.aws.ddb))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/import 'java.nio.ByteBuffer))))
  (defn attribute-value
    ([v]
      (if (string? v)
        {:S v}
        (if (integer? v)
          {:N (str v)}
          (if (instance? java.nio.ByteBuffer v)
            {:B (.duplicate ^java.nio.ByteBuffer v)}
            (do
              (when :default (throw (ex-info "No type defined" #:datomic.core2.aws.ddb{:value v})))
              nil))))))
  (defn item-map
    ([m]
      (reduce-kv
        (fn fn__20432
          ([m k v]
            (when (nil? v) (throw (java.lang.RuntimeException. (str "No value for " k))))
            (assoc m (name k) (attribute-value v))))
        {}
        m)))
  (defn de-attribute-value
    ([m]
      (let [G__20435 (set (keys m))]
        (case
          G__20435
          #{:BOOL}
          (:BOOL m)
          #{:N}
          (long (java.lang.Long/parseLong (:N m)))
          #{:B}
          (:B m)
          #{:S}
          (:S m)
          (do
            (throw (ex-info "Could not parse DDB item " m))
            (bit-and (bit-shift-right (clojure.lang.Util/hash G__20435) 0) 7))))))
  (defn de-item-map
    ([m] (reduce-kv (fn fn__20437 ([m k v] (assoc m k (de-attribute-value v)))) {} m)))
  (defn conditional-put-request
    ([p__20440]
      (let [map__20441 p__20440
            map__20441 (if (seq? map__20441)
                         (if (next map__20441)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20441))
                           (if (seq map__20441) (first map__20441) {}))
                         map__20441)
            table (get map__20441 :table)
            p (get map__20441 :p)
            r (get map__20441 :r)
            item (get map__20441 :item)]
        {:op :PutItem,
         :request
         {:TableName table,
          :Item (item-map item),
          :Expected {(name p) {:Exists false}, (name r) {:Exists false}}}})))
  (defn query-range-request
    ([p__20443]
      (let [map__20444 p__20443
            map__20444 (if (seq? map__20444)
                         (if (next map__20444)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20444))
                           (if (seq map__20444) (first map__20444) {}))
                         map__20444)
            table (get map__20444 :table)
            p (get map__20444 :p)
            r (get map__20444 :r)
            attrs (get map__20444 :attrs)
            forward (get map__20444 :forward)
            limit (get map__20444 :limit)]
        {:op :Query,
         :request
         {:TableName table,
          :ConsistentRead true,
          :ScanIndexForward forward,
          :KeyConditions
          {p
           {:ComparisonOperator "EQ",
            :AttributeValueList [(attribute-value (^clojure.lang.IFn p attrs))]},
           r
           {:ComparisonOperator (if forward "GE" "LE"),
            :AttributeValueList [(attribute-value (^clojure.lang.IFn r attrs))]}},
          :Limit limit}}))))
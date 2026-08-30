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
  (reset-meta!
    #'attribute-value
    (assoc {:arglists (clojure.core/list ['v]), :column (int 1)} :name 'attribute-value :ns *ns*))
  (defn item-map
    ([m]
      (reduce-kv
        (fn fn__21256
          ([m k v]
            (when (nil? v) (throw (java.lang.RuntimeException. (str "No value for " k))))
            (assoc m (name k) (attribute-value v))))
        {}
        m)))
  (reset-meta!
    #'item-map
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'item-map :ns *ns*))
  (defn de-attribute-value
    ([m]
      (let [G__21259 (set (keys m))]
        (case
          G__21259
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
            (bit-and (bit-shift-right (clojure.lang.Util/hash G__21259) 0) 7))))))
  (reset-meta!
    #'de-attribute-value
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'de-attribute-value
      :ns
      *ns*))
  (defn de-item-map
    ([m] (reduce-kv (fn fn__21261 ([m k v] (assoc m k (de-attribute-value v)))) {} m)))
  (reset-meta!
    #'de-item-map
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'de-item-map :ns *ns*))
  (def conditional-put-request
   (fn conditional_put_request
     ([p__21264]
       (let [map__21265 p__21264
             map__21265 (if (seq? map__21265)
                          (if (next map__21265)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21265))
                            (if (seq map__21265) (first map__21265) {}))
                          map__21265)
             table (get map__21265 :table)
             p (get map__21265 :p)
             r (get map__21265 :r)
             item (get map__21265 :item)]
         {:op :PutItem,
          :request
          {:TableName table,
           :Item (item-map item),
           :Expected {(name p) {:Exists false}, (name r) {:Exists false}}}}))))
  (reset-meta!
    #'conditional-put-request
    (assoc
      {:arglists (clojure.core/list [{:keys ['table 'p 'r 'item]}]), :column (int 1)}
      :name
      'conditional-put-request
      :ns
      *ns*))
  (def query-range-request
   (fn query_range_request
     ([p__21267]
       (let [map__21268 p__21267
             map__21268 (if (seq? map__21268)
                          (if (next map__21268)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21268))
                            (if (seq map__21268) (first map__21268) {}))
                          map__21268)
             table (get map__21268 :table)
             p (get map__21268 :p)
             r (get map__21268 :r)
             attrs (get map__21268 :attrs)
             forward (get map__21268 :forward)
             limit (get map__21268 :limit)]
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
  (reset-meta!
    #'query-range-request
    (assoc
      {:arglists (clojure.core/list [{:keys ['table 'p 'r 'attrs 'forward 'limit]}]),
       :column (int 1)}
      :name
      'query-range-request
      :ns
      *ns*)))
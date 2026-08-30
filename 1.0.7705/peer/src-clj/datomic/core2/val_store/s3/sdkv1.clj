(do
  (clojure.core/in-ns 'datomic.core2.val-store.s3.sdkv1)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['cognitect.caster :as 'cast]
        ['datomic.core2.aws.s3.sdkv1 :as 'sdkv1]
        ['datomic.core2.val-store.spi :as 'spi]
        ['datomic.core2.val-store.s3 :as 's3]
        ['datomic.core2.thread :refer (clojure.core/list 'pfuture-ch)]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'com.amazonaws.services.s3.model.ObjectMetadata)
      (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
      (clojure.core/import 'datomic.java.io.impl.ByteBufferInputStream)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.concurrent.ExecutorService)))
  (when-not (.equals 'datomic.core2.val-store.s3.sdkv1 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.val-store.s3.sdkv1))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['cognitect.caster :as 'cast]
          ['datomic.core2.aws.s3.sdkv1 :as 'sdkv1]
          ['datomic.core2.val-store.spi :as 'spi]
          ['datomic.core2.val-store.s3 :as 's3]
          ['datomic.core2.thread :refer (clojure.core/list 'pfuture-ch)]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'com.amazonaws.services.s3.model.ObjectMetadata)
        (clojure.core/import 'com.amazonaws.services.s3.AmazonS3Client)
        (clojure.core/import 'datomic.java.io.impl.ByteBufferInputStream)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.concurrent.ExecutorService))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      Impl
      (-sync-get [_ k opts] "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly.")
      (-sync-put [_ k v opts] "Returns {:result :created} or anomaly.")
      (-sync-delete [_ k opts] "Returns {:result :deleted} or anomaly.")
      (-wrap-op
        [_ f k op opts context]
        "Wraps f in a retry, metric-handler, and exception->anom handler. Returns (f) or anomaly."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.s3.sdkv1" "Impl")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Impl :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-sync-get
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc
                                      "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.core2.val-store.s3.sdkv1"
                                       "Impl"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.s3.sdkv1" "-sync-get")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-sync-put
                                        {:arglists (clojure.core/list ['_ 'k 'v 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'v 'opts]),
                                      :doc "Returns {:result :created} or anomaly."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.core2.val-store.s3.sdkv1"
                                       "Impl"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.s3.sdkv1" "-sync-put")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-sync-delete
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc "Returns {:result :deleted} or anomaly."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.core2.val-store.s3.sdkv1"
                                       "Impl"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.s3.sdkv1" "-sync-delete")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*)))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-wrap-op
                                        {:arglists
                                         (clojure.core/list ['_ 'f 'k 'op 'opts 'context])}),
                                      :arglists (clojure.core/list ['_ 'f 'k 'op 'opts 'context]),
                                      :doc
                                      "Wraps f in a retry, metric-handler, and exception->anom handler. Returns (f) or anomaly."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.core2.val-store.s3.sdkv1"
                                       "Impl"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.s3.sdkv1" "-wrap-op")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
  (deftype
    ValStore
    [read_pool write_pool retry_fn client bucket prefix]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.s3.sdkv1.Impl
    datomic.core2.val_store.spi.Put
    (-delete
      [this k opts]
      (datomic.core2.thread/pfuture-ch
        (-wrap-op
          this
          (fn fn__22625 ([p1__22616#] (-sync-delete this p1__22616# opts)))
          k
          :delete
          opts
          {})
        write_pool))
    (-get
      [this k opts]
      (do
        (io-stats/inc! :s3)
        (datomic.core2.thread/pfuture-ch
          (-wrap-op
            this
            (fn fn__22623 ([p1__22615#] (-sync-get this p1__22615# opts)))
            k
            :get
            opts
            {})
          read_pool)))
    (-put
      [this k v opts]
      (datomic.core2.thread/pfuture-ch
        (-wrap-op
          this
          (fn fn__22621 ([p1__22614#] (-sync-put this p1__22614# v opts)))
          k
          :create
          opts
          {:content-length (java.lang.Integer/valueOf (int (.remaining (:val v))))})
        write_pool))
    (-sync-delete
      [this k opts]
      (do (sdkv1/delete-object client bucket (s3/storage-key prefix k opts)) {:result :deleted}))
    (-sync-put
      [this k p__22617 opts]
      (let [map__22619 p__22617
            map__22619 (if (seq? map__22619)
                         (if (next map__22619)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22619))
                           (if (seq map__22619) (first map__22619) {}))
                         map__22619)
            v map__22619
            val (get map__22619 :val)]
        (or
          (spi/no-val-error k v)
          (let [content_length (.remaining ^java.nio.Buffer val)]
            (sdkv1/put-object
              client
              bucket
              (s3/storage-key prefix k opts)
              (datomic.java.io.impl.ByteBufferInputStream. ^java.nio.ByteBuffer val)
              (let [G__22620 (com.amazonaws.services.s3.model.ObjectMetadata.)]
                (.setContentLength
                  ^com.amazonaws.services.s3.model.ObjectMetadata G__22620
                  (long content_length))
                G__22620))
            (cast/metric*
              cast/instance
              {:name :s3.put.bytes,
               :value (java.lang.Integer/valueOf (int content_length)),
               :units :count})
            {:result :created}))))
    (-sync-get
      [this k opts]
      (let [value (sdkv1/get-bytes client bucket (s3/storage-key prefix k opts))]
        (cond
          (nil? value) {:val nil}
          :default (do
                     (cast/metric*
                       cast/instance
                       {:name :s3.get.bytes,
                        :value (java.lang.Integer/valueOf (int (count value))),
                        :units :count})
                     {:val (ByteBuffer/wrap ^bytes value)}))))
    (-wrap-op
      [this f k op opts context]
      (^clojure.lang.IFn retry_fn
        (s3/wrap-metric-handler
          (sdkv1/wrap-ex-handler
            f
            #:datomic.core2.val-store.s3.sdkv1{:bucket bucket,
                                               :prefix prefix,
                                               :k (s3/storage-key prefix k opts)})
          k
          op
          context)
        op)))
  (clojure.core/import 'datomic.core2.val_store.s3.sdkv1.ValStore)
  (defn ->ValStore
    ([read_pool write_pool retry_fn client bucket prefix]
      (datomic.core2.val_store.s3.sdkv1.ValStore.
        read_pool
        write_pool
        retry_fn
        client
        bucket
        prefix)))
  (reset-meta!
    #'->ValStore
    (assoc
      {:arglists (clojure.core/list ['read-pool 'write-pool 'retry-fn 'client 'bucket 'prefix]),
       :column (int 1)}
      :name
      '->ValStore
      :ns
      *ns*))
  (defn create
    ([p__22632]
      (let [map__22633 p__22632
            map__22633 (if (seq? map__22633)
                         (if (next map__22633)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22633))
                           (if (seq map__22633) (first map__22633) {}))
                         map__22633)
            read_pool (get map__22633 :read-pool)
            write_pool (get map__22633 :write-pool)
            retry_fn (get map__22633 :retry-fn)
            bucket (get map__22633 :bucket)
            client (get map__22633 :client)
            prefix (get map__22633 :prefix)]
        (when-not (and read_pool write_pool retry_fn bucket client prefix)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list
                    'and
                    'read-pool
                    'write-pool
                    'retry-fn
                    'bucket
                    'client
                    'prefix))))))
        (->ValStore read_pool write_pool retry_fn client bucket prefix))))
  (reset-meta!
    #'create
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['read-pool 'write-pool 'retry-fn 'bucket 'client 'prefix]}]),
       :column (int 1)}
      :name
      'create
      :ns
      *ns*)))
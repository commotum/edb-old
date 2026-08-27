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
  (defonce Impl {})
  (defprotocol
    Impl
    (-sync-get [_ k opts])
    (-sync-put [_ k v opts])
    (-sync-delete [_ k opts])
    (-wrap-op [_ f k op opts context]))
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
          (fn fn__21768 ([p1__21759#] (-sync-delete this p1__21759# opts)))
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
            (fn fn__21766 ([p1__21758#] (-sync-get this p1__21758# opts)))
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
          (fn fn__21764 ([p1__21757#] (-sync-put this p1__21757# v opts)))
          k
          :create
          opts
          {:content-length (java.lang.Integer/valueOf (int (.remaining (:val v))))})
        write_pool))
    (-sync-delete
      [this k opts]
      (do (sdkv1/delete-object client bucket (s3/storage-key prefix k opts)) {:result :deleted}))
    (-sync-put
      [this k p__21760 opts]
      (let [map__21762 p__21760
            map__21762 (if (seq? map__21762)
                         (if (next map__21762)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21762))
                           (if (seq map__21762) (first map__21762) {}))
                         map__21762)
            v map__21762
            val (get map__21762 :val)]
        (or
          (spi/no-val-error k v)
          (let [content_length (.remaining ^java.nio.Buffer val)]
            (sdkv1/put-object
              client
              bucket
              (s3/storage-key prefix k opts)
              (datomic.java.io.impl.ByteBufferInputStream. ^java.nio.ByteBuffer val)
              (let [G__21763 (com.amazonaws.services.s3.model.ObjectMetadata.)]
                (.setContentLength
                  ^com.amazonaws.services.s3.model.ObjectMetadata G__21763
                  (long content_length))
                G__21763))
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
  (defn create
    ([p__21775]
      (let [map__21776 p__21775
            map__21776 (if (seq? map__21776)
                         (if (next map__21776)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21776))
                           (if (seq map__21776) (first map__21776) {}))
                         map__21776)
            read_pool (get map__21776 :read-pool)
            write_pool (get map__21776 :write-pool)
            retry_fn (get map__21776 :retry-fn)
            bucket (get map__21776 :bucket)
            client (get map__21776 :client)
            prefix (get map__21776 :prefix)]
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
        (->ValStore read_pool write_pool retry_fn client bucket prefix)))))
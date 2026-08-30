(do
  (clojure.core/in-ns 'datomic.core2.val-store.fs)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list 'offer! 'chan 'close!)]
        ['clojure.java.io :as 'io]
        ['cognitect.anomalies :as 'anom]
        ['cognitect.caster :as 'cast]
        ['datomic.core2.anomalies :as 'canom]
        ['datomic.core2.anomalizer :as 'izer]
        ['datomic.core2.val-store.spi :as 'spi]
        ['datomic.core2.thread :refer (clojure.core/list 'pfuture-ch)]
        ['datomic.java.io.bbuf :as 'bbuf]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'java.io.File)
      (clojure.core/import 'java.nio.channels.FileChannel)
      (clojure.core/import 'java.nio.file.Files)
      (clojure.core/import 'java.nio.file.StandardOpenOption)
      (clojure.core/import 'java.nio.file.NoSuchFileException)
      (clojure.core/import 'java.nio.file.Path)
      (clojure.core/import 'java.nio.file.attribute.FileAttribute)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.util.concurrent.RejectedExecutionException)))
  (when-not (.equals 'datomic.core2.val-store.fs 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.val-store.fs))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :refer (clojure.core/list 'offer! 'chan 'close!)]
          ['clojure.java.io :as 'io]
          ['cognitect.anomalies :as 'anom]
          ['cognitect.caster :as 'cast]
          ['datomic.core2.anomalies :as 'canom]
          ['datomic.core2.anomalizer :as 'izer]
          ['datomic.core2.val-store.spi :as 'spi]
          ['datomic.core2.thread :refer (clojure.core/list 'pfuture-ch)]
          ['datomic.java.io.bbuf :as 'bbuf]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'java.io.File)
        (clojure.core/import 'java.nio.channels.FileChannel)
        (clojure.core/import 'java.nio.file.Files)
        (clojure.core/import 'java.nio.file.StandardOpenOption)
        (clojure.core/import 'java.nio.file.NoSuchFileException)
        (clojure.core/import 'java.nio.file.Path)
        (clojure.core/import 'java.nio.file.attribute.FileAttribute)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.util.concurrent.RejectedExecutionException))))
  (set! *warn-on-reflection* true)
  (def success-metrics
   {:get :efs.get.succeeded.msec,
    :put :efs.put.succeeded.msec,
    :delete :efs.delete.succeeded.msec})
  (reset-meta! #'success-metrics (assoc {:column (int 1)} :name 'success-metrics :ns *ns*))
  (def failure-metrics
   {:get :efs.get.failed.msec, :put :efs.put.failed.msec, :delete :efs.delete.failed.msec})
  (reset-meta! #'failure-metrics (assoc {:column (int 1)} :name 'failure-metrics :ns *ns*))
  (defn wrap-metric-handler
    ([f k op]
      (let [start_nsec (java.lang.System/nanoTime)]
        (fn fn__22116
          ([]
            (let [result (^clojure.lang.IFn f k)
                  ok? (spi/val-op-succeeded? result)
                  nsec (- (java.lang.System/nanoTime) start_nsec)]
              (when (= op :get)
                (io-stats/inc! :fs-ns nsec)
                (cast/metric* cast/instance {:name :efs.hits, :value (if ok? 1 0), :units :count}))
              (cast/metric*
                cast/instance
                {:name (get (if ok? success-metrics failure-metrics) op),
                 :value (java.lang.Double/valueOf (double (io-stats/ns->ms nsec))),
                 :units :msec,
                 :datomic.core2.val-store.fs/key k})
              (when (canom/anom result)
                (cast/event*
                  cast/instance
                  (assoc result :msg "FS cache op failed" :datomic.core2.val-store.fs/k k)))
              result))))))
  (reset-meta!
    #'wrap-metric-handler
    (assoc
      {:arglists (clojure.core/list ['f 'k 'op]), :column (int 1)}
      :name
      'wrap-metric-handler
      :ns
      *ns*))
  (defn wrap-op
    ([f k op]
      (wrap-metric-handler
        (fn fn__22119
          ([& args]
            (try
              (apply f args)
              (catch
                java.lang.Throwable
                t__20198__auto__
                (izer/throwable->anom t__20198__auto__)))))
        k
        op)))
  (reset-meta!
    #'wrap-op
    (assoc {:arglists (clojure.core/list ['f 'k 'op]), :column (int 1)} :name 'wrap-op :ns *ns*))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      Impl
      (-file-path [_ k opts])
      (-sync-get [_ k opts] "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly.")
      (-sync-put [_ k v opts] "Returns {:result :created} or anomaly."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.fs" "Impl")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'Impl :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-file-path
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.fs" "Impl"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.fs" "-file-path")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*)))
    (let [protocol_signature__7434 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-sync-get
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc
                                      "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.fs" "Impl"))
          protocol_method_name__7435 (with-meta
                                       (:name protocol_signature__7434)
                                       protocol_signature__7434)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.fs" "-sync-get")
        (assoc protocol_signature__7434 :name protocol_method_name__7435 :ns *ns*)))
    (let [protocol_signature__7436 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-sync-put
                                        {:arglists (clojure.core/list ['_ 'k 'v 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'v 'opts]),
                                      :doc "Returns {:result :created} or anomaly."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.fs" "Impl"))
          protocol_method_name__7437 (with-meta
                                       (:name protocol_signature__7436)
                                       protocol_signature__7436)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.fs" "-sync-put")
        (assoc protocol_signature__7436 :name protocol_method_name__7437 :ns *ns*))))
  (.setMeta
    (clojure.lang.RT/var "datomic.core2.val-store.fs" "SYNC_PUT_OPEN_OPTIONS")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.core2.val-store.fs" "SYNC_PUT_OPEN_OPTIONS")
    (into-array
      java.nio.file.StandardOpenOption
      [StandardOpenOption/TRUNCATE_EXISTING StandardOpenOption/WRITE StandardOpenOption/CREATE]))
  (.setMeta
    (clojure.lang.RT/var "datomic.core2.val-store.fs" "SYNC_GET_READ_OPTION")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.core2.val-store.fs" "SYNC_GET_READ_OPTION")
    (into-array java.nio.file.StandardOpenOption [StandardOpenOption/READ]))
  (.setMeta
    (clojure.lang.RT/var "datomic.core2.val-store.fs" "CREATE_DIRECTORIES_OPTS")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.core2.val-store.fs" "CREATE_DIRECTORIES_OPTS")
    (into-array java.nio.file.attribute.FileAttribute []))
  (deftype
    FS
    [get_pool put_pool path delete_pool]
    datomic.core2.val_store.spi.Get
    datomic.core2.val_store.fs.Impl
    datomic.core2.val_store.spi.Delete
    datomic.core2.val_store.spi.Put
    (-put
      [this k v opts]
      (try
        (datomic.core2.thread/pfuture-ch
          (wrap-op (fn fn__22190 ([p1__22180#] (-sync-put this p1__22180# v opts))) k :put)
          put_pool)
        (catch
          java.util.concurrent.RejectedExecutionException
          rje
          (doto
            (clojure.core.async/chan 1)
            (clojure.core.async/offer!
              {:cognitect.anomalies/category :cognitect.anomalies/busy,
               :cognitect.anomalies/message "EFS Write Queue Full",
               :cause (izer/throwable->anom rje)})
            (clojure.core.async/close!)))))
    (-get
      [this k opts]
      (do
        (io-stats/inc! :fs)
        (datomic.core2.thread/pfuture-ch
          (wrap-op (fn fn__22188 ([p1__22179#] (-sync-get this p1__22179# opts))) k :get)
          get_pool)))
    (-delete
      [this k opts]
      (datomic.core2.thread/pfuture-ch
        (wrap-op
          (fn fn__22186
            ([p1__22178#]
              (let [f (-file-path this p1__22178# opts)]
                (.delete ^java.io.File f)
                {:result :deleted})))
          k
          :delete)
        delete_pool))
    (-sync-put
      [this k v opts]
      (or
        (spi/no-val-error k v)
        (let [new_file (.toPath (-file-path this k opts))
              doit (fn doit
                     ([]
                       (with-open [fc (FileChannel/open
                                        ^java.nio.file.Path new_file
                                        SYNC_PUT_OPEN_OPTIONS)]
                         (do
                           (bbuf/write-buffer (:val v) fc)
                           (.force
                             ^java.nio.channels.FileChannel fc
                             (boolean (.booleanValue true)))
                           nil))))]
          (try
            (^clojure.lang.IFn doit)
            (catch
              java.nio.file.NoSuchFileException
              _
              (do
                (Files/createDirectories
                  (.getParent ^java.nio.file.Path new_file)
                  CREATE_DIRECTORIES_OPTS)
                (^clojure.lang.IFn doit))))
          {:result :created})))
    (-sync-get
      [this k opts]
      (let [f (-file-path this k opts) len (.length ^java.io.File f)]
        (if (not (zero? len))
          (with-open [fc (FileChannel/open (.toPath ^java.io.File f) SYNC_GET_READ_OPTION)]
            {:val (bbuf/read-n-bytes (long len) fc)})
          {:val nil})))
    (-file-path
      [this k opts]
      (if (= :skip (:datomic.core2.val-store.opts/partition opts))
        (io/file path k)
        (io/file path (spi/splice-partition-key k (spi/partition-key k))))))
  (clojure.core/import 'datomic.core2.val_store.fs.FS)
  (def ->FS
   (fn __GT_FS
     ([get_pool put_pool path delete_pool]
       (datomic.core2.val_store.fs.FS. get_pool put_pool path delete_pool))))
  (reset-meta!
    #'->FS
    (assoc
      {:arglists (clojure.core/list ['get-pool 'put-pool 'path 'delete-pool]), :column (int 1)}
      :name
      '->FS
      :ns
      *ns*))
  (def create
   (fn create
     ([p__22198]
       (let [map__22199 p__22198
             map__22199 (if (seq? map__22199)
                          (if (next map__22199)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__22199))
                            (if (seq map__22199) (first map__22199) {}))
                          map__22199)
             delete_pool (get map__22199 :delete-pool)
             get_pool (get map__22199 :get-pool)
             path (get map__22199 :path)
             put_pool (get map__22199 :put-pool)]
         (when-not (and delete_pool get_pool path put_pool)
           (throw
             (java.lang.AssertionError.
               (str
                 "Assert failed: "
                 (pr-str (clojure.core/list 'and 'delete-pool 'get-pool 'path 'put-pool))))))
         (->FS get_pool put_pool path delete_pool)))))
  (reset-meta!
    #'create
    (assoc
      {:arglists (clojure.core/list [{:keys ['delete-pool 'get-pool 'path 'put-pool]}]),
       :column (int 1)}
      :name
      'create
      :ns
      *ns*)))
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
  (def failure-metrics
   {:get :efs.get.failed.msec, :put :efs.put.failed.msec, :delete :efs.delete.failed.msec})
  (defn wrap-metric-handler
    ([f k op]
      (let [start_nsec (java.lang.System/nanoTime)]
        (fn fn__21259
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
  (defn wrap-op
    ([f k op]
      (wrap-metric-handler
        (fn fn__21262
          ([& args]
            (try
              (apply f args)
              (catch java.lang.Throwable t__19459__auto__ (canom/fault t__19459__auto__)))))
        k
        op)))
  (defonce Impl {})
  (defprotocol Impl (-file-path [_ k opts]) (-sync-get [_ k opts]) (-sync-put [_ k v opts]))
  (def SYNC_PUT_OPEN_OPTIONS
   (into-array
     java.nio.file.StandardOpenOption
     [StandardOpenOption/TRUNCATE_EXISTING StandardOpenOption/WRITE StandardOpenOption/CREATE]))
  (def SYNC_GET_READ_OPTION
   (into-array java.nio.file.StandardOpenOption [StandardOpenOption/READ]))
  (def CREATE_DIRECTORIES_OPTS (into-array java.nio.file.attribute.FileAttribute []))
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
          (wrap-op (fn fn__21333 ([p1__21323#] (-sync-put this p1__21323# v opts))) k :put)
          put_pool)
        (catch
          java.util.concurrent.RejectedExecutionException
          _
          (doto
            (clojure.core.async/chan 1)
            (clojure.core.async/offer!
              #:cognitect.anomalies{:category :cognitect.anomalies/busy,
                                    :message "EFS Write Queue Full"})
            (clojure.core.async/close!)))))
    (-get
      [this k opts]
      (do
        (io-stats/inc! :fs)
        (datomic.core2.thread/pfuture-ch
          (wrap-op (fn fn__21331 ([p1__21322#] (-sync-get this p1__21322# opts))) k :get)
          get_pool)))
    (-delete
      [this k opts]
      (datomic.core2.thread/pfuture-ch
        (wrap-op
          (fn fn__21329
            ([p1__21321#]
              (let [f (-file-path this p1__21321# opts)]
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
  (defn ->FS
    ([get_pool put_pool path delete_pool]
      (datomic.core2.val_store.fs.FS. get_pool put_pool path delete_pool)))
  (defn create
    ([p__21341]
      (let [map__21342 p__21341
            map__21342 (if (seq? map__21342)
                         (if (next map__21342)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21342))
                           (if (seq map__21342) (first map__21342) {}))
                         map__21342)
            delete_pool (get map__21342 :delete-pool)
            get_pool (get map__21342 :get-pool)
            path (get map__21342 :path)
            put_pool (get map__21342 :put-pool)]
        (when-not (and delete_pool get_pool path put_pool)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str (clojure.core/list 'and 'delete-pool 'get-pool 'path 'put-pool))))))
        (->FS get_pool put_pool path delete_pool)))))
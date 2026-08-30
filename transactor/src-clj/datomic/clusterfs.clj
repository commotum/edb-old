(do
  (clojure.core/in-ns 'datomic.clusterfs)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'jio]
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger]
        ['datomic.fressian :as 'fressian]
        ['datomic.io :as 'io])
      (clojure.core/import 'java.io.IOException)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)))
  (when-not (.equals 'datomic.clusterfs 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.clusterfs))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'jio]
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger]
          ['datomic.fressian :as 'fressian]
          ['datomic.io :as 'io])
        (clojure.core/import 'java.io.IOException)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'org.fressian.handlers.WriteHandler)
        (clojure.core/import 'org.fressian.handlers.ReadHandler))))
  (set! *warn-on-reflection* true)
  (defn chunk-path ([path chunk] (str path "_" (format "%015X" chunk))))
  (reset-meta!
    #'chunk-path
    (assoc
      {:arglists (clojure.core/list ['path 'chunk]), :column (int 1)}
      :name
      'chunk-path
      :ns
      *ns*))
  (def floor (fn floor (^long [num] (long (java.lang.Math/floor (double num))))))
  (reset-meta!
    #'floor
    (assoc
      {:arglists (clojure.core/list (.withMeta ['num] {:tag 'long})), :column (int 1)}
      :name
      'floor
      :ns
      *ns*))
  (def ceil (fn ceil (^long [num] (long (java.lang.Math/ceil (double num))))))
  (reset-meta!
    #'ceil
    (assoc
      {:arglists (clojure.core/list (.withMeta ['num] {:tag 'long})), :column (int 1)}
      :name
      'ceil
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.clusterfs" "->Chunk") {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.clusterfs" "map->Chunk")
    {:declared true, :column (int 1)})
  (defrecord Chunk [^bytes chunk])
  (clojure.core/import 'datomic.clusterfs.Chunk)
  (defn ->Chunk ([chunk] (datomic.clusterfs.Chunk. chunk)))
  (reset-meta!
    #'->Chunk
    (assoc {:arglists (clojure.core/list ['chunk]), :column (int 1)} :name '->Chunk :ns *ns*))
  (defn map->Chunk
    ([m__7972__auto__]
      (Chunk/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->Chunk
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->Chunk
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.clusterfs" "->ClusterFS")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.clusterfs" "map->ClusterFS")
    {:declared true, :column (int 1)})
  (defrecord
    ClusterFS
    [dir ^int chunk-size]
    datomic.impl.clusterfs.IClusterFS
    (^bytes getChunk
      [this ^clojure.lang.ILookup olookup ^java.lang.String filekey ^int chunkno]
      (let [prefix (get-in dir [filekey :base])
            temp__5802__auto__ (get
                                 olookup
                                 (chunk-path prefix (java.lang.Integer/valueOf (int chunkno))))]
        (if temp__5802__auto__
          (let [ck temp__5802__auto__] (.-chunk ^datomic.clusterfs.Chunk ck))
          (do
            (throw
              (java.lang.Error.
                (str
                  "Unable to read "
                  filekey
                  " from "
                  (chunk-path prefix (java.lang.Integer/valueOf (int chunkno))))))
            nil))))
    (^int chunkSize [this] chunk-size)
    (^long fileLength [this ^java.lang.String f] (.longValue (get-in dir [f :length])))
    (^java.util.Collection getFiles [this] (keys dir)))
  (clojure.core/import 'datomic.clusterfs.ClusterFS)
  (def ->ClusterFS
   (fn __GT_ClusterFS
     ([dir chunk_size] (datomic.clusterfs.ClusterFS. dir (int ^java.lang.Number chunk_size)))))
  (reset-meta!
    #'->ClusterFS
    (assoc
      {:arglists (clojure.core/list ['dir 'chunk-size]), :column (int 1)}
      :name
      '->ClusterFS
      :ns
      *ns*))
  (defn map->ClusterFS
    ([m__7972__auto__]
      (ClusterFS/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->ClusterFS
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->ClusterFS
      :ns
      *ns*))
  (defn files ([clusterfs] (.getFiles ^datomic.impl.clusterfs.IClusterFS clusterfs)))
  (reset-meta!
    #'files
    (assoc {:arglists (clojure.core/list ['clusterfs]), :column (int 1)} :name 'files :ns *ns*))
  (def file-chunk-keys
   (fn file_chunk_keys
     ([clusterfs file]
       (let [length (.fileLength ^datomic.clusterfs.ClusterFS clusterfs ^java.lang.String file)
             n (ceil (/ length (.chunkSize ^datomic.clusterfs.ClusterFS clusterfs)))
             prefix (cluster/uuid->val-key
                      (get-in (.-dir ^datomic.clusterfs.ClusterFS clusterfs) [file :base]))]
         (map (partial chunk-path prefix) (range (long n)))))))
  (reset-meta!
    #'file-chunk-keys
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'clusterfs {:tag 'ClusterFS}) 'file]),
       :column (int 1)}
      :name
      'file-chunk-keys
      :ns
      *ns*))
  (defn chunk-keys ([clusterfs files] (mapcat (partial file-chunk-keys clusterfs) files)))
  (reset-meta!
    #'chunk-keys
    (assoc
      {:arglists (clojure.core/list ['clusterfs 'files]), :column (int 1)}
      :name
      'chunk-keys
      :ns
      *ns*))
  (defn all-keys ([clusterfs] (chunk-keys clusterfs (files clusterfs))))
  (reset-meta!
    #'all-keys
    (assoc {:arglists (clojure.core/list ['clusterfs]), :column (int 1)} :name 'all-keys :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.clusterfs" "write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.clusterfs" "write-handlers")
    (merge
      fressian/clojure-write-handlers
      {datomic.clusterfs.Chunk
       {"clusterfs-chunk"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w o]
            (do
              (let [c o]
                (.writeTag ^org.fressian.Writer w "clusterfs-chunk" (int 1))
                (.writeObject ^org.fressian.Writer w (.-chunk ^datomic.clusterfs.Chunk c)))
              nil)))},
       datomic.clusterfs.ClusterFS
       {"clusterfs-root"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w o]
            (do
              (let [cfs o]
                (.writeTag ^org.fressian.Writer w "clusterfs-root" (int 2))
                (.writeObject ^org.fressian.Writer w (.-dir ^datomic.clusterfs.ClusterFS cfs))
                (.writeObject
                  ^org.fressian.Writer w
                  (java.lang.Integer/valueOf (int (.chunkSize ^datomic.clusterfs.ClusterFS cfs)))))
              nil)))}}))
  (.setMeta (clojure.lang.RT/var "datomic.clusterfs" "read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.clusterfs" "read-handlers")
    (merge
      fressian/clojure-read-handlers
      {"clusterfs-chunk"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (datomic.clusterfs.Chunk. (.readObject ^org.fressian.Reader rdr)))),
       "clusterfs-root"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (datomic.clusterfs.ClusterFS.
             (.readObject ^org.fressian.Reader rdr)
             (int (.readInt ^org.fressian.Reader rdr)))))}))
  (defn fressian-chunk-from-channel
    ([rc n]
      (fressian/fressian-val
        (->Chunk (io/alias-buf-bytes (io/read-n-bytes n rc)))
        write-handlers)))
  (reset-meta!
    #'fressian-chunk-from-channel
    (assoc
      {:arglists (clojure.core/list ['rc 'n]), :column (int 1)}
      :name
      'fressian-chunk-from-channel
      :ns
      *ns*))
  (def create-file
   (fn create_file
     ([cs local_file & p__12542]
       (let [map__12543 p__12542
             map__12543 (if (seq? map__12543)
                          (if (next map__12543)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__12543))
                            (if (seq map__12543) (first map__12543) {}))
                          map__12543)
             chunk_size (get map__12543 :chunk-size)
             base (common/rand-uuid)
             prefix (cluster/uuid->val-key base)
             size (.length (jio/file local_file))
             fullchunks (floor (/ size chunk_size))
             tailsize (rem size chunk_size)
             m_12544 {:event :clusterfs/create-file,
                      :size (long size),
                      :local-file local_file,
                      :prefix prefix,
                      :chunks (long (ceil (/ size chunk_size)))}
             ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                 (.info
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_12544 :phase :begin))))
                               nil)
             start__8584__auto__ (java.lang.System/nanoTime)
             result__8585__auto__ (try
                                    {:returned
                                     (with-open [s (java.io.FileInputStream.
                                                     (jio/file local_file))]
                                       (do
                                         (cluster/write-vals
                                           cs
                                           :clusterfs
                                           (map
                                             (fn fn__12549
                                               ([chunkno]
                                                 [(chunk-path prefix chunkno)
                                                  (fressian-chunk-from-channel
                                                    (.getChannel ^java.io.FileInputStream s)
                                                    chunk_size)]))
                                             (range (long fullchunks))))
                                         (when-not (zero? tailsize)
                                           (cluster/write-vals
                                             cs
                                             :clusterfs
                                             {(chunk-path prefix (long fullchunks))
                                              (fressian-chunk-from-channel
                                                (.getChannel ^java.io.FileInputStream s)
                                                tailsize)}))
                                         base))}
                                    (catch
                                      java.lang.Throwable
                                      t__8586__auto__
                                      {:threw t__8586__auto__}))
             elapsed_12545 (- (java.lang.System/nanoTime) start__8584__auto__)
             msec_12546 (logger/format-as-msec (long elapsed_12545))]
         (let [endmsg__8587__auto__ (merge
                                      (assoc m_12544 :msec msec_12546 :phase :end)
                                      (when (:threw result__8585__auto__)
                                        {:threw (class (:threw result__8585__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
           nil)
         (if (contains? result__8585__auto__ :returned)
           (:returned result__8585__auto__)
           (do (throw (:threw result__8585__auto__)) nil))))))
  (reset-meta!
    #'create-file
    (assoc
      {:arglists (clojure.core/list ['cs 'local-file '& {:keys ['chunk-size]}]), :column (int 1)}
      :name
      'create-file
      :ns
      *ns*))
  (def create-files
   (fn create_files
     ([cs files & p__12558]
       (let [map__12559 p__12558
             map__12559 (if (seq? map__12559)
                          (if (next map__12559)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__12559))
                            (if (seq map__12559) (first map__12559) {}))
                          map__12559)
             chunk_size (get map__12559 :chunk-size)
             m_12560 {:event :clusterfs/create-files,
                      :count (java.lang.Integer/valueOf (int (count files)))}
             ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_12560 :phase :begin))))
                               nil)
             start__8584__auto__ (java.lang.System/nanoTime)
             result__8585__auto__ (try
                                    {:returned
                                     (reduce
                                       (fn fn__12565
                                         ([m p__12564]
                                           (let [vec__12566 p__12564
                                                 lname (nth vec__12566 (int 0) nil)
                                                 cname (nth vec__12566 (int 1) nil)
                                                 cname (jio/file cname)]
                                             (assoc
                                               m
                                               (.getPath ^java.io.File cname)
                                               {:base
                                                (or
                                                  (create-file cs lname :chunk-size chunk_size)
                                                  (do
                                                    (throw
                                                      (java.lang.Error.
                                                        (str
                                                          "Unable to create clusterfs file "
                                                          lname)))
                                                    1)),
                                                :length (long (.length (jio/file lname)))}))))
                                       {}
                                       files)}
                                    (catch
                                      java.lang.Throwable
                                      t__8586__auto__
                                      {:threw t__8586__auto__}))
             elapsed_12561 (- (java.lang.System/nanoTime) start__8584__auto__)
             msec_12562 (logger/format-as-msec (long elapsed_12561))]
         (let [endmsg__8587__auto__ (merge
                                      (assoc m_12560 :msec msec_12562 :phase :end)
                                      (when (:threw result__8585__auto__)
                                        {:threw (class (:threw result__8585__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
           nil)
         (if (contains? result__8585__auto__ :returned)
           (:returned result__8585__auto__)
           (do (throw (:threw result__8585__auto__)) nil))))))
  (reset-meta!
    #'create-files
    (assoc
      {:arglists (clojure.core/list ['cs 'files '& {:keys ['chunk-size]}]), :column (int 1)}
      :name
      'create-files
      :ns
      *ns*))
  (defn describe
    ([cfs]
      (let [d (:dir cfs)]
        {:file-count (java.lang.Integer/valueOf (int (count d))),
         :seg-count
         (reduce
           +
           (map
             (fn fn__12578
               ([p1__12577#]
                 (long
                   (ceil
                     (java.lang.Double/valueOf
                       (double (/ (float (:length p1__12577#)) (:chunk-size cfs))))))))
             (vals d))),
         :byte-count (reduce + (map :length (vals d)))})))
  (reset-meta!
    #'describe
    (assoc {:arglists (clojure.core/list ['cfs]), :column (int 1)} :name 'describe :ns *ns*))
  (def create-fs
   (fn create_fs
     ([cs files & p__12581]
       (let [map__12582 p__12581
             map__12582 (if (seq? map__12582)
                          (if (next map__12582)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__12582))
                            (if (seq map__12582) (first map__12582) {}))
                          map__12582)
             chunk_size (get map__12582 :chunk-size)
             base (get map__12582 :base)
             dirid (common/rand-uuid)
             m_12583 {:event :clusterfs/create-fs, :uuid dirid}
             ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_12583 :phase :begin))))
                               nil)
             start__8584__auto__ (java.lang.System/nanoTime)
             result__8585__auto__ (try
                                    {:returned
                                     (let [dir (merge
                                                 base
                                                 (create-files cs files :chunk-size chunk_size))
                                           cfs (datomic.clusterfs.ClusterFS.
                                                 dir
                                                 (int ^java.lang.Number chunk_size))
                                           result (cluster/create-val
                                                    cs
                                                    (cluster/uuid->val-key dirid)
                                                    (fressian/fressian-val cfs write-handlers))]
                                       (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.clusterfs")]
                                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                           (.info
                                             ^org.slf4j.Logger logger
                                             (logger/process
                                               (assoc
                                                 (describe cfs)
                                                 :event
                                                 :clusterfs/create-fs
                                                 :uuid
                                                 dirid))))
                                         nil)
                                       (when (= :created (deref result)) dirid))}
                                    (catch
                                      java.lang.Throwable
                                      t__8586__auto__
                                      {:threw t__8586__auto__}))
             elapsed_12584 (- (java.lang.System/nanoTime) start__8584__auto__)
             msec_12585 (logger/format-as-msec (long elapsed_12584))]
         (monitor/add-stat :StorageCreateFSMsec msec_12585)
         (let [endmsg__8587__auto__ (merge
                                      (assoc m_12583 :msec msec_12585 :phase :end)
                                      (when (:threw result__8585__auto__)
                                        {:threw (class (:threw result__8585__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
           nil)
         (if (contains? result__8585__auto__ :returned)
           (:returned result__8585__auto__)
           (do (throw (:threw result__8585__auto__)) nil))))))
  (reset-meta!
    #'create-fs
    (assoc
      {:arglists (clojure.core/list ['cs 'files '& {:keys ['chunk-size 'base]}]), :column (int 1)}
      :name
      'create-fs
      :ns
      *ns*)))
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
    ([m__8001__auto__]
      (Chunk/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->Chunk
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
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
            temp__5823__auto__ (get
                                 olookup
                                 (chunk-path prefix (java.lang.Integer/valueOf (int chunkno))))]
        (if temp__5823__auto__
          (let [ck temp__5823__auto__] (.-chunk ^datomic.clusterfs.Chunk ck))
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
    ([m__8001__auto__]
      (ClusterFS/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->ClusterFS
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
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
     ([cs local_file & p__13728]
       (let [map__13729 p__13728
             map__13729 (if (seq? map__13729)
                          (if (next map__13729)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__13729))
                            (if (seq map__13729) (first map__13729) {}))
                          map__13729)
             chunk_size (get map__13729 :chunk-size)
             base (common/rand-uuid)
             prefix (cluster/uuid->val-key base)
             size (.length (jio/file local_file))
             fullchunks (floor (/ size chunk_size))
             tailsize (rem size chunk_size)
             m_13730 {:event :clusterfs/create-file,
                      :size (long size),
                      :local-file local_file,
                      :prefix prefix,
                      :chunks (long (ceil (/ size chunk_size)))}
             ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                 (.info
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_13730 :phase :begin))))
                               nil)
             start__8599__auto__ (java.lang.System/nanoTime)
             result__8600__auto__ (try
                                    {:returned
                                     (with-open [s (java.io.FileInputStream.
                                                     (jio/file local_file))]
                                       (do
                                         (cluster/write-vals
                                           cs
                                           :clusterfs
                                           (map
                                             (fn fn__13735
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
                                      t__8601__auto__
                                      {:threw t__8601__auto__}))
             elapsed_13731 (- (java.lang.System/nanoTime) start__8599__auto__)
             msec_13732 (logger/format-as-msec (long elapsed_13731))]
         (let [endmsg__8602__auto__ (merge
                                      (assoc m_13730 :msec msec_13732 :phase :end)
                                      (when (:threw result__8600__auto__)
                                        {:threw (class (:threw result__8600__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
           (when (.isInfoEnabled ^org.slf4j.Logger logger)
             (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
           nil)
         (if (contains? result__8600__auto__ :returned)
           (:returned result__8600__auto__)
           (do (throw (:threw result__8600__auto__)) nil))))))
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
     ([cs files & p__13744]
       (let [map__13745 p__13744
             map__13745 (if (seq? map__13745)
                          (if (next map__13745)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__13745))
                            (if (seq map__13745) (first map__13745) {}))
                          map__13745)
             chunk_size (get map__13745 :chunk-size)
             m_13746 {:event :clusterfs/create-files,
                      :count (java.lang.Integer/valueOf (int (count files)))}
             ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_13746 :phase :begin))))
                               nil)
             start__8599__auto__ (java.lang.System/nanoTime)
             result__8600__auto__ (try
                                    {:returned
                                     (reduce
                                       (fn fn__13751
                                         ([m p__13750]
                                           (let [vec__13752 p__13750
                                                 lname (nth vec__13752 (int 0) nil)
                                                 cname (nth vec__13752 (int 1) nil)
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
                                      t__8601__auto__
                                      {:threw t__8601__auto__}))
             elapsed_13747 (- (java.lang.System/nanoTime) start__8599__auto__)
             msec_13748 (logger/format-as-msec (long elapsed_13747))]
         (let [endmsg__8602__auto__ (merge
                                      (assoc m_13746 :msec msec_13748 :phase :end)
                                      (when (:threw result__8600__auto__)
                                        {:threw (class (:threw result__8600__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
           nil)
         (if (contains? result__8600__auto__ :returned)
           (:returned result__8600__auto__)
           (do (throw (:threw result__8600__auto__)) nil))))))
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
             (fn fn__13764
               ([p1__13763#]
                 (long
                   (ceil
                     (java.lang.Double/valueOf
                       (double (/ (float (:length p1__13763#)) (:chunk-size cfs))))))))
             (vals d))),
         :byte-count (reduce + (map :length (vals d)))})))
  (reset-meta!
    #'describe
    (assoc {:arglists (clojure.core/list ['cfs]), :column (int 1)} :name 'describe :ns *ns*))
  (def create-fs
   (fn create_fs
     ([cs files & p__13767]
       (let [map__13768 p__13767
             map__13768 (if (seq? map__13768)
                          (if (next map__13768)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__13768))
                            (if (seq map__13768) (first map__13768) {}))
                          map__13768)
             chunk_size (get map__13768 :chunk-size)
             base (get map__13768 :base)
             dirid (common/rand-uuid)
             m_13769 {:event :clusterfs/create-fs, :uuid dirid}
             ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_13769 :phase :begin))))
                               nil)
             start__8599__auto__ (java.lang.System/nanoTime)
             result__8600__auto__ (try
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
                                      t__8601__auto__
                                      {:threw t__8601__auto__}))
             elapsed_13770 (- (java.lang.System/nanoTime) start__8599__auto__)
             msec_13771 (logger/format-as-msec (long elapsed_13770))]
         (monitor/add-stat :StorageCreateFSMsec msec_13771)
         (let [endmsg__8602__auto__ (merge
                                      (assoc m_13769 :msec msec_13771 :phase :end)
                                      (when (:threw result__8600__auto__)
                                        {:threw (class (:threw result__8600__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
           nil)
         (if (contains? result__8600__auto__ :returned)
           (:returned result__8600__auto__)
           (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'create-fs
    (assoc
      {:arglists (clojure.core/list ['cs 'files '& {:keys ['chunk-size 'base]}]), :column (int 1)}
      :name
      'create-fs
      :ns
      *ns*)))
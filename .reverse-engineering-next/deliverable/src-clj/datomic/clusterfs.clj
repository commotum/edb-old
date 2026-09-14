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
  (defn floor (^long [num] (long (java.lang.Math/floor (double num)))))
  (defn ceil (^long [num] (long (java.lang.Math/ceil (double num)))))
  (declare ->Chunk)
  (declare map->Chunk)
  (defrecord Chunk [^bytes chunk])
  (clojure.core/import 'datomic.clusterfs.Chunk)
  (defn ->Chunk ([chunk] (datomic.clusterfs.Chunk. chunk)))
  (defn map->Chunk
    ([m__7585__auto__]
      (Chunk/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (declare ->ClusterFS)
  (declare map->ClusterFS)
  (defrecord
    ClusterFS
    [dir ^int chunk-size]
    datomic.impl.clusterfs.IClusterFS
    (^bytes getChunk
      [this ^clojure.lang.ILookup olookup ^java.lang.String filekey ^int chunkno]
      (let [prefix (get-in dir [filekey :base])
            temp__5455__auto__ (get
                                 olookup
                                 (chunk-path prefix (java.lang.Integer/valueOf (int chunkno))))]
        (if temp__5455__auto__
          (let [ck temp__5455__auto__] (.-chunk ^datomic.clusterfs.Chunk ck))
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
  (defn ->ClusterFS
    ([dir chunk_size] (datomic.clusterfs.ClusterFS. dir (int ^java.lang.Number chunk_size))))
  (defn map->ClusterFS
    ([m__7585__auto__]
      (ClusterFS/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn files ([clusterfs] (.getFiles ^datomic.impl.clusterfs.IClusterFS clusterfs)))
  (defn file-chunk-keys
    ([clusterfs file]
      (let [length (.fileLength ^datomic.clusterfs.ClusterFS clusterfs ^java.lang.String file)
            n (ceil (/ length (.chunkSize ^datomic.clusterfs.ClusterFS clusterfs)))
            prefix (cluster/uuid->val-key
                     (get-in (.-dir ^datomic.clusterfs.ClusterFS clusterfs) [file :base]))]
        (map (partial chunk-path prefix) (range (long n))))))
  (defn chunk-keys ([clusterfs files] (mapcat (partial file-chunk-keys clusterfs) files)))
  (defn all-keys ([clusterfs] (chunk-keys clusterfs (files clusterfs))))
  (def write-handlers
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
  (def read-handlers
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
  (defn create-file
    ([cs local_file & p__14243]
      (let [map__14244 p__14243
            map__14244 (if (seq? map__14244)
                         (clojure.lang.PersistentHashMap/create (seq map__14244))
                         map__14244)
            chunk_size (get map__14244 :chunk-size)
            base (common/rand-uuid)
            prefix (cluster/uuid->val-key base)
            size (.length (jio/file local_file))
            fullchunks (floor (/ size chunk_size))
            tailsize (rem size chunk_size)
            m_14245 {:event :clusterfs/create-file,
                     :size (long size),
                     :local-file local_file,
                     :prefix prefix,
                     :chunks (long (ceil (/ size chunk_size)))}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_14245 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [s (java.io.FileInputStream. (jio/file local_file))]
                                      (try
                                        (do
                                          (cluster/write-vals
                                            cs
                                            :clusterfs
                                            (map
                                              (fn fn__14250
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
                                          base)
                                        (finally (do (.close ^java.io.FileInputStream s) nil))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_14246 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_14247 (logger/format-as-msec (long elapsed_14246))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_14245 :msec msec_14247 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn create-files
    ([cs files & p__14259]
      (let [map__14260 p__14259
            map__14260 (if (seq? map__14260)
                         (clojure.lang.PersistentHashMap/create (seq map__14260))
                         map__14260)
            chunk_size (get map__14260 :chunk-size)
            m_14261 {:event :clusterfs/create-files,
                     :count (java.lang.Integer/valueOf (int (count files)))}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_14261 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (reduce
                                      (fn fn__14266
                                        ([m p__14265]
                                          (let [vec__14267 p__14265
                                                lname (nth vec__14267 (int 0) nil)
                                                cname (nth vec__14267 (int 1) nil)
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
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_14262 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_14263 (logger/format-as-msec (long elapsed_14262))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_14261 :msec msec_14263 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn describe
    ([cfs]
      (let [d (:dir cfs)]
        {:file-count (java.lang.Integer/valueOf (int (count d))),
         :seg-count
         (reduce
           +
           (map
             (fn fn__14279
               ([p1__14278#]
                 (long
                   (ceil
                     (java.lang.Double/valueOf
                       (double (/ (float (:length p1__14278#)) (:chunk-size cfs))))))))
             (vals d))),
         :byte-count (reduce + (map :length (vals d)))})))
  (defn create-fs
    ([cs files & p__14282]
      (let [map__14283 p__14282
            map__14283 (if (seq? map__14283)
                         (clojure.lang.PersistentHashMap/create (seq map__14283))
                         map__14283)
            chunk_size (get map__14283 :chunk-size)
            base (get map__14283 :base)
            dirid (common/rand-uuid)
            m_14284 {:event :clusterfs/create-fs, :uuid dirid}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_14284 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
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
                                                dirid)))
                                          nil)
                                        nil)
                                      (when (= :created (deref result)) dirid))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_14285 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_14286 (logger/format-as-msec (long elapsed_14285))]
        (monitor/add-stat :StorageCreateFSMsec msec_14286)
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_14284 :msec msec_14286 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.clusterfs")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil))))))
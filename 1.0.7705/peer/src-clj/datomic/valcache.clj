(do
  (clojure.core/in-ns 'datomic.valcache)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.valcache)
    {:doc
     "valcache implements a subset of the memcached binary protocol on top of the file system.\n per: https://github.com/memcached/memcached/wiki/BinaryProtocolRevamped\n\nNarrowing presumptions are:\n\nvalues never change\nare named by uuid-as-string keys\nare big enough to justify being stored in files (several kb +)\neviction is based on (required) threshold\nLRU, tracked with atime (use strictatime + lazytime flags when mounting)\nprovided/required dir is 'owned' by valcache, subdir structure is an impl detail\nlowish connection counts (< 1000)\n\nThe supported memcached commands are get/set/delete/noop/quit\ndata type, vbucket, cas, and expiry are not supported - non-zero values will be rejected.\n"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.async :refer (clojure.core/list 'daemon)]
        ['datomic.io :as 'io]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'java.net.InetSocketAddress)
      (clojure.core/import 'java.io.IOException)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.nio.file.CopyOption)
      (clojure.core/import 'java.nio.file.Files)
      (clojure.core/import 'java.nio.file.FileSystems)
      (clojure.core/import 'java.nio.file.Path)
      (clojure.core/import 'java.nio.file.OpenOption)
      (clojure.core/import 'java.nio.file.StandardCopyOption)
      (clojure.core/import 'java.nio.file.StandardOpenOption)
      (clojure.core/import 'java.nio.file.Files)
      (clojure.core/import 'java.nio.file.FileVisitor)
      (clojure.core/import 'java.nio.file.FileVisitResult)
      (clojure.core/import 'java.nio.file.LinkOption)
      (clojure.core/import 'java.nio.file.attribute.FileTime)
      (clojure.core/import 'java.nio.channels.AsynchronousCloseException)
      (clojure.core/import 'java.nio.channels.ServerSocketChannel)
      (clojure.core/import 'java.nio.channels.SocketChannel)
      (clojure.core/import 'java.nio.channels.FileChannel)
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
      (clojure.core/import 'java.util.concurrent.Semaphore)))
  (when-not (.equals 'datomic.valcache 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.valcache))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.async :refer (clojure.core/list 'daemon)]
          ['datomic.io :as 'io]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'java.net.InetSocketAddress)
        (clojure.core/import 'java.io.IOException)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.nio.file.CopyOption)
        (clojure.core/import 'java.nio.file.Files)
        (clojure.core/import 'java.nio.file.FileSystems)
        (clojure.core/import 'java.nio.file.Path)
        (clojure.core/import 'java.nio.file.OpenOption)
        (clojure.core/import 'java.nio.file.StandardCopyOption)
        (clojure.core/import 'java.nio.file.StandardOpenOption)
        (clojure.core/import 'java.nio.file.Files)
        (clojure.core/import 'java.nio.file.FileVisitor)
        (clojure.core/import 'java.nio.file.FileVisitResult)
        (clojure.core/import 'java.nio.file.LinkOption)
        (clojure.core/import 'java.nio.file.attribute.FileTime)
        (clojure.core/import 'java.nio.channels.AsynchronousCloseException)
        (clojure.core/import 'java.nio.channels.ServerSocketChannel)
        (clojure.core/import 'java.nio.channels.SocketChannel)
        (clojure.core/import 'java.nio.channels.FileChannel)
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'java.util.concurrent.ConcurrentHashMap)
        (clojure.core/import 'java.util.concurrent.Semaphore))))
  (set! *warn-on-reflection* true)
  (def HEADER_LENGTH 24)
  (reset-meta!
    #'HEADER_LENGTH
    (assoc {:const true, :column (int 1)} :name 'HEADER_LENGTH :ns *ns*))
  (def REQUEST_MAGIC -128)
  (reset-meta!
    #'REQUEST_MAGIC
    (assoc {:const true, :column (int 1)} :name 'REQUEST_MAGIC :ns *ns*))
  (def RESPONSE_MAGIC -127)
  (reset-meta!
    #'RESPONSE_MAGIC
    (assoc {:const true, :column (int 1)} :name 'RESPONSE_MAGIC :ns *ns*))
  (def FILE_MAGIC_LENGTH 4)
  (reset-meta!
    #'FILE_MAGIC_LENGTH
    (assoc {:const true, :column (int 1)} :name 'FILE_MAGIC_LENGTH :ns *ns*))
  (def FILE_MAGIC 2048)
  (reset-meta! #'FILE_MAGIC (assoc {:const true, :column (int 1)} :name 'FILE_MAGIC :ns *ns*))
  (def DIRS 4096)
  (reset-meta! #'DIRS (assoc {:const true, :column (int 1)} :name 'DIRS :ns *ns*))
  (def MEG 1048576)
  (reset-meta! #'MEG (assoc {:const true, :column (int 1)} :name 'MEG :ns *ns*))
  (def BLOCK_SIZE_WASTE 2048)
  (reset-meta!
    #'BLOCK_SIZE_WASTE
    (assoc {:const true, :column (int 1)} :name 'BLOCK_SIZE_WASTE :ns *ns*))
  (def GET 0)
  (reset-meta! #'GET (assoc {:const true, :column (int 1)} :name 'GET :ns *ns*))
  (def SET 1)
  (reset-meta! #'SET (assoc {:const true, :column (int 1)} :name 'SET :ns *ns*))
  (def DELETE 4)
  (reset-meta! #'DELETE (assoc {:const true, :column (int 1)} :name 'DELETE :ns *ns*))
  (def QUIT 7)
  (reset-meta! #'QUIT (assoc {:const true, :column (int 1)} :name 'QUIT :ns *ns*))
  (def NOOP 10)
  (reset-meta! #'NOOP (assoc {:const true, :column (int 1)} :name 'NOOP :ns *ns*))
  (def SASL_AUTH 33)
  (reset-meta! #'SASL_AUTH (assoc {:const true, :column (int 1)} :name 'SASL_AUTH :ns *ns*))
  (def SASL_STEP 34)
  (reset-meta! #'SASL_STEP (assoc {:const true, :column (int 1)} :name 'SASL_STEP :ns *ns*))
  (def NOT_FOUND 1)
  (reset-meta! #'NOT_FOUND (assoc {:const true, :column (int 1)} :name 'NOT_FOUND :ns *ns*))
  (def INVALID 4)
  (reset-meta! #'INVALID (assoc {:const true, :column (int 1)} :name 'INVALID :ns *ns*))
  (def UNAUTHORIZED 32)
  (reset-meta! #'UNAUTHORIZED (assoc {:const true, :column (int 1)} :name 'UNAUTHORIZED :ns *ns*))
  (def NOT_SUPPORTED 131)
  (reset-meta!
    #'NOT_SUPPORTED
    (assoc {:const true, :column (int 1)} :name 'NOT_SUPPORTED :ns *ns*))
  (defn read-header
    ([sc bb]
      (io/read-into-buffer bb 24 sc)
      (let [magic (.get ^java.nio.ByteBuffer bb)]
        (when-not (= -128 (long (java.lang.Byte/valueOf (byte magic))))
          (throw
            (java.io.IOException. (str "Framing error: " (java.lang.Byte/valueOf (byte magic)))))))
      (let [opcode (.get ^java.nio.ByteBuffer bb)
            key_length (.getShort ^java.nio.ByteBuffer bb)
            extras_length (.get ^java.nio.ByteBuffer bb)
            data_type (.get ^java.nio.ByteBuffer bb)
            vbucket_id (.getShort ^java.nio.ByteBuffer bb)
            total_body_length (.getInt ^java.nio.ByteBuffer bb)
            opaque (.getInt ^java.nio.ByteBuffer bb)
            cas (.getLong ^java.nio.ByteBuffer bb)]
        {:opcode (java.lang.Byte/valueOf (byte opcode)),
         :key-length (java.lang.Short/valueOf (short key_length)),
         :extras-length (java.lang.Byte/valueOf (byte extras_length)),
         :data-type (java.lang.Byte/valueOf (byte data_type)),
         :vbucket-id (java.lang.Short/valueOf (short vbucket_id)),
         :total-body-length (java.lang.Integer/valueOf (int total_body_length)),
         :cas (long cas)})))
  (reset-meta!
    #'read-header
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'sc {:tag 'SocketChannel}) (.withMeta 'bb {:tag 'ByteBuffer})]),
       :column (int 1)}
      :name
      'read-header
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.valcache" "handle") {:column (int 1)})
  (let [v__5792__auto__ #'handle]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.valcache" "handle") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.valcache" "handle")
        (clojure.lang.MultiFn.
          "handle"
          (fn fn__15461
            ([p__15460 sc]
              (let [map__15462 p__15460
                    map__15462 (if (seq? map__15462)
                                 (if (next map__15462)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__15462))
                                   (if (seq map__15462) (first map__15462) {}))
                                 map__15462)
                    m map__15462
                    opcode (get map__15462 :opcode)]
                opcode)))
          :default
          #'clojure.core/global-hierarchy))
      #'handle))
  (.setMeta (clojure.lang.RT/var "datomic.valcache" "sasl") {:column (int 1)})
  (let [v__5792__auto__ #'sasl]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.valcache" "sasl") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.valcache" "sasl")
        (clojure.lang.MultiFn.
          "sasl"
          (fn fn__15469
            ([p__15468 sc]
              (let [map__15470 p__15468
                    map__15470 (if (seq? map__15470)
                                 (if (next map__15470)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__15470))
                                   (if (seq map__15470) (first map__15470) {}))
                                 map__15470)
                    m map__15470
                    opcode (get map__15470 :opcode)]
                opcode)))
          :default
          #'clojure.core/global-hierarchy))
      #'sasl))
  (defn reply-with-error
    ([opcode status msg sc]
      (let [msg_bytes (.getBytes ^java.lang.String msg "UTF-8")
            mlen (alength ^bytes msg_bytes)
            bb (ByteBuffer/allocate (int (+ 24 mlen)))]
        (io/write-buffer
          (.flip
            (.put
              (.putLong
                (.putInt
                  (.putInt
                    (.putShort
                      (.put
                        (.put
                          (.putShort
                            (.put (.put ^java.nio.ByteBuffer bb (byte -127)) (byte opcode))
                            (short 0))
                          (byte 0))
                        (byte 0))
                      (short ^java.lang.Number status))
                    (int mlen))
                  (int 0))
                0)
              ^bytes msg_bytes))
          sc))))
  (reset-meta!
    #'reply-with-error
    (assoc
      {:private true,
       :arglists (clojure.core/list ['opcode 'status (.withMeta 'msg {:tag 'String}) 'sc]),
       :column (int 1)}
      :name
      'reply-with-error
      :ns
      *ns*))
  (defn supported-or-drain
    ([p__15476 sc mmeth]
      (let [map__15477 p__15476
            map__15477 (if (seq? map__15477)
                         (if (next map__15477)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15477))
                           (if (seq map__15477) (first map__15477) {}))
                         map__15477)
            header map__15477
            opcode (get map__15477 :opcode)
            data_type (get map__15477 :data-type)
            vbucket_id (get map__15477 :vbucket-id)
            cas (get map__15477 :cas)
            total_body_length (get map__15477 :total-body-length)]
        (if (not
              (and
                (zero? data_type)
                (zero? vbucket_id)
                (zero? cas)
                (contains? (methods mmeth) opcode)))
          (let [bb (io/read-n-bytes total_body_length sc)]
            (reply-with-error opcode 131 "Not supported" sc))
          header))))
  (reset-meta!
    #'supported-or-drain
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [{:keys ['opcode 'data-type 'vbucket-id 'cas 'total-body-length], :as 'header}
          'sc
          'mmeth]),
       :column (int 1)}
      :name
      'supported-or-drain
      :ns
      *ns*))
  (defn reply-empty-ok
    ([opcode sc]
      (let [bb (ByteBuffer/allocate (int 24))]
        (io/write-buffer
          (.flip
            (.putLong
              (.putInt
                (.putInt
                  (.putShort
                    (.put
                      (.put
                        (.putShort
                          (.put (.put ^java.nio.ByteBuffer bb (byte -127)) (byte opcode))
                          (short 0))
                        (byte 0))
                      (byte 0))
                    (short 0))
                  (int 0))
                (int 0))
              0))
          sc))))
  (reset-meta!
    #'reply-empty-ok
    (assoc
      {:private true, :arglists (clojure.core/list ['opcode 'sc]), :column (int 1)}
      :name
      'reply-empty-ok
      :ns
      *ns*))
  (defn noop-quit
    ([p__15483 sc]
      (let [map__15484 p__15483
            map__15484 (if (seq? map__15484)
                         (if (next map__15484)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15484))
                           (if (seq map__15484) (first map__15484) {}))
                         map__15484)
            opcode (get map__15484 :opcode)
            key_length (get map__15484 :key-length)
            extras_length (get map__15484 :extras-length)
            total_body_length (get map__15484 :total-body-length)]
        (if (not (and (zero? key_length) (zero? extras_length) (zero? total_body_length)))
          (reply-with-error opcode 4 "Invalid args" sc)
          (reply-empty-ok opcode sc)))))
  (reset-meta!
    #'noop-quit
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['opcode 'key-length 'extras-length 'total-body-length]} 'sc]),
       :column (int 1)}
      :name
      'noop-quit
      :ns
      *ns*))
  (defmethod handle 10 fn__15488 ([header sc] (noop-quit header sc)))
  (defmethod handle 7 fn__15490 ([header sc] (noop-quit header sc)))
  (defn uuid-prefix
    ([s]
      (try
        (str (UUID/fromString (subs s 0 36)))
        (catch
          java.lang.IllegalArgumentException
          ex
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info
                  ^org.slf4j.Logger logger
                  (logger/process {:event :valcache/invalid-uuid, :s s})))
              nil)
            nil)))))
  (reset-meta!
    #'uuid-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'uuid-prefix
      :ns
      *ns*))
  (defn full-path
    ([root k]
      (let [temp__5804__auto__ (uuid-prefix k)]
        (when temp__5804__auto__
          (let [uuid_prefix temp__5804__auto__]
            (.getPath
              (FileSystems/getDefault)
              ^java.lang.String root
              (into-array
                java.lang.String
                [(subs uuid_prefix (long (- (count uuid_prefix) 3))) k])))))))
  (reset-meta!
    #'full-path
    (assoc
      {:private true,
       :arglists (clojure.core/list (.withMeta ['root 'k] {:tag 'java.nio.file.Path})),
       :column (int 1)}
      :name
      'full-path
      :ns
      *ns*))
  (defmethod
    handle
    1
    fn__15496
    ([p__15495 sc]
      (let [map__15497 p__15495
            map__15497 (if (seq? map__15497)
                         (if (next map__15497)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15497))
                           (if (seq map__15497) (first map__15497) {}))
                         map__15497)
            header map__15497
            opcode (get map__15497 :opcode)
            key_length (get map__15497 :key-length)
            extras_length (get map__15497 :extras-length)
            total_body_length (get map__15497 :total-body-length)
            root (get map__15497 :root)]
        (if (or
              (zero? key_length)
              (not= extras_length 8)
              (<= total_body_length (+ key_length extras_length)))
          (do (io/read-n-bytes total_body_length sc) (reply-with-error opcode 4 "Invalid args" sc))
          (let [kelen (+ key_length extras_length)
                eb (ByteBuffer/allocate (int ^java.lang.Number kelen))
                _ (io/read-into-buffer eb kelen sc)
                flags (.getInt ^java.nio.ByteBuffer eb)
                expiry (.getInt ^java.nio.ByteBuffer eb)
                kbytes (byte-array key_length)
                _ (.get ^java.nio.ByteBuffer eb ^bytes kbytes)
                k (java.lang.String. ^bytes kbytes "UTF-8")
                path (full-path root k)
                tmp_path (full-path root (str (UUID/randomUUID)))
                vlen (- total_body_length kelen)]
            (if (not (and (zero? expiry) path))
              (do
                (io/read-n-bytes vlen sc)
                (let [s (str
                          "Unsupported: "
                          (cond (not (zero? expiry)) "expiry" :else (do "non-uuid key")))]
                  (reply-with-error opcode 131 s sc)))
              (do
                (with-open [fc (FileChannel/open
                                 ^java.nio.file.Path tmp_path
                                 (into-array
                                   java.nio.file.OpenOption
                                   [StandardOpenOption/CREATE
                                    StandardOpenOption/WRITE
                                    StandardOpenOption/TRUNCATE_EXISTING]))]
                  (let [bb (ByteBuffer/allocate (int 4))]
                    (io/write-buffer (.flip (.putInt ^java.nio.ByteBuffer bb (int flags))) fc)
                    (.force ^java.nio.channels.FileChannel fc (boolean (.booleanValue true)))
                    (long
                      (.transferFrom
                        ^java.nio.channels.FileChannel fc
                        ^java.nio.channels.ReadableByteChannel sc
                        4
                        (long ^java.lang.Number vlen)))))
                (Files/move
                  ^java.nio.file.Path tmp_path
                  ^java.nio.file.Path path
                  (into-array java.nio.file.CopyOption [StandardCopyOption/REPLACE_EXISTING]))
                (reply-empty-ok opcode sc))))))))
  (defn read-key
    ([key_length sc]
      (let [kb (ByteBuffer/allocate (int ^java.lang.Number key_length))
            _ (io/read-into-buffer kb key_length sc)
            kbytes (byte-array key_length)
            _ (.get ^java.nio.ByteBuffer kb ^bytes kbytes)]
        (java.lang.String. ^bytes kbytes "UTF-8"))))
  (reset-meta!
    #'read-key
    (assoc
      {:private true, :arglists (clojure.core/list ['key-length 'sc]), :column (int 1)}
      :name
      'read-key
      :ns
      *ns*))
  (defmethod
    handle
    0
    fn__15506
    ([p__15505 sc]
      (let [map__15507 p__15505
            map__15507 (if (seq? map__15507)
                         (if (next map__15507)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15507))
                           (if (seq map__15507) (first map__15507) {}))
                         map__15507)
            opcode (get map__15507 :opcode)
            key_length (get map__15507 :key-length)
            extras_length (get map__15507 :extras-length)
            total_body_length (get map__15507 :total-body-length)
            root (get map__15507 :root)]
        (if (not
              (and
                (clojure.lang.Numbers/isPos key_length)
                (zero? extras_length)
                (= total_body_length key_length)))
          (do (io/read-n-bytes total_body_length sc) (reply-with-error opcode 4 "Invalid args" sc))
          (let [k (read-key key_length sc) path (full-path root k)]
            (if (some-> path (.toFile) (.exists))
              (with-open [fc (FileChannel/open
                               ^java.nio.file.Path path
                               (into-array java.nio.file.OpenOption [StandardOpenOption/READ]))]
                (let [bb (ByteBuffer/allocate (int 24))
                      size (.size ^java.nio.channels.FileChannel fc)]
                  (io/write-buffer
                    (.flip
                      (.putLong
                        (.putInt
                          (.putInt
                            (.putShort
                              (.put
                                (.put
                                  (.putShort
                                    (.put (.put ^java.nio.ByteBuffer bb (byte -127)) (byte opcode))
                                    (short 0))
                                  (byte 4))
                                (byte 0))
                              (short 0))
                            (int size))
                          (int 0))
                        0))
                    sc)
                  (let [n (.transferTo
                            ^java.nio.channels.FileChannel fc
                            0
                            (long size)
                            ^java.nio.channels.WritableByteChannel sc)]
                    nil)))
              (reply-with-error opcode 1 "Not found" sc)))))))
  (defmethod
    handle
    4
    fn__15513
    ([p__15512 sc]
      (let [map__15514 p__15512
            map__15514 (if (seq? map__15514)
                         (if (next map__15514)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15514))
                           (if (seq map__15514) (first map__15514) {}))
                         map__15514)
            opcode (get map__15514 :opcode)
            key_length (get map__15514 :key-length)
            extras_length (get map__15514 :extras-length)
            total_body_length (get map__15514 :total-body-length)
            root (get map__15514 :root)]
        (if (not
              (and
                (clojure.lang.Numbers/isPos key_length)
                (zero? extras_length)
                (= total_body_length key_length)))
          (reply-with-error opcode 4 "Invalid args" sc)
          (let [k (read-key key_length sc) path (full-path root k) file (some-> path (.toFile))]
            (when (and file (.exists ^java.io.File file)) (.delete ^java.io.File file))
            (reply-empty-ok opcode sc))))))
  (defn scan-strings
    ([bb]
      (loop [strs [] buf (java.lang.StringBuffer.)]
        (if (.hasRemaining ^java.nio.Buffer bb)
          (let [ch (.get ^java.nio.ByteBuffer bb)]
            (if (= (long (java.lang.Byte/valueOf (byte ch))) 0)
              (if (= (count buf) 0)
                (recur strs buf)
                (recur (conj strs (str buf)) (java.lang.StringBuffer.)))
              (recur strs (.append ^java.lang.StringBuffer buf (char ch)))))
          (conj strs (str buf))))))
  (reset-meta!
    #'scan-strings
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer})]),
       :column (int 1)}
      :name
      'scan-strings
      :ns
      *ns*))
  (defmethod
    sasl
    34
    fn__15522
    ([p__15521 sc]
      (let [map__15523 p__15521
            map__15523 (if (seq? map__15523)
                         (if (next map__15523)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15523))
                           (if (seq map__15523) (first map__15523) {}))
                         map__15523)
            m map__15523
            opcode (get map__15523 :opcode)
            key_length (get map__15523 :key-length)
            extras_length (get map__15523 :extras-length)
            total_body_length (get map__15523 :total-body-length)
            root (get map__15523 :root)
            sasl (get map__15523 :sasl)]
        (if (not (clojure.lang.Numbers/isPos key_length))
          (reply-with-error opcode 4 "Invalid args" sc)
          (let [k (read-key key_length sc)
                extras (when-not (zero? extras_length) (read-key extras_length sc))
                bb (io/read-n-bytes (- (- total_body_length key_length) extras_length) sc)]
            (if (= k "PLAIN")
              (let [vec__15524 (scan-strings bb)
                    username (nth vec__15524 (int 0) nil)
                    password (nth vec__15524 (int 1) nil)]
                (if (and (= (:username sasl) username) (= (:password sasl) password))
                  (do (reply-empty-ok opcode sc) true)
                  (do (reply-empty-ok 32 sc) false)))
              (do (reply-with-error opcode 131 "Not supported" sc) false)))))))
  (defn dirname ([n] (format "%03x" n)))
  (reset-meta!
    #'dirname
    (assoc
      {:private true, :arglists (clojure.core/list ['n]), :column (int 1)}
      :name
      'dirname
      :ns
      *ns*))
  (defn mkdirs
    ([root]
      (dotimes [n 4096]
        (let [path (.getPath
                     (FileSystems/getDefault)
                     ^java.lang.String root
                     (into-array java.lang.String [(dirname (long n))]))]
          (Files/createDirectories
            ^java.nio.file.Path path
            (into-array java.nio.file.attribute.FileAttribute []))))))
  (reset-meta!
    #'mkdirs
    (assoc
      {:private true, :arglists (clojure.core/list ['root]), :column (int 1)}
      :name
      'mkdirs
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      IServer
      (connection-count [s] "Number of connected sockets")
      (running-count [s] "Number of running handler threads")
      (pending-count [s] "Number of pending handler threads")
      (handled-count [s] "Number of handled requests"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.valcache" "IServer")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'IServer :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'connection-count
                                        {:arglists (clojure.core/list ['s])}),
                                      :arglists (clojure.core/list ['s]),
                                      :doc "Number of connected sockets"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.valcache" "IServer"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.valcache" "connection-count")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'running-count
                                        {:arglists (clojure.core/list ['s])}),
                                      :arglists (clojure.core/list ['s]),
                                      :doc "Number of running handler threads"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.valcache" "IServer"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.valcache" "running-count")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'pending-count
                                        {:arglists (clojure.core/list ['s])}),
                                      :arglists (clojure.core/list ['s]),
                                      :doc "Number of pending handler threads"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.valcache" "IServer"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.valcache" "pending-count")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*)))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'handled-count
                                        {:arglists (clojure.core/list ['s])}),
                                      :arglists (clojure.core/list ['s]),
                                      :doc "Number of handled requests"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.valcache" "IServer"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.valcache" "handled-count")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
  (deftype
    Server
    [sem ^long concurrency socket_registry handled host port path shutdown_fn]
    java.lang.AutoCloseable
    datomic.valcache.IServer
    (handled-count [this] (deref handled))
    (pending-count
      [this]
      (java.lang.Integer/valueOf (int (.getQueueLength ^java.util.concurrent.Semaphore sem))))
    (running-count
      [this]
      (long (- concurrency (.availablePermits ^java.util.concurrent.Semaphore sem))))
    (connection-count [this] (java.lang.Integer/valueOf (int (count socket_registry))))
    (^void close [this] (do (^clojure.lang.IFn shutdown_fn) nil)))
  (clojure.core/import 'datomic.valcache.Server)
  (defn ->Server
    ([sem concurrency socket_registry handled host port path shutdown_fn]
      (datomic.valcache.Server.
        sem
        (long ^java.lang.Number concurrency)
        socket_registry
        handled
        host
        port
        path
        shutdown_fn)))
  (reset-meta!
    #'->Server
    (assoc
      {:arglists
       (clojure.core/list
         ['sem 'concurrency 'socket-registry 'handled 'host 'port 'path 'shutdown-fn]),
       :column (int 1)}
      :name
      '->Server
      :ns
      *ns*))
  (defn eviction-loop
    ([path threshold interval_secs file_window shutdown_requested]
      (let [opts (into-array java.nio.file.LinkOption [])
            dirs (into [] (shuffle (range 4096)))
            evict1 (fn evict1
                     ([dir]
                       (let [threshold (/ threshold 4096)
                             path (.getPath
                                    (FileSystems/getDefault)
                                    ^java.lang.String path
                                    (into-array java.lang.String [(dirname dir)]))
                             pq (java.util.PriorityQueue.
                                  (int ^java.lang.Number file_window)
                                  (comparator
                                    (fn fn__15595
                                      ([p1__15592# p2__15593#]
                                        (> (:atime p1__15592#) (:atime p2__15593#))))))
                             size (atom 0)
                             visited (atom 0)]
                         (Files/walkFileTree
                           ^java.nio.file.Path path
                           (reify
                             java.nio.file.FileVisitor
                             (^java.nio.file.FileVisitResult visitFileFailed
                               [this file ^java.io.IOException exc]
                               FileVisitResult/CONTINUE)
                             (^java.nio.file.FileVisitResult visitFile
                               [this file ^java.nio.file.attribute.BasicFileAttributes attrs]
                               (let [length (+
                                              (Files/getAttribute
                                                ^java.nio.file.Path file
                                                "size"
                                                ^"[Ljava.nio.file.LinkOption;" opts)
                                              2048)
                                     atime (Files/getAttribute
                                             ^java.nio.file.Path file
                                             "lastAccessTime"
                                             ^"[Ljava.nio.file.LinkOption;" opts)]
                                 (swap! size + length)
                                 (swap! visited inc)
                                 (.add
                                   ^java.util.PriorityQueue pq
                                   {:atime
                                    (long (.toMillis ^java.nio.file.attribute.FileTime atime)),
                                    :length length,
                                    :file file})
                                 (when (<= file_window (count pq))
                                   (.poll ^java.util.PriorityQueue pq))
                                 FileVisitResult/CONTINUE))
                             (^java.nio.file.FileVisitResult preVisitDirectory
                               [this dir ^java.nio.file.attribute.BasicFileAttributes attrs]
                               FileVisitResult/CONTINUE)
                             (^java.nio.file.FileVisitResult postVisitDirectory
                               [this dir ^java.io.IOException exc]
                               (do (swap! size + (long (* 2 2048))) FileVisitResult/CONTINUE))))
                         (when (and
                                 (> (deref size) threshold)
                                 (clojure.lang.Numbers/isPos (long (count pq))))
                           (let [fa (.toArray ^java.util.PriorityQueue pq)
                                 target (- (deref size) (* 0.9 threshold))]
                             (loop [i (dec (count fa)) deleted 0 files 0]
                               (if (or (neg? i) (>= deleted target))
                                 (let [result {:deleted-files (long files),
                                               :deleted-bytes (long deleted),
                                               :visited-files (deref visited),
                                               :visited-bytes (deref size)}]
                                   result)
                                 (let [map__15599 (aget ^"[Ljava.lang.Object;" fa (int i))
                                       map__15599 (if (seq? map__15599)
                                                    (if (next map__15599)
                                                      (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                        (to-array map__15599))
                                                      (if (seq map__15599) (first map__15599) {}))
                                                    map__15599)
                                       atime (get map__15599 :atime)
                                       length (get map__15599 :length)
                                       file (get map__15599 :file)]
                                   (Files/deleteIfExists ^java.nio.file.Path file)
                                   (recur
                                     (dec i)
                                     (+ deleted (long ^java.lang.Number length))
                                     (inc files))))))))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event "ValcacheEvictPlan",
                 :path (str path),
                 :threshold-mb (quot threshold 1048576),
                 :interval-secs interval_secs,
                 :file-window file_window})))
          nil)
        (loop [counter_base 0]
          (when-not (deref shutdown_requested)
            (let [start (java.lang.System/currentTimeMillis)
                  next_base (loop [counter counter_base iter 0 summary {}]
                              (let [temp__5802__auto__ (try
                                                         (^clojure.lang.IFn evict1
                                                           (nth dirs (int counter)))
                                                         (catch
                                                           java.lang.Throwable
                                                           ex
                                                           (do
                                                             (let
                                                               [logger
                                                                (org.slf4j.LoggerFactory/getLogger
                                                                  "datomic.valcache")
                                                                ex ex]
                                                               (when
                                                                 (.isWarnEnabled
                                                                   ^org.slf4j.Logger logger)
                                                                 (.warn
                                                                   ^org.slf4j.Logger logger
                                                                   (logger/process
                                                                     "ValcacheEvictLoopFailed")
                                                                   ^java.lang.Throwable ex)
                                                                 (logger/caused-by logger ex))
                                                               nil)
                                                             nil)))]
                                (if temp__5802__auto__
                                  (let [step temp__5802__auto__]
                                    (when (zero? (mod (long counter) 128))
                                      (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.valcache")]
                                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                          (.info
                                            ^org.slf4j.Logger logger
                                            (logger/process
                                              (merge
                                                {:event "ValcacheEvictProgress",
                                                 :counter (long counter),
                                                 :iter (long iter),
                                                 :msec
                                                 (long
                                                   (- (java.lang.System/currentTimeMillis) start))}
                                                summary))))
                                        nil))
                                    (recur
                                      (long (mod (long (inc counter)) 4096))
                                      (inc iter)
                                      (merge-with + summary step)))
                                  (do
                                    (when-not (= iter 0)
                                      (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.valcache")]
                                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                          (.info
                                            ^org.slf4j.Logger logger
                                            (logger/process
                                              (merge
                                                {:event "ValcacheEvictCompleted",
                                                 :counter (long counter),
                                                 :iter (long iter),
                                                 :msec
                                                 (long
                                                   (- (java.lang.System/currentTimeMillis) start))}
                                                summary))))
                                        nil))
                                    (mod (long (inc counter)) 4096)))))]
              (java.lang.Thread/sleep (long (* 1000 interval_secs)))
              (recur (long next_base))))))))
  (reset-meta!
    #'eviction-loop
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['path 'threshold 'interval-secs 'file-window 'shutdown-requested]),
       :column (int 1)}
      :name
      'eviction-loop
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.valcache" "valcache-ref")
    {:private true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.valcache" "valcache-ref") (atom nil))
  (defn shutdown
    ([]
      (let [temp__5804__auto__ (deref valcache-ref)]
        (when temp__5804__auto__
          (let [valcache temp__5804__auto__] (compare-and-set! valcache-ref valcache nil))))))
  (reset-meta!
    #'shutdown
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'shutdown :ns *ns*))
  (defn sasl-loop
    ([creds sc sem]
      (loop [hb (ByteBuffer/wrap (byte-array 24))]
        (let [map__15611 (read-header sc hb)
              map__15611 (if (seq? map__15611)
                           (if (next map__15611)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__15611))
                             (if (seq map__15611) (first map__15611) {}))
                           map__15611)
              header map__15611
              opcode (get map__15611 :opcode)]
          (.acquire ^java.util.concurrent.Semaphore sem)
          (let [authed (try
                         (some-> (supported-or-drain (assoc header :sasl creds) sc sasl) (sasl sc))
                         (finally (.release ^java.util.concurrent.Semaphore sem)))]
            (if authed true (if (= opcode 7) false (recur hb))))))))
  (reset-meta!
    #'sasl-loop
    (assoc
      {:arglists
       (clojure.core/list
         ['creds (.withMeta 'sc {:tag 'SocketChannel}) (.withMeta 'sem {:tag 'Semaphore})]),
       :column (int 1)}
      :name
      'sasl-loop
      :ns
      *ns*))
  (defn remote-ip
    ([sc]
      (let [inet (.getRemoteAddress ^java.nio.channels.SocketChannel sc)]
        (.getHostString ^java.net.InetSocketAddress inet))))
  (reset-meta!
    #'remote-ip
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'sc {:tag 'SocketChannel})]),
       :column (int 1)}
      :name
      'remote-ip
      :ns
      *ns*))
  (defn start-server
    ([p__15617]
      (let [map__15618 p__15617
            map__15618 (if (seq? map__15618)
                         (if (next map__15618)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15618))
                           (if (seq map__15618) (first map__15618) {}))
                         map__15618)
            path (get map__15618 :path)
            eviction_threshold_mb (get map__15618 :eviction-threshold-mb)
            eviction_file_window (get map__15618 :eviction-file-window 10000)
            concurrency (get map__15618 :concurrency 8)
            port (get map__15618 :port 11211)
            host (get map__15618 :host)
            eviction_interval_secs (get map__15618 :eviction-interval-secs 60)
            ip_validator (get map__15618 :ip-validator (constantly true))
            sasl (get map__15618 :sasl)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :valcache/start,
                 :datomic.valcache/host host,
                 :datomic.valcache/port port,
                 :datomic.valcache/path path,
                 :datomic.valcache/eviction-threshold-mb eviction_threshold_mb})))
          nil)
        (let [ssc (ServerSocketChannel/open)
              sem (java.util.concurrent.Semaphore.
                    (int ^java.lang.Number concurrency)
                    (boolean (.booleanValue true)))
              socket_registry (java.util.concurrent.ConcurrentHashMap.)
              handled (atom 0)
              shutdown_requested (atom nil)
              internal_shutdown (fn internal_shutdown
                                  ([]
                                    (reset! shutdown_requested true)
                                    (.close
                                      ^java.nio.channels.spi.AbstractInterruptibleChannel ssc)
                                    (loop [seq_15620 (seq
                                                       (.keySet
                                                         ^java.util.concurrent.ConcurrentHashMap socket_registry))
                                           chunk_15621 nil
                                           count_15622 0
                                           i_15623 0]
                                      (if (< i_15623 count_15622)
                                        (let [sc (.nth
                                                   ^clojure.lang.Indexed chunk_15621
                                                   (int i_15623))]
                                          (.close
                                            ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                          (recur seq_15620 chunk_15621 count_15622 (inc i_15623)))
                                        (let [temp__5804__auto__ (seq seq_15620)]
                                          (when temp__5804__auto__
                                            (let [seq_15620 temp__5804__auto__]
                                              (if (chunked-seq? seq_15620)
                                                (let [c__6065__auto__ (chunk-first seq_15620)]
                                                  (recur
                                                    (chunk-rest seq_15620)
                                                    c__6065__auto__
                                                    (int (count c__6065__auto__))
                                                    (int 0)))
                                                (let [sc (first seq_15620)]
                                                  (.close
                                                    ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                                  (recur (next seq_15620) nil 0 0))))))))))
              socket_loop (fn socket_loop
                            ([sc]
                              (try
                                (try
                                  (do
                                    (.setTcpNoDelay
                                      (.socket ^java.nio.channels.SocketChannel sc)
                                      (boolean (.booleanValue true)))
                                    (when (if sasl (sasl-loop sasl sc sem) true)
                                      (loop [hb (ByteBuffer/wrap (byte-array 24))]
                                        (let [map__15628 (read-header sc hb)
                                              map__15628 (if (seq? map__15628)
                                                           (if
                                                             (next map__15628)
                                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                               (to-array map__15628))
                                                             (if
                                                               (seq map__15628)
                                                               (first map__15628)
                                                               {}))
                                                           map__15628)
                                              header map__15628
                                              opcode (get map__15628 :opcode)]
                                          (.acquire ^java.util.concurrent.Semaphore sem)
                                          (try
                                            (do
                                              (some->
                                                (supported-or-drain
                                                  (assoc header :root path)
                                                  sc
                                                  handle)
                                                (handle sc))
                                              (swap! handled inc))
                                            (finally
                                              (.release ^java.util.concurrent.Semaphore sem)))
                                          (when-not (= opcode 7) (recur hb))))
                                      nil))
                                  (catch
                                    java.lang.Throwable
                                    ex
                                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.valcache")
                                          ex ex]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process {:event :valcache/socket-exception})
                                          ^java.lang.Throwable ex)
                                        (logger/caused-by logger ex))
                                      nil))
                                  (catch
                                    java.io.IOException
                                    ex
                                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.valcache")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process
                                            {:event :valcache/io-exception,
                                             :msg (.getMessage ^java.lang.Throwable ex)})))
                                      nil)))
                                (finally
                                  (do
                                    (.close ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                    (.remove
                                      ^java.util.concurrent.ConcurrentHashMap socket_registry
                                      sc))))))
              accept_loop (fn accept_loop
                            ([]
                              (try
                                (try
                                  (do
                                    (loop []
                                      (let [sc (.accept ^java.nio.channels.ServerSocketChannel ssc)
                                            ip (remote-ip sc)]
                                        (if (^clojure.lang.IFn ip_validator ip)
                                          (do
                                            (.put
                                              ^java.util.concurrent.ConcurrentHashMap socket_registry
                                              sc
                                              sc)
                                            (datomic.async/daemon
                                              (fn fn__15634
                                                ([] (^clojure.lang.IFn socket_loop sc)))
                                              "valcache-socket-loop"))
                                          (do
                                            (let [logger (org.slf4j.LoggerFactory/getLogger
                                                           "datomic.valcache")]
                                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                (.info
                                                  ^org.slf4j.Logger logger
                                                  (logger/process
                                                    {:event :valcache/reject-ip, :ip ip})))
                                              nil)
                                            (.close
                                              ^java.nio.channels.spi.AbstractInterruptibleChannel sc)))
                                        (recur)))
                                    nil)
                                  (catch java.nio.channels.AsynchronousCloseException _ nil))
                                (finally (^clojure.lang.IFn internal_shutdown)))))]
          (mkdirs path)
          (.bind
            (.socket ^java.nio.channels.ServerSocketChannel ssc)
            (java.net.InetSocketAddress. ^java.lang.String host (int ^java.lang.Number port)))
          (datomic.async/daemon
            (fn fn__15637
              ([]
                (eviction-loop
                  path
                  (* (* eviction_threshold_mb 1024) 1024)
                  eviction_interval_secs
                  eviction_file_window
                  shutdown_requested)))
            "valcache-eviction-loop")
          (datomic.async/daemon accept_loop "valcache-accept-loop")
          (reset!
            valcache-ref
            (datomic.valcache.Server.
              sem
              (long ^java.lang.Number concurrency)
              socket_registry
              handled
              host
              port
              path
              internal_shutdown))))))
  (reset-meta!
    #'start-server
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys
           [(.withMeta 'host {:tag 'String})
            (.withMeta 'port {:tag 'long})
            (.withMeta 'path {:tag 'String})
            'eviction-threshold-mb
            (.withMeta 'concurrency {:tag 'long})
            'eviction-interval-secs
            'eviction-file-window
            'sasl
            'ip-validator],
           :or
           {'port 11211,
            'concurrency 8,
            'eviction-interval-secs 60,
            'eviction-file-window 10000,
            'ip-validator (.withMeta (clojure.core/list 'constantly true) {:column (int 101)})}}]),
       :column (int 1)}
      :name
      'start-server
      :ns
      *ns*))
  (defn direct-init
    ([p__15640]
      (let [map__15641 p__15640
            map__15641 (if (seq? map__15641)
                         (if (next map__15641)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15641))
                           (if (seq map__15641) (first map__15641) {}))
                         map__15641)
            path (get map__15641 :path)
            eviction_threshold_mb (get map__15641 :eviction-threshold-mb)
            concurrency (get map__15641 :concurrency 8)
            eviction_interval_secs (get map__15641 :eviction-interval-secs 60)
            eviction_file_window (get map__15641 :eviction-file-window 10000)]
        (when-not path (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'path)))))
        (when-not eviction_threshold_mb
          (throw
            (java.lang.AssertionError. (str "Assert failed: " (pr-str 'eviction-threshold-mb)))))
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :valcache/direct-init,
                 :datomic.valcache/path path,
                 :datomic.valcache/eviction-threshold-mb eviction_threshold_mb})))
          nil)
        (let [shutdown_requested (atom nil)]
          (mkdirs path)
          (datomic.async/daemon
            (fn fn__15642
              ([]
                (eviction-loop
                  path
                  (* (* eviction_threshold_mb 1024) 1024)
                  eviction_interval_secs
                  eviction_file_window
                  shutdown_requested)))
            "valcache-eviction-loop")
          (fn fn__15644 ([] (reset! shutdown_requested true)))))))
  (reset-meta!
    #'direct-init
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [{:keys
             [(.withMeta 'path {:tag 'String})
              'eviction-threshold-mb
              (.withMeta 'concurrency {:tag 'long})
              'eviction-interval-secs
              'eviction-file-window],
             :or {'concurrency 8, 'eviction-interval-secs 60, 'eviction-file-window 10000}}]
           {:pre ['path 'eviction-threshold-mb]})),
       :column (int 1)}
      :name
      'direct-init
      :ns
      *ns*))
  (defn direct-get
    ([root k]
      (when-not root (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'root)))))
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'k)))))
      (let [path (full-path root k)]
        (when (some-> path (.toFile) (.exists))
          (with-open [fc (FileChannel/open
                           ^java.nio.file.Path path
                           (into-array java.nio.file.OpenOption [StandardOpenOption/READ]))]
            (let [size (- (.size ^java.nio.channels.FileChannel fc) 4)]
              (when (> size 0)
                (.position ^java.nio.channels.FileChannel fc 4)
                (io/read-into-buffer (ByteBuffer/allocate (int size)) (long size) fc))))))))
  (reset-meta!
    #'direct-get
    (assoc
      {:arglists (clojure.core/list (.withMeta ['root 'k] {:pre ['root 'k]})), :column (int 1)}
      :name
      'direct-get
      :ns
      *ns*))
  (defn direct-put
    ([root k v]
      (when-not root (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'root)))))
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'k)))))
      (when-not v (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'v)))))
      (let [temp__5804__auto__ (full-path root k)]
        (when temp__5804__auto__
          (let [path temp__5804__auto__ tmp_path (full-path root (str (UUID/randomUUID)))]
            (try
              (do
                (with-open [fc (FileChannel/open
                                 ^java.nio.file.Path tmp_path
                                 (into-array
                                   java.nio.file.OpenOption
                                   [StandardOpenOption/CREATE
                                    StandardOpenOption/WRITE
                                    StandardOpenOption/TRUNCATE_EXISTING]))]
                  (do
                    (let [bb (ByteBuffer/allocate (int 4))]
                      (io/write-buffer (.flip (.putInt ^java.nio.ByteBuffer bb (int 2048))) fc))
                    (io/write-buffer (.duplicate ^java.nio.ByteBuffer v) fc)
                    (.force ^java.nio.channels.FileChannel fc (boolean (.booleanValue true)))
                    nil))
                (Files/move
                  ^java.nio.file.Path tmp_path
                  ^java.nio.file.Path path
                  (into-array java.nio.file.CopyOption [StandardCopyOption/REPLACE_EXISTING]))
                true)
              (catch
                java.lang.Throwable
                t
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache") ex t]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info
                      ^org.slf4j.Logger logger
                      (logger/process
                        {:event :valcache/put-exception,
                         :tmp-path (str tmp_path),
                         :path (str path)})
                      ^java.lang.Throwable ex)
                    (logger/caused-by logger ex))
                  nil))))))))
  (reset-meta!
    #'direct-put
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta ['root 'k (.withMeta 'v {:tag 'ByteBuffer})] {:pre ['root 'k 'v]})),
       :column (int 1)}
      :name
      'direct-put
      :ns
      *ns*)))
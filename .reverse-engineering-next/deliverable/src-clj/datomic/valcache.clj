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
  (reset-meta! #'HEADER_LENGTH (assoc {:const true, :column 1} :name 'HEADER_LENGTH :ns *ns*))
  (def REQUEST_MAGIC -128)
  (reset-meta! #'REQUEST_MAGIC (assoc {:const true, :column 1} :name 'REQUEST_MAGIC :ns *ns*))
  (def RESPONSE_MAGIC -127)
  (reset-meta! #'RESPONSE_MAGIC (assoc {:const true, :column 1} :name 'RESPONSE_MAGIC :ns *ns*))
  (def FILE_MAGIC_LENGTH 4)
  (reset-meta!
    #'FILE_MAGIC_LENGTH
    (assoc {:const true, :column 1} :name 'FILE_MAGIC_LENGTH :ns *ns*))
  (def FILE_MAGIC 2048)
  (reset-meta! #'FILE_MAGIC (assoc {:const true, :column 1} :name 'FILE_MAGIC :ns *ns*))
  (def DIRS 4096)
  (reset-meta! #'DIRS (assoc {:const true, :column 1} :name 'DIRS :ns *ns*))
  (def MEG 1048576)
  (reset-meta! #'MEG (assoc {:const true, :column 1} :name 'MEG :ns *ns*))
  (def BLOCK_SIZE_WASTE 2048)
  (reset-meta!
    #'BLOCK_SIZE_WASTE
    (assoc {:const true, :column 1} :name 'BLOCK_SIZE_WASTE :ns *ns*))
  (def GET 0)
  (reset-meta! #'GET (assoc {:const true, :column 1} :name 'GET :ns *ns*))
  (def SET 1)
  (reset-meta! #'SET (assoc {:const true, :column 1} :name 'SET :ns *ns*))
  (def DELETE 4)
  (reset-meta! #'DELETE (assoc {:const true, :column 1} :name 'DELETE :ns *ns*))
  (def QUIT 7)
  (reset-meta! #'QUIT (assoc {:const true, :column 1} :name 'QUIT :ns *ns*))
  (def NOOP 10)
  (reset-meta! #'NOOP (assoc {:const true, :column 1} :name 'NOOP :ns *ns*))
  (def SASL_AUTH 33)
  (reset-meta! #'SASL_AUTH (assoc {:const true, :column 1} :name 'SASL_AUTH :ns *ns*))
  (def SASL_STEP 34)
  (reset-meta! #'SASL_STEP (assoc {:const true, :column 1} :name 'SASL_STEP :ns *ns*))
  (def NOT_FOUND 1)
  (reset-meta! #'NOT_FOUND (assoc {:const true, :column 1} :name 'NOT_FOUND :ns *ns*))
  (def INVALID 4)
  (reset-meta! #'INVALID (assoc {:const true, :column 1} :name 'INVALID :ns *ns*))
  (def UNAUTHORIZED 32)
  (reset-meta! #'UNAUTHORIZED (assoc {:const true, :column 1} :name 'UNAUTHORIZED :ns *ns*))
  (def NOT_SUPPORTED 131)
  (reset-meta! #'NOT_SUPPORTED (assoc {:const true, :column 1} :name 'NOT_SUPPORTED :ns *ns*))
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
       :column 1}
      :name
      'read-header
      :ns
      *ns*))
  (defmulti
    handle
    (fn fn__9632
      ([p__9631 sc]
        (let [map__9633 p__9631
              map__9633 (if (seq? map__9633)
                          (clojure.lang.PersistentHashMap/create (seq map__9633))
                          map__9633)
              m map__9633
              opcode (get map__9633 :opcode)]
          opcode))))
  (defmulti
    sasl
    (fn fn__9640
      ([p__9639 sc]
        (let [map__9641 p__9639
              map__9641 (if (seq? map__9641)
                          (clojure.lang.PersistentHashMap/create (seq map__9641))
                          map__9641)
              m map__9641
              opcode (get map__9641 :opcode)]
          opcode))))
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
       :column 1}
      :name
      'reply-with-error
      :ns
      *ns*))
  (defn supported-or-drain
    ([p__9647 sc mmeth]
      (let [map__9648 p__9647
            map__9648 (if (seq? map__9648)
                        (clojure.lang.PersistentHashMap/create (seq map__9648))
                        map__9648)
            header map__9648
            opcode (get map__9648 :opcode)
            data_type (get map__9648 :data-type)
            vbucket_id (get map__9648 :vbucket-id)
            cas (get map__9648 :cas)
            total_body_length (get map__9648 :total-body-length)]
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
       :column 1}
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
      {:private true, :arglists (clojure.core/list ['opcode 'sc]), :column 1}
      :name
      'reply-empty-ok
      :ns
      *ns*))
  (defn noop-quit
    ([p__9654 sc]
      (let [map__9655 p__9654
            map__9655 (if (seq? map__9655)
                        (clojure.lang.PersistentHashMap/create (seq map__9655))
                        map__9655)
            opcode (get map__9655 :opcode)
            key_length (get map__9655 :key-length)
            extras_length (get map__9655 :extras-length)
            total_body_length (get map__9655 :total-body-length)]
        (if (not (and (zero? key_length) (zero? extras_length) (zero? total_body_length)))
          (reply-with-error opcode 4 "Invalid args" sc)
          (reply-empty-ok opcode sc)))))
  (reset-meta!
    #'noop-quit
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [{:keys ['opcode 'key-length 'extras-length 'total-body-length]} 'sc]),
       :column 1}
      :name
      'noop-quit
      :ns
      *ns*))
  (defmethod handle 10 fn__9659 ([header sc] (noop-quit header sc)))
  (defmethod handle 7 fn__9661 ([header sc] (noop-quit header sc)))
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
                  (logger/process {:event :valcache/invalid-uuid, :s s}))
                nil)
              nil)
            nil)))))
  (reset-meta!
    #'uuid-prefix
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column 1}
      :name
      'uuid-prefix
      :ns
      *ns*))
  (defn full-path
    ([root k]
      (let [temp__5457__auto__ (uuid-prefix k)]
        (when temp__5457__auto__
          (let [uuid_prefix temp__5457__auto__]
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
       :column 1}
      :name
      'full-path
      :ns
      *ns*))
  (defmethod
    handle
    1
    fn__9667
    ([p__9666 sc]
      (let [map__9668 p__9666
            map__9668 (if (seq? map__9668)
                        (clojure.lang.PersistentHashMap/create (seq map__9668))
                        map__9668)
            header map__9668
            opcode (get map__9668 :opcode)
            key_length (get map__9668 :key-length)
            extras_length (get map__9668 :extras-length)
            total_body_length (get map__9668 :total-body-length)
            root (get map__9668 :root)]
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
                (let [fc (FileChannel/open
                           ^java.nio.file.Path tmp_path
                           (into-array
                             java.nio.file.OpenOption
                             [StandardOpenOption/CREATE
                              StandardOpenOption/WRITE
                              StandardOpenOption/TRUNCATE_EXISTING]))]
                  (try
                    (let [bb (ByteBuffer/allocate (int 4))]
                      (io/write-buffer (.flip (.putInt ^java.nio.ByteBuffer bb (int flags))) fc)
                      (.force ^java.nio.channels.FileChannel fc (boolean (.booleanValue true)))
                      (long
                        (.transferFrom
                          ^java.nio.channels.FileChannel fc
                          ^java.nio.channels.ReadableByteChannel sc
                          4
                          (long ^java.lang.Number vlen))))
                    (finally
                      (do (.close ^java.nio.channels.spi.AbstractInterruptibleChannel fc) nil))))
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
      {:private true, :arglists (clojure.core/list ['key-length 'sc]), :column 1}
      :name
      'read-key
      :ns
      *ns*))
  (defmethod
    handle
    0
    fn__9677
    ([p__9676 sc]
      (let [map__9678 p__9676
            map__9678 (if (seq? map__9678)
                        (clojure.lang.PersistentHashMap/create (seq map__9678))
                        map__9678)
            opcode (get map__9678 :opcode)
            key_length (get map__9678 :key-length)
            extras_length (get map__9678 :extras-length)
            total_body_length (get map__9678 :total-body-length)
            root (get map__9678 :root)]
        (if (not
              (and
                (clojure.lang.Numbers/isPos key_length)
                (zero? extras_length)
                (= total_body_length key_length)))
          (do (io/read-n-bytes total_body_length sc) (reply-with-error opcode 4 "Invalid args" sc))
          (let [k (read-key key_length sc) path (full-path root k)]
            (if (some-> path (.toFile) (.exists))
              (let [fc (FileChannel/open
                         ^java.nio.file.Path path
                         (into-array java.nio.file.OpenOption [StandardOpenOption/READ]))]
                (try
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
                                      (.put
                                        (.put ^java.nio.ByteBuffer bb (byte -127))
                                        (byte opcode))
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
                      nil))
                  (finally
                    (do (.close ^java.nio.channels.spi.AbstractInterruptibleChannel fc) nil))))
              (reply-with-error opcode 1 "Not found" sc)))))))
  (defmethod
    handle
    4
    fn__9684
    ([p__9683 sc]
      (let [map__9685 p__9683
            map__9685 (if (seq? map__9685)
                        (clojure.lang.PersistentHashMap/create (seq map__9685))
                        map__9685)
            opcode (get map__9685 :opcode)
            key_length (get map__9685 :key-length)
            extras_length (get map__9685 :extras-length)
            total_body_length (get map__9685 :total-body-length)
            root (get map__9685 :root)]
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
       :column 1}
      :name
      'scan-strings
      :ns
      *ns*))
  (defmethod
    sasl
    34
    fn__9693
    ([p__9692 sc]
      (let [map__9694 p__9692
            map__9694 (if (seq? map__9694)
                        (clojure.lang.PersistentHashMap/create (seq map__9694))
                        map__9694)
            m map__9694
            opcode (get map__9694 :opcode)
            key_length (get map__9694 :key-length)
            extras_length (get map__9694 :extras-length)
            total_body_length (get map__9694 :total-body-length)
            root (get map__9694 :root)
            sasl (get map__9694 :sasl)]
        (if (not (clojure.lang.Numbers/isPos key_length))
          (reply-with-error opcode 4 "Invalid args" sc)
          (let [k (read-key key_length sc)
                extras (when-not (zero? extras_length) (read-key extras_length sc))
                bb (io/read-n-bytes (- (- total_body_length key_length) extras_length) sc)]
            (if (= k "PLAIN")
              (let [vec__9695 (scan-strings bb)
                    username (nth vec__9695 (int 0) nil)
                    password (nth vec__9695 (int 1) nil)]
                (if (and (= (:username sasl) username) (= (:password sasl) password))
                  (do (reply-empty-ok opcode sc) true)
                  (do (reply-empty-ok 32 sc) false)))
              (do (reply-with-error opcode 131 "Not supported" sc) false)))))))
  (defn dirname ([n] (format "%03x" n)))
  (reset-meta!
    #'dirname
    (assoc {:private true, :arglists (clojure.core/list ['n]), :column 1} :name 'dirname :ns *ns*))
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
      {:private true, :arglists (clojure.core/list ['root]), :column 1}
      :name
      'mkdirs
      :ns
      *ns*))
  (defonce IServer {})
  (defprotocol
    IServer
    (connection-count [s])
    (running-count [s])
    (pending-count [s])
    (handled-count [s]))
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
                                    (fn fn__9766
                                      ([p1__9763# p2__9764#]
                                        (> (:atime p1__9763#) (:atime p2__9764#))))))
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
                                 (let [map__9770 (aget ^"[Ljava.lang.Object;" fa (int i))
                                       map__9770 (if (seq? map__9770)
                                                   (clojure.lang.PersistentHashMap/create
                                                     (seq map__9770))
                                                   map__9770)
                                       atime (get map__9770 :atime)
                                       length (get map__9770 :length)
                                       file (get map__9770 :file)]
                                   (Files/deleteIfExists ^java.nio.file.Path file)
                                   (recur
                                     (dec i)
                                     (+ deleted (long ^java.lang.Number length))
                                     (inc files))))))
                           nil))))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event "ValcacheEvictPlan",
                 :path (str path),
                 :threshold-mb (quot threshold 1048576),
                 :interval-secs interval_secs,
                 :file-window file_window}))
            nil)
          nil)
        (loop [counter_base 0]
          (when-not (deref shutdown_requested)
            (let [start (java.lang.System/currentTimeMillis)
                  next_base (loop [counter counter_base iter 0 summary {}]
                              (let [temp__5455__auto__ (try
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
                                (if temp__5455__auto__
                                  (let [step temp__5455__auto__]
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
                                                summary)))
                                          nil)
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
                                                summary)))
                                          nil)
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
       :column 1}
      :name
      'eviction-loop
      :ns
      *ns*))
  (def valcache-ref (atom nil))
  (reset-meta! #'valcache-ref (assoc {:private true, :column 1} :name 'valcache-ref :ns *ns*))
  (defn shutdown
    ([]
      (let [temp__5457__auto__ (deref valcache-ref)]
        (when temp__5457__auto__
          (let [valcache temp__5457__auto__] (compare-and-set! valcache-ref valcache nil))))))
  (defn sasl-loop
    ([creds sc sem]
      (loop [hb (ByteBuffer/wrap (byte-array 24))]
        (let [map__9782 (read-header sc hb)
              map__9782 (if (seq? map__9782)
                          (clojure.lang.PersistentHashMap/create (seq map__9782))
                          map__9782)
              header map__9782
              opcode (get map__9782 :opcode)]
          (.acquire ^java.util.concurrent.Semaphore sem)
          (let [authed (try
                         (some-> (supported-or-drain (assoc header :sasl creds) sc sasl) (sasl sc))
                         (finally (do (.release ^java.util.concurrent.Semaphore sem) nil)))]
            (if authed true (if (= opcode 7) false (recur hb))))))))
  (defn remote-ip
    ([sc]
      (let [inet (.getRemoteAddress ^java.nio.channels.SocketChannel sc)]
        (.getHostString ^java.net.InetSocketAddress inet))))
  (reset-meta!
    #'remote-ip
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'sc {:tag 'SocketChannel})]),
       :column 1}
      :name
      'remote-ip
      :ns
      *ns*))
  (defn start-server
    ([p__9788]
      (let [map__9789 p__9788
            map__9789 (if (seq? map__9789)
                        (clojure.lang.PersistentHashMap/create (seq map__9789))
                        map__9789)
            host (get map__9789 :host)
            port (get map__9789 :port 11211)
            path (get map__9789 :path)
            eviction_threshold_mb (get map__9789 :eviction-threshold-mb)
            concurrency (get map__9789 :concurrency 8)
            eviction_interval_secs (get map__9789 :eviction-interval-secs 60)
            eviction_file_window (get map__9789 :eviction-file-window 10000)
            sasl (get map__9789 :sasl)
            ip_validator (get map__9789 :ip-validator (constantly true))]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.valcache")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :valcache/start,
                 :datomic.valcache/host host,
                 :datomic.valcache/port port,
                 :datomic.valcache/path path,
                 :datomic.valcache/eviction-threshold-mb eviction_threshold_mb}))
            nil)
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
                                    (loop [seq_9791 (seq
                                                      (.keySet
                                                        ^java.util.concurrent.ConcurrentHashMap socket_registry))
                                           chunk_9792 nil
                                           count_9793 0
                                           i_9794 0]
                                      (if (< i_9794 count_9793)
                                        (let [sc (.nth
                                                   ^clojure.lang.Indexed chunk_9792
                                                   (int i_9794))]
                                          (.close
                                            ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                          (recur seq_9791 chunk_9792 count_9793 (inc i_9794)))
                                        (let [temp__5457__auto__ (seq seq_9791)]
                                          (when temp__5457__auto__
                                            (let [seq_9791 temp__5457__auto__]
                                              (if (chunked-seq? seq_9791)
                                                (let [c__5719__auto__ (chunk-first seq_9791)]
                                                  (recur
                                                    (chunk-rest seq_9791)
                                                    c__5719__auto__
                                                    (int (count c__5719__auto__))
                                                    (int 0)))
                                                (let [sc (first seq_9791)]
                                                  (.close
                                                    ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                                  (recur (next seq_9791) nil 0 0))))))))))
              socket_loop (fn socket_loop
                            ([sc]
                              (try
                                (do
                                  (.setTcpNoDelay
                                    (.socket ^java.nio.channels.SocketChannel sc)
                                    (boolean (.booleanValue true)))
                                  (when (if sasl (sasl-loop sasl sc sem) true)
                                    (loop [hb (ByteBuffer/wrap (byte-array 24))]
                                      (let [map__9799 (read-header sc hb)
                                            map__9799 (if (seq? map__9799)
                                                        (clojure.lang.PersistentHashMap/create
                                                          (seq map__9799))
                                                        map__9799)
                                            header map__9799
                                            opcode (get map__9799 :opcode)]
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
                                            (do
                                              (.release ^java.util.concurrent.Semaphore sem)
                                              nil)))
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
                                           :msg (.getMessage ^java.lang.Throwable ex)}))
                                      nil)
                                    nil))
                                (finally
                                  (do
                                    (.close ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                    (.remove
                                      ^java.util.concurrent.ConcurrentHashMap socket_registry
                                      sc))))))
              accept_loop (fn accept_loop
                            ([]
                              (try
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
                                          (fn fn__9805 ([] (^clojure.lang.IFn socket_loop sc)))
                                          "valcache-socket-loop"))
                                      (do
                                        (let [logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.valcache")]
                                          (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                            (.info
                                              ^org.slf4j.Logger logger
                                              (logger/process
                                                {:event :valcache/reject-ip, :ip ip}))
                                            nil)
                                          nil)
                                        (.close
                                          ^java.nio.channels.spi.AbstractInterruptibleChannel sc)
                                        nil))
                                    (recur)))
                                (catch java.nio.channels.AsynchronousCloseException _ nil)
                                (finally (^clojure.lang.IFn internal_shutdown)))))]
          (mkdirs path)
          (.bind
            (.socket ^java.nio.channels.ServerSocketChannel ssc)
            (java.net.InetSocketAddress. ^java.lang.String host (int ^java.lang.Number port)))
          (datomic.async/daemon
            (fn fn__9808
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
  (defn direct-init
    ([p__9811]
      (let [map__9812 p__9811
            map__9812 (if (seq? map__9812)
                        (clojure.lang.PersistentHashMap/create (seq map__9812))
                        map__9812)
            path (get map__9812 :path)
            eviction_threshold_mb (get map__9812 :eviction-threshold-mb)
            concurrency (get map__9812 :concurrency 8)
            eviction_interval_secs (get map__9812 :eviction-interval-secs 60)
            eviction_file_window (get map__9812 :eviction-file-window 10000)]
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
                 :datomic.valcache/eviction-threshold-mb eviction_threshold_mb}))
            nil)
          nil)
        (let [shutdown_requested (atom nil)]
          (mkdirs path)
          (datomic.async/daemon
            (fn fn__9813
              ([]
                (eviction-loop
                  path
                  (* (* eviction_threshold_mb 1024) 1024)
                  eviction_interval_secs
                  eviction_file_window
                  shutdown_requested)))
            "valcache-eviction-loop")
          (fn fn__9815 ([] (reset! shutdown_requested true)))))))
  (defn direct-get
    ([root k]
      (when-not root (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'root)))))
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'k)))))
      (let [path (full-path root k)]
        (when (some-> path (.toFile) (.exists))
          (let [fc (FileChannel/open
                     ^java.nio.file.Path path
                     (into-array java.nio.file.OpenOption [StandardOpenOption/READ]))]
            (try
              (let [size (- (.size ^java.nio.channels.FileChannel fc) 4)]
                (when (> size 0)
                  (.position ^java.nio.channels.FileChannel fc 4)
                  (io/read-into-buffer (ByteBuffer/allocate (int size)) (long size) fc)))
              (finally
                (do (.close ^java.nio.channels.spi.AbstractInterruptibleChannel fc) nil))))))))
  (defn direct-put
    ([root k v]
      (when-not root (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'root)))))
      (when-not k (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'k)))))
      (when-not v (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'v)))))
      (let [temp__5457__auto__ (full-path root k)]
        (when temp__5457__auto__
          (let [path temp__5457__auto__ tmp_path (full-path root (str (UUID/randomUUID)))]
            (try
              (do
                (let [fc (FileChannel/open
                           ^java.nio.file.Path tmp_path
                           (into-array
                             java.nio.file.OpenOption
                             [StandardOpenOption/CREATE
                              StandardOpenOption/WRITE
                              StandardOpenOption/TRUNCATE_EXISTING]))]
                  (try
                    (do
                      (let [bb (ByteBuffer/allocate (int 4))]
                        (io/write-buffer (.flip (.putInt ^java.nio.ByteBuffer bb (int 2048))) fc))
                      (io/write-buffer (.duplicate ^java.nio.ByteBuffer v) fc)
                      (.force ^java.nio.channels.FileChannel fc (boolean (.booleanValue true)))
                      nil)
                    (finally
                      (do (.close ^java.nio.channels.spi.AbstractInterruptibleChannel fc) nil))))
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
                  nil)))))))))
(do
  (clojure.core/in-ns 'datomic.io)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'chunk))
      (clojure.core/require
        ['clojure.java.io :as 'jio]
        ['clojure.edn :as 'edn]
        ['datomic.java.io.stream :as 'stream])
      (clojure.core/import 'org.fressian.impl.BytesOutputStream)
      (clojure.core/import 'org.fressian.impl.ByteBufferInputStream)
      (clojure.core/import 'datomic.impl.JavaByteUtil)
      (clojure.core/import 'java.io.InputStream)
      (clojure.core/import 'java.io.IOException)
      (clojure.core/import 'java.io.OutputStream)
      (clojure.core/import 'java.io.BufferedOutputStream)
      (clojure.core/import 'java.io.OutputStreamWriter)
      (clojure.core/import 'java.io.BufferedReader)
      (clojure.core/import 'java.io.Reader)
      (clojure.core/import 'java.io.InputStreamReader)
      (clojure.core/import 'java.io.PushbackReader)
      (clojure.core/import 'java.io.Closeable)
      (clojure.core/import 'java.io.BufferedWriter)
      (clojure.core/import 'java.io.Writer)
      (clojure.core/import 'java.nio.charset.Charset)
      (clojure.core/import 'java.nio.Buffer)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.nio.channels.ReadableByteChannel)
      (clojure.core/import 'java.nio.channels.WritableByteChannel)
      (clojure.core/import 'java.net.URI)
      (clojure.core/import 'java.net.URL)
      (clojure.core/import 'java.util.zip.GZIPInputStream)
      (clojure.core/import 'java.util.zip.GZIPOutputStream)))
  (when-not (.equals 'datomic.io 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.io))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'chunk))
        (clojure.core/require
          ['clojure.java.io :as 'jio]
          ['clojure.edn :as 'edn]
          ['datomic.java.io.stream :as 'stream])
        (clojure.core/import 'org.fressian.impl.BytesOutputStream)
        (clojure.core/import 'org.fressian.impl.ByteBufferInputStream)
        (clojure.core/import 'datomic.impl.JavaByteUtil)
        (clojure.core/import 'java.io.InputStream)
        (clojure.core/import 'java.io.IOException)
        (clojure.core/import 'java.io.OutputStream)
        (clojure.core/import 'java.io.BufferedOutputStream)
        (clojure.core/import 'java.io.OutputStreamWriter)
        (clojure.core/import 'java.io.BufferedReader)
        (clojure.core/import 'java.io.Reader)
        (clojure.core/import 'java.io.InputStreamReader)
        (clojure.core/import 'java.io.PushbackReader)
        (clojure.core/import 'java.io.Closeable)
        (clojure.core/import 'java.io.BufferedWriter)
        (clojure.core/import 'java.io.Writer)
        (clojure.core/import 'java.nio.charset.Charset)
        (clojure.core/import 'java.nio.Buffer)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.nio.channels.ReadableByteChannel)
        (clojure.core/import 'java.nio.channels.WritableByteChannel)
        (clojure.core/import 'java.net.URI)
        (clojure.core/import 'java.net.URL)
        (clojure.core/import 'java.util.zip.GZIPInputStream)
        (clojure.core/import 'java.util.zip.GZIPOutputStream))))
  (set! *warn-on-reflection* true)
  (defonce ByteSource {})
  (defprotocol ByteSource (slurp-bytes [_]))
  (extend
    java.nio.ByteBuffer
    ByteSource
    {:slurp-bytes
     (fn fn__9289
       ([buff]
         (let [buff (.duplicate ^java.nio.ByteBuffer buff)
               n (.remaining ^java.nio.Buffer buff)
               bytes (byte-array (java.lang.Integer/valueOf (int n)))]
           (.get ^java.nio.ByteBuffer buff ^bytes bytes)
           bytes)))})
  (defonce Coercions {})
  (defprotocol Coercions (as-uri [x]))
  (extend nil Coercions {:as-uri (fn fn__9307 ([s] nil))})
  (extend
    java.lang.String
    Coercions
    {:as-uri (fn fn__9309 ([s] (java.net.URI. ^java.lang.String s)))})
  (extend java.net.URI Coercions {:as-uri (fn fn__9311 ([s] s))})
  (extend java.net.URL Coercions {:as-uri (fn fn__9313 ([s] (.toURI ^java.net.URL s)))})
  (defn byte-source->buffer ([b] (ByteBuffer/wrap (slurp-bytes b))))
  (defn alias-buf-bytes
    ([buff]
      (if (and
            (.hasArray ^java.nio.ByteBuffer buff)
            (=
              (long (.remaining ^java.nio.Buffer buff))
              (long (alength (.array ^java.nio.ByteBuffer buff)))))
        (.array ^java.nio.ByteBuffer buff)
        (slurp-bytes buff))))
  (defn string->bbuf ([s] (ByteBuffer/wrap (.getBytes ^java.lang.String s "UTF-8"))))
  (defn bbuf->string ([bb] (java.lang.String. (alias-buf-bytes bb) "UTF-8")))
  (defn expand-byte-array
    ([buf valid_bytes new_length]
      (if (<= new_length (count buf))
        buf
        (let [expanded_buf (byte-array (max new_length (* 2 (count buf))))]
          (java.lang.System/arraycopy
            buf
            (int 0)
            expanded_buf
            (int 0)
            (int ^java.lang.Number valid_bytes))
          expanded_buf))))
  (defn fill-array
    ([is ba]
      (let [len (alength ^bytes ba)]
        (loop [n 0]
          (when-not (= n len)
            (let [r (.read ^java.io.InputStream is ^bytes ba (int n) (int (- len n)))]
              (if (= -1 r)
                (do (throw (java.lang.Exception. "Premature EOS")) nil)
                (recur (+ n r)))))))))
  (extend
    java.nio.ByteBuffer
    jio/IOFactory
    (assoc
      jio/default-streams-impl
      :make-input-stream
      (fn fn__9322
        ([x opts]
          (jio/make-input-stream
            (org.fressian.impl.ByteBufferInputStream. ^java.nio.ByteBuffer x)
            opts)))))
  (defn fill-buffer
    ([is bb]
      (loop [more (.hasRemaining ^java.nio.Buffer bb)]
        (when more
          (let [n (.read ^java.io.InputStream is)]
            (if (= -1 n)
              (do (throw (java.lang.Exception. "Premature EOS")) nil)
              (do
                (.put ^java.nio.ByteBuffer bb (unchecked-byte n))
                (recur (.hasRemaining ^java.nio.Buffer bb)))))))
      (.flip ^java.nio.ByteBuffer bb)))
  (defn unflipped
    ([b]
      (.limit
        (.position (.duplicate ^java.nio.ByteBuffer b) (int (.limit ^java.nio.Buffer b)))
        (int (.capacity ^java.nio.Buffer b)))))
  (reset-meta!
    #'unflipped
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'b {:tag 'ByteBuffer})]),
       :column 1}
      :name
      'unflipped
      :ns
      *ns*))
  (defn clear-buffer ([bb] (.flip (.clear (.duplicate ^java.nio.ByteBuffer bb)))))
  (reset-meta!
    #'clear-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer})]),
       :column 1}
      :name
      'clear-buffer
      :ns
      *ns*))
  (defn allocate-buffer ([size] (.flip (ByteBuffer/allocate (int ^java.lang.Number size)))))
  (reset-meta!
    #'allocate-buffer
    (assoc
      {:tag java.nio.ByteBuffer, :arglists (clojure.core/list ['size]), :column 1}
      :name
      'allocate-buffer
      :ns
      *ns*))
  (defn expand-buffer
    ([buf ^long extra]
      (let [available (- (.capacity ^java.nio.Buffer buf) (.limit ^java.nio.Buffer buf))]
        (if (< extra available)
          (unflipped buf)
          (let [new_length (max
                             (* 2 (.capacity ^java.nio.Buffer buf))
                             (+ extra (.capacity ^java.nio.Buffer buf)))
                new_buf (if (.isDirect ^java.nio.ByteBuffer buf)
                          (ByteBuffer/allocateDirect (int new_length))
                          (ByteBuffer/allocate (int new_length)))]
            (.put ^java.nio.ByteBuffer new_buf (.duplicate ^java.nio.ByteBuffer buf)))))))
  (reset-meta!
    #'expand-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists
       (clojure.core/list [(.withMeta 'buf {:tag 'ByteBuffer}) (.withMeta 'extra {:tag 'long})]),
       :column 1}
      :name
      'expand-buffer
      :ns
      *ns*))
  (defn append-buffer
    ([dest src]
      (let [result (expand-buffer dest (.remaining ^java.nio.Buffer src))]
        (.put ^java.nio.ByteBuffer result (.duplicate ^java.nio.ByteBuffer src))
        (.flip ^java.nio.ByteBuffer result))))
  (reset-meta!
    #'append-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists
       (clojure.core/list
         [(.withMeta 'dest {:tag 'ByteBuffer}) (.withMeta 'src {:tag 'ByteBuffer})]),
       :column 1}
      :name
      'append-buffer
      :ns
      *ns*))
  (defn directify-buffer
    ([buf]
      (if (.isDirect ^java.nio.ByteBuffer buf)
        buf
        (let [ret (ByteBuffer/allocateDirect (int (.limit ^java.nio.Buffer buf)))]
          (.put ^java.nio.ByteBuffer ret (.duplicate ^java.nio.ByteBuffer buf))
          (.flip ^java.nio.ByteBuffer ret)))))
  (reset-meta!
    #'directify-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'buf {:tag 'ByteBuffer})]),
       :column 1}
      :name
      'directify-buffer
      :ns
      *ns*))
  (defn sub-buffer
    ([bb offset length]
      (.slice
        (.limit
          (.position (.duplicate ^java.nio.ByteBuffer bb) (int offset))
          (int (+ offset length))))))
  (reset-meta!
    #'sub-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer}) 'offset 'length]),
       :column 1}
      :name
      'sub-buffer
      :ns
      *ns*))
  (defn bytestream->buf
    ([stream]
      (ByteBuffer/wrap
        (.internalBuffer ^org.fressian.impl.BytesOutputStream stream)
        (int 0)
        (int (.length ^org.fressian.impl.BytesOutputStream stream)))))
  (reset-meta!
    #'bytestream->buf
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'stream {:tag 'BytesOutputStream})]),
       :column 1}
      :name
      'bytestream->buf
      :ns
      *ns*))
  (defn limit ([buf ^long limit] (.limit (.duplicate ^java.nio.ByteBuffer buf) (int limit))))
  (defn remaining (^long [bb] (.remaining ^java.nio.Buffer bb)))
  (defn position (^long [bb] (.position ^java.nio.Buffer bb)))
  (defn seek
    ([buf ^long n]
      (let [pos (.position ^java.nio.Buffer buf)]
        (.position (.duplicate ^java.nio.ByteBuffer buf) (int (+ n pos))))))
  (defn chunk
    ([buf chunk_size]
      (when-not (integer? chunk_size)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'integer? 'chunk-size))))))
      (when-not (clojure.lang.Numbers/isPos chunk_size)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'pos? 'chunk-size))))))
      (loop [buf (.duplicate ^java.nio.ByteBuffer buf) chunks []]
        (cond
          (= (.remaining ^java.nio.Buffer buf) 0) chunks
          (< (.remaining ^java.nio.Buffer buf) chunk_size) (conj chunks buf)
          :default (do
                     (recur
                       (seek buf (long ^java.lang.Number chunk_size))
                       (conj
                         chunks
                         (limit buf (long (+ (.position ^java.nio.Buffer buf) chunk_size))))))))))
  (defn unchunk
    ([bbufs]
      (let [result (ByteBuffer/allocate
                     (int
                       (apply
                         +
                         (map
                           (fn fn__9339
                             ([p1__9338#]
                               (java.lang.Integer/valueOf
                                 (int (.remaining ^java.nio.Buffer p1__9338#)))))
                           bbufs))))]
        (loop [seq_9341 (seq bbufs) chunk_9342 nil count_9343 0 i_9344 0]
          (if (< i_9344 count_9343)
            (let [bbuf (.nth ^clojure.lang.Indexed chunk_9342 (int i_9344))]
              (.put ^java.nio.ByteBuffer result (.duplicate ^java.nio.ByteBuffer bbuf))
              (recur seq_9341 chunk_9342 count_9343 (inc i_9344)))
            (let [temp__5457__auto__ (seq seq_9341)]
              (when temp__5457__auto__
                (let [seq_9341 temp__5457__auto__]
                  (if (chunked-seq? seq_9341)
                    (let [c__5719__auto__ (chunk-first seq_9341)]
                      (recur
                        (chunk-rest seq_9341)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [bbuf (first seq_9341)]
                      (.put ^java.nio.ByteBuffer result (.duplicate ^java.nio.ByteBuffer bbuf))
                      (recur (next seq_9341) nil 0 0))))))))
        (.flip ^java.nio.ByteBuffer result))))
  (defn gzip-buffer
    ([buff]
      (let [buff (.duplicate ^java.nio.ByteBuffer buff)
            n (.remaining ^java.nio.Buffer buff)
            bytes (if (.hasArray ^java.nio.ByteBuffer buff)
                    (.array ^java.nio.ByteBuffer buff)
                    (alias-buf-bytes buff))
            offset (if (.hasArray ^java.nio.ByteBuffer buff)
                     (java.lang.Integer/valueOf (int (.arrayOffset ^java.nio.ByteBuffer buff)))
                     0)
            os (org.fressian.impl.BytesOutputStream. (int n))]
        (try
          (let [gz (java.util.zip.GZIPOutputStream. ^java.io.OutputStream os)]
            (try
              (let [bs (java.io.BufferedOutputStream. ^java.io.OutputStream gz)]
                (try
                  (do
                    (.write
                      ^java.io.BufferedOutputStream bs
                      ^bytes bytes
                      (int ^java.lang.Number offset)
                      (int n))
                    (.flush ^java.io.BufferedOutputStream bs)
                    (.finish ^java.util.zip.GZIPOutputStream gz)
                    (bytestream->buf os))
                  (finally (do (.close ^java.io.FilterOutputStream bs) nil))))
              (finally (do (.close ^java.util.zip.DeflaterOutputStream gz) nil))))
          (finally (do (.close ^java.io.ByteArrayOutputStream os) nil))))))
  (reset-meta!
    #'gzip-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'buff {:tag 'ByteBuffer})]),
       :column 1}
      :name
      'gzip-buffer
      :ns
      *ns*))
  (defn gunzip-buffer
    ([buff]
      (let [bsize 4096
            bytes (byte-array (long bsize))
            is (org.fressian.impl.ByteBufferInputStream. ^java.nio.ByteBuffer buff)]
        (try
          (let [gz (java.util.zip.GZIPInputStream. ^java.io.InputStream is (int bsize))]
            (try
              (let [bs (stream/buffered-input-stream gz)]
                (try
                  (let [os (org.fressian.impl.BytesOutputStream.
                             (int (* 2 (.remaining ^java.nio.Buffer buff))))]
                    (try
                      (do
                        (loop []
                          (let [n (.read
                                    ^java.io.InputStream bs
                                    ^bytes bytes
                                    (int 0)
                                    (int (alength ^bytes bytes)))]
                            (when-not (= n -1)
                              (.write
                                ^java.io.ByteArrayOutputStream os
                                ^bytes bytes
                                (int 0)
                                (int n))
                              (recur))))
                        (bytestream->buf os))
                      (finally (do (.close ^java.io.ByteArrayOutputStream os) nil))))
                  (finally (do (.close ^java.io.InputStream bs) nil))))
              (finally (do (.close ^java.util.zip.GZIPInputStream gz) nil))))
          (finally (do (.close ^java.io.InputStream is) nil))))))
  (reset-meta!
    #'gunzip-buffer
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list [(.withMeta 'buff {:tag 'ByteBuffer})]),
       :column 1}
      :name
      'gunzip-buffer
      :ns
      *ns*))
  (defn byte-buffer-seq
    ([bb]
      (lazy-seq
        (when (.hasRemaining ^java.nio.Buffer bb)
          (let [next_slice (.slice ^java.nio.ByteBuffer bb)]
            (cons
              (java.lang.Byte/valueOf (byte (.get ^java.nio.ByteBuffer next_slice)))
              (byte-buffer-seq next_slice)))))))
  (defn with-serialization-print-settings
    ([&form &env & forms]
      (seq
        (concat
          (clojure.core/list 'clojure.core/binding)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'clojure.core/*print-length*)
                  (clojure.core/list nil)
                  (clojure.core/list 'clojure.core/*print-level*)
                  (clojure.core/list nil)))))
          forms))))
  (.setMacro #'with-serialization-print-settings)
  (defn clj->bbuf ([s] (binding [*print-length* nil *print-level* nil] (string->bbuf (pr-str s)))))
  (defn bbuf->clj ([bbuf] (edn/read-string (bbuf->string bbuf))))
  (defn valid-buf-limit?
    ([bbuf limit]
      (<= 0 limit (java.lang.Integer/valueOf (int (.capacity ^java.nio.Buffer bbuf))))))
  (defn encode-base128 ([raw] (JavaByteUtil/to7Bit ^bytes raw)))
  (defn decode-base128 ([coded] (JavaByteUtil/to8Bit ^bytes coded)))
  (defn bbuf->base128 ([bbuf] (java.lang.String. (encode-base128 (alias-buf-bytes bbuf)) "UTF-8")))
  (defn base128->bbuf
    ([s]
      (let [bytes (decode-base128 (.getBytes ^java.lang.String s "UTF-8"))]
        (ByteBuffer/wrap ^bytes bytes))))
  (defn read-all
    ([src]
      (let [src src]
        (try
          (let [reader (java.io.PushbackReader. ^java.io.Reader src)]
            (binding [*read-eval* false]
              (loop [tv (transient [])]
                (let [form (read reader false tv)]
                  (if (identical? tv form) (persistent! tv) (recur (conj! tv form)))))))
          (finally (do (.close ^java.io.Reader src) nil))))))
  (defn context-resource
    ([r]
      (let [cl (.getContextClassLoader (java.lang.Thread/currentThread))]
        (.getResource ^java.lang.ClassLoader cl ^java.lang.String r))))
  (defn read-into-buffer
    ([bb n rc]
      (.limit (.clear ^java.nio.ByteBuffer bb) (int n))
      (loop [more (.hasRemaining ^java.nio.Buffer bb)]
        (when more
          (let [n (.read ^java.nio.channels.ReadableByteChannel rc ^java.nio.ByteBuffer bb)
                more (.hasRemaining ^java.nio.Buffer bb)]
            (when (and more (= -1 (long n)))
              (throw (java.io.IOException. "Premature EOS, presumed disconnect")))
            (recur more))))
      (.flip ^java.nio.ByteBuffer bb)))
  (defn read-n-bytes
    ([n rc] (let [bb (ByteBuffer/wrap (byte-array n))] (read-into-buffer bb n rc))))
  (reset-meta!
    #'read-n-bytes
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list ['n (.withMeta 'rc {:tag 'ReadableByteChannel})]),
       :column 1}
      :name
      'read-n-bytes
      :ns
      *ns*))
  (defn read-n-direct-bytes
    ([n rc]
      (let [bb (ByteBuffer/allocateDirect (int ^java.lang.Number n))] (read-into-buffer bb n rc))))
  (reset-meta!
    #'read-n-direct-bytes
    (assoc
      {:tag java.nio.ByteBuffer,
       :arglists (clojure.core/list ['n (.withMeta 'rc {:tag 'ReadableByteChannel})]),
       :column 1}
      :name
      'read-n-direct-bytes
      :ns
      *ns*))
  (defn write-buffer
    ([bb wc]
      (loop [more (.hasRemaining ^java.nio.Buffer bb)]
        (when more
          (java.lang.Integer/valueOf
            (int (.write ^java.nio.channels.WritableByteChannel wc ^java.nio.ByteBuffer bb)))
          (recur (.hasRemaining ^java.nio.Buffer bb))))))
  (defn write-bytes ([bytes wc] (write-buffer (ByteBuffer/wrap ^bytes bytes) wc)))
  (defn crc32
    ([bbuf]
      (long
        (.getValue
          (let [G__9369 (java.util.zip.CRC32.)]
            (.update ^java.util.zip.Checksum G__9369 (alias-buf-bytes bbuf))
            G__9369)))))
  (defn describe-bbuf
    ([bbuf]
      (when (instance? java.nio.ByteBuffer bbuf)
        {:crc32 (crc32 bbuf),
         :size (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer bbuf)))}))))
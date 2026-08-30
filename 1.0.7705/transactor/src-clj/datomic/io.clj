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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      ByteSource
      (slurp-bytes
        [_]
        "Returns copy unless owner always treats arrays as values. Does not consume owner."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.io" "ByteSource")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'ByteSource :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'slurp-bytes
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Returns copy unless owner always treats arrays as values. Does not consume owner."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.io" "ByteSource"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.io" "slurp-bytes")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend
    java.nio.ByteBuffer
    ByteSource
    {:slurp-bytes
     (fn fn__9468
       ([buff]
         (let [buff (.duplicate ^java.nio.ByteBuffer buff)
               n (.remaining ^java.nio.Buffer buff)
               bytes (byte-array (java.lang.Integer/valueOf (int n)))]
           (.get ^java.nio.ByteBuffer buff ^bytes bytes)
           bytes)))})
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol Coercions (as-uri ^URI [x] "Coerce argument to a URI"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.io" "Coercions")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'Coercions :ns *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'as-uri
                                        {:arglists
                                         (clojure.core/list (.withMeta ['x] {:tag 'URI}))}),
                                      :arglists (clojure.core/list (.withMeta ['x] {:tag 'URI})),
                                      :doc "Coerce argument to a URI"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.io" "Coercions"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.io" "as-uri")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*))))
  (extend nil Coercions {:as-uri (fn fn__9486 ([s] nil))})
  (extend
    java.lang.String
    Coercions
    {:as-uri (fn fn__9488 ([s] (java.net.URI. ^java.lang.String s)))})
  (extend java.net.URI Coercions {:as-uri (fn fn__9490 ([s] s))})
  (extend java.net.URL Coercions {:as-uri (fn fn__9492 ([s] (.toURI ^java.net.URL s)))})
  (defn byte-source->buffer ([b] (ByteBuffer/wrap (slurp-bytes b))))
  (reset-meta!
    #'byte-source->buffer
    (assoc
      {:arglists (clojure.core/list ['b]), :column (int 1)}
      :name
      'byte-source->buffer
      :ns
      *ns*))
  (defn alias-buf-bytes
    ([buff]
      (if (and
            (.hasArray ^java.nio.ByteBuffer buff)
            (=
              (long (.remaining ^java.nio.Buffer buff))
              (long (alength (.array ^java.nio.ByteBuffer buff)))))
        (.array ^java.nio.ByteBuffer buff)
        (slurp-bytes buff))))
  (reset-meta!
    #'alias-buf-bytes
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'buff {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'alias-buf-bytes
      :ns
      *ns*))
  (defn string->bbuf ([s] (ByteBuffer/wrap (.getBytes ^java.lang.String s "UTF-8"))))
  (reset-meta!
    #'string->bbuf
    (assoc
      {:arglists (clojure.core/list [(.withMeta 's {:tag 'String})]), :column (int 1)}
      :name
      'string->bbuf
      :ns
      *ns*))
  (defn bbuf->string ([bb] (java.lang.String. (alias-buf-bytes bb) "UTF-8")))
  (reset-meta!
    #'bbuf->string
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'bbuf->string
      :ns
      *ns*))
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
  (reset-meta!
    #'expand-byte-array
    (assoc
      {:arglists (clojure.core/list ['buf 'valid-bytes 'new-length]), :column (int 1)}
      :name
      'expand-byte-array
      :ns
      *ns*))
  (defn fill-array
    ([is ba]
      (let [len (alength ^bytes ba)]
        (loop [n 0]
          (when-not (= n len)
            (let [r (.read ^java.io.InputStream is ^bytes ba (int n) (int (- len n)))]
              (when (= -1 r) (throw (java.lang.Exception. "Premature EOS")))
              (recur (+ n r))))))))
  (reset-meta!
    #'fill-array
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'is {:tag 'InputStream}) (.withMeta 'ba {:tag 'bytes})]),
       :column (int 1)}
      :name
      'fill-array
      :ns
      *ns*))
  (extend
    java.nio.ByteBuffer
    jio/IOFactory
    (assoc
      jio/default-streams-impl
      :make-input-stream
      (fn fn__9501
        ([x opts]
          (jio/make-input-stream
            (org.fressian.impl.ByteBufferInputStream. ^java.nio.ByteBuffer x)
            opts)))))
  (defn fill-buffer
    ([is bb]
      (loop [more (.hasRemaining ^java.nio.Buffer bb)]
        (when more
          (let [n (.read ^java.io.InputStream is)]
            (when (= -1 n) (throw (java.lang.Exception. "Premature EOS")))
            (.put ^java.nio.ByteBuffer bb (unchecked-byte n))
            (recur (.hasRemaining ^java.nio.Buffer bb)))))
      (.flip ^java.nio.ByteBuffer bb)))
  (reset-meta!
    #'fill-buffer
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'is {:tag 'InputStream}) (.withMeta 'bb {:tag 'ByteBuffer})]),
       :column (int 1)}
      :name
      'fill-buffer
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "unflipped")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'b {:tag 'ByteBuffer})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "unflipped")
    (fn unflipped
      ([b]
        (.limit
          (.position (.duplicate ^java.nio.ByteBuffer b) (int (.limit ^java.nio.Buffer b)))
          (int (.capacity ^java.nio.Buffer b))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "clear-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "clear-buffer")
    (fn clear_buffer ([bb] (.flip (.clear (.duplicate ^java.nio.ByteBuffer bb))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "allocate-buffer")
    {:tag java.nio.ByteBuffer, :arglists (clojure.core/list ['size]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "allocate-buffer")
    (fn allocate_buffer ([size] (.flip (ByteBuffer/allocate (int ^java.lang.Number size))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "expand-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists
     (clojure.core/list [(.withMeta 'buf {:tag 'ByteBuffer}) (.withMeta 'extra {:tag 'long})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "expand-buffer")
    (fn expand_buffer
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
              (.put ^java.nio.ByteBuffer new_buf (.duplicate ^java.nio.ByteBuffer buf))))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "append-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists
     (clojure.core/list
       [(.withMeta 'dest {:tag 'ByteBuffer}) (.withMeta 'src {:tag 'ByteBuffer})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "append-buffer")
    (fn append_buffer
      ([dest src]
        (let [result (expand-buffer dest (.remaining ^java.nio.Buffer src))]
          (.put ^java.nio.ByteBuffer result (.duplicate ^java.nio.ByteBuffer src))
          (.flip ^java.nio.ByteBuffer result)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "directify-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'buf {:tag 'ByteBuffer})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "directify-buffer")
    (fn directify_buffer
      ([buf]
        (if (.isDirect ^java.nio.ByteBuffer buf)
          buf
          (let [ret (ByteBuffer/allocateDirect (int (.limit ^java.nio.Buffer buf)))]
            (.put ^java.nio.ByteBuffer ret (.duplicate ^java.nio.ByteBuffer buf))
            (.flip ^java.nio.ByteBuffer ret))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "sub-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer}) 'offset 'length]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "sub-buffer")
    (fn sub_buffer
      ([bb offset length]
        (.slice
          (.limit
            (.position (.duplicate ^java.nio.ByteBuffer bb) (int offset))
            (int (+ offset length)))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "bytestream->buf")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'stream {:tag 'BytesOutputStream})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "bytestream->buf")
    (fn bytestream__GT_buf
      ([stream]
        (ByteBuffer/wrap
          (.internalBuffer ^org.fressian.impl.BytesOutputStream stream)
          (int 0)
          (int (.length ^org.fressian.impl.BytesOutputStream stream))))))
  (defn limit ([buf ^long limit] (.limit (.duplicate ^java.nio.ByteBuffer buf) (int limit))))
  (reset-meta!
    #'limit
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'buf {:tag 'ByteBuffer}) (.withMeta 'limit {:tag 'long})]
           {:tag 'java.nio.ByteBuffer})),
       :column (int 1)}
      :name
      'limit
      :ns
      *ns*))
  (defn remaining (^long [bb] (.remaining ^java.nio.Buffer bb)))
  (reset-meta!
    #'remaining
    (assoc
      {:arglists (clojure.core/list (.withMeta [(.withMeta 'bb {:tag 'ByteBuffer})] {:tag 'long})),
       :column (int 1)}
      :name
      'remaining
      :ns
      *ns*))
  (defn position (^long [bb] (.position ^java.nio.Buffer bb)))
  (reset-meta!
    #'position
    (assoc
      {:arglists (clojure.core/list (.withMeta [(.withMeta 'bb {:tag 'ByteBuffer})] {:tag 'long})),
       :column (int 1)}
      :name
      'position
      :ns
      *ns*))
  (defn seek
    ([buf ^long n]
      (let [pos (.position ^java.nio.Buffer buf)]
        (.position (.duplicate ^java.nio.ByteBuffer buf) (int (+ n pos))))))
  (reset-meta!
    #'seek
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'buf {:tag 'ByteBuffer}) (.withMeta 'n {:tag 'long})]
           {:tag 'java.nio.ByteBuffer})),
       :column (int 1)}
      :name
      'seek
      :ns
      *ns*))
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
  (reset-meta!
    #'chunk
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'buf {:tag 'ByteBuffer}) 'chunk-size]
           {:pre
            [(.withMeta (clojure.core/list 'integer? 'chunk-size) {:column (int 10)})
             (.withMeta (clojure.core/list 'pos? 'chunk-size) {:column (int 33)})]})),
       :column (int 1)}
      :name
      'chunk
      :ns
      *ns*))
  (defn unchunk
    ([bbufs]
      (let [result (ByteBuffer/allocate
                     (int
                       (apply
                         +
                         (map
                           (fn fn__9518
                             ([p1__9517#]
                               (java.lang.Integer/valueOf
                                 (int (.remaining ^java.nio.Buffer p1__9517#)))))
                           bbufs))))]
        (loop [seq_9520 (seq bbufs) chunk_9521 nil count_9522 0 i_9523 0]
          (if (< i_9523 count_9522)
            (let [bbuf (.nth ^clojure.lang.Indexed chunk_9521 (int i_9523))]
              (.put ^java.nio.ByteBuffer result (.duplicate ^java.nio.ByteBuffer bbuf))
              (recur seq_9520 chunk_9521 count_9522 (inc i_9523)))
            (let [temp__5825__auto__ (seq seq_9520)]
              (when temp__5825__auto__
                (let [seq_9520 temp__5825__auto__]
                  (if (chunked-seq? seq_9520)
                    (let [c__6090__auto__ (chunk-first seq_9520)]
                      (recur
                        (chunk-rest seq_9520)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [bbuf (first seq_9520)]
                      (.put ^java.nio.ByteBuffer result (.duplicate ^java.nio.ByteBuffer bbuf))
                      (recur (next seq_9520) nil 0 0))))))))
        (.flip ^java.nio.ByteBuffer result))))
  (reset-meta!
    #'unchunk
    (assoc
      {:arglists (clojure.core/list (.withMeta ['bbufs] {:tag 'java.nio.ByteBuffer})),
       :column (int 1)}
      :name
      'unchunk
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "gzip-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'buff {:tag 'ByteBuffer})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "gzip-buffer")
    (fn gzip_buffer
      ([buff]
        (let [buff (.duplicate ^java.nio.ByteBuffer buff)
              n (.remaining ^java.nio.Buffer buff)
              bytes (if (.hasArray ^java.nio.ByteBuffer buff)
                      (.array ^java.nio.ByteBuffer buff)
                      (alias-buf-bytes buff))
              offset (if (.hasArray ^java.nio.ByteBuffer buff)
                       (java.lang.Integer/valueOf (int (.arrayOffset ^java.nio.ByteBuffer buff)))
                       0)]
          (with-open [os (org.fressian.impl.BytesOutputStream. (int n))]
            (with-open [gz (java.util.zip.GZIPOutputStream. ^java.io.OutputStream os)]
              (with-open [bs (java.io.BufferedOutputStream. ^java.io.OutputStream gz)]
                (do
                  (.write
                    ^java.io.BufferedOutputStream bs
                    ^bytes bytes
                    (int ^java.lang.Number offset)
                    (int n))
                  (.flush ^java.io.BufferedOutputStream bs)
                  (.finish ^java.util.zip.GZIPOutputStream gz)
                  (bytestream->buf os)))))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "gunzip-buffer")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list [(.withMeta 'buff {:tag 'ByteBuffer})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "gunzip-buffer")
    (fn gunzip_buffer
      ([buff]
        (let [bsize 4096 bytes (byte-array (long bsize))]
          (with-open [is (org.fressian.impl.ByteBufferInputStream. ^java.nio.ByteBuffer buff)]
            (with-open [gz (java.util.zip.GZIPInputStream. ^java.io.InputStream is (int bsize))]
              (with-open [bs (stream/buffered-input-stream gz)]
                (with-open [os (org.fressian.impl.BytesOutputStream.
                                 (int (* 2 (.remaining ^java.nio.Buffer buff))))]
                  (do
                    (loop []
                      (let [n (.read
                                ^java.io.InputStream bs
                                ^bytes bytes
                                (int 0)
                                (int (alength ^bytes bytes)))]
                        (when-not (= n -1)
                          (.write ^java.io.ByteArrayOutputStream os ^bytes bytes (int 0) (int n))
                          (recur))))
                    (bytestream->buf os))))))))))
  (defn byte-buffer-seq
    ([bb]
      (lazy-seq
        (when (.hasRemaining ^java.nio.Buffer bb)
          (let [next_slice (.slice ^java.nio.ByteBuffer bb)]
            (cons
              (java.lang.Byte/valueOf (byte (.get ^java.nio.ByteBuffer next_slice)))
              (byte-buffer-seq next_slice)))))))
  (reset-meta!
    #'byte-buffer-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'byte-buffer-seq
      :ns
      *ns*))
  (def with-serialization-print-settings
   (fn with_serialization_print_settings
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
           forms)))))
  (reset-meta!
    #'with-serialization-print-settings
    (assoc
      {:arglists (clojure.core/list ['& 'forms]), :column (int 1)}
      :name
      'with-serialization-print-settings
      :ns
      *ns*))
  (.setMacro #'with-serialization-print-settings)
  (defn clj->bbuf ([s] (binding [*print-length* nil *print-level* nil] (string->bbuf (pr-str s)))))
  (reset-meta!
    #'clj->bbuf
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'clj->bbuf :ns *ns*))
  (defn bbuf->clj ([bbuf] (edn/read-string (bbuf->string bbuf))))
  (reset-meta!
    #'bbuf->clj
    (assoc {:arglists (clojure.core/list ['bbuf]), :column (int 1)} :name 'bbuf->clj :ns *ns*))
  (defn valid-buf-limit?
    ([bbuf limit]
      (<= 0 limit (java.lang.Integer/valueOf (int (.capacity ^java.nio.Buffer bbuf))))))
  (reset-meta!
    #'valid-buf-limit?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bbuf {:tag 'Buffer}) 'limit]), :column (int 1)}
      :name
      'valid-buf-limit?
      :ns
      *ns*))
  (defn encode-base128 ([raw] (JavaByteUtil/to7Bit ^bytes raw)))
  (reset-meta!
    #'encode-base128
    (assoc
      {:arglists (clojure.core/list (.withMeta ['raw] {:tag 'bytes})), :column (int 1)}
      :name
      'encode-base128
      :ns
      *ns*))
  (defn decode-base128 ([coded] (JavaByteUtil/to8Bit ^bytes coded)))
  (reset-meta!
    #'decode-base128
    (assoc
      {:arglists (clojure.core/list ['coded]), :column (int 1)}
      :name
      'decode-base128
      :ns
      *ns*))
  (defn bbuf->base128 ([bbuf] (java.lang.String. (encode-base128 (alias-buf-bytes bbuf)) "UTF-8")))
  (reset-meta!
    #'bbuf->base128
    (assoc {:arglists (clojure.core/list ['bbuf]), :column (int 1)} :name 'bbuf->base128 :ns *ns*))
  (defn base128->bbuf
    ([s]
      (let [bytes (decode-base128 (.getBytes ^java.lang.String s "UTF-8"))]
        (ByteBuffer/wrap ^bytes bytes))))
  (reset-meta!
    #'base128->bbuf
    (assoc
      {:arglists (clojure.core/list [(.withMeta 's {:tag 'String})]), :column (int 1)}
      :name
      'base128->bbuf
      :ns
      *ns*))
  (defn read-all
    ([src]
      (with-open [src src]
        (let [reader (java.io.PushbackReader. ^java.io.Reader src)]
          (binding [*read-eval* false]
            (loop [tv (transient [])]
              (let [form (read reader false tv)]
                (if (identical? tv form) (persistent! tv) (recur (conj! tv form))))))))))
  (reset-meta!
    #'read-all
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'src {:tag 'Reader})]), :column (int 1)}
      :name
      'read-all
      :ns
      *ns*))
  (defn context-resource
    ([r]
      (let [cl (.getContextClassLoader (java.lang.Thread/currentThread))]
        (.getResource ^java.lang.ClassLoader cl ^java.lang.String r))))
  (reset-meta!
    #'context-resource
    (assoc {:arglists (clojure.core/list ['r]), :column (int 1)} :name 'context-resource :ns *ns*))
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
  (reset-meta!
    #'read-into-buffer
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'bb {:tag 'ByteBuffer}) 'n (.withMeta 'rc {:tag 'ReadableByteChannel})]),
       :column (int 1)}
      :name
      'read-into-buffer
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "read-n-bytes")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list ['n (.withMeta 'rc {:tag 'ReadableByteChannel})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "read-n-bytes")
    (fn read_n_bytes
      ([n rc] (let [bb (ByteBuffer/wrap (byte-array n))] (read-into-buffer bb n rc)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.io" "read-n-direct-bytes")
    {:tag java.nio.ByteBuffer,
     :arglists (clojure.core/list ['n (.withMeta 'rc {:tag 'ReadableByteChannel})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.io" "read-n-direct-bytes")
    (fn read_n_direct_bytes
      ([n rc]
        (let [bb (ByteBuffer/allocateDirect (int ^java.lang.Number n))]
          (read-into-buffer bb n rc)))))
  (defn write-buffer
    ([bb wc]
      (loop [more (.hasRemaining ^java.nio.Buffer bb)]
        (when more
          (.write ^java.nio.channels.WritableByteChannel wc ^java.nio.ByteBuffer bb)
          (recur (.hasRemaining ^java.nio.Buffer bb))))))
  (reset-meta!
    #'write-buffer
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'bb {:tag 'ByteBuffer}) (.withMeta 'wc {:tag 'WritableByteChannel})]),
       :column (int 1)}
      :name
      'write-buffer
      :ns
      *ns*))
  (defn write-bytes ([bytes wc] (write-buffer (ByteBuffer/wrap ^bytes bytes) wc)))
  (reset-meta!
    #'write-bytes
    (assoc
      {:arglists (clojure.core/list ['bytes 'wc]), :column (int 1)}
      :name
      'write-bytes
      :ns
      *ns*))
  (defn crc32
    ([bbuf]
      (long
        (.getValue
          (let [G__9548 (java.util.zip.CRC32.)]
            (.update ^java.util.zip.Checksum G__9548 (alias-buf-bytes bbuf))
            G__9548)))))
  (reset-meta!
    #'crc32
    (assoc {:arglists (clojure.core/list ['bbuf]), :column (int 1)} :name 'crc32 :ns *ns*))
  (defn describe-bbuf
    ([bbuf]
      (when (instance? java.nio.ByteBuffer bbuf)
        {:crc32 (crc32 bbuf),
         :size (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer bbuf)))})))
  (reset-meta!
    #'describe-bbuf
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bbuf {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'describe-bbuf
      :ns
      *ns*)))
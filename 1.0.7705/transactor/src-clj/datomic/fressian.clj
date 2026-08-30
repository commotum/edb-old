(do
  (clojure.core/in-ns 'datomic.fressian)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['pr 'read])
      (clojure.core/require
        'datomic.function
        ['clojure.java.io :as 'jio]
        ['datomic.java.io.stream :as 'stream]
        ['datomic.iter :as 'iter]
        ['datomic.io :as 'io]
        ['datomic.common :as 'common]
        ['datomic.monitor :as 'monitor]
        ['datomic.measure.io-stats :as 'io-stats]
        ['datomic.queue :as 'queue]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'java.io.InputStream)
      (clojure.core/import 'java.io.OutputStream)
      (clojure.core/import 'java.io.EOFException)
      (clojure.core/import 'java.io.PipedInputStream)
      (clojure.core/import 'java.io.PipedOutputStream)
      (clojure.core/import 'java.io.ByteArrayOutputStream)
      (clojure.core/import 'java.io.ByteArrayInputStream)
      (clojure.core/import 'java.util.zip.GZIPInputStream)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'org.fressian.FressianWriter)
      (clojure.core/import 'org.fressian.StreamingWriter)
      (clojure.core/import 'org.fressian.FressianReader)
      (clojure.core/import 'org.fressian.Writer)
      (clojure.core/import 'org.fressian.Reader)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'org.fressian.handlers.ILookup)
      (clojure.core/import 'org.fressian.handlers.WriteHandlerLookup)
      (clojure.core/import 'org.fressian.impl.BytesOutputStream)
      (clojure.core/import 'org.fressian.impl.ByteBufferInputStream)))
  (when-not (.equals 'datomic.fressian 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.fressian))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['pr 'read])
        (clojure.core/require
          'datomic.function
          ['clojure.java.io :as 'jio]
          ['datomic.java.io.stream :as 'stream]
          ['datomic.iter :as 'iter]
          ['datomic.io :as 'io]
          ['datomic.common :as 'common]
          ['datomic.monitor :as 'monitor]
          ['datomic.measure.io-stats :as 'io-stats]
          ['datomic.queue :as 'queue]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'java.io.InputStream)
        (clojure.core/import 'java.io.OutputStream)
        (clojure.core/import 'java.io.EOFException)
        (clojure.core/import 'java.io.PipedInputStream)
        (clojure.core/import 'java.io.PipedOutputStream)
        (clojure.core/import 'java.io.ByteArrayOutputStream)
        (clojure.core/import 'java.io.ByteArrayInputStream)
        (clojure.core/import 'java.util.zip.GZIPInputStream)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'org.fressian.FressianWriter)
        (clojure.core/import 'org.fressian.StreamingWriter)
        (clojure.core/import 'org.fressian.FressianReader)
        (clojure.core/import 'org.fressian.Writer)
        (clojure.core/import 'org.fressian.Reader)
        (clojure.core/import 'org.fressian.handlers.WriteHandler)
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'org.fressian.handlers.ILookup)
        (clojure.core/import 'org.fressian.handlers.WriteHandlerLookup)
        (clojure.core/import 'org.fressian.impl.BytesOutputStream)
        (clojure.core/import 'org.fressian.impl.ByteBufferInputStream))))
  (set! *warn-on-reflection* true)
  (defn as-lookup
    ([o] (if (map? o) (reify org.fressian.handlers.ILookup (valAt [this k] (get o k))) o)))
  (reset-meta!
    #'as-lookup
    (assoc {:arglists (clojure.core/list ['o]), :column (int 1)} :name 'as-lookup :ns *ns*))
  (def write-handler-lookup
   (fn write_handler_lookup
     ([custom_lookup] (WriteHandlerLookup/createLookupChain (as-lookup custom_lookup)))))
  (reset-meta!
    #'write-handler-lookup
    (assoc
      {:arglists (clojure.core/list ['custom-lookup]), :column (int 1)}
      :name
      'write-handler-lookup
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.fressian" "create-writer")
    {:tag org.fressian.Writer,
     :arglists (clojure.core/list ['out] ['out 'lookup]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "create-writer")
    (fn create_writer
      ([out lookup] (org.fressian.FressianWriter. ^java.io.OutputStream out (as-lookup lookup)))
      ([out] (create-writer out nil))))
  (.setMeta
    (clojure.lang.RT/var "datomic.fressian" "create-reader")
    {:tag org.fressian.Reader,
     :arglists (clojure.core/list ['in] ['in 'lookup] ['in 'lookup 'validate-checksum]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "create-reader")
    (fn create_reader
      ([in lookup validate_checksum]
        (org.fressian.FressianReader.
          (if (instance? java.io.InputStream in) in (jio/input-stream in))
          (as-lookup lookup)
          (boolean (.booleanValue ^java.lang.Boolean validate_checksum))))
      ([in lookup] (create-reader in lookup true))
      ([in] (create-reader in nil))))
  (def begin-open-list
   (fn begin_open_list ([writer] (.beginOpenList ^org.fressian.StreamingWriter writer))))
  (reset-meta!
    #'begin-open-list
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'writer {:tag 'StreamingWriter})]),
       :column (int 1)}
      :name
      'begin-open-list
      :ns
      *ns*))
  (def begin-closed-list
   (fn begin_closed_list ([writer] (.beginClosedList ^org.fressian.StreamingWriter writer))))
  (reset-meta!
    #'begin-closed-list
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'writer {:tag 'StreamingWriter})]),
       :column (int 1)}
      :name
      'begin-closed-list
      :ns
      *ns*))
  (def end-list (fn end_list ([writer] (.endList ^org.fressian.StreamingWriter writer))))
  (reset-meta!
    #'end-list
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'writer {:tag 'StreamingWriter})]),
       :column (int 1)}
      :name
      'end-list
      :ns
      *ns*))
  (def fressian
   (fn fressian
     ([out obj & p__11472]
       (let [map__11473 p__11472
             map__11473 (if (seq? map__11473)
                          (if (next map__11473)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__11473))
                            (if (seq map__11473) (first map__11473) {}))
                          map__11473)
             handlers (get map__11473 :handlers)
             footer (get map__11473 :footer)]
         (with-open [os (jio/output-stream out)]
           (let [writer (create-writer os handlers)]
             (.writeObject ^org.fressian.Writer writer obj)
             (when footer (.writeFooter ^org.fressian.Writer writer))))))))
  (reset-meta!
    #'fressian
    (assoc
      {:arglists (clojure.core/list ['out 'obj '& {:keys ['handlers 'footer]}]), :column (int 1)}
      :name
      'fressian
      :ns
      *ns*))
  (def defressian
   (fn defressian
     ([in & p__11475]
       (let [map__11476 p__11475
             map__11476 (if (seq? map__11476)
                          (if (next map__11476)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__11476))
                            (if (seq map__11476) (first map__11476) {}))
                          map__11476)
             handlers (get map__11476 :handlers)
             footer (get map__11476 :footer)
             fin (create-reader in handlers (boolean footer))
             result (.readObject ^org.fressian.Reader fin)]
         (when footer (.validateFooter ^org.fressian.Reader fin))
         result))))
  (reset-meta!
    #'defressian
    (assoc
      {:arglists (clojure.core/list ['in '& {:keys ['handlers 'footer]}]), :column (int 1)}
      :name
      'defressian
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.fressian" "byte-buf")
    {:tag java.nio.ByteBuffer, :arglists (clojure.core/list ['obj '& 'options]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "byte-buf")
    (fn byte_buf
      ([obj & options]
        (let [baos (org.fressian.impl.BytesOutputStream.)]
          (apply fressian baos obj options)
          (io/bytestream->buf baos)))))
  (defn fressian-val
    ([val handlers] (io/gzip-buffer (byte-buf val :handlers handlers :footer true))))
  (reset-meta!
    #'fressian-val
    (assoc
      {:arglists (clojure.core/list ['val 'handlers]), :column (int 1)}
      :name
      'fressian-val
      :ns
      *ns*))
  (extend
    org.fressian.FressianReader
    queue/BlockingConsumer
    {:take (fn fn__11480 ([reader] (.readObject ^org.fressian.FressianReader reader)))})
  (def read-batch
   (fn read_batch
     ([fin]
       (let [sentinel (java.lang.Object.)]
         (loop [objects []]
           (let [obj (try
                       (.readObject ^org.fressian.Reader fin)
                       (catch java.io.EOFException e sentinel))]
             (if (= obj sentinel) objects (recur (conj objects obj)))))))))
  (reset-meta!
    #'read-batch
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'fin {:tag 'Reader})]), :column (int 1)}
      :name
      'read-batch
      :ns
      *ns*))
  (def read-seq
   (fn read_seq
     ([readable handler_lookup]
       (let [map__11485 (queue/queue-seq 100)
             map__11485 (if (seq? map__11485)
                          (if (next map__11485)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__11485))
                            (if (seq map__11485) (first map__11485) {}))
                          map__11485)
             fill (get map__11485 :fill)
             done (get map__11485 :done)
             drain (get map__11485 :drain)]
         (future-call
           (fn fn__11486
             ([]
               (try
                 (with-open [s (jio/input-stream readable)]
                   (do
                     (let [f (create-reader s handler_lookup)]
                       (loop []
                         (if (= (.available ^java.io.InputStream s) 0)
                           (^clojure.lang.IFn done)
                           (do
                             (^clojure.lang.IFn fill (.readObject ^org.fressian.Reader f))
                             (recur)))))
                     nil))
                 (catch java.lang.Throwable t (^clojure.lang.IFn fill t))))))
         (^clojure.lang.IFn drain)))
     ([readable] (read-seq readable nil))))
  (reset-meta!
    #'read-seq
    (assoc
      {:arglists (clojure.core/list ['readable] ['readable 'handler-lookup]), :column (int 1)}
      :name
      'read-seq
      :ns
      *ns*))
  (def write-named
   (fn write_named
     ([tag w s]
       (.writeTag ^org.fressian.Writer w tag (int 2))
       (.writeObject ^org.fressian.Writer w (namespace s) (boolean (.booleanValue true)))
       (.writeObject ^org.fressian.Writer w (name s) (boolean (.booleanValue true))))))
  (reset-meta!
    #'write-named
    (assoc
      {:arglists (clojure.core/list ['tag (.withMeta 'w {:tag 'Writer}) 's]), :column (int 1)}
      :name
      'write-named
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.fressian" "clojure-write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "clojure-write-handlers")
    {clojure.lang.Keyword
     {"key"
      (reify
        org.fressian.handlers.WriteHandler
        (^void write [this ^org.fressian.Writer w s] (do (write-named "key" w s) nil)))},
     clojure.lang.BigInt
     {"bigint"
      (reify
        org.fressian.handlers.WriteHandler
        (^void write
          [this ^org.fressian.Writer w d]
          (do
            (let [bi (if (instance? clojure.lang.BigInt d)
                       (.toBigInteger ^clojure.lang.BigInt d)
                       d)]
              (.writeTag ^org.fressian.Writer w "bigint" (int 1))
              (.writeBytes ^org.fressian.Writer w (.toByteArray ^java.math.BigInteger bi)))
            nil)))},
     clojure.lang.Symbol
     {"sym"
      (reify
        org.fressian.handlers.WriteHandler
        (^void write [this ^org.fressian.Writer w s] (do (write-named "sym" w s) nil)))}})
  (.setMeta (clojure.lang.RT/var "datomic.fressian" "clojure-read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "clojure-read-handlers")
    {"key"
     (reify
       org.fressian.handlers.ReadHandler
       (read
         [this ^org.fressian.Reader rdr tag ^int component_count]
         (keyword (.readObject ^org.fressian.Reader rdr) (.readObject ^org.fressian.Reader rdr)))),
     "sym"
     (reify
       org.fressian.handlers.ReadHandler
       (read
         [this ^org.fressian.Reader rdr tag ^int component_count]
         (symbol (.readObject ^org.fressian.Reader rdr) (.readObject ^org.fressian.Reader rdr)))),
     "map"
     (reify
       org.fressian.handlers.ReadHandler
       (read
         [this ^org.fressian.Reader rdr tag ^int component_count]
         (let [kvs (.readObject ^org.fressian.Reader rdr)]
           (if (< (.size ^java.util.List kvs) 16)
             (clojure.lang.PersistentArrayMap. (.toArray ^java.util.List kvs))
             (clojure.lang.PersistentHashMap/create (seq kvs))))))})
  (.setMeta (clojure.lang.RT/var "datomic.fressian" "user-write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "user-write-handlers")
    (merge
      clojure-write-handlers
      {datomic.function.Function
       {"datomic/fn"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w f]
            (do
              (.writeTag ^org.fressian.Writer w "datomic/fn" (int 1))
              (.writeObject
                ^org.fressian.Writer w
                (select-keys f [:lang :imports :requires :params :code]))
              nil)))}}
      (datomic.impl.Config/getWriteHandlers)))
  (.setMeta (clojure.lang.RT/var "datomic.fressian" "user-read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fressian" "user-read-handlers")
    (merge
      clojure-read-handlers
      {"datomic/fn"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (let [m (.readObject ^org.fressian.Reader rdr)] (datomic.function/construct m))))}
      (datomic.impl.Config/getReadHandlers)))
  (defn record-latencies
    ([nanos _]
      (io-stats/inc! :deserialize)
      (monitor/add-stat :DeserializeNsec nanos)
      (io-stats/inc! :deserialize-ns (long ^java.lang.Number nanos))))
  (reset-meta!
    #'record-latencies
    (assoc
      {:private true, :arglists (clojure.core/list ['nanos '_]), :column (int 1)}
      :name
      'record-latencies
      :ns
      *ns*))
  (def val->obj
   (fn val__GT_obj
     ([read_lookup]
       (fn fn__11507
         ([val]
           (let [start__8781__auto__ (java.lang.System/nanoTime)
                 result__8782__auto__ (try
                                        (with-open [is (org.fressian.impl.ByteBufferInputStream.
                                                         ^java.nio.ByteBuffer val)]
                                          (with-open [gz (java.util.zip.GZIPInputStream.
                                                           ^java.io.InputStream is
                                                           (int 4096))]
                                            (with-open [bs (stream/buffered-input-stream gz)]
                                              (let [fr (create-reader bs read_lookup false)
                                                    obj (.readObject ^org.fressian.Reader fr)]
                                                (.readAllBytes ^java.io.InputStream gz)
                                                obj))))
                                        (catch java.lang.Throwable e e))]
             (record-latencies
               (long (- (java.lang.System/nanoTime) start__8781__auto__))
               result__8782__auto__)
             (common/return-or-throw result__8782__auto__)))))))
  (reset-meta!
    #'val->obj
    (assoc
      {:arglists (clojure.core/list ['read-lookup]), :column (int 1)}
      :name
      'val->obj
      :ns
      *ns*))
  (deftype
    FressianIter
    [reader ^{:unsynchronized-mutable true} item]
    datomic.iter.Iter
    (next
      [this]
      (try
        (do (set! item (.readObject ^org.fressian.Reader reader)) this)
        (catch java.io.EOFException _ nil)))
    (get [this] item))
  (clojure.core/import 'datomic.fressian.FressianIter)
  (defn ->FressianIter ([reader item] (datomic.fressian.FressianIter. reader item)))
  (reset-meta!
    #'->FressianIter
    (assoc
      {:arglists (clojure.core/list ['reader 'item]), :column (int 1)}
      :name
      '->FressianIter
      :ns
      *ns*))
  (defn reader-iter
    ([is handlers] (.next (datomic.fressian.FressianIter. (create-reader is handlers) nil))))
  (reset-meta!
    #'reader-iter
    (assoc
      {:arglists (clojure.core/list ['is 'handlers]), :column (int 1)}
      :name
      'reader-iter
      :ns
      *ns*))
  (def fressianable?
   (fn fressianable_QMARK_
     ([val handlers]
       (boolean (try (fressian-val val handlers) (catch java.lang.Throwable _ false))))
     ([val] (fressianable? val clojure-write-handlers))))
  (reset-meta!
    #'fressianable?
    (assoc
      {:arglists (clojure.core/list ['val] ['val 'handlers]), :column (int 1)}
      :name
      'fressianable?
      :ns
      *ns*)))
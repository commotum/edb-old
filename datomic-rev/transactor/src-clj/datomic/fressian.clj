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
  (defn write-handler-lookup
    ([custom_lookup] (WriteHandlerLookup/createLookupChain (as-lookup custom_lookup))))
  (defn create-writer
    ([out lookup] (org.fressian.FressianWriter. ^java.io.OutputStream out (as-lookup lookup)))
    ([out] (create-writer out nil)))
  (reset-meta!
    #'create-writer
    (assoc
      {:tag org.fressian.Writer, :arglists (clojure.core/list ['out] ['out 'lookup]), :column 1}
      :name
      'create-writer
      :ns
      *ns*))
  (defn create-reader
    ([in lookup validate_checksum]
      (org.fressian.FressianReader.
        (if (instance? java.io.InputStream in) in (jio/input-stream in))
        (as-lookup lookup)
        (boolean (.booleanValue ^java.lang.Boolean validate_checksum))))
    ([in lookup] (create-reader in lookup true))
    ([in] (create-reader in nil)))
  (reset-meta!
    #'create-reader
    (assoc
      {:tag org.fressian.Reader,
       :arglists (clojure.core/list ['in] ['in 'lookup] ['in 'lookup 'validate-checksum]),
       :column 1}
      :name
      'create-reader
      :ns
      *ns*))
  (defn begin-open-list ([writer] (.beginOpenList ^org.fressian.StreamingWriter writer)))
  (defn begin-closed-list ([writer] (.beginClosedList ^org.fressian.StreamingWriter writer)))
  (defn end-list ([writer] (.endList ^org.fressian.StreamingWriter writer)))
  (defn fressian
    ([out obj & p__9639]
      (let [map__9640 p__9639
            map__9640 (if (seq? map__9640)
                        (if (next map__9640)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9640))
                          (if (seq map__9640) (first map__9640) {}))
                        map__9640)
            handlers (get map__9640 :handlers)
            footer (get map__9640 :footer)]
        (with-open [os (jio/output-stream out)]
          (let [writer (create-writer os handlers)]
            (.writeObject ^org.fressian.Writer writer obj)
            (when footer (.writeFooter ^org.fressian.Writer writer)))))))
  (defn defressian
    ([in & p__9642]
      (let [map__9643 p__9642
            map__9643 (if (seq? map__9643)
                        (if (next map__9643)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9643))
                          (if (seq map__9643) (first map__9643) {}))
                        map__9643)
            handlers (get map__9643 :handlers)
            footer (get map__9643 :footer)
            fin (create-reader in handlers (boolean footer))
            result (.readObject ^org.fressian.Reader fin)]
        (when footer (.validateFooter ^org.fressian.Reader fin))
        result)))
  (defn byte-buf
    ([obj & options]
      (let [baos (org.fressian.impl.BytesOutputStream.)]
        (apply fressian baos obj options)
        (io/bytestream->buf baos))))
  (reset-meta!
    #'byte-buf
    (assoc
      {:tag java.nio.ByteBuffer, :arglists (clojure.core/list ['obj '& 'options]), :column 1}
      :name
      'byte-buf
      :ns
      *ns*))
  (defn fressian-val
    ([val handlers] (io/gzip-buffer (byte-buf val :handlers handlers :footer true))))
  (extend
    org.fressian.FressianReader
    queue/BlockingConsumer
    {:take (fn fn__9647 ([reader] (.readObject ^org.fressian.FressianReader reader)))})
  (defn read-batch
    ([fin]
      (let [sentinel (java.lang.Object.)]
        (loop [objects []]
          (let [obj (try
                      (.readObject ^org.fressian.Reader fin)
                      (catch java.io.EOFException e sentinel))]
            (if (= obj sentinel) objects (recur (conj objects obj))))))))
  (defn read-seq
    ([readable handler_lookup]
      (let [map__9652 (queue/queue-seq 100)
            map__9652 (if (seq? map__9652)
                        (if (next map__9652)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9652))
                          (if (seq map__9652) (first map__9652) {}))
                        map__9652)
            fill (get map__9652 :fill)
            done (get map__9652 :done)
            drain (get map__9652 :drain)]
        (future-call
          (fn fn__9653
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
    ([readable] (read-seq readable nil)))
  (defn write-named
    ([tag w s]
      (.writeTag ^org.fressian.Writer w tag (int 2))
      (.writeObject ^org.fressian.Writer w (namespace s) (boolean (.booleanValue true)))
      (.writeObject ^org.fressian.Writer w (name s) (boolean (.booleanValue true)))))
  (def clojure-write-handlers
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
  (def clojure-read-handlers
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
  (def user-write-handlers
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
  (def user-read-handlers
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
      {:private true, :arglists (clojure.core/list ['nanos '_]), :column 1}
      :name
      'record-latencies
      :ns
      *ns*))
  (defn val->obj
    ([read_lookup]
      (fn fn__9674
        ([val]
          (let [start__8845__auto__ (java.lang.System/nanoTime)
                result__8846__auto__ (try
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
              (long (- (java.lang.System/nanoTime) start__8845__auto__))
              result__8846__auto__)
            (common/return-or-throw result__8846__auto__))))))
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
  (defn reader-iter
    ([is handlers] (.next (datomic.fressian.FressianIter. (create-reader is handlers) nil))))
  (defn fressianable?
    ([val handlers]
      (boolean (try (fressian-val val handlers) (catch java.lang.Throwable _ false))))
    ([val] (fressianable? val clojure-write-handlers))))
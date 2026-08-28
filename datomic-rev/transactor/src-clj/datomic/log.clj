(do
  (clojure.core/in-ns 'datomic.log)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.log)
    {:doc
     "Database log.\n\nLog comprises a tree, plus a tail pod that does three jobs:\n\n- points to the log tree, via the :d/r key in pod-meta\n- holds the tail of data not in the tree yet, in the pod \"value\"\n- coordinates who is allowed to write transactions\n\nThat sounds complected, and it is. The pod code is subtle and does not\nrepresent our ideas on best practice. It works the way it does for\ncompatibility with existing databases.\n\nAll pod writes increment the revision, which is a CAS guard that\nensures serialization of all activity on the pod. There are several\nkinds of calls to cluster/update-pod:\n\n- Claim the pod by passing a nil buf and the etag from the previous\n  value. This leaves the pod value and metadata unchanged, while\n  causing other writers to encounter a conflict on their next write.\n\n- Append transaction data by passing the previous etag and a buf which\n  gets appended to the buf already in the pod.\n\n- Adopt a new root by setting :d/r in the pod metadata, passing a\n  buf with whatever data is not yet in the root, and setting the\n  etag to nil to reset the pod value to the buf passed in.\n\n- Create a new database log by setting etag to nil, :d/r to point to a\n  new, empty tree, and setting the pod value to an empty list of\n  transactions.\n\nTransactions in the tail pod or in a leaf segment each are their own\ncaching context, and therefore must be preceded by a Fressian\nRESET_CACHES code.\n\nAn empty list of transactions in the pod buffer is the only place\nwhere Datomic uses Fressian's BEGIN_OPEN_LIST bytecode."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['compare])
      (clojure.core/require
        ['datomic.fressian :as 'fressian]
        ['datomic.index :as 'index]
        ['datomic.promise :as 'promise]
        ['datomic.transaction :as 'tx]
        ['datomic.cluster :as 'cluster :refer (clojure.core/list 'uuid->val-key)]
        ['datomic.common :as 'common :refer (clojure.core/list 'getx 'rand-uuid 'require-keys)]
        ['datomic.cache :as 'cache]
        ['datomic.monitor :as 'monitor]
        ['datomic.process.events :as 'events]
        ['datomic.slf4j :as 'logger]
        ['datomic.config :as 'config]
        ['datomic.iter :as 'iter]
        ['datomic.memory-size :as 'size]
        ['datomic.db :as 'db]
        ['datomic.io
         :as
         'io
         :refer
         (clojure.core/list 'bytestream->buf 'position 'remaining 'sub-buffer)]
        ['datomic.excise :as 'x]
        ['datomic.error :as 'error])
      (clojure.core/import 'java.io.ByteArrayOutputStream)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.Collections)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'org.fressian.StreamingWriter)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)
      (clojure.core/import 'org.fressian.impl.Codes)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'org.fressian.impl.ByteBufferInputStream)
      (clojure.core/import 'org.fressian.impl.BytesOutputStream)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.db.IDbImpl)))
  (when-not (.equals 'datomic.log 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.log))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['compare])
        (clojure.core/require
          ['datomic.fressian :as 'fressian]
          ['datomic.index :as 'index]
          ['datomic.promise :as 'promise]
          ['datomic.transaction :as 'tx]
          ['datomic.cluster :as 'cluster :refer (clojure.core/list 'uuid->val-key)]
          ['datomic.common :as 'common :refer (clojure.core/list 'getx 'rand-uuid 'require-keys)]
          ['datomic.cache :as 'cache]
          ['datomic.monitor :as 'monitor]
          ['datomic.process.events :as 'events]
          ['datomic.slf4j :as 'logger]
          ['datomic.config :as 'config]
          ['datomic.iter :as 'iter]
          ['datomic.memory-size :as 'size]
          ['datomic.db :as 'db]
          ['datomic.io
           :as
           'io
           :refer
           (clojure.core/list 'bytestream->buf 'position 'remaining 'sub-buffer)]
          ['datomic.excise :as 'x]
          ['datomic.error :as 'error])
        (clojure.core/import 'java.io.ByteArrayOutputStream)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.Collections)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'org.fressian.StreamingWriter)
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'org.fressian.handlers.WriteHandler)
        (clojure.core/import 'org.fressian.impl.Codes)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'org.fressian.impl.ByteBufferInputStream)
        (clojure.core/import 'org.fressian.impl.BytesOutputStream)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.db.IDbImpl))))
  (set! *warn-on-reflection* true)
  (def btree-search (deref #'index/btree-search))
  (def binary-search (deref #'index/binary-search))
  (def segment-threshold 54000)
  (def dir-threshold 1000)
  (defn last-by-nth ([coll] (let [c (count coll)] (when-not (= c 0) (nth coll (int (dec c)))))))
  (reset-meta!
    #'last-by-nth
    (assoc
      {:private true, :arglists (clojure.core/list ['coll]), :column 1}
      :name
      'last-by-nth
      :ns
      *ns*))
  (defonce LogSeek {})
  (defprotocol LogSeek (seek-tx-impl [log t]) (seek-seg-path [log t]))
  (defn seek-seg-id
    ([log t]
      (let [vec__17120 (seek-seg-path log t)
            dirid (nth vec__17120 (int 0) nil)
            segid (nth vec__17120 (int 1) nil)]
        segid)))
  (defonce Log {})
  (defprotocol
    Log
    (claim [log cs])
    (get-root-val [log])
    (get-root-id [log])
    (adopt-root [log cs root-id t])
    (append [log cs msgs])
    (val-keys [log]))
  (defonce TailTxes {})
  (defprotocol TailTxes (tail-txes [_]))
  (defonce LogKey {})
  (defprotocol LogKey (log-key [_]))
  (defonce LogSegSeq {})
  (defprotocol LogSegSeq (log-seg-seq [_]))
  (defonce LogDirSeq {})
  (defprotocol LogDirSeq (log-dir-seq [_]))
  (defn fressianed-txes-length
    ([fressianed_txes]
      (apply
        +
        (map
          (fn fn__17281 ([tx] (java.lang.Integer/valueOf (int (.length (:fressianed-tx tx))))))
          fressianed_txes))))
  (extend java.util.Map LogKey {:log-key (fn fn__17284 ([_] (.get ^java.util.Map _ :t)))})
  (extend java.lang.Long LogKey {:log-key (fn fn__17286 ([_] _))})
  (declare ->LogDir)
  (declare map->LogDir)
  (defrecord LogDir [^long t ^UUID uuid] datomic.log.LogKey (log-key [this] (long t)))
  (clojure.core/import 'datomic.log.LogDir)
  (defn ->LogDir ([t uuid] (datomic.log.LogDir. (long ^java.lang.Number t) uuid)))
  (defn map->LogDir
    ([m__7972__auto__]
      (LogDir/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (def write-handlers
   (merge
     (tx/write-handlers true)
     {datomic.log.LogDir
      {"log-dir"
       (reify
         org.fressian.handlers.WriteHandler
         (^void write
           [this ^org.fressian.Writer w o]
           (do
             (let [entry o]
               (.writeTag ^org.fressian.Writer w "log-dir" (int 2))
               (.writeInt ^org.fressian.Writer w (:t entry))
               (.writeObject ^org.fressian.Writer w (:uuid entry)))
             nil)))}}))
  (def read-handlers
   (merge
     tx/read-handlers
     {"log-dir"
      (reify
        org.fressian.handlers.ReadHandler
        (read
          [this ^org.fressian.Reader rdr tag ^int component_count]
          (datomic.log.LogDir.
            (long (.readInt ^org.fressian.Reader rdr))
            (.readObject ^org.fressian.Reader rdr))))}))
  (defn fressianed-tx
    ([tx]
      (let [baos (org.fressian.impl.BytesOutputStream.)
            fressian_out (tx/writer baos (> (count (common/getx tx :data)) 1))]
        (.resetCaches ^org.fressian.Writer fressian_out)
        (.writeObject ^org.fressian.Writer fressian_out (common/require-keys tx [:id :t :data]))
        (io/bytestream->buf baos))))
  (defn create-entry
    ([t uuid] (datomic.log.LogDir. (long ^java.lang.Number t) uuid))
    ([t] (create-entry t (common/rand-uuid))))
  (defn tail-pod-key
    ([cs]
      (let [temp__5804__auto__ (cluster/dbId cs)]
        (when temp__5804__auto__ (let [dbid temp__5804__auto__] (str "pod-log-tail/" dbid))))))
  (defn tail-descriptor?
    ([desc]
      (and
        (integer? (:rev desc))
        (integer? (:d/l desc))
        (or (string? (:etag desc)) (nil? (:etag desc)))
        (string? (:d/r desc)))))
  (reset-meta!
    #'tail-descriptor?
    (assoc
      {:private true, :arglists (clojure.core/list ['desc]), :column 1}
      :name
      'tail-descriptor?
      :ns
      *ns*))
  (defn inc-rev ([m] (update-in m [:rev] inc)))
  (defn legacy-root-ref-key
    ([cs]
      (let [temp__5804__auto__ (cluster/dbId cs)]
        (when temp__5804__auto__
          (let [dbid temp__5804__auto__] (str "ref-log-root/" (cluster/dbId cs)))))))
  (def LOG_VERSION 3)
  (reset-meta! #'LOG_VERSION (assoc {:const true, :column 1} :name 'LOG_VERSION :ns *ns*))
  (defn normalize-desc
    ([desc cs]
      (let [rev (long (or (:d/l desc) 1)) G__17328 rev]
        (case
          G__17328
          1
          (assoc desc :d/l 1 :d/r (:key (deref (cluster/get-ref cs (legacy-root-ref-key cs)))))
          (2 3)
          desc
          (error/state
            :db.error/log-version
            (str "This version of Datomic cannot read log version " (long rev))
            #:d{:l (long rev)})))))
  (defn read-tail-descriptor
    ([cs]
      (let [temp__5804__auto__ (deref (cluster/get-pod cs (tail-pod-key cs)))]
        (when temp__5804__auto__
          (let [desc temp__5804__auto__
                desc (normalize-desc desc cs)
                buf (:buf desc)
                desc (dissoc desc :buf)]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                (.debug
                  ^org.slf4j.Logger logger
                  (logger/process {:event :log/read-tail, :tail-desc desc})))
              nil)
            (when-not (tail-descriptor? desc)
              (error/raise
                :db.error/unable-to-read-log-tail
                (str "Unable to read log tail " desc)
                desc))
            [desc buf])))))
  (defn pod-update-succeeded? ([response] (every? response [:rev :etag])))
  (defn write-tail-descriptor
    ([cs desc buf]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process {:event :log/write-tail, :tail-desc desc})))
        nil)
      (when-not (tail-descriptor? desc)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'tail-descriptor? 'desc))))))
      (let [pod_meta #:d{:r (:d/r desc), :v (config/property "datomic.versionUnique"), :l 3}
            result (deref
                     (cluster/update-pod
                       cs
                       (tail-pod-key cs)
                       (:rev desc)
                       (:etag desc)
                       buf
                       pod_meta))]
        (when (pod-update-succeeded? result) (merge pod_meta (select-keys result [:rev :etag]))))))
  (defn claim-log
    ([cs desc]
      (let [temp__5804__auto__ (write-tail-descriptor cs (inc-rev desc) nil)]
        (when temp__5804__auto__
          (let [new_desc temp__5804__auto__]
            (when (= 1 (:d/l desc)) (deref (cluster/delete-reference cs (legacy-root-ref-key cs))))
            new_desc)))))
  (defn convert-log-version
    ([cs to_version]
      (error/raise
        :db.error/log-conversion
        "This version of Datomic cannot convert log versions")))
  (defn zip-and-create
    ([cs uuid buf] (cluster/create-val cs 2 (cluster/uuid->val-key uuid) (io/gzip-buffer buf))))
  (defn write-excise-val
    ([cs uuid fressianed_buf]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process {:event :log/write-excise-val, :id uuid})))
        nil)
      (when-not (= :created (deref (zip-and-create cs uuid fressianed_buf)))
        (throw (java.lang.Error. "Cluster value creation failed. Unable to write log."))
        nil)))
  (deftype
    TailTxIter
    [txes ^{:tag long, :unsynchronized-mutable true} idx]
    datomic.log.LogDirSeq
    datomic.iter.Iter
    datomic.log.LogSegSeq
    (next [this] (when (< (inc idx) (count txes)) (set! idx (long (inc idx))) this))
    (get [this] (nth txes (int idx)))
    (log-dir-seq [this] nil)
    (log-seg-seq [this] nil))
  (clojure.core/import 'datomic.log.TailTxIter)
  (defn ->TailTxIter ([txes idx] (datomic.log.TailTxIter. txes (long ^java.lang.Number idx))))
  (def BEGIN_OPEN_LIST
   (ByteBuffer/wrap
     (byte-array [(java.lang.Byte/valueOf (unchecked-byte Codes/BEGIN_OPEN_LIST))])))
  (def BEGIN_CLOSED_LIST
   (ByteBuffer/wrap
     (byte-array [(java.lang.Byte/valueOf (unchecked-byte Codes/BEGIN_CLOSED_LIST))])))
  (def END_COLLECTION
   (ByteBuffer/wrap (byte-array [(java.lang.Byte/valueOf (unchecked-byte Codes/END_COLLECTION))])))
  (declare create-tail)
  (declare empty-tail)
  (declare ->Tail)
  (declare map->Tail)
  (defrecord
    Tail
    [txes bufs]
    datomic.log.LogSeek
    datomic.log.TailTxes
    (tail-txes [this] txes)
    (seek-tx-impl
      [this k]
      (let [comp (common/key-comparator log-key) idx (binary-search txes k comp)]
        (when idx (datomic.log.TailTxIter. txes (long ^java.lang.Number idx))))))
  (clojure.core/import 'datomic.log.Tail)
  (defn ->Tail ([txes bufs] (datomic.log.Tail. txes bufs)))
  (defn map->Tail
    ([m__7972__auto__]
      (Tail/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn tail-byte-count
    ([p__17369]
      (let [map__17370 p__17369
            map__17370 (if (seq? map__17370)
                         (if (next map__17370)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17370))
                           (if (seq map__17370) (first map__17370) {}))
                         map__17370)
            bufs (get map__17370 :bufs)]
        (apply + (map io/remaining bufs)))))
  (defn tail-ts
    ([p__17372]
      (let [map__17373 p__17372
            map__17373 (if (seq? map__17373)
                         (if (next map__17373)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17373))
                           (if (seq map__17373) (first map__17373) {}))
                         map__17373)
            txes (get map__17373 :txes)]
        (map :t txes))))
  (defn tail-empty?
    ([p__17375]
      (let [map__17376 p__17375
            map__17376 (if (seq? map__17376)
                         (if (next map__17376)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17376))
                           (if (seq map__17376) (first map__17376) {}))
                         map__17376)
            txes (get map__17376 :txes)]
        (empty? txes))))
  (defn extend-tail
    ([p__17378 new_txes new_bufs]
      (let [map__17379 p__17378
            map__17379 (if (seq? map__17379)
                         (if (next map__17379)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17379))
                           (if (seq map__17379) (first map__17379) {}))
                         map__17379)
            txes (get map__17379 :txes)
            bufs (get map__17379 :bufs)]
        (create-tail (into txes new_txes) (into bufs new_bufs)))))
  (defn since
    ([p__17381 since_t]
      (let [map__17382 p__17381
            map__17382 (if (seq? map__17382)
                         (if (next map__17382)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17382))
                           (if (seq map__17382) (first map__17382) {}))
                         map__17382)
            tail map__17382
            txes (get map__17382 :txes)
            bufs (get map__17382 :bufs)]
        (if (or (not since_t) (tail-empty? tail) (< since_t (first (tail-ts tail))))
          tail
          (let [new_txes (loop [txes txes]
                           (let [vec__17384 txes
                                 seq__17385 (seq vec__17384)
                                 first__17386 (first seq__17385)
                                 seq__17385 (next seq__17385)
                                 tx first__17386
                                 more seq__17385]
                             (if tx (if (< since_t (:t tx)) (into [] txes) (recur more)) [])))
                bufs_ct (count bufs)
                new_bufs (into [] (drop (long (- bufs_ct (count new_txes)))) bufs)]
            (create-tail new_txes new_bufs))))))
  (defn resets-caches?
    ([bbuf]
      (=
        (long (java.lang.Byte/valueOf (unchecked-byte Codes/RESET_CACHES)))
        (long
          (java.lang.Byte/valueOf
            (byte (.get ^java.nio.ByteBuffer bbuf (int (.position ^java.nio.Buffer bbuf)))))))))
  (defn create-tail
    ([txes bufs]
      (when-not (= (count txes) (count bufs))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list
                  '=
                  (clojure.core/list 'count 'txes)
                  (clojure.core/list 'count 'bufs)))))))
      (when-not (every? resets-caches? bufs)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'every? 'resets-caches? 'bufs))))))
      (->Tail txes bufs)))
  (defn empty-tail ([] (create-tail [] [])))
  (defn load-tail
    ([bbuf]
      (let [code (.get ^java.nio.ByteBuffer bbuf)
            _ (when-not (=
                          (long (java.lang.Byte/valueOf (unchecked-byte Codes/BEGIN_OPEN_LIST)))
                          (long (java.lang.Byte/valueOf (byte code))))
                (throw
                  (java.lang.AssertionError.
                    (str
                      "Assert failed: "
                      (pr-str
                        (clojure.core/list
                          '=
                          (clojure.core/list 'unchecked-byte 'Codes/BEGIN_OPEN_LIST)
                          'code)))))
                nil)
            bbis (org.fressian.impl.ByteBufferInputStream. ^java.nio.ByteBuffer bbuf)
            fin (fressian/create-reader bbis read-handlers false)]
        (loop [txes []
               bufs []
               pos (io/position bbuf)
               remaining (.available ^org.fressian.impl.ByteBufferInputStream bbis)]
          (if (= remaining 0)
            (create-tail txes bufs)
            (let [tx (.readObject ^org.fressian.Reader fin)
                  size (- remaining (.available ^org.fressian.impl.ByteBufferInputStream bbis))]
              (recur
                (conj txes tx)
                (conj bufs (io/sub-buffer bbuf (long pos) (long size)))
                (+ pos size)
                (- remaining size))))))))
  (defn fressianed-dir
    ([val]
      (when (seq val)
        (when-not (instance? datomic.log.LogDir (first val))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list 'instance? 'LogDir (clojure.core/list 'first 'val))))))))
      (fressian/byte-buf val :handlers write-handlers :footer true)))
  (defn fressianed-leaf
    ([val]
      (when-not (vector? val)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'vector? 'val))))))
      (when-not (not (zero? (count val)))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list
                  'not
                  (clojure.core/list 'zero? (clojure.core/list 'count 'val))))))))
      (when-not (:id (first val))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str (clojure.core/list :id (clojure.core/list 'first 'val)))))))
      (when-not (:data (first val))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str (clojure.core/list :data (clojure.core/list 'first 'val)))))))
      (fressian/byte-buf val :handlers write-handlers :footer true)))
  (deftype
    LogTxIter
    [lookup
     root_val
     tail
     ^{:tag long, :unsynchronized-mutable true} ridx
     ^{:unsynchronized-mutable true} dir
     ^{:tag long, :unsynchronized-mutable true} didx
     ^{:unsynchronized-mutable true} seg
     ^{:tag long, :unsynchronized-mutable true} sidx]
    datomic.log.LogDirSeq
    datomic.iter.Iter
    datomic.log.LogSegSeq
    (next
      [this]
      (if (< (inc sidx) (count seg))
        (do (set! sidx (long (inc sidx))) this)
        (if (< (inc didx) (count dir))
          (do
            (set! didx (long (inc didx)))
            (when (= (rem didx 2) 0)
              (loop [dir dir i 0 didx (+ 4 didx)]
                (when (and (< i 2) (< didx (count dir)))
                  (let [k (.-uuid (nth dir (int didx)))] (cache/read-ahead lookup k))
                  (recur dir (inc i) (inc didx)))))
            (set! seg (common/getx lookup (.-uuid (nth dir (int didx)))))
            (set! sidx (long 0))
            this)
          (if (< (inc ridx) (count root_val))
            (do
              (set! ridx (long (inc ridx)))
              (set! dir (common/getx lookup (.-uuid (nth root_val (int ridx)))))
              (set! didx (long 0))
              (set! seg (common/getx lookup (.-uuid (nth dir (int didx)))))
              (set! sidx (long 0))
              this)
            (seek-tx-impl tail (inc (common/getx (.get this) :t)))))))
    (get [this] (nth seg (int sidx)))
    (log-seg-seq
      [this]
      (let [iter__6373__auto__ (fn iter__17411
                                 ([s__17412]
                                   (lazy-seq
                                     (loop [s__17412 s__17412]
                                       (let [temp__5804__auto__ (seq s__17412)]
                                         (when temp__5804__auto__
                                           (let [xs__6360__auto__ temp__5804__auto__
                                                 ri (first xs__6360__auto__)
                                                 dir (common/getx
                                                       lookup
                                                       (.-uuid
                                                         (nth
                                                           root_val
                                                           (int ^java.lang.Number ri))))
                                                 iterys__6369__auto__ (fn 
                                                                        iter__17413
                                                                        ([s__17414]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__17414 s__17414
                                                                               temp__5804__auto__
                                                                               (seq s__17414)]
                                                                              (when
                                                                                temp__5804__auto__
                                                                                (let 
                                                                                  [s__17414
                                                                                   temp__5804__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__17414)
                                                                                    (let 
                                                                                      [c__6371__auto__
                                                                                       (chunk-first
                                                                                         s__17414)
                                                                                       size__6372__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6371__auto__))
                                                                                       b__17416
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6372__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__17415
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__17415
                                                                                              size__6372__auto__)
                                                                                            (let 
                                                                                              [di
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6371__auto__
                                                                                                 (int
                                                                                                   i__17415))]
                                                                                              (chunk-append
                                                                                                b__17416
                                                                                                (common/getx
                                                                                                  lookup
                                                                                                  (.-uuid
                                                                                                    (nth
                                                                                                      dir
                                                                                                      (int
                                                                                                        ^java.lang.Number di)))))
                                                                                              (recur
                                                                                                (inc
                                                                                                  i__17415)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__17416)
                                                                                          (^clojure.lang.IFn iter__17413
                                                                                            (chunk-rest
                                                                                              s__17414)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__17416)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [di
                                                                                       (first
                                                                                         s__17414)]
                                                                                      (cons
                                                                                        (common/getx
                                                                                          lookup
                                                                                          (.-uuid
                                                                                            (nth
                                                                                              dir
                                                                                              (int
                                                                                                ^java.lang.Number di))))
                                                                                        (^clojure.lang.IFn iter__17413
                                                                                          (rest
                                                                                            s__17414)))))))))))
                                                 fs__6370__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6369__auto__
                                                                      (range
                                                                        (long didx)
                                                                        (java.lang.Integer/valueOf
                                                                          (int (count dir))))))]
                                             (if fs__6370__auto__
                                               (concat
                                                 fs__6370__auto__
                                                 (^clojure.lang.IFn iter__17411 (rest s__17412)))
                                               (recur (rest s__17412))))))))))]
        (^clojure.lang.IFn iter__6373__auto__
          (range (long ridx) (java.lang.Integer/valueOf (int (count root_val)))))))
    (log-dir-seq
      [this]
      (let [iter__6373__auto__ (fn iter__17398
                                 ([s__17399]
                                   (lazy-seq
                                     (let [s__17399 s__17399 temp__5804__auto__ (seq s__17399)]
                                       (when temp__5804__auto__
                                         (let [s__17399 temp__5804__auto__]
                                           (if (chunked-seq? s__17399)
                                             (let [c__6371__auto__ (chunk-first s__17399)
                                                   size__6372__auto__ (int (count c__6371__auto__))
                                                   b__17401 (chunk-buffer
                                                              (java.lang.Integer/valueOf
                                                                (int size__6372__auto__)))]
                                               (if (loop [i__17400 (int 0)]
                                                     (if (< i__17400 size__6372__auto__)
                                                       (let [ri
                                                             (.nth
                                                               ^clojure.lang.Indexed c__6371__auto__
                                                               (int i__17400))]
                                                         (chunk-append
                                                           b__17401
                                                           (common/getx
                                                             lookup
                                                             (:uuid
                                                               (nth
                                                                 root_val
                                                                 (int ^java.lang.Number ri)))))
                                                         (recur (inc i__17400)))
                                                       true))
                                                 (chunk-cons
                                                   (chunk b__17401)
                                                   (^clojure.lang.IFn iter__17398
                                                     (chunk-rest s__17399)))
                                                 (chunk-cons (chunk b__17401) nil)))
                                             (let [ri (first s__17399)]
                                               (cons
                                                 (common/getx
                                                   lookup
                                                   (:uuid
                                                     (nth root_val (int ^java.lang.Number ri))))
                                                 (^clojure.lang.IFn iter__17398
                                                   (rest s__17399)))))))))))]
        (^clojure.lang.IFn iter__6373__auto__
          (range (long ridx) (java.lang.Integer/valueOf (int (count root_val))))))))
  (clojure.core/import 'datomic.log.LogTxIter)
  (defn ->LogTxIter
    ([lookup root_val tail ridx dir didx seg sidx]
      (datomic.log.LogTxIter.
        lookup
        root_val
        tail
        (long ^java.lang.Number ridx)
        dir
        (long ^java.lang.Number didx)
        seg
        (long ^java.lang.Number sidx))))
  (declare ->LogImpl)
  (declare map->LogImpl)
  (defrecord
    LogImpl
    [olookup desc tail]
    datomic.log.Log
    datomic.log.LogSeek
    datomic.log.TailTxes
    (adopt-root
      [this cs new_root_id basis_t]
      (let [tail (since tail basis_t)
            proposed_desc (assoc (inc-rev desc) :etag nil :d/r (cluster/uuid->val-key new_root_id))
            pod_buf (io/unchunk (cons BEGIN_OPEN_LIST (:bufs tail)))
            temp__5802__auto__ (write-tail-descriptor cs proposed_desc pod_buf)]
        (if temp__5802__auto__
          (let [new_desc temp__5802__auto__] (assoc this :desc new_desc :tail tail))
          (do (throw (java.lang.Error. "Conflict adopting log root.")) nil))))
    (append
      [this cs msgs]
      (let [bufs (map :fressianed-tx msgs)
            ids (map :id msgs)
            txes (map :tx msgs)
            new_tail (extend-tail tail txes bufs)
            log (let [m_17459 {:event :log/add-next, :txids ids, :firstT (:t (first txes))}
                      ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.log")]
                                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                          (.debug
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_17459 :phase :begin))))
                                        nil)
                      start__8584__auto__ (java.lang.System/nanoTime)
                      result__8585__auto__ (try
                                             {:returned
                                              (let [temp__5802__auto__ (write-tail-descriptor
                                                                         cs
                                                                         (update-in
                                                                           desc
                                                                           [:rev]
                                                                           inc)
                                                                         (io/unchunk bufs))]
                                                (if temp__5802__auto__
                                                  (let [new_desc temp__5802__auto__]
                                                    (assoc this :tail new_tail :desc new_desc))
                                                  (do
                                                    (throw
                                                      (java.lang.Error.
                                                        "Conflict updating log tail"))
                                                    1)))}
                                             (catch
                                               java.lang.Throwable
                                               t__8586__auto__
                                               {:threw t__8586__auto__}))
                      elapsed_17460 (- (java.lang.System/nanoTime) start__8584__auto__)
                      msec_17461 (logger/format-as-msec (long elapsed_17460))]
                  (monitor/add-stat :LogWriteMsec msec_17461)
                  (let [endmsg__8587__auto__ (merge
                                               (assoc m_17459 :msec msec_17461 :phase :end)
                                               (when (:threw result__8585__auto__)
                                                 {:threw (class (:threw result__8585__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                      (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                    nil)
                  (if (contains? result__8585__auto__ :returned)
                    (:returned result__8585__auto__)
                    (do (throw (:threw result__8585__auto__)) nil)))]
        (tx/log-completion! ids)
        log))
    (val-keys
      [this]
      (tree-seq
        (fn fn__17453 ([k] (instance? datomic.log.LogDir (first (get olookup k)))))
        (fn fn__17455
          ([k] (map (fn fn__17456 ([ld] (cluster/uuid->val-key (:uuid ld)))) (get olookup k))))
        (cluster/uuid->val-key (get-root-id this))))
    (get-root-val [this] (vec (common/getx olookup (get-root-id this))))
    (get-root-id [this] (:d/r desc))
    (claim
      [this cs]
      (let [temp__5804__auto__ (claim-log cs desc)]
        (when temp__5804__auto__ (let [new_desc temp__5804__auto__] (assoc this :desc new_desc)))))
    (seek-seg-path
      [this t]
      (let [comp (common/key-comparator log-key)
            root_val (get-root-val this)
            ridx (btree-search root_val t comp)
            dirid (.-uuid (nth root_val (int ^java.lang.Number ridx)))
            dir (common/getx olookup dirid)]
        (when-not (= (count dir) 0)
          (let [didx (btree-search dir t comp)
                segid (.-uuid (nth dir (int ^java.lang.Number didx)))]
            [dirid segid]))))
    (seek-tx-impl
      [this t]
      (let [comp (common/key-comparator log-key)
            root_val (get-root-val this)
            ridx (btree-search root_val t comp)
            dir (common/getx olookup (.-uuid (nth root_val (int ^java.lang.Number ridx))))]
        (if (= (count dir) 0)
          (seek-tx-impl tail t)
          (let [didx (btree-search dir t comp)
                seg (common/getx olookup (.-uuid (nth dir (int ^java.lang.Number didx))))
                sidx (binary-search seg t comp)]
            (if sidx
              (datomic.log.LogTxIter.
                olookup
                root_val
                tail
                (long ^java.lang.Number ridx)
                dir
                (long ^java.lang.Number didx)
                seg
                (long ^java.lang.Number sidx))
              (loop [iter (seek-tx-impl this (:t (last seg)))]
                (if (and iter (< (:t (.get ^datomic.iter.Iter iter)) t))
                  (recur (.next ^datomic.iter.Iter iter))
                  iter)))))))
    (tail-txes [this] (tail-txes tail)))
  (clojure.core/import 'datomic.log.LogImpl)
  (defn ->LogImpl ([olookup desc tail] (datomic.log.LogImpl. olookup desc tail)))
  (defn map->LogImpl
    ([m__7972__auto__]
      (LogImpl/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn create-log-impl
    ([olookup desc tail]
      (when-not (tail-descriptor? desc)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'tail-descriptor? 'desc))))))
      (when-not (instance? datomic.log.Tail tail)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'instance? 'Tail 'tail))))))
      (->LogImpl olookup desc tail)))
  (defn write-new-log
    ([cs]
      (let [vec__17488 (repeatedly (fn fn__17491 ([] (common/rand-uuid))))
            new_root_id (nth vec__17488 (int 0) nil)
            new_tail_id (nth vec__17488 (int 1) nil)
            root_val_result (deref
                              (zip-and-create
                                cs
                                new_root_id
                                (fressianed-dir [(create-entry 0 new_tail_id)])))
            tail_dir_result (deref (zip-and-create cs new_tail_id (fressianed-dir [])))
            tail (empty-tail)
            root_id (cluster/uuid->val-key new_root_id)
            proposed_desc {:rev 0, :etag nil, :d/r root_id, :d/l 3}
            temp__5804__auto__ (write-tail-descriptor cs proposed_desc BEGIN_OPEN_LIST)]
        (when temp__5804__auto__ (let [desc temp__5804__auto__] [desc tail])))))
  (defn create-new-log
    ([cs olookup]
      (let [temp__5804__auto__ (write-new-log cs)]
        (when temp__5804__auto__
          (let [vec__17495 temp__5804__auto__
                desc (nth vec__17495 (int 0) nil)
                tail (nth vec__17495 (int 1) nil)]
            (create-log-impl olookup desc tail))))))
  (defn seek-tx ([provider t] (seek-tx-impl provider t)))
  (defn find-log
    ([cs olookup]
      (let [temp__5804__auto__ (read-tail-descriptor cs)]
        (when temp__5804__auto__
          (let [vec__17501 temp__5804__auto__
                desc (nth vec__17501 (int 0) nil)
                buf (nth vec__17501 (int 1) nil)
                tail (load-tail buf)]
            (create-log-impl olookup desc tail))))))
  (defn log-tree
    ([olookup root_id] (create-log-impl olookup {:rev 0, :d/l 3, :d/r root_id} (empty-tail))))
  (defn catchup-tx
    ([p__17507 tx]
      (let [map__17508 p__17507
            map__17508 (if (seq? map__17508)
                         (if (next map__17508)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17508))
                           (if (seq map__17508) (first map__17508) {}))
                         map__17508)
            db (get map__17508 :db)
            size (get map__17508 :size)
            data (common/getx tx :data)
            basis (:nextT db)
            d (first data)]
        (if (and
              (< (.getT ^datomic.impl.db.IDatum d) basis)
              (config/property "datomic.allowLogOverlap"))
          (do
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                (.warn
                  ^org.slf4j.Logger logger
                  (logger/process
                    {:event :log/overlap, :t (long (.getT ^datomic.impl.db.IDatum d))})))
              nil)
            {:db db, :size size})
          {:db (.acceptDataCheck ^datomic.db.IDbImpl db data false),
           :size (+ size (size/memory-size data))}))))
  (reset-meta!
    #'catchup-tx
    (assoc
      {:private true,
       :arglists (clojure.core/list [{:keys [(.withMeta 'db {:tag 'IDbImpl}) 'size]} 'tx]),
       :column 1}
      :name
      'catchup-tx
      :ns
      *ns*))
  (defn catchup
    ([db log catchup_ft]
      (let [start (java.lang.System/currentTimeMillis)
            index_t (.basisT ^datomic.Database db)
            iter (seek-tx log (long (.nextT ^datomic.Database db)))
            _ (when iter
                (future-call
                  (fn fn__17512
                    ([]
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                          (.info
                            ^org.slf4j.Logger logger
                            (logger/process
                              {:event :log/load-segments,
                               :count
                               (java.lang.Integer/valueOf
                                 (int (count (pmap identity (log-seg-seq iter)))))})))
                        nil)))))
            log_txes (iter/iter-seq iter)
            map__17511 (reduce catchup-tx {:db db, :size 0} log_txes)
            map__17511 (if (seq? map__17511)
                         (if (next map__17511)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17511))
                           (if (seq map__17511) (first map__17511) {}))
                         map__17511)
            result map__17511
            db (get map__17511 :db)
            size (get map__17511 :size)
            elapsed (- (java.lang.System/currentTimeMillis) start)
            db (if catchup_ft
                 (let [m_17514 {:event :log/catchup-fulltext}
                       ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.log")]
                                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                           (.info
                                             ^org.slf4j.Logger logger
                                             (logger/process (assoc m_17514 :phase :begin))))
                                         nil)
                       start__8584__auto__ (java.lang.System/nanoTime)
                       result__8585__auto__ (try
                                              {:returned
                                               (db/add-fulltext db (mapcat :data log_txes))}
                                              (catch
                                                java.lang.Throwable
                                                t__8586__auto__
                                                {:threw t__8586__auto__}))
                       elapsed_17515 (- (java.lang.System/nanoTime) start__8584__auto__)
                       msec_17516 (logger/format-as-msec (long elapsed_17515))]
                   (let [endmsg__8587__auto__ (merge
                                                (assoc m_17514 :msec msec_17516 :phase :end)
                                                (when (:threw result__8585__auto__)
                                                  {:threw (class (:threw result__8585__auto__))}))
                         logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                       (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                     nil)
                   (if (contains? result__8585__auto__ :returned)
                     (:returned result__8585__auto__)
                     (do (throw (:threw result__8585__auto__)) nil)))
                 db)]
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info
              ^org.slf4j.Logger logger
              (logger/process
                {:event :log/catchup,
                 :bytes size,
                 :tail-t (long (.basisT ^datomic.Database db)),
                 :index-t (long index_t),
                 :msec (long elapsed)})))
          nil)
        (monitor/add-stat :LogIngestMsec (long elapsed))
        (monitor/add-stat :LogIngestBytes size)
        {:db db, :size size}))
    ([db log] (catchup db log false)))
  (defn ensure-index-and-log
    ([cluster olookup db_id]
      (let [m_17524 {:event :transactor/ensure-index-and-log, :db-id db_id}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_17524 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [existing_root (index/find-index-root-id cluster)]
                                      {:idxroot
                                       (or
                                         existing_root
                                         (index/init-index cluster)
                                         (do
                                           (throw
                                             (java.lang.RuntimeException.
                                               (str "Unable to create index root for " db_id)))
                                           1)),
                                       :log
                                       (or
                                         (if existing_root
                                           (find-log cluster olookup)
                                           (create-new-log cluster olookup))
                                         (do
                                           (throw
                                             (java.lang.RuntimeException.
                                               (str "Unable to read log for db id " db_id)))
                                           3))})}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_17525 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_17526 (logger/format-as-msec (long elapsed_17525))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_17524 :msec msec_17526 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn excise-ts
    ([log xpreds]
      (reduce
        (fn fn__17538
          ([s p]
            (into
              s
              (map
                (fn fn__17539 ([p1__17537#] (long (.getT ^datomic.impl.db.IDatum p1__17537#))))
                (x/datoms p)))))
        #{}
        xpreds)))
  (defn excise-dir-map
    ([log ts]
      (reduce
        (fn fn__17545
          ([m p__17544]
            (let [vec__17546 p__17544
                  dirid (nth vec__17546 (int 0) nil)
                  segid (nth vec__17546 (int 1) nil)]
              (if (contains? (get m dirid) segid)
                m
                (assoc m dirid (conj (get m dirid #{}) segid))))))
        {}
        (map (fn fn__17550 ([p1__17543#] (seek-seg-path log p1__17543#))) ts))))
  (defn write-excised-log
    ([cs lookup xpreds ts dir_map]
      (reduce
        (fn fn__17555
          ([m p__17554]
            (let [vec__17556 p__17554
                  dirid (nth vec__17556 (int 0) nil)
                  segids (nth vec__17556 (int 1) nil)
                  excise? (fn excise_QMARK_
                            ([d]
                              (some
                                (fn fn__17563 ([p1__17553#] (x/remove? p1__17553# d)))
                                xpreds)))
                  dir (common/getx lookup dirid)
                  vec__17559 (reduce
                               (fn fn__17567
                                 ([p__17566 direntry]
                                   (let [vec__17568 p__17566
                                         m (nth vec__17568 (int 0) nil)
                                         newdir (nth vec__17568 (int 1) nil)
                                         t (.-t ^datomic.log.LogDir direntry)
                                         segid (.-uuid ^datomic.log.LogDir direntry)]
                                     (if (contains? segids segid)
                                       (let [seg (common/getx lookup segid)
                                             newsid (common/rand-uuid)
                                             newseg (mapv
                                                      (fn fn__17572
                                                        ([p__17571]
                                                          (let [map__17573 p__17571
                                                                map__17573
                                                                (if
                                                                  (seq? map__17573)
                                                                  (if
                                                                    (next map__17573)
                                                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                      (to-array map__17573))
                                                                    (if
                                                                      (seq map__17573)
                                                                      (first map__17573)
                                                                      {}))
                                                                  map__17573)
                                                                tx map__17573
                                                                data (get map__17573 :data)
                                                                t (.getT (first data))]
                                                            (if
                                                              (contains? ts (long t))
                                                              (assoc
                                                                tx
                                                                :data
                                                                (remove excise? data))
                                                              tx))))
                                                      seg)]
                                         (write-excise-val cs newsid (fressianed-leaf newseg))
                                         [(assoc m segid newsid)
                                          (conj
                                            newdir
                                            (datomic.log.LogDir.
                                              (long (.-t ^datomic.log.LogDir direntry))
                                              newsid))])
                                       [m (conj newdir direntry)]))))
                               [m []]
                               dir)
                  m (nth vec__17559 (int 0) nil)
                  newdir (nth vec__17559 (int 1) nil)
                  newdid (common/rand-uuid)]
              (write-excise-val cs newdid (fressianed-dir newdir))
              (assoc m dirid newdid))))
        {}
        dir_map)))
  (defn excise
    ([cs lookup log xpreds]
      (let [ts (excise-ts log xpreds)
            dir_map (excise-dir-map log ts)
            pario (config/property "datomic.exciseIOParallelism")
            cs (if pario
                 (cluster/queueing-writer
                   cs
                   pario
                   cluster/BOUNDING_TIMEOUT_MSEC
                   (fn fn__17579 ([p1__17578#] (monitor/add-stat :ExciseIOQueueCount p1__17578#))))
                 cs)
            replacements (write-excised-log cs lookup xpreds ts dir_map)]
        (when pario
          (common/bounded-deref (cluster/finish-writer cs) cluster/BOUNDING_TIMEOUT_MSEC))
        {:dir-map dir_map, :replacements replacements})))
  (defn excise-root
    ([cs lookup current_root_id p__17584]
      (let [map__17585 p__17584
            map__17585 (if (seq? map__17585)
                         (if (next map__17585)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17585))
                           (if (seq map__17585) (first map__17585) {}))
                         map__17585)
            dir_map (get map__17585 :dir-map)
            replacements (get map__17585 :replacements)
            log (log-tree lookup current_root_id)
            root (get-root-val log)
            dirs (into
                   #{}
                   (map
                     (fn fn__17586 ([p1__17582#] (.-uuid ^datomic.log.LogDir p1__17582#)))
                     root))
            patch_dir (fn patch_dir
                        ([replacements dir]
                          (reduce
                            (fn fn__17589
                              ([newdir direntry]
                                (let [eid (.-uuid ^datomic.log.LogDir direntry)]
                                  (conj
                                    newdir
                                    (let [temp__5802__auto__ (^clojure.lang.IFn replacements eid)]
                                      (if temp__5802__auto__
                                        (let [neweid temp__5802__auto__]
                                          (datomic.log.LogDir.
                                            (long (.-t ^datomic.log.LogDir direntry))
                                            neweid))
                                        direntry))))))
                            []
                            dir)))
            pario (config/property "datomic.exciseIOParallelism")
            cs (if pario
                 (cluster/queueing-writer
                   cs
                   pario
                   cluster/BOUNDING_TIMEOUT_MSEC
                   (fn fn__17593 ([p1__17583#] (monitor/add-stat :ExciseIOQueueCount p1__17583#))))
                 cs)
            replacements (reduce
                           (fn fn__17595
                             ([m did]
                               (let [segids (^clojure.lang.IFn dir_map did)
                                     newdid (common/rand-uuid)
                                     vec__17596 (seek-seg-path
                                                  log
                                                  (long
                                                    (.getT
                                                      (first
                                                        (:data
                                                          (first
                                                            (common/getx
                                                              lookup
                                                              (^clojure.lang.IFn replacements
                                                                (first segids)))))))))
                                     dirid (nth vec__17596 (int 0) nil)
                                     _ (nth vec__17596 (int 1) nil)
                                     dir (common/getx lookup dirid)
                                     newdid (common/rand-uuid)
                                     newdir (^clojure.lang.IFn patch_dir replacements dir)]
                                 (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                                   (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                     (.debug
                                       ^org.slf4j.Logger logger
                                       (logger/process
                                         {:event :log/excise-replace-rightmost,
                                          :oldid dirid,
                                          :newid newdid})))
                                   nil)
                                 (write-excise-val cs newdid (fressianed-dir newdir))
                                 (assoc m dirid newdid))))
                           replacements
                           (remove dirs (keys dir_map)))
            newroot (^clojure.lang.IFn patch_dir replacements root)
            newrid (common/rand-uuid)]
        (write-excise-val cs newrid (fressianed-dir newroot))
        (when pario
          (common/bounded-deref (cluster/finish-writer cs) cluster/BOUNDING_TIMEOUT_MSEC))
        {:root-id (cluster/uuid->val-key newrid),
         :garbage-ids (map cluster/uuid->val-key (cons (get-root-id log) (keys replacements)))})))
  (declare ->LogTailValue)
  (declare map->LogTailValue)
  (defrecord
    LogTailValue
    [txes]
    datomic.log.LogSeek
    (seek-tx-impl
      [this k]
      (let [comp (common/key-comparator log-key) idx (binary-search txes k comp)]
        (when idx (datomic.log.TailTxIter. txes (long ^java.lang.Number idx))))))
  (clojure.core/import 'datomic.log.LogTailValue)
  (defn ->LogTailValue ([txes] (datomic.log.LogTailValue. txes)))
  (defn map->LogTailValue
    ([m__7972__auto__]
      (LogTailValue/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (extend
    datomic.db.MemLog
    LogSeek
    {:seek-tx-impl
     (fn fn__17622
       ([this k]
         (let [comp (common/key-comparator log-key) idx (binary-search (.-txes this) k comp)]
           (when idx (datomic.log.TailTxIter. (.-txes this) (long ^java.lang.Number idx))))))})
  (defn tx-range
    ([log db start end]
      (let [start (if start (max 1000 (db/t-at-or-since db start)) 1000)
            next_t (.getNextT ^datomic.db.IDb db)
            end (if end (min next_t (db/t-at-or-since db end)) next_t)]
        (reify
          java.lang.Iterable
          (^java.util.Iterator iterator
            [this]
            (iter/iterator
              (let [ret (seek-tx log start)]
                (if end
                  (iter/take-while (fn fn__17626 ([p1__17624#] (< (:t p1__17624#) end))) ret)
                  ret))))))))
  (declare ->LogValue)
  (declare map->LogValue)
  (defrecord
    LogValue
    [db olookup root-id tail]
    datomic.log.LogSeek
    datomic.Log
    (^java.lang.Iterable txRange [this start end] (tx-range this db start end))
    (seek-seg-path
      [this t]
      (let [comp (common/key-comparator log-key)
            root_val (vec (common/getx olookup root-id))
            ridx (btree-search root_val t comp)
            dirid (.-uuid (nth root_val (int ^java.lang.Number ridx)))
            dir (common/getx olookup dirid)]
        (when-not (= (count dir) 0)
          (let [didx (btree-search dir t comp)
                segid (.-uuid (nth dir (int ^java.lang.Number didx)))]
            [dirid segid]))))
    (seek-tx-impl
      [this t]
      (let [comp (common/key-comparator log-key)
            root_val (vec (common/getx olookup root-id))
            ridx (btree-search root_val t comp)
            dir (common/getx olookup (.-uuid (nth root_val (int ^java.lang.Number ridx))))]
        (if (= (count dir) 0)
          (seek-tx-impl tail t)
          (let [didx (btree-search dir t comp)
                seg (common/getx olookup (.-uuid (nth dir (int ^java.lang.Number didx))))
                sidx (binary-search seg t comp)]
            (if sidx
              (datomic.log.LogTxIter.
                olookup
                root_val
                tail
                (long ^java.lang.Number ridx)
                dir
                (long ^java.lang.Number didx)
                seg
                (long ^java.lang.Number sidx))
              (loop [iter (seek-tx-impl this (:t (last seg)))]
                (if (and iter (< (:t (.get ^datomic.iter.Iter iter)) t))
                  (recur (.next ^datomic.iter.Iter iter))
                  iter))))))))
  (clojure.core/import 'datomic.log.LogValue)
  (defn ->LogValue ([db olookup root_id tail] (datomic.log.LogValue. db olookup root_id tail)))
  (defn map->LogValue
    ([m__7972__auto__]
      (LogValue/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn segmented-basis-t
    ([log_value]
      (let [olookup (:olookup log_value)]
        (:t
          (last
            (common/getx
              olookup
              (:uuid
                (last
                  (common/getx
                    olookup
                    (:uuid (last (common/getx olookup (get-root-id log_value)))))))))))))
  (defn create-log-val
    ([cs olookup db]
      (when-not cs (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'cs)))))
      (when-not olookup
        (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'olookup)))))
      (when-not db (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db)))))
      (let [temp__5804__auto__ (deref (cluster/get-pod-meta cs (tail-pod-key cs)))]
        (when temp__5804__auto__
          (let [desc temp__5804__auto__
                desc (normalize-desc desc cs)
                root_id (cluster/val-key->uuid (:d/r desc))]
            (->LogValue db olookup root_id (:memlog db)))))))
  (defn max-t
    ([datoms]
      (reduce (fn fn__17664 ([n datom] (max n (db/eid->eidx (long (:tx datom)))))) 0 datoms)))
  (defn max-eidx
    ([datoms]
      (reduce
        (fn fn__17667
          ([n datom]
            (max (max n (db/eid->eidx (long (:e datom)))) (db/eid->eidx (long (:tx datom))))))
        0
        datoms)))
  (defn last-tree-tx
    ([olookup root_id]
      (let [root (common/getx olookup root_id)
            dir_id (:uuid (last-by-nth root))
            dir (when dir_id (common/getx olookup dir_id))
            leaf_id (:uuid (last-by-nth dir))]
        (last-by-nth (when leaf_id (common/getx olookup leaf_id)))))
    ([p__17670]
      (let [map__17671 p__17670
            map__17671 (if (seq? map__17671)
                         (if (next map__17671)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17671))
                           (if (seq map__17671) (first map__17671) {}))
                         map__17671)
            log map__17671
            olookup (get map__17671 :olookup)]
        (last-tree-tx olookup (or (:root-id log) (get-root-id log))))))
  (defn root-id ([cs] (:d/r (deref (cluster/get-pod-meta cs (tail-pod-key cs))))))
  (defn partition-by-weight
    ([weigh target]
      (fn fn__17675
        ([rf]
          (let [weight_ref (volatile! 0) part_ref (volatile! [])]
            (fn fn__17676
              ([] (^clojure.lang.IFn rf))
              ([result]
                (let [temp__5802__auto__ (seq (deref part_ref))]
                  (if temp__5802__auto__
                    (let [part temp__5802__auto__]
                      (^clojure.lang.IFn rf (^clojure.lang.IFn rf result part)))
                    (^clojure.lang.IFn rf result))))
              ([result input]
                (let [weight (vswap!
                               ^clojure.lang.Volatile weight_ref
                               +
                               (^clojure.lang.IFn weigh input))
                      part (vswap! ^clojure.lang.Volatile part_ref conj input)]
                  (if (< weight target)
                    result
                    (do
                      (vreset! weight_ref 0)
                      (vreset! part_ref [])
                      (^clojure.lang.IFn rf result part)))))))))))
  (defn combine-last-if
    ([pred combine]
      (fn fn__17681
        ([rf]
          (let [tail_ref (volatile! nil)]
            (fn fn__17682
              ([] (^clojure.lang.IFn rf))
              ([result]
                (let [vec__17683 (deref tail_ref)
                      p1 (nth vec__17683 (int 0) nil)
                      p2 (nth vec__17683 (int 1) nil)]
                  (if p1
                    (if (^clojure.lang.IFn pred p2)
                      (^clojure.lang.IFn rf
                        (^clojure.lang.IFn rf result (^clojure.lang.IFn combine p1 p2)))
                      (^clojure.lang.IFn rf
                        (^clojure.lang.IFn rf (^clojure.lang.IFn rf result p1) p2)))
                    (cond-> result p2 (^clojure.lang.IFn rf p2) :finish (^clojure.lang.IFn rf)))))
              ([result input]
                (let [vec__17687 (deref tail_ref)
                      p1 (nth vec__17687 (int 0) nil)
                      p2 (nth vec__17687 (int 1) nil)]
                  (vreset! tail_ref [p2 input])
                  (if p1 (^clojure.lang.IFn rf result p1) result)))))))))
  (defn create-leaves*
    ([target_size ftxes]
      (let [weigh (comp io/remaining second)]
        (sequence
          (comp
            (partition-by-weight weigh target_size)
            (combine-last-if
              (fn fn__17693 ([ftxes] (< (apply + (map weigh ftxes)) (quot target_size 2))))
              into)
            (map
              (fn fn__17695
                ([ftxes]
                  [(ffirst ftxes)
                   (io/unchunk
                     (concat [BEGIN_CLOSED_LIST] (map second ftxes) [END_COLLECTION]))]))))
          ftxes))))
  (defn create-leaves
    ([target_size tail]
      (when-not (every? resets-caches? (:bufs tail))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list 'every? 'resets-caches? (clojure.core/list :bufs 'tail)))))))
      (create-leaves* target_size (map vector (tail-ts tail) (:bufs tail)))))
  (defn extend-tree
    ([cs olookup root_id target_dir_count leaf_segs]
      (let [leaf_ts (map first leaf_segs)
            leaf_bufs (map second leaf_segs)
            new_seg_t (ffirst leaf_segs)
            root (vec (common/getx olookup root_id))
            old_tail_dir_uuid (common/getx (peek root) :uuid)
            tail_dir (vec (common/getx olookup old_tail_dir_uuid))
            new_tail_dir? (>= (count tail_dir) target_dir_count)
            leaf_ids (repeatedly
                       (java.lang.Integer/valueOf (int (count leaf_bufs)))
                       common/rand-uuid)
            vec__17700 (repeatedly common/rand-uuid)
            new_root_id (nth vec__17700 (int 0) nil)
            new_dir_id (nth vec__17700 (int 1) nil)
            new_tail_dir (into (if new_tail_dir? [] tail_dir) (map create-entry leaf_ts leaf_ids))
            new_root_entry (if new_tail_dir?
                             (create-entry new_seg_t new_dir_id)
                             (create-entry (common/getx (peek root) :t) new_dir_id))
            new_root (if new_tail_dir?
                       (conj root new_root_entry)
                       (assoc root (long (dec (count root))) new_root_entry))
            garbage_ids (cond->
                          [root_id]
                          (not new_tail_dir?)
                          (conj (cluster/uuid->val-key old_tail_dir_uuid)))
            create (fn create ([uuid val] (zip-and-create cs uuid val)))
            vals (conj (vec leaf_bufs) (fressianed-dir new_tail_dir) (fressianed-dir new_root))
            ids (conj (vec leaf_ids) new_dir_id new_root_id)
            results (mapv create ids vals)]
        (if (every? (fn fn__17706 ([p1__17699#] (= p1__17699# :created))) (map deref results))
          {:root-id (cluster/uuid->val-key new_root_id),
           :dir-id (cluster/uuid->val-key new_dir_id),
           :leaf-ts leaf_ts,
           :garbage-ids garbage_ids}
          (do (throw (java.lang.Error. "Write failure extending log tree")) nil)))))
  (defn segment
    ([cluster olookup]
      (let [map__17709 (find-log cluster olookup)
            map__17709 (if (seq? map__17709)
                         (if (next map__17709)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__17709))
                           (if (seq map__17709) (first map__17709) {}))
                         map__17709)
            log map__17709
            desc (get map__17709 :desc)
            tail (get map__17709 :tail)]
        (if (tail-empty? tail)
          log
          (let [map__17710 (extend-tree
                             cluster
                             olookup
                             (:d/r desc)
                             dir-threshold
                             (create-leaves segment-threshold tail))
                map__17710 (if (seq? map__17710)
                             (if (next map__17710)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__17710))
                               (if (seq map__17710) (first map__17710) {}))
                             map__17710)
                root_id (get map__17710 :root-id)]
            (adopt-root log cluster root_id (last (map :t (:txes tail))))))))))
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
      (let [vec__15974 (seek-seg-path log t)
            dirid (nth vec__15974 (int 0) nil)
            segid (nth vec__15974 (int 1) nil)]
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
          (fn fn__16135 ([tx] (java.lang.Integer/valueOf (int (.length (:fressianed-tx tx))))))
          fressianed_txes))))
  (extend java.util.Map LogKey {:log-key (fn fn__16138 ([_] (.get ^java.util.Map _ :t)))})
  (extend java.lang.Long LogKey {:log-key (fn fn__16140 ([_] _))})
  (declare ->LogDir)
  (declare map->LogDir)
  (defrecord LogDir [^long t ^UUID uuid] datomic.log.LogKey (log-key [this] (long t)))
  (clojure.core/import 'datomic.log.LogDir)
  (defn ->LogDir ([t uuid] (datomic.log.LogDir. (long ^java.lang.Number t) uuid)))
  (defn map->LogDir
    ([m__7585__auto__]
      (LogDir/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
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
      (let [temp__5457__auto__ (cluster/dbId cs)]
        (when temp__5457__auto__ (let [dbid temp__5457__auto__] (str "pod-log-tail/" dbid))))))
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
      (let [temp__5457__auto__ (cluster/dbId cs)]
        (when temp__5457__auto__
          (let [dbid temp__5457__auto__] (str "ref-log-root/" (cluster/dbId cs)))))))
  (def LOG_VERSION 3)
  (reset-meta! #'LOG_VERSION (assoc {:const true, :column 1} :name 'LOG_VERSION :ns *ns*))
  (defn normalize-desc
    ([desc cs]
      (let [rev (long (or (:d/l desc) 1)) G__16182 rev]
        (case
          G__16182
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
      (let [temp__5457__auto__ (deref (cluster/get-pod cs (tail-pod-key cs)))]
        (when temp__5457__auto__
          (let [desc temp__5457__auto__
                desc (normalize-desc desc cs)
                buf (:buf desc)
                desc (dissoc desc :buf)]
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                (.debug
                  ^org.slf4j.Logger logger
                  (logger/process {:event :log/read-tail, :tail-desc desc}))
                nil)
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
            (logger/process {:event :log/write-tail, :tail-desc desc}))
          nil)
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
      (let [temp__5457__auto__ (write-tail-descriptor cs (inc-rev desc) nil)]
        (when temp__5457__auto__
          (let [new_desc temp__5457__auto__]
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
            (logger/process {:event :log/write-excise-val, :id uuid}))
          nil)
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
    ([m__7585__auto__]
      (Tail/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn tail-byte-count
    ([p__16223]
      (let [map__16224 p__16223
            map__16224 (if (seq? map__16224)
                         (clojure.lang.PersistentHashMap/create (seq map__16224))
                         map__16224)
            bufs (get map__16224 :bufs)]
        (apply + (map io/remaining bufs)))))
  (defn tail-ts
    ([p__16226]
      (let [map__16227 p__16226
            map__16227 (if (seq? map__16227)
                         (clojure.lang.PersistentHashMap/create (seq map__16227))
                         map__16227)
            txes (get map__16227 :txes)]
        (map :t txes))))
  (defn tail-empty?
    ([p__16229]
      (let [map__16230 p__16229
            map__16230 (if (seq? map__16230)
                         (clojure.lang.PersistentHashMap/create (seq map__16230))
                         map__16230)
            txes (get map__16230 :txes)]
        (empty? txes))))
  (defn extend-tail
    ([p__16232 new_txes new_bufs]
      (let [map__16233 p__16232
            map__16233 (if (seq? map__16233)
                         (clojure.lang.PersistentHashMap/create (seq map__16233))
                         map__16233)
            txes (get map__16233 :txes)
            bufs (get map__16233 :bufs)]
        (create-tail (into txes new_txes) (into bufs new_bufs)))))
  (defn since
    ([p__16235 since_t]
      (let [map__16236 p__16235
            map__16236 (if (seq? map__16236)
                         (clojure.lang.PersistentHashMap/create (seq map__16236))
                         map__16236)
            tail map__16236
            txes (get map__16236 :txes)
            bufs (get map__16236 :bufs)]
        (if (or (not since_t) (tail-empty? tail) (< since_t (first (tail-ts tail))))
          tail
          (let [new_txes (loop [txes txes]
                           (let [vec__16238 txes
                                 seq__16239 (seq vec__16238)
                                 first__16240 (first seq__16239)
                                 seq__16239 (next seq__16239)
                                 tx first__16240
                                 more seq__16239]
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
      (let [iter__6025__auto__ (fn iter__16265
                                 ([s__16266]
                                   (lazy-seq
                                     (loop [s__16266 s__16266]
                                       (let [temp__5457__auto__ (seq s__16266)]
                                         (when temp__5457__auto__
                                           (let [xs__6012__auto__ temp__5457__auto__
                                                 ri (first xs__6012__auto__)
                                                 dir (common/getx
                                                       lookup
                                                       (.-uuid
                                                         (nth
                                                           root_val
                                                           (int ^java.lang.Number ri))))
                                                 iterys__6021__auto__ (fn 
                                                                        iter__16267
                                                                        ([s__16268]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__16268 s__16268
                                                                               temp__5457__auto__
                                                                               (seq s__16268)]
                                                                              (when
                                                                                temp__5457__auto__
                                                                                (let 
                                                                                  [s__16268
                                                                                   temp__5457__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__16268)
                                                                                    (let 
                                                                                      [c__6023__auto__
                                                                                       (chunk-first
                                                                                         s__16268)
                                                                                       size__6024__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6023__auto__))
                                                                                       b__16270
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6024__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__16269
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__16269
                                                                                              size__6024__auto__)
                                                                                            (let 
                                                                                              [di
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6023__auto__
                                                                                                 (int
                                                                                                   i__16269))]
                                                                                              (chunk-append
                                                                                                b__16270
                                                                                                (common/getx
                                                                                                  lookup
                                                                                                  (.-uuid
                                                                                                    (nth
                                                                                                      dir
                                                                                                      (int
                                                                                                        ^java.lang.Number di)))))
                                                                                              (recur
                                                                                                (inc
                                                                                                  i__16269)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__16270)
                                                                                          (^clojure.lang.IFn iter__16267
                                                                                            (chunk-rest
                                                                                              s__16268)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__16270)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [di
                                                                                       (first
                                                                                         s__16268)]
                                                                                      (cons
                                                                                        (common/getx
                                                                                          lookup
                                                                                          (.-uuid
                                                                                            (nth
                                                                                              dir
                                                                                              (int
                                                                                                ^java.lang.Number di))))
                                                                                        (^clojure.lang.IFn iter__16267
                                                                                          (rest
                                                                                            s__16268)))))))))))
                                                 fs__6022__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6021__auto__
                                                                      (range
                                                                        (long didx)
                                                                        (java.lang.Integer/valueOf
                                                                          (int (count dir))))))]
                                             (if fs__6022__auto__
                                               (concat
                                                 fs__6022__auto__
                                                 (^clojure.lang.IFn iter__16265 (rest s__16266)))
                                               (recur (rest s__16266))))))))))]
        (^clojure.lang.IFn iter__6025__auto__
          (range (long ridx) (java.lang.Integer/valueOf (int (count root_val)))))))
    (log-dir-seq
      [this]
      (let [iter__6025__auto__ (fn iter__16252
                                 ([s__16253]
                                   (lazy-seq
                                     (let [s__16253 s__16253 temp__5457__auto__ (seq s__16253)]
                                       (when temp__5457__auto__
                                         (let [s__16253 temp__5457__auto__]
                                           (if (chunked-seq? s__16253)
                                             (let [c__6023__auto__ (chunk-first s__16253)
                                                   size__6024__auto__ (int (count c__6023__auto__))
                                                   b__16255 (chunk-buffer
                                                              (java.lang.Integer/valueOf
                                                                (int size__6024__auto__)))]
                                               (if (loop [i__16254 (int 0)]
                                                     (if (< i__16254 size__6024__auto__)
                                                       (let [ri
                                                             (.nth
                                                               ^clojure.lang.Indexed c__6023__auto__
                                                               (int i__16254))]
                                                         (chunk-append
                                                           b__16255
                                                           (common/getx
                                                             lookup
                                                             (:uuid
                                                               (nth
                                                                 root_val
                                                                 (int ^java.lang.Number ri)))))
                                                         (recur (inc i__16254)))
                                                       true))
                                                 (chunk-cons
                                                   (chunk b__16255)
                                                   (^clojure.lang.IFn iter__16252
                                                     (chunk-rest s__16253)))
                                                 (chunk-cons (chunk b__16255) nil)))
                                             (let [ri (first s__16253)]
                                               (cons
                                                 (common/getx
                                                   lookup
                                                   (:uuid
                                                     (nth root_val (int ^java.lang.Number ri))))
                                                 (^clojure.lang.IFn iter__16252
                                                   (rest s__16253)))))))))))]
        (^clojure.lang.IFn iter__6025__auto__
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
            temp__5455__auto__ (write-tail-descriptor cs proposed_desc pod_buf)]
        (if temp__5455__auto__
          (let [new_desc temp__5455__auto__] (assoc this :desc new_desc :tail tail))
          (do (throw (java.lang.Error. "Conflict adopting log root.")) nil))))
    (append
      [this cs msgs]
      (let [bufs (map :fressianed-tx msgs)
            ids (map :id msgs)
            txes (map :tx msgs)
            new_tail (extend-tail tail txes bufs)
            log (let [m_16313 {:event :log/add-next, :txids ids, :firstT (:t (first txes))}
                      ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.log")]
                                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                          (.debug
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_16313 :phase :begin)))
                                          nil)
                                        nil)
                      start__8981__auto__ (java.lang.System/nanoTime)
                      result__8982__auto__ (try
                                             {:returned
                                              (let [temp__5455__auto__ (write-tail-descriptor
                                                                         cs
                                                                         (update-in
                                                                           desc
                                                                           [:rev]
                                                                           inc)
                                                                         (io/unchunk bufs))]
                                                (if temp__5455__auto__
                                                  (let [new_desc temp__5455__auto__]
                                                    (assoc this :tail new_tail :desc new_desc))
                                                  (do
                                                    (throw
                                                      (java.lang.Error.
                                                        "Conflict updating log tail"))
                                                    1)))}
                                             (catch
                                               java.lang.Throwable
                                               t__8983__auto__
                                               {:threw t__8983__auto__}))
                      elapsed_16314 (- (java.lang.System/nanoTime) start__8981__auto__)
                      msec_16315 (logger/format-as-msec (long elapsed_16314))]
                  (monitor/add-stat :LogWriteMsec msec_16315)
                  (let [endmsg__8984__auto__ (merge
                                               (assoc m_16313 :msec msec_16315 :phase :end)
                                               (when (:threw result__8982__auto__)
                                                 {:threw (class (:threw result__8982__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                      (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                      nil)
                    nil)
                  (if (contains? result__8982__auto__ :returned)
                    (:returned result__8982__auto__)
                    (do (throw (:threw result__8982__auto__)) nil)))]
        (tx/log-completion! ids)
        log))
    (val-keys
      [this]
      (tree-seq
        (fn fn__16307 ([k] (instance? datomic.log.LogDir (first (get olookup k)))))
        (fn fn__16309
          ([k] (map (fn fn__16310 ([ld] (cluster/uuid->val-key (:uuid ld)))) (get olookup k))))
        (cluster/uuid->val-key (get-root-id this))))
    (get-root-val [this] (vec (common/getx olookup (get-root-id this))))
    (get-root-id [this] (:d/r desc))
    (claim
      [this cs]
      (let [temp__5457__auto__ (claim-log cs desc)]
        (when temp__5457__auto__ (let [new_desc temp__5457__auto__] (assoc this :desc new_desc)))))
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
    ([m__7585__auto__]
      (LogImpl/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
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
      (let [vec__16342 (repeatedly (fn fn__16345 ([] (common/rand-uuid))))
            new_root_id (nth vec__16342 (int 0) nil)
            new_tail_id (nth vec__16342 (int 1) nil)
            root_val_result (deref
                              (zip-and-create
                                cs
                                new_root_id
                                (fressianed-dir [(create-entry 0 new_tail_id)])))
            tail_dir_result (deref (zip-and-create cs new_tail_id (fressianed-dir [])))
            tail (empty-tail)
            root_id (cluster/uuid->val-key new_root_id)
            proposed_desc {:rev 0, :etag nil, :d/r root_id, :d/l 3}
            temp__5457__auto__ (write-tail-descriptor cs proposed_desc BEGIN_OPEN_LIST)]
        (when temp__5457__auto__ (let [desc temp__5457__auto__] [desc tail])))))
  (defn create-new-log
    ([cs olookup]
      (let [temp__5457__auto__ (write-new-log cs)]
        (when temp__5457__auto__
          (let [vec__16349 temp__5457__auto__
                desc (nth vec__16349 (int 0) nil)
                tail (nth vec__16349 (int 1) nil)]
            (create-log-impl olookup desc tail))))))
  (defn seek-tx ([provider t] (seek-tx-impl provider t)))
  (defn find-log
    ([cs olookup]
      (let [temp__5457__auto__ (read-tail-descriptor cs)]
        (when temp__5457__auto__
          (let [vec__16355 temp__5457__auto__
                desc (nth vec__16355 (int 0) nil)
                buf (nth vec__16355 (int 1) nil)
                tail (load-tail buf)]
            (create-log-impl olookup desc tail))))))
  (defn log-tree
    ([olookup root_id] (create-log-impl olookup {:rev 0, :d/l 3, :d/r root_id} (empty-tail))))
  (defn catchup-tx
    ([p__16361 tx]
      (let [map__16362 p__16361
            map__16362 (if (seq? map__16362)
                         (clojure.lang.PersistentHashMap/create (seq map__16362))
                         map__16362)
            db (get map__16362 :db)
            size (get map__16362 :size)
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
                    {:event :log/overlap, :t (long (.getT ^datomic.impl.db.IDatum d))}))
                nil)
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
                  (fn fn__16366
                    ([]
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                          (.info
                            ^org.slf4j.Logger logger
                            (logger/process
                              {:event :log/load-segments,
                               :count
                               (java.lang.Integer/valueOf
                                 (int (count (pmap identity (log-seg-seq iter)))))}))
                          nil)
                        nil)))))
            log_txes (iter/iter-seq iter)
            map__16365 (reduce catchup-tx {:db db, :size 0} log_txes)
            map__16365 (if (seq? map__16365)
                         (clojure.lang.PersistentHashMap/create (seq map__16365))
                         map__16365)
            result map__16365
            db (get map__16365 :db)
            size (get map__16365 :size)
            elapsed (- (java.lang.System/currentTimeMillis) start)
            db (if catchup_ft
                 (let [m_16368 {:event :log/catchup-fulltext}
                       ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.log")]
                                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                           (.info
                                             ^org.slf4j.Logger logger
                                             (logger/process (assoc m_16368 :phase :begin)))
                                           nil)
                                         nil)
                       start__8981__auto__ (java.lang.System/nanoTime)
                       result__8982__auto__ (try
                                              {:returned
                                               (db/add-fulltext db (mapcat :data log_txes))}
                                              (catch
                                                java.lang.Throwable
                                                t__8983__auto__
                                                {:threw t__8983__auto__}))
                       elapsed_16369 (- (java.lang.System/nanoTime) start__8981__auto__)
                       msec_16370 (logger/format-as-msec (long elapsed_16369))]
                   (let [endmsg__8984__auto__ (merge
                                                (assoc m_16368 :msec msec_16370 :phase :end)
                                                (when (:threw result__8982__auto__)
                                                  {:threw (class (:threw result__8982__auto__))}))
                         logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                       (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                       nil)
                     nil)
                   (if (contains? result__8982__auto__ :returned)
                     (:returned result__8982__auto__)
                     (do (throw (:threw result__8982__auto__)) nil)))
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
                 :msec (long elapsed)}))
            nil)
          nil)
        (monitor/add-stat :LogIngestMsec (long elapsed))
        (monitor/add-stat :LogIngestBytes size)
        {:db db, :size size}))
    ([db log] (catchup db log false)))
  (defn ensure-index-and-log
    ([cluster olookup db_id]
      (let [m_16378 {:event :transactor/ensure-index-and-log, :db-id db_id}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_16378 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
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
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_16379 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_16380 (logger/format-as-msec (long elapsed_16379))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_16378 :msec msec_16380 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn excise-ts
    ([log xpreds]
      (reduce
        (fn fn__16392
          ([s p]
            (into
              s
              (map
                (fn fn__16393 ([p1__16391#] (long (.getT ^datomic.impl.db.IDatum p1__16391#))))
                (x/datoms p)))))
        #{}
        xpreds)))
  (defn excise-dir-map
    ([log ts]
      (reduce
        (fn fn__16399
          ([m p__16398]
            (let [vec__16400 p__16398
                  dirid (nth vec__16400 (int 0) nil)
                  segid (nth vec__16400 (int 1) nil)]
              (if (contains? (get m dirid) segid)
                m
                (assoc m dirid (conj (get m dirid #{}) segid))))))
        {}
        (map (fn fn__16404 ([p1__16397#] (seek-seg-path log p1__16397#))) ts))))
  (defn write-excised-log
    ([cs lookup xpreds ts dir_map]
      (reduce
        (fn fn__16409
          ([m p__16408]
            (let [vec__16410 p__16408
                  dirid (nth vec__16410 (int 0) nil)
                  segids (nth vec__16410 (int 1) nil)
                  excise? (fn excise_QMARK_
                            ([d]
                              (some
                                (fn fn__16417 ([p1__16407#] (x/remove? p1__16407# d)))
                                xpreds)))
                  dir (common/getx lookup dirid)
                  vec__16413 (reduce
                               (fn fn__16421
                                 ([p__16420 direntry]
                                   (let [vec__16422 p__16420
                                         m (nth vec__16422 (int 0) nil)
                                         newdir (nth vec__16422 (int 1) nil)
                                         t (.-t ^datomic.log.LogDir direntry)
                                         segid (.-uuid ^datomic.log.LogDir direntry)]
                                     (if (contains? segids segid)
                                       (let [seg (common/getx lookup segid)
                                             newsid (common/rand-uuid)
                                             newseg (mapv
                                                      (fn fn__16426
                                                        ([p__16425]
                                                          (let [map__16427 p__16425
                                                                map__16427
                                                                (if
                                                                  (seq? map__16427)
                                                                  (clojure.lang.PersistentHashMap/create
                                                                    (seq map__16427))
                                                                  map__16427)
                                                                tx map__16427
                                                                data (get map__16427 :data)
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
                  m (nth vec__16413 (int 0) nil)
                  newdir (nth vec__16413 (int 1) nil)
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
                   (fn fn__16433 ([p1__16432#] (monitor/add-stat :ExciseIOQueueCount p1__16432#))))
                 cs)
            replacements (write-excised-log cs lookup xpreds ts dir_map)]
        (when pario
          (common/bounded-deref (cluster/finish-writer cs) cluster/BOUNDING_TIMEOUT_MSEC))
        {:dir-map dir_map, :replacements replacements})))
  (defn excise-root
    ([cs lookup current_root_id p__16438]
      (let [map__16439 p__16438
            map__16439 (if (seq? map__16439)
                         (clojure.lang.PersistentHashMap/create (seq map__16439))
                         map__16439)
            dir_map (get map__16439 :dir-map)
            replacements (get map__16439 :replacements)
            log (log-tree lookup current_root_id)
            root (get-root-val log)
            dirs (into
                   #{}
                   (map
                     (fn fn__16440 ([p1__16436#] (.-uuid ^datomic.log.LogDir p1__16436#)))
                     root))
            patch_dir (fn patch_dir
                        ([replacements dir]
                          (reduce
                            (fn fn__16443
                              ([newdir direntry]
                                (let [eid (.-uuid ^datomic.log.LogDir direntry)]
                                  (conj
                                    newdir
                                    (let [temp__5455__auto__ (^clojure.lang.IFn replacements eid)]
                                      (if temp__5455__auto__
                                        (let [neweid temp__5455__auto__]
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
                   (fn fn__16447 ([p1__16437#] (monitor/add-stat :ExciseIOQueueCount p1__16437#))))
                 cs)
            replacements (reduce
                           (fn fn__16449
                             ([m did]
                               (let [segids (^clojure.lang.IFn dir_map did)
                                     newdid (common/rand-uuid)
                                     vec__16450 (seek-seg-path
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
                                     dirid (nth vec__16450 (int 0) nil)
                                     _ (nth vec__16450 (int 1) nil)
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
                                          :newid newdid}))
                                     nil)
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
    ([m__7585__auto__]
      (LogTailValue/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (extend
    datomic.db.MemLog
    LogSeek
    {:seek-tx-impl
     (fn fn__16476
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
                  (iter/take-while (fn fn__16480 ([p1__16478#] (< (:t p1__16478#) end))) ret)
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
    ([m__7585__auto__]
      (LogValue/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
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
      (let [temp__5457__auto__ (deref (cluster/get-pod-meta cs (tail-pod-key cs)))]
        (when temp__5457__auto__
          (let [desc temp__5457__auto__
                desc (normalize-desc desc cs)
                root_id (cluster/val-key->uuid (:d/r desc))]
            (->LogValue db olookup root_id (:memlog db)))))))
  (defn max-t
    ([datoms]
      (reduce (fn fn__16518 ([n datom] (max n (db/eid->eidx (long (:tx datom)))))) 0 datoms)))
  (defn max-eidx
    ([datoms]
      (reduce
        (fn fn__16521
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
    ([p__16524]
      (let [map__16525 p__16524
            map__16525 (if (seq? map__16525)
                         (clojure.lang.PersistentHashMap/create (seq map__16525))
                         map__16525)
            log map__16525
            olookup (get map__16525 :olookup)]
        (last-tree-tx olookup (or (:root-id log) (get-root-id log))))))
  (defn root-id ([cs] (:d/r (deref (cluster/get-pod-meta cs (tail-pod-key cs))))))
  (defn partition-by-weight
    ([weigh target]
      (fn fn__16529
        ([rf]
          (let [weight_ref (volatile! 0) part_ref (volatile! [])]
            (fn fn__16530
              ([] (^clojure.lang.IFn rf))
              ([result]
                (let [temp__5455__auto__ (seq (deref part_ref))]
                  (if temp__5455__auto__
                    (let [part temp__5455__auto__]
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
      (fn fn__16535
        ([rf]
          (let [tail_ref (volatile! nil)]
            (fn fn__16536
              ([] (^clojure.lang.IFn rf))
              ([result]
                (let [vec__16537 (deref tail_ref)
                      p1 (nth vec__16537 (int 0) nil)
                      p2 (nth vec__16537 (int 1) nil)]
                  (if p1
                    (if (^clojure.lang.IFn pred p2)
                      (^clojure.lang.IFn rf
                        (^clojure.lang.IFn rf result (^clojure.lang.IFn combine p1 p2)))
                      (^clojure.lang.IFn rf
                        (^clojure.lang.IFn rf (^clojure.lang.IFn rf result p1) p2)))
                    (cond-> result p2 (^clojure.lang.IFn rf p2) :finish (^clojure.lang.IFn rf)))))
              ([result input]
                (let [vec__16541 (deref tail_ref)
                      p1 (nth vec__16541 (int 0) nil)
                      p2 (nth vec__16541 (int 1) nil)]
                  (vreset! tail_ref [p2 input])
                  (if p1 (^clojure.lang.IFn rf result p1) result)))))))))
  (defn create-leaves*
    ([target_size ftxes]
      (let [weigh (comp io/remaining second)]
        (sequence
          (comp
            (partition-by-weight weigh target_size)
            (combine-last-if
              (fn fn__16547 ([ftxes] (< (apply + (map weigh ftxes)) (quot target_size 2))))
              into)
            (map
              (fn fn__16549
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
            vec__16554 (repeatedly common/rand-uuid)
            new_root_id (nth vec__16554 (int 0) nil)
            new_dir_id (nth vec__16554 (int 1) nil)
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
        (if (every? (fn fn__16560 ([p1__16553#] (= p1__16553# :created))) (map deref results))
          {:root-id (cluster/uuid->val-key new_root_id),
           :dir-id (cluster/uuid->val-key new_dir_id),
           :leaf-ts leaf_ts,
           :garbage-ids garbage_ids}
          (do (throw (java.lang.Error. "Write failure extending log tree")) nil)))))
  (defn segment
    ([cluster olookup]
      (let [map__16563 (find-log cluster olookup)
            map__16563 (if (seq? map__16563)
                         (clojure.lang.PersistentHashMap/create (seq map__16563))
                         map__16563)
            log map__16563
            desc (get map__16563 :desc)
            tail (get map__16563 :tail)]
        (if (tail-empty? tail)
          log
          (let [map__16564 (extend-tree
                             cluster
                             olookup
                             (:d/r desc)
                             dir-threshold
                             (create-leaves segment-threshold tail))
                map__16564 (if (seq? map__16564)
                             (clojure.lang.PersistentHashMap/create (seq map__16564))
                             map__16564)
                root_id (get map__16564 :root-id)]
            (adopt-root log cluster root_id (last (map :t (:txes tail))))))))))
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
  (.setMeta (clojure.lang.RT/var "datomic.log" "btree-search") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.log" "btree-search") (deref #'index/btree-search))
  (.setMeta (clojure.lang.RT/var "datomic.log" "binary-search") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.log" "binary-search") (deref #'index/binary-search))
  (def segment-threshold 54000)
  (reset-meta! #'segment-threshold (assoc {:column (int 1)} :name 'segment-threshold :ns *ns*))
  (def dir-threshold 1000)
  (reset-meta! #'dir-threshold (assoc {:column (int 1)} :name 'dir-threshold :ns *ns*))
  (defn last-by-nth ([coll] (let [c (count coll)] (when-not (= c 0) (nth coll (int (dec c)))))))
  (reset-meta!
    #'last-by-nth
    (assoc
      {:private true, :arglists (clojure.core/list ['coll]), :column (int 1)}
      :name
      'last-by-nth
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      LogSeek
      (seek-tx-impl [log t] "Returns iterator over log transactions at t")
      (seek-seg-path [log t] "Returns the [dirid segid] that contains/would contain t."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.log" "LogSeek")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'LogSeek :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'seek-tx-impl
                                        {:arglists (clojure.core/list ['log 't])}),
                                      :arglists (clojure.core/list ['log 't]),
                                      :doc "Returns iterator over log transactions at t"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "LogSeek"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "seek-tx-impl")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'seek-seg-path
                                        {:arglists (clojure.core/list ['log 't])}),
                                      :arglists (clojure.core/list ['log 't]),
                                      :doc
                                      "Returns the [dirid segid] that contains/would contain t."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "LogSeek"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "seek-seg-path")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*))))
  (defn seek-seg-id
    ([log t]
      (let [vec__16011 (seek-seg-path log t)
            dirid (nth vec__16011 (int 0) nil)
            segid (nth vec__16011 (int 1) nil)]
        segid)))
  (reset-meta!
    #'seek-seg-id
    (assoc {:arglists (clojure.core/list ['log 't]), :column (int 1)} :name 'seek-seg-id :ns *ns*))
  (let [protocol_metadata__7468 {:column (int 1)}]
    (defprotocol
      Log
      (claim [log cs] "Touch the tail pod and root rev. Returns updated log or nil")
      (get-root-val [log] "Returns the root")
      (get-root-id [log] "Returns the root id")
      (adopt-root
        [log cs root-id t]
        "Adopt a new root. Updates pod. If t is not nil,\ntruncates tail to txes since t. Returns the updated log.")
      (append [log cs msgs] "Appends tx msgs, segments if necessary, updates pod, returns log")
      (val-keys [log] "Returns inorder traversal of keys (strings)"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.log" "Log")
      (assoc (assoc protocol_metadata__7468 :doc nil) :name 'Log :ns *ns*))
    (let [protocol_signature__7469 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'claim
                                        {:arglists (clojure.core/list ['log 'cs])}),
                                      :arglists (clojure.core/list ['log 'cs]),
                                      :doc
                                      "Touch the tail pod and root rev. Returns updated log or nil"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "Log"))
          protocol_method_name__7470 (with-meta
                                       (:name protocol_signature__7469)
                                       protocol_signature__7469)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "claim")
        (assoc protocol_signature__7469 :name protocol_method_name__7470 :ns *ns*)))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-root-val
                                        {:arglists (clojure.core/list ['log])}),
                                      :arglists (clojure.core/list ['log]),
                                      :doc "Returns the root"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "Log"))
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "get-root-val")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-root-id
                                        {:arglists (clojure.core/list ['log])}),
                                      :arglists (clojure.core/list ['log]),
                                      :doc "Returns the root id"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "Log"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "get-root-id")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'adopt-root
                                        {:arglists (clojure.core/list ['log 'cs 'root-id 't])}),
                                      :arglists (clojure.core/list ['log 'cs 'root-id 't]),
                                      :doc
                                      "Adopt a new root. Updates pod. If t is not nil,\ntruncates tail to txes since t. Returns the updated log."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "Log"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "adopt-root")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*)))
    (let [protocol_signature__7477 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'append
                                        {:arglists (clojure.core/list ['log 'cs 'msgs])}),
                                      :arglists (clojure.core/list ['log 'cs 'msgs]),
                                      :doc
                                      "Appends tx msgs, segments if necessary, updates pod, returns log"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "Log"))
          protocol_method_name__7478 (with-meta
                                       (:name protocol_signature__7477)
                                       protocol_signature__7477)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "append")
        (assoc protocol_signature__7477 :name protocol_method_name__7478 :ns *ns*)))
    (let [protocol_signature__7479 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'val-keys {:arglists (clojure.core/list ['log])}),
                                      :arglists (clojure.core/list ['log]),
                                      :doc "Returns inorder traversal of keys (strings)"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "Log"))
          protocol_method_name__7480 (with-meta
                                       (:name protocol_signature__7479)
                                       protocol_signature__7479)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "val-keys")
        (assoc protocol_signature__7479 :name protocol_method_name__7480 :ns *ns*))))
  (let [protocol_metadata__7481 {:column (int 1)}]
    (defprotocol TailTxes (tail-txes [_] "Returns the tail txes"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.log" "TailTxes")
      (assoc (assoc protocol_metadata__7481 :doc nil) :name 'TailTxes :ns *ns*))
    (let [protocol_signature__7482 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'tail-txes {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns the tail txes"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "TailTxes"))
          protocol_method_name__7483 (with-meta
                                       (:name protocol_signature__7482)
                                       protocol_signature__7482)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "tail-txes")
        (assoc protocol_signature__7482 :name protocol_method_name__7483 :ns *ns*))))
  (let [protocol_metadata__7484 {:column (int 1)}]
    (defprotocol LogKey (log-key [_] "Key for navigating log structures on disk."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.log" "LogKey")
      (assoc (assoc protocol_metadata__7484 :doc nil) :name 'LogKey :ns *ns*))
    (let [protocol_signature__7485 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'log-key {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Key for navigating log structures on disk."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "LogKey"))
          protocol_method_name__7486 (with-meta
                                       (:name protocol_signature__7485)
                                       protocol_signature__7485)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "log-key")
        (assoc protocol_signature__7485 :name protocol_method_name__7486 :ns *ns*))))
  (let [protocol_metadata__7487 {:column (int 1)}]
    (defprotocol
      LogSegSeq
      (log-seg-seq
        [_]
        "Returns a seq of log segments starting at iter.\n                       Loads the segs as it goes."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.log" "LogSegSeq")
      (assoc (assoc protocol_metadata__7487 :doc nil) :name 'LogSegSeq :ns *ns*))
    (let [protocol_signature__7488 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'log-seg-seq
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Returns a seq of log segments starting at iter.\n                       Loads the segs as it goes."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "LogSegSeq"))
          protocol_method_name__7489 (with-meta
                                       (:name protocol_signature__7488)
                                       protocol_signature__7488)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "log-seg-seq")
        (assoc protocol_signature__7488 :name protocol_method_name__7489 :ns *ns*))))
  (let [protocol_metadata__7490 {:column (int 1)}]
    (defprotocol
      LogDirSeq
      (log-dir-seq
        [_]
        "Returns a seq of log dir segments starting at iter.\n                       Loads the segs as it goes."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.log" "LogDirSeq")
      (assoc (assoc protocol_metadata__7490 :doc nil) :name 'LogDirSeq :ns *ns*))
    (let [protocol_signature__7491 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'log-dir-seq
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Returns a seq of log dir segments starting at iter.\n                       Loads the segs as it goes."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.log" "LogDirSeq"))
          protocol_method_name__7492 (with-meta
                                       (:name protocol_signature__7491)
                                       protocol_signature__7491)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.log" "log-dir-seq")
        (assoc protocol_signature__7491 :name protocol_method_name__7492 :ns *ns*))))
  (defn fressianed-txes-length
    ([fressianed_txes]
      (apply
        +
        (map
          (fn fn__16172 ([tx] (java.lang.Integer/valueOf (int (.length (:fressianed-tx tx))))))
          fressianed_txes))))
  (reset-meta!
    #'fressianed-txes-length
    (assoc
      {:arglists (clojure.core/list ['fressianed-txes]), :column (int 1)}
      :name
      'fressianed-txes-length
      :ns
      *ns*))
  (extend java.util.Map LogKey {:log-key (fn fn__16175 ([_] (.get ^java.util.Map _ :t)))})
  (extend java.lang.Long LogKey {:log-key (fn fn__16177 ([_] _))})
  (.setMeta (clojure.lang.RT/var "datomic.log" "->LogDir") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.log" "map->LogDir") {:declared true, :column (int 1)})
  (defrecord LogDir [^long t ^UUID uuid] datomic.log.LogKey (log-key [this] (long t)))
  (clojure.core/import 'datomic.log.LogDir)
  (defn ->LogDir ([t uuid] (datomic.log.LogDir. (long ^java.lang.Number t) uuid)))
  (reset-meta!
    #'->LogDir
    (assoc {:arglists (clojure.core/list ['t 'uuid]), :column (int 1)} :name '->LogDir :ns *ns*))
  (defn map->LogDir
    ([m__7972__auto__]
      (LogDir/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->LogDir
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->LogDir
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.log" "write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "write-handlers")
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
  (.setMeta (clojure.lang.RT/var "datomic.log" "read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "read-handlers")
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
  (reset-meta!
    #'fressianed-tx
    (assoc {:arglists (clojure.core/list ['tx]), :column (int 1)} :name 'fressianed-tx :ns *ns*))
  (defn create-entry
    ([t uuid] (datomic.log.LogDir. (long ^java.lang.Number t) uuid))
    ([t] (create-entry t (common/rand-uuid))))
  (reset-meta!
    #'create-entry
    (assoc
      {:arglists (clojure.core/list ['t] ['t 'uuid]), :column (int 1)}
      :name
      'create-entry
      :ns
      *ns*))
  (defn tail-pod-key
    ([cs]
      (let [temp__5804__auto__ (cluster/dbId cs)]
        (when temp__5804__auto__ (let [dbid temp__5804__auto__] (str "pod-log-tail/" dbid))))))
  (reset-meta!
    #'tail-pod-key
    (assoc {:arglists (clojure.core/list ['cs]), :column (int 1)} :name 'tail-pod-key :ns *ns*))
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
      {:private true, :arglists (clojure.core/list ['desc]), :column (int 1)}
      :name
      'tail-descriptor?
      :ns
      *ns*))
  (defn inc-rev ([m] (update-in m [:rev] inc)))
  (reset-meta!
    #'inc-rev
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'inc-rev :ns *ns*))
  (defn legacy-root-ref-key
    ([cs]
      (let [temp__5804__auto__ (cluster/dbId cs)]
        (when temp__5804__auto__
          (let [dbid temp__5804__auto__] (str "ref-log-root/" (cluster/dbId cs)))))))
  (reset-meta!
    #'legacy-root-ref-key
    (assoc
      {:arglists (clojure.core/list ['cs]), :column (int 1)}
      :name
      'legacy-root-ref-key
      :ns
      *ns*))
  (def LOG_VERSION 3)
  (reset-meta! #'LOG_VERSION (assoc {:const true, :column (int 1)} :name 'LOG_VERSION :ns *ns*))
  (defn normalize-desc
    ([desc cs]
      (let [rev (long (or (:d/l desc) 1)) G__16219 rev]
        (case
          G__16219
          1
          (assoc desc :d/l 1 :d/r (:key (deref (cluster/get-ref cs (legacy-root-ref-key cs)))))
          (2 3)
          desc
          (error/state
            :db.error/log-version
            (str "This version of Datomic cannot read log version " (long rev))
            #:d{:l (long rev)})))))
  (reset-meta!
    #'normalize-desc
    (assoc
      {:arglists (clojure.core/list ['desc 'cs]), :column (int 1)}
      :name
      'normalize-desc
      :ns
      *ns*))
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
  (reset-meta!
    #'read-tail-descriptor
    (assoc
      {:arglists (clojure.core/list ['cs]), :column (int 1)}
      :name
      'read-tail-descriptor
      :ns
      *ns*))
  (defn pod-update-succeeded? ([response] (every? response [:rev :etag])))
  (reset-meta!
    #'pod-update-succeeded?
    (assoc
      {:arglists (clojure.core/list ['response]), :column (int 1)}
      :name
      'pod-update-succeeded?
      :ns
      *ns*))
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
  (reset-meta!
    #'write-tail-descriptor
    (assoc
      {:arglists (clojure.core/list ['cs 'desc 'buf]), :column (int 1)}
      :name
      'write-tail-descriptor
      :ns
      *ns*))
  (defn claim-log
    ([cs desc]
      (let [temp__5804__auto__ (write-tail-descriptor cs (inc-rev desc) nil)]
        (when temp__5804__auto__
          (let [new_desc temp__5804__auto__]
            (when (= 1 (:d/l desc)) (deref (cluster/delete-reference cs (legacy-root-ref-key cs))))
            new_desc)))))
  (reset-meta!
    #'claim-log
    (assoc {:arglists (clojure.core/list ['cs 'desc]), :column (int 1)} :name 'claim-log :ns *ns*))
  (defn convert-log-version
    ([cs to_version]
      (error/raise
        :db.error/log-conversion
        "This version of Datomic cannot convert log versions")))
  (reset-meta!
    #'convert-log-version
    (assoc
      {:arglists (clojure.core/list ['cs 'to-version]), :column (int 1)}
      :name
      'convert-log-version
      :ns
      *ns*))
  (defn zip-and-create
    ([cs uuid buf write_type]
      (let [gz_buf (io/gzip-buffer buf)
            kw (let [pred__16229 = expr__16230 write_type]
                 (if (^clojure.lang.IFn pred__16229 :root expr__16230)
                   :LogTreeRootBytesWritten
                   (if (^clojure.lang.IFn pred__16229 :dir expr__16230)
                     :LogTreeDirBytesWritten
                     (if (^clojure.lang.IFn pred__16229 :leaf expr__16230)
                       :LogTreeLeafBytesWritten
                       (if (^clojure.lang.IFn pred__16229 :excise expr__16230)
                         :LogTreeExciseBytesWritten
                         :LogTreeUnknownBytesWritten)))))]
        (monitor/add-stat
          kw
          (java.lang.Integer/valueOf (int (.remaining ^java.nio.Buffer gz_buf))))
        (cluster/create-val cs 2 (cluster/uuid->val-key uuid) gz_buf)))
    ([cs uuid buf] (zip-and-create cs uuid buf nil)))
  (reset-meta!
    #'zip-and-create
    (assoc
      {:arglists (clojure.core/list ['cs 'uuid 'buf] ['cs 'uuid 'buf 'write-type]),
       :column (int 1)}
      :name
      'zip-and-create
      :ns
      *ns*))
  (defn write-excise-val
    ([cs uuid fressianed_buf]
      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
        (when (.isDebugEnabled ^org.slf4j.Logger logger)
          (.debug
            ^org.slf4j.Logger logger
            (logger/process {:event :log/write-excise-val, :id uuid})))
        nil)
      (when-not (= :created (deref (zip-and-create cs uuid fressianed_buf :excise)))
        (throw (java.lang.Error. "Cluster value creation failed. Unable to write log."))
        nil)))
  (reset-meta!
    #'write-excise-val
    (assoc
      {:arglists (clojure.core/list ['cs 'uuid 'fressianed-buf]), :column (int 1)}
      :name
      'write-excise-val
      :ns
      *ns*))
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
  (reset-meta!
    #'->TailTxIter
    (assoc
      {:arglists (clojure.core/list ['txes 'idx]), :column (int 1)}
      :name
      '->TailTxIter
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.log" "BEGIN_OPEN_LIST") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "BEGIN_OPEN_LIST")
    (ByteBuffer/wrap
      (byte-array [(java.lang.Byte/valueOf (unchecked-byte Codes/BEGIN_OPEN_LIST))])))
  (.setMeta (clojure.lang.RT/var "datomic.log" "BEGIN_CLOSED_LIST") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "BEGIN_CLOSED_LIST")
    (ByteBuffer/wrap
      (byte-array [(java.lang.Byte/valueOf (unchecked-byte Codes/BEGIN_CLOSED_LIST))])))
  (.setMeta (clojure.lang.RT/var "datomic.log" "END_COLLECTION") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "END_COLLECTION")
    (ByteBuffer/wrap
      (byte-array [(java.lang.Byte/valueOf (unchecked-byte Codes/END_COLLECTION))])))
  (.setMeta (clojure.lang.RT/var "datomic.log" "create-tail") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.log" "empty-tail") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.log" "->Tail") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.log" "map->Tail") {:declared true, :column (int 1)})
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
  (reset-meta!
    #'->Tail
    (assoc {:arglists (clojure.core/list ['txes 'bufs]), :column (int 1)} :name '->Tail :ns *ns*))
  (defn map->Tail
    ([m__7972__auto__]
      (Tail/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->Tail
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->Tail
      :ns
      *ns*))
  (defn tail-byte-count
    ([p__16262]
      (let [map__16263 p__16262
            map__16263 (if (seq? map__16263)
                         (if (next map__16263)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16263))
                           (if (seq map__16263) (first map__16263) {}))
                         map__16263)
            bufs (get map__16263 :bufs)]
        (apply + (map io/remaining bufs)))))
  (reset-meta!
    #'tail-byte-count
    (assoc
      {:arglists (clojure.core/list [{:keys ['bufs]}]), :column (int 1)}
      :name
      'tail-byte-count
      :ns
      *ns*))
  (defn tail-ts
    ([p__16265]
      (let [map__16266 p__16265
            map__16266 (if (seq? map__16266)
                         (if (next map__16266)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16266))
                           (if (seq map__16266) (first map__16266) {}))
                         map__16266)
            txes (get map__16266 :txes)]
        (map :t txes))))
  (reset-meta!
    #'tail-ts
    (assoc
      {:arglists (clojure.core/list [{:keys ['txes]}]), :column (int 1)}
      :name
      'tail-ts
      :ns
      *ns*))
  (defn tail-empty?
    ([p__16268]
      (let [map__16269 p__16268
            map__16269 (if (seq? map__16269)
                         (if (next map__16269)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16269))
                           (if (seq map__16269) (first map__16269) {}))
                         map__16269)
            txes (get map__16269 :txes)]
        (empty? txes))))
  (reset-meta!
    #'tail-empty?
    (assoc
      {:arglists (clojure.core/list [{:keys ['txes]}]), :column (int 1)}
      :name
      'tail-empty?
      :ns
      *ns*))
  (defn extend-tail
    ([p__16271 new_txes new_bufs]
      (let [map__16272 p__16271
            map__16272 (if (seq? map__16272)
                         (if (next map__16272)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16272))
                           (if (seq map__16272) (first map__16272) {}))
                         map__16272)
            txes (get map__16272 :txes)
            bufs (get map__16272 :bufs)]
        (create-tail (into txes new_txes) (into bufs new_bufs)))))
  (reset-meta!
    #'extend-tail
    (assoc
      {:arglists (clojure.core/list [{:keys ['txes 'bufs]} 'new-txes 'new-bufs]), :column (int 1)}
      :name
      'extend-tail
      :ns
      *ns*))
  (defn since
    ([p__16274 since_t]
      (let [map__16275 p__16274
            map__16275 (if (seq? map__16275)
                         (if (next map__16275)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16275))
                           (if (seq map__16275) (first map__16275) {}))
                         map__16275)
            tail map__16275
            txes (get map__16275 :txes)
            bufs (get map__16275 :bufs)]
        (if (or (not since_t) (tail-empty? tail) (< since_t (first (tail-ts tail))))
          tail
          (let [new_txes (loop [txes txes]
                           (let [vec__16277 txes
                                 seq__16278 (seq vec__16277)
                                 first__16279 (first seq__16278)
                                 seq__16278 (next seq__16278)
                                 tx first__16279
                                 more seq__16278]
                             (if tx (if (< since_t (:t tx)) (into [] txes) (recur more)) [])))
                bufs_ct (count bufs)
                new_bufs (into [] (drop (long (- bufs_ct (count new_txes)))) bufs)]
            (create-tail new_txes new_bufs))))))
  (reset-meta!
    #'since
    (assoc
      {:arglists (clojure.core/list [{:keys ['txes 'bufs], :as 'tail} 'since-t]), :column (int 1)}
      :name
      'since
      :ns
      *ns*))
  (defn resets-caches?
    ([bbuf]
      (=
        (long (java.lang.Byte/valueOf (unchecked-byte Codes/RESET_CACHES)))
        (long
          (java.lang.Byte/valueOf
            (byte (.get ^java.nio.ByteBuffer bbuf (int (.position ^java.nio.Buffer bbuf)))))))))
  (reset-meta!
    #'resets-caches?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bbuf {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'resets-caches?
      :ns
      *ns*))
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
  (reset-meta!
    #'create-tail
    (assoc
      {:arglists (clojure.core/list ['txes 'bufs]), :column (int 1)}
      :name
      'create-tail
      :ns
      *ns*))
  (defn empty-tail ([] (create-tail [] [])))
  (reset-meta!
    #'empty-tail
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'empty-tail :ns *ns*))
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
  (reset-meta!
    #'load-tail
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bbuf {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'load-tail
      :ns
      *ns*))
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
  (reset-meta!
    #'fressianed-dir
    (assoc {:arglists (clojure.core/list ['val]), :column (int 1)} :name 'fressianed-dir :ns *ns*))
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
  (reset-meta!
    #'fressianed-leaf
    (assoc
      {:arglists (clojure.core/list ['val]), :column (int 1)}
      :name
      'fressianed-leaf
      :ns
      *ns*))
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
      (let [iter__6373__auto__ (fn iter__16304
                                 ([s__16305]
                                   (lazy-seq
                                     (loop [s__16305 s__16305]
                                       (let [temp__5804__auto__ (seq s__16305)]
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
                                                                        iter__16306
                                                                        ([s__16307]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__16307 s__16307
                                                                               temp__5804__auto__
                                                                               (seq s__16307)]
                                                                              (when
                                                                                temp__5804__auto__
                                                                                (let 
                                                                                  [s__16307
                                                                                   temp__5804__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__16307)
                                                                                    (let 
                                                                                      [c__6371__auto__
                                                                                       (chunk-first
                                                                                         s__16307)
                                                                                       size__6372__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6371__auto__))
                                                                                       b__16309
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6372__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__16308
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__16308
                                                                                              size__6372__auto__)
                                                                                            (let 
                                                                                              [di
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6371__auto__
                                                                                                 (int
                                                                                                   i__16308))]
                                                                                              (chunk-append
                                                                                                b__16309
                                                                                                (common/getx
                                                                                                  lookup
                                                                                                  (.-uuid
                                                                                                    (nth
                                                                                                      dir
                                                                                                      (int
                                                                                                        ^java.lang.Number di)))))
                                                                                              (recur
                                                                                                (inc
                                                                                                  i__16308)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__16309)
                                                                                          (^clojure.lang.IFn iter__16306
                                                                                            (chunk-rest
                                                                                              s__16307)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__16309)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [di
                                                                                       (first
                                                                                         s__16307)]
                                                                                      (cons
                                                                                        (common/getx
                                                                                          lookup
                                                                                          (.-uuid
                                                                                            (nth
                                                                                              dir
                                                                                              (int
                                                                                                ^java.lang.Number di))))
                                                                                        (^clojure.lang.IFn iter__16306
                                                                                          (rest
                                                                                            s__16307)))))))))))
                                                 fs__6370__auto__ (seq
                                                                    (^clojure.lang.IFn iterys__6369__auto__
                                                                      (range
                                                                        (long didx)
                                                                        (java.lang.Integer/valueOf
                                                                          (int (count dir))))))]
                                             (if fs__6370__auto__
                                               (concat
                                                 fs__6370__auto__
                                                 (^clojure.lang.IFn iter__16304 (rest s__16305)))
                                               (recur (rest s__16305))))))))))]
        (^clojure.lang.IFn iter__6373__auto__
          (range (long ridx) (java.lang.Integer/valueOf (int (count root_val)))))))
    (log-dir-seq
      [this]
      (let [iter__6373__auto__ (fn iter__16291
                                 ([s__16292]
                                   (lazy-seq
                                     (let [s__16292 s__16292 temp__5804__auto__ (seq s__16292)]
                                       (when temp__5804__auto__
                                         (let [s__16292 temp__5804__auto__]
                                           (if (chunked-seq? s__16292)
                                             (let [c__6371__auto__ (chunk-first s__16292)
                                                   size__6372__auto__ (int (count c__6371__auto__))
                                                   b__16294 (chunk-buffer
                                                              (java.lang.Integer/valueOf
                                                                (int size__6372__auto__)))]
                                               (if (loop [i__16293 (int 0)]
                                                     (if (< i__16293 size__6372__auto__)
                                                       (let [ri
                                                             (.nth
                                                               ^clojure.lang.Indexed c__6371__auto__
                                                               (int i__16293))]
                                                         (chunk-append
                                                           b__16294
                                                           (common/getx
                                                             lookup
                                                             (:uuid
                                                               (nth
                                                                 root_val
                                                                 (int ^java.lang.Number ri)))))
                                                         (recur (inc i__16293)))
                                                       true))
                                                 (chunk-cons
                                                   (chunk b__16294)
                                                   (^clojure.lang.IFn iter__16291
                                                     (chunk-rest s__16292)))
                                                 (chunk-cons (chunk b__16294) nil)))
                                             (let [ri (first s__16292)]
                                               (cons
                                                 (common/getx
                                                   lookup
                                                   (:uuid
                                                     (nth root_val (int ^java.lang.Number ri))))
                                                 (^clojure.lang.IFn iter__16291
                                                   (rest s__16292)))))))))))]
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
  (reset-meta!
    #'->LogTxIter
    (assoc
      {:arglists (clojure.core/list ['lookup 'root-val 'tail 'ridx 'dir 'didx 'seg 'sidx]),
       :column (int 1)}
      :name
      '->LogTxIter
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.log" "->LogImpl") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.log" "map->LogImpl") {:declared true, :column (int 1)})
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
            log (let [m_16352 {:event :log/add-next, :txids ids, :firstT (:t (first txes))}
                      ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.log")]
                                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                          (.debug
                                            ^org.slf4j.Logger logger
                                            (logger/process (assoc m_16352 :phase :begin))))
                                        nil)
                      start__8553__auto__ (java.lang.System/nanoTime)
                      result__8554__auto__ (try
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
                                               t__8555__auto__
                                               {:threw t__8555__auto__}))
                      elapsed_16353 (- (java.lang.System/nanoTime) start__8553__auto__)
                      msec_16354 (logger/format-as-msec (long elapsed_16353))]
                  (monitor/add-stat :LogWriteMsec msec_16354)
                  (let [endmsg__8556__auto__ (merge
                                               (assoc m_16352 :msec msec_16354 :phase :end)
                                               (when (:threw result__8554__auto__)
                                                 {:threw (class (:threw result__8554__auto__))}))
                        logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                      (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                    nil)
                  (if (contains? result__8554__auto__ :returned)
                    (:returned result__8554__auto__)
                    (do (throw (:threw result__8554__auto__)) nil)))]
        (tx/log-completion! ids)
        log))
    (val-keys
      [this]
      (tree-seq
        (fn fn__16346 ([k] (instance? datomic.log.LogDir (first (get olookup k)))))
        (fn fn__16348
          ([k] (map (fn fn__16349 ([ld] (cluster/uuid->val-key (:uuid ld)))) (get olookup k))))
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
  (reset-meta!
    #'->LogImpl
    (assoc
      {:arglists (clojure.core/list ['olookup 'desc 'tail]), :column (int 1)}
      :name
      '->LogImpl
      :ns
      *ns*))
  (defn map->LogImpl
    ([m__7972__auto__]
      (LogImpl/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->LogImpl
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->LogImpl
      :ns
      *ns*))
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
  (reset-meta!
    #'create-log-impl
    (assoc
      {:arglists (clojure.core/list ['olookup 'desc 'tail]), :column (int 1)}
      :name
      'create-log-impl
      :ns
      *ns*))
  (defn write-new-log
    ([cs]
      (let [vec__16381 (repeatedly (fn fn__16384 ([] (common/rand-uuid))))
            new_root_id (nth vec__16381 (int 0) nil)
            new_tail_id (nth vec__16381 (int 1) nil)
            root_val_result (deref
                              (zip-and-create
                                cs
                                new_root_id
                                (fressianed-dir [(create-entry 0 new_tail_id)])
                                :root))
            tail_dir_result (deref (zip-and-create cs new_tail_id (fressianed-dir []) :dir))
            tail (empty-tail)
            root_id (cluster/uuid->val-key new_root_id)
            proposed_desc {:rev 0, :etag nil, :d/r root_id, :d/l 3}
            temp__5804__auto__ (write-tail-descriptor cs proposed_desc BEGIN_OPEN_LIST)]
        (when temp__5804__auto__ (let [desc temp__5804__auto__] [desc tail])))))
  (reset-meta!
    #'write-new-log
    (assoc {:arglists (clojure.core/list ['cs]), :column (int 1)} :name 'write-new-log :ns *ns*))
  (defn create-new-log
    ([cs olookup]
      (let [temp__5804__auto__ (write-new-log cs)]
        (when temp__5804__auto__
          (let [vec__16388 temp__5804__auto__
                desc (nth vec__16388 (int 0) nil)
                tail (nth vec__16388 (int 1) nil)]
            (create-log-impl olookup desc tail))))))
  (reset-meta!
    #'create-new-log
    (assoc
      {:arglists (clojure.core/list ['cs 'olookup]), :column (int 1)}
      :name
      'create-new-log
      :ns
      *ns*))
  (defn seek-tx ([provider t] (seek-tx-impl provider t)))
  (reset-meta!
    #'seek-tx
    (assoc
      {:arglists (clojure.core/list (.withMeta ['provider 't] {:tag 'datomic.iter.Iter})),
       :column (int 1)}
      :name
      'seek-tx
      :ns
      *ns*))
  (defn find-log
    ([cs olookup]
      (let [temp__5804__auto__ (read-tail-descriptor cs)]
        (when temp__5804__auto__
          (let [vec__16394 temp__5804__auto__
                desc (nth vec__16394 (int 0) nil)
                buf (nth vec__16394 (int 1) nil)
                tail (load-tail buf)]
            (create-log-impl olookup desc tail))))))
  (reset-meta!
    #'find-log
    (assoc
      {:arglists (clojure.core/list ['cs 'olookup]), :column (int 1)}
      :name
      'find-log
      :ns
      *ns*))
  (defn log-tree
    ([olookup root_id] (create-log-impl olookup {:rev 0, :d/l 3, :d/r root_id} (empty-tail))))
  (reset-meta!
    #'log-tree
    (assoc
      {:arglists (clojure.core/list ['olookup 'root-id]), :column (int 1)}
      :name
      'log-tree
      :ns
      *ns*))
  (defn catchup-tx
    ([p__16400 tx]
      (let [map__16401 p__16400
            map__16401 (if (seq? map__16401)
                         (if (next map__16401)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16401))
                           (if (seq map__16401) (first map__16401) {}))
                         map__16401)
            db (get map__16401 :db)
            size (get map__16401 :size)
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
       :column (int 1)}
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
                  (fn fn__16405
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
            map__16404 (reduce catchup-tx {:db db, :size 0} log_txes)
            map__16404 (if (seq? map__16404)
                         (if (next map__16404)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16404))
                           (if (seq map__16404) (first map__16404) {}))
                         map__16404)
            result map__16404
            db (get map__16404 :db)
            size (get map__16404 :size)
            elapsed (- (java.lang.System/currentTimeMillis) start)
            db (if catchup_ft
                 (let [m_16407 {:event :log/catchup-fulltext}
                       ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                      "datomic.log")]
                                         (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                           (.info
                                             ^org.slf4j.Logger logger
                                             (logger/process (assoc m_16407 :phase :begin))))
                                         nil)
                       start__8553__auto__ (java.lang.System/nanoTime)
                       result__8554__auto__ (try
                                              {:returned
                                               (db/add-fulltext db (mapcat :data log_txes))}
                                              (catch
                                                java.lang.Throwable
                                                t__8555__auto__
                                                {:threw t__8555__auto__}))
                       elapsed_16408 (- (java.lang.System/nanoTime) start__8553__auto__)
                       msec_16409 (logger/format-as-msec (long elapsed_16408))]
                   (let [endmsg__8556__auto__ (merge
                                                (assoc m_16407 :msec msec_16409 :phase :end)
                                                (when (:threw result__8554__auto__)
                                                  {:threw (class (:threw result__8554__auto__))}))
                         logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                     (when (.isInfoEnabled ^org.slf4j.Logger logger)
                       (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                     nil)
                   (if (contains? result__8554__auto__ :returned)
                     (:returned result__8554__auto__)
                     (do (throw (:threw result__8554__auto__)) nil)))
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
  (reset-meta!
    #'catchup
    (assoc
      {:arglists
       (clojure.core/list ['db 'log] [(.withMeta 'db {:tag 'Database}) 'log 'catchup-ft]),
       :column (int 1)}
      :name
      'catchup
      :ns
      *ns*))
  (defn ensure-index-and-log
    ([cluster olookup db_id]
      (let [m_16417 {:event :transactor/ensure-index-and-log, :db-id db_id}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_16417 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
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
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_16418 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_16419 (logger/format-as-msec (long elapsed_16418))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_16417 :msec msec_16419 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.log")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'ensure-index-and-log
    (assoc
      {:arglists (clojure.core/list ['cluster 'olookup 'db-id]), :column (int 1)}
      :name
      'ensure-index-and-log
      :ns
      *ns*))
  (defn excise-ts
    ([log xpreds]
      (reduce
        (fn fn__16431
          ([s p]
            (into
              s
              (map
                (fn fn__16432 ([p1__16430#] (long (.getT ^datomic.impl.db.IDatum p1__16430#))))
                (x/datoms p)))))
        #{}
        xpreds)))
  (reset-meta!
    #'excise-ts
    (assoc
      {:arglists (clojure.core/list ['log 'xpreds]), :column (int 1)}
      :name
      'excise-ts
      :ns
      *ns*))
  (defn excise-dir-map
    ([log ts]
      (reduce
        (fn fn__16438
          ([m p__16437]
            (let [vec__16439 p__16437
                  dirid (nth vec__16439 (int 0) nil)
                  segid (nth vec__16439 (int 1) nil)]
              (if (contains? (get m dirid) segid)
                m
                (assoc m dirid (conj (get m dirid #{}) segid))))))
        {}
        (map (fn fn__16443 ([p1__16436#] (seek-seg-path log p1__16436#))) ts))))
  (reset-meta!
    #'excise-dir-map
    (assoc
      {:arglists (clojure.core/list ['log 'ts]), :column (int 1)}
      :name
      'excise-dir-map
      :ns
      *ns*))
  (defn write-excised-log
    ([cs lookup xpreds ts dir_map]
      (reduce
        (fn fn__16448
          ([m p__16447]
            (let [vec__16449 p__16447
                  dirid (nth vec__16449 (int 0) nil)
                  segids (nth vec__16449 (int 1) nil)
                  excise? (fn excise_QMARK_
                            ([d]
                              (some
                                (fn fn__16456 ([p1__16446#] (x/remove? p1__16446# d)))
                                xpreds)))
                  dir (common/getx lookup dirid)
                  vec__16452 (reduce
                               (fn fn__16460
                                 ([p__16459 direntry]
                                   (let [vec__16461 p__16459
                                         m (nth vec__16461 (int 0) nil)
                                         newdir (nth vec__16461 (int 1) nil)
                                         t (.-t ^datomic.log.LogDir direntry)
                                         segid (.-uuid ^datomic.log.LogDir direntry)]
                                     (if (contains? segids segid)
                                       (let [seg (common/getx lookup segid)
                                             newsid (common/rand-uuid)
                                             newseg (mapv
                                                      (fn fn__16465
                                                        ([p__16464]
                                                          (let [map__16466 p__16464
                                                                map__16466
                                                                (if
                                                                  (seq? map__16466)
                                                                  (if
                                                                    (next map__16466)
                                                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                      (to-array map__16466))
                                                                    (if
                                                                      (seq map__16466)
                                                                      (first map__16466)
                                                                      {}))
                                                                  map__16466)
                                                                tx map__16466
                                                                data (get map__16466 :data)
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
                  m (nth vec__16452 (int 0) nil)
                  newdir (nth vec__16452 (int 1) nil)
                  newdid (common/rand-uuid)]
              (write-excise-val cs newdid (fressianed-dir newdir))
              (assoc m dirid newdid))))
        {}
        dir_map)))
  (reset-meta!
    #'write-excised-log
    (assoc
      {:arglists (clojure.core/list ['cs 'lookup 'xpreds 'ts 'dir-map]), :column (int 1)}
      :name
      'write-excised-log
      :ns
      *ns*))
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
                   (fn fn__16472 ([p1__16471#] (monitor/add-stat :ExciseIOQueueCount p1__16471#))))
                 cs)
            replacements (write-excised-log cs lookup xpreds ts dir_map)]
        (when pario
          (common/bounded-deref (cluster/finish-writer cs) cluster/BOUNDING_TIMEOUT_MSEC))
        {:dir-map dir_map, :replacements replacements})))
  (reset-meta!
    #'excise
    (assoc
      {:arglists (clojure.core/list ['cs 'lookup 'log 'xpreds]), :column (int 1)}
      :name
      'excise
      :ns
      *ns*))
  (defn excise-root
    ([cs lookup current_root_id p__16477]
      (let [map__16478 p__16477
            map__16478 (if (seq? map__16478)
                         (if (next map__16478)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16478))
                           (if (seq map__16478) (first map__16478) {}))
                         map__16478)
            dir_map (get map__16478 :dir-map)
            replacements (get map__16478 :replacements)
            log (log-tree lookup current_root_id)
            root (get-root-val log)
            dirs (into
                   #{}
                   (map
                     (fn fn__16479 ([p1__16475#] (.-uuid ^datomic.log.LogDir p1__16475#)))
                     root))
            patch_dir (fn patch_dir
                        ([replacements dir]
                          (reduce
                            (fn fn__16482
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
                   (fn fn__16486 ([p1__16476#] (monitor/add-stat :ExciseIOQueueCount p1__16476#))))
                 cs)
            changed_dirs (remove dirs (keys dir_map))
            replacements (reduce
                           (fn fn__16488
                             ([m did]
                               (let [segids (^clojure.lang.IFn dir_map did)
                                     newdid (common/rand-uuid)
                                     vec__16489 (seek-seg-path
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
                                     dirid (nth vec__16489 (int 0) nil)
                                     _ (nth vec__16489 (int 1) nil)]
                                 (if (not (contains? m dirid))
                                   (let [dir (common/getx lookup dirid)
                                         newdid (common/rand-uuid)
                                         newdir (^clojure.lang.IFn patch_dir replacements dir)]
                                     (let [logger (org.slf4j.LoggerFactory/getLogger
                                                    "datomic.log")]
                                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                         (.debug
                                           ^org.slf4j.Logger logger
                                           (logger/process
                                             {:event :log/excise-replace-rightmost,
                                              :oldid dirid,
                                              :newid newdid})))
                                       nil)
                                     (write-excise-val cs newdid (fressianed-dir newdir))
                                     (assoc m dirid newdid))
                                   m))))
                           replacements
                           changed_dirs)
            changed_garbage (map replacements changed_dirs)
            newroot (^clojure.lang.IFn patch_dir replacements root)
            newrid (common/rand-uuid)]
        (write-excise-val cs newrid (fressianed-dir newroot))
        (when pario
          (common/bounded-deref (cluster/finish-writer cs) cluster/BOUNDING_TIMEOUT_MSEC))
        {:root-id (cluster/uuid->val-key newrid),
         :garbage-ids
         (map
           cluster/uuid->val-key
           (concat changed_garbage (cons (get-root-id log) (keys replacements))))})))
  (reset-meta!
    #'excise-root
    (assoc
      {:arglists
       (clojure.core/list ['cs 'lookup 'current-root-id {:keys ['dir-map 'replacements]}]),
       :column (int 1)}
      :name
      'excise-root
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.log" "->LogTailValue") {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.log" "map->LogTailValue")
    {:declared true, :column (int 1)})
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
  (reset-meta!
    #'->LogTailValue
    (assoc
      {:arglists (clojure.core/list ['txes]), :column (int 1)}
      :name
      '->LogTailValue
      :ns
      *ns*))
  (defn map->LogTailValue
    ([m__7972__auto__]
      (LogTailValue/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->LogTailValue
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->LogTailValue
      :ns
      *ns*))
  (extend
    datomic.db.MemLog
    LogSeek
    {:seek-tx-impl
     (fn fn__16515
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
                  (iter/take-while (fn fn__16519 ([p1__16517#] (< (:t p1__16517#) end))) ret)
                  ret))))))))
  (reset-meta!
    #'tx-range
    (assoc
      {:arglists (clojure.core/list ['log (.withMeta 'db {:tag 'IDb}) 'start 'end]),
       :column (int 1)}
      :name
      'tx-range
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.log" "->LogValue") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.log" "map->LogValue") {:declared true, :column (int 1)})
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
  (reset-meta!
    #'->LogValue
    (assoc
      {:arglists (clojure.core/list ['db 'olookup 'root-id 'tail]), :column (int 1)}
      :name
      '->LogValue
      :ns
      *ns*))
  (defn map->LogValue
    ([m__7972__auto__]
      (LogValue/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->LogValue
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->LogValue
      :ns
      *ns*))
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
  (reset-meta!
    #'segmented-basis-t
    (assoc
      {:arglists (clojure.core/list ['log-value]), :column (int 1)}
      :name
      'segmented-basis-t
      :ns
      *ns*))
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
  (reset-meta!
    #'create-log-val
    (assoc
      {:arglists (clojure.core/list (.withMeta ['cs 'olookup 'db] {:pre ['cs 'olookup 'db]})),
       :column (int 1)}
      :name
      'create-log-val
      :ns
      *ns*))
  (defn max-t
    ([datoms]
      (reduce (fn fn__16557 ([n datom] (max n (db/eid->eidx (long (:tx datom)))))) 0 datoms)))
  (reset-meta!
    #'max-t
    (assoc {:arglists (clojure.core/list ['datoms]), :column (int 1)} :name 'max-t :ns *ns*))
  (defn max-eidx
    ([datoms]
      (reduce
        (fn fn__16560
          ([n datom]
            (max (max n (db/eid->eidx (long (:e datom)))) (db/eid->eidx (long (:tx datom))))))
        0
        datoms)))
  (reset-meta!
    #'max-eidx
    (assoc {:arglists (clojure.core/list ['datoms]), :column (int 1)} :name 'max-eidx :ns *ns*))
  (defn last-tree-tx
    ([olookup root_id]
      (let [root (common/getx olookup root_id)
            dir_id (:uuid (last-by-nth root))
            dir (when dir_id (common/getx olookup dir_id))
            leaf_id (:uuid (last-by-nth dir))]
        (last-by-nth (when leaf_id (common/getx olookup leaf_id)))))
    ([p__16563]
      (let [map__16564 p__16563
            map__16564 (if (seq? map__16564)
                         (if (next map__16564)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16564))
                           (if (seq map__16564) (first map__16564) {}))
                         map__16564)
            log map__16564
            olookup (get map__16564 :olookup)]
        (last-tree-tx olookup (or (:root-id log) (get-root-id log))))))
  (reset-meta!
    #'last-tree-tx
    (assoc
      {:arglists (clojure.core/list [{:keys ['olookup], :as 'log}] ['olookup 'root-id]),
       :column (int 1)}
      :name
      'last-tree-tx
      :ns
      *ns*))
  (defn root-id ([cs] (:d/r (deref (cluster/get-pod-meta cs (tail-pod-key cs))))))
  (reset-meta!
    #'root-id
    (assoc {:arglists (clojure.core/list ['cs]), :column (int 1)} :name 'root-id :ns *ns*))
  (defn partition-by-weight
    ([weigh target]
      (fn fn__16568
        ([rf]
          (let [weight_ref (volatile! 0) part_ref (volatile! [])]
            (fn fn__16569
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
  (reset-meta!
    #'partition-by-weight
    (assoc
      {:arglists (clojure.core/list ['weigh 'target]), :column (int 1)}
      :name
      'partition-by-weight
      :ns
      *ns*))
  (defn combine-last-if
    ([pred combine]
      (fn fn__16574
        ([rf]
          (let [tail_ref (volatile! nil)]
            (fn fn__16575
              ([] (^clojure.lang.IFn rf))
              ([result]
                (let [vec__16576 (deref tail_ref)
                      p1 (nth vec__16576 (int 0) nil)
                      p2 (nth vec__16576 (int 1) nil)]
                  (if p1
                    (if (^clojure.lang.IFn pred p2)
                      (^clojure.lang.IFn rf
                        (^clojure.lang.IFn rf result (^clojure.lang.IFn combine p1 p2)))
                      (^clojure.lang.IFn rf
                        (^clojure.lang.IFn rf (^clojure.lang.IFn rf result p1) p2)))
                    (cond-> result p2 (^clojure.lang.IFn rf p2) :finish (^clojure.lang.IFn rf)))))
              ([result input]
                (let [vec__16580 (deref tail_ref)
                      p1 (nth vec__16580 (int 0) nil)
                      p2 (nth vec__16580 (int 1) nil)]
                  (vreset! tail_ref [p2 input])
                  (if p1 (^clojure.lang.IFn rf result p1) result)))))))))
  (reset-meta!
    #'combine-last-if
    (assoc
      {:arglists (clojure.core/list ['pred 'combine]), :column (int 1)}
      :name
      'combine-last-if
      :ns
      *ns*))
  (defn create-leaves*
    ([target_size ftxes]
      (let [weigh (comp io/remaining second)]
        (sequence
          (comp
            (partition-by-weight weigh target_size)
            (combine-last-if
              (fn fn__16586 ([ftxes] (< (apply + (map weigh ftxes)) (quot target_size 2))))
              into)
            (map
              (fn fn__16588
                ([ftxes]
                  [(ffirst ftxes)
                   (io/unchunk
                     (concat [BEGIN_CLOSED_LIST] (map second ftxes) [END_COLLECTION]))]))))
          ftxes))))
  (reset-meta!
    #'create-leaves*
    (assoc
      {:arglists (clojure.core/list ['target-size 'ftxes]), :column (int 1)}
      :name
      'create-leaves*
      :ns
      *ns*))
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
  (reset-meta!
    #'create-leaves
    (assoc
      {:arglists (clojure.core/list ['target-size 'tail]), :column (int 1)}
      :name
      'create-leaves
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.log" "coalesce-threshold*") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "coalesce-threshold*")
    (delay (config/property "datomic.logCoalesceThreshold")))
  (.setMeta (clojure.lang.RT/var "datomic.log" "dir-size-limit*") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.log" "dir-size-limit*")
    (delay (config/property "datomic.logDirSizeLimit")))
  (defn select-dirs-to-merge
    ([olookup root]
      (when (> (count root) (long (deref coalesce-threshold*)))
        (let [sample_indexes (take
                               2
                               (distinct
                                 (repeatedly
                                   (fn fn__16600 ([] (rand-int (long (- (count root) 3))))))))
              sample (fn sample
                       ([i]
                         (let [vec__16603 [(:uuid (nth root (int ^java.lang.Number i)))
                                           (:uuid (nth root (int (inc i))))]
                               u0 (nth vec__16603 (int 0) nil)
                               u1 (nth vec__16603 (int 1) nil)
                               d0 (vec (cache/getx-uncached olookup u0))
                               d1 (vec (cache/getx-uncached olookup u1))
                               t (+ (count d0) (count d1))]
                           {:i i, :total (long t), :u0 u0, :u1 u1, :d0 d0, :d1 d1})))
              vec__16596 (map sample sample_indexes)
              s0 (nth vec__16596 (int 0) nil)
              s1 (nth vec__16596 (int 1) nil)
              map__16599 (min-key :total s0 s1)
              map__16599 (if (seq? map__16599)
                           (if (next map__16599)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__16599))
                             (if (seq map__16599) (first map__16599) {}))
                           map__16599)
              data map__16599
              total (get map__16599 :total)]
          (when (< total (long (deref dir-size-limit*))) data)))))
  (reset-meta!
    #'select-dirs-to-merge
    (assoc
      {:private true, :arglists (clojure.core/list ['olookup 'root]), :column (int 1)}
      :name
      'select-dirs-to-merge
      :ns
      *ns*))
  (defn coalesce
    ([cs root p__16608]
      (let [map__16609 p__16608
            map__16609 (if (seq? map__16609)
                         (if (next map__16609)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16609))
                           (if (seq map__16609) (first map__16609) {}))
                         map__16609)
            u0 (get map__16609 :u0)
            u1 (get map__16609 :u1)
            d0 (get map__16609 :d0)
            d1 (get map__16609 :d1)
            i (get map__16609 :i)
            new_dir_id (common/rand-uuid)
            new_dir_entry (datomic.log.LogDir. (long (get-in d0 [0 :t])) new_dir_id)
            new_dir* (zip-and-create cs new_dir_id (fressianed-dir (into d0 d1)) :dir)
            garbage (mapv cluster/uuid->val-key [u0 u1])
            root (vec root)]
        [(into
           (assoc (subvec root 0 (long (inc (long ^java.lang.Number i)))) i new_dir_entry)
           (subvec root (long (+ 2 (long ^java.lang.Number i)))))
         [new_dir*]
         garbage])))
  (reset-meta!
    #'coalesce
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['cs 'root {:keys ['u0 'u1 'd0 'd1 (.withMeta 'i {:tag 'long})]}]),
       :column (int 1)}
      :name
      'coalesce
      :ns
      *ns*))
  (defn coalesce-dirs
    ([cs olookup root]
      (let [start (java.lang.System/nanoTime)
            rv (let [temp__5802__auto__ (select-dirs-to-merge olookup root)]
                 (if temp__5802__auto__
                   (let [data temp__5802__auto__] (coalesce cs root data))
                   [root [] []]))
            elapsed (- (java.lang.System/nanoTime) start)]
        (monitor/add-stat :LogCoalesceDirsNsec (long elapsed))
        rv)))
  (reset-meta!
    #'coalesce-dirs
    (assoc
      {:private true, :arglists (clojure.core/list ['cs 'olookup 'root]), :column (int 1)}
      :name
      'coalesce-dirs
      :ns
      *ns*))
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
            vec__16614 (repeatedly common/rand-uuid)
            new_root_id (nth vec__16614 (int 0) nil)
            new_dir_id (nth vec__16614 (int 1) nil)
            new_tail_dir (into (if new_tail_dir? [] tail_dir) (map create-entry leaf_ts leaf_ids))
            new_root_entry (if new_tail_dir?
                             (create-entry new_seg_t new_dir_id)
                             (create-entry (common/getx (peek root) :t) new_dir_id))
            new_root (if new_tail_dir?
                       (conj root new_root_entry)
                       (assoc root (long (dec (count root))) new_root_entry))
            vec__16617 (coalesce-dirs cs olookup new_root)
            new_root (nth vec__16617 (int 0) nil)
            coalesce_in_flight (nth vec__16617 (int 1) nil)
            coalesce_garbage (nth vec__16617 (int 2) nil)
            garbage_ids (cond->
                          (conj coalesce_garbage root_id)
                          (not new_tail_dir?)
                          (conj (cluster/uuid->val-key old_tail_dir_uuid)))
            create (fn create ([uuid val buf_type] (zip-and-create cs uuid val buf_type)))
            vals (conj (vec leaf_bufs) (fressianed-dir new_tail_dir) (fressianed-dir new_root))
            buf_types (into
                        (vec (repeat (java.lang.Integer/valueOf (int (count leaf_bufs))) :leaf))
                        [:dir :root])
            ids (conj (vec leaf_ids) new_dir_id new_root_id)
            results (into coalesce_in_flight (map create ids vals buf_types))]
        (if (every? (fn fn__16623 ([p1__16613#] (= p1__16613# :created))) (map deref results))
          {:root-id (cluster/uuid->val-key new_root_id),
           :dir-id (cluster/uuid->val-key new_dir_id),
           :leaf-ts leaf_ts,
           :garbage-ids garbage_ids}
          (do (throw (java.lang.Error. "Write failure extending log tree")) nil)))))
  (reset-meta!
    #'extend-tree
    (assoc
      {:arglists (clojure.core/list ['cs 'olookup 'root-id 'target-dir-count 'leaf-segs]),
       :column (int 1)}
      :name
      'extend-tree
      :ns
      *ns*))
  (defn segment
    ([cluster olookup]
      (let [map__16626 (find-log cluster olookup)
            map__16626 (if (seq? map__16626)
                         (if (next map__16626)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16626))
                           (if (seq map__16626) (first map__16626) {}))
                         map__16626)
            log map__16626
            desc (get map__16626 :desc)
            tail (get map__16626 :tail)]
        (if (tail-empty? tail)
          log
          (let [map__16627 (extend-tree
                             cluster
                             olookup
                             (:d/r desc)
                             dir-threshold
                             (create-leaves segment-threshold tail))
                map__16627 (if (seq? map__16627)
                             (if (next map__16627)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__16627))
                               (if (seq map__16627) (first map__16627) {}))
                             map__16627)
                root_id (get map__16627 :root-id)]
            (adopt-root log cluster root_id (last (map :t (:txes tail)))))))))
  (reset-meta!
    #'segment
    (assoc
      {:arglists (clojure.core/list ['cluster 'olookup]), :column (int 1)}
      :name
      'segment
      :ns
      *ns*)))
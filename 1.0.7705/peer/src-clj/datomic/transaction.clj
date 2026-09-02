(do
  (clojure.core/in-ns 'datomic.transaction)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.transaction)
    {:doc
     "Transaction transport and reporting support. Encodes transaction requests and results, assigns message identifiers, and records processing timings as transactions move through the serialized transactor pipeline."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['compare])
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.fressian :as 'fressian]
        ['datomic.index :as 'index]
        ['datomic.cache :as 'cache]
        ['datomic.common :as 'common]
        ['datomic.monitor :as 'monitor]
        ['datomic.config :as 'config]
        ['datomic.error :as 'error]
        ['datomic.slf4j :as 'logger]
        'datomic.validators)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)
      (clojure.core/import 'java.io.InputStream)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'datomic.db.Datum)
      (clojure.core/import 'datomic.db.DbId)
      (clojure.core/import 'datomic.db.IProcess)
      (clojure.core/import 'datomic.db.ProcessInpoint)
      (clojure.core/import 'datomic.db.ProcessCollector)
      (clojure.core/import 'datomic.db.IDatumImpl)
      (clojure.core/import 'datomic.impl.db.IDatum)))
  (when-not (.equals 'datomic.transaction 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.transaction))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['compare])
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.fressian :as 'fressian]
          ['datomic.index :as 'index]
          ['datomic.cache :as 'cache]
          ['datomic.common :as 'common]
          ['datomic.monitor :as 'monitor]
          ['datomic.config :as 'config]
          ['datomic.error :as 'error]
          ['datomic.slf4j :as 'logger]
          'datomic.validators)
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'org.fressian.handlers.WriteHandler)
        (clojure.core/import 'java.io.InputStream)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'datomic.db.Datum)
        (clojure.core/import 'datomic.db.DbId)
        (clojure.core/import 'datomic.db.IProcess)
        (clojure.core/import 'datomic.db.ProcessInpoint)
        (clojure.core/import 'datomic.db.ProcessCollector)
        (clojure.core/import 'datomic.db.IDatumImpl)
        (clojure.core/import 'datomic.impl.db.IDatum))))
  (set! *warn-on-reflection* true)
  (defn peer-message-type
    "Classifies a message received by a peer as a transaction result, error, or index notification."
    ([msg] (or (:type msg) (if (:id msg) (if (:data msg) :tx :error) :index))))
  (reset-meta!
    #'peer-message-type
    (assoc
      {:arglists (clojure.core/list ['msg]),
       :doc
       "Classifies a message received by a peer as a transaction result, error, or index notification.",
       :column (int 1)}
      :name
      'peer-message-type
      :ns
      *ns*))
  (defn submit-address
    "Returns the transport address used to submit transactions for a database."
    ([db-name] (str db-name ".tx-submit")))
  (reset-meta!
    #'submit-address
    (assoc
      {:arglists (clojure.core/list ['db-name]),
       :doc "Returns the transport address used to submit transactions for a database.",
       :column (int 1)}
      :name
      'submit-address
      :ns
      *ns*))
  (defn push-address
    "Returns the transport address on which transaction results are published for a database."
    ([db-name] (str db-name ".tx-result")))
  (reset-meta!
    #'push-address
    (assoc
      {:arglists (clojure.core/list ['db-name]),
       :doc
       "Returns the transport address on which transaction results are published for a database.",
       :column (int 1)}
      :name
      'push-address
      :ns
      *ns*))
  (defn write-handlers
    ([cache]
      (merge
        fressian/user-write-handlers
        index/common-write-handlers
        {datomic.db.DbId
         {"dbid"
          (reify
            org.fressian.handlers.WriteHandler
            (^void write
              [this ^org.fressian.Writer w o]
              (do
                (let [dbid o]
                  (.writeTag ^org.fressian.Writer w "dbid" (int 2))
                  (.writeObject
                    ^org.fressian.Writer w
                    (:part dbid)
                    (boolean (.booleanValue ^java.lang.Boolean cache)))
                  (.writeObject
                    ^org.fressian.Writer w
                    (:idx dbid)
                    (boolean (.booleanValue ^java.lang.Boolean cache))))
                nil)))},
         datomic.db.Datum
         {"datum"
          (reify
            org.fressian.handlers.WriteHandler
            (^void write
              [this ^org.fressian.Writer w o]
              (do
                (let [datum o]
                  (.writeTag ^org.fressian.Writer w "datum" (int 6))
                  (.writeBoolean
                    ^org.fressian.Writer w
                    (boolean (.isAssertion ^datomic.impl.db.IDatum datum)))
                  (.writeObject
                    ^org.fressian.Writer w
                    (java.lang.Integer/valueOf (int (.getP ^datomic.impl.db.IDatum datum)))
                    (boolean (.booleanValue ^java.lang.Boolean cache)))
                  (.writeObject
                    ^org.fressian.Writer w
                    (long (.eidx ^datomic.db.IDatumImpl datum))
                    (boolean (.booleanValue ^java.lang.Boolean cache)))
                  (.writeInt ^org.fressian.Writer w (long (.getA ^datomic.impl.db.IDatum datum)))
                  (.writeObject ^org.fressian.Writer w (.getV ^datomic.impl.db.IDatum datum))
                  (.writeObject
                    ^org.fressian.Writer w
                    (long (.getT ^datomic.impl.db.IDatum datum))
                    (boolean (.booleanValue ^java.lang.Boolean cache))))
                nil)))}}))
    ([] (write-handlers true)))
  (reset-meta!
    #'write-handlers
    (assoc
      {:arglists (clojure.core/list [] ['cache]), :column (int 1)}
      :name
      'write-handlers
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.transaction" "read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.transaction" "read-handlers")
    (merge
      fressian/user-read-handlers
      index/index-read-handlers
      {"dbid"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (db/->DbId
             (.readObject ^org.fressian.Reader rdr)
             (.readObject ^org.fressian.Reader rdr)))),
       "datum"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (let [assert? (.readBoolean ^org.fressian.Reader rdr)
                 part (.readInt ^org.fressian.Reader rdr)
                 eidx (.readInt ^org.fressian.Reader rdr)
                 eid (db/make-eid part eidx)
                 attrid (.readInt ^org.fressian.Reader rdr)
                 v (common/ensure-vector (.readObject ^org.fressian.Reader rdr))
                 t (.readInt ^org.fressian.Reader rdr)]
             (if assert?
               (db/asserting-datum eid attrid v t)
               (db/retracting-datum eid attrid v t)))))}))
  (.setMeta
    (clojure.lang.RT/var "datomic.transaction" "writer")
    {:tag org.fressian.Writer,
     :arglists (clojure.core/list ['out] ['out 'cache]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.transaction" "writer")
    (fn writer
      ([out cache] (fressian/create-writer out (write-handlers cache)))
      ([out] (writer out true))))
  (.setMeta
    (clojure.lang.RT/var "datomic.transaction" "reader")
    {:tag org.fressian.Reader,
     :arglists (clojure.core/list [(.withMeta 'in {:tag 'InputStream})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.transaction" "reader")
    (fn reader ([in] (fressian/create-reader in read-handlers))))
  (.setMeta (clojure.lang.RT/var "datomic.transaction" "log-event-map") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.transaction" "log-event-map")
    (cache/create-limited 100))
  (def loggable-keys
   [:event :txid :t :msec :apply-msec :datom-count :io-stats :tx-stats :pf-stats])
  (reset-meta! #'loggable-keys (assoc {:column (int 1)} :name 'loggable-keys :ns *ns*))
  (defn log-completion!
    ([ids]
      (let [written_at (java.lang.System/nanoTime)]
        (loop [seq_15942 (seq ids) chunk_15943 nil count_15944 0 i_15945 0]
          (if (< i_15945 count_15944)
            (let [id (.nth ^clojure.lang.Indexed chunk_15943 (int i_15945))]
              (let [temp__5804__auto__ (cache/remove log-event-map id)]
                (when temp__5804__auto__
                  (let [map__15946 temp__5804__auto__
                        map__15946 (if (seq? map__15946)
                                     (if (next map__15946)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__15946))
                                       (if (seq map__15946) (first map__15946) {}))
                                     map__15946)
                        tx_info map__15946
                        read_at (get map__15946 :read-at)
                        started_at (get map__15946 :started-at)
                        applied_at (get map__15946 :applied-at)
                        tx_nsec (let [and__5579__auto__ written_at]
                                  (if (long and__5579__auto__)
                                    (and read_at (- written_at read_at))
                                    (long and__5579__auto__)))
                        msec (let [and__5579__auto__ written_at]
                               (if (long and__5579__auto__)
                                 (and read_at (logger/format-as-msec tx_nsec))
                                 (long and__5579__auto__)))
                        apply_nsec (and applied_at started_at (- applied_at started_at))
                        apply_msec (and applied_at started_at (logger/format-as-msec apply_nsec))]
                    (when msec (monitor/add-stat :TransactionNsec tx_nsec))
                    (when apply_msec (monitor/add-stat :TransactionApplyNsec apply_nsec))
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.transaction")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process
                            (select-keys
                              (cond->
                                (merge {:event :tx/process, :txid id} tx_info)
                                msec
                                (assoc :msec msec)
                                apply_msec
                                (assoc :apply-msec apply_msec))
                              loggable-keys))))
                      nil))))
              (recur seq_15942 chunk_15943 count_15944 (inc i_15945)))
            (let [temp__5804__auto__ (seq seq_15942)]
              (when temp__5804__auto__
                (let [seq_15942 temp__5804__auto__]
                  (if (chunked-seq? seq_15942)
                    (let [c__6065__auto__ (chunk-first seq_15942)]
                      (recur
                        (chunk-rest seq_15942)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [id (first seq_15942)]
                      (let [temp__5804__auto__ (cache/remove log-event-map id)]
                        (when temp__5804__auto__
                          (let [map__15948 temp__5804__auto__
                                map__15948 (if (seq? map__15948)
                                             (if (next map__15948)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__15948))
                                               (if (seq map__15948) (first map__15948) {}))
                                             map__15948)
                                tx_info map__15948
                                read_at (get map__15948 :read-at)
                                started_at (get map__15948 :started-at)
                                applied_at (get map__15948 :applied-at)
                                tx_nsec (let [and__5579__auto__ written_at]
                                          (if (long and__5579__auto__)
                                            (and read_at (- written_at read_at))
                                            (long and__5579__auto__)))
                                msec (let [and__5579__auto__ written_at]
                                       (if (long and__5579__auto__)
                                         (and read_at (logger/format-as-msec tx_nsec))
                                         (long and__5579__auto__)))
                                apply_nsec (and applied_at started_at (- applied_at started_at))
                                apply_msec (and
                                             applied_at
                                             started_at
                                             (logger/format-as-msec apply_nsec))]
                            (when msec (monitor/add-stat :TransactionNsec tx_nsec))
                            (when apply_msec (monitor/add-stat :TransactionApplyNsec apply_nsec))
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.transaction")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process
                                    (select-keys
                                      (cond->
                                        (merge {:event :tx/process, :txid id} tx_info)
                                        msec
                                        (assoc :msec msec)
                                        apply_msec
                                        (assoc :apply-msec apply_msec))
                                      loggable-keys))))
                              nil))))
                      (recur (next seq_15942) nil 0 0)))))))))))
  (reset-meta!
    #'log-completion!
    (assoc
      {:arglists (clojure.core/list ['ids]), :column (int 1)}
      :name
      'log-completion!
      :ns
      *ns*))
  (defn add-to-log-event!
    ([id m]
      (let [tx_info (cache/get-from-cache log-event-map id {})]
        (cache/put log-event-map id (merge tx_info m)))))
  (reset-meta!
    #'add-to-log-event!
    (assoc
      {:arglists (clojure.core/list ['id 'm]), :column (int 1)}
      :name
      'add-to-log-event!
      :ns
      *ns*))
  (defn read-message
    "Reads one Fressian transaction message and records when processing of its request began."
    ([is]
      (let [read_at (java.lang.System/nanoTime)
            fin (reader is)
            msg (.readObject ^org.fressian.Reader fin)]
        (cache/put log-event-map (common/getx msg :id) {:read-at (long read_at)})
        msg)))
  (reset-meta!
    #'read-message
    (assoc
      {:arglists (clojure.core/list ['is]),
       :doc
       "Reads one Fressian transaction message and records when processing of its request began.",
       :column (int 1)}
      :name
      'read-message
      :ns
      *ns*))
  (defn create-procargs
    "Builds a transaction request with a unique request id, transaction data, and optional processing options."
    ([tx options] (cond-> {:id (common/rand-uuid), :data tx} options (assoc :options options))))
  (reset-meta!
    #'create-procargs
    (assoc
      {:arglists (clojure.core/list ['tx 'options]),
       :doc
       "Builds a transaction request with a unique request id, transaction data, and optional processing options.",
       :column (int 1)}
      :name
      'create-procargs
      :ns
      *ns*)))

(do
  (clojure.core/in-ns 'datomic.transaction)
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
    ([msg] (or (:type msg) (if (:id msg) (if (:data msg) :tx :error) :index))))
  (reset-meta!
    #'peer-message-type
    (assoc
      {:arglists (clojure.core/list ['msg]), :column (int 1)}
      :name
      'peer-message-type
      :ns
      *ns*))
  (def submit-address (fn submit_address ([db_name] (str db_name ".tx-submit"))))
  (reset-meta!
    #'submit-address
    (assoc
      {:arglists (clojure.core/list ['db-name]), :column (int 1)}
      :name
      'submit-address
      :ns
      *ns*))
  (def push-address (fn push_address ([db_name] (str db_name ".tx-result"))))
  (reset-meta!
    #'push-address
    (assoc
      {:arglists (clojure.core/list ['db-name]), :column (int 1)}
      :name
      'push-address
      :ns
      *ns*))
  (def write-handlers
   (fn write_handlers
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
     ([] (write-handlers true))))
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
        (loop [seq_17051 (seq ids) chunk_17052 nil count_17053 0 i_17054 0]
          (if (< i_17054 count_17053)
            (let [id (.nth ^clojure.lang.Indexed chunk_17052 (int i_17054))]
              (let [temp__5804__auto__ (cache/remove log-event-map id)]
                (when temp__5804__auto__
                  (let [map__17055 temp__5804__auto__
                        map__17055 (if (seq? map__17055)
                                     (if (next map__17055)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__17055))
                                       (if (seq map__17055) (first map__17055) {}))
                                     map__17055)
                        tx_info map__17055
                        read_at (get map__17055 :read-at)
                        started_at (get map__17055 :started-at)
                        applied_at (get map__17055 :applied-at)
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
              (recur seq_17051 chunk_17052 count_17053 (inc i_17054)))
            (let [temp__5804__auto__ (seq seq_17051)]
              (when temp__5804__auto__
                (let [seq_17051 temp__5804__auto__]
                  (if (chunked-seq? seq_17051)
                    (let [c__6065__auto__ (chunk-first seq_17051)]
                      (recur
                        (chunk-rest seq_17051)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [id (first seq_17051)]
                      (let [temp__5804__auto__ (cache/remove log-event-map id)]
                        (when temp__5804__auto__
                          (let [map__17057 temp__5804__auto__
                                map__17057 (if (seq? map__17057)
                                             (if (next map__17057)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__17057))
                                               (if (seq map__17057) (first map__17057) {}))
                                             map__17057)
                                tx_info map__17057
                                read_at (get map__17057 :read-at)
                                started_at (get map__17057 :started-at)
                                applied_at (get map__17057 :applied-at)
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
                      (recur (next seq_17051) nil 0 0)))))))))))
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
    ([is]
      (let [read_at (java.lang.System/nanoTime)
            fin (reader is)
            msg (.readObject ^org.fressian.Reader fin)]
        (cache/put log-event-map (common/getx msg :id) {:read-at (long read_at)})
        msg)))
  (reset-meta!
    #'read-message
    (assoc {:arglists (clojure.core/list ['is]), :column (int 1)} :name 'read-message :ns *ns*))
  (defn create-procargs
    ([tx options] (cond-> {:id (common/rand-uuid), :data tx} options (assoc :options options))))
  (reset-meta!
    #'create-procargs
    (assoc
      {:arglists (clojure.core/list ['tx 'options]), :column (int 1)}
      :name
      'create-procargs
      :ns
      *ns*)))
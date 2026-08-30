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
  (defn submit-address ([db_name] (str db_name ".tx-submit")))
  (defn push-address ([db_name] (str db_name ".tx-result")))
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
  (def read-handlers
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
  (defn writer
    ([out cache] (fressian/create-writer out (write-handlers cache)))
    ([out] (writer out true)))
  (reset-meta!
    #'writer
    (assoc
      {:tag org.fressian.Writer, :arglists (clojure.core/list ['out] ['out 'cache]), :column 1}
      :name
      'writer
      :ns
      *ns*))
  (defn reader ([in] (fressian/create-reader in read-handlers)))
  (reset-meta!
    #'reader
    (assoc
      {:tag org.fressian.Reader,
       :arglists (clojure.core/list [(.withMeta 'in {:tag 'InputStream})]),
       :column 1}
      :name
      'reader
      :ns
      *ns*))
  (def log-event-map (cache/create-limited 100))
  (def loggable-keys
   [:event :txid :t :msec :apply-msec :datom-count :io-stats :tx-stats :pf-stats])
  (defn log-completion!
    ([ids]
      (let [written_at (java.lang.System/nanoTime)]
        (loop [seq_15905 (seq ids) chunk_15906 nil count_15907 0 i_15908 0]
          (if (< i_15908 count_15907)
            (let [id (.nth ^clojure.lang.Indexed chunk_15906 (int i_15908))]
              (let [temp__5457__auto__ (cache/remove log-event-map id)]
                (when temp__5457__auto__
                  (let [map__15909 temp__5457__auto__
                        map__15909 (if (seq? map__15909)
                                     (clojure.lang.PersistentHashMap/create (seq map__15909))
                                     map__15909)
                        tx_info map__15909
                        read_at (get map__15909 :read-at)
                        started_at (get map__15909 :started-at)
                        applied_at (get map__15909 :applied-at)
                        tx_nsec (let [and__5236__auto__ written_at]
                                  (if (long and__5236__auto__)
                                    (and read_at (- written_at read_at))
                                    (long and__5236__auto__)))
                        msec (let [and__5236__auto__ written_at]
                               (if (long and__5236__auto__)
                                 (and read_at (logger/format-as-msec tx_nsec))
                                 (long and__5236__auto__)))
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
                              loggable-keys)))
                        nil)
                      nil))))
              (recur seq_15905 chunk_15906 count_15907 (inc i_15908)))
            (let [temp__5457__auto__ (seq seq_15905)]
              (when temp__5457__auto__
                (let [seq_15905 temp__5457__auto__]
                  (if (chunked-seq? seq_15905)
                    (let [c__5719__auto__ (chunk-first seq_15905)]
                      (recur
                        (chunk-rest seq_15905)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [id (first seq_15905)]
                      (let [temp__5457__auto__ (cache/remove log-event-map id)]
                        (when temp__5457__auto__
                          (let [map__15911 temp__5457__auto__
                                map__15911 (if (seq? map__15911)
                                             (clojure.lang.PersistentHashMap/create
                                               (seq map__15911))
                                             map__15911)
                                tx_info map__15911
                                read_at (get map__15911 :read-at)
                                started_at (get map__15911 :started-at)
                                applied_at (get map__15911 :applied-at)
                                tx_nsec (let [and__5236__auto__ written_at]
                                          (if (long and__5236__auto__)
                                            (and read_at (- written_at read_at))
                                            (long and__5236__auto__)))
                                msec (let [and__5236__auto__ written_at]
                                       (if (long and__5236__auto__)
                                         (and read_at (logger/format-as-msec tx_nsec))
                                         (long and__5236__auto__)))
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
                                      loggable-keys)))
                                nil)
                              nil))))
                      (recur (next seq_15905) nil 0 0)))))))))))
  (defn add-to-log-event!
    ([id m]
      (let [tx_info (cache/get-from-cache log-event-map id {})]
        (cache/put log-event-map id (merge tx_info m)))))
  (defn read-message
    ([is]
      (let [read_at (java.lang.System/nanoTime)
            fin (reader is)
            msg (.readObject ^org.fressian.Reader fin)]
        (cache/put log-event-map (common/getx msg :id) {:read-at (long read_at)})
        msg)))
  (defn create-procargs
    ([tx options] (cond-> {:id (common/rand-uuid), :data tx} options (assoc :options options)))))
(do
  (clojure.core/in-ns 'datomic.peer-client)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.client.api.impl :as 'api-impl]
        ['datomic.client.api.protocols :as 'api-p]
        ['datomic.common :refer (clojure.core/list 'throw-anom) :as 'common]
        ['datomic.api :as 'api]
        ['datomic.error :as 'error]
        ['datomic.peer :as 'peer]
        ['datomic.pull :as 'pull]
        ['datomic.query :as 'q]
        ['datomic.query.support :as 'qs]
        ['datomic.client-server.spi-support :as 'spi-support]
        ['datomic.stats :as 'stats]
        ['datomic.uri :as 'uri])
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.query.support.MapOnIndexed)))
  (when-not (.equals 'datomic.peer-client 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.peer-client))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.client.api.impl :as 'api-impl]
          ['datomic.client.api.protocols :as 'api-p]
          ['datomic.common :refer (clojure.core/list 'throw-anom) :as 'common]
          ['datomic.api :as 'api]
          ['datomic.error :as 'error]
          ['datomic.peer :as 'peer]
          ['datomic.pull :as 'pull]
          ['datomic.query :as 'q]
          ['datomic.query.support :as 'qs]
          ['datomic.client-server.spi-support :as 'spi-support]
          ['datomic.stats :as 'stats]
          ['datomic.uri :as 'uri])
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.query.support.MapOnIndexed))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.peer-client" "->DbProxy")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.peer-client" "create-db-proxy")
    {:declared true, :column (int 1)})
  (defn wrap-tx-result
    ([result conn]
      (update (update result :db-after create-db-proxy conn) :db-before create-db-proxy conn)))
  (reset-meta!
    #'wrap-tx-result
    (assoc
      {:arglists (clojure.core/list ['result 'conn]), :column (int 1)}
      :name
      'wrap-tx-result
      :ns
      *ns*))
  (defn base-uri ([cfg] (let [uri (:uri cfg)] (subs uri 0 (long (- (count uri) 2))))))
  (reset-meta!
    #'base-uri
    (assoc {:arglists (clojure.core/list ['cfg]), :column (int 1)} :name 'base-uri :ns *ns*))
  (def db-uri (fn db_uri ([cfg db_name] (str (base-uri cfg) "/" db_name))))
  (reset-meta!
    #'db-uri
    (assoc
      {:arglists (clojure.core/list ['cfg 'db-name]), :column (int 1)}
      :name
      'db-uri
      :ns
      *ns*))
  (def result-seq
   (fn result_seq
     ([result p__24221]
       (let [map__24222 p__24221
             map__24222 (if (seq? map__24222)
                          (if (next map__24222)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24222))
                            (if (seq map__24222) (first map__24222) {}))
                          map__24222)
             offset (get map__24222 :offset)
             limit (get map__24222 :limit 1000)]
         (sequence (common/result-xform offset limit force) result)))))
  (reset-meta!
    #'result-seq
    (assoc
      {:arglists (clojure.core/list ['result {:keys ['offset 'limit], :or {'limit 1000}}]),
       :column (int 1)}
      :name
      'result-seq
      :ns
      *ns*))
  (def apply-timeout
   (fn apply_timeout
     ([vq user_timeout]
       (merge vq {:timeout [60000]} (when user_timeout {:timeout [user_timeout]})))))
  (reset-meta!
    #'apply-timeout
    (assoc
      {:arglists (clojure.core/list ['vq 'user-timeout]), :column (int 1)}
      :name
      'apply-timeout
      :ns
      *ns*))
  (defn create-db-lookup
    ([conn]
      (fn fn__24225
        ([id]
          (let [db (api/db conn)]
            (if (= (:id db) id)
              db
              (do (throw (java.lang.RuntimeException. "Peer client db lookup failed.")) nil)))))))
  (reset-meta!
    #'create-db-lookup
    (assoc
      {:arglists (clojure.core/list ['conn]), :column (int 1)}
      :name
      'create-db-lookup
      :ns
      *ns*))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol Unwrap (unwrap-proxies [x] "Unwraps x if a db proxy, else returns x unchanged."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.peer-client" "Unwrap")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'Unwrap :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'unwrap-proxies
                                        {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc "Unwraps x if a db proxy, else returns x unchanged."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer-client" "Unwrap"))
          protocol_method_name__7422 'unwrap-proxies]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer-client" "unwrap-proxies")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
  (extend java.lang.Object Unwrap {:unwrap-proxies (fn fn__24244 ([x] x))})
  (extend nil Unwrap {:unwrap-proxies (fn fn__24246 ([_] nil))})
  (defn disallow-find-variants!
    ([query]
      (when (some
              (fn fn__24249 ([p1__24248#] (or (vector? p1__24248#) (= '. p1__24248#))))
              (:find query))
        (common/throw-anom
          #:cognitect.anomalies{:category :cognitect.anomalies/incorrect,
                                :message "Only find-rel elements are allowed in client :find"}))))
  (reset-meta!
    #'disallow-find-variants!
    (assoc
      {:arglists (clojure.core/list ['query]), :column (int 1)}
      :name
      'disallow-find-variants!
      :ns
      *ns*))
  (deftype
    Client
    [cfg]
    datomic.client.api.protocols.Client
    (connect [this arg_map] (peer/connect-uri (db-uri cfg (:db-name arg_map))))
    (delete-database
      [this arg_map]
      (do (peer/delete-database (db-uri cfg (:db-name arg_map))) true))
    (create-database
      [this arg_map]
      (do (peer/create-database (db-uri cfg (:db-name arg_map))) true))
    (list-databases [this arg_map] (peer/get-database-names (:uri cfg)))
    (administer-system [this arg_map] (peer/administer-system arg_map)))
  (clojure.core/import 'datomic.peer_client.Client)
  (defn ->Client ([cfg] (datomic.peer_client.Client. cfg)))
  (reset-meta!
    #'->Client
    (assoc {:arglists (clojure.core/list ['cfg]), :column (int 1)} :name '->Client :ns *ns*))
  (extend
    datomic.peer.LocalConnection
    api-p/Connection
    {:db (fn fn__24258 ([conn] (create-db-proxy conn))),
     :with-db (fn fn__24260 ([conn] (create-db-proxy conn))),
     :sync
     (fn fn__24262
       ([conn t]
         (let [db_id (:id (api/db conn)) desc {:database-id db_id, :t t}] (->DbProxy desc conn)))),
     :transact
     (fn fn__24264
       ([conn arg_map] (wrap-tx-result (deref (api/transact conn (:tx-data arg_map))) conn))),
     :tx-range
     (fn fn__24266
       ([conn arg_map]
         (result-seq (api/tx-range (api/log conn) (:start arg_map) (:end arg_map)) arg_map)))})
  (extend
    datomic.peer.Connection
    api-p/Connection
    {:db (fn fn__24268 ([conn] (create-db-proxy conn))),
     :with-db (fn fn__24270 ([conn] (create-db-proxy conn))),
     :sync
     (fn fn__24272
       ([conn t]
         (let [db_id (:id (api/db conn)) desc {:database-id db_id, :t t}] (->DbProxy desc conn)))),
     :transact
     (fn fn__24274
       ([conn arg_map] (wrap-tx-result (deref (api/transact conn (:tx-data arg_map))) conn))),
     :tx-range
     (fn fn__24276
       ([this arg_map]
         (result-seq (api/tx-range (api/log this) (:start arg_map) (:end arg_map)) arg_map)))})
  (defn create-client
    ([cfg]
      (if (= "*" (some-> (:uri cfg) (uri/parse) (:db-name)))
        (datomic.peer_client.Client. cfg)
        (error/raise
          :db.error/invalid-db-uri
          "Invalid :uri in client connect map. Note that URI must have '*' in place of database name."))))
  (reset-meta!
    #'create-client
    (assoc {:arglists (clojure.core/list ['cfg]), :column (int 1)} :name 'create-client :ns *ns*))
  (def desc->db
   (fn desc__GT_db
     ([desc conn] (spi-support/desc->db desc (:database-id desc) (create-db-lookup conn)))))
  (reset-meta!
    #'desc->db
    (assoc
      {:arglists (clojure.core/list (.withMeta ['desc 'conn] {:tag 'datomic.Database})),
       :column (int 1)}
      :name
      'desc->db
      :ns
      *ns*))
  (def local-q
   (fn local_q
     ([arg_map qtype]
       (let [map__24281 arg_map
             map__24281 (if (seq? map__24281)
                          (if (next map__24281)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24281))
                            (if (seq map__24281) (first map__24281) {}))
                          map__24281)
             query (get map__24281 :query)
             args (get map__24281 :args)
             offset (get map__24281 :offset)
             limit (get map__24281 :limit)
             query (qs/query-map query)
             _ (disallow-find-variants! query)
             vec__24282 (q/q* query (map unwrap-proxies args))
             result (nth vec__24282 (int 0) nil)
             pf (nth vec__24282 (int 1) nil)
             xform (common/result-xform offset limit pf)
             G__24285 qtype]
         (case
           G__24285
           :q
           (into [] xform result)
           :qseq
           (qs/counted-seq (sequence xform result) (common/result-count offset limit result)))))))
  (reset-meta!
    #'local-q
    (assoc
      {:arglists (clojure.core/list ['arg-map 'qtype]), :column (int 1)}
      :name
      'local-q
      :ns
      *ns*))
  (deftype
    DbProxy
    [desc conn]
    datomic.client.api.protocols.Db
    clojure.lang.ILookup
    datomic.client.api.impl.Queryable
    datomic.peer_client.Unwrap
    (with [this arg_map] (wrap-tx-result (.with (desc->db desc conn) (:tx-data arg_map)) conn))
    (index-pull
      [this arg_map]
      (seq
        (result-seq (pull/index-pull (desc->db desc conn) arg_map) (merge {:limit -1} arg_map))))
    (pull [this selector eid] (pull/pull-1 (desc->db desc conn) selector eid))
    (pull [this arg_map] (pull/pull-1 (desc->db desc conn) (:selector arg_map) (:eid arg_map)))
    (index-range
      [this arg_map]
      (let [map__24289 arg_map
            map__24289 (if (seq? map__24289)
                         (if (next map__24289)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24289))
                           (if (seq map__24289) (first map__24289) {}))
                         map__24289)
            attrid (get map__24289 :attrid)
            start (get map__24289 :start)
            end (get map__24289 :end)]
        (result-seq (.indexRange (desc->db desc conn) attrid start end) arg_map)))
    (datoms
      [this arg_map]
      (let [map__24288 arg_map
            map__24288 (if (seq? map__24288)
                         (if (next map__24288)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24288))
                           (if (seq map__24288) (first map__24288) {}))
                         map__24288)
            index (get map__24288 :index)
            components (get map__24288 :components)]
        (result-seq (db/datoms (desc->db desc conn) index components) arg_map)))
    (db-stats [this] (stats/db-stats (unwrap-proxies this)))
    (since [this time_point] (create-db-proxy (.since (desc->db desc conn) time_point) conn))
    (history [this] (create-db-proxy (.history (desc->db desc conn)) conn))
    (as-of [this time_point] (create-db-proxy (.asOf (desc->db desc conn) time_point) conn))
    (qseq [this arg_map] (local-q arg_map :qseq))
    (q [this arg_map] (local-q arg_map :q))
    (valAt [this k not_found] (get desc k not_found))
    (valAt [this k] (get desc k))
    (unwrap-proxies [this] (desc->db desc conn)))
  (clojure.core/import 'datomic.peer_client.DbProxy)
  (defn ->DbProxy ([desc conn] (datomic.peer_client.DbProxy. desc conn)))
  (reset-meta!
    #'->DbProxy
    (assoc
      {:arglists (clojure.core/list ['desc 'conn]), :column (int 1)}
      :name
      '->DbProxy
      :ns
      *ns*))
  (defmethod
    print-method
    datomic.peer_client.DbProxy
    fn__24294
    ([db w]
      (.write
        ^java.io.Writer w
        (str (assoc (.-desc ^datomic.peer_client.DbProxy db) :type :datomic.peer-client/db-proxy)))
      nil))
  (defmethod print-dup datomic.peer_client.DbProxy fn__24296 ([o w] (print-method o w)))
  (def create-db-proxy
   (fn create_db_proxy
     ([db conn] (let [db_id (:id db) desc (spi-support/db->desc db)] (->DbProxy desc conn)))
     ([conn] (create-db-proxy (api/db conn) conn))))
  (reset-meta!
    #'create-db-proxy
    (assoc
      {:arglists (clojure.core/list ['conn] ['db 'conn]), :column (int 1)}
      :name
      'create-db-proxy
      :ns
      *ns*))
  (extend
    datomic.db.Db
    api-impl/Queryable
    {:q (fn fn__24299 ([db arg_map] (local-q arg_map :q))),
     :qseq (fn fn__24301 ([_ arg_map] (local-q arg_map :qseq)))}
    api-p/Db
    {:as-of
     (fn fn__24303
       ([db time_point]
         (let [needed_t (db/as-of-t db time_point)]
           (when (< (.basisT ^datomic.db.Db db) needed_t)
             (common/throw-anom
               #:cognitect.anomalies{:category :cognitect.anomalies/not-found,
                                     :message (str "Db not yet available for t=" needed_t)}))
           (db/local-db (.asOf ^datomic.db.Db db time_point))))),
     :history (fn fn__24305 ([db] (db/local-db (.history ^datomic.db.Db db)))),
     :since (fn fn__24307 ([db time_point] (db/local-db (.since ^datomic.db.Db db time_point)))),
     :db-stats (fn fn__24309 ([db] (stats/db-stats db))),
     :datoms
     (fn fn__24311
       ([db arg_map]
         (let [map__24312 arg_map
               map__24312 (if (seq? map__24312)
                            (if (next map__24312)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__24312))
                              (if (seq map__24312) (first map__24312) {}))
                            map__24312)
               index (get map__24312 :index)
               components (get map__24312 :components)]
           (result-seq (db/datoms db index components) arg_map)))),
     :index-range
     (fn fn__24314
       ([db arg_map]
         (let [map__24315 arg_map
               map__24315 (if (seq? map__24315)
                            (if (next map__24315)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__24315))
                              (if (seq map__24315) (first map__24315) {}))
                            map__24315)
               attrid (get map__24315 :attrid)
               start (get map__24315 :start)
               end (get map__24315 :end)]
           (result-seq (.indexRange ^datomic.db.Db db attrid start end) arg_map)))),
     :pull
     (fn fn__24317
       ([db selector eid] (pull/pull-1 db selector eid))
       ([db arg_map] (pull/pull-1 db (:selector arg_map) (:eid arg_map)))),
     :with
     (fn fn__24319
       ([db arg_map]
         (let [map__24320 arg_map
               map__24320 (if (seq? map__24320)
                            (if (next map__24320)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__24320))
                              (if (seq map__24320) (first map__24320) {}))
                            map__24320)
               tx_data (get map__24320 :tx-data)]
           (wrap-tx-result (.with ^datomic.db.Db db ^java.util.List tx_data)))))}
    db/LocalDb
    {:local-db
     (fn fn__24322
       ([db]
         (assoc
           db
           :t
           (long (.basisT ^datomic.db.Db db))
           :as-of
           (.asOfT ^datomic.db.Db db)
           :since
           (.sinceT ^datomic.db.Db db)
           :history?
           (.isHistory ^datomic.db.Db db))))}))
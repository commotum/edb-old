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
     ([result p__26268]
       (let [map__26269 p__26268
             map__26269 (if (seq? map__26269)
                          (if (next map__26269)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26269))
                            (if (seq map__26269) (first map__26269) {}))
                          map__26269)
             offset (get map__26269 :offset)
             limit (get map__26269 :limit 1000)]
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
      (fn fn__26272
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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol Unwrap (unwrap-proxies [x] "Unwraps x if a db proxy, else returns x unchanged."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.peer-client" "Unwrap")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'Unwrap :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'unwrap-proxies
                                        {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc "Unwraps x if a db proxy, else returns x unchanged."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.peer-client" "Unwrap"))
          protocol_method_name__7433 'unwrap-proxies]
      (reset-meta!
        (clojure.lang.RT/var "datomic.peer-client" "unwrap-proxies")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (extend java.lang.Object Unwrap {:unwrap-proxies (fn fn__26291 ([x] x))})
  (extend nil Unwrap {:unwrap-proxies (fn fn__26293 ([_] nil))})
  (defn disallow-find-variants!
    ([query]
      (when (some
              (fn fn__26296 ([p1__26295#] (or (vector? p1__26295#) (= '. p1__26295#))))
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
    {:db (fn fn__26305 ([conn] (create-db-proxy conn))),
     :with-db (fn fn__26307 ([conn] (create-db-proxy conn))),
     :sync
     (fn fn__26309
       ([conn t]
         (let [db_id (:id (api/db conn)) desc {:database-id db_id, :t t}] (->DbProxy desc conn)))),
     :transact
     (fn fn__26311
       ([conn arg_map] (wrap-tx-result (deref (api/transact conn (:tx-data arg_map))) conn))),
     :tx-range
     (fn fn__26313
       ([conn arg_map]
         (result-seq (api/tx-range (api/log conn) (:start arg_map) (:end arg_map)) arg_map)))})
  (extend
    datomic.peer.Connection
    api-p/Connection
    {:db (fn fn__26315 ([conn] (create-db-proxy conn))),
     :with-db (fn fn__26317 ([conn] (create-db-proxy conn))),
     :sync
     (fn fn__26319
       ([conn t]
         (let [db_id (:id (api/db conn)) desc {:database-id db_id, :t t}] (->DbProxy desc conn)))),
     :transact
     (fn fn__26321
       ([conn arg_map] (wrap-tx-result (deref (api/transact conn (:tx-data arg_map))) conn))),
     :tx-range
     (fn fn__26323
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
       (let [map__26328 arg_map
             map__26328 (if (seq? map__26328)
                          (if (next map__26328)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26328))
                            (if (seq map__26328) (first map__26328) {}))
                          map__26328)
             query (get map__26328 :query)
             args (get map__26328 :args)
             offset (get map__26328 :offset)
             limit (get map__26328 :limit)
             query (qs/query-map query)
             _ (disallow-find-variants! query)
             vec__26329 (q/q* query (map unwrap-proxies args))
             result (nth vec__26329 (int 0) nil)
             pf (nth vec__26329 (int 1) nil)
             xform (common/result-xform offset limit pf)
             G__26332 qtype]
         (case
           G__26332
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
      (let [map__26338 arg_map
            map__26338 (if (seq? map__26338)
                         (if (next map__26338)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26338))
                           (if (seq map__26338) (first map__26338) {}))
                         map__26338)
            attrid (get map__26338 :attrid)
            start (get map__26338 :start)
            end (get map__26338 :end)]
        (result-seq (.indexRange (desc->db desc conn) attrid start end) arg_map)))
    (rseek-datoms
      [this arg_map]
      (let [map__26337 arg_map
            map__26337 (if (seq? map__26337)
                         (if (next map__26337)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26337))
                           (if (seq map__26337) (first map__26337) {}))
                         map__26337)
            index (get map__26337 :index)
            components (get map__26337 :components)]
        (result-seq (db/rseek-datoms (desc->db desc conn) index components) arg_map)))
    (seek-datoms
      [this arg_map]
      (let [map__26336 arg_map
            map__26336 (if (seq? map__26336)
                         (if (next map__26336)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26336))
                           (if (seq map__26336) (first map__26336) {}))
                         map__26336)
            index (get map__26336 :index)
            components (get map__26336 :components)]
        (result-seq (db/seek-datoms (desc->db desc conn) index components) arg_map)))
    (datoms
      [this arg_map]
      (let [map__26335 arg_map
            map__26335 (if (seq? map__26335)
                         (if (next map__26335)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26335))
                           (if (seq map__26335) (first map__26335) {}))
                         map__26335)
            index (get map__26335 :index)
            components (get map__26335 :components)]
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
    fn__26343
    ([db w]
      (.write
        ^java.io.Writer w
        (str (assoc (.-desc ^datomic.peer_client.DbProxy db) :type :datomic.peer-client/db-proxy)))
      nil))
  (defmethod print-dup datomic.peer_client.DbProxy fn__26345 ([o w] (print-method o w)))
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
    {:q (fn fn__26348 ([db arg_map] (local-q arg_map :q))),
     :qseq (fn fn__26350 ([_ arg_map] (local-q arg_map :qseq)))}
    api-p/Db
    {:since (fn fn__26352 ([db time_point] (db/local-db (.since ^datomic.db.Db db time_point)))),
     :index-range
     (fn fn__26354
       ([db arg_map]
         (let [map__26355 arg_map
               map__26355 (if (seq? map__26355)
                            (if (next map__26355)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__26355))
                              (if (seq map__26355) (first map__26355) {}))
                            map__26355)
               attrid (get map__26355 :attrid)
               start (get map__26355 :start)
               end (get map__26355 :end)]
           (result-seq (.indexRange ^datomic.db.Db db attrid start end) arg_map)))),
     :db-stats (fn fn__26357 ([db] (stats/db-stats db))),
     :history (fn fn__26359 ([db] (db/local-db (.history ^datomic.db.Db db)))),
     :seek-datoms
     (fn fn__26361
       ([db arg_map]
         (let [map__26362 arg_map
               map__26362 (if (seq? map__26362)
                            (if (next map__26362)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__26362))
                              (if (seq map__26362) (first map__26362) {}))
                            map__26362)
               index (get map__26362 :index)
               components (get map__26362 :components)]
           (result-seq (db/seek-datoms db index components) arg_map)))),
     :as-of
     (fn fn__26364
       ([db time_point]
         (let [needed_t (db/as-of-t db time_point)]
           (when (< (.basisT ^datomic.db.Db db) needed_t)
             (common/throw-anom
               #:cognitect.anomalies{:category :cognitect.anomalies/not-found,
                                     :message (str "Db not yet available for t=" needed_t)}))
           (db/local-db (.asOf ^datomic.db.Db db time_point))))),
     :with
     (fn fn__26366
       ([db arg_map]
         (let [map__26367 arg_map
               map__26367 (if (seq? map__26367)
                            (if (next map__26367)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__26367))
                              (if (seq map__26367) (first map__26367) {}))
                            map__26367)
               tx_data (get map__26367 :tx-data)]
           (wrap-tx-result (.with ^datomic.db.Db db ^java.util.List tx_data))))),
     :pull
     (fn fn__26369
       ([db selector eid] (pull/pull-1 db selector eid))
       ([db arg_map] (pull/pull-1 db (:selector arg_map) (:eid arg_map)))),
     :datoms
     (fn fn__26371
       ([db arg_map]
         (let [map__26372 arg_map
               map__26372 (if (seq? map__26372)
                            (if (next map__26372)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__26372))
                              (if (seq map__26372) (first map__26372) {}))
                            map__26372)
               index (get map__26372 :index)
               components (get map__26372 :components)]
           (result-seq (db/datoms db index components) arg_map)))),
     :rseek-datoms
     (fn fn__26374
       ([db arg_map]
         (let [map__26375 arg_map
               map__26375 (if (seq? map__26375)
                            (if (next map__26375)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__26375))
                              (if (seq map__26375) (first map__26375) {}))
                            map__26375)
               index (get map__26375 :index)
               components (get map__26375 :components)]
           (result-seq (db/rseek-datoms db index components) arg_map))))}
    db/LocalDb
    {:local-db
     (fn fn__26377
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
(do
  (clojure.core/in-ns 'datomic.client-server.spi-support)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list 'offer!)]
        ['datomic.common :as 'common]
        ['datomic.client-spi.server-spi :as 'server-spi]
        ['datomic.db :as 'db]
        ['datomic.error :as 'error]
        ['datomic.measure.io-stats :as 'io-stats]
        ['datomic.pull :as 'pull]
        ['datomic.query :as 'query]
        ['datomic.query.support :as 'qs]
        ['datomic.stats :as 'stats])
      (clojure.core/import 'datomic.Connection)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.db.Db)))
  (when-not (.equals 'datomic.client-server.spi-support 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.client-server.spi-support))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :refer (clojure.core/list 'offer!)]
          ['datomic.common :as 'common]
          ['datomic.client-spi.server-spi :as 'server-spi]
          ['datomic.db :as 'db]
          ['datomic.error :as 'error]
          ['datomic.measure.io-stats :as 'io-stats]
          ['datomic.pull :as 'pull]
          ['datomic.query :as 'query]
          ['datomic.query.support :as 'qs]
          ['datomic.stats :as 'stats])
        (clojure.core/import 'datomic.Connection)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.db.Db))))
  (set! *warn-on-reflection* true)
  (def client-spi-config
   #:client-spi{:default-query-timeout 10000,
                :query-grammar-docs "http://docs.datomic.com/query.html#grammar"})
  (reset-meta! #'client-spi-config (assoc {:column (int 1)} :name 'client-spi-config :ns *ns*))
  (def needed-t
   (fn needed_t
     ([p__26207]
       (let [map__26208 p__26207
             map__26208 (if (seq? map__26208)
                          (if (next map__26208)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26208))
                            (if (seq map__26208) (first map__26208) {}))
                          map__26208)
             t (get map__26208 :t)
             as_of_t (get map__26208 :as-of-t)
             since_t (get map__26208 :since-t)]
         (apply max (remove nil? [t as_of_t since_t]))))))
  (reset-meta!
    #'needed-t
    (assoc
      {:private true,
       :arglists (clojure.core/list [{:keys ['t 'as-of-t 'since-t]}]),
       :column (int 1)}
      :name
      'needed-t
      :ns
      *ns*))
  (def effective-as-of-t
   (fn effective_as_of_t
     ([p__26210]
       (let [map__26211 p__26210
             map__26211 (if (seq? map__26211)
                          (if (next map__26211)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26211))
                            (if (seq map__26211) (first map__26211) {}))
                          map__26211)
             t (get map__26211 :t)
             as_of_t (get map__26211 :as-of-t)]
         (apply min (remove nil? [t as_of_t]))))))
  (reset-meta!
    #'effective-as-of-t
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['t 'as-of-t]}]), :column (int 1)}
      :name
      'effective-as-of-t
      :ns
      *ns*))
  (def apply-filters
   (fn apply_filters
     ([db p__26213]
       (let [map__26214 p__26213
             map__26214 (if (seq? map__26214)
                          (if (next map__26214)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26214))
                            (if (seq map__26214) (first map__26214) {}))
                          map__26214)
             desc map__26214
             t (get map__26214 :t)
             as_of (get map__26214 :as-of)
             since (get map__26214 :since)
             history (get map__26214 :history)
             basis (.basisT ^datomic.Database db)
             t (or t (long basis))
             tmap {:t t,
                   :as-of-t
                   (let [G__26215 as_of] (when-not (nil? G__26215) (db/as-of-t db G__26215))),
                   :since-t
                   (let [G__26216 since] (when-not (nil? G__26216) (db/as-of-t db G__26216)))}
             needed_t (needed-t tmap)
             eff_as_of_t (effective-as-of-t tmap)]
         (if (< basis needed_t)
           (error/raise
             :cluster.error/db-not-ready
             "DB not ready"
             {:cognitect.anomalies/category :cognitect.anomalies/busy,
              :needed-t needed_t,
              :basis (long basis)})
           (cond->
             db
             (< eff_as_of_t basis)
             (.asOf eff_as_of_t)
             since
             (.since since)
             history
             (.history)))))))
  (reset-meta!
    #'apply-filters
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'db {:tag 'Database}) {:keys ['t 'as-of 'since 'history], :as 'desc}]),
       :column (int 1)}
      :name
      'apply-filters
      :ns
      *ns*))
  (extend
    datomic.db.Db
    server-spi/DbFilters
    {:apply-db-filters
     (fn fn__26220
       ([db desc]
         (let [unfiltered_db (db/unfiltered db)
               filters (select-keys desc [:as-of :since :history])]
           (apply-filters unfiltered_db filters))))})
  (def desc->db
   (fn desc__GT_db
     ([p__26222 db_id db_id_>db]
       (let [map__26223 p__26222
             map__26223 (if (seq? map__26223)
                          (if (next map__26223)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26223))
                            (if (seq map__26223) (first map__26223) {}))
                          map__26223)
             desc map__26223
             with_id (get map__26223 :with-id)
             temp__5823__auto__ (if with_id
                                  ((common/requiring-resolve!
                                     'datomic.client-spi.with-cache/cache-get)
                                    with_id)
                                  (^clojure.lang.IFn db_id_>db db_id))]
         (if temp__5823__auto__
           (let [db temp__5823__auto__] (apply-filters db desc))
           (error/raise
             :cluster.error/db-not-available
             "DB not found"
             {:cognitect.anomalies/category :cognitect.anomalies/not-found,
              :database-id (:database-id desc)}))))))
  (reset-meta!
    #'desc->db
    (assoc
      {:arglists (clojure.core/list [{:keys ['with-id], :as 'desc} 'db-id 'db-id->db]),
       :column (int 1)}
      :name
      'desc->db
      :ns
      *ns*))
  (defn remove-nil-vals
    ([m] (into {} (remove (fn fn__26227 ([p1__26226#] (nil? (second p1__26226#))))) m)))
  (reset-meta!
    #'remove-nil-vals
    (assoc
      {:private true, :arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'remove-nil-vals
      :ns
      *ns*))
  (def db->desc
   (fn db__GT_desc
     ([db]
       (remove-nil-vals
         {:database-id (.id ^datomic.Database db),
          :t (long (.basisT ^datomic.Database db)),
          :next-t (long (.nextT ^datomic.Database db)),
          :as-of (.asOfT ^datomic.Database db),
          :since (.sinceT ^datomic.Database db),
          :history (.isHistory ^datomic.Database db)}))))
  (reset-meta!
    #'db->desc
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]), :column (int 1)}
      :name
      'db->desc
      :ns
      *ns*))
  (def get-db (fn get_db ([conn] (.db ^datomic.Connection conn))))
  (reset-meta!
    #'get-db
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'conn {:tag 'Connection})]),
       :column (int 1)}
      :name
      'get-db
      :ns
      *ns*))
  (deftype
    ClientServer
    [id_>conn token_manager load_ch]
    datomic.client_spi.server_spi.ServerSpi3
    (with-io-stats! [this f context] (io-stats/throw-if-ex! (io-stats/with-io-stats f context)))
    (transact
      [this request complete]
      (let [map__26250 (if (map? request) request {:tx-data request})
            map__26250 (if (seq? map__26250)
                         (if (next map__26250)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26250))
                           (if (seq map__26250) (first map__26250) {}))
                         map__26250)
            database_id (get map__26250 :database-id)
            tx_data (get map__26250 :tx-data)
            io_context (get map__26250 :io-context)
            conn (get id_>conn database_id)
            result (deref
                     (.transactAsync
                       ^datomic.Connection conn
                       ^java.util.List tx_data
                       (when io_context {:io-context io_context})))]
        (^clojure.lang.IFn complete result)))
    (token-manager [this] token_manager)
    (tx-range [this db_id db start end] (.txRange (.log (get id_>conn db_id)) start end))
    (index-pull [this db arg_map] (pull/index-pull db arg_map))
    (pull
      [this db request]
      (let [map__26249 request
            map__26249 (if (seq? map__26249)
                         (if (next map__26249)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26249))
                           (if (seq map__26249) (first map__26249) {}))
                         map__26249)
            selector (get map__26249 :selector)
            eid (get map__26249 :eid)]
        (pull/pull-1 db selector eid)))
    (index-range [this db attrid start end] (.indexRange ^datomic.Database db attrid start end))
    (rseek-datoms
      [this db index eav]
      (let [map__26247 eav
            map__26247 (if (seq? map__26247)
                         (if (next map__26247)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26247))
                           (if (seq map__26247) (first map__26247) {}))
                         map__26247)
            e (get map__26247 :e)
            a (get map__26247 :a)
            v (get map__26247 :v)
            index (db/normalize-kw index)]
        (db/rseek-datoms
          db
          index
          (let [G__26248 index]
            (case G__26248 :aevt [a e v] :avet [a v e] :eavt [e a v] :vaet [v a e])))))
    (seek-datoms
      [this db index eav]
      (let [map__26245 eav
            map__26245 (if (seq? map__26245)
                         (if (next map__26245)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26245))
                           (if (seq map__26245) (first map__26245) {}))
                         map__26245)
            e (get map__26245 :e)
            a (get map__26245 :a)
            v (get map__26245 :v)
            index (db/normalize-kw index)]
        (db/seek-datoms
          db
          index
          (let [G__26246 index]
            (case G__26246 :aevt [a e v] :avet [a v e] :eavt [e a v] :vaet [v a e])))))
    (datoms
      [this db index eav]
      (let [map__26243 eav
            map__26243 (if (seq? map__26243)
                         (if (next map__26243)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26243))
                           (if (seq map__26243) (first map__26243) {}))
                         map__26243)
            e (get map__26243 :e)
            a (get map__26243 :a)
            v (get map__26243 :v)
            index (db/normalize-kw index)]
        (db/datoms
          db
          index
          (let [G__26244 index]
            (case G__26244 :aevt [a e v] :avet [a v e] :eavt [e a v] :vaet [v a e])))))
    (query
      [this query args]
      (do
        (qs/disallow-find-variants! query)
        (let [vec__26236 (query/q* query args)
              result (nth vec__26236 (int 0) nil)
              pf (nth vec__26236 (int 1) nil)]
          (if pf
            (map (fn fn__26239 ([p1__26232#] (delay (^clojure.lang.IFn pf p1__26232#)))) result)
            result))))
    (db-stats [this db] (stats/db-stats db))
    (with [this db tx_data] (.with ^datomic.Database db ^java.util.List tx_data))
    (db->desc [this db] (db->desc db))
    (desc->db
      [this db_id desc]
      (let [temp__5823__auto__ (get id_>conn db_id)]
        (if temp__5823__auto__
          (let [conn temp__5823__auto__] (desc->db desc db_id (fn fn__26234 ([_] (get-db conn)))))
          (do
            (when load_ch
              (clojure.core.async/offer! load_ch {:db-id db_id})
              (throw
                (ex-info
                  "Loading database"
                  #:cognitect.anomalies{:category :cognitect.anomalies/busy,
                                        :message "Loading database"})))
            nil)))))
  (clojure.core/import 'datomic.client_server.spi_support.ClientServer)
  (def ->ClientServer
   (fn __GT_ClientServer
     ([id_>conn token_manager load_ch]
       (datomic.client_server.spi_support.ClientServer. id_>conn token_manager load_ch))))
  (reset-meta!
    #'->ClientServer
    (assoc
      {:arglists (clojure.core/list ['id->conn 'token-manager 'load-ch]), :column (int 1)}
      :name
      '->ClientServer
      :ns
      *ns*))
  (def create-spi
   (fn create_spi
     ([id_>conn token_manager & p__26256]
       (let [vec__26257 p__26256 load_ch (nth vec__26257 (int 0) nil)]
         (->ClientServer id_>conn token_manager load_ch)))))
  (reset-meta!
    #'create-spi
    (assoc
      {:arglists (clojure.core/list ['id->conn 'token-manager '& ['load-ch]]), :column (int 1)}
      :name
      'create-spi
      :ns
      *ns*)))
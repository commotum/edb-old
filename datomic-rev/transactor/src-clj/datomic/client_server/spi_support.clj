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
      (clojure.core/import 'datomic.Database)))
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
        (clojure.core/import 'datomic.Database))))
  (set! *warn-on-reflection* true)
  (def client-spi-config
   #:client-spi{:default-query-timeout 10000,
                :query-grammar-docs "http://docs.datomic.com/query.html#grammar"})
  (reset-meta! #'client-spi-config (assoc {:column (int 1)} :name 'client-spi-config :ns *ns*))
  (def needed-t
   (fn needed_t
     ([p__24169]
       (let [map__24170 p__24169
             map__24170 (if (seq? map__24170)
                          (if (next map__24170)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24170))
                            (if (seq map__24170) (first map__24170) {}))
                          map__24170)
             t (get map__24170 :t)
             as_of_t (get map__24170 :as-of-t)
             since_t (get map__24170 :since-t)]
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
     ([p__24172]
       (let [map__24173 p__24172
             map__24173 (if (seq? map__24173)
                          (if (next map__24173)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24173))
                            (if (seq map__24173) (first map__24173) {}))
                          map__24173)
             t (get map__24173 :t)
             as_of_t (get map__24173 :as-of-t)]
         (apply min (remove nil? [t as_of_t]))))))
  (reset-meta!
    #'effective-as-of-t
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['t 'as-of-t]}]), :column (int 1)}
      :name
      'effective-as-of-t
      :ns
      *ns*))
  (def desc->db
   (fn desc__GT_db
     ([p__24175 db_id db_id_>db]
       (let [map__24176 p__24175
             map__24176 (if (seq? map__24176)
                          (if (next map__24176)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__24176))
                            (if (seq map__24176) (first map__24176) {}))
                          map__24176)
             desc map__24176
             t (get map__24176 :t)
             as_of (get map__24176 :as-of)
             since (get map__24176 :since)
             history (get map__24176 :history)
             with_id (get map__24176 :with-id)
             temp__5802__auto__ (if with_id
                                  ((common/requiring-resolve!
                                     'datomic.client-spi.with-cache/cache-get)
                                    with_id)
                                  (^clojure.lang.IFn db_id_>db db_id))]
         (if temp__5802__auto__
           (let [db temp__5802__auto__
                 basis (.basisT ^datomic.Database db)
                 t (or t (long basis))
                 tmap {:t t,
                       :as-of-t
                       (let [G__24177 as_of] (when-not (nil? G__24177) (db/as-of-t db G__24177))),
                       :since-t
                       (let [G__24178 since] (when-not (nil? G__24178) (db/as-of-t db G__24178)))}
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
                 (.history))))
           (error/raise
             :cluster.error/db-not-available
             "DB not found"
             {:cognitect.anomalies/category :cognitect.anomalies/not-found,
              :database-id (:database-id desc)}))))))
  (reset-meta!
    #'desc->db
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys ['t 'as-of 'since 'history 'with-id], :as 'desc} 'db-id 'db-id->db]),
       :column (int 1)}
      :name
      'desc->db
      :ns
      *ns*))
  (defn remove-nil-vals
    ([m] (into {} (remove (fn fn__24184 ([p1__24183#] (nil? (second p1__24183#))))) m)))
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
      (let [map__24203 (if (map? request) request {:tx-data request})
            map__24203 (if (seq? map__24203)
                         (if (next map__24203)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24203))
                           (if (seq map__24203) (first map__24203) {}))
                         map__24203)
            database_id (get map__24203 :database-id)
            tx_data (get map__24203 :tx-data)
            io_context (get map__24203 :io-context)
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
      (let [map__24202 request
            map__24202 (if (seq? map__24202)
                         (if (next map__24202)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24202))
                           (if (seq map__24202) (first map__24202) {}))
                         map__24202)
            selector (get map__24202 :selector)
            eid (get map__24202 :eid)]
        (pull/pull-1 db selector eid)))
    (index-range [this db attrid start end] (.indexRange ^datomic.Database db attrid start end))
    (datoms
      [this db index eav]
      (let [map__24200 eav
            map__24200 (if (seq? map__24200)
                         (if (next map__24200)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24200))
                           (if (seq map__24200) (first map__24200) {}))
                         map__24200)
            e (get map__24200 :e)
            a (get map__24200 :a)
            v (get map__24200 :v)
            index (db/normalize-kw index)]
        (db/datoms
          db
          index
          (let [G__24201 index]
            (case G__24201 :aevt [a e v] :avet [a v e] :eavt [e a v] :vaet [v a e])))))
    (query
      [this query args]
      (do
        (qs/disallow-find-variants! query)
        (let [vec__24193 (query/q* query args)
              result (nth vec__24193 (int 0) nil)
              pf (nth vec__24193 (int 1) nil)]
          (if pf
            (map (fn fn__24196 ([p1__24189#] (delay (^clojure.lang.IFn pf p1__24189#)))) result)
            result))))
    (db-stats [this db] (stats/db-stats db))
    (with [this db tx_data] (.with ^datomic.Database db ^java.util.List tx_data))
    (db->desc [this db] (db->desc db))
    (desc->db
      [this db_id desc]
      (let [temp__5802__auto__ (get id_>conn db_id)]
        (if temp__5802__auto__
          (let [conn temp__5802__auto__] (desc->db desc db_id (fn fn__24191 ([_] (get-db conn)))))
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
     ([id_>conn token_manager & p__24209]
       (let [vec__24210 p__24209 load_ch (nth vec__24210 (int 0) nil)]
         (->ClientServer id_>conn token_manager load_ch)))))
  (reset-meta!
    #'create-spi
    (assoc
      {:arglists (clojure.core/list ['id->conn 'token-manager '& ['load-ch]]), :column (int 1)}
      :name
      'create-spi
      :ns
      *ns*)))
(do
  (clojure.core/in-ns 'datomic.api)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['filter 'sync])
      (clojure.core/require
        'datomic.query
        ['datomic.db :as 'db]
        ['datomic.common :as 'common]
        ['datomic.fressian :as 'fressian]
        'datomic.pull)
      (clojure.core/import 'datomic.Peer)
      (clojure.core/import 'datomic.Connection)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.Entity)
      (clojure.core/import 'datomic.Log)))
  (when-not (.equals 'datomic.api 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.api))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['filter 'sync])
        (clojure.core/require
          'datomic.query
          ['datomic.db :as 'db]
          ['datomic.common :as 'common]
          ['datomic.fressian :as 'fressian]
          'datomic.pull)
        (clojure.core/import 'datomic.Peer)
        (clojure.core/import 'datomic.Connection)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.Entity)
        (clojure.core/import 'datomic.Log))))
  (set! *warn-on-reflection* true)
  (defn connect ([uri] (Peer/connect uri)))
  (reset-meta!
    #'connect
    (assoc
      {:tag datomic.Connection, :arglists (clojure.core/list ['uri]), :column 1}
      :name
      'connect
      :ns
      *ns*))
  (defn create-database ([uri] (Peer/createDatabase uri)))
  (defn delete-database ([uri] (Peer/deleteDatabase uri)))
  (defn get-database-names ([uri] (Peer/getDatabaseNames uri)))
  (defn administer-system ([options] (Peer/administerSystem ^java.util.Map options)))
  (defn rename-database ([uri new_name] (Peer/renameDatabase uri ^java.lang.String new_name)))
  (defn q ([query & inputs] (datomic.query/q query inputs)))
  (defn query ([query_map] (datomic.query/query query_map)))
  (defn qseq ([query_map] (datomic.query/qseq query_map)))
  (defn tempid
    ([partition n] (Peer/tempid partition (long ^java.lang.Number n)))
    ([partition] (Peer/tempid partition)))
  (defn t->tx ([^long t] (Peer/toTx (long t))))
  (defn tx->t (^long [tx] (Peer/toT tx)))
  (defn part ([eid] (Peer/part eid)))
  (defn function ([m] (Peer/function ^java.util.Map m)))
  (defn db ([connection] (.db ^datomic.Connection connection)))
  (reset-meta!
    #'db
    (assoc
      {:tag datomic.Database,
       :arglists (clojure.core/list [(.withMeta 'connection {:tag 'Connection})]),
       :column 1}
      :name
      'db
      :ns
      *ns*))
  (defn log ([connection] (.log ^datomic.Connection connection)))
  (reset-meta!
    #'log
    (assoc
      {:tag datomic.Log,
       :arglists (clojure.core/list [(.withMeta 'connection {:tag 'Connection})]),
       :column 1}
      :name
      'log
      :ns
      *ns*))
  (defn sync
    ([connection t] (.sync ^datomic.Connection connection (long ^java.lang.Number t)))
    ([connection] (.sync ^datomic.Connection connection)))
  (defn sync-index
    ([connection t] (.syncIndex ^datomic.Connection connection (long ^java.lang.Number t))))
  (defn sync-schema
    ([connection t] (.syncSchema ^datomic.Connection connection (long ^java.lang.Number t))))
  (defn sync-excise
    ([connection t] (.syncExcise ^datomic.Connection connection (long ^java.lang.Number t))))
  (defn request-index ([connection] (.requestIndex ^datomic.Connection connection)))
  (defn gc-storage
    ([connection older_than]
      (.gcStorage ^datomic.Connection connection ^java.util.Date older_than)
      nil))
  (defn transact
    ([connection tx_data & p__15798]
      (let [map__15799 p__15798
            map__15799 (if (seq? map__15799)
                         (if (next map__15799)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15799))
                           (if (seq map__15799) (first map__15799) {}))
                         map__15799)
            options map__15799]
        (.transact ^datomic.Connection connection ^java.util.List tx_data options))))
  (defn transact-async
    ([connection tx_data & p__15801]
      (let [map__15802 p__15801
            map__15802 (if (seq? map__15802)
                         (if (next map__15802)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15802))
                           (if (seq map__15802) (first map__15802) {}))
                         map__15802)
            options map__15802]
        (.transactAsync ^datomic.Connection connection ^java.util.List tx_data options))))
  (defn tx-report-queue ([connection] (.txReportQueue ^datomic.Connection connection)))
  (defn remove-tx-report-queue
    ([connection] (.removeTxReportQueue ^datomic.Connection connection) nil))
  (defn as-of ([db t] (.asOf ^datomic.Database db t)))
  (reset-meta!
    #'as-of
    (assoc
      {:tag datomic.Database,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 't]),
       :column 1}
      :name
      'as-of
      :ns
      *ns*))
  (defn since ([db t] (.since ^datomic.Database db t)))
  (reset-meta!
    #'since
    (assoc
      {:tag datomic.Database,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 't]),
       :column 1}
      :name
      'since
      :ns
      *ns*))
  (defn history ([db] (.history ^datomic.Database db)))
  (reset-meta!
    #'history
    (assoc
      {:tag datomic.Database,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]),
       :column 1}
      :name
      'history
      :ns
      *ns*))
  (defn filter ([db pred] (.filter ^datomic.Database db pred)))
  (reset-meta!
    #'filter
    (assoc
      {:tag datomic.Database,
       :arglists
       (clojure.core/list [(.withMeta 'db {:tag 'Database}) (.withMeta 'pred {:tag 'Object})]),
       :column 1}
      :name
      'filter
      :ns
      *ns*))
  (defn with
    ([db tx_data & opts] (.with ^datomic.Database db ^java.util.List tx_data opts))
    ([db tx_data] (.with ^datomic.Database db ^java.util.List tx_data)))
  (defn basis-t ([db] (long (.basisT ^datomic.Database db))))
  (defn next-t ([db] (long (.nextT ^datomic.Database db))))
  (defn as-of-t ([db] (.asOfT ^datomic.Database db)))
  (defn since-t ([db] (.sinceT ^datomic.Database db)))
  (defn is-history ([db] (.isHistory ^datomic.Database db)))
  (defn is-filtered ([db] (.isFiltered ^datomic.Database db)))
  (defn attribute ([db attrid] (.attribute ^datomic.Database db attrid)))
  (defn entity ([db eid] (.entity ^datomic.Database db eid)))
  (defn pull
    ([db pattern eid & p__15819]
      (let [map__15820 p__15819
            map__15820 (if (seq? map__15820)
                         (if (next map__15820)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15820))
                           (if (seq map__15820) (first map__15820) {}))
                         map__15820)
            options map__15820]
        (.pull ^datomic.Database db pattern eid options))))
  (defn index-pull ([db arg_map] (datomic.pull/dereffed-index-pull db arg_map)))
  (defn pull-many
    ([db pattern eids & p__15823]
      (let [map__15824 p__15823
            map__15824 (if (seq? map__15824)
                         (if (next map__15824)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15824))
                           (if (seq map__15824) (first map__15824) {}))
                         map__15824)
            options map__15824]
        (.pullMany ^datomic.Database db pattern ^java.util.List eids options))))
  (defn touch ([entity] (.touch ^datomic.Entity entity)))
  (defn entity-db ([entity] (.db ^datomic.Entity entity)))
  (defn index-range ([db attrid start end] (.indexRange ^datomic.Database db attrid start end)))
  (defn tx-range ([log start end] (.txRange ^datomic.Log log start end)))
  (defn ident ([db eid] (.ident ^datomic.Database db eid)))
  (defn entid ([db ident] (.entid ^datomic.Database db ident)))
  (defn entid-at ([db part t_or_date] (.entidAt ^datomic.Database db part t_or_date)))
  (defn invoke ([db eid_or_ident & args] (apply db/invoke db eid_or_ident args)))
  (defn datoms ([db index & components] (db/datoms db index components)))
  (defn seek-datoms ([db index & components] (db/seek-datoms db index components)))
  (defn resolve-tempid
    ([db tempids tempid] (Peer/resolveTempid ^datomic.Database db tempids tempid)))
  (defn shutdown
    ([shutdown_clojure]
      (Peer/shutdown (boolean (.booleanValue ^java.lang.Boolean shutdown_clojure)))
      nil))
  (reset-meta!
    #'shutdown
    (assoc
      {:arglists (clojure.core/list ['shutdown-clojure]), :added "0.8.3861", :column 1}
      :name
      'shutdown
      :ns
      *ns*))
  (defn release ([conn] (.release ^datomic.Connection conn) nil))
  (reset-meta!
    #'release
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'conn {:tag 'Connection})]),
       :added "0.8.3861",
       :column 1}
      :name
      'release
      :ns
      *ns*))
  (defn cancel
    ([p__15840]
      (let [map__15841 p__15840
            map__15841 (if (seq? map__15841)
                         (if (next map__15841)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15841))
                           (if (seq map__15841) (first map__15841) {}))
                         map__15841)
            anomaly_map map__15841
            category (get map__15841 :cognitect.anomalies/category)
            allowed_anoms #{:cognitect.anomalies/conflict :cognitect.anomalies/incorrect}
            throw_incorrect_anom (fn throw_incorrect_anom
                                   ([p1__15839#]
                                     (throw
                                       (ex-info
                                         p1__15839#
                                         #:cognitect.anomalies{:category
                                                               :cognitect.anomalies/incorrect,
                                                               :message p1__15839#}))))]
        (if (not category)
          (^clojure.lang.IFn throw_incorrect_anom "Cancel requires :cognitect.anomalies/category")
          (if (not (some #{category} allowed_anoms))
            (^clojure.lang.IFn throw_incorrect_anom
              (str "Invalid :cognitect.anomalies/category provided to cancel: " category))
            (if (not (fressian/fressianable? anomaly_map))
              (^clojure.lang.IFn throw_incorrect_anom "Could not marshal data in cancel anomaly")
              (do
                (when :default
                  (throw
                    (ex-info
                      (or (:cognitect.anomalies/message anomaly_map) "Operation Cancelled")
                      (merge {} anomaly_map #:datomic{:cancelled true}))))
                nil)))))))
  (defn squuid ([] (Peer/squuid)))
  (defn squuid-time-millis (^long [squuid] (Peer/squuidTimeMillis ^java.util.UUID squuid)))
  (defn add-listener
    ([fut f executor]
      (.addListener
        ^datomic.ListenableFuture fut
        ^java.lang.Runnable f
        ^java.util.concurrent.Executor executor)
      nil))
  (defn db-stats ([db] (.dbStats ^datomic.Database db)))
  (reset-meta!
    #'db-stats
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]),
       :added "1.0.6333",
       :column 1}
      :name
      'db-stats
      :ns
      *ns*))
  (defn implicit-part (^long [^long id] (db/implicit-part id)))
  (defn implicit-part-id ([^long part] (db/implicit-part-id part)))
  (loop [seq_15852 (seq
                     {#'administer-system
                      "Administer system. Takes an options map with a required :action key.\n   Throws on failure. Actions include:\n   \n   Release Object Cache\n   :action      :release-object-cache  \n\n   Effect: Clear all entries from the Object Cache.\n\n   Upgrade Schema\n   :action      :upgrade-schema\n   :uri         a URI as per connect\n\n   Effect: Upgrades the base schema of a database to the latest version.\n   NOTE: Read https://docs.datomic.com/operation/deployment.html before calling.",
                      #'since
                      "Returns the value of the database since some point t, exclusive\n   t can be a transaction number, transaction ID, or Date.",
                      #'seek-datoms
                      "Raw access to the index data, by index. The index must be supplied,\n\t and, optionally, one or more leading components of the index can be supplied for the initial search.\n\t Note that, unlike the datoms function, there need not be an exact match on the supplied components.\n\t The iteration will begin at or after the point in the index where the components would reside.\n\t Further, the iteration is not bound by the supplied components, and will only terminate\n\t at the end of the index. Thus you will have to supply your own termination logic, as you rarely\n\t want the entire index. As such, seek-datoms is for more advanced applications, and datoms should be preferred\n\t wherever it is adequate. See also - entid-at.\n\n         :eavt and :aevt indexes will contain all datoms\n         :avet contains datoms for attributes where :db/index = true.\n         :vaet contains datoms for attributes of :db.type/ref\n         :vaet is the reverse index\n\n         See datoms for a description of the returned value.",
                      #'t->tx "Return the transaction id associated with a t value.",
                      #'db
                      "Retrieves a value of the database for reading. Does not\n  communicate with the transactor, nor block.",
                      #'part "Return the partition associated with an entity id.",
                      #'invoke
                      "Lookup the database function named by eid-or-ident, and call it with args.",
                      #'history
                      "Returns a special database containing all assertions and\nretractions across time. This special database can be used for\ndatoms and index-range calls and queries, but not for entity or\nwith calls. as-of and since bounds are also supported. Note that\nqueries will get all of the additions and retractions, which can be\ndistinguished by the fifth datom field :added (true for add/assert)\n[e a v tx added]",
                      #'remove-tx-report-queue
                      "Removes the queue associated with this connection.",
                      #'as-of-t "Returns the as-of point, or nil if none",
                      #'entid
                      "Returns the entity id associated with a symbolic keyword, or the id\n   itself if passed.",
                      #'create-database
                      "Creates database specified by uri. Returns true if the\n   database was created, false if it already exists. See connect\n   for a description of the URI syntax.",
                      #'cancel
                      "Cancels the current Datomic operation (query or transaction).\n\nThrows an ex-info with an anomaly to the original caller.\n\nanomaly-map is an anomaly as described by https://github.com/cognitect-labs/anomalies.\n\n:cognitect.anomalies/category is a required key, valid values are:\n\n  :cognitect.anomalies/incorrect\n  :cognitect.anomalies/conflict\n\nWhen :cognitect.anomalies/message is provided, the message will be used as the Exception's detail message\n\nOther keys should be namespace-qualified.\n\nAll data passed to cancel must be fressian-serializable.",
                      #'entity
                      "Returns a dynamic map of the entity's attributes for the given id, ident or lookup ref.\n   Entities implement\n     clojure.lang.Associative\n     clojure.lang.ILookup\n     clojure.lang.IPersistentCollection\n     clojure.lang.Seqable\n     datomic.Entity",
                      #'pull-many
                      "Returns hierarchical selections of attributes for eids. \n    See https://docs.datomic.com/query/query-pull.html for more information.\n\n    You can get information about the I/O reads performed by a\n    pull with io-stats. To request io-stats, pass :io-context\n    (a qualified keyword) as an option to pull-many. The return\n    value will be a map with\n\n    :ret                the result of the pull\n    :io-stats           io-stats for the pull\n\n    The io-stats map includes:\n\n    :io-context         the io-context passed in\n    :api                :tx-with\n    :api-ms             msec to peform the API call\n    :reads              breakout of reads by cache tier and index sort\n    :nested             breakout iff nested queries set :io-context\n\n    See https://docs.datomic.com/reference/io-stats.html.",
                      #'sync-index
                      "Used to coordinate with background indexing jobs. Returns a\n    future that will acquire a database value that is indexed\n    through time <= t.\n\n    Does not communicate with the transactor, so the future may be\n    available immediately.\n\n    The future can take arbitrarily long to complete.  Waiters\n    should specify a timeout.",
                      #'as-of
                      "Returns the value of the database as of some point t, inclusive.\n   t can be a transaction number, transaction ID, or Date.",
                      #'qseq
                      "Performs the query described by query-map (as per 'query'),\n         returning a lazy seq on the results.  Item transformations such as\n         'pull' are deferred until the seq is consumed. For queries with\n         pull(s), this results in:\n\n         * reduced memory use and the ability to execute larger queries\n         * lower latency before the first results are returned\n\n         The returned seq object efficiently supports 'count'.",
                      #'squuid
                      "Constructs a semi-sequential UUID. Useful for creating UUIDs\nthat don't fragment indexes. Returns a UUID whose most significant\n32 bits are currentTimeMillis rounded to seconds.",
                      #'tempid
                      "Generate a tempid in the specified partition. Within the scope\n   of a single transaction, tempids map consistently to permanent\n   ids. Values of n from -1 to -1000000, inclusive, are reserved for\n   user-created tempids.",
                      #'q
                      "Executes a query against inputs.\n\n   Inputs are data sources e.g. a database value retrieved from\n   Connection.db, a list of lists, and/or rules. If only one data\n   source is provided, no :in section is required, else the :in\n   section describes the inputs.\n\n   query can be a map, list, or string:\n\n   The query map form is {:find vars-and-aggregates\n                          :with vars-included-but-not-returned\n                          :in sources\n                          :where clauses}\n   where vars, sources and clauses are lists.\n\n  :with is optional, and names vars to be kept in the aggregation set but\n   not returned.\n\n   The query list form is [:find ?var1 ?var2 ...\n                           :with ?var3 ...\n                           :in $src1 $src2 ...\n                           :where clause1 clause2 ...]\n   The query list form is converted into the map form internally.\n\n   The query string form is a string which, when read, results\n   in a query list form or query map form.\n\n   Query parse results are cached.\n\n   Returns a data structure based on the find specification passed in.\n   See https://docs.datomic.com/query/query-data-reference.html#find-specs.",
                      #'transact
                      "Given a connection and a set of information (tx-data), submits\n         a transaction, blocking until a result is available. d/transact\n         updates the connection's shared reference to the value of the\n         database by swapping in the result of d/with.\n\n         Returns a completed future. See d/with for a description of\n         tx-data and the return map that will be placed in the future.\n\n         An exception indicates either that a transaction failed, or\n         that the result of the transaction is not known after a\n         communication failure. For detailed information on programmatic\n         error handling, see https://docs.datomic.com/api/error-handling.html.\n         Note that an exception may occur either when invoking the API\n         or when dereferencing the returned reference.\n\n         Optional args:\n          :hints opaque value, used to optimize transaction performance.\n          See d/with or https://docs.datomic.com/reference/hints.html.\n\n          :io-context a qualified keyword\n          When provided, the return map will have an additional :io-stats key describing\n          information about the I/O reads performed by the transaction, if the connection\n          supports io-stats.\n          See https://docs.datomic.com/reference/io-stats.html.",
                      #'transact-async "Same as transact, but returns its future immediately.",
                      #'sync-excise
                      "Used to coordinate with background excision. Returns a\n    future that will acquire a database value that is aware of\n    excisions through time <= t.\n\n    Does not communicate with the transactor, so the future may be\n    available immediately.\n\n    The future can take arbitrarily long to complete.  Waiters\n    should specify a timeout.",
                      #'sync-schema
                      "Used to coordinate with background schema changes. Returns a\n    future that will acquire a database value that is aware of\n    all schema changes through time <= t.\n\n    Does not communicate with the transactor, so the future may be\n    available immediately.\n\n    The future can take arbitrarily long to complete.  Waiters\n    should specify a timeout.",
                      #'is-filtered "Returns true if db has had a filter set with filter",
                      #'release
                      "Request the release of resources associated with this connection.  \nMethod returns immediately, resources will be released \nasynchronously. This method should only be called when the entire \nprocess is no longer interested in the connection. Note\nthat Datomic connections do not adhere to an acquire/use/release \npattern.  They are thread-safe, cached, and long lived.  Many \nprocesses (e.g. application servers) will never call release.",
                      #'entid-at
                      "Returns a fabricated entity id in the supplied partition whose\n\t T component is at or after the supplied t. Entity ids sort by partition,\n\t then T component, such T components interleaving with transaction numbers.\n\t Thus this function can be used to fabricate a time-based entity id component for use\n\t in e.g. seek-datoms.",
                      #'next-t "Returns the t one beyond the highest reachable via this db value.",
                      #'attribute
                      "Returns information about the attribute with the given id or ident.\nSupports ILookup interface for key-based access. Supported keys are:\n\n:id, :ident, :cardinality, :value-type, :unique, :indexed, :has-avet,\n:no-history, :is-component, :fulltext\n",
                      #'db-stats
                      "Queries for database stats. Returns a map including at least:\n  :datoms  total count of datoms in the (history) database",
                      #'sync
                      "Used to coordinate with other peers.\n\n    When called with a t: returns a future that will acquire a\n    database value with basisT >= t. Does not communicate with the\n    transactor.\n\n    When called with no t: Returns a future that will acquire a\n    database value guaranteed to include all transactions that were\n    complete at the time sync was called.  Communicates with the\n    transactor.\n\n    db is the preferred way to get a database value, as it does not\n    need to wait nor block. Only use sync when coordination is\n    required, and prefer the two-argument version when you have a\n    basis t.\n\n    The future returned by sync can take arbitrarily long to\n    complete.  Waiters should use deref forms that specify a timeout.",
                      #'add-listener
                      "Register a completion listener for the future. The listener\n   will run once and only once, if and when the future's work is\n   complete. If the future has completed already, the listener will\n   run immediately.  Ordering of listeners is not guaranteed.",
                      #'query
                      "Executes the query described by query-map\n\n   query-map form is {:query query\n                      :args args\n                      :timeout time-in-milliseconds\n                      :io-context qualified-keyword}\n\n   The query parameter is the same format as described in q.\n\n   The args parameter is the same format as inputs described in q.\n\n   The optional timeout is the number of milliseconds after which a\n   query may be stopped. Note: timeout is approximate, it is meant to\n   protect against long running queries, but is not guaranteed to stop\n   after precisely the duration specified.\n\n   You can get information about the I/O reads performed by a query\n   with io-stats. To request io-stats, add :io-context (a qualified\n   keyword) to the map you use to call query. Query will return a map\n   with\n\n   :ret                the result of the query\n   :io-stats           io-stats for the query\n\n   The io-stats map includes:\n\n   :io-context         the io-context passed in\n   :api                :tx-with\n   :api-ms             msec to perform the API call\n   :reads              breakout of reads by cache tier and index sort\n   :nested             breakout iff nested queries set :io-context\n\n   See https://docs.datomic.com/reference/io-stats.html.",
                      #'tx-report-queue
                      "Gets the data queue associated with this connection, creating one\n   if necessary. At any point in time either zero or one queue is\n   associated with a connection. The returned queue may be consumed\n   from more than one thread. Note that the returned queue does not\n   block producers, and will consume memory until you consume the\n   elements from it. Reports will be added to the queue at some point\n   after the db has been updated. If this connection originated the\n   transaction, the transaction future will be notified first, before\n   a report is placed on the queue.\n\n   Reports are records with the following keys:\n\n     :db-before    value of database before the transaction\n     :db-after     value of database after the transaction\n     :tx-data      the transaction data in E/A/V/Tx form.",
                      #'get-database-names
                      "Returns a list of database names. URI is a database URI as\n   described under the connect documentation, but with a '*' where the\n   database name would be. For instance: datomic:dev://{transactor-host}:{port}/*.\n   When using the map form, :db-name should be omitted.",
                      #'connect
                      "Connects to the specified database, returing a Connection.\n   URI syntax ({} indicate place holders to fill in, [] indicate optional):\n\n   DynamoDB using roles:\n   datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}\n\n   DynamoDB using keys (use roles if possible):\n   datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}?aws_access_key_id={XXX}&aws_secret_key={YYY}\n\n   DynamoDB Local\n   datomic:ddb-local://{endpoint:port}/{dynamodb-table}/{db-name}?aws_access_key_id={XXX}&aws_secret_key={YYY}\n\n   Couchbase:\n   datomic:couchbase://{host}/{bucket}/{dbname}[?password={xxx}]\n\n   SQL:\n   datomic:sql://{db-name}?{jdbc-url}\n\n   Infinispan:\n   datomic:inf://{cluster-member-host}:{port}/{db-name}\n\n   Cassandra:\n   datomic:cass://{cluster-member-host}[:{port}]/{keyspace}.{table}/{db-name}[?user={user}&password={pwd}][&ssl=true]\n\n   Cassandra3:\n   datomic:cass3://{cluster-member-host}[:{port}]/{keyspace}.{table}/{db-name}[?user={user}&password={pwd}][&ssl=true][&local-datacenter=datacenter1]\n\n   Dev Appliance: \n   datomic:dev://{transactor-host}:{port}/{db-name}[?password={password}]\n\n   Free transactor integrated storage:\n   datomic:free://{transactor-host}:{port}/{db-name}[?password={password}]\n\n   In-process Memory:\n   datomic:mem://{db-name}\n\n   Note that URIs must be percent encoded and db-name cannot contain the following characters: / \" * : = ?\n\n   The dev and free protocols use additional ports to communicate with\n   storage.  By default, this ports is one higher than the specified\n   transactor port. You can override the default by specifying h2-port\n   in the query string, e.g.\n\n     datomic:dev://localhost:4334/mydb?h2-port=6000\n\n   The sql protocol also supports a map format instead of the URI\n   string. This is to enable specifying objects that can't be\n   embedded in URI strings, like DataSources. The format for the\n   SQL map is:\n\n     {:protocol :sql                  ;; keyword or string\n      :db-name \"myDb\"               ;; keyword or string\n\n      :data-source aDataSourceObject\n       ;; OR\n      :factory aCallableReturningConnection}\n\n   Note only one of data-source or factory should be supplied.\n\n   The cass protocol also supports a map format instead of the URI\n   string. This is to enable specifying objects that can't be embedded\n   in URI strings. The format for the Cassandra map is:\n\n     {:protocol :cass                 ;; keyword or string\n      :db-name \"myDb\"               ;; keyword or string\n      :table \"myKeyspace.myTable\"\n      :cluster aClusterObject}\n\n   Note that aClusterObject must be an instance of type\n   com.datastax.driver.core.Cluster.\n\n   The cass3 protocol also supports a map format instead of the URI\n   string. This is to enable specifying objects that can't be embedded\n   in URI strings. The format for the Cassandra map is:\n\n     {:protocol :cass                 ;; keyword or string\n      :db-name \"myDb\"               ;; keyword or string\n      :table \"myKeyspace.myTable\"\n      :session aSessionObject}\n\n   Note that aSessionObject must be an instance of type\n   com.datastax.oss.driver.api.core.cql.SyncCqlSession.\n\n   Datomic connections do not adhere to an acquire/use/release\n   pattern. They are thread-safe and long lived. Connections are\n   cached such that calling datomic.api/connect multiple times with\n   the same database value will return the same connection object.",
                      #'with
                      "d/with is a pure function that takes a database value and a\n         set of information (tx-data; held to be true at a point in time),\n         and returns a new database value that includes, via accretion,\n         that new information.\n\n         The tx-data argument is semantically an unordered set of information.\n         Syntactically it is a list that can include primitive assertions,\n         entity maps, and transaction functions.\n         See https://docs.datomic.com/transactions/transaction-data-reference.html.\n\n         If the tx-data is valid, returns a map containing the following keys:\n          :db-before  database value before the transaction\n          :db-after   database value after the transaction\n          :tx-data    collection of Datoms produced by the transaction\n          :tempids    argument to resolve-tempids\n\n         See d/datoms for a description of :tx-data.\n\n         Optional args:\n          :io-context a qualified keyword\n          When provided, the return map will have an additional :io-stats key describing\n          information about the I/O reads performed by the call to d/with.\n          For more detail, see https://docs.datomic.com/reference/io-stats.html.\n\n          :return-hints true\n          When provided, the return map may also include an additional key :hints,\n          an opaque value that can be passed as an option to d/transact or d/transact-async\n          to optimize performance.\n          For more detail, see https://docs.datomic.com/reference/hints.html.",
                      #'since-t "Returns the since point, or nil if none",
                      #'shutdown
                      "Shutdown all peer resources.  This method should be called as\npart of clean shutdown of a JVM process.  Will release all Connections,\nand, if shutdown-clojure is true, will release Clojure resources.\nPrograms written in Clojure can set shutdown-clojure to false if they\nmanage Clojure resources (e.g. agents) outside of Datomic; programs\nwritten in other JVM languages should typically set shutdown-clojure\nto true.",
                      #'delete-database
                      "Deletes the database specified by uri. Returns true if the\n   delete occurred. See connect for a description of the URI\n   syntax.",
                      #'implicit-part
                      "Returns the implicit partition (an entity id) corresponding to the given id, where 0<=id<524288.",
                      #'rename-database
                      "Renames the database specified by uri to new-name. Returns\n   true if rename succeeded. See connect for a description of the\n   URI syntax.",
                      #'basis-t
                      "Returns the t of the most recent transaction reachable via this db value.",
                      #'ident
                      "Returns the keyword associated with an id, or the key itself if passed.",
                      #'datoms
                      "Raw access to the index data, by index. The index must be supplied,\n   and, optionally, one or more leading components of the index can be\n   supplied to narrow the result.\n\n\t :eavt and :aevt indexes will contain all datoms\n\t :avet contains datoms for attributes where :db/index = true.\n\t :vaet contains datoms for attributes of :db.type/ref\n         :vaet is the reverse index\n\n   Returns a java.lang.Iterable of datoms. Datoms are associative and indexed:\n\n   Key     Index        Value\n   --------------------------\n   :e      0            entity id\n   :a      1            attribute id\n   :v      2            value\n   :tx     3            transaction id\n   :added  4            boolean add/retract",
                      #'log "Retrieves a value of the log for use in tx-range or query.",
                      #'squuid-time-millis
                      "get the time part of a squuid (a UUID created by squuid), in\nthe format of System.currentTimeMillis",
                      #'resolve-tempid
                      "Resolve a tempid to the actual id assigned in a database. The\ntempids object must come from the :tempids member returned through\ntransact or transact-async.",
                      #'tx-range
                      "Returns a range of transactions in log, starting at start,\nor from beginning if start is nil, and ending before end, or through\nend of log if end is nil. start and end can be can be a transaction\nnumber, transaction ID, Date or nil.\n\nEach transaction is a map with the following keys:\n :t - the T point of the transaction\n :data -  a Collection of the Datoms asserted/retracted by the transaction",
                      #'tx->t "Return the t value associated with a transaction id.",
                      #'index-range
                      "Returns an Iterable range of datoms in index named by attrid,\nstarting at start, or from beginning if start is nil, and ending\nbefore end, or through end of attr index if end is nil.\n\nSee datoms for a description of the returned value.",
                      #'function
                      "Generates a function object given a map with required keys\n\n  :lang  - clojure or java\n  :params - a list of parameter names used in the code\n  :code - a string or data containing the code of the body\n\n  and optional keys\n\n  :imports - a list to be spliced into (import ...)\n  :requires - a list to be spliced into (require ...)\n\n  Clojure code should consist of a single expression in which\n  the params will be in scope.\n\n  Returns a function object that implements IFn, and is a record with\n  keys :lang, :params, :code, :imports, and :requires.",
                      #'index-pull
                      "Walks an index, pulling entities via :e if :avet or :v if :aevt,\n        using the selector, returning a lazy seq on the results.\n\n        :index     :avet or :aevt\n        :selector  a pull selector (see 'pull')\n        :start     A vector in the same order as the index indicating\n                   the initial position. At least :a must be specified.\n                   Iteration is limited to datoms matching :a.\n        :reverse   optional, when true iterate the index in reverse\n                   order",
                      #'entity-db "Returns the database value that is the basis for this entity",
                      #'touch
                      "Touches all of the attributes of the entity, including any component entities recursively.\n    Returns the entity.",
                      #'filter
                      "Returns the value of the database containing only datoms\nsatisfying the predicate. the predicate will be passed two arguments -\nthe unfiltered db and a Datom. Chained calls compose the predicate with\n'and'",
                      #'pull "Like pull-many, but takes a single eid.",
                      #'implicit-part-id
                      "Returns the id of the given implicit partition, where 0<=id<524288. Returns nil when arg not an implicit partition.",
                      #'gc-storage "Allow storage to reclaim garbage older than a certain age.",
                      #'request-index
                      "Schedules a re-index of the database. The re-indexing happens\n   asynchronously. Returns true if re-index is scheduled."})
         chunk_15853 nil
         count_15854 0
         i_15855 0]
    (if (< i_15855 count_15854)
      (let [vec__15857 (.nth ^clojure.lang.Indexed chunk_15853 (int i_15855))
            v (nth vec__15857 (int 0) nil)
            doc (nth vec__15857 (int 1) nil)]
        (alter-meta! v assoc :doc doc)
        (recur seq_15852 chunk_15853 count_15854 (inc i_15855)))
      (let [temp__5804__auto__ (seq seq_15852)]
        (when temp__5804__auto__
          (let [seq_15852 temp__5804__auto__]
            (if (chunked-seq? seq_15852)
              (let [c__6065__auto__ (chunk-first seq_15852)]
                (recur
                  (chunk-rest seq_15852)
                  c__6065__auto__
                  (int (count c__6065__auto__))
                  (int 0)))
              (let [vec__15860 (first seq_15852)
                    v (nth vec__15860 (int 0) nil)
                    doc (nth vec__15860 (int 1) nil)]
                (alter-meta! v assoc :doc doc)
                (recur (next seq_15852) nil 0 0)))))))))
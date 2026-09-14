---
title: "datomic.api - Datomic Clojure API documentation"
source: "https://docs.datomic.com/clojure/index.html"
word_count: 3622
---

## API for datomic.api - Datomic Clojure

  
Full namespace name: datomic.api

## Overview

## Public Variables and Functions

---

## add-listener

function  
```
Usage: (add-listener fut f executor)
```
```
Register a completion listener for the future. The listener
will run once and only once, if and when the future's work is
complete. If the future has completed already, the listener will
run immediately.  Ordering of listeners is not guaranteed.
```

---

## administer-system

function  
```
Usage: (administer-system options)
```
```
Administer system. Takes an options map with a required :action key.
Throws on failure. Actions include:

Release Object Cache
:action      :release-object-cache

Effect: Clear all entries from the Object Cache.

Upgrade Schema
:action      :upgrade-schema
:uri         a URI as per connect

Effect: Upgrades the base schema of a database to the latest version.
NOTE: Read https://docs.datomic.com/operation/deployment.html before calling.
```

---

## as-of

function  
```
Usage: (as-of db t)
```
```
Returns the value of the database as of some point t, inclusive.
t can be a transaction number, transaction ID, or Date.
```

---

## as-of-t

function  
```
Usage: (as-of-t db)
```
```
Returns the as-of point, or nil if none
```

---

## attribute

function  
```
Usage: (attribute db attrid)
```
```
Returns information about the attribute with the given id or ident.
Supports ILookup interface for key-based access. Supported keys are:

:id, :ident, :cardinality, :value-type, :unique, :indexed, :has-avet,
:no-history, :is-component, :fulltext
```

---

## basis-t

function  
```
Usage: (basis-t db)
```
```
Returns the t of the most recent transaction reachable via this db value.
```

---

## cancel

function  
```
Usage: (cancel {:keys [cognitect.anomalies/category], :as anomaly-map})
```
```
Cancels the current Datomic operation (query or transaction).

Throws an ex-info with an anomaly to the original caller.

anomaly-map is an anomaly as described by https://github.com/cognitect-labs/anomalies.

:cognitect.anomalies/category is a required key, valid values are:

  :cognitect.anomalies/incorrect
  :cognitect.anomalies/conflict

When :cognitect.anomalies/message is provided, the message will be used as the Exception's detail message

Other keys should be namespace-qualified.

All data passed to cancel must be fressian-serializable.
```

---

## connect

function  
```
Usage: (connect uri)
```
```
Connects to the specified database, returning a Connection.
URI syntax ({} indicate place holders to fill in, [] indicate optional):

DynamoDB using roles:
datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}

DynamoDB using keys (use roles if possible):
datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}?aws_access_key_id={XXX}&aws_secret_key={YYY}

DynamoDB Local:
datomic:ddb-local://{endpoint:port}/{dynamodb-table}/{db-name}?aws_access_key_id={XXX}&aws_secret_key={YYY}

Couchbase:
datomic:couchbase://{host}/{bucket}/{dbname}[?password={xxx}]

SQL:
datomic:sql://{db-name}?{jdbc-uri}
datomic:sql://{db-name}?{query-string}#{jdbc-uri}

Infinispan:
datomic:inf://{cluster-member-host}:{port}/{db-name}

Cassandra:
datomic:cass://{cluster-member-host}[:{port}]/{keyspace}.{table}/{db-name}[?user={user}&password={pwd}][&ssl=true]

Cassandra3:
datomic:cass3://{cluster-member-host}[:{port}]/{keyspace}.{table}/{db-name}[?user={user}&password={pwd}][&ssl=true][&local-datacenter=datacenter1]

Backups:
datomic:backup:{backup-uri}[?t={backup-t}]
Backup connections will read the latest backup unless given a t parameter. Connections to
backups are always read-only, supporting only d/db and d/log. See also d/list-backups.

Dev Appliance:
datomic:dev://{transactor-host}:{port}/{db-name}[?password={password}]

Free transactor integrated storage:
datomic:free://{transactor-host}:{port}/{db-name}[?password={password}]

In-process Memory:
datomic:mem://{db-name}

Note that query param values must be URL-encoded, and db-name cannot contain the following characters: / " * : = ?

The dev and free protocols use additional ports to communicate with
storage.  By default, this ports is one higher than the specified
transactor port. You can override the default by specifying h2-port
in the query string, e.g.

  datomic:dev://localhost:4334/mydb?h2-port=6000

The sql protocol also supports a map format instead of the URI
string. This is to enable specifying objects that can't be
embedded in URI strings, like DataSources. The format for the
SQL map is:

  {:protocol :sql                  ;; keyword or string
   :db-name "myDb"               ;; keyword or string

   :data-source aDataSourceObject
    ;; OR
   :factory aCallableReturningConnection}

Note only one of data-source or factory should be supplied.

The cass protocol also supports a map format instead of the URI
string. This is to enable specifying objects that can't be embedded
in URI strings. The format for the Cassandra map is:

  {:protocol :cass                 ;; keyword or string
   :db-name "myDb"               ;; keyword or string
   :table "myKeyspace.myTable"
   :cluster aClusterObject}

Note that aClusterObject must be an instance of type
com.datastax.driver.core.Cluster.

The cass3 protocol also supports a map format instead of the URI
string. This is to enable specifying objects that can't be embedded
in URI strings. The format for the Cassandra map is:

  {:protocol :cass                 ;; keyword or string
   :db-name "myDb"               ;; keyword or string
   :table "myKeyspace.myTable"
   :session aSessionObject}

Note that aSessionObject must be an instance of type
com.datastax.oss.driver.api.core.cql.SyncCqlSession.

d/connect returns a read-only connection when given a URI with
query param read-only=true. These connections do not require a
running transactor, and support only d/db and d/log APIs, which
will always return the same value read at connection time.

Datomic connections do not adhere to an acquire/use/release
pattern. They are thread-safe and long lived. Connections are
cached such that calling datomic.api/connect multiple times with
the same database URI will return the same connection object.
Read-only connections are not cached.
```

---

## create-database

function  
```
Usage: (create-database uri)
```
```
Creates database specified by uri. Returns true if the
database was created, false if it already exists. See connect
for a description of the URI syntax.
```

---

## datoms

function  
```
Usage: (datoms db index & components)
```
```
Raw access to the index data, by index. The index must be supplied,
and, optionally, one or more leading components of the index can be
supplied to narrow the result.

:eavt and :aevt indexes will contain all datoms
:avet contains datoms for attributes where :db/index = true.
:vaet contains datoms for attributes of :db.type/ref
      :vaet is the reverse index

Returns a java.lang.Iterable of datoms. Datoms are associative and indexed:

Key     Index        Value
--------------------------
:e      0            entity id
:a      1            attribute id
:v      2            value
:tx     3            transaction id
:added  4            boolean add/retract
```

---

## db

function  
```
Usage: (db connection)
```
```
Retrieves a value of the database for reading. Does not
communicate with the transactor, nor block.
```

---

## db-stats

function  
```
Usage: (db-stats db)
```
```
Queries for database stats. Returns a map including at least:
:datoms  total count of datoms in the (history) database
```
Added in Datomic Clojure version 1.0.6333

---

## delete-database

function  
```
Usage: (delete-database uri)
```
```
Deletes the database specified by uri. Returns true if the
delete occurred. See connect for a description of the URI
syntax.
```

---

## entid

function  
```
Usage: (entid db ident)
```
```
Returns the entity id associated with a symbolic keyword, or the id
itself if passed.
```

---

## entid-at

function  
```
Usage: (entid-at db part t-or-date)
```
```
Returns a fabricated entity id in the supplied partition whose
T component is at or after the supplied t. Entity ids sort by partition,
then T component, such T components interleaving with transaction numbers.
Thus this function can be used to fabricate a time-based entity id component for use
in e.g. seek-datoms.
```

---

## entity

function  
```
Usage: (entity db eid)
```
```
Returns a dynamic map of the entity's attributes for the given id, ident or lookup ref.
Entities implement
  clojure.lang.Associative
  clojure.lang.ILookup
  clojure.lang.IPersistentCollection
  clojure.lang.Seqable
  datomic.Entity
```

---

## entity-db

function  
```
Usage: (entity-db entity)
```
```
Returns the database value that is the basis for this entity
```

---

## filter

function  
```
Usage: (filter db pred)
```
```
Returns the value of the database containing only datoms
satisfying the predicate. the predicate will be passed two arguments -
the unfiltered db and a Datom. Chained calls compose the predicate with
'and'
```

---

## function

function  
```
Usage: (function m)
```
```
Generates a function object given a map with required keys

:lang  - clojure or java
:params - a list of parameter names used in the code
:code - a string or data containing the code of the body

and optional keys

:imports - a list to be spliced into (import ...)
:requires - a list to be spliced into (require ...)

Clojure code should consist of a single expression in which
the params will be in scope.

Returns a function object that implements IFn, and is a record with
keys :lang, :params, :code, :imports, and :requires.
```

---

## gc-storage

function  
```
Usage: (gc-storage connection older-than)
```
```
Allow storage to reclaim garbage older than a certain age.
```

---

## get-database-names

function  
```
Usage: (get-database-names uri)
```
```
Returns a list of database names. URI is a database URI as
described under the connect documentation, but with a '*' where the
database name would be. For instance: datomic:dev://{transactor-host}:{port}/*.
When using the map form, :db-name should be omitted.
```

---

## history

function  
```
Usage: (history db)
```
```
Returns a special database containing all assertions and
retractions across time. This special database can be used for
datoms and index-range calls and queries, but not for entity or
with calls. as-of and since bounds are also supported. Note that
queries will get all of the additions and retractions, which can be
distinguished by the fifth datom field :added (true for add/assert)
[e a v tx added]
```

---

## ident

function  
```
Usage: (ident db eid)
```
```
Returns the keyword associated with an id, or the key itself if passed.
```

---

## implicit-part

function  
```
Usage: (implicit-part id)
```
```
Returns the implicit partition (an entity id) corresponding to the given id, where 0<=id<524288.
```

---

## implicit-part-id

function  
```
Usage: (implicit-part-id part)
```
```
Returns the id of the given implicit partition, where 0<=id<524288. Returns nil when arg not an implicit partition.
```

---

## index-pull

function  
```
Usage: (index-pull db arg-map)
```
```
Walks an index, pulling entities via :e if :avet or :v if :aevt,
using the selector, returning a lazy seq on the results.

:index     :avet or :aevt
:selector  a pull selector (see 'pull')
:start     A vector in the same order as the index indicating
           the initial position. At least :a must be specified.
           Iteration is limited to datoms matching :a.
:reverse   optional, when true iterate the index in reverse
           order
```

---

## index-range

function  
```
Usage: (index-range db attrid start end)
```
```
Returns an Iterable range of datoms in index named by attrid,
starting at start, or from beginning if start is nil, and ending
before end, or through end of attr index if end is nil.

See datoms for a description of the returned value.
```

---

## invoke

function  
```
Usage: (invoke db eid-or-ident & args)
```
```
Lookup the database function named by eid-or-ident, and call it with args.
```

---

## is-filtered

function  
```
Usage: (is-filtered db)
```
```
Returns true if db has had a filter set with filter
```

---

## list-backups

function  
```
Usage: (list-backups backup-uri)
```
```
Lists all points in time available at the given backup-uri. Returns a map
with :backups, sorted descending by t.
Each backup contains:
:t            the basis t of the backup
:connect-uri  a URI that can be passed to d/connect
```

---

## log

function  
```
Usage: (log connection)
```
```
Retrieves a value of the log for use in tx-range or query.
```

---

## next-t

function  
```
Usage: (next-t db)
```
```
Returns the t one beyond the highest reachable via this db value.
```

---

## part

function  
```
Usage: (part eid)
```
```
Return the partition associated with an entity id.
```

---

## pull

function  
```
Usage: (pull db pattern eid & {:as options})
```
```
Like pull-many, but takes a single eid.
```

---

## pull-many

function  
```
Usage: (pull-many db pattern eids & {:as options})
```
```
Returns hierarchical selections of attributes for eids.
See https://docs.datomic.com/query/query-pull.html for more information.

You can get information about the I/O reads performed by a
pull with io-stats. To request io-stats, pass :io-context
(a qualified keyword) as an option to pull-many. The return
value will be a map with

:ret                the result of the pull
:io-stats           io-stats for the pull

The io-stats map includes:

:io-context         the io-context passed in
:api                :tx-with
:api-ms             msec to peform the API call
:reads              breakout of reads by cache tier and index sort
:nested             breakout iff nested queries set :io-context

See https://docs.datomic.com/reference/io-stats.html.
```

---

## q

function  
```
Usage: (q query & inputs)
```
```
Executes a query against inputs.

 Inputs are data sources e.g. a database value retrieved from
 Connection.db, a list of lists, and/or rules. If only one data
 source is provided, no :in section is required, else the :in
 section describes the inputs.

 query can be a map, list, or string:

 The query map form is {:find vars-and-aggregates
                        :with vars-included-but-not-returned
                        :in sources
                        :where clauses}
 where vars, sources and clauses are lists.

:with is optional, and names vars to be kept in the aggregation set but
 not returned.

 The query list form is [:find ?var1 ?var2 ...
                         :with ?var3 ...
                         :in $src1 $src2 ...
                         :where clause1 clause2 ...]
 The query list form is converted into the map form internally.

 The query string form is a string which, when read, results
 in a query list form or query map form.

 Query parse results are cached.

 Returns a data structure based on the find specification passed in.
 See https://docs.datomic.com/query/query-data-reference.html#find-specs.
```

---

## qseq

function  
```
Usage: (qseq query-map)
```
```
Performs the query described by query-map (as per 'query'),
returning a lazy seq on the results.  Item transformations such as
'pull' are deferred until the seq is consumed. For queries with
pull(s), this results in:

* reduced memory use and the ability to execute larger queries
* lower latency before the first results are returned

The returned seq object efficiently supports 'count'.
```

---

## query

function  
```
Usage: (query query-map)
```
```
Executes the query described by query-map

query-map form is {:query query
                   :args args
                   :timeout time-in-milliseconds
                   :io-context qualified-keyword}

The query parameter is the same format as described in q.

The args parameter is the same format as inputs described in q.

The optional timeout is the number of milliseconds after which a
query may be stopped. Note: timeout is approximate, it is meant to
protect against long running queries, but is not guaranteed to stop
after precisely the duration specified.

You can get information about the I/O reads performed by a query
with io-stats. To request io-stats, add :io-context (a qualified
keyword) to the map you use to call query. Query will return a map
with

:ret                the result of the query
:io-stats           io-stats for the query

The io-stats map includes:

:io-context         the io-context passed in
:api                :tx-with
:api-ms             msec to perform the API call
:reads              breakout of reads by cache tier and index sort
:nested             breakout iff nested queries set :io-context

See https://docs.datomic.com/reference/io-stats.html.
```

---

## release

function  
```
Usage: (release conn)
```
```
Request the release of resources associated with this connection.
Method returns immediately, resources will be released
asynchronously. This method should only be called when the entire
process is no longer interested in the connection. Note
that Datomic connections do not adhere to an acquire/use/release
pattern.  They are thread-safe, cached, and long lived.  Many
processes (e.g. application servers) will never call release.
```
Added in Datomic Clojure version 0.8.3861

---

## remove-tx-report-queue

function  
```
Usage: (remove-tx-report-queue connection)
```
```
Removes the queue associated with this connection.
```

---

## rename-database

function  
```
Usage: (rename-database uri new-name)
```
```
Renames the database specified by uri to new-name. Returns
true if rename succeeded. See connect for a description of the
URI syntax.
```

---

## request-index

function  
```
Usage: (request-index connection)
```
```
Schedules a re-index of the database. The re-indexing happens
asynchronously. Returns true if re-index is scheduled.
```

---

## resolve-tempid

function  
```
Usage: (resolve-tempid db tempids tempid)
```
```
Resolve a tempid to the actual id assigned in a database. The
tempids object must come from the :tempids member returned through
transact or transact-async.
```

---

## rseek-datoms

function  
```
Usage: (rseek-datoms db index & components)
```
```
Like seek-datoms, but iterates the index in reverse, beginning at or before the point where the given
components would reside.

Only terminates at the start of the index, thus callers must supply their own termination logic.
```

---

## seek-datoms

function  
```
Usage: (seek-datoms db index & components)
```
```
Raw access to the index data, by index. The index must be supplied,
and, optionally, one or more leading components of the index can be supplied for the initial search.
Note that, unlike the datoms function, there need not be an exact match on the supplied components.
The iteration will begin at or after the point in the index where the components would reside.
Further, the iteration is not bound by the supplied components, and will only terminate
at the end of the index. Thus you will have to supply your own termination logic, as you rarely
want the entire index. As such, seek-datoms is for more advanced applications, and datoms should be preferred
wherever it is adequate. See also - entid-at.

      :eavt and :aevt indexes will contain all datoms
      :avet contains datoms for attributes where :db/index = true.
      :vaet contains datoms for attributes of :db.type/ref
      :vaet is the reverse index

      See datoms for a description of the returned value.
```

---

## shutdown

function  
```
Usage: (shutdown shutdown-clojure)
```
```
Shutdown all peer resources.  This method should be called as
part of clean shutdown of a JVM process.  Will release all Connections,
and, if shutdown-clojure is true, will release Clojure resources.
Programs written in Clojure can set shutdown-clojure to false if they
manage Clojure resources (e.g. agents) outside of Datomic; programs
written in other JVM languages should typically set shutdown-clojure
to true.
```
Added in Datomic Clojure version 0.8.3861

---

## since

function  
```
Usage: (since db t)
```
```
Returns the value of the database since some point t, exclusive
t can be a transaction number, transaction ID, or Date.
```

---

## since-t

function  
```
Usage: (since-t db)
```
```
Returns the since point, or nil if none
```

---

## squuid

function  
```
Usage: (squuid)
```
```
Constructs a semi-sequential UUID. Useful for creating UUIDs
that don't fragment indexes. Returns a UUID whose most significant
32 bits are currentTimeMillis rounded to seconds.
```

---

## squuid-time-millis

function  
```
Usage: (squuid-time-millis squuid)
```
```
get the time part of a squuid (a UUID created by squuid), in
the format of System.currentTimeMillis
```

---

## sync

function  
```
Usage: (sync connection)
       (sync connection t)
```
```
Used to coordinate with other peers.

When called with a t: returns a future that will acquire a
database value with basisT >= t. Does not communicate with the
transactor.

When called with no t: Returns a future that will acquire a
database value guaranteed to include all transactions that were
complete at the time sync was called.  Communicates with the
transactor.

db is the preferred way to get a database value, as it does not
need to wait nor block. Only use sync when coordination is
required, and prefer the two-argument version when you have a
basis t.

The future returned by sync can take arbitrarily long to
complete.  Waiters should use deref forms that specify a timeout.
```

---

## sync-excise

function  
```
Usage: (sync-excise connection t)
```
```
Used to coordinate with background excision. Returns a
future that will acquire a database value that is aware of
excisions through time <= t.

Does not communicate with the transactor, so the future may be
available immediately.

The future can take arbitrarily long to complete.  Waiters
should specify a timeout.
```

---

## sync-index

function  
```
Usage: (sync-index connection t)
```
```
Used to coordinate with background indexing jobs. Returns a
future that will acquire a database value that is indexed
through time <= t.

Does not communicate with the transactor, so the future may be
available immediately.

The future can take arbitrarily long to complete.  Waiters
should specify a timeout.
```

---

## sync-schema

function  
```
Usage: (sync-schema connection t)
```
```
Used to coordinate with background schema changes. Returns a
future that will acquire a database value that is aware of
all schema changes through time <= t.

Does not communicate with the transactor, so the future may be
available immediately.

The future can take arbitrarily long to complete.  Waiters
should specify a timeout.
```

---

## t-\>tx

function  
```
Usage: (t->tx t)
```
```
Return the transaction id associated with a t value.
```

---

## tempid

function  
```
Usage: (tempid partition)
       (tempid partition n)
```
```
Generate a tempid in the specified partition. Within the scope
of a single transaction, tempids map consistently to permanent
ids. Values of n from -1 to -1000000, inclusive, are reserved for
user-created tempids.
```

---

## touch

function  
```
Usage: (touch entity)
```
```
Touches all of the attributes of the entity, including any component entities recursively.
Returns the entity.
```

---

## transact

function  
```
Usage: (transact connection tx-data & {:as options})
```
```
Given a connection and a set of information (tx-data), submits
a transaction, blocking until a result is available. d/transact
updates the connection's shared reference to the value of the
database by swapping in the result of d/with.

Returns a completed future. See d/with for a description of
tx-data and the return map that will be placed in the future.

An exception indicates either that a transaction failed, or
that the result of the transaction is not known after a
communication failure. For detailed information on programmatic
error handling, see https://docs.datomic.com/api/error-handling.html.
Note that an exception may occur either when invoking the API
or when dereferencing the returned reference.

See https://docs.datomic.com/reference/io-stats.html for how
to get information about I/O reads performed by a transaction
if the connection supports io-stats.
```

---

## transact-async

function  
```
Usage: (transact-async connection tx-data & {:as options})
```
```
Same as transact, but returns its future immediately.
```

---

## tx-\>t

function  
```
Usage: (tx->t tx)
```
```
Return the t value associated with a transaction id.
```

---

## tx-range

function  
```
Usage: (tx-range log start end)
```
```
Returns a range of transactions in log, starting at start,
or from beginning if start is nil, and ending before end, or through
end of log if end is nil. start and end can be can be a transaction
number, transaction ID, Date or nil.

Each transaction is a map with the following keys:
 :t - the T point of the transaction
 :data -  a Collection of the Datoms asserted/retracted by the transaction
```

---

## tx-report-queue

function  
```
Usage: (tx-report-queue connection)
```
```
Gets the data queue associated with this connection, creating one
if necessary. At any point in time either zero or one queue is
associated with a connection. The returned queue may be consumed
from more than one thread. Note that the returned queue does not
block producers, and will consume memory until you consume the
elements from it. Reports will be added to the queue at some point
after the db has been updated. If this connection originated the
transaction, the transaction future will be notified first, before
a report is placed on the queue.

Reports are records with the following keys:

  :db-before    value of database before the transaction
  :db-after     value of database after the transaction
  :tx-data      the transaction data in E/A/V/Tx form.
```

---

## with

function  
```
Usage: (with db tx-data)
```
```
d/with is a pure function that takes a database value and a
set of information (tx-data; held to be true at a point in time),
and returns a new database value that includes, via accretion,
that new information.

The tx-data argument is semantically an unordered set of information.
Syntactically it is a list that can include primitive assertions,
entity maps, and transaction functions.
See https://docs.datomic.com/transactions/transaction-data-reference.html.

If the tx-data is valid, returns a map containing the following keys:
 :db-before  database value before the transaction
 :db-after   database value after the transaction
 :tx-data    collection of Datoms produced by the transaction
 :tempids    argument to resolve-tempids

See d/datoms for a description of :tx-data.

Optional args:
 :io-context a qualified keyword
 When provided, the return map will have an additional :io-stats key describing
 information about the I/O reads performed by the call to d/with.
 For more detail, see https://docs.datomic.com/reference/io-stats.html.

 :return-hints true
 When provided, the return map may also include an additional key :hints,
 an opaque value that can be passed as an option to d/transact or d/transact-async
 to optimize performance.
 For more detail, see https://docs.datomic.com/reference/hints.html.
```

Logo & site design by [Tom Hickey](http://www.tomhickey.com/ "Visit Tom Hickey's website.").  
Clojure auto-documentation system by Tom Faulhaber.
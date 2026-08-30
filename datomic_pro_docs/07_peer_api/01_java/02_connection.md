---
title: "Connection (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Connection.html"
language: "en"
description: "declaration: package: datomic, interface: Connection"
word_count: 1168
---

public interface Connection

A connection to a database for submitting and monitoring transactions, and retrieving the current value of the database.

- ## Field Summary
	Fields
	Modifier and Type
	Field
	Description
	`static final Object`
	`DB_AFTER`
	`static final Object`
	`DB_BEFORE`
	`static final Object`
	`TEMPIDS`
	`static final Object`
	`TX_DATA`
- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`Database`
	`db()`
	Retrieves the current database value.
	`void`
	`gcStorage(Date olderThan)`
	Reclaim storage garbage older than a certain age.
	`Log`
	`log()`
	Retrieves the current value of the log.
	`void`
	`release()`
	Request the release of resources associated with this connection.
	`void`
	`removeTxReportQueue()`
	Removes the queue associated with this connection.
	`boolean`
	`requestIndex()`
	Request that a [background indexing job](https://docs.datomic.com/operation/capacity.html#indexing) begin immediately.
	`ListenableFuture<Database>`
	`sync()`
	Retrieve a database value that includes all transactions completed at the time `sync` was called.
	`ListenableFuture<Database>`
	`sync(long t)`
	Retrieve a database value that includes all transactions completed up to and including time t.
	`ListenableFuture<Database>`
	`syncExcise(long t)`
	Retrieve a database value that is aware of all [excisions](https://docs.datomic.com/operation/excision.html) up to a [database t](https://docs.datomic.com/glossary.html#t).
	`ListenableFuture<Database>`
	`syncIndex(long t)`
	Retrieve a database value that is [indexed](https://docs.datomic.com/operation/capacity.html#indexing) through the [database t](https://docs.datomic.com/glossary.html#t) passed in.
	`ListenableFuture<Database>`
	`syncSchema(long t)`
	Retrieve a database value that is aware of all [schema changes](https://docs.datomic.com/schema/schema-change.html#schema-alteration) up to a [database t](https://docs.datomic.com/glossary.html#t).
	`ListenableFuture<Map>`
	`transact(List txData)`
	Submits a [transaction](https://docs.datomic.com/transactions/transactions.html), blocking until a result is available.
	`ListenableFuture<Map>`
	`transactAsync(List txData)`
	Like [`transact(java.util.List)`](#transact\(java.util.List\)), but returns immediately, with timeout logic left up to the caller.
	`BlockingQueue<Map>`
	`txReportQueue()`
	Gets the single transaction report queue associated with this connection, creating it if necessary.

- ## Method Details
	- ### requestIndex
		boolean requestIndex()
		Request that a [background indexing job](https://docs.datomic.com/operation/capacity.html#indexing) begin immediately.
		Background indexing will happen asynchronously. You can track indexing completion with [`syncIndex(long)`](#syncIndex\(long\)).
		Returns:
		true if indexing job successfully scheduled.
	- ### db
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") db()
		Retrieves the current database value. Does not communicate with the transactor, nor block.
		Returns:
		an immutable database value.
	- ### log
		[Log](https://docs.datomic.com/javadoc/datomic/Log.html "interface in datomic") log()
		Retrieves the current value of the log. Does not communicate with the transactor, nor block.
		Returns:
		an immutable log value
		Since:
		0.8.4122
	- ### sync
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") \> sync()
		Retrieve a database value that includes all transactions completed at the time `sync` was called.
		`sync` is a primitive for coordinating activity across peer processes. `sync` always communicates with the transactor, and should only be used when the following two conditions hold
		1. coordination is required
		2. peers have no way to agree on a basis t for coordination
		If you do not require coordination, prefer [`db()`](#db\(\)). If peers can share a known basis t, prefer [`sync(long)`](#sync\(long\)).
		The future returned by sync can take arbitrarily long to complete. Waiters should specify a timeout.
		Returns:
		a database future.
		Since:
		0.8.3993
	- ### sync
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") \> sync(long t)
		Retrieve a database value that includes all transactions completed up to and including time t.
		`sync` is a primitive for coordinating activity across peer processes. `sync` does not communicate with the transactor, but it can block if the peer has not yet been notified of transactions up to time t. If you do not require coordination, prefer [`db()`](#db\(\)). If peers do not share a basis t, prefer [`sync()`](#sync\(\)).
		The future returned by sync can take arbitrarily long to complete. Waiters should specify a timeout.
		Parameters:
		`t` - a [database t](https://docs.datomic.com/glossary.html#t).
		Returns:
		a database future.
		Since:
		0.8.3993
	- ### syncIndex
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") \> syncIndex(long t)
		Retrieve a database value that is [indexed](https://docs.datomic.com/operation/capacity.html#indexing) through the [database t](https://docs.datomic.com/glossary.html#t) passed in.
		Does not communicate with the transactor, so the future may be available immediately. The future can take arbitrarily long to complete. Waiters should specify a timeout.
		Parameters:
		`t` - a database t.
		Returns:
		a database future.
		Since:
		0.9.4470
	- ### syncSchema
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") \> syncSchema(long t)
		Retrieve a database value that is aware of all [schema changes](https://docs.datomic.com/schema/schema-change.html#schema-alteration) up to a [database t](https://docs.datomic.com/glossary.html#t).
		Does not communicate with the transactor, so the future may be available immediately. The future can take arbitrarily long to complete. Waiters should specify a timeout.
		Parameters:
		`t` - a database t.
		Returns:
		a database future.
		Since:
		0.9.4470
	- ### syncExcise
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") \> syncExcise(long t)
		Retrieve a database value that is aware of all [excisions](https://docs.datomic.com/operation/excision.html) up to a [database t](https://docs.datomic.com/glossary.html#t).
		Does not communicate with the transactor, so the future may be available immediately. The future can take arbitrarily long to complete. Waiters should specify a timeout.
		Parameters:
		`t` - a database t.
		Returns:
		a database future.
		Since:
		0.9.4470
	- ### transact
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") \> transact([List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") txData)
		Submits a [transaction](https://docs.datomic.com/transactions/transactions.html), blocking until a result is available.
		Parameters:
		`txData` - a list of data to be added, containing any combination of [assertions](https://docs.datomic.com/transactions/transactions.html#list-forms), [retractions](https://docs.datomic.com/transactions/transactions.html#retracting-data), [transaction functions](https://docs.datomic.com/reference/database-functions.html#transaction-functions), or [entity maps](https://docs.datomic.com/transactions/transactions.html#map-forms):
		| Type | Example |
		| --- | --- |
		| assertion | `[:db/add some-id :some-attr/name "Some attr value"]` |
		| retraction | `[:db/retract some-id :some-attr/name "Some attr value"]` |
		| transaction function | `[:some/fn-name args-for-fn ...]` |
		| entity map | `{:db/id some-id :some-attr/name "some value" :another-attr/name 42 ...}` |
		Returns:
		a future that can be used to monitor the completion of the transaction. If the transaction commits, the future's value is a map, with the following keys:
		| `DB_BEFORE` | Database value before the transaction |
		| --- | --- |
		| `DB_AFTER` | Database value after the transaction |
		| `TX_DATA` | Collection of [`Datom`](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic") s produced by the transaction |
		| `TEMPID` | Use with [`Peer.resolveTempid(datomic.Database, java.lang.Object, java.lang.Object)`](https://docs.datomic.com/javadoc/datomic/Peer.html#resolveTempid\(datomic.Database,java.lang.Object,java.lang.Object\)) to resolve temporary ids. |
		If the transaction aborts, attempts to get the future's value throw an ExecutionException, wrapping a Error containing error information. If the transaction times out, the call to transact itself will throw a RuntimeException. The transaction timeout can be set via the system property datomic.txTimeoutMsec, and defaults to 10000 (10 seconds).
		See Also:
	- ### transactAsync
		[ListenableFuture](https://docs.datomic.com/javadoc/datomic/ListenableFuture.html "interface in datomic") < [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") \> transactAsync([List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") txData)
		Like [`transact(java.util.List)`](#transact\(java.util.List\)), but returns immediately, with timeout logic left up to the caller.
		Parameters:
		`txData` - see `transact`
		Returns:
		see `transact`
	- ### txReportQueue
		[BlockingQueue](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/BlockingQueue.html "class or interface in java.util.concurrent") < [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") \> txReportQueue()
		Gets the single transaction report queue associated with this connection, creating it if necessary.
		The transaction report queue receives reports from all transactions in the system. Objects on the queue have the same keys as returned by [`transact(java.util.List)`](#transact\(java.util.List\)). The returned queue may be consumed from more than one thread. Note that the returned queue does not block producers, and will consume memory until you consume the elements from it. Reports will be added to the queue at some point after the db has been updated If this connection originated the transaction, the transaction future will be notified first, before a report is placed on the queue.
		Returns:
		a queue
	- ### removeTxReportQueue
		void removeTxReportQueue()
		Removes the queue associated with this connection.
	- ### gcStorage
		void gcStorage([Date](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Date.html "class or interface in java.util") olderThan)
		Reclaim storage garbage older than a certain age.
		As part of [capacity planning](https://docs.datomic.com/operation/capacity.html#garbage-collection) for a Datomic system, you should schedule regular (e.g daily, weekly) calls to `gcStorage`.
		Parameters:
		`olderThan` - limits how recent garbage may be collected
	- ### release
		void release()
		Request the release of resources associated with this connection.
		Method returns immediately, resources will be released asynchronously. This method should only be called when the entire process is no longer interested in the connection. Note that Datomic connections do not adhere to an acquire/use/release pattern. They are thread-safe, cached, and long lived. Many processes (e.g. application servers) will never call release.
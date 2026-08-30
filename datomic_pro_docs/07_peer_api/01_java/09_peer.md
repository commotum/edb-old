---
title: "Peer (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Peer.html"
language: "en"
description: "declaration: package: datomic, class: Peer"
word_count: 1799
---

[java.lang.Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")

datomic.Peer

---

public class Peer extends [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")

Main entry point, used to manage connections, submit transactions, and query.

- ## Method Summary
	All MethodsStatic MethodsConcrete Methods
	Modifier and Type
	Method
	Description
	`static Object`
	`administerSystem(Map options)`
	Administer a Datomic system.
	`static void`
	`cancel(Object anomaly)`
	Cancels the current Datomic operation (query or transaction).
	`static Connection`
	`connect(Object uriOrMap)`
	Connects to the specified database.
	`static boolean`
	`createDatabase(Object uriOrMap)`
	Creates a database with the given name.
	`static boolean`
	`deleteDatabase(Object uriOrMap)`
	Deletes a database.
	`static datomic.functions.Fn`
	`function(Map m)`
	Generates a function object given a map of:lang - clojure or java:params - a list of parameter names used in the code:code - a string containing the code of the function body.
	`static List<String>`
	`getDatabaseNames(Object uriOrMap)`
	Returns a list of database names.
	`static Map<Object,Object>`
	`listBackups(String backupUri)`
	Lists all points in time available at the given `backup-uri`.
	`static Object`
	`part(Object entityId)`
	Returns the partition of this [entity id](https://docs.datomic.com/schema/identity.html#entities).
	`static Collection<List<Object>>`
	`q(Object query,  Object... inputs)`
	Like [`query(Object, Object...)`](#query\(java.lang.Object,java.lang.Object...\)), but with a more specific return signature.
	`static Stream<Object>`
	`qseq(Object query,  Object... inputs)`
	Performs the query described by `query` and `inputs` (as per [`query(Object, Object...)`](#query\(java.lang.Object,java.lang.Object...\))), Item transformations such as pull are deferred until the Stream is consumed.
	`static <T> T`
	`query(QueryRequest queryRequest)`
	Like [`query(Object, Object...)`](#query\(java.lang.Object,java.lang.Object...\)), but accepts a [`QueryRequest`](https://docs.datomic.com/javadoc/datomic/QueryRequest.html "class in datomic") object.
	`static <T> T`
	`query(Object query,  Object... inputs)`
	Executes a [datalog query](https://docs.datomic.com/query/query.html).
	`static boolean`
	`renameDatabase(Object uriOrMap,  String newName)`
	Renames a database.
	`static Object`
	`resolveTempid(Database db,  Object tempids,  Object tempid)`
	Resolve a tempid to the actual id assigned in a database.
	`static void`
	`shutdown(boolean shutdownClojure)`
	Shutdown all peer resources.
	`static UUID`
	`squuid()`
	Constructs a semi-sequential UUID.
	`static long`
	`squuidTimeMillis(UUID squuid)`
	Get the time component of a squuid.
	`static Object`
	`tempid(Object partition)`
	Generates a temp id in the designated partition.
	`static Object`
	`tempid(Object partition,  long idNumber)`
	Generates a temp id in the designated partition.
	`static long`
	`toT(Object tx)`
	Returns the t value associated with this tx.
	`static Object`
	`toTx(long t)`
	Returns the tx associated with this t value.

- ## Method Details
	- ### connect
		public static [Connection](https://docs.datomic.com/javadoc/datomic/Connection.html "interface in datomic") connect([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") uriOrMap)
		Connects to the specified database. The systemId and credentials are defined when a new Datomic instance is provisioned.
		URI syntax:
		```
		;;dynamo using roles
		datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}
		;;dynamo using keys, use roles if possible
		datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}?aws_access_key_id={XXX}&aws_secret_key={YYY}
		;; dynamo local
		datomic:ddb-local://{endpoint}:{port}/{dynamodb-table}/{db-name}?aws_access_key_id={XXX}&aws_secret_key={YYY}
		;; sql
		datomic:sql://{db-name}?{jdbc-uri}
		datomic:sql://{db-name}?{query-string}#{jdbc-uri}
		;; infinispan
		datomic:inf://{cluster-member-host}:{port}/{db-name}
		;; cassandra
		datomic:cass://{cluster-member-host}[:{port}]/{keyspace}.{table}/{db-name}[?user={user}&password={pwd}][&ssl=true]
		;; Cassandra3
		datomic:cass3://{cluster-member-host}[:{port}]/{keyspace}.{table}/{db-name}[?user={user}&password={pwd}][&ssl=true][&local-datacenter=datacenter1]
		;; Backups
		datomic:backup:{backup-uri}[?t={backup-t}]
		Backup connections will read the latest backup unless given a t parameter. Connections to
		backups are always read-only, supporting only d/db and d/log. See also d/list-backups.
		;; dev appliance
		datomic:dev://{transactor-host}:{port}/{db-name}[?password={password}]
		;; limited-edition transactor integrated storage
		datomic:limited-edition://{transactor-host}:{port}/{db-name}[?password={password}]
		;; in-process memory
		datomic:mem://{db-name}
		```
		Note that query param values must be URL-encoded, and db-name cannot contain the following characters: / " \*: =?
		The dev, free, and limited-edition protocols use an additional ports to communicate with storage. By default this port is one higher than the specified transactor port. You can override the default by specifying h2-port in the query string, e.g.
		```
		datomic:limited-edition://localhost:4334/mydb?h2-port=6000&
		```
		The sql protocol also supports a map format for the connection params. This is to enable communicating objects that can't be embedded in URI strings, like DataSources. The format for the map is:
		```
		{
		:protocol :sql
		:db-name "myDb"
		:data-source aDataSourceObject
		;; OR
		:factory aCallableReturningConnection
		}
		```
		Note only one of data-source or factory should be supplied. The keys and protocol name can be keywords or strings.
		The cass protocol also supports a map format for the connection params. The format for the map is:
		```
		{
		:protocol :cass
		:db-name "myDb"
		:table "myKeyspace.myTable"
		:cluster aClusterObject
		}
		```
		Note aClusterObject must be an instance of type com.datastax.driver.core.Cluster. The keys and protocol name can be keywords or strings.
		The cass3 protocol also supports a map format for the connection params. The format for the map is:
		```
		{
		:protocol :cass3
		:db-name "myDb"
		:table "myKeyspace.myTable"
		:session aSessionObject
		}
		```
		Note that aSessionObject must be an instance of type com.datastax.oss.driver.api.core.cql.SyncCqlSession. The keys and protocol name can be keywords or strings.
		`d/connect` returns a read-only connection when given a URI with query param `read-only=true`. These connections do not require a running transactor, and support only `d/db` and `d/log` APIs, which will always return the same value read at connection time.
		Datomic connections do not adhere to an acquire/use/release pattern. They are thread-safe and long lived. Connections are cached such that calling Peer.connect(uri) multiple times with the same URI value will return the same connection object. Read-only connections are not cached.
		Parameters:
		`uriOrMap` - a Datomic connection URI string, or parameters map if supported by the protocol.
		Returns:
		a connection to the database.
	- ### createDatabase
		Creates a database with the given name. The systemId and credentials are defined when a new Datomic instance is provisioned. Idempotent.
		Parameters:
		`uriOrMap` - the uri of the database to create.
		Returns:
		```
		true
		```
		if the database was created,
		```
		false
		```
		if the database already exists.
	- ### renameDatabase
		Parameters:
		`uriOrMap` - same as in #connect(Object)
		`newName` - the new name of the database. This is the database name only and should not be a URI.
		Returns:
		```
		true
		```
		if rename succeeded
	- ### deleteDatabase
		Parameters:
		`uriOrMap` - same as in #connect(Object)
		Returns:
		```
		true
		```
		if delete occurred.
	- ### getDatabaseNames
		Returns a list of database names. URI is a database URI as described in the [`connect(java.lang.Object)`](#connect\(java.lang.Object\)) documentation, but with a '\*' where the database name would be. For instance:
		```
		datomic:dev://{transactor-host}:{port}/*
		```
		When using the map form,:db-name should be omitted.
		Parameters:
		`uriOrMap` - same as in #connect(Object), with a '\*' where the database name would be, or omitted in the map form.
		Returns:
		List of database names.
		Since:
		0.9.5040
	- ### listBackups
		public static [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") < [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang"),[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") \> listBackups([String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") backupUri)
		Lists all points in time available at the given `backup-uri`. Returns a map with `:backups`, sorted descending by `t`.
		Each backup contains:  
		`:t` the basis `t` of the backup  
		`:connect-uri` a URI that can be passed to `d/connect`
		Parameters:
		`backupUri` - String
		Returns:
		Map of backup information.
	- ### administerSystem
		public static [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") administerSystem([Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") options)
		Administer a Datomic system. Throws if operation fails.
		Parameters:
		`options` - Map
		Returns:
		data describing the operation result
		Since:
		0.9.5893
	- ### squuid
		public static [UUID](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/UUID.html "class or interface in java.util") squuid()
		Constructs a semi-sequential UUID. Useful for creating UUIDs that don't fragment indexes.
		Returns:
		a UUID whose most significant 32 bits are currentTimeMillis rounded to seconds
		Since:
		0.8.3343
	- ### squuidTimeMillis
		public static long squuidTimeMillis([UUID](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/UUID.html "class or interface in java.util") squuid)
		Get the time component of a squuid.
		Parameters:
		`squuid` - a UUID created by squuid()
		Returns:
		the time in the format of System.currentTimeMillis
		Since:
		0.8.3343
	- ### tempid
		public static [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") tempid([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") partition)
		Generates a temp id in the designated partition.
		Parameters:
		`partition` - a keyword identifying the partition.
		Returns:
		a temp id
	- ### tempid
		Generates a temp id in the designated partition. Values of idNumber from -1 (inclusive) to -1000000 (exclusive) are reserved for user-created temp ids.
		Parameters:
		`partition` - - a keyword identifying the partition.
		`idNumber` - - a long in the range (-1000000,-1\]
		Returns:
		a temp id
	- ### toT
		public static long toT([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") tx)
		Returns the t value associated with this tx.
		Parameters:
		`tx` - a tx
		Returns:
		a t value
		Since:
		0.8.3372
	- ### toTx
		public static [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") toTx(long t)
		Returns the tx associated with this t value.
		Parameters:
		`t` - a t value
		Returns:
		a tx
		Since:
		0.8.3372
	- ### part
		public static [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") part([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entityId)
		Returns the partition of this [entity id](https://docs.datomic.com/schema/identity.html#entities).
		Parameters:
		`entityId` - an entityId
		Returns:
		a partition
		Since:
		0.8.3372
	- ### q
		Like [`query(Object, Object...)`](#query\(java.lang.Object,java.lang.Object...\)), but with a more specific return signature. Supports same [grammar](https://docs.datomic.com/query/query.html#grammar) as query, except for `find-coll`, `find-scalar`, and `find-tuple`.
	- ### query
		Executes a [datalog query](https://docs.datomic.com/query/query.html).
		In addition to basic pattern matching, query supports
		- joins
		- rules
		- arbitrary predicates and functions
		- aggregates
		- binding options for inputs
		- find specification for outputs
		- pull patterns
		Query can be applied to multiple inputs, including both databases and plain old data. See the [grammar](https://docs.datomic.com/query/query.html#grammar) and [documentation](https://docs.datomic.com/query/query.html#queries) for complete details.
		Query runs locally in a Peer process. Query is set-based: intermediate and final result sets must fit in memory. The `query` data structure passed in can be one of
		- a map that may include `:find`, `:with`, `:in`, and `:where` keys
		- a list representation of that same map
		- a serialized [edn](https://github.com/edn-format/edn) string of the list or map form
		The `inputs` can be any of
		- database values
		- arbitrary values
		- rules
		- pull patterns
		If a single database value is provided as input, then no `:in` section is required in the `query`.
		Type Parameters:
		`T` - a type compatible with the [find specification](https://docs.datomic.com/query/query.html#find-specifications)
		Parameters:
		`query` - a data structure describing the query
		`inputs` - inputs bound to the names in `:in` section of `query`
		Returns:
		a data structure based on the find specification
		Since:
		0.9.5040
	- ### query
		public static <T\> T query([QueryRequest](https://docs.datomic.com/javadoc/datomic/QueryRequest.html "class in datomic") queryRequest)
		Like [`query(Object, Object...)`](#query\(java.lang.Object,java.lang.Object...\)), but accepts a [`QueryRequest`](https://docs.datomic.com/javadoc/datomic/QueryRequest.html "class in datomic") object.
		Since:
		0.9.5153
	- ### qseq
		Performs the query described by `query` and `inputs` (as per [`query(Object, Object...)`](#query\(java.lang.Object,java.lang.Object...\))), Item transformations such as pull are deferred until the Stream is consumed. For queries with pull(s), this results in:
		- reduced memory use and the ability to execute larger queries
		- lower latency before the first results are returned
		Parameters:
		`query` - a data structure describing the query
		`inputs` - inputs bound to the names in `:in` section of `query`
		Returns:
		a [`Stream`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html "class or interface in java.util.stream") of the data structure based on the find specification
		Since:
		0.9.6110
	- ### function
		public static datomic.functions.Fn function([Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") m)
		Generates a function object given a map of:lang - clojure or java:params - a list of parameter names used in the code:code - a string containing the code of the function body.:code should be the body of a method corresponding to the params, which will be Objects, but can begin with one or more import statements.
		Parameters:
		`m` - a map defining the function
		Returns:
		a function object implementing Fn plus one of FnN corresponding to its arity.
	- ### resolveTempid
		Resolve a tempid to the actual id assigned in a database.
		Parameters:
		`db` - a database
		`tempids` - `Connection.TEMPIDS` member of a map returned from [`Connection.transact(java.util.List)`](https://docs.datomic.com/javadoc/datomic/Connection.html#transact\(java.util.List\)) or `Connection.transactAsync`.
		`tempid` - a tempid
		Returns:
		the actual id corresponding to tempid
	- ### shutdown
		public static void shutdown(boolean shutdownClojure)
		Shutdown all peer resources. This method should be called as part of clean shutdown of a JVM process. Will release all Connections, and, if shutdownClojure is true, will release Clojure resources. Programs written in Clojure can set shutdownClojure to false if they manage Clojure resources (e.g. agents) outside of Datomic; programs written in other JVM languages should typically set shutdownClojure to true.
		Parameters:
		`shutdownClojure` - set true to shutdown Clojure resources
		Since:
		0.8.3861
	- ### cancel
		public static void cancel([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") anomaly)
		Cancels the current Datomic operation (query or transaction). Throws an ex-info with an anomaly to the original caller. anomaly is as described by https://github.com/cognitect-labs/anomalies. :cognitect.anomalies/category is a required key, valid values are::cognitect.anomalies/incorrect:cognitect.anomalies/conflict When:cognitect.anomalies/message is provided, the message will be used as the Exception's detail message Other keys should be namespace-qualified. All data passed to cancel must be fressian-serializable.
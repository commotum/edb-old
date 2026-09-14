---
title: "Database (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Database.html"
language: "en"
description: "declaration: package: datomic, interface: Database"
word_count: 1783
---

public interface Database

An immutable, point-in-time database value.

- ## Nested Class Summary
	Nested Classes
	Modifier and Type
	Interface
	Description
	`static interface `
	`Database.Predicate<T>`
	Boolean-valued function for [`filtering`](#filter\(datomic.Database.Predicate\)) a database.
- ## Field Summary
	Fields
	Modifier and Type
	Field
	Description
	`static final Object`
	`AEVT`
	Names the [AEVT index](https://docs.datomic.com/query/indexes.html#aevt).
	`static final Object`
	`AVET`
	Names the [AVET index](https://docs.datomic.com/query/indexes.html#avet).
	`static final Object`
	`EAVT`
	Names the [EAVT index](https://docs.datomic.com/query/indexes.html#eavt).
	`static final Object`
	`VAET`
	Names the [VAET index](https://docs.datomic.com/query/indexes.html#avet).
- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`Database`
	`asOf(Object t)`
	Returns the value of the database filtered to include data up to `t`, inclusive
	`Long`
	`asOfT()`
	`Attribute`
	`attribute(Object attrId)`
	Returns information about an [attribute](https://docs.datomic.com/schema/schema.html#attributes).
	`long`
	`basisT()`
	[t value](https://docs.datomic.com/glossary.html#t) of the most recent transaction in this db.
	`Iterable<Datom>`
	`datoms(Object index,  Object... components)`
	Implements the [Datoms API](https://docs.datomic.com/query/indexes.html#datoms-api) for raw access to matching index data.
	`Map`
	`dbStats()`
	Queries for database stats.
	`Object`
	`entid(Object entityId)`
	Returns the entity id associated with any kind of entity identifier.
	`Object`
	`entidAt(Object partition,  Object timePoint)`
	Returns a fabricated entity id in the supplied partition whose T component is at or after the supplied t
	`Entity`
	`entity(Object entityId)`
	Returns an [entity](https://docs.datomic.com/reference/entities.html): a lazy, dynamic associative view of datoms sharing an entity id.
	`Database`
	`filter(Database.Predicate<Datom> pred)`
	Returns a value of the database containing only Datoms satisfying the predicate.
	`Database`
	`filter(Object pred)`
	`Database`
	`history()`
	Returns a history database value containing all assertions and retractions across time.
	`String`
	`id()`
	Opaque, globally unique database id.
	`Object`
	`ident(Object idOrKey)`
	Returns the symbolic keyword associated with an id, or the key itself if passed.
	`Stream<Object>`
	`indexPull(Object options)`
	"Walks an index, pulling entities via:e if:avet or:v if:aevt, using the selector, returning a Stream of the results.
	`Iterable<Datom>`
	`indexRange(Object attrid,  Object start,  Object end)`
	Returns a range of [AVET-indexed](https://docs.datomic.com/query/indexes.html#avet) datoms.
	`Object`
	`invoke(Object entityId,  Object... args)`
	Look up the [database function](https://docs.datomic.com/reference/database-functions.html) of the entity at `entityId`, and invoke the function with `args`.
	`boolean`
	`isFiltered()`
	Does database have a filter set with e.g.
	`boolean`
	`isHistory()`
	True for databases created with [`history()`](#history\(\))
	`long`
	`nextT()`
	next [t value](https://docs.datomic.com/glossary.html#t) that will be assigned by this database.
	`Map`
	`pull(Object pattern,  Object entityId)`
	Returns a hierarchical selection of attributes for entityId.
	`List<Map>`
	`pullMany(Object pattern,  List entityIds)`
	Returns hierarchical selections of attributes for entityIds.
	`Iterable<Datom>`
	`rseekDatoms(Object index,  Object... components)`
	Like [`seekDatoms(java.lang.Object, java.lang.Object...)`](#seekDatoms\(java.lang.Object,java.lang.Object...\)), but iterates the index in reverse, beginning at or before the point where the given components would reside.
	`Iterable<Datom>`
	`seekDatoms(Object index,  Object... components)`
	Raw access to index data, starting at nearest match to input
	`Database`
	`since(Object t)`
	Returns the value of the database filtered to include only data since `t`, exclusive
	`Long`
	`sinceT()`
	`Map`
	`with(List txData)`
	Returns a database with `txData` applied locally in memory.

- ## Field Details
	- ### EAVT
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") EAVT
		Names the [EAVT index](https://docs.datomic.com/query/indexes.html#eavt).
		Pass to APIs that take an index name such as [`datoms(Object, Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)).
	- ### AEVT
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") AEVT
		Names the [AEVT index](https://docs.datomic.com/query/indexes.html#aevt).
		Pass to APIs that take an index name such as [`datoms(Object, Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)).
	- ### AVET
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") AVET
		Names the [AVET index](https://docs.datomic.com/query/indexes.html#avet).
		Pass to APIs that take an index name such as [`datoms(Object, Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)).
	- ### VAET
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") VAET
		Names the [VAET index](https://docs.datomic.com/query/indexes.html#avet).
		Pass to APIs that take an index name such as [`datoms(Object, Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)).
- ## Method Details
	- ### id
		[String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") id()
		Opaque, globally unique database id.
		Returns:
		the database id
	- ### basisT
		long basisT()
		[t value](https://docs.datomic.com/glossary.html#t) of the most recent transaction in this db.
		Returns:
		a t value
	- ### nextT
		long nextT()
		next [t value](https://docs.datomic.com/glossary.html#t) that will be assigned by this database.
		Returns:
		a t value
	- ### asOfT
		[Long](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Long.html "class or interface in java.lang") asOfT()
		Returns:
		a t value, or null
	- ### sinceT
		[Long](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Long.html "class or interface in java.lang") sinceT()
		Returns:
		a t value, or null
	- ### isHistory
		boolean isHistory()
		True for databases created with [`history()`](#history\(\))
		Returns:
		true for history databases
	- ### with
		[Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") with([List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") txData)
		Returns a database with `txData` applied locally in memory.
		It is as if the data was applied in a [transaction](https://docs.datomic.com/transactions/transactions.html), but no actual transaction takes place.
		Parameters:
		`txData` - in the same format as expected by [`transact`](https://docs.datomic.com/javadoc/datomic/Connection.html#transact\(java.util.List\))
		Returns:
		a map as returned by transact
	- ### asOf
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") asOf([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") t)
		Returns the value of the database filtered to include data up to `t`, inclusive
		Parameters:
		`t` - a [time-point](https://docs.datomic.com/glossary.html#time-point)
		Returns:
		the value of the database as of some point t, inclusive
	- ### since
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") since([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") t)
		Returns the value of the database filtered to include only data since `t`, exclusive
		Parameters:
		`t` - a [time-point](https://docs.datomic.com/glossary.html#time-point)
		Returns:
		the value of the database since some point t, exclusive
	- ### history
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") history()
		Returns a history database value containing all assertions and retractions across time.
		A history database can be used for [`datoms(Object, Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)) and [`indexRange(Object, Object, Object)`](#indexRange\(java.lang.Object,java.lang.Object,java.lang.Object\)), for [`queries`](https://docs.datomic.com/javadoc/datomic/Peer.html#query\(java.lang.Object,java.lang.Object...\)), and for [`asOf(Object)`](#asOf\(java.lang.Object\)) and [`since(Object)`](#since\(java.lang.Object\)).
		A history database *cannot* be used with APIs that require a single point-in-time, i.e. [`entity(Object)`](#entity\(java.lang.Object\)) or [`with(java.util.List)`](#with\(java.util.List\)).
		Note that queries will return all of the additions and retractions, which can be distinguished by [`Datom.added()`](https://docs.datomic.com/javadoc/datomic/Datom.html#added\(\))
		Returns:
		a history Database
	- ### filter
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") filter([Database.Predicate](https://docs.datomic.com/javadoc/datomic/Database.Predicate.html "interface in datomic") < [Datom](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic") \> pred)
		Returns a value of the database containing only Datoms satisfying the predicate.
		The predicate will be passed the unfiltered db and a Datom Chained calls to `filter` compose predicates with logical 'and'.
		Parameters:
		`pred` - a `Predicate<Datom>` or `clojure fn`
		Returns:
		the value of the database satisfying the predicate
		Since:
		0.8.3627
	- ### filter
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") filter([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") pred)
	- ### isFiltered
		boolean isFiltered()
		Does database have a filter set with e.g. [`filter(datomic.Database.Predicate)`](#filter\(datomic.Database.Predicate\))?
		Returns:
		true if db has a filter
		Since:
		0.8.3627
	- ### entity
		[Entity](https://docs.datomic.com/javadoc/datomic/Entity.html "interface in datomic") entity([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entityId)
		Returns an [entity](https://docs.datomic.com/reference/entities.html): a lazy, dynamic associative view of datoms sharing an entity id.
		Parameters:
		`entityId` - an [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers)
		Returns:
		an [`Entity`](https://docs.datomic.com/javadoc/datomic/Entity.html "interface in datomic")
	- ### attribute
		[Attribute](https://docs.datomic.com/javadoc/datomic/Attribute.html "interface in datomic") attribute([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") attrId)
		Returns information about an [attribute](https://docs.datomic.com/schema/schema.html#attributes).
		Parameters:
		`attrId` - an [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers) for an attribute
		Returns:
		an [`Attribute`](https://docs.datomic.com/javadoc/datomic/Attribute.html "interface in datomic")
		Since:
		0.9.4470
	- ### ident
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") ident([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") idOrKey)
		Returns the symbolic keyword associated with an id, or the key itself if passed.
		Parameters:
		`idOrKey` - an id or keyword
		Returns:
		a keyword, or nil if not found
	- ### entid
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entid([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entityId)
		Returns the entity id associated with any kind of entity identifier.
		Parameters:
		`entityId` - an [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers)
		Returns:
		an id, or nil if not found
	- ### entidAt
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entidAt([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") partition, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") timePoint)
		Returns a fabricated entity id in the supplied partition whose T component is at or after the supplied t
		. Entity ids sort by partition, then T component, such T components interleaving with transaction numbers. Thus this method can be used to fabricate a time-based entity id component for use in #seekDatoms.
		Parameters:
		`partition` - an [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers) for a partition
		`timePoint` - a [time-point](https://docs.datomic.com/glossary.html#time-point)
		Returns:
		a fabricated entity id at or after some point t
	- ### invoke
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") invoke([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entityId, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")... args)
		Look up the [database function](https://docs.datomic.com/reference/database-functions.html) of the entity at `entityId`, and invoke the function with `args`.
		Parameters:
		`entityId` - an [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers)
		`args` - the arguments to the database function
		Returns:
		the return value of the database function
	- ### datoms
		[Iterable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html "class or interface in java.lang") < [Datom](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic") \> datoms([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") index, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")... components)
		Implements the [Datoms API](https://docs.datomic.com/query/indexes.html#datoms-api) for raw access to matching index data.
		The index must be supplied, and, optionally, one or more leading components of the index can be supplied to narrow the result. [EAVT](https://docs.datomic.com/query/indexes.html#eavt) and [AEVT](https://docs.datomic.com/query/indexes.html#aevt) indexes will contain all datoms [AVET](https://docs.datomic.com/query/indexes.html#avet) will contain datoms for attributes where either `:db/index` or `:db/unique` are true. [VAET](https://docs.datomic.com/query/indexes.html#avet) will contain datoms for attributes of:db.type/ref - it is the reverse index
		Parameters:
		`index` - one of [`EAVT`](#EAVT), [`AEVT`](#AEVT), [`AVET`](#AVET), or [`VAET`](#VAET)
		`components` - supply any datom components to match, in order corresponding to the index
		Returns:
		the datoms in the specified index matching the specified components
	- ### seekDatoms
		[Iterable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html "class or interface in java.lang") < [Datom](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic") \> seekDatoms([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") index, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")... components)
		Raw access to index data, starting at nearest match to input
		. Arguments are the same as to [`datoms(java.lang.Object, java.lang.Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)), but their interpretation is different in two important ways:
		1. The match need not be exact. Results will begin with the closest matching datom
		2. No termination. Results will continue all the way to the end of the index.
		`seekDatoms` is for more advanced applications, and [`datoms(Object, Object...)`](#datoms\(java.lang.Object,java.lang.Object...\)) should be preferred wherever it is adequate. `seekDatoms` is typically used in conjunction with [`entidAt(Object, Object)`](#entidAt\(java.lang.Object,java.lang.Object\)) to implement [new entity scans](https://docs.datomic.com/query/indexes.html#new-entity-scans).
		Parameters:
		`index` - one of [`EAVT`](#EAVT), [`AEVT`](#AEVT), [`AVET`](#AVET), or [`VAET`](#VAET)
		`components` - supply any datom components to search for, in order corresponding to the index
		Returns:
		all of the datoms in the specified index at or after the specified components
	- ### rseekDatoms
		[Iterable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html "class or interface in java.lang") < [Datom](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic") \> rseekDatoms([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") index, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")... components)
		Like [`seekDatoms(java.lang.Object, java.lang.Object...)`](#seekDatoms\(java.lang.Object,java.lang.Object...\)), but iterates the index in reverse, beginning at or before the point where the given components would reside. Only terminates at the start of the index, thus callers must supply their own termination logic.
		Parameters:
		`index` - one of [`EAVT`](#EAVT), [`AEVT`](#AEVT), [`AVET`](#AVET), or [`VAET`](#VAET)
		`components` - supply any datom components to search for, in order corresponding to the index
		Returns:
		datoms in the specified index at or before the specified components, iterating backward
	- ### indexRange
		Returns a range of [AVET-indexed](https://docs.datomic.com/query/indexes.html#avet) datoms.
		Parameters:
		`attrid` - an [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers) naming an indexed attribute.
		`start` - start value or null if from beginning
		`end` - end value (non-inclusive), or null if through end
		Returns:
		an [`Iterable`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html "class or interface in java.lang") over [`Datom`](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic") positioned between start (inclusive) and end (exclusive)
	- ### pull
		[Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") pull([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") pattern, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") entityId)
		Returns a hierarchical selection of attributes for entityId.
		Parameters:
		`pattern` - A [pattern](https://docs.datomic.com/query/pull.html), or a String containing a pattern serialized into edn.
		`entityId` - An [entity identifier](https://docs.datomic.com/schema/identity.html#entity-identifiers)
		Returns:
		A map containing a selection for the entityId passed in.
		Since:
		0.9.5040
	- ### indexPull
		[Stream](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html "class or interface in java.util.stream") < [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") \> indexPull([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") options)
		"Walks an index, pulling entities via:e if:avet or:v if:aevt, using the selector, returning a Stream of the results.
		Parameters:
		`options` - a data structure describing the indexPull serialized into edn
		- a map that includes the `:index`, `:selector`, `:start`, and `:reverse` keys
		| `:index` | :avet or:aevt |
		| --- | --- |
		| `:selector` | a pull selector (see 'pull') |
		| `:start` | A vector in the same order as the index indicating the initial position. At least:a must be specified. Iteration is limited to datoms matching:a. |
		| `:reverse` | optional, when true iterate the index in reverse order |
		Returns:
		an [`Stream`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html "class or interface in java.util.stream") of the indexPull results
		Since:
		0.9.6079
	- ### pullMany
		[List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") < [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") \> pullMany([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") pattern, [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") entityIds)
		Returns hierarchical selections of attributes for entityIds.
		Parameters:
		`pattern` - A [pattern](https://docs.datomic.com/query/pull.html), or a String containing a pattern serialized into edn.
		`entityIds` - A list of [entity identifiers](https://docs.datomic.com/schema/identity.html#entity-identifiers)
		Returns:
		A list of maps containing a selection for each entityId passed in.
		Since:
		0.9.5040
	- ### dbStats
		[Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") dbStats()
		Queries for database stats.
		Returns:
		a map with at least the following keys:
		| `:datoms` | total count of datoms in the (history) database |
		| --- | --- |
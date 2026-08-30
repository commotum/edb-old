---
title: "QueryRequest (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/QueryRequest.html"
language: "en"
description: "declaration: package: datomic, class: QueryRequest"
word_count: 216
---

[java.lang.Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")

datomic.QueryRequest

---

public class QueryRequest extends [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")

Container for parameters to [`Peer.query(QueryRequest)`](https://docs.datomic.com/javadoc/datomic/Peer.html#query\(datomic.QueryRequest\))

- ## Field Summary
	Fields
	Modifier and Type
	Field
	Description
	`static final Object`
	`ARGS`
	`static final Object`
	`QUERY`
	`static final Object`
	`TIMEOUT`
- ## Method Summary
	All MethodsStatic MethodsInstance MethodsConcrete Methods
	Modifier and Type
	Method
	Description
	`Map`
	`asData()`
	`static QueryRequest`
	`create(Object query,  Object... inputs)`
	Creates a QueryRequest object.
	`QueryRequest`
	`timeout(long timeoutMsec)`
	The number of milliseconds after which a query may be stopped.
	`String`
	`toString()`

- ## Field Details
	- ### ARGS
		public static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") ARGS
	- ### QUERY
		public static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") QUERY
	- ### TIMEOUT
		public static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TIMEOUT
- ## Method Details
	- ### create
		Creates a QueryRequest object. `query` and `inputs` take the same form as described in [`Peer.query(Object, Object...)`](https://docs.datomic.com/javadoc/datomic/Peer.html#query\(java.lang.Object,java.lang.Object...\))
		Parameters:
		`query` - a data structure describing the query
		`inputs` - inputs bound to the names in `:in` section of `query`
		Returns:
		a QueryRequest object that can be passed to [`Peer.query(QueryRequest)`](https://docs.datomic.com/javadoc/datomic/Peer.html#query\(datomic.QueryRequest\))
	- ### timeout
		public [QueryRequest](https://docs.datomic.com/javadoc/datomic/QueryRequest.html "class in datomic") timeout(long timeoutMsec)
		The number of milliseconds after which a query may be stopped.
		Note: timeout is approximate, it is meant to protect against long running queries, but is not guaranteed to stop after precisely the duration specified.
		Parameters:
		`timeoutMsec` - number of milliseconds after which a query may be stopped.
		Returns:
		A reference to the updated QueryRequest so methods can be chained together.
	- ### toString
		public [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") toString()
	- ### asData
		public [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") asData()
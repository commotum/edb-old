---
title: "datomic (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/package-summary.html"
language: "en"
description: "declaration: package: datomic"
word_count: 185
---

package datomic

The Datomic peer library is designed to be embedded in application servers. It is the gateway to the rest of the database, submitting [transactions](https://docs.datomic.com/transactions/transactions.html) and receive live notifications from the transactor. It also provides local, in memory access to the database, including [caching](https://docs.datomic.com/operation/aws.html) and [query](https://docs.datomic.com/query/query.html) capability. It contains all the communication components needed for connecting to the transactor and [storage](https://docs.datomic.com/operation/storage.html) services, as well as Datalog and other facilities for managing your data. The peer library can act in standalone mode, using an in-memory database as a stand-in for the other components.

- All Classes and InterfacesInterfacesClasses
	Class
	Description
	[Attribute](https://docs.datomic.com/javadoc/datomic/Attribute.html "interface in datomic")
	Programmatic representation of a [schema attribute](https://docs.datomic.com/schema/schema.html#attributes).
	[Connection](https://docs.datomic.com/javadoc/datomic/Connection.html "interface in datomic")
	A connection to a database for submitting and monitoring transactions, and retrieving the current value of the database.
	[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic")
	An immutable, point-in-time database value.
	[Database.Predicate](https://docs.datomic.com/javadoc/datomic/Database.Predicate.html "interface in datomic") <T\>
	Boolean-valued function for [`filtering`](https://docs.datomic.com/javadoc/datomic/Database.html#filter\(datomic.Database.Predicate\)) a database.
	[Datom](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic")
	An immmutable, point-in-time fact: `[entity, attribute, value, transaction, added]`
	[Entity](https://docs.datomic.com/javadoc/datomic/Entity.html "interface in datomic")
	A future that supports completion listeners.
	Implements the [Log API](https://docs.datomic.com/api/log.html).
	Main entry point, used to manage connections, submit transactions, and query.
	Container for parameters to [`Peer.query(QueryRequest)`](https://docs.datomic.com/javadoc/datomic/Peer.html#query\(datomic.QueryRequest\))
	Utilities for creating and using data structures.
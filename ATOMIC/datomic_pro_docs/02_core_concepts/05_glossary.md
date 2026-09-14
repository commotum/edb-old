---
title: "Glossary | Datomic"
source: "https://docs.datomic.com/glossary.html"
word_count: 1961
---

## Glossary

## ACID

Atomic, Consistent, Isolated, and Durable

## asOf

A database value as of a point in time. With asOf, you can reuse existing queries and rules to ask questions about points in time other than the present.

## Assertion

An atomic fact in the database, associating an [entity](https://docs.datomic.com/glossary.html#entity), [attribute](https://docs.datomic.com/glossary.html#attribute), [value](https://docs.datomic.com/glossary.html#value), and a [tx](https://docs.datomic.com/glossary.html#tx). Opposite of a [retraction](https://docs.datomic.com/glossary.html#retraction).

## Attribute

Something that can be said about an [entity](https://docs.datomic.com/glossary.html#entity). An attribute has a name, e.g. `:person/first-name`, and a value type, e.g. `:db.type/long`, and a cardinality.

## Attribute Identifier

An entity identifier that refers to an attribute.

## Backup URI

A backup URI is a per-database URI specifying a backup location on a local filesytem path with 'file:/full/path/to/backup-directory' or an S3 path with 's3://bucket/prefix'.

## Basis-t

The database t that is the basis for the current database, i.e. the most recent point-in-time that this database has seen.

## Cardinality

Property of an [attribute](https://docs.datomic.com/glossary.html#attribute) that specifies how many values of the attribute can be associated with a single reference entity. Possible values are `:db.cardinality/one` and `:db.cardinality/many`.

## Caches

Datomic caches immutable data at the transactor and in peers. See the operations reference pages for [Pro](https://docs.datomic.com/operation/caching.html) and [Cloud](https://docs.datomic.com/operation/architecture.html#caching). Datomic transactors use a multi-layered cache which in Pro consists of the object cache, memcached (optional), and [valcache](https://docs.datomic.com/operation/valcache.html) (optional). In Datomic Cloud, nodes use an object cache, [valcache](https://docs.datomic.com/operation/architecture.html#valcache), and an EFS cache.

## Client

A process that uses a Datomic library to obtain [connection](https://docs.datomic.com/glossary.html#connection) to interact with one or more [database](https://docs.datomic.com/glossary.html#database).

## Closed World Assumption

Assumption that truth is what the database knows. Databases that intend to store data of record typically make the closed world assumption. Datomic adheres to the closed world model.

## Component

A [reference attribute](https://docs.datomic.com/glossary.html#reference) that is part of its [entity](https://docs.datomic.com/glossary.html#entity). E.g. your arm is a component of you, but your sister isn't. An attribute is a component if it has `:db/isComponent` set to true.

## Component Attribute

See [component](https://docs.datomic.com/glossary.html#component).

## Compute-group

An Auto Scaling Group of compute resources, either a primary compute group or a query group.

## Connection

Client object that provides access to a database. Programs can use a connection to submit transactions.

## Consistent Hash Ring

Datomic uses a consistent hash ring to route transactions to a preferred Node per database. This is a performance optimization only: any Primary Compute Node can handle any transaction.

## Coordination

The ability of a group of processes to negotiate who is responsible for the various [roles](https://docs.datomic.com/glossary.html#role) in a Datomic [system](https://docs.datomic.com/glossary.html#system).

## Covering Index

A covering index contains (rather than points to) the data. Datomic indexes are covering indexes.

## Credentials

Information used to authenticate for a particular task. In accordance with the principle of least privilege, Datomic allows separate credentials for each different activity performed by a running system.

## Database

A database is a set of datoms.

## Datom

An atomic fact in a database, composed of entity/attribute/value/transaction/added. Pronounced like "datum", but pluralized as datoms.

## Database URI

A Unique Resource Identifier pointing to a specific Datomic database. URI syntax is described in the [datomic.api/connect](https://docs.datomic.com/clojure/index.html#datomic.api/connect) doc string.

## Data Function

A function installed in a database, i.e. an attribute value whose type is `:db/fn`.

## Datalog

A deductive query system, typically consisting of:

- A database of facts
- A set of rules for deriving new facts from existing facts
- A query processor that, given some partial specification of a fact or rule: finds all instances of that specification implied by the database and rules, i.e. all the matching facts
Datomic's built-in [query](https://docs.datomic.com/glossary.html#query) is an implementation of Datalog.

## Domain attribute

An attribute used to model something in your application domain.

## EDN

The [extensible data notation](https://github.com/edn-format/edn) is used by Datomic and other applications as a data transfer format.

## EFS Cache

A cache of segments in EFS that will typically contain the entirety of all databases, eliminating the need to read from S3.

## Encrypted Credentials

Encrypted form of credentials. Datomic encrypts credentials in places like EC2 user data to reduce the threat generic exploits.

## Ensure

An operation that guarantees the existence and correct configuration of a resource. Ensure is typically built out of AWS primitives that create, query, and update resources.

## Entity

The first component of a [datom](https://docs.datomic.com/glossary.html#datom), specifying who or what the datom is about. Also the collection of datoms associated with a single entity, as in the Java type, Entity.

## Entity id

An opaque identifier assigned by Datomic that uniquely identifies an entity. Entity ids are integers for efficiency, but application programs should treat them as opaque ids.

## Entity Identifier

A value that identifies an entity. Can be an entity id, an ident, or a lookup ref.

## Environment

An instantiated set of all the resources need to run an application.

## External Key

A unique identifier external to Datomic. Typical external key types are email address, UUID, and URI. External key attributes should be declared as `:db.unique/identity`.

## Epoch

Period of time bounded by writing index to storage. During an epoch, indexing is done in memory. At epoch boundaries, the in-memory index is merged with the persistent index, and a new persistent index is written to the storage service (without blocking the system).

## Excision

The complete removal of a set of [datoms](https://docs.datomic.com/glossary.html#datom) matching a predicate. Excision should be a very infrequent operation, and should not be used to correct erroneous data.

## Fact

See [datom](https://docs.datomic.com/glossary.html#datom).

## Fressian

[Fressian](https://github.com/Datomic/fressian/wiki/Rationale) is an extensible binary format that is used everywhere data is serialized by Datomic: on the wire, at rest, and in caches. Fressian is designed to be:

- Self-describing
- Language-independent
- Extensible
- Simple to implement and consume
- Compact and fast
- Friendly to dynamic and static languages
- Compressible in domain-specific ways

## Keyword

Data type representing a name, e.g. `:email` or (with namespace) `:customer/email`.

## Ident

A value of type `:db/ident` that uniquely identifies an [entity](https://docs.datomic.com/glossary.html#entity).

## Index

Sorted collection of datoms. Indexes are named by the order in which datom components are used for sort, e.g. An index that sorts first by [entity](https://docs.datomic.com/glossary.html#entity), then [attribute](https://docs.datomic.com/glossary.html#attribute), then [value](https://docs.datomic.com/glossary.html#value), then [tx](https://docs.datomic.com/glossary.html#tx) is called EAVT.

## Infrastructure

The set of all environments for an application.

## Ions

Your application code, running on Datomic compute nodes.

## Lookup-ref

A list containing a unique attribute and a value that identifies an entity.

## LRU

Least Recently Used.

## Metrics

Statistics used to measure the health of a running system. By default, Datomic records metrics using Amazon's CloudWatch.

## Namespace

Prefix portion of a [keyword](https://docs.datomic.com/glossary.html#keyword) used to make the keyword globally unique. Namespaces serve a similar function to table names in a relational store, without imposing any obligations or limitations, e.g. an entity can have attributes from more than one namespace.

## Object Cache

Nodes maintain an on-heap cache of segments containing the most recently used datoms.

## Parameters

Named slots for application configuration data.

## Partition

A logical grouping of entities in a database. Partitions have unique qualified names. Every [entity](https://docs.datomic.com/glossary.html#entity) belongs to a partition that is assigned when the entity is created. Partitions act as a storage hint, so that larger systems can plan ahead for better locality of reference for entities that are frequently accessed together. Partitions are typically coarser grained than relational tables. Partitioning is invisible to the query system, and therefore has no impact on the code you write to access the database.

## Peer

A process that uses the Datomic library to interact with a system, and obtain [connections](https://docs.datomic.com/glossary.html#connection) to interact with one or more [databases](https://docs.datomic.com/glossary.html#database). Peers have in-memory access to database values, and an integrated Datalog query engine. There can be many kinds of peers, with capabilities varying by platform and need.

## Primary Compute Stack

A CloudFormation stack providing computational resources. Every Datomic system has a single primary compute stack, and may also have multiple query groups.

## Pull

A declarative way to make hierarchical selections of information about entities.

## Query

Datomic's Datalog system. A query finds [values](https://docs.datomic.com/glossary.html#value) in a [database](https://docs.datomic.com/glossary.html#database) subject to the given constraints, and is specified as [edn](https://docs.datomic.com/glossary.html#edn).

## Query Group

An AutoScaling Group (ASG) of nodes used to dedicate bandwidth, processing power, and caching to particular jobs. Unlike sharding, query groups never dictate who a client must talk to in order to store or retrieve information. Any node in any group can handle any request.

## Reference

An attribute that refers to another entity. References always have the value type `:db.type/ref`.

## REPL

A Clojure REPL (standing for Read-Eval-Print Loop) is a programming environment which enables the programmer to interact with a running Clojure program and modify it, by evaluating one code expression at a time.

## Reference Attribute

See [reference](https://docs.datomic.com/glossary.html#reference).

## Retraction

An atomic fact in the database, dissociating an [entity](https://docs.datomic.com/glossary.html#entity) from a particular [value](https://docs.datomic.com/glossary.html#value) of an [attribute](https://docs.datomic.com/glossary.html#attribute). Opposite of an [assertion](https://docs.datomic.com/glossary.html#assertion).

## Role

Generic name for transactor/peer/persistence service., e.g. "The process claims the transactor role by placing a well-known value in SDB upon startup." Used in the config tools.

## Rule

A named group of [query](https://docs.datomic.com/glossary.html#query) constraints, to allow re-use of logic across queries.

## Schema

The set of possible attributes that can be associated with entities. Any entity can have any attribute.

## Schema Attribute

A built-in attribute used to define schema, e.g. all attributes are named by `:db/ident`.

## Segment

Indexes store datoms as a tree of segments, where the leaf nodes contain a few thousand datoms each.

## Segment Cache

A cache that stores [fressian](https://docs.datomic.com/glossary.html#Fressian) -serialized data, e.g. in memcached. A segment cache takes much less memory than equivalent data in the [object cache](https://docs.datomic.com/glossary.html#object-cache), but is slower to access. [Peer](https://docs.datomic.com/glossary.html#peer) and [transactor](https://docs.datomic.com/glossary.html#transactor) processes use both object caches and segment caches.

## Storage Resources

The durable elements managed by a Datomic system.

## Storage Service

Subsystem responsible for persistence. Datomic Cloud uses DynamoDB as its storage service.

## Stringified Keyword

A string containing a Clojure keyword, e.g. `":name"` or `":person/name"`

## System

A complete Datomic installation, consisting of storage resources, a primary compute stack, and optional query groups.

## Time-point

Data structure that can be resolved to a point in time in a database. Can be a database t, a tx, or a date.

## t

A point in time in a database. Every transaction is assigned a numeric t value greater than any previous t in the database, and all processes see a [consistent](https://docs.datomic.com/transactions/acid.html#consistency) succession of ts.

## tx

An [entity](https://docs.datomic.com/glossary.html#entity) representing a [transaction](https://docs.datomic.com/glossary.html#transaction). Every [datom](https://docs.datomic.com/glossary.html#datom) in a Datomic database includes the tx that created it, allowing recovery of the entire history of the database. Transactions are automatically associated with wall-clock time, but are otherwise ordinary entities. In particular, application code can make additional assertions about transactions.

## Transaction

An atomic unit of work in a database. All Datomic writes are transactional, fully serialized, and ACID (Atomic, Consistent, Isolated, and Durable).

## Transaction Function

A function that runs inside a transaction, taking the current database value plus user arguments and expanding into data to be added by the transaction.

## Transaction Log

An accumulate-only log of all transactions. Datomic Cloud stores the transaction log in DynamoDB.

## Transactor

A process with the ability to commit transactions for a given database. At any moment in time, a running database has exactly one transactor, but any number of [peers](https://docs.datomic.com/glossary.html#peer).

## Tuple

An ordered list of elements. Datomic queries return sets of tuples.

## Unique

Attribute of an [attribute](https://docs.datomic.com/glossary.html#attribute). Each [entity](https://docs.datomic.com/glossary.html#entity) that has a [value](https://docs.datomic.com/glossary.html#value) for a `:db/unique` attribute must have a different value. `:db/unique` has two possible values:

- db.unique/value: attempts to assert a duplicate value will fail
- db.unique/identity: attempts to assert a duplicate identity will [upsert](https://docs.datomic.com/glossary.html#upsert)

## Upsert

Either insert or update an [entity](https://docs.datomic.com/glossary.html#entity), depending on whether the unique entity already exists.

## Valcache

An SSD-backed cache of segments. Valcache is similar in performance to memcached but durable and capacious.

## Value

Something that does not change, e.g. 42, John, or #inst "2012-02-29". A [datom](https://docs.datomic.com/glossary.html#datom) relates an [entity](https://docs.datomic.com/glossary.html#entity) to a value through an [attribute](https://docs.datomic.com/glossary.html#attribute).

## Value type

Attribute of an [attribute](https://docs.datomic.com/glossary.html#attribute) that specifies the data structure that can be stored in the attribute. The value type determines how a [value](https://docs.datomic.com/glossary.html#value) is
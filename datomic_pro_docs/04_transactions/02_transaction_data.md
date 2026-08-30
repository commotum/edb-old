---
title: "Transaction Data"
site: "Datomic"
source: "https://docs.datomic.com/transactions/transaction-data-reference.html"
description: "Explore the Datomic transaction data reference. Get details on transaction functions, schemas, and examples."
word_count: 2133
---

Transaction data (tx-data) is the data representation of a Datomic transaction. This page assumes that you are familiar with the semantics of Datomic transactions, and provides a detailed reference for transaction data.

## Transaction Data Grammar

### Syntax

```
'' literal
"" string
[] = list or vector
{} = map {k1 v1 ...}
() grouping
| choice
? zero or one
+ one or more
```

The symbols `keyword`, `string`, `boolean`, `instant`, `uuid`, `long`, `bigint`, `float`, `double`, and `bigdec` are the [primitive value types](https://docs.datomic.com/schema/schema-reference.html#db-valuetype) supported by datomic.

### Grammar

```
tx-data      = [list-form | map-form | tx-fn]+
assertion    = ([op tx-entid identifier value])
tx-fn        = (tx-fn-name tx-fn-arg*)
map-form     = {keyword (value | map-form | [map-form])}
datom        = (':db/add' | ':db/retract')
tx-entid     = (identifier | tempid)
tempid       = string
identifier   = (eid | lookup-ref | ident)
eid          = nat-int
lookup-ref   = [identifier value]
ident        = keyword
value        = (string | keyword | boolean | ref | instant | uuid | number)
ref          = tx-entid
number       = (long | bigint | float | double | bigdec)
db-fn        = identifier
classpath-fn = qualified symbol
tx-fn-arg    = (value | [value] | {value value})
tx-fn-name   = db-fn | classpath-fn
```

## Transaction Data

```
tx-data                    = [list-form | map-form | tx-fn]+
```

Datomic represents transaction data as [data structures](https://docs.datomic.com/reference/data-structure-literals.html). This is a significant difference from SQL databases, where requests are submitted as strings. Using data instead of strings makes it easier to build requests programmatically.

Transaction data is *semantically* an unordered set of datoms, all of which are added to a database at an atomic moment in time. Datomic provides a number of *mechanical* conveniences for writing such sets of datoms. First, transaction data is represented as an ordered list. This ordering is entirely for the convenience of transaction authors, who can produce (and print/read!) data in any order that is convenient for human understanding of the transaction.

Three kinds of forms can appear in a transaction data list:

- primitive list forms specify a single datom
- map forms provide a concise representation for multiple assertions about the same entity
- transaction functions allow arbitrary functional expansions and validations of input data with reference to *db-before*, the database value at the start of the transaction

Inside these forms, there are several ways to specify entities:

- entity ids (E) are the actual entity ids stored in the database
- temporary ids (tempids) specify entities where a transaction author does not know or care to provide an entity id
- lookup refs specify entities by a domain unique identifier
- idents specify entities by a qualified keyword for use in programs, and are used to name e.g. attributes
- the reserved tempid "datomic.tx" names the [current Tx](#reified-txes)

Each of these data forms are described in detail below.

## Assert and Retract

```
list-form                  = [op tx-entid identifier value]
```

A primitive list form asserts the addition (*:db/add*) or retraction *:db/retract*) of a single datom. An addition indicates that a fact is true at a point in time, and a retraction indicates that a fact is not true at a point in time.

Both forms include an entity id, attribute, and a value, with the value being optional for retractions:

```
[:db/add entity-id attribute value]
[:db/retract entity-id attribute value?]
```

The example below asserts that 42's primary email is "datomic@example.com":

```
[:db/add 42 :email/primary-address "datomic@example.com"]
```

The example below asserts that 42's primary email is *no longer* "datomic.example.com":

```
[:db/retract 42 :email/primary-address "datomic@example.com"]
```

As a convenience, you can omit the value in a retract form, e.g.

```
[:db/retract 42 :email/primary-address]
```

Datomic will expand such forms to zero or more retractions, matching all additions of that E/A combination present in db-before.

Note that adds and retracts in tx-data do not include the transaction id because it is not known until Datomic assigns it.

While the names "add" and "retract" are verbs, tx-data forms are not operations. Inside d/with, they have no effect, and are merely expanded by a preprocessor with knowledge of the tx-id:

```
;; pseudocode for d/with expansion of primitive list form
(let [tx-id 102411]
  (preprocess 
   [:db/retract 42 :email/primary-address "datomic@example.com"]))
```

```
=> 
[42 :email/primary-address "datomic@example.com" 102411 false]
```

### Redundancy Elimination

Datomic will automatically exclude redundant datoms in tx-data. Datoms are considered redundant if they are equal to an existing datom in every component except for tx-id. A retraction that does not match the E/A/V of an existing datom is also redundant, as the absence of a datom already (and efficiently!) indicates that a fact is not true.

Note that entire transactions are never redundant, as the [transaction time](#reified-txes) is always a new fact.

Some system attributes (e.g. *:db.install/attribute*) trigger side effects. Datomic will never treat such attributes as redundant.

## Map Forms

```
map-form                   = {keyword (value | map-form | [map-form])}
```

The map form is a shorthand for a set of additions. The map has an optional *:db/id* key identifying the entity, plus any number of attribute/value pairs.

```
{:db/id entity-id
 attribute value
 attribute value
 ... }
```

The map form is equivalent to a set of primitive additions, where the entity-id is taken from the *:db/id* key in the map (if present) or assigned an (anonymous) temporary id.

```
[:db/add entity-id attribute value]
[:db/add entity-id attribute value]
...
```

You can specify multiple values for a *:db.cardinality/many* attribute with a list in the value position. The following example transacts a person named "Bob" with multiple aliases:

```
[{:person/name "Bob"
  :person/aliases ["Robert" "Bert" "Bobby" "Curly"]}]
```

### Nested Maps

If an attribute has *:db.type/ref*, you can specify the related entity by nesting a map or maps in the value position. This can be used to specify a parent/child relationship. The map below specifies three entities:

```
[{:order/line-items [{:line-item/product chocolate
                      :line-item/quantity 1}
                     {:line-item/product whisky
                      :line-item/quantity 2}]}]
```

This map is equivalent to writing three separate maps:

```
[{:db/id order-id
  :order/line-items [item-1-id, item-2-id]}
 {:db/id item-1-id
  :line-item/product chocolate
  :line-item/quantity 1}
 {:db/id item-2-id
  :line-item/product whisky
  :line-item/quantity 2}]
```

Which in turn is equivalent to writing each datom as a list form:

```
[[:db/add order-id :order/line-item item-1-id]
 [:db/add order-id :order/line-item item-2-id]
 [:db/add item-1-id :line-item/product chocolate]
 [:db/add item-1-id :line-item/quantity 1]
 [:db/add item-2-id :line-item/product whisky]
 [:db/add item-2-id :line-item/quantity 2]]
```

Datomic requires that a nested map either be referenced by a [component attribute](https://docs.datomic.com/schema/schema-reference.html#db-iscomponent) or include a [unique](https://docs.datomic.com/schema/identity.html) attribute. This constraint prevents the accidental creation of easily-orphaned entities that have no unique identity or relation to other entities.

## Transaction Functions

```
tx-fn                      = [tx-fn-name tx-fn-arg*]
tx-fn-name                 = (db-fn classpath-fn)
db-fn                      = identifier
classpath-fn               = qualified-symbol
db-fn-arg                  = (value | [value] | {value value})
```

A list beginning with anything other than *:db/add* or *:db/retract* names a transaction function. Transaction functions are covered in detail on the [Transaction Functions](https://docs.datomic.com/transactions/transaction-functions.html) page.

## Entity ids

```
eid                        = nat-int
```

Semantically, entity ids are opaque values uniquely identifying an entity within a database.

Programs can obtain entity ids for existing entities via query, pull, etc. Programs can also obtain entity ids for datoms added in a transaction via the return value from transact.

Having obtained an entity id, you can use it anywhere in transaction data that an entity is expected. The example below shows list and map forms asserting that entity 42 is an active customer:

```
;; list form
[:db/add 42 :customer/status :active]
;; equivalent map form
{:db/id 42
 :customer/status :active}
```

For brevity, examples often show entity ids as famous small numbers such as '42' above. In practice, most entity ids are not-so-famous large numbers due to the use of high bits for partitioning, e.g.:

```
{:db/id 17592186046416
 :customer/status :active}
```

## Tempids

```
tempid                     = string
```

Temporary ids (tempids) specify entities without requiring an entity id. Syntactically, a temporary id is a string that must not begin with a colon. Strings beginning with "datomic" are reserved for use by Datomic.

There are many situations where tempids may be more convenient than specifying an entity id:

- no entity id exists yet, because the entity is "new"
- the transaction author does not know the entity id
- the specific entity id is unimportant, but it needs to be stated more than once in transaction data to relate different entities

Tempids can be used in transaction data wherever an entity (E) is expected. The examples below show a few uses of tempids in both list and map forms:

```
;; using "jdoe" to connect two datoms about the same entity
[[:db/add "jdoe" :person/first "Jan"]
 [:db/add "jdoe" :person/last "Doe"]]

;; using "jdoe" twice, to relate three different entities ceo->jdoe->intern
[[:db/add "ceo" :hr/manages "jdoe"]
 [:db/add "jdoe" :hr/manages "intern"]]

;; using "jdoe" to relate two different map forms
[{:person/email "shaftoe@example.com "
  :person/friends ["jdoe"]}
 {:db/id "jdoe"
  :person/email "jdoe@example.com"}]
```

Inside d/with, Datomic will assign an entity id for every distinct tempid in transaction data. In the example below, both instances of "jdoe" are assigned the same entity id:

```
;; pseudocode for d/with expansion of tempids
(let [tx-id 102411]
  (preprocess 
   [[:db/add "jdoe" :name/first "Jane"]
    [:db/add "jdoe" :name/last "Doe"]]))
=> [[42 :name/first "Jane" 102411 true]
    [42 :name/first "Doe" 102411 true]]
```

## Idents

Idents name entities with keywords that are unique within a database. Idents are intended for entities that will be named as literals in source code.

Idents are named via the *:db/ident* attribute, and are most often used to name schema attributes. The example below creates an entity named *:address/city*.

```
{:db/ident :address/city
 :db/valueType :db.type/string
 :db/cardinality :db.cardinality/one}
```

Idents can be used in Datomic data anywhere an entity is expected. Idents are resolved against a database value, i.e. the explicit db passed to a query or the db-before of a transaction.

For example, in any transaction against a database including the *:address/city* entity above, you can use *:address/city* in the attribute position of a datom, e.g.

```
[:db/add 42 :address/city "Olin"]
```

In fact, since idents name attributes, every example of transaction data on this page is an example of idents in use.

Idents can be used for other kinds of programmatic names beyond Datomic schema, e.g. for categoric names of a domain value type (enumerations). Idents are for programmatic names only; for domain unique identifiers use unique identities and lookup refs.

Datomic maintains an in-memory lookup table for idents in every peer process.

## Lookup Refs

```
lookup-ref                 = [identifier value]
```

A *lookup ref* allows you to specify entities by their domain-unique identities. Inside a lookup ref, the *identifier* names a [unique attribute](https://docs.datomic.com/schema/schema-reference.html#db-unique) in the database, and a value is a valid value for that attribute. For example, this lookup ref specifies the entity with the *:person/email* value "joe@example.com".

```
[:person/email "joe@example.com"]
```

Datomic will expand a lookup ref to an entity id, throwing an exception if the A/V pair does not match any entity in the database.

## Entity Identifiers

```
tx-entid                   = (identifier | tempid)
tempid                     = string
identifier                 = eid | lookup-ref | ident
eid                        = nat-int
lookup-ref                 = [identifier value]
ident                      = keyword
```

Every datom is about an entity. There are three possible ways to identify an existing entity:

- an [eid](#eid) for an entity that is already in the database
- an [ident](#entity-identifiers) for an entity that has a `:db/ident`
- a [lookup ref](#lookup-ref) for an entity that has a unique attribute

In transactions, there is a fourth option for identifying an entity that might not exist yet:

- a [tempid](#tempids) for a new entity being added to the database

## Values

```
value                      = (string | keyword | boolean | ref | instant | uuid | number)
```

The value type of a datom is dictated by its attribute's schema. The value types are defined and demonstrated in the [Schema Data Reference](https://docs.datomic.com/schema/schema-reference.html#db-valuetype).

## Reified Transactions

When the transactor processes a transaction, it creates a transaction entity to represent it. By default, this entity has one attribute, *:db/txInstant*, whose value is the instant that the transaction was processed. In addition, every datom in a transaction refers to the transaction entity through its *:tx*.

You can add additional attributes to a transaction entity to capture other useful information, such as the purpose of the transaction, the application that executed it, the provenance of the data it added, or the user who caused it to execute, or any other information that might be useful for auditing purposes.

Datomic will resolve the reserved [tempid](#tempids) "datomic.tx" to the current transaction. You can use this tempid to make assertions about the current transaction as you would for any other entity.

The example below annotates a transaction imported from a file with a *:data/src* attribute to indicate the source of the import.

```
[{:db/id "datomic.tx"
  :data/src "https://example.com/catalogs/catalog-2_29_2012.xml"}
 {:product/name "Marbles" ...}
 ...]
```

You can query transaction entities like any other entities in the system.

### Explicit:db/txInstant

If you are importing past data that includes known transaction times, you may want Datomic's *:db/txInstant* to reflect those times. You can include an explicit *:db/txInstant* addition in tx-data, overriding the transactor's clock time. This is useful when you are importing past data and want to assert when that information was known.

Explicit additions of *:db/txInstant* must respect the monotonic ordering of wall-clock time, i.e. you must choose a *:db/txInstant* value that is not older than any existing transaction, and not newer than the transactor's clock time.
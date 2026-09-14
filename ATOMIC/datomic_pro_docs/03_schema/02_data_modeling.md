---
title: "Data Modeling"
site: "Datomic"
source: "https://docs.datomic.com/schema/schema-modeling.html"
description: "Learn how to apply data modeling for schema in Datomic."
word_count: 176
---

## Use:db/ident For Enumerations

Many databases need to represent a set of enumerated values. In Datomic, it is idiomatic to represent enumerated values as entities with a `:db/ident` attribute.

For example, the transaction below defines an `:artist/country` attribute, and two initial enumerated values, `:country/CA` and `:country/JP`, ISO alpha-2 country codes for Canada and Japan:

```
[{:db/ident :artist/country
 :db/valueType :db.type/ref
 :db/cardinality :db.cardinality/one
 :db/doc "An artist's country of residence"}

{:db/ident :country/CA}

{:db/ident :country/JP}]
```

The use of `:db/ident` on the enumerated value entities makes it possible to refer to the entities using their identity keywords, `:country/CA` and `:country/JP`.

The transaction below creates an artist named *Leonard Cohen* with the country `:country/CA`:

```
[{:artist/name "Leonard Cohen"
 :artist/country :country/CA}]
```

The `:artist/country` attribute is of type `:db.type/ref`, so its value must be a reference to another entity. The `:country/CA` keyword can be used as a value because it is the identity of an entity, as specified by `:db/ident` in the previous transaction.

Attempting to use identity keywords that have not been transacted as referenced identities will result in an [anomaly](https://docs.datomic.com/reference/client-reference.html#anomalies).
---
title: "Index APIs"
site: "Datomic"
source: "https://docs.datomic.com/indexes/index-apis.html"
description: "Discover index APIs for Datomic, including the Datoms API and the Index-range API."
word_count: 232
---

Most Datomic programs will not access the [indexes](https://docs.datomic.com/indexes/index-model.html) directly, instead taking advantage of Datomic's declarative [Datalog](https://docs.datomic.com/data-model.html#datalog) query engine.

However, raw index access is available for integrations. For example, you might use the indexes to make Datomic data available to a map/reduce framework spanning multiple data sources of different types.

Datomic indexes are covering indexes. This means the index actually contains the datoms, rather than just a pointer to them. When you (or a Datomic query) iterates an index, it gets the datoms themselves, without any additional lookup.

## Datoms API

[Peer API](https://docs.datomic.com/clojure/index.html#datomic.api/datoms) | [Client API](https://docs.datomic.com/client-api/datomic.client.api.html#var-datoms)

The Datoms API lets you specify a Datomic index, plus a vector of values in the same order as the index. The [example](https://github.com/cognitect-labs/day-of-datomic-cloud/blob/master/doc-examples/raw_index_access.repl) excerpted below searches the `:avet` index for datoms whose `a` component is `:inv/sku`

```
;; Peer API
(->> (d/datoms db :avet :inv/sku)
     (take 3)
     (map :v))

;; Client API
(->> (d/datoms db
               {:index :avet
                :components [:inv/sku]})
     (take 3)
     (map :v))
```

```
=> ("SKU-0" "SKU-1" "SKU-10")
```

## Index-range API

[Peer API](https://docs.datomic.com/clojure/index.html#datomic.api/index-range) | [Client API](https://docs.datomic.com/client-api/datomic.client.api.html#var-index-range)

The index-range API lets you specify an attribute, and returns all datoms sorted by that attribute, optionally limited by start and end values. The [example](https://github.com/cognitect-labs/day-of-datomic-cloud/blob/master/doc-examples/raw_index_access.repl) excerpted below returns all the SKUs between "SKU-42" inclusive and "SKU-44" exclusive.

```
;; Peer API
(->> (d/index-range db :inv/sku "SKU-42" "SKU-44")
     (map :v))

;; Client API
(->> (d/index-range
      db
      {:attrid :inv/sku
       :start "SKU-42"
       :end "SKU-44"})
     (map :v))
```

```
=> ("SKU-42" "SKU-43")
```
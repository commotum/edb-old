---
title: "Index-Pull | Datomic"
source: "https://docs.datomic.com/indexes/index-pull.html"
description: "Discover how to use the Index-Pull API on Datomic."
word_count: 963
---

## Index-Pull

`index-pull` walks an [index](#indexes), [pulling](https://docs.datomic.com/query/query-pull.html) each entity using the [pattern](https://docs.datomic.com/query/query-pull.html#pattern), returning a lazy seq on the results.

`index-pull` takes a `db` and a map with the following required keys:

- [`:index`](#indexes) - `:avet` or `:aevt`. Entities are pulled from third component.
- [`:selector`](#selector) - a [pull selector](https://docs.datomic.com/query/query-pull.html)
- [`:start`](#start) - A vector in the same order as the index indicating the initial position. At least `:a` must be specified.

Optionally:

- `:timeout` \[Client API\] - Timeout in msec.
- `:offset` \[Client API\] - Number of results to omit from the beginning of the returned data.
- `:limit` \[Client API\] - Maximum total number of results to return. Specify -1 for no limit. Defaults to 1000.
- [`:reverse`](#reverse) - walks the index in reverse.

[Peer API](https://docs.datomic.com/clojure/index.html#datomic.api/index-pull) | [Client API](https://docs.datomic.com/client-api/datomic.client.api.html#var-index-pull)

## Indexes

### AVET

An `:index` of [`:avet`](https://docs.datomic.com/indexes/index-model.html#avet) walks the specified attribute, starting at an optional value, pulling against `:e`. If `:a` is `:db.cardinality/many`, then `:v` must be specified to avoid duplicates.

First let's pull the names, starting years and ending years of artists, starting at the `:artist/startYear`

```
(take 5
      (d/index-pull db {:index    :avet
                        :selector '[:artist/name :artist/startYear :artist/endYear]
                        :start    [:artist/startYear]}))
```

```
=>
(#:artist{:name "Choir of King's College, Cambridge", :startYear 1441}
 #:artist{:name "Heinrich Schütz", :startYear 1585, :endYear 1672}
 #:artist{:name "Antonio Vivaldi", :startYear 1678, :endYear 1741}
 #:artist{:name "Johann Sebastian Bach", :startYear 1685, :endYear 1750}
 #:artist{:name "Georg Friedrich Händel", :startYear 1685, :endYear 1759})
```

Interesting result for a database covering releases from 1968-1973.

Starting at the year 1800, the artist's releases and release years can be pulled to investigate why these older artists are in the database.

```
(take 5
      (d/index-pull db {:index :avet
                        :selector '[:artist/name :artist/startYear {:release/_artists [:release/year]}]
                        :start [:artist/startYear 1800]}))
```

```
=>
({:artist/name "Hector Berlioz",
  :artist/startYear 1803,
  :release/_artists [#:release{:year 1970} #:release{:year 1973} #:release{:year 1969}]}
 {:artist/name "Felix Mendelssohn", :artist/startYear 1809, :release/_artists [#:release{:year 1973}]}
 {:artist/name "Frédéric Chopin",
  :artist/startYear 1810,
  :release/_artists [#:release{:year 1968} #:release{:year 1970}]}
 {:artist/name "Franz Liszt", :artist/startYear 1811, :release/_artists [#:release{:year 1968}]}
 {:artist/name "Richard Wagner",
  :artist/startYear 1813,
  :release/_artists [#:release{:year 1968} #:release{:year 1972} #:release{:year 1971} #:release{:year 1970}]})
```

Since the database contains artists with `startYear` 's prior to 1800, this example starts the `index-pull` near the middle of the index by specifying a `v` of 1800.

Despite these artists being from 1800+, they had posthumous releases in the 1968-1973 era.

This `index-pull` does not pull only artists with a `:artist/startYear` of `1800`, but returns a lazy seq starting at that point in the index. The example starts at a `:artist/startYear` of 1800, but returns ascending results from 1803, 1809, etc…

### AEVT

An `:index` of [`:aevt`](https://docs.datomic.com/indexes/index-model.html#aevt) pulls the the values of an entity for `:a`.

`:v` must be `:db.type/ref` and [`:db.cardinality/many`](https://docs.datomic.com/schema/schema-reference.html#db-cardinality)

This example walks the index starting at `:release/artists`, which is `db.type/ref`, and pulls the artists' names.

```
(take 5 (d/index-pull db {:index :aevt
                          :selector [:artist/name]
                          :start [:release/artists]}))
```

```
=>
(#:artist{:name "A1"}
 #:artist{:name "Antonín Dvořák"}
 #:artist{:name "Rob Lamothe"}
 #:artist{:name "Craig Erickson"}
 #:artist{:name "Frédéric Chopin"})
```

The following example walks the `:aevt` index, starting at the entity of the artist "Bob Dylan", pulling the track names for "Bob Dylan", then "George Harrison" and subsequent artists in the index.

With an entity in hand for a specific release, `index-pull` starts at that release and the example pulls the artist names (plural) and [2 tracks](https://docs.datomic.com/query/query-pull.html#limit-option) by each artist.

```
(def dylan-harrison-index-pull
  (d/index-pull db {:index :aevt
                    :selector [:artist/name {[:track/_artists :limit 2] [:track/name]}]
                    :start [:release/artists dylan-harrison-sessions]}))

(take 5 dylan-harrison-index-pull)
```

```
=>
({:artist/name "Bob Dylan", 
  :track/_artists [#:track{:name "California"} 
                   #:track{:name "Grasshoppers in My Pillow"}]}
 {:artist/name "George Harrison",
  :track/_artists [#:track{:name "Give Me Love (Give Me Peace on Earth)"} 
                   #:track{:name "Miss O'Dell"}]}
 {:artist/name "Bonnie St. Claire", 
  :track/_artists [#:track{:name "Planet of Love"} 
                   #:track{:name "Mañana mañana"}]}
 {:artist/name "The Byrds", 
  :track/_artists [#:track{:name "This Wheel's on Fire"} 
                   #:track{:name "Old Blue"}]}
 {:artist/name "Gilberto Gil", 
  :track/_artists [#:track{:name "Cinema Olympia"} 
                   #:track{:name "Frevo Rasgado"}]})
```

However in this instance care must be taken since Bob Dylan was an artist on many releases. The artist entity for Bob Dylan will be pulled on multiple times:

```
(filter #(= (:artist/name %) "Bob Dylan") dylan-harrison-index-pull)
```

```
=>
({:artist/name "Bob Dylan", :track/_artists [#:track{:name "California"} #:track{:name "Grasshoppers in My Pillow"}]}
 {:artist/name "Bob Dylan", :track/_artists [#:track{:name "California"} #:track{:name "Grasshoppers in My Pillow"}]}
 {:artist/name "Bob Dylan", :track/_artists [#:track{:name "California"} #:track{:name "Grasshoppers in My Pillow"}]}
 {:artist/name "Bob Dylan", :track/_artists [#:track{:name "California"} #:track{:name "Grasshoppers in My Pillow"}]}
 {:artist/name "Bob Dylan", :track/_artists [#:track{:name "California"} #:track{:name "Grasshoppers in My Pillow"}]}
 {:artist/name "Bob Dylan", :track/_artists [#:track{:name "California"} #:track{:name "Grasshoppers in My Pillow"}]})
```

## Start

`:start` provides an attribute and optionally a value (for `:avet`) or entity (for `:aevt`) as an initial point for walking the index.

If the relationship between E and V is many-to-many, you must specify the second component under:start to prevent returning duplicates. Datomic will enforce this requirement when possible.

## Selector

The selector is a [pull pattern](https://docs.datomic.com/query/query-pull.html). The selector is pulled against each e for `:avet` or v for `:aevt`

## Reverse

Supplying `:reverse true` to the arg-map causes the index to be walked in reverse.

When `:reverse` is `true`, any `:offset` is applied in the direction of the iteration. If `:offset` is non-nil, the offset occurs relative to the direction of iteration.

```
;; Sample of how :reverse and :offset affect iteration
;; Sample Data - :example/number entries of all positive integers from 1 to n

(seq (d/index-pull db {:index    :avet
                       :selector '[:example/number]
                       :start    [:example/number]
                       :limit 10 }))
```

```
=>
(#:example{:number 1}
 #:example{:number 2}
 #:example{:number 3}
 #:example{:number 4}
 #:example{:number 5}
 #:example{:number 6}
 #:example{:number 7}
 #:example{:number 8}
 #:example{:number 9}
 #:example{:number 10})
```

```
;; limit not required - iterator terminates at beginning
(seq (d/index-pull db {:index    :avet
                       :selector '[:example/number]
                       :start    [:example/number 5]
                       :reverse  true}))
```

```
=>
(#:example{:number 5}
 #:example{:number 4}
 #:example{:number 3}
 #:example{:number 2}
 #:example{:number 1})
```

```
;; The offset is applied after the reverse.
;; Offset to 3, then walk the index in reverse.
(seq (d/index-pull db {:index    :avet
                       :selector '[:example/number]
                       :start    [:example/number 10]
                       :offset   3
                       :reverse  true
                       :limit    5}))
```

```
=>
(#:example{:number 7}
 #:example{:number 6}
 #:example{:number 5}
 #:example{:number 4}
 #:example{:number 3})
```
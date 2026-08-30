---
title: "Query Stats"
site: "Datomic"
source: "https://docs.datomic.com/reference/query-stats.html"
description: "Learn how to analyze Datomic query performance using query stats."
word_count: 428
---

Everything about query-stats is alpha and subject to change in future releases.

## Background

Datomic evaluates query clauses in the order you specify them; except for a few reorderings that are guaranteed winners, e.g. pushing predicates. You can take advantage of this control to choose a performant clause ordering, e.g. putting more selective clauses earlier in a query.

## Problem

You may not know which clauses are more selective. One measure of clause selectivity is the count of rows flowing in and out of each clause. Another measure is the boundness of variables, which helps to limit work done in later clauses.

## Approach

When you request query-stats, Datomic monitors your query as it runs, returning a map with the following:

- Clause ordering
- Count of rows flowing in and out of each clause
- Boundness of variables in and out of each clause
- Predicates used in each clause
- Possible optimizations (expansions and warnings)

To request query-stats, add a `:query-stats` key to your query map. Datomic will return the query result set under the `:ret` key and a query-stats map under the `:query-stats` key.

Requesting query-stats adds some overhead to query processing and should be used outside of production code.

## The Query-Stats Map

The query-stats map and its submaps are open to extension at any time, and you may notice additional keys not documented here.

The query-stats map will have the following keys:

| Key | Description |
| --- | --- |
| :query | The query you provided. |
| :phases | Subqueries of the query in the order they were executed. See [:phases](#phases). |

## :phases

| Key | Description |
| --- | --- |
| :sched | Clauses of the subquery in the order they were executed. |
| :clauses | For each clause, information about row counts and bindings. See [:clauses](#clauses). |

## :clauses

| Key | Description |
| --- | --- |
| :clause | The clause. |
| :rows-in | Count of rows into the clause. |
| :rows-out | Count of rows out of the clause. A large expansion here could be an indicator of poor selectivity. |
| :binds-in | Variables that are bound coming into the clause. Insufficiently bound clauses can be less selective. |
| :binds-out | Variables that are bound going out of the clause. |
| :preds | Optional. Collection of predicate clauses that were used during this clause. |
| :expansion | Optional. Difference between:rows-out and:rows-in. Present on top 3 clauses with the biggest difference. This may or may not indicate a problem – sometimes a large expansion is part of what the query needs to accomplish. |
| :warnings | Optional. Map of possible concerns. See [:warnings](#warnings) |

## :warnings

| Key | Description |
| --- | --- |
| :unbound-vars | Present when none of the vars needed for the clause were bound. This will almost always cause poor performance. |
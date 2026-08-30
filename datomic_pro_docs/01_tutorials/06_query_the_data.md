---
title: "Query the Data"
site: "Datomic"
source: "https://docs.datomic.com/peer-tutorial/query-the-data.html"
description: "Learn how to query data in Datomic with this tutorial. Get step-by-step instructions to efficiently get data from your database."
word_count: 1170
---

Previously: you learned [how to transact domain data into the database](https://docs.datomic.com/peer-tutorial/transact-data.html). This section expects that you have a running REPL where you have already connected to the database "hello", transacted the movie schema, and added three movies.

You have transacted three sets of attributes into the database, creating three movies. What you actually created were three **entities**, each of which has a unique id and a collection of associated attributes. The unique ids were assigned for you when you transacted the data (for more on entity ids, see [identity and uniqueness](https://docs.datomic.com/schema/identity.html)).

When you query a Datomic database, you may be looking to retrieve attribute values, collections of attribute values, entities, or any combination thereof. The client library offers two mechanisms for retrieving data from your database:

- **Query**: uses Datalog, a declarative query language
- **Pull**: a declarative way to make hierarchical (and possibly nested) selections of information about entities
- **Entity**: pulls a lazy, associative view of all the information that can be reached from an entity id

We'll focus on [query](https://docs.datomic.com/clojure/index.html#datomic.api/q) in this tutorial. For more on pull, read [Datomic pull](https://docs.datomic.com/query/query-pull.html), and for more on entity, read [entities](https://docs.datomic.com/reference/entities.html).

## Database Value

A database value is the state of the database at a given point in time.

You must retrieve the current database value to issue a query against a Datomic database. You can issue as many queries against that database value as you want, and they will always return the same results.

Retrieve the current database value and store it in a var:

```
(def db (d/db conn))
```

```
=> #'user/db
```

## Query

Once you have the database value, then you can issue queries against it. You issue queries via the `q` function. `q` takes two required parameters:

- Query data (represented as a map or list) containing **at least** a `:find` clause and a `:where` clause
- A list of input sources for the query

The query data can also contain a variety of other optional components, which you can learn more about in the [q reference](https://docs.datomic.com/clojure/index.html#datomic.api/q).

Provide enough of a `:where` clause to limit the results, and your query performance is relative to the ordering and selectivity of the clauses. Put your most selective clauses first and it may limit the work that needs to be done by other related clauses which come after.

Right now we want to find every movie in the database. A movie is anything that has an associated `:movie/title` attribute (or `:movie/release-year` or `:movie/genre`, but we'll just use `:movie/title` for now).

You will use the [list form of the query data](https://docs.datomic.com/clojure/index.html#datomic.api/q). Create a vector with the three mandatory components listed above. Let's look at a minimal query:

```
(def all-movies-q '[:find ?e 
                    :where [?e :movie/title]])
```

```
=> #'user/all-movies-q
```

Here we define a var, `all-movies-q`, that holds our query definition, which we will later pass to the query. Look at the two clauses:

- `:find` – specifies what you want to be returned from the query. In this case, `?e` is a logic variable that will be bound within the `:where` clause.
- `:where` – the `:where` clause consists of a list of vectors. In our case, we are only passing one clause, `[?e :movie/title]`, which translates to "bind the id of each entity that has an attribute called `:movie/title` to the logic variable named `?e` ".

The whole query reads as "find me the ids of all entities which have an attribute called `:movie/title` ".

To issue the query, call `q` and pass the query and the database value you captured in the first part of this tutorial:

```
(d/q all-movies-q db)
```

```
=> #{[17592186045420] [17592186045421] [17592186045422]}
```

Note that there were three entities in the database with a `:movie/title`, which maps to the three movies you added in the last tutorial. What you receive back is a collection of return values, each of which in this case is just the entity id.

- Instead of finding the entity ids, perhaps you just want to find all the titles of all the movies in the database.

In that case, your query would specify a `:find` clause with a named value (`?movie-title`) that you bind to an attribute (`:movie/title`). That would look like this:

```
(def all-titles-q '[:find ?movie-title 
                    :where [_ :movie/title ?movie-title]])
```

Note the underscore at the beginning of the clause where `?e` used to be. That indicates that we are not interested in the entity id itself. We are only interested in the existence (and value of) the `:movie/title` attribute. The query reads as "find all movie titles from any entity that has an attribute `:movie/title` and assign the title to a logic variable called `?movie-title` ".

- To execute the query, pass it and the db value in as before:

```
(d/q all-titles-q db)
```

```
=> [["Commando"] ["The Goonies"] ["Repo Man"]]
```

Great! You were able to query the attributes from a collection of all the movies in the database. You received a collection of return values, and each is the string title of the movie.

What if you only want the titles of movies released in 1985? To query for titles of movies released in 1985, your `:where` statement needs to have two clauses: one to bind the `:movie/title` attribute (as before), and one to filter by `:movie/release-year`.

Those two clauses have to be **joined**.

The `:where` clause in our last example contained an `_` where we first saw `?e`, because we were not interested in the entity itself. We were only interested in the value of the ~:movie/title/ attribute.

- For that reason, we will re-insert `?e` into both clauses, and that will serve as the join point for the two clauses. Datalog joins are created by the presence of the same logic variable in multiple clauses. The new query looks like this:

```
(def titles-from-1985 '[:find ?title 
                        :where [?e :movie/title ?title] 
                               [?e :movie/release-year 1985]])
```

```
=> #'user/titles-from-1985
```

- This query reads: "find the title of any entity that has a `:movie/title` attribute and whose `:movie/release-year` is 1985". When you issue the new query:

```
(d/q titles-from-1985 db)
```

```
=> [["Commando"] ["The Goonies"]]
```

- What if you want to return all the entities who have a value for all attributes, and a release-year of 1985?

You need a clause for each attribute you want to return and for the filter on release year, all of which should be joined through `?e`. That query looks like this:

```
(def all-data-from-1985 '[:find ?title ?year ?genre 
                          :where [?e :movie/title ?title] 
                                 [?e :movie/release-year ?year] 
                                 [?e :movie/genre ?genre] 
                                 [?e :movie/release-year 1985]])
```

```
=> #'user/all-data-from-1985
```

- And when you run it:

```
(d/q all-data-from-1985 db)
```

```
=> [["The Goonies" 1985 "action/adventure"] ["Commando" 1985 "action/adventure"]]
```

Your return value contains multiple tuples - one per movie found. The shape of the relation returned is defined by your `:find` specification. More information can be found in the [query reference documentation](https://docs.datomic.com/query/query-data-reference.html#find-specs). There is much more to learn about datalog and query, from [nested queries](https://docs.datomic.com/query/query-data-reference.html#q) to secondary [inputs](https://docs.datomic.com/query/query-data-reference.html#inputs) and more.

In the final step of this tutorial, you will learn about the [history of values in the database](https://docs.datomic.com/peer-tutorial/see-historic-data.html).
---
title: "Peer ~Mem~ DB Getting Started"
site: "Datomic"
source: "https://docs.datomic.com/operation/peer-getting-started.html"
description: "Get started with Datomic Peer. Follow our step-by-step guide to set up, configure, and start using the Datomic Peer library."
word_count: 3379
---

## Peer Mem DB Getting Started

This page contains a 5-minute guide that explores the most common operations of a database using the peer library and `mem` storage protocol:

> You need Datomic Pro to follow this tutorial. Check [get Datomic](https://docs.datomic.com/setup/pro-setup.html#get-datomic) for more details.

## Connecting to a Database

Throughout this tutorial, shell commands are run from the root directory of the Datomic distribution. This will be the version- and edition-qualified directory name. For example:

```
cd /home/user/datomic/datomic-pro-1.0.7705
```

The **bin** directory contains executable scripts for launching a REPL, installing the peer library locally, running a transactor, etc. It also contains storage configuration scripts.

For this example, a local in-memory database will be used, which does not require a transactor to run.

For steps on running a transactor, check [running a dev transactor](https://docs.datomic.com/getting-started/dev-setup.html#run-dev-transactor).

- To begin, launch a REPL from the root of the Datomic folder using the

*bin/repl* script:

```
bin/repl
```

- The first thing you have to do is load the peer library:

```
(require '[datomic.api :as d])
```

```
=> nil
```

- Next, create the URI that allows you to create and connect to a database. For this tutorial, the URI will have three parts:
- "datomic", identifying it as a Datomic URI
- "mem", the mem storage protocol that uses local memory instead of a persistent store
- "hello", the name of the database
- Bind a var, *db-uri*, to the value of the URI:

```
(def db-uri "datomic:mem://hello")
```

```
=> #'user/db-uri
```

- Create the "hello" database using the *create-database* function:

```
(d/create-database db-uri)
```

```
=> true
```

- To interact with Datomic, create a connection first. To create a connection with the Peer library, pass the URI to

the *connect* function and bind the returned connection value to a var called *"conn"*:

```
(def conn (d/connect db-uri))
```

```
=> #'user/conn
```

- You will see that a var was created called *"conn"* which is holding your database connection. To inspect it:

```
conn
```

```
=> #object[datomic.peer.LocalConnection 0x6ca372ef "datomic.peer.LocalConnection@6ca372ef"]
```

You can now use " *conn* " as an input to future commands.

The next step will be to define some schema for your new database.

## Transacting Schema

> This section expects that you have a running REPL where you have already created and connected to the database "hello".

One of the great things about Datomic is that everything is data, including the definition of the schema for your database. Because it is just data, it can be created and manipulated just like any other data you put into the database. To add new schema, you issue a transaction that carries the new schema definition.

We'll create a basic model for movies: a movie can have a title, a release year, and a genre. Title and genre are represented as strings, while release year is an integer. In a relational database, each of those (title, release year, and genre) would be considered "fields" in a "table".

In Datomic, each of those is an attribute, which can be associated with an entity. To start adding movie data to our database, we'll have to tell it about those three attributes.

Since schema is just data, you describe your custom attributes using Datomic's built-in attributes.

Every attribute definition has three required attributes:

- `:db/ident` which specifies a unique name for your attribute
- `:db/valueType` which specifies the type of data that can be stored in the attribute
- `:db/cardinality` which specifies whether the attribute stores a single value or many values

Supply also the optional `:db/doc` attribute, which stores a docstring of the attribute you can query later.

Attribute definitions are defined as maps of data. Our first attribute, "title", would be defined as:

```
{:db/ident :movie/title
 :db/valueType :db.type/string
 :db/cardinality :db.cardinality/one
 :db/doc "The title of the movie"}
```

> Note that:db.type/string and:db.cardinality/one are examples of built-in attributes, used for the purpose of defining attributes. Learn more about it in the [reference guide](https://docs.datomic.com/schema/schema.html)).

Similarly, "genre" would be:

```
{:db/ident :movie/genre
 :db/valueType :db.type/string
 :db/cardinality :db.cardinality/one
 :db/doc "The genre of the movie"}
```

Since "year" will be stored as a number, we'll have to change the:db.type for it:

```
{:db/ident :movie/release-year
 :db/valueType :db.type/long
 :db/cardinality :db.cardinality/one
 :db/doc "The year the movie was released in theaters"}
```

You could choose to send each of these attributes to the database in individual transactions, but for efficiency, we'll batch them up into a single transaction. To do that, you combine the three maps into a single vector of maps. Let's bind our schema vector to a var called *movie-schema*:

```
(def movie-schema [{:db/ident :movie/title
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "The title of the movie"}

                   {:db/ident :movie/genre
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "The genre of the movie"}

                   {:db/ident :movie/release-year
                    :db/valueType :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc "The year the movie was released in theaters"}])
```

Now that we have our attribute definitions ready to go, we have to issue a transaction via the Peer library, using the *transact* function. Transact takes two arguments:

- An active connection
- A collection of transaction data, formatted in [list or map form](https://docs.datomic.com/transactions/transaction-data-reference.html#list-forms)

Transact the schema:

```
@(d/transact conn movie-schema)
```

```
=>
  {:db-before datomic.db.Db@624b73ed, :db-after datomic.db.Db@f0d17d2a,
   :tx-data
    [#datom[13194139534312 50 #inst "2017-02-16T16:24:50.487-00:00" 13194139534312 true]
     #datom[63 10 :movie/title 13194139534312 true]
     #datom[63 40 23 13194139534312 true]
     #datom[63 41 35 13194139534312 true]
     #datom[63 62 "The title of the movie" 13194139534312 true]
     #datom[64 10 :movie/genre 13194139534312 true]
     #datom[64 40 23 13194139534312 true]
     #datom[64 41 35 13194139534312 true]
     #datom[64 62 "The genre of the movie" 13194139534312 true]
     #datom[65 10 :movie/release-year 13194139534312 true]
     #datom[65 40 22 13194139534312 true]
     #datom[65 41 35 13194139534312 true]
     #datom[65 62 "The year the movie was released in theaters" 13194139534312 true]
     #datom[0 13 65 13194139534312 true]
     #datom[0 13 64 13194139534312 true]
     #datom[0 13 63 13194139534312 true]],
   :tempids {-9223301668109598144 63, -9223301668109598143 64, -9223301668109598142 65}}
```

The presence of the *:db-before* and a *:db-after* values shows you that new information was transmitted and persisted in your database.

Refer to [transact](https://docs.datomic.com/clojure/index.html#datomic.api/transact) to learn more about the details of this return value.

Next, we'll start adding movies into the database.

## Transacting Data

> This section expects that you have a running REPL where you have already connected to the database "hello" and transacted the movie schema.

As was mentioned previously, schema is just data, like any other data. So you will be unsurprised to learn that creating domain data is a very similar operation to the creation and transaction of schema.

Once again, you will take advantage of the *transact* method of the peer library, which takes an active connection and a collection of transaction data.

- Start by defining your data, again storing it in a var first:

```
(def first-movies [{:movie/title "The Goonies"
                    :movie/genre "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title "Commando"
                    :movie/genre "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title "Repo Man"
                    :movie/genre "punk dystopia"
                    :movie/release-year 1984}])
```

```
=> #'user/first-movies
```

This is an example of the power of working directly with data – anything you might do to assemble a map of movie data could be substituted for the var first-movies. You can also pass that map to intermediate functions for cleaning/pre-processing before transacting.

- Now that you have the movies ready to go, transact them into the database:

```
@(d/transact conn first-movies)
```

```
=>
{:db-before datomic.db.Db@f0d17d2a, :db-after datomic.db.Db@6bc4da95,
 :tx-data
   [#datom[13194139534313 50 #inst "2017-02-16T16:27:03.660-00:00" 13194139534313 true]
    #datom[17592186045418 63 "The Goonies" 13194139534313 true]
    #datom[17592186045418 64 "action/adventure" 13194139534313 true]
    #datom[17592186045418 65 1985 13194139534313 true]
    #datom[17592186045419 63 "Commando" 13194139534313 true]
    #datom[17592186045419 64 "action/adventure" 13194139534313 true]
    #datom[17592186045419 65 1985 13194139534313 true]
    #datom[17592186045420 63 "Repo Man" 13194139534313 true]
    #datom[17592186045420 64 "punk dystopia" 13194139534313 true]
    #datom[17592186045420 65 1984 13194139534313 true]],
 :tempids {-9223301668109598141 17592186045418,
           -9223301668109598140 17592186045419,
           -9223301668109598139 17592186045420}}
```

Once again, the response you get back contains a *:db-before* and a *:db-after*. Note that it clearly lists out all the actual data that was added to the database in the *:tx-data* key.

So, now you have three movies in the database. Next you will learn how to query for them.

## Query Data

> This section expects that you have a running REPL where you have already connected to the database "hello" and transacted the movie schema and three movies.

Previously, you transacted three sets of attributes into the database, creating three movies. What you actually created were three **entities**, each of which has a unique id and a collection of associated attributes. The [unique ids](https://docs.datomic.com/schema/identity.html) were assigned for you when you transacted the data. When you query a Datomic database, you may be looking to retrieve attribute values, collections of attribute values, entities, or any combination thereof. The peer library offers three mechanisms for retrieving data from your database:

- **Query** which uses Datalog, a declarative query language
- **Pull** which is a declarative way to make hierarchical (and possibly nested) selections of information about entities
- **Entity** which allows you, given an entity id, to pull a lazy, associative view of all the information that can be reached from that entity id

> We'll focus on query in this tutorial. For more on pull, read [Datomic pull](https://docs.datomic.com/query/pull.html), and for more on entity, read [entities](https://docs.datomic.com/overview/entities.html).

First, in order to issue a query against a Datomic database, retrieve the current database value. A database value is the state of the database at a given point in time. You can issue as many queries against that database value as you want, they will always run against the database as of the same point in time.

- Retrieve the current database value and store it in a var:

```
(def db (d/db conn))
```

```
=> #'user/db
```

Once you have the database value, you can issue queries against it. You issues queries via the *q* function. *q* takes two required parameters:

- Query data (represented as a map or list) containing **at least** a *:find* clause and a *:where* clause
- A list of input sources for the query

The query data can also contain a variety of other optional components, which you can learn more about in the [q reference](https://docs.datomic.com/clojure/index.html#datomic.api/q).

When crafting your datalog query, provide enough of a *:where* clause to limit the results. So the question is, how do we ask for all the movies in our database? In our schema, a movie is anything that has an associated *:movie/title* attribute (or *:movie/release-year* or *:movie/genre*, but we'll just use *:movie/title* for now).

In this section, you will use the list form of the query data. To construct your query, create a vector with the three mandatory components listed above. Let's look at a minimal query:

```
(def all-movies-q '[:find ?e :where [?e :movie/title]])
```

Here we define a var (all-movies-q) that holds our query, which later will be passed to the *q* function. Look at the two clauses:

- *:find* – specifies what you want returned from the query. In this case, *?e* is a logic variable that will be bound within the *:where* clause
- *:where* – the *:where* clauses consist of a list of vectors. In our case, we are only passing one clause, *\[?e:movie/title\]*, which translates to "bind the id of each entity that has an attribute called *:movie/title* to the logic variable named *?e* "

So, the whole query reads as "find me the ids of all entities which have an attribute called *:movie/title* ".

- To actually issue the query, call *q* and pass the query and the database value. You will see something like the following:

```
(d/q all-movies-q db)
```

```
=> #{[17592186045418] [17592186045419] [17592186045420]}
```

Notice that there were three entities in the database with a *:movie/title*, corresponding to the three movies you added in the last section. What you received back is a collection of return values, each of which in this case is just the entity id.

Once you have entity ids, use them to find out other data about the given [entity](https://docs.datomic.com/reference/entities.html).

Instead of finding the entity ids, perhaps you just want to find all the titles of all the movies in the database. In that case, your query would specify a *:find* clause with a named value (*?movie-title*) that you bind to an attribute (*:movie/title*). It looks like this:

```
(def all-titles-q '[:find ?movie-title :where [_ :movie/title ?movie-title]])
```

Notice the underscore at the beginning of the *:where* clause where *?e* used to be. That indicates that we are not interested in the entity id itself, just the existence (and value of) the *:movie/title* attribute. The query reads "find all movie titles from any entity that has an attribute *:movie/title* and assign the title to a logic variable called *?movie-title* ".

- To execute the query, pass it and the db value in as before:

```
(d/q all-titles-q db)
```

```
=> #{["Commando"] ["The Goonies"] ["Repo Man"]}
```

Great! You were able to query for the attributes from a collection of all the movies in the database. Again, you received a collection of return values, each of which this time is the string title of the movie.

But what if you only want the titles of movies released in 1985? To issue that query, your *:where* argument is going to have to have two clauses, one to bind the *:movie/title* attribute (as before), and one to filter by *:movie/release-year*. Those two clauses have to be **joined**.

Remember in the last example, our *:where* clause contained an \_ where we first saw *?e*, because we were not interested in the entity id itself, only the value of the *:movie/title* attribute. Now, we will re-insert *?e* into both clauses, and that will serve as the join point for the two clauses. In datalog, joins are created implicitly by the presence of the same logic variable in multiples clauses.

The new query looks like this:

```
(def titles-from-1985 '[:find ?title :where [?e :movie/title ?title]
                                            [?e :movie/release-year 1985]])
```

That reads: "find the title of any entity that has a *:movie/title* attribute and whose *:movie/release-year* is 1985". When you issue the new query:

```
(d/q titles-from-1985 db)
```

```
=> #{["Commando"] ["The Goonies"]}
```

Finally, what if you want to return all the attributes for each movie released in 1985? You need a clause for each attribute you want to return and for the filter on release year, all of which should be joined through?e. That query looks like:

```
(def all-data-from-1985 '[:find ?e ?title ?year ?genre
                          :where [?e :movie/title ?title]
                                 [?e :movie/release-year ?year]
                                 [?e :movie/genre ?genre]
                                 [?e :movie/release-year 1985]])
```

And when you run it:

```
(d/q all-data-from-1985 db)
```

```
=>
#{[17592186045419 "Commando" 1985 "action/adventure"]
  [17592186045418 "The Goonies" 1985 "action/adventure"]}
```

> Notice that this time, each of your returned tuples contains multiple values - one per logic variable in the *:find* specification. The shape of the relations returned is defined by your *:find* specification. More information on that can found in the [query reference documentation](https://docs.datomic.com/query/query.html#find-specifications).

There is much more to learn about datalog, from nested queries to secondary inputs and more. You can find out about it in the [query reference](https://docs.datomic.com/query/query.html).

Next you will learn about the history of values in the database.

## History of Values in the Database

> This section expects that you have a running REPL where you have already connected to the database "hello" and transacted the movie schema and three movies.

You've done all the basics - created, defined and populated a database, and learned how to ask it some questions. Now, we will explore the chronological side of Datomic.

You've created a database with three movies in it. One of them, "Commando", is assigned the "action/adventure" genre. Now, the MPAA has updated its approved list of genres, and added "future governor", for movies starring a future governor. Obviously, Commando needs to be updated.

- To change the value of the:movie/genre attribute, you'll first need to find the entity id of Commando:

```
(d/q '[:find ?e :where [?e :movie/title "Commando"]] db)
```

```
=> #{[17592186045419]}
```

- You should be binding the found entity id to a local variable

so you can use it in your subsequent transactions:

```
(def commando-id (ffirst (d/q '[:find ?e :where [?e :movie/title "Commando"]] db)))
```

```
=> #user/commando-id
```

- Next, you need to issue a transaction, telling Datomic about the new value for:movie/genre. Remember back to the *transacting data* section that a transaction requires an active connection and a map of transaction data. In this case, we will specify one special new attribute,:db/id, to which we will bind the entity id we just retrieved in the previous query. The transaction looks like this:

```
@(d/transact conn [{:db/id commando-id :movie/genre "future governor"}])
```

```
=>
{:db-before datomic.db.Db@6bc4da95, :db-after datomic.db.Db@ef7748f8,
 :tx-data
  [#datom[13194139534317 50 \#inst "2017-02-16T16:42:16.787-00:00" 13194139534317 true]
   #datom[17592186045419 64 "future governor" 13194139534317 true]
   #datom[17592186045419 64 "action/adventure" 13194139534317 false]],
 :tempids {}}
```

- The transaction succeeded. Let's verify if Commando has been updated:

```
(d/q all-data-from-1985 db)
```

```
=>
  #{[17592186045419 "Commando" 1985 "action/adventure"]
    [17592186045418 "The Goonies" 1985 "action/adventure"]}
  user=>
```

WAIT? What happened? You saw that your transaction succeeded, but when you asked the database, it still shows " *action/adventure* " for Commando. Is something wrong?

Absolutely not. Remember this from the "querying the data" tutorial:

> "A database value is the state of the database at a given point in time. You can issue as many queries against that database value as you want, they will always return the same results".

We issued our transaction against a **connection**, but we are issuing queries against a **database value**, which is a snapshot as of a point in time. And we captured that database value in a var called "db" which we pass into the query as the final argument. It doesn't matter that our transaction succeeded - we aren't seeing the new data because we are querying against the old database value.

- Get a current value of the database and then issue your query again:

```
(def db (d/db conn))
```

```
=> #'user/db
```

```
(d/q all-data-from-1985 db)
```

```
=>
#{[17592186045419 "Commando" 1985 "future governor"]
  [17592186045418 "The Goonies" 1985 "action/adventure"]}
```

Great, now we see "Commando" **has** been updated to the latest MPAA specification. What you have seen here is half of the immutability story. As long as you are holding onto a database value and issuing queries against it, you will always see the data as of a single point in time. To get see the latest values, update your database value.

But what if you are at the latest value, and want to see where you came from? You have to go and get another version of the database value, at a time before your last transaction. To do this, you have to know the "time basis" of the database value you want. Datomic can use either a transaction id, *t*, or a Date to establish a time basis. Remember the results from issuing the transaction where you transacted the first movies:

```
{:db-before datomic.db.Db@f0d17d2a,
 :db-after datomic.db.Db@6bc4da95,
 :tx-data [#datom[13194139534313 50
  #inst "2017-02-16T16:27:03.660-00:00" 13194139534313 true]
  #datom[17592186045418 63 "The Goonies" 13194139534313 true]
  #datom[17592186045418 64 "action/adventure" 13194139534313 true]
  #datom[17592186045418 65 1985 13194139534313 true]
  #datom[17592186045419 63 "Commando" 13194139534313 true]
  #datom[17592186045419 64 "action/adventure" 13194139534313 true]
  #datom[17592186045419 65 1985 13194139534313 true]
  #datom[17592186045420 63 "Repo Man" 13194139534313 true]
  #datom[17592186045420 64 "punk dystopia" 13194139534313 true]
  #datom[17592186045420 65 1984 13194139534313 true]],
 :tempids {-9223301668109598141 17592186045418,
  -9223301668109598140 17592186045419,
  -9223301668109598139 17592186045420}}
```

In the *:tx-data* key, the first list in the data is the attributes associated with the transaction itself. The first number (13194139534313) is the transaction id. In our case, this transaction id represents the time basis we'd like to look at - the one right before our current state, which was created when we updated the genre of "Commando". To grab a different view as of a different time basis, we use the " *as-of* " function, which takes a database value and a time basis:

```
(def old-db (d/as-of db 13194139534313))
```

- Now we have a database value from the past, including only changes up until that point in time. Issuing the same query against it, we'll see the value of *:movie/genre* from before our transaction:

```
(d/q all-data-from-1985 old-db)
```

```
=>
#{[17592186045419 "Commando" 1985 "action/adventure"]
  [17592186045418 "The Goonies" 1985 "action/adventure"]}
```

> You can also use " *since* " instead of " *as-of* ", which returns a database value with only changes added after a point in time.

- To see all the values that a given attribute has held over time, access a special view on the database value, called **history**. To get it, call history on your existing database value:

```
(def hdb (d/history db))
```

```
=> #'user/hdb
```

- Now pass that in to your query instead of db, and voila:

```
(d/q '[:find ?genre
       :where [?e :movie/title "Commando"]
              [?e :movie/genre ?genre]] hdb)
```

```
=> #{["action/adventure"] ["future governor"]}
```

Notice that the *:movie/genre* attribute of "Commando" has held two different values over time, " *action/adventure* " and "future governor". There is much more you can do with *as-of*, *since*, and *history*, which you can learn more about in [the filters reference](https://docs.datomic.com/reference/filters.html).

Congratulations, you've finished a whirlwind tour of using Datomic. For further information, we recommend exploring the [Datomic client library](https://docs.datomic.com/peer-tutorial/connect-to-a-database.html).
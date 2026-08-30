---
title: "Transact Data"
site: "Datomic"
source: "https://docs.datomic.com/peer-tutorial/transact-data.html"
description: "Learn how to transact data in Datomic with the peer tutorial. Follow step-by-step instructions to add, update, and delete data efficiently."
word_count: 317
---

You have learned [how to transact a schema into the database](https://docs.datomic.com/peer-tutorial/transact-schema.html). This section expects that you have a running REPL where you have already connected to the database "hello" and transacted the movie schema.

In the previous step, you learned how to transact schema data into the database. As was mentioned there, schema is just data, like any other data. So you will be unsurprised to learn that creating domain data is a very similar operation.

Putting domain data into your database also takes advantage of the [`transact`](https://docs.datomic.com/clojure/index.html#datomic.api/transact) method of the Peer library. `transact` takes an active connection and a map of data.

- Define your data in a var:

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

Once again, the response you get back contains a *:db-before* and a *:db-after*. Note that it lists out all the actual data that was added to the database in the *:tx-data* key.

Now you have three movies in the database. In the next section you will [learn how to query for them](https://docs.datomic.com/peer-tutorial/query-the-data.html).
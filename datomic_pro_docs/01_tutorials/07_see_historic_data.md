---
title: "See Historic Data"
site: "Datomic"
source: "https://docs.datomic.com/peer-tutorial/see-historic-data.html"
description: "Learn how to view historical data in Datomic with this tutorial."
word_count: 969
---

Previously: you learned [how to query domain data in the database](https://docs.datomic.com/peer-tutorial/query-the-data.html). This tutorial expects that you have a running REPL where you have already [connected to](https://docs.datomic.com/peer-tutorial/connect-to-a-database.html) the database "hello", [installed the movie schema](https://docs.datomic.com/peer-tutorial/transact-schema.html), and [added three movies](https://docs.datomic.com/peer-tutorial/transact-data.html).

You've done all the basics - created, defined, and populated a database, and learned how to ask it some questions. Now, we will explore the chronological side of Datomic.

You've created a database with three movies in it. One of them, "Commando", is assigned the "action/adventure" genre. Now, the MPAA has updated its approved list of genres, and added "future governor", for movies starring a future governor. Obviously, Commando needs to be updated.

- To change the value of the `:movie/genre` attribute, you'll first need to find the entity id of Commando:

```
(d/q '[:find ?e 
              :where [?e :movie/title "Commando"]] 
            db)
```

```
=>
  #{[17592186045419]}
```

- Bind the found entity id to a local variable so you can use it in your subsequent transactions:

```
(def commando-id 
  (ffirst (d/q '[:find ?e 
                 :where [?e :movie/title "Commando"]] 
                db)))
```

```
=> #user/commando-id
```

- Next, you need to issue a transaction, telling Datomic about the new value for `:movie/genre`.

Remember back to the [transact data](https://docs.datomic.com/peer-tutorial/transact-data.html) tutorial that a transaction requires an active connection and a map of data. In this case, we will specify one very special new attribute, `:db/id`, to which we will bind the entity id we just retrieved in the previous query. The transaction looks like this:

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
=> [["The Goonies" 1985 "action/adventure"] ["Commando" 1985 "action/adventure"]]
```

WAIT? What happened? You saw that your transaction succeeded, but when you asked the database, it still shows "action/adventure" for Commando. Is something wrong?

Absolutely not. Remember this from the "querying the data" tutorial:

> "A database value is the state of the database at a given point in time. You can issue as many queries against that database value as you want, they will always return the same results."

We issued our transaction against a *connection*, but we are issuing queries against a *database value*, which is a snapshot as of a point in time. And we captured that database value in a var called "db" which we pass into the query as the final argument. It doesn't matter that our transaction succeeded - we aren't seeing the new data because we are querying against the old database value.

- Get a current value of the database, then issue your query again:

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
=> [["Commando" 1985 "future governor"] ["The Goonies" 1985 "action/adventure"]]
```

Great, now we see "Commando" **has** been updated to the latest MPAA specification. What you have seen here is half of the immutability story. As long as you are holding onto a database value and issuing queries against it, you will always see the data as of a single point in time. To see the latest values, update your database value.

- But what if you are at the latest value, and want to see where you came from? You have to go and get another version of the database value, at a time before your last transaction. To do this, you have to know the "time basis" of the database value you want. Datomic can use either a transaction id, t, or a date to establish a time basis. Remember the results from issuing the transaction where you transacted the first movies:

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

In the *:tx-data* key, the first list in the data is the attributes associated with the transaction itself. The first number (13194139534313) is the transaction id. In our case, this transaction id represents the time basis we'd like to look at - the one right before our current state, which was created when we updated the genre of "Commando".

- To grab a different view of a different time basis, we use the " *as-of* " function, which takes a database value and a time basis:

```
(def old-db (d/as-of db 13194139534313))
```

Now we have a database value from the past, including only changes up until that point in time. When issuing the same query against it, the value of *:movie/genre* from before our transaction will be displayed:

```
(d/q all-data-from-1985 old-db)
```

```
=>
#{[17592186045419 "Commando" 1985 "action/adventure"]
  [17592186045418 "The Goonies" 1985 "action/adventure"]}
```

You can also use " *since* " instead of " *as-of* ", which returns a database value with only changes added after a point in time.

- Finally, if you want to see all the values that a given attribute has held over time, you will need to access a special view on the database value, called *history*.

To get it, call the history of your existing database value:

```
(def hdb (d/history db))
```

```
=> #'user/hdb
```

- Now pass that into your query instead of db, and voila:

```
(d/q '[:find ?genre
       :where [?e :movie/title "Commando"]
              [?e :movie/genre ?genre]] hdb)
```

```
=> #{["action/adventure"] ["future governor"]}
```

You can see that the *:movie/genre* attribute of "Commando" has held two different values over time, " *action/adventure* " and "future governor". There is much more you can do with *as-of*, *since*, and *history*, which you can learn more about in [the filters reference](https://docs.datomic.com/reference/filters.html).

Congratulations, you've finished a whirlwind tour of using Datomic.
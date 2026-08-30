---
title: "Connect to a Database"
site: "Datomic"
source: "https://docs.datomic.com/peer-tutorial/connect-to-a-database.html"
description: "Learn how to connect to a Datomic database with this guide. Follow instructions to set up your environment and access your data."
word_count: 266
---

Previously you learned how to [run a transactor](https://docs.datomic.com/peer-tutorial/transactor.html). This section guides on how to connect to a database.

## Creating a Database

- To begin, launch a REPL from the root directory of the Datomic folder using the `bin/repl` script:

```
bin/repl
```

- Require the Peer API:

```
(require '[datomic.api :as d])
```

```
=>
nil
```

The Datomic Peer API [names databases](https://docs.datomic.com/clojure/index.html#datomic.api/connect) with a URI that includes the protocol name, storage connection information, and a database name. The complete URI for a database named "hello" on the [transactor you started in the previous step](https://docs.datomic.com/peer-tutorial/transactor.html#start) is `"datomic:dev://localhost:4334/hello"`.

- Def a var, `db-uri`, with this name:

```
(def db-uri "datomic:dev://localhost:4334/hello")
```

```
=>
#'user/db-uri
```

- Create the "hello" database using the `create-database` function:

```
(d/create-database db-uri)
```

```
=>
true
```

## Create a Connection

Once you have [created a database](#create-db), you can use the Datomic peer library to interact with it.

- Connect to the transactor in the same REPL that you used to create your database:

```
(def conn (d/connect db-uri))
```

```
=>
#'user/conn
```

- You will see that a var was created called `conn` which is holding your database connection. To inspect it, run:

```
conn
```

```
=> #object[datomic.peer.Connection
        0x10a59519
        "{:unsent-updates-queue 0, :pending-txes 0, :next-t 1005, :basis-t 1001, :index-rev 0, :db-id \"hello-c33c1487-877a-404a-88d0-0aac99518598\"}"]
```

Any transactions submitted to the connection will be persisted in the storage that you chose when creating your database.

- Try adding a new entity with the `:db/doc` value "Hello world":

```
@(d/transact conn [{:db/doc "Hello world"}])
```

```
=>
{:db-before datomic.db.Db@e438f2c9,
 :db-after datomic.db.Db@5d0a1343,
 :tx-data [#datom[13194139534317 50 #inst"2024-05-13T00:52:21.776-00:00" 13194139534317 true]
           #datom[17592186045422 62 "Hello world" 13194139534317 true]],
 :tempids {-9223300668110598143 17592186045422}}
```

## Interact with Datomic

The next step will be to define some [schema for your new database](https://docs.datomic.com/peer-tutorial/transact-schema.html).
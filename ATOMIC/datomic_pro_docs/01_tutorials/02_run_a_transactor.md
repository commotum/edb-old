---
title: "Run a Transactor"
site: "Datomic"
source: "https://docs.datomic.com/peer-tutorial/transactor.html"
description: "Learn how to set up and use Datomic transactors."
word_count: 149
---

## Running a Transactor

For this tutorial, you will run a `dev` mode transactor on your local machine. `Dev` storage will persist your data by using local disk files for [storage](https://docs.datomic.com/operation/storage.html). It requires a transactor to be running.

To start a transactor, you need a transactor properties file. Datomic includes example properties files for each storage. Copy the `config/samples/dev-transactor-template.properties` file to a location of your choice, so that you can edit it later if needed.

This guide will use the `config` directory in your datomic-pro distribution directory:

```
cp config/samples/dev-transactor-template.properties config/dev-transactor-template.properties
```

## Starting a Transactor

From your shell system, run:

```
bin/transactor config/dev-transactor-template.properties
```

```
=>
Launching with Java options -server -Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=50
System started
```

Next, you will use this database URI to [connect to a database](https://docs.datomic.com/peer-tutorial/connect-to-a-database.html). The transactor process will be used for the subsequent steps in this guide. Make sure that it's running while you work through this guide.
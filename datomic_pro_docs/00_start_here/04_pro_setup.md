---
title: "Pro Setup"
site: "Datomic"
source: "https://docs.datomic.com/setup/pro-setup.html"
description: "Get started with Datomic Pro. Follow our setup guide for installation, configuration, and initial deployment."
word_count: 288
---

This page is for users who have [chosen Datomic Pro](https://docs.datomic.com/datomic-overview.html#datomic-editions) and covers getting Datomic Pro as well as running a transactor.

## Get Datomic Pro

Datomic Pro is distributed as a zip. You can download the latest version of Datomic Pro from [here](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7705/datomic-pro-1.0.7705.zip) or with this `curl` command:

```
curl https://datomic-pro-downloads.s3.amazonaws.com/1.0.7705/datomic-pro-1.0.7705.zip -O
```

After you download Datomic, unzip it locally. Throughout the documentation, shell commands are run from the root directory of the Datomic install. Change to this directory now, e.g.

```
cd /home/user/datomic/datomic-pro-1.0.7705
```

## Run a Transactor

This section is for users who have [downloaded Datomic Pro](#get-datomic) and covers running a transactor. Before running a production transactor, you must [set up a storage service](https://docs.datomic.com/operation/storage.html) and configure transactor properties. For local development, you can skip these steps and use Datomic’s included dev storage. Datomic’s dev storage uses an H2 database embedded in the transactor process, with a default configuration that exposes H2 on ports *4334* and *4335*.

### Starting a Transactor

To start a transactor, run `bin/transactor`, passing a transactor properties file. This command shows using the sample dev properties file that is included with Datomic.

noslide

```
bin/transactor config/samples/dev-transactor-template.properties
```

The transactor is ready once the message “System started” appears on stdout.

### Stopping a Transactor

To stop a transactor, kill the transactor process, e.g. by typing *Ctrl+C* in the shell process where you ran `bin/transactor` or by sending a kill signal, e.g.

```
kill $(DATOMIC_PROCESS_ID)
```

Once you are able to start and stop Datomic processes, you can [integrate the peer library](https://docs.datomic.com/accessing/integrating-peer-lib.html) with your Java or Clojure project and [start using Datomic](https://docs.datomic.com/peer-tutorial/peer-tutorial.html).

## Supported Java Versions

Datomic Pro supports LTS versions of Java, currently 17, 21, and 25.

Changes to Java support over time are recorded in the [Datomic Pro changelog](https://docs.datomic.com/changes/pro.html) and [release notices](https://docs.datomic.com/release-notices.html).
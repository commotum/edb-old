---
title: "Peer Language Support"
site: "Datomic"
source: "https://docs.datomic.com/operation/languages.html"
description: "Learn how to interact with Datomic using different programming languages."
word_count: 524
---

## JVM Languages

The Datomic team supports the Java and Clojure APIs. Community supported, third-party libraries are described in the section [third-party libraries](#third-party-libraries).

### Java

The [Java API](https://docs.datomic.com/javadoc/index.html) gives Java users access to all the features of the Peer library. Additionally, the [Util](https://docs.datomic.com/javadoc/datomic/Util.html) class contains convenience functions for creating and working with Datomic's commonly used data structures.

### Clojure

The [Clojure API](https://docs.datomic.com/clojure/index.html) is a functional projection of the [Java API](https://docs.datomic.com/javadoc/index.html). The methods of the Java types are flattened to functions in a single *datomic.api* namespace with an instance of the given type as the first argument.

It is idiomatic to reference the *datomic.api* namespace as *d*, with *db* and *q* added to the current namespace.

```
(ns project-ns
  (:require [datomic.api :as d :refer [db q]]))
```

The code below illustrates the translation of the Java API to Clojure.

Here is the code in Java:

```
String uri = "datomic:mem://test";
Connection conn = Peer.connect(uri);
Collection results = Peer.query("[:find ?e :where [?e :db/doc]]", conn.db);
```

And the equivalent code in Clojure:

```
;; clojure
(def uri "datomic:mem://test")
(def conn (d/connect uri))
(def results (q '[:find ?e :where [?e :db/doc]] (db conn)))
```

### Other JVM Languages (JRuby, Scala, Groovy etc.)

Users of other JVM languages can use the [Java API](https://docs.datomic.com/javadoc/index.html) via their respective Java interop capabilities.

## Non-JVM Languages

Non-JVM languages can access Datomic through the REST service via HTTP.

### The REST Service

A Datomic peer can be run as a standalone HTTP service. See the REST service [documentation](https://docs.datomic.com/reference/rest.html) for information on starting and stopping the service. Many clients can connect to a single instance of the HTTP service, and one instance of the service represents one licensed peer.

The service is its own documentation. Just point a browser at the root of the server:port on which you started the service. Note that the 'web app' that results **is** the service. It is not an app built on the service, nor a set of documentation pages about the service. The URIs, query params, and POST data are the same ones you will use when accessing the service programmatically.

In addition to the self-describing nature of the REST service, some basic documentation of the available endpoints is available in the REST service [documentation](https://docs.datomic.com/reference/rest.html).

The REST service accepts and responds with EDN data.

### EDN

EDN is an extensible data notation (pronounced \[eed-n\]). For more information, see the [specification](https://github.com/edn-format/edn). Reader/writer implementations are available in various [languages](https://github.com/edn-format/edn/wiki/Implementations).

A full-featured client of the Datomic REST service should support round-trip conversions between native data structures and [EDN](https://github.com/edn-format/edn), the data format that the REST service sends and receives. Doing so would allow users of the native language to build transactions and queries using native data structures, just like users of the Peer library in JVM languages.

## Third-Party Libraries

Datomic community members have contributed the following language-specific libraries which provide idiomatic support for Datomic. The Datomic team encourages third-party language libraries but does not support them directly.

- [Native JVM Peer with Scala idioms](https://dwhjames.github.io/datomisca)
- [Native (JRuby) and REST ActiveModel wrapper for Ruby](https://github.com/relevance/diametric)
- [REST client in JavaScript, for use with Node.js and similar](https://github.com/limadelic/datomicjs)
- [Native JVM Peer with Groovy idioms](https://github.com/jeffbrown/groovy-datomic)
- [REST client in Python](https://github.com/gns24/pydatomic)
- [Native JVM Peer with type-safe Scala meta-DSL](https://github.com/scalamolecule/molecule)
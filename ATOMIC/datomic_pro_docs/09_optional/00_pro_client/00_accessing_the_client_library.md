---
title: "Accessing the Client Library"
site: "Datomic"
source: "https://docs.datomic.com/accessing/integrating-client-lib.html"
description: "Learn how to integrate the Datomic Client Library into your project."
word_count: 175
---

This page is for users who have completed the [setup](https://docs.datomic.com/setup/setup.html) and covers how to integrate the [Datomic client library](https://docs.datomic.com/client-api/datomic.client.api.html) into your Java or Clojure project.

## Installing the Client Library

The Datomic client library includes both the [synchronous](https://docs.datomic.com/reference/client-reference.html#sync) and [asynchronous](https://docs.datomic.com/reference/client-reference.html#async) APIs, and is provided via [Maven central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22client-cloud%22).

> If you are looking to use Client with Datomic Pro, see the [Pro Client Getting Started Tutorial](https://docs.datomic.com/operation/client-getting-started.html).

### Clojure CLI

To use the Client library from a [Clojure CLI REPL](https://docs.datomic.com/operation/howto.html#clojure-cli), add the following to your [deps.edn](https://clojure.org/guides/deps_and_cli) dependencies map:

```
com.datomic/client-cloud {:mvn/version "1.0.137"}
```

### Maven

To retrieve the Client library for a Maven project, add the following snippet inside the `<dependencies>` block of your pom.xml file:

```
<dependency>
 <groupId>com.datomic</groupId>
 <artifactId>client-cloud</artifactId>
 <version>1.0.137</version>
</dependency>
```

### Leiningen

To include the client library in a Leiningen project, add the following snippet to your [project.clj](https://github.com/technomancy/leiningen#configuration) file in the collection under the `:dependencies` key.

```
[com.datomic/client-cloud "1.0.137"]
```

Make sure that Clojure dependency is set to at least `[org.clojure/clojure "1.9.0"]`.

Now that you have the Datomic client library integrated into your project, you can start the [client tutorial](https://docs.datomic.com/client-tutorial/client-tutorial.html).
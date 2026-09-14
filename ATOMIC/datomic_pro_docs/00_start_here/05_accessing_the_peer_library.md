---
title: "Accessing the Peer Library"
site: "Datomic"
source: "https://docs.datomic.com/accessing/integrating-peer-lib.html"
description: "Learn how to integrate the Datomic Peer Library into your project."
word_count: 228
---

This page is for users who have completed the [Datomic Pro setup](https://docs.datomic.com//setup/pro-setup.html) and covers integrating the [Datomic peer library](https://docs.datomic.com/clojure/index.html) into your Java or Clojure project.

The [peer library](https://docs.datomic.com/clojure/index.html) must be on the classpath to be used to interact with Datomic.

## Maven Setup

There are two ways to install the Datomic libraries in your local Maven repository. You can configure your project to use the com.datomic Maven repository or run an install script included with the Datomic distribution.

## Using From Deps.edn

To include the Datomic peer library in your `deps.edn` project, declare the following dependency in `:deps`:

```
com.datomic/peer {:mvn/version "1.0.7705"}
```

## Using From Maven

To include the Datomic peer library in a Maven-based build, add the modified snippet to the dependencies section of your pom.xml:

```
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>peer</artifactId>
  <version>1.0.7705</version>
</dependency>
```

## Using from Leiningen

To include the Datomic peer library in a leiningen project, add the snippet to the dependencies section of your project.clj:

```
;; in collection under :dependencies key
[com.datomic/peer "1.0.7705"]
```

### Installing from Datomic Distribution

You can install the Datomic peer library in your local Maven repository by running the following command from the Datomic distribution's root directory:

```
bin/maven-install
```

Or on [Windows](https://maven.apache.org/guides/getting-started/windows-prerequisites.html):

```
mvn install:install-file -DgroupId=com.datomic -DartifactId=datomic-pro -Dfile=datomic-peer-0.9.5703.jar -DpomFile=pom.xml
```

## Next Steps

You now have access to the Datomic peer API!

Now that you have the Datomic peer library integrated into your project, you can start the [peer tutorial](https://docs.datomic.com/peer-tutorial/peer-tutorial.html).
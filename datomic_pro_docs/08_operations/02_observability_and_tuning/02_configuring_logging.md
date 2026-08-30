---
title: "Configuring Logging"
site: "Datomic"
source: "https://docs.datomic.com/operation/configuring-logging.html"
description: "Learn how to configure transactor and peer logging in Datomic."
word_count: 353
---

## Transactor Logging

The Datomic transactor logs via [logback](http://logback.qos.ch/). Datomic includes a sample [logback configuration](https://logback.qos.ch/manual/configuration.html) file suitable for development at `bin/logback.xml`. This sample logback configuration is set up by default to log to the directory specified by the *log-dir* transactor property (defaults to `log/` within Datomic distribution).

Logback is open source and highly configurable. It includes built-in [appenders](https://logback.qos.ch/manual/appenders.html) that can send logs via email or to a SQL database, and you can write a custom appender for arbitrary integrations.

## Transactor S3 Log Rotation

The transactor can automatically migrate its logs to S3. When you set the transactor property *aws-s3-log-bucket-id*, the transactor will copy log files found in the directory configured by setting the transactor property *log-dir*. If you need more flexible access to logs, you should configure or implement a logback appender.

## Peer Logging

The Datomic peer library uses [slf4j](http://www.slf4j.org/) for logging, so Peers can be configured to output logs via a variety of standard Java logging libraries, including *logback* and *log4j* by:

- Including the appropriate *slf4j* JAR on the classpath
- (Optionally) adding corresponding configuration files

The following sections show how to configure Peer logging in deps.edn and Maven. If you do not configure logging for your project, the Datomic peer library will not log.

### Peer Logging with logback

If you choose to use logback on the peer, you can use the transactor's *bin/logback.xml* file as a starting point for your configuration.

#### deps.edn

For deps.edn, add the following to the dependencies section of your `deps.edn`, replacing DATOMIC\_VERSION, as described above.

```
com.datomic/peer {:mvn/version "DATOMIC_VERSION"}
ch.qos.logback/logback-classic {:mvn/version "1.5.34"}
```

#### Maven

For Maven, add the following to the dependencies section of your pom.xml, replacing DATOMIC\_VERSION, as described above.

```
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>peer</artifactId>
  <version>DATOMIC_VERSION</version>
</dependency>
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.5.34</version>
</dependency>
```

### Peer Logging with log4j

#### deps.edn

For deps.edn, add the following to the dependencies section of your `deps.edn`, replacing DATOMIC\_VERSION, as described above.

```
com.datomic/peer {:mvn/version "DATOMIC_VERSION"
                  :exclusions [org.slf4j/log4j-over-slf4j]}
org.apache.logging.log4j/log4j-slf4j2-impl {:mvn/version "2.26.0"}
```

#### Maven

For Maven, add the following to the dependencies section of your pom.xml, replacing DATOMIC\_VERSION, as described above.

```
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>peer</artifactId>
  <version>DATOMIC_VERSION</version>
  <exclusions>
    <exclusion>
     <groupId>org.slf4j</groupId>
     <artifactId>log4j-over-slf4j</artifactId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>org.apache.logging.log4j</groupId>
  <artifactId>log4j-slf4j2-impl</artifactId>
  <version>2.26.0</version>
</dependency>
```
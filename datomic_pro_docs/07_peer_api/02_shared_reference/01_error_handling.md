---
title: "Error Handling"
site: "Datomic"
source: "https://docs.datomic.com/api/error-handling.html"
description: "Learn how to handle error in Datomic APIs across all editions."
word_count: 314
---

This page covers error handling in [Datomic APIs](https://docs.datomic.com/datomic-overview.html#datomic-APIs) across all [editions](https://docs.datomic.com/datomic-overview.html#datomic-editions).

Error information should be generic and extensible. Datomic accomplishes this by representing errors as error maps with namespaced keywords for keys. When an error occurs in an asynchronous API, Datomic will place an error map on the channel and then close the channel. When an error occurs in a synchronous API, Datomic will throw an exception that implements IExceptionInfo, and applications can recover the error map with [ex-data](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/ex-data).

Error information should be actionable. Datomic accomplishes this by dividing errors into categories using the [anomalies library](https://github.com/cognitect-labs/anomalies), which includes guidance for which errors are retryable and the approach needed to resolve each category of error.

While these basic mechanisms are simple, there are several points of complexity to consider, explained in more detail below.

## Arbitrary Java Exceptions

Datomic relies on several third-party libraries, each of which has its own conventions for error handling. While Datomic APIs may wrap such exceptions with anomalies, they do not promise to do so in all cases. Some of Datomic's own codebase predates the existence of the facilities described above and may also throw arbitrary exceptions.

Applications must be prepared to handle arbitrary Java exceptions in addition to the information-bearing exceptions described above.

## Wrapped Exceptions

Java exceptions can wrap underlying cause exceptions. Programs can walk this cause chain with e.g. [ex-cause](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/ex-cause) or [getCause](https://docs.oracle.com/javase/8/docs/api/java/lang/Throwable.html#getCause--). Error handling routines should be prepared to walk cause chains to find the "interesting" exceptions. To make matters more challenging, library releases sometimes introduce or remove layers in the cause chain. Error handlers therefore need to search the chain semantically, rather than e.g. assuming that the third exception in the chain is the interesting one.

As an example, Datomic APIs that return futures such as [transact](https://docs.datomic.com/clojure/index.html#datomic.api/transact) and [transact-async](https://docs.datomic.com/clojure/index.html#datomic.api/transact-async) will *always* wrap exceptions in an [ExecutionException](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ExecutionException.html) to comply with the contract of [Future.get](https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/Future.html#get\(\)).
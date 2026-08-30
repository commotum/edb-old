---
title: "ListenableFuture (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/ListenableFuture.html"
language: "en"
description: "declaration: package: datomic, interface: ListenableFuture"
word_count: 73
---

public interface ListenableFuture<T\> extends [Future](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html "class or interface in java.util.concurrent") <T\>

A future that supports completion listeners.

- ## Nested Class Summary
	## Nested classes/interfaces inherited from interface java.util.concurrent.Future
	`Future.State`

- ## Method Details
	- ### addListener
		void addListener([Runnable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Runnable.html "class or interface in java.lang") listener, [Executor](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executor.html "class or interface in java.util.concurrent") executor)
		Register a listener to run on the given executor. The listener will run once and only once, if and when the Future's work is complete. If the future has completed already, the listener will run immediately. Ordering of listeners is not guaranteed.
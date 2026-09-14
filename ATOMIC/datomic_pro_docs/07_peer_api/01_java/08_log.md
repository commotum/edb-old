---
title: "Log (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Log.html"
language: "en"
description: "declaration: package: datomic, interface: Log"
word_count: 176
---

public interface Log

Implements the [Log API](https://docs.datomic.com/api/log.html).

- ## Field Summary
	Fields
	Modifier and Type
	Field
	Description
	`static final Object`
	`DATA`
	`static final Object`
	`T`
- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`Iterable<Map>`
	`txRange(Object startT,  Object endT)`
	Returns a range of transactions in log, starting at start, or from beginning if start is null, and ending before end, or through end of log if end is null.

- ## Field Details
	- ### T
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") T
	- ### DATA
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") DATA
- ## Method Details
	- ### txRange
		[Iterable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html "class or interface in java.lang") < [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") \> txRange([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") startT, [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") endT)
		Returns a range of transactions in log, starting at start, or from beginning if start is null, and ending before end, or through end of log if end is null. Each transaction is a map with the following keys:
		| `T` | the T point of the transaction |
		| --- | --- |
		| `DATA` | a Collection of the Datoms asserted/retracted by the transaction |
		Parameters:
		`startT` - a [time-point](https://docs.datomic.com/glossary.html#time-point) or null
		`endT` - a [time-point](https://docs.datomic.com/glossary.html#time-point) or null
		Returns:
		an Iterable of transaction maps occurring between start (inclusive) and end (exclusive)
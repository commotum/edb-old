---
title: "Datom (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Datom.html"
language: "en"
description: "declaration: package: datomic, interface: Datom"
word_count: 164
---

public interface Datom

An immmutable, point-in-time fact: `[entity, attribute, value, transaction, added]`

- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`Object`
	`a()`
	This datom's [attribute](https://docs.datomic.com/schema/schema.html#attributes) id.
	`boolean`
	`added()`
	Is this datom added or retracted?
	`Object`
	`e()`
	This datom's [entity id](https://docs.datomic.com/schema/identity.html#entities).
	`Object`
	`get(int index)`
	Positional getter, as if datom is tuple of `[e a v tx added]`
	`Object`
	`tx()`
	This datom's [transaction id](https://docs.datomic.com/transactions/transaction-processing.html#reified-transactions).
	`Object`
	`v()`
	This datom's value.

- ## Method Details
	- ### e
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") e()
		This datom's [entity id](https://docs.datomic.com/schema/identity.html#entities).
		Returns:
		entity id
	- ### a
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") a()
		This datom's [attribute](https://docs.datomic.com/schema/schema.html#attributes) id.
		Returns:
		attribute id
	- ### v
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") v()
		This datom's value.
		Returns:
		value
	- ### tx
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") tx()
		This datom's [transaction id](https://docs.datomic.com/transactions/transaction-processing.html#reified-transactions).
	- ### added
		boolean added()
		Is this datom added or retracted?
		When datoms come from [`Database.history()`](https://docs.datomic.com/javadoc/datomic/Database.html#history\(\)), this method can be used to distinguish additions from retractions
		Returns:
		a boolean indicating whether this datom was added or retracted
	- ### get
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") get(int index)
		Positional getter, as if datom is tuple of `[e a v tx added]`
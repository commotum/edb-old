---
title: "Database.Predicate (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Database.Predicate.html"
language: "en"
description: "declaration: package: datomic, interface: Database, interface: Predicate"
word_count: 66
---

Type Parameters:

`T` - Datom type

Enclosing interface:

`Database`

---

public static interface Database.Predicate<T\>

Boolean-valued function for [`filtering`](https://docs.datomic.com/javadoc/datomic/Database.html#filter\(datomic.Database.Predicate\)) a database.

Since:

0.8.3627

- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`boolean`
	`apply(Database db,  T val)`
	Database-filtering predicate.

- ## Method Details
	- ### apply
		boolean apply([Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") db, [T](https://docs.datomic.com/javadoc/datomic/Database.Predicate.html "type parameter in Database.Predicate") val)
		Database-filtering predicate.
		Parameters:
		`db` - a [`Database`](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic")
		`val` - a [`Datom`](https://docs.datomic.com/javadoc/datomic/Datom.html "interface in datomic")
		Returns:
		true if datom matches the predicate.
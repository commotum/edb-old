---
title: "Entity (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Entity.html"
language: "en"
description: "declaration: package: datomic, interface: Entity"
word_count: 216
---

public interface Entity

Implements the [Entity API](https://docs.datomic.com/reference/entities.html) for associative navigation by attribute keys. An Entity is lazy - the values of its attributes are not obtained from the db until get or touch are called, after which they are cached in the entity. Note that entities have reference-like equality semantics - Two entities are equal if they have the same id and their databases have the same id

- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`Database`
	`db()`
	`Object`
	`get(Object key)`
	Gets the value of the attribute named by key, and is polymorphic on key type cardinality:many attributes will always return a collection, even when only one value
	`Set<String>`
	`keySet()`
	`Entity`
	`touch()`
	Touches all of the attributes of the entity, including any component entities recursively.

- ## Method Details
	- ### get
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") get([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") key)
		Gets the value of the attribute named by key, and is polymorphic on key type cardinality:many attributes will always return a collection, even when only one value
		Parameters:
		`key` - A colon-prefixed string, e.g. ":user/firstName"
		Returns:
		the value(s) of that attribute, or null if none
	- ### touch
		[Entity](https://docs.datomic.com/javadoc/datomic/Entity.html "interface in datomic") touch()
		Touches all of the attributes of the entity, including any component entities recursively.
	- ### keySet
		[Set](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Set.html "class or interface in java.util") < [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") \> keySet()
	- ### db
		[Database](https://docs.datomic.com/javadoc/datomic/Database.html "interface in datomic") db()
		Returns:
		the database value that is the basis for this entity
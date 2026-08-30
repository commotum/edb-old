---
title: "Util (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Util.html"
language: "en"
description: "declaration: package: datomic, class: Util"
word_count: 281
---

[java.lang.Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")

datomic.Util

---

public final class Util extends [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")

Utilities for creating and using data structures.

- ## Method Summary
	All MethodsStatic MethodsConcrete Methods
	Modifier and Type
	Method
	Description
	`static List`
	`list(Object... items)`
	Creates an immutable List.
	`static Map`
	`map(Object... keyvals)`
	Creates an immutable Map.
	`static String`
	`name(Object k)`
	Returns the name part of a keyword or symbol.
	`static String`
	`namespace(Object k)`
	Returns the namespace part of a keyword or symbol.
	`static Object`
	`read(String source)`
	Reads one item from source, returning it.
	`static List`
	`readAll(Reader reader)`
	Reads all the data in reader, returning a List.
	`static Stream`
	`streamOn(Iterable it)`
	Create a stream on an immutable Iterable.

- ## Method Details
	- ### name
		public static [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") name([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") k)
		Returns the name part of a keyword or symbol.
		Parameters:
		`k` - - a keyword or symbol
		Returns:
		the name
	- ### namespace
		public static [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") namespace([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") k)
		Returns the namespace part of a keyword or symbol.
		Parameters:
		`k` - - a Keyword
		Returns:
		the namespace
	- ### list
		public static [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") list([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")... items)
		Creates an immutable List.
		Parameters:
		`items` - - Objects to be included in the List
		Returns:
		an unmodifiable List
	- ### map
		public static [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html "class or interface in java.util") map([Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang")... keyvals)
		Creates an immutable Map.
		Parameters:
		`keyvals` - - pairs to include in the Map, written as key1, value1, key2, value2, and so on
		Returns:
		an unmodifiable Map
	- ### read
		public static [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") read([String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html "class or interface in java.lang") source)
		Reads one item from source, returning it.
	- ### readAll
		public static [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html "class or interface in java.util") readAll([Reader](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/Reader.html "class or interface in java.io") reader)
		Reads all the data in reader, returning a List. Closes reader.
		Parameters:
		`reader` - - the [edn](https://github.com/edn-format/edn) data to parse.
		Returns:
		a List for the parsed data.
	- ### streamOn
		public static [Stream](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html "class or interface in java.util.stream") streamOn([Iterable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html "class or interface in java.lang") it)
		Create a stream on an immutable Iterable.
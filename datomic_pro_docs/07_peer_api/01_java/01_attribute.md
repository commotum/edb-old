---
title: "Attribute (Datomic Java API Documentation)"
source: "https://docs.datomic.com/javadoc/datomic/Attribute.html"
language: "en"
description: "declaration: package: datomic, interface: Attribute"
word_count: 463
---

public interface Attribute

Programmatic representation of a [schema attribute](https://docs.datomic.com/schema/schema.html#attributes).

Attribute information always resides in memory, so using this interface is more efficient than accessing the same information from the database via e.g. query.

- ## Field Summary
	Fields
	Modifier and Type
	Field
	Description
	`static final Object`
	`CARDINALITY_MANY`
	`static final Object`
	`CARDINALITY_ONE`
	`static final Object`
	`TYPE_BIGDEC`
	`static final Object`
	`TYPE_BIGINT`
	`static final Object`
	`TYPE_BOOLEAN`
	`static final Object`
	`TYPE_BYTES`
	`static final Object`
	`TYPE_DOUBLE`
	`static final Object`
	`TYPE_FLOAT`
	`static final Object`
	`TYPE_FN`
	`static final Object`
	`TYPE_INSTANT`
	`static final Object`
	`TYPE_KEYWORD`
	`static final Object`
	`TYPE_LONG`
	`static final Object`
	`TYPE_REF`
	`static final Object`
	`TYPE_STRING`
	`static final Object`
	`TYPE_URI`
	`static final Object`
	`TYPE_UUID`
	`static final Object`
	`UNIQUE_IDENTITY`
	`static final Object`
	`UNIQUE_VALUE`
- ## Method Summary
	All MethodsInstance MethodsAbstract Methods
	Modifier and Type
	Method
	Description
	`Object`
	`cardinality()`
	The attribute's [cardinality](https://docs.datomic.com/schema/schema.html#cardinality)
	`boolean`
	`hasAVET()`
	Does this attribute *currently* have an [AVET index](https://docs.datomic.com/query/indexes.html#avet)?
	`boolean`
	`hasFulltext()`
	Does this attribute have a fulltext index?
	`boolean`
	`hasNoHistory()`
	Is this a [noHistory](https://docs.datomic.com/schema/schema.html#nohistory) attribute?
	`Object`
	`id()`
	The attribute's [entity id](https://docs.datomic.com/schema/identity.html#entities)
	`Object`
	`ident()`
	The attribute's [ident](https://docs.datomic.com/schema/identity.html#idents) (programmatic name)
	`boolean`
	`isComponent()`
	Is this a [component](https://docs.datomic.com/schema/schema.html#component) attribute?
	`boolean`
	`isIndexed()`
	Is this attribute configured for an [AVET index](https://docs.datomic.com/query/indexes.html#avet)?
	`Object`
	`unique()`
	Type of the attribute's [unique index](https://docs.datomic.com/schema/identity.html#unique-identities), if any.
	`Object`
	`valueType()`
	The attribute's [value type](https://docs.datomic.com/schema/schema.html#value-types)

- ## Field Details
	- ### CARDINALITY\_MANY
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") CARDINALITY\_MANY
	- ### CARDINALITY\_ONE
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") CARDINALITY\_ONE
	- ### UNIQUE\_IDENTITY
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") UNIQUE\_IDENTITY
	- ### UNIQUE\_VALUE
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") UNIQUE\_VALUE
	- ### TYPE\_BIGDEC
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_BIGDEC
	- ### TYPE\_BIGINT
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_BIGINT
	- ### TYPE\_BOOLEAN
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_BOOLEAN
	- ### TYPE\_BYTES
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_BYTES
	- ### TYPE\_DOUBLE
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_DOUBLE
	- ### TYPE\_FN
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_FN
	- ### TYPE\_FLOAT
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_FLOAT
	- ### TYPE\_INSTANT
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_INSTANT
	- ### TYPE\_KEYWORD
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_KEYWORD
	- ### TYPE\_LONG
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_LONG
	- ### TYPE\_REF
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_REF
	- ### TYPE\_STRING
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_STRING
	- ### TYPE\_URI
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_URI
	- ### TYPE\_UUID
		static final [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") TYPE\_UUID
- ## Method Details
	- ### id
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") id()
		The attribute's [entity id](https://docs.datomic.com/schema/identity.html#entities)
		Returns:
		an entity id
	- ### ident
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") ident()
		The attribute's [ident](https://docs.datomic.com/schema/identity.html#idents) (programmatic name)
		Returns:
		an ident
	- ### valueType
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") valueType()
		The attribute's [value type](https://docs.datomic.com/schema/schema.html#value-types)
		Returns:
		a value type
	- ### cardinality
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") cardinality()
		The attribute's [cardinality](https://docs.datomic.com/schema/schema.html#cardinality)
		Returns:
		either [`CARDINALITY_MANY`](#CARDINALITY_MANY) or [`CARDINALITY_ONE`](#CARDINALITY_ONE)
	- ### unique
		[Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html "class or interface in java.lang") unique()
		Type of the attribute's [unique index](https://docs.datomic.com/schema/identity.html#unique-identities), if any.
		Returns:
		one of [`UNIQUE_IDENTITY`](#UNIQUE_IDENTITY), [`UNIQUE_VALUE`](#UNIQUE_VALUE), or null
	- ### isComponent
		boolean isComponent()
		Is this a [component](https://docs.datomic.com/schema/schema.html#component) attribute?
		Returns:
		true if `:db/isComponent` true for this attribute
	- ### isIndexed
		boolean isIndexed()
		Is this attribute configured for an [AVET index](https://docs.datomic.com/query/indexes.html#avet)?
		Returns:
		true if `:db/index` true for this attribute, or attribute is unique
	- ### hasAVET
		boolean hasAVET()
		Does this attribute *currently* have an [AVET index](https://docs.datomic.com/query/indexes.html#avet)?
		When you [alter](https://docs.datomic.com/schema/schema-change.html#schema-alteration) an existing schema, indexes are created in the background after setting `:db/index` to true. This method returns true once a recently-added index is ready for use.
	- ### hasNoHistory
		boolean hasNoHistory()
		Is this a [noHistory](https://docs.datomic.com/schema/schema.html#nohistory) attribute?
	- ### hasFulltext
		boolean hasFulltext()
		Does this attribute have a fulltext index?
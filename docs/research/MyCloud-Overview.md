# **MyCloud Overview**

## **What is MyCloud?**

[https://docs.datomic.com/cloud/whatis/data-model.html](https://docs.datomic.com/cloud/whatis/data-model.html)

MyCloud is a MacOS and iOS app built with SwiftUI, FastAPI, and JAX, that brings the power of enterprise scale databases to everyone with a distributed, highly available, scalable, real-time, multiplayer database and document store that provides ACID transactions, flexible schema, powerful Datalog queries, and complete data history.

## **MyCloud Data Model**

A MyCloud database is a set of immutable atomic facts called atoms. A database contains no tables; rather, there is a universal schema of user-defined attributes. Any entity can possess any attribute.

MyCloud datalog queries automatically use multiple indexes to support a variety of access patterns. In addition to supporting query, these indexes support identity and uniqueness, an indelible time model, and lookup refs.

### **Database** 

### In MyCloud, *a database value* is a set of *atoms* and is often abbreviated as *database* or *db*. A db is a point-in-time, immutable value and will never change. If you use the same db for several queries, you will know the answers are based upon exactly the same data from a single point in time.  An atom is an immutable atomic fact that represents the addition or retraction of a relation between an entity, an attribute, a value, a transaction, an author/user, and a point in time. An atom is expressed as a seven-tuple:

### 

- ### an entity id (E)

- ### an attribute (A)

- a value for the attribute (V)  
- a boolean (Op) indicating whether the atom is being added or retracted  
- a user id (U) indicating the user/author adding or retracting the atom  
- a timestamp (Tm) indicating the exact moment in time the atom was added or retracted  
- a transaction id (Tx)

#### **Example Datom**

| E | aedb54ac-1b09-497e-a9ab-ef94ae89c330 |
| :---: | :---- |
| **A** | :user/favorite-color |
| **V** | :blue |
| **Tx** | c284d833-4b5a-41ff-b8c7-bd0cd0a2c404 |
| **Op** | TRUE |
| **U** | f637d8ed-58c7-4494-bd54-74014d630676 |
| **Tm** | 2023-10-21T15:25:56.740Z |

### **Entities**

An *entity* is a set of *atoms* that are all about the same E.

#### **Example Entity**

| E | A | V | Tx | Op | U | Tm |
| ----- | ----- | ----- | ----- | :---: | ----- | ----- |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/favorite-color | :blue | e8ba9f4b-7d2d-4d71-af7e-d6f9046a934e | TRUE | f637d8ed-58c7-4494-bd54-74014d630676 | 2023-10-21T15:25:56.740Z |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/first-name | "John" | e8ba9f4b-7d2d-4d71-af7e-d6f9046a934e | TRUE | f637d8ed-58c7-4494-bd54-74014d630677 | 2023-10-21T15:25:56.740Z |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/last-name | "Doe" | e8ba9f4b-7d2d-4d71-af7e-d6f9046a934e | TRUE | f637d8ed-58c7-4494-bd54-74014d630678 | 2023-10-21T15:25:56.740Z |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/favorite-color | :green | c284d833-4b5a-41ff-b8c7-bd0cd0a2c404 | TRUE | f637d8ed-58c7-4494-bd54-74014d630679 | 2024-01-05T15:30:45.123Z |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/favorite-color | :blue | c284d833-4b5a-41ff-b8c7-bd0cd0a2c404 | FALSE | f637d8ed-58c7-4494-bd54-74014d630680 | 2024-01-05T15:30:45.123Z |

#### **Point-In-Time Entity Example**

A point-in-time (as-of) view of an entity considers only atoms whose *Op* is true as of a certain Tm. In the example above, John no longer prefers :blue as-of 2024-01-05T15:30:45.123Z, so the point-in-time view as-of 2024-01-05T15:30:45.123Z is:

| E | A | V | Tx | Op | U | Tm |
| ----- | ----- | ----- | ----- | :---: | ----- | ----- |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/first-name | "John" | e8ba9f4b-7d2d-4d71-af7e-d6f9046a934e | TRUE | f637d8ed-58c7-4494-bd54-74014d630677 | 2023-10-21T15:25:56.740Z |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/last-name | "Doe" | e8ba9f4b-7d2d-4d71-af7e-d6f9046a934e | TRUE | f637d8ed-58c7-4494-bd54-74014d630678 | 2023-10-21T15:25:56.740Z |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/favorite-color | :green | c284d833-4b5a-41ff-b8c7-bd0cd0a2c404 | TRUE | f637d8ed-58c7-4494-bd54-74014d630679 | 2024-01-05T15:30:45.123Z |

#### **Map View Example**

It is often convenient to consider a point-in-time view as only a three-tuple with Tx and Op elided:

| E | A | V |
| ----- | ----- | ----- |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/first-name | "John" |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/last-name | "Doe" |
| aedb54ac-1b09-497e-a9ab-ef94ae89c330 | :user/favorite-color | :green |

This three-tuple view is very similar to a programming language object where the E is analogous to this or self. The map view of an entity at a particular point in time captures this information more compactly, using the reserved pseudo-attribute name :db/id for E:

{:db/id aedb54ac-1b09-497e-a9ab-ef94ae89c330  
 :user/favorite-color :green  
 :user/first-name "John"  
 :user/last-name "Doe"}

### **Universal Schema**

In a relational database, you must specify a table schema that enumerates in advance the attributes (columns) an entity can have. By contrast, MyCloud requires only that you specify the properties of individual attributes. Any entity can then have any attribute. Because all atoms are part of a single relation, this is called a *universal schema*.

For example, consider storing an inventory database in MyCloud. All inventory items have a unique string identifier, so you create an :inv/id attribute. In addition, you create other named attributes, specifying the types and cardinalities of each.

You can then store various inventory items in the database, each with different attributes, as shown in the following table.

| E | A | V |
| ----- | ----- | ----- |
| d68c9f06-d260-4c16-bdb9-fce8f26d10bd | :inv/id | "SKU-1234" |
| d68c9f06-d260-4c16-bdb9-fce8f26d10bd | :inv/color | :green |
| 55543205-b503-4e96-8873-e03ce41f5add | :inv/id | "SKU-5678" |
| 55543205-b503-4e96-8873-e03ce41f5add | :inv/watts | 60000 |
| 55543205-b503-4e96-8873-e03ce41f5add | :doc/url | "http:…" |

Notice that, other than :inv/id, entities *d68c9f06-d260-4c16-bdb9-fce8f26d10bd* and *55543205-b503-4e96-8873-e03ce41f5add* have entirely disjoint attributes.

### **Defining Schema**

Each MyCloud database has a schema that describes the set and kind of attributes that can be associated with your domain entities.

A schema only defines the characteristics of the attributes themselves. It does not define which attributes can be associated with which entities. Decisions about which attributes apply to which entities are made by users.

This gives applications a great degree of freedom to evolve over time. For example, an application that wants to model a person as an entity does not have to decide up front whether the person is an employee or a customer. It can associate a combination of attributes describing customers and attributes describing employees with the same entity. An application can determine whether an entity represents a particular abstraction, customer or employee, simply by looking for the presence of the appropriate attributes.

There are two kinds of attributes in MyCloud:

- Domain attributes \- describe aspects of your domain data. You use domain attributes to describe the data about your domain entities.  
- Schema attributes \- describe aspects of the schema itself. Schema attributes are built-in and cannot be extended. You use schema attributes to define your domain attributes.

For more information see the Schema Documentation.

My application’s name is MyCloud. It’s a document based MacOS and iOS application that brings database capabilities to everyone through an intuitive schema and GUI.

At a high level MyCloud creates and manipulates HyperDocs which are composed of Blocks which are composed of Tags which are composed of Questions and Answers. It uses Datalog on the backend for querying and composition/editing of JSON files. The Swift App composes groups of “Transactions” which are facts for Datalog to use. Each Transaction must include the following:

TransactionID | Timestamp | Author | Parent | 

Everything in MyCloud is made of data. At its lowest level this data takes two forms:

1. Questions (Keys)  
2. Answers (Values)

Questions are formed with Keywords, and can come in one of two types:

1. Univalued  
2. Multivalued

Answers are formed using swift data primitives.

1. Numbers  
   1. int  
   2. float  
   3. frac  
2. Letters  
   1. unicode  
3. Colors  
4. Logic  
   1. is/isn’t  
   2. is this and that  
   3. is this or that  
   4. is not this

(Though I haven’t thought out all the core primitives I want to be available to users of the app.)

Questions and Answers are paired together to create Tags. Univalued Questions only accept one answer at a time while Multivalued Questions can accept multiple answers at once. In general tags are meant to enable classification schemes. MovieGenre, MovieTitle, MovieReleaseDate are just some examples. Some other examples include:

1. Name: Jake (Univalued)  
2. ID:  123e4567-e89b-12d3-a456-426614174000 (Univalued)  
3. Products: Shoes, Shirts (Multivalued)  
4. ShirtColors: Red, Green, Blue (Multivalued)

At a secondary level MyCloud 

1. ID  
2. Name  
3. Type  
4. Origin  
5. Genesis  
6. Authors

The most important questions we ask in the playground are The Six Dubs:

1. Who  
   1. Authors  
   2. Editors  
   3. Viewers  
2. What  
   1. Name  
   2. Unique ID  
3. When  
   1. Date  
   2. Time  
4. Where  
   1.   
5. Why  
6. With

* `Integers:`  
  * `Int: A 32-bit integer on 32-bit platforms and a 64-bit integer on 64-bit platforms.`  
  * `UInt: An unsigned version of Int.`  
  * `Int8, Int16, Int32, Int64: Signed integers with the specified number of bits.`  
  * `UInt8, UInt16, UInt32, UInt64: Unsigned integers with the specified number of bits.`  
* `Floating-Point Numbers:`  
  * `Float: A 32-bit floating-point number.`  
  * `Double: A 64-bit floating-point number, with greater precision than Float.`  
* `Boolean:`  
  * `Bool: Represents a Boolean value (true or false).`  
* `String and Character:`  
  * `String: A collection of characters for text manipulation.`  
  * `Character: Represents a single character.`  
* `Collection Types:`  
  * `Array: An ordered, indexed collection of values.`  
  * `Dictionary: A collection of key-value pairs.`  
  * `Set: An unordered collection of unique values.`  
* `Optional:`  
  * `Optional: Represents a variable that can hold either a value or nil, indicating the absence of a value.`  
* `Tuples:`  
  * `Tuples: Group multiple values into a single compound value. The values within a tuple can be of any type and do not need to be the same type as each other.`  
* `Range Types:`  
  * `ClosedRange: Represents a range that includes both its lower and upper value (e.g., 1...5).`  
  * `Range: Represents a range that includes the lower value but not the upper value (e.g., 1..<5).`  
  * `PartialRangeFrom, PartialRangeThrough, PartialRangeUpTo: Represent one-sided ranges.`  
* `Type Alias:`  
  * `typealias: Define an alternative name for an existing type.`

Numbers:

- Fraction  
- Unknown(algebraic)  
- Integer (Negative, Positive)  
- Decimal  
- Binary  
- Date  
- Quaternion  
- Angle (Radians, Degrees)

SSH Tunneling with WebSocket Connections:

SSH tunneling can be used to secure WebSocket connections, especially if the WebSocket data needs to be encrypted beyond standard WebSocket security (like WSS \- WebSocket Secure).  
This can be useful in environments with strict security requirements or where WebSocket traffic needs to be routed securely through an SSH connection.  
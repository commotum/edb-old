---
title: "Setting up Storage Services"
site: "Datomic"
source: "https://docs.datomic.com/operation/storage.html"
description: "Discover how to choose and configure storage options in Datomic."
word_count: 2770
---

This document walks through the process of provisioning a storage service for use with Datomic Pro.

### Storage Services

Storage service options are listed here:

The steps required to provision them and detailed instructions are included below (links provided here for convenience).

Note that you can move an application from one storage service to another simply by switching the connection string used by peers and the properties file used to start the transactor. All are fully API-compatible.

> All of the script commands described in this document must be executed from the root directory of the Datomic distribution.

### Storage Client Dependencies

Datomic depends on various storage client libraries. The recommended versions of all storage client libraries are in the `provided` scope of the Datomic pom.xml file:

```
mvn dependency:list -DincludeScope=provided
```

These jars are included in the Datomic distribution `/lib`, and are used by the transactor to access the storage systems. Peers will need the jars corresponding to the storage system on their classpath as well.

## In-Memory

In-memory storage is a convenience for local development.

Datomic Pro is a distributed system. Storage services, transactors, and peers are designed to run separately so that load on one process has minimal impact on other processes (while delivering on Datomic's semantic promises).

The in-memory database allows you to 'undistribute' Datomic by running all of these on the same virtual or physical hardware — no separate transactor or storage service. Your JVM application’s in-process memory is used for transient “storage” which disappears when your process exits.

> Note: no data is persisted after the application JVM process stops.

The connection URI takes only a database name, e.g. `my-db`:

```
datomic:mem://my-db
```

## Dev Mode

The dev storage protocol is intended for development. It runs an embedded JDBC server inside the transactor, and uses local disk files for storage.

noslide

```
bin/transactor config/samples/dev-transactor-template.properties
```

By default, the embedded storage runs with default passwords and is accessible only by other processes on the same machine. This configuration is intended for interactive development where application peers and the transactor are colocated.

### Securing Remote Access

To allow remote peers access to embedded storage you must do three things:

- Choose two passwords for the embedded storage
- Set the `storage-access` property
- Add a password to the connection URI used by peers

### Choose Passwords

Datomic's embedded JDBC storage has two passwords: an 'admin' password used by the transactor plus a 'datomic' password used by the peers. Once you set these passwords, you are responsible for remembering them. If you lose a password, you will not be able to access your data and will need to recover from a Datomic backup.

Set the passwords by setting the following transactor properties:

```
storage-admin-password=
storage-datomic-password=
```

### Set storage-access Property

To enable remote access to Datomic's embedded storage, set the following transactor property:

```
storage-access=remote
```

New transactor properties will take effect on the next transactor start.

### Add Password To Connection URI

After you set `storage-datomic-password`, peers must include the 'datomic' password in the [connection URI](https://docs.datomic.com/clojure/index.html#datomic.api/connect), e.g.

```
datomic:dev://my-host:4334/my-db?password=my-secret
```

### Rotating Passwords

Once you have set the `storage-datomic-password`, you can rotate it as follows:

- Set `old-storage-datomic-password` to the current password
- Set `storage-datomic-password` to a new password
- Restart the transactor
- Change peer Connection URIs to use the new password

The `storage-admin-password` can be rotated similarly. Be careful not to lose track of passwords while performing a password rotation.

## SQL Database

The steps to provision a SQL database as your storage service are:

- Setup a SQL database, or use an existing one
- Create SQL table (datomic\_kvs)
- Create SQL user(s), or use an existing one
- Get JDBC connection string
- Add JDBC driver dependencies to your project

There are scripts for doing each of the first three steps with [PostgreSQL](https://www.postgresql.org/), [MySQL](https://www.mysql.com/), and [Oracle](https://www.oracle.com/database/) in the Datomic distribution's *bin/sql* directory. You can run them using their respective command line or GUI admin tools.

For example, for Postgres at the command line:

noslide

```
psql -f bin/sql/postgres-db.sql -U postgres

psql -f bin/sql/postgres-table.sql -U postgres -d datomic

psql -f bin/sql/postgres-user.sql -U postgres -d datomic
```

The last script creates a user named 'datomic' with the password 'datomic'. You can use an existing user instead, or modify the script to create a user with a different username or password, if desired.

If you want to use a different SQL server, mimic the table and schema from one of the included databases.

### JDBC Drivers

Only the Postgres driver is included with the transactor distribution. For other SQL distributions, follow the steps below:

- Make the driver available on the classpath of the transactor by placing it in <datomic-install\>/lib.
- In your peer project, add a dependency for your specific JDBC driver in all SQL distributions. The example below shows how that looks for PostgreSQL.
- In a Maven-based build, add the following snippet to the dependencies section of your pom.xml:

noslide

```
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <version>42.5.1</version>
</dependency>
```

- In a Leiningen project, add the following to the dependencies section of your *project.clj* in the collection under `:dependencies` key:

```
[org.postgresql/postgresql "42.5.1"]
```

### Validation Query

Datomic uses a query to validate JDBC connections. By default, this query is:

noslide

```
select 1 from dual
```

For Oracle JDBC URIs, the query is:

noslide

```
select 1
```

> This validation query can be overridden via the [`datomic.sqlValidationQuery` system property](https://docs.datomic.com/operation/system-properties.html#transactor-properties).

## DynamoDB

> Datomic is not compatible with DynamoDB's [Global Tables](https://aws.amazon.com/dynamodb/global-tables/) feature.

The steps to provision [DynamoDB](https://aws.amazon.com/dynamodb/) as your storage service are:

- Set up an AWS account, or use an existing one.
- Create a DynamoDB table, IAM roles, S3 bucket, permissions, etc. Note that this process can be automated using Datomic's *ensure-transactor* command.
- Add the AWS DynamoDB dependency to your project.

> Datomic supports IAM roles. If you have a transactor that uses IAM user access keys, see [Migrating to IAM Roles](https://docs.datomic.com/legacy/migrate-to-roles.html) for information on how to switch to using roles. Check [AWS Access Control](https://docs.datomic.com/operation/aws-access-control.html) for information about Datomic's use of IAM roles and IAM user access credentials in general.

If you don't already have an AWS account, sign up using the [Amazon Web Services Console](https://aws.amazon.com/).

### DynamoDB AWS Peer Dependency

There is an example in the *provided* scope of the *pom.xml* in the Datomic directory for including the DynamoDB portion of the AWS Java SDK in a Maven project.

To configure deps.edn with this dependency, include the following in your dependencies list:

noslide

```
software.amazon.awssdk/dynamodb {:mvn/version "2.31.45"}
```

#### Legacy

When running a Datomic release prior to 1.0.7553, instead provide AWS SDKv1 dependencies:

noslide

```
com.amazonaws/aws-java-sdk-dynamodb {:mvn/version "1.12.564"}
```

### DDB Automated Setup

There is an automated process to create the necessary DynamoDB table, IAM roles, S3 bucket, permissions etc. To invoke it:

- Copy the *config/samples/ddb-transactor-template.properties* file to another location and give it a new name, for instance, *my-transactor.properties*. The template will work with the default settings, but you can edit your copy if you want to specify something explicitly.
- Export your AWS account's credentials as environment variables so that they're accessible to scripts:

noslide

```
export AWS_ACCESS_KEY_ID=<aws-access-key-id>

export AWS_SECRET_ACCESS_KEY=<aws-secret-access-key>
```

- Run the *ensure-transactor* command, specifying your copy of the properties file as the input and another filename as the output (the names can be the same):

noslide

```
bin/datomic ensure-transactor my-transactor.properties my-transactor.properties
```

> The *ensure-transactor* command will create the necessary AWS constructs, and emit an updated properties file containing information about what it created.

- If errors are reported, fix them, then repeat the process.
- Check the final output file in source control.

### DDB Manual Setup

If you are unable to run the *ensure-transactor* command or wish to customize your AWS configuration, manually configure your AWS setup to match the settings that will generate automatically:

#### Table Configuration

The DynamoDB table schema requires an "id" attribute of type string as the primary key with 'Keytype' "HASH":

```
{
  "Table":
    {
      "AttributeDefinitions": [
          {"AttributeName": "id",
            "AttributeType": "S"}
      ],

    ...

     "KeySchema": [
       {"KeyType": "HASH",
        "AttributeName": "id"}
      ]

    ...
    }
}
```

No indexing should be enabled.

#### IAM Role Configuration

Configure the IAM roles for the transactor and peers.

#### Transactor Role

It is required to apply a policy that grants the transactor role access to the Dynamo table you've created. Optionally, apply two other policies to the transactor role to enable log rotation to S3 and metric reporting.

#### DynamoDB Table Access (Storage Access)

> This policy is required for the transactor to run.

Replace <AWS-ACCOUNT-ID\> and <TABLE-NAME\>:

noslide

```
{"Statement":
 [{"Effect": "Allow",
   "Action": ["dynamodb:*"],
   "Resource": "arn:aws:dynamodb:*:<AWS-ACCOUNT-ID>:table/<TABLE-NAME>"}]}
```

#### DDB S3 Bucket Access (Log Rotation)

This policy is optional - it enables S3 log rotation. The policy below includes access to subfolders within the S3 bucket. Replace <S3-LOG-BUCKET\> with your created S3 log bucket address:

noslide

```
{"Statement":
 [{"Effect": "Allow",
   "Action": ["s3:PutObject"],
   "Resource": ["arn:aws:s3:::<S3-LOG-BUCKET>", "arn:aws:s3:::<S3-LOG-BUCKET>/*"]}]}
```

#### CloudWatch Metrics

This policy is optional - it enables the transactor to report metrics to CloudWatch.

noslide

```
{"Statement":
 [{"Effect":"Allow",
   "Resource":"*",
   "Condition":{"Bool":{"aws:SecureTransport":"true"}},
   "Action": ["cloudwatch:PutMetricData", "cloudwatch:PutMetricDataBatch"]}]}
```

#### DDB Peer Role

It's necessary to grant the peer application a few read-access permissions on the DynamoDB table. Use the following policy to create an IAM role that grants the Peer a few read-access permissions on the DynamoDB table. Replace <AWS-ACCOUNT-ID\> and <TABLE-NAME\>:

```
{"Statement":
 [{"Effect":"Allow",
   "Action": ["dynamodb:GetItem", "dynamodb:BatchGetItem", "dynamodb:Scan", "dynamodb:Query"],
   "Resource":
   "arn:aws:dynamodb:*:<AWS-ACCOUNT-ID>:table/<TABLE-NAME>"}]}
```

#### IAM User Keys

If you're using IAM user keys for the peer or transactor instead of roles, apply the above policies to the user.

#### DynamoDB Transactor Properties

- Configure the DynamoDB transactor properties file to the hostname being manually entered.
- Provide transactor and peer roles, as well as the name of the Dynamo table. Start from the *config/samples/ddb-transactor-template.properties* file in the Datomic distribution and insert values for the following from above, plus whatever other settings are necessary for your configuration:

noslide

```
host=<FINAL-HOST-NAME>
# e.g., ec2-ip-with-hyphens.compute-1.amazonaws.com
...
aws-dynamodb-table=<TABLE-NAME>
...
aws-transactor-role=<TRANSACTOR-IAM-ROLE-NAME>
...
aws-peer-role=<PEER-IAM-ROLE-NAME>
```

#### Other DDB Configuration

If required, for communication between peers and transactors on AWS, configure [security groups](https://docs.datomic.com/operation/aws.html#connecting-transactor-aws) as well.

> Refer to [the AWS docs](https://docs.datomic.com/operation/aws.html) for all other AWS configuration that is not directly related to using DynamoDB as a storage.

There is a [`datomic.ddbRequestTimeout`](https://docs.datomic.com/operation/system-properties.html) system property with a default of 1000 milliseconds. In a high volume, low latency system, you can turn this down to recover more quickly when DDB is poorly behaved.

> Check also [Dynamo Client Configuration](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/ClientConfiguration.html#withRequestTimeout-int-) for more details.

### Provisioning DynamoDB Throughput

By default, the ensure process creates a DynamoDB table with a modest provisioning level suitable for experimentation under low load. When you are ready to plan production use, review the [capacity planning docs](https://docs.datomic.com/operation/capacity.html) and use the [DynamoDB console](https://console.aws.amazon.com/dynamodb/home) to provision more throughput.

Every application will be different, but these three rules of thumb will get you started provisioning DynamoDB for Datomic:

- Budget at least as much for DynamoDB throughput as you

do for transactor EC2 instances. Split this budget evenly between read and write capacity. This will result in higher read capacity, as read capacity is less expensive.

- Measure your actual use. DynamoDB has great CloudWatch metrics that

will tell you how much capacity you are actually using, and you can configure warnings when your application is throttled to its provisioned limit.

- For bulk import jobs, turn write throughput up as high as necessary

(based on monitoring), and then turn it back down when you are done.

Once you have made an initial selection for DynamoDB throughput, you are ready to move on to [transactor setup](https://docs.datomic.com/setup/pro-setup.html#run-a-transactor).

## Running on AWS

Once you know everything is working, you may want to run your Transactor on the Amazon cloud. See [Running on AWS](https://docs.datomic.com/operation/aws.html) for instructions.

## Provisioning Cassandra3

Using Cassandra as a storage service requires a cluster of at least 3 nodes running Cassandra 3.x+ with a replica factor of at least 3. The Cassandra3 storage (using the `cass3` protocol) uses the [Datastax V4 Driver](https://www.datastax.com/blog/introducing-java-driver-4) and supports Cassandra-as-a-Service offerings such as [AstraDB](https://www.datastax.com/lp/cassandra-as-a-service) and [AWS Keyspaces](https://aws.amazon.com/keyspaces/). If you are looking for the Cassandra or Cassandra2 legacy storages, look [here](https://docs.datomic.com/legacy/legacy-storage.html).

The version compatibility matrix below shows which components from the Cassandra integrate with which Datomic release. Refer to the version of the Cassandra driver in the *provided* scope in the *pom.xml* in the Datomic directory and the [Datastax Java Driver Compatibility matrix](https://docs.datastax.com/en/driver-matrix/docs/java-drivers.html), to verify Datastax/Cassandra compatibility for a specific release of Datomic.

| Datomic | Protocol | Artifact Name | Driver Version | Cassandra Version | Datomic CQL Setup Scripts |
| --- | --- | --- | --- | --- | --- |
| 1.0.7075 | cass | com.datastax.cassandra/cassandra-driver-core | 3.7.1 | 2.x, 3.x, 4.x | bin/cassandra-\* |
| 1.0.7075 | cass2 | com.datastax.cassandra/cassandra-driver-core | 3.7.1 | 2.x, 3.x, 4.x | bin/cassandra2-\* |
| 1.0.7180 | cass/cass | com.datastax.cassandra/cassandra-driver-core | 3.7.1 | 2.x, 3.x, 4.x | bin/cassandra-\* |
| 1.0.7180 | cass/cass2 | com.datastax.cassandra/cassandra-driver-core | 3.7.1 | 2.x, 3.x, 4.x | bin/cassandra2-\* |
| 1.0.7180 | cass3 | com.datastax.oss/java-driver-core-shaded | 4.17 | 2.x, 3.x, 4.x | bin/cassandra3-\* |

The cluster needs to be configured to support the CQL native transport (this is on by default in modern releases of Cassandra). If you have an existing cluster that meets these requirements, you can use it. Otherwise, you need to configure one following the instructions on the Cassandra site.

Note that Datomic does not support running on a Cassandra cluster that spans [datacenters](#cassandra-limitations).

Once a cluster is configured, you must provision a keyspace and table (column family) for Datomic to use. You can do this using the CQL scripts provided in the distribution's *bin/cql* directory. You can execute the scripts using the *cqlsh* tool provided by Cassandra (use an appropriate superuser username and password, if required):

noslide

```
cqlsh -f bin/cql/cassandra3-keyspace.cql -u cassandra -p cassandra
```

noslide

```
cqlsh -f bin/cql/cassandra3-table.cql -u cassandra -p cassandra
```

Datomic provides optional support for Cassandra's internal username/password mechanism for authentication and authorization. If your cluster is configured to require authentication and authorization, you must also create a user for Datomic:

noslide

```
cqlsh -f bin/cql/cassandra3-user.cql -u cassandra -p cassandra
```

This script creates the user and grants it access to the Datomic keyspace.

Next, setup your transactor properties file:

- Copy the *config/samples/cassandra3-transactor-template.properties* file to another location and give it a new name, for instance, *cassandra3-transactor.properties*.
- Set the entry for *cassandra-host* to refer to a member of the cluster.
- If your cluster uses a non-standard port for the CQL native transport, set *cassandra-port*.
- If your cluster is configured to require authentication and authorization, set *cassandra-user* and *cassandra-password*.
- If your Cassandra cluster is configured with the necessary certificates to support SSL, and verified it works with a simple client (e.g., *cqlsh*), you can configure the transactor to use SSL by setting *cassandra-ssl* to true. See the [Cassandra docs](https://docs.datastax.com/en/developer/java-driver/4.17/manual/core/ssl/) for more information.
- For advanced configurations, Datomic also supports file based driver configuration via the *cassandra-config-file* transactor property or by copying the file to *./resources/application.conf* within the Datomic distribution directory. Peers similarly need the config file copied to *./resources/application.conf*, see the [Cassandra Driver V4 Docs](https://docs.datastax.com/en/developer/java-driver/4.17/manual/core/configuration/) for more information.
- You can optionally provide *cassandra-session-callback*, whose value is a name identifying either a Java static method (e.g. my.app.Cassandra.buildCluster), or a clojure function (e.g. my.app/build-cluster). This method/function takes as its single argument a map containing the above parameters, and which returns an instance of [SyncCqlSession](https://docs.datastax.com/en/drivers/java/4.17/com/datastax/oss/driver/api/core/cql/SyncCqlSession.html). Note that the argument type for this method is Object, e.g.
	```
	import com.datastax.oss.driver.api.core.cql.SyncCqlSession;
	import com.datastax.oss.driver.api.core.CqlSessionBuilder;
	import java.net.InetSocketAddress;
	import datomic.Util;
	import java.util.Map;
	public class DatomicCassandra {
	  public static SyncCqlSession build(Object args) {
	    Map <Object, Object> params = (Map<Object,Object>) args;
	    CqlSessionBuilder session = new CqlSessionBuilder();
	    session.addContactPoint(InetSocketAddress.createUnresolved(
	                          (String)params.get(Util.read(":host")),
	                          (Integer)params.get(Util.read(":port"))));
	    session.withAuthCredentials((String)params.get(Util.read(":user")),
	                                               (String)params.get(Util.read(":password")));
	    // Set other session options here
	    return session.build();
	  }
	}
	```
- If using [AWS Keyspaces](https://aws.amazon.com/keyspaces/) with IAM, you will need to add the additional Authentication Plugin dependency below (with the exclusions) and follow the [Keyspaces docs](https://docs.aws.amazon.com/keyspaces/latest/devguide/using_java_driver.html#java_tutorial.SigV4):

```
<dependency>
 <groupId>software.aws.mcs</groupId>
 <artifactId>aws-sigv4-auth-cassandra-java-driver-plugin</artifactId>
 <version>4.0.9</version>
 <scope>provided</scope>
 <exclusions>
  <exclusion>
   <groupId>com.datastax.oss</groupId>
   <artifactId>java-driver-core-shaded</artifactId>
  </exclusion>
  <exclusion>
   <groupId>com.datastax.oss</groupId>
   <artifactId>java-driver-core</artifactId>
  </exclusion>
 </exclusions>
</dependency>
```

In your peer project, add a dependency for the [DataStax Java Driver for Apache Cassandra](https://docs.datastax.com/en/developer/java-driver/4.17/manual/core/).

In a tools-deps based project, add the following to the deps section of your *deps.edn*:

noslide

```
com.datastax.oss/java-driver-core-shaded {:mvn/version "4.17.0"}
```

In a maven based build, add the following snippet to the dependencies section of your *pom.xml*:

noslide

```
<dependency>
  <groupId>com.datastax.oss</groupId>
  <artifactId>java-driver-core-shaded</artifactId>
  <version>4.17.0</version>
</dependency>
```

In a leiningen project, add the following to the dependencies section of your *project.clj*:

noslide

```
;; in collection under :dependencies key
[com.datastax.oss/java-driver-core-shaded "4.17.0"]
```

Note that Cassandra, Cassandra2 and Cassandra3 are different storage services! You can use [backup and restore](https://docs.datomic.com/operation/backup.html) to migrate databases from Cassandra or Cassandra2 to Cassandra3 (or between any storage services).

### Limitations

- Datomic does not support Cassandra's multi-datacenter replication. See [the HA section](https://docs.datomic.com/operation/ha.html#moving-across-data-centers) for more information on Datomic's storage level consistent copy requirements.
- Datomic does not support quorum operations in multi-datacenter environments due to the negative impact on availability and write performance.

### DynamoDB Local

Datomic supports using [DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html) as development-time storage. To configure this storage, edit the *ddb-transactor-template.properties* file as follows:

1. Change the protocol to *protocol=ddb-local*
2. Comment out the aws-dynamo-db-region property
3. Uncomment the aws-dynamodb-override-endpoint property and modify its values as appropriate

To start the local storage service:

1. Follow the [DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html) setup instructions
2. Run *bin/datomic ensure-transactor* with the properties files created above
3. Lastly, start the transactor.

DynamoDB Local requires an access key ID and secret access key, even though they are ignored. It is advisable to use dummy values for these properties during development against DynamoDB Local.
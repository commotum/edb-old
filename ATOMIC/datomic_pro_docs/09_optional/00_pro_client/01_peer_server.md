---
title: "Peer Server"
site: "Datomic"
source: "https://docs.datomic.com/operation/peer-server.html"
description: "Learn about the Peer Server, a JVM process that provides an interface for the Datomic Client library."
word_count: 826
---

The Client library can be used to connect lightweight processes to Datomic:

- It submits transactions and queries to the Peer Server
- Supports all query capabilities available to the peer
- Incurs none of the memory overhead of the full Peer library, does not perform local caching within the lightweight process
- Can support non-JVM languages

![clientarch_client.svg](_images/01_peer_server/clientarch-client.svg)

[Peer applications](https://docs.datomic.com/accessing/integrating-peer-lib.html) can co-exist with Client applications. Compare a [Client-based architecture](https://docs.datomic.com/images/clientarch_client.svg) (grey indicating an unused option) with an architecture using [both Peers and Clients](https://docs.datomic.com/images/clientarch_orig.svg).

## Peer Server

The Peer Server is a JVM process that provides an interface for the Datomic Client library which:

- Accepts queries and transactions from Datomic Clients
- Submits transactions to, and accepts changes from, the transactor
- Provides data access, caching, and query capability to connected Clients, reading from the storage service as needed

The Peer Server provides a gateway to the database, submitting transactions and accepting changes from the transactor. Like the Peer Library, the Peer Server provides caching and query capability for connected Datomic Clients.

The Client library requires that one or more Peer Servers be running. Peer servers are special intermediate nodes that handle queries and caching. Peer servers can be scaled horizontally just like Peers. Clients have a network hop for read operations as the queries are sent to Peer Servers. The Client library is much lighter weight than the Peer library at the cost of increasing latency on reads.

## Running the Peer Server

A Datomic Peer Server provides an interface for Datomic clients to access databases.

The Peer Server communicates with storage and the transactor to service both reads *from* and writes *to* Datomic databases. As such, the system running the Peer Server must have appropriate permissions to access storage, as specified in the [storage documentation](https://docs.datomic.com/operation/storage.html).

The Peer Server must also be able to connect to the live Transactor at the location specified by the `host` property in the [transactor properties](https://docs.datomic.com/operation/system-properties.html#transactor-properties) file. For more information on the connection between the peer server and the transactor, see the [deployment documentation](https://docs.datomic.com/operation/deployment.html).

> Note that Peer Servers do not own databases. As such, the Peer Server cannot create or destroy a database.

You can launch a Peer Server for one or more databases with the peer-server command:

```
bin/run -m datomic.peer-server\
        -h host\
        -p port\ 
        -a access-key,secret\
        -d dbname,URI\
```

- The `access-key` and `secret` are opaque strings. Clients must provide

a matching `access-key` and `secret` to connect.

- A `dbname` can consist of alphanumeric characters plus the hyphen '-'.
- The `-d` option may be passed multiple times to serve multiple

databases from a single peer server.

- The `-a` option may be passed multiple times to allow multiple access

keys and secrets, e.g. to support a key rotation scheme.

The database-uri is described in the [documentation for connect](https://docs.datomic.com/javadoc/datomic/Peer.html#connect-java.lang.Object-), e.g.

```
datomic:ddb://us-east-1/my-datomic-table/my-db
```

As a development convenience, a peer server can also serve transient, memory-only databases by passing a mem URI to the peer server command.

## Installing the Client Library

The Datomic client library includes both the [synchronous](https://docs.datomic.com/reference/client-reference.html#sync) and [asynchronous](https://docs.datomic.com/reference/client-reference.html#async) APIs, and is provided via [Maven central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22client-pro%22).

For Peer Server you will need `client-pro` and can follow the [Pro Client Getting Started Tutorial](https://docs.datomic.com/operation/client-getting-started.html#installing) steps for including the dependency in your Clojure CLI, Maven, or Leiningen project.

## Connecting to the Peer Server

To connect to a Peer Server, call [connect](https://docs.datomic.com/client-api/datomic.client.api.html#var-connect), passing a [client](https://docs.datomic.com/client-api/datomic.client.api.html#var-client) created with the arguments used to create the peer server:

- Peer server `host:port` for `:endpoint`
- Peer server `secret` for `:secret`
- Peer server `access-key` for `:access-key`

Along with the other connect hard-coded arguments shown in the example below:

```
(require
 '[datomic.client.api :as d])

(def client
  (d/client
   {:server-type :peer-server
    :endpoint "localhost:8998"
    :secret "my-secret"
    :access-key "my-access-key"
    :validate-hostnames false}))

(def conn (d/connect client {:db-name "my-db"}))
```

> The connection between clients and the peer server does not support SSL hostname validation. Both should be run within trusted network boundaries.

### Health Check

The peer server exposes /health as a health check endpoint, and will respond with a 2xx status code to HTTPS requests for {host}:{port}/health. Given a peer server running with:

```
bin/run -m datomic.peer-server -a k,s -d test,datomic:mem://test
```

The health check will respond:

```
curl -v -k https://localhost:8998/health
```

```
=>
> Trying 127.0.0.1...
> Connected to localhost (127.0.0.1) port 8998 (#0)
> TLS 1.2 connection using TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384
> Server certificate: Unknown
> GET /health HTTP/1.1
> Host: localhost:8998
> User-Agent: curl/7.43.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< Date: Thu, 28 Mar 2019 20:52:37 GMT
< Content-Type: text/plain
< Content-Length: 2
< Server: Jetty(9.3.7.v20160115)
< 
Connection 0 to host localhost left intact
```

## Limitations

The use of iteration in the Client API (i.e. taking from a query or datoms result channel repeatedly) requires that the client communicate with the same Peer Server instance that served the original request. In a system with multiple Peer Servers behind a load balancer we recommend you enable [sticky sessions](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/sticky-sessions.html) to ensure iterative requests are routed to the same Peer Server instance. If sticky sessions are unavailable, it is also possible to handle chunking and iteration manually using the `:limit` and `:offset` arguments.
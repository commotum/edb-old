---
title: "Client Library Reference"
site: "Datomic"
source: "https://docs.datomic.com/reference/client-reference.html"
description: "Learn about Datomic Client API and its functionalities."
word_count: 1668
---

All access to Datomic Cloud is via the Datomic client API. You can also use the client API with Datomic Pro and Datomic Local. This page covers everything you need to use the Datomic client API after you have added the client API to your project:

The [Day of Datomic Cloud](https://youtu.be/ZP-E2IgqKfA?t=2032) videos include an overview of the client API.

## Synchronous API

[Synchronous API](https://docs.datomic.com/client-api/datomic.client.api.html) functions have the following common semantics:

- They block the calling thread if they access a remote resource
- They return a value
- They indicate anomalies by [throwing an exception](#anomalies)

The following example shows a simple transaction using the [synchronous API](https://docs.datomic.com/client-api/datomic.client.api.html):

```
;; load the Synchronous API with prefix 'd'
(require '[datomic.client.api :as d])

;; transact a movie
(def result (d/transact conn {:tx-data [{:db/id "goonies"
                                         :movie/title "The Goonies"
                                         :movie/genre "action/adventure"
                                         :movie/release-year 1985}]}))

;; what id was assigned to The Goonies?
(-> result :tempids (get "goonies"))
```

## Asynchronous API

The [asynchronous API](https://docs.datomic.com/reference/client-reference.html#async) functions differ from those in the synchronous API in that:

- They return a core.async channel if they access a remote resource
- They never block the calling thread
- They place result(s) on the channel
- They place [anomalies](#anomalies) on the channel
- A [server type of `:ion`](#client) is only supported by the [synchronous API](#sync)

You must use a channel-taking operator such as [<!!](https://clojure.github.io/core.async/#clojure.core.async/%3C!!) to retrieve the result of an asynchronous operation, and you must explicitly check for anomalies:

```
;; load the Asynchronous API with prefix 'd', plus core.async and anomaly APIs
(require '[clojure.core.async :refer (<!!)]
         '[cognitect.anomalies :as anom]
         '[datomic.client.api.async :as d])

;; transact a movie
(def result (<!! (d/transact conn {:tx-data [{:db/id "goonies"
                                              :movie/title "The Goonies"
                                              :movie/genre "action/adventure"
                                              :movie/release-year 1985}]})))

;; what id was assigned to The Goonies?
(when-not (::anom/anomaly result)
  (-> result :tempids (get "goonies")))
```

## Client Object

All use of the Client API begins by [creating a client](https://docs.datomic.com/client-api/datomic.client.api.async.html#var-client). The args map for creating a client uses the following keys:

| Key | Value |
| --- | --- |
| :server-type | :cloud (or:ion for [ion applications](https://docs.datomic.com/ions/ions-reference.html#server-type-ion)) |
| :region | AWS region |
| :system | Your system name |
| :endpoint | Compute group endpoint, see below |
| :timeout | Optional msec timeout (default 60000) |
| :creds-profile | Optional [AWS Profile](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html) used to [retrieve keys](https://docs.datomic.com/operation/access-control.html#how-datomic-access-control-works) |
| :proxy-port | OAptional local port for SSH tunnel |

For most systems, the `:endpoint` is an API gateway endpoint. You can find the endpoint name under the `ClientApiGatewayEndpoint` [output](https://docs.datomic.com/operation/howto.html#template-outputs) from your [compute group's CloudFormation template](https://docs.datomic.com/operation/howto.html#compute-group-name) or use [the CLI tool's `describe-groups` command](https://docs.datomic.com/operation/cli-tools.html).

If the `ClientApiGatewayEndpoint` output is missing, you will need to get an `:endpoint` from the Datomic system administrator who configured [intra- and inter- VPC access](https://docs.datomic.com/operation/vpc-access.html) or [created a custom API Gateway](https://docs.datomic.com/operation/custom-api-gateway.html).

Client access through API Gateway is securely managed by [IAM permissions](https://docs.datomic.com/operation/access-control.html#how-datomic-access-control-works).

### Catalog Operations

The client object can be used to [list](https://docs.datomic.com/client-api/datomic.client.api.html#var-list-databases), [create](https://docs.datomic.com/client-api/datomic.client.api.html#var-create-database) and [delete](https://docs.datomic.com/client-api/datomic.client.api.html#var-delete-database) databases.

Create and delete operations take effect immediately. After a database is deleted, a Primary Compute Node will asynchronously delete all resources associated with a database.

### Connection

All use of a database is through a connection object. The [connect API](https://docs.datomic.com/client-api/datomic.client.api.html#var-connect) returns a connection, which is then passed as an argument to the transaction and db APIs.

Database values remember their association with a connection, so APIs that work against a single database (e.g. [datoms](https://docs.datomic.com/client-api/datomic.client.api.html#var-datoms)) do not need to redundantly specify a connection argument.

Connections do not require any special handling on the client side:

- Connections are thread safe.
- Connections do not need to be pooled.
- Connections do not need to be closed when not in use.
- Connections do not need to be recreated. On `:cognitect.anomalies/busy`, `cognitect.anomalies/unavailable` or `cognitect.anomalies/interrupted` the same connection can be reused on retry. The connection can be reused for other anomalies as well, but they may indicate an issue with the code creating the connection or using it.
- Connections are cached automatically, so creating the same connection multiple times is inexpensive.

### Handling Errors

Errors are represented as an [anomalies map](https://github.com/cognitect-labs/anomalies). In the sync API, you can retrieve the specific anomaly as the ex-data of an exception. In the async API, the anomaly will be placed on the channel.

### ::cognitect.anomalies/busy

If you get a *busy* response from the Client API, your request rate has temporarily exceeded the capacity of a node, and has already been through an exponential backoff and retry implemented by the client. At this point you have three options:

- For transactions: continue to retry the request at the application level with your own exponential backoff. The [Mbrainz Importer](https://github.com/Datomic/mbrainz-importer) example project demonstrates a [batch import with retry](https://github.com/Datomic/mbrainz-importer/blob/master/src/cognitect/xform/batch.clj#L70-L92).
- For queries: retry the request as long as the returned anomaly is retryable. If you consistently receive *busy* responses, you may wish to expand the capacity of your system, by [choosing new instance sizes](https://docs.datomic.com/operation/growing-your-system.html) or by adding/scaling a [query group](https://docs.datomic.com/operation/architecture.html#query-groups).
- Give up on completing the request.

### Sync API Error Example

The query below is incorrect because the `:find` clause uses a variable name `?nomen` that is not bound by the query. The sync API will throw an exception:

```
(require '[datomic.client.api :as d])
(d/q '[:find ?nomen
       :where [_ :artist/name ?name]]
     db)
```

```
=> ExceptionInfo Query is referencing unbound variables: #{?nomen}
```

You can discover the category of anomaly by inspecting the `ex-data` of the exception:

```
(ex-data *e)
```

```
=>
{:cognitect.anomalies/category
 :cognitect.anomalies/incorrect,
 :cognitect.anomalies/message
 "Query is referencing unbound variables: #{?nomen}",
 ...}
```

### Async API Error Example

With the async API, the same anomaly appears as a map on channel:

```
(require '[datomic.client.api.async :as d])
(<!! (d/q {:query '[:find ?nomen
                    :where [_ :artist/name ?name]]
           :args [db]}))
```

```
=>
{:cognitect.anomalies/category
 :cognitect.anomalies/incorrect,
 :cognitect.anomalies/message
 "Query is referencing unbound variables: #{?nomen}",
 ...}
```

### Timeouts

All APIs that communicate with a remote process enforce a timeout that you can specify via the args map:

| Key | Meaning | Default |
| --- | --- | --- |
| :timeout | Max msec wait before giving up | 60000 |

The call to create a client takes a `:timeout` which establishes the default for all API calls through that client.

API timeouts cause an anomaly with category `::anom/unavailable`, as shown below:

```
(d/q {:query '[:find (count ?name)
               :where [_ :artist/name ?name]]
      :args [db]
      :timeout 1})
(ex-data *e)
```

```
=>
#:cognitect.anomalies{:category :cognitect.anomalies/unavailable,
                      :message "Total timeout elapsed"}
```

### Chunked Results

Client APIs that can return an arbitrary number of results return those results in *chunks*. You can control chunking by adding the `:chunk` keyword to argument maps:

| Key | Meaning | Default | Max |
| --- | --- | --- | --- |
| :chunk | Max results in a single chunk | 1000 | (Unlimited) |

The default chunk size delivers good performance, and should only be changed to address a measurable performance need.

- Smaller chunks require more roundtrips and potentially higher latency overall, but require less memory on the client (assuming the client program drops each chunk after use).
- Larger chunks reverse this tradeoff, delivering results in fewer roundtrips but requiring more memory on the client side.

Note that the chunk size is orthogonal to the actual work done by the server:

- Datalog queries always realize the entire result in memory first, and then chunk the result back to the client.
- APIs that pull datoms from indexes ([datoms](https://docs.datomic.com/client-api/datomic.client.api.html#var-datoms), [index-range](https://docs.datomic.com/client-api/datomic.client.api.html#var-index-range) and [tx-range](https://docs.datomic.com/client-api/datomic.client.api.html#var-tx-range)) are lazy and realize chunks one at a time on both the server and client.

When you use an [ion client](https://docs.datomic.com/ions/ions.html), the "client" and "server" are the same process, and the `:chunk` argument is ignored.

[Synchronous API](https://docs.datomic.com/client-api/datomic.client.api.html) functions are designed for convenience. They return a single collection or iterable and do not expose chunks directly. The chunk size argument is nevertheless available and relevant for performance tuning.

The [asynchronous API](https://docs.datomic.com/client-api/datomic.client.api.async.html) provides more flexibility, exposing the chunks directly, one at a time, on a core.async channel. Processing results by async transduction is the most efficient way to deal with large results, and is demonstrated in the example below.

### Chunked Results Example

If you wanted to know the average length of an artist's name in the mbrainz data set, you could use the following async transduction to process all the names in chunks of 10,000.

```
(defn averager
  "Reducing fn that calculates average of its inputs"
  ([] [0 0])                  ;; init
  ([[n total]]                ;; complete
     (/ total (double n)))
  ([[n total] count]          ;; step
     [(inc n) (+ total count)]))

(defn nth-counter
  "Transform for chunked query results that gets the count of
the nth item from each result tuple."
  [n]
  (comp (halt-when :anom/category)  ;; fail fast
        cat                         ;; flatten the chunks
        (map #(nth % n))            ;; nth of chunk
        (map count)))

(def datoms-query {:limit -1
                   :chunk 10000
                   :index :aevt
                   :components [:artist/name]})
(->> (async/datoms db datoms-query)
     (a/transduce (nth-counter 2) averager (averager))
     <!!)
```

```
=> 13.893066724625081
```

The decomposition of async programs into named reducing fns and transforms such as `averager` and `nth-counter` makes it easy to test the parts of an async program entirely synchronously, and without the need for mocking and stubbing:

```
;; check that averager actually averages
(transduce identity averager [5 4])
```

```
=> 4.5
```

noslide

```
;; check that nth-counter returns count of nth element in chunk of tuples
(def chunks [[[:person-1 :name "Flintstone"]
              [:person-2 :name "Rubble"]]])
(sequence (nth-counter 2) chunks)
```

```
=> (10 6)
```

```
;; check that the reducing fn and xform compose
(transduce (nth-counter 2) averager chunks)
```

```
=> 8.0
```

You should add async transductions to your program when you have e.g. a measured performance need. For this simple example, all the code above could be replaced by a simple API query:

```
(d/q '[:find (avg ?ct)
       :with ?artist
       :where [?artist :artist/name ?name]
              [(count ?name) ?ct]]
     db)
```

```
=> [[13.893066724625081]]
```

### Offset and Limit

Client APIs that can return an arbitrary number of results allow you to request only part of these results by specifying the following optional keys in the args map:

| Key | Meaning | Default |
| --- | --- | --- |
| :offset | Number of results to omit from the beginning | 0 |
| :limit | Maximum number of results to return | 1000 |

You can specify a `:limit` of `-1` to request all results.

### Offset and Limit Example

The full [Mbrainz example](https://github.com/Datomic/mbrainz-importer) has over 600,000 names. The following query uses `:offset` and `:limit` to return 2 names starting with the 10th item in the query result.

```
;; query
{:query '[:find ?name
          :where [_ :artist/name ?name]]
          :args [db]
          :offset 10
          :limit 2}
```

```
=>
[["Kenneth Ishak & The Freedom Machines"]
 ["The Plastik"]]
```
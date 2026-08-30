;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;      http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS-IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns cognitect.http-client
  (:require
   [clojure.core.async :refer (put!) :as a]
   [clojure.string :as str])
  (:import
   [java.net SocketTimeoutException UnknownHostException ConnectException InetSocketAddress]
   [java.io InputStream IOException EOFException FileInputStream File]
   [java.nio ByteBuffer]
   [java.nio.channels UnresolvedAddressException]
   [java.net URI ProxySelector]
   [java.net.http HttpClient HttpRequest HttpResponse HttpClient$Version
    HttpHeaders HttpRequest$Builder HttpResponse$BodyHandlers
    HttpRequest$BodyPublishers HttpClient$Redirect HttpConnectTimeoutException HttpTimeoutException]
   [java.util.concurrent RejectedExecutionException TimeUnit TimeoutException
    CompletionException ThreadPoolExecutor ThreadFactory
    LinkedBlockingQueue ExecutorService]
   [java.util.function BiConsumer]
   [java.time Duration]
   [java.security KeyStore]
   [javax.net.ssl SSLException SSLContext SSLParameters TrustManagerFactory
    X509ExtendedTrustManager TrustManager]))

(set! *warn-on-reflection* true)

(def method-string
  {:get "GET"
   :post "POST"
   :put "PUT"
   :head "HEAD"
   :delete "DELETE"
   :patch "PATCH"})

(def response-body?
  (complement #{"HEAD"}))

(def restrict-headers
  #{"connection"
    "content-length"
    "expect"
    "host"
    "upgrade"})

(defn slurp-bytes
  [^ByteBuffer bb]
  (let [buff (.duplicate bb)
        n (.remaining buff)
        bytes (byte-array n)]
    (.get buff bytes)
    bytes))

(defn map->http-request
  "Convert a Ring request map into a java.net.http.HttpRequest. Note if :body is present
   it should be a ByteBuffer.

   See https://github.com/mmcgrana/ring/blob/master/SPEC."
  ^HttpRequest [{:keys [server-name server-port uri query-string request-method
                        scheme headers ^ByteBuffer body]
                 :or   {scheme "https"} :as m}]
  {:pre [(string? server-name) (integer? server-port)]}
  (let [;; Adds gzip by default, as done by Jetty
        ;; See: https://github.com/jetty/jetty.project/issues/7681
        headers (if (contains? headers "accept-encoding")
                  headers
                  (assoc headers "accept-encoding" "gzip"))
        ;; Headers listed in restrict-headers are managed by the jdk HttpClient
        ;; and cannot be set by user code
        ;; See: https://docs.oracle.com/en/java/javase/11/core/java-networking.html
        headers (apply dissoc headers restrict-headers)
        uri (str (name scheme) "://"
                 server-name ":" server-port
                 (if query-string
                   (str uri "?" query-string)
                   uri))
        req (HttpRequest/newBuilder (URI. uri))
        req (reduce-kv
             (fn [^HttpRequest$Builder req k v]
               (.header req ^String (name k) ^String v))
             req
             headers)
        req (if-let [to (::timeout-msec m)]
              (if (pos? to) ;; only allow positive timeouts to match jetty
                (.timeout ^HttpRequest$Builder req (Duration/ofMillis to))
                req)
              req)
        req (.method ^HttpRequest$Builder req
                     (method-string request-method)
                     (if body
                       (HttpRequest$BodyPublishers/ofByteArray (slurp-bytes body))
                       (HttpRequest$BodyPublishers/noBody)))]
    (.build ^HttpRequest$Builder req)))

(defn- header-map [^HttpHeaders http-headers]
  (reduce-kv
   (fn [m k v]
     (assoc m
            (str/lower-case k)
            (str/join "," v)))
   {}
   (.map http-headers)))

(defn error->category
  "Categorizes HttpClient exceptions after unwrapping CompletionEx.
  IOExceptions default to :unavailable"
  [throwable]
  (if (instance? CompletionException throwable)
    (recur (ex-cause throwable))
    (cond
      (instance? RejectedExecutionException throwable) :cognitect.anomalies/incorrect
      (instance? HttpConnectTimeoutException throwable) :cognitect.anomalies/unavailable
      (instance? HttpTimeoutException throwable) :cognitect.anomalies/unavailable
      (instance? TimeoutException throwable) :cognitect.anomalies/unavailable
      (instance? SocketTimeoutException throwable) :cognitect.anomalies/unavailable
      ;; DNS resolution failure wrapped in ConnectException needs to be checked first
      (instance? UnresolvedAddressException (ex-cause throwable)) :cognitect.anomalies/not-found
      (instance? ConnectException throwable) :cognitect.anomalies/unavailable
      (instance? UnknownHostException throwable) :cognitect.anomalies/not-found
      (instance? EOFException throwable) :cognitect.anomalies/unavailable
      (instance? SSLException throwable) :cognitect.anomalies/fault
      (instance? IOException throwable) :cognitect.anomalies/unavailable)))

(defn error->anomaly
  [t]
  (let [cat (or (error->category t) :cognitect.anomalies/fault)]
    {:cognitect.anomalies/category cat
     :cognitect.anomalies/message (ex-message t)
     ::throwable t}))

(defprotocol IClient
  (submit* [_ request ch]))

(defprotocol IStoppableClient
  (stop* [this]))

(defn submit
  "Submit an http request, channel will be filled with response. Returns ch.

Request map:

:server-name        string
:server-port         integer
:uri                string
:query-string       string, optional
:request-method     :get/:post/:put/:head
:scheme             :http or :https
:headers            map from downcased string to string
:body               ByteBuffer, optional
:cognitect.http-client/timeout-msec   opt, total request send/receive timeout
:cognitect.http-client/meta           opt, data to be added to the response map

content-type must be specified in the headers map
content-length is derived from the ByteBuffer passed to body

Response map:

:status              integer HTTP status code
:body                ByteBuffer, optional
:header              map from downcased string to string
:cognitect.http-client/meta           opt, data from the request

On error, response map is per cognitect.anomalies"
  ([client request]
   (submit client request (a/chan 1)))
  ([client request ch]
   {:pre [(every? #(contains? request %) [:server-name
                                          :server-port
                                          :uri
                                          :request-method
                                          :scheme])]}
   ;; Not Clojure 1.8 compatible. Using :pre for now
   ;; (s/assert ::submit-request request)
   (submit* client request ch)))

(defn- http-response->map
  [^HttpResponse response]
  {:status (.statusCode response)
   :headers (header-map (.headers response))
   :body (when (not-empty (.body response))
           (ByteBuffer/wrap (.body response)))})

(defn response-info
  "extracts http-response, or anomalizes throwable. must not throw
   according to  CompletableFuture/.whenComplete"
  [response throwable]
  (try
    (if response
      (http-response->map response)
      (error->anomaly throwable))
    (catch Throwable t
      ;; in case response extraction throws
      (error->anomaly t))))

(defn ops-limit-anom
  [pending-ops-limit]
  {:cognitect.anomalies/category :cognitect.anomalies/busy
   :cognitect.anomalies/message (str "Ops limit reached: " pending-ops-limit)})

(deftype Client
    [^HttpClient http-client pending-ops pending-ops-limit ^ExecutorService executor]
  IClient
  (submit*
    [_ request ch]
    (let [base (select-keys request [::meta])]
      (if (< pending-ops-limit (swap! pending-ops inc))
        (do
          (put! ch (merge base (ops-limit-anom pending-ops-limit)))
          (swap! pending-ops dec))
        (try
          (let [http-req (map->http-request request)]
            (-> (.sendAsync http-client http-req
                            (HttpResponse$BodyHandlers/ofByteArray))
                (.whenComplete
                 (reify BiConsumer
                   (accept [_ response throwable]
                     (put! ch (merge base (response-info response throwable)))
                     (swap! pending-ops dec))))))
          (catch Throwable t
            (put! ch (merge base (error->anomaly t)))
            (swap! pending-ops dec)))))
    ch)
  IStoppableClient
  (stop* [_]
    (when executor
      (.shutdown executor)
      (.awaitTermination executor 5 TimeUnit/SECONDS))))

(defn- trust-all-hostnames
  "Returns a trust manager that validates the certificate chain but does
  not verify hostname. Only instances of X509ExtendedTrustManager
  are wrapped; others pass through unchanged."
  [tm]
  (if (instance? X509ExtendedTrustManager tm)
    (proxy [X509ExtendedTrustManager] []
      (checkServerTrusted
        ([chain auth-type _]
         ;; Delegates to the 2-arity overload which validates the
         ;; certificate chain without hostname identity checking
         (.checkServerTrusted ^X509ExtendedTrustManager tm chain auth-type))))
    tm))

(defn ssl-context-factory
  "Create the SSL context required for the HttpClient.
  Returns a map containing the following keys:
  :trust-managers - array of javax.net.ssl.TrustManager loaded from Keystore
  :ssl-context - an instance of javax.net.ssl.SSLContext"
  ^SSLContext [{:keys [classpath-trust-store trust-store-password ^KeyStore trust-store validate-hostnames]
                :or {validate-hostnames true}}]
  (let [ks (or (when classpath-trust-store
                 (let [ks (KeyStore/getInstance (KeyStore/getDefaultType))]
                   (with-open [^InputStream in
                               (.getResourceAsStream
                                (.getContextClassLoader
                                 (Thread/currentThread))
                                classpath-trust-store)]
                     (.load ks in (char-array trust-store-password)))
                   ks))
               trust-store)
        tmf (TrustManagerFactory/getInstance
             (TrustManagerFactory/getDefaultAlgorithm))
        _ (.init tmf ks)
        tms (if validate-hostnames
              (.getTrustManagers tmf)
              (into-array TrustManager
                          (map trust-all-hostnames (.getTrustManagers tmf))))
        ctx (SSLContext/getInstance "TLS")
        _    (.init ctx nil tms nil)]
    {:trust-managers tms
     :ssl-context ctx}))

(defonce ^:private executor-idx (atom 0))

(defn http-cached-thread-pool
  "Create a cached thread pool with daemon threads for handling requests.
  Returns an instance of java.util.concurrent.ThreadPoolExecutor"
  ^ThreadPoolExecutor [{:keys [min-threads max-threads pending-ops-limit]
                        :or   {min-threads 8
                               max-threads 200
                               pending-ops-limit 64}}]
  (doto (ThreadPoolExecutor.
         min-threads
         max-threads
         60 TimeUnit/SECONDS
         (LinkedBlockingQueue. ^int (* 2 pending-ops-limit)))
    (.setThreadFactory
     (let [idx (atom 0)
           executor-id (swap! executor-idx inc)]
       (reify
         ThreadFactory
         (newThread
             [_ runnable]
           (doto
               (Thread. runnable)
             (.setName (str "http-client-executor-" executor-id "-worker-" (swap! idx inc)))
             (.setPriority Thread/NORM_PRIORITY)
             (.setDaemon true))))))))

(defn create
  "Creates an http-client that can be used with submit. Takes a config map with
   the following keys:

   :follow-redirects                 boolean, defaults to true
   :resolve-timeout                  in msec, default 5000
   :connect-timeout                  in msec, default 5000
   :idle-timeout                     default 0 (Jetty default)
   :max-connections-per-destination  default 64
   :max-threads                      200
   :min-threads                      8
   :pending-ops-limit                default 64
   :classpath-trust-store            classpath location of trust store
   :trust-store-password             trust store password
   :trust-store                      java.security.KeyStore instance
   :validate-hostnames               boolean, defaults to true
   :proxy-host                       optional host to use for proxy, string, defaults to 'localhost' if proxy-port is provided with no proxy-host
   :proxy-port                       optional port to use for proxy, int"
  [{:keys [follow-redirects resolve-timeout connect-timeout idle-timeout max-connections-per-destination
           max-threads min-threads
           pending-ops-limit proxy-host proxy-port]
    :or   {follow-redirects true
           resolve-timeout 5000
           connect-timeout 5000
           idle-timeout 0
           max-connections-per-destination 64
           max-threads 200
           min-threads 8
           pending-ops-limit 64}
    :as   config}]
  (let [{:keys [ssl-context]}
        (ssl-context-factory config)
        executor (http-cached-thread-pool config)
        builder (doto (HttpClient/newBuilder)
                      (.sslContext ssl-context)
                      (.executor executor)
                      (.connectTimeout (Duration/ofMillis connect-timeout))
                      (.followRedirects
                       (if follow-redirects
                         HttpClient$Redirect/NORMAL
                         HttpClient$Redirect/NEVER))
                      (.version HttpClient$Version/HTTP_1_1))
        builder (if proxy-port
                      (.proxy
                       builder
                       (ProxySelector/of
                        (InetSocketAddress.
                         (or ^String proxy-host "localhost")
                         ^int proxy-port)))
                      builder)]
    (Client.
     (.build builder)
     (atom 0)
     pending-ops-limit
     executor)))

(defn stop
  "Shuts down the http-client, releasing any resources that might be held
  open."
  [^Client client]
  (stop* client))

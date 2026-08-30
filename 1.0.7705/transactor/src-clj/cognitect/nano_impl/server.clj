;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

(ns cognitect.nano-impl.server
  (:import [java.nio ByteBuffer])
  (:require
   [clojure.core.async :as a :refer (chan <! >! >!! close! go map>)]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [cognitect.http-endpoint :as http-endpoint]
   [cognitect.nano-impl.marshaling :as nm]))
  
(set! *warn-on-reflection* true)

(defn uri->namespaced-op
  "takes uri string and returns a namespaced op as a keyword.
   For example, /~/namespace.of.this/opname is given as uri,
   this function returns keyword :namespace.of.this/opname.
   If leading segment is not ~, returns nil."
  [uri]
  (let [[pre tilde namespace op post] (str/split uri #"\/")]
    (when (and (= "" pre) (= tilde "~") namespace op (not post))
      (keyword namespace op))))

(defn namespaced-op
  "Takes a request map and returns a keyword :namespace.of.this/opname if
   either x-nano-op header is present and uri path is / or x-nano-op header
   is not present and uri path includes namespaced op as parsed by
   uri->namespaced-op. Otherwise returns nil."
  [req]
  (let [uri (:uri req)
             x-nano-op (when-let [^String s (get-in req [:headers "x-nano-op"])]
                         (keyword (if (= \: (.charAt s 0)) (subs s 1) s)))]
    (or (and (not (str/starts-with? uri "/~"))
             x-nano-op)
        (and (not x-nano-op)
             (uri->namespaced-op uri)))))

(defprotocol IServer
  (authenticate [_ nsop request] "If request is not a nano-router callback, ensures request
is authenticated.")
  (create-processing-callback [_] "Create a processing callback function that takes
req + resp channel and writes response + marshal channel to the
response channel. If the callback throws an exception or takes too
long, the HTTP endpoint will take care of responding to the user.")
  (unmarshal [_ request] "Do the inbound unmarshalling, returns a ring request, or
nil if the request not understood.")
  (dispatch [_ nsop request ch] "Dispatch the request, either returning a ring result
or returning nil and putting ring-result on ch later.")
  (marshal [_ response mime] "Do the outbound marshalling, returngs a ring response.")
  (marshal-loop [_] "Run the go loop that takes responses off the marshal chan,
marshals them, and puts them onto the HTTP endpoint response chan."))

(defn nano-router-callback?
  [nsop]
  (= (namespace nsop) "nano.router"))

(defn get-request-fn
  "Given a nano-services structure and namespaced op, returns a fn to handle a request.
   See :cognitect.nano-impl/nano-services spec."
  [nano-services nsop]
  (if (nano-router-callback? nsop)
    (get-in nano-services [:nano-router-callbacks nsop])
    (get-in nano-services [:ops nsop :fn])))

(s/fdef authenticate*
  :args (s/cat :nsop keyword?
               :request :cognitect.nano-impl/request
               :auth-callback fn?)
  :ret (s/nilable :cognitect.nano-impl/request))

(defn- authenticate*
  [nsop request auth-callback]
  (if (nano-router-callback? nsop)
    request
    (auth-callback request)))

(defn content-length
  "Returns content-length header string value as a long or nil
   when absent or not numeric"
  [req]
  (some-> (get-in req [:headers "content-length"]) (parse-long)))

(defn unmarshal*
  "Returns a request with an unmarshalled body, or nil"
  [{:keys [^ByteBuffer body] :as request} marshaling casters]
  (let [clen (or (content-length request) 0)
        buflen (if body (.remaining body) 0)]
    (cond
      (not= clen buflen)
      (do
        ((:alert casters) {:msg "Mismatched content-length"
                           ::content-length clen
                           ::buffer-length buflen})
        nil)
      (pos? clen) (nm/unmarshal request marshaling casters)
      :else (nm/read-query-string (dissoc request :body)))))

(defn- apply-filter
  [f request ch]
  (if f
    (f request ch)
    request))

(defrecord Server
    [auth-callback casters marshal-ch nano-services-ref marshaling registration-ref identity-uuid-ref request-filter]
  IServer
  (authenticate
    [_ nsop request]
    (authenticate* nsop request auth-callback))

  (create-processing-callback
    [this]
    (fn [request ch]
      (if (or (not registration-ref) @registration-ref)
        (if-let [nsop (namespaced-op request)]
          (if-let [areq (authenticate this nsop request)]
            (let [x-nano-process (get-in request [:headers "x-nano-process"])]
              ;;(prn ::identity-uuid-check :x-nano-process x-nano-process :identity-uuid @identity-uuid-ref)
              (if (or (nil? x-nano-process)
                      (= x-nano-process @identity-uuid-ref))
                (when-let [freq (apply-filter request-filter areq ch)]
                  (if-let [um (unmarshal this freq)]
                    (dispatch this nsop um ch)
                    (do
                      ((:metric casters)
                       {:name :nano.impl.invalid.requests
                        :value 1
                        :datadog/type :counter
                        ::uri (:uri request)})
                      (marshal this
                               {:status 400 :body {:cause "Bad Request"}}
                               (nm/error-mime-type request)))))
                (do
                  ((:metric casters)
                   {:name :nano.impl.invalid.identity
                    :value 1
                    :datadog/type :counter
                    ::uri (:uri request)})
                  (marshal this
                           {:status 400
                            :headers {"x-nano-process-error" x-nano-process}
                            :body {:cause "Bad Request"}}
                           (nm/error-mime-type request)))))
            (do
              ((:metric casters)
               {:name :nano.impl.failed.authns
                :value 1
                :datadog/type :counter
                ::uri (:uri request)})
              (marshal this
                       {:status 403 :body {:cause "Forbidden"}}
                       (nm/error-mime-type request))))
            (do
              ((:metric casters)
               {:name :nano.impl.failed.nsop
                :value 1
                :datadog/type :counter
                ::uri (:uri request)})
              (marshal this
                       {:status 400 :body {:cause "No op uniquely specified in x-nano-op header or in URI path"}}
                       (nm/error-mime-type request))))
        (do
          ((:metric casters)
           {:name :nano.impl.unregistered.requests
            :value 1
            :datadog/type :counter
            ::uri (:uri request)})
          (marshal this
                   {:status 503 :body {:cause "Service Unavailable"}}
                   (nm/error-mime-type request))))))
  (unmarshal
   [_ request]
   (unmarshal* request marshaling casters))
  (dispatch
    [this nsop request ch]
    (if-let [f (get-request-fn @nano-services-ref nsop)]
      (let [mime (nm/response-mime-type request)]
        (when-let [resp (f request (map> (fn [resp]
                                           {:response resp :ch ch :mime mime})
                                         marshal-ch))]
          (>!! marshal-ch {:response resp :ch ch :mime mime}))
        nil)
      (let [mime (nm/error-mime-type request)]
        ((:metric casters) {:name :nano.impl.invalid.ops
                            :value 1
                            :datadog/type :counter
                            ::uri (:uri request)})
        (marshal this {:status 400 :body {:cause "Invalid Op"}} mime))))
  (marshal
    [_ response mime]
    (nm/marshal response mime marshaling casters))
  (marshal-loop
    [this]
    (go
     (loop []
       (when-let [{:keys [response mime ch]} (<! marshal-ch)]
         (try
           (>! ch (marshal this response mime))
           (catch Throwable t
             ((:alert casters) {:msg "http-endpoint.server marshal-loop error" :ex t})))
         (recur))))))

(defn create
  "Create an Nano Impl Server

Required keys:
:server                         endpoint-config arg
:nano-services-ref              atom of nano-services map
:identity-uuid-ref              atom of unique identifier

Optional keys:
:auth-callback                  takes req, returns adorned req or nil, default returns req
:casters                        map with :alert/:event/:metric/:dev keys
:registration-ref               if not nil, an atom that derefs to boolean to indicate if registration is successful
:marshaling                     map of marshaling options
:request-filter                 fn to transform request processing

Casters are callback fns that expect args as per cognitect.caster. All
caster callbacks are optional.

Request filters are a limited form of e.g. pedestal interceptors. A
request filter takes a request and a channel, and can return either

- a possibly transformed request object for dispatch per the nano-services map
- nil to indicate that the filter has entirely taken over processing the request

Returns a map with

:server                          the endpoint record (config settigs)
:http-endpoint                  the underlying jetty object (for diagnostics only)
:join                           fn that blocks until endpoint shuts down
:close                          fn that shuts down the endpoint"
  [{:keys [server nano-services-ref casters marshaling registration-ref identity-uuid-ref request-filter]}]
  (when casters ((:event casters) {:msg (str "starting " ::create)}))
  (let [{:keys [processing-concurrency auth-callback] :or {auth-callback identity}} server
        marshal-ch (chan processing-concurrency)
        ni-server (map->Server {:auth-callback auth-callback
                                :casters casters
                                :nano-services-ref nano-services-ref
                                :marshal-ch marshal-ch
                                :marshaling marshaling
                                :identity-uuid-ref identity-uuid-ref
                                :registration-ref registration-ref
                                :request-filter request-filter})
        {:keys [endpoint jetty join close]} (http-endpoint/create-endpoint
                                              (assoc server
                                                :processing-callback (create-processing-callback ni-server)))]
    (dotimes [_ 4]
      (marshal-loop ni-server))
    {:http-endpoint endpoint
     :server ni-server
     :join join
     :close (fn []
              (close! marshal-ch)
              (close))}))

(comment

  (require :reload 'cognitect.nano-impl.server)
  (in-ns 'cognitect.nano-impl.server)

  (def nano-services {:ops
                      {:op/one
                       {:fn (constantly
                             {:status 200
                              :headers {"a" "b"}})}
                       :op/two
                       {:fn (constantly
                             {:status 200})}}})

  (def nano-services {:groups #{:my/first :your/last}
                     :ops
                     {:nano.router/ensure-group
                      {:fn (fn [request]
                            {:status 200
                             :headers {"content-type" "application/transit+json"}
                             :body (java.util.UUID/randomUUID)})
                       :allow-get false
                       :groups [{:group :my/first :routing :balanced}]}
                      :nano.router/ensure-group-member
                      {:fn (fn [request]
                            {:status 200
                             :headers {"content-type" "application/transit+json"}
                             :body (identity (:body request))})
                       :allow-get false
                       :groups [{:group :my/first :routing :balanced}
                                {:group :your/last :routing :oldest}]}
                      :nano.router/ensure-ops
                      {:fn (fn [request]
                            {:status 200
                             :headers {"content-type" "application/transit+json"}
                             :body (identity (:body request))})
                       :allow-get false
                       :groups [{:group :my/first :routing :balanced}]}
                      :nano.router/handle-ops
                      {:fn (fn [request]
                            {:status 200
                             :headers {"content-type" "application/transit+json"}
                             :body (identity (:body request))})
                       :allow-get false
                       :groups [{:group :my/first :routing :balanced}
                                {:group :your/last :routing :oldest}]}}})

  (def casters {:alert println :event println :metric println :dev println})
  (def config {:server {:connection-concurrency 10
                        :bind-address {:host "localhost" :port 8080}
                        :pending-ops-limit 2
                        :processing-concurrency 4
                        :ping-path "/~"
                        :bounding-timeout 5000}
               :casters casters
               :nano-services nano-services})

  (def config {:server {:connection-concurrency 10
                        :bind-address {:host "localhost" :port 8080 :scheme "http"}
                        :pending-ops-limit 2
                        :processing-concurrency 4
                        :bounding-timeout 60000
                        :ping-path "/~"
                        :identity-uuid (str (UUID/randomUUID))}
               :casters casters
               :advertise-addr {:server-name "localhost" :server-port 8080 :scheme "http"}
               :nano-router {:server-name "localhost" :server-port 8080 :scheme "http"}
               :marshaling {:application/transit+json {}}
               :nano-services nano-services})

  ;; test with curl -D headers localhost://... from remote-dev REPL
  (def result (create config))

  (in-ns 'cognitect.http-endpoint.jetty)

  ;; close the server
  ((:close result)))

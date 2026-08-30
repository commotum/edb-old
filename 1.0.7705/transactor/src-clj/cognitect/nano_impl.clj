;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

(ns cognitect.nano-impl
  (:require
   [clojure.core.async :refer (chan close! >!!)]
   [cognitect.nano-impl.aws-detect :as aws]
   [cognitect.http-client :as client]
   [cognitect.nano-impl.registration :as r]
   [cognitect.nano-impl.server :as server]
   [clojure.spec.alpha :as s])
  (:import [java.util UUID]))

(set! *warn-on-reflection* true)

(defn channel?
  [x]
  (instance? clojure.core.async.impl.protocols.Channel x))

(s/def ::handler (s/keys :opt-un [::read-opts ::write-opts]))
(s/def ::marshaling (s/every-kv string? ::handler))
(s/def ::scheme #{"http" "https"})
(s/def ::server-port integer?)
(s/def ::server-name string?)
(s/def ::addr (s/keys :req-un [::server-name ::server-port ::scheme]))
(s/def ::advertise-addr ::addr)
(s/def ::access-key string?)
(s/def ::region string?)
(s/def ::avail-zone string?)
(s/def ::access-key-and-region (s/or :no-access-key-and-region #(not-any? #{:access-key :region} (keys %))
                                     :access-key-and-region (s/keys :req-un [::access-key ::region])))
(s/def ::nano-router (s/nilable (s/and ::addr
                                       ::access-key-and-region)))
(s/def ::service-id (s/and keyword?
                           #(nil? (namespace %))))
(s/def ::code-id (s/nilable string?))
(s/def ::data-id (s/nilable string?))
(s/def ::group-name keyword?)
(s/def ::group (s/or :gn ::group-name
                     :map (s/keys :req-un [::service-id ::code-id]
                                  :opt-un [::data-id])))
(s/def ::ext-map (s/nilable map?))
(s/def ::priority string?)
(s/def ::routing #{:balanced :balanced-az :first :last :hash :hash-first})
(s/def ::target-set keyword?)
(s/def ::process-count pos-int?)
(s/def ::target string?)
(s/def ::groups (s/every (s/and (s/keys :req-un [::group]
                                        :opt-un [::priority
                                                 ::ext-map
                                                 ::target-set
                                                 ::process-count])
                                (fn [{:keys [target-set process-count]}]
                                  (or (and target-set process-count)
                                      (and (nil? target-set) (nil? process-count)))))))

;; could refer to specs from http-endpoint component
(s/def ::fn fn?)
(s/fdef ::req-fn
        :args (s/cat :req (s/keys) :ch channel?)
        :ret (s/nilable (s/keys)))
(s/def ::allow-get boolean?)
(s/def ::allow-no-access-key boolean?)
(s/def :cognitect.nano-impl.op-def/groups
  (s/every (s/and (s/keys :req-un [::group]
                          :opt-un [::routing
                                   ::target]))))
(s/def ::op-def (s/keys :req-un [::fn :cognitect.nano-impl.op-def/groups]
                        :opt-un [::allow-get
                                 ::allow-no-access-key]))
(s/def ::op-name keyword?)
(s/def ::ops (s/every-kv ::op-name ::op-def))
(s/def ::nano-router-callbacks (s/map-of ::op-name ::fn))
(s/def ::nano-services (s/keys :req-un [::groups ::ops]
                               :opt-un [::nano-router-callbacks]))

(s/def ::http-client #(satisfies? cognitect.http-client/IClient %))
(s/def ::identity-uuid string?)
(s/def ::callback fn?)

(s/def ::alert fn?)
(s/def ::event fn?)
(s/def ::metric fn?)
(s/def ::dev fn?)
(s/def ::casters (s/keys :opt-un [::alert ::event ::metric ::dev]))
(s/def ::create (s/and (s/keys :req-un [::server]
                               :opt-un [::nano-router
                                        ::marshaling
                                        ::advertise-addr
                                        ::casters
                                        ::nano-services])
                       (fn [{:keys [nano-router advertise-addr]}]
                         (or (and nano-router advertise-addr)
                             (nil? nano-router)))))
(s/def ::config (s/and (s/keys)
                       (fn [{:keys [nano-router avail-zone]}]
                         (or (and nano-router avail-zone)
                             (nil? nano-router)))))

(s/def ::query-string (s/nilable ::client/query-string))
(s/def ::request (s/keys :req-un [::client/server-name
                                  ::client/server-port
                                  ::client/uri
                                  ::client/request-method
                                  ::client/scheme]
                         :opt [::client/timeout-msec
                               ::client/meta]
                         :opt-un [::client/body
                                  ::query-string
                                  ::client/headers]))



(defn- conform!
  [spec x]
  (let [conformed (s/conform spec x)]
    (if (= ::s/invalid conformed)
      (throw (IllegalArgumentException. ^String (s/explain-str spec x)))
      conformed)))

(defprotocol INanoImpl
  (membership
   [_ group]
   "Channels nano-router's information about members of group.")
  (re-register
    [_ nano-services]
    "Updates internal state with new nano-services, re-registering as necessary")
  (unique-id
   [_]
   "Returns instance's unique id as a string."))

(deftype NanoImpl
  [server client config group-uuids-ref identity-uuid-ref reg-ch registration-callback close]
  INanoImpl
  (membership
   [_ group]
   (if-let [group-uuid (get @group-uuids-ref group)]
     (r/membership client config {:group-uuid group-uuid})
     (doto (chan 1)
       (>!! {:error ::not-registered})
       (close!))))
  (re-register
    [_ nano-services]
    (let [nano-services (conform! ::nano-services nano-services)]
      (>!! reg-ch nano-services)))
  (unique-id
    [_] @identity-uuid-ref)
  
  java.lang.AutoCloseable
  (close [_] (close)))

(defn create
  "Create a Nano Impl

Required keys:
:server               server/create args
:nano-services        nano-services map, see below

Optional keys:
:advertise-addr       an address, see 'address?'
:marshaling           marshaling map, see below
:nano-router          nano-router map, see below
:avail-zone           AWS availability zone, see below
:casters              map with :alert/:event/:metric/:dev keys

Nano-sevices map:
:groups               vector of group membership descriptions
                      ext-map can be nil
:ops                  map from fully-qualified op name to op map

Op map:
:fn                   opfn takes request and channel
:groups               vector of group binding descriptions
:allow-get            true if HTTP get allowed for this
:allow-no-access-key  true if op is allowed without an access key


Request and response maps are the same as in http-endpoint, except

- request and response :body are maps (not byte buffers)
- marshaling is automatic based on accept and content-type headers

Normally, you should NOT set the \"content-type\" header. If you
set \"content-type\", automatic marshaling is disabled and the
response :body must be a ByteBuffer.

Group membership description map:
:group                keyword naming group
:ext-map              optional extenions data in map format
:priority             optional priority string for first/last routing

Group binding description map:
:group                keyword naming group
:routing              keyword naming routing type

The marshaling map has the following nesting:
content-type          string
  :write-opts         options map passed to underlying write/encode fn
  :read-opts          options map passed to underlying read/decode fn

Nano-router map:

An address, see address?, with additional optional keys used for
signing requests to nano-router registration SPI.

:access-key           optional, an S3 bucket/path to use for hmac signing
:region               optional, the AWS region where the nano-router is deployed

If you specify a nano-router you must also specify an AWS availability zone
under :avail-zone.

Casters are callback fns that expect args as per cognitect.caster. All
caster callbacks are optional.

Returns an instance implementing INanoImpl and AutoCloseable."
  [config]
  (let [{:keys [nano-router casters nano-services]} (conform! ::create config)
        registration-ref (when nano-router (atom false))
        nano-services-ref (atom nano-services)
        identity-uuid-ref (atom nil)
        config (-> config
                   (dissoc :nano-services)
                   (merge {:registration-ref registration-ref
                           :identity-uuid-ref identity-uuid-ref
                           :nano-services-ref nano-services-ref}))
        _ (conform! ::config config)
        client (client/create {})  ;; TODO: allow shared
        group-uuids-ref (atom {})
        server (server/create config)
        on-event (or (:dev casters)
                     identity)
        callback (fn [event]
                   (when-let [r (:register event)]
                     (reset! group-uuids-ref (:group-uuids r)))
                   (when-let [r (and registration-ref (contains? event :registered))]
                     (reset! registration-ref r))
                   (on-event event))
        reg-ch (chan 10)
        registrar (when nano-router (r/registrar client config {:reg-ch reg-ch
                                                                :msec 30000
                                                                :callback callback}))
        nano-impl (->NanoImpl
                   server
                   client
                   config
                   group-uuids-ref
                   identity-uuid-ref
                   reg-ch
                   callback
                   (fn []
                     (client/stop client) ;; TODO: share and don't shutdown
                     (when registrar ((:close registrar)))
                     ((:close server))))]
    (when nano-services
      (re-register nano-impl nano-services))
    nano-impl))

(comment
  (require :reload 'cognitect.nano-impl)
  (in-ns 'cognitect.nano-impl)

  ;; edit to match
  (def router {:server-name "us-east-1.int.spi-a9e81e07.datomic.com"
               :server-port 8181
               :scheme "http"
               :access-key "datomic-test-us-east-1/app-privs/nano-router/spi"
               :region "us-east-1"})
  (def task-repl {:server-name "10.2.43.17" :server-port 8181 :scheme "http"})
  (def ip (.getHostAddress (java.net.Inet4Address/getLocalHost)))
  (def casters {:alert println :metric println :event println :dev println})
  (def config {:server {:connection-concurrency 10
                        :bind-address {:host ip
                                       :port 8181}
                        :pending-ops-limit 2
                        :processing-concurrency 4
                        :ping-path "/~"
                        :bounding-timeout 60000}
               :casters casters
               :advertise-addr task-repl
               :marshaling {"application/transit+json" {:read-opts nil :write-opts nil}}
               :nano-router router
               :nano-services {:groups [{:group :example/one
                                         :ext-map nil
                                         :priority "1"}
                                        {:group :example/two
                                         :ext-map nil
                                         :priority "2"}]
                               :ops
                               {:cognitect.nano-impl/hello-sync
                                {:fn (fn [request ch]
                                       {:status 200
                                        :headers {"content-type" "application/transit+json"}
                                        :body {:hello "world"}})
                                 :allow-get true
                                 :groups [{:group :example/one :routing :balanced}
                                          {:group :example/two :routing :balanced}]}
                                :cognitect.nano-impl/hello-async
                                {:fn (fn [request ch]
                                       (future
                                         (Thread/sleep 500)
                                         (clojure.core.async/>!! ch {:status 200
                                                                     :headers {"content-type" "application/transit+json"}
                                                                     :body {:hello "world"}}))
                                       nil)
                                 :allow-get true
                                 :groups [{:group :example/one :routing :balanced}
                                          {:group :example/two :routing :balanced}]}}}})

  ;; without router
  (def nano-impl (create (dissoc config :nano-router)))

  ;; with router
  (def nano-impl (create config))

  ;; without initial nanoservices
  (def nano-impl (create (dissoc config :nano-services)))

  ;; re-register
  (re-register nano-impl {:groups [{:group :example/foo}]
                          :ops
                          {:cognitect.nano-impl/foo
                           {:fn (fn [request ch]
                                  {:status 200
                                   :headers {"content-type" "application/transit+json"}
                                   :body {:hello "world"}})
                            :allow-get true
                            :groups [{:group :example/foo :routing :balanced}]}}})
  (clojure.core.async/poll! (membership nano-impl :example/foo))
  (.close nano-impl)
  )


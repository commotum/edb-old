;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

(ns cognitect.nano-impl.registration
  (:require
   [clojure.core.async :as a :refer [<!! <! >! alts! chan go go-loop map> promise-chan timeout thread put! >!!]]
   [clojure.spec.alpha :as s]
   cognitect.http-client.specs
   [cognitect.http-client :as client]
   [cognitect.nano-impl.marshaling :as marshal]))

(set! *warn-on-reflection* true)

(s/fdef create-request
        :args (s/cat :config map?
                     :op-name keyword?
                     :data map?)
        :ret ::client/submit-request)

(defn create-request
  "Given

config          nano-impl config map
op-name         a namespaced keyword
data            request data

Returns an http-client request for the nano-router backend."
  [config op-name data]
  {:pre [(keyword? op-name) data]}
  (let [mime "application/transit+json"
        request-content (marshal/encode-body data mime (:marshaling config))
        {:keys [server-name server-port scheme]} (:nano-router config)
        qualified-op-name (str "nano.router/" (name op-name))]
    {:server-name server-name
     :server-port server-port
     :scheme scheme
     :request-method :post
     :cognitect.http-client/timeout-msec 10000
     :headers {"content-type" mime
               "x-nano-op" qualified-op-name
               "host" server-name}
     :op qualified-op-name
     :uri "/"
     :body request-content}))

(s/fdef http-success?
        :args (s/cat :response map?))

(defn http-success?
  [response]
  (and (:status response)
       (<= 200 (:status response) 299)
       (map? (:body response))))

(defn http-client-error-cast
  "Returns a tuple of caster type, caster map."
  [op-name response]
  [(if (= ::client/unknown (::client/error response)) :alert :event)
   {:msg (str op-name " request failed")
    ::op-name op-name
    ::response response}])

(s/def ::error ::client/submit-error-response)
(s/fdef translate-response
        :args (s/cat :config (s/keys)
                     :opname keyword?
                     :response (s/keys))
        :ret (s/or :body map?
                   :error (s/keys :req-un [::error])))
(defn translate-response
  [{:keys [casters marshaling] :as config} op-name {:keys [status body headers] :as response}]
  (if (::client/error response)
    (let [[type m] (http-client-error-cast op-name response)]
      ((get casters type) m)
      {:error response})
    (let [body (marshal/decode-body body (get headers "content-type") marshaling)
          response' (assoc response :body body)]
      (if (http-success? response')
       body
       {:error response'}))))

;; sign-request is disabled because we have not updated to use the
;; async hmac-authn code
#_(defn sign-request
  "Given a conn-impl and a Ring compliant request, return a signed request as
   specified by the AWS Signature 4 signing algorithm."
  [auth-info req]
  (let [signed (TODO sign)
        ;;ch (chan 1)
        ;;_ (authn/verify signed ch)
        ;;verified (<!! ch)
        ]
    ;; (prn ::sign-http-req#raw http-req)
    ;;(prn ::sign-http-req#signed signed)
    ;; (prn ::sign-http-req#verified verified)
    signed))

(defn send-request
  "Sends http request to the server.
Content-type is always application/transit+json.
cl - HttpClient
op-name - namespaced op name
data - Clojure data to set request body

Channels a map with :error on error, or unmarshalled response otherwise"
  [client config op-name data]
  (let [ch (chan 1)
        req (create-request config op-name data)
        #_signed-req #_(sign-request (assoc (:nano-router config)
                                     :service "nano.router/spi"
                                     :casters (:casters config)) req)]
    (client/submit client
                   req
                   (map> (partial translate-response config op-name) ch))
    ch))

(defn group-name
  [group]
  (if (keyword? group)
    group
    (let [{:keys [service-id code-id data-id] :or {code-id "dev"}} group]
      (keyword (name service-id)
               (format (str "%s" (when data-id "-%s"))
                       code-id
                       data-id)))))

(defn ensure-group
  "Ensures that a group exists.

:name - string, name of the group.

Channels a map with :result key or :error"
  [client {:keys [casters] :as config} {:keys [group target-set process-count] :as g}]
  (assert g)
  (let [input (merge {:name (group-name group)}
                     (when (and target-set process-count)
                       {:target-set target-set
                        :process-count process-count}))]
    (send-request client config :ensure-group input)))

(defn ensure-groups
  "Inputs:
client    an http-client
config    nano-router config
groups    collection of keywords naming groups

Channels a map with either of

:group-uuids   a map from group name to assigned uuid
:error         what went wrong"
  [client config groups]
  (go-loop [result {}
            [group & more] groups]
    (if group
      (let [ret (<! (ensure-group client config group))]
        (if (:error ret)
          ret
          ;; TODO: remove legacy :result check
          (recur (update result :group-uuids assoc (:group group) (:group-uuid ret))
                 more)))
      result)))

(defn ensure-group-member
  "Ensure that the given endpoint is a member of the given group.
   Takes a map whose keys are,
   :group-uuid - string, unique group identity (returned from ensure-group)
   :endpoint - string, the root URL for the endpoint
   :identity-uuid - string, a unique uuid generated by the nano ez impl lib
   :avail-zone - (string?), the AWS availability zone
   :priority - string, optional, used for order list based first/last routing
   :ext-map - map, an optional map of additional data for endpoint

   Channels a map with :result key or :error"
  [cl {:keys [casters] :as config} {:keys [group-uuid endpoint identity-uuid avail-zone priority ext-map] :as input}]
  {:pre [group-uuid endpoint identity-uuid avail-zone]}
  (send-request cl config :ensure-group-member input))

(defn addr-map->str
  [{:keys [server-name server-port scheme]}]
  (let [port (or server-port
                 (case = scheme
                       "http" 80
                       "https" 443))]
    (str scheme "://" server-name ":" port)))

(defn ensure-group-members
  "Channels a map with {:result :ok} on success or {:error msg} on failure."
  [client config identity-uuid avail-zone group-uuids groups]
  (let [endpoint (addr-map->str (:advertise-addr config))]
    (go-loop [[group-info & more] (seq groups)]
      (if group-info
        (let [{:keys [group priority ext-map]} group-info
              uuid (group-uuids group)
              ret (<! (ensure-group-member client config
                                           (merge 
                                            {:group-uuid uuid
                                             :endpoint endpoint
                                             :identity-uuid identity-uuid
                                             :avail-zone avail-zone}
                                            (when priority {:priority priority})
                                            (when ext-map {:ext-map ext-map}))))]
          (if (:error ret)
            ret
            (recur more)))
        {:result :ok}))))


(defn ensure-ops
  "Takes an ops map as per the :nano-services :ops path in a nano-impl
config. Channels a map with either :ops, the set of ops ensured, or
with :error."
  [cl {:keys [casters] :as config} ops]
  (send-request cl config :ensure-ops
                {:ops (mapv
                       (fn [[op {:keys [allow-get allow-no-access-key]}]]
                         {:name op :allow-get (boolean allow-get) :allow-no-access-key (boolean allow-no-access-key)})
                       ops)}))

(defn handle-ops-args
  "Convert ops and group-uuids into a seq of arg maps suitable
for handle-op"
  [ops group-uuids]
  {:handlers (mapcat
              (fn [[k v]]
                (map
                 (fn [{:keys [group routing target]}]
                   (merge {:name k
                           :group-uuid (group-uuids group)
                           :routing routing}
                          (when target
                            {:target target})))
                 (:groups v)))
              ops)})

(defn handle-ops
  "Channels an empty map on success, :error map on failure"
  [cl {:keys [casters] :as config} ops group-uuids]
  (send-request cl config :handle-ops (handle-ops-args ops group-uuids)))

(defn membership
  "Retrieves information about all endpoints that are members of a specified group.
   Takes a map whose key is,
   :group-uuid - string, a group's unique id, returned from ensure-group

   Channels a map with :members key, or :error."
  [cl {:keys [casters] :as config} {:keys [group-uuid] :as input}]
  (assert group-uuid)
  (send-request cl config :membership input))

(defn register
  "Register the nano-service described by config.

:identity-uuid         service unique id

Channels a map with

:group-uuids           map from group names to group uuids
:ensure-groups-ret     return from ensure-groups
:ensure-groups-members return from ensure-group-members
:ensure-ops-ret        return from ensure-ops
:handle-ops-ret        return from handle-ops"
  [client config {:keys [identity-uuid]}]
  {:pre [(string? identity-uuid)]}
  (let [ch (promise-chan)]
    (go
     (let [nano-services @(:nano-services-ref config)
           ensure-groups-ret (<! (ensure-groups client config (:groups nano-services)))
           group-uuids (:group-uuids ensure-groups-ret)
           group-members-ret (when group-uuids
                               (<! (ensure-group-members client config identity-uuid
                                                         (:avail-zone config)
                                                         group-uuids
                                                         (:groups nano-services))))
           ensure-ops-ret (when (:result group-members-ret)
                            (<! (ensure-ops client config (:ops nano-services))))
           handle-ops-ret (when (:result ensure-ops-ret)
                            (<! (handle-ops client config (:ops nano-services) group-uuids)))]
       (>! ch
           {:group-uuids group-uuids
            :ensure-groups-ret ensure-groups-ret
            :ensure-group-members-ret  group-members-ret
            :ensure-ops-ret ensure-ops-ret
            :handle-ops-ret handle-ops-ret})))
    ch))

(defn need-to-reregister?
  [{:keys [status body] :as response}]
  (or (::client/error response) ;; http-client reported comms error, not HTTP status code
      (and (not= 200 status)
           (:nano.router/register body))))

(defn heartbeat
  "Returns a channel that will get true unless the service needs
to reregister with the router.
Callback will be called with the result of each heartbeat call
to the nano-router."
  [client {:keys [casters] :as config} {:keys [endpoint callback group-uuids identity-uuid]}]
  {:pre [(string? endpoint) callback (string? identity-uuid)
         casters
         (every? string? group-uuids)]}
  (go-loop [[group-uuid & more] group-uuids]
    (if group-uuid
      (let [request {:group-uuid group-uuid
                     :endpoint endpoint
                     :identity-uuid identity-uuid}
            ret (<! (send-request client config :heartbeat request))]
        (callback {:heartbeat request :return ret})
        (if (need-to-reregister? ret)
          (do
            ((:alert casters) {:msg "registration failed" :result ret})
            (callback {:registered false})
            false)
          (do
            (callback {:registered true})
            (recur more))))
      true)))

(defn registered?
  "Tests that the map channeled by a call to register indicates
a successful registration."
  [x]
  (and (-> x :ensure-groups-ret :group-uuids)
       (-> x :ensure-group-members-ret :result)
       (-> x :ensure-ops-ret :result)
       (-> x :handle-ops-ret :result)))

(defn heartbeat-loop
  "Heartbeats an endpoint's group memberships, calling register
when necessary to stay registered with the router.

:msec            interval between heartbeats
:callback        called with each heartbeat return
:identity-uuid   string, a unique uuid generated by the nano ez impl lib

Returns a map with

:close          fn to stop the loop."
  [client {:keys [casters] :as config}
   {:keys [msec callback identity-uuid]}]
  (let [endpoint (addr-map->str (:advertise-addr config))
        close-ch (a/chan 1)]
    (go-loop [to (timeout 0)
              reg nil]
      (let [reg (if (registered? reg)
                  reg
                  (let [r (<! (register client config {:identity-uuid identity-uuid}))]
                    (callback {:register r})
                    r))
            [_ ch] (alts! [close-ch to])]
        (when-not (= ch close-ch)
          (when casters ((:metric casters)
                          {:name :nano.impl.heartbeat
                           :value 1
                           :unit :count}))
          (let [to (timeout msec)]
            (if (<! (heartbeat client config {:endpoint endpoint
                                              :callback callback
                                              :group-uuids (-> reg :ensure-groups-ret :group-uuids vals)
                                              :identity-uuid identity-uuid}))
              (recur to reg)
              (recur to nil))))))
    {:close (fn [] (a/close! close-ch))}))

(s/fdef ::update-registration
        :args (s/cat :http-client :cognitect.nano-impl/http-client
                     :config (s/keys :req-un [:cognitect.nano-impl/nano-router
                                              :cognitect.nano-impl/:nano-services-ref
                                              :cognitect.nano-impl/casters
                                              :cognitect.nano-impl/callback])
                     :nano-services :cognitect.nano-impl/nano-services
                     :identity-uuid :cognitect.nano-impl/identity-uuid))

(defn update-registration
  [http-client {:keys [nano-router nano-services-ref casters] :as config} nano-services identity-uuid registration-callback]
  (try
    (let [conformed-nano-services (s/conform :cognitect.nano-impl/nano-services nano-services)]
      (if (= :clojure.spec/invalid conformed-nano-services)
        ((:alert casters) {:msg (s/explain-str :cognitect.nano-impl/nano-services nano-services)})
        (do
          (reset! nano-services-ref conformed-nano-services)
          (when nano-router
            (go
              (let [r (<! (register http-client config {:identity-uuid identity-uuid}))]
                (registration-callback {:register r
                                        ;; TBD: not true if register failed, should check with
                                        ;; registered?
                                        :registered true})))))))
    (catch Throwable t
      ((:alert casters) {:msg (str ::update-registration " failed: " (.getMessage ^Throwable t))
                         :ex t}))))


(defn registrar*
  [client
   {:keys [nano-router nano-services-ref identity-uuid-ref casters] :as config}
   {:keys [msec callback reg-ch close-ch] :as opts}]
  ((:event casters) {:msg (str "starting " ::registrar)})
  (let [endpoint (addr-map->str (:advertise-addr config))]
    (go 
     (try
       (loop [to-ch (timeout msec)
              reg nil
              identity-uuid nil]
         (let [to-ch' (timeout msec)
               [v ch] (alts! [close-ch reg-ch to-ch])]
           ((:metric casters)
             {:name :nano.impl.registrar
              :value 1
              :unit :count})
           (condp = ch
             reg-ch
             (do
               ((:dev casters) {:fn ::registrar* :msg "new registration"})
               (let [nano-services v
                     conformed-nano-services (s/conform :cognitect.nano-impl/nano-services nano-services)]
                 (if (= :clojure.spec/invalid conformed-nano-services)
                   (do
                     ((:alert casters) {:msg (s/explain-str :cognitect.nano-impl/nano-services nano-services)})
                     (recur to-ch' nil nil))
                   (do
                     (reset! nano-services-ref conformed-nano-services)
                     (if nano-router
                       (let [identity-uuid (str (java.util.UUID/randomUUID))
                             _ (reset! identity-uuid-ref identity-uuid)
                             r (<! (register client config {:identity-uuid identity-uuid}))]
                         (callback {:register r :registered (registered? r)})
                         (recur to-ch' r identity-uuid))
                       (recur to-ch' nil nil))))))
             to-ch
             (if identity-uuid
               (do
                 ((:dev casters) {:fn ::registrar* :msg "heartbeat"})
                 (let [r (if (registered? reg)
                           reg
                           (let [r (<! (register client config {:identity-uuid identity-uuid}))]
                             (callback {:register r :registered (registered? r)})
                             r))]
                   (recur to-ch'
                          (when (<! (heartbeat client config {:endpoint endpoint
                                                              :callback callback
                                                              :group-uuids (-> r :ensure-groups-ret :group-uuids vals)
                                                              :identity-uuid identity-uuid}))
                            r)
                          identity-uuid)))
               (do
                 ((:dev casters) {:fn ::registrar* :msg "no heartbeat, no identity-uuid"})
                 (recur to-ch' nil nil)))

             close-ch
             (do
               ((:event casters) {:msg (str "stopping " ::registrar)})
               :done))))
       (catch Throwable t
         ((:alert casters) {:msg (str ::registrar " failed: " (.getMessage ^Throwable t))
                            :ex t})
         t)))))

(defn registrar
  [client config opts]
  (let [endpoint (addr-map->str (:advertise-addr config))
        close-ch (a/chan 1)]
    (go-loop [] (when-not (= :done (<! (registrar* client config (assoc opts :close-ch close-ch))))
                  (recur)))
    {:close (fn [] (put! close-ch :close))}))



(comment

  (do
    (require :reload
             'cognitect.nano-impl
             'cognitect.nano-impl.registration)
    (in-ns 'cognitect.nano-impl.registration)
    (require '[clojure.pprint :as pp]
             '[clojure.repl :refer :all]
             '[clojure.spec.test :as stest])
    (use 'clojure.repl))

  (def client (client/create {}))

  (def group-uuids {:example/one "6f329386-b7ae-409d-83f3-257afab129e7",
                    :example/two "e378076b-c707-4923-b17f-1f226b779726"})
  
  (def identity-uuid (str (java.util.UUID/randomUUID)))
  (def router {:server-name "10.0.42.183"
               :server-port 8181
               :scheme "http"})
  (def router {:server-name "internal-dev-nano-int-spi-1551795083.us-east-1.elb.amazonaws.com"
               :server-port 8181
               :scheme "http"
               :access-key "datomic-test-us-east-1/app-privs/nano-router/spi"
               :region "us-east-1"})

  (def nano-services {:groups [{:group :example/one
                                :priority "1"}
                               {:group :example/two
                                :priority "2"}]
                      :ops
                      {:cognitect.nano-impl/echo
                       {:fn (fn [request]
                              {:status 200
                               :headers {"content-type" "application/transit+json"}
                               :body (:body request)})
                        :allow-get true
                        :groups [{:group :example/one :routing :balanced}
                                 {:group :example/two :routing :balanced}]}}})
  (def casters {:alert println :metric println :event println :dev println})
  (def casters {:alert identity :metric identity :event identity :dev identity})
  (def config {:nano-router router
               :casters casters
               :nano-services-ref (atom nano-services)})

  (<!! (ensure-group client config :foo))
  (<!! (ensure-group client config nil))

  (<!! (ensure-groups client config (map :group (:groups nano-services))))

  (<!! (ensure-group-members client
                             config
                             identity-uuid
                             "us-east-1c"
                             group-uuids
                             (:groups nano-services)))

  (:ops nano-services)
  (<!! (ensure-ops client config
                   {:my/op {:allow-get false}
                    :my.other/op {:allow-get true}}))

  (<!! (send-request client config :handle-ops {:handlers
                                                [{:name :fred
                                                  :group-uuid "6f329386-b7ae-409d-83f3-257afab129e7",
                                                  :routing :balanced}]}))
  
  (handle-ops-args (:ops nano-services) group-uuids)
  (<!! (handle-ops client config (:ops nano-services) group-uuids))

  (<!! (membership client config {:group-uuid (first (vals group-uuids))}))

  (<!! (register client config {:identity-uuid identity-uuid
                                :callback println}))
  


  (def heartbeat-arg {:group-uuids (vals group-uuids)
                      :endpoint (addr-map->str (:nano-router config))
                      :callback println
                      :identity-uuid identity-uuid})
  (<!! (heartbeat client config heartbeat-arg))




  ;; don't need hloop once registrar is working
  (def heartbeat-loop-arg {:callback println
                           :msec 5000
                           :identity-uuid identity-uuid})
  (def hloop (heartbeat-loop client config heartbeat-loop-arg))
  ((:close hloop))
  ;; don't need this ^^^ once registrar is working

  (def reg-ch (chan 10))
  (def r (registrar client
                    (assoc config :identity-uuid-ref (atom nil))
                    {:reg-ch reg-ch
                     :msec 30000
                     :callback println}))
  (def new-nano-services {:groups [{:group :example/three
                                    :priority "1"}]
                          :ops
                          {:cognitect.nano-impl/echo-three
                           {:fn (fn [request]
                                  {:status 200
                                   :headers {"content-type" "application/transit+json"}
                                   :body (:body request)})
                            :allow-get true
                            :groups [{:group :example/three :routing :balanced}]}}})
  (clojure.core.async/>!! reg-ch new-nano-services)

  ((:close r))

  (stest/instrument [`update-registration])

  (def nano-services-ref (atom {}))
  (update-registration client
                       (merge config {:casters casters
                                      :nano-services-ref nano-services-ref
                                      :identity-uuid "foo"
                                      :callback println})
                       {:groups [{:group :example/three
                                  :priority "1"}]
                        :ops
                        {:cognitect.nano-impl/echo-three
                         {:fn (fn [request]
                                {:status 200
                                 :headers {"content-type" "application/transit+json"}
                                 :body (:body request)})
                          :allow-get true
                          :groups [{:group :example/tthree :routing :balanced}]}}}))

;; Copyright (c) Cognitect, Inc.




(ns cognitect.http-client.specs
  (:require [clojure.core.async :as a]
            [clojure.spec.alpha :as s]
            [cognitect.http-client :as http])
  (:import [clojure.core.async.impl.protocols Channel]
           [java.nio ByteBuffer]))

(defn channel?
  [x]
  (instance? Channel x))

(defn- keyword-or-non-empty-string?
  [x]
  (or (keyword? x)
      (and (string? x)
           (not-empty x))))



(s/def :cognitect.http-client/server-name string?)
(s/def :cognitect.http-client/server-port int?)
(s/def :cognitect.http-client/uri string?)
(s/def :cognitect.http-client/query-string string?)
(s/def :cognitect.http-client/request-method keyword?)
(s/def :cognitect.http-client/scheme keyword-or-non-empty-string?)
(s/def :cognitect.http-client/headers map?)
(s/def :cognitect.http-client/body #(instance? ByteBuffer %))
(s/def :cognitect.http-client/timeout-msec int?)
(s/def :cognitect.http-client/meta map?)
(s/def :cognitect.http-client/status int?)
(s/def :cognitect.http-client/error keyword?)
(s/def :cognitect.http-client/throwable #(instance? Throwable %))
(s/def :cognitect.http-client/submit-request (s/keys :req-un [:cognitect.http-client/server-name
                                                              :cognitect.http-client/server-port
                                                              :cognitect.http-client/uri
                                                              :cognitect.http-client/request-method
                                                              :cognitect.http-client/scheme]
                                                     :opt [:cognitect.http-client/timeout-msec
                                                           :cognitect.http-client/meta]
                                                     :opt-un [:cognitect.http-client/body
                                                              :cognitect.http-client/query-string
                                                              :cognitect.http-client/headers]))
(s/def :cognitect.http-client/submit-error-response (s/keys :req [:cognitect.http-client/error]
                                                            :opt [:cognitect.http-client/throwable
                                                                  :cognitect.http-client/meta]))
(s/def :cognitect.http-client/submit-http-response (s/keys :req-un [:cognitect.http-client/status]
                                                           :opt [:cognitect.http-client/meta]
                                                           :opt-un [:cognitect.http-client/body
                                                                    :cognitect.http-client/headers]))
(s/def :cognitect.http-client/submit-response (s/or :http-response :cognitect.http-client/submit-http-response
                                                    :error-response :cognitect.http-client/submit-error-response))

;; need to read channel w/ timeout for generative testing
(s/def :cognitect.http-client/response-ch (s/and (s/conformer #([% (first (a/alts!! [% (a/timeout 5000)]))]))
                                                 (s/tuple channel? :cognitect.http-client/submit-response)
                                                 (s/conformer first)))

(s/def :cognitect.http-client/client #(satisfies? http/IClient %))

(s/fdef cognitect.http-client/submit
        :args (s/cat :client :cognitect.http-client/client
                     :request :cognitect.http-client/submit-request
                     :chan (s/? channel?))
        :ret :cognitect.http-client/response-ch)

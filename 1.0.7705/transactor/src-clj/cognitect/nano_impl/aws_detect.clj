;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

;; Legacy, probably could be removed.
;; See https://app.shortcut.com/datomic/story/193577.
(ns cognitect.nano-impl.aws-detect
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.string :as str])
  (:import [java.net URL HttpURLConnection]))

(set! *warn-on-reflection* true)

(defn- http-request
  "Makes an HTTP request to the given URL with specified method and headers.
   Returns the response body as a string, or nil on error."
  [{:keys [url method headers conn-timeout read-timeout]
    :or {conn-timeout 1000 read-timeout 1000}}]
  (let [^URL url (io/as-url url)
        conn (doto ^HttpURLConnection (.openConnection url)
               (.setConnectTimeout conn-timeout)
               (.setReadTimeout read-timeout)
               (.setRequestMethod method))]
    (doseq [[k v] headers]
      (.setRequestProperty conn k (str v)))
    (try
      (with-open [stream (.getInputStream conn)]
        (slurp stream))
      (catch Throwable _ nil))))

(defn imds-token
  ([]
   (imds-token {}))
  ([{:keys [token-ttl]
     :as opts
     :or {token-ttl 21600}}]
   (http-request (merge opts
                        {:url "http://169.254.169.254/latest/api/token"
                         :method "PUT"
                         :headers {"X-aws-ec2-metadata-token-ttl-seconds" token-ttl}}))))

(defn imds-request
  ([path]
   (imds-request path {}))
  ([path
    {:keys [token]
     :as opts
     :or {token (imds-token)}}]
   (http-request (merge opts
                        {:url (str "http://169.254.169.254/latest/" path)
                         :method "GET"
                         :headers {"X-aws-ec2-metadata-token" token}}))))

(def normalize-metadata-keys
  "Return copy of metadata tag map with keyword keys whose
   :'s have been converted to -'s"
  (memoize (fn [tags] (into {}
                        (map (fn [[k v]] [(-> k
                                            (clojure.string/replace \: \-)
                                            keyword) v]))
                        tags))))

(defn get-ec2-private-ip
  "Returns private ip or nil"
  []
  (imds-request "meta-data/local-ipv4"))

(defn get-ec2-public-ip
  "Returns public ip or nil"
  []
  (imds-request "meta-data/public-ipv4"))

(defn get-ec2-instance-type
  "Returns instance type or nil"
  []
  (imds-request "meta-data/instance-type"))

(defn get-ec2-instance-id
  "Returns AWS instance id or nil"
  []
  (imds-request "meta-data/instance-id"))

(def running-in-ec2-ref
  (delay (boolean (get-ec2-public-ip))))

(defn running-in-ec2?
  []
  @running-in-ec2-ref)

(defn az->region
  "Given an availability zone string, parses out region."
  [az]
  (subs az 0 (dec (.length ^String az))))

(defn location
  "Returns a tuple of region and availability zone when running on AWS, otherwise nil."
  []
  (when (running-in-ec2?)
    (when-let [az (imds-request "meta-data/placement/availability-zone")]
      [(az->region az) az])))

(defn account-id
  "Returns the account id when running on AWS, otherwise nil."
  []
  (when (running-in-ec2?)
    (when-let [info (imds-request "meta-data/iam/info")]
      (let [arn ((json/read-str info) "InstanceProfileArn")]
        (nth (str/split arn #":") 4)))))
;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

(ns cognitect.nano-impl.aws-detect
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.string :as str])
  (:import [java.net URL]))

(set! *warn-on-reflection* true)

(defn quickstream
  "Returns inputStream at URL path, or fails exceptionally in
   timeout msec"
  [path timeout]
  (let [^URL url (io/as-url path)
        conn (doto (.openConnection url)
               (.setConnectTimeout timeout)
               (.setReadTimeout timeout))]
    (.getInputStream conn)))

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
  (try
    (slurp (quickstream "http://169.254.169.254/latest/meta-data/local-ipv4" 1000))
    (catch Throwable _ nil)))

(defn get-ec2-public-ip
  "Returns public ip or nil"
  []
  (try
    (slurp (quickstream "http://169.254.169.254/latest/meta-data/public-ipv4" 1000))
    (catch Throwable _ nil)))

(defn get-ec2-instance-type
  "Returns instance type or nil"
  []
  (try
    (slurp (quickstream "http://169.254.169.254/latest/meta-data/instance-type" 1000))
    (catch Throwable _ nil)))

(defn get-ec2-instance-id
  "Returns AWS instance id"
  []
  (try
    (slurp (quickstream "http://169.254.169.254/latest/meta-data/instance-id" 1000))
    (catch Throwable _ nil)))

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
    (let [az (slurp (quickstream "http://169.254.169.254/latest/meta-data/placement/availability-zone" 1000))]
      [(az->region az) az])))

(defn account-id
  "Returns the account id when running on AWS, otherwise nil."
  []
  (when (running-in-ec2?)
    (let [arn ((json/read-str (slurp (quickstream "http://169.254.169.254/latest/meta-data/iam/info" 1000))) "InstanceProfileArn")]
      (nth (str/split arn #":") 4))))

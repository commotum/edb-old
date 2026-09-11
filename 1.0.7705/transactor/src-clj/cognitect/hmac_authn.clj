;; Copyright (c) Cognitect, Inc.




(ns cognitect.hmac-authn
  ^{:doc
    "Utilities for signing and verifying Ring requests according to
     AWS Signature Version 4.

     See Also:
       http://docs.aws.amazon.com/general/latest/gr/signing_aws_api_requests.html
       https://github.com/mmcgrana/ring/blob/master/SPEC"}
  (:import
   [java.net URI URLEncoder]
   [java.nio ByteBuffer]
   [java.security MessageDigest]
   [java.text SimpleDateFormat]
   [java.util Date TimeZone]
   [javax.crypto Mac]
   [javax.crypto.spec SecretKeySpec]
   [org.apache.commons.codec.binary Hex])
  (:require
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(def HASH_ALGORITHM "SHA-256")
(def AWS4_ALGORITHM "AWS4-HMAC-SHA256")
(def HMAC_ALGORITHM "HmacSHA256")

(def ^:private ^ThreadLocal x-amz-date-format
  ;; SimpleDateFormat is not thread-safe, so we use a ThreadLocal proxy for access.
  ;; http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4228335
  (proxy [ThreadLocal] []
    (initialValue []
      (doto (SimpleDateFormat. "yyyyMMdd'T'HHmmss'Z'")
        (.setTimeZone (TimeZone/getTimeZone "GMT"))))))

(defn- x-amz-date
  ([]
   (x-amz-date (Date.)))
  ([inst]
   (let [^SimpleDateFormat fmt (.get x-amz-date-format)]
     (.format fmt inst))))

(defn- parse-x-amz-date
  [s]
  (let [^SimpleDateFormat fmt (.get x-amz-date-format)]
    (.parse fmt s)))

(def ^:private ^ThreadLocal x-amz-date-only-format
  (proxy [ThreadLocal] []
    (initialValue []
      (doto (SimpleDateFormat. "yyyyMMdd")
        (.setTimeZone (TimeZone/getTimeZone "GMT"))))))

(defn- format-x-amz-date-only
  [inst]
  (let [^SimpleDateFormat fmt (.get x-amz-date-only-format)]
    (.format fmt inst)))

(defn- parse-x-amz-date-only
  [s]
  (let [^SimpleDateFormat fmt (.get x-amz-date-only-format)]
    (.parse fmt s)))

(defn- hex-encode
  [^bytes arr]
  (String. (Hex/encodeHex arr true)))

(defn sha-256
  ([data]
     (let [digest (MessageDigest/getInstance HASH_ALGORITHM)]
       (.update digest (.duplicate ^ByteBuffer data))
       (.digest digest)))
  ([data length]
     (let [digest (MessageDigest/getInstance HASH_ALGORITHM)]
       (.update digest data 0 length)
       (.digest digest))))

(defn- hmac-sha-256
  [^String data ^bytes key]
  (let [mac (Mac/getInstance HMAC_ALGORITHM)]
    (.init mac (SecretKeySpec. key HMAC_ALGORITHM))
    (.doFinal mac (.getBytes data "UTF8"))))

;; TODO: can we be sure body buffer and content-length match
(defn- x-amz-content-sha
  [{:keys [body]}]
  (hex-encode (sha-256 body)))

(defn- calc-x-amz-headers
  [req]
  {"x-amz-date" (x-amz-date)
   "x-amz-content-sha256" (x-amz-content-sha req)})

(def SIGNED_HEADERS
  #{"content-type" "host" "x-amz-content-sha256"
    "x-amz-date" "x-amz-target"})

(defn- signed-headers-str
  [signed-headers]
  (str/join ";" (sort (map str/lower-case signed-headers))))

(defn- uri-encode
  "Escape (%XX) special characters in the string `s`.
  Letters, digits, and the characters `_-~.` are never encoded.
  The optional string `safe` specifies extra characters to not encode."
  [^String s & [safe]]
  (when s
    (let [safe-chars (->> [\_ \- \~ \.]
                          (concat (set safe))
                          (map byte)
                          set)
          builder (StringBuilder.)]
      (doseq [b (.getBytes s "UTF-8")]
        (.append builder
                 (if (or (<= (byte \A) b (byte \Z))
                         (<= (byte \a) b (byte \z))
                         (<= (byte \0) b (byte \9))
                         (contains? safe-chars b))
                   (char b)
                   (format "%%%02X" b))))
      (.toString builder))))

(defn- canonical-query-str
  [query-str]
  (letfn [(encode-kv [kv]
            (vec (map #(uri-encode %) (str/split kv #"="))))
          (kv-str [[k v]]
            (str k "=" v))]
    (str
      (when (not (str/blank? query-str))
        (str/join "&"
          (->> (str/split query-str #"&")
            (map encode-kv)
            (into (sorted-map))
            (map kv-str)))))))

(defn- canonical-http-method
  [m]
  (str/upper-case (name m)))

(defn- canonical-uri
  [uri]
  (let [norm-path (-> uri (URI.) .normalize .getPath)
        enc-path  (str/join "/" (map #(uri-encode %) (str/split norm-path #"/")))]
    (if-not (.isEmpty enc-path)
      enc-path
      "/")))

(defn canonical-header-value
  [v]
  (str/replace
    (str/join " " (re-seq #"[^\s\"\']+|\"[^\"]*\"|\'[^\']*\'" v))
    #"\s+" " "))

(defn canonical-headers-map
  "Given a Ring request header map canonicalize it. Optional signed headers
   collection may be supplied to filter out headers not involved signing.
   Returns a sorted map."
  ([headers]
    (canonical-headers-map headers nil))
  ([headers signed-headers]
   {:pre [(or (nil? signed-headers) (set? signed-headers))]}
   (letfn [(step-kv [init k v]
             (let [k' (str/lower-case k)]
               (cond-> init
                 (or (nil? signed-headers)
                     (contains? signed-headers k'))
                 (assoc k' (canonical-header-value v)))))]
     (reduce-kv step-kv (sorted-map) headers))))

(defn canonical-headers-str
  [canonical-headers]
  (letfn [(step-kv [^StringBuilder init k v]
            (.append init k)
            (.append init ":")
            (.append init v)
            (.append init "\n"))]
    (str (reduce-kv step-kv (StringBuilder.) canonical-headers))))

(defn- canonical-request-str
  "Given a Ring request, the canonical headers sorted map, and the signed
   headers string - return the canonical request string. May optional pass a
   collection specifying the signed headers."
  [{:keys [uri query-string request-method body headers content-length] :as req}
   signed-headers]
  (let [x-amz-content-sha256 (get-in req [:headers "x-amz-content-sha256"])]
    (assert x-amz-content-sha256)
    (str (canonical-http-method request-method) "\n"
         (canonical-uri uri) "\n"
         (canonical-query-str query-string) "\n"
         (canonical-headers-str (canonical-headers-map headers signed-headers)) "\n"
         (signed-headers-str signed-headers) "\n"
         x-amz-content-sha256)))

(defn- canonical-request-hash
  ^String [req signed-headers]
  (let [crs (canonical-request-str req signed-headers)
        bytes (.getBytes ^String crs)]
    (hex-encode (sha-256 bytes (alength bytes)))))

(defn- credential-scope
  "Given an auth-info map and a Rign request, compute the credential scope
   string."
  [service region x-amz-date]
  (str/join "/"
    [(-> x-amz-date parse-x-amz-date format-x-amz-date-only)
     region service "aws4_request"]))

(defn- string-to-sign
  [x-amz-date credential-scope canonical-request-hash]
  (str AWS4_ALGORITHM "\n"
       x-amz-date "\n" credential-scope "\n"
       canonical-request-hash))

(defn- escape-access-key-id
  "Escapes access-key-id into a string usable as a faux AWS access
key id for HMAC signing algorithm"
  [access-key-id]
  (str/replace access-key-id "/" "\\"))

(defn- unescape-access-key-id
  "Reverses escape-access-key-id"
  [escaped-id]
  (str/replace escaped-id "\\" "/"))

(defn- format-signature
  [access-key-id credential-scope signed-headers signature]
  (format "%s Credential=%s/%s, SignedHeaders=%s, Signature=%s"
          AWS4_ALGORITHM
          (escape-access-key-id access-key-id)
          credential-scope
          signed-headers
          signature))

(defn- auth-headers
  [req {:keys [access-key-id secret service region]}]
  (let [crh (canonical-request-hash req SIGNED_HEADERS)
        x-amz-date (get-in req [:headers "x-amz-date"])
        _ (assert x-amz-date)
        cs (credential-scope service region x-amz-date)
        ss (string-to-sign x-amz-date cs crh)
        date-to-sign (-> x-amz-date parse-x-amz-date format-x-amz-date-only)
        derived-key  (->> (.getBytes (str "AWS4" secret) "UTF-8")
                          (hmac-sha-256 date-to-sign)
                          (hmac-sha-256 region)
                          (hmac-sha-256 service)
                          (hmac-sha-256 "aws4_request"))
        signature (-> ss (hmac-sha-256 derived-key) hex-encode)
        auth-header (format-signature access-key-id 
                          cs
                          (signed-headers-str SIGNED_HEADERS)
                          signature)]
    {"authorization" auth-header}))

(defn- repair-header
  "Repairs req's http headers according to the supplied policy.

   Returns request.

|      policy |                                                    description |
|-------------+----------------------------------------------------------------|
| :if-missing | Set `to` to `from` if `from` exists and `to` is missing.       |
| :overwrite  | Set `to` to `from` if `from` exists regardless of `to`'s value.|
| ifn?        | Pass `req`, `from`, and `to` to `policy`, return new request.  |
"
  [req from to policy]
  (let [headers (:headers req)]
    (if (keyword? policy)
      (case policy
        :if-missing
        (if (and (not (contains? headers to))
                 (contains? headers from))
          (update req :headers assoc to (get headers from))
          req)

        :overwrite
        (if (contains? headers from)
          (update req :headers assoc to (get headers from))
          req))
      (policy req from to))))

(defn- repair-headers
  "Repair `req`'s headers with the supplied set of `repairs`."
  [req repairs]
  (letfn [(step-kv [req [from to] v]
            (repair-header req from to v))]
    (reduce-kv step-kv req repairs)))

(def header-aliases
  "Alias the `authorization` to `x-cognitect-auth` and `host` to
  `x-cognitect-host` headers in order to smuggle it through API-Gateway and ALB,
   which would otherwise mangle them."
  {["authorization" "x-cognitect-auth"] :if-missing
   ["host" "x-cognitect-host"]          :overwrite})

(def header-repairs
  "Headers to repair with (possibly smuggled through API Gateway and ALB) values."
  {["x-cognitect-auth" "authorization"] :if-missing
   ["x-cognitect-host" "host"]          :overwrite

   ["x-cognitect-apig-auth" "authorization"]
   (fn [req from to]
     (let [headers (get req :headers {})]
       (if (and
            (nil? (get headers "x-cognitect-auth"))
            (get headers "x-cognitect-apig-auth"))
         (update req :headers assoc to (get headers from))
         req)))

   ["x-cognitect-apig-host" "host"]
   (fn [req from to]
     (let [headers (get req :headers {})]
       (if (and
            (nil? (get headers "x-cognitect-host"))
            (get headers "x-cognitect-apig-host"))
         (update req :headers assoc to (get headers from))
         req)))})

(defn sign
  "Returns signed request or anomaly."
  [req {:keys [access-key-id secret service region] :as sign-params}]
  (if (and (string? access-key-id)
           (string? secret)
           (string? service)
           (string? region))
    (let [xreq (update req :headers merge (calc-x-amz-headers req))
          ah (auth-headers xreq sign-params)]
      (-> (update xreq :headers merge ah)
          (repair-headers header-aliases)))
    {:cognitect.anomalies/category :cognitect.anomalies/incorrect}))

(defn parse-authorization-header
  "Legacy compat, deprecated. Prefer parse-authorization-headers."
  [auth-header]
  (when auth-header
    (let [pattern #"^AWS4-HMAC-SHA256 Credential=(.*)/(.*)/(.*)/(.*)/aws4_request, SignedHeaders=(.*), Signature=(.*)"
          [_ access-key-id request-date region service signed-headers signature]
          (first (re-seq pattern auth-header))]
      (when (and access-key-id request-date region service signed-headers signature)
        {:access-key-id (unescape-access-key-id access-key-id)
         :request-date   request-date
         :service        service
         :region         region
         :signed-headers (set (str/split signed-headers #";"))
         :signature      signature}))))

(defn parse-authorization-headers
  "Given a request, parse its authorization headers.
The return value will include :bucket :path, :request-date, :service, :region
:signed-headers, :signature."
  [req]
  (parse-authorization-header (get-in (repair-headers req header-repairs) [:headers "authorization"])))

(defn can-verify?
  "Returns true of sign-params can be used to verify request."
  [req sign-params]
  (= (select-keys (parse-authorization-headers req) [:access-key-id :service :region])
     (select-keys sign-params [:access-key-id :service :region])))

(defn verify-failure
  "Returns a keyword indicating the kind of problem verifying,
or nil if verification succeeded."
  [req sign-params]
  (let [req (repair-headers req header-repairs)
        x-amz (calc-x-amz-headers req)]
    (cond
     (not (can-verify? req sign-params))
     :params-mismatch

     (not= (get-in req [:headers "x-amz-content-sha256"])
           (get x-amz "x-amz-content-sha256"))
     :sha-mismatch
     
     (not= (select-keys (:headers req) ["authorization"])
        (auth-headers req sign-params))
     :signature-mismatch)))

(def HEADERS_ADDED_BY_SIGNATURE
  #{"x-amz-content-sha256"
    "x-amz-date"
     ;; "x-amz-target" -- optional
    "authorization"})

(defn signed?
  "Is request signed?"
  [request]
  (let [request (repair-headers request header-repairs)]
    (every? #(string? (get (:headers request) %))  HEADERS_ADDED_BY_SIGNATURE)))

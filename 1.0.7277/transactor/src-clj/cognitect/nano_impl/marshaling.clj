;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

(ns cognitect.nano-impl.marshaling
  (:require
    [clojure.data.json :as json]
    [clojure.edn :as edn]
    [cognitect.anomalies :as anom]
    [cognitect.transit :as transit]
    [clojure.string :as string])
  (:import
   [cognitect.nano_impl.io ByteBufferInputStream BytesOutputStream]
   [java.net URLEncoder URLDecoder]
   [java.nio ByteBuffer]))

(set! *warn-on-reflection* true)

(defn- ^Throwable root-cause
  [^Throwable x]
  (when x
    (let [cause (.getCause x)]
      (if cause
        (recur cause)
        x))))

(defn- non-empty
  [x]
  (when-not (empty? x)
    x))

(defn- best-exception-message
  "Best message for exception, preferring root's then own."
  [^Throwable e]
  (let [cause (root-cause e)]
    (or
      (non-empty (.getMessage cause))
      (non-empty (.getMessage e))
      (str (-> e class .getName) " with empty message"))))

(defn ^ByteBuffer str->bbuf [^String s]
  (ByteBuffer/wrap (.getBytes s)))

(defn bbuf->string
  "Creates a string from java.nio.ByteBuffer object.
   The encoding is fxied to UTF-8."
  [^ByteBuffer bbuf]
  (let [bytes (byte-array (.remaining bbuf))]
    (.get (.duplicate bbuf) bytes)
    (String. bytes "UTF-8")))

(defn bbuf->is
  "Converts given java.nio.ByteBuffer object to InputStream."
  [^ByteBuffer bbuf]
  (ByteBufferInputStream. bbuf))

(defn ^ByteBuffer bytestream->buf
  "Return a readable buf over the current internal state of a
   BytesOutputStream."
  [^BytesOutputStream stream]
  (ByteBuffer/wrap (.internalBuffer stream) 0 (.length stream)))

(defn transit-decode
  "Using given transit type, decodes buf then returns Clojure data.
   type - :json or :msgpack.
   buf - java.nio.ByteBuffer object
   opts - options for http://cognitect.github.io/transit-clj/#cognitect.transit/reader"
  [type buf opts]
  (let [in (bbuf->is  buf)
        reader (transit/reader in type opts)]
    (transit/read reader)))

(def ENCODER_BUFFER_SIZE 4096)

(defn transit-encode
  "Encode data with transit, returning a ByteBuffer
   type - :json or :msgpack.
   data - Clojure data
   opts - options for http://cognitect.github.io/transit-clj/#cognitect.transit/writer"
  [type data opts]
  (let [out (BytesOutputStream. ENCODER_BUFFER_SIZE)
        writer (transit/writer out type opts)]
    (transit/write writer data)
    (bytestream->buf out)))

(defn transit-msgpack-decode
  "Decodes given java.nio.ByteBuffer object when it is encoded by transit msgpack."
  ([^ByteBuffer bbuf]
    (transit-msgpack-decode bbuf {}))
  ([^ByteBuffer bbuf opts]
   (transit-decode :msgpack bbuf opts)))

(defn transit-json-decode
  "Decodes given java.nio.ByteBuffer object when it is encoded by transit json."
  ([^ByteBuffer bbuf]
   (transit-json-decode bbuf {}))
  ([^ByteBuffer bbuf opts]
   (transit-decode :json bbuf opts)))

(defn edn-decode
  "Decodes given java.nio.ByteBuffer object when it is encoded by EDN."
  ([^ByteBuffer bbuf]
    (edn-decode bbuf {}))
  ([^ByteBuffer bbuf opts]
   (edn/read-string (or opts {}) (bbuf->string bbuf))))

(defn transit-msgpack-encode
  "Encodes given Clojure data to transit msgpack format.
   Returns ByteBuffer
   Currently, this function assumes the data is less than 4096 bytes."
  ([data]
    (transit-msgpack-encode data {}))
  ([data opts]
   (transit-encode :msgpack data opts)))

(defn transit-json-encode
  "Encodes given Clojure data to transit json format.
   Returns ByteBuffer
   Currently, this function assumes the data is less than 4096 bytes."
  ([data]
    (transit-json-encode data {}))
  ([data opts]
   (transit-encode :json data opts)))

(defn edn-encode
  "Encodes given Clojure data to Edn format.
   Returns ByteBuffer"
  ([data]
     (-> data pr-str str->bbuf))
  ([data _]
     (-> data pr-str str->bbuf)))

(defn json-encode
  "Encodes given Clojure data to Json format. Returns byte array.
   This function is used when input is given by query string."
  [data _]
  (-> data ^String json/write-str str->bbuf))

(defn json-decode
  [data opts]
  (-> data bbuf->string json/read-str))

(def mime-decoders
  {"application/transit+msgpack" transit-msgpack-decode
   "application/transit+json" transit-json-decode
   "application/edn" edn-decode
   "application/json" json-decode})

(def mime-encoders
  {"application/transit+msgpack" transit-msgpack-encode
   "application/transit+json" transit-json-encode
   "application/edn" edn-encode
   "application/json" json-encode})

(defn header-string->mime-type
  "Extracts only mime-type portion from a given string."
  [header-string]
  (when-let [patterns (re-find (re-pattern "([a-z]+\\/[a-z\\+\\-]+);*") header-string)]
    (.toLowerCase ^String (last patterns))))

(defn request-mime-type
  "Return request's mime-type string or nil."
  [request]
  (when-let [ct (get-in request [:headers "content-type"])]
    (header-string->mime-type ct)))

(defn response-mime-type
  "Given normalized headers, return response's mime-type string or nil."
  [{:keys [headers] :as request}]
  (header-string->mime-type
    (or
      (get headers "accept")
      (get headers "content-type")
      "application/edn")))

(defn error-mime-type
  [request]
  (let [mime (response-mime-type request)]
    (if (get mime-encoders mime)
      mime
      "application/edn")))

(defn can-marshal-response?
  "Is server capable of marshaling a response for this request?"
  [request]
  (boolean (get mime-encoders (response-mime-type request))))

(defn decode-body
  "Decode body.

Returns decoded body on success
Returns nil if decoder not available
Throws if decoder throws

See nano-impl/create for description of marshaling map."
  [body mime marshaling]
  (let [decode (get mime-decoders mime)
        opts   (get-in marshaling [mime :read-opts])]
    (when decode
      (let [decoded (decode body opts)]
        (when (map? decoded)
          decoded)))))

(defn encode-body
  "Encode body

Returns encoded body on success
Returns nil if encoder not available
Throws if encoder throws

See nano-impl/create for description of marshaling map."
  [body mime marshaling]
  (let [encode (get mime-encoders mime)
        opts   (get-in marshaling [mime :write-opts])]
    (encode body opts)))

(defn unmarshal
  "Returns either a marshalled ring request or nil if the request is bad. If no
  body is present the request will be returned unmodified."
  ([request marshaling]
    (unmarshal request marshaling nil))
  ([{:keys [headers body] :as request} marshaling casters]
   (if body
     (when-let [decoded (try (decode-body body (request-mime-type request) marshaling)
                             (catch Throwable t
                               (when casters
                                 ((:alert casters) {:msg "Could not unmarshal request" :ex t})
                                 nil)))]
       (assoc request :body decoded))
     request)))

(defn marshal
  "Marshals response

response     ring response with map for :body
mime         desired mime type string
marshaling   from marshaling key in nano-impl/create map

Returns a response that is either

1. unchanged if content-type was already set
2. with body marshaled to bbuf
   and content type set per mime
3. 500 error if marshaling failed"
  ([response mime marshaling]
   (marshal response mime marshaling nil))
  ([{:keys [headers body] :as response} mime marshaling casters]
   (if (get headers "content-type")
     response
     (let [encoded-or-anom (try
                             (assert (map? body))
                             (encode-body body mime marshaling)
                             (catch Throwable t
                               (when casters
                                 ((:alert casters) {:msg "Could not marshal response" :ex t}))
                               {::anom/category ::anom/fault
                                ::anom/message (str "Could not marshal response: "
                                                    (best-exception-message t))}))]
       (if (::anom/category encoded-or-anom)
         {:status 500
          :headers {"content-type" "application/edn"}
          :body (binding [*print-namespace-maps* false]
                  (edn-encode encoded-or-anom))}
         (-> response
             (assoc :body encoded-or-anom)
             (assoc-in [:headers "content-type"] mime)))))))


(defn decode-query-part
  "Decodes one key or value of URL-encoded UTF-8 characters in a URL
  query string."
  [^String string]
  (URLDecoder/decode string "UTF-8"))

(defn- add!
  "Like 'assoc!' but creates a vector of values if the key already
  exists in the map. Ignores nil values."
  [m k v]
  (assoc! m k
          (if-let [p (get m k)]
            (if (vector? p) (conj p v) [p v])
            v)))

(defn parse-query-string
  "Parses URL query string (not including the leading '?') into a map.
  options are key-value pairs, valid options are:

     :key-fn    Function to call on parameter keys (after URL
                decoding), returns key for the map, default converts
                to a keyword.

     :value-fn  Function to call on the key (after passing through
                key-fn) and parameter value (after URL decoding),
                returns value for the map, default does nothing."
  [^String string & options]
  (let [{:keys [key-fn value-fn]
         :or {key-fn keyword
              value-fn (fn [_ v] v)}} options]
    (let [end (count string)]
      (loop [i 0
             m (transient {})
             key nil
             b (StringBuilder.)]
        (if (= end i)
          (persistent! (add! m key (value-fn key (decode-query-part (str b)))))
          (let [c (.charAt string i)]
            (cond
             (and (= \= c) (not key)) ; unescaped = is allowed in values
             (recur (inc i)
                    m
                    (key-fn (decode-query-part (str b)))
                    (StringBuilder.))
             (= \& c)
             (recur (inc i)
                    (add! m key (value-fn key (decode-query-part (str b))))
                    nil
                    (StringBuilder.))
             :else
             (recur (inc i)
                    m
                    key
                    (.append b c)))))))))


(defn kw-key
  "Converts a kw to a string, taking care of leading :"
  [^String s]
  (keyword
   (if (= \: (.charAt s 0))
     (subs s 1)
     s)))

(defn edn-val
  "Reads string value as EDN"
  [_ ^String s]
  (edn/read-string s))

(defn read-query-string
  "Given a request, if it contains a query-string, attempts to parse it into a
  map of kw -> EDN values. On success, returns request with parsed map as value
  of :body key, otherwise nil. If no query-string entry is present the original
  request is returned unmodified."
  [{:keys [query-string] :as request}]
  (if (and query-string (not (string/blank? query-string)))
    (when-let [decoded (try
                         (parse-query-string query-string
                           :key-fn kw-key :value-fn edn-val)
                         (catch Throwable t
                           (try
                             (parse-query-string query-string
                               :key-fn kw-key :value-fn (fn [_ x] x))
                             (catch Throwable t nil))))]
      (assoc request :body decoded))
    request))
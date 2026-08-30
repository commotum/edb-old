(do
  (clojure.core/in-ns 'datomic.simple-kv)
  (clojure.core/with-loading-context
    (do
      (clojure.core/require ['clojure.edn :as 'edn])
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.simple-kv 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.simple-kv))
    (clojure.core/with-loading-context
      (do
        (clojure.core/require ['clojure.edn :as 'edn])
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      KV
      (put [_ key val] "returns :ok or nil")
      (get [_ key] "returns ByteBuffer val or nil")
      (delete [_ key] "returns :ok"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.simple-kv" "KV")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'KV :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'put
                                        {:arglists (clojure.core/list ['_ 'key 'val])}),
                                      :arglists (clojure.core/list ['_ 'key 'val]),
                                      :doc "returns :ok or nil"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.simple-kv" "KV"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.simple-kv" "put")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*)))
    (let [protocol_signature__7423 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'get {:arglists (clojure.core/list ['_ 'key])}),
                                      :arglists (clojure.core/list ['_ 'key]),
                                      :doc "returns ByteBuffer val or nil"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.simple-kv" "KV"))
          protocol_method_name__7424 (with-meta
                                       (:name protocol_signature__7423)
                                       protocol_signature__7423)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.simple-kv" "get")
        (assoc protocol_signature__7423 :name protocol_method_name__7424 :ns *ns*)))
    (let [protocol_signature__7425 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete
                                        {:arglists (clojure.core/list ['_ 'key])}),
                                      :arglists (clojure.core/list ['_ 'key]),
                                      :doc "returns :ok"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.simple-kv" "KV"))
          protocol_method_name__7426 (with-meta
                                       (:name protocol_signature__7425)
                                       protocol_signature__7425)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.simple-kv" "delete")
        (assoc protocol_signature__7425 :name protocol_method_name__7426 :ns *ns*))))
  (def map-magic 568780356367818079)
  (reset-meta! #'map-magic (assoc {:const true, :column (int 1)} :name 'map-magic :ns *ns*))
  (def pack
   (fn pack
     ([m v]
       (let [mbytes (.getBytes (pr-str m) "UTF-8")]
         (.flip
           (.put
             (.put
               (.putInt
                 (.putLong
                   (ByteBuffer/allocate
                     (int (+ (+ 12 (count mbytes)) (.remaining ^java.nio.Buffer v))))
                   568780356367818079)
                 (int (count mbytes)))
               ^bytes mbytes)
             ^java.nio.ByteBuffer v))))))
  (reset-meta!
    #'pack
    (assoc
      {:arglists (clojure.core/list ['m (.withMeta 'v {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'pack
      :ns
      *ns*))
  (def unpack
   (fn unpack
     ([k v]
       (let [vmap (if (and
                        (> (.remaining ^java.nio.Buffer v) 8)
                        (= 568780356367818079 (long (.getLong ^java.nio.ByteBuffer v (int 0)))))
                    (do
                      (.getLong ^java.nio.ByteBuffer v)
                      (let [mlen (.getInt ^java.nio.ByteBuffer v)
                            bytes (byte-array (java.lang.Integer/valueOf (int mlen)))]
                        (.get ^java.nio.ByteBuffer v ^bytes bytes)
                        (assoc
                          (edn/read-string (java.lang.String. ^bytes bytes "UTF-8"))
                          :v
                          (.slice ^java.nio.ByteBuffer v))))
                    {:v v})]
         (merge {:id k} vmap)))))
  (reset-meta!
    #'unpack
    (assoc
      {:arglists (clojure.core/list ['k (.withMeta 'v {:tag 'ByteBuffer})]), :column (int 1)}
      :name
      'unpack
      :ns
      *ns*))
  (defn get-with-retry
    ([skv k]
      (or
        (get skv k)
        (do
          (java.lang.Thread/sleep 10)
          (or
            nil
            (get skv k)
            (do
              (java.lang.Thread/sleep 40)
              (or
                nil
                (get skv k)
                (do
                  (java.lang.Thread/sleep 160)
                  (or
                    nil
                    (get skv k)
                    (do (java.lang.Thread/sleep 480) (or nil (get skv k))))))))))))
  (reset-meta!
    #'get-with-retry
    (assoc
      {:arglists (clojure.core/list ['skv 'k]), :column (int 1)}
      :name
      'get-with-retry
      :ns
      *ns*)))
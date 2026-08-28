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
  (defonce KV {})
  (defprotocol KV (put [_ key val]) (get [_ key]) (delete [_ key]))
  (def map-magic 568780356367818079)
  (reset-meta! #'map-magic (assoc {:const true, :column 1} :name 'map-magic :ns *ns*))
  (defn pack
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
            ^java.nio.ByteBuffer v)))))
  (defn unpack
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
        (merge {:id k} vmap))))
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
                    (do (java.lang.Thread/sleep 480) (or nil (get skv k)))))))))))))
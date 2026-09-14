(do
  (clojure.core/in-ns 'datomic.core2.val-store.spi)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.string :as 'str] ['cognitect.anomalies :as 'anom])
      (clojure.core/import 'java.nio.ByteBuffer)))
  (when-not (.equals 'datomic.core2.val-store.spi 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.val-store.spi))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.string :as 'str] ['cognitect.anomalies :as 'anom])
        (clojure.core/import 'java.nio.ByteBuffer))))
  (set! *warn-on-reflection* true)
  (defonce Put {})
  (defprotocol Put (-put [_ k v opts]))
  (defonce Get {})
  (defprotocol Get (-get [_ k opts]))
  (defonce Delete {})
  (defprotocol Delete (-delete [_ k opts]))
  (defn no-val-error
    ([k v]
      (when-not (and (:val v) (.hasRemaining (:val v)))
        {:cognitect.anomalies/category :cognitect.anomalies/fault,
         :cognitect.anomalies/message "No value specified",
         :datomic.core2.val-store.spi/key k})))
  (defn partition-key
    ([s]
      (.toLowerCase (.substring ^java.lang.String s (int (- (.length ^java.lang.String s) 3))))))
  (defn splice-partition-key
    ([k pk]
      (if (str/includes? k "/") (str/replace k #"(/[^/]*$)" (str "/" pk "$1")) (str pk "/" k))))
  (defn val-op-succeeded?
    ([store_api_result] (or (:result store_api_result) (:val store_api_result)))))
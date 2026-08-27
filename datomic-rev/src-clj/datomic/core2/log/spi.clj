(do
  (clojure.core/in-ns 'datomic.core2.log.spi)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.core.async :as 'a] ['cognitect.anomalies :as 'anom])))
  (when-not (.equals 'datomic.core2.log.spi 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.log.spi))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.core.async :as 'a] ['cognitect.anomalies :as 'anom]))))
  (defonce Append {})
  (defprotocol Append (-append [_ header body]))
  (defonce Delete {})
  (defprotocol Delete (-delete [_ t]))
  (defonce Scan {})
  (defprotocol Scan (-scan [_ opts]))
  (defonce Item {})
  (defprotocol Item (-item-header [_ item]) (-item-body [_ item]))
  (defn result ([v] (let [ch (a/promise-chan)] (if v (a/put! ch v) (a/close! ch)) ch)))
  (defn normalize-scan-opts
    ([p__20980]
      (let [map__20981 p__20980
            map__20981 (if (seq? map__20981)
                         (if (next map__20981)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20981))
                           (if (seq map__20981) (first map__20981) {}))
                         map__20981)
            opts map__20981
            direction (get map__20981 :direction)
            t (get map__20981 :t)
            ch (get map__20981 :ch)
            limit (get map__20981 :limit)]
        (when-not limit
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'limit)))))
        (cond->
          opts
          (nil? t)
          (assoc :t (if (= direction :backward) (long java.lang.Long/MAX_VALUE) 0))
          (nil? direction)
          (assoc :direction :forward)
          (nil? ch)
          (assoc :ch (a/chan 1000)))))))
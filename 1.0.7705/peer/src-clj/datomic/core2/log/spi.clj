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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Append (-append [_ header body] "SPI for datomic.core2.log/append."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.log.spi" "Append")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Append :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-append
                                        {:arglists (clojure.core/list ['_ 'header 'body])}),
                                      :arglists (clojure.core/list ['_ 'header 'body]),
                                      :doc "SPI for datomic.core2.log/append."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.log.spi" "Append"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.log.spi" "-append")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol Delete (-delete [_ t] "SPI for datomic.core2.log/delete."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.log.spi" "Delete")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'Delete :ns *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta '-delete {:arglists (clojure.core/list ['_ 't])}),
                                      :arglists (clojure.core/list ['_ 't]),
                                      :doc "SPI for datomic.core2.log/delete."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.log.spi" "Delete"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.log.spi" "-delete")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*))))
  (let [protocol_metadata__7469 {:column (int 1)}]
    (defprotocol Scan (-scan [_ opts] "SPI for datomc.core2.log/scan. Return value ignored."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.log.spi" "Scan")
      (assoc (assoc protocol_metadata__7469 :doc nil) :name 'Scan :ns *ns*))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-scan
                                        {:arglists (clojure.core/list ['_ 'opts])}),
                                      :arglists (clojure.core/list ['_ 'opts]),
                                      :doc "SPI for datomc.core2.log/scan. Return value ignored."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.log.spi" "Scan"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.log.spi" "-scan")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
  (let [protocol_metadata__7472 {:column (int 1)}]
    (defprotocol
      Item
      (-item-header [_ item] "SPI for datomic.core2.log/item-header.")
      (-item-body [_ item] "SPI for datomic.core2.log/item-body."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.log.spi" "Item")
      (assoc (assoc protocol_metadata__7472 :doc nil) :name 'Item :ns *ns*))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-item-header
                                        {:arglists (clojure.core/list ['_ 'item])}),
                                      :arglists (clojure.core/list ['_ 'item]),
                                      :doc "SPI for datomic.core2.log/item-header."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.log.spi" "Item"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.log.spi" "-item-header")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-item-body
                                        {:arglists (clojure.core/list ['_ 'item])}),
                                      :arglists (clojure.core/list ['_ 'item]),
                                      :doc "SPI for datomic.core2.log/item-body."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.log.spi" "Item"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.log.spi" "-item-body")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*))))
  (defn result ([v] (let [ch (a/promise-chan)] (if v (a/put! ch v) (a/close! ch)) ch)))
  (reset-meta!
    #'result
    (assoc {:arglists (clojure.core/list ['v]), :column (int 1)} :name 'result :ns *ns*))
  (defn normalize-scan-opts
    ([p__21837]
      (let [map__21838 p__21837
            map__21838 (if (seq? map__21838)
                         (if (next map__21838)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21838))
                           (if (seq map__21838) (first map__21838) {}))
                         map__21838)
            opts map__21838
            direction (get map__21838 :direction)
            t (get map__21838 :t)
            ch (get map__21838 :ch)
            limit (get map__21838 :limit)]
        (when-not limit
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'limit)))))
        (cond->
          opts
          (nil? t)
          (assoc :t (if (= direction :backward) (long java.lang.Long/MAX_VALUE) 0))
          (nil? direction)
          (assoc :direction :forward)
          (nil? ch)
          (assoc :ch (a/chan 1000))))))
  (reset-meta!
    #'normalize-scan-opts
    (assoc
      {:arglists (clojure.core/list [{:keys ['direction 't 'ch 'limit], :as 'opts}]),
       :column (int 1)}
      :name
      'normalize-scan-opts
      :ns
      *ns*)))
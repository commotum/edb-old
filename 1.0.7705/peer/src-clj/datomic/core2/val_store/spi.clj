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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Put (-put [_ k v opts] "SPI for datomic.core2.val-store/put."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.spi" "Put")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Put :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-put
                                        {:arglists (clojure.core/list ['_ 'k 'v 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'v 'opts]),
                                      :doc "SPI for datomic.core2.val-store/put."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.spi" "Put"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.spi" "-put")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol Get (-get [_ k opts] "SPI for datomic.core2.val-store/get."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.spi" "Get")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'Get :ns *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-get
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc "SPI for datomic.core2.val-store/get."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.spi" "Get"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.spi" "-get")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*))))
  (let [protocol_metadata__7469 {:column (int 1)}]
    (defprotocol Delete (-delete [_ k opts] "SPI for datomic.core2.val-store/delete."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.spi" "Delete")
      (assoc (assoc protocol_metadata__7469 :doc nil) :name 'Delete :ns *ns*))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-delete
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc "SPI for datomic.core2.val-store/delete."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.spi" "Delete"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.spi" "-delete")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
  (defn no-val-error
    ([k v]
      (when-not (and (:val v) (.hasRemaining (:val v)))
        {:cognitect.anomalies/category :cognitect.anomalies/fault,
         :cognitect.anomalies/message "No value specified",
         :datomic.core2.val-store.spi/key k})))
  (reset-meta!
    #'no-val-error
    (assoc {:arglists (clojure.core/list ['k 'v]), :column (int 1)} :name 'no-val-error :ns *ns*))
  (defn partition-key
    ([s]
      (.toLowerCase (.substring ^java.lang.String s (int (- (.length ^java.lang.String s) 3))))))
  (reset-meta!
    #'partition-key
    (assoc
      {:arglists
       (clojure.core/list (.withMeta [(.withMeta 's {:tag 'String})] {:tag 'java.lang.String})),
       :column (int 1)}
      :name
      'partition-key
      :ns
      *ns*))
  (defn splice-partition-key
    ([k pk]
      (if (str/includes? k "/") (str/replace k #"(/[^/]*$)" (str "/" pk "$1")) (str pk "/" k))))
  (reset-meta!
    #'splice-partition-key
    (assoc
      {:arglists (clojure.core/list ['k 'pk]), :column (int 1)}
      :name
      'splice-partition-key
      :ns
      *ns*))
  (defn val-op-succeeded?
    ([store_api_result] (or (:result store_api_result) (:val store_api_result))))
  (reset-meta!
    #'val-op-succeeded?
    (assoc
      {:arglists (clojure.core/list ['store-api-result]), :column (int 1)}
      :name
      'val-op-succeeded?
      :ns
      *ns*)))
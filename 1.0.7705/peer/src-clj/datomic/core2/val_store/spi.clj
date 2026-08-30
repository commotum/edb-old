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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol Put (-put [_ k v opts] "SPI for datomic.core2.val-store/put."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.spi" "Put")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'Put :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-put
                                        {:arglists (clojure.core/list ['_ 'k 'v 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'v 'opts]),
                                      :doc "SPI for datomic.core2.val-store/put."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.spi" "Put"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.spi" "-put")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol Get (-get [_ k opts] "SPI for datomic.core2.val-store/get."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.spi" "Get")
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'Get :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-get
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc "SPI for datomic.core2.val-store/get."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.spi" "Get"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.spi" "-get")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*))))
  (let [protocol_metadata__7437 {:column (int 1)}]
    (defprotocol Delete (-delete [_ k opts] "SPI for datomic.core2.val-store/delete."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.val-store.spi" "Delete")
      (assoc (assoc protocol_metadata__7437 :doc nil) :name 'Delete :ns *ns*))
    (let [protocol_signature__7438 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-delete
                                        {:arglists (clojure.core/list ['_ 'k 'opts])}),
                                      :arglists (clojure.core/list ['_ 'k 'opts]),
                                      :doc "SPI for datomic.core2.val-store/delete."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.core2.val-store.spi" "Delete"))
          protocol_method_name__7439 (with-meta
                                       (:name protocol_signature__7438)
                                       protocol_signature__7438)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.val-store.spi" "-delete")
        (assoc protocol_signature__7438 :name protocol_method_name__7439 :ns *ns*))))
  (defn no-val-error
    ([k v]
      (when-not (and (:val v) (.hasRemaining (:val v)))
        {:cognitect.anomalies/category :cognitect.anomalies/fault,
         :cognitect.anomalies/message "No value specified",
         :datomic.core2.val-store.spi/key k})))
  (reset-meta!
    #'no-val-error
    (assoc {:arglists (clojure.core/list ['k 'v]), :column (int 1)} :name 'no-val-error :ns *ns*))
  (def partition-key
   (fn partition_key
     ([s]
       (.toLowerCase (.substring ^java.lang.String s (int (- (.length ^java.lang.String s) 3)))))))
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
  (def val-op-succeeded?
   (fn val_op_succeeded_QMARK_
     ([store_api_result] (or (:result store_api_result) (:val store_api_result)))))
  (reset-meta!
    #'val-op-succeeded?
    (assoc
      {:arglists (clojure.core/list ['store-api-result]), :column (int 1)}
      :name
      'val-op-succeeded?
      :ns
      *ns*)))
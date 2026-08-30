(do
  (clojure.core/in-ns 'datomic.kv-store)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get]))
  (when-not (.equals 'datomic.kv-store 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-store))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get])))
  (.setDynamic (clojure.lang.RT/var "datomic.kv-store" "*retry*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.kv-store" "*retry*") true)
    {:dynamic true, :column (int 1)})
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      KVStore
      (put
        [_ val-map]
        "(put {:id key :v buf :ensure check-map :other-keys ...}) -> :ok or nil\n          optional :ensure {:akey aval ...}\n          special treatment of {:id nil} == exists false")
      (get [_ key consistent?] "returns {:id key :v buf :other keys} or nil")
      (delete [_ key consistent?] "returns :ok")
      (close [_] "Closes resources opened for KVStore"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.kv-store" "KVStore")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'KVStore :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'put
                                        {:arglists (clojure.core/list ['_ 'val-map])}),
                                      :arglists (clojure.core/list ['_ 'val-map]),
                                      :doc
                                      "(put {:id key :v buf :ensure check-map :other-keys ...}) -> :ok or nil\n          optional :ensure {:akey aval ...}\n          special treatment of {:id nil} == exists false"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "put")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*)))
    (let [protocol_signature__7423 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get
                                        {:arglists (clojure.core/list ['_ 'key 'consistent?])}),
                                      :arglists (clojure.core/list ['_ 'key 'consistent?]),
                                      :doc "returns {:id key :v buf :other keys} or nil"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7424 (with-meta
                                       (:name protocol_signature__7423)
                                       protocol_signature__7423)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "get")
        (assoc protocol_signature__7423 :name protocol_method_name__7424 :ns *ns*)))
    (let [protocol_signature__7425 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete
                                        {:arglists (clojure.core/list ['_ 'key 'consistent?])}),
                                      :arglists (clojure.core/list ['_ 'key 'consistent?]),
                                      :doc "returns :ok"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7426 (with-meta
                                       (:name protocol_signature__7425)
                                       protocol_signature__7425)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "delete")
        (assoc protocol_signature__7425 :name protocol_method_name__7426 :ns *ns*)))
    (let [protocol_signature__7427 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'close {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Closes resources opened for KVStore"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7428 (with-meta
                                       (:name protocol_signature__7427)
                                       protocol_signature__7427)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "close")
        (assoc protocol_signature__7427 :name protocol_method_name__7428 :ns *ns*))))
  (let [protocol_metadata__7429 {:column (int 1)}]
    (defprotocol Retryable (retryable? [_]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.kv-store" "Retryable")
      (assoc (assoc protocol_metadata__7429 :doc nil) :name 'Retryable :ns *ns*))
    (let [protocol_signature__7430 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'retryable? {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "Retryable"))
          protocol_method_name__7431 (with-meta
                                       (:name protocol_signature__7430)
                                       protocol_signature__7430)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "retryable?")
        (assoc protocol_signature__7430 :name protocol_method_name__7431 :ns *ns*))))
  (extend java.lang.Throwable Retryable {:retryable? (fn fn__16620 ([_] true))})
  (extend java.lang.InterruptedException Retryable {:retryable? (fn fn__16622 ([_] false))}))
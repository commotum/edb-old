(do
  (clojure.core/in-ns 'datomic.kv-store)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-store)
    {:doc "SPI for datomic.kv-cluster/KVCluster's store access."})
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get]))
  (when-not (.equals 'datomic.kv-store 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-store))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get])))
  (.setDynamic (clojure.lang.RT/var "datomic.kv-store" "*retry*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.kv-store" "*retry*") true)
    {:dynamic true, :column (int 1)})
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      KVStore
      (put
        [_ val-map]
        "Write val-map entry into store. Returns :ok if succeeded, nil if failed.\n\n       Supply val-map with either a val or ref signature.\n       Maintain that signature for all future puts involving the same :id.\n\n       val signature is:\n       {:id key :v ByteBuffer\n        :other-keys string-or-num-values ...}\n       Cannot contain :ensure.\n       May or may not overwrite existing entry.\n\n       ref signature is:\n       {:id key\n        :ensure {:akey aval ...} (or {:id nil})\n        :other-keys string-or-num-values ...}\n       Cannot contain key :v.\n\n       :ensure asserts a precondition atomically against an existing entry\n       and is not stored.\n       :ensure {k v ...} asserts that every key k has corresponding value v.\n       Only guarantees assertions against key :rev.\n       :ensure {:id nil} asserts the entry does not exist.")
      (get
        [_ key consistent?]
        "Return entry stored by a previous put whose :id is key,\n       or nil if not stored.\n       Entry may include additional keys+values not in previous put.\n       consistent? must be true for ref entries, false for vals.")
      (delete
        [_ key consistent?]
        "Deletes entry with {:id key} and returns :ok.\n          consistent? must be true for ref entries, false for vals.")
      (close [_] "Closes resources opened for KVStore"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.kv-store" "KVStore")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'KVStore :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'put
                                        {:arglists (clojure.core/list ['_ 'val-map])}),
                                      :arglists (clojure.core/list ['_ 'val-map]),
                                      :doc
                                      "Write val-map entry into store. Returns :ok if succeeded, nil if failed.\n\n       Supply val-map with either a val or ref signature.\n       Maintain that signature for all future puts involving the same :id.\n\n       val signature is:\n       {:id key :v ByteBuffer\n        :other-keys string-or-num-values ...}\n       Cannot contain :ensure.\n       May or may not overwrite existing entry.\n\n       ref signature is:\n       {:id key\n        :ensure {:akey aval ...} (or {:id nil})\n        :other-keys string-or-num-values ...}\n       Cannot contain key :v.\n\n       :ensure asserts a precondition atomically against an existing entry\n       and is not stored.\n       :ensure {k v ...} asserts that every key k has corresponding value v.\n       Only guarantees assertions against key :rev.\n       :ensure {:id nil} asserts the entry does not exist."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "put")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*)))
    (let [protocol_signature__7434 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get
                                        {:arglists (clojure.core/list ['_ 'key 'consistent?])}),
                                      :arglists (clojure.core/list ['_ 'key 'consistent?]),
                                      :doc
                                      "Return entry stored by a previous put whose :id is key,\n       or nil if not stored.\n       Entry may include additional keys+values not in previous put.\n       consistent? must be true for ref entries, false for vals."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7435 (with-meta
                                       (:name protocol_signature__7434)
                                       protocol_signature__7434)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "get")
        (assoc protocol_signature__7434 :name protocol_method_name__7435 :ns *ns*)))
    (let [protocol_signature__7436 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete
                                        {:arglists (clojure.core/list ['_ 'key 'consistent?])}),
                                      :arglists (clojure.core/list ['_ 'key 'consistent?]),
                                      :doc
                                      "Deletes entry with {:id key} and returns :ok.\n          consistent? must be true for ref entries, false for vals."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7437 (with-meta
                                       (:name protocol_signature__7436)
                                       protocol_signature__7436)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "delete")
        (assoc protocol_signature__7436 :name protocol_method_name__7437 :ns *ns*)))
    (let [protocol_signature__7438 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'close {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Closes resources opened for KVStore"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7439 (with-meta
                                       (:name protocol_signature__7438)
                                       protocol_signature__7438)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "close")
        (assoc protocol_signature__7438 :name protocol_method_name__7439 :ns *ns*))))
  (let [protocol_metadata__7440 {:column (int 1)}]
    (defprotocol Retryable (retryable? [_]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.kv-store" "Retryable")
      (assoc (assoc protocol_metadata__7440 :doc nil) :name 'Retryable :ns *ns*))
    (let [protocol_signature__7441 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'retryable? {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "Retryable"))
          protocol_method_name__7442 (with-meta
                                       (:name protocol_signature__7441)
                                       protocol_signature__7441)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "retryable?")
        (assoc protocol_signature__7441 :name protocol_method_name__7442 :ns *ns*))))
  (extend java.lang.Throwable Retryable {:retryable? (fn fn__10492 ([_] true))})
  (extend java.lang.InterruptedException Retryable {:retryable? (fn fn__10494 ([_] false))}))
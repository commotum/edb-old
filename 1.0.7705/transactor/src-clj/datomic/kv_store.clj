;; ATOMIC-NOTE [observed] This provider protocol is below KVCluster's object/ref
;; semantics. put/get/delete know keys, byte buffers and revision preconditions,
;; not datoms, tree roots or transaction receipts. [documented] Storage Services
;; describes SQL as a provisioned KV table, not a server-side transaction engine.
;; [inferred] Keeping policy above this seam permits reuse across storage services;
;; Atomic keeps that seam without making other providers a product objective.
;; Baseline cd7192e63d883a4a34aa7de4d5bcd17e6edb692d; original forms retained.
(do
  (clojure.core/in-ns 'datomic.kv-store)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.kv-store)
    {:doc
     "Storage-provider interface beneath KVCluster. Immutable values use ByteBuffer payloads and permit non-consistent reads; revisioned refs use consistent reads and atomic :ensure preconditions. Implementations classify failures for bounded retry."})
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get]))
  (when-not (.equals 'datomic.kv-store 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-store))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core :exclude ['get])))
  (.setDynamic (clojure.lang.RT/var "datomic.kv-store" "*retry*") true)
  (.setMeta
    (.setDynamic (clojure.lang.RT/var "datomic.kv-store" "*retry*") true)
    {:dynamic true, :column (int 1)})
  (let [protocol_metadata__7463 {:column (int 1)}]
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
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'KVStore :ns *ns*))
    (let [protocol_signature__7464 (assoc
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
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "put")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
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
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "get")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
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
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "delete")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*)))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'close {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Closes resources opened for KVStore"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "KVStore"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "close")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*))))
  ;; ATOMIC-NOTE [observed] Retryable classifies failures; kv-cluster/retry-fn owns
  ;; bounded retry/backoff. Interruption is not retried. Conditional put's nil
  ;; result is not an exception: higher layers resolve revision conflict or a
  ;; previously successful write, rather than blindly repeating domain work.
  (let [protocol_metadata__7472 {:column (int 1)}]
    (defprotocol
      Retryable
      "Classifies storage failures for the bounded retry policy."
      (retryable? [_] "Returns true when repeating the storage operation may succeed."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.kv-store" "Retryable")
      (assoc
        (assoc protocol_metadata__7472 :doc "Classifies storage failures for the bounded retry policy.")
        :name
        'Retryable
        :ns
        *ns*))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'retryable? {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Returns true when repeating the storage operation may succeed."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.kv-store" "Retryable"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.kv-store" "retryable?")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*))))
  (extend java.lang.Throwable Retryable {:retryable? (fn fn__10492 ([_] true))})
  (extend java.lang.InterruptedException Retryable {:retryable? (fn fn__10494 ([_] false))}))

(do
  (let [protocol_metadata__1 {:doc "Recovered oracle documentation."}]
    (defprotocol KVStore
      "Recovered oracle documentation."
      (put [_ val-map])
      (get [_ key consistent?]))
    (reset-meta!
      #'KVStore
      (assoc
        (assoc protocol_metadata__1 :doc "Recovered oracle documentation.")
        :name 'KVStore :ns *ns*))
    (let [protocol_signature__1
          (assoc {:name 'put :arglists '([_ val-map])} :protocol #'KVStore)
          protocol_method_name__1
          (with-meta (:name protocol_signature__1) protocol_signature__1)]
      (reset-meta! #'put
        (assoc protocol_signature__1 :name protocol_method_name__1 :ns *ns*)))
    (let [protocol_signature__2
          (assoc {:name 'get :arglists '([_ key consistent?])} :protocol #'KVStore)
          protocol_method_name__2
          (with-meta (:name protocol_signature__2) protocol_signature__2)]
      (reset-meta! #'get
        (assoc protocol_signature__2 :name protocol_method_name__2 :ns *ns*))))
  (extend java.lang.Throwable
    KVStore
    {:put (fn put-body ([_ val-map] [:MUTATED val-map]))
     :get (fn get-body ([_ key consistent?] [key consistent?]))}))

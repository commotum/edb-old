(do
  (clojure.core/in-ns 'datomic.valcache.puts-pool)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.valcache.puts-pool 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.valcache.puts-pool))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      PutsPool
      (submit
        [_ k data f]
        "Submits fn f to put key k. Associates data with put of k, see get-queued-put. f should return trueish if put was successful.\nReturns future if submitted, else nil.\n\ndata map should include \n\n:source      keyword tag for source data type, e.g. :bbuf\n:v           source data value.\n\nSee also get-from-queued-put.")
      (get-queued-put [_ k] "Returns a map with :data, :fut if put for k is in queue."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.valcache.puts-pool" "PutsPool")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'PutsPool :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'submit
                                        {:arglists (clojure.core/list ['_ 'k 'data 'f])}),
                                      :arglists (clojure.core/list ['_ 'k 'data 'f]),
                                      :doc
                                      "Submits fn f to put key k. Associates data with put of k, see get-queued-put. f should return trueish if put was successful.\nReturns future if submitted, else nil.\n\ndata map should include \n\n:source      keyword tag for source data type, e.g. :bbuf\n:v           source data value.\n\nSee also get-from-queued-put."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.valcache.puts-pool" "PutsPool"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.valcache.puts-pool" "submit")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*)))
    (let [protocol_signature__7423 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-queued-put
                                        {:arglists (clojure.core/list ['_ 'k])}),
                                      :arglists (clojure.core/list ['_ 'k]),
                                      :doc
                                      "Returns a map with :data, :fut if put for k is in queue."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.valcache.puts-pool" "PutsPool"))
          protocol_method_name__7424 (with-meta
                                       (:name protocol_signature__7423)
                                       protocol_signature__7423)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.valcache.puts-pool" "get-queued-put")
        (assoc protocol_signature__7423 :name protocol_method_name__7424 :ns *ns*))))
  (.setMeta (clojure.lang.RT/var "datomic.valcache.puts-pool" "get-from-put") {:column (int 1)})
  (let [v__5792__auto__ #'get-from-put]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta
        (clojure.lang.RT/var "datomic.valcache.puts-pool" "get-from-put")
        {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.valcache.puts-pool" "get-from-put")
        (clojure.lang.MultiFn. "get-from-put" :source :default #'clojure.core/global-hierarchy))
      #'get-from-put))
  (defmethod get-from-put :default fn__20705 ([_] nil))
  (def get-from-queued-put
   (fn get_from_queued_put
     ([puts_pool k] (some-> (get-queued-put puts_pool k) (:data) (get-from-put)))))
  (reset-meta!
    #'get-from-queued-put
    (assoc
      {:arglists (clojure.core/list ['puts-pool 'k]), :column (int 1)}
      :name
      'get-from-queued-put
      :ns
      *ns*)))
(do
  (clojure.core/in-ns 'datomic.cluster)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common :refer (clojure.core/list 'bounded-deref)]
        ['datomic.config :as 'config]
        ['datomic.monitor :as 'monitor]
        ['datomic.promise :as 'promise]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.lang.AutoCloseable)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'java.util.concurrent.TimeoutException)
      (clojure.core/import 'java.util.concurrent.TimeUnit)))
  (when-not (.equals 'datomic.cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common :refer (clojure.core/list 'bounded-deref)]
          ['datomic.config :as 'config]
          ['datomic.monitor :as 'monitor]
          ['datomic.promise :as 'promise]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.lang.AutoCloseable)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'java.util.concurrent.TimeoutException)
        (clojure.core/import 'java.util.concurrent.TimeUnit))))
  (set! *warn-on-reflection* true)
  (defn uuid->val-key ([uuid] (str uuid)))
  (reset-meta!
    #'uuid->val-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uuid {:tag 'UUID})]), :column (int 1)}
      :name
      'uuid->val-key
      :ns
      *ns*))
  (defn val-key->uuid ([val_key] (UUID/fromString ^java.lang.String val_key)))
  (reset-meta!
    #'val-key->uuid
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'val-key {:tag 'String})]), :column (int 1)}
      :name
      'val-key->uuid
      :ns
      *ns*))
  (defn uuid->pod-key ([uuid] (str "pod-" uuid)))
  (reset-meta!
    #'uuid->pod-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uuid {:tag 'UUID})]), :column (int 1)}
      :name
      'uuid->pod-key
      :ns
      *ns*))
  (defn pod-key->uuid ([pod_key] (UUID/fromString (subs pod_key 4))))
  (reset-meta!
    #'pod-key->uuid
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'pod-key {:tag 'String})]), :column (int 1)}
      :name
      'pod-key->uuid
      :ns
      *ns*))
  (defn new-val-key ([] (uuid->val-key (common/rand-uuid))))
  (reset-meta!
    #'new-val-key
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'new-val-key :ns *ns*))
  (defn new-pod-key ([] (str "pod-" (common/rand-uuid))))
  (reset-meta!
    #'new-pod-key
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'new-pod-key :ns *ns*))
  (defn new-ref-key ([name] (str "ref-" name)))
  (reset-meta!
    #'new-ref-key
    (assoc {:arglists (clojure.core/list ['name]), :column (int 1)} :name 'new-ref-key :ns *ns*))
  (defn new-pod-key ([] (str "pod-" (common/rand-uuid))))
  (reset-meta!
    #'new-pod-key
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'new-pod-key :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.cluster" "path")
    {:tag java.lang.String, :arglists (clojure.core/list ['args]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cluster" "path")
    (fn path
      ([args]
        (let [map__9157 args
              map__9157 (if (seq? map__9157)
                          (if (next map__9157)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__9157))
                            (if (seq map__9157) (first map__9157) {}))
                          map__9157)
              tenant (get map__9157 :tenant)
              db (get map__9157 :db)
              partition (get map__9157 :partition)
              key (get map__9157 :key)]
          (when-not key (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'key)))))
          (str
            (when tenant (format "%s/" tenant))
            (when db (format "%s/" db))
            (when partition (format "%03X/" partition))
            (format "%s" key))))))
  (def PRIORITY_HIGH 2)
  (reset-meta!
    #'PRIORITY_HIGH
    (assoc {:const true, :column (int 1)} :name 'PRIORITY_HIGH :ns *ns*))
  (def PRIORITY_MID 3)
  (reset-meta! #'PRIORITY_MID (assoc {:const true, :column (int 1)} :name 'PRIORITY_MID :ns *ns*))
  (def PRIORITY_LOW 4)
  (reset-meta! #'PRIORITY_LOW (assoc {:const true, :column (int 1)} :name 'PRIORITY_LOW :ns *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Dbid (dbId [c] "Returns the db id of a cluster, or nil if a system cluster."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "Dbid")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Dbid :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name (.withMeta 'dbId {:arglists (clojure.core/list ['c])}),
                                      :arglists (clojure.core/list ['c]),
                                      :doc
                                      "Returns the db id of a cluster, or nil if a system cluster."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "Dbid"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "dbId")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol
      ClusteredStore
      "An interface to a clustered store. All fns might throw an exception on deref if no quorum is available."
      (get-pod-meta
        [cs ^String pod-key]
        "Returns reference to pod-meta, without walking entire linked list a la get-pod.")
      (delete [cs ^String key] "Soft delete. Returns a reference to :ok")
      (get-val
        [cs ^String val-key]
        "Gets the value at a key. Returns a reference to {:buf buf} or nil if not found")
      (get-ref
        [cs ^String ref-key]
        "Returns a reference to {:rev nnn, :key k} or nil if not found")
      (update-pod*
        [cs ^String pod-key rev ^String etag ^ByteBuffer buf metamap]
        "With nil etag, creates or resets pod to be supplied value. When\n   etag is non-nil, appends a non-nil buf to the current value of the\n   pod (a nil buf just 'touches' the pod, incrementing rev and leaving\n   the value intact), iff etag matches.  In all cases rev must be one\n   higher than existing rev. You must obtain rev and etag from a prior\n   get/update, and increment rev. Keys in metamap must be\n   namespaced. Returns a reference to {:rev nnn, :etag xxx, :buf buf}\n   or {:failed :conflict}.")
      (create-val
        [cs ^String val-key ^ByteBuffer buf]
        [cs priority ^String val-key ^ByteBuffer buf]
        "Creates a new value in the store. Returns a reference to :created or nil")
      (delete-reference
        [cs ^String key]
        "Delete a reference (pod or ref).  Returns a reference to :ok")
      (set-ref
        [cs ^String ref-key rev ^String vkey]
        "Makes vkey the new value of ref, iff rev is higher than existing\n   rev. You must have obtained rev from a prior read and incremented\n   it. Returns a reference to :ok or :conflict. set-ref with a rev of 0 can create a ref.")
      (get-pod
        [cs ^String pod-key]
        "Gets the value in a pod. Returns a reference to {:rev nnn, :etag xxx, :buf buf} and any metadata keys.\n   or nil if not found."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "ClusteredStore")
      (assoc
        (assoc
          protocol_metadata__7466
          :doc
          "An interface to a clustered store. All fns might throw an exception on deref if no quorum is available.")
        :name
        'ClusteredStore
        :ns
        *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-pod-meta
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'pod-key {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs (.withMeta 'pod-key {:tag 'String})]),
                                      :doc
                                      "Returns reference to pod-meta, without walking entire linked list a la get-pod."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-pod-meta")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*)))
    (let [protocol_signature__7469 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'key {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list ['cs (.withMeta 'key {:tag 'String})]),
                                      :doc "Soft delete. Returns a reference to :ok"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7470 (with-meta
                                       (:name protocol_signature__7469)
                                       protocol_signature__7469)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "delete")
        (assoc protocol_signature__7469 :name protocol_method_name__7470 :ns *ns*)))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-val
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'val-key {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs (.withMeta 'val-key {:tag 'String})]),
                                      :doc
                                      "Gets the value at a key. Returns a reference to {:buf buf} or nil if not found"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-val")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-ref
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'ref-key {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs (.withMeta 'ref-key {:tag 'String})]),
                                      :doc
                                      "Returns a reference to {:rev nnn, :key k} or nil if not found"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-ref")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'update-pod*
                                        {:arglists
                                         (clojure.core/list
                                           ['cs
                                            (.withMeta 'pod-key {:tag 'String})
                                            'rev
                                            (.withMeta 'etag {:tag 'String})
                                            (.withMeta 'buf {:tag 'ByteBuffer})
                                            'metamap])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs
                                         (.withMeta 'pod-key {:tag 'String})
                                         'rev
                                         (.withMeta 'etag {:tag 'String})
                                         (.withMeta 'buf {:tag 'ByteBuffer})
                                         'metamap]),
                                      :doc
                                      "With nil etag, creates or resets pod to be supplied value. When\n   etag is non-nil, appends a non-nil buf to the current value of the\n   pod (a nil buf just 'touches' the pod, incrementing rev and leaving\n   the value intact), iff etag matches.  In all cases rev must be one\n   higher than existing rev. You must obtain rev and etag from a prior\n   get/update, and increment rev. Keys in metamap must be\n   namespaced. Returns a reference to {:rev nnn, :etag xxx, :buf buf}\n   or {:failed :conflict}."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "update-pod*")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*)))
    (let [protocol_signature__7477 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'create-val
                                        {:arglists
                                         (clojure.core/list
                                           ['cs
                                            (.withMeta 'val-key {:tag 'String})
                                            (.withMeta 'buf {:tag 'ByteBuffer})]
                                           ['cs
                                            'priority
                                            (.withMeta 'val-key {:tag 'String})
                                            (.withMeta 'buf {:tag 'ByteBuffer})])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs
                                         (.withMeta 'val-key {:tag 'String})
                                         (.withMeta 'buf {:tag 'ByteBuffer})]
                                        ['cs
                                         'priority
                                         (.withMeta 'val-key {:tag 'String})
                                         (.withMeta 'buf {:tag 'ByteBuffer})]),
                                      :doc
                                      "Creates a new value in the store. Returns a reference to :created or nil"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7478 (with-meta
                                       (:name protocol_signature__7477)
                                       protocol_signature__7477)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "create-val")
        (assoc protocol_signature__7477 :name protocol_method_name__7478 :ns *ns*)))
    (let [protocol_signature__7479 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete-reference
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'key {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list ['cs (.withMeta 'key {:tag 'String})]),
                                      :doc
                                      "Delete a reference (pod or ref).  Returns a reference to :ok"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7480 (with-meta
                                       (:name protocol_signature__7479)
                                       protocol_signature__7479)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "delete-reference")
        (assoc protocol_signature__7479 :name protocol_method_name__7480 :ns *ns*)))
    (let [protocol_signature__7481 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'set-ref
                                        {:arglists
                                         (clojure.core/list
                                           ['cs
                                            (.withMeta 'ref-key {:tag 'String})
                                            'rev
                                            (.withMeta 'vkey {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs
                                         (.withMeta 'ref-key {:tag 'String})
                                         'rev
                                         (.withMeta 'vkey {:tag 'String})]),
                                      :doc
                                      "Makes vkey the new value of ref, iff rev is higher than existing\n   rev. You must have obtained rev from a prior read and incremented\n   it. Returns a reference to :ok or :conflict. set-ref with a rev of 0 can create a ref."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7482 (with-meta
                                       (:name protocol_signature__7481)
                                       protocol_signature__7481)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "set-ref")
        (assoc protocol_signature__7481 :name protocol_method_name__7482 :ns *ns*)))
    (let [protocol_signature__7483 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-pod
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'pod-key {:tag 'String})])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs (.withMeta 'pod-key {:tag 'String})]),
                                      :doc
                                      "Gets the value in a pod. Returns a reference to {:rev nnn, :etag xxx, :buf buf} and any metadata keys.\n   or nil if not found."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "ClusteredStore"))
          protocol_method_name__7484 (with-meta
                                       (:name protocol_signature__7483)
                                       protocol_signature__7483)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-pod")
        (assoc protocol_signature__7483 :name protocol_method_name__7484 :ns *ns*))))
  (let [protocol_metadata__7485 {:column (int 1)}]
    (defprotocol
      Get2
      "Enhanced ClusteredStore/get-val that accepts opts map."
      (get-val2
        [cs ^String val-key opts]
        "Like ClusteredStore/get-val, but takes an opts map that flows to underlying implementations."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "Get2")
      (assoc
        (assoc
          protocol_metadata__7485
          :doc
          "Enhanced ClusteredStore/get-val that accepts opts map.")
        :name
        'Get2
        :ns
        *ns*))
    (let [protocol_signature__7486 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'get-val2
                                        {:arglists
                                         (clojure.core/list
                                           ['cs (.withMeta 'val-key {:tag 'String}) 'opts])}),
                                      :arglists
                                      (clojure.core/list
                                        ['cs (.withMeta 'val-key {:tag 'String}) 'opts]),
                                      :doc
                                      "Like ClusteredStore/get-val, but takes an opts map that flows to underlying implementations."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "Get2"))
          protocol_method_name__7487 (with-meta
                                       (:name protocol_signature__7486)
                                       protocol_signature__7486)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-val2")
        (assoc protocol_signature__7486 :name protocol_method_name__7487 :ns *ns*))))
  (defn update-pod
    ([cs pod_key rev etag buf metamap]
      (do
        (when-not (every? namespace (keys metamap))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str
                  (clojure.core/list 'every? 'namespace (clojure.core/list 'keys 'metamap)))))))
        (update-pod* cs pod_key rev etag buf metamap)))
    ([cs pod_key rev etag buf] (update-pod cs pod_key rev etag buf nil)))
  (reset-meta!
    #'update-pod
    (assoc
      {:arglists
       (clojure.core/list
         ['cs
          (.withMeta 'pod-key {:tag 'String})
          'rev
          (.withMeta 'etag {:tag 'String})
          (.withMeta 'buf {:tag 'ByteBuffer})]
         ['cs
          (.withMeta 'pod-key {:tag 'String})
          'rev
          (.withMeta 'etag {:tag 'String})
          (.withMeta 'buf {:tag 'ByteBuffer})
          'metamap]),
       :column (int 1)}
      :name
      'update-pod
      :ns
      *ns*))
  (defn reset-ref
    ([cluster k v]
      (let [map__9358 (deref (get-ref cluster k))
            map__9358 (if (seq? map__9358)
                        (if (next map__9358)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9358))
                          (if (seq map__9358) (first map__9358) {}))
                        map__9358)
            prev map__9358
            rev (get map__9358 :rev)
            m_9359 {:event :kv-cluster/reset-ref, :key k, :from (:key prev), :to v}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_9359 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned (set-ref cluster k (if rev (inc rev) 0) v)}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_9360 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_9361 (logger/format-as-msec (long elapsed_9360))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_9359 :msec msec_9361 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'reset-ref
    (assoc
      {:arglists (clojure.core/list ['cluster 'k 'v]), :column (int 1)}
      :name
      'reset-ref
      :ns
      *ns*))
  (defn touch-ref
    ([cluster k]
      (let [map__9369 (deref (get-ref cluster k))
            map__9369 (if (seq? map__9369)
                        (if (next map__9369)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9369))
                          (if (seq map__9369) (first map__9369) {}))
                        map__9369)
            prev map__9369
            rev (get map__9369 :rev)
            m_9370 {:event :kv-cluster/touch-ref, :from prev}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_9370 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (when (= :ok (deref (set-ref cluster k (inc rev) (:key prev))))
                                      prev)}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_9371 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_9372 (logger/format-as-msec (long elapsed_9371))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_9370 :msec msec_9372 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'touch-ref
    (assoc
      {:arglists (clojure.core/list ['cluster 'k]), :column (int 1)}
      :name
      'touch-ref
      :ns
      *ns*))
  (defn reset-pod
    ([cluster k buf metamap]
      (let [map__9380 (deref (get-pod cluster k))
            map__9380 (if (seq? map__9380)
                        (if (next map__9380)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9380))
                          (if (seq map__9380) (first map__9380) {}))
                        map__9380)
            rev (get map__9380 :rev)]
        (update-pod cluster k (if rev (inc rev) 0) nil buf metamap))))
  (reset-meta!
    #'reset-pod
    (assoc
      {:arglists (clojure.core/list ['cluster 'k 'buf 'metamap]), :column (int 1)}
      :name
      'reset-pod
      :ns
      *ns*))
  (defn touch-pod
    ([cs pod_key pod] (deref (update-pod cs pod_key (inc (:rev pod)) (:etag pod) nil))))
  (reset-meta!
    #'touch-pod
    (assoc
      {:arglists (clojure.core/list ['cs 'pod-key 'pod]), :column (int 1)}
      :name
      'touch-pod
      :ns
      *ns*))
  (defn claim-pod
    ([cs pod_key msec]
      (let [start (java.lang.System/currentTimeMillis)]
        (loop []
          (let [temp__5804__auto__ (deref (get-pod cs pod_key))]
            (when temp__5804__auto__
              (let [pod temp__5804__auto__ touched (touch-pod cs pod_key pod)]
                (cond
                  (:rev touched) (assoc touched :buf (:buf pod))
                  (<= (- (java.lang.System/currentTimeMillis) start) msec) (do (recur))))))))))
  (reset-meta!
    #'claim-pod
    (assoc
      {:arglists (clojure.core/list ['cs 'pod-key 'msec]), :column (int 1)}
      :name
      'claim-pod
      :ns
      *ns*))
  (defn clone-pod
    ([cs from_key to_key]
      (let [temp__5802__auto__ (deref (get-pod cs from_key))]
        (if temp__5802__auto__
          (let [map__9385 temp__5802__auto__
                map__9385 (if (seq? map__9385)
                            (if (next map__9385)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__9385))
                              (if (seq map__9385) (first map__9385) {}))
                            map__9385)
                rev (get map__9385 :rev)
                etag (get map__9385 :etag)
                buf (get map__9385 :buf)]
            (deref (update-pod cs to_key 0 nil buf)))
          {:failed :absent}))))
  (reset-meta!
    #'clone-pod
    (assoc
      {:arglists (clojure.core/list ['cs 'from-key 'to-key]), :column (int 1)}
      :name
      'clone-pod
      :ns
      *ns*))
  (defn clone-ref
    ([cs from_key to_key]
      (let [temp__5802__auto__ (deref (get-ref cs from_key))]
        (when temp__5802__auto__
          (let [map__9388 temp__5802__auto__
                map__9388 (if (seq? map__9388)
                            (if (next map__9388)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__9388))
                              (if (seq map__9388) (first map__9388) {}))
                            map__9388)
                rev (get map__9388 :rev)
                key (get map__9388 :key)]
            (if (= :ok (deref (set-ref cs to_key 0 key))) {:rev 0} {:failed :conflict}))))))
  (reset-meta!
    #'clone-ref
    (assoc
      {:arglists (clojure.core/list ['cs 'from-key 'to-key]), :column (int 1)}
      :name
      'clone-ref
      :ns
      *ns*))
  (defn uncached-val-lookup
    ([cs]
      (reify
        clojure.lang.ILookup
        (valAt [this k not_found] (let [ret (deref (get-val cs k))] (if ret (:buf ret) not_found)))
        (valAt [this k] (.valAt this k nil)))))
  (reset-meta!
    #'uncached-val-lookup
    (assoc
      {:arglists (clojure.core/list ['cs]), :column (int 1)}
      :name
      'uncached-val-lookup
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cluster" "pacer") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cluster" "pacer") (java.lang.Object.))
  (.setMeta (clojure.lang.RT/var "datomic.cluster" "segment-writes") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cluster" "segment-writes") (atom 0))
  (.setMeta (clojure.lang.RT/var "datomic.cluster" "segment-pacing-msec") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cluster" "segment-pacing-msec") (atom nil))
  (def BOUNDING_TIMEOUT_MSEC 300000)
  (reset-meta!
    #'BOUNDING_TIMEOUT_MSEC
    (assoc {:column (int 1)} :name 'BOUNDING_TIMEOUT_MSEC :ns *ns*))
  (defn write-vals*
    ([cs source vmap]
      (let [event (let [G__9394 source]
                    (case G__9394 :index :index/write-val :clusterfs :clusterfs/write-val))
            metric (let [G__9395 source]
                     (case G__9395 :index :IndexWriteNsec :clusterfs :FulltextWriteNsec))
            pacing (deref segment-pacing-msec)
            rets (mapv
                   (fn fn__9397
                     ([p__9396]
                       (let [vec__9398 p__9396
                             k (nth vec__9398 (int 0) nil)
                             v (nth vec__9398 (int 1) nil)]
                         (when pacing (java.lang.Thread/sleep (long ^java.lang.Number pacing)))
                         [(long (java.lang.System/nanoTime)) (create-val cs k v)])))
                   vmap)]
        (dorun
          (map
            (fn fn__9403
              ([p__9402]
                (let [vec__9404 p__9402
                      start (nth vec__9404 (int 0) nil)
                      ret (nth vec__9404 (int 1) nil)]
                  (if (= (common/bounded-deref ret BOUNDING_TIMEOUT_MSEC) :created)
                    (let [nsec (- (java.lang.System/nanoTime) start)
                          msec (logger/format-as-msec nsec)]
                      (swap! segment-writes inc)
                      (monitor/add-stat metric nsec)
                      (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug
                            ^org.slf4j.Logger logger
                            (logger/process {:event event, :msec msec})))
                        nil))
                    (do (throw (java.lang.RuntimeException. "Cluster write failed")) nil)))))
            rets)))))
  (reset-meta!
    #'write-vals*
    (assoc
      {:arglists (clojure.core/list ['cs 'source 'vmap]), :column (int 1)}
      :name
      'write-vals*
      :ns
      *ns*))
  (defn write-vals
    ([cs source vmap]
      (let [m_9409 {:event :cluster/write-vals,
                    :vcnt (java.lang.Integer/valueOf (int (count vmap)))}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_9409 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (write-vals*
                                      cs
                                      source
                                      (map
                                        (fn fn__9414
                                          ([p__9413]
                                            (let [vec__9415 p__9413
                                                  k (nth vec__9415 (int 0) nil)
                                                  v (nth vec__9415 (int 1) nil)]
                                              [(uuid->val-key k) v])))
                                        vmap))}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_9410 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_9411 (logger/format-as-msec (long elapsed_9410))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_9409 :msec msec_9411 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'write-vals
    (assoc
      {:arglists (clojure.core/list ['cs 'source 'vmap]), :column (int 1)}
      :name
      'write-vals
      :ns
      *ns*))
  (let [protocol_metadata__7488 {:column (int 1)}]
    (defprotocol
      AsyncWriter
      (finish-writer [_] "Finish writer. Returns a future that will throw if any write failed.")
      (sync-writes [_] "Returns a promise that will be filled after all pending writes complete."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "AsyncWriter")
      (assoc (assoc protocol_metadata__7488 :doc nil) :name 'AsyncWriter :ns *ns*))
    (let [protocol_signature__7489 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'finish-writer
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Finish writer. Returns a future that will throw if any write failed."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "AsyncWriter"))
          protocol_method_name__7490 (with-meta
                                       (:name protocol_signature__7489)
                                       protocol_signature__7489)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "finish-writer")
        (assoc protocol_signature__7489 :name protocol_method_name__7490 :ns *ns*)))
    (let [protocol_signature__7491 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'sync-writes
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Returns a promise that will be filled after all pending writes complete."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "AsyncWriter"))
          protocol_method_name__7492 (with-meta
                                       (:name protocol_signature__7491)
                                       protocol_signature__7491)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "sync-writes")
        (assoc protocol_signature__7491 :name protocol_method_name__7492 :ns *ns*))))
  (deftype
    QueueingWriter
    [cluster done_reason bounding_timeout_msec queue]
    datomic.cluster.AsyncWriter
    datomic.cluster.ClusteredStore
    (finish-writer
      [this]
      (do
        (when-not (.offer
                    ^java.util.concurrent.BlockingQueue queue
                    {:type :finish}
                    (long ^java.lang.Number bounding_timeout_msec)
                    TimeUnit/MILLISECONDS)
          (throw (java.util.concurrent.TimeoutException. "Timed out waiting for queue")))
        done_reason))
    (sync-writes
      [this]
      (let [prom (promise)]
        (when-not (.offer
                    ^java.util.concurrent.BlockingQueue queue
                    {:type :sync-writes, :obj prom}
                    (long ^java.lang.Number bounding_timeout_msec)
                    TimeUnit/MILLISECONDS)
          (throw (java.util.concurrent.TimeoutException. "Timed out waiting for queue")))
        prom))
    (create-val [this k v] (create-val this 3 k v))
    (create-val
      [this priority k v]
      (if (realized? done_reason)
        done_reason
        (if (.offer
              ^java.util.concurrent.BlockingQueue queue
              {:type :create-val, :obj (create-val cluster priority k v)}
              (long ^java.lang.Number bounding_timeout_msec)
              TimeUnit/MILLISECONDS)
          (promise/delivered :created)
          (do
            (throw (java.util.concurrent.TimeoutException. "Timed out waiting for queue"))
            nil)))))
  (clojure.core/import 'datomic.cluster.QueueingWriter)
  (defn ->QueueingWriter
    ([cluster done_reason bounding_timeout_msec queue]
      (datomic.cluster.QueueingWriter. cluster done_reason bounding_timeout_msec queue)))
  (reset-meta!
    #'->QueueingWriter
    (assoc
      {:arglists (clojure.core/list ['cluster 'done-reason 'bounding-timeout-msec 'queue]),
       :column (int 1)}
      :name
      '->QueueingWriter
      :ns
      *ns*))
  (defn queueing-writer
    ([cluster par bounding_timeout_msec progress]
      (let [queue (java.util.concurrent.ArrayBlockingQueue. (int ^java.lang.Number par))
            done_reason (promise/settable-future)
            writer (datomic.cluster.QueueingWriter.
                     cluster
                     done_reason
                     bounding_timeout_msec
                     queue)]
        (future-call
          (fn fn__9459
            ([]
              (try
                (loop []
                  (when-not (realized? done_reason)
                    (let [map__9460 (.take ^java.util.concurrent.ArrayBlockingQueue queue)
                          map__9460 (if (seq? map__9460)
                                      (if (next map__9460)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__9460))
                                        (if (seq map__9460) (first map__9460) {}))
                                      map__9460)
                          type (get map__9460 :type)
                          obj (get map__9460 :obj)
                          G__9461 type]
                      (case
                        G__9461
                        :sync-writes
                        (do (deliver obj :synced) (recur))
                        :create-val
                        (do
                          (^clojure.lang.IFn progress
                            (java.lang.Integer/valueOf (int (count queue))))
                          (let [create_result (deref obj)]
                            (when (= :created create_result) (recur))))
                        :finish
                        (^clojure.lang.IFn done_reason :done)))))
                (catch java.lang.Throwable t (^clojure.lang.IFn done_reason t))))))
        writer)))
  (reset-meta!
    #'queueing-writer
    (assoc
      {:arglists (clojure.core/list ['cluster 'par 'bounding-timeout-msec 'progress]),
       :column (int 1)}
      :name
      'queueing-writer
      :ns
      *ns*))
  (let [protocol_metadata__7493 {:column (int 1)}]
    (defprotocol
      RefClusterStore
      "Helper for getting a ref store from a cluster"
      (-get-ref-store [cs]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "RefClusterStore")
      (assoc
        (assoc protocol_metadata__7493 :doc "Helper for getting a ref store from a cluster")
        :name
        'RefClusterStore
        :ns
        *ns*))
    (let [protocol_signature__7494 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-get-ref-store
                                        {:arglists (clojure.core/list ['cs])}),
                                      :arglists (clojure.core/list ['cs]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "RefClusterStore"))
          protocol_method_name__7495 (with-meta
                                       (:name protocol_signature__7494)
                                       protocol_signature__7494)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "-get-ref-store")
        (assoc protocol_signature__7494 :name protocol_method_name__7495 :ns *ns*))))
  (defn get-ref-store ([cs] (-get-ref-store cs)))
  (reset-meta!
    #'get-ref-store
    (assoc {:arglists (clojure.core/list ['cs]), :column (int 1)} :name 'get-ref-store :ns *ns*))
  (defn close
    ([x]
      (when (and x (instance? java.lang.AutoCloseable x))
        (.close ^java.lang.AutoCloseable x)
        nil)))
  (reset-meta!
    #'close
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'close :ns *ns*)))
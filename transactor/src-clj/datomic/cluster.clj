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
  (def uuid->val-key (fn uuid__GT_val_key ([uuid] (str uuid))))
  (reset-meta!
    #'uuid->val-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uuid {:tag 'UUID})]), :column (int 1)}
      :name
      'uuid->val-key
      :ns
      *ns*))
  (def val-key->uuid (fn val_key__GT_uuid ([val_key] (UUID/fromString ^java.lang.String val_key))))
  (reset-meta!
    #'val-key->uuid
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'val-key {:tag 'String})]), :column (int 1)}
      :name
      'val-key->uuid
      :ns
      *ns*))
  (def uuid->pod-key (fn uuid__GT_pod_key ([uuid] (str "pod-" uuid))))
  (reset-meta!
    #'uuid->pod-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uuid {:tag 'UUID})]), :column (int 1)}
      :name
      'uuid->pod-key
      :ns
      *ns*))
  (def pod-key->uuid (fn pod_key__GT_uuid ([pod_key] (UUID/fromString (subs pod_key 4)))))
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
        (let [map__11960 args
              map__11960 (if (seq? map__11960)
                           (if (next map__11960)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__11960))
                             (if (seq map__11960) (first map__11960) {}))
                           map__11960)
              tenant (get map__11960 :tenant)
              db (get map__11960 :db)
              partition (get map__11960 :partition)
              key (get map__11960 :key)]
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
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol Dbid (dbId [c] "Returns the db id of a cluster, or nil if a system cluster."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "Dbid")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'Dbid :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name (.withMeta 'dbId {:arglists (clojure.core/list ['c])}),
                                      :arglists (clojure.core/list ['c]),
                                      :doc
                                      "Returns the db id of a cluster, or nil if a system cluster."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "Dbid"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "dbId")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
  (let [protocol_metadata__7423 {:column (int 1)}]
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
          protocol_metadata__7423
          :doc
          "An interface to a clustered store. All fns might throw an exception on deref if no quorum is available.")
        :name
        'ClusteredStore
        :ns
        *ns*))
    (let [protocol_signature__7424 (assoc
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
          protocol_method_name__7425 (with-meta
                                       (:name protocol_signature__7424)
                                       protocol_signature__7424)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-pod-meta")
        (assoc protocol_signature__7424 :name protocol_method_name__7425 :ns *ns*)))
    (let [protocol_signature__7426 (assoc
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
          protocol_method_name__7427 (with-meta
                                       (:name protocol_signature__7426)
                                       protocol_signature__7426)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "delete")
        (assoc protocol_signature__7426 :name protocol_method_name__7427 :ns *ns*)))
    (let [protocol_signature__7428 (assoc
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
          protocol_method_name__7429 (with-meta
                                       (:name protocol_signature__7428)
                                       protocol_signature__7428)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-val")
        (assoc protocol_signature__7428 :name protocol_method_name__7429 :ns *ns*)))
    (let [protocol_signature__7430 (assoc
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
          protocol_method_name__7431 (with-meta
                                       (:name protocol_signature__7430)
                                       protocol_signature__7430)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-ref")
        (assoc protocol_signature__7430 :name protocol_method_name__7431 :ns *ns*)))
    (let [protocol_signature__7432 (assoc
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
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "update-pod*")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*)))
    (let [protocol_signature__7434 (assoc
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
          protocol_method_name__7435 (with-meta
                                       (:name protocol_signature__7434)
                                       protocol_signature__7434)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "create-val")
        (assoc protocol_signature__7434 :name protocol_method_name__7435 :ns *ns*)))
    (let [protocol_signature__7436 (assoc
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
          protocol_method_name__7437 (with-meta
                                       (:name protocol_signature__7436)
                                       protocol_signature__7436)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "delete-reference")
        (assoc protocol_signature__7436 :name protocol_method_name__7437 :ns *ns*)))
    (let [protocol_signature__7438 (assoc
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
          protocol_method_name__7439 (with-meta
                                       (:name protocol_signature__7438)
                                       protocol_signature__7438)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "set-ref")
        (assoc protocol_signature__7438 :name protocol_method_name__7439 :ns *ns*)))
    (let [protocol_signature__7440 (assoc
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
          protocol_method_name__7441 (with-meta
                                       (:name protocol_signature__7440)
                                       protocol_signature__7440)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-pod")
        (assoc protocol_signature__7440 :name protocol_method_name__7441 :ns *ns*))))
  (let [protocol_metadata__7442 {:column (int 1)}]
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
          protocol_metadata__7442
          :doc
          "Enhanced ClusteredStore/get-val that accepts opts map.")
        :name
        'Get2
        :ns
        *ns*))
    (let [protocol_signature__7443 (assoc
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
          protocol_method_name__7444 (with-meta
                                       (:name protocol_signature__7443)
                                       protocol_signature__7443)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "get-val2")
        (assoc protocol_signature__7443 :name protocol_method_name__7444 :ns *ns*))))
  (def update-pod
   (fn update_pod
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
     ([cs pod_key rev etag buf] (update-pod cs pod_key rev etag buf nil))))
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
      (let [map__12161 (deref (get-ref cluster k))
            map__12161 (if (seq? map__12161)
                         (if (next map__12161)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12161))
                           (if (seq map__12161) (first map__12161) {}))
                         map__12161)
            prev map__12161
            rev (get map__12161 :rev)
            m_12162 {:event :kv-cluster/reset-ref, :key k, :from (:key prev), :to v}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12162 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned (set-ref cluster k (if rev (inc rev) 0) v)}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12163 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12164 (logger/format-as-msec (long elapsed_12163))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12162 :msec msec_12164 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
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
      (let [map__12172 (deref (get-ref cluster k))
            map__12172 (if (seq? map__12172)
                         (if (next map__12172)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12172))
                           (if (seq map__12172) (first map__12172) {}))
                         map__12172)
            prev map__12172
            rev (get map__12172 :rev)
            m_12173 {:event :kv-cluster/touch-ref, :from prev}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                (.warn
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12173 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (when (= :ok (deref (set-ref cluster k (inc rev) (:key prev))))
                                      prev)}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12174 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12175 (logger/format-as-msec (long elapsed_12174))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12173 :msec msec_12175 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
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
      (let [map__12183 (deref (get-pod cluster k))
            map__12183 (if (seq? map__12183)
                         (if (next map__12183)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12183))
                           (if (seq map__12183) (first map__12183) {}))
                         map__12183)
            rev (get map__12183 :rev)]
        (update-pod cluster k (if rev (inc rev) 0) nil buf metamap))))
  (reset-meta!
    #'reset-pod
    (assoc
      {:arglists (clojure.core/list ['cluster 'k 'buf 'metamap]), :column (int 1)}
      :name
      'reset-pod
      :ns
      *ns*))
  (def touch-pod
   (fn touch_pod
     ([cs pod_key pod] (deref (update-pod cs pod_key (inc (:rev pod)) (:etag pod) nil)))))
  (reset-meta!
    #'touch-pod
    (assoc
      {:arglists (clojure.core/list ['cs 'pod-key 'pod]), :column (int 1)}
      :name
      'touch-pod
      :ns
      *ns*))
  (def claim-pod
   (fn claim_pod
     ([cs pod_key msec]
       (let [start (java.lang.System/currentTimeMillis)]
         (loop []
           (let [temp__5804__auto__ (deref (get-pod cs pod_key))]
             (when temp__5804__auto__
               (let [pod temp__5804__auto__ touched (touch-pod cs pod_key pod)]
                 (cond
                   (:rev touched) (assoc touched :buf (:buf pod))
                   (<= (- (java.lang.System/currentTimeMillis) start) msec) (do (recur)))))))))))
  (reset-meta!
    #'claim-pod
    (assoc
      {:arglists (clojure.core/list ['cs 'pod-key 'msec]), :column (int 1)}
      :name
      'claim-pod
      :ns
      *ns*))
  (def clone-pod
   (fn clone_pod
     ([cs from_key to_key]
       (let [temp__5802__auto__ (deref (get-pod cs from_key))]
         (if temp__5802__auto__
           (let [map__12188 temp__5802__auto__
                 map__12188 (if (seq? map__12188)
                              (if (next map__12188)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__12188))
                                (if (seq map__12188) (first map__12188) {}))
                              map__12188)
                 rev (get map__12188 :rev)
                 etag (get map__12188 :etag)
                 buf (get map__12188 :buf)]
             (deref (update-pod cs to_key 0 nil buf)))
           {:failed :absent})))))
  (reset-meta!
    #'clone-pod
    (assoc
      {:arglists (clojure.core/list ['cs 'from-key 'to-key]), :column (int 1)}
      :name
      'clone-pod
      :ns
      *ns*))
  (def clone-ref
   (fn clone_ref
     ([cs from_key to_key]
       (let [temp__5802__auto__ (deref (get-ref cs from_key))]
         (when temp__5802__auto__
           (let [map__12191 temp__5802__auto__
                 map__12191 (if (seq? map__12191)
                              (if (next map__12191)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__12191))
                                (if (seq map__12191) (first map__12191) {}))
                              map__12191)
                 rev (get map__12191 :rev)
                 key (get map__12191 :key)]
             (if (= :ok (deref (set-ref cs to_key 0 key))) {:rev 0} {:failed :conflict})))))))
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
      (let [event (let [G__12197 source]
                    (case G__12197 :index :index/write-val :clusterfs :clusterfs/write-val))
            metric (let [G__12198 source]
                     (case G__12198 :index :IndexWriteNsec :clusterfs :FulltextWriteNsec))
            pacing (deref segment-pacing-msec)
            rets (mapv
                   (fn fn__12200
                     ([p__12199]
                       (let [vec__12201 p__12199
                             k (nth vec__12201 (int 0) nil)
                             v (nth vec__12201 (int 1) nil)]
                         (when pacing (java.lang.Thread/sleep (long ^java.lang.Number pacing)))
                         [(long (java.lang.System/nanoTime)) (create-val cs k v)])))
                   vmap)]
        (dorun
          (map
            (fn fn__12206
              ([p__12205]
                (let [vec__12207 p__12205
                      start (nth vec__12207 (int 0) nil)
                      ret (nth vec__12207 (int 1) nil)]
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
      (let [m_12212 {:event :cluster/write-vals,
                     :vcnt (java.lang.Integer/valueOf (int (count vmap)))}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12212 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (write-vals*
                                      cs
                                      source
                                      (map
                                        (fn fn__12217
                                          ([p__12216]
                                            (let [vec__12218 p__12216
                                                  k (nth vec__12218 (int 0) nil)
                                                  v (nth vec__12218 (int 1) nil)]
                                              [(uuid->val-key k) v])))
                                        vmap))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12213 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12214 (logger/format-as-msec (long elapsed_12213))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12212 :msec msec_12214 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.cluster")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (reset-meta!
    #'write-vals
    (assoc
      {:arglists (clojure.core/list ['cs 'source 'vmap]), :column (int 1)}
      :name
      'write-vals
      :ns
      *ns*))
  (let [protocol_metadata__7445 {:column (int 1)}]
    (defprotocol
      AsyncWriter
      (finish-writer [_] "Finish writer. Returns a future that will throw if any write failed.")
      (sync-writes [_] "Returns a promise that will be filled after all pending writes complete."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "AsyncWriter")
      (assoc (assoc protocol_metadata__7445 :doc nil) :name 'AsyncWriter :ns *ns*))
    (let [protocol_signature__7446 (assoc
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
          protocol_method_name__7447 (with-meta
                                       (:name protocol_signature__7446)
                                       protocol_signature__7446)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "finish-writer")
        (assoc protocol_signature__7446 :name protocol_method_name__7447 :ns *ns*)))
    (let [protocol_signature__7448 (assoc
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
          protocol_method_name__7449 (with-meta
                                       (:name protocol_signature__7448)
                                       protocol_signature__7448)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "sync-writes")
        (assoc protocol_signature__7448 :name protocol_method_name__7449 :ns *ns*))))
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
  (def ->QueueingWriter
   (fn __GT_QueueingWriter
     ([cluster done_reason bounding_timeout_msec queue]
       (datomic.cluster.QueueingWriter. cluster done_reason bounding_timeout_msec queue))))
  (reset-meta!
    #'->QueueingWriter
    (assoc
      {:arglists (clojure.core/list ['cluster 'done-reason 'bounding-timeout-msec 'queue]),
       :column (int 1)}
      :name
      '->QueueingWriter
      :ns
      *ns*))
  (def queueing-writer
   (fn queueing_writer
     ([cluster par bounding_timeout_msec progress]
       (let [queue (java.util.concurrent.ArrayBlockingQueue. (int ^java.lang.Number par))
             done_reason (promise/settable-future)
             writer (datomic.cluster.QueueingWriter.
                      cluster
                      done_reason
                      bounding_timeout_msec
                      queue)]
         (future-call
           (fn fn__12262
             ([]
               (try
                 (loop []
                   (when-not (realized? done_reason)
                     (let [map__12263 (.take ^java.util.concurrent.ArrayBlockingQueue queue)
                           map__12263 (if (seq? map__12263)
                                        (if (next map__12263)
                                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                            (to-array map__12263))
                                          (if (seq map__12263) (first map__12263) {}))
                                        map__12263)
                           type (get map__12263 :type)
                           obj (get map__12263 :obj)
                           G__12264 type]
                       (case
                         G__12264
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
         writer))))
  (reset-meta!
    #'queueing-writer
    (assoc
      {:arglists (clojure.core/list ['cluster 'par 'bounding-timeout-msec 'progress]),
       :column (int 1)}
      :name
      'queueing-writer
      :ns
      *ns*))
  (let [protocol_metadata__7450 {:column (int 1)}]
    (defprotocol
      RefClusterStore
      "Helper for getting a ref store from a cluster"
      (-get-ref-store [cs]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.cluster" "RefClusterStore")
      (assoc
        (assoc protocol_metadata__7450 :doc "Helper for getting a ref store from a cluster")
        :name
        'RefClusterStore
        :ns
        *ns*))
    (let [protocol_signature__7451 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-get-ref-store
                                        {:arglists (clojure.core/list ['cs])}),
                                      :arglists (clojure.core/list ['cs]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.cluster" "RefClusterStore"))
          protocol_method_name__7452 (with-meta
                                       (:name protocol_signature__7451)
                                       protocol_signature__7451)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.cluster" "-get-ref-store")
        (assoc protocol_signature__7451 :name protocol_method_name__7452 :ns *ns*))))
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
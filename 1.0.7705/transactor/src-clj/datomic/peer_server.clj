(do
  (clojure.core/in-ns 'datomic.peer-server)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :refer (clojure.core/list '<!! 'chan)]
        ['clojure.edn :as 'edn]
        ['clojure.pprint :as 'pp]
        ['clojure.spec.alpha :as 's]
        ['clojure.string :as 'str]
        ['clojure.tools.cli :as 'cli]
        ['cognitect.anomalies :as 'anom]
        ['cognitect.caster :as 'cast]
        ['cognitect.nano-impl :as 'nano-impl]
        ['datomic.api :as 'd]
        ['datomic.cache :as 'cache]
        ['datomic.cast2slf4j.peer-server :as 'cast2slf4j]
        'datomic.client.api
        'datomic.client.protocol
        ['datomic.client-server.auth :as 'auth]
        ['datomic.client-server.marshaling :as 'marshal]
        ['datomic.client-server.spi-support :as 'spi-support]
        ['datomic.client-spi :as 'client-spi]
        ['datomic.config :as 'config]
        ['datomic.coordination :as 'coord]
        ['datomic.db-io :as 'db-io]
        ['datomic.domain :as 'domain]
        ['datomic.error :as 'error]
        ['datomic.peer :as 'peer]
        'datomic.peer-client
        ['datomic.process :as 'process]
        ['datomic.slf4j :as 'logger]
        ['datomic.specs :as 'specs]
        ['datomic.uri :as 'uri])
      (clojure.core/import 'datomic.peer.LocalConnection)))
  (when-not (.equals 'datomic.peer-server 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.peer-server))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :refer (clojure.core/list '<!! 'chan)]
          ['clojure.edn :as 'edn]
          ['clojure.pprint :as 'pp]
          ['clojure.spec.alpha :as 's]
          ['clojure.string :as 'str]
          ['clojure.tools.cli :as 'cli]
          ['cognitect.anomalies :as 'anom]
          ['cognitect.caster :as 'cast]
          ['cognitect.nano-impl :as 'nano-impl]
          ['datomic.api :as 'd]
          ['datomic.cache :as 'cache]
          ['datomic.cast2slf4j.peer-server :as 'cast2slf4j]
          'datomic.client.api
          'datomic.client.protocol
          ['datomic.client-server.auth :as 'auth]
          ['datomic.client-server.marshaling :as 'marshal]
          ['datomic.client-server.spi-support :as 'spi-support]
          ['datomic.client-spi :as 'client-spi]
          ['datomic.config :as 'config]
          ['datomic.coordination :as 'coord]
          ['datomic.db-io :as 'db-io]
          ['datomic.domain :as 'domain]
          ['datomic.error :as 'error]
          ['datomic.peer :as 'peer]
          'datomic.peer-client
          ['datomic.process :as 'process]
          ['datomic.slf4j :as 'logger]
          ['datomic.specs :as 'specs]
          ['datomic.uri :as 'uri])
        (clojure.core/import 'datomic.peer.LocalConnection))))
  (def id-and-conn
   (fn id_and_conn
     ([db_uri]
       (let [map__26381 (uri/parse-db db_uri)
             map__26381 (if (seq? map__26381)
                          (if (next map__26381)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26381))
                            (if (seq map__26381) (first map__26381) {}))
                          map__26381)
             protocol (get map__26381 :protocol)
             db_name (get map__26381 :db-name)
             uri (get map__26381 :uri)]
         (when (= protocol :mem) (peer/create-local-database db_name uri))
         (let [conn (d/connect uri)] [(:id (d/db conn)) conn])))))
  (reset-meta!
    #'id-and-conn
    (assoc {:arglists (clojure.core/list ['db-uri]), :column (int 1)} :name 'id-and-conn :ns *ns*))
  (defn database-uri? ([s] (boolean (:db-name (uri/parse s)))))
  (reset-meta!
    #'database-uri?
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'database-uri? :ns *ns*))
  (defn storage-uri? ([s] (= "*" (:db-name (uri/parse s)))))
  (reset-meta!
    #'storage-uri?
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'storage-uri? :ns *ns*))
  (s/def-impl
    :datomic.peer-server/non-empty-string
    (clojure.core/list
      'clojure.spec.alpha/and
      'clojure.core/string?
      (clojure.core/list
        'fn*
        ['p1__26385#]
        (clojure.core/list
          'clojure.core/not
          (clojure.core/list 'clojure.core/empty? 'p1__26385#))))
    (s/and-spec-impl
      ['clojure.core/string?
       (clojure.core/list
         'clojure.core/fn
         ['%]
         (clojure.core/list 'clojure.core/not (clojure.core/list 'clojure.core/empty? '%)))]
      [string? (fn fn__26386 ([p1__26385#] (not (empty? p1__26385#))))]
      nil))
  (s/def-impl
    :datomic.peer-server/db-uri
    (clojure.core/list
      'clojure.spec.alpha/and
      :datomic.peer-server/non-empty-string
      'datomic.peer-server/database-uri?)
    (s/and-spec-impl
      [:datomic.peer-server/non-empty-string 'datomic.peer-server/database-uri?]
      [:datomic.peer-server/non-empty-string database-uri?]
      nil))
  (s/def-impl
    :datomic.peer-server/storage-uri
    (clojure.core/list
      'clojure.spec.alpha/and
      :datomic.peer-server/non-empty-string
      'datomic.peer-server/storage-uri?)
    (s/and-spec-impl
      [:datomic.peer-server/non-empty-string 'datomic.peer-server/storage-uri?]
      [:datomic.peer-server/non-empty-string storage-uri?]
      nil))
  (s/def-impl
    :datomic.peer-server/access-key
    :datomic.peer-server/non-empty-string
    :datomic.peer-server/non-empty-string)
  (s/def-impl
    :datomic.peer-server/secret
    :datomic.peer-server/non-empty-string
    :datomic.peer-server/non-empty-string)
  (s/def-impl
    :datomic.peer-server/db-map
    (clojure.core/list
      'clojure.spec.alpha/map-of
      :datomic.specs/db-name
      :datomic.peer-server/db-uri)
    (s/every-impl
      (clojure.core/list
        'clojure.spec.alpha/tuple
        :datomic.specs/db-name
        :datomic.peer-server/db-uri)
      (s/tuple-impl
        [:datomic.specs/db-name :datomic.peer-server/db-uri]
        [:datomic.specs/db-name :datomic.peer-server/db-uri])
      {:clojure.spec.alpha/kfn
       (fn fn__26389 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         :datomic.specs/db-name
         :datomic.peer-server/db-uri),
       :clojure.spec.alpha/cpred (fn fn__26391 ([G__26388] (map? G__26388)))}
      nil))
  (s/def-impl :datomic.peer-server/db :datomic.peer-server/db-map :datomic.peer-server/db-map)
  (s/def-impl
    :datomic.peer-server/storage
    :datomic.peer-server/storage-uri
    :datomic.peer-server/storage-uri)
  (s/def-impl
    :datomic.peer-server/auth
    (clojure.core/list
      'clojure.spec.alpha/map-of
      :datomic.peer-server/access-key
      :datomic.peer-server/secret)
    (s/every-impl
      (clojure.core/list
        'clojure.spec.alpha/tuple
        :datomic.peer-server/access-key
        :datomic.peer-server/secret)
      (s/tuple-impl
        [:datomic.peer-server/access-key :datomic.peer-server/secret]
        [:datomic.peer-server/access-key :datomic.peer-server/secret])
      {:clojure.spec.alpha/kfn
       (fn fn__26394 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         :datomic.peer-server/access-key
         :datomic.peer-server/secret),
       :clojure.spec.alpha/cpred (fn fn__26396 ([G__26393] (map? G__26393)))}
      nil))
  (s/def-impl
    :datomic.peer-server/id->conn
    (clojure.core/list 'clojure.spec.alpha/map-of 'clojure.core/string? 'clojure.core/any?)
    (s/every-impl
      (clojure.core/list 'clojure.spec.alpha/tuple 'string? 'any?)
      (s/tuple-impl ['clojure.core/string? 'clojure.core/any?] [string? any?])
      {:clojure.spec.alpha/kfn
       (fn fn__26399 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list 'clojure.spec.alpha/map-of 'clojure.core/string? 'clojure.core/any?),
       :clojure.spec.alpha/cpred (fn fn__26401 ([G__26398] (map? G__26398)))}
      nil))
  (s/def-impl
    :datomic.peer-server/host
    :datomic.peer-server/non-empty-string
    :datomic.peer-server/non-empty-string)
  (s/def-impl :datomic.peer-server/port 'clojure.core/pos-int? pos-int?)
  (s/def-impl
    :datomic.peer-server/name->id
    (clojure.core/list 'clojure.spec.alpha/map-of :datomic.specs/db-name 'clojure.core/string?)
    (s/every-impl
      (clojure.core/list 'clojure.spec.alpha/tuple :datomic.specs/db-name 'string?)
      (s/tuple-impl
        [:datomic.specs/db-name 'clojure.core/string?]
        [:datomic.specs/db-name string?])
      {:clojure.spec.alpha/kfn
       (fn fn__26404 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list 'clojure.spec.alpha/map-of :datomic.specs/db-name 'clojure.core/string?),
       :clojure.spec.alpha/cpred (fn fn__26406 ([G__26403] (map? G__26403)))}
      nil))
  (s/def-impl :datomic.peer-server/concurrency 'clojure.core/pos-int? pos-int?)
  (s/def-impl
    :datomic.peer-server/options
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req-un
      [:datomic.peer-server/host
       :datomic.peer-server/port
       (clojure.core/list 'clojure.core/or :datomic.peer-server/db :datomic.peer-server/storage)
       :datomic.peer-server/auth
       :datomic.peer-server/concurrency])
    (s/map-spec-impl
      {:req-un
       [:datomic.peer-server/host
        :datomic.peer-server/port
        (clojure.core/list 'or :datomic.peer-server/db :datomic.peer-server/storage)
        :datomic.peer-server/auth
        :datomic.peer-server/concurrency],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__26409 ([G__26408] (map? G__26408)))
        (fn fn__26411 ([G__26408] (contains? G__26408 :host)))
        (fn fn__26413 ([G__26408] (contains? G__26408 :port)))
        (fn fn__26415 ([G__26408] (or (contains? G__26408 :db) (contains? G__26408 :storage))))
        (fn fn__26418 ([G__26408] (contains? G__26408 :auth)))
        (fn fn__26420 ([G__26408] (contains? G__26408 :concurrency)))],
       :keys-pred
       (fn fn__26422
         ([G__26408]
           (and
             (map? G__26408)
             (contains? G__26408 :host)
             (contains? G__26408 :port)
             (or (contains? G__26408 :db) (contains? G__26408 :storage))
             (contains? G__26408 :auth)
             (contains? G__26408 :concurrency)))),
       :opt-keys [],
       :req-specs
       [:datomic.peer-server/host
        :datomic.peer-server/port
        :datomic.peer-server/db
        :datomic.peer-server/storage
        :datomic.peer-server/auth
        :datomic.peer-server/concurrency],
       :req nil,
       :req-keys [:host :port :db :storage :auth :concurrency],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :host))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :port))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list
            'clojure.core/or
            (clojure.core/list 'clojure.core/contains? '% :db)
            (clojure.core/list 'clojure.core/contains? '% :storage)))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :auth))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :concurrency))],
       :opt nil}))
  (s/def-impl
    :datomic.peer-server/list-db-names
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :ret
      (clojure.core/list
        'clojure.spec.alpha/coll-of
        :datomic.specs/db-name
        :kind
        'clojure.core/vector?))
    (s/fspec-impl
      nil
      nil
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/coll-of
          :datomic.specs/db-name
          :kind
          'clojure.core/vector?)
        (s/every-impl
          :datomic.specs/db-name
          :datomic.specs/db-name
          {:clojure.spec.alpha/conform-all true,
           :kind vector?,
           :clojure.spec.alpha/kind-form 'clojure.core/vector?,
           :clojure.spec.alpha/describe
           (clojure.core/list
             'clojure.spec.alpha/coll-of
             :datomic.specs/db-name
             :kind
             'clojure.core/vector?),
           :clojure.spec.alpha/cpred (fn fn__26431 ([G__26430] (vector? G__26430)))}
          nil)
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/coll-of
        :datomic.specs/db-name
        :kind
        'clojure.core/vector?)
      nil
      nil
      nil))
  (s/def-impl
    :datomic.peer-server/create-db
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :db-name :datomic.specs/db-name)
      :ret
      'clojure.core/boolean?)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :db-name :datomic.specs/db-name)
        (s/cat-impl [:db-name] [:datomic.specs/db-name] [:datomic.specs/db-name])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :db-name :datomic.specs/db-name)
      (s/spec-impl 'clojure.core/boolean? boolean? nil nil)
      'clojure.core/boolean?
      nil
      nil
      nil))
  (s/def-impl
    :datomic.peer-server/delete-db
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :db-name :datomic.specs/db-name)
      :ret
      'clojure.core/boolean?)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :db-name :datomic.specs/db-name)
        (s/cat-impl [:db-name] [:datomic.specs/db-name] [:datomic.specs/db-name])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :db-name :datomic.specs/db-name)
      (s/spec-impl 'clojure.core/boolean? boolean? nil nil)
      'clojure.core/boolean?
      nil
      nil
      nil))
  (s/def-impl
    :datomic.peer-server/administer-system
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :args 'clojure.core/map?)
      :ret
      'clojure.core/any?)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :args 'clojure.core/map?)
        (s/cat-impl [:args] [map?] ['clojure.core/map?])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :args 'clojure.core/map?)
      (s/spec-impl 'clojure.core/any? any? nil nil)
      'clojure.core/any?
      nil
      nil
      nil))
  (s/def-impl
    :datomic.peer-server/admin-op-impls
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req
      [:datomic.peer-server/id->conn
       :datomic.peer-server/name->id
       :datomic.peer-server/list-db-names]
      :opt
      [:datomic.peer-server/create-db
       :datomic.peer-server/delete-db
       :datomic.peer-server/administer-system])
    (s/map-spec-impl
      {:req-un nil,
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__26434 ([G__26433] (map? G__26433)))
        (fn fn__26436 ([G__26433] (contains? G__26433 :datomic.peer-server/id->conn)))
        (fn fn__26438 ([G__26433] (contains? G__26433 :datomic.peer-server/name->id)))
        (fn fn__26440 ([G__26433] (contains? G__26433 :datomic.peer-server/list-db-names)))],
       :keys-pred
       (fn fn__26442
         ([G__26433]
           (and
             (map? G__26433)
             (contains? G__26433 :datomic.peer-server/id->conn)
             (contains? G__26433 :datomic.peer-server/name->id)
             (contains? G__26433 :datomic.peer-server/list-db-names)))),
       :opt-keys
       [:datomic.peer-server/create-db
        :datomic.peer-server/delete-db
        :datomic.peer-server/administer-system],
       :req-specs
       [:datomic.peer-server/id->conn
        :datomic.peer-server/name->id
        :datomic.peer-server/list-db-names],
       :req
       [:datomic.peer-server/id->conn
        :datomic.peer-server/name->id
        :datomic.peer-server/list-db-names],
       :req-keys
       [:datomic.peer-server/id->conn
        :datomic.peer-server/name->id
        :datomic.peer-server/list-db-names],
       :opt-specs
       [:datomic.peer-server/create-db
        :datomic.peer-server/delete-db
        :datomic.peer-server/administer-system],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :datomic.peer-server/id->conn))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :datomic.peer-server/name->id))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :datomic.peer-server/list-db-names))],
       :opt
       [:datomic.peer-server/create-db
        :datomic.peer-server/delete-db
        :datomic.peer-server/administer-system]}))
  (s/def-impl
    'datomic.peer-server/db-admin-op-impls
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :db :datomic.peer-server/db-map)
      :ret
      :datomic.peer-server/admin-op-impls)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :db :datomic.peer-server/db-map)
        (s/cat-impl [:db] [:datomic.peer-server/db-map] [:datomic.peer-server/db-map])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :db :datomic.peer-server/db-map)
      (s/spec-impl :datomic.peer-server/admin-op-impls :datomic.peer-server/admin-op-impls nil nil)
      :datomic.peer-server/admin-op-impls
      nil
      nil
      nil))
  (def db-admin-op-impls
   (fn db_admin_op_impls
     ([db_map]
       (let [c (reduce-kv
                 (fn fn__26447
                   ([caches name db_uri]
                     (let [vec__26448 (id-and-conn db_uri)
                           id (nth vec__26448 (int 0) nil)
                           conn (nth vec__26448 (int 1) nil)]
                       (assoc-in
                         (assoc-in caches [:datomic.peer-server/id->conn id] conn)
                         [:datomic.peer-server/name->id name]
                         id))))
                 #:datomic.peer-server{:id->conn {}, :name->id {}}
                 db_map)]
         (assoc
           c
           :datomic.peer-server/list-db-names
           (constantly (vec (keys (:datomic.peer-server/name->id c)))))))))
  (reset-meta!
    #'db-admin-op-impls
    (assoc
      {:arglists (clojure.core/list ['db-map]), :column (int 1)}
      :name
      'db-admin-op-impls
      :ns
      *ns*))
  (def connect-to-storage-from-id
   (fn connect_to_storage_from_id
     ([p__26453]
       (let [map__26454 p__26453
             map__26454 (if (seq? map__26454)
                          (if (next map__26454)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26454))
                            (if (seq map__26454) (first map__26454) {}))
                          map__26454)
             resolved_conf map__26454
             db_id (get map__26454 :db-id)
             cluster (coord/create-db-cluster resolved_conf)
             olookup (domain/system-cache-olookup cluster)
             map__26455 (db-io/load-db-from-basis cluster olookup db_id nil true)
             map__26455 (if (seq? map__26455)
                          (if (next map__26455)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26455))
                            (if (seq map__26455) (first map__26455) {}))
                          map__26455)
             db (get map__26455 :db)
             log (get map__26455 :log)]
         (peer/->StorageOnlyConnection db_id cluster db log)))))
  (reset-meta!
    #'connect-to-storage-from-id
    (assoc
      {:arglists (clojure.core/list [{:keys ['db-id], :as 'resolved-conf}]), :column (int 1)}
      :name
      'connect-to-storage-from-id
      :ns
      *ns*))
  (def get-connection-from-id
   (fn get_connection_from_id
     ([system_conf db_id]
       (when db_id
         (let [db_conf (assoc system_conf :db-id db_id)
               temp__5823__auto__ (get peer/connection-cache db_conf)]
           (if temp__5823__auto__
             (let [conn temp__5823__auto__] conn)
             (locking peer/connection-lock
              (let [temp__5823__auto__ (get peer/connection-cache db_conf)]
                (if temp__5823__auto__
                  (let [conn temp__5823__auto__] conn)
                  (let [conn (peer/create-connection db_conf)]
                    (cache/put peer/connection-cache db_conf conn)
                    conn))))))))))
  (reset-meta!
    #'get-connection-from-id
    (assoc
      {:arglists (clojure.core/list ['system-conf 'db-id]), :column (int 1)}
      :name
      'get-connection-from-id
      :ns
      *ns*))
  (defn mem-dbs ([] (deref (deref #'peer/local-dbs))))
  (reset-meta!
    #'mem-dbs
    (assoc
      {:private true, :arglists (clojure.core/list []), :column (int 1)}
      :name
      'mem-dbs
      :ns
      *ns*))
  (def connect-local-database-from-id
   (fn connect_local_database_from_id
     ([db_id]
       (or
         (some (fn fn__26463 ([conn] (when (= db_id (:id (d/db conn))) conn))) (vals (mem-dbs)))
         (error/raise :db.error/db-not-found (str "Could not find " db_id))))))
  (reset-meta!
    #'connect-local-database-from-id
    (assoc
      {:arglists (clojure.core/list ['db-id]), :column (int 1)}
      :name
      'connect-local-database-from-id
      :ns
      *ns*))
  (def read-only-local-database-from-id
   (fn read_only_local_database_from_id
     ([db_id]
       (let [conn (connect-local-database-from-id db_id)
             db (deref (.-db-ref ^datomic.peer.LocalConnection conn))
             log (peer/->LocalLog db)]
         (peer/->StorageOnlyConnection
           (.-dbname ^datomic.peer.LocalConnection conn)
           nil
           db
           log)))))
  (reset-meta!
    #'read-only-local-database-from-id
    (assoc
      {:arglists (clojure.core/list ['db-id]), :column (int 1)}
      :name
      'read-only-local-database-from-id
      :ns
      *ns*))
  (def connect-id-fn
   (fn connect_id_fn
     ([system_conf]
       (let [protocol (:protocol system_conf)]
         (when (= protocol :backup)
           (throw (ex-info ":backup not supported" {:cluster-conf system_conf})))
         (cond
           (= protocol :mem) (if (:read-only system_conf)
                               (fn fn__26468
                                 ([db_id]
                                   (deref peer/initialize)
                                   (read-only-local-database-from-id db_id)))
                               (fn fn__26470
                                 ([db_id]
                                   (deref peer/initialize)
                                   (connect-local-database-from-id db_id))))
           (:read-only system_conf) (fn fn__26472
                                      ([db_id]
                                        (deref peer/initialize)
                                        (connect-to-storage-from-id
                                          (assoc system_conf :db-id db_id))))
           :else (do
                   (fn fn__26474
                     ([db_id]
                       (deref peer/initialize)
                       (get-connection-from-id system_conf db_id)))))))))
  (reset-meta!
    #'connect-id-fn
    (assoc
      {:arglists (clojure.core/list ['system-conf]), :column (int 1)}
      :name
      'connect-id-fn
      :ns
      *ns*))
  (s/def-impl
    'datomic.peer-server/storage-admin-op-impls
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :storage-uri :datomic.peer-server/storage-uri)
      :ret
      :datomic.peer-server/admin-op-impls)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :storage-uri :datomic.peer-server/storage-uri)
        (s/cat-impl
          [:storage-uri]
          [:datomic.peer-server/storage-uri]
          [:datomic.peer-server/storage-uri])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :storage-uri :datomic.peer-server/storage-uri)
      (s/spec-impl :datomic.peer-server/admin-op-impls :datomic.peer-server/admin-op-impls nil nil)
      :datomic.peer-server/admin-op-impls
      nil
      nil
      nil))
  (def storage-admin-op-impls
   (fn storage_admin_op_impls
     ([storage_uri]
       (let [system_conf (dissoc (uri/parse storage_uri) :db-name :db-id)
             db_id_>conn (connect-id-fn system_conf)
             db_name_>db_id (if (= :mem (:protocol system_conf))
                              (fn fn__26479
                                ([p1__26477#] (some-> (get (mem-dbs) p1__26477#) (d/db) (:id))))
                              (fn fn__26482
                                ([p1__26478#]
                                  (:db-id
                                    (coord/resolve-db-name
                                      (assoc system_conf :db-name p1__26478#))))))]
         #:datomic.peer-server{:id->conn (cache/fn->lookup db_id_>conn),
                               :name->id (cache/fn->lookup db_name_>db_id),
                               :list-db-names
                               (fn fn__26484 ([] (vec (peer/get-database-names storage_uri)))),
                               :create-db
                               (fn fn__26486
                                 ([db_name]
                                   (peer/create-database (uri/db-uri storage_uri db_name)))),
                               :delete-db
                               (fn fn__26488
                                 ([db_name]
                                   (peer/delete-database (uri/db-uri storage_uri db_name)))),
                               :administer-system
                               (fn fn__26491
                                 ([p__26490]
                                   (let [map__26492 p__26490
                                         map__26492 (if (seq? map__26492)
                                                      (if (next map__26492)
                                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                          (to-array map__26492))
                                                        (if (seq map__26492)
                                                          (first map__26492)
                                                          {}))
                                                      map__26492)
                                         m map__26492
                                         db_name (get map__26492 :db-name)
                                         args (if db_name
                                                (assoc
                                                  (dissoc m :db-name)
                                                  :uri
                                                  (uri/db-uri storage_uri db_name))
                                                m)]
                                     (peer/administer-system args))))}))))
  (reset-meta!
    #'storage-admin-op-impls
    (assoc
      {:arglists (clojure.core/list ['storage-uri]), :column (int 1)}
      :name
      'storage-admin-op-impls
      :ns
      *ns*))
  (def add-admin-ops
   (fn add_admin_ops
     ([nsm p__26495]
       (let [map__26496 p__26495
             map__26496 (if (seq? map__26496)
                          (if (next map__26496)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26496))
                            (if (seq map__26496) (first map__26496) {}))
                          map__26496)
             name_>id (get map__26496 :datomic.peer-server/name->id)
             list_db_names (get map__26496 :datomic.peer-server/list-db-names)
             create_db (get map__26496 :datomic.peer-server/create-db)
             delete_db (get map__26496 :datomic.peer-server/delete-db)
             administer_system (get map__26496 :datomic.peer-server/administer-system)
             group {:group :peer-server}
             op (fn op ([f] {:fn f, :groups [group], :routing :balanced}))
             ops #:datomic.catalog{:resolve-db
                                   (^clojure.lang.IFn op
                                     (fn fn__26499
                                       ([req _ch]
                                         (let [db_name (some-> req (:body) (:db-name))
                                               temp__5823__auto__ (get name_>id db_name)]
                                           (if temp__5823__auto__
                                             (let [db_id temp__5823__auto__]
                                               {:status 200, :body {:database-id db_id}})
                                             {:status 400,
                                              :body
                                              {:cause
                                               (str "Database " db_name " not found")}}))))),
                                   :list-dbs
                                   (^clojure.lang.IFn op
                                     (fn fn__26503
                                       ([_req _ch]
                                         (try
                                           {:status 200,
                                            :body {:result (^clojure.lang.IFn list_db_names)}}
                                           (catch
                                             java.lang.Exception
                                             _
                                             {:status 500,
                                              :body {:cause "Could not get databases"}})))))}
             ops (cond->
                   ops
                   create_db
                   (assoc
                     :datomic.catalog/create-db
                     (^clojure.lang.IFn op
                       (fn fn__26506
                         ([req _ch]
                           (let [db_name (some-> req (:body) (:db-name))]
                             (try
                               {:status 200, :body {:result (^clojure.lang.IFn create_db db_name)}}
                               (catch
                                 java.lang.IllegalArgumentException
                                 e
                                 {:status 400, :body {:cause (ex-message e)}})
                               (catch
                                 java.lang.Exception
                                 _
                                 {:status 500, :body {:cause "Could not create database"}})))))))
                   delete_db
                   (assoc
                     :datomic.catalog/delete-db
                     (^clojure.lang.IFn op
                       (fn fn__26509
                         ([req _ch]
                           (let [db_name (some-> req (:body) (:db-name))]
                             (try
                               {:status 200, :body {:result (^clojure.lang.IFn delete_db db_name)}}
                               (catch
                                 java.lang.IllegalArgumentException
                                 e
                                 {:status 400, :body {:cause (ex-message e)}})
                               (catch
                                 java.lang.Exception
                                 _
                                 {:status 500, :body {:cause "Could not delete database"}})))))))
                   administer_system
                   (assoc
                     :datomic.catalog/administer-system
                     (^clojure.lang.IFn op
                       (fn fn__26512
                         ([req _ch]
                           (try
                             {:status 200,
                              :body {:result (^clojure.lang.IFn administer_system (:body req))}}
                             (catch
                               java.lang.Exception
                               _
                               {:status 500, :body {:cause "Could not perform action"}})
                             (catch
                               java.lang.IllegalArgumentException
                               e
                               {:status 400, :body {:cause (ex-message e)}})))))))]
         (update (update nsm :groups conj group) :ops merge ops)))))
  (reset-meta!
    #'add-admin-ops
    (assoc
      {:arglists
       (clojure.core/list
         ['nsm
          #:datomic.peer-server{:keys
                                ['name->id
                                 'list-db-names
                                 'create-db
                                 'delete-db
                                 'administer-system]}]),
       :column (int 1)}
      :name
      'add-admin-ops
      :ns
      *ns*))
  (def create
   (fn create
     ([p__26515]
       (let [map__26516 p__26515
             map__26516 (if (seq? map__26516)
                          (if (next map__26516)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__26516))
                            (if (seq map__26516) (first map__26516) {}))
                          map__26516)
             args map__26516
             host (get map__26516 :host)
             port (get map__26516 :port)
             auth (get map__26516 :auth)
             db (get map__26516 :db)
             storage (get map__26516 :storage)
             concurrency (get map__26516 :concurrency)]
         (cast2slf4j/redirect)
         (client-spi/initialize! spi-support/client-spi-config)
         (let [admin_op_impls (if db (db-admin-op-impls db) (storage-admin-op-impls storage))
               token_manager (auth/create-token-manager args)
               server_spi (spi-support/create-spi
                            (:datomic.peer-server/id->conn admin_op_impls)
                            token_manager)
               nsm (client-spi/nano-services-map server_spi :peer-server)
               nsm (add-admin-ops nsm admin_op_impls)
               ncm {:marshaling marshal/instance,
                    :server
                    {:connection-concurrency concurrency,
                     :bind-address {:host host, :ssl-port port},
                     :pending-ops-limit 127,
                     :processing-concurrency concurrency,
                     :ping-path "/health",
                     :auth-callback (partial auth/callback auth),
                     :ssl (auth/ssl-config),
                     :bounding-timeout 60000},
                    :casters cast/casters,
                    :advertise-addr {:server-name host, :server-port port, :scheme "https"},
                    :nano-services nsm}]
           {:caches admin_op_impls,
            :server-spi server_spi,
            :ncm ncm,
            :nano (nano-impl/create ncm)})))))
  (reset-meta!
    #'create
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['host 'port 'auth 'db 'storage 'concurrency], :as 'args}]),
       :column (int 1)}
      :name
      'create
      :ns
      *ns*))
  (defn cli-split-by-comma ([s] (take 2 (str/split s #","))))
  (reset-meta!
    #'cli-split-by-comma
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'cli-split-by-comma
      :ns
      *ns*))
  (def cli-add-to-map
   (fn cli_add_to_map
     ([m k p__26519]
       (let [vec__26520 p__26519
             access_key (nth vec__26520 (int 0) nil)
             secret (nth vec__26520 (int 1) nil)]
         (update m k assoc access_key secret)))))
  (reset-meta!
    #'cli-add-to-map
    (assoc
      {:private true, :arglists (clojure.core/list ['m 'k ['access-key 'secret]]), :column (int 1)}
      :name
      'cli-add-to-map
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.peer-server" "cli-options") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.peer-server" "cli-options")
    [["-h" "--host HOST" "Listen on this host" :default "localhost"]
     ["-c"
      "--concurrency N"
      "Max number of concurrent requests"
      :default
      16
      :parse-fn
      (fn fn__26527 ([p1__26524#] (let [n (edn/read-string p1__26524#)] (when (integer? n) n))))]
     ["-p"
      "--port PORT"
      "Listen on this port"
      :default
      8998
      :parse-fn
      (fn fn__26529 ([p1__26525#] (let [n (edn/read-string p1__26525#)] (when (integer? n) n))))]
     ["-d"
      "--db NAME,URL"
      "Comma-separated database name and URL, repeatable."
      :parse-fn
      cli-split-by-comma
      :assoc-fn
      cli-add-to-map]
     ["-a"
      "--auth ACCESS,SECRET"
      "Comma-separated access key and secret, repeatable."
      :default
      nil
      :parse-fn
      cli-split-by-comma
      :assoc-fn
      cli-add-to-map]
     ["-s"
      "--storage URI"
      (str
        "Access any database at this storage-uri, which must use a '*' for the db-name."
        " Also enables d/create-database and d/delete-database."
        " Mutually exclusive with the -d option.")
      :validate-fn
      [(fn fn__26531 ([p1__26526#] (not= :backup (:protocol (uri/parse p1__26526#)))))]
      :validate-msg
      ["Cannot use :backup protocol with --storage option"]]])
  (def omitted-options #{:storage})
  (reset-meta! #'omitted-options (assoc {:column (int 1)} :name 'omitted-options :ns *ns*))
  (defn summary-fn ([specs] (cli/summarize (remove (comp omitted-options :id) specs))))
  (reset-meta!
    #'summary-fn
    (assoc {:arglists (clojure.core/list ['specs]), :column (int 1)} :name 'summary-fn :ns *ns*))
  (defn cross-opt-validation
    ([opts]
      (cond->
        []
        (and (:db opts) (:storage opts))
        (conj "The --db and --storage options are mutually exclusive."))))
  (reset-meta!
    #'cross-opt-validation
    (assoc
      {:arglists (clojure.core/list ['opts]), :column (int 1)}
      :name
      'cross-opt-validation
      :ns
      *ns*))
  (defn print-summary
    ([summary]
      (println "\nCommand-line arguments:\n")
      (println summary)
      (println "\nSee https://docs.datomic.com/peer-server.html for more information")))
  (reset-meta!
    #'print-summary
    (assoc
      {:arglists (clojure.core/list ['summary]), :column (int 1)}
      :name
      'print-summary
      :ns
      *ns*))
  (defn -main*
    ([& args]
      (let [map__26538 (cli/parse-opts args cli-options :summary-fn summary-fn)
            map__26538 (if (seq? map__26538)
                         (if (next map__26538)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26538))
                           (if (seq map__26538) (first map__26538) {}))
                         map__26538)
            options (get map__26538 :options)
            errors (get map__26538 :errors)
            summary (get map__26538 :summary)
            errors (if (seq errors) errors (cross-opt-validation options))]
        (cond
          (seq errors) (do (println (str/join "\n" errors)) (print-summary summary) nil)
          (not (s/valid? :datomic.peer-server/options options)) (do
                                                                  (println
                                                                    "Parsed peer-server args as:")
                                                                  (pp/pprint options)
                                                                  (println)
                                                                  (s/explain
                                                                    :datomic.peer-server/options
                                                                    options)
                                                                  (print-summary summary)
                                                                  nil)
          :else (do
                  (let [result (create options)]
                    (cast/event*
                      cast/instance
                      {:msg "PeerServerStarted", :options (dissoc options :auth :db)})
                    (if (config/property "datomic.printConnectionInfo")
                      (loop [seq_26539 (seq (:db options)) chunk_26540 nil count_26541 0 i_26542 0]
                        (if (< i_26542 count_26541)
                          (let [vec__26543 (.nth ^clojure.lang.Indexed chunk_26540 (int i_26542))
                                alias (nth vec__26543 (int 0) nil)
                                uri (nth vec__26543 (int 1) nil)]
                            (println "Serving" uri "as" alias)
                            (recur seq_26539 chunk_26540 count_26541 (inc i_26542)))
                          (let [temp__5825__auto__ (seq seq_26539)]
                            (when temp__5825__auto__
                              (let [seq_26539 temp__5825__auto__]
                                (if (chunked-seq? seq_26539)
                                  (let [c__6090__auto__ (chunk-first seq_26539)]
                                    (recur
                                      (chunk-rest seq_26539)
                                      c__6090__auto__
                                      (int (count c__6090__auto__))
                                      (int 0)))
                                  (let [vec__26546 (first seq_26539)
                                        alias (nth vec__26546 (int 0) nil)
                                        uri (nth vec__26546 (int 1) nil)]
                                    (println "Serving" uri "as" alias)
                                    (recur (next seq_26539) nil 0 0))))))))
                      (loop [seq_26549 (seq (:db options)) chunk_26550 nil count_26551 0 i_26552 0]
                        (if (< i_26552 count_26551)
                          (let [vec__26553 (.nth ^clojure.lang.Indexed chunk_26550 (int i_26552))
                                alias (nth vec__26553 (int 0) nil)]
                            (println "Serving" alias)
                            (recur seq_26549 chunk_26550 count_26551 (inc i_26552)))
                          (let [temp__5825__auto__ (seq seq_26549)]
                            (when temp__5825__auto__
                              (let [seq_26549 temp__5825__auto__]
                                (if (chunked-seq? seq_26549)
                                  (let [c__6090__auto__ (chunk-first seq_26549)]
                                    (recur
                                      (chunk-rest seq_26549)
                                      c__6090__auto__
                                      (int (count c__6090__auto__))
                                      (int 0)))
                                  (let [vec__26556 (first seq_26549)
                                        alias (nth vec__26556 (int 0) nil)]
                                    (println "Serving" alias)
                                    (recur (next seq_26549) nil 0 0)))))))))
                    result))))))
  (reset-meta!
    #'-main*
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main* :ns *ns*))
  (defn -main
    ([& args]
      (process/claim-pid-file)
      (when-not (apply -main* args) (java.lang.System/exit (int 1)) nil)))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))
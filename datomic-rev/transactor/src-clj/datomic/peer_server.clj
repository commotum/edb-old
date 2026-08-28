(do
  (clojure.core/in-ns 'datomic.peer-server)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.util.UUID)
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
        ['datomic.cast2slf4j.peer-server :as 'cast2slf4j]
        'datomic.client.api
        'datomic.client.protocol
        ['datomic.client-server.auth :as 'auth]
        ['datomic.client-server.marshaling :as 'marshal]
        ['datomic.client-server.spi-support :as 'spi-support]
        ['datomic.client-spi :as 'client-spi]
        ['datomic.config :as 'config]
        ['datomic.peer :as 'peer]
        'datomic.peer-client
        ['datomic.process :as 'process]
        ['datomic.slf4j :as 'logger]
        ['datomic.specs :as 'specs]
        ['datomic.uri :as 'uri])))
  (when-not (.equals 'datomic.peer-server 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.peer-server))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.util.UUID)
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
          ['datomic.cast2slf4j.peer-server :as 'cast2slf4j]
          'datomic.client.api
          'datomic.client.protocol
          ['datomic.client-server.auth :as 'auth]
          ['datomic.client-server.marshaling :as 'marshal]
          ['datomic.client-server.spi-support :as 'spi-support]
          ['datomic.client-spi :as 'client-spi]
          ['datomic.config :as 'config]
          ['datomic.peer :as 'peer]
          'datomic.peer-client
          ['datomic.process :as 'process]
          ['datomic.slf4j :as 'logger]
          ['datomic.specs :as 'specs]
          ['datomic.uri :as 'uri]))))
  (defn id-and-conn
    ([db_uri]
      (let [map__24326 (uri/parse-db db_uri)
            map__24326 (if (seq? map__24326)
                         (if (next map__24326)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24326))
                           (if (seq map__24326) (first map__24326) {}))
                         map__24326)
            protocol (get map__24326 :protocol)
            db_name (get map__24326 :db-name)
            uri (get map__24326 :uri)]
        (when (= protocol :mem) (peer/create-local-database db_name uri))
        (let [conn (d/connect uri)] [(:id (d/db conn)) conn]))))
  (defn database-uri? ([s] (boolean (:db-name (uri/parse s)))))
  (s/def-impl
    :datomic.peer-server/non-empty-string
    (clojure.core/list
      'clojure.spec.alpha/and
      'clojure.core/string?
      (clojure.core/list
        'fn*
        ['p1__24329#]
        (clojure.core/list
          'clojure.core/not
          (clojure.core/list 'clojure.core/empty? 'p1__24329#))))
    (s/and-spec-impl
      ['clojure.core/string?
       (clojure.core/list
         'clojure.core/fn
         ['%]
         (clojure.core/list 'clojure.core/not (clojure.core/list 'clojure.core/empty? '%)))]
      [string? (fn fn__24330 ([p1__24329#] (not (empty? p1__24329#))))]
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
      {:clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         :datomic.specs/db-name
         :datomic.peer-server/db-uri),
       :into {},
       :clojure.spec.alpha/kfn
       (fn fn__24333 ([i__1941__auto__ v__1942__auto__] (nth v__1942__auto__ (int 0)))),
       :clojure.spec.alpha/conform-all true,
       :clojure.spec.alpha/cpred (fn fn__24335 ([G__24332] (map? G__24332))),
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?}
      nil))
  (s/def-impl :datomic.peer-server/db :datomic.peer-server/db-map :datomic.peer-server/db-map)
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
      {:clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         :datomic.peer-server/access-key
         :datomic.peer-server/secret),
       :into {},
       :clojure.spec.alpha/kfn
       (fn fn__24338 ([i__1941__auto__ v__1942__auto__] (nth v__1942__auto__ (int 0)))),
       :clojure.spec.alpha/conform-all true,
       :clojure.spec.alpha/cpred (fn fn__24340 ([G__24337] (map? G__24337))),
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?}
      nil))
  (s/def-impl
    :datomic.peer-server/id->conn
    (clojure.core/list 'clojure.spec.alpha/map-of 'clojure.core/string? 'clojure.core/any?)
    (s/every-impl
      (clojure.core/list 'clojure.spec.alpha/tuple 'string? 'any?)
      (s/tuple-impl ['clojure.core/string? 'clojure.core/any?] [string? any?])
      {:clojure.spec.alpha/describe
       (clojure.core/list 'clojure.spec.alpha/map-of 'clojure.core/string? 'clojure.core/any?),
       :into {},
       :clojure.spec.alpha/kfn
       (fn fn__24343 ([i__1941__auto__ v__1942__auto__] (nth v__1942__auto__ (int 0)))),
       :clojure.spec.alpha/conform-all true,
       :clojure.spec.alpha/cpred (fn fn__24345 ([G__24342] (map? G__24342))),
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?}
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
      {:clojure.spec.alpha/describe
       (clojure.core/list 'clojure.spec.alpha/map-of :datomic.specs/db-name 'clojure.core/string?),
       :into {},
       :clojure.spec.alpha/kfn
       (fn fn__24348 ([i__1941__auto__ v__1942__auto__] (nth v__1942__auto__ (int 0)))),
       :clojure.spec.alpha/conform-all true,
       :clojure.spec.alpha/cpred (fn fn__24350 ([G__24347] (map? G__24347))),
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?}
      nil))
  (s/def-impl :datomic.peer-server/concurrency 'clojure.core/pos-int? pos-int?)
  (s/def-impl
    :datomic.peer-server/options
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req-un
      [:datomic.peer-server/host
       :datomic.peer-server/port
       :datomic.peer-server/db
       :datomic.peer-server/auth
       :datomic.peer-server/concurrency])
    (s/map-spec-impl
      {:req-un
       [:datomic.peer-server/host
        :datomic.peer-server/port
        :datomic.peer-server/db
        :datomic.peer-server/auth
        :datomic.peer-server/concurrency],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__24353 ([G__24352] (map? G__24352)))
        (fn fn__24355 ([G__24352] (contains? G__24352 :host)))
        (fn fn__24357 ([G__24352] (contains? G__24352 :port)))
        (fn fn__24359 ([G__24352] (contains? G__24352 :db)))
        (fn fn__24361 ([G__24352] (contains? G__24352 :auth)))
        (fn fn__24363 ([G__24352] (contains? G__24352 :concurrency)))],
       :keys-pred
       (fn fn__24365
         ([G__24352]
           (and
             (map? G__24352)
             (contains? G__24352 :host)
             (contains? G__24352 :port)
             (contains? G__24352 :db)
             (contains? G__24352 :auth)
             (contains? G__24352 :concurrency)))),
       :opt-keys [],
       :req-specs
       [:datomic.peer-server/host
        :datomic.peer-server/port
        :datomic.peer-server/db
        :datomic.peer-server/auth
        :datomic.peer-server/concurrency],
       :req nil,
       :req-keys [:host :port :db :auth :concurrency],
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
          (clojure.core/list 'clojure.core/contains? '% :db))
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
    'datomic.peer-server/connection-caches
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :db :datomic.peer-server/db)
      :ret
      (clojure.core/list
        'clojure.spec.alpha/keys
        :req
        [:datomic.peer-server/id->conn :datomic.peer-server/name->id]))
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :db :datomic.peer-server/db)
        (s/cat-impl [:db] [:datomic.peer-server/db] [:datomic.peer-server/db])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :db :datomic.peer-server/db)
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/keys
          :req
          [:datomic.peer-server/id->conn :datomic.peer-server/name->id])
        (s/map-spec-impl
          {:req-un nil,
           :opt-un nil,
           :gfn nil,
           :pred-exprs
           [(fn fn__24373 ([G__24372] (map? G__24372)))
            (fn fn__24375 ([G__24372] (contains? G__24372 :datomic.peer-server/id->conn)))
            (fn fn__24377 ([G__24372] (contains? G__24372 :datomic.peer-server/name->id)))],
           :keys-pred
           (fn fn__24379
             ([G__24372]
               (and
                 (map? G__24372)
                 (contains? G__24372 :datomic.peer-server/id->conn)
                 (contains? G__24372 :datomic.peer-server/name->id)))),
           :opt-keys [],
           :req-specs [:datomic.peer-server/id->conn :datomic.peer-server/name->id],
           :req [:datomic.peer-server/id->conn :datomic.peer-server/name->id],
           :req-keys [:datomic.peer-server/id->conn :datomic.peer-server/name->id],
           :opt-specs [],
           :pred-forms
           [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
            (clojure.core/list
              'clojure.core/fn
              ['%]
              (clojure.core/list 'clojure.core/contains? '% :datomic.peer-server/id->conn))
            (clojure.core/list
              'clojure.core/fn
              ['%]
              (clojure.core/list 'clojure.core/contains? '% :datomic.peer-server/name->id))],
           :opt nil})
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/keys
        :req
        [:datomic.peer-server/id->conn :datomic.peer-server/name->id])
      nil
      nil
      nil))
  (defn connection-caches
    ([db]
      (reduce-kv
        (fn fn__24383
          ([caches name db_uri]
            (let [vec__24384 (id-and-conn db_uri)
                  id (nth vec__24384 (int 0) nil)
                  conn (nth vec__24384 (int 1) nil)]
              (assoc-in
                (assoc-in caches [:datomic.peer-server/id->conn id] conn)
                [:datomic.peer-server/name->id name]
                id))))
        #:datomic.peer-server{:id->conn {}, :name->id {}}
        db)))
  (defn cache-entry->db-config
    ([p__24389]
      (let [vec__24390 p__24389 k (nth vec__24390 (int 0) nil) v (nth vec__24390 (int 1) nil)]
        {:db-id k, :db-name (:name v)})))
  (defn add-admin-ops
    ([nsm name_>id]
      (update
        (update
          (update nsm :groups conj {:group :peer-server})
          :ops
          assoc
          :datomic.catalog/resolve-db
          {:fn
           (fn fn__24394
             ([req ch]
               (let [db_name (some-> req (:body) (:db-name))
                     temp__5802__auto__ (get name_>id db_name)]
                 (if temp__5802__auto__
                   (let [db_id temp__5802__auto__] {:status 200, :body {:database-id db_id}})
                   {:status 400, :body {:cause (str "Database " db_name " not found")}})))),
           :groups [{:group :peer-server}],
           :routing :balanced})
        :ops
        assoc
        :datomic.catalog/list-dbs
        {:fn (fn fn__24398 ([req ch] {:status 200, :body {:result (into [] (keys name_>id))}})),
         :groups [{:group :peer-server}],
         :routing :balanced})))
  (defn create
    ([p__24401]
      (let [map__24402 p__24401
            map__24402 (if (seq? map__24402)
                         (if (next map__24402)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24402))
                           (if (seq map__24402) (first map__24402) {}))
                         map__24402)
            args map__24402
            host (get map__24402 :host)
            port (get map__24402 :port)
            auth (get map__24402 :auth)
            db (get map__24402 :db)
            concurrency (get map__24402 :concurrency)]
        (cast2slf4j/redirect)
        (client-spi/initialize! spi-support/client-spi-config)
        (let [caches (connection-caches db)
              token_manager (auth/create-token-manager args)
              server_spi (spi-support/create-spi
                           (:datomic.peer-server/id->conn caches)
                           token_manager)
              nsm (client-spi/nano-services-map server_spi :peer-server)
              nsm (add-admin-ops nsm (:datomic.peer-server/name->id caches))
              op_limit (* 2 (.availableProcessors (java.lang.Runtime/getRuntime)))
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
          {:caches caches, :server-spi server_spi, :ncm ncm, :nano (nano-impl/create ncm)}))))
  (defn cli-split-by-comma ([s] (take 2 (str/split s #","))))
  (reset-meta!
    #'cli-split-by-comma
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column 1}
      :name
      'cli-split-by-comma
      :ns
      *ns*))
  (defn cli-add-to-map
    ([m k p__24405]
      (let [vec__24406 p__24405
            access_key (nth vec__24406 (int 0) nil)
            secret (nth vec__24406 (int 1) nil)]
        (update m k assoc access_key secret))))
  (reset-meta!
    #'cli-add-to-map
    (assoc
      {:private true, :arglists (clojure.core/list ['m 'k ['access-key 'secret]]), :column 1}
      :name
      'cli-add-to-map
      :ns
      *ns*))
  (def cli-options
   [["-h" "--host HOST" "Listen on this host" :default "localhost"]
    ["-c"
     "--concurrency N"
     "Max number of concurrent requests"
     :default
     16
     :parse-fn
     (fn fn__24412 ([p1__24410#] (let [n (edn/read-string p1__24410#)] (when (integer? n) n))))]
    ["-p"
     "--port PORT"
     "Listen on this port"
     :default
     8998
     :parse-fn
     (fn fn__24414 ([p1__24411#] (let [n (edn/read-string p1__24411#)] (when (integer? n) n))))]
    ["-d"
     "--db NAME,URL"
     "Comma-separated database name and URL, repeatable."
     :default
     nil
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
     cli-add-to-map]])
  (defn print-summary
    ([summary]
      (println "\nCommand-line arguments:\n")
      (println summary)
      (println "\nSee https://docs.datomic.com/peer-server.html for more information")))
  (defn -main*
    ([& args]
      (let [map__24417 (cli/parse-opts args cli-options)
            map__24417 (if (seq? map__24417)
                         (if (next map__24417)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__24417))
                           (if (seq map__24417) (first map__24417) {}))
                         map__24417)
            options (get map__24417 :options)
            errors (get map__24417 :errors)
            summary (get map__24417 :summary)]
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
                      (loop [seq_24418 (seq (:db options)) chunk_24419 nil count_24420 0 i_24421 0]
                        (if (< i_24421 count_24420)
                          (let [vec__24422 (.nth ^clojure.lang.Indexed chunk_24419 (int i_24421))
                                alias (nth vec__24422 (int 0) nil)
                                uri (nth vec__24422 (int 1) nil)]
                            (println "Serving" uri "as" alias)
                            (recur seq_24418 chunk_24419 count_24420 (inc i_24421)))
                          (let [temp__5804__auto__ (seq seq_24418)]
                            (when temp__5804__auto__
                              (let [seq_24418 temp__5804__auto__]
                                (if (chunked-seq? seq_24418)
                                  (let [c__6065__auto__ (chunk-first seq_24418)]
                                    (recur
                                      (chunk-rest seq_24418)
                                      c__6065__auto__
                                      (int (count c__6065__auto__))
                                      (int 0)))
                                  (let [vec__24425 (first seq_24418)
                                        alias (nth vec__24425 (int 0) nil)
                                        uri (nth vec__24425 (int 1) nil)]
                                    (println "Serving" uri "as" alias)
                                    (recur (next seq_24418) nil 0 0))))))))
                      (loop [seq_24428 (seq (:db options)) chunk_24429 nil count_24430 0 i_24431 0]
                        (if (< i_24431 count_24430)
                          (let [vec__24432 (.nth ^clojure.lang.Indexed chunk_24429 (int i_24431))
                                alias (nth vec__24432 (int 0) nil)]
                            (println "Serving" alias)
                            (recur seq_24428 chunk_24429 count_24430 (inc i_24431)))
                          (let [temp__5804__auto__ (seq seq_24428)]
                            (when temp__5804__auto__
                              (let [seq_24428 temp__5804__auto__]
                                (if (chunked-seq? seq_24428)
                                  (let [c__6065__auto__ (chunk-first seq_24428)]
                                    (recur
                                      (chunk-rest seq_24428)
                                      c__6065__auto__
                                      (int (count c__6065__auto__))
                                      (int 0)))
                                  (let [vec__24435 (first seq_24428)
                                        alias (nth vec__24435 (int 0) nil)]
                                    (println "Serving" alias)
                                    (recur (next seq_24428) nil 0 0)))))))))
                    result))))))
  (defn -main
    ([& args]
      (process/claim-pid-file)
      (when-not (apply -main* args) (java.lang.System/exit (int 1)) nil))))
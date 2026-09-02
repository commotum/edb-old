(do
  (clojure.core/in-ns 'datomic.catalog)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.catalog)
    {:doc
     "Persistent catalog of database names, identities, lifecycle state, and storage roots for a Datomic system. Catalog updates use storage coordination so create, rename, delete, and restore preserve database identity."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.io :as 'io]
        ['clojure.set :as 'set]
        ['clojure.edn :as 'edn])))
  (when-not (.equals 'datomic.catalog 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.catalog))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.io :as 'io]
          ['clojure.set :as 'set]
          ['clojure.edn :as 'edn]))))
  (def catalog-key "pod-catalog")
  (reset-meta! #'catalog-key (assoc {:const true, :column (int 1)} :name 'catalog-key :ns *ns*))
  (defn pod->catalog
    ([pod]
      (let [map__9889 pod
            map__9889 (if (seq? map__9889)
                        (if (next map__9889)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9889))
                          (if (seq map__9889) (first map__9889) {}))
                        map__9889)
            buf (get map__9889 :buf)
            rev (get map__9889 :rev)]
        (assoc (edn/read-string (io/bbuf->string buf)) :datomic/rev rev))))
  (reset-meta!
    #'pod->catalog
    (assoc {:arglists (clojure.core/list ['pod]), :column (int 1)} :name 'pod->catalog :ns *ns*))
  (defn valid-db-name? ([db-name] (not (re-find #"[\"*:=/?]" db-name))))
  (reset-meta!
    #'valid-db-name?
    (assoc
      {:private true, :arglists (clojure.core/list ['db-name]), :column (int 1)}
      :name
      'valid-db-name?
      :ns
      *ns*))
  ;; Reads the system-scoped catalog pod and attaches its storage revision as :datomic/rev.
  (defn get-catalog
    ([cluster]
      (when-not (nil? (cluster/dbId cluster))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str (clojure.core/list 'nil? (clojure.core/list 'cluster/dbId 'cluster)))))))
      (let [temp__5823__auto__ (deref (cluster/get-pod cluster "pod-catalog"))]
        (if temp__5823__auto__ (let [pod temp__5823__auto__] (pod->catalog pod)) {}))))
  (reset-meta!
    #'get-catalog
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'get-catalog
      :ns
      *ns*))
  ;; Writes exactly the next catalog revision and returns {:failed :conflict} on a competing update.
  (defn put-catalog
    ([cluster catalog_map]
      (when-not (nil? (cluster/dbId cluster))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str (clojure.core/list 'nil? (clojure.core/list 'cluster/dbId 'cluster)))))))
      (let [current_rev (:datomic/rev catalog_map)
            next_rev (if current_rev (inc current_rev) 0)
            catalog (dissoc catalog_map :datomic/rev)
            pod (deref
                  (cluster/update-pod cluster "pod-catalog" next_rev nil (io/clj->bbuf catalog)))]
        (if (:failed pod) pod (pod->catalog pod)))))
  (reset-meta!
    #'put-catalog
    (assoc
      {:arglists (clojure.core/list ['cluster 'catalog-map]), :column (int 1)}
      :name
      'put-catalog
      :ns
      *ns*))
  (defn db-names ([catalog] (into #{} (filter string? (keys catalog)))))
  (reset-meta!
    #'db-names
    (assoc {:arglists (clojure.core/list ['catalog]), :column (int 1)} :name 'db-names :ns *ns*))
  (defn get-database-names ([cluster] (filter string? (keys (get-catalog cluster)))))
  (reset-meta!
    #'get-database-names
    (assoc
      {:arglists (clojure.core/list ['cluster]), :column (int 1)}
      :name
      'get-database-names
      :ns
      *ns*))
  (defn db-ids
    ([catalog]
      (into
        #{}
        (map (fn fn__9898 ([p1__9897#] (get-in catalog [p1__9897# :db-id]))) (db-names catalog)))))
  (reset-meta!
    #'db-ids
    (assoc {:arglists (clojure.core/list ['catalog]), :column (int 1)} :name 'db-ids :ns *ns*))
  (defn db-id->db-name
    ([catalog db_id]
      (let [inverted (reduce
                       (fn fn__9902
                         ([m p__9901]
                           (let [vec__9903 p__9901
                                 k (nth vec__9903 (int 0) nil)
                                 map__9906 (nth vec__9903 (int 1) nil)
                                 map__9906 (if (seq? map__9906)
                                             (if (next map__9906)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__9906))
                                               (if (seq map__9906) (first map__9906) {}))
                                             map__9906)
                                 db_id (get map__9906 :db-id)]
                             (if db_id (assoc m db_id k) m))))
                       {}
                       catalog)]
        (get inverted db_id))))
  (reset-meta!
    #'db-id->db-name
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-id]), :column (int 1)}
      :name
      'db-id->db-name
      :ns
      *ns*))
  (defn db-name->db-id ([catalog db-name] (get-in catalog [db-name :db-id])))
  (reset-meta!
    #'db-name->db-id
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-name]), :column (int 1)}
      :name
      'db-name->db-id
      :ns
      *ns*))
  (def with-retry
   (fn with_retry
     ([&form &env & body]
       (seq
         (concat
           (clojure.core/list 'datomic.common/retry-fn)
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'clojure.core/fn)
                 (clojure.core/list (apply vector (seq (concat))))
                 body)))
           (clojure.core/list :pred)
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'fn*)
                 (clojure.core/list
                   (apply vector (seq (concat (clojure.core/list 'p1__9910__9911__auto__)))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/=)
                       (clojure.core/list :conflict)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list :failed)
                             (clojure.core/list 'p1__9910__9911__auto__))))))))))
           (clojure.core/list :backoff)
           (clojure.core/list 0)
           (clojure.core/list :max-retries)
           (clojure.core/list 10)
           (clojure.core/list :log-retry)
           (clojure.core/list 'datomic.common/log-retry))))))
  (reset-meta!
    #'with-retry
    (assoc {:arglists (clojure.core/list ['& 'body]), :column (int 1)} :name 'with-retry :ns *ns*))
  (.setMacro #'with-retry)
  ;; Applies a conditional catalog transformation, retrying compare-and-swap conflicts up to ten times.
  (defn update-catalog
    ([cluster condition f]
      (common/retry-fn
        (fn fn__9913
          ([]
            (let [catalog (get-catalog cluster)
                  temp__5823__auto__ (^clojure.lang.IFn condition catalog)]
              (if temp__5823__auto__
                (let [result temp__5823__auto__] result)
                (let [resp (put-catalog cluster (^clojure.lang.IFn f catalog))]
                  (if (:failed resp) resp {:old catalog, :new resp}))))))
        :pred
        (fn fn__9916 ([p1__9910__9911__auto__] (= :conflict (:failed p1__9910__9911__auto__))))
        :backoff
        0
        :max-retries
        10
        :log-retry
        common/log-retry)))
  (reset-meta!
    #'update-catalog
    (assoc
      {:arglists (clojure.core/list ['cluster 'condition 'f]), :column (int 1)}
      :name
      'update-catalog
      :ns
      *ns*))
  (defn update-succeeded? ([m] (contains? m :new)))
  (reset-meta!
    #'update-succeeded?
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'update-succeeded?
      :ns
      *ns*))
  (defn conflict-check-fn
    ([db_name db_id]
      (fn fn__9920
        ([catalog]
          (let [temp__5823__auto__ (db-name->db-id catalog db_name)]
            (cond
              temp__5823__auto__ (let [existing_id temp__5823__auto__]
                                   (if (or (not db_id) (= db_id existing_id))
                                     {:exists db_name}
                                     {:name-conflict db_name}))
              (and db_id (contains? (db-ids catalog) db_id)) (do
                                                               {:id-conflict
                                                                (db-id->db-name
                                                                  catalog
                                                                  db_id)})))))))
  (reset-meta!
    #'conflict-check-fn
    (assoc
      {:arglists (clojure.core/list ['db-name 'db-id]), :column (int 1)}
      :name
      'conflict-check-fn
      :ns
      *ns*))
  (defn add-database
    ([catalog db_name db_id]
      (when-not db_name
        (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-name)))))
      (when-not db_id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-id)))))
      (update (assoc catalog db_name {:db-id db_id}) :datomic/deleted (fnil disj #{}) db_id)))
  (reset-meta!
    #'add-database
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-name 'db-id]), :column (int 1)}
      :name
      'add-database
      :ns
      *ns*))
  ;; Allocates a stable database identity and records its name when neither name nor identity conflicts.
  (defn create-database*
    ([cluster p__9928]
      (let [map__9929 p__9928
            map__9929 (if (seq? map__9929)
                        (if (next map__9929)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9929))
                          (if (seq map__9929) (first map__9929) {}))
                        map__9929)
            db_name (get map__9929 :db-name)
            db_id (get map__9929 :db-id)]
        (when-not db_name
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-name)))))
        (if (valid-db-name? db_name)
          (let [assigned_db_id (or db_id (str db_name "-" (java.util.UUID/randomUUID)))
                resp (update-catalog
                       cluster
                       (conflict-check-fn db_name db_id)
                       (fn fn__9930
                         ([p1__9927#] (add-database p1__9927# db_name assigned_db_id))))]
            (if (update-succeeded? resp) {:created db_name, :db-id assigned_db_id} resp))
          {:invalid-db-name db_name}))))
  (reset-meta!
    #'create-database*
    (assoc
      {:arglists (clojure.core/list ['cluster {:keys ['db-name 'db-id]}]), :column (int 1)}
      :name
      'create-database*
      :ns
      *ns*))
  (defn create-database ([system-cluster desc] (create-database* system-cluster desc)))
  (reset-meta!
    #'create-database
    (assoc
      {:arglists (clojure.core/list ['system-cluster 'desc]), :column (int 1)}
      :name
      'create-database
      :ns
      *ns*))
  (defn rename
    ([catalog db-name new-name] (dissoc (assoc catalog new-name (get catalog db-name)) db-name)))
  (reset-meta!
    #'rename
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-name 'new-name]), :column (int 1)}
      :name
      'rename
      :ns
      *ns*))
  (defn rename-database
    ([cluster db_name new_name]
      (let [condition (fn condition
                        ([p1__9936#]
                          (cond
                            (not (valid-db-name? new_name)) {:invalid-db-name new_name}
                            (not (get p1__9936# db_name)) {:does-not-exist db_name}
                            (get p1__9936# new_name) (do {:exists db_name}))))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__9940 ([p1__9937#] (rename p1__9937# db_name new_name))))]
        (if (update-succeeded? resp) {:renamed-to new_name} resp))))
  (reset-meta!
    #'rename-database
    (assoc
      {:arglists (clojure.core/list ['cluster 'db-name 'new-name]), :column (int 1)}
      :name
      'rename-database
      :ns
      *ns*))
  (defn delete
    ([catalog db_name]
      (let [temp__5823__auto__ (db-name->db-id catalog db_name)]
        (if temp__5823__auto__
          (let [dbid temp__5823__auto__]
            (dissoc (update catalog :datomic/deleted (fnil conj #{}) dbid) db_name))
          catalog))))
  (reset-meta!
    #'delete
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-name]), :column (int 1)}
      :name
      'delete
      :ns
      *ns*))
  ;; Removes the live name and retains the database identity in :datomic/deleted for later reclamation.
  (defn delete-database
    ([cluster db_name]
      (let [condition (fn condition
                        ([p1__9945#]
                          (when (not (get p1__9945# db_name)) {:does-not-exist db_name})))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__9949 ([p1__9946#] (delete p1__9946# db_name))))]
        (if (update-succeeded? resp)
          {:deleted db_name,
           :db-id
           (first
             (set/difference
               (get-in resp [:new :datomic/deleted])
               (get-in resp [:old :datomic/deleted])))}
          resp))))
  (reset-meta!
    #'delete-database
    (assoc
      {:arglists (clojure.core/list ['cluster 'db-name]), :column (int 1)}
      :name
      'delete-database
      :ns
      *ns*))
  (defn undelete-database
    ([cluster db_id db_name]
      (let [condition (fn condition
                        ([p1__9952#]
                          (cond
                            (get p1__9952# db_name) {:name-already-taken db_name}
                            (not (get-in p1__9952# [:datomic/deleted db_id])) (do
                                                                                {:id-not-deleted
                                                                                 db_id}))))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__9956 ([p1__9953#] (add-database p1__9953# db_name db_id))))]
        (if (update-succeeded? resp) {:undeleted db_name, :db-id db_id} resp))))
  (reset-meta!
    #'undelete-database
    (assoc
      {:arglists (clojure.core/list ['cluster 'db-id 'db-name]), :column (int 1)}
      :name
      'undelete-database
      :ns
      *ns*))
  (defn remove-deleted ([catalog db-id] (update catalog :datomic/deleted (fnil disj #{}) db-id)))
  (reset-meta!
    #'remove-deleted
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-id]), :column (int 1)}
      :name
      'remove-deleted
      :ns
      *ns*))
  (defn remove-deleted-database
    ([cluster db_id]
      (let [condition (fn condition
                        ([p1__9960#]
                          (when-not (contains? (get p1__9960# :datomic/deleted) db_id)
                            {:does-not-exist db_id})))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__9964 ([p1__9961#] (remove-deleted p1__9961# db_id))))]
        (if (update-succeeded? resp) {:removed db_id} resp))))
  (reset-meta!
    #'remove-deleted-database
    (assoc
      {:arglists (clojure.core/list ['cluster 'db-id]), :column (int 1)}
      :name
      'remove-deleted-database
      :ns
      *ns*))
  (defn deleted?
    ([catalog db_id]
      (when-not (map? catalog)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'map? 'catalog))))))
      (and
        (not (contains? (db-ids catalog) db_id))
        (contains? (get catalog :datomic/deleted) db_id))))
  (reset-meta!
    #'deleted?
    (assoc
      {:arglists (clojure.core/list ['catalog 'db-id]), :column (int 1)}
      :name
      'deleted?
      :ns
      *ns*))
  (defn deleted-database-id?
    ([cluster db_id]
      (when-not db_id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-id)))))
      (let [temp__5823__auto__ (get-catalog cluster)]
        (if temp__5823__auto__
          (let [catalog temp__5823__auto__] (deleted? catalog db_id))
          (do (throw (java.lang.RuntimeException. "No catalog")) nil)))))
  (reset-meta!
    #'deleted-database-id?
    (assoc
      {:arglists (clojure.core/list ['cluster 'db-id]), :column (int 1)}
      :name
      'deleted-database-id?
      :ns
      *ns*))
  (defn parse-db-conf ([db-conf] db-conf))
  (reset-meta!
    #'parse-db-conf
    (assoc
      {:arglists (clojure.core/list ['db-conf]), :column (int 1)}
      :name
      'parse-db-conf
      :ns
      *ns*)))

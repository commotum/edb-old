(do
  (clojure.core/in-ns 'datomic.catalog)
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
  (reset-meta! #'catalog-key (assoc {:const true, :column 1} :name 'catalog-key :ns *ns*))
  (defn pod->catalog
    ([pod]
      (let [map__11075 pod
            map__11075 (if (seq? map__11075)
                         (clojure.lang.PersistentHashMap/create (seq map__11075))
                         map__11075)
            buf (get map__11075 :buf)
            rev (get map__11075 :rev)]
        (assoc (edn/read-string (io/bbuf->string buf)) :datomic/rev rev))))
  (defn valid-db-name? ([db_name] (not (re-find #"[\"*:=/?]" db_name))))
  (reset-meta!
    #'valid-db-name?
    (assoc
      {:private true, :arglists (clojure.core/list ['db-name]), :column 1}
      :name
      'valid-db-name?
      :ns
      *ns*))
  (defn get-catalog
    ([cluster]
      (when-not (nil? (cluster/dbId cluster))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str (clojure.core/list 'nil? (clojure.core/list 'cluster/dbId 'cluster)))))))
      (let [temp__5455__auto__ (deref (cluster/get-pod cluster "pod-catalog"))]
        (if temp__5455__auto__ (let [pod temp__5455__auto__] (pod->catalog pod)) {}))))
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
  (defn db-names ([catalog] (into #{} (filter string? (keys catalog)))))
  (defn get-database-names ([cluster] (filter string? (keys (get-catalog cluster)))))
  (defn db-ids
    ([catalog]
      (into
        #{}
        (map
          (fn fn__11084 ([p1__11083#] (get-in catalog [p1__11083# :db-id])))
          (db-names catalog)))))
  (defn db-id->db-name
    ([catalog db_id]
      (let [inverted (reduce
                       (fn fn__11088
                         ([m p__11087]
                           (let [vec__11089 p__11087
                                 k (nth vec__11089 (int 0) nil)
                                 map__11092 (nth vec__11089 (int 1) nil)
                                 map__11092 (if (seq? map__11092)
                                              (clojure.lang.PersistentHashMap/create
                                                (seq map__11092))
                                              map__11092)
                                 db_id (get map__11092 :db-id)]
                             (if db_id (assoc m db_id k) m))))
                       {}
                       catalog)]
        (get inverted db_id))))
  (defn db-name->db-id ([catalog db_name] (get-in catalog [db_name :db-id])))
  (defn with-retry
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
                  (apply vector (seq (concat (clojure.core/list 'p1__11096__11097__auto__)))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/=)
                      (clojure.core/list :conflict)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list :failed)
                            (clojure.core/list 'p1__11096__11097__auto__))))))))))
          (clojure.core/list :backoff)
          (clojure.core/list 0)
          (clojure.core/list :max-retries)
          (clojure.core/list 10)
          (clojure.core/list :log-retry)
          (clojure.core/list 'datomic.common/log-retry)))))
  (.setMacro #'with-retry)
  (defn update-catalog
    ([cluster condition f]
      (common/retry-fn
        (fn fn__11099
          ([]
            (let [catalog (get-catalog cluster)
                  temp__5455__auto__ (^clojure.lang.IFn condition catalog)]
              (if temp__5455__auto__
                (let [result temp__5455__auto__] result)
                (let [resp (put-catalog cluster (^clojure.lang.IFn f catalog))]
                  (if (:failed resp) resp {:old catalog, :new resp}))))))
        :pred
        (fn fn__11102
          ([p1__11096__11097__auto__] (= :conflict (:failed p1__11096__11097__auto__))))
        :backoff
        0
        :max-retries
        10
        :log-retry
        common/log-retry)))
  (defn update-succeeded? ([m] (contains? m :new)))
  (defn conflict-check-fn
    ([db_name db_id]
      (fn fn__11106
        ([catalog]
          (let [temp__5455__auto__ (db-name->db-id catalog db_name)]
            (cond
              temp__5455__auto__ (let [existing_id temp__5455__auto__]
                                   (if (or (not db_id) (= db_id existing_id))
                                     {:exists db_name}
                                     {:name-conflict db_name}))
              (and db_id (contains? (db-ids catalog) db_id)) (do
                                                               {:id-conflict
                                                                (db-id->db-name
                                                                  catalog
                                                                  db_id)})))))))
  (defn add-database
    ([catalog db_name db_id]
      (when-not db_name
        (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-name)))))
      (when-not db_id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-id)))))
      (update (assoc catalog db_name {:db-id db_id}) :datomic/deleted (fnil disj #{}) db_id)))
  (defn create-database*
    ([cluster p__11114]
      (let [map__11115 p__11114
            map__11115 (if (seq? map__11115)
                         (clojure.lang.PersistentHashMap/create (seq map__11115))
                         map__11115)
            db_name (get map__11115 :db-name)
            db_id (get map__11115 :db-id)]
        (when-not db_name
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-name)))))
        (if (valid-db-name? db_name)
          (let [assigned_db_id (or db_id (str db_name "-" (java.util.UUID/randomUUID)))
                resp (update-catalog
                       cluster
                       (conflict-check-fn db_name db_id)
                       (fn fn__11116
                         ([p1__11113#] (add-database p1__11113# db_name assigned_db_id))))]
            (if (update-succeeded? resp) {:created db_name, :db-id assigned_db_id} resp))
          {:invalid-db-name db_name}))))
  (defn create-database ([system_cluster desc] (create-database* system_cluster desc)))
  (defn rename
    ([catalog db_name new_name] (dissoc (assoc catalog new_name (get catalog db_name)) db_name)))
  (defn rename-database
    ([cluster db_name new_name]
      (let [condition (fn condition
                        ([p1__11122#]
                          (cond
                            (not (valid-db-name? new_name)) {:invalid-db-name new_name}
                            (not (get p1__11122# db_name)) {:does-not-exist db_name}
                            (get p1__11122# new_name) (do {:exists db_name}))))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__11126 ([p1__11123#] (rename p1__11123# db_name new_name))))]
        (if (update-succeeded? resp) {:renamed-to new_name} resp))))
  (defn delete
    ([catalog db_name]
      (let [temp__5455__auto__ (db-name->db-id catalog db_name)]
        (if temp__5455__auto__
          (let [dbid temp__5455__auto__]
            (dissoc (update catalog :datomic/deleted (fnil conj #{}) dbid) db_name))
          catalog))))
  (defn delete-database
    ([cluster db_name]
      (let [condition (fn condition
                        ([p1__11131#]
                          (when (not (get p1__11131# db_name)) {:does-not-exist db_name})))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__11135 ([p1__11132#] (delete p1__11132# db_name))))]
        (if (update-succeeded? resp)
          {:deleted db_name,
           :db-id
           (first
             (set/difference
               (get-in resp [:new :datomic/deleted])
               (get-in resp [:old :datomic/deleted])))}
          resp))))
  (defn undelete-database
    ([cluster db_id db_name]
      (let [condition (fn condition
                        ([p1__11138#]
                          (cond
                            (get p1__11138# db_name) {:name-already-taken db_name}
                            (not (get-in p1__11138# [:datomic/deleted db_id])) (do
                                                                                 {:id-not-deleted
                                                                                  db_id}))))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__11142 ([p1__11139#] (add-database p1__11139# db_name db_id))))]
        (if (update-succeeded? resp) {:undeleted db_name, :db-id db_id} resp))))
  (defn remove-deleted ([catalog db_id] (update catalog :datomic/deleted (fnil disj #{}) db_id)))
  (defn remove-deleted-database
    ([cluster db_id]
      (let [condition (fn condition
                        ([p1__11146#]
                          (when-not (contains? (get p1__11146# :datomic/deleted) db_id)
                            {:does-not-exist db_id})))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__11150 ([p1__11147#] (remove-deleted p1__11147# db_id))))]
        (if (update-succeeded? resp) {:removed db_id} resp))))
  (defn deleted?
    ([catalog db_id]
      (when-not (map? catalog)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'map? 'catalog))))))
      (and
        (not (contains? (db-ids catalog) db_id))
        (contains? (get catalog :datomic/deleted) db_id))))
  (defn deleted-database-id?
    ([cluster db_id]
      (when-not db_id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-id)))))
      (let [temp__5455__auto__ (get-catalog cluster)]
        (if temp__5455__auto__
          (let [catalog temp__5455__auto__] (deleted? catalog db_id))
          (do (throw (java.lang.RuntimeException. "No catalog")) nil)))))
  (defn parse-db-conf ([db_conf] db_conf)))
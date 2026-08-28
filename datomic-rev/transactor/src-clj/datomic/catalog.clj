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
      (let [map__15943 pod
            map__15943 (if (seq? map__15943)
                         (if (next map__15943)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15943))
                           (if (seq map__15943) (first map__15943) {}))
                         map__15943)
            buf (get map__15943 :buf)
            rev (get map__15943 :rev)]
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
      (let [temp__5802__auto__ (deref (cluster/get-pod cluster "pod-catalog"))]
        (if temp__5802__auto__ (let [pod temp__5802__auto__] (pod->catalog pod)) {}))))
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
          (fn fn__15952 ([p1__15951#] (get-in catalog [p1__15951# :db-id])))
          (db-names catalog)))))
  (defn db-id->db-name
    ([catalog db_id]
      (let [inverted (reduce
                       (fn fn__15956
                         ([m p__15955]
                           (let [vec__15957 p__15955
                                 k (nth vec__15957 (int 0) nil)
                                 map__15960 (nth vec__15957 (int 1) nil)
                                 map__15960 (if (seq? map__15960)
                                              (if (next map__15960)
                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                  (to-array map__15960))
                                                (if (seq map__15960) (first map__15960) {}))
                                              map__15960)
                                 db_id (get map__15960 :db-id)]
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
                  (apply vector (seq (concat (clojure.core/list 'p1__15964__15965__auto__)))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/=)
                      (clojure.core/list :conflict)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list :failed)
                            (clojure.core/list 'p1__15964__15965__auto__))))))))))
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
        (fn fn__15967
          ([]
            (let [catalog (get-catalog cluster)
                  temp__5802__auto__ (^clojure.lang.IFn condition catalog)]
              (if temp__5802__auto__
                (let [result temp__5802__auto__] result)
                (let [resp (put-catalog cluster (^clojure.lang.IFn f catalog))]
                  (if (:failed resp) resp {:old catalog, :new resp}))))))
        :pred
        (fn fn__15970
          ([p1__15964__15965__auto__] (= :conflict (:failed p1__15964__15965__auto__))))
        :backoff
        0
        :max-retries
        10
        :log-retry
        common/log-retry)))
  (defn update-succeeded? ([m] (contains? m :new)))
  (defn conflict-check-fn
    ([db_name db_id]
      (fn fn__15974
        ([catalog]
          (let [temp__5802__auto__ (db-name->db-id catalog db_name)]
            (cond
              temp__5802__auto__ (let [existing_id temp__5802__auto__]
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
    ([cluster p__15982]
      (let [map__15983 p__15982
            map__15983 (if (seq? map__15983)
                         (if (next map__15983)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15983))
                           (if (seq map__15983) (first map__15983) {}))
                         map__15983)
            db_name (get map__15983 :db-name)
            db_id (get map__15983 :db-id)]
        (when-not db_name
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'db-name)))))
        (if (valid-db-name? db_name)
          (let [assigned_db_id (or db_id (str db_name "-" (java.util.UUID/randomUUID)))
                resp (update-catalog
                       cluster
                       (conflict-check-fn db_name db_id)
                       (fn fn__15984
                         ([p1__15981#] (add-database p1__15981# db_name assigned_db_id))))]
            (if (update-succeeded? resp) {:created db_name, :db-id assigned_db_id} resp))
          {:invalid-db-name db_name}))))
  (defn create-database ([system_cluster desc] (create-database* system_cluster desc)))
  (defn rename
    ([catalog db_name new_name] (dissoc (assoc catalog new_name (get catalog db_name)) db_name)))
  (defn rename-database
    ([cluster db_name new_name]
      (let [condition (fn condition
                        ([p1__15990#]
                          (cond
                            (not (valid-db-name? new_name)) {:invalid-db-name new_name}
                            (not (get p1__15990# db_name)) {:does-not-exist db_name}
                            (get p1__15990# new_name) (do {:exists db_name}))))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__15994 ([p1__15991#] (rename p1__15991# db_name new_name))))]
        (if (update-succeeded? resp) {:renamed-to new_name} resp))))
  (defn delete
    ([catalog db_name]
      (let [temp__5802__auto__ (db-name->db-id catalog db_name)]
        (if temp__5802__auto__
          (let [dbid temp__5802__auto__]
            (dissoc (update catalog :datomic/deleted (fnil conj #{}) dbid) db_name))
          catalog))))
  (defn delete-database
    ([cluster db_name]
      (let [condition (fn condition
                        ([p1__15999#]
                          (when (not (get p1__15999# db_name)) {:does-not-exist db_name})))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__16003 ([p1__16000#] (delete p1__16000# db_name))))]
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
                        ([p1__16006#]
                          (cond
                            (get p1__16006# db_name) {:name-already-taken db_name}
                            (not (get-in p1__16006# [:datomic/deleted db_id])) (do
                                                                                 {:id-not-deleted
                                                                                  db_id}))))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__16010 ([p1__16007#] (add-database p1__16007# db_name db_id))))]
        (if (update-succeeded? resp) {:undeleted db_name, :db-id db_id} resp))))
  (defn remove-deleted ([catalog db_id] (update catalog :datomic/deleted (fnil disj #{}) db_id)))
  (defn remove-deleted-database
    ([cluster db_id]
      (let [condition (fn condition
                        ([p1__16014#]
                          (when-not (contains? (get p1__16014# :datomic/deleted) db_id)
                            {:does-not-exist db_id})))
            resp (update-catalog
                   cluster
                   condition
                   (fn fn__16018 ([p1__16015#] (remove-deleted p1__16015# db_id))))]
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
      (let [temp__5802__auto__ (get-catalog cluster)]
        (if temp__5802__auto__
          (let [catalog temp__5802__auto__] (deleted? catalog db_id))
          (do (throw (java.lang.RuntimeException. "No catalog")) nil)))))
  (defn parse-db-conf ([db_conf] db_conf)))
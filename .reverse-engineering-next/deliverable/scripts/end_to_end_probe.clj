(ns parity-probe
  (:require [clojure.string :as str]
            [datomic.api :as d]))

(def uri "datomic:mem://datomic-rev-parity-probe")

(defn fail! [message data]
  (throw (ex-info message data)))

(defn ensure! [pred message data]
  (when-not pred
    (fail! message data)))

(defn canonical-compare [a b]
  (compare (pr-str a) (pr-str b)))

(declare canonical)

(defn canonical [x]
  (cond
    (instance? java.util.Date x) :generated-instant
    (map? x) (into (sorted-map-by canonical-compare)
                   (map (fn [[k v]] [(canonical k) (canonical v)]))
                   x)
    (set? x) (vec (sort-by pr-str (map canonical x)))
    (sequential? x) (mapv canonical x)
    (instance? java.lang.Iterable x) (mapv canonical (iterator-seq (.iterator ^Iterable x)))
    :else x))

(defn tx-id [report]
  (:tx (first (:tx-data report))))

(defn entity-id-map [db]
  (into {}
        (map (fn [[e person-id]] [e [:person person-id]]))
        (d/q '[:find ?e ?person-id
               :where [?e :person/id ?person-id]]
             db)))

(defn maybe-ident [db x]
  (when (integer? x)
    (try
      (d/ident db x)
      (catch Throwable _ nil))))

(defn normalize-id [db entity-ids tx-ids x]
  (if-not (integer? x)
    (canonical x)
    (or (get entity-ids x)
        (when-let [tx-label (get tx-ids x)] [:tx tx-label])
        (maybe-ident db x)
        [:unmapped-id])))

(defn normalize-datom [db entity-ids tx-ids datom]
  (let [a (:a datom)
        value-type (:db/valueType (d/entity db a))]
    {:e (normalize-id db entity-ids tx-ids (:e datom))
     :a (or (d/ident db a) [:unknown-attribute])
     :v (if (= :db.type/ref value-type)
          (normalize-id db entity-ids tx-ids (:v datom))
          (canonical (:v datom)))
     :tx (normalize-id db entity-ids tx-ids (:tx datom))
     :added (boolean (:added datom))}))

(defn normalize-datoms [db entity-ids tx-ids datoms]
  (mapv #(normalize-datom db entity-ids tx-ids %) datoms))

(defn person-view [db person-id]
  (let [entity (d/entity db [:person/id person-id])]
    {:person/id (:person/id entity)
     :person/name (:person/name entity)
     :person/age (:person/age entity)
     :person/active (:person/active entity)
     :person/tags (vec (sort (:person/tags entity)))
     :person/friends (vec (sort (map :person/id (:person/friend entity))))
     :reverse-friends (vec (sort (map :person/id (:person/_friend entity))))}))

(def schema-tx
  [{:db/id (d/tempid :db.part/db)
    :db/ident :person/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db/doc "Stable person identifier"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :person/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Person name"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :person/age
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/index true
    :db/doc "Person age"
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :person/active
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :person/tags
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :person/friend
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}])

(defn seed-tx []
  (let [alice (d/tempid :db.part/user)
        bob (d/tempid :db.part/user)
        cara (d/tempid :db.part/user)]
    [{:db/id alice
      :person/id "alice"
      :person/name "Alice"
      :person/age 30
      :person/active true
      :person/tags #{:tag/clojure :tag/engineer}
      :person/friend [bob cara]}
     {:db/id bob
      :person/id "bob"
      :person/name "Bob"
      :person/age 40
      :person/active true
      :person/tags #{:tag/ops}
      :person/friend [cara]}
     {:db/id cara
      :person/id "cara"
      :person/name "Cara"
      :person/age 25
      :person/active false
      :person/tags #{:tag/art}}]))

(def friend-rules
  '[[(linked ?a ?b) [?a :person/friend ?b]]
    [(linked ?a ?b) [?b :person/friend ?a]]])

(defn run-probe []
  (try
    (d/delete-database uri)
    (catch Throwable _ nil))
  (let [created? (d/create-database uri)
        conn (d/connect uri)
        same-conn? (identical? conn (d/connect uri))
        schema-report @(d/transact conn schema-tx)
        db-schema (d/db conn)
        seed-report @(d/transact conn (seed-tx))
        db-seed (d/db conn)
        seed-t (d/basis-t db-seed)
        update-report @(d/transact conn
                                   [[:db.fn/cas [:person/id "alice"] :person/age 30 31]
                                    [:db/add [:person/id "bob"] :person/tags :tag/clojure]
                                    [:db/retract [:person/id "cara"] :person/active false]
                                    [:db/add [:person/id "cara"] :person/active true]])
        db-now (d/db conn)
        tx-ids {(tx-id schema-report) :schema
                (tx-id seed-report) :seed
                (tx-id update-report) :update}
        entity-ids (entity-id-map db-now)
        alice-eid (d/entid db-now [:person/id "alice"])
        as-of-db (d/as-of db-now seed-t)
        since-db (d/since db-now seed-t)
        with-report (d/with db-now
                            [[:db/add [:person/id "alice"] :person/tags :tag/virtual]
                             [:db/retract [:person/id "bob"] :person/tags :tag/ops]])
        with-db (:db-after with-report)
        with-tx (tx-id with-report)
        tx-ids (assoc tx-ids with-tx :with)
        schema-entity (d/entity db-schema :person/id)
        pull-alice (d/pull db-now
                           '[:person/id :person/name :person/age :person/active
                             :person/tags
                             {:person/friend [:person/id :person/name]}]
                           [:person/id "alice"])
        pull-many (d/pull-many db-now
                               '[:person/id :person/name :person/age]
                               [[:person/id "cara"]
                                [:person/id "alice"]
                                [:person/id "bob"]])
        result
        {:lifecycle
         {:created? created?
          :same-connection-instance? same-conn?
          :schema-attribute-count
          (count (d/q '[:find [?a ...]
                        :where [?a :db/ident ?ident]
                               [(namespace ?ident) ?ns]
                               [(= ?ns "person")]]
                      db-schema))
          :basis-order (< seed-t (d/basis-t db-now))}

         :schema
         {:ident (:db/ident schema-entity)
          :value-type (:db/valueType schema-entity)
          :cardinality (:db/cardinality schema-entity)
          :unique (:db/unique schema-entity)
          :indexed? (boolean (:db/index schema-entity))
          :doc (:db/doc schema-entity)}

         :queries
         {:all (vec (sort (d/q '[:find ?id ?name ?age ?active
                                 :where [?e :person/id ?id]
                                        [?e :person/name ?name]
                                        [?e :person/age ?age]
                                        [?e :person/active ?active]]
                               db-now)))
          :aggregate (first (d/q '[:find (count ?e) (sum ?age) (min ?age) (max ?age)
                                   :where [?e :person/age ?age]]
                                 db-now))
          :input (vec (sort (d/q '[:find [?id ...]
                                   :in $ ?minimum
                                   :where [?e :person/id ?id]
                                          [?e :person/age ?age]
                                          [(>= ?age ?minimum)]]
                                 db-now 30)))
          :scalar (d/q '[:find ?name .
                         :in $ ?id
                         :where [?e :person/id ?id]
                                [?e :person/name ?name]]
                       db-now "alice")
          :rules (vec (sort (d/q '[:find [?friend-id ...]
                                   :in $ % ?person-id
                                   :where [?e :person/id ?person-id]
                                          (linked ?e ?friend)
                                          [?friend :person/id ?friend-id]]
                                 db-now friend-rules "cara")))}

         :entity
         {:alice (person-view db-now "alice")
          :bob (person-view db-now "bob")
          :cara (person-view db-now "cara")
          :touched-keys (vec (sort (keys (d/touch (d/entity db-now [:person/id "alice"])))))
          :entity-db-basis-current?
          (= (d/basis-t db-now)
             (d/basis-t (d/entity-db (d/entity db-now [:person/id "alice"]))))}

         :pull
         {:alice (update pull-alice :person/friend
                         #(vec (sort-by :person/id %)))
          :many pull-many}

         :indexes
         {:eavt (normalize-datoms db-now entity-ids tx-ids
                                  (d/datoms db-now :eavt alice-eid))
          :avet-id (normalize-datoms db-now entity-ids tx-ids
                                     (d/datoms db-now :avet :person/id))
          :age-range (normalize-datoms db-now entity-ids tx-ids
                                       (d/index-range db-now :person/age 26 41))
          :seek-age (->> (d/seek-datoms db-now :aevt :person/age)
                         (take 3)
                         (normalize-datoms db-now entity-ids tx-ids))}

         :time
         {:as-of-ages (vec (sort (d/q '[:find ?id ?age
                                        :where [?e :person/id ?id]
                                               [?e :person/age ?age]]
                                      as-of-db)))
          :since-ages (->> (d/q '[:find ?e ?age
                                  :where [?e :person/age ?age]]
                                since-db)
                           (map (fn [[e age]] [(get entity-ids e) age]))
                           sort
                           vec)
          :history-alice-age
          (normalize-datoms db-now entity-ids tx-ids
                            (d/datoms (d/history db-now) :eavt alice-eid :person/age))
          :history-cara-active
          (normalize-datoms db-now entity-ids tx-ids
                            (d/datoms (d/history db-now) :eavt
                                      (d/entid db-now [:person/id "cara"])
                                      :person/active))}

         :with
         {:after-alice-tags (vec (sort (:person/tags
                                        (d/entity with-db [:person/id "alice"]))))
          :after-bob-tags (vec (sort (:person/tags
                                      (d/entity with-db [:person/id "bob"]))))
          :connection-alice-tags (vec (sort (:person/tags
                                             (d/entity (d/db conn)
                                                       [:person/id "alice"]))))
          :connection-bob-tags (vec (sort (:person/tags
                                           (d/entity (d/db conn)
                                                     [:person/id "bob"]))))
          :tx-data (normalize-datoms with-db entity-ids tx-ids (:tx-data with-report))}

         :transactions
         {:seed (normalize-datoms db-now entity-ids tx-ids (:tx-data seed-report))
          :update (normalize-datoms db-now entity-ids tx-ids (:tx-data update-report))}}]
    (ensure! (= 6 (get-in result [:lifecycle :schema-attribute-count]))
             "schema did not install" result)
    (ensure! (= 31 (get-in result [:entity :alice :person/age]))
             "CAS update did not apply" result)
    (ensure! (= [["alice" 30] ["bob" 40] ["cara" 25]]
                (get-in result [:time :as-of-ages]))
             "as-of view is incorrect" result)
    (ensure! (= [[[:person "alice"] 31]] (get-in result [:time :since-ages]))
             "since view is incorrect" result)
    (ensure! (some #{:tag/virtual} (get-in result [:with :after-alice-tags]))
             "with did not apply hypothetical assertion" result)
    (ensure! (not (some #{:tag/virtual} (get-in result [:with :connection-alice-tags])))
             "with mutated connection" result)
    (ensure! (d/delete-database uri) "database deletion failed" {})
    (canonical result)))

(try
  (println "PARITY-RESULT" (pr-str (run-probe)))
  (shutdown-agents)
  (catch Throwable t
    (binding [*out* *err*]
      (println "PARITY-ERROR" (.getName (class t)) (.getMessage t))
      (when-let [data (ex-data t)]
        (println (pr-str (canonical data))))
      (.printStackTrace t))
    (try (d/delete-database uri) (catch Throwable _ nil))
    (shutdown-agents)
    (System/exit 1)))

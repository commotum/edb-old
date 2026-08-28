(ns stage2.peer-workload
  "Deterministic external-SQL workload for the recovered peer candidate.

  Modes: seed, augment, phase-three (alias fault-seed), snapshot, and
  post-restore. Invoke through clojure.main -m stage2.peer-workload."
  (:require [clojure.string :as str]
            [datomic.api :as d])
  (:import [datomic Database Datom]
           [java.math BigInteger]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(set! *warn-on-reflection* true)

(def ^:private result-prefix "STAGE2-PEER-RESULT ")
(def ^:private error-prefix "STAGE2-PEER-ERROR ")

(def ^:private stage2-attributes
  [:stage2/id :stage2/value :stage2/ordinal :stage2/tags])

(defn- fail!
  [message data]
  (throw (ex-info message (assoc data ::error :workload-failed))))

(defn- ensure!
  [pred message data]
  (when-not pred
    (fail! message data)))

(defn- sha256
  [value]
  (let [bytes (.digest (MessageDigest/getInstance "SHA-256")
                       (.getBytes (pr-str value) StandardCharsets/UTF_8))]
    (format "%064x" (BigInteger. 1 bytes))))

(defn- phase-one-tags
  [n]
  (->> (cond-> [:stage2/all]
         (even? n) (conj :stage2/even)
         (zero? (mod n 5)) (conj :stage2/fifth))
       sort
       vec))

(defn- schema-tx
  []
  [{:db/ident :stage2/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage2/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/ident :stage2/ordinal
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/index true
    :db.install/_attribute :db.part/db}
   {:db/ident :stage2/tags
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}])

(defn- phase-one-entities
  []
  (mapv (fn [n]
          {:stage2/id (format "item-%03d" n)
           :stage2/value (str "value-" (* n n))
           :stage2/ordinal n
           :stage2/tags (phase-one-tags n)})
        (range 64)))

(defn- phase-two-tx
  []
  (vec
    (concat
      (map (fn [n]
             {:stage2/id (format "item-%03d" n)
              :stage2/value (str "phase-two-" (+ 1000 n))
              :stage2/tags [:stage2/updated]})
           (range 16))
      (map (fn [n]
             [:db/retract
              [:stage2/id (format "item-%03d" n)]
              :stage2/tags
              :stage2/even])
           (range 0 16 2))
      (map (fn [n]
             {:stage2/id (format "item-%03d" n)
              :stage2/value (str "phase-two-new-" (* n 17))
              :stage2/ordinal n
              :stage2/tags [:stage2/all :stage2/new]})
           (range 64 96)))))

(defn- phase-three-tx
  []
  (vec
    (concat
      (map (fn [n]
             {:stage2/id (format "item-%03d" n)
              :stage2/value (str "phase-three-" (+ 3000 n))
              :stage2/tags [:stage2/phase-three]})
           (range 32 64))
      (map (fn [n]
             {:stage2/id (format "item-%03d" n)
              :stage2/value (str "phase-three-new-" (* n 19))
              :stage2/ordinal n
              :stage2/tags [:stage2/all :stage2/new :stage2/phase-three]})
           (range 96 128)))))

(defn- phase-two-tags
  [n]
  (cond
    (< n 16)
    (-> (set (phase-one-tags n))
        (conj :stage2/updated)
        (cond-> (even? n) (disj :stage2/even))
        sort
        vec)

    (< n 64)
    (phase-one-tags n)

    :else
    [:stage2/all :stage2/new]))

(defn- expected-phase-one-rows
  []
  (mapv (fn [n]
          [(format "item-%03d" n)
           (str "value-" (* n n))
           n
           (phase-one-tags n)])
        (range 64)))

(defn- expected-phase-two-rows
  []
  (mapv (fn [n]
          [(format "item-%03d" n)
           (cond
             (< n 16) (str "phase-two-" (+ 1000 n))
             (< n 64) (str "value-" (* n n))
             :else (str "phase-two-new-" (* n 17)))
           n
           (phase-two-tags n)])
        (range 96)))

(defn- expected-phase-three-rows
  []
  (mapv (fn [n]
          [(format "item-%03d" n)
           (cond
             (< n 16) (str "phase-two-" (+ 1000 n))
             (< n 32) (str "value-" (* n n))
             (< n 64) (str "phase-three-" (+ 3000 n))
             (< n 96) (str "phase-two-new-" (* n 17))
             :else (str "phase-three-new-" (* n 19)))
           n
           (cond
             (< n 32) (phase-two-tags n)
             (< n 64) (-> (set (phase-two-tags n))
                              (conj :stage2/phase-three)
                              sort
                              vec)
             (< n 96) (phase-two-tags n)
             :else [:stage2/all :stage2/new :stage2/phase-three])])
        (range 128)))

(defn- rows
  [db]
  (->> (d/q '[:find ?e ?id ?value ?ordinal ?tag
              :where
              [?e :stage2/id ?id]
              [?e :stage2/value ?value]
              [?e :stage2/ordinal ?ordinal]
              [?e :stage2/tags ?tag]]
            db)
       (group-by first)
       vals
       (map (fn [entity-rows]
              (let [[e id value ordinal] (take 4 (first entity-rows))]
                [e id value ordinal (->> entity-rows (map #(nth % 4)) sort vec)])))
       (sort-by second)
       vec))

(defn- relevant-datoms
  [db]
  (->> stage2-attributes
       (mapcat (fn [attribute]
                 (map (fn [^Datom datom]
                        [(.e datom)
                         attribute
                         (.v datom)
                         (.tx datom)
                         (.added datom)])
                      (d/datoms db :aevt attribute))))
       (sort-by pr-str)
       vec))

(defn- semantic-rows
  [db]
  (mapv (fn [row] (vec (rest row))) (rows db)))

(defn- ensure-phase!
  [db phase expected]
  (let [actual (semantic-rows db)]
    (ensure! (= expected actual)
             "Stage 2 workload phase differs"
             {:phase phase
              :expected-row-count (count expected)
              :actual-row-count (count actual)
              :expected-sha256 (sha256 expected)
              :actual-sha256 (sha256 actual)}))
  db)

(defn- ensure-t-advance!
  [before-db after-db phase]
  (let [before-t (d/basis-t before-db)
        after-t (d/basis-t after-db)]
    (ensure! (> after-t before-t)
             "Stage 2 transaction did not advance t"
             {:phase phase
              :before-t before-t
              :after-t after-t
              :t-delta (- after-t before-t)})))

(defn- snapshot
  [^Database db]
  (let [rows (rows db)
        history (relevant-datoms (d/history db))
        datoms (relevant-datoms db)
        logical [rows history datoms]]
    (sorted-map
      :as-of-t (d/as-of-t db)
      :basis-t (d/basis-t db)
      :database-id (.id db)
      :datoms-sha256 (sha256 datoms)
      :history-sha256 (sha256 history)
      :logical-sha256 (sha256 logical)
      :row-count (count rows)
      :rows-sha256 (sha256 rows))))

(defn- ensure-current-snapshot!
  [value phase]
  (ensure! (nil? (:as-of-t value))
           "Current database snapshot unexpectedly has an as-of basis"
           {:phase phase :snapshot value})
  (ensure! (and (string? (:database-id value))
                (not (str/blank? (:database-id value))))
           "Current database snapshot has no database identity"
           {:phase phase :snapshot value})
  (ensure! (and (integer? (:basis-t value))
                (not (neg? (:basis-t value))))
           "Current database snapshot has an invalid basis"
           {:phase phase :snapshot value})
  value)

(defn- ensure-restored-identity!
  [value expected-database-id expected-basis-t phase]
  (ensure-current-snapshot! value phase)
  (ensure! (= expected-database-id (:database-id value))
           "Restored database identity differs from the source database"
           {:phase phase
            :expected-database-id expected-database-id
            :actual-database-id (:database-id value)})
  (ensure! (= expected-basis-t (:basis-t value))
           "Restored database basis differs from the requested restore point"
           {:phase phase
            :expected-basis-t expected-basis-t
            :actual-basis-t (:basis-t value)})
  value)

(defn- seed!
  [uri expected-created]
  (let [created? (d/create-database uri)]
    (ensure! (= expected-created created?)
             "Unexpected create-database result"
             {:expected expected-created :actual created?})
    (let [conn (d/connect uri)]
      (try
        @(d/transact conn (schema-tx))
        (let [tx-result @(d/transact conn (phase-one-entities))
              before-db (:db-before tx-result)
              after-db (:db-after tx-result)
              _ (ensure-t-advance! before-db after-db :phase-one)
              _ (ensure-phase! after-db :phase-one (expected-phase-one-rows))
              result (ensure-current-snapshot! (snapshot after-db) :phase-one)]
          (ensure! (= 64 (:row-count result))
                   "Phase-one row count differs"
                   result)
          result)
        (finally
          (d/release conn))))))

(defn- augment!
  [uri]
  (let [conn (d/connect uri)]
    (try
      (let [before-db (ensure-phase!
                        (d/db conn) :phase-one (expected-phase-one-rows))
            tx-result @(d/transact conn (phase-two-tx))
            after-db (:db-after tx-result)
            _ (ensure-t-advance! before-db after-db :phase-two)
            _ (ensure-phase! after-db :phase-two (expected-phase-two-rows))
            result (ensure-current-snapshot! (snapshot after-db) :phase-two)]
        (ensure! (= 96 (:row-count result))
                 "Phase-two row count differs"
                 result)
        result)
      (finally
        (d/release conn)))))

(defn- phase-three!
  [uri]
  (let [conn (d/connect uri)]
    (try
      (let [before-db (ensure-phase!
                        (d/db conn) :phase-two (expected-phase-two-rows))
            tx-result @(d/transact conn (phase-three-tx))
            after-db (:db-after tx-result)
            _ (ensure-t-advance! before-db after-db :phase-three)
            _ (ensure-phase! after-db :phase-three (expected-phase-three-rows))
            result (ensure-current-snapshot! (snapshot after-db) :phase-three)]
        (ensure! (= 128 (:row-count result))
                 "Phase-three row count differs"
                 result)
        result)
      (finally
        (d/release conn)))))

(defn- read-snapshot
  [uri t]
  (let [conn (d/connect uri)]
    (try
      (let [db (d/db conn)
            db (if t (d/as-of db t) db)]
        (snapshot db))
      (finally
        (d/release conn)))))

(defn- post-restore!
  [uri expected-logical-sha expected-database-id expected-basis-t]
  (let [conn (d/connect uri)]
    (try
      (let [before-db (d/db conn)
            before-rows (semantic-rows before-db)
            before (ensure-restored-identity!
                     (snapshot before-db)
                     expected-database-id
                     expected-basis-t
                     :post-restore-before-write)]
        (ensure! (= expected-logical-sha (:logical-sha256 before))
                 "Restored logical snapshot differs"
                 {:expected expected-logical-sha :actual before})
        (ensure! (not-any? #(= "post-restore-sentinel" (first %)) before-rows)
                 "Post-restore sentinel already exists"
                 {:row-count (count before-rows)})
        (let [tx-result @(d/transact conn [{:stage2/id "post-restore-sentinel"
                                           :stage2/value "writable-after-restore"
                                           :stage2/ordinal 1000000
                                           :stage2/tags [:stage2/all
                                                        :stage2/post-restore]}])
              after-db (:db-after tx-result)
              after-rows (semantic-rows after-db)
              expected-sentinel ["post-restore-sentinel"
                                 "writable-after-restore"
                                 1000000
                                 [:stage2/all :stage2/post-restore]]
              without-sentinel (vec (remove #(= "post-restore-sentinel" (first %))
                                            after-rows))
              after (ensure-current-snapshot!
                     (snapshot after-db)
                     :post-restore-after-write)]
          (ensure-t-advance! before-db after-db :post-restore)
          (ensure! (= (inc (count before-rows)) (count after-rows))
                   "Post-restore write did not add exactly one row"
                   {:before-row-count (count before-rows)
                    :after-row-count (count after-rows)})
          (ensure! (= [expected-sentinel]
                      (vec (filter #(= "post-restore-sentinel" (first %)) after-rows)))
                   "Post-restore sentinel differs"
                   {})
          (ensure! (= before-rows without-sentinel)
                   "Post-restore write changed pre-existing workload rows"
                   {:before-sha256 (sha256 before-rows)
                    :after-without-sentinel-sha256 (sha256 without-sentinel)})
          (ensure! (= (:database-id before) (:database-id after))
                   "Database identity changed during post-restore write"
                   {})
          (ensure! (not= (:logical-sha256 before) (:logical-sha256 after))
                   "Post-restore write did not change the logical snapshot"
                   {})
          {:before before
           :restore-identity {:as-of-t nil
                              :basis-t expected-basis-t
                              :database-id expected-database-id
                              :exact? true}
           :after after}))
      (finally
        (d/release conn)))))

(defn- parse-t
  [value]
  (when value (Long/parseLong value)))

(defn- parse-basis-t
  [value]
  (let [basis-t (parse-t value)]
    (ensure! (and (some? basis-t) (not (neg? basis-t)))
             "EXPECTED_BASIS_T must be a non-negative integer"
             {:expected-basis-t value})
    basis-t))

(defn- parse-exact-boolean
  [value label]
  (ensure! (contains? #{"true" "false"} value)
           (str label " must be exactly true or false")
           {:label label})
  (= "true" value))

(defn- parse-command
  [args]
  (let [[mode uri arg] args
        argument-count (count args)]
    (ensure! (and mode uri (not (str/blank? uri)))
             "Usage: peer_workload.clj MODE URI [ARG]"
             {:mode mode
              :argument-count argument-count
              :uri-present? (boolean (and uri (not (str/blank? uri))))})
    (case mode
      "seed"
      (do
        (ensure! (= 3 argument-count)
                 "seed requires exactly MODE URI EXPECTED_CREATED"
                 {:argument-count argument-count})
        {:mode mode :uri uri :arg (parse-exact-boolean arg "EXPECTED_CREATED")})

      "augment"
      (do
        (ensure! (= 2 argument-count)
                 "augment requires exactly MODE URI"
                 {:argument-count argument-count})
        {:mode mode :uri uri})

      ("phase-three" "fault-seed")
      (do
        (ensure! (= 2 argument-count)
                 "phase-three/fault-seed requires exactly MODE URI"
                 {:argument-count argument-count})
        {:mode mode :uri uri})

      "snapshot"
      (do
        (ensure! (or (= 2 argument-count) (= 3 argument-count))
                 "snapshot requires MODE URI [T]"
                 {:argument-count argument-count})
        {:mode mode :uri uri :arg (parse-t arg)})

      "post-restore"
      (let [[_ _ expected-logical-sha expected-database-id expected-basis-t]
            args]
        (ensure! (= 5 argument-count)
                 (str "post-restore requires exactly MODE URI "
                      "EXPECTED_LOGICAL_SHA256 EXPECTED_DATABASE_ID "
                      "EXPECTED_BASIS_T")
                 {:argument-count argument-count})
        (ensure! (boolean (re-matches #"[0-9a-f]{64}" expected-logical-sha))
                 "EXPECTED_LOGICAL_SHA256 must be a lowercase SHA-256 digest"
                 {})
        (ensure! (and (string? expected-database-id)
                      (not (str/blank? expected-database-id)))
                 "EXPECTED_DATABASE_ID must not be blank"
                 {})
        {:mode mode
         :uri uri
         :arg {:expected-basis-t (parse-basis-t expected-basis-t)
               :expected-database-id expected-database-id
               :expected-logical-sha expected-logical-sha}})

      (fail! "Unknown Stage 2 peer workload mode" {:mode mode}))))

(defn- run-command
  [{:keys [mode uri arg]}]
  (case mode
    "seed" (seed! uri arg)
    "augment" (augment! uri)
    "phase-three" (phase-three! uri)
    "fault-seed" (phase-three! uri)
    "snapshot" (read-snapshot uri arg)
    "post-restore" (post-restore! uri
                                  (:expected-logical-sha arg)
                                  (:expected-database-id arg)
                                  (:expected-basis-t arg))))

(defn- cause-chain
  [^Throwable throwable]
  (take-while some? (iterate #(.getCause ^Throwable %) throwable)))

(defn- sanitized-error
  [mode ^Throwable throwable]
  {:mode mode
   :exception-class (.getName (class throwable))
   :cause-classes (mapv #(.getName (class %)) (cause-chain throwable))
   :db-error (some #(some-> % ex-data :db/error) (cause-chain throwable))
   :workload-error (some #(some-> % ex-data ::error) (cause-chain throwable))})

(defn- capture
  [f]
  (try
    {:returned (f)}
    (catch Throwable throwable
      {:thrown throwable})))

(defn- shutdown!
  []
  (try
    (Thread/sleep 250)
    (catch InterruptedException _
      (.interrupt (Thread/currentThread))))
  (try
    (d/shutdown false)
    (finally
      (shutdown-agents)))
  :completed)

(defn- main-error
  [mode operation cleanup]
  {:mode mode
   :operation-error (when-let [throwable (:thrown operation)]
                      (sanitized-error mode throwable))
   :cleanup-error (when-let [throwable (:thrown cleanup)]
                    (sanitized-error mode throwable))})

(defn -main
  [& args]
  (let [mode (first args)
        operation (capture
                   #(let [command (parse-command args)]
                      {:command command :result (run-command command)}))
        ;; Allow release cleanup to finish before shutting down process-global
        ;; peer services. The result marker is deliberately delayed until this
        ;; cleanup succeeds.
        cleanup (capture shutdown!)
        operation-succeeded? (contains? operation :returned)
        cleanup-succeeded? (contains? cleanup :returned)
        exit-code (cond
                    (and operation-succeeded? cleanup-succeeded?) 0
                    (not cleanup-succeeded?) 2
                    :else 1)]
    (if (zero? exit-code)
      (let [{:keys [command result]} (:returned operation)]
        (println (str result-prefix
                      (pr-str {:mode (:mode command) :result result})))
        (flush))
      (binding [*out* *err*]
        (println (str error-prefix (pr-str (main-error mode operation cleanup))))
        (flush)))
    (when-not (zero? exit-code)
      (System/exit exit-code))))

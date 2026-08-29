(ns stage4.storage-cas-probe
  (:require [datomic.cluster :as cluster]
            [datomic.index :as index]
            [datomic.kv-cluster :as kv-cluster]
            [datomic.kv-sql :as kv-sql]
            [datomic.log :as log])
  (:import [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [java.sql DriverManager]
           [java.util.concurrent Executors]))

(defn fail! [message data]
  (throw (ex-info message data)))

(defn require-equal! [label expected actual]
  (when-not (= expected actual)
    (fail! (str label " differs") {:expected expected :actual actual})))

(defn utf8-buffer [value]
  (ByteBuffer/wrap (.getBytes ^String value StandardCharsets/UTF_8)))

(defn buffer->bytes [^ByteBuffer value]
  (let [copy (.duplicate value)
        bytes (byte-array (.remaining copy))]
    (.get copy bytes)
    bytes))

(defn bytes->hex [value]
  (when value
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) value))))

(defn buffer->hex [value]
  (bytes->hex (buffer->bytes value)))

(defn pod-state [pod]
  {:rev (:rev pod)
   :etag (:etag pod)
   :root (:d/r pod)
   :value-hex (buffer->hex (:buf pod))})

(defn sql-metrics [factory]
  (with-open [connection (factory)
              statement (.prepareStatement
                          connection
                          (str "select count(*), "
                               "count(*) filter (where rev is not null), "
                               "coalesce(sum(octet_length(val)), 0) "
                               "from public.datomic_kvs"))
              result-set (.executeQuery statement)]
    (when-not (.next result-set)
      (fail! "PostgreSQL metrics query returned no row" {}))
    {:rows (.getLong result-set 1)
     :revisioned-rows (.getLong result-set 2)
     :value-bytes (.getLong result-set 3)}))

(defn authoritative-state [factory ref-key pod-key]
  (with-open [connection (factory)
              statement (.prepareStatement
                          connection
                          (str "select id, rev, map, val from public.datomic_kvs "
                               "where id in (?, ?) order by id"))]
    (.setString statement 1 ref-key)
    (.setString statement 2 pod-key)
    (with-open [result-set (.executeQuery statement)]
      (loop [rows []]
        (if (.next result-set)
          (recur (conj rows
                       {:id (.getString result-set "id")
                        :rev (.getLong result-set "rev")
                        :map (.getString result-set "map")
                        :val-hex (bytes->hex (.getBytes result-set "val"))}))
          rows)))))

(defn run-probe! [jdbc-url user password expected-before]
  (Class/forName "org.postgresql.Driver")
  (let [factory #(DriverManager/getConnection jdbc-url user password)
        before (sql-metrics factory)
        kvs (kv-sql/from-spec {:factory factory})
        executor (Executors/newFixedThreadPool 2)
        invoke-once (fn [& args] ((last args)))
        cluster (kv-cluster/->KVCluster
                 kvs
                 {:db "stage4-storage-cas"}
                 executor
                 invoke-once
                 invoke-once
                 invoke-once
                 :kvc
                 :kvc-ns
                 (fn [_ _] nil))
        root-a "00000000-0000-0000-0000-00000000000a"
        root-b "00000000-0000-0000-0000-00000000000b"
        root-loser "00000000-0000-0000-0000-00000000000c"
        ref-key (index/index-ref-key-name cluster)
        pod-key (log/tail-pod-key cluster)]
    (try
      (require-equal! "initial SQL row count" expected-before (:rows before))
      (require-equal! "initial root ref" nil @(cluster/get-ref cluster ref-key))

      (doseq [[root value] [[root-a "root-a-bytes"]
                            [root-b "root-b-bytes"]
                            [root-loser "root-loser-bytes"]]]
        (require-equal! "immutable root value creation"
                        :created
                        @(cluster/create-val cluster root (utf8-buffer value)))
        (require-equal! "immutable root value round trip"
                        (bytes->hex (.getBytes ^String value StandardCharsets/UTF_8))
                        (buffer->hex (:buf @(cluster/get-val cluster root)))))

      (require-equal! "root ref creation"
                      :ok
                      @(cluster/set-ref cluster ref-key 0 root-a))
      (require-equal! "root ref exact replay"
                      :ok
                      @(cluster/set-ref cluster ref-key 0 root-a))
      (require-equal! "root ref conflicting duplicate creation"
                      :conflict
                      @(cluster/set-ref cluster ref-key 0 root-loser))
      (require-equal! "root ref revision advance"
                      :ok
                      @(cluster/set-ref cluster ref-key 1 root-b))
      (require-equal! "winning root ref state"
                      {:key root-b :rev 1}
                      @(cluster/get-ref cluster ref-key))

      (let [created (log/write-tail-descriptor
                      cluster
                      {:rev 0 :etag nil :d/r root-a :d/l 3}
                      (.duplicate ^ByteBuffer log/BEGIN_OPEN_LIST))]
        (require-equal! "initial log-root revision" 0 (:rev created)))
      (let [accepted (log/write-tail-descriptor
                       cluster
                       {:rev 1 :etag nil :d/r root-b :d/l 3}
                       (.duplicate ^ByteBuffer log/BEGIN_OPEN_LIST))
            accepted-state (pod-state @(cluster/get-pod cluster pod-key))
            winner-sql-state (authoritative-state factory ref-key pod-key)
            ref-rejected @(cluster/set-ref cluster ref-key 1 root-loser)
            log-rejected (log/write-tail-descriptor
                           cluster
                           {:rev 1 :etag nil :d/r root-loser :d/l 3}
                           (.duplicate ^ByteBuffer log/BEGIN_OPEN_LIST))
            final-state (pod-state @(cluster/get-pod cluster pod-key))]
        (require-equal! "accepted log-root revision" 1 (:rev accepted))
        (require-equal! "accepted log-root state"
                        {:rev 1
                         :root root-b
                         :value-hex (buffer->hex log/BEGIN_OPEN_LIST)}
                        (dissoc accepted-state :etag))
        (require-equal! "root ref stale revision rejection"
                        :conflict
                        ref-rejected)
        (require-equal! "stale log-root rejection"
                        nil
                        log-rejected)
        (require-equal! "winning log-root state after rejection"
                        accepted-state
                        final-state)
        (require-equal! "authoritative PostgreSQL rows after rejection"
                        winner-sql-state
                        (authoritative-state factory ref-key pod-key)))

      (let [after (sql-metrics factory)]
        (require-equal! "SQL rows added" 8 (- (:rows after) (:rows before)))
        (require-equal! "revisioned SQL rows" 2 (:revisioned-rows after))
        (when-not (pos? (:value-bytes after))
          (fail! "PostgreSQL retained no pod value bytes" {:metrics after}))
        (println
          "STAGE4-STORAGE-CAS-RESULT"
          (pr-str
            (sorted-map
              :idempotent-ref-replay :ok
              :log-root-conflict :rejected
              :log-root-winner :unchanged
              :ref-conflicting-create :rejected
              :ref-stale-revision :rejected
              :revisioned-rows (:revisioned-rows after)
              :rows-added (- (:rows after) (:rows before))
              :sql-authoritative-rows :unchanged
              :status :passed
              :value-bytes-positive true))))
      (finally
        (cluster/close cluster)
        (shutdown-agents)))))

(let [[jdbc-url user password expected-before & extra] *command-line-args*]
  (when (or (some nil? [jdbc-url user password expected-before]) (seq extra))
    (fail! "usage: storage_cas_probe.clj JDBC_URL USER PASSWORD EXPECTED_BEFORE" {}))
  (run-probe! jdbc-url user password (Long/parseLong expected-before)))

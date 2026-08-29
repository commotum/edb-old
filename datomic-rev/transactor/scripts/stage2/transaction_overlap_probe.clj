(ns datomic-rev.stage2-transaction-overlap-probe
  (:require [clojure.java.io :as jio]
            [datomic.cache :as cache]
            [datomic.common :as common]
            [datomic.db :as db]
            [datomic.monitor :as monitor]
            [datomic.slf4j :as logger]
            [datomic.transaction :as transaction])
  (:import [ch.qos.logback.classic Level Logger]
           [ch.qos.logback.core.read ListAppender]
           [datomic.db IDatumImpl]
           [datomic.impl.db IDatum]
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.util UUID]
           [org.fressian Reader Writer]
           [org.slf4j LoggerFactory]))

(defn fail! [message data]
  (throw (ex-info message data)))

(defn require-equal! [label expected actual]
  (when-not (= expected actual)
    (fail! "transaction overlap probe mismatch"
           {:label label :expected expected :actual actual})))

(defn require-true! [label value]
  (when-not value
    (fail! "transaction overlap probe condition failed" {:label label})))

(defn contains-token? [value token]
  (and value token (.contains ^String value ^String token)))

(defn class-location [^Class cls]
  (some-> cls .getProtectionDomain .getCodeSource .getLocation str))

(defn origin-result []
  (let [kind (System/getProperty "stage2.transaction.expected-kind")
        transaction-entry
        (if (= kind "recovered")
          "datomic/transaction.clj"
          "datomic/transaction__init.class")
        transaction-location (some-> (jio/resource transaction-entry) str)
        version-location (some-> (jio/resource "datomic/VERSION") str)
        exceptions-location
        (class-location (Class/forName "datomic.impl.Exceptions"))
        transaction-token
        (System/getProperty "stage2.transaction.expected-transaction-origin")
        version-token
        (System/getProperty "stage2.transaction.expected-version-origin")
        java-token
        (System/getProperty "stage2.transaction.expected-java-origin")]
    (require-true! :known-lane-kind (contains? #{"original" "recovered"} kind))
    (require-true! :transaction-origin
                   (contains-token? transaction-location transaction-token))
    (require-true! :version-origin
                   (contains-token? version-location version-token))
    (require-true! :java-origin
                   (contains-token? exceptions-location java-token))
    [:verified true true true]))

(defn datum-result [datum]
  [(.getE ^IDatum datum)
   (.getP ^IDatum datum)
   (.eidx ^IDatumImpl datum)
   (.getA ^IDatum datum)
   (.getV ^IDatum datum)
   (.getT ^IDatum datum)
   (.isAssertion ^IDatum datum)])

(defn dbid-result [dbid]
  [(:part dbid) (:idx dbid)])

(defn bytes-result [^bytes bytes]
  (mapv #(bit-and 255 (int %)) bytes))

(defn serialize-objects [writer-mode objects]
  (let [output (ByteArrayOutputStream.)
        ^Writer writer
        (case writer-mode
          :default (transaction/writer output)
          :explicit-true (transaction/writer output true)
          :explicit-false (transaction/writer output false))]
    (doseq [object objects]
      (.writeObject writer object))
    (.writeFooter writer)
    (.toByteArray output)))

(defn deserialize-objects [^bytes bytes object-count]
  (let [input (ByteArrayInputStream. bytes)
        ^Reader reader (transaction/reader input)
        objects (vec (repeatedly object-count #(.readObject reader)))]
    (.validateFooter reader)
    objects))

(defn handler-result []
  (let [write-inventory
        (->> (transaction/write-handlers false)
             (map (fn [[class handlers]]
                    [(.getName ^Class class) (vec (sort (keys handlers)))]))
             (sort-by first)
             vec)
        read-inventory (vec (sort (keys transaction/read-handlers)))]
    (require-true! :dbid-write-handler
                   (some #(= ["datomic.db.DbId" ["dbid"]] %)
                         write-inventory))
    (require-true! :datum-write-handler
                   (some #(= ["datomic.db.Datum" ["datum"]] %)
                         write-inventory))
    (require-true! :dbid-read-handler (some #{"dbid"} read-inventory))
    (require-true! :datum-read-handler (some #{"datum"} read-inventory))
    [write-inventory read-inventory]))

(defn codec-result []
  (let [eid (db/make-eid 4 12345)
        dbid (db/->DbId :db.part/user -42)
        assertion (db/asserting-datum eid 77 [:stage2/value 17] 101)
        retraction (db/retracting-datum eid 78 [:stage2/retracted] 102)
        objects [dbid assertion retraction dbid assertion retraction]
        normalize
        (fn [decoded]
          [(dbid-result (nth decoded 0))
           (datum-result (nth decoded 1))
           (datum-result (nth decoded 2))
           (dbid-result (nth decoded 3))
           (datum-result (nth decoded 4))
           (datum-result (nth decoded 5))])
        default-bytes (serialize-objects :default objects)
        true-bytes (serialize-objects :explicit-true objects)
        false-bytes (serialize-objects :explicit-false objects)
        default-decoded (normalize (deserialize-objects default-bytes 6))
        true-decoded (normalize (deserialize-objects true-bytes 6))
        false-decoded (normalize (deserialize-objects false-bytes 6))
        expected
        [[:db.part/user -42]
         [eid 4 12345 77 [:stage2/value 17] 101 true]
         [eid 4 12345 78 [:stage2/retracted] 102 false]
         [:db.part/user -42]
         [eid 4 12345 77 [:stage2/value 17] 101 true]
         [eid 4 12345 78 [:stage2/retracted] 102 false]]
        result
        {:handler-inventory (handler-result)
         :default-roundtrip default-decoded
         :explicit-true-roundtrip true-decoded
         :explicit-false-roundtrip false-decoded
         :default-equals-explicit-true
         (= (bytes-result default-bytes) (bytes-result true-bytes))
         :cache-true-bytes (bytes-result true-bytes)
         :cache-false-bytes (bytes-result false-bytes)}]
    (require-equal! :default-roundtrip expected default-decoded)
    (require-equal! :explicit-true-roundtrip expected true-decoded)
    (require-equal! :explicit-false-roundtrip expected false-decoded)
    (require-true! :default-writer-uses-cache
                   (:default-equals-explicit-true result))
    result))

(defn normalize-throw [f]
  (try
    [:ok (f)]
    (catch Throwable t
      [:throw (.getName (class t)) (.getMessage t)])))

(defn encoded-object [value]
  (serialize-objects :default [value]))

(defn read-message-result []
  (cache/clear transaction/log-event-map)
  (let [id (UUID/fromString "11111111-2222-3333-4444-555555555555")
        eid (db/make-eid 4 77)
        msg {:id id
             :data [(db/->DbId :db.part/user -7)
                    (db/asserting-datum eid 91 [:stage2/message] 103)]
             :options {:timeout 5}}
        read-back
        (transaction/read-message
          (ByteArrayInputStream. (encoded-object msg)))
        cached (cache/get-from-cache transaction/log-event-map id nil)
        success
        {:message {:id (:id read-back)
                   :data [(dbid-result (first (:data read-back)))
                          (datum-result (second (:data read-back)))]
                   :options (:options read-back)}
         :cache-keys (vec (sort (keys cached)))
         :read-at-long (integer? (:read-at cached))
         :read-at-positive (pos? (long (:read-at cached)))}
        _ (cache/remove transaction/log-event-map id)
        missing-id
        (normalize-throw
          #(transaction/read-message
             (ByteArrayInputStream.
               (encoded-object {:data [:missing-id]}))))
        malformed
        (normalize-throw
          #(transaction/read-message
             (ByteArrayInputStream. (byte-array 0))))
        result {:success success
                :missing-id missing-id
                :malformed malformed
                :cache-empty (= 0 (cache/fast-count transaction/log-event-map))}]
    (require-equal!
      :read-message-success
      {:message {:id id
                 :data [[:db.part/user -7]
                        [eid 4 77 91 [:stage2/message] 103 true]]
                 :options {:timeout 5}}
       :cache-keys [:read-at]
       :read-at-long true
       :read-at-positive true}
      success)
    (require-equal! :missing-id-class "java.lang.Exception"
                    (second missing-id))
    (require-equal! :missing-id-message "Key not found: :id"
                    (nth missing-id 2))
    (require-equal! :malformed-kind :throw (first malformed))
    (require-true! :read-message-cache-clean (:cache-empty result))
    result))

(defn message-routing-result []
  (let [result
        {:explicit (transaction/peer-message-type
                    {:type :stage2/explicit :id :ignored :data [:ignored]})
         :transaction (transaction/peer-message-type {:id :tx :data []})
         :false-data (transaction/peer-message-type {:id :tx :data false})
         :error (transaction/peer-message-type {:id :error})
         :nil-id (transaction/peer-message-type {:id nil :data [:ignored]})
         :index (transaction/peer-message-type {})
         :submit-string (transaction/submit-address "stage2-db")
         :push-string (transaction/push-address "stage2-db")
         :submit-keyword (transaction/submit-address :stage2/db)
         :push-keyword (transaction/push-address :stage2/db)}]
    (require-equal!
      :message-routing
      {:explicit :stage2/explicit
       :transaction :tx
       :false-data :error
       :error :error
       :nil-id :index
       :index :index
       :submit-string "stage2-db.tx-submit"
       :push-string "stage2-db.tx-result"
       :submit-keyword ":stage2/db.tx-submit"
       :push-keyword ":stage2/db.tx-result"}
      result)
    result))

(defn procargs-result []
  (let [id (UUID/fromString "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
        tx-data [[:db/add 1 :db/doc "stage2"]]
        result
        (with-redefs [common/rand-uuid (fn [] id)]
          [(transaction/create-procargs tx-data nil)
           (transaction/create-procargs tx-data {})
           (transaction/create-procargs tx-data {:timeout 99
                                                  :return :db-after})])]
    (require-equal!
      :create-procargs
      [{:id id :data tx-data}
       {:id id :data tx-data :options {}}
       {:id id :data tx-data
        :options {:timeout 99 :return :db-after}}]
      result)
    result))

(defn add-to-log-result []
  (cache/clear transaction/log-event-map)
  (let [id :stage2/add-to-log]
    (transaction/add-to-log-event! id {:event :first :t 10 :shared :old})
    (transaction/add-to-log-event! id {:datom-count 3
                                       :shared :new})
    (let [result (cache/get-from-cache transaction/log-event-map id nil)]
      (cache/remove transaction/log-event-map id)
      (require-equal!
        :add-to-log-event
        {:event :first :t 10 :shared :new :datom-count 3}
        result)
      result)))

(defn with-captured-transaction-logger
  ([f] (with-captured-transaction-logger Level/INFO f))
  ([level f]
   (let [^Logger log (LoggerFactory/getLogger "datomic.transaction")
         original-level (.getLevel log)
         original-additive (.isAdditive log)
         appender (doto (ListAppender.) (.start))]
     (.setAdditive log false)
     (.setLevel log level)
     (.addAppender log appender)
     (try
       (let [value (f)
             messages (mapv #(.getFormattedMessage %) (.-list appender))]
         [value messages])
       (finally
         (.detachAppender log appender)
         (.stop appender)
         (.setLevel log original-level)
         (.setAdditive log original-additive))))))

(defn loggable-keys-result []
  (let [expected
        [:event :txid :t :msec :apply-msec :datom-count
         :io-stats :tx-stats :pf-stats]]
    (require-equal! :loggable-keys expected transaction/loggable-keys)
    transaction/loggable-keys))

(defn log-completion-result []
  (cache/clear transaction/log-event-map)
  (let [stats (atom [])
        format-calls (atom [])
        logged-maps (atom [])
        format-labels
        [:read-only-msec :timed-transaction-msec :timed-apply-msec]
        nonchunked-ids (list :stage2/absent)
        chunked-ids (vec (range 40))
        _ (require-true! :nonchunked-input
                         (not (chunked-seq? (seq nonchunked-ids))))
        _ (require-true! :chunked-input (chunked-seq? (seq chunked-ids)))
        [[absent-result plain-result read-only-result timed-result
          repeat-result repeat-counts]
         messages]
        (with-captured-transaction-logger
          #(with-redefs
             [monitor/add-stat
              (fn [stat value]
                (swap! stats conj [stat value])
                nil)
              logger/format-as-msec
              (fn [nsec]
                (let [position (count @format-calls)
                      label (nth format-labels position)]
                  (swap! format-calls conj nsec)
                  label))
              logger/process
              (fn [value]
                (swap! logged-maps conj value)
                "stage2-transaction-log")]
             (let [absent (transaction/log-completion! nonchunked-ids)
                   _ (cache/put transaction/log-event-map
                                :stage2/plain
                                {:event :stage2/plain
                                 :t 10
                                 :datom-count 3
                                 :not-loggable :removed})
                   plain (transaction/log-completion! (list :stage2/plain))
                   read-only-at (System/nanoTime)
                   _ (cache/put transaction/log-event-map
                                :stage2/read-only
                                {:t 11
                                 :read-at read-only-at
                                 :datom-count 4})
                   read-only
                   (transaction/log-completion! (list :stage2/read-only))
                   timed-read-at (System/nanoTime)
                   _ (cache/put transaction/log-event-map
                                17
                                {:t 12
                                 :read-at timed-read-at
                                 :started-at 10000000
                                 :applied-at 15000000
                                 :datom-count 5})
                   timed (transaction/log-completion! chunked-ids)
                   before-repeat [(count @stats)
                                  (count @logged-maps)
                                  (count @format-calls)]
                   repeat-completion
                   (transaction/log-completion! (list 17))
                   after-repeat [(count @stats)
                                 (count @logged-maps)
                                 (count @format-calls)]]
               [absent plain read-only timed repeat-completion
                [before-repeat after-repeat]])))
        stat-shapes
        (mapv (fn [[stat value]]
                [stat (integer? value) (not (neg? (long value)))])
              (take 2 @stats))
        info-result
        {:returns [absent-result plain-result read-only-result
                   timed-result repeat-result]
         :messages messages
         :logged-maps @logged-maps
         :format-call-count (count @format-calls)
         :format-stat-matches
         (mapv = @format-calls (mapv second @stats))
         :transaction-stats stat-shapes
         :apply-stat (nth @stats 2)
         :repeat-counts repeat-counts
         :entries-removed
         [(nil? (cache/get-from-cache transaction/log-event-map
                                      :stage2/plain nil))
          (nil? (cache/get-from-cache transaction/log-event-map
                                      :stage2/read-only nil))
          (nil? (cache/get-from-cache transaction/log-event-map 17 nil))]
         :cache-empty-before-disabled
         (= 0 (cache/fast-count transaction/log-event-map))}
        disabled-stats (atom [])
        disabled-formats (atom [])
        disabled-processes (atom [])
        disabled-id :stage2/info-disabled
        _ (cache/put transaction/log-event-map
                     disabled-id
                     {:t 13
                      :read-at (System/nanoTime)
                      :started-at 20000000
                      :applied-at 27000000
                      :datom-count 6})
        [disabled-return disabled-messages]
        (with-captured-transaction-logger
          Level/OFF
          #(with-redefs
             [monitor/add-stat
              (fn [stat value]
                (swap! disabled-stats conj [stat value])
                nil)
              logger/format-as-msec
              (fn [nsec]
                (let [position (count @disabled-formats)
                      label (nth [:disabled-transaction-msec
                                  :disabled-apply-msec]
                                 position)]
                  (swap! disabled-formats conj nsec)
                  label))
              logger/process
              (fn [value]
                (swap! disabled-processes conj value)
                "unexpected-disabled-log")]
             (transaction/log-completion! (list disabled-id))))
        disabled-result
        {:return disabled-return
         :messages disabled-messages
         :process-count (count @disabled-processes)
         :format-call-count (count @disabled-formats)
         :format-stat-matches
         (mapv = @disabled-formats (mapv second @disabled-stats))
         :transaction-stat
         [(ffirst @disabled-stats)
          (integer? (second (first @disabled-stats)))
          (not (neg? (long (second (first @disabled-stats)))))]
         :apply-stat (second @disabled-stats)
         :entry-removed
         (nil? (cache/get-from-cache transaction/log-event-map
                                     disabled-id nil))}
        result
        {:info-enabled info-result
         :info-disabled disabled-result
         :cache-empty (= 0 (cache/fast-count transaction/log-event-map))}]
    (require-equal! :log-completion-returns [nil nil nil nil nil]
                    (:returns info-result))
    (require-equal! :log-completion-messages
                    ["stage2-transaction-log" "stage2-transaction-log"
                     "stage2-transaction-log"]
                    messages)
    (require-equal!
      :log-completion-maps
      [{:event :stage2/plain :txid :stage2/plain :t 10 :datom-count 3}
       {:event :tx/process :txid :stage2/read-only :t 11
        :msec :read-only-msec :datom-count 4}
       {:event :tx/process :txid 17 :t 12
        :msec :timed-transaction-msec
        :apply-msec :timed-apply-msec
        :datom-count 5}]
      @logged-maps)
    (require-equal! :log-completion-format-count 3
                    (:format-call-count info-result))
    (require-equal! :format-stat-matches [true true true]
                    (:format-stat-matches info-result))
    (require-equal! :transaction-stat-shapes
                    [[:TransactionNsec true true]
                     [:TransactionNsec true true]]
                    (:transaction-stats info-result))
    (require-equal! :apply-stat
                    [:TransactionApplyNsec 5000000]
                    (:apply-stat info-result))
    (require-equal! :repeat-completion-no-effects
                    [[3 3 3] [3 3 3]]
                    (:repeat-counts info-result))
    (require-equal! :log-entries-removed [true true true]
                    (:entries-removed info-result))
    (require-true! :pre-disabled-cache-empty
                   (:cache-empty-before-disabled info-result))
    (require-equal!
      :info-disabled
      {:return nil
       :messages []
       :process-count 0
       :format-call-count 2
       :format-stat-matches [true true]
       :transaction-stat [:TransactionNsec true true]
       :apply-stat [:TransactionApplyNsec 7000000]
       :entry-removed true}
      disabled-result)
    (require-true! :log-cache-empty (:cache-empty result))
    result))

(defn compiler-domain-result []
  (cache/clear transaction/log-event-map)
  (let [id :stage2/singleton-sequence
        _ (cache/put transaction/log-event-map
                     id
                     (list {:event :stage2/compiler-edge :t 12}))
        [outcome messages]
        (with-captured-transaction-logger
          #(with-redefs
             [logger/process (constantly "stage2-compiler-edge")]
             (normalize-throw
               (fn []
                 (transaction/log-completion! (list id))
                 :completed))))
        result [outcome messages
                (nil? (cache/get-from-cache transaction/log-event-map id nil))]]
    (cache/clear transaction/log-event-map)
    (require-true! :compiler-edge-known-outcome
                   (or (= [:ok :completed] outcome)
                       (= :throw (first outcome))))
    (require-true! :compiler-edge-removed (nth result 2))
    result))

(let [supported-result
      [[:origin (origin-result)]
       [:loggable-keys (loggable-keys-result)]
       [:routing (message-routing-result)]
       [:codec (codec-result)]
       [:read-message (read-message-result)]
       [:procargs (procargs-result)]
       [:add-to-log-event (add-to-log-result)]
       [:log-completion (log-completion-result)]]
      compiler-result (compiler-domain-result)]
  (println "STAGE2_TRANSACTION_OVERLAP_SUPPORTED_RESULT"
           (pr-str supported-result))
  (println "STAGE2_TRANSACTION_OVERLAP_COMPILER_DOMAIN_RESULT"
           (pr-str compiler-result)))

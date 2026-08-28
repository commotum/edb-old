(require '[clojure.core.async :as async]
         '[datomic.common :as common]
         '[datomic.core2.anomalies :as anomalies]
         '[datomic.db :as db]
         '[datomic.future]
         '[datomic.index]
         '[datomic.memory-size :as memory-size]
         '[datomic.query]
         '[datomic.query.support :as query.support]
         '[datomic.stats :as stats])

(definterface DatomicRevAuditIdent
  (ident [x]))

(def fake-db
  (reify datomic.db.IDb
    DatomicRevAuditIdent
    (getNextT [_] nil)
    (keywordOf [_ x] (keyword (str "a" x)))
    (idOf [_ x] x)
    (getAsOfT [_] nil)
    (getSinceT [_] nil)
    (getRaw [_] nil)
    (getFilter [_] nil)
    (seekEAVT [_ _] nil)
    (seekAVET [_ _] nil)
    (seekAEVT [_ _] nil)
    (seekRAET [_ _] nil)
    (getFn [_ _] nil)
    (ident [_ x] [:ident x])))

(def datum
  (reify datomic.impl.db.IDatum
    (isAssertion [_] true)
    (getE [_] 1)
    (getA [_] 7)
    (getT [_] 1)
    (getTx [_] 1)
    (getP [_] 0)
    (getV [_] nil)
    (getLongV [_] 0)
    (getDoubleV [_] 0.0)
    (getIntV [_] 0)
    (getFloatV [_] (float 0.0))
    (getBooleanV [_] false)))

(defn audit-db [raw-id]
  (db/->Db raw-id nil nil nil nil nil nil
           0 42 0 nil nil nil nil nil nil nil nil false nil))

(let [counted (query.support/counted-seq [1 2] 2)
      supplied (object-array 3)
      output (.toArray ^java.util.List counted supplied)]
  (assert (identical? supplied output))
  (assert (= [1 2 nil] (vec output))))

(let [entry {:key datum :count 3}
      expected-attribute {[:ident :a7] {:data-count 3 :seg-count 1}}
      part (long (db/partition-eid 1))
      expected-entity {[:ident part] {:data-count 3 :seg-count 1}}]
  (assert (= expected-attribute (stats/avet fake-db {:avet [entry]})))
  (assert (= expected-attribute (stats/aevt fake-db {:aevt [entry]})))
  (assert (= expected-entity (stats/eavt fake-db {:eavt [entry]})))
  (assert (= expected-entity (stats/raet fake-db {:raet [entry]}))))

(let [transposed (datomic.index.TransposedData.
                   2
                   (long-array [1 10 2 20])
                   (object-array [:v1 :v2])
                   (long-array [100 200])
                   (boolean-array [true false]))
      pair (fn [datum]
             [(.getE ^datomic.impl.db.IDatum datum)
              (.getA ^datomic.impl.db.IDatum datum)])]
  (assert (= [1 10]
             (pair (.get ^java.util.List
                         (.subList ^java.util.List transposed 0 1)
                         0))))
  (assert (= [1 10]
             (pair (.next ^java.util.Iterator
                          (.iterator ^java.lang.Iterable transposed))))))

(let [d1 (db/->Datum 1 7 :x 2)
      d2 (db/->Datum 2 8 :y 4)]
  (assert (false? (.equals (datomic.db.AVof. d1)
                            (datomic.db.AVof. d2))))
  (assert (false? (.equals (datomic.db.EAVof. d1)
                            (datomic.db.EAVof. d2))))
  (assert (false? (.equals (datomic.db.EAOpof. d1)
                            (datomic.db.EAOpof. d2)))))

(let [database (audit-db :same)
      e1 (datomic.query.EntityMap. database 1 nil nil)
      e2 (datomic.query.EntityMap. database 2 nil nil)]
  (assert (false? (.equals e1 e2))))

(let [d1 (audit-db :one)
      d2 (audit-db :two)]
  (assert (false? (boolean (.eq ^datomic.db.IDbImpl d1 d2)))))

(assert (true? (db/reverse-key? :person/_friend)))
(assert (false? (db/reverse-key? :person/friend)))
(let [needs-avet? (deref (ns-resolve 'datomic.db 'needs-avet?))]
  (assert (true? (needs-avet? {:db/index true})))
  (assert (true? (needs-avet? {:db/unique :db.unique/identity})))
  (assert (false? (needs-avet? {:index true :unique true}))))

(let [value-type (db/->ValueType 7 :db.type/example :example-tag)]
  (assert (= :example-tag (:fressian-tag value-type)))
  (assert (not (contains? value-type :fressian_tag))))

(let [anomaly {:cognitect.anomalies/category :cognitect.anomalies/fault
               :cognitect.anomalies/message "recovered-athrow"
               :audit/value 7}
      thrown (try
               (anomalies/athrow anomaly)
               nil
               (catch Throwable failure failure))]
  (assert (instance? clojure.lang.ExceptionInfo thrown))
  (assert (= "recovered-athrow" (.getMessage ^Throwable thrown)))
  (assert (= anomaly (ex-data thrown))))

(let [filling-promise (deref (ns-resolve 'datomic.future 'filling-promise))
      calls (atom 0)
      [wrapped result-ch] (filling-promise #(do (swap! calls inc) :wrapped-result))]
  (assert (= :wrapped-result (wrapped)))
  (assert (= :wrapped-result (async/<!! result-ch)))
  (assert (= 1 @calls)))

(let [filling-promise (deref (ns-resolve 'datomic.future 'filling-promise))
      failure (ex-info "future-wrapper-failure" {:audit/value 11})
      calls (atom 0)
      [wrapped result-ch] (filling-promise #(do (swap! calls inc) (throw failure)))
      thrown (try
               (wrapped)
               nil
               (catch Throwable t t))]
  (assert (identical? failure thrown))
  (assert (identical? failure (async/<!! result-ch)))
  (assert (= 1 @calls)))

(let [executor (java.util.concurrent.Executors/newSingleThreadExecutor)]
  (try
    (assert (= :pfuture-result
               @(common/pfuture executor (fn [] :pfuture-result))))
    (let [failure (ex-info "pfuture-failure" {:audit/value 13})
          thrown (try
                   @(common/pfuture executor (fn [] (throw failure)))
                   nil
                   (catch java.util.concurrent.ExecutionException t t))]
      (assert (identical? failure (.getCause thrown))))
    (finally
      (.shutdownNow ^java.util.concurrent.ExecutorService executor)
      (assert (.awaitTermination
                ^java.util.concurrent.ExecutorService executor
                5
                java.util.concurrent.TimeUnit/SECONDS)))))

(let [bytes-from-octets (fn [xs] (byte-array (map unchecked-byte xs)))
      compare-octets (fn [a b]
                       (common/compare-byte-arrays
                         (bytes-from-octets a)
                         (bytes-from-octets b)))]
  (assert (= 0 (compare-octets [0 127 128 255] [0 127 128 255])))
  (assert (= -1 (compare-octets [255] [255 0])))
  (assert (= 1 (compare-octets [255 0] [255])))
  ;; Byte/valueOf preserves signed byte values: 0x80 - 0x7f is -255.
  (assert (= -255 (compare-octets [0 128 255] [0 127 255])))
  (assert (= 255 (compare-octets [0 127 255] [0 128 255]))))

(assert (= 16 (memory-size/memory-size (object-array 0))))
(assert (= 82 (memory-size/memory-size (object-array [nil "a"]))))

(println "recovered reflective-call, receiver, literal, lookup, record, throw, future-wrapper, pfuture, byte-array-compare, object-array-size, and nested-reify behavior passed")

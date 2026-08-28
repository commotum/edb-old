(require '[clojure.core.async :as async]
         '[datomic.common :as common]
         '[datomic.core2.anomalies :as anomalies]
         '[datomic.crypto :as crypto]
         '[datomic.datafy :as datafy]
         '[datomic.datalog :as datalog]
         '[datomic.db :as db]
         '[datomic.error :as error]
         '[datomic.future]
         '[datomic.index]
         '[datomic.io :as dio]
         '[datomic.memory-size :as memory-size]
         '[datomic.monitor :as monitor]
         '[datomic.query]
         '[datomic.query.support :as query.support]
         '[datomic.slf4j :as slf4j]
         '[datomic.stats :as stats])

(definterface DatomicRevAuditIdent
  (ident [x]))

(definterface DatomicRevAuditOverloadedSetter
  (^void setAudit [^String value])
  (^void setAudit [^Object value]))

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

(let [selected (datafy/filter-overlapped-setters
                 (datafy/setters DatomicRevAuditOverloadedSetter))]
  (assert (= 1 (count selected)))
  (let [^java.lang.reflect.Method setter (first selected)]
    (assert (= "setAudit" (.getName setter)))
    (assert (= [java.lang.Object] (vec (.getParameterTypes setter))))))

(let [init-stats (deref (ns-resolve 'datomic.monitor 'init-stats))
      observed (atom [])]
  (with-redefs [monitor/statistics (atom (init-stats))
                monitor/metric-event-callback
                (fn [k value] (swap! observed conj [k value]))]
    (monitor/add-stat :DatomicRevAudit 5)
    (monitor/add-stat :DatomicRevAudit -2)
    (assert (= {:DatomicRevAudit {:lo -2 :hi 5 :sum 3 :count 2}}
               (monitor/snapshot-statistics)))
    (assert (= [[:DatomicRevAudit 5] [:DatomicRevAudit -2]] @observed))))

(let [text "héλ🙂"
      ^bytes octets (.getBytes text "UTF-8")
      heap (java.nio.ByteBuffer/wrap octets)
      direct (doto (java.nio.ByteBuffer/allocateDirect (alength octets))
               (.put octets)
               (.flip))
      base128 (dio/bbuf->base128 direct)
      decoded (dio/base128->bbuf base128)]
  (assert (= text (dio/bbuf->string heap)))
  (assert (= text (dio/bbuf->string direct)))
  (assert (java.util.Arrays/equals
            octets
            ^bytes (dio/slurp-bytes decoded)))
  (assert (= 3421780262
             (dio/crc32 (dio/string->bbuf "123456789")))))

(let [algorithm "HmacSHA256"
      key (javax.crypto.spec.SecretKeySpec.
            (.getBytes "datomic-rev-audit-key" "UTF-8")
            algorithm)
      ^bytes payload (.getBytes "authenticated payload" "UTF-8")
      source (doto (java.nio.ByteBuffer/allocate
                     (+ (alength payload) (crypto/hmac-length algorithm)))
               (.put payload)
               (.flip))
      authenticated (crypto/append-hmac source algorithm key)
      validated (crypto/validate-hmac authenticated algorithm key)
      tampered (.duplicate ^java.nio.ByteBuffer authenticated)]
  (assert (java.util.Arrays/equals
            payload
            ^bytes (dio/slurp-bytes validated)))
  (.put ^java.nio.ByteBuffer
        tampered
        (int 0)
        (unchecked-byte
          (bit-xor 1 (bit-and 0xff (.get ^java.nio.ByteBuffer tampered (int 0))))))
  (let [thrown (try
                 (crypto/validate-hmac tampered algorithm key)
                 nil
                 (catch clojure.lang.ExceptionInfo failure failure))]
    (assert (instance? clojure.lang.ExceptionInfo thrown))
    (assert (= :datomic.crypto/validate-hmac-failed
               (:db/error (ex-data thrown))))))

(let [logger (org.slf4j.LoggerFactory/getLogger "datomic.slf4j")
      ^ch.qos.logback.classic.Logger backend logger
      prior-level (.getLevel backend)
      prior-handler (java.lang.Thread/getDefaultUncaughtExceptionHandler)
      failure (doto (ex-info "slf4j focused regression" {:audit/value 17})
                (.setStackTrace (make-array java.lang.StackTraceElement 0)))]
  (try
    (.setLevel backend ch.qos.logback.classic.Level/ALL)
    (assert (.isWarnEnabled ^org.slf4j.Logger logger))
    (assert (nil? ((deref error/reporter) failure)))
    (assert (nil? (slf4j/log-uncaught-exceptions)))
    (let [handler (java.lang.Thread/getDefaultUncaughtExceptionHandler)]
      (assert (instance? java.lang.Thread$UncaughtExceptionHandler handler))
      (.uncaughtException
        ^java.lang.Thread$UncaughtExceptionHandler handler
        (java.lang.Thread/currentThread)
        failure))
    (finally
      (java.lang.Thread/setDefaultUncaughtExceptionHandler prior-handler)
      (.setLevel backend prior-level))))

(let [cases [[nil 0 -1]
             [0 nil 1]
             [1 2 -1]
             [2 1 1]
             [1 "1" -1]
             ["1" 1 1]
             ["a" "b" -1]
             [:a :b -1]
             [[1 2] [1 3] -1]
             [(list 1 2) (list 1 3) -1]
             [[1] (list 1) 0]
             [[1] {:a 1} -1]
             [{:a 1} #{1} -1]
             [:a 'a -1]]]
  (doseq [[a b expected-sign] cases]
    (let [result (common/compare a b)
          actual-sign (cond (neg? result) -1 (pos? result) 1 :else 0)]
      (assert (= expected-sign actual-sign)))))

(doseq [[a b] [[{:a 1} {:a 2}] [#{1} #{2}]]]
  (let [ab (common/compare a b)
        ba (common/compare b a)]
    (assert (not (zero? ab)))
    (assert (= ab (- ba)))))

(let [^java.util.Comparator natural (common/key-comparator :rank)
      ^java.util.Comparator reverse
      (common/key-comparator :rank (java.util.Comparator/reverseOrder))]
  (assert (neg? (.compare natural {:rank 1} {:rank 2})))
  (assert (pos? (.compare reverse {:rank 1} {:rank 2}))))

(let [executor (java.util.concurrent.Executors/newFixedThreadPool 2)]
  (try
    (assert (= [2 4 6]
               (common/pooled-mapv executor #(* 2 %) [1 2 3])))
    (let [failure (ex-info "pooled-mapv failure" {:audit/value 19})
          thrown (try
                   (common/pooled-mapv
                     executor
                     #(if (= 2 %) (throw failure) %)
                     [1 2 3])
                   nil
                   (catch java.util.concurrent.ExecutionException t t))]
      (assert (identical? failure (.getCause thrown))))
    (finally
      (.shutdownNow ^java.util.concurrent.ExecutorService executor)
      (assert (.awaitTermination
                ^java.util.concurrent.ExecutorService executor
                5
                java.util.concurrent.TimeUnit/SECONDS)))))

(let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")
      ^ch.qos.logback.classic.Logger backend logger
      prior-level (.getLevel backend)
      alarmed (promise)
      ran (promise)
      failure (doto (ex-info "scheduled focused regression" {:audit/value 23})
                (.setStackTrace (make-array java.lang.StackTraceElement 0)))]
  (try
    (.setLevel backend ch.qos.logback.classic.Level/ALL)
    (assert (.isWarnEnabled ^org.slf4j.Logger logger))
    (with-redefs [monitor/alarm (fn [k] (deliver alarmed k))]
      (let [scheduled (common/schedule
                        "datomic-rev-focused-regression"
                        #(do (deliver ran :ran) (throw failure))
                        1
                        :once true)]
        (try
          (assert (= :ran (deref ran 5000 ::timeout)))
          (assert (= :UnhandledException (deref alarmed 5000 ::timeout)))
          (finally
            (.close ^java.io.Closeable scheduled)))))
    (finally
      (.setLevel backend prior-level))))

(let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")
      ^ch.qos.logback.classic.Logger backend logger
      prior-level (.getLevel backend)
      failure (doto (ex-info "retry focused regression" {:audit/value 29})
                (.setStackTrace (make-array java.lang.StackTraceElement 0)))]
  (try
    (.setLevel backend ch.qos.logback.classic.Level/ALL)
    (assert (.isInfoEnabled ^org.slf4j.Logger logger))
    (assert (nil? (common/log-retry failure 10 2 5)))
    (finally
      (.setLevel backend prior-level))))

(let [empty-bindings (object-array [nil nil nil])
      bindings (object-array [nil 2 0])]
  (assert (= 0 ((datalog/hashxf empty-bindings) [11 22 33])))
  (assert (= 0 ((datalog/hashyf empty-bindings) [101 202 303])))
  (assert (= -1919633982 ((datalog/hashxf bindings) [11 22 33])))
  (assert (= -1919719376 ((datalog/hashyf bindings) [101 202 303]))))

(let [xs (mapv (fn [i] [i (+ 10 i)]) (range 11))
      ys (mapv (fn [i] [i (+ 100 i)]) (range 11))
      expected (set (map (fn [i] [(+ 10 i) (+ 100 i)]) (range 11)))]
  (assert (= expected
             (datalog/join-project-coll-with
               xs ys {0 0} {1 0} {1 1} datalog/truep))))

(let [invalid-source (java.lang.Object.)
      calls [(fn []
               (datalog/join-project
                 invalid-source [] {} {} {} datalog/truep))
             (fn []
               (datalog/join-project-with
                 invalid-source [] {} {} {} datalog/truep))
             (fn [] (datalog/extrel invalid-source [] nil nil))]]
  (doseq [invoke calls]
    (let [thrown (try
                   (invoke)
                   nil
                   (catch Throwable failure failure))]
      (assert (instance? clojure.lang.IExceptionInfo thrown))
      (assert (= :db.error/invalid-data-source
                 (:db/error (ex-data thrown))))
      (assert (identical? invalid-source (:input (ex-data thrown))))
      (assert (.contains
                ^java.lang.String (.getMessage ^Throwable thrown)
                "class java.lang.Object")))))

(println "recovered reflective-call, receiver, literal, lookup, record, throw, future-wrapper, pfuture, byte-array-compare, object-array-size, and nested-reify behavior passed")

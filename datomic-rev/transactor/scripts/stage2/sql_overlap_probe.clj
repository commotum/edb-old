(ns datomic-rev.stage2-sql-overlap-probe
  (:require [datomic.config :as config]
            [datomic.kv-sql :as kv-sql]
            [datomic.kv-sql-ext :as kv-sql-ext]
            [datomic.kv-store :as kv-store]
            [datomic.monitor :as monitor]
            [datomic.sql :as sql])
  (:import [ch.qos.logback.classic Level Logger]
           [java.lang.reflect InvocationHandler Proxy]
           [java.nio ByteBuffer]
           [java.sql Connection Driver DriverManager DriverPropertyInfo
            PreparedStatement ResultSet SQLException]
           [java.util.concurrent Callable]
           [org.apache.tomcat.jdbc.pool DataSource]
           [org.slf4j LoggerFactory]))

(defn fail! [message data]
  (throw (ex-info message data)))

(defn require-equal! [label expected actual]
  (when-not (= expected actual)
    (fail! "SQL overlap probe mismatch"
           {:label label :expected expected :actual actual})))

(defn bytes-of [value]
  (cond
    (nil? value) nil
    (instance? ByteBuffer value)
    (let [copy (.duplicate ^ByteBuffer value)
          bytes (byte-array (.remaining copy))]
      (.get copy bytes)
      (mapv #(bit-and 255 (int %)) bytes))
    (instance? (Class/forName "[B") value)
    (mapv #(bit-and 255 (int %)) value)
    :else (fail! "unexpected binary value" {:class (class value)})))

(defn stored-value [value]
  [(:id value) (:rev value) (:map value) (bytes-of (:val value))])

(defn sql-exception-result [f]
  (try
    (f)
    [:not-thrown]
    (catch SQLException e
      [:sql-exception (.getMessage e) (.getSQLState e) (.getErrorCode e)])))

(defn normalized-outcome [f normalize]
  (try
    [:ok (normalize (f))]
    (catch Throwable t
      [:throw (.getName (class t))])))

(defn quiet-logging! []
  (let [logger (LoggerFactory/getLogger Logger/ROOT_LOGGER_NAME)]
    (when (instance? Logger logger)
      (.setLevel ^Logger logger Level/OFF))))

(defn constraint-and-retry-result []
  (let [result [(kv-sql/constraint-violation?
                  (SQLException. "duplicate" "23505" 0))
                (kv-sql/constraint-violation?
                  (SQLException. "syntax" "42000" 0))
                (kv-store/retryable?
                  (SQLException. "login" "28000" kv-sql/LOGIN_FAILED))
                (kv-store/retryable?
                  (SQLException. "transient" "08006" 17))]]
    (require-equal! :constraint-and-retry [true false false true] result)
    result))

(defn kv-sql-result []
  (let [calls (atom [])
        spec :stage2/sql-spec
        store (kv-sql/from-spec spec)
        constructed (kv-sql/->KVSql :stage2/constructed)
        constraint (SQLException. "duplicate" "23505" 0)
        nonconstraint (SQLException. "broken" "42000" 7)
        normalize (fn [value] (stored-value value))
        result
        (with-redefs
          [sql/select
           (fn [actual-spec key]
             (swap! calls conj [:select actual-spec key])
             (case key
               :missing nil
               :found {:id :found
                       :rev 9
                       :map "{:kind :persisted, :flag true}"
                       :val (byte-array [0 127 -128 -1])}
               :bare {:id :bare :rev 2 :map nil :val nil}))
           sql/insert
           (fn [actual-spec value]
             (swap! calls conj [:insert actual-spec (normalize value)])
             (case (:id value)
               :constraint (throw constraint)
               :nonconstraint (throw nonconstraint)
               1))
           sql/update
           (fn [actual-spec id ensure-rev value]
             (swap! calls conj
                    [:update actual-spec id ensure-rev (normalize value)])
             (if (= id :cas-win) 1 0))
           sql/delete
           (fn [actual-spec key]
             (swap! calls conj [:delete actual-spec key])
             0)]
          (let [missing (kv-store/get store :missing false)
                found (kv-store/get store :found true)
                bare (kv-store/get store :bare false)
                empty-put (kv-store/put store {:id :empty :rev 1})
                value-put (kv-store/put
                            store
                            (array-map :id :value :rev 2
                                       :v (ByteBuffer/wrap
                                            (byte-array [1 2 -1]))
                                       :kind :probe))
                cas-win (kv-store/put
                          store
                          {:id :cas-win :rev 5 :ensure {:rev 4}
                           :kind :cas})
                cas-loss (kv-store/put
                           store
                           {:id :cas-loss :rev 6 :ensure {:rev 5}})
                conflict (kv-store/put store {:id :constraint :rev 1})
                nonconstraint-result
                (sql-exception-result
                  #(kv-store/put store {:id :nonconstraint :rev 1}))
                deleted (kv-store/delete store :gone true)]
            [[missing
              [(:id found) (:rev found) (:kind found) (:flag found)
               (bytes-of (:v found))]
              [(:id bare) (:rev bare) (contains? bare :v)]
              empty-put value-put cas-win cas-loss conflict
              nonconstraint-result deleted
              (kv-store/close store)
              (kv-store/close constructed)
              (satisfies? kv-store/KVStore store)
              (satisfies? kv-store/KVStore constructed)]
             @calls]))]
    (require-equal!
      :kv-sql-results
      [nil
       [:found 9 :persisted true [0 127 128 255]]
       [:bare 2 false]
       :ok :ok :ok nil nil
       [:sql-exception "broken" "42000" 7]
       :ok nil nil true true]
      (first result))
    (require-equal!
      :kv-sql-calls
      [[:select spec :missing]
       [:select spec :found]
       [:select spec :bare]
       [:insert spec [:empty 1 "{}" nil]]
       [:insert spec [:value 2 "{:kind :probe}" [1 2 255]]]
       [:update spec :cas-win 4 [nil 5 "{:kind :cas}" nil]]
       [:update spec :cas-loss 5 [nil 6 "{}" nil]]
       [:insert spec [:constraint 1 "{}" nil]]
       [:insert spec [:nonconstraint 1 "{}" nil]]
       [:delete spec :gone]]
      (second result))
    result))

(defn interface-proxy [^Class interface handler]
  (Proxy/newProxyInstance
    (.getClassLoader interface)
    (into-array Class [interface])
    (reify InvocationHandler
      (invoke [_ proxy method args]
        (let [method-name (.getName ^java.lang.reflect.Method method)
              argv (if args (vec args) [])]
          (case method-name
            "toString" (str "stage2-proxy:" (.getName interface))
            "hashCode" (System/identityHashCode proxy)
            "equals" (identical? proxy (first argv))
            (handler method-name argv)))))))

(defn jdbc-value [value]
  (if (instance? (Class/forName "[B") value)
    (bytes-of value)
    value))

(defn sql-core-fixture
  [{:keys [row update-result execute-result failure]
    :or {update-result 7 execute-result true}}]
  (let [events (atom [])
        result-set
        (interface-proxy
          ResultSet
          (fn [method-name argv]
            (case method-name
              "next" (do (swap! events conj :result-next) (boolean row))
              "getString"
              (let [column (first argv)]
                (swap! events conj [:result-get-string column])
                (case column "id" (:id row) "map" (:map row)))
              "getLong" (do
                          (swap! events conj [:result-get-long (first argv)])
                          (long (:rev row)))
              "getBytes" (do
                           (swap! events conj [:result-get-bytes (first argv)])
                           (:val row))
              "close" (swap! events conj :result-close)
              (fail! "unexpected SQL ResultSet method" {:method method-name}))))
        statement-for
        (fn [statement-sql]
          (interface-proxy
            PreparedStatement
            (fn [method-name argv]
              (case method-name
                "setObject"
                (swap! events conj
                       (into [:set-object]
                             (mapv jdbc-value argv)))
                "executeUpdate"
                (do
                  (swap! events conj :execute-update)
                  (if (= failure :execute-update)
                    (throw (SQLException. "stage2 update failure" "08006" 71))
                    (int update-result)))
                "executeQuery"
                (do (swap! events conj :execute-query) result-set)
                "execute"
                (do (swap! events conj [:execute statement-sql]) execute-result)
                "close" (swap! events conj :statement-close)
                (fail! "unexpected SQL PreparedStatement method"
                       {:method method-name :sql statement-sql})))))
        connection
        (interface-proxy
          Connection
          (fn [method-name argv]
            (case method-name
              "prepareStatement"
              (let [statement-sql (first argv)]
                (swap! events conj [:prepare statement-sql])
                (if (= failure :prepare)
                  (throw (SQLException. "stage2 prepare failure" "08006" 72))
                  (statement-for statement-sql)))
              "close" (swap! events conj :connection-close)
              (fail! "unexpected SQL Connection method" {:method method-name}))))
        datasource
        (interface-proxy
          javax.sql.DataSource
          (fn [method-name _]
            (case method-name
              "getConnection" (do
                                (swap! events conj :datasource-get-connection)
                                connection)
              (fail! "unexpected SQL DataSource method" {:method method-name}))))]
    {:events events
     :connection connection
     :datasource datasource
     :spec {:factory (fn [] connection)}}))

(defn sql-core-connect-result []
  (let [{:keys [events connection datasource]} (sql-core-fixture {})
        datasource-result (sql/connect {:datasource datasource
                                        :factory (fn [] :wrong)})
        datasource-events @events
        factory-called (atom 0)
        factory-result (sql/connect {:factory (fn []
                                                (swap! factory-called inc)
                                                connection)})
        invalid
        (try
          (sql/connect {})
          [:not-thrown]
          (catch IllegalArgumentException e
            (let [data (ex-data e)]
              [(:db/error data)
               (:cognitect.anomalies/category data)
               (:cognitect.anomalies/message data)])))
        result [(identical? connection datasource-result)
                datasource-events
                (identical? connection factory-result)
                @factory-called
                invalid]]
    (require-equal!
      :sql-core-connect
      [true [:datasource-get-connection] true 1
       [:db.error/invalid-sql-connection
        :cognitect.anomalies/incorrect
        "Must supply DataSource or Callable<Connection>"]]
      result)
    result))

(defn sql-core-run [operation fixture-config f]
  (let [{:keys [events spec] :as fixture} (sql-core-fixture fixture-config)
        result (f spec fixture)
        output [result @events]]
    [operation output]))

(defn sql-core-result []
  (let [binary (byte-array [0 -1 17])
        update-with-nulls
        (sql-core-run
          :update-with-nulls {:update-result 5}
          (fn [spec _]
            (sql/update-with-nulls spec "pod" 2
                                   {:rev 3 :map nil :val binary})))
        insert-with-nulls
        (sql-core-run
          :insert-with-nulls {:update-result 6}
          (fn [spec _]
            (sql/insert-with-nulls spec
                                   {:id "item" :rev 4 :map nil :val binary})))
        update-result
        (sql-core-run
          :update {:update-result 8}
          (fn [spec _]
            (sql/update spec "pod" 7 {:rev 9 :map nil :val binary})))
        insert-result
        (sql-core-run
          :insert {:update-result 9}
          (fn [spec _]
            (sql/insert spec {:id "item" :rev 0 :map "" :val binary})))
        select-present
        (sql-core-run
          :select-present
          {:row {:id "item" :rev 11 :map "{:kind :sql}" :val binary}}
          (fn [spec _]
            (let [selected (sql/select spec "item")]
              [(:id selected) (:rev selected) (:map selected)
               (bytes-of (:val selected))])))
        select-missing
        (sql-core-run :select-missing {}
                      (fn [spec _] (sql/select spec "missing")))
        delete-result
        (sql-core-run :delete {:update-result 2}
                      (fn [spec _] (sql/delete spec "gone")))
        execute-commands
        (let [{:keys [events connection]} (sql-core-fixture {})]
          [:execute-commands
           [(sql/execute-commands connection "one" "two" "three")
            @events]])
        execute-failure
        (let [{:keys [events spec]} (sql-core-fixture
                                      {:failure :execute-update})]
          [:execute-failure
           [(sql-exception-result
              #(sql/delete spec "failure"))
            @events]])
        prepare-failure
        (let [{:keys [events spec]} (sql-core-fixture {:failure :prepare})]
          [:prepare-failure
           [(sql-exception-result
              #(sql/delete spec "failure"))
            @events]])
        result [[:connect (sql-core-connect-result)]
                update-with-nulls insert-with-nulls update-result insert-result
                select-present select-missing delete-result execute-commands
                execute-failure prepare-failure]]
    (require-equal!
      :sql-update-with-nulls
      [:update-with-nulls
       [5
        [[:prepare "update datomic_kvs set rev=?, map=?, val=? where id=? and rev=?"]
         [:set-object 1 3 java.sql.Types/INTEGER]
         [:set-object 2 nil java.sql.Types/LONGVARCHAR]
         [:set-object 3 [0 255 17] java.sql.Types/LONGVARBINARY]
         [:set-object 4 "pod" java.sql.Types/VARCHAR]
         [:set-object 5 2 java.sql.Types/INTEGER]
         :execute-update :statement-close :connection-close]]]
      update-with-nulls)
    (require-equal!
      :sql-insert-with-nulls
      [:insert-with-nulls
       [6
        [[:prepare "insert into datomic_kvs (id, rev, map, val) values (?, ?, ?, ?)"]
         [:set-object 1 "item" java.sql.Types/LONGVARCHAR]
         [:set-object 2 4 java.sql.Types/INTEGER]
         [:set-object 3 nil java.sql.Types/LONGVARCHAR]
         [:set-object 4 [0 255 17] java.sql.Types/LONGVARBINARY]
         :execute-update :statement-close :connection-close]]]
      insert-with-nulls)
    (require-equal!
      :sql-update
      [:update
       [8
        [[:prepare "update datomic_kvs set rev=?, val=? where id=? and rev=?"]
         [:set-object 1 9] [:set-object 2 [0 255 17]]
         [:set-object 3 "pod"] [:set-object 4 7]
         :execute-update :statement-close :connection-close]]]
      update-result)
    (require-equal!
      :sql-insert
      [:insert
       [9
        [[:prepare "insert into datomic_kvs (id, rev, map, val) values (?, ?, ?, ?)"]
         [:set-object 1 "item"] [:set-object 2 0] [:set-object 3 ""]
         [:set-object 4 [0 255 17]]
         :execute-update :statement-close :connection-close]]]
      insert-result)
    (require-equal!
      :sql-select-present
      [:select-present
       [["item" 11 "{:kind :sql}" [0 255 17]]
        [[:prepare "select id, rev, map, val from datomic_kvs where id = ?"]
         [:set-object 1 "item"] :execute-query :result-next
         [:result-get-string "id"] [:result-get-long "rev"]
         [:result-get-string "map"] [:result-get-bytes "val"]
         :result-close :statement-close :connection-close]]]
      select-present)
    (require-equal!
      :sql-select-missing
      [:select-missing
       [nil
        [[:prepare "select id, rev, map, val from datomic_kvs where id = ?"]
         [:set-object 1 "missing"] :execute-query :result-next
         :result-close :statement-close :connection-close]]]
      select-missing)
    (require-equal!
      :sql-delete
      [:delete
       [2
        [[:prepare "delete from datomic_kvs where id = ?"]
         [:set-object 1 "gone"] :execute-update
         :statement-close :connection-close]]]
      delete-result)
    (require-equal!
      :sql-execute-commands
      [:execute-commands
       [nil
        [[:prepare "one"] [:execute "one"] :statement-close
         [:prepare "two"] [:execute "two"] :statement-close
         [:prepare "three"] [:execute "three"] :statement-close]]]
      execute-commands)
    (require-equal!
      :sql-execute-failure
      [:execute-failure
       [[:sql-exception "stage2 update failure" "08006" 71]
        [[:prepare "delete from datomic_kvs where id = ?"]
         [:set-object 1 "failure"] :execute-update
         :statement-close :connection-close]]]
      execute-failure)
    (require-equal!
      :sql-prepare-failure
      [:prepare-failure
       [[:sql-exception "stage2 prepare failure" "08006" 72]
        [[:prepare "delete from datomic_kvs where id = ?"]
         :connection-close]]]
      prepare-failure)
    result))

(defn validation-fixture [mode]
  (let [events (atom [])
        result-set
        (interface-proxy
          ResultSet
          (fn [method-name _]
            (case method-name
              "next" (do
                       (swap! events conj :result-next)
                       (= mode :success))
              (fail! "unexpected ResultSet method" {:method method-name}))))
        statement
        (interface-proxy
          PreparedStatement
          (fn [method-name _]
            (case method-name
              "executeQuery"
              (do
                (swap! events conj :statement-execute)
                (if (= mode :execute-error)
                  (throw (SQLException. "execute failed" "08006" 91))
                  result-set))
              "close" (swap! events conj :statement-close)
              (fail! "unexpected PreparedStatement method"
                     {:method method-name}))))
        connection
        (interface-proxy
          Connection
          (fn [method-name argv]
            (case method-name
              "prepareStatement"
              (do
                (swap! events conj [:prepare (first argv)])
                statement)
              "close" (swap! events conj :connection-close)
              (fail! "unexpected Connection method" {:method method-name}))))]
    {:events events :connection connection}))

(defn try-validation-case [mode]
  (let [{:keys [events connection]} (validation-fixture mode)
        alarms (atom [])
        result
        (with-redefs
          [config/property (fn [property]
                             (case property
                               "datomic.sqlValidationQuery" nil
                               (fail! "unexpected config property"
                                      {:property property})))
           sql/connect (fn [_]
                         (if (= mode :connect-error)
                           (throw (SQLException.
                                    "connect failed" "08001" 92))
                           connection))
           monitor/alarm (fn [& values]
                           (swap! alarms conj (vec values))
                           nil)]
          (kv-sql-ext/try-validation-query
            "jdbc:postgresql://invalid/stage2" {:probe mode}))]
    [result @events @alarms]))

(defn try-validation-result []
  (let [result [[:success (try-validation-case :success)]
                [:empty (try-validation-case :empty)]
                [:execute-error (try-validation-case :execute-error)]
                [:connect-error (try-validation-case :connect-error)]]]
    (require-equal!
      :try-validation-query
      [[:success
        [nil
         [[:prepare "select 1"] :statement-execute :result-next
          :statement-close :connection-close]
         []]]
       [:empty
        [nil
         [[:prepare "select 1"] :statement-execute :result-next
          :statement-close :connection-close]
         [[:SQLValidationQueryFailed]]]]
       [:execute-error
        [nil
         [[:prepare "select 1"] :statement-execute
          :statement-close :connection-close]
         [[:SQLValidationQueryFailed]]]]
       [:connect-error
        [nil [] [[:SQLValidationQueryFailed]]]]]
      result)
    result))

(defn validation-selection-result []
  (let [result
        [(kv-sql-ext/provider "jdbc:postgresql://localhost/example")
         (kv-sql-ext/provider "jdbc:oracle:thin:@localhost:1521/example")
         (kv-sql-ext/provider "not-a-jdbc-url")
         (kv-sql-ext/validation-query*
           "jdbc:postgresql://localhost/example")
         (kv-sql-ext/validation-query*
           "jdbc:oracle:thin:@localhost:1521/example")
         (with-redefs [config/property (constantly nil)]
           (kv-sql-ext/validation-query
             "jdbc:postgresql://localhost/example"))
         (with-redefs [config/property (fn [property]
                                         (if (= property
                                                "datomic.sqlValidationQuery")
                                           "select stage2"
                                           nil))]
           (kv-sql-ext/validation-query
             "jdbc:oracle:thin:@localhost:1521/example"))]]
    (require-equal!
      :validation-selection
      ["postgresql" "oracle" nil "select 1" "select 1 from dual"
       "select 1" "select stage2"]
      result)
    result))

(defn create-datasource-result []
  (let [validation-call (atom [])
        conf {:sql-url "jdbc:postgresql://invalid/stage2"
              :sql-user "stage2-user"
              :sql-password "stage2-password"
              :sql-driver-class "org.postgresql.Driver"
              :sql-driver-params "ssl=false"
              :sql-initial-size 3}
        spec
        (with-redefs
          [config/property
           (fn [property]
             (case property
               "datomic.sqlValidationQuery" nil
               "datomic.heartbeatIntervalMsec" 1234
               (fail! "unexpected datasource config property"
                      {:property property})))
           kv-sql-ext/try-validation-query
           (fn [sql-url actual-spec]
             (swap! validation-call conj [sql-url actual-spec])
             nil)]
          (kv-sql-ext/create-datasource conf))
        ^DataSource datasource (:datasource spec)
        pool-properties (.getPoolProperties datasource)
        result
        [(instance? DataSource datasource)
         (.getUrl datasource)
         (.getValidationQuery datasource)
         (.getValidationInterval datasource)
         (.isTestOnBorrow datasource)
         (.getInitialSize datasource)
         (.getDriverClassName datasource)
         (.getUsername datasource)
         (.getPassword pool-properties)
         (get (into {} (.getDbProperties pool-properties)) "ssl")
         (= 1 (count @validation-call))
         (= (:sql-url conf) (ffirst @validation-call))
         (identical? spec (second (first @validation-call)))
         (identical?
           spec
           (with-redefs
             [config/property (constantly nil)
              kv-sql-ext/try-validation-query (fn [& _] nil)]
             (kv-sql-ext/create-datasource conf)))]]
    (try
      (require-equal!
        :create-datasource
        [true "jdbc:postgresql://invalid/stage2" "select 1" 1234 true 3
         "org.postgresql.Driver" "stage2-user" "stage2-password" "false"
         true true true true]
        result)
      result
      (finally
        (.close datasource)))))

(defn fallback-datasource-result []
  (let [sql-url "jdbc:stage2:fallback"
        driver
        (interface-proxy
          Driver
          (fn [method-name argv]
            (case method-name
              "acceptsURL" (= sql-url (first argv))
              "connect" nil
              "getMajorVersion" 1
              "getMinorVersion" 0
              "jdbcCompliant" false
              "getPropertyInfo" (make-array DriverPropertyInfo 0)
              "getParentLogger" (java.util.logging.Logger/getGlobal)
              (fail! "unexpected Driver method" {:method method-name}))))
        validation-call (atom [])
        datasource-ref (atom nil)]
    (DriverManager/registerDriver ^Driver driver)
    (try
      (let [conf {:sql-url sql-url}
            spec
            (with-redefs
              [config/property
               (fn [property]
                 (case property
                   "datomic.sqlValidationQuery" nil
                   "datomic.heartbeatIntervalMsec" 5678
                   (fail! "unexpected fallback datasource property"
                          {:property property})))
               kv-sql-ext/try-validation-query
               (fn [actual-url actual-spec]
                 (swap! validation-call conj [actual-url actual-spec])
                 nil)]
              (kv-sql-ext/create-datasource conf))
            ^DataSource datasource (:datasource spec)
            _ (reset! datasource-ref datasource)
            result
            [(.getUrl datasource)
             (.getInitialSize datasource)
             (.getValidationQuery datasource)
             (.getValidationInterval datasource)
             (= (.getName (class driver)) (.getDriverClassName datasource))
             (= 1 (count @validation-call))
             (= sql-url (ffirst @validation-call))
             (identical? spec (second (first @validation-call)))]]
        (require-equal!
          :fallback-datasource
          [sql-url 2 "select 1" 5678 true true true true]
          result)
        result)
      (finally
        (when-let [^DataSource datasource @datasource-ref]
          (.close datasource))
        (DriverManager/deregisterDriver ^Driver driver)))))

(defn invalid-config-result []
  (try
    (kv-sql-ext/cluster-conf->spec {})
    [:not-thrown]
    (catch IllegalArgumentException e
      (let [data (ex-data e)]
        [(:db/error data)
         (:cognitect.anomalies/category data)
         (:cognitect.anomalies/message data)]))))

(defn cluster-config-result []
  (let [datasource (Object.)
        callable (reify Callable (call [_] :factory-connection))
        data-source-spec (kv-sql-ext/cluster-conf->spec
                           {:data-source datasource})
        data-source-precedence (kv-sql-ext/cluster-conf->spec
                                 {:data-source datasource
                                  :factory callable})
        factory-spec (kv-sql-ext/cluster-conf->spec {:factory callable})
        sql-seen (atom nil)
        sql-conf {:sql-url "jdbc:postgresql://invalid/stage2"
                  :data-source datasource
                  :factory callable}
        sql-spec
        (with-redefs [kv-sql-ext/create-datasource
                      (fn [conf]
                        (reset! sql-seen conf)
                        {:created (:sql-url conf)})]
          (kv-sql-ext/cluster-conf->spec sql-conf))
        wrapper
        (with-redefs [kv-sql/from-spec
                      (fn [spec]
                        [:wrapped (identical? datasource (:datasource spec))])]
          (kv-sql-ext/kv-sql {:data-source datasource}))
        factory-zero
        (normalized-outcome #((:factory factory-spec)) identity)
        sql-connect-factory
        (normalized-outcome #(sql/connect factory-spec) identity)
        result
        [(identical? datasource (:datasource data-source-spec))
         (identical? datasource (:datasource data-source-precedence))
         ((:factory factory-spec) :ignored)
         sql-spec
         (= sql-conf @sql-seen)
         wrapper
         factory-zero
         sql-connect-factory
         (invalid-config-result)]]
    (require-equal!
      :cluster-config
      [true true :factory-connection
       {:created "jdbc:postgresql://invalid/stage2"}
       true [:wrapped true]
       [:throw "clojure.lang.ArityException"]
       [:throw "clojure.lang.ArityException"]
       [:db.error/invalid-sql-connection
        :cognitect.anomalies/incorrect
        "Must supply jdbc url in uri, or DataSource or Callable<Connection> in protocolObject arg to Peer.connect"]]
      result)
    result))

(defn singleton-create-datasource-outcome []
  (let [datasource-ref (atom nil)
        conf {:sql-url "jdbc:stage2:singleton"
              :sql-driver-class "stage2.SingletonDriver"}]
    (try
      (normalized-outcome
        #(with-redefs
           [config/property
            (fn [property]
              (case property
                "datomic.sqlValidationQuery" nil
                "datomic.heartbeatIntervalMsec" 1
                nil))
            kv-sql-ext/try-validation-query (fn [& _] nil)]
           (let [spec (kv-sql-ext/create-datasource (list conf))]
             (reset! datasource-ref (:datasource spec))
             spec))
        (fn [spec]
          [(instance? DataSource (:datasource spec))
           (= (:sql-url conf) (.getUrl ^DataSource (:datasource spec)))]))
      (finally
        (when-let [^DataSource datasource @datasource-ref]
          (.close datasource))))))

(defn sql-connect-singleton-outcome []
  (let [{:keys [connection]} (sql-core-fixture {})]
    (normalized-outcome
      #(sql/connect (list {:factory (fn [] connection)}))
      #(identical? connection %))))

(defn sql-update-with-nulls-singleton-outcome []
  (let [{:keys [events spec]} (sql-core-fixture {:update-result 3})]
    [(normalized-outcome
       #(sql/update-with-nulls
          spec "pod" 1 (list {:rev 2 :map nil :val nil}))
       identity)
     @events]))

(defn sql-insert-with-nulls-singleton-outcome []
  (let [{:keys [events spec]} (sql-core-fixture {:update-result 4})]
    [(normalized-outcome
       #(sql/insert-with-nulls
          spec (list {:id "item" :rev 2 :map nil :val nil}))
       identity)
     @events]))

(defn compiler-domain-result []
  (let [datasource (Object.)
        store (kv-sql/from-spec :stage2/compiler-domain)
        cluster-outcome
        (normalized-outcome
          #(kv-sql-ext/cluster-conf->spec
             (list {:data-source datasource}))
          #(identical? datasource (:datasource %)))
        datasource-outcome (singleton-create-datasource-outcome)
        put-outcome
        (with-redefs
          [sql/insert (fn [& _] 1)
           sql/update (fn [& _] 1)]
          (normalized-outcome
            #(kv-store/put store (list {:id :singleton :rev 1}))
            identity))]
    [[:cluster-conf-singleton-seq cluster-outcome]
     [:create-datasource-singleton-seq datasource-outcome]
     [:kv-put-singleton-seq put-outcome]
     [:sql-connect-singleton-seq (sql-connect-singleton-outcome)]
     [:sql-update-with-nulls-singleton-seq
      (sql-update-with-nulls-singleton-outcome)]
     [:sql-insert-with-nulls-singleton-seq
      (sql-insert-with-nulls-singleton-outcome)]]))

(quiet-logging!)

(let [result [[:constraint-and-retry (constraint-and-retry-result)]
              [:kv-sql (kv-sql-result)]
              [:sql-core (sql-core-result)]
              [:validation-selection (validation-selection-result)]
              [:try-validation-query (try-validation-result)]
              [:create-datasource (create-datasource-result)]
              [:fallback-datasource (fallback-datasource-result)]
              [:cluster-config (cluster-config-result)]]]
  (println "STAGE2_SQL_OVERLAP_SUPPORTED_RESULT" (pr-str result))
  (println "STAGE2_SQL_OVERLAP_COMPILER_DOMAIN_RESULT"
           (pr-str (compiler-domain-result))))

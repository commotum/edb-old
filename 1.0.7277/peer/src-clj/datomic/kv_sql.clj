(do
  (clojure.core/in-ns 'datomic.kv-sql)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['get])
      (clojure.core/require
        ['datomic.kv-store :as 'kv]
        ['datomic.io :as 'io]
        ['datomic.require :as 'req]
        ['datomic.error :as 'error]
        ['datomic.slf4j :as 'logger]
        ['datomic.sql :as 'sql])
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.sql.SQLException)))
  (when-not (.equals 'datomic.kv-sql 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.kv-sql))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['get])
        (clojure.core/require
          ['datomic.kv-store :as 'kv]
          ['datomic.io :as 'io]
          ['datomic.require :as 'req]
          ['datomic.error :as 'error]
          ['datomic.slf4j :as 'logger]
          ['datomic.sql :as 'sql])
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.sql.SQLException))))
  (set! *warn-on-reflection* true)
  (req/maybe-require 'datomic.kv-sql-ext)
  (defn constraint-violation? ([e] (.startsWith (.getSQLState ^java.sql.SQLException e) "23")))
  (deftype
    KVSql
    [spec]
    datomic.kv_store.KVStore
    (close [this] nil)
    (delete [this key consistent?] (do (sql/delete spec key) :ok))
    (get
      [this key consistent?]
      (let [temp__5457__auto__ (sql/select spec key)]
        (when temp__5457__auto__
          (let [ret temp__5457__auto__
                ret (let [temp__5455__auto__ (:val ret)]
                      (if temp__5455__auto__
                        (let [v temp__5455__auto__] (assoc ret :v (ByteBuffer/wrap ^bytes v)))
                        ret))
                m (and (:map ret) (read-string (:map ret)))
                ret (merge (dissoc ret :map :val) m)]
            ret))))
    (put
      [this v_map]
      (when (let [map__11576 v_map
                  map__11576 (if (seq? map__11576)
                               (clojure.lang.PersistentHashMap/create (seq map__11576))
                               map__11576)
                  id (clojure.core/get map__11576 :id)
                  rev (clojure.core/get map__11576 :rev)
                  v (clojure.core/get map__11576 :v)
                  ensure (clojure.core/get map__11576 :ensure)
                  m (dissoc v_map :id :rev :v :ensure)
                  val_map {:id id,
                           :rev rev,
                           :map (when (java.lang.Integer/valueOf (int (count m))) (pr-str m)),
                           :val (when v (io/alias-buf-bytes v))}]
              (cond
                (:rev ensure) (not (zero? (sql/update spec id (:rev ensure) (dissoc val_map :id))))
                :else (do
                        (try
                          (do (sql/insert spec val_map) true)
                          (catch
                            java.sql.SQLException
                            ex
                            (if (constraint-violation? ex)
                              (do
                                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.kv-sql")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process
                                        {:event :kv-sql/put-failed,
                                         :id id,
                                         :desc (.getMessage ^java.lang.Throwable ex)}))
                                    nil)
                                  nil)
                                false)
                              (do (throw ^java.lang.Throwable ex) nil)))))))
        :ok)))
  (clojure.core/import 'datomic.kv_sql.KVSql)
  (defn ->KVSql ([spec] (datomic.kv_sql.KVSql. spec)))
  (defn from-spec ([spec] (datomic.kv_sql.KVSql. spec)))
  (def LOGIN_FAILED 28000)
  (reset-meta! #'LOGIN_FAILED (assoc {:const true, :column 1} :name 'LOGIN_FAILED :ns *ns*))
  (extend
    java.sql.SQLException
    kv/Retryable
    {:retryable?
     (fn fn__11587
       ([x]
         (let [code (.getErrorCode ^java.sql.SQLException x)]
           (not= (java.lang.Integer/valueOf (int code)) 28000))))}))
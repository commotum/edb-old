(do
  (clojure.core/in-ns 'datomic.slf4j)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.error :as 'error]
        ['datomic.math :as 'math]
        ['clojure.edn :as 'edn]
        ['clojure.string :as 'str])
      (clojure.core/import 'org.slf4j.Logger)
      (clojure.core/import 'org.slf4j.LoggerFactory)))
  (when-not (.equals 'datomic.slf4j 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.slf4j))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.error :as 'error]
          ['datomic.math :as 'math]
          ['clojure.edn :as 'edn]
          ['clojure.string :as 'str])
        (clojure.core/import 'org.slf4j.Logger)
        (clojure.core/import 'org.slf4j.LoggerFactory))))
  (set! *warn-on-reflection* true)
  (def pid
   (edn/read-string
     (str/replace (.getName (java.lang.management.ManagementFactory/getRuntimeMXBean)) #"@.*" "")))
  (when-not (java.lang.System/getProperty "net.spy.log.LoggerImpl")
    (java.lang.System/setProperty
      "net.spy.log.LoggerImpl"
      "datomic.spy.memcached.compat.log.Log4JLogger"))
  (org.slf4j.MDC/put "pid" (str pid))
  (defn print-safely ([o] (binding [*print-length* 100] (pr-str o))))
  (def event->timing
   {:ddb-values/put-value-chunk :DdbPutChunkMsec,
    :io/gunzip-buffer :DecompressMsec,
    :peer/accept-new :PeerAcceptNewMsec,
    :index/write-val :IndexWriteMsec,
    :index/build-fulltext :CreateFulltextIndexMsec,
    :io/gzip-buffer :CompressMsec,
    :peer/integrate-lucene :PeerIntegrateLuceneMsec,
    :log/add-next :LogWriteMsec,
    :kv-cluster/create-val :StoragePutMsec,
    :clusterfs/write-val :FulltextWriteMsec,
    :index/add-avet :AddIndexMsec,
    :kv-cluster/get-pod :PodGetMsec,
    :kv-cluster/get-val :StorageGetMsec,
    :tx/process :TransactionMsec,
    :fressian/defressian :DefressianMsec,
    :fressian/fressian :FressianMsec,
    :kv-cluster/update-pod :PodUpdateMsec,
    :db/accept-index :AcceptIndexMsec,
    :update/create-index :CreateEntireIndexMsec,
    :clusterfs/create-fs :StorageCreateFSMsec,
    :db/add-fulltext :DbAddFulltextMsec,
    :fressian/decompress :DecompressFressianMsec})
  (defn process
    ([msg]
      (let [msg (if (map? msg) msg {:message msg})]
        (print-safely
          (assoc msg :pid pid :tid (long (.getId (java.lang.Thread/currentThread))))))))
  (defn caused-by
    ([logger t]
      (loop [t (.getCause ^java.lang.Throwable t)]
        (when t
          (.warn ^org.slf4j.Logger logger "... caused by ..." ^java.lang.Throwable t)
          (recur (.getCause ^java.lang.Throwable t))))))
  (defn enabled-method ([level] (symbol (str ".is" (str/capitalize (name level)) "Enabled"))))
  (reset-meta!
    #'enabled-method
    (assoc
      {:private true, :arglists (clojure.core/list ['level]), :column 1}
      :name
      'enabled-method
      :ns
      *ns*))
  (defn log-expr
    ([level msg ex]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'logger)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'org.slf4j.LoggerFactory/getLogger)
                        (clojure.core/list (str *ns*)))))
                  (clojure.core/list 'ex)
                  (clojure.core/list ex)))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/when)
                (-> (enabled-method level)
                 (clojure.core/list)
                 (concat (clojure.core/list 'logger))
                 (seq)
                 (clojure.core/list))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list '.)
                      (clojure.core/list 'logger)
                      (clojure.core/list level)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.slf4j/process)
                            (clojure.core/list msg))))
                      (clojure.core/list 'ex))))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'datomic.slf4j/caused-by)
                      (clojure.core/list 'logger)
                      (clojure.core/list 'ex)))))))
          (clojure.core/list nil))))
    ([level msg]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'logger)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'org.slf4j.LoggerFactory/getLogger)
                        (clojure.core/list (str *ns*)))))))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'clojure.core/when)
                (-> (enabled-method level)
                 (clojure.core/list)
                 (concat (clojure.core/list 'logger))
                 (seq)
                 (clojure.core/list))
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list '.)
                      (clojure.core/list 'logger)
                      (clojure.core/list level)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.slf4j/process)
                            (clojure.core/list msg))))))))))
          (clojure.core/list nil)))))
  (reset-meta!
    #'log-expr
    (assoc
      {:private true, :arglists (clojure.core/list ['level 'msg] ['level 'msg 'ex]), :column 1}
      :name
      'log-expr
      :ns
      *ns*))
  (defn trace
    ([&form &env msg ex] (log-expr 'trace msg ex))
    ([&form &env msg] (log-expr 'trace msg)))
  (.setMacro #'trace)
  (defn debug
    ([&form &env msg ex] (log-expr 'debug msg ex))
    ([&form &env msg] (log-expr 'debug msg)))
  (.setMacro #'debug)
  (defn info ([&form &env msg ex] (log-expr 'info msg ex)) ([&form &env msg] (log-expr 'info msg)))
  (.setMacro #'info)
  (defn warn ([&form &env msg ex] (log-expr 'warn msg ex)) ([&form &env msg] (log-expr 'warn msg)))
  (.setMacro #'warn)
  (defn error
    ([&form &env msg ex] (log-expr 'error msg ex))
    ([&form &env msg] (log-expr 'error msg)))
  (.setMacro #'error)
  (defn exception-string
    ([t]
      (let [s (java.io.StringWriter.) p (java.io.PrintWriter. ^java.io.Writer s)]
        (.printStackTrace ^java.lang.Throwable t ^java.io.PrintWriter p)
        (str s))))
  (defn log-agent-error
    ([agent error] (^clojure.lang.IFn error "Unexpected error on agent." error)))
  (defn format-as-msec ([nsec] (math/round (/ (double nsec) 1000000) 3)))
  (defn format-as-musec ([nsec] (math/round (/ (double nsec) 1000) 3)))
  (defn metric-expr
    ([event msec]
      (let [temp__5457__auto__ (event->timing event)]
        (when temp__5457__auto__
          (let [kw temp__5457__auto__]
            (seq
              (concat
                (clojure.core/list 'datomic.monitor/add-stat)
                (clojure.core/list kw)
                (clojure.core/list msec))))))))
  (def log-end-phase-only-events #{:kv-cluster/create-val})
  (defn log-time
    ([&form &env m & body]
      (let [m (if (keyword? m) {:event m} m)
            level (symbol "datomic.slf4j" (name (get m :level :debug)))
            _ (when-not level
                (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'level))))
                nil)
            m (dissoc m :level)
            event (:event m)
            log_begin? (not (contains? log-end-phase-only-events event))
            msym (gensym "m_")
            elapsed (gensym "elapsed_")
            msec (gensym "msec_")]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list msym)
                    (clojure.core/list m)
                    (if log_begin?
                      (apply
                        vector
                        (seq
                          (concat
                            (clojure.core/list '___8980__auto__)
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list level)
                                  (clojure.core/list
                                    (seq
                                      (concat
                                        (clojure.core/list 'clojure.core/assoc)
                                        (clojure.core/list msym)
                                        (clojure.core/list :phase)
                                        (clojure.core/list :begin))))))))))
                      [])
                    (clojure.core/list 'start__8981__auto__)
                    (clojure.core/list
                      (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                    (clojure.core/list 'result__8982__auto__)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'try)
                          (clojure.core/list
                            (apply
                              hash-map
                              (seq
                                (concat
                                  (clojure.core/list :returned)
                                  (clojure.core/list
                                    (seq (concat (clojure.core/list 'do) body)))))))
                          (clojure.core/list
                            (seq
                              (concat
                                (clojure.core/list 'catch)
                                (clojure.core/list 'java.lang.Throwable)
                                (clojure.core/list 't__8983__auto__)
                                (clojure.core/list
                                  (apply
                                    hash-map
                                    (seq
                                      (concat
                                        (clojure.core/list :threw)
                                        (clojure.core/list 't__8983__auto__)))))))))))
                    (clojure.core/list elapsed)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'clojure.core/-)
                          (clojure.core/list
                            (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                          (clojure.core/list 'start__8981__auto__))))
                    (clojure.core/list msec)
                    (clojure.core/list
                      (seq
                        (concat
                          (clojure.core/list 'datomic.slf4j/format-as-msec)
                          (clojure.core/list elapsed))))))))
            (clojure.core/list (metric-expr event msec))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'clojure.core/let)
                  (clojure.core/list
                    (apply
                      vector
                      (seq
                        (concat
                          (clojure.core/list 'endmsg__8984__auto__)
                          (clojure.core/list
                            (seq
                              (concat
                                (clojure.core/list 'clojure.core/merge)
                                (clojure.core/list
                                  (seq
                                    (concat
                                      (clojure.core/list 'clojure.core/assoc)
                                      (clojure.core/list msym)
                                      (clojure.core/list :msec)
                                      (clojure.core/list msec)
                                      (clojure.core/list :phase)
                                      (clojure.core/list :end))))
                                (clojure.core/list
                                  (seq
                                    (concat
                                      (clojure.core/list 'clojure.core/when)
                                      (clojure.core/list
                                        (seq
                                          (concat
                                            (clojure.core/list :threw)
                                            (clojure.core/list 'result__8982__auto__))))
                                      (clojure.core/list
                                        (apply
                                          hash-map
                                          (seq
                                            (concat
                                              (clojure.core/list :threw)
                                              (clojure.core/list
                                                (seq
                                                  (concat
                                                    (clojure.core/list 'clojure.core/class)
                                                    (clojure.core/list
                                                      (seq
                                                        (concat
                                                          (clojure.core/list :threw)
                                                          (clojure.core/list
                                                            'result__8982__auto__)))))))))))))))))))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list level)
                        (clojure.core/list 'endmsg__8984__auto__)))))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'if)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/contains?)
                        (clojure.core/list 'result__8982__auto__)
                        (clojure.core/list :returned))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list :returned)
                        (clojure.core/list 'result__8982__auto__))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'throw)
                        (clojure.core/list
                          (seq
                            (concat
                              (clojure.core/list :threw)
                              (clojure.core/list 'result__8982__auto__)))))))))))))))
  (.setMacro #'log-time)
  (defn dont-log-time ([&form &env m & body] (seq (concat (clojure.core/list 'do) body))))
  (.setMacro #'dont-log-time)
  (defn sanitize-uri
    ([uri]
      (str/replace
        (str/replace uri #"(aws_access_key_id=)([^&]+)" "$1__SANITIZED__")
        #"(aws_secret_key=)([^&]+)"
        "$1--SANITIZED--")))
  (defn print-and-warn
    ([msg]
      (println msg)
      (let [logger (LoggerFactory/getLogger "datomic.slf4j")]
        (when (.isWarnEnabled ^org.slf4j.Logger logger)
          (.warn ^org.slf4j.Logger logger (process msg))
          nil)
        nil)))
  (reset!
    error/reporter
    (fn fn__8989
      ([e]
        (let [logger (LoggerFactory/getLogger "datomic.slf4j") ex e]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger ^java.lang.String (process "Caught exception") ex)
            (caused-by logger ex))
          nil))))
  (defn log-uncaught-exceptions
    ([]
      (java.lang.Thread/setDefaultUncaughtExceptionHandler
        (reify
          java.lang.Thread$UncaughtExceptionHandler
          (^void uncaughtException
            [this ^java.lang.Thread thread ^java.lang.Throwable throwable]
            (do
              (let [logger (LoggerFactory/getLogger "datomic.slf4j") ex throwable]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    ^java.lang.String (process "Uncaught exception")
                    ^java.lang.Throwable ex)
                  (caused-by logger ex))
                nil)
              nil))))
      nil))
  (defmulti redact (fn fn__8995 ([data redact?] (class data))))
  (defmethod
    redact
    java.util.Map
    fn__9000
    ([data redact?]
      (reduce-kv
        (fn fn__9001 ([m k v] (assoc m k (if (^clojure.lang.IFn redact? k) "<redacted>" v))))
        {}
        data)))
  (defmethod
    redact
    java.util.Collection
    fn__9005
    ([data redact?] (mapv (fn fn__9006 ([p1__9004#] (redact p1__9004# redact?))) data)))
  (defmethod redact :default fn__9009 ([data redact?] data)))

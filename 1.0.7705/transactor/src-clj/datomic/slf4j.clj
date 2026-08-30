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
  (.setMeta (clojure.lang.RT/var "datomic.slf4j" "pid") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.slf4j" "pid")
    (edn/read-string
      (str/replace
        (.getName (java.lang.management.ManagementFactory/getRuntimeMXBean))
        #"@.*"
        "")))
  (when-not (java.lang.System/getProperty "net.spy.log.LoggerImpl")
    (java.lang.System/setProperty
      "net.spy.log.LoggerImpl"
      "datomic.spy.memcached.compat.log.Log4JLogger"))
  (org.slf4j.MDC/put "pid" (str pid))
  (defn print-safely ([o] (binding [*print-length* 100] (pr-str o))))
  (reset-meta!
    #'print-safely
    (assoc
      {:arglists (clojure.core/list (.withMeta ['o] {:tag 'java.lang.String})), :column (int 1)}
      :name
      'print-safely
      :ns
      *ns*))
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
  (reset-meta! #'event->timing (assoc {:column (int 1)} :name 'event->timing :ns *ns*))
  (defn process
    ([msg]
      (let [msg (if (map? msg) msg {:message msg})]
        (print-safely
          (assoc msg :pid pid :tid (long (.getId (java.lang.Thread/currentThread))))))))
  (reset-meta!
    #'process
    (assoc
      {:arglists (clojure.core/list (.withMeta ['msg] {:tag 'java.lang.String})), :column (int 1)}
      :name
      'process
      :ns
      *ns*))
  (defn caused-by
    ([logger t]
      (loop [t (.getCause ^java.lang.Throwable t)]
        (when t
          (.warn ^org.slf4j.Logger logger "... caused by ..." ^java.lang.Throwable t)
          (recur (.getCause ^java.lang.Throwable t))))))
  (reset-meta!
    #'caused-by
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'logger {:tag 'Logger}) (.withMeta 't {:tag 'Throwable})]),
       :column (int 1)}
      :name
      'caused-by
      :ns
      *ns*))
  (defn enabled-method ([level] (symbol (str ".is" (str/capitalize (name level)) "Enabled"))))
  (reset-meta!
    #'enabled-method
    (assoc
      {:private true, :arglists (clojure.core/list ['level]), :column (int 1)}
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
      {:private true,
       :arglists (clojure.core/list ['level 'msg] ['level 'msg 'ex]),
       :column (int 1)}
      :name
      'log-expr
      :ns
      *ns*))
  (def trace
   (fn trace
     ([&form &env msg ex] (log-expr 'trace msg ex))
     ([&form &env msg] (log-expr 'trace msg))))
  (reset-meta!
    #'trace
    (assoc
      {:arglists (clojure.core/list ['msg] ['msg 'ex]), :column (int 1)}
      :name
      'trace
      :ns
      *ns*))
  (.setMacro #'trace)
  (def debug
   (fn debug
     ([&form &env msg ex] (log-expr 'debug msg ex))
     ([&form &env msg] (log-expr 'debug msg))))
  (reset-meta!
    #'debug
    (assoc
      {:arglists (clojure.core/list ['msg] ['msg 'ex]), :column (int 1)}
      :name
      'debug
      :ns
      *ns*))
  (.setMacro #'debug)
  (def info
   (fn info ([&form &env msg ex] (log-expr 'info msg ex)) ([&form &env msg] (log-expr 'info msg))))
  (reset-meta!
    #'info
    (assoc
      {:arglists (clojure.core/list ['msg] ['msg 'ex]), :column (int 1)}
      :name
      'info
      :ns
      *ns*))
  (.setMacro #'info)
  (def warn
   (fn warn ([&form &env msg ex] (log-expr 'warn msg ex)) ([&form &env msg] (log-expr 'warn msg))))
  (reset-meta!
    #'warn
    (assoc
      {:arglists (clojure.core/list ['msg] ['msg 'ex]), :column (int 1)}
      :name
      'warn
      :ns
      *ns*))
  (.setMacro #'warn)
  (def error
   (fn error
     ([&form &env msg ex] (log-expr 'error msg ex))
     ([&form &env msg] (log-expr 'error msg))))
  (reset-meta!
    #'error
    (assoc
      {:arglists (clojure.core/list ['msg] ['msg 'ex]), :column (int 1)}
      :name
      'error
      :ns
      *ns*))
  (.setMacro #'error)
  (defn exception-string
    ([t]
      (let [s (java.io.StringWriter.) p (java.io.PrintWriter. ^java.io.Writer s)]
        (.printStackTrace ^java.lang.Throwable t ^java.io.PrintWriter p)
        (str s))))
  (reset-meta!
    #'exception-string
    (assoc
      {:arglists (clojure.core/list [(.withMeta 't {:tag 'Throwable})]), :column (int 1)}
      :name
      'exception-string
      :ns
      *ns*))
  (defn log-agent-error
    ([agent error] (^clojure.lang.IFn error "Unexpected error on agent." error)))
  (reset-meta!
    #'log-agent-error
    (assoc
      {:arglists (clojure.core/list ['agent 'error]), :column (int 1)}
      :name
      'log-agent-error
      :ns
      *ns*))
  (defn format-as-msec ([nsec] (math/round (/ (double nsec) 1000000) 3)))
  (reset-meta!
    #'format-as-msec
    (assoc
      {:arglists (clojure.core/list ['nsec]), :column (int 1)}
      :name
      'format-as-msec
      :ns
      *ns*))
  (defn format-as-musec ([nsec] (math/round (/ (double nsec) 1000) 3)))
  (reset-meta!
    #'format-as-musec
    (assoc
      {:arglists (clojure.core/list ['nsec]), :column (int 1)}
      :name
      'format-as-musec
      :ns
      *ns*))
  (defn metric-expr
    ([event msec]
      (let [temp__5825__auto__ (event->timing event)]
        (when temp__5825__auto__
          (let [kw temp__5825__auto__]
            (seq
              (concat
                (clojure.core/list 'datomic.monitor/add-stat)
                (clojure.core/list kw)
                (clojure.core/list msec))))))))
  (reset-meta!
    #'metric-expr
    (assoc
      {:arglists (clojure.core/list ['event 'msec]), :column (int 1)}
      :name
      'metric-expr
      :ns
      *ns*))
  (def log-end-phase-only-events #{:kv-cluster/create-val})
  (reset-meta!
    #'log-end-phase-only-events
    (assoc {:column (int 1)} :name 'log-end-phase-only-events :ns *ns*))
  (def log-time
   (fn log_time
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
                             (clojure.core/list '___8598__auto__)
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
                     (clojure.core/list 'start__8599__auto__)
                     (clojure.core/list
                       (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                     (clojure.core/list 'result__8600__auto__)
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
                                 (clojure.core/list 't__8601__auto__)
                                 (clojure.core/list
                                   (apply
                                     hash-map
                                     (seq
                                       (concat
                                         (clojure.core/list :threw)
                                         (clojure.core/list 't__8601__auto__)))))))))))
                     (clojure.core/list elapsed)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'clojure.core/-)
                           (clojure.core/list
                             (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                           (clojure.core/list 'start__8599__auto__))))
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
                           (clojure.core/list 'endmsg__8602__auto__)
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
                                             (clojure.core/list 'result__8600__auto__))))
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
                                                             'result__8600__auto__)))))))))))))))))))))
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list level)
                         (clojure.core/list 'endmsg__8602__auto__)))))))
             (clojure.core/list
               (seq
                 (concat
                   (clojure.core/list 'if)
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list 'clojure.core/contains?)
                         (clojure.core/list 'result__8600__auto__)
                         (clojure.core/list :returned))))
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list :returned)
                         (clojure.core/list 'result__8600__auto__))))
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list 'throw)
                         (clojure.core/list
                           (seq
                             (concat
                               (clojure.core/list :threw)
                               (clojure.core/list 'result__8600__auto__))))))))))))))))
  (reset-meta!
    #'log-time
    (assoc
      {:arglists (clojure.core/list ['m '& 'body]), :column (int 1)}
      :name
      'log-time
      :ns
      *ns*))
  (.setMacro #'log-time)
  (def dont-log-time
   (fn dont_log_time ([&form &env m & body] (seq (concat (clojure.core/list 'do) body)))))
  (reset-meta!
    #'dont-log-time
    (assoc
      {:arglists (clojure.core/list ['m '& 'body]), :column (int 1)}
      :name
      'dont-log-time
      :ns
      *ns*))
  (.setMacro #'dont-log-time)
  (defn sanitize-uri
    ([uri]
      (str/replace
        (str/replace uri #"(aws_access_key_id=)([^&]+)" "$1__SANITIZED__")
        #"(aws_secret_key=)([^&]+)"
        "$1--SANITIZED--")))
  (reset-meta!
    #'sanitize-uri
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'sanitize-uri :ns *ns*))
  (defn print-and-warn
    ([msg]
      (println msg)
      (let [logger (LoggerFactory/getLogger "datomic.slf4j")]
        (when (.isWarnEnabled ^org.slf4j.Logger logger)
          (.warn ^org.slf4j.Logger logger (process msg)))
        nil)))
  (reset-meta!
    #'print-and-warn
    (assoc {:arglists (clojure.core/list ['msg]), :column (int 1)} :name 'print-and-warn :ns *ns*))
  (reset!
    error/reporter
    (fn fn__8607
      ([e]
        (let [logger (LoggerFactory/getLogger "datomic.slf4j") ex e]
          (when (.isWarnEnabled ^org.slf4j.Logger logger)
            (.warn ^org.slf4j.Logger logger (process "Caught exception") ex)
            (caused-by logger ex))
          nil))))
  (defn log-uncaught-exceptions
    ([]
      (java.lang.Thread/setDefaultUncaughtExceptionHandler
        (if (instance?
              clojure.lang.IFn
              (reify
                java.lang.Thread$UncaughtExceptionHandler
                (^void uncaughtException
                  [this ^java.lang.Thread thread ^java.lang.Throwable throwable]
                  (do
                    (let [logger (LoggerFactory/getLogger "datomic.slf4j") ex throwable]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn
                          ^org.slf4j.Logger logger
                          (process "Uncaught exception")
                          ^java.lang.Throwable ex)
                        (caused-by logger ex))
                      nil)
                    nil))))
          (instance?
            java.lang.Thread$UncaughtExceptionHandler
            (reify
              java.lang.Thread$UncaughtExceptionHandler
              (^void uncaughtException
                [this ^java.lang.Thread thread ^java.lang.Throwable throwable]
                (do
                  (let [logger (LoggerFactory/getLogger "datomic.slf4j") ex throwable]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (process "Uncaught exception")
                        ^java.lang.Throwable ex)
                      (caused-by logger ex))
                    nil)
                  nil))))
          (reify
            java.lang.Thread$UncaughtExceptionHandler
            (^void uncaughtException
              [this ^java.lang.Thread thread ^java.lang.Throwable throwable]
              (do
                (let [logger (LoggerFactory/getLogger "datomic.slf4j") ex throwable]
                  (when (.isWarnEnabled ^org.slf4j.Logger logger)
                    (.warn
                      ^org.slf4j.Logger logger
                      (process "Uncaught exception")
                      ^java.lang.Throwable ex)
                    (caused-by logger ex))
                  nil)
                nil)))))
      nil))
  (reset-meta!
    #'log-uncaught-exceptions
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'log-uncaught-exceptions
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.slf4j" "redact") {:column (int 1)})
  (let [v__5813__auto__ #'redact]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.slf4j" "redact") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.slf4j" "redact")
        (clojure.lang.MultiFn.
          "redact"
          (fn fn__8613 ([data redact?] (class data)))
          :default
          #'clojure.core/global-hierarchy))
      #'redact))
  (defmethod
    redact
    java.util.Map
    fn__8618
    ([data redact?]
      (reduce-kv
        (fn fn__8619 ([m k v] (assoc m k (if (^clojure.lang.IFn redact? k) "<redacted>" v))))
        {}
        data)))
  (defmethod
    redact
    java.util.Collection
    fn__8623
    ([data redact?] (mapv (fn fn__8624 ([p1__8622#] (redact p1__8622# redact?))) data)))
  (defmethod redact :default fn__8627 ([data redact?] data)))
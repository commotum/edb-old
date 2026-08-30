(do
  (clojure.core/in-ns 'datomic.transactor-ext)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.set :as 'set]
        ['cognitect.nano-impl :as 'nano-impl]
        ['datomic.callback :as 'cb]
        ['datomic.config :as 'config]
        ['datomic.cloudwatch :as 'cw]
        ['datomic.config-ext :as 'cfext]
        ['datomic.aws-monitor :as 'awsm]
        ['datomic.slf4j :as 'logger]
        ['datomic.common :as 'common]
        ['datomic.cluster :as 'cluster]
        ['datomic.coordination :as 'coord]
        ['datomic.domain :as 'domain]
        ['datomic.error :as 'error]
        ['datomic.process :as 'process]
        ['datomic.monitor :as 'monitor]
        ['datomic.logrotate :as 'logrotate])))
  (when-not (.equals 'datomic.transactor-ext 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.transactor-ext))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.set :as 'set]
          ['cognitect.nano-impl :as 'nano-impl]
          ['datomic.callback :as 'cb]
          ['datomic.config :as 'config]
          ['datomic.cloudwatch :as 'cw]
          ['datomic.config-ext :as 'cfext]
          ['datomic.aws-monitor :as 'awsm]
          ['datomic.slf4j :as 'logger]
          ['datomic.common :as 'common]
          ['datomic.cluster :as 'cluster]
          ['datomic.coordination :as 'coord]
          ['datomic.domain :as 'domain]
          ['datomic.error :as 'error]
          ['datomic.process :as 'process]
          ['datomic.monitor :as 'monitor]
          ['datomic.logrotate :as 'logrotate]))))
  (set! *warn-on-reflection* true)
  (def start-logrotate
   (fn start_logrotate
     ([p__31815]
       (let [map__31816 p__31815
             map__31816 (if (seq? map__31816)
                          (if (next map__31816)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31816))
                            (if (seq map__31816) (first map__31816) {}))
                          map__31816)
             log_dir (get map__31816 :log-dir)
             creds (get map__31816 :creds)
             aws_s3_log_bucket_id (get map__31816 :aws-s3-log-bucket-id)
             log_path_fn (get map__31816 :log-path-fn)
             process (get map__31816 :process)]
         (when aws_s3_log_bucket_id
           (try
             (logrotate/probe creds aws_s3_log_bucket_id)
             (catch
               java.lang.Throwable
               t
               (error/raise
                 :transactor/config
                 "Unable to write to S3 bucket"
                 {:bucket aws_s3_log_bucket_id}
                 t)))
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.transactor-ext")]
             (when (.isInfoEnabled ^org.slf4j.Logger logger)
               (.info ^org.slf4j.Logger logger (logger/process "Logrotation started.")))
             nil)
           (let [interval (* (* 5 60) 1000)]
             (logrotate/watch-dir log_dir (long interval) aws_s3_log_bucket_id log_path_fn creds)
             (process/add-fail-handler
               process
               (fn fn__31819
                 ([]
                   (logrotate/zip-and-put-in-s3
                     log_dir
                     aws_s3_log_bucket_id
                     log_path_fn
                     creds
                     0))))))))))
  (reset-meta!
    #'start-logrotate
    (assoc
      {:arglists
       (clojure.core/list [{:keys ['log-dir 'creds 'aws-s3-log-bucket-id 'log-path-fn 'process]}]),
       :column (int 1)}
      :name
      'start-logrotate
      :ns
      *ns*))
  (defn start-ping-endpoint
    ([]
      (let [host (config/property "datomic.pingHost")
            port (config/property "datomic.pingPort")
            cores (.availableProcessors (java.lang.Runtime/getRuntime))
            cconn (or
                    (config/property "datomic.pingConcurrency")
                    (long (max 3 (inc (+ (quot cores 2) (quot cores 8))))))]
        (when (and host port)
          (try
            (do
              (nano-impl/create
                {:server
                 {:connection-concurrency cconn,
                  :bind-address {:host host, :port port},
                  :pending-ops-limit 10,
                  :processing-concurrency 1,
                  :ping-path "/health",
                  :bounding-timeout 15000},
                 :nano-services {:groups [], :ops []}})
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.transactor-ext")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process {:event :healthcheck-started, :host host, :port port})))
                nil))
            (catch
              java.lang.Throwable
              t
              (do
                (throw
                  (java.lang.RuntimeException.
                    (str "Unable to start ping endpoint " host ":" port)
                    ^java.lang.Throwable t))
                nil)))))))
  (reset-meta!
    #'start-ping-endpoint
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      'start-ping-endpoint
      :ns
      *ns*))
  (defn start-pro
    ([args]
      (start-ping-endpoint)
      (let [result (let [k__20246__auto__ (:license-key args)]
                     (try
                       (let [temp__5823__auto__ (clojure.edn/read-string
                                                  (let [vec__31826 [(str
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            41)))
                                                                      \S
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110))))
                                                                    (str
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            16)))
                                                                      \I
                                                                      \I
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            77)))
                                                                      \A
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      \B
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      \k
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            28)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            44)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            57)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            37)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            63)))
                                                                      \0
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      \A
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      \Q
                                                                      \8
                                                                      \A
                                                                      \M
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      \I
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
                                                                      \g
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      \i
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            89)))
                                                                      \Z
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            82)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            19)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            19)))
                                                                      \z
                                                                      \K
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            76)))
                                                                      \1
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            96)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            53)))
                                                                      \f
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            89)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            89)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            41)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            16)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            28)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            58)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            82)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      \6
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            82)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            15)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      \3
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            28)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            28)))
                                                                      \C
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            63)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            57)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            127)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            121)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            107)))
                                                                      \c
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            17)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      \f
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            114)))
                                                                      \y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      \m
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      \g
                                                                      \Y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            1)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            53)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            17)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            121)))
                                                                      \2
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
                                                                      \Y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            77)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            53)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            3)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            56)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      \c
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            107)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            87)))
                                                                      \f
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      \A
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      \R
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      \h
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            87)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            114)))
                                                                      \Z
                                                                      \D
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      \H
                                                                      \5
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      \3
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            99)))
                                                                      \S
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            59)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      \z
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            1)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            127)))
                                                                      \Z
                                                                      \F
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            1)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      \v
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            76)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      \F
                                                                      \D
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            58)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      \i
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      \W
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            8)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      \f
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      \B
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      \f
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            59)))
                                                                      \B
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            76)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      \n
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            44)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            41)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            89)))
                                                                      \o
                                                                      \U
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            89)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            63)))
                                                                      \q
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            62)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            8)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            127)))
                                                                      \Y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      \3
                                                                      \y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            56)))
                                                                      \u
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            29)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            91)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            15)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      \l
                                                                      \h
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      \7
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      \6
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      \X
                                                                      \S
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            29)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            83)))
                                                                      \W
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            17)))
                                                                      \h
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            62)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            114)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            91)))
                                                                      \c
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      \M
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      \O
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            126)))
                                                                      \n
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            62)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            121)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            29)))
                                                                      \M
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      \+
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            44)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            44)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      \n
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            127)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            126)))
                                                                      \n
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      \V
                                                                      \Z
                                                                      \R
                                                                      \9
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            61)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            122)))
                                                                      \K
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            53)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            87)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            57)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            58)))
                                                                      \D
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            83)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            61)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            83)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            87)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            77)))
                                                                      \F
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            119)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            95)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            41)))
                                                                      \i
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            44)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      \T
                                                                      \P
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            59)))
                                                                      \m
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            62)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            82)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            95)))
                                                                      \y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      \n
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            126)))
                                                                      \7
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      \G
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            37)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      \V
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      \Z
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            61)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            37)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            95)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            37)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
                                                                      \0
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            8)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            76)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            41)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      \0
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            91)))
                                                                      \H
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            114)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            15)))
                                                                      \g
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            29)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      \Z
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            15)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            62)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            63)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            122)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78))))]
                                                        alg__20238__auto__ (nth
                                                                             vec__31826
                                                                             (int 0)
                                                                             nil)
                                                        b64b__20239__auto__ (nth
                                                                              vec__31826
                                                                              (int 1)
                                                                              nil)]
                                                    (datomic.crypto/decrypt-pk
                                                      (datomic.codec/decode-64
                                                        (datomic.codec/string->bytes
                                                          k__20246__auto__))
                                                      (datomic.crypto/spec->public-key
                                                        alg__20238__auto__
                                                        b64b__20239__auto__))))]
                         (if temp__5823__auto__
                           (let [m__20247__auto__ temp__5823__auto__]
                             (when (or
                                     (>
                                       (.getTime
                                         (.parse
                                           (java.text.SimpleDateFormat.
                                             (str
                                               \y
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 120)))
                                               \y
                                               \y
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 79)))
                                               \M
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 16)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 79)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 58)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 58)))))
                                           "2026-06-29"))
                                       ((keyword
                                          (str
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 95)))
                                            \x
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 19)))
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 48)))
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 82)))
                                            \e
                                            \s))
                                         m__20247__auto__))
                                     (not
                                       (contains?
                                         #{(keyword
                                             (str
                                               \s
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 113)))
                                               \a
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 82)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 113)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 95)))
                                               \r))
                                           (keyword
                                             (str
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 19)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 82)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 39)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 58)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 56)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 126)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 113)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 48)))
                                               \o
                                               \n))
                                           (keyword
                                             (str
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 95)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 121)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 87)))
                                               \l))}
                                         ((keyword
                                            (str
                                              (java.lang.Character/valueOf
                                                (char (get datomic.data/table 113)))
                                              (java.lang.Character/valueOf
                                                (char (get datomic.data/table 120)))
                                              (java.lang.Character/valueOf
                                                (char (get datomic.data/table 19)))
                                              (java.lang.Character/valueOf
                                                (char (get datomic.data/table 95)))))
                                           m__20247__auto__))))
                               (process/fail
                                 process/instance
                                 (str
                                   \L
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 126)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   \n
                                   \s
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 121)))
                                   \a
                                   \l
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 58)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 12)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 82)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   \h
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 106)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   \r
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   \s
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   \f
                                   \space
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 122)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   \o
                                   \m
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 126))))))
                             (when (=
                                     ((keyword
                                        (str
                                          (java.lang.Character/valueOf
                                            (char (get datomic.data/table 113)))
                                          (java.lang.Character/valueOf
                                            (char (get datomic.data/table 120)))
                                          \p
                                          (java.lang.Character/valueOf
                                            (char (get datomic.data/table 95)))))
                                       m__20247__auto__)
                                     (keyword
                                       (str
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 95)))
                                         \v
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 87)))
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 91))))))
                               (let [m__20242__auto__ m__20247__auto__
                                     eval_msg__20243__auto__ (str
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               \*
                                                               \*
                                                               \*
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 40)))
                                                               \h
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               \s
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 120)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               \e
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 66)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 82)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 56)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               \n
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 115)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               \space
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 121)))
                                                               \a
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 91)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 56)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 87)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 66)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 58)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 86)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 94)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               \r
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               \a
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 19)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 82)))
                                                               \o
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 58)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 56)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 126)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               \o
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               \space
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 91)))
                                                               \i
                                                               \c
                                                               \e
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 92)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 115)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 33)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 19)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 67)))
                                                               (java.lang.Character/valueOf
                                                                 (char (get datomic.data/table 1)))
                                                               (java.lang.Character/valueOf
                                                                 (char (get datomic.data/table 1)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 58)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 87)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               \o
                                                               \m
                                                               \i
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 126)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 86)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 126)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 66)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 86)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               \*
                                                               \*
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               \*
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               \*
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105))))]
                                 (println eval_msg__20243__auto__)
                                 (future-call
                                   (fn fn__31829
                                     ([]
                                       (try
                                         (loop []
                                           (if (>
                                                 (java.lang.System/currentTimeMillis)
                                                 ((keyword
                                                    (str
                                                      \e
                                                      \x
                                                      \p
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 48)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 82)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 95)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 106)))))
                                                   m__20242__auto__))
                                             (process/fail
                                               process/instance
                                               (str
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 30)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 121)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 87)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 91)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 \a
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 48)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 \n
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 35)))
                                                 \m
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 \d
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 95)))
                                                 \space
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 48)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 66)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 95)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 58)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 35)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 \t))
                                             (do
                                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                                              "datomic.transactor-ext")]
                                                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                                   (.warn
                                                     ^org.slf4j.Logger logger
                                                     (logger/process eval_msg__20243__auto__)))
                                                 nil)
                                               (java.lang.Thread/sleep
                                                 (long (+ 59904 (get datomic.data/table 81))))
                                               (recur))))
                                         (catch
                                           java.lang.Throwable
                                           t__20244__auto__
                                           (do
                                             (.printStackTrace
                                               ^java.lang.Throwable t__20244__auto__)
                                             (process/fail
                                               process/instance
                                               (str
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 30)))
                                                 \v
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 87)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 91)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 87)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 48)))
                                                 \o
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 93)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 35)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 66)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 58)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 95)))
                                                 \space
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 48)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 66)))
                                                 \e
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 58)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 35)))
                                                 \o
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 \*))))))))))
                             (select-keys
                               m__20247__auto__
                               [(keyword
                                  (str
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 19)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 95)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 95)))
                                    \r
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 106)))))
                                (keyword
                                  (str
                                    \t
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 120)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 19)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 95)))))]))
                           (do
                             (throw
                               (java.lang.RuntimeException.
                                 (str
                                   (java.lang.Character/valueOf (char (get datomic.data/table 17)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                   \a
                                   \b
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   \e
                                   \space
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   \o
                                   \a
                                   (java.lang.Character/valueOf (char (get datomic.data/table 58)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   \l
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 126)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 106)))
                                   \e
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 44)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   \y)))
                             nil)))
                       (catch
                         java.lang.Throwable
                         t__20248__auto__
                         (do
                           (throw
                             (java.lang.RuntimeException.
                               (str
                                 (java.lang.Character/valueOf (char (get datomic.data/table 17)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                 \b
                                 (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                 \e
                                 \space
                                 (java.lang.Character/valueOf (char (get datomic.data/table 113)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                 \space
                                 (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 58)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                 \l
                                 (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 126)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 106)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 44)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 120)))
                                 \*)))
                           nil))))
            type (get
                   result
                   (keyword
                     (str
                       (java.lang.Character/valueOf (char (get datomic.data/table 113)))
                       (java.lang.Character/valueOf (char (get datomic.data/table 120)))
                       (java.lang.Character/valueOf (char (get datomic.data/table 19)))
                       \e)))]
        (aset (ints cfext/flags) (int 253) (int 64))
        (aset (ints cfext/flags) (int 19) (int 64))
        (aset (ints cfext/flags) (int 66) (int 64))
        result)))
  (reset-meta!
    #'start-pro
    (assoc {:arglists (clojure.core/list ['args]), :column (int 1)} :name 'start-pro :ns *ns*))
  (defn humanize-key
    ([k]
      (let [mkdate (fn mkdate ([p1__31841#] (java.util.Date. (long p1__31841#))))
            norm (update-in
                   (update-in (update-in k [:issued] mkdate) [:expires] mkdate)
                   [:type]
                   (fn fn__31844
                     ([t]
                       (let [G__31845 t]
                         (case
                           G__31845
                           :starter
                           "Pro Starter"
                           :eval
                           "Pro Eval"
                           :production
                           "Pro")))))]
        (if (= :eval (:type k))
          norm
          (set/rename-keys norm {:expires :valid-for-releases-up-to})))))
  (reset-meta!
    #'humanize-key
    (assoc {:arglists (clojure.core/list ['k]), :column (int 1)} :name 'humanize-key :ns *ns*))
  (def describe-license-key
   (fn describe_license_key
     ([p__31848]
       (let [map__31849 p__31848
             map__31849 (if (seq? map__31849)
                          (if (next map__31849)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__31849))
                            (if (seq map__31849) (first map__31849) {}))
                          map__31849)
             properties_file (get map__31849 :properties-file)]
         (prn
           (let [G__31850 properties_file
                 G__31850 (some-> G__31850 (common/load-properties))
                 G__31850 (some-> G__31850 (common/props->map))
                 G__31850 (some-> G__31850 (common/force-map-keywords))
                 G__31850 (some-> G__31850 (:license-key))]
             (some->
               (when-not (nil? G__31850)
                 (clojure.edn/read-string
                   (let [vec__31851 [(str
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 41)))
                                       \S
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110))))
                                     (str
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 16)))
                                       \I
                                       \I
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 78)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 77)))
                                       \A
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       \B
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       \k
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 28)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 44)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 48)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 57)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 37)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 63)))
                                       \0
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 78)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 94)))
                                       \A
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 71)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       \Q
                                       \8
                                       \A
                                       \M
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       \I
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 78)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 71)))
                                       \g
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 49)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 71)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       \i
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 89)))
                                       \Z
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 82)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 19)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 19)))
                                       \z
                                       \K
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 76)))
                                       \1
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 96)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 53)))
                                       \f
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 12)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 89)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 40)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 48)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 89)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 41)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 84)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 16)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 28)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 94)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 58)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 82)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 71)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       \6
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 82)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 15)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       \3
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 28)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 28)))
                                       \C
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 63)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 57)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 127)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 121)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 107)))
                                       \c
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 17)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       \f
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 114)))
                                       \y
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 66)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       \m
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       \g
                                       \Y
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 1)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 98)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 53)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 17)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 121)))
                                       \2
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 48)))
                                       \Y
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 49)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 77)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 12)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 53)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 3)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 56)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 94)))
                                       \c
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 107)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 84)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 87)))
                                       \f
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       \A
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 4)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       \R
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 113)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 39)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       \h
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 87)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 114)))
                                       \Z
                                       \D
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       \H
                                       \5
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       \3
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 99)))
                                       \S
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 40)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 71)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 59)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       \z
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 1)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 94)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 127)))
                                       \Z
                                       \F
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 94)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 1)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       \v
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 76)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       \F
                                       \D
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 58)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       \i
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 66)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 66)))
                                       \W
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 8)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 98)))
                                       \f
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       \B
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       \f
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 59)))
                                       \B
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 76)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       \n
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 4)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 113)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 44)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 41)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 66)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 89)))
                                       \o
                                       \U
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 89)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 63)))
                                       \q
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 62)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 8)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 127)))
                                       \Y
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 12)))
                                       \3
                                       \y
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 56)))
                                       \u
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 29)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 91)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 15)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 39)))
                                       \l
                                       \h
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       \7
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 98)))
                                       \6
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 66)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 12)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       \X
                                       \S
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 29)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 83)))
                                       \W
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 17)))
                                       \h
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 62)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 114)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 91)))
                                       \c
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       \M
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       \O
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 126)))
                                       \n
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 62)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 121)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 29)))
                                       \M
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 40)))
                                       \+
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 44)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 44)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 12)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       \n
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 84)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 127)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 49)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 126)))
                                       \n
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 40)))
                                       \V
                                       \Z
                                       \R
                                       \9
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 61)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 113)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 84)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 122)))
                                       \K
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 53)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 87)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 57)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 58)))
                                       \D
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 83)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 61)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 71)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 4)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 83)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 87)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 77)))
                                       \F
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 84)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 98)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 120)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 119)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 95)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 41)))
                                       \i
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 44)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 113)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       \T
                                       \P
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 39)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 59)))
                                       \m
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 98)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 33)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 117)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 62)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 82)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 95)))
                                       \y
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 39)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       \n
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 115)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 126)))
                                       \7
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 39)))
                                       \G
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 37)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       \V
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 39)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       \Z
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 61)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 65)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 37)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 48)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 95)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 12)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 37)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 49)))
                                       \0
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 113)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 8)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 76)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 9)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 41)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 30)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 78)))
                                       \0
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 91)))
                                       \H
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 98)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 114)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 15)))
                                       \g
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 29)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 113)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 93)))
                                       \Z
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 15)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 118)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 64)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 62)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 63)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 68)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 122)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 108)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 110)))
                                       (java.lang.Character/valueOf
                                         (char (get datomic.data/table 78))))]
                         alg__20238__auto__ (nth vec__31851 (int 0) nil)
                         b64b__20239__auto__ (nth vec__31851 (int 1) nil)]
                     (datomic.crypto/decrypt-pk
                       (datomic.codec/decode-64 (datomic.codec/string->bytes G__31850))
                       (datomic.crypto/spec->public-key alg__20238__auto__ b64b__20239__auto__)))))
               (humanize-key))))))))
  (reset-meta!
    #'describe-license-key
    (assoc
      {:arglists (clojure.core/list [{:keys ['properties-file]}]), :column (int 1)}
      :name
      'describe-license-key
      :ns
      *ns*)))
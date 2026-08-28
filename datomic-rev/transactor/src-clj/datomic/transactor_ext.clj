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
  (defn start-logrotate
    ([p__26457]
      (let [map__26458 p__26457
            map__26458 (if (seq? map__26458)
                         (if (next map__26458)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26458))
                           (if (seq map__26458) (first map__26458) {}))
                         map__26458)
            log_dir (get map__26458 :log-dir)
            creds (get map__26458 :creds)
            aws_s3_log_bucket_id (get map__26458 :aws-s3-log-bucket-id)
            log_path_fn (get map__26458 :log-path-fn)
            process (get map__26458 :process)]
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
              (fn fn__26461
                ([]
                  (logrotate/zip-and-put-in-s3
                    log_dir
                    aws_s3_log_bucket_id
                    log_path_fn
                    creds
                    0)))))))))
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
  (defn start-pro
    ([args]
      (start-ping-endpoint)
      (let [result (let [k__20457__auto__ (:license-key args)]
                     (try
                       (let [temp__5802__auto__ (clojure.edn/read-string
                                                  (let [vec__26468 [(str
                                                                      \R
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            68)))
                                                                      \I
                                                                      \B
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
                                                                      \N
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      \g
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            44)))
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
                                                                      \k
                                                                      \i
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            57)))
                                                                      \9
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
                                                                      \A
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
                                                                      \E
                                                                      \F
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            108)))
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
                                                                            16)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
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
                                                                      \A
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
                                                                      \A
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            82)))
                                                                      \p
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            30)))
                                                                      \p
                                                                      \z
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      \J
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
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
                                                                      \Q
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            71)))
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
                                                                      \v
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            126)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      \O
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      \Y
                                                                      \/
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            8)))
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
                                                                      \j
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
                                                                      \g
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            3)))
                                                                      \u
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            126)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            110)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            41)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            87)))
                                                                      \1
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            122)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            3)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            61)))
                                                                      \4
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            99)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
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
                                                                      \C
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            96)))
                                                                      \/
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
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
                                                                      \/
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            121)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      \m
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            53)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      \L
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      \3
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      \k
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
                                                                      \Y
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            17)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            28)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            89)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            12)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            56)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            56)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            33)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            66)))
                                                                      \f
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      \X
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            117)))
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
                                                                            62)))
                                                                      \1
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            16)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            118)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            9)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            126)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
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
                                                                      \g
                                                                      \v
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            16)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            83)))
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
                                                                      \O
                                                                      \n
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            115)))
                                                                      \6
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
                                                                      \y
                                                                      \K
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            93)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            29)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            64)))
                                                                      \R
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            37)))
                                                                      \S
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            65)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
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
                                                                      \G
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
                                                                      \C
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            4)))
                                                                      \+
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            94)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            84)))
                                                                      \7
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            48)))
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            40)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            15)))
                                                                      \o
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
                                                                      \S
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            120)))
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
                                                                      \c
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            98)))
                                                                      \o
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            29)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            39)))
                                                                      \O
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
                                                                      \N
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
                                                                      \n
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
                                                                      \9
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            49)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            99)))
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
                                                                      \E
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            78)))
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            99)))
                                                                      \l
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
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            113)))
                                                                      \n
                                                                      \Z
                                                                      (java.lang.Character/valueOf
                                                                        (char
                                                                          (get
                                                                            datomic.data/table
                                                                            15)))
                                                                      \4
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
                                                                      \I
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
                                                        alg__20449__auto__ (nth
                                                                             vec__26468
                                                                             (int 0)
                                                                             nil)
                                                        b64b__20450__auto__ (nth
                                                                              vec__26468
                                                                              (int 1)
                                                                              nil)]
                                                    (datomic.crypto/decrypt-pk
                                                      (datomic.codec/decode-64
                                                        (datomic.codec/string->bytes
                                                          k__20457__auto__))
                                                      (datomic.crypto/spec->public-key
                                                        alg__20449__auto__
                                                        b64b__20450__auto__))))]
                         (if temp__5802__auto__
                           (let [m__20458__auto__ temp__5802__auto__]
                             (when (or
                                     (>
                                       (.getTime
                                         (.parse
                                           (java.text.SimpleDateFormat.
                                             (str
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 120)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 120)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 120)))
                                               \y
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 79)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 16)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 16)))
                                               \-
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 58)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 58)))))
                                           "2024-11-13"))
                                       ((keyword
                                          (str
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 95)))
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 119)))
                                            \p
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 48)))
                                            \r
                                            \e
                                            (java.lang.Character/valueOf
                                              (char (get datomic.data/table 106)))))
                                         m__20458__auto__))
                                     (not
                                       (contains?
                                         #{(keyword
                                             (str
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 106)))
                                               \t
                                               \a
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 82)))
                                               \t
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 95)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 82)))))
                                           (keyword
                                             (str
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 19)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 82)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 39)))
                                               \d
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 56)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 126)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 113)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 48)))
                                               (java.lang.Character/valueOf
                                                 (char (get datomic.data/table 39)))
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
                                           m__20458__auto__))))
                               (process/fail
                                 process/instance
                                 (str
                                   (java.lang.Character/valueOf (char (get datomic.data/table 59)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 126)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 106)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   \space
                                   (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                   \o
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 121)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   \l
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 58)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 12)))
                                   \o
                                   (java.lang.Character/valueOf (char (get datomic.data/table 82)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 33)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   \s
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 82)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 106)))
                                   \e
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 12)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 122)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 66)))
                                   \i
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 126))))))
                             (when (=
                                     ((keyword
                                        (str
                                          \t
                                          (java.lang.Character/valueOf
                                            (char (get datomic.data/table 120)))
                                          (java.lang.Character/valueOf
                                            (char (get datomic.data/table 19)))
                                          (java.lang.Character/valueOf
                                            (char (get datomic.data/table 95)))))
                                       m__20458__auto__)
                                     (keyword
                                       (str
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 95)))
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 121)))
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 87)))
                                         (java.lang.Character/valueOf
                                           (char (get datomic.data/table 91))))))
                               (let [m__20453__auto__ m__20458__auto__
                                     eval_msg__20454__auto__ (str
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
                                                               \T
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 33)))
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
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 120)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
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
                                                               \n
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               \g
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 35)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
                                                               \v
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 87)))
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
                                                               \F
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 82)))
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
                                                                   (get datomic.data/table 91)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 126)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 93)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 106)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 95)))
                                                               \,
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
                                                               \h
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 113)))
                                                               \t
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
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 39)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 66)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 48)))
                                                               \c
                                                               \.
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
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               \*
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105)))
                                                               (java.lang.Character/valueOf
                                                                 (char
                                                                   (get datomic.data/table 105))))]
                                 (println eval_msg__20454__auto__)
                                 (future-call
                                   (fn fn__26471
                                     ([]
                                       (try
                                         (loop []
                                           (if (>
                                                 (java.lang.System/currentTimeMillis)
                                                 ((keyword
                                                    (str
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 95)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 119)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 19)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 48)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 82)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 95)))
                                                      (java.lang.Character/valueOf
                                                        (char (get datomic.data/table 106)))))
                                                   m__20453__auto__))
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
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 35)))
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
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))))
                                             (do
                                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                                              "datomic.transactor-ext")]
                                                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                                   (.warn
                                                     ^org.slf4j.Logger logger
                                                     (logger/process eval_msg__20454__auto__)))
                                                 nil)
                                               (java.lang.Thread/sleep
                                                 (long (+ 59904 (get datomic.data/table 81))))
                                               (recur))))
                                         (catch
                                           java.lang.Throwable
                                           t__20455__auto__
                                           (do
                                             (.printStackTrace
                                               ^java.lang.Throwable t__20455__auto__)
                                             (process/fail
                                               process/instance
                                               (str
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 30)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 121)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 87)))
                                                 \l
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 87)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 48)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 93)))
                                                 \space
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 66)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 \d
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 95)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 35)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 \i
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 66)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 95)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 58)))
                                                 \space
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 39)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 56)))
                                                 (java.lang.Character/valueOf
                                                   (char (get datomic.data/table 113)))
                                                 \*))))))))))
                             (select-keys
                               m__20458__auto__
                               [(keyword
                                  (str
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 19)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 95)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 95)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 82)))
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 106)))))
                                (keyword
                                  (str
                                    (java.lang.Character/valueOf
                                      (char (get datomic.data/table 113)))
                                    \y
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
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   \b
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   \space
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 113)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   \space
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 58)))
                                   \space
                                   (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 126)))
                                   \e
                                   (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                   (java.lang.Character/valueOf
                                     (char (get datomic.data/table 106)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                   \k
                                   (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                   \y)))
                             nil)))
                       (catch
                         java.lang.Throwable
                         t__20459__auto__
                         (do
                           (throw
                             (java.lang.RuntimeException.
                               (str
                                 (java.lang.Character/valueOf (char (get datomic.data/table 17)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 107)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                 \e
                                 \space
                                 (java.lang.Character/valueOf (char (get datomic.data/table 113)))
                                 \o
                                 (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 39)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 87)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 58)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 91)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 48)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 126)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 93)))
                                 \s
                                 (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 35)))
                                 \k
                                 (java.lang.Character/valueOf (char (get datomic.data/table 95)))
                                 (java.lang.Character/valueOf (char (get datomic.data/table 120)))
                                 \*)))
                           nil))))
            type (get
                   result
                   (keyword
                     (str
                       \t
                       (java.lang.Character/valueOf (char (get datomic.data/table 120)))
                       (java.lang.Character/valueOf (char (get datomic.data/table 19)))
                       (java.lang.Character/valueOf (char (get datomic.data/table 95))))))]
        (aset (ints cfext/flags) (int 89) (int 52))
        (aset (ints cfext/flags) (int 175) (int 52))
        (aset (ints cfext/flags) (int 37) (int 52))
        result)))
  (defn humanize-key
    ([k]
      (let [mkdate (fn mkdate ([p1__26483#] (java.util.Date. (long p1__26483#))))
            norm (update-in
                   (update-in (update-in k [:issued] mkdate) [:expires] mkdate)
                   [:type]
                   (fn fn__26486
                     ([t]
                       (let [G__26487 t]
                         (case
                           G__26487
                           :starter
                           "Pro Starter"
                           :eval
                           "Pro Eval"
                           :production
                           "Pro")))))]
        (if (= :eval (:type k))
          norm
          (set/rename-keys norm {:expires :valid-for-releases-up-to})))))
  (defn describe-license-key
    ([p__26490]
      (let [map__26491 p__26490
            map__26491 (if (seq? map__26491)
                         (if (next map__26491)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26491))
                           (if (seq map__26491) (first map__26491) {}))
                         map__26491)
            properties_file (get map__26491 :properties-file)]
        (prn
          (let [G__26492 properties_file
                G__26492 (some-> G__26492 (common/load-properties))
                G__26492 (some-> G__26492 (common/props->map))
                G__26492 (some-> G__26492 (common/force-map-keywords))
                G__26492 (some-> G__26492 (:license-key))]
            (some->
              (when-not (nil? G__26492)
                (clojure.edn/read-string
                  (let [vec__26493 [(str
                                      \R
                                      \S
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110))))
                                    (str
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 16)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      \I
                                      \B
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 77)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      \N
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 78)))
                                      \g
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 44)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 28)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      \k
                                      \i
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 57)))
                                      \9
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 63)))
                                      \0
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 78)))
                                      \A
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 108)))
                                      \E
                                      \F
                                      \A
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 9)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 71)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 108)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 127)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 16)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 78)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 71)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 49)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 71)))
                                      \A
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 108)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 30)))
                                      \A
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 48)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 89)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 82)))
                                      \p
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 30)))
                                      \p
                                      \z
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 49)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      \J
                                      \1
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 96)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 65)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 65)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 53)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
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
                                      \Q
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
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 84)))
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
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 4)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 28)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 28)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 71)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 63)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 57)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 127)))
                                      \v
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 107)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 126)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 17)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      \f
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 114)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 66)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 119)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 66)))
                                      \O
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      \Y
                                      \/
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 53)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 17)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 121)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 8)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 48)))
                                      \Y
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 49)))
                                      \j
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 53)))
                                      \g
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 3)))
                                      \u
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 94)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 126)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 107)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 84)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 87)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 9)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 4)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 119)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 41)))
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
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 87)))
                                      \1
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 122)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 3)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 61)))
                                      \4
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 4)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 99)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 117)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 40)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 9)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      \C
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 59)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 96)))
                                      \/
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 94)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 127)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 94)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 94)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 108)))
                                      \/
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 121)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 76)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 119)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 94)))
                                      \D
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 58)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 108)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 48)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 66)))
                                      \m
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 53)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 8)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 9)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 117)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 78)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      \L
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 78)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 76)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 119)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      \3
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 113)))
                                      \k
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 41)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 66)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      \Y
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 39)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 17)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 89)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 63)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 28)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 108)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 62)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 8)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 127)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 89)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 4)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 56)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 56)))
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
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 84)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 66)))
                                      \f
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      \X
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 117)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 29)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 83)))
                                      \W
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 17)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      \h
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 62)))
                                      \1
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 91)))
                                      \c
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 119)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 16)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 118)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 9)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 126)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 62)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 65)))
                                      \g
                                      \v
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 117)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 29)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 16)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 40)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 83)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 44)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 44)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      \O
                                      \n
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      \6
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 127)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      \y
                                      \K
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 65)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 126)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 40)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 29)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      \R
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 37)))
                                      \S
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 65)))
                                      \Z
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 61)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 113)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 84)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 122)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 49)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 53)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 87)))
                                      \G
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 58)))
                                      \D
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 83)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 61)))
                                      \C
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 4)))
                                      \+
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 87)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 77)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 94)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 84)))
                                      \7
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 119)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 95)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 41)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 48)))
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
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 40)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 15)))
                                      \o
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 59)))
                                      \m
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 118)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 33)))
                                      \S
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 62)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 82)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 95)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 120)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 39)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 118)))
                                      \n
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      \c
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      \o
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 57)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 37)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 93)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 30)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 110)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 29)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 39)))
                                      \O
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 61)))
                                      \N
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 37)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 48)))
                                      \n
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 95)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 12)))
                                      \9
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 49)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 99)))
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
                                      \E
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 78)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 99)))
                                      \l
                                      \H
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 68)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 98)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 114)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 15)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 115)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 29)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 113)))
                                      \n
                                      \Z
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 15)))
                                      \4
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 64)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 62)))
                                      (java.lang.Character/valueOf
                                        (char (get datomic.data/table 63)))
                                      \I
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
                        alg__20449__auto__ (nth vec__26493 (int 0) nil)
                        b64b__20450__auto__ (nth vec__26493 (int 1) nil)]
                    (datomic.crypto/decrypt-pk
                      (datomic.codec/decode-64 (datomic.codec/string->bytes G__26492))
                      (datomic.crypto/spec->public-key alg__20449__auto__ b64b__20450__auto__)))))
              (humanize-key))))))))
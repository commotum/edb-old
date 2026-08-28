(do
  (clojure.core/in-ns 'datomic.logrotate)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['clojure.java.io :as 'io]
        ['clojure.string :as 'str]
        ['datomic.jar :as 'jar]
        ['datomic.slf4j :as 'logger]
        ['datomic.monitor :as 'monitor]
        ['datomic.s3 :as 's3])
      (clojure.core/import 'java.io.File)))
  (when-not (.equals 'datomic.logrotate 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.logrotate))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['clojure.java.io :as 'io]
          ['clojure.string :as 'str]
          ['datomic.jar :as 'jar]
          ['datomic.slf4j :as 'logger]
          ['datomic.monitor :as 'monitor]
          ['datomic.s3 :as 's3])
        (clojure.core/import 'java.io.File))))
  (defn all-but-most-recent-n-non-zip-files
    ([dir n]
      (drop
        n
        (sort-by
          (fn fn__26398 ([p1__26397#] (long (- (.lastModified ^java.io.File p1__26397#)))))
          (filter
            (fn fn__26400
              ([p1__26396#]
                (and
                  (.isFile ^java.io.File p1__26396#)
                  (not (.endsWith (.getName ^java.io.File p1__26396#) "zip")))))
            (file-seq (io/file dir)))))))
  (defn zip-up-file
    ([file]
      (let [zipname (str (.getName ^java.io.File file) ".zip")
            zipfile (io/file (.getParentFile ^java.io.File file) zipname)]
        (jar/create-zip
          zipfile
          (fn fn__26405
            ([jos]
              (jar/add-zip-entries
                jos
                [file]
                (fn fn__26406
                  ([p1__26404#]
                    (str/replace
                      p1__26404#
                      #".*/(.*)\.(.*)"
                      (fn fn__26408
                        ([p__26407]
                          (let [vec__26409 p__26407
                                _ (nth vec__26409 (int 0) nil)
                                name (nth vec__26409 (int 1) nil)
                                ext (nth vec__26409 (int 2) nil)]
                            (str name "/" name "." ext)))))))))))
        zipfile)))
  (defn zip-and-put-in-s3
    ([dir bucket log_path_fn creds n]
      (try
        (do
          (let [s3 (s3/s3-service creds)]
            (loop [seq_26416 (seq (all-but-most-recent-n-non-zip-files dir n))
                   chunk_26417 nil
                   count_26418 0
                   i_26419 0]
              (if (< i_26419 count_26418)
                (let [fname (.nth ^clojure.lang.Indexed chunk_26417 (int i_26419))]
                  (let [m_26420 {:event :logrotate/put-file, :file fname}
                        ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.logrotate")]
                                          (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                            (.info
                                              ^org.slf4j.Logger logger
                                              (logger/process (assoc m_26420 :phase :begin))))
                                          nil)
                        start__8584__auto__ (java.lang.System/nanoTime)
                        result__8585__auto__ (try
                                               {:returned
                                                (let [logfile (io/file fname)
                                                      zipped (zip-up-file logfile)]
                                                  (s3/put-file-as
                                                    s3
                                                    bucket
                                                    zipped
                                                    (str
                                                      (^clojure.lang.IFn log_path_fn)
                                                      "/"
                                                      (.getName ^java.io.File zipped)))
                                                  (.delete ^java.io.File zipped)
                                                  (.delete ^java.io.File logfile)
                                                  (monitor/add-stat :RotateLog 1))}
                                               (catch
                                                 java.lang.Throwable
                                                 t__8586__auto__
                                                 {:threw t__8586__auto__}))
                        elapsed_26421 (- (java.lang.System/nanoTime) start__8584__auto__)
                        msec_26422 (logger/format-as-msec (long elapsed_26421))]
                    (let [endmsg__8587__auto__ (merge
                                                 (assoc m_26420 :msec msec_26422 :phase :end)
                                                 (when (:threw result__8585__auto__)
                                                   {:threw (class (:threw result__8585__auto__))}))
                          logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate")]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                      nil)
                    (if (contains? result__8585__auto__ :returned)
                      (:returned result__8585__auto__)
                      (throw (:threw result__8585__auto__))))
                  (recur seq_26416 chunk_26417 count_26418 (inc i_26419)))
                (let [temp__5804__auto__ (seq seq_26416)]
                  (when temp__5804__auto__
                    (let [seq_26416 temp__5804__auto__]
                      (if (chunked-seq? seq_26416)
                        (let [c__6065__auto__ (chunk-first seq_26416)]
                          (recur
                            (chunk-rest seq_26416)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [fname (first seq_26416)]
                          (let [m_26425 {:event :logrotate/put-file, :file fname}
                                ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                               "datomic.logrotate")]
                                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                    (.info
                                                      ^org.slf4j.Logger logger
                                                      (logger/process
                                                        (assoc m_26425 :phase :begin))))
                                                  nil)
                                start__8584__auto__ (java.lang.System/nanoTime)
                                result__8585__auto__ (try
                                                       {:returned
                                                        (let [logfile (io/file fname)
                                                              zipped (zip-up-file logfile)]
                                                          (s3/put-file-as
                                                            s3
                                                            bucket
                                                            zipped
                                                            (str
                                                              (^clojure.lang.IFn log_path_fn)
                                                              "/"
                                                              (.getName ^java.io.File zipped)))
                                                          (.delete ^java.io.File zipped)
                                                          (.delete ^java.io.File logfile)
                                                          (monitor/add-stat :RotateLog 1))}
                                                       (catch
                                                         java.lang.Throwable
                                                         t__8586__auto__
                                                         {:threw t__8586__auto__}))
                                elapsed_26426 (- (java.lang.System/nanoTime) start__8584__auto__)
                                msec_26427 (logger/format-as-msec (long elapsed_26426))]
                            (let [endmsg__8587__auto__ (merge
                                                         (assoc
                                                           m_26425
                                                           :msec
                                                           msec_26427
                                                           :phase
                                                           :end)
                                                         (when (:threw result__8585__auto__)
                                                           {:threw
                                                            (class
                                                              (:threw result__8585__auto__))}))
                                  logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate")]
                              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                (.info
                                  ^org.slf4j.Logger logger
                                  (logger/process endmsg__8587__auto__)))
                              nil)
                            (if (contains? result__8585__auto__ :returned)
                              (:returned result__8585__auto__)
                              (throw (:threw result__8585__auto__))))
                          (recur (next seq_26416) nil 0 0)))))))))
          nil)
        (catch
          java.lang.Throwable
          t
          (do
            (monitor/alarm :LogRotationFailed)
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate") ex t]
              (when (.isWarnEnabled ^org.slf4j.Logger logger)
                (.warn
                  ^org.slf4j.Logger logger
                  (logger/process "Failure putting logfile in S3")
                  ^java.lang.Throwable ex)
                (logger/caused-by logger ex))
              nil))))))
  (defn probe
    ([creds bucket]
      (let [s3 (s3/s3-service creds)]
        (s3/put-clj s3 bucket common/probe-name :logrotate-probe)
        :ok)))
  (defn watch-dir
    ([dir interval bucket log_path_fn creds]
      (doto
        (java.lang.Thread.
          (fn fn__26443
            ([]
              (loop []
                (do
                  (let [m_26444 {:event :logrotate/check}
                        ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                       "datomic.logrotate")]
                                          (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                            (.debug
                                              ^org.slf4j.Logger logger
                                              (logger/process (assoc m_26444 :phase :begin))))
                                          nil)
                        start__8584__auto__ (java.lang.System/nanoTime)
                        result__8585__auto__ (try
                                               {:returned
                                                (zip-and-put-in-s3 dir bucket log_path_fn creds 1)}
                                               (catch
                                                 java.lang.Throwable
                                                 t__8586__auto__
                                                 {:threw t__8586__auto__}))
                        elapsed_26445 (- (java.lang.System/nanoTime) start__8584__auto__)
                        msec_26446 (logger/format-as-msec (long elapsed_26445))]
                    (let [endmsg__8587__auto__ (merge
                                                 (assoc m_26444 :msec msec_26446 :phase :end)
                                                 (when (:threw result__8585__auto__)
                                                   {:threw (class (:threw result__8585__auto__))}))
                          logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate")]
                      (when (.isDebugEnabled ^org.slf4j.Logger logger)
                        (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                      nil)
                    (if (contains? result__8585__auto__ :returned)
                      (:returned result__8585__auto__)
                      (throw (:threw result__8585__auto__))))
                  (java.lang.Thread/sleep (long ^java.lang.Number interval))
                  (recur)))
              nil)))
        (.setDaemon (boolean (.booleanValue true)))
        (.start)))))
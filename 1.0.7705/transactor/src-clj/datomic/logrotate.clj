(do
  (clojure.core/in-ns 'datomic.logrotate)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.core2.aws.helpers :as 'aws-helpers]
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
          ['datomic.core2.aws.helpers :as 'aws-helpers]
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
          (fn fn__26720 ([p1__26719#] (long (- (.lastModified ^java.io.File p1__26719#)))))
          (filter
            (fn fn__26722
              ([p1__26718#]
                (and
                  (.isFile ^java.io.File p1__26718#)
                  (not (.endsWith (.getName ^java.io.File p1__26718#) "zip")))))
            (file-seq (io/file dir)))))))
  (reset-meta!
    #'all-but-most-recent-n-non-zip-files
    (assoc
      {:arglists (clojure.core/list ['dir 'n]), :column (int 1)}
      :name
      'all-but-most-recent-n-non-zip-files
      :ns
      *ns*))
  (def zip-up-file
   (fn zip_up_file
     ([file]
       (let [zipname (str (.getName ^java.io.File file) ".zip")
             zipfile (io/file (.getParentFile ^java.io.File file) zipname)]
         (jar/create-zip
           zipfile
           (fn fn__26727
             ([jos]
               (jar/add-zip-entries
                 jos
                 [file]
                 (fn fn__26728
                   ([p1__26726#]
                     (str/replace
                       p1__26726#
                       #".*/(.*)\.(.*)"
                       (fn fn__26730
                         ([p__26729]
                           (let [vec__26731 p__26729
                                 _ (nth vec__26731 (int 0) nil)
                                 name (nth vec__26731 (int 1) nil)
                                 ext (nth vec__26731 (int 2) nil)]
                             (str name "/" name "." ext)))))))))))
         zipfile))))
  (reset-meta!
    #'zip-up-file
    (assoc
      {:arglists
       (clojure.core/list (.withMeta [(.withMeta 'file {:tag 'File})] {:tag 'java.io.File})),
       :column (int 1)}
      :name
      'zip-up-file
      :ns
      *ns*))
  (def zip-and-put-in-s3
   (fn zip_and_put_in_s3
     ([dir bucket log_path_fn creds n]
       (try
         (do
           (let [s3 (if (:aws-access-key-id creds)
                      (s3/s3-service (aws-helpers/static-credentials-provider creds) {})
                      (s3/s3-service))]
             (loop [seq_26738 (seq (all-but-most-recent-n-non-zip-files dir n))
                    chunk_26739 nil
                    count_26740 0
                    i_26741 0]
               (if (< i_26741 count_26740)
                 (let [fname (.nth ^clojure.lang.Indexed chunk_26739 (int i_26741))]
                   (let [m_26742 {:event :logrotate/put-file, :file fname}
                         ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                        "datomic.logrotate")]
                                           (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                             (.info
                                               ^org.slf4j.Logger logger
                                               (logger/process (assoc m_26742 :phase :begin))))
                                           nil)
                         start__8599__auto__ (java.lang.System/nanoTime)
                         result__8600__auto__ (try
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
                                                  t__8601__auto__
                                                  {:threw t__8601__auto__}))
                         elapsed_26743 (- (java.lang.System/nanoTime) start__8599__auto__)
                         msec_26744 (logger/format-as-msec (long elapsed_26743))]
                     (let [endmsg__8602__auto__ (merge
                                                  (assoc m_26742 :msec msec_26744 :phase :end)
                                                  (when (:threw result__8600__auto__)
                                                    {:threw
                                                     (class (:threw result__8600__auto__))}))
                           logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate")]
                       (when (.isInfoEnabled ^org.slf4j.Logger logger)
                         (.info ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                       nil)
                     (if (contains? result__8600__auto__ :returned)
                       (:returned result__8600__auto__)
                       (throw (:threw result__8600__auto__))))
                   (recur seq_26738 chunk_26739 count_26740 (inc i_26741)))
                 (let [temp__5825__auto__ (seq seq_26738)]
                   (when temp__5825__auto__
                     (let [seq_26738 temp__5825__auto__]
                       (if (chunked-seq? seq_26738)
                         (let [c__6090__auto__ (chunk-first seq_26738)]
                           (recur
                             (chunk-rest seq_26738)
                             c__6090__auto__
                             (int (count c__6090__auto__))
                             (int 0)))
                         (let [fname (first seq_26738)]
                           (let [m_26747 {:event :logrotate/put-file, :file fname}
                                 ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                                "datomic.logrotate")]
                                                   (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                                     (.info
                                                       ^org.slf4j.Logger logger
                                                       (logger/process
                                                         (assoc m_26747 :phase :begin))))
                                                   nil)
                                 start__8599__auto__ (java.lang.System/nanoTime)
                                 result__8600__auto__ (try
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
                                                          t__8601__auto__
                                                          {:threw t__8601__auto__}))
                                 elapsed_26748 (- (java.lang.System/nanoTime) start__8599__auto__)
                                 msec_26749 (logger/format-as-msec (long elapsed_26748))]
                             (let [endmsg__8602__auto__ (merge
                                                          (assoc
                                                            m_26747
                                                            :msec
                                                            msec_26749
                                                            :phase
                                                            :end)
                                                          (when (:threw result__8600__auto__)
                                                            {:threw
                                                             (class
                                                               (:threw result__8600__auto__))}))
                                   logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate")]
                               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                 (.info
                                   ^org.slf4j.Logger logger
                                   (logger/process endmsg__8602__auto__)))
                               nil)
                             (if (contains? result__8600__auto__ :returned)
                               (:returned result__8600__auto__)
                               (throw (:threw result__8600__auto__))))
                           (recur (next seq_26738) nil 0 0)))))))))
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
               nil)))))))
  (reset-meta!
    #'zip-and-put-in-s3
    (assoc
      {:arglists (clojure.core/list ['dir 'bucket 'log-path-fn 'creds 'n]), :column (int 1)}
      :name
      'zip-and-put-in-s3
      :ns
      *ns*))
  (defn probe
    ([creds bucket]
      (let [s3 (if (:aws-access-key-id creds)
                 (s3/s3-service (aws-helpers/static-credentials-provider creds) {})
                 (s3/s3-service))]
        (s3/put-clj s3 bucket common/probe-name :logrotate-probe)
        :ok)))
  (reset-meta!
    #'probe
    (assoc
      {:arglists (clojure.core/list ['creds 'bucket]), :column (int 1)}
      :name
      'probe
      :ns
      *ns*))
  (def watch-dir
   (fn watch_dir
     ([dir interval bucket log_path_fn creds]
       (doto
         (java.lang.Thread.
           (fn fn__26765
             ([]
               (loop []
                 (do
                   (let [m_26766 {:event :logrotate/check}
                         ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                        "datomic.logrotate")]
                                           (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                             (.debug
                                               ^org.slf4j.Logger logger
                                               (logger/process (assoc m_26766 :phase :begin))))
                                           nil)
                         start__8599__auto__ (java.lang.System/nanoTime)
                         result__8600__auto__ (try
                                                {:returned
                                                 (zip-and-put-in-s3
                                                   dir
                                                   bucket
                                                   log_path_fn
                                                   creds
                                                   1)}
                                                (catch
                                                  java.lang.Throwable
                                                  t__8601__auto__
                                                  {:threw t__8601__auto__}))
                         elapsed_26767 (- (java.lang.System/nanoTime) start__8599__auto__)
                         msec_26768 (logger/format-as-msec (long elapsed_26767))]
                     (let [endmsg__8602__auto__ (merge
                                                  (assoc m_26766 :msec msec_26768 :phase :end)
                                                  (when (:threw result__8600__auto__)
                                                    {:threw
                                                     (class (:threw result__8600__auto__))}))
                           logger (org.slf4j.LoggerFactory/getLogger "datomic.logrotate")]
                       (when (.isDebugEnabled ^org.slf4j.Logger logger)
                         (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
                       nil)
                     (if (contains? result__8600__auto__ :returned)
                       (:returned result__8600__auto__)
                       (throw (:threw result__8600__auto__))))
                   (java.lang.Thread/sleep (long ^java.lang.Number interval))
                   (recur)))
               nil)))
         (.setDaemon (boolean (.booleanValue true)))
         (.start)))))
  (reset-meta!
    #'watch-dir
    (assoc
      {:arglists (clojure.core/list ['dir 'interval 'bucket 'log-path-fn 'creds]), :column (int 1)}
      :name
      'watch-dir
      :ns
      *ns*)))
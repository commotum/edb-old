(do
  (clojure.core/in-ns 'datomic.lifecycle)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.io :as 'io]
        ['datomic.coordination :as 'coord]
        ['datomic.process :as 'process]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'java.util.UUID)))
  (when-not (.equals 'datomic.lifecycle 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.lifecycle))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.io :as 'io]
          ['datomic.coordination :as 'coord]
          ['datomic.process :as 'process]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'java.util.UUID))))
  (set! *warn-on-reflection* true)
  (def pump
   (fn pump
     ([cluster endpoint tick previous_rev]
       (let [timestamp (java.lang.System/currentTimeMillis)
             basis (assoc endpoint :timestamp (long timestamp))
             value (coord/create-heartbeat basis)
             rev (inc previous_rev)
             vk (pr-str value)
             result (deref (cluster/set-ref cluster coord/pod-key rev vk) (* 2 tick) :timeout)]
         (if (= :ok result)
           (do
             (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.lifecycle")]
               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                 (.info
                   ^org.slf4j.Logger logger
                   (logger/process
                     (merge
                       {:event :transactor/heartbeat, :rev rev}
                       (select-keys
                         basis
                         [:host :port :encrypt-channel :version :timestamp :username])))))
               nil)
             {:rev rev, :vk vk, :timestamp (long timestamp)})
           (do
             (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.lifecycle")]
               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                 (.info
                   ^org.slf4j.Logger logger
                   (logger/process {:event :transactor/heartbeat-failed, :cause result})))
               nil)
             nil))))))
  (reset-meta!
    #'pump
    (assoc
      {:arglists (clojure.core/list ['cluster 'endpoint 'tick 'previous-rev]), :column (int 1)}
      :name
      'pump
      :ns
      *ns*))
  (def master-loop
   (fn master_loop
     ([beat p__10991]
       (let [map__10992 p__10991
             map__10992 (if (seq? map__10992)
                          (if (next map__10992)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10992))
                            (if (seq map__10992) (first map__10992) {}))
                          map__10992)
             cluster (get map__10992 :cluster)
             tick (get map__10992 :tick)
             endpoint (get map__10992 :endpoint)
             G__10994 beat
             map__10995 G__10994
             map__10995 (if (seq? map__10995)
                          (if (next map__10995)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10995))
                            (if (seq map__10995) (first map__10995) {}))
                          map__10995)
             rev (get map__10995 :rev)
             vk (get map__10995 :vk)
             timestamp (get map__10995 :timestamp)]
         (loop [G__10994 G__10994]
           (let [map__10996 G__10994
                 map__10996 (if (seq? map__10996)
                              (if (next map__10996)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__10996))
                                (if (seq map__10996) (first map__10996) {}))
                              map__10996)
                 rev (get map__10996 :rev)
                 vk (get map__10996 :vk)
                 timestamp (get map__10996 :timestamp)]
             (if (process/failing? process/instance)
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.lifecycle")]
                 (when (.isInfoEnabled ^org.slf4j.Logger logger)
                   (.info
                     ^org.slf4j.Logger logger
                     (logger/process {:event :transactor/heartbeat-stopped})))
                 nil)
               (if rev
                 (let [elapsed (- (java.lang.System/currentTimeMillis) timestamp)]
                   (java.lang.Thread/sleep (long (max 1 (- tick elapsed))))
                   (let [new_beat (pump cluster endpoint tick rev)]
                     (when new_beat
                       (monitor/add-stat :HeartbeatMsec (- (:timestamp new_beat) timestamp)))
                     (recur new_beat)))
                 (do
                   (monitor/alarm :HeartbeatFailed)
                   (process/fail process/instance "Heartbeat failed"))))))))))
  (reset-meta!
    #'master-loop
    (assoc
      {:arglists (clojure.core/list ['beat {:keys ['cluster 'tick 'endpoint]}]), :column (int 1)}
      :name
      'master-loop
      :ns
      *ns*))
  (def standby-loop
   (fn standby_loop
     ([p__10998]
       (let [map__10999 p__10998
             map__10999 (if (seq? map__10999)
                          (if (next map__10999)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__10999))
                            (if (seq map__10999) (first map__10999) {}))
                          map__10999)
             args map__10999
             cluster (get map__10999 :cluster)
             tick (get map__10999 :tick)
             serve (get map__10999 :serve)
             endpoint (get map__10999 :endpoint)
             refval (deref (cluster/get-ref cluster coord/pod-key))
             beat (pump cluster endpoint tick (or (:rev refval) -1))]
         (when beat (^clojure.lang.IFn serve))
         (master-loop beat args)))))
  (reset-meta!
    #'standby-loop
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'tick 'serve 'endpoint], :as 'args}]),
       :column (int 1)}
      :name
      'standby-loop
      :ns
      *ns*))
  (def start
   (fn start
     ([& p__11002]
       (let [map__11003 p__11002
             map__11003 (if (seq? map__11003)
                          (if (next map__11003)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__11003))
                            (if (seq map__11003) (first map__11003) {}))
                          map__11003)
             arg map__11003
             serve (get map__11003 :serve)
             start_serving (fn start_serving
                             ([]
                               (future-call
                                 (fn fn__11005
                                   ([]
                                     (try
                                       (^clojure.lang.IFn serve)
                                       (catch
                                         java.lang.Throwable
                                         t
                                         (do
                                           (monitor/alarm :ServeFailed)
                                           (process/fail process/instance "Serve failed" t)))))))))
             standby_arg (assoc arg :serve start_serving)]
         (let [G__11008 (java.lang.Thread.
                          (fn fn__11009
                            ([]
                              (try
                                ((if (config/pro?)
                                   (resolve 'datomic.lifecycle-ext/standby-loop)
                                   standby-loop)
                                  standby_arg)
                                (catch
                                  java.lang.Throwable
                                  t
                                  (do
                                    (monitor/alarm :LifecycleThreadFailed)
                                    (process/fail
                                      process/instance
                                      "Lifecycle thread failed"
                                      t)))))))]
           (.start ^java.lang.Thread G__11008))
         :started))))
  (reset-meta!
    #'start
    (assoc
      {:arglists (clojure.core/list ['& {:keys ['serve], :as 'arg}]), :column (int 1)}
      :name
      'start
      :ns
      *ns*)))
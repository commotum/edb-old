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
  (defn pump
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
            nil)))))
  (defn master-loop
    ([beat p__25583]
      (let [map__25584 p__25583
            map__25584 (if (seq? map__25584)
                         (if (next map__25584)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25584))
                           (if (seq map__25584) (first map__25584) {}))
                         map__25584)
            cluster (get map__25584 :cluster)
            tick (get map__25584 :tick)
            endpoint (get map__25584 :endpoint)
            G__25586 beat
            map__25587 G__25586
            map__25587 (if (seq? map__25587)
                         (if (next map__25587)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25587))
                           (if (seq map__25587) (first map__25587) {}))
                         map__25587)
            rev (get map__25587 :rev)
            vk (get map__25587 :vk)
            timestamp (get map__25587 :timestamp)]
        (loop [G__25586 G__25586]
          (let [map__25588 G__25586
                map__25588 (if (seq? map__25588)
                             (if (next map__25588)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__25588))
                               (if (seq map__25588) (first map__25588) {}))
                             map__25588)
                rev (get map__25588 :rev)
                vk (get map__25588 :vk)
                timestamp (get map__25588 :timestamp)]
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
                  (process/fail process/instance "Heartbeat failed")))))))))
  (defn standby-loop
    ([p__25590]
      (let [map__25591 p__25590
            map__25591 (if (seq? map__25591)
                         (if (next map__25591)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25591))
                           (if (seq map__25591) (first map__25591) {}))
                         map__25591)
            args map__25591
            cluster (get map__25591 :cluster)
            tick (get map__25591 :tick)
            serve (get map__25591 :serve)
            endpoint (get map__25591 :endpoint)
            refval (deref (cluster/get-ref cluster coord/pod-key))
            beat (pump cluster endpoint tick (or (:rev refval) -1))]
        (when beat (^clojure.lang.IFn serve))
        (master-loop beat args))))
  (defn start
    ([& p__25594]
      (let [map__25595 p__25594
            map__25595 (if (seq? map__25595)
                         (if (next map__25595)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__25595))
                           (if (seq map__25595) (first map__25595) {}))
                         map__25595)
            arg map__25595
            serve (get map__25595 :serve)
            start_serving (fn start_serving
                            ([]
                              (future-call
                                (fn fn__25597
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
        (let [G__25600 (java.lang.Thread.
                         (fn fn__25601
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
          (.start ^java.lang.Thread G__25600))
        :started))))
(do
  (clojure.core/in-ns 'datomic.lifecycle-ext)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['datomic.common :as 'common]
        ['datomic.coordination :as 'coord]
        ['datomic.config-ext :as 'cfext]
        ['datomic.cluster :as 'cluster]
        ['datomic.lifecycle :as 'lifecycle]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.lifecycle-ext 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.lifecycle-ext))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['datomic.common :as 'common]
          ['datomic.coordination :as 'coord]
          ['datomic.config-ext :as 'cfext]
          ['datomic.cluster :as 'cluster]
          ['datomic.lifecycle :as 'lifecycle]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger]))))
  (defn timestamp
    ([refval]
      (let [temp__5804__auto__ (:key refval)]
        (when temp__5804__auto__
          (let [s temp__5804__auto__ v (edn/read-string s)]
            (and (vector? v) (common/getx (coord/heartbeat->endpoint v) :timestamp)))))))
  (defn set-standby-ref
    ([cluster basis]
      (try
        (let [k coord/standby-key
              map__26508 (deref (cluster/get-ref cluster k) 500 nil)
              map__26508 (if (seq? map__26508)
                           (if (next map__26508)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__26508))
                             (if (seq map__26508) (first map__26508) {}))
                           map__26508)
              prev map__26508
              rev (get map__26508 :rev)]
          (when-not rev
            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.lifecycle-ext")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process "Unable to get standby ref")))
              nil))
          (cluster/set-ref cluster k (if rev (inc rev) 0) (pr-str (coord/create-heartbeat basis))))
        (catch
          java.lang.Throwable
          t
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.lifecycle-ext") ex t]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info
                ^org.slf4j.Logger logger
                (logger/process "Unable to set standby ref")
                ^java.lang.Throwable ex)
              (logger/caused-by logger ex))
            nil)))))
  (defn standby-loop
    ([p__26510]
      (let [map__26511 p__26510
            map__26511 (if (seq? map__26511)
                         (if (next map__26511)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__26511))
                           (if (seq map__26511) (first map__26511) {}))
                         map__26511)
            args map__26511
            cluster (get map__26511 :cluster)
            tick (get map__26511 :tick)
            serve (get map__26511 :serve)
            endpoint (get map__26511 :endpoint)
            ha? (get map__26511 :ha?)]
        (loop [refval (deref (cluster/get-ref cluster coord/pod-key))
               tstamp (java.lang.System/currentTimeMillis)
               missed 0]
          (if (and (< missed 2) ha?)
            (do
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.lifecycle-ext")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info
                    ^org.slf4j.Logger logger
                    (logger/process
                      {:event :transactor/standby,
                       :rev (:rev refval),
                       :missed (long missed),
                       :timestamp (timestamp refval)})))
                nil)
              (java.lang.Thread/sleep (long ^java.lang.Number tick))
              (let [new_refval (deref (cluster/get-ref cluster coord/pod-key))
                    new_tstamp (java.lang.System/currentTimeMillis)]
                (monitor/add-stat :HeartMonitorMsec (long (- new_tstamp tstamp)))
                (set-standby-ref cluster (assoc endpoint :timestamp (long new_tstamp)))
                (recur
                  new_refval
                  new_tstamp
                  (if (= (timestamp refval) (timestamp new_refval)) (inc missed) 0))))
            (let [beat (lifecycle/pump cluster endpoint tick (or (:rev refval) -1))]
              (when beat (^clojure.lang.IFn serve))
              (lifecycle/master-loop beat args))))))))
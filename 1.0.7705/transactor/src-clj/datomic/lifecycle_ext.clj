(do
  (clojure.core/in-ns 'datomic.lifecycle-ext)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.lifecycle-ext)
    {:doc
     "High-availability standby coordination. Tracks standby ownership in storage and starts the active lifecycle when the published transactor heartbeat expires."})
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
  ;; Extract the active transactor timestamp encoded in a coordination reference.
  (defn timestamp
    ([refval]
      (let [temp__5825__auto__ (:key refval)]
        (when temp__5825__auto__
          (let [s temp__5825__auto__ v (edn/read-string s)]
            (and (vector? v) (common/getx (coord/heartbeat->endpoint v) :timestamp)))))))
  (reset-meta!
    #'timestamp
    (assoc {:arglists (clojure.core/list ['refval]), :column (int 1)} :name 'timestamp :ns *ns*))
  ;; Publish this process's standby heartbeat using the reference revision observed in storage.
  (defn set-standby-ref
    ([cluster basis]
      (try
        (let [k coord/standby-key
              map__28134 (deref (cluster/get-ref cluster k) 500 nil)
              map__28134 (if (seq? map__28134)
                           (if (next map__28134)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__28134))
                             (if (seq map__28134) (first map__28134) {}))
                           map__28134)
              prev map__28134
              rev (get map__28134 :rev)]
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
  (reset-meta!
    #'set-standby-ref
    (assoc
      {:arglists (clojure.core/list ['cluster 'basis]), :column (int 1)}
      :name
      'set-standby-ref
      :ns
      *ns*))
  ;; Observe the active heartbeat once per tick. Two consecutive unchanged
  ;; timestamps trigger the attempt to acquire the active process reference.
  (defn standby-loop
    ([p__28136]
      (let [map__28137 p__28136
            map__28137 (if (seq? map__28137)
                         (if (next map__28137)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__28137))
                           (if (seq map__28137) (first map__28137) {}))
                         map__28137)
            args map__28137
            cluster (get map__28137 :cluster)
            tick (get map__28137 :tick)
            serve (get map__28137 :serve)
            endpoint (get map__28137 :endpoint)
            ha? (get map__28137 :ha?)]
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
              (lifecycle/master-loop beat args)))))))
  (reset-meta!
    #'standby-loop
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'tick 'serve 'endpoint 'ha?], :as 'args}]),
       :column (int 1)}
      :name
      'standby-loop
      :ns
      *ns*)))

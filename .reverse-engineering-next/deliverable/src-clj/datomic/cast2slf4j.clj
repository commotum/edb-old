(do
  (clojure.core/in-ns 'datomic.cast2slf4j)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['cognitect.caster :as 'cast]
        ['clojure.string :as 'str]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'slf4j])))
  (when-not (.equals 'datomic.cast2slf4j 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cast2slf4j))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['cognitect.caster :as 'cast]
          ['clojure.string :as 'str]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'slf4j]))))
  (def cast-name->cloudwatch-name
   (memoize
     (fn fn__21288
       ([mname] (keyword (apply str (map str/capitalize (str/split (name mname) #"\."))))))))
  (defonce work
   (delay
     (cast/register*
       cast/instance
       (fn fn__21292
         ([alarm]
           (let [temp__5455__auto__ (:ex alarm)]
             (if temp__5455__auto__
               (let [ex temp__5455__auto__
                     logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")
                     ex ex]
                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                   (.warn ^org.slf4j.Logger logger (slf4j/process (dissoc alarm :ex)) ex)
                   (slf4j/caused-by logger ex))
                 nil)
               (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                   (.warn ^org.slf4j.Logger logger (slf4j/process alarm))
                   nil)
                 nil)))))
       :alert)
     (cast/register*
       cast/instance
       (fn fn__21295
         ([event]
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
             (when (.isInfoEnabled ^org.slf4j.Logger logger)
               (.info ^org.slf4j.Logger logger (slf4j/process event))
               nil)
             nil)))
       :event)
     (cast/register*
       cast/instance
       (fn fn__21298
         ([p__21297]
           (let [map__21299 p__21297
                 map__21299 (if (seq? map__21299)
                              (clojure.lang.PersistentHashMap/create (seq map__21299))
                              map__21299)
                 metric map__21299
                 name (get map__21299 :name)
                 value (get map__21299 :value)]
             (monitor/add-stat (cast-name->cloudwatch-name name) value))))
       :metric)
     (cast/register*
       cast/instance
       (fn fn__21301
         ([dev]
           (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
             (when (.isDebugEnabled ^org.slf4j.Logger logger)
               (.debug ^org.slf4j.Logger logger (slf4j/process dev))
               nil)
             nil)))
       :dev)))
  (reset-meta! #'work (assoc {:private true, :column 1} :name 'work :ns *ns*))
  (defn redirect ([] (deref work))))
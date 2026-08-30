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
  (.setMeta
    (clojure.lang.RT/var "datomic.cast2slf4j" "cast-name->cloudwatch-name")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.cast2slf4j" "cast-name->cloudwatch-name")
    (memoize
      (fn fn__20155
        ([mname] (keyword (apply str (map str/capitalize (str/split (name mname) #"\."))))))))
  (.setMeta (clojure.lang.RT/var "datomic.cast2slf4j" "work") {:private true, :column (int 1)})
  (let [v__6812__auto__ #'work]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.cast2slf4j" "work") {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cast2slf4j" "work")
        (delay
          (cast/register*
            cast/instance
            (fn fn__20159
              ([alarm]
                (let [temp__5802__auto__ (:ex alarm)]
                  (if temp__5802__auto__
                    (let [ex temp__5802__auto__
                          logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")
                          ex ex]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn ^org.slf4j.Logger logger (slf4j/process (dissoc alarm :ex)) ex)
                        (slf4j/caused-by logger ex))
                      nil)
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn ^org.slf4j.Logger logger (slf4j/process alarm)))
                      nil)))))
            :alert)
          (cast/register*
            cast/instance
            (fn fn__20162
              ([event]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (slf4j/process event)))
                  nil)))
            :event)
          (cast/register*
            cast/instance
            (fn fn__20165
              ([p__20164]
                (let [map__20166 p__20164
                      map__20166 (if (seq? map__20166)
                                   (if (next map__20166)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__20166))
                                     (if (seq map__20166) (first map__20166) {}))
                                   map__20166)
                      metric map__20166
                      name (get map__20166 :name)
                      value (get map__20166 :value)]
                  (monitor/add-stat (cast-name->cloudwatch-name name) value))))
            :metric)
          (cast/register*
            cast/instance
            (fn fn__20168
              ([dev]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                    (.debug ^org.slf4j.Logger logger (slf4j/process dev)))
                  nil)))
            :dev)))
      #'work))
  (defn redirect ([] (deref work)))
  (reset-meta!
    #'redirect
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'redirect :ns *ns*)))
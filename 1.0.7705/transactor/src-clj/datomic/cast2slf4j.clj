(do
  (clojure.core/in-ns 'datomic.cast2slf4j)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cast2slf4j)
    {:doc
     "Routes Cognitect Caster events to structured SLF4J logging and increments Datomic metrics for event timing and alarm categories."})
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
      (fn fn__19140
        ([mname] (keyword (apply str (map str/capitalize (str/split (name mname) #"\."))))))))
  (.setMeta (clojure.lang.RT/var "datomic.cast2slf4j" "work") {:private true, :column (int 1)})
  (let [v__6837__auto__ #'work]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.cast2slf4j" "work") {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cast2slf4j" "work")
        (delay
          (cast/register*
            cast/instance
            (fn fn__19144
              ([alarm]
                (let [temp__5823__auto__ (:ex alarm)]
                  (if temp__5823__auto__
                    (let [ex temp__5823__auto__
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
            (fn fn__19147
              ([event]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (slf4j/process event)))
                  nil)))
            :event)
          (cast/register*
            cast/instance
            (fn fn__19150
              ([p__19149]
                (let [map__19151 p__19149
                      map__19151 (if (seq? map__19151)
                                   (if (next map__19151)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__19151))
                                     (if (seq map__19151) (first map__19151) {}))
                                   map__19151)
                      metric map__19151
                      name (get map__19151 :name)
                      value (get map__19151 :value)]
                  (monitor/add-stat (cast-name->cloudwatch-name name) value))))
            :metric)
          (cast/register*
            cast/instance
            (fn fn__19153
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

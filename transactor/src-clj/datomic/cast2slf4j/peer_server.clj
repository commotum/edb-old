(do
  (clojure.core/in-ns 'datomic.cast2slf4j.peer-server)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cast2slf4j.peer-server)
    {:doc "Caster metrics for peer-server."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['cognitect.caster :as 'cast]
        ['datomic.client-spi :as 'client-spi]
        ['datomic.slf4j :as 'slf4j])))
  (when-not (.equals 'datomic.cast2slf4j.peer-server 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cast2slf4j.peer-server))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['cognitect.caster :as 'cast]
          ['datomic.client-spi :as 'client-spi]
          ['datomic.slf4j :as 'slf4j]))))
  (defn add-context ([m] (merge client-spi/*request-context* m)))
  (reset-meta!
    #'add-context
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'add-context :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.cast2slf4j.peer-server" "work")
    {:private true, :column (int 1)})
  (let [v__6812__auto__ #'work]
    (when-not (.hasRoot ^clojure.lang.Var v__6812__auto__)
      (.setMeta
        (clojure.lang.RT/var "datomic.cast2slf4j.peer-server" "work")
        {:private true, :column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.cast2slf4j.peer-server" "work")
        (delay
          (cast/register*
            cast/instance
            (fn fn__23992
              ([alarm]
                (let [alarm (add-context alarm) temp__5802__auto__ (:ex alarm)]
                  (if temp__5802__auto__
                    (let [ex temp__5802__auto__
                          logger (org.slf4j.LoggerFactory/getLogger
                                   "datomic.cast2slf4j.peer-server")
                          ex ex]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn ^org.slf4j.Logger logger (slf4j/process (dissoc alarm :ex)) ex)
                        (slf4j/caused-by logger ex))
                      nil)
                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                   "datomic.cast2slf4j.peer-server")]
                      (when (.isWarnEnabled ^org.slf4j.Logger logger)
                        (.warn ^org.slf4j.Logger logger (slf4j/process alarm)))
                      nil)))))
            :alert)
          (cast/register*
            cast/instance
            (fn fn__23995
              ([event]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j.peer-server")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (slf4j/process (add-context event))))
                  nil)))
            :event)
          (cast/register*
            cast/instance
            (fn fn__23997
              ([metric]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j.peer-server")]
                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                    (.info ^org.slf4j.Logger logger (slf4j/process metric)))
                  nil)))
            :metric)
          (cast/register*
            cast/instance
            (fn fn__23999
              ([dev]
                (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.cast2slf4j.peer-server")]
                  (when (.isDebugEnabled ^org.slf4j.Logger logger)
                    (.debug ^org.slf4j.Logger logger (slf4j/process (add-context dev))))
                  nil)))
            :dev)))
      #'work))
  (defn redirect ([] (deref work)))
  (reset-meta!
    #'redirect
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'redirect :ns *ns*)))
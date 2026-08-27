(do
  (clojure.core/in-ns 'datomic.callback)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.reflect :as 'reflect]
        ['clojure.set :as 'set]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.callback 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.callback))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.reflect :as 'reflect]
          ['clojure.set :as 'set]
          ['datomic.slf4j :as 'logger]))))
  (set! *warn-on-reflection* true)
  (defn has-callback-signature?
    ([cls mname]
      (when-not (symbol? mname)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list 'symbol? 'mname))))))
      (boolean
        (seq
          (filter
            (fn fn__10086
              ([member]
                (and
                  (= (:name member) mname)
                  (= (:parameter-types member) ['java.lang.Object])
                  (= #{:public :static} (set/intersection #{:public :static} (:flags member))))))
            (:members (reflect/reflect cls)))))))
  (defn compile-static-method-callback
    ([sym]
      (let [temp__5455__auto__ (re-matches #"(.*)\.(.*)" (str sym))]
        (if temp__5455__auto__
          (let [vec__10092 temp__5455__auto__
                _ (nth vec__10092 (int 0) nil)
                cname (nth vec__10092 (int 1) nil)
                mname (nth vec__10092 (int 2) nil)
                temp__5455__auto__ (try
                                     (java.lang.Class/forName ^java.lang.String cname)
                                     (catch java.lang.Throwable _ nil))]
            (if temp__5455__auto__
              (let [cls temp__5455__auto__]
                (if (has-callback-signature? cls (symbol mname))
                  (eval
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/fn)
                        (clojure.core/list
                          (apply vector (seq (concat (clojure.core/list 'x__10091__auto__)))))
                        (-> (symbol cname mname)
                         (clojure.core/list)
                         (concat (clojure.core/list 'x__10091__auto__))
                         (seq)
                         (clojure.core/list)))))
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process (str "Callback " sym "does not have required signature")))
                      nil)
                    nil)))
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process (str "Callback class " cname " could not be loaded")))
                  nil)
                nil)))
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process (str "Callback symbol " sym " does not have the required format")))
              nil)
            nil)))))
  (defn create-callback
    ([sym]
      (if (namespace sym)
        (do
          (clojure.core/require (symbol (namespace sym)))
          (let [temp__5455__auto__ (resolve sym)]
            (if temp__5455__auto__
              (let [cb temp__5455__auto__] cb)
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process (str "Callback " sym " does not exist")))
                  nil)
                nil))))
        (compile-static-method-callback sym)))))
(do
  (clojure.core/in-ns 'datomic.callback)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.callback)
    {:doc
     "Resolves configured callbacks to a Clojure function or public static Java method. Java callbacks accept one Object argument; Clojure callbacks accept one value."})
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
            (fn fn__22615
              ([member]
                (and
                  (= (:name member) mname)
                  (= (:parameter-types member) ['java.lang.Object])
                  (= #{:public :static} (set/intersection #{:public :static} (:flags member))))))
            (:members (reflect/reflect cls)))))))
  (reset-meta!
    #'has-callback-signature?
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['cls 'mname]
           {:pre [(.withMeta (clojure.core/list 'symbol? 'mname) {:column (int 10)})]})),
       :column (int 1)}
      :name
      'has-callback-signature?
      :ns
      *ns*))
  ;; Compile ClassName.method into a one-argument callback after verifying a
  ;; public static method whose sole parameter is Object.
  (defn compile-static-method-callback
    ([sym]
      (let [temp__5823__auto__ (re-matches #"(.*)\.(.*)" (str sym))]
        (if temp__5823__auto__
          (let [vec__22621 temp__5823__auto__
                _ (nth vec__22621 (int 0) nil)
                cname (nth vec__22621 (int 1) nil)
                mname (nth vec__22621 (int 2) nil)
                temp__5823__auto__ (try
                                     (java.lang.Class/forName ^java.lang.String cname)
                                     (catch java.lang.Throwable _ nil))]
            (if temp__5823__auto__
              (let [cls temp__5823__auto__]
                (if (has-callback-signature? cls (symbol mname))
                  (eval
                    (seq
                      (concat
                        (clojure.core/list 'clojure.core/fn)
                        (clojure.core/list
                          (apply vector (seq (concat (clojure.core/list 'x__22620__auto__)))))
                        (-> (symbol cname mname)
                         (clojure.core/list)
                         (concat (clojure.core/list 'x__22620__auto__))
                         (seq)
                         (clojure.core/list)))))
                  (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
                    (when (.isWarnEnabled ^org.slf4j.Logger logger)
                      (.warn
                        ^org.slf4j.Logger logger
                        (logger/process (str "Callback " sym "does not have required signature"))))
                    nil)))
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process (str "Callback class " cname " could not be loaded"))))
                nil)))
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
            (when (.isWarnEnabled ^org.slf4j.Logger logger)
              (.warn
                ^org.slf4j.Logger logger
                (logger/process
                  (str "Callback symbol " sym " does not have the required format"))))
            nil)))))
  (reset-meta!
    #'compile-static-method-callback
    (assoc
      {:arglists (clojure.core/list ['sym]), :column (int 1)}
      :name
      'compile-static-method-callback
      :ns
      *ns*))
  ;; Resolve a namespace-qualified symbol as a Clojure var; otherwise interpret
  ;; the symbol as a Java static method name. Missing vars and unsupported methods warn and return nil.
  (defn create-callback
    ([sym]
      (if (namespace sym)
        (do
          (clojure.core/require (symbol (namespace sym)))
          (let [temp__5823__auto__ (resolve sym)]
            (if temp__5823__auto__
              (let [cb temp__5823__auto__] cb)
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.callback")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process (str "Callback " sym " does not exist"))))
                nil))))
        (compile-static-method-callback sym))))
  (reset-meta!
    #'create-callback
    (assoc
      {:arglists (clojure.core/list ['sym]), :column (int 1)}
      :name
      'create-callback
      :ns
      *ns*)))

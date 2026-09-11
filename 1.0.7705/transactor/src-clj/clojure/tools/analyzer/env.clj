;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.


(ns clojure.tools.analyzer.env
  (:refer-clojure :exclude [ensure]))

(def ^:dynamic *env*
  "Global env atom containing a map.
   Required options:
    * :namespaces a map from namespace symbol to namespace map,
      the namespace map contains at least the following keys:
     ** :mappings a map of mappings of the namespace, symbol to var/class
     ** :aliases a map of the aliases of the namespace, symbol to symbol
     ** :ns a symbol representing the namespace"
  nil)

(defmacro with-env
  "Binds the global env to env, then executes the body"
  [env & body]
  `(let [env# ~env
         env# (cond
               (map? env#) (atom env#)
               (and (instance? clojure.lang.Atom env#)
                    (map? @env#)) env#
               :default (throw (ex-info (str "global env must be a map or atom containing a map, not "
                                             (class env#))
                                        {:env env#})))]
     (binding [*env* env#] ~@body)))

;; if *env* is not bound, bind it to env
(defmacro ensure
  "If *env* is not bound it binds it to env before executing the body"
  [env & body]
  `(if *env*
     (do ~@body)
     (with-env ~env
       ~@body)))

(defn deref-env
  "Returns the value of the current global env if bound, otherwise
   throws an exception."
  []
  (if *env*
    @*env*
    (throw (Exception. "global env not bound"))))

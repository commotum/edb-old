(do
  (clojure.core/in-ns 'datomic.extension-resolver)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['qualified-symbol?])
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['clojure.java.io :as 'io]
        ['datomic.common :as 'common :refer (clojure.core/list 'qualified-symbol?)])))
  (when-not (.equals 'datomic.extension-resolver 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.extension-resolver))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['qualified-symbol?])
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['clojure.java.io :as 'io]
          ['datomic.common :as 'common :refer (clojure.core/list 'qualified-symbol?)]))))
  (defn wildcard-name? ([s] (and (common/qualified-symbol? s) (= "*" (name s)))))
  (defn anom-map
    ([category msg]
      #:cognitect.anomalies{:category (keyword "cognitect.anomalies" (name category)),
                            :message msg}))
  (reset-meta!
    #'anom-map
    (assoc
      {:private true, :arglists (clojure.core/list ['category 'msg]), :column 1}
      :name
      'anom-map
      :ns
      *ns*))
  (defn anomaly!
    ([name msg cause] (do (throw (ex-info msg (anom-map name msg) cause)) nil))
    ([name msg] (do (throw (ex-info msg (anom-map name msg))) nil)))
  (reset-meta!
    #'anomaly!
    (assoc
      {:private true, :arglists (clojure.core/list ['name 'msg] ['name 'msg 'cause]), :column 1}
      :name
      'anomaly!
      :ns
      *ns*))
  (defn explicit-pred
    ([allow]
      (let [syms (into #{} (remove wildcard-name?) allow)]
        (fn fn__14303 ([p1__14302#] (contains? syms p1__14302#))))))
  (reset-meta!
    #'explicit-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column 1}
      :name
      'explicit-pred
      :ns
      *ns*))
  (defn wildcard-pred
    ([allow]
      (let [nses (into #{} (comp (filter wildcard-name?) (map namespace)) allow)]
        (fn fn__14307 ([p1__14306#] (contains? nses (namespace p1__14306#)))))))
  (reset-meta!
    #'wildcard-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column 1}
      :name
      'wildcard-pred
      :ns
      *ns*))
  (defn ensure-allow-list!
    ([allow_list]
      (when allow_list
        (when-not (coll? allow_list)
          (throw
            (ex-info
              "xforms expects a vector"
              {:cognitect.anomalies/category :cognitect.anomalies/incorrect, :value allow_list}))
          nil))))
  (reset-meta!
    #'ensure-allow-list!
    (assoc
      {:private true, :arglists (clojure.core/list ['allow-list]), :column 1}
      :name
      'ensure-allow-list!
      :ns
      *ns*))
  (defn ensure-extensions-config!
    ([p__14311]
      (let [map__14312 p__14311
            map__14312 (if (seq? map__14312)
                         (clojure.lang.PersistentHashMap/create (seq map__14312))
                         map__14312)
            xforms (get map__14312 :xforms)]
        (when xforms (ensure-allow-list! xforms)))))
  (reset-meta!
    #'ensure-extensions-config!
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['xforms]}]), :column 1}
      :name
      'ensure-extensions-config!
      :ns
      *ns*))
  (defn allow-list->pred
    ([allow]
      (ensure-allow-list! allow)
      (let [ep (explicit-pred allow) wp (wildcard-pred allow)]
        (fn fn__14314
          ([sym]
            (and
              (common/qualified-symbol? sym)
              (or (^clojure.lang.IFn ep sym) (^clojure.lang.IFn wp sym))))))))
  (reset-meta!
    #'allow-list->pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column 1}
      :name
      'allow-list->pred
      :ns
      *ns*))
  (defn load-extensions-config
    ([rsrc]
      (let [temp__5455__auto__ (io/resource rsrc)]
        (if temp__5455__auto__
          (let [r temp__5455__auto__ m (edn/read-string (slurp r))]
            (ensure-extensions-config! m)
            m)
          (anomaly! :not-found (str "'" rsrc "' is not on the classpath"))))))
  (def config-resource "datomic/extensions.edn")
  (defn load-preds
    ([rsrc]
      (let [config (load-extensions-config rsrc)] {:xforms (allow-list->pred (:xforms config))})))
  (reset-meta!
    #'load-preds
    (assoc
      {:private true, :arglists (clojure.core/list ['rsrc]), :column 1}
      :name
      'load-preds
      :ns
      *ns*))
  (def preds-ref (delay (load-preds config-resource)))
  (reset-meta! #'preds-ref (assoc {:private true, :column 1} :name 'preds-ref :ns *ns*))
  (defn allow? ([sym pred] ((^clojure.lang.IFn pred (deref preds-ref)) sym)))
  (reset-meta!
    #'allow?
    (assoc
      {:private true, :arglists (clojure.core/list ['sym 'pred]), :column 1}
      :name
      'allow?
      :ns
      *ns*))
  (defn user-namespaces
    ([path]
      (let [map__14325 (load-extensions-config path)
            map__14325 (if (seq? map__14325)
                         (clojure.lang.PersistentHashMap/create (seq map__14325))
                         map__14325)
            xforms (get map__14325 :xforms)]
        (into #{} (map namespace (remove nil? xforms))))))
  (defn preload!
    ([path]
      (loop [seq_14327 (seq (user-namespaces path)) chunk_14328 nil count_14329 0 i_14330 0]
        (if (< i_14330 count_14329)
          (let [ns (.nth ^clojure.lang.Indexed chunk_14328 (int i_14330))]
            (clojure.core/require (symbol ns))
            (recur seq_14327 chunk_14328 count_14329 (inc i_14330)))
          (let [temp__5457__auto__ (seq seq_14327)]
            (when temp__5457__auto__
              (let [seq_14327 temp__5457__auto__]
                (if (chunked-seq? seq_14327)
                  (let [c__5719__auto__ (chunk-first seq_14327)]
                    (recur
                      (chunk-rest seq_14327)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [ns (first seq_14327)]
                    (clojure.core/require (symbol ns))
                    (recur (next seq_14327) nil 0 0))))))))))
  (defn resolve!
    ([x context]
      (if (and (common/qualified-symbol? x) (allow? x context))
        (or
          (resolve x)
          (let [temp__5457__auto__ (namespace x)]
            (when temp__5457__auto__
              (let [nsname temp__5457__auto__]
                (clojure.core/require (symbol nsname))
                (or (resolve x) (anomaly! :not-found (str "Unable to resolve '" x "'")))))))
        (anomaly!
          :forbidden
          (str "'" x "' needs to be listed under " context " in datomic/extensions.edn")))))
  (defn resolve-built-in-xform
    ([sym]
      (when (contains? #{'clojure.edn/read-string 'name 'str 'namespace 'keyword 'symbol} sym)
        (resolve sym))))
  (defn resolve-xform! ([sym] (or (resolve-built-in-xform sym) (resolve! sym :xforms)))))
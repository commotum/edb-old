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
  (reset-meta!
    #'wildcard-name?
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'wildcard-name? :ns *ns*))
  (defn anom-map
    ([category msg]
      #:cognitect.anomalies{:category (keyword "cognitect.anomalies" (name category)),
                            :message msg}))
  (reset-meta!
    #'anom-map
    (assoc
      {:private true, :arglists (clojure.core/list ['category 'msg]), :column (int 1)}
      :name
      'anom-map
      :ns
      *ns*))
  (def anomaly!
   (fn anomaly_BANG_
     ([name msg cause] (throw (ex-info msg (anom-map name msg) cause)))
     ([name msg] (throw (ex-info msg (anom-map name msg))))))
  (reset-meta!
    #'anomaly!
    (assoc
      {:private true,
       :arglists (clojure.core/list ['name 'msg] ['name 'msg 'cause]),
       :column (int 1)}
      :name
      'anomaly!
      :ns
      *ns*))
  (defn explicit-pred
    ([allow]
      (let [syms (into #{} (remove wildcard-name?) allow)]
        (fn fn__16175 ([p1__16174#] (contains? syms p1__16174#))))))
  (reset-meta!
    #'explicit-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column (int 1)}
      :name
      'explicit-pred
      :ns
      *ns*))
  (defn wildcard-pred
    ([allow]
      (let [nses (into #{} (comp (filter wildcard-name?) (map namespace)) allow)]
        (fn fn__16179 ([p1__16178#] (contains? nses (namespace p1__16178#)))))))
  (reset-meta!
    #'wildcard-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column (int 1)}
      :name
      'wildcard-pred
      :ns
      *ns*))
  (def ensure-allow-list!
   (fn ensure_allow_list_BANG_
     ([allow_list]
       (when allow_list
         (when-not (coll? allow_list)
           (throw
             (ex-info
               "xforms expects a vector"
               {:cognitect.anomalies/category :cognitect.anomalies/incorrect, :value allow_list}))
           nil)))))
  (reset-meta!
    #'ensure-allow-list!
    (assoc
      {:private true, :arglists (clojure.core/list ['allow-list]), :column (int 1)}
      :name
      'ensure-allow-list!
      :ns
      *ns*))
  (def ensure-extensions-config!
   (fn ensure_extensions_config_BANG_
     ([p__16183]
       (let [map__16184 p__16183
             map__16184 (if (seq? map__16184)
                          (if (next map__16184)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__16184))
                            (if (seq map__16184) (first map__16184) {}))
                          map__16184)
             xforms (get map__16184 :xforms)]
         (when xforms (ensure-allow-list! xforms))))))
  (reset-meta!
    #'ensure-extensions-config!
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['xforms]}]), :column (int 1)}
      :name
      'ensure-extensions-config!
      :ns
      *ns*))
  (defn allow-list->pred
    ([allow]
      (ensure-allow-list! allow)
      (let [ep (explicit-pred allow) wp (wildcard-pred allow)]
        (fn fn__16186
          ([sym]
            (and
              (common/qualified-symbol? sym)
              (or (^clojure.lang.IFn ep sym) (^clojure.lang.IFn wp sym))))))))
  (reset-meta!
    #'allow-list->pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column (int 1)}
      :name
      'allow-list->pred
      :ns
      *ns*))
  (defn load-extensions-config
    ([rsrc]
      (let [temp__5823__auto__ (io/resource rsrc)]
        (if temp__5823__auto__
          (let [r temp__5823__auto__ m (edn/read-string (slurp r))]
            (ensure-extensions-config! m)
            m)
          (anomaly! :not-found (str "'" rsrc "' is not on the classpath"))))))
  (reset-meta!
    #'load-extensions-config
    (assoc
      {:arglists (clojure.core/list ['rsrc]), :column (int 1)}
      :name
      'load-extensions-config
      :ns
      *ns*))
  (def config-resource "datomic/extensions.edn")
  (reset-meta! #'config-resource (assoc {:column (int 1)} :name 'config-resource :ns *ns*))
  (defn load-preds
    ([rsrc]
      (let [config (load-extensions-config rsrc)] {:xforms (allow-list->pred (:xforms config))})))
  (reset-meta!
    #'load-preds
    (assoc
      {:private true, :arglists (clojure.core/list ['rsrc]), :column (int 1)}
      :name
      'load-preds
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.extension-resolver" "preds-ref")
    {:private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.extension-resolver" "preds-ref")
    (delay (load-preds config-resource)))
  (defn allow? ([sym pred] ((^clojure.lang.IFn pred (deref preds-ref)) sym)))
  (reset-meta!
    #'allow?
    (assoc
      {:private true, :arglists (clojure.core/list ['sym 'pred]), :column (int 1)}
      :name
      'allow?
      :ns
      *ns*))
  (defn user-namespaces
    ([path]
      (let [map__16197 (load-extensions-config path)
            map__16197 (if (seq? map__16197)
                         (if (next map__16197)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16197))
                           (if (seq map__16197) (first map__16197) {}))
                         map__16197)
            xforms (get map__16197 :xforms)]
        (into #{} (map namespace (remove nil? xforms))))))
  (reset-meta!
    #'user-namespaces
    (assoc
      {:arglists (clojure.core/list ['path]), :column (int 1)}
      :name
      'user-namespaces
      :ns
      *ns*))
  (defn preload!
    ([path]
      (loop [seq_16199 (seq (user-namespaces path)) chunk_16200 nil count_16201 0 i_16202 0]
        (if (< i_16202 count_16201)
          (let [ns (.nth ^clojure.lang.Indexed chunk_16200 (int i_16202))]
            (clojure.core/require (symbol ns))
            (recur seq_16199 chunk_16200 count_16201 (inc i_16202)))
          (let [temp__5825__auto__ (seq seq_16199)]
            (when temp__5825__auto__
              (let [seq_16199 temp__5825__auto__]
                (if (chunked-seq? seq_16199)
                  (let [c__6090__auto__ (chunk-first seq_16199)]
                    (recur
                      (chunk-rest seq_16199)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [ns (first seq_16199)]
                    (clojure.core/require (symbol ns))
                    (recur (next seq_16199) nil 0 0))))))))))
  (reset-meta!
    #'preload!
    (assoc {:arglists (clojure.core/list ['path]), :column (int 1)} :name 'preload! :ns *ns*))
  (defn resolve!
    ([x context]
      (if (and (common/qualified-symbol? x) (allow? x context))
        (or
          (resolve x)
          (let [temp__5825__auto__ (namespace x)]
            (when temp__5825__auto__
              (let [nsname temp__5825__auto__]
                (clojure.core/require (symbol nsname))
                (or (resolve x) (anomaly! :not-found (str "Unable to resolve '" x "'")))))))
        (anomaly!
          :forbidden
          (str "'" x "' needs to be listed under " context " in datomic/extensions.edn")))))
  (reset-meta!
    #'resolve!
    (assoc
      {:arglists (clojure.core/list ['x 'context]), :column (int 1)}
      :name
      'resolve!
      :ns
      *ns*))
  (defn resolve-built-in-xform
    ([sym]
      (when (contains? #{'clojure.edn/read-string 'name 'str 'namespace 'keyword 'symbol} sym)
        (resolve sym))))
  (reset-meta!
    #'resolve-built-in-xform
    (assoc
      {:arglists (clojure.core/list ['sym]), :column (int 1)}
      :name
      'resolve-built-in-xform
      :ns
      *ns*))
  (defn resolve-xform! ([sym] (or (resolve-built-in-xform sym) (resolve! sym :xforms))))
  (reset-meta!
    #'resolve-xform!
    (assoc {:arglists (clojure.core/list ['sym]), :column (int 1)} :name 'resolve-xform! :ns *ns*)))
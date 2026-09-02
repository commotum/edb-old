(do
  (clojure.core/in-ns 'datomic.extension-resolver)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.extension-resolver)
    {:doc
     "Loads the classpath extension policy and resolves functions used by pull transforms. datomic/extensions.edn controls which qualified symbols may be loaded; entries may name individual Vars or use namespace/* to allow every symbol in a namespace. A small set of data-conversion transforms is always available."})
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
  (defn anomaly!
    ([name msg cause] (throw (ex-info msg (anom-map name msg) cause)))
    ([name msg] (throw (ex-info msg (anom-map name msg)))))
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
        (fn fn__15070 ([p1__15069#] (contains? syms p1__15069#))))))
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
        (fn fn__15074 ([p1__15073#] (contains? nses (namespace p1__15073#)))))))
  (reset-meta!
    #'wildcard-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['allow]), :column (int 1)}
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
      {:private true,
       :arglists (clojure.core/list ['allow-list]),
       :doc
       "Validates an optional extension allowlist. A present value must be a collection; invalid configuration raises an :incorrect anomaly carrying the supplied value.",
       :column (int 1)}
      :name
      'ensure-allow-list!
      :ns
      *ns*))
  (defn ensure-extensions-config!
    ([p__15078]
      (let [map__15079 p__15078
            map__15079 (if (seq? map__15079)
                         (if (next map__15079)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15079))
                           (if (seq map__15079) (first map__15079) {}))
                         map__15079)
            xforms (get map__15079 :xforms)]
        (when xforms (ensure-allow-list! xforms)))))
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
        (fn fn__15081
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
      (let [temp__5802__auto__ (io/resource rsrc)]
        (if temp__5802__auto__
          (let [r temp__5802__auto__ m (edn/read-string (slurp r))]
            (ensure-extensions-config! m)
            m)
          (anomaly! :not-found (str "'" rsrc "' is not on the classpath"))))))
  (reset-meta!
    #'load-extensions-config
    (assoc
      {:arglists (clojure.core/list ['resource-name]),
       :doc
       "Reads and validates an EDN extension policy from a classpath resource. Raises a :not-found anomaly when the resource is absent.",
       :column (int 1)}
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
      {:private true,
       :arglists (clojure.core/list ['resource-name]),
       :doc
       "Compiles each configured allowlist into a predicate that accepts explicitly named symbols and namespace wildcards.",
       :column (int 1)}
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
      (let [map__15092 (load-extensions-config path)
            map__15092 (if (seq? map__15092)
                         (if (next map__15092)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15092))
                           (if (seq map__15092) (first map__15092) {}))
                         map__15092)
            xforms (get map__15092 :xforms)]
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
      (loop [seq_15094 (seq (user-namespaces path)) chunk_15095 nil count_15096 0 i_15097 0]
        (if (< i_15097 count_15096)
          (let [ns (.nth ^clojure.lang.Indexed chunk_15095 (int i_15097))]
            (clojure.core/require (symbol ns))
            (recur seq_15094 chunk_15095 count_15096 (inc i_15097)))
          (let [temp__5804__auto__ (seq seq_15094)]
            (when temp__5804__auto__
              (let [seq_15094 temp__5804__auto__]
                (if (chunked-seq? seq_15094)
                  (let [c__6065__auto__ (chunk-first seq_15094)]
                    (recur
                      (chunk-rest seq_15094)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [ns (first seq_15094)]
                    (clojure.core/require (symbol ns))
                    (recur (next seq_15094) nil 0 0))))))))))
  (reset-meta!
    #'preload!
    (assoc
      {:arglists (clojure.core/list ['path]),
       :doc
       "Requires every namespace named by the extension policy so permitted transform Vars are available before use.",
       :column (int 1)}
      :name
      'preload!
      :ns
      *ns*))
  (defn resolve!
    ([x context]
      (if (and (common/qualified-symbol? x) (allow? x context))
        (or
          (resolve x)
          (let [temp__5804__auto__ (namespace x)]
            (when temp__5804__auto__
              (let [nsname temp__5804__auto__]
                (clojure.core/require (symbol nsname))
                (or (resolve x) (anomaly! :not-found (str "Unable to resolve '" x "'")))))))
        (anomaly!
          :forbidden
          (str "'" x "' needs to be listed under " context " in datomic/extensions.edn")))))
  (reset-meta!
    #'resolve!
    (assoc
      {:arglists (clojure.core/list ['symbol 'policy-key]),
       :doc
       "Resolves an allowlisted qualified symbol, requiring its namespace on demand. Raises :forbidden when the symbol is outside policy and :not-found when an allowed symbol cannot be resolved.",
       :column (int 1)}
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
      {:arglists (clojure.core/list ['symbol]),
       :doc
       "Resolves a built-in pull transform. The built-ins are str, name, namespace, keyword, symbol, and clojure.edn/read-string.",
       :column (int 1)}
      :name
      'resolve-built-in-xform
      :ns
      *ns*))
  (defn resolve-xform! ([sym] (or (resolve-built-in-xform sym) (resolve! sym :xforms))))
  (reset-meta!
    #'resolve-xform!
    (assoc
      {:arglists (clojure.core/list ['symbol]),
       :doc
       "Resolves a pull transform from the built-in set or from the :xforms allowlist in datomic/extensions.edn.",
       :column (int 1)}
      :name
      'resolve-xform!
      :ns
      *ns*)))

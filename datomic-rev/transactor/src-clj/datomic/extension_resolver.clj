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
    ([name msg cause] (throw (ex-info msg (anom-map name msg) cause)))
    ([name msg] (throw (ex-info msg (anom-map name msg)))))
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
        (fn fn__15039 ([p1__15038#] (contains? syms p1__15038#))))))
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
        (fn fn__15043 ([p1__15042#] (contains? nses (namespace p1__15042#)))))))
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
    ([p__15047]
      (let [map__15048 p__15047
            map__15048 (if (seq? map__15048)
                         (if (next map__15048)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15048))
                           (if (seq map__15048) (first map__15048) {}))
                         map__15048)
            xforms (get map__15048 :xforms)]
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
        (fn fn__15050
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
      (let [temp__5802__auto__ (io/resource rsrc)]
        (if temp__5802__auto__
          (let [r temp__5802__auto__ m (edn/read-string (slurp r))]
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
      (let [map__15061 (load-extensions-config path)
            map__15061 (if (seq? map__15061)
                         (if (next map__15061)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15061))
                           (if (seq map__15061) (first map__15061) {}))
                         map__15061)
            xforms (get map__15061 :xforms)]
        (into #{} (map namespace (remove nil? xforms))))))
  (defn preload!
    ([path]
      (loop [seq_15063 (seq (user-namespaces path)) chunk_15064 nil count_15065 0 i_15066 0]
        (if (< i_15066 count_15065)
          (let [ns (.nth ^clojure.lang.Indexed chunk_15064 (int i_15066))]
            (clojure.core/require (symbol ns))
            (recur seq_15063 chunk_15064 count_15065 (inc i_15066)))
          (let [temp__5804__auto__ (seq seq_15063)]
            (when temp__5804__auto__
              (let [seq_15063 temp__5804__auto__]
                (if (chunked-seq? seq_15063)
                  (let [c__6065__auto__ (chunk-first seq_15063)]
                    (recur
                      (chunk-rest seq_15063)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [ns (first seq_15063)]
                    (clojure.core/require (symbol ns))
                    (recur (next seq_15063) nil 0 0))))))))))
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
  (defn resolve-built-in-xform
    ([sym]
      (when (contains? #{'clojure.edn/read-string 'name 'str 'namespace 'keyword 'symbol} sym)
        (resolve sym))))
  (defn resolve-xform! ([sym] (or (resolve-built-in-xform sym) (resolve! sym :xforms)))))
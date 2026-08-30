(do
  (clojure.core/in-ns 'datomic.function)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'compare))
      (clojure.core/require
        'datomic.janino
        ['datomic.error :as 'error]
        ['datomic.common :as 'common :refer (clojure.core/list 'compare)]
        ['datomic.memory-size :as 'size])
      (clojure.core/import 'datomic.functions.Fn)
      (clojure.core/import 'datomic.functions.Fn0)
      (clojure.core/import 'datomic.functions.Fn1)
      (clojure.core/import 'datomic.functions.Fn2)
      (clojure.core/import 'datomic.functions.Fn3)
      (clojure.core/import 'datomic.functions.Fn4)
      (clojure.core/import 'datomic.functions.Fn5)
      (clojure.core/import 'datomic.functions.Fn6)
      (clojure.core/import 'datomic.functions.Fn7)
      (clojure.core/import 'datomic.functions.Fn8)
      (clojure.core/import 'datomic.functions.Fn9)
      (clojure.core/import 'datomic.functions.Fn10)))
  (when-not (.equals 'datomic.function 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.function))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'compare))
        (clojure.core/require
          'datomic.janino
          ['datomic.error :as 'error]
          ['datomic.common :as 'common :refer (clojure.core/list 'compare)]
          ['datomic.memory-size :as 'size])
        (clojure.core/import 'datomic.functions.Fn)
        (clojure.core/import 'datomic.functions.Fn0)
        (clojure.core/import 'datomic.functions.Fn1)
        (clojure.core/import 'datomic.functions.Fn2)
        (clojure.core/import 'datomic.functions.Fn3)
        (clojure.core/import 'datomic.functions.Fn4)
        (clojure.core/import 'datomic.functions.Fn5)
        (clojure.core/import 'datomic.functions.Fn6)
        (clojure.core/import 'datomic.functions.Fn7)
        (clojure.core/import 'datomic.functions.Fn8)
        (clojure.core/import 'datomic.functions.Fn9)
        (clojure.core/import 'datomic.functions.Fn10))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.function" "->Function")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.function" "map->Function")
    {:declared true, :column (int 1)})
  (defrecord
    Function
    [lang imports requires params code fnref]
    datomic.functions.Fn10
    datomic.functions.Fn4
    datomic.functions.Fn2
    datomic.functions.Fn6
    datomic.functions.Fn7
    datomic.memory_size.MemorySize
    datomic.functions.Fn
    datomic.functions.Fn1
    java.lang.Comparable
    clojure.lang.IFn
    datomic.functions.Fn9
    datomic.functions.Fn8
    datomic.functions.Fn0
    datomic.functions.Fn5
    datomic.functions.Fn3
    (applyTo
      [this ^clojure.lang.ISeq args]
      (clojure.lang.AFn/applyToHelper this ^clojure.lang.ISeq args))
    (invoke [this a1 a2 a3 a4 a5 a6 a7 a8 a9 a10] ((deref fnref) a1 a2 a3 a4 a5 a6 a7 a8 a9 a10))
    (invoke [this a1 a2 a3 a4 a5 a6 a7 a8 a9] ((deref fnref) a1 a2 a3 a4 a5 a6 a7 a8 a9))
    (invoke [this a1 a2 a3 a4 a5 a6 a7 a8] ((deref fnref) a1 a2 a3 a4 a5 a6 a7 a8))
    (invoke [this a1 a2 a3 a4 a5 a6 a7] ((deref fnref) a1 a2 a3 a4 a5 a6 a7))
    (invoke [this a1 a2 a3 a4 a5 a6] ((deref fnref) a1 a2 a3 a4 a5 a6))
    (invoke [this a1 a2 a3 a4 a5] ((deref fnref) a1 a2 a3 a4 a5))
    (invoke [this a1 a2 a3 a4] ((deref fnref) a1 a2 a3 a4))
    (invoke [this a1 a2 a3] ((deref fnref) a1 a2 a3))
    (invoke [this a1 a2] ((deref fnref) a1 a2))
    (invoke [this a1] ((deref fnref) a1))
    (invoke [this] ((deref fnref)))
    (^int compareTo
      [this o]
      (int
        (let [c (common/compare lang (:lang o))]
          (if (not (zero? c))
            c
            (let [c (common/compare params (:params o))]
              (if (not (zero? c)) c (common/compare code (:code o))))))))
    (^java.lang.String code [this] ^java.lang.String code)
    (^java.util.List params [this] (mapv str params))
    (^java.lang.String lang [this] (name lang))
    (memory-size [this] (+ 1024 (size/memory-size code))))
  (clojure.core/import 'datomic.function.Function)
  (defn ->Function
    ([lang imports requires params code fnref]
      (datomic.function.Function. lang imports requires params code fnref)))
  (reset-meta!
    #'->Function
    (assoc
      {:arglists (clojure.core/list ['lang 'imports 'requires 'params 'code 'fnref]),
       :column (int 1)}
      :name
      '->Function
      :ns
      *ns*))
  (defn map->Function
    ([m__7972__auto__]
      (Function/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->Function
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->Function
      :ns
      *ns*))
  (def print-function
   (fn print_function
     ([dbfn w]
       (.write
         ^java.io.Writer w
         (str "#db/fn" (select-keys dbfn [:lang :imports :requires :params :code])))
       nil)))
  (reset-meta!
    #'print-function
    (assoc
      {:private true,
       :arglists (clojure.core/list ['dbfn (.withMeta 'w {:tag 'java.io.Writer})]),
       :column (int 1)}
      :name
      'print-function
      :ns
      *ns*))
  (defmethod print-method datomic.function.Function fn__9257 ([dbfn w] (print-function dbfn w)))
  (defmethod print-dup datomic.function.Function fn__9259 ([dbfn w] (print-function dbfn w)))
  (defn compile-clojure
    ([imports requires params code]
      (binding [*ns* *ns*]
        (let [code (read-string code)
              expr (seq
                     (concat
                       (clojure.core/list 'clojure.core/fn)
                       (clojure.core/list params)
                       (clojure.core/list code)))
              gns (gensym "ns_")]
          (clojure.core/in-ns gns)
          (clojure.core/refer 'clojure.core)
          (clojure.core/use ['datomic.api :as 'd :only (clojure.core/list 'q 'db)])
          (when imports (eval (seq (concat (clojure.core/list 'clojure.core/import) imports))))
          (when (seq requires) (apply clojure.core/require requires))
          (let [f (eval expr)] (clojure.lang.Namespace/remove ^clojure.lang.Symbol gns) f)))))
  (reset-meta!
    #'compile-clojure
    (assoc
      {:arglists (clojure.core/list ['imports 'requires 'params 'code]), :column (int 1)}
      :name
      'compile-clojure
      :ns
      *ns*))
  (defn normalize
    ([m]
      (let [map__9263 (common/force-map-keywords m)
            map__9263 (if (seq? map__9263)
                        (if (next map__9263)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9263))
                          (if (seq map__9263) (first map__9263) {}))
                        map__9263)
            lang (get map__9263 :lang)
            imports (get map__9263 :imports)
            requires (get map__9263 :requires)
            params (get map__9263 :params)
            code (get map__9263 :code)
            lang (keyword lang)
            params (mapv symbol params)
            code (if (string? code)
                   code
                   (binding [*print-length* nil *print-level* nil *print-meta* true]
                     (pr-str code)))
            vectorize (fn vectorize ([o] (if (instance? java.util.List o) (into [] o) o)))]
        (when-not (and lang params code)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                "Must supply lang, params and code"
                "\n"
                (pr-str (clojure.core/list 'and 'lang 'params 'code))))))
        {:lang lang,
         :imports (mapv vectorize imports),
         :requires
         (mapv vectorize (mapv (fn fn__9268 ([p1__9262#] (mapv vectorize p1__9262#))) requires)),
         :params params,
         :code code})))
  (reset-meta!
    #'normalize
    (assoc
      {:private true, :arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'normalize
      :ns
      *ns*))
  (defn construct
    ([m]
      (let [map__9273 (normalize m)
            map__9273 (if (seq? map__9273)
                        (if (next map__9273)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9273))
                          (if (seq map__9273) (first map__9273) {}))
                        map__9273)
            lang (get map__9273 :lang)
            imports (get map__9273 :imports)
            requires (get map__9273 :requires)
            params (get map__9273 :params)
            code (get map__9273 :code)
            fnref (let [G__9274 lang]
                    (case
                      G__9274
                      :java
                      (delay (datomic.janino/java-data-fn params code))
                      :clojure
                      (delay (compile-clojure imports requires params code))
                      (error/arg
                        :db.error/source-lang-not-supported
                        (str "Source lang: " lang " not supported yet."))))]
        (datomic.function.Function. lang imports requires params code fnref))))
  (reset-meta!
    #'construct
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'construct :ns *ns*)))
(do
  (set! *warn-on-reflection* true)
  (clojure.core/in-ns 'datomic.janino)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.janino)
    {:doc
     "Java database-function compilation. Adapts a Java expression body to the fixed Fn0 through Fn10 invocation interfaces used by database function values."})
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.janino 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.janino))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (defn java-data-fn
    "Compiles a Java database-function body with the supplied parameter names and returns a Clojure-callable function. The parameter count selects Fn0 through Fn10 and therefore cannot exceed ten."
    ([params body]
      (let [se (let [G__11229 (org.codehaus.commons.compiler.jdk.ScriptEvaluator.)]
                 (.setDefaultImports
                   ^org.codehaus.commons.compiler.jdk.ScriptEvaluator G__11229
                   (into-array
                     ["static datomic.Util.*"
                      "static datomic.Peer.*"
                      "datomic.functions.*"
                      "datomic.Database"
                      "datomic.Datom"
                      "static datomic.Database.*"]))
                 G__11229)
            iname (str "datomic.functions.Fn" (java.lang.Integer/valueOf (int (count params))))
            iface (java.lang.Class/forName ^java.lang.String iname)
            proc (.createFastEvaluator
                   ^org.codehaus.commons.compiler.jdk.ScriptEvaluator se
                   ^java.lang.String body
                   ^java.lang.Class iface
                   (into-array java.lang.String (map str params)))
            gproc (with-meta (gensym) {:tag (symbol iname)})
            params (mapv symbol params)
            closer (eval
                     (seq
                       (concat
                         (clojure.core/list 'clojure.core/fn)
                         (clojure.core/list
                           (apply vector (seq (concat (clojure.core/list gproc)))))
                         (clojure.core/list
                           (seq
                             (concat
                               (clojure.core/list 'clojure.core/fn)
                               (clojure.core/list params)
                               (clojure.core/list
                                 (seq
                                   (concat
                                     (clojure.core/list '.invoke)
                                     (clojure.core/list gproc)
                                     params)))))))))]
        (^clojure.lang.IFn closer proc))))
  (reset-meta!
    #'java-data-fn
    (assoc
      {:arglists (clojure.core/list ['params (.withMeta 'body {:tag 'String})]),
       :doc
       "Compiles a Java database-function body with the supplied parameter names and returns a Clojure-callable function. The parameter count selects Fn0 through Fn10 and therefore cannot exceed ten.",
       :column (int 1)}
      :name
      'java-data-fn
      :ns
      *ns*)))

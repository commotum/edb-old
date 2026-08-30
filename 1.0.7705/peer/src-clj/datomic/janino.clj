(do
  (set! *warn-on-reflection* true)
  (clojure.core/in-ns 'datomic.janino)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.janino 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.janino))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (def java-data-fn
   (fn java_data_fn
     ([params body]
       (let [se (let [G__10796 (org.codehaus.commons.compiler.jdk.ScriptEvaluator.)]
                  (.setDefaultImports
                    ^org.codehaus.commons.compiler.jdk.ScriptEvaluator G__10796
                    (into-array
                      ["static datomic.Util.*"
                       "static datomic.Peer.*"
                       "datomic.functions.*"
                       "datomic.Database"
                       "datomic.Datom"
                       "static datomic.Database.*"]))
                  G__10796)
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
         (^clojure.lang.IFn closer proc)))))
  (reset-meta!
    #'java-data-fn
    (assoc
      {:arglists (clojure.core/list ['params (.withMeta 'body {:tag 'String})]), :column (int 1)}
      :name
      'java-data-fn
      :ns
      *ns*)))
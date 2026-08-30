(do
  (clojure.core/in-ns 'datomic.assert)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['assert])
      (clojure.core/require ['clojure.main :as 'main])))
  (when-not (.equals 'datomic.assert 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.assert))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['assert])
        (clojure.core/require ['clojure.main :as 'main]))))
  (def ^{:dynamic true} *level* 0)
  (reset-meta! #'*level* (assoc {:dynamic true, :column (int 1)} :name '*level* :ns *ns*))
  (def ^{:dynamic true} *result* 0)
  (reset-meta! #'*result* (assoc {:dynamic true, :column (int 1)} :name '*result* :ns *ns*))
  (def ^{:dynamic true} *assert-handler* nil)
  (reset-meta!
    #'*assert-handler*
    (assoc {:dynamic true, :column (int 1)} :name '*assert-handler* :ns *ns*))
  (defn local-bindings
    ([env]
      (let [symbols (map key env)]
        (zipmap
          (map
            (fn fn__21875
              ([sym] (seq (concat (clojure.core/list 'quote) (clojure.core/list sym)))))
            symbols)
          symbols))))
  (reset-meta!
    #'local-bindings
    (assoc
      {:private true, :arglists (clojure.core/list ['env]), :column (int 1)}
      :name
      'local-bindings
      :ns
      *ns*))
  (defn assertion-repl
    ([error]
      (println
        "Assertion failed, entering subrepl. See *e for details.\nWhen you close the input stream, the last REPL value will become the\nresult of the assertion expression (and be thrown if a Throwable).")
      (push-thread-bindings (hash-map #'*level* (inc *level*) #'*result* nil))
      (try
        (let [stashing_eval (fn stashing_eval
                              ([x] (let [result (eval x)] (set! *result* result) result)))
              prompt (fn prompt ([] (print (str "(" *level* ")=> "))))]
          (main/repl :prompt prompt :eval stashing_eval :init (fn fn__21882 ([] (set! *e error))))
          (when (instance? java.lang.Throwable *result*) (throw *result*))
          *result*)
        (finally (pop-thread-bindings)))))
  (reset-meta!
    #'assertion-repl
    (assoc
      {:arglists (clojure.core/list ['error]), :column (int 1)}
      :name
      'assertion-repl
      :ns
      *ns*))
  (def assert
   (fn assert
     ([&form &env x msg]
       (when *assert*
         (let [bindings (local-bindings &env)]
           (seq
             (concat
               (clojure.core/list 'clojure.core/when-not)
               (clojure.core/list x)
               (clojure.core/list
                 (seq
                   (concat
                     (clojure.core/list 'clojure.core/let)
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list 'form__21885__auto__)
                             (clojure.core/list
                               (seq (concat (clojure.core/list 'quote) (clojure.core/list x))))
                             (clojure.core/list 'error__21886__auto__)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'clojure.core/ex-info)
                                   (clojure.core/list msg)
                                   (clojure.core/list
                                     (apply
                                       hash-map
                                       (seq
                                         (concat
                                           (clojure.core/list :form)
                                           (clojure.core/list 'form__21885__auto__)
                                           (clojure.core/list :bindings)
                                           (clojure.core/list bindings))))))))))))
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'if)
                           (clojure.core/list 'datomic.assert/*assert-handler*)
                           (clojure.core/list
                             (seq
                               (concat
                                 (clojure.core/list 'datomic.assert/*assert-handler*)
                                 (clojure.core/list 'error__21886__auto__))))
                           (clojure.core/list
                             (seq
                               (concat
                                 (clojure.core/list 'throw)
                                 (clojure.core/list 'error__21886__auto__)))))))))))))))
     ([&form &env x]
       (seq
         (concat
           (clojure.core/list 'datomic.assert/assert)
           (clojure.core/list x)
           (clojure.core/list "Assertion failed, see ex-data for details"))))))
  (reset-meta!
    #'assert
    (assoc {:arglists (clojure.core/list ['x] ['x 'msg]), :column (int 1)} :name 'assert :ns *ns*))
  (.setMacro #'assert))
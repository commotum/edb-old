(do
  (clojure.core/in-ns 'datomic.error)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.pprint :as 'pp] ['clojure.reflect :as 'reflect])))
  (when-not (.equals 'datomic.error 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.error))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.pprint :as 'pp] ['clojure.reflect :as 'reflect]))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.error" "anomalize") {:column (int 1)})
  (let [v__5792__auto__ #'anomalize]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.error" "anomalize") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.error" "anomalize")
        (clojure.lang.MultiFn.
          "anomalize"
          (fn fn__8482 ([m cls msg] cls))
          :default
          #'clojure.core/global-hierarchy))
      #'anomalize))
  (defmethod
    anomalize
    datomic.impl.Exceptions$IllegalArgumentExceptionInfo
    fn__8487
    ([m cls msg]
      (merge #:cognitect.anomalies{:category :cognitect.anomalies/incorrect, :message msg} m)))
  (defmethod
    anomalize
    datomic.impl.Exceptions$IllegalStateExceptionInfo
    fn__8489
    ([m cls msg]
      (merge #:cognitect.anomalies{:category :cognitect.anomalies/conflict, :message msg} m)))
  (defmethod anomalize :default fn__8491 ([m _ _] m))
  (def create
   (fn create
     ([&form &env cls code msg details cause]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq (concat (clojure.core/list 'msg__8494__auto__) (clojure.core/list msg)))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'new)
                 (clojure.core/list cls)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/str)
                       (clojure.core/list code)
                       (clojure.core/list " ")
                       (clojure.core/list 'msg__8494__auto__))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.error/anomalize)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'clojure.core/assoc)
                             (clojure.core/list details)
                             (clojure.core/list :db/error)
                             (clojure.core/list code))))
                       (clojure.core/list cls)
                       (clojure.core/list 'msg__8494__auto__))))
                 (clojure.core/list cause)))))))
     ([&form &env cls code msg details]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq (concat (clojure.core/list 'msg__8493__auto__) (clojure.core/list msg)))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'new)
                 (clojure.core/list cls)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/str)
                       (clojure.core/list code)
                       (clojure.core/list " ")
                       (clojure.core/list 'msg__8493__auto__))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.error/anomalize)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'clojure.core/assoc)
                             (clojure.core/list details)
                             (clojure.core/list :db/error)
                             (clojure.core/list code))))
                       (clojure.core/list cls)
                       (clojure.core/list 'msg__8493__auto__))))))))))
     ([&form &env cls code msg]
       (seq
         (concat
           (clojure.core/list 'datomic.error/create)
           (clojure.core/list cls)
           (clojure.core/list code)
           (clojure.core/list msg)
           (clojure.core/list nil))))
     ([&form &env cls code]
       (seq
         (concat
           (clojure.core/list 'datomic.error/create)
           (clojure.core/list cls)
           (clojure.core/list code)
           (clojure.core/list "")
           (clojure.core/list nil))))))
  (reset-meta!
    #'create
    (assoc
      {:arglists
       (clojure.core/list
         ['cls 'code]
         ['cls 'code 'msg]
         ['cls 'code 'msg 'details]
         ['cls 'code 'msg 'details 'cause]),
       :column (int 1)}
      :name
      'create
      :ns
      *ns*))
  (.setMacro #'create)
  (def raise
   (fn raise
     ([code msg details cause]
       (throw
         (let [msg__8494__auto__ msg]
           (clojure.lang.ExceptionInfo.
             (str code " " msg__8494__auto__)
             (anomalize
               (assoc details :db/error code)
               clojure.lang.ExceptionInfo
               msg__8494__auto__)
             ^java.lang.Throwable cause))))
     ([code msg details]
       (throw
         (let [msg__8493__auto__ msg]
           (clojure.lang.ExceptionInfo.
             (str code " " msg__8493__auto__)
             (anomalize
               (assoc details :db/error code)
               clojure.lang.ExceptionInfo
               msg__8493__auto__)))))
     ([code msg] (raise code msg nil))
     ([code] (raise code "" nil))))
  (reset-meta!
    #'raise
    (assoc
      {:arglists
       (clojure.core/list ['code] ['code 'msg] ['code 'msg 'details] ['code 'msg 'details 'cause]),
       :column (int 1)}
      :name
      'raise
      :ns
      *ns*))
  (def arg
   (fn arg
     ([code msg details cause]
       (throw
         (let [msg__8494__auto__ msg]
           (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
             (str code " " msg__8494__auto__)
             (anomalize
               (assoc details :db/error code)
               datomic.impl.Exceptions$IllegalArgumentExceptionInfo
               msg__8494__auto__)
             ^java.lang.Throwable cause))))
     ([code msg details]
       (throw
         (let [msg__8493__auto__ msg]
           (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
             (str code " " msg__8493__auto__)
             (anomalize
               (assoc details :db/error code)
               datomic.impl.Exceptions$IllegalArgumentExceptionInfo
               msg__8493__auto__)))))
     ([code msg] (arg code msg nil))
     ([code] (arg code "" nil))))
  (reset-meta!
    #'arg
    (assoc
      {:arglists
       (clojure.core/list ['code] ['code 'msg] ['code 'msg 'details] ['code 'msg 'details 'cause]),
       :column (int 1)}
      :name
      'arg
      :ns
      *ns*))
  (def eval-exception
   (fn eval_exception
     ([p__8502 t]
       (let [map__8503 p__8502
             map__8503 (if (seq? map__8503)
                         (if (next map__8503)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8503))
                           (if (seq map__8503) (first map__8503) {}))
                         map__8503)
             cmap map__8503
             context (get map__8503 :context)
             expr (get map__8503 :expr)
             arguments (get map__8503 :arguments)
             msg (str "Error evaluating " (name context) ": " expr)]
         (ex-info
           msg
           {:cognitect.anomalies/category :cognitect.anomalies/fault,
            :cognitect.anomalies/message msg,
            :datomic/eval-exception cmap}
           t)))))
  (reset-meta!
    #'eval-exception
    (assoc
      {:arglists (clojure.core/list [{:keys ['context 'expr 'arguments], :as 'cmap} 't]),
       :column (int 1)}
      :name
      'eval-exception
      :ns
      *ns*))
  (defn add-details-to-msg
    ([msg details]
      (if details
        (do
          (push-thread-bindings (hash-map #'*print-length* 10 #'*print-level* 5))
          (str
            msg
            "\n"
            (try
              (let [s__6419__auto__ (java.io.StringWriter.)]
                (binding [*out* s__6419__auto__] (do (pp/pprint details) (str s__6419__auto__))))
              (finally (pop-thread-bindings)))))
        msg)))
  (reset-meta!
    #'add-details-to-msg
    (assoc
      {:arglists (clojure.core/list ['msg 'details]), :column (int 1)}
      :name
      'add-details-to-msg
      :ns
      *ns*))
  (def argd
   (fn argd
     ([code msg details cause]
       (throw
         (let [msg__8494__auto__ (add-details-to-msg msg details)]
           (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
             (str code " " msg__8494__auto__)
             (anomalize
               (assoc details :db/error code)
               datomic.impl.Exceptions$IllegalArgumentExceptionInfo
               msg__8494__auto__)
             ^java.lang.Throwable cause))))
     ([code msg details]
       (throw
         (let [msg__8493__auto__ (add-details-to-msg msg details)]
           (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
             (str code " " msg__8493__auto__)
             (anomalize
               (assoc details :db/error code)
               datomic.impl.Exceptions$IllegalArgumentExceptionInfo
               msg__8493__auto__)))))
     ([code msg] (arg code msg nil))
     ([code] (arg code "" nil))))
  (reset-meta!
    #'argd
    (assoc
      {:arglists
       (clojure.core/list ['code] ['code 'msg] ['code 'msg 'details] ['code 'msg 'details 'cause]),
       :column (int 1)}
      :name
      'argd
      :ns
      *ns*))
  (def state
   (fn state
     ([code msg details cause]
       (throw
         (let [msg__8494__auto__ msg]
           (datomic.impl.Exceptions$IllegalStateExceptionInfo.
             (str code " " msg__8494__auto__)
             (anomalize
               (assoc details :db/error code)
               datomic.impl.Exceptions$IllegalStateExceptionInfo
               msg__8494__auto__)
             ^java.lang.Throwable cause))))
     ([code msg details]
       (throw
         (let [msg__8493__auto__ msg]
           (datomic.impl.Exceptions$IllegalStateExceptionInfo.
             (str code " " msg__8493__auto__)
             (anomalize
               (assoc details :db/error code)
               datomic.impl.Exceptions$IllegalStateExceptionInfo
               msg__8493__auto__)))))
     ([code msg] (state code msg nil))
     ([code] (state code "" nil))))
  (reset-meta!
    #'state
    (assoc
      {:arglists
       (clojure.core/list ['code] ['code 'msg] ['code 'msg 'details] ['code 'msg 'details 'cause]),
       :column (int 1)}
      :name
      'state
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.error" "reporter") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.error" "reporter")
    (atom (fn fn__8515 ([t] (.printStackTrace ^java.lang.Throwable t) nil))))
  (defn has-string-constructor?
    ([cls]
      (not
        (empty?
          (filter
            (fn fn__8519 ([p1__8518#] (= ['java.lang.String] (:parameter-types p1__8518#))))
            (filter
              (fn fn__8521 ([p1__8517#] (instance? clojure.reflect.Constructor p1__8517#)))
              (:members (reflect/reflect cls))))))))
  (reset-meta!
    #'has-string-constructor?
    (assoc
      {:private true, :arglists (clojure.core/list ['cls]), :column (int 1)}
      :name
      'has-string-constructor?
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.error" "exception-deserializer") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.error" "exception-deserializer")
    (memoize
      (fn fn__8525
        ([classname]
          (if (and
                (.startsWith ^java.lang.String classname "java")
                (has-string-constructor? (java.lang.Class/forName ^java.lang.String classname)))
            (eval
              (seq
                (concat
                  (clojure.core/list 'clojure.core/fn)
                  (clojure.core/list
                    (apply vector (seq (concat (clojure.core/list 'msg__8524__auto__)))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'new)
                        (clojure.core/list (symbol classname))
                        (clojure.core/list 'msg__8524__auto__)))))))
            (fn fn__8526 ([msg] (java.lang.RuntimeException. ^java.lang.String msg))))))))
  (def deserialize-exception
   (fn deserialize_exception
     ([p__8530]
       (let [map__8531 p__8530
             map__8531 (if (seq? map__8531)
                         (if (next map__8531)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8531))
                           (if (seq map__8531) (first map__8531) {}))
                         map__8531)
             classname (get map__8531 :classname)
             error (get map__8531 :error)
             error_data (get map__8531 :error-data)]
         (if error_data
           (let [G__8532 classname]
             (case
               G__8532
               "datomic.impl.Exceptions$IllegalStateExceptionInfo"
               (datomic.impl.Exceptions$IllegalStateExceptionInfo.
                 ^java.lang.String error
                 ^clojure.lang.IPersistentMap error_data)
               "datomic.impl.Exceptions$IllegalArgumentExceptionInfo"
               (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
                 ^java.lang.String error
                 ^clojure.lang.IPersistentMap error_data)
               "clojure.lang.ExceptionInfo"
               (ex-info error error_data)
               (java.lang.RuntimeException. ^java.lang.String error)))
           ((exception-deserializer classname) error))))))
  (reset-meta!
    #'deserialize-exception
    (assoc
      {:arglists (clojure.core/list [{:keys ['classname 'error 'error-data]}]), :column (int 1)}
      :name
      'deserialize-exception
      :ns
      *ns*))
  (defn report ([t] ((deref reporter) t)))
  (reset-meta!
    #'report
    (assoc {:arglists (clojure.core/list ['t]), :column (int 1)} :name 'report :ns *ns*))
  (defn runonce
    ([f]
      (let [sentinel (java.lang.Object.) result (atom sentinel)]
        (fn fn__8535
          ([& args]
            (locking sentinel
             (if (= (deref result) sentinel) (reset! result (apply f args)) (deref result))))))))
  (reset-meta!
    #'runonce
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'runonce :ns *ns*))
  (def with-unwind*
   (fn with_unwind_STAR_
     ([&form &env unwind_prev unwind_all bindings & body]
       (if (= (count bindings) 0)
         (seq (concat (clojure.core/list 'do) body))
         (if (symbol? (^clojure.lang.IFn bindings 0))
           (let [unwind_one (seq
                              (concat
                                (clojure.core/list 'try)
                                (-> (^clojure.lang.IFn bindings 2)
                                 (clojure.core/list)
                                 (concat (clojure.core/list (^clojure.lang.IFn bindings 0)))
                                 (seq)
                                 (clojure.core/list))
                                (clojure.core/list
                                  (seq
                                    (concat
                                      (clojure.core/list 'catch)
                                      (clojure.core/list 'java.lang.Throwable)
                                      (clojure.core/list 't__8540__auto__)
                                      (clojure.core/list
                                        (seq
                                          (concat
                                            (clojure.core/list 'datomic.error/report)
                                            (clojure.core/list 't__8540__auto__)))))))))]
             (seq
               (concat
                 (clojure.core/list 'clojure.core/let)
                 (clojure.core/list
                   (into
                     (subvec bindings 0 2)
                     [unwind_all
                      (seq
                        (concat
                          (clojure.core/list 'datomic.error/runonce)
                          (clojure.core/list
                            (seq
                              (concat
                                (clojure.core/list 'clojure.core/fn)
                                (clojure.core/list (apply vector (seq (concat))))
                                (clojure.core/list unwind_one)
                                (clojure.core/list
                                  (when unwind_prev
                                    (seq (concat (clojure.core/list unwind_prev))))))))))]))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'try)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'datomic.error/with-unwind*)
                             (clojure.core/list unwind_all)
                             (clojure.core/list unwind_all)
                             (clojure.core/list (subvec bindings 3))
                             body)))
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'catch)
                             (clojure.core/list 'java.lang.Throwable)
                             (clojure.core/list 't__8541__auto__)
                             (clojure.core/list unwind_one)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'throw)
                                   (clojure.core/list 't__8541__auto__)))))))))))))
           (do
             (when :else
               (throw
                 (java.lang.IllegalArgumentException.
                   "with-unwind only allows symbols in binding names")))
             nil))))))
  (reset-meta!
    #'with-unwind*
    (assoc
      {:arglists (clojure.core/list ['unwind-prev 'unwind-all 'bindings '& 'body]),
       :column (int 1)}
      :name
      'with-unwind*
      :ns
      *ns*))
  (.setMacro #'with-unwind*)
  (def with-unwind
   (fn with_unwind
     ([&form &env unwind_all bindings & body]
       (seq
         (concat
           (clojure.core/list 'datomic.error/with-unwind*)
           (clojure.core/list nil)
           (clojure.core/list unwind_all)
           (clojure.core/list bindings)
           body)))))
  (reset-meta!
    #'with-unwind
    (assoc
      {:arglists (clojure.core/list ['unwind-all 'bindings '& 'body]), :column (int 1)}
      :name
      'with-unwind
      :ns
      *ns*))
  (.setMacro #'with-unwind)
  (defn cancelled? ([anom] (:datomic/cancelled anom)))
  (reset-meta!
    #'cancelled?
    (assoc {:arglists (clojure.core/list ['anom]), :column (int 1)} :name 'cancelled? :ns *ns*))
  (defn should-log-exception? ([anom] (not (cancelled? anom))))
  (reset-meta!
    #'should-log-exception?
    (assoc
      {:arglists (clojure.core/list ['anom]), :column (int 1)}
      :name
      'should-log-exception?
      :ns
      *ns*)))
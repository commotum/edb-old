(do
  (clojure.core/in-ns 'datomic.error)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.error)
    {:doc
     "Constructs information-bearing Datomic exceptions and anomaly maps. Error data uses namespaced keys and preserves cause chains so callers can classify failures and choose retry or correction strategies."})
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
          (fn fn__8451 ([m cls msg] cls))
          :default
          #'clojure.core/global-hierarchy))
      #'anomalize))
  (defmethod
    anomalize
    datomic.impl.Exceptions$IllegalArgumentExceptionInfo
    fn__8456
    ([m cls msg]
      (merge #:cognitect.anomalies{:category :cognitect.anomalies/incorrect, :message msg} m)))
  (defmethod
    anomalize
    datomic.impl.Exceptions$IllegalStateExceptionInfo
    fn__8458
    ([m cls msg]
      (merge #:cognitect.anomalies{:category :cognitect.anomalies/conflict, :message msg} m)))
  (defmethod anomalize :default fn__8460 ([m _ _] m))
  (def create
   (fn create
     ([&form &env cls code msg details cause]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq (concat (clojure.core/list 'msg__8463__auto__) (clojure.core/list msg)))))
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
                       (clojure.core/list 'msg__8463__auto__))))
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
                       (clojure.core/list 'msg__8463__auto__))))
                 (clojure.core/list cause)))))))
     ([&form &env cls code msg details]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq (concat (clojure.core/list 'msg__8462__auto__) (clojure.core/list msg)))))
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
                       (clojure.core/list 'msg__8462__auto__))))
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
                       (clojure.core/list 'msg__8462__auto__))))))))))
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
  ;; Throw a general Datomic ExceptionInfo carrying :db/error and anomaly data.
  (defn raise
    ([code msg details cause]
      (throw
        (let [msg__8463__auto__ msg]
          (clojure.lang.ExceptionInfo.
            (str code " " msg__8463__auto__)
            (anomalize (assoc details :db/error code) clojure.lang.ExceptionInfo msg__8463__auto__)
            ^java.lang.Throwable cause))))
    ([code msg details]
      (throw
        (let [msg__8462__auto__ msg]
          (clojure.lang.ExceptionInfo.
            (str code " " msg__8462__auto__)
            (anomalize
              (assoc details :db/error code)
              clojure.lang.ExceptionInfo
              msg__8462__auto__)))))
    ([code msg] (raise code msg nil))
    ([code] (raise code "" nil)))
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
  ;; Throw a Datomic illegal-argument exception for invalid caller input.
  (defn arg
    ([code msg details cause]
      (throw
        (let [msg__8463__auto__ msg]
          (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
            (str code " " msg__8463__auto__)
            (anomalize
              (assoc details :db/error code)
              datomic.impl.Exceptions$IllegalArgumentExceptionInfo
              msg__8463__auto__)
            ^java.lang.Throwable cause))))
    ([code msg details]
      (throw
        (let [msg__8462__auto__ msg]
          (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
            (str code " " msg__8462__auto__)
            (anomalize
              (assoc details :db/error code)
              datomic.impl.Exceptions$IllegalArgumentExceptionInfo
              msg__8462__auto__)))))
    ([code msg] (arg code msg nil))
    ([code] (arg code "" nil)))
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
  (defn eval-exception
    ([p__8471 t]
      (let [map__8472 p__8471
            map__8472 (if (seq? map__8472)
                        (if (next map__8472)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8472))
                          (if (seq map__8472) (first map__8472) {}))
                        map__8472)
            cmap map__8472
            context (get map__8472 :context)
            expr (get map__8472 :expr)
            arguments (get map__8472 :arguments)
            msg (str "Error evaluating " (name context) ": " expr)]
        (ex-info
          msg
          {:cognitect.anomalies/category :cognitect.anomalies/fault,
           :cognitect.anomalies/message msg,
           :datomic/eval-exception cmap}
          t))))
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
  ;; Throw an illegal-argument exception with bounded printed details in its message.
  (defn argd
    ([code msg details cause]
      (throw
        (let [msg__8463__auto__ (add-details-to-msg msg details)]
          (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
            (str code " " msg__8463__auto__)
            (anomalize
              (assoc details :db/error code)
              datomic.impl.Exceptions$IllegalArgumentExceptionInfo
              msg__8463__auto__)
            ^java.lang.Throwable cause))))
    ([code msg details]
      (throw
        (let [msg__8462__auto__ (add-details-to-msg msg details)]
          (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
            (str code " " msg__8462__auto__)
            (anomalize
              (assoc details :db/error code)
              datomic.impl.Exceptions$IllegalArgumentExceptionInfo
              msg__8462__auto__)))))
    ([code msg] (arg code msg nil))
    ([code] (arg code "" nil)))
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
  ;; Throw a Datomic illegal-state exception for an invalid runtime transition.
  (defn state
    ([code msg details cause]
      (throw
        (let [msg__8463__auto__ msg]
          (datomic.impl.Exceptions$IllegalStateExceptionInfo.
            (str code " " msg__8463__auto__)
            (anomalize
              (assoc details :db/error code)
              datomic.impl.Exceptions$IllegalStateExceptionInfo
              msg__8463__auto__)
            ^java.lang.Throwable cause))))
    ([code msg details]
      (throw
        (let [msg__8462__auto__ msg]
          (datomic.impl.Exceptions$IllegalStateExceptionInfo.
            (str code " " msg__8462__auto__)
            (anomalize
              (assoc details :db/error code)
              datomic.impl.Exceptions$IllegalStateExceptionInfo
              msg__8462__auto__)))))
    ([code msg] (state code msg nil))
    ([code] (state code "" nil)))
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
    (atom (fn fn__8484 ([t] (.printStackTrace ^java.lang.Throwable t) nil))))
  (defn has-string-constructor?
    ([cls]
      (not
        (empty?
          (filter
            (fn fn__8488 ([p1__8487#] (= ['java.lang.String] (:parameter-types p1__8487#))))
            (filter
              (fn fn__8490 ([p1__8486#] (instance? clojure.reflect.Constructor p1__8486#)))
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
      (fn fn__8494
        ([classname]
          (if (and
                (.startsWith ^java.lang.String classname "java")
                (has-string-constructor? (java.lang.Class/forName ^java.lang.String classname)))
            (eval
              (seq
                (concat
                  (clojure.core/list 'clojure.core/fn)
                  (clojure.core/list
                    (apply vector (seq (concat (clojure.core/list 'msg__8493__auto__)))))
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'new)
                        (clojure.core/list (symbol classname))
                        (clojure.core/list 'msg__8493__auto__)))))))
            (fn fn__8495 ([msg] (java.lang.RuntimeException. ^java.lang.String msg))))))))
  ;; Reconstruct serialized exception data through the recognized information
  ;; classes, or use a cached String constructor for supported java.* classes.
  ;; ATOMIC-NOTE [observed/disposition]: peer/Connection.notify-error reconstructs
  ;; recognized information-bearing exceptions before promise delivery wraps
  ;; them in ExecutionException. Preserve category/data across the boundary;
  ;; arbitrary Java class identity is neither portable nor a native contract.
  ;; A transport error still cannot establish whether a transaction committed.
  (defn deserialize-exception
    ([p__8499]
      (let [map__8500 p__8499
            map__8500 (if (seq? map__8500)
                        (if (next map__8500)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8500))
                          (if (seq map__8500) (first map__8500) {}))
                        map__8500)
            classname (get map__8500 :classname)
            error (get map__8500 :error)
            error_data (get map__8500 :error-data)]
        (if error_data
          (let [G__8501 classname]
            (case
              G__8501
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
          ((exception-deserializer classname) error)))))
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
        (fn fn__8504
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
                                      (clojure.core/list 't__8509__auto__)
                                      (clojure.core/list
                                        (seq
                                          (concat
                                            (clojure.core/list 'datomic.error/report)
                                            (clojure.core/list 't__8509__auto__)))))))))]
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
                             (clojure.core/list 't__8510__auto__)
                             (clojure.core/list unwind_one)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'throw)
                                   (clojure.core/list 't__8510__auto__)))))))))))))
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

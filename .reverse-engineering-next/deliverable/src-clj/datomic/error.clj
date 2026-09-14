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
  (defmulti anomalize (fn fn__651 ([m cls msg] cls)))
  (defmethod
    anomalize
    datomic.impl.Exceptions$IllegalArgumentExceptionInfo
    fn__656
    ([m cls msg]
      (merge #:cognitect.anomalies{:category :cognitect.anomalies/incorrect, :message msg} m)))
  (defmethod
    anomalize
    datomic.impl.Exceptions$IllegalStateExceptionInfo
    fn__658
    ([m cls msg]
      (merge #:cognitect.anomalies{:category :cognitect.anomalies/conflict, :message msg} m)))
  (defmethod anomalize :default fn__660 ([m _ _] m))
  (defn create
    ([&form &env cls code msg details cause]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq (concat (clojure.core/list 'msg__663__auto__) (clojure.core/list msg)))))
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
                      (clojure.core/list 'msg__663__auto__))))
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
                      (clojure.core/list 'msg__663__auto__))))
                (clojure.core/list cause)))))))
    ([&form &env cls code msg details]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq (concat (clojure.core/list 'msg__662__auto__) (clojure.core/list msg)))))
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
                      (clojure.core/list 'msg__662__auto__))))
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
                      (clojure.core/list 'msg__662__auto__))))))))))
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
          (clojure.core/list nil)))))
  (.setMacro #'create)
  (defn raise
    ([code msg details cause]
      (do
        (throw
          (let [msg__663__auto__ msg]
            (clojure.lang.ExceptionInfo.
              (str code " " msg__663__auto__)
              (anomalize
                (assoc details :db/error code)
                clojure.lang.ExceptionInfo
                msg__663__auto__)
              ^java.lang.Throwable cause)))
        nil))
    ([code msg details]
      (do
        (throw
          (let [msg__662__auto__ msg]
            (clojure.lang.ExceptionInfo.
              (str code " " msg__662__auto__)
              (anomalize
                (assoc details :db/error code)
                clojure.lang.ExceptionInfo
                msg__662__auto__))))
        nil))
    ([code msg] (raise code msg nil))
    ([code] (raise code "" nil)))
  (defn arg
    ([code msg details cause]
      (do
        (throw
          (let [msg__663__auto__ msg]
            (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
              (str code " " msg__663__auto__)
              (anomalize
                (assoc details :db/error code)
                datomic.impl.Exceptions$IllegalArgumentExceptionInfo
                msg__663__auto__)
              ^java.lang.Throwable cause)))
        nil))
    ([code msg details]
      (do
        (throw
          (let [msg__662__auto__ msg]
            (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
              (str code " " msg__662__auto__)
              (anomalize
                (assoc details :db/error code)
                datomic.impl.Exceptions$IllegalArgumentExceptionInfo
                msg__662__auto__))))
        nil))
    ([code msg] (arg code msg nil))
    ([code] (arg code "" nil)))
  (defn eval-exception
    ([p__671 t]
      (let [map__672 p__671
            map__672 (if (seq? map__672)
                       (clojure.lang.PersistentHashMap/create (seq map__672))
                       map__672)
            cmap map__672
            context (get map__672 :context)
            expr (get map__672 :expr)
            arguments (get map__672 :arguments)
            msg (str "Error evaluating " (name context) ": " expr)]
        (ex-info
          msg
          {:cognitect.anomalies/category :cognitect.anomalies/fault,
           :cognitect.anomalies/message msg,
           :datomic/eval-exception cmap}
          t))))
  (defn add-details-to-msg
    ([msg details]
      (if details
        (do
          (push-thread-bindings (hash-map #'*print-length* 10 #'*print-level* 5))
          (str
            msg
            "\n"
            (try
              (let [s__6071__auto__ (java.io.StringWriter.)]
                (binding [*out* s__6071__auto__] (do (pp/pprint details) (str s__6071__auto__))))
              (finally (pop-thread-bindings)))))
        msg)))
  (defn argd
    ([code msg details cause]
      (do
        (throw
          (let [msg__663__auto__ (add-details-to-msg msg details)]
            (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
              (str code " " msg__663__auto__)
              (anomalize
                (assoc details :db/error code)
                datomic.impl.Exceptions$IllegalArgumentExceptionInfo
                msg__663__auto__)
              ^java.lang.Throwable cause)))
        nil))
    ([code msg details]
      (do
        (throw
          (let [msg__662__auto__ (add-details-to-msg msg details)]
            (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
              (str code " " msg__662__auto__)
              (anomalize
                (assoc details :db/error code)
                datomic.impl.Exceptions$IllegalArgumentExceptionInfo
                msg__662__auto__))))
        nil))
    ([code msg] (arg code msg nil))
    ([code] (arg code "" nil)))
  (defn state
    ([code msg details cause]
      (do
        (throw
          (let [msg__663__auto__ msg]
            (datomic.impl.Exceptions$IllegalStateExceptionInfo.
              (str code " " msg__663__auto__)
              (anomalize
                (assoc details :db/error code)
                datomic.impl.Exceptions$IllegalStateExceptionInfo
                msg__663__auto__)
              ^java.lang.Throwable cause)))
        nil))
    ([code msg details]
      (do
        (throw
          (let [msg__662__auto__ msg]
            (datomic.impl.Exceptions$IllegalStateExceptionInfo.
              (str code " " msg__662__auto__)
              (anomalize
                (assoc details :db/error code)
                datomic.impl.Exceptions$IllegalStateExceptionInfo
                msg__662__auto__))))
        nil))
    ([code msg] (state code msg nil))
    ([code] (state code "" nil)))
  (def reporter (atom (fn fn__684 ([t] (.printStackTrace ^java.lang.Throwable t) nil))))
  (defn has-string-constructor?
    ([cls]
      (not
        (empty?
          (filter
            (fn fn__688 ([p1__687#] (= ['java.lang.String] (:parameter-types p1__687#))))
            (filter
              (fn fn__690 ([p1__686#] (instance? clojure.reflect.Constructor p1__686#)))
              (:members (reflect/reflect cls))))))))
  (reset-meta!
    #'has-string-constructor?
    (assoc
      {:private true, :arglists (clojure.core/list ['cls]), :column 1}
      :name
      'has-string-constructor?
      :ns
      *ns*))
  (def exception-deserializer
   (memoize
     (fn fn__694
       ([classname]
         (if (and
               (.startsWith ^java.lang.String classname "java")
               (has-string-constructor? (java.lang.Class/forName ^java.lang.String classname)))
           (eval
             (seq
               (concat
                 (clojure.core/list 'clojure.core/fn)
                 (clojure.core/list
                   (apply vector (seq (concat (clojure.core/list 'msg__693__auto__)))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'new)
                       (clojure.core/list (symbol classname))
                       (clojure.core/list 'msg__693__auto__)))))))
           (fn fn__695 ([msg] (java.lang.RuntimeException. ^java.lang.String msg))))))))
  (defn deserialize-exception
    ([p__699]
      (let [map__700 p__699
            map__700 (if (seq? map__700)
                       (clojure.lang.PersistentHashMap/create (seq map__700))
                       map__700)
            classname (get map__700 :classname)
            error (get map__700 :error)
            error_data (get map__700 :error-data)]
        (if error_data
          (let [G__701 classname]
            (case
              G__701
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
  (defn report ([t] ((deref reporter) t)))
  (defn runonce
    ([f]
      (let [sentinel (java.lang.Object.) result (atom sentinel)]
        (fn fn__704
          ([& args]
            (locking sentinel
             (if (= (deref result) sentinel) (reset! result (apply f args)) (deref result))))))))
  (defn with-unwind*
    ([&form &env unwind_prev unwind_all bindings & body]
      (cond
        (= (count bindings) 0) (seq (concat (clojure.core/list 'do) body))
        (symbol? (^clojure.lang.IFn bindings 0)) (let [unwind_one (seq
                                                                    (concat
                                                                      (clojure.core/list 'try)
                                                                      (->
                                                                       (^clojure.lang.IFn bindings
                                                                         2)
                                                                       (clojure.core/list)
                                                                       (concat
                                                                         (clojure.core/list
                                                                           (^clojure.lang.IFn bindings
                                                                             0)))
                                                                       (seq)
                                                                       (clojure.core/list))
                                                                      (clojure.core/list
                                                                        (seq
                                                                          (concat
                                                                            (clojure.core/list
                                                                              'catch)
                                                                            (clojure.core/list
                                                                              'java.lang.Throwable)
                                                                            (clojure.core/list
                                                                              't__708__auto__)
                                                                            (clojure.core/list
                                                                              (seq
                                                                                (concat
                                                                                  (clojure.core/list
                                                                                    'datomic.error/report)
                                                                                  (clojure.core/list
                                                                                    't__708__auto__)))))))))]
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'clojure.core/let)
                                                       (clojure.core/list
                                                         (into
                                                           (subvec bindings 0 2)
                                                           [unwind_all
                                                            (seq
                                                              (concat
                                                                (clojure.core/list
                                                                  'datomic.error/runonce)
                                                                (clojure.core/list
                                                                  (seq
                                                                    (concat
                                                                      (clojure.core/list
                                                                        'clojure.core/fn)
                                                                      (clojure.core/list
                                                                        (apply
                                                                          vector
                                                                          (seq (concat))))
                                                                      (clojure.core/list
                                                                        unwind_one)
                                                                      (clojure.core/list
                                                                        (when
                                                                          unwind_prev
                                                                          (seq
                                                                            (concat
                                                                              (clojure.core/list
                                                                                unwind_prev))))))))))]))
                                                       (clojure.core/list
                                                         (seq
                                                           (concat
                                                             (clojure.core/list 'try)
                                                             (clojure.core/list
                                                               (seq
                                                                 (concat
                                                                   (clojure.core/list
                                                                     'datomic.error/with-unwind*)
                                                                   (clojure.core/list unwind_all)
                                                                   (clojure.core/list unwind_all)
                                                                   (clojure.core/list
                                                                     (subvec bindings 3))
                                                                   body)))
                                                             (clojure.core/list
                                                               (seq
                                                                 (concat
                                                                   (clojure.core/list 'catch)
                                                                   (clojure.core/list
                                                                     'java.lang.Throwable)
                                                                   (clojure.core/list
                                                                     't__709__auto__)
                                                                   (clojure.core/list unwind_one)
                                                                   (clojure.core/list
                                                                     (seq
                                                                       (concat
                                                                         (clojure.core/list 'throw)
                                                                         (clojure.core/list
                                                                           't__709__auto__)))))))))))))
        :else (do
                (throw
                  (java.lang.IllegalArgumentException.
                    "with-unwind only allows symbols in binding names"))
                nil))))
  (.setMacro #'with-unwind*)
  (defn with-unwind
    ([&form &env unwind_all bindings & body]
      (seq
        (concat
          (clojure.core/list 'datomic.error/with-unwind*)
          (clojure.core/list nil)
          (clojure.core/list unwind_all)
          (clojure.core/list bindings)
          body))))
  (.setMacro #'with-unwind)
  (defn cancelled? ([anom] (:datomic/cancelled anom)))
  (defn should-log-exception? ([anom] (not (cancelled? anom)))))
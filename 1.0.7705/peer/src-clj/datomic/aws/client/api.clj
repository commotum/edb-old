(do
  (clojure.core/in-ns 'datomic.aws.client.api)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.aws.client.api)
    {:doc
     "API for AWS SDK v2 client operations. Provides sync and async\n  invocation of AWS operations with responses as Clojure data.\n\n  AWS ops are registered to the provided client class via register-op(s),\n  and must be registered to be invoked.\n\n  Example:\n  (register-op {:client-class DynamoDbAsyncClient :op :GetItem})\n  (invoke client {:op :GetItem :req {:TableName \"mytable\" :Key {...}}})"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.aws.client.datafy :as 'datafy]
        ['datomic.aws.client.impl.async :as 'impl.async]
        ['datomic.aws.client.impl.sync :as 'impl.sync]
        ['datomic.aws.client.registry :as 'registry]
        ['clojure.core.async :as 'a])
      (clojure.core/import 'java.lang.reflect.Method)))
  (when-not (.equals 'datomic.aws.client.api 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.aws.client.api))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.aws.client.datafy :as 'datafy]
          ['datomic.aws.client.impl.async :as 'impl.async]
          ['datomic.aws.client.impl.sync :as 'impl.sync]
          ['datomic.aws.client.registry :as 'registry]
          ['clojure.core.async :as 'a])
        (clojure.core/import 'java.lang.reflect.Method))))
  (set! *warn-on-reflection* true)
  (def invoke
   (fn invoke
     ([client p__14265]
       (let [map__14266 p__14265
             map__14266 (if (seq? map__14266)
                          (if (next map__14266)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14266))
                            (if (seq map__14266) (first map__14266) {}))
                          map__14266)
             op_map map__14266
             op (get map__14266 :op)
             map__14267 (registry/lookup-op (class client) op)
             map__14267 (if (seq? map__14267)
                          (if (next map__14267)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14267))
                            (if (seq map__14267) (first map__14267) {}))
                          map__14267)
             op_spec map__14267
             ret_mode (get map__14267 :ret-mode)]
         (cond
           (:cognitect.anomalies/category op_spec) op_spec
           (= :async ret_mode) (let [ret_ch (a/promise-chan)]
                                 (impl.async/exec-op client (assoc op_map :ch ret_ch) op_spec)
                                 (a/<!! ret_ch))
           :else (do (impl.sync/exec-op client op_map op_spec)))))))
  (reset-meta!
    #'invoke
    (assoc
      {:arglists (clojure.core/list ['client {:keys ['op], :as 'op-map}]), :column (int 1)}
      :name
      'invoke
      :ns
      *ns*))
  (def invoke-async
   (fn invoke_async
     ([client p__14269]
       (let [map__14270 p__14269
             map__14270 (if (seq? map__14270)
                          (if (next map__14270)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14270))
                            (if (seq map__14270) (first map__14270) {}))
                          map__14270)
             op_map map__14270
             op (get map__14270 :op)
             ret_ch (or (:ch op_map) (a/promise-chan))
             map__14271 (registry/lookup-op (class client) op)
             map__14271 (if (seq? map__14271)
                          (if (next map__14271)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14271))
                            (if (seq map__14271) (first map__14271) {}))
                          map__14271)
             op_spec map__14271
             ret_mode (get map__14271 :ret-mode)]
         (cond
           (:cognitect.anomalies/category op_spec) (a/>!! ret_ch op_spec)
           (= :sync ret_mode) (a/thread-call
                                (fn fn__14272
                                  ([] (a/>!! ret_ch (impl.sync/exec-op client op_map op_spec))))
                                :io)
           :else (do (impl.async/exec-op client (assoc op_map :ch ret_ch) op_spec)))
         ret_ch))))
  (reset-meta!
    #'invoke-async
    (assoc
      {:arglists (clojure.core/list ['client {:keys ['op], :as 'op-map}]), :column (int 1)}
      :name
      'invoke-async
      :ns
      *ns*))
  (def register-op*
   (fn register_op_STAR_
     ([p__14279]
       (let [map__14280 p__14279
             map__14280 (if (seq? map__14280)
                          (if (next map__14280)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14280))
                            (if (seq map__14280) (first map__14280) {}))
                          map__14280)
             client_class (get map__14280 :client-class)
             op (get map__14280 :op)
             method (datafy/select-method (resolve client_class) op)
             param_types (.getParameterTypes ^java.lang.reflect.Method method)
             syms (cons
                    (with-meta (gensym "client") {:tag client_class})
                    (map
                      (fn fn__14281
                        ([p1__14276#]
                          (with-meta
                            (gensym)
                            {:tag (symbol (.getName ^java.lang.Class p1__14276#))})))
                      param_types))]
         (seq
           (concat
             (clojure.core/list 'clojure.core/defmethod)
             (clojure.core/list 'datomic.aws.client.registry/lookup-op)
             (clojure.core/list
               (apply
                 vector
                 (seq (concat (clojure.core/list client_class) (clojure.core/list op)))))
             (clojure.core/list
               (apply
                 vector
                 (seq
                   (concat
                     (clojure.core/list 'client-class__14277__auto__)
                     (clojure.core/list 'op__14278__auto__)))))
             (clojure.core/list
               (apply
                 hash-map
                 (seq
                   (concat
                     (clojure.core/list :request-builder)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'fn*)
                           (clojure.core/list (apply vector (seq (concat))))
                           (-> (symbol (.getName (first param_types)) "builder")
                            (clojure.core/list)
                            (concat)
                            (seq)
                            (clojure.core/list)))))
                     (clojure.core/list :ret-mode)
                     (clojure.core/list (datafy/ret-mode method))
                     (clojure.core/list :variant)
                     (clojure.core/list (datafy/variant method))
                     (clojure.core/list :method)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'clojure.core/fn)
                           (clojure.core/list (apply vector (seq (concat syms))))
                           (clojure.core/list
                             (-> (str "." (.getName ^java.lang.reflect.Method method))
                              (symbol)
                              (clojure.core/list)
                              (concat syms)
                              (seq))))))))))))))))
  (reset-meta!
    #'register-op*
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['client-class 'op]}]), :column (int 1)}
      :name
      'register-op*
      :ns
      *ns*))
  (def register-op (fn register_op ([&form &env spec] (register-op* spec))))
  (reset-meta!
    #'register-op
    (assoc {:arglists (clojure.core/list ['spec]), :column (int 1)} :name 'register-op :ns *ns*))
  (.setMacro #'register-op)
  (def register-ops
   (fn register_ops
     ([&form &env p__14285]
       (let [map__14286 p__14285
             map__14286 (if (seq? map__14286)
                          (if (next map__14286)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__14286))
                            (if (seq map__14286) (first map__14286) {}))
                          map__14286)
             client_class (get map__14286 :client-class)
             ops (get map__14286 :ops)]
         (seq
           (concat
             (clojure.core/list 'do)
             (map
               (fn fn__14287
                 ([op]
                   (seq
                     (concat
                       (clojure.core/list 'datomic.aws.client.api/register-op)
                       (clojure.core/list
                         (apply
                           hash-map
                           (seq
                             (concat
                               (clojure.core/list :client-class)
                               (clojure.core/list client_class)
                               (clojure.core/list :op)
                               (clojure.core/list op)))))))))
               ops)))))))
  (reset-meta!
    #'register-ops
    (assoc
      {:arglists (clojure.core/list [{:keys ['client-class 'ops]}]), :column (int 1)}
      :name
      'register-ops
      :ns
      *ns*))
  (.setMacro #'register-ops))
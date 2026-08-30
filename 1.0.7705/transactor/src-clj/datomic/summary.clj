(do
  (clojure.core/in-ns 'datomic.summary)
  (clojure.core/with-loading-context
    (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'print-method)))
  (when-not (.equals 'datomic.summary 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.summary))
    (clojure.core/with-loading-context
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'print-method))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Summary (summary [x]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.summary" "Summary")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Summary :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'summary {:arglists (clojure.core/list ['x])}),
                                      :arglists (clojure.core/list ['x]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.summary" "Summary"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.summary" "summary")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend nil Summary {:summary (fn fn__17813 ([_] nil))})
  (extend java.util.Map Summary {:summary (fn fn__17815 ([m] m))})
  (defn write ([w o] (.write ^java.io.Writer w ^java.lang.String o) nil))
  (reset-meta!
    #'write
    (assoc {:arglists (clojure.core/list ['w 'o]), :column (int 1)} :name 'write :ns *ns*))
  (def print-method
   (fn print_method
     ([&form &env clazz]
       (seq
         (concat
           (clojure.core/list 'do)
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'clojure.core/defmethod)
                 (clojure.core/list 'clojure.core/print-method)
                 (clojure.core/list clazz)
                 (clojure.core/list
                   (apply vector (seq (concat (clojure.core/list 'o) (clojure.core/list 'w)))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.summary/write)
                       (clojure.core/list 'w)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'clojure.core/str)
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'clojure.core/assoc)
                                   (clojure.core/list
                                     (seq
                                       (concat
                                         (clojure.core/list 'datomic.summary/summary)
                                         (clojure.core/list 'o))))
                                   (clojure.core/list :type)
                                   (clojure.core/list clazz)))))))))))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'clojure.core/defmethod)
                 (clojure.core/list 'clojure.core/print-dup)
                 (clojure.core/list clazz)
                 (clojure.core/list
                   (apply vector (seq (concat (clojure.core/list 'o) (clojure.core/list 'w)))))
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/print-method)
                       (clojure.core/list 'o)
                       (clojure.core/list 'w))))))))))))
  (reset-meta!
    #'print-method
    (assoc {:arglists (clojure.core/list ['clazz]), :column (int 1)} :name 'print-method :ns *ns*))
  (.setMacro #'print-method))
(do
  (clojure.core/in-ns 'datomic.summary)
  (clojure.core/with-loading-context
    (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'print-method)))
  (when-not (.equals 'datomic.summary 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.summary))
    (clojure.core/with-loading-context
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'print-method))))
  (set! *warn-on-reflection* true)
  (defonce Summary {})
  (defprotocol Summary (summary [x]))
  (extend nil Summary {:summary (fn fn__16663 ([_] nil))})
  (extend java.util.Map Summary {:summary (fn fn__16665 ([m] m))})
  (defn write ([w o] (.write ^java.io.Writer w ^java.lang.String o) nil))
  (defn print-method
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
                      (clojure.core/list 'w)))))))))))
  (.setMacro #'print-method))
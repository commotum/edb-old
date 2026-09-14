(do
  (clojure.core/in-ns 'datomic.core2.anomalies)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['cognitect.anomalies :as 'anom])))
  (when-not (.equals 'datomic.core2.anomalies 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.anomalies))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['cognitect.anomalies :as 'anom]))))
  (set! *warn-on-reflection* true)
  (def forbidden #:cognitect.anomalies{:category :cognitect.anomalies/forbidden})
  (def not-found #:cognitect.anomalies{:category :cognitect.anomalies/not-found})
  (defn anom
    ([x context] (when (:cognitect.anomalies/category x) (merge x context)))
    ([x] (when (:cognitect.anomalies/category x) x)))
  (defn ok? ([x] (not (anom x))))
  (defn forbidden? ([x] (= :cognitect.anomalies/forbidden (:cognitect.anomalies/category x))))
  (defn incorrect? ([x] (= :cognitect.anomalies/incorrect (:cognitect.anomalies/category x))))
  (defn conflict? ([x] (= :cognitect.anomalies/conflict (:cognitect.anomalies/category x))))
  (defn not-found? ([x] (= :cognitect.anomalies/not-found (:cognitect.anomalies/category x))))
  (defn fault? ([x] (= :cognitect.anomalies/fault (:cognitect.anomalies/category x))))
  (defn busy? ([x] (= :cognitect.anomalies/busy (:cognitect.anomalies/category x))))
  (defn unavailable? ([x] (= :cognitect.anomalies/unavailable (:cognitect.anomalies/category x))))
  (defn ok->
    ([&form &env expr & forms]
      (let [g (gensym)
            steps (map
                    (fn fn__19452
                      ([step]
                        (seq
                          (concat
                            (clojure.core/list 'clojure.core/or)
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list 'datomic.core2.anomalies/anom)
                                  (clojure.core/list g))))
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list 'clojure.core/->)
                                  (clojure.core/list g)
                                  (clojure.core/list step))))))))
                    forms)]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply
                vector
                (seq
                  (concat
                    (clojure.core/list g)
                    (clojure.core/list expr)
                    (interleave (repeat g) (butlast steps))))))
            (clojure.core/list (if (empty? steps) g (last steps))))))))
  (.setMacro #'ok->)
  (defn fault
    ([t]
      (merge
        {:cognitect.anomalies/category :cognitect.anomalies/fault,
         :datomic.core2.anomalies/exception (Throwable->map t)}
        (let [temp__5804__auto__ (.getMessage ^java.lang.Throwable t)]
          (when temp__5804__auto__
            (let [msg temp__5804__auto__] #:cognitect.anomalies{:message msg}))))))
  (defn athrow
    ([anom]
      (throw
        (ex-info
          (str (or (:cognitect.anomalies/message anom) (:cognitect.anomalies/category anom)))
          anom))))
  (defn returning
    ([&form &env & body]
      (seq
        (concat
          (clojure.core/list 'try)
          body
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'catch)
                (clojure.core/list 'java.lang.Throwable)
                (clojure.core/list 't__19459__auto__)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'datomic.core2.anomalies/fault)
                      (clojure.core/list 't__19459__auto__)))))))))))
  (.setMacro #'returning)
  (defn -slet*
    ([bindings body]
      (let [vec__19461 bindings
            seq__19462 (seq vec__19461)
            first__19463 (first seq__19462)
            seq__19462 (next seq__19462)
            s first__19463
            first__19463 (first seq__19462)
            seq__19462 (next seq__19462)
            v first__19463
            more seq__19462]
        (seq
          (concat
            (clojure.core/list 'clojure.core/let)
            (clojure.core/list
              (apply vector (seq (concat (clojure.core/list s) (clojure.core/list v)))))
            (clojure.core/list
              (seq
                (concat
                  (clojure.core/list 'if)
                  (clojure.core/list
                    (seq
                      (concat
                        (clojure.core/list 'datomic.core2.anomalies/anom)
                        (clojure.core/list s))))
                  (clojure.core/list s)
                  (clojure.core/list
                    (if (seq more)
                      (-slet* more body)
                      (seq (concat (clojure.core/list 'do) body))))))))))))
  (defn slet ([&form &env bindings & body] (-slet* (destructure bindings) body)))
  (.setMacro #'slet))
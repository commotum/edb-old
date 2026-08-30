(do
  (clojure.core/in-ns 'datomic.core2.anomalies)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['cognitect.anomalies :as 'anom]
        ['datomic.core2.anomalizer :as 'izer])))
  (when-not (.equals 'datomic.core2.anomalies 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.anomalies))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['cognitect.anomalies :as 'anom]
          ['datomic.core2.anomalizer :as 'izer]))))
  (set! *warn-on-reflection* true)
  (def forbidden #:cognitect.anomalies{:category :cognitect.anomalies/forbidden})
  (reset-meta! #'forbidden (assoc {:column (int 1)} :name 'forbidden :ns *ns*))
  (def not-found #:cognitect.anomalies{:category :cognitect.anomalies/not-found})
  (reset-meta! #'not-found (assoc {:column (int 1)} :name 'not-found :ns *ns*))
  (defn anom
    ([x context] (when (:cognitect.anomalies/category x) (merge x context)))
    ([x] (when (:cognitect.anomalies/category x) x)))
  (reset-meta!
    #'anom
    (assoc
      {:arglists (clojure.core/list ['x] ['x 'context]), :column (int 1)}
      :name
      'anom
      :ns
      *ns*))
  (defn ok? ([x] (not (anom x))))
  (reset-meta!
    #'ok?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'ok? :ns *ns*))
  (defn forbidden? ([x] (= :cognitect.anomalies/forbidden (:cognitect.anomalies/category x))))
  (reset-meta!
    #'forbidden?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'forbidden? :ns *ns*))
  (defn incorrect? ([x] (= :cognitect.anomalies/incorrect (:cognitect.anomalies/category x))))
  (reset-meta!
    #'incorrect?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'incorrect? :ns *ns*))
  (defn conflict? ([x] (= :cognitect.anomalies/conflict (:cognitect.anomalies/category x))))
  (reset-meta!
    #'conflict?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'conflict? :ns *ns*))
  (defn not-found? ([x] (= :cognitect.anomalies/not-found (:cognitect.anomalies/category x))))
  (reset-meta!
    #'not-found?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'not-found? :ns *ns*))
  (defn fault? ([x] (= :cognitect.anomalies/fault (:cognitect.anomalies/category x))))
  (reset-meta!
    #'fault?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'fault? :ns *ns*))
  (defn busy? ([x] (= :cognitect.anomalies/busy (:cognitect.anomalies/category x))))
  (reset-meta!
    #'busy?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'busy? :ns *ns*))
  (defn unavailable? ([x] (= :cognitect.anomalies/unavailable (:cognitect.anomalies/category x))))
  (reset-meta!
    #'unavailable?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'unavailable? :ns *ns*))
  (def ok->
   (fn ok__GT_
     ([&form &env expr & forms]
       (let [g (gensym)
             steps (map
                     (fn fn__20193
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
             (clojure.core/list (if (empty? steps) g (last steps)))))))))
  (reset-meta!
    #'ok->
    (assoc
      {:arglists (clojure.core/list ['expr '& 'forms]), :column (int 1)}
      :name
      'ok->
      :ns
      *ns*))
  (.setMacro #'ok->)
  (defn athrow
    ([anom]
      (throw
        (ex-info
          (str (or (:cognitect.anomalies/message anom) (:cognitect.anomalies/category anom)))
          anom))))
  (reset-meta!
    #'athrow
    (assoc {:arglists (clojure.core/list ['anom]), :column (int 1)} :name 'athrow :ns *ns*))
  (def returning
   (fn returning
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
                 (clojure.core/list 't__20198__auto__)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'datomic.core2.anomalizer/throwable->anom)
                       (clojure.core/list 't__20198__auto__))))))))))))
  (reset-meta!
    #'returning
    (assoc {:arglists (clojure.core/list ['& 'body]), :column (int 1)} :name 'returning :ns *ns*))
  (.setMacro #'returning)
  (defn -slet*
    ([bindings body]
      (let [vec__20200 bindings
            seq__20201 (seq vec__20200)
            first__20202 (first seq__20201)
            seq__20201 (next seq__20201)
            s first__20202
            first__20202 (first seq__20201)
            seq__20201 (next seq__20201)
            v first__20202
            more seq__20201]
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
  (reset-meta!
    #'-slet*
    (assoc
      {:arglists (clojure.core/list ['bindings 'body]), :column (int 1)}
      :name
      '-slet*
      :ns
      *ns*))
  (def slet (fn slet ([&form &env bindings & body] (-slet* (destructure bindings) body))))
  (reset-meta!
    #'slet
    (assoc
      {:arglists (clojure.core/list ['bindings '& 'body]), :column (int 1)}
      :name
      'slet
      :ns
      *ns*))
  (.setMacro #'slet))
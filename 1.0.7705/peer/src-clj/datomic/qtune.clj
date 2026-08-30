(do
  (clojure.core/in-ns 'datomic.qtune)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.api :as 'd]
        ['datomic.query :as 'query]
        ['datomic.datalog :as 'datalog]
        ['datomic.common :as 'common]
        ['clojure.edn :as 'edn]
        ['clojure.set :as 'set])))
  (when-not (.equals 'datomic.qtune 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.qtune))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.api :as 'd]
          ['datomic.query :as 'query]
          ['datomic.datalog :as 'datalog]
          ['datomic.common :as 'common]
          ['clojure.edn :as 'edn]
          ['clojure.set :as 'set]))))
  (def mapq->listq
   (fn mapq__GT_listq
     ([p__19471]
       (let [map__19472 p__19471
             map__19472 (if (seq? map__19472)
                          (if (next map__19472)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__19472))
                            (if (seq map__19472) (first map__19472) {}))
                          map__19472)
             find (get map__19472 :find)
             with (get map__19472 :with)
             in (get map__19472 :in)
             where (get map__19472 :where)
             timeout (get map__19472 :timeout)
             G__19473 []
             G__19473 (if find (into (conj G__19473 :find) find) G__19473)
             G__19473 (if with (into (conj G__19473 :with) with) G__19473)
             G__19473 (if in (into (conj G__19473 :in) in) G__19473)]
         (cond->
           (if where (into (conj G__19473 :where) where) G__19473)
           timeout
           (conj :timeout (first timeout)))))))
  (reset-meta!
    #'mapq->listq
    (assoc
      {:arglists (clojure.core/list [{:keys ['find 'with 'in 'where 'timeout]}]), :column (int 1)}
      :name
      'mapq->listq
      :ns
      *ns*))
  (defn cvars
    ([c] (filter datalog/variable? (if (instance? java.util.List (first c)) (first c) c))))
  (reset-meta!
    #'cvars
    (assoc {:arglists (clojure.core/list ['c]), :column (int 1)} :name 'cvars :ns *ns*))
  (defn cbinds
    ([c]
      (concat
        (cvars c)
        (when (instance? java.util.List (first c))
          (let [binds (second c)]
            (cond
              (symbol? binds) [binds]
              (instance? java.util.List binds) (do
                                                 (cond
                                                   (instance? java.util.List (first binds)) (first
                                                                                              binds)
                                                   (= '... (second binds)) [(first binds)]
                                                   :else (do binds)))))))))
  (reset-meta!
    #'cbinds
    (assoc {:arglists (clojure.core/list ['c]), :column (int 1)} :name 'cbinds :ns *ns*))
  (def partial-query
   (fn partial_query
     ([qmap preds clauses clause rclauses allow_cross]
       (let [bindings (into #{} (mapcat cbinds clauses))
             fxp? (fn fxp_QMARK_ ([p1__19477#] (instance? java.util.List (first p1__19477#))))]
         (when (if (^clojure.lang.IFn fxp? clause)
                 (every? bindings (cvars clause))
                 (or allow_cross (some bindings (cvars clause))))
           (let [bindings (into bindings (cbinds clause))
                 remvars (into (set (:find qmap)) (mapcat cvars rclauses))
                 next_find (vec (set/intersection bindings remvars))
                 next_preds (filter
                              (fn fn__19481 ([p1__19478#] (every? bindings (cvars p1__19478#))))
                              preds)]
             {:find next_find, :in (:in qmap), :where (concat clauses [clause] next_preds)}))))))
  (reset-meta!
    #'partial-query
    (assoc
      {:arglists (clojure.core/list ['qmap 'preds 'clauses 'clause 'rclauses 'allow-cross]),
       :column (int 1)}
      :name
      'partial-query
      :ns
      *ns*))
  (def partial-queries
   (fn partial_queries
     ([query preds clauses rclauses allow_cross]
       (reduce
         (fn fn__19485
           ([m c]
             (let [temp__5802__auto__ (partial-query
                                        query
                                        preds
                                        clauses
                                        c
                                        (disj rclauses c)
                                        allow_cross)]
               (if temp__5802__auto__ (let [q temp__5802__auto__] (assoc m c q)) m))))
         {}
         rclauses))))
  (reset-meta!
    #'partial-queries
    (assoc
      {:arglists (clojure.core/list ['query 'preds 'clauses 'rclauses 'allow-cross]),
       :column (int 1)}
      :name
      'partial-queries
      :ns
      *ns*))
  (defn min-ret
    ([qs args timeout]
      (if (= 1 (count qs))
        (let [vec__19490 (first qs) clause (nth vec__19490 (int 0) nil)] (prn clause) clause)
        (let [ret (apply
                    min-key
                    (fn fn__19493
                      ([p1__19489#] (or (second p1__19489#) (long java.lang.Long/MAX_VALUE))))
                    (map
                      (fn fn__19497
                        ([p__19496]
                          (let [vec__19498 p__19496
                                clause (nth vec__19498 (int 0) nil)
                                q (nth vec__19498 (int 1) nil)]
                            (try
                              (do
                                (print clause "... ")
                                (flush)
                                (let [ret [clause
                                           (java.lang.Integer/valueOf
                                             (int
                                               (count
                                                 (apply d/q (assoc q :timeout [timeout]) args))))]]
                                  (prn (second ret))
                                  ret))
                              (catch
                                java.lang.Exception
                                e
                                (if (instance?
                                      java.util.concurrent.TimeoutException
                                      (common/root-cause e))
                                  (do (prn :timeout) [clause nil])
                                  (do (throw ^java.lang.Throwable e) nil)))))))
                      qs))]
          (if (nil? (second ret))
            (let [timeout (* 2 timeout)]
              (println "Retrying with timeout: " timeout " milliseconds")
              (recur qs args timeout))
            (first ret))))))
  (reset-meta!
    #'min-ret
    (assoc
      {:arglists (clojure.core/list ['qs 'args 'timeout]), :column (int 1)}
      :name
      'min-ret
      :ns
      *ns*))
  (defn aug
    ([query preds clauses rclauses args]
      (loop [i 0 clauses clauses rclauses rclauses]
        (do
          (println "Slot: " (long i))
          (if (= 1 (count rclauses))
            (into clauses rclauses)
            (let [qs (partial-queries query preds clauses rclauses (zero? i))
                  clause (min-ret qs args 1)]
              (recur (inc i) (conj clauses clause) (disj rclauses clause))))))))
  (reset-meta!
    #'aug
    (assoc
      {:arglists (clojure.core/list ['query 'preds 'clauses 'rclauses 'args]), :column (int 1)}
      :name
      'aug
      :ns
      *ns*))
  (defn qtune
    ([query & args]
      (let [query (if (string? query) (edn/read-string query) query)
            query (if (instance? java.util.List query) (query/listq->mapq query) query)
            _ (when-not (instance? java.util.Map query)
                (throw
                  (java.lang.IllegalArgumentException.
                    "query must be a readable edn string, list, or map"))
                nil)
            query (if (map? query) query (into {} query))
            oq query
            query (query/process-aggregates query)
            query (query/process-in-bindings query "$__")
            pred? (fn pred_QMARK_
                    ([p1__19504#]
                      (=
                        1
                        (java.lang.Integer/valueOf (int (count p1__19504#)))
                        (instance? java.util.List (first p1__19504#)))))
            sv? (fn sv_QMARK_
                  ([p__19513]
                    (let [vec__19515 p__19513
                          call (nth vec__19515 (int 0) nil)
                          binds (nth vec__19515 (int 1) nil)]
                      (when (instance? java.util.List call)
                        (let [vec__19518 call
                              seq__19519 (seq vec__19518)
                              first__19520 (first seq__19519)
                              seq__19519 (next seq__19519)
                              f first__19520
                              body seq__19519]
                          (and
                            binds
                            (nil? (seq (filter datalog/variable? body)))
                            (or (symbol? binds) (= '... (second binds)))))))))
            vec__19505 (common/split-filter pred? (:where query))
            preds (nth vec__19505 (int 0) nil)
            clauses (nth vec__19505 (int 1) nil)
            vec__19508 (common/split-filter sv? clauses)
            svs (nth vec__19508 (int 0) nil)
            rclauses (nth vec__19508 (int 1) nil)
            svs (vec svs)
            rclauses (set rclauses)]
        (prn :warmup)
        (dotimes [_ 5]
          (let [start__6203__auto__ (java.lang.System/nanoTime)
                ret__6204__auto__ (try
                                    (apply d/q (assoc oq :timeout [5000]) args)
                                    (catch
                                      java.lang.Exception
                                      ex
                                      (if (instance?
                                            java.util.concurrent.TimeoutException
                                            (common/root-cause ex))
                                        (println :timeout)
                                        (do (throw ^java.lang.Throwable ex) nil))))]
            (prn
              (str
                "Elapsed time: "
                (java.lang.Double/valueOf
                  (double (/ (- (java.lang.System/nanoTime) start__6203__auto__) 1000000.0)))
                " msecs"))))
        (prn)
        (let [cs (aug query preds svs rclauses args)
              retq (mapq->listq (assoc oq :in (:in query) :where (concat cs preds)))]
          (prn :result)
          (dotimes [_ 10]
            (let [start__6203__auto__ (java.lang.System/nanoTime)
                  ret__6204__auto__ (apply d/q retq args)]
              (prn
                (str
                  "Elapsed time: "
                  (java.lang.Double/valueOf
                    (double (/ (- (java.lang.System/nanoTime) start__6203__auto__) 1000000.0)))
                  " msecs"))))
          retq))))
  (reset-meta!
    #'qtune
    (assoc
      {:arglists (clojure.core/list ['query '& 'args]), :column (int 1)}
      :name
      'qtune
      :ns
      *ns*)))
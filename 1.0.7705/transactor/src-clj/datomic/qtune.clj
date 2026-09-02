(do
  (clojure.core/in-ns 'datomic.qtune)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.qtune)
    {:doc
     "Interactive query clause-order tuning. Candidate clause orders are measured by running progressively larger partial queries, favoring clauses that produce the fewest intermediate rows. Tuning executes the supplied query repeatedly, prints measurements, and returns an equivalent list-form query with a selected clause order."})
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
  (defn mapq->listq
    ([{:keys [find with in where timeout]}]
      (let [query []
            query (if find (into (conj query :find) find) query)
            query (if with (into (conj query :with) with) query)
            query (if in (into (conj query :in) in) query)]
        (cond->
          (if where (into (conj query :where) where) query)
          timeout
          (conj :timeout (first timeout))))))
  (reset-meta!
    #'mapq->listq
    (assoc
      {:arglists (clojure.core/list [{:keys ['find 'with 'in 'where 'timeout]}]),
       :doc "Converts a map-form query to the equivalent list form, retaining find, with, in, where, and timeout clauses.",
       :column (int 1)}
      :name
      'mapq->listq
      :ns
      *ns*))
  (defn cvars
    ([clause]
      (filter
        datalog/variable?
        (if (instance? java.util.List (first clause)) (first clause) clause))))
  (reset-meta!
    #'cvars
    (assoc
      {:arglists (clojure.core/list ['clause]),
       :doc "Returns the logic variables referenced by a data, predicate, or function clause.",
       :column (int 1)}
      :name
      'cvars
      :ns
      *ns*))
  (defn cbinds
    ([clause]
      (concat
        (cvars clause)
        (when (instance? java.util.List (first clause))
          (let [binds (second clause)]
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
    (assoc
      {:arglists (clojure.core/list ['clause]),
       :doc
       "Returns variables made available after a clause runs, including scalar, tuple, collection, and relation bindings from function expressions.",
       :column (int 1)}
      :name
      'cbinds
      :ns
      *ns*))
  (defn partial-query
    ([qmap preds clauses clause rclauses allow-cross]
      (let [bindings (into #{} (mapcat cbinds clauses))
            fxp? (fn fxp_QMARK_ ([p1__31867#] (instance? java.util.List (first p1__31867#))))]
        (when (if (^clojure.lang.IFn fxp? clause)
                (every? bindings (cvars clause))
                (or allow-cross (some bindings (cvars clause))))
          (let [bindings (into bindings (cbinds clause))
                remvars (into (set (:find qmap)) (mapcat cvars rclauses))
                next_find (vec (set/intersection bindings remvars))
                next_preds (filter
                             (fn fn__31871 ([p1__31868#] (every? bindings (cvars p1__31868#))))
                             preds)]
            {:find next_find, :in (:in qmap), :where (concat clauses [clause] next_preds)})))))
  (reset-meta!
    #'partial-query
    (assoc
      {:arglists (clojure.core/list ['qmap 'preds 'clauses 'clause 'rclauses 'allow-cross]),
       :doc
       "Builds the smallest executable query that appends clause to the already selected clauses. Returns nil while the clause lacks required bindings; allow-cross permits the initial unbound data clause.",
       :column (int 1)}
      :name
      'partial-query
      :ns
      *ns*))
  (defn partial-queries
    ([query preds clauses rclauses allow-cross]
      (reduce
        (fn fn__31875
          ([m c]
            (let [temp__5823__auto__ (partial-query
                                       query
                                       preds
                                       clauses
                                       c
                                       (disj rclauses c)
                                       allow-cross)]
              (if temp__5823__auto__ (let [q temp__5823__auto__] (assoc m c q)) m))))
        {}
        rclauses)))
  (reset-meta!
    #'partial-queries
    (assoc
      {:arglists (clojure.core/list ['query 'preds 'clauses 'rclauses 'allow-cross]),
       :doc "Returns each currently executable remaining clause paired with the partial query used to measure it.",
       :column (int 1)}
      :name
      'partial-queries
      :ns
      *ns*))
  (defn min-ret
    ([queries args timeout]
      (if (= 1 (count queries))
        (let [vec__31880 (first queries) clause (nth vec__31880 (int 0) nil)]
          (prn clause)
          clause)
        (let [ret (apply
                    min-key
                    (fn fn__31883
                      ([p1__31879#] (or (second p1__31879#) (long java.lang.Long/MAX_VALUE))))
                    (map
                      (fn fn__31887
                        ([p__31886]
                          (let [vec__31888 p__31886
                                clause (nth vec__31888 (int 0) nil)
                                q (nth vec__31888 (int 1) nil)]
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
                      queries))]
          (if (nil? (second ret))
            (let [timeout (* 2 timeout)]
              (println "Retrying with timeout: " timeout " milliseconds")
              (recur queries args timeout))
            (first ret))))))
  (reset-meta!
    #'min-ret
    (assoc
      {:arglists (clojure.core/list ['queries 'args 'timeout]),
       :doc
       "Runs candidate partial queries and returns the clause with the smallest result. Timed-out candidates are ranked after completed candidates; when all candidates time out, the timeout is doubled and the measurements repeat.",
       :column (int 1)}
      :name
      'min-ret
      :ns
      *ns*))
  (defn aug
    ([query predicates clauses remaining-clauses args]
      (loop [i 0 clauses clauses remaining-clauses remaining-clauses]
        (do
          (println "Slot: " (long i))
          (if (= 1 (count remaining-clauses))
            (into clauses remaining-clauses)
            (let [qs (partial-queries query predicates clauses remaining-clauses (zero? i))
                  clause (min-ret qs args 1)]
              (recur (inc i) (conj clauses clause) (disj remaining-clauses clause))))))))
  (reset-meta!
    #'aug
    (assoc
      {:arglists (clojure.core/list ['query 'predicates 'clauses 'remaining-clauses 'args]),
       :doc
       "Greedily orders remaining-clauses by repeatedly measuring executable partial queries. The predicates collection is passed through when each candidate partial query is constructed.",
       :column (int 1)}
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
                    ([p1__31894#]
                      (=
                        1
                        (java.lang.Integer/valueOf (int (count p1__31894#)))
                        (instance? java.util.List (first p1__31894#)))))
            sv? (fn sv_QMARK_
                  ([p__31903]
                    (let [vec__31905 p__31903
                          call (nth vec__31905 (int 0) nil)
                          binds (nth vec__31905 (int 1) nil)]
                      (when (instance? java.util.List call)
                        (let [vec__31908 call
                              seq__31909 (seq vec__31908)
                              first__31910 (first seq__31909)
                              seq__31909 (next seq__31909)
                              f first__31910
                              body seq__31909]
                          (and
                            binds
                            (nil? (seq (filter datalog/variable? body)))
                            (or (symbol? binds) (= '... (second binds)))))))))
            vec__31895 (common/split-filter pred? (:where query))
            preds (nth vec__31895 (int 0) nil)
            clauses (nth vec__31895 (int 1) nil)
            vec__31898 (common/split-filter sv? clauses)
            svs (nth vec__31898 (int 0) nil)
            rclauses (nth vec__31898 (int 1) nil)
            svs (vec svs)
            rclauses (set rclauses)]
        (prn :warmup)
        (dotimes [_ 5]
          (let [start__6228__auto__ (java.lang.System/nanoTime)
                ret__6229__auto__ (try
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
                  (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                " msecs"))))
        (prn)
        (let [cs (aug query preds svs rclauses args)
              retq (mapq->listq (assoc oq :in (:in query) :where (concat cs preds)))]
          (prn :result)
          (dotimes [_ 10]
            (let [start__6228__auto__ (java.lang.System/nanoTime)
                  ret__6229__auto__ (apply d/q retq args)]
              (prn
                (str
                  "Elapsed time: "
                  (java.lang.Double/valueOf
                    (double (/ (- (java.lang.System/nanoTime) start__6228__auto__) 1000000.0)))
                  " msecs"))))
          retq))))
  (reset-meta!
    #'qtune
    (assoc
      {:arglists (clojure.core/list ['query '& 'args]),
       :doc
       "Tunes a query supplied as EDN text, list form, or map form. The input sources follow query in args. Runs five warmup executions with a five-second timeout, selects a clause order from partial-result cardinalities, runs the selected query ten times, prints timings, and returns the selected list-form query. Throws IllegalArgumentException when query has an unsupported representation.",
       :column (int 1)}
      :name
      'qtune
      :ns
      *ns*)))

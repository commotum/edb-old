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
  (defn mapq->listq
    ([p__15868]
      (let [map__15869 p__15868
            map__15869 (if (seq? map__15869)
                         (if (next map__15869)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15869))
                           (if (seq map__15869) (first map__15869) {}))
                         map__15869)
            find (get map__15869 :find)
            with (get map__15869 :with)
            in (get map__15869 :in)
            where (get map__15869 :where)
            timeout (get map__15869 :timeout)
            G__15870 []
            G__15870 (if find (into (conj G__15870 :find) find) G__15870)
            G__15870 (if with (into (conj G__15870 :with) with) G__15870)
            G__15870 (if in (into (conj G__15870 :in) in) G__15870)]
        (cond->
          (if where (into (conj G__15870 :where) where) G__15870)
          timeout
          (conj :timeout (first timeout))))))
  (defn cvars
    ([c] (filter datalog/variable? (if (instance? java.util.List (first c)) (first c) c))))
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
  (defn partial-query
    ([qmap preds clauses clause rclauses allow_cross]
      (let [bindings (into #{} (mapcat cbinds clauses))
            fxp? (fn fxp_QMARK_ ([p1__15874#] (instance? java.util.List (first p1__15874#))))]
        (when (if (^clojure.lang.IFn fxp? clause)
                (every? bindings (cvars clause))
                (or allow_cross (some bindings (cvars clause))))
          (let [bindings (into bindings (cbinds clause))
                remvars (into (set (:find qmap)) (mapcat cvars rclauses))
                next_find (vec (set/intersection bindings remvars))
                next_preds (filter
                             (fn fn__15878 ([p1__15875#] (every? bindings (cvars p1__15875#))))
                             preds)]
            {:find next_find, :in (:in qmap), :where (concat clauses [clause] next_preds)})))))
  (defn partial-queries
    ([query preds clauses rclauses allow_cross]
      (reduce
        (fn fn__15882
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
        rclauses)))
  (defn min-ret
    ([qs args timeout]
      (if (= 1 (count qs))
        (let [vec__15887 (first qs) clause (nth vec__15887 (int 0) nil)] (prn clause) clause)
        (let [ret (apply
                    min-key
                    (fn fn__15890
                      ([p1__15886#] (or (second p1__15886#) (long java.lang.Long/MAX_VALUE))))
                    (map
                      (fn fn__15894
                        ([p__15893]
                          (let [vec__15895 p__15893
                                clause (nth vec__15895 (int 0) nil)
                                q (nth vec__15895 (int 1) nil)]
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
                    ([p1__15901#]
                      (=
                        1
                        (java.lang.Integer/valueOf (int (count p1__15901#)))
                        (instance? java.util.List (first p1__15901#)))))
            sv? (fn sv_QMARK_
                  ([p__15910]
                    (let [vec__15912 p__15910
                          call (nth vec__15912 (int 0) nil)
                          binds (nth vec__15912 (int 1) nil)]
                      (when (instance? java.util.List call)
                        (let [vec__15915 call
                              seq__15916 (seq vec__15915)
                              first__15917 (first seq__15916)
                              seq__15916 (next seq__15916)
                              f first__15917
                              body seq__15916]
                          (and
                            binds
                            (nil? (seq (filter datalog/variable? body)))
                            (or (symbol? binds) (= '... (second binds)))))))))
            vec__15902 (common/split-filter pred? (:where query))
            preds (nth vec__15902 (int 0) nil)
            clauses (nth vec__15902 (int 1) nil)
            vec__15905 (common/split-filter sv? clauses)
            svs (nth vec__15905 (int 0) nil)
            rclauses (nth vec__15905 (int 1) nil)
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
          retq)))))
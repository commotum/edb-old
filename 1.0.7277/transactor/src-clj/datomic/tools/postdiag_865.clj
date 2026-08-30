(do
  (clojure.core/in-ns 'datomic.tools.postdiag-865)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['clojure.java.io :as 'io]
        ['clojure.pprint :as 'pp]
        ['datomic.api :as 'd]
        ['datomic.tools :as 'tools]
        ['datomic.tools.repair-865 :as 'repair])
      (clojure.core/import 'java.io.PushbackReader)))
  (when-not (.equals 'datomic.tools.postdiag-865 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.postdiag-865))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['clojure.java.io :as 'io]
          ['clojure.pprint :as 'pp]
          ['datomic.api :as 'd]
          ['datomic.tools :as 'tools]
          ['datomic.tools.repair-865 :as 'repair])
        (clojure.core/import 'java.io.PushbackReader))))
  (def unique-problems
   (fn unique_problems
     ([db es unique_fn]
       (mapv
         (fn fn__31134
           ([p__31133]
             (let [vec__31135 p__31133
                   _ (nth vec__31135 (int 0) nil)
                   a (nth vec__31135 (int 1) nil)
                   v (nth vec__31135 (int 2) nil)]
               (into [] (d/datoms db :avet a v)))))
         (sort-by
           first
           (filter
             (fn fn__31140
               ([p__31139]
                 (let [vec__31141 p__31139
                       ce (nth vec__31141 (int 0) nil)
                       a (nth vec__31141 (int 1) nil)
                       v (nth vec__31141 (int 2) nil)]
                   (> ce 1))))
             (d/q
               [:find
                (clojure.core/list 'count '?e2)
                '?a
                '?v
                :in
                '$
                ['?e1 '...]
                ['?a '...]
                :where
                ['?e1 '?a '?v]
                ['?e2 '?a '?v]]
               db
               es
               (^clojure.lang.IFn unique_fn db))))))))
  (reset-meta!
    #'unique-problems
    (assoc
      {:arglists (clojure.core/list ['db 'es 'unique-fn]), :column (int 1)}
      :name
      'unique-problems
      :ns
      *ns*))
  (defn card-problems
    ([db es]
      (mapv
        (fn fn__31147
          ([p__31146]
            (let [vec__31148 p__31146
                  e (nth vec__31148 (int 0) nil)
                  a (nth vec__31148 (int 1) nil)]
              (into [] (d/datoms db :eavt e a)))))
        (sort-by
          first
          (filter
            (fn fn__31153
              ([p__31152]
                (let [vec__31154 p__31152
                      e (nth vec__31154 (int 0) nil)
                      a (nth vec__31154 (int 1) nil)
                      cv (nth vec__31154 (int 2) nil)]
                  (> cv 1))))
            (d/q
              [:find
               '?e
               '?a
               (clojure.core/list 'count '?v)
               :in
               '$
               ['?e '...]
               ['?a '...]
               :where
               ['?e '?a '?v]]
              db
              es
              (tools/card-ones db)))))))
  (reset-meta!
    #'card-problems
    (assoc
      {:arglists (clojure.core/list ['db 'es]), :column (int 1)}
      :name
      'card-problems
      :ns
      *ns*))
  (defn fulltext-problems
    ([db es ts]
      (sort-by
        first
        (d/q
          [:find
           '?e
           '?a
           '?v
           '?tx
           :in
           '$
           ['?e '...]
           ['?a '...]
           ['?tx '...]
           :where
           ['?e '?a '?v '?tx]]
          db
          es
          (tools/fulltexts db)
          (mapv d/t->tx ts)))))
  (reset-meta!
    #'fulltext-problems
    (assoc
      {:arglists (clojure.core/list ['db 'es 'ts]), :column (int 1)}
      :name
      'fulltext-problems
      :ns
      *ns*))
  (defn ts->es
    ([cr ts]
      (reduce
        (fn fn__31160
          ([es t] (into es (map :e (:data (first (tools/tx-range-from-log cr t (inc t))))))))
        #{}
        ts)))
  (reset-meta!
    #'ts->es
    (assoc {:arglists (clojure.core/list ['cr 'ts]), :column (int 1)} :name 'ts->es :ns *ns*))
  (defn partially-indexed-transactions
    ([db]
      (d/q
        [:find
         ['?t '...]
         :where
         ['?e :db.sys/partiallyIndexed]
         [(clojure.core/list 'datomic.api/tx->t '?e) '?t]]
        db)))
  (reset-meta!
    #'partially-indexed-transactions
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'partially-indexed-transactions
      :ns
      *ns*))
  (defn -main
    ([uri checkfile]
      (try
        (let [cr (tools/connection-resources uri)
              map__31165 (tools/db-resources cr)
              map__31165 (if (seq? map__31165)
                           (if (next map__31165)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__31165))
                             (if (seq map__31165) (first map__31165) {}))
                           map__31165)
              db (get map__31165 :db)
              map__31166 (repair/read-desc-file checkfile)
              map__31166 (if (seq? map__31166)
                           (if (next map__31166)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__31166))
                             (if (seq map__31166) (first map__31166) {}))
                           map__31166)
              ts (get map__31166 :ts)
              es (ts->es cr ts)]
          (pp/pprint
            {:cardinality-problems (card-problems db es),
             :unique-identity-problems (unique-problems db es tools/unique-identities),
             :unique-value-problems (unique-problems db es tools/unique-values),
             :partially-indexed-ts (partially-indexed-transactions db)}))
        (catch
          java.lang.Throwable
          t
          (do
            (.printStackTrace ^java.lang.Throwable t)
            (d/shutdown true)
            (java.lang.System/exit (int -1))
            nil)))
      (d/shutdown true)
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc
      {:arglists (clojure.core/list ['uri 'checkfile]), :column (int 1)}
      :name
      '-main
      :ns
      *ns*)))
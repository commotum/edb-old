(do
  (clojure.core/in-ns 'datomic.tools.detect-865)
  (clojure.core/with-loading-context
    (do
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['clojure.set :as 'set]
        ['datomic.api :as 'd]
        ['datomic.db :as 'db]
        ['datomic.common :as 'common :refer ['compare]]
        ['datomic.tools :as 'tools])
      (clojure.core/refer 'clojure.core :exclude ['compare])
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'java.util.Comparator)))
  (when-not (.equals 'datomic.tools.detect-865 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.detect-865))
    (clojure.core/with-loading-context
      (do
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['clojure.set :as 'set]
          ['datomic.api :as 'd]
          ['datomic.db :as 'db]
          ['datomic.common :as 'common :refer ['compare]]
          ['datomic.tools :as 'tools])
        (clojure.core/refer 'clojure.core :exclude ['compare])
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'java.util.Comparator))))
  (set! *warn-on-reflection* true)
  (.setMeta
    (clojure.lang.RT/var "datomic.tools.detect-865" "avto-cmp")
    {:tag java.util.Comparator, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.tools.detect-865" "avto-cmp")
    (reify
      java.util.Comparator
      (^int compare
        [this x y]
        (.intValue
          (let [x x y y]
            (cond
              (< (.a ^datomic.Datom x) (.a ^datomic.Datom y)) -1
              (> (.a ^datomic.Datom x) (.a ^datomic.Datom y)) 1
              :else (do
                      (let [c (common/compare (.v ^datomic.Datom x) (.v ^datomic.Datom y))]
                        (cond
                          (not (zero? c)) (long c)
                          (< (.tx ^datomic.Datom x) (.tx ^datomic.Datom y)) -1
                          (> (.tx ^datomic.Datom x) (.tx ^datomic.Datom y)) 1
                          :else (do
                                  (cond
                                    (= (.added ^datomic.Datom x) (.added ^datomic.Datom y)) 0
                                    (.added ^datomic.Datom x) (do 1))))))))))))
  (defn lesser-avto
    ([d1 d2] (cond (nil? d2) d1 (nil? d1) d2 (< (.compare avto-cmp d1 d2) 0) d1 :else (do d2))))
  (reset-meta!
    #'lesser-avto
    (assoc {:arglists (clojure.core/list ['d1 'd2]), :column (int 1)} :name 'lesser-avto :ns *ns*))
  (def next-2-datoms
   (fn next_2_datoms
     ([p__20004 p__20005]
       (let [vec__20006 p__20004
             seq__20007 (seq vec__20006)
             first__20008 (first seq__20007)
             seq__20007 (next seq__20007)
             g1 first__20008
             first__20008 (first seq__20007)
             seq__20007 (next seq__20007)
             g2 first__20008
             gmore seq__20007
             gall vec__20006
             vec__20009 p__20005
             seq__20010 (seq vec__20009)
             first__20011 (first seq__20010)
             seq__20010 (next seq__20010)
             i1 first__20011
             first__20011 (first seq__20010)
             seq__20010 (next seq__20010)
             i2 first__20011
             imore seq__20010
             iall vec__20009]
         (if (= g1 (lesser-avto g1 i1))
           (if (= g2 (lesser-avto g2 i1))
             [g1 g2 gmore iall]
             [g1 i1 (cons g2 gmore) (cons i1 imore)])
           (if (= i2 (lesser-avto g1 i2))
             [i1 i2 gall imore]
             [i1 g1 (cons g2 gmore) (cons i1 imore)]))))))
  (reset-meta!
    #'next-2-datoms
    (assoc
      {:arglists (clojure.core/list [['g1 'g2 '& 'gmore :as 'gall] ['i1 'i2 '& 'imore :as 'iall]]),
       :column (int 1)}
      :name
      'next-2-datoms
      :ns
      *ns*))
  (def ar-pair?
   (fn ar_pair_QMARK_
     ([ad rd]
       (and
         (= (.e ^datomic.Datom ad) (.e ^datomic.Datom rd))
         (= (.a ^datomic.Datom ad) (.a ^datomic.Datom rd))
         (zero? (common/compare (.v ^datomic.Datom ad) (.v ^datomic.Datom rd)))
         (.added ^datomic.Datom ad)
         (not (.added ^datomic.Datom rd))
         (< (.tx ^datomic.Datom ad) (.tx ^datomic.Datom rd))))))
  (reset-meta!
    #'ar-pair?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'ad {:tag 'Datom}) (.withMeta 'rd {:tag 'Datom})]),
       :column (int 1)}
      :name
      'ar-pair?
      :ns
      *ns*))
  (defn precise-identity-substitutions*
    ([db gdatoms]
      (let [gap_datoms (sort avto-cmp gdatoms)
            index_datoms (let [d (first gap_datoms)]
                           (sort
                             avto-cmp
                             (seq
                               (d/datoms
                                 (d/history db)
                                 :avet
                                 (.a ^datomic.Datom d)
                                 (.v ^datomic.Datom d)))))
            sub {}
            G__20025 gap_datoms
            vec__20027 G__20025
            g1 (nth vec__20027 (int 0) nil)
            gdatoms vec__20027
            G__20026 index_datoms
            vec__20030 G__20026
            i1 (nth vec__20030 (int 0) nil)
            idatoms vec__20030]
        (loop [sub sub G__20025 G__20025 G__20026 G__20026]
          (let [sub sub
                vec__20033 G__20025
                g1 (nth vec__20033 (int 0) nil)
                gdatoms vec__20033
                vec__20036 G__20026
                i1 (nth vec__20036 (int 0) nil)
                idatoms vec__20036]
            (if (nil? g1)
              [sub {}]
              (let [vec__20039 (next-2-datoms gdatoms idatoms)
                    d1 (nth vec__20039 (int 0) nil)
                    d2 (nth vec__20039 (int 1) nil)
                    gmore (nth vec__20039 (int 2) nil)
                    dmore (nth vec__20039 (int 3) nil)]
                (cond
                  (nil? d2) [sub {}]
                  (ar-pair? d1 d2) (recur sub gmore dmore)
                  (and
                    (= d1 (first gdatoms))
                    (.added ^datomic.Datom d1)
                    (.added ^datomic.Datom d2)) (recur
                                                  (assoc
                                                    sub
                                                    (.e ^datomic.Datom d1)
                                                    (.e ^datomic.Datom d2))
                                                  gmore
                                                  dmore)
                  :default (do
                             (let [cause {:log gap_datoms, :index index_datoms}]
                               [{}
                                (reduce
                                  (fn fn__20042 ([m gd] (assoc m (.e ^datomic.Datom gd) cause)))
                                  {}
                                  gdatoms)]))))))))))
  (reset-meta!
    #'precise-identity-substitutions*
    (assoc
      {:arglists (clojure.core/list ['db 'gdatoms]), :column (int 1)}
      :name
      'precise-identity-substitutions*
      :ns
      *ns*))
  (defn precise-identity-substitutions
    ([idmap db aids]
      (reduce
        (fn fn__20049
          ([p__20047 p__20048]
            (let [vec__20050 p__20047
                  sub (nth vec__20050 (int 0) nil)
                  cant (nth vec__20050 (int 1) nil)
                  vec__20053 p__20048
                  newsub (nth vec__20053 (int 0) nil)
                  newcant (nth vec__20053 (int 1) nil)
                  collisions (set/intersection (set (keys sub)) (set (keys newsub)))]
              [(apply dissoc (apply dissoc (merge sub newsub) collisions) (keys newcant))
               (merge (zipmap collisions (repeat :collision)) newcant cant)])))
        [{} {}]
        (map
          (fn fn__20058
            ([p__20057]
              (let [vec__20059 p__20057
                    d (nth vec__20059 (int 0) nil)
                    gdatoms vec__20059
                    vec__20062 (precise-identity-substitutions* db gdatoms)
                    submap (nth vec__20062 (int 0) nil)
                    cantsubmap (nth vec__20062 (int 1) nil)]
                (if (contains? aids (.a ^datomic.Datom d))
                  [submap cantsubmap]
                  [{}
                   (zipmap
                     (map :e (concat (keys submap) (keys cantsubmap)))
                     (repeat {:skipped (d/ident db (.a ^datomic.Datom d))}))]))))
          (vals idmap)))))
  (reset-meta!
    #'precise-identity-substitutions
    (assoc
      {:arglists (clojure.core/list ['idmap 'db 'aids]), :column (int 1)}
      :name
      'precise-identity-substitutions
      :ns
      *ns*))
  (def crosscheck-tx-instants*
   (fn crosscheck_tx_instants_STAR_
     ([cr db first_t]
       (let [identities (into #{} (tools/unique-identities db)) instid (d/entid db :db/txInstant)]
         (map
           (fn fn__20067
             ([logentry]
               (let [data (:data logentry)
                     inst (first
                            (filter
                              (fn fn__20070
                                ([p__20069]
                                  (let [map__20071 p__20069
                                        map__20071 (if (seq? map__20071)
                                                     (if (next map__20071)
                                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                         (to-array map__20071))
                                                       (if (seq map__20071) (first map__20071) {}))
                                                     map__20071)
                                        a (get map__20071 :a)]
                                    (= a instid))))
                              data))
                     map__20068 inst
                     map__20068 (if (seq? map__20068)
                                  (if (next map__20068)
                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                      (to-array map__20068))
                                    (if (seq map__20068) (first map__20068) {}))
                                  map__20068)
                     e (get map__20068 :e)
                     a (get map__20068 :a)
                     v (get map__20068 :v)
                     tx (get map__20068 :tx)
                     indexed? (boolean (seq (d/datoms db :avet a v e tx)))
                     identities (into
                                  #{}
                                  (when-not indexed?
                                    (reduce
                                      (fn fn__20074
                                        ([ret p__20073]
                                          (let [map__20075 p__20073
                                                map__20075 (if
                                                             (seq? map__20075)
                                                             (if
                                                               (next map__20075)
                                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                 (to-array map__20075))
                                                               (if
                                                                 (seq map__20075)
                                                                 (first map__20075)
                                                                 {}))
                                                             map__20075)
                                                d map__20075
                                                a (get map__20075 :a)]
                                            (if (^clojure.lang.IFn identities a)
                                              (conj ret d)
                                              ret))))
                                      []
                                      data)))]
                 (merge
                   {:t (:t logentry), :log-inst v, :indexed? indexed?}
                   (when (seq identities) {:identities identities})
                   (when-not indexed? {:datoms (java.lang.Integer/valueOf (int (count data)))})))))
           (tools/tx-range-from-log cr first_t (inc (:indexBasisT db))))))))
  (reset-meta!
    #'crosscheck-tx-instants*
    (assoc
      {:arglists (clojure.core/list ['cr 'db 'first-t]), :column (int 1)}
      :name
      'crosscheck-tx-instants*
      :ns
      *ns*))
  (defn unique-conflicts
    ([db datoms]
      (let [aids (into #{} (tools/unique-identities db))
            vec__20079 (precise-identity-substitutions (tools/avof-map datoms) db aids)
            sub (nth vec__20079 (int 0) nil)
            cantsub (nth vec__20079 (int 1) nil)]
        (filter
          (fn fn__20083
            ([p__20082]
              (let [map__20084 p__20082
                    map__20084 (if (seq? map__20084)
                                 (if (next map__20084)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__20084))
                                   (if (seq map__20084) (first map__20084) {}))
                                 map__20084)
                    e (get map__20084 :e)
                    a (get map__20084 :a)]
                (or (contains? sub e) (contains? cantsub e)))))
          datoms))))
  (reset-meta!
    #'unique-conflicts
    (assoc
      {:arglists (clojure.core/list ['db 'datoms]), :column (int 1)}
      :name
      'unique-conflicts
      :ns
      *ns*))
  (def crosscheck-tx-instants
   (fn crosscheck_tx_instants
     ([uri t & p__20090]
       (let [vec__20091 p__20090 unique_datom_file (nth vec__20091 (int 0) nil) done (promise)]
         (try
           (let [cr (tools/connection-resources uri)
                 map__20094 (tools/db-resources cr)
                 map__20094 (if (seq? map__20094)
                              (if (next map__20094)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__20094))
                                (if (seq map__20094) (first map__20094) {}))
                              map__20094)
                 db (get map__20094 :db)
                 log (get map__20094 :log)
                 goal_t (d/basis-t db)
                 _ (tools/progress
                     prn
                     {:phase :detect/start, :goal-t goal_t, :uri uri, :version 1})
                 progress_state (atom t)
                 pace (some->
                        uri
                        (tools/read-capacity-units)
                        (tools/pace-msec-per-seg)
                        (tools/pace-fn))
                 _ (future-call
                     (fn fn__20096
                       ([]
                         (try
                           (loop []
                             (do
                               (java.lang.Thread/sleep 15000)
                               (when-not (realized? done)
                                 (tools/progress
                                   tools/prn-err
                                   {:scanned-up-to-t (deref progress_state), :basis-t goal_t})
                                 (recur))))
                           (catch
                             java.lang.Throwable
                             t__8829__auto__
                             (do
                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                              "datomic.tools.detect-865")
                                     ex t__8829__auto__]
                                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                   (.warn
                                     ^org.slf4j.Logger logger
                                     (datomic.slf4j/process "error executing future")
                                     ^java.lang.Throwable ex)
                                   (datomic.slf4j/caused-by logger ex))
                                 nil)
                               (datomic.monitor/alarm :UnhandledException)
                               (throw ^java.lang.Throwable t__8829__auto__)
                               nil))))))
                 results (remove
                           :indexed?
                           (map
                             (fn fn__20098
                               ([x]
                                 (when pace (^clojure.lang.IFn pace))
                                 (reset! progress_state (:t x))
                                 x))
                             (crosscheck-tx-instants* cr db t)))
                 initial_t (:t (first results))
                 identities (unique-conflicts db (mapcat :identities results))
                 results (mapv
                           (fn fn__20100 ([p1__20088#] (dissoc p1__20088# :identities :indexed?)))
                           results)
                 unique_attrs (into
                                #{}
                                (mapv
                                  (fn fn__20102
                                    ([p1__20089#]
                                      (db/resolve-kw db (.a ^datomic.Datom p1__20089#))))
                                  identities))]
             (push-thread-bindings (hash-map #'*print-length* nil))
             (try
               (let [ts (reduce
                          (fn fn__20105
                            ([ts result]
                              (tools/progress prn (merge {:phase :detect/progress} result))
                              (conj ts (:t result))))
                          []
                          results)]
                 (tools/progress
                   tools/prn-err
                   (merge
                     {:problems-detected (java.lang.Integer/valueOf (int (count ts)))}
                     (when initial_t {:initial-t initial_t})))
                 (await tools/serializer)
                 (tools/progress
                   prn
                   (merge
                     {:phase :detect/end,
                      :problems-detected (java.lang.Integer/valueOf (int (count ts))),
                      :initial-t initial_t,
                      :end-t (:t (last results)),
                      :version 1}
                     (when (seq unique_attrs) {:unique-attributes unique_attrs}))))
               (finally (pop-thread-bindings)))
             (when unique_datom_file
               (with-open [writer (io/writer unique_datom_file)]
                 (loop [seq_20108 (seq identities) chunk_20109 nil count_20110 0 i_20111 0]
                   (if (< i_20111 count_20110)
                     (let [d (.nth ^clojure.lang.Indexed chunk_20109 (int i_20111))]
                       (.write ^java.io.Writer writer (prn-str (tools/pretty-datom db d)))
                       (recur seq_20108 chunk_20109 count_20110 (inc i_20111)))
                     (let [temp__5804__auto__ (seq seq_20108)]
                       (when temp__5804__auto__
                         (let [seq_20108 temp__5804__auto__]
                           (if (chunked-seq? seq_20108)
                             (let [c__6065__auto__ (chunk-first seq_20108)]
                               (recur
                                 (chunk-rest seq_20108)
                                 c__6065__auto__
                                 (int (count c__6065__auto__))
                                 (int 0)))
                             (let [d (first seq_20108)]
                               (.write ^java.io.Writer writer (prn-str (tools/pretty-datom db d)))
                               (recur (next seq_20108) nil 0 0)))))))))))
           (finally (deliver done true)))))))
  (reset-meta!
    #'crosscheck-tx-instants
    (assoc
      {:arglists (clojure.core/list ['uri 't '& ['unique-datom-file]]), :column (int 1)}
      :name
      'crosscheck-tx-instants
      :ns
      *ns*))
  (defn -main
    ([uri & args]
      (try
        (apply crosscheck-tx-instants uri 0 args)
        (catch
          java.lang.Throwable
          t
          (do
            (tools/flush-progress)
            (d/shutdown true)
            (.printStackTrace ^java.lang.Throwable t)
            (java.lang.System/exit (int -1))
            nil)))
      (tools/flush-progress)
      (d/shutdown true)
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri '& 'args]), :column (int 1)} :name '-main :ns *ns*)))
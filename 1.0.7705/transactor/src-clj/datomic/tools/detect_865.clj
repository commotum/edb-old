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
     ([p__31411 p__31412]
       (let [vec__31413 p__31411
             seq__31414 (seq vec__31413)
             first__31415 (first seq__31414)
             seq__31414 (next seq__31414)
             g1 first__31415
             first__31415 (first seq__31414)
             seq__31414 (next seq__31414)
             g2 first__31415
             gmore seq__31414
             gall vec__31413
             vec__31416 p__31412
             seq__31417 (seq vec__31416)
             first__31418 (first seq__31417)
             seq__31417 (next seq__31417)
             i1 first__31418
             first__31418 (first seq__31417)
             seq__31417 (next seq__31417)
             i2 first__31418
             imore seq__31417
             iall vec__31416]
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
            G__31432 gap_datoms
            vec__31434 G__31432
            g1 (nth vec__31434 (int 0) nil)
            gdatoms vec__31434
            G__31433 index_datoms
            vec__31437 G__31433
            i1 (nth vec__31437 (int 0) nil)
            idatoms vec__31437]
        (loop [sub sub G__31432 G__31432 G__31433 G__31433]
          (let [sub sub
                vec__31440 G__31432
                g1 (nth vec__31440 (int 0) nil)
                gdatoms vec__31440
                vec__31443 G__31433
                i1 (nth vec__31443 (int 0) nil)
                idatoms vec__31443]
            (if (nil? g1)
              [sub {}]
              (let [vec__31446 (next-2-datoms gdatoms idatoms)
                    d1 (nth vec__31446 (int 0) nil)
                    d2 (nth vec__31446 (int 1) nil)
                    gmore (nth vec__31446 (int 2) nil)
                    dmore (nth vec__31446 (int 3) nil)]
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
                                  (fn fn__31449 ([m gd] (assoc m (.e ^datomic.Datom gd) cause)))
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
        (fn fn__31456
          ([p__31454 p__31455]
            (let [vec__31457 p__31454
                  sub (nth vec__31457 (int 0) nil)
                  cant (nth vec__31457 (int 1) nil)
                  vec__31460 p__31455
                  newsub (nth vec__31460 (int 0) nil)
                  newcant (nth vec__31460 (int 1) nil)
                  collisions (set/intersection (set (keys sub)) (set (keys newsub)))]
              [(apply dissoc (apply dissoc (merge sub newsub) collisions) (keys newcant))
               (merge (zipmap collisions (repeat :collision)) newcant cant)])))
        [{} {}]
        (map
          (fn fn__31465
            ([p__31464]
              (let [vec__31466 p__31464
                    d (nth vec__31466 (int 0) nil)
                    gdatoms vec__31466
                    vec__31469 (precise-identity-substitutions* db gdatoms)
                    submap (nth vec__31469 (int 0) nil)
                    cantsubmap (nth vec__31469 (int 1) nil)]
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
           (fn fn__31474
             ([logentry]
               (let [data (:data logentry)
                     inst (first
                            (filter
                              (fn fn__31477
                                ([p__31476]
                                  (let [map__31478 p__31476
                                        map__31478 (if (seq? map__31478)
                                                     (if (next map__31478)
                                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                         (to-array map__31478))
                                                       (if (seq map__31478) (first map__31478) {}))
                                                     map__31478)
                                        a (get map__31478 :a)]
                                    (= a instid))))
                              data))
                     map__31475 inst
                     map__31475 (if (seq? map__31475)
                                  (if (next map__31475)
                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                      (to-array map__31475))
                                    (if (seq map__31475) (first map__31475) {}))
                                  map__31475)
                     e (get map__31475 :e)
                     a (get map__31475 :a)
                     v (get map__31475 :v)
                     tx (get map__31475 :tx)
                     indexed? (boolean (seq (d/datoms db :avet a v e tx)))
                     identities (into
                                  #{}
                                  (when-not indexed?
                                    (reduce
                                      (fn fn__31481
                                        ([ret p__31480]
                                          (let [map__31482 p__31480
                                                map__31482 (if
                                                             (seq? map__31482)
                                                             (if
                                                               (next map__31482)
                                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                 (to-array map__31482))
                                                               (if
                                                                 (seq map__31482)
                                                                 (first map__31482)
                                                                 {}))
                                                             map__31482)
                                                d map__31482
                                                a (get map__31482 :a)]
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
            vec__31486 (precise-identity-substitutions (tools/avof-map datoms) db aids)
            sub (nth vec__31486 (int 0) nil)
            cantsub (nth vec__31486 (int 1) nil)]
        (filter
          (fn fn__31490
            ([p__31489]
              (let [map__31491 p__31489
                    map__31491 (if (seq? map__31491)
                                 (if (next map__31491)
                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                     (to-array map__31491))
                                   (if (seq map__31491) (first map__31491) {}))
                                 map__31491)
                    e (get map__31491 :e)
                    a (get map__31491 :a)]
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
     ([uri t & p__31497]
       (let [vec__31498 p__31497 unique_datom_file (nth vec__31498 (int 0) nil) done (promise)]
         (try
           (let [cr (tools/connection-resources uri)
                 map__31501 (tools/db-resources cr)
                 map__31501 (if (seq? map__31501)
                              (if (next map__31501)
                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                  (to-array map__31501))
                                (if (seq map__31501) (first map__31501) {}))
                              map__31501)
                 db (get map__31501 :db)
                 log (get map__31501 :log)
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
                     (fn fn__31503
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
                             t__8765__auto__
                             (do
                               (let [logger (org.slf4j.LoggerFactory/getLogger
                                              "datomic.tools.detect-865")
                                     ex t__8765__auto__]
                                 (when (.isWarnEnabled ^org.slf4j.Logger logger)
                                   (.warn
                                     ^org.slf4j.Logger logger
                                     (datomic.slf4j/process "error executing future")
                                     ^java.lang.Throwable ex)
                                   (datomic.slf4j/caused-by logger ex))
                                 nil)
                               (datomic.monitor/alarm :UnhandledException)
                               (throw ^java.lang.Throwable t__8765__auto__)
                               nil))))))
                 results (remove
                           :indexed?
                           (map
                             (fn fn__31505
                               ([x]
                                 (when pace (^clojure.lang.IFn pace))
                                 (reset! progress_state (:t x))
                                 x))
                             (crosscheck-tx-instants* cr db t)))
                 initial_t (:t (first results))
                 identities (unique-conflicts db (mapcat :identities results))
                 results (mapv
                           (fn fn__31507 ([p1__31495#] (dissoc p1__31495# :identities :indexed?)))
                           results)
                 unique_attrs (into
                                #{}
                                (mapv
                                  (fn fn__31509
                                    ([p1__31496#]
                                      (db/resolve-kw db (.a ^datomic.Datom p1__31496#))))
                                  identities))]
             (push-thread-bindings (hash-map #'*print-length* nil))
             (try
               (let [ts (reduce
                          (fn fn__31512
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
                 (loop [seq_31515 (seq identities) chunk_31516 nil count_31517 0 i_31518 0]
                   (if (< i_31518 count_31517)
                     (let [d (.nth ^clojure.lang.Indexed chunk_31516 (int i_31518))]
                       (.write ^java.io.Writer writer (prn-str (tools/pretty-datom db d)))
                       (recur seq_31515 chunk_31516 count_31517 (inc i_31518)))
                     (let [temp__5825__auto__ (seq seq_31515)]
                       (when temp__5825__auto__
                         (let [seq_31515 temp__5825__auto__]
                           (if (chunked-seq? seq_31515)
                             (let [c__6090__auto__ (chunk-first seq_31515)]
                               (recur
                                 (chunk-rest seq_31515)
                                 c__6090__auto__
                                 (int (count c__6090__auto__))
                                 (int 0)))
                             (let [d (first seq_31515)]
                               (.write ^java.io.Writer writer (prn-str (tools/pretty-datom db d)))
                               (recur (next seq_31515) nil 0 0)))))))))))
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
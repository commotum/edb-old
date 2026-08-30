(do
  (clojure.core/in-ns 'datomic.tools.index-checks)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.api :as 'd]
        ['datomic.db :as 'db]
        ['datomic.tools :as 'tools])
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.db.Attribute)))
  (when-not (.equals 'datomic.tools.index-checks 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.index-checks))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.api :as 'd]
          ['datomic.db :as 'db]
          ['datomic.tools :as 'tools])
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.db.Attribute))))
  (set! *warn-on-reflection* true)
  (defn card-one-collisions
    ([db sort progress & components]
      (when-not (#{:aevt :eavt} sort)
        (throw
          (java.lang.AssertionError.
            (str "Assert failed: " (pr-str (clojure.core/list #{:aevt :eavt} 'sort))))))
      (let [as (into #{} (tools/card-ones db))]
        (tools/unsorted-seq
          (fn fn__20681
            ([d1 d2]
              (^clojure.lang.IFn progress)
              (not
                (and
                  (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
                  (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))))))
          (filter
            (fn fn__20684 ([p1__20680#] (contains? as (.a ^datomic.Datom p1__20680#))))
            (apply d/datoms db sort components))))))
  (reset-meta!
    #'card-one-collisions
    (assoc
      {:arglists (clojure.core/list ['db 'sort 'progress '& 'components]), :column (int 1)}
      :name
      'card-one-collisions
      :ns
      *ns*))
  (def boot-tail-collision?
   (fn boot_tail_collision_QMARK_
     ([p__20687]
       (let [vec__20688 p__20687 d1 (nth vec__20688 (int 0) nil) _ (nth vec__20688 (int 1) nil)]
         (= (.e ^datomic.Datom d1) (db/BOOT-IDS :db.bootstrap/part))))))
  (reset-meta!
    #'boot-tail-collision?
    (assoc
      {:arglists (clojure.core/list [[(.withMeta 'd1 {:tag 'Datom}) '_]]), :column (int 1)}
      :name
      'boot-tail-collision?
      :ns
      *ns*))
  (def non-unique?
   (fn non_unique_QMARK_
     ([p__20692]
       (let [vec__20693 p__20692 d1 (nth vec__20693 (int 0) nil) d2 (nth vec__20693 (int 1) nil)]
         (and
           (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
           (= (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))
           (= (boolean (.added ^datomic.Datom d1)) (boolean (.added ^datomic.Datom d2))))))))
  (reset-meta!
    #'non-unique?
    (assoc
      {:arglists
       (clojure.core/list [[(.withMeta 'd1 {:tag 'Datom}) (.withMeta 'd2 {:tag 'Datom})]]),
       :column (int 1)}
      :name
      'non-unique?
      :ns
      *ns*))
  (defn reporter ([f] (fn fn__20699 ([x] (^clojure.lang.IFn f x) x))))
  (reset-meta!
    #'reporter
    (assoc {:arglists (clojure.core/list ['f]), :column (int 1)} :name 'reporter :ns *ns*))
  (defn unique-collisions
    ([db progress]
      (seq
        (filter
          non-unique?
          (map
            (reporter progress)
            (partition
              2
              1
              (mapcat
                (fn fn__20702 ([a] (d/datoms db :avet a)))
                (concat (tools/unique-identities db) (tools/unique-values db)))))))))
  (reset-meta!
    #'unique-collisions
    (assoc
      {:arglists (clojure.core/list ['db 'progress]), :column (int 1)}
      :name
      'unique-collisions
      :ns
      *ns*))
  (defn rename-to ([db e] (:v (first (d/datoms db :eavt e :db.sys/reId)))))
  (reset-meta!
    #'rename-to
    (assoc {:arglists (clojure.core/list ['db 'e]), :column (int 1)} :name 'rename-to :ns *ns*))
  (defn rename-from ([db e] (:e (first (d/datoms db :vaet e :db.sys/reId)))))
  (reset-meta!
    #'rename-from
    (assoc {:arglists (clojure.core/list ['db 'e]), :column (int 1)} :name 'rename-from :ns *ns*))
  (def maybe-renamed?
   (fn maybe_renamed_QMARK_
     ([db p__20707]
       (let [map__20708 p__20707
             map__20708 (if (seq? map__20708)
                          (if (next map__20708)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20708))
                            (if (seq map__20708) (first map__20708) {}))
                          map__20708)
             e (get map__20708 :e)
             a (get map__20708 :a)
             v (get map__20708 :v)
             tx (get map__20708 :tx)]
         (or
           (rename-from db e)
           (rename-to db e)
           (and
             (= 20 (.-vtypeid (db/attribute db a)))
             (or (rename-to db v) (rename-from db v))))))))
  (reset-meta!
    #'maybe-renamed?
    (assoc
      {:arglists (clojure.core/list ['db {:keys ['e 'a 'v 'tx]}]), :column (int 1)}
      :name
      'maybe-renamed?
      :ns
      *ns*))
  (defn log-only
    ([cr db progress]
      (let [indexed? (fn indexed_QMARK_
                       ([p__20714]
                         (let [map__20716 p__20714
                               map__20716 (if (seq? map__20716)
                                            (if (next map__20716)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__20716))
                                              (if (seq map__20716) (first map__20716) {}))
                                            map__20716)
                               d map__20716
                               e (get map__20716 :e)
                               a (get map__20716 :a)
                               v (get map__20716 :v)
                               tx (get map__20716 :tx)]
                           (= d (first (d/datoms db :eavt e a v tx))))))]
        (remove
          (partial maybe-renamed? db)
          (remove
            indexed?
            (map (reporter progress) (mapcat :data (tools/tx-range-from-log cr 0 nil))))))))
  (reset-meta!
    #'log-only
    (assoc
      {:arglists (clojure.core/list ['cr 'db 'progress]), :column (int 1)}
      :name
      'log-only
      :ns
      *ns*))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__20719
          ([& _]
            (when (zero? (mod (swap! c inc) n))
              (binding [*out* *err*] (do (print ".") (flush)))))))))
  (reset-meta!
    #'progress-dot-fn
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'progress-dot-fn :ns *ns*))
  (defn -main*
    ([uri]
      (let [cr (tools/connection-resources uri)
            map__20722 (tools/db-resources cr)
            map__20722 (if (seq? map__20722)
                         (if (next map__20722)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20722))
                           (if (seq map__20722) (first map__20722) {}))
                         map__20722)
            db (get map__20722 :db)
            log (get map__20722 :log)
            p (partial tools/pretty-datom db)
            progress (progress-dot-fn 1000)
            probs (atom 0)
            nohist? (tools/nohistory-checker db)]
        (loop [seq_20723 (seq
                           (remove boot-tail-collision? (card-one-collisions db :eavt progress)))
               chunk_20724 nil
               count_20725 0
               i_20726 0]
          (if (< i_20726 count_20725)
            (let [vec__20727 (.nth ^clojure.lang.Indexed chunk_20724 (int i_20726))
                  d1 (nth vec__20727 (int 0) nil)
                  d2 (nth vec__20727 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :card-1-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_20723 chunk_20724 count_20725 (inc i_20726)))
            (let [temp__5825__auto__ (seq seq_20723)]
              (when temp__5825__auto__
                (let [seq_20723 temp__5825__auto__]
                  (if (chunked-seq? seq_20723)
                    (let [c__6090__auto__ (chunk-first seq_20723)]
                      (recur
                        (chunk-rest seq_20723)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [vec__20730 (first seq_20723)
                          d1 (nth vec__20730 (int 0) nil)
                          d2 (nth vec__20730 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :card-1-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_20723) nil 0 0))))))))
        (loop [seq_20733 (seq (unique-collisions db progress))
               chunk_20734 nil
               count_20735 0
               i_20736 0]
          (if (< i_20736 count_20735)
            (let [vec__20737 (.nth ^clojure.lang.Indexed chunk_20734 (int i_20736))
                  d1 (nth vec__20737 (int 0) nil)
                  d2 (nth vec__20737 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :unique-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_20733 chunk_20734 count_20735 (inc i_20736)))
            (let [temp__5825__auto__ (seq seq_20733)]
              (when temp__5825__auto__
                (let [seq_20733 temp__5825__auto__]
                  (if (chunked-seq? seq_20733)
                    (let [c__6090__auto__ (chunk-first seq_20733)]
                      (recur
                        (chunk-rest seq_20733)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [vec__20740 (first seq_20733)
                          d1 (nth vec__20740 (int 0) nil)
                          d2 (nth vec__20740 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :unique-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_20733) nil 0 0))))))))
        (loop [seq_20743 (seq (remove nohist? (log-only cr (d/history db) progress)))
               chunk_20744 nil
               count_20745 0
               i_20746 0]
          (if (< i_20746 count_20745)
            (let [d (.nth ^clojure.lang.Indexed chunk_20744 (int i_20746))]
              (swap! probs inc)
              (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
              (recur seq_20743 chunk_20744 count_20745 (inc i_20746)))
            (let [temp__5825__auto__ (seq seq_20743)]
              (when temp__5825__auto__
                (let [seq_20743 temp__5825__auto__]
                  (if (chunked-seq? seq_20743)
                    (let [c__6090__auto__ (chunk-first seq_20743)]
                      (recur
                        (chunk-rest seq_20743)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [d (first seq_20743)]
                      (swap! probs inc)
                      (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
                      (recur (next seq_20743) nil 0 0))))))))
        (prn {:type :summary, :problems (deref probs)}))))
  (reset-meta!
    #'-main*
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main* :ns *ns*))
  (defn -main
    ([uri]
      (try
        (-main* uri)
        (catch
          java.lang.Throwable
          t
          (do
            (d/shutdown true)
            (.printStackTrace ^java.lang.Throwable t)
            (java.lang.System/exit (int -1))
            nil)))
      (d/shutdown true)
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main :ns *ns*)))
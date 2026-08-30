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
          (fn fn__20986
            ([d1 d2]
              (^clojure.lang.IFn progress)
              (not
                (and
                  (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
                  (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))))))
          (filter
            (fn fn__20989 ([p1__20985#] (contains? as (.a ^datomic.Datom p1__20985#))))
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
     ([p__20992]
       (let [vec__20993 p__20992 d1 (nth vec__20993 (int 0) nil) _ (nth vec__20993 (int 1) nil)]
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
     ([p__20997]
       (let [vec__20998 p__20997 d1 (nth vec__20998 (int 0) nil) d2 (nth vec__20998 (int 1) nil)]
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
  (defn reporter ([f] (fn fn__21004 ([x] (^clojure.lang.IFn f x) x))))
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
                (fn fn__21007 ([a] (d/datoms db :avet a)))
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
     ([db p__21012]
       (let [map__21013 p__21012
             map__21013 (if (seq? map__21013)
                          (if (next map__21013)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21013))
                            (if (seq map__21013) (first map__21013) {}))
                          map__21013)
             e (get map__21013 :e)
             a (get map__21013 :a)
             v (get map__21013 :v)
             tx (get map__21013 :tx)]
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
                       ([p__21019]
                         (let [map__21021 p__21019
                               map__21021 (if (seq? map__21021)
                                            (if (next map__21021)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__21021))
                                              (if (seq map__21021) (first map__21021) {}))
                                            map__21021)
                               d map__21021
                               e (get map__21021 :e)
                               a (get map__21021 :a)
                               v (get map__21021 :v)
                               tx (get map__21021 :tx)]
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
        (fn fn__21024
          ([& _]
            (when (zero? (mod (swap! c inc) n))
              (binding [*out* *err*] (do (print ".") (flush)))))))))
  (reset-meta!
    #'progress-dot-fn
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'progress-dot-fn :ns *ns*))
  (defn -main*
    ([uri]
      (let [cr (tools/connection-resources uri)
            map__21027 (tools/db-resources cr)
            map__21027 (if (seq? map__21027)
                         (if (next map__21027)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21027))
                           (if (seq map__21027) (first map__21027) {}))
                         map__21027)
            db (get map__21027 :db)
            log (get map__21027 :log)
            p (partial tools/pretty-datom db)
            progress (progress-dot-fn 1000)
            probs (atom 0)
            nohist? (tools/nohistory-checker db)]
        (loop [seq_21028 (seq
                           (remove boot-tail-collision? (card-one-collisions db :eavt progress)))
               chunk_21029 nil
               count_21030 0
               i_21031 0]
          (if (< i_21031 count_21030)
            (let [vec__21032 (.nth ^clojure.lang.Indexed chunk_21029 (int i_21031))
                  d1 (nth vec__21032 (int 0) nil)
                  d2 (nth vec__21032 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :card-1-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_21028 chunk_21029 count_21030 (inc i_21031)))
            (let [temp__5804__auto__ (seq seq_21028)]
              (when temp__5804__auto__
                (let [seq_21028 temp__5804__auto__]
                  (if (chunked-seq? seq_21028)
                    (let [c__6065__auto__ (chunk-first seq_21028)]
                      (recur
                        (chunk-rest seq_21028)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__21035 (first seq_21028)
                          d1 (nth vec__21035 (int 0) nil)
                          d2 (nth vec__21035 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :card-1-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_21028) nil 0 0))))))))
        (loop [seq_21038 (seq (unique-collisions db progress))
               chunk_21039 nil
               count_21040 0
               i_21041 0]
          (if (< i_21041 count_21040)
            (let [vec__21042 (.nth ^clojure.lang.Indexed chunk_21039 (int i_21041))
                  d1 (nth vec__21042 (int 0) nil)
                  d2 (nth vec__21042 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :unique-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_21038 chunk_21039 count_21040 (inc i_21041)))
            (let [temp__5804__auto__ (seq seq_21038)]
              (when temp__5804__auto__
                (let [seq_21038 temp__5804__auto__]
                  (if (chunked-seq? seq_21038)
                    (let [c__6065__auto__ (chunk-first seq_21038)]
                      (recur
                        (chunk-rest seq_21038)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__21045 (first seq_21038)
                          d1 (nth vec__21045 (int 0) nil)
                          d2 (nth vec__21045 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :unique-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_21038) nil 0 0))))))))
        (loop [seq_21048 (seq (remove nohist? (log-only cr (d/history db) progress)))
               chunk_21049 nil
               count_21050 0
               i_21051 0]
          (if (< i_21051 count_21050)
            (let [d (.nth ^clojure.lang.Indexed chunk_21049 (int i_21051))]
              (swap! probs inc)
              (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
              (recur seq_21048 chunk_21049 count_21050 (inc i_21051)))
            (let [temp__5804__auto__ (seq seq_21048)]
              (when temp__5804__auto__
                (let [seq_21048 temp__5804__auto__]
                  (if (chunked-seq? seq_21048)
                    (let [c__6065__auto__ (chunk-first seq_21048)]
                      (recur
                        (chunk-rest seq_21048)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [d (first seq_21048)]
                      (swap! probs inc)
                      (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
                      (recur (next seq_21048) nil 0 0))))))))
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
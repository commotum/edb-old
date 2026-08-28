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
          (fn fn__26979
            ([d1 d2]
              (^clojure.lang.IFn progress)
              (not
                (and
                  (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
                  (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))))))
          (filter
            (fn fn__26982 ([p1__26978#] (contains? as (.a ^datomic.Datom p1__26978#))))
            (apply d/datoms db sort components))))))
  (defn boot-tail-collision?
    ([p__26985]
      (let [vec__26986 p__26985 d1 (nth vec__26986 (int 0) nil) _ (nth vec__26986 (int 1) nil)]
        (= (.e ^datomic.Datom d1) (db/BOOT-IDS :db.bootstrap/part)))))
  (defn non-unique?
    ([p__26990]
      (let [vec__26991 p__26990 d1 (nth vec__26991 (int 0) nil) d2 (nth vec__26991 (int 1) nil)]
        (and
          (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
          (= (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))
          (= (boolean (.added ^datomic.Datom d1)) (boolean (.added ^datomic.Datom d2)))))))
  (defn reporter ([f] (fn fn__26997 ([x] (^clojure.lang.IFn f x) x))))
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
                (fn fn__27000 ([a] (d/datoms db :avet a)))
                (concat (tools/unique-identities db) (tools/unique-values db)))))))))
  (defn rename-to ([db e] (:v (first (d/datoms db :eavt e :db.sys/reId)))))
  (defn rename-from ([db e] (:e (first (d/datoms db :vaet e :db.sys/reId)))))
  (defn maybe-renamed?
    ([db p__27005]
      (let [map__27006 p__27005
            map__27006 (if (seq? map__27006)
                         (if (next map__27006)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27006))
                           (if (seq map__27006) (first map__27006) {}))
                         map__27006)
            e (get map__27006 :e)
            a (get map__27006 :a)
            v (get map__27006 :v)
            tx (get map__27006 :tx)]
        (or
          (rename-from db e)
          (rename-to db e)
          (and (= 20 (.-vtypeid (db/attribute db a))) (or (rename-to db v) (rename-from db v)))))))
  (defn log-only
    ([cr db progress]
      (let [indexed? (fn indexed_QMARK_
                       ([p__27012]
                         (let [map__27014 p__27012
                               map__27014 (if (seq? map__27014)
                                            (if (next map__27014)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__27014))
                                              (if (seq map__27014) (first map__27014) {}))
                                            map__27014)
                               d map__27014
                               e (get map__27014 :e)
                               a (get map__27014 :a)
                               v (get map__27014 :v)
                               tx (get map__27014 :tx)]
                           (= d (first (d/datoms db :eavt e a v tx))))))]
        (remove
          (partial maybe-renamed? db)
          (remove
            indexed?
            (map (reporter progress) (mapcat :data (tools/tx-range-from-log cr 0 nil))))))))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__27017
          ([& _]
            (when (zero? (mod (swap! c inc) n))
              (binding [*out* *err*] (do (print ".") (flush)))))))))
  (defn -main*
    ([uri]
      (let [cr (tools/connection-resources uri)
            map__27020 (tools/db-resources cr)
            map__27020 (if (seq? map__27020)
                         (if (next map__27020)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27020))
                           (if (seq map__27020) (first map__27020) {}))
                         map__27020)
            db (get map__27020 :db)
            log (get map__27020 :log)
            p (partial tools/pretty-datom db)
            progress (progress-dot-fn 1000)
            probs (atom 0)
            nohist? (tools/nohistory-checker db)]
        (loop [seq_27021 (seq
                           (remove boot-tail-collision? (card-one-collisions db :eavt progress)))
               chunk_27022 nil
               count_27023 0
               i_27024 0]
          (if (< i_27024 count_27023)
            (let [vec__27025 (.nth ^clojure.lang.Indexed chunk_27022 (int i_27024))
                  d1 (nth vec__27025 (int 0) nil)
                  d2 (nth vec__27025 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :card-1-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_27021 chunk_27022 count_27023 (inc i_27024)))
            (let [temp__5804__auto__ (seq seq_27021)]
              (when temp__5804__auto__
                (let [seq_27021 temp__5804__auto__]
                  (if (chunked-seq? seq_27021)
                    (let [c__6065__auto__ (chunk-first seq_27021)]
                      (recur
                        (chunk-rest seq_27021)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__27028 (first seq_27021)
                          d1 (nth vec__27028 (int 0) nil)
                          d2 (nth vec__27028 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :card-1-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_27021) nil 0 0))))))))
        (loop [seq_27031 (seq (unique-collisions db progress))
               chunk_27032 nil
               count_27033 0
               i_27034 0]
          (if (< i_27034 count_27033)
            (let [vec__27035 (.nth ^clojure.lang.Indexed chunk_27032 (int i_27034))
                  d1 (nth vec__27035 (int 0) nil)
                  d2 (nth vec__27035 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :unique-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_27031 chunk_27032 count_27033 (inc i_27034)))
            (let [temp__5804__auto__ (seq seq_27031)]
              (when temp__5804__auto__
                (let [seq_27031 temp__5804__auto__]
                  (if (chunked-seq? seq_27031)
                    (let [c__6065__auto__ (chunk-first seq_27031)]
                      (recur
                        (chunk-rest seq_27031)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__27038 (first seq_27031)
                          d1 (nth vec__27038 (int 0) nil)
                          d2 (nth vec__27038 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :unique-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_27031) nil 0 0))))))))
        (loop [seq_27041 (seq (remove nohist? (log-only cr (d/history db) progress)))
               chunk_27042 nil
               count_27043 0
               i_27044 0]
          (if (< i_27044 count_27043)
            (let [d (.nth ^clojure.lang.Indexed chunk_27042 (int i_27044))]
              (swap! probs inc)
              (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
              (recur seq_27041 chunk_27042 count_27043 (inc i_27044)))
            (let [temp__5804__auto__ (seq seq_27041)]
              (when temp__5804__auto__
                (let [seq_27041 temp__5804__auto__]
                  (if (chunked-seq? seq_27041)
                    (let [c__6065__auto__ (chunk-first seq_27041)]
                      (recur
                        (chunk-rest seq_27041)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [d (first seq_27041)]
                      (swap! probs inc)
                      (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
                      (recur (next seq_27041) nil 0 0))))))))
        (prn {:type :summary, :problems (deref probs)}))))
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
      nil)))
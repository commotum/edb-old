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
          (fn fn__21884
            ([d1 d2]
              (^clojure.lang.IFn progress)
              (not
                (and
                  (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
                  (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))))))
          (filter
            (fn fn__21887 ([p1__21883#] (contains? as (.a ^datomic.Datom p1__21883#))))
            (apply d/datoms db sort components))))))
  (defn boot-tail-collision?
    ([p__21890]
      (let [vec__21891 p__21890 d1 (nth vec__21891 (int 0) nil) _ (nth vec__21891 (int 1) nil)]
        (= (.e ^datomic.Datom d1) (db/BOOT-IDS :db.bootstrap/part)))))
  (defn non-unique?
    ([p__21895]
      (let [vec__21896 p__21895 d1 (nth vec__21896 (int 0) nil) d2 (nth vec__21896 (int 1) nil)]
        (and
          (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
          (= (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))
          (= (boolean (.added ^datomic.Datom d1)) (boolean (.added ^datomic.Datom d2)))))))
  (defn reporter ([f] (fn fn__21902 ([x] (^clojure.lang.IFn f x) x))))
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
                (fn fn__21905 ([a] (d/datoms db :avet a)))
                (concat (tools/unique-identities db) (tools/unique-values db)))))))))
  (defn rename-to ([db e] (:v (first (d/datoms db :eavt e :db.sys/reId)))))
  (defn rename-from ([db e] (:e (first (d/datoms db :vaet e :db.sys/reId)))))
  (defn maybe-renamed?
    ([db p__21910]
      (let [map__21911 p__21910
            map__21911 (if (seq? map__21911)
                         (clojure.lang.PersistentHashMap/create (seq map__21911))
                         map__21911)
            e (get map__21911 :e)
            a (get map__21911 :a)
            v (get map__21911 :v)
            tx (get map__21911 :tx)]
        (or
          (rename-from db e)
          (rename-to db e)
          (and (= 20 (.-vtypeid (db/attribute db a))) (or (rename-to db v) (rename-from db v)))))))
  (defn log-only
    ([cr db progress]
      (let [indexed? (fn indexed_QMARK_
                       ([p__21917]
                         (let [map__21919 p__21917
                               map__21919 (if (seq? map__21919)
                                            (clojure.lang.PersistentHashMap/create
                                              (seq map__21919))
                                            map__21919)
                               d map__21919
                               e (get map__21919 :e)
                               a (get map__21919 :a)
                               v (get map__21919 :v)
                               tx (get map__21919 :tx)]
                           (= d (first (d/datoms db :eavt e a v tx))))))]
        (remove
          (partial maybe-renamed? db)
          (remove
            indexed?
            (map (reporter progress) (mapcat :data (tools/tx-range-from-log cr 0 nil))))))))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__21922
          ([& _]
            (when (zero? (mod (swap! c inc) n))
              (binding [*out* *err*] (do (print ".") (flush)))))))))
  (defn -main*
    ([uri]
      (let [cr (tools/connection-resources uri)
            map__21925 (tools/db-resources cr)
            map__21925 (if (seq? map__21925)
                         (clojure.lang.PersistentHashMap/create (seq map__21925))
                         map__21925)
            db (get map__21925 :db)
            log (get map__21925 :log)
            p (partial tools/pretty-datom db)
            progress (progress-dot-fn 1000)
            probs (atom 0)
            nohist? (tools/nohistory-checker db)]
        (loop [seq_21926 (seq
                           (remove boot-tail-collision? (card-one-collisions db :eavt progress)))
               chunk_21927 nil
               count_21928 0
               i_21929 0]
          (if (< i_21929 count_21928)
            (let [vec__21930 (.nth ^clojure.lang.Indexed chunk_21927 (int i_21929))
                  d1 (nth vec__21930 (int 0) nil)
                  d2 (nth vec__21930 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :card-1-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_21926 chunk_21927 count_21928 (inc i_21929)))
            (let [temp__5457__auto__ (seq seq_21926)]
              (when temp__5457__auto__
                (let [seq_21926 temp__5457__auto__]
                  (if (chunked-seq? seq_21926)
                    (let [c__5719__auto__ (chunk-first seq_21926)]
                      (recur
                        (chunk-rest seq_21926)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [vec__21933 (first seq_21926)
                          d1 (nth vec__21933 (int 0) nil)
                          d2 (nth vec__21933 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :card-1-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_21926) nil 0 0))))))))
        (loop [seq_21936 (seq (unique-collisions db progress))
               chunk_21937 nil
               count_21938 0
               i_21939 0]
          (if (< i_21939 count_21938)
            (let [vec__21940 (.nth ^clojure.lang.Indexed chunk_21937 (int i_21939))
                  d1 (nth vec__21940 (int 0) nil)
                  d2 (nth vec__21940 (int 1) nil)]
              (swap! probs inc)
              (prn
                {:type :unique-collision,
                 :d1 (^clojure.lang.IFn p d1),
                 :d2 (^clojure.lang.IFn p d2)})
              (recur seq_21936 chunk_21937 count_21938 (inc i_21939)))
            (let [temp__5457__auto__ (seq seq_21936)]
              (when temp__5457__auto__
                (let [seq_21936 temp__5457__auto__]
                  (if (chunked-seq? seq_21936)
                    (let [c__5719__auto__ (chunk-first seq_21936)]
                      (recur
                        (chunk-rest seq_21936)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [vec__21943 (first seq_21936)
                          d1 (nth vec__21943 (int 0) nil)
                          d2 (nth vec__21943 (int 1) nil)]
                      (swap! probs inc)
                      (prn
                        {:type :unique-collision,
                         :d1 (^clojure.lang.IFn p d1),
                         :d2 (^clojure.lang.IFn p d2)})
                      (recur (next seq_21936) nil 0 0))))))))
        (loop [seq_21946 (seq (remove nohist? (log-only cr (d/history db) progress)))
               chunk_21947 nil
               count_21948 0
               i_21949 0]
          (if (< i_21949 count_21948)
            (let [d (.nth ^clojure.lang.Indexed chunk_21947 (int i_21949))]
              (swap! probs inc)
              (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
              (recur seq_21946 chunk_21947 count_21948 (inc i_21949)))
            (let [temp__5457__auto__ (seq seq_21946)]
              (when temp__5457__auto__
                (let [seq_21946 temp__5457__auto__]
                  (if (chunked-seq? seq_21946)
                    (let [c__5719__auto__ (chunk-first seq_21946)]
                      (recur
                        (chunk-rest seq_21946)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [d (first seq_21946)]
                      (swap! probs inc)
                      (prn {:type :log-only, :d (^clojure.lang.IFn p d)})
                      (recur (next seq_21946) nil 0 0))))))))
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
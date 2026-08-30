(do
  (clojure.core/in-ns 'datomic.core2.log.mem)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.core2.log.mem)
    {:doc "In-memory implementation of datomic.core2.log."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.core.async :as 'a]
        ['cognitect.anomalies :as 'anom]
        ['datomic.core2.algo.search :refer (clojure.core/list 'binary-search 'fn-comparator)]
        ['datomic.core2.log.spi :as 'spi])))
  (when-not (.equals 'datomic.core2.log.mem 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.log.mem))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.core.async :as 'a]
          ['cognitect.anomalies :as 'anom]
          ['datomic.core2.algo.search :refer (clojure.core/list 'binary-search 'fn-comparator)]
          ['datomic.core2.log.spi :as 'spi]))))
  (deftype
    Log
    [items_ref]
    datomic.core2.log.spi.Append
    datomic.core2.log.spi.Item
    datomic.core2.log.spi.Scan
    (-item-body [this item] (spi/result (:body item)))
    (-item-header [this item] (:header item))
    (-scan
      [this opts]
      (let [map__21734 opts
            map__21734 (if (seq? map__21734)
                         (if (next map__21734)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21734))
                           (if (seq map__21734) (first map__21734) {}))
                         map__21734)
            t (get map__21734 :t)
            ch (get map__21734 :ch)
            limit (get map__21734 :limit)
            direction (get map__21734 :direction)
            next_t (if (= (long java.lang.Long/MAX_VALUE) t)
                     (long java.lang.Long/MAX_VALUE)
                     (inc t))
            items (deref items_ref)
            ct (count items)
            idx (datomic.core2.algo.search/binary-search
                  items
                  {:header {:next-t next_t}}
                  (datomic.core2.algo.search/fn-comparator
                    (fn fn__21735 ([p1__21725#] (get-in p1__21725# [:header :next-t])))))
            G__21737 direction]
        (case
          G__21737
          :forward
          (a/onto-chan! ch (take limit (if idx (subvec items idx) [])))
          :backward
          (a/onto-chan! ch (take limit (rseq (if idx (subvec items 0 (inc idx)) items)))))))
    (-append
      [this header body]
      (let [vec__21727 (swap-vals!
                         items_ref
                         (fn fn__21730
                           ([items]
                             (let [map__21731 (:header (peek items))
                                   map__21731 (if (seq? map__21731)
                                                (if (next map__21731)
                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                    (to-array map__21731))
                                                  (if (seq map__21731) (first map__21731) {}))
                                                map__21731)
                                   t (get map__21731 :t)
                                   next_t (get map__21731 :next-t)]
                               (if (or (not t) (= next_t (:t header)))
                                 (conj items {:header header, :body body})
                                 items)))))
            old (nth vec__21727 (int 0) nil)
            new (nth vec__21727 (int 1) nil)]
        (spi/result
          (if (= old new)
            {:cognitect.anomalies/category :cognitect.anomalies/conflict,
             :cognitect.anomalies/message "Not a continuation of the log",
             :datomic.core2.log.mem/tail (:header (peek old)),
             :datomic.core2.log.mem/header header}
            header)))))
  (clojure.core/import 'datomic.core2.log.mem.Log)
  (defn ->Log ([items_ref] (datomic.core2.log.mem.Log. items_ref)))
  (reset-meta!
    #'->Log
    (assoc {:arglists (clojure.core/list ['items-ref]), :column (int 1)} :name '->Log :ns *ns*))
  (defn create
    ([header body] (datomic.core2.log.mem.Log. (atom [{:header header, :body body}])))
    ([] (datomic.core2.log.mem.Log. (atom []))))
  (reset-meta!
    #'create
    (assoc
      {:arglists (clojure.core/list [] ['header 'body]), :column (int 1)}
      :name
      'create
      :ns
      *ns*)))
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
      (let [map__20877 opts
            map__20877 (if (seq? map__20877)
                         (if (next map__20877)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20877))
                           (if (seq map__20877) (first map__20877) {}))
                         map__20877)
            t (get map__20877 :t)
            ch (get map__20877 :ch)
            limit (get map__20877 :limit)
            direction (get map__20877 :direction)
            next_t (if (= (long java.lang.Long/MAX_VALUE) t)
                     (long java.lang.Long/MAX_VALUE)
                     (inc t))
            items (deref items_ref)
            ct (count items)
            idx (datomic.core2.algo.search/binary-search
                  items
                  {:header {:next-t next_t}}
                  (datomic.core2.algo.search/fn-comparator
                    (fn fn__20878 ([p1__20868#] (get-in p1__20868# [:header :next-t])))))
            G__20880 direction]
        (case
          G__20880
          :forward
          (a/onto-chan! ch (take limit (if idx (subvec items idx) [])))
          :backward
          (a/onto-chan! ch (take limit (rseq (if idx (subvec items 0 (inc idx)) items)))))))
    (-append
      [this header body]
      (let [vec__20870 (swap-vals!
                         items_ref
                         (fn fn__20873
                           ([items]
                             (let [map__20874 (:header (peek items))
                                   map__20874 (if (seq? map__20874)
                                                (if (next map__20874)
                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                    (to-array map__20874))
                                                  (if (seq map__20874) (first map__20874) {}))
                                                map__20874)
                                   t (get map__20874 :t)
                                   next_t (get map__20874 :next-t)]
                               (if (or (not t) (= next_t (:t header)))
                                 (conj items {:header header, :body body})
                                 items)))))
            old (nth vec__20870 (int 0) nil)
            new (nth vec__20870 (int 1) nil)]
        (spi/result
          (if (= old new)
            {:cognitect.anomalies/category :cognitect.anomalies/conflict,
             :cognitect.anomalies/message "Not a continuation of the log",
             :datomic.core2.log.mem/tail (:header (peek old)),
             :datomic.core2.log.mem/header header}
            header)))))
  (clojure.core/import 'datomic.core2.log.mem.Log)
  (defn ->Log ([items_ref] (datomic.core2.log.mem.Log. items_ref)))
  (defn create
    ([header body] (datomic.core2.log.mem.Log. (atom [{:header header, :body body}])))
    ([] (datomic.core2.log.mem.Log. (atom [])))))
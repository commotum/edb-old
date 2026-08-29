(require '[datomic.common :as common]
         '[datomic.db :as db]
         '[datomic.index :as index]
         '[datomic.iter :as iter]
         '[datomic.kv-cluster :as kv-cluster]
         '[datomic.promise :as promise]
         '[datomic.update :as update])

(defn vector-iter
  [items]
  (let [index (volatile! 0)]
    (reify datomic.iter.Iter
      (get [_] (nth items @index))
      (next [this]
        (let [next-index (vswap! index inc)]
          (when (< next-index (count items)) this)))
      (prev [_] nil))))

(defn drain-iter
  [source]
  (loop [source source
         result []]
    (if source
      (let [datum (db/dget source)
            next-source (iter/inext source)]
        (recur next-source (conj result datum)))
      result)))

;; A retraction must consume only the older history of the same E/A/V and
;; continue at the next value. The truncated recovery stopped the iterator.
(let [visible-ident (db/asserting-datum 54 10 :db.fn/retractEntity 54)
      ident-retraction (db/retracting-datum 54 10 :db.fn/retractEntity 54)
      older-ident (db/asserting-datum 54 10 :db.fn/retractEntity 0)
      visible-lang (db/asserting-datum 54 46 48 54)
      visible (drain-iter
                (db/filter-retractions
                  (vector-iter
                    [visible-ident ident-retraction older-ident visible-lang])))]
  (assert (= [[54 10 :db.fn/retractEntity]
              [54 46 48]]
             (mapv (fn [datum]
                     [(.getE ^datomic.impl.db.IDatum datum)
                      (.getA ^datomic.impl.db.IDatum datum)
                      (.getV ^datomic.impl.db.IDatum datum)])
                   visible))))

;; swap-xf! returns the successful function result while installing the
;; transformed value. The truncated recovery evaluated the loop, then nil.
(let [state (atom 3)
      result (#'update/swap-xf!
               state
               :after
               (fn [before increment]
                 {:before before :after (+ before increment)})
               4)]
  (assert (= {:before 3 :after 7} result))
  (assert (= 7 @state)))

;; Retry reporting must walk a Throwable cause chain and retain the deepest
;; cause. The truncated recovery discarded the lexical branch value and
;; returned nil for every Throwable, crashing empty-catalog startup while
;; attempting to report a retryable PostgreSQL read.
(let [root (IllegalStateException. "root")
      middle (RuntimeException. "middle" root)
      outer (Exception. "outer" middle)]
  (assert (identical? root (common/root-cause outer)))
  (assert (identical? root (kv-cluster/root-cause outer)))
  (assert (= "java.lang.IllegalStateException"
             (kv-cluster/retry-cause outer)))
  (assert (nil? (common/root-cause nil))))

;; Equal-length byte arrays must return the value produced by the comparison
;; loop. The stale recovery evaluated that loop and then returned nil.
(let [one-two (byte-array [1 2])
      one-three (byte-array [1 3])
      signed-low (byte-array [-128])
      signed-high (byte-array [127])
      shorter (byte-array [127])
      longer (byte-array [-128 0])]
  (assert (= 0 (common/compare-byte-arrays one-two one-two)))
  (assert (= -1 (common/compare-byte-arrays one-two one-three)))
  (assert (= 1 (common/compare-byte-arrays one-three one-two)))
  (assert (= -255 (common/compare-byte-arrays signed-low signed-high)))
  (assert (= -1 (common/compare-byte-arrays shorter longer))))

;; Recovered clojure.core/locking expansions must not remain as raw
;; monitor-enter/monitor-exit source. Delivering these futures is on the
;; storage writer's persistent-index publication path.
(let [completed (promise/delivered :created)
      pending (promise/settable-future)
      calls (atom [])
      direct-executor
      (reify java.util.concurrent.Executor
        (execute [_ runnable]
          (.run ^Runnable runnable)))]
  (assert (= :created @completed))
  (.addListener ^datomic.ListenableFuture
                pending
                ^Runnable (fn [] (swap! calls conj :called))
                direct-executor)
  (assert (identical? pending (deliver pending :published)))
  (assert (= :published @pending))
  (assert (= [:called] @calls)))

;; An empty persistent index must return the initialized disjoined-datom
;; partition. The stale recovery evaluated the result-producing loop and then
;; returned nil, which disabled repair-disjoined's downstream classification.
(let [empty-tree-iter
      (reify datomic.index.ITreeIter
        datomic.iter.Iter
        (seg+item-seq [_] [])
        (seg-seq [_] [])
        (dir-seq [_] [])
        (get [_] nil)
        (next [_] nil)
        (prev [_] nil))
      empty-index
      (reify datomic.btset.IDataSet
        (^long longCount [_] 0)
        (seek [_] empty-tree-iter)
        (seek [_ _] empty-tree-iter)
        (seekLast [_] empty-tree-iter))
      fake-db {:history {:eavt empty-index}
               :recent {:aevt empty-index}}]
  (assert (= {:segmented [], :separated [], :absent []}
             (index/disjoined-datoms fake-db :history :eavt)))
  (assert (= {:segmented [], :separated []}
             (index/disjoined-datoms fake-db :recent :aevt))))

;; Persistent-index construction must continue past no-history retractions.
;; The stale recovery discarded both branch values below, truncating the
;; index input at its first retraction and suppressing the history stream.
(let [historic-attr (db/->Attribute 101 :test/historic nil nil nil nil nil nil nil false nil)
      nohist-attr (db/->Attribute 102 :test/no-history nil nil nil nil nil nil nil true nil)
      attr-by-id {101 historic-attr, 102 nohist-attr}
      historic-retract (db/retracting-datum 2001 101 :historic 1060)
      nohist-retract (db/retracting-datum 2002 102 :replace 1061)
      nohist-assert (db/asserting-datum 2002 102 :replace 1062)
      tail-assert (db/asserting-datum 2003 101 :tail 1063)
      fake-db (Object.)]
  (with-redefs [db/attribute (fn [_ attr-id] (get attr-by-id attr-id))]
    (assert (= [historic-retract tail-assert]
               (vec (index/filter-nohist-pairs
                      fake-db
                      [historic-retract nohist-retract nohist-assert tail-assert])))))
  (let [historic-pair-retract (db/retracting-datum 3001 101 :historic-pair 1064)
        historic-pair-assert (db/asserting-datum 3001 101 :historic-pair 1065)
        nohist-pair-retract (db/retracting-datum 3002 102 :nohist-pair 1066)
        nohist-pair-assert (db/asserting-datum 3002 102 :nohist-pair 1067)
        historic-unmatched (db/retracting-datum 3003 101 :historic-only 1068)
        tail-assert (db/asserting-datum 3004 101 :tail 1069)
        retractions (atom [])]
    (with-redefs [db/attribute (fn [_ attr-id] (get attr-by-id attr-id))]
      (assert (= [tail-assert]
                 (vec (index/separating-retractions
                        fake-db
                        retractions
                        [historic-pair-retract
                         historic-pair-assert
                         nohist-pair-retract
                         nohist-pair-assert
                         historic-unmatched
                         tail-assert])))))
    (assert (= [historic-pair-retract historic-pair-assert historic-unmatched]
               @retractions))))

(println "transactor focused runtime regressions passed")

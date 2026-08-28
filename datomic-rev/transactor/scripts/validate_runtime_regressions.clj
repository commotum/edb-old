(require '[datomic.db :as db]
         '[datomic.iter :as iter]
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

(println "transactor focused runtime regressions passed")

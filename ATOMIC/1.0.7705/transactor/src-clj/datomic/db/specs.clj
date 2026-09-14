(do
  (clojure.core/in-ns 'datomic.db.specs)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.spec.alpha :as 's] ['datomic.db :as 'db])
      (clojure.core/import 'datomic.Database)))
  (when-not (.equals 'datomic.db.specs 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.db.specs))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.spec.alpha :as 's] ['datomic.db :as 'db])
        (clojure.core/import 'datomic.Database))))
  (s/def-impl
    :datomic.db.specs/with-result
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req-un
      [:datomic.db.specs/db-after
       :datomic.db.specs/db-before
       :datomic.db.specs/tx-data
       :datomic.db.specs/tempids])
    (s/map-spec-impl
      {:req-un
       [:datomic.db.specs/db-after
        :datomic.db.specs/db-before
        :datomic.db.specs/tx-data
        :datomic.db.specs/tempids],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__20255 ([G__20254] (map? G__20254)))
        (fn fn__20257 ([G__20254] (contains? G__20254 :db-after)))
        (fn fn__20259 ([G__20254] (contains? G__20254 :db-before)))
        (fn fn__20261 ([G__20254] (contains? G__20254 :tx-data)))
        (fn fn__20263 ([G__20254] (contains? G__20254 :tempids)))],
       :keys-pred
       (fn fn__20265
         ([G__20254]
           (and
             (map? G__20254)
             (contains? G__20254 :db-after)
             (contains? G__20254 :db-before)
             (contains? G__20254 :tx-data)
             (contains? G__20254 :tempids)))),
       :opt-keys [],
       :req-specs
       [:datomic.db.specs/db-after
        :datomic.db.specs/db-before
        :datomic.db.specs/tx-data
        :datomic.db.specs/tempids],
       :req nil,
       :req-keys [:db-after :db-before :tx-data :tempids],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :db-after))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :db-before))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :tx-data))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :tempids))],
       :opt nil})))
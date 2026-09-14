(do
  (clojure.core/in-ns (.withMeta 'datomic.fulltext-index {:author "Stu Halloway"}))
  (.resetMeta
    (clojure.lang.Namespace/find (.withMeta 'datomic.fulltext-index {:author "Stu Halloway"}))
    {:doc "building fulltext index", :author "Stu Halloway"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.common :as 'common]
        ['datomic.monitor :as 'monitor]
        ['datomic.lucene :as 'lucene])
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.ArrayList)))
  (when-not (.equals (.withMeta 'datomic.fulltext-index {:author "Stu Halloway"}) 'clojure.core)
    (dosync
      (commute
        (deref #'clojure.core/*loaded-libs*)
        conj
        (.withMeta 'datomic.fulltext-index {:author "Stu Halloway"})))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.common :as 'common]
          ['datomic.monitor :as 'monitor]
          ['datomic.lucene :as 'lucene])
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.ArrayList))))
  (defn datum->doc
    ([datum]
      (lucene/document
        (lucene/long-field "e" (.getE ^datomic.impl.db.IDatum datum))
        (lucene/long-field "t" (.getT ^datomic.impl.db.IDatum datum))
        (lucene/string-field
          "v"
          (.getV ^datomic.impl.db.IDatum datum)
          :store
          true
          :index
          true
          :analyze
          true))))
  (defonce LuceneProvider {})
  (defprotocol LuceneProvider (fulltext-attr-reader [this attr]))
  (declare datomic.fulltext-index/->PersistentFulltext)
  (declare datomic.fulltext-index/map->PersistentFulltext)
  (defrecord
    PersistentFulltext
    []
    datomic.fulltext_index.LuceneProvider
    (fulltext-attr-reader
      [this attr]
      (let [temp__5457__auto__ (get this attr)]
        (when temp__5457__auto__
          (let [m temp__5457__auto__] (lucene/index-reader (lucene/persistent-directory m)))))))
  (clojure.core/import 'datomic.fulltext_index.PersistentFulltext)
  (defn ->PersistentFulltext ([] (datomic.fulltext_index.PersistentFulltext.)))
  (defn map->PersistentFulltext
    ([m__7585__auto__]
      (PersistentFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn update-fulltext
    ([pft data]
      (let [pft (or pft (datomic.fulltext_index.PersistentFulltext.))
            tmap {}
            G__12366 data
            vec__12367 G__12366
            seq__12368 (seq vec__12367)
            first__12369 (first seq__12368)
            seq__12368 (next seq__12368)
            datum first__12369
            more seq__12368]
        (loop [tmap tmap G__12366 G__12366]
          (let [tmap tmap
                vec__12370 G__12366
                seq__12371 (seq vec__12370)
                first__12372 (first seq__12371)
                seq__12371 (next seq__12371)
                datum first__12372
                more seq__12371]
            (if datum
              (let [a (.getA ^datomic.impl.db.IDatum datum)
                    vec__12373 (let [temp__5455__auto__ (get
                                                          tmap
                                                          (java.lang.Integer/valueOf (int a)))]
                                 (if temp__5455__auto__
                                   (let [entry temp__5455__auto__] [tmap (:writer entry)])
                                   (let [dir (lucene/persistent-directory
                                               (get pft (java.lang.Integer/valueOf (int a)) {}))
                                         entry {:directory dir, :writer (lucene/index-writer dir)}]
                                     [(assoc tmap (java.lang.Integer/valueOf (int a)) entry)
                                      (:writer entry)])))
                    tmap (nth vec__12373 (int 0) nil)
                    writer (nth vec__12373 (int 1) nil)]
                (lucene/add-document writer (datomic.fulltext-index/datum->doc datum))
                (recur tmap more))
              (reduce
                (fn fn__12377
                  ([pft p__12376]
                    (let [vec__12378 p__12376
                          k (nth vec__12378 (int 0) nil)
                          map__12381 (nth vec__12378 (int 1) nil)
                          map__12381 (if (seq? map__12381)
                                       (clojure.lang.PersistentHashMap/create (seq map__12381))
                                       map__12381)
                          directory (get map__12381 :directory)
                          writer (get map__12381 :writer)]
                      (.close ^java.io.Closeable writer)
                      (assoc pft k (deref directory)))))
                pft
                tmap))))))))
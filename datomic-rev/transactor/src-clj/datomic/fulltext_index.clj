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
      (let [temp__5804__auto__ (get this attr)]
        (when temp__5804__auto__
          (let [m temp__5804__auto__] (lucene/index-reader (lucene/persistent-directory m)))))))
  (clojure.core/import 'datomic.fulltext_index.PersistentFulltext)
  (defn ->PersistentFulltext ([] (datomic.fulltext_index.PersistentFulltext.)))
  (defn map->PersistentFulltext
    ([m__7972__auto__]
      (PersistentFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn update-fulltext
    ([pft data]
      (let [pft (or pft (datomic.fulltext_index.PersistentFulltext.))
            tmap {}
            G__9091 data
            vec__9092 G__9091
            seq__9093 (seq vec__9092)
            first__9094 (first seq__9093)
            seq__9093 (next seq__9093)
            datum first__9094
            more seq__9093]
        (loop [tmap tmap G__9091 G__9091]
          (let [tmap tmap
                vec__9095 G__9091
                seq__9096 (seq vec__9095)
                first__9097 (first seq__9096)
                seq__9096 (next seq__9096)
                datum first__9097
                more seq__9096]
            (if datum
              (let [a (.getA ^datomic.impl.db.IDatum datum)
                    vec__9098 (let [temp__5802__auto__ (get
                                                         tmap
                                                         (java.lang.Integer/valueOf (int a)))]
                                (if temp__5802__auto__
                                  (let [entry temp__5802__auto__] [tmap (:writer entry)])
                                  (let [dir (lucene/persistent-directory
                                              (get pft (java.lang.Integer/valueOf (int a)) {}))
                                        entry {:directory dir, :writer (lucene/index-writer dir)}]
                                    [(assoc tmap (java.lang.Integer/valueOf (int a)) entry)
                                     (:writer entry)])))
                    tmap (nth vec__9098 (int 0) nil)
                    writer (nth vec__9098 (int 1) nil)]
                (lucene/add-document writer (datomic.fulltext-index/datum->doc datum))
                (recur tmap more))
              (reduce
                (fn fn__9102
                  ([pft p__9101]
                    (let [vec__9103 p__9101
                          k (nth vec__9103 (int 0) nil)
                          map__9106 (nth vec__9103 (int 1) nil)
                          map__9106 (if (seq? map__9106)
                                      (if (next map__9106)
                                        (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                          (to-array map__9106))
                                        (if (seq map__9106) (first map__9106) {}))
                                      map__9106)
                          directory (get map__9106 :directory)
                          writer (get map__9106 :writer)]
                      (.close ^java.io.Closeable writer)
                      (assoc pft k (deref directory)))))
                pft
                tmap))))))))
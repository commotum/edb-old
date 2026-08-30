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
  (reset-meta!
    #'datum->doc
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'datum {:tag 'IDatum})]), :column (int 1)}
      :name
      'datum->doc
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol LuceneProvider (fulltext-attr-reader [this attr]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.fulltext-index" "LuceneProvider")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'LuceneProvider :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'fulltext-attr-reader
                                        {:arglists (clojure.core/list ['this 'attr])}),
                                      :arglists (clojure.core/list ['this 'attr]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.fulltext-index"
                                       "LuceneProvider"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.fulltext-index" "fulltext-attr-reader")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (.setMeta
    (clojure.lang.RT/var "datomic.fulltext-index" "->PersistentFulltext")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.fulltext-index" "map->PersistentFulltext")
    {:declared true, :column (int 1)})
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
  (reset-meta!
    #'->PersistentFulltext
    (assoc
      {:arglists (clojure.core/list []), :column (int 1)}
      :name
      '->PersistentFulltext
      :ns
      *ns*))
  (defn map->PersistentFulltext
    ([m__7972__auto__]
      (PersistentFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->PersistentFulltext
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->PersistentFulltext
      :ns
      *ns*))
  (defn update-fulltext
    ([pft data]
      (let [pft (or pft (datomic.fulltext_index.PersistentFulltext.))
            tmap {}
            G__11232 data
            vec__11233 G__11232
            seq__11234 (seq vec__11233)
            first__11235 (first seq__11234)
            seq__11234 (next seq__11234)
            datum first__11235
            more seq__11234]
        (loop [tmap tmap G__11232 G__11232]
          (let [tmap tmap
                vec__11236 G__11232
                seq__11237 (seq vec__11236)
                first__11238 (first seq__11237)
                seq__11237 (next seq__11237)
                datum first__11238
                more seq__11237]
            (if datum
              (let [a (.getA ^datomic.impl.db.IDatum datum)
                    vec__11239 (let [temp__5802__auto__ (get
                                                          tmap
                                                          (java.lang.Integer/valueOf (int a)))]
                                 (if temp__5802__auto__
                                   (let [entry temp__5802__auto__] [tmap (:writer entry)])
                                   (let [dir (lucene/persistent-directory
                                               (get pft (java.lang.Integer/valueOf (int a)) {}))
                                         entry {:directory dir, :writer (lucene/index-writer dir)}]
                                     [(assoc tmap (java.lang.Integer/valueOf (int a)) entry)
                                      (:writer entry)])))
                    tmap (nth vec__11239 (int 0) nil)
                    writer (nth vec__11239 (int 1) nil)]
                (lucene/add-document writer (datomic.fulltext-index/datum->doc datum))
                (recur tmap more))
              (reduce
                (fn fn__11243
                  ([pft p__11242]
                    (let [vec__11244 p__11242
                          k (nth vec__11244 (int 0) nil)
                          map__11247 (nth vec__11244 (int 1) nil)
                          map__11247 (if (seq? map__11247)
                                       (if (next map__11247)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__11247))
                                         (if (seq map__11247) (first map__11247) {}))
                                       map__11247)
                          directory (get map__11247 :directory)
                          writer (get map__11247 :writer)]
                      (.close ^java.io.Closeable writer)
                      (assoc pft k (deref directory)))))
                pft
                tmap)))))))
  (reset-meta!
    #'update-fulltext
    (assoc
      {:arglists (clojure.core/list ['pft 'data]), :column (int 1)}
      :name
      'update-fulltext
      :ns
      *ns*)))
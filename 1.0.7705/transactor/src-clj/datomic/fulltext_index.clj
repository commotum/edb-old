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
  (def datum->doc
   (fn datum__GT_doc
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
           true)))))
  (reset-meta!
    #'datum->doc
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'datum {:tag 'IDatum})]), :column (int 1)}
      :name
      'datum->doc
      :ns
      *ns*))
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol LuceneProvider (fulltext-attr-reader [this attr]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.fulltext-index" "LuceneProvider")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'LuceneProvider :ns *ns*))
    (let [protocol_signature__7432 (assoc
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
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.fulltext-index" "fulltext-attr-reader")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
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
      (let [temp__5825__auto__ (get this attr)]
        (when temp__5825__auto__
          (let [m temp__5825__auto__] (lucene/index-reader (lucene/persistent-directory m)))))))
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
    ([m__8001__auto__]
      (PersistentFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->PersistentFulltext
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->PersistentFulltext
      :ns
      *ns*))
  (defn update-fulltext
    ([pft data]
      (let [pft (or pft (datomic.fulltext_index.PersistentFulltext.))
            tmap {}
            G__11665 data
            vec__11666 G__11665
            seq__11667 (seq vec__11666)
            first__11668 (first seq__11667)
            seq__11667 (next seq__11667)
            datum first__11668
            more seq__11667]
        (loop [tmap tmap G__11665 G__11665]
          (let [tmap tmap
                vec__11669 G__11665
                seq__11670 (seq vec__11669)
                first__11671 (first seq__11670)
                seq__11670 (next seq__11670)
                datum first__11671
                more seq__11670]
            (if datum
              (let [a (.getA ^datomic.impl.db.IDatum datum)
                    vec__11672 (let [temp__5823__auto__ (get
                                                          tmap
                                                          (java.lang.Integer/valueOf (int a)))]
                                 (if temp__5823__auto__
                                   (let [entry temp__5823__auto__] [tmap (:writer entry)])
                                   (let [dir (lucene/persistent-directory
                                               (get pft (java.lang.Integer/valueOf (int a)) {}))
                                         entry {:directory dir, :writer (lucene/index-writer dir)}]
                                     [(assoc tmap (java.lang.Integer/valueOf (int a)) entry)
                                      (:writer entry)])))
                    tmap (nth vec__11672 (int 0) nil)
                    writer (nth vec__11672 (int 1) nil)]
                (lucene/add-document writer (datomic.fulltext-index/datum->doc datum))
                (recur tmap more))
              (reduce
                (fn fn__11676
                  ([pft p__11675]
                    (let [vec__11677 p__11675
                          k (nth vec__11677 (int 0) nil)
                          map__11680 (nth vec__11677 (int 1) nil)
                          map__11680 (if (seq? map__11680)
                                       (if (next map__11680)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__11680))
                                         (if (seq map__11680) (first map__11680) {}))
                                       map__11680)
                          directory (get map__11680 :directory)
                          writer (get map__11680 :writer)]
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
(do
  (clojure.core/in-ns (.withMeta 'datomic.fulltext {:author "Stuart Halloway"}))
  (.resetMeta
    (clojure.lang.Namespace/find (.withMeta 'datomic.fulltext {:author "Stuart Halloway"}))
    {:doc
     "Builds and searches per-attribute fulltext indexes. Indexing stores entity, transaction, and analyzed text fields, preserves historical assertions, and publishes immutable Lucene directories with the database index. Search merges readers from the memory, indexing, durable, and history tiers and produces scored hit rows for the Datalog fulltext relation. The consumer-facing entry point is search.",
     :author "Stuart Halloway"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['comp 'compare])
      (clojure.core/use ['datomic.common :only ['compare]])
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['datomic.db :as 'db]
        ['datomic.iter :as 'iter]
        ['datomic.lucene :as 'lucene]
        ['datomic.common :as 'common]
        ['datomic.monitor :as 'monitor]
        ['datomic.fressian :as 'fressian]
        ['datomic.cluster :as 'cluster]
        ['datomic.slf4j :as 'logger]
        ['datomic.fulltext-index :as 'ftindex]
        ['datomic.clusterfs :as 'fs])
      (clojure.core/import 'datomic.db.IDatumImpl)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'java.io.Closeable)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.util.Iterator)
      (clojure.core/import 'com.datomic.lucene.index.IndexReader)
      (clojure.core/import 'com.datomic.lucene.document.Document)
      (clojure.core/import 'com.datomic.lucene.document.Field)
      (clojure.core/import 'com.datomic.lucene.search.IndexSearcher)
      (clojure.core/import 'com.datomic.lucene.search.Query)
      (clojure.core/import 'com.datomic.lucene.search.ScoreDoc)
      (clojure.core/import 'com.datomic.lucene.store.Directory)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'datomic.impl.clusterfs.IClusterFS)
      (clojure.core/import 'datomic.impl.lucene.ClusterDirectory)
      (clojure.core/import 'datomic.impl.lucene.HybridDirectory)))
  (when-not (.equals (.withMeta 'datomic.fulltext {:author "Stuart Halloway"}) 'clojure.core)
    (dosync
      (commute
        (deref #'clojure.core/*loaded-libs*)
        conj
        (.withMeta 'datomic.fulltext {:author "Stuart Halloway"})))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['comp 'compare])
        (clojure.core/use ['datomic.common :only ['compare]])
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['datomic.db :as 'db]
          ['datomic.iter :as 'iter]
          ['datomic.lucene :as 'lucene]
          ['datomic.common :as 'common]
          ['datomic.monitor :as 'monitor]
          ['datomic.fressian :as 'fressian]
          ['datomic.cluster :as 'cluster]
          ['datomic.slf4j :as 'logger]
          ['datomic.fulltext-index :as 'ftindex]
          ['datomic.clusterfs :as 'fs])
        (clojure.core/import 'datomic.db.IDatumImpl)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'java.io.Closeable)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.util.Iterator)
        (clojure.core/import 'com.datomic.lucene.index.IndexReader)
        (clojure.core/import 'com.datomic.lucene.document.Document)
        (clojure.core/import 'com.datomic.lucene.document.Field)
        (clojure.core/import 'com.datomic.lucene.search.IndexSearcher)
        (clojure.core/import 'com.datomic.lucene.search.Query)
        (clojure.core/import 'com.datomic.lucene.search.ScoreDoc)
        (clojure.core/import 'com.datomic.lucene.store.Directory)
        (clojure.core/import 'org.fressian.handlers.WriteHandler)
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'datomic.impl.clusterfs.IClusterFS)
        (clojure.core/import 'datomic.impl.lucene.ClusterDirectory)
        (clojure.core/import 'datomic.impl.lucene.HybridDirectory))))
  (set! *warn-on-reflection* true)
  (clojure.core/use 'clojure.pprint)
  ;; ATOMIC-NOTE [observed; document identity]: Per-attribute directories supply A; each
  ;; document stores E, original V and assertion T. An (E,T)-only native key
  ;; would conflate cardinality-many strings; native document IDs also hash V.
  (defn doc->datum
    ([doc ^long a]
      (let [e (lucene/long-value (lucene/get-field doc "e"))
            t (lucene/long-value (lucene/get-field doc "t"))
            v (lucene/string-value (lucene/get-field doc "v"))]
        (db/asserting-datum (long ^java.lang.Number e) a v (long ^java.lang.Number t)))))
  (reset-meta!
    #'doc->datum
    (assoc
      {:arglists (clojure.core/list ['doc (.withMeta 'a {:tag 'long})]), :column (int 1)}
      :name
      'doc->datum
      :ns
      *ns*))
  (deftype
  ;; ATOMIC-NOTE [observed; result boundary]: This iterator carries original document T
  ;; and normalizes Lucene score by the first hit. extensions/fulltext projects
  ;; columns 2..5 into a set. Native exact-view transaction rebinding and BM25
  ;; scores are explicit adaptations, not Lucene numeric/row-order equivalence.
    SearchIterator
    [searcher search score_docs attr ^float high_score]
    java.util.Iterator
    (next
      [this]
      (let [sd (.next ^java.util.Iterator score_docs)
            doc (.doc
                  ^com.datomic.lucene.search.IndexSearcher searcher
                  (int (.-doc ^com.datomic.lucene.search.ScoreDoc sd)))]
        [search
         attr
         (lucene/long-value (lucene/get-field doc "e"))
         (lucene/string-value (lucene/get-field doc "v"))
         (lucene/long-value (lucene/get-field doc "t"))
         (java.lang.Double/valueOf
           (double (/ (.-score ^com.datomic.lucene.search.ScoreDoc sd) high_score)))]))
    (^boolean hasNext [this] (.hasNext ^java.util.Iterator score_docs)))
  (clojure.core/import 'datomic.fulltext.SearchIterator)
  (defn ->SearchIterator
    ([searcher search score_docs attr high_score]
      (datomic.fulltext.SearchIterator.
        searcher
        search
        score_docs
        attr
        (float ^java.lang.Number high_score))))
  (reset-meta!
    #'->SearchIterator
    (assoc
      {:arglists (clojure.core/list ['searcher 'search 'score-docs 'attr 'high-score]),
       :column (int 1)}
      :name
      '->SearchIterator
      :ns
      *ns*))
  (deftype
    SearchIterable
    [searcher search score_docs attr ^float high_score]
    java.lang.Iterable
    (^java.util.Iterator iterator
      [this]
      (datomic.fulltext.SearchIterator.
        searcher
        search
        (.iterator ^java.lang.Iterable score_docs)
        attr
        (float high_score))))
  (clojure.core/import 'datomic.fulltext.SearchIterable)
  (defn ->SearchIterable
    ([searcher search score_docs attr high_score]
      (datomic.fulltext.SearchIterable.
        searcher
        search
        score_docs
        attr
        (float ^java.lang.Number high_score))))
  (reset-meta!
    #'->SearchIterable
    (assoc
      {:arglists (clojure.core/list ['searcher 'search 'score-docs 'attr 'high-score]),
       :column (int 1)}
      :name
      '->SearchIterable
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; candidates are not facts]: Top-N Lucene documents are checked
  ;; against db/windowed before being returned. Native validation must debit
  ;; rejected historical candidates too, polling direct and enclosing-query
  ;; cancellation inside the cursor. Native limit follows exact-view validation;
  ;; the recovered top-N limit precedes it and may leave fewer visible hits.
  (defn search-iterable
    ([searcher db attr search-map]
      (let [qmap (if (string? search-map) {:search search-map} search-map)
            map__13183 qmap
            map__13183 (if (seq? map__13183)
                         (if (next map__13183)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__13183))
                           (if (seq map__13183) (first map__13183) {}))
                         map__13183)
            search (get map__13183 :search)
            limit (get map__13183 :limit 10000)
            query (lucene/parse-query "v" search)
            scoredocs (.-scoreDocs
                        (.search
                          ^com.datomic.lucene.search.IndexSearcher searcher
                          ^com.datomic.lucene.search.Query query
                          (int limit)))]
        (if (seq scoredocs)
          (datomic.fulltext.SearchIterable.
            searcher
            search-map
            (remove
              nil?
              (map
                (fn fn__13184
                  ([sd]
                    (let [doc (.doc
                                ^com.datomic.lucene.search.IndexSearcher searcher
                                (int (.-doc ^com.datomic.lucene.search.ScoreDoc sd)))
                          d (db/datum
                              db
                              :e
                              (lucene/long-value (lucene/get-field doc "e"))
                              :a
                              attr
                              :v
                              (lucene/string-value (lucene/get-field doc "v"))
                              :asserting
                              false)
                          iter (db/windowed
                                 db
                                 nil
                                 (.seekAEVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d))
                          it (db/dget iter)]
                      (when (and
                              it
                              (=
                                (long (.getE ^datomic.impl.db.IDatum d))
                                (long (.getE ^datomic.impl.db.IDatum it)))
                              (=
                                (long (.getA ^datomic.impl.db.IDatum d))
                                (long (.getA ^datomic.impl.db.IDatum it)))
                              (zero?
                                (common/compare
                                  (.getV ^datomic.impl.db.IDatum d)
                                  (.getV ^datomic.impl.db.IDatum it))))
                        sd))))
                scoredocs))
            attr
            (float (.-score (first scoredocs))))
          []))))
  (reset-meta!
    #'search-iterable
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'searcher {:tag 'IndexSearcher}) 'db 'attr 'search-map]),
       :doc
       "Executes a fulltext search against searcher. search-map may be a search string or {:search string :limit n}; the default limit is 10000. Stale Lucene documents whose assertions are absent from db are removed. Results are lazy and ordered by relevance with scores normalized to the highest-scoring hit.",
       :column (int 1)}
      :name
      'search-iterable
      :ns
      *ns*))
  (def default-chunk-size 54000)
  (reset-meta!
    #'default-chunk-size
    (assoc {:const true, :column (int 1)} :name 'default-chunk-size :ns *ns*))
  (defn hybrid-dir
    ([writer reader delete_handler]
      (datomic.impl.lucene.HybridDirectory.
        ^com.datomic.lucene.store.Directory writer
        ^com.datomic.lucene.store.Directory reader
        ^clojure.lang.IFn delete_handler)))
  (reset-meta!
    #'hybrid-dir
    (assoc
      {:arglists (clojure.core/list ['writer 'reader 'delete-handler]), :column (int 1)}
      :name
      'hybrid-dir
      :ns
      *ns*))
  (defn index-files
    ([index_dir]
      (reduce
        (fn fn__13191
          ([m f] (if (.isFile ^java.io.File f) (assoc m f (.getName ^java.io.File f)) m)))
        {}
        (file-seq index_dir))))
  (reset-meta!
    #'index-files
    (assoc
      {:arglists (clojure.core/list ['index-dir]), :column (int 1)}
      :name
      'index-files
      :ns
      *ns*))
  (defn promote-to-cluster
    ([indexing_job]
      (let [map__13194 indexing_job
            map__13194 (if (seq? map__13194)
                         (if (next map__13194)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__13194))
                           (if (seq map__13194) (first map__13194) {}))
                         map__13194)
            cstore (get map__13194 :cstore)
            path (get map__13194 :path)
            baseid (get map__13194 :baseid)
            basefs (get map__13194 :basefs)
            delete_requests (get map__13194 :delete-requests)
            filemap (datomic.fulltext/index-files path)]
        (if (seq filemap)
          [(fs/create-fs
             cstore
             (datomic.fulltext/index-files path)
             :chunk-size
             (if basefs (:chunk-size basefs) 54000)
             :base
             (when basefs (reduce dissoc (:dir basefs) (deref delete_requests))))
           (fs/chunk-keys basefs (deref delete_requests))]
          [baseid nil]))))
  (reset-meta!
    #'promote-to-cluster
    (assoc
      {:arglists (clojure.core/list ['indexing-job]), :column (int 1)}
      :name
      'promote-to-cluster
      :ns
      *ns*))
  (defn cluster-directory
    ([clusterfs olookup]
      (datomic.impl.lucene.ClusterDirectory.
        ^datomic.impl.clusterfs.IClusterFS clusterfs
        ^clojure.lang.ILookup olookup)))
  (reset-meta!
    #'cluster-directory
    (assoc
      {:arglists (clojure.core/list ['clusterfs 'olookup]), :column (int 1)}
      :name
      'cluster-directory
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.fulltext" "null-dir") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fulltext" "null-dir")
    (datomic.fulltext/cluster-directory
      (reify datomic.impl.clusterfs.IClusterFS (^java.util.Collection getFiles [this] []))
      nil))
  (.setMeta (clojure.lang.RT/var "datomic.fulltext" "work-dir") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.fulltext" "work-dir") (atom "tmp/search"))
  ;; ATOMIC-NOTE [observed; immutable publication]: A local writable directory overlays an
  ;; immutable clustered base; deletion requests accumulate without deleting the
  ;; base in place. promote-to-cluster emits new file metadata/chunks and garbage
  ;; candidates. Native authenticated page/path-copy publication retains this
  ;; separation without preserving Lucene files or local-directory merge policy.
  (defn create-indexing-job
    ([cstore olookup baseid]
      (let [basefs (when baseid (get olookup baseid))
            reader_dir (if baseid
                         (datomic.fulltext/cluster-directory basefs olookup)
                         datomic.fulltext/null-dir)
            path (common/create-temp-directory (deref datomic.fulltext/work-dir))
            writer_dir (lucene/fs-directory path)
            delete_requests (atom [])
            directory (datomic.fulltext/hybrid-dir
                        writer_dir
                        reader_dir
                        (fn fn__13200 ([p1__13199#] (swap! delete_requests conj p1__13199#))))]
        {:cstore cstore,
         :baseid baseid,
         :directory directory,
         :path path,
         :basefs basefs,
         :delete-requests delete_requests})))
  (reset-meta!
    #'create-indexing-job
    (assoc
      {:arglists (clojure.core/list ['cstore 'olookup 'baseid]), :column (int 1)}
      :name
      'create-indexing-job
      :ns
      *ns*))
  (defn add-data-to-writer
    ([writer data]
      (loop [seq_13203 (seq data) chunk_13204 nil count_13205 0 i_13206 0]
        (if (< i_13206 count_13205)
          (let [datum (.nth ^clojure.lang.Indexed chunk_13204 (int i_13206))]
            (lucene/add-document writer (ftindex/datum->doc datum))
            (recur seq_13203 chunk_13204 count_13205 (inc i_13206)))
          (let [temp__5804__auto__ (seq seq_13203)]
            (when temp__5804__auto__
              (let [seq_13203 temp__5804__auto__]
                (if (chunked-seq? seq_13203)
                  (let [c__6065__auto__ (chunk-first seq_13203)]
                    (recur
                      (chunk-rest seq_13203)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [datum (first seq_13203)]
                    (lucene/add-document writer (ftindex/datum->doc datum))
                    (recur (next seq_13203) nil 0 0))))))))))
  (reset-meta!
    #'add-data-to-writer
    (assoc
      {:arglists (clojure.core/list ['writer 'data]), :column (int 1)}
      :name
      'add-data-to-writer
      :ns
      *ns*))
  (defn add-chunked-data-to-writer
    ([writer data]
      (loop [seq_13210 (seq (partition-all 1000 data)) chunk_13211 nil count_13212 0 i_13213 0]
        (if (< i_13213 count_13212)
          (let [datums (.nth ^clojure.lang.Indexed chunk_13211 (int i_13213))]
            (let [m_13214 {:event :index/fulltext-datoms}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.fulltext")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_13214 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (lucene/add-documents
                                            writer
                                            (pmap ftindex/datum->doc datums))}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_13215 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_13216 (logger/format-as-msec (long elapsed_13215))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_13214 :msec msec_13216 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (throw (:threw result__8554__auto__))))
            (recur seq_13210 chunk_13211 count_13212 (inc i_13213)))
          (let [temp__5804__auto__ (seq seq_13210)]
            (when temp__5804__auto__
              (let [seq_13210 temp__5804__auto__]
                (if (chunked-seq? seq_13210)
                  (let [c__6065__auto__ (chunk-first seq_13210)]
                    (recur
                      (chunk-rest seq_13210)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [datums (first seq_13210)]
                    (let [m_13219 {:event :index/fulltext-datoms}
                          ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.fulltext")]
                                            (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                              (.info
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_13219 :phase :begin))))
                                            nil)
                          start__8553__auto__ (java.lang.System/nanoTime)
                          result__8554__auto__ (try
                                                 {:returned
                                                  (lucene/add-documents
                                                    writer
                                                    (pmap ftindex/datum->doc datums))}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8555__auto__
                                                   {:threw t__8555__auto__}))
                          elapsed_13220 (- (java.lang.System/nanoTime) start__8553__auto__)
                          msec_13221 (logger/format-as-msec (long elapsed_13220))]
                      (let [endmsg__8556__auto__ (merge
                                                   (assoc m_13219 :msec msec_13221 :phase :end)
                                                   (when (:threw result__8554__auto__)
                                                     {:threw
                                                      (class (:threw result__8554__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                          (.info ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                        nil)
                      (if (contains? result__8554__auto__ :returned)
                        (:returned result__8554__auto__)
                        (throw (:threw result__8554__auto__))))
                    (recur (next seq_13210) nil 0 0))))))))))
  (reset-meta!
    #'add-chunked-data-to-writer
    (assoc
      {:arglists (clojure.core/list ['writer 'data]), :column (int 1)}
      :name
      'add-chunked-data-to-writer
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; history identity]: Search by E and T, then compare the original
  ;; V before deleting a current document. Multiple strings can share E and T.
  ;; The native assertion key preserves that distinction directly.
  (defn find-historic-docid
    ([searcher datum]
      (let [q (lucene/boolean-query
                (lucene/long-query "e" (long (.getE ^datomic.impl.db.IDatum datum)))
                :must
                (lucene/long-query "t" (long (.getT ^datomic.impl.db.IDatum datum)))
                :must)
            G__13238 (.-scoreDocs
                       (.search
                         ^com.datomic.lucene.search.IndexSearcher searcher
                         ^com.datomic.lucene.search.Query q
                         (int 1000)))
            vec__13239 G__13238
            seq__13240 (seq vec__13239)
            first__13241 (first seq__13240)
            seq__13240 (next seq__13240)
            sd first__13241
            more seq__13240]
        (loop [G__13238 G__13238]
          (let [vec__13242 G__13238
                seq__13243 (seq vec__13242)
                first__13244 (first seq__13243)
                seq__13243 (next seq__13243)
                sd first__13244
                more seq__13243]
            (when sd
              (let [docid (.-doc ^com.datomic.lucene.search.ScoreDoc sd)
                    found (datomic.fulltext/doc->datum
                            (.doc ^com.datomic.lucene.search.IndexSearcher searcher (int docid))
                            (.getA ^datomic.impl.db.IDatum datum))]
                (if (and
                      found
                      (zero?
                        (common/compare
                          (.getV ^datomic.impl.db.IDatum datum)
                          (.getV ^datomic.impl.db.IDatum found))))
                  (java.lang.Integer/valueOf (int docid))
                  (recur more)))))))))
  (reset-meta!
    #'find-historic-docid
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'searcher {:tag 'IndexSearcher}) (.withMeta 'datum {:tag 'IDatum})]),
       :column (int 1)}
      :name
      'find-historic-docid
      :ns
      *ns*))
  (defn remove-data-from-reader
    ([reader data]
      (let [findid (partial datomic.fulltext/find-historic-docid (lucene/index-searcher reader))]
        (loop [seq_13247 (seq data) chunk_13248 nil count_13249 0 i_13250 0]
          (if (< i_13250 count_13249)
            (let [datum (.nth ^clojure.lang.Indexed chunk_13248 (int i_13250))]
              (let [temp__5804__auto__ (^clojure.lang.IFn findid datum)]
                (when temp__5804__auto__
                  (let [docid temp__5804__auto__]
                    (.deleteDocument
                      ^com.datomic.lucene.index.IndexReader reader
                      (int ^java.lang.Number docid))
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                      (when (.isDebugEnabled ^org.slf4j.Logger logger)
                        (.debug
                          ^org.slf4j.Logger logger
                          (logger/process #:fulltext{:remove-data docid})))
                      nil))))
              (recur seq_13247 chunk_13248 count_13249 (inc i_13250)))
            (let [temp__5804__auto__ (seq seq_13247)]
              (when temp__5804__auto__
                (let [seq_13247 temp__5804__auto__]
                  (if (chunked-seq? seq_13247)
                    (let [c__6065__auto__ (chunk-first seq_13247)]
                      (recur
                        (chunk-rest seq_13247)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [datum (first seq_13247)]
                      (let [temp__5804__auto__ (^clojure.lang.IFn findid datum)]
                        (when temp__5804__auto__
                          (let [docid temp__5804__auto__]
                            (.deleteDocument
                              ^com.datomic.lucene.index.IndexReader reader
                              (int ^java.lang.Number docid))
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process #:fulltext{:remove-data docid})))
                              nil))))
                      (recur (next seq_13247) nil 0 0)))))))))))
  (reset-meta!
    #'remove-data-from-reader
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'reader {:tag 'IndexReader}) 'data]),
       :column (int 1)}
      :name
      'remove-data-from-reader
      :ns
      *ns*))
  (defn create-index-on-dir
    ([dir add_data remove_data]
      (with-open [writer (lucene/index-writer dir)]
        (datomic.fulltext/add-chunked-data-to-writer writer add_data))
      (when (seq remove_data)
        (with-open [reader (IndexReader/open
                             ^com.datomic.lucene.store.Directory dir
                             (boolean (.booleanValue false)))]
          (datomic.fulltext/remove-data-from-reader reader remove_data)))
      dir))
  (reset-meta!
    #'create-index-on-dir
    (assoc
      {:arglists (clojure.core/list ['dir 'add-data 'remove-data]), :column (int 1)}
      :name
      'create-index-on-dir
      :ns
      *ns*))
  (defn do-indexing-job
    ([cstore olookup add_data remove_data attr_id dirid]
      (let [job (datomic.fulltext/create-indexing-job cstore olookup dirid)]
        (try
          (do
            (let [m_13261 {:event :index/build-fulltext-local, :attr attr_id}
                  ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.fulltext")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_13261 :phase :begin))))
                                    nil)
                  start__8553__auto__ (java.lang.System/nanoTime)
                  result__8554__auto__ (try
                                         {:returned
                                          (datomic.fulltext/create-index-on-dir
                                            (:directory job)
                                            add_data
                                            remove_data)}
                                         (catch
                                           java.lang.Throwable
                                           t__8555__auto__
                                           {:threw t__8555__auto__}))
                  elapsed_13262 (- (java.lang.System/nanoTime) start__8553__auto__)
                  msec_13263 (logger/format-as-msec (long elapsed_13262))]
              (let [endmsg__8556__auto__ (merge
                                           (assoc m_13261 :msec msec_13263 :phase :end)
                                           (when (:threw result__8554__auto__)
                                             {:threw (class (:threw result__8554__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                nil)
              (if (contains? result__8554__auto__ :returned)
                (:returned result__8554__auto__)
                (throw (:threw result__8554__auto__))))
            (datomic.fulltext/promote-to-cluster job))
          (finally
            (future-call
              (fn fn__13266
                ([]
                  (try
                    (let [m_13267 {:event :index/cleanup-fulltext, :dir (:path job)}
                          ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.fulltext")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_13267 :phase :begin))))
                                            nil)
                          start__8553__auto__ (java.lang.System/nanoTime)
                          result__8554__auto__ (try
                                                 {:returned
                                                  (common/delete-file-recursively (:path job))}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8555__auto__
                                                   {:threw t__8555__auto__}))
                          elapsed_13268 (- (java.lang.System/nanoTime) start__8553__auto__)
                          msec_13269 (logger/format-as-msec (long elapsed_13268))]
                      (let [endmsg__8556__auto__ (merge
                                                   (assoc m_13267 :msec msec_13269 :phase :end)
                                                   (when (:threw result__8554__auto__)
                                                     {:threw
                                                      (class (:threw result__8554__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
                        nil)
                      (if (contains? result__8554__auto__ :returned)
                        (:returned result__8554__auto__)
                        (do (throw (:threw result__8554__auto__)) nil)))
                    (catch
                      java.lang.Throwable
                      t__8798__auto__
                      (do
                        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")
                              ex t__8798__auto__]
                          (when (.isWarnEnabled ^org.slf4j.Logger logger)
                            (.warn
                              ^org.slf4j.Logger logger
                              (logger/process "error executing future")
                              ^java.lang.Throwable ex)
                            (logger/caused-by logger ex))
                          nil)
                        (monitor/alarm :UnhandledException)
                        (throw ^java.lang.Throwable t__8798__auto__)
                        nil)))))))))))
  (reset-meta!
    #'do-indexing-job
    (assoc
      {:arglists (clojure.core/list ['cstore 'olookup 'add-data 'remove-data 'attr-id 'dirid]),
       :column (int 1)}
      :name
      'do-indexing-job
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.fulltext" "->Root") {:declared true, :column (int 1)})
  (.setMeta (clojure.lang.RT/var "datomic.fulltext" "map->Root") {:declared true, :column (int 1)})
  (defrecord Root [attrmap])
  (clojure.core/import 'datomic.fulltext.Root)
  (defn ->Root ([attrmap] (datomic.fulltext.Root. attrmap)))
  (reset-meta!
    #'->Root
    (assoc {:arglists (clojure.core/list ['attrmap]), :column (int 1)} :name '->Root :ns *ns*))
  (defn map->Root
    ([m__7972__auto__]
      (Root/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->Root
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->Root
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.fulltext" "write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fulltext" "write-handlers")
    (merge
      fs/write-handlers
      {datomic.fulltext.Root
       {"search-root"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer w o]
            (do
              (let [r o]
                (.writeTag ^org.fressian.Writer w "search-root" (int 1))
                (.writeObject ^org.fressian.Writer w (.-attrmap ^datomic.fulltext.Root r)))
              nil)))}}))
  (.setMeta (clojure.lang.RT/var "datomic.fulltext" "read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.fulltext" "read-handlers")
    (merge
      fs/read-handlers
      {"search-root"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader rdr tag ^int component_count]
           (datomic.fulltext.Root. (.readObject ^org.fressian.Reader rdr))))}))
  (defn write-changed-val
    ([cstore oldid oldval newval garbage]
      (if (= oldval newval)
        [oldid garbage]
        (let [id (common/rand-uuid)
              result (cluster/create-val
                       cstore
                       (cluster/uuid->val-key id)
                       (fressian/fressian-val newval datomic.fulltext/write-handlers))]
          (if (= :created (deref result))
            [id (conj garbage (cluster/uuid->val-key oldid))]
            (do (throw (java.lang.Error. (str "value write failed " (deref result)))) nil))))))
  (reset-meta!
    #'write-changed-val
    (assoc
      {:arglists (clojure.core/list ['cstore 'oldid 'oldval 'newval 'garbage]), :column (int 1)}
      :name
      'write-changed-val
      :ns
      *ns*))
  (defn find-matching-assertion
    ([db d]
      (let [iter (iter/filter
                   (fn fn__13309 ([p1__13308#] (.isAssertion ^datomic.impl.db.IDatum p1__13308#)))
                   (.seekAEVT
                     ^datomic.db.IDb db
                     (db/datum
                       db
                       :a
                       (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                       :e
                       (long (.getE ^datomic.impl.db.IDatum d))
                       :v
                       (.getV ^datomic.impl.db.IDatum d)
                       :asserting
                       true)))
            it (db/dget iter)]
        (when (and
                it
                (=
                  (long (.getE ^datomic.impl.db.IDatum d))
                  (long (.getE ^datomic.impl.db.IDatum it)))
                (=
                  (long (.getA ^datomic.impl.db.IDatum d))
                  (long (.getA ^datomic.impl.db.IDatum it)))
                (zero?
                  (common/compare
                    (.getV ^datomic.impl.db.IDatum d)
                    (.getV ^datomic.impl.db.IDatum it))))
          it))))
  (reset-meta!
    #'find-matching-assertion
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'IDb}) (.withMeta 'd {:tag 'IDatum})]),
       :column (int 1)}
      :name
      'find-matching-assertion
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; retractions]: An AEVT retraction finds its matching assertion
  ;; and transfers that assertion to history; it is not a fresh searchable text
  ;; identity. Native canonical-history deltas retain ordinary retractions as
  ;; history and remove search documents only when assertions physically disappear
  ;; (for example noHistory/excision), then validate each hit against the view.
  (defn separate-history
    ([db data history]
      (loop [ds (transient []) data data history history]
        (if data
          (let [d (first data)]
            (if (.isAssertion ^datomic.impl.db.IDatum d)
              (recur (conj! ds d) (next data) history)
              (let [n (fnext data)]
                (if (and
                      n
                      (.isAssertion ^datomic.impl.db.IDatum n)
                      (=
                        (long (.getE ^datomic.impl.db.IDatum d))
                        (long (.getE ^datomic.impl.db.IDatum n)))
                      (=
                        (long (.getA ^datomic.impl.db.IDatum d))
                        (long (.getA ^datomic.impl.db.IDatum n)))
                      (zero?
                        (common/compare
                          (.getV ^datomic.impl.db.IDatum d)
                          (.getV ^datomic.impl.db.IDatum n))))
                  (recur ds (nnext data) (conj history n))
                  (recur
                    ds
                    (next data)
                    (let [temp__5802__auto__ (datomic.fulltext/find-matching-assertion db d)]
                      (when temp__5802__auto__
                        (let [match temp__5802__auto__] (conj history match)))))))))
          [(persistent! ds) history]))))
  (reset-meta!
    #'separate-history
    (assoc
      {:arglists (clojure.core/list ['db 'data 'history]), :column (int 1)}
      :name
      'separate-history
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; source job]: Only changed fulltext attributes are handed to
  ;; per-attribute jobs; current/history roots are written after their files.
  ;; Lucene can still merge large segments. Native history-tree differences,
  ;; streaming analysis, spill bounds and path-copied pages are a distinct cost
  ;; model; incremental is not a blanket sublinear or constant-I/O guarantee.
  (defn build-index
    ([cstore olookup db aevt attrids old_root_id old_hist_id]
      (let [m_13321 {:event :index/build-fulltext}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_13321 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (let [attriters (reduce
                                                      (fn fn__13334
                                                        ([m attrid]
                                                          (let [temp__5802__auto__
                                                                (db/scan-aevt aevt attrid)]
                                                            (if
                                                              temp__5802__auto__
                                                              (let
                                                                [iter temp__5802__auto__]
                                                                (assoc m attrid iter))
                                                              m))))
                                                      nil
                                                      attrids)
                                          oldroot (when old_root_id
                                                    (common/getx olookup old_root_id))
                                          oldhist (when old_hist_id
                                                    (common/getx olookup old_hist_id))
                                          vec__13325 (let [rootmap (if
                                                                     oldroot
                                                                     (.-attrmap
                                                                       ^datomic.fulltext.Root oldroot)
                                                                     {})
                                                           histmap (if
                                                                     oldhist
                                                                     (.-attrmap
                                                                       ^datomic.fulltext.Root oldhist)
                                                                     {})
                                                           garbage []
                                                           G__13343 (seq attriters)
                                                           vec__13344 G__13343
                                                           seq__13345 (seq vec__13344)
                                                           first__13346 (first seq__13345)
                                                           seq__13345 (next seq__13345)
                                                           vec__13347 first__13346
                                                           attrid (nth vec__13347 (int 0) nil)
                                                           iter (nth vec__13347 (int 1) nil)
                                                           more seq__13345]
                                                       (loop [rootmap rootmap
                                                              histmap histmap
                                                              garbage garbage
                                                              G__13343 G__13343]
                                                         (let [rootmap rootmap
                                                               histmap histmap
                                                               garbage garbage
                                                               vec__13351 G__13343
                                                               seq__13352 (seq vec__13351)
                                                               first__13353 (first seq__13352)
                                                               seq__13352 (next seq__13352)
                                                               vec__13354 first__13353
                                                               attrid (nth vec__13354 (int 0) nil)
                                                               iter (nth vec__13354 (int 1) nil)
                                                               more seq__13352]
                                                           (if
                                                             attrid
                                                             (let
                                                               [vec__13357
                                                                (datomic.fulltext/separate-history
                                                                  db
                                                                  (iter/iter-seq iter)
                                                                  [])
                                                                data (nth vec__13357 (int 0) nil)
                                                                histdata
                                                                (nth vec__13357 (int 1) nil)
                                                                vec__13360
                                                                (datomic.fulltext/do-indexing-job
                                                                  cstore
                                                                  olookup
                                                                  data
                                                                  histdata
                                                                  attrid
                                                                  (get rootmap attrid))
                                                                newdirid
                                                                (nth vec__13360 (int 0) nil)
                                                                gids (nth vec__13360 (int 1) nil)
                                                                vec__13363
                                                                (datomic.fulltext/do-indexing-job
                                                                  cstore
                                                                  olookup
                                                                  histdata
                                                                  nil
                                                                  attrid
                                                                  (get histmap attrid))
                                                                newhistdirid
                                                                (nth vec__13363 (int 0) nil)
                                                                histgids
                                                                (nth vec__13363 (int 1) nil)]
                                                               (recur
                                                                 (assoc rootmap attrid newdirid)
                                                                 (assoc
                                                                   histmap
                                                                   attrid
                                                                   newhistdirid)
                                                                 (reduce
                                                                   into
                                                                   garbage
                                                                   [gids histgids])
                                                                 more))
                                                             [(datomic.fulltext.Root. rootmap)
                                                              (datomic.fulltext.Root. histmap)
                                                              garbage]))))
                                          root (nth vec__13325 (int 0) nil)
                                          hist (nth vec__13325 (int 1) nil)
                                          garbage (nth vec__13325 (int 2) nil)
                                          vec__13328 (datomic.fulltext/write-changed-val
                                                       cstore
                                                       old_root_id
                                                       oldroot
                                                       root
                                                       garbage)
                                          new_root_id (nth vec__13328 (int 0) nil)
                                          garbage (nth vec__13328 (int 1) nil)
                                          vec__13331 (datomic.fulltext/write-changed-val
                                                       cstore
                                                       old_hist_id
                                                       oldhist
                                                       hist
                                                       garbage)
                                          new_hist_id (nth vec__13331 (int 0) nil)
                                          garbage (nth vec__13331 (int 1) nil)]
                                      (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.fulltext")]
                                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                          (.debug
                                            ^org.slf4j.Logger logger
                                            (logger/process
                                              #:index{:fulltext-garbage
                                                      {:garbage-count
                                                       (java.lang.Integer/valueOf
                                                         (int (count garbage)))}})))
                                        nil)
                                      [new_root_id new_hist_id garbage])}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_13322 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_13323 (logger/format-as-msec (long elapsed_13322))]
        (monitor/add-stat :CreateFulltextIndexMsec msec_13323)
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_13321 :msec msec_13323 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'build-index
    (assoc
      {:arglists
       (clojure.core/list ['cstore 'olookup 'db 'aevt 'attrids 'old-root-id 'old-hist-id]),
       :doc
       "Builds durable fulltext and fulltext-history roots for attrids from the AEVT indexing tier. Each attribute is maintained in its own immutable Lucene directory. Returns the new root identifiers and the durable values superseded by the job.",
       :column (int 1)}
      :name
      'build-index
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.fulltext" "->ClusteredFulltext")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.fulltext" "map->ClusteredFulltext")
    {:declared true, :column (int 1)})
  (defrecord
    ClusteredFulltext
    [olookup ^Root root]
    datomic.fulltext_index.LuceneProvider
    (fulltext-attr-reader
      [this attrid]
      (let [temp__5804__auto__ (get (.-attrmap ^datomic.fulltext.Root root) attrid)]
        (when temp__5804__auto__
          (let [cfsid temp__5804__auto__]
            (lucene/index-reader
              (datomic.fulltext/cluster-directory (common/getx olookup cfsid) olookup)))))))
  (clojure.core/import 'datomic.fulltext.ClusteredFulltext)
  (defn ->ClusteredFulltext ([olookup root] (datomic.fulltext.ClusteredFulltext. olookup root)))
  (reset-meta!
    #'->ClusteredFulltext
    (assoc
      {:arglists (clojure.core/list ['olookup 'root]), :column (int 1)}
      :name
      '->ClusteredFulltext
      :ns
      *ns*))
  (defn map->ClusteredFulltext
    ([m__7972__auto__]
      (ClusteredFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (reset-meta!
    #'map->ClusteredFulltext
    (assoc
      {:arglists (clojure.core/list ['m__7972__auto__]), :column (int 1)}
      :name
      'map->ClusteredFulltext
      :ns
      *ns*))
  (defn clustered-fulltext
    ([olookup rootid]
      (when rootid (datomic.fulltext.ClusteredFulltext. olookup (common/getx olookup rootid)))))
  (reset-meta!
    #'clustered-fulltext
    (assoc
      {:arglists (clojure.core/list ['olookup 'rootid]), :column (int 1)}
      :name
      'clustered-fulltext
      :ns
      *ns*))
  (defn fulltext-index-reader
    ([db idx attrid]
      (let [temp__5804__auto__ (get-in db [idx :fulltext])]
        (when temp__5804__auto__
          (let [lprov temp__5804__auto__] (ftindex/fulltext-attr-reader lprov attrid))))))
  (reset-meta!
    #'fulltext-index-reader
    (assoc
      {:arglists (clojure.core/list ['db 'idx 'attrid]), :column (int 1)}
      :name
      'fulltext-index-reader
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; tier composition]: Merge memory, active indexing and durable
  ;; readers; add the history reader only for isHistory. peer/integrate-lucene
  ;; updates its memory search tier asynchronously, explaining eventual search
  ;; availability. Native recent/speculative candidate completion is synchronous
  ;; and bounded; physical index lag is reported, not used to return stale facts.
  (defn search
    ([db a search-map]
      (let [attrid (db/resolve-id db a)
            temp__5802__auto__ (seq
                                 (remove
                                   nil?
                                   (map
                                     (fn fn__13402
                                       ([p1__13401#]
                                         (datomic.fulltext/fulltext-index-reader
                                           db
                                           p1__13401#
                                           attrid)))
                                     (if (.isHistory ^datomic.Database db)
                                       [:memidx :indexing :index :history]
                                       [:memidx :indexing :index]))))]
        (if temp__5802__auto__
          (let [readers temp__5802__auto__
                reader (lucene/multi-reader readers :close-subreaders false)]
            (datomic.fulltext/search-iterable (lucene/index-searcher reader) db a search-map))
          []))))
  (reset-meta!
    #'search
    (assoc
      {:arglists (clojure.core/list (.withMeta ['db 'a 'search-map] {:tag 'java.lang.Iterable})),
       :doc
       "Searches fulltext attribute a across the database's memory, active-indexing, durable, and optional history tiers. search-map is a search string or {:search string :limit n}. Returns lazy rows of [search-map attribute entity value transaction normalized-score]; the Datalog fulltext function projects entity, value, transaction, and score.",
       :column (int 1)}
      :name
      'search
      :ns
      *ns*)))

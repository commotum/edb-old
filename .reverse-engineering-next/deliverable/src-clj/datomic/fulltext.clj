(do
  (clojure.core/in-ns (.withMeta 'datomic.fulltext {:author "Stuart Halloway"}))
  (.resetMeta
    (clojure.lang.Namespace/find (.withMeta 'datomic.fulltext {:author "Stuart Halloway"}))
    {:doc
     "Fulltext search. Most of the code in this namespace is used\nby the master and the peers to build indices. The only consumer-facing\nAPI is search.",
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
  (defn doc->datum
    ([doc ^long a]
      (let [e (lucene/long-value (lucene/get-field doc "e"))
            t (lucene/long-value (lucene/get-field doc "t"))
            v (lucene/string-value (lucene/get-field doc "v"))]
        (db/asserting-datum (long ^java.lang.Number e) a v (long ^java.lang.Number t)))))
  (deftype
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
  (defn search-iterable
    ([searcher db attr search_map]
      (let [qmap (if (string? search_map) {:search search_map} search_map)
            map__14533 qmap
            map__14533 (if (seq? map__14533)
                         (clojure.lang.PersistentHashMap/create (seq map__14533))
                         map__14533)
            search (get map__14533 :search)
            limit (get map__14533 :limit 10000)
            query (lucene/parse-query "v" search)
            scoredocs (.-scoreDocs
                        (.search
                          ^com.datomic.lucene.search.IndexSearcher searcher
                          ^com.datomic.lucene.search.Query query
                          (int limit)))]
        (if (seq scoredocs)
          (datomic.fulltext.SearchIterable.
            searcher
            search_map
            (remove
              nil?
              (map
                (fn fn__14534
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
  (def default-chunk-size 54000)
  (reset-meta!
    #'default-chunk-size
    (assoc {:const true, :column 1} :name 'default-chunk-size :ns *ns*))
  (defn hybrid-dir
    ([writer reader delete_handler]
      (datomic.impl.lucene.HybridDirectory.
        ^com.datomic.lucene.store.Directory writer
        ^com.datomic.lucene.store.Directory reader
        ^clojure.lang.IFn delete_handler)))
  (defn index-files
    ([index_dir]
      (reduce
        (fn fn__14541
          ([m f] (if (.isFile ^java.io.File f) (assoc m f (.getName ^java.io.File f)) m)))
        {}
        (file-seq index_dir))))
  (defn promote-to-cluster
    ([indexing_job]
      (let [map__14544 indexing_job
            map__14544 (if (seq? map__14544)
                         (clojure.lang.PersistentHashMap/create (seq map__14544))
                         map__14544)
            cstore (get map__14544 :cstore)
            path (get map__14544 :path)
            baseid (get map__14544 :baseid)
            basefs (get map__14544 :basefs)
            delete_requests (get map__14544 :delete-requests)
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
  (defn cluster-directory
    ([clusterfs olookup]
      (datomic.impl.lucene.ClusterDirectory.
        ^datomic.impl.clusterfs.IClusterFS clusterfs
        ^clojure.lang.ILookup olookup)))
  (def null-dir
   (datomic.fulltext/cluster-directory
     (reify datomic.impl.clusterfs.IClusterFS (^java.util.Collection getFiles [this] []))
     nil))
  (def work-dir (atom "tmp/search"))
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
                        (fn fn__14550 ([p1__14549#] (swap! delete_requests conj p1__14549#))))]
        {:cstore cstore,
         :baseid baseid,
         :directory directory,
         :path path,
         :basefs basefs,
         :delete-requests delete_requests})))
  (defn add-data-to-writer
    ([writer data]
      (loop [seq_14553 (seq data) chunk_14554 nil count_14555 0 i_14556 0]
        (if (< i_14556 count_14555)
          (let [datum (.nth ^clojure.lang.Indexed chunk_14554 (int i_14556))]
            (lucene/add-document writer (ftindex/datum->doc datum))
            (recur seq_14553 chunk_14554 count_14555 (inc i_14556)))
          (let [temp__5457__auto__ (seq seq_14553)]
            (when temp__5457__auto__
              (let [seq_14553 temp__5457__auto__]
                (if (chunked-seq? seq_14553)
                  (let [c__5719__auto__ (chunk-first seq_14553)]
                    (recur
                      (chunk-rest seq_14553)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [datum (first seq_14553)]
                    (lucene/add-document writer (ftindex/datum->doc datum))
                    (recur (next seq_14553) nil 0 0))))))))))
  (defn add-chunked-data-to-writer
    ([writer data]
      (loop [seq_14560 (seq (partition-all 1000 data)) chunk_14561 nil count_14562 0 i_14563 0]
        (if (< i_14563 count_14562)
          (let [datums (.nth ^clojure.lang.Indexed chunk_14561 (int i_14563))]
            (let [m_14564 {:event :index/fulltext-datoms}
                  ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.fulltext")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_14564 :phase :begin)))
                                      nil)
                                    nil)
                  start__8981__auto__ (java.lang.System/nanoTime)
                  result__8982__auto__ (try
                                         {:returned
                                          (lucene/add-documents
                                            writer
                                            (pmap ftindex/datum->doc datums))}
                                         (catch
                                           java.lang.Throwable
                                           t__8983__auto__
                                           {:threw t__8983__auto__}))
                  elapsed_14565 (- (java.lang.System/nanoTime) start__8981__auto__)
                  msec_14566 (logger/format-as-msec (long elapsed_14565))]
              (let [endmsg__8984__auto__ (merge
                                           (assoc m_14564 :msec msec_14566 :phase :end)
                                           (when (:threw result__8982__auto__)
                                             {:threw (class (:threw result__8982__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                  nil)
                nil)
              (if (contains? result__8982__auto__ :returned)
                (:returned result__8982__auto__)
                (throw (:threw result__8982__auto__))))
            (recur seq_14560 chunk_14561 count_14562 (inc i_14563)))
          (let [temp__5457__auto__ (seq seq_14560)]
            (when temp__5457__auto__
              (let [seq_14560 temp__5457__auto__]
                (if (chunked-seq? seq_14560)
                  (let [c__5719__auto__ (chunk-first seq_14560)]
                    (recur
                      (chunk-rest seq_14560)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [datums (first seq_14560)]
                    (let [m_14569 {:event :index/fulltext-datoms}
                          ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.fulltext")]
                                            (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                              (.info
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_14569 :phase :begin)))
                                              nil)
                                            nil)
                          start__8981__auto__ (java.lang.System/nanoTime)
                          result__8982__auto__ (try
                                                 {:returned
                                                  (lucene/add-documents
                                                    writer
                                                    (pmap ftindex/datum->doc datums))}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8983__auto__
                                                   {:threw t__8983__auto__}))
                          elapsed_14570 (- (java.lang.System/nanoTime) start__8981__auto__)
                          msec_14571 (logger/format-as-msec (long elapsed_14570))]
                      (let [endmsg__8984__auto__ (merge
                                                   (assoc m_14569 :msec msec_14571 :phase :end)
                                                   (when (:threw result__8982__auto__)
                                                     {:threw
                                                      (class (:threw result__8982__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                          (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                          nil)
                        nil)
                      (if (contains? result__8982__auto__ :returned)
                        (:returned result__8982__auto__)
                        (throw (:threw result__8982__auto__))))
                    (recur (next seq_14560) nil 0 0))))))))))
  (defn find-historic-docid
    ([searcher datum]
      (let [q (lucene/boolean-query
                (lucene/long-query "e" (long (.getE ^datomic.impl.db.IDatum datum)))
                :must
                (lucene/long-query "t" (long (.getT ^datomic.impl.db.IDatum datum)))
                :must)
            G__14588 (.-scoreDocs
                       (.search
                         ^com.datomic.lucene.search.IndexSearcher searcher
                         ^com.datomic.lucene.search.Query q
                         (int 1000)))
            vec__14589 G__14588
            seq__14590 (seq vec__14589)
            first__14591 (first seq__14590)
            seq__14590 (next seq__14590)
            sd first__14591
            more seq__14590]
        (loop [G__14588 G__14588]
          (let [vec__14592 G__14588
                seq__14593 (seq vec__14592)
                first__14594 (first seq__14593)
                seq__14593 (next seq__14593)
                sd first__14594
                more seq__14593]
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
  (defn remove-data-from-reader
    ([reader data]
      (let [findid (partial datomic.fulltext/find-historic-docid (lucene/index-searcher reader))]
        (loop [seq_14597 (seq data) chunk_14598 nil count_14599 0 i_14600 0]
          (if (< i_14600 count_14599)
            (let [datum (.nth ^clojure.lang.Indexed chunk_14598 (int i_14600))]
              (let [temp__5457__auto__ (^clojure.lang.IFn findid datum)]
                (when temp__5457__auto__
                  (let [docid temp__5457__auto__]
                    (.deleteDocument
                      ^com.datomic.lucene.index.IndexReader reader
                      (int ^java.lang.Number docid))
                    (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                      (when (.isDebugEnabled ^org.slf4j.Logger logger)
                        (.debug
                          ^org.slf4j.Logger logger
                          (logger/process #:fulltext{:remove-data docid}))
                        nil)
                      nil))))
              (recur seq_14597 chunk_14598 count_14599 (inc i_14600)))
            (let [temp__5457__auto__ (seq seq_14597)]
              (when temp__5457__auto__
                (let [seq_14597 temp__5457__auto__]
                  (if (chunked-seq? seq_14597)
                    (let [c__5719__auto__ (chunk-first seq_14597)]
                      (recur
                        (chunk-rest seq_14597)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [datum (first seq_14597)]
                      (let [temp__5457__auto__ (^clojure.lang.IFn findid datum)]
                        (when temp__5457__auto__
                          (let [docid temp__5457__auto__]
                            (.deleteDocument
                              ^com.datomic.lucene.index.IndexReader reader
                              (int ^java.lang.Number docid))
                            (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process #:fulltext{:remove-data docid}))
                                nil)
                              nil))))
                      (recur (next seq_14597) nil 0 0)))))))))))
  (defn create-index-on-dir
    ([dir add_data remove_data]
      (let [writer (lucene/index-writer dir)]
        (try
          (datomic.fulltext/add-chunked-data-to-writer writer add_data)
          (finally (do (.close ^com.datomic.lucene.index.IndexWriter writer) nil))))
      (when (seq remove_data)
        (let [reader (IndexReader/open
                       ^com.datomic.lucene.store.Directory dir
                       (boolean (.booleanValue false)))]
          (try
            (datomic.fulltext/remove-data-from-reader reader remove_data)
            (finally (do (.close ^java.io.Closeable reader) nil)))))
      dir))
  (defn do-indexing-job
    ([cstore olookup add_data remove_data attr_id dirid]
      (let [job (datomic.fulltext/create-indexing-job cstore olookup dirid)]
        (try
          (do
            (let [m_14611 {:event :index/build-fulltext-local, :attr attr_id}
                  ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.fulltext")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_14611 :phase :begin)))
                                      nil)
                                    nil)
                  start__8981__auto__ (java.lang.System/nanoTime)
                  result__8982__auto__ (try
                                         {:returned
                                          (datomic.fulltext/create-index-on-dir
                                            (:directory job)
                                            add_data
                                            remove_data)}
                                         (catch
                                           java.lang.Throwable
                                           t__8983__auto__
                                           {:threw t__8983__auto__}))
                  elapsed_14612 (- (java.lang.System/nanoTime) start__8981__auto__)
                  msec_14613 (logger/format-as-msec (long elapsed_14612))]
              (let [endmsg__8984__auto__ (merge
                                           (assoc m_14611 :msec msec_14613 :phase :end)
                                           (when (:threw result__8982__auto__)
                                             {:threw (class (:threw result__8982__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                  nil)
                nil)
              (if (contains? result__8982__auto__ :returned)
                (:returned result__8982__auto__)
                (throw (:threw result__8982__auto__))))
            (datomic.fulltext/promote-to-cluster job))
          (finally
            (future-call
              (fn fn__14616
                ([]
                  (try
                    (let [m_14617 {:event :index/cleanup-fulltext, :dir (:path job)}
                          ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.fulltext")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_14617 :phase :begin)))
                                              nil)
                                            nil)
                          start__8981__auto__ (java.lang.System/nanoTime)
                          result__8982__auto__ (try
                                                 {:returned
                                                  (common/delete-file-recursively (:path job))}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8983__auto__
                                                   {:threw t__8983__auto__}))
                          elapsed_14618 (- (java.lang.System/nanoTime) start__8981__auto__)
                          msec_14619 (logger/format-as-msec (long elapsed_14618))]
                      (let [endmsg__8984__auto__ (merge
                                                   (assoc m_14617 :msec msec_14619 :phase :end)
                                                   (when (:threw result__8982__auto__)
                                                     {:threw
                                                      (class (:threw result__8982__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                          nil)
                        nil)
                      (if (contains? result__8982__auto__ :returned)
                        (:returned result__8982__auto__)
                        (do (throw (:threw result__8982__auto__)) nil)))
                    (catch
                      java.lang.Throwable
                      t__9147__auto__
                      (do
                        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")
                              ex t__9147__auto__]
                          (when (.isWarnEnabled ^org.slf4j.Logger logger)
                            (.warn
                              ^org.slf4j.Logger logger
                              (logger/process "error executing future")
                              ^java.lang.Throwable ex)
                            (logger/caused-by logger ex))
                          nil)
                        (monitor/alarm :UnhandledException)
                        (throw ^java.lang.Throwable t__9147__auto__)
                        nil)))))))))))
  (declare datomic.fulltext/->Root)
  (declare datomic.fulltext/map->Root)
  (defrecord Root [attrmap])
  (clojure.core/import 'datomic.fulltext.Root)
  (defn ->Root ([attrmap] (datomic.fulltext.Root. attrmap)))
  (defn map->Root
    ([m__7585__auto__]
      (Root/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (def write-handlers
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
  (def read-handlers
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
  (defn find-matching-assertion
    ([db d]
      (let [iter (iter/filter
                   (fn fn__14659 ([p1__14658#] (.isAssertion ^datomic.impl.db.IDatum p1__14658#)))
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
                    (let [temp__5455__auto__ (datomic.fulltext/find-matching-assertion db d)]
                      (when temp__5455__auto__
                        (let [match temp__5455__auto__] (conj history match)))))))))
          [(persistent! ds) history]))))
  (defn build-index
    ([cstore olookup db aevt attrids old_root_id old_hist_id]
      (let [m_14671 {:event :index/build-fulltext}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_14671 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [attriters (reduce
                                                      (fn fn__14684
                                                        ([m attrid]
                                                          (let [temp__5455__auto__
                                                                (db/scan-aevt aevt attrid)]
                                                            (if
                                                              temp__5455__auto__
                                                              (let 
                                                                [iter temp__5455__auto__]
                                                                (assoc m attrid iter))
                                                              m))))
                                                      nil
                                                      attrids)
                                          oldroot (when old_root_id
                                                    (common/getx olookup old_root_id))
                                          oldhist (when old_hist_id
                                                    (common/getx olookup old_hist_id))
                                          vec__14675 (let [rootmap (if
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
                                                           G__14693 (seq attriters)
                                                           vec__14694 G__14693
                                                           seq__14695 (seq vec__14694)
                                                           first__14696 (first seq__14695)
                                                           seq__14695 (next seq__14695)
                                                           vec__14697 first__14696
                                                           attrid (nth vec__14697 (int 0) nil)
                                                           iter (nth vec__14697 (int 1) nil)
                                                           more seq__14695]
                                                       (loop [rootmap rootmap
                                                              histmap histmap
                                                              garbage garbage
                                                              G__14693 G__14693]
                                                         (let [rootmap rootmap
                                                               histmap histmap
                                                               garbage garbage
                                                               vec__14701 G__14693
                                                               seq__14702 (seq vec__14701)
                                                               first__14703 (first seq__14702)
                                                               seq__14702 (next seq__14702)
                                                               vec__14704 first__14703
                                                               attrid (nth vec__14704 (int 0) nil)
                                                               iter (nth vec__14704 (int 1) nil)
                                                               more seq__14702]
                                                           (if
                                                             attrid
                                                             (let 
                                                               [vec__14707
                                                                (datomic.fulltext/separate-history
                                                                  db
                                                                  (iter/iter-seq iter)
                                                                  [])
                                                                data (nth vec__14707 (int 0) nil)
                                                                histdata
                                                                (nth vec__14707 (int 1) nil)
                                                                vec__14710
                                                                (datomic.fulltext/do-indexing-job
                                                                  cstore
                                                                  olookup
                                                                  data
                                                                  histdata
                                                                  attrid
                                                                  (get rootmap attrid))
                                                                newdirid
                                                                (nth vec__14710 (int 0) nil)
                                                                gids (nth vec__14710 (int 1) nil)
                                                                vec__14713
                                                                (datomic.fulltext/do-indexing-job
                                                                  cstore
                                                                  olookup
                                                                  histdata
                                                                  nil
                                                                  attrid
                                                                  (get histmap attrid))
                                                                newhistdirid
                                                                (nth vec__14713 (int 0) nil)
                                                                histgids
                                                                (nth vec__14713 (int 1) nil)]
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
                                          root (nth vec__14675 (int 0) nil)
                                          hist (nth vec__14675 (int 1) nil)
                                          garbage (nth vec__14675 (int 2) nil)
                                          vec__14678 (datomic.fulltext/write-changed-val
                                                       cstore
                                                       old_root_id
                                                       oldroot
                                                       root
                                                       garbage)
                                          new_root_id (nth vec__14678 (int 0) nil)
                                          garbage (nth vec__14678 (int 1) nil)
                                          vec__14681 (datomic.fulltext/write-changed-val
                                                       cstore
                                                       old_hist_id
                                                       oldhist
                                                       hist
                                                       garbage)
                                          new_hist_id (nth vec__14681 (int 0) nil)
                                          garbage (nth vec__14681 (int 1) nil)]
                                      (let [logger (org.slf4j.LoggerFactory/getLogger
                                                     "datomic.fulltext")]
                                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                          (.debug
                                            ^org.slf4j.Logger logger
                                            (logger/process
                                              #:index{:fulltext-garbage
                                                      {:garbage-count
                                                       (java.lang.Integer/valueOf
                                                         (int (count garbage)))}}))
                                          nil)
                                        nil)
                                      [new_root_id new_hist_id garbage])}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_14672 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_14673 (logger/format-as-msec (long elapsed_14672))]
        (monitor/add-stat :CreateFulltextIndexMsec msec_14673)
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_14671 :msec msec_14673 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (declare datomic.fulltext/->ClusteredFulltext)
  (declare datomic.fulltext/map->ClusteredFulltext)
  (defrecord
    ClusteredFulltext
    [olookup ^Root root]
    datomic.fulltext_index.LuceneProvider
    (fulltext-attr-reader
      [this attrid]
      (let [temp__5457__auto__ (get (.-attrmap ^datomic.fulltext.Root root) attrid)]
        (when temp__5457__auto__
          (let [cfsid temp__5457__auto__]
            (lucene/index-reader
              (datomic.fulltext/cluster-directory (common/getx olookup cfsid) olookup)))))))
  (clojure.core/import 'datomic.fulltext.ClusteredFulltext)
  (defn ->ClusteredFulltext ([olookup root] (datomic.fulltext.ClusteredFulltext. olookup root)))
  (defn map->ClusteredFulltext
    ([m__7585__auto__]
      (ClusteredFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn clustered-fulltext
    ([olookup rootid]
      (when rootid (datomic.fulltext.ClusteredFulltext. olookup (common/getx olookup rootid)))))
  (defn fulltext-index-reader
    ([db idx attrid]
      (let [temp__5457__auto__ (get-in db [idx :fulltext])]
        (when temp__5457__auto__
          (let [lprov temp__5457__auto__] (ftindex/fulltext-attr-reader lprov attrid))))))
  (defn search
    ([db a search_map]
      (let [attrid (db/resolve-id db a)
            temp__5455__auto__ (seq
                                 (remove
                                   nil?
                                   (map
                                     (fn fn__14752
                                       ([p1__14751#]
                                         (datomic.fulltext/fulltext-index-reader
                                           db
                                           p1__14751#
                                           attrid)))
                                     (if (.isHistory ^datomic.Database db)
                                       [:memidx :indexing :index :history]
                                       [:memidx :indexing :index]))))]
        (if temp__5455__auto__
          (let [readers temp__5455__auto__
                reader (lucene/multi-reader readers :close-subreaders false)]
            (datomic.fulltext/search-iterable (lucene/index-searcher reader) db a search_map))
          [])))))
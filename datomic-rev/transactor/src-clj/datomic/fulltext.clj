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
            map__12606 qmap
            map__12606 (if (seq? map__12606)
                         (if (next map__12606)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12606))
                           (if (seq map__12606) (first map__12606) {}))
                         map__12606)
            search (get map__12606 :search)
            limit (get map__12606 :limit 10000)
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
                (fn fn__12607
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
        (fn fn__12614
          ([m f] (if (.isFile ^java.io.File f) (assoc m f (.getName ^java.io.File f)) m)))
        {}
        (file-seq index_dir))))
  (defn promote-to-cluster
    ([indexing_job]
      (let [map__12617 indexing_job
            map__12617 (if (seq? map__12617)
                         (if (next map__12617)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12617))
                           (if (seq map__12617) (first map__12617) {}))
                         map__12617)
            cstore (get map__12617 :cstore)
            path (get map__12617 :path)
            baseid (get map__12617 :baseid)
            basefs (get map__12617 :basefs)
            delete_requests (get map__12617 :delete-requests)
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
                        (fn fn__12623 ([p1__12622#] (swap! delete_requests conj p1__12622#))))]
        {:cstore cstore,
         :baseid baseid,
         :directory directory,
         :path path,
         :basefs basefs,
         :delete-requests delete_requests})))
  (defn add-data-to-writer
    ([writer data]
      (loop [seq_12626 (seq data) chunk_12627 nil count_12628 0 i_12629 0]
        (if (< i_12629 count_12628)
          (let [datum (.nth ^clojure.lang.Indexed chunk_12627 (int i_12629))]
            (lucene/add-document writer (ftindex/datum->doc datum))
            (recur seq_12626 chunk_12627 count_12628 (inc i_12629)))
          (let [temp__5804__auto__ (seq seq_12626)]
            (when temp__5804__auto__
              (let [seq_12626 temp__5804__auto__]
                (if (chunked-seq? seq_12626)
                  (let [c__6065__auto__ (chunk-first seq_12626)]
                    (recur
                      (chunk-rest seq_12626)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [datum (first seq_12626)]
                    (lucene/add-document writer (ftindex/datum->doc datum))
                    (recur (next seq_12626) nil 0 0))))))))))
  (defn add-chunked-data-to-writer
    ([writer data]
      (loop [seq_12633 (seq (partition-all 1000 data)) chunk_12634 nil count_12635 0 i_12636 0]
        (if (< i_12636 count_12635)
          (let [datums (.nth ^clojure.lang.Indexed chunk_12634 (int i_12636))]
            (let [m_12637 {:event :index/fulltext-datoms}
                  ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.fulltext")]
                                    (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                      (.info
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_12637 :phase :begin))))
                                    nil)
                  start__8584__auto__ (java.lang.System/nanoTime)
                  result__8585__auto__ (try
                                         {:returned
                                          (lucene/add-documents
                                            writer
                                            (pmap ftindex/datum->doc datums))}
                                         (catch
                                           java.lang.Throwable
                                           t__8586__auto__
                                           {:threw t__8586__auto__}))
                  elapsed_12638 (- (java.lang.System/nanoTime) start__8584__auto__)
                  msec_12639 (logger/format-as-msec (long elapsed_12638))]
              (let [endmsg__8587__auto__ (merge
                                           (assoc m_12637 :msec msec_12639 :phase :end)
                                           (when (:threw result__8585__auto__)
                                             {:threw (class (:threw result__8585__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                (when (.isInfoEnabled ^org.slf4j.Logger logger)
                  (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                nil)
              (if (contains? result__8585__auto__ :returned)
                (:returned result__8585__auto__)
                (throw (:threw result__8585__auto__))))
            (recur seq_12633 chunk_12634 count_12635 (inc i_12636)))
          (let [temp__5804__auto__ (seq seq_12633)]
            (when temp__5804__auto__
              (let [seq_12633 temp__5804__auto__]
                (if (chunked-seq? seq_12633)
                  (let [c__6065__auto__ (chunk-first seq_12633)]
                    (recur
                      (chunk-rest seq_12633)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [datums (first seq_12633)]
                    (let [m_12642 {:event :index/fulltext-datoms}
                          ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.fulltext")]
                                            (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                              (.info
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_12642 :phase :begin))))
                                            nil)
                          start__8584__auto__ (java.lang.System/nanoTime)
                          result__8585__auto__ (try
                                                 {:returned
                                                  (lucene/add-documents
                                                    writer
                                                    (pmap ftindex/datum->doc datums))}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8586__auto__
                                                   {:threw t__8586__auto__}))
                          elapsed_12643 (- (java.lang.System/nanoTime) start__8584__auto__)
                          msec_12644 (logger/format-as-msec (long elapsed_12643))]
                      (let [endmsg__8587__auto__ (merge
                                                   (assoc m_12642 :msec msec_12644 :phase :end)
                                                   (when (:threw result__8585__auto__)
                                                     {:threw
                                                      (class (:threw result__8585__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                        (when (.isInfoEnabled ^org.slf4j.Logger logger)
                          (.info ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                        nil)
                      (if (contains? result__8585__auto__ :returned)
                        (:returned result__8585__auto__)
                        (throw (:threw result__8585__auto__))))
                    (recur (next seq_12633) nil 0 0))))))))))
  (defn find-historic-docid
    ([searcher datum]
      (let [q (lucene/boolean-query
                (lucene/long-query "e" (long (.getE ^datomic.impl.db.IDatum datum)))
                :must
                (lucene/long-query "t" (long (.getT ^datomic.impl.db.IDatum datum)))
                :must)
            G__12661 (.-scoreDocs
                       (.search
                         ^com.datomic.lucene.search.IndexSearcher searcher
                         ^com.datomic.lucene.search.Query q
                         (int 1000)))
            vec__12662 G__12661
            seq__12663 (seq vec__12662)
            first__12664 (first seq__12663)
            seq__12663 (next seq__12663)
            sd first__12664
            more seq__12663]
        (loop [G__12661 G__12661]
          (let [vec__12665 G__12661
                seq__12666 (seq vec__12665)
                first__12667 (first seq__12666)
                seq__12666 (next seq__12666)
                sd first__12667
                more seq__12666]
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
        (loop [seq_12670 (seq data) chunk_12671 nil count_12672 0 i_12673 0]
          (if (< i_12673 count_12672)
            (let [datum (.nth ^clojure.lang.Indexed chunk_12671 (int i_12673))]
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
              (recur seq_12670 chunk_12671 count_12672 (inc i_12673)))
            (let [temp__5804__auto__ (seq seq_12670)]
              (when temp__5804__auto__
                (let [seq_12670 temp__5804__auto__]
                  (if (chunked-seq? seq_12670)
                    (let [c__6065__auto__ (chunk-first seq_12670)]
                      (recur
                        (chunk-rest seq_12670)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [datum (first seq_12670)]
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
                      (recur (next seq_12670) nil 0 0)))))))))))
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
  (defn do-indexing-job
    ([cstore olookup add_data remove_data attr_id dirid]
      (let [job (datomic.fulltext/create-indexing-job cstore olookup dirid)]
        (try
          (do
            (let [m_12684 {:event :index/build-fulltext-local, :attr attr_id}
                  ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                 "datomic.fulltext")]
                                    (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                      (.debug
                                        ^org.slf4j.Logger logger
                                        (logger/process (assoc m_12684 :phase :begin))))
                                    nil)
                  start__8584__auto__ (java.lang.System/nanoTime)
                  result__8585__auto__ (try
                                         {:returned
                                          (datomic.fulltext/create-index-on-dir
                                            (:directory job)
                                            add_data
                                            remove_data)}
                                         (catch
                                           java.lang.Throwable
                                           t__8586__auto__
                                           {:threw t__8586__auto__}))
                  elapsed_12685 (- (java.lang.System/nanoTime) start__8584__auto__)
                  msec_12686 (logger/format-as-msec (long elapsed_12685))]
              (let [endmsg__8587__auto__ (merge
                                           (assoc m_12684 :msec msec_12686 :phase :end)
                                           (when (:threw result__8585__auto__)
                                             {:threw (class (:threw result__8585__auto__))}))
                    logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                  (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                nil)
              (if (contains? result__8585__auto__ :returned)
                (:returned result__8585__auto__)
                (throw (:threw result__8585__auto__))))
            (datomic.fulltext/promote-to-cluster job))
          (finally
            (future-call
              (fn fn__12689
                ([]
                  (try
                    (let [m_12690 {:event :index/cleanup-fulltext, :dir (:path job)}
                          ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                                         "datomic.fulltext")]
                                            (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                              (.debug
                                                ^org.slf4j.Logger logger
                                                (logger/process (assoc m_12690 :phase :begin))))
                                            nil)
                          start__8584__auto__ (java.lang.System/nanoTime)
                          result__8585__auto__ (try
                                                 {:returned
                                                  (common/delete-file-recursively (:path job))}
                                                 (catch
                                                   java.lang.Throwable
                                                   t__8586__auto__
                                                   {:threw t__8586__auto__}))
                          elapsed_12691 (- (java.lang.System/nanoTime) start__8584__auto__)
                          msec_12692 (logger/format-as-msec (long elapsed_12691))]
                      (let [endmsg__8587__auto__ (merge
                                                   (assoc m_12690 :msec msec_12692 :phase :end)
                                                   (when (:threw result__8585__auto__)
                                                     {:threw
                                                      (class (:threw result__8585__auto__))}))
                            logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                        (when (.isDebugEnabled ^org.slf4j.Logger logger)
                          (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
                        nil)
                      (if (contains? result__8585__auto__ :returned)
                        (:returned result__8585__auto__)
                        (do (throw (:threw result__8585__auto__)) nil)))
                    (catch
                      java.lang.Throwable
                      t__8829__auto__
                      (do
                        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")
                              ex t__8829__auto__]
                          (when (.isWarnEnabled ^org.slf4j.Logger logger)
                            (.warn
                              ^org.slf4j.Logger logger
                              (logger/process "error executing future")
                              ^java.lang.Throwable ex)
                            (logger/caused-by logger ex))
                          nil)
                        (monitor/alarm :UnhandledException)
                        (throw ^java.lang.Throwable t__8829__auto__)
                        nil)))))))))))
  (declare datomic.fulltext/->Root)
  (declare datomic.fulltext/map->Root)
  (defrecord Root [attrmap])
  (clojure.core/import 'datomic.fulltext.Root)
  (defn ->Root ([attrmap] (datomic.fulltext.Root. attrmap)))
  (defn map->Root
    ([m__7972__auto__]
      (Root/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
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
                   (fn fn__12732 ([p1__12731#] (.isAssertion ^datomic.impl.db.IDatum p1__12731#)))
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
                    (let [temp__5802__auto__ (datomic.fulltext/find-matching-assertion db d)]
                      (when temp__5802__auto__
                        (let [match temp__5802__auto__] (conj history match)))))))))
          [(persistent! ds) history]))))
  (defn build-index
    ([cstore olookup db aevt attrids old_root_id old_hist_id]
      (let [m_12744 {:event :index/build-fulltext}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_12744 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [attriters (reduce
                                                      (fn fn__12757
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
                                          vec__12748 (let [rootmap (if
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
                                                           G__12766 (seq attriters)
                                                           vec__12767 G__12766
                                                           seq__12768 (seq vec__12767)
                                                           first__12769 (first seq__12768)
                                                           seq__12768 (next seq__12768)
                                                           vec__12770 first__12769
                                                           attrid (nth vec__12770 (int 0) nil)
                                                           iter (nth vec__12770 (int 1) nil)
                                                           more seq__12768]
                                                       (loop [rootmap rootmap
                                                              histmap histmap
                                                              garbage garbage
                                                              G__12766 G__12766]
                                                         (let [rootmap rootmap
                                                               histmap histmap
                                                               garbage garbage
                                                               vec__12774 G__12766
                                                               seq__12775 (seq vec__12774)
                                                               first__12776 (first seq__12775)
                                                               seq__12775 (next seq__12775)
                                                               vec__12777 first__12776
                                                               attrid (nth vec__12777 (int 0) nil)
                                                               iter (nth vec__12777 (int 1) nil)
                                                               more seq__12775]
                                                           (if
                                                             attrid
                                                             (let 
                                                               [vec__12780
                                                                (datomic.fulltext/separate-history
                                                                  db
                                                                  (iter/iter-seq iter)
                                                                  [])
                                                                data (nth vec__12780 (int 0) nil)
                                                                histdata
                                                                (nth vec__12780 (int 1) nil)
                                                                vec__12783
                                                                (datomic.fulltext/do-indexing-job
                                                                  cstore
                                                                  olookup
                                                                  data
                                                                  histdata
                                                                  attrid
                                                                  (get rootmap attrid))
                                                                newdirid
                                                                (nth vec__12783 (int 0) nil)
                                                                gids (nth vec__12783 (int 1) nil)
                                                                vec__12786
                                                                (datomic.fulltext/do-indexing-job
                                                                  cstore
                                                                  olookup
                                                                  histdata
                                                                  nil
                                                                  attrid
                                                                  (get histmap attrid))
                                                                newhistdirid
                                                                (nth vec__12786 (int 0) nil)
                                                                histgids
                                                                (nth vec__12786 (int 1) nil)]
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
                                          root (nth vec__12748 (int 0) nil)
                                          hist (nth vec__12748 (int 1) nil)
                                          garbage (nth vec__12748 (int 2) nil)
                                          vec__12751 (datomic.fulltext/write-changed-val
                                                       cstore
                                                       old_root_id
                                                       oldroot
                                                       root
                                                       garbage)
                                          new_root_id (nth vec__12751 (int 0) nil)
                                          garbage (nth vec__12751 (int 1) nil)
                                          vec__12754 (datomic.fulltext/write-changed-val
                                                       cstore
                                                       old_hist_id
                                                       oldhist
                                                       hist
                                                       garbage)
                                          new_hist_id (nth vec__12754 (int 0) nil)
                                          garbage (nth vec__12754 (int 1) nil)]
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
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_12745 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_12746 (logger/format-as-msec (long elapsed_12745))]
        (monitor/add-stat :CreateFulltextIndexMsec msec_12746)
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_12744 :msec msec_12746 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.fulltext")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (declare datomic.fulltext/->ClusteredFulltext)
  (declare datomic.fulltext/map->ClusteredFulltext)
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
  (defn map->ClusteredFulltext
    ([m__7972__auto__]
      (ClusteredFulltext/create
        (if (instance? clojure.lang.MapEquivalence m__7972__auto__)
          m__7972__auto__
          (into {} m__7972__auto__)))))
  (defn clustered-fulltext
    ([olookup rootid]
      (when rootid (datomic.fulltext.ClusteredFulltext. olookup (common/getx olookup rootid)))))
  (defn fulltext-index-reader
    ([db idx attrid]
      (let [temp__5804__auto__ (get-in db [idx :fulltext])]
        (when temp__5804__auto__
          (let [lprov temp__5804__auto__] (ftindex/fulltext-attr-reader lprov attrid))))))
  (defn search
    ([db a search_map]
      (let [attrid (db/resolve-id db a)
            temp__5802__auto__ (seq
                                 (remove
                                   nil?
                                   (map
                                     (fn fn__12825
                                       ([p1__12824#]
                                         (datomic.fulltext/fulltext-index-reader
                                           db
                                           p1__12824#
                                           attrid)))
                                     (if (.isHistory ^datomic.Database db)
                                       [:memidx :indexing :index :history]
                                       [:memidx :indexing :index]))))]
        (if temp__5802__auto__
          (let [readers temp__5802__auto__
                reader (lucene/multi-reader readers :close-subreaders false)]
            (datomic.fulltext/search-iterable (lucene/index-searcher reader) db a search_map))
          [])))))
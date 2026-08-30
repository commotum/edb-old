(do
  (clojure.core/in-ns 'datomic.lucene)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.java.io :as 'io])
      (clojure.core/import 'java.io.ByteArrayInputStream)
      (clojure.core/import 'java.io.File)
      (clojure.core/import 'java.io.InputStream)
      (clojure.core/import 'com.datomic.lucene.index.IndexReader)
      (clojure.core/import 'com.datomic.lucene.index.IndexWriter)
      (clojure.core/import 'com.datomic.lucene.index.IndexWriterConfig)
      (clojure.core/import 'com.datomic.lucene.index.Term)
      (clojure.core/import 'com.datomic.lucene.index.TermEnum)
      (clojure.core/import 'com.datomic.lucene.index.MultiReader)
      (clojure.core/import 'com.datomic.lucene.index.TieredMergePolicy)
      (clojure.core/import 'com.datomic.lucene.search.IndexSearcher)
      (clojure.core/import 'com.datomic.lucene.search.Query)
      (clojure.core/import 'com.datomic.lucene.search.TermQuery)
      (clojure.core/import 'com.datomic.lucene.search.ScoreDoc)
      (clojure.core/import 'com.datomic.lucene.search.PhraseQuery)
      (clojure.core/import 'com.datomic.lucene.search.NumericRangeQuery)
      (clojure.core/import 'com.datomic.lucene.search.BooleanQuery)
      (clojure.core/import 'com.datomic.lucene.search.BooleanClause$Occur)
      (clojure.core/import 'com.datomic.lucene.analysis.Analyzer)
      (clojure.core/import 'com.datomic.lucene.analysis.TokenStream)
      (clojure.core/import 'com.datomic.lucene.analysis.standard.StandardAnalyzer)
      (clojure.core/import 'com.datomic.lucene.store.Directory)
      (clojure.core/import 'com.datomic.lucene.store.FSDirectory)
      (clojure.core/import 'com.datomic.lucene.store.RAMDirectory)
      (clojure.core/import 'com.datomic.lucene.util.Version)
      (clojure.core/import 'com.datomic.lucene.queryParser.QueryParser)
      (clojure.core/import 'com.datomic.lucene.document.Document)
      (clojure.core/import 'com.datomic.lucene.document.Field)
      (clojure.core/import 'com.datomic.lucene.document.Field$Index)
      (clojure.core/import 'com.datomic.lucene.document.Field$Store)
      (clojure.core/import 'com.datomic.lucene.document.NumericField)))
  (when-not (.equals 'datomic.lucene 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.lucene))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.java.io :as 'io])
        (clojure.core/import 'java.io.ByteArrayInputStream)
        (clojure.core/import 'java.io.File)
        (clojure.core/import 'java.io.InputStream)
        (clojure.core/import 'com.datomic.lucene.index.IndexReader)
        (clojure.core/import 'com.datomic.lucene.index.IndexWriter)
        (clojure.core/import 'com.datomic.lucene.index.IndexWriterConfig)
        (clojure.core/import 'com.datomic.lucene.index.Term)
        (clojure.core/import 'com.datomic.lucene.index.TermEnum)
        (clojure.core/import 'com.datomic.lucene.index.MultiReader)
        (clojure.core/import 'com.datomic.lucene.index.TieredMergePolicy)
        (clojure.core/import 'com.datomic.lucene.search.IndexSearcher)
        (clojure.core/import 'com.datomic.lucene.search.Query)
        (clojure.core/import 'com.datomic.lucene.search.TermQuery)
        (clojure.core/import 'com.datomic.lucene.search.ScoreDoc)
        (clojure.core/import 'com.datomic.lucene.search.PhraseQuery)
        (clojure.core/import 'com.datomic.lucene.search.NumericRangeQuery)
        (clojure.core/import 'com.datomic.lucene.search.BooleanQuery)
        (clojure.core/import 'com.datomic.lucene.search.BooleanClause$Occur)
        (clojure.core/import 'com.datomic.lucene.analysis.Analyzer)
        (clojure.core/import 'com.datomic.lucene.analysis.TokenStream)
        (clojure.core/import 'com.datomic.lucene.analysis.standard.StandardAnalyzer)
        (clojure.core/import 'com.datomic.lucene.store.Directory)
        (clojure.core/import 'com.datomic.lucene.store.FSDirectory)
        (clojure.core/import 'com.datomic.lucene.store.RAMDirectory)
        (clojure.core/import 'com.datomic.lucene.util.Version)
        (clojure.core/import 'com.datomic.lucene.queryParser.QueryParser)
        (clojure.core/import 'com.datomic.lucene.document.Document)
        (clojure.core/import 'com.datomic.lucene.document.Field)
        (clojure.core/import 'com.datomic.lucene.document.Field$Index)
        (clojure.core/import 'com.datomic.lucene.document.Field$Store)
        (clojure.core/import 'com.datomic.lucene.document.NumericField))))
  (set! *warn-on-reflection* true)
  (when-not (.startsWith com.datomic.lucene.util.Constants/LUCENE_MAIN_VERSION "3")
    (throw (java.lang.Error. "This version of Datomic requires Lucene 3.x")))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "create-config")
    {:tag com.datomic.lucene.index.IndexWriterConfig,
     :private true,
     :arglists (clojure.core/list ['& {:keys ['version 'analyzer]}]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "create-config")
    (fn create_config
      ([& p__11531]
        (let [map__11532 p__11531
              map__11532 (if (seq? map__11532)
                           (if (next map__11532)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__11532))
                             (if (seq map__11532) (first map__11532) {}))
                           map__11532)
              version (get map__11532 :version)
              analyzer (get map__11532 :analyzer)]
          (com.datomic.lucene.index.IndexWriterConfig.
            ^com.datomic.lucene.util.Version version
            ^com.datomic.lucene.analysis.Analyzer analyzer)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "fs-directory")
    {:tag com.datomic.lucene.store.Directory, :arglists (clojure.core/list ['f]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "fs-directory")
    (fn fs_directory ([f] (FSDirectory/open (io/file f)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "ram-directory")
    {:tag com.datomic.lucene.store.Directory, :arglists (clojure.core/list []), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "ram-directory")
    (fn ram_directory ([] (com.datomic.lucene.store.RAMDirectory.))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "index-writer")
    {:tag com.datomic.lucene.index.IndexWriter,
     :arglists
     (clojure.core/list
       [(.withMeta 'directory {:tag 'Directory}) '& {:keys ['version 'analyzer-fn]}]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "index-writer")
    (fn index_writer
      ([directory & p__11537]
        (let [map__11538 p__11537
              map__11538 (if (seq? map__11538)
                           (if (next map__11538)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__11538))
                             (if (seq map__11538) (first map__11538) {}))
                           map__11538)
              version (get map__11538 :version)
              analyzer_fn (get map__11538 :analyzer-fn)
              version (or version Version/LUCENE_33)
              analyzer_fn (or
                            analyzer_fn
                            (fn fn__11539
                              ([p1__11536#]
                                (com.datomic.lucene.analysis.standard.StandardAnalyzer.
                                  ^com.datomic.lucene.util.Version p1__11536#))))
              analyzer (^clojure.lang.IFn analyzer_fn version)
              config (create-config :version version :analyzer analyzer)
              writer (com.datomic.lucene.index.IndexWriter.
                       ^com.datomic.lucene.store.Directory directory
                       ^com.datomic.lucene.index.IndexWriterConfig config)]
          writer))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "string-field")
    {:tag com.datomic.lucene.document.Field,
     :arglists
     (clojure.core/list
       [(.withMeta 'name {:tag 'String})
        (.withMeta 'value {:tag 'String})
        '&
        {:keys ['index 'store 'analyze 'omit-norms], :as 'opts}]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "string-field")
    (fn string_field
      ([name value & p__11544]
        (let [map__11545 p__11544
              map__11545 (if (seq? map__11545)
                           (if (next map__11545)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__11545))
                             (if (seq map__11545) (first map__11545) {}))
                           map__11545)
              opts map__11545
              index (get map__11545 :index)
              store (get map__11545 :store)
              analyze (get map__11545 :analyze)
              omit_norms (get map__11545 :omit-norms)]
          (com.datomic.lucene.document.Field.
            ^java.lang.String name
            ^java.lang.String value
            (if store Field$Store/YES Field$Store/NO)
            (Field$Index/toIndex (boolean index) (boolean analyze) (boolean omit_norms)))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "long-field")
    {:tag com.datomic.lucene.document.NumericField,
     :arglists
     (clojure.core/list [(.withMeta 'name {:tag 'String}) (.withMeta 'value {:tag 'long})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "long-field")
    (fn long_field
      ([name ^long value]
        (let [G__11547 (com.datomic.lucene.document.NumericField.
                         ^java.lang.String name
                         Field$Store/YES
                         (boolean (.booleanValue true)))]
          (.setLongValue ^com.datomic.lucene.document.NumericField G__11547 (long value))
          G__11547))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "binary-field")
    {:tag com.datomic.lucene.document.Field,
     :arglists
     (clojure.core/list
       [(.withMeta 'name {:tag 'String})
        (.withMeta 'value {:tag 'bytes})
        (.withMeta 'offset {:tag 'long})
        (.withMeta 'length {:tag 'long})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "binary-field")
    (fn binary_field
      ([name value ^long offset ^long length]
        (com.datomic.lucene.document.Field.
          ^java.lang.String name
          ^bytes value
          (int offset)
          (int length)))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "string-value")
    {:tag java.lang.String,
     :arglists (clojure.core/list [(.withMeta 'f {:tag 'Field})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "string-value")
    (fn string_value ([f] (.stringValue ^com.datomic.lucene.document.Field f))))
  (defn long-value ([f] (.getNumericValue ^com.datomic.lucene.document.NumericField f)))
  (reset-meta!
    #'long-value
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'f {:tag 'NumericField})]), :column (int 1)}
      :name
      'long-value
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "long-query")
    {:tag com.datomic.lucene.search.Query, :arglists (clojure.core/list ['f 'l]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "long-query")
    (fn long_query
      ([f l]
        (NumericRangeQuery/newLongRange
          ^java.lang.String f
          ^java.lang.Long l
          ^java.lang.Long l
          (boolean (.booleanValue true))
          (boolean (.booleanValue true))))))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "boolean-query")
    {:tag com.datomic.lucene.search.Query,
     :arglists (clojure.core/list ['& 'queryoccurs]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "boolean-query")
    (fn boolean_query
      ([& queryoccurs]
        (let [bq (com.datomic.lucene.search.BooleanQuery.)]
          (loop [seq_11553 (seq (partition 2 queryoccurs)) chunk_11554 nil count_11555 0 i_11556 0]
            (if (< i_11556 count_11555)
              (let [vec__11557 (.nth ^clojure.lang.Indexed chunk_11554 (int i_11556))
                    q (nth vec__11557 (int 0) nil)
                    o (nth vec__11557 (int 1) nil)]
                (.add
                  ^com.datomic.lucene.search.BooleanQuery bq
                  ^com.datomic.lucene.search.Query q
                  (get
                    {:should BooleanClause$Occur/SHOULD,
                     :must BooleanClause$Occur/MUST,
                     :must_not BooleanClause$Occur/MUST_NOT}
                    o
                    o))
                (recur seq_11553 chunk_11554 count_11555 (inc i_11556)))
              (let [temp__5825__auto__ (seq seq_11553)]
                (when temp__5825__auto__
                  (let [seq_11553 temp__5825__auto__]
                    (if (chunked-seq? seq_11553)
                      (let [c__6090__auto__ (chunk-first seq_11553)]
                        (recur
                          (chunk-rest seq_11553)
                          c__6090__auto__
                          (int (count c__6090__auto__))
                          (int 0)))
                      (let [vec__11560 (first seq_11553)
                            q (nth vec__11560 (int 0) nil)
                            o (nth vec__11560 (int 1) nil)]
                        (.add
                          ^com.datomic.lucene.search.BooleanQuery bq
                          ^com.datomic.lucene.search.Query q
                          (get
                            {:should BooleanClause$Occur/SHOULD,
                             :must BooleanClause$Occur/MUST,
                             :must_not BooleanClause$Occur/MUST_NOT}
                            o
                            o))
                        (recur (next seq_11553) nil 0 0))))))))
          bq))))
  (clojure.core/import 'com.datomic.lucene.search.BooleanClause$Occur)
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "field-stream")
    {:tag java.io.InputStream,
     :arglists (clojure.core/list [(.withMeta 'field {:tag 'Field})]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "field-stream")
    (fn field_stream
      ([field]
        (java.io.ByteArrayInputStream.
          (.getBinaryValue ^com.datomic.lucene.document.AbstractField field)
          (int (.getBinaryOffset ^com.datomic.lucene.document.AbstractField field))
          (int (.getBinaryLength ^com.datomic.lucene.document.AbstractField field))))))
  (defn read-only-clone
    ([rdr] (.clone ^com.datomic.lucene.index.IndexReader rdr (boolean (.booleanValue true)))))
  (reset-meta!
    #'read-only-clone
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'rdr {:tag 'IndexReader})]), :column (int 1)}
      :name
      'read-only-clone
      :ns
      *ns*))
  (defn get-field
    ([doc s] (.getFieldable ^com.datomic.lucene.document.Document doc ^java.lang.String s)))
  (reset-meta!
    #'get-field
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'doc {:tag 'Document}) 's]), :column (int 1)}
      :name
      'get-field
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Readerable (index-reader [_] "Returns a reader which must be closed."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.lucene" "Readerable")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Readerable :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'index-reader
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns a reader which must be closed."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.lucene" "Readerable"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.lucene" "index-reader")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (extend
    com.datomic.lucene.store.Directory
    Readerable
    {:index-reader (fn fn__11585 ([this] (IndexReader/open this)))})
  (extend
    com.datomic.lucene.index.IndexWriter
    Readerable
    {:index-reader
     (fn fn__11587 ([this] (IndexReader/open this (boolean (.booleanValue false)))))})
  (defn multi-reader
    ([rdrs & p__11589]
      (let [map__11590 p__11589
            map__11590 (if (seq? map__11590)
                         (if (next map__11590)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__11590))
                           (if (seq map__11590) (first map__11590) {}))
                         map__11590)
            close_subreaders (get map__11590 :close-subreaders)]
        (com.datomic.lucene.index.MultiReader.
          (into-array com.datomic.lucene.index.IndexReader rdrs)
          (boolean (.booleanValue ^java.lang.Boolean close_subreaders))))))
  (reset-meta!
    #'multi-reader
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['rdrs '& {:keys ['close-subreaders]}]
           {:tag 'com.datomic.lucene.index.IndexReader})),
       :column (int 1)}
      :name
      'multi-reader
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "index-searcher")
    {:tag com.datomic.lucene.search.IndexSearcher,
     :arglists (clojure.core/list ['rdr]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "index-searcher")
    (fn index_searcher
      ([rdr]
        (com.datomic.lucene.search.IndexSearcher. ^com.datomic.lucene.index.IndexReader rdr))))
  (defn persistent-directory
    ([pdmap] (datomic.impl.lucene.DirectoryRef. ^clojure.lang.IPersistentMap pdmap)))
  (reset-meta!
    #'persistent-directory
    (assoc
      {:arglists (clojure.core/list ['pdmap]), :column (int 1)}
      :name
      'persistent-directory
      :ns
      *ns*))
  (defn search-seq
    ([searcher query & p__11595]
      (let [map__11596 p__11595
            map__11596 (if (seq? map__11596)
                         (if (next map__11596)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__11596))
                           (if (seq map__11596) (first map__11596) {}))
                         map__11596)
            max (get map__11596 :max 10000)]
        (map
          (fn fn__11597
            ([p1__11594#]
              (java.lang.Integer/valueOf
                (int (.-doc ^com.datomic.lucene.search.ScoreDoc p1__11594#)))))
          (.-scoreDocs
            (.search
              ^com.datomic.lucene.search.IndexSearcher searcher
              ^com.datomic.lucene.search.Query query
              (int max)))))))
  (reset-meta!
    #'search-seq
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'searcher {:tag 'IndexSearcher})
          (.withMeta 'query {:tag 'Query})
          '&
          {:keys ['max], :or {'max 10000}}]),
       :column (int 1)}
      :name
      'search-seq
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "document")
    {:tag com.datomic.lucene.document.Document,
     :arglists (clojure.core/list ['& 'fields]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "document")
    (fn document
      ([& fields]
        (let [d (com.datomic.lucene.document.Document.)]
          (loop [seq_11600 (seq fields) chunk_11601 nil count_11602 0 i_11603 0]
            (if (< i_11603 count_11602)
              (let [f (.nth ^clojure.lang.Indexed chunk_11601 (int i_11603))]
                (.add
                  ^com.datomic.lucene.document.Document d
                  ^com.datomic.lucene.document.Fieldable f)
                (recur seq_11600 chunk_11601 count_11602 (inc i_11603)))
              (let [temp__5825__auto__ (seq seq_11600)]
                (when temp__5825__auto__
                  (let [seq_11600 temp__5825__auto__]
                    (if (chunked-seq? seq_11600)
                      (let [c__6090__auto__ (chunk-first seq_11600)]
                        (recur
                          (chunk-rest seq_11600)
                          c__6090__auto__
                          (int (count c__6090__auto__))
                          (int 0)))
                      (let [f (first seq_11600)]
                        (.add
                          ^com.datomic.lucene.document.Document d
                          ^com.datomic.lucene.document.Fieldable f)
                        (recur (next seq_11600) nil 0 0))))))))
          d))))
  (defn add-document
    ([writer doc]
      (let [G__11607 writer]
        (.addDocument
          ^com.datomic.lucene.index.IndexWriter G__11607
          ^com.datomic.lucene.document.Document doc)
        G__11607)))
  (reset-meta!
    #'add-document
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'writer {:tag 'IndexWriter}) (.withMeta 'doc {:tag 'Document})]),
       :column (int 1)}
      :name
      'add-document
      :ns
      *ns*))
  (defn add-documents
    ([writer docs]
      (let [G__11609 writer]
        (.addDocuments ^com.datomic.lucene.index.IndexWriter G__11609 ^java.util.Collection docs)
        G__11609)))
  (reset-meta!
    #'add-documents
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'writer {:tag 'IndexWriter}) 'docs]),
       :column (int 1)}
      :name
      'add-documents
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "term-query")
    {:tag com.datomic.lucene.search.Query,
     :arglists (clojure.core/list ['field 'term]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "term-query")
    (fn term_query
      ([field term]
        (com.datomic.lucene.search.TermQuery.
          (com.datomic.lucene.index.Term. ^java.lang.String field ^java.lang.String term)))))
  (defn term-enum-seq
    ([te]
      (when (.next ^com.datomic.lucene.index.TermEnum te)
        (lazy-seq (cons (.term ^com.datomic.lucene.index.TermEnum te) (term-enum-seq te))))))
  (reset-meta!
    #'term-enum-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'te {:tag 'TermEnum})]), :column (int 1)}
      :name
      'term-enum-seq
      :ns
      *ns*))
  (defn term-from-tokenizer
    ([t]
      (.term
        (.getAttribute
          ^com.datomic.lucene.util.AttributeSource t
          com.datomic.lucene.analysis.tokenattributes.TermAttribute))))
  (reset-meta!
    #'term-from-tokenizer
    (assoc
      {:arglists (clojure.core/list [(.withMeta 't {:tag 'TokenStream})]), :column (int 1)}
      :name
      'term-from-tokenizer
      :ns
      *ns*))
  (defn tokenize-terms
    ([s]
      (let [t (.tokenStream
                (com.datomic.lucene.analysis.standard.StandardAnalyzer. Version/LUCENE_33)
                "v"
                (java.io.StringReader. ^java.lang.String s))]
        (loop [terms []]
          (if (.incrementToken ^com.datomic.lucene.analysis.TokenStream t)
            (recur (conj terms (term-from-tokenizer t)))
            terms)))))
  (reset-meta!
    #'tokenize-terms
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'tokenize-terms :ns *ns*))
  (defn escape-query ([s] (QueryParser/escape ^java.lang.String s)))
  (reset-meta!
    #'escape-query
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'escape-query :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.lucene" "parse-query")
    {:tag com.datomic.lucene.search.Query,
     :arglists (clojure.core/list ['f 's '& {:keys ['analyzer]}]),
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.lucene" "parse-query")
    (fn parse_query
      ([f s & p__11618]
        (let [map__11619 p__11618
              map__11619 (if (seq? map__11619)
                           (if (next map__11619)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__11619))
                             (if (seq map__11619) (first map__11619) {}))
                           map__11619)
              analyzer (get map__11619 :analyzer)
              analyzer (or
                         analyzer
                         (com.datomic.lucene.analysis.standard.StandardAnalyzer.
                           Version/LUCENE_33))]
          (.parse
            (com.datomic.lucene.queryParser.QueryParser.
              Version/LUCENE_33
              ^java.lang.String f
              ^com.datomic.lucene.analysis.Analyzer analyzer)
            ^java.lang.String s))))))
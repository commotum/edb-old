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
      ([& p__8957]
        (let [map__8958 p__8957
              map__8958 (if (seq? map__8958)
                          (if (next map__8958)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__8958))
                            (if (seq map__8958) (first map__8958) {}))
                          map__8958)
              version (get map__8958 :version)
              analyzer (get map__8958 :analyzer)]
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
      ([directory & p__8963]
        (let [map__8964 p__8963
              map__8964 (if (seq? map__8964)
                          (if (next map__8964)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__8964))
                            (if (seq map__8964) (first map__8964) {}))
                          map__8964)
              version (get map__8964 :version)
              analyzer_fn (get map__8964 :analyzer-fn)
              version (or version Version/LUCENE_33)
              analyzer_fn (or
                            analyzer_fn
                            (fn fn__8965
                              ([p1__8962#]
                                (com.datomic.lucene.analysis.standard.StandardAnalyzer.
                                  ^com.datomic.lucene.util.Version p1__8962#))))
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
      ([name value & p__8970]
        (let [map__8971 p__8970
              map__8971 (if (seq? map__8971)
                          (if (next map__8971)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__8971))
                            (if (seq map__8971) (first map__8971) {}))
                          map__8971)
              opts map__8971
              index (get map__8971 :index)
              store (get map__8971 :store)
              analyze (get map__8971 :analyze)
              omit_norms (get map__8971 :omit-norms)]
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
        (let [G__8973 (com.datomic.lucene.document.NumericField.
                        ^java.lang.String name
                        Field$Store/YES
                        (boolean (.booleanValue true)))]
          (.setLongValue ^com.datomic.lucene.document.NumericField G__8973 (long value))
          G__8973))))
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
  (def long-value
   (fn long_value ([f] (.getNumericValue ^com.datomic.lucene.document.NumericField f))))
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
          (loop [seq_8979 (seq (partition 2 queryoccurs)) chunk_8980 nil count_8981 0 i_8982 0]
            (if (< i_8982 count_8981)
              (let [vec__8983 (.nth ^clojure.lang.Indexed chunk_8980 (int i_8982))
                    q (nth vec__8983 (int 0) nil)
                    o (nth vec__8983 (int 1) nil)]
                (.add
                  ^com.datomic.lucene.search.BooleanQuery bq
                  ^com.datomic.lucene.search.Query q
                  (get
                    {:should BooleanClause$Occur/SHOULD,
                     :must BooleanClause$Occur/MUST,
                     :must_not BooleanClause$Occur/MUST_NOT}
                    o
                    o))
                (recur seq_8979 chunk_8980 count_8981 (inc i_8982)))
              (let [temp__5804__auto__ (seq seq_8979)]
                (when temp__5804__auto__
                  (let [seq_8979 temp__5804__auto__]
                    (if (chunked-seq? seq_8979)
                      (let [c__6065__auto__ (chunk-first seq_8979)]
                        (recur
                          (chunk-rest seq_8979)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [vec__8986 (first seq_8979)
                            q (nth vec__8986 (int 0) nil)
                            o (nth vec__8986 (int 1) nil)]
                        (.add
                          ^com.datomic.lucene.search.BooleanQuery bq
                          ^com.datomic.lucene.search.Query q
                          (get
                            {:should BooleanClause$Occur/SHOULD,
                             :must BooleanClause$Occur/MUST,
                             :must_not BooleanClause$Occur/MUST_NOT}
                            o
                            o))
                        (recur (next seq_8979) nil 0 0))))))))
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
  (def read-only-clone
   (fn read_only_clone
     ([rdr] (.clone ^com.datomic.lucene.index.IndexReader rdr (boolean (.booleanValue true))))))
  (reset-meta!
    #'read-only-clone
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'rdr {:tag 'IndexReader})]), :column (int 1)}
      :name
      'read-only-clone
      :ns
      *ns*))
  (def get-field
   (fn get_field
     ([doc s] (.getFieldable ^com.datomic.lucene.document.Document doc ^java.lang.String s))))
  (reset-meta!
    #'get-field
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'doc {:tag 'Document}) 's]), :column (int 1)}
      :name
      'get-field
      :ns
      *ns*))
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol Readerable (index-reader [_] "Returns a reader which must be closed."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.lucene" "Readerable")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'Readerable :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'index-reader
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns a reader which must be closed."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.lucene" "Readerable"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.lucene" "index-reader")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
  (extend
    com.datomic.lucene.store.Directory
    Readerable
    {:index-reader (fn fn__9011 ([this] (IndexReader/open this)))})
  (extend
    com.datomic.lucene.index.IndexWriter
    Readerable
    {:index-reader (fn fn__9013 ([this] (IndexReader/open this (boolean (.booleanValue false)))))})
  (def multi-reader
   (fn multi_reader
     ([rdrs & p__9015]
       (let [map__9016 p__9015
             map__9016 (if (seq? map__9016)
                         (if (next map__9016)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9016))
                           (if (seq map__9016) (first map__9016) {}))
                         map__9016)
             close_subreaders (get map__9016 :close-subreaders)]
         (com.datomic.lucene.index.MultiReader.
           (into-array com.datomic.lucene.index.IndexReader rdrs)
           (boolean (.booleanValue ^java.lang.Boolean close_subreaders)))))))
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
  (def search-seq
   (fn search_seq
     ([searcher query & p__9021]
       (let [map__9022 p__9021
             map__9022 (if (seq? map__9022)
                         (if (next map__9022)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9022))
                           (if (seq map__9022) (first map__9022) {}))
                         map__9022)
             max (get map__9022 :max 10000)]
         (map
           (fn fn__9023
             ([p1__9020#]
               (java.lang.Integer/valueOf
                 (int (.-doc ^com.datomic.lucene.search.ScoreDoc p1__9020#)))))
           (.-scoreDocs
             (.search
               ^com.datomic.lucene.search.IndexSearcher searcher
               ^com.datomic.lucene.search.Query query
               (int max))))))))
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
          (loop [seq_9026 (seq fields) chunk_9027 nil count_9028 0 i_9029 0]
            (if (< i_9029 count_9028)
              (let [f (.nth ^clojure.lang.Indexed chunk_9027 (int i_9029))]
                (.add
                  ^com.datomic.lucene.document.Document d
                  ^com.datomic.lucene.document.Fieldable f)
                (recur seq_9026 chunk_9027 count_9028 (inc i_9029)))
              (let [temp__5804__auto__ (seq seq_9026)]
                (when temp__5804__auto__
                  (let [seq_9026 temp__5804__auto__]
                    (if (chunked-seq? seq_9026)
                      (let [c__6065__auto__ (chunk-first seq_9026)]
                        (recur
                          (chunk-rest seq_9026)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [f (first seq_9026)]
                        (.add
                          ^com.datomic.lucene.document.Document d
                          ^com.datomic.lucene.document.Fieldable f)
                        (recur (next seq_9026) nil 0 0))))))))
          d))))
  (def add-document
   (fn add_document
     ([writer doc]
       (let [G__9033 writer]
         (.addDocument
           ^com.datomic.lucene.index.IndexWriter G__9033
           ^com.datomic.lucene.document.Document doc)
         G__9033))))
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
  (def add-documents
   (fn add_documents
     ([writer docs]
       (let [G__9035 writer]
         (.addDocuments ^com.datomic.lucene.index.IndexWriter G__9035 ^java.util.Collection docs)
         G__9035))))
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
  (def term-enum-seq
   (fn term_enum_seq
     ([te]
       (when (.next ^com.datomic.lucene.index.TermEnum te)
         (lazy-seq (cons (.term ^com.datomic.lucene.index.TermEnum te) (term-enum-seq te)))))))
  (reset-meta!
    #'term-enum-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'te {:tag 'TermEnum})]), :column (int 1)}
      :name
      'term-enum-seq
      :ns
      *ns*))
  (def term-from-tokenizer
   (fn term_from_tokenizer
     ([t]
       (.term
         (.getAttribute
           ^com.datomic.lucene.util.AttributeSource t
           com.datomic.lucene.analysis.tokenattributes.TermAttribute)))))
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
      ([f s & p__9044]
        (let [map__9045 p__9044
              map__9045 (if (seq? map__9045)
                          (if (next map__9045)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__9045))
                            (if (seq map__9045) (first map__9045) {}))
                          map__9045)
              analyzer (get map__9045 :analyzer)
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
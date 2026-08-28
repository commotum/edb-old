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
  (defn create-config
    ([& p__8957]
      (let [map__8958 p__8957
            map__8958 (if (seq? map__8958)
                        (if (next map__8958)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8958))
                          (if (seq map__8958) (first map__8958) {}))
                        map__8958)
            version (get map__8958 :version)
            analyzer (get map__8958 :analyzer)]
        (com.datomic.lucene.index.IndexWriterConfig.
          ^com.datomic.lucene.util.Version version
          ^com.datomic.lucene.analysis.Analyzer analyzer))))
  (reset-meta!
    #'create-config
    (assoc
      {:tag com.datomic.lucene.index.IndexWriterConfig,
       :private true,
       :arglists (clojure.core/list ['& {:keys ['version 'analyzer]}]),
       :column 1}
      :name
      'create-config
      :ns
      *ns*))
  (defn fs-directory ([f] (FSDirectory/open (io/file f))))
  (reset-meta!
    #'fs-directory
    (assoc
      {:tag com.datomic.lucene.store.Directory, :arglists (clojure.core/list ['f]), :column 1}
      :name
      'fs-directory
      :ns
      *ns*))
  (defn ram-directory ([] (com.datomic.lucene.store.RAMDirectory.)))
  (reset-meta!
    #'ram-directory
    (assoc
      {:tag com.datomic.lucene.store.Directory, :arglists (clojure.core/list []), :column 1}
      :name
      'ram-directory
      :ns
      *ns*))
  (defn index-writer
    ([directory & p__8963]
      (let [map__8964 p__8963
            map__8964 (if (seq? map__8964)
                        (if (next map__8964)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8964))
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
        writer)))
  (reset-meta!
    #'index-writer
    (assoc
      {:tag com.datomic.lucene.index.IndexWriter,
       :arglists
       (clojure.core/list
         [(.withMeta 'directory {:tag 'Directory}) '& {:keys ['version 'analyzer-fn]}]),
       :column 1}
      :name
      'index-writer
      :ns
      *ns*))
  (defn string-field
    ([name value & p__8970]
      (let [map__8971 p__8970
            map__8971 (if (seq? map__8971)
                        (if (next map__8971)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8971))
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
          (Field$Index/toIndex (boolean index) (boolean analyze) (boolean omit_norms))))))
  (reset-meta!
    #'string-field
    (assoc
      {:tag com.datomic.lucene.document.Field,
       :arglists
       (clojure.core/list
         [(.withMeta 'name {:tag 'String})
          (.withMeta 'value {:tag 'String})
          '&
          {:keys ['index 'store 'analyze 'omit-norms], :as 'opts}]),
       :column 1}
      :name
      'string-field
      :ns
      *ns*))
  (defn long-field
    ([name ^long value]
      (let [G__8973 (com.datomic.lucene.document.NumericField.
                      ^java.lang.String name
                      Field$Store/YES
                      (boolean (.booleanValue true)))]
        (.setLongValue ^com.datomic.lucene.document.NumericField G__8973 (long value))
        G__8973)))
  (reset-meta!
    #'long-field
    (assoc
      {:tag com.datomic.lucene.document.NumericField,
       :arglists
       (clojure.core/list [(.withMeta 'name {:tag 'String}) (.withMeta 'value {:tag 'long})]),
       :column 1}
      :name
      'long-field
      :ns
      *ns*))
  (defn binary-field
    ([name value ^long offset ^long length]
      (com.datomic.lucene.document.Field.
        ^java.lang.String name
        ^bytes value
        (int offset)
        (int length))))
  (reset-meta!
    #'binary-field
    (assoc
      {:tag com.datomic.lucene.document.Field,
       :arglists
       (clojure.core/list
         [(.withMeta 'name {:tag 'String})
          (.withMeta 'value {:tag 'bytes})
          (.withMeta 'offset {:tag 'long})
          (.withMeta 'length {:tag 'long})]),
       :column 1}
      :name
      'binary-field
      :ns
      *ns*))
  (defn string-value ([f] (.stringValue ^com.datomic.lucene.document.Field f)))
  (reset-meta!
    #'string-value
    (assoc
      {:tag java.lang.String,
       :arglists (clojure.core/list [(.withMeta 'f {:tag 'Field})]),
       :column 1}
      :name
      'string-value
      :ns
      *ns*))
  (defn long-value ([f] (.getNumericValue ^com.datomic.lucene.document.NumericField f)))
  (defn long-query
    ([f l]
      (NumericRangeQuery/newLongRange
        ^java.lang.String f
        ^java.lang.Long l
        ^java.lang.Long l
        (boolean (.booleanValue true))
        (boolean (.booleanValue true)))))
  (reset-meta!
    #'long-query
    (assoc
      {:tag com.datomic.lucene.search.Query, :arglists (clojure.core/list ['f 'l]), :column 1}
      :name
      'long-query
      :ns
      *ns*))
  (defn boolean-query
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
        bq)))
  (reset-meta!
    #'boolean-query
    (assoc
      {:tag com.datomic.lucene.search.Query,
       :arglists (clojure.core/list ['& 'queryoccurs]),
       :column 1}
      :name
      'boolean-query
      :ns
      *ns*))
  (clojure.core/import 'com.datomic.lucene.search.BooleanClause$Occur)
  (defn field-stream
    ([field]
      (java.io.ByteArrayInputStream.
        (.getBinaryValue ^com.datomic.lucene.document.AbstractField field)
        (int (.getBinaryOffset ^com.datomic.lucene.document.AbstractField field))
        (int (.getBinaryLength ^com.datomic.lucene.document.AbstractField field)))))
  (reset-meta!
    #'field-stream
    (assoc
      {:tag java.io.InputStream,
       :arglists (clojure.core/list [(.withMeta 'field {:tag 'Field})]),
       :column 1}
      :name
      'field-stream
      :ns
      *ns*))
  (defn read-only-clone
    ([rdr] (.clone ^com.datomic.lucene.index.IndexReader rdr (boolean (.booleanValue true)))))
  (defn get-field
    ([doc s] (.getFieldable ^com.datomic.lucene.document.Document doc ^java.lang.String s)))
  (defonce Readerable {})
  (defprotocol Readerable (index-reader [_]))
  (extend
    com.datomic.lucene.store.Directory
    Readerable
    {:index-reader (fn fn__9011 ([this] (IndexReader/open this)))})
  (extend
    com.datomic.lucene.index.IndexWriter
    Readerable
    {:index-reader (fn fn__9013 ([this] (IndexReader/open this (boolean (.booleanValue false)))))})
  (defn multi-reader
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
          (boolean (.booleanValue ^java.lang.Boolean close_subreaders))))))
  (defn index-searcher
    ([rdr] (com.datomic.lucene.search.IndexSearcher. ^com.datomic.lucene.index.IndexReader rdr)))
  (reset-meta!
    #'index-searcher
    (assoc
      {:tag com.datomic.lucene.search.IndexSearcher,
       :arglists (clojure.core/list ['rdr]),
       :column 1}
      :name
      'index-searcher
      :ns
      *ns*))
  (defn persistent-directory
    ([pdmap] (datomic.impl.lucene.DirectoryRef. ^clojure.lang.IPersistentMap pdmap)))
  (defn search-seq
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
              (int max)))))))
  (defn document
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
        d)))
  (reset-meta!
    #'document
    (assoc
      {:tag com.datomic.lucene.document.Document,
       :arglists (clojure.core/list ['& 'fields]),
       :column 1}
      :name
      'document
      :ns
      *ns*))
  (defn add-document
    ([writer doc]
      (let [G__9033 writer]
        (.addDocument
          ^com.datomic.lucene.index.IndexWriter G__9033
          ^com.datomic.lucene.document.Document doc)
        G__9033)))
  (defn add-documents
    ([writer docs]
      (let [G__9035 writer]
        (.addDocuments ^com.datomic.lucene.index.IndexWriter G__9035 ^java.util.Collection docs)
        G__9035)))
  (defn term-query
    ([field term]
      (com.datomic.lucene.search.TermQuery.
        (com.datomic.lucene.index.Term. ^java.lang.String field ^java.lang.String term))))
  (reset-meta!
    #'term-query
    (assoc
      {:tag com.datomic.lucene.search.Query,
       :arglists (clojure.core/list ['field 'term]),
       :column 1}
      :name
      'term-query
      :ns
      *ns*))
  (defn term-enum-seq
    ([te]
      (when (.next ^com.datomic.lucene.index.TermEnum te)
        (lazy-seq (cons (.term ^com.datomic.lucene.index.TermEnum te) (term-enum-seq te))))))
  (defn term-from-tokenizer
    ([t]
      (.term
        (.getAttribute
          ^com.datomic.lucene.util.AttributeSource t
          com.datomic.lucene.analysis.tokenattributes.TermAttribute))))
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
  (defn escape-query ([s] (QueryParser/escape ^java.lang.String s)))
  (defn parse-query
    ([f s & p__9044]
      (let [map__9045 p__9044
            map__9045 (if (seq? map__9045)
                        (if (next map__9045)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__9045))
                          (if (seq map__9045) (first map__9045) {}))
                        map__9045)
            analyzer (get map__9045 :analyzer)
            analyzer (or
                       analyzer
                       (com.datomic.lucene.analysis.standard.StandardAnalyzer. Version/LUCENE_33))]
        (.parse
          (com.datomic.lucene.queryParser.QueryParser.
            Version/LUCENE_33
            ^java.lang.String f
            ^com.datomic.lucene.analysis.Analyzer analyzer)
          ^java.lang.String s))))
  (reset-meta!
    #'parse-query
    (assoc
      {:tag com.datomic.lucene.search.Query,
       :arglists (clojure.core/list ['f 's '& {:keys ['analyzer]}]),
       :column 1}
      :name
      'parse-query
      :ns
      *ns*)))
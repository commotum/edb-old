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
    (throw (java.lang.Error. "This version of Datomic requires Lucene 3.x"))
    nil)
  (defn create-config
    ([& p__12232]
      (let [map__12233 p__12232
            map__12233 (if (seq? map__12233)
                         (clojure.lang.PersistentHashMap/create (seq map__12233))
                         map__12233)
            version (get map__12233 :version)
            analyzer (get map__12233 :analyzer)]
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
    ([directory & p__12238]
      (let [map__12239 p__12238
            map__12239 (if (seq? map__12239)
                         (clojure.lang.PersistentHashMap/create (seq map__12239))
                         map__12239)
            version (get map__12239 :version)
            analyzer_fn (get map__12239 :analyzer-fn)
            version (or version Version/LUCENE_33)
            analyzer_fn (or
                          analyzer_fn
                          (fn fn__12240
                            ([p1__12237#]
                              (com.datomic.lucene.analysis.standard.StandardAnalyzer.
                                ^com.datomic.lucene.util.Version p1__12237#))))
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
    ([name value & p__12245]
      (let [map__12246 p__12245
            map__12246 (if (seq? map__12246)
                         (clojure.lang.PersistentHashMap/create (seq map__12246))
                         map__12246)
            opts map__12246
            index (get map__12246 :index)
            store (get map__12246 :store)
            analyze (get map__12246 :analyze)
            omit_norms (get map__12246 :omit-norms)]
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
      (let [G__12248 (com.datomic.lucene.document.NumericField.
                       ^java.lang.String name
                       Field$Store/YES
                       (boolean (.booleanValue true)))]
        (.setLongValue ^com.datomic.lucene.document.NumericField G__12248 (long value))
        G__12248)))
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
        (loop [seq_12254 (seq (partition 2 queryoccurs)) chunk_12255 nil count_12256 0 i_12257 0]
          (if (< i_12257 count_12256)
            (let [vec__12258 (.nth ^clojure.lang.Indexed chunk_12255 (int i_12257))
                  q (nth vec__12258 (int 0) nil)
                  o (nth vec__12258 (int 1) nil)]
              (.add
                ^com.datomic.lucene.search.BooleanQuery bq
                ^com.datomic.lucene.search.Query q
                (get
                  {:should BooleanClause$Occur/SHOULD,
                   :must BooleanClause$Occur/MUST,
                   :must_not BooleanClause$Occur/MUST_NOT}
                  o
                  o))
              (recur seq_12254 chunk_12255 count_12256 (inc i_12257)))
            (let [temp__5457__auto__ (seq seq_12254)]
              (when temp__5457__auto__
                (let [seq_12254 temp__5457__auto__]
                  (if (chunked-seq? seq_12254)
                    (let [c__5719__auto__ (chunk-first seq_12254)]
                      (recur
                        (chunk-rest seq_12254)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [vec__12261 (first seq_12254)
                          q (nth vec__12261 (int 0) nil)
                          o (nth vec__12261 (int 1) nil)]
                      (.add
                        ^com.datomic.lucene.search.BooleanQuery bq
                        ^com.datomic.lucene.search.Query q
                        (get
                          {:should BooleanClause$Occur/SHOULD,
                           :must BooleanClause$Occur/MUST,
                           :must_not BooleanClause$Occur/MUST_NOT}
                          o
                          o))
                      (recur (next seq_12254) nil 0 0))))))))
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
    {:index-reader (fn fn__12286 ([this] (IndexReader/open this)))})
  (extend
    com.datomic.lucene.index.IndexWriter
    Readerable
    {:index-reader
     (fn fn__12288 ([this] (IndexReader/open this (boolean (.booleanValue false)))))})
  (defn multi-reader
    ([rdrs & p__12290]
      (let [map__12291 p__12290
            map__12291 (if (seq? map__12291)
                         (clojure.lang.PersistentHashMap/create (seq map__12291))
                         map__12291)
            close_subreaders (get map__12291 :close-subreaders)]
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
    ([searcher query & p__12296]
      (let [map__12297 p__12296
            map__12297 (if (seq? map__12297)
                         (clojure.lang.PersistentHashMap/create (seq map__12297))
                         map__12297)
            max (get map__12297 :max 10000)]
        (map
          (fn fn__12298
            ([p1__12295#]
              (java.lang.Integer/valueOf
                (int (.-doc ^com.datomic.lucene.search.ScoreDoc p1__12295#)))))
          (.-scoreDocs
            (.search
              ^com.datomic.lucene.search.IndexSearcher searcher
              ^com.datomic.lucene.search.Query query
              (int max)))))))
  (defn document
    ([& fields]
      (let [d (com.datomic.lucene.document.Document.)]
        (loop [seq_12301 (seq fields) chunk_12302 nil count_12303 0 i_12304 0]
          (if (< i_12304 count_12303)
            (let [f (.nth ^clojure.lang.Indexed chunk_12302 (int i_12304))]
              (.add
                ^com.datomic.lucene.document.Document d
                ^com.datomic.lucene.document.Fieldable f)
              (recur seq_12301 chunk_12302 count_12303 (inc i_12304)))
            (let [temp__5457__auto__ (seq seq_12301)]
              (when temp__5457__auto__
                (let [seq_12301 temp__5457__auto__]
                  (if (chunked-seq? seq_12301)
                    (let [c__5719__auto__ (chunk-first seq_12301)]
                      (recur
                        (chunk-rest seq_12301)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [f (first seq_12301)]
                      (.add
                        ^com.datomic.lucene.document.Document d
                        ^com.datomic.lucene.document.Fieldable f)
                      (recur (next seq_12301) nil 0 0))))))))
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
      (let [G__12308 writer]
        (.addDocument
          ^com.datomic.lucene.index.IndexWriter G__12308
          ^com.datomic.lucene.document.Document doc)
        G__12308)))
  (defn add-documents
    ([writer docs]
      (let [G__12310 writer]
        (.addDocuments ^com.datomic.lucene.index.IndexWriter G__12310 ^java.util.Collection docs)
        G__12310)))
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
    ([f s & p__12319]
      (let [map__12320 p__12319
            map__12320 (if (seq? map__12320)
                         (clojure.lang.PersistentHashMap/create (seq map__12320))
                         map__12320)
            analyzer (get map__12320 :analyzer)
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
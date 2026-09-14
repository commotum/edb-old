(do
  (clojure.core/in-ns 'datomic.external-sort-datoms)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.external-sort-datoms)
    {:doc
     "Datom-specific spill format for disk-backed sorting during background indexing. Datoms are written to temporary Fressian runs, ordered and merged by datomic.external-sort, and decoded as an iterator for a caller-supplied handler. The handler must consume the iterator before returning because the underlying streams are then closed and the temporary runs deleted."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.external-sort :as 'es]
        'datomic.iter
        ['datomic.fressian :as 'fress]
        ['datomic.memory-size :as 'ms])
      (clojure.core/import 'datomic.db.Datum)
      (clojure.core/import 'datomic.db.IDatumImpl)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'java.io.EOFException)
      (clojure.core/import 'org.fressian.Reader)
      (clojure.core/import 'org.fressian.Writer)
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)))
  (when-not (.equals 'datomic.external-sort-datoms 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.external-sort-datoms))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.external-sort :as 'es]
          'datomic.iter
          ['datomic.fressian :as 'fress]
          ['datomic.memory-size :as 'ms])
        (clojure.core/import 'datomic.db.Datum)
        (clojure.core/import 'datomic.db.IDatumImpl)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'java.io.EOFException)
        (clojure.core/import 'org.fressian.Reader)
        (clojure.core/import 'org.fressian.Writer)
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'org.fressian.handlers.WriteHandler))))
  (set! *warn-on-reflection* true)
  (defn create-file-writer
    ([output-stream handlers]
      (let [writer (fress/create-writer output-stream handlers)]
        (fn fn__13564 ([datom] (.writeObject ^org.fressian.Writer writer datom))))))
  (reset-meta!
    #'create-file-writer
    (assoc
      {:arglists (clojure.core/list ['output-stream 'handlers]),
       :doc "Returns a function that writes one datom at a time to a Fressian sort run.",
       :column (int 1)}
      :name
      'create-file-writer
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-write-handlers")
    {:doc
     "Fressian handlers for temporary datom runs. Each datum records assertion state, partition, entity index, attribute id, value, and transaction t in that order.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-write-handlers")
    (merge
      fress/user-write-handlers
      {datomic.db.Datum
       {"datum"
        (reify
          org.fressian.handlers.WriteHandler
          (^void write
            [this ^org.fressian.Writer writer value]
            (do
              (let [datum value]
                (.writeTag ^org.fressian.Writer writer "datum" (int 6))
                (.writeBoolean
                  ^org.fressian.Writer writer
                  (boolean (.isAssertion ^datomic.impl.db.IDatum datum)))
                (.writeObject
                  ^org.fressian.Writer writer
                  (java.lang.Integer/valueOf (int (.getP ^datomic.impl.db.IDatum datum)))
                  (boolean (.booleanValue false)))
                (.writeObject
                  ^org.fressian.Writer writer
                  (long (.eidx ^datomic.db.IDatumImpl datum))
                  (boolean (.booleanValue false)))
                (.writeInt
                  ^org.fressian.Writer writer
                  (long (.getA ^datomic.impl.db.IDatum datum)))
                (.writeObject ^org.fressian.Writer writer (.getV ^datomic.impl.db.IDatum datum))
                (.writeObject
                  ^org.fressian.Writer writer
                  (long (.getT ^datomic.impl.db.IDatum datum))
                  (boolean (.booleanValue false))))
              nil)))}}))
  (.setMeta
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-read-handlers")
    {:doc
     "Fressian handlers that reconstruct asserting and retracting datoms from temporary sort runs.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-read-handlers")
    (merge
      fress/user-read-handlers
      {"datum"
       (reify
         org.fressian.handlers.ReadHandler
         (read
           [this ^org.fressian.Reader reader tag ^int component-count]
           (let [assert? (.readBoolean ^org.fressian.Reader reader)
                 partition (.readInt ^org.fressian.Reader reader)
                 entity-index (.readInt ^org.fressian.Reader reader)
                 entity-id (db/make-eid partition entity-index)
                 attribute-id (.readInt ^org.fressian.Reader reader)
                 value (.readObject ^org.fressian.Reader reader)
                 t (.readInt ^org.fressian.Reader reader)]
             (if assert?
               (db/asserting-datum entity-id attribute-id value t)
               (db/retracting-datum entity-id attribute-id value t)))))}))
  (defn consume-sorted-datoms
    ([datoms {:keys [cmp dir max-chunk-size prog-fn]} handler]
      (es/consume-iter
        (es/file-system-sorter
          {:max-chunk-size max-chunk-size,
           :item-sizer ms/memory-size,
           :io (es/temp-file-io dir),
           :prog-fn prog-fn,
           :cmp cmp,
           :file-iter-fn
           (fn fn__13575 ([input-stream] (fress/reader-iter input-stream datom-read-handlers))),
           :create-file-writer-fn
           (fn fn__13577
             ([output-stream] (create-file-writer output-stream datom-write-handlers)))}
          datoms)
        handler)))
  (reset-meta!
    #'consume-sorted-datoms
    (assoc
      {:arglists (clojure.core/list ['datoms {:keys ['cmp 'dir 'max-chunk-size 'prog-fn]} 'handler]),
       :doc
       "Externally sorts datoms according to cmp and calls handler with the resulting iterator, returning the handler result. max-chunk-size bounds each in-memory run using estimated datom memory size; dir holds temporary Fressian runs; prog-fn receives file progress events. The iterator is valid only during the handler call, and nil is supplied for empty input.",
       :column (int 1)}
      :name
      'consume-sorted-datoms
      :ns
      *ns*)))

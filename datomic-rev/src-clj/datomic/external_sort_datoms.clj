(do
  (clojure.core/in-ns 'datomic.external-sort-datoms)
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
    ([os handlers]
      (let [writer (fress/create-writer os handlers)]
        (fn fn__14502 ([o] (.writeObject ^org.fressian.Writer writer o))))))
  (def datom-write-handlers
   (merge
     fress/user-write-handlers
     {datomic.db.Datum
      {"datum"
       (reify
         org.fressian.handlers.WriteHandler
         (^void write
           [this ^org.fressian.Writer w o]
           (do
             (let [datum o]
               (.writeTag ^org.fressian.Writer w "datum" (int 6))
               (.writeBoolean
                 ^org.fressian.Writer w
                 (boolean (.isAssertion ^datomic.impl.db.IDatum datum)))
               (.writeObject
                 ^org.fressian.Writer w
                 (java.lang.Integer/valueOf (int (.getP ^datomic.impl.db.IDatum datum)))
                 (boolean (.booleanValue false)))
               (.writeObject
                 ^org.fressian.Writer w
                 (long (.eidx ^datomic.db.IDatumImpl datum))
                 (boolean (.booleanValue false)))
               (.writeInt ^org.fressian.Writer w (long (.getA ^datomic.impl.db.IDatum datum)))
               (.writeObject ^org.fressian.Writer w (.getV ^datomic.impl.db.IDatum datum))
               (.writeObject
                 ^org.fressian.Writer w
                 (long (.getT ^datomic.impl.db.IDatum datum))
                 (boolean (.booleanValue false))))
             nil)))}}))
  (def datom-read-handlers
   (merge
     fress/user-read-handlers
     {"datum"
      (reify
        org.fressian.handlers.ReadHandler
        (read
          [this ^org.fressian.Reader rdr tag ^int component_count]
          (let [assert? (.readBoolean ^org.fressian.Reader rdr)
                part (.readInt ^org.fressian.Reader rdr)
                eidx (.readInt ^org.fressian.Reader rdr)
                eid (db/make-eid part eidx)
                attrid (.readInt ^org.fressian.Reader rdr)
                v (.readObject ^org.fressian.Reader rdr)
                t (.readInt ^org.fressian.Reader rdr)]
            (if assert?
              (db/asserting-datum eid attrid v t)
              (db/retracting-datum eid attrid v t)))))}))
  (defn consume-sorted-datoms
    ([iter p__14511 handler]
      (let [map__14512 p__14511
            map__14512 (if (seq? map__14512)
                         (clojure.lang.PersistentHashMap/create (seq map__14512))
                         map__14512)
            cmp (get map__14512 :cmp)
            dir (get map__14512 :dir)
            max_chunk_size (get map__14512 :max-chunk-size)
            prog_fn (get map__14512 :prog-fn)]
        (es/consume-iter
          (es/file-system-sorter
            {:max-chunk-size max_chunk_size,
             :item-sizer ms/memory-size,
             :io (es/temp-file-io dir),
             :prog-fn prog_fn,
             :cmp cmp,
             :file-iter-fn
             (fn fn__14513 ([p1__14509#] (fress/reader-iter p1__14509# datom-read-handlers))),
             :create-file-writer-fn
             (fn fn__14515 ([p1__14510#] (create-file-writer p1__14510# datom-write-handlers)))}
            iter)
          handler)))))
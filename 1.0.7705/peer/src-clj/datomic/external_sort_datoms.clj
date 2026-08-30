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
        (fn fn__13564 ([o] (.writeObject ^org.fressian.Writer writer o))))))
  (reset-meta!
    #'create-file-writer
    (assoc
      {:arglists (clojure.core/list ['os 'handlers]), :column (int 1)}
      :name
      'create-file-writer
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-write-handlers")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-write-handlers")
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
  (.setMeta
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-read-handlers")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.external-sort-datoms" "datom-read-handlers")
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
    ([iter p__13573 handler]
      (let [map__13574 p__13573
            map__13574 (if (seq? map__13574)
                         (if (next map__13574)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__13574))
                           (if (seq map__13574) (first map__13574) {}))
                         map__13574)
            cmp (get map__13574 :cmp)
            dir (get map__13574 :dir)
            max_chunk_size (get map__13574 :max-chunk-size)
            prog_fn (get map__13574 :prog-fn)]
        (es/consume-iter
          (es/file-system-sorter
            {:max-chunk-size max_chunk_size,
             :item-sizer ms/memory-size,
             :io (es/temp-file-io dir),
             :prog-fn prog_fn,
             :cmp cmp,
             :file-iter-fn
             (fn fn__13575 ([p1__13571#] (fress/reader-iter p1__13571# datom-read-handlers))),
             :create-file-writer-fn
             (fn fn__13577 ([p1__13572#] (create-file-writer p1__13572# datom-write-handlers)))}
            iter)
          handler))))
  (reset-meta!
    #'consume-sorted-datoms
    (assoc
      {:arglists (clojure.core/list ['iter {:keys ['cmp 'dir 'max-chunk-size 'prog-fn]} 'handler]),
       :column (int 1)}
      :name
      'consume-sorted-datoms
      :ns
      *ns*)))
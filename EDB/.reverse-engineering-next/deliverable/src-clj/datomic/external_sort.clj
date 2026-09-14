(do
  (clojure.core/in-ns 'datomic.external-sort)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'compare))
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['datomic.common :as 'common :refer (clojure.core/list 'compare)]
        ['datomic.config :as 'config]
        ['datomic.iter :as 'iter])
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'datomic.iter.Iter)))
  (when-not (.equals 'datomic.external-sort 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.external-sort))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'compare))
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['datomic.common :as 'common :refer (clojure.core/list 'compare)]
          ['datomic.config :as 'config]
          ['datomic.iter :as 'iter])
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'datomic.iter.Iter))))
  (set! *warn-on-reflection* true)
  (defonce IO {})
  (defprotocol IO (make-temp-file [_]) (temp-file-size [_ f]) (delete-temp-file [_ fname]))
  (defn temp-file-io
    ([dir]
      (reify
        datomic.external_sort.IO
        (delete-temp-file [this f] (.delete ^java.io.File f))
        (temp-file-size [this f] (long (.length ^java.io.File f)))
        (make-temp-file
          [this]
          (java.io.File/createTempFile (str (java.util.UUID/randomUUID)) "" (io/file dir))))))
  (defn next-chunk
    ([max_size sizer iter]
      (when iter
        (let [a (java.util.ArrayList.)]
          (loop [size 0 iter iter]
            (let [item (.get ^datomic.iter.Iter iter)
                  size (long (+ size (^clojure.lang.IFn sizer item)))]
              (.add ^java.util.ArrayList a item)
              (if (< size max_size)
                (let [temp__5455__auto__ (.next ^datomic.iter.Iter iter)]
                  (if temp__5455__auto__
                    (let [inext temp__5455__auto__] (recur size inext))
                    [a nil]))
                [a (.next ^datomic.iter.Iter iter)]))))
        nil)))
  (reset-meta!
    #'next-chunk
    (assoc
      {:private true,
       :arglists (clojure.core/list ['max-size 'sizer (.withMeta 'iter {:tag 'Iter})]),
       :column 1}
      :name
      'next-chunk
      :ns
      *ns*))
  (defn chunk-seq
    ([max_size sizer iter]
      (map
        first
        (take-while
          identity
          (iterate
            (fn fn__14404
              ([p__14403]
                (let [vec__14405 p__14403
                      chunk (nth vec__14405 (int 0) nil)
                      more (nth vec__14405 (int 1) nil)]
                  (when more (next-chunk max_size sizer more)))))
            (next-chunk max_size sizer iter))))))
  (defonce ExternalSort {})
  (defprotocol
    ExternalSort
    (consume-iter [_ handler])
    (consume-files-iter [_ files handler])
    (merge-step [_]))
  (def MAX_MERGE 4)
  (reset-meta! #'MAX_MERGE (assoc {:const true, :column 1} :name 'MAX_MERGE :ns *ns*))
  (deftype
    FileSystemSorter
    [pool cmp io file_iter_fn create_file_writer_fn prog_fn ^{:unsynchronized-mutable true} files]
    datomic.external_sort.ExternalSort
    (merge-step
      [this]
      (common/pooled-mapv
        pool
        (fn fn__14469
          ([infiles]
            (let [outfile (make-temp-file io)]
              (let [os (io/output-stream outfile)]
                (try
                  (let [write (^clojure.lang.IFn create_file_writer_fn os)]
                    (^clojure.lang.IFn prog_fn {:before outfile})
                    (consume-files-iter
                      this
                      infiles
                      (fn fn__14471
                        ([p1__14458#]
                          (iter/reduce
                            (fn fn__14472 ([_ item] (^clojure.lang.IFn write item)))
                            nil
                            p1__14458#))))
                    (^clojure.lang.IFn prog_fn
                      {:after outfile, :size (temp-file-size io outfile)}))
                  (finally (do (.close ^java.io.OutputStream os) nil))))
              outfile)))
        (partition-all 4 files)))
    (consume-iter
      [this handler]
      (do
        (loop []
          (if (<= (count files) 4)
            (consume-files-iter this files handler)
            (do (set! files (merge-step this)) (recur))))
        nil))
    (consume-files-iter
      [this infiles handler]
      (let [istreams (mapv io/input-stream infiles) iters (mapv file_iter_fn istreams)]
        (try
          (let [G__14460 (count iters)]
            (case
              G__14460
              0
              (^clojure.lang.IFn handler nil)
              1
              (^clojure.lang.IFn handler (^clojure.lang.IFn iters 0))
              (^clojure.lang.IFn handler (apply iter/merge-iters cmp iters))))
          (finally
            (do
              (loop [seq_14461 (seq istreams) chunk_14462 nil count_14463 0 i_14464 0]
                (if (< i_14464 count_14463)
                  (let [i (.nth ^clojure.lang.Indexed chunk_14462 (int i_14464))]
                    (.close ^java.io.InputStream i)
                    (recur seq_14461 chunk_14462 count_14463 (inc i_14464)))
                  (let [temp__5457__auto__ (seq seq_14461)]
                    (when temp__5457__auto__
                      (let [seq_14461 temp__5457__auto__]
                        (if (chunked-seq? seq_14461)
                          (let [c__5719__auto__ (chunk-first seq_14461)]
                            (recur
                              (chunk-rest seq_14461)
                              c__5719__auto__
                              (int (count c__5719__auto__))
                              (int 0)))
                          (let [i (first seq_14461)]
                            (.close ^java.io.InputStream i)
                            (recur (next seq_14461) nil 0 0))))))))
              (loop [seq_14465 (seq infiles) chunk_14466 nil count_14467 0 i_14468 0]
                (if (< i_14468 count_14467)
                  (let [f (.nth ^clojure.lang.Indexed chunk_14466 (int i_14468))]
                    (delete-temp-file io f)
                    (recur seq_14465 chunk_14466 count_14467 (inc i_14468)))
                  (let [temp__5457__auto__ (seq seq_14465)]
                    (when temp__5457__auto__
                      (let [seq_14465 temp__5457__auto__]
                        (if (chunked-seq? seq_14465)
                          (let [c__5719__auto__ (chunk-first seq_14465)]
                            (recur
                              (chunk-rest seq_14465)
                              c__5719__auto__
                              (int (count c__5719__auto__))
                              (int 0)))
                          (let [f (first seq_14465)]
                            (delete-temp-file io f)
                            (recur (next seq_14465) nil 0 0))))))))))))))
  (clojure.core/import 'datomic.external_sort.FileSystemSorter)
  (defn ->FileSystemSorter
    ([pool cmp io file_iter_fn create_file_writer_fn prog_fn files]
      (datomic.external_sort.FileSystemSorter.
        pool
        cmp
        io
        file_iter_fn
        create_file_writer_fn
        prog_fn
        files)))
  (defn file-system-sorter
    ([p__14491 iter]
      (let [map__14492 p__14491
            map__14492 (if (seq? map__14492)
                         (clojure.lang.PersistentHashMap/create (seq map__14492))
                         map__14492)
            max_chunk_size (get map__14492 :max-chunk-size)
            item_sizer (get map__14492 :item-sizer)
            io (get map__14492 :io)
            cmp (get map__14492 :cmp common/compare)
            prog_fn (get map__14492 :prog-fn (constantly nil))
            file_iter_fn (get map__14492 :file-iter-fn)
            create_file_writer_fn (get map__14492 :create-file-writer-fn)
            threads (get map__14492 :threads (config/property "datomic.externalSortPool"))
            files (into
                    []
                    (map
                      (fn fn__14493
                        ([chunk]
                          (let [f (make-temp-file io)]
                            (let [os (io/output-stream f)]
                              (try
                                (let [write (^clojure.lang.IFn create_file_writer_fn os)]
                                  (^clojure.lang.IFn prog_fn
                                    {:before f,
                                     :count (java.lang.Integer/valueOf (int (count chunk)))})
                                  (reduce
                                    (fn fn__14495
                                      ([p1__14490# p2__14489#]
                                        (^clojure.lang.IFn write p2__14489#)))
                                    nil
                                    (if cmp (sort cmp chunk) (sort chunk)))
                                  (^clojure.lang.IFn prog_fn
                                    {:after f,
                                     :count (java.lang.Integer/valueOf (int (count chunk))),
                                     :size (temp-file-size io f)}))
                                (finally (do (.close ^java.io.OutputStream os) nil))))
                            f)))
                      (chunk-seq max_chunk_size item_sizer iter)))
            idx (atom 0)
            pool (common/thread-pool {:nthreads threads, :name "external-sort"})]
        (datomic.external_sort.FileSystemSorter.
          pool
          cmp
          io
          file_iter_fn
          create_file_writer_fn
          prog_fn
          files)))))
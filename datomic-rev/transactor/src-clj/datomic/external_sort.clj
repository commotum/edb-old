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
                (let [temp__5802__auto__ (.next ^datomic.iter.Iter iter)]
                  (if temp__5802__auto__
                    (let [inext temp__5802__auto__] (recur size inext))
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
            (fn fn__12344
              ([p__12343]
                (let [vec__12345 p__12343
                      chunk (nth vec__12345 (int 0) nil)
                      more (nth vec__12345 (int 1) nil)]
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
        (fn fn__12409
          ([infiles]
            (let [outfile (make-temp-file io)]
              (with-open [os (io/output-stream outfile)]
                (let [write (^clojure.lang.IFn create_file_writer_fn os)]
                  (^clojure.lang.IFn prog_fn {:before outfile})
                  (consume-files-iter
                    this
                    infiles
                    (fn fn__12411
                      ([p1__12398#]
                        (iter/reduce
                          (fn fn__12412 ([_ item] (^clojure.lang.IFn write item)))
                          nil
                          p1__12398#))))
                  (^clojure.lang.IFn prog_fn {:after outfile, :size (temp-file-size io outfile)})))
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
          (let [G__12400 (count iters)]
            (case
              G__12400
              0
              (^clojure.lang.IFn handler nil)
              1
              (^clojure.lang.IFn handler (^clojure.lang.IFn iters 0))
              (^clojure.lang.IFn handler (apply iter/merge-iters cmp iters))))
          (finally
            (do
              (loop [seq_12401 (seq istreams) chunk_12402 nil count_12403 0 i_12404 0]
                (if (< i_12404 count_12403)
                  (let [i (.nth ^clojure.lang.Indexed chunk_12402 (int i_12404))]
                    (.close ^java.io.InputStream i)
                    (recur seq_12401 chunk_12402 count_12403 (inc i_12404)))
                  (let [temp__5804__auto__ (seq seq_12401)]
                    (when temp__5804__auto__
                      (let [seq_12401 temp__5804__auto__]
                        (if (chunked-seq? seq_12401)
                          (let [c__6065__auto__ (chunk-first seq_12401)]
                            (recur
                              (chunk-rest seq_12401)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [i (first seq_12401)]
                            (.close ^java.io.InputStream i)
                            (recur (next seq_12401) nil 0 0))))))))
              (loop [seq_12405 (seq infiles) chunk_12406 nil count_12407 0 i_12408 0]
                (if (< i_12408 count_12407)
                  (let [f (.nth ^clojure.lang.Indexed chunk_12406 (int i_12408))]
                    (delete-temp-file io f)
                    (recur seq_12405 chunk_12406 count_12407 (inc i_12408)))
                  (let [temp__5804__auto__ (seq seq_12405)]
                    (when temp__5804__auto__
                      (let [seq_12405 temp__5804__auto__]
                        (if (chunked-seq? seq_12405)
                          (let [c__6065__auto__ (chunk-first seq_12405)]
                            (recur
                              (chunk-rest seq_12405)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [f (first seq_12405)]
                            (delete-temp-file io f)
                            (recur (next seq_12405) nil 0 0))))))))))))))
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
    ([p__12431 iter]
      (let [map__12432 p__12431
            map__12432 (if (seq? map__12432)
                         (if (next map__12432)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__12432))
                           (if (seq map__12432) (first map__12432) {}))
                         map__12432)
            max_chunk_size (get map__12432 :max-chunk-size)
            item_sizer (get map__12432 :item-sizer)
            io (get map__12432 :io)
            cmp (get map__12432 :cmp common/compare)
            prog_fn (get map__12432 :prog-fn (constantly nil))
            file_iter_fn (get map__12432 :file-iter-fn)
            create_file_writer_fn (get map__12432 :create-file-writer-fn)
            threads (get map__12432 :threads (config/property "datomic.externalSortPool"))
            files (into
                    []
                    (map
                      (fn fn__12433
                        ([chunk]
                          (let [f (make-temp-file io)]
                            (with-open [os (io/output-stream f)]
                              (let [write (^clojure.lang.IFn create_file_writer_fn os)]
                                (^clojure.lang.IFn prog_fn
                                  {:before f,
                                   :count (java.lang.Integer/valueOf (int (count chunk)))})
                                (reduce
                                  (fn fn__12435
                                    ([p1__12430# p2__12429#] (^clojure.lang.IFn write p2__12429#)))
                                  nil
                                  (if cmp (sort cmp chunk) (sort chunk)))
                                (^clojure.lang.IFn prog_fn
                                  {:after f,
                                   :count (java.lang.Integer/valueOf (int (count chunk))),
                                   :size (temp-file-size io f)})))
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
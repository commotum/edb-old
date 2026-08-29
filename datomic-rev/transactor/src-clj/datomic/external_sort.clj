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
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol
      IO
      (make-temp-file [_] "Returns something that can be opened by io/input-stream")
      (temp-file-size [_ f] "Returns the size of the created file")
      (delete-temp-file [_ fname] "Deletes thing created by make-temp-file"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.external-sort" "IO")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'IO :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'make-temp-file
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Returns something that can be opened by io/input-stream"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "make-temp-file")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*)))
    (let [protocol_signature__7423 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'temp-file-size
                                        {:arglists (clojure.core/list ['_ 'f])}),
                                      :arglists (clojure.core/list ['_ 'f]),
                                      :doc "Returns the size of the created file"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7424 (with-meta
                                       (:name protocol_signature__7423)
                                       protocol_signature__7423)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "temp-file-size")
        (assoc protocol_signature__7423 :name protocol_method_name__7424 :ns *ns*)))
    (let [protocol_signature__7425 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete-temp-file
                                        {:arglists (clojure.core/list ['_ 'fname])}),
                                      :arglists (clojure.core/list ['_ 'fname]),
                                      :doc "Deletes thing created by make-temp-file"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7426 (with-meta
                                       (:name protocol_signature__7425)
                                       protocol_signature__7425)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "delete-temp-file")
        (assoc protocol_signature__7425 :name protocol_method_name__7426 :ns *ns*))))
  (defn temp-file-io
    ([dir]
      (reify
        datomic.external_sort.IO
        (delete-temp-file [this f] (.delete ^java.io.File f))
        (temp-file-size [this f] (long (.length ^java.io.File f)))
        (make-temp-file
          [this]
          (java.io.File/createTempFile (str (java.util.UUID/randomUUID)) "" (io/file dir))))))
  (reset-meta!
    #'temp-file-io
    (assoc {:arglists (clojure.core/list ['dir]), :column (int 1)} :name 'temp-file-io :ns *ns*))
  (def next-chunk
   (fn next_chunk
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
                 [a (.next ^datomic.iter.Iter iter)]))))))))
  (reset-meta!
    #'next-chunk
    (assoc
      {:private true,
       :arglists (clojure.core/list ['max-size 'sizer (.withMeta 'iter {:tag 'Iter})]),
       :column (int 1)}
      :name
      'next-chunk
      :ns
      *ns*))
  (def chunk-seq
   (fn chunk_seq
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
             (next-chunk max_size sizer iter)))))))
  (reset-meta!
    #'chunk-seq
    (assoc
      {:arglists (clojure.core/list ['max-size 'sizer 'iter]), :column (int 1)}
      :name
      'chunk-seq
      :ns
      *ns*))
  (let [protocol_metadata__7427 {:column (int 1)}]
    (defprotocol
      ExternalSort
      (consume-iter [_ handler] "Call hanlder with final result")
      (consume-files-iter [_ files handler] "Call handler with iterator over merged files")
      (merge-step [_] "Returns coll of new files made in this step"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.external-sort" "ExternalSort")
      (assoc (assoc protocol_metadata__7427 :doc nil) :name 'ExternalSort :ns *ns*))
    (let [protocol_signature__7428 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'consume-iter
                                        {:arglists (clojure.core/list ['_ 'handler])}),
                                      :arglists (clojure.core/list ['_ 'handler]),
                                      :doc "Call hanlder with final result"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7429 (with-meta
                                       (:name protocol_signature__7428)
                                       protocol_signature__7428)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "consume-iter")
        (assoc protocol_signature__7428 :name protocol_method_name__7429 :ns *ns*)))
    (let [protocol_signature__7430 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'consume-files-iter
                                        {:arglists (clojure.core/list ['_ 'files 'handler])}),
                                      :arglists (clojure.core/list ['_ 'files 'handler]),
                                      :doc "Call handler with iterator over merged files"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7431 (with-meta
                                       (:name protocol_signature__7430)
                                       protocol_signature__7430)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "consume-files-iter")
        (assoc protocol_signature__7430 :name protocol_method_name__7431 :ns *ns*)))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'merge-step {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Returns coll of new files made in this step"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "merge-step")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (def MAX_MERGE 4)
  (reset-meta! #'MAX_MERGE (assoc {:const true, :column (int 1)} :name 'MAX_MERGE :ns *ns*))
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
      (loop []
        (if (<= (count files) 4)
          (consume-files-iter this files handler)
          (do (set! files (merge-step this)) (recur)))))
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
  (def ->FileSystemSorter
   (fn __GT_FileSystemSorter
     ([pool cmp io file_iter_fn create_file_writer_fn prog_fn files]
       (datomic.external_sort.FileSystemSorter.
         pool
         cmp
         io
         file_iter_fn
         create_file_writer_fn
         prog_fn
         files))))
  (reset-meta!
    #'->FileSystemSorter
    (assoc
      {:arglists
       (clojure.core/list ['pool 'cmp 'io 'file-iter-fn 'create-file-writer-fn 'prog-fn 'files]),
       :column (int 1)}
      :name
      '->FileSystemSorter
      :ns
      *ns*))
  (def file-system-sorter
   (fn file_system_sorter
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
                                     ([p1__12430# p2__12429#]
                                       (^clojure.lang.IFn write p2__12429#)))
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
  (reset-meta!
    #'file-system-sorter
    (assoc
      {:arglists
       (clojure.core/list
         [{:keys
           ['max-chunk-size
            'item-sizer
            'io
            'cmp
            'prog-fn
            'file-iter-fn
            'create-file-writer-fn
            'threads],
           :or
           {'cmp 'compare,
            'prog-fn (.withMeta (clojure.core/list 'constantly nil) {:column (int 30)}),
            'threads
            (.withMeta
              (clojure.core/list 'config/property "datomic.externalSortPool")
              {:column (int 55)})}}
          'iter]),
       :column (int 1)}
      :name
      'file-system-sorter
      :ns
      *ns*)))
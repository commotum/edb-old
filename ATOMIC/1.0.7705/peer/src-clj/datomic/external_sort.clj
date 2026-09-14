(do
  (clojure.core/in-ns 'datomic.external-sort)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.external-sort)
    {:doc
     "Disk-backed merge sorting for iterators that may exceed available memory. Background indexing uses this boundary to divide input into size-bounded runs, sort each run, spill it to a temporary file, and merge at most four runs per pass. The final ordered iterator is passed to a handler while its input streams remain open; those streams are closed and the consumed temporary files are deleted when the handler returns or throws."})
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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      IO
      (make-temp-file [_] "Creates a new sort-run target that can be opened as an input or output stream.")
      (temp-file-size [_ temp-file] "Returns the size in bytes of a sort-run target.")
      (delete-temp-file [_ temp-file] "Deletes a sort-run target created by make-temp-file."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.external-sort" "IO")
      (assoc
        (assoc
          protocol_metadata__7463
          :doc
          "Temporary storage operations required by the external sorter.")
        :name
        'IO
        :ns
        *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'make-temp-file
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Creates a new sort-run target that can be opened as an input or output stream."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "make-temp-file")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'temp-file-size
                                        {:arglists (clojure.core/list ['_ 'temp-file])}),
                                      :arglists (clojure.core/list ['_ 'temp-file]),
                                      :doc "Returns the size in bytes of a sort-run target."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "temp-file-size")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*)))
    (let [protocol_signature__7468 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'delete-temp-file
                                        {:arglists (clojure.core/list ['_ 'temp-file])}),
                                      :arglists (clojure.core/list ['_ 'temp-file]),
                                      :doc "Deletes a sort-run target created by make-temp-file."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "delete-temp-file")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*))))
  (defn temp-file-io
    ([directory]
      (reify
        datomic.external_sort.IO
        (delete-temp-file [this temp-file] (.delete ^java.io.File temp-file))
        (temp-file-size [this temp-file] (long (.length ^java.io.File temp-file)))
        (make-temp-file
          [this]
          (java.io.File/createTempFile
            (str (java.util.UUID/randomUUID))
            ""
            (io/file directory))))))
  (reset-meta!
    #'temp-file-io
    (assoc
      {:arglists (clojure.core/list ['directory]),
       :doc "Returns a filesystem IO implementation that creates sort-run files in directory.",
       :column (int 1)}
      :name
      'temp-file-io
      :ns
      *ns*))
  (defn next-chunk
    ([max-size item-sizer iter]
      (when iter
        (let [items (java.util.ArrayList.)]
          (loop [size 0 iter iter]
            (let [item (.get ^datomic.iter.Iter iter)
                  size (long (+ size (^clojure.lang.IFn item-sizer item)))]
              (.add ^java.util.ArrayList items item)
              (if (< size max-size)
                (let [temp__5802__auto__ (.next ^datomic.iter.Iter iter)]
                  (if temp__5802__auto__
                    (let [next-iter temp__5802__auto__] (recur size next-iter))
                    [items nil]))
                [items (.next ^datomic.iter.Iter iter)])))))))
  (reset-meta!
    #'next-chunk
    (assoc
      {:private true,
       :arglists (clojure.core/list ['max-size 'item-sizer (.withMeta 'iter {:tag 'Iter})]),
       :doc
       "Consumes one nonempty run from iter and returns [items remaining-iter]. The run includes the item that reaches or exceeds max-size, so a single oversized item still forms a run.",
       :column (int 1)}
      :name
      'next-chunk
      :ns
      *ns*))
  (defn chunk-seq
    ([max-size item-sizer iter]
      (map
        first
        (take-while
          identity
          (iterate
            (fn fn__13466
              ([p__13465]
                (let [vec__13467 p__13465
                      chunk (nth vec__13467 (int 0) nil)
                      more (nth vec__13467 (int 1) nil)]
                  (when more (next-chunk max-size item-sizer more)))))
            (next-chunk max-size item-sizer iter))))))
  (reset-meta!
    #'chunk-seq
    (assoc
      {:arglists (clojure.core/list ['max-size 'item-sizer 'iter]),
       :doc
       "Returns the input iterator as an ordered sequence of nonempty in-memory runs bounded by item-sizer estimates and max-size.",
       :column (int 1)}
      :name
      'chunk-seq
      :ns
      *ns*))
  (let [protocol_metadata__7470 {:column (int 1)}]
    (defprotocol
      ExternalSort
      (consume-iter
        [_ handler]
        "Calls handler with the final ordered iterator. The iterator is valid only for the duration of the call; its streams are then closed and its temporary files deleted.")
      (consume-files-iter
        [_ files handler]
        "Calls handler with the ordered merge of files, then closes every input stream and deletes every consumed file, including when handler throws.")
      (merge-step
        [_]
        "Merges each group of at most MAX_MERGE current runs into a new run and returns the new run files."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.external-sort" "ExternalSort")
      (assoc
        (assoc
          protocol_metadata__7470
          :doc
          "Consumption and merge operations for a disk-backed external sort.")
        :name
        'ExternalSort
        :ns
        *ns*))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'consume-iter
                                        {:arglists (clojure.core/list ['_ 'handler])}),
                                      :arglists (clojure.core/list ['_ 'handler]),
                                      :doc
                                      "Calls handler with the final ordered iterator. The iterator is valid only for the duration of the call; its streams are then closed and its temporary files deleted."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7472 (with-meta
                                       (:name protocol_signature__7471)
                                       protocol_signature__7471)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "consume-iter")
        (assoc protocol_signature__7471 :name protocol_method_name__7472 :ns *ns*)))
    (let [protocol_signature__7473 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'consume-files-iter
                                        {:arglists (clojure.core/list ['_ 'files 'handler])}),
                                      :arglists (clojure.core/list ['_ 'files 'handler]),
                                      :doc
                                      "Calls handler with the ordered merge of files, then closes every input stream and deletes every consumed file, including when handler throws."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7474 (with-meta
                                       (:name protocol_signature__7473)
                                       protocol_signature__7473)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "consume-files-iter")
        (assoc protocol_signature__7473 :name protocol_method_name__7474 :ns *ns*)))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'merge-step {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Merges each group of at most MAX_MERGE current runs into a new run and returns the new run files."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "merge-step")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*))))
  (def MAX_MERGE 4)
  (reset-meta!
    #'MAX_MERGE
    (assoc
      {:const true, :doc "Maximum number of sorted runs merged into one run per pass.", :column (int 1)}
      :name
      'MAX_MERGE
      :ns
      *ns*))
  (deftype
    FileSystemSorter
    [pool cmp io file-iter-fn create-file-writer-fn prog-fn ^{:unsynchronized-mutable true} files]
    datomic.external_sort.ExternalSort
    (merge-step
      [this]
      (common/pooled-mapv
        pool
        (fn fn__13531
          ([infiles]
            (let [outfile (make-temp-file io)]
              (with-open [os (io/output-stream outfile)]
                (let [write (^clojure.lang.IFn create-file-writer-fn os)]
                  (^clojure.lang.IFn prog-fn {:before outfile})
                  (consume-files-iter
                    this
                    infiles
                    (fn fn__13533
                      ([p1__13520#]
                        (iter/reduce
                          (fn fn__13534 ([_ item] (^clojure.lang.IFn write item)))
                          nil
                          p1__13520#))))
                  (^clojure.lang.IFn prog-fn {:after outfile, :size (temp-file-size io outfile)})))
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
      (let [istreams (mapv io/input-stream infiles) iters (mapv file-iter-fn istreams)]
        (try
          (let [G__13522 (count iters)]
            (case
              G__13522
              0
              (^clojure.lang.IFn handler nil)
              1
              (^clojure.lang.IFn handler (^clojure.lang.IFn iters 0))
              (^clojure.lang.IFn handler (apply iter/merge-iters cmp iters))))
          (finally
            (do
              (loop [seq_13523 (seq istreams) chunk_13524 nil count_13525 0 i_13526 0]
                (if (< i_13526 count_13525)
                  (let [i (.nth ^clojure.lang.Indexed chunk_13524 (int i_13526))]
                    (.close ^java.io.InputStream i)
                    (recur seq_13523 chunk_13524 count_13525 (inc i_13526)))
                  (let [temp__5804__auto__ (seq seq_13523)]
                    (when temp__5804__auto__
                      (let [seq_13523 temp__5804__auto__]
                        (if (chunked-seq? seq_13523)
                          (let [c__6065__auto__ (chunk-first seq_13523)]
                            (recur
                              (chunk-rest seq_13523)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [i (first seq_13523)]
                            (.close ^java.io.InputStream i)
                            (recur (next seq_13523) nil 0 0))))))))
              (loop [seq_13527 (seq infiles) chunk_13528 nil count_13529 0 i_13530 0]
                (if (< i_13530 count_13529)
                  (let [f (.nth ^clojure.lang.Indexed chunk_13528 (int i_13530))]
                    (delete-temp-file io f)
                    (recur seq_13527 chunk_13528 count_13529 (inc i_13530)))
                  (let [temp__5804__auto__ (seq seq_13527)]
                    (when temp__5804__auto__
                      (let [seq_13527 temp__5804__auto__]
                        (if (chunked-seq? seq_13527)
                          (let [c__6065__auto__ (chunk-first seq_13527)]
                            (recur
                              (chunk-rest seq_13527)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [f (first seq_13527)]
                            (delete-temp-file io f)
                            (recur (next seq_13527) nil 0 0))))))))))))))
  (clojure.core/import 'datomic.external_sort.FileSystemSorter)
  (defn ->FileSystemSorter
    ([pool cmp io file-iter-fn create-file-writer-fn prog-fn files]
      (datomic.external_sort.FileSystemSorter.
        pool
        cmp
        io
        file-iter-fn
        create-file-writer-fn
        prog-fn
        files)))
  (reset-meta!
    #'->FileSystemSorter
    (assoc
      {:arglists
       (clojure.core/list ['pool 'cmp 'io 'file-iter-fn 'create-file-writer-fn 'prog-fn 'files]),
       :doc "Low-level constructor for a sorter over existing sorted run files.",
       :column (int 1)}
      :name
      '->FileSystemSorter
      :ns
      *ns*))
  (defn file-system-sorter
    ([{:keys [max-chunk-size item-sizer io cmp prog-fn file-iter-fn create-file-writer-fn threads],
       :or
       {cmp compare,
        prog-fn (constantly nil),
        threads (config/property "datomic.externalSortPool")}}
      iter]
      (let [files (into
                    []
                    (map
                      (fn fn__13555
                        ([chunk]
                          (let [f (make-temp-file io)]
                            (with-open [os (io/output-stream f)]
                              (let [write (^clojure.lang.IFn create-file-writer-fn os)]
                                (^clojure.lang.IFn prog-fn
                                  {:before f,
                                   :count (java.lang.Integer/valueOf (int (count chunk)))})
                                (reduce
                                  (fn fn__13557
                                    ([p1__13552# p2__13551#] (^clojure.lang.IFn write p2__13551#)))
                                  nil
                                  (if cmp (sort cmp chunk) (sort chunk)))
                                (^clojure.lang.IFn prog-fn
                                  {:after f,
                                   :count (java.lang.Integer/valueOf (int (count chunk))),
                                   :size (temp-file-size io f)})))
                            f)))
                      (chunk-seq max-chunk-size item-sizer iter)))
            idx (atom 0)
            pool (common/thread-pool {:nthreads threads, :name "external-sort"})]
        (datomic.external_sort.FileSystemSorter.
          pool
          cmp
          io
          file-iter-fn
          create-file-writer-fn
          prog-fn
          files))))
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
       :doc
       "Spills iter into sorted temporary runs and returns an ExternalSort. max-chunk-size is an estimated in-memory byte limit computed with item-sizer; cmp defines both run and merge order; file-iter-fn and create-file-writer-fn define the run format; prog-fn receives :before and :after file events; threads controls parallel merge work.",
       :column (int 1)}
      :name
      'file-system-sorter
      :ns
      *ns*)))

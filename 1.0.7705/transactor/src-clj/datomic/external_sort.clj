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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      IO
      (make-temp-file [_] "Returns something that can be opened by io/input-stream")
      (temp-file-size [_ f] "Returns the size of the created file")
      (delete-temp-file [_ fname] "Deletes thing created by make-temp-file"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.external-sort" "IO")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'IO :ns *ns*))
    (let [protocol_signature__7464 (assoc
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
                                        {:arglists (clojure.core/list ['_ 'f])}),
                                      :arglists (clojure.core/list ['_ 'f]),
                                      :doc "Returns the size of the created file"}
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
                                        {:arglists (clojure.core/list ['_ 'fname])}),
                                      :arglists (clojure.core/list ['_ 'fname]),
                                      :doc "Deletes thing created by make-temp-file"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "IO"))
          protocol_method_name__7469 (with-meta
                                       (:name protocol_signature__7468)
                                       protocol_signature__7468)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "delete-temp-file")
        (assoc protocol_signature__7468 :name protocol_method_name__7469 :ns *ns*))))
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
  (defn next-chunk
    ([max_size sizer iter]
      (when iter
        (let [a (java.util.ArrayList.)]
          (loop [size 0 iter iter]
            (let [item (.get ^datomic.iter.Iter iter)
                  size (long (+ size (^clojure.lang.IFn sizer item)))]
              (.add ^java.util.ArrayList a item)
              (if (< size max_size)
                (let [temp__5823__auto__ (.next ^datomic.iter.Iter iter)]
                  (if temp__5823__auto__
                    (let [inext temp__5823__auto__] (recur size inext))
                    [a nil]))
                [a (.next ^datomic.iter.Iter iter)])))))))
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
  (defn chunk-seq
    ([max_size sizer iter]
      (map
        first
        (take-while
          identity
          (iterate
            (fn fn__13546
              ([p__13545]
                (let [vec__13547 p__13545
                      chunk (nth vec__13547 (int 0) nil)
                      more (nth vec__13547 (int 1) nil)]
                  (when more (next-chunk max_size sizer more)))))
            (next-chunk max_size sizer iter))))))
  (reset-meta!
    #'chunk-seq
    (assoc
      {:arglists (clojure.core/list ['max-size 'sizer 'iter]), :column (int 1)}
      :name
      'chunk-seq
      :ns
      *ns*))
  (let [protocol_metadata__7470 {:column (int 1)}]
    (defprotocol
      ExternalSort
      (consume-iter [_ handler] "Call hanlder with final result")
      (consume-files-iter [_ files handler] "Call handler with iterator over merged files")
      (merge-step [_] "Returns coll of new files made in this step"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.external-sort" "ExternalSort")
      (assoc (assoc protocol_metadata__7470 :doc nil) :name 'ExternalSort :ns *ns*))
    (let [protocol_signature__7471 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'consume-iter
                                        {:arglists (clojure.core/list ['_ 'handler])}),
                                      :arglists (clojure.core/list ['_ 'handler]),
                                      :doc "Call hanlder with final result"}
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
                                      :doc "Call handler with iterator over merged files"}
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
                                      :doc "Returns coll of new files made in this step"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.external-sort" "ExternalSort"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.external-sort" "merge-step")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*))))
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
        (fn fn__13611
          ([infiles]
            (let [outfile (make-temp-file io)]
              (with-open [os (io/output-stream outfile)]
                (let [write (^clojure.lang.IFn create_file_writer_fn os)]
                  (^clojure.lang.IFn prog_fn {:before outfile})
                  (consume-files-iter
                    this
                    infiles
                    (fn fn__13613
                      ([p1__13600#]
                        (iter/reduce
                          (fn fn__13614 ([_ item] (^clojure.lang.IFn write item)))
                          nil
                          p1__13600#))))
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
          (let [G__13602 (count iters)]
            (case
              G__13602
              0
              (^clojure.lang.IFn handler nil)
              1
              (^clojure.lang.IFn handler (^clojure.lang.IFn iters 0))
              (^clojure.lang.IFn handler (apply iter/merge-iters cmp iters))))
          (finally
            (do
              (loop [seq_13603 (seq istreams) chunk_13604 nil count_13605 0 i_13606 0]
                (if (< i_13606 count_13605)
                  (let [i (.nth ^clojure.lang.Indexed chunk_13604 (int i_13606))]
                    (.close ^java.io.InputStream i)
                    (recur seq_13603 chunk_13604 count_13605 (inc i_13606)))
                  (let [temp__5825__auto__ (seq seq_13603)]
                    (when temp__5825__auto__
                      (let [seq_13603 temp__5825__auto__]
                        (if (chunked-seq? seq_13603)
                          (let [c__6090__auto__ (chunk-first seq_13603)]
                            (recur
                              (chunk-rest seq_13603)
                              c__6090__auto__
                              (int (count c__6090__auto__))
                              (int 0)))
                          (let [i (first seq_13603)]
                            (.close ^java.io.InputStream i)
                            (recur (next seq_13603) nil 0 0))))))))
              (loop [seq_13607 (seq infiles) chunk_13608 nil count_13609 0 i_13610 0]
                (if (< i_13610 count_13609)
                  (let [f (.nth ^clojure.lang.Indexed chunk_13608 (int i_13610))]
                    (delete-temp-file io f)
                    (recur seq_13607 chunk_13608 count_13609 (inc i_13610)))
                  (let [temp__5825__auto__ (seq seq_13607)]
                    (when temp__5825__auto__
                      (let [seq_13607 temp__5825__auto__]
                        (if (chunked-seq? seq_13607)
                          (let [c__6090__auto__ (chunk-first seq_13607)]
                            (recur
                              (chunk-rest seq_13607)
                              c__6090__auto__
                              (int (count c__6090__auto__))
                              (int 0)))
                          (let [f (first seq_13607)]
                            (delete-temp-file io f)
                            (recur (next seq_13607) nil 0 0))))))))))))))
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
  (defn file-system-sorter
    ([p__13633 iter]
      (let [map__13634 p__13633
            map__13634 (if (seq? map__13634)
                         (if (next map__13634)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__13634))
                           (if (seq map__13634) (first map__13634) {}))
                         map__13634)
            max_chunk_size (get map__13634 :max-chunk-size)
            item_sizer (get map__13634 :item-sizer)
            io (get map__13634 :io)
            cmp (get map__13634 :cmp common/compare)
            prog_fn (get map__13634 :prog-fn (constantly nil))
            file_iter_fn (get map__13634 :file-iter-fn)
            create_file_writer_fn (get map__13634 :create-file-writer-fn)
            threads (get map__13634 :threads (config/property "datomic.externalSortPool"))
            files (into
                    []
                    (map
                      (fn fn__13635
                        ([chunk]
                          (let [f (make-temp-file io)]
                            (with-open [os (io/output-stream f)]
                              (let [write (^clojure.lang.IFn create_file_writer_fn os)]
                                (^clojure.lang.IFn prog_fn
                                  {:before f,
                                   :count (java.lang.Integer/valueOf (int (count chunk)))})
                                (reduce
                                  (fn fn__13637
                                    ([p1__13632# p2__13631#] (^clojure.lang.IFn write p2__13631#)))
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
       :column (int 1)}
      :name
      'file-system-sorter
      :ns
      *ns*)))
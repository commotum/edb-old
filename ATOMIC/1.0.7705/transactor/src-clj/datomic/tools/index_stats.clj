(do
  (clojure.core/in-ns 'datomic.tools.index-stats)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.memory-size :as 'size]
        ['datomic.math :as 'math]
        ['datomic.stats :as 'stats]
        ['datomic.tools :as 'tools])))
  (when-not (.equals 'datomic.tools.index-stats 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.index-stats))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.memory-size :as 'size]
          ['datomic.math :as 'math]
          ['datomic.stats :as 'stats]
          ['datomic.tools :as 'tools]))))
  (defn summarize-keys
    ([index]
      (map
        (fn fn__28313
          ([p__28312]
            (let [map__28314 p__28312
                  map__28314 (if (seq? map__28314)
                               (if (next map__28314)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__28314))
                                 (if (seq map__28314) (first map__28314) {}))
                               map__28314)
                  key (get map__28314 :key)
                  seg (get map__28314 :seg)
                  count (get map__28314 :count)]
              {:count count,
               :key-size (long (size/memory-size key)),
               :attr-id (:a key),
               :id seg})))
        index)))
  (reset-meta!
    #'summarize-keys
    (assoc
      {:arglists (clojure.core/list ['index]), :column (int 1)}
      :name
      'summarize-keys
      :ns
      *ns*))
  (defn get-a ([iselem] (:a (:key iselem))))
  (reset-meta!
    #'get-a
    (assoc
      {:private true, :arglists (clojure.core/list ['iselem]), :column (int 1)}
      :name
      'get-a
      :ns
      *ns*))
  (defn key-means
    ([index]
      (let [leaves (count index)
            datoms (apply + (map :count index))
            ms (math/mean-and-stddev (map :count index))]
        (assoc
          (stats/key-summary index)
          :attr-id
          (get-a (first index))
          :leaves
          (java.lang.Integer/valueOf (int leaves))
          :datoms
          datoms
          :leaf-count-mean
          (long (:mean ms))))))
  (reset-meta!
    #'key-means
    (assoc {:arglists (clojure.core/list ['index]), :column (int 1)} :name 'key-means :ns *ns*))
  (defn -main*
    ([uri]
      (let [db (tools/index-db (tools/connection-resources uri))]
        (into
          {}
          (let [iter__6398__auto__ (fn iter__28319
                                     ([s__28320]
                                       (lazy-seq
                                         (loop [s__28320 s__28320]
                                           (let [temp__5825__auto__ (seq s__28320)]
                                             (when temp__5825__auto__
                                               (let [xs__6385__auto__ temp__5825__auto__
                                                     index (first xs__6385__auto__)
                                                     iterys__6394__auto__ (fn
                                                                            iter__28321
                                                                            ([s__28322]
                                                                              (lazy-seq
                                                                                (let
                                                                                  [s__28322
                                                                                   s__28322
                                                                                   temp__5825__auto__
                                                                                   (seq s__28322)]
                                                                                  (when
                                                                                    temp__5825__auto__
                                                                                    (let
                                                                                      [s__28322
                                                                                       temp__5825__auto__]
                                                                                      (if
                                                                                        (chunked-seq?
                                                                                          s__28322)
                                                                                        (let
                                                                                          [c__6396__auto__
                                                                                           (chunk-first
                                                                                             s__28322)
                                                                                           size__6397__auto__
                                                                                           (int
                                                                                             (count
                                                                                               c__6396__auto__))
                                                                                           b__28324
                                                                                           (chunk-buffer
                                                                                             (java.lang.Integer/valueOf
                                                                                               (int
                                                                                                 size__6397__auto__)))]
                                                                                          (if
                                                                                            (loop
                                                                                              [i__28323
                                                                                               (int
                                                                                                 0)]
                                                                                              (if
                                                                                                (<
                                                                                                  i__28323
                                                                                                  size__6397__auto__)
                                                                                                (let
                                                                                                  [tier
                                                                                                   (.nth
                                                                                                     ^clojure.lang.Indexed c__6396__auto__
                                                                                                     (int
                                                                                                       i__28323))]
                                                                                                  (chunk-append
                                                                                                    b__28324
                                                                                                    [[index
                                                                                                      tier]
                                                                                                     (let
                                                                                                       [leaves
                                                                                                        (^clojure.lang.IFn index
                                                                                                          (^clojure.lang.IFn tier
                                                                                                            db))]
                                                                                                       {:attrs
                                                                                                        (into
                                                                                                          {}
                                                                                                          (map
                                                                                                            (fn
                                                                                                              fn__28330
                                                                                                              ([a_leaves]
                                                                                                                [(get-a
                                                                                                                   (first
                                                                                                                     a_leaves))
                                                                                                                 (key-means
                                                                                                                   a_leaves)])))
                                                                                                          (partition-by
                                                                                                            get-a
                                                                                                            leaves))})])
                                                                                                  (recur
                                                                                                    (inc
                                                                                                      i__28323)))
                                                                                                true))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__28324)
                                                                                              (^clojure.lang.IFn iter__28321
                                                                                                (chunk-rest
                                                                                                  s__28322)))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__28324)
                                                                                              nil)))
                                                                                        (let
                                                                                          [tier
                                                                                           (first
                                                                                             s__28322)]
                                                                                          (cons
                                                                                            [[index
                                                                                              tier]
                                                                                             (let
                                                                                               [leaves
                                                                                                (^clojure.lang.IFn index
                                                                                                  (^clojure.lang.IFn tier
                                                                                                    db))]
                                                                                               {:attrs
                                                                                                (into
                                                                                                  {}
                                                                                                  (map
                                                                                                    (fn
                                                                                                      fn__28333
                                                                                                      ([a_leaves]
                                                                                                        [(get-a
                                                                                                           (first
                                                                                                             a_leaves))
                                                                                                         (key-means
                                                                                                           a_leaves)])))
                                                                                                  (partition-by
                                                                                                    get-a
                                                                                                    leaves))})]
                                                                                            (^clojure.lang.IFn iter__28321
                                                                                              (rest
                                                                                                s__28322)))))))))))
                                                     fs__6395__auto__ (seq
                                                                        (^clojure.lang.IFn iterys__6394__auto__
                                                                          [:index
                                                                           :mid-index
                                                                           :history]))]
                                                 (if fs__6395__auto__
                                                   (concat
                                                     fs__6395__auto__
                                                     (^clojure.lang.IFn iter__28319
                                                       (rest s__28320)))
                                                   (recur (rest s__28320))))))))))]
            (^clojure.lang.IFn iter__6398__auto__ [:avet :aevt]))))))
  (reset-meta!
    #'-main*
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main* :ns *ns*))
  (defn -main ([uri] (try (pp/pprint (-main* uri)) (finally (shutdown-agents)))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main :ns *ns*)))
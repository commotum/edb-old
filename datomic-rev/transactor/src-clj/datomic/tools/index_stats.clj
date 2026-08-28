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
        (fn fn__33071
          ([p__33070]
            (let [map__33072 p__33070
                  map__33072 (if (seq? map__33072)
                               (if (next map__33072)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__33072))
                                 (if (seq map__33072) (first map__33072) {}))
                               map__33072)
                  key (get map__33072 :key)
                  seg (get map__33072 :seg)
                  count (get map__33072 :count)]
              {:count count, :key-size (size/memory-size key), :attr-id (:a key), :id seg})))
        index)))
  (defn get-a ([iselem] (:a (:key iselem))))
  (reset-meta!
    #'get-a
    (assoc
      {:private true, :arglists (clojure.core/list ['iselem]), :column 1}
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
  (defn -main*
    ([uri]
      (let [db (tools/index-db (tools/connection-resources uri))]
        (into
          {}
          (let [iter__6373__auto__ (fn iter__33077
                                     ([s__33078]
                                       (lazy-seq
                                         (loop [s__33078 s__33078]
                                           (let [temp__5804__auto__ (seq s__33078)]
                                             (when temp__5804__auto__
                                               (let [xs__6360__auto__ temp__5804__auto__
                                                     index (first xs__6360__auto__)
                                                     iterys__6369__auto__ (fn 
                                                                            iter__33079
                                                                            ([s__33080]
                                                                              (lazy-seq
                                                                                (let 
                                                                                  [s__33080
                                                                                   s__33080
                                                                                   temp__5804__auto__
                                                                                   (seq s__33080)]
                                                                                  (when
                                                                                    temp__5804__auto__
                                                                                    (let 
                                                                                      [s__33080
                                                                                       temp__5804__auto__]
                                                                                      (if
                                                                                        (chunked-seq?
                                                                                          s__33080)
                                                                                        (let 
                                                                                          [c__6371__auto__
                                                                                           (chunk-first
                                                                                             s__33080)
                                                                                           size__6372__auto__
                                                                                           (int
                                                                                             (count
                                                                                               c__6371__auto__))
                                                                                           b__33082
                                                                                           (chunk-buffer
                                                                                             (java.lang.Integer/valueOf
                                                                                               (int
                                                                                                 size__6372__auto__)))]
                                                                                          (if
                                                                                            (loop 
                                                                                              [i__33081
                                                                                               (int
                                                                                                 0)]
                                                                                              (if
                                                                                                (<
                                                                                                  i__33081
                                                                                                  size__6372__auto__)
                                                                                                (let 
                                                                                                  [tier
                                                                                                   (.nth
                                                                                                     ^clojure.lang.Indexed c__6371__auto__
                                                                                                     (int
                                                                                                       i__33081))]
                                                                                                  (chunk-append
                                                                                                    b__33082
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
                                                                                                              fn__33088
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
                                                                                                      i__33081)))
                                                                                                true))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__33082)
                                                                                              (^clojure.lang.IFn iter__33079
                                                                                                (chunk-rest
                                                                                                  s__33080)))
                                                                                            (chunk-cons
                                                                                              (chunk
                                                                                                b__33082)
                                                                                              nil)))
                                                                                        (let 
                                                                                          [tier
                                                                                           (first
                                                                                             s__33080)]
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
                                                                                                      fn__33091
                                                                                                      ([a_leaves]
                                                                                                        [(get-a
                                                                                                           (first
                                                                                                             a_leaves))
                                                                                                         (key-means
                                                                                                           a_leaves)])))
                                                                                                  (partition-by
                                                                                                    get-a
                                                                                                    leaves))})]
                                                                                            (^clojure.lang.IFn iter__33079
                                                                                              (rest
                                                                                                s__33080)))))))))))
                                                     fs__6370__auto__ (seq
                                                                        (^clojure.lang.IFn iterys__6369__auto__
                                                                          [:index
                                                                           :mid-index
                                                                           :history]))]
                                                 (if fs__6370__auto__
                                                   (concat
                                                     fs__6370__auto__
                                                     (^clojure.lang.IFn iter__33077
                                                       (rest s__33078)))
                                                   (recur (rest s__33078))))))))))]
            (^clojure.lang.IFn iter__6373__auto__ [:avet :aevt]))))))
  (defn -main ([uri] (try (pp/pprint (-main* uri)) (finally (shutdown-agents))))))
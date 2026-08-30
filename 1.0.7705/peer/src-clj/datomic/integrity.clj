(do
  (clojure.core/in-ns 'datomic.integrity)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'assert))
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.api :as 'd]
        ['datomic.api :as 'd]
        ['datomic.assert :refer (clojure.core/list 'assert)]
        ['datomic.btset :as 'btset]
        ['datomic.cache :as 'cache]
        ['datomic.cli :as 'cli]
        ['datomic.cluster :as 'cluster]
        ['datomic.clusterfs :as 'clusterfs]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.coordination :as 'coord]
        ['datomic.db :as 'db]
        ['datomic.domain :as 'domain]
        ['datomic.excise :as 'excise]
        ['datomic.fressian :as 'fressian]
        ['datomic.fulltext :as 'fulltext]
        ['datomic.index :as 'index]
        ['datomic.io :as 'dio]
        ['datomic.iter :as 'iter]
        ['datomic.kv-store :as 'kvs]
        ['datomic.log :as 'log]
        ['datomic.math :as 'math]
        ['datomic.memory-size :as 'size]
        ['datomic.peer :as 'peer]
        ['datomic.garbage :as 'garbage]
        ['datomic.uri :as 'uri]
        ['datomic.slf4j :as 'logger]
        ['datomic.stats :as 'stats]
        ['datomic.tools :as 'tools]
        ['datomic.tools.index-checks :as 'ic])
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.index.RootNode)
      (clojure.core/import 'datomic.index.DirNode)
      (clojure.core/import 'datomic.fulltext.Root)
      (clojure.core/import 'datomic.clusterfs.ClusterFS)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'datomic.btset.IDataSet)
      (clojure.core/import 'java.util.Comparator)))
  (when-not (.equals 'datomic.integrity 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.integrity))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude (clojure.core/list 'assert))
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.api :as 'd]
          ['datomic.api :as 'd]
          ['datomic.assert :refer (clojure.core/list 'assert)]
          ['datomic.btset :as 'btset]
          ['datomic.cache :as 'cache]
          ['datomic.cli :as 'cli]
          ['datomic.cluster :as 'cluster]
          ['datomic.clusterfs :as 'clusterfs]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.coordination :as 'coord]
          ['datomic.db :as 'db]
          ['datomic.domain :as 'domain]
          ['datomic.excise :as 'excise]
          ['datomic.fressian :as 'fressian]
          ['datomic.fulltext :as 'fulltext]
          ['datomic.index :as 'index]
          ['datomic.io :as 'dio]
          ['datomic.iter :as 'iter]
          ['datomic.kv-store :as 'kvs]
          ['datomic.log :as 'log]
          ['datomic.math :as 'math]
          ['datomic.memory-size :as 'size]
          ['datomic.peer :as 'peer]
          ['datomic.garbage :as 'garbage]
          ['datomic.uri :as 'uri]
          ['datomic.slf4j :as 'logger]
          ['datomic.stats :as 'stats]
          ['datomic.tools :as 'tools]
          ['datomic.tools.index-checks :as 'ic])
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.index.RootNode)
        (clojure.core/import 'datomic.index.DirNode)
        (clojure.core/import 'datomic.fulltext.Root)
        (clojure.core/import 'datomic.clusterfs.ClusterFS)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'datomic.btset.IDataSet)
        (clojure.core/import 'java.util.Comparator))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.integrity" "enhance-uri") {:column (int 1)})
  (let [v__5792__auto__ #'enhance-uri]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5792__auto__)
                (instance? clojure.lang.MultiFn (deref v__5792__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.integrity" "enhance-uri") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.integrity" "enhance-uri")
        (clojure.lang.MultiFn.
          "enhance-uri"
          (fn fn__21968 ([uri] (:protocol (uri/parse uri))))
          :default
          #'clojure.core/global-hierarchy))
      #'enhance-uri))
  (defmethod enhance-uri :default fn__21973 ([uri] uri))
  (defmethod
    enhance-uri
    :ddb+s3
    fn__21975
    ([uri] (uri/create (merge (uri/parse uri) {:skip-efs true}))))
  (defn progress-reduce
    ([f val p__21977 coll]
      (let [map__21978 p__21977
            map__21978 (if (seq? map__21978)
                         (if (next map__21978)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21978))
                           (if (seq map__21978) (first map__21978) {}))
                         map__21978)
            progress (get map__21978 :progress)
            n (get map__21978 :n)]
        (if progress
          (let [pf (fn pf
                     ([p__21979 item]
                       (let [vec__21981 p__21979
                             c (nth vec__21981 (int 0) nil)
                             acc (nth vec__21981 (int 1) nil)]
                         (when (zero? (mod c n)) (^clojure.lang.IFn progress acc n))
                         [(inc c) (^clojure.lang.IFn f acc item)])))]
            (second (reduce pf [0 val] coll)))
          (reduce f val coll)))))
  (reset-meta!
    #'progress-reduce
    (assoc
      {:arglists (clojure.core/list ['f 'val {:keys ['progress 'n]} 'coll]), :column (int 1)}
      :name
      'progress-reduce
      :ns
      *ns*))
  (defn progress-dot ([& _] (print ".") (flush)))
  (reset-meta!
    #'progress-dot
    (assoc {:arglists (clojure.core/list ['& '_]), :column (int 1)} :name 'progress-dot :ns *ns*))
  (defn cauterize
    ([db & ks]
      (merge
        db
        (select-keys
          {:index nil, :history nil, :indexing nil, :mid-index nil, :memidx db/mem-index-set}
          ks))))
  (reset-meta!
    #'cauterize
    (assoc
      {:arglists (clojure.core/list ['db '& 'ks]), :column (int 1)}
      :name
      'cauterize
      :ns
      *ns*))
  (defn nohistory-attrs
    ([db]
      (reduce
        conj
        #{}
        (map
          :id
          (filter
            (fn fn__21989 ([p1__21988#] (.-noHistory ^datomic.db.Attribute p1__21988#)))
            (filter (partial instance? datomic.db.Attribute) (:elements db)))))))
  (reset-meta!
    #'nohistory-attrs
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'nohistory-attrs :ns *ns*))
  (defn attr-datoms ([db attrs] (mapcat (partial d/datoms db :aevt) attrs)))
  (reset-meta!
    #'attr-datoms
    (assoc
      {:arglists (clojure.core/list ['db 'attrs]), :column (int 1)}
      :name
      'attr-datoms
      :ns
      *ns*))
  (defn dups
    ([db attr]
      (distinct
        (map
          sort
          (d/q
            [:find
             '?e
             '?e2
             :in
             '$
             '?attr
             :where
             ['?e '?attr '?v]
             ['?e2 '?attr '?v]
             [(clojure.core/list '!= '?e '?e2)]]
            db
            attr)))))
  (reset-meta!
    #'dups
    (assoc {:arglists (clojure.core/list ['db 'attr]), :column (int 1)} :name 'dups :ns *ns*))
  (defn restatement?
    ([p__21994]
      (let [vec__21995 p__21994 d1 (nth vec__21995 (int 0) nil) d2 (nth vec__21995 (int 1) nil)]
        (and
          (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
          (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
          (= (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))
          (= (boolean (.added ^datomic.Datom d1)) (boolean (.added ^datomic.Datom d2)))))))
  (reset-meta!
    #'restatement?
    (assoc
      {:arglists
       (clojure.core/list [[(.withMeta 'd1 {:tag 'Datom}) (.withMeta 'd2 {:tag 'Datom})]]),
       :column (int 1)}
      :name
      'restatement?
      :ns
      *ns*))
  (defn attr-id-set
    ([db pred]
      (into
        #{}
        (map
          :id
          (filter
            pred
            (map
              (fn fn__22003
                ([p__22002]
                  (let [vec__22004 p__22002 e (nth vec__22004 (int 0) nil)] (d/attribute db e))))
              (d/q [:find '?e :where ['?e :db/valueType]] db)))))))
  (reset-meta!
    #'attr-id-set
    (assoc
      {:arglists (clojure.core/list ['db 'pred]), :column (int 1)}
      :name
      'attr-id-set
      :ns
      *ns*))
  (defn make-tupler
    ([index]
      (let [G__22009 index]
        (case
          G__22009
          :aevt
          (fn fn__22010 ([d] [(:a d) (:e d) (:v d) (:tx d)]))
          :avet
          (fn fn__22012 ([d] [(:a d) (:v d) (:e d) (:tx d)]))
          :eavt
          (fn fn__22014 ([d] [(:e d) (:a d) (:v d) (:tx d)]))
          :vaet
          (fn fn__22016 ([d] [(:v d) (:a d) (:e d) (:tx d)]))))))
  (reset-meta!
    #'make-tupler
    (assoc {:arglists (clojure.core/list ['index]), :column (int 1)} :name 'make-tupler :ns *ns*))
  (defn unfindable-datom-seq
    ([db index progress]
      (let [op (let [G__22028 index]
                 (case
                   G__22028
                   :aevt
                   (fn fn__22029
                     ([p1__22022# p2__22023#]
                       (.seekAEVT ^datomic.db.IDb p1__22022# ^datomic.impl.db.IDatum p2__22023#)))
                   :avet
                   (fn fn__22031
                     ([p1__22024# p2__22025#]
                       (.seekAVET ^datomic.db.IDb p1__22024# ^datomic.impl.db.IDatum p2__22025#)))
                   :eavt
                   (fn fn__22033
                     ([p1__22020# p2__22021#]
                       (.seekEAVT ^datomic.db.IDb p1__22020# ^datomic.impl.db.IDatum p2__22021#)))
                   :vaet
                   (fn fn__22035
                     ([p1__22026# p2__22027#]
                       (.seekRAET
                         ^datomic.db.IDb p1__22026#
                         ^datomic.impl.db.IDatum p2__22027#)))))]
        (remove
          (fn fn__22037
            ([datom]
              (when progress (^clojure.lang.IFn progress))
              (= datom (.get (^clojure.lang.IFn op db datom)))))
          (d/datoms db index)))))
  (reset-meta!
    #'unfindable-datom-seq
    (assoc
      {:arglists (clojure.core/list ['db 'index 'progress]), :column (int 1)}
      :name
      'unfindable-datom-seq
      :ns
      *ns*))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__22040 ([& _] (when (zero? (mod (swap! c inc) n)) (print ".") (flush)))))))
  (reset-meta!
    #'progress-dot-fn
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'progress-dot-fn :ns *ns*))
  (defn datom-comparator
    ([sort]
      (let [G__22043 sort]
        (case G__22043 :aevt db/aevt-cmp :avet db/avet-cmp :eavt db/eavt-cmp :vaet db/raet-cmp))))
  (reset-meta!
    #'datom-comparator
    (assoc
      {:arglists (clojure.core/list (.withMeta ['sort] {:tag 'java.util.Comparator})),
       :column (int 1)}
      :name
      'datom-comparator
      :ns
      *ns*))
  (defn unsorted-dirs
    ([db tier sort progress]
      (let [cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__22045
            ([a b]
              (^clojure.lang.IFn progress)
              (< (.compare ^java.util.Comparator cmp (:key a) (:key b)) 0)))
          (some-> db (^clojure.lang.IFn tier) (^clojure.lang.IFn sort) (.seek) (index/dir-seq))))))
  (reset-meta!
    #'unsorted-dirs
    (assoc
      {:arglists (clojure.core/list ['db 'tier 'sort 'progress]), :column (int 1)}
      :name
      'unsorted-dirs
      :ns
      *ns*))
  (defn validate-dir-sorts
    ([db]
      (let [tses (let [iter__6373__auto__ (fn iter__22049
                                            ([s__22050]
                                              (lazy-seq
                                                (loop [s__22050 s__22050]
                                                  (let [temp__5804__auto__ (seq s__22050)]
                                                    (when temp__5804__auto__
                                                      (let [xs__6360__auto__ temp__5804__auto__
                                                            tier (first xs__6360__auto__)
                                                            iterys__6369__auto__
                                                            (fn
                                                              iter__22051
                                                              ([s__22052]
                                                                (lazy-seq
                                                                  (let
                                                                    [s__22052 s__22052
                                                                     temp__5804__auto__
                                                                     (seq s__22052)]
                                                                    (when
                                                                      temp__5804__auto__
                                                                      (let
                                                                        [s__22052
                                                                         temp__5804__auto__]
                                                                        (if
                                                                          (chunked-seq? s__22052)
                                                                          (let
                                                                            [c__6371__auto__
                                                                             (chunk-first s__22052)
                                                                             size__6372__auto__
                                                                             (int
                                                                               (count
                                                                                 c__6371__auto__))
                                                                             b__22054
                                                                             (chunk-buffer
                                                                               (java.lang.Integer/valueOf
                                                                                 (int
                                                                                   size__6372__auto__)))]
                                                                            (if
                                                                              (loop
                                                                                [i__22053 (int 0)]
                                                                                (if
                                                                                  (<
                                                                                    i__22053
                                                                                    size__6372__auto__)
                                                                                  (let
                                                                                    [sort
                                                                                     (.nth
                                                                                       ^clojure.lang.Indexed c__6371__auto__
                                                                                       (int
                                                                                         i__22053))]
                                                                                    (chunk-append
                                                                                      b__22054
                                                                                      [tier sort])
                                                                                    (recur
                                                                                      (inc
                                                                                        i__22053)))
                                                                                  true))
                                                                              (chunk-cons
                                                                                (chunk b__22054)
                                                                                (^clojure.lang.IFn iter__22051
                                                                                  (chunk-rest
                                                                                    s__22052)))
                                                                              (chunk-cons
                                                                                (chunk b__22054)
                                                                                nil)))
                                                                          (let
                                                                            [sort (first s__22052)]
                                                                            (cons
                                                                              [tier sort]
                                                                              (^clojure.lang.IFn iter__22051
                                                                                (rest
                                                                                  s__22052)))))))))))
                                                            fs__6370__auto__
                                                            (seq
                                                              (^clojure.lang.IFn iterys__6369__auto__
                                                                [:eavt :aevt :avet :vaet]))]
                                                        (if fs__6370__auto__
                                                          (concat
                                                            fs__6370__auto__
                                                            (^clojure.lang.IFn iter__22049
                                                              (rest s__22050)))
                                                          (recur (rest s__22050))))))))))]
                   (^clojure.lang.IFn iter__6373__auto__ [:mid-index :main :history]))]
        (print "Validating dirs for ")
        (loop [seq_22072 (seq tses) chunk_22073 nil count_22074 0 i_22075 0]
          (if (< i_22075 count_22074)
            (let [vec__22076 (.nth ^clojure.lang.Indexed chunk_22073 (int i_22075))
                  tier (nth vec__22076 (int 0) nil)
                  sort (nth vec__22076 (int 1) nil)]
              (print "[" tier sort "]")
              (let [temp__5804__auto__ (unsorted-dirs db tier sort (progress-dot-fn 10000))]
                (when temp__5804__auto__
                  (let [s temp__5804__auto__]
                    (println)
                    (throw
                      (ex-info
                        (str "Disorderly dirs in " tier sort (first s))
                        {:pairs s, :tier tier, :sort sort}))))
                nil)
              (recur seq_22072 chunk_22073 count_22074 (inc i_22075)))
            (let [temp__5804__auto__ (seq seq_22072)]
              (when temp__5804__auto__
                (let [seq_22072 temp__5804__auto__]
                  (if (chunked-seq? seq_22072)
                    (let [c__6065__auto__ (chunk-first seq_22072)]
                      (recur
                        (chunk-rest seq_22072)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__22079 (first seq_22072)
                          tier (nth vec__22079 (int 0) nil)
                          sort (nth vec__22079 (int 1) nil)]
                      (print "[" tier sort "]")
                      (let [temp__5804__auto__ (unsorted-dirs
                                                 db
                                                 tier
                                                 sort
                                                 (progress-dot-fn 10000))]
                        (when temp__5804__auto__
                          (let [s temp__5804__auto__]
                            (println)
                            (throw
                              (ex-info
                                (str "Disorderly dirs in " tier sort (first s))
                                {:pairs s, :tier tier, :sort sort}))))
                        nil)
                      (recur (next seq_22072) nil 0 0))))))))
        (println))))
  (reset-meta!
    #'validate-dir-sorts
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'validate-dir-sorts
      :ns
      *ns*))
  (defn unsorted-datoms
    ([db sort order progress]
      (let [op (let [G__22088 order] (case G__22088 :allow-duplicates <= :strict <))
            cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__22089
            ([a b]
              (^clojure.lang.IFn progress)
              (^clojure.lang.IFn op
                (java.lang.Integer/valueOf (int (.compare ^java.util.Comparator cmp a b)))
                0)))
          (d/datoms db sort)))))
  (reset-meta!
    #'unsorted-datoms
    (assoc
      {:arglists (clojure.core/list ['db 'sort 'order 'progress]), :column (int 1)}
      :name
      'unsorted-datoms
      :ns
      *ns*))
  (defn validate-index-sorts
    ([db order]
      (loop [seq_22092 (seq [:eavt :aevt :avet :vaet]) chunk_22093 nil count_22094 0 i_22095 0]
        (if (< i_22095 count_22094)
          (let [sort (.nth ^clojure.lang.Indexed chunk_22093 (int i_22095))]
            (println "Validating " sort)
            (let [temp__5804__auto__ (unsorted-datoms db sort order (progress-dot-fn 10000))]
              (when temp__5804__auto__
                (let [s temp__5804__auto__]
                  (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s}))))
              nil)
            (println)
            (recur seq_22092 chunk_22093 count_22094 (inc i_22095)))
          (let [temp__5804__auto__ (seq seq_22092)]
            (when temp__5804__auto__
              (let [seq_22092 temp__5804__auto__]
                (if (chunked-seq? seq_22092)
                  (let [c__6065__auto__ (chunk-first seq_22092)]
                    (recur
                      (chunk-rest seq_22092)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [sort (first seq_22092)]
                    (println "Validating " sort)
                    (let [temp__5804__auto__ (unsorted-datoms
                                               db
                                               sort
                                               order
                                               (progress-dot-fn 10000))]
                      (when temp__5804__auto__
                        (let [s temp__5804__auto__]
                          (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s}))))
                      nil)
                    (println)
                    (recur (next seq_22092) nil 0 0)))))))))
    ([db] (validate-index-sorts db :strict)))
  (reset-meta!
    #'validate-index-sorts
    (assoc
      {:arglists (clojure.core/list ['db] ['db 'order]), :column (int 1)}
      :name
      'validate-index-sorts
      :ns
      *ns*))
  (defn unpaired-history-assertions
    ([db sort progress]
      (let [temp__5804__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5804__auto__
          (let [hist temp__5804__auto__]
            (tools/unsorted-seq
              (fn fn__22102
                ([d1 d2]
                  (^clojure.lang.IFn progress)
                  (or (not (.added ^datomic.Datom d2)) (index/retract-assert-pair? d1 d2))))
              (iter/iter-seq (.seek ^datomic.btset.IDataSet hist))))))))
  (reset-meta!
    #'unpaired-history-assertions
    (assoc
      {:arglists (clojure.core/list ['db 'sort 'progress]), :column (int 1)}
      :name
      'unpaired-history-assertions
      :ns
      *ns*))
  (defn unpaired-history-retractions
    ([db sort progress]
      (let [temp__5804__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5804__auto__
          (let [hist temp__5804__auto__]
            (tools/unsorted-seq
              (fn fn__22108
                ([d1 d2]
                  (^clojure.lang.IFn progress)
                  (or (.added ^datomic.Datom d1) (index/retract-assert-pair? d1 d2))))
              (iter/iter-seq (.seek ^datomic.btset.IDataSet hist))))))))
  (reset-meta!
    #'unpaired-history-retractions
    (assoc
      {:arglists (clojure.core/list ['db 'sort 'progress]), :column (int 1)}
      :name
      'unpaired-history-retractions
      :ns
      *ns*))
  (defn validate-history-pairs
    ([db]
      (loop [seq_22113 (seq [:eavt :aevt :avet :vaet]) chunk_22114 nil count_22115 0 i_22116 0]
        (if (< i_22116 count_22115)
          (let [sort (.nth ^clojure.lang.Indexed chunk_22114 (int i_22116))]
            (println "Validating history pairs " sort)
            (let [temp__5804__auto__ (unpaired-history-assertions
                                       db
                                       sort
                                       (progress-dot-fn 100000))]
              (when temp__5804__auto__
                (let [s temp__5804__auto__]
                  (throw
                    (ex-info (str "Unpaired history assertions " (first s)) {:assertions s}))))
              nil)
            (println)
            (recur seq_22113 chunk_22114 count_22115 (inc i_22116)))
          (let [temp__5804__auto__ (seq seq_22113)]
            (when temp__5804__auto__
              (let [seq_22113 temp__5804__auto__]
                (if (chunked-seq? seq_22113)
                  (let [c__6065__auto__ (chunk-first seq_22113)]
                    (recur
                      (chunk-rest seq_22113)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [sort (first seq_22113)]
                    (println "Validating history pairs " sort)
                    (let [temp__5804__auto__ (unpaired-history-assertions
                                               db
                                               sort
                                               (progress-dot-fn 100000))]
                      (when temp__5804__auto__
                        (let [s temp__5804__auto__]
                          (throw
                            (ex-info
                              (str "Unpaired history assertions " (first s))
                              {:assertions s}))))
                      nil)
                    (println)
                    (recur (next seq_22113) nil 0 0))))))))))
  (reset-meta!
    #'validate-history-pairs
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'validate-history-pairs
      :ns
      *ns*))
  (defn selfcheck-index-cli
    ([p__22122]
      (let [map__22123 p__22122
            map__22123 (if (seq? map__22123)
                         (if (next map__22123)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22123))
                           (if (seq map__22123) (first map__22123) {}))
                         map__22123)
            uri (get map__22123 :uri)
            uri (enhance-uri uri)
            conn (d/connect uri)
            db (d/db conn)]
        (loop [seq_22124 (seq [:eavt :aevt :avet :vaet]) chunk_22125 nil count_22126 0 i_22127 0]
          (if (< i_22127 count_22126)
            (let [index (.nth ^clojure.lang.Indexed chunk_22125 (int i_22127))]
              (print "Self-checking " index)
              (let [count (atom 0)
                    progress (fn progress
                               ([]
                                 (when (zero? (mod (swap! count inc) 10000)) (print ".") (flush))))
                    problem (first (unfindable-datom-seq db index progress))]
                (when problem
                  (throw
                    (ex-info
                      (str "Unable to seek to " (pr-str problem) " in " index)
                      {:datom problem, :index index})))
                (println "\nChecked " (deref count) " datoms"))
              (recur seq_22124 chunk_22125 count_22126 (inc i_22127)))
            (let [temp__5804__auto__ (seq seq_22124)]
              (when temp__5804__auto__
                (let [seq_22124 temp__5804__auto__]
                  (if (chunked-seq? seq_22124)
                    (let [c__6065__auto__ (chunk-first seq_22124)]
                      (recur
                        (chunk-rest seq_22124)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [index (first seq_22124)]
                      (print "Self-checking " index)
                      (let [count (atom 0)
                            progress (fn progress
                                       ([]
                                         (when (zero? (mod (swap! count inc) 10000))
                                           (print ".")
                                           (flush))))
                            problem (first (unfindable-datom-seq db index progress))]
                        (when problem
                          (throw
                            (ex-info
                              (str "Unable to seek to " (pr-str problem) " in " index)
                              {:datom problem, :index index})))
                        (println "\nChecked " (deref count) " datoms"))
                      (recur (next seq_22124) nil 0 0)))))))))))
  (reset-meta!
    #'selfcheck-index-cli
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri]}]), :column (int 1)}
      :name
      'selfcheck-index-cli
      :ns
      *ns*))
  (defn mk-attr-pred ([s] (fn fn__22136 ([p1__22135#] (contains? s (:a p1__22135#))))))
  (reset-meta!
    #'mk-attr-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'mk-attr-pred
      :ns
      *ns*))
  (defn mk-index-pred
    ([db index]
      (let [G__22140 index]
        (case
          G__22140
          (:aevt :eavt)
          identity
          :avet
          (mk-attr-pred (attr-id-set db :has-avet))
          :raet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__22141 ([p1__22139#] (= :db.type/ref (:value-type p1__22139#))))))
          :vaet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__22143 ([p1__22139#] (= :db.type/ref (:value-type p1__22139#))))))))))
  (reset-meta!
    #'mk-index-pred
    (assoc
      {:arglists (clojure.core/list ['db 'index]), :column (int 1)}
      :name
      'mk-index-pred
      :ns
      *ns*))
  (defn crosscheck-log
    ([log db index progress]
      (do
        (let [m_22147 {:event :integrity/crosscheck-log, :index index, :db (:id db)}
              ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_22147 :phase :begin))))
                                nil)
              start__8553__auto__ (java.lang.System/nanoTime)
              result__8554__auto__ (try
                                     {:returned nil}
                                     (catch
                                       java.lang.Throwable
                                       t__8555__auto__
                                       {:threw t__8555__auto__}))
              elapsed_22148 (- (java.lang.System/nanoTime) start__8553__auto__)
              msec_22149 (logger/format-as-msec (long elapsed_22148))]
          (let [endmsg__8556__auto__ (merge
                                       (assoc m_22147 :msec msec_22149 :phase :end)
                                       (when (:threw result__8554__auto__)
                                         {:threw (class (:threw result__8554__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
            nil)
          (if (contains? result__8554__auto__ :returned)
            (:returned result__8554__auto__)
            (throw (:threw result__8554__auto__))))
        (let [temp__5802__auto__ (seq (iter/iter-seq (log/seek-tx log 0)))]
          (when temp__5802__auto__
            (let [log_seq temp__5802__auto__
                  tupler (make-tupler index)
                  nohists (tools/ever-nohistory-attrs db)
                  basis_t (d/basis-t db)
                  limit_tx (d/t->tx (long ^java.lang.Number basis_t))
                  hist (d/history db)
                  index_pred (mk-index-pred db index)]
              (reduce
                (fn fn__22152
                  ([ctr tx]
                    (loop [seq_22153 (seq
                                       (take-while
                                         (fn fn__22157
                                           ([p1__22146#] (<= (:tx p1__22146#) limit_tx)))
                                         (:data tx)))
                           chunk_22154 nil
                           count_22155 0
                           i_22156 0]
                      (if (< i_22156 count_22155)
                        (let [d (.nth ^clojure.lang.Indexed chunk_22154 (int i_22156))]
                          (when (and (.added ^datomic.Datom d) (^clojure.lang.IFn index_pred d))
                            (let [dnow (first (db/datoms db index (^clojure.lang.IFn tupler d)))]
                              (if (= d dnow)
                                (when progress (^clojure.lang.IFn progress :current))
                                (if (contains? nohists (:a d))
                                  (when progress (^clojure.lang.IFn progress :nohistory))
                                  (let [dhist (first
                                                (db/datoms
                                                  hist
                                                  index
                                                  (^clojure.lang.IFn tupler d)))]
                                    (if (= d dhist)
                                      (when progress (^clojure.lang.IFn progress :history))
                                      (throw
                                        (ex-info
                                          (str "Unable to seek to " (pr-str d) " in " index)
                                          {:datom d,
                                           :dhist dhist,
                                           :index index,
                                           :basis-t basis_t}))))))))
                          (recur seq_22153 chunk_22154 count_22155 (inc i_22156)))
                        (let [temp__5804__auto__ (seq seq_22153)]
                          (when temp__5804__auto__
                            (let [seq_22153 temp__5804__auto__]
                              (if (chunked-seq? seq_22153)
                                (let [c__6065__auto__ (chunk-first seq_22153)]
                                  (recur
                                    (chunk-rest seq_22153)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [d (first seq_22153)]
                                  (when (and
                                          (.added ^datomic.Datom d)
                                          (^clojure.lang.IFn index_pred d))
                                    (let [dnow (first
                                                 (db/datoms
                                                   db
                                                   index
                                                   (^clojure.lang.IFn tupler d)))]
                                      (if (= d dnow)
                                        (when progress (^clojure.lang.IFn progress :current))
                                        (if (contains? nohists (:a d))
                                          (when progress (^clojure.lang.IFn progress :nohistory))
                                          (let [dhist (first
                                                        (db/datoms
                                                          hist
                                                          index
                                                          (^clojure.lang.IFn tupler d)))]
                                            (if (= d dhist)
                                              (when progress (^clojure.lang.IFn progress :history))
                                              (throw
                                                (ex-info
                                                  (str
                                                    "Unable to seek to "
                                                    (pr-str d)
                                                    " in "
                                                    index)
                                                  {:datom d,
                                                   :dhist dhist,
                                                   :index index,
                                                   :basis-t basis_t}))))))))
                                  (recur (next seq_22153) nil 0 0))))))))
                    (inc ctr)))
                0
                log_seq))))))
    ([log db index] (crosscheck-log log db index nil)))
  (reset-meta!
    #'crosscheck-log
    (assoc
      {:arglists (clojure.core/list ['log 'db 'index] ['log 'db 'index 'progress]),
       :column (int 1)}
      :name
      'crosscheck-log
      :ns
      *ns*))
  (defn crosscheck-log-representations
    ([uri progress]
      (let [uri (enhance-uri uri)
            map__22170 (tools/connection-resources uri)
            map__22170 (if (seq? map__22170)
                         (if (next map__22170)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22170))
                           (if (seq map__22170) (first map__22170) {}))
                         map__22170)
            cluster (get map__22170 :cluster)
            olookup (get map__22170 :olookup)
            conn (d/connect uri)
            db (d/db conn)
            log_1 (log/find-log cluster olookup)
            log_2 (log/create-log-val cluster olookup db)
            rep_1 (iter/iter-seq (log/seek-tx log_1 0))
            rep_2 (iter/iter-seq (log/seek-tx log_2 0))
            diffs (remove
                    (fn fn__22172
                      ([p__22171]
                        (let [vec__22173 p__22171
                              a (nth vec__22173 (int 0) nil)
                              b (nth vec__22173 (int 1) nil)]
                          (^clojure.lang.IFn progress)
                          (= (dissoc a :id) (dissoc b :id)))))
                    (map vector rep_1 rep_2))
            desc {:basis-t (:basisT db),
                  :index-basis-t (:indexBasisT db),
                  :tail-1 (first (:txes (:tail log_1))),
                  :tail-2 (first (:txes (:tail log_2)))}]
        (when (seq diffs)
          (throw (ex-info "Log representations did not match" (assoc desc :diffs diffs))))
        desc))
    ([uri] (crosscheck-log-representations uri (progress-dot-fn 1000))))
  (reset-meta!
    #'crosscheck-log-representations
    (assoc
      {:arglists (clojure.core/list ['uri] ['uri 'progress]), :column (int 1)}
      :name
      'crosscheck-log-representations
      :ns
      *ns*))
  (defn crosscheck-log-cli
    ([p__22178]
      (let [map__22179 p__22178
            map__22179 (if (seq? map__22179)
                         (if (next map__22179)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22179))
                           (if (seq map__22179) (first map__22179) {}))
                         map__22179)
            uri (get map__22179 :uri)]
        (println)
        (let [uri (enhance-uri uri) cr (tools/connection-resources uri) conn (d/connect uri)]
          (loop [seq_22180 (seq [:eavt :aevt :avet :vaet]) chunk_22181 nil count_22182 0 i_22183 0]
            (if (< i_22183 count_22182)
              (let [index (.nth ^clojure.lang.Indexed chunk_22181 (int i_22183))]
                (print "Checking " index " against log")
                (flush)
                (let [current (atom 0)
                      history (atom 0)
                      nohistory (atom 0)
                      log (log/find-log (:cluster cr) (:olookup cr))
                      txes (crosscheck-log
                             log
                             (d/db conn)
                             index
                             (fn fn__22184
                               ([result]
                                 (let [G__22185 result]
                                   (case
                                     G__22185
                                     :history
                                     (when (zero? (mod (swap! history inc) 10000))
                                       (print "-")
                                       (flush))
                                     :current
                                     (when (zero? (mod (swap! current inc) 10000))
                                       (print ".")
                                       (flush))
                                     :nohistory
                                     (when (zero? (mod (swap! nohistory inc) 10000))
                                       (print "~")
                                       (flush)))))))]
                  (println
                    "\n"
                    {:txes txes,
                     :current-datoms (deref current),
                     :history-datoms (deref history),
                     :nohistory-dropped (deref nohistory)}
                    "\n"))
                (recur seq_22180 chunk_22181 count_22182 (inc i_22183)))
              (let [temp__5804__auto__ (seq seq_22180)]
                (when temp__5804__auto__
                  (let [seq_22180 temp__5804__auto__]
                    (if (chunked-seq? seq_22180)
                      (let [c__6065__auto__ (chunk-first seq_22180)]
                        (recur
                          (chunk-rest seq_22180)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [index (first seq_22180)]
                        (print "Checking " index " against log")
                        (flush)
                        (let [current (atom 0)
                              history (atom 0)
                              nohistory (atom 0)
                              log (log/find-log (:cluster cr) (:olookup cr))
                              txes (crosscheck-log
                                     log
                                     (d/db conn)
                                     index
                                     (fn fn__22187
                                       ([result]
                                         (let [G__22188 result]
                                           (case
                                             G__22188
                                             :history
                                             (when (zero? (mod (swap! history inc) 10000))
                                               (print "-")
                                               (flush))
                                             :current
                                             (when (zero? (mod (swap! current inc) 10000))
                                               (print ".")
                                               (flush))
                                             :nohistory
                                             (when (zero? (mod (swap! nohistory inc) 10000))
                                               (print "~")
                                               (flush)))))))]
                          (println
                            "\n"
                            {:txes txes,
                             :current-datoms (deref current),
                             :history-datoms (deref history),
                             :nohistory-dropped (deref nohistory)}
                            "\n"))
                        (recur (next seq_22180) nil 0 0))))))))))))
  (reset-meta!
    #'crosscheck-log-cli
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri]}]), :column (int 1)}
      :name
      'crosscheck-log-cli
      :ns
      *ns*))
  (def defcrosscheck
   (fn defcrosscheck
     ([&form &env i1 i2]
       (let [mname (symbol (str "crosscheck-" (name i1) "-" (name i2)))
             i1_up (symbol (str ".seek" (.toUpperCase (name i1))))
             i2_up (symbol (str ".seek" (.toUpperCase (name i2))))]
         (seq
           (concat
             (clojure.core/list 'clojure.core/defn)
             (clojure.core/list mname)
             (clojure.core/list
               (apply
                 vector
                 (seq
                   (concat
                     (clojure.core/list (.withMeta 'db1 {:tag 'IDb}))
                     (clojure.core/list (.withMeta 'db2 {:tag 'IDb}))
                     (clojure.core/list 'progress)))))
             (clojure.core/list
               (seq
                 (concat
                   (clojure.core/list 'clojure.core/let)
                   (clojure.core/list
                     (apply
                       vector
                       (seq
                         (concat
                           (clojure.core/list 'start)
                           (clojure.core/list
                             (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                           (clojure.core/list 'index-pred)
                           (clojure.core/list
                             (seq
                               (concat
                                 (clojure.core/list 'datomic.integrity/mk-index-pred)
                                 (clojure.core/list 'db2)
                                 (clojure.core/list (keyword i2)))))))))
                   (clojure.core/list
                     (seq
                       (concat
                         (clojure.core/list 'clojure.core/loop)
                         (clojure.core/list
                           (apply
                             vector
                             (seq
                               (concat
                                 (clojure.core/list 'i)
                                 (-> (clojure.core/list 'datomic.db/filter-retractions)
                                  (clojure.core/list i1_up)
                                  (concat
                                    (clojure.core/list 'db1)
                                    (clojure.core/list
                                      (seq
                                        (concat
                                          (clojure.core/list 'datomic.db/datum)
                                          (clojure.core/list 'db1)))))
                                  (seq)
                                  (clojure.core/list)
                                  (concat)
                                  (seq)
                                  (with-meta
                                    (apply
                                      hash-map
                                      (seq
                                        (concat
                                          (clojure.core/list :line)
                                          (clojure.core/list 402)
                                          (clojure.core/list :column)
                                          (clojure.core/list 21)
                                          (clojure.core/list :tag)
                                          (clojure.core/list 'datomic.iter.Iter)))))
                                  (clojure.core/list))
                                 (clojure.core/list 'c)
                                 (clojure.core/list 0)))))
                         (clojure.core/list
                           (seq (concat (clojure.core/list 'progress) (clojure.core/list 'c))))
                         (clojure.core/list
                           (seq
                             (concat
                               (clojure.core/list 'if)
                               (clojure.core/list 'i)
                               (clojure.core/list
                                 (seq
                                   (concat
                                     (clojure.core/list 'clojure.core/let)
                                     (clojure.core/list
                                       (apply
                                         vector
                                         (seq
                                           (concat
                                             (clojure.core/list 'datum)
                                             (clojure.core/list
                                               (seq
                                                 (concat
                                                   (clojure.core/list '.get)
                                                   (clojure.core/list 'i))))))))
                                     (clojure.core/list
                                       (seq
                                         (concat
                                           (clojure.core/list 'if)
                                           (clojure.core/list
                                             (seq
                                               (concat
                                                 (clojure.core/list 'index-pred)
                                                 (clojure.core/list 'datum))))
                                           (clojure.core/list
                                             (seq
                                               (concat
                                                 (clojure.core/list 'do)
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'clojure.core/when-not)
                                                       (clojure.core/list
                                                         (seq
                                                           (concat
                                                             (clojure.core/list 'clojure.core/=)
                                                             (clojure.core/list
                                                               (seq
                                                                 (concat
                                                                   (clojure.core/list '.get)
                                                                   (clojure.core/list
                                                                     (seq
                                                                       (concat
                                                                         (clojure.core/list i2_up)
                                                                         (clojure.core/list 'db2)
                                                                         (clojure.core/list
                                                                           'datum)))))))
                                                             (clojure.core/list 'datum))))
                                                       (clojure.core/list
                                                         (seq
                                                           (concat
                                                             (clojure.core/list 'throw)
                                                             (clojure.core/list
                                                               (seq
                                                                 (concat
                                                                   (clojure.core/list
                                                                     'clojure.core/ex-info)
                                                                   (clojure.core/list
                                                                     (seq
                                                                       (concat
                                                                         (clojure.core/list
                                                                           'clojure.core/str)
                                                                         (clojure.core/list
                                                                           "Found ")
                                                                         (clojure.core/list
                                                                           (seq
                                                                             (concat
                                                                               (clojure.core/list
                                                                                 'clojure.core/pr-str)
                                                                               (clojure.core/list
                                                                                 'datum))))
                                                                         (clojure.core/list " in ")
                                                                         (clojure.core/list
                                                                           (seq
                                                                             (concat
                                                                               (clojure.core/list
                                                                                 'quote)
                                                                               (clojure.core/list
                                                                                 i1))))
                                                                         (clojure.core/list
                                                                           " but not in ")
                                                                         (clojure.core/list
                                                                           (seq
                                                                             (concat
                                                                               (clojure.core/list
                                                                                 'quote)
                                                                               (clojure.core/list
                                                                                 i2)))))))
                                                                   (clojure.core/list
                                                                     (apply
                                                                       hash-map
                                                                       (seq
                                                                         (concat
                                                                           (clojure.core/list
                                                                             :datom)
                                                                           (clojure.core/list
                                                                             'datum)
                                                                           (clojure.core/list :db1)
                                                                           (clojure.core/list 'db2)
                                                                           (clojure.core/list :db2)
                                                                           (clojure.core/list
                                                                             'db2))))))))))))))
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list 'recur)
                                                       (clojure.core/list
                                                         (seq
                                                           (concat
                                                             (clojure.core/list '.next)
                                                             (clojure.core/list 'i))))
                                                       (clojure.core/list
                                                         (seq
                                                           (concat
                                                             (clojure.core/list 'clojure.core/inc)
                                                             (clojure.core/list 'c))))))))))
                                           (clojure.core/list
                                             (seq
                                               (concat
                                                 (clojure.core/list 'recur)
                                                 (clojure.core/list
                                                   (seq
                                                     (concat
                                                       (clojure.core/list '.next)
                                                       (clojure.core/list 'i))))
                                                 (clojure.core/list 'c))))))))))
                               (clojure.core/list
                                 (apply
                                   hash-map
                                   (seq
                                     (concat
                                       (clojure.core/list :count)
                                       (clojure.core/list 'c)
                                       (clojure.core/list :check)
                                       (clojure.core/list
                                         (seq
                                           (concat
                                             (clojure.core/list 'quote)
                                             (clojure.core/list mname))))
                                       (clojure.core/list :next-t)
                                       (clojure.core/list
                                         (seq
                                           (concat
                                             (clojure.core/list '.getNextT)
                                             (clojure.core/list 'db1))))
                                       (clojure.core/list :msec)
                                       (clojure.core/list
                                         (seq
                                           (concat
                                             (clojure.core/list 'clojure.core/quot)
                                             (clojure.core/list
                                               (seq
                                                 (concat
                                                   (clojure.core/list 'clojure.core/-)
                                                   (clojure.core/list
                                                     (seq
                                                       (concat
                                                         (clojure.core/list
                                                           'java.lang.System/nanoTime))))
                                                   (clojure.core/list 'start))))
                                             (clojure.core/list 1000000)))))))))))))))))))))))
  (reset-meta!
    #'defcrosscheck
    (assoc
      {:arglists (clojure.core/list ['i1 'i2]), :column (int 1)}
      :name
      'defcrosscheck
      :ns
      *ns*))
  (.setMacro #'defcrosscheck)
  (defn crosscheck-eavt-eavt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :eavt)]
        (loop [i (db/filter-retractions (.seekEAVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekEAVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'eavt " but not in " 'eavt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-eavt-eavt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-eavt-eavt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-eavt-eavt
      :ns
      *ns*))
  (defn crosscheck-aevt-aevt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :aevt)]
        (loop [i (db/filter-retractions (.seekAEVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAEVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'aevt " but not in " 'aevt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-aevt-aevt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-aevt-aevt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-aevt-aevt
      :ns
      *ns*))
  (defn crosscheck-avet-avet
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :avet)]
        (loop [i (db/filter-retractions (.seekAVET ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAVET ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'avet " but not in " 'avet)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-avet-avet,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-avet-avet
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-avet-avet
      :ns
      *ns*))
  (defn crosscheck-raet-raet
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :raet)]
        (loop [i (db/filter-retractions (.seekRAET ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekRAET ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'raet " but not in " 'raet)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-raet-raet,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-raet-raet
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-raet-raet
      :ns
      *ns*))
  (defn crosscheck-eavt-aevt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :aevt)]
        (loop [i (db/filter-retractions (.seekEAVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAEVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'eavt " but not in " 'aevt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-eavt-aevt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-eavt-aevt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-eavt-aevt
      :ns
      *ns*))
  (defn crosscheck-aevt-eavt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :eavt)]
        (loop [i (db/filter-retractions (.seekAEVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekEAVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'aevt " but not in " 'eavt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-aevt-eavt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-aevt-eavt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-aevt-eavt
      :ns
      *ns*))
  (defn crosscheck-avet-eavt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :eavt)]
        (loop [i (db/filter-retractions (.seekAVET ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekEAVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'avet " but not in " 'eavt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-avet-eavt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-avet-eavt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-avet-eavt
      :ns
      *ns*))
  (defn crosscheck-avet-aevt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :aevt)]
        (loop [i (db/filter-retractions (.seekAVET ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAEVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'avet " but not in " 'aevt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-avet-aevt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-avet-aevt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-avet-aevt
      :ns
      *ns*))
  (defn crosscheck-raet-aevt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :aevt)]
        (loop [i (db/filter-retractions (.seekRAET ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAEVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'raet " but not in " 'aevt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-raet-aevt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-raet-aevt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-raet-aevt
      :ns
      *ns*))
  (defn crosscheck-raet-eavt
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :eavt)]
        (loop [i (db/filter-retractions (.seekRAET ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekEAVT ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'raet " but not in " 'eavt)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-raet-eavt,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-raet-eavt
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-raet-eavt
      :ns
      *ns*))
  (defn crosscheck-aevt-avet
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :avet)]
        (loop [i (db/filter-retractions (.seekAEVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAVET ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'aevt " but not in " 'avet)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-aevt-avet,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-aevt-avet
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-aevt-avet
      :ns
      *ns*))
  (defn crosscheck-eavt-avet
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :avet)]
        (loop [i (db/filter-retractions (.seekEAVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekAVET ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'eavt " but not in " 'avet)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-eavt-avet,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-eavt-avet
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-eavt-avet
      :ns
      *ns*))
  (defn crosscheck-aevt-raet
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :raet)]
        (loop [i (db/filter-retractions (.seekAEVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekRAET ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'aevt " but not in " 'raet)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-aevt-raet,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-aevt-raet
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-aevt-raet
      :ns
      *ns*))
  (defn crosscheck-eavt-raet
    ([db1 db2 progress]
      (let [start (java.lang.System/nanoTime) index_pred (mk-index-pred db2 :raet)]
        (loop [i (db/filter-retractions (.seekEAVT ^datomic.db.IDb db1 (db/datum db1))) c 0]
          (do
            (^clojure.lang.IFn progress (long c))
            (if i
              (let [datum (.get ^datomic.iter.Iter i)]
                (if (^clojure.lang.IFn index_pred datum)
                  (do
                    (when-not (=
                                (.get
                                  (.seekRAET ^datomic.db.IDb db2 ^datomic.impl.db.IDatum datum))
                                datum)
                      (throw
                        (ex-info
                          (str "Found " (pr-str datum) " in " 'eavt " but not in " 'raet)
                          {:datom datum, :db2 db2, :db1 db2})))
                    (recur (.next ^datomic.iter.Iter i) (inc c)))
                  (recur (.next ^datomic.iter.Iter i) c)))
              {:check 'crosscheck-eavt-raet,
               :next-t (.getNextT ^datomic.db.IDb db1),
               :count (long c),
               :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))}))))))
  (reset-meta!
    #'crosscheck-eavt-raet
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'db1 {:tag 'IDb}) (.withMeta 'db2 {:tag 'IDb}) 'progress]),
       :column (int 1)}
      :name
      'crosscheck-eavt-raet
      :ns
      *ns*))
  (defn get-db ([o] (if (instance? datomic.Database o) o (d/db o))))
  (reset-meta!
    #'get-db
    (assoc
      {:arglists (clojure.core/list (.withMeta ['o] {:tag 'datomic.db.IDb})), :column (int 1)}
      :name
      'get-db
      :ns
      *ns*))
  (defn crosscheck-indexes
    ([o]
      (let [m_22209 {:event :integrity/crosscheck-indexes}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22209 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (loop [seq_22213 (seq
                                                       ['crosscheck-eavt-aevt
                                                        'crosscheck-aevt-eavt
                                                        'crosscheck-avet-eavt
                                                        'crosscheck-avet-aevt
                                                        'crosscheck-raet-aevt
                                                        'crosscheck-raet-eavt
                                                        'crosscheck-aevt-avet
                                                        'crosscheck-eavt-avet])
                                           chunk_22214 nil
                                           count_22215 0
                                           i_22216 0]
                                      (if (< i_22216 count_22215)
                                        (let [c (.nth
                                                  ^clojure.lang.Indexed chunk_22214
                                                  (int i_22216))]
                                          (print "\n" c)
                                          (flush)
                                          (println
                                            ((ns-resolve 'datomic.integrity c)
                                              (get-db o)
                                              (get-db o)
                                              (fn fn__22218
                                                ([n]
                                                  (when (zero? (mod n 100000))
                                                    (print ".")
                                                    (flush))))))
                                          (flush)
                                          (recur seq_22213 chunk_22214 count_22215 (inc i_22216)))
                                        (let [temp__5804__auto__ (seq seq_22213)]
                                          (when temp__5804__auto__
                                            (let [seq_22213 temp__5804__auto__]
                                              (if (chunked-seq? seq_22213)
                                                (let [c__6065__auto__ (chunk-first seq_22213)]
                                                  (recur
                                                    (chunk-rest seq_22213)
                                                    c__6065__auto__
                                                    (int (count c__6065__auto__))
                                                    (int 0)))
                                                (let [c (first seq_22213)]
                                                  (print "\n" c)
                                                  (flush)
                                                  (println
                                                    ((ns-resolve 'datomic.integrity c)
                                                      (get-db o)
                                                      (get-db o)
                                                      (fn fn__22220
                                                        ([n]
                                                          (when (zero? (mod n 100000))
                                                            (print ".")
                                                            (flush))))))
                                                  (flush)
                                                  (recur (next seq_22213) nil 0 0))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_22210 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_22211 (logger/format-as-msec (long elapsed_22210))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_22209 :msec msec_22211 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'crosscheck-indexes
    (assoc
      {:arglists (clojure.core/list ['o]), :column (int 1)}
      :name
      'crosscheck-indexes
      :ns
      *ns*))
  (defn path-to-t
    ([cs olookup t]
      (let [temp__5804__auto__ (some-> (log/read-tail-descriptor cs) (first) (:d/r))]
        (when temp__5804__auto__
          (let [root_val_key temp__5804__auto__
                rootid (cluster/val-key->uuid root_val_key)
                root (vec (get olookup rootid))
                comp (common/key-comparator log/log-key)
                ridx (log/btree-search root t comp)
                dirid (:uuid (nth root (int ^java.lang.Number ridx)))
                dir (get olookup dirid)]
            (when-not (= (count dir) 0)
              (let [didx (log/btree-search dir t comp)
                    segid (:uuid (nth dir (int ^java.lang.Number didx)))
                    seg (get olookup segid)
                    sidx (log/binary-search seg t comp)]
                {:rootid rootid,
                 :ridx ridx,
                 :dirid dirid,
                 :didx didx,
                 :segid segid,
                 :segidx sidx})))))))
  (reset-meta!
    #'path-to-t
    (assoc
      {:arglists (clojure.core/list ['cs 'olookup 't]), :column (int 1)}
      :name
      'path-to-t
      :ns
      *ns*))
  (defn dir-seg-info-seq
    ([dir olookup]
      (map
        (fn fn__22234
          ([d] {:dir-t (:t d), :segid (:uuid d), :seg-t (:t (first (get olookup (:uuid d))))}))
        dir)))
  (reset-meta!
    #'dir-seg-info-seq
    (assoc
      {:arglists (clojure.core/list ['dir 'olookup]), :column (int 1)}
      :name
      'dir-seg-info-seq
      :ns
      *ns*))
  (defn validate-t-order*
    ([log progress]
      (if (seq (iter/iter-seq (log/seek-tx log 0)))
        (do
          (when-not (= 1000 (-> (log/seek-tx log 0) (iter/iter-seq) (seq) (first) (:t)))
            (let [form__21885__auto__ (clojure.core/list
                                        '=
                                        1000
                                        (clojure.core/list
                                          :t
                                          (clojure.core/list
                                            'first
                                            (clojure.core/list
                                              'seq
                                              (clojure.core/list
                                                'iter/iter-seq
                                                (clojure.core/list 'log/seek-tx 'log 0))))))
                  error__21886__auto__ (ex-info
                                         "Assertion failed, see ex-data for details"
                                         {:bindings {'log log, 'progress progress},
                                          :form form__21885__auto__})]
              (if datomic.assert/*assert-handler*
                (datomic.assert/*assert-handler* error__21886__auto__)
                (throw ^java.lang.Throwable error__21886__auto__))))
          (reduce
            (fn fn__22238
              ([ctr p__22237]
                (let [vec__22239 p__22237
                      tx1 (nth vec__22239 (int 0) nil)
                      tx2 (nth vec__22239 (int 1) nil)]
                  (when progress (^clojure.lang.IFn progress {:n ctr, :t (:t tx1)}))
                  (let [tx (d/t->tx (long (:t tx1))) max_eidx (log/max-eidx (:data tx1))]
                    (loop [seq_22242 (seq (:data tx1)) chunk_22243 nil count_22244 0 i_22245 0]
                      (if (< i_22245 count_22244)
                        (let [d (.nth ^clojure.lang.Indexed chunk_22243 (int i_22245))]
                          (when-not (= (:tx d) tx)
                            (let [form__21885__auto__ (clojure.core/list
                                                        '=
                                                        (clojure.core/list :tx 'd)
                                                        'tx)
                                  error__21886__auto__ (ex-info
                                                         "not all datoms in transaction have same tx"
                                                         {:bindings
                                                          {'vec__22239 vec__22239,
                                                           (.withMeta
                                                             'chunk_22243
                                                             {:tag 'clojure.lang.IChunk})
                                                           chunk_22243,
                                                           'max-eidx max_eidx,
                                                           'i_22245 (long i_22245),
                                                           'count_22244 (long count_22244),
                                                           'progress progress,
                                                           'log log,
                                                           'ctr ctr,
                                                           'tx1 tx1,
                                                           'seq_22242 seq_22242,
                                                           'tx tx,
                                                           'tx2 tx2,
                                                           'd d,
                                                           'p__22237 p__22237},
                                                          :form form__21885__auto__})]
                              (if datomic.assert/*assert-handler*
                                (datomic.assert/*assert-handler* error__21886__auto__)
                                (throw ^java.lang.Throwable error__21886__auto__))))
                          (recur seq_22242 chunk_22243 count_22244 (inc i_22245)))
                        (let [temp__5804__auto__ (seq seq_22242)]
                          (when temp__5804__auto__
                            (let [seq_22242 temp__5804__auto__]
                              (if (chunked-seq? seq_22242)
                                (let [c__6065__auto__ (chunk-first seq_22242)]
                                  (recur
                                    (chunk-rest seq_22242)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [d (first seq_22242)]
                                  (when-not (= (:tx d) tx)
                                    (let [form__21885__auto__ (clojure.core/list
                                                                '=
                                                                (clojure.core/list :tx 'd)
                                                                'tx)
                                          error__21886__auto__ (ex-info
                                                                 "not all datoms in transaction have same tx"
                                                                 {:bindings
                                                                  {'vec__22239 vec__22239,
                                                                   (.withMeta
                                                                     'chunk_22243
                                                                     {:tag 'clojure.lang.IChunk})
                                                                   chunk_22243,
                                                                   'max-eidx max_eidx,
                                                                   'i_22245 (long i_22245),
                                                                   'count_22244 (long count_22244),
                                                                   'progress progress,
                                                                   'log log,
                                                                   'ctr ctr,
                                                                   'tx1 tx1,
                                                                   'seq_22242 seq_22242,
                                                                   'tx tx,
                                                                   'tx2 tx2,
                                                                   'd d,
                                                                   'p__22237 p__22237,
                                                                   'temp__5804__auto__
                                                                   temp__5804__auto__},
                                                                  :form form__21885__auto__})]
                                      (if datomic.assert/*assert-handler*
                                        (datomic.assert/*assert-handler* error__21886__auto__)
                                        (throw ^java.lang.Throwable error__21886__auto__))))
                                  (recur (next seq_22242) nil 0 0))))))))
                    (when tx2
                      (when-not (< (:t tx1) (:t tx2))
                        (let [form__21885__auto__ (clojure.core/list
                                                    '<
                                                    (clojure.core/list :t 'tx1)
                                                    (clojure.core/list :t 'tx2))
                              error__21886__auto__ (ex-info
                                                     "txes are not ascending"
                                                     {:bindings
                                                      {'vec__22239 vec__22239,
                                                       'max-eidx max_eidx,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'tx1 tx1,
                                                       'tx tx,
                                                       'tx2 tx2,
                                                       'p__22237 p__22237},
                                                      :form form__21885__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__21886__auto__)
                            (throw ^java.lang.Throwable error__21886__auto__))))
                      (when-not (<= (inc max_eidx) (:t tx2))
                        (let [form__21885__auto__ (clojure.core/list
                                                    '<=
                                                    (clojure.core/list 'inc 'max-eidx)
                                                    (clojure.core/list :t 'tx2))
                              error__21886__auto__ (ex-info
                                                     "entity t too high for tx"
                                                     {:bindings
                                                      {'vec__22239 vec__22239,
                                                       'max-eidx max_eidx,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'tx1 tx1,
                                                       'tx tx,
                                                       'tx2 tx2,
                                                       'p__22237 p__22237},
                                                      :form form__21885__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__21886__auto__)
                            (throw ^java.lang.Throwable error__21886__auto__))))))
                  (inc ctr))))
            0
            (partition-all 2 1 (iter/iter-seq (log/seek-tx log 0)))))
        0)))
  (reset-meta!
    #'validate-t-order*
    (assoc
      {:arglists (clojure.core/list ['log 'progress]), :column (int 1)}
      :name
      'validate-t-order*
      :ns
      *ns*))
  (defn validate-t-order
    ([uri log_fn progress]
      (let [m_22260 {:event :integrity/validate-t-order, :log-fn log_fn}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22260 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (let [uri (enhance-uri uri)
                                          map__22264 (tools/connection-resources uri)
                                          map__22264 (if (seq? map__22264)
                                                       (if (next map__22264)
                                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                           (to-array map__22264))
                                                         (if (seq map__22264)
                                                           (first map__22264)
                                                           {}))
                                                       map__22264)
                                          cluster (get map__22264 :cluster)
                                          olookup (get map__22264 :olookup)
                                          temp__5802__auto__ (let
                                                               [G__22265 log_fn]
                                                               (case
                                                                 G__22265
                                                                 :create-log-val
                                                                 (log/create-log-val
                                                                   cluster
                                                                   olookup
                                                                   (d/db (d/connect uri)))
                                                                 :find-log
                                                                 (log/find-log cluster olookup)))]
                                      (if temp__5802__auto__
                                        (let [log temp__5802__auto__]
                                          (validate-t-order* log progress))
                                        0))}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_22261 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_22262 (logger/format-as-msec (long elapsed_22261))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_22260 :msec msec_22262 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'validate-t-order
    (assoc
      {:arglists (clojure.core/list ['uri 'log-fn 'progress]), :column (int 1)}
      :name
      'validate-t-order
      :ns
      *ns*))
  (defn log-dir-entry-seq
    ([p__22273 t]
      (let [map__22274 p__22273
            map__22274 (if (seq? map__22274)
                         (if (next map__22274)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22274))
                           (if (seq map__22274) (first map__22274) {}))
                         map__22274)
            cluster (get map__22274 :cluster)
            olookup (get map__22274 :olookup)
            temp__5804__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
        (when temp__5804__auto__
          (let [tree_iter temp__5804__auto__] (mapcat identity (log/log-dir-seq tree_iter)))))))
  (reset-meta!
    #'log-dir-entry-seq
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 't]), :column (int 1)}
      :name
      'log-dir-entry-seq
      :ns
      *ns*))
  (defn log-seg-t-seq
    ([p__22277 t]
      (let [map__22278 p__22277
            map__22278 (if (seq? map__22278)
                         (if (next map__22278)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22278))
                           (if (seq map__22278) (first map__22278) {}))
                         map__22278)
            cluster (get map__22278 :cluster)
            olookup (get map__22278 :olookup)
            temp__5804__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
        (when temp__5804__auto__
          (let [tree_iter temp__5804__auto__]
            (map :t (mapcat identity (log/log-seg-seq tree_iter))))))))
  (reset-meta!
    #'log-seg-t-seq
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 't]), :column (int 1)}
      :name
      'log-seg-t-seq
      :ns
      *ns*))
  (defn crosscheck-dir-segs
    ([p__22282 progress]
      (let [map__22283 p__22282
            map__22283 (if (seq? map__22283)
                         (if (next map__22283)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22283))
                           (if (seq map__22283) (first map__22283) {}))
                         map__22283)
            cluster (get map__22283 :cluster)
            olookup (get map__22283 :olookup)
            m_22284 {:event :integrity/crosscheck-dir-segs}
            ___8552__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22284 :phase :begin))))
                              nil)
            start__8553__auto__ (java.lang.System/nanoTime)
            result__8554__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (log/find-log cluster olookup)]
                                      (when temp__5804__auto__
                                        (let [log temp__5804__auto__
                                              temp__5804__auto__ (log/seek-tx log 0)]
                                          (when temp__5804__auto__
                                            (let [tree_iter temp__5804__auto__]
                                              (loop [seq_22288 (seq (log/log-dir-seq tree_iter))
                                                     chunk_22289 nil
                                                     count_22290 0
                                                     i_22291 0]
                                                (if (< i_22291 count_22290)
                                                  (let [adir (.nth
                                                               ^clojure.lang.Indexed chunk_22289
                                                               (int i_22291))]
                                                    (when progress
                                                      (^clojure.lang.IFn progress adir))
                                                    (let [bad_dirs (seq
                                                                     (remove
                                                                       (fn
                                                                         fn__22293
                                                                         ([p1__22281#]
                                                                           (=
                                                                             (:dir-t p1__22281#)
                                                                             (:seg-t p1__22281#))))
                                                                       (dir-seg-info-seq
                                                                         adir
                                                                         olookup)))]
                                                      (when-not (not bad_dirs)
                                                        (let [form__21885__auto__
                                                              (clojure.core/list 'not 'bad-dirs)
                                                              error__21886__auto__
                                                              (ex-info
                                                                "Assertion failed, see ex-data for details"
                                                                {:bindings
                                                                 {'map__22283 map__22283,
                                                                  'start__8553__auto__
                                                                  (long start__8553__auto__),
                                                                  'olookup olookup,
                                                                  (.withMeta
                                                                    'chunk_22289
                                                                    {:tag 'clojure.lang.IChunk})
                                                                  chunk_22289,
                                                                  'progress progress,
                                                                  'log log,
                                                                  'adir adir,
                                                                  '___8552__auto__ ___8552__auto__,
                                                                  'count_22290 (long count_22290),
                                                                  'tree-iter tree_iter,
                                                                  'p__22282 p__22282,
                                                                  'bad-dirs bad_dirs,
                                                                  'seq_22288 seq_22288,
                                                                  'temp__5804__auto__
                                                                  temp__5804__auto__,
                                                                  'm_22284 m_22284,
                                                                  'i_22291 (long i_22291),
                                                                  'cluster cluster},
                                                                 :form form__21885__auto__})]
                                                          (if datomic.assert/*assert-handler*
                                                            (datomic.assert/*assert-handler*
                                                              error__21886__auto__)
                                                            (throw
                                                              ^java.lang.Throwable error__21886__auto__)))))
                                                    (recur
                                                      seq_22288
                                                      chunk_22289
                                                      count_22290
                                                      (inc i_22291)))
                                                  (let [temp__5804__auto__ (seq seq_22288)]
                                                    (when temp__5804__auto__
                                                      (let [seq_22288 temp__5804__auto__]
                                                        (if (chunked-seq? seq_22288)
                                                          (let [c__6065__auto__
                                                                (chunk-first seq_22288)]
                                                            (recur
                                                              (chunk-rest seq_22288)
                                                              c__6065__auto__
                                                              (int (count c__6065__auto__))
                                                              (int 0)))
                                                          (let [adir (first seq_22288)]
                                                            (when
                                                              progress
                                                              (^clojure.lang.IFn progress adir))
                                                            (let
                                                              [bad_dirs
                                                               (seq
                                                                 (remove
                                                                   (fn
                                                                     fn__22295
                                                                     ([p1__22281#]
                                                                       (=
                                                                         (:dir-t p1__22281#)
                                                                         (:seg-t p1__22281#))))
                                                                   (dir-seg-info-seq
                                                                     adir
                                                                     olookup)))]
                                                              (when-not
                                                                (not bad_dirs)
                                                                (let
                                                                  [form__21885__auto__
                                                                   (clojure.core/list
                                                                     'not
                                                                     'bad-dirs)
                                                                   error__21886__auto__
                                                                   (ex-info
                                                                     "Assertion failed, see ex-data for details"
                                                                     {:bindings
                                                                      {'map__22283 map__22283,
                                                                       'start__8553__auto__
                                                                       (long start__8553__auto__),
                                                                       'olookup olookup,
                                                                       (.withMeta
                                                                         'chunk_22289
                                                                         {:tag
                                                                          'clojure.lang.IChunk})
                                                                       chunk_22289,
                                                                       'progress progress,
                                                                       'log log,
                                                                       'adir adir,
                                                                       '___8552__auto__
                                                                       ___8552__auto__,
                                                                       'count_22290
                                                                       (long count_22290),
                                                                       'tree-iter tree_iter,
                                                                       'p__22282 p__22282,
                                                                       'bad-dirs bad_dirs,
                                                                       'seq_22288 seq_22288,
                                                                       'temp__5804__auto__
                                                                       temp__5804__auto__,
                                                                       'm_22284 m_22284,
                                                                       'i_22291 (long i_22291),
                                                                       'cluster cluster},
                                                                      :form form__21885__auto__})]
                                                                  (if
                                                                    datomic.assert/*assert-handler*
                                                                    (datomic.assert/*assert-handler*
                                                                      error__21886__auto__)
                                                                    (throw
                                                                      ^java.lang.Throwable error__21886__auto__)))))
                                                            (recur
                                                              (next seq_22288)
                                                              nil
                                                              0
                                                              0)))))))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8555__auto__
                                     {:threw t__8555__auto__}))
            elapsed_22285 (- (java.lang.System/nanoTime) start__8553__auto__)
            msec_22286 (logger/format-as-msec (long elapsed_22285))]
        (let [endmsg__8556__auto__ (merge
                                     (assoc m_22284 :msec msec_22286 :phase :end)
                                     (when (:threw result__8554__auto__)
                                       {:threw (class (:threw result__8554__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8556__auto__)))
          nil)
        (if (contains? result__8554__auto__ :returned)
          (:returned result__8554__auto__)
          (do (throw (:threw result__8554__auto__)) nil)))))
  (reset-meta!
    #'crosscheck-dir-segs
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 'progress]), :column (int 1)}
      :name
      'crosscheck-dir-segs
      :ns
      *ns*))
  (defn merge-seqs
    ([cmp s1 s2 s3 s4] (merge-seqs cmp s1 (merge-seqs cmp s2 s3 s4)))
    ([cmp s1 s2 s3] (merge-seqs cmp s1 (merge-seqs cmp s2 s3)))
    ([cmp s1 s2]
      (let [s1 (seq s1) s2 (seq s2)]
        (if (and s1 s2)
          (let [vec__22312 s1
                seq__22313 (seq vec__22312)
                first__22314 (first seq__22313)
                seq__22313 (next seq__22313)
                o1 first__22314
                m1 seq__22313
                vec__22315 s2
                seq__22316 (seq vec__22315)
                first__22317 (first seq__22316)
                seq__22316 (next seq__22316)
                o2 first__22317
                m2 seq__22316]
            (if (< (.compare ^java.util.Comparator cmp o1 o2) 0)
              (lazy-seq (cons o1 (merge-seqs cmp m1 s2)))
              (lazy-seq (cons o2 (merge-seqs cmp s1 m2)))))
          (or s1 s2)))))
  (reset-meta!
    #'merge-seqs
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'cmp {:tag 'java.util.Comparator}) 's1 's2]
         ['cmp 's1 's2 's3]
         ['cmp 's1 's2 's3 's4]),
       :column (int 1)}
      :name
      'merge-seqs
      :ns
      *ns*))
  (defn aevt-dquark-seq
    ([db d]
      (map
        (fn fn__22326 ([p1__22325#] (dissoc p1__22325# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__22331
              ([p__22330]
                (let [vec__22332 p__22330
                      index (nth vec__22332 (int 0) nil)
                      iter (nth vec__22332 (int 1) nil)]
                  (map
                    (fn fn__22336
                      ([p__22335]
                        (let [map__22337 p__22335
                              map__22337 (if (seq? map__22337)
                                           (if (next map__22337)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__22337))
                                             (if (seq map__22337) (first map__22337) {}))
                                           map__22337)
                              datom map__22337
                              e (get map__22337 :e)
                              a (get map__22337 :a)
                              v (get map__22337 :v)
                              tx (get map__22337 :tx)
                              added (get map__22337 :added)]
                          {:v v,
                           :index index,
                           :datom datom,
                           :attrid a,
                           :added added,
                           :part
                           (db/resolve-kw db (long (db/eid->part (long ^java.lang.Number e)))),
                           :eidx (long (db/eid->eidx (long ^java.lang.Number e))),
                           :t (long (d/tx->t tx)),
                           :a (db/resolve-kw db a)})))
                    (iter/iter-seq iter)))))
            [[:memidx (btset/seek (:aevt (:memidx db)) d)]
             [:indexing
              (btset/seek
                (let [temp__5804__auto__ (:indexing db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:aevt index))))
                d)]
             [:index
              (btset/seek
                (let [temp__5804__auto__ (:index db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:aevt index))))
                d)]
             [:mid-index
              (btset/seek
                (let [temp__5804__auto__ (:mid-index db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:aevt index))))
                d)]
             [:history
              (btset/seek
                (let [temp__5804__auto__ (:history db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:aevt index))))
                d)]])))))
  (reset-meta!
    #'aevt-dquark-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'datomic.db.Db}) 'd]), :column (int 1)}
      :name
      'aevt-dquark-seq
      :ns
      *ns*))
  (defn eavt-dquark-seq
    ([db d]
      (map
        (fn fn__22346 ([p1__22345#] (dissoc p1__22345# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__22351
              ([p__22350]
                (let [vec__22352 p__22350
                      index (nth vec__22352 (int 0) nil)
                      iter (nth vec__22352 (int 1) nil)]
                  (map
                    (fn fn__22356
                      ([p__22355]
                        (let [map__22357 p__22355
                              map__22357 (if (seq? map__22357)
                                           (if (next map__22357)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__22357))
                                             (if (seq map__22357) (first map__22357) {}))
                                           map__22357)
                              datom map__22357
                              e (get map__22357 :e)
                              a (get map__22357 :a)
                              v (get map__22357 :v)
                              tx (get map__22357 :tx)
                              added (get map__22357 :added)]
                          {:datom datom,
                           :index index,
                           :part
                           (db/resolve-kw db (long (db/eid->part (long ^java.lang.Number e)))),
                           :eidx (long (db/eid->eidx (long ^java.lang.Number e))),
                           :a (db/resolve-kw db a),
                           :v v,
                           :t (long (d/tx->t tx)),
                           :added added})))
                    (iter/iter-seq iter)))))
            [[:memidx (btset/seek (:eavt (:memidx db)) d)]
             [:indexing
              (btset/seek
                (let [temp__5804__auto__ (:indexing db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:eavt index))))
                d)]
             [:index
              (btset/seek
                (let [temp__5804__auto__ (:index db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:eavt index))))
                d)]
             [:mid-index
              (btset/seek
                (let [temp__5804__auto__ (:mid-index db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:eavt index))))
                d)]
             [:history
              (btset/seek
                (let [temp__5804__auto__ (:history db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:eavt index))))
                d)]])))))
  (reset-meta!
    #'eavt-dquark-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'datomic.db.Db}) 'd]), :column (int 1)}
      :name
      'eavt-dquark-seq
      :ns
      *ns*))
  (defn avet-dquark-seq
    ([db d]
      (map
        (fn fn__22366 ([p1__22365#] (dissoc p1__22365# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__22371
              ([p__22370]
                (let [vec__22372 p__22370
                      index (nth vec__22372 (int 0) nil)
                      iter (nth vec__22372 (int 1) nil)]
                  (map
                    (fn fn__22376
                      ([p__22375]
                        (let [map__22377 p__22375
                              map__22377 (if (seq? map__22377)
                                           (if (next map__22377)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__22377))
                                             (if (seq map__22377) (first map__22377) {}))
                                           map__22377)
                              datom map__22377
                              e (get map__22377 :e)
                              a (get map__22377 :a)
                              v (get map__22377 :v)
                              tx (get map__22377 :tx)
                              added (get map__22377 :added)]
                          {:datom datom,
                           :index index,
                           :part
                           (db/resolve-kw db (long (db/eid->part (long ^java.lang.Number e)))),
                           :eidx (long (db/eid->eidx (long ^java.lang.Number e))),
                           :a (db/resolve-kw db a),
                           :v v,
                           :t (long (d/tx->t tx)),
                           :added added})))
                    (iter/iter-seq iter)))))
            [[:memidx (btset/seek (:avet (:memidx db)) d)]
             [:indexing
              (btset/seek
                (let [temp__5804__auto__ (:indexing db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:avet index))))
                d)]
             [:index
              (btset/seek
                (let [temp__5804__auto__ (:index db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:avet index))))
                d)]
             [:mid-index
              (btset/seek
                (let [temp__5804__auto__ (:mid-index db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:avet index))))
                d)]
             [:history
              (btset/seek
                (let [temp__5804__auto__ (:history db)]
                  (when temp__5804__auto__ (let [index temp__5804__auto__] (:avet index))))
                d)]])))))
  (reset-meta!
    #'avet-dquark-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'datomic.db.Db}) 'd]), :column (int 1)}
      :name
      'avet-dquark-seq
      :ns
      *ns*))
  (defn validate-log-cli
    ([uri]
      (let [uri (enhance-uri uri)
            cr (tools/connection-resources uri)
            count (atom 0)
            progress (fn progress
                       ([p1__22385#]
                         (fn fn__22387
                           ([_ & more]
                             (when (zero? (mod (swap! count inc) p1__22385#))
                               (print ".")
                               (flush))))))]
        (print "\nValidating t order (find log)")
        (flush)
        (println (validate-t-order uri :find-log (^clojure.lang.IFn progress 100)))
        (print "\nValidating t order (create-log-val)")
        (flush)
        (println (validate-t-order uri :create-log-val (^clojure.lang.IFn progress 100)))
        (print "\nCrosschecking dir segs")
        (flush)
        (crosscheck-dir-segs cr (^clojure.lang.IFn progress 100)))))
  (reset-meta!
    #'validate-log-cli
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'validate-log-cli
      :ns
      *ns*))
  (defn validate-memory-safe
    ([uri]
      (let [uri (enhance-uri uri)]
        (validate-log-cli uri)
        (let [conn (d/connect uri)] (crosscheck-log-cli {:uri uri})))))
  (reset-meta!
    #'validate-memory-safe
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'validate-memory-safe
      :ns
      *ns*))
  (defn excisions
    ([db] (seq (map (fn fn__22392 ([d] (d/entity db (:e d)))) (d/datoms db :aevt :db/excise)))))
  (reset-meta!
    #'excisions
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'excisions :ns *ns*))
  (defn e-ts
    ([db e]
      (sort
        (distinct
          (map
            (fn fn__22396 ([p1__22395#] (long (d/tx->t (:tx p1__22395#)))))
            (d/datoms db :eavt e))))))
  (reset-meta!
    #'e-ts
    (assoc {:arglists (clojure.core/list ['db 'e]), :column (int 1)} :name 'e-ts :ns *ns*))
  (defn validate-excision
    ([spec]
      (let [db (d/entity-db spec)
            id (fn id ([p1__22399#] (or (:db/id p1__22399#) (db/resolve-id db p1__22399#))))
            target (:db/excise spec)
            before_t (let [temp__5802__auto__ (excise/get-before-t db spec)]
                       (if temp__5802__auto__
                         (let [bt temp__5802__auto__] (dec bt))
                         (d/basis-t db)))
            spec_t (first (e-ts db (:db/id spec)))
            t (min (min before_t spec_t) (:indexBasisT db))
            valdb (d/as-of db t)
            type (if (db/attribute db (^clojure.lang.IFn id target)) :a :e)]
        (let [G__22403 type]
          (case
            G__22403
            :a
            (let [datoms (seq (d/datoms valdb :aevt (^clojure.lang.IFn id target)))]
              (when-not (nil? datoms)
                (throw
                  (ex-info
                    "Found datoms that should have been excised"
                    {:spec spec, :datoms datoms, :t t}))
                (clojure.lang.Util/hash G__22403)))
            :e
            (let [ent (d/entity valdb (^clojure.lang.IFn id target))
                  attrs (into #{} (:db.excise/attrs spec))]
              (if (seq attrs)
                (do
                  (when (some attrs (keys ent))
                    (throw
                      (ex-info
                        "Found entity attributes that should have been excised"
                        {:spec spec, :attrs attrs, :entity ent, :t t})))
                  nil)
                (do
                  (when (seq (keys ent))
                    (throw
                      (ex-info
                        "Found entity that should have been excised"
                        {:spec spec, :entity ent, :t t})))
                  nil)))))
        spec)))
  (reset-meta!
    #'validate-excision
    (assoc
      {:arglists (clojure.core/list ['spec]), :column (int 1)}
      :name
      'validate-excision
      :ns
      *ns*))
  (defn validate-indexed-excisions
    ([db]
      (dorun
        (map
          (fn fn__22407 ([spec] (validate-excision spec) (print ".") (flush) spec))
          (take-while
            (fn fn__22409
              ([p1__22406#] (<= (first (e-ts db (:db/id p1__22406#))) (:indexBasisT db))))
            (excisions db))))))
  (reset-meta!
    #'validate-indexed-excisions
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'validate-indexed-excisions
      :ns
      *ns*))
  (defn clusterfs-path-reachability
    ([cfs olookup progress]
      (mapcat
        (fn fn__22413
          ([p__22412]
            (let [vec__22414 p__22412
                  filename (nth vec__22414 (int 0) nil)
                  map__22417 (nth vec__22414 (int 1) nil)
                  map__22417 (if (seq? map__22417)
                               (if (next map__22417)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__22417))
                                 (if (seq map__22417) (first map__22417) {}))
                               map__22417)
                  base (get map__22417 :base)
                  length (get map__22417 :length)]
              (map
                (fn fn__22418
                  ([ckey]
                    (let [item [filename ckey (boolean (get olookup ckey))]]
                      (when progress (^clojure.lang.IFn progress item))
                      item)))
                (clusterfs/file-chunk-keys cfs filename)))))
        (:dir cfs))))
  (reset-meta!
    #'clusterfs-path-reachability
    (assoc
      {:arglists (clojure.core/list ['cfs 'olookup 'progress]), :column (int 1)}
      :name
      'clusterfs-path-reachability
      :ns
      *ns*))
  (defn fulltext-path-reachability
    ([ft db olookup progress]
      (when ft
        (mapcat
          (fn fn__22423
            ([p__22422]
              (let [vec__22424 p__22422
                    attrid (nth vec__22424 (int 0) nil)
                    uuid (nth vec__22424 (int 1) nil)
                    temp__5802__auto__ (get olookup (str uuid))]
                (if temp__5802__auto__
                  (let [cfs temp__5802__auto__]
                    (cons
                      [(d/ident db attrid) (str uuid) true]
                      (clusterfs-path-reachability cfs olookup progress)))
                  [(d/ident db attrid) (str uuid) false]))))
          (:attrmap ft)))))
  (reset-meta!
    #'fulltext-path-reachability
    (assoc
      {:arglists (clojure.core/list ['ft 'db 'olookup 'progress]), :column (int 1)}
      :name
      'fulltext-path-reachability
      :ns
      *ns*))
  (defn validate-fulltext
    ([db olookup]
      (let [count (atom 0)
            progress (fn progress
                       ([& _] (when (zero? (mod (swap! count inc) 100)) (print ".") (flush))))
            temp__5804__auto__ (seq
                                 (remove
                                   (fn fn__22433 ([p1__22430#] (nth p1__22430# (int 2))))
                                   (-> (:index db)
                                    (:fulltext)
                                    (:root)
                                    (fulltext-path-reachability db olookup progress)
                                    (:history db)
                                    (:fulltext)
                                    (:root)
                                    (fulltext-path-reachability db olookup progress)
                                    (concat))))]
        (when temp__5804__auto__
          (let [missing temp__5804__auto__]
            (throw (ex-info "Some fulltext paths are missing" {:paths missing}))))
        nil)))
  (reset-meta!
    #'validate-fulltext
    (assoc
      {:arglists (clojure.core/list ['db 'olookup]), :column (int 1)}
      :name
      'validate-fulltext
      :ns
      *ns*))
  (defn tx-range-ts ([conn] (map :t (d/tx-range (d/log conn) 1000 (d/next-t (d/db conn))))))
  (reset-meta!
    #'tx-range-ts
    (assoc {:arglists (clojure.core/list ['conn]), :column (int 1)} :name 'tx-range-ts :ns *ns*))
  (defn tx-instant-ts
    ([db]
      (drop-while
        (fn fn__22439 ([p1__22438#] (< p1__22438# 1000)))
        (map (comp d/tx->t :tx) (d/datoms db :avet :db/txInstant)))))
  (reset-meta!
    #'tx-instant-ts
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'tx-instant-ts :ns *ns*))
  (defn seq-diffs
    ([colla collb progress n]
      (let [G__22448 (seq colla)
            vec__22450 G__22448
            seq__22451 (seq vec__22450)
            first__22452 (first seq__22451)
            seq__22451 (next seq__22451)
            a first__22452
            morea seq__22451
            G__22449 (seq collb)
            vec__22453 G__22449
            seq__22454 (seq vec__22453)
            first__22455 (first seq__22454)
            seq__22454 (next seq__22454)
            b first__22455
            moreb seq__22454
            n n]
        (loop [G__22448 G__22448 G__22449 G__22449 n n]
          (let [vec__22456 G__22448
                seq__22457 (seq vec__22456)
                first__22458 (first seq__22457)
                seq__22457 (next seq__22457)
                a first__22458
                morea seq__22457
                vec__22459 G__22449
                seq__22460 (seq vec__22459)
                first__22461 (first seq__22460)
                seq__22460 (next seq__22460)
                b first__22461
                moreb seq__22460
                n n]
            (^clojure.lang.IFn progress n)
            (when (or a b)
              (if (= a b)
                (recur morea moreb (inc n))
                (lazy-seq (cons {:a a, :b b, :n n} (seq-diffs morea moreb progress (inc n))))))))))
    ([colla collb progress] (seq-diffs colla collb progress 0)))
  (reset-meta!
    #'seq-diffs
    (assoc
      {:arglists (clojure.core/list ['colla 'collb 'progress] ['colla 'collb 'progress 'n]),
       :column (int 1)}
      :name
      'seq-diffs
      :ns
      *ns*))
  (defn validate-garbage
    ([cr progress]
      (let [temp__5802__auto__ (deref
                                 (cluster/get-ref
                                   (:cluster cr)
                                   (garbage/root-ref-key (:cluster cr))))]
        (if temp__5802__auto__
          (let [root_key temp__5802__auto__
                root (get (:olookup cr) (:key root_key))
                valid? (comp not empty?)
                result (progress-reduce
                         (fn fn__22466
                           ([n k]
                             (if (not (^clojure.lang.IFn valid? k))
                               (reduced
                                 {:valid false,
                                  :desc "Invalid key in garbage leaf seq",
                                  :root-key root_key,
                                  :leaves n})
                               (inc n))))
                         0
                         {:progress progress, :n 1000}
                         (garbage/leaf-seq (:olookup cr) root))]
            (if (number? result)
              {:valid true, :desc "Valid garbage sequence", :root-key root_key, :leaves result}
              result))
          {:valid true, :root-key nil}))))
  (reset-meta!
    #'validate-garbage
    (assoc
      {:arglists (clojure.core/list ['cr 'progress]), :column (int 1)}
      :name
      'validate-garbage
      :ns
      *ns*))
  (defn crosscheck-tx-range-with-tx-instant
    ([conn]
      (let [count (atom 0)
            progress (fn progress
                       ([& _] (when (zero? (mod (swap! count inc) 10000)) (print ".") (flush))))
            temp__5804__auto__ (seq-diffs (tx-range-ts conn) (tx-instant-ts (d/db conn)) progress)]
        (when temp__5804__auto__
          (let [diffs temp__5804__auto__]
            (throw
              (ex-info
                "tx-range and :db/txInstant did not agree on ts."
                {:desc ":a values are from tx-range, :b values from :db/txInstant, :n offset",
                 :diffs diffs}))))
        nil)))
  (reset-meta!
    #'crosscheck-tx-range-with-tx-instant
    (assoc
      {:arglists (clojure.core/list ['conn]), :column (int 1)}
      :name
      'crosscheck-tx-range-with-tx-instant
      :ns
      *ns*))
  (defn validate-nohistory
    ([db]
      (let [hdb (cauterize (d/history db) :memidx :indexing :mid-index :index)
            temp__5802__auto__ (seq (attr-datoms db (nohistory-attrs db)))]
        (when temp__5802__auto__
          (let [datoms temp__5802__auto__]
            (throw (ex-info "Found :db/noHistory datoms in the history index" {:datoms datoms}))))
        nil)))
  (reset-meta!
    #'validate-nohistory
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'validate-nohistory
      :ns
      *ns*))
  (defn stat-counts
    ([db a]
      (let [aevt (stats/aevt db (:index db))
            aevt_mid (stats/aevt db (:mid-index db))
            aevt_hist (stats/aevt db (:history db))
            avet (stats/avet db (:index db))
            avet_mid (stats/avet db (:mid-index db))
            avet_hist (stats/avet db (:history db))
            attr (d/attribute db a)
            kw (:ident attr)
            aevtc (:data-count (^clojure.lang.IFn kw aevt))
            aevt_midc (:data-count (^clojure.lang.IFn kw aevt_mid))
            aevt_histc (:data-count (^clojure.lang.IFn kw aevt_hist))
            avetc (:data-count (^clojure.lang.IFn kw avet))
            avet_midc (:data-count (^clojure.lang.IFn kw avet_mid))
            avet_histc (:data-count (^clojure.lang.IFn kw avet_hist))]
        {:aevt-total (+ (+ (or aevtc 0) (or aevt_midc 0)) (or aevt_histc 0)),
         :aevt aevtc,
         :avet-total (+ (+ (or avetc 0) (or avet_midc 0)) (or avet_histc 0)),
         :avet avetc,
         :avet-mid avet_midc,
         :kw kw,
         :avet-hist avet_histc,
         :aevt-mid aevt_midc,
         :aevt-hist aevt_histc,
         :attr a})))
  (reset-meta!
    #'stat-counts
    (assoc {:arglists (clojure.core/list ['db 'a]), :column (int 1)} :name 'stat-counts :ns *ns*))
  (defn aevt-avet-stats
    ([db]
      (let [nohists (tools/ever-nohistory-attrs db)]
        (map
          (fn fn__22486 ([p1__22485#] (stat-counts db (.id ^datomic.db.Attribute p1__22485#))))
          (remove
            (fn fn__22488
              ([p1__22484#] (contains? nohists (.id ^datomic.db.Attribute p1__22484#))))
            (filter
              (fn fn__22490 ([p1__22483#] (.hasAVET ^datomic.db.Attribute p1__22483#)))
              (db/attribute-seq db)))))))
  (reset-meta!
    #'aevt-avet-stats
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'aevt-avet-stats :ns *ns*))
  (defn aevt-avet-stats-consistent?
    ([p__22493]
      (let [map__22494 p__22493
            map__22494 (if (seq? map__22494)
                         (if (next map__22494)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22494))
                           (if (seq map__22494) (first map__22494) {}))
                         map__22494)
            aevt_total (get map__22494 :aevt-total)
            avet_total (get map__22494 :avet-total)]
        (= aevt_total avet_total))))
  (reset-meta!
    #'aevt-avet-stats-consistent?
    (assoc
      {:arglists (clojure.core/list [{:keys ['aevt-total 'avet-total]}]), :column (int 1)}
      :name
      'aevt-avet-stats-consistent?
      :ns
      *ns*))
  (defn report-aevt-avet-stats
    ([db]
      (let [temp__5804__auto__ (seq (remove aevt-avet-stats-consistent? (aevt-avet-stats db)))]
        (when temp__5804__auto__
          (let [mismatch temp__5804__auto__] (prn {:stats-mismatch mismatch}))))))
  (reset-meta!
    #'report-aevt-avet-stats
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'report-aevt-avet-stats
      :ns
      *ns*))
  (defn validate-index-totals
    ([db]
      (let [eavt (stats/datom-counts stats/eavt db)
            aevt (stats/datom-counts stats/aevt db)
            summary {:eavt eavt, :aevt aevt}
            total_datoms (fn total_datoms
                           ([p1__22498#]
                             (apply
                               +
                               (vals
                                 (select-keys
                                   p1__22498#
                                   [:index-datoms :mid-index-datoms :history-datoms])))))
            eavt (assoc eavt :total-datoms (^clojure.lang.IFn total_datoms eavt))
            aevt (assoc aevt :total-datoms (^clojure.lang.IFn total_datoms aevt))]
        (if (= (:total-datoms eavt) (:total-datoms aevt))
          summary
          (do (throw (ex-info (str "total datoms not equal" summary) summary)) nil)))))
  (reset-meta!
    #'validate-index-totals
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'validate-index-totals
      :ns
      *ns*))
  (defn validate-all
    ([uri]
      (let [uri (enhance-uri uri)
            cr (tools/connection-resources uri)
            conn (d/connect uri)
            db (d/db conn)]
        (println
          "Log dir count"
          (java.lang.Integer/valueOf (int (count (log-dir-entry-seq cr 0)))))
        (report-aevt-avet-stats db)
        (validate-dir-sorts db)
        (validate-log-cli uri)
        (crosscheck-log-cli {:uri uri})
        (println "\nCrosschecking log representations")
        (println (select-keys (crosscheck-log-representations uri) [:basis-t :index-basis-t]))
        (when-not (nil? (ic/unique-collisions db identity))
          (let [form__21885__auto__ (clojure.core/list
                                      'nil?
                                      (clojure.core/list 'ic/unique-collisions 'db 'identity))
                error__21886__auto__ (ex-info
                                       "Assertion failed, see ex-data for details"
                                       {:bindings {'uri uri, 'cr cr, 'conn conn, 'db db},
                                        :form form__21885__auto__})]
            (if datomic.assert/*assert-handler*
              (datomic.assert/*assert-handler* error__21886__auto__)
              (throw ^java.lang.Throwable error__21886__auto__))))
        (crosscheck-indexes db)
        (print "\nValidating fulltext trees")
        (validate-fulltext db (peer/get-olookup conn))
        (print "\nValidating excisions")
        (validate-indexed-excisions db)
        (print "\nValidating tx-range")
        (crosscheck-tx-range-with-tx-instant conn)
        (print "\nValidating garbage")
        (let [gval (validate-garbage cr progress-dot)]
          (if (:valid gval) (println gval) (throw (ex-info "Invalid garbage seq" gval))))
        (prn))))
  (reset-meta!
    #'validate-all
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'validate-all :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.integrity" "diagnostics") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.integrity" "diagnostics") tools/diagnostics)
  (defn describe-segment
    ([uri segment_id]
      (let [uri (enhance-uri uri)
            cr (tools/connection-resources uri)
            buf (:buf (deref (cluster/get-val (:cluster cr) (str segment_id))))]
        (dio/describe-bbuf buf))))
  (reset-meta!
    #'describe-segment
    (assoc
      {:arglists (clojure.core/list ['uri 'segment-id]), :column (int 1)}
      :name
      'describe-segment
      :ns
      *ns*))
  (defn fulltext-storage-seq
    ([uri k]
      (let [uri (enhance-uri uri)
            map__22507 (tools/connection-resources uri)
            map__22507 (if (seq? map__22507)
                         (if (next map__22507)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22507))
                           (if (seq map__22507) (first map__22507) {}))
                         map__22507)
            cluster (get map__22507 :cluster)
            olookup (get map__22507 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)]
        (when root_id
          (let [root (get olookup root_id)
                branch? (fn branch_QMARK_
                          ([p__22508]
                            (let [map__22510 p__22508
                                  map__22510 (if (seq? map__22510)
                                               (if (next map__22510)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__22510))
                                                 (if (seq map__22510) (first map__22510) {}))
                                               map__22510)
                                  seg (get map__22510 :seg)]
                              (or
                                (instance? datomic.fulltext.Root seg)
                                (instance? datomic.clusterfs.ClusterFS seg)))))
                children (fn children
                           ([p__22513]
                             (let [map__22515 p__22513
                                   map__22515 (if (seq? map__22515)
                                                (if (next map__22515)
                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                    (to-array map__22515))
                                                  (if (seq map__22515) (first map__22515) {}))
                                                map__22515)
                                   seg (get map__22515 :seg)]
                               (cond
                                 (instance? datomic.fulltext.Root seg) (map
                                                                         (fn
                                                                           fn__22516
                                                                           ([p1__22506#]
                                                                             (let
                                                                               [uuid
                                                                                (str p1__22506#)]
                                                                               {:type :clusterfs,
                                                                                :uuid uuid,
                                                                                :seg
                                                                                (get
                                                                                  olookup
                                                                                  uuid)})))
                                                                         (vals (:attrmap seg)))
                                 (instance? datomic.clusterfs.ClusterFS seg) (do
                                                                               (map
                                                                                 (fn
                                                                                   fn__22518
                                                                                   ([k]
                                                                                     {:type :chunk,
                                                                                      :path k,
                                                                                      :seg
                                                                                      (get
                                                                                        olookup
                                                                                        k)}))
                                                                                 (clusterfs/all-keys
                                                                                   seg)))))))]
            (tree-seq branch? children {:type :root, :uuid root_id, :seg root}))))))
  (reset-meta!
    #'fulltext-storage-seq
    (assoc
      {:arglists (clojure.core/list ['uri 'k]), :column (int 1)}
      :name
      'fulltext-storage-seq
      :ns
      *ns*))
  (defn log-storage-seq
    ([uri]
      (let [uri (enhance-uri uri)
            map__22522 (tools/connection-resources uri)
            map__22522 (if (seq? map__22522)
                         (if (next map__22522)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22522))
                           (if (seq map__22522) (first map__22522) {}))
                         map__22522)
            cluster (get map__22522 :cluster)
            olookup (get map__22522 :olookup)
            root_id (log/root-id cluster)
            branch? (fn branch_QMARK_
                      ([p__22523]
                        (let [map__22525 p__22523
                              map__22525 (if (seq? map__22525)
                                           (if (next map__22525)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__22525))
                                             (if (seq map__22525) (first map__22525) {}))
                                           map__22525)
                              seg (get map__22525 :seg)]
                          (instance? datomic.log.LogDir (first seg)))))
            children (fn children
                       ([p__22527]
                         (let [map__22529 p__22527
                               map__22529 (if (seq? map__22529)
                                            (if (next map__22529)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__22529))
                                              (if (seq map__22529) (first map__22529) {}))
                                            map__22529)
                               uuid (get map__22529 :uuid)
                               seg (get map__22529 :seg)]
                           (map
                             (fn fn__22530
                               ([entry]
                                 (let [subseg (get olookup (:uuid entry))]
                                   (assoc
                                     entry
                                     :type
                                     (if (= uuid root_id) :branch :leaf)
                                     :seg
                                     subseg))))
                             seg))))
            root (get olookup root_id)]
        (tree-seq branch? children {:type :root, :uuid root_id, :seg root}))))
  (reset-meta!
    #'log-storage-seq
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'log-storage-seq
      :ns
      *ns*))
  (defn index-storage-seq
    ([uri k]
      (let [uri (enhance-uri uri)
            map__22534 (tools/connection-resources uri)
            map__22534 (if (seq? map__22534)
                         (if (next map__22534)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22534))
                           (if (seq map__22534) (first map__22534) {}))
                         map__22534)
            cluster (get map__22534 :cluster)
            olookup (get map__22534 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)
            branch? (fn branch_QMARK_
                      ([p__22535]
                        (let [map__22537 p__22535
                              map__22537 (if (seq? map__22537)
                                           (if (next map__22537)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__22537))
                                             (if (seq map__22537) (first map__22537) {}))
                                           map__22537)
                              seg (get map__22537 :seg)]
                          (or
                            (instance? datomic.index.RootNode seg)
                            (instance? datomic.index.DirNode seg)))))
            children (fn children
                       ([p__22540]
                         (let [map__22542 p__22540
                               map__22542 (if (seq? map__22542)
                                            (if (next map__22542)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__22542))
                                              (if (seq map__22542) (first map__22542) {}))
                                            map__22542)
                               seg (get map__22542 :seg)]
                           (cond
                             (instance? datomic.index.RootNode seg) (map
                                                                      (fn
                                                                        fn__22543
                                                                        ([id]
                                                                          {:type :branch,
                                                                           :uuid id,
                                                                           :seg
                                                                           (get
                                                                             olookup
                                                                             (str id))}))
                                                                      (.-dirids
                                                                        ^datomic.index.RootNode seg))
                             (instance? datomic.index.DirNode seg) (do
                                                                     (map
                                                                       (fn
                                                                         fn__22545
                                                                         ([id]
                                                                           {:type :leaf,
                                                                            :uuid id,
                                                                            :seg
                                                                            (get
                                                                              olookup
                                                                              (str id))}))
                                                                       (.-segids
                                                                         ^datomic.index.DirNode seg)))))))]
        (when root_id
          (tree-seq branch? children {:type :root, :uuid root_id, :seg (get olookup root_id)})))))
  (reset-meta!
    #'index-storage-seq
    (assoc
      {:arglists (clojure.core/list ['uri 'k]), :column (int 1)}
      :name
      'index-storage-seq
      :ns
      *ns*))
  (defn pod-storage-seq
    ([uri]
      (let [uri (enhance-uri uri)
            map__22549 (tools/connection-resources uri)
            map__22549 (if (seq? map__22549)
                         (if (next map__22549)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22549))
                           (if (seq map__22549) (first map__22549) {}))
                         map__22549)
            cluster (get map__22549 :cluster)
            olookup (get map__22549 :olookup)
            pod_key (log/tail-pod-key cluster)
            pod_meta (deref (cluster/get-pod-meta cluster pod_key))
            mkv (fn mkv
                  ([id]
                    (let [temp__5802__auto__ (kvs/get (cluster/get-ref-store cluster) id false)]
                      (if temp__5802__auto__
                        (let [entry temp__5802__auto__]
                          (update-in
                            entry
                            [:v]
                            (fn fn__22551
                              ([bb]
                                (when bb (fressian/defressian bb :handlers log/read-handlers))))))
                        {:id id}))))]
        (cons
          (if pod_meta (assoc pod_meta :type :pod-key) {:id pod_key})
          (when pod_meta
            (take-while
              identity
              (iterate
                (fn fn__22556
                  ([p__22555]
                    (let [map__22557 p__22555
                          map__22557 (if (seq? map__22557)
                                       (if (next map__22557)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__22557))
                                         (if (seq map__22557) (first map__22557) {}))
                                       map__22557)
                          prev (get map__22557 :prev)]
                      (when prev (^clojure.lang.IFn mkv prev)))))
                (^clojure.lang.IFn mkv (:tail pod_meta)))))))))
  (reset-meta!
    #'pod-storage-seq
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'pod-storage-seq
      :ns
      *ns*))
  (def esafe
   (fn esafe
     ([&form &env & body]
       (seq
         (concat
           (clojure.core/list 'try)
           body
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'catch)
                 (clojure.core/list 'java.lang.Throwable)
                 (clojure.core/list 't__22560__auto__)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list '.printStackTrace)
                       (clojure.core/list 't__22560__auto__))))))))))))
  (reset-meta!
    #'esafe
    (assoc {:arglists (clojure.core/list ['& 'body]), :column (int 1)} :name 'esafe :ns *ns*))
  (.setMacro #'esafe)
  (def index-sort-root-keys
   [:aevt-hist
    :aevt
    :aevt-mid
    :aevt-main
    :eavt-hist
    :eavt
    :eavt-mid
    :eavt-main
    :raet-hist
    :raet
    :raet-mid
    :raet-main
    :avet-hist
    :avet
    :avet-mid
    :avet-main])
  (reset-meta!
    #'index-sort-root-keys
    (assoc {:column (int 1)} :name 'index-sort-root-keys :ns *ns*))
  (defn -main*
    ([p__22563]
      (let [map__22564 p__22563
            map__22564 (if (seq? map__22564)
                         (if (next map__22564)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__22564))
                           (if (seq map__22564) (first map__22564) {}))
                         map__22564)
            uri (get map__22564 :uri)
            validate (get map__22564 :validate)]
        (let [uri (enhance-uri uri)]
          (println "\nDiagnostics:")
          (pp/pprint
            (try
              (diagnostics uri)
              (catch
                java.lang.Throwable
                t__22560__auto__
                (do (.printStackTrace ^java.lang.Throwable t__22560__auto__) nil)))))
        (when validate
          (let [uri (enhance-uri uri)]
            (println
              (str
                "\nValidating database at "
                uri
                ".\nThis read-only process will read every segment of the database,\nreporting data to stdout and stacktraces to stderr.\nValidation can take a long time!"))
            (println "\nMissing log tail identities:")
            (prn
              (seq
                (try
                  (filter
                    (fn fn__22568 ([p1__22562#] (= 1 (long (count p1__22562#)))))
                    (pod-storage-seq uri))
                  (catch
                    java.lang.Throwable
                    t__22560__auto__
                    (do (.printStackTrace ^java.lang.Throwable t__22560__auto__) nil)))))
            (println "\nMissing log segments: ")
            (prn
              (seq
                (try
                  (remove :seg (log-storage-seq uri))
                  (catch
                    java.lang.Throwable
                    t__22560__auto__
                    (do (.printStackTrace ^java.lang.Throwable t__22560__auto__) nil)))))
            (loop [seq_22573 (seq index-sort-root-keys) chunk_22574 nil count_22575 0 i_22576 0]
              (if (< i_22576 count_22575)
                (let [k (.nth ^clojure.lang.Indexed chunk_22574 (int i_22576))]
                  (println "\nMissing segments in " k)
                  (prn
                    (seq
                      (try
                        (remove :seg (index-storage-seq uri k))
                        (catch
                          java.lang.Throwable
                          t__22560__auto__
                          (do (.printStackTrace ^java.lang.Throwable t__22560__auto__) nil)))))
                  (recur seq_22573 chunk_22574 count_22575 (inc i_22576)))
                (let [temp__5804__auto__ (seq seq_22573)]
                  (when temp__5804__auto__
                    (let [seq_22573 temp__5804__auto__]
                      (if (chunked-seq? seq_22573)
                        (let [c__6065__auto__ (chunk-first seq_22573)]
                          (recur
                            (chunk-rest seq_22573)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [k (first seq_22573)]
                          (println "\nMissing segments in " k)
                          (prn
                            (seq
                              (try
                                (remove :seg (index-storage-seq uri k))
                                (catch
                                  java.lang.Throwable
                                  t__22560__auto__
                                  (do
                                    (.printStackTrace ^java.lang.Throwable t__22560__auto__)
                                    nil)))))
                          (recur (next seq_22573) nil 0 0))))))))
            (loop [seq_22581 (seq [:fulltext :fulltext-hist])
                   chunk_22582 nil
                   count_22583 0
                   i_22584 0]
              (if (< i_22584 count_22583)
                (let [k (.nth ^clojure.lang.Indexed chunk_22582 (int i_22584))]
                  (println "\nMissing segments in " k)
                  (prn
                    (seq
                      (try
                        (remove :seg (fulltext-storage-seq uri k))
                        (catch
                          java.lang.Throwable
                          t__22560__auto__
                          (do (.printStackTrace ^java.lang.Throwable t__22560__auto__) nil)))))
                  (recur seq_22581 chunk_22582 count_22583 (inc i_22584)))
                (let [temp__5804__auto__ (seq seq_22581)]
                  (when temp__5804__auto__
                    (let [seq_22581 temp__5804__auto__]
                      (if (chunked-seq? seq_22581)
                        (let [c__6065__auto__ (chunk-first seq_22581)]
                          (recur
                            (chunk-rest seq_22581)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [k (first seq_22581)]
                          (println "\nMissing segments in " k)
                          (prn
                            (seq
                              (try
                                (remove :seg (fulltext-storage-seq uri k))
                                (catch
                                  java.lang.Throwable
                                  t__22560__auto__
                                  (do
                                    (.printStackTrace ^java.lang.Throwable t__22560__auto__)
                                    nil)))))
                          (recur (next seq_22581) nil 0 0))))))))
            (println "\nDone!"))))))
  (reset-meta!
    #'-main*
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri 'validate]}]), :column (int 1)}
      :name
      '-main*
      :ns
      *ns*))
  (defn -main
    ([& args]
      (try
        (-main*
          (cli/parse-or-exit!
            "datomic.integrity"
            args
            #{{:long-name :validate,
               :short-name :v,
               :doc "Validate all segments (pulls entire db through memory)",
               :default nil}
              {:long-name :uri, :required true, :doc "Database URI"}}
            [:uri]))
        (finally (d/shutdown true)))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))
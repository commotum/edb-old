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
  (let [v__5813__auto__ #'enhance-uri]
    (when-not (and
                (.hasRoot ^clojure.lang.Var v__5813__auto__)
                (instance? clojure.lang.MultiFn (deref v__5813__auto__)))
      (.setMeta (clojure.lang.RT/var "datomic.integrity" "enhance-uri") {:column (int 1)})
      (.bindRoot
        (clojure.lang.RT/var "datomic.integrity" "enhance-uri")
        (clojure.lang.MultiFn.
          "enhance-uri"
          (fn fn__20760 ([uri] (:protocol (uri/parse uri))))
          :default
          #'clojure.core/global-hierarchy))
      #'enhance-uri))
  (defmethod enhance-uri :default fn__20765 ([uri] uri))
  (defmethod
    enhance-uri
    :ddb+s3
    fn__20767
    ([uri] (uri/create (merge (uri/parse uri) {:skip-efs true}))))
  (def progress-reduce
   (fn progress_reduce
     ([f val p__20769 coll]
       (let [map__20770 p__20769
             map__20770 (if (seq? map__20770)
                          (if (next map__20770)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20770))
                            (if (seq map__20770) (first map__20770) {}))
                          map__20770)
             progress (get map__20770 :progress)
             n (get map__20770 :n)]
         (if progress
           (let [pf (fn pf
                      ([p__20771 item]
                        (let [vec__20773 p__20771
                              c (nth vec__20773 (int 0) nil)
                              acc (nth vec__20773 (int 1) nil)]
                          (when (zero? (mod c n)) (^clojure.lang.IFn progress acc n))
                          [(inc c) (^clojure.lang.IFn f acc item)])))]
             (second (reduce pf [0 val] coll)))
           (reduce f val coll))))))
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
            (fn fn__20781 ([p1__20780#] (.-noHistory ^datomic.db.Attribute p1__20780#)))
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
  (def restatement?
   (fn restatement_QMARK_
     ([p__20786]
       (let [vec__20787 p__20786 d1 (nth vec__20787 (int 0) nil) d2 (nth vec__20787 (int 1) nil)]
         (and
           (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
           (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
           (= (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))
           (= (boolean (.added ^datomic.Datom d1)) (boolean (.added ^datomic.Datom d2))))))))
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
              (fn fn__20795
                ([p__20794]
                  (let [vec__20796 p__20794 e (nth vec__20796 (int 0) nil)] (d/attribute db e))))
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
      (let [G__20801 index]
        (case
          G__20801
          :aevt
          (fn fn__20802 ([d] [(:a d) (:e d) (:v d) (:tx d)]))
          :avet
          (fn fn__20804 ([d] [(:a d) (:v d) (:e d) (:tx d)]))
          :eavt
          (fn fn__20806 ([d] [(:e d) (:a d) (:v d) (:tx d)]))
          :vaet
          (fn fn__20808 ([d] [(:v d) (:a d) (:e d) (:tx d)]))))))
  (reset-meta!
    #'make-tupler
    (assoc {:arglists (clojure.core/list ['index]), :column (int 1)} :name 'make-tupler :ns *ns*))
  (defn unfindable-datom-seq
    ([db index progress]
      (let [op (let [G__20820 index]
                 (case
                   G__20820
                   :aevt
                   (fn fn__20821
                     ([p1__20814# p2__20815#]
                       (.seekAEVT ^datomic.db.IDb p1__20814# ^datomic.impl.db.IDatum p2__20815#)))
                   :avet
                   (fn fn__20823
                     ([p1__20816# p2__20817#]
                       (.seekAVET ^datomic.db.IDb p1__20816# ^datomic.impl.db.IDatum p2__20817#)))
                   :eavt
                   (fn fn__20825
                     ([p1__20812# p2__20813#]
                       (.seekEAVT ^datomic.db.IDb p1__20812# ^datomic.impl.db.IDatum p2__20813#)))
                   :vaet
                   (fn fn__20827
                     ([p1__20818# p2__20819#]
                       (.seekRAET
                         ^datomic.db.IDb p1__20818#
                         ^datomic.impl.db.IDatum p2__20819#)))))]
        (remove
          (fn fn__20829
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
        (fn fn__20832 ([& _] (when (zero? (mod (swap! c inc) n)) (print ".") (flush)))))))
  (reset-meta!
    #'progress-dot-fn
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'progress-dot-fn :ns *ns*))
  (def datom-comparator
   (fn datom_comparator
     ([sort]
       (let [G__20835 sort]
         (case
           G__20835
           :aevt
           db/aevt-cmp
           :avet
           db/avet-cmp
           :eavt
           db/eavt-cmp
           :vaet
           db/raet-cmp)))))
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
          (fn fn__20837
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
      (let [tses (let [iter__6398__auto__ (fn iter__20841
                                            ([s__20842]
                                              (lazy-seq
                                                (loop [s__20842 s__20842]
                                                  (let [temp__5825__auto__ (seq s__20842)]
                                                    (when temp__5825__auto__
                                                      (let [xs__6385__auto__ temp__5825__auto__
                                                            tier (first xs__6385__auto__)
                                                            iterys__6394__auto__
                                                            (fn 
                                                              iter__20843
                                                              ([s__20844]
                                                                (lazy-seq
                                                                  (let 
                                                                    [s__20844 s__20844
                                                                     temp__5825__auto__
                                                                     (seq s__20844)]
                                                                    (when
                                                                      temp__5825__auto__
                                                                      (let 
                                                                        [s__20844
                                                                         temp__5825__auto__]
                                                                        (if
                                                                          (chunked-seq? s__20844)
                                                                          (let 
                                                                            [c__6396__auto__
                                                                             (chunk-first s__20844)
                                                                             size__6397__auto__
                                                                             (int
                                                                               (count
                                                                                 c__6396__auto__))
                                                                             b__20846
                                                                             (chunk-buffer
                                                                               (java.lang.Integer/valueOf
                                                                                 (int
                                                                                   size__6397__auto__)))]
                                                                            (if
                                                                              (loop 
                                                                                [i__20845 (int 0)]
                                                                                (if
                                                                                  (<
                                                                                    i__20845
                                                                                    size__6397__auto__)
                                                                                  (let 
                                                                                    [sort
                                                                                     (.nth
                                                                                       ^clojure.lang.Indexed c__6396__auto__
                                                                                       (int
                                                                                         i__20845))]
                                                                                    (chunk-append
                                                                                      b__20846
                                                                                      [tier sort])
                                                                                    (recur
                                                                                      (inc
                                                                                        i__20845)))
                                                                                  true))
                                                                              (chunk-cons
                                                                                (chunk b__20846)
                                                                                (^clojure.lang.IFn iter__20843
                                                                                  (chunk-rest
                                                                                    s__20844)))
                                                                              (chunk-cons
                                                                                (chunk b__20846)
                                                                                nil)))
                                                                          (let 
                                                                            [sort (first s__20844)]
                                                                            (cons
                                                                              [tier sort]
                                                                              (^clojure.lang.IFn iter__20843
                                                                                (rest
                                                                                  s__20844)))))))))))
                                                            fs__6395__auto__
                                                            (seq
                                                              (^clojure.lang.IFn iterys__6394__auto__
                                                                [:eavt :aevt :avet :vaet]))]
                                                        (if fs__6395__auto__
                                                          (concat
                                                            fs__6395__auto__
                                                            (^clojure.lang.IFn iter__20841
                                                              (rest s__20842)))
                                                          (recur (rest s__20842))))))))))]
                   (^clojure.lang.IFn iter__6398__auto__ [:mid-index :main :history]))]
        (print "Validating dirs for ")
        (loop [seq_20864 (seq tses) chunk_20865 nil count_20866 0 i_20867 0]
          (if (< i_20867 count_20866)
            (let [vec__20868 (.nth ^clojure.lang.Indexed chunk_20865 (int i_20867))
                  tier (nth vec__20868 (int 0) nil)
                  sort (nth vec__20868 (int 1) nil)]
              (print "[" tier sort "]")
              (let [temp__5825__auto__ (unsorted-dirs db tier sort (progress-dot-fn 10000))]
                (when temp__5825__auto__
                  (let [s temp__5825__auto__]
                    (println)
                    (throw
                      (ex-info
                        (str "Disorderly dirs in " tier sort (first s))
                        {:pairs s, :tier tier, :sort sort}))))
                nil)
              (recur seq_20864 chunk_20865 count_20866 (inc i_20867)))
            (let [temp__5825__auto__ (seq seq_20864)]
              (when temp__5825__auto__
                (let [seq_20864 temp__5825__auto__]
                  (if (chunked-seq? seq_20864)
                    (let [c__6090__auto__ (chunk-first seq_20864)]
                      (recur
                        (chunk-rest seq_20864)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [vec__20871 (first seq_20864)
                          tier (nth vec__20871 (int 0) nil)
                          sort (nth vec__20871 (int 1) nil)]
                      (print "[" tier sort "]")
                      (let [temp__5825__auto__ (unsorted-dirs
                                                 db
                                                 tier
                                                 sort
                                                 (progress-dot-fn 10000))]
                        (when temp__5825__auto__
                          (let [s temp__5825__auto__]
                            (println)
                            (throw
                              (ex-info
                                (str "Disorderly dirs in " tier sort (first s))
                                {:pairs s, :tier tier, :sort sort}))))
                        nil)
                      (recur (next seq_20864) nil 0 0))))))))
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
      (let [op (let [G__20880 order] (case G__20880 :allow-duplicates <= :strict <))
            cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__20881
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
  (def validate-index-sorts
   (fn validate_index_sorts
     ([db order]
       (loop [seq_20884 (seq [:eavt :aevt :avet :vaet]) chunk_20885 nil count_20886 0 i_20887 0]
         (if (< i_20887 count_20886)
           (let [sort (.nth ^clojure.lang.Indexed chunk_20885 (int i_20887))]
             (println "Validating " sort)
             (let [temp__5825__auto__ (unsorted-datoms db sort order (progress-dot-fn 10000))]
               (when temp__5825__auto__
                 (let [s temp__5825__auto__]
                   (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s}))))
               nil)
             (println)
             (recur seq_20884 chunk_20885 count_20886 (inc i_20887)))
           (let [temp__5825__auto__ (seq seq_20884)]
             (when temp__5825__auto__
               (let [seq_20884 temp__5825__auto__]
                 (if (chunked-seq? seq_20884)
                   (let [c__6090__auto__ (chunk-first seq_20884)]
                     (recur
                       (chunk-rest seq_20884)
                       c__6090__auto__
                       (int (count c__6090__auto__))
                       (int 0)))
                   (let [sort (first seq_20884)]
                     (println "Validating " sort)
                     (let [temp__5825__auto__ (unsorted-datoms
                                                db
                                                sort
                                                order
                                                (progress-dot-fn 10000))]
                       (when temp__5825__auto__
                         (let [s temp__5825__auto__]
                           (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s}))))
                       nil)
                     (println)
                     (recur (next seq_20884) nil 0 0)))))))))
     ([db] (validate-index-sorts db :strict))))
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
      (let [temp__5825__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5825__auto__
          (let [hist temp__5825__auto__]
            (tools/unsorted-seq
              (fn fn__20894
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
      (let [temp__5825__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5825__auto__
          (let [hist temp__5825__auto__]
            (tools/unsorted-seq
              (fn fn__20900
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
      (loop [seq_20905 (seq [:eavt :aevt :avet :vaet]) chunk_20906 nil count_20907 0 i_20908 0]
        (if (< i_20908 count_20907)
          (let [sort (.nth ^clojure.lang.Indexed chunk_20906 (int i_20908))]
            (println "Validating history pairs " sort)
            (let [temp__5825__auto__ (unpaired-history-assertions
                                       db
                                       sort
                                       (progress-dot-fn 100000))]
              (when temp__5825__auto__
                (let [s temp__5825__auto__]
                  (throw
                    (ex-info (str "Unpaired history assertions " (first s)) {:assertions s}))))
              nil)
            (println)
            (recur seq_20905 chunk_20906 count_20907 (inc i_20908)))
          (let [temp__5825__auto__ (seq seq_20905)]
            (when temp__5825__auto__
              (let [seq_20905 temp__5825__auto__]
                (if (chunked-seq? seq_20905)
                  (let [c__6090__auto__ (chunk-first seq_20905)]
                    (recur
                      (chunk-rest seq_20905)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [sort (first seq_20905)]
                    (println "Validating history pairs " sort)
                    (let [temp__5825__auto__ (unpaired-history-assertions
                                               db
                                               sort
                                               (progress-dot-fn 100000))]
                      (when temp__5825__auto__
                        (let [s temp__5825__auto__]
                          (throw
                            (ex-info
                              (str "Unpaired history assertions " (first s))
                              {:assertions s}))))
                      nil)
                    (println)
                    (recur (next seq_20905) nil 0 0))))))))))
  (reset-meta!
    #'validate-history-pairs
    (assoc
      {:arglists (clojure.core/list ['db]), :column (int 1)}
      :name
      'validate-history-pairs
      :ns
      *ns*))
  (def selfcheck-index-cli
   (fn selfcheck_index_cli
     ([p__20914]
       (let [map__20915 p__20914
             map__20915 (if (seq? map__20915)
                          (if (next map__20915)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20915))
                            (if (seq map__20915) (first map__20915) {}))
                          map__20915)
             uri (get map__20915 :uri)
             uri (enhance-uri uri)
             conn (d/connect uri)
             db (d/db conn)]
         (loop [seq_20916 (seq [:eavt :aevt :avet :vaet]) chunk_20917 nil count_20918 0 i_20919 0]
           (if (< i_20919 count_20918)
             (let [index (.nth ^clojure.lang.Indexed chunk_20917 (int i_20919))]
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
               (recur seq_20916 chunk_20917 count_20918 (inc i_20919)))
             (let [temp__5825__auto__ (seq seq_20916)]
               (when temp__5825__auto__
                 (let [seq_20916 temp__5825__auto__]
                   (if (chunked-seq? seq_20916)
                     (let [c__6090__auto__ (chunk-first seq_20916)]
                       (recur
                         (chunk-rest seq_20916)
                         c__6090__auto__
                         (int (count c__6090__auto__))
                         (int 0)))
                     (let [index (first seq_20916)]
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
                       (recur (next seq_20916) nil 0 0))))))))))))
  (reset-meta!
    #'selfcheck-index-cli
    (assoc
      {:arglists (clojure.core/list [{:keys ['uri]}]), :column (int 1)}
      :name
      'selfcheck-index-cli
      :ns
      *ns*))
  (defn mk-attr-pred ([s] (fn fn__20928 ([p1__20927#] (contains? s (:a p1__20927#))))))
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
      (let [G__20932 index]
        (case
          G__20932
          (:aevt :eavt)
          identity
          :avet
          (mk-attr-pred (attr-id-set db :has-avet))
          :raet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__20933 ([p1__20931#] (= :db.type/ref (:value-type p1__20931#))))))
          :vaet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__20935 ([p1__20931#] (= :db.type/ref (:value-type p1__20931#))))))))))
  (reset-meta!
    #'mk-index-pred
    (assoc
      {:arglists (clojure.core/list ['db 'index]), :column (int 1)}
      :name
      'mk-index-pred
      :ns
      *ns*))
  (def crosscheck-log
   (fn crosscheck_log
     ([log db index progress]
       (do
         (let [m_20939 {:event :integrity/crosscheck-log, :index index, :db (:id db)}
               ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger
                                              "datomic.integrity")]
                                 (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                   (.debug
                                     ^org.slf4j.Logger logger
                                     (logger/process (assoc m_20939 :phase :begin))))
                                 nil)
               start__8599__auto__ (java.lang.System/nanoTime)
               result__8600__auto__ (try
                                      {:returned nil}
                                      (catch
                                        java.lang.Throwable
                                        t__8601__auto__
                                        {:threw t__8601__auto__}))
               elapsed_20940 (- (java.lang.System/nanoTime) start__8599__auto__)
               msec_20941 (logger/format-as-msec (long elapsed_20940))]
           (let [endmsg__8602__auto__ (merge
                                        (assoc m_20939 :msec msec_20941 :phase :end)
                                        (when (:threw result__8600__auto__)
                                          {:threw (class (:threw result__8600__auto__))}))
                 logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
             (when (.isDebugEnabled ^org.slf4j.Logger logger)
               (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
             nil)
           (if (contains? result__8600__auto__ :returned)
             (:returned result__8600__auto__)
             (throw (:threw result__8600__auto__))))
         (let [temp__5823__auto__ (seq (iter/iter-seq (log/seek-tx log 0)))]
           (when temp__5823__auto__
             (let [log_seq temp__5823__auto__
                   tupler (make-tupler index)
                   nohists (tools/ever-nohistory-attrs db)
                   basis_t (d/basis-t db)
                   limit_tx (d/t->tx (long ^java.lang.Number basis_t))
                   hist (d/history db)
                   index_pred (mk-index-pred db index)]
               (reduce
                 (fn fn__20944
                   ([ctr tx]
                     (loop [seq_20945 (seq
                                        (take-while
                                          (fn fn__20949
                                            ([p1__20938#] (<= (:tx p1__20938#) limit_tx)))
                                          (:data tx)))
                            chunk_20946 nil
                            count_20947 0
                            i_20948 0]
                       (if (< i_20948 count_20947)
                         (let [d (.nth ^clojure.lang.Indexed chunk_20946 (int i_20948))]
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
                           (recur seq_20945 chunk_20946 count_20947 (inc i_20948)))
                         (let [temp__5825__auto__ (seq seq_20945)]
                           (when temp__5825__auto__
                             (let [seq_20945 temp__5825__auto__]
                               (if (chunked-seq? seq_20945)
                                 (let [c__6090__auto__ (chunk-first seq_20945)]
                                   (recur
                                     (chunk-rest seq_20945)
                                     c__6090__auto__
                                     (int (count c__6090__auto__))
                                     (int 0)))
                                 (let [d (first seq_20945)]
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
                                               (when progress
                                                 (^clojure.lang.IFn progress :history))
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
                                   (recur (next seq_20945) nil 0 0))))))))
                     (inc ctr)))
                 0
                 log_seq))))))
     ([log db index] (crosscheck-log log db index nil))))
  (reset-meta!
    #'crosscheck-log
    (assoc
      {:arglists (clojure.core/list ['log 'db 'index] ['log 'db 'index 'progress]),
       :column (int 1)}
      :name
      'crosscheck-log
      :ns
      *ns*))
  (def crosscheck-log-representations
   (fn crosscheck_log_representations
     ([uri progress]
       (let [uri (enhance-uri uri)
             map__20962 (tools/connection-resources uri)
             map__20962 (if (seq? map__20962)
                          (if (next map__20962)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20962))
                            (if (seq map__20962) (first map__20962) {}))
                          map__20962)
             cluster (get map__20962 :cluster)
             olookup (get map__20962 :olookup)
             conn (d/connect uri)
             db (d/db conn)
             log_1 (log/find-log cluster olookup)
             log_2 (log/create-log-val cluster olookup db)
             rep_1 (iter/iter-seq (log/seek-tx log_1 0))
             rep_2 (iter/iter-seq (log/seek-tx log_2 0))
             diffs (remove
                     (fn fn__20964
                       ([p__20963]
                         (let [vec__20965 p__20963
                               a (nth vec__20965 (int 0) nil)
                               b (nth vec__20965 (int 1) nil)]
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
     ([uri] (crosscheck-log-representations uri (progress-dot-fn 1000)))))
  (reset-meta!
    #'crosscheck-log-representations
    (assoc
      {:arglists (clojure.core/list ['uri] ['uri 'progress]), :column (int 1)}
      :name
      'crosscheck-log-representations
      :ns
      *ns*))
  (def crosscheck-log-cli
   (fn crosscheck_log_cli
     ([p__20970]
       (let [map__20971 p__20970
             map__20971 (if (seq? map__20971)
                          (if (next map__20971)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__20971))
                            (if (seq map__20971) (first map__20971) {}))
                          map__20971)
             uri (get map__20971 :uri)]
         (println)
         (let [uri (enhance-uri uri) cr (tools/connection-resources uri) conn (d/connect uri)]
           (loop [seq_20972 (seq [:eavt :aevt :avet :vaet])
                  chunk_20973 nil
                  count_20974 0
                  i_20975 0]
             (if (< i_20975 count_20974)
               (let [index (.nth ^clojure.lang.Indexed chunk_20973 (int i_20975))]
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
                              (fn fn__20976
                                ([result]
                                  (let [G__20977 result]
                                    (case
                                      G__20977
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
                 (recur seq_20972 chunk_20973 count_20974 (inc i_20975)))
               (let [temp__5825__auto__ (seq seq_20972)]
                 (when temp__5825__auto__
                   (let [seq_20972 temp__5825__auto__]
                     (if (chunked-seq? seq_20972)
                       (let [c__6090__auto__ (chunk-first seq_20972)]
                         (recur
                           (chunk-rest seq_20972)
                           c__6090__auto__
                           (int (count c__6090__auto__))
                           (int 0)))
                       (let [index (first seq_20972)]
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
                                      (fn fn__20979
                                        ([result]
                                          (let [G__20980 result]
                                            (case
                                              G__20980
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
                         (recur (next seq_20972) nil 0 0)))))))))))))
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
  (def crosscheck-eavt-eavt
   (fn crosscheck_eavt_eavt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-aevt-aevt
   (fn crosscheck_aevt_aevt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-avet-avet
   (fn crosscheck_avet_avet
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-raet-raet
   (fn crosscheck_raet_raet
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-eavt-aevt
   (fn crosscheck_eavt_aevt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-aevt-eavt
   (fn crosscheck_aevt_eavt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-avet-eavt
   (fn crosscheck_avet_eavt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-avet-aevt
   (fn crosscheck_avet_aevt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-raet-aevt
   (fn crosscheck_raet_aevt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-raet-eavt
   (fn crosscheck_raet_eavt
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-aevt-avet
   (fn crosscheck_aevt_avet
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-eavt-avet
   (fn crosscheck_eavt_avet
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-aevt-raet
   (fn crosscheck_aevt_raet
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def crosscheck-eavt-raet
   (fn crosscheck_eavt_raet
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
                :msec (long (quot (- (java.lang.System/nanoTime) start) 1000000))})))))))
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
  (def get-db (fn get_db ([o] (if (instance? datomic.Database o) o (d/db o)))))
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
      (let [m_21001 {:event :integrity/crosscheck-indexes}
            ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_21001 :phase :begin))))
                              nil)
            start__8599__auto__ (java.lang.System/nanoTime)
            result__8600__auto__ (try
                                   {:returned
                                    (loop [seq_21005 (seq
                                                       ['crosscheck-eavt-aevt
                                                        'crosscheck-aevt-eavt
                                                        'crosscheck-avet-eavt
                                                        'crosscheck-avet-aevt
                                                        'crosscheck-raet-aevt
                                                        'crosscheck-raet-eavt
                                                        'crosscheck-aevt-avet
                                                        'crosscheck-eavt-avet])
                                           chunk_21006 nil
                                           count_21007 0
                                           i_21008 0]
                                      (if (< i_21008 count_21007)
                                        (let [c (.nth
                                                  ^clojure.lang.Indexed chunk_21006
                                                  (int i_21008))]
                                          (print "\n" c)
                                          (flush)
                                          (println
                                            ((ns-resolve 'datomic.integrity c)
                                              (get-db o)
                                              (get-db o)
                                              (fn fn__21010
                                                ([n]
                                                  (when (zero? (mod n 100000))
                                                    (print ".")
                                                    (flush))))))
                                          (flush)
                                          (recur seq_21005 chunk_21006 count_21007 (inc i_21008)))
                                        (let [temp__5825__auto__ (seq seq_21005)]
                                          (when temp__5825__auto__
                                            (let [seq_21005 temp__5825__auto__]
                                              (if (chunked-seq? seq_21005)
                                                (let [c__6090__auto__ (chunk-first seq_21005)]
                                                  (recur
                                                    (chunk-rest seq_21005)
                                                    c__6090__auto__
                                                    (int (count c__6090__auto__))
                                                    (int 0)))
                                                (let [c (first seq_21005)]
                                                  (print "\n" c)
                                                  (flush)
                                                  (println
                                                    ((ns-resolve 'datomic.integrity c)
                                                      (get-db o)
                                                      (get-db o)
                                                      (fn fn__21012
                                                        ([n]
                                                          (when (zero? (mod n 100000))
                                                            (print ".")
                                                            (flush))))))
                                                  (flush)
                                                  (recur (next seq_21005) nil 0 0))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8601__auto__
                                     {:threw t__8601__auto__}))
            elapsed_21002 (- (java.lang.System/nanoTime) start__8599__auto__)
            msec_21003 (logger/format-as-msec (long elapsed_21002))]
        (let [endmsg__8602__auto__ (merge
                                     (assoc m_21001 :msec msec_21003 :phase :end)
                                     (when (:threw result__8600__auto__)
                                       {:threw (class (:threw result__8600__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
          nil)
        (if (contains? result__8600__auto__ :returned)
          (:returned result__8600__auto__)
          (do (throw (:threw result__8600__auto__)) nil)))))
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
      (let [temp__5825__auto__ (some-> (log/read-tail-descriptor cs) (first) (:d/r))]
        (when temp__5825__auto__
          (let [root_val_key temp__5825__auto__
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
        (fn fn__21026
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
            (let [form__20673__auto__ (clojure.core/list
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
                  error__20674__auto__ (ex-info
                                         "Assertion failed, see ex-data for details"
                                         {:bindings {'log log, 'progress progress},
                                          :form form__20673__auto__})]
              (if datomic.assert/*assert-handler*
                (datomic.assert/*assert-handler* error__20674__auto__)
                (throw ^java.lang.Throwable error__20674__auto__))))
          (reduce
            (fn fn__21030
              ([ctr p__21029]
                (let [vec__21031 p__21029
                      tx1 (nth vec__21031 (int 0) nil)
                      tx2 (nth vec__21031 (int 1) nil)]
                  (when progress (^clojure.lang.IFn progress {:n ctr, :t (:t tx1)}))
                  (let [tx (d/t->tx (long (:t tx1))) max_eidx (log/max-eidx (:data tx1))]
                    (loop [seq_21034 (seq (:data tx1)) chunk_21035 nil count_21036 0 i_21037 0]
                      (if (< i_21037 count_21036)
                        (let [d (.nth ^clojure.lang.Indexed chunk_21035 (int i_21037))]
                          (when-not (= (:tx d) tx)
                            (let [form__20673__auto__ (clojure.core/list
                                                        '=
                                                        (clojure.core/list :tx 'd)
                                                        'tx)
                                  error__20674__auto__ (ex-info
                                                         "not all datoms in transaction have same tx"
                                                         {:bindings
                                                          {'vec__21031 vec__21031,
                                                           (.withMeta
                                                             'chunk_21035
                                                             {:tag 'clojure.lang.IChunk})
                                                           chunk_21035,
                                                           'max-eidx max_eidx,
                                                           'progress progress,
                                                           'log log,
                                                           'count_21036 (long count_21036),
                                                           'ctr ctr,
                                                           'p__21029 p__21029,
                                                           'tx1 tx1,
                                                           'seq_21034 seq_21034,
                                                           'i_21037 (long i_21037),
                                                           'tx tx,
                                                           'tx2 tx2,
                                                           'd d},
                                                          :form form__20673__auto__})]
                              (if datomic.assert/*assert-handler*
                                (datomic.assert/*assert-handler* error__20674__auto__)
                                (throw ^java.lang.Throwable error__20674__auto__))))
                          (recur seq_21034 chunk_21035 count_21036 (inc i_21037)))
                        (let [temp__5825__auto__ (seq seq_21034)]
                          (when temp__5825__auto__
                            (let [seq_21034 temp__5825__auto__]
                              (if (chunked-seq? seq_21034)
                                (let [c__6090__auto__ (chunk-first seq_21034)]
                                  (recur
                                    (chunk-rest seq_21034)
                                    c__6090__auto__
                                    (int (count c__6090__auto__))
                                    (int 0)))
                                (let [d (first seq_21034)]
                                  (when-not (= (:tx d) tx)
                                    (let [form__20673__auto__ (clojure.core/list
                                                                '=
                                                                (clojure.core/list :tx 'd)
                                                                'tx)
                                          error__20674__auto__ (ex-info
                                                                 "not all datoms in transaction have same tx"
                                                                 {:bindings
                                                                  {'vec__21031 vec__21031,
                                                                   (.withMeta
                                                                     'chunk_21035
                                                                     {:tag 'clojure.lang.IChunk})
                                                                   chunk_21035,
                                                                   'max-eidx max_eidx,
                                                                   'progress progress,
                                                                   'log log,
                                                                   'count_21036 (long count_21036),
                                                                   'ctr ctr,
                                                                   'p__21029 p__21029,
                                                                   'tx1 tx1,
                                                                   'temp__5825__auto__
                                                                   temp__5825__auto__,
                                                                   'seq_21034 seq_21034,
                                                                   'i_21037 (long i_21037),
                                                                   'tx tx,
                                                                   'tx2 tx2,
                                                                   'd d},
                                                                  :form form__20673__auto__})]
                                      (if datomic.assert/*assert-handler*
                                        (datomic.assert/*assert-handler* error__20674__auto__)
                                        (throw ^java.lang.Throwable error__20674__auto__))))
                                  (recur (next seq_21034) nil 0 0))))))))
                    (when tx2
                      (when-not (< (:t tx1) (:t tx2))
                        (let [form__20673__auto__ (clojure.core/list
                                                    '<
                                                    (clojure.core/list :t 'tx1)
                                                    (clojure.core/list :t 'tx2))
                              error__20674__auto__ (ex-info
                                                     "txes are not ascending"
                                                     {:bindings
                                                      {'vec__21031 vec__21031,
                                                       'max-eidx max_eidx,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'p__21029 p__21029,
                                                       'tx1 tx1,
                                                       'tx tx,
                                                       'tx2 tx2},
                                                      :form form__20673__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__20674__auto__)
                            (throw ^java.lang.Throwable error__20674__auto__))))
                      (when-not (<= (inc max_eidx) (:t tx2))
                        (let [form__20673__auto__ (clojure.core/list
                                                    '<=
                                                    (clojure.core/list 'inc 'max-eidx)
                                                    (clojure.core/list :t 'tx2))
                              error__20674__auto__ (ex-info
                                                     "entity t too high for tx"
                                                     {:bindings
                                                      {'vec__21031 vec__21031,
                                                       'max-eidx max_eidx,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'p__21029 p__21029,
                                                       'tx1 tx1,
                                                       'tx tx,
                                                       'tx2 tx2},
                                                      :form form__20673__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__20674__auto__)
                            (throw ^java.lang.Throwable error__20674__auto__))))))
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
  (def validate-t-order
   (fn validate_t_order
     ([uri log_fn progress]
       (let [m_21052 {:event :integrity/validate-t-order, :log-fn log_fn}
             ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_21052 :phase :begin))))
                               nil)
             start__8599__auto__ (java.lang.System/nanoTime)
             result__8600__auto__ (try
                                    {:returned
                                     (let [uri (enhance-uri uri)
                                           map__21056 (tools/connection-resources uri)
                                           map__21056 (if (seq? map__21056)
                                                        (if (next map__21056)
                                                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                            (to-array map__21056))
                                                          (if (seq map__21056)
                                                            (first map__21056)
                                                            {}))
                                                        map__21056)
                                           cluster (get map__21056 :cluster)
                                           olookup (get map__21056 :olookup)
                                           temp__5823__auto__ (let 
                                                                [G__21057 log_fn]
                                                                (case
                                                                  G__21057
                                                                  :create-log-val
                                                                  (log/create-log-val
                                                                    cluster
                                                                    olookup
                                                                    (d/db (d/connect uri)))
                                                                  :find-log
                                                                  (log/find-log cluster olookup)))]
                                       (if temp__5823__auto__
                                         (let [log temp__5823__auto__]
                                           (validate-t-order* log progress))
                                         0))}
                                    (catch
                                      java.lang.Throwable
                                      t__8601__auto__
                                      {:threw t__8601__auto__}))
             elapsed_21053 (- (java.lang.System/nanoTime) start__8599__auto__)
             msec_21054 (logger/format-as-msec (long elapsed_21053))]
         (let [endmsg__8602__auto__ (merge
                                      (assoc m_21052 :msec msec_21054 :phase :end)
                                      (when (:threw result__8600__auto__)
                                        {:threw (class (:threw result__8600__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
           nil)
         (if (contains? result__8600__auto__ :returned)
           (:returned result__8600__auto__)
           (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'validate-t-order
    (assoc
      {:arglists (clojure.core/list ['uri 'log-fn 'progress]), :column (int 1)}
      :name
      'validate-t-order
      :ns
      *ns*))
  (def log-dir-entry-seq
   (fn log_dir_entry_seq
     ([p__21065 t]
       (let [map__21066 p__21065
             map__21066 (if (seq? map__21066)
                          (if (next map__21066)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21066))
                            (if (seq map__21066) (first map__21066) {}))
                          map__21066)
             cluster (get map__21066 :cluster)
             olookup (get map__21066 :olookup)
             temp__5825__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
         (when temp__5825__auto__
           (let [tree_iter temp__5825__auto__] (mapcat identity (log/log-dir-seq tree_iter))))))))
  (reset-meta!
    #'log-dir-entry-seq
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 't]), :column (int 1)}
      :name
      'log-dir-entry-seq
      :ns
      *ns*))
  (def log-seg-t-seq
   (fn log_seg_t_seq
     ([p__21069 t]
       (let [map__21070 p__21069
             map__21070 (if (seq? map__21070)
                          (if (next map__21070)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21070))
                            (if (seq map__21070) (first map__21070) {}))
                          map__21070)
             cluster (get map__21070 :cluster)
             olookup (get map__21070 :olookup)
             temp__5825__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
         (when temp__5825__auto__
           (let [tree_iter temp__5825__auto__]
             (map :t (mapcat identity (log/log-seg-seq tree_iter)))))))))
  (reset-meta!
    #'log-seg-t-seq
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 't]), :column (int 1)}
      :name
      'log-seg-t-seq
      :ns
      *ns*))
  (def crosscheck-dir-segs
   (fn crosscheck_dir_segs
     ([p__21074 progress]
       (let [map__21075 p__21074
             map__21075 (if (seq? map__21075)
                          (if (next map__21075)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21075))
                            (if (seq map__21075) (first map__21075) {}))
                          map__21075)
             cluster (get map__21075 :cluster)
             olookup (get map__21075 :olookup)
             m_21076 {:event :integrity/crosscheck-dir-segs}
             ___8598__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                               (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                 (.debug
                                   ^org.slf4j.Logger logger
                                   (logger/process (assoc m_21076 :phase :begin))))
                               nil)
             start__8599__auto__ (java.lang.System/nanoTime)
             result__8600__auto__ (try
                                    {:returned
                                     (let [temp__5825__auto__ (log/find-log cluster olookup)]
                                       (when temp__5825__auto__
                                         (let [log temp__5825__auto__
                                               temp__5825__auto__ (log/seek-tx log 0)]
                                           (when temp__5825__auto__
                                             (let [tree_iter temp__5825__auto__]
                                               (loop [seq_21080 (seq (log/log-dir-seq tree_iter))
                                                      chunk_21081 nil
                                                      count_21082 0
                                                      i_21083 0]
                                                 (if (< i_21083 count_21082)
                                                   (let [adir (.nth
                                                                ^clojure.lang.Indexed chunk_21081
                                                                (int i_21083))]
                                                     (when progress
                                                       (^clojure.lang.IFn progress adir))
                                                     (let [bad_dirs (seq
                                                                      (remove
                                                                        (fn 
                                                                          fn__21085
                                                                          ([p1__21073#]
                                                                            (=
                                                                              (:dir-t p1__21073#)
                                                                              (:seg-t
                                                                                p1__21073#))))
                                                                        (dir-seg-info-seq
                                                                          adir
                                                                          olookup)))]
                                                       (when-not (not bad_dirs)
                                                         (let [form__20673__auto__
                                                               (clojure.core/list 'not 'bad-dirs)
                                                               error__20674__auto__
                                                               (ex-info
                                                                 "Assertion failed, see ex-data for details"
                                                                 {:bindings
                                                                  {'seq_21080 seq_21080,
                                                                   'olookup olookup,
                                                                   'progress progress,
                                                                   'log log,
                                                                   'adir adir,
                                                                   'm_21076 m_21076,
                                                                   'p__21074 p__21074,
                                                                   'tree-iter tree_iter,
                                                                   'i_21083 (long i_21083),
                                                                   'start__8599__auto__
                                                                   (long start__8599__auto__),
                                                                   'bad-dirs bad_dirs,
                                                                   'temp__5825__auto__
                                                                   temp__5825__auto__,
                                                                   'count_21082 (long count_21082),
                                                                   'map__21075 map__21075,
                                                                   (.withMeta
                                                                     'chunk_21081
                                                                     {:tag 'clojure.lang.IChunk})
                                                                   chunk_21081,
                                                                   '___8598__auto__
                                                                   ___8598__auto__,
                                                                   'cluster cluster},
                                                                  :form form__20673__auto__})]
                                                           (if
                                                             datomic.assert/*assert-handler*
                                                             (datomic.assert/*assert-handler*
                                                               error__20674__auto__)
                                                             (throw
                                                               ^java.lang.Throwable error__20674__auto__)))))
                                                     (recur
                                                       seq_21080
                                                       chunk_21081
                                                       count_21082
                                                       (inc i_21083)))
                                                   (let [temp__5825__auto__ (seq seq_21080)]
                                                     (when temp__5825__auto__
                                                       (let [seq_21080 temp__5825__auto__]
                                                         (if (chunked-seq? seq_21080)
                                                           (let 
                                                             [c__6090__auto__
                                                              (chunk-first seq_21080)]
                                                             (recur
                                                               (chunk-rest seq_21080)
                                                               c__6090__auto__
                                                               (int (count c__6090__auto__))
                                                               (int 0)))
                                                           (let 
                                                             [adir (first seq_21080)]
                                                             (when
                                                               progress
                                                               (^clojure.lang.IFn progress adir))
                                                             (let 
                                                               [bad_dirs
                                                                (seq
                                                                  (remove
                                                                    (fn 
                                                                      fn__21087
                                                                      ([p1__21073#]
                                                                        (=
                                                                          (:dir-t p1__21073#)
                                                                          (:seg-t p1__21073#))))
                                                                    (dir-seg-info-seq
                                                                      adir
                                                                      olookup)))]
                                                               (when-not
                                                                 (not bad_dirs)
                                                                 (let 
                                                                   [form__20673__auto__
                                                                    (clojure.core/list
                                                                      'not
                                                                      'bad-dirs)
                                                                    error__20674__auto__
                                                                    (ex-info
                                                                      "Assertion failed, see ex-data for details"
                                                                      {:bindings
                                                                       {'seq_21080 seq_21080,
                                                                        'olookup olookup,
                                                                        'progress progress,
                                                                        'log log,
                                                                        'adir adir,
                                                                        'm_21076 m_21076,
                                                                        'p__21074 p__21074,
                                                                        'tree-iter tree_iter,
                                                                        'i_21083 (long i_21083),
                                                                        'start__8599__auto__
                                                                        (long start__8599__auto__),
                                                                        'bad-dirs bad_dirs,
                                                                        'temp__5825__auto__
                                                                        temp__5825__auto__,
                                                                        'count_21082
                                                                        (long count_21082),
                                                                        'map__21075 map__21075,
                                                                        (.withMeta
                                                                          'chunk_21081
                                                                          {:tag
                                                                           'clojure.lang.IChunk})
                                                                        chunk_21081,
                                                                        '___8598__auto__
                                                                        ___8598__auto__,
                                                                        'cluster cluster},
                                                                       :form form__20673__auto__})]
                                                                   (if
                                                                     datomic.assert/*assert-handler*
                                                                     (datomic.assert/*assert-handler*
                                                                       error__20674__auto__)
                                                                     (throw
                                                                       ^java.lang.Throwable error__20674__auto__)))))
                                                             (recur
                                                               (next seq_21080)
                                                               nil
                                                               0
                                                               0)))))))))))))}
                                    (catch
                                      java.lang.Throwable
                                      t__8601__auto__
                                      {:threw t__8601__auto__}))
             elapsed_21077 (- (java.lang.System/nanoTime) start__8599__auto__)
             msec_21078 (logger/format-as-msec (long elapsed_21077))]
         (let [endmsg__8602__auto__ (merge
                                      (assoc m_21076 :msec msec_21078 :phase :end)
                                      (when (:threw result__8600__auto__)
                                        {:threw (class (:threw result__8600__auto__))}))
               logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
           (when (.isDebugEnabled ^org.slf4j.Logger logger)
             (.debug ^org.slf4j.Logger logger (logger/process endmsg__8602__auto__)))
           nil)
         (if (contains? result__8600__auto__ :returned)
           (:returned result__8600__auto__)
           (do (throw (:threw result__8600__auto__)) nil))))))
  (reset-meta!
    #'crosscheck-dir-segs
    (assoc
      {:arglists (clojure.core/list [{:keys ['cluster 'olookup]} 'progress]), :column (int 1)}
      :name
      'crosscheck-dir-segs
      :ns
      *ns*))
  (def merge-seqs
   (fn merge_seqs
     ([cmp s1 s2 s3 s4] (merge-seqs cmp s1 (merge-seqs cmp s2 s3 s4)))
     ([cmp s1 s2 s3] (merge-seqs cmp s1 (merge-seqs cmp s2 s3)))
     ([cmp s1 s2]
       (let [s1 (seq s1) s2 (seq s2)]
         (if (and s1 s2)
           (let [vec__21104 s1
                 seq__21105 (seq vec__21104)
                 first__21106 (first seq__21105)
                 seq__21105 (next seq__21105)
                 o1 first__21106
                 m1 seq__21105
                 vec__21107 s2
                 seq__21108 (seq vec__21107)
                 first__21109 (first seq__21108)
                 seq__21108 (next seq__21108)
                 o2 first__21109
                 m2 seq__21108]
             (if (< (.compare ^java.util.Comparator cmp o1 o2) 0)
               (lazy-seq (cons o1 (merge-seqs cmp m1 s2)))
               (lazy-seq (cons o2 (merge-seqs cmp s1 m2)))))
           (or s1 s2))))))
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
  (def aevt-dquark-seq
   (fn aevt_dquark_seq
     ([db d]
       (map
         (fn fn__21118 ([p1__21117#] (dissoc p1__21117# :datom)))
         (apply
           merge-seqs
           (reify
             java.util.Comparator
             (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
           (map
             (fn fn__21123
               ([p__21122]
                 (let [vec__21124 p__21122
                       index (nth vec__21124 (int 0) nil)
                       iter (nth vec__21124 (int 1) nil)]
                   (map
                     (fn fn__21128
                       ([p__21127]
                         (let [map__21129 p__21127
                               map__21129 (if (seq? map__21129)
                                            (if (next map__21129)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__21129))
                                              (if (seq map__21129) (first map__21129) {}))
                                            map__21129)
                               datom map__21129
                               e (get map__21129 :e)
                               a (get map__21129 :a)
                               v (get map__21129 :v)
                               tx (get map__21129 :tx)
                               added (get map__21129 :added)]
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
                 (let [temp__5825__auto__ (:indexing db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:aevt index))))
                 d)]
              [:index
               (btset/seek
                 (let [temp__5825__auto__ (:index db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:aevt index))))
                 d)]
              [:mid-index
               (btset/seek
                 (let [temp__5825__auto__ (:mid-index db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:aevt index))))
                 d)]
              [:history
               (btset/seek
                 (let [temp__5825__auto__ (:history db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:aevt index))))
                 d)]]))))))
  (reset-meta!
    #'aevt-dquark-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'datomic.db.Db}) 'd]), :column (int 1)}
      :name
      'aevt-dquark-seq
      :ns
      *ns*))
  (def eavt-dquark-seq
   (fn eavt_dquark_seq
     ([db d]
       (map
         (fn fn__21138 ([p1__21137#] (dissoc p1__21137# :datom)))
         (apply
           merge-seqs
           (reify
             java.util.Comparator
             (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
           (map
             (fn fn__21143
               ([p__21142]
                 (let [vec__21144 p__21142
                       index (nth vec__21144 (int 0) nil)
                       iter (nth vec__21144 (int 1) nil)]
                   (map
                     (fn fn__21148
                       ([p__21147]
                         (let [map__21149 p__21147
                               map__21149 (if (seq? map__21149)
                                            (if (next map__21149)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__21149))
                                              (if (seq map__21149) (first map__21149) {}))
                                            map__21149)
                               datom map__21149
                               e (get map__21149 :e)
                               a (get map__21149 :a)
                               v (get map__21149 :v)
                               tx (get map__21149 :tx)
                               added (get map__21149 :added)]
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
                 (let [temp__5825__auto__ (:indexing db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:eavt index))))
                 d)]
              [:index
               (btset/seek
                 (let [temp__5825__auto__ (:index db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:eavt index))))
                 d)]
              [:mid-index
               (btset/seek
                 (let [temp__5825__auto__ (:mid-index db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:eavt index))))
                 d)]
              [:history
               (btset/seek
                 (let [temp__5825__auto__ (:history db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:eavt index))))
                 d)]]))))))
  (reset-meta!
    #'eavt-dquark-seq
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'datomic.db.Db}) 'd]), :column (int 1)}
      :name
      'eavt-dquark-seq
      :ns
      *ns*))
  (def avet-dquark-seq
   (fn avet_dquark_seq
     ([db d]
       (map
         (fn fn__21158 ([p1__21157#] (dissoc p1__21157# :datom)))
         (apply
           merge-seqs
           (reify
             java.util.Comparator
             (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
           (map
             (fn fn__21163
               ([p__21162]
                 (let [vec__21164 p__21162
                       index (nth vec__21164 (int 0) nil)
                       iter (nth vec__21164 (int 1) nil)]
                   (map
                     (fn fn__21168
                       ([p__21167]
                         (let [map__21169 p__21167
                               map__21169 (if (seq? map__21169)
                                            (if (next map__21169)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__21169))
                                              (if (seq map__21169) (first map__21169) {}))
                                            map__21169)
                               datom map__21169
                               e (get map__21169 :e)
                               a (get map__21169 :a)
                               v (get map__21169 :v)
                               tx (get map__21169 :tx)
                               added (get map__21169 :added)]
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
                 (let [temp__5825__auto__ (:indexing db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:avet index))))
                 d)]
              [:index
               (btset/seek
                 (let [temp__5825__auto__ (:index db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:avet index))))
                 d)]
              [:mid-index
               (btset/seek
                 (let [temp__5825__auto__ (:mid-index db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:avet index))))
                 d)]
              [:history
               (btset/seek
                 (let [temp__5825__auto__ (:history db)]
                   (when temp__5825__auto__ (let [index temp__5825__auto__] (:avet index))))
                 d)]]))))))
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
                       ([p1__21177#]
                         (fn fn__21179
                           ([_ & more]
                             (when (zero? (mod (swap! count inc) p1__21177#))
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
    ([db] (seq (map (fn fn__21184 ([d] (d/entity db (:e d)))) (d/datoms db :aevt :db/excise)))))
  (reset-meta!
    #'excisions
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'excisions :ns *ns*))
  (defn e-ts
    ([db e]
      (sort
        (distinct
          (map
            (fn fn__21188 ([p1__21187#] (long (d/tx->t (:tx p1__21187#)))))
            (d/datoms db :eavt e))))))
  (reset-meta!
    #'e-ts
    (assoc {:arglists (clojure.core/list ['db 'e]), :column (int 1)} :name 'e-ts :ns *ns*))
  (defn validate-excision
    ([spec]
      (let [db (d/entity-db spec)
            id (fn id ([p1__21191#] (or (:db/id p1__21191#) (db/resolve-id db p1__21191#))))
            target (:db/excise spec)
            before_t (let [temp__5823__auto__ (excise/get-before-t db spec)]
                       (if temp__5823__auto__
                         (let [bt temp__5823__auto__] (dec bt))
                         (d/basis-t db)))
            spec_t (first (e-ts db (:db/id spec)))
            t (min (min before_t spec_t) (:indexBasisT db))
            valdb (d/as-of db t)
            type (if (db/attribute db (^clojure.lang.IFn id target)) :a :e)]
        (let [G__21195 type]
          (case
            G__21195
            :a
            (let [datoms (seq (d/datoms valdb :aevt (^clojure.lang.IFn id target)))]
              (when-not (nil? datoms)
                (throw
                  (ex-info
                    "Found datoms that should have been excised"
                    {:spec spec, :datoms datoms, :t t}))
                (clojure.lang.Util/hash G__21195)))
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
          (fn fn__21199 ([spec] (validate-excision spec) (print ".") (flush) spec))
          (take-while
            (fn fn__21201
              ([p1__21198#] (<= (first (e-ts db (:db/id p1__21198#))) (:indexBasisT db))))
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
        (fn fn__21205
          ([p__21204]
            (let [vec__21206 p__21204
                  filename (nth vec__21206 (int 0) nil)
                  map__21209 (nth vec__21206 (int 1) nil)
                  map__21209 (if (seq? map__21209)
                               (if (next map__21209)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__21209))
                                 (if (seq map__21209) (first map__21209) {}))
                               map__21209)
                  base (get map__21209 :base)
                  length (get map__21209 :length)]
              (map
                (fn fn__21210
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
          (fn fn__21215
            ([p__21214]
              (let [vec__21216 p__21214
                    attrid (nth vec__21216 (int 0) nil)
                    uuid (nth vec__21216 (int 1) nil)
                    temp__5823__auto__ (get olookup (str uuid))]
                (if temp__5823__auto__
                  (let [cfs temp__5823__auto__]
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
            temp__5825__auto__ (seq
                                 (remove
                                   (fn fn__21225 ([p1__21222#] (nth p1__21222# (int 2))))
                                   (-> (:index db)
                                    (:fulltext)
                                    (:root)
                                    (fulltext-path-reachability db olookup progress)
                                    (:history db)
                                    (:fulltext)
                                    (:root)
                                    (fulltext-path-reachability db olookup progress)
                                    (concat))))]
        (when temp__5825__auto__
          (let [missing temp__5825__auto__]
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
        (fn fn__21231 ([p1__21230#] (< p1__21230# 1000)))
        (map (comp d/tx->t :tx) (d/datoms db :avet :db/txInstant)))))
  (reset-meta!
    #'tx-instant-ts
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'tx-instant-ts :ns *ns*))
  (def seq-diffs
   (fn seq_diffs
     ([colla collb progress n]
       (let [G__21240 (seq colla)
             vec__21242 G__21240
             seq__21243 (seq vec__21242)
             first__21244 (first seq__21243)
             seq__21243 (next seq__21243)
             a first__21244
             morea seq__21243
             G__21241 (seq collb)
             vec__21245 G__21241
             seq__21246 (seq vec__21245)
             first__21247 (first seq__21246)
             seq__21246 (next seq__21246)
             b first__21247
             moreb seq__21246
             n n]
         (loop [G__21240 G__21240 G__21241 G__21241 n n]
           (let [vec__21248 G__21240
                 seq__21249 (seq vec__21248)
                 first__21250 (first seq__21249)
                 seq__21249 (next seq__21249)
                 a first__21250
                 morea seq__21249
                 vec__21251 G__21241
                 seq__21252 (seq vec__21251)
                 first__21253 (first seq__21252)
                 seq__21252 (next seq__21252)
                 b first__21253
                 moreb seq__21252
                 n n]
             (^clojure.lang.IFn progress n)
             (when (or a b)
               (if (= a b)
                 (recur morea moreb (inc n))
                 (lazy-seq
                   (cons {:a a, :b b, :n n} (seq-diffs morea moreb progress (inc n))))))))))
     ([colla collb progress] (seq-diffs colla collb progress 0))))
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
      (let [temp__5823__auto__ (deref
                                 (cluster/get-ref
                                   (:cluster cr)
                                   (garbage/root-ref-key (:cluster cr))))]
        (if temp__5823__auto__
          (let [root_key temp__5823__auto__
                root (get (:olookup cr) (:key root_key))
                valid? (comp not empty?)
                result (progress-reduce
                         (fn fn__21258
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
            temp__5825__auto__ (seq-diffs (tx-range-ts conn) (tx-instant-ts (d/db conn)) progress)]
        (when temp__5825__auto__
          (let [diffs temp__5825__auto__]
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
            temp__5823__auto__ (seq (attr-datoms db (nohistory-attrs db)))]
        (when temp__5823__auto__
          (let [datoms temp__5823__auto__]
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
          (fn fn__21278 ([p1__21277#] (stat-counts db (.id ^datomic.db.Attribute p1__21277#))))
          (remove
            (fn fn__21280
              ([p1__21276#] (contains? nohists (.id ^datomic.db.Attribute p1__21276#))))
            (filter
              (fn fn__21282 ([p1__21275#] (.hasAVET ^datomic.db.Attribute p1__21275#)))
              (db/attribute-seq db)))))))
  (reset-meta!
    #'aevt-avet-stats
    (assoc {:arglists (clojure.core/list ['db]), :column (int 1)} :name 'aevt-avet-stats :ns *ns*))
  (def aevt-avet-stats-consistent?
   (fn aevt_avet_stats_consistent_QMARK_
     ([p__21285]
       (let [map__21286 p__21285
             map__21286 (if (seq? map__21286)
                          (if (next map__21286)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21286))
                            (if (seq map__21286) (first map__21286) {}))
                          map__21286)
             aevt_total (get map__21286 :aevt-total)
             avet_total (get map__21286 :avet-total)]
         (= aevt_total avet_total)))))
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
      (let [temp__5825__auto__ (seq (remove aevt-avet-stats-consistent? (aevt-avet-stats db)))]
        (when temp__5825__auto__
          (let [mismatch temp__5825__auto__] (prn {:stats-mismatch mismatch}))))))
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
                           ([p1__21290#]
                             (apply
                               +
                               (vals
                                 (select-keys
                                   p1__21290#
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
          (let [form__20673__auto__ (clojure.core/list
                                      'nil?
                                      (clojure.core/list 'ic/unique-collisions 'db 'identity))
                error__20674__auto__ (ex-info
                                       "Assertion failed, see ex-data for details"
                                       {:bindings {'uri uri, 'cr cr, 'conn conn, 'db db},
                                        :form form__20673__auto__})]
            (if datomic.assert/*assert-handler*
              (datomic.assert/*assert-handler* error__20674__auto__)
              (throw ^java.lang.Throwable error__20674__auto__))))
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
  (def describe-segment
   (fn describe_segment
     ([uri segment_id]
       (let [uri (enhance-uri uri)
             cr (tools/connection-resources uri)
             buf (:buf (deref (cluster/get-val (:cluster cr) (str segment_id))))]
         (dio/describe-bbuf buf)))))
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
            map__21299 (tools/connection-resources uri)
            map__21299 (if (seq? map__21299)
                         (if (next map__21299)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21299))
                           (if (seq map__21299) (first map__21299) {}))
                         map__21299)
            cluster (get map__21299 :cluster)
            olookup (get map__21299 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)]
        (when root_id
          (let [root (get olookup root_id)
                branch? (fn branch_QMARK_
                          ([p__21300]
                            (let [map__21302 p__21300
                                  map__21302 (if (seq? map__21302)
                                               (if (next map__21302)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__21302))
                                                 (if (seq map__21302) (first map__21302) {}))
                                               map__21302)
                                  seg (get map__21302 :seg)]
                              (or
                                (instance? datomic.fulltext.Root seg)
                                (instance? datomic.clusterfs.ClusterFS seg)))))
                children (fn children
                           ([p__21305]
                             (let [map__21307 p__21305
                                   map__21307 (if (seq? map__21307)
                                                (if (next map__21307)
                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                    (to-array map__21307))
                                                  (if (seq map__21307) (first map__21307) {}))
                                                map__21307)
                                   seg (get map__21307 :seg)]
                               (cond
                                 (instance? datomic.fulltext.Root seg) (map
                                                                         (fn 
                                                                           fn__21308
                                                                           ([p1__21298#]
                                                                             (let 
                                                                               [uuid
                                                                                (str p1__21298#)]
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
                                                                                   fn__21310
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
            map__21314 (tools/connection-resources uri)
            map__21314 (if (seq? map__21314)
                         (if (next map__21314)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21314))
                           (if (seq map__21314) (first map__21314) {}))
                         map__21314)
            cluster (get map__21314 :cluster)
            olookup (get map__21314 :olookup)
            root_id (log/root-id cluster)
            branch? (fn branch_QMARK_
                      ([p__21315]
                        (let [map__21317 p__21315
                              map__21317 (if (seq? map__21317)
                                           (if (next map__21317)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__21317))
                                             (if (seq map__21317) (first map__21317) {}))
                                           map__21317)
                              seg (get map__21317 :seg)]
                          (instance? datomic.log.LogDir (first seg)))))
            children (fn children
                       ([p__21319]
                         (let [map__21321 p__21319
                               map__21321 (if (seq? map__21321)
                                            (if (next map__21321)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__21321))
                                              (if (seq map__21321) (first map__21321) {}))
                                            map__21321)
                               uuid (get map__21321 :uuid)
                               seg (get map__21321 :seg)]
                           (map
                             (fn fn__21322
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
            map__21326 (tools/connection-resources uri)
            map__21326 (if (seq? map__21326)
                         (if (next map__21326)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21326))
                           (if (seq map__21326) (first map__21326) {}))
                         map__21326)
            cluster (get map__21326 :cluster)
            olookup (get map__21326 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)
            branch? (fn branch_QMARK_
                      ([p__21327]
                        (let [map__21329 p__21327
                              map__21329 (if (seq? map__21329)
                                           (if (next map__21329)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__21329))
                                             (if (seq map__21329) (first map__21329) {}))
                                           map__21329)
                              seg (get map__21329 :seg)]
                          (or
                            (instance? datomic.index.RootNode seg)
                            (instance? datomic.index.DirNode seg)))))
            children (fn children
                       ([p__21332]
                         (let [map__21334 p__21332
                               map__21334 (if (seq? map__21334)
                                            (if (next map__21334)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__21334))
                                              (if (seq map__21334) (first map__21334) {}))
                                            map__21334)
                               seg (get map__21334 :seg)]
                           (cond
                             (instance? datomic.index.RootNode seg) (map
                                                                      (fn 
                                                                        fn__21335
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
                                                                         fn__21337
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
            map__21341 (tools/connection-resources uri)
            map__21341 (if (seq? map__21341)
                         (if (next map__21341)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21341))
                           (if (seq map__21341) (first map__21341) {}))
                         map__21341)
            cluster (get map__21341 :cluster)
            olookup (get map__21341 :olookup)
            pod_key (log/tail-pod-key cluster)
            pod_meta (deref (cluster/get-pod-meta cluster pod_key))
            mkv (fn mkv
                  ([id]
                    (let [temp__5823__auto__ (kvs/get (cluster/get-ref-store cluster) id false)]
                      (if temp__5823__auto__
                        (let [entry temp__5823__auto__]
                          (update-in
                            entry
                            [:v]
                            (fn fn__21343
                              ([bb]
                                (when bb (fressian/defressian bb :handlers log/read-handlers))))))
                        {:id id}))))]
        (cons
          (if pod_meta (assoc pod_meta :type :pod-key) {:id pod_key})
          (when pod_meta
            (take-while
              identity
              (iterate
                (fn fn__21348
                  ([p__21347]
                    (let [map__21349 p__21347
                          map__21349 (if (seq? map__21349)
                                       (if (next map__21349)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__21349))
                                         (if (seq map__21349) (first map__21349) {}))
                                       map__21349)
                          prev (get map__21349 :prev)]
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
                 (clojure.core/list 't__21352__auto__)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list '.printStackTrace)
                       (clojure.core/list 't__21352__auto__))))))))))))
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
  (def -main*
   (fn _main_STAR_
     ([p__21355]
       (let [map__21356 p__21355
             map__21356 (if (seq? map__21356)
                          (if (next map__21356)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21356))
                            (if (seq map__21356) (first map__21356) {}))
                          map__21356)
             uri (get map__21356 :uri)
             validate (get map__21356 :validate)]
         (let [uri (enhance-uri uri)]
           (println "\nDiagnostics:")
           (pp/pprint
             (try
               (diagnostics uri)
               (catch
                 java.lang.Throwable
                 t__21352__auto__
                 (do (.printStackTrace ^java.lang.Throwable t__21352__auto__) nil)))))
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
                     (fn fn__21360 ([p1__21354#] (= 1 (long (count p1__21354#)))))
                     (pod-storage-seq uri))
                   (catch
                     java.lang.Throwable
                     t__21352__auto__
                     (do (.printStackTrace ^java.lang.Throwable t__21352__auto__) nil)))))
             (println "\nMissing log segments: ")
             (prn
               (seq
                 (try
                   (remove :seg (log-storage-seq uri))
                   (catch
                     java.lang.Throwable
                     t__21352__auto__
                     (do (.printStackTrace ^java.lang.Throwable t__21352__auto__) nil)))))
             (loop [seq_21365 (seq index-sort-root-keys) chunk_21366 nil count_21367 0 i_21368 0]
               (if (< i_21368 count_21367)
                 (let [k (.nth ^clojure.lang.Indexed chunk_21366 (int i_21368))]
                   (println "\nMissing segments in " k)
                   (prn
                     (seq
                       (try
                         (remove :seg (index-storage-seq uri k))
                         (catch
                           java.lang.Throwable
                           t__21352__auto__
                           (do (.printStackTrace ^java.lang.Throwable t__21352__auto__) nil)))))
                   (recur seq_21365 chunk_21366 count_21367 (inc i_21368)))
                 (let [temp__5825__auto__ (seq seq_21365)]
                   (when temp__5825__auto__
                     (let [seq_21365 temp__5825__auto__]
                       (if (chunked-seq? seq_21365)
                         (let [c__6090__auto__ (chunk-first seq_21365)]
                           (recur
                             (chunk-rest seq_21365)
                             c__6090__auto__
                             (int (count c__6090__auto__))
                             (int 0)))
                         (let [k (first seq_21365)]
                           (println "\nMissing segments in " k)
                           (prn
                             (seq
                               (try
                                 (remove :seg (index-storage-seq uri k))
                                 (catch
                                   java.lang.Throwable
                                   t__21352__auto__
                                   (do
                                     (.printStackTrace ^java.lang.Throwable t__21352__auto__)
                                     nil)))))
                           (recur (next seq_21365) nil 0 0))))))))
             (loop [seq_21373 (seq [:fulltext :fulltext-hist])
                    chunk_21374 nil
                    count_21375 0
                    i_21376 0]
               (if (< i_21376 count_21375)
                 (let [k (.nth ^clojure.lang.Indexed chunk_21374 (int i_21376))]
                   (println "\nMissing segments in " k)
                   (prn
                     (seq
                       (try
                         (remove :seg (fulltext-storage-seq uri k))
                         (catch
                           java.lang.Throwable
                           t__21352__auto__
                           (do (.printStackTrace ^java.lang.Throwable t__21352__auto__) nil)))))
                   (recur seq_21373 chunk_21374 count_21375 (inc i_21376)))
                 (let [temp__5825__auto__ (seq seq_21373)]
                   (when temp__5825__auto__
                     (let [seq_21373 temp__5825__auto__]
                       (if (chunked-seq? seq_21373)
                         (let [c__6090__auto__ (chunk-first seq_21373)]
                           (recur
                             (chunk-rest seq_21373)
                             c__6090__auto__
                             (int (count c__6090__auto__))
                             (int 0)))
                         (let [k (first seq_21373)]
                           (println "\nMissing segments in " k)
                           (prn
                             (seq
                               (try
                                 (remove :seg (fulltext-storage-seq uri k))
                                 (catch
                                   java.lang.Throwable
                                   t__21352__auto__
                                   (do
                                     (.printStackTrace ^java.lang.Throwable t__21352__auto__)
                                     nil)))))
                           (recur (next seq_21373) nil 0 0))))))))
             (println "\nDone!")))))))
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
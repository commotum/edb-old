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
  (defmulti enhance-uri (fn fn__21963 ([uri] (:protocol (uri/parse uri)))))
  (defmethod enhance-uri :default fn__21968 ([uri] uri))
  (defmethod
    enhance-uri
    :ddb+s3
    fn__21970
    ([uri] (uri/create (merge (uri/parse uri) {:skip-efs true}))))
  (defn progress-reduce
    ([f val p__21972 coll]
      (let [map__21973 p__21972
            map__21973 (if (seq? map__21973)
                         (clojure.lang.PersistentHashMap/create (seq map__21973))
                         map__21973)
            progress (get map__21973 :progress)
            n (get map__21973 :n)]
        (if progress
          (let [pf (fn pf
                     ([p__21974 item]
                       (let [vec__21976 p__21974
                             c (nth vec__21976 (int 0) nil)
                             acc (nth vec__21976 (int 1) nil)]
                         (when (zero? (mod c n)) (^clojure.lang.IFn progress acc n))
                         [(inc c) (^clojure.lang.IFn f acc item)])))]
            (second (reduce pf [0 val] coll)))
          (reduce f val coll)))))
  (defn progress-dot ([& _] (print ".") (flush)))
  (defn cauterize
    ([db & ks]
      (merge
        db
        (select-keys
          {:index nil, :history nil, :indexing nil, :mid-index nil, :memidx db/mem-index-set}
          ks))))
  (defn nohistory-attrs
    ([db]
      (reduce
        conj
        #{}
        (map
          :id
          (filter
            (fn fn__21984 ([p1__21983#] (.-noHistory ^datomic.db.Attribute p1__21983#)))
            (filter (partial instance? datomic.db.Attribute) (:elements db)))))))
  (defn attr-datoms ([db attrs] (mapcat (partial d/datoms db :aevt) attrs)))
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
  (defn restatement?
    ([p__21989]
      (let [vec__21990 p__21989 d1 (nth vec__21990 (int 0) nil) d2 (nth vec__21990 (int 1) nil)]
        (and
          (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
          (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
          (= (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))
          (= (boolean (.added ^datomic.Datom d1)) (boolean (.added ^datomic.Datom d2)))))))
  (defn attr-id-set
    ([db pred]
      (into
        #{}
        (map
          :id
          (filter
            pred
            (map
              (fn fn__21998
                ([p__21997]
                  (let [vec__21999 p__21997 e (nth vec__21999 (int 0) nil)] (d/attribute db e))))
              (d/q [:find '?e :where ['?e :db/valueType]] db)))))))
  (defn make-tupler
    ([index]
      (let [G__22004 index]
        (case
          G__22004
          :aevt
          (fn fn__22005 ([d] [(:a d) (:e d) (:v d) (:tx d)]))
          :avet
          (fn fn__22007 ([d] [(:a d) (:v d) (:e d) (:tx d)]))
          :eavt
          (fn fn__22009 ([d] [(:e d) (:a d) (:v d) (:tx d)]))
          :vaet
          (fn fn__22011 ([d] [(:v d) (:a d) (:e d) (:tx d)]))))))
  (defn unfindable-datom-seq
    ([db index progress]
      (let [op (let [G__22023 index]
                 (case
                   G__22023
                   :aevt
                   (fn fn__22024
                     ([p1__22017# p2__22018#]
                       (.seekAEVT ^datomic.db.IDb p1__22017# ^datomic.impl.db.IDatum p2__22018#)))
                   :avet
                   (fn fn__22026
                     ([p1__22019# p2__22020#]
                       (.seekAVET ^datomic.db.IDb p1__22019# ^datomic.impl.db.IDatum p2__22020#)))
                   :eavt
                   (fn fn__22028
                     ([p1__22015# p2__22016#]
                       (.seekEAVT ^datomic.db.IDb p1__22015# ^datomic.impl.db.IDatum p2__22016#)))
                   :vaet
                   (fn fn__22030
                     ([p1__22021# p2__22022#]
                       (.seekRAET
                         ^datomic.db.IDb p1__22021#
                         ^datomic.impl.db.IDatum p2__22022#)))))]
        (remove
          (fn fn__22032
            ([datom]
              (when progress (^clojure.lang.IFn progress))
              (= datom (.get (^clojure.lang.IFn op db datom)))))
          (d/datoms db index)))))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__22035 ([& _] (when (zero? (mod (swap! c inc) n)) (print ".") (flush)))))))
  (defn datom-comparator
    ([sort]
      (let [G__22038 sort]
        (case G__22038 :aevt db/aevt-cmp :avet db/avet-cmp :eavt db/eavt-cmp :vaet db/raet-cmp))))
  (defn unsorted-dirs
    ([db tier sort progress]
      (let [cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__22040
            ([a b]
              (^clojure.lang.IFn progress)
              (< (.compare ^java.util.Comparator cmp (:key a) (:key b)) 0)))
          (some-> db (^clojure.lang.IFn tier) (^clojure.lang.IFn sort) (.seek) (index/dir-seq))))))
  (defn validate-dir-sorts
    ([db]
      (let [tses (let [iter__6025__auto__ (fn iter__22044
                                            ([s__22045]
                                              (lazy-seq
                                                (loop [s__22045 s__22045]
                                                  (let [temp__5457__auto__ (seq s__22045)]
                                                    (when temp__5457__auto__
                                                      (let [xs__6012__auto__ temp__5457__auto__
                                                            tier (first xs__6012__auto__)
                                                            iterys__6021__auto__
                                                            (fn 
                                                              iter__22046
                                                              ([s__22047]
                                                                (lazy-seq
                                                                  (let 
                                                                    [s__22047 s__22047
                                                                     temp__5457__auto__
                                                                     (seq s__22047)]
                                                                    (when
                                                                      temp__5457__auto__
                                                                      (let 
                                                                        [s__22047
                                                                         temp__5457__auto__]
                                                                        (if
                                                                          (chunked-seq? s__22047)
                                                                          (let 
                                                                            [c__6023__auto__
                                                                             (chunk-first s__22047)
                                                                             size__6024__auto__
                                                                             (int
                                                                               (count
                                                                                 c__6023__auto__))
                                                                             b__22049
                                                                             (chunk-buffer
                                                                               (java.lang.Integer/valueOf
                                                                                 (int
                                                                                   size__6024__auto__)))]
                                                                            (if
                                                                              (loop 
                                                                                [i__22048 (int 0)]
                                                                                (if
                                                                                  (<
                                                                                    i__22048
                                                                                    size__6024__auto__)
                                                                                  (let 
                                                                                    [sort
                                                                                     (.nth
                                                                                       ^clojure.lang.Indexed c__6023__auto__
                                                                                       (int
                                                                                         i__22048))]
                                                                                    (chunk-append
                                                                                      b__22049
                                                                                      [tier sort])
                                                                                    (recur
                                                                                      (inc
                                                                                        i__22048)))
                                                                                  true))
                                                                              (chunk-cons
                                                                                (chunk b__22049)
                                                                                (^clojure.lang.IFn iter__22046
                                                                                  (chunk-rest
                                                                                    s__22047)))
                                                                              (chunk-cons
                                                                                (chunk b__22049)
                                                                                nil)))
                                                                          (let 
                                                                            [sort (first s__22047)]
                                                                            (cons
                                                                              [tier sort]
                                                                              (^clojure.lang.IFn iter__22046
                                                                                (rest
                                                                                  s__22047)))))))))))
                                                            fs__6022__auto__
                                                            (seq
                                                              (^clojure.lang.IFn iterys__6021__auto__
                                                                [:eavt :aevt :avet :vaet]))]
                                                        (if fs__6022__auto__
                                                          (concat
                                                            fs__6022__auto__
                                                            (^clojure.lang.IFn iter__22044
                                                              (rest s__22045)))
                                                          (recur (rest s__22045))))))))))]
                   (^clojure.lang.IFn iter__6025__auto__ [:mid-index :main :history]))]
        (print "Validating dirs for ")
        (loop [seq_22067 (seq tses) chunk_22068 nil count_22069 0 i_22070 0]
          (if (< i_22070 count_22069)
            (let [vec__22071 (.nth ^clojure.lang.Indexed chunk_22068 (int i_22070))
                  tier (nth vec__22071 (int 0) nil)
                  sort (nth vec__22071 (int 1) nil)]
              (print "[" tier sort "]")
              (let [temp__5457__auto__ (unsorted-dirs db tier sort (progress-dot-fn 10000))]
                (when temp__5457__auto__
                  (let [s temp__5457__auto__]
                    (println)
                    (throw
                      (ex-info
                        (str "Disorderly dirs in " tier sort (first s))
                        {:pairs s, :tier tier, :sort sort})))))
              (recur seq_22067 chunk_22068 count_22069 (inc i_22070)))
            (let [temp__5457__auto__ (seq seq_22067)]
              (when temp__5457__auto__
                (let [seq_22067 temp__5457__auto__]
                  (if (chunked-seq? seq_22067)
                    (let [c__5719__auto__ (chunk-first seq_22067)]
                      (recur
                        (chunk-rest seq_22067)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [vec__22074 (first seq_22067)
                          tier (nth vec__22074 (int 0) nil)
                          sort (nth vec__22074 (int 1) nil)]
                      (print "[" tier sort "]")
                      (let [temp__5457__auto__ (unsorted-dirs
                                                 db
                                                 tier
                                                 sort
                                                 (progress-dot-fn 10000))]
                        (when temp__5457__auto__
                          (let [s temp__5457__auto__]
                            (println)
                            (throw
                              (ex-info
                                (str "Disorderly dirs in " tier sort (first s))
                                {:pairs s, :tier tier, :sort sort})))))
                      (recur (next seq_22067) nil 0 0))))))))
        (println))))
  (defn unsorted-datoms
    ([db sort order progress]
      (let [op (let [G__22083 order] (case G__22083 :allow-duplicates <= :strict <))
            cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__22084
            ([a b]
              (^clojure.lang.IFn progress)
              (^clojure.lang.IFn op
                (java.lang.Integer/valueOf (int (.compare ^java.util.Comparator cmp a b)))
                0)))
          (d/datoms db sort)))))
  (defn validate-index-sorts
    ([db order]
      (loop [seq_22087 (seq [:eavt :aevt :avet :vaet]) chunk_22088 nil count_22089 0 i_22090 0]
        (if (< i_22090 count_22089)
          (let [sort (.nth ^clojure.lang.Indexed chunk_22088 (int i_22090))]
            (println "Validating " sort)
            (let [temp__5457__auto__ (unsorted-datoms db sort order (progress-dot-fn 10000))]
              (when temp__5457__auto__
                (let [s temp__5457__auto__]
                  (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s})))))
            (println)
            (recur seq_22087 chunk_22088 count_22089 (inc i_22090)))
          (let [temp__5457__auto__ (seq seq_22087)]
            (when temp__5457__auto__
              (let [seq_22087 temp__5457__auto__]
                (if (chunked-seq? seq_22087)
                  (let [c__5719__auto__ (chunk-first seq_22087)]
                    (recur
                      (chunk-rest seq_22087)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [sort (first seq_22087)]
                    (println "Validating " sort)
                    (let [temp__5457__auto__ (unsorted-datoms
                                               db
                                               sort
                                               order
                                               (progress-dot-fn 10000))]
                      (when temp__5457__auto__
                        (let [s temp__5457__auto__]
                          (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s})))))
                    (println)
                    (recur (next seq_22087) nil 0 0)))))))))
    ([db] (validate-index-sorts db :strict)))
  (defn unpaired-history-assertions
    ([db sort progress]
      (let [temp__5457__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5457__auto__
          (let [hist temp__5457__auto__]
            (tools/unsorted-seq
              (fn fn__22097
                ([d1 d2]
                  (^clojure.lang.IFn progress)
                  (or (not (.added ^datomic.Datom d2)) (index/retract-assert-pair? d1 d2))))
              (iter/iter-seq (.seek ^datomic.btset.IDataSet hist))))))))
  (defn unpaired-history-retractions
    ([db sort progress]
      (let [temp__5457__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5457__auto__
          (let [hist temp__5457__auto__]
            (tools/unsorted-seq
              (fn fn__22103
                ([d1 d2]
                  (^clojure.lang.IFn progress)
                  (or (.added ^datomic.Datom d1) (index/retract-assert-pair? d1 d2))))
              (iter/iter-seq (.seek ^datomic.btset.IDataSet hist))))))))
  (defn validate-history-pairs
    ([db]
      (loop [seq_22108 (seq [:eavt :aevt :avet :vaet]) chunk_22109 nil count_22110 0 i_22111 0]
        (if (< i_22111 count_22110)
          (let [sort (.nth ^clojure.lang.Indexed chunk_22109 (int i_22111))]
            (println "Validating history pairs " sort)
            (let [temp__5457__auto__ (unpaired-history-assertions
                                       db
                                       sort
                                       (progress-dot-fn 100000))]
              (when temp__5457__auto__
                (let [s temp__5457__auto__]
                  (throw
                    (ex-info (str "Unpaired history assertions " (first s)) {:assertions s})))))
            (println)
            (recur seq_22108 chunk_22109 count_22110 (inc i_22111)))
          (let [temp__5457__auto__ (seq seq_22108)]
            (when temp__5457__auto__
              (let [seq_22108 temp__5457__auto__]
                (if (chunked-seq? seq_22108)
                  (let [c__5719__auto__ (chunk-first seq_22108)]
                    (recur
                      (chunk-rest seq_22108)
                      c__5719__auto__
                      (int (count c__5719__auto__))
                      (int 0)))
                  (let [sort (first seq_22108)]
                    (println "Validating history pairs " sort)
                    (let [temp__5457__auto__ (unpaired-history-assertions
                                               db
                                               sort
                                               (progress-dot-fn 100000))]
                      (when temp__5457__auto__
                        (let [s temp__5457__auto__]
                          (throw
                            (ex-info
                              (str "Unpaired history assertions " (first s))
                              {:assertions s})))))
                    (println)
                    (recur (next seq_22108) nil 0 0))))))))))
  (defn selfcheck-index-cli
    ([p__22117]
      (let [map__22118 p__22117
            map__22118 (if (seq? map__22118)
                         (clojure.lang.PersistentHashMap/create (seq map__22118))
                         map__22118)
            uri (get map__22118 :uri)
            uri (enhance-uri uri)
            conn (d/connect uri)
            db (d/db conn)]
        (loop [seq_22119 (seq [:eavt :aevt :avet :vaet]) chunk_22120 nil count_22121 0 i_22122 0]
          (if (< i_22122 count_22121)
            (let [index (.nth ^clojure.lang.Indexed chunk_22120 (int i_22122))]
              (print "Self-checking " index)
              (let [count (atom 0)
                    progress (fn progress
                               ([]
                                 (when (zero? (mod (swap! count inc) 10000)) (print ".") (flush))))
                    problem (first (unfindable-datom-seq db index progress))]
                (if problem
                  (throw
                    (ex-info
                      (str "Unable to seek to " (pr-str problem) " in " index)
                      {:datom problem, :index index}))
                  (println "\nChecked " (deref count) " datoms")))
              (recur seq_22119 chunk_22120 count_22121 (inc i_22122)))
            (let [temp__5457__auto__ (seq seq_22119)]
              (when temp__5457__auto__
                (let [seq_22119 temp__5457__auto__]
                  (if (chunked-seq? seq_22119)
                    (let [c__5719__auto__ (chunk-first seq_22119)]
                      (recur
                        (chunk-rest seq_22119)
                        c__5719__auto__
                        (int (count c__5719__auto__))
                        (int 0)))
                    (let [index (first seq_22119)]
                      (print "Self-checking " index)
                      (let [count (atom 0)
                            progress (fn progress
                                       ([]
                                         (when (zero? (mod (swap! count inc) 10000))
                                           (print ".")
                                           (flush))))
                            problem (first (unfindable-datom-seq db index progress))]
                        (if problem
                          (throw
                            (ex-info
                              (str "Unable to seek to " (pr-str problem) " in " index)
                              {:datom problem, :index index}))
                          (println "\nChecked " (deref count) " datoms")))
                      (recur (next seq_22119) nil 0 0)))))))))))
  (defn mk-attr-pred ([s] (fn fn__22131 ([p1__22130#] (contains? s (:a p1__22130#))))))
  (reset-meta!
    #'mk-attr-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column 1}
      :name
      'mk-attr-pred
      :ns
      *ns*))
  (defn mk-index-pred
    ([db index]
      (let [G__22135 index]
        (case
          G__22135
          (:aevt :eavt)
          identity
          :avet
          (mk-attr-pred (attr-id-set db :has-avet))
          :raet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__22136 ([p1__22134#] (= :db.type/ref (:value-type p1__22134#))))))
          :vaet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__22138 ([p1__22134#] (= :db.type/ref (:value-type p1__22134#))))))))))
  (defn crosscheck-log
    ([log db index progress]
      (do
        (let [m_22142 {:event :integrity/crosscheck-log, :index index, :db (:id db)}
              ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_22142 :phase :begin)))
                                  nil)
                                nil)
              start__8981__auto__ (java.lang.System/nanoTime)
              result__8982__auto__ (try
                                     {:returned nil}
                                     (catch
                                       java.lang.Throwable
                                       t__8983__auto__
                                       {:threw t__8983__auto__}))
              elapsed_22143 (- (java.lang.System/nanoTime) start__8981__auto__)
              msec_22144 (logger/format-as-msec (long elapsed_22143))]
          (let [endmsg__8984__auto__ (merge
                                       (assoc m_22142 :msec msec_22144 :phase :end)
                                       (when (:threw result__8982__auto__)
                                         {:threw (class (:threw result__8982__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
              nil)
            nil)
          (if (contains? result__8982__auto__ :returned)
            (:returned result__8982__auto__)
            (throw (:threw result__8982__auto__))))
        (let [temp__5455__auto__ (seq (iter/iter-seq (log/seek-tx log 0)))]
          (when temp__5455__auto__
            (let [log_seq temp__5455__auto__
                  tupler (make-tupler index)
                  nohists (tools/ever-nohistory-attrs db)
                  basis_t (d/basis-t db)
                  limit_tx (d/t->tx (long ^java.lang.Number basis_t))
                  hist (d/history db)
                  index_pred (mk-index-pred db index)]
              (reduce
                (fn fn__22147
                  ([ctr tx]
                    (loop [seq_22148 (seq
                                       (take-while
                                         (fn fn__22152
                                           ([p1__22141#] (<= (:tx p1__22141#) limit_tx)))
                                         (:data tx)))
                           chunk_22149 nil
                           count_22150 0
                           i_22151 0]
                      (if (< i_22151 count_22150)
                        (let [d (.nth ^clojure.lang.Indexed chunk_22149 (int i_22151))]
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
                          (recur seq_22148 chunk_22149 count_22150 (inc i_22151)))
                        (let [temp__5457__auto__ (seq seq_22148)]
                          (when temp__5457__auto__
                            (let [seq_22148 temp__5457__auto__]
                              (if (chunked-seq? seq_22148)
                                (let [c__5719__auto__ (chunk-first seq_22148)]
                                  (recur
                                    (chunk-rest seq_22148)
                                    c__5719__auto__
                                    (int (count c__5719__auto__))
                                    (int 0)))
                                (let [d (first seq_22148)]
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
                                  (recur (next seq_22148) nil 0 0))))))))
                    (inc ctr)))
                0
                log_seq))))))
    ([log db index] (crosscheck-log log db index nil)))
  (defn crosscheck-log-representations
    ([uri progress]
      (let [uri (enhance-uri uri)
            map__22165 (tools/connection-resources uri)
            map__22165 (if (seq? map__22165)
                         (clojure.lang.PersistentHashMap/create (seq map__22165))
                         map__22165)
            cluster (get map__22165 :cluster)
            olookup (get map__22165 :olookup)
            conn (d/connect uri)
            db (d/db conn)
            log_1 (log/find-log cluster olookup)
            log_2 (log/create-log-val cluster olookup db)
            rep_1 (iter/iter-seq (log/seek-tx log_1 0))
            rep_2 (iter/iter-seq (log/seek-tx log_2 0))
            diffs (remove
                    (fn fn__22167
                      ([p__22166]
                        (let [vec__22168 p__22166
                              a (nth vec__22168 (int 0) nil)
                              b (nth vec__22168 (int 1) nil)]
                          (^clojure.lang.IFn progress)
                          (= (dissoc a :id) (dissoc b :id)))))
                    (map vector rep_1 rep_2))
            desc {:basis-t (:basisT db),
                  :index-basis-t (:indexBasisT db),
                  :tail-1 (first (:txes (:tail log_1))),
                  :tail-2 (first (:txes (:tail log_2)))}]
        (if (seq diffs)
          (do (throw (ex-info "Log representations did not match" (assoc desc :diffs diffs))) nil)
          desc)))
    ([uri] (crosscheck-log-representations uri (progress-dot-fn 1000))))
  (defn crosscheck-log-cli
    ([p__22173]
      (let [map__22174 p__22173
            map__22174 (if (seq? map__22174)
                         (clojure.lang.PersistentHashMap/create (seq map__22174))
                         map__22174)
            uri (get map__22174 :uri)]
        (println)
        (let [uri (enhance-uri uri) cr (tools/connection-resources uri) conn (d/connect uri)]
          (loop [seq_22175 (seq [:eavt :aevt :avet :vaet]) chunk_22176 nil count_22177 0 i_22178 0]
            (if (< i_22178 count_22177)
              (let [index (.nth ^clojure.lang.Indexed chunk_22176 (int i_22178))]
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
                             (fn fn__22179
                               ([result]
                                 (let [G__22180 result]
                                   (case
                                     G__22180
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
                (recur seq_22175 chunk_22176 count_22177 (inc i_22178)))
              (let [temp__5457__auto__ (seq seq_22175)]
                (when temp__5457__auto__
                  (let [seq_22175 temp__5457__auto__]
                    (if (chunked-seq? seq_22175)
                      (let [c__5719__auto__ (chunk-first seq_22175)]
                        (recur
                          (chunk-rest seq_22175)
                          c__5719__auto__
                          (int (count c__5719__auto__))
                          (int 0)))
                      (let [index (first seq_22175)]
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
                                     (fn fn__22182
                                       ([result]
                                         (let [G__22183 result]
                                           (case
                                             G__22183
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
                        (recur (next seq_22175) nil 0 0))))))))))))
  (defn defcrosscheck
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
                                            (clojure.core/list 1000000))))))))))))))))))))))
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
  (defn get-db ([o] (if (instance? datomic.Database o) o (d/db o))))
  (defn crosscheck-indexes
    ([o]
      (let [m_22204 {:event :integrity/crosscheck-indexes}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22204 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (loop [seq_22208 (seq
                                                       ['crosscheck-eavt-aevt
                                                        'crosscheck-aevt-eavt
                                                        'crosscheck-avet-eavt
                                                        'crosscheck-avet-aevt
                                                        'crosscheck-raet-aevt
                                                        'crosscheck-raet-eavt
                                                        'crosscheck-aevt-avet
                                                        'crosscheck-eavt-avet])
                                           chunk_22209 nil
                                           count_22210 0
                                           i_22211 0]
                                      (if (< i_22211 count_22210)
                                        (let [c (.nth
                                                  ^clojure.lang.Indexed chunk_22209
                                                  (int i_22211))]
                                          (print "\n" c)
                                          (flush)
                                          (println
                                            ((ns-resolve 'datomic.integrity c)
                                              (get-db o)
                                              (get-db o)
                                              (fn fn__22213
                                                ([n]
                                                  (when (zero? (mod n 100000))
                                                    (print ".")
                                                    (flush))))))
                                          (flush)
                                          (recur seq_22208 chunk_22209 count_22210 (inc i_22211)))
                                        (let [temp__5457__auto__ (seq seq_22208)]
                                          (when temp__5457__auto__
                                            (let [seq_22208 temp__5457__auto__]
                                              (if (chunked-seq? seq_22208)
                                                (let [c__5719__auto__ (chunk-first seq_22208)]
                                                  (recur
                                                    (chunk-rest seq_22208)
                                                    c__5719__auto__
                                                    (int (count c__5719__auto__))
                                                    (int 0)))
                                                (let [c (first seq_22208)]
                                                  (print "\n" c)
                                                  (flush)
                                                  (println
                                                    ((ns-resolve 'datomic.integrity c)
                                                      (get-db o)
                                                      (get-db o)
                                                      (fn fn__22215
                                                        ([n]
                                                          (when (zero? (mod n 100000))
                                                            (print ".")
                                                            (flush))))))
                                                  (flush)
                                                  (recur (next seq_22208) nil 0 0))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_22205 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_22206 (logger/format-as-msec (long elapsed_22205))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_22204 :msec msec_22206 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn path-to-t
    ([cs olookup t]
      (let [temp__5457__auto__ (some-> (log/read-tail-descriptor cs) (first) (:d/r))]
        (when temp__5457__auto__
          (let [root_val_key temp__5457__auto__
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
  (defn dir-seg-info-seq
    ([dir olookup]
      (map
        (fn fn__22229
          ([d] {:dir-t (:t d), :segid (:uuid d), :seg-t (:t (first (get olookup (:uuid d))))}))
        dir)))
  (defn validate-t-order*
    ([log progress]
      (if (seq (iter/iter-seq (log/seek-tx log 0)))
        (do
          (when-not (= 1000 (-> (log/seek-tx log 0) (iter/iter-seq) (seq) (first) (:t)))
            (let [form__20659__auto__ (clojure.core/list
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
                  error__20660__auto__ (ex-info
                                         "Assertion failed, see ex-data for details"
                                         {:bindings {'log log, 'progress progress},
                                          :form form__20659__auto__})]
              (if datomic.assert/*assert-handler*
                (datomic.assert/*assert-handler* error__20660__auto__)
                (throw ^java.lang.Throwable error__20660__auto__))))
          (reduce
            (fn fn__22233
              ([ctr p__22232]
                (let [vec__22234 p__22232
                      tx1 (nth vec__22234 (int 0) nil)
                      tx2 (nth vec__22234 (int 1) nil)]
                  (when progress (^clojure.lang.IFn progress {:n ctr, :t (:t tx1)}))
                  (let [tx (d/t->tx (long (:t tx1))) max_eidx (log/max-eidx (:data tx1))]
                    (loop [seq_22237 (seq (:data tx1)) chunk_22238 nil count_22239 0 i_22240 0]
                      (if (< i_22240 count_22239)
                        (let [d (.nth ^clojure.lang.Indexed chunk_22238 (int i_22240))]
                          (when-not (= (:tx d) tx)
                            (let [form__20659__auto__ (clojure.core/list
                                                        '=
                                                        (clojure.core/list :tx 'd)
                                                        'tx)
                                  error__20660__auto__ (ex-info
                                                         "not all datoms in transaction have same tx"
                                                         {:bindings
                                                          {(.withMeta
                                                             'chunk_22238
                                                             {:tag 'clojure.lang.IChunk})
                                                           chunk_22238,
                                                           'max-eidx max_eidx,
                                                           'seq_22237 seq_22237,
                                                           'vec__22234 vec__22234,
                                                           'progress progress,
                                                           'log log,
                                                           'ctr ctr,
                                                           'tx1 tx1,
                                                           'tx tx,
                                                           'tx2 tx2,
                                                           'count_22239 (long count_22239),
                                                           'p__22232 p__22232,
                                                           'i_22240 (long i_22240),
                                                           'd d},
                                                          :form form__20659__auto__})]
                              (if datomic.assert/*assert-handler*
                                (datomic.assert/*assert-handler* error__20660__auto__)
                                (throw ^java.lang.Throwable error__20660__auto__))))
                          (recur seq_22237 chunk_22238 count_22239 (inc i_22240)))
                        (let [temp__5457__auto__ (seq seq_22237)]
                          (when temp__5457__auto__
                            (let [seq_22237 temp__5457__auto__]
                              (if (chunked-seq? seq_22237)
                                (let [c__5719__auto__ (chunk-first seq_22237)]
                                  (recur
                                    (chunk-rest seq_22237)
                                    c__5719__auto__
                                    (int (count c__5719__auto__))
                                    (int 0)))
                                (let [d (first seq_22237)]
                                  (when-not (= (:tx d) tx)
                                    (let [form__20659__auto__ (clojure.core/list
                                                                '=
                                                                (clojure.core/list :tx 'd)
                                                                'tx)
                                          error__20660__auto__ (ex-info
                                                                 "not all datoms in transaction have same tx"
                                                                 {:bindings
                                                                  {(.withMeta
                                                                     'chunk_22238
                                                                     {:tag 'clojure.lang.IChunk})
                                                                   chunk_22238,
                                                                   'max-eidx max_eidx,
                                                                   'seq_22237 seq_22237,
                                                                   'vec__22234 vec__22234,
                                                                   'progress progress,
                                                                   'log log,
                                                                   'ctr ctr,
                                                                   'temp__5457__auto__
                                                                   temp__5457__auto__,
                                                                   'tx1 tx1,
                                                                   'tx tx,
                                                                   'tx2 tx2,
                                                                   'count_22239 (long count_22239),
                                                                   'p__22232 p__22232,
                                                                   'i_22240 (long i_22240),
                                                                   'd d},
                                                                  :form form__20659__auto__})]
                                      (if datomic.assert/*assert-handler*
                                        (datomic.assert/*assert-handler* error__20660__auto__)
                                        (throw ^java.lang.Throwable error__20660__auto__))))
                                  (recur (next seq_22237) nil 0 0))))))))
                    (when tx2
                      (when-not (< (:t tx1) (:t tx2))
                        (let [form__20659__auto__ (clojure.core/list
                                                    '<
                                                    (clojure.core/list :t 'tx1)
                                                    (clojure.core/list :t 'tx2))
                              error__20660__auto__ (ex-info
                                                     "txes are not ascending"
                                                     {:bindings
                                                      {'max-eidx max_eidx,
                                                       'vec__22234 vec__22234,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'tx1 tx1,
                                                       'tx tx,
                                                       'tx2 tx2,
                                                       'p__22232 p__22232},
                                                      :form form__20659__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__20660__auto__)
                            (throw ^java.lang.Throwable error__20660__auto__))))
                      (when-not (<= (inc max_eidx) (:t tx2))
                        (let [form__20659__auto__ (clojure.core/list
                                                    '<=
                                                    (clojure.core/list 'inc 'max-eidx)
                                                    (clojure.core/list :t 'tx2))
                              error__20660__auto__ (ex-info
                                                     "entity t too high for tx"
                                                     {:bindings
                                                      {'max-eidx max_eidx,
                                                       'vec__22234 vec__22234,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'tx1 tx1,
                                                       'tx tx,
                                                       'tx2 tx2,
                                                       'p__22232 p__22232},
                                                      :form form__20659__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__20660__auto__)
                            (throw ^java.lang.Throwable error__20660__auto__))))))
                  (inc ctr))))
            0
            (partition-all 2 1 (iter/iter-seq (log/seek-tx log 0)))))
        0)))
  (defn validate-t-order
    ([uri log_fn progress]
      (let [m_22255 {:event :integrity/validate-t-order, :log-fn log_fn}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22255 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [uri (enhance-uri uri)
                                          map__22259 (tools/connection-resources uri)
                                          map__22259 (if (seq? map__22259)
                                                       (clojure.lang.PersistentHashMap/create
                                                         (seq map__22259))
                                                       map__22259)
                                          cluster (get map__22259 :cluster)
                                          olookup (get map__22259 :olookup)
                                          temp__5455__auto__ (let 
                                                               [G__22260 log_fn]
                                                               (case
                                                                 G__22260
                                                                 :create-log-val
                                                                 (log/create-log-val
                                                                   cluster
                                                                   olookup
                                                                   (d/db (d/connect uri)))
                                                                 :find-log
                                                                 (log/find-log cluster olookup)))]
                                      (if temp__5455__auto__
                                        (let [log temp__5455__auto__]
                                          (validate-t-order* log progress))
                                        0))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_22256 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_22257 (logger/format-as-msec (long elapsed_22256))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_22255 :msec msec_22257 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn log-dir-entry-seq
    ([p__22268 t]
      (let [map__22269 p__22268
            map__22269 (if (seq? map__22269)
                         (clojure.lang.PersistentHashMap/create (seq map__22269))
                         map__22269)
            cluster (get map__22269 :cluster)
            olookup (get map__22269 :olookup)
            temp__5457__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
        (when temp__5457__auto__
          (let [tree_iter temp__5457__auto__] (mapcat identity (log/log-dir-seq tree_iter)))))))
  (defn log-seg-t-seq
    ([p__22272 t]
      (let [map__22273 p__22272
            map__22273 (if (seq? map__22273)
                         (clojure.lang.PersistentHashMap/create (seq map__22273))
                         map__22273)
            cluster (get map__22273 :cluster)
            olookup (get map__22273 :olookup)
            temp__5457__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
        (when temp__5457__auto__
          (let [tree_iter temp__5457__auto__]
            (map :t (mapcat identity (log/log-seg-seq tree_iter))))))))
  (defn crosscheck-dir-segs
    ([p__22277 progress]
      (let [map__22278 p__22277
            map__22278 (if (seq? map__22278)
                         (clojure.lang.PersistentHashMap/create (seq map__22278))
                         map__22278)
            cluster (get map__22278 :cluster)
            olookup (get map__22278 :olookup)
            m_22279 {:event :integrity/crosscheck-dir-segs}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_22279 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (let [temp__5457__auto__ (log/find-log cluster olookup)]
                                      (when temp__5457__auto__
                                        (let [log temp__5457__auto__
                                              temp__5457__auto__ (log/seek-tx log 0)]
                                          (when temp__5457__auto__
                                            (let [tree_iter temp__5457__auto__]
                                              (loop [seq_22283 (seq (log/log-dir-seq tree_iter))
                                                     chunk_22284 nil
                                                     count_22285 0
                                                     i_22286 0]
                                                (if (< i_22286 count_22285)
                                                  (let [adir (.nth
                                                               ^clojure.lang.Indexed chunk_22284
                                                               (int i_22286))]
                                                    (when progress
                                                      (^clojure.lang.IFn progress adir))
                                                    (let [bad_dirs (seq
                                                                     (remove
                                                                       (fn 
                                                                         fn__22288
                                                                         ([p1__22276#]
                                                                           (=
                                                                             (:dir-t p1__22276#)
                                                                             (:seg-t p1__22276#))))
                                                                       (dir-seg-info-seq
                                                                         adir
                                                                         olookup)))]
                                                      (when-not (not bad_dirs)
                                                        (let [form__20659__auto__
                                                              (clojure.core/list 'not 'bad-dirs)
                                                              error__20660__auto__
                                                              (ex-info
                                                                "Assertion failed, see ex-data for details"
                                                                {:bindings
                                                                 {'p__22277 p__22277,
                                                                  'olookup olookup,
                                                                  'progress progress,
                                                                  '___8980__auto__ ___8980__auto__,
                                                                  'm_22279 m_22279,
                                                                  'log log,
                                                                  'adir adir,
                                                                  'count_22285 (long count_22285),
                                                                  'i_22286 (long i_22286),
                                                                  'tree-iter tree_iter,
                                                                  'seq_22283 seq_22283,
                                                                  'temp__5457__auto__
                                                                  temp__5457__auto__,
                                                                  'start__8981__auto__
                                                                  (long start__8981__auto__),
                                                                  'bad-dirs bad_dirs,
                                                                  (.withMeta
                                                                    'chunk_22284
                                                                    {:tag 'clojure.lang.IChunk})
                                                                  chunk_22284,
                                                                  'map__22278 map__22278,
                                                                  'cluster cluster},
                                                                 :form form__20659__auto__})]
                                                          (if datomic.assert/*assert-handler*
                                                            (datomic.assert/*assert-handler*
                                                              error__20660__auto__)
                                                            (throw
                                                              ^java.lang.Throwable error__20660__auto__)))))
                                                    (recur
                                                      seq_22283
                                                      chunk_22284
                                                      count_22285
                                                      (inc i_22286)))
                                                  (let [temp__5457__auto__ (seq seq_22283)]
                                                    (when temp__5457__auto__
                                                      (let [seq_22283 temp__5457__auto__]
                                                        (if (chunked-seq? seq_22283)
                                                          (let [c__5719__auto__
                                                                (chunk-first seq_22283)]
                                                            (recur
                                                              (chunk-rest seq_22283)
                                                              c__5719__auto__
                                                              (int (count c__5719__auto__))
                                                              (int 0)))
                                                          (let [adir (first seq_22283)]
                                                            (when
                                                              progress
                                                              (^clojure.lang.IFn progress adir))
                                                            (let 
                                                              [bad_dirs
                                                               (seq
                                                                 (remove
                                                                   (fn 
                                                                     fn__22290
                                                                     ([p1__22276#]
                                                                       (=
                                                                         (:dir-t p1__22276#)
                                                                         (:seg-t p1__22276#))))
                                                                   (dir-seg-info-seq
                                                                     adir
                                                                     olookup)))]
                                                              (when-not
                                                                (not bad_dirs)
                                                                (let 
                                                                  [form__20659__auto__
                                                                   (clojure.core/list
                                                                     'not
                                                                     'bad-dirs)
                                                                   error__20660__auto__
                                                                   (ex-info
                                                                     "Assertion failed, see ex-data for details"
                                                                     {:bindings
                                                                      {'p__22277 p__22277,
                                                                       'olookup olookup,
                                                                       'progress progress,
                                                                       '___8980__auto__
                                                                       ___8980__auto__,
                                                                       'm_22279 m_22279,
                                                                       'log log,
                                                                       'adir adir,
                                                                       'count_22285
                                                                       (long count_22285),
                                                                       'i_22286 (long i_22286),
                                                                       'tree-iter tree_iter,
                                                                       'seq_22283 seq_22283,
                                                                       'temp__5457__auto__
                                                                       temp__5457__auto__,
                                                                       'start__8981__auto__
                                                                       (long start__8981__auto__),
                                                                       'bad-dirs bad_dirs,
                                                                       (.withMeta
                                                                         'chunk_22284
                                                                         {:tag
                                                                          'clojure.lang.IChunk})
                                                                       chunk_22284,
                                                                       'map__22278 map__22278,
                                                                       'cluster cluster},
                                                                      :form form__20659__auto__})]
                                                                  (if
                                                                    datomic.assert/*assert-handler*
                                                                    (datomic.assert/*assert-handler*
                                                                      error__20660__auto__)
                                                                    (throw
                                                                      ^java.lang.Throwable error__20660__auto__)))))
                                                            (recur
                                                              (next seq_22283)
                                                              nil
                                                              0
                                                              0)))))))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_22280 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_22281 (logger/format-as-msec (long elapsed_22280))]
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_22279 :msec msec_22281 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn merge-seqs
    ([cmp s1 s2 s3 s4] (merge-seqs cmp s1 (merge-seqs cmp s2 s3 s4)))
    ([cmp s1 s2 s3] (merge-seqs cmp s1 (merge-seqs cmp s2 s3)))
    ([cmp s1 s2]
      (let [s1 (seq s1) s2 (seq s2)]
        (if (and s1 s2)
          (let [vec__22307 s1
                seq__22308 (seq vec__22307)
                first__22309 (first seq__22308)
                seq__22308 (next seq__22308)
                o1 first__22309
                m1 seq__22308
                vec__22310 s2
                seq__22311 (seq vec__22310)
                first__22312 (first seq__22311)
                seq__22311 (next seq__22311)
                o2 first__22312
                m2 seq__22311]
            (if (< (.compare ^java.util.Comparator cmp o1 o2) 0)
              (lazy-seq (cons o1 (merge-seqs cmp m1 s2)))
              (lazy-seq (cons o2 (merge-seqs cmp s1 m2)))))
          (or s1 s2)))))
  (defn aevt-dquark-seq
    ([db d]
      (map
        (fn fn__22321 ([p1__22320#] (dissoc p1__22320# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__22326
              ([p__22325]
                (let [vec__22327 p__22325
                      index (nth vec__22327 (int 0) nil)
                      iter (nth vec__22327 (int 1) nil)]
                  (map
                    (fn fn__22331
                      ([p__22330]
                        (let [map__22332 p__22330
                              map__22332 (if (seq? map__22332)
                                           (clojure.lang.PersistentHashMap/create (seq map__22332))
                                           map__22332)
                              datom map__22332
                              e (get map__22332 :e)
                              a (get map__22332 :a)
                              v (get map__22332 :v)
                              tx (get map__22332 :tx)
                              added (get map__22332 :added)]
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
                (let [temp__5457__auto__ (:indexing db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:aevt index))))
                d)]
             [:index
              (btset/seek
                (let [temp__5457__auto__ (:index db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:aevt index))))
                d)]
             [:mid-index
              (btset/seek
                (let [temp__5457__auto__ (:mid-index db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:aevt index))))
                d)]
             [:history
              (btset/seek
                (let [temp__5457__auto__ (:history db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:aevt index))))
                d)]])))))
  (defn eavt-dquark-seq
    ([db d]
      (map
        (fn fn__22341 ([p1__22340#] (dissoc p1__22340# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__22346
              ([p__22345]
                (let [vec__22347 p__22345
                      index (nth vec__22347 (int 0) nil)
                      iter (nth vec__22347 (int 1) nil)]
                  (map
                    (fn fn__22351
                      ([p__22350]
                        (let [map__22352 p__22350
                              map__22352 (if (seq? map__22352)
                                           (clojure.lang.PersistentHashMap/create (seq map__22352))
                                           map__22352)
                              datom map__22352
                              e (get map__22352 :e)
                              a (get map__22352 :a)
                              v (get map__22352 :v)
                              tx (get map__22352 :tx)
                              added (get map__22352 :added)]
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
                (let [temp__5457__auto__ (:indexing db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:eavt index))))
                d)]
             [:index
              (btset/seek
                (let [temp__5457__auto__ (:index db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:eavt index))))
                d)]
             [:mid-index
              (btset/seek
                (let [temp__5457__auto__ (:mid-index db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:eavt index))))
                d)]
             [:history
              (btset/seek
                (let [temp__5457__auto__ (:history db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:eavt index))))
                d)]])))))
  (defn avet-dquark-seq
    ([db d]
      (map
        (fn fn__22361 ([p1__22360#] (dissoc p1__22360# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__22366
              ([p__22365]
                (let [vec__22367 p__22365
                      index (nth vec__22367 (int 0) nil)
                      iter (nth vec__22367 (int 1) nil)]
                  (map
                    (fn fn__22371
                      ([p__22370]
                        (let [map__22372 p__22370
                              map__22372 (if (seq? map__22372)
                                           (clojure.lang.PersistentHashMap/create (seq map__22372))
                                           map__22372)
                              datom map__22372
                              e (get map__22372 :e)
                              a (get map__22372 :a)
                              v (get map__22372 :v)
                              tx (get map__22372 :tx)
                              added (get map__22372 :added)]
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
                (let [temp__5457__auto__ (:indexing db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:avet index))))
                d)]
             [:index
              (btset/seek
                (let [temp__5457__auto__ (:index db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:avet index))))
                d)]
             [:mid-index
              (btset/seek
                (let [temp__5457__auto__ (:mid-index db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:avet index))))
                d)]
             [:history
              (btset/seek
                (let [temp__5457__auto__ (:history db)]
                  (when temp__5457__auto__ (let [index temp__5457__auto__] (:avet index))))
                d)]])))))
  (defn validate-log-cli
    ([uri]
      (let [uri (enhance-uri uri)
            cr (tools/connection-resources uri)
            count (atom 0)
            progress (fn progress
                       ([p1__22380#]
                         (fn fn__22382
                           ([_ & more]
                             (when (zero? (mod (swap! count inc) p1__22380#))
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
  (defn validate-memory-safe
    ([uri]
      (let [uri (enhance-uri uri)]
        (validate-log-cli uri)
        (let [conn (d/connect uri)] (crosscheck-log-cli {:uri uri})))))
  (defn excisions
    ([db] (seq (map (fn fn__22387 ([d] (d/entity db (:e d)))) (d/datoms db :aevt :db/excise)))))
  (defn e-ts
    ([db e]
      (sort
        (distinct
          (map
            (fn fn__22391 ([p1__22390#] (long (d/tx->t (:tx p1__22390#)))))
            (d/datoms db :eavt e))))))
  (defn validate-excision
    ([spec]
      (let [db (d/entity-db spec)
            id (fn id ([p1__22394#] (or (:db/id p1__22394#) (db/resolve-id db p1__22394#))))
            target (:db/excise spec)
            before_t (let [temp__5455__auto__ (excise/get-before-t db spec)]
                       (if temp__5455__auto__
                         (let [bt temp__5455__auto__] (dec bt))
                         (d/basis-t db)))
            spec_t (first (e-ts db (:db/id spec)))
            t (min (min before_t spec_t) (:indexBasisT db))
            valdb (d/as-of db t)
            type (if (db/attribute db (^clojure.lang.IFn id target)) :a :e)]
        (let [G__22398 type]
          (case
            G__22398
            :a
            (let [datoms (seq (d/datoms valdb :aevt (^clojure.lang.IFn id target)))]
              (when-not (nil? datoms)
                (throw
                  (ex-info
                    "Found datoms that should have been excised"
                    {:spec spec, :datoms datoms, :t t}))
                (clojure.lang.Util/hash G__22398)))
            :e
            (let [ent (d/entity valdb (^clojure.lang.IFn id target))
                  attrs (into #{} (:db.excise/attrs spec))]
              (cond
                (seq attrs) (when (some attrs (keys ent))
                              (throw
                                (ex-info
                                  "Found entity attributes that should have been excised"
                                  {:spec spec, :attrs attrs, :entity ent, :t t}))
                              (clojure.lang.Util/hash G__22398))
                (seq (keys ent)) (do
                                   (throw
                                     (ex-info
                                       "Found entity that should have been excised"
                                       {:spec spec, :entity ent, :t t}))
                                   (clojure.lang.Util/hash G__22398))))))
        spec)))
  (defn validate-indexed-excisions
    ([db]
      (dorun
        (map
          (fn fn__22402 ([spec] (validate-excision spec) (print ".") (flush) spec))
          (take-while
            (fn fn__22404
              ([p1__22401#] (<= (first (e-ts db (:db/id p1__22401#))) (:indexBasisT db))))
            (excisions db))))))
  (defn clusterfs-path-reachability
    ([cfs olookup progress]
      (mapcat
        (fn fn__22408
          ([p__22407]
            (let [vec__22409 p__22407
                  filename (nth vec__22409 (int 0) nil)
                  map__22412 (nth vec__22409 (int 1) nil)
                  map__22412 (if (seq? map__22412)
                               (clojure.lang.PersistentHashMap/create (seq map__22412))
                               map__22412)
                  base (get map__22412 :base)
                  length (get map__22412 :length)]
              (map
                (fn fn__22413
                  ([ckey]
                    (let [item [filename ckey (boolean (get olookup ckey))]]
                      (when progress (^clojure.lang.IFn progress item))
                      item)))
                (clusterfs/file-chunk-keys cfs filename)))))
        (:dir cfs))))
  (defn fulltext-path-reachability
    ([ft db olookup progress]
      (when ft
        (mapcat
          (fn fn__22418
            ([p__22417]
              (let [vec__22419 p__22417
                    attrid (nth vec__22419 (int 0) nil)
                    uuid (nth vec__22419 (int 1) nil)
                    temp__5455__auto__ (get olookup (str uuid))]
                (if temp__5455__auto__
                  (let [cfs temp__5455__auto__]
                    (cons
                      [(d/ident db attrid) (str uuid) true]
                      (clusterfs-path-reachability cfs olookup progress)))
                  [(d/ident db attrid) (str uuid) false]))))
          (:attrmap ft)))))
  (defn validate-fulltext
    ([db olookup]
      (let [count (atom 0)
            progress (fn progress
                       ([& _] (when (zero? (mod (swap! count inc) 100)) (print ".") (flush))))
            temp__5457__auto__ (seq
                                 (remove
                                   (fn fn__22428 ([p1__22425#] (nth p1__22425# (int 2))))
                                   (-> (:index db)
                                    (:fulltext)
                                    (:root)
                                    (fulltext-path-reachability db olookup progress)
                                    (:history db)
                                    (:fulltext)
                                    (:root)
                                    (fulltext-path-reachability db olookup progress)
                                    (concat))))]
        (when temp__5457__auto__
          (let [missing temp__5457__auto__]
            (throw (ex-info "Some fulltext paths are missing" {:paths missing})))
          nil))))
  (defn tx-range-ts ([conn] (map :t (d/tx-range (d/log conn) 1000 (d/next-t (d/db conn))))))
  (defn tx-instant-ts
    ([db]
      (drop-while
        (fn fn__22434 ([p1__22433#] (< p1__22433# 1000)))
        (map (comp d/tx->t :tx) (d/datoms db :avet :db/txInstant)))))
  (defn seq-diffs
    ([colla collb progress n]
      (let [G__22443 (seq colla)
            vec__22445 G__22443
            seq__22446 (seq vec__22445)
            first__22447 (first seq__22446)
            seq__22446 (next seq__22446)
            a first__22447
            morea seq__22446
            G__22444 (seq collb)
            vec__22448 G__22444
            seq__22449 (seq vec__22448)
            first__22450 (first seq__22449)
            seq__22449 (next seq__22449)
            b first__22450
            moreb seq__22449
            n n]
        (loop [G__22443 G__22443 G__22444 G__22444 n n]
          (let [vec__22451 G__22443
                seq__22452 (seq vec__22451)
                first__22453 (first seq__22452)
                seq__22452 (next seq__22452)
                a first__22453
                morea seq__22452
                vec__22454 G__22444
                seq__22455 (seq vec__22454)
                first__22456 (first seq__22455)
                seq__22455 (next seq__22455)
                b first__22456
                moreb seq__22455
                n n]
            (^clojure.lang.IFn progress n)
            (when (or a b)
              (if (= a b)
                (recur morea moreb (inc n))
                (lazy-seq (cons {:a a, :b b, :n n} (seq-diffs morea moreb progress (inc n))))))))))
    ([colla collb progress] (seq-diffs colla collb progress 0)))
  (defn validate-garbage
    ([cr progress]
      (let [temp__5455__auto__ (deref
                                 (cluster/get-ref
                                   (:cluster cr)
                                   (garbage/root-ref-key (:cluster cr))))]
        (if temp__5455__auto__
          (let [root_key temp__5455__auto__
                root (get (:olookup cr) (:key root_key))
                valid? (comp not empty?)
                result (progress-reduce
                         (fn fn__22461
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
  (defn crosscheck-tx-range-with-tx-instant
    ([conn]
      (let [count (atom 0)
            progress (fn progress
                       ([& _] (when (zero? (mod (swap! count inc) 10000)) (print ".") (flush))))
            temp__5457__auto__ (seq-diffs (tx-range-ts conn) (tx-instant-ts (d/db conn)) progress)]
        (when temp__5457__auto__
          (let [diffs temp__5457__auto__]
            (throw
              (ex-info
                "tx-range and :db/txInstant did not agree on ts."
                {:desc ":a values are from tx-range, :b values from :db/txInstant, :n offset",
                 :diffs diffs})))
          nil))))
  (defn validate-nohistory
    ([db]
      (let [hdb (cauterize (d/history db) :memidx :indexing :mid-index :index)
            temp__5455__auto__ (seq (attr-datoms db (nohistory-attrs db)))]
        (when temp__5455__auto__
          (let [datoms temp__5455__auto__]
            (throw (ex-info "Found :db/noHistory datoms in the history index" {:datoms datoms})))
          nil))))
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
  (defn aevt-avet-stats
    ([db]
      (let [nohists (tools/ever-nohistory-attrs db)]
        (map
          (fn fn__22481 ([p1__22480#] (stat-counts db (.id ^datomic.db.Attribute p1__22480#))))
          (remove
            (fn fn__22483
              ([p1__22479#] (contains? nohists (.id ^datomic.db.Attribute p1__22479#))))
            (filter
              (fn fn__22485 ([p1__22478#] (.hasAVET ^datomic.db.Attribute p1__22478#)))
              (db/attribute-seq db)))))))
  (defn aevt-avet-stats-consistent?
    ([p__22488]
      (let [map__22489 p__22488
            map__22489 (if (seq? map__22489)
                         (clojure.lang.PersistentHashMap/create (seq map__22489))
                         map__22489)
            aevt_total (get map__22489 :aevt-total)
            avet_total (get map__22489 :avet-total)]
        (= aevt_total avet_total))))
  (defn report-aevt-avet-stats
    ([db]
      (let [temp__5457__auto__ (seq (remove aevt-avet-stats-consistent? (aevt-avet-stats db)))]
        (when temp__5457__auto__
          (let [mismatch temp__5457__auto__] (prn {:stats-mismatch mismatch}))))))
  (defn validate-index-totals
    ([db]
      (let [eavt (stats/datom-counts stats/eavt db)
            aevt (stats/datom-counts stats/aevt db)
            summary {:eavt eavt, :aevt aevt}
            total_datoms (fn total_datoms
                           ([p1__22493#]
                             (apply
                               +
                               (vals
                                 (select-keys
                                   p1__22493#
                                   [:index-datoms :mid-index-datoms :history-datoms])))))
            eavt (assoc eavt :total-datoms (^clojure.lang.IFn total_datoms eavt))
            aevt (assoc aevt :total-datoms (^clojure.lang.IFn total_datoms aevt))]
        (if (= (:total-datoms eavt) (:total-datoms aevt))
          summary
          (do (throw (ex-info (str "total datoms not equal" summary) summary)) nil)))))
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
          (let [form__20659__auto__ (clojure.core/list
                                      'nil?
                                      (clojure.core/list 'ic/unique-collisions 'db 'identity))
                error__20660__auto__ (ex-info
                                       "Assertion failed, see ex-data for details"
                                       {:bindings {'uri uri, 'cr cr, 'conn conn, 'db db},
                                        :form form__20659__auto__})]
            (if datomic.assert/*assert-handler*
              (datomic.assert/*assert-handler* error__20660__auto__)
              (throw ^java.lang.Throwable error__20660__auto__))))
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
  (def diagnostics tools/diagnostics)
  (defn describe-segment
    ([uri segment_id]
      (let [uri (enhance-uri uri)
            cr (tools/connection-resources uri)
            buf (:buf (deref (cluster/get-val (:cluster cr) (str segment_id))))]
        (dio/describe-bbuf buf))))
  (defn fulltext-storage-seq
    ([uri k]
      (let [uri (enhance-uri uri)
            map__22502 (tools/connection-resources uri)
            map__22502 (if (seq? map__22502)
                         (clojure.lang.PersistentHashMap/create (seq map__22502))
                         map__22502)
            cluster (get map__22502 :cluster)
            olookup (get map__22502 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)]
        (when root_id
          (let [root (get olookup root_id)
                branch? (fn branch_QMARK_
                          ([p__22503]
                            (let [map__22505 p__22503
                                  map__22505 (if (seq? map__22505)
                                               (clojure.lang.PersistentHashMap/create
                                                 (seq map__22505))
                                               map__22505)
                                  seg (get map__22505 :seg)]
                              (or
                                (instance? datomic.fulltext.Root seg)
                                (instance? datomic.clusterfs.ClusterFS seg)))))
                children (fn children
                           ([p__22508]
                             (let [map__22510 p__22508
                                   map__22510 (if (seq? map__22510)
                                                (clojure.lang.PersistentHashMap/create
                                                  (seq map__22510))
                                                map__22510)
                                   seg (get map__22510 :seg)]
                               (cond
                                 (instance? datomic.fulltext.Root seg) (map
                                                                         (fn 
                                                                           fn__22511
                                                                           ([p1__22501#]
                                                                             (let 
                                                                               [uuid
                                                                                (str p1__22501#)]
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
                                                                                   fn__22513
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
  (defn log-storage-seq
    ([uri]
      (let [uri (enhance-uri uri)
            map__22517 (tools/connection-resources uri)
            map__22517 (if (seq? map__22517)
                         (clojure.lang.PersistentHashMap/create (seq map__22517))
                         map__22517)
            cluster (get map__22517 :cluster)
            olookup (get map__22517 :olookup)
            root_id (log/root-id cluster)
            branch? (fn branch_QMARK_
                      ([p__22518]
                        (let [map__22520 p__22518
                              map__22520 (if (seq? map__22520)
                                           (clojure.lang.PersistentHashMap/create (seq map__22520))
                                           map__22520)
                              seg (get map__22520 :seg)]
                          (instance? datomic.log.LogDir (first seg)))))
            children (fn children
                       ([p__22522]
                         (let [map__22524 p__22522
                               map__22524 (if (seq? map__22524)
                                            (clojure.lang.PersistentHashMap/create
                                              (seq map__22524))
                                            map__22524)
                               uuid (get map__22524 :uuid)
                               seg (get map__22524 :seg)]
                           (map
                             (fn fn__22525
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
  (defn index-storage-seq
    ([uri k]
      (let [uri (enhance-uri uri)
            map__22529 (tools/connection-resources uri)
            map__22529 (if (seq? map__22529)
                         (clojure.lang.PersistentHashMap/create (seq map__22529))
                         map__22529)
            cluster (get map__22529 :cluster)
            olookup (get map__22529 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)
            branch? (fn branch_QMARK_
                      ([p__22530]
                        (let [map__22532 p__22530
                              map__22532 (if (seq? map__22532)
                                           (clojure.lang.PersistentHashMap/create (seq map__22532))
                                           map__22532)
                              seg (get map__22532 :seg)]
                          (or
                            (instance? datomic.index.RootNode seg)
                            (instance? datomic.index.DirNode seg)))))
            children (fn children
                       ([p__22535]
                         (let [map__22537 p__22535
                               map__22537 (if (seq? map__22537)
                                            (clojure.lang.PersistentHashMap/create
                                              (seq map__22537))
                                            map__22537)
                               seg (get map__22537 :seg)]
                           (cond
                             (instance? datomic.index.RootNode seg) (map
                                                                      (fn 
                                                                        fn__22538
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
                                                                         fn__22540
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
  (defn pod-storage-seq
    ([uri]
      (let [uri (enhance-uri uri)
            map__22544 (tools/connection-resources uri)
            map__22544 (if (seq? map__22544)
                         (clojure.lang.PersistentHashMap/create (seq map__22544))
                         map__22544)
            cluster (get map__22544 :cluster)
            olookup (get map__22544 :olookup)
            pod_key (log/tail-pod-key cluster)
            pod_meta (deref (cluster/get-pod-meta cluster pod_key))
            mkv (fn mkv
                  ([id]
                    (let [temp__5455__auto__ (kvs/get (cluster/get-ref-store cluster) id false)]
                      (if temp__5455__auto__
                        (let [entry temp__5455__auto__]
                          (update-in
                            entry
                            [:v]
                            (fn fn__22546
                              ([bb]
                                (when bb (fressian/defressian bb :handlers log/read-handlers))))))
                        {:id id}))))]
        (cons
          (if pod_meta (assoc pod_meta :type :pod-key) {:id pod_key})
          (when pod_meta
            (take-while
              identity
              (iterate
                (fn fn__22551
                  ([p__22550]
                    (let [map__22552 p__22550
                          map__22552 (if (seq? map__22552)
                                       (clojure.lang.PersistentHashMap/create (seq map__22552))
                                       map__22552)
                          prev (get map__22552 :prev)]
                      (when prev (^clojure.lang.IFn mkv prev)))))
                (^clojure.lang.IFn mkv (:tail pod_meta)))))))))
  (defn esafe
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
                (clojure.core/list 't__22555__auto__)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list '.printStackTrace)
                      (clojure.core/list 't__22555__auto__)))))))))))
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
  (defn -main*
    ([p__22558]
      (let [map__22559 p__22558
            map__22559 (if (seq? map__22559)
                         (clojure.lang.PersistentHashMap/create (seq map__22559))
                         map__22559)
            uri (get map__22559 :uri)
            validate (get map__22559 :validate)]
        (let [uri (enhance-uri uri)]
          (println "\nDiagnostics:")
          (pp/pprint
            (try
              (diagnostics uri)
              (catch
                java.lang.Throwable
                t__22555__auto__
                (do (.printStackTrace ^java.lang.Throwable t__22555__auto__) nil)))))
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
                    (fn fn__22563 ([p1__22557#] (= 1 (long (count p1__22557#)))))
                    (pod-storage-seq uri))
                  (catch
                    java.lang.Throwable
                    t__22555__auto__
                    (do (.printStackTrace ^java.lang.Throwable t__22555__auto__) nil)))))
            (println "\nMissing log segments: ")
            (prn
              (seq
                (try
                  (remove :seg (log-storage-seq uri))
                  (catch
                    java.lang.Throwable
                    t__22555__auto__
                    (do (.printStackTrace ^java.lang.Throwable t__22555__auto__) nil)))))
            (loop [seq_22568 (seq index-sort-root-keys) chunk_22569 nil count_22570 0 i_22571 0]
              (if (< i_22571 count_22570)
                (let [k (.nth ^clojure.lang.Indexed chunk_22569 (int i_22571))]
                  (println "\nMissing segments in " k)
                  (prn
                    (seq
                      (try
                        (remove :seg (index-storage-seq uri k))
                        (catch
                          java.lang.Throwable
                          t__22555__auto__
                          (do (.printStackTrace ^java.lang.Throwable t__22555__auto__) nil)))))
                  (recur seq_22568 chunk_22569 count_22570 (inc i_22571)))
                (let [temp__5457__auto__ (seq seq_22568)]
                  (when temp__5457__auto__
                    (let [seq_22568 temp__5457__auto__]
                      (if (chunked-seq? seq_22568)
                        (let [c__5719__auto__ (chunk-first seq_22568)]
                          (recur
                            (chunk-rest seq_22568)
                            c__5719__auto__
                            (int (count c__5719__auto__))
                            (int 0)))
                        (let [k (first seq_22568)]
                          (println "\nMissing segments in " k)
                          (prn
                            (seq
                              (try
                                (remove :seg (index-storage-seq uri k))
                                (catch
                                  java.lang.Throwable
                                  t__22555__auto__
                                  (do
                                    (.printStackTrace ^java.lang.Throwable t__22555__auto__)
                                    nil)))))
                          (recur (next seq_22568) nil 0 0))))))))
            (loop [seq_22576 (seq [:fulltext :fulltext-hist])
                   chunk_22577 nil
                   count_22578 0
                   i_22579 0]
              (if (< i_22579 count_22578)
                (let [k (.nth ^clojure.lang.Indexed chunk_22577 (int i_22579))]
                  (println "\nMissing segments in " k)
                  (prn
                    (seq
                      (try
                        (remove :seg (fulltext-storage-seq uri k))
                        (catch
                          java.lang.Throwable
                          t__22555__auto__
                          (do (.printStackTrace ^java.lang.Throwable t__22555__auto__) nil)))))
                  (recur seq_22576 chunk_22577 count_22578 (inc i_22579)))
                (let [temp__5457__auto__ (seq seq_22576)]
                  (when temp__5457__auto__
                    (let [seq_22576 temp__5457__auto__]
                      (if (chunked-seq? seq_22576)
                        (let [c__5719__auto__ (chunk-first seq_22576)]
                          (recur
                            (chunk-rest seq_22576)
                            c__5719__auto__
                            (int (count c__5719__auto__))
                            (int 0)))
                        (let [k (first seq_22576)]
                          (println "\nMissing segments in " k)
                          (prn
                            (seq
                              (try
                                (remove :seg (fulltext-storage-seq uri k))
                                (catch
                                  java.lang.Throwable
                                  t__22555__auto__
                                  (do
                                    (.printStackTrace ^java.lang.Throwable t__22555__auto__)
                                    nil)))))
                          (recur (next seq_22576) nil 0 0))))))))
            (println "\nDone!"))))))
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
        (finally (d/shutdown true))))))
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
  (defmulti enhance-uri (fn fn__32029 ([uri] (:protocol (uri/parse uri)))))
  (defmethod enhance-uri :default fn__32034 ([uri] uri))
  (defmethod
    enhance-uri
    :ddb+s3
    fn__32036
    ([uri] (uri/create (merge (uri/parse uri) {:skip-efs true}))))
  (defn progress-reduce
    ([f val p__32038 coll]
      (let [map__32039 p__32038
            map__32039 (if (seq? map__32039)
                         (if (next map__32039)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32039))
                           (if (seq map__32039) (first map__32039) {}))
                         map__32039)
            progress (get map__32039 :progress)
            n (get map__32039 :n)]
        (if progress
          (let [pf (fn pf
                     ([p__32040 item]
                       (let [vec__32042 p__32040
                             c (nth vec__32042 (int 0) nil)
                             acc (nth vec__32042 (int 1) nil)]
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
            (fn fn__32050 ([p1__32049#] (.-noHistory ^datomic.db.Attribute p1__32049#)))
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
    ([p__32055]
      (let [vec__32056 p__32055 d1 (nth vec__32056 (int 0) nil) d2 (nth vec__32056 (int 1) nil)]
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
              (fn fn__32064
                ([p__32063]
                  (let [vec__32065 p__32063 e (nth vec__32065 (int 0) nil)] (d/attribute db e))))
              (d/q [:find '?e :where ['?e :db/valueType]] db)))))))
  (defn make-tupler
    ([index]
      (let [G__32070 index]
        (case
          G__32070
          :aevt
          (fn fn__32071 ([d] [(:a d) (:e d) (:v d) (:tx d)]))
          :avet
          (fn fn__32073 ([d] [(:a d) (:v d) (:e d) (:tx d)]))
          :eavt
          (fn fn__32075 ([d] [(:e d) (:a d) (:v d) (:tx d)]))
          :vaet
          (fn fn__32077 ([d] [(:v d) (:a d) (:e d) (:tx d)]))))))
  (defn unfindable-datom-seq
    ([db index progress]
      (let [op (let [G__32089 index]
                 (case
                   G__32089
                   :aevt
                   (fn fn__32090
                     ([p1__32083# p2__32084#]
                       (.seekAEVT ^datomic.db.IDb p1__32083# ^datomic.impl.db.IDatum p2__32084#)))
                   :avet
                   (fn fn__32092
                     ([p1__32085# p2__32086#]
                       (.seekAVET ^datomic.db.IDb p1__32085# ^datomic.impl.db.IDatum p2__32086#)))
                   :eavt
                   (fn fn__32094
                     ([p1__32081# p2__32082#]
                       (.seekEAVT ^datomic.db.IDb p1__32081# ^datomic.impl.db.IDatum p2__32082#)))
                   :vaet
                   (fn fn__32096
                     ([p1__32087# p2__32088#]
                       (.seekRAET
                         ^datomic.db.IDb p1__32087#
                         ^datomic.impl.db.IDatum p2__32088#)))))]
        (remove
          (fn fn__32098
            ([datom]
              (when progress (^clojure.lang.IFn progress))
              (= datom (.get (^clojure.lang.IFn op db datom)))))
          (d/datoms db index)))))
  (defn progress-dot-fn
    ([n]
      (let [c (atom 0)]
        (fn fn__32101 ([& _] (when (zero? (mod (swap! c inc) n)) (print ".") (flush)))))))
  (defn datom-comparator
    ([sort]
      (let [G__32104 sort]
        (case G__32104 :aevt db/aevt-cmp :avet db/avet-cmp :eavt db/eavt-cmp :vaet db/raet-cmp))))
  (defn unsorted-dirs
    ([db tier sort progress]
      (let [cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__32106
            ([a b]
              (^clojure.lang.IFn progress)
              (< (.compare ^java.util.Comparator cmp (:key a) (:key b)) 0)))
          (some-> db (^clojure.lang.IFn tier) (^clojure.lang.IFn sort) (.seek) (index/dir-seq))))))
  (defn validate-dir-sorts
    ([db]
      (let [tses (let [iter__6373__auto__ (fn iter__32110
                                            ([s__32111]
                                              (lazy-seq
                                                (loop [s__32111 s__32111]
                                                  (let [temp__5804__auto__ (seq s__32111)]
                                                    (when temp__5804__auto__
                                                      (let [xs__6360__auto__ temp__5804__auto__
                                                            tier (first xs__6360__auto__)
                                                            iterys__6369__auto__
                                                            (fn 
                                                              iter__32112
                                                              ([s__32113]
                                                                (lazy-seq
                                                                  (let 
                                                                    [s__32113 s__32113
                                                                     temp__5804__auto__
                                                                     (seq s__32113)]
                                                                    (when
                                                                      temp__5804__auto__
                                                                      (let 
                                                                        [s__32113
                                                                         temp__5804__auto__]
                                                                        (if
                                                                          (chunked-seq? s__32113)
                                                                          (let 
                                                                            [c__6371__auto__
                                                                             (chunk-first s__32113)
                                                                             size__6372__auto__
                                                                             (int
                                                                               (count
                                                                                 c__6371__auto__))
                                                                             b__32115
                                                                             (chunk-buffer
                                                                               (java.lang.Integer/valueOf
                                                                                 (int
                                                                                   size__6372__auto__)))]
                                                                            (if
                                                                              (loop 
                                                                                [i__32114 (int 0)]
                                                                                (if
                                                                                  (<
                                                                                    i__32114
                                                                                    size__6372__auto__)
                                                                                  (let 
                                                                                    [sort
                                                                                     (.nth
                                                                                       ^clojure.lang.Indexed c__6371__auto__
                                                                                       (int
                                                                                         i__32114))]
                                                                                    (chunk-append
                                                                                      b__32115
                                                                                      [tier sort])
                                                                                    (recur
                                                                                      (inc
                                                                                        i__32114)))
                                                                                  true))
                                                                              (chunk-cons
                                                                                (chunk b__32115)
                                                                                (^clojure.lang.IFn iter__32112
                                                                                  (chunk-rest
                                                                                    s__32113)))
                                                                              (chunk-cons
                                                                                (chunk b__32115)
                                                                                nil)))
                                                                          (let 
                                                                            [sort (first s__32113)]
                                                                            (cons
                                                                              [tier sort]
                                                                              (^clojure.lang.IFn iter__32112
                                                                                (rest
                                                                                  s__32113)))))))))))
                                                            fs__6370__auto__
                                                            (seq
                                                              (^clojure.lang.IFn iterys__6369__auto__
                                                                [:eavt :aevt :avet :vaet]))]
                                                        (if fs__6370__auto__
                                                          (concat
                                                            fs__6370__auto__
                                                            (^clojure.lang.IFn iter__32110
                                                              (rest s__32111)))
                                                          (recur (rest s__32111))))))))))]
                   (^clojure.lang.IFn iter__6373__auto__ [:mid-index :main :history]))]
        (print "Validating dirs for ")
        (loop [seq_32133 (seq tses) chunk_32134 nil count_32135 0 i_32136 0]
          (if (< i_32136 count_32135)
            (let [vec__32137 (.nth ^clojure.lang.Indexed chunk_32134 (int i_32136))
                  tier (nth vec__32137 (int 0) nil)
                  sort (nth vec__32137 (int 1) nil)]
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
              (recur seq_32133 chunk_32134 count_32135 (inc i_32136)))
            (let [temp__5804__auto__ (seq seq_32133)]
              (when temp__5804__auto__
                (let [seq_32133 temp__5804__auto__]
                  (if (chunked-seq? seq_32133)
                    (let [c__6065__auto__ (chunk-first seq_32133)]
                      (recur
                        (chunk-rest seq_32133)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__32140 (first seq_32133)
                          tier (nth vec__32140 (int 0) nil)
                          sort (nth vec__32140 (int 1) nil)]
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
                      (recur (next seq_32133) nil 0 0))))))))
        (println))))
  (defn unsorted-datoms
    ([db sort order progress]
      (let [op (let [G__32149 order] (case G__32149 :allow-duplicates <= :strict <))
            cmp (datom-comparator sort)]
        (tools/unsorted-seq
          (fn fn__32150
            ([a b]
              (^clojure.lang.IFn progress)
              (^clojure.lang.IFn op
                (java.lang.Integer/valueOf (int (.compare ^java.util.Comparator cmp a b)))
                0)))
          (d/datoms db sort)))))
  (defn validate-index-sorts
    ([db order]
      (loop [seq_32153 (seq [:eavt :aevt :avet :vaet]) chunk_32154 nil count_32155 0 i_32156 0]
        (if (< i_32156 count_32155)
          (let [sort (.nth ^clojure.lang.Indexed chunk_32154 (int i_32156))]
            (println "Validating " sort)
            (let [temp__5804__auto__ (unsorted-datoms db sort order (progress-dot-fn 10000))]
              (when temp__5804__auto__
                (let [s temp__5804__auto__]
                  (throw (ex-info (str "Disorderly datom pairs " (first s)) {:pairs s}))))
              nil)
            (println)
            (recur seq_32153 chunk_32154 count_32155 (inc i_32156)))
          (let [temp__5804__auto__ (seq seq_32153)]
            (when temp__5804__auto__
              (let [seq_32153 temp__5804__auto__]
                (if (chunked-seq? seq_32153)
                  (let [c__6065__auto__ (chunk-first seq_32153)]
                    (recur
                      (chunk-rest seq_32153)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [sort (first seq_32153)]
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
                    (recur (next seq_32153) nil 0 0)))))))))
    ([db] (validate-index-sorts db :strict)))
  (defn unpaired-history-assertions
    ([db sort progress]
      (let [temp__5804__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5804__auto__
          (let [hist temp__5804__auto__]
            (tools/unsorted-seq
              (fn fn__32163
                ([d1 d2]
                  (^clojure.lang.IFn progress)
                  (or (not (.added ^datomic.Datom d2)) (index/retract-assert-pair? d1 d2))))
              (iter/iter-seq (.seek ^datomic.btset.IDataSet hist))))))))
  (defn unpaired-history-retractions
    ([db sort progress]
      (let [temp__5804__auto__ (some-> db (:history) (^clojure.lang.IFn sort))]
        (when temp__5804__auto__
          (let [hist temp__5804__auto__]
            (tools/unsorted-seq
              (fn fn__32169
                ([d1 d2]
                  (^clojure.lang.IFn progress)
                  (or (.added ^datomic.Datom d1) (index/retract-assert-pair? d1 d2))))
              (iter/iter-seq (.seek ^datomic.btset.IDataSet hist))))))))
  (defn validate-history-pairs
    ([db]
      (loop [seq_32174 (seq [:eavt :aevt :avet :vaet]) chunk_32175 nil count_32176 0 i_32177 0]
        (if (< i_32177 count_32176)
          (let [sort (.nth ^clojure.lang.Indexed chunk_32175 (int i_32177))]
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
            (recur seq_32174 chunk_32175 count_32176 (inc i_32177)))
          (let [temp__5804__auto__ (seq seq_32174)]
            (when temp__5804__auto__
              (let [seq_32174 temp__5804__auto__]
                (if (chunked-seq? seq_32174)
                  (let [c__6065__auto__ (chunk-first seq_32174)]
                    (recur
                      (chunk-rest seq_32174)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [sort (first seq_32174)]
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
                    (recur (next seq_32174) nil 0 0))))))))))
  (defn selfcheck-index-cli
    ([p__32183]
      (let [map__32184 p__32183
            map__32184 (if (seq? map__32184)
                         (if (next map__32184)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32184))
                           (if (seq map__32184) (first map__32184) {}))
                         map__32184)
            uri (get map__32184 :uri)
            uri (enhance-uri uri)
            conn (d/connect uri)
            db (d/db conn)]
        (loop [seq_32185 (seq [:eavt :aevt :avet :vaet]) chunk_32186 nil count_32187 0 i_32188 0]
          (if (< i_32188 count_32187)
            (let [index (.nth ^clojure.lang.Indexed chunk_32186 (int i_32188))]
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
              (recur seq_32185 chunk_32186 count_32187 (inc i_32188)))
            (let [temp__5804__auto__ (seq seq_32185)]
              (when temp__5804__auto__
                (let [seq_32185 temp__5804__auto__]
                  (if (chunked-seq? seq_32185)
                    (let [c__6065__auto__ (chunk-first seq_32185)]
                      (recur
                        (chunk-rest seq_32185)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [index (first seq_32185)]
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
                      (recur (next seq_32185) nil 0 0)))))))))))
  (defn mk-attr-pred ([s] (fn fn__32197 ([p1__32196#] (contains? s (:a p1__32196#))))))
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
      (let [G__32201 index]
        (case
          G__32201
          (:aevt :eavt)
          identity
          :avet
          (mk-attr-pred (attr-id-set db :has-avet))
          :raet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__32202 ([p1__32200#] (= :db.type/ref (:value-type p1__32200#))))))
          :vaet
          (mk-attr-pred
            (attr-id-set
              db
              (fn fn__32204 ([p1__32200#] (= :db.type/ref (:value-type p1__32200#))))))))))
  (defn crosscheck-log
    ([log db index progress]
      (do
        (let [m_32208 {:event :integrity/crosscheck-log, :index index, :db (:id db)}
              ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                                (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                  (.debug
                                    ^org.slf4j.Logger logger
                                    (logger/process (assoc m_32208 :phase :begin))))
                                nil)
              start__8584__auto__ (java.lang.System/nanoTime)
              result__8585__auto__ (try
                                     {:returned nil}
                                     (catch
                                       java.lang.Throwable
                                       t__8586__auto__
                                       {:threw t__8586__auto__}))
              elapsed_32209 (- (java.lang.System/nanoTime) start__8584__auto__)
              msec_32210 (logger/format-as-msec (long elapsed_32209))]
          (let [endmsg__8587__auto__ (merge
                                       (assoc m_32208 :msec msec_32210 :phase :end)
                                       (when (:threw result__8585__auto__)
                                         {:threw (class (:threw result__8585__auto__))}))
                logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
            (when (.isDebugEnabled ^org.slf4j.Logger logger)
              (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
            nil)
          (if (contains? result__8585__auto__ :returned)
            (:returned result__8585__auto__)
            (throw (:threw result__8585__auto__))))
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
                (fn fn__32213
                  ([ctr tx]
                    (loop [seq_32214 (seq
                                       (take-while
                                         (fn fn__32218
                                           ([p1__32207#] (<= (:tx p1__32207#) limit_tx)))
                                         (:data tx)))
                           chunk_32215 nil
                           count_32216 0
                           i_32217 0]
                      (if (< i_32217 count_32216)
                        (let [d (.nth ^clojure.lang.Indexed chunk_32215 (int i_32217))]
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
                          (recur seq_32214 chunk_32215 count_32216 (inc i_32217)))
                        (let [temp__5804__auto__ (seq seq_32214)]
                          (when temp__5804__auto__
                            (let [seq_32214 temp__5804__auto__]
                              (if (chunked-seq? seq_32214)
                                (let [c__6065__auto__ (chunk-first seq_32214)]
                                  (recur
                                    (chunk-rest seq_32214)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [d (first seq_32214)]
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
                                  (recur (next seq_32214) nil 0 0))))))))
                    (inc ctr)))
                0
                log_seq))))))
    ([log db index] (crosscheck-log log db index nil)))
  (defn crosscheck-log-representations
    ([uri progress]
      (let [uri (enhance-uri uri)
            map__32231 (tools/connection-resources uri)
            map__32231 (if (seq? map__32231)
                         (if (next map__32231)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32231))
                           (if (seq map__32231) (first map__32231) {}))
                         map__32231)
            cluster (get map__32231 :cluster)
            olookup (get map__32231 :olookup)
            conn (d/connect uri)
            db (d/db conn)
            log_1 (log/find-log cluster olookup)
            log_2 (log/create-log-val cluster olookup db)
            rep_1 (iter/iter-seq (log/seek-tx log_1 0))
            rep_2 (iter/iter-seq (log/seek-tx log_2 0))
            diffs (remove
                    (fn fn__32233
                      ([p__32232]
                        (let [vec__32234 p__32232
                              a (nth vec__32234 (int 0) nil)
                              b (nth vec__32234 (int 1) nil)]
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
  (defn crosscheck-log-cli
    ([p__32239]
      (let [map__32240 p__32239
            map__32240 (if (seq? map__32240)
                         (if (next map__32240)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32240))
                           (if (seq map__32240) (first map__32240) {}))
                         map__32240)
            uri (get map__32240 :uri)]
        (println)
        (let [uri (enhance-uri uri) cr (tools/connection-resources uri) conn (d/connect uri)]
          (loop [seq_32241 (seq [:eavt :aevt :avet :vaet]) chunk_32242 nil count_32243 0 i_32244 0]
            (if (< i_32244 count_32243)
              (let [index (.nth ^clojure.lang.Indexed chunk_32242 (int i_32244))]
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
                             (fn fn__32245
                               ([result]
                                 (let [G__32246 result]
                                   (case
                                     G__32246
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
                (recur seq_32241 chunk_32242 count_32243 (inc i_32244)))
              (let [temp__5804__auto__ (seq seq_32241)]
                (when temp__5804__auto__
                  (let [seq_32241 temp__5804__auto__]
                    (if (chunked-seq? seq_32241)
                      (let [c__6065__auto__ (chunk-first seq_32241)]
                        (recur
                          (chunk-rest seq_32241)
                          c__6065__auto__
                          (int (count c__6065__auto__))
                          (int 0)))
                      (let [index (first seq_32241)]
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
                                     (fn fn__32248
                                       ([result]
                                         (let [G__32249 result]
                                           (case
                                             G__32249
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
                        (recur (next seq_32241) nil 0 0))))))))))))
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
      (let [m_32270 {:event :integrity/crosscheck-indexes}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_32270 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (loop [seq_32274 (seq
                                                       ['crosscheck-eavt-aevt
                                                        'crosscheck-aevt-eavt
                                                        'crosscheck-avet-eavt
                                                        'crosscheck-avet-aevt
                                                        'crosscheck-raet-aevt
                                                        'crosscheck-raet-eavt
                                                        'crosscheck-aevt-avet
                                                        'crosscheck-eavt-avet])
                                           chunk_32275 nil
                                           count_32276 0
                                           i_32277 0]
                                      (if (< i_32277 count_32276)
                                        (let [c (.nth
                                                  ^clojure.lang.Indexed chunk_32275
                                                  (int i_32277))]
                                          (print "\n" c)
                                          (flush)
                                          (println
                                            ((ns-resolve 'datomic.integrity c)
                                              (get-db o)
                                              (get-db o)
                                              (fn fn__32279
                                                ([n]
                                                  (when (zero? (mod n 100000))
                                                    (print ".")
                                                    (flush))))))
                                          (flush)
                                          (recur seq_32274 chunk_32275 count_32276 (inc i_32277)))
                                        (let [temp__5804__auto__ (seq seq_32274)]
                                          (when temp__5804__auto__
                                            (let [seq_32274 temp__5804__auto__]
                                              (if (chunked-seq? seq_32274)
                                                (let [c__6065__auto__ (chunk-first seq_32274)]
                                                  (recur
                                                    (chunk-rest seq_32274)
                                                    c__6065__auto__
                                                    (int (count c__6065__auto__))
                                                    (int 0)))
                                                (let [c (first seq_32274)]
                                                  (print "\n" c)
                                                  (flush)
                                                  (println
                                                    ((ns-resolve 'datomic.integrity c)
                                                      (get-db o)
                                                      (get-db o)
                                                      (fn fn__32281
                                                        ([n]
                                                          (when (zero? (mod n 100000))
                                                            (print ".")
                                                            (flush))))))
                                                  (flush)
                                                  (recur (next seq_32274) nil 0 0))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_32271 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_32272 (logger/format-as-msec (long elapsed_32271))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_32270 :msec msec_32272 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
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
  (defn dir-seg-info-seq
    ([dir olookup]
      (map
        (fn fn__32295
          ([d] {:dir-t (:t d), :segid (:uuid d), :seg-t (:t (first (get olookup (:uuid d))))}))
        dir)))
  (defn validate-t-order*
    ([log progress]
      (if (seq (iter/iter-seq (log/seek-tx log 0)))
        (do
          (when-not (= 1000 (-> (log/seek-tx log 0) (iter/iter-seq) (seq) (first) (:t)))
            (let [form__30735__auto__ (clojure.core/list
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
                  error__30736__auto__ (ex-info
                                         "Assertion failed, see ex-data for details"
                                         {:bindings {'log log, 'progress progress},
                                          :form form__30735__auto__})]
              (if datomic.assert/*assert-handler*
                (datomic.assert/*assert-handler* error__30736__auto__)
                (throw ^java.lang.Throwable error__30736__auto__))))
          (reduce
            (fn fn__32299
              ([ctr p__32298]
                (let [vec__32300 p__32298
                      tx1 (nth vec__32300 (int 0) nil)
                      tx2 (nth vec__32300 (int 1) nil)]
                  (when progress (^clojure.lang.IFn progress {:n ctr, :t (:t tx1)}))
                  (let [tx (d/t->tx (long (:t tx1))) max_eidx (log/max-eidx (:data tx1))]
                    (loop [seq_32303 (seq (:data tx1)) chunk_32304 nil count_32305 0 i_32306 0]
                      (if (< i_32306 count_32305)
                        (let [d (.nth ^clojure.lang.Indexed chunk_32304 (int i_32306))]
                          (when-not (= (:tx d) tx)
                            (let [form__30735__auto__ (clojure.core/list
                                                        '=
                                                        (clojure.core/list :tx 'd)
                                                        'tx)
                                  error__30736__auto__ (ex-info
                                                         "not all datoms in transaction have same tx"
                                                         {:bindings
                                                          {'max-eidx max_eidx,
                                                           'progress progress,
                                                           'log log,
                                                           'seq_32303 seq_32303,
                                                           'ctr ctr,
                                                           'count_32305 (long count_32305),
                                                           'tx1 tx1,
                                                           'p__32298 p__32298,
                                                           (.withMeta
                                                             'chunk_32304
                                                             {:tag 'clojure.lang.IChunk})
                                                           chunk_32304,
                                                           'tx tx,
                                                           'tx2 tx2,
                                                           'i_32306 (long i_32306),
                                                           'vec__32300 vec__32300,
                                                           'd d},
                                                          :form form__30735__auto__})]
                              (if datomic.assert/*assert-handler*
                                (datomic.assert/*assert-handler* error__30736__auto__)
                                (throw ^java.lang.Throwable error__30736__auto__))))
                          (recur seq_32303 chunk_32304 count_32305 (inc i_32306)))
                        (let [temp__5804__auto__ (seq seq_32303)]
                          (when temp__5804__auto__
                            (let [seq_32303 temp__5804__auto__]
                              (if (chunked-seq? seq_32303)
                                (let [c__6065__auto__ (chunk-first seq_32303)]
                                  (recur
                                    (chunk-rest seq_32303)
                                    c__6065__auto__
                                    (int (count c__6065__auto__))
                                    (int 0)))
                                (let [d (first seq_32303)]
                                  (when-not (= (:tx d) tx)
                                    (let [form__30735__auto__ (clojure.core/list
                                                                '=
                                                                (clojure.core/list :tx 'd)
                                                                'tx)
                                          error__30736__auto__ (ex-info
                                                                 "not all datoms in transaction have same tx"
                                                                 {:bindings
                                                                  {'max-eidx max_eidx,
                                                                   'progress progress,
                                                                   'log log,
                                                                   'seq_32303 seq_32303,
                                                                   'ctr ctr,
                                                                   'count_32305 (long count_32305),
                                                                   'tx1 tx1,
                                                                   'p__32298 p__32298,
                                                                   (.withMeta
                                                                     'chunk_32304
                                                                     {:tag 'clojure.lang.IChunk})
                                                                   chunk_32304,
                                                                   'tx tx,
                                                                   'tx2 tx2,
                                                                   'i_32306 (long i_32306),
                                                                   'vec__32300 vec__32300,
                                                                   'd d,
                                                                   'temp__5804__auto__
                                                                   temp__5804__auto__},
                                                                  :form form__30735__auto__})]
                                      (if datomic.assert/*assert-handler*
                                        (datomic.assert/*assert-handler* error__30736__auto__)
                                        (throw ^java.lang.Throwable error__30736__auto__))))
                                  (recur (next seq_32303) nil 0 0))))))))
                    (when tx2
                      (when-not (< (:t tx1) (:t tx2))
                        (let [form__30735__auto__ (clojure.core/list
                                                    '<
                                                    (clojure.core/list :t 'tx1)
                                                    (clojure.core/list :t 'tx2))
                              error__30736__auto__ (ex-info
                                                     "txes are not ascending"
                                                     {:bindings
                                                      {'max-eidx max_eidx,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'tx1 tx1,
                                                       'p__32298 p__32298,
                                                       'tx tx,
                                                       'tx2 tx2,
                                                       'vec__32300 vec__32300},
                                                      :form form__30735__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__30736__auto__)
                            (throw ^java.lang.Throwable error__30736__auto__))))
                      (when-not (<= (inc max_eidx) (:t tx2))
                        (let [form__30735__auto__ (clojure.core/list
                                                    '<=
                                                    (clojure.core/list 'inc 'max-eidx)
                                                    (clojure.core/list :t 'tx2))
                              error__30736__auto__ (ex-info
                                                     "entity t too high for tx"
                                                     {:bindings
                                                      {'max-eidx max_eidx,
                                                       'progress progress,
                                                       'log log,
                                                       'ctr ctr,
                                                       'tx1 tx1,
                                                       'p__32298 p__32298,
                                                       'tx tx,
                                                       'tx2 tx2,
                                                       'vec__32300 vec__32300},
                                                      :form form__30735__auto__})]
                          (if datomic.assert/*assert-handler*
                            (datomic.assert/*assert-handler* error__30736__auto__)
                            (throw ^java.lang.Throwable error__30736__auto__))))))
                  (inc ctr))))
            0
            (partition-all 2 1 (iter/iter-seq (log/seek-tx log 0)))))
        0)))
  (defn validate-t-order
    ([uri log_fn progress]
      (let [m_32321 {:event :integrity/validate-t-order, :log-fn log_fn}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_32321 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [uri (enhance-uri uri)
                                          map__32325 (tools/connection-resources uri)
                                          map__32325 (if (seq? map__32325)
                                                       (if (next map__32325)
                                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                           (to-array map__32325))
                                                         (if (seq map__32325)
                                                           (first map__32325)
                                                           {}))
                                                       map__32325)
                                          cluster (get map__32325 :cluster)
                                          olookup (get map__32325 :olookup)
                                          temp__5802__auto__ (let 
                                                               [G__32326 log_fn]
                                                               (case
                                                                 G__32326
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
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_32322 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_32323 (logger/format-as-msec (long elapsed_32322))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_32321 :msec msec_32323 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn log-dir-entry-seq
    ([p__32334 t]
      (let [map__32335 p__32334
            map__32335 (if (seq? map__32335)
                         (if (next map__32335)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32335))
                           (if (seq map__32335) (first map__32335) {}))
                         map__32335)
            cluster (get map__32335 :cluster)
            olookup (get map__32335 :olookup)
            temp__5804__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
        (when temp__5804__auto__
          (let [tree_iter temp__5804__auto__] (mapcat identity (log/log-dir-seq tree_iter)))))))
  (defn log-seg-t-seq
    ([p__32338 t]
      (let [map__32339 p__32338
            map__32339 (if (seq? map__32339)
                         (if (next map__32339)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32339))
                           (if (seq map__32339) (first map__32339) {}))
                         map__32339)
            cluster (get map__32339 :cluster)
            olookup (get map__32339 :olookup)
            temp__5804__auto__ (log/seek-tx (log/find-log cluster olookup) t)]
        (when temp__5804__auto__
          (let [tree_iter temp__5804__auto__]
            (map :t (mapcat identity (log/log-seg-seq tree_iter))))))))
  (defn crosscheck-dir-segs
    ([p__32343 progress]
      (let [map__32344 p__32343
            map__32344 (if (seq? map__32344)
                         (if (next map__32344)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32344))
                           (if (seq map__32344) (first map__32344) {}))
                         map__32344)
            cluster (get map__32344 :cluster)
            olookup (get map__32344 :olookup)
            m_32345 {:event :integrity/crosscheck-dir-segs}
            ___8583__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_32345 :phase :begin))))
                              nil)
            start__8584__auto__ (java.lang.System/nanoTime)
            result__8585__auto__ (try
                                   {:returned
                                    (let [temp__5804__auto__ (log/find-log cluster olookup)]
                                      (when temp__5804__auto__
                                        (let [log temp__5804__auto__
                                              temp__5804__auto__ (log/seek-tx log 0)]
                                          (when temp__5804__auto__
                                            (let [tree_iter temp__5804__auto__]
                                              (loop [seq_32349 (seq (log/log-dir-seq tree_iter))
                                                     chunk_32350 nil
                                                     count_32351 0
                                                     i_32352 0]
                                                (if (< i_32352 count_32351)
                                                  (let [adir (.nth
                                                               ^clojure.lang.Indexed chunk_32350
                                                               (int i_32352))]
                                                    (when progress
                                                      (^clojure.lang.IFn progress adir))
                                                    (let [bad_dirs (seq
                                                                     (remove
                                                                       (fn 
                                                                         fn__32354
                                                                         ([p1__32342#]
                                                                           (=
                                                                             (:dir-t p1__32342#)
                                                                             (:seg-t p1__32342#))))
                                                                       (dir-seg-info-seq
                                                                         adir
                                                                         olookup)))]
                                                      (when-not (not bad_dirs)
                                                        (let [form__30735__auto__
                                                              (clojure.core/list 'not 'bad-dirs)
                                                              error__30736__auto__
                                                              (ex-info
                                                                "Assertion failed, see ex-data for details"
                                                                {:bindings
                                                                 {'seq_32349 seq_32349,
                                                                  '___8583__auto__ ___8583__auto__,
                                                                  'start__8584__auto__
                                                                  (long start__8584__auto__),
                                                                  'olookup olookup,
                                                                  'progress progress,
                                                                  'count_32351 (long count_32351),
                                                                  'log log,
                                                                  'adir adir,
                                                                  'map__32344 map__32344,
                                                                  'm_32345 m_32345,
                                                                  'tree-iter tree_iter,
                                                                  'i_32352 (long i_32352),
                                                                  'bad-dirs bad_dirs,
                                                                  (.withMeta
                                                                    'chunk_32350
                                                                    {:tag 'clojure.lang.IChunk})
                                                                  chunk_32350,
                                                                  'p__32343 p__32343,
                                                                  'temp__5804__auto__
                                                                  temp__5804__auto__,
                                                                  'cluster cluster},
                                                                 :form form__30735__auto__})]
                                                          (if datomic.assert/*assert-handler*
                                                            (datomic.assert/*assert-handler*
                                                              error__30736__auto__)
                                                            (throw
                                                              ^java.lang.Throwable error__30736__auto__)))))
                                                    (recur
                                                      seq_32349
                                                      chunk_32350
                                                      count_32351
                                                      (inc i_32352)))
                                                  (let [temp__5804__auto__ (seq seq_32349)]
                                                    (when temp__5804__auto__
                                                      (let [seq_32349 temp__5804__auto__]
                                                        (if (chunked-seq? seq_32349)
                                                          (let [c__6065__auto__
                                                                (chunk-first seq_32349)]
                                                            (recur
                                                              (chunk-rest seq_32349)
                                                              c__6065__auto__
                                                              (int (count c__6065__auto__))
                                                              (int 0)))
                                                          (let [adir (first seq_32349)]
                                                            (when
                                                              progress
                                                              (^clojure.lang.IFn progress adir))
                                                            (let 
                                                              [bad_dirs
                                                               (seq
                                                                 (remove
                                                                   (fn 
                                                                     fn__32356
                                                                     ([p1__32342#]
                                                                       (=
                                                                         (:dir-t p1__32342#)
                                                                         (:seg-t p1__32342#))))
                                                                   (dir-seg-info-seq
                                                                     adir
                                                                     olookup)))]
                                                              (when-not
                                                                (not bad_dirs)
                                                                (let 
                                                                  [form__30735__auto__
                                                                   (clojure.core/list
                                                                     'not
                                                                     'bad-dirs)
                                                                   error__30736__auto__
                                                                   (ex-info
                                                                     "Assertion failed, see ex-data for details"
                                                                     {:bindings
                                                                      {'seq_32349 seq_32349,
                                                                       '___8583__auto__
                                                                       ___8583__auto__,
                                                                       'start__8584__auto__
                                                                       (long start__8584__auto__),
                                                                       'olookup olookup,
                                                                       'progress progress,
                                                                       'count_32351
                                                                       (long count_32351),
                                                                       'log log,
                                                                       'adir adir,
                                                                       'map__32344 map__32344,
                                                                       'm_32345 m_32345,
                                                                       'tree-iter tree_iter,
                                                                       'i_32352 (long i_32352),
                                                                       'bad-dirs bad_dirs,
                                                                       (.withMeta
                                                                         'chunk_32350
                                                                         {:tag
                                                                          'clojure.lang.IChunk})
                                                                       chunk_32350,
                                                                       'p__32343 p__32343,
                                                                       'temp__5804__auto__
                                                                       temp__5804__auto__,
                                                                       'cluster cluster},
                                                                      :form form__30735__auto__})]
                                                                  (if
                                                                    datomic.assert/*assert-handler*
                                                                    (datomic.assert/*assert-handler*
                                                                      error__30736__auto__)
                                                                    (throw
                                                                      ^java.lang.Throwable error__30736__auto__)))))
                                                            (recur
                                                              (next seq_32349)
                                                              nil
                                                              0
                                                              0)))))))))))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8586__auto__
                                     {:threw t__8586__auto__}))
            elapsed_32346 (- (java.lang.System/nanoTime) start__8584__auto__)
            msec_32347 (logger/format-as-msec (long elapsed_32346))]
        (let [endmsg__8587__auto__ (merge
                                     (assoc m_32345 :msec msec_32347 :phase :end)
                                     (when (:threw result__8585__auto__)
                                       {:threw (class (:threw result__8585__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.integrity")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8587__auto__)))
          nil)
        (if (contains? result__8585__auto__ :returned)
          (:returned result__8585__auto__)
          (do (throw (:threw result__8585__auto__)) nil)))))
  (defn merge-seqs
    ([cmp s1 s2 s3 s4] (merge-seqs cmp s1 (merge-seqs cmp s2 s3 s4)))
    ([cmp s1 s2 s3] (merge-seqs cmp s1 (merge-seqs cmp s2 s3)))
    ([cmp s1 s2]
      (let [s1 (seq s1) s2 (seq s2)]
        (if (and s1 s2)
          (let [vec__32373 s1
                seq__32374 (seq vec__32373)
                first__32375 (first seq__32374)
                seq__32374 (next seq__32374)
                o1 first__32375
                m1 seq__32374
                vec__32376 s2
                seq__32377 (seq vec__32376)
                first__32378 (first seq__32377)
                seq__32377 (next seq__32377)
                o2 first__32378
                m2 seq__32377]
            (if (< (.compare ^java.util.Comparator cmp o1 o2) 0)
              (lazy-seq (cons o1 (merge-seqs cmp m1 s2)))
              (lazy-seq (cons o2 (merge-seqs cmp s1 m2)))))
          (or s1 s2)))))
  (defn aevt-dquark-seq
    ([db d]
      (map
        (fn fn__32387 ([p1__32386#] (dissoc p1__32386# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__32392
              ([p__32391]
                (let [vec__32393 p__32391
                      index (nth vec__32393 (int 0) nil)
                      iter (nth vec__32393 (int 1) nil)]
                  (map
                    (fn fn__32397
                      ([p__32396]
                        (let [map__32398 p__32396
                              map__32398 (if (seq? map__32398)
                                           (if (next map__32398)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__32398))
                                             (if (seq map__32398) (first map__32398) {}))
                                           map__32398)
                              datom map__32398
                              e (get map__32398 :e)
                              a (get map__32398 :a)
                              v (get map__32398 :v)
                              tx (get map__32398 :tx)
                              added (get map__32398 :added)]
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
  (defn eavt-dquark-seq
    ([db d]
      (map
        (fn fn__32407 ([p1__32406#] (dissoc p1__32406# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__32412
              ([p__32411]
                (let [vec__32413 p__32411
                      index (nth vec__32413 (int 0) nil)
                      iter (nth vec__32413 (int 1) nil)]
                  (map
                    (fn fn__32417
                      ([p__32416]
                        (let [map__32418 p__32416
                              map__32418 (if (seq? map__32418)
                                           (if (next map__32418)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__32418))
                                             (if (seq map__32418) (first map__32418) {}))
                                           map__32418)
                              datom map__32418
                              e (get map__32418 :e)
                              a (get map__32418 :a)
                              v (get map__32418 :v)
                              tx (get map__32418 :tx)
                              added (get map__32418 :added)]
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
  (defn avet-dquark-seq
    ([db d]
      (map
        (fn fn__32427 ([p1__32426#] (dissoc p1__32426# :datom)))
        (apply
          merge-seqs
          (reify
            java.util.Comparator
            (^int compare [this x y] (.compare db/aevt-cmp (:datom x) (:datom y))))
          (map
            (fn fn__32432
              ([p__32431]
                (let [vec__32433 p__32431
                      index (nth vec__32433 (int 0) nil)
                      iter (nth vec__32433 (int 1) nil)]
                  (map
                    (fn fn__32437
                      ([p__32436]
                        (let [map__32438 p__32436
                              map__32438 (if (seq? map__32438)
                                           (if (next map__32438)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__32438))
                                             (if (seq map__32438) (first map__32438) {}))
                                           map__32438)
                              datom map__32438
                              e (get map__32438 :e)
                              a (get map__32438 :a)
                              v (get map__32438 :v)
                              tx (get map__32438 :tx)
                              added (get map__32438 :added)]
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
  (defn validate-log-cli
    ([uri]
      (let [uri (enhance-uri uri)
            cr (tools/connection-resources uri)
            count (atom 0)
            progress (fn progress
                       ([p1__32446#]
                         (fn fn__32448
                           ([_ & more]
                             (when (zero? (mod (swap! count inc) p1__32446#))
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
    ([db] (seq (map (fn fn__32453 ([d] (d/entity db (:e d)))) (d/datoms db :aevt :db/excise)))))
  (defn e-ts
    ([db e]
      (sort
        (distinct
          (map
            (fn fn__32457 ([p1__32456#] (long (d/tx->t (:tx p1__32456#)))))
            (d/datoms db :eavt e))))))
  (defn validate-excision
    ([spec]
      (let [db (d/entity-db spec)
            id (fn id ([p1__32460#] (or (:db/id p1__32460#) (db/resolve-id db p1__32460#))))
            target (:db/excise spec)
            before_t (let [temp__5802__auto__ (excise/get-before-t db spec)]
                       (if temp__5802__auto__
                         (let [bt temp__5802__auto__] (dec bt))
                         (d/basis-t db)))
            spec_t (first (e-ts db (:db/id spec)))
            t (min (min before_t spec_t) (:indexBasisT db))
            valdb (d/as-of db t)
            type (if (db/attribute db (^clojure.lang.IFn id target)) :a :e)]
        (let [G__32464 type]
          (case
            G__32464
            :a
            (let [datoms (seq (d/datoms valdb :aevt (^clojure.lang.IFn id target)))]
              (when-not (nil? datoms)
                (throw
                  (ex-info
                    "Found datoms that should have been excised"
                    {:spec spec, :datoms datoms, :t t}))
                (clojure.lang.Util/hash G__32464)))
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
  (defn validate-indexed-excisions
    ([db]
      (dorun
        (map
          (fn fn__32468 ([spec] (validate-excision spec) (print ".") (flush) spec))
          (take-while
            (fn fn__32470
              ([p1__32467#] (<= (first (e-ts db (:db/id p1__32467#))) (:indexBasisT db))))
            (excisions db))))))
  (defn clusterfs-path-reachability
    ([cfs olookup progress]
      (mapcat
        (fn fn__32474
          ([p__32473]
            (let [vec__32475 p__32473
                  filename (nth vec__32475 (int 0) nil)
                  map__32478 (nth vec__32475 (int 1) nil)
                  map__32478 (if (seq? map__32478)
                               (if (next map__32478)
                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                   (to-array map__32478))
                                 (if (seq map__32478) (first map__32478) {}))
                               map__32478)
                  base (get map__32478 :base)
                  length (get map__32478 :length)]
              (map
                (fn fn__32479
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
          (fn fn__32484
            ([p__32483]
              (let [vec__32485 p__32483
                    attrid (nth vec__32485 (int 0) nil)
                    uuid (nth vec__32485 (int 1) nil)
                    temp__5802__auto__ (get olookup (str uuid))]
                (if temp__5802__auto__
                  (let [cfs temp__5802__auto__]
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
            temp__5804__auto__ (seq
                                 (remove
                                   (fn fn__32494 ([p1__32491#] (nth p1__32491# (int 2))))
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
  (defn tx-range-ts ([conn] (map :t (d/tx-range (d/log conn) 1000 (d/next-t (d/db conn))))))
  (defn tx-instant-ts
    ([db]
      (drop-while
        (fn fn__32500 ([p1__32499#] (< p1__32499# 1000)))
        (map (comp d/tx->t :tx) (d/datoms db :avet :db/txInstant)))))
  (defn seq-diffs
    ([colla collb progress n]
      (let [G__32509 (seq colla)
            vec__32511 G__32509
            seq__32512 (seq vec__32511)
            first__32513 (first seq__32512)
            seq__32512 (next seq__32512)
            a first__32513
            morea seq__32512
            G__32510 (seq collb)
            vec__32514 G__32510
            seq__32515 (seq vec__32514)
            first__32516 (first seq__32515)
            seq__32515 (next seq__32515)
            b first__32516
            moreb seq__32515
            n n]
        (loop [G__32509 G__32509 G__32510 G__32510 n n]
          (let [vec__32517 G__32509
                seq__32518 (seq vec__32517)
                first__32519 (first seq__32518)
                seq__32518 (next seq__32518)
                a first__32519
                morea seq__32518
                vec__32520 G__32510
                seq__32521 (seq vec__32520)
                first__32522 (first seq__32521)
                seq__32521 (next seq__32521)
                b first__32522
                moreb seq__32521
                n n]
            (^clojure.lang.IFn progress n)
            (when (or a b)
              (if (= a b)
                (recur morea moreb (inc n))
                (lazy-seq (cons {:a a, :b b, :n n} (seq-diffs morea moreb progress (inc n))))))))))
    ([colla collb progress] (seq-diffs colla collb progress 0)))
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
                         (fn fn__32527
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
            temp__5804__auto__ (seq-diffs (tx-range-ts conn) (tx-instant-ts (d/db conn)) progress)]
        (when temp__5804__auto__
          (let [diffs temp__5804__auto__]
            (throw
              (ex-info
                "tx-range and :db/txInstant did not agree on ts."
                {:desc ":a values are from tx-range, :b values from :db/txInstant, :n offset",
                 :diffs diffs}))))
        nil)))
  (defn validate-nohistory
    ([db]
      (let [hdb (cauterize (d/history db) :memidx :indexing :mid-index :index)
            temp__5802__auto__ (seq (attr-datoms db (nohistory-attrs db)))]
        (when temp__5802__auto__
          (let [datoms temp__5802__auto__]
            (throw (ex-info "Found :db/noHistory datoms in the history index" {:datoms datoms}))))
        nil)))
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
          (fn fn__32547 ([p1__32546#] (stat-counts db (.id ^datomic.db.Attribute p1__32546#))))
          (remove
            (fn fn__32549
              ([p1__32545#] (contains? nohists (.id ^datomic.db.Attribute p1__32545#))))
            (filter
              (fn fn__32551 ([p1__32544#] (.hasAVET ^datomic.db.Attribute p1__32544#)))
              (db/attribute-seq db)))))))
  (defn aevt-avet-stats-consistent?
    ([p__32554]
      (let [map__32555 p__32554
            map__32555 (if (seq? map__32555)
                         (if (next map__32555)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32555))
                           (if (seq map__32555) (first map__32555) {}))
                         map__32555)
            aevt_total (get map__32555 :aevt-total)
            avet_total (get map__32555 :avet-total)]
        (= aevt_total avet_total))))
  (defn report-aevt-avet-stats
    ([db]
      (let [temp__5804__auto__ (seq (remove aevt-avet-stats-consistent? (aevt-avet-stats db)))]
        (when temp__5804__auto__
          (let [mismatch temp__5804__auto__] (prn {:stats-mismatch mismatch}))))))
  (defn validate-index-totals
    ([db]
      (let [eavt (stats/datom-counts stats/eavt db)
            aevt (stats/datom-counts stats/aevt db)
            summary {:eavt eavt, :aevt aevt}
            total_datoms (fn total_datoms
                           ([p1__32559#]
                             (apply
                               +
                               (vals
                                 (select-keys
                                   p1__32559#
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
          (let [form__30735__auto__ (clojure.core/list
                                      'nil?
                                      (clojure.core/list 'ic/unique-collisions 'db 'identity))
                error__30736__auto__ (ex-info
                                       "Assertion failed, see ex-data for details"
                                       {:bindings {'uri uri, 'cr cr, 'conn conn, 'db db},
                                        :form form__30735__auto__})]
            (if datomic.assert/*assert-handler*
              (datomic.assert/*assert-handler* error__30736__auto__)
              (throw ^java.lang.Throwable error__30736__auto__))))
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
            map__32568 (tools/connection-resources uri)
            map__32568 (if (seq? map__32568)
                         (if (next map__32568)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32568))
                           (if (seq map__32568) (first map__32568) {}))
                         map__32568)
            cluster (get map__32568 :cluster)
            olookup (get map__32568 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)]
        (when root_id
          (let [root (get olookup root_id)
                branch? (fn branch_QMARK_
                          ([p__32569]
                            (let [map__32571 p__32569
                                  map__32571 (if (seq? map__32571)
                                               (if (next map__32571)
                                                 (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                   (to-array map__32571))
                                                 (if (seq map__32571) (first map__32571) {}))
                                               map__32571)
                                  seg (get map__32571 :seg)]
                              (or
                                (instance? datomic.fulltext.Root seg)
                                (instance? datomic.clusterfs.ClusterFS seg)))))
                children (fn children
                           ([p__32574]
                             (let [map__32576 p__32574
                                   map__32576 (if (seq? map__32576)
                                                (if (next map__32576)
                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                    (to-array map__32576))
                                                  (if (seq map__32576) (first map__32576) {}))
                                                map__32576)
                                   seg (get map__32576 :seg)]
                               (cond
                                 (instance? datomic.fulltext.Root seg) (map
                                                                         (fn 
                                                                           fn__32577
                                                                           ([p1__32567#]
                                                                             (let 
                                                                               [uuid
                                                                                (str p1__32567#)]
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
                                                                                   fn__32579
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
            map__32583 (tools/connection-resources uri)
            map__32583 (if (seq? map__32583)
                         (if (next map__32583)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32583))
                           (if (seq map__32583) (first map__32583) {}))
                         map__32583)
            cluster (get map__32583 :cluster)
            olookup (get map__32583 :olookup)
            root_id (log/root-id cluster)
            branch? (fn branch_QMARK_
                      ([p__32584]
                        (let [map__32586 p__32584
                              map__32586 (if (seq? map__32586)
                                           (if (next map__32586)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__32586))
                                             (if (seq map__32586) (first map__32586) {}))
                                           map__32586)
                              seg (get map__32586 :seg)]
                          (instance? datomic.log.LogDir (first seg)))))
            children (fn children
                       ([p__32588]
                         (let [map__32590 p__32588
                               map__32590 (if (seq? map__32590)
                                            (if (next map__32590)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__32590))
                                              (if (seq map__32590) (first map__32590) {}))
                                            map__32590)
                               uuid (get map__32590 :uuid)
                               seg (get map__32590 :seg)]
                           (map
                             (fn fn__32591
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
            map__32595 (tools/connection-resources uri)
            map__32595 (if (seq? map__32595)
                         (if (next map__32595)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32595))
                           (if (seq map__32595) (first map__32595) {}))
                         map__32595)
            cluster (get map__32595 :cluster)
            olookup (get map__32595 :olookup)
            index_root_id (:key
                            (deref (cluster/get-ref cluster (index/index-ref-key-name cluster))))
            index_root (get olookup index_root_id)
            root_id (get index_root k)
            branch? (fn branch_QMARK_
                      ([p__32596]
                        (let [map__32598 p__32596
                              map__32598 (if (seq? map__32598)
                                           (if (next map__32598)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__32598))
                                             (if (seq map__32598) (first map__32598) {}))
                                           map__32598)
                              seg (get map__32598 :seg)]
                          (or
                            (instance? datomic.index.RootNode seg)
                            (instance? datomic.index.DirNode seg)))))
            children (fn children
                       ([p__32601]
                         (let [map__32603 p__32601
                               map__32603 (if (seq? map__32603)
                                            (if (next map__32603)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__32603))
                                              (if (seq map__32603) (first map__32603) {}))
                                            map__32603)
                               seg (get map__32603 :seg)]
                           (cond
                             (instance? datomic.index.RootNode seg) (map
                                                                      (fn 
                                                                        fn__32604
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
                                                                         fn__32606
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
            map__32610 (tools/connection-resources uri)
            map__32610 (if (seq? map__32610)
                         (if (next map__32610)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32610))
                           (if (seq map__32610) (first map__32610) {}))
                         map__32610)
            cluster (get map__32610 :cluster)
            olookup (get map__32610 :olookup)
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
                            (fn fn__32612
                              ([bb]
                                (when bb (fressian/defressian bb :handlers log/read-handlers))))))
                        {:id id}))))]
        (cons
          (if pod_meta (assoc pod_meta :type :pod-key) {:id pod_key})
          (when pod_meta
            (take-while
              identity
              (iterate
                (fn fn__32617
                  ([p__32616]
                    (let [map__32618 p__32616
                          map__32618 (if (seq? map__32618)
                                       (if (next map__32618)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__32618))
                                         (if (seq map__32618) (first map__32618) {}))
                                       map__32618)
                          prev (get map__32618 :prev)]
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
                (clojure.core/list 't__32621__auto__)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list '.printStackTrace)
                      (clojure.core/list 't__32621__auto__)))))))))))
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
    ([p__32624]
      (let [map__32625 p__32624
            map__32625 (if (seq? map__32625)
                         (if (next map__32625)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__32625))
                           (if (seq map__32625) (first map__32625) {}))
                         map__32625)
            uri (get map__32625 :uri)
            validate (get map__32625 :validate)]
        (let [uri (enhance-uri uri)]
          (println "\nDiagnostics:")
          (pp/pprint
            (try
              (diagnostics uri)
              (catch
                java.lang.Throwable
                t__32621__auto__
                (do (.printStackTrace ^java.lang.Throwable t__32621__auto__) nil)))))
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
                    (fn fn__32629 ([p1__32623#] (= 1 (long (count p1__32623#)))))
                    (pod-storage-seq uri))
                  (catch
                    java.lang.Throwable
                    t__32621__auto__
                    (do (.printStackTrace ^java.lang.Throwable t__32621__auto__) nil)))))
            (println "\nMissing log segments: ")
            (prn
              (seq
                (try
                  (remove :seg (log-storage-seq uri))
                  (catch
                    java.lang.Throwable
                    t__32621__auto__
                    (do (.printStackTrace ^java.lang.Throwable t__32621__auto__) nil)))))
            (loop [seq_32634 (seq index-sort-root-keys) chunk_32635 nil count_32636 0 i_32637 0]
              (if (< i_32637 count_32636)
                (let [k (.nth ^clojure.lang.Indexed chunk_32635 (int i_32637))]
                  (println "\nMissing segments in " k)
                  (prn
                    (seq
                      (try
                        (remove :seg (index-storage-seq uri k))
                        (catch
                          java.lang.Throwable
                          t__32621__auto__
                          (do (.printStackTrace ^java.lang.Throwable t__32621__auto__) nil)))))
                  (recur seq_32634 chunk_32635 count_32636 (inc i_32637)))
                (let [temp__5804__auto__ (seq seq_32634)]
                  (when temp__5804__auto__
                    (let [seq_32634 temp__5804__auto__]
                      (if (chunked-seq? seq_32634)
                        (let [c__6065__auto__ (chunk-first seq_32634)]
                          (recur
                            (chunk-rest seq_32634)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [k (first seq_32634)]
                          (println "\nMissing segments in " k)
                          (prn
                            (seq
                              (try
                                (remove :seg (index-storage-seq uri k))
                                (catch
                                  java.lang.Throwable
                                  t__32621__auto__
                                  (do
                                    (.printStackTrace ^java.lang.Throwable t__32621__auto__)
                                    nil)))))
                          (recur (next seq_32634) nil 0 0))))))))
            (loop [seq_32642 (seq [:fulltext :fulltext-hist])
                   chunk_32643 nil
                   count_32644 0
                   i_32645 0]
              (if (< i_32645 count_32644)
                (let [k (.nth ^clojure.lang.Indexed chunk_32643 (int i_32645))]
                  (println "\nMissing segments in " k)
                  (prn
                    (seq
                      (try
                        (remove :seg (fulltext-storage-seq uri k))
                        (catch
                          java.lang.Throwable
                          t__32621__auto__
                          (do (.printStackTrace ^java.lang.Throwable t__32621__auto__) nil)))))
                  (recur seq_32642 chunk_32643 count_32644 (inc i_32645)))
                (let [temp__5804__auto__ (seq seq_32642)]
                  (when temp__5804__auto__
                    (let [seq_32642 temp__5804__auto__]
                      (if (chunked-seq? seq_32642)
                        (let [c__6065__auto__ (chunk-first seq_32642)]
                          (recur
                            (chunk-rest seq_32642)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [k (first seq_32642)]
                          (println "\nMissing segments in " k)
                          (prn
                            (seq
                              (try
                                (remove :seg (fulltext-storage-seq uri k))
                                (catch
                                  java.lang.Throwable
                                  t__32621__auto__
                                  (do
                                    (.printStackTrace ^java.lang.Throwable t__32621__auto__)
                                    nil)))))
                          (recur (next seq_32642) nil 0 0))))))))
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
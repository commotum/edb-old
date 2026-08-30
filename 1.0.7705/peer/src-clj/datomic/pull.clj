(do
  (clojure.core/in-ns 'datomic.pull)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.db :as 'db]
        ['datomic.cache :as 'cache]
        ['datomic.common :as 'common]
        ['datomic.extension-resolver :as 'ext-resolver]
        ['datomic.iter :as 'iter]
        ['datomic.error :as 'error]
        ['datomic.measure.io-stats :as 'io-stats]
        ['clojure.edn :as 'edn])
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'java.util.Iterator)
      (clojure.core/import 'java.util.NoSuchElementException)))
  (when-not (.equals 'datomic.pull 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.pull))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.db :as 'db]
          ['datomic.cache :as 'cache]
          ['datomic.common :as 'common]
          ['datomic.extension-resolver :as 'ext-resolver]
          ['datomic.iter :as 'iter]
          ['datomic.error :as 'error]
          ['datomic.measure.io-stats :as 'io-stats]
          ['clojure.edn :as 'edn])
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'java.util.Iterator)
        (clojure.core/import 'java.util.NoSuchElementException))))
  (set! *warn-on-reflection* true)
  (def limit-iterable
   (fn limit_iterable
     ([limit iterable]
       (if (not limit)
         iterable
         (reify
           java.lang.Iterable
           (^java.util.Iterator iterator
             [this]
             (let [iter (.iterator ^java.lang.Iterable iterable) i (long-array (int 1) 0)]
               (reify
                 java.util.Iterator
                 (next
                   [this]
                   (let [_i (aget ^longs i (int 0))]
                     (when-not (< _i limit) (throw (java.util.NoSuchElementException.)))
                     (aset ^longs i (int 0) (long (inc _i)))
                     (.next ^java.util.Iterator iter)))
                 (^boolean hasNext
                   [this]
                   (and
                     (< (aget ^longs i (int 0)) limit)
                     (.hasNext ^java.util.Iterator iter)))))))))))
  (reset-meta!
    #'limit-iterable
    (assoc
      {:arglists (clojure.core/list ['limit (.withMeta 'iterable {:tag 'Iterable})]),
       :column (int 1)}
      :name
      'limit-iterable
      :ns
      *ns*))
  (defn nilify-empty ([x] (when (seq x) x)))
  (reset-meta!
    #'nilify-empty
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'nilify-empty :ns *ns*))
  (def ra->e
   (fn ra__GT_e
     ([db r attr xf limit valfn]
       (^clojure.lang.IFn valfn
         (when attr
           (let [rid (db/resolve-id db r)
                 attrid (.id ^datomic.db.Attribute attr)
                 mk_iter (fn mk_iter
                           ([]
                             (iter/map
                               (fn fn__15120 ([d] (.e ^datomic.Datom d)))
                               (db/windowed
                                 db
                                 (fn fn__15122
                                   ([p1__15118#]
                                     (and
                                       (= rid (.v ^datomic.Datom p1__15118#))
                                       (= attrid (.a ^datomic.Datom p1__15118#)))))
                                 (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid))))))
                 temp__5804__auto__ (^clojure.lang.IFn mk_iter)]
             (when temp__5804__auto__
               (let [iter temp__5804__auto__]
                 (if (.-isComponent ^datomic.db.Attribute attr)
                   (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter))
                   (nilify-empty
                     (persistent!
                       (reduce
                         (fn fn__15126
                           ([coll item]
                             (let [temp__5802__auto__ (^clojure.lang.IFn xf item)]
                               (if temp__5802__auto__
                                 (let [xitem temp__5802__auto__] (conj! coll xitem))
                                 coll))))
                         (transient [])
                         (limit-iterable limit (iter/iterable mk_iter))))))))))))))
  (reset-meta!
    #'ra->e
    (assoc
      {:arglists
       (clojure.core/list ['db 'r (.withMeta 'attr {:tag 'Attribute}) 'xf 'limit 'valfn]),
       :column (int 1)}
      :name
      'ra->e
      :ns
      *ns*))
  (def ea->v
   (fn ea__GT_v
     ([db e attr xf limit valfn use_aevt?]
       (^clojure.lang.IFn valfn
         (when attr
           (let [eid (db/resolve-id db e)
                 attrid (.id ^datomic.db.Attribute attr)
                 d (db/datum db :e eid :a attrid)
                 mk_iter (fn mk_iter
                           ([]
                             (iter/map
                               (fn fn__15133 ([d] (.v ^datomic.Datom d)))
                               (db/windowed
                                 db
                                 (fn fn__15135
                                   ([p1__15131#]
                                     (and
                                       (= eid (.e ^datomic.Datom p1__15131#))
                                       (= attrid (.a ^datomic.Datom p1__15131#)))))
                                 (if use_aevt?
                                   (.seekAEVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d)
                                   (.seekEAVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d))))))
                 temp__5804__auto__ (^clojure.lang.IFn mk_iter)]
             (when temp__5804__auto__
               (let [iter temp__5804__auto__]
                 (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                   (nilify-empty
                     (persistent!
                       (reduce
                         (fn fn__15139
                           ([coll item]
                             (let [xitem (^clojure.lang.IFn xf item)]
                               (if (nil? xitem) coll (conj! coll xitem)))))
                         (transient [])
                         (limit-iterable limit (iter/iterable mk_iter)))))
                   (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter)))))))))
     ([db e attr xf limit valfn] (ea->v db e attr xf limit valfn false))))
  (reset-meta!
    #'ea->v
    (assoc
      {:arglists
       (clojure.core/list
         ['db 'e 'attr 'xf 'limit 'valfn]
         ['db 'e (.withMeta 'attr {:tag 'Attribute}) 'xf 'limit 'valfn 'use-aevt?]),
       :column (int 1)}
      :name
      'ea->v
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.pull" "default-limit") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.pull" "default-limit") (atom 1000))
  (def attr-spec->fn
   (fn attr_spec__GT_fn
     ([attr_spec]
       (if (not (instance? java.util.List attr_spec))
         [(deref default-limit) identity]
         (let [vec__15143 attr_spec
               sym (nth vec__15143 (int 0) nil)
               kw (nth vec__15143 (int 1) nil)
               arg (nth vec__15143 (int 2) nil)
               G__15146 (db/normalize-kw sym)]
           (case
             G__15146
             :default
             [(deref default-limit) (fn fn__15147 ([v] (or v arg)))]
             :limit
             (if (or (integer? arg) (nil? arg))
               [arg identity]
               (error/arg
                 :db.error/invalid-limit
                 (str "'" arg "' is not a valid limit in '" attr_spec "'")))
             (error/arg
               :db.error/invalid-attr-spec
               (str
                 "Cannot interpret as an attribute spec: "
                 attr_spec
                 " of class: "
                 (class attr_spec)))))))))
  (reset-meta!
    #'attr-spec->fn
    (assoc
      {:arglists (clojure.core/list ['attr-spec]), :column (int 1)}
      :name
      'attr-spec->fn
      :ns
      *ns*))
  (def attr-spec->attr
   (fn attr_spec__GT_attr
     ([attr_spec]
       (if (instance? java.util.List attr_spec)
         (attr-spec->attr (second attr_spec))
         (if (not (keyword? attr_spec))
           (let [s (str attr_spec) s (if (or (= s "*") (= s ":*")) "*" s)]
             (if (= s "*")
               (if (string? attr_spec) s (keyword attr_spec))
               (if (= (char (.charAt ^java.lang.String s (int 0))) (char (.charValue \:)))
                 s
                 (error/arg
                   :db.error/invalid-attr-spec
                   (str
                     "Attribute identifier "
                     s
                     " of class: "
                     (class s)
                     " does not start with a colon")))))
           attr_spec)))))
  (reset-meta!
    #'attr-spec->attr
    (assoc
      {:arglists (clojure.core/list ['attr-spec]), :column (int 1)}
      :name
      'attr-spec->attr
      :ns
      *ns*))
  (defn attr-with-opts?
    ([expr]
      (and
        (instance? java.util.List expr)
        (odd? (java.lang.Integer/valueOf (int (count expr))))
        (keyword? (first expr)))))
  (reset-meta!
    #'attr-with-opts?
    (assoc
      {:arglists (clojure.core/list ['expr]), :column (int 1)}
      :name
      'attr-with-opts?
      :ns
      *ns*))
  (def limit-default-from-map
   (fn limit_default_from_map
     ([p__15157]
       (let [map__15158 p__15157
             map__15158 (if (seq? map__15158)
                          (if (next map__15158)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15158))
                            (if (seq map__15158) (first map__15158) {}))
                          map__15158)
             args map__15158
             limit (get map__15158 :limit)]
         (if (contains? args :limit) limit (deref default-limit))))))
  (reset-meta!
    #'limit-default-from-map
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['limit], :as 'args}]), :column (int 1)}
      :name
      'limit-default-from-map
      :ns
      *ns*))
  (defn try-xform
    ([xform]
      (let [f (ext-resolver/resolve-xform! xform)]
        (fn fn__15160
          ([v]
            (try
              (^clojure.lang.IFn f v)
              (catch
                java.lang.Throwable
                t
                (do
                  (when (error/cancelled? (ex-data t)) (throw ^java.lang.Throwable t))
                  (throw (error/eval-exception {:context :xform, :expr xform, :arguments [v]} t))
                  nil))))))))
  (reset-meta!
    #'try-xform
    (assoc
      {:private true, :arglists (clojure.core/list ['xform]), :column (int 1)}
      :name
      'try-xform
      :ns
      *ns*))
  (def attr-with-opts->valfn
   (fn attr_with_opts__GT_valfn
     ([p__15163]
       (let [vec__15164 p__15163
             seq__15165 (seq vec__15164)
             args seq__15165
             map__15167 (apply hash-map args)
             map__15167 (if (seq? map__15167)
                          (if (next map__15167)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15167))
                            (if (seq map__15167) (first map__15167) {}))
                          map__15167)
             opts map__15167
             default (get map__15167 :default)
             xform (get map__15167 :xform)]
         (apply
           comp
           (let [G__15168 [identity]
                 G__15168 (if xform (cons (try-xform xform) G__15168) G__15168)]
             (if (contains? opts :default)
               (cons (fn fn__15169 ([v] (if (nil? v) default v))) G__15168)
               G__15168)))))))
  (reset-meta!
    #'attr-with-opts->valfn
    (assoc
      {:private true, :arglists (clojure.core/list [['& 'args]]), :column (int 1)}
      :name
      'attr-with-opts->valfn
      :ns
      *ns*))
  (def attr-with-opts->attr-tuple
   (fn attr_with_opts__GT_attr_tuple
     ([p__15172]
       (let [vec__15173 p__15172
             seq__15174 (seq vec__15173)
             first__15175 (first seq__15174)
             seq__15174 (next seq__15174)
             attr first__15175
             args seq__15174
             map__15176 (apply hash-map args)
             map__15176 (if (seq? map__15176)
                          (if (next map__15176)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15176))
                            (if (seq map__15176) (first map__15176) {}))
                          map__15176)
             opts map__15176
             as (get map__15176 :as)]
         [attr
          (limit-default-from-map opts)
          (attr-with-opts->valfn args)
          (if as (constantly as) identity)]))))
  (reset-meta!
    #'attr-with-opts->attr-tuple
    (assoc
      {:arglists (clojure.core/list [['attr '& 'args]]), :column (int 1)}
      :name
      'attr-with-opts->attr-tuple
      :ns
      *ns*))
  (def normalize-attr
   (fn normalize_attr
     ([attr_spec]
       (if (attr-with-opts? attr_spec)
         (attr-with-opts->attr-tuple attr_spec)
         (conj (into [(attr-spec->attr attr_spec)] (attr-spec->fn attr_spec)) identity)))))
  (reset-meta!
    #'normalize-attr
    (assoc
      {:arglists (clojure.core/list ['attr-spec]), :column (int 1)}
      :name
      'normalize-attr
      :ns
      *ns*))
  (defn normalize-recur-limit
    ([x]
      (cond
        (#{"..." '...} x) '...
        (and (integer? x) (< 0 x)) x
        :default (do
                   (error/arg
                     :db.error/invalid-recur-limit
                     (str "Cannot interpret as a recursive pull specification: " x))))))
  (reset-meta!
    #'normalize-recur-limit
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'normalize-recur-limit
      :ns
      *ns*))
  (def normalize-pattern
   (fn normalize_pattern
     ([pull_spec]
       (cond
         (map? pull_spec) pull_spec
         (string? pull_spec) (normalize-pattern (edn/read-string pull_spec))
         :else (do
                 (let [direction (fn direction
                                   ([kw]
                                     (if (db/reverse-key? (db/normalize-kw kw))
                                       :reverse
                                       :forward)))]
                   (reduce
                     (fn fn__15183
                       ([m i]
                         (if (instance? java.util.Map i)
                           (reduce-kv
                             (fn fn__15184
                               ([m k subspec]
                                 (let [vec__15185 (normalize-attr k)
                                       attr_name (nth vec__15185 (int 0) nil)
                                       limit (nth vec__15185 (int 1) nil)
                                       valfn (nth vec__15185 (int 2) nil)
                                       keyfn (nth vec__15185 (int 3) nil)]
                                   (if (get-in
                                         m
                                         [(^clojure.lang.IFn direction attr_name) attr_name])
                                     (error/arg
                                       :pull/duplicate-attribute
                                       (str "Multiple specifications for " attr_name))
                                     (assoc-in
                                       m
                                       [(^clojure.lang.IFn direction attr_name) attr_name]
                                       {:limit limit,
                                        :valfn valfn,
                                        :keyfn keyfn,
                                        :subspec
                                        (if (sequential? subspec)
                                          (normalize-pattern subspec)
                                          (normalize-recur-limit subspec))})))))
                             m
                             i)
                           (let [vec__15189 (normalize-attr i)
                                 attr_name (nth vec__15189 (int 0) nil)
                                 limit (nth vec__15189 (int 1) nil)
                                 valfn (nth vec__15189 (int 2) nil)
                                 keyfn (nth vec__15189 (int 3) nil)
                                 attr_name_type (class attr_name)
                                 G__15192 (keyword attr_name)]
                             (case
                               G__15192
                               :*
                               (assoc m :wildcard attr_name_type :dbid attr_name_type)
                               :db/id
                               (assoc m :dbid attr_name_type)
                               (if (get-in m [(^clojure.lang.IFn direction attr_name) attr_name])
                                 (error/arg
                                   :pull/duplicate-attribute
                                   (str "Multiple specifications for " attr_name))
                                 (assoc-in
                                   m
                                   [(^clojure.lang.IFn direction attr_name) attr_name]
                                   {:limit limit, :keyfn keyfn, :valfn valfn})))))))
                     {}
                     pull_spec)))))))
  (reset-meta!
    #'normalize-pattern
    (assoc
      {:arglists (clojure.core/list ['pull-spec]), :column (int 1)}
      :name
      'normalize-pattern
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.pull" "normalized-pattern-cache") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.pull" "normalized-pattern-cache")
    (cache/create-computing normalize-pattern 1000))
  (defn next-a
    ([db e a]
      (let [iter (db/windowed
                   db
                   (fn fn__15196 ([p1__15195#] (= e (.e ^datomic.Datom p1__15195#))))
                   (.seekEAVT ^datomic.db.IDb db (db/datum db :e e :a (inc a))))
            d (iter/iget iter)]
        (when (and d (= e (.e ^datomic.Datom d))) (.a ^datomic.Datom d)))))
  (reset-meta!
    #'next-a
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'e 'a]), :column (int 1)}
      :name
      'next-a
      :ns
      *ns*))
  (deftype
    AIter
    [db ^long e ^{:tag long, :unsynchronized-mutable true} a]
    datomic.iter.Iter
    (next
      [this]
      (let [temp__5804__auto__ (next-a db (long e) (long a))]
        (when temp__5804__auto__ (let [na temp__5804__auto__] (set! a (long na)) this))))
    (get [this] (long a)))
  (clojure.core/import 'datomic.pull.AIter)
  (defn ->AIter
    ([db e a] (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a))))
  (reset-meta!
    #'->AIter
    (assoc {:arglists (clojure.core/list ['db 'e 'a]), :column (int 1)} :name '->AIter :ns *ns*))
  (defn a-iter
    ([db e]
      (let [temp__5804__auto__ (next-a db e 0)]
        (when temp__5804__auto__
          (let [a temp__5804__auto__]
            (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a)))))))
  (reset-meta!
    #'a-iter
    (assoc {:arglists (clojure.core/list ['db 'e]), :column (int 1)} :name 'a-iter :ns *ns*))
  (defn resolve-attr
    ([db kw]
      (let [G__15208 kw
            G__15208 (some-> G__15208 (db/normalize-kw))
            G__15208 (when-not (nil? G__15208) (db/resolve-id db G__15208))]
        (when-not (nil? G__15208) (db/attribute db G__15208)))))
  (reset-meta!
    #'resolve-attr
    (assoc
      {:arglists (clojure.core/list ['db 'kw]), :column (int 1)}
      :name
      'resolve-attr
      :ns
      *ns*))
  (def default-spec
   (fn default_spec
     ([attr kw db]
       (when (and attr (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
         (if (and
               (.-isComponent ^datomic.db.Attribute attr)
               (not (db/reverse-lookup? db (db/normalize-kw kw))))
           {:wildcard (class kw), :dbid (class kw)}
           {:dbid (class kw)})))))
  (reset-meta!
    #'default-spec
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'attr {:tag 'Attribute}) 'kw 'db]),
       :column (int 1)}
      :name
      'default-spec
      :ns
      *ns*))
  (defn denormalize-kw
    ([kw type]
      (let [pred__15213 = expr__15214 type]
        (if (^clojure.lang.IFn pred__15213 clojure.lang.Keyword expr__15214)
          kw
          (if (^clojure.lang.IFn pred__15213 clojure.lang.Symbol expr__15214)
            (symbol kw)
            (if (^clojure.lang.IFn pred__15213 java.lang.String expr__15214)
              (str kw)
              (do
                (throw
                  (java.lang.IllegalArgumentException. (str "No matching clause: " expr__15214)))
                nil)))))))
  (reset-meta!
    #'denormalize-kw
    (assoc
      {:arglists (clojure.core/list ['kw 'type]), :column (int 1)}
      :name
      'denormalize-kw
      :ns
      *ns*))
  (def fix-specs-for-underscore-prefix-attrs
   (fn fix_specs_for_underscore_prefix_attrs
     ([p__15216 db]
       (let [map__15217 p__15216
             map__15217 (if (seq? map__15217)
                          (if (next map__15217)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15217))
                            (if (seq map__15217) (first map__15217) {}))
                          map__15217)
             m map__15217
             forward (get map__15217 :forward)
             reverse (get map__15217 :reverse)
             temp__5802__auto__ (:_keys db)]
         (if temp__5802__auto__
           (let [ks temp__5802__auto__]
             {:forward (merge forward (select-keys reverse ks)),
              :reverse (apply dissoc reverse ks)})
           m)))))
  (reset-meta!
    #'fix-specs-for-underscore-prefix-attrs
    (assoc
      {:arglists (clojure.core/list [{:keys ['forward 'reverse], :as 'm} 'db]), :column (int 1)}
      :name
      'fix-specs-for-underscore-prefix-attrs
      :ns
      *ns*))
  (def pull*
   (fn pull_STAR_
     ([db p__15220 recursed prefer_aevt? e]
       (let [map__15221 p__15220
             map__15221 (if (seq? map__15221)
                          (if (next map__15221)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15221))
                            (if (seq map__15221) (first map__15221) {}))
                          map__15221)
             spec map__15221
             wildcard (get map__15221 :wildcard)
             dbid (get map__15221 :dbid)]
         (when (.isHistory ^datomic.Database db)
           (throw (java.lang.IllegalStateException. "Can't pull from history")))
         (let [map__15222 (fix-specs-for-underscore-prefix-attrs spec db)
               map__15222 (if (seq? map__15222)
                            (if (next map__15222)
                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                (to-array map__15222))
                              (if (seq map__15222) (first map__15222) {}))
                            map__15222)
               forward (get map__15222 :forward)
               reverse (get map__15222 :reverse)
               kw_>attr (partial resolve-attr db)
               mk_xf (fn mk_xf
                       ([path subspec def_subspec]
                         (let [vec__15224 (cond
                                            (nil? subspec) [def_subspec recursed]
                                            (map? subspec) [subspec recursed]
                                            (integer? subspec) (if
                                                                 (and
                                                                   (clojure.lang.Numbers/isPos
                                                                     subspec)
                                                                   (not
                                                                     (^clojure.lang.IFn recursed
                                                                       e)))
                                                                 [(update-in
                                                                    spec
                                                                    (conj path :subspec)
                                                                    dec)
                                                                  (conj recursed e)]
                                                                 [(let 
                                                                    [temp__5802__auto__
                                                                     (get spec :dbid)]
                                                                    (if
                                                                      temp__5802__auto__
                                                                      (let 
                                                                        [idc temp__5802__auto__]
                                                                        {:dbid idc})
                                                                      {}))
                                                                  recursed])
                                            (= '... subspec) (if
                                                               (not (^clojure.lang.IFn recursed e))
                                                               [spec (conj recursed e)]
                                                               [(let 
                                                                  [temp__5802__auto__
                                                                   (get spec :dbid)]
                                                                  (if
                                                                    temp__5802__auto__
                                                                    (let 
                                                                      [idc temp__5802__auto__]
                                                                      {:dbid idc})
                                                                    {}))
                                                                recursed])
                                            :else (do
                                                    (error/arg
                                                      :db.error/invalid-attr-subspec
                                                      (str
                                                        "Cannot interpret as sub-pull pattern: "
                                                        subspec
                                                        " of class: "
                                                        (class subspec)))))
                               _subspec (nth vec__15224 (int 0) nil)
                               _recursed (nth vec__15224 (int 1) nil)]
                           (if _subspec
                             (fn fn__15227 ([e] (pull* db _subspec _recursed prefer_aevt? e)))
                             identity))))
               eid (db/resolve-id db e)
               ret (transient (if dbid {(denormalize-kw :db/id dbid) eid} {}))
               ret (if wildcard
                     (iter/reduce
                       (fn fn__15233
                         ([ret a]
                           (let [kw (denormalize-kw (.ident ^datomic.Database db a) wildcard)
                                 attr (db/attribute db a)
                                 def_subspec (default-spec attr kw db)
                                 map__15234 (get forward kw)
                                 map__15234 (if (seq? map__15234)
                                              (if (next map__15234)
                                                (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                  (to-array map__15234))
                                                (if (seq map__15234) (first map__15234) {}))
                                              map__15234)
                                 forward_args map__15234
                                 valfn (get map__15234 :valfn)
                                 keyfn (get map__15234 :keyfn)
                                 subspec (get map__15234 :subspec)
                                 v (ea->v
                                     db
                                     eid
                                     attr
                                     (^clojure.lang.IFn mk_xf [] subspec def_subspec)
                                     (limit-default-from-map forward_args)
                                     (or valfn identity))]
                             (assoc! ret ((or keyfn identity) kw) v))))
                       ret
                       (a-iter db eid))
                     ret)
               ret (reduce-kv
                     (fn fn__15239
                       ([ret kw p__15238]
                         (let [map__15240 p__15238
                               map__15240 (if (seq? map__15240)
                                            (if (next map__15240)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__15240))
                                              (if (seq map__15240) (first map__15240) {}))
                                            map__15240)
                               limit (get map__15240 :limit)
                               valfn (get map__15240 :valfn)
                               keyfn (get map__15240 :keyfn)
                               subspec (get map__15240 :subspec)]
                           (if (get ret (^clojure.lang.IFn keyfn (denormalize-kw kw (type kw))))
                             ret
                             (let [attr (^clojure.lang.IFn kw_>attr kw)
                                   def_subspec (default-spec attr kw db)
                                   v (ea->v
                                       db
                                       eid
                                       attr
                                       (^clojure.lang.IFn mk_xf [:forward kw] subspec def_subspec)
                                       limit
                                       valfn
                                       (and prefer_aevt? (not wildcard)))]
                               (if (nil? v)
                                 ret
                                 (assoc!
                                   ret
                                   (^clojure.lang.IFn keyfn (denormalize-kw kw (type kw)))
                                   v)))))))
                     ret
                     forward)
               ret (reduce-kv
                     (fn fn__15244
                       ([ret kw p__15243]
                         (let [map__15245 p__15243
                               map__15245 (if (seq? map__15245)
                                            (if (next map__15245)
                                              (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                (to-array map__15245))
                                              (if (seq map__15245) (first map__15245) {}))
                                            map__15245)
                               limit (get map__15245 :limit)
                               valfn (get map__15245 :valfn)
                               keyfn (get map__15245 :keyfn)
                               subspec (get map__15245 :subspec)
                               attr (^clojure.lang.IFn kw_>attr (db/forward-attr (db/to-kw kw)))
                               def_subspec (default-spec attr kw db)
                               r (ra->e
                                   db
                                   eid
                                   attr
                                   (^clojure.lang.IFn mk_xf [:reverse kw] subspec def_subspec)
                                   limit
                                   valfn)]
                           (if (nil? r)
                             ret
                             (assoc!
                               ret
                               (^clojure.lang.IFn keyfn (denormalize-kw kw (type kw)))
                               r)))))
                     ret
                     reverse)]
           (nilify-empty (persistent! ret)))))))
  (reset-meta!
    #'pull*
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'db {:tag 'Database})
          {:keys ['wildcard 'dbid], :as 'spec}
          'recursed
          'prefer-aevt?
          'e]),
       :column (int 1)}
      :name
      'pull*
      :ns
      *ns*))
  (def parse-index-pull-arg-map
   (fn parse_index_pull_arg_map
     ([arg_map] (if (string? arg_map) (edn/read-string arg_map) arg_map))))
  (reset-meta!
    #'parse-index-pull-arg-map
    (assoc
      {:private true, :arglists (clojure.core/list ['arg-map]), :column (int 1)}
      :name
      'parse-index-pull-arg-map
      :ns
      *ns*))
  (def index-pull
   (fn index_pull
     ([db arg_map]
       (let [map__15250 (parse-index-pull-arg-map arg_map)
             map__15250 (if (seq? map__15250)
                          (if (next map__15250)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15250))
                            (if (seq map__15250) (first map__15250) {}))
                          map__15250)
             index (get map__15250 :index)
             reverse (get map__15250 :reverse)
             selector (get map__15250 :selector)
             start (get map__15250 :start)]
         (when (or (empty? start) (not selector) (not index))
           (error/arg
             :db.error/nil-input
             "selector, index, and start attribute must be specified"))
         (when (.isHistory ^datomic.Database db)
           (throw (java.lang.IllegalStateException. "Can't pull from history")))
         (let [a (first start)
               attrid (db/require-attrid db a)
               attribute (db/attribute db attrid)
               seek_fn (if reverse db/rseek-datoms db/seek-datoms)
               cached_selector (get normalized-pattern-cache selector)
               pull_kw (if (= index :avet) :e :v)]
           (cond
             (and (= index :avet) (= (:cardinality attribute) 36) (< (count start) 2)) (error/arg
                                                                                         :db.error/invalid-pull
                                                                                         (str
                                                                                           a
                                                                                           " is not card-one, as required for :avet when start has only A specified"))
             (and (= index :aevt) (not= (:cardinality attribute) 36)) (error/arg
                                                                        :db.error/invalid-pull
                                                                        (str
                                                                          a
                                                                          " is not card-many, as required for :aevt"))
             (and (= index :aevt) (not= (:vtypeid attribute) 20)) (error/arg
                                                                    :db.error/invalid-pull
                                                                    (str
                                                                      a
                                                                      " is not a ref, as required for :aevt"))
             (contains? #{:aevt :avet} index) (map
                                                (fn fn__15251
                                                  ([datom]
                                                    (delay
                                                      (pull*
                                                        db
                                                        cached_selector
                                                        #{}
                                                        true
                                                        (^clojure.lang.IFn pull_kw datom)))))
                                                (take-while
                                                  (fn fn__15255
                                                    ([p1__15249#]
                                                      (=
                                                        attrid
                                                        (long
                                                          (.getA
                                                            ^datomic.impl.db.IDatum p1__15249#)))))
                                                  (^clojure.lang.IFn seek_fn db index start)))
             :default (do
                        (error/arg
                          :db.error/invalid-pull
                          (str (name index) " is an invalid index, must be :avet or :aevt")))))))))
  (reset-meta!
    #'index-pull
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'arg-map]), :column (int 1)}
      :name
      'index-pull
      :ns
      *ns*))
  (def dereffed-index-pull
   (fn dereffed_index_pull ([db arg_map] (seq (map deref (index-pull db arg_map))))))
  (reset-meta!
    #'dereffed-index-pull
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'arg-map]), :column (int 1)}
      :name
      'dereffed-index-pull
      :ns
      *ns*))
  (def pull-1
   (fn pull_1
     ([db selector e p__15265]
       (let [map__15266 p__15265
             map__15266 (if (seq? map__15266)
                          (if (next map__15266)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15266))
                            (if (seq map__15266) (first map__15266) {}))
                          map__15266)
             options map__15266
             io_context (get map__15266 :io-context)
             f (fn f ([] (pull* db (get normalized-pattern-cache selector) #{} false e)))]
         (if io_context
           (io-stats/throw-if-ex! (io-stats/with-io-stats f {:io-context io_context, :api :pull}))
           (^clojure.lang.IFn f))))
     ([db selector e] (pull-1 db selector e nil))))
  (reset-meta!
    #'pull-1
    (assoc
      {:arglists
       (clojure.core/list
         ['db 'selector 'e]
         ['db 'selector 'e {:keys ['io-context], :as 'options}]),
       :column (int 1)}
      :name
      'pull-1
      :ns
      *ns*))
  (def pull
   (fn pull
     ([db selector es p__15270]
       (let [map__15271 p__15270
             map__15271 (if (seq? map__15271)
                          (if (next map__15271)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__15271))
                            (if (seq map__15271) (first map__15271) {}))
                          map__15271)
             options map__15271
             io_context (get map__15271 :io-context)
             f (fn f
                 ([]
                   (mapv (partial pull* db (get normalized-pattern-cache selector) #{} true) es)))]
         (if io_context
           (io-stats/throw-if-ex!
             (io-stats/with-io-stats f {:io-context io_context, :api :pull-many}))
           (^clojure.lang.IFn f))))
     ([db selector es] (pull db selector es nil))))
  (reset-meta!
    #'pull
    (assoc
      {:arglists
       (clojure.core/list
         ['db 'selector 'es]
         ['db 'selector 'es {:keys ['io-context], :as 'options}]),
       :column (int 1)}
      :name
      'pull
      :ns
      *ns*)))
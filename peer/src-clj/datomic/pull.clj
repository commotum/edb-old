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
  (defn limit-iterable
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
                    (.hasNext ^java.util.Iterator iter))))))))))
  (defn nilify-empty ([x] (when (seq x) x)))
  (defn ra->e
    ([db r attr xf limit valfn]
      (^clojure.lang.IFn valfn
        (when attr
          (let [rid (db/resolve-id db r)
                attrid (.id ^datomic.db.Attribute attr)
                mk_iter (fn mk_iter
                          ([]
                            (iter/map
                              (fn fn__18907 ([d] (.e ^datomic.Datom d)))
                              (db/windowed
                                db
                                (fn fn__18909
                                  ([p1__18905#]
                                    (and
                                      (= rid (.v ^datomic.Datom p1__18905#))
                                      (= attrid (.a ^datomic.Datom p1__18905#)))))
                                (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid))))))
                temp__5457__auto__ (^clojure.lang.IFn mk_iter)]
            (when temp__5457__auto__
              (let [iter temp__5457__auto__]
                (if (.-isComponent ^datomic.db.Attribute attr)
                  (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter))
                  (nilify-empty
                    (persistent!
                      (reduce
                        (fn fn__18913
                          ([coll item]
                            (let [temp__5455__auto__ (^clojure.lang.IFn xf item)]
                              (if temp__5455__auto__
                                (let [xitem temp__5455__auto__] (conj! coll xitem))
                                coll))))
                        (transient [])
                        (limit-iterable limit (iter/iterable mk_iter)))))))))))))
  (defn ea->v
    ([db e attr xf limit valfn use_aevt?]
      (^clojure.lang.IFn valfn
        (when attr
          (let [eid (db/resolve-id db e)
                attrid (.id ^datomic.db.Attribute attr)
                d (db/datum db :e eid :a attrid)
                mk_iter (fn mk_iter
                          ([]
                            (iter/map
                              (fn fn__18920 ([d] (.v ^datomic.Datom d)))
                              (db/windowed
                                db
                                (fn fn__18922
                                  ([p1__18918#]
                                    (and
                                      (= eid (.e ^datomic.Datom p1__18918#))
                                      (= attrid (.a ^datomic.Datom p1__18918#)))))
                                (if use_aevt?
                                  (.seekAEVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d)
                                  (.seekEAVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d))))))
                temp__5457__auto__ (^clojure.lang.IFn mk_iter)]
            (when temp__5457__auto__
              (let [iter temp__5457__auto__]
                (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                  (nilify-empty
                    (persistent!
                      (reduce
                        (fn fn__18926
                          ([coll item]
                            (let [xitem (^clojure.lang.IFn xf item)]
                              (if (nil? xitem) coll (conj! coll xitem)))))
                        (transient [])
                        (limit-iterable limit (iter/iterable mk_iter)))))
                  (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter)))))))))
    ([db e attr xf limit valfn] (ea->v db e attr xf limit valfn false)))
  (def default-limit (atom 1000))
  (defn attr-spec->fn
    ([attr_spec]
      (if (not (instance? java.util.List attr_spec))
        [(deref default-limit) identity]
        (let [vec__18930 attr_spec
              sym (nth vec__18930 (int 0) nil)
              kw (nth vec__18930 (int 1) nil)
              arg (nth vec__18930 (int 2) nil)
              G__18933 (db/normalize-kw sym)]
          (case
            G__18933
            :default
            [(deref default-limit) (fn fn__18934 ([v] (or v arg)))]
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
                (class attr_spec))))))))
  (defn attr-spec->attr
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
          attr_spec))))
  (defn attr-with-opts?
    ([expr]
      (and
        (instance? java.util.List expr)
        (odd? (java.lang.Integer/valueOf (int (count expr))))
        (keyword? (first expr)))))
  (defn limit-default-from-map
    ([p__18944]
      (let [map__18945 p__18944
            map__18945 (if (seq? map__18945)
                         (clojure.lang.PersistentHashMap/create (seq map__18945))
                         map__18945)
            args map__18945
            limit (get map__18945 :limit)]
        (if (contains? args :limit) limit (deref default-limit)))))
  (reset-meta!
    #'limit-default-from-map
    (assoc
      {:private true, :arglists (clojure.core/list [{:keys ['limit], :as 'args}]), :column 1}
      :name
      'limit-default-from-map
      :ns
      *ns*))
  (defn try-xform
    ([xform]
      (let [f (ext-resolver/resolve-xform! xform)]
        (fn fn__18947
          ([v]
            (try
              (^clojure.lang.IFn f v)
              (catch
                java.lang.Throwable
                t
                (do
                  (if (error/cancelled? (ex-data t))
                    (throw ^java.lang.Throwable t)
                    (throw
                      (error/eval-exception {:context :xform, :expr xform, :arguments [v]} t)))
                  nil))))))))
  (reset-meta!
    #'try-xform
    (assoc
      {:private true, :arglists (clojure.core/list ['xform]), :column 1}
      :name
      'try-xform
      :ns
      *ns*))
  (defn attr-with-opts->valfn
    ([p__18950]
      (let [vec__18951 p__18950
            seq__18952 (seq vec__18951)
            args seq__18952
            map__18954 (apply hash-map args)
            map__18954 (if (seq? map__18954)
                         (clojure.lang.PersistentHashMap/create (seq map__18954))
                         map__18954)
            opts map__18954
            default (get map__18954 :default)
            xform (get map__18954 :xform)]
        (apply
          comp
          (let [G__18955 [identity] G__18955 (if xform (cons (try-xform xform) G__18955) G__18955)]
            (if (contains? opts :default)
              (cons (fn fn__18956 ([v] (if (nil? v) default v))) G__18955)
              G__18955))))))
  (reset-meta!
    #'attr-with-opts->valfn
    (assoc
      {:private true, :arglists (clojure.core/list [['& 'args]]), :column 1}
      :name
      'attr-with-opts->valfn
      :ns
      *ns*))
  (defn attr-with-opts->attr-tuple
    ([p__18959]
      (let [vec__18960 p__18959
            seq__18961 (seq vec__18960)
            first__18962 (first seq__18961)
            seq__18961 (next seq__18961)
            attr first__18962
            args seq__18961
            map__18963 (apply hash-map args)
            map__18963 (if (seq? map__18963)
                         (clojure.lang.PersistentHashMap/create (seq map__18963))
                         map__18963)
            opts map__18963
            as (get map__18963 :as)]
        [attr
         (limit-default-from-map opts)
         (attr-with-opts->valfn args)
         (if as (constantly as) identity)])))
  (defn normalize-attr
    ([attr_spec]
      (if (attr-with-opts? attr_spec)
        (attr-with-opts->attr-tuple attr_spec)
        (conj (into [(attr-spec->attr attr_spec)] (attr-spec->fn attr_spec)) identity))))
  (defn normalize-recur-limit
    ([x]
      (cond
        (#{"..." '...} x) '...
        (and (integer? x) (< 0 x)) x
        :default (do
                   (error/arg
                     :db.error/invalid-recur-limit
                     (str "Cannot interpret as a recursive pull specification: " x))))))
  (defn normalize-pattern
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
                    (fn fn__18970
                      ([m i]
                        (if (instance? java.util.Map i)
                          (reduce-kv
                            (fn fn__18971
                              ([m k subspec]
                                (let [vec__18972 (normalize-attr k)
                                      attr_name (nth vec__18972 (int 0) nil)
                                      limit (nth vec__18972 (int 1) nil)
                                      valfn (nth vec__18972 (int 2) nil)
                                      keyfn (nth vec__18972 (int 3) nil)]
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
                          (let [vec__18976 (normalize-attr i)
                                attr_name (nth vec__18976 (int 0) nil)
                                limit (nth vec__18976 (int 1) nil)
                                valfn (nth vec__18976 (int 2) nil)
                                keyfn (nth vec__18976 (int 3) nil)
                                attr_name_type (class attr_name)
                                G__18979 (keyword attr_name)]
                            (case
                              G__18979
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
                    pull_spec))))))
  (def normalized-pattern-cache (cache/create-computing normalize-pattern 1000))
  (defn next-a
    ([db e a]
      (let [iter (db/windowed
                   db
                   (fn fn__18983 ([p1__18982#] (= e (.e ^datomic.Datom p1__18982#))))
                   (.seekEAVT ^datomic.db.IDb db (db/datum db :e e :a (inc a))))
            d (iter/iget iter)]
        (when (and d (= e (.e ^datomic.Datom d))) (.a ^datomic.Datom d)))))
  (reset-meta!
    #'next-a
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'e 'a]), :column 1}
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
      (let [temp__5457__auto__ (next-a db (long e) (long a))]
        (when temp__5457__auto__ (let [na temp__5457__auto__] (set! a (long na)) this))))
    (get [this] (long a)))
  (clojure.core/import 'datomic.pull.AIter)
  (defn ->AIter
    ([db e a] (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a))))
  (defn a-iter
    ([db e]
      (let [temp__5457__auto__ (next-a db e 0)]
        (when temp__5457__auto__
          (let [a temp__5457__auto__]
            (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a)))))))
  (defn resolve-attr
    ([db kw]
      (let [G__18995 kw
            G__18995 (some-> G__18995 (db/normalize-kw))
            G__18995 (when-not (nil? G__18995) (db/resolve-id db G__18995))]
        (when-not (nil? G__18995) (db/attribute db G__18995)))))
  (defn default-spec
    ([attr kw db]
      (when (and attr (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
        (if (and
              (.-isComponent ^datomic.db.Attribute attr)
              (not (db/reverse-lookup? db (db/normalize-kw kw))))
          {:wildcard (class kw), :dbid (class kw)}
          {:dbid (class kw)}))))
  (defn denormalize-kw
    ([kw type]
      (let [pred__19000 = expr__19001 type]
        (if (^clojure.lang.IFn pred__19000 clojure.lang.Keyword expr__19001)
          kw
          (if (^clojure.lang.IFn pred__19000 clojure.lang.Symbol expr__19001)
            (symbol kw)
            (if (^clojure.lang.IFn pred__19000 java.lang.String expr__19001)
              (str kw)
              (do
                (throw
                  (java.lang.IllegalArgumentException. (str "No matching clause: " expr__19001)))
                nil)))))))
  (defn fix-specs-for-underscore-prefix-attrs
    ([p__19003 db]
      (let [map__19004 p__19003
            map__19004 (if (seq? map__19004)
                         (clojure.lang.PersistentHashMap/create (seq map__19004))
                         map__19004)
            m map__19004
            forward (get map__19004 :forward)
            reverse (get map__19004 :reverse)
            temp__5455__auto__ (:_keys db)]
        (if temp__5455__auto__
          (let [ks temp__5455__auto__]
            {:forward (merge forward (select-keys reverse ks)),
             :reverse (apply dissoc reverse ks)})
          m))))
  (defn pull*
    ([db p__19007 recursed prefer_aevt? e]
      (let [map__19008 p__19007
            map__19008 (if (seq? map__19008)
                         (clojure.lang.PersistentHashMap/create (seq map__19008))
                         map__19008)
            spec map__19008
            wildcard (get map__19008 :wildcard)
            dbid (get map__19008 :dbid)]
        (when (.isHistory ^datomic.Database db)
          (throw (java.lang.IllegalStateException. "Can't pull from history")))
        (let [map__19009 (fix-specs-for-underscore-prefix-attrs spec db)
              map__19009 (if (seq? map__19009)
                           (clojure.lang.PersistentHashMap/create (seq map__19009))
                           map__19009)
              forward (get map__19009 :forward)
              reverse (get map__19009 :reverse)
              kw_>attr (partial resolve-attr db)
              mk_xf (fn mk_xf
                      ([path subspec def_subspec]
                        (let [vec__19011 (cond
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
                                                                   [temp__5455__auto__
                                                                    (get spec :dbid)]
                                                                   (if
                                                                     temp__5455__auto__
                                                                     (let 
                                                                       [idc temp__5455__auto__]
                                                                       {:dbid idc})
                                                                     {}))
                                                                 recursed])
                                           (= '... subspec) (if
                                                              (not (^clojure.lang.IFn recursed e))
                                                              [spec (conj recursed e)]
                                                              [(let 
                                                                 [temp__5455__auto__
                                                                  (get spec :dbid)]
                                                                 (if
                                                                   temp__5455__auto__
                                                                   (let 
                                                                     [idc temp__5455__auto__]
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
                              _subspec (nth vec__19011 (int 0) nil)
                              _recursed (nth vec__19011 (int 1) nil)]
                          (if _subspec
                            (fn fn__19014 ([e] (pull* db _subspec _recursed prefer_aevt? e)))
                            identity))))
              eid (db/resolve-id db e)
              ret (transient (if dbid {(denormalize-kw :db/id dbid) eid} {}))
              ret (if wildcard
                    (iter/reduce
                      (fn fn__19020
                        ([ret a]
                          (let [kw (denormalize-kw (.ident ^datomic.Database db a) wildcard)
                                attr (db/attribute db a)
                                def_subspec (default-spec attr kw db)
                                map__19021 (get forward kw)
                                map__19021 (if (seq? map__19021)
                                             (clojure.lang.PersistentHashMap/create
                                               (seq map__19021))
                                             map__19021)
                                forward_args map__19021
                                valfn (get map__19021 :valfn)
                                keyfn (get map__19021 :keyfn)
                                subspec (get map__19021 :subspec)
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
                    (fn fn__19026
                      ([ret kw p__19025]
                        (let [map__19027 p__19025
                              map__19027 (if (seq? map__19027)
                                           (clojure.lang.PersistentHashMap/create (seq map__19027))
                                           map__19027)
                              limit (get map__19027 :limit)
                              valfn (get map__19027 :valfn)
                              keyfn (get map__19027 :keyfn)
                              subspec (get map__19027 :subspec)]
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
                    (fn fn__19031
                      ([ret kw p__19030]
                        (let [map__19032 p__19030
                              map__19032 (if (seq? map__19032)
                                           (clojure.lang.PersistentHashMap/create (seq map__19032))
                                           map__19032)
                              limit (get map__19032 :limit)
                              valfn (get map__19032 :valfn)
                              keyfn (get map__19032 :keyfn)
                              subspec (get map__19032 :subspec)
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
          (nilify-empty (persistent! ret))))))
  (defn parse-index-pull-arg-map
    ([arg_map] (if (string? arg_map) (edn/read-string arg_map) arg_map)))
  (reset-meta!
    #'parse-index-pull-arg-map
    (assoc
      {:private true, :arglists (clojure.core/list ['arg-map]), :column 1}
      :name
      'parse-index-pull-arg-map
      :ns
      *ns*))
  (defn index-pull
    ([db arg_map]
      (let [map__19037 (parse-index-pull-arg-map arg_map)
            map__19037 (if (seq? map__19037)
                         (clojure.lang.PersistentHashMap/create (seq map__19037))
                         map__19037)
            index (get map__19037 :index)
            reverse (get map__19037 :reverse)
            selector (get map__19037 :selector)
            start (get map__19037 :start)]
        (when (or (empty? start) (not selector) (not index))
          (error/arg :db.error/nil-input "selector, index, and start attribute must be specified"))
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
                                               (fn fn__19038
                                                 ([datom]
                                                   (delay
                                                     (pull*
                                                       db
                                                       cached_selector
                                                       #{}
                                                       true
                                                       (^clojure.lang.IFn pull_kw datom)))))
                                               (take-while
                                                 (fn fn__19042
                                                   ([p1__19036#]
                                                     (=
                                                       attrid
                                                       (long
                                                         (.getA
                                                           ^datomic.impl.db.IDatum p1__19036#)))))
                                                 (^clojure.lang.IFn seek_fn db index start)))
            :default (do
                       (error/arg
                         :db.error/invalid-pull
                         (str (name index) " is an invalid index, must be :avet or :aevt"))))))))
  (defn dereffed-index-pull ([db arg_map] (seq (map deref (index-pull db arg_map)))))
  (defn pull-1
    ([db selector e p__19052]
      (let [map__19053 p__19052
            map__19053 (if (seq? map__19053)
                         (clojure.lang.PersistentHashMap/create (seq map__19053))
                         map__19053)
            options map__19053
            io_context (get map__19053 :io-context)
            f (fn f ([] (pull* db (get normalized-pattern-cache selector) #{} false e)))]
        (if io_context
          (io-stats/throw-if-ex! (io-stats/with-io-stats f {:io-context io_context, :api :pull}))
          (^clojure.lang.IFn f))))
    ([db selector e] (pull-1 db selector e nil)))
  (defn pull
    ([db selector es p__19057]
      (let [map__19058 p__19057
            map__19058 (if (seq? map__19058)
                         (clojure.lang.PersistentHashMap/create (seq map__19058))
                         map__19058)
            options map__19058
            io_context (get map__19058 :io-context)
            f (fn f
                ([]
                  (mapv (partial pull* db (get normalized-pattern-cache selector) #{} true) es)))]
        (if io_context
          (io-stats/throw-if-ex!
            (io-stats/with-io-stats f {:io-context io_context, :api :pull-many}))
          (^clojure.lang.IFn f))))
    ([db selector es] (pull db selector es nil))))
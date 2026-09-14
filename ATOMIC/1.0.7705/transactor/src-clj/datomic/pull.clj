(do
  (clojure.core/in-ns 'datomic.pull)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.pull)
    {:doc
     "Compiles pull patterns and realizes hierarchical entity selections. Patterns support forward and reverse attributes, wildcards, nested maps, bounded or unbounded recursion, result-key aliases, cardinality-many limits, defaults, and configured value transforms. Cardinality-many attributes return at most 1000 values unless :limit supplies a positive bound or nil for all values. Recursive traversal tracks visited entities to terminate cycles. Index pull combines the same selectors with lazy AVET or AEVT index walks."})
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
  ;; ATOMIC-NOTE [observed; bounded work]: Limit wraps the source iterator before nested
  ;; projection in ea->v/ra->e, not after all children are pulled. Native bounded
  ;; cursors retain that ordering and poll even during filter-rejected candidates.
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
  (reset-meta!
    #'limit-iterable
    (assoc
      {:arglists (clojure.core/list ['limit (.withMeta 'iterable {:tag 'Iterable})]),
       :column (int 1)}
      :name
      'limit-iterable
      :ns
      *ns*))
  ;; ATOMIC-NOTE [source/prose boundary]: Recovered empty roots and nested collections
  ;; become nil here. Pro Pull / Empty Results specifies {} and retained empty
  ;; nested vectors. Native projection deliberately follows those documented shapes.
  (defn nilify-empty ([x] (when (seq x) x)))
  (reset-meta!
    #'nilify-empty
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'nilify-empty :ns *ns*))
  ;; ATOMIC-NOTE [observed; shape]: Reverse components select one parent; other reverse
  ;; refs collect a vector. This cardinality rule is separate from default-spec's
  ;; forward-only component expansion: implicit reverse components are id-only maps.
  (defn ra->e
    ([db r attr xf limit valfn]
      (^clojure.lang.IFn valfn
        (when attr
          (let [rid (db/resolve-id db r)
                attrid (.id ^datomic.db.Attribute attr)
                mk_iter (fn mk_iter
                          ([]
                            (iter/map
                              (fn fn__16225 ([d] (.e ^datomic.Datom d)))
                              (db/windowed
                                db
                                (fn fn__16227
                                  ([p1__16223#]
                                    (and
                                      (= rid (.v ^datomic.Datom p1__16223#))
                                      (= attrid (.a ^datomic.Datom p1__16223#)))))
                                (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid))))))
                temp__5825__auto__ (^clojure.lang.IFn mk_iter)]
            (when temp__5825__auto__
              (let [iter temp__5825__auto__]
                (if (.-isComponent ^datomic.db.Attribute attr)
                  (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter))
                  (nilify-empty
                    (persistent!
                      (reduce
                        (fn fn__16231
                          ([coll item]
                            (let [temp__5823__auto__ (^clojure.lang.IFn xf item)]
                              (if temp__5823__auto__
                                (let [xitem temp__5823__auto__] (conj! coll xitem))
                                coll))))
                        (transient [])
                        (limit-iterable limit (iter/iterable mk_iter)))))))))))))
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
                              (fn fn__16238 ([d] (.v ^datomic.Datom d)))
                              (db/windowed
                                db
                                (fn fn__16240
                                  ([p1__16236#]
                                    (and
                                      (= eid (.e ^datomic.Datom p1__16236#))
                                      (= attrid (.a ^datomic.Datom p1__16236#)))))
                                (if use_aevt?
                                  (.seekAEVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d)
                                  (.seekEAVT ^datomic.db.IDb db ^datomic.impl.db.IDatum d))))))
                temp__5825__auto__ (^clojure.lang.IFn mk_iter)]
            (when temp__5825__auto__
              (let [iter temp__5825__auto__]
                (if (= 36 (.-cardinality ^datomic.db.Attribute attr))
                  (nilify-empty
                    (persistent!
                      (reduce
                        (fn fn__16244
                          ([coll item]
                            (let [xitem (^clojure.lang.IFn xf item)]
                              (if (nil? xitem) coll (conj! coll xitem)))))
                        (transient [])
                        (limit-iterable limit (iter/iterable mk_iter)))))
                  (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter)))))))))
    ([db e attr xf limit valfn] (ea->v db e attr xf limit valfn false)))
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
  (.setMeta
    (clojure.lang.RT/var "datomic.pull" "default-limit")
    {:doc "Default maximum number of values returned for a cardinality-many attribute.",
     :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.pull" "default-limit") (atom 1000))
  (defn attr-spec->fn
    ([attr-spec]
      (if (not (instance? java.util.List attr-spec))
        [(deref default-limit) identity]
        (let [vec__16248 attr-spec
              sym (nth vec__16248 (int 0) nil)
              kw (nth vec__16248 (int 1) nil)
              arg (nth vec__16248 (int 2) nil)
              G__16251 (db/normalize-kw sym)]
          (case
            G__16251
            :default
            [(deref default-limit) (fn fn__16252 ([v] (or v arg)))]
            :limit
            (if (or (integer? arg) (nil? arg))
              [arg identity]
              (error/arg
                :db.error/invalid-limit
                (str "'" arg "' is not a valid limit in '" attr-spec "'")))
            (error/arg
              :db.error/invalid-attr-spec
              (str
                "Cannot interpret as an attribute spec: "
                attr-spec
                " of class: "
                (class attr-spec))))))))
  (reset-meta!
    #'attr-spec->fn
    (assoc
      {:arglists (clojure.core/list ['attr-spec]), :column (int 1)}
      :name
      'attr-spec->fn
      :ns
      *ns*))
  (defn attr-spec->attr
    ([attr-spec]
      (if (instance? java.util.List attr-spec)
        (attr-spec->attr (second attr-spec))
        (if (not (keyword? attr-spec))
          (let [s (str attr-spec) s (if (or (= s "*") (= s ":*")) "*" s)]
            (if (= s "*")
              (if (string? attr-spec) s (keyword attr-spec))
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
          attr-spec))))
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
  (defn limit-default-from-map
    ([p__16262]
      (let [map__16263 p__16262
            map__16263 (if (seq? map__16263)
                         (if (next map__16263)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16263))
                           (if (seq map__16263) (first map__16263) {}))
                         map__16263)
            args map__16263
            limit (get map__16263 :limit)]
        (if (contains? args :limit) limit (deref default-limit)))))
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
        (fn fn__16265
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
      {:private true,
       :arglists (clojure.core/list ['xform]),
       :doc
       "Resolves an allowed pull transform and returns a wrapper that preserves cancellation and reports transform failures with the expression and input value.",
       :column (int 1)}
      :name
      'try-xform
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; unresolved entities]: ea->v/ra->e call valfn even without a
  ;; resolved value. An early native unresolved-id return must not skip explicit
  ;; xform(nil) followed by default only when the transform result is nil.
  (defn attr-with-opts->valfn
    ([p__16268]
      (let [vec__16269 p__16268
            seq__16270 (seq vec__16269)
            args seq__16270
            map__16272 (apply hash-map args)
            map__16272 (if (seq? map__16272)
                         (if (next map__16272)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16272))
                           (if (seq map__16272) (first map__16272) {}))
                         map__16272)
            opts map__16272
            default (get map__16272 :default)
            xform (get map__16272 :xform)]
        (apply
          comp
          (let [G__16273 [identity] G__16273 (if xform (cons (try-xform xform) G__16273) G__16273)]
            (if (contains? opts :default)
              (cons (fn fn__16274 ([v] (if (nil? v) default v))) G__16273)
              G__16273))))))
  (reset-meta!
    #'attr-with-opts->valfn
    (assoc
      {:private true,
       :arglists (clojure.core/list [['& 'args]]),
       :doc
       "Builds the value transform for an attribute expression. A configured :xform receives the pulled value, including nil. Its result takes precedence; :default is applied afterward only when that result is nil and is never passed through the transform.",
       :column (int 1)}
      :name
      'attr-with-opts->valfn
      :ns
      *ns*))
  (defn attr-with-opts->attr-tuple
    ([p__16277]
      (let [vec__16278 p__16277
            seq__16279 (seq vec__16278)
            first__16280 (first seq__16279)
            seq__16279 (next seq__16279)
            attr first__16280
            args seq__16279
            map__16281 (apply hash-map args)
            map__16281 (if (seq? map__16281)
                         (if (next map__16281)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16281))
                           (if (seq map__16281) (first map__16281) {}))
                         map__16281)
            opts map__16281
            as (get map__16281 :as)]
        [attr
         (limit-default-from-map opts)
         (attr-with-opts->valfn args)
         (if as (constantly as) identity)])))
  (reset-meta!
    #'attr-with-opts->attr-tuple
    (assoc
      {:arglists (clojure.core/list [['attr '& 'args]]), :column (int 1)}
      :name
      'attr-with-opts->attr-tuple
      :ns
      *ns*))
  (defn normalize-attr
    ([attr-spec]
      (if (attr-with-opts? attr-spec)
        (attr-with-opts->attr-tuple attr-spec)
        (conj (into [(attr-spec->attr attr-spec)] (attr-spec->fn attr-spec)) identity))))
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
  ;; ATOMIC-NOTE [observed; preparation]: Validate grammar/duplicates before entity reads.
  ;; Wildcard overrides remain explicit forward specs. The normalized-pattern cache
  ;; stores selectors, not answers. Native typed patterns and bounded EDN admission
  ;; replace host reader/key-class conveniences without a second traversal engine.
  (defn normalize-pattern
    ([pull-spec]
      (cond
        (map? pull-spec) pull-spec
        (string? pull-spec) (normalize-pattern (edn/read-string pull-spec))
        :else (do
                (let [direction (fn direction
                                  ([kw]
                                    (if (db/reverse-key? (db/normalize-kw kw))
                                      :reverse
                                      :forward)))]
                  (reduce
                    (fn fn__16288
                      ([m i]
                        (if (instance? java.util.Map i)
                          (reduce-kv
                            (fn fn__16289
                              ([m k subspec]
                                (let [vec__16290 (normalize-attr k)
                                      attr_name (nth vec__16290 (int 0) nil)
                                      limit (nth vec__16290 (int 1) nil)
                                      valfn (nth vec__16290 (int 2) nil)
                                      keyfn (nth vec__16290 (int 3) nil)]
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
                          (let [vec__16294 (normalize-attr i)
                                attr_name (nth vec__16294 (int 0) nil)
                                limit (nth vec__16294 (int 1) nil)
                                valfn (nth vec__16294 (int 2) nil)
                                keyfn (nth vec__16294 (int 3) nil)
                                attr_name_type (class attr_name)
                                G__16297 (keyword attr_name)]
                            (case
                              G__16297
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
                    pull-spec))))))
  (reset-meta!
    #'normalize-pattern
    (assoc
      {:arglists (clojure.core/list ['pull-spec]),
       :doc
       "Compiles an EDN pull pattern into forward and reverse attribute specifications. Accepts an EDN string, a pattern sequence, or an already normalized map. Validates recursion limits and rejects duplicate attribute specifications. Wildcards include direct attributes and recursively select component references; ordinary references default to :db/id.",
       :column (int 1)}
      :name
      'normalize-pattern
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.pull" "normalized-pattern-cache") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.pull" "normalized-pattern-cache")
    (cache/create-computing normalize-pattern 1000))
  ;; ATOMIC-NOTE [observed; wildcard cost]: Seeking (e,a+1) skips the unselected many-value
  ;; tail of a. Native wildcard discovery retains this property after explicit
  ;; limit/alias overrides while still discovering later attributes.
  (defn next-a
    ([db e a]
      (let [iter (db/windowed
                   db
                   (fn fn__16301 ([p1__16300#] (= e (.e ^datomic.Datom p1__16300#))))
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
      (let [temp__5825__auto__ (next-a db (long e) (long a))]
        (when temp__5825__auto__ (let [na temp__5825__auto__] (set! a (long na)) this))))
    (get [this] (long a)))
  (clojure.core/import 'datomic.pull.AIter)
  (defn ->AIter
    ([db e a] (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a))))
  (reset-meta!
    #'->AIter
    (assoc {:arglists (clojure.core/list ['db 'e 'a]), :column (int 1)} :name '->AIter :ns *ns*))
  (defn a-iter
    ([db e]
      (let [temp__5825__auto__ (next-a db e 0)]
        (when temp__5825__auto__
          (let [a temp__5825__auto__]
            (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a)))))))
  (reset-meta!
    #'a-iter
    (assoc {:arglists (clojure.core/list ['db 'e]), :column (int 1)} :name 'a-iter :ns *ns*))
  (defn resolve-attr
    ([db kw]
      (let [G__16313 kw
            G__16313 (some-> G__16313 (db/normalize-kw))
            G__16313 (when-not (nil? G__16313) (db/resolve-id db G__16313))]
        (when-not (nil? G__16313) (db/attribute db G__16313)))))
  (reset-meta!
    #'resolve-attr
    (assoc
      {:arglists (clojure.core/list ['db 'kw]), :column (int 1)}
      :name
      'resolve-attr
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; defaults]: Only forward component refs get wildcard subpatterns.
  ;; All other refs get :db/id. Explicit nesting overrides either default; keep
  ;; this decision separate from whether the direction returns one value or many.
  (defn default-spec
    ([attr kw db]
      (when (and attr (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
        (if (and
              (.-isComponent ^datomic.db.Attribute attr)
              (not (db/reverse-lookup? db (db/normalize-kw kw))))
          {:wildcard (class kw), :dbid (class kw)}
          {:dbid (class kw)}))))
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
      (let [pred__16318 = expr__16319 type]
        (if (^clojure.lang.IFn pred__16318 clojure.lang.Keyword expr__16319)
          kw
          (if (^clojure.lang.IFn pred__16318 clojure.lang.Symbol expr__16319)
            (symbol kw)
            (if (^clojure.lang.IFn pred__16318 java.lang.String expr__16319)
              (str kw)
              (do
                (throw
                  (java.lang.IllegalArgumentException. (str "No matching clause: " expr__16319)))
                nil)))))))
  (reset-meta!
    #'denormalize-kw
    (assoc
      {:arglists (clojure.core/list ['kw 'type]), :column (int 1)}
      :name
      'denormalize-kw
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; schema wins]: An exact installed :ns/_a wins over reverse :ns/a.
  ;; Normalization needs this database's schema to decide. SchemaResolved directions
  ;; defer that choice; explicitly typed Forward/Reverse remain unambiguous.
  (defn fix-specs-for-underscore-prefix-attrs
    ([p__16321 db]
      (let [map__16322 p__16321
            map__16322 (if (seq? map__16322)
                         (if (next map__16322)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16322))
                           (if (seq map__16322) (first map__16322) {}))
                         map__16322)
            m map__16322
            forward (get map__16322 :forward)
            reverse (get map__16322 :reverse)
            temp__5823__auto__ (:_keys db)]
        (if temp__5823__auto__
          (let [ks temp__5823__auto__]
            {:forward (merge forward (select-keys reverse ks)),
             :reverse (apply dissoc reverse ks)})
          m))))
  (reset-meta!
    #'fix-specs-for-underscore-prefix-attrs
    (assoc
      {:arglists (clojure.core/list [{:keys ['forward 'reverse], :as 'm} 'db]), :column (int 1)}
      :name
      'fix-specs-for-underscore-prefix-attrs
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; value boundary]: Every nested call retains db and rejects history.
  ;; mk_xf carries path recursion state, not a global database depth. Native task
  ;; stacks add stack-safe ownership/component-cycle termination; query projection
  ;; also shares its original work, deadline and value-byte accounting.
  (defn pull*
    ([db {:keys [wildcard dbid], :as spec} recursed prefer-aevt? e]
      (when (.isHistory ^datomic.Database db)
        (throw (java.lang.IllegalStateException. "Can't pull from history")))
      (let [{:keys [forward reverse]} (fix-specs-for-underscore-prefix-attrs spec db)
              kw_>attr (partial resolve-attr db)
              mk_xf (fn mk_xf
                      ([path subspec def_subspec]
                        (let [vec__16329 (cond
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
                                                                   [temp__5823__auto__
                                                                    (get spec :dbid)]
                                                                   (if
                                                                     temp__5823__auto__
                                                                     (let
                                                                       [idc temp__5823__auto__]
                                                                       {:dbid idc})
                                                                     {}))
                                                                 recursed])
                                           (= '... subspec) (if
                                                              (not (^clojure.lang.IFn recursed e))
                                                              [spec (conj recursed e)]
                                                              [(let
                                                                 [temp__5823__auto__
                                                                  (get spec :dbid)]
                                                                 (if
                                                                   temp__5823__auto__
                                                                   (let
                                                                     [idc temp__5823__auto__]
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
                              _subspec (nth vec__16329 (int 0) nil)
                              _recursed (nth vec__16329 (int 1) nil)]
                          (if _subspec
                            (fn fn__16332 ([e] (pull* db _subspec _recursed prefer-aevt? e)))
                            identity))))
              eid (db/resolve-id db e)
              ret (transient (if dbid {(denormalize-kw :db/id dbid) eid} {}))
              ret (if wildcard
                    (iter/reduce
                      (fn fn__16338
                        ([ret a]
                          (let [kw (denormalize-kw (.ident ^datomic.Database db a) wildcard)
                                attr (db/attribute db a)
                                def_subspec (default-spec attr kw db)
                                map__16339 (get forward kw)
                                map__16339 (if (seq? map__16339)
                                             (if (next map__16339)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__16339))
                                               (if (seq map__16339) (first map__16339) {}))
                                             map__16339)
                                forward_args map__16339
                                valfn (get map__16339 :valfn)
                                keyfn (get map__16339 :keyfn)
                                subspec (get map__16339 :subspec)
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
                    (fn fn__16344
                      ([ret kw p__16343]
                        (let [map__16345 p__16343
                              map__16345 (if (seq? map__16345)
                                           (if (next map__16345)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__16345))
                                             (if (seq map__16345) (first map__16345) {}))
                                           map__16345)
                              limit (get map__16345 :limit)
                              valfn (get map__16345 :valfn)
                              keyfn (get map__16345 :keyfn)
                              subspec (get map__16345 :subspec)]
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
                                      (and prefer-aevt? (not wildcard)))]
                              (if (nil? v)
                                ret
                                (assoc!
                                  ret
                                  (^clojure.lang.IFn keyfn (denormalize-kw kw (type kw)))
                                  v)))))))
                    ret
                    forward)
              ret (reduce-kv
                    (fn fn__16349
                      ([ret kw p__16348]
                        (let [map__16350 p__16348
                              map__16350 (if (seq? map__16350)
                                           (if (next map__16350)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__16350))
                                             (if (seq map__16350) (first map__16350) {}))
                                           map__16350)
                              limit (get map__16350 :limit)
                              valfn (get map__16350 :valfn)
                              keyfn (get map__16350 :keyfn)
                              subspec (get map__16350 :subspec)
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
        (nilify-empty (persistent! ret)))))
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
  (defn parse-index-pull-arg-map
    ([arg-map] (if (string? arg-map) (edn/read-string arg-map) arg-map)))
  (reset-meta!
    #'parse-index-pull-arg-map
    (assoc
      {:private true, :arglists (clojure.core/list ['arg-map]), :column (int 1)}
      :name
      'parse-index-pull-arg-map
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed; lazy range]: The same db supplies the seek and each delayed map.
  ;; The attribute fence is not a second-component equality filter; AEVT can repeat
  ;; entities. Native cursor errors fuse iteration, and cancellation reaches inside
  ;; filter consumption rather than only the outer next-result boundary.
  (defn index-pull
    ([db arg-map]
      (let [map__16355 (parse-index-pull-arg-map arg-map)
            map__16355 (if (seq? map__16355)
                         (if (next map__16355)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__16355))
                           (if (seq map__16355) (first map__16355) {}))
                         map__16355)
            index (get map__16355 :index)
            reverse (get map__16355 :reverse)
            selector (get map__16355 :selector)
            start (get map__16355 :start)]
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
                                               (fn fn__16356
                                                 ([datom]
                                                   (delay
                                                     (pull*
                                                       db
                                                       cached_selector
                                                       #{}
                                                       true
                                                       (^clojure.lang.IFn pull_kw datom)))))
                                               (take-while
                                                 (fn fn__16360
                                                   ([p1__16354#]
                                                     (=
                                                       attrid
                                                       (long
                                                         (.getA
                                                           ^datomic.impl.db.IDatum p1__16354#)))))
                                                 (^clojure.lang.IFn seek_fn db index start)))
            :default (do
                       (error/arg
                         :db.error/invalid-pull
                         (str (name index) " is an invalid index, must be :avet or :aevt"))))))))
  (reset-meta!
    #'index-pull
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'arg-map]),
       :doc
       "Walks :avet or :aevt from :start and returns a lazy sequence of delayed pull results using :selector. :start contains components in index order and must begin with an attribute. :reverse selects reverse iteration. AVET pulls the entity component and requires a value component for cardinality-many attributes. AEVT pulls reference values from cardinality-many reference attributes and can return an entity more than once. History database values are rejected.",
       :column (int 1)}
      :name
      'index-pull
      :ns
      *ns*))
  (defn dereffed-index-pull ([db arg-map] (seq (map deref (index-pull db arg-map)))))
  (reset-meta!
    #'dereffed-index-pull
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'arg-map]),
       :doc
       "Returns the lazy index-pull result sequence, realizing each selected entity map as it is consumed.",
       :column (int 1)}
      :name
      'dereffed-index-pull
      :ns
      *ns*))
  ;; ATOMIC-NOTE [adaptation; controls]: io-context wraps accounting here, not a global
  ;; traversal budget. Native PullControl adds depth/entity/cancel limits; query
  ;; Pull additionally shares work/deadline/value limits through lazy consumption.
  (defn pull-1
    ([db selector e {:keys [io-context], :as options}]
      (let [run-pull (fn [] (pull* db (get normalized-pattern-cache selector) #{} false e))]
        (if io-context
          (io-stats/throw-if-ex!
            (io-stats/with-io-stats run-pull {:io-context io-context, :api :pull}))
          (run-pull))))
    ([db selector e] (pull-1 db selector e nil)))
  (reset-meta!
    #'pull-1
    (assoc
      {:arglists
       (clojure.core/list
         ['db 'selector 'e]
         ['db 'selector 'e {:keys ['io-context], :as 'options}]),
       :doc
       "Applies selector to one entity identifier in db. Normally returns the selected attribute map, or nil when no selected attribute yields a value. Selector may be an EDN string or pattern data. Missing attributes are omitted unless they specify :default; recursive cycles terminate with :db/id. With :io-context, returns a map containing the pull result under :ret and index I/O accounting under :io-stats.",
       :column (int 1)}
      :name
      'pull-1
      :ns
      *ns*))
  (defn pull
    ([db selector es {:keys [io-context], :as options}]
      (let [run-pull (fn []
                       (mapv
                         (partial pull* db (get normalized-pattern-cache selector) #{} true)
                         es))]
        (if io-context
          (io-stats/throw-if-ex!
            (io-stats/with-io-stats
              run-pull
              {:io-context io-context, :api :pull-many}))
          (run-pull))))
    ([db selector es] (pull db selector es nil)))
  (reset-meta!
    #'pull
    (assoc
      {:arglists
       (clojure.core/list
         ['db 'selector 'es]
         ['db 'selector 'es {:keys ['io-context], :as 'options}]),
       :doc
       "Applies selector to each entity identifier in es. Normally returns a vector preserving input order; each entry is the selected map, or nil when no selected attribute yields a value. Cardinality-many attributes return collections and reverse attributes navigate incoming references. With :io-context, returns a map containing the result vector under :ret and index I/O accounting under :io-stats.",
       :column (int 1)}
      :name
      'pull
      :ns
      *ns*)))

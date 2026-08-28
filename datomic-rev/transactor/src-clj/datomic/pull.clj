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
                              (fn fn__15089 ([d] (.e ^datomic.Datom d)))
                              (db/windowed
                                db
                                (fn fn__15091
                                  ([p1__15087#]
                                    (and
                                      (= rid (.v ^datomic.Datom p1__15087#))
                                      (= attrid (.a ^datomic.Datom p1__15087#)))))
                                (.seekRAET ^datomic.db.IDb db (db/datum db :v rid :a attrid))))))
                temp__5804__auto__ (^clojure.lang.IFn mk_iter)]
            (when temp__5804__auto__
              (let [iter temp__5804__auto__]
                (if (.-isComponent ^datomic.db.Attribute attr)
                  (^clojure.lang.IFn xf (.get ^datomic.iter.Iter iter))
                  (nilify-empty
                    (persistent!
                      (reduce
                        (fn fn__15095
                          ([coll item]
                            (let [temp__5802__auto__ (^clojure.lang.IFn xf item)]
                              (if temp__5802__auto__
                                (let [xitem temp__5802__auto__] (conj! coll xitem))
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
                              (fn fn__15102 ([d] (.v ^datomic.Datom d)))
                              (db/windowed
                                db
                                (fn fn__15104
                                  ([p1__15100#]
                                    (and
                                      (= eid (.e ^datomic.Datom p1__15100#))
                                      (= attrid (.a ^datomic.Datom p1__15100#)))))
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
                        (fn fn__15108
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
        (let [vec__15112 attr_spec
              sym (nth vec__15112 (int 0) nil)
              kw (nth vec__15112 (int 1) nil)
              arg (nth vec__15112 (int 2) nil)
              G__15115 (db/normalize-kw sym)]
          (case
            G__15115
            :default
            [(deref default-limit) (fn fn__15116 ([v] (or v arg)))]
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
    ([p__15126]
      (let [map__15127 p__15126
            map__15127 (if (seq? map__15127)
                         (if (next map__15127)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15127))
                           (if (seq map__15127) (first map__15127) {}))
                         map__15127)
            args map__15127
            limit (get map__15127 :limit)]
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
        (fn fn__15129
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
      {:private true, :arglists (clojure.core/list ['xform]), :column 1}
      :name
      'try-xform
      :ns
      *ns*))
  (defn attr-with-opts->valfn
    ([p__15132]
      (let [vec__15133 p__15132
            seq__15134 (seq vec__15133)
            args seq__15134
            map__15136 (apply hash-map args)
            map__15136 (if (seq? map__15136)
                         (if (next map__15136)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15136))
                           (if (seq map__15136) (first map__15136) {}))
                         map__15136)
            opts map__15136
            default (get map__15136 :default)
            xform (get map__15136 :xform)]
        (apply
          comp
          (let [G__15137 [identity] G__15137 (if xform (cons (try-xform xform) G__15137) G__15137)]
            (if (contains? opts :default)
              (cons (fn fn__15138 ([v] (if (nil? v) default v))) G__15137)
              G__15137))))))
  (reset-meta!
    #'attr-with-opts->valfn
    (assoc
      {:private true, :arglists (clojure.core/list [['& 'args]]), :column 1}
      :name
      'attr-with-opts->valfn
      :ns
      *ns*))
  (defn attr-with-opts->attr-tuple
    ([p__15141]
      (let [vec__15142 p__15141
            seq__15143 (seq vec__15142)
            first__15144 (first seq__15143)
            seq__15143 (next seq__15143)
            attr first__15144
            args seq__15143
            map__15145 (apply hash-map args)
            map__15145 (if (seq? map__15145)
                         (if (next map__15145)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15145))
                           (if (seq map__15145) (first map__15145) {}))
                         map__15145)
            opts map__15145
            as (get map__15145 :as)]
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
                    (fn fn__15152
                      ([m i]
                        (if (instance? java.util.Map i)
                          (reduce-kv
                            (fn fn__15153
                              ([m k subspec]
                                (let [vec__15154 (normalize-attr k)
                                      attr_name (nth vec__15154 (int 0) nil)
                                      limit (nth vec__15154 (int 1) nil)
                                      valfn (nth vec__15154 (int 2) nil)
                                      keyfn (nth vec__15154 (int 3) nil)]
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
                          (let [vec__15158 (normalize-attr i)
                                attr_name (nth vec__15158 (int 0) nil)
                                limit (nth vec__15158 (int 1) nil)
                                valfn (nth vec__15158 (int 2) nil)
                                keyfn (nth vec__15158 (int 3) nil)
                                attr_name_type (class attr_name)
                                G__15161 (keyword attr_name)]
                            (case
                              G__15161
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
                   (fn fn__15165 ([p1__15164#] (= e (.e ^datomic.Datom p1__15164#))))
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
      (let [temp__5804__auto__ (next-a db (long e) (long a))]
        (when temp__5804__auto__ (let [na temp__5804__auto__] (set! a (long na)) this))))
    (get [this] (long a)))
  (clojure.core/import 'datomic.pull.AIter)
  (defn ->AIter
    ([db e a] (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a))))
  (defn a-iter
    ([db e]
      (let [temp__5804__auto__ (next-a db e 0)]
        (when temp__5804__auto__
          (let [a temp__5804__auto__]
            (datomic.pull.AIter. db (long ^java.lang.Number e) (long ^java.lang.Number a)))))))
  (defn resolve-attr
    ([db kw]
      (let [G__15177 kw
            G__15177 (some-> G__15177 (db/normalize-kw))
            G__15177 (when-not (nil? G__15177) (db/resolve-id db G__15177))]
        (when-not (nil? G__15177) (db/attribute db G__15177)))))
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
      (let [pred__15182 = expr__15183 type]
        (if (^clojure.lang.IFn pred__15182 clojure.lang.Keyword expr__15183)
          kw
          (if (^clojure.lang.IFn pred__15182 clojure.lang.Symbol expr__15183)
            (symbol kw)
            (if (^clojure.lang.IFn pred__15182 java.lang.String expr__15183)
              (str kw)
              (do
                (throw
                  (java.lang.IllegalArgumentException. (str "No matching clause: " expr__15183)))
                nil)))))))
  (defn fix-specs-for-underscore-prefix-attrs
    ([p__15185 db]
      (let [map__15186 p__15185
            map__15186 (if (seq? map__15186)
                         (if (next map__15186)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15186))
                           (if (seq map__15186) (first map__15186) {}))
                         map__15186)
            m map__15186
            forward (get map__15186 :forward)
            reverse (get map__15186 :reverse)
            temp__5802__auto__ (:_keys db)]
        (if temp__5802__auto__
          (let [ks temp__5802__auto__]
            {:forward (merge forward (select-keys reverse ks)),
             :reverse (apply dissoc reverse ks)})
          m))))
  (defn pull*
    ([db p__15189 recursed prefer_aevt? e]
      (let [map__15190 p__15189
            map__15190 (if (seq? map__15190)
                         (if (next map__15190)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15190))
                           (if (seq map__15190) (first map__15190) {}))
                         map__15190)
            spec map__15190
            wildcard (get map__15190 :wildcard)
            dbid (get map__15190 :dbid)]
        (when (.isHistory ^datomic.Database db)
          (throw (java.lang.IllegalStateException. "Can't pull from history")))
        (let [map__15191 (fix-specs-for-underscore-prefix-attrs spec db)
              map__15191 (if (seq? map__15191)
                           (if (next map__15191)
                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                               (to-array map__15191))
                             (if (seq map__15191) (first map__15191) {}))
                           map__15191)
              forward (get map__15191 :forward)
              reverse (get map__15191 :reverse)
              kw_>attr (partial resolve-attr db)
              mk_xf (fn mk_xf
                      ([path subspec def_subspec]
                        (let [vec__15193 (cond
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
                              _subspec (nth vec__15193 (int 0) nil)
                              _recursed (nth vec__15193 (int 1) nil)]
                          (if _subspec
                            (fn fn__15196 ([e] (pull* db _subspec _recursed prefer_aevt? e)))
                            identity))))
              eid (db/resolve-id db e)
              ret (transient (if dbid {(denormalize-kw :db/id dbid) eid} {}))
              ret (if wildcard
                    (iter/reduce
                      (fn fn__15202
                        ([ret a]
                          (let [kw (denormalize-kw (.ident ^datomic.Database db a) wildcard)
                                attr (db/attribute db a)
                                def_subspec (default-spec attr kw db)
                                map__15203 (get forward kw)
                                map__15203 (if (seq? map__15203)
                                             (if (next map__15203)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__15203))
                                               (if (seq map__15203) (first map__15203) {}))
                                             map__15203)
                                forward_args map__15203
                                valfn (get map__15203 :valfn)
                                keyfn (get map__15203 :keyfn)
                                subspec (get map__15203 :subspec)
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
                    (fn fn__15208
                      ([ret kw p__15207]
                        (let [map__15209 p__15207
                              map__15209 (if (seq? map__15209)
                                           (if (next map__15209)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__15209))
                                             (if (seq map__15209) (first map__15209) {}))
                                           map__15209)
                              limit (get map__15209 :limit)
                              valfn (get map__15209 :valfn)
                              keyfn (get map__15209 :keyfn)
                              subspec (get map__15209 :subspec)]
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
                    (fn fn__15213
                      ([ret kw p__15212]
                        (let [map__15214 p__15212
                              map__15214 (if (seq? map__15214)
                                           (if (next map__15214)
                                             (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                               (to-array map__15214))
                                             (if (seq map__15214) (first map__15214) {}))
                                           map__15214)
                              limit (get map__15214 :limit)
                              valfn (get map__15214 :valfn)
                              keyfn (get map__15214 :keyfn)
                              subspec (get map__15214 :subspec)
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
      (let [map__15219 (parse-index-pull-arg-map arg_map)
            map__15219 (if (seq? map__15219)
                         (if (next map__15219)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15219))
                           (if (seq map__15219) (first map__15219) {}))
                         map__15219)
            index (get map__15219 :index)
            reverse (get map__15219 :reverse)
            selector (get map__15219 :selector)
            start (get map__15219 :start)]
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
                                               (fn fn__15220
                                                 ([datom]
                                                   (delay
                                                     (pull*
                                                       db
                                                       cached_selector
                                                       #{}
                                                       true
                                                       (^clojure.lang.IFn pull_kw datom)))))
                                               (take-while
                                                 (fn fn__15224
                                                   ([p1__15218#]
                                                     (=
                                                       attrid
                                                       (long
                                                         (.getA
                                                           ^datomic.impl.db.IDatum p1__15218#)))))
                                                 (^clojure.lang.IFn seek_fn db index start)))
            :default (do
                       (error/arg
                         :db.error/invalid-pull
                         (str (name index) " is an invalid index, must be :avet or :aevt"))))))))
  (defn dereffed-index-pull ([db arg_map] (seq (map deref (index-pull db arg_map)))))
  (defn pull-1
    ([db selector e p__15234]
      (let [map__15235 p__15234
            map__15235 (if (seq? map__15235)
                         (if (next map__15235)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15235))
                           (if (seq map__15235) (first map__15235) {}))
                         map__15235)
            options map__15235
            io_context (get map__15235 :io-context)
            f (fn f ([] (pull* db (get normalized-pattern-cache selector) #{} false e)))]
        (if io_context
          (io-stats/throw-if-ex! (io-stats/with-io-stats f {:io-context io_context, :api :pull}))
          (^clojure.lang.IFn f))))
    ([db selector e] (pull-1 db selector e nil)))
  (defn pull
    ([db selector es p__15239]
      (let [map__15240 p__15239
            map__15240 (if (seq? map__15240)
                         (if (next map__15240)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15240))
                           (if (seq map__15240) (first map__15240) {}))
                         map__15240)
            options map__15240
            io_context (get map__15240 :io-context)
            f (fn f
                ([]
                  (mapv (partial pull* db (get normalized-pattern-cache selector) #{} true) es)))]
        (if io_context
          (io-stats/throw-if-ex!
            (io-stats/with-io-stats f {:io-context io_context, :api :pull-many}))
          (^clojure.lang.IFn f))))
    ([db selector es] (pull db selector es nil))))
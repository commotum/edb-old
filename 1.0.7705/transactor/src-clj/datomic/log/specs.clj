(do
  (clojure.core/in-ns 'datomic.log.specs)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.spec.alpha :as 's] ['datomic.log :as 'log])))
  (when-not (.equals 'datomic.log.specs 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.log.specs))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.spec.alpha :as 's] ['datomic.log :as 'log]))))
  (defn sorted-by-t? ([x] (= (map :t x) (sort (map :t x)))))
  (reset-meta!
    #'sorted-by-t?
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'sorted-by-t? :ns *ns*))
  (clojure.core/in-ns 'datomic.log)
  (alias 's 'clojure.spec.alpha)
  (s/def-impl
    :datomic.log/vecize
    (clojure.core/list
      'clojure.spec.alpha/and
      (clojure.core/list
        'fn*
        ['p1__21394#]
        (clojure.core/list 'clojure.core/instance? java.util.List 'p1__21394#))
      (clojure.core/list 'clojure.spec.alpha/conformer 'clojure.core/vec))
    (s/and-spec-impl
      [(clojure.core/list
         'clojure.core/fn
         ['%]
         (clojure.core/list 'clojure.core/instance? java.util.List '%))
       (clojure.core/list 'clojure.spec.alpha/conformer 'clojure.core/vec)]
      [(fn fn__21395 ([p1__21394#] (instance? java.util.List p1__21394#)))
       (s/spec-impl
         (clojure.core/list 'clojure.spec.alpha/conformer 'clojure.core/vec)
         vec
         nil
         true)]
      nil))
  (s/def-impl :datomic.log/t 'clojure.core/nat-int? nat-int?)
  (s/def-impl :datomic.log/uuid 'clojure.core/uuid? uuid?)
  (s/def-impl :datomic.log/id 'clojure.core/uuid? uuid?)
  (s/def-impl
    :datomic.log/datom
    (clojure.core/list
      'clojure.core/fn
      ['%]
      (clojure.core/list 'clojure.core/instance? datomic.impl.db.IDatum '%))
    (fn fn__21398 ([p1__21397#] (instance? datomic.impl.db.IDatum p1__21397#))))
  (s/def-impl
    :datomic.log/data
    (clojure.core/list
      'clojure.spec.alpha/and
      :datomic.log/vecize
      (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/datom))
    (s/and-spec-impl
      [:datomic.log/vecize (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/datom)]
      [:datomic.log/vecize
       (s/every-impl
         :datomic.log/datom
         :datomic.log/datom
         #:clojure.spec.alpha{:conform-all true,
                              :kind-form nil,
                              :describe
                              (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/datom),
                              :cpred (fn fn__21401 ([G__21400] (coll? G__21400)))}
         nil)]
      nil))
  (s/def-impl
    :datomic.log/node-entry
    (clojure.core/list 'clojure.spec.alpha/keys :req-un [:datomic.log/t :datomic.log/uuid])
    (s/map-spec-impl
      {:req-un [:datomic.log/t :datomic.log/uuid],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__21404 ([G__21403] (map? G__21403)))
        (fn fn__21406 ([G__21403] (contains? G__21403 :t)))
        (fn fn__21408 ([G__21403] (contains? G__21403 :uuid)))],
       :keys-pred
       (fn fn__21410
         ([G__21403] (and (map? G__21403) (contains? G__21403 :t) (contains? G__21403 :uuid)))),
       :opt-keys [],
       :req-specs [:datomic.log/t :datomic.log/uuid],
       :req nil,
       :req-keys [:t :uuid],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/contains? '% :t))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :uuid))],
       :opt nil}))
  (s/def-impl
    :datomic.log/node
    (clojure.core/list
      'clojure.spec.alpha/and
      :datomic.log/vecize
      (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/node-entry)
      'datomic.log.specs/sorted-by-t?)
    (s/and-spec-impl
      [:datomic.log/vecize
       (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/node-entry)
       'datomic.log.specs/sorted-by-t?]
      [:datomic.log/vecize
       (s/every-impl
         :datomic.log/node-entry
         :datomic.log/node-entry
         #:clojure.spec.alpha{:conform-all true,
                              :kind-form nil,
                              :describe
                              (clojure.core/list
                                'clojure.spec.alpha/coll-of
                                :datomic.log/node-entry),
                              :cpred (fn fn__21415 ([G__21414] (coll? G__21414)))}
         nil)
       datomic.log.specs/sorted-by-t?]
      nil))
  (s/def-impl :datomic.log/leaf-key 'clojure.core/string? string?)
  (s/def-impl
    :datomic.log/leaf-entry
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req-un
      [:datomic.log/t :datomic.log/id :datomic.log/data])
    (s/map-spec-impl
      {:req-un [:datomic.log/t :datomic.log/id :datomic.log/data],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__21418 ([G__21417] (map? G__21417)))
        (fn fn__21420 ([G__21417] (contains? G__21417 :t)))
        (fn fn__21422 ([G__21417] (contains? G__21417 :id)))
        (fn fn__21424 ([G__21417] (contains? G__21417 :data)))],
       :keys-pred
       (fn fn__21426
         ([G__21417]
           (and
             (map? G__21417)
             (contains? G__21417 :t)
             (contains? G__21417 :id)
             (contains? G__21417 :data)))),
       :opt-keys [],
       :req-specs [:datomic.log/t :datomic.log/id :datomic.log/data],
       :req nil,
       :req-keys [:t :id :data],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/contains? '% :t))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :id))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :data))],
       :opt nil}))
  (s/def-impl
    :datomic.log/leaf
    (clojure.core/list
      'clojure.spec.alpha/and
      :datomic.log/vecize
      (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/leaf-entry))
    (s/and-spec-impl
      [:datomic.log/vecize (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/leaf-entry)]
      [:datomic.log/vecize
       (s/every-impl
         :datomic.log/leaf-entry
         :datomic.log/leaf-entry
         #:clojure.spec.alpha{:conform-all true,
                              :kind-form nil,
                              :describe
                              (clojure.core/list
                                'clojure.spec.alpha/coll-of
                                :datomic.log/leaf-entry),
                              :cpred (fn fn__21432 ([G__21431] (coll? G__21431)))}
         nil)]
      nil))
  (s/def-impl
    :datomic.log/log
    (clojure.core/list
      'clojure.core/fn
      ['%]
      (clojure.core/list
        'clojure.core/extends?
        'datomic.log/LogSeek
        (clojure.core/list 'clojure.core/class '%)))
    (fn fn__21435 ([p1__21434#] (extends? LogSeek (class p1__21434#)))))
  (s/def-impl
    :datomic.log/log-impl
    (clojure.core/list
      'clojure.spec.alpha/and
      :datomic.log/log
      (clojure.core/list 'clojure.spec.alpha/keys :req-un [:datomic.log/desc]))
    (s/and-spec-impl
      [:datomic.log/log (clojure.core/list 'clojure.spec.alpha/keys :req-un [:datomic.log/desc])]
      [:datomic.log/log
       (s/map-spec-impl
         {:req-un [:datomic.log/desc],
          :opt-un nil,
          :gfn nil,
          :pred-exprs
          [(fn fn__21438 ([G__21437] (map? G__21437)))
           (fn fn__21440 ([G__21437] (contains? G__21437 :desc)))],
          :keys-pred (fn fn__21442 ([G__21437] (and (map? G__21437) (contains? G__21437 :desc)))),
          :opt-keys [],
          :req-specs [:datomic.log/desc],
          :req nil,
          :req-keys [:desc],
          :opt-specs [],
          :pred-forms
          [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
           (clojure.core/list
             'clojure.core/fn
             ['%]
             (clojure.core/list 'clojure.core/contains? '% :desc))],
          :opt nil})]
      nil))
  (s/def-impl
    :datomic.log/semantic-tx
    (clojure.core/list
      'clojure.spec.alpha/and
      (clojure.core/list 'clojure.spec.alpha/keys :req-un [:datomic.log/t :datomic.log/data])
      (clojure.core/list
        'fn*
        ['p1__21445#]
        (clojure.core/list 'clojure.core/set? (clojure.core/list :data 'p1__21445#))))
    (s/and-spec-impl
      [(clojure.core/list 'clojure.spec.alpha/keys :req-un [:datomic.log/t :datomic.log/data])
       (clojure.core/list
         'clojure.core/fn
         ['%]
         (clojure.core/list 'clojure.core/set? (clojure.core/list :data '%)))]
      [(s/map-spec-impl
         {:req-un [:datomic.log/t :datomic.log/data],
          :opt-un nil,
          :gfn nil,
          :pred-exprs
          [(fn fn__21447 ([G__21446] (map? G__21446)))
           (fn fn__21449 ([G__21446] (contains? G__21446 :t)))
           (fn fn__21451 ([G__21446] (contains? G__21446 :data)))],
          :keys-pred
          (fn fn__21453
            ([G__21446] (and (map? G__21446) (contains? G__21446 :t) (contains? G__21446 :data)))),
          :opt-keys [],
          :req-specs [:datomic.log/t :datomic.log/data],
          :req nil,
          :req-keys [:t :data],
          :opt-specs [],
          :pred-forms
          [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
           (clojure.core/list
             'clojure.core/fn
             ['%]
             (clojure.core/list 'clojure.core/contains? '% :t))
           (clojure.core/list
             'clojure.core/fn
             ['%]
             (clojure.core/list 'clojure.core/contains? '% :data))],
          :opt nil})
       (fn fn__21457 ([p1__21445#] (set? (:data p1__21445#))))]
      nil))
  (s/def-impl :datomic.log/root :datomic.log/node :datomic.log/node)
  (s/def-impl :datomic.log/dir :datomic.log/node :datomic.log/node)
  (s/def-impl :datomic.log/dir-idx 'clojure.core/nat-int? nat-int?)
  (s/def-impl :datomic.log/root-idx 'clojure.core/nat-int? nat-int?)
  (s/def-impl :datomic.log/root-id 'clojure.core/string? string?)
  (s/def-impl :datomic.log/dir-id 'clojure.core/string? string?)
  (s/def-impl
    :datomic.log/path
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req-un
      [:datomic.log/root-id
       :datomic.log/root
       :datomic.log/root-idx
       :datomic.log/dir-id
       :datomic.log/dir
       :datomic.log/dir-idx])
    (s/map-spec-impl
      {:req-un
       [:datomic.log/root-id
        :datomic.log/root
        :datomic.log/root-idx
        :datomic.log/dir-id
        :datomic.log/dir
        :datomic.log/dir-idx],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__21460 ([G__21459] (map? G__21459)))
        (fn fn__21462 ([G__21459] (contains? G__21459 :root-id)))
        (fn fn__21464 ([G__21459] (contains? G__21459 :root)))
        (fn fn__21466 ([G__21459] (contains? G__21459 :root-idx)))
        (fn fn__21468 ([G__21459] (contains? G__21459 :dir-id)))
        (fn fn__21470 ([G__21459] (contains? G__21459 :dir)))
        (fn fn__21472 ([G__21459] (contains? G__21459 :dir-idx)))],
       :keys-pred
       (fn fn__21474
         ([G__21459]
           (and
             (map? G__21459)
             (contains? G__21459 :root-id)
             (contains? G__21459 :root)
             (contains? G__21459 :root-idx)
             (contains? G__21459 :dir-id)
             (contains? G__21459 :dir)
             (contains? G__21459 :dir-idx)))),
       :opt-keys [],
       :req-specs
       [:datomic.log/root-id
        :datomic.log/root
        :datomic.log/root-idx
        :datomic.log/dir-id
        :datomic.log/dir
        :datomic.log/dir-idx],
       :req nil,
       :req-keys [:root-id :root :root-idx :dir-id :dir :dir-idx],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :root-id))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :root))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :root-idx))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :dir-id))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :dir))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :dir-idx))],
       :opt nil}))
  (s/def-impl :datomic.log/rev 'clojure.core/nat-int? nat-int?)
  (s/def-impl
    :datomic.log/etag
    (clojure.core/list 'clojure.spec.alpha/nilable 'clojure.core/string?)
    (s/nilable-impl 'clojure.core/string? string? nil))
  (s/def-impl :d/l 'clojure.core/pos-int? pos-int?)
  (s/def-impl :d/r 'clojure.core/string? string?)
  (s/def-impl
    :datomic.log/desc
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req-un
      [:datomic.log/rev :datomic.log/etag]
      :req
      [:d/l :d/r])
    (s/map-spec-impl
      {:req-un [:datomic.log/rev :datomic.log/etag],
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__21483 ([G__21482] (map? G__21482)))
        (fn fn__21485 ([G__21482] (contains? G__21482 :d/l)))
        (fn fn__21487 ([G__21482] (contains? G__21482 :d/r)))
        (fn fn__21489 ([G__21482] (contains? G__21482 :rev)))
        (fn fn__21491 ([G__21482] (contains? G__21482 :etag)))],
       :keys-pred
       (fn fn__21493
         ([G__21482]
           (and
             (map? G__21482)
             (contains? G__21482 :d/l)
             (contains? G__21482 :d/r)
             (contains? G__21482 :rev)
             (contains? G__21482 :etag)))),
       :opt-keys [],
       :req-specs [:d/l :d/r :datomic.log/rev :datomic.log/etag],
       :req [:d/l :d/r],
       :req-keys [:d/l :d/r :rev :etag],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :d/l))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :d/r))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :rev))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :etag))],
       :opt nil}))
  (s/def-impl
    'datomic.log/write-tail-descriptor
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list
        'clojure.spec.alpha/cat
        :cs
        'clojure.core/any?
        :desc
        :datomic.log/desc
        :buf
        'clojure.core/any?))
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/cat
          :cs
          'clojure.core/any?
          :desc
          :datomic.log/desc
          :buf
          'clojure.core/any?)
        (s/cat-impl
          [:cs :desc :buf]
          [any? :datomic.log/desc any?]
          ['clojure.core/any? :datomic.log/desc 'clojure.core/any?])
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/cat
        :cs
        'clojure.core/any?
        :desc
        :datomic.log/desc
        :buf
        'clojure.core/any?)
      (s/spec-impl 'clojure.core/any? any? nil nil)
      'clojure.core/any?
      nil
      nil
      nil)))
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
        ['p1__29947#]
        (clojure.core/list 'clojure.core/instance? java.util.List 'p1__29947#))
      (clojure.core/list 'clojure.spec.alpha/conformer 'clojure.core/vec))
    (s/and-spec-impl
      [(clojure.core/list
         'clojure.core/fn
         ['%]
         (clojure.core/list 'clojure.core/instance? java.util.List '%))
       (clojure.core/list 'clojure.spec.alpha/conformer 'clojure.core/vec)]
      [(fn fn__29948 ([p1__29947#] (instance? java.util.List p1__29947#)))
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
    (fn fn__29951 ([p1__29950#] (instance? datomic.impl.db.IDatum p1__29950#))))
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
         #:clojure.spec.alpha{:describe
                              (clojure.core/list 'clojure.spec.alpha/coll-of :datomic.log/datom),
                              :conform-all true,
                              :cpred (fn fn__29954 ([G__29953] (coll? G__29953))),
                              :kind-form nil}
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
       [(fn fn__29957 ([G__29956] (map? G__29956)))
        (fn fn__29959 ([G__29956] (contains? G__29956 :t)))
        (fn fn__29961 ([G__29956] (contains? G__29956 :uuid)))],
       :keys-pred
       (fn fn__29963
         ([G__29956] (and (map? G__29956) (contains? G__29956 :t) (contains? G__29956 :uuid)))),
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
         #:clojure.spec.alpha{:describe
                              (clojure.core/list
                                'clojure.spec.alpha/coll-of
                                :datomic.log/node-entry),
                              :conform-all true,
                              :cpred (fn fn__29968 ([G__29967] (coll? G__29967))),
                              :kind-form nil}
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
       [(fn fn__29971 ([G__29970] (map? G__29970)))
        (fn fn__29973 ([G__29970] (contains? G__29970 :t)))
        (fn fn__29975 ([G__29970] (contains? G__29970 :id)))
        (fn fn__29977 ([G__29970] (contains? G__29970 :data)))],
       :keys-pred
       (fn fn__29979
         ([G__29970]
           (and
             (map? G__29970)
             (contains? G__29970 :t)
             (contains? G__29970 :id)
             (contains? G__29970 :data)))),
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
         #:clojure.spec.alpha{:describe
                              (clojure.core/list
                                'clojure.spec.alpha/coll-of
                                :datomic.log/leaf-entry),
                              :conform-all true,
                              :cpred (fn fn__29985 ([G__29984] (coll? G__29984))),
                              :kind-form nil}
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
    (fn fn__29988 ([p1__29987#] (extends? LogSeek (class p1__29987#)))))
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
          [(fn fn__29991 ([G__29990] (map? G__29990)))
           (fn fn__29993 ([G__29990] (contains? G__29990 :desc)))],
          :keys-pred (fn fn__29995 ([G__29990] (and (map? G__29990) (contains? G__29990 :desc)))),
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
        ['p1__29998#]
        (clojure.core/list 'clojure.core/set? (clojure.core/list :data 'p1__29998#))))
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
          [(fn fn__30000 ([G__29999] (map? G__29999)))
           (fn fn__30002 ([G__29999] (contains? G__29999 :t)))
           (fn fn__30004 ([G__29999] (contains? G__29999 :data)))],
          :keys-pred
          (fn fn__30006
            ([G__29999] (and (map? G__29999) (contains? G__29999 :t) (contains? G__29999 :data)))),
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
       (fn fn__30010 ([p1__29998#] (set? (:data p1__29998#))))]
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
       [(fn fn__30013 ([G__30012] (map? G__30012)))
        (fn fn__30015 ([G__30012] (contains? G__30012 :root-id)))
        (fn fn__30017 ([G__30012] (contains? G__30012 :root)))
        (fn fn__30019 ([G__30012] (contains? G__30012 :root-idx)))
        (fn fn__30021 ([G__30012] (contains? G__30012 :dir-id)))
        (fn fn__30023 ([G__30012] (contains? G__30012 :dir)))
        (fn fn__30025 ([G__30012] (contains? G__30012 :dir-idx)))],
       :keys-pred
       (fn fn__30027
         ([G__30012]
           (and
             (map? G__30012)
             (contains? G__30012 :root-id)
             (contains? G__30012 :root)
             (contains? G__30012 :root-idx)
             (contains? G__30012 :dir-id)
             (contains? G__30012 :dir)
             (contains? G__30012 :dir-idx)))),
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
       [(fn fn__30036 ([G__30035] (map? G__30035)))
        (fn fn__30038 ([G__30035] (contains? G__30035 :d/l)))
        (fn fn__30040 ([G__30035] (contains? G__30035 :d/r)))
        (fn fn__30042 ([G__30035] (contains? G__30035 :rev)))
        (fn fn__30044 ([G__30035] (contains? G__30035 :etag)))],
       :keys-pred
       (fn fn__30046
         ([G__30035]
           (and
             (map? G__30035)
             (contains? G__30035 :d/l)
             (contains? G__30035 :d/r)
             (contains? G__30035 :rev)
             (contains? G__30035 :etag)))),
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
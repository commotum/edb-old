(do
  (clojure.core/in-ns 'datomic.core2.anomalizer)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.core2.anomalizer)
    {:doc
     "Converting throwables to anomalies.\n\nSee https://github.com/cognitect-labs/anomalies.\n\nThere are two main APIs: \n\nthrowable-category        returns an ::anom/category\nthrowable->anom           returns an anomaly map\n\nThe default return of throwable-category is ::anom/fault, which is the\ncatch-all category saying that we cannot categorize the throwable \nmore specifically.\n\nExtenders can improve the categorization of throwables in three ways:\n\n1. Add a class to the throwable-categories map. This is the \n   preferred approach for throwables that can be categorized by \n   type alone.\n2. Override ThrowableAnomCat. This is necessary if you need to look\n   at instance values of the throwable to decide what category is \n   appropriate.\n3. Add a class to the delegating-throwables map. This tells \n   throwable-category to base the category on the getCause of\n   this throwable, and is useful for skipping over 'wrapper' \n   exceptions.\n\nThe default return of throwable->anom is an anomaly map that \nincludes the throwable-category and the data that is common\nto all throwables, i.e. the stack trace, message, and cause chain.\n\nExtenders can improve throwable->anom of a throwable by \noverriding ThrowableAnomData and returning an arbitrary map.\n\nTo take advantage of particular extensions callers must load both \nthis namespace and the namespace providing the extensions, e.g.\n\n(require \n 'datomic.core2.anomalizer.aws.sdkv1     ;; for extensions\n '[datomic.core2.anomalizer :as izer)\n"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.spec.alpha :as 's] ['cognitect.anomalies :as 'anom])))
  (when-not (.equals 'datomic.core2.anomalizer 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.anomalizer))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.spec.alpha :as 's] ['cognitect.anomalies :as 'anom]))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol
      ThrowableAnomCat
      (-throwable-anom-category
        [_]
        "Impl helper for throwable-category. Returns a category by\nwalking the superclasses until a class matches an entry in the\nthrowable-categories map."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.anomalizer" "ThrowableAnomCat")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'ThrowableAnomCat :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-throwable-anom-category
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Impl helper for throwable-category. Returns a category by\nwalking the superclasses until a class matches an entry in the\nthrowable-categories map."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.core2.anomalizer"
                                       "ThrowableAnomCat"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.anomalizer" "-throwable-anom-category")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol
      ThrowableAnomData
      (-throwable->anom-data
        [_]
        "Impl helper for throwable->anom. Returns data to be added\nunder the :data key in the anomaly. Default impl returns nil."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.core2.anomalizer" "ThrowableAnomData")
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'ThrowableAnomData :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-throwable->anom-data
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Impl helper for throwable->anom. Returns data to be added\nunder the :data key in the anomaly. Default impl returns nil."}
                                     :protocol
                                     (clojure.lang.RT/var
                                       "datomic.core2.anomalizer"
                                       "ThrowableAnomData"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.core2.anomalizer" "-throwable->anom-data")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*))))
  (def anom-categories
   #{:cognitect.anomalies/unavailable :cognitect.anomalies/unsupported
     :cognitect.anomalies/interrupted :cognitect.anomalies/conflict :cognitect.anomalies/incorrect
     :cognitect.anomalies/not-found :cognitect.anomalies/busy :cognitect.anomalies/forbidden
     :cognitect.anomalies/fault})
  (reset-meta!
    #'anom-categories
    (assoc {:private true, :column (int 1)} :name 'anom-categories :ns *ns*))
  (s/def-impl
    :datomic.core2.anomalizer/throwable-categories
    (clojure.core/list
      'clojure.spec.alpha/map-of
      'clojure.core/symbol?
      'datomic.core2.anomalizer/anom-categories)
    (s/every-impl
      (clojure.core/list 'clojure.spec.alpha/tuple 'symbol? 'anom-categories)
      (s/tuple-impl
        ['clojure.core/symbol? 'datomic.core2.anomalizer/anom-categories]
        [symbol? anom-categories])
      {:clojure.spec.alpha/kfn
       (fn fn__20242 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         'clojure.core/symbol?
         'datomic.core2.anomalizer/anom-categories),
       :clojure.spec.alpha/cpred (fn fn__20244 ([G__20241] (map? G__20241)))}
      nil))
  (s/def-impl
    :datomic.core2.anomalizer/delegating-throwables
    (clojure.core/list
      'clojure.spec.alpha/and
      'clojure.core/set?
      (clojure.core/list 'clojure.spec.alpha/coll-of 'clojure.core/symbol?))
    (s/and-spec-impl
      ['clojure.core/set? (clojure.core/list 'clojure.spec.alpha/coll-of 'clojure.core/symbol?)]
      [set?
       (s/every-impl
         'symbol?
         symbol?
         #:clojure.spec.alpha{:conform-all true,
                              :kind-form nil,
                              :describe
                              (clojure.core/list
                                'clojure.spec.alpha/coll-of
                                'clojure.core/symbol?),
                              :cpred (fn fn__20247 ([G__20246] (coll? G__20246)))}
         nil)]
      nil))
  (defn spec-validator
    ([s]
      (fn fn__20249
        ([v]
          (let [temp__5823__auto__ (s/explain-data s v)]
            (when temp__5823__auto__
              (let [d temp__5823__auto__]
                (throw
                  (ex-info
                    "Data not to spec"
                    {:cognitect.anomalies/message "Data not to spec",
                     :cognitect.anomalies/category :cognitect.anomalies/incorrect,
                     :explain-data d}))))
            true)))))
  (reset-meta!
    #'spec-validator
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'spec-validator
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.core2.anomalizer" "throwable-categories")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.core2.anomalizer" "throwable-categories")
    (atom
      {'java.io.InterruptedIOException :cognitect.anomalies/interrupted,
       'java.io.FileNotFoundException :cognitect.anomalies/not-found,
       'java.lang.IllegalAccessException :cognitect.anomalies/forbidden,
       'java.lang.IllegalArgumentException :cognitect.anomalies/incorrect,
       'java.lang.InterruptedException :cognitect.anomalies/interrupted}
      :validator
      (spec-validator :datomic.core2.anomalizer/throwable-categories)))
  (def throwable-class-category
   (fn throwable_class_category
     ([tc]
       (let [cmap (deref throwable-categories)]
         (loop [c tc]
           (let [temp__5823__auto__ (^clojure.lang.IFn cmap
                                      (symbol (.getName ^java.lang.Class c)))]
             (if temp__5823__auto__
               (let [cat temp__5823__auto__] cat)
               (let [temp__5823__auto__ (.getSuperclass ^java.lang.Class c)]
                 (if temp__5823__auto__
                   (let [super temp__5823__auto__] (recur super))
                   :cognitect.anomalies/fault)))))))))
  (reset-meta!
    #'throwable-class-category
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'tc {:tag 'Class})]), :column (int 1)}
      :name
      'throwable-class-category
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.core2.anomalizer" "delegating-throwables")
    {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.core2.anomalizer" "delegating-throwables")
    (atom
      #{'java.util.concurrent.ExecutionException}
      :validator
      (spec-validator :datomic.core2.anomalizer/delegating-throwables)))
  (def category-delegate
   (fn category_delegate
     ([t]
       (let [dset (deref delegating-throwables)]
         (loop [c (.getClass t)]
           (if (^clojure.lang.IFn dset (symbol (.getName ^java.lang.Class c)))
             (category-delegate (.getCause ^java.lang.Throwable t))
             (let [temp__5823__auto__ (.getSuperclass ^java.lang.Class c)]
               (if temp__5823__auto__ (let [super temp__5823__auto__] (recur super)) t))))))))
  (reset-meta!
    #'category-delegate
    (assoc
      {:arglists (clojure.core/list [(.withMeta 't {:tag 'Throwable})]), :column (int 1)}
      :name
      'category-delegate
      :ns
      *ns*))
  (extend
    java.lang.Throwable
    ThrowableAnomCat
    {:-throwable-anom-category (fn fn__20258 ([t] (throwable-class-category (class t))))})
  (defn throwable-category ([t] (-throwable-anom-category (category-delegate t))))
  (reset-meta!
    #'throwable-category
    (assoc
      {:arglists (clojure.core/list ['t]), :column (int 1)}
      :name
      'throwable-category
      :ns
      *ns*))
  (extend java.lang.Throwable ThrowableAnomData {:-throwable->anom-data (fn fn__20261 ([t] nil))})
  (def throwable->anom
   (fn throwable__GT_anom
     ([t return_throwable?]
       (let [data (-throwable->anom-data t)
             ret (cond->
                   #:cognitect.anomalies{:message (.getMessage ^java.lang.Throwable t),
                                         :category (throwable-category t)}
                   data
                   (assoc :data data)
                   return_throwable?
                   (assoc :throwable (Throwable->map t)))
             temp__5823__auto__ (.getCause ^java.lang.Throwable t)]
         (if temp__5823__auto__
           (let [cause temp__5823__auto__] (assoc ret :cause (throwable->anom cause false)))
           ret)))
     ([t] (throwable->anom t true))))
  (reset-meta!
    #'throwable->anom
    (assoc
      {:arglists (clojure.core/list ['t] [(.withMeta 't {:tag 'Throwable}) 'return-throwable?]),
       :column (int 1)}
      :name
      'throwable->anom
      :ns
      *ns*)))
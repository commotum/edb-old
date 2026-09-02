(do
  (clojure.core/in-ns 'datomic.anomalizer)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.anomalizer)
    {:doc
     "Converting throwables to anomalies.\n\nSee https://github.com/cognitect-labs/anomalies.\n\nThere are two main APIs:\n\nthrowable-category        returns an ::anom/category\nthrowable->anom           returns an anomaly map\n\nThe default return of throwable-category is ::anom/fault, which is the\ncatch-all category saying that we cannot categorize the throwable\nmore specifically.\n\nExtenders can improve the categorization of throwables in three ways:\n\n1. Add a class to the throwable-categories map. This is the\n   preferred approach for throwables that can be categorized by\n   type alone.\n2. Override ThrowableAnomCat. This is necessary if you need to look\n   at instance values of the throwable to decide what category is\n   appropriate.\n3. Add a class to the delegating-throwables map. This tells\n   throwable-category to base the category on the getCause of\n   this throwable, and is useful for skipping over 'wrapper'\n   exceptions.\n\nThe default return of throwable->anom is an anomaly map that\nincludes the throwable-category and the data that is common\nto all throwables, i.e. the stack trace, message, and cause chain.\n\nExtenders can improve throwable->anom of a throwable by\noverriding ThrowableAnomData and returning an arbitrary map.\n\nTo take advantage of particular extensions callers must load both\nthis namespace and the namespace providing the extensions\n"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.spec.alpha :as 's] ['cognitect.anomalies :as 'anom])))
  (when-not (.equals 'datomic.anomalizer 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.anomalizer))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.spec.alpha :as 's] ['cognitect.anomalies :as 'anom]))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      ThrowableAnomCat
      (-throwable-anom-category
        [_]
        "Impl helper for throwable-category. Returns a category by\nwalking the superclasses until a class matches an entry in the\nthrowable-categories map."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.anomalizer" "ThrowableAnomCat")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'ThrowableAnomCat :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        '-throwable-anom-category
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Impl helper for throwable-category. Returns a category by\nwalking the superclasses until a class matches an entry in the\nthrowable-categories map."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.anomalizer" "ThrowableAnomCat"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.anomalizer" "-throwable-anom-category")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol
      ThrowableAnomData
      (-throwable->anom-data
        [_]
        "Impl helper for throwable->anom. Returns data to be added\nunder the :data key in the anomaly. Default impl returns nil."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.anomalizer" "ThrowableAnomData")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'ThrowableAnomData :ns *ns*))
    (let [protocol_signature__7467 (assoc
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
                                       "datomic.anomalizer"
                                       "ThrowableAnomData"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.anomalizer" "-throwable->anom-data")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*))))
  (def anom-categories
   #{:cognitect.anomalies/unavailable :cognitect.anomalies/unsupported
     :cognitect.anomalies/interrupted :cognitect.anomalies/conflict :cognitect.anomalies/incorrect
     :cognitect.anomalies/not-found :cognitect.anomalies/busy :cognitect.anomalies/forbidden
     :cognitect.anomalies/fault})
  (reset-meta!
    #'anom-categories
    (assoc {:private true, :column (int 1)} :name 'anom-categories :ns *ns*))
  (s/def-impl
    :datomic.anomalizer/throwable-categories
    (clojure.core/list
      'clojure.spec.alpha/map-of
      'clojure.core/symbol?
      'datomic.anomalizer/anom-categories)
    (s/every-impl
      (clojure.core/list 'clojure.spec.alpha/tuple 'symbol? 'anom-categories)
      (s/tuple-impl
        ['clojure.core/symbol? 'datomic.anomalizer/anom-categories]
        [symbol? anom-categories])
      {:clojure.spec.alpha/kfn
       (fn fn__3544 ([i__1934__auto__ v__1935__auto__] (nth v__1935__auto__ (int 0)))),
       :into {},
       :clojure.spec.alpha/conform-all true,
       :kind map?,
       :clojure.spec.alpha/kind-form 'clojure.core/map?,
       :clojure.spec.alpha/describe
       (clojure.core/list
         'clojure.spec.alpha/map-of
         'clojure.core/symbol?
         'datomic.anomalizer/anom-categories),
       :clojure.spec.alpha/cpred (fn fn__3546 ([G__3543] (map? G__3543)))}
      nil))
  (s/def-impl
    :datomic.anomalizer/delegating-throwables
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
                              :cpred (fn fn__3549 ([G__3548] (coll? G__3548)))}
         nil)]
      nil))
  (defn spec-validator
    ([s]
      (fn fn__3551
        ([v]
          (let [temp__5802__auto__ (s/explain-data s v)]
            (when temp__5802__auto__
              (let [d temp__5802__auto__]
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
    (clojure.lang.RT/var "datomic.anomalizer" "throwable-categories")
    {:doc
     "Validated atom mapping throwable class symbols to anomaly categories. Category lookup walks superclasses, so entries apply to subclasses unless a more specific mapping is present.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.anomalizer" "throwable-categories")
    (atom
      {'java.io.InterruptedIOException :cognitect.anomalies/interrupted,
       'java.io.FileNotFoundException :cognitect.anomalies/not-found,
       'java.lang.IllegalAccessException :cognitect.anomalies/forbidden,
       'java.lang.IllegalArgumentException :cognitect.anomalies/incorrect,
       'java.lang.InterruptedException :cognitect.anomalies/interrupted}
      :validator
      (spec-validator :datomic.anomalizer/throwable-categories)))
  (defn throwable-class-category
    ([throwable-class]
      (let [cmap (deref throwable-categories)]
        (loop [c throwable-class]
          (let [temp__5802__auto__ (^clojure.lang.IFn cmap (symbol (.getName ^java.lang.Class c)))]
            (if temp__5802__auto__
              (let [cat temp__5802__auto__] cat)
              (let [temp__5802__auto__ (.getSuperclass ^java.lang.Class c)]
                (if temp__5802__auto__
                  (let [super temp__5802__auto__] (recur super))
                  :cognitect.anomalies/fault))))))))
  (reset-meta!
    #'throwable-class-category
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'throwable-class {:tag 'Class})]),
       :doc
       "Returns the configured anomaly category for throwable-class or its nearest configured superclass. Unclassified throwables use :cognitect.anomalies/fault.",
       :column (int 1)}
      :name
      'throwable-class-category
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.anomalizer" "delegating-throwables")
    {:doc
     "Validated atom of wrapper-exception class symbols whose anomaly category is determined from their cause.",
     :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.anomalizer" "delegating-throwables")
    (atom
      #{'java.util.concurrent.ExecutionException}
      :validator
      (spec-validator :datomic.anomalizer/delegating-throwables)))
  (defn category-delegate
    ([throwable]
      (let [dset (deref delegating-throwables)]
        (loop [c (.getClass throwable)]
          (if (^clojure.lang.IFn dset (symbol (.getName ^java.lang.Class c)))
            (category-delegate (.getCause ^java.lang.Throwable throwable))
            (let [temp__5802__auto__ (.getSuperclass ^java.lang.Class c)]
              (if temp__5802__auto__
                (let [super temp__5802__auto__] (recur super))
                throwable)))))))
  (reset-meta!
    #'category-delegate
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'throwable {:tag 'Throwable})]),
       :doc "Unwraps configured wrapper exceptions until reaching the throwable that determines category.",
       :column (int 1)}
      :name
      'category-delegate
      :ns
      *ns*))
  (extend
    java.lang.Throwable
    ThrowableAnomCat
    {:-throwable-anom-category (fn fn__3560 ([t] (throwable-class-category (class t))))})
  (defn throwable-category
    ([throwable] (-throwable-anom-category (category-delegate throwable))))
  (reset-meta!
    #'throwable-category
    (assoc
      {:arglists (clojure.core/list ['throwable]),
       :doc
       "Returns the Cognitect anomaly category for throwable after applying wrapper delegation and extensible category dispatch.",
       :column (int 1)}
      :name
      'throwable-category
      :ns
      *ns*))
  (extend java.lang.Throwable ThrowableAnomData {:-throwable->anom-data (fn fn__3563 ([t] nil))})
  (defn throwable->anom
    ([t return-throwable?]
      (let [data (-throwable->anom-data t)
            ret (cond->
                  #:cognitect.anomalies{:message (.getMessage ^java.lang.Throwable t),
                                        :category (throwable-category t)}
                  data
                  (assoc :data data)
                  return-throwable?
                  (assoc :throwable (Throwable->map t)))
            temp__5802__auto__ (.getCause ^java.lang.Throwable t)]
        (if temp__5802__auto__
          (let [cause temp__5802__auto__] (assoc ret :cause (throwable->anom cause false)))
          ret)))
    ([t] (throwable->anom t true)))
  (reset-meta!
    #'throwable->anom
    (assoc
      {:arglists (clojure.core/list ['t] [(.withMeta 't {:tag 'Throwable}) 'return-throwable?]),
       :doc
       "Converts a throwable into an anomaly map containing category and message, optional extension data, and a recursively converted cause. The one-argument form also includes Throwable->map output under :throwable; nested causes omit it.",
       :column (int 1)}
      :name
      'throwable->anom
      :ns
      *ns*)))

(do
  (clojure.core/in-ns 'datomic.common)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.common)
    {:doc
     "Shared runtime primitives used across Datomic components. Defines the total ordering used for indexed values and provides lifecycle, retry, scheduling, configuration, identifier, bounded-wait, and result-pagination utilities."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['compare 'qualified-symbol?])
      (clojure.core/use 'clojure.pprint)
      (clojure.core/require
        ['clojure.java.io :as 'io]
        ['datomic.core2.thread :as 'thread]
        ['datomic.slf4j :as 'logger]
        ['datomic.monitor :as 'monitor]
        ['clojure.string :as 'str]
        ['clojure.set :as 'set])
      (clojure.core/import 'java.io.IOException)
      (clojure.core/import 'java.io.InterruptedIOException)
      (clojure.core/import 'java.util.concurrent.ExecutorService)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.Properties)
      (clojure.core/import 'java.util.List)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.util.Set)
      (clojure.core/import 'java.util.Collection)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'java.util.Collections)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.Map$Entry)))
  (when-not (.equals 'datomic.common 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.common))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['compare 'qualified-symbol?])
        (clojure.core/use 'clojure.pprint)
        (clojure.core/require
          ['clojure.java.io :as 'io]
          ['datomic.core2.thread :as 'thread]
          ['datomic.slf4j :as 'logger]
          ['datomic.monitor :as 'monitor]
          ['clojure.string :as 'str]
          ['clojure.set :as 'set])
        (clojure.core/import 'java.io.IOException)
        (clojure.core/import 'java.io.InterruptedIOException)
        (clojure.core/import 'java.util.concurrent.ExecutorService)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.Properties)
        (clojure.core/import 'java.util.List)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.util.Set)
        (clojure.core/import 'java.util.Collection)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'java.util.Collections)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.Map$Entry))))
  (set! *warn-on-reflection* true)
  (set! *unchecked-math* true)
  (def DEFAULT_SYSTEM_NAME "_default")
  (reset-meta!
    #'DEFAULT_SYSTEM_NAME
    (assoc {:const true, :column (int 1)} :name 'DEFAULT_SYSTEM_NAME :ns *ns*))
  ;; ATOMIC-NOTE BEGIN foundation-order (baseline cd7192e63d883a4a34aa7de4d5bcd17e6edb692d)
  ;; Observed: db/Datum.equals and the eavt-cmp/avet-cmp consumers use this
  ;; namespace's value comparison. Byte arrays need content comparison because
  ;; JVM array equals is identity-based; the actual order is length first, then
  ;; signed bytes, not unsigned Rust slice order. Rust model/value::index_cmp
  ;; preserves that rule. These are shared value semantics, not cache policy.
  ;; The peer copy's comparison section through cl is identical at the baseline;
  ;; the complete files differ, so this note does not claim whole-file identity.
  ;; ATOMIC-NOTE END foundation-order
  (defn compare-byte-arrays
    (^long [a b]
      (let [a a b b len (alength ^bytes a) lencomp (- len (alength ^bytes b))]
        (if (= lencomp 0)
          (loop [pos 0]
            (if (= pos len)
              0
              (let [c (-
                        (unchecked-long (java.lang.Byte/valueOf (byte (aget ^bytes a (int pos)))))
                        (unchecked-long
                          (java.lang.Byte/valueOf (byte (aget ^bytes b (int pos))))))]
                (if (= c 0) (recur (inc pos)) c))))
          lencomp))))
  (reset-meta!
    #'compare-byte-arrays
    (assoc
      {:arglists (clojure.core/list (.withMeta ['a 'b] {:tag 'long})),
       :doc
       "Compares byte arrays for index ordering. Arrays are ordered first by length and then lexicographically by signed byte value. Returns a negative number, zero, or a positive number. Java byte-array equality is identity based; query predicates that need content equality use java.util.Arrays/equals.",
       :column (int 1)}
      :name
      'compare-byte-arrays
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.common" "BYTES") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.common" "BYTES") (java.lang.Class/forName "[B"))
  (.setMeta
    (clojure.lang.RT/var "datomic.common" "coll-compare")
    {:private true, :declared true, :column (int 1)})
  ;; ATOMIC-NOTE BEGIN foundation-runtime-values
  ;; Observed: same-class values use equals/Comparable, while unrelated classes
  ;; fall back to class-name order. In particular java.net.URI is not compared as
  ;; its printed string: its component equality ignores scheme/host case and
  ;; escaped-hex case. Atomic's original Value::Uri raw-string comparison/hash
  ;; differed for unique keys; Stage 2 model/uri now separates component meaning
  ;; from exact retained spelling. Native authority-kind ranking additionally
  ;; preserves transitive ordering (the Java mixed raw fallback does not). See
  ;; the chapter-local
  ;; datomic_pro_docs/03_schema/03_identity_and_uniqueness.atomic.md trace.
  ;; Inference: a native explicit cross-type rank can replace class packaging,
  ;; but comparisons within a supported type still require a contract decision.
  ;; ATOMIC-NOTE END foundation-runtime-values
  (defn compare-ex
    (^long [a b]
      (.longValue
        (cond
          (.equals a b) 0
          (or
            (instance? java.util.List a)
            (and (instance? java.util.Map a) (not (instance? clojure.lang.IRecord a)))
            (instance? java.util.Set a)) (long (coll-compare a b))
          (identical? (.getClass a) (.getClass b)) (if (instance? BYTES a)
                                                     (long (compare-byte-arrays a b))
                                                     (java.lang.Integer/valueOf
                                                       (int
                                                         (.compareTo ^java.lang.Comparable a b))))
          (or (instance? java.util.Collection b) (instance? java.util.Map b)) 1
          :else (do
                  (java.lang.Integer/valueOf
                    (int (.compareTo (.getName (.getClass a)) (.getName (.getClass b))))))))))
  (reset-meta!
    #'compare-ex
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'a {:tag 'Object}) (.withMeta 'b {:tag 'Object})] {:tag 'long})),
       :doc
       "Compares non-nil, non-numeric values in the Datomic total order. Lists compare positionally. Maps and sets compare by element count and then collection hash, with sorted entries or elements breaking equal-hash ties. Byte arrays use byte content; equal runtime classes use Comparable; remaining values are ordered by class name.",
       :column (int 1)}
      :name
      'compare-ex
      :ns
      *ns*))
  (defn compare
    (^long [a b]
      (.longValue
        (cond
          (identical? a b) 0
          (nil? a) (if (nil? b) 0 -1)
          (nil? b) 1
          (instance? java.lang.Number a) (if (instance? java.lang.Number b)
                                           (java.lang.Integer/valueOf
                                             (int
                                               (clojure.lang.Numbers/compare
                                                 ^java.lang.Number a
                                                 ^java.lang.Number b)))
                                           -1)
          (and (instance? java.lang.String a) (instance? java.lang.String b)) (java.lang.Integer/valueOf
                                                                                (int
                                                                                  (.compareTo
                                                                                    ^java.lang.Comparable a
                                                                                    b)))
          (instance? java.lang.Number b) 1
          :else (do (long (compare-ex a b)))))))
  (reset-meta!
    #'compare
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'a {:tag 'Object}) (.withMeta 'b {:tag 'Object})] {:tag 'long})),
       :doc
       "Compares two values using Datomic's stable total ordering. Nil sorts first, numbers sort before non-numbers and compare across numeric types, strings use lexical order, and other supported values follow compare-ex. Returns a negative number, zero, or a positive number.",
       :column (int 1)}
      :name
      'compare
      :ns
      *ns*))
  ;; ATOMIC-NOTE BEGIN foundation-storage-equality
  ;; Observed: db/Accrual plus prefetch redundancy checks call this predicate,
  ;; whereas Datum/index comparison above uses compare. Only two top-level
  ;; BigDecimals add the scale check; lists recurse through logical comparison.
  ;; Rust Value::stored_eq versus index_cmp preserves this separation. Do not
  ;; collapse the predicates merely because both look like equality: doing so
  ;; would erase an explicitly asserted decimal representation or change keys.
  ;; ATOMIC-NOTE END foundation-storage-equality
  (defn equals-with-strict-scale
    ([a b]
      (and
        (zero? (compare a b))
        (if (and (instance? java.math.BigDecimal a) (instance? java.math.BigDecimal b))
          (= (long (.scale ^java.math.BigDecimal a)) (long (.scale ^java.math.BigDecimal b)))
          true))))
  (reset-meta!
    #'equals-with-strict-scale
    (assoc
      {:arglists (clojure.core/list ['a 'b]),
       :doc
       "Returns true when values compare equal, with BigDecimal values additionally required to have the same scale. BigDecimal scale is treated as part of value equality.",
       :column (int 1)}
      :name
      'equals-with-strict-scale
      :ns
      *ns*))
  (defn cl
    (^long [a b]
      (.longValue
        (loop [as (.iterator ^java.util.List a) bs (.iterator ^java.util.List b)]
          (let [ha (.hasNext ^java.util.Iterator as) hb (.hasNext ^java.util.Iterator bs)]
            (cond
              (and ha hb) (let [c (compare
                                    (.next ^java.util.Iterator as)
                                    (.next ^java.util.Iterator bs))]
                            (if (= c 0) (recur as bs) (long c)))
              ha 1
              hb -1
              :else (do 0)))))))
  (reset-meta!
    #'cl
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'a {:tag 'List}) (.withMeta 'b {:tag 'List})] {:tag 'long})),
       :column (int 1)}
      :name
      'cl
      :ns
      *ns*))
  (defn cx
    (^long [a b]
      (let [cmp (reify
                  java.util.Comparator
                  (^int compare
                    [this a b]
                    (int
                      (if (and (instance? java.util.Map$Entry a) (instance? java.util.Map$Entry b))
                        (let [kc (compare (key a) (key b))]
                          (if (= kc 0) (compare (val a) (val b)) kc))
                        (compare a b)))))
            alist (fn alist
                    ([x]
                      (let [xs (seq x) G__8648 (java.util.ArrayList. ^java.util.Collection xs)]
                        (Collections/sort ^java.util.List G__8648 ^java.util.Comparator cmp)
                        G__8648)))
            as (^clojure.lang.IFn alist a)
            bs (^clojure.lang.IFn alist b)]
        (cl as bs))))
  (reset-meta!
    #'cx
    (assoc
      {:private true,
       :arglists (clojure.core/list (.withMeta ['a 'b] {:tag 'long})),
       :column (int 1)}
      :name
      'cx
      :ns
      *ns*))
  (defn cc
    (^long [a b]
      (.longValue
        (let [ca (count a) cb (count b)]
          (cond
            (< ca cb) -1
            (> ca cb) 1
            :else (do
                    (let [ha (.hashCode a) hb (.hashCode b)]
                      (if (= ha hb) (long (cx a b)) (long (- ha hb))))))))))
  (reset-meta!
    #'cc
    (assoc
      {:private true,
       :arglists (clojure.core/list (.withMeta ['a 'b] {:tag 'long})),
       :column (int 1)}
      :name
      'cc
      :ns
      *ns*))
  (defn coll-compare
    (^long [a b]
      (.longValue
        (cond
          (instance? java.util.List a) (if (instance? java.util.List b) (long (cl a b)) -1)
          (instance? java.util.List b) 1
          (instance? java.util.Map a) (if (instance? java.util.Map b) (long (cc a b)) -1)
          (instance? java.util.Map b) 1
          (instance? java.util.Set a) (do (if (instance? java.util.Set b) (long (cc a b)) -1))))))
  (reset-meta!
    #'coll-compare
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'a {:tag 'Object}) (.withMeta 'b {:tag 'Object})] {:tag 'long})),
       :column (int 1)}
      :name
      'coll-compare
      :ns
      *ns*))
  (defn <' ([a b] (neg? (compare a b))))
  (reset-meta!
    #'<'
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '<' :ns *ns*))
  (defn >' ([a b] (not (<' a b))))
  (reset-meta!
    #'>'
    (assoc {:arglists (clojure.core/list ['a 'b]), :column (int 1)} :name '>' :ns *ns*))
  (defn split-filter ([pred coll] [(filter pred coll) (remove pred coll)]))
  (reset-meta!
    #'split-filter
    (assoc
      {:arglists (clojure.core/list ['pred 'coll]), :column (int 1)}
      :name
      'split-filter
      :ns
      *ns*))
  (defn getx
    ([m k]
      (let [e (get m k :datomic.common/getx-sentinel-42)]
        (if (not (= e :datomic.common/getx-sentinel-42))
          e
          (do (throw (java.lang.Exception. (str "Key not found: " k))) nil)))))
  (reset-meta!
    #'getx
    (assoc
      {:arglists (clojure.core/list ['m 'k]),
       :doc "Returns the value for k, including nil, and throws when m does not contain k.",
       :column (int 1)}
      :name
      'getx
      :ns
      *ns*))
  (defn getx-in ([m ks] (reduce getx m ks)))
  (reset-meta!
    #'getx-in
    (assoc {:arglists (clojure.core/list ['m 'ks]), :column (int 1)} :name 'getx-in :ns *ns*))
  (defn require-keys
    ([m keyseq]
      (let [result (select-keys m keyseq)]
        (if (= (count result) (count keyseq))
          result
          (do
            (throw
              (ex-info
                "Missing keys"
                {:missing (set/difference (into #{} keyseq) (into #{} (keys result)))}))
            nil)))))
  (reset-meta!
    #'require-keys
    (assoc
      {:arglists (clojure.core/list ['m 'keyseq]),
       :doc
       "Returns a map containing the requested keys. Throws ExceptionInfo with :missing set to every absent key when the input map is incomplete.",
       :column (int 1)}
      :name
      'require-keys
      :ns
      *ns*))
  (defn env-key->clj-key ([k] (keyword (str/replace (str/lower-case k) "_" "-"))))
  (reset-meta!
    #'env-key->clj-key
    (assoc {:arglists (clojure.core/list ['k]), :column (int 1)} :name 'env-key->clj-key :ns *ns*))
  (defn clj-key->env-key ([s] (str/upper-case (str/replace (name s) "-" "_"))))
  (reset-meta!
    #'clj-key->env-key
    (assoc {:arglists (clojure.core/list ['s]), :column (int 1)} :name 'clj-key->env-key :ns *ns*))
  (defn map->env
    ([m]
      (reduce
        (fn fn__8662
          ([m p__8661]
            (let [vec__8663 p__8661
                  k (nth vec__8663 (unchecked-int 0) nil)
                  v (nth vec__8663 (unchecked-int 1) nil)]
              (assoc m (clj-key->env-key k) v))))
        {}
        m)))
  (reset-meta!
    #'map->env
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'map->env :ns *ns*))
  (defn map-from-env
    ([env]
      (reduce
        (fn fn__8668
          ([m k]
            (let [temp__5823__auto__ (get env k)]
              (if temp__5823__auto__
                (let [v temp__5823__auto__] (assoc m (env-key->clj-key k) v))
                m))))
        {}
        (keys env))))
  (reset-meta!
    #'map-from-env
    (assoc {:arglists (clojure.core/list ['env]), :column (int 1)} :name 'map-from-env :ns *ns*))
  (defn coerce-args
    ([m & predfns]
      (let [predfns (partition 2 predfns)]
        (reduce
          (fn fn__8673
            ([m p__8672]
              (let [vec__8674 p__8672
                    k (nth vec__8674 (unchecked-int 0) nil)
                    v (nth vec__8674 (unchecked-int 1) nil)
                    temp__5823__auto__ (some
                                         (fn fn__8678
                                           ([p__8677]
                                             (let [vec__8679 p__8677
                                                   p (nth vec__8679 (unchecked-int 0) nil)
                                                   f (nth vec__8679 (unchecked-int 1) nil)]
                                               (when (^clojure.lang.IFn p k) f))))
                                         predfns)]
                (if temp__5823__auto__
                  (let [f temp__5823__auto__] (assoc m k (^clojure.lang.IFn f v)))
                  (assoc m k v)))))
          {}
          m))))
  (reset-meta!
    #'coerce-args
    (assoc
      {:arglists (clojure.core/list ['m '& 'predfns]), :column (int 1)}
      :name
      'coerce-args
      :ns
      *ns*))
  (def assert-all
   (fn assert_all
     ([&form &env & args]
       (seq
         (concat
           (clojure.core/list 'do)
           (map
             (fn fn__8686
               ([a] (seq (concat (clojure.core/list 'clojure.core/assert) (clojure.core/list a)))))
             args))))))
  (reset-meta!
    #'assert-all
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name 'assert-all :ns *ns*))
  (.setMacro #'assert-all)
  (defn log-and-print
    ([& xs]
      (let [s (apply print-str xs)]
        (println s)
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process s)))
          nil))))
  (reset-meta!
    #'log-and-print
    (assoc
      {:arglists (clojure.core/list ['& 'xs]), :column (int 1)}
      :name
      'log-and-print
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      AsyncShutdown
      (async-shutdown
        [o]
        "Returns an IBlockingDeref that will deref to some true value on successful\n    shutdown, or to a Throwable if shutdown is known to have failed."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.common" "AsyncShutdown")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'AsyncShutdown :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'async-shutdown
                                        {:arglists (clojure.core/list ['o])}),
                                      :arglists (clojure.core/list ['o]),
                                      :doc
                                      "Returns an IBlockingDeref that will deref to some true value on successful\n    shutdown, or to a Throwable if shutdown is known to have failed."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.common" "AsyncShutdown"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.common" "async-shutdown")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (defn sync-shutdown ([x] (deref (async-shutdown x))))
  (reset-meta!
    #'sync-shutdown
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'sync-shutdown :ns *ns*))
  (def with-shutdown
   (fn with_shutdown
     ([&form &env bindings & body]
       (if (= (count bindings) 0)
         (seq (concat (clojure.core/list 'do) body))
         (if (symbol? (^clojure.lang.IFn bindings 0))
           (seq
             (concat
               (clojure.core/list 'clojure.core/let)
               (clojure.core/list (subvec bindings 0 2))
               (clojure.core/list
                 (seq
                   (concat
                     (clojure.core/list 'try)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'datomic.common/with-shutdown)
                           (clojure.core/list (subvec bindings 2))
                           body)))
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'finally)
                           (clojure.core/list
                             (seq
                               (concat
                                 (clojure.core/list 'clojure.core/deref)
                                 (clojure.core/list
                                   (seq
                                     (concat
                                       (clojure.core/list 'datomic.common/async-shutdown)
                                       (clojure.core/list
                                         (^clojure.lang.IFn bindings 0))))))))))))))))
           (do
             (when :else
               (throw
                 (java.lang.IllegalArgumentException.
                   "with-shutdown only allows Symbols in bindings")))
             nil))))))
  (reset-meta!
    #'with-shutdown
    (assoc
      {:arglists (clojure.core/list ['bindings '& 'body]), :column (int 1)}
      :name
      'with-shutdown
      :ns
      *ns*))
  (.setMacro #'with-shutdown)
  (defn await-derefs
    ([msec coll]
      (let [timed_out (java.lang.Object.)
            limit (+ msec (java.lang.System/currentTimeMillis))
            G__8715 coll
            vec__8716 G__8715
            seq__8717 (seq vec__8716)
            first__8718 (first seq__8717)
            seq__8717 (next seq__8717)
            item first__8718
            more seq__8717]
        (loop [G__8715 G__8715]
          (let [vec__8719 G__8715
                seq__8720 (seq vec__8719)
                first__8721 (first seq__8720)
                seq__8720 (next seq__8720)
                item first__8721
                more seq__8720]
            (if item
              (if (=
                    timed_out
                    (deref item (- limit (java.lang.System/currentTimeMillis)) timed_out))
                false
                (recur more))
              true)))))
    ([coll]
      (do
        (loop [seq_8708 (seq coll) chunk_8709 nil count_8710 0 i_8711 0]
          (if (< i_8711 count_8710)
            (let [c (.nth ^clojure.lang.Indexed chunk_8709 (unchecked-int i_8711))]
              (deref c)
              (recur seq_8708 chunk_8709 count_8710 (inc i_8711)))
            (let [temp__5825__auto__ (seq seq_8708)]
              (when temp__5825__auto__
                (let [seq_8708 temp__5825__auto__]
                  (if (chunked-seq? seq_8708)
                    (let [c__6090__auto__ (chunk-first seq_8708)]
                      (recur (chunk-rest seq_8708) c__6090__auto__ (count c__6090__auto__) 0))
                    (let [c (first seq_8708)] (deref c) (recur (next seq_8708) nil 0 0))))))))
        true)))
  (reset-meta!
    #'await-derefs
    (assoc
      {:arglists (clojure.core/list ['coll] ['msec 'coll]),
       :doc
       "Waits for every dereferenceable value in coll. The timed arity shares one millisecond deadline across the collection and returns false on timeout; the untimed arity waits indefinitely and returns true.",
       :column (int 1)}
      :name
      'await-derefs
      :ns
      *ns*))
  (defn find-free-port
    ([]
      (with-open [s (java.net.ServerSocket. (unchecked-int 0))]
        (java.lang.Integer/valueOf (int (.getLocalPort ^java.net.ServerSocket s))))))
  (reset-meta!
    #'find-free-port
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'find-free-port :ns *ns*))
  (defn array-cat
    ([& p__8726]
      (let [vec__8727 p__8726 a (nth vec__8727 (unchecked-int 0) nil) as vec__8727]
        (when a
          (let [length (apply + (map count as))
                type (.getComponentType (class a))
                result (java.lang.reflect.Array/newInstance
                         ^java.lang.Class type
                         (unchecked-int length))]
            (let [i 0
                  G__8733 as
                  vec__8734 G__8733
                  seq__8735 (seq vec__8734)
                  first__8736 (first seq__8735)
                  seq__8735 (next seq__8735)
                  a first__8736
                  more seq__8735]
              (loop [i i G__8733 G__8733]
                (let [i i
                      vec__8737 G__8733
                      seq__8738 (seq vec__8737)
                      first__8739 (first seq__8738)
                      seq__8738 (next seq__8738)
                      a first__8739
                      more seq__8738]
                  (when a
                    (java.lang.System/arraycopy
                      a
                      (unchecked-int 0)
                      result
                      (unchecked-int i)
                      (int (count a)))
                    (recur (+ i (count a)) more)))))
            result)))))
  (reset-meta!
    #'array-cat
    (assoc
      {:arglists (clojure.core/list ['& ['a :as 'as]]), :column (int 1)}
      :name
      'array-cat
      :ns
      *ns*))
  (defn squuid
    ([]
      (let [uuid (java.util.UUID/randomUUID)
            time (java.lang.System/currentTimeMillis)
            secs (quot time 1000)
            lsb (.getLeastSignificantBits ^java.util.UUID uuid)
            msb (.getMostSignificantBits ^java.util.UUID uuid)]
        (java.util.UUID.
          (long (bit-or (bit-shift-left secs 32) (bit-and 4294967295 msb)))
          (long lsb)))))
  (reset-meta!
    #'squuid
    (assoc
      {:arglists (clojure.core/list []),
       :doc
       "Returns a semi-sequential UUID whose high 32 bits contain the current Unix time in seconds. Temporal locality reduces index scatter for UUID-valued attributes.",
       :column (int 1)}
      :name
      'squuid
      :ns
      *ns*))
  (defn squuid-time-ms
    ([squuid]
      (long
        (*
          1000
          (bit-and
            4294967295
            (bit-shift-right (.getMostSignificantBits ^java.util.UUID squuid) 32))))))
  (reset-meta!
    #'squuid-time-ms
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'squuid {:tag 'java.util.UUID})]),
       :doc "Returns the embedded creation time of a squuid in Unix epoch milliseconds.",
       :column (int 1)}
      :name
      'squuid-time-ms
      :ns
      *ns*))
  (defn rand-uuid ([] (squuid)))
  (reset-meta!
    #'rand-uuid
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'rand-uuid :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.common" "run-uuid") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.common" "run-uuid") (rand-uuid))
  (defn root-cause
    ([x] (when x (let [cause (.getCause ^java.lang.Throwable x)] (if cause (recur cause) x)))))
  (reset-meta!
    #'root-cause
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'x {:tag 'Throwable})]), :column (int 1)}
      :name
      'root-cause
      :ns
      *ns*))
  (defn qualified-symbol? ([x] (boolean (and (symbol? x) (namespace x) true))))
  (reset-meta!
    #'qualified-symbol?
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'qualified-symbol?
      :ns
      *ns*))
  (defn requiring-resolve!
    ([x]
      (if (symbol? x)
        (or
          (resolve x)
          (let [temp__5825__auto__ (namespace x)]
            (when temp__5825__auto__
              (let [nsname temp__5825__auto__]
                (clojure.core/require (symbol nsname))
                (resolve x))))
          (do
            (let [msg (str "Can't resolve symbol: " x)]
              (throw
                (ex-info
                  msg
                  #:cognitect.anomalies{:category :cognitect.anomalies/not-found, :message msg})))
            nil))
        x)))
  (reset-meta!
    #'requiring-resolve!
    (assoc
      {:arglists (clojure.core/list ['x]), :column (int 1)}
      :name
      'requiring-resolve!
      :ns
      *ns*))
  (defn maybe-class
    ([cls]
      (try
        (clojure.lang.RT/classForNameNonLoading ^java.lang.String cls)
        (catch java.lang.ClassNotFoundException _ nil))))
  (reset-meta!
    #'maybe-class
    (assoc {:arglists (clojure.core/list ['cls]), :column (int 1)} :name 'maybe-class :ns *ns*))
  (defn maybe-require
    ([ns s]
      (let [temp__5825__auto__ (and (qualified-symbol? s) (namespace s))]
        (when temp__5825__auto__
          (let [sns temp__5825__auto__]
            (let [or__5602__auto__ (ns-resolve ns s)]
              (when-not or__5602__auto__
                (let [or__5602__auto__ (contains? (ns-imports ns) (symbol sns))]
                  (when-not or__5602__auto__
                    (let [or__5602__auto__ (maybe-class sns)]
                      (when-not or__5602__auto__ (clojure.core/require (symbol sns))))))))
            nil)))))
  (reset-meta!
    #'maybe-require
    (assoc
      {:arglists (clojure.core/list ['ns 's]), :column (int 1)}
      :name
      'maybe-require
      :ns
      *ns*))
  (defn throw-anom ([anom] (throw (ex-info (:cognitect.anomalies/message anom) anom))))
  (reset-meta!
    #'throw-anom
    (assoc {:arglists (clojure.core/list ['anom]), :column (int 1)} :name 'throw-anom :ns *ns*))
  (defn key-comparator
    ([key_fn comp]
      (reify
        java.util.Comparator
        (^int compare
          [this o1 o2]
          (.compare
            ^java.util.Comparator comp
            (^clojure.lang.IFn key_fn o1)
            (^clojure.lang.IFn key_fn o2)))))
    ([key_fn]
      (reify
        java.util.Comparator
        (^int compare
          [this o1 o2]
          (.compareTo (^clojure.lang.IFn key_fn o1) (^clojure.lang.IFn key_fn o2))))))
  (reset-meta!
    #'key-comparator
    (assoc
      {:arglists (clojure.core/list ['key-fn] ['key-fn (.withMeta 'comp {:tag 'Comparator})]),
       :column (int 1)}
      :name
      'key-comparator
      :ns
      *ns*))
  (def fire
   (fn fire
     ([&form &env & body]
       (seq
         (concat
           (clojure.core/list 'clojure.core/future)
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'try)
                 body
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'catch)
                       (clojure.core/list 'java.lang.Throwable)
                       (clojure.core/list 't__8765__auto__)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'datomic.slf4j/warn)
                             (clojure.core/list "error executing future")
                             (clojure.core/list 't__8765__auto__))))
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'datomic.monitor/alarm)
                             (clojure.core/list :UnhandledException))))
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'throw)
                             (clojure.core/list 't__8765__auto__)))))))))))))))
  (reset-meta!
    #'fire
    (assoc {:arglists (clojure.core/list ['& 'body]), :column (int 1)} :name 'fire :ns *ns*))
  (.setMacro #'fire)
  (defn schedule
    ([taskname f msec & p__8767]
      (let [map__8768 p__8767
            map__8768 (if (seq? map__8768)
                        (if (next map__8768)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8768))
                          (if (seq map__8768) (first map__8768) {}))
                        map__8768)
            once (get map__8768 :once)
            msec (long msec)
            t (java.util.Timer. ^java.lang.String taskname (boolean (.booleanValue true)))
            tt (proxy
                 [java.util.TimerTask]
                 []
                 (run
                   []
                   (try
                     (^clojure.lang.IFn f)
                     (catch
                       java.lang.Throwable
                       t
                       (do
                         (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common") ex t]
                           (when (.isWarnEnabled ^org.slf4j.Logger logger)
                             (.warn
                               ^org.slf4j.Logger logger
                               (logger/process "Scheduled task failed")
                               ^java.lang.Throwable ex)
                             (logger/caused-by logger ex))
                           nil)
                         (monitor/alarm :UnhandledException))))))]
        (if once
          (.schedule ^java.util.Timer t ^java.util.TimerTask tt (long msec))
          (.schedule ^java.util.Timer t ^java.util.TimerTask tt (long msec) (long msec)))
        (reify java.io.Closeable (^void close [this] (do (.cancel ^java.util.Timer t) nil))))))
  (reset-meta!
    #'schedule
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta ['taskname 'f 'msec '& {:keys ['once]}] {:tag 'java.io.Closeable})),
       :column (int 1)}
      :name
      'schedule
      :ns
      *ns*))
  (defn mapk ([f coll] (reduce (fn fn__8775 ([m k] (assoc m k (^clojure.lang.IFn f k)))) {} coll)))
  (reset-meta!
    #'mapk
    (assoc {:arglists (clojure.core/list ['f 'coll]), :column (int 1)} :name 'mapk :ns *ns*))
  (def returning-throwable
   (fn returning_throwable
     ([&form &env & forms]
       (seq
         (concat
           (clojure.core/list 'try)
           forms
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'catch)
                 (clojure.core/list 'java.lang.Throwable)
                 (clojure.core/list 'e)
                 (clojure.core/list 'e)))))))))
  (reset-meta!
    #'returning-throwable
    (assoc
      {:arglists (clojure.core/list ['& 'forms]), :column (int 1)}
      :name
      'returning-throwable
      :ns
      *ns*))
  (.setMacro #'returning-throwable)
  (defn log-retry
    ([result backoff attempts max_retries]
      (let [m {:event :common/retry,
               :backoff backoff,
               :attempts attempts,
               :max-retries max_retries}]
        (if (instance? java.lang.Throwable result)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common") ex result]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process m) ex)
              (logger/caused-by logger ex))
            nil)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process (assoc m :result result))))
            nil)))))
  (reset-meta!
    #'log-retry
    (assoc
      {:arglists (clojure.core/list ['result 'backoff 'attempts 'max-retries]), :column (int 1)}
      :name
      'log-retry
      :ns
      *ns*))
  (defn return-or-throw
    ([x] (when (instance? java.lang.Throwable x) (throw ^java.lang.Throwable x)) x))
  (reset-meta!
    #'return-or-throw
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'return-or-throw :ns *ns*))
  (def with-nano-time
   (fn with_nano_time
     ([&form &env f & body]
       (seq
         (concat
           (clojure.core/list 'clojure.core/let)
           (clojure.core/list
             (apply
               vector
               (seq
                 (concat
                   (clojure.core/list 'start__8781__auto__)
                   (clojure.core/list
                     (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                   (clojure.core/list 'result__8782__auto__)
                   (clojure.core/list
                     (seq
                       (concat (clojure.core/list 'datomic.common/returning-throwable) body)))))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list f)
                 (clojure.core/list
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/-)
                       (clojure.core/list
                         (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                       (clojure.core/list 'start__8781__auto__))))
                 (clojure.core/list 'result__8782__auto__))))
           (clojure.core/list
             (seq
               (concat
                 (clojure.core/list 'datomic.common/return-or-throw)
                 (clojure.core/list 'result__8782__auto__)))))))))
  (reset-meta!
    #'with-nano-time
    (assoc
      {:arglists (clojure.core/list ['f '& 'body]), :column (int 1)}
      :name
      'with-nano-time
      :ns
      *ns*))
  (.setMacro #'with-nano-time)
  (defn retry-fn
    ([f & p__8784]
      (let [map__8785 p__8784
            map__8785 (if (seq? map__8785)
                        (if (next map__8785)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8785))
                          (if (seq map__8785) (first map__8785) {}))
                        map__8785)
            pred (get map__8785 :pred)
            backoff (get map__8785 :backoff)
            max_retries (get map__8785 :max-retries)
            log_retry (get map__8785 :log-retry)
            backoff (if (number? backoff) (constantly backoff) backoff)]
        (loop [attempts 1]
          (let [result (try (^clojure.lang.IFn f) (catch java.lang.Throwable e e))]
            (when (or
                    (instance? java.lang.InterruptedException result)
                    (instance? java.io.InterruptedIOException result))
              (throw ^java.lang.Throwable result))
            (if (^clojure.lang.IFn pred result)
              (if (< attempts max_retries)
                (let [msec (^clojure.lang.IFn backoff (long attempts))]
                  (when log_retry
                    (^clojure.lang.IFn log_retry result msec (long attempts) max_retries))
                  (when (clojure.lang.Numbers/isPos msec)
                    (java.lang.Thread/sleep (unchecked-long ^java.lang.Number msec)))
                  (recur (inc attempts)))
                (return-or-throw result))
              (return-or-throw result)))))))
  (reset-meta!
    #'retry-fn
    (assoc
      {:arglists (clojure.core/list ['f '& {:keys ['pred 'backoff 'max-retries 'log-retry]}]),
       :doc
       "Calls f until pred rejects its return value or max-retries attempts have run. Throwables are supplied to pred as values; InterruptedException and InterruptedIOException always propagate immediately. Backoff is either a millisecond value or a function of the completed attempt count. The optional log-retry function receives result, delay, attempt count, and maximum attempts before each retry.",
       :column (int 1)}
      :name
      'retry-fn
      :ns
      *ns*))
  (defn create-temp-directory
    ([dir]
      (.mkdirs (io/file dir))
      (let [f (java.io.File/createTempFile
                (.format (java.text.SimpleDateFormat. "yyyy-MM-dd-kk-mm-ss-") (java.util.Date.))
                ""
                (io/file dir))]
        (let [or__5602__auto__ (.delete ^java.io.File f)]
          (when-not or__5602__auto__ (throw (java.io.IOException.))))
        (let [or__5602__auto__ (.mkdir ^java.io.File f)]
          (when-not or__5602__auto__ (throw (java.io.IOException.))))
        f)))
  (reset-meta!
    #'create-temp-directory
    (assoc
      {:arglists (clojure.core/list ['dir]), :column (int 1)}
      :name
      'create-temp-directory
      :ns
      *ns*))
  (defn delete-file-recursively
    ([f & p__8793]
      (let [vec__8794 p__8793 silently (nth vec__8794 (unchecked-int 0) nil) f (io/file f)]
        (when (.isDirectory ^java.io.File f)
          (loop [seq_8797 (seq (.listFiles ^java.io.File f)) chunk_8798 nil count_8799 0 i_8800 0]
            (if (< i_8800 count_8799)
              (let [child (.nth ^clojure.lang.Indexed chunk_8798 (unchecked-int i_8800))]
                (delete-file-recursively child silently)
                (recur seq_8797 chunk_8798 count_8799 (inc i_8800)))
              (let [temp__5825__auto__ (seq seq_8797)]
                (when temp__5825__auto__
                  (let [seq_8797 temp__5825__auto__]
                    (if (chunked-seq? seq_8797)
                      (let [c__6090__auto__ (chunk-first seq_8797)]
                        (recur (chunk-rest seq_8797) c__6090__auto__ (count c__6090__auto__) 0))
                      (let [child (first seq_8797)]
                        (delete-file-recursively child silently)
                        (recur (next seq_8797) nil 0 0)))))))))
        (io/delete-file f silently))))
  (reset-meta!
    #'delete-file-recursively
    (assoc
      {:arglists (clojure.core/list ['f '& ['silently]]), :column (int 1)}
      :name
      'delete-file-recursively
      :ns
      *ns*))
  (defn bean-setters
    ([bean_class]
      (reduce
        (fn fn__8804
          ([m pd]
            (let [name (.getName ^java.beans.FeatureDescriptor pd)
                  method (.getWriteMethod ^java.beans.PropertyDescriptor pd)]
              (if (and
                    method
                    (= 1 (long (alength (.getParameterTypes ^java.lang.reflect.Method method)))))
                (assoc
                  m
                  (keyword name)
                  (fn fn__8805
                    ([bean value]
                      (.invoke ^java.lang.reflect.Method method bean (into-array [value])))))
                m))))
        {}
        (.getPropertyDescriptors
          (java.beans.Introspector/getBeanInfo ^java.lang.Class bean_class)))))
  (reset-meta!
    #'bean-setters
    (assoc
      {:arglists (clojure.core/list ['bean-class]), :column (int 1)}
      :name
      'bean-setters
      :ns
      *ns*))
  (defn into-bean
    ([bean props]
      (let [setters (bean-setters (class bean))]
        (loop [seq_8810 (seq props) chunk_8811 nil count_8812 0 i_8813 0]
          (if (< i_8813 count_8812)
            (let [vec__8814 (.nth ^clojure.lang.Indexed chunk_8811 (unchecked-int i_8813))
                  k (nth vec__8814 (unchecked-int 0) nil)
                  v (nth vec__8814 (unchecked-int 1) nil)]
              (let [temp__5823__auto__ (^clojure.lang.IFn setters k)]
                (if temp__5823__auto__
                  (let [setter temp__5823__auto__] (^clojure.lang.IFn setter bean v))
                  (throw (java.lang.IllegalArgumentException. (str "No property named " k)))))
              (recur seq_8810 chunk_8811 count_8812 (inc i_8813)))
            (let [temp__5825__auto__ (seq seq_8810)]
              (when temp__5825__auto__
                (let [seq_8810 temp__5825__auto__]
                  (if (chunked-seq? seq_8810)
                    (let [c__6090__auto__ (chunk-first seq_8810)]
                      (recur (chunk-rest seq_8810) c__6090__auto__ (count c__6090__auto__) 0))
                    (let [vec__8817 (first seq_8810)
                          k (nth vec__8817 (unchecked-int 0) nil)
                          v (nth vec__8817 (unchecked-int 1) nil)]
                      (let [temp__5823__auto__ (^clojure.lang.IFn setters k)]
                        (if temp__5823__auto__
                          (let [setter temp__5823__auto__] (^clojure.lang.IFn setter bean v))
                          (throw
                            (java.lang.IllegalArgumentException. (str "No property named " k)))))
                      (recur (next seq_8810) nil 0 0))))))))
        bean)))
  (reset-meta!
    #'into-bean
    (assoc
      {:arglists (clojure.core/list ['bean 'props]), :column (int 1)}
      :name
      'into-bean
      :ns
      *ns*))
  (defn endpoint? ([m] (= #{:port :host} (into #{} (keys m)))))
  (reset-meta!
    #'endpoint?
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'endpoint? :ns *ns*))
  (defn qualified-name
    ([k]
      (let [temp__5823__auto__ (.getNamespace ^clojure.lang.Keyword k)]
        (if temp__5823__auto__
          (let [ns temp__5823__auto__] (str ns "/" (.getName ^clojure.lang.Keyword k)))
          (.getName ^clojure.lang.Keyword k)))))
  (reset-meta!
    #'qualified-name
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'k {:tag 'clojure.lang.Keyword})] {:tag 'java.lang.String})),
       :column (int 1)}
      :name
      'qualified-name
      :ns
      *ns*))
  (defn force-keyword ([x] (if (keyword? x) x (keyword (str x)))))
  (reset-meta!
    #'force-keyword
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'force-keyword :ns *ns*))
  (defn force-map-keywords
    ([m]
      (if (every? keyword? (keys m))
        m
        (reduce
          (fn fn__8830
            ([m p__8829]
              (let [vec__8831 p__8829
                    k (nth vec__8831 (unchecked-int 0) nil)
                    v (nth vec__8831 (unchecked-int 1) nil)]
                (assoc m (force-keyword k) v))))
          {}
          m))))
  (reset-meta!
    #'force-map-keywords
    (assoc
      {:arglists (clojure.core/list ['m]), :column (int 1)}
      :name
      'force-map-keywords
      :ns
      *ns*))
  (defn load-properties
    ([filename]
      (let [G__8836 (java.util.Properties.)]
        (.load ^java.util.Properties G__8836 (io/input-stream (io/file filename)))
        G__8836)))
  (reset-meta!
    #'load-properties
    (assoc
      {:arglists (clojure.core/list ['filename]), :column (int 1)}
      :name
      'load-properties
      :ns
      *ns*))
  (defn store-properties
    ([props filename]
      (.store
        ^java.util.Properties props
        (io/output-stream (io/file filename))
        "Generated Datomic Properties")
      nil))
  (reset-meta!
    #'store-properties
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'props {:tag 'Properties}) 'filename]),
       :column (int 1)}
      :name
      'store-properties
      :ns
      *ns*))
  (defn map->props
    ([m]
      (let [props (java.util.Properties.)]
        (loop [seq_8839 (seq (keys m)) chunk_8840 nil count_8841 0 i_8842 0]
          (if (< i_8842 count_8841)
            (let [k (.nth ^clojure.lang.Indexed chunk_8840 (unchecked-int i_8842))]
              (.setProperty ^java.util.Properties props (str k) (str (get m k)))
              (recur seq_8839 chunk_8840 count_8841 (inc i_8842)))
            (let [temp__5825__auto__ (seq seq_8839)]
              (when temp__5825__auto__
                (let [seq_8839 temp__5825__auto__]
                  (if (chunked-seq? seq_8839)
                    (let [c__6090__auto__ (chunk-first seq_8839)]
                      (recur (chunk-rest seq_8839) c__6090__auto__ (count c__6090__auto__) 0))
                    (let [k (first seq_8839)]
                      (.setProperty ^java.util.Properties props (str k) (str (get m k)))
                      (recur (next seq_8839) nil 0 0))))))))
        props)))
  (reset-meta!
    #'map->props
    (assoc {:arglists (clojure.core/list ['m]), :column (int 1)} :name 'map->props :ns *ns*))
  (defn props->map
    ([props]
      (reduce
        (fn fn__8846
          ([m k] (assoc m k (.getProperty ^java.util.Properties props ^java.lang.String k))))
        {}
        (keys props))))
  (reset-meta!
    #'props->map
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'props {:tag 'Properties})]), :column (int 1)}
      :name
      'props->map
      :ns
      *ns*))
  (def probe-name "probe-fa35e6c5-a886-46e7-b90b-1f0d36b3f2fe")
  (reset-meta! #'probe-name (assoc {:column (int 1)} :name 'probe-name :ns *ns*))
  (defn pop-atom!
    ([atm]
      (loop [xs (deref atm)]
        (when (seq xs) (if (compare-and-set! atm xs (rest xs)) (first xs) (recur (deref atm)))))))
  (reset-meta!
    #'pop-atom!
    (assoc {:arglists (clojure.core/list ['atm]), :column (int 1)} :name 'pop-atom! :ns *ns*))
  (defn closing-watch ([_ _ old _] (when old (.close ^java.lang.AutoCloseable old) nil)))
  (reset-meta!
    #'closing-watch
    (assoc
      {:arglists (clojure.core/list ['_ '_ (.withMeta 'old {:tag 'java.lang.AutoCloseable}) '_]),
       :column (int 1)}
      :name
      'closing-watch
      :ns
      *ns*))
  (defn pfuture
    ([exec f]
      (let [fut (.submit
                  ^java.util.concurrent.ExecutorService exec
                  ((deref #'clojure.core/binding-conveyor-fn) f))]
        (reify
          clojure.lang.IPending
          clojure.lang.IBlockingDeref
          clojure.lang.IDeref
          (deref
            [this ^long timeout_ms timeout_val]
            (try
              (.get
                ^java.util.concurrent.Future fut
                (long timeout_ms)
                java.util.concurrent.TimeUnit/MILLISECONDS)
              (catch java.util.concurrent.TimeoutException e timeout_val)))
          (deref [this] (.get ^java.util.concurrent.Future fut))
          (^boolean isRealized [this] (.isDone ^java.util.concurrent.Future fut))))))
  (reset-meta!
    #'pfuture
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'exec {:tag 'ExecutorService})
          (.withMeta 'f {:tag 'java.util.concurrent.Callable})]),
       :column (int 1)}
      :name
      'pfuture
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.common" "thread-pool") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.common" "thread-pool") thread/thread-pool)
  (.setMeta (clojure.lang.RT/var "datomic.common" "handoff-thread-pool") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.common" "handoff-thread-pool")
    thread/handoff-thread-pool)
  (.setMeta (clojure.lang.RT/var "datomic.common" "cached-thread-pool") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.common" "cached-thread-pool") thread/cached-thread-pool)
  (defn bounded-deref
    ([ref timeout_ms]
      (let [sentinel (java.lang.Object.) v (deref ref timeout_ms sentinel)]
        (when (= v sentinel)
          (throw
            (java.util.concurrent.TimeoutException.
              (str "Deref timed out after " timeout_ms " msec"))))
        v)))
  (reset-meta!
    #'bounded-deref
    (assoc
      {:arglists (clojure.core/list ['ref 'timeout-ms]),
       :doc
       "Dereferences ref within timeout-ms milliseconds and returns its value. Throws TimeoutException when the deadline expires.",
       :column (int 1)}
      :name
      'bounded-deref
      :ns
      *ns*))
  (defn pooled-mapv
    ([exec f coll]
      (mapv
        deref
        (mapv
          (fn fn__8856
            ([p1__8855#]
              (.submit
                ^java.util.concurrent.ExecutorService exec
                (bound-fn [] (^clojure.lang.IFn f p1__8855#)))))
          coll))))
  (reset-meta!
    #'pooled-mapv
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'exec {:tag 'ExecutorService}) 'f 'coll]),
       :column (int 1)}
      :name
      'pooled-mapv
      :ns
      *ns*))
  (defn distinct-by
    ([f coll]
      (let [step (fn step
                   ([xs seen]
                     (lazy-seq
                       ((fn fn__8868
                          ([p__8867 seen]
                            (let [vec__8869 p__8867
                                  fst (nth vec__8869 (unchecked-int 0) nil)
                                  xs vec__8869
                                  temp__5825__auto__ (seq xs)]
                              (when temp__5825__auto__
                                (let [s temp__5825__auto__ k (^clojure.lang.IFn f fst)]
                                  (if (contains? seen k)
                                    (recur (rest s) seen)
                                    (cons
                                      fst
                                      (^clojure.lang.IFn step (rest s) (conj seen k)))))))))
                         xs
                         seen))))]
        (^clojure.lang.IFn step coll #{})))
    ([f]
      (fn fn__8861
        ([rf]
          (let [seen (volatile! #{})]
            (fn fn__8862
              ([] (^clojure.lang.IFn rf))
              ([result] (^clojure.lang.IFn rf result))
              ([result input]
                (let [k (^clojure.lang.IFn f input)]
                  (if (contains? (deref seen) k)
                    result
                    (do
                      (vswap! ^clojure.lang.Volatile seen conj k)
                      (^clojure.lang.IFn rf result input)))))))))))
  (reset-meta!
    #'distinct-by
    (assoc
      {:arglists (clojure.core/list ['f] ['f 'coll]), :column (int 1)}
      :name
      'distinct-by
      :ns
      *ns*))
  (defn ensure-vector
    ([x]
      (if (instance? java.util.List x)
        (clojure.lang.PersistentVector/create ^java.util.List x)
        x)))
  (reset-meta!
    #'ensure-vector
    (assoc {:arglists (clojure.core/list ['x]), :column (int 1)} :name 'ensure-vector :ns *ns*))
  (defn ensure-vectors-in-array
    ([vs]
      (dotimes [i (alength ^"[Ljava.lang.Object;" vs)]
        (let [elem (aget ^"[Ljava.lang.Object;" vs (int i))]
          (when (instance? java.util.List elem)
            (aset
              ^"[Ljava.lang.Object;" vs
              (int i)
              (clojure.lang.PersistentVector/create ^java.util.List elem)))))
      vs))
  (reset-meta!
    #'ensure-vectors-in-array
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'vs {:tag 'objects})]), :column (int 1)}
      :name
      'ensure-vectors-in-array
      :ns
      *ns*))
  (defn maybe-deref ([v] (cond-> v (instance? clojure.lang.IDeref v) (deref))))
  (reset-meta!
    #'maybe-deref
    (assoc {:arglists (clojure.core/list ['v]), :column (int 1)} :name 'maybe-deref :ns *ns*))
  (defn result-count
    ([offset limit coll]
      (let [G__8882 (count coll)]
        (cond->
          (if offset (- G__8882 offset) (java.lang.Integer/valueOf (int G__8882)))
          (and limit (not (neg? limit)))
          (min limit)))))
  (reset-meta!
    #'result-count
    (assoc
      {:arglists (clojure.core/list ['offset 'limit 'coll]),
       :doc
       "Computes the count marker for a paged result by subtracting offset and capping it at a nonnegative limit. A value less than one tells counted-seq to return nil. Nil options are ignored and a negative limit means unbounded.",
       :column (int 1)}
      :name
      'result-count
      :ns
      *ns*))
  (defn result-xform
    ([offset limit f]
      (apply
        comp
        (remove
          nil?
          [(when offset (drop offset))
           (when f (map f))
           (when (and limit (not (neg? limit))) (take limit))]))))
  (reset-meta!
    #'result-xform
    (assoc
      {:arglists (clojure.core/list ['offset 'limit 'f]),
       :doc
       "Builds the result transformation used by query and index APIs: drop offset rows, optionally map f over each row, then take at most limit rows. Nil options are omitted and a negative limit is unbounded.",
       :column (int 1)}
      :name
      'result-xform
      :ns
      *ns*)))

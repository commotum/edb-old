(do
  (clojure.core/in-ns 'datomic.common)
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
    (assoc {:const true, :column 1} :name 'DEFAULT_SYSTEM_NAME :ns *ns*))
  (defn compare-byte-arrays
    (^long [a b]
      (let [a a b b len (alength ^bytes a) lencomp (- len (alength ^bytes b))]
        (if (= lencomp 0)
          (loop [pos 0]
            (if (= pos len)
              0
              (let [c (-
                        (unchecked-long
                          (java.lang.Byte/valueOf (byte (aget ^bytes a (int pos)))))
                        (unchecked-long
                          (java.lang.Byte/valueOf (byte (aget ^bytes b (int pos))))))]
                (if (= c 0) (recur (inc pos)) c))))
          lencomp))))
  (def BYTES (java.lang.Class/forName "[B"))
  (declare coll-compare)
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
  (defn equals-with-strict-scale
    ([a b]
      (and
        (zero? (compare a b))
        (if (and (instance? java.math.BigDecimal a) (instance? java.math.BigDecimal b))
          (= (long (.scale ^java.math.BigDecimal a)) (long (.scale ^java.math.BigDecimal b)))
          true))))
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
       :column 1}
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
                      (let [xs (seq x) G__8712 (java.util.ArrayList. ^java.util.Collection xs)]
                        (Collections/sort ^java.util.List G__8712 ^java.util.Comparator cmp)
                        G__8712)))
            as (^clojure.lang.IFn alist a)
            bs (^clojure.lang.IFn alist b)]
        (cl as bs))))
  (reset-meta!
    #'cx
    (assoc
      {:private true, :arglists (clojure.core/list (.withMeta ['a 'b] {:tag 'long})), :column 1}
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
      {:private true, :arglists (clojure.core/list (.withMeta ['a 'b] {:tag 'long})), :column 1}
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
  (defn <' ([a b] (neg? (compare a b))))
  (defn >' ([a b] (not (<' a b))))
  (defn split-filter ([pred coll] [(filter pred coll) (remove pred coll)]))
  (defn getx
    ([m k]
      (let [e (get m k :datomic.common/getx-sentinel-42)]
        (if (not (= e :datomic.common/getx-sentinel-42))
          e
          (do (throw (java.lang.Exception. (str "Key not found: " k))) nil)))))
  (defn getx-in ([m ks] (reduce getx m ks)))
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
  (defn env-key->clj-key ([k] (keyword (str/replace (str/lower-case k) "_" "-"))))
  (defn clj-key->env-key ([s] (str/upper-case (str/replace (name s) "-" "_"))))
  (defn map->env
    ([m]
      (reduce
        (fn fn__8726
          ([m p__8725]
            (let [vec__8727 p__8725
                  k (nth vec__8727 (unchecked-int 0) nil)
                  v (nth vec__8727 (unchecked-int 1) nil)]
              (assoc m (clj-key->env-key k) v))))
        {}
        m)))
  (defn map-from-env
    ([env]
      (reduce
        (fn fn__8732
          ([m k]
            (let [temp__5802__auto__ (get env k)]
              (if temp__5802__auto__
                (let [v temp__5802__auto__] (assoc m (env-key->clj-key k) v))
                m))))
        {}
        (keys env))))
  (defn coerce-args
    ([m & predfns]
      (let [predfns (partition 2 predfns)]
        (reduce
          (fn fn__8737
            ([m p__8736]
              (let [vec__8738 p__8736
                    k (nth vec__8738 (unchecked-int 0) nil)
                    v (nth vec__8738 (unchecked-int 1) nil)
                    temp__5802__auto__ (some
                                         (fn fn__8742
                                           ([p__8741]
                                             (let [vec__8743 p__8741
                                                   p (nth vec__8743 (unchecked-int 0) nil)
                                                   f (nth vec__8743 (unchecked-int 1) nil)]
                                               (when (^clojure.lang.IFn p k) f))))
                                         predfns)]
                (if temp__5802__auto__
                  (let [f temp__5802__auto__] (assoc m k (^clojure.lang.IFn f v)))
                  (assoc m k v)))))
          {}
          m))))
  (defn assert-all
    ([&form &env & args]
      (seq
        (concat
          (clojure.core/list 'do)
          (map
            (fn fn__8750
              ([a] (seq (concat (clojure.core/list 'clojure.core/assert) (clojure.core/list a)))))
            args)))))
  (.setMacro #'assert-all)
  (defn log-and-print
    ([& xs]
      (let [s (apply print-str xs)]
        (println s)
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process s)))
          nil))))
  (defonce AsyncShutdown {})
  (defprotocol AsyncShutdown (async-shutdown [o]))
  (defn sync-shutdown ([x] (deref (async-shutdown x))))
  (defn with-shutdown
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
            nil)))))
  (.setMacro #'with-shutdown)
  (defn await-derefs
    ([msec coll]
      (let [timed_out (java.lang.Object.)
            limit (+ msec (java.lang.System/currentTimeMillis))
            G__8779 coll
            vec__8780 G__8779
            seq__8781 (seq vec__8780)
            first__8782 (first seq__8781)
            seq__8781 (next seq__8781)
            item first__8782
            more seq__8781]
        (loop [G__8779 G__8779]
          (let [vec__8783 G__8779
                seq__8784 (seq vec__8783)
                first__8785 (first seq__8784)
                seq__8784 (next seq__8784)
                item first__8785
                more seq__8784]
            (if item
              (if (=
                    timed_out
                    (deref item (- limit (java.lang.System/currentTimeMillis)) timed_out))
                false
                (recur more))
              true)))))
    ([coll]
      (do
        (loop [seq_8772 (seq coll) chunk_8773 nil count_8774 0 i_8775 0]
          (if (< i_8775 count_8774)
            (let [c (.nth ^clojure.lang.Indexed chunk_8773 (unchecked-int i_8775))]
              (deref c)
              (recur seq_8772 chunk_8773 count_8774 (inc i_8775)))
            (let [temp__5804__auto__ (seq seq_8772)]
              (when temp__5804__auto__
                (let [seq_8772 temp__5804__auto__]
                  (if (chunked-seq? seq_8772)
                    (let [c__6065__auto__ (chunk-first seq_8772)]
                      (recur (chunk-rest seq_8772) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [c (first seq_8772)] (deref c) (recur (next seq_8772) nil 0 0))))))))
        true)))
  (defn find-free-port
    ([]
      (with-open [s (java.net.ServerSocket. (unchecked-int 0))]
        (java.lang.Integer/valueOf (int (.getLocalPort ^java.net.ServerSocket s))))))
  (defn array-cat
    ([& p__8790]
      (let [vec__8791 p__8790 a (nth vec__8791 (unchecked-int 0) nil) as vec__8791]
        (when a
          (let [length (apply + (map count as))
                type (.getComponentType (class a))
                result (java.lang.reflect.Array/newInstance
                         ^java.lang.Class type
                         (unchecked-int length))]
            (let [i 0
                  G__8797 as
                  vec__8798 G__8797
                  seq__8799 (seq vec__8798)
                  first__8800 (first seq__8799)
                  seq__8799 (next seq__8799)
                  a first__8800
                  more seq__8799]
              (loop [i i G__8797 G__8797]
                (let [i i
                      vec__8801 G__8797
                      seq__8802 (seq vec__8801)
                      first__8803 (first seq__8802)
                      seq__8802 (next seq__8802)
                      a first__8803
                      more seq__8802]
                  (when a
                    (java.lang.System/arraycopy
                      a
                      (unchecked-int 0)
                      result
                      (unchecked-int i)
                      (int (count a)))
                    (recur (+ i (count a)) more)))))
            result)))))
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
  (defn squuid-time-ms
    ([squuid]
      (long
        (*
          1000
          (bit-and
            4294967295
            (bit-shift-right (.getMostSignificantBits ^java.util.UUID squuid) 32))))))
  (defn rand-uuid ([] (squuid)))
  (def run-uuid (rand-uuid))
  (defn root-cause
    ([x] (when x (let [cause (.getCause ^java.lang.Throwable x)] (if cause (recur cause) x)))))
  (defn qualified-symbol? ([x] (boolean (and (symbol? x) (namespace x) true))))
  (defn requiring-resolve!
    ([x]
      (if (symbol? x)
        (or
          (resolve x)
          (let [temp__5804__auto__ (namespace x)]
            (when temp__5804__auto__
              (let [nsname temp__5804__auto__]
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
  (defn maybe-class
    ([cls]
      (try
        (clojure.lang.RT/classForNameNonLoading ^java.lang.String cls)
        (catch java.lang.ClassNotFoundException _ nil))))
  (defn maybe-require
    ([ns s]
      (let [temp__5804__auto__ (and (qualified-symbol? s) (namespace s))]
        (when temp__5804__auto__
          (let [sns temp__5804__auto__]
            (let [or__5581__auto__ (ns-resolve ns s)]
              (when-not or__5581__auto__
                (let [or__5581__auto__ (contains? (ns-imports ns) (symbol sns))]
                  (when-not or__5581__auto__
                    (let [or__5581__auto__ (maybe-class sns)]
                      (when-not or__5581__auto__ (clojure.core/require (symbol sns))))))))
            nil)))))
  (defn throw-anom ([anom] (throw (ex-info (:cognitect.anomalies/message anom) anom))))
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
  (defn fire
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
                      (clojure.core/list 't__8829__auto__)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.slf4j/warn)
                            (clojure.core/list "error executing future")
                            (clojure.core/list 't__8829__auto__))))
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.monitor/alarm)
                            (clojure.core/list :UnhandledException))))
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'throw)
                            (clojure.core/list 't__8829__auto__))))))))))))))
  (.setMacro #'fire)
  (defn schedule
    ([taskname f msec & p__8831]
      (let [map__8832 p__8831
            map__8832 (if (seq? map__8832)
                        (if (next map__8832)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8832))
                          (if (seq map__8832) (first map__8832) {}))
                        map__8832)
            once (get map__8832 :once)
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
  (defn mapk ([f coll] (reduce (fn fn__8839 ([m k] (assoc m k (^clojure.lang.IFn f k)))) {} coll)))
  (defn returning-throwable
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
                (clojure.core/list 'e))))))))
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
  (defn return-or-throw
    ([x] (when (instance? java.lang.Throwable x) (throw ^java.lang.Throwable x)) x))
  (defn with-nano-time
    ([&form &env f & body]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'start__8845__auto__)
                  (clojure.core/list (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                  (clojure.core/list 'result__8846__auto__)
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
                      (clojure.core/list 'start__8845__auto__))))
                (clojure.core/list 'result__8846__auto__))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'datomic.common/return-or-throw)
                (clojure.core/list 'result__8846__auto__))))))))
  (.setMacro #'with-nano-time)
  (defn retry-fn
    ([f & p__8848]
      (let [map__8849 p__8848
            map__8849 (if (seq? map__8849)
                        (if (next map__8849)
                          (clojure.lang.PersistentArrayMap/createAsIfByAssoc (to-array map__8849))
                          (if (seq map__8849) (first map__8849) {}))
                        map__8849)
            pred (get map__8849 :pred)
            backoff (get map__8849 :backoff)
            max_retries (get map__8849 :max-retries)
            log_retry (get map__8849 :log-retry)
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
  (defn create-temp-directory
    ([dir]
      (.mkdirs (io/file dir))
      (let [f (java.io.File/createTempFile
                (.format (java.text.SimpleDateFormat. "yyyy-MM-dd-kk-mm-ss-") (java.util.Date.))
                ""
                (io/file dir))]
        (let [or__5581__auto__ (.delete ^java.io.File f)]
          (when-not or__5581__auto__ (throw (java.io.IOException.))))
        (let [or__5581__auto__ (.mkdir ^java.io.File f)]
          (when-not or__5581__auto__ (throw (java.io.IOException.))))
        f)))
  (defn delete-file-recursively
    ([f & p__8857]
      (let [vec__8858 p__8857 silently (nth vec__8858 (unchecked-int 0) nil) f (io/file f)]
        (when (.isDirectory ^java.io.File f)
          (loop [seq_8861 (seq (.listFiles ^java.io.File f)) chunk_8862 nil count_8863 0 i_8864 0]
            (if (< i_8864 count_8863)
              (let [child (.nth ^clojure.lang.Indexed chunk_8862 (unchecked-int i_8864))]
                (delete-file-recursively child silently)
                (recur seq_8861 chunk_8862 count_8863 (inc i_8864)))
              (let [temp__5804__auto__ (seq seq_8861)]
                (when temp__5804__auto__
                  (let [seq_8861 temp__5804__auto__]
                    (if (chunked-seq? seq_8861)
                      (let [c__6065__auto__ (chunk-first seq_8861)]
                        (recur (chunk-rest seq_8861) c__6065__auto__ (count c__6065__auto__) 0))
                      (let [child (first seq_8861)]
                        (delete-file-recursively child silently)
                        (recur (next seq_8861) nil 0 0)))))))))
        (io/delete-file f silently))))
  (defn bean-setters
    ([bean_class]
      (reduce
        (fn fn__8868
          ([m pd]
            (let [name (.getName ^java.beans.FeatureDescriptor pd)
                  method (.getWriteMethod ^java.beans.PropertyDescriptor pd)]
              (if (and
                    method
                    (= 1 (long (alength (.getParameterTypes ^java.lang.reflect.Method method)))))
                (assoc
                  m
                  (keyword name)
                  (fn fn__8869
                    ([bean value]
                      (.invoke ^java.lang.reflect.Method method bean (into-array [value])))))
                m))))
        {}
        (.getPropertyDescriptors
          (java.beans.Introspector/getBeanInfo ^java.lang.Class bean_class)))))
  (defn into-bean
    ([bean props]
      (let [setters (bean-setters (class bean))]
        (loop [seq_8874 (seq props) chunk_8875 nil count_8876 0 i_8877 0]
          (if (< i_8877 count_8876)
            (let [vec__8878 (.nth ^clojure.lang.Indexed chunk_8875 (unchecked-int i_8877))
                  k (nth vec__8878 (unchecked-int 0) nil)
                  v (nth vec__8878 (unchecked-int 1) nil)]
              (let [temp__5802__auto__ (^clojure.lang.IFn setters k)]
                (if temp__5802__auto__
                  (let [setter temp__5802__auto__] (^clojure.lang.IFn setter bean v))
                  (throw (java.lang.IllegalArgumentException. (str "No property named " k)))))
              (recur seq_8874 chunk_8875 count_8876 (inc i_8877)))
            (let [temp__5804__auto__ (seq seq_8874)]
              (when temp__5804__auto__
                (let [seq_8874 temp__5804__auto__]
                  (if (chunked-seq? seq_8874)
                    (let [c__6065__auto__ (chunk-first seq_8874)]
                      (recur (chunk-rest seq_8874) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [vec__8881 (first seq_8874)
                          k (nth vec__8881 (unchecked-int 0) nil)
                          v (nth vec__8881 (unchecked-int 1) nil)]
                      (let [temp__5802__auto__ (^clojure.lang.IFn setters k)]
                        (if temp__5802__auto__
                          (let [setter temp__5802__auto__] (^clojure.lang.IFn setter bean v))
                          (throw
                            (java.lang.IllegalArgumentException. (str "No property named " k)))))
                      (recur (next seq_8874) nil 0 0))))))))
        bean)))
  (defn endpoint? ([m] (= #{:port :host} (into #{} (keys m)))))
  (defn qualified-name
    ([k]
      (let [temp__5802__auto__ (.getNamespace ^clojure.lang.Keyword k)]
        (if temp__5802__auto__
          (let [ns temp__5802__auto__] (str ns "/" (.getName ^clojure.lang.Keyword k)))
          (.getName ^clojure.lang.Keyword k)))))
  (defn force-keyword ([x] (if (keyword? x) x (keyword (str x)))))
  (defn force-map-keywords
    ([m]
      (if (every? keyword? (keys m))
        m
        (reduce
          (fn fn__8894
            ([m p__8893]
              (let [vec__8895 p__8893
                    k (nth vec__8895 (unchecked-int 0) nil)
                    v (nth vec__8895 (unchecked-int 1) nil)]
                (assoc m (force-keyword k) v))))
          {}
          m))))
  (defn load-properties
    ([filename]
      (let [G__8900 (java.util.Properties.)]
        (.load ^java.util.Properties G__8900 (io/input-stream (io/file filename)))
        G__8900)))
  (defn store-properties
    ([props filename]
      (.store
        ^java.util.Properties props
        (io/output-stream (io/file filename))
        "Generated Datomic Properties")
      nil))
  (defn map->props
    ([m]
      (let [props (java.util.Properties.)]
        (loop [seq_8903 (seq (keys m)) chunk_8904 nil count_8905 0 i_8906 0]
          (if (< i_8906 count_8905)
            (let [k (.nth ^clojure.lang.Indexed chunk_8904 (unchecked-int i_8906))]
              (.setProperty ^java.util.Properties props (str k) (str (get m k)))
              (recur seq_8903 chunk_8904 count_8905 (inc i_8906)))
            (let [temp__5804__auto__ (seq seq_8903)]
              (when temp__5804__auto__
                (let [seq_8903 temp__5804__auto__]
                  (if (chunked-seq? seq_8903)
                    (let [c__6065__auto__ (chunk-first seq_8903)]
                      (recur (chunk-rest seq_8903) c__6065__auto__ (count c__6065__auto__) 0))
                    (let [k (first seq_8903)]
                      (.setProperty ^java.util.Properties props (str k) (str (get m k)))
                      (recur (next seq_8903) nil 0 0))))))))
        props)))
  (defn props->map
    ([props]
      (reduce
        (fn fn__8910
          ([m k] (assoc m k (.getProperty ^java.util.Properties props ^java.lang.String k))))
        {}
        (keys props))))
  (def probe-name "probe-fa35e6c5-a886-46e7-b90b-1f0d36b3f2fe")
  (defn pop-atom!
    ([atm]
      (loop [xs (deref atm)]
        (when (seq xs) (if (compare-and-set! atm xs (rest xs)) (first xs) (recur (deref atm)))))))
  (defn closing-watch ([_ _ old _] (when old (.close ^java.lang.AutoCloseable old) nil)))
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
  (def thread-pool thread/thread-pool)
  (def handoff-thread-pool thread/handoff-thread-pool)
  (def cached-thread-pool thread/cached-thread-pool)
  (defn bounded-deref
    ([ref timeout_ms]
      (let [sentinel (java.lang.Object.) v (deref ref timeout_ms sentinel)]
        (when (= v sentinel)
          (throw
            (java.util.concurrent.TimeoutException.
              (str "Deref timed out after " timeout_ms " msec"))))
        v)))
  (defn pooled-mapv
    ([exec f coll]
      (mapv
        deref
        (mapv
          (fn fn__8920
            ([p1__8919#]
              (.submit
                ^java.util.concurrent.ExecutorService exec
                (bound-fn [] (^clojure.lang.IFn f p1__8919#)))))
          coll))))
  (defn distinct-by
    ([f coll]
      (let [step (fn step
                   ([xs seen]
                     (lazy-seq
                       ((fn fn__8932
                          ([p__8931 seen]
                            (let [vec__8933 p__8931
                                  fst (nth vec__8933 (unchecked-int 0) nil)
                                  xs vec__8933
                                  temp__5804__auto__ (seq xs)]
                              (when temp__5804__auto__
                                (let [s temp__5804__auto__ k (^clojure.lang.IFn f fst)]
                                  (if (contains? seen k)
                                    (recur (rest s) seen)
                                    (cons
                                      fst
                                      (^clojure.lang.IFn step (rest s) (conj seen k)))))))))
                         xs
                         seen))))]
        (^clojure.lang.IFn step coll #{})))
    ([f]
      (fn fn__8925
        ([rf]
          (let [seen (volatile! #{})]
            (fn fn__8926
              ([] (^clojure.lang.IFn rf))
              ([result] (^clojure.lang.IFn rf result))
              ([result input]
                (let [k (^clojure.lang.IFn f input)]
                  (if (contains? (deref seen) k)
                    result
                    (do
                      (vswap! ^clojure.lang.Volatile seen conj k)
                      (^clojure.lang.IFn rf result input)))))))))))
  (defn ensure-vector
    ([x]
      (if (instance? java.util.List x)
        (clojure.lang.PersistentVector/create ^java.util.List x)
        x)))
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
  (defn maybe-deref ([v] (cond-> v (instance? clojure.lang.IDeref v) (deref))))
  (defn result-count
    ([offset limit coll]
      (let [G__8946 (count coll)]
        (cond->
          (if offset (- G__8946 offset) (java.lang.Integer/valueOf (int G__8946)))
          (and limit (not (neg? limit)))
          (min limit)))))
  (defn result-xform
    ([offset limit f]
      (apply
        comp
        (remove
          nil?
          [(when offset (drop offset))
           (when f (map f))
           (when (and limit (not (neg? limit))) (take limit))])))))

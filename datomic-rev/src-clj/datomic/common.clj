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
      (let [^java.lang.Number result
            (cond
              (.equals ^java.lang.Object a b) 0
              (or
                (instance? java.util.List a)
                (and (instance? java.util.Map a) (not (instance? clojure.lang.IRecord a)))
                (instance? java.util.Set a)) (long (coll-compare a b))
              (identical?
                (.getClass ^java.lang.Object a)
                (.getClass ^java.lang.Object b)) (if (instance? BYTES a)
                                                   (long (compare-byte-arrays a b))
                                                   (java.lang.Integer/valueOf
                                                     (int
                                                       (.compareTo
                                                         ^java.lang.Comparable a
                                                         b))))
              (or (instance? java.util.Collection b) (instance? java.util.Map b)) 1
              :else (do
                      (java.lang.Integer/valueOf
                        (int
                          (.compareTo
                            (.getName (.getClass ^java.lang.Object a))
                            (.getName (.getClass ^java.lang.Object b)))))))]
        (.longValue result))))
  (defn compare
    (^long [a b]
      (let [^java.lang.Number result
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
              (and
                (instance? java.lang.String a)
                (instance? java.lang.String b)) (java.lang.Integer/valueOf
                                                  (int
                                                    (.compareTo
                                                      ^java.lang.Comparable a
                                                      b)))
              (instance? java.lang.Number b) 1
              :else (do (long (compare-ex a b))))]
        (.longValue result))))
  (defn equals-with-strict-scale
    ([a b]
      (and
        (zero? (compare a b))
        (if (and (instance? java.math.BigDecimal a) (instance? java.math.BigDecimal b))
          (= (long (.scale ^java.math.BigDecimal a)) (long (.scale ^java.math.BigDecimal b)))
          true))))
  (defn cl
    (^long [a b]
      (let [^java.lang.Number result
            (loop [as (.iterator ^java.util.List a) bs (.iterator ^java.util.List b)]
              (let [ha (.hasNext ^java.util.Iterator as) hb (.hasNext ^java.util.Iterator bs)]
                (cond
                  (and ha hb) (let [c (compare
                                        (.next ^java.util.Iterator as)
                                        (.next ^java.util.Iterator bs))]
                                (if (= c 0) (recur as bs) (long c)))
                  ha 1
                  hb -1
                  :else (do 0))))]
        (.longValue result))))
  (reset-meta!
    #'cl
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (let [^clojure.lang.IObj args
               [(.withMeta 'a {:tag 'List}) (.withMeta 'b {:tag 'List})]]
           (.withMeta args {:tag 'long}))),
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
                      (let [xs (seq x) G__9030 (java.util.ArrayList. ^java.util.Collection xs)]
                        (Collections/sort ^java.util.List G__9030 ^java.util.Comparator cmp)
                        G__9030)))
            as (^clojure.lang.IFn alist a)
            bs (^clojure.lang.IFn alist b)]
        (cl as bs))))
  (reset-meta!
    #'cx
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (let [^clojure.lang.IObj args ['a 'b]]
           (.withMeta args {:tag 'long}))),
       :column 1}
      :name
      'cx
      :ns
      *ns*))
  (defn cc
    (^long [a b]
      (let [^java.lang.Number result
            (let [ca (count a) cb (count b)]
              (cond
                (< ca cb) -1
                (> ca cb) 1
                :else (do
                        (let [ha (.hashCode ^java.lang.Object a)
                              hb (.hashCode ^java.lang.Object b)]
                          (if (= ha hb) (long (cx a b)) (long (- ha hb)))))))]
        (.longValue result))))
  (reset-meta!
    #'cc
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (let [^clojure.lang.IObj args ['a 'b]]
           (.withMeta args {:tag 'long}))),
       :column 1}
      :name
      'cc
      :ns
      *ns*))
  (defn coll-compare
    (^long [a b]
      (let [^java.lang.Number result
            (cond
              (instance? java.util.List a) (if (instance? java.util.List b) (long (cl a b)) -1)
              (instance? java.util.List b) 1
              (instance? java.util.Map a) (if (instance? java.util.Map b) (long (cc a b)) -1)
              (instance? java.util.Map b) 1
              (instance? java.util.Set a) (do
                                             (if (instance? java.util.Set b)
                                               (long (cc a b))
                                               -1)))]
        (.longValue result))))
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
        (fn fn__9044
          ([m p__9043]
            (let [vec__9045 p__9043
                  k (nth vec__9045 (unchecked-int 0) nil)
                  v (nth vec__9045 (unchecked-int 1) nil)]
              (assoc m (clj-key->env-key k) v))))
        {}
        m)))
  (defn map-from-env
    ([env]
      (reduce
        (fn fn__9050
          ([m k]
            (let [temp__5455__auto__ (get env k)]
              (if temp__5455__auto__
                (let [v temp__5455__auto__] (assoc m (env-key->clj-key k) v))
                m))))
        {}
        (keys env))))
  (defn coerce-args
    ([m & predfns]
      (let [predfns (partition 2 predfns)]
        (reduce
          (fn fn__9055
            ([m p__9054]
              (let [vec__9056 p__9054
                    k (nth vec__9056 (unchecked-int 0) nil)
                    v (nth vec__9056 (unchecked-int 1) nil)
                    temp__5455__auto__ (some
                                         (fn fn__9060
                                           ([p__9059]
                                             (let [vec__9061 p__9059
                                                   p (nth vec__9061 (unchecked-int 0) nil)
                                                   f (nth vec__9061 (unchecked-int 1) nil)]
                                               (when (^clojure.lang.IFn p k) f))))
                                         predfns)]
                (if temp__5455__auto__
                  (let [f temp__5455__auto__] (assoc m k (^clojure.lang.IFn f v)))
                  (assoc m k v)))))
          {}
          m))))
  (defn assert-all
    ([&form &env & args]
      (seq
        (concat
          (clojure.core/list 'do)
          (map
            (fn fn__9068
              ([a] (seq (concat (clojure.core/list 'clojure.core/assert) (clojure.core/list a)))))
            args)))))
  (.setMacro #'assert-all)
  (defn log-and-print
    ([& xs]
      (let [s (apply print-str xs)]
        (println s)
        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")]
          (when (.isInfoEnabled ^org.slf4j.Logger logger)
            (.info ^org.slf4j.Logger logger (logger/process s))
            nil)
          nil))))
  (defonce AsyncShutdown {})
  (defprotocol AsyncShutdown (async-shutdown [o]))
  (defn sync-shutdown ([x] (deref (async-shutdown x))))
  (defn with-shutdown
    ([&form &env bindings & body]
      (cond
        (= (count bindings) 0) (seq (concat (clojure.core/list 'do) body))
        (symbol? (^clojure.lang.IFn bindings 0)) (seq
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
                                                                 (clojure.core/list
                                                                   'datomic.common/with-shutdown)
                                                                 (clojure.core/list
                                                                   (subvec bindings 2))
                                                                 body)))
                                                           (clojure.core/list
                                                             (seq
                                                               (concat
                                                                 (clojure.core/list 'finally)
                                                                 (clojure.core/list
                                                                   (seq
                                                                     (concat
                                                                       (clojure.core/list
                                                                         'clojure.core/deref)
                                                                       (clojure.core/list
                                                                         (seq
                                                                           (concat
                                                                             (clojure.core/list
                                                                               'datomic.common/async-shutdown)
                                                                             (clojure.core/list
                                                                               (^clojure.lang.IFn bindings
                                                                                 0))))))))))))))))
        :else (do
                (throw
                  (java.lang.IllegalArgumentException.
                    "with-shutdown only allows Symbols in bindings"))
                nil))))
  (.setMacro #'with-shutdown)
  (defn await-derefs
    ([msec coll]
      (let [timed_out (java.lang.Object.)
            limit (+ msec (java.lang.System/currentTimeMillis))
            G__9097 coll
            vec__9098 G__9097
            seq__9099 (seq vec__9098)
            first__9100 (first seq__9099)
            seq__9099 (next seq__9099)
            item first__9100
            more seq__9099]
        (loop [G__9097 G__9097]
          (let [vec__9101 G__9097
                seq__9102 (seq vec__9101)
                first__9103 (first seq__9102)
                seq__9102 (next seq__9102)
                item first__9103
                more seq__9102]
            (if item
              (if (=
                    timed_out
                    (deref item (- limit (java.lang.System/currentTimeMillis)) timed_out))
                false
                (recur more))
              true)))))
    ([coll]
      (do
        (loop [seq_9090 (seq coll) chunk_9091 nil count_9092 0 i_9093 0]
          (if (< i_9093 count_9092)
            (let [c (.nth ^clojure.lang.Indexed chunk_9091 (unchecked-int i_9093))]
              (deref c)
              (recur seq_9090 chunk_9091 count_9092 (inc i_9093)))
            (let [temp__5457__auto__ (seq seq_9090)]
              (when temp__5457__auto__
                (let [seq_9090 temp__5457__auto__]
                  (if (chunked-seq? seq_9090)
                    (let [c__5719__auto__ (chunk-first seq_9090)]
                      (recur (chunk-rest seq_9090) c__5719__auto__ (count c__5719__auto__) 0))
                    (let [c (first seq_9090)] (deref c) (recur (next seq_9090) nil 0 0))))))))
        true)))
  (defn find-free-port
    ([]
      (let [s (java.net.ServerSocket. (unchecked-int 0))]
        (try
          (java.lang.Integer/valueOf (int (.getLocalPort ^java.net.ServerSocket s)))
          (finally (do (.close ^java.net.ServerSocket s) nil))))))
  (defn array-cat
    ([& p__9108]
      (let [vec__9109 p__9108 a (nth vec__9109 (unchecked-int 0) nil) as vec__9109]
        (when a
          (let [length (apply + (map count as))
                type (.getComponentType (class a))
                result (java.lang.reflect.Array/newInstance
                         ^java.lang.Class type
                         (unchecked-int length))]
            (let [i 0
                  G__9115 as
                  vec__9116 G__9115
                  seq__9117 (seq vec__9116)
                  first__9118 (first seq__9117)
                  seq__9117 (next seq__9117)
                  a first__9118
                  more seq__9117]
              (loop [i i G__9115 G__9115]
                (let [i i
                      vec__9119 G__9115
                      seq__9120 (seq vec__9119)
                      first__9121 (first seq__9120)
                      seq__9120 (next seq__9120)
                      a first__9121
                      more seq__9120]
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
          (let [temp__5457__auto__ (namespace x)]
            (when temp__5457__auto__
              (let [nsname temp__5457__auto__]
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
      (let [temp__5457__auto__ (and (qualified-symbol? s) (namespace s))]
        (when temp__5457__auto__
          (let [sns temp__5457__auto__]
            (let [or__5238__auto__ (ns-resolve ns s)]
              (when-not or__5238__auto__
                (let [or__5238__auto__ (contains? (ns-imports ns) (symbol sns))]
                  (when-not or__5238__auto__
                    (let [or__5238__auto__ (maybe-class sns)]
                      (when-not or__5238__auto__ (clojure.core/require (symbol sns))))))))
            nil)))))
  (defn throw-anom ([anom] (throw (ex-info (:cognitect.anomalies/message anom) anom)) nil))
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
          (.compareTo
            ^java.lang.Comparable (^clojure.lang.IFn key_fn o1)
            (^clojure.lang.IFn key_fn o2))))))
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
                      (clojure.core/list 't__9147__auto__)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.slf4j/warn)
                            (clojure.core/list "error executing future")
                            (clojure.core/list 't__9147__auto__))))
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.monitor/alarm)
                            (clojure.core/list :UnhandledException))))
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'throw)
                            (clojure.core/list 't__9147__auto__))))))))))))))
  (.setMacro #'fire)
  (defn schedule
    ([taskname f msec & p__9149]
      (let [map__9150 p__9149
            map__9150 (if (seq? map__9150)
                        (clojure.lang.PersistentHashMap/create (seq map__9150))
                        map__9150)
            once (get map__9150 :once)
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
                               ^java.lang.String (logger/process "Scheduled task failed")
                               ^java.lang.Throwable ex)
                             (logger/caused-by logger ex))
                           nil)
                         (monitor/alarm :UnhandledException))))))]
        (if once
          (do (.schedule ^java.util.Timer t ^java.util.TimerTask tt (long msec)) nil)
          (do (.schedule ^java.util.Timer t ^java.util.TimerTask tt (long msec) (long msec)) nil))
        (reify java.io.Closeable (^void close [this] (do (.cancel ^java.util.Timer t) nil))))))
  (defn mapk ([f coll] (reduce (fn fn__9157 ([m k] (assoc m k (^clojure.lang.IFn f k)))) {} coll)))
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
              (.info ^org.slf4j.Logger logger ^java.lang.String (logger/process m) ex)
              (logger/caused-by logger ex))
            nil)
          (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.common")]
            (when (.isInfoEnabled ^org.slf4j.Logger logger)
              (.info ^org.slf4j.Logger logger (logger/process (assoc m :result result)))
              nil)
            nil)))))
  (defn return-or-throw
    ([x] (if (instance? java.lang.Throwable x) (do (throw ^java.lang.Throwable x) nil) x)))
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
                  (clojure.core/list 'start__9163__auto__)
                  (clojure.core/list (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                  (clojure.core/list 'result__9164__auto__)
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
                      (clojure.core/list 'start__9163__auto__))))
                (clojure.core/list 'result__9164__auto__))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'datomic.common/return-or-throw)
                (clojure.core/list 'result__9164__auto__))))))))
  (.setMacro #'with-nano-time)
  (defn retry-fn
    ([f & p__9166]
      (let [map__9167 p__9166
            map__9167 (if (seq? map__9167)
                        (clojure.lang.PersistentHashMap/create (seq map__9167))
                        map__9167)
            pred (get map__9167 :pred)
            backoff (get map__9167 :backoff)
            max_retries (get map__9167 :max-retries)
            log_retry (get map__9167 :log-retry)
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
        (let [or__5238__auto__ (.delete ^java.io.File f)]
          (when-not or__5238__auto__ (throw (java.io.IOException.))))
        (let [or__5238__auto__ (.mkdir ^java.io.File f)]
          (when-not or__5238__auto__ (throw (java.io.IOException.))))
        f)))
  (defn delete-file-recursively
    ([f & p__9175]
      (let [vec__9176 p__9175 silently (nth vec__9176 (unchecked-int 0) nil) f (io/file f)]
        (when (.isDirectory ^java.io.File f)
          (loop [seq_9179 (seq (.listFiles ^java.io.File f)) chunk_9180 nil count_9181 0 i_9182 0]
            (if (< i_9182 count_9181)
              (let [child (.nth ^clojure.lang.Indexed chunk_9180 (unchecked-int i_9182))]
                (delete-file-recursively child silently)
                (recur seq_9179 chunk_9180 count_9181 (inc i_9182)))
              (let [temp__5457__auto__ (seq seq_9179)]
                (when temp__5457__auto__
                  (let [seq_9179 temp__5457__auto__]
                    (if (chunked-seq? seq_9179)
                      (let [c__5719__auto__ (chunk-first seq_9179)]
                        (recur (chunk-rest seq_9179) c__5719__auto__ (count c__5719__auto__) 0))
                      (let [child (first seq_9179)]
                        (delete-file-recursively child silently)
                        (recur (next seq_9179) nil 0 0)))))))))
        (io/delete-file f silently))))
  (defn bean-setters
    ([bean_class]
      (reduce
        (fn fn__9186
          ([m pd]
            (let [name (.getName ^java.beans.FeatureDescriptor pd)
                  method (.getWriteMethod ^java.beans.PropertyDescriptor pd)]
              (if (and
                    method
                    (= 1 (long (alength (.getParameterTypes ^java.lang.reflect.Method method)))))
                (assoc
                  m
                  (keyword name)
                  (fn fn__9187
                    ([bean value]
                      (.invoke ^java.lang.reflect.Method method bean (into-array [value])))))
                m))))
        {}
        (.getPropertyDescriptors
          (java.beans.Introspector/getBeanInfo ^java.lang.Class bean_class)))))
  (defn into-bean
    ([bean props]
      (let [setters (bean-setters (class bean))]
        (loop [seq_9192 (seq props) chunk_9193 nil count_9194 0 i_9195 0]
          (if (< i_9195 count_9194)
            (let [vec__9196 (.nth ^clojure.lang.Indexed chunk_9193 (unchecked-int i_9195))
                  k (nth vec__9196 (unchecked-int 0) nil)
                  v (nth vec__9196 (unchecked-int 1) nil)]
              (let [temp__5455__auto__ (^clojure.lang.IFn setters k)]
                (if temp__5455__auto__
                  (let [setter temp__5455__auto__] (^clojure.lang.IFn setter bean v))
                  (throw (java.lang.IllegalArgumentException. (str "No property named " k)))))
              (recur seq_9192 chunk_9193 count_9194 (inc i_9195)))
            (let [temp__5457__auto__ (seq seq_9192)]
              (when temp__5457__auto__
                (let [seq_9192 temp__5457__auto__]
                  (if (chunked-seq? seq_9192)
                    (let [c__5719__auto__ (chunk-first seq_9192)]
                      (recur (chunk-rest seq_9192) c__5719__auto__ (count c__5719__auto__) 0))
                    (let [vec__9199 (first seq_9192)
                          k (nth vec__9199 (unchecked-int 0) nil)
                          v (nth vec__9199 (unchecked-int 1) nil)]
                      (let [temp__5455__auto__ (^clojure.lang.IFn setters k)]
                        (if temp__5455__auto__
                          (let [setter temp__5455__auto__] (^clojure.lang.IFn setter bean v))
                          (throw
                            (java.lang.IllegalArgumentException. (str "No property named " k)))))
                      (recur (next seq_9192) nil 0 0))))))))
        bean)))
  (defn endpoint? ([m] (= #{:port :host} (into #{} (keys m)))))
  (defn qualified-name
    ([k]
      (let [temp__5455__auto__ (.getNamespace ^clojure.lang.Keyword k)]
        (if temp__5455__auto__
          (let [ns temp__5455__auto__] (str ns "/" (.getName ^clojure.lang.Keyword k)))
          (.getName ^clojure.lang.Keyword k)))))
  (defn force-keyword ([x] (if (keyword? x) x (keyword (str x)))))
  (defn force-map-keywords
    ([m]
      (if (every? keyword? (keys m))
        m
        (reduce
          (fn fn__9212
            ([m p__9211]
              (let [vec__9213 p__9211
                    k (nth vec__9213 (unchecked-int 0) nil)
                    v (nth vec__9213 (unchecked-int 1) nil)]
                (assoc m (force-keyword k) v))))
          {}
          m))))
  (defn load-properties
    ([filename]
      (let [G__9218 (java.util.Properties.)]
        (.load ^java.util.Properties G__9218 (io/input-stream (io/file filename)))
        G__9218)))
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
        (loop [seq_9221 (seq (keys m)) chunk_9222 nil count_9223 0 i_9224 0]
          (if (< i_9224 count_9223)
            (let [k (.nth ^clojure.lang.Indexed chunk_9222 (unchecked-int i_9224))]
              (.setProperty ^java.util.Properties props (str k) (str (get m k)))
              (recur seq_9221 chunk_9222 count_9223 (inc i_9224)))
            (let [temp__5457__auto__ (seq seq_9221)]
              (when temp__5457__auto__
                (let [seq_9221 temp__5457__auto__]
                  (if (chunked-seq? seq_9221)
                    (let [c__5719__auto__ (chunk-first seq_9221)]
                      (recur (chunk-rest seq_9221) c__5719__auto__ (count c__5719__auto__) 0))
                    (let [k (first seq_9221)]
                      (.setProperty ^java.util.Properties props (str k) (str (get m k)))
                      (recur (next seq_9221) nil 0 0))))))))
        props)))
  (defn props->map
    ([props]
      (reduce
        (fn fn__9228
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
                  ^java.util.concurrent.Callable
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
        (if (= v sentinel)
          (do
            (throw
              (java.util.concurrent.TimeoutException.
                (str "Deref timed out after " timeout_ms " msec")))
            nil)
          v))))
  (defn pooled-mapv
    ([exec f coll]
      (mapv
        deref
        (mapv
          (fn fn__9238
            ([p1__9237#]
              (let [^java.util.concurrent.Callable task
                    (bound-fn [] (^clojure.lang.IFn f p1__9237#))]
                (.submit ^java.util.concurrent.ExecutorService exec task))))
          coll))))
  (defn distinct-by
    ([f coll]
      (let [step (fn step
                   ([xs seen]
                     (lazy-seq
                       ((fn fn__9250
                          ([p__9249 seen]
                            (let [vec__9251 p__9249
                                  fst (nth vec__9251 (unchecked-int 0) nil)
                                  xs vec__9251
                                  temp__5457__auto__ (seq xs)]
                              (when temp__5457__auto__
                                (let [s temp__5457__auto__ k (^clojure.lang.IFn f fst)]
                                  (if (contains? seen k)
                                    (recur (rest s) seen)
                                    (cons
                                      fst
                                      (^clojure.lang.IFn step (rest s) (conj seen k)))))))))
                         xs
                         seen))))]
        (^clojure.lang.IFn step coll #{})))
    ([f]
      (fn fn__9243
        ([rf]
          (let [seen (volatile! #{})]
            (fn fn__9244
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
      (let [G__9264 (count coll)]
        (cond->
          (if offset (- G__9264 offset) (java.lang.Integer/valueOf (int G__9264)))
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

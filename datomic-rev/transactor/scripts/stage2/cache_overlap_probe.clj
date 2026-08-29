(ns datomic-rev.stage2-cache-overlap-probe
  (:require [clojure.java.io :as jio]
            [datomic.cache :as cache]
            [datomic.cache.caffeine :as caffeine]
            [datomic.cache.impl :as impl]
            [datomic.io :as dio]
            [datomic.measure.io-stats :as io-stats])
  (:import [java.lang AutoCloseable Thread$State]
           [java.util.concurrent ConcurrentHashMap CountDownLatch Executors
            ThreadFactory TimeUnit]))

(defn fail! [message data]
  (throw (ex-info message data)))

(defn require-equal! [label expected actual]
  (when-not (= expected actual)
    (fail! "cache overlap probe mismatch"
           {:label label :expected expected :actual actual})))

(defn require-true! [label value]
  (when-not value
    (fail! "cache overlap probe condition failed" {:label label})))

(defn contains-token? [value token]
  (and value token (.contains ^String value ^String token)))

(defn class-location [^Class cls]
  (some-> cls .getProtectionDomain .getCodeSource .getLocation str))

(defn normalize-throw [f]
  (try
    [:ok (f)]
    (catch Throwable t
      [:throw (.getName (class t)) (.getMessage t)])))

(defn origin-result []
  (let [kind (System/getProperty "stage2.cache.expected-kind")
        cache-entry
        (if (= kind "recovered")
          "datomic/cache.clj"
          "datomic/cache__init.class")
        cache-location (some-> (jio/resource cache-entry) str)
        version-location (some-> (jio/resource "datomic/VERSION") str)
        exceptions-location
        (class-location (Class/forName "datomic.impl.Exceptions"))
        cache-token
        (System/getProperty "stage2.cache.expected-cache-origin")
        version-token
        (System/getProperty "stage2.cache.expected-version-origin")
        java-token
        (System/getProperty "stage2.cache.expected-java-origin")]
    (require-true! :known-lane-kind (contains? #{"original" "recovered"} kind))
    (require-true! :cache-origin
                   (contains-token? cache-location cache-token))
    (require-true! :version-origin
                   (contains-token? version-location version-token))
    (require-true! :java-origin
                   (contains-token? exceptions-location java-token))
    [:verified true true true]))

(defn arity-shape [v]
  (->> (:arglists (meta v))
       (map (fn [args]
              (let [args (vec args)
                    amp-index (.indexOf ^java.util.List args '&)]
                [(if (neg? amp-index) (count args) amp-index)
                 (not (neg? amp-index))])))
       sort
       vec))

(def alias-specs
  [[:fast-count #'cache/fast-count #'impl/fast-count [[1 false]]]
   [:cache-keys #'cache/cache-keys #'impl/cache-keys [[1 false]]]
   [:put #'cache/put #'impl/put [[3 false]]]
   [:remove #'cache/remove #'impl/remove [[2 false]]]
   [:clear #'cache/clear #'impl/clear [[1 false]]]
   [:create-write-limited #'cache/create-write-limited
    #'caffeine/create-write-limited [[1 false] [2 false]]]
   [:create-limited #'cache/create-limited
    #'caffeine/create-limited [[1 false] [2 false]]]
   [:create-soft-limited #'cache/create-soft-limited
    #'caffeine/create-soft-limited [[0 false] [1 false] [2 false]]]
   [:create-scaled-weight-limited #'cache/create-scaled-weight-limited
    #'caffeine/create-scaled-weight-limited [[3 false]]]
   [:create-computing #'cache/create-computing
    #'caffeine/create-computing [[2 false]]]
   [:create-response-map #'cache/create-response-map
    #'caffeine/create-response-map [[1 false]]]])

(defn alias-result []
  (mapv
    (fn [[label alias-var target-var expected-arities]]
      (let [identity-match (identical? @alias-var target-var)
            actual-arities (arity-shape target-var)]
        (require-true! [label :root-identity] identity-match)
        (require-equal! [label :arities] expected-arities actual-arities)
        [label identity-match actual-arities]))
    alias-specs))

(defn cached-lookup [normal uncached cached calls]
  (reify
    datomic.cache.ICachedLookup
    (valAtUncached [_ k not-found]
      (swap! calls conj [:uncached k])
      (get uncached k not-found))
    (getFromCache [_ k not-found]
      (swap! calls conj [:cached k])
      (get cached k not-found))
    clojure.lang.ILookup
    (valAt [_ k]
      (swap! calls conj [:normal k])
      (get normal k))
    (valAt [_ k not-found]
      (swap! calls conj [:normal k])
      (get normal k not-found))))

(defn basic-lookup-result []
  (let [calls (atom [])
        lookup (cached-lookup {:normal :normal-value}
                              {:uncached :uncached-value}
                              {:cached :cached-value}
                              calls)
        result
        {:plain-cache-hit (cache/get-from-cache {:k :v} :k :nf)
         :plain-cache-miss (cache/get-from-cache {} :k :nf)
         :custom-cache-hit (cache/get-from-cache lookup :cached :nf)
         :custom-cache-miss (cache/get-from-cache lookup :missing :nf)
         :plain-uncached-hit (cache/get-uncached {:k :v} :k :nf)
         :plain-uncached-miss (cache/get-uncached {} :k :nf)
         :custom-uncached-hit (cache/get-uncached lookup :uncached :nf)
         :custom-uncached-miss (cache/get-uncached lookup :missing :nf)
         :plain-getx (cache/getx-uncached {:k :v} :k)
         :custom-getx (cache/getx-uncached lookup :uncached)
         :plain-getx-miss (normalize-throw #(cache/getx-uncached {} :missing))
         :custom-getx-miss
         (normalize-throw #(cache/getx-uncached lookup :missing))}
        expected
        {:plain-cache-hit :v
         :plain-cache-miss :nf
         :custom-cache-hit :cached-value
         :custom-cache-miss :nf
         :plain-uncached-hit :v
         :plain-uncached-miss :nf
         :custom-uncached-hit :uncached-value
         :custom-uncached-miss :nf
         :plain-getx :v
         :custom-getx :uncached-value
         :plain-getx-miss
         [:throw "java.lang.Exception" "Key not found: :missing"]
         :custom-getx-miss
         [:throw "java.lang.Exception" "Key not found: :missing"]}]
    (require-equal! :basic-lookups expected result)
    (require-equal!
      :basic-lookup-call-routing
      (frequencies
        [[:cached :cached]
         [:cached :missing]
         [:uncached :uncached]
         [:uncached :missing]
         [:uncached :uncached]
         [:uncached :missing]])
      (frequencies @calls))
    result))

(defn daemon-thread-factory [prefix]
  (let [counter (atom 0)]
    (reify ThreadFactory
      (newThread [_ runnable]
        (doto (Thread. ^Runnable runnable
                       (str prefix "-" (swap! counter inc)))
          (.setDaemon true))))))

(defn read-ahead-result []
  (let [executor (Executors/newSingleThreadExecutor
                   (daemon-thread-factory "stage2-cache-read-ahead"))
        calls (atom [])
        completed (CountDownLatch. 1)
        lookup (cached-lookup
                 {:work :loaded}
                 {}
                 {:already :cached}
                 calls)]
    (try
      (with-redefs-fn
        {#'datomic.cache/read-ahead-pool-prop (delay 0)
         #'datomic.cache/read-ahead-pool (delay executor)}
        #(require-equal! :read-ahead-disabled nil
                         (cache/read-ahead lookup :disabled)))
      (with-redefs-fn
        {#'datomic.cache/read-ahead-pool-prop (delay 1)
         #'datomic.cache/read-ahead-pool (delay executor)}
        #(do
           (require-equal! :read-ahead-cache-hit nil
                           (cache/read-ahead lookup :already))
           (let [work-lookup
                 (reify
                   datomic.cache.ICachedLookup
                   (valAtUncached [_ k not-found] not-found)
                   (getFromCache [_ k not-found]
                     (swap! calls conj [:work-cache k])
                     not-found)
                   clojure.lang.ILookup
                   (valAt [_ k]
                     (swap! calls conj [:work-normal k])
                     (.countDown completed)
                     :loaded)
                   (valAt [_ k not-found]
                     (swap! calls conj [:work-normal k])
                     (.countDown completed)
                     :loaded))]
             (cache/read-ahead work-lookup :work)
             (require-true! :read-ahead-completed
                            (.await completed 5 TimeUnit/SECONDS)))))
      (let [call-frequencies (frequencies @calls)
            result {:disabled-calls
                    (count (filter #(= :disabled (second %)) @calls))
                    :cache-hit-calls
                    (count (filter #(= [:cached :already] %) @calls))
                    :cache-hit-normal-calls
                    (count (filter #(= [:normal :already] %) @calls))
                    :submitted-cache-calls
                    (count (filter #(= [:work-cache :work] %) @calls))
                    :submitted-normal-calls
                    (count (filter #(= [:work-normal :work] %) @calls))}]
        (require-equal!
          :read-ahead-complete-call-trace
          {[:cached :already] 1
           [:work-cache :work] 1
           [:work-normal :work] 1}
          call-frequencies)
        (require-equal!
          :read-ahead
          {:disabled-calls 0
           :cache-hit-calls 1
           :cache-hit-normal-calls 0
           :submitted-cache-calls 1
           :submitted-normal-calls 1}
          result)
        result)
      (finally
        (.shutdownNow executor)
        (require-true! :read-ahead-executor-cleanup
                       (.awaitTermination executor 5 TimeUnit/SECONDS))))))

(defn report-failure [description raw]
  (let [cause (IllegalStateException. "stage2-conversion-cause")]
    (with-redefs [dio/describe-bbuf (fn [_] description)]
      (try
        (cache/report-val-fn-fail cause raw :stage2/key)
        (fail! "report-val-fn-fail returned" {:description description})
        (catch clojure.lang.ExceptionInfo e
          [(.getName (class e))
           (.startsWith (.getMessage e) "Unable to convert data: ")
           (:key (ex-data e))
           (:kind (ex-data e))
           (some-> (:class (ex-data e)) .getName)
           (.getName (class (.getCause e)))
           (.getMessage (.getCause e))])))))

(defn report-val-fn-result []
  (let [result
        [(report-failure {:kind :described} "raw-buffer")
         (report-failure nil "raw-object")]
        expected
        [["clojure.lang.ExceptionInfo" true :stage2/key :described nil
          "java.lang.IllegalStateException" "stage2-conversion-cause"]
         ["clojure.lang.ExceptionInfo" true :stage2/key nil "java.lang.String"
          "java.lang.IllegalStateException" "stage2-conversion-cause"]]]
    (require-equal! :report-val-fn-fail expected result)
    result))

(defn transformer-error [lookup]
  (with-redefs [dio/describe-bbuf (fn [_] nil)]
    (try
      (get lookup :bad :nf)
      (fail! "transformer error path returned" {})
      (catch clojure.lang.ExceptionInfo e
        [(.getName (class e))
         (:key (ex-data e))
         (some-> (:class (ex-data e)) .getName)
         (.getName (class (.getCause e)))
         (.getMessage (.getCause e))]))))

(defn transformer-result []
  (let [calls (atom [])
        raw (cached-lookup {:raw-normal "normal" :raw-bad "bad"}
                           {:raw-uncached "uncached"}
                           {:raw-cached "cached"}
                           calls)
        key-fn #(keyword (str "raw-" (name %)))
        transformed
        (cache/lookup-transformer raw
                                  :key-fn key-fn
                                  :val-fn #(str "value:" %))
        default-transformer (cache/lookup-transformer {:k :v})
        error-transformer
        (cache/lookup-transformer
          raw
          :key-fn key-fn
          :val-fn (fn [_]
                    (throw (IllegalArgumentException. "transformer-boom"))))
        safe-calls (atom [])
        safe-transformer
        (cache/safe-lookup-transformer
          {:raw-safe "safe" :raw-bad "bad"}
          :key-fn key-fn
          :val-fn (fn [k raw-value not-found]
                    (swap! safe-calls conj [k raw-value not-found])
                    (str (name k) ":" raw-value)))
        safe-error
        (cache/safe-lookup-transformer
          {:raw-bad "bad"}
          :key-fn key-fn
          :val-fn (fn [_ _ _]
                    (throw (IllegalStateException. "safe-transformer-boom"))))
        default-hit (get default-transformer :k)
        default-miss (get default-transformer :missing :nf)
        normal-hit (get transformed :normal :nf)
        normal-miss (get transformed :missing :nf)
        cache-hit (cache/get-from-cache transformed :cached :nf)
        cache-miss (cache/get-from-cache transformed :missing :nf)
        uncached-hit (cache/get-uncached transformed :uncached :nf)
        uncached-miss (cache/get-uncached transformed :missing :nf)
        transformer-failure (transformer-error error-transformer)
        safe-hit (get safe-transformer :safe :nf)
        safe-miss (get safe-transformer :missing :nf)
        safe-failure (transformer-error safe-error)
        observed-safe-calls @safe-calls
        result
        {:default-hit default-hit
         :default-miss default-miss
         :normal-hit normal-hit
         :normal-miss normal-miss
         :cache-hit cache-hit
         :cache-miss cache-miss
         :uncached-hit uncached-hit
         :uncached-miss uncached-miss
         :transformer-error transformer-failure
         :safe-hit safe-hit
         :safe-miss safe-miss
         :safe-error safe-failure
         :safe-calls observed-safe-calls}
        expected
        {:default-hit :v
         :default-miss :nf
         :normal-hit "value:normal"
         :normal-miss :nf
         :cache-hit "value:cached"
         :cache-miss :nf
         :uncached-hit "value:uncached"
         :uncached-miss :nf
         :transformer-error
         ["clojure.lang.ExceptionInfo" :bad "java.lang.String"
          "java.lang.IllegalArgumentException" "transformer-boom"]
         :safe-hit "raw-safe:safe"
         :safe-miss :nf
         :safe-error
         ["clojure.lang.ExceptionInfo" :bad "java.lang.String"
          "java.lang.IllegalStateException" "safe-transformer-boom"]
         :safe-calls [[:raw-safe "safe" :nf]]}]
    (require-equal! :transformers expected result)
    result))

(defn lookup-cache-result []
  (let [source {:source :from-source
                :false-cached :from-fallback}
        backing (ConcurrentHashMap.)
        events (atom [])
        _ (.put backing :cached :from-cache)
        _ (.put backing :false-cached false)
        lookup (cache/lookup-cache source backing
                                   #(swap! events conj [%1 %2]))
        two-arity-backing (ConcurrentHashMap.)
        two-arity (cache/lookup-cache {:two :two-value}
                                      two-arity-backing)
        cache-hit (get lookup :cached :nf)
        cache-only-hit (cache/get-from-cache lookup :cached :nf)
        cache-only-miss (cache/get-from-cache lookup :absent :nf)
        uncached-source-hit (cache/get-uncached lookup :source :nf)
        uncached-source-miss (cache/get-uncached lookup :absent :nf)
        normal-source-hit (get lookup :source :nf)
        source-populated (.get backing :source)
        normal-miss (get lookup :absent :nf)
        false-treated-as-miss (get lookup :false-cached :nf)
        false-replaced (.get backing :false-cached)
        two-arity-hit (get two-arity :two :nf)
        two-arity-populated (.get two-arity-backing :two)
        result-before-protocols
        {:cache-hit cache-hit
         :cache-only-hit cache-only-hit
         :cache-only-miss cache-only-miss
         :uncached-source-hit uncached-source-hit
         :uncached-source-miss uncached-source-miss
         :normal-source-hit normal-source-hit
         :source-populated source-populated
         :normal-miss normal-miss
         :false-treated-as-miss false-treated-as-miss
         :false-replaced false-replaced
         :two-arity-hit two-arity-hit
         :two-arity-populated two-arity-populated}
        expected-before-protocols
        {:cache-hit :from-cache
         :cache-only-hit :from-cache
         :cache-only-miss :nf
         :uncached-source-hit :from-source
         :uncached-source-miss :nf
         :normal-source-hit :from-source
         :source-populated :from-source
         :normal-miss :nf
         :false-treated-as-miss :from-fallback
         :false-replaced :from-fallback
         :two-arity-hit :two-value
         :two-arity-populated :two-value}]
    (require-equal! :lookup-cache-before-protocols
                    expected-before-protocols result-before-protocols)
    (cache/put lookup :manual :manual-value)
    (let [manual (get lookup :manual :nf)
          removed (cache/remove lookup :manual)
          count-before-clear (cache/fast-count lookup)]
      (cache/clear lookup)
      (let [result
            {:lookups result-before-protocols
             :manual manual
             :removed removed
             :count-before-clear count-before-clear
             :count-after-clear (cache/fast-count lookup)
             :callbacks @events}]
        (require-equal! :lookup-cache-manual :manual-value manual)
        (require-equal! :lookup-cache-removed :manual-value removed)
        (require-equal! :lookup-cache-count-before-clear 3 count-before-clear)
        (require-equal! :lookup-cache-count-after-clear 0
                        (:count-after-clear result))
        (require-equal!
          :lookup-cache-callbacks
          [[:cached :hit]
           [:cached :hit]
           [:absent :miss]
           [:source :miss]
           [:absent :miss]
           [:source :miss]
           [:absent :miss]
           [:false-cached :miss]
           [:manual :hit]]
          @events)
        result))))

(defn constructor-result []
  (let [write-limited (cache/create-write-limited 4 2)
        limited (cache/create-limited 4 2)
        soft-limited (cache/create-soft-limited 4 2)
        scaled
        (cache/create-scaled-weight-limited 100 (fn [_ _] 1) 2)
        computing (cache/create-computing #(str "loaded:" (name %)) 4)
        response-map (cache/create-response-map 2)
        pairs [[write-limited :write]
               [limited :limited]
               [soft-limited :soft]
               [scaled :scaled]
               [response-map :response]]
        _ (doseq [[c value] pairs]
            (cache/put c :k value))
        values (mapv (fn [[c _]] (get c :k :nf)) pairs)
        counts (mapv (fn [[c _]] (cache/fast-count c)) pairs)
        keys-result
        (mapv (fn [[c _]] (vec (sort (cache/cache-keys c)))) pairs)
        computed (get computing :computed :nf)
        computed-count (cache/fast-count computing)
        removed (cache/remove limited :k)
        limited-count-after-remove (cache/fast-count limited)
        result
        {:values values
         :counts counts
         :keys keys-result
         :computed computed
         :computed-count computed-count
         :removed removed
         :limited-count-after-remove limited-count-after-remove}
        expected
        {:values [:write :limited :soft :scaled :response]
         :counts [1 1 1 1 1]
         :keys [[:k] [:k] [:k] [:k] [:k]]
         :computed "loaded:computed"
         :computed-count 1
         :removed :limited
         :limited-count-after-remove 0}]
    (require-equal! :constructors expected result)
    (doseq [[c _] pairs]
      (cache/clear c))
    (cache/clear computing)
    result))

(defn thread-blocked? [^Thread thread]
  (contains? #{Thread$State/BLOCKED
               Thread$State/WAITING
               Thread$State/TIMED_WAITING}
             (.getState thread)))

(defn await-thread-blocked! [^Thread thread]
  (let [deadline (+ (System/nanoTime) (.toNanos TimeUnit/SECONDS 5))]
    (loop []
      (cond
        (thread-blocked? thread) true
        (not (.isAlive thread)) false
        (< (System/nanoTime) deadline)
        (do (Thread/yield) (recur))
        :else false))))

(defn inflight-result []
  (let [entered (CountDownLatch. 1)
        release (CountDownLatch. 1)
        calls (atom 0)
        stats (atom [])
        results (atom [])
        underlying
        (reify clojure.lang.ILookup
          (valAt [_ k]
            (swap! calls inc)
            (.countDown entered)
            (require-true! :inflight-release
                           (.await release 5 TimeUnit/SECONDS))
            [:shared k])
          (valAt [this k _] (.valAt ^clojure.lang.ILookup this k)))
        lookup (cache/lookup-with-inflight-cache underlying)
        make-thread
        (fn [label]
          (doto
            (Thread.
              ^Runnable
              (fn []
                (swap! results conj
                       [label (normalize-throw
                                #(get lookup :same :nf))]))
              (str "stage2-cache-inflight-" (name label)))
            (.setDaemon true)))
        t1 (make-thread :one)
        t2 (make-thread :two)]
    (with-redefs [io-stats/inc!
                  (fn [key ^long nanos]
                    (swap! stats conj [key nanos]))]
      (.start t1)
      (require-true! :inflight-owner-entered
                     (.await entered 5 TimeUnit/SECONDS))
      (.start t2)
      (require-true! :inflight-waiter-blocked (await-thread-blocked! t2))
      (.countDown release)
      (.join t1 5000)
      (.join t2 5000))
    (require-true! :inflight-owner-stopped (not (.isAlive t1)))
    (require-true! :inflight-waiter-stopped (not (.isAlive t2)))
    (let [ordered-results (vec (sort-by first @results))
          waiter-stat (first @stats)
          success
          {:underlying-calls @calls
           :thread-results ordered-results
           :waiter-stat-count (count @stats)
           :waiter-stat-key (first waiter-stat)
           :waiter-stat-nanos-valid
           (and (integer? (second waiter-stat))
                (not (neg? (long (second waiter-stat)))))}
          failure-attempts (atom 0)
          failing-underlying
          (reify clojure.lang.ILookup
            (valAt [_ k]
              (if (= 1 (swap! failure-attempts inc))
                (throw (IllegalStateException. "first-inflight-failure"))
                [:retry k]))
            (valAt [this k _] (.valAt ^clojure.lang.ILookup this k)))
          failing-lookup (cache/lookup-with-inflight-cache failing-underlying)
          first-failure (normalize-throw #(get failing-lookup :retry :nf))
          second-attempt (normalize-throw #(get failing-lookup :retry :nf))
          observed-attempts @failure-attempts
          failure
          {:first first-failure
           :second second-attempt
           :attempts observed-attempts}
          result {:success success :failure-cleanup failure}]
      (require-equal!
        :inflight-success
        {:underlying-calls 1
         :thread-results
         [[:one [:ok [:shared :same]]]
          [:two [:ok [:shared :same]]]]
         :waiter-stat-count 1
         :waiter-stat-key :inflight-lookup-ns
         :waiter-stat-nanos-valid true}
        success)
      (require-equal!
        :inflight-failure-cleanup
        {:first [:throw "java.lang.IllegalStateException"
                 "first-inflight-failure"]
         :second [:ok [:retry :retry]]
         :attempts 2}
        failure)
      result)))

(defn function-lookup-result []
  (let [lookup (cache/fn->lookup #(str "fn:" (name %)))
        doubled (cache/double-lookup {:first :one :false false}
                                     {:second :two :first :ignored
                                      :false :ignored})
        result
        {:fn-two-arity (get lookup :alpha)
         :fn-three-arity (get lookup :beta :nf)
         :double-first (get doubled :first :nf)
         :double-second (get doubled :second :nf)
         :double-miss (get doubled :missing :nf)
         :double-false (get doubled :false :nf)}
        expected
        {:fn-two-arity "fn:alpha"
         :fn-three-arity "fn:beta"
         :double-first :one
         :double-second :two
         :double-miss :nf
         :double-false false}]
    (require-equal! :function-lookups expected result)
    result))

(defn tracking-cache [label initial events throw-on-close?]
  (let [state (atom initial)
        lookup
        (reify
          clojure.lang.ILookup
          (valAt [_ k] (get @state k))
          (valAt [_ k not-found] (get @state k not-found))
          datomic.cache.impl.CachePut
          (put [_ k v]
            (swap! events conj [:put label k v])
            (swap! state assoc k v)
            nil)
          java.lang.AutoCloseable
          (close [_]
            (swap! events conj [:close label])
            (when throw-on-close?
              (throw (IllegalStateException.
                       (str "close-failure-" (name label)))))))]
    {:lookup lookup :state state}))

(defn repairing-stack-result []
  (let [events (atom [])
        first-cache (tracking-cache :one {:first :one} events false)
        second-cache (tracking-cache :two {:repair :two} events false)
        repairs (atom [])
        stack (cache/repairing-cache-stack
                {:cache-1 (:lookup first-cache)
                 :cache-2 (:lookup second-cache)
                 :on-repair #(swap! repairs conj %)})
        first-hit (get stack :first :nf)
        repaired-hit (get stack :repair :nf)
        repair-written (get @(:state first-cache) :repair :nf)
        missing (get stack :missing :nf)
        observed-repairs @repairs
        lookups {:first first-hit
                 :repair repaired-hit
                 :repair-written repair-written
                 :missing missing
                 :repairs observed-repairs}
        _ (cache/put stack :put :value)
        writes [(get @(:state first-cache) :put)
                (get @(:state second-cache) :put)]
        _ (.close ^AutoCloseable stack)
        normal-events @events
        flag-events (atom [])
        flag-one (tracking-cache :flag-one {} flag-events false)
        flag-two (tracking-cache :flag-two {} flag-events false)
        flag-stack (cache/repairing-cache-stack
                     {:cache-1 (:lookup flag-one)
                      :cache-2 (:lookup flag-two)
                      :close-cache-1? false
                      :close-cache-2? true
                      :on-repair (fn [_])})
        _ (.close ^AutoCloseable flag-stack)
        failure-events (atom [])
        failure-one (tracking-cache :failure-one {} failure-events true)
        failure-two (tracking-cache :failure-two {} failure-events false)
        failure-stack (cache/repairing-cache-stack
                        {:cache-1 (:lookup failure-one)
                         :cache-2 (:lookup failure-two)
                         :on-repair (fn [_])})
        close-failure (normalize-throw #(.close ^AutoCloseable failure-stack))
        result
        {:lookups lookups
         :writes writes
         :normal-events normal-events
         :flag-events @flag-events
         :close-failure close-failure
         :failure-events @failure-events}
        expected
        {:lookups {:first :one
                   :repair :two
                   :repair-written :two
                   :missing :nf
                   :repairs [:repair]}
         :writes [:value :value]
         :normal-events
         [[:put :one :repair :two]
          [:put :one :put :value]
          [:put :two :put :value]
          [:close :one]
          [:close :two]]
         :flag-events [[:close :flag-two]]
         :close-failure
         [:throw "java.lang.IllegalStateException"
          "close-failure-failure-one"]
         :failure-events [[:close :failure-one]]}]
    (require-equal! :repairing-cache-stack expected result)
    result))

(defn supported-result []
  {:origin (origin-result)
   :aliases (alias-result)
   :basic-lookups (basic-lookup-result)
   :read-ahead (read-ahead-result)
   :report-val-fn-fail (report-val-fn-result)
   :transformers (transformer-result)
   :lookup-cache (lookup-cache-result)
   :constructors (constructor-result)
   :inflight (inflight-result)
   :function-lookups (function-lookup-result)
   :repairing-cache-stack (repairing-stack-result)})

(defn compiler-domain-result []
  [(normalize-throw
     #(boolean
        (cache/lookup-transformer {} {:key-fn :identity-keyword})))
   (normalize-throw
     #(boolean
        (cache/safe-lookup-transformer {} {:key-fn :identity-keyword})))
   (normalize-throw
     #(boolean
        (cache/repairing-cache-stack
          (list (array-map :cache-1 {}
                           :cache-2 {}
                           :on-repair :repair)))))] )

(println "STAGE2_CACHE_OVERLAP_SUPPORTED_RESULT" (pr-str (supported-result)))
(println "STAGE2_CACHE_OVERLAP_COMPILER_DOMAIN_RESULT"
         (pr-str (compiler-domain-result)))

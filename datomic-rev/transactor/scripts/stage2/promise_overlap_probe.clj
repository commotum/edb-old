(ns datomic-rev.stage2-promise-overlap-probe
  (:require [clojure.java.io :as jio]
            [clojure.string :as str]
            [datomic.promise :as promise])
  (:import [datomic ListenableFuture]
           [java.io ByteArrayOutputStream PrintStream]
           [java.lang Thread$State Thread$UncaughtExceptionHandler]
           [java.util.concurrent CancellationException CountDownLatch
            ExecutionException Executor Future TimeUnit TimeoutException]))

(defn fail! [message data]
  (throw (ex-info message data)))

(defn require-equal! [label expected actual]
  (when-not (= expected actual)
    (fail! "promise overlap probe mismatch"
           {:label label :expected expected :actual actual})))

(defn require-true! [label value]
  (when-not value
    (fail! "promise overlap probe condition failed" {:label label})))

(defn contains-token? [value token]
  (and value token (.contains ^String value ^String token)))

(defn class-location [^Class cls]
  (some-> cls .getProtectionDomain .getCodeSource .getLocation str))

(defn origin-result []
  (let [kind (System/getProperty "stage2.promise.expected-kind")
        promise-entry
        (if (= kind "recovered")
          "datomic/promise.clj"
          "datomic/promise__init.class")
        promise-location (some-> (jio/resource promise-entry) str)
        version-location (some-> (jio/resource "datomic/VERSION") str)
        java-location (class-location datomic.ListenableFuture)
        promise-token
        (System/getProperty "stage2.promise.expected-promise-origin")
        version-token
        (System/getProperty "stage2.promise.expected-version-origin")
        java-token
        (System/getProperty "stage2.promise.expected-java-origin")]
    (require-true! :known-lane-kind (contains? #{"original" "recovered"} kind))
    (require-true! :promise-origin
                   (contains-token? promise-location promise-token))
    (require-true! :version-origin
                   (contains-token? version-location version-token))
    (require-true! :java-origin
                   (contains-token? java-location java-token))
    [:verified true true true]))

(def promise-vars
  '[call-user-code delivered settable-future
    throw-executionexception-if-throwable])

(def expected-arities
  {'call-user-code [[2 false]]
   'delivered [[1 false]]
   'settable-future [[0 false]]
   'throw-executionexception-if-throwable [[1 false]]})

(defn arity-shape [v]
  (->> (:arglists (meta v))
       (map (fn [args]
              (let [args (vec args)
                    amp-index (.indexOf ^java.util.List args '&)]
                [(if (neg? amp-index) (count args) amp-index)
                 (not (neg? amp-index))])))
       sort
       vec))

(defn normalized-symbol-meta [symbol]
  (into
    (sorted-map)
    (map (fn [[key value]]
           [key (if (symbol? value) (str value) value)]))
    (meta symbol)))

(defn metadata-row [symbol]
  (let [v (ns-resolve 'datomic.promise symbol)
        m (meta v)]
    (require-true! [symbol :var-present] (instance? clojure.lang.Var v))
    (require-true! [symbol :fn-root] (fn? @v))
    (require-equal! [symbol :arity]
                    (get expected-arities symbol)
                    (arity-shape v))
    [symbol
     {:keys (vec (sort (keys m)))
      :column [(:column m) (some-> (:column m) class .getName)]
      :file (:file m)
      :line (:line m)
      :name (str (:name m))
      :ns (str (ns-name (:ns m)))
      :arglists
      (mapv
        (fn [args]
          (mapv (fn [arg] [(str arg) (normalized-symbol-meta arg)]) args))
        (:arglists m))}]))

(def exact-original-metadata
  [['call-user-code
    {:keys [:arglists :column :name :ns]
     :column [1 "java.lang.Integer"]
     :file nil
     :line nil
     :name "call-user-code"
     :ns "datomic.promise"
     :arglists [[["exec" {:tag "Executor"}]
                 ["listener" {}]]]}]
   ['delivered
    {:keys [:arglists :column :name :ns]
     :column [1 "java.lang.Integer"]
     :file nil
     :line nil
     :name "delivered"
     :ns "datomic.promise"
     :arglists [[["o" {}]]]}]
   ['settable-future
    {:keys [:arglists :column :name :ns]
     :column [1 "java.lang.Integer"]
     :file nil
     :line nil
     :name "settable-future"
     :ns "datomic.promise"
     :arglists [[]]}]
   ['throw-executionexception-if-throwable
    {:keys [:arglists :column :name :ns]
     :column [1 "java.lang.Integer"]
     :file nil
     :line nil
     :name "throw-executionexception-if-throwable"
     :ns "datomic.promise"
     :arglists [[["o" {}]]]}]])

(def recovered-peer-metadata
  [['call-user-code
    {:keys [:arglists :column :file :line :name :ns]
     :column [3 "java.lang.Integer"]
     :file "datomic/promise.clj"
     :line 29
     :name "call-user-code"
     :ns "datomic.promise"
     :arglists [[["exec" {}] ["listener" {}]]]}]
   ['delivered
    {:keys [:arglists :column :file :line :name :ns]
     :column [3 "java.lang.Integer"]
     :file "datomic/promise.clj"
     :line 140
     :name "delivered"
     :ns "datomic.promise"
     :arglists [[["o" {}]]]}]
   ['settable-future
    {:keys [:arglists :column :file :line :name :ns]
     :column [3 "java.lang.Integer"]
     :file "datomic/promise.clj"
     :line 45
     :name "settable-future"
     :ns "datomic.promise"
     :arglists [[]]}]
   ['throw-executionexception-if-throwable
    {:keys [:arglists :column :file :line :name :ns]
     :column [3 "java.lang.Integer"]
     :file "datomic/promise.clj"
     :line 24
     :name "throw-executionexception-if-throwable"
     :ns "datomic.promise"
     :arglists [[["o" {}]]]}]])

(defn metadata-result []
  (let [profile (System/getProperty "stage2.promise.expected-metadata-profile")
        actual (mapv metadata-row promise-vars)
        expected
        (case profile
          "exact-original" exact-original-metadata
          "recovered-peer-source" recovered-peer-metadata
          (fail! "unknown promise metadata profile" {:profile profile}))]
    (require-equal! :metadata-profile expected actual)
    actual))

(defn execution-failure [f expected-cause]
  (try
    (f)
    (fail! "expected ExecutionException" {})
    (catch ExecutionException failure
      [(.getName (class failure))
       (identical? expected-cause (.getCause failure))
       (.getName (class (.getCause failure)))
       (.getMessage (.getCause failure))])))

(defn timeout-failure [f]
  (try
    (f)
    (fail! "expected TimeoutException" {})
    (catch TimeoutException failure
      [(.getName (class failure)) (nil? (.getMessage failure))])))

(defn helper-result []
  (let [ordinary [:value nil false 0]
        ordinary-result
        (mapv promise/throw-executionexception-if-throwable ordinary)
        failure (IllegalStateException. "stage2-helper-failure")
        wrapped
        (execution-failure
          #(promise/throw-executionexception-if-throwable failure)
          failure)
        direct-calls (atom 0)
        direct-executor
        (reify Executor
          (execute [_ runnable]
            (swap! direct-calls inc)
            (.run ^Runnable runnable)))
        direct-listener (reify Runnable (run [_] (swap! direct-calls inc)))
        direct-return (promise/call-user-code direct-executor direct-listener)
        old-handler (Thread/getDefaultUncaughtExceptionHandler)
        handler-reports (atom [])
        rejection (java.util.concurrent.RejectedExecutionException.
                    "stage2-executor-rejection")
        listener-failure (IllegalArgumentException. "stage2-listener-failure")
        rejecting-executor
        (reify Executor (execute [_ _] (throw rejection)))
        throwing-listener
        (reify Runnable (run [_] (throw listener-failure)))
        handler
        (reify Thread$UncaughtExceptionHandler
          (uncaughtException [_ thread throwable]
            (swap! handler-reports conj
                   [(identical? thread (Thread/currentThread))
                    (cond
                      (identical? throwable rejection) :executor
                      (identical? throwable listener-failure) :listener
                      :else :unexpected)])))]
    (try
      (Thread/setDefaultUncaughtExceptionHandler handler)
      (require-equal! :rejected-executor-return nil
                      (promise/call-user-code rejecting-executor direct-listener))
      (require-equal! :throwing-listener-return nil
                      (promise/call-user-code direct-executor throwing-listener))
      (finally
        (Thread/setDefaultUncaughtExceptionHandler old-handler)))
    (let [old-err System/err
          capture-bytes (ByteArrayOutputStream.)
          capture (PrintStream. capture-bytes true "UTF-8")]
      (try
        (Thread/setDefaultUncaughtExceptionHandler nil)
        (System/setErr capture)
        (require-equal! :fallback-return nil
                        (promise/call-user-code rejecting-executor direct-listener))
        (.flush capture)
        (let [captured (.toString capture-bytes "UTF-8")
              result
              {:ordinary ordinary-result
               :wrapped wrapped
               :direct-return direct-return
               :direct-calls @direct-calls
               :handler-reports @handler-reports
               :fallback-class
               (str/includes? captured
                              "java.util.concurrent.RejectedExecutionException")
               :fallback-message
               (str/includes? captured "stage2-executor-rejection")
               :fallback-nonempty (pos? (count captured))}]
          (require-equal!
            :helpers
            {:ordinary ordinary
             :wrapped
             ["java.util.concurrent.ExecutionException" true
              "java.lang.IllegalStateException" "stage2-helper-failure"]
             :direct-return nil
             :direct-calls 3
             :handler-reports [[true :executor] [true :listener]]
             :fallback-class true
             :fallback-message true
             :fallback-nonempty true}
            result)
          result)
        (finally
          (System/setErr old-err)
          (Thread/setDefaultUncaughtExceptionHandler old-handler)
          (.close capture))))))

(defn future-interface-result [future]
  {:listenable (instance? ListenableFuture future)
   :future (instance? Future future)
   :pending (instance? clojure.lang.IPending future)
   :blocking-deref (instance? clojure.lang.IBlockingDeref future)
   :deref (instance? clojure.lang.IDeref future)
   :function (instance? clojure.lang.IFn future)
   :metadata (instance? clojure.lang.IObj future)})

(defn future-result []
  (let [pending (promise/settable-future)
        pending-result
        {:interfaces (future-interface-result pending)
         :realized (realized? pending)
         :done (.isDone ^Future pending)
         :cancelled (.isCancelled ^Future pending)
         :bounded-deref (deref pending 1 :stage2/pending)
         :timed-get
         (timeout-failure #(.get ^Future pending 1 TimeUnit/NANOSECONDS))
         :string (str pending)}
        expected-pending
        {:interfaces {:listenable true
                      :future true
                      :pending true
                      :blocking-deref true
                      :deref true
                      :function true
                      :metadata true}
         :realized false
         :done false
         :cancelled false
         :bounded-deref :stage2/pending
         :timed-get ["java.util.concurrent.TimeoutException" true]
         :string "#<Future: :pending>"}
        callbacks (atom [])
        direct-executor
        (reify Executor
          (execute [_ runnable] (.run ^Runnable runnable)))
        add-listener!
        (fn [value]
          (.addListener
            ^ListenableFuture pending
            (reify Runnable (run [_] (swap! callbacks conj value)))
            direct-executor))]
    (require-equal! :pending-future expected-pending pending-result)
    (add-listener! :before-a)
    (add-listener! :before-b)
    (let [first-delivery (deliver pending :delivered-value)
          callbacks-after-delivery @callbacks
          _ (add-listener! :after)
          second-delivery (deliver pending :ignored)
          cancel-after-delivery (.cancel ^Future pending true)
          completed
          {:first-delivery-identical (identical? pending first-delivery)
           :callbacks-after-delivery callbacks-after-delivery
           :callbacks-final @callbacks
           :second-delivery-nil (nil? second-delivery)
           :cancel-after-delivery cancel-after-delivery
           :realized (realized? pending)
           :done (.isDone ^Future pending)
           :cancelled (.isCancelled ^Future pending)
           :deref @pending
           :bounded-deref (deref pending 1 :stage2/timeout)
           :get (.get ^Future pending)
           :timed-get (.get ^Future pending 1 TimeUnit/NANOSECONDS)
           :string (str pending)}
          metadata-future (with-meta (promise/settable-future)
                            {:stage2/trace :attached})
          nil-future (promise/delivered nil)
          false-future (promise/delivered false)
          long-future (promise/delivered (vec (range 10)))
          helpers
          {:metadata (meta metadata-future)
           :metadata-interface (future-interface-result metadata-future)
           :nil [@nil-future (.get ^Future nil-future)
                 (realized? nil-future)]
           :false [@false-future (.get ^Future false-future)
                   (realized? false-future)]
           :bounded-string (str long-future)}
          result {:pending pending-result
                  :completed completed
                  :helpers helpers}]
      (require-equal!
        :completed-future
        {:first-delivery-identical true
         :callbacks-after-delivery [:before-a :before-b]
         :callbacks-final [:before-a :before-b :after]
         :second-delivery-nil true
         :cancel-after-delivery false
         :realized true
         :done true
         :cancelled false
         :deref :delivered-value
         :bounded-deref :delivered-value
         :get :delivered-value
         :timed-get :delivered-value
         :string "#<Future: :delivered-value>"}
        completed)
      (require-equal!
        :future-helpers
        {:metadata {:stage2/trace :attached}
         :metadata-interface {:listenable true
                              :future true
                              :pending true
                              :blocking-deref true
                              :deref true
                              :function true
                              :metadata true}
         :nil [nil nil true]
         :false [false false true]
         :bounded-string "#<Future: [0 1 2 3 4 ...]>"}
        helpers)
      result)))

(defn throwable-and-cancel-result []
  (let [failure (ex-info "stage2-promise-failure" {:case :promise})
        failed (promise/delivered failure)
        failure-result
        {:deref (execution-failure #(deref failed) failure)
         :bounded-deref
         (execution-failure #(deref failed 1 :stage2/timeout) failure)
         :get (execution-failure #(.get ^Future failed) failure)
         :timed-get
         (execution-failure
           #(.get ^Future failed 1 TimeUnit/NANOSECONDS)
           failure)
         :realized (realized? failed)
         :done (.isDone ^Future failed)
         :cancelled (.isCancelled ^Future failed)
         :string-prefix (str/starts-with? (str failed) "#<Future: #error")}
        cancelled (promise/settable-future)
        first-cancel (.cancel ^Future cancelled false)
        second-cancel (.cancel ^Future cancelled true)
        cancel-cause
        (try
          (.get ^Future cancelled)
          (fail! "cancelled future returned" {})
          (catch ExecutionException wrapped (.getCause wrapped)))
        cancel-result
        {:first first-cancel
         :second second-cancel
         :realized (realized? cancelled)
         :done (.isDone ^Future cancelled)
         :cancelled (.isCancelled ^Future cancelled)
         :cause-class (.getName (class cancel-cause))
         :cause-message (.getMessage ^Throwable cancel-cause)
         :deref-cause
         (execution-failure #(deref cancelled) cancel-cause)}
        result {:failure failure-result :cancellation cancel-result}]
    (require-equal!
      :throwable-future
      {:deref ["java.util.concurrent.ExecutionException" true
               "clojure.lang.ExceptionInfo" "stage2-promise-failure"]
       :bounded-deref ["java.util.concurrent.ExecutionException" true
                       "clojure.lang.ExceptionInfo" "stage2-promise-failure"]
       :get ["java.util.concurrent.ExecutionException" true
             "clojure.lang.ExceptionInfo" "stage2-promise-failure"]
       :timed-get ["java.util.concurrent.ExecutionException" true
                   "clojure.lang.ExceptionInfo" "stage2-promise-failure"]
       :realized true
       :done true
       :cancelled false
       :string-prefix true}
      failure-result)
    (require-equal!
      :cancelled-future
      {:first true
       :second false
       :realized true
       :done true
       :cancelled true
       :cause-class "java.util.concurrent.CancellationException"
       :cause-message nil
       :deref-cause
       ["java.util.concurrent.ExecutionException" true
        "java.util.concurrent.CancellationException" nil]}
      cancel-result)
    result))

(defn daemon-thread [name errors f]
  (doto
    (Thread.
      ^Runnable
      (fn []
        (try
          (f)
          (catch Throwable failure
            (swap! errors conj
                   [name (.getName (class failure)) (.getMessage failure)]))))
      name)
    (.setDaemon true)))

(defn start-and-release! [threads ^CountDownLatch ready ^CountDownLatch start]
  (doseq [^Thread thread threads] (.start thread))
  (require-true! :race-ready (.await ready 5 TimeUnit/SECONDS))
  (.countDown start))

(defn join-threads! [label threads]
  (doseq [^Thread thread threads] (.join thread 5000))
  (require-true! label (every? #(not (.isAlive ^Thread %)) threads)))

(defn delivery-race-result []
  (let [thread-count 12
        future (promise/settable-future)
        ready (CountDownLatch. thread-count)
        start (CountDownLatch. 1)
        errors (atom [])
        outcomes (atom [])
        callback-count (atom 0)
        direct-executor
        (reify Executor (execute [_ runnable] (.run ^Runnable runnable)))
        _ (.addListener
            ^ListenableFuture future
            (reify Runnable (run [_] (swap! callback-count inc)))
            direct-executor)
        threads
        (mapv
          (fn [index]
            (daemon-thread
              (str "stage2-promise-delivery-" index)
              errors
              (fn []
                (.countDown ready)
                (require-true! :delivery-race-start
                               (.await start 5 TimeUnit/SECONDS))
                (let [result (deliver future [:candidate index])]
                  (swap! outcomes conj
                         [index (identical? future result)])))))
          (range thread-count))]
    (start-and-release! threads ready start)
    (join-threads! :delivery-race-cleanup threads)
    (let [winners (filter second @outcomes)
          winner-index (ffirst winners)
          result
          {:threads thread-count
           :outcomes (count @outcomes)
           :winner-count (count winners)
           :loser-count (count (remove second @outcomes))
           :final-matches-winner (= [:candidate winner-index] @future)
           :callback-count @callback-count
           :errors @errors}]
      (require-equal!
        :delivery-race
        {:threads 12
         :outcomes 12
         :winner-count 1
         :loser-count 11
         :final-matches-winner true
         :callback-count 1
         :errors []}
        result)
      result)))

(defn listener-race-result []
  (let [listener-count 20
        future (promise/settable-future)
        thread-count (inc listener-count)
        ready (CountDownLatch. thread-count)
        start (CountDownLatch. 1)
        errors (atom [])
        seen (atom [])
        direct-executor
        (reify Executor (execute [_ runnable] (.run ^Runnable runnable)))
        listener-threads
        (mapv
          (fn [index]
            (daemon-thread
              (str "stage2-promise-listener-" index)
              errors
              (fn []
                (.countDown ready)
                (require-true! :listener-race-start
                               (.await start 5 TimeUnit/SECONDS))
                (.addListener
                  ^ListenableFuture future
                  (reify Runnable (run [_] (swap! seen conj index)))
                  direct-executor))))
          (range listener-count))
        deliver-thread
        (daemon-thread
          "stage2-promise-listener-delivery"
          errors
          (fn []
            (.countDown ready)
            (require-true! :listener-delivery-start
                           (.await start 5 TimeUnit/SECONDS))
            (require-true! :listener-delivery-won
                           (identical? future (deliver future :published)))))
        threads (conj listener-threads deliver-thread)]
    (start-and-release! threads ready start)
    (join-threads! :listener-race-cleanup threads)
    (let [frequencies (frequencies @seen)
          result
          {:listeners listener-count
           :observed (count @seen)
           :unique (count frequencies)
           :each-once (every? #(= 1 %) (vals frequencies))
           :complete-ids (= (set (range listener-count))
                            (set (keys frequencies)))
           :value @future
           :errors @errors}]
      (require-equal!
        :listener-race
        {:listeners 20
         :observed 20
         :unique 20
         :each-once true
         :complete-ids true
         :value :published
         :errors []}
        result)
      result)))

(defn listener-failure-result []
  (let [old-handler (Thread/getDefaultUncaughtExceptionHandler)
        reports (atom [])
        good-calls (atom 0)
        rejection (java.util.concurrent.RejectedExecutionException.
                    "stage2-future-listener-rejection")
        rejecting-executor
        (reify Executor (execute [_ _] (throw rejection)))
        direct-executor
        (reify Executor (execute [_ runnable] (.run ^Runnable runnable)))
        handler
        (reify Thread$UncaughtExceptionHandler
          (uncaughtException [_ _ throwable]
            (swap! reports conj
                   (if (identical? throwable rejection) :rejection :unexpected))))
        future (promise/settable-future)]
    (try
      (Thread/setDefaultUncaughtExceptionHandler handler)
      (.addListener ^ListenableFuture future
                    (reify Runnable (run [_] nil))
                    rejecting-executor)
      (.addListener ^ListenableFuture future
                    (reify Runnable (run [_] (swap! good-calls inc)))
                    direct-executor)
      (require-true! :listener-failure-delivery
                     (identical? future (deliver future :survived)))
      (.addListener ^ListenableFuture future
                    (reify Runnable (run [_] nil))
                    rejecting-executor)
      (finally
        (Thread/setDefaultUncaughtExceptionHandler old-handler)))
    (let [result {:reports @reports
                  :good-calls @good-calls
                  :value @future
                  :done (.isDone ^Future future)}]
      (require-equal!
        :listener-failure-cleanup
        {:reports [:rejection :rejection]
         :good-calls 1
         :value :survived
         :done true}
        result)
      result)))

(defn interrupt-result []
  (let [future (promise/settable-future)
        errors (atom [])
        outcome (atom nil)
        waiter
        (daemon-thread
          "stage2-promise-interrupt"
          errors
          (fn []
            (try
              (.get ^Future future)
              (reset! outcome :returned)
              (catch InterruptedException _
                (reset! outcome :interrupted)))))
        deadline (+ (System/nanoTime) (.toNanos TimeUnit/SECONDS 5))]
    (.start waiter)
    (loop []
      (when-not (contains? #{Thread$State/WAITING Thread$State/TIMED_WAITING}
                           (.getState waiter))
        (if (< (System/nanoTime) deadline)
          (do (Thread/yield) (recur))
          (fail! "promise waiter did not block" {:state (.getState waiter)}))))
    (.interrupt waiter)
    (.join waiter 5000)
    (require-true! :interrupt-waiter-cleanup (not (.isAlive waiter)))
    (let [delivery (deliver future :after-interrupt)
          result {:outcome @outcome
                  :errors @errors
                  :delivery-identical (identical? future delivery)
                  :value @future}]
      (require-equal!
        :interrupt-cleanup
        {:outcome :interrupted
         :errors []
         :delivery-identical true
         :value :after-interrupt}
        result)
      result)))

(defn supported-result []
  {:origin (origin-result)
   :helpers (helper-result)
   :future (future-result)
   :throwable-and-cancel (throwable-and-cancel-result)
   :delivery-race (delivery-race-result)
   :listener-race (listener-race-result)
   :listener-failure (listener-failure-result)
   :interrupt-cleanup (interrupt-result)})

(println "STAGE2_PROMISE_OVERLAP_SUPPORTED_RESULT"
         (pr-str (supported-result)))
(println "STAGE2_PROMISE_OVERLAP_METADATA_RESULT"
         (pr-str (metadata-result)))

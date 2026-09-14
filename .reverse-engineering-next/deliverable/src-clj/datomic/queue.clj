(do
  (clojure.core/in-ns 'datomic.queue)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['take])
      (clojure.core/import 'java.util.concurrent.BlockingQueue)
      (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/import 'java.lang.ref.ReferenceQueue)))
  (when-not (.equals 'datomic.queue 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.queue))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['take])
        (clojure.core/import 'java.util.concurrent.BlockingQueue)
        (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/import 'java.lang.ref.ReferenceQueue))))
  (set! *warn-on-reflection* true)
  (defonce Producer {})
  (defprotocol Producer (offer-nb [sink item]))
  (defonce Consumer {})
  (defprotocol Consumer (poll-nb [source or-else]))
  (defonce BlockingProducer {})
  (defprotocol BlockingProducer (put [sink item]) (offer-b [sink item msec]))
  (defonce BlockingConsumer {})
  (defprotocol BlockingConsumer (take [source]) (poll-b [source or-else msec]))
  (defonce Clear {})
  (defprotocol Clear (clear [q]))
  (defn offer ([sink item msec] (offer-b sink item msec)) ([sink item] (offer-nb sink item)))
  (defn poll
    ([source or_else msec] (poll-b source or_else msec))
    ([source or_else] (poll-nb source or_else))
    ([source] (poll-nb source nil)))
  (extend
    java.util.concurrent.BlockingQueue
    Clear
    {:clear (fn fn__12120 ([q] (.clear ^java.util.Collection q) q))}
    Producer
    {:offer-nb (fn fn__12122 ([q item] (.offer ^java.util.concurrent.BlockingQueue q item)))}
    BlockingProducer
    {:put (fn fn__12124 ([q item] (.put ^java.util.concurrent.BlockingQueue q item) true)),
     :offer-b
     (fn fn__12126
       ([q item msec]
         (.offer
           ^java.util.concurrent.BlockingQueue q
           item
           (long ^java.lang.Number msec)
           TimeUnit/MILLISECONDS)))}
    Consumer
    {:poll-nb (fn fn__12128 ([q or_else] (or (.poll ^java.util.Queue q) or_else)))}
    BlockingConsumer
    {:take (fn fn__12131 ([q] (.take ^java.util.concurrent.BlockingQueue q))),
     :poll-b
     (fn fn__12133
       ([q or_else msec]
         (or
           (.poll
             ^java.util.concurrent.BlockingQueue q
             (long ^java.lang.Number msec)
             TimeUnit/MILLISECONDS)
           or_else)))})
  (extend
    java.lang.ref.ReferenceQueue
    Consumer
    {:poll-nb (fn fn__12136 ([q or_else] (or (.poll ^java.lang.ref.ReferenceQueue q) or_else)))}
    BlockingConsumer
    {:take (fn fn__12139 ([q] (.remove ^java.lang.ref.ReferenceQueue q))),
     :poll-b
     (fn fn__12141
       ([q or_else msec]
         (or (.remove ^java.lang.ref.ReferenceQueue q (long ^java.lang.Number msec)) or_else)))})
  (deftype
    DelayingQueue
    [delay delay_queue thread]
    datomic.queue.BlockingProducer
    java.io.Closeable
    clojure.lang.Counted
    (^void close [this] (do (put delay_queue delay_queue) (.join ^java.lang.Thread thread) nil))
    (^int count [this] (count delay_queue))
    (put
      [this item]
      (put delay_queue {:item item, :timestamp (+ delay (java.lang.System/currentTimeMillis))})))
  (clojure.core/import 'datomic.queue.DelayingQueue)
  (defn ->DelayingQueue
    ([delay delay_queue thread] (datomic.queue.DelayingQueue. delay delay_queue thread)))
  (defn delaying-queue
    ([msec dest_queue]
      (let [delay_queue (java.util.concurrent.LinkedBlockingQueue.)
            t (java.lang.Thread.
                (fn fn__12149
                  ([]
                    (loop []
                      (let [obj (take delay_queue)]
                        (when-not (= obj delay_queue)
                          (let [map__12150 obj
                                map__12150 (if (seq? map__12150)
                                             (clojure.lang.PersistentHashMap/create
                                               (seq map__12150))
                                             map__12150)
                                item (get map__12150 :item)
                                timestamp (get map__12150 :timestamp)]
                            (let [sleep (- timestamp (java.lang.System/currentTimeMillis))]
                              (when (> sleep 0)
                                (java.lang.Thread/sleep (long ^java.lang.Number sleep))))
                            (put dest_queue item)
                            (recur)))))
                    nil)))]
        (.start ^java.lang.Thread t)
        (datomic.queue.DelayingQueue. msec delay_queue t))))
  (defn queue-seq
    ([size]
      (let [q (java.util.concurrent.LinkedBlockingQueue. (int size))
            fill (fn fill ([s] (.put ^java.util.concurrent.BlockingQueue q s) nil))
            done (fn done ([] (.put ^java.util.concurrent.BlockingQueue q q) nil))
            drain (fn drain
                    ([]
                      (lazy-seq
                        (let [x (.take ^java.util.concurrent.BlockingQueue q)]
                          (when (instance? java.lang.Throwable x) (throw ^java.lang.Throwable x))
                          (when-not (identical? x q) (cons x (^clojure.lang.IFn drain)))))))]
        {:fill fill, :done done, :drain drain}))))
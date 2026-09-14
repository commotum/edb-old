(do
  (clojure.core/in-ns 'datomic.queue)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.queue)
    {:doc
     "Queue capabilities shared by transport and asynchronous processing. Protocols distinguish immediate, timed, and indefinitely blocking producer and consumer operations. Implementations cover Java blocking and reference queues, delayed forwarding, and a bounded queue-to-lazy-sequence bridge with producer backpressure."})
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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Producer (offer-nb [sink item] "Attempts immediate insertion and returns whether the item was accepted."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "Producer")
      (assoc
        (assoc protocol_metadata__7463 :doc "Nonblocking queue insertion.")
        :name
        'Producer
        :ns
        *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'offer-nb
                                        {:arglists (clojure.core/list ['sink 'item])}),
                                      :arglists (clojure.core/list ['sink 'item]),
                                      :doc
                                      "Attempts immediate insertion and returns whether the item was accepted."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "Producer"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "offer-nb")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol Consumer (poll-nb [source or-else] "Returns an immediately available item, or or-else when none is available."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "Consumer")
      (assoc
        (assoc protocol_metadata__7466 :doc "Nonblocking queue retrieval.")
        :name
        'Consumer
        :ns
        *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'poll-nb
                                        {:arglists (clojure.core/list ['source 'or-else])}),
                                      :arglists (clojure.core/list ['source 'or-else]),
                                      :doc
                                      "Returns an immediately available item, or or-else when none is available."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "Consumer"))
          protocol_method_name__7468 (with-meta
                                       (:name protocol_signature__7467)
                                       protocol_signature__7467)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "poll-nb")
        (assoc protocol_signature__7467 :name protocol_method_name__7468 :ns *ns*))))
  (let [protocol_metadata__7469 {:column (int 1)}]
    (defprotocol
      BlockingProducer
      (put [sink item] "Inserts item into sink, blocking until successful. Returns logical true.")
      (offer-b
        [sink item timeout-ms]
        "Waits up to timeout-ms milliseconds to insert item and returns whether it was accepted."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "BlockingProducer")
      (assoc
        (assoc protocol_metadata__7469 :doc "Blocking and timed queue insertion.")
        :name
        'BlockingProducer
        :ns
        *ns*))
    (let [protocol_signature__7470 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'put
                                        {:arglists (clojure.core/list ['sink 'item])}),
                                      :arglists (clojure.core/list ['sink 'item]),
                                      :doc
                                      "Inserts item into sink, blocking until successful. Returns logical true."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "BlockingProducer"))
          protocol_method_name__7471 (with-meta
                                       (:name protocol_signature__7470)
                                       protocol_signature__7470)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "put")
        (assoc protocol_signature__7470 :name protocol_method_name__7471 :ns *ns*)))
    (let [protocol_signature__7472 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'offer-b
                                        {:arglists (clojure.core/list ['sink 'item 'timeout-ms])}),
                                      :arglists (clojure.core/list ['sink 'item 'timeout-ms]),
                                      :doc
                                      "Waits up to timeout-ms milliseconds to insert item and returns whether it was accepted."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "BlockingProducer"))
          protocol_method_name__7473 (with-meta
                                       (:name protocol_signature__7472)
                                       protocol_signature__7472)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "offer-b")
        (assoc protocol_signature__7472 :name protocol_method_name__7473 :ns *ns*))))
  (let [protocol_metadata__7474 {:column (int 1)}]
    (defprotocol
      BlockingConsumer
      (take [source] "Retrieves item from source, blocking until available.")
      (poll-b
        [source or-else timeout-ms]
        "Waits up to timeout-ms milliseconds for an item and returns or-else on timeout."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "BlockingConsumer")
      (assoc
        (assoc protocol_metadata__7474 :doc "Blocking and timed queue retrieval.")
        :name
        'BlockingConsumer
        :ns
        *ns*))
    (let [protocol_signature__7475 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'take {:arglists (clojure.core/list ['source])}),
                                      :arglists (clojure.core/list ['source]),
                                      :doc "Retrieves item from source, blocking until available."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "BlockingConsumer"))
          protocol_method_name__7476 (with-meta
                                       (:name protocol_signature__7475)
                                       protocol_signature__7475)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "take")
        (assoc protocol_signature__7475 :name protocol_method_name__7476 :ns *ns*)))
    (let [protocol_signature__7477 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'poll-b
                                        {:arglists
                                         (clojure.core/list ['source 'or-else 'timeout-ms])}),
                                      :arglists
                                      (clojure.core/list ['source 'or-else 'timeout-ms]),
                                      :doc
                                      "Waits up to timeout-ms milliseconds for an item and returns or-else on timeout."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "BlockingConsumer"))
          protocol_method_name__7478 (with-meta
                                       (:name protocol_signature__7477)
                                       protocol_signature__7477)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "poll-b")
        (assoc protocol_signature__7477 :name protocol_method_name__7478 :ns *ns*))))
  (let [protocol_metadata__7479 {:column (int 1)}]
    (defprotocol Clear (clear [q] "Clear all items from queue, returning queue."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "Clear")
      (assoc
        (assoc protocol_metadata__7479 :doc "Removal of all currently queued items.")
        :name
        'Clear
        :ns
        *ns*))
    (let [protocol_signature__7480 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'clear {:arglists (clojure.core/list ['q])}),
                                      :arglists (clojure.core/list ['q]),
                                      :doc "Clear all items from queue, returning queue."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "Clear"))
          protocol_method_name__7481 (with-meta
                                       (:name protocol_signature__7480)
                                       protocol_signature__7480)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "clear")
        (assoc protocol_signature__7480 :name protocol_method_name__7481 :ns *ns*))))
  (defn offer
    ([sink item timeout-ms] (offer-b sink item timeout-ms))
    ([sink item] (offer-nb sink item)))
  (reset-meta!
    #'offer
    (assoc
      {:arglists (clojure.core/list ['sink 'item] ['sink 'item 'timeout-ms]),
       :doc
       "Attempts to insert item into sink. The two-argument form returns immediately; the timed form waits up to timeout-ms milliseconds. Both return whether the item was accepted.",
       :column (int 1)}
      :name
      'offer
      :ns
      *ns*))
  (defn poll
    ([source or-else timeout-ms] (poll-b source or-else timeout-ms))
    ([source or-else] (poll-nb source or-else))
    ([source] (poll-nb source nil)))
  (reset-meta!
    #'poll
    (assoc
      {:arglists
       (clojure.core/list ['source] ['source 'or-else] ['source 'or-else 'timeout-ms]),
       :doc
       "Retrieves an item from source without waiting, or waits up to timeout-ms in the timed form. Returns or-else, nil by default, when no item is available.",
       :column (int 1)}
      :name
      'poll
      :ns
      *ns*))
  (extend
    java.util.concurrent.BlockingQueue
    Clear
    {:clear (fn fn__11419 ([q] (.clear ^java.util.Collection q) q))}
    Producer
    {:offer-nb (fn fn__11421 ([q item] (.offer ^java.util.concurrent.BlockingQueue q item)))}
    BlockingProducer
    {:put (fn fn__11423 ([q item] (.put ^java.util.concurrent.BlockingQueue q item) true)),
     :offer-b
     (fn fn__11425
       ([q item timeout-ms]
         (.offer
           ^java.util.concurrent.BlockingQueue q
           item
           (long ^java.lang.Number timeout-ms)
           TimeUnit/MILLISECONDS)))}
    Consumer
    {:poll-nb (fn fn__11427 ([q or-else] (or (.poll ^java.util.Queue q) or-else)))}
    BlockingConsumer
    {:take (fn fn__11430 ([q] (.take ^java.util.concurrent.BlockingQueue q))),
     :poll-b
     (fn fn__11432
       ([q or-else timeout-ms]
         (or
           (.poll
             ^java.util.concurrent.BlockingQueue q
             (long ^java.lang.Number timeout-ms)
             TimeUnit/MILLISECONDS)
           or-else)))})
  (extend
    java.lang.ref.ReferenceQueue
    Consumer
    {:poll-nb (fn fn__11435 ([q or-else] (or (.poll ^java.lang.ref.ReferenceQueue q) or-else)))}
    BlockingConsumer
    {:take (fn fn__11438 ([q] (.remove ^java.lang.ref.ReferenceQueue q))),
     :poll-b
     (fn fn__11440
       ([q or-else timeout-ms]
         (or
           (.remove ^java.lang.ref.ReferenceQueue q (long ^java.lang.Number timeout-ms))
           or-else)))})
  (deftype
    DelayingQueue
    [delay-ms delay-queue thread]
    datomic.queue.BlockingProducer
    java.io.Closeable
    clojure.lang.Counted
    (^void close [this] (do (put delay-queue delay-queue) (.join ^java.lang.Thread thread) nil))
    (^int count [this] (count delay-queue))
    (put
      [this item]
      (put
        delay-queue
        {:item item, :timestamp (+ delay-ms (java.lang.System/currentTimeMillis))})))
  (clojure.core/import 'datomic.queue.DelayingQueue)
  (defn ->DelayingQueue
    ([delay-ms delay-queue thread]
      (datomic.queue.DelayingQueue. delay-ms delay-queue thread)))
  (reset-meta!
    #'->DelayingQueue
    (assoc
      {:arglists (clojure.core/list ['delay-ms 'delay-queue 'thread]),
       :doc "Low-level constructor for a delayed forwarding queue and its worker thread.",
       :column (int 1)}
      :name
      '->DelayingQueue
      :ns
      *ns*))
  (defn delaying-queue
    ([delay-ms destination]
      (let [delay-queue (java.util.concurrent.LinkedBlockingQueue.)
            worker (java.lang.Thread.
                (fn fn__11448
                  ([]
                    (loop []
                      (let [entry (take delay-queue)]
                        (when-not (= entry delay-queue)
                          (let [{:keys [item timestamp]} entry]
                            (let [sleep (- timestamp (java.lang.System/currentTimeMillis))]
                              (when (> sleep 0)
                                (java.lang.Thread/sleep (long ^java.lang.Number sleep))))
                            (put destination item)
                            (recur))))))))]
        (.start ^java.lang.Thread worker)
        (datomic.queue.DelayingQueue. delay-ms delay-queue worker))))
  (reset-meta!
    #'delaying-queue
    (assoc
      {:arglists (clojure.core/list ['delay-ms 'destination]),
       :doc
       "Returns a Closeable BlockingProducer that forwards items to destination in FIFO order no earlier than delay-ms after insertion. The internal queue is unbounded; blocking in destination applies backpressure to later items. Closing waits for items queued before the close marker to be forwarded and for the worker to stop.",
       :column (int 1)}
      :name
      'delaying-queue
      :ns
      *ns*))
  (defn queue-seq
    ([capacity]
      (let [q (java.util.concurrent.LinkedBlockingQueue. (int capacity))
            fill (fn fill ([s] (.put ^java.util.concurrent.BlockingQueue q s) nil))
            done (fn done ([] (.put ^java.util.concurrent.BlockingQueue q q) nil))
            drain (fn drain
                    ([]
                      (lazy-seq
                        (let [x (.take ^java.util.concurrent.BlockingQueue q)]
                          (when (instance? java.lang.Throwable x) (throw ^java.lang.Throwable x))
                          (when-not (identical? x q) (cons x (^clojure.lang.IFn drain)))))))]
        {:fill fill, :done done, :drain drain})))
  (reset-meta!
    #'queue-seq
    (assoc
      {:arglists (clojure.core/list ['capacity]),
       :doc
       "Creates a bounded producer-to-sequence bridge. Returns :fill, a blocking one-item producer; :done, a blocking end marker; and :drain, a function returning a lazy blocking sequence. Capacity controls backpressure. A Throwable supplied to :fill is thrown when drained, and values after :done are not visible.",
       :column (int 1)}
      :name
      'queue-seq
      :ns
      *ns*)))

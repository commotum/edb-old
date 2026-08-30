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
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol Producer (offer-nb [sink item] "Implementaion detail. See offer."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "Producer")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'Producer :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'offer-nb
                                        {:arglists (clojure.core/list ['sink 'item])}),
                                      :arglists (clojure.core/list ['sink 'item]),
                                      :doc "Implementaion detail. See offer."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.queue" "Producer"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.queue" "offer-nb")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (let [protocol_metadata__7466 {:column (int 1)}]
    (defprotocol Consumer (poll-nb [source or-else] "Implementation detail. See poll."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "Consumer")
      (assoc (assoc protocol_metadata__7466 :doc nil) :name 'Consumer :ns *ns*))
    (let [protocol_signature__7467 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'poll-nb
                                        {:arglists (clojure.core/list ['source 'or-else])}),
                                      :arglists (clojure.core/list ['source 'or-else]),
                                      :doc "Implementation detail. See poll."}
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
      (offer-b [sink item msec] "Implementation detail, see offer."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "BlockingProducer")
      (assoc (assoc protocol_metadata__7469 :doc nil) :name 'BlockingProducer :ns *ns*))
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
                                        {:arglists (clojure.core/list ['sink 'item 'msec])}),
                                      :arglists (clojure.core/list ['sink 'item 'msec]),
                                      :doc "Implementation detail, see offer."}
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
      (poll-b [source or-else msec] "Implementation detail, see poll."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.queue" "BlockingConsumer")
      (assoc (assoc protocol_metadata__7474 :doc nil) :name 'BlockingConsumer :ns *ns*))
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
                                        {:arglists (clojure.core/list ['source 'or-else 'msec])}),
                                      :arglists (clojure.core/list ['source 'or-else 'msec]),
                                      :doc "Implementation detail, see poll."}
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
      (assoc (assoc protocol_metadata__7479 :doc nil) :name 'Clear :ns *ns*))
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
  (defn offer ([sink item msec] (offer-b sink item msec)) ([sink item] (offer-nb sink item)))
  (reset-meta!
    #'offer
    (assoc
      {:arglists (clojure.core/list ['sink 'item] ['sink 'item 'msec]), :column (int 1)}
      :name
      'offer
      :ns
      *ns*))
  (defn poll
    ([source or_else msec] (poll-b source or_else msec))
    ([source or_else] (poll-nb source or_else))
    ([source] (poll-nb source nil)))
  (reset-meta!
    #'poll
    (assoc
      {:arglists (clojure.core/list ['source] ['source 'or-else] ['source 'or-else 'msec]),
       :column (int 1)}
      :name
      'poll
      :ns
      *ns*))
  (extend
    java.util.concurrent.BlockingQueue
    Clear
    {:clear (fn fn__10986 ([q] (.clear ^java.util.Collection q) q))}
    Producer
    {:offer-nb (fn fn__10988 ([q item] (.offer ^java.util.concurrent.BlockingQueue q item)))}
    BlockingProducer
    {:put (fn fn__10990 ([q item] (.put ^java.util.concurrent.BlockingQueue q item) true)),
     :offer-b
     (fn fn__10992
       ([q item msec]
         (.offer
           ^java.util.concurrent.BlockingQueue q
           item
           (long ^java.lang.Number msec)
           TimeUnit/MILLISECONDS)))}
    Consumer
    {:poll-nb (fn fn__10994 ([q or_else] (or (.poll ^java.util.Queue q) or_else)))}
    BlockingConsumer
    {:take (fn fn__10997 ([q] (.take ^java.util.concurrent.BlockingQueue q))),
     :poll-b
     (fn fn__10999
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
    {:poll-nb (fn fn__11002 ([q or_else] (or (.poll ^java.lang.ref.ReferenceQueue q) or_else)))}
    BlockingConsumer
    {:take (fn fn__11005 ([q] (.remove ^java.lang.ref.ReferenceQueue q))),
     :poll-b
     (fn fn__11007
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
  (reset-meta!
    #'->DelayingQueue
    (assoc
      {:arglists (clojure.core/list ['delay 'delay-queue 'thread]), :column (int 1)}
      :name
      '->DelayingQueue
      :ns
      *ns*))
  (defn delaying-queue
    ([msec dest_queue]
      (let [delay_queue (java.util.concurrent.LinkedBlockingQueue.)
            t (java.lang.Thread.
                (fn fn__11015
                  ([]
                    (loop []
                      (let [obj (take delay_queue)]
                        (when-not (= obj delay_queue)
                          (let [map__11016 obj
                                map__11016 (if (seq? map__11016)
                                             (if (next map__11016)
                                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                 (to-array map__11016))
                                               (if (seq map__11016) (first map__11016) {}))
                                             map__11016)
                                item (get map__11016 :item)
                                timestamp (get map__11016 :timestamp)]
                            (let [sleep (- timestamp (java.lang.System/currentTimeMillis))]
                              (when (> sleep 0)
                                (java.lang.Thread/sleep (long ^java.lang.Number sleep))))
                            (put dest_queue item)
                            (recur))))))))]
        (.start ^java.lang.Thread t)
        (datomic.queue.DelayingQueue. msec delay_queue t))))
  (reset-meta!
    #'delaying-queue
    (assoc
      {:arglists (clojure.core/list ['msec 'dest-queue]), :column (int 1)}
      :name
      'delaying-queue
      :ns
      *ns*))
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
        {:fill fill, :done done, :drain drain})))
  (reset-meta!
    #'queue-seq
    (assoc {:arglists (clojure.core/list ['size]), :column (int 1)} :name 'queue-seq :ns *ns*)))
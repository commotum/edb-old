(ns datomic-rev.stage2-trivial-overlap-probe
  (:require [datomic.cache.impl :as cache]
            [datomic.kv-store :as kv-store]
            [datomic.simple-kv :as simple-kv]
            [datomic.valcache.puts-pool :as puts-pool])
  (:import [clojure.lang MultiFn Var]
           [java.nio ByteBuffer]
           [java.util.concurrent ConcurrentHashMap]))

(defn fail! [message data]
  (throw (ex-info message data)))

(defn bytes-of [^ByteBuffer buffer]
  (let [copy (.duplicate buffer)
        bytes (byte-array (.remaining copy))]
    (.get copy bytes)
    (mapv #(bit-and 255 (int %)) bytes)))

(defn require-equal! [label expected actual]
  (when-not (= expected actual)
    (fail! "trivial overlap probe mismatch"
           {:label label :expected expected :actual actual})))

(defn cache-result []
  (let [value (ConcurrentHashMap.)
        first-put (cache/put value :key :first)
        second-put (cache/put value :key :second)
        count-after-put (cache/fast-count value)
        removed (cache/remove value :key)
        count-after-remove (cache/fast-count value)
        _ (cache/put value :one 1)
        _ (cache/put value :two 2)
        cleared (cache/clear value)]
    (require-equal! :cache-state
                    [nil :first 1 :second 0 nil 0]
                    [first-put second-put count-after-put removed
                     count-after-remove cleared (count value)])
    [first-put second-put count-after-put removed
     count-after-remove cleared (count value)]))

(defn kv-store-result []
  (let [retry-var ^Var #'kv-store/*retry*
        result [(.isDynamic retry-var)
                (bound? retry-var)
                (binding [kv-store/*retry* :bound] kv-store/*retry*)
                (kv-store/retryable? (Exception. "ordinary"))
                (kv-store/retryable? (InterruptedException. "interrupt"))]]
    (require-equal! :kv-store [true false :bound true false] result)
    result))

(defn puts-pool-result []
  (let [dispatch :datomic-rev.stage2/probe
        multifn ^MultiFn @#'puts-pool/get-from-put
        pool (reify puts-pool/PutsPool
               (submit [_ key data f]
                 [:submitted key (:source data) (f)])
               (get-queued-put [_ key]
                 (when (= key :present)
                   {:data {:source dispatch :value 17}})))]
    (.addMethod multifn dispatch
                (fn [value] [:custom (:value value)]))
    (try
      (let [result [(puts-pool/get-from-put {:source :unknown})
                    (puts-pool/get-from-put
                      {:source dispatch :value 11})
                    (puts-pool/get-from-queued-put pool :present)
                    (puts-pool/get-from-queued-put pool :absent)
                    (puts-pool/submit pool :key {:source dispatch}
                                      (fn [] :ran))]]
        (require-equal! :puts-pool
                        [nil [:custom 11] [:custom 17] nil
                         [:submitted :key dispatch :ran]]
                        result)
        result)
      (finally
        (.removeMethod multifn dispatch)))))

(defn simple-kv-result []
  (let [metadata (array-map :kind :probe :nested [true nil 9])
        payload-bytes [0 1 127 128 255]
        payload (ByteBuffer/wrap (byte-array [0 1 127 -128 -1]))
        packed (simple-kv/pack metadata payload)
        packed-bytes (bytes-of packed)
        unpacked (simple-kv/unpack :item (.duplicate packed))
        unpacked-value (bytes-of (:v unpacked))
        raw (ByteBuffer/wrap (byte-array [9 8 7]))
        raw-unpacked (simple-kv/unpack :raw raw)
        exact-eight (ByteBuffer/allocate 8)
        _ (.putLong exact-eight simple-kv/map-magic)
        _ (.flip exact-eight)
        exact-eight-unpacked (simple-kv/unpack :eight exact-eight)
        store (reify simple-kv/KV
                (put [_ key value] [:put key (bytes-of value)])
                (get [_ key]
                  (when (= key :present)
                    (ByteBuffer/wrap (byte-array [4 5 6]))))
                (delete [_ key] [:delete key]))
        result [packed-bytes
                [(:id unpacked) (:kind unpacked) (:nested unpacked)
                 unpacked-value]
                [(:id raw-unpacked) (identical? raw (:v raw-unpacked))
                 (bytes-of (:v raw-unpacked))]
                [(:id exact-eight-unpacked)
                 (identical? exact-eight (:v exact-eight-unpacked))
                 (bytes-of (:v exact-eight-unpacked))]
                (bytes-of (simple-kv/get-with-retry store :present))
                (simple-kv/put store :write
                               (ByteBuffer/wrap (byte-array [2 3])))
                (simple-kv/delete store :write)]]
    (require-equal! :simple-kv-unpacked
                    [:item :probe [true nil 9] payload-bytes]
                    (second result))
    (require-equal! :simple-kv-raw
                    [:raw true [9 8 7]]
                    (nth result 2))
    (require-equal! :simple-kv-eight-byte-boundary
                    [:eight true (bytes-of exact-eight)]
                    (nth result 3))
    (require-equal! :simple-kv-protocol
                    [[4 5 6] [:put :write [2 3]] [:delete :write]]
                    (subvec (vec result) 4))
    result))

(let [result [[:cache (cache-result)]
              [:kv-store (kv-store-result)]
              [:puts-pool (puts-pool-result)]
              [:simple-kv (simple-kv-result)]]]
  (println "STAGE2_TRIVIAL_OVERLAP_RESULT" (pr-str result)))

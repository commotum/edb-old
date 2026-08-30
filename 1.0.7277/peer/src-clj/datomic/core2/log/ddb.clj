(do
  (clojure.core/in-ns 'datomic.core2.log.ddb)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.core2.log.ddb)
    {:doc "Implementaion of datomic.core2.log backed by DynamoDB."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/require
        ['clojure.core.async :as 'a :refer (clojure.core/list '>! '<! 'go 'go-loop)]
        ['clojure.edn :as 'edn]
        ['cognitect.aws.client.api :as 'aws]
        ['datomic.core2.anomalies :refer (clojure.core/list 'anom)]
        ['datomic.core2.async :as 'da]
        ['datomic.core2.aws.ddb :as 'ddb]
        ['datomic.core2.log.spi :as 'spi]
        ['datomic.java.io :as 'dio])))
  (when-not (.equals 'datomic.core2.log.ddb 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.core2.log.ddb))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/require
          ['clojure.core.async :as 'a :refer (clojure.core/list '>! '<! 'go 'go-loop)]
          ['clojure.edn :as 'edn]
          ['cognitect.aws.client.api :as 'aws]
          ['datomic.core2.anomalies :refer (clojure.core/list 'anom)]
          ['datomic.core2.async :as 'da]
          ['datomic.core2.aws.ddb :as 'ddb]
          ['datomic.core2.log.spi :as 'spi]
          ['datomic.java.io :as 'dio]))))
  (defn append-request
    ([table p header body]
      (ddb/conditional-put-request
        {:table table,
         :p :p,
         :r :r,
         :item {:p p, :r (:t header), :header (dio/clj->str (dissoc header :t)), :body body}})))
  (reset-meta!
    #'append-request
    (assoc
      {:private true, :arglists (clojure.core/list ['table 'p 'header 'body]), :column 1}
      :name
      'append-request
      :ns
      *ns*))
  (defn ddb-item->log-item
    ([ddb_item]
      (let [map__20639 (ddb/de-item-map ddb_item)
            map__20639 (if (seq? map__20639)
                         (if (next map__20639)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20639))
                           (if (seq map__20639) (first map__20639) {}))
                         map__20639)
            r (get map__20639 :r)
            header (get map__20639 :header)
            body (get map__20639 :body)]
        (cond-> {:header (assoc (edn/read-string header) :t r)} body (assoc :body body)))))
  (reset-meta!
    #'ddb-item->log-item
    (assoc
      {:private true, :arglists (clojure.core/list ['ddb-item]), :column 1}
      :name
      'ddb-item->log-item
      :ns
      *ns*))
  (deftype
    Log
    [client table p chunk_size]
    datomic.core2.log.spi.Append
    datomic.core2.log.spi.Item
    datomic.core2.log.spi.Scan
    (-scan
      [this opts]
      (let [map__20643 opts
            map__20643 (if (seq? map__20643)
                         (if (next map__20643)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20643))
                           (if (seq map__20643) (first map__20643) {}))
                         map__20643)
            ch (get map__20643 :ch)
            direction (get map__20643 :direction)
            limit (get map__20643 :limit)
            t (get map__20643 :t)
            query (fn query
                    ([forward? r limit]
                      (aws/invoke-async
                        client
                        (ddb/query-range-request
                          {:table table,
                           :p :p,
                           :r :r,
                           :forward forward?,
                           :attrs {:p p, :r r},
                           :limit limit}))))
            next_r (fn next_r
                     ([resp]
                       (let [temp__5804__auto__ (let [G__20647 resp
                                                      G__20647 (some->
                                                                 G__20647
                                                                 (:LastEvaluatedKey))
                                                      G__20647 (some-> G__20647 (:r))
                                                      G__20647 (some-> G__20647 (:N))]
                                                  (when-not (nil? G__20647)
                                                    (long
                                                      (java.lang.Long/parseLong
                                                        ^java.lang.String G__20647))))]
                         (when temp__5804__auto__
                           (let [r temp__5804__auto__ G__20648 direction]
                             (case G__20648 :forward (inc r) :backward (dec r)))))))
            c__10230__auto__ (a/chan 1)
            captured_bindings__10231__auto__ (clojure.lang.Var/getThreadBindingFrame)]
        (clojure.core.async.impl.dispatch/run
          (fn fn__20770
            ([]
              (let [G__20651 (fn G__20651 ([] t))
                    G__20652 (fn G__20652 ([] query))
                    G__20653 (fn G__20653 ([] chunk_size))
                    G__20654 (fn G__20654 ([] direction))
                    G__20655 (fn G__20655 ([] p))
                    G__20656 (fn G__20656 ([] next_r))
                    G__20657 (fn G__20657 ([] table))
                    G__20658 (fn G__20658 ([] limit))
                    G__20659 (fn G__20659 ([] this))
                    G__20660 (fn G__20660 ([] client))
                    G__20661 (fn G__20661 ([] ch))
                    G__20662 (fn G__20662 ([] map__20643))
                    G__20663 (fn G__20663 ([] opts))
                    f__10232__auto__ (fn state_machine__9975__auto__
                                       ([]
                                         (let [statearr_20798 (java.util.concurrent.atomic.AtomicReferenceArray.
                                                                (int 29))]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20798
                                             0
                                             state_machine__9975__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20798
                                             1
                                             1)
                                           statearr_20798))
                                       ([state_20769]
                                         (let [old_frame__9976__auto__ (clojure.lang.Var/getThreadBindingFrame)
                                               ret_value__9977__auto__ (try
                                                                         (do
                                                                           (clojure.lang.Var/resetThreadBindingFrame
                                                                             (clojure.core.async.impl.ioc-macros/aget-object
                                                                               state_20769
                                                                               3))
                                                                           (loop 
                                                                             []
                                                                             (let 
                                                                               [result__9978__auto__
                                                                                (let 
                                                                                  [G__20801
                                                                                   (int
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_20769
                                                                                       1))]
                                                                                  (case
                                                                                    G__20801
                                                                                    10
                                                                                    (let 
                                                                                      [inst_20765
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20813
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20813
                                                                                          2
                                                                                          inst_20765)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20813
                                                                                          1
                                                                                          7))
                                                                                      :recur)
                                                                                    28
                                                                                    (let 
                                                                                      [inst_20747
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20839
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20839
                                                                                          2
                                                                                          inst_20747)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20839
                                                                                          1
                                                                                          25))
                                                                                      :recur)
                                                                                    16
                                                                                    (let 
                                                                                      [inst_20721
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         9)
                                                                                       inst_20724
                                                                                       (not
                                                                                         inst_20721)]
                                                                                      (let 
                                                                                        [statearr_20821
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20821
                                                                                          2
                                                                                          inst_20724)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20821
                                                                                          1
                                                                                          18))
                                                                                      :recur)
                                                                                    27
                                                                                    (let 
                                                                                      [inst_20743
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         13)]
                                                                                      (let 
                                                                                        [statearr_20838
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20838
                                                                                          2
                                                                                          inst_20743)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20838
                                                                                          1
                                                                                          28))
                                                                                      :recur)
                                                                                    9
                                                                                    (let 
                                                                                      [inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20710
                                                                                       (:Items
                                                                                         inst_20701)
                                                                                       inst_20711
                                                                                       (seq
                                                                                         inst_20710)]
                                                                                      (if
                                                                                        inst_20711
                                                                                        (let 
                                                                                          [statearr_20811
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20811
                                                                                            1
                                                                                            12))
                                                                                        (let 
                                                                                          [statearr_20812
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20812
                                                                                            1
                                                                                            13)))
                                                                                      :recur)
                                                                                    17
                                                                                    (let 
                                                                                      [inst_20722
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         27)]
                                                                                      (let 
                                                                                        [statearr_20822
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20822
                                                                                          2
                                                                                          inst_20722)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20822
                                                                                          1
                                                                                          18))
                                                                                      :recur)
                                                                                    14
                                                                                    (let 
                                                                                      [inst_20702
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         26)
                                                                                       inst_20722
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         27)
                                                                                       inst_20720
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)
                                                                                       inst_20721
                                                                                       inst_20720
                                                                                       inst_20722
                                                                                       inst_20702
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20817
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20817
                                                                                           9
                                                                                           inst_20721)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20817
                                                                                           27
                                                                                           inst_20722)
                                                                                         statearr_20817)]
                                                                                      (if
                                                                                        inst_20722
                                                                                        (let 
                                                                                          [statearr_20818
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20818
                                                                                            1
                                                                                            16))
                                                                                        (let 
                                                                                          [statearr_20819
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20819
                                                                                            1
                                                                                            17)))
                                                                                      :recur)
                                                                                    19
                                                                                    (let 
                                                                                      [inst_20682
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         17)
                                                                                       inst_20680
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         15)
                                                                                       inst_20679
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         14)
                                                                                       inst_20700
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         7)
                                                                                       inst_20729
                                                                                       (=
                                                                                         inst_20682
                                                                                         :forward)
                                                                                       inst_20730
                                                                                       (^clojure.lang.IFn inst_20680
                                                                                         inst_20729
                                                                                         inst_20679
                                                                                         inst_20700)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20769
                                                                                        22
                                                                                        inst_20730))
                                                                                    11
                                                                                    (let 
                                                                                      [inst_20679
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         14)
                                                                                       inst_20680
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         15)
                                                                                       inst_20681
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         16)
                                                                                       inst_20702
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         26)
                                                                                       inst_20682
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         17)
                                                                                       inst_20683
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         18)
                                                                                       inst_20684
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         8)
                                                                                       inst_20685
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         19)
                                                                                       inst_20700
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         7)
                                                                                       inst_20687
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         21)
                                                                                       inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20688
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         22)
                                                                                       inst_20689
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         23)
                                                                                       inst_20690
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         24)
                                                                                       inst_20691
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         25)
                                                                                       inst_20707
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)
                                                                                       t inst_20679
                                                                                       query
                                                                                       inst_20680
                                                                                       chunk_size
                                                                                       inst_20681
                                                                                       first_pass?
                                                                                       inst_20702
                                                                                       direction
                                                                                       inst_20682
                                                                                       p inst_20683
                                                                                       next_r
                                                                                       inst_20684
                                                                                       table
                                                                                       inst_20685
                                                                                       limit
                                                                                       inst_20700
                                                                                       _ inst_20687
                                                                                       resp
                                                                                       inst_20701
                                                                                       client
                                                                                       inst_20688
                                                                                       ch
                                                                                       inst_20689
                                                                                       map__20643
                                                                                       inst_20690
                                                                                       opts
                                                                                       inst_20691
                                                                                       inst_20708
                                                                                       (a/close!
                                                                                         ch)
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20814
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20814
                                                                                           28
                                                                                           inst_20707)
                                                                                         statearr_20814)]
                                                                                      (let 
                                                                                        [statearr_20815
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20815
                                                                                          2
                                                                                          inst_20708)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20815
                                                                                          1
                                                                                          10))
                                                                                      :recur)
                                                                                    23
                                                                                    (let 
                                                                                      [inst_20738
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         11)
                                                                                       inst_20743
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         13)
                                                                                       inst_20742
                                                                                       (clojure.lang.Numbers/isPos
                                                                                         inst_20738)
                                                                                       inst_20743
                                                                                       inst_20742
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20831
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20831
                                                                                           13
                                                                                           inst_20743)
                                                                                         statearr_20831)]
                                                                                      (if
                                                                                        inst_20743
                                                                                        (let 
                                                                                          [statearr_20832
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20832
                                                                                            1
                                                                                            26))
                                                                                        (let 
                                                                                          [statearr_20833
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20833
                                                                                            1
                                                                                            27)))
                                                                                      :recur)
                                                                                    6
                                                                                    (let 
                                                                                      [inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20704
                                                                                       (datomic.core2.anomalies/anom
                                                                                         inst_20701)]
                                                                                      (if
                                                                                        inst_20704
                                                                                        (let 
                                                                                          [statearr_20809
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20809
                                                                                            1
                                                                                            8))
                                                                                        (let 
                                                                                          [statearr_20810
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20810
                                                                                            1
                                                                                            9)))
                                                                                      :recur)
                                                                                    25
                                                                                    (let 
                                                                                      [inst_20750
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (if
                                                                                        inst_20750
                                                                                        (let 
                                                                                          [statearr_20835
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20835
                                                                                            1
                                                                                            29))
                                                                                        (let 
                                                                                          [statearr_20836
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20836
                                                                                            1
                                                                                            30)))
                                                                                      :recur)
                                                                                    26
                                                                                    (let 
                                                                                      [inst_20739
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         12)]
                                                                                      (let 
                                                                                        [statearr_20837
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20837
                                                                                          2
                                                                                          inst_20739)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20837
                                                                                          1
                                                                                          28))
                                                                                      :recur)
                                                                                    13
                                                                                    (do
                                                                                      (let 
                                                                                        [statearr_20816
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20816
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20816
                                                                                          1
                                                                                          14))
                                                                                      :recur)
                                                                                    12
                                                                                    (let 
                                                                                      [inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20689
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         23)
                                                                                       inst_20713
                                                                                       (:Items
                                                                                         inst_20701)
                                                                                       inst_20714
                                                                                       (map
                                                                                         ddb-item->log-item
                                                                                         inst_20713)
                                                                                       inst_20715
                                                                                       (da/put-all!
                                                                                         inst_20689
                                                                                         inst_20714
                                                                                         false)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20769
                                                                                        15
                                                                                        inst_20715))
                                                                                    1
                                                                                    (let 
                                                                                      [inst_20679
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         14)
                                                                                       inst_20680
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         15)
                                                                                       inst_20681
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         16)
                                                                                       inst_20682
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         17)
                                                                                       inst_20683
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         18)
                                                                                       inst_20684
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         8)
                                                                                       inst_20685
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         19)
                                                                                       inst_20686
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         20)
                                                                                       inst_20687
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         21)
                                                                                       inst_20688
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         22)
                                                                                       inst_20689
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         23)
                                                                                       inst_20690
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         24)
                                                                                       inst_20691
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         25)
                                                                                       inst_20666
                                                                                       (^clojure.lang.IFn G__20651)
                                                                                       t inst_20666
                                                                                       inst_20667
                                                                                       (^clojure.lang.IFn G__20652)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       inst_20668
                                                                                       (^clojure.lang.IFn G__20653)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       inst_20669
                                                                                       (^clojure.lang.IFn G__20654)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       inst_20670
                                                                                       (^clojure.lang.IFn G__20655)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       inst_20671
                                                                                       (^clojure.lang.IFn G__20656)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       inst_20672
                                                                                       (^clojure.lang.IFn G__20657)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       table
                                                                                       inst_20672
                                                                                       inst_20673
                                                                                       (^clojure.lang.IFn G__20658)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       table
                                                                                       inst_20672
                                                                                       limit
                                                                                       inst_20673
                                                                                       inst_20674
                                                                                       (^clojure.lang.IFn G__20659)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       table
                                                                                       inst_20672
                                                                                       limit
                                                                                       inst_20673
                                                                                       _ inst_20674
                                                                                       inst_20675
                                                                                       (^clojure.lang.IFn G__20660)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       table
                                                                                       inst_20672
                                                                                       limit
                                                                                       inst_20673
                                                                                       _ inst_20674
                                                                                       client
                                                                                       inst_20675
                                                                                       inst_20676
                                                                                       (^clojure.lang.IFn G__20661)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       table
                                                                                       inst_20672
                                                                                       limit
                                                                                       inst_20673
                                                                                       _ inst_20674
                                                                                       client
                                                                                       inst_20675
                                                                                       ch
                                                                                       inst_20676
                                                                                       inst_20677
                                                                                       (^clojure.lang.IFn G__20662)
                                                                                       t inst_20666
                                                                                       query
                                                                                       inst_20667
                                                                                       chunk_size
                                                                                       inst_20668
                                                                                       direction
                                                                                       inst_20669
                                                                                       p inst_20670
                                                                                       next_r
                                                                                       inst_20671
                                                                                       table
                                                                                       inst_20672
                                                                                       limit
                                                                                       inst_20673
                                                                                       _ inst_20674
                                                                                       client
                                                                                       inst_20675
                                                                                       ch
                                                                                       inst_20676
                                                                                       map__20643
                                                                                       inst_20677
                                                                                       inst_20678
                                                                                       (^clojure.lang.IFn G__20663)
                                                                                       inst_20679
                                                                                       inst_20666
                                                                                       inst_20680
                                                                                       inst_20667
                                                                                       inst_20681
                                                                                       inst_20668
                                                                                       inst_20682
                                                                                       inst_20669
                                                                                       inst_20683
                                                                                       inst_20670
                                                                                       inst_20684
                                                                                       inst_20671
                                                                                       inst_20685
                                                                                       inst_20672
                                                                                       inst_20686
                                                                                       inst_20673
                                                                                       inst_20687
                                                                                       inst_20674
                                                                                       inst_20688
                                                                                       inst_20675
                                                                                       inst_20689
                                                                                       inst_20676
                                                                                       inst_20690
                                                                                       inst_20677
                                                                                       inst_20691
                                                                                       inst_20678
                                                                                       t inst_20679
                                                                                       query
                                                                                       inst_20680
                                                                                       chunk_size
                                                                                       inst_20681
                                                                                       direction
                                                                                       inst_20682
                                                                                       p inst_20683
                                                                                       next_r
                                                                                       inst_20684
                                                                                       table
                                                                                       inst_20685
                                                                                       limit
                                                                                       inst_20686
                                                                                       _ inst_20687
                                                                                       client
                                                                                       inst_20688
                                                                                       ch
                                                                                       inst_20689
                                                                                       map__20643
                                                                                       inst_20690
                                                                                       opts
                                                                                       inst_20691
                                                                                       inst_20692
                                                                                       (=
                                                                                         direction
                                                                                         :forward)
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20802
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           14
                                                                                           inst_20679)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           15
                                                                                           inst_20680)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           16
                                                                                           inst_20681)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           17
                                                                                           inst_20682)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           18
                                                                                           inst_20683)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           8
                                                                                           inst_20684)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           19
                                                                                           inst_20685)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           20
                                                                                           inst_20686)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           21
                                                                                           inst_20687)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           22
                                                                                           inst_20688)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           23
                                                                                           inst_20689)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           24
                                                                                           inst_20690)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20802
                                                                                           25
                                                                                           inst_20691)
                                                                                         statearr_20802)]
                                                                                      (if
                                                                                        inst_20692
                                                                                        (let 
                                                                                          [statearr_20803
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20803
                                                                                            1
                                                                                            2))
                                                                                        (let 
                                                                                          [statearr_20804
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20804
                                                                                            1
                                                                                            3)))
                                                                                      :recur)
                                                                                    30
                                                                                    (let 
                                                                                      [inst_20679
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         14)
                                                                                       inst_20680
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         15)
                                                                                       inst_20681
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         16)
                                                                                       inst_20702
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         26)
                                                                                       inst_20682
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         17)
                                                                                       inst_20683
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         18)
                                                                                       inst_20739
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         12)
                                                                                       inst_20684
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         8)
                                                                                       inst_20685
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         19)
                                                                                       inst_20738
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         11)
                                                                                       inst_20687
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         21)
                                                                                       inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20688
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         22)
                                                                                       inst_20689
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         23)
                                                                                       inst_20690
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         24)
                                                                                       inst_20721
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         9)
                                                                                       inst_20691
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         25)
                                                                                       t inst_20679
                                                                                       query
                                                                                       inst_20680
                                                                                       chunk_size
                                                                                       inst_20681
                                                                                       first_pass?
                                                                                       inst_20702
                                                                                       direction
                                                                                       inst_20682
                                                                                       p inst_20683
                                                                                       r inst_20739
                                                                                       next_r
                                                                                       inst_20684
                                                                                       table
                                                                                       inst_20685
                                                                                       limit
                                                                                       inst_20738
                                                                                       _ inst_20687
                                                                                       resp
                                                                                       inst_20701
                                                                                       client
                                                                                       inst_20688
                                                                                       ch
                                                                                       inst_20689
                                                                                       map__20643
                                                                                       inst_20690
                                                                                       put_items?
                                                                                       inst_20721
                                                                                       opts
                                                                                       inst_20691
                                                                                       inst_20759
                                                                                       (a/close!
                                                                                         ch)]
                                                                                      (let 
                                                                                        [statearr_20840
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20840
                                                                                          2
                                                                                          inst_20759)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20840
                                                                                          1
                                                                                          31))
                                                                                      :recur)
                                                                                    7
                                                                                    (let 
                                                                                      [inst_20767
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (clojure.core.async.impl.ioc-macros/return-chan
                                                                                        state_20769
                                                                                        inst_20767))
                                                                                    22
                                                                                    (let 
                                                                                      [inst_20700
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         7)
                                                                                       inst_20732
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)
                                                                                       tmp20799
                                                                                       inst_20700
                                                                                       inst_20700
                                                                                       tmp20799
                                                                                       inst_20701
                                                                                       inst_20732
                                                                                       inst_20702
                                                                                       false
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20829
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20829
                                                                                           7
                                                                                           inst_20700)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20829
                                                                                           6
                                                                                           inst_20701)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20829
                                                                                           26
                                                                                           inst_20702)
                                                                                         statearr_20829)]
                                                                                      (let 
                                                                                        [statearr_20830
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20830
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20830
                                                                                          1
                                                                                          6))
                                                                                      :recur)
                                                                                    20
                                                                                    (let 
                                                                                      [inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20700
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         7)
                                                                                       inst_20684
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         8)
                                                                                       inst_20721
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         9)
                                                                                       inst_20740
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         10)
                                                                                       inst_20735
                                                                                       (:Count
                                                                                         inst_20701)
                                                                                       inst_20736
                                                                                       (-
                                                                                         inst_20700
                                                                                         inst_20735)
                                                                                       inst_20737
                                                                                       (^clojure.lang.IFn inst_20684
                                                                                         inst_20701)
                                                                                       inst_20738
                                                                                       inst_20736
                                                                                       inst_20739
                                                                                       inst_20737
                                                                                       inst_20740
                                                                                       inst_20721
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20825
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20825
                                                                                           11
                                                                                           inst_20738)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20825
                                                                                           12
                                                                                           inst_20739)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20825
                                                                                           10
                                                                                           inst_20740)
                                                                                         statearr_20825)]
                                                                                      (if
                                                                                        inst_20740
                                                                                        (let 
                                                                                          [statearr_20826
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20826
                                                                                            1
                                                                                            23))
                                                                                        (let 
                                                                                          [statearr_20827
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20827
                                                                                            1
                                                                                            24)))
                                                                                      :recur)
                                                                                    3
                                                                                    (do
                                                                                      (let 
                                                                                        [statearr_20805
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20805
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20805
                                                                                          1
                                                                                          4))
                                                                                      :recur)
                                                                                    5
                                                                                    (let 
                                                                                      [inst_20696
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20808
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20808
                                                                                          2
                                                                                          inst_20696)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20808
                                                                                          1
                                                                                          4))
                                                                                      :recur)
                                                                                    4
                                                                                    (let 
                                                                                      [inst_20686
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         20)
                                                                                       inst_20699
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)
                                                                                       inst_20700
                                                                                       inst_20686
                                                                                       inst_20701
                                                                                       inst_20699
                                                                                       inst_20702
                                                                                       true
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20806
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20806
                                                                                           7
                                                                                           inst_20700)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20806
                                                                                           6
                                                                                           inst_20701)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20806
                                                                                           26
                                                                                           inst_20702)
                                                                                         statearr_20806)]
                                                                                      (let 
                                                                                        [statearr_20807
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20807
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20807
                                                                                          1
                                                                                          6))
                                                                                      :recur)
                                                                                    29
                                                                                    (let 
                                                                                      [inst_20682
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         17)
                                                                                       inst_20684
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         8)
                                                                                       inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)
                                                                                       inst_20680
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         15)
                                                                                       inst_20738
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         11)
                                                                                       inst_20752
                                                                                       (=
                                                                                         inst_20682
                                                                                         :forward)
                                                                                       inst_20753
                                                                                       (^clojure.lang.IFn inst_20684
                                                                                         inst_20701)
                                                                                       inst_20754
                                                                                       (^clojure.lang.IFn inst_20680
                                                                                         inst_20752
                                                                                         inst_20753
                                                                                         inst_20738)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20769
                                                                                        32
                                                                                        inst_20754))
                                                                                    24
                                                                                    (let 
                                                                                      [inst_20740
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         10)]
                                                                                      (let 
                                                                                        [statearr_20834
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20834
                                                                                          2
                                                                                          inst_20740)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20834
                                                                                          1
                                                                                          25))
                                                                                      :recur)
                                                                                    21
                                                                                    (let 
                                                                                      [inst_20763
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20828
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20828
                                                                                          2
                                                                                          inst_20763)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20828
                                                                                          1
                                                                                          10))
                                                                                      :recur)
                                                                                    8
                                                                                    (let 
                                                                                      [inst_20689
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         23)
                                                                                       inst_20701
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         6)]
                                                                                      (clojure.core.async.impl.ioc-macros/put!
                                                                                        state_20769
                                                                                        11
                                                                                        inst_20689
                                                                                        inst_20701))
                                                                                    2
                                                                                    (let 
                                                                                      [inst_20679
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         14)
                                                                                       inst_20680
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         15)
                                                                                       inst_20681
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         16)
                                                                                       inst_20682
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         17)
                                                                                       inst_20683
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         18)
                                                                                       inst_20684
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         8)
                                                                                       inst_20685
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         19)
                                                                                       inst_20686
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         20)
                                                                                       inst_20687
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         21)
                                                                                       inst_20688
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         22)
                                                                                       inst_20689
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         23)
                                                                                       inst_20690
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         24)
                                                                                       inst_20691
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         25)
                                                                                       t inst_20679
                                                                                       query
                                                                                       inst_20680
                                                                                       chunk_size
                                                                                       inst_20681
                                                                                       direction
                                                                                       inst_20682
                                                                                       p inst_20683
                                                                                       next_r
                                                                                       inst_20684
                                                                                       table
                                                                                       inst_20685
                                                                                       limit
                                                                                       inst_20686
                                                                                       _ inst_20687
                                                                                       client
                                                                                       inst_20688
                                                                                       ch
                                                                                       inst_20689
                                                                                       map__20643
                                                                                       inst_20690
                                                                                       opts
                                                                                       inst_20691
                                                                                       inst_20694
                                                                                       (^clojure.lang.IFn query
                                                                                         (=
                                                                                           direction
                                                                                           :backward)
                                                                                         t
                                                                                         1)]
                                                                                      (clojure.core.async.impl.ioc-macros/take!
                                                                                        state_20769
                                                                                        5
                                                                                        inst_20694))
                                                                                    18
                                                                                    (let 
                                                                                      [inst_20727
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (if
                                                                                        inst_20727
                                                                                        (let 
                                                                                          [statearr_20823
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20823
                                                                                            1
                                                                                            19))
                                                                                        (let 
                                                                                          [statearr_20824
                                                                                           state_20769]
                                                                                          (clojure.core.async.impl.ioc-macros/aset-object
                                                                                            statearr_20824
                                                                                            1
                                                                                            20)))
                                                                                      :recur)
                                                                                    15
                                                                                    (let 
                                                                                      [inst_20717
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20820
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20820
                                                                                          2
                                                                                          inst_20717)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20820
                                                                                          1
                                                                                          14))
                                                                                      :recur)
                                                                                    31
                                                                                    (let 
                                                                                      [inst_20761
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)]
                                                                                      (let 
                                                                                        [statearr_20841
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20841
                                                                                          2
                                                                                          inst_20761)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20841
                                                                                          1
                                                                                          21))
                                                                                      :recur)
                                                                                    32
                                                                                    (let 
                                                                                      [inst_20738
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         11)
                                                                                       inst_20756
                                                                                       (clojure.core.async.impl.ioc-macros/aget-object
                                                                                         state_20769
                                                                                         2)
                                                                                       inst_20700
                                                                                       inst_20738
                                                                                       inst_20701
                                                                                       inst_20756
                                                                                       inst_20702
                                                                                       false
                                                                                       state_20769
                                                                                       (let 
                                                                                         [statearr_20842
                                                                                          state_20769]
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20842
                                                                                           7
                                                                                           inst_20700)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20842
                                                                                           6
                                                                                           inst_20701)
                                                                                         (clojure.core.async.impl.ioc-macros/aset-object
                                                                                           statearr_20842
                                                                                           26
                                                                                           inst_20702)
                                                                                         statearr_20842)]
                                                                                      (let 
                                                                                        [statearr_20843
                                                                                         state_20769]
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20843
                                                                                          2
                                                                                          nil)
                                                                                        (clojure.core.async.impl.ioc-macros/aset-object
                                                                                          statearr_20843
                                                                                          1
                                                                                          6))
                                                                                      :recur)))]
                                                                               (if
                                                                                 (identical?
                                                                                   result__9978__auto__
                                                                                   :recur)
                                                                                 (recur)
                                                                                 result__9978__auto__))))
                                                                         (catch
                                                                           java.lang.Throwable
                                                                           ex__9979__auto__
                                                                           (do
                                                                             (let 
                                                                               [statearr_20844
                                                                                state_20769]
                                                                               (clojure.core.async.impl.ioc-macros/aset-object
                                                                                 statearr_20844
                                                                                 2
                                                                                 ex__9979__auto__))
                                                                             (if
                                                                               (seq
                                                                                 (clojure.core.async.impl.ioc-macros/aget-object
                                                                                   state_20769
                                                                                   4))
                                                                               (let 
                                                                                 [statearr_20845
                                                                                  state_20769]
                                                                                 (clojure.core.async.impl.ioc-macros/aset-object
                                                                                   statearr_20845
                                                                                   1
                                                                                   (first
                                                                                     (clojure.core.async.impl.ioc-macros/aget-object
                                                                                       state_20769
                                                                                       4))))
                                                                               (throw
                                                                                 ^java.lang.Throwable ex__9979__auto__))
                                                                             :recur))
                                                                         (finally
                                                                           (do
                                                                             (clojure.core.async.impl.ioc-macros/aset-object
                                                                               state_20769
                                                                               3
                                                                               (clojure.lang.Var/getThreadBindingFrame))
                                                                             (clojure.lang.Var/resetThreadBindingFrame
                                                                               old_frame__9976__auto__))))]
                                           (if (identical? ret_value__9977__auto__ :recur)
                                             (recur state_20769)
                                             ret_value__9977__auto__))))
                    state__10233__auto__ (let [statearr_20851 (^clojure.lang.IFn f__10232__auto__)]
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20851
                                             5
                                             c__10230__auto__)
                                           (clojure.core.async.impl.ioc-macros/aset-object
                                             statearr_20851
                                             3
                                             captured_bindings__10231__auto__)
                                           statearr_20851)]
                (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped
                  state__10233__auto__)))))
        c__10230__auto__))
    (-item-body [this item] (:body item))
    (-item-header [this item] (:header item))
    (-append [this header body] (aws/invoke-async client (append-request table p header body))))
  (clojure.core/import 'datomic.core2.log.ddb.Log)
  (defn ->Log ([client table p chunk_size] (datomic.core2.log.ddb.Log. client table p chunk_size)))
  (defn create
    ([p__20861]
      (let [map__20862 p__20861
            map__20862 (if (seq? map__20862)
                         (if (next map__20862)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20862))
                           (if (seq map__20862) (first map__20862) {}))
                         map__20862)
            client (get map__20862 :client)
            table (get map__20862 :table)
            p (get map__20862 :p)
            chunk_size (get map__20862 :chunk-size)]
        (->Log client table p chunk_size)))))
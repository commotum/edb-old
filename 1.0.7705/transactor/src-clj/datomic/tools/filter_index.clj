(do
  (clojure.core/in-ns 'datomic.tools.filter-index)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.index :as 'index]
        ['datomic.monitor :as 'monitor]
        ['datomic.slf4j :as 'logger])
      (clojure.core/import 'datomic.index.DirNode)
      (clojure.core/import 'datomic.index.RootNode)))
  (when-not (.equals 'datomic.tools.filter-index 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.filter-index))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.index :as 'index]
          ['datomic.monitor :as 'monitor]
          ['datomic.slf4j :as 'logger])
        (clojure.core/import 'datomic.index.DirNode)
        (clojure.core/import 'datomic.index.RootNode))))
  (def filter-index-leaf
   (fn filter_index_leaf
     ([old_kd leaf_id offset ct cluster olookup pred garbage]
       (let [raw_leaf (common/getx olookup leaf_id) leaf (take ct (drop offset raw_leaf))]
         (if (some pred leaf)
           (let [filtered (filterv (complement pred) leaf)]
             (if (seq filtered)
               (let [transposed (index/transpose filtered)]
                 [(or old_kd (first filtered))
                  (index/write-object cluster olookup transposed)
                  0
                  (java.lang.Integer/valueOf (int (count transposed)))
                  (conj garbage (cluster/uuid->val-key leaf_id))])
               [nil nil nil nil (conj garbage (cluster/uuid->val-key leaf_id))]))
           [(or old_kd (first leaf)) leaf_id offset ct garbage])))))
  (reset-meta!
    #'filter-index-leaf
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['old-kd 'leaf-id 'offset 'ct 'cluster 'olookup 'pred 'garbage]),
       :column (int 1)}
      :name
      'filter-index-leaf
      :ns
      *ns*))
  (def filter-index-dir
   (fn filter_index_dir
     ([old_kd dir_id cluster olookup pred garbage]
       (let [dir (common/getx olookup dir_id)
             vec__28148 (reduce
                          (fn fn__28152
                            ([p__28151 n]
                              (let [vec__28153 p__28151
                                    kds (nth vec__28153 (int 0) nil)
                                    ids (nth vec__28153 (int 1) nil)
                                    offsets (nth vec__28153 (int 2) nil)
                                    cts (nth vec__28153 (int 3) nil)
                                    garbage (nth vec__28153 (int 4) nil)
                                    segid (nth
                                            (.-segids ^datomic.index.DirNode dir)
                                            (int ^java.lang.Number n))
                                    vec__28156 (filter-index-leaf
                                                 (nth
                                                   (.-keydata ^datomic.index.DirNode dir)
                                                   (int ^java.lang.Number n))
                                                 segid
                                                 (nth
                                                   (.-offsets ^datomic.index.DirNode dir)
                                                   (int ^java.lang.Number n))
                                                 (nth
                                                   (.-counts ^datomic.index.DirNode dir)
                                                   (int ^java.lang.Number n))
                                                 cluster
                                                 olookup
                                                 pred
                                                 garbage)
                                    kd (nth vec__28156 (int 0) nil)
                                    id (nth vec__28156 (int 1) nil)
                                    offset (nth vec__28156 (int 2) nil)
                                    ct (nth vec__28156 (int 3) nil)
                                    garbage (nth vec__28156 (int 4) nil)]
                                (let [logger (org.slf4j.LoggerFactory/getLogger
                                               "datomic.tools.filter-index")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process
                                        {:event :filter-index/leaf, :from segid, :to id})))
                                  nil)
                                (cond
                                  (= segid id) (monitor/add-stat :FilterIndexKeepSegment ct)
                                  (nil? id) (monitor/add-stat
                                              :FilterIndexDropSegment
                                              (nth
                                                (.-counts ^datomic.index.DirNode dir)
                                                (int ^java.lang.Number n)))
                                  :default (do (monitor/add-stat :FilterIndexShrinkSegment ct)))
                                (if id
                                  [(conj kds kd)
                                   (conj ids id)
                                   (conj offsets offset)
                                   (conj cts ct)
                                   garbage]
                                  [kds ids offsets cts garbage]))))
                          [[] [] [] [] [] garbage]
                          (range
                            (java.lang.Integer/valueOf
                              (int (count (.-segids ^datomic.index.DirNode dir))))))
             kds (nth vec__28148 (int 0) nil)
             ids (nth vec__28148 (int 1) nil)
             offsets (nth vec__28148 (int 2) nil)
             cts (nth vec__28148 (int 3) nil)
             garbage (nth vec__28148 (int 4) nil)]
         (cond
           (= (count ids) 0) [nil nil (conj garbage (cluster/uuid->val-key dir_id))]
           (= ids (seq (.-segids ^datomic.index.DirNode dir))) [dir_id
                                                                (or
                                                                  old_kd
                                                                  (first
                                                                    (.-keydata
                                                                      ^datomic.index.DirNode dir)))
                                                                garbage]
           :default (do
                      [(index/write-object
                         cluster
                         olookup
                         (index/dir-node
                           (index/transpose kds)
                           (to-array ids)
                           (int-array offsets)
                           (int-array cts)))
                       (or old_kd (first kds))
                       (conj garbage (cluster/uuid->val-key dir_id))]))))))
  (reset-meta!
    #'filter-index-dir
    (assoc
      {:private true,
       :arglists (clojure.core/list ['old-kd 'dir-id 'cluster 'olookup 'pred 'garbage]),
       :column (int 1)}
      :name
      'filter-index-dir
      :ns
      *ns*))
  (def filter-index-root
   (fn filter_index_root
     ([root_id cluster olookup pred garbage]
       (let [root (common/getx olookup root_id)
             vec__28163 (reduce
                          (fn fn__28167
                            ([p__28166 n]
                              (let [vec__28168 p__28166
                                    ids (nth vec__28168 (int 0) nil)
                                    kds (nth vec__28168 (int 1) nil)
                                    garbage (nth vec__28168 (int 2) nil)
                                    vec__28171 (filter-index-dir
                                                 (nth
                                                   (.-keydata ^datomic.index.RootNode root)
                                                   (int ^java.lang.Number n))
                                                 (nth
                                                   (.-dirids ^datomic.index.RootNode root)
                                                   (int ^java.lang.Number n))
                                                 cluster
                                                 olookup
                                                 pred
                                                 garbage)
                                    id (nth vec__28171 (int 0) nil)
                                    kd (nth vec__28171 (int 1) nil)
                                    garbage (nth vec__28171 (int 2) nil)]
                                (let [logger (org.slf4j.LoggerFactory/getLogger
                                               "datomic.tools.filter-index")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process
                                        {:event :filter-index/dir, :from root_id, :dir/to id})))
                                  nil)
                                (if id [(conj ids id) (conj kds kd) garbage] [ids kds garbage]))))
                          [[] [] garbage]
                          (range
                            (java.lang.Integer/valueOf
                              (int (count (.-dirids ^datomic.index.RootNode root))))))
             ids (nth vec__28163 (int 0) nil)
             kds (nth vec__28163 (int 1) nil)
             garbage (nth vec__28163 (int 2) nil)]
         (cond
           (= (count ids) 0) [nil (conj garbage (cluster/uuid->val-key root_id))]
           (= ids (seq (.-dirids ^datomic.index.RootNode root))) [root_id garbage]
           :default (do
                      [(index/write-object
                         cluster
                         olookup
                         (index/root-node (index/transpose kds) (object-array ids)))
                       (conj garbage (cluster/uuid->val-key root_id))]))))))
  (reset-meta!
    #'filter-index-root
    (assoc
      {:private true,
       :arglists (clojure.core/list ['root-id 'cluster 'olookup 'pred 'garbage]),
       :column (int 1)}
      :name
      'filter-index-root
      :ns
      *ns*))
  (def index-root-key?
   #{:eavt-main :avet-mid :aevt-main :raet-main :raet-mid :avet-hist :eavt-hist :raet-hist
     :aevt-mid :avet-main :aevt-hist :eavt-mid})
  (reset-meta! #'index-root-key? (assoc {:column (int 1)} :name 'index-root-key? :ns *ns*))
  (def filter-index
   (fn filter_index
     ([index_id cluster olookup pred]
       (when-not (string? index_id)
         (throw
           (java.lang.AssertionError.
             (str "Assert failed: " (pr-str (clojure.core/list 'string? 'index-id))))))
       (let [index (common/getx olookup index_id)
             vec__28176 (reduce-kv
                          (fn fn__28180
                            ([p__28179 k v]
                              (let [vec__28181 p__28179
                                    m (nth vec__28181 (int 0) nil)
                                    garbage (nth vec__28181 (int 1) nil)]
                                (if (and (index-root-key? k) v)
                                  (let [vec__28184 (filter-index-root
                                                     v
                                                     cluster
                                                     olookup
                                                     pred
                                                     garbage)
                                        id (nth vec__28184 (int 0) nil)
                                        new_garbage (nth vec__28184 (int 1) nil)]
                                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                                   "datomic.tools.filter-index")]
                                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                        (.info
                                          ^org.slf4j.Logger logger
                                          (logger/process
                                            {:event :filter-index/root, :key k, :from v, :to id})))
                                      nil)
                                    [(assoc m k id) (into garbage new_garbage)])
                                  [(assoc m k v) garbage]))))
                          [index []]
                          index)
             new_index (nth vec__28176 (int 0) nil)
             garbage (nth vec__28176 (int 1) nil)]
         (if (= new_index index)
           [index_id garbage]
           (let [new_index_id (cluster/uuid->val-key
                                (index/write-object
                                  cluster
                                  olookup
                                  (merge new_index {:version 2, :index/rewrite true})))]
             (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.tools.filter-index")]
               (when (.isInfoEnabled ^org.slf4j.Logger logger)
                 (.info
                   ^org.slf4j.Logger logger
                   (logger/process
                     {:event :filter-index/index, :from index_id, :to new_index_id})))
               nil)
             [new_index_id (conj garbage (cluster/uuid->val-key index_id))]))))))
  (reset-meta!
    #'filter-index
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta
           ['index-id 'cluster 'olookup 'pred]
           {:pre [(.withMeta (clojure.core/list 'string? 'index-id) {:column (int 10)})]})),
       :column (int 1)}
      :name
      'filter-index
      :ns
      *ns*)))
(do
  (clojure.core/in-ns 'datomic.treewalk)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        'datomic.fulltext
        'datomic.index
        ['datomic.clusterfs :as 'clusterfs]
        ['datomic.common :as 'common])))
  (when-not (.equals 'datomic.treewalk 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.treewalk))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          'datomic.fulltext
          'datomic.index
          ['datomic.clusterfs :as 'clusterfs]
          ['datomic.common :as 'common]))))
  (defonce NodeId {})
  (defprotocol NodeId (node-id [_]))
  (defonce TreeWalker {})
  (defprotocol TreeWalker (child-node-ids [_]) (subtrees [_ ids->nodes]))
  (def index-root-keys
   [:aevt-hist
    :aevt
    :aevt-mid
    :aevt-main
    :eavt-hist
    :eavt
    :eavt-mid
    :eavt-main
    :raet-hist
    :raet
    :raet-mid
    :raet-main
    :avet-hist
    :avet
    :avet-mid
    :avet-main
    :fulltext-hist
    :fulltext])
  (extend
    datomic.fulltext.Root
    TreeWalker
    {:child-node-ids (fn fn__24729 ([root] (map str (vals (:attrmap root))))),
     :subtrees
     (fn fn__24731 ([root ids_>nodes] (^clojure.lang.IFn ids_>nodes (child-node-ids root))))})
  (extend
    datomic.clusterfs.ClusterFS
    TreeWalker
    {:child-node-ids (fn fn__24733 ([clusterfs] (clusterfs/all-keys clusterfs))),
     :subtrees (fn fn__24735 ([_ _] nil))})
  (extend
    datomic.index.RootNode
    TreeWalker
    {:child-node-ids (fn fn__24737 ([rn] (map str (.-dirids ^datomic.index.RootNode rn)))),
     :subtrees
     (fn fn__24739 ([rn ids_>nodes] (^clojure.lang.IFn ids_>nodes (child-node-ids rn))))})
  (extend
    datomic.index.DirNode
    TreeWalker
    {:child-node-ids (fn fn__24741 ([dn] (map str (.-segids ^datomic.index.DirNode dn)))),
     :subtrees (fn fn__24743 ([_ _] nil))})
  (deftype
    Node
    [id walker]
    datomic.treewalk.TreeWalker
    datomic.treewalk.NodeId
    (subtrees [this ids_>nodes] (subtrees walker ids_>nodes))
    (child-node-ids [this] (child-node-ids walker))
    (node-id [this] id))
  (clojure.core/import 'datomic.treewalk.Node)
  (defn ->Node ([id walker] (datomic.treewalk.Node. id walker)))
  (defn lookup-val
    ([lookup id allow_missing?] (if allow_missing? (get lookup id) (common/getx lookup id))))
  (defn create-node
    ([id lookup allow_missing?]
      (when-not id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'id)))))
      (when-not lookup
        (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'lookup)))))
      (let [temp__5804__auto__ (lookup-val lookup id allow_missing?)]
        (when temp__5804__auto__ (let [v temp__5804__auto__] (->Node id v))))))
  (defn create-ids->nodes
    ([lookup allow_missing?]
      (do
        (when-not lookup
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'lookup)))))
        (fn fn__24754
          ([ids]
            (remove
              nil?
              (map
                (fn fn__24755 ([p1__24753#] (create-node p1__24753# lookup allow_missing?)))
                ids))))))
    ([lookup] (create-ids->nodes lookup false)))
  (defn create-parent-node
    ([id walker lookup allow_missing?]
      (let [ids_>nodes (create-ids->nodes lookup allow_missing?)
            temp__5804__auto__ (lookup-val lookup id allow_missing?)]
        (when temp__5804__auto__
          (let [top temp__5804__auto__]
            (datomic.treewalk.Node. id (^clojure.lang.IFn walker top))))))
    ([id walker lookup] (create-parent-node id walker lookup false)))
  (defn tree-node-ids
    ([node ids_>nodes]
      (concat
        (mapcat
          (fn fn__24762 ([p1__24761#] (tree-node-ids p1__24761# ids_>nodes)))
          (subtrees node ids_>nodes))
        (child-node-ids node))))
  (defn index-top-walker
    ([top]
      (reify
        datomic.treewalk.TreeWalker
        (subtrees [this ids_>nodes] (^clojure.lang.IFn ids_>nodes (child-node-ids this)))
        (child-node-ids [this] (map str (remove nil? (map top index-root-keys)))))))
  (defn index-tree-seq
    ([id lookup allow_missing?]
      (do
        (when-not id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'id)))))
        (when-not lookup
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'lookup)))))
        (let [ids_>nodes (create-ids->nodes lookup allow_missing?)
              temp__5804__auto__ (lookup-val lookup id allow_missing?)]
          (when temp__5804__auto__
            (let [top temp__5804__auto__]
              (concat (tree-node-ids (index-top-walker top) ids_>nodes) [id]))))))
    ([id lookup] (index-tree-seq id lookup false)))
  (defn log-root-walker
    ([root lookup allow_missing?]
      (reify
        datomic.treewalk.TreeWalker
        (subtrees
          [this ids_>nodes]
          (map
            (fn fn__24772
              ([p__24771]
                (let [map__24773 p__24771
                      map__24773 (if (seq? map__24773)
                                   (if (next map__24773)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__24773))
                                     (if (seq map__24773) (first map__24773) {}))
                                   map__24773)
                      uuid (get map__24773 :uuid)
                      temp__5804__auto__ (lookup-val lookup (str uuid) allow_missing?)]
                  (when temp__5804__auto__
                    (let [v temp__5804__auto__]
                      (datomic.treewalk.Node.
                        (str uuid)
                        (reify
                          datomic.treewalk.TreeWalker
                          (subtrees [this _] nil)
                          (child-node-ids [this] (map (comp str :uuid) v)))))))))
            root))
        (child-node-ids [this] (map (comp str :uuid) root)))))
  (defn log-tree-seq
    ([id lookup allow_missing?]
      (do
        (when-not id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'id)))))
        (when-not lookup
          (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'lookup)))))
        (let [ids_>nodes (create-ids->nodes lookup allow_missing?)
              temp__5804__auto__ (lookup-val lookup id allow_missing?)]
          (when temp__5804__auto__
            (let [root temp__5804__auto__]
              (concat
                (tree-node-ids (log-root-walker root lookup allow_missing?) ids_>nodes)
                [id]))))))
    ([id lookup] (log-tree-seq id lookup false)))
  (defn db-seq
    ([log_root_node index_top_node lookup]
      (concat
        (index-tree-seq (node-id index_top_node) lookup)
        (log-tree-seq (node-id log_root_node) lookup)))))
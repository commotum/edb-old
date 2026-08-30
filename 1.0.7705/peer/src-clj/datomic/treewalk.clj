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
  (let [protocol_metadata__7431 {:column (int 1)}]
    (defprotocol NodeId (node-id [_] "Return storage id of this node."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.treewalk" "NodeId")
      (assoc (assoc protocol_metadata__7431 :doc nil) :name 'NodeId :ns *ns*))
    (let [protocol_signature__7432 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta 'node-id {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Return storage id of this node."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.treewalk" "NodeId"))
          protocol_method_name__7433 (with-meta
                                       (:name protocol_signature__7432)
                                       protocol_signature__7432)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.treewalk" "node-id")
        (assoc protocol_signature__7432 :name protocol_method_name__7433 :ns *ns*))))
  (let [protocol_metadata__7434 {:column (int 1)}]
    (defprotocol
      TreeWalker
      (child-node-ids [_] "Ids of nodes owned by this node.")
      (subtrees
        [_ ids->nodes]
        "Subtree objects for child nodes that contain more nodes.\n    Subtrees implement TreeWalker. Nil if children are leaves."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.treewalk" "TreeWalker")
      (assoc (assoc protocol_metadata__7434 :doc nil) :name 'TreeWalker :ns *ns*))
    (let [protocol_signature__7435 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'child-node-ids
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Ids of nodes owned by this node."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.treewalk" "TreeWalker"))
          protocol_method_name__7436 (with-meta
                                       (:name protocol_signature__7435)
                                       protocol_signature__7435)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.treewalk" "child-node-ids")
        (assoc protocol_signature__7435 :name protocol_method_name__7436 :ns *ns*)))
    (let [protocol_signature__7437 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'subtrees
                                        {:arglists (clojure.core/list ['_ 'ids->nodes])}),
                                      :arglists (clojure.core/list ['_ 'ids->nodes]),
                                      :doc
                                      "Subtree objects for child nodes that contain more nodes.\n    Subtrees implement TreeWalker. Nil if children are leaves."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.treewalk" "TreeWalker"))
          protocol_method_name__7438 (with-meta
                                       (:name protocol_signature__7437)
                                       protocol_signature__7437)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.treewalk" "subtrees")
        (assoc protocol_signature__7437 :name protocol_method_name__7438 :ns *ns*))))
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
  (reset-meta! #'index-root-keys (assoc {:column (int 1)} :name 'index-root-keys :ns *ns*))
  (extend
    datomic.fulltext.Root
    TreeWalker
    {:child-node-ids (fn fn__14813 ([root] (map str (vals (:attrmap root))))),
     :subtrees
     (fn fn__14815 ([root ids_>nodes] (^clojure.lang.IFn ids_>nodes (child-node-ids root))))})
  (extend
    datomic.clusterfs.ClusterFS
    TreeWalker
    {:child-node-ids (fn fn__14817 ([clusterfs] (clusterfs/all-keys clusterfs))),
     :subtrees (fn fn__14819 ([_ _] nil))})
  (extend
    datomic.index.RootNode
    TreeWalker
    {:child-node-ids (fn fn__14821 ([rn] (map str (.-dirids ^datomic.index.RootNode rn)))),
     :subtrees
     (fn fn__14823 ([rn ids_>nodes] (^clojure.lang.IFn ids_>nodes (child-node-ids rn))))})
  (extend
    datomic.index.DirNode
    TreeWalker
    {:child-node-ids (fn fn__14825 ([dn] (map str (.-segids ^datomic.index.DirNode dn)))),
     :subtrees (fn fn__14827 ([_ _] nil))})
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
  (reset-meta!
    #'->Node
    (assoc {:arglists (clojure.core/list ['id 'walker]), :column (int 1)} :name '->Node :ns *ns*))
  (def lookup-val
   (fn lookup_val
     ([lookup id allow_missing?] (if allow_missing? (get lookup id) (common/getx lookup id)))))
  (reset-meta!
    #'lookup-val
    (assoc
      {:arglists (clojure.core/list ['lookup 'id 'allow-missing?]), :column (int 1)}
      :name
      'lookup-val
      :ns
      *ns*))
  (def create-node
   (fn create_node
     ([id lookup allow_missing?]
       (when-not id (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'id)))))
       (when-not lookup
         (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'lookup)))))
       (let [temp__5804__auto__ (lookup-val lookup id allow_missing?)]
         (when temp__5804__auto__ (let [v temp__5804__auto__] (->Node id v)))))))
  (reset-meta!
    #'create-node
    (assoc
      {:arglists
       (clojure.core/list (.withMeta ['id 'lookup 'allow-missing?] {:pre ['id 'lookup]})),
       :column (int 1)}
      :name
      'create-node
      :ns
      *ns*))
  (def create-ids->nodes
   (fn create_ids__GT_nodes
     ([lookup allow_missing?]
       (do
         (when-not lookup
           (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'lookup)))))
         (fn fn__14838
           ([ids]
             (remove
               nil?
               (map
                 (fn fn__14839 ([p1__14837#] (create-node p1__14837# lookup allow_missing?)))
                 ids))))))
     ([lookup] (create-ids->nodes lookup false))))
  (reset-meta!
    #'create-ids->nodes
    (assoc
      {:arglists
       (clojure.core/list ['lookup] (.withMeta ['lookup 'allow-missing?] {:pre ['lookup]})),
       :column (int 1)}
      :name
      'create-ids->nodes
      :ns
      *ns*))
  (def create-parent-node
   (fn create_parent_node
     ([id walker lookup allow_missing?]
       (let [ids_>nodes (create-ids->nodes lookup allow_missing?)
             temp__5804__auto__ (lookup-val lookup id allow_missing?)]
         (when temp__5804__auto__
           (let [top temp__5804__auto__]
             (datomic.treewalk.Node. id (^clojure.lang.IFn walker top))))))
     ([id walker lookup] (create-parent-node id walker lookup false))))
  (reset-meta!
    #'create-parent-node
    (assoc
      {:arglists (clojure.core/list ['id 'walker 'lookup] ['id 'walker 'lookup 'allow-missing?]),
       :column (int 1)}
      :name
      'create-parent-node
      :ns
      *ns*))
  (def tree-node-ids
   (fn tree_node_ids
     ([node ids_>nodes]
       (concat
         (mapcat
           (fn fn__14846 ([p1__14845#] (tree-node-ids p1__14845# ids_>nodes)))
           (subtrees node ids_>nodes))
         (child-node-ids node)))))
  (reset-meta!
    #'tree-node-ids
    (assoc
      {:arglists (clojure.core/list ['node 'ids->nodes]), :column (int 1)}
      :name
      'tree-node-ids
      :ns
      *ns*))
  (defn index-top-walker
    ([top]
      (reify
        datomic.treewalk.TreeWalker
        (subtrees [this ids_>nodes] (^clojure.lang.IFn ids_>nodes (child-node-ids this)))
        (child-node-ids [this] (map str (remove nil? (map top index-root-keys)))))))
  (reset-meta!
    #'index-top-walker
    (assoc
      {:arglists (clojure.core/list ['top]), :column (int 1)}
      :name
      'index-top-walker
      :ns
      *ns*))
  (def index-tree-seq
   (fn index_tree_seq
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
     ([id lookup] (index-tree-seq id lookup false))))
  (reset-meta!
    #'index-tree-seq
    (assoc
      {:arglists
       (clojure.core/list
         ['id 'lookup]
         (.withMeta ['id 'lookup 'allow-missing?] {:pre ['id 'lookup]})),
       :column (int 1)}
      :name
      'index-tree-seq
      :ns
      *ns*))
  (def log-root-walker
   (fn log_root_walker
     ([root lookup allow_missing?]
       (reify
         datomic.treewalk.TreeWalker
         (subtrees
           [this ids_>nodes]
           (map
             (fn fn__14856
               ([p__14855]
                 (let [map__14857 p__14855
                       map__14857 (if (seq? map__14857)
                                    (if (next map__14857)
                                      (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                        (to-array map__14857))
                                      (if (seq map__14857) (first map__14857) {}))
                                    map__14857)
                       uuid (get map__14857 :uuid)
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
         (child-node-ids [this] (map (comp str :uuid) root))))))
  (reset-meta!
    #'log-root-walker
    (assoc
      {:arglists (clojure.core/list ['root 'lookup 'allow-missing?]), :column (int 1)}
      :name
      'log-root-walker
      :ns
      *ns*))
  (def log-tree-seq
   (fn log_tree_seq
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
     ([id lookup] (log-tree-seq id lookup false))))
  (reset-meta!
    #'log-tree-seq
    (assoc
      {:arglists
       (clojure.core/list
         ['id 'lookup]
         (.withMeta ['id 'lookup 'allow-missing?] {:pre ['id 'lookup]})),
       :column (int 1)}
      :name
      'log-tree-seq
      :ns
      *ns*))
  (def db-seq
   (fn db_seq
     ([log_root_node index_top_node lookup]
       (concat
         (index-tree-seq (node-id index_top_node) lookup)
         (log-tree-seq (node-id log_root_node) lookup)))))
  (reset-meta!
    #'db-seq
    (assoc
      {:arglists (clojure.core/list ['log-root-node 'index-top-node 'lookup]), :column (int 1)}
      :name
      'db-seq
      :ns
      *ns*)))
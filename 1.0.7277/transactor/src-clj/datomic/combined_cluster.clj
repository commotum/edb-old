(do
  (clojure.core/in-ns 'datomic.combined-cluster)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.combined-cluster)
    {:doc
     "A combiner for ClusteredStores.\nTakes two ClusteredStores, one for refs and one for values."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.cluster :as 'cluster])
      (clojure.core/import 'java.io.Closeable)))
  (when-not (.equals 'datomic.combined-cluster 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.combined-cluster))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.cluster :as 'cluster])
        (clojure.core/import 'java.io.Closeable))))
  (set! *warn-on-reflection* true)
  (deftype
    CombinedCluster
    [ref_cluster val_cluster]
    datomic.cluster.Dbid
    datomic.cluster.Get2
    datomic.cluster.ClusteredStore
    java.io.Closeable
    datomic.cluster.RefClusterStore
    (-get-ref-store [this] (cluster/get-ref-store ref_cluster))
    (get-val2 [this val_key opts] (cluster/get-val2 val_cluster val_key opts))
    (update-pod*
      [this pod_key rev etag buf metamap]
      (cluster/update-pod* ref_cluster pod_key rev etag buf metamap))
    (get-pod [this pod_key] (cluster/get-pod ref_cluster pod_key))
    (get-pod-meta [this pod_key] (cluster/get-pod-meta ref_cluster pod_key))
    (set-ref [this ref_key rev vkey] (cluster/set-ref ref_cluster ref_key rev vkey))
    (get-ref [this ref_key] (cluster/get-ref ref_cluster ref_key))
    (delete-reference [this key] (cluster/delete-reference ref_cluster key))
    (delete [this key] (cluster/delete val_cluster key))
    (get-val [this val_key] (cluster/get-val val_cluster val_key))
    (create-val [this val_key buf] (cluster/create-val this 3 val_key buf))
    (create-val [this priority val_key buf] (cluster/create-val val_cluster priority val_key buf))
    (dbId [this] (cluster/dbId ref_cluster))
    (^void close [this] (do (cluster/close ref_cluster) (cluster/close val_cluster) nil)))
  (clojure.core/import 'datomic.combined_cluster.CombinedCluster)
  (def ->CombinedCluster
   (fn __GT_CombinedCluster
     ([ref_cluster val_cluster]
       (datomic.combined_cluster.CombinedCluster. ref_cluster val_cluster))))
  (reset-meta!
    #'->CombinedCluster
    (assoc
      {:arglists (clojure.core/list ['ref-cluster 'val-cluster]), :column (int 1)}
      :name
      '->CombinedCluster
      :ns
      *ns*))
  (def combined-cluster
   (fn combined_cluster
     ([ref_cluster val_cluster]
       (datomic.combined_cluster.CombinedCluster. ref_cluster val_cluster))))
  (reset-meta!
    #'combined-cluster
    (assoc
      {:arglists (clojure.core/list ['ref-cluster 'val-cluster]), :column (int 1)}
      :name
      'combined-cluster
      :ns
      *ns*)))
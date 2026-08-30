(do
  (clojure.core/in-ns 'datomic.cloudwatch)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['datomic.aws.client.api :as 'aws])
      (clojure.core/import 'software.amazon.awssdk.services.cloudwatch.CloudWatchClient)))
  (when-not (.equals 'datomic.cloudwatch 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cloudwatch))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['datomic.aws.client.api :as 'aws])
        (clojure.core/import 'software.amazon.awssdk.services.cloudwatch.CloudWatchClient))))
  (set! *warn-on-reflection* true)
  (defn client
    ([creds opts] (aws-helpers/sync-client (CloudWatchClient/builder) creds opts))
    ([opts] (aws-helpers/sync-client (CloudWatchClient/builder) opts)))
  (reset-meta!
    #'client
    (assoc
      {:arglists (clojure.core/list ['opts] ['creds 'opts]), :column (int 1)}
      :name
      'client
      :ns
      *ns*))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.cloudwatch.CloudWatchClient :PutMetricData]
    fn__17612
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__17613
         ([client17610 G__17611]
           (.putMetricData
             ^software.amazon.awssdk.services.cloudwatch.CloudWatchClient client17610
             ^software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest G__17611))),
       :ret-mode :sync,
       :request-builder
       (fn fn__17615
         ([] (software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest/builder)))})))
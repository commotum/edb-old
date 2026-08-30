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
    fn__30645
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__30646
         ([client30643 G__30644]
           (.putMetricData
             ^software.amazon.awssdk.services.cloudwatch.CloudWatchClient client30643
             ^software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest G__30644))),
       :ret-mode :sync,
       :request-builder
       (fn fn__30648
         ([] (software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest/builder)))})))
(do
  (clojure.core/in-ns 'datomic.ec2)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.core2.aws.helpers :as 'aws-helpers]
        ['datomic.aws.client.api :as 'aws])
      (clojure.core/import 'software.amazon.awssdk.services.ec2.Ec2Client)))
  (when-not (.equals 'datomic.ec2 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.ec2))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.core2.aws.helpers :as 'aws-helpers]
          ['datomic.aws.client.api :as 'aws])
        (clojure.core/import 'software.amazon.awssdk.services.ec2.Ec2Client))))
  (set! *warn-on-reflection* true)
  (defn client ([opts] (aws-helpers/sync-client (Ec2Client/builder) opts)) ([] (client nil)))
  (reset-meta!
    #'client
    (assoc {:arglists (clojure.core/list [] ['opts]), :column (int 1)} :name 'client :ns *ns*))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.ec2.Ec2Client :CreateSecurityGroup]
    fn__27633
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__27634
         ([client27631 G__27632]
           (.createSecurityGroup
             ^software.amazon.awssdk.services.ec2.Ec2Client client27631
             ^software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest G__27632))),
       :ret-mode :sync,
       :request-builder
       (fn fn__27636
         ([] (software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest/builder)))}))
  (defmethod
    datomic.aws.client.registry/lookup-op
    [software.amazon.awssdk.services.ec2.Ec2Client :AuthorizeSecurityGroupIngress]
    fn__27641
    ([client_class__14277__auto__ op__14278__auto__]
      {:variant :request,
       :method
       (fn fn__27642
         ([client27639 G__27640]
           (.authorizeSecurityGroupIngress
             ^software.amazon.awssdk.services.ec2.Ec2Client client27639
             ^software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest G__27640))),
       :ret-mode :sync,
       :request-builder
       (fn fn__27644
         ([]
           (software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest/builder)))}))
  (defn authorize-security-group-ingress-command
    ([p__27647]
      (let [map__27648 p__27647
            map__27648 (if (seq? map__27648)
                         (if (next map__27648)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27648))
                           (if (seq map__27648) (first map__27648) {}))
                         map__27648)
            group_name (get map__27648 :group-name)
            address (get map__27648 :address)
            protocol (get map__27648 :protocol)
            port (get map__27648 :port)
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (println
              (aws-helpers/invoke
                (client)
                {:op :AuthorizeSecurityGroupIngress,
                 :req
                 {:GroupName group_name,
                  :IpPermissions
                  [{:IpProtocol protocol, :ToPort port, :FromPort port, :IpRanges [address]}]}}))
            (str s__6444__auto__))))))
  (reset-meta!
    #'authorize-security-group-ingress-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['group-name 'address 'protocol 'port]}]),
       :column (int 1)}
      :name
      'authorize-security-group-ingress-command
      :ns
      *ns*))
  (defn create-security-group-command
    ([p__27651]
      (let [map__27652 p__27651
            map__27652 (if (seq? map__27652)
                         (if (next map__27652)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__27652))
                           (if (seq map__27652) (first map__27652) {}))
                         map__27652)
            group_name (get map__27652 :group-name)
            description (get map__27652 :description)
            s__6444__auto__ (java.io.StringWriter.)]
        (binding [*out* s__6444__auto__]
          (do
            (println
              (aws-helpers/invoke
                (client)
                {:op :CreateSecurityGroup,
                 :req {:GroupName group_name, :Description description}}))
            (str s__6444__auto__))))))
  (reset-meta!
    #'create-security-group-command
    (assoc
      {:arglists (clojure.core/list [{:keys ['group-name 'description]}]), :column (int 1)}
      :name
      'create-security-group-command
      :ns
      *ns*)))
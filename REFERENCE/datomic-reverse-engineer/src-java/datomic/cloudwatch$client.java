/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.AmazonWebServiceClient
 *  com.amazonaws.ClientConfiguration
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.AWSCredentialsProvider
 *  com.amazonaws.auth.DefaultAWSCredentialsProviderChain
 *  com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

public final class cloudwatch$client
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.aws", (String)"credentials");
    public static final Keyword const__4 = RT.keyword(null, (String)"region");
    public static final Keyword const__5 = RT.keyword(null, (String)"override-endpoint");
    public static final Var const__6 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Object const__8 = RT.classForName((String)"com.amazonaws.ClientConfiguration");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"datomic.aws", (String)"endpoint-for");
    public static final Keyword const__11 = RT.keyword(null, (String)"monitoring");
    public static final Var const__12 = RT.var((String)"datomic.cloudwatch", (String)"client");

    public static Object invokeStatic(Object creds, Object config2) {
        Object object;
        Object object2 = config2;
        if (object2 != null && object2 != Boolean.FALSE) {
            AmazonCloudWatchClient amazonCloudWatchClient;
            Object region = RT.get((Object)config2, (Object)const__4);
            Object override_endpoint = RT.get((Object)config2, (Object)const__5);
            Object object3 = config2;
            config2 = null;
            Object conf = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object3, (Object)const__4, (Object)const__5), const__8);
            Object object4 = creds;
            if (object4 != null && object4 != Boolean.FALSE) {
                if (((IFn)const__2.getRawRoot()).invoke(creds) instanceof AWSCredentialsProvider) {
                    creds = null;
                    conf = null;
                    amazonCloudWatchClient = new AmazonCloudWatchClient((AWSCredentialsProvider)((IFn)const__2.getRawRoot()).invoke(creds), (ClientConfiguration)conf);
                } else {
                    creds = null;
                    conf = null;
                    amazonCloudWatchClient = new AmazonCloudWatchClient((AWSCredentials)((IFn)const__2.getRawRoot()).invoke(creds), (ClientConfiguration)conf);
                }
            } else if (new DefaultAWSCredentialsProviderChain() instanceof AWSCredentialsProvider) {
                conf = null;
                amazonCloudWatchClient = new AmazonCloudWatchClient((AWSCredentialsProvider)new DefaultAWSCredentialsProviderChain(), (ClientConfiguration)conf);
            } else {
                conf = null;
                amazonCloudWatchClient = new AmazonCloudWatchClient((AWSCredentialsProvider)new DefaultAWSCredentialsProviderChain(), (ClientConfiguration)conf);
            }
            AmazonCloudWatchClient conn = amazonCloudWatchClient;
            Object object5 = override_endpoint;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = override_endpoint;
                override_endpoint = null;
                ((AmazonWebServiceClient)conn).setEndpoint((String)((IFn)const__9.getRawRoot()).invoke((Object)"http://", object6));
            } else {
                Object object7 = region;
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = region;
                    region = null;
                    ((AmazonWebServiceClient)conn).setEndpoint((String)((IFn)const__10.getRawRoot()).invoke((Object)const__11, object8));
                }
            }
            object = conn;
            conn = null;
        } else {
            Object object9 = creds;
            creds = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object9);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cloudwatch$client.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object creds) {
        AmazonCloudWatchClient amazonCloudWatchClient;
        Object object = creds;
        if (object != null && object != Boolean.FALSE) {
            if (((IFn)const__2.getRawRoot()).invoke(creds) instanceof AWSCredentialsProvider) {
                creds = null;
                amazonCloudWatchClient = new AmazonCloudWatchClient((AWSCredentialsProvider)((IFn)const__2.getRawRoot()).invoke(creds));
            } else {
                creds = null;
                amazonCloudWatchClient = new AmazonCloudWatchClient((AWSCredentials)((IFn)const__2.getRawRoot()).invoke(creds));
            }
        } else {
            amazonCloudWatchClient = new DefaultAWSCredentialsProviderChain() instanceof AWSCredentialsProvider ? new AmazonCloudWatchClient((AWSCredentialsProvider)new DefaultAWSCredentialsProviderChain()) : new AmazonCloudWatchClient((AWSCredentialsProvider)new DefaultAWSCredentialsProviderChain());
        }
        return amazonCloudWatchClient;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cloudwatch$client.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return new DefaultAWSCredentialsProviderChain() instanceof AWSCredentialsProvider ? new AmazonCloudWatchClient((AWSCredentialsProvider)new DefaultAWSCredentialsProviderChain()) : new AmazonCloudWatchClient((AWSCredentialsProvider)new DefaultAWSCredentialsProviderChain());
    }

    public Object invoke() {
        return cloudwatch$client.invokeStatic();
    }
}


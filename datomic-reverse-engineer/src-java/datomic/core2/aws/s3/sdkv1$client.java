/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.ClientConfiguration
 *  com.amazonaws.auth.AWSCredentialsProvider
 *  com.amazonaws.auth.DefaultAWSCredentialsProviderChain
 *  com.amazonaws.client.builder.AwsClientBuilder
 *  com.amazonaws.retry.RetryPolicy
 *  com.amazonaws.services.s3.AmazonS3Client
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3Client;

public final class sdkv1$client
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"region");
    public static final Keyword const__7 = RT.keyword(null, (String)"retryPolicy");
    public static final Keyword const__8 = RT.keyword(null, (String)"client-conf");
    public static final Keyword const__9 = RT.keyword(null, (String)"creds-provider");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object p__20526) {
        Object object;
        Object object2 = p__20526;
        p__20526 = null;
        Object map__20527 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20527);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20527);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__20527;
                map__20527 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__3.getRawRoot()).invoke(map__20527);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__20527;
                    map__20527 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object7);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20527;
            map__20527 = null;
        }
        Object map__205272 = object;
        Object region = RT.get((Object)map__205272, (Object)const__6);
        Object retryPolicy = RT.get((Object)map__205272, (Object)const__7);
        Object client_conf = RT.get((Object)map__205272, (Object)const__8, (Object)new ClientConfiguration());
        Object object8 = map__205272;
        map__205272 = null;
        Object creds_provider = RT.get((Object)object8, (Object)const__9, (Object)new DefaultAWSCredentialsProviderChain());
        Object object9 = retryPolicy;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = retryPolicy;
            retryPolicy = null;
            ((ClientConfiguration)client_conf).setRetryPolicy((RetryPolicy)object10);
        }
        Object object11 = client_conf;
        client_conf = null;
        Object object12 = creds_provider;
        creds_provider = null;
        Object object13 = region;
        region = null;
        return ((AwsClientBuilder)AmazonS3Client.builder()).withClientConfiguration((ClientConfiguration)object11).withCredentials((AWSCredentialsProvider)object12).withRegion((String)((IFn)const__10.getRawRoot()).invoke(object13)).build();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$client.invokeStatic(object2);
    }
}


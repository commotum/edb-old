/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.AmazonWebServiceRequest
 *  com.amazonaws.HttpMethod
 *  com.amazonaws.RequestClientOptions
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.AWSCredentialsProvider
 *  com.amazonaws.event.ProgressListener
 *  com.amazonaws.metrics.RequestMetricCollector
 *  com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
 *  com.amazonaws.services.s3.model.ResponseHeaderOverrides
 *  com.amazonaws.services.s3.model.SSECustomerKey
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.HttpMethod;
import com.amazonaws.RequestClientOptions;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.SSECustomerKey;
import java.util.Date;
import java.util.Map;

public final class s3_api$fn__22990
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"contentType");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"expiration");
    public static final Keyword const__6 = RT.keyword(null, (String)"bucketName");
    public static final Keyword const__7 = RT.keyword(null, (String)"versionId");
    public static final Keyword const__8 = RT.keyword(null, (String)"sSECustomerKey");
    public static final Keyword const__9 = RT.keyword(null, (String)"responseHeaders");
    public static final Keyword const__10 = RT.keyword(null, (String)"sSEAlgorithm");
    public static final Keyword const__11 = RT.keyword(null, (String)"zeroByteContent");
    public static final Keyword const__12 = RT.keyword(null, (String)"requestParameters");
    public static final Keyword const__13 = RT.keyword(null, (String)"contentMd5");
    public static final Keyword const__14 = RT.keyword(null, (String)"kmsCmkId");
    public static final Keyword const__15 = RT.keyword(null, (String)"method");
    public static final Keyword const__16 = RT.keyword(null, (String)"key");
    public static final Keyword const__17 = RT.keyword(null, (String)"requestMetricCollector");
    public static final Keyword const__18 = RT.keyword(null, (String)"requestCredentials");
    public static final Keyword const__19 = RT.keyword(null, (String)"requestCredentialsProvider");
    public static final Keyword const__20 = RT.keyword(null, (String)"generalProgressListener");
    public static final Keyword const__21 = RT.keyword(null, (String)"customRequestHeaders");
    public static final Keyword const__22 = RT.keyword(null, (String)"customQueryParameters");
    public static final Keyword const__23 = RT.keyword(null, (String)"readLimit");
    public static final Keyword const__24 = RT.keyword(null, (String)"cloneSource");
    public static final Keyword const__25 = RT.keyword(null, (String)"cloneRoot");
    public static final Keyword const__26 = RT.keyword(null, (String)"sdkRequestTimeout");
    public static final Keyword const__27 = RT.keyword(null, (String)"sdkClientExecutionTimeout");
    public static final Keyword const__28 = RT.keyword(null, (String)"requestClientOptions");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        RequestClientOptions temp__5457__auto__23041;
        IPersistentVector iPersistentVector2;
        Integer temp__5457__auto__23039;
        IPersistentVector iPersistentVector3;
        Integer temp__5457__auto__23037;
        IPersistentVector iPersistentVector4;
        AmazonWebServiceRequest temp__5457__auto__23035;
        IPersistentVector iPersistentVector5;
        AmazonWebServiceRequest temp__5457__auto__23033;
        IPersistentVector iPersistentVector6;
        IPersistentVector iPersistentVector7;
        Map temp__5457__auto__23029;
        IPersistentVector iPersistentVector8;
        Map temp__5457__auto__23027;
        IPersistentVector iPersistentVector9;
        ProgressListener temp__5457__auto__23025;
        IPersistentVector iPersistentVector10;
        AWSCredentialsProvider temp__5457__auto__23023;
        IPersistentVector iPersistentVector11;
        AWSCredentials temp__5457__auto__23021;
        IPersistentVector iPersistentVector12;
        RequestMetricCollector temp__5457__auto__23019;
        IPersistentVector iPersistentVector13;
        String temp__5457__auto__23017;
        IPersistentVector iPersistentVector14;
        HttpMethod temp__5457__auto__23015;
        IPersistentVector iPersistentVector15;
        String temp__5457__auto__23013;
        IPersistentVector iPersistentVector16;
        String temp__5457__auto__23011;
        IPersistentVector iPersistentVector17;
        Map temp__5457__auto__23009;
        IPersistentVector iPersistentVector18;
        IPersistentVector iPersistentVector19;
        String temp__5457__auto__23005;
        IPersistentVector iPersistentVector20;
        ResponseHeaderOverrides temp__5457__auto__23003;
        IPersistentVector iPersistentVector21;
        SSECustomerKey temp__5457__auto__23001;
        IPersistentVector iPersistentVector22;
        String temp__5457__auto__22999;
        IPersistentVector iPersistentVector23;
        String temp__5457__auto__22997;
        IPersistentVector iPersistentVector24;
        Date temp__5457__auto__22995;
        IPersistentVector iPersistentVector25;
        String temp__5457__auto__22993;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        String string = temp__5457__auto__22993 = ((GeneratePresignedUrlRequest)o).getContentType();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__22992;
            String string2 = temp__5457__auto__22993;
            temp__5457__auto__22993 = null;
            String string3 = v__17285__auto__22992 = string2;
            v__17285__auto__22992 = null;
            iPersistentVector25 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector25 = null;
        }
        Date date = temp__5457__auto__22995 = ((GeneratePresignedUrlRequest)o).getExpiration();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__22994;
            Date date2 = temp__5457__auto__22995;
            temp__5457__auto__22995 = null;
            Date date3 = v__17285__auto__22994 = date2;
            v__17285__auto__22994 = null;
            iPersistentVector24 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector24 = null;
        }
        String string4 = temp__5457__auto__22997 = ((GeneratePresignedUrlRequest)o).getBucketName();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__22996;
            String string5 = temp__5457__auto__22997;
            temp__5457__auto__22997 = null;
            String string6 = v__17285__auto__22996 = string5;
            v__17285__auto__22996 = null;
            iPersistentVector23 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector23 = null;
        }
        String string7 = temp__5457__auto__22999 = ((GeneratePresignedUrlRequest)o).getVersionId();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__22998;
            String string8 = temp__5457__auto__22999;
            temp__5457__auto__22999 = null;
            String string9 = v__17285__auto__22998 = string8;
            v__17285__auto__22998 = null;
            iPersistentVector22 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector22 = null;
        }
        SSECustomerKey sSECustomerKey = temp__5457__auto__23001 = ((GeneratePresignedUrlRequest)o).getSSECustomerKey();
        if (sSECustomerKey != null && sSECustomerKey != Boolean.FALSE) {
            SSECustomerKey v__17285__auto__23000;
            SSECustomerKey sSECustomerKey2 = temp__5457__auto__23001;
            temp__5457__auto__23001 = null;
            SSECustomerKey sSECustomerKey3 = v__17285__auto__23000 = sSECustomerKey2;
            v__17285__auto__23000 = null;
            iPersistentVector21 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)sSECustomerKey3));
        } else {
            iPersistentVector21 = null;
        }
        ResponseHeaderOverrides responseHeaderOverrides = temp__5457__auto__23003 = ((GeneratePresignedUrlRequest)o).getResponseHeaders();
        if (responseHeaderOverrides != null && responseHeaderOverrides != Boolean.FALSE) {
            ResponseHeaderOverrides v__17285__auto__23002;
            ResponseHeaderOverrides responseHeaderOverrides2 = temp__5457__auto__23003;
            temp__5457__auto__23003 = null;
            ResponseHeaderOverrides responseHeaderOverrides3 = v__17285__auto__23002 = responseHeaderOverrides2;
            v__17285__auto__23002 = null;
            iPersistentVector20 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)responseHeaderOverrides3));
        } else {
            iPersistentVector20 = null;
        }
        String string10 = temp__5457__auto__23005 = ((GeneratePresignedUrlRequest)o).getSSEAlgorithm();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__23004;
            String string11 = temp__5457__auto__23005;
            temp__5457__auto__23005 = null;
            String string12 = v__17285__auto__23004 = string11;
            v__17285__auto__23004 = null;
            iPersistentVector19 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector19 = null;
        }
        boolean temp__5457__auto__23007 = ((GeneratePresignedUrlRequest)o).isZeroByteContent();
        if (temp__5457__auto__23007) {
            boolean v__17285__auto__23006 = temp__5457__auto__23007;
            iPersistentVector18 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)(v__17285__auto__23006 ? Boolean.TRUE : Boolean.FALSE)));
        } else {
            iPersistentVector18 = null;
        }
        Map map2 = temp__5457__auto__23009 = ((GeneratePresignedUrlRequest)o).getRequestParameters();
        if (map2 != null && map2 != Boolean.FALSE) {
            Map v__17285__auto__23008;
            Map map3 = temp__5457__auto__23009;
            temp__5457__auto__23009 = null;
            Map map4 = v__17285__auto__23008 = map3;
            v__17285__auto__23008 = null;
            iPersistentVector17 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map4));
        } else {
            iPersistentVector17 = null;
        }
        String string13 = temp__5457__auto__23011 = ((GeneratePresignedUrlRequest)o).getContentMd5();
        if (string13 != null && string13 != Boolean.FALSE) {
            String v__17285__auto__23010;
            String string14 = temp__5457__auto__23011;
            temp__5457__auto__23011 = null;
            String string15 = v__17285__auto__23010 = string14;
            v__17285__auto__23010 = null;
            iPersistentVector16 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string15));
        } else {
            iPersistentVector16 = null;
        }
        String string16 = temp__5457__auto__23013 = ((GeneratePresignedUrlRequest)o).getKmsCmkId();
        if (string16 != null && string16 != Boolean.FALSE) {
            String v__17285__auto__23012;
            String string17 = temp__5457__auto__23013;
            temp__5457__auto__23013 = null;
            String string18 = v__17285__auto__23012 = string17;
            v__17285__auto__23012 = null;
            iPersistentVector15 = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string18));
        } else {
            iPersistentVector15 = null;
        }
        HttpMethod httpMethod = temp__5457__auto__23015 = ((GeneratePresignedUrlRequest)o).getMethod();
        if (httpMethod != null && httpMethod != Boolean.FALSE) {
            HttpMethod v__17285__auto__23014;
            HttpMethod httpMethod2 = temp__5457__auto__23015;
            temp__5457__auto__23015 = null;
            HttpMethod httpMethod3 = v__17285__auto__23014 = httpMethod2;
            v__17285__auto__23014 = null;
            iPersistentVector14 = Tuple.create((Object)const__15, (Object)((IFn)const__4.getRawRoot()).invoke((Object)httpMethod3));
        } else {
            iPersistentVector14 = null;
        }
        String string19 = temp__5457__auto__23017 = ((GeneratePresignedUrlRequest)o).getKey();
        if (string19 != null && string19 != Boolean.FALSE) {
            String v__17285__auto__23016;
            String string20 = temp__5457__auto__23017;
            temp__5457__auto__23017 = null;
            String string21 = v__17285__auto__23016 = string20;
            v__17285__auto__23016 = null;
            iPersistentVector13 = Tuple.create((Object)const__16, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string21));
        } else {
            iPersistentVector13 = null;
        }
        RequestMetricCollector requestMetricCollector = temp__5457__auto__23019 = ((AmazonWebServiceRequest)o).getRequestMetricCollector();
        if (requestMetricCollector != null && requestMetricCollector != Boolean.FALSE) {
            RequestMetricCollector v__17285__auto__23018;
            RequestMetricCollector requestMetricCollector2 = temp__5457__auto__23019;
            temp__5457__auto__23019 = null;
            RequestMetricCollector requestMetricCollector3 = v__17285__auto__23018 = requestMetricCollector2;
            v__17285__auto__23018 = null;
            iPersistentVector12 = Tuple.create((Object)const__17, (Object)((IFn)const__4.getRawRoot()).invoke((Object)requestMetricCollector3));
        } else {
            iPersistentVector12 = null;
        }
        AWSCredentials aWSCredentials = temp__5457__auto__23021 = ((AmazonWebServiceRequest)o).getRequestCredentials();
        if (aWSCredentials != null && aWSCredentials != Boolean.FALSE) {
            AWSCredentials v__17285__auto__23020;
            AWSCredentials aWSCredentials2 = temp__5457__auto__23021;
            temp__5457__auto__23021 = null;
            AWSCredentials aWSCredentials3 = v__17285__auto__23020 = aWSCredentials2;
            v__17285__auto__23020 = null;
            iPersistentVector11 = Tuple.create((Object)const__18, (Object)((IFn)const__4.getRawRoot()).invoke((Object)aWSCredentials3));
        } else {
            iPersistentVector11 = null;
        }
        AWSCredentialsProvider aWSCredentialsProvider = temp__5457__auto__23023 = ((AmazonWebServiceRequest)o).getRequestCredentialsProvider();
        if (aWSCredentialsProvider != null && aWSCredentialsProvider != Boolean.FALSE) {
            AWSCredentialsProvider v__17285__auto__23022;
            AWSCredentialsProvider aWSCredentialsProvider2 = temp__5457__auto__23023;
            temp__5457__auto__23023 = null;
            AWSCredentialsProvider aWSCredentialsProvider3 = v__17285__auto__23022 = aWSCredentialsProvider2;
            v__17285__auto__23022 = null;
            iPersistentVector10 = Tuple.create((Object)const__19, (Object)((IFn)const__4.getRawRoot()).invoke((Object)aWSCredentialsProvider3));
        } else {
            iPersistentVector10 = null;
        }
        ProgressListener progressListener = temp__5457__auto__23025 = ((AmazonWebServiceRequest)o).getGeneralProgressListener();
        if (progressListener != null && progressListener != Boolean.FALSE) {
            ProgressListener v__17285__auto__23024;
            ProgressListener progressListener2 = temp__5457__auto__23025;
            temp__5457__auto__23025 = null;
            ProgressListener progressListener3 = v__17285__auto__23024 = progressListener2;
            v__17285__auto__23024 = null;
            iPersistentVector9 = Tuple.create((Object)const__20, (Object)((IFn)const__4.getRawRoot()).invoke((Object)progressListener3));
        } else {
            iPersistentVector9 = null;
        }
        Map map5 = temp__5457__auto__23027 = ((AmazonWebServiceRequest)o).getCustomRequestHeaders();
        if (map5 != null && map5 != Boolean.FALSE) {
            Map v__17285__auto__23026;
            Map map6 = temp__5457__auto__23027;
            temp__5457__auto__23027 = null;
            Map map7 = v__17285__auto__23026 = map6;
            v__17285__auto__23026 = null;
            iPersistentVector8 = Tuple.create((Object)const__21, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map7));
        } else {
            iPersistentVector8 = null;
        }
        Map map8 = temp__5457__auto__23029 = ((AmazonWebServiceRequest)o).getCustomQueryParameters();
        if (map8 != null && map8 != Boolean.FALSE) {
            Map v__17285__auto__23028;
            Map map9 = temp__5457__auto__23029;
            temp__5457__auto__23029 = null;
            Map map10 = v__17285__auto__23028 = map9;
            v__17285__auto__23028 = null;
            iPersistentVector7 = Tuple.create((Object)const__22, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map10));
        } else {
            iPersistentVector7 = null;
        }
        int temp__5457__auto__23031 = ((AmazonWebServiceRequest)o).getReadLimit();
        Integer n = temp__5457__auto__23031;
        if (n != null && n != Boolean.FALSE) {
            int v__17285__auto__23030 = temp__5457__auto__23031;
            iPersistentVector6 = Tuple.create((Object)const__23, (Object)((IFn)const__4.getRawRoot()).invoke((Object)v__17285__auto__23030));
        } else {
            iPersistentVector6 = null;
        }
        Object[] objectArray = new Object[5];
        AmazonWebServiceRequest amazonWebServiceRequest = temp__5457__auto__23033 = ((AmazonWebServiceRequest)o).getCloneSource();
        if (amazonWebServiceRequest != null && amazonWebServiceRequest != Boolean.FALSE) {
            AmazonWebServiceRequest v__17285__auto__23032;
            AmazonWebServiceRequest amazonWebServiceRequest2 = temp__5457__auto__23033;
            temp__5457__auto__23033 = null;
            AmazonWebServiceRequest amazonWebServiceRequest3 = v__17285__auto__23032 = amazonWebServiceRequest2;
            v__17285__auto__23032 = null;
            iPersistentVector5 = Tuple.create((Object)const__24, (Object)((IFn)const__4.getRawRoot()).invoke((Object)amazonWebServiceRequest3));
        } else {
            iPersistentVector5 = null;
        }
        objectArray[0] = iPersistentVector5;
        AmazonWebServiceRequest amazonWebServiceRequest4 = temp__5457__auto__23035 = ((AmazonWebServiceRequest)o).getCloneRoot();
        if (amazonWebServiceRequest4 != null && amazonWebServiceRequest4 != Boolean.FALSE) {
            AmazonWebServiceRequest v__17285__auto__23034;
            AmazonWebServiceRequest amazonWebServiceRequest5 = temp__5457__auto__23035;
            temp__5457__auto__23035 = null;
            AmazonWebServiceRequest amazonWebServiceRequest6 = v__17285__auto__23034 = amazonWebServiceRequest5;
            v__17285__auto__23034 = null;
            iPersistentVector4 = Tuple.create((Object)const__25, (Object)((IFn)const__4.getRawRoot()).invoke((Object)amazonWebServiceRequest6));
        } else {
            iPersistentVector4 = null;
        }
        objectArray[1] = iPersistentVector4;
        Integer n2 = temp__5457__auto__23037 = ((AmazonWebServiceRequest)o).getSdkRequestTimeout();
        if (n2 != null && n2 != Boolean.FALSE) {
            Integer v__17285__auto__23036;
            Integer n3 = temp__5457__auto__23037;
            temp__5457__auto__23037 = null;
            Integer n4 = v__17285__auto__23036 = n3;
            v__17285__auto__23036 = null;
            iPersistentVector3 = Tuple.create((Object)const__26, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n4));
        } else {
            iPersistentVector3 = null;
        }
        objectArray[2] = iPersistentVector3;
        Integer n5 = temp__5457__auto__23039 = ((AmazonWebServiceRequest)o).getSdkClientExecutionTimeout();
        if (n5 != null && n5 != Boolean.FALSE) {
            Integer v__17285__auto__23038;
            Integer n6 = temp__5457__auto__23039;
            temp__5457__auto__23039 = null;
            Integer n7 = v__17285__auto__23038 = n6;
            v__17285__auto__23038 = null;
            iPersistentVector2 = Tuple.create((Object)const__27, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n7));
        } else {
            iPersistentVector2 = null;
        }
        objectArray[3] = iPersistentVector2;
        Object object2 = o;
        o = null;
        RequestClientOptions requestClientOptions = temp__5457__auto__23041 = ((AmazonWebServiceRequest)object2).getRequestClientOptions();
        if (requestClientOptions != null && requestClientOptions != Boolean.FALSE) {
            RequestClientOptions v__17285__auto__23040;
            RequestClientOptions requestClientOptions2 = temp__5457__auto__23041;
            temp__5457__auto__23041 = null;
            RequestClientOptions requestClientOptions3 = v__17285__auto__23040 = requestClientOptions2;
            v__17285__auto__23040 = null;
            iPersistentVector = Tuple.create((Object)const__28, (Object)((IFn)const__4.getRawRoot()).invoke((Object)requestClientOptions3));
        } else {
            iPersistentVector = null;
        }
        objectArray[4] = iPersistentVector;
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector25, (Object)iPersistentVector24, (Object)iPersistentVector23, (Object)iPersistentVector22, (Object)iPersistentVector21, (Object)iPersistentVector20, (Object)iPersistentVector19, (Object)iPersistentVector18, (Object)iPersistentVector17, (Object)iPersistentVector16, (Object)iPersistentVector15, (Object)iPersistentVector14, (Object)iPersistentVector13, (Object)iPersistentVector12, (Object)iPersistentVector11, (Object)iPersistentVector10, (Object)iPersistentVector9, (Object)iPersistentVector8, (Object)iPersistentVector7, (Object)iPersistentVector6, objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__22990.invokeStatic(object2);
    }
}


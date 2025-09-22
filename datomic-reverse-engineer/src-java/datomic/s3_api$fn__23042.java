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
 *  com.amazonaws.RequestClientOptions
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.AWSCredentialsProvider
 *  com.amazonaws.event.ProgressListener
 *  com.amazonaws.metrics.RequestMetricCollector
 *  com.amazonaws.services.s3.model.ListObjectsRequest
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
import com.amazonaws.RequestClientOptions;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import java.util.List;
import java.util.Map;

public final class s3_api$fn__23042
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"prefix");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"marker");
    public static final Keyword const__6 = RT.keyword(null, (String)"bucketName");
    public static final Keyword const__7 = RT.keyword(null, (String)"requesterPays");
    public static final Keyword const__8 = RT.keyword(null, (String)"delimiter");
    public static final Keyword const__9 = RT.keyword(null, (String)"maxKeys");
    public static final Keyword const__10 = RT.keyword(null, (String)"encodingType");
    public static final Keyword const__11 = RT.keyword(null, (String)"optionalObjectAttributes");
    public static final Keyword const__12 = RT.keyword(null, (String)"expectedBucketOwner");
    public static final Keyword const__13 = RT.keyword(null, (String)"requestMetricCollector");
    public static final Keyword const__14 = RT.keyword(null, (String)"requestCredentials");
    public static final Keyword const__15 = RT.keyword(null, (String)"requestCredentialsProvider");
    public static final Keyword const__16 = RT.keyword(null, (String)"generalProgressListener");
    public static final Keyword const__17 = RT.keyword(null, (String)"customRequestHeaders");
    public static final Keyword const__18 = RT.keyword(null, (String)"customQueryParameters");
    public static final Keyword const__19 = RT.keyword(null, (String)"readLimit");
    public static final Keyword const__20 = RT.keyword(null, (String)"cloneSource");
    public static final Keyword const__21 = RT.keyword(null, (String)"cloneRoot");
    public static final Keyword const__22 = RT.keyword(null, (String)"sdkRequestTimeout");
    public static final Keyword const__23 = RT.keyword(null, (String)"sdkClientExecutionTimeout");
    public static final Keyword const__24 = RT.keyword(null, (String)"requestClientOptions");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        RequestClientOptions temp__5457__auto__23085;
        IPersistentVector iPersistentVector2;
        Integer temp__5457__auto__23083;
        IPersistentVector iPersistentVector3;
        Integer temp__5457__auto__23081;
        IPersistentVector iPersistentVector4;
        AmazonWebServiceRequest temp__5457__auto__23079;
        IPersistentVector iPersistentVector5;
        AmazonWebServiceRequest temp__5457__auto__23077;
        IPersistentVector iPersistentVector6;
        IPersistentVector iPersistentVector7;
        Map temp__5457__auto__23073;
        IPersistentVector iPersistentVector8;
        Map temp__5457__auto__23071;
        IPersistentVector iPersistentVector9;
        ProgressListener temp__5457__auto__23069;
        IPersistentVector iPersistentVector10;
        AWSCredentialsProvider temp__5457__auto__23067;
        IPersistentVector iPersistentVector11;
        AWSCredentials temp__5457__auto__23065;
        IPersistentVector iPersistentVector12;
        RequestMetricCollector temp__5457__auto__23063;
        IPersistentVector iPersistentVector13;
        String temp__5457__auto__23061;
        IPersistentVector iPersistentVector14;
        List temp__5457__auto__23059;
        IPersistentVector iPersistentVector15;
        String temp__5457__auto__23057;
        IPersistentVector iPersistentVector16;
        Integer temp__5457__auto__23055;
        IPersistentVector iPersistentVector17;
        String temp__5457__auto__23053;
        IPersistentVector iPersistentVector18;
        IPersistentVector iPersistentVector19;
        String temp__5457__auto__23049;
        IPersistentVector iPersistentVector20;
        String temp__5457__auto__23047;
        IPersistentVector iPersistentVector21;
        String temp__5457__auto__23045;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        String string = temp__5457__auto__23045 = ((ListObjectsRequest)o).getPrefix();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__23044;
            String string2 = temp__5457__auto__23045;
            temp__5457__auto__23045 = null;
            String string3 = v__17285__auto__23044 = string2;
            v__17285__auto__23044 = null;
            iPersistentVector21 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector21 = null;
        }
        String string4 = temp__5457__auto__23047 = ((ListObjectsRequest)o).getMarker();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__23046;
            String string5 = temp__5457__auto__23047;
            temp__5457__auto__23047 = null;
            String string6 = v__17285__auto__23046 = string5;
            v__17285__auto__23046 = null;
            iPersistentVector20 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector20 = null;
        }
        String string7 = temp__5457__auto__23049 = ((ListObjectsRequest)o).getBucketName();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__23048;
            String string8 = temp__5457__auto__23049;
            temp__5457__auto__23049 = null;
            String string9 = v__17285__auto__23048 = string8;
            v__17285__auto__23048 = null;
            iPersistentVector19 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector19 = null;
        }
        boolean temp__5457__auto__23051 = ((ListObjectsRequest)o).isRequesterPays();
        if (temp__5457__auto__23051) {
            boolean v__17285__auto__23050 = temp__5457__auto__23051;
            iPersistentVector18 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)(v__17285__auto__23050 ? Boolean.TRUE : Boolean.FALSE)));
        } else {
            iPersistentVector18 = null;
        }
        String string10 = temp__5457__auto__23053 = ((ListObjectsRequest)o).getDelimiter();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__23052;
            String string11 = temp__5457__auto__23053;
            temp__5457__auto__23053 = null;
            String string12 = v__17285__auto__23052 = string11;
            v__17285__auto__23052 = null;
            iPersistentVector17 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector17 = null;
        }
        Integer n = temp__5457__auto__23055 = ((ListObjectsRequest)o).getMaxKeys();
        if (n != null && n != Boolean.FALSE) {
            Integer v__17285__auto__23054;
            Integer n2 = temp__5457__auto__23055;
            temp__5457__auto__23055 = null;
            Integer n3 = v__17285__auto__23054 = n2;
            v__17285__auto__23054 = null;
            iPersistentVector16 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n3));
        } else {
            iPersistentVector16 = null;
        }
        String string13 = temp__5457__auto__23057 = ((ListObjectsRequest)o).getEncodingType();
        if (string13 != null && string13 != Boolean.FALSE) {
            String v__17285__auto__23056;
            String string14 = temp__5457__auto__23057;
            temp__5457__auto__23057 = null;
            String string15 = v__17285__auto__23056 = string14;
            v__17285__auto__23056 = null;
            iPersistentVector15 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string15));
        } else {
            iPersistentVector15 = null;
        }
        List list = temp__5457__auto__23059 = ((ListObjectsRequest)o).getOptionalObjectAttributes();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__23058;
            List list2 = temp__5457__auto__23059;
            temp__5457__auto__23059 = null;
            List list3 = v__17285__auto__23058 = list2;
            v__17285__auto__23058 = null;
            iPersistentVector14 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector14 = null;
        }
        String string16 = temp__5457__auto__23061 = ((ListObjectsRequest)o).getExpectedBucketOwner();
        if (string16 != null && string16 != Boolean.FALSE) {
            String v__17285__auto__23060;
            String string17 = temp__5457__auto__23061;
            temp__5457__auto__23061 = null;
            String string18 = v__17285__auto__23060 = string17;
            v__17285__auto__23060 = null;
            iPersistentVector13 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string18));
        } else {
            iPersistentVector13 = null;
        }
        RequestMetricCollector requestMetricCollector = temp__5457__auto__23063 = ((AmazonWebServiceRequest)o).getRequestMetricCollector();
        if (requestMetricCollector != null && requestMetricCollector != Boolean.FALSE) {
            RequestMetricCollector v__17285__auto__23062;
            RequestMetricCollector requestMetricCollector2 = temp__5457__auto__23063;
            temp__5457__auto__23063 = null;
            RequestMetricCollector requestMetricCollector3 = v__17285__auto__23062 = requestMetricCollector2;
            v__17285__auto__23062 = null;
            iPersistentVector12 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)requestMetricCollector3));
        } else {
            iPersistentVector12 = null;
        }
        AWSCredentials aWSCredentials = temp__5457__auto__23065 = ((AmazonWebServiceRequest)o).getRequestCredentials();
        if (aWSCredentials != null && aWSCredentials != Boolean.FALSE) {
            AWSCredentials v__17285__auto__23064;
            AWSCredentials aWSCredentials2 = temp__5457__auto__23065;
            temp__5457__auto__23065 = null;
            AWSCredentials aWSCredentials3 = v__17285__auto__23064 = aWSCredentials2;
            v__17285__auto__23064 = null;
            iPersistentVector11 = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)aWSCredentials3));
        } else {
            iPersistentVector11 = null;
        }
        AWSCredentialsProvider aWSCredentialsProvider = temp__5457__auto__23067 = ((AmazonWebServiceRequest)o).getRequestCredentialsProvider();
        if (aWSCredentialsProvider != null && aWSCredentialsProvider != Boolean.FALSE) {
            AWSCredentialsProvider v__17285__auto__23066;
            AWSCredentialsProvider aWSCredentialsProvider2 = temp__5457__auto__23067;
            temp__5457__auto__23067 = null;
            AWSCredentialsProvider aWSCredentialsProvider3 = v__17285__auto__23066 = aWSCredentialsProvider2;
            v__17285__auto__23066 = null;
            iPersistentVector10 = Tuple.create((Object)const__15, (Object)((IFn)const__4.getRawRoot()).invoke((Object)aWSCredentialsProvider3));
        } else {
            iPersistentVector10 = null;
        }
        ProgressListener progressListener = temp__5457__auto__23069 = ((AmazonWebServiceRequest)o).getGeneralProgressListener();
        if (progressListener != null && progressListener != Boolean.FALSE) {
            ProgressListener v__17285__auto__23068;
            ProgressListener progressListener2 = temp__5457__auto__23069;
            temp__5457__auto__23069 = null;
            ProgressListener progressListener3 = v__17285__auto__23068 = progressListener2;
            v__17285__auto__23068 = null;
            iPersistentVector9 = Tuple.create((Object)const__16, (Object)((IFn)const__4.getRawRoot()).invoke((Object)progressListener3));
        } else {
            iPersistentVector9 = null;
        }
        Map map2 = temp__5457__auto__23071 = ((AmazonWebServiceRequest)o).getCustomRequestHeaders();
        if (map2 != null && map2 != Boolean.FALSE) {
            Map v__17285__auto__23070;
            Map map3 = temp__5457__auto__23071;
            temp__5457__auto__23071 = null;
            Map map4 = v__17285__auto__23070 = map3;
            v__17285__auto__23070 = null;
            iPersistentVector8 = Tuple.create((Object)const__17, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map4));
        } else {
            iPersistentVector8 = null;
        }
        Map map5 = temp__5457__auto__23073 = ((AmazonWebServiceRequest)o).getCustomQueryParameters();
        if (map5 != null && map5 != Boolean.FALSE) {
            Map v__17285__auto__23072;
            Map map6 = temp__5457__auto__23073;
            temp__5457__auto__23073 = null;
            Map map7 = v__17285__auto__23072 = map6;
            v__17285__auto__23072 = null;
            iPersistentVector7 = Tuple.create((Object)const__18, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map7));
        } else {
            iPersistentVector7 = null;
        }
        int temp__5457__auto__23075 = ((AmazonWebServiceRequest)o).getReadLimit();
        Integer n4 = temp__5457__auto__23075;
        if (n4 != null && n4 != Boolean.FALSE) {
            int v__17285__auto__23074 = temp__5457__auto__23075;
            iPersistentVector6 = Tuple.create((Object)const__19, (Object)((IFn)const__4.getRawRoot()).invoke((Object)v__17285__auto__23074));
        } else {
            iPersistentVector6 = null;
        }
        AmazonWebServiceRequest amazonWebServiceRequest = temp__5457__auto__23077 = ((AmazonWebServiceRequest)o).getCloneSource();
        if (amazonWebServiceRequest != null && amazonWebServiceRequest != Boolean.FALSE) {
            AmazonWebServiceRequest v__17285__auto__23076;
            AmazonWebServiceRequest amazonWebServiceRequest2 = temp__5457__auto__23077;
            temp__5457__auto__23077 = null;
            AmazonWebServiceRequest amazonWebServiceRequest3 = v__17285__auto__23076 = amazonWebServiceRequest2;
            v__17285__auto__23076 = null;
            iPersistentVector5 = Tuple.create((Object)const__20, (Object)((IFn)const__4.getRawRoot()).invoke((Object)amazonWebServiceRequest3));
        } else {
            iPersistentVector5 = null;
        }
        AmazonWebServiceRequest amazonWebServiceRequest4 = temp__5457__auto__23079 = ((AmazonWebServiceRequest)o).getCloneRoot();
        if (amazonWebServiceRequest4 != null && amazonWebServiceRequest4 != Boolean.FALSE) {
            AmazonWebServiceRequest v__17285__auto__23078;
            AmazonWebServiceRequest amazonWebServiceRequest5 = temp__5457__auto__23079;
            temp__5457__auto__23079 = null;
            AmazonWebServiceRequest amazonWebServiceRequest6 = v__17285__auto__23078 = amazonWebServiceRequest5;
            v__17285__auto__23078 = null;
            iPersistentVector4 = Tuple.create((Object)const__21, (Object)((IFn)const__4.getRawRoot()).invoke((Object)amazonWebServiceRequest6));
        } else {
            iPersistentVector4 = null;
        }
        Integer n5 = temp__5457__auto__23081 = ((AmazonWebServiceRequest)o).getSdkRequestTimeout();
        if (n5 != null && n5 != Boolean.FALSE) {
            Integer v__17285__auto__23080;
            Integer n6 = temp__5457__auto__23081;
            temp__5457__auto__23081 = null;
            Integer n7 = v__17285__auto__23080 = n6;
            v__17285__auto__23080 = null;
            iPersistentVector3 = Tuple.create((Object)const__22, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n7));
        } else {
            iPersistentVector3 = null;
        }
        Integer n8 = temp__5457__auto__23083 = ((AmazonWebServiceRequest)o).getSdkClientExecutionTimeout();
        if (n8 != null && n8 != Boolean.FALSE) {
            Integer v__17285__auto__23082;
            Integer n9 = temp__5457__auto__23083;
            temp__5457__auto__23083 = null;
            Integer n10 = v__17285__auto__23082 = n9;
            v__17285__auto__23082 = null;
            iPersistentVector2 = Tuple.create((Object)const__23, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n10));
        } else {
            iPersistentVector2 = null;
        }
        Object[] objectArray = new Object[1];
        Object object2 = o;
        o = null;
        RequestClientOptions requestClientOptions = temp__5457__auto__23085 = ((AmazonWebServiceRequest)object2).getRequestClientOptions();
        if (requestClientOptions != null && requestClientOptions != Boolean.FALSE) {
            RequestClientOptions v__17285__auto__23084;
            RequestClientOptions requestClientOptions2 = temp__5457__auto__23085;
            temp__5457__auto__23085 = null;
            RequestClientOptions requestClientOptions3 = v__17285__auto__23084 = requestClientOptions2;
            v__17285__auto__23084 = null;
            iPersistentVector = Tuple.create((Object)const__24, (Object)((IFn)const__4.getRawRoot()).invoke((Object)requestClientOptions3));
        } else {
            iPersistentVector = null;
        }
        objectArray[0] = iPersistentVector;
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector21, (Object)iPersistentVector20, (Object)iPersistentVector19, (Object)iPersistentVector18, (Object)iPersistentVector17, (Object)iPersistentVector16, (Object)iPersistentVector15, (Object)iPersistentVector14, (Object)iPersistentVector13, (Object)iPersistentVector12, (Object)iPersistentVector11, (Object)iPersistentVector10, (Object)iPersistentVector9, (Object)iPersistentVector8, (Object)iPersistentVector7, (Object)iPersistentVector6, (Object)iPersistentVector5, (Object)iPersistentVector4, (Object)iPersistentVector3, (Object)iPersistentVector2, objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__23042.invokeStatic(object2);
    }
}


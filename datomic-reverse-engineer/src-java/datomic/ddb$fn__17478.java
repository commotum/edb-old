/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.AmazonWebServiceRequest
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.AWSCredentialsProvider
 *  com.amazonaws.event.ProgressListener
 *  com.amazonaws.metrics.RequestMetricCollector
 *  com.amazonaws.services.dynamodbv2.model.CreateTableRequest
 *  com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
 *  com.amazonaws.services.dynamodbv2.model.SSESpecification
 *  com.amazonaws.services.dynamodbv2.model.StreamSpecification
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.SSESpecification;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import java.util.Collection;

public final class ddb$fn__17478
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"provisionedThroughput");
    public static final Keyword const__3 = RT.keyword(null, (String)"tags");
    public static final Keyword const__4 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__5 = RT.keyword(null, (String)"keySchema");
    public static final Keyword const__6 = RT.keyword(null, (String)"requestCredentials");
    public static final Keyword const__7 = RT.keyword(null, (String)"sdkClientExecutionTimeout");
    public static final Keyword const__8 = RT.keyword(null, (String)"localSecondaryIndexes");
    public static final Keyword const__9 = RT.keyword(null, (String)"deletionProtectionEnabled");
    public static final Keyword const__10 = RT.keyword(null, (String)"generalProgressListener");
    public static final Keyword const__11 = RT.keyword(null, (String)"billingMode");
    public static final Keyword const__12 = RT.keyword(null, (String)"tableClass");
    public static final Keyword const__13 = RT.keyword(null, (String)"sdkRequestTimeout");
    public static final Keyword const__14 = RT.keyword(null, (String)"streamSpecification");
    public static final Keyword const__15 = RT.keyword(null, (String)"requestMetricCollector");
    public static final Keyword const__16 = RT.keyword(null, (String)"globalSecondaryIndexes");
    public static final Keyword const__17 = RT.keyword(null, (String)"sSESpecification");
    public static final Keyword const__18 = RT.keyword(null, (String)"requestCredentialsProvider");
    public static final Keyword const__19 = RT.keyword(null, (String)"attributeDefinitions");
    public static final AFn const__20 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"provisionedThroughput"), RT.keyword(null, (String)"tags"), RT.keyword(null, (String)"tableName"), RT.keyword(null, (String)"keySchema"), RT.keyword(null, (String)"requestCredentials"), RT.keyword(null, (String)"sdkClientExecutionTimeout"), RT.keyword(null, (String)"localSecondaryIndexes"), RT.keyword(null, (String)"deletionProtectionEnabled"), RT.keyword(null, (String)"generalProgressListener"), RT.keyword(null, (String)"billingMode"), RT.keyword(null, (String)"tableClass"), RT.keyword(null, (String)"sdkRequestTimeout"), RT.keyword(null, (String)"streamSpecification"), RT.keyword(null, (String)"requestMetricCollector"), RT.keyword(null, (String)"globalSecondaryIndexes"), RT.keyword(null, (String)"sSESpecification"), RT.keyword(null, (String)"requestCredentialsProvider"), RT.keyword(null, (String)"attributeDefinitions")});
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__25 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__26 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"provisionedThroughput"), RT.keyword(null, (String)"tags"), RT.keyword(null, (String)"tableName"), RT.keyword(null, (String)"keySchema"), RT.keyword(null, (String)"requestCredentials"), RT.keyword(null, (String)"sdkClientExecutionTimeout"), RT.keyword(null, (String)"localSecondaryIndexes"), RT.keyword(null, (String)"deletionProtectionEnabled"), RT.keyword(null, (String)"generalProgressListener"), RT.keyword(null, (String)"billingMode"), RT.keyword(null, (String)"tableClass"), RT.keyword(null, (String)"sdkRequestTimeout"), RT.keyword(null, (String)"streamSpecification"), RT.keyword(null, (String)"requestMetricCollector"), RT.keyword(null, (String)"globalSecondaryIndexes"), RT.keyword(null, (String)"sSESpecification"), RT.keyword(null, (String)"requestCredentialsProvider"), RT.keyword(null, (String)"attributeDefinitions")});
    public static final Keyword const__27 = RT.keyword(null, (String)"keys");
    public static final Keyword const__28 = RT.keyword(null, (String)"constructor");
    public static final Object const__29 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.CreateTableRequest");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__31 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__33 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.StreamSpecification");
    public static final Object const__34 = RT.classForName((String)"java.lang.Boolean");
    public static final Object const__35 = Integer.TYPE;
    public static final Object const__36 = RT.classForName((String)"java.util.Collection");
    public static final Object const__37 = RT.classForName((String)"com.amazonaws.metrics.RequestMetricCollector");
    public static final Object const__38 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput");
    public static final Object const__39 = RT.classForName((String)"com.amazonaws.auth.AWSCredentials");
    public static final Object const__40 = RT.classForName((String)"java.lang.String");
    public static final Object const__41 = RT.classForName((String)"com.amazonaws.auth.AWSCredentialsProvider");
    public static final Object const__42 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.SSESpecification");
    public static final Object const__43 = RT.classForName((String)"com.amazonaws.event.ProgressListener");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"streamSpecification"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"deletionProtectionEnabled"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"sdkClientExecutionTimeout"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"keySchema"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"requestMetricCollector"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"provisionedThroughput"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"attributeDefinitions"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"requestCredentials"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"sdkRequestTimeout"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"tableName"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"tableClass"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"localSecondaryIndexes"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"tags"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"billingMode"));
    static ILookupThunk __thunk__13__ = __site__13__;
    static final KeywordLookupSite __site__14__ = new KeywordLookupSite(RT.keyword(null, (String)"globalSecondaryIndexes"));
    static ILookupThunk __thunk__14__ = __site__14__;
    static final KeywordLookupSite __site__15__ = new KeywordLookupSite(RT.keyword(null, (String)"requestCredentialsProvider"));
    static ILookupThunk __thunk__15__ = __site__15__;
    static final KeywordLookupSite __site__16__ = new KeywordLookupSite(RT.keyword(null, (String)"sSESpecification"));
    static ILookupThunk __thunk__16__ = __site__16__;
    static final KeywordLookupSite __site__17__ = new KeywordLookupSite(RT.keyword(null, (String)"generalProgressListener"));
    static ILookupThunk __thunk__17__ = __site__17__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__17480;
        Object object = temp__5457__auto__17480 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__20, ((IFn)const__21.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__17480;
            temp__5457__auto__17480 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__23.getRawRoot()).invoke(const__24.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__25;
            objectArray[1] = const__26;
            objectArray[2] = const__27;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__28;
            objectArray[5] = const__29;
            throw (Throwable)((IFn)const__22.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        CreateTableRequest o = new CreateTableRequest();
        Object object5 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__14);
        if (object5 != null && object5 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = m;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            Object object8 = v = object7;
            v = null;
            Object object9 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__14, object8, const__33);
            k = null;
            o.setStreamSpecification((StreamSpecification)object9);
        }
        Object object10 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__9);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object11 = m;
            Object object12 = iLookupThunk.get(object11);
            if (iLookupThunk == object12) {
                __thunk__1__ = __site__1__.fault(object11);
                object12 = __thunk__1__.get(object11);
            }
            Object object13 = v = object12;
            v = null;
            Object object14 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__9, object13, const__34);
            k = null;
            o.setDeletionProtectionEnabled((Boolean)object14);
        }
        Object object15 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__7);
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object16 = m;
            Object object17 = iLookupThunk.get(object16);
            if (iLookupThunk == object17) {
                __thunk__2__ = __site__2__.fault(object16);
                object17 = __thunk__2__.get(object16);
            }
            Object object18 = v = object17;
            v = null;
            Object object19 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__7, object18, const__35);
            k = null;
            ((AmazonWebServiceRequest)o).setSdkClientExecutionTimeout(RT.intCast((Object)((Number)object19)));
        }
        Object object20 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__5);
        if (object20 != null && object20 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__3__;
            Object object21 = m;
            Object object22 = iLookupThunk.get(object21);
            if (iLookupThunk == object22) {
                __thunk__3__ = __site__3__.fault(object21);
                object22 = __thunk__3__.get(object21);
            }
            Object object23 = v = object22;
            v = null;
            Object object24 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__5, object23, const__36);
            k = null;
            o.setKeySchema((Collection)object24);
        }
        Object object25 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__15);
        if (object25 != null && object25 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__4__;
            Object object26 = m;
            Object object27 = iLookupThunk.get(object26);
            if (iLookupThunk == object27) {
                __thunk__4__ = __site__4__.fault(object26);
                object27 = __thunk__4__.get(object26);
            }
            Object object28 = v = object27;
            v = null;
            Object object29 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__15, object28, const__37);
            k = null;
            ((AmazonWebServiceRequest)o).setRequestMetricCollector((RequestMetricCollector)object29);
        }
        Object object30 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__2);
        if (object30 != null && object30 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__5__;
            Object object31 = m;
            Object object32 = iLookupThunk.get(object31);
            if (iLookupThunk == object32) {
                __thunk__5__ = __site__5__.fault(object31);
                object32 = __thunk__5__.get(object31);
            }
            Object object33 = v = object32;
            v = null;
            Object object34 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__2, object33, const__38);
            k = null;
            o.setProvisionedThroughput((ProvisionedThroughput)object34);
        }
        Object object35 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__19);
        if (object35 != null && object35 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__6__;
            Object object36 = m;
            Object object37 = iLookupThunk.get(object36);
            if (iLookupThunk == object37) {
                __thunk__6__ = __site__6__.fault(object36);
                object37 = __thunk__6__.get(object36);
            }
            Object object38 = v = object37;
            v = null;
            Object object39 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__19, object38, const__36);
            k = null;
            o.setAttributeDefinitions((Collection)object39);
        }
        Object object40 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__6);
        if (object40 != null && object40 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__7__;
            Object object41 = m;
            Object object42 = iLookupThunk.get(object41);
            if (iLookupThunk == object42) {
                __thunk__7__ = __site__7__.fault(object41);
                object42 = __thunk__7__.get(object41);
            }
            Object object43 = v = object42;
            v = null;
            Object object44 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__6, object43, const__39);
            k = null;
            ((AmazonWebServiceRequest)o).setRequestCredentials((AWSCredentials)object44);
        }
        Object object45 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__13);
        if (object45 != null && object45 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__8__;
            Object object46 = m;
            Object object47 = iLookupThunk.get(object46);
            if (iLookupThunk == object47) {
                __thunk__8__ = __site__8__.fault(object46);
                object47 = __thunk__8__.get(object46);
            }
            Object object48 = v = object47;
            v = null;
            Object object49 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__13, object48, const__35);
            k = null;
            ((AmazonWebServiceRequest)o).setSdkRequestTimeout(RT.intCast((Object)((Number)object49)));
        }
        Object object50 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__4);
        if (object50 != null && object50 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__9__;
            Object object51 = m;
            Object object52 = iLookupThunk.get(object51);
            if (iLookupThunk == object52) {
                __thunk__9__ = __site__9__.fault(object51);
                object52 = __thunk__9__.get(object51);
            }
            Object object53 = v = object52;
            v = null;
            Object object54 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__4, object53, const__40);
            k = null;
            o.setTableName((String)object54);
        }
        Object object55 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__12);
        if (object55 != null && object55 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__10__;
            Object object56 = m;
            Object object57 = iLookupThunk.get(object56);
            if (iLookupThunk == object57) {
                __thunk__10__ = __site__10__.fault(object56);
                object57 = __thunk__10__.get(object56);
            }
            Object object58 = v = object57;
            v = null;
            Object object59 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__12, object58, const__40);
            k = null;
            o.setTableClass((String)object59);
        }
        Object object60 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__8);
        if (object60 != null && object60 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__11__;
            Object object61 = m;
            Object object62 = iLookupThunk.get(object61);
            if (iLookupThunk == object62) {
                __thunk__11__ = __site__11__.fault(object61);
                object62 = __thunk__11__.get(object61);
            }
            Object object63 = v = object62;
            v = null;
            Object object64 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__8, object63, const__36);
            k = null;
            o.setLocalSecondaryIndexes((Collection)object64);
        }
        Object object65 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__3);
        if (object65 != null && object65 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__12__;
            Object object66 = m;
            Object object67 = iLookupThunk.get(object66);
            if (iLookupThunk == object67) {
                __thunk__12__ = __site__12__.fault(object66);
                object67 = __thunk__12__.get(object66);
            }
            Object object68 = v = object67;
            v = null;
            Object object69 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__3, object68, const__36);
            k = null;
            o.setTags((Collection)object69);
        }
        Object object70 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__11);
        if (object70 != null && object70 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__13__;
            Object object71 = m;
            Object object72 = iLookupThunk.get(object71);
            if (iLookupThunk == object72) {
                __thunk__13__ = __site__13__.fault(object71);
                object72 = __thunk__13__.get(object71);
            }
            Object object73 = v = object72;
            v = null;
            Object object74 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__11, object73, const__40);
            k = null;
            o.setBillingMode((String)object74);
        }
        Object object75 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__16);
        if (object75 != null && object75 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__14__;
            Object object76 = m;
            Object object77 = iLookupThunk.get(object76);
            if (iLookupThunk == object77) {
                __thunk__14__ = __site__14__.fault(object76);
                object77 = __thunk__14__.get(object76);
            }
            Object object78 = v = object77;
            v = null;
            Object object79 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__16, object78, const__36);
            k = null;
            o.setGlobalSecondaryIndexes((Collection)object79);
        }
        Object object80 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__18);
        if (object80 != null && object80 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__15__;
            Object object81 = m;
            Object object82 = iLookupThunk.get(object81);
            if (iLookupThunk == object82) {
                __thunk__15__ = __site__15__.fault(object81);
                object82 = __thunk__15__.get(object81);
            }
            Object object83 = v = object82;
            v = null;
            Object object84 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__18, object83, const__41);
            k = null;
            ((AmazonWebServiceRequest)o).setRequestCredentialsProvider((AWSCredentialsProvider)object84);
        }
        Object object85 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__17);
        if (object85 != null && object85 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__16__;
            Object object86 = m;
            Object object87 = iLookupThunk.get(object86);
            if (iLookupThunk == object87) {
                __thunk__16__ = __site__16__.fault(object86);
                object87 = __thunk__16__.get(object86);
            }
            Object object88 = v = object87;
            v = null;
            Object object89 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__17, object88, const__42);
            k = null;
            o.setSSESpecification((SSESpecification)object89);
        }
        Object object90 = ((IFn)const__30.getRawRoot()).invoke(m, (Object)const__10);
        if (object90 != null && object90 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__17__;
            Object object91 = m;
            m = null;
            Object object92 = iLookupThunk.get(object91);
            if (iLookupThunk == object92) {
                __thunk__17__ = __site__17__.fault(object91);
                object92 = __thunk__17__.get(object91);
            }
            Object object93 = v = object92;
            v = null;
            Object object94 = k = ((IFn)const__31.getRawRoot()).invoke(((IFn)const__32.getRawRoot()).invoke((Object)o), (Object)const__10, object93, const__43);
            k = null;
            ((AmazonWebServiceRequest)o).setGeneralProgressListener((ProgressListener)object94);
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$fn__17478.invokeStatic(object3, object4);
    }
}


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
 *  com.amazonaws.services.dynamodbv2.model.ListTablesRequest
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
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;

public final class ddb$fn__17475
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"limit");
    public static final Keyword const__3 = RT.keyword(null, (String)"requestCredentials");
    public static final Keyword const__4 = RT.keyword(null, (String)"sdkClientExecutionTimeout");
    public static final Keyword const__5 = RT.keyword(null, (String)"generalProgressListener");
    public static final Keyword const__6 = RT.keyword(null, (String)"exclusiveStartTableName");
    public static final Keyword const__7 = RT.keyword(null, (String)"sdkRequestTimeout");
    public static final Keyword const__8 = RT.keyword(null, (String)"requestMetricCollector");
    public static final Keyword const__9 = RT.keyword(null, (String)"requestCredentialsProvider");
    public static final AFn const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"limit"), RT.keyword(null, (String)"requestCredentials"), RT.keyword(null, (String)"sdkClientExecutionTimeout"), RT.keyword(null, (String)"generalProgressListener"), RT.keyword(null, (String)"exclusiveStartTableName"), RT.keyword(null, (String)"sdkRequestTimeout"), RT.keyword(null, (String)"requestMetricCollector"), RT.keyword(null, (String)"requestCredentialsProvider")});
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__15 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__16 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"limit"), RT.keyword(null, (String)"requestCredentials"), RT.keyword(null, (String)"sdkClientExecutionTimeout"), RT.keyword(null, (String)"generalProgressListener"), RT.keyword(null, (String)"exclusiveStartTableName"), RT.keyword(null, (String)"sdkRequestTimeout"), RT.keyword(null, (String)"requestMetricCollector"), RT.keyword(null, (String)"requestCredentialsProvider")});
    public static final Keyword const__17 = RT.keyword(null, (String)"keys");
    public static final Keyword const__18 = RT.keyword(null, (String)"constructor");
    public static final Object const__19 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.ListTablesRequest");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__21 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__23 = RT.classForName((String)"java.lang.String");
    public static final Object const__24 = RT.classForName((String)"java.lang.Integer");
    public static final Object const__25 = RT.classForName((String)"com.amazonaws.auth.AWSCredentials");
    public static final Object const__26 = RT.classForName((String)"com.amazonaws.auth.AWSCredentialsProvider");
    public static final Object const__27 = RT.classForName((String)"com.amazonaws.metrics.RequestMetricCollector");
    public static final Object const__28 = RT.classForName((String)"com.amazonaws.event.ProgressListener");
    public static final Object const__29 = Integer.TYPE;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"exclusiveStartTableName"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"limit"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"requestCredentials"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"requestCredentialsProvider"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"requestMetricCollector"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"generalProgressListener"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"sdkRequestTimeout"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"sdkClientExecutionTimeout"));
    static ILookupThunk __thunk__7__ = __site__7__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__17477;
        Object object = temp__5457__auto__17477 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__10, ((IFn)const__11.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__17477;
            temp__5457__auto__17477 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__13.getRawRoot()).invoke(const__14.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__15;
            objectArray[1] = const__16;
            objectArray[2] = const__17;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__18;
            objectArray[5] = const__19;
            throw (Throwable)((IFn)const__12.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        ListTablesRequest o = new ListTablesRequest();
        Object object5 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__6);
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
            Object object9 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__6, object8, const__23);
            k = null;
            o.setExclusiveStartTableName((String)object9);
        }
        Object object10 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__2);
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
            Object object14 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__2, object13, const__24);
            k = null;
            o.setLimit((Integer)object14);
        }
        Object object15 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__3);
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
            Object object19 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__3, object18, const__25);
            k = null;
            ((AmazonWebServiceRequest)o).setRequestCredentials((AWSCredentials)object19);
        }
        Object object20 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__9);
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
            Object object24 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__9, object23, const__26);
            k = null;
            ((AmazonWebServiceRequest)o).setRequestCredentialsProvider((AWSCredentialsProvider)object24);
        }
        Object object25 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__8);
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
            Object object29 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__8, object28, const__27);
            k = null;
            ((AmazonWebServiceRequest)o).setRequestMetricCollector((RequestMetricCollector)object29);
        }
        Object object30 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__5);
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
            Object object34 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__5, object33, const__28);
            k = null;
            ((AmazonWebServiceRequest)o).setGeneralProgressListener((ProgressListener)object34);
        }
        Object object35 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__7);
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
            Object object39 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__7, object38, const__29);
            k = null;
            ((AmazonWebServiceRequest)o).setSdkRequestTimeout(RT.intCast((Object)((Number)object39)));
        }
        Object object40 = ((IFn)const__20.getRawRoot()).invoke(m, (Object)const__4);
        if (object40 != null && object40 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__7__;
            Object object41 = m;
            m = null;
            Object object42 = iLookupThunk.get(object41);
            if (iLookupThunk == object42) {
                __thunk__7__ = __site__7__.fault(object41);
                object42 = __thunk__7__.get(object41);
            }
            Object object43 = v = object42;
            v = null;
            Object object44 = k = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)o), (Object)const__4, object43, const__29);
            k = null;
            ((AmazonWebServiceRequest)o).setSdkClientExecutionTimeout(RT.intCast((Object)((Number)object44)));
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$fn__17475.invokeStatic(object3, object4);
    }
}


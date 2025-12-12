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
 *  com.amazonaws.services.cloudwatch.model.MetricDatum
 *  com.amazonaws.services.cloudwatch.model.StandardUnit
 *  com.amazonaws.services.cloudwatch.model.StatisticSet
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
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.StatisticSet;
import java.util.Collection;
import java.util.Date;

public final class cloudwatch$fn__22938
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"counts");
    public static final Keyword const__3 = RT.keyword(null, (String)"unit");
    public static final Keyword const__4 = RT.keyword(null, (String)"value");
    public static final Keyword const__5 = RT.keyword(null, (String)"dimensions");
    public static final Keyword const__6 = RT.keyword(null, (String)"storageResolution");
    public static final Keyword const__7 = RT.keyword(null, (String)"values");
    public static final Keyword const__8 = RT.keyword(null, (String)"metricName");
    public static final Keyword const__9 = RT.keyword(null, (String)"timestamp");
    public static final Keyword const__10 = RT.keyword(null, (String)"statisticValues");
    public static final AFn const__11 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"counts"), RT.keyword(null, (String)"unit"), RT.keyword(null, (String)"value"), RT.keyword(null, (String)"dimensions"), RT.keyword(null, (String)"storageResolution"), RT.keyword(null, (String)"values"), RT.keyword(null, (String)"metricName"), RT.keyword(null, (String)"timestamp"), RT.keyword(null, (String)"statisticValues")});
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__16 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__17 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"counts"), RT.keyword(null, (String)"unit"), RT.keyword(null, (String)"value"), RT.keyword(null, (String)"dimensions"), RT.keyword(null, (String)"storageResolution"), RT.keyword(null, (String)"values"), RT.keyword(null, (String)"metricName"), RT.keyword(null, (String)"timestamp"), RT.keyword(null, (String)"statisticValues")});
    public static final Keyword const__18 = RT.keyword(null, (String)"keys");
    public static final Keyword const__19 = RT.keyword(null, (String)"constructor");
    public static final Object const__20 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.MetricDatum");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__22 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__24 = RT.classForName((String)"java.util.Date");
    public static final Object const__25 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.StandardUnit");
    public static final Object const__26 = RT.classForName((String)"java.lang.String");
    public static final Object const__27 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.StatisticSet");
    public static final Object const__28 = RT.classForName((String)"java.util.Collection");
    public static final Object const__29 = RT.classForName((String)"java.lang.Integer");
    public static final Object const__30 = RT.classForName((String)"java.lang.Double");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"timestamp"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"unit"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"metricName"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"statisticValues"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"dimensions"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"storageResolution"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"counts"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"values"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__8__ = __site__8__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__22940;
        Object object = temp__5457__auto__22940 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__11, ((IFn)const__12.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__22940;
            temp__5457__auto__22940 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__16;
            objectArray[1] = const__17;
            objectArray[2] = const__18;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__19;
            objectArray[5] = const__20;
            throw (Throwable)((IFn)const__13.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        MetricDatum o = new MetricDatum();
        Object object5 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__9);
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
            Object object9 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__9, object8, const__24);
            k = null;
            o.setTimestamp((Date)object9);
        }
        Object object10 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__3);
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
            Object object14 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__3, object13, const__25);
            k = null;
            o.setUnit((StandardUnit)object14);
        }
        Object object15 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__8);
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
            Object object19 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__8, object18, const__26);
            k = null;
            o.setMetricName((String)object19);
        }
        Object object20 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__10);
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
            Object object24 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__10, object23, const__27);
            k = null;
            o.setStatisticValues((StatisticSet)object24);
        }
        Object object25 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__5);
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
            Object object29 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__5, object28, const__28);
            k = null;
            o.setDimensions((Collection)object29);
        }
        Object object30 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__6);
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
            Object object34 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__6, object33, const__29);
            k = null;
            o.setStorageResolution((Integer)object34);
        }
        Object object35 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__2);
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
            Object object39 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__2, object38, const__28);
            k = null;
            o.setCounts((Collection)object39);
        }
        Object object40 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__7);
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
            Object object44 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__7, object43, const__28);
            k = null;
            o.setValues((Collection)object44);
        }
        Object object45 = ((IFn)const__21.getRawRoot()).invoke(m, (Object)const__4);
        if (object45 != null && object45 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__8__;
            Object object46 = m;
            m = null;
            Object object47 = iLookupThunk.get(object46);
            if (iLookupThunk == object47) {
                __thunk__8__ = __site__8__.fault(object46);
                object47 = __thunk__8__.get(object46);
            }
            Object object48 = v = object47;
            v = null;
            Object object49 = k = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)o), (Object)const__4, object48, const__30);
            k = null;
            o.setValue((Double)object49);
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cloudwatch$fn__22938.invokeStatic(object3, object4);
    }
}


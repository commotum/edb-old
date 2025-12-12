/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.api$cancel$throw_incorrect_anom__19631;

public final class api$cancel
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final AFn const__6 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword((String)"cognitect.anomalies", (String)"conflict"), RT.keyword((String)"cognitect.anomalies", (String)"incorrect")});
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"datomic.fressian", (String)"fressianable?");
    public static final Keyword const__11 = RT.keyword(null, (String)"default");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final AFn const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"datomic", (String)"cancelled"), Boolean.TRUE});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"message"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object p__19629) {
        Object object;
        Object map__19630;
        Object object2;
        Object object3 = p__19629;
        p__19629 = null;
        Object map__196302 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__196302);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__196302;
            map__196302 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__196302;
            map__196302 = null;
        }
        Object anomaly_map = map__19630 = object2;
        Object object6 = map__19630;
        map__19630 = null;
        Object category = RT.get((Object)object6, (Object)const__3);
        AFn allowed_anoms = const__6;
        api$cancel$throw_incorrect_anom__19631 throw_incorrect_anom = new api$cancel$throw_incorrect_anom__19631();
        Object object7 = ((IFn)const__7.getRawRoot()).invoke(category);
        if (object7 != null && object7 != Boolean.FALSE) {
            api$cancel$throw_incorrect_anom__19631 api$cancel$throw_incorrect_anom__19631 = throw_incorrect_anom;
            throw_incorrect_anom = null;
            object = ((IFn)api$cancel$throw_incorrect_anom__19631).invoke((Object)"Cancel requires :cognitect.anomalies/category");
        } else {
            AFn aFn = allowed_anoms;
            allowed_anoms = null;
            Object object8 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)RT.set((Object[])new Object[]{category}), (Object)aFn));
            if (object8 != null && object8 != Boolean.FALSE) {
                api$cancel$throw_incorrect_anom__19631 api$cancel$throw_incorrect_anom__19631 = throw_incorrect_anom;
                throw_incorrect_anom = null;
                Object object9 = category;
                category = null;
                object = ((IFn)api$cancel$throw_incorrect_anom__19631).invoke(((IFn)const__9.getRawRoot()).invoke((Object)"Invalid :cognitect.anomalies/category provided to cancel: ", object9));
            } else {
                Object object10 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(anomaly_map));
                if (object10 != null && object10 != Boolean.FALSE) {
                    api$cancel$throw_incorrect_anom__19631 api$cancel$throw_incorrect_anom__19631 = throw_incorrect_anom;
                    throw_incorrect_anom = null;
                    object = ((IFn)api$cancel$throw_incorrect_anom__19631).invoke((Object)"Could not marshal data in cancel anomaly");
                } else {
                    Keyword keyword = const__11;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object11;
                        Object or__5238__auto__19634;
                        IFn iFn = (IFn)const__12.getRawRoot();
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object12 = anomaly_map;
                        Object object13 = iLookupThunk.get(object12);
                        if (iLookupThunk == object13) {
                            __thunk__0__ = __site__0__.fault(object12);
                            object13 = __thunk__0__.get(object12);
                        }
                        Object object14 = or__5238__auto__19634 = object13;
                        if (object14 != null && object14 != Boolean.FALSE) {
                            object11 = or__5238__auto__19634;
                            or__5238__auto__19634 = null;
                        } else {
                            object11 = "Operation Cancelled";
                        }
                        Object object15 = anomaly_map;
                        anomaly_map = null;
                        throw (Throwable)iFn.invoke(object11, ((IFn)const__14.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, object15, (Object)const__16));
                    }
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$cancel.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class stats$key_summary
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"key-summary");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__4 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    public static final Keyword const__5 = RT.keyword(null, (String)"key");
    public static final Var const__6 = RT.var((String)"datomic.math", (String)"mean-and-stddev");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__8 = RT.keyword(null, (String)"key-size-min");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"min");
    public static final Keyword const__11 = RT.keyword(null, (String)"key-size-max");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"max");
    public static final Keyword const__13 = RT.keyword(null, (String)"key-size-mean");
    public static final Keyword const__16 = RT.keyword(null, (String)"key-size-stddev");
    public static final Keyword const__18 = RT.keyword(null, (String)"sparse-vs");
    public static final Var const__19 = RT.var((String)"datomic.stats", (String)"sparse-v-count");
    public static final Keyword const__20 = RT.keyword(null, (String)"a-freqs");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"frequencies");
    public static final Keyword const__22 = RT.keyword(null, (String)"a");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"mean"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"stddev"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object des, Object a_freqs_QMARK_) {
        Object object;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(des);
        if (object2 != null && object2 != Boolean.FALSE) {
            IPersistentMap iPersistentMap;
            Object sizes2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), (Object)const__5), des);
            Object ms = ((IFn)const__6.getRawRoot()).invoke(sizes2);
            IFn iFn = (IFn)const__7.getRawRoot();
            Object[] objectArray = new Object[10];
            objectArray[0] = const__8;
            objectArray[1] = ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), sizes2);
            objectArray[2] = const__11;
            Object object3 = sizes2;
            sizes2 = null;
            objectArray[3] = ((IFn)const__9.getRawRoot()).invoke(const__12.getRawRoot(), object3);
            objectArray[4] = const__13;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = ms;
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__0__ = __site__0__.fault(object4);
                object5 = __thunk__0__.get(object4);
            }
            objectArray[5] = Numbers.num((long)RT.longCast((Object)object5));
            objectArray[6] = const__16;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object6 = ms;
            ms = null;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__1__ = __site__1__.fault(object6);
                object7 = __thunk__1__.get(object6);
            }
            objectArray[7] = Numbers.num((long)RT.longCast((Object)object7));
            objectArray[8] = const__18;
            objectArray[9] = ((IFn)const__19.getRawRoot()).invoke(des);
            IPersistentMap iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
            Object object8 = a_freqs_QMARK_;
            a_freqs_QMARK_ = null;
            if (object8 != null && object8 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__20;
                Object object9 = des;
                des = null;
                objectArray2[1] = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)const__22, (Object)const__5), object9));
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap = null;
            }
            object = iFn.invoke((Object)iPersistentMap2, iPersistentMap);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return stats$key_summary.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object des) {
        Object object = des;
        des = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$key_summary.invokeStatic(object2);
    }
}


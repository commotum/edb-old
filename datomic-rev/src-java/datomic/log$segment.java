/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.log.Log;

public final class log$segment
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Keyword const__16;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cluster2, Object olookup) {
        Object object;
        Object root_id2;
        Object object2;
        Object tail;
        block12: {
            block11: {
                Object map__16564;
                Object object3;
                Object map__16563;
                Object object4;
                Object map__165632 = ((IFn)const__0.getRawRoot()).invoke(cluster2, olookup);
                Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__165632);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__165632;
                    map__165632 = null;
                    object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object6)));
                } else {
                    object4 = map__165632;
                    map__165632 = null;
                }
                Object log2 = map__16563 = object4;
                Object desc = RT.get((Object)map__16563, (Object)const__4);
                Object object7 = map__16563;
                map__16563 = null;
                tail = RT.get((Object)object7, (Object)const__5);
                Object object8 = ((IFn)const__6.getRawRoot()).invoke(tail);
                if (object8 != null && object8 != Boolean.FALSE) {
                    object2 = log2;
                    return object2;
                }
                IFn iFn = (IFn)const__7.getRawRoot();
                Object object9 = olookup;
                olookup = null;
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object10 = desc;
                desc = null;
                Object object11 = iLookupThunk.get(object10);
                if (iLookupThunk == object11) {
                    __thunk__0__ = __site__0__.fault(object10);
                    object11 = __thunk__0__.get(object10);
                }
                Object map__165642 = iFn.invoke(cluster2, object9, object11, const__9.getRawRoot(), ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), tail));
                Object object12 = ((IFn)const__1.getRawRoot()).invoke(map__165642);
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object object13 = map__165642;
                    map__165642 = null;
                    object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object13)));
                } else {
                    object3 = map__165642;
                    map__165642 = null;
                }
                Object object14 = map__16564 = object3;
                map__16564 = null;
                root_id2 = RT.get((Object)object14, (Object)const__12);
                Object object15 = log2;
                log2 = null;
                object = object15;
                if (Util.classOf((Object)object15) == __cached_class__0) break block11;
                if (object instanceof Log) break block12;
                object = object;
                __cached_class__0 = Util.classOf((Object)object);
            }
            Object object16 = cluster2;
            cluster2 = null;
            Object object17 = root_id2;
            root_id2 = null;
            IFn iFn = (IFn)const__14.getRawRoot();
            IFn iFn2 = (IFn)const__15.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object18 = tail;
            tail = null;
            Object object19 = iLookupThunk.get(object18);
            if (iLookupThunk == object19) {
                __thunk__1__ = __site__1__.fault(object18);
                object19 = __thunk__1__.get(object18);
            }
            object2 = const__13.getRawRoot().invoke(object, object16, object17, iFn.invoke(iFn2.invoke((Object)const__16, object19)));
            return object2;
        }
        Log log3 = (Log)object;
        Object object20 = cluster2;
        cluster2 = null;
        Object object21 = root_id2;
        root_id2 = null;
        IFn iFn = (IFn)const__14.getRawRoot();
        IFn iFn3 = (IFn)const__15.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__1__;
        Object object22 = tail;
        tail = null;
        Object object23 = iLookupThunk.get(object22);
        if (iLookupThunk == object23) {
            __thunk__1__ = __site__1__.fault(object22);
            object23 = __thunk__1__.get(object22);
        }
        object2 = log3.adopt_root(object20, object21, iFn.invoke(iFn3.invoke((Object)const__16, object23)));
        return object2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$segment.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"find-log");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.keyword(null, (String)"desc");
        const__5 = RT.keyword(null, (String)"tail");
        const__6 = RT.var((String)"datomic.log", (String)"tail-empty?");
        const__7 = RT.var((String)"datomic.log", (String)"extend-tree");
        const__9 = RT.var((String)"datomic.log", (String)"dir-threshold");
        const__10 = RT.var((String)"datomic.log", (String)"create-leaves");
        const__11 = RT.var((String)"datomic.log", (String)"segment-threshold");
        const__12 = RT.keyword(null, (String)"root-id");
        const__13 = RT.var((String)"datomic.log", (String)"adopt-root");
        const__14 = RT.var((String)"clojure.core", (String)"last");
        const__15 = RT.var((String)"clojure.core", (String)"map");
        const__16 = RT.keyword(null, (String)"t");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"txes"));
        __thunk__1__ = __site__1__;
    }
}


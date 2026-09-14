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
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class integrity$path_to_t
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"read-tail-descriptor");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"key-comparator");
    public static final Var const__8 = RT.var((String)"datomic.log", (String)"log-key");
    public static final Var const__9 = RT.var((String)"datomic.log", (String)"btree-search");
    public static final Var const__14 = RT.var((String)"datomic.log", (String)"binary-search");
    public static final Keyword const__15 = RT.keyword(null, (String)"rootid");
    public static final Keyword const__16 = RT.keyword(null, (String)"ridx");
    public static final Keyword const__17 = RT.keyword(null, (String)"dirid");
    public static final Keyword const__18 = RT.keyword(null, (String)"didx");
    public static final Keyword const__19 = RT.keyword(null, (String)"segid");
    public static final Keyword const__20 = RT.keyword(null, (String)"segidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object cs, Object olookup, Object t) {
        IPersistentMap iPersistentMap;
        Object temp__5457__auto__22228;
        Object object;
        Object G__22226;
        Object object2;
        Object object3 = cs;
        cs = null;
        Object G__222262 = ((IFn)const__0.getRawRoot()).invoke(object3);
        if (Util.identical((Object)G__222262, null)) {
            object2 = null;
        } else {
            G__222262 = null;
            object2 = G__22226 = ((IFn)const__2.getRawRoot()).invoke(G__222262);
        }
        if (Util.identical(G__22226, null)) {
            object = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = G__22226;
            G__22226 = null;
            object = iLookupThunk.get(object4);
            if (iLookupThunk == object) {
                __thunk__0__ = __site__0__.fault(object4);
                object = __thunk__0__.get(object4);
            }
        }
        Object object5 = temp__5457__auto__22228 = object;
        if (object5 != null && object5 != Boolean.FALSE) {
            Object root_val_key;
            Object object6 = temp__5457__auto__22228;
            temp__5457__auto__22228 = null;
            Object object7 = root_val_key = object6;
            root_val_key = null;
            Object rootid = ((IFn)const__4.getRawRoot()).invoke(object7);
            Object root = ((IFn)const__5.getRawRoot()).invoke(RT.get((Object)olookup, (Object)rootid));
            Object comp2 = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot());
            Object ridx = ((IFn)const__9.getRawRoot()).invoke(root, t, comp2);
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object8 = root;
            root = null;
            Object object9 = RT.nth((Object)object8, (int)RT.intCast((Object)((Number)ridx)));
            Object object10 = iLookupThunk.get(object9);
            if (iLookupThunk == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            Object dirid = object10;
            Object dir = RT.get((Object)olookup, (Object)dirid);
            if ((long)RT.count((Object)dir) == 0L) {
                iPersistentMap = null;
            } else {
                Object seg;
                Object didx = ((IFn)const__9.getRawRoot()).invoke(dir, t, comp2);
                ILookupThunk iLookupThunk2 = __thunk__2__;
                Object object11 = dir;
                dir = null;
                Object object12 = RT.nth((Object)object11, (int)RT.intCast((Object)((Number)didx)));
                Object object13 = iLookupThunk2.get(object12);
                if (iLookupThunk2 == object13) {
                    __thunk__2__ = __site__2__.fault(object12);
                    object13 = __thunk__2__.get(object12);
                }
                Object segid = object13;
                Object object14 = olookup;
                olookup = null;
                Object object15 = seg = RT.get((Object)object14, (Object)segid);
                seg = null;
                Object object16 = t;
                t = null;
                Object object17 = comp2;
                comp2 = null;
                Object sidx = ((IFn)const__14.getRawRoot()).invoke(object15, object16, object17);
                Object[] objectArray = new Object[12];
                objectArray[0] = const__15;
                Object object18 = rootid;
                rootid = null;
                objectArray[1] = object18;
                objectArray[2] = const__16;
                Object object19 = ridx;
                ridx = null;
                objectArray[3] = object19;
                objectArray[4] = const__17;
                Object object20 = dirid;
                dirid = null;
                objectArray[5] = object20;
                objectArray[6] = const__18;
                Object object21 = didx;
                didx = null;
                objectArray[7] = object21;
                objectArray[8] = const__19;
                Object object22 = segid;
                segid = null;
                objectArray[9] = object22;
                objectArray[10] = const__20;
                Object object23 = sidx;
                sidx = null;
                objectArray[11] = object23;
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            }
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return integrity$path_to_t.invokeStatic(object4, object5, object6);
    }
}


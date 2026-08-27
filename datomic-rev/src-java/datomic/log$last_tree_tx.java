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

public final class log$last_tree_tx
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__9;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    public static Object invokeStatic(Object olookup, Object root_id2) {
        Object object;
        Object object2;
        Object dir_id;
        Object object3 = root_id2;
        root_id2 = null;
        Object root = ((IFn)const__7.getRawRoot()).invoke(olookup, object3);
        ILookupThunk iLookupThunk = __thunk__1__;
        Object object4 = root;
        root = null;
        Object object5 = ((IFn)const__9.getRawRoot()).invoke(object4);
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        Object object7 = dir_id = object6;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = dir_id;
            dir_id = null;
            object2 = ((IFn)const__7.getRawRoot()).invoke(olookup, object8);
        } else {
            object2 = null;
        }
        Object dir = object2;
        ILookupThunk iLookupThunk2 = __thunk__2__;
        Object object9 = dir;
        dir = null;
        Object object10 = ((IFn)const__9.getRawRoot()).invoke(object9);
        Object object11 = iLookupThunk2.get(object10);
        if (iLookupThunk2 == object11) {
            __thunk__2__ = __site__2__.fault(object10);
            object11 = __thunk__2__.get(object10);
        }
        Object leaf_id = object11;
        IFn iFn = (IFn)const__9.getRawRoot();
        Object object12 = leaf_id;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object object13 = olookup;
            olookup = null;
            Object object14 = leaf_id;
            leaf_id = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object13, object14);
        } else {
            object = null;
        }
        return iFn.invoke(object);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$last_tree_tx.invokeStatic(object3, object4);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object p__16524) {
        Object object;
        Object or__5238__auto__16527;
        Object map__16525;
        Object object2;
        Object object3 = p__16524;
        p__16524 = null;
        Object map__165252 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__165252);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__165252;
            map__165252 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__165252;
            map__165252 = null;
        }
        Object log2 = map__16525 = object2;
        Object object6 = map__16525;
        map__16525 = null;
        Object olookup = RT.get((Object)object6, (Object)const__3);
        IFn iFn = (IFn)const__4.getRawRoot();
        Object object7 = olookup;
        olookup = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object8 = log2;
        Object object9 = iLookupThunk.get(object8);
        if (iLookupThunk == object9) {
            __thunk__0__ = __site__0__.fault(object8);
            object9 = __thunk__0__.get(object8);
        }
        Object object10 = or__5238__auto__16527 = object9;
        if (object10 != null && object10 != Boolean.FALSE) {
            object = or__5238__auto__16527;
            return iFn.invoke(object7, object);
        }
        Object object11 = log2;
        log2 = null;
        Object object12 = object11;
        if (Util.classOf((Object)object11) != __cached_class__0) {
            if (object12 instanceof Log) {
                object = ((Log)object12).get_root_id();
                return iFn.invoke(object7, object);
            }
            object12 = object12;
            __cached_class__0 = Util.classOf((Object)object12);
        }
        object = const__6.getRawRoot().invoke(object12);
        return iFn.invoke(object7, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$last_tree_tx.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"olookup");
        const__4 = RT.var((String)"datomic.log", (String)"last-tree-tx");
        const__6 = RT.var((String)"datomic.log", (String)"get-root-id");
        const__7 = RT.var((String)"datomic.common", (String)"getx");
        const__9 = RT.var((String)"datomic.log", (String)"last-by-nth");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"root-id"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
        __thunk__2__ = __site__2__;
    }
}


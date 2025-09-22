/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class garbage$gc_get_node
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object uuid) {
        v0 = garbage$gc_get_node.__thunk__0__;
        v1 = (IFn)garbage$gc_get_node.const__1.getRawRoot();
        v2 = cluster;
        cluster = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == garbage$gc_get_node.__cached_class__0) ** GOTO lbl10
        if (!(v3 instanceof ClusteredStore)) {
            v3 = v3;
            garbage$gc_get_node.__cached_class__0 = Util.classOf((Object)v3);
lbl10:
            // 2 sources

            v4 = uuid;
            uuid = null;
            v5 = garbage$gc_get_node.const__2.getRawRoot().invoke(v3, ((IFn)garbage$gc_get_node.const__3.getRawRoot()).invoke(v4));
        } else {
            v6 = uuid;
            uuid = null;
            v5 = ((ClusteredStore)v3).get_val(((IFn)garbage$gc_get_node.const__3.getRawRoot()).invoke(v6));
        }
        v7 = v1.invoke(v5);
        v8 = v0.get(v7);
        if (v0 == v8) {
            garbage$gc_get_node.__thunk__0__ = garbage$gc_get_node.__site__0__.fault(v7);
            v8 = garbage$gc_get_node.__thunk__0__.get(v7);
        }
        v9 = temp__5457__auto__19832 = v8;
        if (v9 != null && v9 != Boolean.FALSE) {
            v10 = temp__5457__auto__19832;
            temp__5457__auto__19832 = null;
            v11 = v = v10;
            v = null;
            v12 = ((IFn)garbage$gc_get_node.const__4.getRawRoot()).invoke(v11);
        } else {
            v12 = null;
        }
        return v12;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$gc_get_node.invokeStatic(object3, object4);
    }

    static {
        const__1 = RT.var((String)"clojure.core", (String)"deref");
        const__2 = RT.var((String)"datomic.cluster", (String)"get-val");
        const__3 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__4 = RT.var((String)"datomic.garbage", (String)"gc-val->obj");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__0__ = __site__0__;
    }
}


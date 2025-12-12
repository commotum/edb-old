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

public final class log$root_id
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs) {
        v0 = log$root_id.__thunk__0__;
        v1 = (IFn)log$root_id.const__1.getRawRoot();
        v2 = cs;
        if (Util.classOf((Object)v2) == log$root_id.__cached_class__0) ** GOTO lbl8
        if (!(v2 instanceof ClusteredStore)) {
            v2 = v2;
            log$root_id.__cached_class__0 = Util.classOf((Object)v2);
lbl8:
            // 2 sources

            v3 = cs;
            cs = null;
            v4 = log$root_id.const__2.getRawRoot().invoke(v2, ((IFn)log$root_id.const__3.getRawRoot()).invoke(v3));
        } else {
            v5 = cs;
            cs = null;
            v4 = ((ClusteredStore)v2).get_pod_meta(((IFn)log$root_id.const__3.getRawRoot()).invoke(v5));
        }
        v6 = v1.invoke(v4);
        v7 = v0.get(v6);
        if (v0 == v7) {
            log$root_id.__thunk__0__ = log$root_id.__site__0__.fault(v6);
            v7 = log$root_id.__thunk__0__.get(v6);
        }
        return v7;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$root_id.invokeStatic(object2);
    }

    static {
        const__1 = RT.var((String)"clojure.core", (String)"deref");
        const__2 = RT.var((String)"datomic.cluster", (String)"get-pod-meta");
        const__3 = RT.var((String)"datomic.log", (String)"tail-pod-key");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
        __thunk__0__ = __site__0__;
    }
}


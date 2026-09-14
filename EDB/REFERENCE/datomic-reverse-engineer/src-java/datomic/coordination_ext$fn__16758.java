/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.coordination_ext$fn__16758$fn__16759;

public final class coordination_ext$fn__16758
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.coordination-ext", (String)"remote-sql-stores");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__5 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"system-root"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object kvs;
        Object object;
        Object or__5238__auto__16762;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object ck = object3;
        Object object4 = or__5238__auto__16762 = RT.get((Object)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot()), (Object)ck);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__16762;
            or__5238__auto__16762 = null;
        } else {
            ((IFn)const__4.getRawRoot()).invoke(const__3.getRawRoot(), (Object)new coordination_ext$fn__16758$fn__16759(cluster_conf, ck));
            Object object5 = ck;
            ck = null;
            object = RT.get((Object)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot()), (Object)object5);
        }
        Object object6 = kvs = object;
        kvs = null;
        Object object7 = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__5.getRawRoot()).invoke(object6, object7);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination_ext$fn__16758.invokeStatic(object2);
    }
}


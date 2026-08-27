/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class coordination$create_db_cluster
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"db-id"), Symbol.intern(null, (String)"cluster-conf")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__4 = RT.var((String)"datomic.coordination", (String)"create-cluster");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__6 = RT.var((String)"datomic.cluster-stack", (String)"kv-cache-ref");
    public static final Var const__7 = RT.var((String)"datomic.cluster-stack", (String)"cluster-with-cache");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get-fallback-msec"), 5L});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-id"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5455__auto__11680;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        Object object4 = cluster_conf;
        cluster_conf = null;
        Object cluster2 = ((IFn)const__4.getRawRoot()).invoke(object4);
        Object object5 = temp__5455__auto__11680 = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot());
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = temp__5455__auto__11680;
            temp__5455__auto__11680 = null;
            Object kv_cache2 = object6;
            Object object7 = cluster2;
            cluster2 = null;
            Object object8 = kv_cache2;
            kv_cache2 = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object7, object8, (Object)const__10);
        } else {
            object = cluster2;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$create_db_cluster.invokeStatic(object2);
    }
}


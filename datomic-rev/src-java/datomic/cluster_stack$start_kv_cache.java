/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster_stack$start_kv_cache$close__11455;
import datomic.cluster_stack$start_kv_cache$combine__11448;
import datomic.cluster_stack$start_kv_cache$wrap__11445;

public final class cluster_stack$start_kv_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"requiring-resolve!");
    public static final AFn const__1 = (AFn)Symbol.intern((String)"datomic.memcached", (String)"start-memcached-from-config");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"valcache-args");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.valcache-direct", (String)"create");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.memcached", (String)"start-local-memcached-from-config");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Keyword const__6 = RT.keyword(null, (String)"mc.repair");
    public static final Object const__7 = 5L;
    public static final Keyword const__8 = RT.keyword(null, (String)"vc.repair");
    public static final Keyword const__9 = RT.keyword(null, (String)"lmc.repair");
    public static final Var const__11 = RT.var((String)"datomic.cluster-stack", (String)"val-store-with-close");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__13 = RT.var((String)"datomic.cluster-stack", (String)"kv-cache-ref");

    public static Object invokeStatic() {
        Object stack;
        Object object;
        Object object2;
        Object valcache_args2;
        cluster_stack$start_kv_cache$wrap__11445 wrap;
        cluster_stack$start_kv_cache$wrap__11445 cluster_stack$start_kv_cache$wrap__11445 = wrap = new cluster_stack$start_kv_cache$wrap__11445();
        wrap = null;
        cluster_stack$start_kv_cache$combine__11448 combine = new cluster_stack$start_kv_cache$combine__11448((Object)cluster_stack$start_kv_cache$wrap__11445);
        Object memcached2 = ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__1)).invoke();
        Object object3 = valcache_args2 = ((IFn)const__2.getRawRoot()).invoke();
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = valcache_args2;
            valcache_args2 = null;
            object2 = ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__3)).invoke(object4);
        } else {
            object2 = null;
        }
        Object valcache2 = object2;
        Object local_memcached = ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__4)).invoke();
        cluster_stack$start_kv_cache$combine__11448 cluster_stack$start_kv_cache$combine__11448 = combine;
        combine = null;
        Object stack2 = ((IFn)const__5.getRawRoot()).invoke((Object)cluster_stack$start_kv_cache$combine__11448, null, (Object)Tuple.create((Object)Tuple.create((Object)memcached2, (Object)const__6, (Object)const__7), (Object)Tuple.create((Object)valcache2, (Object)const__8, (Object)const__7), (Object)Tuple.create((Object)local_memcached, (Object)const__9, (Object)const__7)));
        Object object5 = valcache2;
        valcache2 = null;
        Object object6 = memcached2;
        memcached2 = null;
        Object object7 = local_memcached;
        local_memcached = null;
        cluster_stack$start_kv_cache$close__11455 close2 = new cluster_stack$start_kv_cache$close__11455(object5, object6, object7);
        Object object8 = stack2;
        stack2 = null;
        Object G__11463 = object8;
        if (Util.identical((Object)G__11463, null)) {
            object = null;
        } else {
            Object object9 = G__11463;
            G__11463 = null;
            cluster_stack$start_kv_cache$close__11455 cluster_stack$start_kv_cache$close__11455 = close2;
            close2 = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object9, (Object)cluster_stack$start_kv_cache$close__11455);
        }
        Object object10 = stack = object;
        stack = null;
        return ((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), object10);
    }

    public Object invoke() {
        return cluster_stack$start_kv_cache.invokeStatic();
    }
}


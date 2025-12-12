/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_cache$start_kv_cache$combine__10073;
import datomic.kv_cache$start_kv_cache$record__10071;

public final class kv_cache$start_kv_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"resolve");
    public static final AFn const__1 = (AFn)Symbol.intern((String)"datomic.memcached", (String)"start-memcached-from-config");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"valcache-args");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.valcache-direct", (String)"create");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.memcached", (String)"start-local-memcached-from-config");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__7 = RT.var((String)"datomic.kv-cache", (String)"kv-cache-ref");

    public static Object invokeStatic() {
        Object stack;
        Object object;
        Object valcache_args2;
        kv_cache$start_kv_cache$record__10071 record;
        kv_cache$start_kv_cache$record__10071 kv_cache$start_kv_cache$record__10071 = record = new kv_cache$start_kv_cache$record__10071();
        record = null;
        kv_cache$start_kv_cache$combine__10073 combine = new kv_cache$start_kv_cache$combine__10073((Object)kv_cache$start_kv_cache$record__10071);
        Object memcached2 = ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__1)).invoke();
        Object object2 = valcache_args2 = ((IFn)const__2.getRawRoot()).invoke();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = valcache_args2;
            valcache_args2 = null;
            object = ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__3)).invoke(object3);
        } else {
            object = null;
        }
        Object valcache2 = object;
        Object local_memcached = ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__4)).invoke();
        kv_cache$start_kv_cache$combine__10073 kv_cache$start_kv_cache$combine__10073 = combine;
        combine = null;
        Object object4 = memcached2;
        memcached2 = null;
        Object object5 = valcache2;
        valcache2 = null;
        Object object6 = local_memcached;
        local_memcached = null;
        Object object7 = stack = ((IFn)const__5.getRawRoot()).invoke((Object)kv_cache$start_kv_cache$combine__10073, null, (Object)Tuple.create((Object)object4, (Object)object5, (Object)object6));
        stack = null;
        return ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), object7);
    }

    public Object invoke() {
        return kv_cache$start_kv_cache.invokeStatic();
    }
}


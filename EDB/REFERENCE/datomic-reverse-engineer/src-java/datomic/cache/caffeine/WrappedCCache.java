/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.github.benmanes.caffeine.cache.Cache
 */
package datomic.cache.caffeine;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import com.github.benmanes.caffeine.cache.Cache;
import datomic.cache.caffeine.CacheGet;
import datomic.cache.caffeine.WrappedCCache$fn__606;
import datomic.cache.impl.CacheKeys;
import datomic.cache.impl.CachePut;
import datomic.cache.impl.CacheRemove;
import datomic.cache.impl.FastCount;
import datomic.memory_size.MemorySize;
import datomic.monitor.Metrics;

public final class WrappedCCache
implements FastCount,
CachePut,
ILookup,
Metrics,
CacheRemove,
MemorySize,
CacheKeys,
IType {
    public final Object cache;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    private static Class __cached_class__6;
    private static Class __cached_class__7;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Object const__8;
    public static final Var const__9;
    public static final Keyword const__10;

    public WrappedCCache(Object object) {
        this.cache = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cache")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Cache")})));
    }

    /*
     * Unable to fully structure code
     */
    public Object metrics() {
        v0 = new Object[2];
        v0[0] = WrappedCCache.const__10;
        v1 = this.cache;
        if (Util.classOf((Object)v1) == WrappedCCache.__cached_class__7) ** GOTO lbl8
        if (!(v1 instanceof FastCount)) {
            v1 = v1;
            WrappedCCache.__cached_class__7 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = WrappedCCache.const__5.getRawRoot().invoke(v1);
        } else {
            v2 = ((FastCount)v1).fast_count();
        }
        v0[1] = v2;
        return RT.mapUniqueKeys((Object[])v0);
    }

    /*
     * Unable to fully structure code
     */
    public Object memory_size() {
        v0 = (IFn)WrappedCCache.const__7.getRawRoot();
        v1 = new WrappedCCache$fn__606(this);
        v2 = this;
        if (Util.classOf((Object)v2) == WrappedCCache.__cached_class__6) ** GOTO lbl8
        if (!(v2 instanceof CacheKeys)) {
            v2 = v2;
            WrappedCCache.__cached_class__6 = Util.classOf((Object)v2);
lbl8:
            // 2 sources

            v3 = WrappedCCache.const__9.getRawRoot().invoke((Object)v2);
        } else {
            v3 = ((CacheKeys)v2).cache_keys();
        }
        this = null;
        return v0.invoke((Object)v1, WrappedCCache.const__8, v3);
    }

    public Object cache_keys() {
        WrappedCCache this_ = null;
        return ((IFn)const__6.getRawRoot()).invoke((Object)((Cache)this_.cache).asMap());
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object fast_count() {
        Object object;
        Object object2 = this_.cache;
        if (Util.classOf((Object)object2) != __cached_class__5) {
            if (object2 instanceof FastCount) {
                object = ((FastCount)object2).fast_count();
                return object;
            }
            object2 = object2;
            __cached_class__5 = Util.classOf((Object)object2);
        }
        WrappedCCache this_ = null;
        object = const__5.getRawRoot().invoke(object2);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object clear() {
        Object object;
        Object object2 = this_.cache;
        if (Util.classOf((Object)object2) != __cached_class__4) {
            if (object2 instanceof CacheRemove) {
                object = ((CacheRemove)object2).clear();
                return object;
            }
            object2 = object2;
            __cached_class__4 = Util.classOf((Object)object2);
        }
        WrappedCCache this_ = null;
        object = const__4.getRawRoot().invoke(object2);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object remove(Object k) {
        Object object;
        Object object2 = this_.cache;
        if (Util.classOf((Object)object2) != __cached_class__3) {
            if (object2 instanceof CacheRemove) {
                Object object3 = k;
                k = null;
                object = ((CacheRemove)object2).remove(object3);
                return object;
            }
            object2 = object2;
            __cached_class__3 = Util.classOf((Object)object2);
        }
        Object object4 = k;
        k = null;
        WrappedCCache this_ = null;
        object = const__3.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object put(Object k, Object v) {
        Object object;
        Object object2 = this_.cache;
        if (Util.classOf((Object)object2) != __cached_class__2) {
            if (object2 instanceof CachePut) {
                Object object3 = k;
                k = null;
                Object object4 = v;
                v = null;
                object = ((CachePut)object2).put(object3, object4);
                return object;
            }
            object2 = object2;
            __cached_class__2 = Util.classOf((Object)object2);
        }
        Object object5 = k;
        k = null;
        Object object6 = v;
        v = null;
        WrappedCCache this_ = null;
        object = const__2.getRawRoot().invoke(object2, object5, object6);
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object valAt(Object k, Object nf) {
        v0 = this.cache;
        if (Util.classOf((Object)v0) == WrappedCCache.__cached_class__1) ** GOTO lbl6
        if (!(v0 instanceof CacheGet)) {
            v0 = v0;
            WrappedCCache.__cached_class__1 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = k;
            k = null;
            v2 = WrappedCCache.const__0.getRawRoot().invoke(v0, v1);
        } else {
            v3 = k;
            k = null;
            v2 = v = ((CacheGet)v0).cache_get(v3);
        }
        if (Util.identical((Object)v, null)) {
            v4 = nf;
            nf = null;
        } else {
            v4 = v;
            var3_3 = null;
        }
        return v4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object valAt(Object k) {
        Object object;
        Object object2 = this_.cache;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof CacheGet) {
                Object object3 = k;
                k = null;
                object = ((CacheGet)object2).cache_get(object3);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = k;
        k = null;
        WrappedCCache this_ = null;
        object = const__0.getRawRoot().invoke(object2, object4);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.cache.caffeine", (String)"cache-get");
        const__2 = RT.var((String)"datomic.cache.impl", (String)"put");
        const__3 = RT.var((String)"datomic.cache.impl", (String)"remove");
        const__4 = RT.var((String)"datomic.cache.impl", (String)"clear");
        const__5 = RT.var((String)"datomic.cache.impl", (String)"fast-count");
        const__6 = RT.var((String)"clojure.core", (String)"keys");
        const__7 = RT.var((String)"clojure.core", (String)"reduce");
        const__8 = 0L;
        const__9 = RT.var((String)"datomic.cache.impl", (String)"cache-keys");
        const__10 = RT.keyword(null, (String)"ObjectCacheCount");
    }
}


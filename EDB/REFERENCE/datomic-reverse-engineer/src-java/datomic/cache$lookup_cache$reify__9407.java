/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cache.ICachedLookup;
import datomic.cache.impl.CachePut;
import datomic.cache.impl.CacheRemove;
import datomic.cache.impl.FastCount;

public final class cache$lookup_cache$reify__9407
implements FastCount,
CachePut,
ILookup,
CacheRemove,
ICachedLookup,
IObj {
    final IPersistentMap __meta;
    Object m;
    Object f;
    Object cache;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"fast-count");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"put");
    public static final Var const__2 = RT.var((String)"datomic.cache", (String)"remove");
    public static final Var const__3 = RT.var((String)"datomic.cache", (String)"clear");
    public static final Keyword const__5 = RT.keyword(null, (String)"hit");
    public static final Keyword const__6 = RT.keyword(null, (String)"miss");

    public cache$lookup_cache$reify__9407(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.m = object;
        this.f = object2;
        this.cache = object3;
    }

    public cache$lookup_cache$reify__9407(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$lookup_cache$reify__9407(iPersistentMap, this.m, this.f, this.cache);
    }

    public Object valAtUncached(Object k, Object not_found) {
        Object object;
        Object ret = RT.get((Object)this_.cache, (Object)k);
        Object object2 = this_.f;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ret;
            ((IFn)this_.f).invoke(k, (Object)(object3 != null && object3 != Boolean.FALSE ? const__5 : const__6));
        }
        Object object4 = ret;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = ret;
            ret = null;
        } else {
            Object object5 = k;
            k = null;
            Object object6 = not_found;
            not_found = null;
            cache$lookup_cache$reify__9407 this_ = null;
            object = RT.get((Object)this_.m, (Object)object5, (Object)object6);
        }
        return object;
    }

    public Object getFromCache(Object k, Object not_found) {
        Object object;
        Object ret = RT.get((Object)this.cache, (Object)k);
        Object object2 = this.f;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = k;
            k = null;
            Object object4 = ret;
            ((IFn)this.f).invoke(object3, (Object)(object4 != null && object4 != Boolean.FALSE ? const__5 : const__6));
        }
        Object object5 = ret;
        if (object5 != null && object5 != Boolean.FALSE) {
            object = ret;
            ret = null;
        } else {
            object = not_found;
            Object var2_2 = null;
        }
        return object;
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object ret = RT.get((Object)this.cache, (Object)k);
        Object object2 = this.f;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ret;
            ((IFn)this.f).invoke(k, (Object)(object3 != null && object3 != Boolean.FALSE ? const__5 : const__6));
        }
        Object object4 = ret;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = ret;
            ret = null;
        } else {
            Object v = RT.get((Object)this.m, (Object)k, (Object)not_found);
            Object object5 = not_found;
            not_found = null;
            if (Util.equiv((Object)v, (Object)object5)) {
            } else {
                Object object6 = k;
                k = null;
                ((IFn)const__1.getRawRoot()).invoke(this.cache, object6, v);
            }
            object = v;
            v = null;
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    public Object clear() {
        cache$lookup_cache$reify__9407 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(this_.cache);
    }

    public Object remove(Object k) {
        Object object = k;
        k = null;
        cache$lookup_cache$reify__9407 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(this_.cache, object);
    }

    public Object put(Object k, Object v) {
        Object object = k;
        k = null;
        Object object2 = v;
        v = null;
        cache$lookup_cache$reify__9407 this_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(this_.cache, object, object2);
    }

    public Object fast_count() {
        cache$lookup_cache$reify__9407 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cache);
    }
}


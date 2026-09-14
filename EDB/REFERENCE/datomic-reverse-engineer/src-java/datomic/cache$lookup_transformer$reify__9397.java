/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cache.ICachedLookup;

public final class cache$lookup_transformer$reify__9397
implements ILookup,
ICachedLookup,
IObj {
    final IPersistentMap __meta;
    Object m;
    Object try_val_fn;
    Object key_fn;
    public static final Var const__2 = RT.var((String)"datomic.cache", (String)"get-from-cache");
    public static final Var const__3 = RT.var((String)"datomic.cache", (String)"get-uncached");

    public cache$lookup_transformer$reify__9397(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.m = object;
        this.try_val_fn = object2;
        this.key_fn = object3;
    }

    public cache$lookup_transformer$reify__9397(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$lookup_transformer$reify__9397(iPersistentMap, this.m, this.try_val_fn, this.key_fn);
    }

    public Object valAtUncached(Object k, Object not_found) {
        Object object;
        Object ret = ((IFn)const__3.getRawRoot()).invoke(this_.m, ((IFn)this_.key_fn).invoke(k), not_found);
        if (Util.equiv((Object)ret, (Object)not_found)) {
            object = not_found;
            not_found = null;
        } else {
            Object object2 = ret;
            ret = null;
            Object object3 = k;
            k = null;
            cache$lookup_transformer$reify__9397 this_ = null;
            object = ((IFn)this_.try_val_fn).invoke(object2, object3);
        }
        return object;
    }

    public Object getFromCache(Object k, Object not_found) {
        Object object;
        Object ret = ((IFn)const__2.getRawRoot()).invoke(this_.m, ((IFn)this_.key_fn).invoke(k), not_found);
        if (Util.equiv((Object)ret, (Object)not_found)) {
            object = not_found;
            not_found = null;
        } else {
            Object object2 = ret;
            ret = null;
            Object object3 = k;
            k = null;
            cache$lookup_transformer$reify__9397 this_ = null;
            object = ((IFn)this_.try_val_fn).invoke(object2, object3);
        }
        return object;
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object ret = RT.get((Object)this_.m, (Object)((IFn)this_.key_fn).invoke(k), (Object)not_found);
        if (Util.equiv((Object)ret, (Object)not_found)) {
            object = not_found;
            not_found = null;
        } else {
            Object object2 = ret;
            ret = null;
            Object object3 = k;
            k = null;
            cache$lookup_transformer$reify__9397 this_ = null;
            object = ((IFn)this_.try_val_fn).invoke(object2, object3);
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }
}


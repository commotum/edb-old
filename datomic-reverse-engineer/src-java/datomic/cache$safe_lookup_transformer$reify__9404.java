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
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;

public final class cache$safe_lookup_transformer$reify__9404
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object try_val_fn;
    Object key_fn;
    Object m;

    public cache$safe_lookup_transformer$reify__9404(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.try_val_fn = object;
        this.key_fn = object2;
        this.m = object3;
    }

    public cache$safe_lookup_transformer$reify__9404(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$safe_lookup_transformer$reify__9404(iPersistentMap, this.try_val_fn, this.key_fn, this.m);
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
            Object object4 = not_found;
            not_found = null;
            cache$safe_lookup_transformer$reify__9404 this_ = null;
            object = ((IFn)this_.try_val_fn).invoke(object2, object3, object4);
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }
}


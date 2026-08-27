/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;

public final class cache$double_lookup$reify__9419
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object m2;
    Object m1;

    public cache$double_lookup$reify__9419(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.m2 = object;
        this.m1 = object2;
    }

    public cache$double_lookup$reify__9419(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$double_lookup$reify__9419(iPersistentMap, this.m2, this.m1);
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object ret = RT.get((Object)this_.m1, (Object)k, (Object)not_found);
        if (Util.equiv((Object)ret, (Object)not_found)) {
            Object object2 = k;
            k = null;
            Object object3 = not_found;
            not_found = null;
            cache$double_lookup$reify__9419 this_ = null;
            object = RT.get((Object)this_.m2, (Object)object2, (Object)object3);
        } else {
            object = ret;
            Object var3_3 = null;
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }
}


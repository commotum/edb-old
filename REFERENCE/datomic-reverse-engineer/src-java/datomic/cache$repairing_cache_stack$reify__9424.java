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
import datomic.cache.impl.CachePut;

public final class cache$repairing_cache_stack$reify__9424
implements CachePut,
ILookup,
AutoCloseable,
IObj {
    final IPersistentMap __meta;
    Object on_repair;
    Object cache_1;
    Object cache_2;
    Object close_cache_1_QMARK_;
    Object close_cache_2_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"put");

    public cache$repairing_cache_stack$reify__9424(IPersistentMap iPersistentMap, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.__meta = iPersistentMap;
        this.on_repair = object;
        this.cache_1 = object2;
        this.cache_2 = object3;
        this.close_cache_1_QMARK_ = object4;
        this.close_cache_2_QMARK_ = object5;
    }

    public cache$repairing_cache_stack$reify__9424(Object object, Object object2, Object object3, Object object4, Object object5) {
        this(null, object, object2, object3, object4, object5);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$repairing_cache_stack$reify__9424(iPersistentMap, this.on_repair, this.cache_1, this.cache_2, this.close_cache_1_QMARK_, this.close_cache_2_QMARK_);
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object v = RT.get((Object)this.cache_1, (Object)k, (Object)not_found);
        if (Util.equiv((Object)v, (Object)not_found)) {
            Object v2 = RT.get((Object)this.cache_2, (Object)k, (Object)not_found);
            Object object2 = not_found;
            not_found = null;
            if (Util.equiv((Object)v2, (Object)object2)) {
            } else {
                ((IFn)const__0.getRawRoot()).invoke(this.cache_1, k, v2);
                Object object3 = k;
                k = null;
                ((IFn)this.on_repair).invoke(object3);
            }
            object = v2;
            v2 = null;
        } else {
            object = v;
            Object var3_3 = null;
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    public Object put(Object k, Object v) {
        ((IFn)const__0.getRawRoot()).invoke(this_.cache_1, k, v);
        Object object = k;
        k = null;
        Object object2 = v;
        v = null;
        cache$repairing_cache_stack$reify__9424 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cache_2, object, object2);
    }

    public void close() throws Exception {
        Object v2;
        Object object = this.close_cache_1_QMARK_;
        if (object != null && object != Boolean.FALSE) {
            ((AutoCloseable)this.cache_1).close();
        }
        Object object2 = this.close_cache_2_QMARK_;
        if (object2 != null && object2 != Boolean.FALSE) {
            ((AutoCloseable)this.cache_2).close();
            v2 = null;
        } else {
            v2 = null;
        }
    }
}


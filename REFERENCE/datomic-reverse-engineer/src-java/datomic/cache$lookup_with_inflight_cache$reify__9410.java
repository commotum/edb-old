/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cache$lookup_with_inflight_cache$reify__9410$fn__9411;
import java.util.concurrent.ConcurrentMap;

public final class cache$lookup_with_inflight_cache$reify__9410
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object in_flight;
    Object m;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__2 = RT.keyword(null, (String)"inflight-lookup-ns");

    public cache$lookup_with_inflight_cache$reify__9410(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.in_flight = object;
        this.m = object2;
    }

    public cache$lookup_with_inflight_cache$reify__9410(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$lookup_with_inflight_cache$reify__9410(iPersistentMap, this.in_flight, this.m);
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Delay temp__5455__auto__9414;
        Object object2 = not_found;
        not_found = null;
        Delay thunk = new Delay((IFn)new cache$lookup_with_inflight_cache$reify__9410$fn__9411(object2, this_.in_flight, k, this_.m));
        Object object3 = k;
        k = null;
        Delay delay = temp__5455__auto__9414 = ((ConcurrentMap)this_.in_flight).putIfAbsent(object3, thunk);
        if (delay != null && delay != Boolean.FALSE) {
            Delay delay2 = temp__5455__auto__9414;
            temp__5455__auto__9414 = null;
            Delay existing = delay2;
            long now = System.nanoTime();
            Delay delay3 = existing;
            existing = null;
            Object ret = ((IFn)const__0.getRawRoot()).invoke((Object)delay3);
            ((IFn.OLO)const__1.getRawRoot()).invokePrim((Object)const__2, Numbers.minus((long)System.nanoTime(), (long)now));
            object = ret;
            ret = null;
        } else {
            Delay delay4 = thunk;
            thunk = null;
            cache$lookup_with_inflight_cache$reify__9410 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)delay4);
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  datomic.spy.memcached.CachedData
 *  datomic.spy.memcached.transcoders.Transcoder
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.spy.memcached.CachedData;
import datomic.spy.memcached.transcoders.Transcoder;

public final class memcached$reify__9950
implements Transcoder,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.memcached", (String)"SPY_BYTEARRAY_FLAGS");
    public static final Var const__1 = RT.var((String)"datomic.memcached", (String)"SPY_MAX_SIZE");

    public memcached$reify__9950(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public memcached$reify__9950() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new memcached$reify__9950(iPersistentMap);
    }

    public int getMaxSize() {
        return ((Number)const__1.getRawRoot()).intValue();
    }

    public Object decode(CachedData cd) {
        CachedData cachedData = cd;
        cd = null;
        return cachedData.getData();
    }

    public CachedData encode(Object bs) {
        Object object = bs;
        bs = null;
        return new CachedData(RT.intCast((Object)((Number)const__0.getRawRoot())), (byte[])object, RT.intCast((Object)((Number)const__1.getRawRoot())));
    }

    public boolean asyncDecode(CachedData _) {
        return Boolean.FALSE;
    }
}


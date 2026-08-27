/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_store.KVStore;

public final class KVCluster$fn__11004$fn__11008$fn__11013
extends AFunction {
    Object item;
    Object kvs;
    Object etag;
    Object rev;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__4;
    public static final AFn const__7;
    public static final Keyword const__8;

    public KVCluster$fn__11004$fn__11008$fn__11013(Object object, Object object2, Object object3, Object object4) {
        this.item = object;
        this.kvs = object2;
        this.etag = object3;
        this.rev = object4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2;
        block3: {
            block2: {
                object2 = this_.kvs;
                if (Util.classOf((Object)object2) == __cached_class__0) break block2;
                if (object2 instanceof KVStore) break block3;
                object2 = object2;
                __cached_class__0 = Util.classOf((Object)object2);
            }
            KVCluster$fn__11004$fn__11008$fn__11013 this_ = null;
            object = const__0.getRawRoot().invoke(object2, ((IFn)const__1.getRawRoot()).invoke(this_.item, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, ((IFn)const__1.getRawRoot()).invoke(Util.identical((Object)this_.etag, null) ? PersistentArrayMap.EMPTY : RT.mapUniqueKeys((Object[])new Object[]{const__4, this_.etag}), Numbers.isZero((Object)this_.rev) ? const__7 : RT.mapUniqueKeys((Object[])new Object[]{const__8, Numbers.dec((Object)this_.rev)}))})));
            return object;
        }
        object = ((KVStore)object2).put(((IFn)const__1.getRawRoot()).invoke(this_.item, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, ((IFn)const__1.getRawRoot()).invoke(Util.identical((Object)this_.etag, null) ? PersistentArrayMap.EMPTY : RT.mapUniqueKeys((Object[])new Object[]{const__4, this_.etag}), Numbers.isZero((Object)this_.rev) ? const__7 : RT.mapUniqueKeys((Object[])new Object[]{const__8, Numbers.dec((Object)this_.rev)}))})));
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"put");
        const__1 = RT.var((String)"clojure.core", (String)"merge");
        const__2 = RT.keyword(null, (String)"ensure");
        const__4 = RT.keyword(null, (String)"tail");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"id"), null});
        const__8 = RT.keyword(null, (String)"rev");
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_store.KVStore;

public final class KVCluster$doit__10886$fn__10890$fn__10891
extends AFunction {
    Object buf;
    Object kvs;
    Object val_key;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;

    public KVCluster$doit__10886$fn__10890$fn__10891(Object object, Object object2, Object object3) {
        this.buf = object;
        this.kvs = object2;
        this.val_key = object3;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        v0 = this.kvs;
        if (Util.classOf((Object)v0) == KVCluster$doit__10886$fn__10890$fn__10891.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof KVStore)) {
            v0 = v0;
            KVCluster$doit__10886$fn__10890$fn__10891.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = KVCluster$doit__10886$fn__10890$fn__10891.const__0.getRawRoot().invoke(v0, (Object)RT.mapUniqueKeys((Object[])new Object[]{KVCluster$doit__10886$fn__10890$fn__10891.const__1, this.val_key, KVCluster$doit__10886$fn__10890$fn__10891.const__2, this.buf}));
        } else {
            v1 = ((KVStore)v0).put(RT.mapUniqueKeys((Object[])new Object[]{KVCluster$doit__10886$fn__10890$fn__10891.const__1, this.val_key, KVCluster$doit__10886$fn__10890$fn__10891.const__2, this.buf}));
        }
        return v1 != null && v1 != Boolean.FALSE ? KVCluster$doit__10886$fn__10890$fn__10891.const__3 : null;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"put");
        const__1 = RT.keyword(null, (String)"id");
        const__2 = RT.keyword(null, (String)"v");
        const__3 = RT.keyword(null, (String)"created");
    }
}


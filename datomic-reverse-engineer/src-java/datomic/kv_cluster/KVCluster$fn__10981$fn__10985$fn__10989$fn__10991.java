/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_store.KVStore;

public final class KVCluster$fn__10981$fn__10985$fn__10989$fn__10991
extends AFunction {
    Object kvs;
    Object tail;
    private static Class __cached_class__0;
    public static final Var const__0;

    public KVCluster$fn__10981$fn__10985$fn__10989$fn__10991(Object object, Object object2) {
        this.kvs = object;
        this.tail = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.kvs;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof KVStore) {
                object = ((KVStore)object2).get(this_.tail, Boolean.FALSE);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        KVCluster$fn__10981$fn__10985$fn__10989$fn__10991 this_ = null;
        object = const__0.getRawRoot().invoke(object2, this_.tail, (Object)Boolean.FALSE);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"get");
    }
}


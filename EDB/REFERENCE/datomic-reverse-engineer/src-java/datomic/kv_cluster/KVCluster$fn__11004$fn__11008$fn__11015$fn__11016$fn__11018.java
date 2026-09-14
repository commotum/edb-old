/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_store.KVStore;

public final class KVCluster$fn__11004$fn__11008$fn__11015$fn__11016$fn__11018
extends AFunction {
    Object ks;
    Object kvs;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public KVCluster$fn__11004$fn__11008$fn__11015$fn__11016$fn__11018(Object object, Object object2) {
        this.ks = object;
        this.kvs = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.kvs;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof KVStore) {
                object = ((KVStore)object2).get(((IFn)const__1.getRawRoot()).invoke(this_.ks), Boolean.FALSE);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        KVCluster$fn__11004$fn__11008$fn__11015$fn__11016$fn__11018 this_ = null;
        object = const__0.getRawRoot().invoke(object2, ((IFn)const__1.getRawRoot()).invoke(this_.ks), (Object)Boolean.FALSE);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"get");
        const__1 = RT.var((String)"clojure.core", (String)"peek");
    }
}


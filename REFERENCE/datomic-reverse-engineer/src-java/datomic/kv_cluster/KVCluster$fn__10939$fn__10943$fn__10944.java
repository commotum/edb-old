/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_store.KVStore;

public final class KVCluster$fn__10939$fn__10943$fn__10944
extends AFunction {
    Object kvs;
    Object ref_key;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final AFn const__4;

    public KVCluster$fn__10939$fn__10943$fn__10944(Object object, Object object2) {
        this.kvs = object;
        this.ref_key = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        v0 = this.kvs;
        if (Util.classOf((Object)v0) == KVCluster$fn__10939$fn__10943$fn__10944.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof KVStore)) {
            v0 = v0;
            KVCluster$fn__10939$fn__10943$fn__10944.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = KVCluster$fn__10939$fn__10943$fn__10944.const__0.getRawRoot().invoke(v0, this.ref_key, (Object)Boolean.TRUE);
        } else {
            v1 = ((KVStore)v0).get(this.ref_key, Boolean.TRUE);
        }
        v2 = temp__5457__auto__10946 = v1;
        if (v2 != null && v2 != Boolean.FALSE) {
            v3 = temp__5457__auto__10946;
            temp__5457__auto__10946 = null;
            v4 = ret = v3;
            ret = null;
            this = null;
            v5 = ((IFn)KVCluster$fn__10939$fn__10943$fn__10944.const__1.getRawRoot()).invoke(v4, (Object)KVCluster$fn__10939$fn__10943$fn__10944.const__4);
        } else {
            v5 = null;
        }
        return v5;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"get");
        const__1 = RT.var((String)"clojure.core", (String)"select-keys");
        const__4 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"key"), (Object)RT.keyword(null, (String)"rev"));
    }
}


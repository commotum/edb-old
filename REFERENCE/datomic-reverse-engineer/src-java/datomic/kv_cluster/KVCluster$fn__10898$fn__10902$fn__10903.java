/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_store.KVStore;
import java.nio.Buffer;

public final class KVCluster$fn__10898$fn__10902$fn__10903
extends AFunction {
    Object protocol_nsec_k;
    Object kvs;
    Object val_key;
    long start;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;

    public KVCluster$fn__10898$fn__10902$fn__10903(Object object, Object object2, Object object3, long l) {
        this.protocol_nsec_k = object;
        this.kvs = object2;
        this.val_key = object3;
        this.start = l;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        v0 = this.kvs;
        if (Util.classOf((Object)v0) == KVCluster$fn__10898$fn__10902$fn__10903.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof KVStore)) {
            v0 = v0;
            KVCluster$fn__10898$fn__10902$fn__10903.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = KVCluster$fn__10898$fn__10902$fn__10903.const__0.getRawRoot().invoke(v0, this.val_key, (Object)Boolean.FALSE);
        } else {
            v1 = ((KVStore)v0).get(this.val_key, Boolean.FALSE);
        }
        v2 = temp__5457__auto__10906 = v1;
        if (v2 != null && v2 != Boolean.FALSE) {
            v3 = temp__5457__auto__10906;
            temp__5457__auto__10906 = null;
            map__10904 = v3;
            v4 = ((IFn)KVCluster$fn__10898$fn__10902$fn__10903.const__1.getRawRoot()).invoke(map__10904);
            if (v4 != null && v4 != Boolean.FALSE) {
                v5 = map__10904;
                map__10904 = null;
                v6 = PersistentHashMap.create((ISeq)((ISeq)((IFn)KVCluster$fn__10898$fn__10902$fn__10903.const__2.getRawRoot()).invoke(v5)));
            } else {
                v6 = map__10904;
                map__10904 = null;
            }
            v7 = map__10904 = v6;
            map__10904 = null;
            buf = RT.get((Object)v7, (Object)KVCluster$fn__10898$fn__10902$fn__10903.const__4);
            ((IFn.OLO)KVCluster$fn__10898$fn__10902$fn__10903.const__5.getRawRoot()).invokePrim(this.protocol_nsec_k, Numbers.minus((long)System.nanoTime(), (long)this.start));
            ((IFn)KVCluster$fn__10898$fn__10902$fn__10903.const__7.getRawRoot()).invoke((Object)KVCluster$fn__10898$fn__10902$fn__10903.const__8, (Object)((Buffer)buf).remaining());
            ((IFn)KVCluster$fn__10898$fn__10902$fn__10903.const__9.getRawRoot()).invoke(KVCluster$fn__10898$fn__10902$fn__10903.const__10.getRawRoot(), KVCluster$fn__10898$fn__10902$fn__10903.const__11.getRawRoot());
            v8 = new Object[2];
            v8[0] = KVCluster$fn__10898$fn__10902$fn__10903.const__12;
            v9 = buf;
            buf = null;
            v8[1] = v9;
            v10 = RT.mapUniqueKeys((Object[])v8);
        } else {
            v10 = null;
        }
        return v10;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"get");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.keyword(null, (String)"v");
        const__5 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__7 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__8 = RT.keyword(null, (String)"StorageGetBytes");
        const__9 = RT.var((String)"clojure.core", (String)"swap!");
        const__10 = RT.var((String)"datomic.kv-cluster", (String)"val-gets-ref");
        const__11 = RT.var((String)"clojure.core", (String)"inc");
        const__12 = RT.keyword(null, (String)"buf");
    }
}


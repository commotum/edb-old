/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.kv_cluster.KVCluster$doit__10886$fn__10890$fn__10891;

public final class KVCluster$doit__10886$fn__10890
extends AFunction {
    Object buf;
    Object kvs;
    Object backoff;
    Object retrying_write;
    Object val_key;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"threw");

    public KVCluster$doit__10886$fn__10890(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.buf = object;
        this.kvs = object2;
        this.backoff = object3;
        this.retrying_write = object4;
        this.val_key = object5;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.backoff = null;
            this.buf = null;
            this.val_key = null;
            objectArray[1] = ((IFn)this.retrying_write).invoke(this.backoff, (Object)new KVCluster$doit__10886$fn__10890$fn__10891(this.buf, this.kvs, this.val_key));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}


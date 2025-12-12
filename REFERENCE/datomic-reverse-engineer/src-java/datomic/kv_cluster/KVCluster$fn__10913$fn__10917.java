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
import datomic.kv_cluster.KVCluster$fn__10913$fn__10917$fn__10918;

public final class KVCluster$fn__10913$fn__10917
extends AFunction {
    Object kvs;
    Object key;
    Object retrying_delete;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"threw");

    public KVCluster$fn__10913$fn__10917(Object object, Object object2, Object object3) {
        this.kvs = object;
        this.key = object2;
        this.retrying_delete = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.key = null;
            objectArray[1] = ((IFn)this.retrying_delete).invoke((Object)new KVCluster$fn__10913$fn__10917$fn__10918(this.kvs, this.key));
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


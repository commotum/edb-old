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
import datomic.kv_cluster.KVCluster$fn__10939$fn__10943$fn__10944;

public final class KVCluster$fn__10939$fn__10943
extends AFunction {
    Object kvs;
    Object retrying_read;
    Object ref_key;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"linear");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public KVCluster$fn__10939$fn__10943(Object object, Object object2, Object object3) {
        this.kvs = object;
        this.retrying_read = object2;
        this.ref_key = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.ref_key = null;
            objectArray[1] = ((IFn)this.retrying_read).invoke((Object)const__1, (Object)new KVCluster$fn__10939$fn__10943$fn__10944(this.kvs, this.ref_key));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__2;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}


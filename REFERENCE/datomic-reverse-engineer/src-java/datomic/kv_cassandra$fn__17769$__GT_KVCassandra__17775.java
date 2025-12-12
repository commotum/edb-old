/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_cassandra.KVCassandra;

public final class kv_cassandra$fn__17769$__GT_KVCassandra__17775
extends AFunction {
    public Object invoke(Object cluster2, Object session, Object table) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = session;
        session = null;
        Object object3 = table;
        table = null;
        return new KVCassandra(object, object2, object3);
    }
}


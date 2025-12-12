/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_cassandra3.KVCassandra3;

public final class kv_cassandra3$fn__23645$__GT_KVCassandra3__23653
extends AFunction {
    public Object invoke(Object session, Object table) {
        Object object = session;
        session = null;
        Object object2 = table;
        table = null;
        return new KVCassandra3(object, object2);
    }
}


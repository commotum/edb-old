/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.kv_cassandra3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class KVCassandra3$fn__23647
extends AFunction {
    Object session;
    Object v_map;
    Object table;
    public static final Var const__0 = RT.var((String)"datomic.cassandra-values-v4", (String)"put-value");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public KVCassandra3$fn__23647(Object object, Object object2, Object object3) {
        this.session = object;
        this.v_map = object2;
        this.table = object3;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.session, this.table, this.v_map);
        }
        finally {
            ((IFn)const__1.getRawRoot()).invoke();
        }
        return object;
    }
}


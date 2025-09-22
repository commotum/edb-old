/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295$fn__10296
extends AFunction {
    Object session;
    Object val_map;
    Object table;
    public static final Var const__0 = RT.var((String)"datomic.cassandra-v4", (String)"cql-insert");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values-v4", (String)"cql-keys");

    public cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295$fn__10296(Object object, Object object2, Object object3) {
        this.session = object;
        this.val_map = object2;
        this.table = object3;
    }

    public Object invoke() {
        cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295$fn__10296 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.session, this_.table, const__1.getRawRoot(), this_.val_map, (Object)Boolean.FALSE);
    }
}


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

public final class cassandra_values$put_value$fn__20532$fn__20533$fn__20537$fn__20538
extends AFunction {
    Object table;
    Object session;
    Object val_map;
    public static final Var const__0 = RT.var((String)"datomic.cassandra", (String)"cql-insert");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values", (String)"cql-keys");

    public cassandra_values$put_value$fn__20532$fn__20533$fn__20537$fn__20538(Object object, Object object2, Object object3) {
        this.table = object;
        this.session = object2;
        this.val_map = object3;
    }

    public Object invoke() {
        cassandra_values$put_value$fn__20532$fn__20533$fn__20537$fn__20538 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.session, this_.table, const__1.getRawRoot(), this_.val_map, (Object)Boolean.FALSE);
    }
}


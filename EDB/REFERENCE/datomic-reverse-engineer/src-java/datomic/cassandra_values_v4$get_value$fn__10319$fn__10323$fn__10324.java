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

public final class cassandra_values_v4$get_value$fn__10319$fn__10323$fn__10324
extends AFunction {
    Object id;
    Object session;
    Object p1__10312_SHARP_;
    Object table;
    public static final Var const__0 = RT.var((String)"datomic.cassandra-v4", (String)"cql-select");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values-v4", (String)"chunk-key");
    public static final Var const__2 = RT.var((String)"datomic.cassandra-values-v4", (String)"cql-keys");

    public cassandra_values_v4$get_value$fn__10319$fn__10323$fn__10324(Object object, Object object2, Object object3, Object object4) {
        this.id = object;
        this.session = object2;
        this.p1__10312_SHARP_ = object3;
        this.table = object4;
    }

    public Object invoke() {
        cassandra_values_v4$get_value$fn__10319$fn__10323$fn__10324 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.session, this_.table, ((IFn)const__1.getRawRoot()).invoke(this_.id, this_.p1__10312_SHARP_), const__2.getRawRoot(), (Object)Boolean.FALSE);
    }
}


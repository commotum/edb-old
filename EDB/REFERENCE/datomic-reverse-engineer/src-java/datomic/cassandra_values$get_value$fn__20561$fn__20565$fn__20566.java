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

public final class cassandra_values$get_value$fn__20561$fn__20565$fn__20566
extends AFunction {
    Object session;
    Object id;
    Object table;
    Object p1__20554_SHARP_;
    public static final Var const__0 = RT.var((String)"datomic.cassandra", (String)"cql-select");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values", (String)"chunk-key");
    public static final Var const__2 = RT.var((String)"datomic.cassandra-values", (String)"cql-keys");

    public cassandra_values$get_value$fn__20561$fn__20565$fn__20566(Object object, Object object2, Object object3, Object object4) {
        this.session = object;
        this.id = object2;
        this.table = object3;
        this.p1__20554_SHARP_ = object4;
    }

    public Object invoke() {
        cassandra_values$get_value$fn__20561$fn__20565$fn__20566 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.session, this_.table, ((IFn)const__1.getRawRoot()).invoke(this_.id, this_.p1__20554_SHARP_), const__2.getRawRoot(), (Object)Boolean.FALSE);
    }
}


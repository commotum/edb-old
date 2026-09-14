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

public final class ddb_values$get_value$fn__20431$fn__20435$fn__20436
extends AFunction {
    Object ddb_client;
    Object table;
    Object p1__20420_SHARP_;
    Object id;
    public static final Var const__0 = RT.var((String)"datomic.ddb-values", (String)"get-deitem");
    public static final Var const__1 = RT.var((String)"datomic.ddb-values", (String)"chunk-key");

    public ddb_values$get_value$fn__20431$fn__20435$fn__20436(Object object, Object object2, Object object3, Object object4) {
        this.ddb_client = object;
        this.table = object2;
        this.p1__20420_SHARP_ = object3;
        this.id = object4;
    }

    public Object invoke() {
        ddb_values$get_value$fn__20431$fn__20435$fn__20436 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.ddb_client, this_.table, ((IFn)const__1.getRawRoot()).invoke(this_.id, this_.p1__20420_SHARP_));
    }
}


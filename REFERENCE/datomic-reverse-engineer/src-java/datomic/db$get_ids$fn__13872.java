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

public final class db$get_ids$fn__13872
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"default-partition");

    public db$get_ids$fn__13872(Object object) {
        this.db = object;
    }

    public Object invoke() {
        this_.db = null;
        db$get_ids$fn__13872 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db);
    }
}


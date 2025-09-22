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

public final class db$run_hooks$fn__13583
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"key-hook");

    public Object invoke(Object db2, Object datom) {
        Object object = db2;
        db2 = null;
        Object object2 = datom;
        datom = null;
        db$run_hooks$fn__13583 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(null, object, object2, (Object)Boolean.FALSE);
    }
}


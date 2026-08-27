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
import datomic.db$with_tx$inject_all__14099$fn__14100;

public final class db$with_tx$inject_all__14099
extends AFunction {
    Object local_tempids;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public db$with_tx$inject_all__14099(Object object) {
        this.local_tempids = object;
    }

    public Object invoke(Object tx, Object procargs) {
        Object object = tx;
        tx = null;
        Object object2 = procargs;
        procargs = null;
        db$with_tx$inject_all__14099 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new db$with_tx$inject_all__14099$fn__14100(this_.local_tempids), object, object2);
    }
}


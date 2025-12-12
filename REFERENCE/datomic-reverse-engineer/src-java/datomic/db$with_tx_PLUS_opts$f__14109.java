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

public final class db$with_tx_PLUS_opts$f__14109
extends AFunction {
    Object db;
    Object txdata;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"with-tx");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"get-prefetch-dispatcher");

    public db$with_tx_PLUS_opts$f__14109(Object object, Object object2) {
        this.db = object;
        this.txdata = object2;
    }

    public Object invoke() {
        db$with_tx_PLUS_opts$f__14109 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, ((IFn)const__1.getRawRoot()).invoke(), this_.txdata);
    }
}


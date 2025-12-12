/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class LocalConnection$fn__21602
extends AFunction {
    Object db;
    Object txdata;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"with-tx+opts");

    public LocalConnection$fn__21602(Object object, Object object2) {
        this.db = object;
        this.txdata = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.db, this.txdata, null);
        }
        catch (Throwable e2) {
            Object e2 = null;
            object = e2;
        }
        return object;
    }
}


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

public final class tools$db_diagnostics$fn__21802
extends AFunction {
    Object conn;
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"db");

    public tools$db_diagnostics$fn__21802(Object object) {
        this.conn = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.conn);
        }
        catch (Throwable t) {
            object = null;
        }
        return object;
    }
}


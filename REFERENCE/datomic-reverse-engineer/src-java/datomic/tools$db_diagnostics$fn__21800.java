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

public final class tools$db_diagnostics$fn__21800
extends AFunction {
    Object uri;
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"connect");

    public tools$db_diagnostics$fn__21800(Object object) {
        this.uri = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.uri = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.uri);
        }
        catch (Throwable t) {
            object = null;
        }
        return object;
    }
}


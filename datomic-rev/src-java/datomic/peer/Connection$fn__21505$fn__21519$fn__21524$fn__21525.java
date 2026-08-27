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

public final class Connection$fn__21505$fn__21519$fn__21524$fn__21525
extends AFunction {
    Object shutdown;
    Object updater;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public Connection$fn__21505$fn__21519$fn__21524$fn__21525(Object object, Object object2) {
        this.shutdown = object;
        this.updater = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.shutdown = null;
            this.updater = null;
            object = ((IFn)this.shutdown).invoke(this.updater);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}


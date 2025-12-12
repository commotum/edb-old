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

public final class peer$await_tx_result$fn__21390
extends AFunction {
    Object prom;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property");

    public peer$await_tx_result$fn__21390(Object object) {
        this.prom = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.prom = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.prom, ((IFn)const__1.getRawRoot()).invoke((Object)"datomic.txTimeoutMsec"), this.prom);
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = t2;
        }
        return object;
    }
}


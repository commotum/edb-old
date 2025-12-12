/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class db$for_side_effects$fn__13943
extends AFunction {
    Object proc;

    public db$for_side_effects$fn__13943(Object object) {
        this.proc = object;
    }

    public Object invoke(Object x) {
        ((IFn)this.proc).invoke(x);
        Object var1_1 = null;
        return x;
    }
}


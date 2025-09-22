/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class index_checks$reporter$fn__21902
extends AFunction {
    Object f;

    public index_checks$reporter$fn__21902(Object object) {
        this.f = object;
    }

    public Object invoke(Object x) {
        ((IFn)this.f).invoke(x);
        Object var1_1 = null;
        return x;
    }
}


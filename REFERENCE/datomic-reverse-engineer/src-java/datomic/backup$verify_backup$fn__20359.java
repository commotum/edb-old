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

public final class backup$verify_backup$fn__20359
extends AFunction {
    Object progress;

    public backup$verify_backup$fn__20359(Object object) {
        this.progress = object;
    }

    public Object invoke(Object idx, Object arg2) {
        Object object = idx;
        idx = null;
        ((IFn)this.progress).invoke(object);
        Object var2_2 = null;
        return arg2;
    }
}


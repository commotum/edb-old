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

public final class common$retry_fn$fn__9168
extends AFunction {
    Object f;

    public common$retry_fn$fn__9168(Object object) {
        this.f = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)this.f).invoke();
        }
        catch (Throwable e2) {
            Object e2 = null;
            object = e2;
        }
        return object;
    }
}


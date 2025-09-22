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

public final class cleanup$run_queue_loop$fn__20746
extends AFunction {
    Object error_handler;
    Object f;

    public cleanup$run_queue_loop$fn__20746(Object object, Object object2) {
        this.error_handler = object;
        this.f = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)this.f).invoke();
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = ((IFn)this.error_handler).invoke((Object)t2);
        }
        return object;
    }
}


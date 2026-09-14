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
import java.util.concurrent.atomic.AtomicBoolean;

public final class db$get_prefetch_dispatcher$reify__14046$fn__14047
extends AFunction {
    Object done_flag;
    Object f;

    public db$get_prefetch_dispatcher$reify__14046$fn__14047(Object object, Object object2) {
        this.done_flag = object;
        this.f = object2;
    }

    public Object invoke() {
        Object object;
        if (((AtomicBoolean)this_.done_flag).get()) {
            object = null;
        } else {
            db$get_prefetch_dispatcher$reify__14046$fn__14047 this_ = null;
            object = ((IFn)this_.f).invoke();
        }
        return object;
    }
}


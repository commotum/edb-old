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
import datomic.db$safe_compile_function$try_compile__13238$fn__13239;

public final class db$safe_compile_function$try_compile__13238
extends AFunction {
    public Object invoke(Object p1__13237_SHARP_) {
        Object object;
        try {
            Object object2 = p1__13237_SHARP_;
            p1__13237_SHARP_ = null;
            object = ((IFn)object2).invoke();
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = new db$safe_compile_function$try_compile__13238$fn__13239(t2);
        }
        return object;
    }
}


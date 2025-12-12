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

public final class common$pooled_mapv$fn__9238$fn__9239
extends AFunction {
    Object f;
    Object p1__9237_SHARP_;

    public common$pooled_mapv$fn__9238$fn__9239(Object object, Object object2) {
        this.f = object;
        this.p1__9237_SHARP_ = object2;
    }

    public Object invoke() {
        common$pooled_mapv$fn__9238$fn__9239 this_ = null;
        return ((IFn)this_.f).invoke(this_.p1__9237_SHARP_);
    }
}


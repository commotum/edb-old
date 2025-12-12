/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class thread$pmap_n$fn__21021$fn__21022
extends AFunction {
    Object p1__21019_SHARP_;
    Object f;

    public thread$pmap_n$fn__21021$fn__21022(Object object, Object object2) {
        this.p1__21019_SHARP_ = object;
        this.f = object2;
    }

    public Object invoke() {
        this_.f = null;
        this_.p1__21019_SHARP_ = null;
        thread$pmap_n$fn__21021$fn__21022 this_ = null;
        return ((IFn)this_.f).invoke(this_.p1__21019_SHARP_);
    }
}


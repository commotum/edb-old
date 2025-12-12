/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Util
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Util;

public final class lazy$fully_partition_by$fpb__19419$fn__19420$fn__19421
extends AFunction {
    Object f;
    Object fv;

    public lazy$fully_partition_by$fpb__19419$fn__19420$fn__19421(Object object, Object object2) {
        this.f = object;
        this.fv = object2;
    }

    public Object invoke(Object p1__19418_SHARP_) {
        Object object = p1__19418_SHARP_;
        p1__19418_SHARP_ = null;
        lazy$fully_partition_by$fpb__19419$fn__19420$fn__19421 this_ = null;
        return Util.equiv((Object)this_.fv, (Object)((IFn)this_.f).invoke(object)) ? Boolean.TRUE : Boolean.FALSE;
    }
}


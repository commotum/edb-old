/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Util;

public final class index$fully_partition_by$fpb__15365$fn__15366$fn__15367
extends AFunction {
    Object fv;
    Object f;

    public index$fully_partition_by$fpb__15365$fn__15366$fn__15367(Object object, Object object2) {
        this.fv = object;
        this.f = object2;
    }

    public Object invoke(Object p1__15364_SHARP_) {
        Object object = p1__15364_SHARP_;
        p1__15364_SHARP_ = null;
        index$fully_partition_by$fpb__15365$fn__15366$fn__15367 this_ = null;
        return Util.equiv((Object)this_.fv, (Object)((IFn)this_.f).invoke(object)) ? Boolean.TRUE : Boolean.FALSE;
    }
}


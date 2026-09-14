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

public final class datalog$ranges$fn__18866$fn__18870
extends AFunction {
    Object c;
    Object cmp;

    public datalog$ranges$fn__18866$fn__18870(Object object, Object object2) {
        this.c = object;
        this.cmp = object2;
    }

    public Object invoke(Object p1__18862_SHARP_) {
        Object object = p1__18862_SHARP_;
        p1__18862_SHARP_ = null;
        datalog$ranges$fn__18866$fn__18870 this_ = null;
        return ((IFn)this_.cmp).invoke(object, this_.c);
    }
}


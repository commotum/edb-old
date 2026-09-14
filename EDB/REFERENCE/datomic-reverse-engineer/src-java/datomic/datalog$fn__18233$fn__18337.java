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

public final class datalog$fn__18233$fn__18337
extends AFunction {
    Object join;
    Object ret;

    public datalog$fn__18233$fn__18337(Object object, Object object2) {
        this.join = object;
        this.ret = object2;
    }

    public Object invoke(Object p1__18230_SHARP_) {
        Object object = p1__18230_SHARP_;
        p1__18230_SHARP_ = null;
        datalog$fn__18233$fn__18337 this_ = null;
        return ((IFn)this_.join).invoke(object, this_.ret);
    }
}


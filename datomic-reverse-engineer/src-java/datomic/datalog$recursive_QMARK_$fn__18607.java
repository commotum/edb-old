/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$recursive_QMARK_$fn__18607
extends AFunction {
    Object prog;
    Object seen;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"recursive?");

    public datalog$recursive_QMARK_$fn__18607(Object object, Object object2) {
        this.prog = object;
        this.seen = object2;
    }

    public Object invoke(Object p1__18606_SHARP_) {
        Object object = p1__18606_SHARP_;
        p1__18606_SHARP_ = null;
        datalog$recursive_QMARK_$fn__18607 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.prog, object, this_.seen);
    }
}


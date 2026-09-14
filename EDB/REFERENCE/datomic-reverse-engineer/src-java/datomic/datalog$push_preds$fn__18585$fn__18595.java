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

public final class datalog$push_preds$fn__18585$fn__18595
extends AFunction {
    Object ctor;
    Object outbinds;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");

    public datalog$push_preds$fn__18585$fn__18595(Object object, Object object2) {
        this.ctor = object;
        this.outbinds = object2;
    }

    public Object invoke(Object p1__18552_SHARP_) {
        Object object = p1__18552_SHARP_;
        p1__18552_SHARP_ = null;
        datalog$push_preds$fn__18585$fn__18595 this_ = null;
        return ((IFn)this_.ctor).invoke(this_.outbinds, ((IFn)const__0.getRawRoot()).invoke(object));
    }
}


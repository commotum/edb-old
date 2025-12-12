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

public final class datalog$push_preds$ctor__18558$fn__18563
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"variable-or-blank?");

    public Object invoke(Object p1__18551_SHARP_) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(p1__18551_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = null;
        } else {
            object = p1__18551_SHARP_;
            Object var1_1 = null;
        }
        return object;
    }
}


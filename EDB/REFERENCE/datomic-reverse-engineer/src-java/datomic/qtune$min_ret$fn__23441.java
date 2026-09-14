/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class qtune$min_ret$fn__23441
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"second");

    public Object invoke(Object p1__23437_SHARP_) {
        Object object;
        Object or__5238__auto__23443;
        Object object2 = p1__23437_SHARP_;
        p1__23437_SHARP_ = null;
        Object object3 = or__5238__auto__23443 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__23443;
            or__5238__auto__23443 = null;
        } else {
            object = Numbers.num((long)Long.MAX_VALUE);
        }
        return object;
    }
}


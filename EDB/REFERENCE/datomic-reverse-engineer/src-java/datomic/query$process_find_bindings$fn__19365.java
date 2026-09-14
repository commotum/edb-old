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
import java.util.List;

public final class query$process_find_bindings$fn__19365
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"variable?");

    public Object invoke(Object p1__19362_SHARP_) {
        Object object;
        Object or__5238__auto__19367;
        Object object2 = or__5238__auto__19367 = ((IFn)const__0.getRawRoot()).invoke(p1__19362_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__19367;
            or__5238__auto__19367 = null;
        } else {
            Object object3 = p1__19362_SHARP_;
            p1__19362_SHARP_ = null;
            object = object3 instanceof List ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }
}


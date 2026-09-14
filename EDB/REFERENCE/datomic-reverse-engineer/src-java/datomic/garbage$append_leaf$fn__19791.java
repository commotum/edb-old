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

public final class garbage$append_leaf$fn__19791
extends AFunction {
    Object root_entry;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");

    public garbage$append_leaf$fn__19791(Object object) {
        this.root_entry = object;
    }

    public Object invoke(Object p1__19782_SHARP_) {
        Object object = p1__19782_SHARP_;
        Object object2 = p1__19782_SHARP_;
        p1__19782_SHARP_ = null;
        garbage$append_leaf$fn__19791 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Numbers.num((long)Numbers.dec((long)RT.count((Object)object2))), this_.root_entry);
    }
}


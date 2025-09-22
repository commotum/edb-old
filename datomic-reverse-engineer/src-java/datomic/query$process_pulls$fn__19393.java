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

public final class query$process_pulls$fn__19393
extends AFunction {
    Object pull_QMARK_;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"variable?");

    public query$process_pulls$fn__19393(Object object) {
        this.pull_QMARK_ = object;
    }

    public Object invoke(Object p1__19381_SHARP_) {
        Object object;
        Object object2 = ((IFn)this_.pull_QMARK_).invoke(p1__19381_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__19381_SHARP_;
            p1__19381_SHARP_ = null;
            query$process_pulls$fn__19393 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object3));
        } else {
            object = p1__19381_SHARP_;
            Object var1_1 = null;
        }
        return object;
    }
}


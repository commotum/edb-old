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

public final class config$in_range$fn__768
extends AFunction {
    Object lo;
    Object hi;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"<=");

    public config$in_range$fn__768(Object object, Object object2) {
        this.lo = object;
        this.hi = object2;
    }

    public Object invoke(Object n) {
        Object object;
        Object and__5236__auto__770;
        Object object2 = and__5236__auto__770 = ((IFn)const__0.getRawRoot()).invoke(n);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = n;
            n = null;
            config$in_range$fn__768 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(this_.lo, object3, this_.hi);
        } else {
            object = and__5236__auto__770;
            Object var2_2 = null;
        }
        return object;
    }
}


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

public final class config$at_least$fn__764
extends AFunction {
    Object minval;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");

    public config$at_least$fn__764(Object object) {
        this.minval = object;
    }

    public Object invoke(Object n) {
        Object object;
        Object and__5236__auto__766;
        Object object2 = and__5236__auto__766 = ((IFn)const__0.getRawRoot()).invoke(n);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = n;
            n = null;
            config$at_least$fn__764 this_ = null;
            object = Numbers.lte((Object)this_.minval, (Object)object3) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__766;
            Object var2_2 = null;
        }
        return object;
    }
}


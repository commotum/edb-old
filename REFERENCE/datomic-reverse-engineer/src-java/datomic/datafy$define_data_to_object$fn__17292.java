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
import java.lang.reflect.Method;

public final class datafy$define_data_to_object$fn__17292
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"setter-name->keyword");

    public Object invoke(Object p1__17291_SHARP_) {
        Object object = p1__17291_SHARP_;
        p1__17291_SHARP_ = null;
        datafy$define_data_to_object$fn__17292 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)((Method)object).getName());
    }
}


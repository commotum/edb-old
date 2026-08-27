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

public final class function$normalize$fn__11982
extends AFunction {
    Object vectorize;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");

    public function$normalize$fn__11982(Object object) {
        this.vectorize = object;
    }

    public Object invoke(Object p1__11976_SHARP_) {
        Object object = p1__11976_SHARP_;
        p1__11976_SHARP_ = null;
        function$normalize$fn__11982 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.vectorize, object);
    }
}


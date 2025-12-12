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

public final class excise$create_as_pred$reify__14869$fn__14870
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__1 = RT.var((String)"datomic.excise", (String)"ep-datoms");

    public Object invoke(Object xpreds) {
        Object object = xpreds;
        xpreds = null;
        excise$create_as_pred$reify__14869$fn__14870 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object);
    }
}


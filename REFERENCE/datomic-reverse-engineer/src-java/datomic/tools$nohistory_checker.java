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
import datomic.tools$nohistory_checker$fn__21876;

public final class tools$nohistory_checker
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"ever-nohistory-attrs");

    public static Object invokeStatic(Object db2) {
        Object attrs;
        Object object = db2;
        db2 = null;
        Object object2 = attrs = ((IFn)const__0.getRawRoot()).invoke(object);
        attrs = null;
        return new tools$nohistory_checker$fn__21876(object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$nohistory_checker.invokeStatic(object2);
    }
}


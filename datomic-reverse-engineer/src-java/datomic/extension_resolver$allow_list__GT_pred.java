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
import datomic.extension_resolver$allow_list__GT_pred$fn__14314;

public final class extension_resolver$allow_list__GT_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extension-resolver", (String)"ensure-allow-list!");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"explicit-pred");
    public static final Var const__2 = RT.var((String)"datomic.extension-resolver", (String)"wildcard-pred");

    public static Object invokeStatic(Object allow) {
        Object wp;
        ((IFn)const__0.getRawRoot()).invoke(allow);
        Object ep = ((IFn)const__1.getRawRoot()).invoke(allow);
        Object object = allow;
        allow = null;
        Object object2 = wp = ((IFn)const__2.getRawRoot()).invoke(object);
        wp = null;
        Object object3 = ep;
        ep = null;
        return new extension_resolver$allow_list__GT_pred$fn__14314(object2, object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$allow_list__GT_pred.invokeStatic(object2);
    }
}


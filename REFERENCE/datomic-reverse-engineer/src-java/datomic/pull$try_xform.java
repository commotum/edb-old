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
import datomic.pull$try_xform$fn__18947;

public final class pull$try_xform
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extension-resolver", (String)"resolve-xform!");

    public static Object invokeStatic(Object xform) {
        Object f;
        Object object = f = ((IFn)const__0.getRawRoot()).invoke(xform);
        f = null;
        Object object2 = xform;
        xform = null;
        return new pull$try_xform$fn__18947(object, object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$try_xform.invokeStatic(object2);
    }
}


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

public final class extension_resolver$fn__14322
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extension-resolver", (String)"load-preds");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"config-resource");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
    }

    public Object invoke() {
        return extension_resolver$fn__14322.invokeStatic();
    }
}


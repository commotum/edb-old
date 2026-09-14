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

public final class cli$fail
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__1 = RT.var((String)"datomic.cli", (String)"failed");

    public static Object invokeStatic(Object msg) {
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)Boolean.TRUE);
        Object object = null;
        return msg;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cli$fail.invokeStatic(object2);
    }
}


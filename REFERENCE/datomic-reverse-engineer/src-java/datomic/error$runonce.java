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
import datomic.error$runonce$fn__704;

public final class error$runonce
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");

    public static Object invokeStatic(Object f) {
        Object sentinel = new Object();
        Object result2 = ((IFn)const__0.getRawRoot()).invoke(sentinel);
        Object object = f;
        f = null;
        Object object2 = result2;
        result2 = null;
        Object object3 = sentinel;
        sentinel = null;
        return new error$runonce$fn__704(object, object2, object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$runonce.invokeStatic(object2);
    }
}


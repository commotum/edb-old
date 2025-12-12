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

public final class assert$assertion_repl$stashing_eval__20652
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"eval");
    public static final Var const__1 = RT.var((String)"datomic.assert", (String)"*result*");

    public Object invoke(Object x) {
        Object object = x;
        x = null;
        Object result2 = ((IFn)const__0.getRawRoot()).invoke(object);
        const__1.set(result2);
        Object var2_2 = null;
        return result2;
    }
}


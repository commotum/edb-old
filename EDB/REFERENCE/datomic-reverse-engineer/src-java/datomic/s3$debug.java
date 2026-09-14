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

public final class s3$debug
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.pprint", (String)"pprint");

    public static Object invokeStatic(Object x) {
        ((IFn)const__0.getRawRoot()).invoke(x);
        Object object = null;
        return x;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3$debug.invokeStatic(object2);
    }
}


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

public final class aggregation$sample
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.math", (String)"reservoir-sample");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"set");

    public static Object invokeStatic(Object n, Object coll) {
        Object object = n;
        n = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aggregation$sample.invokeStatic(object3, object4);
    }
}


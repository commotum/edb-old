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

public final class aggregation$sum
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"+");
    public static final Object const__2 = 0L;

    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), const__2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$sum.invokeStatic(object2);
    }
}


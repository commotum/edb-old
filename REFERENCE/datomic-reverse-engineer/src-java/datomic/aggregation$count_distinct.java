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

public final class aggregation$count_distinct
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"set");

    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        return RT.count((Object)((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$count_distinct.invokeStatic(object2);
    }
}


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

public final class api$query
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"query");

    public static Object invokeStatic(Object query_map2) {
        Object object = query_map2;
        query_map2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$query.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Var;

public final class query$cache
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"query-cache");

    public static Object invokeStatic(Object q2) {
        Object object = q2;
        q2 = null;
        return RT.get((Object)const__1.getRawRoot(), (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$cache.invokeStatic(object2);
    }
}


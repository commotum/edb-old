/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$growvec
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj");

    public static Object invokeStatic(Object v, Object n) {
        Object object = v;
        v = null;
        Object v2 = object;
        long i = RT.count((Object)v2);
        while (Numbers.lte((long)i, (Object)n)) {
            Object object2 = v2;
            v2 = null;
            ++i;
            v2 = ((IFn)const__2.getRawRoot()).invoke(object2, null);
        }
        Object var2_2 = null;
        return v2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$growvec.invokeStatic(object3, object4);
    }
}


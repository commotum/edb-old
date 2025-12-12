/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$DLO
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

public final class slf4j$format_as_msec
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.math", (String)"round");

    public static Object invokeStatic(Object nsec) {
        Object object = nsec;
        nsec = null;
        return ((IFn.DLO)const__0.getRawRoot()).invokePrim(Numbers.divide((double)RT.doubleCast((Object)object), (long)1000000L), 3L);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$format_as_msec.invokeStatic(object2);
    }
}


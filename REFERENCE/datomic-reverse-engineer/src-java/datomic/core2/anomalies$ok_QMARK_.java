/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class anomalies$ok_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"datomic.core2.anomalies", (String)"anom");

    public static Object invokeStatic(Object x) {
        Object object = x;
        x = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return anomalies$ok_QMARK_.invokeStatic(object2);
    }
}


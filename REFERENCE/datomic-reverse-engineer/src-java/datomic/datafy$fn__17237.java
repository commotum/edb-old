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

public final class datafy$fn__17237
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"object-to-data");

    public static Object invokeStatic(Object o) {
        Object object = o;
        o = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$fn__17237.invokeStatic(object2);
    }
}


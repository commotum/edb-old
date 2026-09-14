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

public final class common$getx_in
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");

    public static Object invokeStatic(Object m, Object ks) {
        Object object = m;
        m = null;
        Object object2 = ks;
        ks = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$getx_in.invokeStatic(object3, object4);
    }
}


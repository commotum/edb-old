/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazilyPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazilyPersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$tuple
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"to-array");

    public static Object invokeStatic(Object arr) {
        Object array;
        Object object = arr;
        arr = null;
        Object object2 = array = ((IFn)const__0.getRawRoot()).invoke(object);
        array = null;
        return LazilyPersistentVector.createOwning((Object[])((Object[])object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$tuple.invokeStatic(object2);
    }
}


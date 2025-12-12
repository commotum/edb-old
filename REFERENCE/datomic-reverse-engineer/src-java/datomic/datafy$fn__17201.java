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

public final class datafy$fn__17201
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"type-descriptor");

    public static Object invokeStatic(Object _, Object _2, Object type) {
        Object object = type;
        type = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datafy$fn__17201.invokeStatic(object4, object5, object6);
    }
}


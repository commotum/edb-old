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

public final class datalog$fn__18215
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"extrel-coll");

    public static Object invokeStatic(Object src, Object consts, Object _, Object _2) {
        Object object = src;
        src = null;
        Object object2 = consts;
        consts = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return datalog$fn__18215.invokeStatic(object5, object6, object7, object8);
    }
}


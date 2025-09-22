/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.excise$create_e__GT_xpreds$fn__14850;

public final class excise$create_e__GT_xpreds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object db2, Object specs) {
        Object object = db2;
        db2 = null;
        Object object2 = specs;
        specs = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new excise$create_e__GT_xpreds$fn__14850(object), (Object)PersistentArrayMap.EMPTY, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$create_e__GT_xpreds.invokeStatic(object3, object4);
    }
}


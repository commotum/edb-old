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
import datomic.cli$unique_index$fn__20666;

public final class cli$unique_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cli", (String)"unique-index");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object xrel, Object k, Object v) {
        Object object = k;
        k = null;
        Object object2 = v;
        v = null;
        Object object3 = xrel;
        xrel = null;
        return ((IFn)const__1.getRawRoot()).invoke((Object)new cli$unique_index$fn__20666(object, object2), (Object)PersistentArrayMap.EMPTY, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cli$unique_index.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object xrel, Object k) {
        Object object = xrel;
        xrel = null;
        Object object2 = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cli$unique_index.invokeStatic(object3, object4);
    }
}


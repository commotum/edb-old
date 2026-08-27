/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.ddb_values$split_map_keys_with$fn__20373;

public final class ddb_values$split_map_keys_with
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object m, Object pred2) {
        Object object = pred2;
        pred2 = null;
        Object object2 = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new ddb_values$split_map_keys_with$fn__20373(object), (Object)Tuple.create((Object)PersistentArrayMap.EMPTY, (Object)PersistentArrayMap.EMPTY), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb_values$split_map_keys_with.invokeStatic(object3, object4);
    }
}


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
import datomic.db$create_attr_pred$fn__13097;
import datomic.db$create_attr_pred$fn__13099;

public final class db$create_attr_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object kw, Object fn_names) {
        Object fn_map;
        Object object = fn_names;
        fn_names = null;
        Object object2 = fn_map = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new db$create_attr_pred$fn__13097()), object);
        fn_map = null;
        Object object3 = kw;
        kw = null;
        return new db$create_attr_pred$fn__13099(object2, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$create_attr_pred.invokeStatic(object3, object4);
    }
}


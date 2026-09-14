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

public final class db$normalize_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"associative?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into");

    public static Object invokeStatic(Object m) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(m);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = m;
            m = null;
        } else {
            Object object3 = m;
            m = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, object3);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$normalize_map.invokeStatic(object2);
    }
}


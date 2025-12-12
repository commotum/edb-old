/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$get_hook
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"hooks");

    public static Object invokeStatic(Object id) {
        Object object;
        if (Numbers.lt((Object)id, (long)RT.count((Object)const__2.getRawRoot()))) {
            Object object2 = id;
            id = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$get_hook.invokeStatic(object2);
    }
}


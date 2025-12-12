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
import datomic.Database;

public final class integrity$get_db
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.api", (String)"db");

    public static Object invokeStatic(Object o) {
        Object object;
        if (o instanceof Database) {
            object = o;
            o = null;
        } else {
            Object object2 = o;
            o = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$get_db.invokeStatic(object2);
    }
}


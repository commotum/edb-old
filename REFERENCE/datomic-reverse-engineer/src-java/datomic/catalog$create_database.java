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

public final class catalog$create_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"create-database*");

    public static Object invokeStatic(Object system_cluster2, Object desc) {
        Object object = system_cluster2;
        system_cluster2 = null;
        Object object2 = desc;
        desc = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$create_database.invokeStatic(object3, object4);
    }
}


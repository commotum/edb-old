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

public final class coordination$fn__11665
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.coordination", (String)"init-dev");

    public static Object invokeStatic(Object cluster_map, Object data_dir) {
        Object object = cluster_map;
        cluster_map = null;
        Object object2 = data_dir;
        data_dir = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return coordination$fn__11665.invokeStatic(object3, object4);
    }
}


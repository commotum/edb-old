/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Var;

public final class coordination$resolve_db_name
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"db-cache");

    public static Object invokeStatic(Object cluster_conf) {
        Object object = cluster_conf;
        cluster_conf = null;
        return RT.get((Object)const__1.getRawRoot(), (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$resolve_db_name.invokeStatic(object2);
    }
}


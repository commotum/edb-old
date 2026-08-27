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

public final class clusterfs$all_keys
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.clusterfs", (String)"chunk-keys");
    public static final Var const__1 = RT.var((String)"datomic.clusterfs", (String)"files");

    public static Object invokeStatic(Object clusterfs2) {
        Object object = clusterfs2;
        Object object2 = clusterfs2;
        clusterfs2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return clusterfs$all_keys.invokeStatic(object2);
    }
}


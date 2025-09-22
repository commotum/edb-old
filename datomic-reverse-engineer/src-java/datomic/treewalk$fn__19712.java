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

public final class treewalk$fn__19712
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.clusterfs", (String)"all-keys");

    public static Object invokeStatic(Object clusterfs2) {
        Object object = clusterfs2;
        clusterfs2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return treewalk$fn__19712.invokeStatic(object2);
    }
}


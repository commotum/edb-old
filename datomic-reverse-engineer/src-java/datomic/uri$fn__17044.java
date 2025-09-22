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

public final class uri$fn__17044
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"create-h2");

    public static Object invokeStatic(Object cluster_conf) {
        Object object = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17044.invokeStatic(object2);
    }
}


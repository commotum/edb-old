/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class coordination_ext$fn__16743
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__1 = (AFn)Symbol.intern((String)"datomic.ddb-s3-cluster", (String)"create-connection");

    public static Object invokeStatic(Object cluster_conf) {
        Object object = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination_ext$fn__16743.invokeStatic(object2);
    }
}


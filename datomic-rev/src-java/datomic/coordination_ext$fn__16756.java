/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class coordination_ext$fn__16756
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__1 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__2 = (AFn)Symbol.intern((String)"datomic.kv-couchbase", (String)"kv-couchbase");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"host"), (Object)RT.keyword(null, (String)"bucket"), (Object)RT.keyword(null, (String)"password"));

    public static Object invokeStatic(Object cluster_conf) {
        Object object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, ((IFn)const__3.getRawRoot()).invoke(cluster_conf, (Object)const__7));
        Object object2 = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination_ext$fn__16756.invokeStatic(object2);
    }
}


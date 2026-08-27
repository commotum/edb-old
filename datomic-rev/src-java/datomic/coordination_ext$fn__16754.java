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

public final class coordination_ext$fn__16754
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__10 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"host"), RT.keyword(null, (String)"port"), RT.keyword(null, (String)"table"), RT.keyword(null, (String)"user"), RT.keyword(null, (String)"password"), RT.keyword(null, (String)"local-datacenter"), RT.keyword(null, (String)"session"), RT.keyword(null, (String)"ssl"), RT.keyword(null, (String)"session-callback")});
    public static final Var const__11 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__12 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"datomic.kv-cassandra3", (String)"kv-cassandra");

    public static Object invokeStatic(Object cluster_conf) {
        Object endpoint;
        Object object = endpoint = ((IFn)const__0.getRawRoot()).invoke(cluster_conf, (Object)const__10);
        endpoint = null;
        Object object2 = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)const__13, object), object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination_ext$fn__16754.invokeStatic(object2);
    }
}


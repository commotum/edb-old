/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class config$memcached_args
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property-map");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"servers"), "datomic.memcachedServers", RT.keyword(null, (String)"username"), "datomic.memcachedUsername", RT.keyword(null, (String)"password"), "datomic.memcachedPassword", RT.keyword(null, (String)"auto-discovery"), "datomic.memcachedAutoDiscovery", RT.keyword(null, (String)"config-timeout-msec"), "datomic.memcachedConfigTimeoutMsec"});

    public static Object invokeStatic() {
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.memcachedServers");
        return object != null && object != Boolean.FALSE ? ((IFn)const__1.getRawRoot()).invoke((Object)const__7) : null;
    }

    public Object invoke() {
        return config$memcached_args.invokeStatic();
    }
}


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

public final class domain$system_cache_olookup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"lookup-with-object-cache");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"lookup-with-inflight-cache");
    public static final Var const__2 = RT.var((String)"datomic.domain", (String)"deserializing-repairing-lookup");

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$system_cache_olookup.invokeStatic(object2);
    }
}


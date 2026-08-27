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

public final class config$local_valcache_enabled_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property");

    public static Object invokeStatic() {
        Object object;
        Object and__5236__auto__850;
        Object object2 = and__5236__auto__850 = ((IFn)const__1.getRawRoot()).invoke((Object)"datomic.valcachePath");
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)const__1.getRawRoot()).invoke((Object)"datomic.valcacheMaxGb");
        } else {
            object = and__5236__auto__850;
            Object var0 = null;
        }
        return RT.booleanCast((Object)object) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke() {
        return config$local_valcache_enabled_QMARK_.invokeStatic();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.config$fn__867$fn__868;

public final class config$fn__867
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property");

    public static Object invokeStatic() {
        Object object;
        Object and__5236__auto__871;
        Object object2 = and__5236__auto__871 = ((IFn)new config$fn__867$fn__868()).invoke();
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)const__1.getRawRoot()).invoke((Object)"datomic.memcachedLib");
        } else {
            object = and__5236__auto__871;
            Object var0 = null;
        }
        return Util.equiv((Object)"folsom", (Object)object) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke() {
        return config$fn__867.invokeStatic();
    }
}


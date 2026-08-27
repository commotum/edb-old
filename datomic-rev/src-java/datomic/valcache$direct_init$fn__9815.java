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

public final class valcache$direct_init$fn__9815
extends AFunction {
    Object shutdown_requested;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");

    public valcache$direct_init$fn__9815(Object object) {
        this.shutdown_requested = object;
    }

    public Object invoke() {
        valcache$direct_init$fn__9815 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.shutdown_requested, (Object)Boolean.TRUE);
    }
}


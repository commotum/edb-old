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

public final class memcached$start_local_memcached_from_config$fn__10058
extends AFunction {
    Object local_memcached_args;
    public static final Var const__0 = RT.var((String)"datomic.memcached", (String)"create-client");

    public memcached$start_local_memcached_from_config$fn__10058(Object object) {
        this.local_memcached_args = object;
    }

    public Object invoke() {
        memcached$start_local_memcached_from_config$fn__10058 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.local_memcached_args);
    }
}


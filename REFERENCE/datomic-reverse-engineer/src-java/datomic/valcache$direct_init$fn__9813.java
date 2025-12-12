/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class valcache$direct_init$fn__9813
extends AFunction {
    Object eviction_interval_secs;
    Object eviction_threshold_mb;
    Object eviction_file_window;
    Object shutdown_requested;
    Object path;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"eviction-loop");

    public valcache$direct_init$fn__9813(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.eviction_interval_secs = object;
        this.eviction_threshold_mb = object2;
        this.eviction_file_window = object3;
        this.shutdown_requested = object4;
        this.path = object5;
    }

    public Object invoke() {
        valcache$direct_init$fn__9813 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.path, (Object)Numbers.multiply((Object)Numbers.multiply((Object)this_.eviction_threshold_mb, (long)1024L), (long)1024L), this_.eviction_interval_secs, this_.eviction_file_window, this_.shutdown_requested);
    }
}


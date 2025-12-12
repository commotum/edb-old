/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.domain.ValcachePoller;

public final class domain$fn__16781$__GT_ValcachePoller__16783
extends AFunction {
    public Object invoke(Object cluster2, Object valcache_group_config, Object server_specs_ref, Object timer) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = valcache_group_config;
        valcache_group_config = null;
        Object object3 = server_specs_ref;
        server_specs_ref = null;
        Object object4 = timer;
        timer = null;
        return new ValcachePoller(object, object2, object3, object4);
    }
}


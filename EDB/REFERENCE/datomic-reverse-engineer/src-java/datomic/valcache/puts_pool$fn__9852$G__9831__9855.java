/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.valcache;

import clojure.lang.AFunction;
import datomic.valcache.puts_pool.PutsPool;

public final class puts_pool$fn__9852$G__9831__9855
extends AFunction {
    public Object invoke(Object gf_____9853, Object gf__k__9854) {
        Object object = gf_____9853;
        gf_____9853 = null;
        Object object2 = gf__k__9854;
        gf__k__9854 = null;
        return ((PutsPool)object).get_queued_put(object2);
    }
}


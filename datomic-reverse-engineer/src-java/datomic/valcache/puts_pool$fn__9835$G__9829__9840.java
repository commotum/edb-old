/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.valcache;

import clojure.lang.AFunction;
import datomic.valcache.puts_pool.PutsPool;

public final class puts_pool$fn__9835$G__9829__9840
extends AFunction {
    public Object invoke(Object gf_____9836, Object gf__k__9837, Object gf__data__9838, Object gf__f__9839) {
        Object object = gf_____9836;
        gf_____9836 = null;
        Object object2 = gf__k__9837;
        gf__k__9837 = null;
        Object object3 = gf__data__9838;
        gf__data__9838 = null;
        Object object4 = gf__f__9839;
        gf__f__9839 = null;
        return ((PutsPool)object).submit(object2, object3, object4);
    }
}


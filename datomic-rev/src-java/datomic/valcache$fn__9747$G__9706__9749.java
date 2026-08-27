/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.valcache.IServer;

public final class valcache$fn__9747$G__9706__9749
extends AFunction {
    public Object invoke(Object gf__s__9748) {
        Object object = gf__s__9748;
        gf__s__9748 = null;
        return ((IServer)object).running_count();
    }
}


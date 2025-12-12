/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.valcache.IServer;

public final class valcache$fn__9725$G__9708__9727
extends AFunction {
    public Object invoke(Object gf__s__9726) {
        Object object = gf__s__9726;
        gf__s__9726 = null;
        return ((IServer)object).pending_count();
    }
}


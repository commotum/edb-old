/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.valcache.IServer;

public final class valcache$fn__9714$G__9704__9716
extends AFunction {
    public Object invoke(Object gf__s__9715) {
        Object object = gf__s__9715;
        gf__s__9715 = null;
        return ((IServer)object).connection_count();
    }
}


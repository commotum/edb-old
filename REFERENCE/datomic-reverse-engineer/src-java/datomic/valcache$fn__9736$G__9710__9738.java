/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.valcache.IServer;

public final class valcache$fn__9736$G__9710__9738
extends AFunction {
    public Object invoke(Object gf__s__9737) {
        Object object = gf__s__9737;
        gf__s__9737 = null;
        return ((IServer)object).handled_count();
    }
}


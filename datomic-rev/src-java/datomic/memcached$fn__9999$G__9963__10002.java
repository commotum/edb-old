/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.memcached.RecoveringClientImpl;

public final class memcached$fn__9999$G__9963__10002
extends AFunction {
    public Object invoke(Object gf_____10000, Object gf__k__10001) {
        Object object = gf_____10000;
        gf_____10000 = null;
        Object object2 = gf__k__10001;
        gf__k__10001 = null;
        return ((RecoveringClientImpl)object).rc_get(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.memcached.RecoveringClientImpl;

public final class memcached$fn__10012$G__9961__10014
extends AFunction {
    public Object invoke(Object gf_____10013) {
        Object object = gf_____10013;
        gf_____10013 = null;
        return ((RecoveringClientImpl)object).rc_shutdown();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.memcached.RecoveringClientImpl;

public final class memcached$fn__9982$G__9965__9987
extends AFunction {
    public Object invoke(Object gf_____9983, Object gf__k__9984, Object gf__ttl__9985, Object gf__v__9986) {
        Object object = gf_____9983;
        gf_____9983 = null;
        Object object2 = gf__k__9984;
        gf__k__9984 = null;
        Object object3 = gf__ttl__9985;
        gf__ttl__9985 = null;
        Object object4 = gf__v__9986;
        gf__v__9986 = null;
        return ((RecoveringClientImpl)object).rc_set(object2, object3, object4);
    }
}


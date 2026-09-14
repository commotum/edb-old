/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.TWatcher;

public final class peer$fn__21417$G__21398__21420
extends AFunction {
    public Object invoke(Object gf_____21418, Object gf__t__21419) {
        Object object = gf_____21418;
        gf_____21418 = null;
        Object object2 = gf__t__21419;
        gf__t__21419 = null;
        return ((TWatcher)object).wait_for_future_t(object2);
    }
}

